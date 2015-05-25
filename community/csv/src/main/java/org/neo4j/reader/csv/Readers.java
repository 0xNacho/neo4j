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
import java.io.PushbackReader;
import java.nio.charset.Charset;

public class Readers
{
    private Readers()
    {   // No instances allowed
    }

    public static Reader forInputStream(
            final InputStream input, final String name, final Charset encoding, final int bufferSize )
    {
        return new Reader()
        {
            private final PushbackReader reader =
                    new PushbackReader( new InputStreamReader( input, encoding ), bufferSize );

            @Override
            public int read( char[] into ) throws IOException
            {
                int totalRead = 0, read = 0;
                while ( totalRead < into.length && read >= 0 )
                {
                    read = reader.read( into );
                    totalRead += Math.max( read, 0 );
                }
                return totalRead;
            }

            @Override
            public void unread( char[] chars, int offset, int length ) throws IOException
            {
                reader.unread( chars, offset, length );
            }

            @Override
            public void close() throws IOException
            {
                reader.close();
            }

            @Override
            public String toString()
            {
                return name;
            }
        };
    }

    public static Reader oneLineOf( final Reader reader )
    {
        return new Reader()
        {
            private boolean hasRead;

            @Override
            public int read( char[] into ) throws IOException
            {
                if ( hasRead )
                {
                    return -1;
                }

                hasRead = true;
                char[] chunk = new char[100];
                int totalRead = 0;
                boolean newlineFound = false;
                while ( !newlineFound )
                {
                    // Read a little
                    int read = reader.read( chunk );
                    if ( read < 0 )
                    {
                        break;
                    }

                    // Look for newline
                    int chunkLength = read;
                    for ( int i = 0; i < chunk.length; i++ )
                    {
                        if ( chunk[i] == '\n' )
                        {
                            chunkLength = i + 1 /*since we want to include \n*/;
                            newlineFound = true;
                            reader.unread( chunk, chunkLength, chunk.length - i - 1 /*the rest of the chars*/ );
                            break;
                        }
                    }

                    // Copy into result
                    System.arraycopy( chunk, 0, into, totalRead, chunkLength );
                    totalRead += chunkLength;
                }

                return totalRead;
            }

            @Override
            public void unread( char[] chars, int offset, int length ) throws IOException
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() throws IOException
            {   // Don't close
            }
        };
    }

    // TODO add multi-reader
}
