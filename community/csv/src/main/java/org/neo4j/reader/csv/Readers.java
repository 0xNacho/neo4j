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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;

public class Readers
{
    private Readers()
    {   // No instances allowed
    }

    public Reader forInputStream(
            final InputStream input, final String name, final Charset encoding, final int bufferSize )
    {
        return new Reader()
        {
            private final PushbackInputStream in = new PushbackInputStream( input, bufferSize );
            private final java.io.Reader reader = new InputStreamReader( input, encoding );

            @Override
            public int read( char[] into ) throws IOException
            {
                int totalRead = 0, read = 0;
                while ( totalRead < into.length && read >= 0 )
                {
                    read = reader.read( into );
                    totalRead += Math.min( read, 0 );
                }
                return totalRead;
            }

            @Override
            public void goBack( int chars ) throws IOException
            {
                in.unread( chars*2 );
            }

            @Override
            public String toString()
            {
                return name;
            }
        };
    }

    // TODO add multi-reader
}
