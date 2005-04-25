/* A CT director that utilizes multiple ODE solvers.

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
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// CTMultiSolverDirector

/**
   A CTDirector that uses multiple ODE solvers. The reason for using different
   solvers is that we need to handle both normal integration of ODEs over a
   time interval and abrupt changes in signals (or actors' functions) that
   happen at discrete time points.
   <p>
   At the time points where abrupt changes happen, a special ODE solver, called
   <i>breakpointODESolver</i>, is used. Typically, breakpointODESolver does not
   advance time. The job for a breakpointODESolver is to find the states of
   the system at a breakpoint. Usually, the system has more than one state at
   such time points, which is also known as discontinuities. We call the first
   state at a discontinuity the <i>initial</i> state, and the last state the
   <i>final</i> state. Therefore, a breakpointODESolver is responsible to
   resolve the final state of a discontinuity from the inputs and the initial
   states.
   <p>
   The following paper gives a detailed explanation of initial and final states
   and how the initial and final states are resolved.
   <ul>
   <li>Edward A. Lee and Haiyang Zheng, <a href=
   "http://ptolemy.eecs.berkeley.edu/publications/papers/05/OperationalSemantics">
   <i>Operational Semantics of Hybrid Systems</i>,
   Invited paper in Hybrid Systems: Computation and Control: 8th International
   Workshop, HSCC, LNCS 3414, Zurich, Switzerland, March 9-11, 2005</a></ul>
   <p>
   This director handles both predictable breakpoints, whose appearance can be
   assured before reaching the time points they happen, and unpredictable
   breakpoints, whose appearance is unknown before the simulation passes the
   time points they happen.
   <P>
   This director can only be a top-level director. For a CT model as
   an opaque composite actor inside another model, use
   CTMixedSignalDirector (if the outer model is a discrete-event
   model) or CTEmbeddedDirector (if the outer model is a CT model or a Modal
   model with a HSDirector.)
   <P>
   This director recognizes actors that implement the CTStepSizeControlActor
   interface and adjusts the step size by polling such actors. If all actors
   are content with the current step size, then it attempts to raise the
   step size. If any actor is not satisfied with the current step size, then
   this director reduces the step size. A special case is that if there are
   no CT step size control actors, then this director uses 5 times of the
   current step size or the maximum step size, whichever is smaller.
   <P>
   This director has two more parameters than the CTDirector base class.<BR>
   <UL>
   <LI><I>ODESolver</I>: The name of the ODE solver that is used
   to integrate ODEs over a time interval.
   <LI><I>breakpointODESolver</I>: The name of the ODE solver used
   at breakpoints. The breakpoint ODE solvers do not need history information
   (this property is called self-start). Currently, there is only one such
   solver available,
   {@link ptolemy.domains.ct.kernel.solver.DerivativeResolver}.
   </UL>

   @see ptolemy.domains.ct.kernel.CTDirector
   @author  Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class CTMultiSolverDirector extends CTDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTMultiSolverDirector() {
        super();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in derived classes.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTMultiSolverDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTMultiSolverDirector(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The class name of the ODE solver that is used in the breakpoint
     *  iterations. The default value is a string:
     *  "ptolemy.domains.ct.kernel.solver.DerivativeResolver".
     */
    public Parameter breakpointODESolver;

    /** The class name of the normal ODE solver used in iterations for
     *  normal integration. The default value is a string:
     *  "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver".
     */
    public Parameter ODESolver;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  In particular, if the <i>ODESolver</i> parameter is changed, then
     *  the corresponding solver will be instantiated. If the new ODEsolver
     *  can not be instantiated, an IllegalActionException will be thrown, and
     *  the original ODESolver will be unchanged.
     *  @param attribute The changed attribute.
     *  @exception IllegalActionException If the new solver can not be
     *  instantiated, or the change to other attributes is invalid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == ODESolver) {
            if (_debugging) {
                _debug(getFullName() + " updating ODE solver...");
            }

            String newSolverClassName = ((StringToken) ODESolver.getToken()).stringValue()
                .trim();

            if (newSolverClassName.trim().startsWith(_solverClasspath)) {
                // The old solver name is a parameter starts with
                // "ptolemy.domains.ct.kernel.solver."
                _normalSolverClassName = newSolverClassName;
            } else {
                _normalSolverClassName = _solverClasspath + newSolverClassName;
            }

            ODESolver newODESolver = _instantiateODESolver(_normalSolverClassName);

            if (newODESolver instanceof BreakpointODESolver) {
                throw new IllegalActionException(this,
                        _normalSolverClassName
                        + " can only be used as a breakpoint ODE solver.");
            }

            _normalSolver = newODESolver;
            _setCurrentODESolver(_normalSolver);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return false always, since this director cannot be an inside director.
     *  @return false.
     */
    public boolean canBeInsideDirector() {
        return false;
    }

    /** Return true since this director can be a top-level director.
     *  @return true.
     */
    public boolean canBeTopLevelDirector() {
        return true;
    }

    /** Establish the initial states for discrete phase of execution. This
     *  method should be called if the initial states are not available. For
     *  example, the first iteration of the simulation, or the first execution
     *  of an enabled CT refinement.
     *  @exception IllegalActionException If any actor can not be iterated, or
     *  can not ODE solver can not be set.
     */
    public void establishInitialStates() throws IllegalActionException {
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();

        if (_debugging) {
            _debug("Establish the initial states for "
                    + "discrete phase of execution");
            _debug("  ---> " + getName(),
                    ": iterating waveform generators (discrete -> continuous)");
        }

        _iterateWaveformGenerators(schedule);

        if (_debugging) {
            _debug("  ---> " + getName(),
                    ": using a breakpoint solver, find integrator output "
                    + "values and iterate continuous actors to find all "
                    + "continuous-time signal values.");
        }

        _propagateResolvedStates();

        if (_debugging) {
            _debug("  ---> " + getName(),
                    ": iterating event generators (continuous -> discrete)");
        }

        _iterateEventGenerators(schedule);

        if (_debugging) {
            _debug("  ---> " + getName(),
                    ": iterating purely discrete actors (discrete -> discrete)");
        }

        _iteratePurelyDiscreteActors(schedule);
    }

    /** Fire the system for one iteration. One iteration is defined as
     *  solving the states of a system over a time interval [t_0, t_1].
     *  A complete iteration includes resolving final states at t_0,
     *  resolving the initial states at t_1, and producing outputs. This
     *  process includes a discrete phase of execution and a continuous phase
     *  of execution.
     *  <P>
     *  To resolve the final states at the time point t_0, a discrete
     *  phase of execution is performed. A discrete phase of execution
     *  at a time point is a fixed-point iteration, where the fixed
     *  point is reached when no more events exist and there will be
     *  no more events to be generated at that time point. To be
     *  concrete, at a discrete phase execution, event generators,
     *  purely discrete actors, waveform generators, and continuous
     *  actors are repeatedly iterated. The discrete phase of execution
     *  stops only when no event generators generate any more events. At the
     *  ending of the execution, the system states are resolved,
     *  which are called the final states at t_0. The solver for the discrete
     *  phase of execution is a breakpoint ODE solver.
     *  <P>
     *  The way we find the fixed point is based on the synchronous reactive
     *  semantics. To be specific, the directors resolve the value of each
     *  signal from <i>unknown</i> to be either <i>absent</i> or
     *  <i>present</i>. This design is simple but firing all actors at each
     *  breakpoint causes overhead.
     *  <p>
     *  We could have used a smarter event queue like the one used by the DE
     *  director. Therefore only those actors with trigger events will be
     *  executed. However, this design will increase the complexity of this
     *  class. We assume that a CT model does not have many DE actors inside.
     *  If the discrete part of the model is complicated with a lot of DE
     *  actors, we would suggest using an opaque DE composite actor to
     *  encapsulate these DE actors and associate the composite actor with a
     *  DE director to take charge of the execution of those DE actors.
     *  <p>
     *  A continuous phase of execution immediately follows a discrete one,
     *  which solves the initial states at t_1. The initial states at t_1 are
     *  resolved by a normal ODE solver. This process is a normal integration
     *  over a time interval.
     *  <p>
     *  The ending time point t_1 of an iteration is determined by the step
     *  sizes suggested by the step size control actors and the earliest entry
     *  in the breakpoint table. A correct step size is no greater than the
     *  smallest suggested step size, and the current time plus the step size
     *  should not be later than the first entry in the breakpoint table.
     *  <p>
     *  Because of the existence of unpredictable events, a step size
     *  may need to be refined. Another reason to adjust step size is
     *  to achieve a reasonably accurate approximation of states. The
     *  mechanism to control step size is described below. After the
     *  states are resolved, step size control actors in the dynamic
     *  actor schedule and the state transition schedule are queried
     *  for the accuracy of the current step size. If any one of them
     *  is not satisfied with the current step size, then the states
     *  will be recalculated with a refined step size, which is the
     *  minimum of the refined step sizes from all step size control
     *  actors in the dynamic actor schedule and the state transition
     *  schedule. On the other hand, if all the above actors are
     *  satisfied with the current step, then the actors in the output
     *  path will be fired according to the output schedule. Then, the
     *  step size control actors in the output path will be checked
     *  for accuracy. If any actor is not satisfied with the current
     *  step size, the current step size is refined. Note that states
     *  have to be resolved again with this new step size. States are
     *  completely resolved only when all actors agree that the step size
     *  is accurate.
     *
     *  @exception IllegalActionException If thrown in discrete or continuous
     *  phase of execution.
     */
    public void fire() throws IllegalActionException {
        if (_debugging && _verbose) {
            _debug(getName(), " fire: <<< ");
        }

        // A complete iteration consists of a discrete phase of execution
        // and a continuous phase of execution.
        // The discrete phase of execution resolves the final states at the
        // current time by processing discrete events according to the SR
        // semantics.
        _discretePhaseExecution();

        // If the current time is the stop time, then the fire method
        // should immediately return because no further execution is necessary.
        // NOTE: the final states at the model stop time are resolved before
        // the model stops.
        // Also, if there is a stop request, stop the model immediately.
        if (getModelTime().equals(getModelStopTime()) || _stopRequested) {
            return;
        }

        // The continuous phase execution resolves the initial states
        // in some future time by normal integration.
        _continuousPhaseExecution();

        if (_debugging && _verbose) {
            _debug(getName(), " end of fire. >>>");
        }
    }

    /** Fire event generators. This method is only called in a continuous
     *  phase of execution. Note that event generators are only fired but not
     *  postfired in this method.
     *  @exception IllegalActionException If the schedule does not exist,
     *  or any actor can not be prefired, or any actor throws it during firing.
     */
    public void fireEventGenerators() throws IllegalActionException {
        _setExecutionPhase(CTExecutionPhase.FIRING_EVENT_GENERATORS_PHASE);

        try {
            CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
            Iterator eventGenerators = schedule.get(CTSchedule.EVENT_GENERATORS)
                .actorIterator();

            while (eventGenerators.hasNext() && !_stopRequested) {
                Actor actor = (Actor) eventGenerators.next();

                // FIXME: if all actors are prefired before being fired, the
                // checking for prefiring is unnecessary.
                if (!isPrefireComplete(actor)) {
                    if (_debugging && _verbose) {
                        _debug("Prefire event generator: "
                                + ((Nameable) actor).getName() + " at time "
                                + getModelTime());
                    }

                    if (!actor.prefire()) {
                        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
                        throw new IllegalActionException((Nameable) actor,
                                "Actor is not ready to fire. In the CT domain, "
                                + "all event generators should be ready to fire"
                                + " at all times.\n"
                                + "Does the actor only operate on sequence "
                                + "of tokens?");
                    }

                    setPrefireComplete(actor);
                }

                if (_debugging) {
                    _debug("Fire event generator : "
                            + ((Nameable) actor).getName() + " at time "
                            + getModelTime());
                }

                actor.fire();
            }
        } finally {
            _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
        }
    }

    /** Return the breakpoint ODE solver.
     *  @return The breakpoint ODE solver.
     *  @see #getNormalODESolver
     */
    public ODESolver getBreakpointSolver() {
        return _breakpointSolver;
    }

    /** Always return null, because this director can not be an inside director.
     *  @return Null, always.
     */
    public CTGeneralDirector getExecutiveCTGeneralDirector() {
        return null;
    }

    /** Return the ODE solver for normal integration.
     *  @return The ODE solver for normal integration.
     *  @see #getBreakpointSolver
     */
    public ODESolver getNormalODESolver() {
        return _normalSolver;
    }

    /** Return true if there is an event at the current time. The event may be
     *  generated by event generators or if the current time was registered as a
     *  breakpoint.
     *  @return True if there is an event at current time.
     */
    public boolean hasCurrentEvent() {
        // NOTE: we need to make this method public because it is also used
        // by the CTEmbeddedDirector class, which implements the
        // CTTransparentDirector interface.
        // NOTE: We have to check both the breakpoint table and event
        // generators, because event generators do not post breakpoints to the
        // breakpoint tables directly.
        // The reason for not posting possible events to the breakpoint table
        // is that not all events detected will happen. In fact, if multiple
        // events are detected during an continuous phase of execution with a
        // integration step size, only the earliest one is guaranteed to
        // happen. The integration step size needs to be reduced
        // according to that event. Consequently, the rest of events may not
        // occur in reality.
        // In summary, breakpoints and unpredictable events have to
        // be dinstinguished and treated differently.
        try {
            CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
            Iterator eventGenerators = schedule.get(CTSchedule.EVENT_GENERATORS)
                .actorIterator();
            boolean discreteEventsExists = false;

            // Note that we do not have to go over all event generators.
            // As long as one of them has event, we need a discrete phase of
            // execution.
            while (!discreteEventsExists && eventGenerators.hasNext()) {
                CTEventGenerator eventGenerator = (CTEventGenerator) eventGenerators
                    .next();

                if (eventGenerator.hasCurrentEvent()) {
                    discreteEventsExists = true;
                }
            }

            // Check the breakpoint table for see if the the earliest
            // breakpoint happens at the current time. If so, remove the
            // breakpoint from the breakpoints table.
            discreteEventsExists |= _removeCurrentTimeFromBreakpointTable();
            return discreteEventsExists;
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }
    }

    /** Initialize the model for a simulation. Construct a valid schedule.
     *  Invoke the initialize() method of the super class to set the current
     *  time and initialize all the actors directed by this director.
     *  Set the step size and the suggested next step size for the first firing.
     *  Register the stop time as a breakpoint.
     *  This method is called after data types are resolved.
     *
     *  @exception IllegalActionException If the initialize() method of the
     *  super class throws it, or the stop time can not be
     *  registered as a breakpoint.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("----- Initializing: " + getFullName());
        }

        // NOTE: The initialization order of the model is as follows: first,
        // the container actor of this director initializes to resolve the
        // signal types of its ports partialy (including the signal types of
        // input ports of the contained actors); then the contained actors
        // are initialized, where the signal types of the output ports of the
        // contained actors are resolved; last, the signal types of the rest
        // ports of the container actor are resolved.
        // NOTE: It is possible that the signal type of an input port of a
        // subsystem depends on one of its output ports, e.g., a feedback loop.
        // It may take several iterations (by going into and out of the
        // container) to completely resolve the signal type of all input and
        // output ports. A fixed point can be reached when the signal type of
        // all ports are resolved and they will never be changed.
        // We have a lattice that is UNKNOWN -> DISCRETE | CONTINUOUS -> GENERAL
        // for the signal types. When a fixed point is reached, if any port
        // has either UNKNOWN or GENERAL signal type, there is something wrong
        // with the model or the CTScheduler.
        // FIXME: leverage the algorithm for the Data Type resolution.
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();

        if (_debugging) {
            // Display schedule
            _debug(getFullName(), " A schedule is computed: ");
            _debug(schedule.toString());
        }

        // Initialize protected variables and the contained actors.
        super.initialize();

        // Set the current step size and the suggested next step size.
        double initialStepSize = getInitialStepSize();
        setCurrentStepSize(initialStepSize);
        setSuggestedNextStepSize(initialStepSize);

        if (_debugging) {
            _debug("Set both current step size and suggested next "
                    + "step size to " + initialStepSize);
        }

        // register the stop time as a breakpoint.
        if (_debugging && _verbose) {
            _debug("Set the stop time as a breakpoint: " + getModelStopTime());
        }

        fireAt((Actor) getContainer(), getModelStopTime());

        _initialStatesNotReady = true;

        if (_debugging) {
            _debug("----- End of Initialization of: " + getFullName());
        }
    }

    /** Return true if this director wants to be fired again. Return false, if
     *  the stop time is reached and no more actors need to be fired at that
     *  time, or if any actor returned false from its postfire() method.
     *  @return false If the simulation is finished.
     *  @exception IllegalActionException If thrown by the super class, or the
     *  current model time is bigger than the stop time.
     */
    public boolean postfire() throws IllegalActionException {
        if (getModelTime().compareTo(getModelStopTime()) > 0) {
            // This should never happen. Otherwise,
            // there is a bug in the fire method.
            // Whenever an InternalErrorException happens, there
            // is (at least) one bug. :(
            throw new IllegalActionException(
                    "Execution can not exceed the stop time.");
        }

        // Now, the current time is equal to the stop time.
        // If the breakpoints table does not contain the current model time,
        // which means no events are and will be generated,
        // the execution stops by returning false in this postfire method.
        if (getModelTime().equals(getModelStopTime())
                && !getBreakPoints().contains(getModelTime())) {
            if (_debugging) {
                _debug("Postfire returns false at: " + getModelTime());
            }

            return false;
        }

        return super.postfire();
    }

    /** Call the prefire() method of the super class and return its value.
     *  Record the current model time as the beginning time of the current
     *  iteration.
     *  @return True if this director is ready to fire.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean prefire() throws IllegalActionException {
        boolean prefireReturns = super.prefire();

        // Record the start time of the current iteration
        // The begin time of an iteration can be changed only by directors.
        // On the other hand, the model time may be changed by ODE solvers.
        // One example solver is the RK23 solver. It resolves the states in
        // three steps, and it increment the model time at each step. If
        // the CurrentTime actor is involved as one of the state transition
        // actors, it needs to report the model time at each intermediate steps.
        // (The CurrentTime actor reports the model time.)
        _setIterationBeginTime(getModelTime());
        return prefireReturns;
    }

    /** After calling the preinitialize() method of the super class,
     *  instantiate all the solvers.
     *  @exception IllegalActionException If thrown by the super class,
     *  or not all solvers can be instantiated, or the current solver
     *  can not be set.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Instantiate a normal ODE solver.
        if (_debugging) {
            _debug(getFullName(), " instantiating the ODE solver ",
                    _normalSolverClassName);
        }

        _normalSolver = _instantiateODESolver(_normalSolverClassName);

        // Instantiate a breakpoint solver.
        if (_debugging) {
            _debug(getFullName(), "instantiating the breakpoint solver ",
                    _breakpointSolverClassName);
        }

        _breakpointSolver = _instantiateODESolver(_breakpointSolverClassName);

        // Set the current ODE solver to the normal solver.  NOTE: In
        // fact, it does not matter which solver to choose here
        // because when the fire() method is called, the current
        // solver gets set corresponding to the phases of
        // execution. We choose the normal solver just to ensure back
        // compatibility of tests.  In parcicular, we set the solver
        // here because the CTBaseIntegrator checks the existence of
        // solver during its initialize() method.
        _setCurrentODESolver(_normalSolver);
    }

    /** Fire all the actors in the output schedule.
     *  <p>
     *  If they have not be prefired, prefire them first. The abstract
     *  semantics require that prefire() be called exactly once in an iteration.
     *  This is important because, for example, time can only be tested
     *  reliably in prefire(). The time tested indicates the starting point of
     *  an integration step. During the following possibly multiple firings,
     *  time may progress, depending on the ODE solver used. Hierarchies in CT
     *  and hybrid systems cases actually rely on this fact to control internal
     *  step sizes.
     *  <p>
     *  FIXME: If we treat time as a state, it should not change until the
     *  postfire method!
     *
     *  @exception IllegalActionException If there is no schedule, or any
     *  output actor can not be prefired or fired, or any output actor
     *  returns false from its prefire method.
     */
    public void produceOutput() throws IllegalActionException {
        _setExecutionPhase(CTExecutionPhase.PRODUCING_OUTPUTS_PHASE);

        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator outputActors = schedule.get(CTSchedule.OUTPUT_ACTORS)
            .actorIterator();

        while (outputActors.hasNext() && !_stopRequested) {
            Actor actor = (Actor) outputActors.next();

            if (!isPrefireComplete(actor)) {
                setPrefireComplete(actor);

                if (_debugging && _verbose) {
                    _debug("Prefire output actor: "
                            + ((Nameable) actor).getName() + " at time "
                            + getModelTime());
                }

                if (!actor.prefire()) {
                    throw new IllegalActionException((Nameable) actor,
                            "Actor is not ready to fire. In the CT domain, "
                            + "all continuous actors should be ready to fire "
                            + "at all times.\n"
                            + "Does the actor only operate on sequence "
                            + "of tokens?");
                }
            }

            if (_debugging) {
                _debug("Fire output actor: " + ((Nameable) actor).getName()
                        + " at time " + getModelTime());
            }

            actor.fire();
        }

        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
    }

    /** Postfire event generators. This method is only called in the
     *  continuous phase of execution, after the current step size is accepted
     *  by the output step size controllers. Note that discrete events must
     *  not be generated in this method. Instead, they will be
     *  generated in the immediately following discrete phase of execution.
     *
     *  @exception IllegalActionException If the schedule does not exist, or
     *  any actor throws it during postfiring.
     */
    public void postfireEventGenerators() throws IllegalActionException {
        _setExecutionPhase(CTExecutionPhase.POSTFIRING_EVENT_GENERATORS_PHASE);

        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator eventGenerators = schedule.get(CTSchedule.EVENT_GENERATORS)
            .actorIterator();

        boolean postfireReturns = true;

        while (eventGenerators.hasNext() && !_stopRequested) {
            Actor actor = (Actor) eventGenerators.next();
            postfireReturns = actor.postfire();
            _postfireReturns = _postfireReturns && postfireReturns;

            if (_debugging) {
                _debug("Postfire event generator : "
                        + ((Nameable) actor).getName() + " at time "
                        + getModelTime() + ", which returns " + postfireReturns);
            }
        }

        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
    }

    /** Set the flag indicating that the initial states are not ready.
     *  This method should only be used by the HSDirector.
     */
    public void setInitialStatesNotReady() {
        _initialStatesNotReady = true;
    }

    /** Set a new value to the current time of the model, where the new
     *  time can be earlier than the current time to support rollback.
     *  This overrides the setCurrentTime() in the Director base class.
     *  This is a critical parameter in an execution, and the
     *  actors are not supposed to call it.
     *  @param newTime The new current simulation time.
     */
    public final void setModelTime(Time newTime) {
        // This method is final for performance reason.
        // NOTE: this feature is necessary for the CTMixedSignalDirector
        // and CTEmbeddedDirector to roll back.
        if (_debugging) {
            _debug("----- Setting current time to " + newTime);
        }

        _currentTime = newTime;
    }

    /** Call the postfire() method on all continuous actors in the schedule.
     *  For a correct CT simulation, the state of a continuous actor can only
     *  change at this stage of an iteration. Meanwhile, the current states
     *  are marked as a known good checkpoint.
     *  <p>
     *  If the <i>synchronizeToRealTime</i> parameter is <i>true</i>,
     *  then this method will block execution until the real time catches
     *  up with current model time. The units for time are seconds.
     *
     *  @exception IllegalActionException If the synchronizeToRealTime
     *  parameter does not have a valid token, or the sleep is interrupted,
     *  or there is not a schedule, or any of the actors in the schedule can
     *  not be postfired.
     */
    public void updateContinuousStates() throws IllegalActionException {
        // Synchronize to real time if necessary.
        if (((BooleanToken) synchronizeToRealTime.getToken()).booleanValue()) {
            long realTime = System.currentTimeMillis() - _timeBase;
            long simulationTime = (long) ((getModelTime()
                                                  .subtract(getModelStartTime())
                                                  .getDoubleValue()) * 1000);

            if (_debugging) {
                _debug("real time " + realTime + " and simulation time "
                        + simulationTime);
            }

            long timeDifference = simulationTime - realTime;

            if (timeDifference > 20) {
                try {
                    if (_debugging) {
                        _debug("Sleep for " + timeDifference + "ms");
                    }

                    Thread.sleep(timeDifference - 20);
                } catch (Exception e) {
                    throw new IllegalActionException(this,
                            "Sleep Interrupted" + e.getMessage());
                }
            } else {
                if (_debugging) {
                    _debug("Warning: " + getFullName(),
                            " cannot achieve real-time performance"
                            + " at simulation time " + getModelTime());
                }
            }
        }

        _setExecutionPhase(CTExecutionPhase.UPDATING_CONTINUOUS_STATES_PHASE);

        // Call the postfire method of the continuous actors.
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(CTSchedule.CONTINUOUS_ACTORS)
            .actorIterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (_debugging) {
                _debug("Postfire " + (Nameable) actor);
            }

            boolean postfireReturns = actor.postfire();
            _postfireReturns = _postfireReturns && postfireReturns;
        }

        // Mark the current state of the stateful actors.
        actors = schedule.get(CTSchedule.STATEFUL_ACTORS).actorIterator();

        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor) actors.next();

            if (_debugging) {
                _debug("Postfire " + (Nameable) actor);
            }

            actor.markState();
        }

        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** This method performs a continuous phase of execution. At this phase,
     *  a normal ODE solver tries to solve the initial states at a further time
     *  t_1, based on the inputs and final states at t_0, where t_0 < t_1.
     *  At the end of this method, outputs are generated.
     *  <p>
     *  Note that t_1 can be adjusted by the normal ODE solver with respect to
     *  the accuracy requirements from step size control actors. Therefore,
     *  this method starts with a guess of t_1, or an integration step size
     *  as (t_1 - t_0). If all step size control actors are satisfied with the
     *  guess, this method returns. Otherwise, the normal ODE solver chooses
     *  a smaller step size and tries to solve the states. Therefore, this
     *  method ends up with either resolved states or an exception complaining
     *  that states can not be resolved even with the preconfigured minimum
     *  step size.
     *
     *  @exception IllegalActionException If ODESolver can not be set or
     *  one of the continuous actors throws it.
     */
    protected void _continuousPhaseExecution() throws IllegalActionException {
        if (_debugging) {
            _debug("\n !!! continuous phase execution at " + getModelTime());
        }

        // Choose a suggested step size, which is a guess.
        setCurrentStepSize(getSuggestedNextStepSize());

        // Refine the correct step size for the continuous phase execution
        // with respect to the breakpoint table.
        setCurrentStepSize(_refinedStepWRTBreakpoints());

        // Set the current ODE solver to a normal ODE solver to do integration.
        _setCurrentODESolver(getNormalODESolver());

        if (_debugging) {
            _debug("execute the system from " + getModelTime()
                    + " with a step size " + getCurrentStepSize()
                    + " using solver " + getCurrentODESolver().getName());
        }

        // Resolve the initial states at a future time
        // (the current time plus the current step size).
        _resolveInitialStates();
    }

    /** Perform a discrete phase of execution by processing all discrete events
     *  happening at the current model time. In this method, event generators,
     *  purely discrete actors, waveform generators, and continuous actors are
     *  repeatedly iterated until the execution reaches a fixed point, where no
     *  more events exist at the current time.
     *  @exception IllegalActionException If one of the actors throws it or
     *  the schedule does not exist.
     */
    protected void _discretePhaseExecution() throws IllegalActionException {
        _setDiscretePhase(true);

        prefireClear();

        if (_debugging) {
            _debug("\n !!! discrete phase execution at " + getModelTime());
        }

        // configure step sizes
        setCurrentStepSize(0.0);

        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();

        // The reason to perform the below operation at the first
        // iteration is to establish the initial conditions for event
        // generators. For example, make the _lastTrigger of the level
        // crossing detector or the variable used by guard expressions
        // available.
        if (_initialStatesNotReady) {
            establishInitialStates();
            _initialStatesNotReady = false;
        }

        // NOTE: If there is some event at the current time,
        // perform a discrete phase of execution.
        // The event may be generated either by event generators or a
        // modal model.
        // NOTE: To support transient states in modal models,
        // which are event generators, modal models need to register the
        // current time as a breakpoint if there is an enabled transitioin
        // at the current time.
        while (hasCurrentEvent()) {
            if (_debugging) {
                _debug("Iterate all actors once in the following order:");
                _debug("  ---> " + getName(),
                        ": iterating event generators (continuous -> discrete)");
            }

            _iterateEventGenerators(schedule);

            if (_debugging) {
                _debug("  ---> " + getName(),
                        ": iterating purely discrete actors (discrete -> discrete)");
            }

            _iteratePurelyDiscreteActors(schedule);

            // Reacts to discrete events.
            // If a modal model takes a transition, that transition may
            // generate a discrete event or change the value of a continuous
            // variable. Therefore, waveform generators and continuous actors
            // need to be iterated to catch the event and the value change.
            if (_debugging) {
                _debug("  ---> " + getName(),
                        ": iterating waveform generators (discrete -> continuous)");
            }

            _iterateWaveformGenerators(schedule);

            if (_debugging) {
                _debug("  ---> " + getName(),
                        ": using a breakpoint solver, find integrator output "
                        + "values and iterate continuous actors to find all "
                        + "continuous-time signal values.");
            }

            _propagateResolvedStates();

            // If stop is requested, stop this discrete phase of execution.
            if (_stopRequested) {
                break;
            }
        }

        // When we jump out of the previous loop, the _propagateResolvedStates()
        // method already created the final states at the current model time.
        // These states will be used by the immediately following continuous
        // phase of execution to predict the initial states at some future time.
        if (_debugging) {
            _debug(" >>> The next breakpoint is at " + getBreakPoints().first()
                    + ", which is in the future.");
            _debug(" >>> The fixed point of the current discrete "
                    + "phase of execution is reached.");
        }

        // We are leaving discrete phase of execution...
        _setDiscretePhase(false);
    }

    /** Initialize parameters to their default values.
     */
    protected void _initParameters() {
        super._initParameters();

        try {
            _normalSolverClassName = "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver";
            ODESolver = new Parameter(this, "ODESolver",
                    new StringToken("ExplicitRK45Solver"));
            ODESolver.setTypeEquals(BaseType.STRING);
            ODESolver.addChoice(new StringToken("ExplicitRK23Solver").toString());
            ODESolver.addChoice(new StringToken("ExplicitRK45Solver").toString());
            ODESolver.addChoice(new StringToken("BackwardEulerSolver").toString());
            ODESolver.addChoice(new StringToken("ForwardEulerSolver").toString());
            _breakpointSolverClassName = "ptolemy.domains.ct.kernel.solver.DerivativeResolver";
            breakpointODESolver = new Parameter(this, "breakpointODESolver",
                    new StringToken("DerivativeResolver"));
            breakpointODESolver.setTypeEquals(BaseType.STRING);

            // We have only one breakpoint ODE solver.
            breakpointODESolver.setVisibility(Settable.NOT_EDITABLE);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this, "Parameter name duplication.");
        }
    }

    /** Return true if all step size control actors in the output
     *  schedule agree that the current step is accurate.
     *  @return True if all step size control actors agree with the current
     *  step size.
     */
    protected boolean _isOutputAccurate() {
        if (_debugging) {
            _debug("Check accuracy for output step size control actors:");
        }

        boolean accurate = true;

        // Get all the output step size control actors.
        Iterator actors;

        try {
            CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
            actors = schedule.get(CTSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS)
                .actorIterator();
        } catch (IllegalActionException e) {
            throw new InternalErrorException("Can not get schedule.");
        }

        // Ask -ALL- the actors whether the current step size is accurate.
        // THIS IS VERY IMPORTANT!!!
        // NOTE: all actors are guranteed to be asked once even if some
        // actors already set the "accurate" variable to false.
        // The reason is that event generators do not check the step size
        // accuracy in their fire emthods and they need to check the existence
        // of events in the special isOutputAccurate() method.
        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors.next();
            boolean thisAccurate = actor.isOutputAccurate();

            if (_debugging) {
                _debug("  Checking output step size control actor: "
                        + ((NamedObj) actor).getName() + ", which returns "
                        + thisAccurate);
            }

            accurate = accurate && thisAccurate;
        }

        if (_debugging) {
            _debug("Overall output accuracy result: " + accurate);
        }

        return accurate;
    }

    /** Return true if all step size control actors in the dynamic actor
     *  schedule and the state transition schedule agree that
     *  the current step size is accurate.
     *  @return True if all state step size control actors agree with the
     *  current step size.
     */
    protected boolean _isStateAccurate() {
        if (_debugging) {
            _debug("Checking accuracy for state step size control actors:");
        }

        boolean accurate = true;

        // Get all the output step size control actors.
        Iterator actors;

        try {
            CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
            actors = schedule.get(CTSchedule.STATE_STEP_SIZE_CONTROL_ACTORS)
                .actorIterator();
        } catch (IllegalActionException e) {
            throw new InternalErrorException("Can not get schedule.");
        }

        // Ask -ALL- the actors whether the current step size is accurate.
        // THIS IS VERY IMPORTANT!!!
        // NOTE: all actors are guranteed to be asked once. See the
        // _isOutputAccurate() method for the similar reason.
        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors.next();
            boolean thisAccurate = actor.isStateAccurate();

            if (_debugging) {
                _debug("  Checking state step size control actor: "
                        + ((NamedObj) actor).getName() + ", which returns "
                        + thisAccurate);
            }

            accurate = accurate && thisAccurate;
        }

        if (_debugging) {
            _debug("Overall state accuracy result: " + accurate);
        }

        return accurate;
    }

    // The following methods are protected because they are
    // also used by CTEmbeddedDirector.

    /** Iterate all purely discrete-event actors. Purely discrete-event actors
     *  are those take discrete signals as inputs and generate discrete
     *  signals as outputs.
     *  @param schedule The schedule that contains purely discrete-event actors.
     *  @exception IllegalActionException If any actor can not be iterated.
     */
    protected void _iteratePurelyDiscreteActors(CTSchedule schedule)
            throws IllegalActionException {
        _setExecutionPhase(CTExecutionPhase.ITERATING_PURELY_DISCRETE_ACTORS_PHASE);
        _iterateSchedule(schedule.get(CTSchedule.DISCRETE_ACTORS));
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
    }

    /** Iterate all event generators .
     *  @param schedule The schedule that contains event generators.
     *  @exception IllegalActionException If any actor can not be iterated.
     */
    protected void _iterateEventGenerators(CTSchedule schedule)
            throws IllegalActionException {
        _setExecutionPhase(CTExecutionPhase.GENERATING_EVENTS_PHASE);
        _iterateSchedule(schedule.get(CTSchedule.EVENT_GENERATORS));
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
    }

    /** Iterate all wave generators.
     *  @param schedule The schedule that contains wave generators.
     *  @exception IllegalActionException If any actor can not be iterated.
     */
    protected void _iterateWaveformGenerators(CTSchedule schedule)
            throws IllegalActionException {
        _setExecutionPhase(CTExecutionPhase.GENERATING_WAVEFORMS_PHASE);
        _iterateSchedule(schedule.get(CTSchedule.WAVEFORM_GENERATORS));
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
    }

    /** Predict the next step size. If the current integration step is accurate,
     *  estimate the step size for the next iteration. The predicted step size
     *  is the minimum of predictions from all step size control actors,
     *  and it never exceeds 10 times of the current step size.
     *  If there are no step-size control actors at all, then return
     *  the current step size times 5. However, it never exceeds the maximum
     *  step size.
     *  @return the prediced next step size.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _predictNextStepSize() throws IllegalActionException {
        double predictedStep = getCurrentStepSize();

        if (!isDiscretePhase()) {
            // The assumption here is that if the current phase of execution is
            // not a discrete one, it must be a continuous one.
            predictedStep = 10.0 * getCurrentStepSize();

            boolean foundOne = false;
            CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
            Iterator actors = schedule.get(CTSchedule.STATE_STEP_SIZE_CONTROL_ACTORS)
                .actorIterator();

            while (actors.hasNext()) {
                CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                    .next();
                predictedStep = Math.min(predictedStep,
                        actor.predictedStepSize());
                foundOne = true;
            }

            actors = schedule.get(CTSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS)
                .actorIterator();

            while (actors.hasNext()) {
                CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                    .next();
                predictedStep = Math.min(predictedStep,
                        actor.predictedStepSize());
                foundOne = true;
            }

            if (!foundOne) {
                // We multiple the step size by 5.0.
                // This is an agressive guess.
                predictedStep = getCurrentStepSize() * 5.0;
            }

            if (predictedStep > getMaxStepSize()) {
                predictedStep = getMaxStepSize();
            }
        }

        // If the current phase of execution is a discrete one, do nothing.
        return predictedStep;
    }

    /** Return the refined step size with respect to the output actors.
     *  All the step size control actors in the output schedule are queried for
     *  a refined step size. The smallest one is returned.
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it or the
     *  refined step size is less than the time resolution.
     */
    protected double _refinedStepWRTOutput() throws IllegalActionException {
        if (_debugging) {
            _debug("Refining the current step size w.r.t. all output actors:");
        }

        double timeResolution = getTimeResolution();
        double refinedStep = getCurrentStepSize();
        boolean triedTheMinimumStepSize = (refinedStep == timeResolution);

        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(CTSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS)
            .actorIterator();

        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors.next();
            refinedStep = Math.min(refinedStep, actor.refinedStepSize());
        }

        if (refinedStep < (0.5 * timeResolution)) {
            if (triedTheMinimumStepSize) {
                if (_debugging) {
                    _debug("The previous step size is the time"
                            + " resolution. The refined step size is less than"
                            + " the time resolution. We can not refine the step"
                            + " size more.");
                }

                throw new IllegalActionException(this,
                        "The refined step size is less than the minimum time "
                        + "resolution, at time " + getModelTime());
            } else {
                if (_debugging) {
                    _debug("The previous step size is bigger than the time"
                            + " resolution. The refined step size is less than"
                            + " the time resolution, try setting the step size"
                            + " to the time resolution.");
                }

                refinedStep = timeResolution;
            }
        }

        if (_debugging && _verbose) {
            _debug(getFullName(),
                    "refine step with respect to output to" + refinedStep);
        }

        _setIterationEndTime(getModelTime().add(refinedStep));
        return refinedStep;
    }

    /** Return the refined step size with respect to state accuracy
     *  requirement.
     *  All the step size control actors in the state transition
     *  and dynamic actor schedule are queried for a refined step size. Then
     *  the smallest one is returned.
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it, or the
     *  refined step size is less than the minimum step size parameter
     *  of the director.
     */
    protected double _refinedStepWRTState() throws IllegalActionException {
        if (_debugging) {
            _debug("Refining the current step size w.r.t. all state actors:");
        }

        double refinedStep = getCurrentStepSize();
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(CTSchedule.STATE_STEP_SIZE_CONTROL_ACTORS)
            .actorIterator();

        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors.next();
            double size = actor.refinedStepSize();

            if (_debugging && _verbose) {
                _debug(((NamedObj) actor).getName(),
                        "refines step size to " + size);
            }

            refinedStep = Math.min(refinedStep, size);
        }

        if (refinedStep < (0.5 * getMinStepSize())) {
            throw new IllegalActionException(this,
                    "Cannot resolve new states even using "
                    + "the minimum step size, at time " + getModelTime());
        }

        _setIterationEndTime(getModelTime().add(refinedStep));
        return refinedStep;
    }

    /** Return true if the current time is the first element in the breakpoint
     *  table, and remove that element from the breakpoint table. Otherwise,
     *  the breakpoint table is unchanged.
     *  @return true if the current time is a breakpoint.
     *  @exception IllegalActionException If a breakpoint is missed.
     */
    protected boolean _removeCurrentTimeFromBreakpointTable()
            throws IllegalActionException {
        // NOTE: We only remove elements from breakpoint table in this method
        // and the postfire() method of the CT director.
        boolean currentTimeIsABreakpoint = false;
        TotallyOrderedSet breakPoints = getBreakPoints();
        Time now = getModelTime();

        // If the current time is a breakpoint, remove it from table.
        if ((breakPoints != null) && !breakPoints.isEmpty()) {
            if (breakPoints.contains(now)) {
                // The current time is a break point.
                currentTimeIsABreakpoint = true;

                Time time = (Time) breakPoints.removeFirst();

                if (time.compareTo(now) < 0) {
                    // This should not happen for CTMultisolverDirector,
                    // but it is possible for CTEmbeddedDirector.
                    // When a CT refinement is made inactive for a long time
                    // and reentered, the previously stored breakpoints may
                    // be in the past... The same thing happens in the
                    // prefire() method of DE director.
                    breakPoints.removeAllLessThan(now);
                }

                if (_debugging) {
                    _debug("Remove " + now + " from the break-point list.");
                }
            }
        }

        return currentTimeIsABreakpoint;
    }

    /** Use breakpoint ODE solver to propagate the resolved states at the
     *  current phase of execution.
     *  @exception IllegalActionException If one of the actors throws it or
     *  the ODE solver can not be set.
     */
    protected void _propagateResolvedStates() throws IllegalActionException {
        if (_debugging) {
            _debug("Propagating the resolved states ...");
        }

        // set a breakpoint solver
        _setCurrentODESolver(_breakpointSolver);

        // NOTE: The step size used in the breakpoint iteration will be
        // set by the breakpoint ODE solver. There is no need to set the
        // step size here.
        // During this method, no step size refinement is needed.
        // Two important facts: states can always be resolved;
        // outputs are always correct.
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();

        ODESolver solver = getCurrentODESolver();

        if (_debugging && _verbose) {
            _debug("Using breakpoint solver: " + solver.getName()
                    + " to propagate states.");
        }

        _setExecutionPhase(CTExecutionPhase.PREFIRING_DYNAMIC_ACTORS_PHASE);
        prefireDynamicActors();
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);

        // build history information. In particular, the derivative.
        _setExecutionPhase(CTExecutionPhase.FIRING_STATE_TRANSITION_ACTORS_PHASE);
        solver.fireStateTransitionActors();
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);

        _setExecutionPhase(CTExecutionPhase.FIRING_DYNAMIC_ACTORS_PHASE);
        solver.fireDynamicActors();
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);

        // Iterate output actors.
        // NOTE: Output schedule does not include event generators
        // because they are iterated separately.
        _setExecutionPhase(CTExecutionPhase.PRODUCING_OUTPUTS_PHASE);
        produceOutput();
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);

        // postfire all continuous actors.
        _setExecutionPhase(CTExecutionPhase.UPDATING_CONTINUOUS_STATES_PHASE);
        updateContinuousStates();
        _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
    }

    /** Resolve the initial states with a normal ODE solver at a further time.
     *  The future time is the current time puls the step size used by the
     *  solver. Return immediately if any actor returns false in their
     *  prefire() method. After this method is called, time advances to the
     *  future time.
     *  @exception IllegalActionException If one the actors throws it
     *  in its execution methods.
     */
    protected void _resolveInitialStates() throws IllegalActionException {
        if (_debugging && _verbose) {
            _debug("Resolving the initial states at " + getModelTime(),
                    " (current time) plus step size " + getCurrentStepSize());
        }

        ODESolver solver = getCurrentODESolver();

        if (_debugging && _verbose) {
            _debug("Using ODE solver: " + solver.getName());
        }

        // NOTE: This used to execute prefire() methods all at once before
        // anything else happened.  But this was too early.
        // The input data is not yet present.
        // Instead, we execute it before the first fire() in the iteration
        // for all actors except DYNAMIC actors (like integrators).
        // FIXME: The previous note does not make sense. Continuous
        // signals should always have values. This should be guaranteed at
        // the end of the initialize() method.
        // FIXME: Calling the resolveStates() method increments time prior
        // to calling prefire() for the actors, so current time when
        // prefire is not (as it was before) the start time of the
        // iteration.  It is (unfortunately) the first guess as to the
        // end time of the iteration.  If this needs to be changed,
        // then probably the easiest place to do this is where prefire()
        // is called (which includes in ODESolver), by setting time back
        // to the start of the iteration. EAL 1/13/03
        prefireClear();

        // Prefire dynamic actors (intergrators actually) to produce temporary
        // inputs for state transition actors.
        // NOTE: We need to prefire dynamic actors again even though they have
        // been prefired in the _propagateResolvedStates method, because ODE
        // solver changes and the Integrator Aux Variables need updated.
        prefireDynamicActors();

        // If stop is not requested, keep trying to resolve states, where
        // the current step size may get refined if the resolved
        // states and outputs are inaccurate.
        while (!_stopRequested) {
            while (!_stopRequested) {
                // Restore the saved state of the stateful actors.
                CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
                Iterator actors = schedule.get(CTSchedule.STATEFUL_ACTORS)
                    .actorIterator();

                while (actors.hasNext()) {
                    CTStatefulActor actor = (CTStatefulActor) actors.next();

                    if (_debugging) {
                        _debug("Restore states " + (Nameable) actor);
                    }

                    actor.goToMarkedState();
                }

                // Reset the round counts and the convergencies to false.
                // NOTE: some solvers have their convergencies depending on
                // the round counts. For example, it takes 3 rounds for a
                // RK-23 solver to solve states.
                solver._resetRoundCount();
                solver._setConverged(false);

                // repeating resolving states until states converge.
                while (!solver._isConverged() && solver.resolveStates()) {
                    // fire dynamic actors
                    _setExecutionPhase(CTExecutionPhase.FIRING_DYNAMIC_ACTORS_PHASE);

                    // NOTE: at exactly this point, time is advanced.
                    // The amount of advance depends on the current ODE solver.
                    solver.fireDynamicActors();
                    _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);

                    // fire state transition actors to calculate derivatives
                    _setExecutionPhase(CTExecutionPhase.FIRING_STATE_TRANSITION_ACTORS_PHASE);
                    solver.fireStateTransitionActors();
                    _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
                }

                // NOTE: solver.resolveStates() returns true if the states are
                // resolved, and it returns false if the maximum number of
                // iterations is reached but the states have not be resolved.
                if (solver.resolveStates()) {
                    if (_debugging && _verbose) {
                        _debug("State resolved by solver.");
                    }

                    // Check whether this step is acceptable
                    if (!_isStateAccurate()) {
                        setCurrentStepSize(_refinedStepWRTState());
                    } else {
                        // states are resolved successfully.
                        break;
                    }
                } else {
                    // Failed to resolve state within the maximum number
                    // of iterations. e.g. in implicit methods.
                    if (getCurrentStepSize() < (0.5 * getMinStepSize())) {
                        throw new IllegalActionException(this,
                                "Cannot resolve new states even using "
                                + "the minimum step size, at time "
                                + getModelTime());
                    }

                    setCurrentStepSize(0.5 * getCurrentStepSize());
                }

                // Restore the model time to the beginning time of this
                // iteration.
                setModelTime(getIterationBeginTime());

                if (_debugging && _verbose) {
                    _debug("Execute the system from " + getModelTime()
                            + " with a smaller step size" + getCurrentStepSize());
                }
            }

            // States have be resolved. Note that the current
            // time has been increased by the amount of the
            // step size used in this iteration.
            // If the while loop was terminated by the stop request,
            // jump out of the loop in spite of whether the states
            // have been resolved or not.
            if (_stopRequested) {
                break;
            }

            // Iterate output actors to produce outputs.
            produceOutput();

            // NOTE: Event generators are fired but not postfired
            // because the current step size may not be accurate.
            // The event generators will be postfired when the current step
            // size is confirmed by all step size control actorls.
            fireEventGenerators();

            // If event generators are not satisfied with the current step
            // size, refine the step size to a smaller one.
            if (!_isOutputAccurate()) {
                setModelTime(getIterationBeginTime());
                setCurrentStepSize(_refinedStepWRTOutput());

                if (_debugging && _verbose) {
                    _debug("Refine the current step size"
                            + " with a smaller one " + getCurrentStepSize());
                }
            } else {
                // outputs are generated successfully.
                break;
            }
        }

        // postfire all continuous actors to commit their states.
        // Note that event generators are postfired.
        updateContinuousStates();
        postfireEventGenerators();

        // predict the next step size.
        setSuggestedNextStepSize(_predictNextStepSize());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         proetected variables              ////

    /** Flag indicating the initial states are not ready.
     *  FIXME: This isn't clear.... What is this flag for?
     *  Why is it so publically visible?
     */
    protected boolean _initialStatesNotReady;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Iterate all the actors inside a given schedule, by prefiring,
    // firing and postfiring them.
    private void _iterateSchedule(ScheduleElement schedule)
            throws IllegalActionException {
        Iterator actors = schedule.actorIterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (_debugging && _verbose) {
                _debug("Prefire actor: " + ((Nameable) actor).getName()
                        + " at time " + getModelTime());
            }

            if (actor.prefire()) {
                if (_debugging && _verbose) {
                    _debug("Fire actor: " + ((Nameable) actor).getName()
                            + " at time " + getModelTime());
                }

                actor.fire();

                if (_debugging && _verbose) {
                    _debug("Postfire actor: " + ((Nameable) actor).getName()
                            + " at time " + getModelTime());
                }

                _postfireReturns = actor.postfire() && _postfireReturns;
            }
        }
    }

    // Return the refined step size with respect to the breakpoints.
    // If the current time plus the current step size exceeds the
    // time of the next breakpoint, reduce the step size such that the next
    // breakpoint is the end time of the current iteration.
    private double _refinedStepWRTBreakpoints() {
        double currentStepSize = getCurrentStepSize();
        Time iterationEndTime = getModelTime().add(currentStepSize);
        _setIterationEndTime(iterationEndTime);

        TotallyOrderedSet breakPoints = getBreakPoints();

        if ((breakPoints != null) && !breakPoints.isEmpty()) {
            if (_debugging && _verbose) {
                _debug("The first breakpoint in the breakpoint list is at "
                        + breakPoints.first());
            }

            // Adjust step size so that the first breakpoint is
            // not in the middle of this step.
            // NOTE: the breakpoint table is not changed.
            Time point = ((Time) breakPoints.first());

            if (iterationEndTime.compareTo(point) > 0) {
                currentStepSize = point.subtract(getModelTime()).getDoubleValue();

                if (_debugging && _verbose) {
                    _debug("Refining the current step size w.r.t. "
                            + "the next breakpoint to " + currentStepSize);
                }

                _setIterationEndTime(point);
            }
        }

        return currentStepSize;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The breakpoint solver.
    private ODESolver _breakpointSolver;

    // The classname of the breakpoint ODE solver
    private String _breakpointSolverClassName;

    // The normal solver.
    private ODESolver _normalSolver;

    // The classname of the normal ODE solver.
    private String _normalSolverClassName;

    // The classpath for solvers.
    private static String _solverClasspath = "ptolemy.domains.ct.kernel.solver.";
}
