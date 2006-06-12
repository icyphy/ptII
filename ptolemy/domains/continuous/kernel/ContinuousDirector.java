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
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.GeneralComparator;
import ptolemy.actor.util.SuperdenseTime;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TotallyOrderedSet;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
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
 execution. This parameter has no effect if
 there is an enclosing ContinuousDirector (i.e., above
 in the hierarchy, separated possibly by FIXME: What are
 we going to call the intervening FSMs or Case actors?.
 
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
 The maximum number of iterations that an
 ODE solver can use to resolve the states of integrators.
 Implicit solvers, for example, iterate until they converge,
 and this parameter bounds the number of iterations.
 An example of an implicit solver is the BackwardsEuler solver.
 The default value is 20, and the type is int.
 FIXME: Currently, this package implements no implicit solvers.

 <LI> <i>ODESolver</i>:
 The class name of the ODE solver used for integration. 
 This is a string that defaults to "ExplicitRK45Solver".
 Solvers are all required to be in package
 "ptolemy.domains.continuous.kernel.solver".
 If there is another ContinuousDirector above this one
 in the hierarchy, separated possibly by MultiComposite,
 then the value of this parameter is ignored and the
 solver given by the other ContinuousDirector will be used.
    
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
        TimedDirector, ContinuousStatefulActor, ContinuousStepSizeControlActor {

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
     *  If a solver is changed during execution, the
     *  change does not take effect until the next execution
     *  of the model.
     *  If there is another ContinuousDirector above this one
     *  in the hierarchy, separated possibly by MultiComposite,
     *  then the value of this parameter is ignored and the
     *  solver given by the other ContinuousDirector will be used.
     */
    public StringParameter ODESolver;

    /** Starting time of the execution. The default value is 0.0,
     *  and the type is double. This parameter has no effect if
     *  there is an enclosing ContinuousDirector (i.e., above
     *  in the hierarchy, separated possibly by FIXME: What are
     *  we going to call the intervening FSMs or Case actors?.
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
        if (_debugging) {
            _debug("Updating ContinuousDirector parameter: " 
                    + attribute.getName());
        }
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

    /** Perform an integration step. This invokes prefire() and fire() of
     *  actors (possibly repeatedly) and advances the local view of
     *  time by one step. This normally involves three nested iterative procedures.
     *  The outer procedure invokes the possibly multiple steps of
     *  the solver (if it is a multistep solver), unless the step size
     *  is zero.  The middle one iterates until a suitable step size is
     *  found. The inner one, implemented by the superclass,
     *  iterates until a fixed point is found at each time point.
     *  <p>
     *  If there is an enclosing ContinuousDirector, however, then this
     *  method simply performs the current round of execution of the enclosing
     *  director, using the step size of the enclosing director.
     *  @throws IllegalActionException If an actor throws it.
     */
    public void fire() throws IllegalActionException {
        
        // If there is an enclosing director, then just execute its current round.
        ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
        if (enclosingDirector != null) {
            _currentStepSize = enclosingDirector._currentStepSize;
            _ODESolver._setRound(enclosingDirector._ODESolver._getRound());
            if (super.prefire()) {
                super.fire();
            }
            return;
        }
        
        if (_debugging) {
            _debug("Execute the system from iteration begin time "
                    + _iterationBeginTime
                    + " with step size "
                    + _currentStepSize
                    + ".");
        }

        // Iterate until we conclude that the step size is acceptable
        // or we exceed the maximum allowable number of iterations.
        while (!_stopRequested) {
            // Some solvers take multiple rounds to perform an integration step.
            // Tell the solver we are starting an integration step by resetting
            // the solver.
            _ODESolver._reset();

            // Iterate until the solver is done with the integration step
            // or the maximum number of iterations is reached.
            int iterations = 0;
            while (!_ODESolver._isStepFinished()
                    && iterations < _maxIterations
                    && !_stopRequested) {
                // Resolve the fixed point at the current time.
                // Note that prefire resets all receivers to unknown,
                // and fire() iterates to a fixed point where all signals
                // become known.
                // Although super.prefire() is called in the prefire() method,
                // super.prefire() is called again here, because it may take 
                // several iterations to complete an integration step. 
                if (super.prefire()) {
                    super.fire();
                }
                // If the step size is zero, then one iteration is sufficient.
                if (_currentStepSize == 0.0) break;
                
                // Advance the local view of time such that
                // the derivatives of the integrators 
                // can be calculated by firing all the actors.
                // Note that this doesn't change global model time.
                // It only changes the local view of time.
                double timeIncrement = _ODESolver._getRoundTimeIncrement();
                _ODESolver._setRound(_ODESolver._getRound() + 1);
                setModelTime(_iterationBeginTime.add(_currentStepSize * timeIncrement));
                
                // Increase the iteration count.
                iterations++;
            }
            // If the step size is accurate and we did not reach the
            // maximum number of iterations then we are done.
            // Otherwise, we have to try again with a smaller step size.
            // FIXME: handling _maxIterations here isn't right.
            // The step size won't change if we hit maxIterations,
            // but the integration will be redone anyway.
            if (isStepSizeAccurate() && iterations < _maxIterations) {
                // All actors agree with the current step size,
                // or we have reached the maximum allowed number of iterations.
                // The integration step is finished.
                break;
            } else {
                // If any step size control actor is unsatisfied with the 
                // current step size, refine the step size to a smaller one.
                _setCurrentStepSize(refinedStepSize());

                if (_debugging) {
                    _debug("Step was not accurate. Refine the step size to: " 
                            + _currentStepSize);
                }
                // Restore the saved state of the stateful actors, 
                // including the save starting time of this integration.
                rollBackToCommittedState();
            }
        }
    }

    /** Handle firing requests from the contained actors by registrating
     *  breakpoints. If the specified time is earlier than the current time, 
     *  throw an exception. Otherwise, insert the specified time into the 
     *  breakpoint table.
     *  @param actor The actor that requests the firing.
     *  @param time The requested firing time.
     *  @exception IllegalActionException If the time is earlier than
     *  the current time.
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        if (_debugging){
            _debug(actor.getName() + " requests refiring at " + time);
        }
        // Check if the request time is earlier than the current time.
        Time currentTime = getModelTime();
        int index = 1;

        int comparisonResult = time.compareTo(currentTime); 
        if (comparisonResult < 0) {
            throw new IllegalActionException(actor, 
                    "Requested time: " + time 
                    + " is earlier than the current time: " + currentTime);
        } else if (comparisonResult == 0) {
            index = _index + 1;
        }
        // Insert a superdense time object as a breakpoint into the 
        // breakpoint table.
        _breakpoints.insert(new SuperdenseTime(time, index));
        if (_debugging){
            _debug("Inserted breakpoint with time = " + time + ", and index = " + index);
        }
    }

    /** Return the current integration step size.
     *  @return The current integration step size.
     */
    public final double getCurrentStepSize() {
        // This method is final for performance reason.
        return _currentStepSize;
    }

    /** Return the local truncation error tolerance.
     *  @return The local truncation error tolerance.
     */
    public final double getErrorTolerance() {
        // This method is final for performance reason.
        return _errorTolerance;
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

    /** Return the end time of the current integration step.
     *  @return The next time at which controlled actors will be fired.
     */
    public Time getModelNextIterationTime() {
        return _iterationBeginTime.add(_currentStepSize);
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
        // set current time and initialize actors.
        super.initialize();

        // Make sure the first step has zero step size.
        // This ensures that actors like plotters will be postfired at the start time.
        _currentStepSize = 0.0;

        // If we are embedded, then request a firing at the start and
        // stop times. However, do not do this if there is an enclosing
        // ContinuousDirector.
        if (_isEmbedded() && (_enclosingContinuousDirector() == null)) {
            Actor container = (Actor)getContainer();
            Director director = container.getExecutiveDirector();
            director.fireAt(container, getModelStartTime());
            director.fireAt(container, getModelStopTime());
        }

        // Record starting point of the real time (the computer system time)
        // in case the director is synchronized to the real time.
        _timeBase = System.currentTimeMillis();

        _index = 0;
    }

    /** Return true if all step size control actors agree that the current 
     *  step is accurate.
     *  @return True if all step size control actors agree with the current
     *   step size.
     */
    public boolean isStepSizeAccurate() {
        _debug("Check accuracy for output step size control actors:");
    
        // A zero step size is always accurate.
        if (_currentStepSize == 0) return true;
    
        boolean accurate = true;
    
        // Ask the actors whether the current step size is accurate.
        Iterator stepSizeControlActors = _stepSizeControlActors().iterator();
        while (stepSizeControlActors.hasNext() && !_stopRequested) {
            ContinuousStepSizeControlActor actor
                    = (ContinuousStepSizeControlActor) stepSizeControlActors.next();
            boolean thisAccurate = actor.isStepSizeAccurate();
            if (_debugging) {
                _debug("  Checking output step size control actor: "
                        + ((NamedObj)actor).getFullName() 
                        + ", which returns " 
                        + thisAccurate);
            }
            accurate = accurate && thisAccurate;
        }
        if (_debugging) {
            _debug("Overall output is accurate: " + accurate);
        }
        return accurate;
    }

    /** If this director is not at the top level and the breakpoint table
     *  is not empty, request a refiring at the first breakpoint or at
     *  the local current time (iteration start time plus the step size),
     *  whichever is less.
     *  Postfire all controlled actors.
     *  @return True if the Director wants to be fired again in the future.
     *  @exception IllegalActionException If the current model time exceeds
     *   the stop time, or refiring can not be granted, or the super class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("ContinuousDirector: Called postfire().");
        }
        
        // If time exceeds the stop time, then either we failed
        // to execute at the stop time, or the return value of
        // false was ignored. Either condition is a bug.
        if (_currentTime.compareTo(getModelStopTime()) > 0) {
            throw new IllegalActionException(this,
                    "Current time exceeds the specified stopTime.");
        }
        
        // Postfire the contained actors.
        // This must be done before refining the step size and
        // before updating _index because the actors may call fireAt()
        // in their postfire() method, which will insert breakpoints,
        // which will affect the next step size.
        boolean result = super.postfire();

        // If the current time is equal to the stop time, return false.
        // Check, however, to make sure that the breakpoints table
        // does not contain the current model time, which would mean
        // that more events may be generated at this time.
        // If the index of the breakpoint is the same as the current
        // index, then we have executed it.  This must be called
        // after postfire() of the controlled actors is called, because
        // they may call fireAt(), which inserts events in the breakpoint
        // table.
        if (_currentTime.equals(getModelStopTime())) {
            SuperdenseTime nextBreakpoint = 
                    (SuperdenseTime) _breakpoints.first();
            if (nextBreakpoint == null
                    || nextBreakpoint.timestamp().compareTo(_currentTime) > 0) {
                return false;
            }
        }
        
        // If this director is enclosed within an opaque composite
        // actor, then request that the enclosing director refire
        // the composite actor containing this director.
        // However, do not make the request if there is an
        // enclosing ContinuousDirector. It will handle it.
        // Note that we do this even if the current step size is zero.
        if (_isEmbedded() && (_enclosingContinuousDirector() == null)) {
            CompositeActor container = (CompositeActor) getContainer();
            container.getExecutiveDirector().fireAt(container, _currentTime);
        }

        // Set the suggested step size for next integration step,
        // but only if we are not enclosed by another continuous director.
        // If we are, then the enclosing director will call suggestedStepSize()
        // and adjust the step size to the right value.
        if (_enclosingContinuousDirector() == null) {
            _setCurrentStepSize(suggestedStepSize());
        }

        // Update the index. Note that this must happen after
        // we refine the step size.
        if (_currentStepSize == 0.0) {
            _index++;
        } else {
            _index = 0;
        }
        
        // Set the start time of the current iteration.
        // The iterationBegintime will be used for roll back when the current
        // step size is incorrect.
        _iterationBeginTime = _currentTime;
        
        // Synchronize to real time if necessary.
        if (((BooleanToken) synchronizeToRealTime.getToken()).booleanValue()) {
            long realTime = System.currentTimeMillis() - _timeBase;
            long simulationTime = (long) ((_iterationBeginTime.subtract(
                    getModelStartTime()).getDoubleValue()) * 1000);
            long timeDifference = simulationTime - realTime;
            // If the time difference is large enough, go to sleep.
            if (timeDifference > 20) {
                try {
                    if (_debugging) {
                        _debug("Sleep for " + timeDifference + "ms.");
                    }
                    Thread.sleep(timeDifference - 20);
                } catch (Exception e) {
                    throw new IllegalActionException(this, "Sleep Interrupted"
                            + e.getMessage());
                }
            } else {
                if (_debugging) {
                    _debug("Warning: cannot achieve real-time performance"
                            + " at simulation time " + _iterationBeginTime);
                }
            }
        }

        return result;
    }

    /** Call the prefire() method of the super class and return its value.
     *  Record the current model time as the beginning time of the current
     *  iteration, and if there is a pending invocation of postfire()
     *  from a previous integration step, invoke that now.
     *  If the <i>synchronizeToRealTime</i> parameter is <i>true</i>,
     *  then this method will block execution until the real time catches
     *  up with current model time. The units for time are seconds.
     *  @return True if this director is ready to fire.
     *  @exception IllegalActionException If thrown by the super class,
     *   or if the model time of the environment is less than our current
     *   model time.
     */
    public boolean prefire() throws IllegalActionException {
        
        // If we are not at the toplevel, then check the model
        // time of the environment against ours.
        Nameable container = getContainer();
        
        // If this model is inside another that is not a Continuous model,
        // then check current time against the environment time, and catch
        // up to the environment time if necessary. Also, refine the step
        // size if necessary to match 
        if (_isEmbedded()) {
            ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
            if (enclosingDirector == null) {
                // Make sure the time does not exceed the next iteration
                // time of the environment.
                _refineStepWRTEnvironment();
                
                Director executiveDirector = ((Actor) container).getExecutiveDirector();
                Time outTime = executiveDirector.getModelTime();
                int comparison = _currentTime.compareTo(outTime);
                if (comparison > 0) {
                    throw new IllegalActionException(this,
                            "Model time in the environment is less than my model time. "
                            + "Environment: "
                            + outTime
                            + ", my model time: "
                            + _currentTime);
                } else if (comparison < 0 && executiveDirector != _enclosingContinuousDirector()) {
                    _catchUpTo(outTime);
                }
            } else {
                // When a ContinuousDirector is under the control of another
                // ContinuousDirector, then match the step size of the outside.
                _currentStepSize = enclosingDirector._currentStepSize;
            }
        }
        
        // If the current time and index matches the first entry in the breakpoint
        // table, then remove that entry.
        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints.first();
            Time breakpointTime = nextBreakpoint.timestamp();
            int comparison = breakpointTime.compareTo(_currentTime);
            if (comparison < 0) {
                throw new IllegalActionException(this,
                        "Missed a breakpoint time at "
                        + breakpointTime
                        + ", with index "
                        + nextBreakpoint.index());
            }
            if (comparison == 0) {
                if (nextBreakpoint.index() == _index) {
                    if (_debugging){
                        _debug("The current superdense time is a breakpoint, "
                                + nextBreakpoint + " , which is removed.");
                    }
                    _breakpoints.removeFirst();
                } else if (nextBreakpoint.index() < _index) {
                    throw new IllegalActionException(this,
                            "Missed a breakpoint time at "
                            + breakpointTime
                            + ", with index "
                            + nextBreakpoint.index());                    
                }
            }
        }

        // The super.prefire() method cannot be called before this point
        // because it sets the local model time to match that of the
        // executive director.
        return super.prefire();
    }

    /** Preinitialize the model for an execution. This method is
     *  called only once for each simulation. 
     *  <p>
     *  Note, however, time does not have a meaning when actors are
     *  preinitialized. So actors must not use a notion of time in their
     *  preinitialize() methods.
     *
     *  @exception IllegalActionException If the super class throws it, or 
     *  local variables cannot be initialized.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Time objects can only be initialized at the end of this method after
        // the time scale and time resolution are evaluated.
        // Time resolution is provided by the preinitialize() method in
        // the super class (Director). So, the following method must be called
        // after the super.preinitialize() is called.
        _initializeLocalVariables();
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
        if (_debugging) {
            _debug("Refining the current step size of "
                    + _currentStepSize
                    + " WRT step size control actors:");
        }
    
        double timeResolution = getTimeResolution();
        double refinedStep = _currentStepSize;
    
        Iterator stepSizeControlActors = _stepSizeControlActors().iterator();
        while (stepSizeControlActors.hasNext() && !_stopRequested) {
            ContinuousStepSizeControlActor actor
                    = (ContinuousStepSizeControlActor) stepSizeControlActors.next();
            refinedStep = Math.min(refinedStep, actor.refinedStepSize());
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
        if (_debugging) {
            _debug("Suggested step size: " + refinedStep);
        }
        return refinedStep;
    }
    
    /** Roll back all actors that implement ContinuousStatefulActor
     *  to committed state, and set local model time to the start
     *  of the integration period.
     */
    public void rollBackToCommittedState() {
        // Restore the local view of model time to
        // the start of the integration step.
        setModelTime(_iterationBeginTime);
        
        Iterator rollbacks = _statefulActors().iterator();
        while (rollbacks.hasNext()) {
            ContinuousStatefulActor actor = (ContinuousStatefulActor) rollbacks.next();
            actor.rollBackToCommittedState();
        }
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
        if (_debugging) {
            _debug("----- Setting current time to " + newTime);
        }
        _currentTime = newTime;
    }

    /** Return an array of suggested ModalModel directors to use
     *  with ContinuousDirector. The only available director is
     *  ModalDirector because we need a director that implements
     *  the strict actor semantics.
     *  @return An array of suggested directors to be used with ModalModel.
     */
    public String[] suggestedModalModelDirectors() {
        // This method does not call the method defined in the super class,
        // because this method provides complete new information.
        String[] defaultSuggestions = new String[1];
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.ModalDirector";
        return defaultSuggestions;
    }

    /** Return the suggested step size for next integration. The suggested step 
     *  size is the minimum of suggestions from all step size control actors
     *  and the time until the next breakpoint,
     *  and it never exceeds 10 times of the current step size.
     *  If there are no step size control actors at all, then return
     *  5 times of the current step size. However, the suggested step size
     *  never exceeds the maximum step size.  If this director has not been
     *  fired since initialize() or the previous call to suggestedStepSize(),
     *  then return the value of <i>maxStepSize</i>.
     *  @return The suggested step size for next integration.
     */
    public double suggestedStepSize() {
        double suggestedStep = _initStepSize;
        if (_currentStepSize != 0.0) {
            // Increase the current step size, then ask the step-size control
            // actors for their suggestions and choose the minimum.
            // We could use _maxStepSize here, but this if the current step
            // size is very small w.r.t. the maximum, then this will almost
            // certainly be too much of a change in step size. Hence, we
            // increase the step size more slowly.
            suggestedStep = 10.0 * _currentStepSize;
            
            Iterator stepSizeControlActors = _stepSizeControlActors().iterator();
            while (stepSizeControlActors.hasNext() && !_stopRequested) {
                ContinuousStepSizeControlActor actor
                        = (ContinuousStepSizeControlActor) stepSizeControlActors.next();
                double suggestedStepSize = actor.suggestedStepSize();
                if (suggestedStep > suggestedStepSize) {
                    suggestedStep = suggestedStepSize;
                }
            }
        }
        // The suggested step size should not exceed the maximum step size.
        if (suggestedStep > _maxStepSize) {
            suggestedStep = _maxStepSize;
        }
        // Make sure time does not pass the next breakpoint.
        // If it matches current time, this removes the first
        // breakpoint from the table.
        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints.first();
            if (_debugging){
                _debug("The first breakpoint is at " + nextBreakpoint);
            }
            Time breakpointTime = nextBreakpoint.timestamp();
            double result = breakpointTime.subtract(getModelTime()).getDoubleValue();
            if (result < suggestedStep) {
                suggestedStep = result;
                if (_debugging) {
                    _debug("----- Revising step size due to breakpoint to "
                            + suggestedStep);
                }
            }
        }
        // Next ensure the selected step size does not take us
        // past the stop time.
        // FIXME: This test could possibly be eliminated by
        // putting the stop time on the breakpoint table. Be sure,
        // however, that this results in the right number of
        // events generated at the stop time.
        Time targetTime = getModelTime().add(suggestedStep);
        if (targetTime.compareTo(getModelStopTime()) > 0) {
            suggestedStep = getModelStopTime()
            .subtract(getModelTime()).getDoubleValue();
            if (_debugging) {
                _debug("----- Revising step size due to stop time to "
                        + suggestedStep);
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

            ODESolver = new StringParameter(this, "ODESolver");
            ODESolver.setExpression("ExplicitRK45Solver");
            ODESolver.addChoice("ExplicitRK23Solver");
            ODESolver.addChoice("ExplicitRK45Solver");
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

        if (_debugging) {
            _debug("instantiating solver..." + className);
        }

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

    /** Expose the debug method to the package. */
    protected void _reportDebugMessage(String message) {
        _debug(message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The real starting time in term of system millisecond counts.
     */
    protected long _timeBase;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Perform integration up to the specified target time, which is
     *  assumed to be in the future relative to current time.
     *  For all steps earlier the targetTime, force all inputs to
     *  be absent before executing at those times.
     *  @throws IllegalActionException If an actor throws it.
     */
    private void _catchUpTo(Time targetTime) throws IllegalActionException {
        // FIXME: Not implemented yet.
        throw new IllegalActionException(this,
                "Catch up to future time is not implemented yet!");
    }
    
    /** Return a list of all the enclosed continuous directors.
     *  These are instances of this class that are either contained
     *  by opaque composite actors in the specified composite, or
     *  are contained by opaque composite actors that are refinements
     *  of a MultiComposite directly contained by the specified composite.
     *  @return A list of enclosed instances of ContinuousDirector.
     */
    private List _enclosedContinuousDirectors(CompositeEntity composite) {
        if (_enclosedContinuousDirectorsVersion != _workspace.getVersion()) {
            _enclosedContinuousDirectors.clear();
            Iterator composites = composite.entityList(CompositeActor.class).iterator();
            while (composites.hasNext()) {
                CompositeActor actor = (CompositeActor)composites.next();
                if (actor.isOpaque()) {
                    Director director = actor.getDirector();
                    if (director instanceof ContinuousDirector) {
                        _enclosedContinuousDirectors.add(director);
                    } else {
                        /* FIXME: Define a MultiComposite interface in the kernel
                        and have MultiCompositeActor and ModalModel implement it.
                        Then do:
                        else if (actor instanceof MultiComposite) {
                        // Iterate over the refinements and do:
                         _stepSizeControlActors.addAll(_enclosedContinuousDirectors(refinement));
                         } */
                    }
                } else {
                    _enclosedContinuousDirectors.addAll(_enclosedContinuousDirectors(actor));
                }
            }
            _enclosedContinuousDirectorsVersion = _workspace.getVersion();
        }
        return _enclosedContinuousDirectors;
    }

    /** Return the enclosing continuous director, or null if there
     *  is none.  The enclosing continous director is a director
     *  above this in the hierarchy, possibly separated by composite
     *  actors that implement the MultiComposite interface.
     *  @return The enclosing ContinuousDirector, or null if there is none.
     */
    private ContinuousDirector _enclosingContinuousDirector() {
        if (_enclosingContinuousDirectorVersion != _workspace.getVersion()) {
            // Update the cache.
            _enclosingContinuousDirector = null;
            NamedObj container = getContainer().getContainer();
            while (container != null) {
                if (container instanceof Actor) {
                    Director director = ((Actor)container).getDirector();
                    if (director instanceof ContinuousDirector) {
                        _enclosingContinuousDirector = (ContinuousDirector)director;
                    }
                    // FIXME: Handle intervening FSMs or Case.
                    break;
                }
                container = container.getContainer();
            }
            _enclosingContinuousDirectorVersion = _workspace.getVersion();
        }
        return _enclosingContinuousDirector;
    }
    
    /** Initialize the local variables of this ContinuousDirector. Create or
     *  clear the breakpoints table. Instantiate an ODE solver. 
     *  This method is called in the preinitialize method.
     */
    private void _initializeLocalVariables() throws IllegalActionException {
        _maxIterations = ((IntToken) maxIterations.getToken()).intValue();
        _maxStepSize = ((DoubleToken) maxStepSize.getToken()).doubleValue();
        _currentStepSize = _initStepSize;
    
        _startTime = new Time(this, ((DoubleToken) startTime.getToken()).doubleValue());
        _stopTime = new Time(this, ((DoubleToken) stopTime.getToken()).doubleValue());
        _iterationBeginTime = _startTime;
        
        // clear the existing breakpoint table or
        // create a breakpoint table if necessary
        if (_debugging) {
            _debug(getFullName(), " create/clear break point table.");
        }
        if (_breakpoints != null) {
            _breakpoints.clear();
        } else {
            _breakpoints = new TotallyOrderedSet(new GeneralComparator());
        }

        // Instantiate an ODE solver, using the class name given
        // by ODESolver.
        String solverClassName;
        // If there is an enclosing ContinuousDirector, then use its solver
        // specification instead of the local one.
        ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
        if (enclosingDirector != null) {
            solverClassName = ((StringToken) enclosingDirector.ODESolver.getToken()).stringValue().trim();
        } else {
            solverClassName = ((StringToken) ODESolver.getToken()).stringValue().trim();
        }
        _ODESolver = _instantiateODESolver(_solverClasspath + solverClassName);
    }
    
    /** If necesssary, modify the current step size so that
     *  current model time plus the step size does not exceed
     *  the time of the next iteration of the environment.
     *  If this director controls a top-level model, then there
     *  is no environment, so this method does nothing.
     */
    private void _refineStepWRTEnvironment() {
        if (_isEmbedded()) {
            CompositeActor container = (CompositeActor) getContainer();
            Time environmentTime = container.getExecutiveDirector().getModelNextIterationTime();
            Time localTargetTime = _iterationBeginTime.add(_currentStepSize);
            if (environmentTime.compareTo(localTargetTime) < 0) {
                _currentStepSize = environmentTime.subtract(_currentTime).getDoubleValue();
                if (_debugging) {
                    _debug("----- Revising step size due to environment to "
                            + _currentStepSize);
                }
            }
        }
    }

    /** Set the current step size. Only CT directors can call this method.
     *  Solvers and actors must not call this method.
     *  @param stepSize The step size to be set.
     *  @see #_currentStepSize
     */
    private void _setCurrentStepSize(double stepSize) {
        if (_debugging) {
            _debug("----- Setting the current step size to " + stepSize);
        }
        _currentStepSize = stepSize;
    }
    
    /** Return a list of stateful actors.
     *  @return A list of actors implementing ContinuousStatefulActor.
     */
    private List _statefulActors() {
        if (_workspace.getVersion() != _statefulActorsVersion) {
            // Construct the list.
            _statefulActors.clear();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof ContinuousStatefulActor) {
                    _statefulActors.add(actor);
                }
                _statefulActors.addAll(_enclosedContinuousDirectors(container));
            }
            _statefulActorsVersion = _workspace.getVersion();
        }
        return _statefulActors;
    }
    
    /** Return a list of step-size control actors.
     *  @return A list of step-size control actors.
     */
    private List _stepSizeControlActors() {
        if (_workspace.getVersion() != _stepSizeControlActorsVersion) {
            // Construct the list.
            _stepSizeControlActors.clear();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof ContinuousStepSizeControlActor) {
                    _stepSizeControlActors.add(actor);
                }
                _stepSizeControlActors.addAll(_enclosedContinuousDirectors(container));
            }
            _stepSizeControlActorsVersion = _workspace.getVersion();
        }
        return _stepSizeControlActors;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** A table for breakpoints. */
    private TotallyOrderedSet _breakpoints;

    /** Simulation step sizes. */
    private double _currentStepSize;
    
    /** A list of the enclosed continuous directors. */
    private List _enclosedContinuousDirectors = new LinkedList();
    
    /** The version for _enclosedContinuousDirectors. */
    private long _enclosedContinuousDirectorsVersion = -1;

    /** The enclosing continuous director. */
    private ContinuousDirector _enclosingContinuousDirector = null;
    
    /** The version for __enclosingContinuousDirector. */
    private long _enclosingContinuousDirectorVersion = -1;

    /** The error tolerance for state resolution. */
    private double _errorTolerance;
    
    /** A cache of the value of initStepSize. */
    private double _initStepSize;

    /** The current time at the start of the current integration step. */
    private Time _iterationBeginTime;

    /** The maximum iterations for implicit ODE solver to resolve states. */
    private int _maxIterations;

    /** The maximum step size used for integration. */
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
    
    /** The list of stateful actors. */
    private List _statefulActors = new LinkedList();
    
    /** The version for the list of step size control actors. */
    private long _statefulActorsVersion = -1;
    
    /** The list of step size control actors. */
    private List _stepSizeControlActors = new LinkedList();
    
    /** The version for the list of step size control actors. */
    private long _stepSizeControlActorsVersion = -1;

    /** The cached value of the stopTime parameter. */
    private Time _stopTime;

    /** The local flag variable indicating whether the we have tried
     *  the time resolution as the integration step size. */
    private boolean _triedTheMinimumStepSize = false;
}
