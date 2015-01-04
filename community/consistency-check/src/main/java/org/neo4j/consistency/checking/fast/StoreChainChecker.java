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
package org.neo4j.consistency.checking.fast;

import org.neo4j.function.primitive.FunctionToPrimitiveLong;
import org.neo4j.kernel.impl.store.format.Store;
import org.neo4j.kernel.impl.store.format.Store.RecordCursor;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;

/**
 * Checks consistency of pointers within a store, chains of pointers.
 */
public class StoreChainChecker<RECORD extends AbstractBaseRecord> implements Checker
{
    private final Store<RECORD,RecordCursor<RECORD>> store;

    // for example node id
    private final FunctionToPrimitiveLong<RECORD> key;

    // for example first node next
    private final FunctionToPrimitiveLong<RECORD> next;

    private final ValueChecker<RECORD> checker;

    public StoreChainChecker( Store<RECORD,Store.RecordCursor<RECORD>> store,
            FunctionToPrimitiveLong<RECORD> key,
            FunctionToPrimitiveLong<RECORD> next,
            ValueChecker<RECORD> checker )
    {
        this.store = store;
        this.key = key;
        this.next = next;
        this.checker = checker;
    }

    @Override
    public void check()
    {
        long toLowId = store.numberOfReservedIds();
        long toHighId = Long.MAX_VALUE; // TODO use later for multi-passing if memory is low
        RecordCursor<RECORD> cursor = store.cursor( Store.SF_SCAN, false );

        boolean chainsAreTraversed = true;
        while ( chainsAreTraversed )
        {
            cursor.position( toLowId );

            chainsAreTraversed = false;
            while ( cursor.next() )
            {
                RECORD record = cursor.record();
                if ( record.getLongId() >= toHighId )
                {
                    break;
                }

                long key = this.key.apply( record );
                if ( checker.check( record, next.apply( record ), key ) )
                {
                    chainsAreTraversed = true;
                }
            }
        }

        // TODO extra verification here, like if there are uncompleted chains and what not
    }
}
