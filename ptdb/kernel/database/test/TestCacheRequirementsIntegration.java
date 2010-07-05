package ptdb.kernel.database.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.save.SaveModelManager;
import ptdb.kernel.database.CacheManager;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// TestCacheRequirementsIntegration

/**
 * Unit tests for TestCacheManager.
 * 
 * @author lholsing
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (lholsing)
 * @Pt.AcceptedRating Red (lholsing)
 * 
 */

public class TestCacheRequirementsIntegration {
    
    /**
     * Verify that models in the cache can be updated.
     * 
     * @throws Exception 
     */
    @Test
    public void testUpdateCache() throws Exception {
/*
        java.util.Date time = new java.util.Date();
        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"" + dbModel.getModelName() + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                        + "</property>"
                        + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={232, 141, 815, 517}, maximized=false}\">"
                        + "</property>"
                        + "<property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                        + "</property>"
                        + "<property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                        + "</property>"
                        + "<property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                        + "</property>"
                        + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                        + "<doc>Create a constant sequence.</doc>"
                        + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                        + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                        + "</property>"
                        + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                        + "</property>"
                        + "</property>"
                        + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{150, 150}\">"
                        + "</property>" + "</entity>" + "</entity>");

        HashMap assemblies = new HashMap();
        
        ArrayList<XMLDBModel> modelsToRemove = new ArrayList();
   
        modelsToRemove.add(dbModel);
        
        SaveModelManager saveManager = new SaveModelManager();
        
        String modelID = saveManager.save(dbModel);
       
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"" + dbModel.getModelName() + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"DBModelID\" class=\"ptolemy.data.expr.StringParameter\" value=\"" + modelID + "\">"
                + "</property>"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={232, 141, 815, 517}, maximized=false}\">"
                + "</property>"
                + "<property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                + "</property>"
                + "<property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                + "</property>"
                + "<property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                + "</property>"
                + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{150, 150}\">"
                + "</property>" + "</entity>" + "</entity>");
        dbModel.setModelId(modelID);

        assemblies.put(dbModel.getModelName(), dbModel.getModel());
        
        CacheManager.updateCache(assemblies);
        
        assertTrue(true);
        
        CacheManager.removeFromCache(modelsToRemove);
        
        assertTrue(true);
        
        PowerMock.verifyAll();
 */
    }
  
 
    /**
     * Verify that given a model name, null is not returned.
     * @exception Exception
     */
    @Test
    public void testLoadFromCache() throws Exception {
/*
        String loadModel = "model1";

        PowerMock.mockStatic(DBConnectorFactory.class);

        GetModelTask getModelTaskMock = PowerMock.createMock(GetModelTask.class);
        DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false)).andReturn(dBConnectionMock);
        PowerMock.expectNew(GetModelTask.class, loadModel).andReturn(getModelTaskMock);
        EasyMock.expect(dBConnectionMock.executeGetModelTask(getModelTaskMock)).andReturn(modelMock);

        dBConnectionMock.closeConnection();

        PowerMock.replayAll();

        XMLDBModel dbModel = null;
        dbModel = CacheManager.loadFromCache(loadModel);
        assertNotNull(dbModel);

        PowerMock.verifyAll();
*/
    }
    
    /**
     * Verify that given a list of XMLDBModels can be successfully removed
     * from the cache.
     * 
     * @throws Exception 
     */
    @Test
    public void testRemoveFromCache() throws Exception {

 /*
        PowerMock.mockStatic(DBConnectorFactory.class);
        
        XMLDBModel dbModel1 = new XMLDBModel("model1");
        XMLDBModel dbModel2 = new XMLDBModel("model2");
        XMLDBModel dbModel3 = new XMLDBModel("model3");
    
        ArrayList<XMLDBModel> modelsToRemoveList = new ArrayList();
        
        modelsToRemoveList.add(dbModel1);
        modelsToRemoveList.add(dbModel2);
        modelsToRemoveList.add(dbModel3);
    
        DBConnection dbConnectionMock = PowerMock
            .createMock(DBConnection.class);
        
        EasyMock.expect(DBConnectorFactory.getCacheConnection(true))
            .andReturn(dbConnectionMock);

        RemoveModelsTask removeModelsMock = PowerMock
        .createMockAndExpectNew(RemoveModelsTask.class,
                modelsToRemoveList);
    
        dbConnectionMock.executeRemoveModelsTask(removeModelsMock);
        
        dbConnectionMock.commitConnection();
        dbConnectionMock.closeConnection();

        PowerMock.replayAll();

        boolean success = false;
        success = CacheManager.removeFromCache(modelsToRemoveList);
        assertTrue(success);

        PowerMock.verifyAll();
*/
    }
    
    /** Test removing models from the cache with null input.
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveWithNullModelList() throws Exception {
/*
        ArrayList<XMLDBModel> modelsToRemoveList = null;

        PowerMock.replayAll();

        boolean success = false;
        
        try{
            
            CacheManager.removeFromCache(modelsToRemoveList);
        
        } catch(IllegalArgumentException e){
            
            success = true;
        
        }
        assertTrue(success);
        
        PowerMock.verifyAll();
  */
    }
    
    /** Test removing models from the cache with a null connection.
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveWithNullConnection() throws Exception {
/*
        PowerMock.mockStatic(DBConnectorFactory.class);
        
        XMLDBModel dbModel1 = new XMLDBModel("model1");
        XMLDBModel dbModel2 = new XMLDBModel("model2");
        XMLDBModel dbModel3 = new XMLDBModel("model3");
    
        ArrayList<XMLDBModel> modelsToRemoveList = new ArrayList();
        
        modelsToRemoveList.add(dbModel1);
        modelsToRemoveList.add(dbModel2);
        modelsToRemoveList.add(dbModel3);
    
        DBConnection dbConnectionMock = null;
        
        EasyMock.expect(DBConnectorFactory.getCacheConnection(true))
            .andReturn(dbConnectionMock);

        PowerMock.replayAll();

        boolean success = false;
        try{

            CacheManager.removeFromCache(modelsToRemoveList);
        
        } catch(DBConnectionException e){
            
            success = true;
            
        }
            
        assertTrue(success);

        PowerMock.verifyAll();
 */
    }
    
    /** Test loading with a null argument.
     * 
     * @throws Exception
     */
    @Test
    public void testLoadWithNullModelName() throws Exception {
/*
        String loadModel = null;

        PowerMock.replayAll();

        boolean success = false;
        
        try {
            
            XMLDBModel dbModel = null;
            dbModel = CacheManager.loadFromCache(loadModel);

        } catch( IllegalArgumentException e){
            
            success = true;
        
        }
        
        assertTrue(success);

        PowerMock.verifyAll();
 */
    }
    

    /** Test loading with a null connection.
     * @throws Exception
     */
    @Test
    public void testLoadWithNullConnection() throws Exception {
 /*
        String loadModel = "model1";

        PowerMock.mockStatic(DBConnectorFactory.class);

        DBConnection dBConnectionMock = null;

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false)).andReturn(dBConnectionMock);
        
        PowerMock.replayAll();

        boolean success = false;
        
        try {
        
            XMLDBModel dbModel = null;
            dbModel = CacheManager.loadFromCache(loadModel);
        
        } catch (DBConnectionException e){
            
            success = true;
            
        }

        assertTrue(success);

        PowerMock.verifyAll();
  */
    }
    
    /** Test updating the cache with a null argument.
     * @throws Exception
     */
    @Test
    public void testUpdateCacheWithNullHashMap() throws Exception {
/*
        PowerMock.mockStatic(DBConnectorFactory.class);
        
        HashMap assemblies = null;
        
        PowerMock.replayAll();

        boolean isSuccess = false;
        
        try{
            
        CacheManager.updateCache(assemblies);
        
        } catch(IllegalArgumentException e){
            
            isSuccess = true;
        
        }
        assertTrue(isSuccess);

        PowerMock.verifyAll();
*/
    }
    
    /** Test updating cache with null exception.
     * @throws Exception
     */
    @Test
    public void testUpdateCacheWithNullConnection() throws Exception {
/*
        PowerMock.mockStatic(DBConnectorFactory.class);
        
        HashMap assemblies = new HashMap();
        
        DBConnection dbConnectionMock = null;
        
        EasyMock.expect(DBConnectorFactory.getCacheConnection(true))
            .andReturn(dbConnectionMock);
        
        PowerMock.replayAll();

        boolean isSuccess = false;
        
        try{
        
            CacheManager.updateCache(assemblies);
        
        } catch(DBConnectionException e){
            
            isSuccess = true;
            
        }
        
        assertTrue(isSuccess);

        PowerMock.verifyAll();
        
        */
    }
}
