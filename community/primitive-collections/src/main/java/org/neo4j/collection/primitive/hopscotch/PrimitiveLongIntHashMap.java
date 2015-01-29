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

import static org.neo4j.collection.primitive.hopscotch.HashFunction.DEFAULT_HASHING;

public class PrimitiveLongIntHashMap extends HopScotchHashingLongCollection<int[]> implements PrimitiveLongIntMap
{
    private static final int[] NULL = new int[] {-1};
    private final int[] transport = new int[1];

    public PrimitiveLongIntHashMap( HashFunction hashFunction, NumberArrayFactory factory, int initialCapacity )
    {
        super( hashFunction, factory, 4, NULL, initialCapacity );
    }

    @Override
    public int put( long key, int value )
    {
        transport[0] = value;
        return _put( key,transport )[0];
    }

    @Override
    protected int[] getValue( IntArray array, int index, int absIndex )
    {
        transport[0] = array.get( absIndex+3 );
        return transport;
    }

    @Override
    protected void putValue( IntArray array, int index, int absIndex, int[] value )
    {
        array.set( absIndex+3, value[0] );
    }

    @Override
    public boolean containsKey( long key )
    {
        return contains( key );
    }

    @Override
    public int get( long key )
    {
        return _get( key )[0];
    }

    @Override
    public int remove( long key )
    {
        return getAndRemove( key )[0];
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveLongIntVisitor<E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( key, getValue( array, i, k )[0] ) )
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
            PrimitiveLongIntHashMap that = (PrimitiveLongIntHashMap) other;
            LongIntEquality equality = new LongIntEquality( that );
            visitEntries( equality );
            return equality.isEqual();
        }
        return false;
    }

    private static class LongIntEquality implements PrimitiveLongIntVisitor<RuntimeException>
    {
        private final PrimitiveLongIntHashMap other;
        private boolean equal = true;

        public LongIntEquality( PrimitiveLongIntHashMap that )
        {
            this.other = that;
        }

        @Override
        public boolean visited( long key, int value )
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

    private static class HashCodeComputer implements PrimitiveLongIntVisitor<RuntimeException>
    {
        private int hash = 1337;

        @Override
        public boolean visited( long key, int value ) throws RuntimeException
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
