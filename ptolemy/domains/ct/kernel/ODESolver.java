/* The abstract base class of the solvers for ODEs.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// ODESolver
/**
Abstract base class for ODE solvers. The key method for the class is 
proceedOneStep(), which executes the integration method for one successful
step. This method does not consider any breakpoint affect. In general,
proceedOneStep() will first resolver the integrators' new states,
then perform error control. If the step is not successful, then the step 
size will be reduced and try again. How the states are resolved and
how the errors are controlled are method dependant.  Derived class
may implement these methods according to individual ODE solving methods.
CTDirectors can switch their solver at each iteration
of simulation seamlessly. 
<P>
The behavior of the integrators also changes
when changing ODE solver, so this class provides the some methods
for the integrators too, including the fire() method, and the error
contol related methods. Even for one integration method, the integrator's
fire() method may depends on the round of fires.  Round counter is a counter
for the number of fire() rounds in one iteration to help the actors that
may behaves differently under different round. The round can be get by
the getRound() method. IncrRound method will increase the counter by one,
and resetRound() will always reset the counter to 0.
<P>
Conceptually, ODE solvers do not maintain simulation parameters,
but they get these parameters from the director. So a set of parameters
are shared by all the switchable solvers.
@author Jie Liu
@version $Id$
*/
public abstract class ODESolver extends NamedObj {

    public static final boolean VERBOSE = false;
    public static final boolean DEBUG = false;

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
     */
    public ODESolver(String name) {
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
    public ODESolver(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Abstract method, which should return true if the local error
     *  in the last step is tolerable.
     *  This is the AND of all the error control actor's response.
     *  @return true if the local error in the last step is tolerable.
     */
    public abstract boolean errorTolerable();

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

    /** Abstarct method returns the number of auxilary variable number
     *  needed by the
     *  integrators when solving the ODE.
     *  @return The number of auxilary variables 
     */
    public abstract int integratorAuxVariableNumber();

    /** Abstract fire() method for integrators.
     *
     *  @param integrator The integrator of that calls this method.
     *  @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public abstract void integratorFire(CTBaseIntegrator integrator)
            throws  IllegalActionException;

    /** Abstract isSuccess() method for integrators.
     *  @param integrator The integrator of that calls this method.
     *  @return True if the intergrator report a success on the last step.
     */
    public abstract boolean integratorIsSuccess(CTBaseIntegrator integrator);

    /** Abstract suggestedNextStepSize() method for integrators.
     *  @param integrator The integrator of that calls this method.
     *  @return The suggested next step by the given integrator.
     */
    public abstract double integratorSuggestedNextStepSize(
        CTBaseIntegrator integrator);

    /** Solver the ODE for one successful step. In this default 
     *  implementation, the method will try to resolve the states
     *  of the system for the given step size. If it is not succeeded,
     *  the the step will be restarted by startOverLastStep() method.
     *  Different solver may interprete "success" and implement
     *  it differently. 
     *
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public void proceedOneStep() throws IllegalActionException {
        while(true) {
            resolveStates();
            if(errorTolerable()){
                resolveNextStepSize();
                break;
            }
            startOverLastStep();
        }
    }

    /** Reset the round counter to 0.
     */
    public void resetRound() {
        _round = 0;
    }

    /** Abstract method for resolveing the next step size if the current
     *  step is a success.
     *  Different solver may implement it differently.
     *
     */
    public abstract void resolveNextStepSize();

    /** Abstract method for resolving the new states of the integrators.
     *  Different solver may implement it differently.
     *
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public abstract void resolveStates() throws IllegalActionException;

    /** Abstract method for restarting the last integration step with a
     *  smaller step size.
     *  The typical operations incolved in this method are reset
     *  the currentTime and halve the currentStepSize of the director.
     */
    public abstract void startOverLastStep();


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this solver to be the solver of a CTDirector. This method
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
