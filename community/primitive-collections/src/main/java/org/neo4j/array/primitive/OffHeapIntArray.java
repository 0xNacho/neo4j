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

import static org.neo4j.collection.primitive.Primitive.safeCastLongToInt;

/**
 * Off-heap version of {@link IntArray} using {@code sun.misc.Unsafe}. Supports arrays with length beyond
 * Integer.MAX_VALUE.
 */
public class OffHeapIntArray extends OffHeapNumberArray implements IntArray
{
    private final int defaultValue;

    public OffHeapIntArray( long length, int defaultValue )
    {
        super( length, 2 );
        this.defaultValue = defaultValue;
        clear();
    }

    @Override
    public int get( long index )
    {
        return unsafe.getInt( addressOf( index ) );
    }

    @Override
    public long genericGet( long index )
    {
        // Will this copied code actually perform better than than delegating?
        return unsafe.getInt( addressOf( index ) );
    }

    @Override
    public void set( long index, int value )
    {
        unsafe.putInt( addressOf( index ), value );
    }

    @Override
    public void genericSet( long index, long value )
    {
        set( index, safeCastLongToInt( value ) );
    }

    @Override
    public void clear()
    {
        if ( isByteUniform( defaultValue ) )
        {
            unsafe.setMemory( address, length << shift, (byte)defaultValue );
        }
        else
        {
            for ( long i = 0, adr = address; i < length; i++, adr += stride )
            {
                unsafe.putInt( adr, defaultValue );
            }
        }
    }

    @Override
    public void swap( long fromIndex, long toIndex, int numberOfEntries )
    {
        long fromAddress = addressOf( fromIndex );
        long toAddress = addressOf( toIndex );

        for ( int i = 0; i < numberOfEntries; i++, fromAddress += stride, toAddress += stride )
        {
            int fromValue = unsafe.getInt( fromAddress );
            unsafe.putInt( fromAddress, unsafe.getInt( toAddress ) );
            unsafe.putInt( toAddress, fromValue );
        }
    }

    @Override
    public void remove( long index, int numberOfEntries )
    {
        long address = addressOf( index );
        for ( int i = 0; i < numberOfEntries; i++ )
        {
            unsafe.putInt( address, defaultValue );
            address += stride;
        }
    }

    @Override
    public void genericAnd( long index, long mask )
    {
        long address = addressOf( index );
        int value = unsafe.getInt( address );
        value &= mask;
        unsafe.putInt( address, value );
    }

    @Override
    public void genericOr( long index, long mask )
    {
        long address = addressOf( index );
        int value = unsafe.getInt( address );
        value |= mask;
        unsafe.putInt( address, value );
    }

    @Override
    public void genericXor( long index, long mask )
    {
        long address = addressOf( index );
        int value = unsafe.getInt( address );
        value ^= mask;
        unsafe.putInt( address, value );
    }
}
