/* Base class for fixed step size (no error control) ODE solvers.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.solver;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// FixedStepSolver
/**
Base class for fixed step size (no error control) ODE solvers.
@version $Id$
@see classname
@see full-classname
*/
public abstract class FixedStepSolver extends ODESolver{
    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public FixedStepSolver() {
        super();
    }

    /** Construct a solver in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this solver.
     */
    public FixedStepSolver(String name) {
        super(name);
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
    public FixedStepSolver(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Hook method for isSuccess() method of integrators.
     *  Always returns true, since not error control.
     *  @return True always.
     */
    public final boolean integratorIsSuccessful(CTBaseIntegrator integrator) {
        return true;
    }

    /** Hook method for suggestedNextStepSize() method of integrators.
     *  Always returns 0 since no error control functionality.
     *  @return 0 always.
     */
    public final double integratorPredictedStepSize(
         CTBaseIntegrator integrator){
        CTDirector dir = (CTDirector) getContainer();
        return dir.getCurrentStepSize();
    }
}
