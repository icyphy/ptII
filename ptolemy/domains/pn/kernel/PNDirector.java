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
import ptolemy.actor.util.*;
import ptolemy.data.*;
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
	_checkForDeadlock();
        if (_pauseRequested) {
            _checkForPause();
        }
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
        while (!_handleDeadlock());
        //System.out.println("Done firing");
    }

    /** Schedule to be restart execution after a specified delay 
     *  with respect to the current time.
     */
    public synchronized void fireAfterDelay(Actor actor, double delay) 
            throws IllegalActionException {
        double newfiringtime = getCurrentTime()+delay;
        _eventQueue.put(new Double(newfiringtime), actor);
        //_eventQueue.insert(new Double(newfiringtime));
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


    /** This method iterates through the set of actors in the compositeActor
     *  and sets the pause flag of all the receivers.
     *  @return The set of LinkedLists that are being paused.
     *  @exception IllegalActionException Might be thrown by a called method.
     */
    public LinkedList setPause() throws IllegalActionException {
        synchronized(this) {
            _pauseRequested = true;
        }
	workspace().getReadAccess();
	try {
	    // Obtaining a list of all actors in this compositeActor 
	    LinkedList pausedreceivers = new LinkedList();
	    Enumeration allStars =
		((CompositeEntity)getContainer()).deepGetEntities();
	    while (allStars.hasMoreElements()) {
		// Obtaining all the ports of each actor 
		Enumeration starPorts =
		    ((Entity)(allStars.nextElement())).getPorts();
		while (starPorts.hasMoreElements()) {
		    IOPort port = (IOPort)starPorts.nextElement();
		    if(port.isInput()) {
			Receiver[][] receivers = port.getReceivers();
			for (int i=0; i<receivers.length; i++) {
			    for (int j=0; j<receivers[i].length; j++) {
				((PNQueueReceiver)receivers[i][j]).setPause(true);
				pausedreceivers.insertFirst(receivers[i][j]);
			    }
			}
		    }
		}
	    }
            //A linked list of pausedreceivers is returned as it might not be 
            //possible to resume these receivers otherwise if they have been
            //removed from the hierarchy. They still need to be awakened so 
            //that the deadlocks can be detected and threads terminated.
	    return pausedreceivers;
	} finally {
	    workspace().doneReading();
	}
    }

    /** This resumes all the paused receivers 
     *  @param pausedreceivers This is the list of receivers whose pause flag
     *  was set to true in the setPause() method
     */
    //FIXME: Should the list of receivers be passed as an argument?
    public void setResumeRequested() {
	Enumeration receivers = _pausedRecs.elements();
	while (receivers.hasMoreElements()) {
	    PNQueueReceiver receiver = (PNQueueReceiver)receivers.nextElement();
            receiver.setPause(false);
	}
        synchronized (this) {
            _pausedRecs.clear();
            _actorsPaused= 0;
            _pauseRequested = false;
        }
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
            //FIXME: Make capacity a director parameter.
            rec.setCapacity(1);
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
        try {
            super.queueTopologyChangeRequest(request);
            _pausedRecs = setPause();
            synchronized(this) {
                _urgentMutations = true;
                notifyAll();
            } 
        } catch (IllegalActionException e) {
            System.err.println(e.toString());
        }
    }

    /** Return true indicating that the director is ready to be fired. This 
     *  starts a thread corresponding to all the actors that were created
     *  in the initialize() method.
     *  @return true always.
     *  @exception IllegalActionException This is never thrown in PN
     */
    public boolean prefire() 
            throws IllegalActionException  {
        Enumeration threads = _threadList.elements();
        // Starting threads.
        while (threads.hasMoreElements()) {
            ProcessThread pnt = (ProcessThread)threads.nextElement();
            pnt.start();        
        }
        _threadList.clear(); // Should it be cleared or given to manager?
        return true;
    }



    /** Increments the number of queues and hence processes blocked on a read.
     *  It checks for deadlocks as a consequence of an additional process being
     *  blocked. If a pause is requested, it even checks if the simulation has
     *  paused. 
     */
    public synchronized void readBlock() {
	_readBlockCount++;
	//System.out.println("Readblocked with count "+_readBlockCount);
	_checkForDeadlock();
        if (_pauseRequested) {
            _checkForPause();
        }
	return;
    }

 
    /** Decreases the number of queues and hence processes blocked on a read.
     */
    public synchronized  void readUnblock() {
	_readBlockCount--;
	return;
    }


    public void terminate() {
        //FIXME: Create this method. Should wait till the end of ActorListener
    }

    /** This terminates all the actors in the corresponding CompositeActor
     *  @exception IllegalActionException if a method accessing the topology
     *  throws it.
     */
    public void wrapup() throws IllegalActionException {
	System.out.println("Wrapup calling terminateALl");
        _terminateAll();
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
	_checkForDeadlock();
        if (_pauseRequested) {
            _checkForPause();
        }
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
    protected synchronized void _checkForDeadlock() {
	if (_readBlockCount + _writeBlockCount + _delayBlockCount >= _actorsActive) {
	    _deadlock = true;
            //System.out.println("aac ="+_actorsActive+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" db = "+ _delayBlockCount);
            
	    notifyAll();
	}
	return;
    }

    //Check if all threads are either blocked or paused
    protected synchronized void _checkForPause() {
	//System.out.println("aac ="+_activeActorsCount+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" *PAUSED*"+"pausedcoint = "+_actorsPaused);
	if (_readBlockCount + _writeBlockCount + _actorsPaused + _delayBlockCount >= _actorsActive) {
	    _paused = true;
            notifyAll();
	}
	return;
    }
    
    // Finds the QueueReceiver with the smallest write capacity 
    // that is blocked on a write and incrementes its capacity by 1.
    private void _incrementLowestWriteCapacityPort() {
        //System.out.println("Incrementing capacity");    
        PNQueueReceiver smallestCapacityQueue = null;
        int smallestCapacity = -1;
	Enumeration receps = _writeblockedQs.elements();
	//System.out.println("Enumeration receos done");
	while (receps.hasMoreElements()) {
	    PNQueueReceiver queue = (PNQueueReceiver)receps.nextElement();
	    if (smallestCapacity == -1) {
	        smallestCapacityQueue = queue;
		smallestCapacity = queue.capacity();
		//smallestCapacityRecep = flowqueue;
	    } else if (smallestCapacity > queue.capacity()) {
	        smallestCapacityQueue = queue;
	        smallestCapacity = queue.capacity();
	    }
	}
        //System.out.println("I am here");
        try {
            if (smallestCapacityQueue.capacity() <= 0) { 
                smallestCapacityQueue.setCapacity(1);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to 1");
            } else { 
	        smallestCapacityQueue.setCapacity(smallestCapacityQueue.capacity()+1);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to "+(smallestCapacityQueue.capacity()+1) );
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
        //Maintaining cache so that I can synchronize after this
	boolean urgentmut;
	boolean deadl;
	int writebl;
        int delaybl;
        Workspace worksp = workspace();
	synchronized (this) {
	    while (!_deadlock && !_urgentMutations) {
		//System.out.println("Waiting with mutations = "+_urgentMutations);
		worksp.wait(this);
	    }
	    urgentmut = _urgentMutations;
	    _urgentMutations = false;
	    deadl = _deadlock;
	    writebl = _writeBlockCount;
            delaybl = _delayBlockCount;
	}
	//System.out.println(" deadlock = "+deadl+" and mut ="+urgentmut);
	if (urgentmut) {
            synchronized (this) {
                while (!_paused) {
                    worksp.wait(this);
                }
                _paused = false;
                _deadlock = false;
            }
	    //System.out.println("Performed mutations");
            try {
                //FIXME: Should it be while or if?
                _processTopologyRequests();
                //if (_performMutations()) {
                // Initialize any new actors
                //Creates receivers and then starts up threads for all
                //System.out.println("Initializing new actors");
	 	//_pnActorListener.initializeNewActors();
                // FIXME: Should type resolution be done here?
                //}
                
                LinkedList threadlist = new LinkedList();
                //FIXME: Maybe we require a cleaner method for this
                //FIXME: Where does the type resolution go?
                Enumeration newactors = _newActors();
                while (newactors.hasMoreElements()) {
                    increaseActiveCount();
                    Actor actor = (Actor)newactors.nextElement();
                    actor.createReceivers();
                    actor.initialize();
                    ProcessThread pnt = new ProcessThread(actor, this);
                    threadlist.insertFirst(pnt);
                }
                //Resume the paused actors
                setResumeRequested();
                Enumeration threads = threadlist.elements();
                //Starting threads;
                while (threads.hasMoreElements()) {
                    ProcessThread pnt = (ProcessThread)threads.nextElement();
                    pnt.start();
                    //System.out.println("Started a thread for "+((Entity)pnt.getActor()).getName());
                }
            } catch (TopologyChangeFailedException e) {
                throw new IllegalActionException("Name duplication error: " +
                        e.getMessage());
            }
	    return false;
	} 
        if (writebl==0 && deadl) {
            //Check if there are any events in the future.
            //FIXME: Is this the right way?
            if (delaybl ==0) {
                System.out.println("real deadlock. Everyone would be erased");
                _notdone = false;
                return true;
            } else {
                //Advance time to next possible time.
                synchronized(this) {
                    _deadlock = false;
                    try {
                        _eventQueue.take();
                        _currenttime = ((Double)(_eventQueue.getPreviousKey())).doubleValue();
                    } catch (IllegalAccessException e) {
                        throw new IllegalActionException(this, "Inconsistency"+
                                " in number of actors blocked on delays count"+
                                " and the entries in the CalendarQueue");
                    }
                    
                    delayUnblock();
                    boolean sametime = true;
                    while (sametime) {
                        if (!_eventQueue.isEmpty()) {
                            try {
                                Actor actor = (Actor)_eventQueue.take();
                                
                                double newtime = ((Double)(_eventQueue.getPreviousKey())).doubleValue();
                                if (newtime != _currenttime) {
                                    _eventQueue.put(new Double(newtime), actor);
                                    sametime = false;
                                } else {
                                    delayUnblock();
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
            _deadlock = false;
            // find the input port with lowest capacity queue;
            // that is blocked on a write and increment its capacity;
            _incrementLowestWriteCapacityPort();
            //System.out.println("Incrementing capacity done");
        }
        return false;
    }
        
    // Terminates all stars and hence the simulation 
    //FIXME: This does not handle compositeActors within toplevel CompositeActor
    private void _terminateAll() throws IllegalActionException {
	    // Obtaining all stars in the current galaxy 
	    Enumeration allStars =
		((CompositeEntity)getContainer()).deepGetEntities();
	    while (allStars.hasMoreElements()) {
		// Obtaining all the ports of each star 
		Enumeration starPorts =
		    ((Entity)(allStars.nextElement())).getPorts();
		while (starPorts.hasMoreElements()) {
		    IOPort port = (IOPort)starPorts.nextElement();
		    // Terminating the ports and hence the star
		    if(port.isInput()) {
			//FIXME: Get all receivers
			Receiver[][] receivers = port.getReceivers();
			for (int i=0; i<receivers.length; i++) {
			    for (int j=0; j<receivers[i].length; j++) {
				((PNQueueReceiver)receivers[i][j]).setFinish();
			    }
			}
		    }
		}
	    }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    //private long _pausedcount = 0;

    //private LinkedList _threadlist = new LinkedList();
    //private boolean _pause = false;
    private boolean _paused = false;
    private boolean _mutate = true;
    private boolean _deadlock = false;
    private boolean _notdone = true;
    private int _delayBlockCount = 0;
    private int _readBlockCount = 0;    
    private boolean _terminate = false;
    private boolean _urgentMutations = false;
    private int _writeBlockCount = 0;
    private LinkedList _pausedRecs = new LinkedList();
    private LinkedList _writeblockedQs = new LinkedList();
    private CalendarQueue _eventQueue; 
    private double _currenttime = 0;
}


















































































