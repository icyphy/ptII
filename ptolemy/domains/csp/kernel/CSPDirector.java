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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating none

*/

package ptolemy.domains.csp.kernel;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import collections.LinkedList;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CSPDirector
/**
A CSPDirector governs the execution of a CompositeActor with 
Communicating Seqential Processes (CSP) semantics. Thus to make 
a composite actor obey CSP semantics, call the method setDirector() 
with an instance of this class.
<p>
In the CSP domain, the director creates a thread for executing each 
actor under its control. Each actor corresponds to a 
process in the model. The threads are created in the directors initialize 
method and started in its prefire method.  After the thread for an actor 
is started it is <i>active</i> until the thread finishes. While the 
process is active, it can also be <i>blocked</i> or <i>delayed</i>, but
not both. A process is blocked if it is trying to communicate but 
the the process with which it is trying to communicate is not 
ready to do so yet. A process is delayed if it is waiting for 
time to advance. It the simulation is untimed this cannot happen. 
<p>
The director is responsible for handling deadlocks, both real 
and artificial. It maintains counts of the number of active 
processes, the number of blocked processes, and the number of 
delayed processes. A deadlock is when the number of blocked processes 
plus the number delayed processes equals the number of active processes. 
The deadlock is artificial if at least one of the active processes 
is delayed or mutations are waiting to be performed. Otherwise the 
deadlock is real in that all actors under the control of this 
director are blocked trying to communicate. The fire method 
controls and responds to artificial deadlocks and returns when a 
real deadlock occurs.
<p>
If real deadlock occurs, the fire method returns and is the end 
of one iteration one level up in the hierarchy. If there are no 
levels above this level in the hierarchy then this marks the end 
of the simulation. The simulation is terminated by setting a flag in every 
receiver contained in actors controlled by this director. When a 
process tries to send or receive from a receiver with the terminated 
flag set, a TerminateProcessException is thrown which causes the 
actors execution thread to terminate.
<p>
If artificial deadlock occurs and mutations are waiting to be 
perfromed, then the changes are made to the topology of the simulation 
and it is allowed to proceed(if it can). If no mutations are pending, 
then at least one process must be delayed, so time is advanced and 
one or more of the delayed processes will continue.
<p>
Time is controlled by the director. Each process can delay for some 
delta time, and it will continue when the director has sufficently 
advanced time. The director <i>advances</i> time each occasion an 
artificial deadlock occurs and no mutations are pending. Processes 
may specify zero delay, in which case they 
delay until the next occasion time is advanced. Note that time can 
be advanced to the current time. This happens if one of the 
delayed actors delayed with a delta delay of zero. Otherwise the 
simulation time is increased as well as being advanced. Time can be 
turned on or off by calling setUntimed(). By default the simulation 
uses time. 
<p>
The simulation may be paused by calling setPauseRequested() which 
will cause each process to pause the next time it tries to communicate.
Note that the a pause can only be requested and may not happen 
immediately or even at all(if there are no communications taking 
place).To resume a paused simulation call setResumeRequested(). The 
simulation may also be terminated abruptly by calling the 
terminate() method directly. This may led to inconsistant state 
so any results outputted after it should be ignored. <p>
<p>
<p>
mutations...
<p>
more compositionality discussion...
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.actor.Director;

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

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  current time is 0.0, and no actors are delayed or blocked.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new CSPDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        CSPDirector newobj = (CSPDirector)super.clone(ws);
        newobj._actorsBlocked = 0;
	newobj._actorsDelayed = 0;
        newobj._currentTime = 0.0;
        newobj._delayedActorList = new LinkedList();
        newobj._simulationUntimed = false;
        newobj._topologyChangesPending = false;
        return newobj;
    }

    /** Handles artificial deadlocks and mutations. Returns when a 
     *  real deadlock(all processes blocked trying to communicate) 
     *  occurs.
     */
    public synchronized void fire() {
        // FIXME: should call checkFordeadlock here to see if still 
        // deadlocked after transfering tokens from ports of composite 
        // actor to internal actors.
        // This method will not return until real deadlock occurs.
        while(!_handleDeadlock()) {
            // siomething may go here e.g. notify GUI
        };
    }

    /** The current simulation time.
     *  @return The current simulation time.
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
     *   deadlock must have occured.
     *  @return false indicating the iteration is over.
     */
    public boolean postfire() {
        return false;
    }

    /** Queue a topology change request. This sets a flag so that 
     *  the next time artificial deadlock is reached the changes 
     *  to the topology are made.
     *  @param req the topology change being queued.
     */
    public void queueTopologyChangeRequest(TopologyChangeRequest req) {
        System.out.println("queuning topology change in CSPDirector.");
        synchronized(this) {
            _topologyChangesPending = true;
            super.queueTopologyChangeRequest(req);
            System.out.println("queuedtopology change in CSPDirector.");
        }
    }
            
    /** Set the current simulation time. This method should only be 
     *  called when no processes are delayed. It is intended for 
     *  use when composing CSP with other timed domains.
     *  @exception IllegalActionException If one or more processes
     *   are delayed.
     *  @param newTime The new current simulation time.
     */
    public synchronized void setCurrentTime(double newTime) 
          {
        if (_actorsDelayed != 0) {
            //throw new IllegalActionException("CSPDirector.setTime() can " +
            //      "only be called when no processes are delayed.");
        }
        _currentTime = newTime;
    }

    /** Call this method with a true argument to turn off time.
     *  @param value Boolean seting whether or not the simulation 
     *   uses time.
     */
    public synchronized void setUntimed(boolean value) {
        _simulationUntimed = value;
    }

    /** End the simulation. A flag is set in all the receivers, and 
     *  in the director, which causes each process to terminate the 
     *  next time it reaches a synchronization or delay point.
     *  <p>
     *  The simulation is ended when real deadlock occurs and this 
     *  director is controlling the top-level composite actor. It can 
     *  also happen when the desired number of iterations have passed 
     *  one level up or when the user decides to finish the simulation.
     *  <p>
     *  Note that the wrapup methods are not invoked on the actors 
     *  under control of this director as each actor is executed by a 
     *  seperate thread.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @exception IllegalActionException if a method accessing the topology
     *   throws it.
     */
    public synchronized void wrapup() throws IllegalActionException {
        System.out.println(Thread.currentThread().getName() +
                ": CSPDirector: about to end the simulation");
        if ((_actorsDelayed !=0) || _topologyChangesPending 
                || (_actorsPaused != 0)){
            throw new InvalidStateException( "CSPDirector wrapping up " +
                    "when there are actors delayeed or paused, or when " +
                    "topology changes are pending.");
        }
        super.wrapup();      
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
  
    /** Increase the count of blocked processes and check for deadlock.
     */
    protected synchronized void _actorBlocked() {
        _actorsBlocked++;
        _checkForDeadlock();
    }

    /** Called by a CSPActor when it wants to delay. When the 
     *  director has sufficently advanced time the process 
     *  corresponding to the actor will continue.
     *  Note that actors can only deal with delta time.
     *  <p>
     *  If delta is negative treat as delaying until next time 
     *  artificial deadlock is reached.
     *  @param delta The length of time to delay the actor.
     *  @param actor The actor being delayed.
     */
    protected synchronized void _actorDelayed(double delta, CSPActor actor) {
        if (_simulationUntimed) {
            actor._continue();
            return;
        }
        System.out.println(Thread.currentThread().getName() + 
                ": delaying for " + delta);
        double resumeTime = 0.0;
        _actorsDelayed++;
        resumeTime = getCurrentTime() + delta;
        // Enter the actor and the time to wake it up into the
        // LinkedList of delayed actors..
        _registerDelayedActor(resumeTime, actor);

        _checkForDeadlock();
        return;
    }
    
    /** A actor has unblocked, decrease the count of blocked actors.
     */
    protected synchronized void _actorUnblocked() {     
        _actorsBlocked--;
    }

    /** Check if all active processes are either blocked or delayed.
     *  If so, then wake up the director so that it can handle 
     *  the deadlock.
     */
    protected synchronized void _checkForDeadlock() {
        System.out.println(Thread.currentThread().getName() +
                ": _checkForDeadlock: Active = " + 
                _actorsActive + ", blocked = " + _actorsBlocked + 
                ", delayed = " + _actorsDelayed );
        if (_actorsActive == (_actorsBlocked + _actorsDelayed)) {
            this.notifyAll();
        }
    }

    /** Check if all active processes are either blocked, delayed or
     *  paused. If so, then none of the processes are running so the 
     *  simulation has been paused. This allows urgent mutations to occur.
     */
    protected synchronized void _checkForPause() {
        if (_actorsBlocked + _actorsPaused + _actorsDelayed == _actorsActive) {
            _paused = true;
            System.out.println("CSPDirector: simulation successfully paused!");
            notifyAll();
        }
        return;
    }
  
    /** The heart of the director that responds
     *  when a deadlock is detected. It is where nearly all the control for 
     *  the simulation at this level in the hierarchy is located.
     *  <p>
     *  Deadlock occurs if the number of blocked and delayed process 
     *  equals the number of active processes. If the number of delayed 
     *  processes is greater than zero, or there are mutations waiting 
     *  to happen, then the deadlock is artificial. In this case this
     *  method determines what needs to be done, does it and waits 
     *  until the next deadlock. Otherwaise the 
     *  deadlock is real and this method returns.
     *  If this director is not controlling the top level 
     *  composite actor, then real deadlock marks the end of one 
     *  iteration one level up in the hierarchy. If there is no level 
     *  above this one, then real deadlock marks the end of the simulation.
     *  <p>
     *  When artificial deadlock occurs and there are no pending 
     *  mutations, then time is advanced and at least one of the 
     *  delayed actors will wake up and continue. Note that time can 
     *  be advanced to the current time. This happens if one of the 
     *  delayed actors delayed with a delta delay of zero. Otherwise the 
     *  simulation time is increased as well as being advanced.
     *  <p>
     *  Current time is defined as the double value returned by 
     *  getCurrentTime plus/minus 10e-10.
     *  @param True if real deadlock occured, false otherwise.
     */
    protected synchronized boolean _handleDeadlock() {
        try {          
            if (_actorsActive == (_actorsBlocked + _actorsDelayed)) {
                if (_topologyChangesPending) {
                    System.out.println("ARTIFICIAL MUTATION DEADLOCK!!");
                    _processTopologyRequests();
                    LinkedList newThreads = new LinkedList();
                    Enumeration newActors = _newActors();
                    while (newActors.hasMoreElements()) {
                        Actor actor = (Actor)newActors.nextElement(); 
                        System.out.println("Adding and starting new actor; " +
                                ((Nameable)actor).getName() + "\n");
                        //increaseActiveCount should be called before 
                        // createReceivers as that might increase the 
                        // count of processes blocked and deadlocks 
                        // might get detected even if there are no 
                        //deadlocks
                        increaseActiveCount();
                        actor.createReceivers();
                        actor.initialize();
                        String name = ((Nameable)actor).getName();
                        ProcessThread pnt = new ProcessThread(actor, 
                                this, name);
                        newThreads.insertFirst(pnt);
                    }
                    // Note we only start the threads after they have 
                    // all had the receivers created.
                    Enumeration allThreads = newThreads.elements();
                    while (allThreads.hasMoreElements()) {
                            ProcessThread p = 
                                (ProcessThread)allThreads.nextElement(); 
                            p.start();
                           
                            _threadList.insertFirst(p);
                    }
                    _topologyChangesPending = false;
                    _checkForDeadlock();
                    System.out.println("Finished dealing with " +
                            "mutation deadlock.");
                } else if (_actorsDelayed > 0) {
                    // Artificial deadlock.
                    System.out.println("ARTIFICIAL TIME DEADLOCK!!");
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
                        DelayListLink val = 
                            (DelayListLink)_delayedActorList.first();
                        double tolerance = Math.pow(10, -10);
                        if (Math.abs(val._resumeTime - nextTime) < tolerance) {
                            _delayedActorList.removeFirst();
                            val._actor._continue();
                            System.out.println("\nresumeing actor at " + 
                                    "time: " + val._resumeTime);
                            _actorsDelayed--;
                        } else {
                            done = true;
                        }
                    }
                } else {
                    System.out.println("REAL DEADLOCK!!");
                    // Real deadlock. This marks the end of an 
                    // iteration so return.
                    return true;
                }
            }
            System.out.println("\nhandleDeadlock waiting...");
            this.wait();
            return false;
        } catch (InterruptedException ex ) {
            throw new InvalidStateException("CSPDirector: interruped " +
                    "while waiting for deadlock to occur or " +
                    "resolving an artificial deadlock.");
        } catch (TopologyChangeFailedException ex ) {
            throw new InvalidStateException("CSPDirector: failed to " +
                    "complete topology change requests.");
        } catch (IllegalActionException ex ) {
            throw new InvalidStateException("CSPDirector: failed to " +
                    "create new receivers following a topology " +
                    "change request.");
        } catch (Exception ex) {
            System.out.println("arrrgggghhhh!!!" + ex.getClass().getName() + 
                    ": " + ex.getMessage());
        }
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Used to keep track of when and for how long processes are delayed.
     *  @param actor The delayed actor.
     *  @param actorTime The time at which to rsume the actor.
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
                //System.out.println("Inserting at " + actorTime);
                done = true;
            }
        }
        if (!done) {
            _delayedActorList.insertLast(newLink);
            //System.out.println("Inserting at " + actorTime);
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
    // sufficently advances.
    private int _actorsDelayed = 0;

    // A sorted list of the times of delayed actors. The time the simulation 
    // will next be advanced to is the time at the top of the list.
    private LinkedList _delayedActorList = new LinkedList();
    
    // Flag indicating that changes in the topology have been
    // registered with this director.
    private boolean _topologyChangesPending = false;

    // Flag indicating if the simulation is timed.
    private boolean _simulationUntimed = false;

    // The current time of this simulation.
    private double _currentTime = 0;

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
