/*
 * 
 */
package ptdb.kernel.database.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.OracleXMLDBConnection;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

import com.sleepycat.dbxml.XmlException;

///////////////////////////////////////////////////////////////
//// TestOracleXMLDBConnection

/**
 * Unit tests for OracleXMLDBConnection.
 * 
 * @author Ashwini Bijwe
 *
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
@PrepareForTest( { OracleXMLDBConnection.class, TestOracleXMLDBConnection.class })
@RunWith(PowerMockRunner.class)
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
	 * Correct url, container name and transaction required = true.
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
	    conn.abortConnection();
	    conn.closeConnection();
	} catch (DBConnectionException e) {

	    fail("Test 1 - " + e.getMessage());
	    e.printStackTrace();
	}

	//////////////////////////////////////////////////////////////////////////////////////////        
	/**
	 * Correct url, container name and transaction required = false.
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
	 * Incorrect url, correct container name and transaction required = true.
	 */

	url = "c:\\users\\error";
	containerName = "temp.dbxml";
	isTransactionRequired = true;

	dbConnParams = new DBConnectionParameters(url, containerName,
	        isTransactionRequired);

	try {
	    OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

	    fail("Test 3 - No exception thrown when there was an error in path");
	    conn.abortConnection();
	    conn.closeConnection();
	} catch (DBConnectionException e) {

	    assertTrue("Test 3 - Incorrect exception thrown -"
		    + e.getClass().getName(),
		    e.getCause() instanceof FileNotFoundException);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Correct URL, incorrect container name and transaction required = true.
	 */

	url = "c:\\users\\wini";
	containerName = "nosuchcontainer.dbxml";
	isTransactionRequired = true;

	dbConnParams = new DBConnectionParameters(url, containerName,
	        isTransactionRequired);

	try {
	    OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

	    fail("Test 4 - No exception thrown when there was an error in path");
	    conn.abortConnection();
	    conn.closeConnection();
	} catch (DBConnectionException e) {

	    assertTrue("Test 4 - Incorrect exception thrown - "
		    + e.getClass().getName(),
		    e.getCause() instanceof XmlException);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Incorrect url, container name and transaction required = true.
	 */

	url = "c:\\users\\error";
	containerName = "nosuchcontainer.dbxml";
	isTransactionRequired = true;

	dbConnParams = new DBConnectionParameters(url, containerName,
	        isTransactionRequired);

	try {
	    OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);

	    fail("Test 5 - No exception thrown when there was an error in path");
	    conn.abortConnection();
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
	    fail("Failed while creting a connection without transaction");
	}

	try {
	    conn.abortConnection();

	} catch (DBConnectionException e) {
	    fail("Test 1 - Exception while aborting an open connection without transaction");
	}

	try {
	    conn = createConnWithTransaction();
	} catch (DBConnectionException e1) {
	    fail("Faile while creating a connection without transaction");
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
	    conn.abortConnection();
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

    /**
     * Test method for {@link ptdb.kernel.database.OracleXMLDBConnection#executeAttributeSearchTask(ptdb.common.dto.AttributeSearchTask)}.
     * @throws Exception 
     */
    @Test
    public void testExecuteAttributesSearchTask() throws Exception {

	OracleXMLDBConnection conn = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(false);

	AttributeSearchTask task = new AttributeSearchTask();
	Attribute attribute = PowerMock.createMock(Attribute.class);

	Variable variableCreatedBy = new Variable();
	variableCreatedBy.setClassName("ptolemy.data.expr.StringParameter");
	variableCreatedBy.setName("CreatedBy");
	Token tokenCreatedBy = new StringToken("Ashwini Bijwe");
	variableCreatedBy.setToken(tokenCreatedBy);

	Variable variableModelId = new Variable();
	variableModelId.setClassName("ptolemy.data.expr.Parameter");
	variableModelId.setName("ModelId");
	Token tokenModelId = new StringToken("13781");
	variableModelId.setToken(tokenModelId);

	task.addAttribute(attribute);
	task.addAttribute(variableCreatedBy);
	task.addAttribute(variableModelId);
	task.addAttribute(attribute);

	try {

	    ArrayList<XMLDBModel> modelsList = conn
		    .executeAttributeSearchTask(task);
	    assertTrue("More than one results returned.",
		    modelsList.size() == 1);
	    String modelName = modelsList.get(0).getModelName();
	    assertTrue(modelName + " - Wrong model returned.",
		    "ModelContainsBothAttributes.xml".equals(modelName));

	    ///////////////////////////////////////////////////////////////////////////////////////
	    //IllegalActionException
	    try {
		OracleXMLDBConnection mockConn = PowerMock.createPartialMock(
		        OracleXMLDBConnection.class, "_createAttributeClause",
		        "_executeSingleAttributeMatch");

		PowerMock.expectPrivate(mockConn, "_createAttributeClause",
		        OracleXMLDBConnection.class, variableCreatedBy)
		        .andThrow(new IllegalActionException("Test Exception"));

		PowerMock.expectPrivate(mockConn, "_createAttributeClause",
		        OracleXMLDBConnection.class, variableModelId)
		        .andReturn("Test String");

		PowerMock.expectPrivate(mockConn,
		        "_executeSingleAttributeMatch",
		        OracleXMLDBConnection.class, "Test String").andThrow(
		        new XmlException(1, "Mock Exception"));

		PowerMock.replay(mockConn);

		mockConn.executeAttributeSearchTask(task);

		fail("No Exception thrown");
	    } catch (DBExecutionException e) {

	    }
	    ///////////////////////////////////////////////////////////////////////////////////////
	    //null attribute list

	    task.setAttributesList(null);
	    modelsList = conn.executeAttributeSearchTask(task);
	    assertTrue("Search was performed without attributes list.",
		    modelsList == null);

	    ///////////////////////////////////////////////////////////////////////////////////////
	    //0-size attribute list
	    task.setAttributesList(new ArrayList<Attribute>());
	    modelsList = conn.executeAttributeSearchTask(task);
	    assertTrue("Search was performed without attributes list.",
		    modelsList == null);

	    conn.closeConnection();
	} catch (DBExecutionException e) {
	    fail("Unexpected Exception - " + e.getMessage());
	    e.printStackTrace();
	    conn.closeConnection();
	}

    }

    /**
     * Test the executCreateTask method.
     * Conditions for the test:
     * The model being saved is a new model.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_NewModel() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	CreateModelTask task = new CreateModelTask();
	XMLDBModel xmlModel = new XMLDBModel();

	xmlModel.setIsNew(true);
	// Change the name of the model with each test to ensure it is treated as a new model.
	xmlModel.setModelName("test2");
	xmlModel
	        .setModel("<entity name=\"test2\" class=\"test.class\"></entity>");

	task.setXMLDBModel(xmlModel);

	try {
	    oracleXMLDBConnection.executeCreateModelTask(task);

	    assertTrue("Model was created", true);

	} catch (DBExecutionException e) {
	    fail("Exception thrown - " + e.getMessage());
	} finally {
	    if (oracleXMLDBConnection != null) {
		oracleXMLDBConnection.closeConnection();
	    }
	}

    }

    /**
     * Test the executCreateTask method.
     * Conditions for the test:
     * The model to be created already exist in the database.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_ExistingModel() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	CreateModelTask task = new CreateModelTask();
	XMLDBModel xmlModel = new XMLDBModel();

	xmlModel.setIsNew(true);
	// make sure the file exists in the database.
	xmlModel.setModelName("test");
	xmlModel
	        .setModel("<entity name=\"test\" class=\"test.class\"></entity>");

	task.setXMLDBModel(xmlModel);

	try {
	    oracleXMLDBConnection.executeCreateModelTask(task);

	    fail("Model was created when it should be already there.");

	} catch (DBExecutionException e) {

	    if (e.getMessage().contains("The model already exist")) {
		assertTrue("model was not created because it already exists",
		        true);
	    }
	} finally {
	    if (oracleXMLDBConnection != null) {
		oracleXMLDBConnection.closeConnection();
	    }
	}

    }

    /**
     * Test the executCreateTask method.
     * Conditions for the test:
     * The create model task is null.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_NullTask() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	CreateModelTask task = null;

	try {
	    oracleXMLDBConnection.executeCreateModelTask(task);

	    fail("Model was created when it should not be.");

	} catch (DBExecutionException e) {

	    if (e.getMessage().contains(
		    "the CreateModelTask object passed was null")) {
		assertTrue("model was not created because the task is null",
		        true);
	    }
	} finally {
	    if (oracleXMLDBConnection != null) {
	        oracleXMLDBConnection.closeConnection();
            }
	}

    }

    /**
     * Test the executCreateTask method.
     * Conditions for the test:
     * The model in the task is null.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_NullModelInTask() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	CreateModelTask task = new CreateModelTask();

	task.setXMLDBModel(null);

	try {
	    oracleXMLDBConnection.executeCreateModelTask(task);

	    fail("Model was created when it should not be.");

	} catch (DBExecutionException e) {

	    if (e.getMessage().contains("the XMLDBModel object passed in the CreateModelTask was null")) {
		assertTrue("model was not created because the model in the task was null.",
		        true);
	    }
	} finally {
	    if (oracleXMLDBConnection != null) {
	        oracleXMLDBConnection.closeConnection();
            }
	}

    }

    /**
     * Test the executSaveTask method.
     * Conditions for the test:
     * the model being saved is a new model.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_NewModel() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	SaveModelTask task = new SaveModelTask();
	XMLDBModel xmlModel = new XMLDBModel();

	xmlModel.setIsNew(true);
	// Make sure the file does not exist in the database when running the test.
	xmlModel.setModelName("test5");
	xmlModel.setModel("<entity name=\"test5\" class=\"test.class\"></entity>");

	task.setXMLDBModel(xmlModel);

	try {
	    oracleXMLDBConnection.executeSaveModelTask(task);

	    fail("Model should not be saved");

	} catch (DBExecutionException e) {

	    if (e.getMessage().contains(
		    "the model does not exist in the database.")) {
		assertTrue(
		        "model was not updated since it does not exist in the database.",
		        true);
	    }
	} finally {
	    if (oracleXMLDBConnection != null) {
	        oracleXMLDBConnection.closeConnection();
            }
	}

    }

    /**
     * Test the executSaveTask method.
     * Conditions for the test:
     * The model to be saved already exist in the database.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_ExistingModel() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	SaveModelTask task = new SaveModelTask();
	XMLDBModel xmlModel = new XMLDBModel();

	xmlModel.setIsNew(true);
	// Make sure the file exists in the database when running the test.
	xmlModel.setModelName("test2");
	xmlModel
	        .setModel("<entity name=\"test2\" class=\"test1.class\"></entity>");

	task.setXMLDBModel(xmlModel);

	try {
	    oracleXMLDBConnection.executeSaveModelTask(task);

	    assertTrue("Model was updated...", true);

	} catch (DBExecutionException e) {

	    fail("Exception thrown - " + e.getMessage());
	} finally {
	    if (oracleXMLDBConnection != null) {
	        oracleXMLDBConnection.closeConnection();
            }
	}

    }

    /**
     * Test the executSaveTask method.
     * Conditions for the test:
     * The save model task is null.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_NullTask() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	SaveModelTask task = null;

	try {
	    oracleXMLDBConnection.executeSaveModelTask(task);

	    fail("Model was saved when it should not be.");

	} catch (DBExecutionException e) {

	    if (e.getMessage().contains(
		    "the SaveModelTask object passed was null")) {
		assertTrue("model was not saved because the task is null", true);
	    }
	} finally {
	    if (oracleXMLDBConnection != null) {
	        oracleXMLDBConnection.closeConnection();
            }
	}

    }

    /**
     * Test the executSaveTask method.
     * Conditions for the test:
     * The model in the task is null.
     * @exception Exception thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_NullModelInTask() throws Exception {

	OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
	        .getSyncConnection(true);

	SaveModelTask task = new SaveModelTask();

	task.setXMLDBModel(null);

	try {
	    oracleXMLDBConnection.executeSaveModelTask(task);

	    fail("Model was created when it should not be.");

	} catch (DBExecutionException e) {

	    if (e.getMessage().contains(
		    "the XMLDBModel object passed in the SaveModelTask was null")) {
		assertTrue("model was not saved because the model in the task was null.",
		        true);
	    }
	} finally {
	    if (oracleXMLDBConnection != null) {
	        oracleXMLDBConnection.closeConnection();
            }
	}

    }

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
    ////		private variables			   //////

}
