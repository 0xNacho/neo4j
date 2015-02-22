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
package org.neo4j.helpers.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.neo4j.kernel.impl.util.collection.Iterators;

/**
 * Wraps a {@link Collection}, making it look (and function) like a collection
 * holding another type of items. The wrapper delegates to its underlying
 * collection instead of keeping track of the items itself.
 *
 * @param <T> the type of items
 * @param <U> the type of items of the underlying/wrapped collection
 */
public abstract class CollectionWrapper<T, U> implements Collection<T>
{
	private final Collection<U> collection;

	public CollectionWrapper( Collection<U> underlyingCollection )
	{
		this.collection = underlyingCollection;
	}

	protected abstract U objectToUnderlyingObject( T object );

	protected abstract T underlyingObjectToObject( U object );

	@Override
    public boolean add( T o )
	{
		return collection.add( objectToUnderlyingObject( o ) );
	}

	@Override
    public void clear()
	{
		collection.clear();
	}

	@Override
    public boolean contains( Object o )
	{
		return collection.contains( objectToUnderlyingObject( ( T ) o ) );
	}

	@Override
    public boolean isEmpty()
	{
		return collection.isEmpty();
	}

	@Override
    public Iterator<T> iterator()
	{
	    return new Iterators.Map<U,T>( collection.iterator() )
        {
            @Override
            protected T map( U item )
            {
                return CollectionWrapper.this.underlyingObjectToObject( item );
            }
        };
	}

	@Override
    public boolean remove( Object o )
	{
		return collection.remove( objectToUnderlyingObject( ( T ) o ) );
	}

	@Override
    public int size()
	{
		return collection.size();
	}

	protected Collection<U> convertCollection( Collection c )
	{
		Collection<U> converted = new HashSet<U>();
		for ( Object item : c )
		{
			converted.add( objectToUnderlyingObject( ( T ) item ) );
		}
		return converted;
	}

	@Override
    public boolean retainAll( Collection c )
	{
		return collection.retainAll( convertCollection( c ) );
	}

	@Override
    public boolean addAll( Collection c )
	{
		return collection.addAll( convertCollection( c ) );
	}

	@Override
    public boolean removeAll( Collection c )
	{
		return collection.removeAll( convertCollection( c ) );
	}

	@Override
    public boolean containsAll( Collection c )
	{
		return collection.containsAll( convertCollection( c ) );
	}

	@Override
    public Object[] toArray()
	{
		Object[] array = collection.toArray();
		Object[] result = new Object[ array.length ];
		for ( int i = 0; i < array.length; i++ )
		{
			result[ i ] = underlyingObjectToObject( ( U ) array[ i ] );
		}
		return result;
	}

	@Override
    public <R> R[] toArray( R[] a )
	{
		Object[] array = collection.toArray();
		ArrayList<R> result = new ArrayList<R>();
		for ( int i = 0; i < array.length; i++ )
		{
			result.add( ( R ) underlyingObjectToObject( ( U ) array[ i ] ) );
		}
		return result.toArray( a );
	}
}
