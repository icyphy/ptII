/* The Forward Euler ODE solver.

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
@ProposedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.solver;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import java.util.Enumeration;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ForwardEulerSolver
/**
The Forward Euler(FE) ODE solver. For ODE
    dx/dt = f(x, u, t), x(0) = x0;
The FE method approximate the x(t+h) as:
    x(t+h) =  x(t) + h * f(x(t), u(t), t)
No error control is performed.
@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class ForwardEulerSolver extends FixedStepSolver{


    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ForwardEulerSolver() {
        super(_name);
    }

    /** Construct a solver in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this solver.
     */
    public ForwardEulerSolver(Workspace workspace) {
        super(workspace, _name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true always, indicating that the states of the system
     *  is correctly resolved. 
     *  The resolved states are at time
     *  CurrentTime+CurrentStepSize. It gets the state transition
     *  schedule from the scheduler and fire for one iteration.
     *  
     * @exception IllegalActionException If the firing of some actors
     *       throw it.
     */
    public boolean resolveStates() throws IllegalActionException {
        if(VERBOSE) {
            System.out.println("FE: resolveState().");
        }
        CTDirector dir = (CTDirector)getContainer();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " must have a CT director.");
        }
        CTScheduler sch = (CTScheduler)dir.getScheduler();
        if (sch == null) {
            throw new IllegalActionException( dir,
                    " must have a director to fire.");
        }
        resetRound();
        if(dir.STAT) {
            dir.NFUNC++;
        }
        Enumeration actors = sch.stateTransitionSchedule();
        while(actors.hasMoreElements()) {
            Actor next = (Actor)actors.nextElement();
            if(DEBUG) {
                System.out.println("Firing..."+((Nameable)next).getName());
            }
            next.fire();
        }
        actors = sch.dynamicActorSchedule();
        while(actors.hasMoreElements()) {
            Actor next = (Actor)actors.nextElement();
            if(DEBUG) {
                System.out.println("Firing..."+((Nameable)next).getName());
            }
            next.fire();
        }
        dir.setCurrentTime(dir.getCurrentTime()+dir.getCurrentStepSize());
        return true;
    }

    /**  fire() method for integrators.
     *
     *  @param integrator The integrator of that calls this method.
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        CTDirector dir = (CTDirector)getContainer();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " must have a CT director.");
        }
        double f = ((DoubleToken)integrator.input.get(0)).doubleValue();
        double pstate = integrator.getState() + f*(dir.getCurrentStepSize());
        integrator.setPotentialState(pstate);
        integrator.setPotentialDerivative(f);

        integrator.output.broadcast(new DoubleToken(pstate));
    }

    /** Integrator's aux variable number needed when solving the ODE.
     *  @return The number of auxilary variables for the solver in each
     *       integrator.
     */
    public final int integratorAuxVariableNumber() {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // static name.
    private static final String _name="CT_Forward_Euler_Solver" ;
}
