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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red

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
Base class of directors for the process orientated domains. It provides
default implementations for methods that are common across such domains.

@author Mudit Goel, Neil Smyth
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

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  and no mutation listeners.
     *
     *  FIXME: not finished.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new CSPDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        ProcessDirector newobj = (ProcessDirector)super.clone(ws);
        newobj._actorsActive = 0;
        newobj._pausedReceivers = new LinkedList();
        return newobj;
    }

    /** This decreases the number of active threads in the
     *  compositeActor by 1.
     *  It also checks if the simulation has paused if a pause was
     *  requested.
     *  This method should be called only when an active thread that was
     *  registered using increaseActiveCount() is terminated.
     */
    // FIXME: If _threadList is not being destroyed then remove current thread
    // from it.
    public synchronized void decreaseActiveCount() {
	_actorsActive--;
	_checkForDeadlock();
	//System.out.println("decreased active count");
        //If pause requested, then check if paused
        if (_pauseRequested) {
            _checkForPause();
        }
    }


    /** This normally waits till the detection of a deadlock.
     *  In the base class this waits for all process threads to terminate.
     * @exception IllegalActionException If a derived class throws it.
     */
    public void fire()
	    throws IllegalActionException {
        while (!_handleDeadlock());
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
                String name = ((Nameable)actor).getName();
                ProcessThread pnt = new ProcessThread(actor, this, name);
                _threadList.insertFirst(pnt);
                // FIXME: perhaps we want ot destroy the threadlist?
            }
            setCurrentTime(0.0);
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

    /** This returns false if the simulation has reached a deadlock and can
     *  be terminated if desired. This flag is set on detection of a deadlock
     *  in the fire() method.
     *  @return false if the director has detected a deadlock and does not
     *  wish to be scheduled.
     *  @exception IllegalActionException This is never thrown in PN
     */
    public boolean postfire() throws IllegalActionException {
	return _notdone;
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
        //_threadList.clear(); // Should it be cleared or given to manager?
        return true;
    }

    /** Pause the simulation. This method iterates through the set of
     *  actors in the compositeActor and sets the pause flag of all
     *  the receivers. It also sets the pause flag in all the output
     *  ports of the CompositeActor under control of this director.
     *  <p>
     *  FIXME: should a simulationPausedEvent be sent when the
     *  simulation is fully paused?
     *  <p>
     *  FIXME: why is this read locked?
     *  @exception IllegalActionException If cannot access all the receivers.
     */
    public void setPauseRequested() throws IllegalActionException {
        synchronized(this) {
            // If already paused do nothing.
            if (_pauseRequested) {
                return;
            }
            _pauseRequested = true;
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
                    for (int i = 0; i<receivers.length; i++) {
                        for (int j = 0; j<receivers[i].length; j++) {
                            nextRec = (ProcessReceiver)receivers[i][j];
                            nextRec.setPause(true);
                            _pausedReceivers.insertFirst(receivers[i][j]);
                        }
		    }
		}
	    }

            // If this director is controlling a CompositeActor with
            // output ports, need to set the simulationFinished flag
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

            // Now wake up all the receivers.
            (new NotifyThread(_pausedReceivers)).start();

            return;
	} finally {
	    workspace().doneReading();
	}
    }

    /** Resumes the simulation. If the simulation is not paused do nothing.
     */
    public synchronized void setResumeRequested() {
        if (!_pauseRequested) {
            return;
        }

	Enumeration receivers = _pausedReceivers.elements();
	while (receivers.hasMoreElements()) {
	    ProcessReceiver rec = (ProcessReceiver)receivers.nextElement();
            rec.setPause(false);
	}

        // Now wake up all the receivers.
        (new NotifyThread(_pausedReceivers)).start();

        _pausedReceivers.clear();
        _actorsPaused = 0;
        _pauseRequested = false;
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
            // output ports, need to set the simulationFinished flag
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

    // Checks for deadlock.
    // In the base class implementation it returns true only if there are
    // no active processes.
    protected synchronized void _checkForDeadlock() {
        if (_actorsActive == 0) {
            _deadlock = true;
            notifyAll();
        }
        return;

    }

    // Check if all threads are either blocked or paused.
    protected synchronized void _checkForPause() {
	System.out.println("_checkForPause: No default implementation, " +
                "should be overridden in derived classes.");
       	return;
    }


    // Handle deadlock. In the base class implementation this returns true
    // on detection of a deadlock.
    // @return true for termination.
    // @exception IllegalActionException If a derived class throws it.
    protected boolean _handleDeadlock()
	    throws IllegalActionException {
        Workspace worksp = workspace();
	synchronized (this) {
	    while (!_deadlock) {
		worksp.wait(this);
	    }
	}
        _notdone = false;
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected long _actorsActive = 0;
    protected long _actorsPaused = 0;

    protected boolean _pauseRequested = false;
    protected boolean _paused = false;

    protected LinkedList _pausedReceivers = new LinkedList();

    // The threads under started by this director.
    protected LinkedList _threadList = new LinkedList();


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _deadlock = false;
    private boolean _notdone = true;
}










