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
package ptdb.common.dto;

///////////////////////////////////////////////////////////////////
//// Task
/**
 *  Abstract class that defines a task that can be executed over the database.
 *
 *  @author Ashwini Bijwe
 *
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (abijwe)
 *  @Pt.AcceptedRating Red (abijwe)
 */
public abstract class Task {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return true if the given task is an update task or
     * false if it is a select task.
     * @return True if task is an update task, false otherwise.
     */
    public boolean isUpdateTask() {
        return _isUpdateTask;
    }

    /**
     * Set the given task as an update task or select task
     * depending on the value of isUpdateTask.
     * @param isUpdateTask Boolean that specifies if the
     *                       given task is an update task.
     */
    public void setIsUpdateTask(boolean isUpdateTask) {
        _isUpdateTask = isUpdateTask;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /**
     * There are two types of tasks - Update/Write tasks
     * that change the data in the database and Select/Read
     * that read the data from the database.
     *
     * If this is true, then the task is an Update/Write task.
     * If this is false, then the task is a Read/Select task.
     */
    protected boolean _isUpdateTask;
}
