/* The Forward Euler ODE solver.

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
import ptolemy.domains.ct.kernel.CTSchedule;
import ptolemy.domains.ct.kernel.CTScheduler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ForwardEulerSolver
/**
The Forward Euler(FE) ODE solver. For ODE
<pre>
    dx/dt = f(x, u, t), x(0) = x0;
</pre>
The FE method approximate the x(t+h) as:
<pre>
    x(t+h) =  x(t) + h * f(x(t), u(t), t)
</pre>
No error control and step size control is performed. This is the
simplest algorithm for solving an ODE. It is a first order method,
and has stability problem for some systems.

@author  Jie Liu
@version $Id$
@since Ptolemy II 0.2
*/
public class ForwardEulerSolver extends FixedStepSolver {

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  The name of the solver is set to "CT_Forward_Euler_Solver".
     */
    public ForwardEulerSolver() {
        this(null);
    }

    /** Construct a solver in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  The name of the solver is set to "CT_Forward_Euler_Solver".
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public ForwardEulerSolver(Workspace workspace) {
        super(workspace);
        try {
            setName(_DEFAULT_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(this, ex, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return 0 to indicate that this solver needs no history information.
     *  @return 0.
     */
    public int getHistoryCapacityRequirement() {
        return 0;
    }

    /** Return 1 to indicate that this solver needs one auxiliary variable
     *  from each integrator when solving the ODE.
     *  @return 1.
     */
    public int getIntegratorAuxVariableCount() {
        return 1;
    }

    /** Perform the fire() method for the argument integrator. This implements
     *  the ODE solving algorithm as shown in the class comment.
     *
     *  @param integrator The integrator that calls this method.
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
        integrator.setTentativeState(tentativeState);
        integrator.setTentativeDerivative(f);

        integrator.output.broadcast(new DoubleToken(tentativeState));
    }


    /** Advance the current time by the current step size and resolve
     *  the state of the system. This method always returns true to
     *  indicate that the state is found accurately.
     *  This gets the state transition
     *  schedule from the scheduler and fire for one iteration
     *  (which consists of 1 round). This method only resoles the
     *  tentative state. It is the director's job to update the
     *  states.
     *
     * @return True.
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
        CTScheduler scheduler = (CTScheduler)dir.getScheduler();
        if (scheduler == null) {
            throw new IllegalActionException( dir,
                    " must have a director to fire.");
        }
        CTSchedule schedule = (CTSchedule)scheduler.getSchedule();
        resetRound();
        Iterator actors = schedule.get(
                CTSchedule.STATE_TRANSITION_ACTORS).actorIterator();
        while (actors.hasNext()) {
            Actor next = (Actor)actors.next();
            _prefireIfNecessary(next);
            _debug(getFullName() + " is firing..."+((Nameable)next).getName());
            next.fire();
        }
        actors = schedule.get(
                CTSchedule.DYNAMIC_ACTORS).actorIterator();
        while (actors.hasNext()) {
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
