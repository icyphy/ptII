/* An ExecutionListener is able to receive ExecutionEvents from the Manager.

Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// ExecutionListener
/**
An ExecutionListener is able to receive ExecutionEvents that are issued
during the execution of a model by a Manager.   In general, an
object that implements this interface will probably be a front end such
as a GUI for
the Ptolemy II system, or an object that is communicating with a front end.
The events are issued only when the event actually occurs, not when it is
requested.   For example: A Manager receives a call to pause(), corresponding
to a request to pause execution after the current top-level iteration ends.
The executionPaused() method will not be called until the iteration actually
completes and the top-level thread is suspended.   These calls are synchronous
to the model and will block further execution within the topology
until they return.

@author Steve Neuendorffer
@version $Id$
*/

public interface ExecutionListener {

    /** Called to report an execution failure.   This method will be called
     *  when an Exception is caught at the top level of execution.  Instead
     *  of allowing the exception to propagate out the user interface,
     *  it is caught
     *  here and encapsulated within an event.   The event will also report
     *  the toplevel iteration during which the exception was caught,
     *  if possible.
     *
     *  @param event An ExecutionEvent that contains a valid exception.
     */
    public void executionError(ExecutionEvent event);

    /** Called to report that the current execution finished and
     *  the wrapup sequence completed normally.   The execution event will
     *  contain the number of the last successfully completed iteration.
     *
     *  @param event An ExecutionEvent with a valid iteration field.
     */
    public void executionFinished(ExecutionEvent event);

    /** Called to report that a top-level iteration has begun.   The execution
     *  event will contain the number of the iteration that is starting.
     *
     *  @param event An ExecutionEvent with valid iteration field.
     */
    public void executionIterationStarted(ExecutionEvent event);

    /** Called to report a successful pause of execution.   The execution
     *  event will contain the number of the last successfully completed
     *  iteration.
     *
     *  @param event An ExecutionEvent with a valid iteration field.
     */
    public void executionPaused(ExecutionEvent event);

    /** Called to report a successful resumption of execution.   The execution
     *  event will contain the number of the last successfully completed
     *  iteration.
     *
     *  @param event An ExecutionEvent with a valid iteration field.
     */
    public void executionResumed(ExecutionEvent event);

    /** Called to report a successful start of execution.   This method
     *  indicates that it was valid for execution to begin, and that any
     *  initialization performed within the manager thread, such as
     *  creating a thread for the simulation, has completed successfully.
     *  It does not imply that the initialize method was successfully
     *  called on all the actors, only that the manager thread is ready
     *  begin execution of the model.
     *
     *  @param event an ExecutionEvent with iterations set equal to zero.
     */
    public void executionStarted(ExecutionEvent event);

    /** Called to report termination of execution.   This method is called
     *  when the Manager has completed terminating execution of a model
     *  via the terminate() method.
     *
     *  @param event an ExecutionEvent.
     */
    public void executionTerminated(ExecutionEvent event);
}
