/*
@Copyright (c) 2010-2013 The Regents of the University of California.
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
package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////////
//// TaskQueue
/**
 * Enqueue tasks before executing them.
 *
 * @author Ashwini Bijwe
 *
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
@SuppressWarnings("serial")
public class TaskQueue extends ArrayList<Task> {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return true if the task queue has all the required tasks,
     * else return false.
     * @return True if all tasks are added, false otherwise.
     */
    public boolean areAllTasksAdded() {
        return _allTasksAdded;
    }

    /**
     * Return the execution error message.
     * @return The execution error message.
     */
    public String getExecutionErrorMessage() {
        return _errorMessage;
    }

    /**
     * Return true if the execution has completed;
     * else return false.
     * @return True if execution is complete, false otherwise.
     */
    public boolean hasExecutionCompleted() {
        return _executionCompleted;
    }

    /**
     * Return true if any execution error has occurred;
     * else return false.
     * @return True if execution error present, false otherwise.
     */
    public boolean hasExecutionError() {
        return _isExecutionError;
    }

    /**
     * Return true if any processing error has occurred;
     * else return false.
     * @return True if processing error is present, false otherwise.
     */
    public boolean hasProcessingError() {
        return _isProcessingError;
    }

    /**
     * Set the value for allTasksAdded to true.
     */
    public void setAllTasksAdded() {
        _allTasksAdded = true;
    }

    /**
     * Set the value for executionCompleted to true.
     */
    public void setExecutionCompleted() {
        _executionCompleted = true;
    }

    /**
     * Set the execution error to the given value.
     * @param errorMessage Error message for the
     * execution error.
     */
    public void setExecutionError(String errorMessage) {
        _isExecutionError = true;
        this._errorMessage = errorMessage;
    }

    /**
     * Set the processing error to true.
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
