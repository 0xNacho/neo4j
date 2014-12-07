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
package org.neo4j.array.primitive;

/**
 * Visits objects able to provide stats about amount of used memory.
 */
public interface MemoryStatsVisitor
{
    public interface Home
    {
        /**
         * A home for a {@link MemoryStatsVisitor} to visit. Any class expecting a {@link MemoryStatsVisitor}
         * to come and visit should implement this interface.
         *
         * @param visitor {@link MemoryStatsVisitor} to visit.
         */
        void visitMemoryStats( MemoryStatsVisitor visitor );
    }

    void heapUsage( long bytes );

    void offHeapUsage( long bytes );
}
