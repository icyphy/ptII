/* A CSPDirector governs the execution of a CSPCompositeActor.

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
import ptolemy.kernel.mutation.*;
import collections.LinkedList;
import ptolemy.data.*;

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
actor under its controlhandles deadlocks, both real
(simulation is terminated) and artificial (some process are delaying).
It maintains counts of the number of active processes, the number of
blocked processes, and the number of delayed processes. A deadlock is 
when the number of blocked processes plus the number delayed processes 
equals the number of active processes. The deadlock is artificial if 
at least one of the active processes is delayed or mutations are waiting to be performed. Otherwise the deadlock is real. 
<p>
There are two levels of iterations: those at the level of the composite 
actor controlled by this director, and those at the level of the 
composite actor containing this level of hierarchy. Each time deadlock 
is reached marks the end of an iteration. If the deadlock is real and 
there are no pending mutations, then this marks the end of en iteration 
one level up in the hierarchy. Otherwise the deadlock marks the end 
of an iteration at this level. Mutations are only perfromed at the 
start of each iteration at this level.
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
public class CSPDirector extends Director {

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
        try {
            workspace().getReadAccess();
            _actorsBlocked++;
            //System.out.println("Actor blocked, count is: " + _actorsBlocked);
            _handleDeadlock();
        } finally {
            workspace().doneReading();
        }
    }

    /** Update the count of active actor processes each time a new actor is
     *  fired up.
     */
    public synchronized void actorStarted() {
        // No need to synchronize this because the action is atomic
        // and synchronization would just ensure that no write action
        // is in progress.
        workspace().getReadAccess();
        _actorsAlive++;
        workspace().doneReading();
    }

    /** Checks for deadlock each time an Actor stops(finishes). Also updates
     *  count of number of actors still alive.
     */
    public synchronized void actorStopped() {
        try {
            workspace().getReadAccess();
            _actorsAlive--;
            System.out.println("actor stopped, still alive:" + _actorsAlive);
            _handleDeadlock();
        } finally {
            workspace().doneReading();
        }
    }

    /** A actor has unblocked, update count of blocked actors.
     */
    public synchronized void actorUnblocked() {
        // No need to synchronize this because the action is atomic
        // and synchronization would just ensure that no write action
        // is in progress.
        workspace().getReadAccess();
        _actorsBlocked--;
        //System.out.println("Actor unblocked, count is: " + _actorsBlocked);
        workspace().doneReading();
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
        _actorsAlive = 0;
	_actorsBlocked = 0;
	_actorsDelayed = 0;
        _simulationTerminated = false;
        return newobj;
    }

    /** Need to override the fire method of base director class so 
    *  that it doesn't call the fire methods on each of the contained 
    *  actors as these actors get executed in a separate thread.
    */
    public void fire() throws IllegalActionException {
        //System.out.println("Ha, ha!");
    }


    /** Create the execution threads that will control the execution 
     *  of each actor controlled by this director. This method should 
     *  be invoked once per execution.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be. Note that this method cannot be write synchronized
     *  as this would then preclude having csp in pn, pn in csp pn in pn etc...
     *  
     *  @exception IllegalActionException If the receivers could not 
     *   be created for any of the actors under the control of this director.
     */
    public synchronized void initialize() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement(); 
                actor.createReceivers();
                String name = ((NamedObj)actor).getName();
                PNThread pnt = new PNThread(actor, name);
                _threadlist.insertFirst(pnt);
            }
            Enumeration threads = _threadlist.elements();
            while (threads.hasMoreElements()) {
                PNThread pnt = (PNThread)threads.nextElement();
                System.out.println("Starting thread for: " +
                      pnt.getName());
                actorStarted();
                pnt.start();
            }
            System.out.println("CSPDirector: have started actor threads.");
        }
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
            while (!_simulationTerminated) {
                // a HORRIBLE hack, waits until all processes are
                // stopped to invoke their postfire methods.
                System.out.println("Postfire:Waiting for simulation to finish.");
                Thread.currentThread().sleep(2000);
            }
        } catch (InterruptedException ex) {
            System.out.println("Local cspDirector interrupted while waiting for simulation to finish");
        }
        System.out.println("CSPDirector: Postfire returning true.");
        return true;
    }

    /** FIXME!
    */
    public boolean prefire() {
        return true;
    }

    /** The action to terminate all actors under control of this local
     *  director because a real deadlock has occurred. It could also
     *  be called when a UI decides to terminate a simulation prematurely.
     *  <p>
     *  FIXME: this method is designed to be used with a local director.
     *  How should an executive director call the local director to make
     *  this happen?
     */
    public synchronized void terminateSimulation() {
        System.out.println("about to terminate simulation");
        try {
            workspace().getReadAccess();
            if (_simulationTerminated) {
                // simulation has already been terminated!
                return;
            }
            _simulationTerminated = true;

            CompositeActor cont = (CompositeActor)getContainer();
            Enumeration allMyActors = cont.deepGetEntities();

            while (allMyActors.hasMoreElements()) {
                try {
                    Actor actor = (Actor)allMyActors.nextElement();
                    Enumeration actorPorts = actor.inputPorts();
                    while (actorPorts.hasMoreElements()) {
                        IOPort port = (IOPort)actorPorts.nextElement();
                        // Terminating the ports and hence the star
                        Receiver[][] receivers = port.getReceivers();
                        for (int i=0; i<receivers.length; i++) {
                            for (int j=0; j<receivers[i].length; j++) {
                                CSPReceiver next = (CSPReceiver)receivers[i][j];
                                next.setSimulationTerminated();
                                System.out.println("synchronizing on receiver to kill");
                                // FIXME: need to add this below!
                                synchronized(next) {
                                    next.notifyAll();
                                }
                            }
                        }
                    }
                    // FIXME: is terminating all receivers on non-atomic 
                    // output ports enough?
                    if (!((ComponentEntity)actor).isAtomic()) {
                        actorPorts = actor.outputPorts();
                        while (actorPorts.hasMoreElements()) {
                            IOPort port = (IOPort)actorPorts.nextElement();
                            // Terminating the ports and hence the star
                            Receiver[][] receivers = port.getReceivers();
                            for (int i=0; i<receivers.length; i++) {
                                for (int j=0; j<receivers[i].length; j++) {
                                    ((CSPReceiver)receivers[i][j]).setSimulationTerminated();
                                    receivers[i][j].notifyAll();
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("CSPDirector: unable to terminate all actors");
                    System.out.println("because: " + ex.getClass().getName() + ", message: " + ex.getMessage());
                            //FIXME: should not catch general exception
                }
            }
        } finally {
            workspace().doneReading();
        }
    }

    /** FIXME! Needed to mask trhe wrapup method from Director.
    */
    public void wrapup() {
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** FIXME: this needs to be made more sophisticated. How to 
    *  handle delays, iterations, real/artificial deadlock, if real 
    *  then transfer outputs...
    */
    private synchronized void _handleDeadlock() {
        if (_simulationTerminated) {
            return;
        }
        //System.out.println("HandleDeadlock: alive = " + _actorsAlive + ", blocked = " + _actorsBlocked + ", delayed = " + _actorsDelayed);
        if (_actorsAlive == (_actorsBlocked + _actorsDelayed)) {
            // simulation has ended in deadlock, so terminate
            terminateSimulation();
        }
    }
     
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _actorsBlocked = 0;
    private int _actorsAlive = 0;
    private int _actorsDelayed = 0;

    // Set to true when the simulation is terminated
    private boolean _simulationTerminated = false;

    // The threads under started by this director.
    private LinkedList _threadlist = new LinkedList();
}
