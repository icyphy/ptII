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
//// CTODESolver
/** 
Abstract base class for ODE solvers. This class hooks the method in one
iteration in the CTDirector, ie. prefire(), fire(), and postfire().
The idea is that CTDirector can switch its solver at each iteration
of simulation seamlessly. The behavior of the integrators also changes
when changing ODE solver, so this class provides the action methods
for the integrators too.
Conceptually, ODE solvers do not maintain simulation parameters, 
but they get these parameters from the director. So a set of parameters
are shared by all the switchable solvers.
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

    /** Abstract method hooks the fire() method of director.
     *  Different solver may implement it differently.
     * 
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public abstract void resolveStates() throws IllegalActionException;

    /** Increase the round counter by one. Round counter is a counter
     *  for the number of fire() rounds in one iteration. Some 
     *  CTActors (like the integrator) may behaves differently under
     *  different round. The round can be get by the round() method.
     *  And resetRound() will always reset the counter to 0.
     */
    public void incrRound() {
        _round ++ ;
    }

    /** Reset the round counter to 0.
     */
    public void resetRound() {
        _round = 0;
    }

    /** Return the round counter record.
     * 
     *  @return The round of fire().
     */
    public int round() {
        return _round;
    }

    /** Abstract fire() method for integrators.
     *
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public abstract void integratorFire(CTBaseIntegrator integrator)
            throws  IllegalActionException;

    /** Abstract hook method for isSuccess() method of integrators.
     */
    public abstract boolean integratorIsSuccess(CTBaseIntegrator integrator);

    /** Abstract hook method for suggestedNextStepSize() method of 
     *  integrators.
     */
    public abstract double integratorSuggestedNextStepSize(
        CTBaseIntegrator integrator);

    /** Integrator's aux variable number needed when solving the ODE.
     */
    public abstract int integratorAuxVariableNumber();

    /** Run the CT subsystem for one successful step.
     *  Different solver may interprete "success" and implement
     *  it differently.
     * 
     * @exception IllegalActionException Not thrown in this base
     *  class. May be needed by the derived class.
     */
    public void iterate() throws IllegalActionException {
        while(true) {
            resolveStates();
            if(errorTolerable()){
                break;
            }
            startOverLastStep();
        }        
    }

    public void startOverLastStep() {
        CTDirector dir = (CTDirector)getContainer();
        dir.setCurrentStepSize(dir.getCurrentStepSize()/2.0);
    }

    /** Error Control, may label this step to be unsuccessful.
     */
    public boolean errorTolerable() {
        return true;
    }
    
    /** Return the director contains this solver.
     *  @return the director contains this solver.
     */
    public final Nameable getContainer() {
        return _container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this solver to be the solver of a CTDirector. This method
     *  should not be called directly. Call the setCurrentODESolver()
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
    private int _round = 0;

}
