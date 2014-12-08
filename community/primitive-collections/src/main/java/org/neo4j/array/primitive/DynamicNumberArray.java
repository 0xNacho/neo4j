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

import java.util.Arrays;

import static org.neo4j.collection.primitive.Primitive.safeCastLongToInt;

/**
 * Base class for common functionality for any {@link NumberArray} where the data is dynamically growing,
 * where parts can live inside and parts off-heap.
 *
 * @see NumberArrayFactory#newDynamicLongArray(long, long)
 * @see NumberArrayFactory#newDynamicIntArray(long, int)
 */
abstract class DynamicNumberArray<N extends NumberArray> implements NumberArray
{
    protected final NumberArrayFactory factory;
    private final long chunkSize;
    private NumberArray[] chunks = new NumberArray[0];

    DynamicNumberArray( NumberArrayFactory factory, long chunkSize )
    {
        this.factory = factory;
        this.chunkSize = chunkSize;
    }

    @Override
    public long length()
    {
        return chunks.length * chunkSize;
    }

    @Override
    public void clear()
    {
        for ( NumberArray chunk : chunks )
        {
            chunk.clear();
        }
    }

    @Override
    public void visitMemoryStats( MemoryStatsVisitor visitor )
    {
        for ( NumberArray chunk : chunks )
        {
            chunk.visitMemoryStats( visitor );
        }
    }

    @SuppressWarnings( "unchecked" )
    protected N ensureChunkAt( long index )
    {
        while ( index >= length() )
        {
            chunks = Arrays.copyOf( chunks, chunks.length+1 );
            chunks[chunks.length-1] = addChunk( chunkSize );
        }
        return (N) chunks[chunkIndex( index )];
    }

    protected abstract N addChunk( long chunkSize );

    @SuppressWarnings( "unchecked" )
    protected N chunkAt( long index )
    {
        int chunkIndex = chunkIndex( index );
        return chunkIndex < chunks.length ? (N) chunks[chunkIndex] : null;
    }

    private int chunkIndex( long index )
    {
        return (int) (index/chunkSize);
    }

    protected long index( long index )
    {
        return index % chunkSize;
    }

    @Override
    public void close()
    {
        for ( NumberArray chunk : chunks )
        {
            chunk.close();
        }
    }

    @Override
    public void genericAnd( long index, long mask )
    {
        ensureChunkAt( index ).genericAnd( index( index ), mask );
    }

    @Override
    public void genericOr( long index, long mask )
    {
        ensureChunkAt( index ).genericOr( index( index ), mask );
    }

    @Override
    public void genericXor( long index, long mask )
    {
        ensureChunkAt( index ).genericXor( index( index ), mask );
    }

    @Override
    public void remove( long index, int numberOfEntries )
    {
        NumberArray chunk = chunkAt( index );
        if ( chunk != null )
        {
            chunk.remove( index( index ), numberOfEntries );
        }
    }

    @Override
    public void genericSet( long index, long value )
    {
        // Will this copied code actually perform better than delegating?
        ensureChunkAt( index ).genericSet( index( index ), safeCastLongToInt( value ) );
    }
}
