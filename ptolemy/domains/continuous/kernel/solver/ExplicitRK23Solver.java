/* Explicit variable step size Runge-Kutta 2(3) ODE solver.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
 K3 = f(x(n+1), tn+h);
 </pre>,
 and error control:
 <pre>
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

 @author  Jie Liu, Haiyang Zheng, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class ExplicitRK23Solver extends ContinuousODESolver {

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
        double outputValue;
        double xn = integrator.getState();
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
                        * _E[3]);

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

    /** Provide the predictedStepSize() method for the integrators
     *  under this solver. It uses the algorithm in the class comments
     *  to predict the next step size based on the current estimation
     *  of the local truncation error.
     *
     *  @param integrator The integrator of that calls this method.
     *  @return The next step size suggested by the given integrator.
     */
    @Override
    public double integratorSuggestedStepSize(ContinuousIntegrator integrator) {
        double error = integrator.getAuxVariables()[_ERROR_INDEX];
        double h = _director.getCurrentStepSize();
        double tolerance = _director.getErrorTolerance();
        double newh = 5.0 * h;

        if (error > tolerance) {
            newh = 0.8 * Math.pow(tolerance / error, 1.0 / _ORDER);
            if (newh > h) {
                newh = 0.5 * h;
            }
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

    /** Get the current round factor.
     */
    @Override
    protected final double _getRoundTimeIncrement() {
        return _TIME_INCREMENTS[_roundCount];
    }

    /** Return true if the current integration step is finished.
     *  This method will return true if _incrementRound() has been
     *  called 4 or more times since _reset().
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
    ////                         protected variables               ////

    /** The ratio of time increments within one integration step. */
    protected static final double[] _TIME_INCREMENTS = { 0.5, 0.75, 1.0, 1.0 };

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** B coefficients. */
    private static final double[][] _B = { { 0.5 }, { 0, 0.75 },
            { 2.0 / 9.0, 1.0 / 3.0, 4.0 / 9.0 } };

    /** E coefficients. */
    private static final double[] _E = { -5.0 / 72.0, 1.0 / 12.0, 1.0 / 9.0,
            -1.0 / 8.0 };

    /** The index of the error stored in the auxiliary variables. */
    private static final int _ERROR_INDEX = _TIME_INCREMENTS.length;

    /** The order of the algorithm. */
    private static final int _ORDER = 3;

    /** The round counter. */
    private int _roundCount = 0;
}
