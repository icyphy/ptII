/* The Backward Euler ODE solver.

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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// BackwardEulerSolver

/**
   The Backward Euler ODE solver. For an ODE
   <pre>
   x' = f(x, u, t), x(0) = x0
   </pre>
   This solver uses the following formula to solve it:
   <pre>
   x(t+h) = x(t) + h*x'(t+h)
   </pre>
   where x(t) is the current state, x(t+h) is the next
   state, h is the step size, and x'(t+h) is the derivative of x at t+h.
   The formula above is an algebraic equation, and this solver uses fixed
   point iteration to solve it.
   <P>
   This solver does not perform step size control even when the states do not
   converge after the maximum number of iterations is reached. However, CT
   directors may try to reduce step size. This solver does not give suggestions
   on choosing step size.

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (hyzheng)
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
        } catch (KernelException ex) {
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the integrator to resolve states. Vote false for convergence if
     *  a fixed-point solution is not found.
     *
     *  @param integrator The integrator to be fired.
     *  @exception IllegalActionException If there is no director, or can not
     *  read input, or can not send output.
     */
    public void integratorFire(CTBaseIntegrator integrator)
        throws IllegalActionException {
        CTDirector director = (CTDirector) getContainer();
        double tentativeState = integrator.getState();

        if (_getRoundCount() == 0) {
            // During the first round, use the current derivative to predict
            // the states at currentModelTime + currentStepSize. The predicted
            // states are the initial guesses for fixed-point iteration.
            double f = integrator.getDerivative();
            tentativeState = tentativeState
                + (f * (director.getCurrentStepSize()));

            // Set converged to false such that the integrator will be refired
            // again to check convergence of resolved states.
            _voteForConverged(false);
        } else {
            // Not the first round, keep iterating until resolved states
            // converge.
            double f = ((DoubleToken) integrator.input.get(0)).doubleValue();
            tentativeState = tentativeState
                + (f * (director.getCurrentStepSize()));

            double error = Math.abs(tentativeState
                    - integrator.getTentativeState());

            if (error < director.getValueResolution()) {
                integrator.setTentativeDerivative(f);

                // Note that the FixedStepSolver sets converged to true for
                // each round by default. Therefore, we do not need to set it
                // to true again.
            } else {
                _voteForConverged(false);
            }
        }

        integrator.setTentativeState(tentativeState);
        integrator.output.broadcast(new DoubleToken(tentativeState));
    }

    /** Return true if the resolved states have converged. Return false if
     *  states have not converged but the number of iterations reaches the
     *  <i>maxIterations</i> number. Meanwhile, the round count is reset.
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
    private static final String _DEFAULT_NAME = "CT_Backward_Euler_Solver";
}
