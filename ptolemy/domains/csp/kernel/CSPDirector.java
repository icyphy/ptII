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
Communicating Seqential Process (CSP) semantics. Thus to make 
a composite actor obey CSP semantics, call the method setDirector() 
with an instance of this class.
<p>
In the CSP domain, the director creates a thread for executing each 
actor under its control. The director is responsible for handling 
deadlocks, both real  and artificial.
It maintains counts of the number of active processes, the number of
blocked processes, and the number of delayed processes. A deadlock is 
when the number of blocked processes plus the number delayed processes 
equals the number of active processes. The deadlock is artificial if 
at least one of the active processes is delayed or mutations are waiting 
to be performed. Otherwise the deadlock is real in that all actors 
under the control of this director are blocked trying to communicate.
<p>
There are two levels of iterations: those at the level of the composite 
actor controlled by this director, and those at the level of the 
composite actor containing this level of hierarchy. Each time deadlock 
is reached marks the end of an iteration. If the deadlock is real and 
there are no pending mutations, then this marks the end of an iteration 
one level up in the hierarchy. Otherwise the deadlock marks the end 
of an iteration at this level. Mutations are only perfromed at the 
start of each iteration at this level. FIXME: two types of mutations.
Also after a pause.
<p>
If this director is controlling the top-level composite actor, and 
real deadlock is encountered(an iteration one level up), then terminate 
the simulation. The simulation is terminated by setting a flag in every 
receiver contained in actors controlled by this director. When a 
process tries to send or receive from a receiver with the terminated 
flag set, a TerminateProcessException is thrown which causes the 
actors execution thread to terminate. The simulation may also be 
terminated directly by calling the terminateSimulation() method 
directly.
<p>
If this director is not controlling the top-level composite actor, and 
real deadlock is encountered(an iteration one level up), then set a flag 
which allows the postfire() method of this director to return. This 
returns control to whatever called the execution commands on the 
composite actor controlled by this director. Control is not returned 
immediately as this would cause one iteration at this level to 
proceed in parallel to the thread that contained the calls to the 
execution commands.
<p>
more compositionality discussion...
<p>
Time....
<p>
mutations...
<p>
FIXME: want to be able to turn time on/off for a simulation. Just 
ignore delay() calls, i.e have them return immediately.
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


    /** Checks for deadlock each time an Actor blocks. Also updates
     *  count of number of blocked actors.
     */
    public synchronized void actorBlocked() {
        _actorsBlocked++;
        _checkForDeadlock();
    }

    /** Called by a CSPActor when it wants to delay. FIXME: This method 
     *  does not return until it is ok for the actor to proceed 
     *  i.e. time has advanced enough.
     *  Note that actors can only deal with delta time.
     *  <p>
     *  If delta is negative treat as delaying until next time 
     *  artificial deadlock is reached.
     *  @param delta The length of time to delay the actor.
     */
    public synchronized void actorDelayed(double delta, CSPActor actor) {
        System.out.println(Thread.currentThread().getName() + 
                ": delaying for " + delta);
        double resumeTime = 0.0;
        _actorsDelayed++;
        resumeTime = getTime() + delta;
        // Enter the actor and the time to wake it up into the
        // LinkedList of delayed actors..
        _delayedUntilTime(resumeTime, actor);

        System.out.println("actorsDelayed: " + _actorsDelayed + ", delayList size: " + _delayedActorList.size());
        
        _checkForDeadlock();
        return;
    }
    
    /** A actor has unblocked, update count of blocked actors.
     */
    public synchronized void actorUnblocked() {     
        _actorsBlocked--;
        System.out.println(getName() + ": Actor unblocked, count is: " + 
                _actorsBlocked);
    }

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  and no mutation listeners.
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
        newobj._simulationFinished = false;
        return newobj;
    }

    /** Handles artificial deadlocks and mutations. Returns when a 
     *  real deadlock(all processes blocked trying to communicate) 
     *  occurs.
     *  FIXME: IllegalActionException needed?
     */
    public void fire() throws IllegalActionException {
        // This method will not return until real deadlock occurs.
        _handleDeadlock();
    }

    /** The current simulation time.
     *  @return The current simulation time.
     */
    public double getTime() {
        return _time;
    }
  
    /** Return a new CSPReceiver compatible with this director.
     *  In the CSP domain, we use CSPReceivers.
     *  @return A new CSPReceiver.
     */
    public Receiver newReceiver() {
        return new CSPReceiver();
    }

    public boolean postfire() {
        return false;
    }

    /** FIXME!
    */
    public boolean prefire() throws IllegalActionException  {
        // FIXME: should call checkFordeadlock here to see if still 
        // deadlocked after transfering tokens from ports of composite 
        // actor to internal actors.
        return super.prefire();
    }

    /** Request the director to process topology change requests
     *  immediately. This should be called by clients that have
     *  queued a request and require that the request be processed
     *  immediately. The method does not return until requests
     *  have been processed.
     *  <p> 
     *  @exception TopologyChangeFailedException If the mutations could not 
     *   be completed.
     */
    public void processTopologyRequests() throws TopologyChangeFailedException{
        try {
            setPauseRequested();
            synchronized(this) {
                _urgentMutations = true;
                while (!_paused) {
                    this.wait();
                }
            }
            // simulation is paused.
            // Do mutations.
            // Resume.
            setResumeRequested();
        } catch (InterruptedException ex) {
            // FIXME: what goes here?
        } catch (IllegalActionException ex) {
            // FIXME: throw new TopologyChangeFailedException();
        }
    }
            
    /** Set the current simulation time.
     *  @param newTime The new current simulation time.
     */
    public void setTime(double newTime) {
        _time = newTime;
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
     *  @exception IllegalActionExcepion if a method accessing the topology
     *   throws it.
     */
    public void wrapup() throws IllegalActionException {
        System.out.println(Thread.currentThread().getName() +
                ": CSPDirector: about to end the simulation");
        synchronized(this) {
            _simulationFinished = true;
        }
        super.wrapup();      
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check if all active processes are either blocked or delayed.
     *  If so, then wake up the director so that it can handle 
     *  the deadlock.
     */
    protected synchronized void _checkForDeadlock() {
        System.out.println("_checkForDeadlock: Active = " + 
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
  
    /** The heart of the director that controls what happens 
     *  when a deadlock is detected. It is where nearly all the control for 
     *  the simulation at this level in the hierarchy is located.
     *  <p>
     *  Deadlock occurs if the number of blocked and delayed process 
     *  equals the number of activeprocesses. If the number of delayed 
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
     *  FIXME: Needs a lot of polishing...
     *  FIXME: possible for an actor to be both blocked and paused!
    */
    protected synchronized boolean _handleDeadlock() {
        while (true) {
            try {
                this.wait();
                if (_actorsActive == (_actorsBlocked + _actorsDelayed)) {
                    if (_mutationsPending) {
                        // FIXME: fill in later!
                        System.out.println("ARTIFICIAL MUTATION DEADLOCK!!");
                    } else if (_actorsDelayed > 0) {
                        // Artificial deadlock.
                        System.out.println("ARTIFICIAL TIME DEADLOCK!!");
                        double nextTime = _getNextTime();
                        System.out.println("\nCSPDirector: advancing time " + 
                                "to: " + nextTime);
                        setTime(nextTime);
                            
                        // Now go through list of delayed actors 
                        // and wake up those at this time
                        // FIXME: what about round off errors leading 
                        // to errors?
                        boolean done = false;
                        while (!done && _delayedActorList.size() > 0 ) {
                            DelayListLink val = 
                                (DelayListLink)_delayedActorList.first();
                            if (val._resumeTime == nextTime) {
                                _delayedActorList.removeFirst();
                                val._actor._delayed = false;
                                Object lock = val._actor._getInternalLock();
                                // FIXME: not efficent!
                                (new NotifyThread(lock)).start();
                                System.out.println("\nresumeing actor at " + 
                                        "time: " + val._resumeTime);
                                _actorsDelayed--;
                            } else {
                                done = true;
                            }
                        }
                                        
                        System.out.println("All delayed actors woken up.");
                    } else {
                        System.out.println("REAL DEADLOCK!!");
                        // Real deadlock. This marks the end of an 
                        // iteration so return.
                        System.out.println("_handleDeadlock returning...");
                        return true;
                    }
                    System.out.println("_handleDeadlock waiting again.");
                }
            } catch (InterruptedException ex ) {
                throw new InvalidStateException("CSPDirector interruped " +
                        "while waiting for deadlock to occur or " +
                        "resolving an artificial deadlock.");
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Used to keep track of when are for how long processes are delayed.
     * FIXME: this method is horribly inefficent, change data structure!
     */
    private void _delayedUntilTime(double actorTime, CSPActor actor) {
        // FIXME: initial simple implementation, make more sophisticted later.
        DelayListLink newLink = new DelayListLink();
        newLink._resumeTime = actorTime;
        newLink._actor = actor;

        int size = _delayedActorList.size();
      
        boolean done = false;
        for (int i = 0; i < size; i++) {
            DelayListLink tmp = (DelayListLink)_delayedActorList.at(i);
            if (!done && actorTime < tmp._resumeTime) {
                _delayedActorList.insertAt(i, newLink);
                //System.out.println("Inserting at " + actorTime);
                done = true;
            }
        }
        if (!done) {
            _delayedActorList.insertLast(newLink);
            //System.out.println("Inserting at " + actorTime);
        }
        
        // For debugging
        System.out.println("\n");
        Enumeration enum = _delayedActorList.elements();
        while (enum.hasMoreElements()) {
            DelayListLink val = (DelayListLink)enum.nextElement();
            System.out.println(val._actor.getName() + 
                    ": Delayyed actor at time: " + val._resumeTime);
        }
    }

    /* Get the earliest time which an actor has been delayed to. This 
     * should always be the top link on the list.
     */
    private double _getNextTime() {
        double val = 0.0;
        if (_delayedActorList.size() > 0) {
            return ((DelayListLink)_delayedActorList.first())._resumeTime;
        } else {
            System.out.println("getNextTime called in error.");
        }
        return val;
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _actorsBlocked = 0;
    private int _actorsDelayed = 0;

    // the current time of this simulation.
    private double _time = 0;

    // Lock object used to notify handleDeadlock a deadlock has occured.
    private Object _deadlock = new Object();

    // A sorted list of the times of delayed actors. The time the simulation 
    // will next be advanced to is the time at the top of the list.
    private LinkedList _delayedActorList = new LinkedList();

    // Flag indicating that mutations have been registered with this director.
    private boolean _mutationsPending = false;

    // Flag indicating whether time was advanced. Note that thsi amy 
    // include "advancing" time to the same time as before!
    private boolean _timeAdvanced = false;

    // Set to true when the simulation is terminated
    private boolean _simulationFinished = false;
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
