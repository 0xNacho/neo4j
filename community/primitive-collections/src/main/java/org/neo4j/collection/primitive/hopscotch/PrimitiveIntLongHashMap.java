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

import static org.neo4j.collection.primitive.hopscotch.HashFunction.DEFAULT_HASHING;

public class PrimitiveIntLongHashMap extends HopScotchHashingIntCollection<long[]> implements PrimitiveIntLongMap
{
    private static final long[] NULL = new long[] {-1};

    private final long[] transport = new long[1];

    public PrimitiveIntLongHashMap( HashFunction hashFunction, NumberArrayFactory factory, int initialCapacity )
    {
        super( hashFunction, factory, 4, 1, NULL, initialCapacity );
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
    protected long[] getValue( IntArray array, int index, int absIndex )
    {
        transport[0] = getLong( array, absIndex+2 );
        return transport;
    }

    @Override
    protected void putValue( IntArray array, int index, int absIndex, long[] value )
    {
        putLong( array, absIndex+2, value[0] );
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
            if ( isVisible( i, key ) && visitor.visited( (int) key, getValue( array, i, k )[0] ) )
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
            PrimitiveIntLongHashMap that = (PrimitiveIntLongHashMap) other;
            IntLongEquality equality = new IntLongEquality( that );
            visitEntries( equality );
            return equality.isEqual();
        }
        return false;
    }

    private static class IntLongEquality implements PrimitiveIntLongVisitor<RuntimeException>
    {
        private final PrimitiveIntLongHashMap other;
        private boolean equal = true;

        public IntLongEquality( PrimitiveIntLongHashMap that )
        {
            this.other = that;
        }

        @Override
        public boolean visited( int key, long value )
        {
            equal = other.get( key ) == value;
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
        HashCodeComputer hash = new HashCodeComputer();
        visitEntries( hash );
        return hash.hashCode();
    }

    private static class HashCodeComputer implements PrimitiveIntLongVisitor<RuntimeException>
    {
        private int hash = 1337;

        @Override
        public boolean visited( int key, long value ) throws RuntimeException
        {
            hash += DEFAULT_HASHING.hash( key + DEFAULT_HASHING.hash( value ) );
            return false;
        }

        @Override
        public int hashCode()
        {
            return hash;
        }
    }
}
