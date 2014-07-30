/*
@Copyright (c) 2010-2014 The Regents of the University of California.
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
/*
 *
 */
package ptdb.kernel.database.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
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
import ptdb.common.dto.CreateAttributeTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.DeleteAttributeTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetFirstLevelParentsTask;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.GetReferenceStringTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.ModelNameSearchTask;
import ptdb.common.dto.PTDBGenericAttribute;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.UpdateAttributeTask;
import ptdb.common.dto.UpdateParentsToNewVersionTask;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.OracleXMLDBConnection;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;

import com.sleepycat.db.Environment;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlManager;

///////////////////////////////////////////////////////////////////
//// TestOracleXMLDBConnection

/**
 * Unit tests for OracleXMLDBConnection.
 *
 * @author Ashwini Bijwe, Yousef Alsaeed
 *
 * @version $Id: TestOracleXMLDBConnection.java 58206 2010-06-16 19:54:03Z
 * yalsaeed $
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
@PrepareForTest({ OracleXMLDBConnection.class, TestOracleXMLDBConnection.class })
@RunWith(PowerMockRunner.class)
public class TestOracleXMLDBConnection {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
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
     * @exception Exception
     */

    @Test
    public void testOracleXMLDBConnection() throws Exception {

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

        }
        ///////////////////////////////////////////////////////////////////
        /*
         * XMLException
         */
        connectionParameters = DBConnectorFactory.getDBConnectionParameters();

        Environment environment = PowerMock.createMock(Environment.class);
        XmlManager xmlManager = PowerMock.createMock(XmlManager.class);

        PowerMock.mockStaticPartial(OracleXMLDBConnection.class,
                "_getEnvironment");

        PowerMock.expectPrivate(OracleXMLDBConnection.class, "_getEnvironment")
                .andReturn(environment);

        PowerMock.expectNew(XmlManager.class, environment, null).andReturn(
                xmlManager);

        EasyMock.expect(
                xmlManager.openContainer(connectionParameters
                        .getContainerName())).andThrow(
                new XmlException(0, "Mock Error"));
        PowerMock.replayAll();
        try {
            new OracleXMLDBConnection(connectionParameters);
            fail("No XmlException thrown");
        } catch (DBConnectionException e) {
            assertTrue("Incorrect Exception",
                    e.getCause() instanceof XmlException);
        }
    }

    /**
     * Test method for
     * {@link ptdb.kernel.database.OracleXMLDBConnection#abortConnection()}.
     * @exception Exception
     */

    @Test
    public void testAbortConnection() throws Exception {

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
            if (conn != null) {
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

    @Test
    public void testExecuteGraphSearchTask_Port() throws DBConnectionException {
        OracleXMLDBConnection conn = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        try {
            GraphSearchTask task = new GraphSearchTask();
            task.setGraphSearchCriteria(new DBGraphSearchCriteria());
            ArrayList<XMLDBModel> list = conn.executeGraphSearchTask(task);
            assertTrue("Result is not null", list == null);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        }

        try {
            GraphSearchTask task = new GraphSearchTask();
            DBGraphSearchCriteria criteria = new DBGraphSearchCriteria();
            task.setGraphSearchCriteria(criteria);

            ArrayList<Port> portsList = new ArrayList<Port>();
            TypedIOPort ioPort1 = new TypedIOPort();
            ioPort1.setInput(true);
            portsList.add(ioPort1);

            TypedIOPort ioPort2 = new TypedIOPort();
            ioPort2.setOutput(true);
            portsList.add(ioPort2);

            TypedIOPort ioPort3 = new TypedIOPort();
            ioPort3.setOutput(true);
            ioPort3.setInput(true);
            portsList.add(ioPort3);

            TypedIOPort ioPort = new TypedIOPort();
            ioPort.setMultiport(true);
            portsList.add(ioPort);

            criteria.setPortsList(portsList);

            ArrayList<XMLDBModel> list = conn.executeGraphSearchTask(task);
            assertTrue("Result is null", list != null);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        } catch (IllegalActionException e) {
            fail("Failed with exception - " + e.getMessage());
        }

        conn.closeConnection();
    }

    @Test
    public void testExecuteGraphSearchTask_Actor() throws DBConnectionException {
        OracleXMLDBConnection conn = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        try {
            GraphSearchTask task = new GraphSearchTask();
            task.setGraphSearchCriteria(new DBGraphSearchCriteria());
            ArrayList<XMLDBModel> list = conn.executeGraphSearchTask(task);
            assertTrue("Result is not null", list == null);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        }

        try {
            GraphSearchTask task = new GraphSearchTask();
            DBGraphSearchCriteria criteria = new DBGraphSearchCriteria();
            task.setGraphSearchCriteria(criteria);

            ArrayList<ComponentEntity> entityList = new ArrayList<ComponentEntity>();
            ComponentEntity entity = PowerMock.createMock(AddSubtract.class);
            entityList.add(entity);
            criteria.setComponentEntitiesList(entityList);

            EasyMock.expect(entity.portList()).andReturn(null);
            EasyMock.expect(entity.getClassName())
                    .andReturn("ptolemy.actor.lib.AddSubtract").times(3);

            PowerMock.replayAll();

            ArrayList<XMLDBModel> list = conn.executeGraphSearchTask(task);
            PowerMock.verifyAll();

            assertTrue("Result is null", list != null);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        }
        conn.closeConnection();
    }

    @Test
    public void testExecuteUpdateParentsToNewVersion() throws Exception {
        OracleXMLDBConnection conn = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        UpdateParentsToNewVersionTask task = new UpdateParentsToNewVersionTask();
        try {
            conn.executeUpdateParentsToNewVersion(task);
            fail("No exception thrown.");
        } catch (Exception e) {

        }

        XMLDBModel oldModel = new XMLDBModel("MM");
        XMLDBModel newModel = new XMLDBModel("DD");

        task.setNewModel(newModel);
        try {
            conn.executeUpdateParentsToNewVersion(task);
            fail("No exception thrown.");
        } catch (Exception e) {

        }

        task.setOldModel(oldModel);
        try {
            conn.executeUpdateParentsToNewVersion(task);
            fail("No exception thrown.");
        } catch (Exception e) {

        }

        ArrayList<String> parentsList = new ArrayList<String>();
        parentsList.add("CC");
        task.setParentsList(parentsList);
        try {
            conn.executeUpdateParentsToNewVersion(task);
            GetReferenceStringTask task1 = new GetReferenceStringTask("CC");
            String referenceString = conn.executeGetReferenceStringTask(task1);

            assertTrue("Updation Incorrect - New model not found.",
                    referenceString.indexOf("DD") != -1);
            assertTrue("Updation Incorrect - Old model still present.",
                    referenceString.indexOf("MM") == -1);

            oldModel = new XMLDBModel("DD");
            newModel = new XMLDBModel("MM");

            task = new UpdateParentsToNewVersionTask();
            task.setNewModel(newModel);
            task.setOldModel(oldModel);
            parentsList = new ArrayList<String>();
            parentsList.add("CC");
            task.setParentsList(parentsList);

            conn.executeUpdateParentsToNewVersion(task);
            task1 = new GetReferenceStringTask("CC");

            referenceString = conn.executeGetReferenceStringTask(task1);

            assertTrue("Updation Incorrect - New model(MM) not found.",
                    referenceString.indexOf("MM") != -1);
            assertTrue("Updation Incorrect - Old model(DD) still present.",
                    referenceString.indexOf("DD") == -1);

            newModel = new XMLDBModel("DoesNotExist.xml", "DoesNotExist.xml");
            task.setNewModel(newModel);

            try {
                conn.executeUpdateParentsToNewVersion(task);
                fail("No exception thrown.");
            } catch (Exception e) {

            }

        } catch (DBExecutionException e) {
            fail("Failed with error - " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.abortConnection();
                conn.closeConnection();
            }
        }
    }

    @Test
    public void testExecuteGetFirstLevelParents() throws Exception {
        OracleXMLDBConnection conn = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);
        try {
            GetFirstLevelParentsTask task = new GetFirstLevelParentsTask(null);
            conn.executeGetFirstLevelParents(task);
            fail("No exception thrown when model was null.");
        } catch (Exception e) {

        }

        try {

            XMLDBModel model = new XMLDBModel("Adder");
            GetFirstLevelParentsTask task = new GetFirstLevelParentsTask(model);
            List<XMLDBModel> parentsList = conn
                    .executeGetFirstLevelParents(task);

            assertTrue("No parents returned.", parentsList != null);

            assertTrue("Invalid parents returned - "
                    + parentsList.get(0).getModelName(),
                    "newModel".equals(parentsList.get(0).getModelName()));

            assertTrue("Invalid parents returned - "
                    + parentsList.get(0).getModelId(),
                    "newModel_1279245716984".equals(parentsList.get(0)
                            .getModelId()));

            model = new XMLDBModel("Adder");
            task = new GetFirstLevelParentsTask(model);
            parentsList = conn.executeGetFirstLevelParents(task);

            assertTrue("No parents returned. " + parentsList.size(),
                    parentsList != null && parentsList.size() == 3);

            model = new XMLDBModel("ModelB");
            task = new GetFirstLevelParentsTask(model);
            parentsList = conn.executeGetFirstLevelParents(task);

            assertTrue("Parents returned when it should be null.",
                    parentsList == null || parentsList.size() == 0);
        } catch (DBExecutionException e) {
            fail("Failed with exeception - " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.closeConnection();
            }
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
        PTDBGenericAttribute createdByattribute = new PTDBGenericAttribute(
                "ptolemy.data.expr.StringParameter");

        //        Variable variableCreatedBy = new Variable();
        //        createdByattribute.setClassName("ptolemy.data.expr.StringParameter");
        createdByattribute.setAttributeName("CreatedBy");

        ArrayList<String> values = new ArrayList<String>();
        values.add("Ashwini Bijwe");
        createdByattribute.setValues(values);

        PTDBGenericAttribute modelIdAttribute = new PTDBGenericAttribute(
                "ptolemy.data.expr.Parameter");

        //        Variable variableModelId = new Variable();
        //        modelIdAttribute.setClassName("ptolemy.data.expr.Parameter");
        modelIdAttribute.setAttributeName("ModelId");

        ArrayList<String> modelIdValues = new ArrayList<String>();

        modelIdValues.add("13781");

        modelIdAttribute.setValues(modelIdValues);

        task.addAttribute(createdByattribute);
        task.addAttribute(modelIdAttribute);

        try {

            ArrayList<XMLDBModel> modelsList = conn
                    .executeAttributeSearchTask(task);
            assertTrue("No results returned.", modelsList.size() > 0);
            String modelName = modelsList.get(0).getModelName();
            assertTrue(modelName + " - Wrong model returned.",
                    "ModelContainsBothAttributes".equals(modelName));

            ///////////////////////////////////////////////////////////////////////////////////////
            //IllegalActionException
            try {
                OracleXMLDBConnection mockConn = PowerMock.createPartialMock(
                        OracleXMLDBConnection.class, "_createAttributeClause",
                        "_executeSingleAttributeMatch");

                PowerMock.expectPrivate(mockConn, "_createAttributeClause",
                        OracleXMLDBConnection.class, createdByattribute)
                        .andReturn("Test String");

                PowerMock.expectPrivate(mockConn, "_createAttributeClause",
                        OracleXMLDBConnection.class, modelIdAttribute)
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
            task.setAttributesList(new ArrayList<PTDBGenericAttribute>());
            modelsList = conn.executeAttributeSearchTask(task);
            assertTrue("Search was performed without attributes list.",
                    modelsList == null);

            PTDBGenericAttribute multiValueAttribute = new PTDBGenericAttribute(
                    "ptolemy.data.expr.StringParameter");

            //            multiValueAttribute.setClassName("ptolemy.data.expr.StringParameter");

            multiValueAttribute.setAttributeName("DBModelId");

            modelIdValues = new ArrayList<String>();

            modelIdValues.add("Y");
            modelIdValues.add("ModelB");

            multiValueAttribute.setValues(modelIdValues);

            ArrayList<PTDBGenericAttribute> attributesList = new ArrayList<PTDBGenericAttribute>();
            attributesList.add(multiValueAttribute);
            task.setAttributesList(attributesList);

            modelsList = conn.executeAttributeSearchTask(task);
            assertTrue("Number of models should be 2.", modelsList.size() == 2);

            PTDBGenericAttribute classAndNameOnlyAttribute = new PTDBGenericAttribute(
                    "ptolemy.data.expr.StringParameter");

            //            classAndNameOnlyAttribute.setClassName("ptolemy.data.expr.StringParameter");

            classAndNameOnlyAttribute.setAttributeName("DBModelId");

            attributesList = new ArrayList<PTDBGenericAttribute>();
            attributesList.add(classAndNameOnlyAttribute);
            task.setAttributesList(attributesList);

            modelsList = conn.executeAttributeSearchTask(task);
            assertTrue("Number of models should be greater than 0.",
                    modelsList.size() > 0);

            PTDBGenericAttribute nameAndValueAttribute = new PTDBGenericAttribute(
                    "DBModelId");

            //            nameAndValueAttribute.setAttributeName("DBModelId");

            modelIdValues = new ArrayList<String>();

            modelIdValues.add("X");
            modelIdValues.add("ModelB");

            nameAndValueAttribute.setValues(modelIdValues);

            attributesList = new ArrayList<PTDBGenericAttribute>();
            attributesList.add(nameAndValueAttribute);
            task.setAttributesList(attributesList);

            modelsList = conn.executeAttributeSearchTask(task);
            assertTrue("Number of models should be greater than 0.",
                    modelsList.size() > 0);

            PTDBGenericAttribute nameOnlyAttribute = new PTDBGenericAttribute(
                    "DBModelId");

            //            nameOnlyAttribute.setAttributeName("DBModelId");

            attributesList = new ArrayList<PTDBGenericAttribute>();
            attributesList.add(nameOnlyAttribute);
            task.setAttributesList(attributesList);

            modelsList = conn.executeAttributeSearchTask(task);
            assertTrue("Number of models should be greater than 0.",
                    modelsList.size() > 0);

            PTDBGenericAttribute valueOnlyAttribute = new PTDBGenericAttribute(
                    null);

            modelIdValues = new ArrayList<String>();

            modelIdValues.add("X");
            modelIdValues.add("ModelB");

            valueOnlyAttribute.setValues(modelIdValues);

            attributesList = new ArrayList<PTDBGenericAttribute>();
            attributesList.add(valueOnlyAttribute);
            task.setAttributesList(attributesList);

            modelsList = conn.executeAttributeSearchTask(task);
            assertTrue("Number of models should be greater than 0.",
                    modelsList.size() > 0);

            PTDBGenericAttribute classOnlyAttribute = new PTDBGenericAttribute(
                    null);

            classOnlyAttribute
                    .setClassName("ptolemy.data.expr.StringParameter");

            attributesList = new ArrayList<PTDBGenericAttribute>();
            attributesList.add(classOnlyAttribute);
            task.setAttributesList(attributesList);

            modelsList = conn.executeAttributeSearchTask(task);
            assertTrue("Number of models should be greater than 0.",
                    modelsList.size() > 0);

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
        XMLDBModel dbModel = new XMLDBModel("Adder");
        dbModel.setModelId("Adder");
        ArrayList<XMLDBModel> list = new ArrayList<XMLDBModel>();
        list.add(dbModel);
        task.setModelsList(list);

        try {
            list = conn.executeFetchHierarchyTask(task);
            assertTrue("No models returned", list.size() > 0);
            dbModel = list.get(0);
            int hierarchySize = dbModel.getParents().size();

            assertTrue("Wrong number of hierarchies returned - "
                    + hierarchySize, hierarchySize == 3);

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
            assertTrue("Hierarchies returned when they should be null",
                    dbModel.getParents() == null
                            || dbModel.getParents().size() == 0);

        } catch (DBExecutionException e) {
            e.printStackTrace();
            fail("Failed with exception - " + e.getMessage());
        }
    }

    /**
     * Test the executCreateTask() method.
     * <p>Conditions for the test:
     * <br>The model being saved is a new model.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_NewModel() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        java.util.Date time = new java.util.Date();

        XMLDBModel xmlModel = new XMLDBModel(String.valueOf(time.getTime()));

        xmlModel.setIsNew(true);

        xmlModel.setModel("<entity name=\"new_test2001\" "
                + "class=\"test.class\"></entity>");

        xmlModel.setReferencedChildren(new ArrayList<String>());

        CreateModelTask task = new CreateModelTask(xmlModel);

        try {
            oracleXMLDBConnection.executeCreateModelTask(task);
            assertTrue("Model was created", true);

        } catch (DBExecutionException e) {
            fail("Exception thrown - " + e.getMessage());
        } finally {

            if (oracleXMLDBConnection != null) {

                oracleXMLDBConnection.abortConnection();
            }
        }

    }

    /**
     * Test the executCreateTask() method.
     * <p>Conditions for the test:
     * <br>The model to be created already exist in the database.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_ExistingModel() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBModel xmlModel = new XMLDBModel("Y");

        xmlModel.setIsNew(true);

        xmlModel.setModel("<entity name=\"Y\" "
                + "class=\"test.class\"></entity>");

        CreateModelTask task = new CreateModelTask(xmlModel);

        try {
            oracleXMLDBConnection.executeCreateModelTask(task);
            oracleXMLDBConnection.commitConnection();

            fail("Model was created when it should be already there.");

        } catch (ModelAlreadyExistException e) {
            oracleXMLDBConnection.abortConnection();
            if (e.getMessage().contains("The model already exist")) {
                assertTrue("model was not created because it already exists",
                        true);
            }

        } catch (DBExecutionException e) {

            oracleXMLDBConnection.abortConnection();
            fail("The wrong exception was thrwon" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executCreateTask() method.
     * <p>Conditions for the test:
     * <br>The create model task is null.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        CreateModelTask task = null;

        try {
            // FindBugs complains about this, which we can ignore.
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

    @Test
    public void testExecuteModelNameSearchTask() throws DBConnectionException {
        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        try {
            ArrayList<XMLDBModel> list = oracleXMLDBConnection
                    .executeModelNameSearchTask(new ModelNameSearchTask("Adder"));
            assertTrue("Model not returned. ", list.size() > 0);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        }

        try {
            ArrayList<XMLDBModel> list = oracleXMLDBConnection
                    .executeModelNameSearchTask(new ModelNameSearchTask(
                            "AdderDoesNotExist"));
            assertTrue("Model returned.", list.size() == 0);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        }

        try {
            ArrayList<XMLDBModel> list = oracleXMLDBConnection
                    .executeModelNameSearchTask(new ModelNameSearchTask("model"));
            assertTrue("Model returned.", list.size() > 1);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        }
    }

    /*@Test
    public void testUpdateReferenceFile() throws DBConnectionException {
    //        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
    //        .getSyncConnection(true);
    //        try {
    //            oracleXMLDBConnection._updateReferenceFile(new XMLDBModel("Test"));
    //            fail("No Exception thrown");
    //        } catch (DBExecutionException e) {
    //
    //        }

        try {
            XMLDBModel existingModel = new XMLDBModel("D.xml");
            existingModel.setIsNew(false);
            existingModel.setModelId("D.xml");
            existingModel.setReferencedChildren(new ArrayList<String>());
            oracleXMLDBConnection._updateReferenceFile(existingModel);

        } catch (DBExecutionException e) {
            fail("Failed with xception - " + e.getMessage());
        }

        try {
            XMLDBModel newModel = new XMLDBModel("NewD.xml");
            newModel.setIsNew(true);
            newModel.setModelId("NewD.xml");
            newModel.setReferencedChildren(new ArrayList<String>());
            oracleXMLDBConnection._updateReferenceFile(newModel);

        } catch (DBExecutionException e) {
            fail("Failed with xception - " + e.getMessage());
        }

        try {
            XMLDBModel newModelWithChildren = new XMLDBModel("NewD1.xml");
            newModelWithChildren.setIsNew(true);
            newModelWithChildren.setModelId("NewD1.xml");
            newModelWithChildren.addReferencedChild("X");
            oracleXMLDBConnection._updateReferenceFile(newModelWithChildren);

        } catch (DBExecutionException e) {
            fail("Failed with exception - " + e.getMessage());
        }

        try {
            XMLDBModel existingModelWithChildren = new XMLDBModel("D.xml");
            existingModelWithChildren.setIsNew(false);
            existingModelWithChildren.setModelId("D.xml");
            existingModelWithChildren.addReferencedChild("X");
            oracleXMLDBConnection._updateReferenceFile(existingModelWithChildren);

        } catch (DBExecutionException e) {
            fail("Failed with Exception - " + e.getMessage());
        }
        oracleXMLDBConnection.abortConnection();
        oracleXMLDBConnection.closeConnection();
    }*/
    /**
     * Test the doesModelExist() method.
     * @exception DBConnectionException If thrown while creating a connection.
     */

    @Test
    public void testDoesModelExist() throws DBConnectionException {
        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        try {
            XMLDBModel existingModel = new XMLDBModel("Y");
            boolean doesExist = oracleXMLDBConnection
                    .doesModelExist(existingModel);
            assertTrue("An existing model is flagged as does not exist.",
                    doesExist == true);
        } catch (DBExecutionException e) {
            fail("Test failed with exception - " + e.getMessage());
        }

        try {
            XMLDBModel nonExistingModel = new XMLDBModel("X_doesnotExist");
            boolean doesExist = oracleXMLDBConnection
                    .doesModelExist(nonExistingModel);
            assertTrue("A non-existing model is flagged existing.",
                    doesExist == false);
        } catch (DBExecutionException e) {
            fail("Test failed with exception - " + e.getMessage());
        }

        try {
            oracleXMLDBConnection.doesModelExist(null);
            fail("No exception was thrown for null model.");
        } catch (DBExecutionException e) {

        }

        try {
            oracleXMLDBConnection.doesModelExist(new XMLDBModel("Test's"));
        } catch (DBExecutionException e) {

        }
    }

    /**
     * Test the executCreateTask() method.
     * <p>Conditions for the test:
     * <br>The model in the task is null.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateTask_NullModelInTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        CreateModelTask task = new CreateModelTask(null);

        try {
            oracleXMLDBConnection.executeCreateModelTask(task);

            fail("Model was created when it should not be.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "the XMLDBModel object passed in the "
                            + "CreateModelTask was null")) {

                assertTrue("model was not created because the model in the "
                        + "task was null.", true);
            }
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executSaveTask() method.
     * <p>Conditions for the test:
     * <br>The model being saved is a new model.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_NewModel() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        java.util.Date time = new java.util.Date();

        XMLDBModel xmlModel = new XMLDBModel(String.valueOf(time.getTime()));

        xmlModel.setIsNew(true);

        xmlModel.setModel("<entity name=\"test5\" "
                + "class=\"test.class\"></entity>");

        SaveModelTask task = new SaveModelTask(xmlModel);

        try {
            oracleXMLDBConnection.executeSaveModelTask(task);

            fail("Model should not be saved");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "the model does not exist in " + "the database.")) {

                assertTrue("model was not updated since it does not exist "
                        + "in the database.", true);
            }
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executSaveTask() method.
     * <p>Conditions for the test:
     * <br>The model to be saved already exist in the database.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_ExistingModel() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBModel xmlModel = new XMLDBModel("test2");

        xmlModel.setIsNew(false);

        xmlModel.setModel("<entity name=\"test2\" "
                + "class=\"test1.class\"></entity>");

        xmlModel.setReferencedChildren(new ArrayList<String>());

        //try to create the model first... so that the test case works.
        CreateModelTask createTask = new CreateModelTask(xmlModel);

        try {

            oracleXMLDBConnection.executeCreateModelTask(createTask);

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        SaveModelTask task = new SaveModelTask(xmlModel);

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
     * Test the executSaveTask() method.
     * <p>Conditions for the test:
     * <br>The save model task is null.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        SaveModelTask task = null;

        try {
            // FindBugs complains about this, which we can ignore.
            oracleXMLDBConnection.executeSaveModelTask(task);

            fail("Model was saved when it should not be.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "the SaveModelTask object passed " + "was null")) {

                assertTrue("model was not saved because the task is null", true);
            }
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executSaveTask() method.
     * <p>Conditions for the test:
     * <br>The model in the task is null.</p>
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteSaveTask_NullModelInTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        SaveModelTask task = new SaveModelTask(null);

        try {
            oracleXMLDBConnection.executeSaveModelTask(task);

            fail("Model was created when it should not be.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "the XMLDBModel object passed in the "
                            + "SaveModelTask was null")) {

                assertTrue("model was not saved because the model in "
                        + "the task was null.", true);
            }
        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executeGetModelTask() method.
     *
     * <p>Test conditions:
     * <br>The model exist in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetModelTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        String modelName = "NoReferences";

        GetModelTask task = new GetModelTask(modelName);

        try {

            XMLDBModel model = oracleXMLDBConnection.executeGetModelTask(task);

            if (model != null && model.getModelName().equals(modelName)) {

                assertTrue("Model was retrieved successfully.", true);

            } else if (model != null && !model.getModelName().equals(modelName)) {

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
     * Test the executeGetModelTask() method.
     *
     * <p>Test conditions:
     * <br>The model exist in the database and the model id is passed.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetModelTask_ModelId() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        String modelId = "Y";

        GetModelTask task = new GetModelTask(null);
        task.setModelId(modelId);

        try {

            XMLDBModel model = oracleXMLDBConnection.executeGetModelTask(task);

            if (model != null && model.getModelName().equals("Y")) {

                assertTrue("Model was retrieved successfully.", true);

            } else if (model != null && !model.getModelName().equals("Y")) {

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
     * Test the executeGetModelTask() method.
     *
     * <p>Test conditions:
     * <br>The model does not exist in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteModelTask_NotExist() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        String modelName = "Not In DB";

        GetModelTask task = new GetModelTask(modelName);

        try {

            XMLDBModel model = oracleXMLDBConnection.executeGetModelTask(task);

            if (model != null && model.getModelName().equals(modelName)) {

                //System.out.println(model.getModel());
                fail("The model should not be in the database...");

            } else if (model != null && !model.getModelName().equals(modelName)) {

                fail("Different model was retrieved. " + model.getModelName());

            } else {

                fail("Operation should throw an exception.");
            }

        } catch (DBExecutionException e) {

            if (e.getMessage().contains("Could not find the model")) {
                assertTrue("", true);
            } else {

                fail("Operation threw an exception. " + e.getMessage());

            }

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }
    }

    /**
     * Test the executeGetModelTask() method.
     *
     * <p>Test conditions:
     * <br>The task is null.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteModelTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelTask task = null;

        try {

            /* XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetModelTask(task);

            fail("method should throw an exception");

        } catch (NullPointerException e) {

            assertTrue("", true);

        } catch (DBExecutionException e) {

            fail("Operation threw an exception. " + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }
    }

    /**
     * Test the executeGetModelTask() method.
     *
     * <p>Test conditions:
     * <br>The model name is empty</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteModelTask_EmptyModeName() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelTask task = new GetModelTask("");

        try {

            /* XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetModelTask(task);

            fail("method should throw an exception");

        } catch (NullPointerException e) {

            assertTrue("", true);

        } catch (DBExecutionException e) {

            if (e.getMessage().contains("<empty name>")) {

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
     * Test the executeGetModelTask() method.
     *
     * <p>Test conditions:
     * <br>The model exist in the database and the model id is passed.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetModelTask_noNameOrId() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelTask task = new GetModelTask(null);

        try {

            /* XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetModelTask(task);

            fail("Method should throw an exception since the task does not contain enough information.");
        } catch (DBExecutionException e) {

            assertTrue(true);

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }
    }

    /**
     * Test the executeGetCompleteModelTask() method.
     *
     * <p>Test conditions:
     * <br>The model exist in the database and does not have references.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetCompleteModel_ModelWithNoReferences()
            throws Exception {

        String modelName = "NoReferences";

        GetModelTask task = new GetModelTask(modelName);

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);
        try {

            XMLDBModel model = oracleXMLDBConnection
                    .executeGetCompleteModelTask(task);

            if (model != null && model.getModelName().equals(modelName)) {

                //System.out.println(model.getModel());
                assertTrue("Model was retrieved successfully.", true);

            } else if (model != null && !model.getModelName().equals(modelName)) {

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
     * Test the executeGetCompleteModelTask() method.
     *
     * <p>Test conditions:
     * <br>The model exist in the database and has references in it.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetCompleteModel_ModelWithReferences()
            throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        String modelName = "modelWithTwoAdders";

        GetModelTask task = new GetModelTask(modelName);

        try {

            XMLDBModel model = oracleXMLDBConnection
                    .executeGetCompleteModelTask(task);

            if (model != null && model.getModelName().equals(modelName)) {

                // System.out.println(model.getModel());
                assertTrue("Model was retrieved successfully.", true);

            } else if (model != null && !model.getModelName().equals(modelName)) {

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
     * Test the executeGetModelsTask() method.
     *
     * <p>Test conditions:
     * <br>The GetModelsTask passed to the method is null.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetModels_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelTask task = null;

        try {

            /* XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetCompleteModelTask(task);

            fail("Method should throw an exception since the task is null.");

        } catch (NullPointerException e) {

            assertTrue("Method threw the right exception", true);

        } catch (DBExecutionException e) {

            fail("Method threw the wrong exception" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executeGetModelsTask() method.
     *
     * <p>Test conditions:
     * <br>The GetModelsTask passed to the method has no model name in it.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetModels_TaskWithNoModelName() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelTask task = new GetModelTask(null);

        try {

            /*XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetCompleteModelTask(task);

            fail("Method should throw an exception since the task has no model name set.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains("<empty name>")) {

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
     * Test the executeGetModelsTask() method.
     *
     * <p>Test conditions:
     * <br>The model does not exist in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetModels_ModelNotInDatabase() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetModelTask task = new GetModelTask("model not in database");

        try {

            /*XMLDBModel model =*/oracleXMLDBConnection
                    .executeGetCompleteModelTask(task);

            fail("Method should throw an exception since the model does not "
                    + "exist in database.");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains("Could not find the model")) {
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
     * Test the executeCreateAttribueTask() method.
     *
     * <p>Test conditions:
     * <br>The attribute is of type string and it is new.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateAttributeTask_String() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        java.util.Date time = new java.util.Date();

        XMLDBAttribute attribute = new XMLDBAttribute("author_"
                + String.valueOf(time.getTime()),
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, null);

        CreateAttributeTask task = new CreateAttributeTask(attribute);

        try {

            XMLDBAttribute newAttribute = oracleXMLDBConnection
                    .executeCreateAttributeTask(task);

            assertTrue("Method successfully created attribute", true);

            DeleteAttributeTask deleteTask = new DeleteAttributeTask(
                    newAttribute);
            oracleXMLDBConnection.executeDeleteAttributeTask(deleteTask);

        } catch (DBExecutionException e) {

            fail("Failed to create Attribute" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executeCreateAttribueTask() method.
     *
     * <p>Test conditions:
     * <br>The attribute is of type list and it is new.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateAttributeTask_List() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        java.util.Date time = new java.util.Date();
        XMLDBAttribute attribute = new XMLDBAttribute("countries_"
                + String.valueOf(time.getTime()),
                XMLDBAttribute.ATTRIBUTE_TYPE_LIST, null);

        List<String> attributeValues = new ArrayList<String>();

        attributeValues.add("India");
        attributeValues.add("China");
        attributeValues.add("Saudi Arabia");
        attributeValues.add("United States");
        attribute.setAttributeValue(attributeValues);
        CreateAttributeTask task = new CreateAttributeTask(attribute);

        try {

            XMLDBAttribute newAttribute = oracleXMLDBConnection
                    .executeCreateAttributeTask(task);

            assertTrue("Method successfully created attribute", true);

            DeleteAttributeTask deleteTask = new DeleteAttributeTask(
                    newAttribute);

            oracleXMLDBConnection.executeDeleteAttributeTask(deleteTask);

        } catch (DBExecutionException e) {

            fail("Failed to create Attribute" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    /**
     * Test the executeCreateAttribueTask() method.
     *
     * <p>Test conditions:
     * <br>The attribute is already in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateAttributeTask_AlreadyExists() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = new XMLDBAttribute("author",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, null);

        CreateAttributeTask task = new CreateAttributeTask(attribute);

        try {

            XMLDBAttribute created = oracleXMLDBConnection
                    .executeCreateAttributeTask(task);

            CreateAttributeTask alreadyExist = new CreateAttributeTask(created);

            oracleXMLDBConnection.executeCreateAttributeTask(alreadyExist);

            fail("The method created the attirbute when it should throw an exception");

        } catch (DBExecutionException e) {

            if (e.getMessage().contains(
                    "An attribute with the " + "same name already exist")) {
                assertTrue("Method threw the right exception.", true);
            } else {
                fail("Method threw the wrong exception" + e.getMessage());
            }

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }

    }

    /**
     * Test the executeCreateAttribueTask() method.
     *
     * <p>Test conditions:
     * <br>Null task was provided.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateAttributeTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        CreateAttributeTask task = null;

        try {

            oracleXMLDBConnection.executeCreateAttributeTask(task);

            fail("The method returned without any errors when it should "
                    + "throw an exception");

        } catch (NullPointerException e) {

            assertTrue("Method threw the right exception", true);

        } catch (DBExecutionException e) {

            fail("Method threw the wrong exception" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }

    }

    /**
     * Test the executeCreateAttribueTask() method.
     *
     * <p>Test conditions:
     * <br>Null attribute was provided.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteCreateAttributeTask_NullAttribute() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = null;
        CreateAttributeTask task = new CreateAttributeTask(attribute);

        try {

            oracleXMLDBConnection.executeCreateAttributeTask(task);

            fail("The method returned without any errors when it should "
                    + "throw an exception");

        } catch (NullPointerException e) {

            assertTrue("Method threw the right exception", true);

        } catch (DBExecutionException e) {

            fail("Method threw the wrong exception" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }

    }

    /**
     * Test the executeGetAttributesTask() method.
     *
     * <p>Test conditions:
     * <br>There are valid attributes in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetAttributesTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(false);

        GetAttributesTask task = new GetAttributesTask();

        try {

            ArrayList<XMLDBAttribute> attributesList = oracleXMLDBConnection
                    .executeGetAttributesTask(task);

            assertTrue("The method returned the expected results",
                    (attributesList != null && attributesList.size() > 0));
            //             for (int i = 0; i < attributesList.size(); i++) {
            //                 XMLDBAttribute attribute = (XMLDBAttribute) attributesList.get(i);

            //                 System.out.println(attribute.getAttributeXMLStringFormat());
            //             }

        } catch (DBExecutionException e) {
            fail("Method threw and exception - " + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }

    }

    //    /**
    //     * Test the executeGetAttributesTask() method.
    //     *
    //     * <p>Test conditions:
    //     * <br>Attribues.ptdbxml document does not exist in the database.</p>
    //     *
    //     * @exception Exception Thrown if the test fails and the exception was not handled.
    //     */
    //    @Test
    //    public void testExecuteGetAttributesTask_DocumentDoesNotExist() throws Exception {
    //
    //
    //        Environment environmentMock = PowerMock.createMock(Environment.class);
    //        XmlContainer xmlContainerMock = PowerMock.createMock(XmlContainer.class);
    //        XmlManager xmlManagerMock =  PowerMock.createMock(XmlManager.class);
    //
    //
    //        //Object [] parameters = {Environment.class, XmlManagerConfig.class};
    //
    ////        PowerMock.expectNew(XmlManager.class, Environment.class, null).andReturn(
    ////                xmlManagerMock);
    //
    //
    //
    //        EasyMock.expect(xmlManagerMock.openContainer("temp.dbxml")).andReturn(xmlContainerMock);
    //
    //        EasyMock.expect(xmlContainerMock.getDocument("Attributes.ptdbxml")).andReturn(
    //                null);
    //
    //        PowerMock.replayAll();
    //
    //
    //        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
    //                .getSyncConnection(false);
    //
    //
    //        GetAttributesTask task = new GetAttributesTask();
    //
    //
    //        try {
    //
    //
    //            ArrayList<XMLDBAttribute> attributesList = oracleXMLDBConnection
    //                    .executeGetAttributesTask(task);
    //
    //            fail("No exception was thrown");
    //
    //
    //
    //        } catch (DBExecutionException e) {
    //            if (e.getMessage().contains("Could not fetch the Attribute.ptdbxml.")) {
    //                assertTrue("Method threw the right exception", true);
    //            } else {
    //                fail("Wrong exception was thrown" + e.getMessage());
    //            }
    //
    //
    //        } finally {
    //            if (oracleXMLDBConnection != null) {
    //                oracleXMLDBConnection.closeConnection();
    //            }
    //        }
    //    }

    /**
     * Test the executeDeleteAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>Simple Attribute the exists in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteDeleteAttributeTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = new XMLDBAttribute("author_test",
                XMLDBAttribute.ATTRIBUTE_TYPE_STRING, "a_testId");

        DeleteAttributeTask task = new DeleteAttributeTask(attribute);

        try {

            oracleXMLDBConnection.executeDeleteAttributeTask(task);

            assertTrue("Method was successful.", true);

        } catch (DBExecutionException e) {

            fail("Wrong exception was thrown" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }
    }

    /**
     * Test the executeDeleteAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>A list attribute that exists in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteDeleteAttributeTask_List() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = new XMLDBAttribute("Countries_test",
                XMLDBAttribute.ATTRIBUTE_TYPE_LIST, "c_testId");

        DeleteAttributeTask task = new DeleteAttributeTask(attribute);

        try {

            oracleXMLDBConnection.executeDeleteAttributeTask(task);

            assertTrue("Method was successful.", true);

        } catch (DBExecutionException e) {

            fail("Wrong exception was thrown" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }
    }

    /**
     * Test the executeDeleteAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>The task is null.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteDeleteAttributeTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        /*XMLDBAttribute attribute = */new XMLDBAttribute("Countries",
                XMLDBAttribute.ATTRIBUTE_TYPE_LIST, "testId");

        DeleteAttributeTask task = null;

        try {

            oracleXMLDBConnection.executeDeleteAttributeTask(task);

            fail("Method did not throw exception");

        } catch (NullPointerException e) {

            assertTrue("Method threw the right exception", true);

        } catch (DBExecutionException e) {
            fail("Wrong exception was thrown" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }
    }

    /**
     * Test the executeDeleteAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>The attribute object is null.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteDeleteAttributeTask_NullAttribute() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = null;

        DeleteAttributeTask task = new DeleteAttributeTask(attribute);

        try {

            oracleXMLDBConnection.executeDeleteAttributeTask(task);

            fail("Method did not throw exception");

        } catch (NullPointerException e) {

            assertTrue("Method threw the right exception", true);

        } catch (DBExecutionException e) {
            fail("Wrong exception was thrown" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }
    }

    /**
     * Test the executeUpdateAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>The attribute exist in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteUpdateAttributeTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = new XMLDBAttribute("author",
                XMLDBAttribute.ATTRIBUTE_TYPE_BOOLEAN, "author_1277754213656");

        UpdateAttributeTask task = new UpdateAttributeTask(attribute);

        try {

            oracleXMLDBConnection.executeUpdateAttributeTask(task);

            assertTrue("Method returned without exceptions", true);

        } catch (DBExecutionException e) {
            fail("Exception was thrown - " + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }
    }

    /**
     * Test the executeUpdateAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>The attribute does not exist in the database.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteUpdateAttributeTask_DoesNotExist() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = new XMLDBAttribute("UpdatedCountry",
                XMLDBAttribute.ATTRIBUTE_TYPE_LIST, "country_123");

        UpdateAttributeTask task = new UpdateAttributeTask(attribute);

        try {

            oracleXMLDBConnection.executeUpdateAttributeTask(task);

            fail("Method did not throw exception");

        } catch (DBExecutionException e) {
            assertTrue("Method threw the right exception", true);

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.abortConnection();
            }
        }
    }

    /**
     * Test the executeUpdateAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>The task is null.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteUpdateAttributeTask_NullTask() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        UpdateAttributeTask task = null;

        try {

            oracleXMLDBConnection.executeUpdateAttributeTask(task);

            fail("Method did not throw exception");

        } catch (NullPointerException e) {

            assertTrue("Method threw the right exception", true);

        } catch (DBExecutionException e) {

            fail("Wrong exception was thrown" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }
    }

    /**
     * Test the executeUpdateAttributeTask() method.
     *
     * <p>Test conditions:
     * <br>The attribute object is null.</p>
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteUpdateAttributeTask_NullAttribute() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        XMLDBAttribute attribute = null;

        UpdateAttributeTask task = new UpdateAttributeTask(attribute);

        try {

            oracleXMLDBConnection.executeUpdateAttributeTask(task);

            fail("Method did not throw exception");

        } catch (NullPointerException e) {

            assertTrue("Method threw the right exception", true);

        } catch (DBExecutionException e) {

            fail("Wrong exception was thrown" + e.getMessage());

        } finally {
            if (oracleXMLDBConnection != null) {
                oracleXMLDBConnection.closeConnection();
            }
        }
    }

    /**
     * Test executeGetListOfAllModels method.
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testExecuteGetListOfAllModels() throws Exception {

        OracleXMLDBConnection oracleXMLDBConnection = (OracleXMLDBConnection) DBConnectorFactory
                .getSyncConnection(true);

        try {

            List<XMLDBModel> allModels = oracleXMLDBConnection
                    .executeGetListOfAllModels();

            assertTrue("Is number of models returned greater than ZERO?",
                    allModels.size() > 0);

        } catch (Exception e) {

            fail("Method threw an Exception - " + e.getMessage());
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
        DBConnectionParameters dbConnParams = getDBConnectionParameters(tranReqd);
        OracleXMLDBConnection conn = new OracleXMLDBConnection(dbConnParams);
        return conn;
    }

    private DBConnectionParameters getDBConnectionParameters(boolean tranReqd) {
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

        return dbConnParams;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
