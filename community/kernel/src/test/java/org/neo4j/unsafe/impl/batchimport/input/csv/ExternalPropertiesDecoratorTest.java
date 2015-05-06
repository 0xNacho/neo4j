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

import org.junit.Test;

import java.io.StringReader;
import java.util.Map;

import org.neo4j.csv.reader.CharReadable;
import org.neo4j.csv.reader.Readables;
import org.neo4j.csv.reader.SourceTraceability;
import org.neo4j.function.Factory;
import org.neo4j.function.Function;
import org.neo4j.unsafe.impl.batchimport.input.Groups;
import org.neo4j.unsafe.impl.batchimport.input.InputEntity;
import org.neo4j.unsafe.impl.batchimport.input.InputNode;
import org.neo4j.unsafe.impl.batchimport.input.csv.Configuration.OverrideFromConfig;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.neo4j.csv.reader.CharSeekers.charSeeker;
import static org.neo4j.csv.reader.Readables.wrap;
import static org.neo4j.helpers.collection.MapUtil.map;
import static org.neo4j.unsafe.impl.batchimport.input.InputEntityDecorators.NO_NODE_DECORATOR;
import static org.neo4j.unsafe.impl.batchimport.input.csv.DataFactories.defaultFormatNodeFileHeader;

public class ExternalPropertiesDecoratorTest
{
    @Test
    public void shouldCombineNodesWithExternalPropertiesFile() throws Exception
    {
        // GIVEN
        String propertyData =
                ":ID,email:string\n" +
                "1,mattias@some.com\n" +
                "1,mattiasp@someother.com\n" +
                "3,chris@abc\n" +
                "4,dude@yo";
        Configuration config = config();
        IdType idType = IdType.STRING;
        Function<Builder<InputNode>,Builder<InputNode>> externalPropertiesDecorator = new ExternalPropertiesDecorator(
                DataFactories.<InputNode>data( NO_NODE_DECORATOR, readable( propertyData ) ),
                defaultFormatNodeFileHeader(), config, idType, false );
        SourceTraceability source = mock( SourceTraceability.class );
        when( source.sourceDescription() ).thenReturn( "source" );
        Header nodeHeader = defaultFormatNodeFileHeader().create(
                charSeeker( wrap( new StringReader( ":ID,key,email" ) ), 1_000, false, '"' ), config, idType );

        // WHEN
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "1", "key", "value1" ),
                "key", "value1", "email", new String[] {"mattias@some.com", "mattiasp@someother.com" } );
        // simulate there being a node in between here that has no corresponding external property
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "2", "key", "value2" ),
                "key", "value2" );
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "3", "key", "value3" ),
                "key", "value3", "email", "chris@abc" );
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "4", "key", "value4" ),
                "key", "value4", "email", "dude@yo" );
    }

    @Test
    public void shouldForceCreatingArraysIfToldTo() throws Exception
    {
        // GIVEN
        String propertyData =
                ":ID,email:string\n" +
                "1,mattias@some.com\n" +
                "1,mattiasp@someother.com\n" +
                "3,chris@abc\n" +
                "4,dude@yo";
        Configuration config = config();
        IdType idType = IdType.STRING;
        Function<Builder<InputNode>,Builder<InputNode>> externalPropertiesDecorator = new ExternalPropertiesDecorator(
                DataFactories.<InputNode>data( NO_NODE_DECORATOR, readable( propertyData ) ),
                defaultFormatNodeFileHeader(), config, idType, true );
        SourceTraceability source = mock( SourceTraceability.class );
        when( source.sourceDescription() ).thenReturn( "source" );
        Header nodeHeader = defaultFormatNodeFileHeader().create(
                charSeeker( wrap( new StringReader( ":ID,key,email" ) ), 1_000, false, '"' ), config, idType );

        // WHEN
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "1", "key", "value1" ),
                "key", "value1", "email", new String[] {"mattias@some.com", "mattiasp@someother.com" } );
        // simulate there being a node in between here that has no corresponding external property
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "2", "key", "value2" ),
                "key", "value2" );
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "3", "key", "value3" ),
                "key", "value3", "email", new String[] {"chris@abc"} );
        assertProperties( build( externalPropertiesDecorator, source, nodeHeader, "4", "key", "value4" ),
                "key", "value4", "email", new String[] {"dude@yo"} );
    }

    private InputNode build( Function<Builder<InputNode>,Builder<InputNode>> externalPropertiesDecorator,
            SourceTraceability source, Header nodeHeader, Object id, Object... props )
    {
        Builder<InputNode> builder = externalPropertiesDecorator.apply(
                new InputNodeBuilder( source, nodeHeader, new Groups(), true ) );
        builder.handle( new Header.Entry( null, Type.ID, null, null ), id );
        for ( int i = 0; i < props.length; i++ )
        {
            builder.handle( new Header.Entry( (String) props[i++], Type.PROPERTY, null, null ), props[i] );
        }
        return builder.materialize();
    }

    private <ENTITY> Builder<ENTITY> singleEntityBuilder( final ENTITY entity )
    {
        return new Builder.Adapter<ENTITY>()
        {
            @Override
            public ENTITY materialize()
            {
                return entity;
            }
        };
    }

    private void assertProperties( InputNode decoratedNode, Object... expectedKeyValuePairs )
    {
        Map<String,Object> expectedProperties = map( expectedKeyValuePairs );
        Map<String,Object> properties = map( decoratedNode.properties() );
        assertEquals( properties + " vs expected " + expectedProperties, expectedProperties.size(), properties.size() );
        for ( Map.Entry<String,Object> expectedProperty : expectedProperties.entrySet() )
        {
            Object value = properties.get( expectedProperty.getKey() );
            assertNotNull( value );
            assertEquals( expectedProperty.getValue().getClass(), value.getClass() );
            if ( value.getClass().isArray() )
            {
                assertArrayEquals( (Object[]) expectedProperty.getValue(), (Object[]) value );
            }
            else
            {
                assertEquals( expectedProperty.getValue(), value );
            }
        }
    }

    private Builder<InputNode> node( Object id, Object... props )
    {
        return singleEntityBuilder( new InputNode( "source", 1, 0, id, props, null, InputEntity.NO_LABELS, null ) );
    }

    private Factory<CharReadable> readable( final String data )
    {
        return new Factory<CharReadable>()
        {
            @Override
            public CharReadable newInstance()
            {
                return Readables.wrap( new StringReader( data ) );
            }
        };
    }

    private OverrideFromConfig config()
    {
        return new Configuration.OverrideFromConfig( Configuration.COMMAS )
        {
            @Override
            public int bufferSize()
            {
                return 1_000;
            }
        };
    }
}
