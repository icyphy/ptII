package ptdb.kernel.bl.save;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////
//// SaveModelManager

/**
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 * 
 *  This class represent the business layer of the save function.
 *  
 *  It is responsible for:
 *  
 *  1- Get the XMLDBModel which is the Ptolemy model wrapped in a special Data Transfer Object (dto).
 *  
 *  2- Wrap the model object in another dto (SaveModelTask or CreateModelTask) to either create it in 
 *  the database or save the modification made on it.
 *  
 *  3- Get a database connection and run the execute method on the database connection object to 
 *  perform the saving or creation of the model.
 */
public class SaveModelManager {

    //////////////////////////////////////////////////////////////////////
    ////		public methods 				    //////

    /**
     * Save the changes of an existing model in the database or create a new model in the database.
     * 
     * @param p_xmlDBModel the model object that is required to be saved or created in the database. 
     * @throws DBConnectionException, DBExecutionException, IllegalArgumentException.
     * @return boolean an indicator of weather the operation was successful or not.
     */
    public boolean save(XMLDBModel p_xmlDBModel) throws DBConnectionException,
	    DBExecutionException, IllegalArgumentException {
	//variable to indicate if the operation was successful or not
	boolean bIsSuccessful = false;

	//database connection object
	DBConnection dbConn = null;

	try {

	    //if the p_xmlDBModel is null, throw an exception
	    if (p_xmlDBModel == null) {
		throw new IllegalArgumentException(
		        "Failed while attempting to save."
		                + " The XMLDBModel to be saved is null");
	    }

	    //get sync connection with transaction enabled using the DBConnectorFactory.
	    dbConn = DBConnectorFactory.getSyncConnection(true);

	    //if the dbConn is null, throw an exception
	    if (dbConn == null) {
		throw new DBConnectionException(
		        "Unable to get synchronous connection from the database");
	    }

	    //if the model to be saved is a new model (i.e. not in the database),
	    //create the model in the database
	    if (p_xmlDBModel.getIsNew()) {

		//create a new CreateModelTask 
		CreateModelTask createModelTask = new CreateModelTask();

		//set the model to be created in the database to the model passed to this method
		createModelTask.setXMLDBModel(p_xmlDBModel);

		//use the dbConn object to execute the model creation task
		dbConn.executeCreateModelTask(createModelTask);

		//if no exception was thrown, then the operation was successful
		//set the success flag to true
		bIsSuccessful = true;

	    }
	    //if the model is already in the database, save the modification made on it.
	    else {

		//create a SaveModelTask
		SaveModelTask saveModelTask = new SaveModelTask();

		//set the XMLDBModel to be saved to the model passed to this method
		saveModelTask.setXMLDBModel(p_xmlDBModel);

		//use the dbConn object to execute the save model task
		dbConn.executeSaveModelTask(saveModelTask);

		//if no exception was thrown, then the operation was successful
		//set the success flag to true
		bIsSuccessful = true;

	    }

	} catch (DBExecutionException e) {

	    //if the connection object is not null, abort the connection
	    if (dbConn != null) {

		//abort the connection to rollback any changes happened to the database
		//and clean up 
		dbConn.abortConnection();
	    }

	    //throw an exception to notify the caller of what went wrong
	    throw new DBExecutionException("Failed to save the model - "
		    + e.getMessage(), e);
	} finally {

	    //if the db connection is not null, close it.
	    if (dbConn != null) {

		//close the connection
		dbConn.closeConnection();

	    }
	}

	//return the success flag to the caller 
	return bIsSuccessful;

    }
}
