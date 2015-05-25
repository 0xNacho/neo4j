package org.neo4j.unsafe.impl.batchimport.input.csv;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.csv.reader.ControlledBufferedCharSeeker;
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
    private final ControlledBufferedCharSeeker parser;
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
        this.parser = new ControlledBufferedCharSeeker( config );
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
