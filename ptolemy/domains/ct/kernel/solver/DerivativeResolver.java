/* Resolve the derivative of the current state.

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
import ptolemy.domains.ct.kernel.BreakpointODESolver;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTDynamicActor;
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
//// DerivativeResolver
/**
This solver finds the derivatives of the state variables of an ODE
with respect to the current time.
For example, if the ODE is
<pre>
    x' = f(x, t),
</pre>
the current time is t0, and
<pre>
    x(t0) = x0,
</pre>
then this method calculates
<pre>
    x'(t0) = f(x(t0), t0).
</pre>

<P>
The derivative is obtained by firing the system for one iteration.
This is used for preparing the history for other methods.
Note that time does not progress under this solver.
So, this class implements BreakpointODESolver and can only be
used as a breakpoint solver.
It assumes that state variables are continuous after the breakpoint.
This may not be true if there are impulses in the system.
In that case, use ImpulseBESolver as the breakpoint solver for a
better result.

@author Jie Liu
@version $Id$
@since Ptolemy II 0.4
*/
public class DerivativeResolver extends ODESolver
    implements BreakpointODESolver {

    /** Construct a solver in the default workspace with the name
     *  "CT_Derivative_Resolver". The solver is added to the list of
     *  objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public DerivativeResolver() {
        this(null);
    }

    /** Construct a solver in the given workspace with the name
     *  "CT_Derivative_Resolver".
     *  If the workspace argument is null, use the default workspace.
     *  The solver is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public DerivativeResolver(Workspace workspace) {
        super(workspace);
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException e) {
            // this should never happen.
            throw new InternalErrorException(e.toString());
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

    /** Return 0 to indicate that the solver needs no auxiliary variable.
     *  @return 0.
     */
    public final int getIntegratorAuxVariableCount() {
        return 0;
    }

    /** Provides the fire() method for the given integrator.
     *  This remembers the input token, and use it for x'(t).
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException Not thrown in this base class.
     *  May be needed by the derived class.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        integrator.setTentativeState(integrator.getState());
        integrator.setTentativeDerivative(
                ((DoubleToken)integrator.input.get(0)).doubleValue());
    }

    /** Return true, since there is no step size control.
     *
     *  @param integrator The integrator of that calls this method.
     *  @return True always.
     */
    public boolean integratorIsAccurate(CTBaseIntegrator
            integrator) {
        return true;
    }

    /** Return the initial step size of the director. Since this solver
     *  is always used as the breakpoint solver, the next integration
     *  step will use the initial step size.
     *
     *  @param integrator The integrator of that calls this method.
     *  @return The initial step size.
     */
    public double integratorPredictedStepSize(
            CTBaseIntegrator integrator){
        CTDirector dir = (CTDirector)getContainer();
        return dir.getInitialStepSize();
    }

    /** Resolve the derivative of the integrators at the
     *  current time. It gets the state transition
     *  schedule from the scheduler and fire for one iteration.
     *
     * @exception IllegalActionException Not thrown in this base class
     *  May be needed by the derived class.
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
        dir.setCurrentStepSize(0.0);
        dir.setSuggestedNextStepSize(dir.getInitialStepSize());
        Iterator actors = schedule.get(CTSchedule.DYNAMIC_ACTORS).
            actorIterator();
        while (actors.hasNext()) {
            CTDynamicActor next = (CTDynamicActor)actors.next();
            _debug(getFullName() + " Guessing..."+((Nameable)next).getName());
            next.emitTentativeOutputs();
        }
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
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Name of this Solver. */
    private static final String _DEFAULT_NAME="CT_Derivative_Resolver" ;
}
