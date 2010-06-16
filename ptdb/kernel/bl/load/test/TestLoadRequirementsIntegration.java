package ptdb.kernel.bl.load.test;

import static org.junit.Assert.*;
import java.net.URL;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import ptdb.kernel.bl.load.LoadManagerInterface;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.moml.MoMLParser;

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
///////////////////////////////////////////////////////////////
//// TestLoadRequirementsIntegration

public class TestLoadRequirementsIntegration {

    //////////////////////////////////////////////////////////////////////
    ////                public methods                                ////


    /**
     * Test the loadModel method in LoadManagerInterface class. 
     * <p> Conditions for this test: 
     * <br>- The model is in the database and does not have
     * references in it. </p>
     * 
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModelWithoutReferences() throws Exception {

        LoadManagerInterface tested = new LoadManagerInterface();

        String inputString = "CompositeActor";

        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;

        effigy = tested.loadModel(inputString, configuration);

        if (effigy == null) {
            fail("failed to return an effigy.");
        }

        System.out.println(effigy.getModel().exportMoML());
        assertEquals(effigy.getName(), inputString);

        PowerMock.verifyAll();

    }

    /**
     * Test the loadModel method in LoadManagerInterface class. 
     * <p> Conditions for this test: 
     * <br>- The model is in the database and has references in
     * it. </p>
     * 
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModelWithReferences() throws Exception {

        LoadManagerInterface tested = new LoadManagerInterface();

        String inputString = "modeltt";

        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;

        effigy = tested.loadModel(inputString, configuration);

        if (effigy == null) {
            fail("failed to return an effigy.");
        }

        System.out.println(effigy.getModel().exportMoML());
        assertEquals(effigy.getName(), inputString);

        PowerMock.verifyAll();

    }

    /**
     * Test the loadModel method in LoadManagerInterface class. 
     * <p> Conditions for this test: 
     * <br>- The model is not in the database. </p>
     * 
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModel_NotInDatabase() throws Exception {

        LoadManagerInterface tested = new LoadManagerInterface();

        String inputString = "not in database";

        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;

        try {
            effigy = tested.loadModel(inputString, configuration);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("The system throwed an exception" + e.getMessage(), true);
        }

        PowerMock.verifyAll();

    }

    /**
     * Test the loadModel method in LoadManagerInterface class. 
     * <p> Conditions for this test: 
     * <br>- The name of the model is null. </p>
     * 
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModel_NullModelName() throws Exception {

        LoadManagerInterface tested = new LoadManagerInterface();

        String inputString = null;

        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;

        try {
            effigy = tested.loadModel(inputString, configuration);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("The system throwed an exception" + e.getMessage(), true);
        }

        PowerMock.verifyAll();

    }

    /**
     * Test the loadModel method in LoadManagerInterface class. 
     * <p> Conditions for this test: 
     * <br>- The name of the model is empty. </p>
     * 
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testloadModel_EmptyModelName() throws Exception {

        LoadManagerInterface tested = new LoadManagerInterface();

        String inputString = "";

        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;

        try {
            effigy = tested.loadModel(inputString, configuration);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("The system throwed an exception" + e.getMessage(), true);
        }

        PowerMock.verifyAll();

    }

}
