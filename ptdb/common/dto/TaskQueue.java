package ptdb.common.dto;

import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////
//// TaskQueue
/**
 * Enqueue tasks before executing them.
 * 
 * @author Ashwini Bijwe
 * 
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 * 
 */
public class TaskQueue extends ArrayList<Task> {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return true if the task queue has all the required tasks, 
     * else return false 
     * @return True if all tasks are added, false otherwise
     */
    public boolean areAllTasksAdded() {
        return _allTasksAdded;
    }

    /**
     * Return the execution error message
     * @return The execution error message 
     */
    public String getExecutionErrorMessage() {
        return _errorMessage;
    }

    /**
     * Return true if the execution has completed; 
     * else return false
     * @return True if execution is complete, false otherwise
     */
    public boolean hasExecutionCompleted() {
        return _executionCompleted;
    }

    /**
     * Return true if any execution error has occurred; 
     * else return false 
     * @return True if execution error present, false otherwise
     */
    public boolean hasExecutionError() {
        return _isExecutionError;
    }

    /**
     * Return true if any processing error has occurred; 
     * else return false
     * @return True if processing error is present, false otherwise
     */
    public boolean hasProcessingError() {
        return _isProcessingError;
    }

    /**
     * Set the value for allTasksAdded to true
     */
    public void setAllTasksAdded() {
        _allTasksAdded = true;
    }

    /**
     * Set the value for executionCompleted to true
     */
    public void setExecutionCompleted() {
        _executionCompleted = true;
    }

    /**
     * Set the execution error to the given value
     * @param errorMessage
     */
    public void setExecutionError(String errorMessage) {
        _isExecutionError = true;
        this._errorMessage = errorMessage;
    }

    /**
     * Set the processing error to true
     */
    public void setProcessingError() {
        _isProcessingError = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////   
    private boolean _executionCompleted = false;

    private boolean _isProcessingError = false;

    private boolean _isExecutionError = false;

    private String _errorMessage;

    private boolean _allTasksAdded = false;

}
