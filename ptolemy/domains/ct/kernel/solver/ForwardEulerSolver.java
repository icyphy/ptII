/* The Forward Euler ODE solver.

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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.solver;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import java.util.Iterator;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ForwardEulerSolver
/**
The Forward Euler(FE) ODE solver. For ODE<BR>
    dx/dt = f(x, u, t), x(0) = x0;<BR>
The FE method approximate the x(t+h) as:<BR>
    x(t+h) =  x(t) + h * f(x(t), u(t), t)<BR>
No error control is performed.<BR>
@author  Jie Liu
@version $Id$
*/
public class ForwardEulerSolver extends FixedStepSolver {

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ForwardEulerSolver() {
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
    public ForwardEulerSolver(Workspace workspace) {
        super(workspace);
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return 1 always.
     *  @return 1.
     */
    public final int getIntegratorAuxVariableCount() {
        return 1;
    }

    /** Return 0 always. No history information is needed.
     *  @return 0.
     */
    public final int getHistoryCapacityRequirement() {
        return 0;
    }

    /** This method is delegated to the fire() method of the integrator.
     *  It implements the formula in the class document..
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
        integrator.setTentativeState(pstate);
        integrator.setTentativeDerivative(f);

        integrator.output.broadcast(new DoubleToken(pstate));
    }


    /** Return true always, indicating that the states of the system
     *  is correctly resolved.
     *  The resolved states are at time
     *  CurrentTime+CurrentStepSize. It gets the state transition
     *  schedule from the scheduler and fire for one iteration
     *  (which consists of 1 round).
     *
     * @exception IllegalActionException If the firing of some actors
     *       throw it.
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
        if(dir.STAT) {
            dir.NFUNC++;
        }
        Iterator actors = sch.scheduledStateTransitionActorList().iterator();
        while(actors.hasNext()) {
            Actor next = (Actor)actors.next();
            _debug(getFullName() + " is firing..."+((Nameable)next).getName());
            next.fire();
        }
        actors = sch.scheduledDynamicActorList().iterator();
        while(actors.hasNext()) {
            Actor next = (Actor)actors.next();
            _debug(getFullName() + " is firing..."+((Nameable)next).getName());
            next.fire();
        }
        dir.setCurrentTime(dir.getCurrentTime()+dir.getCurrentStepSize());
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // static name.
    private static final String _DEFAULT_NAME="CT_Forward_Euler_Solver" ;
}
