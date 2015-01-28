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
package org.neo4j.collection.primitive.hopscotch;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.collection.primitive.hopscotch.TestResources.PrimitiveCollectionTestResource;
import org.neo4j.function.Factory;
import org.neo4j.test.randomized.Action;
import org.neo4j.test.randomized.RandomizedTester;
import org.neo4j.test.randomized.RandomizedTester.ActionFactory;
import org.neo4j.test.randomized.Result;
import org.neo4j.test.randomized.TestResource;

import static org.junit.Assert.fail;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;

@Ignore( "Not a test, a base class for thoroughly and randomly testing primitive collection implementations" )
public abstract class PrimitiveCollectionRIT<VALUE,TARGET extends PrimitiveCollectionTestResource<VALUE>>
{
    protected final Random random = new Random();
    private final Class<TARGET> targetClass;

    protected PrimitiveCollectionRIT( Class<TARGET> targetClass )
    {
        this.targetClass = targetClass;
    }

    @Test
    public void thoroughlyTestIt() throws Exception
    {
        long endTime = currentTimeMillis() + SECONDS.toMillis( 5 );
        while ( currentTimeMillis() < endTime )
        {
            long seed = currentTimeMillis();
            final Random random = new Random( seed );
            int max = random.nextInt( 10_000 ) + 100;

            RandomizedTester<TARGET,String> actions = new RandomizedTester<>( targetFactory(), actionFactory() );

            Result<? extends TestResource,String> result = actions.run( max );
            if ( result.isFailure() )
            {
                System.out.println( "Found failure at " + result );
                actions.testCaseWriter( "shouldOnlyContainAddedValues" ).print( System.out );
                System.out.println( "Actually, minimal reproducible test of that is..." );
                actions.findMinimalReproducible().testCaseWriter( "shouldContainExpectedData" ).print( System.out );
                fail( "Failed, see printed test case for how to reproduce. Seed:" + seed );
            }
        }
    }

    private ActionFactory<TARGET,String> actionFactory()
    {
        return new ActionFactory<TARGET,String>()
        {
            @Override
            public Action<TARGET,String> apply( TARGET from )
            {
                return generateAction( random, from );
            }
        };
    }

    private Factory<TARGET> targetFactory()
    {
        return new Factory<TARGET>()
        {
            @Override
            public TARGET newInstance()
            {
                try
                {
                    return targetClass.newInstance();
                }
                catch ( Exception e )
                {
                    throw new RuntimeException( e );
                }
            }
        };
    }

    protected Action<TARGET,String> generateAction( Random random, TARGET from )
    {
        boolean anExisting = from.size() > 0 && random.nextInt( 3 ) == 0;
        long key = anExisting ?
                randomExistingKey( random, from ) :
                randomNonExistentKey( random, from );
        VALUE value = generateValue();

        int typeOfAction = random.nextInt( 5 );
        if ( typeOfAction == 0 )
        {   // remove
            return newRemoveAction( key, value );
        }

        // add
        return newAddAction( key, value );
    }

    protected abstract Action<TARGET,String> newAddAction( long key, VALUE value );

    protected abstract Action<TARGET,String> newRemoveAction( long key, VALUE value );

    protected VALUE generateValue()
    {
        return null;
    }

    private long randomNonExistentKey( Random random, TARGET from )
    {
        while ( true )
        {
            long value = Math.abs( random.nextLong() );
            if ( !from.containsKey( value ) )
            {
                return value;
            }
        }
    }

    private long randomExistingKey( Random random, TARGET from )
    {
        int index = random.nextInt( from.size() )+1;
        PrimitiveLongIterator iterator = from.keyIterator();
        long key = 0;
        for ( int i = 0; i < index; i++ )
        {
            key = iterator.next();
        }
        return key;
    }

    protected static String capitilize( boolean bool )
    {
        String string = Boolean.valueOf( bool ).toString();
        return string.substring( 0, 1 ).toUpperCase() + string.substring( 1 ).toLowerCase();
    }
}
