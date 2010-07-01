/*
 *
 */
package ptdb.kernel.bl.search.test;

import org.junit.Test;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBConnectionException;
import ptdb.kernel.bl.search.AttributeSearcher;

///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * The the wholeSearchDone() method.
     *
     * <p>Test the case when there is no next result handler set in the
     * searcher. </p>
     * @exception DBConnectionException Thrown if the DB connection cannot be 
     * attained. 
     */
    @Test
    public void testWholeSearchDoneWithoutNextHandler() throws DBConnectionException {
        AttributeSearcher attributeSearcher = new AttributeSearcher(
                new SearchCriteria());

        attributeSearcher.wholeSearchDone();
    }

}
