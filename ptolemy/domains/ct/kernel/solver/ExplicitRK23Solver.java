/* Explicit variable step size Runge-Kutta 2(3) ODE solver.

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

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.ODESolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ExplicitRK23Solver

/**
 This class implements the Explicit Runge-Kutta 2(3) ODE solving method.
 For an ODE of the form:
 <pre>
 dx/dt = f(x, t), x(0) = x0
 </pre>
 it does the following:
 <pre>
 K0 = f(x(n), tn);
 K1 = f(x(n)+0.5*h*K0, tn+0.5*h);
 K2 = f(x(n)+0.75*h*K1, tn+0.75*h);
 x(n+1) = x(n)+(2/9)*h*K0+(1/3)*h*K0+(4/9)*h*K2;
 </pre>,
 and error control:
 <pre>
 K3 = f(x(n+1), tn+h);
 LTE = h*[(-5.0/72.0)*K0 + (1.0/12.0)*K1 + (1.0/9.0)*K2 + (-1.0/8.0)*K3]
 </pre>
 <P>
 If the LTE is less than the error tolerance, then this step is considered
 successful, and the next integration step is predicted as:
 <pre>
 h' = 0.8*Math.pow((ErrorTolerance/LTE), 1.0/3.0)
 </pre>
 This is a second order method, but uses a third order procedure to estimate
 the local truncation error.

 @author  Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class ExplicitRK23Solver extends ODESolver {
    /** Construct a solver in the default workspace.
     *  The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  The name of the solver is set to "CT_Runge_Kutta_2_3_Solver".
     */
    public ExplicitRK23Solver() {
        this(null);
    }

    /** Construct a solver in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The name of the solver is set to "CT_Runge_Kutta_2_3_Solver".
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public ExplicitRK23Solver(Workspace workspace) {
        super(workspace);

        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire dynamic actors. Advance the model time. The amount of the
     *  increment is decided by the number of the round counter and
     *  the current step size.  In particular, at the first round,
     *  time is incremented by 1/2 of the current step size; at the
     *  second round, time is incremented by another 1/4 of the
     *  current step size; in the third round, time gets incremented
     *  by the last 1/4 of the current step size.
     *  @exception IllegalActionException If thrown in the super class or the
     *  model time can not be set.
     */
    public void fireDynamicActors() throws IllegalActionException {
        super.fireDynamicActors();

        CTDirector director = (CTDirector) getContainer();

        // NOTE: why is the current model time changed here?
        // Some state transition actors may be some functions
        // defined on the current time, such as the CurrentTime actor.
        Time iterationBeginTime = director.getIterationBeginTime();
        double currentStepSize = director.getCurrentStepSize();
        director.setModelTime(iterationBeginTime.add(currentStepSize
                * _timeInc[_getRoundCount()]));
    }

    /** Fire state transition actors. Increment the round count.
     *  If the current round is the third round, set converged flag to
     *  true indicating a fixed-point states have been reached. Reset
     *  the round count if the current round is the last one.
     *  @exception IllegalActionException If thrown in the super class.
     */
    public void fireStateTransitionActors() throws IllegalActionException {
        super.fireStateTransitionActors();
        _incrementRoundCount();

        if (_getRoundCount() == _timeInc.length) {
            _resetRoundCount();
            _setConverged(true);
        }
    }

    /** Return 0 to indicate that no history information is needed
     *  by this solver.
     *  @return 0.
     */
    public final int getAmountOfHistoryInformation() {
        return 0;
    }

    /** Return 4 to indicate that four auxiliary variables are
     *  needed by this solver.
     *  @return 4.
     */
    public final int getIntegratorAuxVariableCount() {
        return 4;
    }

    /** Fire the given integrator. This method performs the ODE solving
     *  algorithm described in the class comment.
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException If there is no director, or can not
     *  read input, or can not send output.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        CTDirector director = (CTDirector) getContainer();
        int r = _getRoundCount();
        double xn = integrator.getState();
        double outvalue;
        double h = director.getCurrentStepSize();
        double[] k = integrator.getAuxVariables();

        switch (r) {
        case 0:

            // Get the derivative at t;
            double k0 = integrator.getDerivative();
            integrator.setAuxVariables(0, k0);
            outvalue = xn + (h * k0 * _B[0][0]);
            break;

        case 1:

            double k1 = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(1, k1);
            outvalue = xn + (h * ((k[0] * _B[1][0]) + (k1 * _B[1][1])));
            break;

        case 2:

            double k2 = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(2, k2);
            outvalue = xn
                    + (h * ((k[0] * _B[2][0]) + (k[1] * _B[2][1]) + (k2 * _B[2][2])));
            integrator.setTentativeState(outvalue);
            break;

        default:
            throw new InvalidStateException(this,
                    "execution sequence out of range.");
        }

        integrator.output.broadcast(new DoubleToken(outvalue));
    }

    /** Return true if the integration is accurate for the given
     *  integrator. This estimates the local truncation error for that
     *  integrator and compare it with the error tolerance.
     *
     *  @param integrator The integrator of that calls this method.
     *  @return True if the integration is successful.
     */
    public boolean integratorIsAccurate(CTBaseIntegrator integrator) {
        try {
            CTDirector director = (CTDirector) getContainer();
            double tolerance = director.getErrorTolerance();
            double h = director.getCurrentStepSize();
            double f = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setTentativeDerivative(f);

            double[] k = integrator.getAuxVariables();
            double error = h
                    * Math.abs((k[0] * _E[0]) + (k[1] * _E[1]) + (k[2] * _E[2])
                            + (f * _E[3]));

            //k[3] is Local Truncation Error
            integrator.setAuxVariables(3, error);
            _debug("Integrator: " + integrator.getName()
                    + " local truncation error = " + error);

            if (error < tolerance) {
                _debug("Integrator: " + integrator.getName()
                        + " report a success.");
                return true;
            } else {
                _debug("Integrator: " + integrator.getName()
                        + " reports a failure.");
                return false;
            }
        } catch (IllegalActionException e) {
            //should never happen.
            throw new InternalErrorException(integrator.getName()
                    + " can't read input." + e.getMessage());
        }
    }

    /** Provide the predictedStepSize() method for the integrators
     *  under this solver. It uses the algorithm in the class comments
     *  to predict the next step size based on the current estimation
     *  of the local truncation error.
     *
     *  @param integrator The integrator of that calls this method.
     *  @return The next step size suggested by the given integrator.
     */
    public double integratorPredictedStepSize(CTBaseIntegrator integrator) {
        CTDirector director = (CTDirector) getContainer();
        double error = (integrator.getAuxVariables())[3];
        double h = director.getCurrentStepSize();
        double tolerance = director.getErrorTolerance();
        double newh = 5.0 * h;

        if (error > director.getValueResolution()) {
            newh = h
                    * Math.max(0.5, 0.8 * Math.pow((tolerance / error),
                            1.0 / _order));
        }

        _debug("integrator: " + integrator.getName()
                + " suggests next step size = " + newh);
        return newh;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The name of the solver
    private static final String _DEFAULT_NAME = "CT_Runge_Kutta_2_3_Solver";

    // The ratio of time increments within one integration step.
    private static final double[] _timeInc = { 0.5, 0.75, 1.0 };

    // B coefficients
    private static final double[][] _B = { { 0.5 }, { 0, 0.75 },
            { 2.0 / 9.0, 1.0 / 3.0, 4.0 / 9.0 } };

    // E coefficients
    private static final double[] _E = { -5.0 / 72.0, 1.0 / 12.0, 1.0 / 9.0,
            -1.0 / 8.0 };

    // The order of the algorithm.
    private static final int _order = 3;
}
