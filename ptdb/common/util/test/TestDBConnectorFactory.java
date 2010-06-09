/*
 * 
 */
package ptdb.common.util.test;

import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;
import static org.easymock.EasyMock.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import ptdb.common.exception.DBConnectionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;
import ptdb.kernel.database.OracleXMLDBConnection;

///////////////////////////////////////////////////////////////
//// TestDBConnectorFactory

/**
 * Unit test DBConnectorFactory.
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
@PrepareForTest({DBConnectorFactory.class, TestDBConnectorFactory.class})
@RunWith(PowerMockRunner.class)

public class TestDBConnectorFactory {
    
    String systemPath = System.getProperty("user.dir");
    String fileSeparator = System.getProperty("file.separator");
    String _testPath;
    
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
       
       StringBuffer strBuf = new StringBuffer(systemPath);
       
       strBuf.append(fileSeparator).append("ptdb");
       strBuf.append(fileSeparator).append("kernel");
       strBuf.append(fileSeparator).append("database");
       strBuf.append(fileSeparator).append("test");
       strBuf.append(fileSeparator);
       
       _testPath = strBuf.toString();
       
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link ptdb.common.util.DBConnectorFactory#loadDBProperties()}.
     */
    @Test
    public void testLoadDBProperties() {
        
        // All Correct values
        DBConnectorFactory.loadDBProperties();
        String parameters = DBConnectorFactory.getParametersString();
        assertTrue(
                "DB ClassName is incorrect.",
                parameters
                        .contains("_dbClassName = ptdb.kernel.database.OracleXMLDBConnection"));
        assertTrue("DB URL is incorrect.", parameters
                .contains("_dbUrl = C:/users/wini"));
        assertTrue("DB Conatainer Name is incorrect.", parameters
                .contains("_dbContainerName = temp.dbxml"));
        assertTrue("Cache Conatainer Name is incorrect.", parameters
                .contains("_cacheContainerName = temp_cache.dbxml"));

        
//////////////////////////////////////////////////////////////////////////////////////////        
        //Incorrect URL
        boolean isSetupDone = DBConnectorFactory.isSetupDone();
        try {
            
            URL urlInvalid = ClassLoader.getSystemResource("invalid_resource");
            mockStatic(ClassLoader.class);
            expect(
                    ClassLoader.getSystemResource("ptdb-params.properties"))
                    .andReturn(urlInvalid);
            replayAll();
            
            DBConnectorFactory.loadDBProperties();
            
            verifyAll();
            fail("No error reported");
            
        } catch (ExceptionInInitializerError e) {
            assertTrue("Set up setting has unchanged", DBConnectorFactory.isSetupDone() == isSetupDone);
        }
        
//////////////////////////////////////////////////////////////////////////////////////////
        //IO Exception
        isSetupDone = DBConnectorFactory.isSetupDone();
        try {
            
            URL url = PowerMock.createMock(URL.class);
            mockStatic(ClassLoader.class);
            expect(
                    ClassLoader.getSystemResource("ptdb-params.properties"))
                    .andReturn(url);
            expect(
                    url.openStream())
                    .andThrow(new IOException());
            PowerMock.replay(url, ClassLoader.class);
            
            DBConnectorFactory.loadDBProperties();
            
            PowerMock.verify(url, ClassLoader.class);
            fail("No error reported");
        
        } catch (ExceptionInInitializerError e) {
            assertTrue("Set up setting has unchanged", DBConnectorFactory.isSetupDone() == isSetupDone);
        } catch (IOException e) {
            fail("Wrong error reported");
            e.printStackTrace();
        }
        
//////////////////////////////////////////////////////////////////////////////////////////       
      //URL is null
        try {
            
            URL url = PowerMock.createMock(URL.class);
            InputStream is = new FileInputStream(_testPath + "ptdb-test1.properties");
            
            mockStatic(ClassLoader.class);
            expect(
                    ClassLoader.getSystemResource("ptdb-params.properties"))
                    .andReturn(url);
            expect(url.openStream()).andReturn(is);
            PowerMock.replay(url, ClassLoader.class);
            
            DBConnectorFactory.loadDBProperties();
            parameters = DBConnectorFactory.getParametersString();
            
            PowerMock.verify(url, ClassLoader.class);
            
            assertTrue(
                    "DB ClassName is incorrect.",
                    parameters
                            .contains("_dbClassName = ptdb.kernel.database.OracleXMLDBConnection"));
            assertTrue("DB URL is incorrect.", parameters
                    .contains("_dbUrl = null;"));
            assertTrue("DB Container Name is incorrect.", parameters
                    .contains("_dbContainerName = null"));
            assertTrue("Cache Container Name is incorrect.", parameters
                    .contains("_cacheContainerName = null"));
            assertTrue("Set up cannot be completed", parameters
                    .contains("_isDBSetupDone = false"));
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
              
              InputStream is = new FileInputStream(_testPath + "ptdb-test2.properties");
              URL url = PowerMock.createMock(URL.class);
              
              mockStatic(ClassLoader.class);
              expect(
                      ClassLoader.getSystemResource("ptdb-params.properties"))
                      .andReturn(url);
              expect(url.openStream()).andReturn(is);
              PowerMock.replay(url,ClassLoader.class);
              
              DBConnectorFactory.loadDBProperties();
              parameters = DBConnectorFactory.getParametersString();
              
              PowerMock.verify(url, ClassLoader.class);
              
              assertTrue(
                      "DB ClassName is incorrect.",
                      parameters
                              .contains("_dbClassName = ptdb.kernel.database.OracleXMLDBConnection"));
              assertTrue("DB URL is incorrect.", parameters
                      .contains("_dbUrl = ;"));
              assertTrue("DB Container Name is incorrect.", parameters
                      .contains("_dbContainerName = null"));
              assertTrue("Cache Container Name is incorrect.", parameters
                      .contains("_cacheContainerName = null"));
              assertTrue("Set up cannot be completed", parameters
                      .contains("_isDBSetupDone = false"));
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
     */
    @Test
    public void testGetSyncConnection() {
//////////////////////////////////////////////////////////////////////////////////////////
        //No transaction
        try {
            DBConnectorFactory.loadDBProperties();
            DBConnection conn = DBConnectorFactory.getSyncConnection(false);
            
            assertTrue("Incorrect class returned", conn instanceof OracleXMLDBConnection);
            assertTrue("Test1 - _xmlManager not initialized", conn.toString()
                    .contains("_xmlManager:Initialized"));

            assertTrue(
                    "Test1 - _xmlTransaction  initialized when transaction required was false",
                    conn.toString().contains("_xmlTransaction:Not Initialized"));
        } catch (DBConnectionException e) {
            
            fail("Unexpected error - " + e.getMessage());
        }
 
//////////////////////////////////////////////////////////////////////////////////////////
        //With transaction
        try {
            
            DBConnection conn = DBConnectorFactory.getSyncConnection(true);
            
            assertTrue("Incorrect class returned", conn instanceof OracleXMLDBConnection);
            assertTrue("Test1 - _xmlManager not initialized", conn.toString()
                    .contains("_xmlManager:Initialized"));

            assertTrue(
                    "Test1 - _xmlTransaction not initialized when transaction required was true",
                    conn.toString().contains("_xmlTransaction:Initialized"));
        } catch (DBConnectionException e) {
            
            fail("Unexpected error - " + e.getMessage());
        }
//////////////////////////////////////////////////////////////////////////////////////////
        //With wrong database class - ClassNotFoundException
        try {
            
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection")).andThrow(new ClassNotFoundException());
            PowerMock.replay(Class.class);
            
            DBConnection conn = DBConnectorFactory.getSyncConnection(true);
            
            PowerMock.verify(Class.class);
            
            fail("Did not give the expected exception");
            
        } catch (DBConnectionException e) {
            
            assertTrue("Unexpected exception - " + e.getMessage(), e.getCause() instanceof ClassNotFoundException);
            
        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        }
        
//////////////////////////////////////////////////////////////////////////////////////////
        //With wrong constructor - SecurityException
        try {
            Class mockXmlDBClass = Class.class;
            mockStatic(Class.class);
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection")).andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andThrow(new SecurityException());

            PowerMock.replay(Class.class, mockXmlDBClass);
            
            DBConnection conn = DBConnectorFactory.getSyncConnection(true);
            
            PowerMock.verify(Class.class, mockXmlDBClass);
            
            fail("Did not give the expected exception");
            
        } catch (DBConnectionException e) {
            
            assertTrue("Unexpected exception - " + e.getMessage(), e.getCause() instanceof SecurityException);
            
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
            expect(Class.forName("ptdb.kernel.database.OracleXMLDBConnection")).andReturn(mockXmlDBClass);

            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            expect(mockXmlDBClass.getConstructor(parameterTypes)).andThrow(new NoSuchMethodException());

            PowerMock.replay(Class.class, mockXmlDBClass);
            
            DBConnection conn = DBConnectorFactory.getSyncConnection(true);
            
            PowerMock.verify(Class.class, mockXmlDBClass);
            
            fail("Did not give the expected exception");
            
        } catch (DBConnectionException e) {
            
            assertTrue("Unexpected exception - " + e.getMessage(), e.getCause() instanceof NoSuchMethodException);
            
        } catch (ClassNotFoundException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (SecurityException e) {
            fail("Unexpected error - " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Unexpected error - " + e.getMessage());
        }
    }

    /**
     * Test method for {@link ptdb.common.util.DBConnectorFactory#getAsyncConnection()}.
     */
    @Test
    public void testGetAsyncConnection() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link ptdb.common.util.DBConnectorFactory#getCacheConnection(boolean)}.
     */
    @Test
    public void testGetCacheConnection() {
        fail("Not yet implemented");
    }

    //////////////////////////////////////////////////////////////////////
    ////		public variables 				//////

    //////////////////////////////////////////////////////////////////////
    ////		public methods 					//////

    //////////////////////////////////////////////////////////////////////
    ////		protected methods 				//////

    //////////////////////////////////////////////////////////////////////
    ////		protected variables 				//////

    //////////////////////////////////////////////////////////////////////
    ////		private methods 				//////

    //////////////////////////////////////////////////////////////////////
    ////		private variables				//////

}
