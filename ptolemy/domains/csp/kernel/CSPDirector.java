/* A CSPDirector governs the execution of a CompositeActor with CSP semantics.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;

import collections.LinkedList;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CSPDirector
/**
CSPDirector governs the execution of a CompositeActor with the semantics 
of the Communicating Sequential Processes (CSP) domain.
<p>
In the CSP domain, the director creates a thread for executing each
actor under its control. Each actor corresponds to a
process in the model. The threads are created in the initialize
method and started in the prefire method.  After the thread for an actor
is started it is <i>active</i> until the thread finishes. While the
process is active, it can also be <i>blocked</i> or <i>delayed</i>, but
not both. A process is blocked if it is trying to communicate but
the the process with which it is trying to communicate is not
ready to do so yet. A process is delayed if it is waiting for
time to advance, or if it is waiting for a deadlock to occur.
<p>
The director is responsible for handling deadlocks, both real
and timed.  It is also responsible for carrying out any requests for
changes to the topology, that have been made when a deadlock occurs.
It maintains counts of the number of active
processes, the number of blocked processes, and the number of
delayed processes. <i>Deadlock</i> occurs when the number of blocked processes
plus the number of delayed processes equals the number of active processes.
<i>Time deadlock</i> occurs if at least one of the active processes
is delayed. <i>Real deadlock</i> occurs if all of the active processes
under the control of this director are blocked trying to communicate.
The fire method controls and responds to deadlocks and carries out
changes to the topology when it is appropriate.
<p>
If real deadlock occurs, the fire method returns. If there are no
levels above this level in the hierarchy then this marks the end
of execution of the model. The model execution is terminated by setting 
a flag in every receiver contained in actors controlled by this director. 
When a process tries to send or receive from a receiver with the terminated
flag set, a TerminateProcessException is thrown which causes the
actors execution thread to terminate.
<p>
Time is controlled by the director. Each process can delay for some
delta time, and it will continue when the director has advanced time 
by that length of time from the current time. A process is delayed by 
calling delay(double) method. The director <i>advances</i> time each 
occasion a time deadlock occurs and no changes to the topology  are 
pending. If a process specifies zero delay, then the process 
continues immediately. A process may delay itself until the next 
time deadlock occurs by calling waitForDeadlock(). Then the next 
occasion time deadlock occurs, the director wakes up any processes 
waiting for deadlock, and does not advance the current time. Otherwise 
the current model time is increased as well as being advanced.  By default 
the model of computation used in the CSP domain is timed. To use CSP 
without a notion of time, do not use the delay(double) method in any process.
<p>
The execution of the model may be paused by calling pause() which
will cause each process to pause the next time it tries to communicate 
or delay itself. The pause() method only returns when the execution of 
the model is paused. A paused model may be resumed by calling resume(). These 
methods only effect the progress of executing the model; they do not have any 
effect on the results of executing the model. The execution of the model 
may also be terminated abruptly by calling the terminate() method directly. 
This may lead to inconsistent state so any results generated after 
it should be ignored.
<p>
Changes to the topology can occur each occasion deadlock, real or time, is
reached. The director carries out any changes that have been queued
with it. Note that the result of the topology changes may remove the
deadlock that caused the changes to be carried out.
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.actor.Director
*/
public class CSPDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public CSPDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public CSPDirector(String name) {
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
    public CSPDirector(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new
     *  object is <i>not</i> added to the directory of that
     *  workspace (you must do this yourself if you want it there).
     *  The result is a new director with no container, no pending
     *  changes to the topology, current time is 0.0, and no actors
     *  are delayed or blocked.
     *  <p>
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new CSPDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        CSPDirector newobj = (CSPDirector)super.clone(ws);
        newobj._actorsBlocked = 0;
	newobj._actorsDelayed = 0;
        newobj._delayedActorList = new LinkedList();
        newobj._topologyChangesPending = false;
        return newobj;
    }

    /** Handles deadlocks, both real and timed, and changes to the
     *  topology. It returns when a real deadlock (all processes blocked
     *  trying to communicate) occurs.
     */
    public synchronized void fire() {
        // Check if still deadlocked after transferring tokens
        // from ports of composite actor to internal actors.
	boolean continueExec = true;
	while (continueExec) {
	    while (!_checkForDeadlock()) {
	        System.out.println("fire() waiting for deadlock.");
	        workspace().wait(this);
	    }
	    System.out.println("fire() handling deadlock.");
	    continueExec = !_handleDeadlock();
	}
	// something may go here e.g. notify GUI
    }

    /** Returns the current model time. 
     *  Note: this method may disappear if time is implemented in 
     *  super classes.
     *  @return The current model time.
     */
    public double getCurrentTime() {
        return _currentTime;
    }

    /** Return a new CSPReceiver compatible with this director.
     *  In the CSP domain, we use CSPReceivers.
     *  @return A new CSPReceiver.
     */
    public Receiver newReceiver() {
        return new CSPReceiver();
    }

    /** Return false to indicate that the iteration is over. Real
     *   deadlock must have occurred.
     *  <P>
     *  NOTE: when considering composing CSP with other domains, this method
     *  will need to be changed, together with prefire.
     *  <P>
     *  @return false indicating the iteration is over.
     */
    public boolean postfire() {
        return false;
    }

    /** Queue a topology change request. This sets a flag so that
     *  the next occasion deadlock is reached the changes
     *  to the topology are made.
     *  @param req The topology change being queued.
     */
    public synchronized void
            queueTopologyChangeRequest(TopologyChangeRequest req) {
        _topologyChangesPending = true;
        super.queueTopologyChangeRequest(req);
    }

    /** Set the current model time. It is intended for use when composing 
     *  CSP with other timed domains.
     *  <P>
     *  This method should only be called when no processes are delayed, as 
     *  the director stores the model time at which to resume them. If 
     *  the current model time changed while one or more processes are 
     *  delayed, then the state of the director would be undefined as 
     *  the resumption time of the delayed processes would not be 
     *  comparable with the new model time.
     *  <P>
     *  @exception IllegalActionException If one or more processes
     *   are delayed.
     *  @param newTime The new current model time.
     */
    public synchronized void setCurrentTime(double newTime)
            throws IllegalActionException {
        if (_actorsDelayed != 0) {
            throw new IllegalActionException("CSPDirector.setCurrentTime() " +
                    "can only be called when no processes are delayed.");
        }
        _currentTime = newTime;
    }

    /** Finish executing the model. A flag is set in all the receivers and
     *  in the director, which causes each process to terminate the
     *  next occasion it tries to communicate or delay itself.
     *  <p>
     *  The model finishes executing when real deadlock occurs and this
     *  director is controlling the top-level composite actor. It can
     *  also happen when the desired number of iterations have passed
     *  one level up or when the user decides to finish executing the model.
     *  <p>
     *  Note that the wrapup() methods are not directly invoked on the actors
     *  under control of this director as each actor is executed by a
     *  separate thread.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @exception IllegalActionException If a method accessing the topology
     *   throws it.
     */
    public void wrapup() throws IllegalActionException {
        System.out.println(Thread.currentThread().getName() +
                ": CSPDirector: about to end the model");
        if ((_actorsDelayed !=0) || _topologyChangesPending || 
                (_getPausedActorsCount() != 0)) {
            /*throw new InvalidStateException( "CSPDirector wrapping up " +
                    "when there are actors delayed or paused, or when " +
                    "topology changes are pending.");*/
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Increase the count of blocked processes and check for deadlock.
     */
    protected synchronized void _actorBlocked() {
        _actorsBlocked++;
        if (_checkForDeadlock()) {
	    notifyAll();
	}
    }

    /** Called by a CSPActor when it wants to delay. When the
     *  director has advanced time to "getCurrentTime() + delta", the process
     *  corresponding to the actor will continue. Note that actors 
     *  can only deal with delta time.
     *  <P>
     *  The method waitForDeadlock() in CSPActor calls this method 
     *  with a zero argument. Thus the process will continue the 
     *  next occasion time deadlock occurs. 
     *  <p>
     *  @param delta The length of time to delay the actor.
     *  @param actor The actor being delayed.
     *  @exception InvalidStateException If an actor is delayed for 
     *   negative time.
     */
    protected synchronized void _actorDelayed(double delta, CSPActor actor) 
        throws InvalidStateException {
        if (delta < 0.0) {
	    throw new InvalidStateException(((Nameable)actor).getName() +
		       ": delayed for negative time.");
	} else {
            // System.out.println("Delaying actor " + 
            // ((Nameable)actor).getName()+
            //    " for time " + delta + ".");
	    _actorsDelayed++;
	    // Enter the actor and the time to wake it up into the
	    // LinkedList of delayed actors.
	    _registerDelayedActor( (getCurrentTime() + delta), actor);
	    if (_checkForDeadlock()) {
	        notifyAll();
	    }
	    return;
	}
    }

    /** An actor has unblocked, decrease the count of blocked actors.
     */
    protected synchronized void _actorUnblocked() {
        _actorsBlocked--;
    }

    /** Returns true if all active processes are either blocked or 
     *  delayed, false otherwise.
     */
    protected synchronized boolean _checkForDeadlock() {
        System.out.println(Thread.currentThread().getName() +
                ": _checkForDeadlock: Active = " +
                _getActiveActorsCount() + ", blocked = " + _actorsBlocked +
                ", delayed = " + _actorsDelayed );
        if (_getActiveActorsCount() == (_actorsBlocked + _actorsDelayed)) {
            return true;
        } 
        return false;
    }

    /** Returns true if all active processes are either blocked, delayed or
     *  paused. If so, then all of the processes cannot make any progress 
     *  and the model has been paused. It returns false otherwise.
     */
    protected synchronized boolean _checkForPause() {
        if (_actorsBlocked + _getPausedActorsCount() + _actorsDelayed == 
	    _getActiveActorsCount()) {
            System.out.println("CSPDirector: model successfully paused!");
	    return true;
        }
        return false;
    }

    /** Determines how the director responds when a deadlock is
     *  detected. It is where nearly all the control for the
     *  model at this level in the hierarchy is located.
     *  <p>
     *  Deadlock occurs if the number of blocked and delayed processes
     *  equals the number of active processes. The method looks for
     *  three cases in the following order: are there topology changes
     *  waiting to happen, are there any processes delayed, are all the
     *  processes blocked trying to rendezvous.
     *  <p>
     *  If there are changes to the topology waiting to happen, they are
     *  performed and the execution of the model continues. 
     *  Note that the result of performing the topology changes may be 
     *  to remove the deadlock that had occurred.
     *  <p>
     *  If the number of delayed processes is greater than zero, then
     *  <i>time deadlock</i> has occurred. If one or more processes 
     *  are delayed waiting for deadlock to occur, then those processes 
     *  are resumed and time is not advanced. Otherwise time is advanced 
     *  and the earliest delayed process is resumed. Current time is 
     *  defined as the double value returned by getCurrentTime() 
     *  plus/minus 10e-10.
     *  <p>
     *  If all the processes are blocked, then <i>real deadlock</i> has
     *  occurred, and this method returns true. If there are no levels
     *  above this one in the hierarchy, then real deadlock marks the 
     *  end of executing the model.
     *  @return True if real deadlock occurred, false otherwise.
     */
    protected synchronized boolean _handleDeadlock() {
        try {
	  if (_topologyChangesPending) {
	      System.out.println("TOPOLOGY CHANGES PENDING!!");
	      _processTopologyRequests();
	      LinkedList newThreads = new LinkedList();
	      Enumeration newActors = _newActors();
	      while (newActors.hasMoreElements()) {
		  Actor actor = (Actor)newActors.nextElement();
		  System.out.println("Adding and starting new actor; " +
				     ((Nameable)actor).getName() + "\n");
		  actor.createReceivers();
		  actor.initialize();
		  String name = ((Nameable)actor).getName();
		  ProcessThread pnt = new ProcessThread(actor, this);
		  newThreads.insertFirst(pnt);
	      }
	      // Note we only start the threads after they have
	      // all had the receivers created.
	      Enumeration allThreads = newThreads.elements();
	      while (allThreads.hasMoreElements()) {
		  ProcessThread p = (ProcessThread)allThreads.nextElement();
		  p.start();
		  _addNewThread(p);
	      }
	      _topologyChangesPending = false;

          } else if (_actorsDelayed > 0) {
	      // Time deadlock.
	      double nextTime = _getNextTime();
	      System.out.println("\nCSPDirector: advancing time " +
				 "to: " + nextTime);
	      _currentTime = nextTime;
	      
	      // Now go through list of delayed actors
	      // and wake up those at this time
	      // Note that to deal with roundoff errors on doubles,
	      // any times within 0.000000001 are considered the same.
	      boolean done = false;
	      while (!done && _delayedActorList.size() > 0 ) {
	          DelayListLink val = (DelayListLink)_delayedActorList.first();
		  double tolerance = Math.pow(10, -10);
		  if (Math.abs(val._resumeTime - nextTime) < tolerance) {
		      _delayedActorList.removeFirst();
		      val._actor._continue();
		      System.out.println("\nresumeing actor at time: " +
					 val._resumeTime);
		      _actorsDelayed--;
		  } else {
		      done = true;
		  }
	      }
	  } else {
	      System.out.println("REAL DEADLOCK!!");
	      // Real deadlock. Return true so that the fire method can return.
	      return true;
	  }
          // Return false for topology changes and time deadlock.
	  return false;
        } catch (TopologyChangeFailedException ex ) {
            throw new InvalidStateException("CSPDirector: failed to " +
                    "complete topology change requests.");
        } catch (IllegalActionException ex ) {
            throw new InvalidStateException("CSPDirector: failed to " +
                    "create new receivers following a topology " +
                    "change request.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Used to keep track of when and for how long processes are delayed.
     *  @param actor The delayed actor.
     *  @param actorTime The time at which to resume the actor.
     */
    private void _registerDelayedActor(double actorTime, CSPActor actor) {
        DelayListLink newLink = new DelayListLink();
        newLink._resumeTime = actorTime;
        newLink._actor = actor;

        int size = _delayedActorList.size();

        boolean done = false;
        for (int i = 0; i < size; i++) {
            DelayListLink tmp = (DelayListLink)_delayedActorList.at(i);
            if (!done && (actorTime < tmp._resumeTime)) {
                _delayedActorList.insertAt(i, newLink);
                done = true;
            }
        }
        if (!done) {
            _delayedActorList.insertLast(newLink);
        }
    }

    /* Get the earliest time which an actor has been delayed to. This
     * should always be the top link on the list.
     */
    private double _getNextTime() {
        if (_delayedActorList.size() > 0) {
            return ((DelayListLink)_delayedActorList.first())._resumeTime;
        } else {
            throw new InvalidStateException("CSPDirector.getNextTime(): " +
                    " called in error.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Count of the number of processes blocked trying to rendezvous.
    private int _actorsBlocked = 0;

    // Count of the number of processes delayed until time
    // sufficiently advances.
    private int _actorsDelayed = 0;

    // The current model time.
    private double _currentTime = 0.0;

    // A sorted list of the times of delayed actors. The time the model
    // will next be advanced to is the time at the top of the list.
    private LinkedList _delayedActorList = new LinkedList();

    // Flag indicating that changes in the topology have been
    // registered with this director.
    private boolean _topologyChangesPending = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Class DelayedListLink
    // Keeps track of the actor that is delayed and the time
    // at which to resume it.
    private class DelayListLink {
        public double _resumeTime;
        public CSPActor _actor;
    }
}

