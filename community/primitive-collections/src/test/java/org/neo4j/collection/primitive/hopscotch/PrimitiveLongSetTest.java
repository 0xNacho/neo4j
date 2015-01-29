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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import org.neo4j.collection.primitive.Primitive;
import org.neo4j.collection.primitive.PrimitiveIntSet;
import org.neo4j.collection.primitive.PrimitiveIntVisitor;
import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.collection.primitive.PrimitiveLongVisitor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PrimitiveLongSetTest
{
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
