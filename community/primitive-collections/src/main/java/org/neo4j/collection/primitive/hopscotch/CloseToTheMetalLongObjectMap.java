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
import org.neo4j.collection.primitive.PrimitiveLongObjectMap;
import org.neo4j.collection.primitive.PrimitiveLongObjectVisitor;

public class CloseToTheMetalLongObjectMap<VALUE> extends CloseToTheMetalLongCollection<VALUE> implements PrimitiveLongObjectMap<VALUE>
{
    public CloseToTheMetalLongObjectMap( NumberArrayFactory factory )
    {
        super( factory, 3, 2, null );
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
        return _remove( key );
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveLongObjectVisitor<VALUE,E> visitor ) throws E
    {
        int capacity = capacity();
        for ( int i = 0, k = 0; i < capacity; i++, k += itemsPerEntry )
        {
            long key = getKey( array, k );
            if ( isVisible( i, key ) && visitor.visited( key, getValue( array, k ) ) )
            {
                return;
            }
        }
    }
}
