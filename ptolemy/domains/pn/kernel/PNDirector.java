/* Starts all the processes and handles deadlocks

 Copyright (c) 1998 The Regents of the University of California.
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
Handles deadlocks and creates Processes corresponding to all PN stars

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

//     public boolean canContinue() {
// 	return !(_terminate || _mutate || _pause); 
//     }

    public synchronized  void decreaseActiveCount() {
	_activeActorsCount--;	    
	//System.out.println("decreasing active count");
	_checkForDeadlock();
    }
    
    /** This does not do anything in PN.
     * @exception IllegalActionException It should not be thrown.
     */
    public void fire()
	    throws IllegalActionException {
    }

    //FIXME: How do you let the number of iterations to the sub-galaxies
    /** This invokes initialize()
     *  @param iterations This argument is ignored in PN.
     *  @exception IllegalActionException If thrown by initialize()
     *  @exception NameDuplicationException This should not be thrown in PN
     */
    public void go(int iterations)
            throws IllegalActionException, NameDuplicationException {
        CompositeActor container = ((CompositeActor)getContainer());
	if (container != null) {
	    initialize();
	    //I cannot call initialize here as the simulations are sync on
	    //iterate and NOT on go. SO in subsytems, as I would only be 
	    //calling iterate, I would not be able to call handledeadlock
	    //FIXME: Should it wait? Because now it returns immediately 
	    //after creating and starting threads
	    //while (!_handleeadlock());
	    //I donot call handledeadlock as no check if it is an executive
	    //director
	}
    }        
    

    /** If this is the local director of its container, invoke the initialize()
     *  methods of all its deeply contained actors.  If this is the executive
     *  director of its container, then invoke the initialize() method of the
     *  container.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception CloneNotSupportedException If the initialize() method of the
     *   container or one of the deeply contained actors throws it.
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
                actor.createReceivers();
                PNThread pnt = new PNThread(actor);
                _threadlist.insertFirst(pnt);
                //increaseActiveCount();
            }
            Enumeration threads = _threadlist.elements();
            //Starting threads;
            while (threads.hasMoreElements()) {
                PNThread pnt = (PNThread)threads.nextElement();
                increaseActiveCount();
                pnt.start();
            }
        }
        while (!_handleDeadlock());
    }

    public synchronized void increaseActiveCount() {
	_activeActorsCount++;
    }

    public LinkedList setPause(boolean pause) throws IllegalActionException {
	workspace().getReadAccess();
	try {
	    // Obtaining all stars in the current galaxy 
	    LinkedList pausedreceivers = new LinkedList();
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
				((PNQueueReceiver)receivers[i][j]).setPause(pause);
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

    public void setResume(LinkedList pausedreceivers) {
	Enumeration receivers = pausedreceivers.elements();
	while (receivers.hasMoreElements()) {
	    PNQueueReceiver receiver = (PNQueueReceiver)receivers.nextElement();
	    receiver.setPause(false);
	}
    }

    //FIXME: Should be called only if director is Executive
    //Should already have read access to the workspace
//     public boolean iterate()
// 	    throws IllegalActionException, NameDuplicationException {
// 	if (isExecutiveDirector()) {
// 	    CompositeActor container = ((CompositeActor)getContainer());
// 	    if (container.prefire()) {
// 		container.fire();
// 		return container.postfire();
// 	    }
// 	    return false;
// 	} else {
// 	    throw new IllegalActionException(this, "iterate() can be called" +
// 		    " on the PNDirector only if the director is an executive" +
// 		    " director.");
// 	}
//     } 

    //FIXME: Should it always return true?
    /** Does nothing for PN */
    public boolean postfire() throws IllegalActionException {
        //_terminateAll();
	//FIXME: Return something
	return true;
    }

    //FIXME: Should it always return true?
    public boolean prefire() 
	    throws IllegalActionException  {
        return true;
    }

    /** Return a new receiver compatible with this director.
     *  In the PN domain, we use PNQueueReceivers.
     *  @return A new PNReceiver.
     */
    public Receiver newReceiver() {
        return new PNQueueReceiver();
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
	    setPause(true);
	    synchronized(this) {
		_urgentMutations = true;
		//setPause(true);
		notifyAll();
	    } 
	    //setPause(true);
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
	return;
    }

 
    /** Decreases the number of queues blocked on a read
     * @param recep is the receptionist/queue being unblocked on a read
     */
    public synchronized  void readUnblock() {
	_readBlockCount--;
	return;
    }

    /** The action to terminate all actors under control of this local 
     *  director because a real deadlock has occured or the UI has ordered
     *  the simulation to be terminated prematurely.
     *  <p>
     */
    //public void terminate() {
        //System.out.println("about to terminate simulation");
	// This is the local director.
    //try {
    //    workspace().getReadAccess();
	    //This should normally not happen
    //    if (_terminate) {
		// simulation has already been terminated!
    //	return;
    //    }
    //    _terminate = true;
    //    CompositeActor cont = (CompositeActor)getContainer();
    //    Enumeration threads = _threadlist.elements();
	    //FIXME: Probably should get receivers and terminate them
    //    while (threads.hasMoreElements()) {
    //	PNThread pnt = (PNThread)threads.nextElement();
    //	pnt.terminate();
    //    }
	    //  Enumeration allMyActors = cont.deepGetEntities(); 
	    // 	    while (allMyActors.hasMoreElements()) {
	    // ComponentEntity entity =
	    //   (ComponentEntity)allMyActors.nextElement();
	    //try {
	    //  if (entity.isAtomic()) {
	    // 			((Actor)entity).terminate();
	    // 		    } else {
	    // 			((CompositeActor)entity).terminate();
	    // 		    }
	    //removeEntity(entity);
	    //} catch (IllegalActionException ex) {
	    //throw new InvalidStateException(this, entity,
	    //"Inconsistent container relationship!");
	    //}
	    //}    
    //	} finally {
    //    workspace().doneReading();
    //	}
    //}
    
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
	//synchronized(this) {
	// _urgentMutations = false;
	//}
	//System.out.println("Done mutations");
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
	//System.out.println("aac ="+_activeActorsCount+" wb ="+_writeBlockCount+" rb = "+_readBlockCount+" **************************");
	if (_readBlockCount + _writeBlockCount >= _activeActorsCount) {
	    _deadlock = true;
	    //FIXME: Who should the notify go on?
	    notifyAll();
	}
	return;
    }
    
    // Finds and returns the port with the smallest write capacity 
    // that is blocked on a write.
    private void _incrementLowestWriteCapacityPort() {
        //System.out.println("Incrementing capacity");    
        //FIXME: Should this be synchronized
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
		//smallestCapacityRecep = flowqueue;
	    }
	}
        try {
            if (smallestCapacityQueue.capacity() <= 0) { 
                smallestCapacityQueue.setCapacity(1);
            } else { 
	        smallestCapacityQueue.setCapacity(smallestCapacityQueue.capacity()+1);
            }
	    //_readblockedQs.remove((PNPort)smallestCapacityRecep.getContainer());
	    //FIXME: Wont this alwas be true?? Check it out
	    //if ((PNInPort)(smallestCapacityRecep.getContainer()).isWritePending()) {
	    writeUnblock(smallestCapacityQueue);
	    smallestCapacityQueue.setWritePending(false);
	    smallestCapacityQueue.notifyAll();
	    //}
        } catch (IllegalActionException e) {
	    System.err.println("Exception: " + e.toString());
	    //Should not be thrown as this exception is thrown 
            //only if port is not an input port, checked above
        }
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
	synchronized (this) {
	    while (!_deadlock && !_urgentMutations) {
		//System.out.println("Waiting with mutations = "+_urgentMutations);
		workspace().wait(this);
		//System.out.println("Woken up hmm");
	    }
	    urgentmut = _urgentMutations;
	    _urgentMutations = false;
	    deadl = _deadlock;
	    writebl = _writeBlockCount;
	}
	//}
	//System.out.println(" deadlock = "+_deadlock+" and mut ="+_urgentMutations);
	if (urgentmut) {
	    //FIXME: Should get a linked list anyway, for what about 
	    //paused receivers of deleted actors?
	    //FIXME: I should perhaps check that all of them are paused!!
	    LinkedList pausedrecs = setPause(true);
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
	        }
            } catch (NameDuplicationException nde) {
                // FIXME: this is just to get this thing to compile
                throw new IllegalActionException("Name duplication error: " +
                        nde.getMessage());
            }
	    //System.out.println("Done mutations");
	    //setPause(false);
	    setResume(pausedrecs);
	    //}
	    //_urgentMutations = false;
	    return false;
	}
	if (writebl==0 && deadl) {
	    //_terminate = true;
	    _terminateAll();
	    System.out.println("real deadlock. Everyone would be erased");
	    return true;
	}
	else {
	    // it's an artificial deadlock
	    System.out.println("Artificial deadlock");
	    _deadlock = false;
	    // find the input port with lowest capacity queue
	    // that is blocked on a write and increment it's capacity
	    _incrementLowestWriteCapacityPort();
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

    private LinkedList _threadlist = new LinkedList();
    private boolean _pause = false;
    private boolean _mutate = true;
    // Container is the CompositeEntity the executive is responsible for
    //private CompositeEntity _container;
    // Is set when a deadlock occurs
    private boolean _deadlock = false;
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
    //private LinkedList _readblockedQs = new LinkedList();
    private LinkedList _writeblockedQs = new LinkedList();
    private PNActorListener _pnActorListener;
}


















































































