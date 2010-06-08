package ptdb.kernel.database;

import java.util.ArrayList;

import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.Task;
import ptdb.common.dto.TaskQueue;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.database.DBConnection;

/**
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (abijwe)
 *  @Pt.AcceptedRating Red (abijwe)

 * This class provides an asynchronous mechanism of executing tasks over the database 
 * All the tasks are queued and executed parallel to the processing tasks
 * We plan to use this kind of connection for operations like saving a model that require a lot of processing
 *    
 * @author abijwe
 *
 */
public class AsynchronousDBConnection implements DBConnection {

    /**
     * Create the taskQueue to enqueue tasks and the executor thread that enables task execution in parallel
     * @throws DBConnectionException
     */
    public AsynchronousDBConnection() throws DBConnectionException {
        _taskQueue = new TaskQueue();
        _executorThread = new ExecutorThread(_taskQueue);
    }

    /**
     * Abort connection signifies that the execution needs to be terminated as there was some error in execution
     * Abort the execution by setting the execution flag as false 
     */
    public void abortConnection() {
        _taskQueue.setProcessingError();
    }

    /**
     * Close connection signifies that the processing is completed
     * close the connection if all tasks in the queue are executed successfully
     *  
     */
    public void closeConnection() throws DBConnectionException {

        /**
         * All tasks added denotes that the tasks have been added and processing is successfully completed 
         */
        _taskQueue.setAllTasksAdded();

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
     * Get Attributes Task is not supported by the asynchronous connection
     * Use a synchronous connection for that
     * @param task
     *          The task to be completed.
     * @return XMLDBModel
     *          The model object containing the MoML
     * @throws DBExecutionException
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetAttributes is not supported by this type of DBConnection");
    }

    /**
     * Get Models Task is not supported by the asynchronous connection
     * Use a synchronous connection for that
     * @param task
     *          The task to be completed.
     * @return XMLDBModel
     *          The model object containing the MoML
     * @throws DBExecutionException
     */
    public XMLDBModel executeGetModelsTask(GetModelsTask task)
            throws DBExecutionException {
        throw new DBExecutionException(
                "Asynchronous DB Execution error - executeGetModels is not supported by this type of DBConnection");
    }

    /**
     * Adds tasks to the task queue for the executor thread to execute
     * 
     * @param task - task that needs to be executed on the database
     * @throws DBConnectionException - When the executor thread fails due to an exception 
     */
    private void executeTask(Task task) throws DBExecutionException {

        /**
         * If this is the first task, then start the executor thread 
         */
        if (_taskQueue.size() == 0)
            _executorThread.run();
        /**
         * If the executor thread failed due to an exception; throw that exception 
         */
        if (_taskQueue.hasExecutionError())
            throw new DBExecutionException(_taskQueue
                    .getExecutionErrorMessage());
        /**
         * Add the task to the queue
         */
        _taskQueue.add(task);
    }

    /**
     * The executor thread that processes the tasks from the taskQueue
     */
    private ExecutorThread _executorThread;

    /**
     * Queue where the tasks are added one after the another  
     */
    private TaskQueue _taskQueue;

}
