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
import org.neo4j.collection.primitive.PrimitiveLongLongMap;
import org.neo4j.collection.primitive.PrimitiveLongLongVisitor;

import static org.neo4j.collection.primitive.hopscotch.HashFunction.DEFAULT_HASHING;

public class PrimitiveLongLongHashMap extends HopScotchHashingLongCollection<long[]> implements PrimitiveLongLongMap
{
    private static final long[] NULL = new long[] {-1};
    private final long[] transport = new long[1];

    public PrimitiveLongLongHashMap( HashFunction hashFunction, NumberArrayFactory factory, int initialCapacity )
    {
        super( hashFunction, factory, 5, 2, NULL, initialCapacity );
    }

    @Override
    public long put( long key, long value )
    {
        transport[0] = value;
        return _put( key, transport )[0];
    }

    @Override
    protected long[] getValue( IntArray array, int index, int absIndex )
    {
        transport[0] = getLong( array, absIndex+3 );
        return transport;
    }

    @Override
    protected void putValue( IntArray array, int index, int absIndex, long[] value )
    {
        putLong( array, absIndex+3, value[0] );
    }

    @Override
    public boolean containsKey( long key )
    {
        return contains( key );
    }

    @Override
    public long get( long key )
    {
        return _get( key )[0];
    }

    @Override
    public long remove( long key )
    {
        return _remove( key )[0];
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveLongLongVisitor<E> visitor ) throws E
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
            PrimitiveLongLongHashMap that = (PrimitiveLongLongHashMap) other;
            LongLongEquality equality = new LongLongEquality( that );
            visitEntries( equality );
            return equality.isEqual();
        }
        return false;
    }

    private static class LongLongEquality implements PrimitiveLongLongVisitor<RuntimeException>
    {
        private final PrimitiveLongLongHashMap other;
        private boolean equal = true;

        public LongLongEquality( PrimitiveLongLongHashMap that )
        {
            this.other = that;
        }

        @Override
        public boolean visited( long key, long value )
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

    private static class HashCodeComputer implements PrimitiveLongLongVisitor<RuntimeException>
    {
        private int hash = 1337;

        @Override
        public boolean visited( long key, long value ) throws RuntimeException
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
