/*
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
package org.neo4j.kernel.impl.api.store;

import org.neo4j.kernel.impl.api.cursor.RelationshipCursor;
import org.neo4j.kernel.impl.api.cursor.RelationshipSelection;
import org.neo4j.kernel.impl.store.RelationshipStore;

class StoreRelationshipCursor extends StoreEntityCursor implements RelationshipCursor
{
    private final RelationshipStore relationshipStore;
    private final RelationshipSelection selection;
    private final long nextRel;

    StoreRelationshipCursor( RelationshipStore relationshipStore, RelationshipSelection selection, long nextRel )
    {
        this.relationshipStore = relationshipStore;
        this.selection = selection;
        this.nextRel = nextRel;
    }

    @Override
    public long startNode()
    {
        return 0;
    }

    @Override
    public long endNode()
    {
        return 0;
    }

    @Override
    public int type()
    {
        return 0;
    }
}
