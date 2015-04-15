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
package org.neo4j.unsafe.impl.batchimport.input;

import java.io.IOException;

import org.neo4j.io.fs.StoreChannel;

import static org.neo4j.unsafe.impl.batchimport.input.InputCache.END_OF_LABEL_CHANGES;
import static org.neo4j.unsafe.impl.batchimport.input.InputCache.HAS_LABEL_FIELD;
import static org.neo4j.unsafe.impl.batchimport.input.InputCache.LABEL_ADDITION;
import static org.neo4j.unsafe.impl.batchimport.input.InputCache.LABEL_REMOVAL;
import static org.neo4j.unsafe.impl.batchimport.input.Inputs.INPUT_NODE_FACTORY;

/**
 * Reads cached {@link InputNode} previously stored using {@link InputNodeCacher}.
 */
public class InputNodeReader extends InputEntityReader<InputNode>
{
    private GrowableArray<String> previousLabels = GrowableArray.empty();

    public InputNodeReader( StoreChannel channel, StoreChannel header, int bufferSize ) throws IOException
    {
        super( channel, header, bufferSize, 1, INPUT_NODE_FACTORY );
    }

    @Override
    protected void readNextOrNull( Long firstPropertyId, InputNode node ) throws IOException
    {
        // group
        Group group = readGroup( 0 );

        // id
        Object id = readValue();

        // labels (diff from previous node)
        byte labelsMode = channel.get();
        Object labels;
        if ( labelsMode == HAS_LABEL_FIELD )
        {
            labels = channel.getLong();
        }
        else if ( labelsMode == END_OF_LABEL_CHANGES )
        {   // Same as for previous node
            labels = previousLabels;
        }
        else
        {
            GrowableArray<String> newLabels = node.labels();
            newLabels.mirrorFrom( previousLabels );
            while ( labelsMode != END_OF_LABEL_CHANGES )
            {
                switch ( labelsMode )
                {
                case LABEL_REMOVAL: remove( readToken(), newLabels ); break;
                case LABEL_ADDITION: newLabels.add( readToken() ); break;
                default: throw new IllegalArgumentException( "Unrecognized label mode " + labelsMode );
                }
                labelsMode = channel.get();
            }
            labels = previousLabels = newLabels;
        }

        node.initialize( sourceDescription(), lineNumber(), position(), group, id, firstPropertyId,
                labelsMode == HAS_LABEL_FIELD ? (Long) labels : null );
    }

    private void remove( String item, GrowableArray<String> from )
    {
        if ( !from.remove( item ) )
        {
            throw new IllegalArgumentException( "Diff said to remove " + item + " from " +
                        from + ", but it didn't contain it" );
        }
    }
}
