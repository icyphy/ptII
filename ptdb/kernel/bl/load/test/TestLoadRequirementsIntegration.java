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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;

import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import ptdb.kernel.bl.load.LoadManager;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TestLoadRequirementsIntegration

/**
 * JUnit test for integration testing of the Load feature.
 *
 * <p> This test starts at the interface between the GUI layer and the business
 * layer. In this case the interface is LoadManagerInterface class. This should
 * be a pure full test with no mocked methods or classes. </p>
 *
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */
public class TestLoadRequirementsIntegration {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set up the actor module by injecting dependencies for
     *  actors like Const.  This is needed for the HandSimDroid project,
     *  see $PTII/ptserver.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        ActorModuleInitializer.initializeInjector();
    }


    /**
     * Test the loadModel() method in LoadManagerInterface class.
     * <p> Conditions for this test:
     * <br>- The model is in the database and does not have
     * references in it. </p>
     *
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModelWithoutReferences() throws Exception {

        String inputString = "CompositeActor";

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        effigy = LoadManager.loadModel(inputString, configuration);

        if (effigy == null) {
            fail("failed to return an effigy.");
        }

        // FIXME: compare against a known good output.
        //System.out.println(effigy.getModel().exportMoML());
        assertEquals(effigy.getName(), inputString);

        PowerMock.verifyAll();

    }

    /**
     * Test the loadModel() method in LoadManagerInterface class.
     * <p> Conditions for this test:
     * <br>- The model is in the database and has references in
     * it. </p>
     *
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModelWithReferences() throws Exception {

        String inputString = "newModel";

        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);

        PtolemyEffigy effigy = null;

        effigy = LoadManager.loadModel(inputString, configuration);

        if (effigy == null) {
            fail("failed to return an effigy.");
        }

        assertEquals(effigy.getName(), inputString);

        PowerMock.verifyAll();

    }

    /**
     * Test the loadModel() method in LoadManagerInterface class.
     * <p> Conditions for this test:
     * <br>- The model is not in the database. </p>
     *
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModel_NotInDatabase() throws Exception {
        String inputString = "not in database";
        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);
        try {
            LoadManager.loadModel(inputString, configuration);
        } catch (Exception e) {
            assertTrue("The system threw an exception" + e.getMessage(), true);
        }
        PowerMock.verifyAll();
    }

    /**
     * Test the loadModel() method in LoadManagerInterface class.
     * <p> Conditions for this test:
     * <br>- The name of the model is null. </p>
     *
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModel_NullModelName() throws Exception {
        String inputString = null;
        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);
        try {
            LoadManager.loadModel(inputString, configuration);

        } catch (Exception e) {
            assertTrue("The system throwed an exception" + e.getMessage(), true);
        }
        PowerMock.verifyAll();
    }

    /**
     * Test the loadModel() method in LoadManagerInterface class.
     * <p> Conditions for this test:
     * <br>- The name of the model is empty. </p>
     *
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModel_EmptyModelName() throws Exception {
        String inputString = "";
        Workspace workspace = new Workspace();
        Configuration configuration = new Configuration(workspace);
        ModelDirectory modelDirectory = new ModelDirectory(configuration,
                "directory");
        modelDirectory.setContainer(configuration);
        try {
            LoadManager.loadModel(inputString, configuration);
        } catch (Exception e) {
            assertTrue("The system throwed an exception" + e.getMessage(), true);
        }
        PowerMock.verifyAll();
    }
}
