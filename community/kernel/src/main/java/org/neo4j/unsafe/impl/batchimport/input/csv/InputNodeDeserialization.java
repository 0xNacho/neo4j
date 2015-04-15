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
package org.neo4j.unsafe.impl.batchimport.input.csv;

import org.neo4j.csv.reader.SourceTraceability;
import org.neo4j.unsafe.impl.batchimport.input.Group;
import org.neo4j.unsafe.impl.batchimport.input.Groups;
import org.neo4j.unsafe.impl.batchimport.input.GrowableArray;
import org.neo4j.unsafe.impl.batchimport.input.InputNode;
import org.neo4j.unsafe.impl.batchimport.input.csv.Header.Entry;

/**
 * Builds {@link InputNode} from CSV data.
 */
public class InputNodeDeserialization extends InputEntityDeserialization<InputNode>
{
    private final Header header;
    private final Groups groups;

    private final boolean idsAreExternal;
    private Group group;
    private Object id;
    private GrowableArray<String> labels;

    public InputNodeDeserialization( SourceTraceability source, Header header, Groups groups, boolean idsAreExternal )
    {
        super( source );
        this.header = header;
        this.groups = groups;
        this.idsAreExternal = idsAreExternal;
    }

    @Override
    public void initialize()
    {
        // ID header entry is optional
        Entry idEntry = header.entry( Type.ID );
        this.group = groups.getOrCreate( idEntry != null ? idEntry.groupName() : null );
    }

    @Override
    public void handle( Entry entry, Object value )
    {
        switch ( entry.type() )
        {
        case ID:
            if ( entry.name() != null && idsAreExternal )
            {
                addProperty( entry.name(), value );
            }
            id = value;
            break;
        case LABEL:
            addLabels( value );
            break;
        default:
            super.handle( entry, value );
            break;
        }
    }

    @Override
    public InputNode materialize()
    {
        return entity.initialize(
                source.sourceDescription(), source.lineNumber(), source.position(),
                group, id, null, null );
    }

    @Override
    public void prepare( InputNode node )
    {
        super.prepare( node );
        labels = node.labels();
        labels.clear();
    }

    private void addLabels( Object value )
    {
        if ( value instanceof String )
        {
            labels.add( (String) value );
        }
        else if ( value instanceof String[] )
        {
            labels.addAll( (String[]) value );
        }
        else
        {
            throw new IllegalArgumentException( "Unexpected label value type " +
                    value.getClass() + ": " + value );
        }
    }
}
