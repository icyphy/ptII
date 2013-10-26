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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.load.LoadManager;
import ptdb.kernel.bl.save.SaveModelManager;
import ptdb.kernel.database.CacheManager;
import ptdb.kernel.database.DBConnection;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// TestCacheRequirementsIntegration

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

public class TestCacheRequirementsIntegration {

    /**
     * Verify that models in the cache can be updated.
     *
     * @exception Exception
     */
    @Test
    public void testUpdateCache() throws Exception {

        java.util.Date time = new java.util.Date();
        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime())
                + "model");
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
                + "<property name=\"TestAttribute\" class=\"ptolemy.data.expr.StringParameter\" value=\"OLD\">"
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

        SaveModelManager saveManager = new SaveModelManager();

        /*String modelID =*/saveManager.save(dbModel);

        // Load the model to place it into cache.
        PtolemyEffigy effigy = loadModel(dbModel.getModelName());

        // Change the value of the attribute for the copy that will go in the cache.
        ((StringParameter) effigy.getModel().getAttribute("TestAttribute"))
                .setExpression("NEW");
        changeModel(effigy.getModel());

        // Create an assembly and update the cache.
        HashMap assemblies = new HashMap();
        assemblies.put(effigy.getModel().getName(), effigy.getModel()
                .exportMoML());
        CacheManager.updateCache(assemblies);

        // Now load the model from the cache.
        effigy = null;
        effigy = loadModel(dbModel.getModelName());

        // Verify that the cache was actually updated.
        //  Verify that the model is loaded from the cache.
        // The Attribute value will be "NEW".
        assertEquals(
                ((StringParameter) effigy.getModel().getAttribute(
                        "TestAttribute")).getExpression(), "NEW");

        // Save the model again.  This should clear it from the cache.
        dbModel.setIsNew(false);
        /*modelID =*/saveManager.save(dbModel);

        // Now load the model again.  This time it will be from the DB (not the cache).
        effigy = null;
        effigy = loadModel(dbModel.getModelName());

        // Verify it it is loaded from the DB.
        // Attribute value will be "OLD".
        assertEquals(
                ((StringParameter) effigy.getModel().getAttribute(
                        "TestAttribute")).getExpression(), "OLD");

        // Clean up.
        ArrayList<XMLDBModel> modelsToRemove = new ArrayList();
        modelsToRemove.add(new XMLDBModel(dbModel.getModelName()));
        CacheManager.removeFromCache(modelsToRemove);
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

    private PtolemyEffigy loadModel(String modelName) throws Exception {

        // MoMLParser parser = new MoMLParser();
        //parser.resetAll();
        //String configPath = "ptolemy/configs/ptdb/configuration.xml";

        //URL configURL = ConfigurationApplication.specToURL(configPath);
        //Configuration configuration = (Configuration) parser.parse(configURL,
        //       configURL);

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        effigy = LoadManager.loadModel(modelName, configuration);

        return effigy;
    }

    private void changeModel(NamedObj model) throws Exception {

        try {

            MoMLChangeRequest change = new MoMLChangeRequest(this, null,
                    model.exportMoML());
            change.setUndoable(true);

            model.requestChange(change);

        } catch (Exception e) {
            throw e;
        }
    }
}
