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
package ptdb.kernel.database;

import java.util.ArrayList;
import java.util.List;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateAttributeTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DeleteAttributeTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetFirstLevelParentsTask;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.GetReferenceStringTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.ModelNameSearchTask;
import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.RenameModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.Task;
import ptdb.common.dto.TaskQueue;
import ptdb.common.dto.UpdateAttributeTask;
import ptdb.common.dto.UpdateParentsToNewVersionTask;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.DBModelNotFoundException;
import ptdb.common.exception.ModelAlreadyExistException;

///////////////////////////////////////////////////////////////////
////AsynchronousDBConnection
/**
 * Provide an asynchronous mechanism of executing tasks over the database.
 *
 * <p>All the tasks are queued and executed parallel to the processing tasks.
 * We plan to use this kind of connection for operations like saving a model
 * that require a lot of processing.</p>
 *
 * @author Ashwini Bijwe, Yousef Alsaeed
 *
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class AsynchronousDBConnection implements DBConnection {

    /**
     * Construct an instance that creates the taskQueue to enqueue tasks
     * and the executor thread that enables task execution in parallel.
     *
     * @exception DBConnectionException If thrown while
     * creating a connection.
     */
    public AsynchronousDBConnection() throws DBConnectionException {
        _taskQueue = new TaskQueue();
        _executorThread = new ExecutorThread(_taskQueue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Abort the execution by setting the execution flag as false.
     * Abort connection signifies that the execution needs to be terminated
     * as there was some error in execution.
     */
    @Override
    public void abortConnection() {
        _taskQueue.setProcessingError();
    }

    /**
     * Close the connection if all tasks in the queue
     * are executed successfully.
     * Close connection signifies that the processing is completed.
     *
     * @exception DBConnectionException If thrown while
     * closing a connection.
     */
    @Override
    public void closeConnection() throws DBConnectionException {

        boolean hasExecutionCompleted = false;
        int maxWait = _getMaxTurns();
        /*
         * The thread waits for one of the three things to happen
         *  - task execution to complete successfully
         *  - task execution to generate an error
         *  - maximum wait time is over (120000 milliseconds = 2 minutes)
         */
        while (!hasExecutionCompleted && maxWait != 0) {

            if (_taskQueue.hasExecutionError()) {
                throw new DBConnectionException(
                        _taskQueue.getExecutionErrorMessage());
            }

            hasExecutionCompleted = _taskQueue.hasExecutionCompleted();
            maxWait--;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Commit the transaction running over the connection.
     *
     * @exception DBConnectionException If thrown while committing a
     * transaction in the database.
     */
    @Override
    public void commitConnection() throws DBConnectionException {
        /*
         * All tasks added denotes that the tasks have been added
         * and processing is successfully completed.
         */
        _taskQueue.setAllTasksAdded();
    }

    /**
     * Search models that contain the given attributes in the database.
     * Not supported by this type of connection.
     *
     * @param task Task that contains a list of attributes that
     * need to be searched in the database.
     *
     * @return List of models that contain the attributes.
     *
     * @exception DBExecutionException If thrown while searching.
     */
    @Override
    public ArrayList<XMLDBModel> executeAttributeSearchTask(
            AttributeSearchTask task) throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeAttributeSearchTask is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Execute the necessary commands to create a new attribute in the database
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateAttributeTask.
     *          This will tell the DB layer to create a new attribute in the database.
     * @return The XMLDBAttribute object that was stored in the Database.
     *
     * @exception DBExecutionException Thrown if the operation fails.
     */
    @Override
    public XMLDBAttribute executeCreateAttributeTask(CreateAttributeTask task)
            throws DBExecutionException {

        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeCreateAttributeTask is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Execute the necessary commands to create a new model in the database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateModelTask.
     *          This will tell the DB layer to create a new model in the database.
     *
     * @return The Id of the newly created model.
     * @exception DBExecutionException If thrown while creating a model.
     * @exception ModelAlreadyExistException Thrown if the model being created already exists.
     */
    @Override
    public String executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException, ModelAlreadyExistException {

        //call the execueTask method to execute the given task
        _executeTask(task);

        return null;

    }

    /**
     * Execute the necessary commands to delete an attribute from the database
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, DeleteAttributeTask.
     *          This will tell the DB layer to delete an attribute from the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    @Override
    public void executeDeleteAttributeTask(DeleteAttributeTask task)
            throws DBExecutionException {

        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeDeleteAttributeTask is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Execute Fetch Hierarchy task.
     *
     * <p>Fetch the parent model hierarchies is
     * not supported by the asynchronous connection.
     * Use a synchronous connection for that.</p>
     *
     * @param task Task that contains the list of models.
     * @return List of models that contain the parent hierarchies.
     * @exception DBExecutionException If thrown while searching.
     */
    @Override
    public ArrayList<XMLDBModel> executeFetchHierarchyTask(
            FetchHierarchyTask task) throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeFetchHierarchyTask is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Get Attributes Task is not supported by the asynchronous connection.
     * Use a synchronous connection for that.
     * @param task
     *          The task to be completed.
     * @return List
     *          The list of attributes in database.
     * @exception DBExecutionException If thrown while getting attributes from the
     * database.
     */
    @Override
    public List<XMLDBAttribute> executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetAttributes is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Fetch the first level parents for the given model is not supported by
     * this connection.
     * Use a synchronous connection for that.
     *
     * @param task Task that contains the model for which the first level
     * parents list needs to be fetched.
     *
     * @return List of models that are the first-level parents of the given
     * model.
     *
     * @exception DBExecutionException If thrown while fetching the parents list
     * from the database.
     */
    @Override
    public List<XMLDBModel> executeGetFirstLevelParents(
            GetFirstLevelParentsTask task) throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetFirstLevelParents is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Get the model reference string is not supported by the asynchronous
     * connection.
     * Use a synchronous connection for that.
     *
     * @param task Task that contains the model name.
     * @return The reference string for the given model name.
     * @exception DBExecutionException If thrown while fetching the reference
     * string.
     */
    @Override
    public String executeGetReferenceStringTask(GetReferenceStringTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetReferenceStringTask is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Execute the necessary commands to retrieve a model from the database.
     *
     * @param task
     *          The task to be completed.  In this case, GetModelTask.
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string.
     * @exception DBExecutionException Thrown if the operations fails.
     */
    @Override
    public XMLDBModel executeGetModelTask(GetModelTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetModelTask "
                        + "is not supported by this type of DBConnection");

    }

    /**
     * Execute the necessary commands to retrieve a model from the database
     * and resolve all the references in it if any.
     *
     * @param task
     *          The task to be completed.  In this case, GetModelTask.
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string.
     * @exception DBExecutionException Thrown if the operations fails.
     */
    @Override
    public XMLDBModel executeGetCompleteModelTask(GetModelTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetCompleteModelTask "
                        + "is not supported by this type of DBConnection");

    }

    /**
     * Retrieve and return the list of all models in the database.
     * @return List of models in the database.
     * @exception DBExecutionException thrown if there is an error while reading
     * the model list from the database.
     */
    @Override
    public List<XMLDBModel> executeGetListOfAllModels()
            throws DBExecutionException {

        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetListOfAllModels "
                        + "is not supported by this type of DBConnection");

    }

    /**
     * Search for graph is not supported by the asynchronous connection.
     * Use a synchronous connection for that.
     *
     * @param task - Task that contains the graph search criteria.
     * @return - List of models that match the given search criteria.
     * @exception DBExecutionException If thrown while searching
     * in the database.
     */
    @Override
    public ArrayList<XMLDBModel> executeGraphSearchTask(GraphSearchTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGraphSearchTask "
                        + "is not supported by this type of DBConnection");
    }

    /**
     * Execute the model name search task is not supported by the asynchronous
     * connection.
     * Use a synchronous connection for that.
     *
     * @param modelNameSearchTask Task that contains the model name to be
     * searched for.
     * @return List of matching models.
     * @exception DBExecutionException If thrown while searching the database.
     */
    @Override
    public ArrayList<XMLDBModel> executeModelNameSearchTask(
            ModelNameSearchTask modelNameSearchTask)
                    throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeModelNameSearchTask "
                        + "is not supported by this type of DBConnection");
    }

    /**
     * Execute the necessary commands to save/update a model in the
     * database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, SaveModelTask.
     *          This will tell the DB layer to save/update a model
     *          already existing in the database.
     *
     * @return The Id of the model being saved.
     *
     * @exception DBExecutionException If thrown while saving model
     * in the database.
     */
    @Override
    public String executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException {

        //call the execueTask method to execute the given task
        _executeTask(task);

        return null;
    }

    /**
     * Execute remove models task to delete a list of models from the database.
     * @param task Contains a list of models to be deleted from the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    @Override
    public void executeRemoveModelsTask(RemoveModelsTask task)
            throws DBExecutionException {

        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeRemoveModelsTask "
                        + "is not supported by this type of DBConnection");
    }

    /**
     * Execute rename model task which will change the name of the model in
     * the database and reflect the change in the reference file.
     * @param task RenameModelTask object that contains the XMLDBModel
     * object and the new name.
     * @exception DBConnectionException Thrown if there was a problem with the connection.
     * @exception DBExecutionException Thrown if there is a problem in executing the task.
     * @exception DBModelNotFoundException Thrown if the model with the name to be changed does not exist.
     * @exception ModelAlreadyExistException Thrown if the new name is a name of a model that is already in the database.
     */
    @Override
    public void executeRenameModelTask(RenameModelTask task)
            throws DBConnectionException, DBExecutionException,
            ModelAlreadyExistException, DBModelNotFoundException {

        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeRenameModelTask "
                        + "is not supported by this type of DBConnection");

    }

    /**
     * Execute the necessary commands to update an attribute in the database
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, UpdateAttributeTask.
     *          This will tell the DB layer to update an attribute in the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    @Override
    public void executeUpdateAttributeTask(UpdateAttributeTask task)
            throws DBExecutionException {

        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeUpdateAttributeTask "
                        + "is not supported by this type of DBConnection");

    }

    /**
     * Execute the necessary commands to update the cache with the given model.
     *
     * <p>If the model exists, replace it with the new model.
     * <br> If the model does not exist, save it in the cache.</p>
     *
     * @param xmlDBModel The model object that needs to be added to the cache.
     * @exception DBExecutionException Thrown if the operation fails.
     * already exists.
     */
    @Override
    public void executeUpdateModelInCache(XMLDBModel xmlDBModel)
            throws DBExecutionException {

        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeUpdateModelInCache "
                        + "is not supported by this type of DBConnection");

    }

    /**
     * Execute the given task to update the referenced version for the given
     * parents from the old model to the new model.
     * @param task Task that contains the list of parents, the old model and the
     * new model.
     * @exception DBExecutionException If thrown while updating the parents in the
     * database.
     */
    @Override
    public void executeUpdateParentsToNewVersion(
            UpdateParentsToNewVersionTask task) throws DBExecutionException {
        _executeTask(task);
    }

    /**
     * Return a string representation for the
     * internal parameters of the class.
     * Used for verifying during unit testing.
     *
     * @return String representation of the
     * internal parameters of the class.
     */
    @Override
    public String toString() {

        StringBuffer connState = new StringBuffer();
        connState.append(":Processing Error-")
        .append(_taskQueue.hasProcessingError()).append(":");
        connState.append(":All Tasks Added-")
        .append(_taskQueue.areAllTasksAdded()).append(":");
        return connState.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Adds tasks to the task queue for the executor thread to execute.
     *
     * @param task Task that needs to be executed on the database.
     * @exception DBConnectionException If thrown by executor thread
     * while executing tasks on the database.
     */
    private void _executeTask(Task task) throws DBExecutionException {

        /*
         * If this is the first task, then start the executor thread
         */
        if (_taskQueue.size() == 0) {
            _executorThread.run();
        }
        /*
         * If the executor thread failed due to an exception;
         * throw that exception
         */
        if (_taskQueue.hasExecutionError()) {
            throw new DBExecutionException(
                    _taskQueue.getExecutionErrorMessage());
        }
        /*
         * Add the task to the queue
         */
        _taskQueue.add(task);
    }

    /**
     * Provide maximum turns for waiting for
     * database processing to finish.
     * @return 1200 (Maximum) turns to wait for
     * the processing to finish.
     */
    private int _getMaxTurns() {
        return 1200;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The executor thread that processes the tasks from the taskQueue.
     */
    private ExecutorThread _executorThread;

    /**
     * Queue where the tasks are added one after the another.
     */
    private TaskQueue _taskQueue;

}
