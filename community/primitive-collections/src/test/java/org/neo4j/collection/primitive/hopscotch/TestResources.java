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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.collection.primitive.Primitive;
import org.neo4j.collection.primitive.PrimitiveLongCollections;
import org.neo4j.collection.primitive.PrimitiveLongCollections.PrimitiveLongBaseIterator;
import org.neo4j.collection.primitive.PrimitiveLongIntMap;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.test.randomized.Printable;
import org.neo4j.test.randomized.TestResource;

import static org.neo4j.test.randomized.Printables.line;

public class TestResources
{
    public static abstract class PrimitiveCollectionTestResource<VALUE> implements TestResource
    {
        abstract int size();

        abstract PrimitiveLongIterator keyIterator();

        abstract boolean containsKey( long key );
    }

    public static class PrimitiveLongHashSetResource extends PrimitiveCollectionTestResource<Void>
    {
        final Set<Long> normalSet = new HashSet<>();
        final PrimitiveLongSet set = Primitive.longSet();

        @Override
        public void close()
        {
            set.close();
        }

        @Override
        public Printable given()
        {
            return line( PrimitiveLongSet.class.getSimpleName() + " set = " +
                    Primitive.class.getSimpleName() + ".longSet();" );
        }

        @Override
        int size()
        {
            return normalSet.size();
        }

        @Override
        PrimitiveLongIterator keyIterator()
        {
            return fromBoxedLong( normalSet.iterator() );
        }

        @Override
        boolean containsKey( long key )
        {
            return normalSet.contains( key );
        }
    }

    public static class PrimitiveLongIntHashMapResource extends PrimitiveCollectionTestResource<Integer>
    {
        final Map<Long,Integer> normalMap = new HashMap<>();
        final PrimitiveLongIntMap map = Primitive.longIntMap();

        @Override
        public void close()
        {
            map.close();
        }

        @Override
        public Printable given()
        {
            return line( PrimitiveLongIntMap.class.getSimpleName() + " map = " +
                    Primitive.class.getSimpleName() + ".longIntMap();" );
        }

        @Override
        int size()
        {
            return normalMap.size();
        }

        @Override
        PrimitiveLongIterator keyIterator()
        {
            return fromBoxedLong( normalMap.keySet().iterator() );
        }

        @Override
        boolean containsKey( long key )
        {
            return normalMap.containsKey( key );
        }
    }

    private static PrimitiveLongBaseIterator fromBoxedLong( final Iterator<Long> source )
    {
        return new PrimitiveLongCollections.PrimitiveLongBaseIterator()
        {
            @Override
            protected boolean fetchNext()
            {
                return source.hasNext() ? next( source.next().longValue() ) : false;
            }
        };
    }
}
