/* The Variable Step Trapezoidal Rule Solver

Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.ODESolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// TrapezoidalRuleSolver

/**This is a second order variable step size ODE solver that uses the
   trapezoidal rule algorithm. Unlike the solvers with fixed step size,
   this solver adjusts its step size based on the estimation of the
   local truncation error.
   <p>
   NOTE: The design of this solver, in particular, the design of estimation of
   the local truncation error, is still priliminary.
   <p>

   For an ODE
   <pre>
   x' = f(x, t)
   x(0) = x0
   </pre>
   the solver iterates:
   <pre>
   x(t+h) = x(t) + (h/2)*(x'(t) + x'(t+h))
   </pre>
   This solver uses an implicit algorithm, which involves a fixed-point
   iteration to find x(t+h) and x'(t+h).
   <p>
   The local truncation error (LTE) control is based on the formula 9.78 in
   "Modeling and Simulation of Dynamic Systems" by Robert L. Woods and Kent L.
   Lawrence.
   <p>
   The basic idea is that once states and derivatives are resolved, denoted as
   x(t+h) and x'(t+h), use a two-step method with the calculated derivatives
   to recalculate the states, denoted as xx(t+h). Since this solver is second
   order, the LTE is approximately:
   <pre>
   abs(x(t+h) - xx(t+h))/(2^2 - 1)
   </pre>
   This is used to adjust the step size.

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (hyzheng)
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
        } catch (KernelException ex) {
            // this should never happen.
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire dynamic actors. Advance the model time to the current model time
     *  plus the current step size.
     *  @exception IllegalActionException If thrown in the super class or the
     *  model time can not be set.
     */
    public void fireDynamicActors() throws IllegalActionException {
        // First assume that the states will converge after this round.
        // If any integrator does not agree, the final converged status may be
        // changed via calling _voteForConverged() by that integrator.
        _setConverged(true);

        CTDirector director = (CTDirector) getContainer();
        super.fireDynamicActors();

        if (_getRoundCount() == 0) {
            _recalculatingWithTwoSteps = false;
            _firstStep = true;
            director.setModelTime(director.getModelTime().add(director
                                          .getCurrentStepSize()));
        }

        if (_isConverged()) {
            // Resolved states have converged.
            // We need to recalculate the states with two steps.
            // The new states will be used to control local truncation error
            if (!_recalculatingWithTwoSteps) {
                _setConverged(false);
                _recalculatingWithTwoSteps = true;
            } else {
                if (_firstStep) {
                    _setConverged(false);
                    _firstStep = false;
                }
            }
        }
    }

    /** Fire state transition actors. Increment the round count. If the states
     *  have converged, reset the round count.
     *  @exception IllegalActionException If thrown in the super class.
     */
    public void fireStateTransitionActors() throws IllegalActionException {
        super.fireStateTransitionActors();
        _incrementRoundCount();

        if (_isConverged()) {
            _resetRoundCount();
        }
    }

    /** Return 0 to indicate that this solver needs no
     *  history information.
     *  @return 0.
     */
    public int getAmountOfHistoryInformation() {
        return 0;
    }

    /** Return 2 to indicate that an integrator under this solver needs
     *  2 auxiliary variables.
     *  @return 2.
     */
    public int getIntegratorAuxVariableCount() {
        return 2;
    }

    /** Fire the given integrator. Vote false for convergence if
     *  a fixed-point solution is not found. When the states first converged,
     *  calculate the states again with two steps (each step advance time half
     *  of the current step size) for error control.
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException If there is no director, or can not
     *  read input, or send output.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        CTDirector director = (CTDirector) getContainer();
        double h = director.getCurrentStepSize();
        double tentativeState;

        if (_getRoundCount() == 0) {
            // During the first round, use the current derivative to predict
            // the states at currentModelTime + currentStepSize. The predicted
            // states are the initial guesses for fixed-point iteration.
            double f1 = integrator.getDerivative();
            tentativeState = integrator.getState() + (f1 * h);

            // Set converged to false such that the integrator will be refired
            // again to check convergence of resolved states.
            _voteForConverged(false);
        } else {
            // get the derivative at the beginning time of current integration.
            double f1 = integrator.getDerivative();

            // get predicated derivative at the end time of current integration.
            double f2 = ((DoubleToken) integrator.input.get(0)).doubleValue();

            if (!_recalculatingWithTwoSteps) {
                tentativeState = integrator.getState()
                    + ((h * (f1 + f2)) / 2.0);

                double error = Math.abs(tentativeState
                        - integrator.getTentativeState());

                if (error < director.getValueResolution()) {
                    // save resolved states for local truncation error control
                    integrator.setAuxVariables(0, tentativeState);
                    _voteForConverged(true);
                } else {
                    _voteForConverged(false);
                }
            } else {
                if (_firstStep) {
                    // calculate the states with half of the step size.
                    tentativeState = integrator.getState()
                        + ((h * (f1 + f2)) / 2.0 / 2.0);
                } else {
                    // calculate the states with half of the step size.
                    tentativeState = integrator.getTentativeState()
                        + ((h * (f1 + f2)) / 2.0 / 2.0);

                    // NOTE: We save the newly calculated state as the saved
                    // aux variable and restore the tentativeState back to
                    // the fixed-point state
                    double temp = tentativeState;
                    integrator.setAuxVariables(0, temp);
                    tentativeState = (integrator.getAuxVariables())[0];
                    integrator.setTentativeDerivative(f2);
                }

                _voteForConverged(true);
            }
        }

        integrator.setTentativeState(tentativeState);
        integrator.output.broadcast(new DoubleToken(tentativeState));
    }

    /** Perform the integratorIsAccurate() test for the integrator under
     *  this solver. This method returns true if the local truncation error
     *  (an estimation of the local error) is less than the error tolerance.
     *  Otherwise, return false.
     *  @param integrator The integrator that calls this method.
     *  @return True if the local truncation error is less than the
     *  error tolerance.
     */
    public boolean integratorIsAccurate(CTBaseIntegrator integrator) {
        CTDirector director = (CTDirector) getContainer();
        double tolerance = director.getErrorTolerance();
        double[] k = integrator.getAuxVariables();
        double localError = (1.0 / 3.0) * Math.abs(integrator.getTentativeState()
                - k[0]);
        integrator.setAuxVariables(1, localError);

        if (_debugging) {
            _debug("Integrator: " + integrator.getName()
                    + " local truncation error = " + localError);
        }

        if (localError < tolerance) {
            if (_debugging) {
                _debug("Integrator: " + integrator.getName()
                        + " report a success.");
            }

            return true;
        } else {
            if (_debugging) {
                _debug("Integrator: " + integrator.getName()
                        + " reports a failure.");
            }

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
        CTDirector director = (CTDirector) getContainer();
        double localError = (integrator.getAuxVariables())[1];
        double h = director.getCurrentStepSize();
        double tolerance = director.getErrorTolerance();
        double newh = h;

        if ((localError / tolerance) < 0.1) {
            newh = h * Math.min(2,
                    Math.pow(((3.0 * tolerance) / localError),
                            1.0 / 3.0));
        }

        _debug("integrator: " + integrator.getName()
                + " suggests next step size = " + newh);
        return newh;
    }

    /** Return true if the resolved states have converged. Return false if
     *  states have not converged but the number of iterations reaches the
     *  <i>maxIterations</i> number. Mean while, the round count is reset.
     *  @return True if the resolved states have converged.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean resolveStates() throws IllegalActionException {
        CTDirector director = (CTDirector) getContainer();

        if (_getRoundCount() > director.getMaxIterations()) {
            _resetRoundCount();
            return false;
        }

        return super.resolveStates();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Name of this Solver. */
    private static final String _DEFAULT_NAME = "CT_Trapezoidal_Rule_Solver";
    private boolean _recalculatingWithTwoSteps = false;
    private boolean _firstStep = true;
}
