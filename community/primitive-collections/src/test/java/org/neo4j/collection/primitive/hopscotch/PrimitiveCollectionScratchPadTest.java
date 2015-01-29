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

import org.junit.Test;

import org.neo4j.collection.primitive.Primitive;
import org.neo4j.collection.primitive.PrimitiveLongSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrimitiveCollectionScratchPadTest
{
    @Test
    public void shouldOnlyContainAddedValues() throws Exception
    {
        // GIVEN
        PrimitiveLongSet set = Primitive.longSet();
        set.add( 1988041139911619632L );
        set.add( 959573531184926997L );
        set.add( 959573531184926997L );
        set.remove( 7450050849469060519L );
        set.add( 959573531184926997L );
        set.add( 3202948401879942100L );
        set.add( 1147855352937986159L );
        set.add( 3202948401879942100L );
        set.add( 3355532703471451501L );
        set.add( 5942162973651286301L );
        set.add( 3202948401879942100L );
        set.add( 1988041139911619632L );
        set.add( 6201709865475932583L );
        set.add( 520661468394995850L );
        set.add( 3819146285895508205L );
        set.add( 7112379905551885387L );
        set.add( 3674378746651978263L );
        set.remove( 8549756118026414618L );
        set.add( 520661468394995850L );
        set.add( 8204245633873430898L );
        set.remove( 8204245633873430898L );
        set.add( 2182215655445442029L );
        set.remove( 4701362235274474734L );
        set.add( 1839370361461224836L );
        set.add( 959573531184926997L );
        set.add( 1048507875323218853L );
        set.add( 77466870816884524L );
        set.add( 5186237460591757866L );
        set.remove( 1883456733867520890L );
        set.remove( 7855324671742199566L );
        set.remove( 2761993061743160374L );
        set.remove( 7128429470681920620L );
        set.add( 1495750234281427863L );
        set.add( 4585072609797600156L );
        set.add( 1495750234281427863L );
        set.remove( 6201709865475932583L );
        set.add( 1048507875323218853L );
        set.add( 1495750234281427863L );
        set.add( 1839370361461224836L );
        set.remove( 6006104122030420134L );
        set.add( 5991906290569163352L );
        set.add( 1076153510890766525L );
        set.add( 1949360323094501965L );
        set.add( 8090307929195118384L );
        set.add( 4742091974939502923L );
        set.add( 520661468394995850L );
        set.add( 959573531184926997L );
        set.add( 8294661536239705144L );
        set.add( 3674378746651978263L );
        set.add( 2254033120346733791L );
        set.add( 7112379905551885387L );
        set.remove( 3355532703471451501L );
        set.add( 2123771117500926867L );
        set.remove( 392516346580324363L );
        set.add( 1495750234281427863L );
        set.add( 4141211254283008970L );
        set.add( 4141211254283008970L );
        set.add( 4742091974939502923L );
        set.remove( 3819146285895508205L );
        set.add( 3902903775736799309L );
        set.add( 3674378746651978263L );
        set.add( 9153964002874162549L );
        set.add( 1839370361461224836L );
        set.add( 5791257869260966061L );
        set.add( 3777659743519674290L );
        set.add( 1495750234281427863L );
        set.add( 6134220320635315750L );
        set.add( 6658518276093654454L );
        set.add( 1000684369116756758L );
        set.add( 6181891596102684254L );
        set.remove( 8090307929195118384L );
        set.add( 4166536820076443326L );
        set.remove( 4674292049100449934L );
        set.add( 3300422770936195076L );
        set.add( 9156111996324767202L );
        set.remove( 8156615413337843988L );
        set.remove( 1048507875323218853L );
        set.add( 1076153510890766525L );
        set.add( 3777659743519674290L );
        set.add( 965271134950276218L );
        set.add( 959573531184926997L );
        set.add( 873283422351147806L );
        set.add( 8282976881167657764L );
        set.add( 4385624204720481807L );
        set.add( 3300422770936195076L );
        set.add( 4385624204720481807L );
        set.remove( 1597827662634390203L );
        set.add( 2123771117500926867L );
        set.add( 8654889166816953623L );
        set.add( 1889471738094813427L );
        set.add( 6233237857754146735L );
        set.remove( 520661468394995850L );
        set.add( 1880646517432937902L );
        set.remove( 8294661536239705144L );
        set.add( 3143404348876493320L );
        set.remove( 3300422770936195076L );
        set.add( 3202948401879942100L );
        set.add( 2123771117500926867L );
        set.add( 2957297892184601966L );
        set.remove( 8750691814490478747L );
        set.add( 931068770152336933L );
        set.add( 4033860562228899730L );
        set.add( 6613045488674573626L );
        set.add( 6762238128945853988L );
        set.add( 5192246918162468179L );
        set.add( 3158292988612935534L );
        set.remove( 6762238128945853988L );
        set.add( 8923295482907275908L );
        set.add( 4033860562228899730L );
        set.remove( 1120748360969261190L );
        set.add( 2249369307573050587L );
        set.add( 1959863435666691382L );
        set.add( 6109686342232516513L );
        set.remove( 1988041139911619632L );
        set.remove( 8923295482907275908L );
        set.add( 7046791269612125378L );
        set.add( 2250765789552053525L );
        set.remove( 2703685690387365935L );
        set.add( 2110423206743870294L );
        set.add( 1080893737764323731L );
        set.add( 9166822468818649217L );
        set.add( 7591600486553773290L );
        set.add( 7677823120162327375L );
        set.add( 9079215634086660606L );
        set.add( 1000684369116756758L );
        set.add( 7909495933485430979L );
        set.add( 3995428824283440653L );
        set.add( 5991906290569163352L );
        set.remove( 3777659743519674290L );
        set.remove( 8326121426766585829L );
        set.add( 873283422351147806L );
        set.add( 6109686342232516513L );
        set.add( 3902903775736799309L );
        set.remove( 6887323157755945733L );
        set.add( 3158292988612935534L );
        set.add( 5192246918162468179L );
        set.remove( 1042225283405976425L );
        set.add( 4984801032163601591L );
        set.add( 8617416773792593124L );
        set.remove( 7900384434767407625L );
        set.add( 6695890809610880139L );
        set.add( 965271134950276218L );
//        set.remove( 7138038838234090473L );
//        set.add( 5368251703170950758L );
//        set.add( 4875992633358361557L );
//        set.add( 2838339734854418948L );
//        set.add( 8805267694454601399L );
//        set.remove( 8323831897145649586L );
//        set.add( 1889471738094813427L );
//        set.add( 77466870816884524L );
//        set.add( 1834226226347098333L );
//        set.add( 6695890809610880139L );
//        set.add( 1550984506696833928L );
//        set.remove( 7460929546909135206L );
//        set.add( 1000684369116756758L );
//        set.remove( 1949360323094501965L );
//        set.remove( 4855995272475720353L );
//        set.add( 7586143081507971082L );
//        set.add( 6181891596102684254L );
//        set.add( 7857842784391464142L );
//        set.add( 2254033120346733791L );
//        set.add( 5791257869260966061L );
//        set.add( 5903024123717868026L );
//        set.remove( 1083920865877990363L );
//        set.add( 3674378746651978263L );
//        set.add( 291292874733474123L );
//        set.add( 4904489530254570160L );
//        set.add( 6756638739450233343L );
//        set.remove( 5479225921623475909L );
//        set.add( 1293560837009932744L );
        set.add( 7541468417122517635L );
//        set.add( 185355773938486852L );
//        set.add( 5678213602895031990L );
//        set.remove( 1080893737764323731L );
//        set.add( 5991906290569163352L );
//        set.add( 4677416021806652910L );
//        set.add( 4385624204720481807L );
//        set.add( 6333881819460752019L );
//        set.add( 1788948889827755729L );
//        set.add( 5955543215242963105L );
//        set.add( 8730557060034285536L );
//        set.remove( 8730557060034285536L );
//        set.add( 2253745148511423400L );
//        set.add( 4086194287272393697L );
//        set.add( 7857842784391464142L );
//        set.add( 7852501413832209982L );
//        set.remove( 6658518276093654454L );
//        set.add( 2957297892184601966L );
//        set.add( 2160454188889420679L );
//        set.add( 7138099937169279805L );
//        set.add( 3674378746651978263L );
//        set.add( 4585072609797600156L );
//        set.add( 2758698279967921857L );
//        set.add( 4875992633358361557L );
//        set.add( 3125770930776623196L );
//        set.add( 7213148910906575008L );
//        set.remove( 4166536820076443326L );
//        set.add( 7712616919567612938L );
//        set.add( 5976237919188948213L );
//        set.add( 6695890809610880139L );
//        set.add( 2126428637819054436L );
//        set.add( 8206982758311669701L );
//        set.add( 4654963727271544253L );
        set.remove( 4385624204720481807L );
//        set.add( 2775951392531958120L );
//        set.add( 1853934339299427303L );
//        set.add( 1853934339299427303L );
//        set.add( 4723898240505325505L );
//        set.add( 4585072609797600156L );
//        set.add( 7644853274424385905L );
//        set.remove( 787713361807692241L );
//        set.add( 597693115690984440L );
//        set.add( 5991906290569163352L );
//        set.add( 6134220320635315750L );
//        set.remove( 7071432764829805982L );

        // WHEN/THEN
        boolean existedBefore = set.contains( 7541468417122517635L );
        boolean added = set.add( 7541468417122517635L );
        boolean existsAfter = set.contains( 7541468417122517635L );
        assertTrue( "7541468417122517635 should exist before adding here", existedBefore );
        assertFalse( "7541468417122517635 should not be reported as added here", added );
        assertTrue( "7541468417122517635 should exist", existsAfter );
    }
}
