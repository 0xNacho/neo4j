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
package org.neo4j.unsafe.impl.batchimport;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.neo4j.unsafe.impl.batchimport.input.InputEntity;
import org.neo4j.unsafe.impl.batchimport.recycling.RecycleAware;
import org.neo4j.unsafe.impl.batchimport.staging.IoProducerStep;
import org.neo4j.unsafe.impl.batchimport.staging.StageControl;

/**
 * Takes an {@link InputIterator} and chops it up into {@link Batch} instances.
 */
public class InputIteratorBatcherStep<INPUT extends InputEntity> extends IoProducerStep<Batch<INPUT,?>>
{
    private final InputIterator<INPUT> data;
    private final Class<INPUT> itemClass;
    private final RecycleAware<INPUT[]> recycler;

    public InputIteratorBatcherStep( StageControl control, Configuration config,
            InputIterator<INPUT> data, Class<INPUT> itemClass )
    {
        super( control, config );
        this.data = data;
        this.itemClass = itemClass;
        this.recycler = data;
    }

    @Override
    protected Batch<INPUT,?> nextBatchOrNull( long ticket, int batchSize )
    {
        if ( !data.hasNext() )
        {
            return null;
        }

        @SuppressWarnings( "unchecked" )
        INPUT[] batch = (INPUT[]) Array.newInstance( itemClass, batchSize );
        int i = 0;
        for ( ; i < batchSize && data.hasNext(); i++ )
        {
            batch[i] = data.next();
        }
        // shrink
        batch = i == batchSize ? batch : Arrays.copyOf( batch, i );
        return new Batch<>( batch );
    }

    @Override
    protected long position()
    {
        return data.position();
    }

    @Override
    public void recycled( Batch<INPUT,?> fromDownstream )
    {
        recycler.recycled( fromDownstream.input );
    }
}
