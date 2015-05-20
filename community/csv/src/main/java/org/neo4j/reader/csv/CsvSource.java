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

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.neo4j.reader.RawMaterial;
import org.neo4j.reader.Source;

public class CsvSource implements Source
{
    // Source that is being read from
    private final Reader reader;
    private final int bufferSize;

    // State during reading
    private long nextId;

    // Keeping track of returned RawMaterial, to reduce object churn
    private final Deque<char[]> returnedRawMaterial = new ConcurrentLinkedDeque<>();

    public CsvSource( Reader reader, int bufferSize )
    {
        this.reader = reader;
        this.bufferSize = bufferSize;
    }

    @Override
    public RawMaterial next() throws IOException
    {
        char[] buffer = returnedRawMaterial.poll();
        if ( buffer == null )
        {
            buffer = new char[bufferSize];
        }

        int read = reader.read( buffer );
        if ( read == bufferSize )
        {   // We read data into the whole buffer, we're most likely not at the end so seek backwards to
            // the last newline character and reset the reader to that position.
            int charactersToBackUp = findLastNewline( buffer );
            if ( charactersToBackUp > -1 )
            {   // We found a newline character some characters back
                reader.goBack( charactersToBackUp );
                read -= charactersToBackUp;
            }
            else
            {   // There was no newline character, isn't that weird?
                throw new IllegalStateException( "Weird input data, no newline character in the whole buffer " +
                        bufferSize + ", not supported a.t.m." );
            }
        }
        else
        {   // We couldn't completely fill the buffer, this means that we're at the end of a data source, we're good.
        }

        return new CsvRawMaterial( nextId++, buffer, read, returnedRawMaterial );
    }

    private int findLastNewline( char[] buffer )
    {
        for ( int i = buffer.length-1, stepsBack = 0; i >= 0; i--, stepsBack++ )
        {
            if ( buffer[i] == '\n' ) // TODO check for \r too?
            {
                return stepsBack;
            }
        }
        return -1;
    }

    @Override
    public String toString()
    {
        return reader.toString();
    }
}
