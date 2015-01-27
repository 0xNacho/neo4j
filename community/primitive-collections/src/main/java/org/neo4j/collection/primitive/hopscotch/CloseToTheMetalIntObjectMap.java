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
import org.neo4j.collection.primitive.PrimitiveIntObjectMap;
import org.neo4j.collection.primitive.PrimitiveIntObjectVisitor;

import static org.neo4j.collection.primitive.Primitive.safeCastLongToInt;

public class CloseToTheMetalIntObjectMap<VALUE> extends CloseToTheMetalIntCollection<VALUE> implements PrimitiveIntObjectMap<VALUE>
{
    public CloseToTheMetalIntObjectMap( NumberArrayFactory factory )
    {
        super( factory, 3, 2, null );
    }

    @Override
    public VALUE put( int key, VALUE value )
    {
        return _put( key, value );
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
        return _remove( key );
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveIntObjectVisitor<VALUE,E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( safeCastLongToInt( key ), getValue( array, k ) ) )
            {
                return;
            }
        }
    }
}
