/* Governs the execution of a CompositeActor with extended Kahn process 
network semantics.

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
A PNDirector governs the execution of a CompositeActor with
Kahn process networks (PN) semantics. This model of computation has been 
extended further by introducing a notion of global time. This also supports
mutations of graphs in a non-deterministic way. To make a composite actor 
obey these semantics, call the method setDirector() with an instance of 
this class.
<p>
In the PN domain, the director creates a thread (an instance of 
ProcessThread), representing a Kahn process, for each actor in the model. 
The threads are created in initialize() and started in the prefire() method 
of the ProcessDirector. A process is considered <i>active</i> from its 
creation until its termination. An active process can either <i>block</i> 
or <i>delay</i>. A process can block when trying to read from a channel
(read-blocked) or when trying to write to a channel (write-blocked). A 
delayed process is a process waiting for time to advance.
<p>
This director is responsible for handling legal deadlocks during execution.
Legal deadlocks are of three sorts, a real deadlock, an artificial deadlock,
and a timed deadlock. 
<p>
A real deadlock is when all the processes are blocked on a read and no 
process can proceed until it receives new data. The execution can be 
terminated, if desired, in such a situation. It is decided by the executive 
director of the container (a composite actor). If the container is the 
top-level composite actor, then the manager calls wrapup() and terminates 
the execution.
<p>
A timed deadlock is when all the processes under the control of this 
director are blocked on a read or on a delay. In such a case, the director
advances time to the time when the first delay-blocked process can be woken 
up.
<p>
An artificial deadlock is when all processes are blocked on a read, on a 
delay, or a write (atleast one process is blocked on a write). In this 
case the director chooses a receiver with the smallest capacity amongst all 
the receivers on which a process is blocked on a write and increases its 
capacity. This breaks the artificial deadlock and the execution can proceed.
<p>
This director is capable of handling mutations of graphs. These mutations can
be non-deterministic. In PN, since the execution of a model is not centralized,
it is impossible to define a useful fixed point in the execution of all the 
active processes where mutations can occur. Due to this, PN permits mutations
to happen as soon as they occur. Thus as soon as a process queues mutations
in PN, the director is notified and the director pauses the simulation. Then 
it performs all the mutations requested, and notifies the topology listeners.
After this the execution is resumed.
<p>
In case of PN, a process can be paused only when it tries to communicate with
other processes. A pause in PN is defined as a state when all processes are 
blocked on a read, a write, delayed or are explicitly paused in the get() or
put() method of the receiver. Thus if a process does not communicate with 
other processes in the model, then the simulation can never pause in that 
model.
<p>


@author Mudit Goel
@version $Id$
*/
public class PNDirector extends ptolemy.actor.process.ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace. A 
     *  priority queue is created to keep a track of delayed actors. The 
     *  default capacity of the queues in all the receivers set to 1.
     */
    public PNDirector() {
        super();
        _eventQueue = new CalendarQueue(new DoubleCQComparator());
        try {
            Parameter param = new Parameter(this,"Initial queue capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            throw new InvalidStateException(e.toString());
        }
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace. A 
     *  priority queue is created to keep a track of delayed actors. The 
     *  default capacity of the queues in all the receivers set to 1.
     *  @param name Name of this object.
     */
    public PNDirector(String name) {
        super(name);
        _eventQueue = new CalendarQueue(new DoubleCQComparator());
        try {
            Parameter param = new Parameter(this,"Initial queue capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            throw new InvalidStateException(e.toString());
        }
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace. A 
     *  priority queue is created to keep a track of delayed actors. The 
     *  default capacity of the queues in all the receivers set to 1.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public PNDirector(Workspace workspace, String name) {
        super(workspace, name);
        _eventQueue = new CalendarQueue(new DoubleCQComparator());
        try {
            Parameter param = new Parameter(this,"Initial queue capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            throw new InvalidStateException(e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Handles various deadlocks appropriately and performs mutations 
     *  when requested. It suspends the thread corresponding to the 
     *  director until it is notified of either a request for mutation 
     *  or an occurence of a deadlock. On notification, it calls the 
     *  appropriate methods and returns. In case it is responding to a
     *  detection of a deadlock and the deadlock detected is a real 
     *  deadlock, it sets a flag forcing the postfire() method to return
     *  false whenever it is called next. 
     *  
     *  @exception IllegalActionException If any of the called methods throw
     *  it. 
     *  @see _processTopologyRequests
     *  @see _handleDeadlock
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
	    _urgentMutations = false;
	}
	//System.out.println(" deadlock = "+deadl+" and mut ="+urgentmut);
	if (urgentmut) {
	    //System.out.println("Performed mutations");
            try {
                _processTopologyRequests();
                // FIXME: Should type resolution be done here?
            } catch (TopologyChangeFailedException e) {
                throw new IllegalActionException("Name duplication error: " +
                        e.getMessage());
            }
	    return;
	} else {
	    _notdone = !_handleDeadlock();
	}
        //System.out.println("Done firing");
    }

    /** Schedule a resumption of the given actor at the given time. It 
     *  suspends the calling thread until the time has sufficiently progressed
     *  to atleast the given time. It increments the count of the actors 
     *  blocked on a delay. It also queues the actor in a priority 
     *  queue with the given time as the index to keep a count of actors 
     *  blocked at the current time.
     *  @exception IllegalActionException If the operation is not
     *  permissible (e.g. the given time is in the past).
     */
    public synchronized void fireAt(Actor actor, double newfiringtime)
            throws IllegalActionException {
        _eventQueue.put(new Double(newfiringtime), actor);
        //FIXME: Blocked on a delay
        _delayBlock();
        try {
            while (getCurrentTime() < newfiringtime) {
                wait(); //Should I call workspace().wait(this) ?
            }
        } catch (InterruptedException e) {}
        //System.out.println("Currenttime is "+_currenttime);
        //delayUnblock();
        //FIXME: Unblocked on a delay
    }

    /** Return the current time of the simulation.
     *  @return the current time of the simulation
     */
    public synchronized double getCurrentTime() {
        return _currenttime;
    }

    /** Set the current time.
     *  @exception IllegalActionException If time cannot be changed
     *   due to the state of the simulation.
     *  @param newTime The new current simulation time.
     */
    // FIXME: complete this.
    public synchronized void setCurrentTime(double newTime)
            throws IllegalActionException {
        _currenttime = newTime;
    }

    /** Return false if the simulation has reached a real deadlock and can
     *  be terminated if desired. This flag is set on detection of a deadlock
     *  in the fire() method.
     *  @return false if the director has detected a deadlock and does not
     *  wish to be scheduled.
     *  @exception IllegalActionException Never thrown in PN
     */
    public boolean postfire() throws IllegalActionException {
        //System.out.println("Postifre printing " +_notdone);
	return _notdone;
    }


    /** Return a new receiver compatible with this director.
     *  PNQueueReceivers are used in the PN domain. Set the initial capacity
     *  of the FIFO queue in the receiver.
     *  @return A new PNReceiver.
     */
    public Receiver newReceiver() {
        PNQueueReceiver rec =  new PNQueueReceiver();
        try {
            Parameter par = (Parameter)getAttribute("Initial queue capacity");
            int cap = ((IntToken)par.getToken()).intValue();
            rec.setCapacity(cap);
        } catch (IllegalActionException e) {
            //This exception should never be thrown, as size of queue should
            //be 0, and capacity should be set to a non-negative number
            throw new InternalErrorException(e.toString());
        }
        return rec;
    }


    /** Add a topology change request to the request queue. These changes 
     *  are executed when the _performTopologyChanges() method is called.
     *  After queueing the requests, it notifies the thread responsible
     *  for the director of pending topology changes. This method
     *  also arranges that all additions of new actors are recorded.
     *
     *  @param request An object with commands to perform topology changes
     *  and to inform the topology listeners of the same.
     *  @see ptolemy.kernel.event.TopologyChangeRequest
     *  @see ptolemy.kernel.event.TopologyListener
     */
    public void queueTopologyChangeRequest(TopologyChangeRequest request) {
	super.queueTopologyChangeRequest(request);
	synchronized(this) {
	    _urgentMutations = true;
	    notifyAll();
	}
    }



    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if a deadlock(All real, artificial and delayed 
     *  deadlocks) is detected.
     *  @return true if a deadlock is detected.
     */
    protected synchronized boolean _checkForDeadlock() {
	if (_readBlockCount + _writeBlockCount + _delayBlockCount 
		>= _getActiveActorsCount()) {
	    return true;
            //System.out.println("aac ="+_actorsActive+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" db = "+ _delayBlockCount);
	} else {
	    return false;
	}
    }

    /** Return true if the execution has paused. 
     *  @return true if a deadlock is detected.
     */
    protected synchronized boolean _checkForPause() {
	//System.out.println("aac ="+_activeActorsCount+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" *PAUSED*"+"pausedcoint = "+_actorsPaused);
	if (_readBlockCount + _writeBlockCount + _getPausedActorsCount() +
		_delayBlockCount >= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Increments the number of processes blocked on a delay.
     *  If a deadlock occurs or if the execution is paused as a consequence, 
     *  the director is notified of the same.
     */
    synchronized void _delayBlock() {
	_delayBlockCount++;
	//System.out.println("Readblocked with count "+_readBlockCount);
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }

    /** Decreases the number of processes blocked on a delay.
     */
    synchronized protected void _delayUnblock() {
	_delayBlockCount--;
	return;
    }

    /** Handle real, artificial or timed deadlocks. 
     *  If a real deadlock is detected, then return true. 
     *  If a timed deadlock is detected, then advance time to the earliest
     *  time that a delayed process is waiting for, wake up all the actors
     *  waiting for time to advance to the new time, and remove them from 
     *  the priority queue. 
     *  If an artificial deadlock is detected, then increase the capacity
     *  of the receiver with the smallest capacity on which a process is 
     *  blocked.
     *  
     *  @return true if a real deadlock is detected.
     *  @exception IllegalActionException Not thrown in PN.
     */
    protected boolean _handleDeadlock()
	    throws IllegalActionException {
        if (_writeBlockCount==0) {
            //Check if there are any events in the future.
            if (_delayBlockCount ==0) {
                System.out.println("real deadlock. Everyone would be erased");
                return true;
            } else {
                //Advance time to next possible time.
                synchronized(this) {
                    try {
                        _eventQueue.take();
                        _currenttime = ((Double)(_eventQueue.getPreviousKey())).doubleValue();
			_delayUnblock();
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

                                double newtime = ((Double)(_eventQueue.getPreviousKey())).doubleValue();
				if (newtime == _currenttime) {
				    _delayUnblock();
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
        } else {
            //its an artificial deadlock;
            System.out.println("Artificial deadlock");
            //_deadlock = false;
            // find the input port with lowest capacity queue;
            // that is blocked on a write and increment its capacity;
            _incrementLowestWriteCapacityPort();
            //System.out.println("Incrementing capacity done");
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
	    ptolemy.actor.process.ProcessThread pnt = new ptolemy.actor.process.ProcessThread(actor, this);
	    threadlist.insertFirst(pnt);
	    _addNewThread(pnt);
	}
	//Resume the paused actors
	resume();
	Enumeration threads = threadlist.elements();
	//Starting threads;
	while (threads.hasMoreElements()) {
	    ptolemy.actor.process.ProcessThread pnt = (ptolemy.actor.process.ProcessThread)threads.nextElement();
	    pnt.start();
	    //System.out.println("Started a thread for "+((Entity)pnt.getActor()).getName());
	}
    }


    /** Increment the count of processes blocked on a read.
     *  It checks for deadlocks or pausing of the simulation, as a 
     *  consequence of an additional process being blocked. If either of the
     *  two is detected, it notifies the director of the same.
     */
    synchronized void _readBlock() {
	_readBlockCount++;
	//System.out.println("Readblocked with count "+_readBlockCount);
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }


    /** Decrease the count of processes blocked on a read.
     */
    synchronized  void _readUnblock() {
	_readBlockCount--;
	return;
    }


    /** Increment the count of actors blocked while writing to a receiver.
     *  Check for a deadlock or a pausing of the simulation. If either of 
     *  them is detected, then notify the director.
     *  @param queue Receiver whose size equals capacity resulting in the
     *  writing process being blocked.
     */
    synchronized void _writeBlock(PNQueueReceiver queue) {
	_writeBlockCount++;
	_writeblockedQs.insertFirst(queue);
	//System.out.println("WriteBlockedQ "+_writeBlockCount );
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }


    /** Decrease the count of processes blocked on a write to a receiver.
     *  @param queue is the receiver on which the process was blocked.
     */
    synchronized void _writeUnblock(PNQueueReceiver queue) {
	_writeBlockCount--;
	_writeblockedQs.removeOneOf(queue);
	return;
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////



    // Finds the QueueReceiver with the smallest write capacity
    // that is blocked on a write and incrementes its capacity by 1.
    private void _incrementLowestWriteCapacityPort() {
        //System.out.println("Incrementing capacity");
        PNQueueReceiver smallestCapacityQueue = null;
        int smallestCapacity = -1;
        //FIXME: Should I traverse the topology and get receivers blocked on 
        // a write or should I stick with this strategy?
	Enumeration receps = _writeblockedQs.elements();
	//System.out.println("Enumeration receos done");
	while (receps.hasMoreElements()) {
	    PNQueueReceiver queue = (PNQueueReceiver)receps.nextElement();
	    if (smallestCapacity == -1) {
	        smallestCapacityQueue = queue;
		smallestCapacity = queue.getCapacity();
		//smallestCapacityRecep = flowqueue;
	    } else if (smallestCapacity > queue.getCapacity()) {
	        smallestCapacityQueue = queue;
	        smallestCapacity = queue.getCapacity();
	    }
	}
        //System.out.println("I am here");
        try {
            if (smallestCapacityQueue.getCapacity() <= 0) {
                smallestCapacityQueue.setCapacity(1);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to 1");
            } else {
	        smallestCapacityQueue.setCapacity(smallestCapacityQueue.getCapacity()+1);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to "+(smallestCapacityQueue.getCapacity()+1) );
            }
	    _writeUnblock(smallestCapacityQueue);
	    smallestCapacityQueue.setWritePending(false);
            synchronized(smallestCapacityQueue) {
                System.out.println("Notifying ........ All");
                smallestCapacityQueue.notifyAll();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
	    //Should not be thrown as this exception is thrown
            //only if port is not an input port, checked above
        }
        //System.out.println("returning");
        return;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    protected boolean _notdone = true;
    protected int _delayBlockCount = 0;
    protected int _readBlockCount = 0;
    //private boolean _terminate = false;
    protected boolean _urgentMutations = false;
    protected int _writeBlockCount = 0;
    protected LinkedList _writeblockedQs = new LinkedList();
    protected CalendarQueue _eventQueue;
    protected double _currenttime = 0;
}


