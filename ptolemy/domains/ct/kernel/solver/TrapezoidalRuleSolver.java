/* The Variable Step Trapezoidal Rule Solver

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTSchedule;
import ptolemy.domains.ct.kernel.CTScheduler;
import ptolemy.domains.ct.kernel.ODESolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// TrapezoidalRuleSolver
/**
NOTE: The step size control mechanism in this class is not very elegant.
Please avoiding using this class if possible.

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
@since Ptolemy II 0.2
*/
public class TrapezoidalRuleSolver extends ODESolver {

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  The name of the solver is set to "CT_Trapezoidal_Rule_Solver".
     */
    public TrapezoidalRuleSolver() {
        this(null);
    }

    /** Construct a solver in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The solver is added to the list of objects in the workspace.
     *  The name of the solver is set to "CT_Trapezoidal_Rule_Solver".
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
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

    /** Return 0 to indicate that this solver needs no
     *  history information.
     *  @return 0.
     */
    public int getHistoryCapacityRequirement() {
        return 0;
    }

    /** Return 2 to indicate that an integrator under this solver needs
     *  2 auxiliary variables.
     *  @return 2.
     */
    public int getIntegratorAuxVariableCount() {
        return 2;
    }

    /** The fire() method for integrators under this solver. It performs
     *  the ODE solving algorithm.
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException If there is no director.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        CTDirector dir = (CTDirector)getContainer();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " must have a CT director.");
        }
        // Tf there's no enough history, use the derivative resolver
        // to start the method.

        double f1 = integrator.getDerivative();
        double h = dir.getCurrentStepSize();
        double tentativeState;
        if (getRound() == 0) {
            // prediction
            tentativeState = integrator.getState() + f1*h;
            // for error control
            integrator.setAuxVariables(0, tentativeState);
        } else {
            //correction
            double f2 = ((DoubleToken)integrator.input.get(0)).doubleValue();
            tentativeState = integrator.getState() + (h*(f1+f2))/(double)2.0;
            double error =
                Math.abs(tentativeState-integrator.getTentativeState());
            if ( !(error < dir.getValueResolution())) {
                _voteForConvergence(false);
            }
            integrator.setTentativeDerivative(f2);
        }
        integrator.setTentativeState(tentativeState);
        integrator.output.broadcast(new DoubleToken(tentativeState));
    }

    /** Perform the isThisStepAccurate() test for the integrator under
     *  this solver. This method calculates the tentative state
     *  and test whether the local
     *  truncation error (an estimation of the local error) is less
     *  than the error tolerance
     *  @param integrator The integrator that calls this method.
     *  @return True if the intergrator report a success on the this step.
     */
    public boolean integratorIsAccurate(CTBaseIntegrator integrator) {
        CTDirector dir = (CTDirector)getContainer();
        double tolerance = dir.getErrorTolerance();
        double[] k = integrator.getAuxVariables();
        double localError =
            0.1*Math.abs(integrator.getTentativeState() - k[0]);
        integrator.setAuxVariables(1, localError);
        _debug("Integrator: "+ integrator.getName() +
                " local truncation error = " + localError);
        if (localError < tolerance) {
            _debug("Integrator: " + integrator.getName() +
                    " report a success.");
            return true;
        } else {
            _debug("Integrator: " + integrator.getName() +
                    " reports a failure.");
            return false;
        }
    }

    /** Provide the suggestedNextStepSize() method for integrators under
     *  this solver. If this step (with step size 'h') is successful,
     *  the local truncation error is 'localError', and the local truncation
     *  error tolerance is 'tolerance', then the suggested next step size is:
     *  <pre>
     *     h* max(0.5, power((3.0*tolerance/localError), 1.0/3.0))
     *  </pre>
     *  @param integrator The integrator of that calls this method.
     *  @return The suggested next step by the given integrator.
     */
    public double integratorPredictedStepSize(CTBaseIntegrator integrator) {
        CTDirector dir = (CTDirector)getContainer();
        double localError = (integrator.getAuxVariables())[1];
        double h = dir.getCurrentStepSize();
        double tolerance = dir.getErrorTolerance();
        double newh = h;
        if (localError/tolerance < 0.1) {
            newh =
                h* Math.min(2, Math.pow((3.0*tolerance/localError), 1.0/3.0));
        }
        _debug("integrator: " + integrator.getName() +
                " suggests next step size = " + newh);
        return newh;
    }

    /** Resolve the state of the integrators at time
     *  CurrentTime+CurrentStepSize. It gets the state transition
     *  schedule from the scheduler and fire until the fixed point
     *  is reached.
     *
     * @exception IllegalActionException Not thrown in this base class
     *  May be needed by the derived class.
     */
    public boolean resolveStates() throws IllegalActionException {
        _debug(getFullName() + ": in resolveState().");

        CTDirector dir = (CTDirector)getContainer();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " must have a CT director.");
        }
        CTScheduler scheduler = (CTScheduler)dir.getScheduler();
        if (scheduler == null) {
            throw new IllegalActionException( dir,
                    " must have a director to fire.");
        }
        resetRound();
        // prediction
        CTSchedule schedule = (CTSchedule)scheduler.getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.DYNAMIC_ACTORS).actorIterator();
        while (actors.hasNext()) {
            Actor next = (Actor)actors.next();
            _debug("Guessing..."+((Nameable)next).getName());
            next.fire();
        }
        dir.setCurrentTime(dir.getCurrentTime()+dir.getCurrentStepSize());
        _setConvergence(false);
        int iterations = 0;
        while (!_isConverged()) {
            incrementRound();
            _setConvergence(true);
            actors = schedule.get(
                    CTSchedule.STATE_TRANSITION_ACTORS).actorIterator();
            while (actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _prefireIfNecessary(next);
                _debug(getFullName() + "Firing..."+((Nameable)next).getName());
                next.fire();
            }
            actors = schedule.get(
                    CTSchedule.DYNAMIC_ACTORS).actorIterator();
            while (actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " refiring..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            if (iterations++ > dir.getMaxIterations()) {
                //reduce step size and start over.
                //startOverLastStep();
                resetRound();
                // prediction
                actors = schedule.get(
                        CTSchedule.DYNAMIC_ACTORS).actorIterator();
                while (actors.hasNext()) {
                    Actor next = (Actor)actors.next();
                    _debug(getFullName()+" asking..."+
                            ((Nameable)next).getName());
                    next.fire();
                }
                dir.setCurrentTime(dir.getCurrentTime()+
                        dir.getCurrentStepSize());
                _setConvergence(false);
                iterations = 0;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /** Return true if the fixed point iteration is converged. This is
     *  the result of all _voteForConvergence() in the current integration
     *  step.
     *  @return True if all the votes are true.
     */
    protected boolean _isConverged() {
        return _converge;
    }

    /** Set the convergence flag. Usually called to reset the flag to false
     *  at the beginning of an integration step.
     *  @param converge The flag setting.
     */
    protected void _setConvergence(boolean converge) {
        _converge = converge;
    }

    /** Vote for whether a fixed point has reached. The final result
     *  is the <i>and</i> of all votes.
     *  @param converge True if vote for converge.
     */
    protected void _voteForConvergence(boolean converge) {
        _converge = _converge && converge;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Name of this Solver. */
    private static final String _DEFAULT_NAME="CT_Trapezoidal_Rule_Solver" ;

    /** @serial True if all the votes are true. */
    private boolean _converge;
}
