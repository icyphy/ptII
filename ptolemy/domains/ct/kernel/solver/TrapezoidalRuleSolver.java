/* The Variable Step Trapezoidal Rule Solver

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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.domains.ct.kernel.solver;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
//import java.util.Enumeration;
import java.util.Iterator;

import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// TrapezoidalRuleSolver
/**
This class is under significant rework. Please don't use it!!!
@author Jie Liu
@version $Id$
*/
public class TrapezoidalRuleSolver extends ODESolver{


    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public TrapezoidalRuleSolver() {
        super();
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException e) {
            // this should never happen.
            throw new InternalErrorException(e.toString());
        }
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
    public TrapezoidalRuleSolver(Workspace workspace) {
        super(workspace);
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException e) {
            // this should never happen.
            throw new InternalErrorException(e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
    public boolean resolveStates() throws IllegalActionException {
        _debug(getFullName() + ": in resolveState().");

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
        // prediction
        Iterator actors = sch.dynamicActorList().iterator();
        while(actors.hasNext()) {
            Actor next = (Actor)actors.next();
            _debug("Guessing..."+((Nameable)next).getName());
            next.fire();
        }
        dir.setCurrentTime(dir.getCurrentTime()+dir.getCurrentStepSize());
        _setConverge(false);
        int iterations = 0;
        while(!isConverged()) {
            if(dir.STAT) {
                dir.NFUNC ++;
            }
            incrRound();
            _setConverge(true);
            actors = sch.scheduledStateTransitionActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + "Firing..."+((Nameable)next).getName());

                next.fire();
            }
            actors = sch.scheduledDynamicActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " refiring..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            if(iterations++ > dir.getMaxIterations()) {
                //reduce step size and start over.
                //startOverLastStep();
                resetRound();
                // prediction
                actors = sch.scheduledDynamicActorList().iterator();
                while(actors.hasNext()) {
                    Actor next = (Actor)actors.next();
                    _debug(getFullName()+" asking..."+
                            ((Nameable)next).getName());
                    next.fire();
                }
                dir.setCurrentTime(dir.getCurrentTime()+
                        dir.getCurrentStepSize());
                _setConverge(false);
                iterations = 0;
            }
        }
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
        double f1 = (integrator.getHistory(0))[1];;
        double h = dir.getCurrentStepSize();
        double pstate;
        if (getRound() == 0) {
            // prediction
            pstate = integrator.getState() + f1*h;
            // for error control
            integrator.setAuxVariables(0, pstate);
        } else {
            //correction
            double f2 = ((DoubleToken)integrator.input.get(0)).doubleValue();
            pstate = integrator.getState() + (h*(f1+f2))/(double)2.0;
            double cerror = Math.abs(pstate-integrator.getTentativeState());
            if( !(cerror < dir.getValueResolution())) {
                voteForConverge(false);
            }
            integrator.setTentativeDerivative(f2);
        }
        integrator.setTentativeState(pstate);
        integrator.output.broadcast(new DoubleToken(pstate));
    }

    /** Integrator calculate potential state and test for local
     *  truncation error.
     *  @param integrator The integrator of that calls this method.
     *  @return True if the intergrator report a success on the last step.
     */
    public boolean integratorIsSuccessful(CTBaseIntegrator integrator) {
        try {
            CTDirector dir = (CTDirector)getContainer();
            double errtol = dir.getErrorTolerance();
            double[] k = integrator.getAuxVariables();
            double lte = 0.5*Math.abs(integrator.getTentativeState() - k[0]);
            integrator.setAuxVariables(1, lte);
            _debug("Integrator: "+ integrator.getName() +
                    " local truncation error = " + lte);
            if(lte<errtol) {
                _debug("Integrator: " + integrator.getName() +
                        " report a success.");
                return true;
            } else {
                _debug("Integrator: " + integrator.getName() +
                        " reports a failure.");
                return false;
            }
        } catch (IllegalActionException e) {
            //should never happen.
            throw new InternalErrorException(integrator.getName() +
                    " can't read input." + e.getMessage());
        }
    }

    /** Hook method for suggestedNextStepSize() method of
     *  integrators.
     *  @param integrator The integrator of that calls this method.
     *  @return The suggested next step by the given integrator.
     */
    public double integratorPredictedStepSize(CTBaseIntegrator integrator) {
        CTDirector dir = (CTDirector)getContainer();
        double lte = (integrator.getAuxVariables())[1];
        double h = dir.getCurrentStepSize();
        double errtol = dir.getErrorTolerance();
        double newh = 5.0*h;
        if(lte>dir.getValueResolution()) {
            newh = h* Math.max(0.5, Math.pow((3.0*errtol/lte), 1.0/3.0));
        }
        _debug("integrator: " + integrator.getName() +
                " suggests next step size = " + newh);
        return newh;
    }

    /** Integrator's aux variable number needed when solving the ODE.
     *  @return The number of auxiliary variables for the solver in each
     *       integrator.
     */
    public final int getIntegratorAuxVariableCount() {
        return 2;
    }

    /** Return 2 always. It needs two history points to do prediction.
     *  @return 2.
     */
    public final int getHistoryCapacityRequirement() {
        return 2;
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

    /** Name of this Solver. */
    private static final String _DEFAULT_NAME="CT_Trapezoidal_Rule_Solver" ;

    /** @serial True if all the votes are true. */
    private boolean _converge;
}
