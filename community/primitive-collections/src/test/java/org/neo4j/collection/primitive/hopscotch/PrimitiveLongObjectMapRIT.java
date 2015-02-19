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

import org.neo4j.collection.primitive.hopscotch.TestResources.PrimitiveLongObjectHashMapResource;
import org.neo4j.test.randomized.Action;
import org.neo4j.test.randomized.LinePrinter;

import static java.lang.String.format;

public class PrimitiveLongObjectMapRIT extends PrimitiveCollectionRIT<Integer,PrimitiveLongObjectHashMapResource>
{
    public PrimitiveLongObjectMapRIT()
    {
        super( PrimitiveLongObjectHashMapResource.class );
    }

    @Override
    protected Integer generateValue()
    {
        return Math.abs( random.nextInt() );
    }

    @Override
    protected Action<PrimitiveLongObjectHashMapResource,String> newAddAction( final long key, final Integer value )
    {
        return new Action<PrimitiveLongObjectHashMapResource,String>()
        {
            @Override
            public String apply( PrimitiveLongObjectHashMapResource target )
            {
                Integer existingValue = target.normalMap.get( key );
                int actualSizeBefore = target.normalMap.size();

                int sizeBefore = target.map.size();
                boolean existedBefore = target.map.containsKey( key );
                Integer valueBefore = target.map.get( key );
                Integer previous = target.map.put( key, value );
                boolean existsAfter = target.map.containsKey( key );
                Integer valueAfter = target.map.get( key );
                target.normalMap.put( key, value );
                int sizeAfter = target.map.size();

                int actualSizeAfter = target.normalMap.size();
                boolean existing = existingValue != null;
                boolean ok =
                        (sizeBefore == actualSizeBefore) &
                        (existedBefore == existing) &
                        (existing ? existingValue.equals( valueBefore ) : valueBefore == null) &
                        (existing ? previous.equals( existingValue ) : previous == null) &
                        (valueAfter != null && valueAfter.equals( value )) &
                        existsAfter &
                        (sizeAfter == actualSizeAfter);
                return ok ? null : "" + key + ":" + value + "," + existingValue + "," + existedBefore +
                        "," + previous + "," + existsAfter;
            }

            @Override
            public void printAsCode( PrimitiveLongObjectHashMapResource source, LinePrinter out,
                    boolean includeChecks )
            {
                Integer existingValue = source.normalMap.get( key );

                String addition = "map.put( " + key + "L, " + value + " );";
                if ( includeChecks )
                {
                    boolean existing = existingValue != null;
                    out.println( format( "int sizeBefore = map.size();" ) );
                    out.println( format( "boolean existedBefore = map.containsKey( %dL );", key ) );
                    out.println( format( "Integer valueBefore = map.get( %dL );", key ) );
                    out.println( format( "Integer previous = %s", addition ) );
                    out.println( format( "boolean existsAfter = map.containsKey( %dL );", key ) );
                    out.println( format( "Integer valueAfter = map.get( %dL );", key ) );
                    out.println( format( "int sizeAfter = map.size();" ) );

                    int actualSizeBefore = source.normalMap.size();
                    out.println( format( "assertEquals( \"%s\", %d, sizeBefore );",
                            "Size before put should have been " + actualSizeBefore, actualSizeBefore ) );
                    out.println( format( "assert%s( \"%s\", existedBefore );", capitilize( existing ),
                            key + " should " + (existing?"":"not ") + "exist before putting here" ) );
                    if ( existing )
                    {
                        out.println( format( "assertEquals( \"%s\", (Integer)%d, valueBefore );",
                                "value before should be " + existingValue, existingValue ) );
                        out.println( format( "assertEquals( \"%s\", (Integer)%d, previous );",
                                "value returned from put should be " + existingValue, existingValue ) );
                    }
                    else
                    {
                        out.println( format( "assertNull( \"%s\", valueBefore );",
                                "value before putting should be null" ) );
                        out.println( format( "assertNull( \"%s\", previous );",
                                "value returned from putting should be null" ) );
                    }
                    out.println( format( "assertTrue( \"%s\", existsAfter );",
                            key + " should exist" ) );
                    out.println( format( "assertEquals( \"%s\", (Integer)%d, valueAfter );",
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
    protected Action<PrimitiveLongObjectHashMapResource,String> newRemoveAction( final long key, Integer value )
    {
        return new Action<PrimitiveLongObjectHashMapResource,String>()
        {
            @Override
            public String apply( PrimitiveLongObjectHashMapResource target )
            {
                Integer existingValue = target.normalMap.get( key );

                boolean existedBefore = target.map.containsKey( key );
                Integer valueBefore = target.map.get( key );
                Integer removed = target.map.remove( key );
                boolean existsAfter = target.map.containsKey( key );
                Integer valueAfter = target.map.get( key );
                target.normalMap.remove( key );

                boolean existing = existingValue != null;
                boolean ok =
                        (existedBefore == existing) &
                        (existing ? valueBefore.equals( existingValue ) : valueBefore == null) &
                        (existing ? removed.equals( existingValue ) : removed == null) &
                        (valueAfter == null) & !existsAfter;
                return ok ? null : "" + key + "," + existingValue + "," + existedBefore +
                        "," + removed + "," + existsAfter;
            }

            @Override
            public void printAsCode( PrimitiveLongObjectHashMapResource source, LinePrinter out,
                    boolean includeChecks )
            {
                Integer existingValue = source.normalMap.get( key );

                String removal = "map.remove( " + key + "L );";
                if ( includeChecks )
                {
                    boolean existing = existingValue != null;
                    out.println( format( "boolean existedBefore = map.containsKey( %dL );", key ) );
                    out.println( format( "Integer valueBefore = map.get( %dL );", key ) );
                    out.println( format( "Integer removed = %s", removal ) );
                    out.println( format( "boolean existsAfter = map.containsKey( %dL );", key ) );
                    out.println( format( "Integer valueAfter = map.get( %dL );", key ) );

                    out.println( format( "assert%s( \"%s\", existedBefore );", capitilize( existing ),
                            key + " should " + (existing?"":"not ") + "exist before putting here" ) );
                    if ( existing )
                    {
                        out.println( format( "assertEquals( \"%s\", (Integer)%d, valueBefore );",
                                "value before should be " + existingValue, existingValue ) );
                        out.println( format( "assertEquals( \"%s\", (Integer)%d, removed );",
                                "value returned from put should be " + existingValue, existingValue ) );
                    }
                    else
                    {
                        out.println( format( "assertNull( \"%s\", valueBefore );",
                                "value before putting should be null" ) );
                        out.println( format( "assertNull( \"%s\", removed );",
                                "value returned from putting should be null" ) );
                    }
                    out.println( format( "assertFalse( \"%s\", existsAfter );",
                            key + " should not exist" ) );
                    out.println( format( "assertNull( \"%s\", valueAfter );",
                            "value after removing should be null" ) );
                }
                else
                {
                    out.println( removal );
                }
            }
        };
    }
}
