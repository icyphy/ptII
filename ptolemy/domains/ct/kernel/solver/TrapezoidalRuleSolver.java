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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Nameable;
import ptolemy.actor.Actor;
import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.kernel.*;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// TrapezoidalRuleSolver
/**
NOTE: This class is under significant rework. Please don't use it!

This is a second order variable step size ODE solver that uses the
trapezoidal rule algorithm. For an ODE
<pre>
    x' = f(x, t)
    x(0) = x0
</pre>
the solver iterates:
<pre>
x(t+h) = x(t) + (h/2)*(x'(t) + x'(t+h))
</pre>
This is the most accurate second order multi-step ODE solver.
It is an implicit algorithm, which involves a fixed-point iteration
to find x(t+h) and x'(t+h).

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
            throw new InternalErrorException(e.getMessage());
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
            throw new InternalErrorException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Integrator's auxiliary variable number needed when solving the ODE.
     *  @return 2.
     */
    public int getIntegratorAuxVariableCount() {
        return 2;
    }

    /** Return the number of history points needed.
     *  @return 2.
     */
    public int getHistoryCapacityRequirement() {
        return 2;
    }   

    /** The fire() method for integrators under this solver. It performs
     *  the ODE solving algorithm.
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException Not thrown in this
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

    /** Perform the isSuccessful() test for integrators under this solver.
     *  It calculates the tentative state and test for local
     *  truncation error.
     *  @param integrator The integrator of that calls this method.
     *  @return True if the intergrator report a success on the this step.
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
                    " No input token available." + e.getMessage());
        }
    }

    /** Provide the suggestedNextStepSize() method for integrators under
     *  this solver. If this step (with step size 'h') is successful, 
     *  the local truncation error is 'lte', and the local truncation
     *  error tolerance is 'errtol', then the suggested next step size is:
     *  <pre>
     *     h* max(0.5, power((3.0*errtol/lte), 1.0/3.0))
     *  </pre>
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

    /** Return true if the fixed point iteration is converged. This is 
     *  the result of all voteForConverge() in the current integration
     *  step.
     *  @return True if all the votes are true.
     */
    public boolean isConverged() {
        return _converge;
    }

    /** Resolve the state of the integrators at time
     *  CurrentTime+CurrentStepSize. It gets the state transition
     *  schedule from the scheduler and fire until the fixed point
     *  is reached.
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

    /** To Vote for whether a fixed point has reached. The final result 
     *  is the <i>and</i> of all votes.
     *  @param converge True if vote for converge.
     */
    public void voteForConverge(boolean converge) {
        _converge = _converge && converge;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                       protected methods                        ////
    /** Set the convergence flag. Usually called to reset the flag to false
     *  at the beginning of an integration step.
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
