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
package org.neo4j.unsafe.impl.batchimport.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.neo4j.csv.reader.SourceTraceability;
import org.neo4j.helpers.Pair;

import static java.lang.String.format;

/**
 * Represents an entity from an input source, for example a .csv file.
 */
public abstract class InputEntity implements SourceTraceability
{
    public static final Object[] NO_PROPERTIES = new Object[0];
    public static final String[] NO_LABELS = new String[0];

    private final GrowableArray<Object> properties = new GrowableArray<>( Object.class, 10 );
    private Long firstPropertyId;
    private String sourceDescription;
    private long lineNumber;
    private long position;

    /**
     * Initializes everything except {@link #properties()} which is designed to be accessed and modified externally.
     *
     * @param properties alternating key/value (two items per property)
     * @param numberOfProperties number of key/value pairs. Actual items use in properties array is 2*numberOfProperties
     */
    protected void initialize( String sourceDescription, long sourceLineNumber, long sourcePosition,
            Long firstPropertyId )
    {
        this.sourceDescription = sourceDescription;
        this.lineNumber = sourceLineNumber;
        this.position = sourcePosition;
        this.firstPropertyId = firstPropertyId;
    }

    public GrowableArray<Object> properties()
    {
        return properties;
    }

    /**
     * Adds properties to existing properties in this entity. Properties that exist
     * @param keyValuePairs
     */
    public void updateProperties( UpdateBehaviour behaviour, Object... keyValuePairs )
    {
        assert keyValuePairs.length % 2 == 0 : Arrays.toString( keyValuePairs );

        // There were no properties before, just set these and be done
        if ( properties.length() == 0 )
        {
            properties.addAll( keyValuePairs );
            return;
        }

        // We need to look at existing properties
        // First make room for any new properties
        for ( int i = 0; i < keyValuePairs.length; i++ )
        {
            Object key = keyValuePairs[i++];
            Object value = keyValuePairs[i];
            updateProperty( key, value, behaviour );
        }
    }

    private void updateProperty( Object key, Object value, UpdateBehaviour behaviour )
    {
        int length = properties.length();
        for ( int i = 0; i < length; i++ )
        {
            Object existingKey = properties.get( i++ );
            if ( existingKey.equals( key ) )
            {   // Update
                properties.set( i, behaviour.merge( properties.get( i ), value ) );
                return;
            }
        }

        // Add
        properties.add( key );
        properties.add( value );
    }

    public boolean hasFirstPropertyId()
    {
        return firstPropertyId != null;
    }

    public long firstPropertyId()
    {
        return firstPropertyId;
    }

    @Override
    public String sourceDescription()
    {
        return sourceDescription;
    }

    @Override
    public long lineNumber()
    {
        return lineNumber;
    }

    @Override
    public long position()
    {
        return position;
    }

    @Override
    public String toString()
    {
        Collection<Pair<String,?>> fields = new ArrayList<>();
        toStringFields( fields );

        StringBuilder builder = new StringBuilder( "%s:" );
        Object[] arguments = new Object[fields.size()+1];
        int cursor = 0;
        arguments[cursor++] = getClass().getSimpleName();
        for ( Pair<String, ?> item : fields )
        {
            builder.append( "%n   %s" );
            arguments[cursor++] = item.first() + ": " + item.other();
        }

        return format( builder.append( "%n" ).toString(), arguments );
    }

    protected void toStringFields( Collection<Pair<String, ?>> fields )
    {
        fields.add( Pair.of( "source", sourceDescription + ":" + lineNumber ) );
        if ( hasFirstPropertyId() )
        {
            fields.add( Pair.of( "nextProp", firstPropertyId ) );
        }
        else if ( properties.length() > 0 )
        {
            fields.add( Pair.of( "properties", properties ) );
        }
    }
}
