/* A CT director that uses real time.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.*;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// CTMultiSolverDirector
/**
A CTDirector that uses real time. This director can only be the top level
director. When initializing, it set the start time to be the computer
time. At the postfire statge of each iteration, it waits until the
computer time has progressed to the director time.
FIXME: still under development.
@author  Jie Liu
@version $Id$
@see ptolemy.domains.ct.kernel.CTDirector
*/
public class CTRealTimeDirector extends CTMultiSolverDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     */
    public CTRealTimeDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace The workspace of this object.
     */
    public CTRealTimeDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in derived classes.
     */
    public CTRealTimeDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialization after type resolution.
     *  It sets the step size and the suggested next step size
     *  to the initial step size. The ODE solver  and the
     *  breakpoint ODE solver are instantiated.
     *  Set the current time to be the start time of the simulation.
     *  Both the current time and the stop time are registered
     *  as a breakpoint.
     *  It invoke the initialize() method for all the Actors in the
     *  container.
     *
     *  @exception IllegalActionException If the instantiation of the solvers
     *  are not succeded or one of the directed actors throw it.
     */
    public void initialize() throws IllegalActionException {
        System.out.println("_timebase = " +  System.currentTimeMillis());
        super.initialize();
        _timebase = System.currentTimeMillis(); 
    }

    /** Call postfire() on all actors. For a correct CT simulation,
     *  the state of an actor can only change at this stage of an
     *  iteration.
     *  @exception IllegalActionException If any of the actors
     *      throws it.
     */
    public void updateStates() throws IllegalActionException {
        long realTime = System.currentTimeMillis()-_timebase;
        if(_debugging) _debug("real time " + realTime);
        long simulationTime = (long)((getCurrentTime()-getStartTime())*1000);
        if(_debugging) _debug("simulation time " + simulationTime);
        long timeDifference = simulationTime-realTime;
        if(timeDifference > 20) {
            try {
                Thread.sleep(timeDifference - 20);
            }  catch (Exception e) {
                throw new IllegalActionException(this,
                        "Sleep Interruptted" + e.getMessage());
            }
        } else {
            if(_debugging) _debug("Warning: " + getFullName() + 
                    " cannot achieve real-time performance",
                    " at simulation time " + getCurrentTime());
        }
        CompositeActor container = (CompositeActor) getContainer();
        Iterator allactors = container.deepEntityList().iterator();
        while(allactors.hasNext()) {
            Actor nextactor = (Actor)allactors.next();
            nextactor.postfire();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The classname of the ODE solver.
    private long _timebase;
}
