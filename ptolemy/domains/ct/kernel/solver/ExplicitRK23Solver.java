/* Explicit variable step size Runge-Kutta 2(3) ODE solver.

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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.solver;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import java.util.Iterator;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ExplicitRK23Solver
/**
This class implements the Explicit Runge-Kutta 2(3) ODE solving method.
For an ODE with form: <BR>
dx/dt = f(x, t) <BR>
it does the following:<BR>
K0 = f(x(n), tn);<BR>
K1 = f(x(n)+0.5*h*K0, tn+0.5*h);<BR>
K2 = f(x(n)+0.75*h*K1, tn+0.75*h);<BR>
x(n+1) = x(n)+(2/9)*h*K0+(1/3)*h*K0+(4/9)*h*K2;<BR>
For error control:
K3 = f(x(n+1), tn+h); <BR>
LTE = h*[(-5.0/72.0)*K0 + (1.0/12.0)*K1 + (1.0/9.0)*K2 + (-1.0/8.0)*K3]<BR>
<P>
If the LTE is less than the error tolerance, then this step is considered
successful, and the next integration step is predicted as:
h' = 0.8*Math.pow((ErrorTolerance/LTE), 1.0/3.0)
<P>
@author  Jie Liu
@version $Id$
*/
public class ExplicitRK23Solver extends ODESolver{


    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ExplicitRK23Solver() {
        super();
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex.toString());
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
    public ExplicitRK23Solver(Workspace workspace) {
        super(workspace);
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return 4. Four auxiliary variables are needed for this solver.
     *  @return 4.
     */
    public final int getIntegratorAuxVariableCount() {
        return 4;
    }

    /** This method is delegated to the fire() method of the integrator.
     *  It implements the formula in the class document.
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException If no director.
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
            //derivative at t;
            double k0 = (integrator.getHistory(0))[1];
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

    /** Integrator calculate potential state and test for local
     *  truncation error.
     *  @param integrator The integrator of that calls this method.
     *  @return True if the intergrator report a success on the last step.
     */
    public boolean integratorIsSuccessful(CTBaseIntegrator integrator) {
        try {
            CTDirector dir = (CTDirector)getContainer();
            double errtol = dir.getErrorTolerance();
            double h = dir.getCurrentStepSize();
            double f = ((DoubleToken)integrator.input.get(0)).doubleValue();
            integrator.setTentativeDerivative(f);
            double[] k = integrator.getAuxVariables();
            double lte = h * Math.abs(k[0]*_E[0] + k[1]*_E[1]
                    + k[2]*_E[2] + f* _E[3]);
            //k[3] is Local Truncation Error
            integrator.setAuxVariables(3, lte);
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

    /** This method is delegated to the predictedStepSize() method of the
     *  integrator.
     *  @param integrator The integrator of that calls this method.
     *  @return The suggested next step by the given integrator.
     */
    public double integratorPredictedStepSize(CTBaseIntegrator integrator) {
        CTDirector dir = (CTDirector)getContainer();
        double lte = (integrator.getAuxVariables())[3];
        double h = dir.getCurrentStepSize();
        double errtol = dir.getErrorTolerance();
        double newh = 5.0*h;
        if(lte>dir.getValueResolution()) {
            newh = h* Math.max(0.5, 0.8*Math.pow((errtol/lte), 1.0/_order));
        }
        _debug("integrator: " + integrator.getName() +
                " suggests next step size = " + newh);
        return newh;
    }

    /**  Return true always.
     *  Resolve the state of the integrators at time currentTime
     *  CurrentTime+CurrentStepSize. It gets the state transition
     *  schedule from the scheduler and fire for one iteration,
     *  (which consists of 4 rounds.
     *
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public boolean resolveStates() throws IllegalActionException {
        _debug(getFullName() + ": resolveState().");
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
        Iterator actors;
        // for the first iteration after a breakpoint, create the history.
        if(dir.isBPIteration()) {
            if(dir.STAT) {
                dir.NFUNC ++;
            }
            actors = sch.scheduledDynamicActorList().iterator();
            while(actors.hasNext()) {
                CTDynamicActor next = (CTDynamicActor)actors.next();
                _debug(getFullName() + ": Build integrator history"
                        +((Nameable)next).getName());
                next.emitTentativeOutputs();
            }
            actors = sch.scheduledStateTransitionActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + ": Build integrator history..."
                        +((Nameable)next).getName());
                next.fire();
            }
        }
        for (int i = 0; i < _timeInc.length; i++) {
            if(dir.STAT) {
                dir.NFUNC ++;
            }
            actors = sch.scheduledDynamicActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " firing..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            dir.setCurrentTime(dir.getCurrentTime()+
                    dir.getCurrentStepSize()*_timeInc[i]);
            actors = sch.scheduledStateTransitionActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " firing..."+((Nameable)next).getName());
                next.fire();
            }
            incrRound();
        }
        if(dir.STAT) {
            // for error control.
            dir.NFUNC ++;
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //name
    private static final String _DEFAULT_NAME = "CT_Runge_Kutta_2_3_Solver";

    // time increase value.
    private static final double[] _timeInc = {0.5, 0.25, 0.25};

    // B coefficients
    private static final double[][] _B = {{0.5},
                                          {0, 0.75},
                                          {2.0/9.0, 1.0/3.0, 4.0/9.0}};
    // E coefficients
    private static final double[] _E =
    {-5.0/72.0, 1.0/12.0, 1.0/9.0, -1.0/8.0};

    // order.
    private static final double _order = 3;
}
