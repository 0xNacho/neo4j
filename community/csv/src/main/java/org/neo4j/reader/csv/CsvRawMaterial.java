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
package org.neo4j.reader.csv;

import java.util.Deque;

import org.neo4j.reader.RawMaterial;

public class CsvRawMaterial implements RawMaterial
{
    private final char[] buffer;
    private final int length;
    private final String sourceDescription;
    private final Deque<char[]> recycleStation;

    public CsvRawMaterial( char[] buffer, int length, String sourceDescription, Deque<char[]> recycleStation )
    {
        this.buffer = buffer;
        this.length = length;
        this.sourceDescription = sourceDescription;
        this.recycleStation = recycleStation;
    }

    public char[] getBuffer()
    {
        return buffer;
    }

    public int getLength()
    {
        return length;
    }

    public String getSourceDescription()
    {
        return sourceDescription;
    }

    @Override
    public void close()
    {
        recycleStation.add( buffer );
    }
}
