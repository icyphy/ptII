/* Base class for fixed step size (no error control) ODE solvers.

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
package ptolemy.domains.hs.kernel.solver;

import ptolemy.domains.hs.kernel.HSBaseIntegrator;
import ptolemy.domains.hs.kernel.HSDirector;
import ptolemy.domains.hs.kernel.ODESolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FixedStepSolver

/**
 Abstract base class for fixed step size ODE solvers, which provides no
 error control. It provides base implementation for some methods that
 are shared by all fixed-step-size solvers.

 @author Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public abstract class FixedStepSolver extends ODESolver {
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

    /** Fire all continuous actors. Derived classes may advance the model time. 
     *  The amount of time increment depends on the solving algorithms.
     *  @exception IllegalActionException If schedule can not be found or
     *  continuous actors throw it from their fire() methods.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        HSDirector director = (HSDirector) getContainer();
        double currentStepSize = director.getCurrentStepSize();
        if (currentStepSize != 0) {
            // At the first round, advance the time with the current step size.
            // Skip this operation if step size is 0.
            director.setModelTime(director.getModelTime().add(currentStepSize));
        }
        _setConverged(true);
    }
        
    /** Return 0 to indicate that no history information is needed by
     *  this solver.
     *  @return 0.
     */
    public int getAmountOfHistoryInformation() {
        return 0;
    }

    /** Return 0 to indicate that an integrator under this solver needs
     *  no auxiliary variable.
     *  @return 0.
     */
    public int getIntegratorAuxVariableCount() {
        return 0;
    }

    /** Return true always, since no error control is performed.
     *  @param integrator The integrator that wants to do the test.
     *  @return True always.
     */
    public final boolean integratorIsAccurate(HSBaseIntegrator integrator) {
        return true;
    }

    /** Return the current step size of the director.
     *  @see ptolemy.domains.hs.kernel.StepSizeControlActor#predictedStepSize
     *  @param integrator The integrator that want to predict the step size.
     *  @return The current step size of the director.
     */
    public final double integratorPredictedStepSize(HSBaseIntegrator integrator) {
        HSDirector director = (HSDirector) getContainer();
        return director.getCurrentStepSize();
    }
}
