/* The Fixed Step Backward Euler ODE solver.

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
*/

package ptolemy.domains.ct.kernel.solver;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import java.util.Enumeration;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// BackwardEulerSolver
/** 
Description of the class
@author Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class BackwardEulerSolver extends FixedStepSolver
        implements ImplicitMethodSolver{

    public static final boolean VERBOSE = true;
    public static final boolean DEBUG = true;

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */	
    public BackwardEulerSolver() {
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
    public BackwardEulerSolver(Workspace workspace) {
        super(workspace, _name);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Vote if a fixed point has reached. The final result is the 
     *  <i>and</i> of all votes.
     *  @param converge True if vote for converge.
     */
    public void voteForConverge(boolean converge) {
        _converge = _converge && converge;
    }

    /** Return true if the vote result is true.
     *  @return True if all the votes are true.
     */
    public boolean isConverged() {
        return _converge;
    }

    /** Resolve the state of the integrators at time 
     *  CurrentTime+CurrentStepSize. It gets the state transition
     *  schedule from the scheduler and fire until the fixed point.
     * 
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public void resolveStates() throws IllegalActionException {
        if(VERBOSE) {
            System.out.println("BE: resolveState().");
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
        dir.setCurrentTime(dir.getCurrentTime()+dir.getCurrentStepSize());
        Enumeration actors = sch.dynamicActorSchedule();
        while(actors.hasMoreElements()) {
            CTDynamicActor next = (CTDynamicActor)actors.nextElement();
            if(DEBUG) {
                System.out.println("Guessing..."+((Nameable)next).getName());
            }
            next.emitPotentialStates();
        }
        _setConverge(false);
        int iterations = 0;
        while(!isConverged()) {
            _setConverge(true);
            actors = sch.stateTransitionSchedule();
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
                    System.out.println("Refiring..."+((Nameable)next).getName());
                }
                next.fire();
            }
            if(iterations++ > dir.getMaxIterations()) {
                throw new InvalidStateException(this,
                    "Fixed point is not converge for maximum iterations "+
                    dir.getMaxIterations());
            }
        }
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
        double cerror =Math.abs(pstate-integrator.getPotentialState());
        if( !(cerror < dir.getValueAccuracy())) {
            voteForConverge(false);
        }
        integrator.setPotentialState(pstate);
        
        integrator.output.broadcast(new DoubleToken(pstate));
    }

    /** Integrator's aux variable number needed when solving the ODE.
     *  @return The number of auxilary variables for the solver in each
     *       integrator.
     */
    public final int integratorAuxVariableNumber() {
        return 1;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                       protected methods                        ////
    /** Set the convergence flag.
     *  @param converge The flag setting.
     */
    protected void _setConverge(boolean converge) {
        _converge = converge;
    }
    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // static name.
    private static final String _name="CT_Backward_Euler_Solver" ;
    private boolean _converge;
}
