/* A ProcessStateListener is able to receive ProcessStateEvents from the Processes.

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

@ProposedRating Yellow (mudit@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.domains.pn.kernel;

//////////////////////////////////////////////////////////////////////////
//// ProcessStateListener
/**
An ProcessStateListener is able to receive ProcessStateEvents that are issued
during the execution of a process by a ProcessThread or director in PN.   
In general, an object that implements this interface will probably be a front 
end such as a execution visualization tool for
the Ptolemy II system, or an object that is communicating with a front end.
The events are issued only when the event actually occurs, not when it is
requested.   For example: A process receives a call to pause(), but the 
process may pause some time after the pause was requested. The processPaused()
method will not be called until the process actually pauses and corresponding 
thread is suspended.   

@author Mudit Goel
@version $Id$
*/

public interface ProcessStateListener {

    /** Called to report an execution failure. This method will be called
     *  when an Exception is caught by a ProcessThread which calls the 
     *  execution methods of an actor repeatedly.  Instead
     *  of allowing the exception to propagate out the user interface,
     *  it is caught and encapsulated within an event. The reference
     *  to the actor corresponding to the process is encapsulated in the 
     *  processStateEvent. 
     *  @param event A processStateEvent that contains a reference to an
     *  actor and a valid exception.
     */
    public void processExecutionError(ProcessStateEvent event);

    /** Called to report that the execution of a process finished and
     *  the wrapup sequence completed normally.   The execution event will
     *  contain a reference to the actor corresponding to the process that 
     *  finished. 
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor.
     */
    public void processFinished(ProcessStateEvent event);

    /** Called to report the pausing of a process. The processStateEvent 
     *  will contain a reference to the actor corresponding to the process.
     * 
     *  @param event A processStateEvent that contains a reference to an
     *  actor.
     */
    public void processPaused(ProcessStateEvent event);

    /** Called to report a resumption of a process. This can be a resumption
     *  after either a pausing or blocking of the process. The 
     *  processStateEvent will contain a reference to the actor corresponding 
     *  to the process. 
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor.
     */
    public void processResumed(ProcessStateEvent event);

    /** Called to report that a process has been started. The processStateEvent
     *  will contain a reference to the actor corresponding to the process. 
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor.
     */
    public void processStarted(ProcessStateEvent event);

    /** Called to report the blocking of a process on a read. 
     *  The event also contains a reference to the actor corresponding to 
     *  the blocking process. 
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor. 
     */
    public void processBlockedOnRead(ProcessStateEvent event);

    /** Called to report the blocking of a process on a write. 
     *  The event also contains a reference to the actor corresponding to 
     *  the blocking process. 
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor. 
     */
    public void processBlockedOnWrite(ProcessStateEvent event);

    /** Called to report the blocking of a process on a delay. 
     *  The event also contains a reference to the actor corresponding to 
     *  the blocking process. 
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor. 
     */
    public void processBlockedOnDelay(ProcessStateEvent event);

    /** Called to report the blocking of a process on a mutation. 
     *  The event also contains a reference to the actor corresponding to 
     *  the blocking process. 
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor. 
     */
    public void processBlockedOnMutation(ProcessStateEvent event);
   
    /** Called to report an abrupt termination of a process. This method is 
     *  called when the process was terminated because of an urgent request
     *  from the director.
     *
     *  @param event A processStateEvent that contains a reference to an
     *  actor.
     */
    public void processTerminated(ProcessStateEvent event);
}




