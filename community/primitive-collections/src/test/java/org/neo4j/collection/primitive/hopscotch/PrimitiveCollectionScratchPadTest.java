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
import org.neo4j.collection.primitive.PrimitiveLongSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrimitiveCollectionScratchPadTest
{
    @Test
    public void shouldOnlyContainAddedValues() throws Exception
    {
        // GIVEN
        PrimitiveLongSet set = Primitive.longSet();
        set.remove( 7674816551404805368L );
        set.remove( 9065945518293259861L );
        set.add( 5448152767464273014L );
        set.add( 5448152767464273014L );
        set.add( 5329744275006658514L );
        set.remove( 5448152767464273014L );
        set.remove( 5329744275006658514L );
        set.remove( 3577366136656480011L );
        set.add( 8896357306703152668L );
        set.add( 6339285844872567732L );
        set.remove( 6414728979239891317L );
        set.add( 9004470511856605432L );
        set.add( 5092771043704664374L );
        set.add( 6339285844872567732L );
        set.add( 679853127148551587L );
        set.add( 8201204846073810321L );
        set.add( 7543213924739801455L );
        set.add( 6339285844872567732L );
        set.add( 1685063052496924247L );
        set.add( 6440145555015871348L );
        set.add( 8201204846073810321L );
        set.add( 1663075004675006388L );
        set.add( 3605114121902147904L );
        set.remove( 2813207776261138449L );
        set.add( 5092771043704664374L );
        set.remove( 5533206057376566781L );
        set.add( 8201204846073810321L );
        set.add( 4151803240726510862L );
        set.remove( 3605114121902147904L );
        set.remove( 7565867765424859304L );
        set.add( 731507002360288506L );
        set.add( 1277385644447999766L );
        set.add( 8896357306703152668L );
        set.add( 6339285844872567732L );
        set.add( 6339285844872567732L );
        set.add( 3488081892392193796L );

        // WHEN/THEN
        boolean existedBefore = set.contains( 8931018280251638658L );
        boolean added = set.add( 8931018280251638658L );
        boolean existsAfter = set.contains( 8931018280251638658L );
        assertFalse( "8931018280251638658 should not exist before adding here", existedBefore );
        assertTrue( "8931018280251638658 should be reported as added here", added );
        assertTrue( "8931018280251638658 should exist", existsAfter );
    }
}
