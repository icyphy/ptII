/*
@Copyright (c) 2010-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


*/
package ptdb.kernel.bl.search.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.SearchCriteriaParseException;
import ptdb.kernel.bl.search.SearchCriteriaManager;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////
//// TestSearchCriteriaManager

/**
 * Test cases for uniting testing class SearchCriteriaManager.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class TestSearchCriteriaManager {

    /**
     * Test methods open() and save() with the complete working flow.
     *
     * @exception IOException Thrown if IO exception happens during writing or
     * reading the file.
     * @exception SearchCriteriaParseException Thrown if errors happen during
     * the parsing of the search criteria file.
     * @exception NameDuplicationException Thrown if attributes with duplicated
     * names are found in the search criteria file.
     * @exception IllegalActionException Thrown if failed to create the Ptolemy
     * configuration.
     */
    @Test
    public void testOpenAndSave() throws IllegalActionException,
            NameDuplicationException, SearchCriteriaParseException, IOException {

        String workingPath = System.getProperty("user.dir");
        if (workingPath.contains("build")) {
            workingPath = workingPath.substring(0, workingPath.length() - 11);
        }

        SearchCriteria storedSearchCriteria1 = SearchCriteriaManager.open(
                workingPath + "/ptdb/kernel/bl/search/test/searchcriteria.xml",
                new Configuration(new Workspace()));
        ;

        SearchCriteriaManager.save(storedSearchCriteria1, workingPath
                + "/ptdb/kernel/bl/search/test/searchcriteria2.xml");

        SearchCriteria storedSearchCriteria2 = SearchCriteriaManager
                .open(workingPath
                        + "/ptdb/kernel/bl/search/test/searchcriteria2.xml",
                        new Configuration(new Workspace()));
        ;

        SearchCriteriaManager.save(storedSearchCriteria2, workingPath
                + "/ptdb/kernel/bl/search/test/searchcriteria3.xml");

        assertEquals("test", storedSearchCriteria1.getModelName());

        assertEquals(storedSearchCriteria1.getModelName(),
                storedSearchCriteria2.getModelName());
        assertEquals(storedSearchCriteria1.getPatternMoML(),
                storedSearchCriteria2.getPatternMoML());

        ArrayList<Attribute> attributesList1 = storedSearchCriteria1
                .getAttributes();
        ArrayList<Attribute> attributesList2 = storedSearchCriteria2
                .getAttributes();

        for (int i = 0; i < storedSearchCriteria1.getAttributes().size(); i++) {
            assertEquals(attributesList1.get(i).getName(),
                    attributesList2.get(i).getName());
            assertEquals(
                    ((StringParameter) attributesList1.get(i))
                            .getValueAsString(),
                    ((StringParameter) attributesList2.get(i))
                            .getValueAsString());
        }

    }

    /**
     * Test the method open() with the case no file can be found in the
     * given location.
     *
     * @exception IOException Thrown if IO exception happens during writing or
     * reading the file.
     * @exception SearchCriteriaParseException Thrown if errors happen during
     * the parsing of the search criteria file.
     * @exception NameDuplicationException Thrown if attributes with duplicated
     * names are found in the search criteria file.
     * @exception IllegalActionException Thrown if failed to create the Ptolemy
     * configuration.
     */
    @Test
    public void testOpenNoFileFound() throws IllegalActionException,
            NameDuplicationException, SearchCriteriaParseException, IOException {

        boolean flag = false;

        try {
            SearchCriteriaManager.open("searchcriteria333.xml",
                    new Configuration(new Workspace()));
            ;
        } catch (FileNotFoundException e) {
            flag = true;
        }

        assertTrue(flag);
    }

}
