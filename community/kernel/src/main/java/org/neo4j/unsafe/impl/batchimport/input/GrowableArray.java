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
package org.neo4j.unsafe.impl.batchimport.input;

import java.util.Arrays;

import org.neo4j.helpers.ArrayUtil;

import static java.lang.Math.max;
import static java.lang.reflect.Array.newInstance;
import static java.util.Arrays.copyOf;

public class GrowableArray<T>
{
    private T[] array;
    private int cursor;

    @SuppressWarnings( "unchecked" )
    public GrowableArray( Class<T> klass, int initialSize )
    {
        this.array = (T[]) newInstance( klass, initialSize );
    }

    @SafeVarargs
    public GrowableArray( T... initialItems )
    {
        this.array = initialItems;
        this.cursor = this.array.length;
    }

    public void add( T item )
    {
        ensureCapacity( cursor+1 );
        array[cursor++] = item;
    }

    private void ensureCapacity( int capacity )
    {
        if ( capacity > array.length )
        {
            array = copyOf( array, max( cursor*2, capacity ) );
        }
    }

    public void addAll( T[] items )
    {
        ensureCapacity( cursor+items.length );
        System.arraycopy( items, 0, array, cursor, items.length );
        cursor += items.length;
    }

    public void addMissing( T[] items )
    {
        for ( T item : items )
        {
            if ( !ArrayUtil.contains( array, cursor, item ) )
            {
                add( item );
            }
        }
    }

    public int length()
    {
        return cursor;
    }

    public T get( int i )
    {
        return array[i];
    }

    public void set( int i, T item )
    {
        array[i] = item;
    }

    public void clear()
    {
        cursor = 0;
    }

    public boolean remove( T item )
    {
        for ( int i = 0; i < cursor; i++ )
        {
            if ( item.equals( array[i] ) )
            {
                if ( cursor >= i-1 )
                {
                    array[i] = array[cursor-1];
                }
                cursor--;
                return true;
            }
        }
        return false;
    }

    public boolean contains( T item )
    {
        for ( int i = 0; i < cursor; i++ )
        {
            if ( array[i].equals( item ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return toString( "[", ",", "]" );
    }

    public String toString( String before, String delimiter, String after )
    {
        StringBuilder builder = new StringBuilder( before );
        for ( int i = 0; i < cursor; i++ )
        {
            if ( i > 0 )
            {
                builder.append( delimiter );
            }
            builder.append( array[i] );
        }
        return builder.append( after ).toString();
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private static final GrowableArray EMPTY = new GrowableArray( Object.class, 0 )
    {
        @Override
        public void add( Object item )
        {
            throw new UnsupportedOperationException( "EMPTY instance" );
        }

        @Override
        public void addAll( Object[] items )
        {
            throw new UnsupportedOperationException( "EMPTY instance" );
        }

        @Override
        public void addMissing( Object[] items )
        {
            throw new UnsupportedOperationException( "EMPTY instance" );
        }

        @Override
        public void set( int i, Object item )
        {
            throw new UnsupportedOperationException( "EMPTY instance" );
        }
    };

    @SuppressWarnings( "unchecked" )
    public static <T> GrowableArray<T> empty()
    {
        return EMPTY;
    }

    public void mirrorFrom( GrowableArray<String> other )
    {
        ensureCapacity( other.cursor );
        System.arraycopy( other.array, 0, array, 0, other.cursor );
        cursor = other.cursor;
    }

    public T[] copyToArray()
    {
        return Arrays.copyOf( array, cursor );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        for ( int i = 0; i < cursor; i++ )
        {
            result = prime * result + array[i].hashCode();
        }
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null || getClass() != obj.getClass() )
        {
            return false;
        }
        @SuppressWarnings( "unchecked" )
        GrowableArray<T> other = (GrowableArray<T>) obj;
        if ( cursor != other.cursor )
        {
            return false;
        }
        for ( int i = 0; i < cursor; i++ )
        {
            if ( !array[i].equals( other.array[i] ) )
            {
                return false;
            }
        }
        return true;
    }
}
