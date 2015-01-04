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
package org.neo4j.consistency.checking.fast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.neo4j.function.primitive.FunctionToPrimitiveLong;
import org.neo4j.graphdb.mockfs.EphemeralFileSystemAbstraction;
import org.neo4j.helpers.Triplet;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.impl.store.format.Store;
import org.neo4j.kernel.impl.store.format.Store.RecordCursor;
import org.neo4j.kernel.impl.store.format.TestFormatWithHeader;
import org.neo4j.kernel.impl.store.format.TestRecord;
import org.neo4j.kernel.impl.store.impl.TestStoreIdGenerator;
import org.neo4j.kernel.impl.store.standard.StandardStore;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.unsafe.impl.batchimport.store.BatchingPageCache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.neo4j.unsafe.impl.batchimport.store.BatchingPageCache.SYNCHRONOUS;
import static org.neo4j.unsafe.impl.batchimport.store.BatchingPageCache.Mode.UPDATE;
import static org.neo4j.unsafe.impl.batchimport.store.io.Monitor.NO_MONITOR;

public class CrossStorePointerCheckerTest
{
    @Test
    public void shouldDoShit() throws Exception
    {
        // GIVEN
        Store<TestRecord,RecordCursor<TestRecord>> store1 = newStore( "store1", 5, -1, 3, 10 );
        Store<TestRecord,RecordCursor<TestRecord>> store2 = newStore( "store2", -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, 1 );
        Verifier verifier = new Verifier();
        Checker checker = new CrossStorePointerChecker<>(
                store1, TEST_RECORD_KEY,
                store2, TEST_RECORD_KEY, verifier );

        // WHEN
        checker.check();

        // THEN
        verifier.verifyCalled( record( 0L, 5L ), 5L, 1L );
        verifier.verifyCalled( record( 2L, 3L ), 3L, 1L );
        verifier.verifyCalled( record( 3L, 10L ), 10L, 1L );
    }

    private TestRecord record( long id, long value )
    {
        return new TestRecord( id, value );
    }

    private static final FunctionToPrimitiveLong<TestRecord> TEST_RECORD_KEY = new FunctionToPrimitiveLong<TestRecord>()
    {
        @Override
        public long apply( TestRecord value )
        {
            return value.value;
        }
    };

    private Store<TestRecord,RecordCursor<TestRecord>> newStore( String name, long... initialData ) throws IOException
    {
        StandardStore<TestRecord,RecordCursor<TestRecord>> store = new StandardStore<>(
                new TestFormatWithHeader( 9 ),
                new File( dir, name ),
                new TestStoreIdGenerator(),
                pageCache,
                fs,
                StringLogger.DEV_NULL );
        life.add( store );

        long id = 0;
        for ( long value : initialData )
        {
            store.write( new TestRecord( id++, value ) );
        }

        return store;
    }

    private static class Verifier implements ValueChecker<TestRecord>
    {
        private final List<Triplet<TestRecord,Long,Long>> calls = new ArrayList<>();

        @Override
        public boolean check( TestRecord atRecord, long toId, long toValue )
        {
            calls.add( entry( atRecord.clone(), toId, toValue ) );
            if ( atRecord.id != -1 )
            {
                assertEquals( 1L, toValue );
            }
            return false;
        }

        private Triplet<TestRecord,Long,Long> entry( TestRecord atRecord, long toId, long toValue )
        {
            return Triplet.of( atRecord, toId, toValue );
        }

        void verifyCalled( TestRecord record, long toId, long toValue )
        {
            Triplet<TestRecord,Long,Long> entry = entry( record, toId, toValue );
            for ( Triplet<TestRecord,Long,Long> call : calls )
            {
                if ( call.equals( entry ) )
                {
                    return;
                }
            }
            fail( "Not called with " + entry );
        }
    }

    private EphemeralFileSystemAbstraction fs;
    private PageCache pageCache;
    private final LifeSupport life = new LifeSupport();
    private final File dir = new File( "dir" );

    @Before
    public void before()
    {
        fs = new EphemeralFileSystemAbstraction();
        pageCache = new BatchingPageCache( fs, 1000, SYNCHRONOUS, NO_MONITOR, UPDATE );
        fs.mkdir( dir );
        life.start();
    }

    @After
    public void after() throws IOException
    {
        life.shutdown();
        pageCache.close();
        fs.shutdown();
    }
}
