/* The Impulse Backward Euler Solver

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
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.solver;

import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Nameable;
import ptolemy.actor.Actor;
import ptolemy.domains.ct.kernel.*;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ImpulseBESolver
/**
This class implements the impulse backward Euler ODE solver. This solver
uses two backward Euler solving processes to handle Dirac Delta functions
in a CT system. The first backward Euler process has a positive
step size, h, and the second backward Euler process has a negative step
size, -h, where h is the minimum step size.
<P>
By using this solver, we can find the state of the system at t+, which is
the time at which the impulse occurs, but the effect of the impulse is
taken care of.
<P>
This ODE solver does not advance time. So it can only be used as
a breakpointSolver.

@author  Jie Liu
@version $Id$
*/
public class ImpulseBESolver extends BackwardEulerSolver 
    implements BreakpointODESolver {

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  The name of the solver is set to "CT_ImpulseBE_Solver".
     */
    public ImpulseBESolver() {
        this(null);
    }

    /** Construct a solver in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The solver is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The name of the solver is set to "CT_ImpulseBE_Solver".
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public ImpulseBESolver(Workspace workspace) {
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

    /** Perform two successive backward Euler ODE solving method, with
     *  step size h and -h, respectively, where h is the minimum
     *  step size.
     *  @return True if the state is successfully resolved.
     */
    public boolean resolveStates() throws IllegalActionException {
        CTDirector dir = (CTDirector) getContainer();
        dir.setCurrentStepSize(dir.getMinStepSize());
        if (super.resolveStates()) {

            Iterator actors = ((CTScheduler)dir.getScheduler()
                               ).scheduledDynamicActorList().iterator();
            while(actors.hasNext()) {
                Actor next = (Actor)actors.next();
                _debug(getFullName() + "update..."+((Nameable)next).getName());
                next.postfire();
            }

            dir.setCurrentStepSize(-dir.getCurrentStepSize());
            if (super.resolveStates()) {
                dir.setCurrentStepSize(-dir.getCurrentStepSize());
                return true;
            }else {
                return false;
            }
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The static name.
    private static final String _DEFAULT_NAME="CT_ImpulseBE_Solver" ;

}
