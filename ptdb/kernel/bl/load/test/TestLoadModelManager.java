package ptdb.kernel.bl.load.test;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import org.powermock.core.classloader.annotations.PrepareForTest;
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
* the number of model names passed to load() will be the same
* as the number of XMLDBModels returned.
*
* @author Lyle Holsinger
* @since Ptolemy II 8.1
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*/

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoadModelManager.class)
public class TestLoadModelManager {

    /**
     * Verify that given two model names, two XMLDBModels are returned.
     * @throws Exception
     */
    @Test
    public void testLoad() throws Exception {
        /*
        
        //Fails on DBConnectoFactory static method.
        String[] loadModel = {"model1", "model2"};
        LoadModelManager tested = new LoadModelManager();
        
        PowerMock.mockStatic(DBConnectorFactory.class);
        
        GetModelsTask getModelsTaskMock = PowerMock.createMock(GetModelsTask.class);
        DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);
        
        EasyMock.expect(DBConnectorFactory.getSyncConnection(false) ).andReturn(dBConnectionMock);
        EasyMock.expect(dBConnectionMock.executeGetModelsTask(getModelsTaskMock)).andReturn(modelMock);
        
        //Execute the test.  Verify that, load returns an ArrayList of 2 XMLDBModel.
        PowerMock.replayAll();
        
        ArrayList<XMLDBModel> modelList = new ArrayList();
        modelList = tested.load(loadModel);
        assertEquals(modelList.size(), 2);
        
        PowerMock.verifyAll();  
        */
    }

}
