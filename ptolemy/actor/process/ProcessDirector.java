/* Base class of directors for the process oriented domains.

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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.data.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ProcessDirector
/**
Base class of directors for the process oriented domains. It provides
default implementations for methods that are common across such domains.
<p>
In the process oriented domains, the director controlling a model 
needs to keep track of the state of the model. In particular it needs 
to maintain an accurate count of the number of active processes under 
its control, any processes that are blocked for whatever reason (trying 
to read from an empty channel as in PN) and the number of processes that have 
been paused. These counts, and perhaps other counts, are needed by the 
director to control and respond when deadlock is detected (no processes 
can make progress), or to respond to requests from higher in the hierarchy.
<p>
The methods that control how the director detects and responds to deadlocks
are _checkForDeadlock() and _handleDeadlock(). These methods should be
overridden in derived classes to get domain-specific behaviour. The 
implementations given here are trivial and suffice only to illustrate 
the approach that should be followed.
<p>
@author Mudit Goel, Neil Smyth
@version $Id$
@see Director
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
     *  @param name Name of this director.
     */
    public ProcessDirector(String name) {
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
    public ProcessDirector(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active proceses is zero 
     *  and it is not paused.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new ProcessDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        ProcessDirector newobj = (ProcessDirector)super.clone(ws);
        newobj._actorsActive = 0;
        newobj._actorsPaused = 0;
        newobj._notdone = true;
        newobj._pausedReceivers = new LinkedList();
        newobj._threadList = new LinkedList();
        return newobj;
    }

    /** Wait till the detection of a deadlock. Then handle the deadlock
     *  and return.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void fire()
	    throws IllegalActionException {
	Workspace worksp = workspace();
	synchronized (this) {
	    while (!_checkForDeadlock()) {
		worksp.wait(this);
	    }
	    _notdone = !_handleDeadlock();
	}
        return;
    }


    /** Increases the count of paused threads and checks whether the
     *  entire model has sucessfully paused.
     */
    public synchronized void increasePausedCount() {
        _actorsPaused++;
        if (_checkForPause()) {
	    notifyAll();
	}
    }

    /** Invokes the initialize() methods of all the deeply contained
     *  actors in the container (a composite actor) of this director.
     *  It creates a new ProcessThread for each actor. It also creates 
     *  receivers for all the ports.
     *  <p>
     *  The current time of execution of the container and all the 
     *  actors contained in it is set.
     *  <p>
     *
     *  @exception IllegalActionException If the initialize() method 
     *   of one of the deeply contained actors throws it.
     */
    public void initialize()
            throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            //Creating receivers and threads for all actors;
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                ProcessThread pnt = new ProcessThread(actor, this);
                _threadList.insertFirst(pnt);
		_newthreads.insertFirst(pnt);
                actor.createReceivers();
                actor.initialize();
            }
            setCurrentTime(getCurrentTime());
        }
    }


    /** Return false if the model has reached a deadlock and can
     *  be terminated if desired. Return true otherwise.
     *  This flag is set on detection of a deadlock in the fire() method.
     *  @return false if the director has detected a deadlock and can be 
     *  terminated if desired. 
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean postfire() throws IllegalActionException {
	return _notdone;
    }

    /** Return true.
     *  This starts the threads, corresponding to all the actors, that 
     *  were created in the initialize() method.
     *  @return true Always returns true.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean prefire() throws IllegalActionException  {
        Enumeration threads = _newthreads.elements();
        // Starting threads.
        while (threads.hasMoreElements()) {
            ProcessThread thread = (ProcessThread)threads.nextElement();
            thread.start();
        }
	_newthreads.clear();
        return true;
    }

    /** Pause the execution of the model. This method iterates through the 
     *  set of actors in the compositeActor and sets the pause flag of all
     *  the receivers. It also sets the pause flag in all the output
     *  ports of the CompositeActor under control of this director.
     *  <p>
     *  @exception IllegalActionException If cannot access all the receivers.
     */
    //  Note: Should be fixed after Director/Manager define pause, et. al.
    //  Should a pausedEvent be sent when the model is fully paused?
    public void pause() 
            throws IllegalActionException {
        Workspace worksp = workspace();
	worksp.getReadAccess();
	try {
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
			    nextRec.setPause(true);
			    _pausedReceivers.insertFirst(receivers[i][j]);
			}
		    }
		}
	    }
	    
	    // If this director is controlling a CompositeActor with
	    // output ports, need to set the finished flag
	    // there as well.
	    // FIXME: is this the best way to set these flags.
	    actorPorts  = cont.outputPorts();
	    while (actorPorts.hasMoreElements()) {
		IOPort port = (IOPort)actorPorts.nextElement();
		// Terminating the ports and hence the star
		Receiver[][] receivers = port.getReceivers();
		for (int i = 0; i<receivers.length; i++) {
		    for (int j = 0; j<receivers[i].length; j++) {
			nextRec = (ProcessReceiver)receivers[i][j];
			nextRec.setPause(true);
			_pausedReceivers.insertFirst(receivers[i][j]);
		    }
		}
	    }
	} finally {
	    worksp.doneReading();
	}
	
	synchronized (this) {
	    while (!_checkForPause()) {
		worksp.wait(this);
	    }
	}
        return;
    }
    
    /** Resumes execution of the model. 
     *  All the actors that were paused using setResumePaused are resumed. 
     *  The pause flag of the receivers is set to false and all the threads
     *  that are waiting on the receiver are notified.
     */
    public void resume() {
	LinkedList copy = new LinkedList();
	synchronized(this) {
	    copy.appendElements(_pausedReceivers.elements());
	    _pausedReceivers.clear();
	    _actorsPaused = 0;
	}
	Enumeration receivers = copy.elements();
	while (receivers.hasMoreElements()) {
	    ProcessReceiver rec = (ProcessReceiver)receivers.nextElement();
            rec.setPause(false);
	}
        // Now wake up all the receivers.
        (new NotifyThread(copy)).start();
    }


    /** Terminates all threads under control of this director immediately.
     *  This abrupt termination will not allow normal cleanup actions
     *  to be performed, and the model should be recreated after calling 
     *  this method.
     */
    //  Note: for now call Thread.stop() but should change to use
    //  Thread.destroy() when it is eventually implemented.
    public void terminate() {
        // First need to invoke terminate on all actors under the
        // control of this director.
        super.terminate();
        // Now stop any threads created by this director.
	LinkedList list = new LinkedList();
	list.appendElements(_threadList.elements());
	_threadList.clear();
        Enumeration threads = list.elements();
        while (threads.hasMoreElements()) {
	    ((Thread)threads.nextElement()).stop();
        }
    }


    /** Ends the execution of the model under the control of this 
     *  director. A flag is set in all the receivers
     *  which causes each process to terminate at the earliest
     *  communication point.
     *  <p>
     *  This method is not synchronized on the workspace, so the caller 
     *  should be.
     *  Note that the wrapup methods are not invoked on the actors
     *  under control of this director as each actor is executed by a
     *  seperate thread. They are called from the thread itself.
     *  <p>
     *  @exception IllegalActionException if a method accessing the topology
     *   throws it.
     */
    public void wrapup() throws IllegalActionException {
	CompositeActor cont = (CompositeActor)getContainer();
        Enumeration allMyActors = cont.deepGetEntities();
        Enumeration actorPorts;
        ProcessReceiver nextRec;
        LinkedList recs = new LinkedList();
        while (allMyActors.hasMoreElements()) {
            Actor actor = (Actor)allMyActors.nextElement();
            actorPorts = actor.inputPorts();
            while (actorPorts.hasMoreElements()) {
                IOPort port = (IOPort)actorPorts.nextElement();
                // Setting finished flag in the receivers.
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i<receivers.length; i++) {
                    for (int j = 0; j<receivers[i].length; j++) {
                        nextRec = (ProcessReceiver)receivers[i][j];
                        nextRec.setFinish();
                        recs.insertFirst(nextRec);
                    }
                }
            }

            // If this director is controlling a CompositeActor with
            // output ports, need to set the finished flag
            // there as well.
            actorPorts  = cont.outputPorts();
            while (actorPorts.hasMoreElements()) {
                IOPort port = (IOPort)actorPorts.nextElement();
                // Terminating the ports and hence the star
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i<receivers.length; i++) {
                    for (int j = 0; j<receivers[i].length; j++) {
                        nextRec = (ProcessReceiver)receivers[i][j];
                        nextRec.setFinish();
                        recs.insertFirst(nextRec);
                    }
                }
            }

            // Now wake up all the receivers.
            (new NotifyThread(recs)).start();
        }
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a thread to the list of threads in the model. 
     *  This list is used in case of abrupt termination of the model
     *  @param thr The newly created thread
     */
    protected synchronized void _addNewThread(ProcessThread thr) {
	_threadList.insertFirst(thr);
    }

    /** Checks for deadlock. In the base class implementation it 
     *  notifies the director of a deadlock only if there are no active 
     *  processes.
     */
    protected synchronized boolean _checkForDeadlock() {
        if (_actorsActive == 0) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Checks if all active processes are either blocked or paused.
     *  Should be overridden in derived classes. In the base class it 
     *  verifies if all the active actors are paused.
     */
    protected synchronized boolean _checkForPause() {
        if (_actorsPaused >= _actorsActive) {
	     return true;
	} else {
	    return false;
	}
    }

    /** Decrease by one the count of active processes under the control of 
     *  this director. Also check whether the model is now paused
     *  if a pause was requested.
     *  This method should be called only when an active thread that was
     *  registered using _increaseActiveCount() is terminated.
     *  This count is used to detect deadlocks for termination and other
     *  reasons.
     */
    synchronized void _decreaseActiveCount() {
	_actorsActive--;
	if (_checkForDeadlock() || _checkForPause()) {
	    //Wake up the director waiting for a deadlock
	    notifyAll();
	}
    }

    /** Return the number of active processes under the control of this 
     *  director.
     *  @return The number of active actors.
     */
    protected synchronized long _getActiveActorsCount() {
	return _actorsActive;
    }


    /** Return the number of paused processes under the control of this 
     *  director.
     *  @return The number of active actors.
     */
    protected synchronized long _getPausedActorsCount() {
	return _actorsPaused;
    }

    /** Return true. 
     *  In derived classes, override this method to obtain domain
     *  specific handling of deadlocks. It should return true if a 
     *  real deadlock has occured and the simulation can be ended.
     *  It should return if the simulation has data to proceed and
     *  need not be terminated.
     *
     *  @return true.
     *  @exception IllegalActionException If a derived class throws it.
     */
    protected boolean _handleDeadlock()
	    throws IllegalActionException {
	return true;
    }

    /** Increases the count of active actors in the composite actor 
     *  corresponding to this director by 1. This method should be 
     *  called when a new thread corresponding to an actor is started 
     *  in the model under the control of this director. This method 
     *  is required for detection of deadlocks.
     *  The corresponding method _decreaseActiveCount should be called
     *  when the thread is terminated.
     */
    synchronized void _increaseActiveCount() {
	_actorsActive++;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    // Flag for determining when an iteration completes
    protected boolean _notdone = true;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Count of the number of processes that were started by this 
    // director but have not yet finished.    
    private long _actorsActive = 0;

    // Count of the number of processes that have been paused 
    // following a request for a pause.
    private long _actorsPaused = 0;

    //private boolean _deadlock = false;

    // The receivers that were paused when a pause was repuested.
    private LinkedList _pausedReceivers = new LinkedList();

    // The threads started by this director.
    private LinkedList _threadList = new LinkedList();

    //A copy of threads started by the directors in this iteration.
    private LinkedList _newthreads = new LinkedList();
}










