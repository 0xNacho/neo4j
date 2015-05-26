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
import java.util.ArrayList;
import java.util.List;

import org.neo4j.csv.reader.MinimumCharSeeker;
import org.neo4j.function.Functions;
import org.neo4j.kernel.impl.util.Validator;
import org.neo4j.reader.Processor;
import org.neo4j.reader.csv.CsvRawMaterial;
import org.neo4j.unsafe.impl.batchimport.input.InputEntity;

/**
 * Create one of these per thread and feed it multiple {@link CsvRawMaterial}.
 */
public class CsvMaterialProcessor<ENTITY extends InputEntity> implements Processor<CsvRawMaterial,ENTITY>
{
    private final MinimumCharSeeker parser;
    private final List<ENTITY> resultList = new ArrayList<>();
    private final Class<ENTITY> cls;
    private final Configuration config;
    private final Header header;
    private final Deserialization<ENTITY> deserialization;

    public CsvMaterialProcessor( Configuration config, Header header, Deserialization<ENTITY> deserialization,
            Class<ENTITY> cls )
    {
        this.config = config;
        this.header = header;
        this.deserialization = deserialization;
        this.cls = cls;
        this.parser = new MinimumCharSeeker( config );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public ENTITY[] apply( CsvRawMaterial material )
    {
        InputEntityDeserializer<ENTITY> deserializer = new InputEntityDeserializer<>( header, parser,
                config.delimiter(), deserialization, Functions.<ENTITY>identity(), new Validator<ENTITY>()
        {
            @Override
            public void validate( ENTITY value )
            {
            }
        } );

        try
        {
            resultList.clear();
            parser.giveData( material.getBuffer(), material.getLength(), material.getSourceDescription() );
            // here do the parse-n-populate-entities-thing
            while ( deserializer.hasNext() )
            {
                resultList.add( deserializer.next() );
            }

            // TODO we'd better be strict in the parsing here to remove surprises around unexpected newlines
            // here where we know we're dealing with single-line entities

            return resultList.toArray( (ENTITY[]) Array.newInstance( cls, resultList.size() ) );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            material.close();
        }
    }
}
