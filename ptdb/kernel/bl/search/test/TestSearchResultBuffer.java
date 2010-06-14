/*
 *
 */
package ptdb.kernel.bl.search.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.search.SearchResultBuffer;

///////////////////////////////////////////////////////////////////
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
@PrepareForTest( { SearchResultBuffer.class, Observable.class })
public class TestSearchResultBuffer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Set up the test by creating a new SearchResultBuffer.
     */
    @Before
    public void setUp() {

        _searchResultBufferTest = new SearchResultBuffer();

    }

    /**
     * Test the handleResults() method. Test the case when null is sent to
     * the buffer as the results.
     */
    @Test
    public void testHandleResultsNull() {
        _searchResultBufferTest.handleResults(null);
        assertNull(_searchResultBufferTest.getResults());
    }

    /**
     * Test the handleResults() and getResults() methods.
     */
    @Test
    public void testHandleResultsWithResults() {

        Observer observerMock = PowerMock.createMock(Observer.class);

        observerMock.update(_searchResultBufferTest, null);
        observerMock.update(_searchResultBufferTest, null);

        PowerMock.replayAll();

        ArrayList<XMLDBModel> modelResults = new ArrayList();
        _modelResults2 = new ArrayList<XMLDBModel>();

        for (int i = 0; i < 20; i++) {

            _modelResults2.add(new XMLDBModel());

        }

        _modelResults3 = new ArrayList<XMLDBModel>();
        for (int i = 20; i < 30; i++) {

            _modelResults3.add(new XMLDBModel());

        }

        _searchResultBufferTest.addObserver(observerMock);

        _searchResultBufferTest.handleResults(modelResults);
        _searchResultBufferTest.handleResults(_modelResults2);
        _searchResultBufferTest.handleResults(_modelResults3);

        _modelResults2.addAll(_modelResults3);
        assertEquals(_modelResults2, _searchResultBufferTest.getResults());
        assertNull(_searchResultBufferTest.getResults());

        PowerMock.verifyAll();
    }

    /**
     * Test the isSearchCancelled() method.
     */
    @Test
    public void testIsSearchCancelled() {

        assertFalse(_searchResultBufferTest.isSearchCancelled());

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ArrayList<XMLDBModel> _modelResults2;

    private ArrayList<XMLDBModel> _modelResults3;

    private SearchResultBuffer _searchResultBufferTest;

}
