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
package org.neo4j.kernel.impl.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.neo4j.function.Function;
import org.neo4j.function.Function2;
import org.neo4j.helpers.CloneableInPublic;
import org.neo4j.helpers.Predicate;

/**
 * {@link Iterable} wrappers around all available {@link Iterator iterators} found in {@link Iterators}.
 *
 * @author Mattias Persson
 * @see Iterators
 */
public abstract class Iterables
{
    private Iterables()
    {   // Singleton
    }

    // Array
    @SafeVarargs
    public static <T> Iterable<T> iterable( final T... items )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.iterator( items );
            }
        };
    }

    @SafeVarargs
    public static <T> Iterable<T> reversed( final T... items )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.reversed( items );
            }
        };
    }

    // Caching
    public static interface ListIterable<T> extends Iterable<T>
    {
        @Override
        ListIterator<T> iterator();
    }

    public static <T> ListIterable<T> cache( final Iterable<T> source )
    {
        return new ListIterable<T>()
        {
            private final List<T> visited = new ArrayList<>();

            @Override
            public ListIterator<T> iterator()
            {
                return new Iterators.Cache<>( source.iterator(), visited );
            }
        };
    }

    // Catching
    public static <T> Iterable<T> catching( Iterable<T> source, final Predicate<Throwable> catchAndIgnoreException )
    {
        return new Catch<T>( source )
        {
            @Override
            protected boolean exceptionOk( Throwable t )
            {
                return catchAndIgnoreException.accept( t );
            }
        };
    }

    public static abstract class Catch<T> implements Iterable<T>
    {
        private final Iterable<T> source;

        public Catch( Iterable<T> source )
        {
            this.source = source;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new Iterators.Catch<T>( source.iterator() )
            {
                @Override
                protected boolean exceptionOk( Throwable t )
                {
                    return Catch.this.exceptionOk( t );
                }
            };
        }

        protected abstract boolean exceptionOk( Throwable t );
    }

    // Concat
    public static <T> Iterable<T> concat( final Iterable<? extends Iterable<T>> iterables )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return new Iterators.Concat<T>()
                {
                    private final Iterator<? extends Iterable<T>> iterator = iterables.iterator();

                    @Override
                    protected Iterator<T> nextIterator()
                    {
                        return iterator.hasNext() ? iterator.next().iterator() : null;
                    }
                };
            }
        };
    }

    @SafeVarargs
    public static <T> Iterable<T> concat( final Iterable<T>... iterables )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return new Iterators.Concat<T>()
                {
                    private int cursor;

                    @Override
                    protected Iterator<T> nextIterator()
                    {
                        return cursor < iterables.length ? iterables[cursor++].iterator() : null;
                    }
                };
            }
        };
    }

    public static <T> Iterable<T> prepend( final T item, final Iterable<T> source )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.prepend( item, source.iterator() );
            }
        };
    }

    public static <T> Iterable<T> append( final Iterable<T> source, final T item )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.append( source.iterator(), item );
            }
        };
    }

    // Interleaving
    public static <T> Iterable<T> interleave( final Iterable<Iterable<T>> iterables )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return new Iterators.Interleave<>(
                        new Map<Iterable<T>,Iterator<T>>( iterables )
                {
                    @Override
                    protected Iterator<T> map( Iterable<T> item )
                    {
                        return item.iterator();
                    }
                } );
            }
        };
    }

    // Filtering
    public static <T> Iterable<T> filter( Iterable<T> source, final Predicate<T> filter )
    {
        return new Filter<T>( source )
        {
            @Override
            public boolean accept( T item )
            {
                return filter.accept( item );
            }
        };
    }

    public static <T> Iterable<T> dedup( final Iterable<T> source )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.dedup( source.iterator() );
            }
        };
    }

    public static <T> Iterable<T> skip( final Iterable<T> source, final int skipTheFirstNItems )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.skip( source.iterator(), skipTheFirstNItems );
            }
        };
    }

    public static abstract class Filter<T> implements Iterable<T>, Predicate<T>
    {
        private final Iterable<T> source;

        public Filter( Iterable<T> source )
        {
            this.source = source;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new Iterators.Filter<T>( source.iterator() )
            {
                @Override
                public boolean accept( T item )
                {
                    return Filter.this.accept( item );
                }
            };
        }

        @Override
        public abstract boolean accept( T item );
    }

    // Limiting
    public static <T> Iterable<T> limit( final Iterable<T> source, final int maxItems )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.limit( source.iterator(), maxItems );
            }
        };
    }

    // Mapping
    public static abstract class Map<FROM,TO> implements Iterable<TO>
    {
        private final Iterable<FROM> source;

        public Map( Iterable<FROM> source )
        {
            this.source = source;
        }

        @Override
        public Iterator<TO> iterator()
        {
            return new Iterators.Map<FROM,TO>( source.iterator() )
            {
                @Override
                protected TO map( FROM item )
                {
                    return Map.this.map( item );
                }
            };
        }

        protected abstract TO map( FROM item );
    }

    public static <FROM,TO> Iterable<TO> map( final Iterable<FROM> source, final Function<FROM,TO> function )
    {
        return new Iterable<TO>()
        {
            @Override
            public Iterator<TO> iterator()
            {
                return Iterators.map( source.iterator(), function );
            }
        };
    }

    // Nested
    public static <FROM,TO> Iterable<TO> nest( Iterable<FROM> source, final Function<FROM,Iterator<TO>> nester )
    {
        return new Nest<FROM, TO>( source )
        {
            @Override
            protected Iterator<TO> nested( FROM item )
            {
                return nester.apply( item );
            }
        };
    }

    public static <FROM,TO> Iterable<TO> nestIterables( Iterable<FROM> source,
            final Function<FROM,Iterable<TO>> nester )
    {
        return new Nest<FROM, TO>( source )
        {
            @Override
            protected Iterator<TO> nested( FROM item )
            {
                return nester.apply( item ).iterator();
            }
        };
    }

    public static abstract class Nest<FROM,TO> implements Iterable<TO>
    {
        private final Iterable<FROM> source;

        public Nest( Iterable<FROM> source )
        {
            this.source = source;
        }

        @Override
        public Iterator<TO> iterator()
        {
            return new Iterators.Nest<FROM,TO>( source.iterator() )
            {
                @Override
                protected Iterator<TO> nested( FROM item )
                {
                    return Nest.this.nested( item );
                }
            };
        }

        protected abstract Iterator<TO> nested( FROM item );
    }

    // Casting
    public static <FROM,TO> Iterable<TO> cast( final Iterable<FROM> source, final Class<TO> toClass )
    {
        return new Iterable<TO>()
        {
            @Override
            public Iterator<TO> iterator()
            {
                return Iterators.cast( source.iterator(), toClass );
            }
        };
    }

    // Range
    public static Iterable<Integer> intRange( int end )
    {
        return intRange( 0, end );
    }

    public static Iterable<Integer> intRange( int start, int end )
    {
        return intRange( start, end, 1 );
    }

    public static Iterable<Integer> intRange( int start, int end, int stride )
    {
        return new Range<>( start, end, stride,
                Iterators.INTEGER_STRIDER, Iterators.INTEGER_COMPARATOR );
    }

    public static class Range<T,S> implements Iterable<T>
    {
        private final T start;
        private final T end;
        private final S stride;
        private final Function2<T, S, T> strider;
        private final Comparator<T> comparator;

        public Range( T start, T end, S stride, Function2<T, S, T> strider, Comparator<T> comparator )
        {
            this.start = start;
            this.end = end;
            this.stride = stride;
            this.strider = strider;
            this.comparator = comparator;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new Iterators.Range<>( start, end, stride, strider, comparator );
        }
    }

    // Singleton
    public static <T> Iterable<T> singleton( final T item )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.singleton( item );
            }
        };
    }

    // Cloning
    public static <T extends CloneableInPublic> Iterable<T> cloning( final Iterable<T> items,
            final Class<T> itemClass )
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return Iterators.cloning( items.iterator(), itemClass );
            }
        };
    }

    // Reversed
    public static <T> Iterable<T> reversed( Iterable<T> source )
    {
        List<T> allItems = new ArrayList<>();
        for ( Iterator<T> iterator = source.iterator(); iterator.hasNext(); )
        {
            allItems.add( iterator.next() );
        }
        Collections.reverse( allItems );
        return allItems;
    }

    // === Operations ===
    public static <T> T first( Iterable<T> iterable )
    {
        return Iterators.first( iterable.iterator() );
    }

    public static <T> T first( Iterable<T> iterable, T defaultItem )
    {
        return Iterators.first( iterable.iterator(), defaultItem );
    }

    public static <T> T last( Iterable<T> iterable )
    {
        return Iterators.last( iterable.iterator() );
    }

    public static <T> T last( Iterable<T> iterable, T defaultItem )
    {
        return Iterators.last( iterable.iterator(), defaultItem );
    }

    public static <T> T single( Iterable<T> iterable )
    {
        return Iterators.single( iterable.iterator() );
    }

    public static <T> T single( Iterable<T> iterable, T defaultItem )
    {
        return Iterators.single( iterable.iterator(), defaultItem );
    }

    public static <T> T itemAt( Iterable<T> iterable, int index )
    {
        return Iterators.itemAt( iterable.iterator(), index );
    }

    public static <T> T itemAt( Iterable<T> iterable, int index, T defaultItem )
    {
        return Iterators.itemAt( iterable.iterator(), index, defaultItem );
    }

    public static <T> int indexOf( T item, Iterable<T> iterable )
    {
        return Iterators.indexOf( item, iterable.iterator() );
    }

    public static boolean equals( Iterable<?> first, Iterable<?> other )
    {
        return Iterators.equals( first.iterator(), other.iterator() );
    }

    public static <C extends Collection<T>,T> C addToCollection( Iterable<T> iterable, C collection,
            Function2<T, C, Void> adder )
    {
        return Iterators.addToCollection( iterable.iterator(), collection, adder );
    }

    public static <C extends Collection<T>,T> C addToCollection( Iterable<T> iterable, C collection )
    {
        return Iterators.addToCollection( iterable.iterator(), collection );
    }

    public static <C extends Collection<T>,T> C addToCollectionAssertChanged( Iterable<T> iterable, C collection )
    {
        return Iterators.addToCollectionAssertChanged( iterable.iterator(), collection );
    }

    public static <T> List<T> asList( Iterable<T> items )
    {
        return Iterators.asList( items.iterator() );
    }

    public static <T> Set<T> asSet( Iterable<T> items )
    {
        return Iterators.asSet( items.iterator() );
    }

    public static <T> Set<T> asSetAllowDuplicates( Iterable<T> items )
    {
        return Iterators.asSetAllowDuplicates( items.iterator() );
    }

    public static int count( Iterable<?> iterable )
    {
        return Iterators.count( iterable.iterator() );
    }

    public static <T> T[] asArray( Iterable<T> items, Class<T> itemClass )
    {
        return Iterators.asArray( items.iterator(), itemClass );
    }
}
