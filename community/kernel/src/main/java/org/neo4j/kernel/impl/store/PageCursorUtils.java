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
package org.neo4j.kernel.impl.store;

import org.neo4j.io.pagecache.PageCursor;

public class PageCursorUtils
{
    public static void write6B( PageCursor cursor, long value )
    {
        // lsb
        cursor.putInt( (int) value );
        // msb
        cursor.putShort( (short) ((value & 0xFFFF00000000L) >> 32) );
    }

    public static void write3B( PageCursor cursor, int value )
    {
        // lsb
        cursor.putShort( (short) value );
        // msb
        cursor.putByte( (byte) ((value & 0xFF0000L) >> 16) );
    }

    public static long read6B( PageCursor cursor )
    {
        // lsb
        long lsb = cursor.getInt();
        // msb
        long msb = cursor.getShort();
        return (msb << 32) | lsb;
    }

    public static int read3B( PageCursor cursor )
    {
        // lsb
        int lsb = cursor.getShort();
        // msb
        int msb = cursor.getByte();
        return (msb << 16) | lsb;
    }
}
