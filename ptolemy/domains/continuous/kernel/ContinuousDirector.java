/* Continuous-time director.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.domains.continuous.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.GeneralComparator;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TotallyOrderedSet;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// ContinuousDirector

/**
 The continuous time domain is a timed domain that supports
 continuous-time signals, discrete-event signals, and mixtures of the
 two. There is a global notion of time that all the actors are aware of.
 The semantics of this domain is given in:
 Edward A. Lee and Haiyang Zheng, "Operational Semantics of Hybrid Systems,"
 Invited paper in Proceedings of Hybrid Systems: Computation and Control
 (HSCC) LNCS 3414, Zurich, Switzerland, March 9-11, 2005.
 <p>
 A signal is a set of "events," each of which has a tag and value.
 The set of values includes a special element, called "absent", denoting
 the absence of a (normal) value.
 This director uses superdense time, where every event has a tag
 that is a member of the set RxN.
 R is a connected subset of the real numbers (giving "time",
 and approximated by instances of the Time class),
 and N is the natural numbers (giving an "index").
 At a time <i>t</i>, a signal
 may have multiple values in sequence with tags
 (<i>t</i>, 0), (<i>t</i>, 1)... Its "initial value" is the value
 at tag (<i>t</i>, 0). It typically settles to
 a "final value" after a finite number of indices.
 If it fails to settle to a final value, the signal is said to
 have a "stuttering Zeno" condition, and time will not progress.
 <p>
 In our semantics, all signals are piecewise continuous.
 This means that the initial value, as a function of time,
 is continuous on the left, the final value, as a function
 of time, is continuous on the right, and the signal
 has exactly one value at all times except those
 on a discrete subset D.
 <p>
 A purely continouous signal has exactly one value at
 all times, meaning that the final value equals the initial
 value at all times.
 A purely discrete signal has
 initial value "absent" and final value "absent" at all
 times, and at a discrete subset of the times, it may
 have non-absent values. The only signal that is both
 purely continuous and purely discrete is the one that
 is absent at all tags.
 <p>
 A signal may be mostly continuous,
 but have multiple values at a discrete subset of times.
 These multiple values semantically represent discontinuities
 in an otherwise continuous signal.
 <p>
 The set of times where signals have more than one distinct value
 are a discrete subset D of the time set. These times are called
 "breakpoints" and are treated specially in the execution.
 Between these times, an ordinary differential equation (ODE)
 solver governs the execution. Initial values are always given
 by the ODE solver.
 <P>
 The parameters of this director are:
 <UL>
 <LI> <i>startTime</i>: The start time of the
 simulation. This parameter is ignored if the director
 is not at the top level. The default value is 0.0.
 
 <LI> <i>stopTime</i>: The stop time of the execution.
 When the current time reaches this value, postfire() will return false.
 
 <LI> <i>initStepSize</i>: The suggested integration step size.
 If the ODE solver is a fixed step size solver, then this parameter
 gives the step size taken. Otherwise, at the start of execution,
 this provides the first guess for the integration step size.
 In later iterations, the integrators provide the suggested step
 size. This is a double with default value 0.1
 
 <LI> <i>maxStepSize</i>: The maximum step size.
 This can be used to prevent the solver from too few
 samples of signals. That is, for certain models, it might
 be possible to get accurate results for very large step
 sizes, but plots of the signals may be misleading (even
 if they are accurate) because they represent the signal
 with only a few samples. The default value is 1.0.
 
 <LI> <i>maxIterations</i>:
 The maximum number of iterations that an implicit
 ODE solver will use to resolve the states of integrators.
 An example of an implicit solver is the BackwardsEuler solver.
 The default value is 20, and the type is int.
 FIXME: Currently, this package implements no implicit solvers.

 <LI> <i>ODESolver</i>:
 The class name of the ODE solver used for integration. 
 This is a string that defaults to "ExplicitRK45Solver".
 Solvers are all required to be in package
 "ptolemy.domains.continuous.kernel.solver".
    
 <LI> <i>errorTolerance</i>: This is the local truncation
 error tolerance, used for controlling the integration accuracy
 in variable step size ODE solvers, and also for determining whether
 unpredictable breakpoints have been accurately identified. Any actor
 that implements ContinuousStepSizeControl may use this error
 tolerance to determine whether the current step is accurate.
 For example, if the local truncation error
 in some integrator is greater than this tolerance, then the
 integration step is considered to have failed, and should be restarted with
 a reduced step size. The default value is 1e-4.
 
 <LI> <i>valueResolution</i>:
 This is used to control the convergence of fixed point iterations.
 If in two successive iterations the difference of the state variables
 is less than this resolution, then the fixed point is considered to have
 reached. The default value is 1e-6.
 </UL>
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
 <p>
 This director is based on the CTDirector by Jie Liu and Haiyang Zheng,
 but it has a much simpler scheduler.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class ContinuousDirector extends FixedPointDirector implements
        TimedDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container. May be thrown by a derived class.
     *  @exception NameDuplicationException If the name collides with
     *   a property in the container.
     */
    public ContinuousDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
        setScheduler(new ContinuousScheduler(this, "scheduler"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Error tolerance for data values, used with variable step
     *  size solvers to determine whether the current step size is accurate.
     *  The default value is 1e-4, and the type is double.
     */
    public Parameter errorTolerance;

    /** User's hint for the initial integration step size.
     *  The default value is 0.1, and the type is double.
     */
    public Parameter initStepSize;

    /** The maximum number of rounds that an
     *  ODE solver can use to resolve the states of integrators.
     *  Many solvers, such as RK 2-3 and RK 4-5, use a fixed number
     *  of rounds (3 and 6, respectively). Implicit ODE solvers use
     *  however many rounds it takes to converge to a solution within
     *  a specified accuracy (given by <i>errorTolerance</i>).
     *  An example of an implicit solver is the BackwardsEuler solver.
     *  This parameter limits the number of rounds.
     *  The default value is 20, and the type is int.
     */
    public Parameter maxIterations;

    /** The maximum step size.
     *  The default value is 1.0, and the type is double.
     */
    public Parameter maxStepSize;

    /** The class name of the ODE solver used for integration. 
     *  This is a string that defaults to "ExplicitRK45Solver".
     *  Solvers are all required to be in package
     *  "ptolemy.domains.continuous.kernel.solver".
     */
    public Parameter ODESolver;

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
        _debug("Updating ContinuousDirector parameter: ", attribute.getName());

        if (attribute == errorTolerance) {
            double value = ((DoubleToken) errorTolerance.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative error tolerance.");
            }
            _errorTolerance = value;
        } else if (attribute == initStepSize) {
            double value = ((DoubleToken) initStepSize.getToken())
            .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                    "Cannot set a negative step size.");
            }
            _initStepSize = value;
        } else if (attribute == maxIterations) {
            int value = ((IntToken) maxIterations.getToken()).intValue();

            if (value < 1) {
                throw new IllegalActionException(this,
                        "Cannot set a zero or negative iteration number.");
            }
            _maxIterations = value;
        } else if (attribute == maxStepSize) {
            double value = ((DoubleToken) maxStepSize.getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative step size.");
            }
            _maxStepSize = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** FIXME
     */
    public void fire() throws IllegalActionException {
        
        // Make sure time does not pass a breakpoint.
        _refineStepWRTBreakpoints();
        
        if (_debugging) {
            _debug("Execute the system from "
                    + getModelTime()
                    + " with a step size "
                    + _currentStepSize
                    + ".");
        }

        // Iterate until we conclude that the step size is acceptable.
        while (!_stopRequested) {
            // Some solvers take multiple rounds to perform an integration step.
            // Tell the solver we are starting an integration step.
            _ODESolver._reset();

            // Iterate until the solver is done with the integration step
            // or we reach the maximum number of iterations.
            // If the step size is zero, then we take only one round.
            int iterations = 0;
            while (!_ODESolver._isStepFinished() && iterations < _maxIterations) {
                if (_currentStepSize > 0) {
                    Time iterationBeginTime = getIterationBeginTime();
                    setModelTime(
                            iterationBeginTime.add(_currentStepSize
                                    * _ODESolver._incrementRound()));
                }
                _resetAllReceivers();
                super.prefire();
                super.fire();
                if (_currentStepSize == 0) break;
            }
            if (_isStepSizeAccurate() && iterations < _maxIterations) {
                // All actors agree with the current step size.
                // The integration step is finished.
                break;
            } else {
                // If any step size control actor is unsatisfied with the 
                // current step size, refine the step size to a smaller one.
                _setCurrentStepSize(refinedStepSize());
                _debug("Refine the current step size"
                        + " with a smaller one " + _currentStepSize);
                
                // Restore the saved state of the stateful actors, 
                // including the save starting time of this integration.
                setModelTime(getIterationBeginTime());
                
                // FIXME: may generate StatefulActor set for more 
                // efficient execution.
                Schedule schedule = getScheduler().getSchedule();
                Iterator firingIterator = schedule.firingIterator();
                while (firingIterator.hasNext() && !_stopRequested) {
                    Actor actor = ((Firing) firingIterator.next()).getActor();
                    if (actor instanceof ContinuousStatefulActor) {
                        _debug("Restoring states of " + actor);
                        ((ContinuousStatefulActor) actor).goToMarkedState();
                    }
                }
            }
        }

    }

    /** Handle firing requests from the contained actors by registrating
     *  breakpoints.
     *  If the specified time is earlier than the current time, or the
     *  breakpoint table is null, throw an exception. Otherwise, insert
     *  the specified time into the breakpoint table.

     *  @param actor The actor that requests the firing.
     *  @param time The requested firing time.
     *  @exception IllegalActionException If the time is earlier than
     *  the current time, or the breakpoint table is null.
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        _debug("----> " + actor.getName() + " requests refiring at " + time);

        // Check if the request time is earlier than the current time.
        Time currentTime = getModelTime();
        if (time.compareTo(currentTime) < 0) {
            throw new IllegalActionException(actor, "Requested time: " + time 
                    + " is earlier than the current time: " + currentTime);
        }

        // insert a new breakpoint into the breakpoint table.
        _breakpoints.insert(time);
    }

    /** Return the current integration step size.
     *  @return The current integration step size.
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

    /** Return the begin time of the current iteration.
     *  The solvers use this.
     *  @return The begin time of the current iteration.
     */
    public Time getIterationBeginTime() {
        return _iterationBeginTime;
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

    /** Return the current iteration begin time plus the current step size.
     *  @return The iteration begin time plus the current step size.
     */
    public Time getModelNextIterationTime() {
        return getIterationBeginTime().add(_currentStepSize);
    }

    /** Return the start time, which is the value of the
     *  <i>startTime</i> parameter, represented as an instance
     *  of the Time class. This will be null before preinitialize()
     *  is called. 
     *  @return The start time.
     */
    public final Time getModelStartTime() {
        // This method is final for performance reason.
        return _startTime;
    }

    /** Return the stop time, which is the value of the
     *  <i>stopTime</i> parameter, represented as an instance
     *  of the Time class. This will be null before preinitialize()
     *  is called.
     *  @return The stop time.
     */
    public final Time getModelStopTime() {
        // This method is final for performance reason.
        return _stopTime;
    }

    /** Initialize model after type resolution.
     *  In addition to calling the initialize() method of the super class,
     *  this method records the current system time as the "real" starting
     *  time of the execution. This starting time is used when the
     *  execution is synchronized to real time.
     *
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        // Record starting point of the real time (the computer system time)
        // in case the director is synchronized to the real time.
        _timeBase = System.currentTimeMillis();
        
        // set current time and initialize actors.
        super.initialize();

        // register the stop time as a breakpoint.
        _debug("Set the stop time as a breakpoint: " + getModelStopTime());
        fireAt((Actor) getContainer(), getModelStopTime());
    }

    /** Return true if this is the discrete phase of execution.
     *  @return True if this is the discrete phase of execution.
     */
    public boolean isDiscretePhase() {
        // FIXME: this may not be necessary. We can check whether the 
        // integration step size is 0.        
        return _discretePhase;
    }

    /** If this director is not at the top level and the breakpoint table
     *  is not empty, request a refiring at the first breakpoint.
     *  Call the super.postfire() method and return its result.
     *  
     *  @return True if the Director wants to be fired again in the future.
     *  @exception IllegalActionException If refiring can not be granted.
     */
    public boolean postfire() throws IllegalActionException {
        // postfire all continuous actors to commit their states.
        // Note that event generators are postfired.
        _markStates();

        // If the current time is equal to the stop time, return false.
        // Check, however, to make sure that the breakpoints table
        // does not contain the current model time, which would mean
        // that more events may be generated at this time.
        if (getModelTime().equals(getModelStopTime())
                && !_breakpoints.contains(getModelTime())) {
            return false;
        }
        
        // If time exceeds the stop time, then either we failed
        // to execute at the stop time, or the return value of
        // false was ignored. Either condition is a bug.
        if (getModelTime().compareTo(getModelStopTime()) > 0) {
            throw new IllegalActionException(this,
                    "Current time exceeds the specified stopTime.");
        }

        // set the suggested step size for next integration.
        _setCurrentStepSize(suggestedStepSize());

        if (_isEmbedded() && (_breakpoints.size() > 0)) {
            Time time = (Time) _breakpoints.removeFirst();
            CompositeActor container = (CompositeActor) getContainer();
            container.getExecutiveDirector().fireAt(container, time);
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
        _iterationBeginTime = getModelTime();
    
        return prefireReturns;
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
        super.preinitialize();

        // FIXME: can we put the time objects into the 
        // _initializeLocalVariables method?
        
        // Initialize the local variables except the time objects.
        _initializeLocalVariables();
        
        // Time objects can only be initialized at the end of this method after
        // the time scale and time resolution are evaluated.
        // NOTE: Time resolution is provided by the preinitialize() method in
        // the super class (Director). So, this method must be called
        // after the super.preinitialize() is called.
        // NOTE: _timeBase is not initialized here but in the initialize()
        // method instead in order to provide more accurate real-time
        // information.
        _startTime = new Time(this, ((DoubleToken) startTime.getToken()).doubleValue());
        _stopTime = new Time(this, ((DoubleToken) stopTime.getToken()).doubleValue());
        _iterationBeginTime = _startTime;
        
        // Instantiate a new ODE solver, using the class name given
        // by ODESolver.
        String solverClassName = ((StringToken) ODESolver.getToken()).stringValue().trim();
        _ODESolver = _instantiateODESolver(_solverClasspath + solverClassName);
    }

    /** Return the refined step size, which is the minimum of the
     *  current step size and the suggested step size of all actors that
     *  implement ContinuousStepSizeControlActor. If these actors
     *  request a step size smaller than the time resolution, then
     *  the first time this happens this method returns the time resolution.
     *  If it happens again on the next call to this method, then this
     *  method throws an exception.
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it or the
     *  refined step size is less than the time resolution.
     */
    public double refinedStepSize() throws IllegalActionException {
        _debug("Refining the current step size WRT step size control actors:");
    
        double timeResolution = getTimeResolution();
        double refinedStep = _currentStepSize;
    
        // FIXME: may generate StepSizeControlActor set for more 
        // efficient execution.
        Schedule schedule = getScheduler().getSchedule();
        Iterator firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext() && !_stopRequested) {
            Actor actor = ((Firing) firingIterator.next()).getActor();
            if (actor instanceof ContinuousStepSizeControlActor) {
                refinedStep = Math.min(refinedStep, 
                        ((ContinuousStepSizeControlActor) actor).refinedStepSize());
            }
        }
    
        // If the requested step size is smaller than the time
        // resolution, then set the step size to the time resolution.
        // Set a flag indicating that we have done that so that if
        // the step size is still too large, we throw an exception.
        if (refinedStep < timeResolution) {
            if (!_triedTheMinimumStepSize) {
                // First time requested step size is less than time resolution
                _debug("The requested step size is less than the"
                        + " time resolution; try setting the step size"
                        + " to the time resolution.");
                refinedStep = timeResolution;
                _triedTheMinimumStepSize = true;
            } else {
                _debug("The requested step size is less than the"
                        + " time resolution, but we already tried setting"
                        + " it to the time resolution and that failed.");
                throw new IllegalActionException(this,
                        "The refined step size is less than the time "
                                + "resolution, at time " + getModelTime());
            }
        } else {
            _triedTheMinimumStepSize = false;
        }
        return refinedStep;
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

    /** Return an array of suggested ModalModel directors  to use
     *  with ContinuousDirector. The default director is HSFSMDirector, which
     *  is used in hybrid system. FSMDirector could also be used
     *  with ContinuousDirector in some simple cases.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        // This method does not call the method defined in the super class,
        // because this method provides complete new information.
        // Default is a HSFSMDirector, while FSMDirector is also in the array.
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.HSFSMDirector";
        defaultSuggestions[1] = "ptolemy.domains.fsm.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Set the suggested step size for next integration. The suggested step 
     *  size is the minimum of suggestions from all step size control actors,
     *  and it never exceeds 10 times of the current step size.
     *  If there are no step size control actors at all, then return
     *  5 times of the current step size. However, the suggested step size
     *  never exceeds the maximum step size.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    public double suggestedStepSize() throws IllegalActionException {
        double suggestedStep = _currentStepSize;
        if (suggestedStep == 0.0) {
            // The current step size is 0.0. Predict a positive value to let
            // time advance.
            suggestedStep = _initStepSize;
        } else {
            suggestedStep = 10.0 * _currentStepSize;
            // FIXME: may generate ContinuousStepSizeControlActor set for more 
            // efficient execution.
            Schedule schedule = getScheduler().getSchedule();
            Iterator firingIterator = schedule.firingIterator();
            while (firingIterator.hasNext() && !_stopRequested) {
                Actor actor = ((Firing) firingIterator.next()).getActor();
                if (actor instanceof ContinuousStepSizeControlActor) {
                    double suggestedStepSize = 
                        ((ContinuousStepSizeControlActor) actor)
                            .suggestedStepSize();
                        if (suggestedStep > suggestedStepSize) {
                            suggestedStep = suggestedStepSize;
                        }
                }
            }
    
            if (suggestedStep > _maxStepSize) {
                suggestedStep = _maxStepSize;
            }
        }
        return suggestedStep;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the ODE solver used to resolve states by the director.
     *  @return The ODE solver used to resolve states by the director.
     */
    protected final ContinuousODESolver _getODESolver() {
        // This method is final for performance reason.
        return _ODESolver;
    }

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

            maxStepSize = new Parameter(this, "maxStepSize");
            maxStepSize.setExpression("1.0");
            maxStepSize.setTypeEquals(BaseType.DOUBLE);

            maxIterations = new Parameter(this, "maxIterations");
            maxIterations.setExpression("20");
            maxIterations.setTypeEquals(BaseType.INT);

            errorTolerance = new Parameter(this, "errorTolerance");
            errorTolerance.setExpression("1e-4");
            errorTolerance.setTypeEquals(BaseType.DOUBLE);

            synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
            synchronizeToRealTime.setExpression("false");
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

            timeResolution.setVisibility(Settable.FULL);
            iterations.setVisibility(Settable.NONE);

            ODESolver = new Parameter(this, "ODESolver");
            ODESolver.setTypeEquals(BaseType.STRING);
            ODESolver.setExpression("ExplicitRK45Solver");
            ODESolver.addChoice(new StringToken("ExplicitRK23Solver")
                    .toString());
            ODESolver.addChoice(new StringToken("ExplicitRK45Solver")
                    .toString());
            /* FIXME: These solvers are currently not implemented in this package.
            ODESolver.addChoice(new StringToken("BackwardEulerSolver")
                    .toString());
            ODESolver.addChoice(new StringToken("ForwardEulerSolver")
                    .toString());
            */
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
    protected final ContinuousODESolver _instantiateODESolver(String className)
            throws IllegalActionException {
        
        // All solvers must be in the package given by _solverClasspath.
        if (!className.trim().startsWith(_solverClasspath)) {
            className = _solverClasspath + className;
        }

        _debug("instantiating solver..." + className);

        ContinuousODESolver newSolver;
        
        try {
            Class solver = Class.forName(className);
            newSolver = (ContinuousODESolver) solver.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, "ODESolver: " + className
                    + " is not found.");
        } catch (InstantiationException e) {
            throw new IllegalActionException(this, "ODESolver: " + className
                    + " instantiation failed." + e);
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(this, "ODESolver: " + className
                    + " is not accessible.");
        }

        newSolver._makeSolverOf(this);
        return newSolver;
    }

    /** Return true if debugging is turned on.
     *  This exposes whether debugging is happening to the package.
     *  @return True if debugging is turned on.
     */
    protected final boolean _isDebugging() {
        return _debugging;
    }

    /** The current states are marked as a known good checkpoint.
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
    protected void _markStates() throws IllegalActionException {
        // Mark the current state of the stateful actors.
        // FIXME: may generate StatefulActor set for more 
        // efficient execution.
        Schedule schedule = getScheduler().getSchedule();
        Iterator firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext() && !_stopRequested) {
            Actor actor = ((Firing) firingIterator.next()).getActor();
            if (actor instanceof ContinuousStatefulActor) {
                _debug("Saving states of " + actor);
                ((ContinuousStatefulActor) actor).markState();
            }
        }

        // Synchronize to real time if necessary.
        if (((BooleanToken) synchronizeToRealTime.getToken()).booleanValue()) {
            long realTime = System.currentTimeMillis() - _timeBase;
            long simulationTime = (long) ((getModelTime().subtract(
                    getModelStartTime()).getDoubleValue()) * 1000);
    
            _debug("real time " + realTime + " and simulation time "
                    + simulationTime);
    
            long timeDifference = simulationTime - realTime;
    
            if (timeDifference > 20) {
                try {
                    _debug("Sleep for " + timeDifference + "ms");
                    Thread.sleep(timeDifference - 20);
                } catch (Exception e) {
                    throw new IllegalActionException(this, "Sleep Interrupted"
                            + e.getMessage());
                }
            } else {
                _debug("Warning: " + getFullName(),
                        " cannot achieve real-time performance"
                        + " at simulation time " + getModelTime());
            }
        }
    }

    /** Expose the debug method to the package. */
    protected void _reportDebugMessage(String message) {
        _debug(message);
    }

    /** Set the current phase of execution as a discrete phase. The value
     *  set can be returned by the isDiscretePhase() method.
     *  @param discrete True if this is the discrete phase.
     */
    protected final void _setDiscretePhase(boolean discrete) {
        _discretePhase = discrete;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The real starting time in term of system millisecond counts.
     */
    protected long _timeBase;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize the local variables of this ContinuousDirector. This is called in
    // the preinitialize method.
    // NOTE: Time objects are not initialized here. They are initialized at
    // the end of the preinitialize method of this director.
    private void _initializeLocalVariables() throws IllegalActionException {
        _maxIterations = ((IntToken) maxIterations.getToken()).intValue();
        _maxStepSize = ((DoubleToken) maxStepSize.getToken()).doubleValue();
        _currentStepSize = _initStepSize;
    
        // A simulation always starts with a discrete phase execution.
        _discretePhase = true;
    
        // clear the existing breakpoint table or
        // create a breakpoint table if necessary
        _debug(getFullName(), " create/clear break point table.");
    
        if (_breakpoints != null) {
            _breakpoints.clear();
        } else {
            _breakpoints = new TotallyOrderedSet(new GeneralComparator());
        }
    }

    /** Return true if all step size control actors in the output
     *  schedule agree that the current step is accurate.
     *  @return True if all step size control actors agree with the current
     *  step size.
     */
    private boolean _isStepSizeAccurate() throws IllegalActionException {
        _debug("Check accuracy for output step size control actors:");
    
        // FIXME: During the initialize() method, the step size is 0.
        // No step size refinement is needed. What is a better solution?
        if (_currentStepSize == 0) {
            return true;
        }
    
        boolean accurate = true;
    
        // Get all the output step size control actors.
        Schedule schedule = getScheduler().getSchedule();
        Iterator firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext() && !_stopRequested) {
            Actor actor = ((Firing) firingIterator.next()).getActor();
            // Ask -ALL- the actors whether the current step size is accurate.
            // THIS IS VERY IMPORTANT!!!
            // NOTE: all actors are guranteed to be asked once even if some
            // actors already set the "accurate" variable to false.
            // The reason is that event generators do not check the step size
            // accuracy in their fire emthods and they need to check the existence
            // of events in the special isOutputAccurate() method.
            // FIXME: may generate StepSizeControlActor set for more 
            // efficient execution.
            if (actor instanceof ContinuousStepSizeControlActor) {
                boolean thisAccurate = 
                    ((ContinuousStepSizeControlActor) actor).isStepSizeAccurate();
                _debug("  Checking output step size control actor: "
                        + actor.getName() + ", which returns " + thisAccurate);
                accurate = accurate && thisAccurate;
            }
        }
    
        _debug("Overall output accuracy result: " + accurate);
    
        return accurate;
    }

    /** Modify the current step size so that current model time plus the
     *  step size does not exceed the time of the next breakpoint.
     *  NOTE: if the current time is a breakpoint, that breakpoint is
     *  removed. Otherwise, the breakpoint table is left unmodified.
     */
    private void _refineStepWRTBreakpoints() {
        if (!_breakpoints.isEmpty()) {
            Time point = ((Time) _breakpoints.first());
            _debug("The first breakpoint is at " + point);
            double maximumAllowedStepSize
                    = point.subtract(getModelTime()).getDoubleValue();
            if (maximumAllowedStepSize == 0.0) {
                _breakpoints.removeFirst();
            }
            if (_currentStepSize > maximumAllowedStepSize) {
                _currentStepSize = maximumAllowedStepSize;
            }
        }
    }

    /** Set the current step size. Only CT directors can call this method.
     *  Solvers and actors must not call this method.
     *  @param stepSize The step size to be set.
     *  @see #_currentStepSize
     */
    private void _setCurrentStepSize(double stepSize) {
        _debug("----- Setting the current step size to " + stepSize);
        _currentStepSize = stepSize;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** A table for breakpoints. */
    private TotallyOrderedSet _breakpoints;

    /** Simulation step sizes. */
    private double _currentStepSize;

    /** Indicate that this is the discrete phase. */
    private boolean _discretePhase;

    /** the error tolerance for state resolution */
    private double _errorTolerance;

    /** A cache of the value of initStepSize. */
    private double _initStepSize;

    /** The current time at the start of the current integration step. */
    private Time _iterationBeginTime;

    /** The maximum iterations for implicit ODE solver to resolve states. */
    private int _maxIterations;

    /** The maximum step size used for intergration. */
    private double _maxStepSize;

    /** The ODE solver, which is an instance of the class given by
     *  the <i>ODESolver</i> parameter.
     */
    private ContinuousODESolver _ODESolver = null;

    /** The package name for the solvers supported by this director. */
    private static String _solverClasspath = 
        "ptolemy.domains.continuous.kernel.solver.";
    
    /** The cached value of the startTime parameter. */
    private Time _startTime;

    /** The cached value of the stopTime parameter. */
    private Time _stopTime;

    /** The local flag variable indicating whether the we have tried
     *  the time resolution as the integration step size. */
    private boolean _triedTheMinimumStepSize = false;
}
