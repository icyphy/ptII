/* Starts all the processes and handles deadlocks

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
Handles deadlocks and creates Processes corresponding to all PN actors

@author Mudit Goel
@version $Id$
*/
public class PNDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
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
     *  Increment the version number of the workspace.
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
     *  empty string. Increment the version number of the workspace.
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


    /** Increments the number of processes blocked on a delay.
     *  It checks for deadlocks as a consequence of an additional process being
     *  blocked. If a pause is requested, it even checks if the simulation has
     *  paused.
     */
    public synchronized void delayBlock() {
	_delayBlockCount++;
	//System.out.println("Readblocked with count "+_readBlockCount);
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
        //if (_pauseRequested) {
	//_checkForPause();
        //}
	return;
    }

    /** Decreases the number of queues and hence processes blocked on a read.
     */
    public synchronized  void delayUnblock() {
	_delayBlockCount--;
	return;
    }

    /** This handles deadlocks in PN. It is responsible for doing mutations,
     *  and increasing queue capacities in PNQueueReceivers if required.
     * @exception IllegalActionException It should not be thrown.
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

    /** Schedule to be restart execution after a specified delay
     *  with respect to the current time.
     */
    public synchronized void fireAt(Actor actor, double newfiringtime)
            throws IllegalActionException {
        _eventQueue.put(new Double(newfiringtime), actor);
        //FIXME: Blocked on a delay
        delayBlock();
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

    /** This returns false if the simulation has reached a deadlock and can
     *  be terminated if desired. This flag is set on detection of a deadlock
     *  in the fire() method.
     *  @return false if the director has detected a deadlock and does not
     *  wish to be scheduled.
     *  @exception IllegalActionException This is never thrown in PN
     */
    public boolean postfire() throws IllegalActionException {
        //System.out.println("Postifre printing " +_notdone);
	return _notdone;
    }


    /** Return a new receiver compatible with this director.
     *  In the PN domain, we use PNQueueReceivers.
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


    /** Add a mutation object to the mutation queue. These mutations
     *  are executed when the _performTopologyChanges() method is called,
     *  which in this base class is in the prefire() method.  This method
     *  also arranges that all additions of new actors are recorded.
     *  The prefire() method then invokes the initialize() method of all
     *  new actors after the mutations have been completed.
     *
     *  @param request A object with a perform() and update() method that
     *   performs a mutation and informs any listeners about it.
     */
    public void queueTopologyChangeRequest(TopologyChangeRequest request) {
	super.queueTopologyChangeRequest(request);
	synchronized(this) {
	    _urgentMutations = true;
	    notifyAll();
	}
    }

    /** Increments the number of queues and hence processes blocked on a read.
     *  It checks for deadlocks as a consequence of an additional process being
     *  blocked. If a pause is requested, it even checks if the simulation has
     *  paused.
     */
    public synchronized void readBlock() {
	_readBlockCount++;
	//System.out.println("Readblocked with count "+_readBlockCount);
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }


    /** Decreases the number of queues and hence processes blocked on a read.
     */
    public synchronized  void readUnblock() {
	_readBlockCount--;
	return;
    }


    /** Increments the number of actors blocked while writing to a receiver
     *  and checks for deadlocks. If pause has been requested, it checks
     *  if the simulation has been paused.
     *  @param queue Receiver whose size equals capacity resulting in the
     *  writing process being blocked.
     */
    public synchronized void writeBlock(PNQueueReceiver queue) {
	_writeBlockCount++;
	_writeblockedQs.insertFirst(queue);
	//System.out.println("WriteBlockedQ "+_writeBlockCount );
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
        //if (_pauseRequested) {
	//_checkForPause();
        //}
	return;
    }


    /** Decrease the number of processes blocked on a write to a receiver.
     *  If the actor can be blocked on a write, then unblock it and
     *  decrement the number of stars blocked on write.
     *  @param recep is the receptionist/queue being unblocked
     */
    public synchronized void writeUnblock(PNQueueReceiver queue) {
	_writeBlockCount--;
	_writeblockedQs.removeOneOf(queue);
	return;
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Checks for deadlock(Both artificial and true deadlocks).
    // noOfStoppedProcess is 0 if the deadlock is being checked for a
    // blocked queue. 1 if check is being done when a process stopped.
    // This is not synchronized and thus should be called from a synchronized
    // method
    protected synchronized boolean _checkForDeadlock() {
	if (_readBlockCount + _writeBlockCount + _delayBlockCount 
		>= _getActiveActorsCount()) {
	    return true;
            //System.out.println("aac ="+_actorsActive+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" db = "+ _delayBlockCount);
	} else {
	    return false;
	}
    }

    //Check if all threads are either blocked or paused
    protected synchronized boolean _checkForPause() {
	//System.out.println("aac ="+_activeActorsCount+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" *PAUSED*"+"pausedcoint = "+_actorsPaused);
	if (_readBlockCount + _writeBlockCount + _getPausedActorsCount() +
		_delayBlockCount >= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
	//return;
    }

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
	    writeUnblock(smallestCapacityQueue);
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

    //Handles deadlock, both real and artificial
    //Returns false only if it detects a mutation.
    //Returns true for termination
    //This is not synchronized and should be synchronized in the calling method
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
			delayUnblock();
                    } catch (IllegalAccessException e) {
                        throw new IllegalActionException(this, "Inconsistency"+
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
				    delayUnblock();
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
	Enumeration threads = threadlist.elements();
	//Starting threads;
	while (threads.hasMoreElements()) {
	    ProcessThread pnt = (ProcessThread)threads.nextElement();
	    pnt.start();
	    //System.out.println("Started a thread for "+((Entity)pnt.getActor()).getName());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _notdone = true;
    private int _delayBlockCount = 0;
    private int _readBlockCount = 0;
    //private boolean _terminate = false;
    private boolean _urgentMutations = false;
    private int _writeBlockCount = 0;
    private LinkedList _writeblockedQs = new LinkedList();
    private CalendarQueue _eventQueue;
    private double _currenttime = 0;
}
