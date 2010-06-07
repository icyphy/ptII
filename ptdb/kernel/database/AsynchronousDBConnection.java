package ptdb.kernel.database;

import java.util.ArrayList;

import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.Task;
import ptdb.common.dto.TaskQueue;
import ptdb.common.exception.DBConnectionException;
import ptdb.kernel.database.DBConnection;

/**
 * 
 * @author abijwe
 *
 */
public class AsynchronousDBConnection implements DBConnection {

    /**
     * 
     */
    private ExecutorThread _executorThread;
    
    /*
     * 
     */
    private TaskQueue _taskQueue;

    /**
     * 
     * @throws DBConnectionException
     */
    public AsynchronousDBConnection()
            throws DBConnectionException {
        _taskQueue = new TaskQueue();
        _executorThread = new ExecutorThread(_taskQueue);
    }

    /**
     * 
     */
    public void abortConnection() {
        _taskQueue.setProcessingError();
    }

    /**
     * 
     */
    public void closeConnection() throws DBConnectionException {

        _taskQueue.setAllTasksAdded();

        boolean hasExecutionCompleted = false;
        int maxWait = 1000;
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
     * 
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBConnectionException {
        throw new DBConnectionException("Asynchronous DB Execution error - executeGetAttributes is not supported by this type of DBConnection");
    }

    /**
     * 
     * @param task
     * @throws DBConnectionException
     */
    private void executeTask(Task task) throws DBConnectionException {

        if (_taskQueue.size() == 0)
            _executorThread.run();

        if (_taskQueue.hasExecutionError())
            throw new DBConnectionException(_taskQueue.getExecutionErrorMessage());

        _taskQueue.add(task);
    }
}
