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
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.collection.primitive.PrimitiveLongSet;

public class CloseToTheMetalLongSet extends CloseToTheMetalLongCollection<Void> implements PrimitiveLongSet
{
    public CloseToTheMetalLongSet( NumberArrayFactory factory )
    {
        super( factory, 3, 2, null );
    }

    @Override
    protected long getKey( IntArray array, int absIndex )
    {
        return getLong( array, absIndex );
    }

    @Override
    protected void putKey( IntArray array, int absIndex, long key )
    {
        array.set( absIndex, (int)key );
        array.set( absIndex+1, (int)((key&0xFFFFFFFF00000000L) >>> 32) );
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
        return _remove( value ) != null;
    }
}
