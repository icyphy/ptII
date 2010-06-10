/*
 * 
 */
package ptdb.kernel.bl.search.test;

import java.util.ArrayList;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.search.AttributeSearcher;
import ptdb.kernel.bl.search.CommandSearcher;
import ptdb.kernel.bl.search.HierarchyFetcher;
import ptdb.kernel.bl.search.PatternMatchGraphSearcher;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptdb.kernel.bl.search.XQueryGraphSearcher;
import ptdb.kernel.database.DBConnection;
import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////
//// TestSearchManager

/**
 * JUnit test case for class TestSearchManager.
 * 
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { SearchManager.class, AttributeSearcher.class,
        CommandSearcher.class, XQueryGraphSearcher.class,
        PatternMatchGraphSearcher.class, HierarchyFetcher.class,
        SearchResultBuffer.class })
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestSearchManager {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                  //////

    /**
     * Test the search() method.
     * 
     * @exception Exception Thrown by PowerMock during the execution of test 
     *  cases. 
     */
    @Test
    public void testSearch() throws Exception {

        SearchManager searchManager = new SearchManager();

        SearchCriteria searchCriteria = new SearchCriteria();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        Attribute attribute = new Attribute();
        attribute.setName("test name");
        attributes.add(attribute);

        searchCriteria.setAttributes(attributes);

        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        GetAttributesTask getAttributesTaskMock = PowerMock
                .createMockAndExpectNew(GetAttributesTask.class);
        
        mockStatic(DBConnectorFactory.class);

        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dbConnectionMock);

        expect(dbConnectionMock.executeGetAttributesTask(getAttributesTaskMock))
                .andReturn(new ArrayList<XMLDBModel>());

        dbConnectionMock.closeConnection();

        PowerMock.replayAll();

        searchManager.search(searchCriteria, searchResultBuffer);

        PowerMock.verifyAll();

    }

}
