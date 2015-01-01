/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.consistency.checking.fast;

import org.neo4j.function.primitive.FunctionToPrimitiveLong;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.kernel.impl.store.format.Store;
import org.neo4j.kernel.impl.store.format.Store.RecordCursor;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.unsafe.impl.batchimport.cache.LongArray;
import org.neo4j.unsafe.impl.batchimport.cache.NumberArrayFactory;

/**
 * Checks consistency between stores, pointers (ids) from one store pointing to another.
 */
public class CrossStorePointerChecker<FROM extends AbstractBaseRecord,TO extends AbstractBaseRecord>
        implements Checker
{
    private final Store<FROM,RecordCursor<FROM>> from;
    private final FunctionToPrimitiveLong<FROM> fromKey;
    private final Store<TO,RecordCursor<TO>> to;
    private final FunctionToPrimitiveLong<TO> toKey;
    private final ValueChecker checker;
    private final LongArray cache;

    public CrossStorePointerChecker(
            Store<FROM,RecordCursor<FROM>> from, FunctionToPrimitiveLong<FROM> fromKey,
            Store<TO,RecordCursor<TO>> to, FunctionToPrimitiveLong<TO> toKey,
            ValueChecker checker )
    {
        this.from = from;
        this.fromKey = fromKey;
        this.to = to;
        this.toKey = toKey;
        this.checker = checker;
        this.cache = NumberArrayFactory.AUTO.newDynamicLongArray( 1_000_000, -1 );
    }

    @Override
    public void check()
    {
        long toLowId = to.numberOfReservedIds();
        long toHighId = 20; // TODO use later for multi-passing if memory is low

        // cache "to" side
        RecordCursor<TO> toCursor = to.cursor( PagedFile.PF_READ_AHEAD );
        toCursor.position( toLowId );
        while ( toCursor.next() )
        {
            TO toRecord = toCursor.record();
            if ( toRecord.getLongId() >= toHighId )
            {
                break;
            }
            cache.set( offset( toLowId, toRecord.getLongId() ), toKey.apply( toRecord ) );
        }

        // verify "from" side
        RecordCursor<FROM> fromCursor = from.cursor( PagedFile.PF_READ_AHEAD );
        long fromHighId = 20;
        while ( fromCursor.next() )
        {
            FROM fromRecord = fromCursor.record();
            if ( fromRecord.getLongId() >= fromHighId )
            {
                break;
            }
            long toId = fromKey.apply( fromRecord );
            if ( toId >= toLowId && toId <= toHighId )
            {
                long to = cache.get( offset( toLowId, toId ) );
                checker.check( fromRecord.getLongId(), toId, to );
            }
        }
    }

    private long offset( long offset, long id )
    {
        return id - offset;
    }
}
