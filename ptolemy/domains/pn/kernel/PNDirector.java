/* Governs the execution of a CompositeActor with extended Kahn process
network semantics supporting non-deterministic mutations.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Green (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.kernel;
import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNDirector
/**
A PNDirector governs the execution of a CompositeActor with extended
Kahn-MacQueen process networks (PN) semantics. This model of computation has
been extended to support mutations of graphs in a non-deterministic way.
<p>
The thread that calls the various execution methods (initialize, prefire, fire
and postfire) on the director is referred to as the <i>directing thread</i>.
This directing thread might be the main thread responsible for the execution
of the entire simulation or might be the thread created by the executive
director of the containing composite actor.
<p>
In the PN domain, the director creates a thread (an instance of
ProcessThread), representing a Kahn process, for each actor in the model.
The threads are created in initialize() and started in the prefire() method
of the ProcessDirector. A process is considered <i>active</i> from its
creation until its termination. An active process can block when trying to
read from a channel (read-blocked), when trying to write to a channel
(write-blocked) or when waiting for a queued topology change request to be
processed (mutation-blocked).
<p>
A <i>deadlock</i> is when all the active processes are blocked.
The director is responsible for handling deadlocks during execution.
This director handles two different sorts of deadlocks, <i>real deadlock</i>
and <i>artificial deadlock</i>.
<p>
A real deadlock is when all the processes are blocked on a read meaning that
no process can proceed until it receives new data. The execution can be
terminated, if desired, in such a situation. If the container of this director
does not have any input ports (as is in the case of a top-level composite
actor), then the executive director or manager terminates the execution.
If the container has input ports, then it is upto the
executive director of the container to decide on the termination of the
execution. To terminate the execution after detection of a real deadlock, the
manager or the executive director calls wrapup() on the director.
<p>
An artificial deadlock is when all processes are blocked and at least one
process is blocked on a write. In this case the director increases the
capacity of the receiver with the smallest capacity amongst all the
receivers on which a process is blocked on a write.
This breaks the deadlock and the execution can resume.
<p>
Currently this director does not properly deal with topology mutations.

@author Mudit Goel
@version $Id$
*/
public class PNDirector extends BasePNDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     */
    public PNDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param workspace The workspace of this object.
     */
    public PNDirector(Workspace workspace) {
        super(workspace);
    }

    /**  Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in derived classes.
     */
    public PNDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero.
     *  The parameter "Initial_queue_capacity" has the
     *  same value as the director being cloned.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PNDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        PNDirector newobj = (PNDirector)super.clone(ws);
	newobj._mutationsRequested = false;
        return newobj;
    }

    /** Suspend the calling thread until a deadlock or request for topology
     *  changes is detected. On resuming, process the requests for topology
     *  changes if any, or handle the various deadlocks appropriately.
     *
     *  If requested, process the queued topology change requests. Registered
     *  topology listeners are informed of each change in a series of calls
     *  after successful completion of each request. If any queued
     *  request fails, the request is undone, and no further requests
     *  are processed. Note that change requests processed successfully
     *  prior to the failed request are <i>not</i> undone.
     *  Initialize any new actors created, create receivers for them,
     *  initialize the receivers and create new threads for the new actors
     *  created. After all threads
     *  are created, resume the execution and start the threads for the
     *  newly created actors.
     *
     *  If the resumption was on detection of a deadlock, break the deadlock
     *  if possible. If the deadlock is an artificial deadlock, then select the
     *  receiver with the smallest queue capacity on which any process is
     *  blocked on a write and increment the capacity of the contained queue.
     *  If the capacity is non-negative, then increment the capacity by 1.
     *  Otherwise set the capacity to 1. Unblock the process blocked on
     *  this receiver. Notify the thread corresponding to the blocked
     *  process. If the deadlock detected is a real deadlock, then do nothing.
     *
     *  This method is synchronized on the director. This method is normally
     *  called by the directing thread.
     *  @exception IllegalActionException If any of the called methods throw
     *  it.
     */
    public void fire() throws IllegalActionException {
        Workspace worksp = workspace();
        synchronized (this) { //Reset this as mutations must be done by now
            if( _getActiveActorsCount() == 0 ) {
                _notDone = false;
                return;
            }
            _mutationBlockCount = 0;
            _mutationsRequested = false;
            //Loop until a deadlock other than an artificial deadlock is
            //detected.
            while( _readBlockCount != _getActiveActorsCount() ) { 
            // !_areActorsStopped()) {
                //Sleep until a deadlock is detected or mutations are requested
		while( !_areActorsDeadlocked() ) {
                // while (!_areActorsDeadlocked() && !_areActorsStopped()) {
		    worksp.wait(this);
		}
                _notDone = _resolveDeadlock();
                /*
                if (!_areActorsStopped()) {
                    _notDone = _resolveDeadlock();
                }
                */
	    }
	}
	return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if a deadlock is detected. Return false otherwise.
     *  A detected deadlock is when all the active processes in the container
     *  are either blocked on a read, write or are waiting after requesting
     *  a mutation.
     *  @return true if a deadlock is detected.
     */
    protected synchronized boolean _areActorsDeadlocked() {
	return (_readBlockCount + _writeBlockCount +
                _mutationBlockCount == _getActiveActorsCount());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // This flag is set to true when mutations are pending
    private boolean _mutationsRequested = false;
}
