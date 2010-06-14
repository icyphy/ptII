package ptdb.kernel.bl.load.test;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;

import static org.junit.Assert.assertNotNull;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.load.LoadModelManager;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
////TestLoadModelManager

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
@PrepareForTest({LoadModelManager.class, DBConnectorFactory.class, GetModelsTask.class})
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestLoadModelManager {

    /**
     * Verify that given a model name, null is not returned.
     * @exception Exception
     */
    @Test
    public void testLoad() throws Exception {


        String loadModel = "model1";
        LoadModelManager tested = new LoadModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);

        GetModelsTask getModelsTaskMock = PowerMock.createMock(GetModelsTask.class);
        DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);

        PowerMock.expectNew(GetModelsTask.class).andReturn(getModelsTaskMock);
        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(dBConnectionMock);
        getModelsTaskMock.setModelName(loadModel);
        EasyMock.expect(dBConnectionMock.executeGetModelsTask(getModelsTaskMock)).andReturn(modelMock);
        dBConnectionMock.closeConnection();

        //Execute the test.  Verify that, load does not return null if the database layer is mocked.
        PowerMock.replayAll();

        XMLDBModel dbModel = null;
        dbModel = tested.load(loadModel);
        assertNotNull(dbModel);

        PowerMock.verifyAll();

    }

}
