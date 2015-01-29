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

import org.neo4j.collection.primitive.hopscotch.TestResources.PrimitiveLongIntHashMapResource;
import org.neo4j.test.randomized.Action;
import org.neo4j.test.randomized.LinePrinter;

import static java.lang.String.format;

public class PrimitiveLongIntMapRIT extends PrimitiveCollectionRIT<Integer,PrimitiveLongIntHashMapResource>
{
    public PrimitiveLongIntMapRIT()
    {
        super( PrimitiveLongIntHashMapResource.class );
    }

    @Override
    protected Integer generateValue()
    {
        return Math.abs( random.nextInt() );
    }

    @Override
    protected Action<PrimitiveLongIntHashMapResource,String> newAddAction( final long key, final Integer value )
    {
        return new Action<PrimitiveLongIntHashMapResource,String>()
        {
            @Override
            public String apply( PrimitiveLongIntHashMapResource target )
            {
                boolean existing = target.normalMap.containsKey( key );
                int existingValue = existing ? target.normalMap.get( key ) : -1;
                int actualSizeBefore = target.normalMap.size();

                int sizeBefore = target.map.size();
                boolean existedBefore = target.map.containsKey( key );
                int valueBefore = target.map.get( key );
                int previous = target.map.put( key, value );
                boolean existsAfter = target.map.containsKey( key );
                int valueAfter = target.map.get( key );
                target.normalMap.put( key, value );
                int sizeAfter = target.map.size();

                int actualSizeAfter = target.normalMap.size();
                boolean ok =
                        (sizeBefore == actualSizeBefore) &
                        (existedBefore == existing) &
                        (existingValue == valueBefore) &
                        (existingValue == previous) &
                        (valueAfter == value) &
                        existsAfter &
                        (sizeAfter == actualSizeAfter);
                return ok ? null : "" + key + ":" + value + "," + existingValue + "," + existedBefore +
                        "," + previous + "," + existsAfter;
            }

            @Override
            public void printAsCode( PrimitiveLongIntHashMapResource source, LinePrinter out, boolean includeChecks )
            {
                String addition = "map.put( " + key + "L, " + value + " );";
                if ( includeChecks )
                {
                    boolean existing = source.normalMap.containsKey( key );
                    int existingValue = existing ? source.normalMap.get( key ) : -1;
                    out.println( format( "int sizeBefore = map.size();" ) );
                    out.println( format( "boolean existedBefore = map.containsKey( %dL );", key ) );
                    out.println( format( "int valueBefore = map.get( %dL );", key ) );
                    out.println( format( "int previous = %s", addition ) );
                    out.println( format( "boolean existsAfter = map.containsKey( %dL );", key ) );
                    out.println( format( "int valueAfter = map.get( %dL );", key ) );
                    out.println( format( "int sizeAfter = map.size();" ) );

                    int actualSizeBefore = source.normalMap.size();
                    out.println( format( "assertEquals( \"%s\", %d, sizeBefore );",
                            "Size before put should have been " + actualSizeBefore, actualSizeBefore ) );
                    out.println( format( "assert%s( \"%s\", existedBefore );", capitilize( existing ),
                            key + " should " + (existing?"":"not ") + "exist before putting here" ) );
                    out.println( format( "assertEquals( \"%s\", %d, valueBefore );",
                            "value before should be " + existingValue, existingValue ) );
                    out.println( format( "assertEquals( \"%s\", %d, previous );",
                            "value returned from put should be " + existingValue, existingValue ) );
                    out.println( format( "assertTrue( \"%s\", existsAfter );",
                            key + " should exist" ) );
                    out.println( format( "assertEquals( \"%s\", %d, valueAfter );",
                            "value after putting should be " + value, value ) );
                    int actualSizeAfter = existing ? actualSizeBefore : actualSizeBefore+1;
                    out.println( format( "assertEquals( \"%s\", %d, sizeAfter );",
                            "Size after put should have been " + actualSizeAfter, actualSizeAfter ) );
                }
                else
                {
                    out.println( addition );
                }
            }
        };
    }

    @Override
    protected Action<PrimitiveLongIntHashMapResource,String> newRemoveAction( final long key, Integer value )
    {
        return new Action<PrimitiveLongIntHashMapResource,String>()
        {
            @Override
            public String apply( PrimitiveLongIntHashMapResource target )
            {
                boolean existing = target.normalMap.containsKey( key );
                int existingValue = existing ? target.normalMap.get( key ) : -1;

                boolean existedBefore = target.map.containsKey( key );
                int valueBefore = target.map.get( key );
                int removed = target.map.remove( key );
                boolean existsAfter = target.map.containsKey( key );
                int valueAfter = target.map.get( key );
                target.normalMap.remove( key );

                boolean ok =
                        (existedBefore == existing) &
                        (existingValue == valueBefore) &
                        (existingValue == removed) &
                        (valueAfter == -1) &
                        !existsAfter;
                return ok ? null : "" + key + "," + existingValue + "," + existedBefore +
                        "," + removed + "," + existsAfter;
            }

            @Override
            public void printAsCode( PrimitiveLongIntHashMapResource source, LinePrinter out, boolean includeChecks )
            {
                String removal = "map.remove( " + key + "L );";
                if ( includeChecks )
                {
                    boolean existing = source.normalMap.containsKey( key );
                    int existingValue = existing ? source.normalMap.get( key ) : -1;
                    out.println( format( "boolean existedBefore = map.containsKey( %dL );", key ) );
                    out.println( format( "int valueBefore = map.get( %dL );", key ) );
                    out.println( format( "int removed = %s", removal ) );
                    out.println( format( "boolean existsAfter = map.containsKey( %dL );", key ) );
                    out.println( format( "int valueAfter = map.get( %dL );", key ) );

                    out.println( format( "assert%s( \"%s\", existedBefore );", capitilize( existing ),
                            key + " should " + (existing?"":"not ") + "exist before removing here" ) );
                    out.println( format( "assertEquals( \"%s\", %d, valueBefore );",
                            "value before should be " + existingValue, existingValue ) );
                    out.println( format( "assertEquals( \"%s\", %d, removed );",
                            "value returned from remove should be " + existingValue, existingValue ) );
                    out.println( format( "assertFalse( \"%s\", existsAfter );",
                            key + " should not exist" ) );
                    out.println( format( "assertEquals( \"%s\", -1, valueAfter );",
                            "value after removing should be -1" ) );
                }
                else
                {
                    out.println( removal );
                }
            }
        };
    }
}
