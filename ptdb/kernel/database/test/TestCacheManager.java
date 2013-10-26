/*
@Copyright (c) 2010-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


*/
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
import ptdb.kernel.database.CacheManager;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// TestCacheManager

/**
 * Unit tests for TestCacheManager.
 *
 * @author lholsing
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (lholsing)
 * @Pt.AcceptedRating Red (lholsing)
 *
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CacheManager.class, DBConnectorFactory.class,
        RemoveModelsTask.class, GetModelTask.class, CreateModelTask.class,
        SaveModelTask.class, DBConnection.class })
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestCacheManager {

    /**
     * Verify that models in the cache can be updated.
     *
     * @exception Exception
     */
    @Test
    public void testUpdateCache() throws Exception {

        PowerMock.mockStatic(DBConnectorFactory.class);

        HashMap assemblies = new HashMap();

        int numModels = 1;
        for (int i = 0; i < numModels; i++) {

            assemblies.put("model" + i, "<entity name=\"model" + i
                    + "\" class="
                    + "\"ptolemy.actor.TypedCompositeActor\"></entity>");

        }

        DBConnection dbConnectionMock = PowerMock
                .createMock(DBConnection.class);

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false))
                .andReturn(dbConnectionMock);

        for (int i = 0; i < numModels; i++) {

            XMLDBModel updateModel = PowerMock.createMock(XMLDBModel.class);
            PowerMock.expectNew(XMLDBModel.class, "model" + i).andReturn(
                    updateModel);
            updateModel.setModel((String) assemblies.get("model" + i));

            //CreateModelTask createModelTask = PowerMock.createMock(CreateModelTask.class);
            //PowerMock.expectNew(CreateModelTask.class, updateModel).andReturn(createModelTask);

            //EasyMock.expect(dbConnectionMock.executeCreateModelTask
            //       (createModelTask)).andReturn(
            //        "ID");
            dbConnectionMock.executeUpdateModelInCache(updateModel);
        }

        dbConnectionMock.commitConnection();
        dbConnectionMock.closeConnection();

        PowerMock.replayAll();

        CacheManager.updateCache(assemblies);
        assertTrue(true);

        PowerMock.verifyAll();
    }

    /**
     * Verify that given a model name, null is not returned.
     * @exception Exception
     */
    @Test
    public void testLoadFromCache() throws Exception {

        String loadModel = "model1";

        PowerMock.mockStatic(DBConnectorFactory.class);

        GetModelTask getModelTaskMock = PowerMock
                .createMock(GetModelTask.class);
        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false))
                .andReturn(dBConnectionMock);
        PowerMock.expectNew(GetModelTask.class, loadModel).andReturn(
                getModelTaskMock);
        getModelTaskMock.setModelFromCache(true);
        EasyMock.expect(dBConnectionMock.executeGetModelTask(getModelTaskMock))
                .andReturn(modelMock);

        dBConnectionMock.closeConnection();

        PowerMock.replayAll();

        XMLDBModel dbModel = null;
        dbModel = CacheManager.loadFromCache(loadModel);
        assertNotNull(dbModel);

        PowerMock.verifyAll();

    }

    /**
     * Verify that given a list of XMLDBModels can be successfully removed
     * from the cache.
     *
     * @exception Exception
     */
    @Test
    public void testRemoveFromCache() throws Exception {

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

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false))
                .andReturn(dbConnectionMock);

        RemoveModelsTask removeModelsMock = PowerMock.createMockAndExpectNew(
                RemoveModelsTask.class, modelsToRemoveList);

        dbConnectionMock.executeRemoveModelsTask(removeModelsMock);

        dbConnectionMock.commitConnection();
        dbConnectionMock.closeConnection();

        PowerMock.replayAll();

        boolean success = false;
        success = CacheManager.removeFromCache(modelsToRemoveList);
        assertTrue(success);

        PowerMock.verifyAll();
    }

    /** Test removing models from the cache with null input.
     *
     * @exception Exception
     */
    @Test
    public void testRemoveWithNullModelList() throws Exception {

        ArrayList<XMLDBModel> modelsToRemoveList = null;

        PowerMock.replayAll();

        boolean success = false;

        try {

            CacheManager.removeFromCache(modelsToRemoveList);

        } catch (IllegalArgumentException e) {

            success = true;

        }
        assertTrue(success);

        PowerMock.verifyAll();
    }

    /** Test removing models from the cache with a null connection.
     *
     * @exception Exception
     */
    @Test
    public void testRemoveWithNullConnection() throws Exception {

        PowerMock.mockStatic(DBConnectorFactory.class);

        XMLDBModel dbModel1 = new XMLDBModel("model1");
        XMLDBModel dbModel2 = new XMLDBModel("model2");
        XMLDBModel dbModel3 = new XMLDBModel("model3");

        ArrayList<XMLDBModel> modelsToRemoveList = new ArrayList();

        modelsToRemoveList.add(dbModel1);
        modelsToRemoveList.add(dbModel2);
        modelsToRemoveList.add(dbModel3);

        DBConnection dbConnectionMock = null;

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false))
                .andReturn(dbConnectionMock);

        PowerMock.replayAll();

        boolean success = false;
        try {

            CacheManager.removeFromCache(modelsToRemoveList);

        } catch (DBConnectionException e) {

            success = true;

        }

        assertTrue(success);

        PowerMock.verifyAll();
    }

    /** Test loading with a null argument.
     *
     * @exception Exception
     */
    @Test
    public void testLoadWithNullModelName() throws Exception {

        String loadModel = null;

        PowerMock.replayAll();

        boolean success = false;

        try {

            //XMLDBModel dbModel = null;
            /*dbModel = */CacheManager.loadFromCache(loadModel);

        } catch (IllegalArgumentException e) {

            success = true;

        }

        assertTrue(success);

        PowerMock.verifyAll();
    }

    /** Test loading with a null connection.
     * @exception Exception
     */
    @Test
    public void testLoadWithNullConnection() throws Exception {

        String loadModel = "model1";

        PowerMock.mockStatic(DBConnectorFactory.class);

        DBConnection dBConnectionMock = null;

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false))
                .andReturn(dBConnectionMock);

        PowerMock.replayAll();

        boolean success = false;

        try {

            //XMLDBModel dbModel = null;
            /*dbModel =*/CacheManager.loadFromCache(loadModel);

        } catch (DBConnectionException e) {

            success = true;

        }

        assertTrue(success);

        PowerMock.verifyAll();

    }

    /** Test updating the cache with a null argument.
     * @exception Exception
     */
    @Test
    public void testUpdateCacheWithNullHashMap() throws Exception {

        PowerMock.mockStatic(DBConnectorFactory.class);

        HashMap assemblies = null;

        PowerMock.replayAll();

        boolean isSuccess = false;

        try {

            CacheManager.updateCache(assemblies);

        } catch (IllegalArgumentException e) {

            isSuccess = true;

        }
        assertTrue(isSuccess);

        PowerMock.verifyAll();
    }

    /** Test updating cache with null exception.
     * @exception Exception
     */
    @Test
    public void testUpdateCacheWithNullConnection() throws Exception {

        PowerMock.mockStatic(DBConnectorFactory.class);

        HashMap assemblies = new HashMap();

        DBConnection dbConnectionMock = null;

        EasyMock.expect(DBConnectorFactory.getCacheConnection(false))
                .andReturn(dbConnectionMock);

        PowerMock.replayAll();

        boolean isSuccess = false;

        try {

            CacheManager.updateCache(assemblies);

        } catch (DBConnectionException e) {

            isSuccess = true;

        }

        assertTrue(isSuccess);

        PowerMock.verifyAll();
    }
}
