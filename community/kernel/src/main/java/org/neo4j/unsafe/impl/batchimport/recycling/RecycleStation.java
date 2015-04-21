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
package org.neo4j.unsafe.impl.batchimport.recycling;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import org.neo4j.function.Factory;

/**
 * An advise is to use either {@link #get()} OR {@link #getBatch(int)}.
 */
public class RecycleStation<T> implements RecycleAware<T[]>, Supplier<T>
{
    private final Factory<T> factory;
    private final Queue<T[]> batches = new ConcurrentLinkedQueue<>();
    private T[] current;
    private int cursor;

    public RecycleStation( Factory<T> factory )
    {
        this.factory = factory;
    }

    @Override
    public void recycled( T[] object )
    {
        batches.add( object );
    }

    @Override
    public T get()
    {
        if ( current == null || cursor == current.length )
        {
            current = batches.poll();
            cursor = 0;
        }
        if ( current != null )
        {
            return current[cursor++];
        }
        return factory.newInstance();
    }

    @SuppressWarnings( "unchecked" )
    public T[] getBatch( int size )
    {
        T[] batch = batches.poll();
        if ( batch == null )
        {
            T first = factory.newInstance();
            batch = (T[]) Array.newInstance( first.getClass(), size );
            batch[0] = first;
            for ( int i = 1; i < batch.length; i++ )
            {
                batch[i] = factory.newInstance();
            }
            return batch;
        }

        if ( batch.length == size )
        {
            return batch;
        }
        else if ( size < batch.length )
        {
            return Arrays.copyOf( batch, size );
        }
        throw new IllegalArgumentException( "Requested a batch of size " + size +
                ", but the batches handled in here are of size " + batch.length );
    }
}
