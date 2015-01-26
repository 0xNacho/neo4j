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
package org.neo4j.collection.primitive.hopscotch;

/**
 * Monitor for what how a {@link HopScotchHashingAlgorithm} changes the items in a {@link Table}.
 */
public interface Monitor
{
    boolean tableGrowing( int fromCapacity, int currentSize );

    boolean tableGrew( int fromCapacity, int toCapacity, int currentSize );

    boolean placedAtFreeAndIntendedIndex( long key, int index );

    boolean pushedToFreeIndex( int intendedIndex, long oldHopBits, long newHopBits, int neighborIndex,
            long key, int fromIndex, int toIndex );

    boolean placedAtFreedIndex( int intendedIndex, long newHopBits, long key, int actualIndex );

    boolean pulledToFreeIndex( int intendedIndex, long newHopBits, long key, int fromIndex, int toIndex );

    public abstract static class Adapter implements Monitor
    {
        @Override
        public boolean placedAtFreedIndex( int intendedIndex, long newHopBits, long key, int actualIndex )
        {
            return true;
        }

        @Override
        public boolean placedAtFreeAndIntendedIndex( long key, int index )
        {
            return true;
        }

        @Override
        public boolean pushedToFreeIndex( int intendedIndex, long oldHopBits, long newHopBits,
                int neighborIndex, long key, int fromIndex, int toIndex )
        {
            return true;
        }

        @Override
        public boolean pulledToFreeIndex( int intendedIndex, long newHopBits, long key,
                int fromIndex, int toIndex )
        {
            return true;
        }

        @Override
        public boolean tableGrowing( int fromCapacity, int currentSize )
        {
            return true;
        }

        @Override
        public boolean tableGrew( int fromCapacity, int toCapacity, int currentSize )
        {
            return true;
        }
    }

    public static final Monitor NO_MONITOR = new Monitor.Adapter() { /*No additional logic*/ };
}
