/* An ExecutionListener is able to receive ExecutionEvents from the Manager.

Copyright (c) 1997-1998 The Regents of the University of California.
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
*/

package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// ExecutionListener
/**
An ExecutionListener is able to receive ExecutionEvents that are issued 
during the execution of a simulation by a Manager.   In general, an
object that implements this interface will probably be a frontend for 
the Ptolemy II system, or an object that is communicating with a frontend.
The events are issued only when the event actually occurs, not when it is 
requested.   For example: A Manager receives a call to pause(), corresponding 
to a request to pause execution after the current toplevel iteration ends.
The executionPaused() method will not be called until the iteration actually 
completes and the toplevel thread is suspended.   These calls are synchronous
to the ptolemy system and will block further execution within the topology
until they return.

@author Steve Neuendorffer
@version: $Id$
*/

public interface ExecutionListener {

    /** Called to report an execution failure.   This method will be called 
     *  when an Exeception is caught at the toplevel of execution.  Instead 
     *  of allowing the exception to propagate out of ptolemy, it is caught
     *  here and encapsulated within an event.   It will also report the 
     *  iteration during which the exception was caught, if possible.
     *
     *  @param event an ExecutionEvent that contains a valid exception.
     */
    public void executionError(ExecutionEvent event);

    /** Called to report that the current iteration finished and 
     *  the wrapup sequence completed normally.   The execution event will
     *  contain the number of the last successfully completed iteration.
     *
     *  @param event an ExecutionEvent with valid iteration field.
     */
    public void executionFinished(ExecutionEvent event);

    /** Called to report that a toplevel iteration has begun.   The execution
     *  event will contain the number of the iteration that is starting.
     *
     *  @param event an ExecutionEvent with valid iteration field.
     */
    public void executionIterationStarted(ExecutionEvent event);
    
    /** Called to report a successful pause of execution.   The execution
     *  event will contain the number of the last successfully completed 
     *  iteration.
     *  
     *  @param event an ExecutionEvent with a valid iterations field.
     */
    public void executionPaused(ExecutionEvent event);
    
    /** Called to report a successful resumption of execution.   The execution
     *  event will contain the number of the last successfully completed 
     *  iteration.
     *  
     *  @param event an ExecutionEvent with a valid iterations field.
     */
    public void executionResumed(ExecutionEvent event);

    /** Called to report a successful start of execution.   This method 
     *  indicates that it was valid for execution to begin, and that any 
     *  initialization performed by the manager has completed successfully.
     *  It does not imply that initialize completed sucessfully.
     *
     *  @param event an ExecutionEvent with iterations set equal to zero.
     */
    public void executionStarted(ExecutionEvent event);

    /** Called to report termination of execution.   This method is called 
     *  when the Manager has done everything it can to forcibly stop a
     *  running execution.
     *
     *  @param event an ExecutionEvent.
     */    
    public void executionTerminated(ExecutionEvent event);
    
}
