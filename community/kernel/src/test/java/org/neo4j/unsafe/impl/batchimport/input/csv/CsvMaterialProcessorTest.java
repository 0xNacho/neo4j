package org.neo4j.unsafe.impl.batchimport.input.csv;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.neo4j.csv.reader.ControlledBufferedCharSeeker;
import org.neo4j.csv.reader.SourceTraceability;
import org.neo4j.kernel.impl.storemigration.StoreSourceTraceability;
import org.neo4j.reader.Processor;
import org.neo4j.reader.Source;
import org.neo4j.reader.csv.CsvRawMaterial;
import org.neo4j.reader.csv.CsvSource;
import org.neo4j.reader.csv.Reader;
import org.neo4j.reader.csv.Readers;
import org.neo4j.test.Race;
import org.neo4j.unsafe.impl.batchimport.input.Groups;
import org.neo4j.unsafe.impl.batchimport.input.InputNode;

import static java.nio.charset.Charset.forName;

import static org.neo4j.helpers.Format.duration;
import static org.neo4j.reader.csv.Readers.forInputStream;
import static org.neo4j.unsafe.impl.batchimport.input.csv.Configuration.COMMAS;

public class CsvMaterialProcessorTest
{
    @Test
    public void shouldDoTheCsvProcessingThing() throws Throwable
    {
        final Configuration config = COMMAS;
        Reader reader = forInputStream( input(), "yada", forName( "utf-8" ), config.bufferSize() );
        final Header header = extractHeader( reader );
        final Source<CsvRawMaterial> source = new CsvSource( reader, config.bufferSize() );
        final SourceTraceability traceability = new StoreSourceTraceability( "dfhkjj", 10 );
        Processor<CsvRawMaterial,InputNode> processor = new CsvMaterialProcessor<>( config, header,
                new InputNodeDeserialization( traceability, header, new Groups(), true ), InputNode.class );

        final Queue<CsvRawMaterial> materials = new ArrayBlockingQueue<>( 30 );
        final AtomicBoolean end = new AtomicBoolean();
        final AtomicInteger totalCount = new AtomicInteger();
        Race race = new Race();
        for ( int i = 0; i < 4; i++ )
        {
            race.addContestant( new Runnable()
            {
                @Override
                public void run()
                {
                    Processor<CsvRawMaterial,InputNode> processor = new CsvMaterialProcessor<>( config, header.clone(),
                            new InputNodeDeserialization( traceability, header,
                                    new Groups(), true ), InputNode.class );

                    int count = 0;
                    while ( true )
                    {
                        CsvRawMaterial material = materials.poll();
                        if ( material == null )
                        {
                            if ( end.get() )
                            {
                                break;
                            }
                            try
                            {
                                Thread.sleep( 1 );
                            }
                            catch ( InterruptedException e )
                            {
                                throw new RuntimeException( e );
                            }
                            continue;
                        }
                        int c = processor.apply( material ).length;
                        System.out.println( c );
                        count += c;
                    }
                }
            } );
        }
        race.addContestant( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CsvRawMaterial material = null;
                    while ( (material = source.next()) != null )
                    {
                        while ( !materials.offer( material ) )
                        {
                            Thread.sleep( 1 );
                        }
                    }
                    end.set( true );
                }
                catch ( Exception e )
                {
                    throw new RuntimeException( e );
                }
            }
        } );
        long time = System.currentTimeMillis();
        race.go();
        time = System.currentTimeMillis()-time;
        System.out.println( duration( time ) + " parsing " + totalCount + " entities" );
        source.close();
    }

    private Header extractHeader( Reader reader ) throws IOException
    {
        Reader singleLine = Readers.oneLineOf( reader );
        char[] buffer = new char[10_000];
        int read = singleLine.read( buffer );
        try ( ControlledBufferedCharSeeker parser = new ControlledBufferedCharSeeker( COMMAS ) )
        {
            parser.giveData( buffer, read, "header" );
            return DataFactories.defaultFormatNodeFileHeader().create( parser, COMMAS, IdType.STRING );
        }
    }

    private InputStream input() throws FileNotFoundException
    {
        return new FileInputStream(
                new File( "C:\\Users\\Matilas\\dev\\neo4j\\neo4j\\community\\import-tool\\target\\nodes.csv" ) );
    }
}
