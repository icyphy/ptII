/*
 * 
 */
package ptdb.kernel.bl.search.tests;

import java.util.ArrayList;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptdb.kernel.database.DBConnection;


///////////////////////////////////////////////////////////////
//// TestSearchManager

/**
 * This class is the JUnit test case for class TestSearchManager.
 * 
 * @author Alek Wang
 * @version $Id$
 * @since
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchManager.class)
public class TestSearchManager {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                  //////

        
        /**
         * Test method for {@link ptdb.kernel.bl.search.SearchManager#search(ptdb.common.dto.SearchCriteria, ptdb.kernel.bl.search.ResultHandler)}.
         * @throws Exception 
         */
        @Test
        public void testSearch() throws Exception {
            
            
            
            SearchManager searchManager = new SearchManager();
            
            SearchCriteria searchCriteria = new SearchCriteria();
            SearchResultBuffer searchResultBuffer = new SearchResultBuffer();
            
            DBConnection dbConnectionMock = PowerMock.createMock(DBConnection.class);
    
    
            mockStatic(DBConnectorFactory.class);
            
            expect(DBConnectorFactory.getSyncConnection(false)).andReturn(dbConnectionMock);
    
            
            GetAttributesTask getAttributesTaskMock = PowerMock.createMock(GetAttributesTask.class);
            PowerMock.expectNew(GetAttributesTask.class).andReturn(getAttributesTaskMock);
            
            expect(dbConnectionMock.executeGetAttributesTask(getAttributesTaskMock)).andReturn(new ArrayList<XMLDBModel>());
            
            dbConnectionMock.closeConnection();
    
    //        PowerMock.replay(DBConnectorFactory.class);
            PowerMock.replayAll();
            
            
            searchManager.search(searchCriteria, searchResultBuffer);
    
            
            PowerMock.verifyAll();
            
        }

//    /**
//     * Test method for {@link ptdb.kernel.bl.search.SearchManager#search(ptdb.common.dto.SearchCriteria, ptdb.kernel.bl.search.ResultHandler)}.
//     * @throws Exception 
//     */
//    @Test
//    public void testSearch() throws Exception {
//
//        DBConnection dbConnectionMock = PowerMock.createMock(DBConnection.class);
//        
//        mockStatic(DBConnectorFactory.class);
//
//        expect(DBConnectorFactory.getSyncConnection(false)).andReturn(dbConnectionMock);
//
//        PowerMock.replay(DBConnectorFactory.class);
//
//        DBConnectorFactory.getSyncConnection(false);
//
//        PowerMock.verify(DBConnectorFactory.class);
//
//    }

}
