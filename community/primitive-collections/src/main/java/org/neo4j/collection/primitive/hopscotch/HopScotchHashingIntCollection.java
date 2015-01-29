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
import org.neo4j.collection.primitive.PrimitiveIntCollection;
import org.neo4j.collection.primitive.PrimitiveIntCollections;
import org.neo4j.collection.primitive.PrimitiveIntIterator;
import org.neo4j.collection.primitive.PrimitiveIntVisitor;

public abstract class HopScotchHashingIntCollection<VALUE> extends HopScotchHashingCollection<VALUE>
        implements PrimitiveIntCollection
{
    public HopScotchHashingIntCollection( HashFunction hashFunction, NumberArrayFactory factory,
            int itemsPerEntry, VALUE nullValue, int initialCapacity )
    {
        super( hashFunction, factory, itemsPerEntry, nullValue, initialCapacity );
    }

    @Override
    public PrimitiveIntIterator iterator()
    {
        return new PrimitiveIntCollections.PrimitiveIntBaseIterator()
        {
            private final int max = capacity();
            private int i;

            @Override
            protected boolean fetchNext()
            {
                while ( i < max )
                {
                    int index = i++;
                    long key = getKey( array, index );
                    if ( isVisible( index, key ) )
                    {
                        return next( (int) key ); // TODO safe cast
                    }
                }
                return false;
            }
        };
    }

    protected boolean isVisible( int index, long key )
    {
        return key != nullKey;
    }

    @Override
    public <E extends Exception> void visitKeys( PrimitiveIntVisitor<E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( (int) key ) )
            {
                return;
            }
        }
    }
}
