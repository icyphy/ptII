/* Base class of directors for the process oriented domains.

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
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.data.*;

import java.util.Iterator;
import java.util.LinkedList;

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
are _isDeadlocked() and _handleDeadlock(). These methods should be
overridden in derived classes to get domain-specific behaviour. The
implementations given here are trivial and suffice only to illustrate
the approach that should be followed.
<p>
@author Mudit Goel, Neil Smyth, John S. Davis II
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

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public ProcessDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name contains a period,
     *   or if the director is not compatible with the specified container.
     */
    public ProcessDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero
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
        newobj._notDone = true;
        newobj._pausedReceivers = new LinkedList();
        newobj._threadList = new LinkedList();
	newobj._threadsStopped = 0;
        return newobj;
    }

    /** End the execution of the model under the control of this
     *  director. A flag is set in all the receivers which causes
     *  each process to terminate at the earliest communication point.
     */
    public void finish() {
	try {
	    wrapup();
	} catch( IllegalActionException e ) {
	    System.err.println("Error in calling "+getName()+".wrapup()");
	    e.printStackTrace();
	}
    }

    /** Wait until a deadlock is detected. Then handle the deadlock
     *  (by calling the protected method _handleDeadlock()) and return.
     *  This method is synchronized on the director.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void fire() throws IllegalActionException {
	Workspace workspace = workspace();
        synchronized (this) {
            while( !_areAllThreadsStopped() && !_isDeadlocked() ) {
		workspace.wait(this);
            }
            if( _isDeadlocked() ) {
                _notDone = _resolveDeadlock();
            } else {
		_notDone = true;
	    }
        }
    }

    /** Increases the count of paused threads and checks whether the
     *  entire model has successfully paused.
     */
    public synchronized void increasePausedCount() {
        _actorsPaused++;
        if (_isPaused()) {
	    notifyAll();
	}
    }

    /** Invoke the initialize() methods of all the deeply contained
     *  actors in the container (a composite actor) of this director.
     *  These are expected to call initialize(Actor), which will result
     *  in the creation of a new thread for each actor.
     *  Also, set current time to 0.0, or to the current time of
     *  the executive director of the container, if there is one.
     *
     *  @exception IllegalActionException If the initialize() method
     *   of one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
	_notDone = true;
	_actorsActive = 0;
	_actorsPaused = 0;
	_threadsStopped = 0;
	_pausedReceivers = new LinkedList();
	_threadList = new LinkedList();
	_newthreads = new LinkedList();
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            /*
	    CompositeActor containersContainer =
                (CompositeActor)container.getContainer();
	    if( containersContainer == null ) {
		setCurrentTime(0.0);
	    } else {
		double time =
                    containersContainer.getDirector().getCurrentTime();
                setCurrentTime(time);
	    }
            */

            // Creating threads for all actors;
            Iterator actors = container.deepEntityList().iterator();
            while( actors.hasNext() ) {
                Actor actor = (Actor)actors.next();
                actor.initialize();
            }
        }
    }

    /** Perform domain-specific initialization on the specified actor, if any.
     *  In this base class, initialize a ProcessThread for each actor.
     *  This is called by the initialize() method of the actor, and may be
     *  called after the initialization phase of an execution.  In particular,
     *  in the event of mutations during an execution that introduce new
     *  actors, this method will be called as part of initializing the
     *  new actors.
     *  @exception IllegalActionException If the actor is not acceptable
     *   to the domain.  Not thrown in this base class.
     */
    public void initialize(Actor actor) throws IllegalActionException {
        // Reset the receivers.
        Iterator ports = actor.inputPortList().iterator();
        while( ports.hasNext() ) {
            IOPort port = (IOPort)ports.next();
            Receiver[][] rcvrs = port.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    ((ProcessReceiver)rcvrs[i][j]).reset();
                }
            }
        }

        // Initialize threads
        ProcessThread processThread = _getProcessThread(actor, this);
        _threadList.addFirst(processThread);
        _newthreads.addFirst(processThread);
    }

    /** Return false if the model has reached a deadlock and can
     *  be terminated if desired. Return true otherwise.
     *  This flag is set on detection of a deadlock in the fire() method.
     *  @return false if the director has detected a deadlock and can be
     *  terminated if desired.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean postfire() throws IllegalActionException {
	return _notDone;
    }

    /** Start threads for all actors that have not had threads started
     *  already (this might include actors initialized since the last
     *  invocation of prefire). This starts the threads, corresponding
     *  to all the actors, that were created in the initialize() method.
     *  @return true Always returns true.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean prefire() throws IllegalActionException  {
        Iterator threads = _newthreads.iterator();
        ProcessThread thread = null;
	if( _areAllThreadsStopped() ) {
	    threads = _threadList.iterator();
	    while( threads.hasNext() ) {
		thread = (ProcessThread)threads.next();
		thread.restartThread();
		synchronized(thread) {
		    thread.notifyAll();
		}
		if( _threadsStopped > 0 ) {
		    _threadsStopped--;
		}
	    }
	} else {
            threads = _newthreads.iterator();
            while (threads.hasNext()) {
                thread = (ProcessThread)threads.next();
		thread.start();
	    }
	    _newthreads.clear();
        }
        return true;
    }

    /** Pause the execution of the model. This method iterates through the
     *  set of actors in the compositeActor and sets the pause flag of all
     *  the receivers. It also sets the pause flag in all the output
     *  ports of the CompositeActor under control of this director.
     *  <p>
     *  @exception IllegalActionException If cannot access all the receivers.
     */
    //  Note: Should be fixed after Director/Manager define pause, etc.
    //  Should a pausedEvent be sent when the model is fully paused?
    public void pause() throws IllegalActionException {
        Workspace workspace = workspace();
	workspace.getReadAccess();
	try {
	    // Obtaining a list of all actors in this compositeActor
	    CompositeActor cont = (CompositeActor)getContainer();
            Iterator actors = cont.deepEntityList().iterator();
	    Iterator actorPorts;
	    ProcessReceiver nextRec;

	    while (actors.hasNext()) {
		// Obtaining all the ports of each actor
		Actor actor = (Actor)actors.next();
		actorPorts = actor.inputPortList().iterator();
		while (actorPorts.hasNext()) {
		    IOPort port = (IOPort)actorPorts.next();
		    // Setting paused flag in the receivers..
		    Receiver[][] receivers = port.getReceivers();
		    for (int i = 0; i < receivers.length; i++) {
			for (int j = 0; j < receivers[i].length; j++) {
			    nextRec = (ProcessReceiver)receivers[i][j];
			    nextRec.requestPause(true);
			    _pausedReceivers.addFirst(receivers[i][j]);
			}
		    }
		}
	    }

	    // If this director is controlling a CompositeActor with
	    // output ports, need to set the finished flag
	    // there as well.
	    // NOTE: is this the best way to set these flags.
	    actorPorts  = cont.outputPortList().iterator();
	    while (actorPorts.hasNext()) {
		IOPort port = (IOPort)actorPorts.next();
		// Terminating the ports and hence the star
		Receiver[][] receivers = port.getReceivers();
		for (int i = 0; i < receivers.length; i++) {
		    for (int j = 0; j < receivers[i].length; j++) {
			nextRec = (ProcessReceiver)receivers[i][j];
			nextRec.requestPause(true);
			_pausedReceivers.addFirst(receivers[i][j]);
		    }
		}
	    }
	} finally {
	    workspace.doneReading();
	}

	synchronized (this) {
	    while (!_isPaused()) {
		workspace.wait(this);
	    }
	}
        return;
    }

    /** Indicate to the director that a new thread under it's control
     *  has been stopped.
     */
    public synchronized void registerStoppedThread() {
 	_threadsStopped++;
 	notifyAll();
    }

    /** Resumes execution of the model.
     *  All the actors that were paused using setResumePaused are resumed.
     *  The pause flag of the receivers is set to false and all the threads
     *  that are waiting on the receiver are notified.
     */
    public void resume() {
	LinkedList copy = new LinkedList();
	synchronized(this) {
	    copy.addAll(_pausedReceivers);
	    _pausedReceivers.clear();
	    _actorsPaused = 0;
	}
	Iterator receivers = copy.iterator();
	while (receivers.hasNext()) {
	    ProcessReceiver rec = (ProcessReceiver)receivers.next();
            rec.requestPause(false);
	}
        // Now wake up all the receivers.
        (new NotifyThread(copy)).start();
    }

    /** Request that execution of the current iteration stop. Call
     *  stopThread on each of the process threads that contain actors
     *  controlled by this director and call stopFire on the actors
     *  that are contained by these threads. This method is non-blocking.
     *  After calling this method, the fire() method of this director
     *  is guaranteed to return in finite time.
     */
    public void stopFire() {
 	Iterator threads = _threadList.iterator();
 	while( threads.hasNext() ) {
 	    ProcessThread thread = (ProcessThread)threads.next();

	    // Call stopThread() on the threads first
 	    thread.stopThread();
	    thread.getActor().stopFire();
 	}
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
	list.addAll(_threadList);
	_threadList.clear();
        Iterator threads = list.iterator();
        while (threads.hasNext()) {
	    ((Thread)threads.next()).stop();
        }
    }

    /** End the execution of the model under the control of this
     *  director. A flag is set in all the receivers that causes
     *  each process to terminate at the earliest communication point.
     *  <P>
     *  Prior to setting receiver flags, this method wakes up the
     *  threads if they all are stopped.
     *  <P>
     *  This method is not synchronized on the workspace, so the caller
     *  should be.
     * @exception IllegalActionException If an error occurs while
     *  accessing the receivers of all actors under the control of
     *  this director.
     */
    public void wrapup() throws IllegalActionException {
	// First wake up threads if they are stopped.
        ProcessThread thread = null;
	if( _areAllThreadsStopped() ) {
	    Iterator threads = _threadList.iterator();
	    while( threads.hasNext() ) {
		if( _threadsStopped > 0 ) {
		    _threadsStopped--;
		}
		thread = (ProcessThread)threads.next();
		thread.restartThread();
	    }
	}

	CompositeActor cont = (CompositeActor)getContainer();
        Iterator actors = cont.deepEntityList().iterator();
        Iterator actorPorts;
        ProcessReceiver nextRec;
        LinkedList recs = new LinkedList();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            actorPorts = actor.inputPortList().iterator();
            while (actorPorts.hasNext()) {
                IOPort port = (IOPort)actorPorts.next();
                // Setting finished flag in the receivers.
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        nextRec = (ProcessReceiver)receivers[i][j];
                        nextRec.requestFinish();
                        recs.addFirst(nextRec);
                    }
                }
            }

            // If this director is controlling a CompositeActor with
            // output ports, need to set the finished flag
            // there as well.
            actorPorts  = cont.outputPortList().iterator();
            while (actorPorts.hasNext()) {
                IOPort port = (IOPort)actorPorts.next();
                // Terminating the ports and hence the actor
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        nextRec = (ProcessReceiver)receivers[i][j];
                        nextRec.requestFinish();
                        recs.addFirst(nextRec);
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
	_threadList.addFirst(thr);
    }

    /** Determine if all of the threads containing actors controlled
     *  by this director have stopped due to a call of stopFire().
     *  Override this method in subclasses to account for possible
     *  deadlock situations due to additional flags that are not
     *  present in this base class.
     * @return True if all active threads containing actors controlled
     *  by this thread have stopped; otherwise return false.
     */
    protected synchronized boolean _areAllThreadsStopped() {
	// All threads are stopped due to stopFire()
	if( _threadsStopped > 0 && _threadsStopped >= _actorsActive ) {
	    return true;
	}
	return false;
    }

    /** Return true if the count of active processes in the container is 0.
     *  Otherwise return true. Derived classes must override this method to
     *  return true to any other forms of deadlocks that they might introduce.
     * @return true if there are no active processes in the container.
     * @deprecated use isDeadlocked() instead.
     */
    protected synchronized boolean _checkForDeadlock() {
        return (_actorsActive == 0);
    }

    /** Return true if all active processes in the container are paused.
     *  Return false otherwise.
     *  Should be overridden in derived classes to return true if all the
     *  active processes are either blocked or paused.
     * @deprecated Use isPaused() instead.
     */
    protected synchronized boolean _checkForPause() {
        return (_actorsPaused == _actorsActive);
    }

    /** Decrease by one the count of active processes under the control of
     *  this director. Also check whether the model is now paused
     *  if a pause was requested.
     *  This method should be called only when an active thread that was
     *  registered using _increaseActiveCount() is terminated.
     *  This count is used to detect deadlocks for termination and other
     *  reasons.
     */
    protected synchronized void _decreaseActiveCount() {
	_actorsActive--;
	if (_isDeadlocked() || _isPaused()) {
	    //Wake up the director waiting for a deadlock
	    notifyAll();
	}
    }

    /** Return the number of active processes under the control of this
     *  director.
     * @return The number of active actors.
     */
    protected synchronized long _getActiveActorsCount() {
	return _actorsActive;
    }


    /** Return the number of paused processes under the control of this
     *  director.
     * @return The number of paused actors.
     */
    protected synchronized long _getPausedActorsCount() {
	return _actorsPaused;
    }

    /** Create a new ProcessThread for controlling the actor that
     *  is passed as a parameter of this method. Subclasses are
     *  encouraged to override this method as necessary for domain
     *  specific functionality.
     * @param actor The actor that the created ProcessThread will
     *  control.
     * @param director The director that manages the model that the
     *  created thread is associated with.
     * @return Return a new ProcessThread that will control the
     *  actor passed as a parameter for this method.
     */
    protected ProcessThread _getProcessThread(Actor actor,
	    ProcessDirector director) throws IllegalActionException {
	return new ProcessThread(actor, director);
    }

    /** Return the number of processes stopped because of a call to the
     *  stopfire method of the process.
     * @return The number of stopped processes.
     */
    protected synchronized long _getStoppedProcessesCount() {
	return _threadsStopped;
    }

    /** Return true.
     *  In derived classes, override this method to obtain domain
     *  specific handling of deadlocks. It should return true if a
     *  real deadlock has occurred and the simulation can be ended.
     *  It should return false if the simulation has data to proceed and
     *  need not be terminated.
     * @return True.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _handleDeadlock() throws IllegalActionException {
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

    /** Return true if the count of active processes in the container is 0.
     *  Otherwise return true. Derived classes must override this method to
     *  return true to any other forms of deadlocks that they might introduce.
     * @return true if there are no active processes in the container.
     */
    protected synchronized boolean _isDeadlocked() {
        return (_actorsActive == 0);
    }

    /** Return true if all active processes in the container are paused.
     *  Return false otherwise.
     *  Should be overridden in derived classes to return true if all the
     *  active processes are either blocked or paused.
     * @return true only if all active processes are paused.
     */
    protected synchronized boolean _isPaused() {
        return (_actorsPaused == _actorsActive);
    }

    /** Return false.
     *  In derived classes, override this method to obtain domain
     *  specific handling of deadlocks. Return false if a
     *  real deadlock has occurred and the simulation can be ended.
     *  Return true if the simulation can proceed given additional
     *  data and need not be terminated.
     * @return False.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _resolveDeadlock() throws IllegalActionException {
	return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Flag for determining when an iteration completes
    protected boolean _notDone = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Count of the number of processes that were started by this
    // director but have not yet finished.
    private long _actorsActive;

    // Count of the number of processes that have been paused
    // following a request for a pause.
    private long _actorsPaused;

    // The receivers that were paused when a pause was requested.
    private LinkedList _pausedReceivers;

    // The threads started by this director.
    private LinkedList _threadList;

    //A copy of threads started since the last invocation of prefire().
    private LinkedList _newthreads;

    // A count of the active threads controlled by this director that
    // have been stopped
    private int _threadsStopped = 0;

}
