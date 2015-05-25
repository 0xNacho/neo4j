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

import java.io.Closeable;
import java.io.IOException;

/**
 * Simplified, re-purposed {@link java.io.Reader} basically.
 */
public interface Reader extends Closeable
{
    /**
     * Read into the buffer.
     *
     * @param into buffer to read into.
     * @return number of characters read, max the length of the buffer.
     * @throws IOException if there was a problem reading.
     */
    int read( char[] into ) throws IOException;

    /**
     * Go back the specified number of characters, so that they will be read the next call to {@link #read(char[])}.
     */
    void unread( char[] chars, int offset, int length ) throws IOException;
}
