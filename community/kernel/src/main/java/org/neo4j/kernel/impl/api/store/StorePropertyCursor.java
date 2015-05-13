/*
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
package org.neo4j.kernel.impl.api.store;

import org.neo4j.kernel.impl.api.cursor.PropertyCursor;

public class StorePropertyCursor implements PropertyCursor
{

    @Override
    public boolean next()
    {
        return false;
    }

    @Override
    public int key()
    {
        return 0;
    }

    @Override
    public Object value()
    {
        return null;
    }

    @Override
    public boolean booleanValue()
    {
        return false;
    }

    @Override
    public byte byteValue()
    {
        return 0;
    }

    @Override
    public short shortValue()
    {
        return 0;
    }

    @Override
    public char charValue()
    {
        return 0;
    }

    @Override
    public int intValue()
    {
        return 0;
    }

    @Override
    public long longValue()
    {
        return 0;
    }

    @Override
    public float floatValue()
    {
        return 0;
    }

    @Override
    public double doubleValue()
    {
        return 0;
    }

    @Override
    public boolean valueEquals( boolean value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( byte value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( short value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( char value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( int value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( long value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( float value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( double value )
    {
        return false;
    }

    @Override
    public boolean valueEquals( String value )
    {
        return false;
    }
}
