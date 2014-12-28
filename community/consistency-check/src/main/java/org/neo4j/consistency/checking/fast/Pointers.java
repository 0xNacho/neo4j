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
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.PropertyRecord;

public class Pointers
{
    public static final FunctionToPrimitiveLong<NodeRecord> NODE_FIRST_PROPERTY = new FunctionToPrimitiveLong<NodeRecord>()
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

    public static ValueChecker CHECK_IN_USE = new ValueChecker()
    {
        @Override
        public void check( long fromId, long toId, long toValue )
        {
            // TODO implement for real
            assert (toValue&1) == 1;
        }
    };
}
