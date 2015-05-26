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
package org.neo4j.csv.reader;

import java.io.IOException;

public class MorselCharSeeker extends AbstractCharSeeker
{
    public MorselCharSeeker( Configuration config )
    {
        super( config );
    }

    public void giveData( char[] data, int length, String newSourceDescription )
    {
        absoluteBufferStartPosition += buffer != null ? bufferEnd : 0;
        buffer = data;
        bufferEnd = length;
        bufferPos = 0;
        sourceDescription = newSourceDescription;
        eof = false;
        // TODO lineStartPos?
    }

    @Override
    protected boolean readMoreIntoBuffer() throws IOException
    {   // We won't read more into this buffer
        return false;
    }

    @Override
    public void close() throws IOException
    {   // Nothing to close
    }
}
