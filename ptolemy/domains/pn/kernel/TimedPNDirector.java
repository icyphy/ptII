/* Governs the execution of a CompositeActor with timed Kahn process 
network semantics.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
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
//// TimedPNDirector
/**
A TimedPNDirector governs the execution of a CompositeActor with
Kahn-MacQueen process networks (PN) semantics extended by introduction of a
notion of global time. This model of computation has 
been extended to support mutations of graphs (dynamic changes to topology) in 
a deterministic way. 
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
(write-blocked), when waiting for a queued topology change request to be
processed (mutation-blocked) or when waiting for time to progress. Time can
progress for an active process in this model of computation only when the
process is  blocked.
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
This director handles three different sorts of deadlocks, real deadlock, timed
deadlock and artificial deadlock. 
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
An artificial deadlock is when all processes are blocked and atleast one 
process is blocked on a write. In this case the director increases the 
capacity of the receiver with the smallest capacity amongst all the 
receivers on which a process is blocked on a write. 
This breaks the deadlock and the execution can proceed.
<p>
A timed deadlock is when all the processes under the control of this 
director are blocked, atleast one process is blocked on a delay (timed-block)
and no process is blocked on a write. This director supports a notion of global
time. All active processes that are not blocked and are executing concurrently
are executing at the same global time. A process that wants time to advance, 
suspends itself by calling the fireAt() method of the director and specifies 
the time it wants to be awakened at. Time can advance only when a timed 
deadlock occurs. In such a case, the director
processes requests for mutations, if any. Otherwise the director advances time
to the time when the first delay-blocked process can be awakened.
<p>
This director is capable of handling mutations of graphs (dynamic changes to 
topology). These mutations are deterministic and are performed only when a 
timed deadlock occurs and the mutations are pending (topology changes have been
requested). On requesting a mutation, the process queues the request 
and suspends (mutation-blocked) until the request for topology changes is 
processed. The directing thread processes these requests on the next occurence
of a timed-deadlock. After this the directing thread awakens the processes 
blocked on a mutation (mutation-blocked) and the execution resumes.
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
public class TimedPNDirector extends BasePNDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the FIFO queues in all 
     *  the receivers created in the PN domain. 
     *  A priority queue is created to keep a track of delayed actors sorted
     *  by the time at which they wish to be awakened. 
     */
    public TimedPNDirector() {
        super();
        _eventQueue = new CalendarQueue(new DoubleCQComparator());
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the queues in all 
     *  the receivers created in the PN domain.
     *  A priority queue is created to keep a track of delayed actors sorted
     *  by the time at which they wish to be awakened. 
     *  @param name Name of this director.
     */
    public TimedPNDirector(String name) {
        super(name);
        _eventQueue = new CalendarQueue(new DoubleCQComparator());
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the queues in all 
     *  the receivers created in the PN domain.
     *  A priority queue is created to keep a track of delayed actors sorted
     *  by the time at which they wish to be awakened.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public TimedPNDirector(Workspace workspace, String name) {
        super(workspace, name);
        _eventQueue = new CalendarQueue(new DoubleCQComparator());
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
     *  @return The new TimedPNDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        TimedPNDirector newobj = (TimedPNDirector)super.clone(ws);
	newobj._eventQueue.clear();
	newobj._currenttime = 0;
	newobj._delayBlockCount = 0;
	newobj._timedMutations = false;
        return newobj;
    }
    /** Suspend the calling thread until a deadlock
     *  is detected. On resuming, handle the various deadlocks appropriately.
     *  Break the deadlock if possible. If the deadlock is an artificial 
     *  deadlock, then select the receiver with the smallest queue capacity on
     *  which any process is blocked on a write and increment the capacity of 
     *  the contained queue. 
     *  If the capacity is non-negative, then increment the capacity by 1. 
     *  Otherwise set the capacity to 1. Unblock the process blocked on 
     *  this receiver. Notify the thread corresponding to the blocked 
     *  process. 
     *  If the deadlock is a timed deadlock, process any pending topology 
     *  change requests. If there are no pending requests, then advance the
     *  time to the earliest value when a delay blocked process can be 
     *  awakenened. If the  deadlock detected is a real deadlock, then do 
     *  nothing.
     *  
     *  If processing the queued topology change requests, then inform the
     *  registered topology listeners of each change in a series of calls
     *  after successful completion of each request. If any queued
     *  request fails, the request is undone, and no further requests
     *  are processed. Note that change requests processed successfully
     *  prior to the failed request are <i>not</i> undone.
     *  Initialize any new actors created, create receivers for them, 
     *  initialize them and create new threads for them. After all threads
     *  are created, resume the execution and start the threads for the 
     *  newly created actors. This method returns only on occurence of a real
     *  deadlock.
     *  
     *  <b>This method is synchronized on the director. This method is normally
     *  called by the directing thread. </b>
     *  @exception IllegalActionException If any of the called methods throw
     *  it. 
     */
    public void fire()
	    throws IllegalActionException {
        boolean timedmut;
        Workspace worksp = workspace();
	while (_readBlockCount != _getActiveActorsCount()) {
	    synchronized (this) {
		while (!_checkForDeadlock()) {
		    worksp.wait(this);
		}
		timedmut = _timedMutations;
	    }
	    if (_writeBlockCount != 0) {
		System.out.println("Artificial deadlock");
		_incrementLowestWriteCapacityPort();
	    } else if (timedmut) {
		try {
		    _processTopologyRequests();
		    // FIXME: Should type resolution be done here?
		} catch (TopologyChangeFailedException e) {
		    throw new IllegalActionException("TopologyChangeFailed: " +
			    e.getMessage());
		}
	    } else {
		_notdone = !_handleDeadlock();
	    }
	}
	return;
    }

    /** Suspend the calling process until the time has advanced to atleast the
     *  time specified by the method argument.
     *  Add the actor corresponding to the calling process to the priority 
     *  queue and sort it by the time specified by the method argument.
     *  Increment the count of the actors blocked on a delay. Suspend the 
     *  calling process until the time has advanced to atleast the time 
     *  specified by the method argument. Resume the execution of the calling
     *  process and return.
     *  @exception IllegalActionException If the operation is not
     *  permissible (e.g. the given time is in the past).
     */
    public synchronized void fireAt(Actor actor, double newfiringtime)
            throws IllegalActionException {
        _eventQueue.put(new Double(newfiringtime), actor);
        _informOfDelayBlock();
        try {
            while (getCurrentTime() < newfiringtime) {
                wait(); //Should I call workspace().wait(this) ?
            }
        } catch (InterruptedException e) {}
    }

    /** Return the current time of the simulation.
     *  @return the current time of the simulation
     *  @deprecate This should be removed after the Director class gets the 
     *  functionality.
     */
    public synchronized double getCurrentTime() {
        return _currenttime;
    }


    /** Set the current time.
     *  @exception IllegalActionException If time cannot be changed
     *   due to the state of the simulation.
     *  @param newTime The new current simulation time.
     *  @deprecate This should be removed after the Director class gets the 
     *  functionality.
     */
    public synchronized void setCurrentTime(double newTime)
            throws IllegalActionException {
        _currenttime = newTime;
    }

    /** Add a topology change request to the request queue and suspend the 
     *  calling thread until the requests are processed. These changes 
     *  are executed in the fire() method of the director.
     *  After queuing the requests, increment the count of processes blocked
     *  while waiting for the topology change requests to be processed 
     *  (mutation-blocked). On detecting a timed-deadlock,
     *  the directing thread processes the queued topology change requests in 
     *  the fire() method
     *  of the director. After the directing thread processes all the requests,
     *  it notifies the all the processes blocked on a mutation (including the
     *  calling process) to resume. The count of mutation-blocked processes
     *  is decreased by the directing thread.
     *  This method is synchronized on the director.
     *  
     *  @param request An object with commands to perform topology changes
     *  and to inform the topology listeners of the same.
     *  @see ptolemy.kernel.event.TopologyChangeRequest
     *  @see ptolemy.kernel.event.TopologyListener
     *  @see #fire
     */
    public void queueTopologyChangeRequest(TopologyChangeRequest request) {
	super.queueTopologyChangeRequest(request);
	synchronized(this) {
	    _timedMutations = true;
	    _informOfMutationBlock();
	    while(_timedMutations) {
		try {
		    wait();
		} catch (InterruptedException e) {
		    System.err.println(e.toString());
		}
	    }
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if a deadlock is detected. Return false otherwise.
     *  @return true if a deadlock is detected.
     */
    protected synchronized boolean _checkForDeadlock() {
	if (_readBlockCount + _writeBlockCount + _delayBlockCount 
		+ _mutationBlockCount >= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Return true if the execution has paused or deadlocked. 
     *  Return false otherwise. This method should be used only to detect
     *  if the execution has paused. To detect deadlocks, _checkForDeadlock
     *  should be used.
     *  @return true if the execution has paused.
     */
    protected synchronized boolean _checkForPause() {
	if (_readBlockCount + _writeBlockCount + _getPausedActorsCount() +
		_mutationBlockCount + _delayBlockCount 
		>= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Increment by 1 the count of actors waiting for the time to advance.
     *  Check for a resultant deadlock or pausing of the 
     *  execution. If either of them is detected, then notify the directing 
     *  thread of the same.
     */
    protected synchronized void _informOfDelayBlock() {
	_delayBlockCount++;
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }

    /** Decrease by 1 the count of processes blocked on a delay.
     */
    protected synchronized void _informOfDelayUnblock() {
	_delayBlockCount--;
	return;
    }

    /** Return true on detection of a real deadlock. Otherwise break the 
     *  deadlock and return false otherwise. 
     *  On detection of a timed deadlock, advance time to the earliest
     *  time that a delayed process is waiting for, wake up all the actors
     *  waiting for time to advance to the new time, and remove them from 
     *  the priority queue. 
     *  
     *  @return true if a real deadlock is detected, false otherwise.
     *  @exception IllegalActionException Not thrown in this base class. This
     *  might be thrown by derived classes.
     */
    protected boolean _handleDeadlock()
	    throws IllegalActionException {
            //Check if there are any events in the future.
	if (_delayBlockCount ==0) {
	    System.out.println("real deadlock. Everyone would be erased");
	    return true;
	} else {
	    //Advance time to next possible time.
	    synchronized(this) {
		try {
		    _eventQueue.take();
		    _currenttime = 
			((Double)(_eventQueue.getPreviousKey())).doubleValue();
		    _informOfDelayUnblock();
		} catch (IllegalAccessException e) {
		    throw new InternalErrorException("Inconsistency"+
			    " in number of actors blocked on delays count"+
			    " and the entries in the CalendarQueue");
		}
		
		boolean sametime = true;
		while (sametime) {
		    if (!_eventQueue.isEmpty()) {
			try {
			    Actor actor = (Actor)_eventQueue.take();
			    double newtime = ((Double)(_eventQueue.
				    getPreviousKey())).doubleValue();
			    if (newtime == _currenttime) {
				_informOfDelayUnblock();
			    } else {
				_eventQueue.put(new Double(newtime), actor);
				sametime = false;
			    }
			} catch (IllegalAccessException e) {
			    throw new InternalErrorException(e.toString());
			}
		    } else {
			sametime = false;
		    }
		}
		//Wake up all delayed actors
		notifyAll();
	    }
	}
        return false;
    }

    /** Process the queued topology change requests. Registered topology
     *  listeners are informed of each change in a series of calls
     *  after successful completion of each request. If any queued
     *  request fails, the request is undone, and no further requests
     *  are processed. Note that change requests processed successfully
     *  prior to the failed request are <i>not</i> undone.
     *
     *  Initialize any new actors created, create receivers for them, 
     *  initialize them and create new threads for them. After all threads
     *  are created, unblock the processes blocked on a mutation 
     *  (mutation-blocked) and start the threads for the 
     *  newly created actors.
     *
     *  @exception IllegalActionException If any of the pending requests have
     *  already been implemented.
     *  @exception TopologyChangeFailedException If any of the requests fails.
     */
    protected void _processTopologyRequests()
            throws IllegalActionException, TopologyChangeFailedException {
	Workspace worksp = workspace();
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
	synchronized (this) {
	    _timedMutations = false;
	    _mutationBlockCount = 0;
	    notifyAll();
	}
	Enumeration threads = threadlist.elements();
	//Starting threads;
	while (threads.hasMoreElements()) {
	    ProcessThread pnt = (ProcessThread)threads.nextElement();
	    pnt.start();
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    protected CalendarQueue _eventQueue;
    protected double _currenttime = 0;
    
    private boolean _timedMutations = false;
    protected int _delayBlockCount = 0;
}










