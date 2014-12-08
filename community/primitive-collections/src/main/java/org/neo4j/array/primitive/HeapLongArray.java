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
package org.neo4j.array.primitive;

import java.util.Arrays;

import static org.neo4j.collection.primitive.Primitive.safeCastLongToInt;

/**
 * A {@code long[]} on heap, abstracted into a {@link LongArray}.
 */
public class HeapLongArray extends HeapNumberArray implements LongArray
{
    private final long[] array;
    private final long defaultValue;

    public HeapLongArray( int length, long defaultValue )
    {
        super( 8 );
        this.defaultValue = defaultValue;
        this.array = new long[length];
        clear();
    }

    @Override
    public long length()
    {
        return array.length;
    }

    @Override
    public long get( long index )
    {
        return array[safeCastLongToInt( index )];
    }

    @Override
    public long genericGet( long index )
    {
        // Will this copied code actually perform better than than delegating?
        return array[safeCastLongToInt( index )];
    }

    @Override
    public void set( long index, long value )
    {
        array[safeCastLongToInt( index )] = value;
    }

    @Override
    public void genericSet( long index, long value )
    {
        set( index, value );
    }

    @Override
    public void clear()
    {
        Arrays.fill( array, defaultValue );
    }

    @Override
    public void swap( long fromIndex, long toIndex, int numberOfEntries )
    {
        for ( int i = 0; i < numberOfEntries; i++ )
        {
            long fromValue = get( fromIndex+i );
            set( fromIndex+i, get( toIndex+i ) );
            set( toIndex+i, fromValue );
        }
    }

    @Override
    public void remove( long index, int numberOfEntries )
    {
        int intIndex = safeCastLongToInt( index );
        Arrays.fill( array, intIndex, intIndex+2, defaultValue );
    }

    @Override
    public void genericAnd( long index, long mask )
    {
        array[safeCastLongToInt( index )] &= mask;
    }

    @Override
    public void genericOr( long index, long mask )
    {
        array[safeCastLongToInt( index )] |= mask;
    }

    @Override
    public void genericXor( long index, long mask )
    {
        array[safeCastLongToInt( index )] ^= mask;
    }
}
