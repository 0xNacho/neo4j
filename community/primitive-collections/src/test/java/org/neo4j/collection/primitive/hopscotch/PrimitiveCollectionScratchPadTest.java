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

import org.neo4j.collection.primitive.Primitive;
import org.neo4j.collection.primitive.PrimitiveLongObjectMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PrimitiveCollectionScratchPadTest
{
    @Test
    public void shouldOnlyContainAddedValues() throws Exception
    {
        // GIVEN
        PrimitiveLongObjectMap<Integer> map = Primitive.longObjectMap();
        map.remove( 2594961505054962866L );
        map.put( 3591455405498939436L, 1586692009 );
        map.put( 3591455405498939436L, 2096941953 );
        map.put( 3370039946778124805L, 2006230179 );
        map.put( 3370039946778124805L, 717922382 );
        map.put( 3591455405498939436L, 662773739 );
        map.put( 3370039946778124805L, 1538510404 );
        map.put( 7958661502461218416L, 1662719918 );
        map.remove( 8685767270613519112L );
        map.put( 8920130466538479354L, 1617694941 );
        map.put( 3370039946778124805L, 260525065 );
        map.put( 1200487730846162100L, 1143841738 );
        map.put( 7838559825943394111L, 2117398429 );
        map.put( 1590910116521427942L, 1865824781 );
        map.put( 7838559825943394111L, 781278283 );
        map.remove( 7958661502461218416L );
        map.put( 8920130466538479354L, 1972871138 );
        map.put( 7838559825943394111L, 464772168 );
        map.put( 4267383227057247296L, 1394260047 );
        map.put( 9097938674946795511L, 947645099 );
        map.put( 8691969275931191929L, 1193382712 );
        map.remove( 3591455405498939436L );
        map.put( 709621884406701937L, 236223348 );
        map.put( 4390484047757874523L, 1887030634 );
        map.put( 1590910116521427942L, 1371217910 );
        map.put( 4390484047757874523L, 497345816 );
        map.put( 4032335737459559007L, 1749975122 );
        map.put( 4390484047757874523L, 361920165 );
        map.put( 6669782845312451860L, 214449690 );
        map.put( 3645944490305966759L, 1260277621 );
        map.put( 4796757694636829274L, 1981464707 );
        map.put( 3627549917774462835L, 420278072 );
        map.put( 2000529336825794299L, 885973155 );
        map.put( 7617847163236534985L, 1871260652 );
        map.put( 7838559825943394111L, 772203708 );
        map.put( 4116806826826798188L, 1636003926 );
        map.put( 6669782845312451860L, 1638891642 );
        map.put( 8493960171252293339L, 726954551 );
        map.put( 1739219706926778053L, 327814917 );
        map.put( 743460567003319082L, 2088452342 );
        map.put( 6350812272557272229L, 1352120602 );
        map.remove( 2000529336825794299L );
        map.remove( 4638806476764859482L );
        map.put( 7838559825943394111L, 1108416116 );
        map.put( 4116806826826798188L, 675818678 );
        map.remove( 4116806826826798188L );
        map.put( 153654215893094159L, 2002993437 );
        map.put( 8064664609161083426L, 1373037912 );
        map.remove( 8240444402626360205L );
        map.put( 1684259903015275450L, 2048133737 );
        map.put( 307387931688650203L, 823372645 );
        map.put( 307387931688650203L, 393541107 );
        map.put( 2135588129641767878L, 1751845126 );
        map.put( 6669782845312451860L, 361596771 );
        map.put( 1044300731351505700L, 1836708005 );
        map.put( 6488041016744044438L, 1421162930 );
        map.put( 743460567003319082L, 1329367251 );
        map.put( 4054874960948679566L, 885227376 );
        map.remove( 8064664609161083426L );
        map.remove( 3645944490305966759L );
        map.put( 5667890474623456750L, 1277835759 );
        map.put( 307387931688650203L, 184807894 );
        map.remove( 5019076004037832786L );
        map.put( 8728795041114329851L, 192972882 );
        map.put( 6651612472719392029L, 1193656301 );
        map.put( 9016547366678039845L, 1428695802 );
        map.remove( 5478470769249752884L );
        map.put( 2821381694741631363L, 1458548089 );
        map.remove( 2954797860938844833L );
        map.put( 3155245093924723409L, 1636209235 );

        // WHEN/THEN
        int sizeBefore = map.size();
        boolean existedBefore = map.containsKey( 2557025480072821847L );
        Integer valueBefore = map.get( 2557025480072821847L );
        Integer previous = map.put( 2557025480072821847L, 744750862 );
        boolean existsAfter = map.containsKey( 2557025480072821847L );
        Integer valueAfter = map.get( 2557025480072821847L );
        int sizeAfter = map.size();
        assertEquals( "Size before put should have been 32", 32, sizeBefore );
        assertFalse( "2557025480072821847 should not exist before putting here", existedBefore );
        assertNull( "value before putting should be null", valueBefore );
        assertNull( "value returned from putting should be null", previous );
        assertTrue( "2557025480072821847 should exist", existsAfter );
        assertEquals( "value after putting should be 744750862", (Integer)744750862, valueAfter );
        assertEquals( "Size after put should have been 33", 33, sizeAfter );
    }
}
