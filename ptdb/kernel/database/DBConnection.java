package ptdb.kernel.database;

import java.util.ArrayList;
import java.util.List;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateAttributeTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DeleteAttributeTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.ModelNameSearchTask;
import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.UpdateAttributeTask;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;

///////////////////////////////////////////////////////////////////
//// DBConnection
/**
 *  Interface for XML Database connections.
 *
 *  @author Ashwini Bijwe, Yousef Alsaeed
 *
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (abijwe)
 *  @Pt.AcceptedRating Red (abijwe)
 *
 */
public interface DBConnection {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Abort the connection to the database and roll back the transaction.
     * @exception DBConnectionException
     */
    public void abortConnection() throws DBConnectionException;

    /**
     * Close the connection to the database and commit the transaction.
     * @exception DBConnectionException
     */
    public void closeConnection() throws DBConnectionException;

    /**
     * Commit the transaction running over the connection.
     * @exception DBConnectionException - When there is a problem while committing
     * transaction in the database.
     */
    public void commitConnection() throws DBConnectionException;
    
    
    /**
     * Execute the necessary commands to create a new attribute in the database 
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateAttributeTask.
     *          This will tell the DB layer to create a new attribute in the database.
     * 
     * @return The XMLDBAttribute object that was stored in the Database.
     *  
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public XMLDBAttribute executeCreateAttributeTask(CreateAttributeTask task)
            throws DBExecutionException;
    
    /**
     * Execute the necessary commands to create a new model in the database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateModelTask.
     *          This will tell the DB layer to create a new model in the database.
     * 
     * @return The Id of the newly created model.
     * 
     * @exception DBExecutionException
     * @exception ModelAlreadyExistException Thrown if the model being created already exists.
     */
    public String executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException, ModelAlreadyExistException;
    
    
    /**
     * Execute the necessary commands to delete an attribute from the database 
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, DeleteAttributeTask.
     *          This will tell the DB layer to delete an attribute from the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public void executeDeleteAttributeTask(DeleteAttributeTask task)
            throws DBExecutionException;

    /**
     * Fetch the parent model hierarchies for the given models.
     *
     * @param task - Task that contains the list of models.
     * @return - List of models that contain the parent hierarchies.
     * @exception DBExecutionException - When the database encounters
     * error while searching.
     */
    public ArrayList<XMLDBModel> executeFetchHierarchyTask(
            FetchHierarchyTask task) throws DBExecutionException;

    /**
     * Search models that contain the given attributes in the database.
     *
     * @param task Task that contains a list of attributes that
     * need to be searched in the database.
     *
     * @return List of models that contain the attributes.
     *
     * @exception DBExecutionException - When the database encounters error while searching.
     */
    public ArrayList<XMLDBModel> executeAttributeSearchTask(
            AttributeSearchTask task) throws DBExecutionException;

    /**
     * Get the attributes defined from the database.
     * @param task The criteria to get the attribute.
     * @return List of attributes stored in the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public List<XMLDBAttribute> executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException;

    /**
     * Execute the necessary commands to retrieve a model from the database.
     *
     * @param task
     *          The task to be completed.  In this case, GetModelTask.
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string.
     * @exception DBConnectionException Thrown if the operations fails.
     */
    public XMLDBModel executeGetModelTask(GetModelTask task)
            throws DBExecutionException;
    
    
    /**
     * Execute the necessary commands to retrieve a model from the database 
     * and resolve all the references in it if any.
     *
     * @param task
     *          The task to be completed.  In this case, GetModelTask.
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string.
     * @exception DBConnectionException Thrown if the operations fails.
     */
    public XMLDBModel executeGetCompleteModelTask(GetModelTask task)
            throws DBExecutionException;
    
    

    /**
     * Search models that contain given graphical pattern in the database.
     *
     * @param task - Task that contains the graph search criteria.
     * @return - List of models that match the given search criteria.
     * @exception DBExecutionException - When the database encounters
     * error while searching.
     */
    public ArrayList<XMLDBModel> executeGraphSearchTask(GraphSearchTask task)
            throws DBExecutionException;

    /** Execute the model name search task.
     * 
     * @param modelNameSearchTask Task that contains the model name to be searched for.
     * @return List of matching models.
     * @throws DBExecutionException If thrown while searching the database.
     */
    public ArrayList<XMLDBModel> executeModelNameSearchTask(
            ModelNameSearchTask modelNameSearchTask)
            throws DBExecutionException;
    
    /**
     * Execute the necessary commands to save/update a model in the database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, SaveModelTask.
     *          This will tell the DB layer to save/update a model already existing in the database.
     * 
     * @return The Id of the saved model.
     * 
     * @exception DBExecutionException Thrown when there is a problem in executing the task.
     */
    public String executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException;
    
    
    /**
     * Execute remove models task to delete a list of models from the database.
     * @param task Contains a list of models to be deleted from the database.
     * @throws DBExecutionException Thrown if the operation fails.
     */
    public void executeRemoveModelsTask (RemoveModelsTask task) 
            throws DBExecutionException;
    

    /**
     * Execute the necessary commands to update an attribute in the database 
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, UpdateAttributeTask.
     *          This will tell the DB layer to update an attribute in the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public void executeUpdateAttributeTask(UpdateAttributeTask task)
            throws DBExecutionException;

}
