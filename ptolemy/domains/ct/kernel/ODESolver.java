/* The abstract base class of the solvers for ODEs.

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
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// ODESolver
/**
Abstract base class for ODE solvers. The key method for the class is
resolveState(), which executes the integration method for one
step. This method does not consider any breakpoint affect. In general,
resolveState() will resolve the integrators' new states.
Step size control (including error control) is performed by the
director, but solvers may provide support for it.
How the states are resolved and how the errors are controlled are
solver dependent.  Derived classes
may implement these methods according to individual ODE
solving algorithm.
<P>
The behavior of the integrators also changes
when changing ODE solver, so this class provides the some methods
for the integrators too, including the fire() method, and the step size
control related methods. CTBaseIntegrator delegated its corresponding
methods to this class.
<P>
An integer called "round" is used to indicate the number of firing rounds
within one iteration. For some integration method, (the so called explicit
methods) the round of firings are fixed. For some others (called implicit
methods), the round could be arbitrary integer.
<P>
 Round counter is a counter
for the number of fire() rounds in one iteration to help the actors that
may behaves differently under different round. The round can be get by
the getRound() method. The incrRound() method will increase the counter by one,
and resetRound() will always reset the counter to 0.
<P>
Conceptually, ODE solvers do not maintain simulation parameters,
they get these parameters from the director. So the same set of parameters
are shared by all the solvers in a simulation.
@author Jie Liu
@version $Id$
*/
public abstract class ODESolver extends NamedObj {

    /** Construct a solver in the default workspace with an empty
     *  string as name. The solver is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ODESolver() {
        super();
    }

    /** Construct a solver in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this solver.
     *  @exception IllegalActionException If the name has a period.
     */
    public ODESolver(String name) throws IllegalActionException {
        super(name);
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
     *  The director is added to the list of objects in the workspace.
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

    /** Return the director contains this solver.
     *  @return the director contains this solver.
     */
    public final Nameable getContainer() {
        return _container;
    }

    /** Return the round counter record. Round counter will be increased
     *  for each round the state transition schedule is fired.
     *
     *  @return The round of firing the state transition schedule.
     */
    public int getRound() {
        return _round;
    }

    /** Increase the round counter by one.
     */
    public void incrRound() {
        _round ++ ;
    }

    /** Abstract method returns the number of auxiliary variable number
     *  needed by the
     *  integrators when solving the ODE.
     *  @return The number of auxiliary variables
     */
    public abstract int getIntegratorAuxVariableCount();

    /** Abstract method returns the number of history information needed
     *  by this solver.
     *  @return The number of history information needed.
     */
    public abstract int getHistoryCapacityRequirement();

    /** The fire method of the integrator is delegated to this method.
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException Not thrown in this base class.
     *  May be needed by the derived class.
     */
    public abstract void integratorFire(CTBaseIntegrator integrator)
            throws  IllegalActionException;

    /** The isThisStepSuccessful() method of the integrator is delegated to
     *  this method.
     *  @param integrator The integrator of that calls this method.
     *  @return True if the intergrator report a success on the last step.
     */
    public abstract boolean integratorIsSuccessful(CTBaseIntegrator
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
     *  Different solver may implement it differently.
     *
     * @exception IllegalActionException Not thrown in this base class.
     * May be needed by the derived class.
     */
    public abstract boolean resolveStates() throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this solver to be the solver of the given Director. This method
     *  should not be called directly. Call the _instantiateODESolver()
     *  or the setCurrentODESolver()
     *  method of the director instead.
     *
     *  @param dir The CT director
     */
    protected void _makeSolverOf(Director dir) {
        _container = dir;
        if (dir != null) {
            workspace().remove(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The CT director that contains this solver.
    private Director _container = null;
    // The round counter.
    private int _round = 0;

}
