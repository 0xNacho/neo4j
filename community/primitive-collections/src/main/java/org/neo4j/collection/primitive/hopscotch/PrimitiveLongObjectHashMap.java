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
import org.neo4j.collection.primitive.PrimitiveLongObjectMap;
import org.neo4j.collection.primitive.PrimitiveLongObjectVisitor;

import static org.neo4j.collection.primitive.hopscotch.HashFunction.DEFAULT_HASHING;

public class PrimitiveLongObjectHashMap<VALUE> extends HopScotchHashingLongCollection<VALUE> implements PrimitiveLongObjectMap<VALUE>
{
    private VALUE[] values;

    public PrimitiveLongObjectHashMap( HashFunction hashFunction, NumberArrayFactory factory, int initialCapacity,
            Monitor monitor )
    {
        super( hashFunction, factory, 3, null, initialCapacity, monitor );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void newArray( int logicalCapacity )
    {
        super.newArray( logicalCapacity );
        values = (VALUE[]) new Object[logicalCapacity];
    }

    @Override
    protected VALUE getValue( IntArray array, int index, int absIndex )
    {
        return values[index];
    }

    @Override
    protected VALUE putValue( IntArray array, int index, int absIndex, VALUE value )
    {
        VALUE prev = values[index];
        values[index] = value;
        return prev;
    }

    @Override
    public VALUE put( long key, VALUE value )
    {
        return _put( key, value );
    }

    @Override
    public boolean containsKey( long key )
    {
        return contains( key );
    }

    @Override
    public VALUE get( long key )
    {
        return _get( key );
    }

    @Override
    public VALUE remove( long key )
    {
        return getAndRemove( key );
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveLongObjectVisitor<VALUE,E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( key, getValue( array, i, k ) ) )
            {
                return;
            }
        }
    }

    @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" ) // yes it does
    @Override
    public boolean equals( Object other )
    {
        if ( typeAndSizeEqual( other ) )
        {
            PrimitiveLongObjectHashMap<?> that = (PrimitiveLongObjectHashMap<?>) other;
            LongObjEquality<VALUE> equality = new LongObjEquality<VALUE>( that );
            visitEntries( equality );
            return equality.isEqual();
        }
        return false;
    }

    private static class LongObjEquality<T> implements PrimitiveLongObjectVisitor<T,RuntimeException>
    {
        private final PrimitiveLongObjectHashMap other;
        private boolean equal = true;

        public LongObjEquality( PrimitiveLongObjectHashMap that )
        {
            this.other = that;
        }

        @Override
        public boolean visited( long key, T value )
        {
            Object otherValue = other.get( key );
            equal = otherValue == value || (otherValue != null && otherValue.equals( value ) );
            return !equal;
        }

        public boolean isEqual()
        {
            return equal;
        }
    }

    @Override
    public int hashCode()
    {
        HashCodeComputer<VALUE> hash = new HashCodeComputer<VALUE>();
        visitEntries( hash );
        return hash.hashCode();
    }

    private static class HashCodeComputer<T> implements PrimitiveLongObjectVisitor<T,RuntimeException>
    {
        private int hash = 1337;

        @Override
        public boolean visited( long key, T value ) throws RuntimeException
        {
            hash += DEFAULT_HASHING.hash( key + value.hashCode() );
            return false;
        }

        @Override
        public int hashCode()
        {
            return hash;
        }
    }
}
