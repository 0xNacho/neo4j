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
import org.neo4j.helpers.Predicate;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.unsafe.impl.batchimport.cache.LongArray;
import org.neo4j.unsafe.impl.batchimport.cache.LongBitsManipulator;
import org.neo4j.unsafe.impl.batchimport.cache.NumberArrayFactory;

public class Checkers
{
    public static final FunctionToPrimitiveLong<NodeRecord> NODE_FIRST_PROPERTY =
            new FunctionToPrimitiveLong<NodeRecord>()
    {
        @Override
        public long apply( NodeRecord value )
        {
            return value.getNextProp();
        }
    };

    public static FunctionToPrimitiveLong<PropertyRecord> IN_USE = new FunctionToPrimitiveLong<PropertyRecord>()
    {
        @Override
        public long apply( PropertyRecord value )
        {
            return value.inUse() ? 1 : 0;
        }
    };

    @SuppressWarnings( "rawtypes" )
    private static ValueChecker CHECK_IN_USE = new ValueChecker()
    {
        @Override
        public boolean check( AbstractBaseRecord record, long toId, long toValue )
        {
            // TODO implement for real
            assert (toValue&1) == 1;
            return false;
        }
    };

    @SuppressWarnings( "unchecked" )
    public static <RECORD extends AbstractBaseRecord> ValueChecker<RECORD> checkInUse()
    {
        return CHECK_IN_USE;
    }

    private static final LongBitsManipulator CHAIN_MARKERS = new LongBitsManipulator( 62, 1, 1 );

    public static <RECORD extends AbstractBaseRecord> ValueChecker<RECORD> checkSingleLinkedChain(
            final Predicate<RECORD> isStartOfChain )
    {
        return new ValueChecker<RECORD>()
        {
            private final LongArray cache = NumberArrayFactory.AUTO.newDynamicLongArray( 1_000_000, -1 );

            /**
             * @return {@code true } if pointer was advanced, otherwise {@code false} .
             */
            @Override
            public boolean check( RECORD atRecord, long toId, long key )
            {
                long expectancy = cache.get( key );
                if ( expectancy == -1 )
                {   // we're looking for the start of the chain
                    if ( atRecord.inUse() && isStartOfChain.accept( atRecord ) )
                    {   // start of chain found
                        expectancy = CHAIN_MARKERS.set( toId, 1, 1 );
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {   // step in the chain
                    if ( CHAIN_MARKERS.get( expectancy, 0 ) == atRecord.getLongId() )
                    {   // we're at the expected next step in the chain
                        // TODO do verifications like inUse
                        // TODO also possibility to hook in prev pointer checking here?
                        expectancy = CHAIN_MARKERS.set( expectancy, 0, toId );
                    }
                    else
                    {
                        return false;
                    }
                }

                // check end of chain
                if ( expectancy != -1 && toId == -1 )
                {   // yup, we're at the end
                    expectancy = CHAIN_MARKERS.set( expectancy, 2, 1 );
                }

                cache.set( key, expectancy );
                return true;
            }
        };
    }
}
