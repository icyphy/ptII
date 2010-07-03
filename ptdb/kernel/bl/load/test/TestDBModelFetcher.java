package ptdb.kernel.bl.load.test;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;

import static org.junit.Assert.assertNotNull;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.load.DBModelFetcher;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// TestDBModelFetcher

/**
* This JUnit tests that, assuming the Database layer is correct,
* load() not return null.
*
* @author Lyle Holsinger
* @since Ptolemy II 8.1
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*/

@RunWith(PowerMockRunner.class)
@PrepareForTest({DBModelFetcher.class, DBConnectorFactory.class, GetModelTask.class})
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestDBModelFetcher {

    /**
     * Verify that given a model name, null is not returned.
     * @exception Exception
     */
    @Test
    public void testLoad() throws Exception {

        String loadModel = "model1";

        PowerMock.mockStatic(DBConnectorFactory.class);

        GetModelTask getModelTaskMock = PowerMock.createMock(GetModelTask.class);
        DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(dBConnectionMock);
        PowerMock.expectNew(GetModelTask.class, loadModel).andReturn(getModelTaskMock);
        EasyMock.expect(dBConnectionMock.executeGetCompleteModelTask(getModelTaskMock)).andReturn(modelMock);

        dBConnectionMock.closeConnection();

        //Execute the test.  Verify that, load does not return null if the database layer is mocked.
        PowerMock.replayAll();

        XMLDBModel dbModel = null;
        dbModel = DBModelFetcher.load(loadModel);
        assertNotNull(dbModel);

        PowerMock.verifyAll();

    }

}
