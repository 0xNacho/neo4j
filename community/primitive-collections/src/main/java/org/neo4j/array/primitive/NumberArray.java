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
 * Abstraction over primitive arrays.
 *
 * @see NumberArrayFactory
 */
public interface NumberArray extends MemoryStatsVisitor.Home, AutoCloseable
{
    /**
     * @return length of the array, i.e. the capacity.
     */
    long length();

    /**
     * Swaps the values of indexes from {@code fromIndex} to {@code toIndex}.
     *
     * @param fromIndex one side of the indexes to swap.
     * @param toIndex the other side of the indexes to swap.
     * @param numberOfEntries number of entries (indexes) to swap.
     */
    void swap( long fromIndex, long toIndex, int numberOfEntries );

    /**
     * Sets all values to a default value.
     */
    void clear();

    /**
     * Removes values spawning from {@code index} and {@code numberOfEntries-1} indexes ahead.
     * Values in this range will be reset to default value.
     *
     * @param index start index to remove.
     * @param numberOfEntries number of entries to remove.
     */
    void remove( long index, int numberOfEntries );

    /**
     * Releases any resources that GC won't release automatically.
     */
    @Override
    public void close();

    /**
     * For objects there are generics that allow the same class to handle different types of objects.
     * For primitives there aren't and so this is an attempt to provide a similar facility for primitive numbers,
     * so that any {@link NumberArray} implementation may be used in generic primitive number code.
     *
     * @param index the array index to get the value for.
     * @return a long representation of getting a primitive number of the type that the particular implementation
     * of this class handles.
     */
    long genericGet( long index );

    /**
     * For objects there are generics that allow the same class to handle different types of objects.
     * For primitives there aren't and so this is an attempt to provide a similar facility for primitive numbers,
     * so that any {@link NumberArray} implementation may be used in generic primitive number code.
     *
     * @param index the array index to set the value for.
     * @param value value to set for the given index. This value needs to be losslessly coerced into
     * the primitive number type that this particular implementation handles, or throw exception.
     */
    void genericSet( long index, long value );

    /**
     * For objects there are generics that allow the same class to handle different types of objects.
     * For primitives there aren't and so this is an attempt to provide a similar facility for primitive numbers,
     * so that any {@link NumberArray} implementation may be used in generic primitive number code.
     *
     * @param index the affected index.
     * @param mask mask to AND the existing value with.
     */
    void genericAnd( long index, long mask );

    /**
     * For objects there are generics that allow the same class to handle different types of objects.
     * For primitives there aren't and so this is an attempt to provide a similar facility for primitive numbers,
     * so that any {@link NumberArray} implementation may be used in generic primitive number code.
     *
     * @param index the affected index.
     * @param mask mask to OR the existing value with.
     */
    void genericOr( long index, long mask );

    /**
     * For objects there are generics that allow the same class to handle different types of objects.
     * For primitives there aren't and so this is an attempt to provide a similar facility for primitive numbers,
     * so that any {@link NumberArray} implementation may be used in generic primitive number code.
     *
     * @param index the affected index.
     * @param mask mask to XOR the existing value with.
     */
    void genericXor( long index, long mask );
}
