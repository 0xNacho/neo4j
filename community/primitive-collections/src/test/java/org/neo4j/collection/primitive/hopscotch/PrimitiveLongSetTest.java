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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.neo4j.array.primitive.NumberArrayFactory;
import org.neo4j.collection.primitive.Primitive;
import org.neo4j.collection.primitive.PrimitiveIntSet;
import org.neo4j.collection.primitive.PrimitiveIntVisitor;
import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.collection.primitive.PrimitiveLongVisitor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PrimitiveLongSetTest
{
    private PrimitiveLongHashSet newSet()
    {
        return new PrimitiveLongHashSet( HashFunction.DEFAULT_HASHING, NumberArrayFactory.HEAP, 1 << 4 );
    }

    @Test
    public void shouldOnlyContainAddedValues() throws Exception
    {
        // GIVEN
        PrimitiveLongSet set = Primitive.longSet();
        set.add( 1782159662243139806L );
        set.add( 1998940269699431967L );
        set.add( 6237973095280222104L );
        set.remove( 2094308016346104534L );
        set.add( 6512105166676564768L );
        set.add( 6120498779828695406L );
        set.add( 6512105166676564768L );
        set.add( 715733209311190193L );
        set.add( 5579397148461206583L );
        set.remove( 8484687959885845004L );
        set.add( 715733209311190193L );
        set.add( 5495448419530643274L );
        set.add( 392053931805482047L );
        set.add( 5495448419530643274L );
        set.add( 1782159662243139806L );
        set.add( 6512105166676564768L );
        set.add( 981507809131455862L );
        set.add( 953475307207219665L );
        set.add( 8479551558857653641L );
        set.add( 7486245702546711200L );
        set.add( 6237973095280222104L );
        set.add( 4922210374246859883L );

        // WHEN/THEN
        boolean existedBefore = set.contains( 392053931805482047L );
        boolean removed = set.remove( 392053931805482047L );
        boolean existsAfter = set.contains( 392053931805482047L );
        assertTrue( "392053931805482047 should exist before removing here", existedBefore );
        assertTrue( "392053931805482047 should be reported as removed here", removed );
        assertFalse( "392053931805482047 should not exist", existsAfter );
    }


    @SuppressWarnings( "unchecked" )
    @Test
    public void longVisitorShouldSeeAllEntriesIfItDoesNotBreakOut()
    {
        // GIVEN
        PrimitiveLongSet set = Primitive.longSet();
        set.add( 1 );
        set.add( 2 );
        set.add( 3 );
        PrimitiveLongVisitor<RuntimeException> visitor = mock( PrimitiveLongVisitor.class );

        // WHEN
        set.visitKeys( visitor );

        // THEN
        verify( visitor ).visited( 1 );
        verify( visitor ).visited( 2 );
        verify( visitor ).visited( 3 );
        verifyNoMoreInteractions( visitor );
    }

    @Test
    public void longVisitorShouldNotSeeEntriesAfterRequestingBreakOut()
    {
        // GIVEN
        PrimitiveIntSet map = Primitive.intSet();
        map.add( 1 );
        map.add( 2 );
        map.add( 3 );
        map.add( 4 );
        final AtomicInteger counter = new AtomicInteger();

        // WHEN
        map.visitKeys( new PrimitiveIntVisitor<RuntimeException>()
        {
            @Override
            public boolean visited( int value )
            {
                return counter.incrementAndGet() > 2;
            }
        } );

        // THEN
        assertThat( counter.get(), is( 3 ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void intVisitorShouldSeeAllEntriesIfItDoesNotBreakOut()
    {
        // GIVEN
        PrimitiveIntSet set = Primitive.intSet();
        set.add( 1 );
        set.add( 2 );
        set.add( 3 );
        PrimitiveIntVisitor<RuntimeException> visitor = mock( PrimitiveIntVisitor.class );

        // WHEN
        set.visitKeys( visitor );

        // THEN
        verify( visitor ).visited( 1 );
        verify( visitor ).visited( 2 );
        verify( visitor ).visited( 3 );
        verifyNoMoreInteractions( visitor );
    }

    @Test
    public void intVisitorShouldNotSeeEntriesAfterRequestingBreakOut()
    {
        // GIVEN
        PrimitiveIntSet map = Primitive.intSet();
        map.add( 1 );
        map.add( 2 );
        map.add( 3 );
        map.add( 4 );
        final AtomicInteger counter = new AtomicInteger();

        // WHEN
        map.visitKeys( new PrimitiveIntVisitor<RuntimeException>()
        {
            @Override
            public boolean visited( int value )
            {
                return counter.incrementAndGet() > 2;
            }
        } );

        // THEN
        assertThat( counter.get(), is( 3 ) );
    }

    @Test
    public void shouldHandleEmptySet() throws Exception
    {
        // GIVEN
        PrimitiveLongSet set = Primitive.longSet( 0 );

        // THEN
        assertFalse( set.contains( 564 ) );
    }

}
