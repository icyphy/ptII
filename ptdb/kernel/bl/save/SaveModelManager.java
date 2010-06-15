package ptdb.kernel.bl.save;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// SaveModelManager

/**
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 * 
 * The business layer of the save to database function.
 * 
 * <p> 
 * It is responsible for:
 * 
 * <br>- Get the XMLDBModel which is the Ptolemy model wrapped in a special Data
 * Transfer Object (dto).
 * 
 * <br>- Wrap the model object in another dto (SaveModelTask or CreateModelTask)
 * to either create it in the database or save the modification made on it.
 * 
 * <br>- Get a database connection and run the execute method on the database
 * connection object to perform the saving or creation of the model. 
 * </p>
 */
public class SaveModelManager {

    ///////////////////////////////////////////////////////////////////
    ////                public methods                            ////

    /**
     * Save the changes of an existing model in the database or create a new
     * model in the database.
     * 
     * @param xmlDBModel The model object that is required to be saved or
     * created in the database.
     * 
     * @exception DBConnectionException Thrown if there is a database connection error.
     * @exception DBExecutionException Thrown if the execution failed.
     * @exception IllegalArgumentException Thrown if the parameters were not right.
     * 
     * @return boolean An indicator of weather the operation was successful or
     * not.
     */
    public boolean save(XMLDBModel xmlDBModel) throws DBConnectionException,
            DBExecutionException, IllegalArgumentException {
        
        boolean isSuccessful = false;

        
        DBConnection dbConnection = null;

        try {

            if (xmlDBModel == null) {
                throw new IllegalArgumentException(
                        "Failed while attempting to save."
                                + " The XMLDBModel to be saved is null");
            }

            
            dbConnection = DBConnectorFactory.getSyncConnection(true);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database");
            }

            if (xmlDBModel.getIsNew()) {

                CreateModelTask createModelTask = new CreateModelTask();

                createModelTask.setXMLDBModel(xmlDBModel);

                dbConnection.executeCreateModelTask(createModelTask);

                isSuccessful = true;

                dbConnection.commitConnection();

            } else {

                SaveModelTask saveModelTask = new SaveModelTask();

                saveModelTask.setXMLDBModel(xmlDBModel);

                dbConnection.executeSaveModelTask(saveModelTask);

                isSuccessful = true;

                dbConnection.commitConnection();

            }

        } catch (DBExecutionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw new DBExecutionException("Failed to save the model - "
                    + e.getMessage(), e);
        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();

            }
        }
        
        return isSuccessful;

    }
}
