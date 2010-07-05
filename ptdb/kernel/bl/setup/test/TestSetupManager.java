package ptdb.kernel.bl.setup.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.SetupParameters;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.setup.SetupManager;
import ptdb.kernel.database.DBConnection;
import ptdb.kernel.database.OracleXMLDBConnection;
import ptolemy.util.FileUtilities;

/**
 * JUnit test for testing the SetupManager class.
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
//// TestSetupManager

@RunWith(PowerMockRunner.class)
@PrepareForTest( { SetupManager.class, DBConnection.class,
        DBConnectorFactory.class, DBExecutionException.class,
        OracleXMLDBConnection.class })
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestSetupManager {

    //////////////////////////////////////////////////////////////////////
    ////		public methods 				      ////

    /**
     * Test the SetupManager.getSetupParameters() method. <p> The conditions for
     * this test case:<br/>
     * 
     * - The connection was done and the parameters are returned successfully.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testGetSetupParameters() throws Exception {

        SetupManager setupManager = new SetupManager();

        PowerMock.mockStatic(DBConnectorFactory.class);

        SetupParameters setupParametersMock = PowerMock
                .createMock(SetupParameters.class);

        EasyMock.expect(DBConnectorFactory.getSetupParameters()).andReturn(
                setupParametersMock);

        PowerMock.replayAll();

        SetupParameters resultSetupParameters = setupManager
                .getSetupParameters();

        if (resultSetupParameters == null) {
            fail("The setup parameters were null when they should be not null");
        } else {
            assertTrue("Setup Parameters were returned successfully.", true);
        }

        PowerMock.verifyAll();

    }

    /**
     * Test the SetupManager.getSetupParameters() method. <p> The conditions for
     * this test case:<br/>
     * 
     * - The connection was not setup. </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testGetSetupParameters_NotSetup() throws Exception {

        SetupManager setupManager = new SetupManager();

        PowerMock.mockStatic(DBConnectorFactory.class);

        EasyMock.expect(DBConnectorFactory.getSetupParameters())
                .andReturn(null);

        PowerMock.replayAll();

        SetupParameters resultSetupParameters = setupManager
                .getSetupParameters();

        if (resultSetupParameters != null) {
            fail("The setup parameters were not null when they should be.");
        } else {
            assertTrue("Setup Parameters were null as expected.", true);
        }

        PowerMock.verifyAll();

    }

    /**
     * Test the SetupManager.testConnection() method. <p> The conditions for
     * this test case:<br/>
     * 
     * - The setup parameters are correct and the test should be successful.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testTestConnection() throws Exception {

        
        
        SetupManager setupManager = new SetupManager();

        SetupParameters setupParam = setupManager.getSetupParameters();
        
        
        
        try {
            DBConnectorFactory.loadDBProperties();

            setupManager.testConnection(setupParam);
        } catch (DBConnectionException e) {

            fail("The method failed to test the connection" + e.getMessage());
        }

        assertTrue("The method was successful in testing the connection.", true);
    }

    /**
     * Test the SetupManager.testConnection() method. <p> The conditions for
     * this test case:<br/>
     * 
     * - The setup parameters are incorrect and the test should throw an
     * exception. </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testTestConnection_IncorrectParam() throws Exception {

        SetupParameters setupParam = new SetupParameters("D:/dbxml",
                "temp1.dbxml", "temp_cache.dbxml");

        SetupManager setupManager = new SetupManager();

        boolean isSuccessful = false;

        try {
            DBConnectorFactory.loadDBProperties();    

            setupManager.testConnection(setupParam);
        } catch (DBConnectionException e) {

            isSuccessful = true;
            assertTrue("An exception was thrown as expected", true);
        }

        if (!isSuccessful) {
            fail("The method did not throw an exception.");
        }

    }

    /**
     * Test the SetupManager.testConnection() method. <p> The conditions for
     * this test case:<br/>
     * 
     * - The setup parameters is null. </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testTestConnection_NullSetupParam() throws Exception {

        SetupParameters setupParam = null;

        SetupManager setupManager = new SetupManager();

        boolean isSuccessful = false;

        try {

            DBConnectorFactory.loadDBProperties();

            setupManager.testConnection(setupParam);
        } catch (DBConnectionException e) {

            isSuccessful = true;
            assertTrue("An exception was thrown as expected", true);
        }

        if (!isSuccessful) {
            fail("The method did not throw an exception.");
        }
    }

    /**
     * Test the SetupManager.updateConnection() method. <p> The conditions for
     * this test case:<br/>
     * 
     * - The setup parameters are correct. </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testUpdateConnection() throws Exception {

        SetupParameters setupParam = new SetupParameters("D:/dbxml",
                "temp.dbxml", "temp_cache.dbxml");

        SetupManager setupManager = new SetupManager();

        SetupParameters oldParam = setupManager.getSetupParameters();
        
        
        try {
            DBConnectorFactory.loadDBProperties();

            setupManager.updateDBConnectionSetupParameters(setupParam);
            assertTrue("Completed the update.", true);
            
            
            setupManager.updateDBConnectionSetupParameters(oldParam);
            
        } catch (DBConnectionException e) {

            fail("An exception was thrown" + e.getMessage());
        }

        
        
        
        
    }

    /**
     * Test the SetupManager.updateConnection() method. <p> The conditions for
     * this test case:<br/>
     * 
     * - The setup parameters are null. </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testUpdateConnection_NullParam() throws Exception {

        SetupParameters setupParam = null;

        SetupManager setupManager = new SetupManager();

        SetupParameters oldParam = setupManager.getSetupParameters();
        
        boolean isSuccessful = false;

        try {
            setupManager.updateDBConnectionSetupParameters(setupParam);
        } catch (DBConnectionException e) {

            isSuccessful = true;
            assertTrue("An exception was thrown as expected", true);
        }

        if (!isSuccessful) {
            fail("Updated completed without throwing an exception when it should.");
            setupManager.updateDBConnectionSetupParameters(oldParam);
            
        }
    }
}
