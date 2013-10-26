/*
@Copyright (c) 2010-2013 The Regents of the University of California.
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
/*
 *
 */
package ptdb.kernel.bl.search.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TestSearchByAttributeIntegration

/**
 * Integration test case for integrating the business layer and DB layer for
 * requirement search by attributes.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class TestSearchByAttributeIntegration {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Test searching the models by empty search criteria.
     *
     * @exception DBExecutionException Thrown by the db layer when errors
     *  occurs when executing the db task.
     * @exception DBConnectionException Thrown by the db layer when the db
     * connection cannot be created.
     *
     */
    @Test
    public void testSearchByAttributeNoCriteria() throws DBConnectionException,
            DBExecutionException {

        SearchManager searchManager = new SearchManager();
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        searchManager.search(searchCriteria, searchResultBuffer);

        assertNull(searchResultBuffer.getResults());

    }

    /**
     * Test searching the models by attributes search criteria with found
     * results returned.
     *
     * @exception NameDuplicationException Thrown when setting name to the
     *  attribute criteria.
     * @exception IllegalActionException Thrown when setting name to the
     *  attribute criteria.
     * @exception DBExecutionException Thrown by the db layer when errors
     *  occurs when executing the db task.
     * @exception DBConnectionException Thrown by the db layer when the db
     * connection cannot be created.
     */
    @Test
    public void testSearchByAttributeWithResults()
            throws IllegalActionException, NameDuplicationException,
            DBConnectionException, DBExecutionException {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        StringParameter criteriaVariable = new StringParameter(new Entity(),
                "CreatedBy");
        criteriaVariable.setToken("Ashwini Bijwe");

        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
        attributesList.add(criteriaVariable);
        searchCriteria.setAttributes(attributesList);

        searchManager.search(searchCriteria, searchResultBuffer);

        ArrayList<XMLDBModel> resultModels = searchResultBuffer.getResults();

        boolean doesExist = false;

        for (XMLDBModel model : resultModels) {
            if (model.getModelName().equals("ModelB")) {
                doesExist = true;
                break;
            }
        }
        assertTrue(doesExist);

    }

    /**
     * Test searching the models by attributes search criteria without found
     * result returned.
     *
     * @exception NameDuplicationException Thrown when setting name to the
     *  attribute criteria.
     * @exception IllegalActionException Thrown when setting name to the
     *  attribute criteria.
     * @exception DBExecutionException Thrown by the db layer when errors
     * occurs when executing the db task.
     * @exception DBConnectionException Thrown by the db layer when the db
     * connection cannot be created.
     */
    @Test
    public void testSearhcByAttributeWithoutResult()
            throws IllegalActionException, NameDuplicationException,
            DBConnectionException, DBExecutionException {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        StringParameter criteriaVariable = new StringParameter(new Entity(),
                "No Match attribute");
        criteriaVariable.setToken("test valuexxx");

        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
        attributesList.add(criteriaVariable);

        searchCriteria.setAttributes(attributesList);

        searchManager.search(searchCriteria, searchResultBuffer);

        assertNull(searchResultBuffer.getResults());

    }

}
