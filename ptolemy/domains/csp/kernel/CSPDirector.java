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
        System.out.println(getName() + ": Actor blocked, count is: " +
                _actorsBlocked);
        _checkForDeadlock();
    }

    /** Called by a CSPActor when it wants to delay. This method 
     *  does not return until it is ok for the actor to proceed 
     *  i.e. time has advanced enough.
     *  Note that actors can only deal with delta time.
     *  <p>
     *  FIXME: what is here is my first pass thru time...
     *  @param delta The length of time to delay the actor.
     */
    public void actorDelayed(double delta) {
        double resumeTime = 0;
        synchronized(this) {
            _actorsDelayed++;
            resumeTime = getTime() + delta;
            _delayedUntilTime(resumeTime);
        }

        // FIXME: where to call checkForDeadlock()?
        // Delay the actor calling this method until appropriate time.
        try {
            synchronized(_delayLock) {
                _delayLock.wait();
                // Note that the director could decide to move to
                // next time instant without increasing real time!
                while (getTime() < resumeTime) {
                    _delayLock.wait();
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("CSPDirector.postfire: interrupted while" +
                    " waiting for iteration to finish.");
        }
        
        synchronized(this) {
            _actorsDelayed--;
        }    
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

    /** Need to override the fire method of base director class so 
    *  that it doesn't call the fire methods on each of the contained 
    *  actors as these actors get executed in a separate thread.
    */
    public void fire() throws IllegalActionException {
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
        try {
            synchronized(_iterationLock) {
                while (!_iterationFinished) {
                    // simulation is not yet finished so must wait.
                    _iterationLock.wait();
                }
                // FIXME: what other checks should go in here?
             }
        } catch (InterruptedException ex) {
            System.out.println("CSPDirector.postfire: interrupted while" +
                    " waiting for iteration to finish.");
        }
        // FIXME: do we always only run for one iteration?;
        _iterationFinished = false;
        
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
    ////                         private methods                   ////

    /* Used to keep track of when are for how long processes are delayed.
     */
    private void _delayedUntilTime(double actorTime) {
        // FIXME: initial simple implementation, make more sophistictaed later.
        if ( (actorTime <= _getNextTime()) && (actorTime >= getTime()) ) {
            _nextTime= actorTime;
        }
    }

    /* FIXME: this will be more sophisticated later.
     */
    private double _getNextTime() {
        return _nextTime;
    }

    /** FIXME: Needs a lot of polishing...
    */
    protected synchronized void _checkForDeadlock() {
        if (_simulationFinished) {
            return;
        }
        System.out.println("_checkForDeadlock: Active = " + _actorsActive + 
                ", blocked = " + _actorsBlocked);
        if (_actorsActive == (_actorsBlocked + _actorsDelayed)) {
            if (_actorsDelayed > 0) {
                // Artificial deadlock.
                double nextTime = _getNextTime();
                setTime(nextTime);
                synchronized(_delayLock) {
                    _delayLock.notifyAll();
                }
            } else if (false) {
                // FIXME: handle immediate mutations here.
            } else {
                System.out.println("REAL DEADLOCK!!");
                // Real deadlock. This marks the end of an iteration so 
                // allow the postfire method to continue by issueing 
                // a notifyAll of the delayLock.
                // FIXME: this could be todied up a bit.
                synchronized(_iterationLock) {
                    _iterationFinished = true;
                    _iterationLock.notifyAll();
                }
                System.out.println("returning from _checkForDeadlock");
            }
        }
    }
     
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _actorsBlocked = 0;
    private int _actorsDelayed = 0;

    private boolean _iterationFinished = false;

    // the current time of this simulation.
    private double _time = 0;

    // Lock object used to delay actors in time.
    private Object _delayLock = new Object();

    // Lock object used to mark the end of an iteration one level 
    // up in the hierarchy.elay actors in time.
    private Object _iterationLock = new Object();

    // Store the next time that simulation time will get advanced to.
    private double _nextTime;

    // Set to true when the simulation is terminated
    private boolean _simulationFinished = false;
}
