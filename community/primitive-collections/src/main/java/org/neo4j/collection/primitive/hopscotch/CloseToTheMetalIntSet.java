package org.neo4j.collection.primitive.hopscotch;

import org.neo4j.array.primitive.NumberArrayFactory;
import org.neo4j.collection.primitive.PrimitiveIntIterator;
import org.neo4j.collection.primitive.PrimitiveIntSet;
import org.neo4j.collection.primitive.PrimitiveIntVisitor;

public class CloseToTheMetalIntSet extends CloseToTheMetalHopScotchHashingAlgorithm<Void> implements PrimitiveIntSet
{
    public CloseToTheMetalIntSet( NumberArrayFactory factory )
    {
        super( factory, 2, null );
    }

    @Override
    public <E extends Exception> void visitKeys( PrimitiveIntVisitor<E> visitor ) throws E
    {
    }

    @Override
    public PrimitiveIntIterator iterator()
    {
        return null;
    }

    @Override
    public boolean accept( int value )
    {
        return false;
    }

    @Override
    public boolean add( int value )
    {
        return add( (long) value );
    }

    @Override
    public boolean addAll( PrimitiveIntIterator values )
    {
        return false;
    }

    @Override
    public boolean contains( int value )
    {
        return contains( (long) value );
    }

    @Override
    public boolean remove( int value )
    {
        return false;
    }
}
