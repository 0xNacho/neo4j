package org.neo4j.collection.primitive.hopscotch;

import org.neo4j.array.primitive.IntArray;
import org.neo4j.array.primitive.NumberArrayFactory;
import org.neo4j.collection.primitive.PrimitiveLongIntMap;
import org.neo4j.collection.primitive.PrimitiveLongIntVisitor;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.collection.primitive.PrimitiveLongVisitor;

public class CloseToTheMetalLongIntMap extends CloseToTheMetalHopScotchHashingAlgorithm<int[]> implements PrimitiveLongIntMap
{
    private static final int[] NULL = new int[] {-1};
    private final int[] transport = new int[1];

    public CloseToTheMetalLongIntMap( NumberArrayFactory factory )
    {
        super( factory, 4, NULL );
    }

    @Override
    public <E extends Exception> void visitKeys( PrimitiveLongVisitor<E> visitor ) throws E
    {
    }

    @Override
    public PrimitiveLongIterator iterator()
    {
        return null;
    }

    @Override
    public int put( long key, int value )
    {
        transport[0] = value;
        return _put( key,transport )[0];
    }

    @Override
    protected int[] getValue( IntArray array, int absIndex )
    {
        transport[0] = array.get( absIndex+2 );
        return transport;
    }

    @Override
    public boolean containsKey( long key )
    {
        return contains( key );
    }

    @Override
    public int get( long key )
    {
        return _get( key )[0];
    }

    @Override
    public int remove( long key )
    {
        return 0;
    }

    @Override
    public <E extends Exception> void visitEntries( PrimitiveLongIntVisitor<E> visitor ) throws E
    {
    }
}
