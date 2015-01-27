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
import org.neo4j.collection.primitive.PrimitiveIntIterator;
import org.neo4j.collection.primitive.PrimitiveIntSet;

public class CloseToTheMetalIntSet extends CloseToTheMetalIntCollection<Void> implements PrimitiveIntSet
{
    public CloseToTheMetalIntSet( NumberArrayFactory factory )
    {
        super( factory, 2, 1, null );
    }

    @Override
    public boolean accept( int value )
    {
        return contains( (long) value );
    }

    @Override
    public boolean add( int value )
    {
        return add( (long) value );
    }

    @Override
    public boolean addAll( PrimitiveIntIterator values )
    {
        boolean result = false;
        while ( values.hasNext() )
        {
            result |= add( (long) values.next() );
        }
        return result;
    }

    @Override
    public boolean contains( int value )
    {
        return contains( (long) value );
    }

    @Override
    public boolean remove( int value )
    {
        return _remove( value ) != null;
    }
}
