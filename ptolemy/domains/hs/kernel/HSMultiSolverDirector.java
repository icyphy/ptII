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
package ptolemy.domains.hs.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.ct.kernel.CTDynamicActor;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.domains.ct.kernel.CTGeneralDirector;
import ptolemy.domains.ct.kernel.CTStatefulActor;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

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
 model with a HSFSMDirector.)
 <P>
 This director recognizes actors that implement the CTStepSizeControlActor
 interface and adjusts the step size by polling such actors. If all actors
 are content with the current step size, then it attempts to raise the
 step size. If any actor is not satisfied with the current step size, then
 this director reduces the step size. A special case is that if there are
 no CT step size control actors, then this director uses 5 times of the
 current step size or the maximum step size, whichever is smaller.
 <P>
 This director has a parameter <I>ODESolver</I>: The name of the ODE solver that is used
 to integrate ODEs over a time interval.

 @see ptolemy.domains.hs.kernel.HSDirector
 @author  Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class HSMultiSolverDirector extends HSDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public HSMultiSolverDirector() {
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
    public HSMultiSolverDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public HSMultiSolverDirector(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The class name of the normal ODE solver used in iterations for
     *  normal integration. The default value is a string:
     *  "ptolemy.domains.hs.kernel.solver.ExplicitRK23Solver".
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

            String newSolverClassName = ((StringToken) ODESolver.getToken())
                    .stringValue().trim();

            if (newSolverClassName.trim().startsWith(_solverClasspath)) {
                // The old solver name is a parameter starts with
                // "ptolemy.domains.hs.kernel.solver."
                _ODESolverClassName = newSolverClassName;
            } else {
                _ODESolverClassName = _solverClasspath + newSolverClassName;
            }

            ODESolver newODESolver = _instantiateODESolver(_ODESolverClassName);
            _ODESolver = newODESolver;
        } else {
            super.attributeChanged(attribute);
        }
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

        if (_firstFiring) {
            _preamble();
            _firstFiring = false;
        } else {
            // A complete iteration consists is either a discrete phase of 
            // execution or a continuous phase of execution.
            // set up the solver and step size used for this iteration. 
            if (hasCurrentEvent()) {
                _setDiscretePhase(true);
                // Choose 0 as the step size.
                setCurrentStepSize(0.0);
                // The discrete phase of execution resolves the final states at the
                // current time by processing discrete events according to the SR
                // semantics.
                _discretePhaseExecution();
                _setDiscretePhase(false);
                // FIXME: will this be necessary?
                // It seems unnecessary because the immediately following 
                // continuous phase of execution will propogate the resolved states.
                _ODESolver.fire();
            } else {
                if (getExecutiveCTGeneralDirector() == null) {
                    // Choose a suggested step size, which is a guess.
                    setCurrentStepSize(getSuggestedNextStepSize());
                    // Refine the correct step size for the continuous phase execution
                    // with respect to the breakpoint table.
                    setCurrentStepSize(_refinedStepWRTBreakpoints());
                }
                // If the current time is the stop time, then the fire method
                // should immediately return. No further execution is necessary.
                // The final states at the model stop time are resolved before
                // the model stops.
                // Also, if there is a stop request, stop the model immediately.
                if (getModelTime().equals(getModelStopTime()) || _stopRequested) {
                    return;
                }
                // The continuous phase execution resolves the initial states
                // in some future time point through numerical integration.
                _continuousPhaseExecution();
            }
            // FIXME: we distinguish embedded and not-embedded directors. In 
            // particular, an embedded director has no control of the step size. 
        }
        
        if (_debugging && _verbose) {
            _debug(getName(), " end of fire. >>>");
        }
    }

    /** Always return null, because this director can not be an inside director.
     *  @return Null, always.
     */
    public CTGeneralDirector getExecutiveCTGeneralDirector() {
        return null;
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
            // Check the breakpoint table for see if the the earliest
            // breakpoint happens at the current time. If so, remove the
            // breakpoint from the breakpoints table. Otherwise, no change 
            // is made.
            boolean discreteEventExists = 
                _removeCurrentTimeFromBreakpointTable();
            
            // if a discrete event has been found, return immediately.
            if (discreteEventExists) {
                return true;
            }

            // Otherwise, go through event generators to check events.
            // Note that we do not have to go over all event generators.
            // As long as one of them has event, we need a discrete phase of
            // execution.
            Iterator eventGenerators = _schedule
                    .get(HSSchedule.EVENT_GENERATORS).actorIterator();
            while (!discreteEventExists && eventGenerators.hasNext()) {
                CTEventGenerator eventGenerator = 
                    (CTEventGenerator) eventGenerators.next();
                discreteEventExists |= eventGenerator.hasCurrentEvent();
            }

            return discreteEventExists;
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
            _debug("=====> Initializing: " + getFullName() + ":\n");
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
        _schedule = (HSSchedule) getScheduler().getSchedule();

        if (_debugging) {
            // Display schedule
            _debug(getFullName(), " A schedule is computed: ");
            _debug(_schedule.toString());
        }

        // Initialize protected variables and the contained actors.
        super.initialize();

        // Dynamic actors emit their states.
        prefireDynamicActors();

        // Set the suggested next step size. The actual step size will be
        // decided from the prefire() method.
        setSuggestedNextStepSize(getInitialStepSize());

        // register the stop time as a breakpoint.
        if (_debugging) {
            _debug("Set the stop time as a breakpoint: " + getModelStopTime());
        }
        fireAt((Actor) getContainer(), getModelStopTime());
        
        if (_debugging) {
            _debug("=====> End of Initialization of: " + getFullName() + ".\n");
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

        // postfire all continuous actors to commit their states.
        // Note that event generators are postfired.
        updateContinuousStates();

        // Now, the current time is equal to the stop time.
        // If the breakpoints table does not contain the current model time,
        // which means no events are and will be generated,
        // the execution stops by returning false in this postfire method.
        if (getModelTime().equals(getModelStopTime())
                && !getBreakPoints().contains(getModelTime())) {
            return false;
        }

        // predict the next step size.
        setSuggestedNextStepSize(_predictNextStepSize());

        return super.postfire();
    }

    /** Call the prefire() method of the super class and return its value.
     *  Record the current model time as the beginning time of the current
     *  iteration.
     *  @return True if this director is ready to fire.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean prefire() throws IllegalActionException {
        // NOTE: super.prefire() has to be called at the very beginning because
        // it synchronizes the model time with that of the executive director.
        boolean prefireReturns = super.prefire();
        
        // No actors are prefired here. Depending on the phase of execution,
        // actors may be prefired (discrete phase) or not (continuous phase).

        // Record the start time of the current iteration.
        // The begin time of an iteration can be changed only by directors.
        // On the other hand, the model time may be changed by ODE solvers.
        // One example solver is the RK23 solver. It resolves the states in
        // three steps, and it increment the model time at each step. If
        // the CurrentTime actor is involved as one of the state transition
        // actors, it needs to report the model time at each intermediate steps.
        // (The CurrentTime actor reports the model time.)
        // The iterationBegintime will be used for roll back when the current
        // step size is incorrect.
        _setIterationBeginTime(getModelTime());
        
        return prefireReturns;
    }

    /** Invoke prefire() on all DYNAMIC_ACTORS, such as integrators,
     *  and emit their current states.
     *  Return true if all the prefire() methods return true and stop()
     *  is not called. Otherwise, return false.
     *  @return True if all dynamic actors return true from their prefire()
     *  methods and stop() is called.
     *  @exception IllegalActionException If scheduler throws it, or dynamic
     *  actors throw it in their prefire() method, or they can not be prefired.
     */
    public boolean prefireDynamicActors() throws IllegalActionException {
        // NOTE: We will also treat dynamic actors as waveform generators.
        // This is crucial to implement Dirac function.
        HSSchedule schedule = (HSSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(HSSchedule.DYNAMIC_ACTORS)
                .actorIterator();
    
        while (actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor) actors.next();
    
            if (_debugging && _verbose) {
                _debug("Prefire dynamic actor: "
                        + ((Nameable) actor).getName());
            }
    
            boolean ready = actor.prefire();
    
            if (actor instanceof CTCompositeActor) {
                ready = ready
                        && ((CTCompositeActor) actor)
                                .prefireDynamicActors();
            }
    
            // If ready is false, at least one dynamic actor is not
            // ready to fire. This should never happen.
            if (!ready) {
                throw new IllegalActionException(
                        (Nameable) actor,
                        "Actor is not ready to fire. In the CT domain, all "
                                + "dynamic actors should be ready to fire at "
                                + "all times.\n Does the actor only operate on "
                                + "sequence of tokens?");
            }
    
            if (_debugging && _verbose) {
                _debug("Prefire of " + ((Nameable) actor).getName()
                        + " returns " + ready);
            }
        }
    
        // NOTE: Need for integrators to emit their current states so that
        // the state transition actors can operate on the most up-to
        // date inputs and generate derivatives for integrators.
        // Without this, on the first round of integration, the state
        // transition actors will complain that inputs are not ready.
        Iterator integrators = schedule.get(HSSchedule.DYNAMIC_ACTORS)
                .actorIterator();
    
        while (integrators.hasNext() && !_stopRequested) {
            CTDynamicActor dynamic = (CTDynamicActor) integrators.next();
    
            if (_debugging && _verbose) {
                _debug("Emit tentative state "
                        + ((Nameable) dynamic).getName());
            }
    
            dynamic.emitCurrentStates();
        }
        
        return !_stopRequested;
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
                    _ODESolverClassName);
        }

        _ODESolver = _instantiateODESolver(_ODESolverClassName);
        _firstFiring = true;
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
        // NOTE: this method is public because ODE solvers need to advance time.
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
        // Call the postfire method of the continuous actors.
        Iterator actors = _schedule.get(HSSchedule.CONTINUOUS_ACTORS)
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
        actors = _schedule.get(HSSchedule.STATEFUL_ACTORS).actorIterator();

        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor) actors.next();

            if (_debugging) {
                _debug("Postfire " + (Nameable) actor);
            }

            actor.markState();
        }
        
        // Synchronize to real time if necessary.
        if (((BooleanToken) synchronizeToRealTime.getToken()).booleanValue()) {
            long realTime = System.currentTimeMillis() - _timeBase;
            long simulationTime = (long) ((getModelTime().subtract(
                    getModelStartTime()).getDoubleValue()) * 1000);

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
                    throw new IllegalActionException(this, "Sleep Interrupted"
                            + e.getMessage());
                }
            } else {
                if (_debugging) {
                    _debug("Warning: " + getFullName(),
                            " cannot achieve real-time performance"
                                    + " at simulation time " + getModelTime());
                }
            }
        }
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
            _debug("\n !!! continuous phase execution: \n " + 
                    "Resolving the initial states at " + getModelTime(),
                    " (current time) plus step size " + getCurrentStepSize());
        }

        if (_debugging && _verbose) {
            _debug("Using ODE solver: " + _ODESolver.getName());
        }

        // If stop is not requested, keep trying to resolve states, where
        // the current step size may get refined if the resolved
        // states and outputs are inaccurate.
        while (!_stopRequested) {
            while (!_stopRequested) {
                // Reset the round counts and the convergencies to false.
                // NOTE: some solvers have their convergencies depending on
                // the round counts. For example, it takes 3 rounds for a
                // RK-23 solver to solve states.
                _ODESolver._resetRoundCount();
                _ODESolver._setConverged(false);

                // repeating resolving states until states converge.
                while (!_ODESolver._isConverged() && _ODESolver.resolveStates()) {
                    _ODESolver.fire();
                }

                // NOTE: solver.resolveStates() returns true if the states are
                // resolved, and it returns false if the maximum number of
                // iterations is reached but the states have not be resolved.
                if (_ODESolver.resolveStates()) {
                    if (_debugging && _verbose) {
                        _debug("State resolved by solver.");
                    }

                    // Check whether this step is acceptable according to 
                    // the states accuracy
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

                // Restore the saved state of the stateful actors.
                Iterator actors = _schedule.get(HSSchedule.STATEFUL_ACTORS)
                        .actorIterator();

                while (actors.hasNext()) {
                    CTStatefulActor actor = (CTStatefulActor) actors.next();

                    if (_debugging) {
                        _debug("Restore states " + (Nameable) actor);
                    }

                    actor.goToMarkedState();
                }

                if (_debugging && _verbose) {
                    _debug("Execute the system from " + getModelTime()
                            + " with a smaller step size"
                            + getCurrentStepSize());
                }
            }

            // States have been resolved. Note that the current
            // time has been increased by the amount of the
            // step size used in this iteration.
            // If the while loop was terminated by the stop request,
            // jump out of the loop in spite of whether the states
            // have been resolved or not. 
            // States are not saved.
            if (_stopRequested) {
                break;
            }

            // propogate resolved states to output actors.
            _produceOutputs();

            // If event generators are not satisfied with the current step
            // size, refine the step size to a smaller one.
            if (!_isOutputAccurate()) {
                setCurrentStepSize(_refinedStepWRTOutput());

                // Restore the save starting time of this integration.
                setModelTime(getIterationBeginTime());
                // Restore the saved state of the stateful actors.
                Iterator actors = _schedule.get(HSSchedule.STATEFUL_ACTORS)
                        .actorIterator();

                while (actors.hasNext()) {
                    CTStatefulActor actor = (CTStatefulActor) actors.next();
                    if (_debugging) {
                        _debug("Restore states " + (Nameable) actor);
                    }
                    actor.goToMarkedState();
                }

                if (_debugging && _verbose) {
                    _debug("Refine the current step size"
                            + " with a smaller one " + getCurrentStepSize());
                }
            } else {
                // outputs are generated successfully.
                break;
            }
        }
    }

    /**
     * @throws IllegalActionException
     */
    protected void _produceOutputs() throws IllegalActionException {
        Iterator outputActors = _schedule.get(
                HSSchedule.OUTPUT_ACTORS).actorIterator();
        while (outputActors.hasNext() && !_stopRequested) {
            Actor actor = (Actor) outputActors.next();
            actor.fire();
        }
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
        if (_debugging) {
            _debug("\n !!! discrete phase execution at " + getModelTime());
        }
        _iterateSchedule(_schedule.get(HSSchedule.DISCRETE_ACTORS));
    }

    /** Initialize parameters to their default values.
     */
    protected void _initParameters() {
        super._initParameters();

        try {
            _ODESolverClassName = "ptolemy.domains.hs.kernel.solver.ExplicitRK23Solver";
            ODESolver = new Parameter(this, "ODESolver", new StringToken(
                    "ExplicitRK45Solver"));
            ODESolver.setTypeEquals(BaseType.STRING);
            ODESolver.addChoice(new StringToken("ExplicitRK23Solver")
                    .toString());
            ODESolver.addChoice(new StringToken("ExplicitRK45Solver")
                    .toString());
            ODESolver.addChoice(new StringToken("BackwardEulerSolver")
                    .toString());
            ODESolver.addChoice(new StringToken("ForwardEulerSolver")
                    .toString());
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
        
        // FIXME: During the initialize() method, the step size is 0.
        // No step size refinement is needed. What is a better solution?
        if (getCurrentStepSize() == 0) {
            return true;
        }

        boolean accurate = true;

        // Get all the output step size control actors.
        Iterator actors;

        actors = _schedule.get(HSSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS)
                .actorIterator();

        // Ask -ALL- the actors whether the current step size is accurate.
        // THIS IS VERY IMPORTANT!!!
        // NOTE: all actors are guranteed to be asked once even if some
        // actors already set the "accurate" variable to false.
        // The reason is that event generators do not check the step size
        // accuracy in their fire emthods and they need to check the existence
        // of events in the special isOutputAccurate() method.
        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                    .next();
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

        // FIXME: During the initialize() method, the step size is 0.
        // No step size refinement is needed. What is a better solution?
        if (getCurrentStepSize() == 0) {
            return true;
        }

        boolean accurate = true;

        // Get all the output step size control actors.
        Iterator actors;

        actors = _schedule.get(HSSchedule.STATE_STEP_SIZE_CONTROL_ACTORS)
                .actorIterator();

        // Ask -ALL- the actors whether the current step size is accurate.
        // THIS IS VERY IMPORTANT!!!
        // NOTE: all actors are guranteed to be asked once. 
        // The reason is that actors do not check the state accuracy in 
        // their fire() emthods and they need to check it in their special 
        // isOutputAccurate() method.
        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                    .next();
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

    /**
     * @throws IllegalActionException
     */
    protected void _preamble() throws IllegalActionException {
        setCurrentStepSize(0.0);
        _ODESolver.fire();
        _produceOutputs();
        updateContinuousStates();
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

        if (predictedStep == 0.0) {
            // The current step size is 0.0. Predict a positive value to let
            // time advance.
            predictedStep = getInitialStepSize();
        } else {
            predictedStep = 10.0 * getCurrentStepSize();

            boolean foundOne = false;
            Iterator actors = _schedule.get(
                    HSSchedule.STATE_STEP_SIZE_CONTROL_ACTORS).actorIterator();

            while (actors.hasNext()) {
                CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                        .next();
                predictedStep = Math.min(predictedStep, actor
                        .predictedStepSize());
                foundOne = true;
            }

            actors = _schedule.get(HSSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS)
                    .actorIterator();

            while (actors.hasNext()) {
                CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                        .next();
                predictedStep = Math.min(predictedStep, actor
                        .predictedStepSize());
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

        Iterator actors = _schedule.get(
                HSSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS).actorIterator();

        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                    .next();
            refinedStep = Math.min(refinedStep, actor.refinedStepSize());
        }

        if (refinedStep < (0.5 * timeResolution)) {
            if (_triedTheMinimumStepSize) {
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
                _triedTheMinimumStepSize = true;
            }
        } else {
            _triedTheMinimumStepSize = false;
        }

        if (_debugging && _verbose) {
            _debug(getFullName(), "refine step with respect to output to"
                    + refinedStep);
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
        Iterator actors = _schedule.get(
                HSSchedule.STATE_STEP_SIZE_CONTROL_ACTORS).actorIterator();

        while (actors.hasNext()) {
            CTStepSizeControlActor actor = (CTStepSizeControlActor) actors
                    .next();
            double size = actor.refinedStepSize();

            if (_debugging && _verbose) {
                _debug(((NamedObj) actor).getName(), "refines step size to "
                        + size);
            }

            refinedStep = Math.min(refinedStep, size);
        }

        if (refinedStep < (0.5 * getMinStepSize())) {
            throw new IllegalActionException(this,
                    "Cannot resolve new states even using "
                            + "the minimum step size, at time "
                            + getModelTime());
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
                currentStepSize = point.subtract(getModelTime())
                        .getDoubleValue();

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

    // The classname of the normal ODE solver.
    private String _ODESolverClassName;

    // The classpath for solvers.
    private static String _solverClasspath = "ptolemy.domains.hs.kernel.solver.";
    
    private boolean _triedTheMinimumStepSize = false;
    
    private boolean _firstFiring = true;
    
    // Cache the schedule for better performance.
    // This disables the mutation, but this director is a 
    // StaticSchedulingDirector anyway.
    protected HSSchedule _schedule;

    public CTExecutionPhase getExecutionPhase() {
        // TODO Auto-generated method stub
        return null;
    }
}
