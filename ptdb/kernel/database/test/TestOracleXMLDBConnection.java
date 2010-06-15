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
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetModelsTask;
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

///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    ////                public methods                                    //////
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
    }

    /**
     * @exception java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link ptdb.kernel.database.OracleXMLDBConnection#OracleXMLDBConnection(ptdb.common.dto.DBConnectionParameters)}
     * .
     */

    @Test
    public void testOracleXMLDBConnection() {

        //////////////////////////////////////////////////////////////////////////////////////////
        /*
         * Correct url, container name and transaction required = true.
         */
        DBConnectorFactory.loadDBProperties();
        DBConnectionParameters connectionParameters = DBConnectorFactory
                .getDBConnectionParameters();
        String url = connectionParameters.getUrl();
        String containerName = connectionParameters.getContainerName();
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
        /*
         * Correct url, container name and transaction required = false.
         */

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
        /*
         * Incorrect url, correct container name and transaction required = true.
         */

        url += "error";
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
        /*
         * Correct URL, incorrect container name and transaction required = true.
         */

        url = connectionParameters.getUrl();
        containerName += "error";
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
        /*
         * Incorrect url, container name and transaction required = true.
         */

        url += "error";
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
     * Test method for
     * {@link ptdb.kernel.database.OracleXMLDBConnection#abortConnection()}.
     * @throws DBConnectionException 
     */

    @Test
    public void testAbortConnection() throws DBConnectionException {

        OracleXMLDBConnection conn = null;
        try {
            try {
                conn = _createConnWithoutTransaction();
            } catch (DBConnectionException e1) {
                fail("Failed while creting a connection without transaction");
            }

            try {
                conn.abortConnection();

            } catch (DBConnectionException e) {
                fail("Test 1 - Exception while aborting an open connection without transaction");
            }

            try {
                conn = _createConnWithTransaction();
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
        } finally {
            if(conn != null) {
                conn.closeConnection();
            }
                
        }
    }

    /**
     * Test method for
     * {@link ptdb.kernel.database.OracleXMLDBConnection#closeConnection()}.
     */

    @Test
    public void testCloseConnection() {
        OracleXMLDBConnection conn = null;

        try {
            conn = _createConnWithoutTransaction();
        } catch (DBConnectionException e1) {
            fail("Faile while creating a connection without transaction - "
                    + e1.getMessage());
        }

        try {
            conn.closeConnection();

        } catch (DBConnectionException e) {
            fail("Test 1 - Exception while closing an open connection without transaction"
                    + e.getMessage());
        }

        try {
            conn = _createConnWithTransaction();
        } catch (DBConnectionException e1) {
            fail("Faile while creating a connection without transaction"
                    + e1.getMessage());
        }

        try {
            conn.abortConnection();
            conn.closeConnection();

        } catch (DBConnectionException e) {
            fail("Test 2 - Exception while closing an open connection without transaction"
                    + e.getMessage());
        }

        try {
            conn.closeConnection();
            fail("Test 3 - Failed to throw an exception while closing an already closed connection");
        } catch (DBConnectionException e) {

        }
    }

    /**
     * Test method for
     * {@link ptdb.kernel.database.OracleXMLDBConnection#executeAttributeSearchTask(ptdb.common.dto.AttributeSearchTask)}
     * .
     * @exception Exception
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
     * Test executeFetchHierarchyTask of OracleXMLDBConnection.
     * @exception DBConnectionException If thrown while creating a connection.
     */
    @Test
    public void testExecuteFetchHierarchyTask() throws DBConnectionException {
        OracleXMLDBConnection conn = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        FetchHierarchyTask task = new FetchHierarchyTask();
        XMLDBModel dbModel = new XMLDBModel("D.xml");
        ArrayList<XMLDBModel> list = new ArrayList<XMLDBModel>();
        list.add(dbModel);
        task.setModelsList(list);

        try {
            list = conn.executeFetchHierarchyTask(task);
            assertTrue("No models returned", list.size() > 0);
            dbModel = list.get(0);
            int hierarchySize = dbModel.getParents().size();

            assertTrue("Wrong number of hierarchies returned - "
                    + hierarchySize, hierarchySize == 5);

        } catch (DBExecutionException e) {
            e.printStackTrace();
            fail("Failed with exception - " + e.getMessage());
        }
        ////////////////////////////////////////////////////////////////
        //Model that is not present in the database.

        task = new FetchHierarchyTask();
        dbModel = new XMLDBModel("DB.xml");
        list = new ArrayList<XMLDBModel>();
        list.add(dbModel);
        task.setModelsList(list);

        try {
            list = conn.executeFetchHierarchyTask(task);
            assertTrue("No models returned", list.size() > 0);
            dbModel = list.get(0);
            assertTrue("Hierarchies returned when they should be null", dbModel
                    .getParents() == null
                    || dbModel.getParents().size() == 0);

        } catch (DBExecutionException e) {
            e.printStackTrace();
            fail("Failed with exception - " + e.getMessage());
        }
    }

    /**
     * Test the executCreateTask method. Conditions for the test: The model
     * being saved is a new model.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
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
            oracleXMLDBConnection.commitConnection();
            assertTrue("Model was created", true);

        } catch (DBExecutionException e) {
            oracleXMLDBConnection.abortConnection();
            fail("Exception thrown - " + e.getMessage());
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executCreateTask method. Conditions for the test: The model to
     * be created already exist in the database.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
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
            oracleXMLDBConnection.commitConnection();

            fail("Model was created when it should be already there.");

        } catch (DBExecutionException e) {
            oracleXMLDBConnection.abortConnection();
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
     * Test the executCreateTask method. Conditions for the test: The create
     * model task is null.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteCreateTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

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
     * Test the executCreateTask method. Conditions for the test: The model in
     * the task is null.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteCreateTask_NullModelInTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        CreateModelTask task = new CreateModelTask();

        task.setXMLDBModel(null);

        try {
            oracleXMLDBConnection.executeCreateModelTask(task);

            fail("Model was created when it should not be.");

        } catch (DBExecutionException e) {

            if (e
                    .getMessage()
                    .contains(
                            "the XMLDBModel object passed in the CreateModelTask was null")) {
                assertTrue(
                        "model was not created because the model in the task was null.",
                        true);
            }
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executSaveTask method. Conditions for the test: the model being
     * saved is a new model.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteSaveTask_NewModel() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        SaveModelTask task = new SaveModelTask();
        XMLDBModel xmlModel = new XMLDBModel();

        xmlModel.setIsNew(true);
        // Make sure the file does not exist in the database when running the test.
        xmlModel.setModelName("test5");
        xmlModel
                .setModel("<entity name=\"test5\" class=\"test.class\"></entity>");

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
     * Test the executSaveTask method. Conditions for the test: The model to be
     * saved already exist in the database.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
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
            oracleXMLDBConnection.commitConnection();
            assertTrue("Model was updated...", true);

        } catch (DBExecutionException e) {
            oracleXMLDBConnection.abortConnection();
            fail("Exception thrown - " + e.getMessage());
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executSaveTask method. Conditions for the test: The save model
     * task is null.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteSaveTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

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
     * Test the executSaveTask method. Conditions for the test: The model in the
     * task is null.
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteSaveTask_NullModelInTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        SaveModelTask task = new SaveModelTask();

        task.setXMLDBModel(null);

        try {
            oracleXMLDBConnection.executeSaveModelTask(task);

            fail("Model was created when it should not be.");

        } catch (DBExecutionException e) {

            if (e
                    .getMessage()
                    .contains(
                            "the XMLDBModel object passed in the SaveModelTask was null")) {
                assertTrue(
                        "model was not saved because the model in the task was null.",
                        true);
            }
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    ///////////////////////////////////////////////////////////////////

    /**
     * Test executeGetModelsTask() method.
     * 
     * Test conditions: The model exist in the database and does not have
     * references.
     * 
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteGetModels_ModelWithNoReferences() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelsTask task = new GetModelsTask();

        String strModelName = "NoReferences";

        task.setModelName(strModelName);

        try {

            XMLDBModel model = oracleXMLDBConnection.executeGetModelsTask(task);

            if (model != null && model.getModelName().equals(strModelName)) {

                System.out.println(model.getModel());
                assertTrue("Model was retrieved successfully.", true);
            } else if (model != null
                    && !model.getModelName().equals(strModelName)) {
                fail("Different model was retrieved. " + model.getModelName());
            } else {
                fail("no model was returned");
            }

        } catch (DBExecutionException e) {

            fail("Operation threw an exception. " + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test executeGetModelsTask() method.
     * 
     * Test conditions: The model exist in the database and has references in
     * it.
     * 
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteGetModels_ModelWithReferences() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelsTask task = new GetModelsTask();

        String strModelName = "modeltt";

        task.setModelName(strModelName);

        try {

            XMLDBModel model = oracleXMLDBConnection.executeGetModelsTask(task);

            if (model != null && model.getModelName().equals(strModelName)) {

                System.out.println(model.getModel());
                assertTrue("Model was retrieved successfully.", true);
            } else if (model != null
                    && !model.getModelName().equals(strModelName)) {
                fail("Different model was retrieved. " + model.getModelName());
            } else {
                fail("no model was returned");
            }

        } catch (DBExecutionException e) {

            fail("Operation threw an exception. " + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test executeGetModelsTask() method.
     * 
     * Test conditions: The GetModelsTask passed to the method is null.
     * 
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteGetModels_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelsTask task = null;

        try {

            /* XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetModelsTask(task);

            fail("Method should throw an exception since the task is null.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "the GetModelsTask object passed was null")) {
                assertTrue("model was not loaded because the task was null",
                        true);
            }

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test executeGetModelsTask() method.
     * 
     * Test conditions: The GetModelsTask passed to the method has no model name
     * in it.
     * 
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteGetModels_TaskWithNoModelName() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelsTask task = new GetModelsTask();

        try {

            /*XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetModelsTask(task);

            fail("Method should throw an exception since the task has no model name set.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "Could not find the model in the database")) {
                assertTrue("model was not loaded because the task was null",
                        true);
            }

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test executeGetModelsTask() method.
     * 
     * Test conditions: The model does not exist in the database.
     * 
     * @exception Exception thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testExecuteGetModels_ModelNotInDatabase() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelsTask task = new GetModelsTask();

        task.setModelName("model not in database");

        try {

            /*XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetModelsTask(task);

            fail("Method should throw an exception since the model does not exist in database.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "Could not find the model in the database")) {
                assertTrue("model was not loaded because the task was null",
                        true);
            }

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                                 //////

    private OracleXMLDBConnection _createConnWithoutTransaction()
            throws DBConnectionException {
        return _createConn(false);
    }

    private OracleXMLDBConnection _createConnWithTransaction()
            throws DBConnectionException {
        return _createConn(true);
    }

    private OracleXMLDBConnection _createConn(boolean tranReqd)
            throws DBConnectionException {
        DBConnectionParameters connectionParameters = DBConnectorFactory
                .getDBConnectionParameters();
        String url = connectionParameters.getUrl();
        String containerName = connectionParameters.getContainerName();
        boolean isTransactionRequired = tranReqd;

        DBConnectionParameters dbConnParams = new DBConnectionParameters(url,
                containerName, isTransactionRequired);
        dbConnParams.setUrl(url);
        dbConnParams.setContainerName(containerName);
        dbConnParams.setIsTransactionRequired(isTransactionRequired);

        OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);
        return conn;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                           //////

}
