package ptdb.kernel.database;

import java.util.ArrayList;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.Task;
import ptdb.common.dto.TaskQueue;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
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
 * @since Ptolemy II 8.1
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

            if (_taskQueue.hasExecutionError())
                throw new DBConnectionException(_taskQueue
                        .getExecutionErrorMessage());

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
    public ArrayList<XMLDBModel> executeAttributeSearchTask(
            AttributeSearchTask task) throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeAttributeSearchTask is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Execute the necessary commands to create a new model in the database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateModelTask.
     *          This will tell the DB layer to create a new model in the database.
     * @exception DBExecutionException If thrown while creating a model.
     * @exception ModelAlreadyExistException Thrown if the model being created already exists.
     */
    public void executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException, ModelAlreadyExistException {

        //call the execueTask method to execute the given task
        _executeTask(task);

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
     * @return XMLDBModel
     *          The model object containing the MoML.
     * @exception DBExecutionException If thrown while getting attributes from the
     * database.
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetAttributes is "
                        + "not supported by this type of DBConnection");
    }

    /**
     * Get Models Task is not supported by the asynchronous connection.
     * Use a synchronous connection for that.
     * @param task
     *          The task to be completed.
     * @return XMLDBModel
     *          The model object containing the MoML.
     * @exception DBExecutionException
     */
    public XMLDBModel executeGetModelsTask(GetModelsTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetModels "
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
    public ArrayList<XMLDBModel> executeGraphSearchTask(GraphSearchTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGraphSearchTask "
                        + "is not supported by this type of DBConnection");
    }

    /**
     * Execute the necessary commands to save/update a model in the database according
     * to the model specification given in the task parameter
     *
     * @param task
     *          The task to be completed.  In this case, SaveModelTask.
     *          This will tell the DB layer to save/update a model
     *          already existing in the database.
     * @exception DBExecutionException If thrown while saving model
     * in the database.
     */
    public void executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException {

        //call the execueTask method to execute the given task
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
    public String toString() {

        StringBuffer connState = new StringBuffer();
        connState.append(":Processing Error-").append(
                _taskQueue.hasProcessingError()).append(":");
        connState.append(":All Tasks Added-").append(
                _taskQueue.areAllTasksAdded()).append(":");
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
        if (_taskQueue.size() == 0)
            _executorThread.run();
        /*
         * If the executor thread failed due to an exception;
         * throw that exception
         */
        if (_taskQueue.hasExecutionError())
            throw new DBExecutionException(_taskQueue
                    .getExecutionErrorMessage());
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
