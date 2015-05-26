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

public class BufferedCharSeeker extends AbstractCharSeeker
{
    private final CharReadable reader;

    // Wraps the char[] buffer and is only used during reading more data, using f.ex. compact()
    // so that we don't have to duplicate that functionality.
    private SectionedCharBuffer charBuffer;

    public BufferedCharSeeker( CharReadable reader, Configuration config )
    {
        super( config );
        this.reader = reader;
        this.charBuffer = new SectionedCharBuffer( config.bufferSize() );
        this.buffer = charBuffer.array();
        this.bufferPos = this.bufferEnd = this.lineStartPos = charBuffer.pivot();
        this.sourceDescription = reader.sourceDescription();
    }

    /**
     * @return {@code true} if something was read, otherwise {@code false} which means that we reached EOF.
     */
    @Override
    protected boolean readMoreIntoBuffer() throws IOException
    {
        if ( bufferPos - seekStartPos >= charBuffer.pivot() )
        {
            throw new IllegalStateException( "Tried to read in a value larger than effective buffer size " +
                    charBuffer.pivot() );
        }

        absoluteBufferStartPosition += charBuffer.available();

        // Fill the buffer with new characters
        charBuffer = reader.read( charBuffer, seekStartPos );
        buffer = charBuffer.array();
        bufferPos = charBuffer.pivot();
        bufferEnd = charBuffer.front();
        int shift = seekStartPos-charBuffer.back();
        seekStartPos = charBuffer.back();
        lineStartPos -= shift;
        String sourceDescriptionAfterRead = reader.sourceDescription();
        if ( !sourceDescription.equals( sourceDescriptionAfterRead ) )
        {   // We moved over to a new source, reset line number
            lineNumber = 0;
            sourceDescription = sourceDescriptionAfterRead;
        }
        return charBuffer.hasAvailable();
    }

    @Override
    public void close() throws IOException
    {
        reader.close();
    }
}
