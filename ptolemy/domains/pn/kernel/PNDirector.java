/* Governs the execution of a CompositeActor with extended Kahn process 
network semantics supporting non-deterministic mutations.

 Copyright (c)  The Regents of the University of California.
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
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PNDirector
/**
A PNDirector governs the execution of a CompositeActor with extended 
Kahn-MacQueen process networks (PN) semantics. This model of computation has 
been extended to support mutations of graphs in a non-deterministic way. 
<p>
The thread that calls the various execution methods (initialize, prefire, fire
and postfire) on the director is referred to as the <i>directing thread</i>. 
This directing thread might be the main thread reponsible for the execution 
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
This director also permits pausing of the execution. An execution is paused
when all active processes are blocked or paused (atleast one process is 
paused). In case of PN, a process can be paused only when it tries to 
communicate with other processes. Thus a process can be paused in the get() 
or put() methods of the receivers alone. In case a pause is requested, the 
process does not return from the call to the get() or the put() method of the
receiver until the execution is resumed. If there is a process that does 
not communicate with other processes in the model, then the simulation can 
never pause in that model.
<p>
A <i>deadlock</i> is when all the active processes are blocked.
The director is responsible for handling deadlocks during execution.
This director handles two different sorts of deadlocks, real deadlock and
artificial deadlock. 
<p>
A real deadlock is when all the processes are blocked on a read meaning that
no process can proceed until it receives new data. The execution can be 
terminated, if desired, in such a situation. If the container of this director
is the top-level composite actor, then the manager terminates the execution. 
If the container is not the top-level composite actor, then it is upto the 
executive director of the container to decide on the termination of the 
execution. To terminate the execution, the manager or the executive director
calls wrapup() on the director.
<p>
An artificial deadlock is when all processes are blocked and atleast one 
process is blocked on a write. In this case the director increases the 
capacity of the receiver with the smallest capacity amongst all the 
receivers on which a process is blocked on a write. 
This breaks the deadlock and the execution can proceed.
<p>
This director is capable of handling dynamic changes to the topology, 
i.e. mutations of graphs. These mutations can be non-deterministic. In PN, 
since the execution of a model is not centralized, it is impossible to define 
a useful point in the execution of all the active processes where 
mutations can occur. Due to this, PN permits mutations
to happen as soon as they are requested. Thus as soon as a process queues 
mutations in PN, the director is notified and the director pauses the 
execution. Then it performs all the mutations requested, and notifies the 
topology listeners. After this the execution is resumed.
<p>
In case of PN, a process can be paused only when it tries to communicate with
other processes. A pause in PN is defined as a state when all processes are 
blocked or are explicitly paused in the get() or
put() method of the receiver. Thus if there is a process that does not 
communicate with other processes in the model, then the simulation may 
never pause in that model.
<p>


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

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the queues in all 
     *  the receivers created in the PN domain.
     *  @param name Name of this director.
     */
    public PNDirector(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the queues in all 
     *  the receivers created in the PN domain.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public PNDirector(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero 
     *  and it is not paused. The parameter "Initial_queue_capacity" has the 
     *  same value as the director being cloned.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PNDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        PNDirector newobj = (PNDirector)super.clone(ws);
	newobj._urgentMutations = false;
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
     *  initialize them and create new threads for them. After all threads
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
    public void fire()
	    throws IllegalActionException {
	boolean urgentmut;
        Workspace worksp = workspace();
	synchronized (this) {
	    while (!_checkForDeadlock() && !_urgentMutations) {
		//System.out.println("Waiting with mutations = "+_urgentMutations);
		worksp.wait(this);
	    }
	    urgentmut = _urgentMutations;
	    //_urgentMutations = false;
	}
	//System.out.println(" deadlock = "+deadl+" and mut ="+urgentmut);
	if (urgentmut) {
	    //System.out.println("Performed mutations");
            try {
                _processTopologyRequests();
		//_urgentMutations = false;
                // FIXME: Should type resolution be done here?
            } catch (TopologyChangeFailedException e) {
                throw new IllegalActionException("Name duplication error: " +
                        e.getMessage());
            }
	    return;
	} else {
	    //_notdone = !_handleDeadlock();
	    _handleDeadlock();
	}
        //System.out.println("Done firing");
    }

    /** Add a topology change request to the request queue and suspend the 
     *  calling thread until the requests are processed. These changes 
     *  are executed in the fire() method of the director.
     *  After queuing the requests, increment the count of processes blocked
     *  while waiting for the topology change requests to be processed 
     *  (mutation-blocked). Notify the directing thread 
     *  of pending topology changes. The directing thread pauses the execution
     *  and processes the queued topology change requests in the fire() method
     *  of the director. After the directing thread processes all the requests,
     *  it notifies the calling thread to resume. On resuming, decrease the 
     *  count of processes blocked while waiting for topology changes.
     *  This method is synchronized on the director.
     *  
     *  @param request An object with commands to perform topology changes
     *  and to inform the topology listeners of the same.
     *  @see ptolemy.kernel.event.TopologyChangeRequest
     *  @see ptolemy.kernel.event.TopologyListener
     *  @see fire()
     */
    public void queueTopologyChangeRequest(TopologyChangeRequest request) {
	super.queueTopologyChangeRequest(request);
	synchronized(this) {
	    _urgentMutations = true;
	    _informOfMutationBlock();
	    notifyAll();
	    while(_urgentMutations) {
		try {
		    wait();
		} catch (InterruptedException e) {
		    System.err.println(e.toString());
		}
	    }
	    _informOfMutationUnblock();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if a deadlock is detected. Return false otherwise.
     *  @return true if a deadlock is detected.
     */
    protected synchronized boolean _checkForDeadlock() {
	if (_readBlockCount + _writeBlockCount + _mutationBlockCount
		>= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Return true if the execution has paused. Return false otherwise.
     *  @return true if the execution has paused.
     */
    protected synchronized boolean _checkForPause() {
	if (_readBlockCount + _writeBlockCount + _getPausedActorsCount()
		+ _mutationBlockCount >= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Pause the execution and process the queued topology change requests. 
     *  Registered topology
     *  listeners are informed of each change in a series of calls
     *  after successful completion of each request. If any queued
     *  request fails, the request is undone, and no further requests
     *  are processed. Note that change requests processed successfully
     *  prior to the failed request are <i>not</i> undone.
     *
     *  Initialize any new actors created, create receivers for them, 
     *  initialize them and create new threads for them. After all threads
     *  are created, resume the execution and start the threads for the 
     *  newly created actors.
     *
     *  @exception IllegalActionException If any of the pending requests have
     *  already been implemented.
     *  @exception TopologyChangeFailedException If any of the requests fails.
     */
    protected void _processTopologyRequests()
            throws IllegalActionException, TopologyChangeFailedException {
	Workspace worksp = workspace();
	pause();
	super._processTopologyRequests();
	LinkedList threadlist = new LinkedList();
	//FIXME: Where does the type resolution go?
	Enumeration newactors = _newActors();
	while (newactors.hasMoreElements()) {
	    Actor actor = (Actor)newactors.nextElement();
	    actor.createReceivers();
	    actor.initialize();
	    ProcessThread pnt = new ProcessThread(actor, this);
	    threadlist.insertFirst(pnt);
	    _addNewThread(pnt);
	}
	//Resume the paused actors
	resume();
	_urgentMutations = false;
	//Resume the actors paused on mutations
	synchronized(this) {
	    notifyAll();
	}
	Enumeration threads = threadlist.elements();
	//Starting threads;
	while (threads.hasMoreElements()) {
	    ProcessThread pnt = (ProcessThread)threads.nextElement();
	    pnt.start();
	    //System.out.println("Started a thread for "+((Entity)pnt.getActor()).getName());
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private boolean _urgentMutations = false;
}





