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
package ptdb.common.util.test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.SetupParameters;
import ptdb.common.dto.TaskQueue;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.AsynchronousDBConnection;
import ptdb.kernel.database.DBConnection;
import ptdb.kernel.database.ExecutorThread;
import ptdb.kernel.database.OracleXMLDBConnection;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////
//// TestDBConnectorFactory

/**
 * Unit tests for DBConnectorFactory.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
@PrepareForTest({ DBConnectorFactory.class, TestDBConnectorFactory.class,
        FileUtilities.class, DBConnectionParameters.class })
@RunWith(PowerMockRunner.class)
public class TestDBConnectorFactory {

    String systemPath = System.getProperty("user.dir");
    String ptIIPath = StringUtilities.getProperty("ptolemy.ptII.dir");

    String fileSeparator = System.getProperty("file.separator");
    String _testPath;

    /**
     * @exception java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @exception java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @exception java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        _testPath = ptIIPath + fileSeparator + "ptdb" + fileSeparator
                + "kernel" + fileSeparator + "database" + fileSeparator
                + "test" + fileSeparator;
    }

    /**
     * @exception java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link ptdb.common.util.DBConnectorFactory#loadDBProperties()}.
     * @exception IOException
     */
    @Test
    public void testLoadDBProperties() throws IOException {
        String ptdbParamsURL = "$CLASSPATH/ptdb/config/ptdb-params.properties";
        // All Correct values
        DBConnectorFactory.loadDBProperties();
        String parameters = DBConnectorFactory.getParametersString();
        assertTrue(
                "DB ClassName is incorrect.",
                parameters
                        .contains("_dbClassName = ptdb.kernel.database.OracleXMLDBConnection"));
        assertTrue("DB URL is incorrect.",
                parameters.contains("_dbUrl = " + _getDatabasePath()));
        assertTrue("DB Container Name is incorrect.",
                parameters.contains("_dbContainerName = Testing.dbxml"));
        assertTrue("Cache Container Name is incorrect.",
                parameters
                        .contains("_cacheContainerName = Testing_cache.dbxml"));

        //////////////////////////////////////////////////////////////////////////////////////////
        //Incorrect URL
        boolean isSetupDone = DBConnectorFactory.isSetupDone();
        try {

            URL urlInvalid = ClassLoader.getSystemResource("invalid_resource");
            mockStatic(FileUtilities.class);
            expect(
                    FileUtilities.nameToURL(
                            "$CLASSPATH/ptdb/config/ptdb-params.properties",
                            null, null)).andReturn(urlInvalid);
            PowerMock.replay(FileUtilities.class);

            DBConnectorFactory.loadDBProperties();

            PowerMock.verifyAll();
            fail("No error reported");

        } catch (ExceptionInInitializerError e) {
            assertTrue("Set up setting has unchanged",
                    DBConnectorFactory.isSetupDone() == isSetupDone);
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //IO Exception
        isSetupDone = DBConnectorFactory.isSetupDone();
        try {

            URL url = PowerMock.createMock(URL.class);
            mockStatic(FileUtilities.class);
            expect(FileUtilities.nameToURL(ptdbParamsURL, null, null))
                    .andReturn(url);
            expect(url.openStream()).andThrow(new IOException());
            PowerMock.replay(url, FileUtilities.class);

            DBConnectorFactory.loadDBProperties();

            PowerMock.verify(url, FileUtilities.class);
            fail("No error reported");

        } catch (ExceptionInInitializerError e) {
            assertTrue("Set up setting has unchanged",
                    DBConnectorFactory.isSetupDone() == isSetupDone);
        } catch (IOException e) {
            fail("Wrong error reported");
            e.printStackTrace();
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //URL is null
        try {

            URL url = PowerMock.createMock(URL.class);
            InputStream is = new FileInputStream(_testPath
                    + "ptdb-test1.properties");

            mockStatic(FileUtilities.class);
            expect(FileUtilities.nameToURL(ptdbParamsURL, null, null))
                    .andReturn(url);
            expect(url.openStream()).andReturn(is);
            PowerMock.replay(url, FileUtilities.class);

            DBConnectorFactory.loadDBProperties();
            parameters = DBConnectorFactory.getParametersString();

            PowerMock.verify(url, FileUtilities.class);

            assertTrue(
                    "DB ClassName is incorrect.",
                    parameters
                            .contains("_dbClassName = ptdb.kernel.database.OracleXMLDBConnection"));
            assertTrue("DB URL is incorrect.",
                    parameters.contains("_dbUrl = null;"));
            assertTrue("DB Container Name is incorrect.",
                    parameters.contains("_dbContainerName = null"));
            assertTrue("Cache Container Name is incorrect.",
                    parameters.contains("_cacheContainerName = null"));
            assertTrue("Set up cannot be completed",
                    parameters.contains("_isDBSetupDone = false"));
            is.close();

        } catch (ExceptionInInitializerError e) {

        } catch (FileNotFoundException e) {
            fail("FileNotFoundException - " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            fail("IOException - " + e.getMessage());
            e.printStackTrace();
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //URL is blank
        try {

            InputStream is = new FileInputStream(_testPath
                    + "ptdb-test2.properties");
            URL url = PowerMock.createMock(URL.class);

            mockStatic(FileUtilities.class);
            expect(FileUtilities.nameToURL(ptdbParamsURL, null, null))
                    .andReturn(url);
            expect(url.openStream()).andReturn(is);
            PowerMock.replay(url, FileUtilities.class);

            DBConnectorFactory.loadDBProperties();
            parameters = DBConnectorFactory.getParametersString();

            PowerMock.verify(url, FileUtilities.class);

            assertTrue(
                    "DB ClassName is incorrect.",
                    parameters
                            .contains("_dbClassName = ptdb.kernel.database.OracleXMLDBConnection"));
            assertTrue("DB URL is incorrect.",
                    parameters.contains("_dbUrl = ;"));
            assertTrue("DB Container Name is incorrect.",
                    parameters.contains("_dbContainerName = null"));
            assertTrue("Cache Container Name is incorrect.",
                    parameters.contains("_cacheContainerName = null"));
            assertTrue("Set up cannot be completed",
                    parameters.contains("_isDBSetupDone = false"));
            is.close();

        } catch (ExceptionInInitializerError e) {

        } catch (FileNotFoundException e) {
            fail("FileNotFoundException - " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            fail("IOException - " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Test method for {@link ptdb.common.util.DBConnectorFactory#getSyncConnection(boolean)}.
     * @exception Exception
     */
    @Test
    public void testGetSyncConnection() throws Exception {
        //////////////////////////////////////////////////////////////////////////////////////////
        //No transaction
        try {
            DBConnectorFactory.loadDBProperties();
            DBConnection conn = DBConnectorFactory.getSyncConnection(false);

            assertTrue("Incorrect class returned",
                    conn instanceof OracleXMLDBConnection);
            assertTrue("Test1 - _xmlManager not initialized", conn.toString()
                    .contains("_xmlManager:Initialized"));

            assertTrue(
                    "Test1 - _xmlTransaction  initialized when transaction required was false",
                    conn.toString().contains("_xmlTransaction:Not Initialized"));
            conn.closeConnection();
        } catch (DBConnectionException e) {

            fail("Unexpected error - " + e.getMessage());
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //With transaction
        try {

            DBConnection conn = DBConnectorFactory.getSyncConnection(true);

            assertTrue("Incorrect class returned",
                    conn instanceof OracleXMLDBConnection);
            assertTrue("Test1 - _xmlManager not initialized", conn.toString()
                    .contains("_xmlManager:Initialized"));

            assertTrue(
                    "Test1 - _xmlTransaction not initialized when transaction required was true",
                    conn.toString().contains("_xmlTransaction:Initialized"));
            conn.closeConnection();
        } catch (DBConnectionException e) {

            fail("Unexpected error - " + e.getMessage());
        }
        //////////////////////////////////////////////////////////////////////////////////////////
        //With wrong database class - ClassNotFoundException
        try {

            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection"))
                    .andThrow(new ClassNotFoundException());
            PowerMock.replay(Class.class);

            /*DBConnection conn = */DBConnectorFactory.getSyncConnection(true);

            PowerMock.verify(Class.class);

            fail("Did not give the expected ClassNotFoundException");

        } catch (DBConnectionException e) {

            assertTrue("Unexpected exception - " + e.getMessage(),
                    e.getCause() instanceof ClassNotFoundException);

        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //With wrong constructor - SecurityException
        try {
            Class mockXmlDBClass = Class.class;
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection"))
                    .andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andThrow(
                    new SecurityException());

            PowerMock.replay(Class.class, mockXmlDBClass);

            /*DBConnection conn =*/DBConnectorFactory.getSyncConnection(true);

            PowerMock.verify(Class.class, mockXmlDBClass);

            fail("Did not give the expected SecurityException exception");

        } catch (DBConnectionException e) {

            assertTrue("Unexpected exception - " + e.getMessage(),
                    e.getCause() instanceof SecurityException);

        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (SecurityException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Unexpected error - " + e.getMessage());
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //With wrong constructor - NoSuchMethodException
        try {
            Class mockXmlDBClass = Class.class;
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection"))
                    .andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andThrow(
                    new NoSuchMethodException());

            PowerMock.replay(Class.class, mockXmlDBClass);

            DBConnectorFactory.getSyncConnection(true);

            PowerMock.verify(Class.class, mockXmlDBClass);

            fail("Did not give the expected NosuchMethod exception");

        } catch (DBConnectionException e) {

            assertTrue("Unexpected exception - " + e.getMessage(),
                    e.getCause() instanceof NoSuchMethodException);

        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (SecurityException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Unexpected error - " + e.getMessage());
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        //With wrong constructor - IllegalArgumentException
        try {
            Class mockXmlDBClass = Class.class;
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection"))
                    .andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andThrow(
                    new IllegalArgumentException());

            PowerMock.replay(Class.class, mockXmlDBClass);

            /*DBConnection conn =*/DBConnectorFactory.getSyncConnection(true);

            PowerMock.verify(Class.class, mockXmlDBClass);

            fail("Did not give the expected IllegalArgument exception");

        } catch (DBConnectionException e) {

            assertTrue("Unexpected exception - " + e.getMessage(),
                    e.getCause() instanceof IllegalArgumentException);

        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (SecurityException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Unexpected error - " + e.getMessage());
        }
        DBConnectionParameters dbParams = DBConnectorFactory
                .getDBConnectionParameters();
        //////////////////////////////////////////////////////////////////////////////////////////
        //With wrong newInstance - InstantiationException
        try {

            Class mockXmlDBClass = Class.class;
            Constructor xmlDBConstructor = PowerMock
                    .createMock(Constructor.class);

            PowerMock.expectNew(DBConnectionParameters.class,
                    dbParams.getUrl(), dbParams.getContainerName(), true)
                    .andReturn(dbParams);
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection"))
                    .andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andReturn(
                    xmlDBConstructor);
            expect(xmlDBConstructor.newInstance(dbParams)).andThrow(
                    new InstantiationException());

            PowerMock.replay(DBConnectionParameters.class, Class.class,
                    mockXmlDBClass, xmlDBConstructor);

            DBConnectorFactory.getSyncConnection(true);

            PowerMock.verify(DBConnectionParameters.class, Class.class,
                    mockXmlDBClass, xmlDBConstructor);

            fail("Did not give the expected Instantiation exception");

        } catch (DBConnectionException e) {

            assertTrue("Unexpected exception - " + e.getMessage(),
                    e.getCause() instanceof InstantiationException);

        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (SecurityException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (IllegalArgumentException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (InstantiationException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (InvocationTargetException e) {
            fail("Unexpected error - " + e.getMessage());
        }
    }

    @Test
    public void testGetSyncConnection_WithException() throws Exception {
        //////////////////////////////////////////////////////////////////////////////////////////
        DBConnectionParameters dbParams = DBConnectorFactory
                .getDBConnectionParameters();
        //With wrong newInstance - IllegalAccessException
        try {

            Class mockXmlDBClass = Class.class;
            Constructor xmlDBConstructor = PowerMock
                    .createMock(Constructor.class);

            PowerMock.expectNew(DBConnectionParameters.class,
                    dbParams.getUrl(), dbParams.getContainerName(), false)
                    .andReturn(dbParams);
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection"))
                    .andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andReturn(
                    xmlDBConstructor);
            expect(xmlDBConstructor.newInstance(dbParams)).andThrow(
                    new IllegalAccessException());

            //expect(xmlDBConstructor.newInstance(dbParams)).andReturn(
            //                                                             mockXmlDBClass);

            PowerMock.replay(DBConnectionParameters.class, Class.class,
                    mockXmlDBClass, xmlDBConstructor);

            DBConnectorFactory.getSyncConnection(false);

            PowerMock.verify(DBConnectionParameters.class, Class.class,
                    mockXmlDBClass, xmlDBConstructor);

            fail("Did not give the expected IllegalAccessException");

        } catch (DBConnectionException e) {

            assertTrue("Unexpected exception - " + e.getMessage(),
                    e.getCause() instanceof IllegalAccessException);

        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (SecurityException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (IllegalArgumentException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (InstantiationException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (InvocationTargetException e) {
            fail("Unexpected error - " + e.getMessage());
        }
    }

    @Test
    public void testGetSyncConnection_WithException2() throws Exception {
        //////////////////////////////////////////////////////////////////////////////////////////
        DBConnectionParameters dbParams = DBConnectorFactory
                .getDBConnectionParameters();

        //With wrong newInstance - InvocationTargetException
        try {

            Class mockXmlDBClass = Class.class;
            Constructor xmlDBConstructor = PowerMock
                    .createMock(Constructor.class);

            PowerMock.expectNew(DBConnectionParameters.class,
                    dbParams.getUrl(), dbParams.getContainerName(), true)
                    .andReturn(dbParams);
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection"))
                    .andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andReturn(
                    xmlDBConstructor);
            expect(xmlDBConstructor.newInstance(dbParams)).andThrow(
                    new InvocationTargetException(new Exception("Test")));
            //PowerMock.createMock(InvocationTargetException.class));
            PowerMock.replay(DBConnectionParameters.class, Class.class,
                    mockXmlDBClass, xmlDBConstructor);

            DBConnectorFactory.getSyncConnection(true);

            PowerMock.verify(DBConnectionParameters.class, Class.class,
                    mockXmlDBClass, xmlDBConstructor);

            fail("Did not give the expected InvocationTargetException");

        } catch (DBConnectionException e) {

            assertTrue("Unexpected exception - " + e.getMessage(),
                    e.getCause() instanceof InvocationTargetException);

        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (SecurityException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (IllegalArgumentException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (InstantiationException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (InvocationTargetException e) {
            fail("Unexpected error - " + e.getMessage());
        }
    }

    /**
     * Test getSyncConnection when setup is not done.
     * @exception Exception
     */
    @Test
    public void testGetSyncConn_SetupNotDone() throws Exception {

        try {
            PowerMock
                    .mockStaticPartial(DBConnectorFactory.class, "isSetupDone");
            expect(DBConnectorFactory.isSetupDone()).andReturn(false);
            PowerMock.replay(DBConnectorFactory.class);

            DBConnectorFactory.getSyncConnection(true);
            fail("Did not give the expected DBConnectionException exception");
            PowerMock.verify(DBConnectorFactory.class);

        } catch (DBConnectionException e) {
            assertTrue(
                    "Unexpected exception - " + e.getMessage(),
                    e.getMessage().startsWith(
                            "XML Database Connection is not configured."));
        }
    }

    @Test
    public void testGetSetupParameters() throws Exception {

        try {
            PowerMock
                    .mockStaticPartial(DBConnectorFactory.class, "isSetupDone");
            PowerMock.expectPrivate(DBConnectorFactory.class, "isSetupDone")
                    .andReturn(false);
            PowerMock.replayAll();

            SetupParameters setupParameters = DBConnectorFactory
                    .getSetupParameters();
            assertTrue("Setup parameters should be null",
                    setupParameters == null);
            PowerMock.verifyAll();

        } catch (DBConnectionException e) {
            fail("Unexpected exception - " + e.getMessage());
        }

        PowerMock.mockStaticPartial(DBConnectorFactory.class, "isSetupDone");
        PowerMock.expectPrivate(DBConnectorFactory.class, "isSetupDone")
                .andReturn(true);
        PowerMock.replayAll();
        SetupParameters setupParameters = DBConnectorFactory
                .getSetupParameters();
        PowerMock.verifyAll();
        assertTrue("Setup parameters should be null", setupParameters != null);
        String parameters = DBConnectorFactory.getParametersString();

        assertTrue("DB URL is incorrect.",
                parameters.contains("_dbUrl = " + setupParameters.getUrl()));
        assertTrue(
                "DB Container Name is incorrect.",
                parameters.contains("_dbContainerName = "
                        + setupParameters.getContainerName()));
        assertTrue(
                "Cache Container Name is incorrect.",
                parameters.contains("_cacheContainerName = "
                        + setupParameters.getCacheContainerName()));

    }

    /**
     * Test method for {@link ptdb.common.util.DBConnectorFactory#getAsyncConnection()}.
     * @exception Exception
     */
    @Test
    public void testGetAsyncConnection() throws Exception {
        TaskQueue mockTaskQueue = PowerMock.createMock(TaskQueue.class);
        ExecutorThread mockExecutorThread = PowerMock
                .createMock(ExecutorThread.class);
        PowerMock.expectNew(TaskQueue.class).andReturn(mockTaskQueue);
        PowerMock.expectNew(ExecutorThread.class, mockTaskQueue).andReturn(
                mockExecutorThread);
        PowerMock.verify();

        DBConnection dbConnection = DBConnectorFactory.getAsyncConnection();
        assertTrue("Incorrect instance returned",
                dbConnection instanceof AsynchronousDBConnection);
    }

    /**
     * Test method for {@link ptdb.common.util.DBConnectorFactory#getCacheConnection(boolean)}.
     * @exception DBConnectionException
     */
    @Test
    public void testGetCacheConnection() throws DBConnectionException {
        DBConnection dbConnection = null;
        try {
            dbConnection = DBConnectorFactory.getCacheConnection(false);
            assertTrue("Incorrect instance returned",
                    dbConnection instanceof OracleXMLDBConnection);
        } finally {
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }
        }

    }

    private String _getDatabasePath() {
        String path = StringUtilities.getProperty("ptolemy.ptII.dir")
                + fileSeparator + "ptdb" + fileSeparator + "config"
                + fileSeparator + "database" + fileSeparator + "testdata";
        return path.replace('\\', '/');
    }
}
