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
package org.neo4j.test.randomized;

import java.io.PrintStream;
import java.util.List;

import org.neo4j.function.Factory;

public class TestCaseWriter<T extends TestResource,F>
{
    private final String testName;
    private final List<Action<T,F>> actions;
    private final Action<T,F> failingAction;
    private final Factory<T> targetFactory;

    TestCaseWriter( String testName, Factory<T> targetFactory,
            List<Action<T,F>> actions, Action<T,F> failingAction )
    {
        this.testName = testName;
        this.targetFactory = targetFactory;
        this.actions = actions;
        this.failingAction = failingAction;
    }

    public void print( PrintStream out )
    {
        T target = targetFactory.newInstance();
        LinePrinter baseLinePrinter = new PrintStreamLinePrinter( out, 0 );
        baseLinePrinter.println( "@Test" );
        baseLinePrinter.println( "public void " + testName + "() throws Exception" );
        baseLinePrinter.println( "{" );

        LinePrinter codePrinter = baseLinePrinter.indent();
        codePrinter.println( "// GIVEN" );
        target.given().print( codePrinter );
        for ( Action<T,F> action : actions )
        {
            action.printAsCode( target, codePrinter, false );
            action.apply( target );
        }

        codePrinter.println( "" );
        codePrinter.println( "// WHEN/THEN" );
        failingAction.printAsCode( target, codePrinter, true );
        baseLinePrinter.println( "}" );
        out.flush();
    }
}
