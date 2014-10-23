/* Explicit variable step size Runge-Kutta 4(5) ODE solver.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.domains.continuous.kernel.solver;

import ptolemy.domains.continuous.kernel.ContinuousIntegrator;
import ptolemy.domains.continuous.kernel.ContinuousODESolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

///////////////////////////////////////////////////////////////////
//// ExplicitRK45Solver

/**
 This class implements a fourth-order Runge-Kutta ODE solving method.
 The algorithm was introduced in "A Variable Order Runge-Kutta
 Method for Initial Value Problems with Rapidly Varying Right-Hand Sides"
 by J. R. Cash and Alan H. Karp, ACM Transactions on Mathematical Software,
 vol 16, pp. 201-222, 1990. For completeness, a brief explanation of the
 algorithm is explained below.
 <p>
 For an ODE of the form:
 <pre>
 dx(t)/dt = f(x(t), t), x(0) = x0
 </pre>
 it does the following:
 <pre>
 K0 = f(x(n), tn);
 K1 = f(x(n) + 0.2*K0*h, tn + 0.2*h);
 K2 = f(x(n) + (3.0/40*K0 + 9.0/40*K1)*h, tn + 0.3*h);
 K3 = f(x(n) + (0.3*K0 - 0.9*K1 + 1.2*K2)*h, tn + 0.6*h);
 K4 = f(x(n) + (-11/54*K0 + 5.0/2*K1 -70/27*K2 + 35/27*K3)*h, tn + 1.0*h);
 K5 = f(x(n) + (1631/55296*K0 + 175/512*K1 + 575/13824*K2 + 3544275/110592*K3 + 253/4096*K4)*h, tn + 7/8*h);
 x(n+1) = x(n)+(37/378*K0 + 250/621*K2 + 125.0/594*K3 + 512.0/1771*K5)*h;
 </pre>,
 and error control:
 <pre>
 LTE = [(37.0/378 - 2825.0/27648)*K0 + (250.0/621 - 18575.0/48384)*K2 +
 (125.0/594 - 13525.0/55296)*K3 + (0.0 - 277.0/14336)*K4 +
 (512.0/1771 - 0.25)*K5]*h.
 </pre>
 <P>
 If the LTE is less than the error tolerance, then this step size h is
 considered successful, and the next integration step size h' is predicted as:
 <pre>
 h' = h * Math.pow((ErrorTolerance/LTE), 1.0/5.0)
 </pre>
 This is a fourth order method, but uses a fifth order procedure to estimate
 the local truncation error.
 <p>
 It takes 6 steps for this solver to resolve a state with an integration
 step size.

 @author  Haiyang Zheng, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class ExplicitRK45Solver extends ContinuousODESolver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of time increments plus one (to store the
     *  truncation error).
     *  @return The number of time increments plus one.
     */
    @Override
    public final int getIntegratorAuxVariableCount() {
        // Allow one for the truncation error
        return _TIME_INCREMENTS.length + 1;
    }

    /** Fire the given integrator. This method performs the ODE solving
     *  algorithm described in the class comment.
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException If there is no director, or can not
     *  read input, or can not send output.
     */
    @Override
    public void integratorIntegrate(ContinuousIntegrator integrator)
            throws IllegalActionException {
        double xn = integrator.getState();
        double outputValue;
        double h = _director.getCurrentStepSize();
        double[] k = integrator.getAuxVariables();
        integrator.setAuxVariables(_roundCount, integrator.getDerivative());

        switch (_roundCount) {
        case 0:
            outputValue = xn + h * k[0] * _B[0][0];
            break;

        case 1:
            outputValue = xn + h * (k[0] * _B[1][0] + k[1] * _B[1][1]);
            break;

        case 2:
            outputValue = xn + h
            * (k[0] * _B[2][0] + k[1] * _B[2][1] + k[2] * _B[2][2]);
            break;

        case 3:
            outputValue = xn
            + h
            * (k[0] * _B[3][0] + k[1] * _B[3][1] + k[2] * _B[3][2] + k[3]
                    * _B[3][3]);
            break;

        case 4:
            outputValue = xn
            + h
            * (k[0] * _B[4][0] + k[1] * _B[4][1] + k[2] * _B[4][2]
                    + k[3] * _B[4][3] + k[4] * _B[4][4]);
            break;

        case 5:
            outputValue = xn
            + h
            * (k[0] * _B[5][0] + k[1] * _B[5][1] + k[2] * _B[5][2]
                    + k[3] * _B[5][3] + k[4] * _B[5][4] + k[5]
                            * _B[5][5]);
            break;

        case 6:
            outputValue = integrator.getTentativeState();
            return;

        default:
            throw new InvalidStateException("Execution sequence out of range.");
        }

        integrator.setTentativeState(outputValue);
    }

    /** Return true if the integration is accurate for the given
     *  integrator. This estimates the local truncation error for that
     *  integrator and compare it with the error tolerance.
     *
     *  @param integrator The integrator of that calls this method.
     *  @return True if the integration is successful.
     */
    @Override
    public boolean integratorIsAccurate(ContinuousIntegrator integrator) {
        double tolerance = _director.getErrorTolerance();
        double h = _director.getCurrentStepSize();
        double[] k = integrator.getAuxVariables();
        double error = h
                * Math.abs(k[0] * _E[0] + k[1] * _E[1] + k[2] * _E[2] + k[3]
                        * _E[3] + k[4] * _E[4] + k[5] * _E[5]);

        integrator.setAuxVariables(_ERROR_INDEX, error);

        if (_isDebugging()) {
            _debug("Integrator: " + integrator.getName()
                    + " local truncation error = " + error);
        }
        if (error < tolerance) {
            if (_isDebugging()) {
                _debug("Integrator: " + integrator.getName()
                        + " report a success.");
            }
            return true;
        } else {
            if (_isDebugging()) {
                _debug("Integrator: " + integrator.getName()
                        + " reports a failure.");
            }
            return false;
        }
    }

    /** Predict the next step size for the integrators executed under this
     *  solver. This uses the algorithm in the class comments
     *  to predict the next step size based on the current estimation
     *  of the local truncation error.
     *
     *  @param integrator The integrator that calls this method.
     *  @return The next step size suggested by the given integrator.
     */
    @Override
    public double integratorSuggestedStepSize(ContinuousIntegrator integrator) {
        double error = integrator.getAuxVariables()[_ERROR_INDEX];
        double h = _director.getCurrentStepSize();
        double tolerance = _director.getErrorTolerance();
        double newh = 5.0 * h;

        if (error > tolerance) {
            newh = h * Math.pow(tolerance / error, 1.0 / _ORDER);
        }

        if (_isDebugging()) {
            _debug("integrator: " + integrator.getName()
                    + " suggests next step size = " + newh);
        }

        return newh;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the current round.
     *  @return The current round.
     */
    @Override
    protected int _getRound() {
        return _roundCount;
    }

    /** Get the current round factor. If the
     *  step is finished, then return 1.0.
     */
    @Override
    protected final double _getRoundTimeIncrement() {
        return _TIME_INCREMENTS[_roundCount];
    }

    /** Return true if the current integration step is finished.
     *  This method will return true if _incrementRound() has been
     *  called 6 or more times since _reset().
     *  @see #_reset()
     */
    @Override
    protected final boolean _isStepFinished() {
        return _roundCount >= _TIME_INCREMENTS.length;
    }

    /** Reset the solver, indicating to it that we are starting an
     *  integration step. This method resets the round counter.
     */
    @Override
    protected final void _reset() {
        _roundCount = 0;
    }

    /** Set the round for the next integration step.
     *  @param round The round for the next integration step.
     */
    @Override
    protected void _setRound(int round) {
        _roundCount = round;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The ratio of time increments within one integration step. */
    protected static final double[] _TIME_INCREMENTS = { 0.2, 0.3, 0.6, 1.0,
        0.875, 1.0, 1.0 };

    /** B coefficients */
    private static final double[][] _B = {
        { 0.2 },
        { 3.0 / 40, 9.0 / 40 },
        { 0.3, -0.9, 1.2 },
        { -11.0 / 54, 5.0 / 2, -70.0 / 27, 35.0 / 27 },
        { 1631.0 / 55296, 175.0 / 512, 575.0 / 13824, 44275.0 / 110592,
            253.0 / 4096 },
            { 37.0 / 378, 0.0, 250.0 / 621, 125.0 / 594, 0.0, 512.0 / 1771 } };

    /** E coefficients */
    private static final double[] _E = { 37.0 / 378 - 2825.0 / 27648, 0.0,
        250.0 / 621 - 18575.0 / 48384, 125.0 / 594 - 13525.0 / 55296,
        0.0 - 277.0 / 14336, 512.0 / 1771 - 0.25 };

    /** The index of the error stored in the auxiliary variables. */
    private static final int _ERROR_INDEX = _TIME_INCREMENTS.length;

    /** The order of the algorithm. */
    private static final int _ORDER = 5;

    /** The round counter. */
    private int _roundCount = 0;
}
