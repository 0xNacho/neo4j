package org.neo4j.collection.primitive.hopscotch;

import org.neo4j.array.primitive.IntArray;
import org.neo4j.array.primitive.NumberArrayFactory;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.collection.primitive.PrimitiveLongVisitor;

public class CloseToTheMetalLongSet extends CloseToTheMetalHopScotchHashingAlgorithm<Void> implements PrimitiveLongSet
{
    public CloseToTheMetalLongSet( NumberArrayFactory factory )
    {
        super( factory, 3, null );
    }

    @Override
    public <E extends Exception> void visitKeys( PrimitiveLongVisitor<E> visitor ) throws E
    {
    }

    @Override
    protected long getKey( IntArray array, int absIndex )
    {
        long low = array.get( absIndex )&0xFFFFFFFFL;
        long high = array.get( absIndex+1 )&0xFFFFFFFFL;
        return (high << 32) | low;
    }

    @Override
    protected void putKey( IntArray array, int absIndex, long key )
    {
        array.set( absIndex, (int)key );
        array.set( absIndex+1, (int)((key&0xFFFFFFFF00000000L) >>> 32) );
    }

    @Override
    public PrimitiveLongIterator iterator()
    {
        return null;
    }

    @Override
    public boolean accept( long value )
    {
        return contains( value );
    }

    @Override
    public boolean addAll( PrimitiveLongIterator values )
    {
        return false;
    }

    @Override
    public boolean remove( long value )
    {
        return false;
    }
}
