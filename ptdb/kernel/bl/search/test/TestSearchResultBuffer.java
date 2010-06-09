/*
 * 
 */
package ptdb.kernel.bl.search.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.kernel.bl.search.SearchResultBuffer;

///////////////////////////////////////////////////////////////
//// TestSearchResultBuffer

/**
 * The JUnit test case class for testing class SearchResutlBuffer.
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @see SearchResultBuffer
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchResultBuffer.class)
public class TestSearchResultBuffer {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                //////

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        _searchResultBufferTest = new SearchResultBuffer();

    }

    /**
     * Test method for {@link ptdb.kernel.bl.search.SearchResultBuffer#
     * handleResults(java.util.ArrayList)}.
     * 
     * Test the initial write to the buffer. 
     * 
     * @exception DBConnectionException Exception from the database connection.
     */
    @Test
    public void testHandleResultsInitial() throws DBConnectionException {

        ArrayList<XMLDBModel> modelResults = new ArrayList();
        _modelResults2 = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 20; i++) {

            _modelResults2.add(new XMLDBModel());

        }

        _searchResultBufferTest.handleResults(modelResults);
        _searchResultBufferTest.handleResults(_modelResults2);

    }

    /**
     * Test method for {@link ptdb.kernel.bl.search.SearchResultBuffer
     * #getResults()}.
     *
     */
    @Test
    public void testGetResults() {

        assertEquals(_modelResults2, _searchResultBufferTest.getResults());
        assertNull(_searchResultBufferTest.getResults());

    }

    /**
     * Test method for {@link ptdb.kernel.bl.search.SearchResultBuffer#
     * isSearchCancelled()}.
     */
    @Test
    public void testIsSearchCancelled() {

        assertFalse(_searchResultBufferTest.isSearchCancelled());

    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables                              //////

    private SearchResultBuffer _searchResultBufferTest;

    private ArrayList<XMLDBModel> _modelResults2;

}
