/* The abstract base class of the ODE solvers.

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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (yuhong@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ODESolver
/**
Abstract base class for ODE solvers. The key method for the class is
resolveState(), which executes the integration method for one
step. This method does not consider any breakpoints. In general,
resolveState() will resolve the integrators' new states.
Step size control (including error control) is performed by the
director, but solvers may provide support for it.
How the states are resolved and how the errors are controlled are
solver dependent.  Derived classes
may implement these methods according to individual ODE
solving algorithms.
<P>
The behavior of integrators also changes
when changing the ODE solver, so this class provides some methods
for the integrators too, including the fire() method and the step size
control related methods. Here we use the strategy and delegation design
patterns. CTBaseIntegrator delegated its corresponding
methods to this class. And subclasses of this class provide concrete
implementations of these methods.
<P>
An integer called "round" is used to indicate the number of firing rounds
within one iteration. For some integration methods, (i.e. the so called
explicit methods) the round of firings are fixed.
For some others (i.e. implicit methods), the round could be an arbitrary
positive integer.
<P>
A round counter is a counter
for the number of fire() rounds in one iteration to help the actors that
may behave differently under different rounds. The round can be got by
the getRound() method. The incrementRound() method will increase the
counter by one, and resetRound() will always reset the counter to 0.
<P>
Conceptually, ODE solvers do not maintain simulation parameters,
like step sizes and error tolerance.
They get these parameters from the director. So the same set of parameters
are shared by all the solvers in a simulation.
@author Jie Liu
@version $Id$
@since Ptolemy II 0.2
*/
public abstract class ODESolver extends NamedObj {

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ODESolver() {
        super();
    }

    /** Construct a solver in the given workspace with a null string name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public ODESolver(Workspace workspace) {
        super(workspace);
    }

    /** Construct a solver in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The solver is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this solver.
     *  @exception IllegalActionException If the name has a period.
     */
    public ODESolver(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the director that contains this solver.
     *  @return the director that contains this solver.
     */
    public final NamedObj getContainer() {
        return _container;
    }

    /** Return the number of history information needed by this solver.
     *  Some solvers need history information from each integrator.
     *  The derived class should implement this method to return the
     *  number of history information needed so that the integrator can
     *  prepare for that in advance.
     *  @return The number of history information needed.
     */
    public abstract int getHistoryCapacityRequirement();

    /** Return the number of auxiliary variables that an integrator should
     *  provide when solving the ODE. Auxiliary variables are variables
     *  in integrators to store integrator-dependent intermediate results
     *  when solving an ODE.
     *  @return The number of auxiliary variables.
     */
    public abstract int getIntegratorAuxVariableCount();

    /** Return the round counter record.
     *
     *  @return The round of firing the state transition schedule.
     */
    public int getRound() {
        return _round;
    }

    /** Increase the round counter by one. In general, the round counter
     *  will be increased
     *  for each round the state transition schedule is fired.
     */
    public void incrementRound() {
        _round ++ ;
    }

    /** The fire() method of integrators is delegated to this method.
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException Not thrown in this base class.
     *  May be needed by the derived class.
     */
    public abstract void integratorFire(CTBaseIntegrator integrator)
            throws  IllegalActionException;

    /** The isThisStepAccurate() method of integrators is delegated to
     *  this method. It returns true if the current integration step
     *  is accurate from the argument integrator's point of view.
     *  @param integrator The integrator of that calls this method.
     *  @return True if the integrator finds the step accurate.
     */
    public abstract boolean integratorIsAccurate(CTBaseIntegrator
            integrator);

    /** The predictedStepSize() method of the integrator is delegated
     *  to this method.
     *  @param integrator The integrator of that calls this method.
     *  @return The suggested next step by the given integrator.
     */
    public abstract double integratorPredictedStepSize(
            CTBaseIntegrator integrator);

    /** Reset the round counter to 0.
     */
    public void resetRound() {
        _round = 0;
    }

    /** Return true if the state of the system is resolved successfully.
     *  Different solvers may implement it differently. Implementations
     *  of this method will fire STATE_TRANSITION_ACTORS and
     *  DYNAMIC actors.
     *
     * @exception IllegalActionException Thrown in derived classes if
     * the exception is thrown by * one of the execution methods of some
     * actors.
     */
    public abstract boolean resolveStates() throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this solver to be the solver of the given Director. This method
     *  should not be called directly. The solvers will be instantiated
     *  by the director according to the parameters.
     *
     *  @param dir The CT director
     */
    protected void _makeSolverOf(Director dir) {
        _container = dir;
        if (dir != null) {
            workspace().remove(this);
        }
    }

    /** If the specified actor has not be prefired() in the current
     *  iteration, then prefire() it.
     *  @param actor The actor to prefire().
     *  @exception IllegalActionException If the actor returns false.
     */
    protected void _prefireIfNecessary(Actor actor)
            throws IllegalActionException {
        CTDirector dir = (CTDirector)getContainer();
        if (!dir.isPrefireComplete(actor)) {
            _debug(getFullName()
                    + " is prefiring: "
                    + ((Nameable)actor).getName());
            dir.setPrefireComplete(actor);
            if (!actor.prefire()) {
                throw new IllegalActionException((Nameable)actor,
                        "Expected prefire() to return true!\n"
                        + "Perhaps a continuous input is being driven by a "
                        + "discrete output?");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The CT director that contains this solver.
    private Director _container = null;
    // The round counter.
    private int _round = 0;
}
