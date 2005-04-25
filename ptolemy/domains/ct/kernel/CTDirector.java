/* An abstract base class for directors in the CT domain.

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
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.util.GeneralComparator;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


//////////////////////////////////////////////////////////////////////////
//// CTDirector

/**
   Abstract base class for directors in the CT domain. A CTDirector
   has a CTScheduler which provides static schedules for firing
   the actors in different phases of execution in one iteration.
   <P>
   A CTDirector may have more than one ODE solver. In each phase of execution,
   one ODE solver takes charge of solving the behavior of a model. This solver
   is called the <I>current ODE solver</I>.
   <P>
   The continuous time (CT) domain is a timed domain. There is a global
   notion of time that all the actors are aware of. Time is maintained
   by the director. The method getModelTime() returns the current notion of
   model time. Time can be set by the setModelTime() method, but this
   method should not the called by the actors. Time can only be set
   by directors or their ODE solvers. Because ODE solvers can change time
   in their fire() methods, we need to record the beginning time of an
   iteration to support roll back. The _setIterationBeginTime() method is just
   designed for this purpose. It is called in the prefire() method of each
   iteration to store the beginning time, and the getIterationBeginTime()
   returns the lastest stored time.
   <P>
   This base class maintains a list of parameters that may be used by
   ODE solvers and actors. These parameters are: <Br>
   <LI> <code>startTime</code>: The start time of the
   simulation. This parameter is effective only if the director
   is at the top level. The default value is 0.0.
   <LI> <code>stopTime</code>: The stop time of the simulation.
   This parameter is effective only if the director
   is at the top level. The default value is Infinity, which
   results in execution that does not stop on its own.
   <LI> <code>initStepSize</code>: The suggested integration step size
   by the user. This will be the step size for fixed step
   size ODE solvers if there is no breakpoint. However, it is just
   a hint. The default value is 0.1
   <LI> <code>minStepSize</code>: The minimum step
   size that users want to use in the simulation. The default value is 1e-5.
   <LI> <code>maxStepSize</code>: The maximum step
   size that users want to use in the simulation. Usually used to control
   the simulation speed. The default value is 1.0.
   <LI> <code>maxIterations</code>:
   Used only in implicit ODE solvers. This is the maximum number of
   iterations for finding the fixed point at one time point.
   The default value is 20.
   <LI> <code>errorTolerance</code>: This is the local truncation
   error tolerance, used for controlling the integration accuracy
   in variable step size ODE solvers. If the local truncation error
   at some step size control actors are greater than this tolerance, then the
   integration step is considered to have failed, and should be restarted with
   a reduced step size. The default value is 1e-4.
   <LI> <code>valueResolution</code>:
   This is used to control the convergence of fixed point iterations.
   If in two successive iterations the difference of the state variables
   is less than this resolution, then the fixed point is considered to have
   reached. The default value is 1e-6.
   <P>
   This director maintains a breakpoint table to record all predictable
   breakpoints that are greater than or equal to
   the current time. The breakpoints are sorted in their chronological order.
   Breakpoints at the same time are considered to be identical, and the
   breakpoint table does not contain duplicate time points. A breakpoint can
   be inserted into the table by calling the fireAt() method. The fireAt method
   may be requested by the director, which inserts the stop time of the
   execution. The fireAt method may also be requested by actors and the
   requested firing time will be inserted into the breakpoint table.

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (hyzheng)
*/
public abstract class CTDirector extends StaticSchedulingDirector
    implements TimedDirector, CTGeneralDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values. A CTScheduler
     *  is created.
     */
    public CTDirector() {
        this(null);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. All the parameters take their default values.
     *  A CTScheduler is created.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container. May be thrown by a derived class.
     *  @exception NameDuplicationException If the name collides with
     *   a property in the container.
     */
    public CTDirector(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();

        try {
            setScheduler(new CTScheduler(container.workspace()));
        } catch (IllegalActionException e) {
            // This should never occur.
            throw new InternalErrorException(this.getFullName()
                + "Error setting a CTScheduler.");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("There is already a scheduler"
                + " with name " + this.getFullName());
        }
    }

    /** Construct a director in the workspace with an empty name.
     *  If the argument is null, then the default workspace will be used.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values. A CTScheduler
     *  is created.
     *  @param workspace The workspace of this object.
     */
    public CTDirector(Workspace workspace) {
        super(workspace);
        _initParameters();

        try {
            setScheduler(new CTScheduler(workspace));
        } catch (IllegalActionException e) {
            // This should never occur.
            throw new InternalErrorException(this.getFullName()
                + "Error setting a CTScheduler.");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("There is already a scheduler"
                + " with name " + this.getFullName());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Error tolerance for local truncation error control, only effective
     *  in variable step size methods.
     *  The default value is 1e-4, and the type is double.
     */
    public Parameter errorTolerance;

    /** User's hint for the initial integration step size.
     *  The default value is 0.1, and the type is double.
     */
    public Parameter initStepSize;

    /** The maximum number of iterations in looking for a fixed point.
     *  The default value is 20, and the type is int.
     */
    public Parameter maxIterations;

    /** User's guide for the maximum integration step size.
     *  The default value is 1.0, and the type is double.
     */
    public Parameter maxStepSize;

    /** User's guide for the minimum integration step size.
     *  The default value is 1e-5, and the type is double.
     */
    public Parameter minStepSize;

    /** Starting time of the simulation. The default value is 0.0,
     *  and the type is double.
     */
    public Parameter startTime;

    /** Stop time of the simulation. The default value is Infinity,
     *  and the type is double.
     */
    public Parameter stopTime;

    /** Indicator whether the execution will synchronize to real time. The
     *  default value is false, and the type is boolean.
     */
    public Parameter synchronizeToRealTime;

    /** Value resolution in looking for a fixed-point state resolution.
     *  The default value is 1e-6, and the type is double.
     */
    public Parameter valueResolution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the new parameter value
     *  is not valid.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (_debugging) {
            _debug("Updating CTDirector parameter: ", attribute.getName());
        }

        if (attribute == startTime) {
            double startTimeValue = ((DoubleToken) startTime.getToken())
                            .doubleValue();
            _startTimeValue = startTimeValue;
        } else if (attribute == stopTime) {
            double stopTimeValue = ((DoubleToken) stopTime.getToken())
                            .doubleValue();
            _stopTimeValue = stopTimeValue;
        } else if (attribute == initStepSize) {
            double value = ((DoubleToken) initStepSize.getToken()).doubleValue();

            if (value < 0.0) {
                throw new IllegalActionException(this,
                    "Cannot set a negative step size.");
            }

            _initStepSize = value;
        } else if (attribute == errorTolerance) {
            double value = ((DoubleToken) errorTolerance.getToken())
                            .doubleValue();

            if (value < 0.0) {
                throw new IllegalActionException(this,
                    "Cannot set a negative error tolerance.");
            }

            _errorTolerance = value;
        } else if (attribute == minStepSize) {
            double value = ((DoubleToken) minStepSize.getToken()).doubleValue();

            if (value < 0.0) {
                throw new IllegalActionException(this,
                    "Cannot set a negative step size.");
            }

            _minStepSize = value;
        } else if (attribute == maxStepSize) {
            double value = ((DoubleToken) maxStepSize.getToken()).doubleValue();

            if (value < 0.0) {
                throw new IllegalActionException(this,
                    "Cannot set a negative step size.");
            }

            _maxStepSize = value;
        } else if (attribute == valueResolution) {
            double value = ((DoubleToken) valueResolution.getToken())
                            .doubleValue();

            if (value < 0.0) {
                throw new IllegalActionException(this,
                    "Cannot set a negative value resolution.");
            }

            _valueResolution = value;
        } else if (attribute == maxIterations) {
            int value = ((IntToken) maxIterations.getToken()).intValue();

            if (value < 1) {
                throw new IllegalActionException(this,
                    "Cannot set a zero or negative iteration number.");
            }

            _maxIterations = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return true if the director can be an inside director, i.e.
     *  a director of an opaque composite actor not at the top level.
     *  This method is abstract in this class and derived classes need to
     *  override this to show whether it can serve as an inside director.
     *  @return True if this director can be an inside director.
     */
    public abstract boolean canBeInsideDirector();

    /** Return true if the director can be a top-level director.
     *  This method is abstract in this class and derived classes need to
     *  override this to show whether it can serve as a top-level director.
     *  @return True if this director can be a top-level director.
     */
    public abstract boolean canBeTopLevelDirector();

    /** Override the fire() method of the super class. This method is
     *  abstract in this abstract base class. The derived classes need to
     *  override this method for concrete implementation.
     */
    public abstract void fire() throws IllegalActionException;

    /** Handle firing requests from the contained actors.
     *  If the specified time is earlier than the current time, or the
     *  breakpoint table is null, throw an exception. Otherwise, insert
     *  the specified time into the breakpoint table.

     *  @param actor The actor that requests the firing.
     *  @param time The requested firing time.
     *  @exception IllegalActionException If the time is earlier than
     *  the current time, or the breakpont table is null.
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        // Check if the request time is earlier than the current time.
        Time currentTime = getModelTime();

        if (time.compareTo(currentTime) < 0) {
            throw new IllegalActionException((Nameable) actor,
                "Requested fire time: " + time + " is earlier than"
                + " the current time." + currentTime);
        }

        // check the validity of breakpoint table
        if (_breakpoints == null) {
            throw new IllegalActionException(
                "Breakpoint table can not be null!");
        }

        if (_debugging) {
            String name = ((Nameable) actor).getName();
            _debug("----> " + name + " requests refiring at " + time);
        }

        // insert a new breakpoint into the breakpoint table.
        _breakpoints.insert(time);
    }

    /** Return the breakpoint table. The result can be null if the breakpoint
     *  table has never been created.
     *  @return The breakpoint table.
     */
    public final TotallyOrderedSet getBreakPoints() {
        // This method is final for performance reason.
        return _breakpoints;
    }

    /** Return the current ODE solver used to resolve states by the director.
     *  @return The current ODE solver used to resolve states by the director.
     */
    public final ODESolver getCurrentODESolver() {
        // This method is final for performance reason.
        return _currentSolver;
    }

    /** Return the current integration step size.
     *  @return The current integration step size.
     *  @see #setCurrentStepSize
     */
    public double getCurrentStepSize() {
        return _currentStepSize;
    }

    /** Return the local truncation error tolerance, used by
     *  variable step size solvers.
     *  @return The local truncation error tolerance.
     */
    public final double getErrorTolerance() {
        // This method is final for performance reason.
        return _errorTolerance;
    }

    /** Get the current execution phase of this director.
     *  In this abstract class, always return the local execution phase.
     *  The derived classes, specially the CTEmbeddedDirector, needs to
     *  override this method.
     *  @return The current execution phase of this director.
     */
    public CTExecutionPhase getExecutionPhase() {
        return _executionPhase;
    }

    /** Return the initial step size.
     *  @return The initial step size.
     */
    public final double getInitialStepSize() {
        // This method is final for performance reason.
        return _initStepSize;
    }

    /** Return the begin time of the current iteration.
     *  @return The begin time of the current iteration.
     */
    public Time getIterationBeginTime() {
        return _iterationBeginTime;
    }

    /** Return the end time of the current iteration.
     *  @return The end time of the current iteration.
     */
    public Time getIterationEndTime() {
        return _iterationEndTime;
    }

    /** Return the maximum number of iterations in a fixed point
     *  calculation. If the iteration has exceeded this number
     *  and the fixed point is still not found, then the algorithm
     *  is considered to have failed.
     *  @return The maximum number of iterations when calculating
     *  fixed points.
     */
    public final int getMaxIterations() {
        // This method is final for performance reason.
        return _maxIterations;
    }

    /** Return the maximum step size used in variable step size
     *  ODE solvers.
     *  @return The maximum step size.
     */
    public final double getMaxStepSize() {
        // This method is final for performance reason.
        return _maxStepSize;
    }

    /** Return the minimum step size used in variable step size
     *  ODE solvers.
     *  @return The minimum step size.
     */
    public final double getMinStepSize() {
        // This method is final for performance reason.
        return _minStepSize;
    }

    /** Return the current iteration begin time plus the current step size.
     *  @return The iteration begin time plus the current step size.
     */
    public Time getModelNextIterationTime() {
        return getIterationBeginTime().add(getCurrentStepSize());
    }

    /** Return the start time.
     *  @return the start time.
     */
    public final Time getModelStartTime() {
        // This method is final for performance reason.
        return _startTime;
    }

    /** Return the stop time.
     *  @return the stop time.
     */
    public final Time getModelStopTime() {
        // This method is final for performance reason.
        return _stopTime;
    }

    /** Return the current iteration begin time plus the current step size.
     *  @return The iteration begin time plus the current step size.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelNextIterationTime}
     */
    public double getNextIterationTime() {
        return getModelNextIterationTime().getDoubleValue();
    }

    /** Return the suggested next step size. The suggested step size is
     *  the minimum step size that all the step-size-control actors suggested
     *  at the end of last integration step. It is the prediction
     *  of the new step size.
     *  @return The suggested next step size.
     *  @see #setSuggestedNextStepSize
     */
    public final double getSuggestedNextStepSize() {
        // This method is final for performance reason.
        return _suggestedNextStepSize;
    }

    /** Return the value resolution, used for testing if an implicit method
     *  has reached the fixed point. Two values that are differed less than
     *  this accuracy are considered identical in the fixed-point
     *  calculation.
     *
     *  @return The value resolution for finding fixed point.
     */
    public final double getValueResolution() {
        // This method is final for performance reason.
        return _valueResolution;
    }

    /** Initialize model after type resolution.
     *  In addition to calling the initialize() method of the super class,
     *  this method records the current system time as the "real" starting
     *  time of the execution. This starting time is used when the
     *  execution is synchronized to real time. This method also resets
     *  the protected variables of this director.
     *
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        // Reset _postFireReturns to true.
        _postfireReturns = true;

        // Record starting point of the real time (the computer system time)
        // in case the director is synchronized to the real time.
        _timeBase = System.currentTimeMillis();

        // set current time and initialize actors.
        super.initialize();
    }

    /** Return true if this is the discrete phase of execution.
     *  @return True if this is the discrete phase of execution.
     */
    public boolean isDiscretePhase() {
        return _discretePhase;
    }

    /** Return true if the actor has been prefired in the current iteration.
     *  @param actor The actor about which we are querying.
     *  @return True if the actor has been prefired.
     *  @see #setPrefireComplete(Actor)
     */
    public boolean isPrefireComplete(Actor actor) {
        return _prefiredActors.contains(actor);
    }

    /** Return a new CTReceiver.
     *  @return A new CTReceiver.
     */
    public Receiver newReceiver() {
        return new CTReceiver();
    }

    /** If the stop() method has not been called and all the actors return
     *  true at postfire, return true. Otherwise, return false.
     *  If this director is not at the top level and the breakpoint table
     *  is not empty, request a refiring at the first breakpoint.
     *  @return True if the Director wants to be fired again in the future.
     *  @exception IllegalActionException If refiring can not be granted.
     */
    public boolean postfire() throws IllegalActionException {
        if (!_isTopLevel() && (getBreakPoints().size() > 0)) {
            Time time = (Time) getBreakPoints().removeFirst();
            CompositeActor container = (CompositeActor) getContainer();
            container.getExecutiveDirector().fireAt(container, time);
        }

        boolean postfireReturns = _postfireReturns && !_stopRequested;

        if (_debugging && _verbose) {
            _debug("Postfire returns " + postfireReturns + " at: "
                + getModelTime());
        }

        return postfireReturns;
    }

    /** Clear the set of actors that have been prefired.
     */
    public void prefireClear() {
        _prefiredActors.clear();
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
        _setExecutionPhase(CTExecutionPhase.PREFIRING_DYNAMIC_ACTORS_PHASE);

        try {
            CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
            Iterator actors = schedule.get(CTSchedule.DYNAMIC_ACTORS)
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
                    _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
                    throw new IllegalActionException((Nameable) actor,
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
            Iterator integrators = schedule.get(CTSchedule.DYNAMIC_ACTORS)
                                                       .actorIterator();

            while (integrators.hasNext() && !_stopRequested) {
                CTDynamicActor dynamic = (CTDynamicActor) integrators.next();

                if (_debugging && _verbose) {
                    _debug("Emit tentative state "
                        + ((Nameable) dynamic).getName());
                }

                dynamic.emitCurrentStates();
            }
        } finally {
            _setExecutionPhase(CTExecutionPhase.UNKNOWN_PHASE);
        }

        return !_stopRequested;
    }

    /** Preinitialize the model for an execution. This method is
     *  called only once for each simulation. The schedule is invalidated,
     *  statistical variables and the breakpoint table are cleared, all actors
     *  are preinitialized.
     *  If this director does not have a container and a scheduler, or the
     *  director does not fit in this level of hierarchy, an
     *  IllegalActionException will be thrown.
     *  <p>
     *  Note, however, time does not have a meaning when actors are
     *  preinitialized. So actors must not use a notion of time in their
     *  preinitialize() methods.
     *
     *  @exception IllegalActionException If this director has no
     *  container, or this director does not fit this level of hierarchy,
     *  or there is no scheduler.
     */
    public void preinitialize() throws IllegalActionException {
        if (_debugging) {
            _debug(getFullName(), "preinitializing.");
        }

        // Verify that this director resides in an approriate level
        // of hierarchy.
        Nameable nameable = getContainer();

        if (!(nameable instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                "has no CompositeActor container.");
        }

        CompositeActor container = (CompositeActor) nameable;

        if (container.getContainer() != null) {
            if (!canBeInsideDirector()) {
                throw new IllegalActionException(this,
                    "cannot serve as an inside director.");
            }
        } else {
            if (!canBeTopLevelDirector()) {
                throw new IllegalActionException(this,
                    "cannot serve as an top-level director.");
            }
        }

        // Construct a scheduler.
        CTScheduler scheduler = (CTScheduler) getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException(this, "has no scheduler.");
        }

        // Invalidate schedule and force a reconstructition of the schedule.
        scheduler.setValid(false);

        // Initialize the local variables except the time objects.
        _initializeLocalVariables();

        super.preinitialize();

        // Time objects can only be initialized at the end of this method after
        // the time scale and time resolution are evaluated.
        // NOTE: Time resolution is provided by the preinitialize() method in
        // the super class (Director). So, this method must be called
        // after the super.preinitialize() is called.
        // NOTE: _timeBase is not initialized here but in the initialize()
        // method instead in order to provide more accurate real-time
        // information.
        _startTime = new Time(this, _startTimeValue);
        _stopTime = new Time(this, _stopTimeValue);
        _iterationBeginTime = _startTime;
        _iterationEndTime = _stopTime;
    }

    /** Set the current step size. Only CT directors can call this method.
     *  Solvers and actors must not call this method.
     *  @param stepSize The step size to be set.
     *  @see #getCurrentStepSize
     */
    public void setCurrentStepSize(double stepSize) {
        if (_debugging) {
            _debug("----- Setting the current step size to " + stepSize);
        }

        _currentStepSize = stepSize;
    }

    /** Mark the specified actor as having been prefired in the current
     *  iteration.
     *  @see #isPrefireComplete(Actor)
     *  @param actor The actor to be marked.
     */
    public void setPrefireComplete(Actor actor) {
        _prefiredActors.add(actor);
    }

    /** Set the suggested next step size. If the argument is larger than
     *  the maximum step size, then set the suggested next step size to
     *  the maximum step size.
     *  @param stepsize The suggested next step size.
     *  @see #getSuggestedNextStepSize
     */
    public void setSuggestedNextStepSize(double stepsize) {
        if (stepsize > getMaxStepSize()) {
            _suggestedNextStepSize = getMaxStepSize();
        } else {
            _suggestedNextStepSize = stepsize;
        }
    }

    /** Return an array of suggested ModalModel directors  to use
     *  with CTDirector. The default director is HSDirector, which
     *  is used in hybird system. FSMDirector could also be used
     *  with CTDirector in some simple cases.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        // This method does not call the method defined in the super class,
        // because this method provides complete new information.
        // Default is a HSDirector, while FSMDirector is also in the array.
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.HSDirector";
        defaultSuggestions[1] = "ptolemy.domains.fsm.kernel.FSMDirector";
        return defaultSuggestions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create and initialize all parameters to their default values.
     *  This is called by the constructor.
     */
    protected void _initParameters() {
        try {
            startTime = new Parameter(this, "startTime");
            startTime.setExpression("0.0");
            startTime.setTypeEquals(BaseType.DOUBLE);

            stopTime = new Parameter(this, "stopTime");
            stopTime.setExpression("Infinity");
            stopTime.setTypeEquals(BaseType.DOUBLE);

            initStepSize = new Parameter(this, "initStepSize");
            initStepSize.setExpression("0.1");
            initStepSize.setTypeEquals(BaseType.DOUBLE);

            minStepSize = new Parameter(this, "minStepSize");
            minStepSize.setExpression("1e-5");
            minStepSize.setTypeEquals(BaseType.DOUBLE);

            maxStepSize = new Parameter(this, "maxStepSize");
            maxStepSize.setExpression("1.0");
            maxStepSize.setTypeEquals(BaseType.DOUBLE);

            maxIterations = new Parameter(this, "maxIterations");
            maxIterations.setExpression("20");
            maxIterations.setTypeEquals(BaseType.INT);

            errorTolerance = new Parameter(this, "errorTolerance");
            errorTolerance.setExpression("1e-4");
            errorTolerance.setTypeEquals(BaseType.DOUBLE);

            valueResolution = new Parameter(this, "valueResolution");
            valueResolution.setExpression("1e-6");
            valueResolution.setTypeEquals(BaseType.DOUBLE);

            synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
            synchronizeToRealTime.setExpression("false");
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

            timeResolution.setVisibility(Settable.FULL);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error: " + e);
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                "Parameter name duplication: " + ex);
        }
    }

    /** Instantiate an ODESolver from its classname. Given the solver's full
     *  class name, this method will try to instantiate it by looking
     *  for the corresponding java class.
     *  @param className The solver's full class name.
     *  @return a new ODE solver.
     *  @exception IllegalActionException If the solver can not be created.
     */
    protected final ODESolver _instantiateODESolver(String className)
        throws IllegalActionException {
        ODESolver newSolver;

        if (_debugging) {
            _debug("instantiating solver..." + className);
        }

        try {
            Class solver = Class.forName(className);
            newSolver = (ODESolver) solver.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this,
                "ODESolver: " + className + " is not found.");
        } catch (InstantiationException e) {
            throw new IllegalActionException(this,
                "ODESolver: " + className + " instantiation failed.");
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(this,
                "ODESolver: " + className + " is not accessible.");
        }

        newSolver._makeSolverOf(this);
        return newSolver;
    }

    /** Set the current ODE solver to be the given ODE solver.
     *  @param solver The solver to set.
     *  @exception  IllegalActionException Not thrown in this base class.
     *  It may be thrown by the derived classes if the solver is not
     *  appropriate.
     */
    protected final void _setCurrentODESolver(ODESolver solver)
        throws IllegalActionException {
        _currentSolver = solver;
    }

    /** Set the current phase of execution as a discrete phase. The value
     *  set can be returned by the isDiscretePhase() method.
     *  @param discrete True if this is the discrete phase.
     */
    protected final void _setDiscretePhase(boolean discrete) {
        _discretePhase = discrete;
    }

    /** Set the execution phase to the given phase.
     *  @param phase The current phase of the CT director.
     */
    protected final void _setExecutionPhase(CTExecutionPhase phase) {
        _executionPhase = phase;
    }

    /** Set the iteration begin time. The iteration begin time is
     *  the start time for one integration step. This variable is used
     *  when the integration step is failed, and need to be restarted
     *  with another step size.
     *  @param time The iteration begin time.
     */
    protected final void _setIterationBeginTime(Time time) {
        _iterationBeginTime = time;
    }

    /** Set the iteration end time. The iteration end time is
     *  the stop time for the current integration. This variable is used
     *  to ensure an iteration ends at an expected time.
     *  <p>
     *  If the argument is earlier than the current time, then an
     *  InvalidStateException will be thrown.
     *  @param time The iteration end time.
     */
    protected final void _setIterationEndTime(Time time) {
        if (time.compareTo(getModelTime()) < 0) {
            throw new InvalidStateException(this,
                " Iteration end time" + time + " is earlier than"
                + " the current time." + getModelTime());
        }

        _iterationEndTime = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** This flag will be set to false if any actor returns false from
     *  its postfire().
     */
    protected boolean _postfireReturns = true;

    /** The real starting time in term of system millisecond counts.
     */
    protected long _timeBase;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Initialize the local variables of this CTDirector. This is called in
    // the preinitialize method.
    // NOTE: Time objects are not initialized here. They are initialized at
    // the end of the preinitialize method of this director.
    private void _initializeLocalVariables() throws IllegalActionException {
        _errorTolerance = ((DoubleToken) errorTolerance.getToken()).doubleValue();
        _initStepSize = ((DoubleToken) initStepSize.getToken()).doubleValue();
        _maxIterations = ((IntToken) maxIterations.getToken()).intValue();
        _maxStepSize = ((DoubleToken) maxStepSize.getToken()).doubleValue();
        _minStepSize = ((DoubleToken) minStepSize.getToken()).doubleValue();
        _valueResolution = ((DoubleToken) valueResolution.getToken())
                        .doubleValue();

        _currentSolver = null;
        _prefiredActors = new HashSet();
        _currentStepSize = _initStepSize;
        _suggestedNextStepSize = _initStepSize;

        // A simulation always starts with a discrete phase execution.
        _discretePhase = true;
        _executionPhase = CTExecutionPhase.UNKNOWN_PHASE;

        // clear the existing breakpoint table or
        // create a breakpoint table if necessary
        if (_debugging) {
            _debug(getFullName(), "create/clear break point table.");
        }

        TotallyOrderedSet breakpoints = getBreakPoints();

        if (breakpoints != null) {
            breakpoints.clear();
        } else {
            _breakpoints = new TotallyOrderedSet(new GeneralComparator());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Indicate whether this is a breakpoint iteration.
    private boolean _breakpointIteration = false;

    // A table for breakpoints.
    private TotallyOrderedSet _breakpoints;

    // NOTE: all the following private variables are initialized
    // in the _initializeLocalVariables() method before their usage.
    // Current ODE solver.
    private ODESolver _currentSolver = null;

    // Simulation step sizes.
    private double _currentStepSize;

    // Indicate that this is the discrete phase.
    private boolean _discretePhase;

    // the error tolerance for state resolution
    private double _errorTolerance;

    // The private variable indicates the current execution phase of this
    // director.
    private CTExecutionPhase _executionPhase;

    // the first step size used by solver.
    private double _initStepSize;

    // he iteration begin time is the start time for one integration step.
    // This variable is used when the integration step is failed, and need
    // to be restarte with another step size.
    private Time _iterationBeginTime;

    // the iteration end time.
    private Time _iterationEndTime;
    private int _maxIterations;
    private double _maxStepSize;
    private double _minStepSize;

    // Collection of actors that have been prefired()
    private Set _prefiredActors = new HashSet();

    // Local copies of parameters.
    private Time _startTime;
    private double _startTimeValue;
    private Time _stopTime;
    private double _stopTimeValue;
    private double _suggestedNextStepSize;
    private double _valueResolution;
}
