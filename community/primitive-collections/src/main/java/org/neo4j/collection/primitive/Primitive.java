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
package org.neo4j.collection.primitive;

import org.neo4j.collection.primitive.hopscotch.HashFunction;
import org.neo4j.collection.primitive.hopscotch.PrimitiveIntHashSet;
import org.neo4j.collection.primitive.hopscotch.PrimitiveIntLongHashMap;
import org.neo4j.collection.primitive.hopscotch.PrimitiveIntObjectHashMap;
import org.neo4j.collection.primitive.hopscotch.PrimitiveLongHashSet;
import org.neo4j.collection.primitive.hopscotch.PrimitiveLongIntHashMap;
import org.neo4j.collection.primitive.hopscotch.PrimitiveLongLongHashMap;
import org.neo4j.collection.primitive.hopscotch.PrimitiveLongObjectHashMap;

import static org.neo4j.array.primitive.NumberArrayFactory.HEAP;
import static org.neo4j.array.primitive.NumberArrayFactory.OFF_HEAP;

/**
 * Convenient factory for common primitive sets and maps.
 *
 * @see PrimitiveIntCollections
 * @see PrimitiveLongCollections
 */
public class Primitive
{
    /**
     * Used as value marker for sets, where values aren't applicable. The hop scotch algorithm still
     * deals with values so having this will have no-value collections, like sets communicate
     * the correct semantics to the algorithm.
     */
    public static final Object VALUE_MARKER = new Object();
    public static final int DEFAULT_HEAP_CAPACITY = 1 << 8;
    public static final int DEFAULT_OFFHEAP_CAPACITY = 1 << 20;
    public static final HashFunction DEFAULT_HASH_FUNCTION = HashFunction.JUL_HASHING;

    // Some example would be...
    public static PrimitiveLongSet longSet()
    {
        return longSet( DEFAULT_HEAP_CAPACITY );
    }

    public static PrimitiveLongSet longSet( int initialCapacity )
    {
        return new PrimitiveLongHashSet( DEFAULT_HASH_FUNCTION, HEAP, initialCapacity );
    }

    public static PrimitiveLongSet offHeapLongSet()
    {
        return offHeapLongSet( DEFAULT_OFFHEAP_CAPACITY );
    }

    public static PrimitiveLongSet offHeapLongSet( int initialCapacity )
    {
        return new PrimitiveLongHashSet( DEFAULT_HASH_FUNCTION, OFF_HEAP, initialCapacity );
    }

    public static PrimitiveLongIntMap longIntMap()
    {
        return longIntMap( DEFAULT_HEAP_CAPACITY );
    }

    public static PrimitiveLongIntMap longIntMap( int initialCapacity )
    {
        return new PrimitiveLongIntHashMap( DEFAULT_HASH_FUNCTION, HEAP, initialCapacity );
    }

    public static PrimitiveLongLongMap offHeapLongLongMap()
    {
        return offHeapLongLongMap( DEFAULT_OFFHEAP_CAPACITY );
    }

    public static PrimitiveLongLongMap offHeapLongLongMap( int initialCapacity )
    {
        return new PrimitiveLongLongHashMap( DEFAULT_HASH_FUNCTION, OFF_HEAP, initialCapacity );
    }

    public static <VALUE> PrimitiveLongObjectMap<VALUE> longObjectMap()
    {
        return longObjectMap( DEFAULT_HEAP_CAPACITY );
    }

    public static <VALUE> PrimitiveLongObjectMap<VALUE> longObjectMap( int initialCapacity )
    {
        return new PrimitiveLongObjectHashMap<>( DEFAULT_HASH_FUNCTION, HEAP, initialCapacity );
    }

    public static PrimitiveIntSet intSet()
    {
        return intSet( DEFAULT_HEAP_CAPACITY );
    }

    public static PrimitiveIntSet intSet( int initialCapacity )
    {
        return new PrimitiveIntHashSet( DEFAULT_HASH_FUNCTION, HEAP, initialCapacity );
    }

    public static PrimitiveIntSet offHeapIntSet()
    {
        return offHeapIntSet( DEFAULT_OFFHEAP_CAPACITY );
    }

    public static PrimitiveIntSet offHeapIntSet( int initialCapacity )
    {
        return new PrimitiveIntHashSet( DEFAULT_HASH_FUNCTION, OFF_HEAP, initialCapacity );
    }

    public static <VALUE> PrimitiveIntObjectMap<VALUE> intObjectMap()
    {
        return intObjectMap( DEFAULT_HEAP_CAPACITY );
    }

    public static <VALUE> PrimitiveIntObjectMap<VALUE> intObjectMap( int initialCapacity )
    {
        return new PrimitiveIntObjectHashMap<>( DEFAULT_HASH_FUNCTION, HEAP, initialCapacity );
    }

    public static PrimitiveIntLongMap intLongMap()
    {
        return intLongMap( DEFAULT_HEAP_CAPACITY );
    }

    public static PrimitiveIntLongMap intLongMap( int initialCapacity )
    {
        return new PrimitiveIntLongHashMap( DEFAULT_HASH_FUNCTION, HEAP, initialCapacity );
    }

    public static PrimitiveLongIterator iterator( final long... longs )
    {
        return new PrimitiveLongIterator()
        {
            int i;

            @Override
            public boolean hasNext()
            {
                return i < longs.length;
            }

            @Override
            public long next()
            {
                return longs[i++];
            }
        };
    }

    // TODO for the methods below, use built-in and efficient methods when available (JDK 8?)

    public static int safeCastLongToInt( long value )
    {
        if ( value > Integer.MAX_VALUE || value < Integer.MIN_VALUE )
        {
            throw new AssertionError( "Tried to cast long value " + value + " to int, but value doesn't fit" );
        }
        return (int) value;
    }

    public static short safeCastLongToShort( long value )
    {
        if ( value > Short.MAX_VALUE || value < Short.MIN_VALUE )
        {
            throw new AssertionError( "Tried to cast long value " + value + " to short, but value doesn't fit" );
        }
        return (short) value;
    }

    public static byte safeCastLongToByte( long value )
    {
        if ( value > Byte.MAX_VALUE || value < Byte.MIN_VALUE )
        {
            throw new AssertionError( "Tried to cast long value " + value + " to byte, but value doesn't fit" );
        }
        return (byte) value;
    }

    // TODO Copied from o.n.h.Format. Move the class to this component instead?
    public static int KB = 1024;
    public static int MB = KB * KB;
    public static int GB = KB * MB;
    private static final String[] BYTE_SIZES = { "B", "kB", "MB", "GB" };

    public static String bytes( long bytes )
    {
        double size = bytes;
        for ( String suffix : BYTE_SIZES )
        {
            if ( size < KB )
            {
                return String.format( "%.2f %s", Double.valueOf( size ), suffix );
            }
            size /= KB;
        }
        return String.format( "%.2f TB", Double.valueOf( size ) );
    }
}
