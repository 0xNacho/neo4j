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
import java.nio.ByteBuffer;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RelationshipGroupRecord;
import org.neo4j.logging.LogProvider;
import org.neo4j.kernel.monitoring.Monitors;

import static org.neo4j.io.pagecache.PagedFile.PF_EXCLUSIVE_LOCK;
import static org.neo4j.io.pagecache.PagedFile.PF_SHARED_LOCK;
import static org.neo4j.kernel.impl.store.PageCursorUtils.read3B;
import static org.neo4j.kernel.impl.store.PageCursorUtils.read6B;
import static org.neo4j.kernel.impl.store.PageCursorUtils.write3B;
import static org.neo4j.kernel.impl.store.PageCursorUtils.write6B;

public class RelationshipGroupStore extends AbstractRecordStore<RelationshipGroupRecord> implements Store
{
    /*
     * 1B  inUse
     * 3B  type
     * 6B  firstOut
     * 6B  firstIn
     * 6B  firstLoop
     * 6B  next
     * 6B  owner
     * 4B  version
     * 4B  version pointer
     * 22B <free>
     *=64B
     */
    public static final int RECORD_SIZE = 64;
    public static final String TYPE_DESCRIPTOR = "RelationshipGroupStore";

    private int denseNodeThreshold;

    public RelationshipGroupStore(
            File fileName,
            Config config,
            IdGeneratorFactory idGeneratorFactory,
            PageCache pageCache,
            FileSystemAbstraction fileSystemAbstraction,
            LogProvider logProvider,
            StoreVersionMismatchHandler versionMismatchHandler,
            Monitors monitors )
    {
        super( fileName, config, IdType.RELATIONSHIP_GROUP, idGeneratorFactory, pageCache,
                fileSystemAbstraction, logProvider, versionMismatchHandler, monitors );
    }

    @Override
    public RelationshipGroupRecord getRecord( long id )
    {
        try ( PageCursor cursor = storeFile.io( pageIdForRecord( id ), PF_SHARED_LOCK ) )
        {
            if ( cursor.next() )
            {
                RelationshipGroupRecord record;
                do
                {
                    record = getRecord( id, cursor );
                } while ( cursor.shouldRetry() );

                if ( record != null )
                {
                    return record;
                }
            }
            throw new InvalidRecordException( "Record[" + id + "] not in use" );
        }
        catch ( IOException e )
        {
            throw new UnderlyingStorageException( e );
        }
    }

    @Override
    public int getNumberOfReservedLowIds()
    {
        return 1;
    }

    @Override
    protected void readAndVerifyBlockSize() throws IOException
    {
        // Read dense node threshold from the first record in the store (reserved for this threshold)
        ByteBuffer buffer = ByteBuffer.allocate( 4 );
        getFileChannel().position( 0 );
        getFileChannel().read( buffer );
        buffer.flip();
        denseNodeThreshold = buffer.getInt();
        if ( denseNodeThreshold < 0 )
        {
            throw new InvalidRecordException( "Illegal block size: " + denseNodeThreshold
                    + " in " + getStorageFileName() );
        }
    }

    private RelationshipGroupRecord getRecord( long id, PageCursor cursor )
    {
        cursor.setOffset( offsetForId( id ) );
        byte inUseByte = cursor.getByte();
        boolean inUse = (inUseByte&0x1) > 0;
        if ( !inUse )
        {
            return null;
        }

        int type = read3B( cursor );
        RelationshipGroupRecord record = new RelationshipGroupRecord( id, type );
        record.setInUse( inUse );
        record.setFirstOut( read6B( cursor ) );
        record.setFirstIn( read6B( cursor ) );
        record.setFirstLoop( read6B( cursor ) );
        record.setNext( read6B( cursor ) );
        record.setOwningNode( read6B( cursor ) );
        cursor.getInt();
        cursor.getInt();
        return record;
    }

    @Override
    public void updateRecord( RelationshipGroupRecord record )
    {
        try ( PageCursor cursor = storeFile.io( pageIdForRecord( record.getId() ), PF_EXCLUSIVE_LOCK ) )
        {
            if ( cursor.next() )
            {
                do
                {
                    updateRecord( record, cursor, false );
                }
                while ( cursor.shouldRetry() );
            }
        }
        catch ( IOException e )
        {
            throw new UnderlyingStorageException( e );
        }
    }

    private void updateRecord( RelationshipGroupRecord record, PageCursor cursor, boolean force )
    {
        long id = record.getId();
        cursor.setOffset( offsetForId( id ) );
        if ( record.inUse() || force )
        {
            cursor.putByte( record.inUse() ? Record.IN_USE.byteValue() : Record.NOT_IN_USE.byteValue() );
            write3B( cursor, record.getType() );
            write6B( cursor, record.getFirstOut() );
            write6B( cursor, record.getFirstIn() );
            write6B( cursor, record.getFirstLoop() );
            write6B( cursor, record.getNext() );
            write6B( cursor, record.getOwningNode() );
            cursor.putInt( 0 );
            cursor.putInt( 0 );
        }
        else
        {
            cursor.putByte( Record.NOT_IN_USE.byteValue() );
            freeId( id );
        }
    }

    @Override
    public RelationshipGroupRecord forceGetRecord( long id )
    {
        try ( PageCursor cursor = storeFile.io( pageIdForRecord( id ), PF_SHARED_LOCK ) )
        {
            if ( cursor.next() )
            {
                RelationshipGroupRecord record;
                do
                {
                    record = getRecord( id, cursor );
                } while ( cursor.shouldRetry() );

                if ( record != null )
                {
                    return record;
                }
            }
            return new RelationshipGroupRecord( id, -1 );
        }
        catch ( IOException e )
        {
            return new RelationshipGroupRecord( id, -1 );
        }
    }

    @Override
    public RelationshipGroupRecord forceGetRaw( long id )
    {
        return forceGetRecord( id );
    }

    @Override
    public void forceUpdateRecord( RelationshipGroupRecord record )
    {
        try ( PageCursor cursor = storeFile.io( pageIdForRecord( record.getId() ), PF_EXCLUSIVE_LOCK ) )
        {
            if ( cursor.next() ) // should always be true
            {
                do
                {
                    updateRecord( record, cursor, true );
                } while ( cursor.shouldRetry() );
            }
        }
        catch ( IOException e )
        {
            throw new UnderlyingStorageException( e );
        }
    }

    @Override
    public RelationshipGroupRecord forceGetRaw( RelationshipGroupRecord record )
    {
        return record;
    }

    @Override
    public <FAILURE extends Exception> void accept( Processor<FAILURE> processor, RelationshipGroupRecord record )
            throws FAILURE
    {
        processor.processRelationshipGroup( this, record );
    }

    @Override
    public int getRecordHeaderSize()
    {
        return getRecordSize();
    }

    @Override
    public int getRecordSize()
    {
        return RECORD_SIZE;
    }

    @Override
    public String getTypeDescriptor()
    {
        return TYPE_DESCRIPTOR;
    }

    public int getDenseNodeThreshold()
    {
        return denseNodeThreshold;
    }
}
