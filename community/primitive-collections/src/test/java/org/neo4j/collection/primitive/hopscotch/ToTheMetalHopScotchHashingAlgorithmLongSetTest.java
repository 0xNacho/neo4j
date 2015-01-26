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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import static java.lang.Math.abs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ToTheMetalHopScotchHashingAlgorithmLongSetTest
{
    @Test
    public void shouldWork() throws Exception
    {
        ToTheMetalHopScotchHashingAlgorithmLongSet set = new ToTheMetalHopScotchHashingAlgorithmLongSet();
        Set<Integer> expected = new HashSet<>();
        Random random = new Random();

        for ( int i = 0; i < 1_000; i++ )
        {
            int value = abs( random.nextInt( 4 ) );
            switch ( random.nextInt( 2 ) )
            {
            case 0: // add
                System.out.println( "add " + value );
                assertEquals( expected.add( value ), set.add( value ) );
                break;
            case 1: // contains
                System.out.println( "contains " + value );
                assertEquals( expected.contains( value ), set.contains( value ) );
                break;
            }
        }
    }

    @Test
    public void should() throws Exception
    {
        ToTheMetalHopScotchHashingAlgorithmLongSet set = new ToTheMetalHopScotchHashingAlgorithmLongSet();

        assertTrue( set.add( 2 ) );
        assertTrue( set.add( 1 ) );
        assertFalse( set.add( 2 ) );
        assertTrue( set.contains( 2 ) );
        assertFalse( set.contains( 3 ) );
        assertFalse( set.add( 2 ) );
        assertTrue( set.add( 0 ) );
        assertFalse( set.add( 2 ) );
        assertTrue( set.contains( 0 ) );
        assertFalse( set.add( 0 ) );
    }
}
