/* Explicit variable step size Runge-Kutta 2(3) ODE solver.

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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (liuxj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.solver;

import ptolemy.actor.Actor;
import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTDynamicActor;
import ptolemy.domains.ct.kernel.CTSchedule;
import ptolemy.domains.ct.kernel.CTScheduler;
import ptolemy.domains.ct.kernel.ODESolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

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

@author  Jie Liu
@version $Id$
@since Ptolemy II 0.2
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
            throw new InternalErrorException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return 0 to indicate that no history information is needed
     *  by this solver.
     *  @return 0.
     */
    public final int getHistoryCapacityRequirement() {
        return 0;
    }

    /** Return 4 to indicate that four auxiliary variables are
     *  needed by this solver.
     *  @return 4.
     */
    public final int getIntegratorAuxVariableCount() {
        return 4;
    }

    /** Provide the fire() method for integrators under this solver.
     *  This performs the ODE solving algorithm.
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
        int r = getRound();
        double xn =  integrator.getState();
        double outvalue;
        double h = dir.getCurrentStepSize();
        double[] k = integrator.getAuxVariables();
        switch (r) {
        case 0:
            // Get the derivative at t;
            double k0 = integrator.getDerivative();
            integrator.setAuxVariables(0, k0);
            outvalue = xn + h * k0 *_B[0][0];
            break;
        case 1:
            double k1 = ((DoubleToken)integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(1, k1);
            outvalue = xn + h * (k[0]*_B[1][0] + k1 *_B[1][1]);
            break;
        case 2:
            double k2 = ((DoubleToken)integrator.input.get(0)).doubleValue();
            integrator.setAuxVariables(2, k2);
            outvalue = xn + h * (k[0]*_B[2][0] + k[1]*_B[2][1]
                    + k2*_B[2][2]);
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
            CTDirector dir = (CTDirector)getContainer();
            double tolerance = dir.getErrorTolerance();
            double h = dir.getCurrentStepSize();
            double f = ((DoubleToken)integrator.input.get(0)).doubleValue();
            integrator.setTentativeDerivative(f);
            double[] k = integrator.getAuxVariables();
            double error = h * Math.abs(k[0]*_E[0] + k[1]*_E[1]
                    + k[2]*_E[2] + f* _E[3]);
            //k[3] is Local Truncation Error
            integrator.setAuxVariables(3, error);
            _debug("Integrator: "+ integrator.getName() +
                    " local truncation error = " + error);
            if (error < tolerance) {
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

    /** Provide the  predictedStepSize() method for the integrators
     *  under this solver. It uses the algorithm in the class comments
     *  to predict the next step size based on the current estimation
     *  of the local truncation error.
     *
     *  @param integrator The integrator of that calls this method.
     *  @return The next step size suggested by the given integrator.
     */
    public double integratorPredictedStepSize(CTBaseIntegrator integrator) {
        CTDirector dir = (CTDirector)getContainer();
        double error = (integrator.getAuxVariables())[3];
        double h = dir.getCurrentStepSize();
        double tolerance = dir.getErrorTolerance();
        double newh = 5.0*h;
        if (error > dir.getValueResolution()) {
            newh = h*
                Math.max(0.5, 0.8*Math.pow((tolerance/error), 1.0/_order));
        }
        _debug("integrator: " + integrator.getName() +
                " suggests next step size = " + newh);
        return newh;
    }

    /** Advance the current time by the current step size, and
     *  resolve the state of the integrators at that time.
     *  This method always returns true since this class implements
     *  an explicit method. xIt gets the state transition
     *  schedule from the scheduler and fire for one iteration,
     *  (which consists of 4 rounds).
     *
     * @exception IllegalActionException If there is no director,
     *  no scheduler, or one of the actors throw it in its fire()
     *  method or emitTentativeOutput() method.
     */
    public boolean resolveStates() throws IllegalActionException {
        _debug(getFullName() + ": resolveState().");
        CTDirector dir = (CTDirector)getContainer();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " must have a CT director.");
        }
        CTScheduler scheduler = (CTScheduler)dir.getScheduler();
        if (scheduler == null) {
            throw new IllegalActionException( dir,
                    " must have a scheduler to fire.");
        }
        CTSchedule schedule = (CTSchedule)scheduler.getSchedule();
        resetRound();
        Iterator actors;
        // for the first iteration after a breakpoint, create the history.
        if (dir.isBreakpointIteration()) {
            actors = schedule.get(CTSchedule.DYNAMIC_ACTORS).actorIterator();
            while (actors.hasNext()) {
                CTDynamicActor next = (CTDynamicActor)actors.next();
                _debug(getFullName() + ": Build integrator history"
                        +((Nameable)next).getName());
                next.emitTentativeOutputs();
            }
            actors = schedule.get(
                    CTSchedule.STATE_TRANSITION_ACTORS).actorIterator();
            while (actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _prefireIfNecessary(next);
                _debug(getFullName() + ": Build integrator history..."
                        +((Nameable)next).getName());
                next.fire();
            }
        }
        for (int i = 0; i < _timeInc.length; i++) {
            actors = schedule.get(CTSchedule.DYNAMIC_ACTORS).actorIterator();
            while (actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " firing..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            dir.setCurrentTime(dir.getCurrentTime()+
                    dir.getCurrentStepSize()*_timeInc[i]);
            actors = schedule.get(CTSchedule.STATE_TRANSITION_ACTORS).
                actorIterator();
            while (actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _prefireIfNecessary(next);
                _debug(getFullName(), " firing... ",
                        ((Nameable)next).getName());
                next.fire();
            }
            incrementRound();
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The name of the solver
    private static final String _DEFAULT_NAME = "CT_Runge_Kutta_2_3_Solver";

    // The ratio of time increments within one integration step.
    private static final double[] _timeInc = {0.5, 0.25, 0.25};

    // B coefficients
    private static final double[][] _B = {{0.5},
                                          {0, 0.75},
                                          {2.0/9.0, 1.0/3.0, 4.0/9.0}};
    // E coefficients
    private static final double[] _E =
    {-5.0/72.0, 1.0/12.0, 1.0/9.0, -1.0/8.0};

    // The order of the algorithm.
    private static final double _order = 3;
}
