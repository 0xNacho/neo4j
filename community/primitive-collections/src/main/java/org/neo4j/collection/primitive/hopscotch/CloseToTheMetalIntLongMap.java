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
package org.neo4j.collection.primitive.hopscotch;

import org.neo4j.array.primitive.IntArray;
import org.neo4j.array.primitive.NumberArrayFactory;
import org.neo4j.collection.primitive.PrimitiveIntLongMap;
import org.neo4j.collection.primitive.PrimitiveIntLongVisitor;

public class CloseToTheMetalIntLongMap extends CloseToTheMetalIntCollection<long[]> implements PrimitiveIntLongMap
{
    private static final long[] NULL = new long[] {-1};

    private final long[] transport = new long[1];

    public CloseToTheMetalIntLongMap( NumberArrayFactory factory )
    {
        super( factory, 4, 1, NULL );
    }

    @Override
    public long put( int key, long value )
    {
        transport[0] = value;
        return _put( key, NULL )[0];
    }

    @Override
    public boolean containsKey( int key )
    {
        return contains( key );
    }

    @Override
    protected long[] getValue( IntArray array, int absIndex )
    {
        transport[0] = getLong( array, absIndex+2 );
        return transport;
    }

    /**
     * Overridden for performance reasons only
     */
    @Override
    public long get( int key )
    {
        return _get( key )[0];
    }

    @Override
    public long remove( int key )
    {
        return _remove( key )[0];
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveIntLongVisitor<E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( (int) key, getValue( array, k )[0] ) )
            {
                return;
            }
        }
    }
}
