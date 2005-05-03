/* Explicit variable step size Runge-Kutta 4(5) ODE solver.

Copyright (c) 2004-2005 The Regents of the University of California.
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
   K5 = f(x(n) + (1631/55296*K0 + 175/512*K1 + 575/13824*K2 + 3544275/110592*K3
   + 253/4096*K4)*h, tn + 7/8*h);
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
   step size. A round counter is used to record which step this solver performs. 

   @author  Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (hyzheng)
*/
public class ExplicitRK45Solver extends ODESolver {
    /** Construct a solver in the default workspace.
     *  The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  The name of the solver is set to "CT_Runge_Kutta_4_5_Solver".
     */
    public ExplicitRK45Solver() {
        this(null);
    }

    /** Construct a solver in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The name of the solver is set to "CT_Runge_Kutta_4_5_Solver".
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public ExplicitRK45Solver(Workspace workspace) {
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
     *  the current step size.
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
        director.setModelTime(iterationBeginTime.add(
                                      currentStepSize
                                      * _timeInc[_getRoundCount()]));
    }

    /** Fire state transition actors. Increment the round count.
     *  If the current round is the last (sixth) round, set converged flag to
     *  true indicating the fixed-point states have been reached. Reset
     *  the round count if the current round is the last round.
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

    /** Return 7 to indicate that 7 auxiliary variables are
     *  needed by this solver.
     *  @return 7.
     */
    public final int getIntegratorAuxVariableCount() {
        return 7;
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
        double outputValue;
        double h = director.getCurrentStepSize();
        double[] k = integrator.getAuxVariables();

        switch (r) {
        case 0:

            // Get the derivative at t;
            double k0 = integrator.getDerivative();
            integrator.setAuxVariables(0, k0);
            outputValue = xn + (h * k0 * _B[0][0]);
            break;

        case 1:

            double k1 = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(1, k1);
            outputValue = xn + (h * ((k[0] * _B[1][0]) + (k1 * _B[1][1])));
            break;

        case 2:

            double k2 = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(2, k2);
            outputValue = xn
                + (h * ((k[0] * _B[2][0]) + (k[1] * _B[2][1]) + (k2 * _B[2][2])));
            break;

        case 3:

            double k3 = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(3, k3);
            outputValue = xn
                + (h * ((k[0] * _B[3][0]) + (k[1] * _B[3][1])
                           + (k[2] * _B[3][2]) + (k3 * _B[3][3])));
            break;

        case 4:

            double k4 = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(4, k4);
            outputValue = xn
                + (h * ((k[0] * _B[4][0]) + (k[1] * _B[4][1])
                           + (k[2] * _B[4][2]) + (k[3] * _B[4][3])
                           + (k4 * _B[4][4])));
            break;

        case 5:

            double k5 = ((DoubleToken) integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(5, k5);
            outputValue = xn
                + (h * ((k[0] * _B[5][0]) + (k[1] * _B[5][1])
                           + (k[2] * _B[5][2]) + (k[3] * _B[5][3])
                           + (k[4] * _B[5][4]) + (k5 * _B[5][5])));
            integrator.setTentativeState(outputValue);
            break;

        default:
            throw new InvalidStateException(this,
                    "execution sequence out of range.");
        }

        integrator.output.broadcast(new DoubleToken(outputValue));
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
            double error = h * Math.abs((k[0] * _E[0]) + (k[1] * _E[1])
                    + (k[2] * _E[2]) + (k[3] * _E[3])
                    + (k[4] * _E[4]) + (k[5] * _E[5]));

            //store the Local Truncation Error into k[6]
            integrator.setAuxVariables(6, error);
            if (_debugging) {
                _debug("Integrator: " + integrator.getName()
                        + " local truncation error = " + error);
            }
            if (error < tolerance) {
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
        } catch (IllegalActionException e) {
            //should never happen.
            throw new InternalErrorException(this, e, 
                    integrator.getName() + " can't read input.");
        }
    }

    /** Predict the next step size for the integrators executed under this
     *  solver. It uses the algorithm in the class comments
     *  to predict the next step size based on the current estimation
     *  of the local truncation error.
     *
     *  @param integrator The integrator that calls this method.
     *  @return The next step size suggested by the given integrator.
     */
    public double integratorPredictedStepSize(CTBaseIntegrator integrator) {
        CTDirector director = (CTDirector) getContainer();
        double error = (integrator.getAuxVariables())[6];
        double h = director.getCurrentStepSize();
        double tolerance = director.getErrorTolerance();
        double newh = 5.0 * h;

        if (error > director.getValueResolution()) {
            newh = h * Math.pow((tolerance / error), 1.0 / _order);
        }

        if (_debugging) {
            _debug("integrator: " + integrator.getName()
                    + " suggests next step size = " + newh);
        }
        return newh;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the solver */
    private static final String _DEFAULT_NAME = "CT_Runge_Kutta_4_5_Solver";

    /** The ratio of time increments within one integration step. */
    private static final double[] _timeInc = {
        0.2,
        0.3,
        0.6,
        1.0,
        0.875,
        1.0
    };

    /** B coefficients */
    private static final double[][] _B = {
        {
            0.2
        },
        {
            3.0 / 40,
            9.0 / 40
        },
        {
            0.3,
            -0.9,
            1.2
        },
        {
            -11.0 / 54,
            5.0 / 2,
            -70.0 / 27,
            35.0 / 27
        },
        {
            1631.0 / 55296,
            175.0 / 512,
            575.0 / 13824,
            44275.0 / 110592,
            253.0 / 4096
        },
        {
            37.0 / 378,
            0.0,
            250.0 / 621,
            125.0 / 594,
            0.0,
            512.0 / 1771
        }
    };

    /** E coefficients */
    private static final double[] _E = {
        (37.0 / 378) - (2825.0 / 27648),
        0.0,
        (250.0 / 621) - (18575.0 / 48384),
        (125.0 / 594) - (13525.0 / 55296),
        0.0 - (277.0 / 14336),
        (512.0 / 1771) - 0.25
    };

    /** The order of the algorithm. */
    private static final int _order = 5;
}
