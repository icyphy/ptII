package ptdb.kernel.bl.load.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.load.LoadManagerInterface;
import ptdb.kernel.bl.load.LoadModelManager;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.util.Workspace;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoadManagerInterface.class)
public class TestLoadManagerInterface {

    public void testloadModels() throws Exception{
        
        /*
        String[] inputArray = {"model1.xml"};
        ArrayList<XMLDBModel> effigyList = new XMLDBModel();
        Workspace workspace = new Workspace();
        PtolemyEffigy effigy = new PtolemyEffigy(workspace);
        effigyList.add(effigy);
        
        LoadModelManager loadManagerMock = PowerMock.createMock(LoadModelManager.class);
        
        PowerMock.expectNew(LoadModelManager.class).andReturn(loadManagerMock);
        
        EasyMock.expect(loadManagerMock.load(inputArray)).andReturn(effigyList);
        */
    }
}
