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

import org.neo4j.array.primitive.IntArray;
import org.neo4j.array.primitive.NumberArrayFactory;

import static java.lang.Integer.highestOneBit;
import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Long.numberOfTrailingZeros;

/**
 * <p>
 * An implementation of the hop-scotch algorithm, see http://en.wikipedia.org/wiki/Hopscotch_hashing.
 * It's a static set of methods that implements the essence of the algorithm, where storing and retrieving data is
 * abstracted into the {@link Table} interface. Also things like {@link Monitor monitoring} and choice of
 * {@link HashFunction} gets passed in.
 * </p>
 *
 * <p>
 * About hop scotching and hop bits: Each index in the table has hop bits which is a list of which nearby
 * indexes contain entries with a key hashing to the same index as itself - its neighborhood.
 * A neighbor can at most be {@code H} indexes away. This is the collision resolution method of choice
 * for the hop scotch algorithm. Getting and putting entries at an index first checks the index at hand.
 * If occupied by another entry its neighbors, dictated by the hop bits, are checked.
 *
 * When putting an entry and the index and all its neighbors are occupied, the hop scotching begins, where a free
 * index further away is picked and iteratively moved closer and closer until it's within the neighborhood
 * of the intended index. The entry is then placed at that newly "freed" index.
 *
 * Removing an entry will put some effort into do reverse hop scotching as well, i.e. moving a neighbor into
 * the newly removed index and iteratively also move neighbors of the moved neighbor, and so forth.
 *
 * This behavior has the benefit of keeping entries hashing to the same index very close together,
 * and so will take more advantage of CPU caches and pre-fetching in general, especially for lookups.
 * </p>
 *
 * <p>
 * Why are these methods (like {@link #put(Table, Monitor, HashFunction, long, Object, ResizeMonitor)},
 * {@link #get(Table, Monitor, HashFunction, long)} a.s.o. static? To reduce garbage and also reduce overhead of each
 * set or map object making use of hop-scotch hashing where they won't need to have a reference to an algorithm
 * object, merely use its static methods. Also, all essential state is managed by {@link Table}.
 * </p>
 */
public abstract class HopScotchHashingCollection<VALUE>
{
    /**
     * Default number of hop bits per index, i.e. size of neighborhood.
     */
    public static final int DEFAULT_H = 32;

    private final HashFunction hashFunction;
    protected IntArray array;
    private int tableMask;
    protected final long nullKey = -1;
    private final NumberArrayFactory factory;
    protected final int itemsPerEntry;
    private int size;
    private final VALUE nullValue;
    private final Monitor monitor;

    public HopScotchHashingCollection( HashFunction hashFunction, NumberArrayFactory factory,
            int itemsPerEntry, VALUE nullValue, int initialCapacity, Monitor monitor )
    {
        this.hashFunction = hashFunction;
        this.factory = factory;
        this.itemsPerEntry = itemsPerEntry;
        this.nullValue = nullValue;
        this.monitor = monitor;
        newArray( initialCapacity );
    }

    public boolean contains( long key )
    {
        int index = indexOf( key );
        int absIndex = index( index );
        long existingKey = getKey( array, absIndex );
        if ( existingKey == key )
        {   // Bulls eye
            return true;
        }

        // Look in its neighborhood
        long hopBits = hopBits( absIndex );
        while ( hopBits > 0 )
        {
            int hopIndex = nextIndex( index, numberOfTrailingZeros( hopBits )+1 );
            if ( getKey( array, index( hopIndex ) ) == key )
            {   // There it is
                return true;
            }
            hopBits &= hopBits-1;
        }

        return false;
    }

    private long hopBits( int absIndex )
    {
        return ~(array.get( absIndex+itemsPerEntry-1 ) | 0xFFFFFFFF00000000L);
    }

    public VALUE _get( long key )
    {
        int index = indexOf( key );
        int absIndex = index( index );
        long existingKey = getKey( array, absIndex );
        if ( existingKey == key )
        {   // Bulls eye
            return getValue( array, index, absIndex );
        }

        // Look in its neighborhood
        long hopBits = hopBits( absIndex );
        while ( hopBits > 0 )
        {
            int hopIndex = nextIndex( index, numberOfTrailingZeros( hopBits )+1 );
            int absHopIndex = index( hopIndex);
            if ( getKey( array, absHopIndex ) == key )
            {   // There it is
                return getValue( array, hopIndex, absHopIndex );
            }
            hopBits &= hopBits-1;
        }

        return nullValue;
    }

    public boolean add( long key )
    {
        int index = indexOf( key );
        int absIndex = index( index );
        long keyAtIndex = getKey( array, absIndex );
        if ( keyAtIndex == nullKey )
        {   // this index is free, just place it there
            putKey( array, absIndex, key );
            size++;
//            assert monitor.placedAtFreeAndIntendedIndex( key, index );
            return true;
        }
        else if ( keyAtIndex == key )
        {   // this index is occupied with the same key
            return false;
        }
        else
        {   // look at the neighbors of this entry to see if any is the requested key
            long hopBits = hopBits( absIndex );
            while ( hopBits > 0 )
            {
                int hopIndex = nextIndex( index, numberOfTrailingZeros( hopBits )+1 );
                if ( getKey( array, index( hopIndex ) ) == key )
                {   // this index is occupied with the same key
                    return false;
                }
                hopBits &= hopBits-1;
            }
        }

        // this key does not exist in this set. put it there using hop-scotching
        if ( hopScotchPut( key, index, null ) )
        {   // we managed to wiggle our way to a free spot and put it there
            size++;
            return true;
        }

        // we couldn't add this value, even in the H-1 neighborhood, so grow table...
        growTable();

        // ...and try again
        return add( key );
    }

    public VALUE _put( long key, VALUE value )
    {
        int index = indexOf( key );
        int absIndex = index( index );
        long keyAtIndex = getKey( array, absIndex );
        if ( keyAtIndex == nullKey )
        {   // this index is free, just place it there
            putKey( array, absIndex, key );
            putValue( array, index, absIndex, value );
            size++;
//            assert monitor.placedAtFreeAndIntendedIndex( key, index );
            return nullValue;
        }
        else if ( keyAtIndex == key )
        {   // this index is occupied with the same key
            return putValue( array, index, absIndex, value );
        }
        else
        {   // look at the neighbors of this entry to see if any is the requested key
            long hopBits = hopBits( absIndex );
            while ( hopBits > 0 )
            {
                int hopIndex = nextIndex( index, numberOfTrailingZeros( hopBits )+1 );
                int absHopIndex = index( hopIndex );
                if ( getKey( array, absHopIndex ) == key )
                {   // this index is occupied with the same key
                    return putValue( array, hopIndex, absHopIndex, value );
                }
                hopBits &= hopBits-1;
            }
        }

        // this key does not exist in this set. put it there using hop-scotching
        if ( hopScotchPut( key, index, value ) )
        {   // we managed to wiggle our way to a free spot and put it there
            size++;
            return nullValue;
        }

        // we couldn't add this value, even in the H-1 neighborhood, so grow table...
        value = cloneValue( value );
        growTable();

        // ...and try again
        return _put( key, value );
    }

    private boolean hopScotchPut( long key, int index, VALUE value )
    {
        int freeIndex = nextIndex( index, 1 );
        int totalHd = 0; // h delta, i.e. distance from first neighbor to current tentative index, the first neighbor has hd=0
        boolean foundFreeSpot = false;

        // linear probe for finding a free slot in ASC index direction
        while ( freeIndex != index ) // one round is enough, albeit far, but at the same time very unlikely
        {
            if ( getKey( array, index( freeIndex ) ) == nullKey )
            {   // free slot found
                foundFreeSpot = true;
                break;
            }

            // move on to the next index in the search for a free slot
            freeIndex = nextIndex( freeIndex, 1 );
            totalHd++;
        }

        if ( !foundFreeSpot )
        {
            return false;
        }

        while ( totalHd >= DEFAULT_H )
        {   // grab a closer index and see which of its neighbors is OK to move further away,
            // so that there will be a free space to place the new value. I.e. move the free space closer
            // and some close neighbors a bit further away (although never outside its neighborhood)
            int neighborIndex = nextIndex( freeIndex, -(DEFAULT_H-1) ); // hopscotch hashing says to try h-1 entries closer

            boolean swapped = false;
            for ( int d = 0; d < (DEFAULT_H >> 1) && !swapped; d++ )
            {   // examine hop information (i.e. is there's someone in the neighborhood here to swap with 'hopIndex'?)
                long neighborHopBits = hopBits( index( neighborIndex ) );
//                final long oldNeighborHopBits = neighborHopBits;
                while ( neighborHopBits > 0 && !swapped )
                {
                    int hd = numberOfTrailingZeros( neighborHopBits );
                    if ( hd+d >= DEFAULT_H-1 )
                    {   // that would be too far
                        break;
                    }
                    neighborHopBits &= neighborHopBits-1;
                    int candidateIndex = nextIndex( neighborIndex, hd+1 );

                    // OK, here's a neighbor, let's examine it's neighbors (candidates to move)
                    //  - move the candidate entry (incl. updating its hop bits) to the free index
                    int distance = (freeIndex-candidateIndex)&tableMask;
                    array.swap( index( candidateIndex ), index( freeIndex ), itemsPerEntry-1 );
                    //  - update the neighbor entry with the move of the candidate entry
                    array.genericXor( index( neighborIndex )+itemsPerEntry-1, hopBit( hd ) | hopBit( hd+distance ) );
//                    assert monitor.pushedToFreeIndex( index, oldNeighborHopBits, hopBits( index( neighborIndex ) ),
//                            neighborIndex, getKey( array, index( freeIndex ) ), candidateIndex, freeIndex );
                    freeIndex = candidateIndex;
                    swapped = true;
                    totalHd -= distance;
                }
                if ( !swapped )
                {
                    neighborIndex = nextIndex( neighborIndex, 1 );
                }
            }

            if ( !swapped )
            {   // we could not make any room to swap, tell that to the outside world
                return false;
            }
        }

        // OK, now we're within distance to just place it there. Do it
        int absIndex = index( freeIndex );
        putKey( array, absIndex, key );
        putValue( array, index, absIndex, value );
        // and update the hop bits of "index"
        array.genericAnd( index( index )+itemsPerEntry-1, ~hopBit( totalHd ) );
//        assert monitor.placedAtFreedIndex( index, hopBits( index( index ) ), key, freeIndex );
        return true;
    }

    private long hopBit( int hd )
    {
        return (1L << hd);
    }

    public VALUE getAndRemove( long key )
    {
        int index = indexOf( key );
        int absIndex = index( index );
        int freedIndex = -1;
        VALUE result = nullValue;
        if ( getKey( array, absIndex ) == key )
        {   // Bulls eye
            freedIndex = index;
            result = getValue( array, index, absIndex );
            removeEntry( array, absIndex );
            size--;
        }
        else
        {   // Look in its neighborhood
            long hopBits = hopBits( absIndex );
            while ( hopBits > 0 )
            {
                int hd = numberOfTrailingZeros( hopBits );
                int hopIndex = nextIndex( index, hd+1 );
                int absHopIndex = index( hopIndex );
                if ( getKey( array, absHopIndex ) == key )
                {   // there it is
                    freedIndex = hopIndex;
                    result = getValue( array, hopIndex, absHopIndex );
                    removeEntry( array, absHopIndex );
                    size--;
                    array.genericOr( absIndex+itemsPerEntry-1, hopBit( hd ) );
                    break;
                }
                hopBits &= hopBits-1;
            }
        }

        reverseHopScotching( index, freedIndex );
        return result;
    }

    public boolean _remove( long key )
    {
        int index = indexOf( key );
        int absIndex = index( index );
        int freedIndex = -1;
        boolean removed = false;
        if ( getKey( array, absIndex ) == key )
        {   // Bulls eye
            freedIndex = index;
            removeEntry( array, absIndex );
            removed = true;
        }
        else
        {   // Look in its neighborhood
            long hopBits = hopBits( absIndex );
            while ( hopBits > 0 )
            {
                int hd = numberOfTrailingZeros( hopBits );
                int hopIndex = nextIndex( index, hd+1 );
                int absHopIndex = index( hopIndex );
                if ( getKey( array, absHopIndex ) == key )
                {   // there it is
                    freedIndex = hopIndex;
                    removeEntry( array, absHopIndex );
                    array.genericOr( absIndex+itemsPerEntry-1, hopBit( hd ) );
                    removed = true;
                    break;
                }
                hopBits &= hopBits-1;
            }
        }

        reverseHopScotching( index, freedIndex );
        return removed;
    }

    private void reverseHopScotching( int index, int freedIndex )
    {
        // reversed hop-scotching, i.e. pull in the most distant neighbor, iteratively as long as the
        // pulled index has neighbors of its own
        while ( freedIndex != -1 )
        {
            long freedHopBits = hopBits( index( freedIndex ) );
            if ( freedHopBits > 0 )
            {   // It's got a neighbor, go ahead and move it here
                int hd = 63-numberOfLeadingZeros( freedHopBits );
                int candidateIndex = nextIndex( freedIndex, hd+1 );
                // move key/value
                array.swap( index( candidateIndex ), index( freedIndex ), itemsPerEntry-1 );
                // remove that hop bit, since that one is no longer a neighbor, it's "the one" at the index
                array.genericOr( index( freedIndex )+itemsPerEntry-1, hopBit( hd ) );
//                assert monitor.pulledToFreeIndex( index, hopBits( index( freedIndex ) ), getKey( array, freedIndex ),
//                        candidateIndex, freedIndex );
                freedIndex = candidateIndex;
            }
            else
            {
                break;
            }
        }
    }

    protected void newArray( int logicalCapacity )
    {
        array = factory.newIntArray( index( logicalCapacity ), -1 );
        tableMask = highestOneBit( logicalCapacity ) - 1;
    }

    protected int index( int logicalIndex )
    {
        return logicalIndex * itemsPerEntry;
    }

    protected int nextIndex( int index, int delta )
    {
        return (index+delta)&tableMask;
    }

    protected int indexOf( long key )
    {
        return hashFunction.hash( key ) & tableMask;
    }

    protected int capacity()
    {
        return (int) (array.length() / itemsPerEntry); // TODO safe cast
    }

    private void growTable()
    {
        assert monitor.tableGrowing( capacity(), size );

        IntArray oldArray = array;
        int oldCapacity = capacity();
        newArray( oldCapacity * 2 );
        size = 0;

        // place all entries in the new table
        for ( int i = 0, k = 0; i < oldCapacity; i++, k += itemsPerEntry )
        {
            long key = getKey( oldArray, k );
            if ( key != nullKey )
            {
                VALUE value = getValue( oldArray, i, k );
                if ( _put( key, value ) != nullValue )
                {
                    throw new IllegalStateException( "Couldn't add " + key + " at index " + i + " when growing table" );
                }
            }
        }
        oldArray.close();
        assert monitor.tableGrew( oldCapacity, capacity(), size );
    }

    // =============================================================
    // Methods to help implement boiler plate methods on sub-classes
    // =============================================================

    public void close()
    {
        array.close();
    }

    public void clear()
    {
        array.clear();
        size = 0;
    }

    public boolean isEmpty()
    {
        return size == 0;
    }

    public int size()
    {
        return size;
    }

    protected long getLong( IntArray array, int absIndex )
    {
        long low = array.get( absIndex )&0xFFFFFFFFL;
        long high = array.get( absIndex+1 )&0xFFFFFFFFL;
        return (high << 32) | low;
    }

    protected void putLong( IntArray array, int absIndex, long value )
    {
        array.set( absIndex, (int)value );
        array.set( absIndex+1, (int)((value&0xFFFFFFFF00000000L) >>> 32) );
    }

    protected final boolean typeAndSizeEqual( Object other )
    {
        if ( this.getClass() == other.getClass() )
        {
            HopScotchHashingCollection that = (HopScotchHashingCollection) other;
            if ( this.size() == that.size() )
            {
                return true;
            }
        }
        return false;
    }

    // ========================================================
    // Methods for sub-classes to implement to change behavior,
    // mostly regarding how keys/values are stored in the array
    // ========================================================

    protected VALUE getValue( IntArray array, int index, int absIndex )
    {
        return nullValue;
    }

    protected VALUE putValue( IntArray array, int index, int absIndex, VALUE value )
    {
        return nullValue;
    }

    /**
     * Special method for cloning a value. This solves a problem that primitive maps with primitive values
     * have, a problem very specific to their current implementation where there VALUE type is declared using
     * their primitive type as an array, just to fit into generics. The thing is that there's only one
     * such array with length 1 and it's reused everywhere. This clashes in the case where {@link #_put(long, Object)}
     * or {@link #add(long)} needs to grow the table. The re-adding of all values in {@link #growTable()}
     * will reuse the value array in each call, and so the original value will be lost. To solve this
     * the value is cloned before growing the table. For implementations that doesn't have primitive type as value
     * this can be left as-is, otherwise a standard implementation is to just return {@code value.clone()}.
     */
    protected VALUE cloneValue( VALUE value )
    {
        return value;
    }

    protected long getKey( IntArray array, int absIndex )
    {
        return array.get( absIndex );
    }

    protected void putKey( IntArray array, int absIndex, long key )
    {
        array.set( absIndex, (int) key );
    }

    protected void removeEntry( IntArray array, int absIndex )
    {
        array.remove( absIndex, itemsPerEntry-1 );
    }
}