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
import org.neo4j.collection.primitive.PrimitiveIntObjectMap;
import org.neo4j.collection.primitive.PrimitiveIntObjectVisitor;

import static org.neo4j.collection.primitive.Primitive.safeCastLongToInt;
import static org.neo4j.collection.primitive.hopscotch.HashFunction.DEFAULT_HASHING;

public class PrimitiveIntObjectHashMap<VALUE> extends HopScotchHashingIntCollection<VALUE> implements PrimitiveIntObjectMap<VALUE>
{
    private VALUE[] values;

    public PrimitiveIntObjectHashMap( HashFunction hashFunction, NumberArrayFactory factory, int initialCapacity,
            Monitor monitor )
    {
        super( hashFunction, factory, 3, null, initialCapacity, monitor );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void newArray( int logicalCapacity )
    {
        super.newArray( logicalCapacity );
        values = (VALUE[]) new Object[ logicalCapacity ];
    }

    @Override
    public VALUE put( int key, VALUE value )
    {
        return _put( key, value );
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
    public boolean containsKey( int key )
    {
        return contains( key );
    }

    @Override
    public VALUE get( int key )
    {
        return _get( key );
    }

    @Override
    public VALUE remove( int key )
    {
        return getAndRemove( key );
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveIntObjectVisitor<VALUE,E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( safeCastLongToInt( key ), getValue( array, i, k ) ) )
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
            PrimitiveIntObjectHashMap<?> that = (PrimitiveIntObjectHashMap<?>) other;
            IntObjEquality<VALUE> equality = new IntObjEquality<VALUE>( that );
            visitEntries( equality );
            return equality.isEqual();
        }
        return false;
    }

    private static class IntObjEquality<T> implements PrimitiveIntObjectVisitor<T,RuntimeException>
    {
        private final PrimitiveIntObjectHashMap other;
        private boolean equal = true;

        public IntObjEquality( PrimitiveIntObjectHashMap that )
        {
            this.other = that;
        }

        @Override
        public boolean visited( int key, T value )
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

    private static class HashCodeComputer<T> implements PrimitiveIntObjectVisitor<T,RuntimeException>
    {
        private int hash = 1337;

        @Override
        public boolean visited( int key, T value ) throws RuntimeException
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
