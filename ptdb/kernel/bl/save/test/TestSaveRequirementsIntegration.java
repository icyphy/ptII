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
package ptdb.kernel.bl.save.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.load.DBModelFetcher;
import ptdb.kernel.bl.load.LoadManager;
import ptdb.kernel.bl.save.SaveModelManager;
import ptdb.kernel.database.DBConnection;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////
//// TestSaveRequirementsIntegration

/**
 * JUnit test for integration testing of the Save feature.
 *
 *
 *
 * @author Lyle Holsinger
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */

public class TestSaveRequirementsIntegration {

    /** Set up the actor module by injecting dependencies for
     *  actors like Const.  This is needed for the HandSimDroid project,
     *  see $PTII/ptserver.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        ActorModuleInitializer.initializeInjector();
    }


    @Test
    public void testNullModel() throws Exception {

        XMLDBModel dbModel = null;

        boolean exceptionThrown = false;

        SaveModelManager saveModelManager = new SaveModelManager();

        try {

            saveModelManager.save(dbModel);

        } catch (IllegalArgumentException e) {

            exceptionThrown = true;

        }

        assertTrue(exceptionThrown);

    }

    @Test
    public void testSavingNewModel() throws Exception {

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        SaveModelManager saveModelManager = new SaveModelManager();

        String id = saveModelManager.save(dbModel);
        assertNotNull(id);

        effigy = LoadManager.loadModel(dbModel.getModelName(), configuration);

        boolean equal = effigy.getModel().getName()
                .equals(dbModel.getModelName());

        assertTrue(equal);

        removeModel(new XMLDBModel(dbModel.getModelName()));

    }

    @Test
    public void testUpdatingModel() throws Exception {

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        SaveModelManager saveModelManager = new SaveModelManager();

        saveModelManager.save(dbModel);

        // We know that the model is already in the DB,
        // so test overwriting it.
        dbModel.setIsNew(false);

        String id = saveModelManager.save(dbModel);
        assertNotNull(id);

        effigy = LoadManager.loadModel(dbModel.getModelName(), configuration);

        boolean equal = effigy.getModel().getName()
                .equals(dbModel.getModelName());

        assertTrue(equal);

        removeModel(new XMLDBModel(dbModel.getModelName()));

    }

    @Test
    public void testExceptions() throws Exception {

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        SaveModelManager saveModelManager = new SaveModelManager();

        boolean exceptionThrown = false;
        String id = null;

        try {

            id = saveModelManager.save(dbModel);

        } catch (DBExecutionException e) {

            exceptionThrown = true;

        }

        assertTrue(exceptionThrown);
        assertTrue(id == null);

        exceptionThrown = false;
        id = null;

        dbModel.setIsNew(true);
        id = saveModelManager.save(dbModel);
        assertNotNull(id);
        id = null;

        try {

            id = saveModelManager.save(dbModel);

        } catch (ModelAlreadyExistException e) {

            exceptionThrown = true;

        }

        assertTrue(exceptionThrown);
        assertTrue(id == null);

        effigy = LoadManager.loadModel(dbModel.getModelName(), configuration);

        boolean equal = (effigy != null);

        assertTrue(equal);

        removeModel(new XMLDBModel(dbModel.getModelName()));

    }

    private void removeModel(XMLDBModel dbModel) throws Exception {

        DBConnection dbConnection = null;

        try {

            ArrayList<XMLDBModel> modelList = new ArrayList();
            modelList.add(dbModel);
            dbConnection = DBConnectorFactory.getSyncConnection(true);
            RemoveModelsTask removeModelsTask = new RemoveModelsTask(modelList);
            dbConnection.executeRemoveModelsTask(removeModelsTask);
            dbConnection.commitConnection();

        } catch (DBExecutionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;

        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();

            }
        }

    }

    @Test
    public void testRenameModel() {
        SaveModelManager saveModelManager = new SaveModelManager();
        XMLDBModel originalModel = new XMLDBModel("Adder");
        try {
            saveModelManager.renameModel(originalModel, "Y");
            fail("Model already exists exception not thrown.");
        } catch (ModelAlreadyExistException e) {

        } catch (Exception e) {
            fail("Model already exists exception not thrown - "
                    + e.getMessage());
        }

        try {
            saveModelManager.renameModel(new XMLDBModel(null), "Y");
            fail("Illegal argument exception not thrown.");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Illegal argument exception not thrown - " + e.getMessage());
        }

        try {
            saveModelManager.renameModel(originalModel, "");
            fail("Illegal argument exception not thrown.");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Illegal argument exception not thrown - " + e.getMessage());
        }

        try {
            saveModelManager.renameModel(originalModel, null);
            fail("Illegal argument exception not thrown.");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Illegal argument exception not thrown - " + e.getMessage());
        }

        try {
            saveModelManager.renameModel(originalModel, "TestRenameModel");
            try {
                DBModelFetcher.load("Adder");
                fail("Test failed. Model(Adder) still in the database");
            } catch (DBExecutionException e) {

            }

            try {
                DBModelFetcher.load("TestRenameModel");
            } catch (DBExecutionException e) {
                fail("Test failed. Model (TestRenameModel) not found in the database."
                        + e.getMessage());
            }
        } catch (Exception e) {
            fail("Failed with exception - " + e.getMessage());
        }
    }

    @Test
    public void testResetModels() {
        SaveModelManager saveModelManager = new SaveModelManager();
        XMLDBModel originalModel = new XMLDBModel("TestRenameModel");
        System.out.println("Called----");
        // Reset the name back to original.
        try {
            saveModelManager.renameModel(originalModel, "Adder");
        } catch (Exception e) {
            fail("Failed with exception - " + e.getMessage());
        }
    }
}
