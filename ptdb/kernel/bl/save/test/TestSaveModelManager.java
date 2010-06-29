package ptdb.kernel.bl.save.test;

import static org.junit.Assert.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.bl.save.SaveModelManager;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// TestSaveModelManager

/**
 * JUnit test case for testing SaveModelManager class.
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (yalsaeed)
 * @Pt.AcceptedRating red (yalsaeed)
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { SaveModelManager.class, DBConnection.class,
        DBConnectorFactory.class, CreateModelTask.class, SaveModelTask.class,
        DBExecutionException.class })
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestSaveModelManager {

    ///////////////////////////////////////////////////////////////////
    ////                public methods                            ////

    /**
     * Test the SaveManager.save() method.
     * <p>
     * The condition for this test case:<br/>
     * 
     * - The model being saved is a new model and should be created in the
     * database.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_CreateModel() throws Exception {

        SaveModelManager saveManager = new SaveModelManager();

        PowerMock.mockStatic(DBConnectorFactory.class);


        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);


        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);


        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);


        EasyMock.expect(modelMock.getIsNew()).andReturn(true);


        CreateModelTask createModelTaskMock = PowerMock
                .createMock(CreateModelTask.class);

        PowerMock.expectNew(CreateModelTask.class, modelMock).andReturn(
                createModelTaskMock);

        //createModelTaskMock.setXMLDBModel(modelMock);

        dBConnectionMock.executeCreateModelTask(createModelTaskMock);

        dBConnectionMock.commitConnection();


        dBConnectionMock.closeConnection();

        PowerMock.replayAll();


        boolean isSuccess = saveManager.save(modelMock);


        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method.
     * <p>
     * The condition for this test case:
     * 
     * <br>- The model being saved is a new model and should be created in the
     * database. 
     * <br>- The executeCreateModelTask method throws exception.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_CreateModelNotSuccessful() throws Exception {


        SaveModelManager saveManager = new SaveModelManager();


        PowerMock.mockStatic(DBConnectorFactory.class);


        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);

        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);


        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);


        EasyMock.expect(modelMock.getIsNew()).andReturn(true);


        CreateModelTask createModelTaskMock = PowerMock
                .createMock(CreateModelTask.class);

        PowerMock.expectNew(CreateModelTask.class, modelMock).andReturn(
                createModelTaskMock);


        //createModelTaskMock.setXMLDBModel(modelMock);


        dBConnectionMock.executeCreateModelTask(createModelTaskMock);


        PowerMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() throws DBExecutionException {

                throw new DBExecutionException("Message");

            }
        });

        dBConnectionMock.closeConnection();


        dBConnectionMock.abortConnection();

        PowerMock.replayAll();


        boolean isSuccess = false;

        try {

            saveManager.save(modelMock);

        } catch (DBExecutionException e) {

            isSuccess = true;
        }


        assertTrue(isSuccess);


        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method.
     * <p>
     * The condition for this test case:
     * 
     * <br>- The model being saved is an existing model and should be updated in
     * the database.
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_SaveModel() throws Exception {


        SaveModelManager saveManager = new SaveModelManager();


        PowerMock.mockStatic(DBConnectorFactory.class);


        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);


        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);


        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);


        EasyMock.expect(modelMock.getIsNew()).andReturn(false);

        SaveModelTask saveModelTaskMock = PowerMock
                .createMock(SaveModelTask.class);

        PowerMock.expectNew(SaveModelTask.class, modelMock).andReturn(saveModelTaskMock);


        //saveModelTaskMock.setXMLDBModel(modelMock);


        dBConnectionMock.executeSaveModelTask(saveModelTaskMock);

        dBConnectionMock.commitConnection();


        dBConnectionMock.closeConnection();


        PowerMock.replayAll();


        boolean isSuccess = saveManager.save(modelMock);


        assertTrue(isSuccess);


        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method. 
     * <p> 
     * The condition for this test case:
     * 
     * <br> - The model being saved is an existing model and should be
     * updated in the database. 
     * <br>- The executeSaveModelTask method throws
     * exception. 
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_SaveModelNotSuccessful() throws Exception {


        SaveModelManager saveManager = new SaveModelManager();


        PowerMock.mockStatic(DBConnectorFactory.class);


        DBConnection dBConnectionMock = PowerMock
                .createMock(DBConnection.class);


        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                dBConnectionMock);


        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);


        EasyMock.expect(modelMock.getIsNew()).andReturn(false);


        SaveModelTask saveModelTaskMock = PowerMock
                .createMock(SaveModelTask.class);

        PowerMock.expectNew(SaveModelTask.class, modelMock).andReturn(saveModelTaskMock);


        //saveModelTaskMock.setXMLDBModel(modelMock);


        dBConnectionMock.executeSaveModelTask(saveModelTaskMock);


        PowerMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() throws DBExecutionException {

                throw new DBExecutionException("Test Message");

            }
        });


        dBConnectionMock.closeConnection();


        dBConnectionMock.abortConnection();


        PowerMock.replayAll();


        boolean isSuccess = false;

        try {

            saveManager.save(modelMock);

        } catch (DBExecutionException e) {

            isSuccess = true;
        }


        assertTrue(isSuccess);


        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() method with DBConnection being null.
     * 
     * <p>
     * The condition for this test case:
     * 
     * <br>- Fail to get a DBConnection from the DBConnectionFactory.
     * 
     * </p>
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_NullDBConn() throws Exception {


        SaveModelManager saveManager = new SaveModelManager();


        PowerMock.mockStatic(DBConnectorFactory.class);


        EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(
                null);


        XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);


        PowerMock.replayAll();


        boolean isSuccess = false;

        try {

            saveManager.save(modelMock);

        } catch (DBConnectionException e) {

            isSuccess = true;
        }


        assertTrue(isSuccess);


        PowerMock.verifyAll();

    }

    /**
     * Test the SaveManager.save() with null parameters passed to it.
     * 
     * <p>
     * The condition for this test case: 
     * <br>- The model passed as a parameter is null. The test result should 
     * throw an exception.
     * </p>
     * 
     * @exception Exception Thrown if the test fails and the exception was not
     * handled.
     */
    @Test
    public void testSave_NullModelParam() throws Exception {


        SaveModelManager saveManager = new SaveModelManager();


        PowerMock.replayAll();


        boolean isSuccess = false;

        try {

            saveManager.save(null);
        } catch (IllegalArgumentException e) {

            isSuccess = true;
        }


        assertTrue(isSuccess);

        PowerMock.verifyAll();

    }
}
