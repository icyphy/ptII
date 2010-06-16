package ptdb.kernel.database;

import java.util.ArrayList;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.SaveModelTask;
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
     * Execute the necessary commands to create a new model in the database according
     * to the model specification given in the task parameter
     *
     * @param task
     *          The task to be completed.  In this case, CreateModelTask.
     *          This will tell the DB layer to create a new model in the database.
     * @exception DBExecutionException
     * @exception ModelAlreadyExistException Thrown if the model being created already exists.
     */
    public void executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException, ModelAlreadyExistException;

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
     * @exception DBExecutionException
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException;

    /**
     * Execute the necessary commands to retrieve a model from the database.
     *
     * @param task
     *          The task to be completed.  In this case, GetModelsTask.
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string.
     * @exception DBConnectionException
     */
    public XMLDBModel executeGetModelsTask(GetModelsTask task)
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

    /**
     * Execute the necessary commands to save/update a model in the database according
     * to the model specification given in the task parameter
     *
     * @param task
     *          The task to be completed.  In this case, SaveModelTask.
     *          This will tell the DB layer to save/update a model already existing in the database.
     * @exception DBExecutionException
     */
    public void executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException;

}
