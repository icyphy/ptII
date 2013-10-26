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
package ptdb.kernel.bl.load.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.GetReferenceStringTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.CircularDependencyException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.load.DBModelFetcher;
import ptdb.kernel.bl.load.LoadManager;
import ptdb.kernel.database.DBConnection;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.StringConstantParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TestLoadManager

/**
* This JUnit tests the translation of an XMLDBModel object into an effigy.
* It mocks the LoadModelManager.  An XMLDBModel object is created.  The
* loadModel method is called and then we confirm that the model name
* in the returned effigy is as expected: "model1".
*
* @author Lyle Holsinger
* @since Ptolemy II 10.0
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*/

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DBModelFetcher.class, LoadManager.class,
        DBConnectorFactory.class, Utilities.class })
@SuppressStaticInitializationFor("ptdb.kernel.bl.load.DBModelFetcher")
public class TestLoadManager {

    /** Set up the actor module by injecting dependencies for
     *  actors like Const.  This is needed for the HandSimDroid project,
     *  see $PTII/ptserver.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        ActorModuleInitializer.initializeInjector();
    }

    /**
     * Mock the LoadModelManager.  Create a fake XMLDBModel object and specify
     * it as the return from load().  Then verify that, given an XMLDBModelObject,
     * the loadModel() method will return a valid effigy.
     * @exception Exception
     */
    @Test
    public void testloadModel() throws Exception {

        String inputString = "model1";

        //MoMLParser parser = new MoMLParser();
        //parser.reset();
        //String configPath = "ptolemy/configs/ptdb/configuration.xml";

        //URL configURL = ConfigurationApplication.specToURL(configPath);
        //Configuration configuration = (Configuration) parser.parse(configURL,
        //        configURL);

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        // Use a different model name to
        // demonstrate that the name is taken from the MoML.
        XMLDBModel dbModel = new XMLDBModel("model2");
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"model1\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        //Mock the LoadModelManager class and assume that load() returns the XMLDBModel we've created.

        PowerMock.mockStatic(DBModelFetcher.class);
        EasyMock.expect(DBModelFetcher.load(inputString)).andReturn(dbModel);

        //Execute the test.  Verify that, given an XMLDBModel object, we can get a valid effigy.
        PowerMock.replayAll();
        effigy = LoadManager.loadModel(inputString, configuration);
        assertEquals(effigy.getModel().getName(), "model1");
        PowerMock.verifyAll();

    }

    /**
     * Mock the LoadModelManager.  Create a fake XMLDBModel object and specify
     * it as the return from load().  Then verify that, given an XMLDBModelObject,
     * the loadModel() method will return a valid effigy.
     * @exception Exception
     */
    @Test
    public void testLoadModelUsingId() throws Exception {

        String inputString = "model1";

        //MoMLParser parser = new MoMLParser();
        //parser.reset();
        //String configPath = "ptolemy/configs/ptdb/configuration.xml";

        //URL configURL = ConfigurationApplication.specToURL(configPath);
        //Configuration configuration = (Configuration) parser.parse(configURL,
        //        configURL);

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        // Use a different model name to
        // demonstrate that the name is taken from the MoML.
        XMLDBModel dbModel = new XMLDBModel("model2");
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"model1\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        //Mock the LoadModelManager class and assume that load() returns the XMLDBModel we've created.

        PowerMock.mockStatic(DBModelFetcher.class);
        EasyMock.expect(DBModelFetcher.loadUsingId(inputString)).andReturn(
                dbModel);

        //Execute the test.  Verify that, given an XMLDBModel object, we can get a valid effigy.
        PowerMock.replayAll();
        effigy = LoadManager.loadModelUsingId(inputString, configuration);
        assertEquals(effigy.getModel().getName(), "model1");
        PowerMock.verifyAll();

    }

    /**
     * Test importing a model by reference.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByRefNoTag() throws Exception {

        Entity container = new Entity("container");

        String inputString = "model1";

        Entity entity = null;

        // Use a different model name to
        // demonstrate that the name is taken from the MoML.
        XMLDBModel dbModel = new XMLDBModel("model2");
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"modelName\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        //Mock the LoadModelManager class and assume that load() returns the XMLDBModel we've created.

        PowerMock.mockStatic(DBModelFetcher.class);
        EasyMock.expect(DBModelFetcher.load(inputString)).andReturn(dbModel);

        //Execute the test.  Verify that, given an XMLDBModel object, we can get a valid effigy.
        PowerMock.replayAll();
        entity = LoadManager.importModel(inputString, true, container);
        assertEquals(entity.getName(), "modelName");

        assertEquals(
                ((StringConstantParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), "TRUE");
        PowerMock.verifyAll();

    }

    /**
     * Test importing a model by value.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByRefWithTag() throws Exception {

        Entity container = new Entity("container");

        String inputString = "model1";

        Entity entity = null;

        // Use a different model name to
        // demonstrate that the name is taken from the MoML.
        XMLDBModel dbModel = new XMLDBModel("model2");
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"modelName\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>" + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\"></property>"
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

        //Mock the LoadModelManager class and assume that load() returns the XMLDBModel we've created.

        PowerMock.mockStatic(DBModelFetcher.class);
        EasyMock.expect(DBModelFetcher.load(inputString)).andReturn(dbModel);

        //Execute the test.  Verify that, given an XMLDBModel object, we can get a valid effigy.
        PowerMock.replayAll();
        entity = LoadManager.importModel(inputString, true, container);
        assertEquals(entity.getName(), "modelName");

        assertEquals(
                ((StringConstantParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), "TRUE");
        PowerMock.verifyAll();

    }

    /**
     * Test importing a model by reference.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByValueNoTag() throws Exception {

        Entity container = new Entity("container");

        String inputString = "model1";

        Entity entity = null;

        // Use a different model name to
        // demonstrate that the name is taken from the MoML.
        XMLDBModel dbModel = new XMLDBModel("model2");
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"modelName\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        //Mock the LoadModelManager class and assume that load() returns the XMLDBModel we've created.

        PowerMock.mockStatic(DBModelFetcher.class);
        EasyMock.expect(DBModelFetcher.load(inputString)).andReturn(dbModel);

        //Execute the test.  Verify that, given an XMLDBModel object, we can get a valid effigy.
        PowerMock.replayAll();
        entity = LoadManager.importModel(inputString, false, container);
        assertEquals(entity.getName(), "modelName");

        assertEquals(
                ((StringConstantParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), "FALSE");
        PowerMock.verifyAll();

    }

    /**
     * Test importing a model by reference.  No DBReference tag is present.
     * @exception Exception
     */
    @Test
    public void testImportModelByValueWithTag() throws Exception {

        Entity container = new Entity("container");

        String inputString = "model1";

        Entity entity = null;

        // Use a different model name to
        // demonstrate that the name is taken from the MoML.
        XMLDBModel dbModel = new XMLDBModel("model2");
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\"modelName\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>" + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\"></property>"
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

        //Mock the LoadModelManager class and assume that load() returns the XMLDBModel we've created.

        PowerMock.mockStatic(DBModelFetcher.class);
        EasyMock.expect(DBModelFetcher.load(inputString)).andReturn(dbModel);

        //Execute the test.  Verify that, given an XMLDBModel object, we can get a valid effigy.
        PowerMock.replayAll();
        entity = LoadManager.importModel(inputString, false, container);
        assertEquals(entity.getName(), "modelName");

        assertEquals(
                ((StringConstantParameter) entity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), "FALSE");
        PowerMock.verifyAll();

    }

    /**
     * Test attempting to import a model with a null model name.
     * @exception Exception
     */
    @Test
    public void testNull() throws Exception {

        Entity container = new Entity("container");

        String inputString = null;

        //Entity entity = null;

        PowerMock.replayAll();

        boolean isSuccess = false;

        try {

            /*entity =*/LoadManager.importModel(inputString, false, container);

        } catch (Exception e) {

            isSuccess = true;

        }

        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    /**
     * Test importing a model with a circular dependency present.
     * @exception Exception
     */
    @Test
    public void testCircularDepencency() throws Exception {

        String containerModel = "container";
        String importModel = "model1";
        Entity container = new Entity(containerModel);
        String referenceString = "<reference string>";

        XMLDBModel dbModel = new XMLDBModel(importModel);
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + importModel
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\"></property>"
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

        PowerMock.mockStatic(DBConnectorFactory.class);
        PowerMock.mockStatic(DBModelFetcher.class);
        PowerMock.mockStatic(Utilities.class);

        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);
        GetReferenceStringTask getReferenceStringTask = PowerMock
                .createMock(GetReferenceStringTask.class);

        EasyMock.expect(DBModelFetcher.load(importModel)).andReturn(dbModel);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dBConnectionMock);

        PowerMock.expectNew(GetReferenceStringTask.class, importModel)
                .andReturn(getReferenceStringTask);

        EasyMock.expect(
                dBConnectionMock
                        .executeGetReferenceStringTask(getReferenceStringTask))
                .andReturn(referenceString);

        EasyMock.expect(
                Utilities.modelReferenceExists(containerModel, referenceString))
                .andReturn(true);

        dBConnectionMock.closeConnection();

        PowerMock.replayAll();

        boolean exceptionThrown = false;

        try {

            /*Entity modelWithImport = */
            LoadManager.importModel(importModel, true, container);

        } catch (CircularDependencyException e) {

            exceptionThrown = true;

        }

        assertTrue(exceptionThrown);

        PowerMock.verifyAll();
    }

    /**
     * Test importing a model with no circular dependency present.
     * @exception Exception
     */
    @Test
    public void testNoCircularDepencency() throws Exception {

        String containerModel = "container";
        String importModel = "model1";
        Entity container = new Entity(containerModel);
        String referenceString = "<reference string>";

        XMLDBModel dbModel = new XMLDBModel(importModel);
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + importModel
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\"></property>"
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

        PowerMock.mockStatic(DBConnectorFactory.class);
        PowerMock.mockStatic(DBModelFetcher.class);
        PowerMock.mockStatic(Utilities.class);

        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);
        GetReferenceStringTask getReferenceStringTask = PowerMock
                .createMock(GetReferenceStringTask.class);

        EasyMock.expect(DBModelFetcher.load(importModel)).andReturn(dbModel);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(false)).andReturn(
                dBConnectionMock);

        PowerMock.expectNew(GetReferenceStringTask.class, importModel)
                .andReturn(getReferenceStringTask);

        EasyMock.expect(
                dBConnectionMock
                        .executeGetReferenceStringTask(getReferenceStringTask))
                .andReturn(referenceString);

        EasyMock.expect(
                Utilities.modelReferenceExists(containerModel, referenceString))
                .andReturn(false);

        dBConnectionMock.closeConnection();

        PowerMock.replayAll();

        boolean exceptionThrown = false;
        Entity modelWithImport = new Entity();

        try {

            modelWithImport = LoadManager.importModel(importModel, true,
                    container);

        } catch (CircularDependencyException e) {

            exceptionThrown = true;

        }

        assertFalse(exceptionThrown);

        assertEquals(
                ((StringConstantParameter) modelWithImport.getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .getExpression(), "TRUE");

        PowerMock.verifyAll();
    }
}
