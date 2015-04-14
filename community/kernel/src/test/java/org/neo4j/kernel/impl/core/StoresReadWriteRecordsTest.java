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
package org.neo4j.kernel.impl.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import org.neo4j.io.fs.DefaultFileSystemAbstraction;
import org.neo4j.kernel.api.index.SchemaIndexProvider;
import org.neo4j.kernel.impl.store.DynamicStringStore;
import org.neo4j.kernel.impl.store.NeoStore;
import org.neo4j.kernel.impl.store.NodeStore;
import org.neo4j.kernel.impl.store.PropertyStore;
import org.neo4j.kernel.impl.store.RelationshipGroupStore;
import org.neo4j.kernel.impl.store.RelationshipStore;
import org.neo4j.kernel.impl.store.SchemaStore;
import org.neo4j.kernel.impl.store.StoreFactory;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.IndexRule;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.PropertyBlock;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.RelationshipGroupRecord;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.logging.NullLogProvider;
import org.neo4j.test.PageCacheRule;
import org.neo4j.test.TargetDirectory;
import org.neo4j.test.TargetDirectory.TestDirectory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StoresReadWriteRecordsTest
{
    @Test
    public void shouldWriteAndReadNodeRecord() throws Exception
    {
        // GIVEN
        NodeStore store = neoStore.getNodeStore();
        NodeRecord record = new NodeRecord( 0, true, true, 123, 456, 789 );

        // WHEN
        store.updateRecord( record );

        // THEN
        NodeRecord read = store.getRecord( 0 );
        assertEquals( record.inUse(), read.inUse() );
        assertEquals( record.isDense(), read.isDense() );
        assertEquals( record.getNextRel(), read.getNextRel() );
        assertEquals( record.getNextProp(), read.getNextProp() );
        assertEquals( record.getLabelField(), read.getLabelField() );
    }

    @Test
    public void shouldWriteAndReadPropertyRecordWithoutDynamicRecords() throws Exception
    {
        // GIVEN
        PropertyStore store = neoStore.getPropertyStore();
        PropertyRecord record = new PropertyRecord( 0 );
        record.setInUse( true );
        record.setPrevProp( 123 );
        record.setNextProp( 456 );
        PropertyBlock block1 = new PropertyBlock();
        store.encodeValue( block1, 1, 555L );
        record.addPropertyBlock( block1 );
        PropertyBlock block2 = new PropertyBlock();
        store.encodeValue( block2, 2, "My String" );
        record.addPropertyBlock( block2 );

        // WHEN
        store.updateRecord( record );

        // THEN
        PropertyRecord read = store.getRecord( 0 );
        assertEquals( record.inUse(), read.inUse() );
        assertEquals( record.getPrevProp(), read.getPrevProp() );
        assertEquals( record.getNextProp(), read.getNextProp() );
        Iterator<PropertyBlock> blocksIterator = read.iterator();
        PropertyBlock readBlock1 = blocksIterator.next();
        assertPropertyBlockEquals( block1, readBlock1 );
    }

    @Test
    public void shouldWriteAndReadRelationshipRecord() throws Exception
    {
        // GIVEN
        RelationshipStore store = neoStore.getRelationshipStore();
        RelationshipRecord record = new RelationshipRecord( 0, true, 123, 456, 789, 12345, 67890, 11111, 22222,
                true, false );

        // WHEN
        store.updateRecord( record );

        // THEN
        RelationshipRecord read = store.getRecord( 0 );
        assertEquals( record.inUse(), read.inUse() );
        assertEquals( record.getFirstNode(), read.getFirstNode() );
        assertEquals( record.getSecondNode(), read.getSecondNode() );
        assertEquals( record.getFirstPrevRel(), read.getFirstPrevRel() );
        assertEquals( record.getFirstNextRel(), read.getFirstNextRel() );
        assertEquals( record.getSecondPrevRel(), read.getSecondPrevRel() );
        assertEquals( record.getSecondNextRel(), read.getSecondNextRel() );
        assertEquals( record.isFirstInFirstChain(), read.isFirstInFirstChain() );
        assertEquals( record.isFirstInSecondChain(), read.isFirstInSecondChain() );
    }

    @Test
    public void shouldWriteAndReadRelationshipGroupRecord() throws Exception
    {
        // GIVEN
        RelationshipGroupStore store = neoStore.getRelationshipGroupStore();
        RelationshipGroupRecord record = new RelationshipGroupRecord( 0, 23, 123, 456, 789, 112233, 445566, true );

        // WHEN
        store.updateRecord( record );

        // THEN
        RelationshipGroupRecord read = store.getRecord( 0 );
        assertEquals( record.getNext(), read.getNext() );
        assertEquals( record.getFirstOut(), read.getFirstOut() );
        assertEquals( record.getFirstIn(), read.getFirstIn() );
        assertEquals( record.getFirstLoop(), read.getFirstLoop() );
        assertEquals( record.getOwningNode(), read.getOwningNode() );
        assertEquals( record.getType(), read.getType() );
    }

    @Test
    public void shouldWriteAndReadDynamicRecordSpanningOneRecord() throws Exception
    {
        // GIVEN
        DynamicStringStore store = neoStore.getPropertyStore().getStringStore();
        DynamicRecord record = new DynamicRecord( 5 );
        record.setInUse( true );
        record.setStartRecord( true );
        record.setData( new byte[] {1,2,3,4,5,6,7,8,9,0} );
        // type is dependent on the data it seems, so don't test it specifically

        // WHEN
        store.updateRecord( record );

        // THEN
        DynamicRecord read = store.getRecord( record.getId() );
        assertDynamicRecordEquals( record, read );
        // type is dependent on the data it seems, so don't test it specifically
    }

    @Test
    public void shouldWriteAndReadSchemaRecord() throws Exception
    {
        // GIVEN
        SchemaStore store = neoStore.getSchemaStore();
        IndexRule rule = IndexRule.indexRule( 10, 12, 14, new SchemaIndexProvider.Descriptor( "key", "version" ) );
        Collection<DynamicRecord> records = store.allocateFrom( rule );

        // WHEN
        for ( DynamicRecord record : records )
        {
            store.updateRecord( record );
        }

        // THEN
        Collection<DynamicRecord> readRecords = store.getRecords( rule.getId() );
        Iterator<DynamicRecord> recordIterator = records.iterator();
        Iterator<DynamicRecord> readIterator = readRecords.iterator();
        while ( recordIterator.hasNext() )
        {
            assertTrue( readIterator.hasNext() );
            DynamicRecord record = recordIterator.next();
            DynamicRecord read = readIterator.next();
            assertDynamicRecordEquals( record, read );
        }
        assertEquals( rule, SchemaStore.readSchemaRule( rule.getId(), readRecords ) );
    }

    private void assertDynamicRecordEquals( DynamicRecord record, DynamicRecord read )
    {
        assertEquals( record.inUse(), read.inUse() );
        assertArrayEquals( record.getData(), read.getData() );
        assertEquals( record.getLength(), read.getLength() );
        assertEquals( record.getNextBlock(), read.getNextBlock() );
    }

    private void assertPropertyBlockEquals( PropertyBlock expected, PropertyBlock read )
    {
        assertArrayEquals( expected.getValueBlocks(), read.getValueBlocks() );
    }

    public final @Rule TestDirectory directory = TargetDirectory.testDirForTest( getClass() );
    public final @Rule PageCacheRule pageCacheRule = new PageCacheRule();
    private NeoStore neoStore;

    @Before
    public void setup()
    {
        DefaultFileSystemAbstraction fs = new DefaultFileSystemAbstraction();
        StoreFactory storeFactory = new StoreFactory( fs, directory.directory(),
                pageCacheRule.getPageCache( fs ), NullLogProvider.getInstance(), new Monitors() );
        neoStore = storeFactory.createNeoStore();
    }

    @After
    public void close()
    {
        neoStore.close();
    }
}
