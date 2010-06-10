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
import ptdb.kernel.bl.search.SearchResultBuffer;

///////////////////////////////////////////////////////////////
//// TestSearchResultBuffer

/**
 * The JUnit test case for testing class SearchResutlBuffer.
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @see SearchResultBuffer
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchResultBuffer.class)
public class TestSearchResultBuffer {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                //////

    /**
     * Set up the test by creating a new SearchResultBuffer.
     */
    @Before
    public void setUp() {

        _searchResultBufferTest = new SearchResultBuffer();

    }

    /**
     * Test the handleResults() method. 
     */
    @Test
    public void testHandleResults() {

        ArrayList<XMLDBModel> modelResults = new ArrayList();
        _modelResults2 = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 20; i++) {

            _modelResults2.add(new XMLDBModel());

        }

        _searchResultBufferTest.handleResults(modelResults);
        _searchResultBufferTest.handleResults(_modelResults2);

    }

    /**
     * Test the getResults() method. 
     */
    @Test
    public void testGetResults() {

        assertEquals(_modelResults2, _searchResultBufferTest.getResults());
        assertNull(_searchResultBufferTest.getResults());

    }

    /**
     * Test the isSearchCancelled() method. 
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
