/* The Backward Euler ODE solver.

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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// BackwardEulerSolver
/**
The backward Euler ODE solver. For an ODE
<pre>
    x' = f(x, t), x(0) = x0
</pre>
This solver uses the following formula to solve it:
<pre>
    x(t+h) = x(t) + h*x'(t+h)
</pre>
where x(t) is the current state, x(t+h) is the next
state, h is the step size, and x'(t+h) is the derivative of x at t+h.
The formula above is an algebraic equation, and this method uses fixed
point iteration to solve it.
<P>
This method does not perform step size control other than reducing the
step sizes when the fixed-point iteration does not converge.

@author Jie Liu
@version $Id$
@since Ptolemy II 0.2
*/
public class BackwardEulerSolver extends FixedStepSolver {

    /** Construct a solver in the default workspace with the
     *  name "CT_Backward_Euler_Solver".
     *  The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public BackwardEulerSolver() {
        this(null);
    }

    /** Construct a solver in the given workspace with the name
     *  "CT_Backward_Euler_Solver".
     *  If the workspace argument is null, use the default workspace.
     *  The solver is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public BackwardEulerSolver(Workspace workspace) {
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

    /** Return 0 to indicate that no history information is needed by
     *  this solver.
     *  @return 0.
     */
    public int getHistoryCapacityRequirement() {
        return 0;
    }

    /** Return 1 to indicate that an integrator under this solver needs
     *  one auxiliary variable.
     *  @return 1.
     */
    public int getIntegratorAuxVariableCount() {
        return 1;
    }

    /** Provide the fire() method for the integrator under this solver.
     *  For the given integrator, do x(n+1) = x(n)+h*x'(n+1) and test
     *  whether this calculation converges.
     *
     *  @param integrator The integrator that uses the fire() method.
     *  @exception IllegalActionException If there is no director.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        CTDirector dir = (CTDirector)getContainer();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " must have a CT director.");
        }
        double f = ((DoubleToken)integrator.input.get(0)).doubleValue();
        double tentativeState =
            integrator.getState() + f*(dir.getCurrentStepSize());
        double error = Math.abs(tentativeState-integrator.getTentativeState());
        if ( !(error < dir.getValueResolution())) {
            _voteForConvergence(false);
        }
        integrator.setTentativeState(tentativeState);
        integrator.setTentativeDerivative(f);

        integrator.output.broadcast(new DoubleToken(tentativeState));
    }


    /** Advance time by the current step size and return true if
     *  the state of the integrators are resolved accurately at that time.
     *  It resolves the states by getting the state transition
     *  schedule from the scheduler and trying to iteration until
     *  the fixed point is reached. Return false if the fixed point
     *  is not reached after <i>maxIterations</i> number of iterations.
     *  This method only resoles the
     *  tentative state. It is the director's job to update the
     *  states.
     *  @exception IllegalActionException If there is no director or
     *  no scheduler.
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
                    " must have a director to fire.");
        }
        CTSchedule schedule = (CTSchedule)scheduler.getSchedule();

        resetRound();
        dir.setCurrentTime(dir.getCurrentTime()+dir.getCurrentStepSize());
        Iterator actors = schedule.get(
                CTSchedule.DYNAMIC_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTDynamicActor next = (CTDynamicActor)actors.next();
            _debug(getFullName(), " ask ", ((Nameable)next).getName(),
                    " to emit tentative output");
            next.emitTentativeOutputs();
        }
        _setConvergence(false);
        int iterations = 0;
        while (!_isConverged()) {
            _setConvergence(true);
            incrementRound();
            actors = schedule.get(
                    CTSchedule.STATE_TRANSITION_ACTORS).actorIterator();
            while (actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _prefireIfNecessary(next);
                _debug(getFullName() + " Firing..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            actors = schedule.get(CTSchedule.DYNAMIC_ACTORS).actorIterator();
            while (actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " Refiring..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            if (iterations++ > dir.getMaxIterations()) {
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if all integrators agree that the fixed-point
     *  iteration has converged. Integrators vote for convergence
     *  by calling the _voteForConvergence() with a true arguments.
     *  And the value returned here is the <i>AND</i> of all votes.
     *  @return True if all the votes are true.
     */
    protected boolean _isConverged() {
        return _converge;
    }

    /** Set the convergence flag.
     *  @param converge The flag setting.
     */
    protected void _setConvergence(boolean converge) {
        _converge = converge;
    }

    /** Vote on whether a fixed point has reached.
     *  @param converge True if vote for convergence.
     */
    protected void _voteForConvergence(boolean converge) {
        _converge = _converge && converge;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Name of this Solver. */
    private static final String _DEFAULT_NAME="CT_Backward_Euler_Solver" ;

    /** @serial True if all the votes are true. */
    private boolean _converge;
}
