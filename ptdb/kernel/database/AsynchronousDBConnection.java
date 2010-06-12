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

//////////////////////////////////////////////////////////////////////////
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
     * @throws DBConnectionException
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
     */
    public void closeConnection() throws DBConnectionException {

        boolean hasExecutionCompleted = false;
        int maxWait = 1200;
        /**
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
     * @throws DBConnectionException - When there is a problem while committing 
     * transaction in the database.
     */
    public void commitConnection() throws DBConnectionException {
        /**
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
     * @throws DBExecutionException - When the database encounters error while searching.
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
     * @throws DBExecutionException
     */
    public void executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException {

        //call the execueTask method to execute the given task
        executeTask(task);

    }

    /**
     * Fetch the parent model hierarchies is 
     * not supported by the asynchronous connection.
     * Use a synchronous connection for that.
     * 
     * @param task - Task that contains the list of models.
     * @return - List of models that contain the parent hierarchies.
     * @throws DBExecutionException - When the database encounters 
     * error while searching.
     */
    public ArrayList<XMLDBModel> executeFetchHierarchyTask(FetchHierarchyTask task)
            throws DBExecutionException {
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
     * @throws DBExecutionException
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
     * @throws DBExecutionException
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
     * @throws DBExecutionException - When the database encounters 
     * error while searching.
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
     *          This will tell the DB layer to save/update a model already existing in the database.
     * @throws DBExecutionException
     */
    public void executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException {

        //call the execueTask method to execute the given task
        executeTask(task);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Adds tasks to the task queue for the executor thread to execute.
     * 
     * @param task - Task that needs to be executed on the database.
     * @throws DBConnectionException - When the executor thread 
     * fails due to an exception.
     */
    private void executeTask(Task task) throws DBExecutionException {

        /**
         * If this is the first task, then start the executor thread 
         */
        if (_taskQueue.size() == 0)
            _executorThread.run();
        /**
         * If the executor thread failed due to an exception; 
         * throw that exception 
         */
        if (_taskQueue.hasExecutionError())
            throw new DBExecutionException(_taskQueue
                    .getExecutionErrorMessage());
        /**
         * Add the task to the queue
         */
        _taskQueue.add(task);
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
