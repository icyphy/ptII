/* Base class for fixed step size (no error control) ODE solvers.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.ODESolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FixedStepSolver
/**
   Abstract base class for fixed step size ODE solvers, which provide no
   error control. It provide base implementation for some methods that
   are shared by all fixed-step-size solvers.

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public abstract class FixedStepSolver extends ODESolver {
    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public FixedStepSolver() {
        super();
    }

    /** Construct a solver in the given workspace with a null string name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public FixedStepSolver(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire dynamic actors. Advance the model time to the current model time
     *  plus the current step size.
     *  @throws IllegalActionException If thrown in the super class or
     *  the model time can not be set.
     */
    public void fireDynamicActors() throws IllegalActionException {
        // First assume that the states will converge after this round.
        // If any integrator does not agree, the final converged status may be
        // changed via calling _voteForConverged() by that integrator.
        _setConverged(true);
        super.fireDynamicActors();
        CTDirector dir = (CTDirector)getContainer();
        if (_getRoundCount() == 0) {
            // At the first round, advance the time with the current step size.
            dir.setModelTime(dir.getModelTime().add(dir.getCurrentStepSize()));
        }
    }

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

    /** Return true always, since no error control is performed.
     *  @see ptolemy.domains.ct.kernel.CTStepSizeControlActor#isThisStepAccurate
     *  @param integrator The integrator that wants to do the test.
     *  @return True always.
     */
    public final boolean integratorIsAccurate(CTBaseIntegrator integrator) {
        return true;
    }

    /** Return the current step size of the director, since no step
     *  size control is performed.
     *  @see ptolemy.domains.ct.kernel.CTStepSizeControlActor#predictedStepSize
     *  @param integrator The integrator that want to predict the step size.
     *  @return The current step size of the director.
     */
    public final double integratorPredictedStepSize(
            CTBaseIntegrator integrator) {
        CTDirector dir = (CTDirector) getContainer();
        return dir.getCurrentStepSize();
    }
}
