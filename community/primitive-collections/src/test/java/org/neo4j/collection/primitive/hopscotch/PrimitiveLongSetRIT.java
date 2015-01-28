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

import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.collection.primitive.hopscotch.TestResources.PrimitiveLongHashSetResource;
import org.neo4j.test.randomized.Action;
import org.neo4j.test.randomized.LinePrinter;

import static java.lang.String.format;

public class PrimitiveLongSetRIT extends PrimitiveCollectionRIT<Void,PrimitiveLongHashSetResource>
{
    public PrimitiveLongSetRIT()
    {
        super( PrimitiveLongHashSetResource.class );
    }

    @Override
    protected Action<PrimitiveLongHashSetResource,String> newAddAction( final long key, Void value )
    {
        return new Action<PrimitiveLongHashSetResource,String>()
        {
            @Override
            public String apply( PrimitiveLongHashSetResource target )
            {
                try
                {
                    boolean alreadyExisting = target.normalSet.contains( key );

                    PrimitiveLongSet set = target.set;
                    boolean existedBefore = set.contains( key );
                    boolean added = set.add( key );
                    boolean existsAfter = set.contains( key );
                    target.normalSet.add( key );

                    boolean ok = (existedBefore == alreadyExisting) & (added == !alreadyExisting) & existsAfter;
                    return ok ? null : "" + key + alreadyExisting + "," + existedBefore + "," + added + "," + existsAfter;
                }
                catch ( Exception e )
                {
                    return "exception:" + e.getMessage();
                }
            }

            @Override
            public void printAsCode( PrimitiveLongHashSetResource source, LinePrinter out, boolean includeChecks )
            {
                boolean alreadyExisting = source.normalSet.contains( key );
                String addition = "set.add( " + key + "L );";
                if ( includeChecks )
                {
                    out.println( format( "boolean existedBefore = set.contains( %dL );", key ) );
                    out.println( format( "boolean added = %s", addition ) );
                    out.println( format( "boolean existsAfter = set.contains( %dL );", key ) );
                    out.println( format( "assert%s( \"%s\", existedBefore );", capitilize( alreadyExisting ),
                            key + " should " + (alreadyExisting?"":"not ") + "exist before adding here" ) );
                    out.println( format( "assert%s( \"%s\", added );", capitilize( !alreadyExisting ),
                            key + " should " + (!alreadyExisting?"":"not ") + "be reported as added here" ) );
                    out.println( format( "assertTrue( \"%s\", existsAfter );",
                            key + " should exist" ) );
                }
                else
                {
                    out.println( addition );
                }
            }
        };
    }

    @Override
    protected Action<PrimitiveLongHashSetResource,String> newRemoveAction( final long key, Void value )
    {
        return new Action<PrimitiveLongHashSetResource,String>()
        {
            @Override
            public String apply( PrimitiveLongHashSetResource target )
            {
                try
                {
                    boolean alreadyExisting = target.normalSet.contains( key );
                    PrimitiveLongSet set = target.set;
                    boolean existedBefore = set.contains( key );
                    boolean removed = set.remove( key );
                    boolean existsAfter = set.contains( key );
                    target.normalSet.remove( key );

                    boolean ok = (existedBefore == alreadyExisting) & (removed == alreadyExisting) & !existsAfter;
                    return ok ? null : "" + key + alreadyExisting + "," + existedBefore + "," + removed + "," + existsAfter;
                }
                catch ( Exception e )
                {
                    return "exception: " + e.getMessage();
                }
            }

            @Override
            public void printAsCode( PrimitiveLongHashSetResource source, LinePrinter out, boolean includeChecks )
            {
                boolean alreadyExisting = source.normalSet.contains( key );
                String removal = "set.remove( " + key + "L );";
                if ( includeChecks )
                {
                    out.println( format( "boolean existedBefore = set.contains( %dL );", key ) );
                    out.println( format( "boolean removed = %s", removal ) );
                    out.println( format( "boolean existsAfter = set.contains( %dL );", key ) );
                    out.println( format( "assert%s( \"%s\", existedBefore );", capitilize( alreadyExisting ),
                            key + " should " + (alreadyExisting?"":"not ") + "exist before removing here" ) );
                    out.println( format( "assert%s( \"%s\", removed );", capitilize( alreadyExisting ),
                            key + " should " + (alreadyExisting?"":"not ") + "be reported as removed here" ) );
                    out.println( format( "assertFalse( \"%s\", existsAfter );",
                            key + " should not exist" ) );
                }
                else
                {
                    out.println( removal );
                }
            }
        };
    }
}
