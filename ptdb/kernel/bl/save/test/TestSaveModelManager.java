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


///////////////////////////////////////////////////////////////
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
@PrepareForTest( { SaveModelManager.class, DBConnection.class, DBConnectorFactory.class,
    	CreateModelTask.class, SaveModelTask.class, DBExecutionException.class})
    	
@SuppressStaticInitializationFor("ptdb.common.util.DBConnectorFactory")
public class TestSaveModelManager {


    //////////////////////////////////////////////////////////////////////
    ////		public methods 				    //////
    
    /**
     * method to test the SaveManager.save() method.
     * 
     * The condition for this test case:
     * 
     * 1- The model being saved is a new model and should be created in the database.
     */
    @Test
    public void testSave_CreateModel() throws Exception {
	
	//create an object to be tested.
	SaveModelManager saveManager = new SaveModelManager();
	
	//mock the connection factory.
	PowerMock.mockStatic(DBConnectorFactory.class);
	
	
	//mock the dbconnection
	DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);
	
	//if the connectionfactory.getsyncConnection(true) is invoked return dbConnection
	EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(dBConnectionMock);


	//mock the xmldb model
	XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);
	
	//mock the is new to true.
	EasyMock.expect(modelMock.getIsNew()).andReturn(true);
	
	//mock the create task.
	CreateModelTask createModelTaskMock = PowerMock.createMock(CreateModelTask.class);
	
	PowerMock.expectNew(CreateModelTask.class).andReturn(createModelTaskMock);
	
	//
	createModelTaskMock.setXMLDBModel(modelMock);

	//mock the call to the execute create model task.
	dBConnectionMock.executeCreateModelTask(createModelTaskMock);
	
	
	//close the connection
	dBConnectionMock.closeConnection();
	
	
	

	//replay all...
	PowerMock.replayAll();
	
	//execute the save method.
	boolean bIsSuccess = saveManager.save(modelMock);
	
	
	
	//check if the returned value is true.
	 assertTrue(bIsSuccess);
	
	 //verify.
	PowerMock.verifyAll();
	
    }

    
    /**
     * method to test the SaveManager.save() method.
     * 
     * The condition for this test case:
     * 
     * 1- The model being saved is a new model and should be created in the database.
     * 2- The executeCreateModelTask method throws exception.
     */
    @Test
    public void testSave_CreateModelNotSuccessful() throws Exception {
	
	//create an object to be tested.
	SaveModelManager saveManager = new SaveModelManager();
	
	//mock the connection factory.
	PowerMock.mockStatic(DBConnectorFactory.class);
	
	
	//mock the dbconnection
	DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);
	
	//if the connectionfactory.getsyncConnection(true) is invoked return dbConnection
	EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(dBConnectionMock);


	//mock the xmldb model
	XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);
	
	//mock the is new to true.
	EasyMock.expect(modelMock.getIsNew()).andReturn(true);
	
	//mock the create task.
	CreateModelTask createModelTaskMock = PowerMock.createMock(CreateModelTask.class);
	
	PowerMock.expectNew(CreateModelTask.class).andReturn(createModelTaskMock);
	
	//
	createModelTaskMock.setXMLDBModel(modelMock);

	//mock the call to the execute create model task.
	dBConnectionMock.executeCreateModelTask(createModelTaskMock);
	
	
	//make sure the executeCreateModelTask method throws exception.
	PowerMock.expectLastCall().andAnswer(new IAnswer() {
	    public Object answer() throws DBExecutionException {
	       
	        throw new DBExecutionException("Message");
	        
	    }
	});
	
	//close the connection
	dBConnectionMock.closeConnection();
	
	//
	dBConnectionMock.abortConnection();
	
	

	//replay all...
	PowerMock.replayAll();
	
	//execute the save method.
	boolean bIsSuccess = false;
	
        try {
	    
            saveManager.save(modelMock);
            
        } catch (DBExecutionException e) {
            
	   bIsSuccess = true;
        }
	
	
	
	//check if the returned value is true.
	 assertTrue(bIsSuccess);
	
	 //verify.
	PowerMock.verifyAll();
	
    }

    
    
    
    /**
     * method to test the SaveManager.save() method.
     * 
     * The condition for this test case:
     * 
     * 1- The model being saved is an existing model and should be updated in the database.
     */
    @Test
    public void testSave_SaveModel() throws Exception {
	
	//create an object to be tested.
	SaveModelManager saveManager = new SaveModelManager();
	
	//mock the connection factory.
	PowerMock.mockStatic(DBConnectorFactory.class);
	
	
	//mock the dbconnection
	DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);
	
	//if the connectionfactory.getsyncConnection(true) is invoked return dbConnection
	EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(dBConnectionMock);


	//mock the xmldb model
	XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);
	
	//mock the is new to true.
	EasyMock.expect(modelMock.getIsNew()).andReturn(false);
	
	//mock the save task.
	SaveModelTask saveModelTaskMock = PowerMock.createMock(SaveModelTask.class);
	
	PowerMock.expectNew(SaveModelTask.class).andReturn(saveModelTaskMock);
	
	//
	saveModelTaskMock.setXMLDBModel(modelMock);

	//mock the call to the execute create model task.
	dBConnectionMock.executeSaveModelTask(saveModelTaskMock);
	
	
	//close the connection
	dBConnectionMock.closeConnection();

	//replay all...
	PowerMock.replayAll();
	
	//execute the save method.
	boolean bIsSuccess = saveManager.save(modelMock);
	
	
	
	//check if the returned value is true.
	 assertTrue(bIsSuccess);
	
	 //verify.
	PowerMock.verifyAll();
	
    }
    
    
    
    /**
     * method to test the SaveManager.save() method.
     * 
     * The condition for this test case:
     * 
     * 1- The model being saved is an existing model and should be updated in the database.
     * 2- The executeSaveModelTask method throws exception.
     */
    @Test
    public void testSave_SaveModelNotSuccessful() throws Exception {
	
	//create an object to be tested.
	SaveModelManager saveManager = new SaveModelManager();
	
	//mock the connection factory.
	PowerMock.mockStatic(DBConnectorFactory.class);
	
	
	//mock the dbconnection
	DBConnection dBConnectionMock = PowerMock.createMock(DBConnection.class);
	
	//if the connectionfactory.getsyncConnection(true) is invoked return dbConnection
	EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(dBConnectionMock);


	//mock the xmldb model
	XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);
	
	//mock the is new to true.
	EasyMock.expect(modelMock.getIsNew()).andReturn(false);
	
	//mock the create task.
	SaveModelTask saveModelTaskMock = PowerMock.createMock(SaveModelTask.class);
	
	PowerMock.expectNew(SaveModelTask.class).andReturn(saveModelTaskMock);
	
	//
	saveModelTaskMock.setXMLDBModel(modelMock);

	//mock the call to the execute create model task.
	dBConnectionMock.executeSaveModelTask(saveModelTaskMock);
	
	
	//make sure the executeCreateModelTask method throws exception.
	PowerMock.expectLastCall().andAnswer(new IAnswer() {
	    public Object answer() throws DBExecutionException {
	       
	        throw new DBExecutionException("Test Message");
	        
	    }
	});
	
	//close the connection
	dBConnectionMock.closeConnection();
	
	//abort the connection
	dBConnectionMock.abortConnection();
	
	

	//replay all...
	PowerMock.replayAll();
	
	//execute the save method.
	boolean bIsSuccess = false;
	
        try {
	    
            saveManager.save(modelMock);
            
        } catch (DBExecutionException e) {
            
	   bIsSuccess = true;
        }
	
	
	
	//check if the returned value is true.
	 assertTrue(bIsSuccess);
	
	 //verify.
	PowerMock.verifyAll();
	
    }
    
    
    /**
     * test the SaveManager.save() method with DBConnection being null.
     * 
     * The condition for this test case:
     * 
     * 1- Fail to get a DBConnection from the DBConnectionFactory.
     */
    @Test
    public void testSave_NullDBConn() throws Exception {
	
	//create an object to be tested.
	SaveModelManager saveManager = new SaveModelManager();
	
	//mock the connection factory.
	PowerMock.mockStatic(DBConnectorFactory.class);
	
	//if the connectionfactory.getsyncConnection(true) is invoked return dbConnection
	EasyMock.expect(DBConnectorFactory.getSyncConnection(true)).andReturn(null);


	//mock the xmldb model
	XMLDBModel modelMock = PowerMock.createMock(XMLDBModel.class);
	

	//replay all...
	PowerMock.replayAll();
	
	//execute the save method.
	boolean bIsSuccess = false;
	
        try {
	    
            saveManager.save(modelMock);
            
        } catch (DBConnectionException e) {
            
	   bIsSuccess = true;
        }
	
	
	
	//check if the returned value is true.
	 assertTrue(bIsSuccess);
	
	 //verify.
	PowerMock.verifyAll();
	
    }

    
    
    /**
     * test the SaveManager.save() with null parameters passed to it.
     * 
     * The condition for this test case:
     * 1- The model passed as a parameter is null.
     * 
     * the test result should throw an exception.
     */
    @Test
    public void testSave_NullModelParam() throws Exception {
	
	//create an object to be tested.
	SaveModelManager saveManager = new SaveModelManager();

	//replay all...
	PowerMock.replayAll();
	
	//boolean to hold the results.
        boolean bIsSuccess = false;
        
        try {
            //this should throw and exception
            saveManager.save(null);
        } catch (IllegalArgumentException e) {
            //if an exception is thrown then the test pass.
            bIsSuccess = true;
        }

	//check if the returned value is true.
	 assertTrue(bIsSuccess);
	
	 //verify.
	PowerMock.verifyAll();
	
    }
}
