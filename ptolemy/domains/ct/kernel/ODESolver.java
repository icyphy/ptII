/* The abstract base class of the ODE solvers.

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
package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// ODESolver

/**
   Abstract base class for ODE solvers. The key methods for the class
   are {@link #fireDynamicActors} and {@link
   #fireStateTransitionActors}.  CT directors call these methods to
   resolve the initial states in a future time in the continuous phase
   of exeution of a complete iteration. See {@link
   CTMultiSolverDirector} for explanation of initial states and phases
   of executions. The process of resolving the initial states in a
   future time is also known as an integration. A complete integration
   is composed of one or more rounds of executions. One round of
   execution consists of calling fireDynamicActors() once followed by
   calling fireStateTransitionActors() once. How the states are
   resolved are solver dependent. Derived classes need to implement
   these methods according to their ODE solving algorithms.
   <P>
   The behavior of integrators also changes when changing the ODE solver,
   so this class provides some methods for the integrators too, including the
   fire() method and the step size control related methods. Here we use the
   strategy and delegation design patterns. CTBaseIntegrator delegates its
   corresponding methods to this class. And subclasses of this class provide
   concrete implementations of these methods.
   <P>
   How many rounds are needed in one integration is solver dependent. For some
   solving algorithms, (i.e. the so called explicit methods) the number of
   rounds is fixed. For some others (i.e. implicit methods), the number of
   rounds can not be decided beforehand.
   <P>
   A round counter is a counter for the number of rounds in one integration.
   It helps the solvers to decide how to behave under different rounds.
   The round counter can be retrieved by the _getRoundCount() method.
   The _incrementRoundCount() method will increase the counter by one,
   and _resetRoundCount() will always reset the counter to 0. These methods are
   protected because they are only used by solvers and CT directors.
   <p>
   In this class, two methods {@link #_isConverged} and {@link
   #_voteForConverged} are defined to let CT directors know the status
   of resolved states. If multiple integrators exist, only when all of
   them vote true for converged, will the _isConverged() return
   true. Another related method is {@link #resolveStates()}, which
   always returns true in this base class. However, in the solvers
   that implement the implicit solving methods, this method may return
   false if the maximum number of iterations is reached but states
   have not been resolved.
   <P>
   Conceptually, ODE solvers do not maintain simulation parameters,
   like step sizes and error tolerance.
   They get these parameters from the director. So the same set of parameters
   are shared by all the solvers in a simulation.

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (hyzheng)
*/
public abstract class ODESolver extends NamedObj {
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire dynamic actors. Derived classes may advance the model time. The
     *  amount of time increment depends on the solving algorithms.
     *  @exception IllegalActionException If schedule can not be found or
     *  dynamic actors throw it from their fire() methods.
     */
    public void fireDynamicActors() throws IllegalActionException {
        if (_debugging) {
            _debug(getFullName() + " firing dynamic actors ...");
        }

        CTSchedule schedule = _getSchedule();
        Iterator actors = schedule.get(CTSchedule.DYNAMIC_ACTORS).actorIterator();

        while (actors.hasNext()) {
            Actor next = (Actor) actors.next();

            if (_debugging) {
                _debug("  firing..." + ((Nameable) next).getFullName());
            }

            next.fire();
        }
    }

    /** Fire state transition actors. See {@link CTScheduler} for explanation
     *  of state transition actors. Derived classes may increse the round count.
     *  @exception IllegalActionException If schedule can not be found or
     *  state transition actors throw it from their fire() methods.
     */
    public void fireStateTransitionActors() throws IllegalActionException {
        if (_debugging) {
            _debug(getFullName() + " firing state transition actors ...");
        }

        CTSchedule schedule = _getSchedule();
        Iterator actors = schedule.get(CTSchedule.STATE_TRANSITION_ACTORS)
                                              .actorIterator();

        while (actors.hasNext()) {
            Actor next = (Actor) actors.next();
            _prefireIfNecessary(next);

            if (_debugging) {
                _debug("  firing..." + ((Nameable) next).getFullName());
            }

            next.fire();
        }
    }

    /** Return the amount of history information needed by this solver.
     *  Some solvers need history information from each integrator.
     *  The derived class should implement this method to return the
     *  number of history information needed so that the integrator can
     *  prepare for that in advance. In particular, if a solver needs no
     *  history information, this method returns 0.
     *  @return The amount of history information needed.
     */
    public abstract int getAmountOfHistoryInformation();

    /** Return the director that contains this solver.
     *  @return the director that contains this solver.
     */
    public final NamedObj getContainer() {
        return _director;
    }

    /** Return the number of auxiliary variables that an integrator should
     *  provide when solving the ODE. Auxiliary variables are variables
     *  in integrators to store integrator-dependent intermediate results
     *  when solving an ODE.
     *  <br>
     *  For example, the fixed-step solvers need 0 auxiliary variable, but
     *  the RK23 solver needs 4 auxiliary variables to store the temporary
     *  derivatives at different time points during an integration.
     *  @return The number of auxiliary variables.
     */
    public abstract int getIntegratorAuxVariableCount();

    /** Perfrom one integration step. The fire() method of integrators
     *  delegates to this method. Derived classes need to implement
     *  the details.
     *  @param integrator The integrator that calls this method.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void integratorFire(CTBaseIntegrator integrator)
        throws IllegalActionException;

    /** Return true if the current integration step is accurate from the
     *  argument integrator's point of view. The integratorIsAccurate() method
     *  of integrators delegates to this method.
     *  Derived classes need to implement the details.
     *  @param integrator The integrator that calls this method.
     *  @return True if the integrator finds the step accurate.
     */
    public abstract boolean integratorIsAccurate(CTBaseIntegrator integrator);

    /** The predictedStepSize() method of the integrator delegates to this
     *  method. Derived classes need to implement the details.
     *  @param integrator The integrator that calls this method.
     *  @return The suggested next step size by the given integrator.
     */
    public abstract double integratorPredictedStepSize(
        CTBaseIntegrator integrator);

    /** Return true if the states of the system have been resolved
     *  successfully.
     *  In this base class, always return true. Derived classes may change
     *  the returned value.
     *  @return True If states of the system have been resolved sucessfully.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean resolveStates() throws IllegalActionException {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the number of the round counter.
     *  @return The number of the round counter.
     */
    protected int _getRoundCount() {
        return _roundCount;
    }

    /** Get the current schedule.
     *  @return The current schedule.
     *  @exception IllegalActionException If this solver is not contained by
     *  a CT director, or the director does not have a scheduler.
     */
    protected CTSchedule _getSchedule() throws IllegalActionException {
        CTDirector director = (CTDirector) getContainer();

        if (director == null) {
            throw new IllegalActionException(this, " must have a CT director.");
        }

        CTScheduler scheduler = (CTScheduler) director.getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException(director,
                " does not contain a valid scheduler.");
        }

        return (CTSchedule) scheduler.getSchedule();
    }

    /** Increase the round counter by one. In general, the round counter
     *  will be increased for each time the state transition actors are fired.
     */
    protected void _incrementRoundCount() {
        _roundCount++;
    }

    /** Return true if all integrators agree that the current states have
     *  converged to a fixed point.
     *  @return Return true if all integrators agree that the current states
     *  have converged to a fixed point.
     */
    protected boolean _isConverged() {
        return _isConverged;
    }

    /** Make this solver the solver of the given Director. This method
     *  should only be called by CT directors, when they instantiate solvers
     *  according to the ODESolver parameters.
     *  @param director The CT director that contains this solver.
     */
    protected void _makeSolverOf(CTDirector director) {
        _director = director;

        if (director != null) {
            workspace().remove(this);
        }
    }

    /** If the specified actor has not been prefired in the current
     *  iteration, then prefire it.
     *  @param actor The actor to be prefired.
     *  @exception IllegalActionException If the actor returns false from the
     *  prefire method.
     */
    protected void _prefireIfNecessary(Actor actor)
        throws IllegalActionException {
        CTDirector director = (CTDirector) getContainer();

        if (!director.isPrefireComplete(actor)) {
            if (_debugging) {
                _debug(getFullName() + " is prefiring: "
                    + ((Nameable) actor).getName());
            }

            if (!actor.prefire()) {
                throw new IllegalActionException((Nameable) actor,
                    "Expected prefire() to return true!\n"
                    + "Perhaps a continuous input is being driven by a "
                    + "discrete output?");
            }

            director.setPrefireComplete(actor);
        }
    }

    /** Reset the round counter to 0. This method is called when either the
     *  fixed-point solution of states has been found or the current integration
     *  fails to find the fixed-point solution within the maximum number of
     *  rounds.
     */
    protected void _resetRoundCount() {
        _roundCount = 0;
    }

    /** Set a flag to indicate whether the fixed point of states has been
     *  reached. Solvers and CT directors may call this method to
     *  change the convergence.
     *  <p>
     *  This method should not be called by individual integrators.
     *  If an integrator thinks the states have not converged, it should call
     *  the _voteForConverged() method, which influences the convergence of the
     *  solver.
     *  @param converged The flag setting.
     *  @see #_voteForConverged
     */
    protected void _setConverged(boolean converged) {
        _isConverged = converged;
    }

    /** An integrator calls this method to vote whether a fixed point has been
     *  reached. The final result is the logic <i>and</i> of votes from all
     *  integrators. This method is particularly designed for integrators and
     *  it should be called from the integratorFire() method.
     *  Solvers and CT directors should use _setConverged() instead.
     *  @param converged True if vote for convergence.
     *  @see #integratorFire
     *  @see #_setConverged
     */
    protected void _voteForConverged(boolean converged) {
        _setConverged(converged && _isConverged());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The CT director that contains this solver.
    private CTDirector _director = null;

    // The flag indicating whether the fixed point of states has been reached.
    // The default value is false.
    private boolean _isConverged = false;

    // The round counter.
    private int _roundCount = 0;
}
