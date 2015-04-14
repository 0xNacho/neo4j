/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.store;

import java.io.File;
import java.io.IOException;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.logging.LogProvider;
import org.neo4j.kernel.monitoring.Monitors;

import static org.neo4j.io.pagecache.PagedFile.PF_EXCLUSIVE_LOCK;
import static org.neo4j.io.pagecache.PagedFile.PF_SHARED_LOCK;
import static org.neo4j.kernel.impl.store.PageCursorUtils.read3B;
import static org.neo4j.kernel.impl.store.PageCursorUtils.read6B;
import static org.neo4j.kernel.impl.store.PageCursorUtils.write3B;
import static org.neo4j.kernel.impl.store.PageCursorUtils.write6B;

/**
 * Implementation of the relationship store.
 */
public class RelationshipStore extends AbstractRecordStore<RelationshipRecord> implements Store
{
    public static abstract class Configuration
        extends AbstractStore.Configuration
    {
    }

    public static final String TYPE_DESCRIPTOR = "RelationshipStore";

    /*
     * 1B inUse + first in start chain + first in end chain
     * 6B start node
     * 6B end node
     * 3B type
     * 6B startPrevRel/chain length
     * 6B startNextRel
     * 6B endPrevRel/chain length
     * 6B endNextRel
     * 6B prop
     * 4B version
     * 4B version pointer
     * 10B <free>
     *=64B
     */
    public static final int RECORD_SIZE = 64;

    public RelationshipStore(
            File fileName,
            Config configuration,
            IdGeneratorFactory idGeneratorFactory,
            PageCache pageCache,
            FileSystemAbstraction fileSystemAbstraction,
            LogProvider logProvider,
            StoreVersionMismatchHandler versionMismatchHandler,
            Monitors monitors )
    {
        super( fileName, configuration, IdType.RELATIONSHIP, idGeneratorFactory,
                pageCache, fileSystemAbstraction, logProvider, versionMismatchHandler, monitors );
    }

    @Override
    public <FAILURE extends Exception> void accept( Processor<FAILURE> processor, RelationshipRecord record ) throws FAILURE
    {
        processor.processRelationship( this, record );
    }

    @Override
    public String getTypeDescriptor()
    {
        return TYPE_DESCRIPTOR;
    }

    @Override
    public int getRecordSize()
    {
        return RECORD_SIZE;
    }

    @Override
    public int getRecordHeaderSize()
    {
        return getRecordSize();
    }

    @Override
    public RelationshipRecord getRecord( long id )
    {
        RelationshipRecord record = new RelationshipRecord( id );
        return fillRecord( id, record, RecordLoad.NORMAL ) ? record : null;
    }

    @Override
    public RelationshipRecord forceGetRecord( long id )
    {
        RelationshipRecord record = new RelationshipRecord( -1 );
        return fillRecord( id, record, RecordLoad.FORCE ) ? record : null;
    }

    @Override
    public RelationshipRecord forceGetRaw( RelationshipRecord record )
    {
        return record;
    }

    @Override
    public RelationshipRecord forceGetRaw( long id )
    {
        return forceGetRecord( id );
    }

    public RelationshipRecord getLightRel( long id )
    {
        RelationshipRecord record = new RelationshipRecord( id );
        return fillRecord( id, record, RecordLoad.CHECK ) ? record : null;
    }

    public boolean fillRecord( long id, RelationshipRecord target, RecordLoad loadMode )
    {
        try ( PageCursor cursor = storeFile.io( pageIdForRecord( id ), PF_SHARED_LOCK ) )
        {
            boolean success = false;
            if ( cursor.next() )
            {
                do
                {
                    success = readRecord( id, cursor, target );
                } while ( cursor.shouldRetry() );
            }

            if ( !success )
            {
                if ( loadMode == RecordLoad.NORMAL )
                {
                    throw new InvalidRecordException( "RelationshipRecord[" + id + "] not in use" );
                }
                else if ( loadMode == RecordLoad.CHECK )
                {
                    return false;
                }
            }
            return true;
        }
        catch ( IOException e )
        {
            throw new UnderlyingStorageException( e );
        }
    }

    public boolean inUse( long id )
    {
        long pageId = pageIdForRecord( id );
        int offset = offsetForId( id );

        try ( PageCursor cursor = storeFile.io( pageId, PF_SHARED_LOCK ) )
        {
            boolean recordIsInUse = false;
            if ( cursor.next() )
            {
                do
                {
                    cursor.setOffset( offset );
                    recordIsInUse = isInUse( cursor.getByte() );
                } while ( cursor.shouldRetry() );
            }
            return recordIsInUse;
        }
        catch ( IOException e )
        {
            throw new UnderlyingStorageException( e );
        }
    }

    @Override
    public void forceUpdateRecord( RelationshipRecord record )
    {
        updateRecord( record, true );
    }

    @Override
    public void updateRecord( RelationshipRecord record )
    {
        updateRecord( record, false );
    }

    private void updateRecord( RelationshipRecord record, boolean force )
    {
        try ( PageCursor cursor = storeFile.io( pageIdForRecord( record.getId() ), PF_EXCLUSIVE_LOCK ) )
        {
            if ( cursor.next() ) // should always be true
            {
                do
                {
                    updateRecord( record, cursor, force );
                } while ( cursor.shouldRetry() );
            }
        }
        catch ( IOException e )
        {
            throw new UnderlyingStorageException( e );
        }
    }

    private void updateRecord( RelationshipRecord record,
        PageCursor cursor, boolean force )
    {
        long id = record.getId();
        cursor.setOffset( offsetForId( id ) );
        if ( record.inUse() || force )
        {
            cursor.putByte( (byte) (
                    (record.isFirstInFirstChain() ? 0x2 : 0) |
                    (record.isFirstInSecondChain() ? 0x4 : 0) |
                    (record.inUse() ? 0x1 : 0) ));
            write6B( cursor, record.getFirstNode() );
            write6B( cursor, record.getSecondNode() );
            write3B( cursor, record.getType() );
            write6B( cursor, record.getFirstPrevRel() );
            write6B( cursor, record.getFirstNextRel() );
            write6B( cursor, record.getSecondPrevRel() );
            write6B( cursor, record.getSecondNextRel() );
            write6B( cursor, record.getNextProp() );
            cursor.putInt( 0 ); // version
            cursor.putInt( 0 ); // version pointer
        }
        else
        {
            cursor.putByte( Record.NOT_IN_USE.byteValue() );
            freeId( id );
        }
    }

    private boolean readRecord( long id, PageCursor cursor,
        RelationshipRecord record )
    {
        cursor.setOffset( offsetForId( id ) );
        byte inUseByte = cursor.getByte();
        record.setId( id );
        record.setInUse( (inUseByte & 0x1) != 0 );
        record.setFirstInFirstChain( (inUseByte & 0x2) != 0 );
        record.setFirstInSecondChain( (inUseByte & 0x4) != 0 );

        record.setFirstNode( read6B( cursor ) );
        record.setSecondNode( read6B( cursor ) );
        record.setType( read3B( cursor ) );
        record.setFirstPrevRel( read6B( cursor ) );
        record.setFirstNextRel( read6B( cursor ) );
        record.setSecondPrevRel( read6B( cursor ) );
        record.setSecondNextRel( read6B( cursor ) );
        record.setNextProp( read6B( cursor ) );
        cursor.getInt();
        cursor.getInt();
        return record.inUse();
    }

    public boolean fillChainRecord( long id, RelationshipRecord record )
    {
        return fillRecord( id, record, RecordLoad.CHECK );
    }
}
