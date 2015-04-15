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

import java.util.Collection;

import org.neo4j.helpers.Pair;

/**
 * Represents a node from an input source, for example a .csv file.
 */
public class InputNode extends InputEntity
{
    private Group group;
    private Object id;
    private final GrowableArray<String> labels = new GrowableArray<>( String.class, 5 );
    private Long labelField;

    /**
     * Initializes everything except {@link #properties()} and {@link #labels()} which are designed to be
     * accessed and modified externally.
     *
     * @param labelField is a hack to bypass String[] labels, consumers should check that field first.
     */
    public InputNode initialize( String sourceDescription, long lineNumber, long position,
            Group group, Object id, Long firstPropertyId, Long labelField )
    {
        super.initialize( sourceDescription, lineNumber, position, firstPropertyId );
        this.group = group;
        this.id = id;
        this.labelField = labelField;
        return this;
    }

    public Group group()
    {
        return group;
    }

    public Object id()
    {
        return id;
    }

    public GrowableArray<String> labels()
    {
        return labels;
    }

    public boolean hasLabelField()
    {
        return labelField != null;
    }

    public Long labelField()
    {
        return labelField;
    }

    @Override
    protected void toStringFields( Collection<Pair<String, ?>> fields )
    {
        super.toStringFields( fields );
        fields.add( Pair.of( "id", id ) );
        fields.add( Pair.of( "group", group ) );
        if ( hasLabelField() )
        {
            fields.add( Pair.of( "labelField", labelField ) );
        }
        else if ( labels.length() > 0 )
        {
            fields.add( Pair.of( "labels", labels ) );
        }
    }
}
