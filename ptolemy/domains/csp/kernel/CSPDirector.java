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
import ptolemy.data.*;

import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CSPDirector
/**
A CSPDirector governs the execution of a CSPCompositeActor. It can serve 
either as a local director or as an executive director.  To make it a 
local director, use the setDirector() method of CSPCompositeActor. To make 
it an executive director, use the setExecutiveDirector() method of 
CSPCompositeActor. In both cases, the director will report the 
CSPCompositeActor as its container when queried by calling getContainer().
<p>
In the CSP domain, the executive director handles deadlocks, both real 
(simulation is terminated) and artificial (some process are delaying). 
<p>
It maintains counts of the number of active processes and the number of 
blocked processes(and eventually the number of delayed processes). When 
the number of blocked processes is equal to the number of active processes, if there are no pending mutations, terminate the simulation by setting flag 
in every receiver to that effect. 
The simulation is terminated by throwing a TerminateProcessException in 
each actor. The simulation may also be terminated directly by calling the 
terminateSimulation() method on the director.
<p>
When all the actors are blocked and there are mutations waiting to be 
performed, this is the end of an iteration. If the simulation is being 
run for more than one iteration, the prefire method in the Director 
performs any necessary mutations and wakes up any threads waiting for 
the mutations to be performed.
<p>
FIXME: what else could mark the end of an iteration?
FIXME: have not worried about mutations, time for now, do later..
FIXME: how to handle compositionality?
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

  
    /** Checks for deadlock each time a Actor blocks. Also updates 
     *  count of number of blocked actors.
     *  FIXME: need to put in code to account for delays here.
     */
    public void actorBlocked() {
        try {
            workspace().getReadAccess();
            _actorsBlocked++;
            if (_actorsAlive == _actorsBlocked) {
                // simulation has ended in deadlock, so terminate
                terminateSimulation();
            }
        } finally {
            workspace().doneReading();
        }
    }
    
    /** Update the count of active actor processes each time a new actor is 
     *  fired up.
     */
    public void actorStarted() {
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
    public void actorStopped() {
        try {
            workspace().getReadAccess();
            _actorsAlive--;
            System.out.println("actor stopped, still alive:" + _actorsAlive);
            
            if (_simulationTerminated) {
                return; 
                // simulation has already been terminated so do not need to
                // terminate it again!
            }
            if (_actorsAlive == _actorsBlocked) {
                // simulation has ended in deadlock, so terminate
                terminateSimulation();
            }
        } finally {
            workspace().doneReading();
        }
    }
    
    /** A actor has unblocked, update count of blocked actors.
     */
    public void actorUnblocked() {
        // No need to synchronize this because the action is atomic
        // and synchronization would just ensure that no write action
        // is in progress.
        workspace().getReadAccess();
        _actorsBlocked--;
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
	_processGroup = null;
	_simulationTerminated = false;
        return newobj;
    }
     
    /** Returns the threadGroup in which all the threads corresponding to 
     *  CSP actors are started. If this is an executive director this 
     *  returns null.
     * 
     *  @return The ThreadGroup for the actors controlled by this director.
     */
    public ThreadGroup getProcessGroup() {
        if (isExecutiveDirector()) {
            // this is a local director
            if (_processGroup == null) {
                String str = "ThreadGroup for " + getName() + " director";
                _processGroup = new ThreadGroup(str);
            }          
        }
        return _processGroup;
    }
    
    /** If this is the local director of the container, then invoke the fire
     *  methods of all its deeply contained actors.  Otherwise, invoke the
     *  fire() method of the container.  In general, this may be called more
     *  than once in the same iteration, where an iteration is defined as one
     *  invocation of prefire(), any number of invocations of fire(),
     *  and one invocation of postfire().
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception CloneNotSupportedException If the fire() method of the
     *   container or one of the deeply contained actors throws it.
     *  @exception IllegalActionException If the fire() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            if (!isExecutiveDirector()) {
                // This is the local director.
                Enumeration allactors = container.deepGetEntities();
                while (allactors.hasMoreElements()) {
                    Actor actor = (Actor)allactors.nextElement();
                    actor.fire();
                }
		System.out.println("invoked fire methods of all actors, now wait for the simulation to terminate: " + getName());
		try {
                    while (!_simulationTerminated) {
                        // a HORRIBLE hack, waits until all processes are 
                        // stopped to invoke their postfire methods.
                        Thread.currentThread().sleep(2000);
                    }
		} catch (InterruptedException ex) {
                    System.out.println("Local cspDirector interrupted while waiting for firing to finish");
		}
            } else {
                // This is the executive director.
                container.fire();
            }
        }
    }
    
    /** Return a new CSPReceiver compatible with this director.
     *  In the CSP domain, we use CSPReceivers.
     *  @return A new CSPReceiver.
     */
    public Receiver newReceiver() {
        return new CSPReceiver();
    }
    
    /** The action to terminate all actors under control of this local 
     *  director because a real deadlock has occurred. It could also 
     *  be called when a UI decides to terminate a simulation prematurely.
     *  <p>
     *  FIXME: this method is designed to be used with a local director. 
     *  How should an executive director call the local director to make 
     *  this happen?
     */
    public void terminateSimulation() {
        System.out.println("about to terminate simulation");
        // FIXME: check this is a local director.
        try {
            workspace().getReadAccess();
            if (_simulationTerminated) {
                // simulation has already been terminated!
                return;
            }
            _simulationTerminated = true;
            
            CSPCompositeActor cont = (CSPCompositeActor)getContainer();
            Enumeration allMyActors = cont.deepGetEntities(); 
            
            while (allMyActors.hasMoreElements()) {
                try {
                    CSPActor actor = (CSPActor)allMyActors.nextElement(); 
                    // the method in the CSPActor should set a flag in each 
                    // receiver & notifyAll thread waiting on the lock 
                    // for that receiver.
                    actor.terminate();
                    System.out.println("CSPDirector: terminating actor " + actor.getName());
                } catch (Exception ex) {
                    System.out.println("CSPDirector: unable to terminate all actors");
                    //FIXME: should not catch general exception
                }
            }      
        } finally {
            workspace().doneReading();
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _actorsBlocked = 0;
    private int _actorsAlive = 0;
    private int _actorsDelayed = 0;
    
    // The threadgroup in which all the stars are created.
    private ThreadGroup _processGroup;
    
    // Set to true when the simulation is terminated
    private boolean _simulationTerminated = false;
}
