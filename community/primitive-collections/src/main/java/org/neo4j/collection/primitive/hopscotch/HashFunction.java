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

public interface HashFunction
{
    /**
     * Same hash function as that used by the standard library hash collections. It generates a hash by splitting the
     * input value into segments, and then re-distributing those segments, so the end result is effectively a striped
     * and then jumbled version of the input data. For randomly distributed keys, this has a good chance at generating
     * an even hash distribution over the full hash space.
     *
     * It performs exceptionally poorly for sequences of numbers, as the sequence increments all end up in the same
     * stripe, generating hash values that will end up in the same buckets in collections.
     */
    public static final HashFunction JUL_HASHING = new HashFunction()
    {
        @Override
        public int hash( long value )
        {
            int h = (int) ((value >>> 32) ^ value);
            h ^= (h >>> 20) ^ (h >>> 12);
            return h ^ (h >>> 7) ^ (h >>> 4);
        }
    };

    /**
     * The default hash function is based on a pseudo-random number generator, which uses the input value as a seed
     * to the generator. This is very fast, and performs well for most input data. However, it is not guaranteed to
     * generate a superb distribution, only a "decent" one.
     */
    public static final HashFunction DEFAULT_HASHING = new HashFunction()
    {
        @Override
        public int hash( long value )
        {
            value ^= (value << 21);
            value ^= (value >>> 35);
            value ^= (value << 4);

            return (int) ((value >>> 32) ^ value);
        }
    };

    int hash( long value );
}
