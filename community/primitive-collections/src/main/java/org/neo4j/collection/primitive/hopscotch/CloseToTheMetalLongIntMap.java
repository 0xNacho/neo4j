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
import org.neo4j.collection.primitive.PrimitiveLongIntMap;
import org.neo4j.collection.primitive.PrimitiveLongIntVisitor;

import static java.lang.Integer.numberOfTrailingZeros;

public class CloseToTheMetalLongIntMap extends CloseToTheMetalLongCollection<int[]> implements PrimitiveLongIntMap
{
    private static final int[] NULL = new int[] {-1};
    private final int[] transport = new int[1];

    public CloseToTheMetalLongIntMap( NumberArrayFactory factory )
    {
        super( factory, 4, 2, NULL );
    }

    @Override
    public int put( long key, int value )
    {
        transport[0] = value;
        return _put( key,transport )[0];
    }

    @Override
    protected int[] getValue( IntArray array, int absIndex )
    {
        transport[0] = array.get( absIndex+3 );
        return transport;
    }

    @Override
    public boolean containsKey( long key )
    {
        return contains( key );
    }

    @Override
    public int get( long key )
    {
        int index = indexOf( key );
        int absIndex = index( index );
        long existingKey = getKey( array, absIndex );
        if ( existingKey == key )
        {   // Bulls eye
            return array.get( absIndex+3 );
        }

        // Look in its neighborhood
        int hopBits = array.get( absIndex+itemsPerKey );
        while ( hopBits > 0 )
        {
            int hopIndex = nextIndex( index, numberOfTrailingZeros( hopBits )+1 );
            if ( array.get( index( hopIndex )) == key )
            {   // There it is
                return array.get( absIndex+3 );
            }
            hopBits &= hopBits-1;
        }

        return -1;
    }

    @Override
    public int remove( long key )
    {
        return _remove( key )[0];
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveLongIntVisitor<E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( key, getValue( array, k )[0] ) )
            {
                return;
            }
        }
    }
}
