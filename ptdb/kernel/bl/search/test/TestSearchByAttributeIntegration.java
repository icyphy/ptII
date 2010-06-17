/*
 * 
 */
package ptdb.kernel.bl.search.test;

import static org.junit.Assert.*;

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

///////////////////////////////////////////////////////////////
//// TestSearchByAttributeIntegration

/**
 * Integration test case for integrating the business layer and DB layer for 
 * requirement search by attributes. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class TestSearchByAttributeIntegration {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                ////

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

        StringParameter criteriaVariable = new StringParameter(new Entity(), "TestAttribute");
        criteriaVariable.setToken("test value");


        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
        attributesList.add(criteriaVariable);
        searchCriteria.setAttributes(attributesList);

        searchManager.search(searchCriteria, searchResultBuffer);

        ArrayList<XMLDBModel> resultModels = searchResultBuffer.getResults();

        assertEquals("Spectrum2.xml", resultModels.get(0).getModelName());
        assertEquals("model4", resultModels.get(1).getModelName());

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

        StringParameter criteriaVariable = new StringParameter(new Entity(), "No Match attribute");
        criteriaVariable.setToken("test valuexxx");

        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
        attributesList.add(criteriaVariable);

        searchCriteria.setAttributes(attributesList);

        searchManager.search(searchCriteria, searchResultBuffer);

        assertNull(searchResultBuffer.getResults());

    }

}
