/* The Backward Euler ODE solver.

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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.Actor;
import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.kernel.*;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// BackwardEulerSolver
/**
The backward Euler ODE solver. For an ODE
<pre>
    x'=f(x, t), x(0)=x0
</pre>
This solver uses the following formula to solve it:
<pre>
    x(t+h) = x(t) + h*x'(t+h)
</pre>
where x(t) is the previous state, x(t+h) is the current (to be resolved)
state, h is the step size, and x'(t+h) is the derivative of x(t+h).
The formula above is an algebraic equation, and this method uses fixed
point iteration to solve it.
<P>
This method does not perform step size control other than reducing the 
step sizes when the fixed-point iteration does not converge.

@author Jie Liu
@version $Id$
*/
public class BackwardEulerSolver extends FixedStepSolver
    implements ImplicitMethodSolver{

    /** Construct a solver in the default workspace with the
     *  name "CT_Backward_Euler_Solver".
     *  The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public BackwardEulerSolver() {
        super();
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException e) {
            // this should never happen.
            throw new InternalErrorException(e.toString());
        }
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
            throw new InternalErrorException(e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return 1. The integrator only need one auxiliary variable.
     * 
     *  @return 1.
     */
    public int getIntegratorAuxVariableCount() {
        return 1;
    }

    /** Return 0. No history information is needed.
     *  @return 0.
     */
    public int getHistoryCapacityRequirement() {
        return 0;
    }

    /** Provide the fire() method for the integrators under this solver.
     *  For the given integrator, do x(n+1) = x(n)+h*x'(n+1). Test if this
     *  calculation converge for this integrator and report it to the
     *  solver by calling voteForConvergence().
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public void integratorFire(CTBaseIntegrator integrator)
            throws IllegalActionException {
        CTDirector dir = (CTDirector)getContainer();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " must have a CT director.");
        }
        double f = ((DoubleToken)integrator.input.get(0)).doubleValue();
        double pstate = integrator.getState() + f*(dir.getCurrentStepSize());
        double cerror = Math.abs(pstate-integrator.getTentativeState());
        if( !(cerror < dir.getValueResolution())) {
            voteForConvergence(false);
        }
        integrator.setTentativeState(pstate);
        integrator.setTentativeDerivative(f);

        integrator.output.broadcast(new DoubleToken(pstate));
    }

    /** Return true if the overall vote result is true.
     *  @return True if all the votes are true.
     */
    public boolean isConverged() {
        return _converge;
    }

    /** Resolve the state of the integrators at time
     *  CurrentTime+CurrentStepSize. It gets the state transition
     *  schedule from the scheduler and fire until the fixed point.
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
        dir.setCurrentTime(dir.getCurrentTime()+dir.getCurrentStepSize());
        Iterator actors = sch.scheduledDynamicActorList().iterator();
        while(actors.hasNext()) {
            CTDynamicActor next = (CTDynamicActor)actors.next();
            _debug(getFullName() + " Guessing..."+((Nameable)next).getName());
            next.emitTentativeOutputs();
        }
        _setConverge(false);
        int iterations = 0;
        while(!isConverged()) {
            if(dir.STAT) {
                dir.NFUNC ++;
            }
            _setConverge(true);
            actors = sch.scheduledStateTransitionActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " Firing..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            actors = sch.scheduledDynamicActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + " Refiring..."+
                        ((Nameable)next).getName());
                next.fire();
            }
            if(iterations++ > dir.getMaxIterations()) {
                return false;
            }
        }
        return true;
    }

    /** Vote for whether a fixed point has reached. The final result 
     *  is the <i>and</i> of all votes.
     *  
     *  @param converge True if vote for convergence.
     */
    public void voteForConvergence(boolean converge) {
        _converge = _converge && converge;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the convergence flag.
     *  @param converge The flag setting.
     */
    protected void _setConverge(boolean converge) {
        _converge = converge;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Name of this Solver. */
    private static final String _DEFAULT_NAME="CT_Backward_Euler_Solver" ;

    /** @serial True if all the votes are true. */
    private boolean _converge;
}
