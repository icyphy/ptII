/* A listener for events from a manager.

 Copyright (c) 1997-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// ExecutionListener

/**
 An ExecutionListener listens for events that are issued
 during the execution of a model by a Manager.   In general, an
 object that implements this interface will probably be a front end such
 as an applet, a GUI, or a command-line interpreter,
 or an object that is communicating with a front end. Most of the
 events that are reported are state changes in the manager.
 <p>
 Some execution events happen in response to an asynchronous
 request.  For example, when the pause() method of a manager is
 called, a flag is set, and at the end of the next toplevel
 iteration, the manager will notice the flag and pause execution. In
 such cases, the execution listeners are not notified when the
 request occurs, but only when the execution thread has actually
 been suspended.

 @author Steve Neuendorffer, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Green (bart)
 @see Manager
 */
public interface ExecutionListener {
    /** Report an execution failure.   This method will be called
     *  when an exception or error is caught by a manager.
     *  Exceptions are reported this way when the run() or startRun()
     *  methods of the manager are used to perform the execution.
     *  If instead the execute() method is used, then exceptions are
     *  not caught, and are instead just passed up to the caller of
     *  the execute() method.  Those exceptions are not reported
     *  here (unless, of course, the caller of the execute() method does
     *  so).
     *
     *  @param manager The manager controlling the execution.
     *  @param throwable The throwable to report.
     */
    public void executionError(Manager manager, Throwable throwable);

    /** Report that the current execution has finished and
     *  the wrapup sequence has completed normally. The number of successfully
     *  completed iterations can be obtained by calling getIterationCount()
     *  on the manager.
     *
     *  @param manager The manager controlling the execution.
     */
    public void executionFinished(Manager manager);

    /** Report that the manager has changed state.
     *  To access the new state, use the getState() method of Manager.
     *
     *  @param manager The manager controlling the execution.
     *  @see Manager#getState()
     */
    public void managerStateChanged(Manager manager);
}
