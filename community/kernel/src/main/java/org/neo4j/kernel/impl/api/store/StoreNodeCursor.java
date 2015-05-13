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

import org.neo4j.collection.primitive.PrimitiveIntIterator;
import org.neo4j.kernel.api.EntityType;
import org.neo4j.kernel.api.exceptions.EntityNotFoundException;
import org.neo4j.kernel.impl.api.cursor.NodeCursor;
import org.neo4j.kernel.impl.api.cursor.RelationshipCursor;
import org.neo4j.kernel.impl.api.cursor.RelationshipSelection;
import org.neo4j.kernel.impl.store.NodeStore;
import org.neo4j.kernel.impl.store.PropertyStore;
import org.neo4j.kernel.impl.store.RelationshipStore;
import org.neo4j.kernel.impl.store.record.NodeRecord;

class StoreNodeCursor extends StoreEntityCursor implements NodeCursor
{
    private final NodeStore nodeStore;
    private final PropertyStore propertyStore;
    private final RelationshipStore relationshipStore;
    private long id;

    // TODO We can do this better in the end, skip the record indirection as a whole perhaps
    private final NodeRecord nodeRecord = new NodeRecord( -1 );

    StoreNodeCursor( NodeStore nodeStore, PropertyStore propertyStore, RelationshipStore relationshipStore, long id )
            throws EntityNotFoundException
    {
        this.nodeStore = nodeStore;
        this.propertyStore = propertyStore;
        this.relationshipStore = relationshipStore;
        if ( !next( id ) )
        {
            throw new EntityNotFoundException( EntityType.NODE, id );
        }
    }

    @Override
    public long id()
    {
        return id;
    }

    @Override
    public boolean next( long id )
    {
        if ( nodeStore.loadRecord( id, nodeRecord ) == null )
        {
            return false;
        }
        this.id = id;
        return true;
    }

    @Override
    public boolean isDense()
    {
        return nodeRecord.isDense();
    }

    @Override
    public RelationshipCursor relationships( RelationshipSelection selection )
    {
        return new StoreRelationshipCursor( relationshipStore, selection, nodeRecord.getNextRel() );
    }

    @Override
    public boolean hasLabel( int labelId )
    {
        return false;
    }

    @Override
    public PrimitiveIntIterator labels()
    {
        return null;
    }
}
