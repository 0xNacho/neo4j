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

import org.neo4j.array.primitive.NumberArrayFactory;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.collection.primitive.PrimitiveLongVisitor;

import static org.neo4j.collection.primitive.hopscotch.HashFunction.DEFAULT_HASHING;

public class PrimitiveLongHashSet extends HopScotchHashingLongCollection<Boolean> implements PrimitiveLongSet
{
    public PrimitiveLongHashSet( HashFunction hashFunction, NumberArrayFactory factory, int initialCapacity,
            Monitor monitor )
    {
        super( hashFunction, factory, 3, Boolean.TRUE, initialCapacity, monitor );
    }

    @Override
    public boolean accept( long value )
    {
        return contains( value );
    }

    @Override
    public boolean addAll( PrimitiveLongIterator values )
    {
        boolean result = false;
        while ( values.hasNext() )
        {
            result |= add( values.next() );
        }
        return result;
    }

    @Override
    public boolean remove( long value )
    {
        return _remove( value );
    }

    @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" ) // yes it does
    @Override
    public boolean equals( Object other )
    {
        if ( typeAndSizeEqual( other ) )
        {
            PrimitiveLongHashSet that = (PrimitiveLongHashSet) other;
            LongKeyEquality equality = new LongKeyEquality( that );
            visitKeys( equality );
            return equality.isEqual();
        }
        return false;
    }

    private static class LongKeyEquality implements PrimitiveLongVisitor<RuntimeException>
    {
        private final PrimitiveLongHashSet other;
        private boolean equal = true;

        public LongKeyEquality( PrimitiveLongHashSet that )
        {
            this.other = that;
        }

        @Override
        public boolean visited( long value )
        {
            equal = other.contains( value );
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
        visitKeys( hash );
        return hash.hashCode();
    }

    private static class HashCodeComputer implements PrimitiveLongVisitor<RuntimeException>
    {
        private int hash = 1337;

        @Override
        public boolean visited( long value ) throws RuntimeException
        {
            hash += DEFAULT_HASHING.hash( value );
            return false;
        }

        @Override
        public int hashCode()
        {
            return hash;
        }
    }
}
