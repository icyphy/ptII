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
import ptolemy.kernel.mutation.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
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
public class PNDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public PNDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public PNDirector(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public PNDirector(Workspace workspace, String name) {
        super(workspace, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This decreases the number of active threads in the compositeActor by 1.
     *  This method should be called only when an active thread that was 
     *  registered using increaseActiveCount() is terminated
     */
    public synchronized  void decreaseActiveCount() {
	_activeActorsCount--;	    
	_checkForDeadlock();
	//System.out.println("decreased active count");
        _checkForPause();
    }
    
    /** This does not do anything in PN.
     * @exception IllegalActionException It should not be thrown.
     */
    public void fire()
	    throws IllegalActionException {
        while (!_handleDeadlock());
        //System.out.println("Done firing");
    }

    /** This invokes the initialize() methods of all its deeply contained actors.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void initialize()
            throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            //Creating receivers and threads for all actors;
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement(); 
                increaseActiveCount();
                actor.createReceivers();
                actor.initialize();
                PNThread pnt = new PNThread(actor, this);
                _threadlist.insertFirst(pnt);
            }
        }
    }

    /** This method should be called when a new thread corresponding to an actor
     *  is started in a simulation. This method is required for detection of
     *  deadlocks. The corresponding method decreaseActiveCount should be called 
     *  when the thread is terminated.
     */
    public synchronized void increaseActiveCount() {
	_activeActorsCount++;
    }

    /** This method increases the number of paused threads and checks if the 
     *  entire simulation is paused. 
     */
    public void paused() {
        _pausedcount++;
        _checkForPause();
    }

    /** This method iterates through the set of actors in the compositeActor and 
     *  sets the pause flag of all the receivers.
     *  @return The set of LinkedLists that are being paused.
     *  @exception IllegalActionException Might be thrown by a called method.
     */
    public LinkedList setPause() throws IllegalActionException {
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
	    return pausedreceivers;
	} finally {
	    workspace().doneReading();
	}
    }

    /** This resumes all the receivers in the list being passed as an argument
     *  @param pausedreceivers This is the list of receivers whose pause flag
     *  was set to true in the setPause() method
     */
    public void setResume() {
	Enumeration receivers = _pausedRecs.elements();
	while (receivers.hasMoreElements()) {
	    PNQueueReceiver receiver = (PNQueueReceiver)receivers.nextElement();
            receiver.setPause(false);
	}
        _pausedRecs.clear();
        _pausedcount= 0;
    }

    //FIXME: Should it always return true?
    /** Does nothing for PN */
    public boolean postfire() throws IllegalActionException {
        //_terminateAll();
        //System.out.println("Postifre printing " +_notdone);
	return _notdone;
    }

    //FIXME: Should it always return true?
    public boolean prefire() 
	    throws IllegalActionException  {
        Enumeration threads = _threadlist.elements();
        //Starting threads;
        while (threads.hasMoreElements()) {
            PNThread pnt = (PNThread)threads.nextElement();
            //increaseActiveCount();
            pnt.start();
        }
        _threadlist.clear();
        return true;
    }

    /** Return a new receiver compatible with this director.
     *  In the PN domain, we use PNQueueReceivers.
     *  @return A new PNReceiver.
     */
    public Receiver newReceiver() {
        PNQueueReceiver rec =  new PNQueueReceiver();
        try {
            //rec.setCapacity(1);
        } catch (Exception e) {
            //This exception should never be thrown, as size of queue should 
            //be 0, and capacity should be set to a non-negative number
            throw new InternalErrorException(e.toString());
        }
        return rec;
    }

    /** Add a mutation object to the mutation queue. These mutations
     *  are executed when the _performMutations() method is called,
     *  which in this base class is in the prefire() method.  This method
     *  also arranges that all additions of new actors are recorded.
     *  The prefire() method then invokes the initialize() method of all
     *  new actors after the mutations have been completed.
     *
     *  @param mutation A object with a perform() and update() method that
     *   performs a mutation and informs any listeners about it.
     */
    public void queueMutation(Mutation mutation) {
	try {
	    // The private member is created only if mutation is being used.
	    super.queueMutation(mutation);
	    _pausedRecs = setPause();
	    synchronized(this) {
		_urgentMutations = true;
		notifyAll();
	    } 
	} catch (IllegalActionException e) {
	    System.err.println(e.toString());
	}
    }

    /** Increments the no of queues blocked on read. Also checks for deadlocks 
     * @param recep is the receptionist/queue that is blocking on a read
     */
    public synchronized void readBlock() {
	_readBlockCount++;
	//System.out.println("Readblocked with count "+_readBlockCount);
	_checkForDeadlock();
        _checkForPause();
	return;
    }

 
    /** Decreases the number of queues blocked on a read
     * @param recep is the receptionist/queue being unblocked on a read
     */
    public synchronized  void readUnblock() {
	_readBlockCount--;
	return;
    }

    /** This terminates all the actors in the corresponding CompositeActor
     */
    public void wrapup() throws IllegalActionException {
	System.out.println("Wrapup calling terminateALl");
        _terminateAll();
        return;
    } 

    /** Increments the capacity, if it can be incremented. Else increments the
     *  number of stars blocked while writing and checks for deadlocks.
     * @param latest input port that blocked the corresponding output star
     *  on a write.
     */
    public synchronized void writeBlock(PNQueueReceiver queue) {
	_writeBlockCount++;
	_writeblockedQs.insertFirst(queue);
	//System.out.println("WriteBlockedQ "+_writeBlockCount );
	_checkForDeadlock();
        _checkForPause();
	return;
    } 
    

    /** If the stars can be blocked on a write, then unblock it and 
     *  decrement the number of stars blocked on write.
     * @param recep is the receptionist/queue being unblocked
     */
    public synchronized void writeUnblock(PNQueueReceiver queue) {
	_writeBlockCount--;
	_writeblockedQs.removeOneOf(queue);
	return;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Perform all pending mutations and inform all registered listeners
     *  of the mutations.  Return true if any mutations were performed,
     *  and false otherwise.
     *
     *  @exception IllegalActionException If the mutation throws it.
     *  @exception NameDuplicationException If the mutation throws it.
     */
    protected boolean _performMutations() 
	    throws IllegalActionException, NameDuplicationException {
        if (_pnActorListener==null){
	    _pnActorListener = new PNActorListener(this);
	    addMutationListener(_pnActorListener);
        }
	//System.out.println("Calling super.mutations");
	boolean mut = super._performMutations();
	return mut;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Checks for deadlock(Both artificial and true deadlocks). 
    // noOfStoppedProcess is 0 if the deadlock is being checked for a 
    // blocked queue. 1 if check is being done when a process stopped.
    //This is not synchronized and thus should be called from a synchronized
    //method
    private synchronized void _checkForDeadlock() {
	if (_readBlockCount + _writeBlockCount >= _activeActorsCount) {
	    _deadlock = true;
            //System.out.println("aac ="+_activeActorsCount+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" **************************");
            
	    //FIXME: Who should the notify go on?
	    notifyAll();
	}
	return;
    }

    //Check if all threads are either blocked or paused
    private synchronized void _checkForPause() {
	//System.out.println("aac ="+_activeActorsCount+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" *PAUSED*"+"pausedcoint = "+_pausedcount);
	if (_readBlockCount + _writeBlockCount + _pausedcount >= _activeActorsCount) {
	    _paused = true;
            //_pausedcount = 0;
            notifyAll();
	}
	return;
    }
    
    // Finds the QueueReceiver with the smallest write capacity 
    // that is blocked on a write.
    private void _incrementLowestWriteCapacityPort() {
        //System.out.println("Incrementing capacity");    
        //FIXME: Should this be synchronized
        PNQueueReceiver smallestCapacityQueue = null;
        int smallestCapacity = -1;
	Enumeration receps = _writeblockedQs.elements();
	System.out.println("Enumeration receos done");
	while (receps.hasMoreElements()) {
	    PNQueueReceiver queue = (PNQueueReceiver)receps.nextElement();
	    if (smallestCapacity == -1) {
	        smallestCapacityQueue = queue;
		smallestCapacity = queue.capacity();
		//smallestCapacityRecep = flowqueue;
	    } else if (smallestCapacity > queue.capacity()) {
	        smallestCapacityQueue = queue;
	        smallestCapacity = queue.capacity();
		//smallestCapacityRecep = flowqueue;
	    }
	}
        System.out.println("I am here");
        try {
            if (smallestCapacityQueue.capacity() <= 0) { 
                smallestCapacityQueue.setCapacity(1);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to 1");
            } else { 
	        smallestCapacityQueue.setCapacity(smallestCapacityQueue.capacity()+1);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to "+(smallestCapacityQueue.capacity()+1) );
            }
	    //_readblockedQs.remove((PNPort)smallestCapacityRecep.getContainer());
	    //FIXME: Wont this alwas be true?? Check it out
	    //if ((PNInPort)(smallestCapacityRecep.getContainer()).isWritePending()) {
	    writeUnblock(smallestCapacityQueue);
	    smallestCapacityQueue.setWritePending(false);
            synchronized(smallestCapacityQueue) {
                System.out.println("Notifying ........ All");
                smallestCapacityQueue.notifyAll();
            }
        } catch (IllegalActionException e) {
	    System.err.println("Exception: " + e.toString());
	    //Should not be thrown as this exception is thrown 
            //only if port is not an input port, checked above
        }
        System.out.println("returning");
        return;
    }

    // Handles deadlock, both real and artificial
    //Returns false only if it detects a mutation.
    //Returns true for termination
    //This is not synchronized and should be synchronized in the calling method
    private boolean _handleDeadlock() 
	    throws IllegalActionException {
        // This exception should never be thrown!!
	// Process deadlocks
	// Wait for a deadlock to occur.
	//while (!hasNewActors() &&!_mutate && !_terminate && !_deadlock) {
	boolean urgentmut;
	boolean deadl;
	int writebl;
        Workspace worksp = workspace();
	synchronized (this) {
	    while (!_deadlock && !_urgentMutations) {
		//System.out.println("Waiting with mutations = "+_urgentMutations);
		worksp.wait(this);
		//System.out.println("Woken up hmm");
	    }
	    urgentmut = _urgentMutations;
	    _urgentMutations = false;
	    deadl = _deadlock;
	    writebl = _writeBlockCount;
	}
	//}
	//System.out.println(" deadlock = "+deadl+" and mut ="+urgentmut);
	if (urgentmut) {
	    //FIXME: Should get a linked list anyway, for what about; 
	    //paused receivers of deleted actors?;
            //FIXME: I should perhaps check that all of them are paused!!;
            //LinkedList pausedrecs = setPause(true);
            synchronized (this) {
                while (!_paused) {
                    worksp.wait(this);
                }
                _paused = false;
                _deadlock = false;
            }
            //isPaused();
	    //System.out.println("Performed mutations");
	    //boolean mutationOccured = false;
	    //while (_performMutations()) {
            try {
                if (_performMutations()) {
                    //mutationOccured = true;
                    // NOTE: Should type resolution be done here?
                    // Initialize any new actors
                    //Creates receivers and then starts up threads for all
                        //System.out.println("Initializing new actors");
	 	    _pnActorListener.initializeNewActors();
                    //_pausedcount = 0;
                    //setResume(_pausedRecs);
	        }
            } catch (NameDuplicationException nde) {
                // FIXME: this is just to get this thing to compile
                throw new IllegalActionException("Name duplication error: " +
                        nde.getMessage());
            }
	    //System.out.println("Done mutations");
	    //setResume(_pausedRecs);
            
	    //}
	    //_urgentMutations = false;
	    return false;
	}
        if (writebl==0 && deadl) {
            //_terminate = true;
            //FIXME: Terminate only if toplevel container
                                                    
            _terminateAll();
            System.out.println("real deadlock. Everyone would be erased");
            _notdone = false;
            return true;
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
    private void _terminateAll() throws IllegalActionException {
	try {
	    //workspace().getReadAccess(); 
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
				((PNQueueReceiver)receivers[i][j]).setTerminate();
				//receivers[i][j].notifyAll();
			    }
			}
			//port.setTerminate();
		    }
		}
	    }
	    //}
	} finally {
	    //workspace().doneReading();
	    //catch (IllegalActionException e) {
	    //System.out.print("IlleagalActionException in PNDirector");
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private long _activeActorsCount = 0;
    private long _pausedcount = 0;

    private LinkedList _threadlist = new LinkedList();
    private boolean _pause = false;
    private boolean _paused = false;
    private boolean _mutate = true;
    // Container is the CompositeEntity the executive is responsible for
    //private CompositeEntity _container;
    // Is set when a deadlock occurs
    private boolean _deadlock = false;
    private boolean _notdone = true;
    // Level of debugging output
    //private int _debug = 0;
    // The threadgroup in which all the stars are created.
    //private ThreadGroup _processGroup;
    // Number of stars blocking on read.
    private int _readBlockCount = 0;    
    // Is set when the simulation is to be terminated
    private boolean _terminate = false;
    private boolean _urgentMutations = false;
    // No of stars blocking on write.
    private int _writeBlockCount = 0;
    private LinkedList _pausedRecs = new LinkedList();
    private LinkedList _writeblockedQs = new LinkedList();
    private PNActorListener _pnActorListener;
}


















































































