/*
 * 
 */
package ptdb.gui.test;

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
//// TestActorGraphDBFrame

/**
 * The JUnit test case for ActorGraphDBFrame.
 * 
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (neuendor)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchManager.class)
public class TestActorGraphDBFrame {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                              //////

        
    /**
     * Stub for testing.  
     * @exception Exception Not thrown in this base clase. 
     */
    @Test
    public void test() throws Exception {
    }
}
