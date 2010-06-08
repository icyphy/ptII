/*
 * 
 */
package ptdb.kernel.database.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sleepycat.dbxml.XmlException;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.exception.DBConnectionException;
import ptdb.kernel.database.OracleXMLDBConnection;

///////////////////////////////////////////////////////////////
//// TestOracleXMLDBConnection

/**
 * Unit Test the OracleXMLDBConnection class 
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class TestOracleXMLDBConnection {

    //////////////////////////////////////////////////////////////////////
    ////		public variables 			   //////

    //////////////////////////////////////////////////////////////////////
    ////		public methods 			           //////
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link ptdb.kernel.database.OracleXMLDBConnection#OracleXMLDBConnection(ptdb.common.dto.DBConnectionParameters)}.
     */
    @Test
    public void testOracleXMLDBConnection() {

        //////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Correct url, container name and transaction required = true
         */
        String url = "c:\\users\\wini";
        String containerName = "temp.dbxml";
        boolean isTransactionRequired = true;

        DBConnectionParameters dbConnParams = new DBConnectionParameters(url,
                containerName, isTransactionRequired);
        try {
            OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

            assertTrue("Test1 - _xmlManager not initialized", conn.toString()
                    .contains("_xmlManager:Initialized"));

            assertTrue(
                    "Test1 - _xmlTransaction not initialized when transaction required was true",
                    conn.toString().contains("_xmlTransaction:Initialized"));

            conn.closeConnection();
        } catch (DBConnectionException e) {

            fail("Test 1 - " + e.getMessage());
        }

        //////////////////////////////////////////////////////////////////////////////////////////        
        /**
         * Correct url, container name and transaction required = false
         */
        url = "c:\\users\\wini";
        containerName = "temp.dbxml";
        isTransactionRequired = false;

        dbConnParams = new DBConnectionParameters(url, containerName,
                isTransactionRequired);

        try {
            OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

            assertTrue("Test2 - _xmlManager not initialized", conn.toString()
                    .contains("_xmlManager:Initialized"));

            assertTrue(
                    "Test2 - _xmlTransaction initialized when transaction required was false",
                    conn.toString().contains("_xmlTransaction:Not Initialized"));

            conn.closeConnection();
        } catch (DBConnectionException e) {

            fail("Test 2 - " + e.getMessage());
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Incorrect url, correct container name and transaction required = true
         */
        url = "c:\\users\\error";
        containerName = "temp.dbxml";
        isTransactionRequired = true;

        dbConnParams = new DBConnectionParameters(url, containerName,
                isTransactionRequired);

        try {
            OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

            fail("Test 3 - No exception thrown when there was an error in path");

            conn.closeConnection();
        } catch (DBConnectionException e) {

            assertTrue("Test 3 - Incorrect exception thrown -"
                    + e.getClass().getName(),
                    e.getCause() instanceof FileNotFoundException);
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Correct URL, incorrect container name and transaction required = true
         */
        url = "c:\\users\\wini";
        containerName = "nosuchcontainer.dbxml";
        isTransactionRequired = true;

        dbConnParams = new DBConnectionParameters(url, containerName,
                isTransactionRequired);

        try {
            OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

            fail("Test 4 - No exception thrown when there was an error in path");

            conn.closeConnection();
        } catch (DBConnectionException e) {

            assertTrue("Test 4 - Incorrect exception thrown - "
                    + e.getClass().getName(),
                    e.getCause() instanceof XmlException);
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Incorrect url, container name and transaction required = true
         */
        url = "c:\\users\\error";
        containerName = "nosuchcontainer.dbxml";
        isTransactionRequired = true;

        dbConnParams = new DBConnectionParameters(url, containerName,
                isTransactionRequired);

        try {
            OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

            fail("Test 5 - No exception thrown when there was an error in path");

            conn.closeConnection();
        } catch (DBConnectionException e) {

            assertTrue("Test 5 - Incorrect exception thrown - "
                    + e.getClass().getName(),
                    e.getCause() instanceof FileNotFoundException);
        }

    }

    /**
     * Test method for {@link ptdb.kernel.database.OracleXMLDBConnection#abortConnection()}.
     */
    @Test
    public void testAbortConnection() {

        OracleXMLDBConnection conn = null;

        try {
            conn = createConnWithoutTransaction();
        } catch (DBConnectionException e1) {
            fail("Faile while creting a connection without transaction");
        }

        try {
            conn.abortConnection();
            
        } catch (DBConnectionException e) {
            fail("Test 1 - Exception while aborting an open connection without transaction");
        }

        try {
            conn = createConnWithTransaction();
        } catch (DBConnectionException e1) {
            fail("Faile while creting a connection without transaction");
        }

        try {
            conn.abortConnection();
            
        } catch (DBConnectionException e) {
            fail("Test 2 - Exception while aborting an open connection without transaction");
        }
        
        try {
            conn.abortConnection();
            fail("Test 3 - Failed to throw an exception while aborting an already aborted connection");
        } catch (DBConnectionException e) {
            
        }
    }

    /**
     * Test method for {@link ptdb.kernel.database.OracleXMLDBConnection#closeConnection()}.
     */
    @Test
    public void testCloseConnection() {
        OracleXMLDBConnection conn = null;

        try {
            conn = createConnWithoutTransaction();
        } catch (DBConnectionException e1) {
            fail("Faile while creating a connection without transaction");
        }

        try {
            conn.closeConnection();
            
        } catch (DBConnectionException e) {
            fail("Test 1 - Exception while closing an open connection without transaction");
        }

        try {
            conn = createConnWithTransaction();
        } catch (DBConnectionException e1) {
            fail("Faile while creating a connection without transaction");
        }

        try {
            conn.closeConnection();
            
        } catch (DBConnectionException e) {
            fail("Test 2 - Exception while closing an open connection without transaction");
        }
        
        try {
            conn.closeConnection();
            fail("Test 3 - Failed to throw an exception while closing an already closed connection");
        } catch (DBConnectionException e) {
            
        }
    }

    /**
     * Test method for {@link ptdb.kernel.database.OracleXMLDBConnection#executeGetAttributesTask(ptdb.common.dto.GetAttributesTask)}.
     */
    @Test
    public void testExecuteGetAttributesTask() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link ptdb.kernel.database.OracleXMLDBConnection#executeGetModelsTask(ptdb.common.dto.GetModelsTask)}.
     */
    @Test
    public void testExecuteGetModelsTask() {
        //fail("Not yet implemented");
    }

    //////////////////////////////////////////////////////////////////////
    ////		protected methods 				//////

    //////////////////////////////////////////////////////////////////////
    ////		protected variables 				//////

    //////////////////////////////////////////////////////////////////////
    ////		private methods 				//////

    private OracleXMLDBConnection createConnWithoutTransaction()
            throws DBConnectionException {
        return createConn(false);
    }

    private OracleXMLDBConnection createConnWithTransaction()
            throws DBConnectionException {
        return createConn(true);
    }

    private OracleXMLDBConnection createConn(boolean tranReqd)
            throws DBConnectionException {
        String url = "c:\\users\\wini";
        String containerName = "temp.dbxml";
        boolean isTransactionRequired = tranReqd;

        DBConnectionParameters dbConnParams = new DBConnectionParameters(url,
                containerName, isTransactionRequired);
        dbConnParams.setUrl(url);
        dbConnParams.setContainerName(containerName);
        dbConnParams.setIsTransactionRequired(isTransactionRequired);
        
        OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);
        return conn;
    }

    //////////////////////////////////////////////////////////////////////
    ////		private variables				//////

}
