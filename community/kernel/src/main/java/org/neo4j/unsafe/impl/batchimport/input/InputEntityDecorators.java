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

import org.neo4j.function.Function;
import org.neo4j.function.Functions;
import org.neo4j.helpers.ArrayUtil;
import org.neo4j.unsafe.impl.batchimport.input.csv.Builder;

/**
 * Common {@link InputEntity} decorators, able to provide defaults or overrides.
 */
public class InputEntityDecorators
{
    /**
     * Ensures that all {@link InputNode input nodes} will at least have the given set of labels.
     */
    public static Function<Builder<InputNode>,Builder<InputNode>> additiveLabels( final String[] labelNamesToAdd )
    {
        if ( labelNamesToAdd == null || labelNamesToAdd.length == 0 )
        {
            return Functions.identity();
        }

        return new Function<Builder<InputNode>,Builder<InputNode>>()
        {
            @Override
            public Builder<InputNode> apply( final Builder<InputNode> from ) throws RuntimeException
            {
                return new Builder.Decorator<InputNode>( from )
                {
                    @Override
                    public InputNode materialize()
                    {
                        InputNode node = from.materialize();
                        if ( node.hasLabelField() )
                        {
                            return node;
                        }

                        String[] union = ArrayUtil.union( node.labels(), labelNamesToAdd );
                        if ( union != node.labels() )
                        {
                            node.setLabels( union );
                        }
                        return node;
                    }
                };
            }
        };
    }

    /**
     * Ensures that {@link InputRelationship input relationships} without a specified relationship type will get
     * the specified default relationship type.
     */
    public static Function<Builder<InputRelationship>,Builder<InputRelationship>> defaultRelationshipType(
            final String defaultType )
    {
        if ( defaultType == null )
        {
            return Functions.identity();
        }

        return new Function<Builder<InputRelationship>,Builder<InputRelationship>>()
        {
            @Override
            public Builder<InputRelationship> apply( final Builder<InputRelationship> from ) throws RuntimeException
            {
                return new Builder.Decorator<InputRelationship>( from )
                {
                    @Override
                    public InputRelationship materialize()
                    {
                        InputRelationship relationship = from.materialize();
                        if ( relationship.type() == null && !relationship.hasTypeId() )
                        {
                            relationship.setType( defaultType );
                        }
                        return relationship;
                    }
                };
            }
        };
    }

    @SafeVarargs
    public static <ENTITY extends InputEntity> Function<Builder<ENTITY>,Builder<ENTITY>> decorators(
            final Function<Builder<ENTITY>,Builder<ENTITY>>... decorators )
    {
        return new Function<Builder<ENTITY>,Builder<ENTITY>>()
        {
            @Override
            public Builder<ENTITY> apply( Builder<ENTITY> from ) throws RuntimeException
            {
                for ( Function<Builder<ENTITY>,Builder<ENTITY>> decorator : decorators )
                {
                    from = decorator.apply( from );
                }
                return from;
            }
        };
    }

    public static final Function<Builder<InputNode>,Builder<InputNode>> NO_NODE_DECORATOR =
            Functions.identity();
    public static final Function<Builder<InputRelationship>,Builder<InputRelationship>> NO_RELATIONSHIP_DECORATOR =
            Functions.identity();
}
