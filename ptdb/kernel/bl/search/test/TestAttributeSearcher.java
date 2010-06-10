/*
 * 
 */
package ptdb.kernel.bl.search.test;

import java.util.ArrayList;

import org.junit.Test;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.search.AttributeSearcher;

///////////////////////////////////////////////////////////////
//// TestAttributeSearcher

/**
 * JUnit test case for testing AttributeSearcher. 
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class TestAttributeSearcher {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                  //////

    /**
     * Test the handleResults() method. Test the case when search criteria
     *  is not set. 
     * 
     * @exception DBExecutionException Happens in the execution 
     *          of DB tasks, and is thrown by the concrete searcher when they
     *          are performing the actual searching in the database. 
     * @exception DBConnectionException Thrown by the DBConnectorFactory
     *           when getting the DBConnection from it, which
     *          indicates that the DBConnection cannot be obtained.
     * 
     */
    @Test
    public void testHandleResults() throws DBConnectionException,
            DBExecutionException {

        AttributeSearcher attributeSearcher = new AttributeSearcher(
                new SearchCriteria());

        attributeSearcher.handleResults(new ArrayList<XMLDBModel>());

    }

}
