/* Base class of directors for the process orientated domains.

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

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.mutation.Mutation; // FIXME: this will change to kernel.event
import ptolemy.data.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ProcessDirector 
/** 
Base class of directors for the process orientated domains. It provides 
default implementations for methods that are common across such domains.

@author Neil Smyth, Mudit Goel
@version $Id$
*/
public class ProcessDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ProcessDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public ProcessDirector(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public ProcessDirector(Workspace workspace, String name) {
        super(workspace, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This decreases the number of active threads in the 
     *  compositeActor by 1.
     *  It also checks if the simulation has paused if a pause was 
     *  requested.
     *  This method should be called only when an active thread that was 
     *  registered using increaseActiveCount() is terminated.
     */
    public synchronized void decreaseActiveCount() {
	_actorsActive--;	    
	_checkForDeadlock();
	//System.out.println("decreased active count");
        //If pause requested, then check if paused
        if (_pause) {
            _checkForPause();
        }
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

    /** This invokes the initialize() methods of all its deeply contained 
     *  actors. It also creates receivers for all the ports and increases the
     *  number of active threads for each actor being initialized.
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
                //increaseActiveCount should be called before createReceivers
                // as that might increase the count of processes blocked 
                //and deadlocks might get detected even if there are no 
                //deadlocks
                increaseActiveCount();
                actor.createReceivers();
                actor.initialize();
                ProcessThread pnt = new ProcessThread(actor, this);
                _threadList.insertFirst(pnt);
            }
        }
    }

    /** This method should be called when a new thread corresponding 
     *  to an actor is started in a simulation. This method is 
     *  required for detection of deadlocks. 
     *  The corresponding method decreaseActiveCount should be called 
     *  when the thread is terminated.
     */
    public synchronized void increaseActiveCount() {
	_actorsActive++;
    }

    /** This method increases the number of paused threads and checks if the 
     *  entire simulation is paused. 
     */
    public synchronized void increasePausedCount() {
        _actorsPaused++;
        _checkForPause();
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
        //_threadList.clear();
        return true;
    }

    /** Add a mutation object to the mutation queue. These mutations
     *  are executed when the _performMutations() method is called,
     *  which in this class is in the fire() method.  This method
     *  also arranges that the director gets notified of queueing of new 
     *  mutations.
     *  <p>
     *  This method should be called only if the mutations are to be performed
     *  immediately.
     *  <p> FIXME: why does this method need readAccess
     *  FIXME: this will change to use TopologyChangeRequest.
     *
     *  @param mutation A object with a perform() and update() method that
     *   performs a mutation and informs any listeners about it.
     */
    public void queueMutation(Mutation mutation) {
	try {
	    super.queueMutation(mutation);
	    setPause();
	    synchronized(this) {
		_urgentMutations = true;
		notifyAll();
	    } 
	} catch (IllegalActionException e) {
	    System.err.println(e.toString());
	}
    }
    /** Pause the simulation. This method iterates through the set of 
     *  actors in the compositeActor and sets the pause flag of all 
     *  the receivers. It also sets the pause flag in all the output 
     *  ports of the CompositeActor under control of this director.
     *  <p>
     *  FIXME: why is this read locked?
     *  @exception IllegalActionException FIXME: is this called?
     */
    public void setPause() throws IllegalActionException {        
        synchronized(this) {
            // If already paused do nothing.
            if (_pause) {
                return;
            }
            _pause = true;
        }
	workspace().getReadAccess();
	try {
	    //LinkedList _pausedReceivers = new LinkedList();

	    // Obtaining a list of all actors in this compositeActor 
	    CompositeActor cont = (CompositeActor)getContainer();
            Enumeration allMyActors = cont.deepGetEntities();
            Enumeration actorPorts;
            
            ProcessReceiver nextRec;
                
            while (allMyActors.hasMoreElements()) {
                // Obtaining all the ports of each actor 
                Actor actor = (Actor)allMyActors.nextElement();
                 actorPorts = actor.inputPorts();
                while (actorPorts.hasMoreElements()) {
                    IOPort port = (IOPort)actorPorts.nextElement();
                    // Setting paused flag in the receivers..
                    Receiver[][] receivers = port.getReceivers();
                    for (int i=0; i<receivers.length; i++) {
                        for (int j=0; j<receivers[i].length; j++) {
                            nextRec = (ProcessReceiver)receivers[i][j];
                            nextRec.setSimulationPaused(true);
                            _pausedReceivers.insertFirst(receivers[i][j]);
                            synchronized(nextRec) {
                                nextRec.notifyAll();
                            }
                        }
		    }
		}
	    }
    
            // If this director is controlling a CompositeActor with 
            // output ports, need to set the simulationFinished flag 
            // there as well. 
            actorPorts  = cont.outputPorts();
            while (actorPorts.hasMoreElements()) {
                IOPort port = (IOPort)actorPorts.nextElement();
                // Terminating the ports and hence the star
                Receiver[][] receivers = port.getReceivers();
                for (int i=0; i<receivers.length; i++) {
                    for (int j=0; j<receivers[i].length; j++) {
                        nextRec = (ProcessReceiver)receivers[i][j];
                        nextRec.setSimulationPaused(true);
                        _pausedReceivers.insertFirst(receivers[i][j]);
                        synchronized(nextRec) {
                            nextRec.notifyAll();
                        }
                    }
                }
            }

            // A linked list of pausedreceivers is returned as it might not be 
            // possible to resume these receivers otherwise if they have been
            // removed from the hierarchy. They still need to be awakened so 
            // that the deadlocks can be detected and threads terminated.
	    return; 
	} finally {
	    workspace().doneReading();
	}
    }

    /** Resumes the simulation. If the simulation is not paused do nothing.
     */
    public void setResume() {
        synchronized(this) {
            if (!_pause) {
                return;
            }
        }
	Enumeration receivers = _pausedReceivers.elements();
	while (receivers.hasMoreElements()) {
	    ProcessReceiver rec = (ProcessReceiver)receivers.nextElement();
            rec.setSimulationPaused(false);
	}
        synchronized (this) {
            _pausedReceivers.clear();
            _actorsPaused= 0;
            _pause = false;
        }
    }

    /** Terminate all threads under control of this director immediately.
     *  This abrupt termination will not allow normal cleanup actions 
     *  to be performed.
     *
     *  FIXME: for now call Thread.stop() but should change to use 
     *  Thread.destroy() when it is eventually implemented.
     */
    public synchronized void terminate() {
        // First need to invoke terminate on all actors under the 
        // control of this director.
        super.terminate();
        // Now stop any threads created by this director.
        Enumeration threads = _threadList.elements();
        while (threads.hasMoreElements()) {
            Thread next = (Thread)threads.nextElement();
            if (next.isAlive()) {
                next.stop();
            }
        }
    }


    /** End the simulation. A flag is set in all the receivers
     *  which causes each process to terminate the next time it 
     *  reaches a communication point.
     *  <p>
     *  Note that the wrapup methods are not invoked on the actors 
     *  under control of this director as each actor is executed by a 
     *  seperate thread.
     *  <p>
     **  @exception IllegalActionException if a method accessing the topology
     *   throws it.
     */
    public synchronized void wrapup() throws IllegalActionException {
	CompositeActor cont = (CompositeActor)getContainer();
        Enumeration allMyActors = cont.deepGetEntities();
        
        Enumeration actorPorts;
        ProcessReceiver nextRec;

        while (allMyActors.hasMoreElements()) {
            try {
                Actor actor = (Actor)allMyActors.nextElement();
                actorPorts = actor.inputPorts();
                while (actorPorts.hasMoreElements()) {
                    IOPort port = (IOPort)actorPorts.nextElement();
                    // Setting finished flag in the receivers.
                    Receiver[][] receivers = port.getReceivers();
                    for (int i=0; i<receivers.length; i++) {
                        for (int j=0; j<receivers[i].length; j++) {
                            nextRec = (ProcessReceiver)receivers[i][j];
                            nextRec.setSimulationFinished();
                            synchronized(nextRec) {
                                nextRec.notifyAll();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // FIXME: should not catch general exception
                System.out.println("ProcessDirector: unable to terminate " +
                        "all actors because: " + ex.getClass().getName() +
                        ", message: " + ex.getMessage());
            }

        }
        
        // If this director is controlling a CompositeActor with 
        // output ports, need to set the simulationFinished flag 
        // there as well. 
        actorPorts  = cont.outputPorts();
        while (actorPorts.hasMoreElements()) {
            IOPort port = (IOPort)actorPorts.nextElement();
            // Terminating the ports and hence the star
            Receiver[][] receivers = port.getReceivers();
            for (int i=0; i<receivers.length; i++) {
                for (int j=0; j<receivers[i].length; j++) {
                    nextRec = (ProcessReceiver)receivers[i][j];
                    nextRec.setSimulationFinished();
                    synchronized(nextRec) {
                        nextRec.notifyAll();
                    }
                }
            }
        }
        return;
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Checks for deadlock(Both artificial and true deadlocks). 
     *  noOfStoppedProcess is 0 if the deadlock is being checked for a 
     *  blocked queue. 1 if check is being done when a process stopped.
     *  This is not synchronized and thus should be called from a 
     *  synchronized method.
     */
    protected synchronized void _checkForDeadlock() {
	System.out.println("_checkForDeadlock: No default implementation, " +
               "should be overridden in derived classes.");
        return;
    
    }

    /** Check if all threads are either blocked or paused.
     */
    protected synchronized void _checkForPause() {
	System.out.println("_checkForPause: No default implementation, " +
                "should be overridden in derived classes.");
       	return;
    }


    /* Handles deadlock, both real and artificial
     * Returns false only if it detects a mutation.
     * Returns true for termination.
     * This is not synchronized and should be synchronized in the 
     * calling method.
     */
    protected boolean _handleDeadlock() 
	    throws IllegalActionException {
       System.out.println("_handleDeadlock: No default implementation, " +
               "should be overridden in derived classes.");
       return true;
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    protected long _actorsActive = 0;
    protected long _actorsPaused = 0;

    // The threads under started by this director.
    private LinkedList _threadList = new LinkedList();

    private boolean _pause = false;
    private boolean _paused = false;
    private boolean _mutate = true;
    
    private boolean _notdone = true;
    
    private boolean _terminate = false;
    private boolean _urgentMutations = false;

    private LinkedList _pausedReceivers = new LinkedList();
}


















































































