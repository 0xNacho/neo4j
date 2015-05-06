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
package org.neo4j.unsafe.impl.batchimport.input.csv;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.neo4j.csv.reader.CharSeeker;
import org.neo4j.function.Function;
import org.neo4j.kernel.impl.util.Validators;
import org.neo4j.unsafe.impl.batchimport.input.Groups;
import org.neo4j.unsafe.impl.batchimport.input.InputNode;
import org.neo4j.unsafe.impl.batchimport.input.UpdateBehaviour;
import org.neo4j.unsafe.impl.batchimport.input.csv.Header.Entry;

/**
 * Pulls in properties from an external CSV source and amends them to the "main" input nodes.
 * Imagine some node input source:
 * <pre>
 * :ID,name
 * 1,First
 * 2,Second
 * 3,Third
 * 4,Fourth
 * </pre>
 * and an external properties source:
 * <pre>
 * :ID,email
 * 1.abc@somewhere
 * 1,def@somewhere
 * 3,ghi@someplace
 * <Pre>
 * Then properties {@code abc@somewhere} and {@code def@somewhere} will be amended to input node {@code 1}
 * and {@code ghi@someplace} to input node {@code 3}.
 */
public class ExternalPropertiesDecorator implements Function<Builder<InputNode>,Builder<InputNode>>
{
    private final InputEntityDeserializer<InputNode> deserializer;
    private InputNode currentExternal;
    private final Header header;
    private final boolean alwaysArrays;

    /**
     * @param headerFactory creates a {@link Header} that will specify which field is the {@link Type#ID id field}
     * and which properties to extract. All other should be {@link Type#IGNORE ignored}. I think.
     */
    public ExternalPropertiesDecorator( DataFactory<InputNode> data, Header.Factory headerFactory,
            Configuration config, IdType idType, boolean alwaysArrays )
    {
        this.alwaysArrays = alwaysArrays;
        CharSeeker dataStream = data.create( config ).stream();
        this.header = headerFactory.create( dataStream, config, idType );
        this.deserializer = new InputEntityDeserializer<>( header, dataStream, config.delimiter(),
                new InputNodeBuilder( dataStream, header, new Groups(), idType.idsAreExternal() ),
                Validators.<InputNode>emptyValidator() );
    }

    @Override
    public Builder<InputNode> apply( Builder<InputNode> node ) throws RuntimeException
    {
        return new Builder.Decorator<InputNode>( node )
        {
            private Object id;
            private Object[] properties;

            @Override
            public void handle( Entry entry, Object value )
            {
                if ( entry.type() == Type.ID )
                {
                    id = value;
                }
                super.handle( entry, value );
            }

            @Override
            public void clear()
            {
                id = null;
                properties = null;
                super.clear();
            }

            @Override
            public InputNode materialize()
            {
                // Nodes come in here. Correlate by id to the external properties data
                boolean lookFurther = true;
                if ( currentExternal != null )
                {
                    if ( id.equals( currentExternal.id() ) )
                    {
                        updateProperties( UpdateBehaviour.ADD, currentExternal.properties() );
                        currentExternal = null;
                    }
                    else
                    {
                        lookFurther = false;
                    }
                }

                while ( lookFurther && deserializer.hasNext() )
                {
                    currentExternal = deserializer.next();
                    if ( id.equals( currentExternal.id() ) )
                    {
                        // decorate as well. I.e. there were multiple rows for this node id
                        updateProperties( UpdateBehaviour.ADD, currentExternal.properties() );
                    }
                    else
                    {
                        lookFurther = false;
                    }
                }

                decorate();
                return super.materialize();
            }

            private void decorate()
            {
                for ( int i = 0; properties != null && i < properties.length; i++ )
                {
                    // TODO costly, creating all these Entry objects
                    handle( new Entry( (String) properties[i++], Type.PROPERTY, null, null ), properties[i] );
                }
            }

            private void updateProperties( UpdateBehaviour behaviour, Object... keyValuePairs )
            {
                assert keyValuePairs.length % 2 == 0 : Arrays.toString( keyValuePairs );

                // There were no properties before, just set these and be done
                if ( properties == null || properties.length == 0 )
                {
                    properties = keyValuePairs;
                    if ( alwaysArrays )
                    {
                        for ( int i = 0; i < properties.length; i++ )
                        {
                            properties[++i] = singleValueArray( properties[i] );
                        }
                    }
                    return;
                }

                // We need to look at existing properties
                // First make room for any new properties
                int newLength = collectiveNumberOfKeys( properties, keyValuePairs ) * 2;
                properties = newLength == properties.length ? properties : Arrays.copyOf( properties, newLength );
                for ( int i = 0; i < keyValuePairs.length; i++ )
                {
                    Object key = keyValuePairs[i++];
                    Object value = keyValuePairs[i];
                    updateProperty( key, value, behaviour );
                }
            }

            private int collectiveNumberOfKeys( Object[] properties, Object[] otherProperties )
            {
                int collidingKeys = 0;
                for ( int i = 0; i < properties.length; i += 2 )
                {
                    Object key = properties[i];
                    for ( int j = 0; j < otherProperties.length; j += 2 )
                    {
                        Object otherKey = otherProperties[j];
                        if ( otherKey.equals( key ) )
                        {
                            collidingKeys++;
                            break;
                        }
                    }
                }
                return properties.length/2 + otherProperties.length/2 - collidingKeys;
            }

            private void updateProperty( Object key, Object value, UpdateBehaviour behaviour )
            {
                for ( int i = 0; i < properties.length; i++ )
                {
                    Object existingKey = properties[i++];
                    if ( existingKey.equals( key ) )
                    {   // update
                        properties[i] = behaviour.merge( properties[i], value );
                        return;
                    }
                }
            }

            private Object singleValueArray( Object value )
            {
                Object array = Array.newInstance( value.getClass(), 1 );
                Array.set( array, 0, value );
                return array;
            }
        };
    }
}
