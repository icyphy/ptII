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

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.Task;
import ptdb.common.dto.TaskQueue;
import ptdb.common.dto.UpdateParentsToNewVersionTask;
import ptdb.common.exception.CircularDependencyException;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.util.DBConnectorFactory;

///////////////////////////////////////////////////////////////////
//// ExecutorThread

/**
 * Execute the queries asynchronously.
 *
 * <p>Monitor the asynchronous connection's task queue
 * and execute tasks one by one over a synchronous connection.</p>
 *
 * @author Ashwini Bijwe, Yousef Alsaeed
 *
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class ExecutorThread implements Runnable {

    /** Construct an instance of the executor thread that
     *  performs tasks one by one from the taskQueue.
     *
     * @param taskQueue List of Tasks that need to be executed.
     *
     * @exception DBConnectionException - When we face a problem
     * while creating a database connection. These problems could
     * be that configured connection class does not exist,
     * the path for the database is not found, the container name
     * is incorrect, the connection to the database
     * could not be established etc.
     */
    public ExecutorThread(TaskQueue taskQueue) throws DBConnectionException {

        this._taskQueue = taskQueue;
        _dbConn = DBConnectorFactory.getSyncConnection(true);
        _noOfTasksExecuted = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Manage the execution of tasks from the task queue.
     *
     * <p>It aborts its working if it encounter an exception
     * or if the processing error flag in the taskQueue
     * is set to true. </p>
     *
     * <p>Stop execution if the taskQueue is completed
     * and all the tasks are executed
     * or if it exceeds its max wait time of 50 seconds. </p>
     *
     */
    @Override
    public void run() {
        int maxWait = 100;
        while (maxWait != 0) {

            /**
             * In case of a processing error, we abort the connection
             * and stop the execution of this thread
             */
            if (_taskQueue.hasProcessingError()) {
                _abortConnection(null);
                return;
            }

            try {

                if (_noOfTasksExecuted < _taskQueue.size()) {
                    /*
                     * If there exists a task in the TaskQueue that
                     * as not been executed, then the task is executed,
                     * the task counter incremented and the maxWait
                     * counter is reset.
                     */
                    _executeTask();
                    _noOfTasksExecuted++;
                    maxWait = 100;

                } else if (_taskQueue.size() == _noOfTasksExecuted
                        && _taskQueue.areAllTasksAdded()) {
                    /*
                     * If all the tasks are executed and the taskQueue is
                     * complete, then the processing is completed.
                     * We then close the connection and mark execution
                     * as complete and stop the execution of this thread.
                     */
                    _closeConnection();
                    return;

                } else {
                    /*
                     * If neither a new task is present in the queue nor
                     * the processing is completed,
                     * the thread decrements the wait counter
                     * and goes to sleep.
                     */
                    maxWait--;
                    Thread.sleep(500);

                }

            } catch (DBConnectionException e) {
                /**
                 * In case of error, we abort the connection and set the
                 * ExecutionError and stop the execution of this thread
                 */
                _abortConnection("Database execution error - " + e.getMessage());
                return;

            } catch (InterruptedException e) {
                _abortConnection("Database execution error - " + e.getMessage());
                e.printStackTrace();
                return;
            } catch (DBExecutionException e) {
                _abortConnection("Database execution error - " + e.getMessage());
                e.printStackTrace();
                return;
            } catch (ModelAlreadyExistException e) {
                _abortConnection("Database execution error - " + e.getMessage());
                e.printStackTrace();
                return;
            } catch (CircularDependencyException e) {
                _abortConnection("Circular dependency error - "
                        + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        System.out.println("DB Connection thread timed out" + this.toString());
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Delegate the call to the abortConnection() API of DBConnection.
     * Set the error message in the task queue if the error message is not null.
     * @param errorMessage Error message due to which the transaction needs to
     * be aborted.
     */
    private void _abortConnection(String errorMessage) {
        try {
            _dbConn.abortConnection();

        } catch (DBConnectionException e) {
            e.printStackTrace();
        }
        if (errorMessage != null) {
            _taskQueue.setExecutionError(errorMessage);
        }
    }

    /**
     * Commit and close the connection.
     * Mark execution completed in the task queue.
     *
     * @exception DBConnectionException If thrown while closing or committing the
     * database connection.
     */
    private void _closeConnection() throws DBConnectionException {
        _dbConn.commitConnection();
        _dbConn.closeConnection();
        _taskQueue.setExecutionCompleted();
    }

    /**
     * Delegate the call to the appropriate API of DBConnection
     * depending on the type of the task it is executing
     * @exception DBExecutionException If thrown while executing the tasks.
     * @exception ModelAlreadyExistException Thrown when the model being created
     * is already in the database.
     * @exception CircularDependencyException If thrown while creating the model
     * reference.
     */
    private void _executeTask() throws DBExecutionException,
            ModelAlreadyExistException, CircularDependencyException {
        Task task = _taskQueue.get(_noOfTasksExecuted);

        //if the task is of type save model task, then execute the proper method from the connection
        if (task instanceof SaveModelTask) {
            _dbConn.executeSaveModelTask((SaveModelTask) task);
        }

        //if the task is of type CreateModelTask then execute the proper method from the connection
        else if (task instanceof CreateModelTask) {
            _dbConn.executeCreateModelTask((CreateModelTask) task);
        }

        else if (task instanceof UpdateParentsToNewVersionTask) {
            _dbConn.executeUpdateParentsToNewVersion((UpdateParentsToNewVersionTask) task);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /** This is the actual database connection(synchronous)
     * over which the tasks are executed.
     */
    private DBConnection _dbConn;

    /** This is used to keep track of the number of tasks
     * executed by the executor thread.
     *
     */
    private int _noOfTasksExecuted;

    /**
     * This is the taskQueue into which an Asynchronous
     * connection adds the tasks for execution.
     *
     * The Executor thread reads tasks one by one from this
     * queue and executes them on the database.
     *
     * The taskQueue is also used as a communication mechanism
     * between the Executor thread and the Asynchronous connection.
     *
     * Each informs the other of an error by setting it
     * the error flag in the taskQueue.
     */
    private TaskQueue _taskQueue;

}
