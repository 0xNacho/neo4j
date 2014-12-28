/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.consistency.checking.fast;

import org.junit.Rule;
import org.junit.Test;

import org.neo4j.consistency.checking.GraphStoreFixture;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.fail;

import static org.neo4j.consistency.checking.fast.Pointers.IN_USE;
import static org.neo4j.consistency.checking.fast.Pointers.NODE_FIRST_PROPERTY;

public class CrossStorePointerCheckerTest
{
    @Test
    public void shouldDoShit() throws Exception
    {
        // GIVEN
        Checker checker = new CrossStorePointerChecker<>(
                // from node
                store.directStoreAccess().nativeStores().getNodeStore(), NODE_FIRST_PROPERTY,
                // to property
                store.directStoreAccess().nativeStores().getPropertyStore(), IN_USE, null );

        // WHEN
        checker.check();

        // THEN
        fail( "Test not fully implemented" );
    }

    public final @Rule GraphStoreFixture store = new GraphStoreFixture()
    {
        @Override
        protected void generateInitialData( GraphDatabaseService db )
        {
            try ( org.neo4j.graphdb.Transaction tx = db.beginTx() )
            {
                Node node = db.createNode();
                node.setProperty( "name", "something" );
                tx.success();
            }
        }
    };
}
