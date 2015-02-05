/* Continuous-time director.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import ptolemy.actor.IOPort;
import ptolemy.actor.QuasiTransparentDirector;
import ptolemy.actor.continuous.ContinuousStatefulComponent;
import ptolemy.actor.continuous.ContinuousStatefulDirector;
import ptolemy.actor.continuous.ContinuousStepSizeController;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.GeneralComparator;
import ptolemy.actor.util.SuperdenseTime;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TotallyOrderedSet;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ContinuousDirector

/**
 The continuous time domain is a timed domain that supports
 continuous-time signals, discrete-event signals, and mixtures of the
 two. There is a global notion of time that all the actors are aware of.
 The semantics of this domain is given in the following two papers:
 <ul>
 <li>
 Edward A. Lee and Haiyang Zheng, "<a href="http://ptolemy.eecs.berkeley.edu/publications/papers/05/OperationalSemantics/#in_browser">Operational Semantics of Hybrid Systems</a>,"
 Invited paper in Proceedings of Hybrid Systems: Computation and Control
 (HSCC) LNCS 3414, Zurich, Switzerland, March 9-11, 2005.</li>
 <li>
 Edward A. Lee, Haiyang Zheng, "<a href="http://ptolemy.eecs.berkeley.edu/publications/papers/07/unifying/index.htm#in_browser">Leveraging Synchronous Language Principles for Heterogeneous Modeling and Design of Embedded Systems</a>,"
 EMSOFT 07, September 30-October 3, 2007, Salzburg, Austria.</li>
 </ul>
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
 have a "stuttering Zeno" condition, and time will not progress.</p>
 <p>
 In our semantics, all signals are piecewise continuous.
 This means that the initial value, as a function of time,
 is continuous on the left, the final value, as a function
 of time, is continuous on the right, and the signal
 has exactly one value (meaning the initial value and the final value
 are the same) at all times except those
 on a discrete subset D.</p>
 <p>
 A purely continuous signal has exactly one value at
 all times, meaning that the final value equals the initial
 value at all times.
 A purely discrete signal has
 initial value "absent" and final value "absent" at all
 times, and at a discrete subset of the times, it may
 have non-absent values. The only signal that is both
 purely continuous and purely discrete is the one that
 is absent at all tags.</p>
 <p>
 A signal may be mostly continuous,
 but have multiple values at a discrete subset of times.
 These multiple values semantically represent discontinuities
 in a continuous signal that is not purely continuous.</p>
 <p>
 The set of times where signals have more than one distinct value
 is a discrete subset D of the time line. These times are called
 "breakpoints" and are treated specially in the execution.
 Between these times, an ordinary differential equation (ODE)
 solver governs the execution. Initial values are always given
 by the ODE solver.</p>
 <p>
 The parameters of this director are:
 <ul>
 <li> <i>startTime</i>: The start time of the
 execution.</li>

 <li> <i>stopTime</i>: The stop time of the execution.
 When the current time reaches this value, postfire() will return false.
 This will occur whether or not this director is at the top level.</li>

 <li> <i>initStepSize</i>: The suggested integration step size.
 If the ODE solver is a fixed step size solver, then this parameter
 gives the step size taken. Otherwise, at the start of execution,
 this provides the first guess for the integration step size.
 In later iterations, the integrators provide the suggested step
 size. This is a double with default value 0.1</li>

 <li> <i>maxStepSize</i>: The maximum step size.
 This can be used to prevent the solver from too few
 samples of signals. That is, for certain models, it might
 be possible to get accurate results for very large step
 sizes, but plots of the signals may be misleading (even
 if they are accurate) because they represent the signal
 with only a few samples. The default value is 1.0.</li>

 <li> <i>maxIterations</i>:
 The maximum number of iterations that an
 ODE solver can use to resolve the states of integrators.
 Implicit solvers, for example, iterate until they converge,
 and this parameter bounds the number of iterations.
 An example of an implicit solver is the BackwardsEuler solver.
 The default value is 20, and the type is int.
 FIXME: Currently, this package implements no implicit solvers.</li>

 <li> <i>ODESolver</i>:
 The class name of the ODE solver used for integration.
 This is a string that defaults to "ExplicitRK23Solver",
 a solver that tends to deliver smooth renditions of signals.
 The "ExplicitRK45Solver" may be more efficient in that it can
 use larger step sizes, but the resulting signals when displayed
 may be more jagged in appearance.
 Solvers are all required to be in package
 "ptolemy.domains.continuous.kernel.solver".
 If there is another ContinuousDirector above this one
 in the hierarchy, then the value of this parameter is ignored and the
 solver given by the first ContinuousDirector above will be used.</li>

 <li> <i>errorTolerance</i>: This is the local truncation
 error tolerance, used for controlling the integration accuracy
 in variable step size ODE solvers, and also for determining whether
 unpredictable breakpoints have been accurately identified. Any actor
 that implements ContinuousStepSizeControl may use this error
 tolerance to determine whether the current step is accurate.
 For example, if the local truncation error
 in some integrator is greater than this tolerance, then the
 integration step is considered to have failed, and should be restarted with
 a reduced step size. The default value is 1e-4.</li>
 </ul>
 <p>
 This director maintains a breakpoint table to record all predictable
 breakpoints that are greater than or equal to
 the current time. The breakpoints are sorted in chronological order.
 Breakpoints at the same time are considered to be identical, and the
 breakpoint table does not contain duplicate time points. A breakpoint can
 be inserted into the table by calling the fireAt() method. The fireAt method
 may be requested by the director, which inserts the stop time of the
 execution. The fireAt method may also be requested by actors and the
 requested firing time will be inserted into the breakpoint table.</p>
 <p>
 This director is designed to work with any other director that
 implements the strict actor semantics. As long as the other director does
 not commit state changes except in postfire(), that director
 can be used within the model controlled by
 this director. If, in addition to implementing the strict
 actor semantics that other director also respects calls to
 fireAt(), then this director may be used within a model
 governed by that director.</p>
 <p>
 This director is based on the CTDirector by Jie Liu and Haiyang Zheng,
 but it has a much simpler scheduler and a fixed-point semantics.</p>

 @author Haiyang Zheng and Edward A. Lee, based on CTDirector by Jie Liu and Haiyang Zheng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class ContinuousDirector extends FixedPointDirector implements
ContinuousStatefulDirector, ContinuousStepSizeController {

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
     *  This is a string that defaults to "ExplicitRK23Solver".
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the new parameter value
     *  is not valid.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) {
            _debug("attributeChanged: Updating ContinuousDirector parameter: "
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

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ContinuousDirector newObject = (ContinuousDirector) super
                .clone(workspace);
        newObject._breakpoints = null;
        newObject._enclosingContinuousDirectorVersion = -1L;
        newObject._ODESolver = null;
        newObject._statefulComponents = new LinkedList();
        newObject._statefulComponentsVersion = -1L;
        newObject._stepSizeControllers = new LinkedList();
        newObject._stepSizeControllersVersion = -1L;
        return newObject;
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
     *  @exception IllegalActionException If an actor throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling fire() at time " + getModelTime() + " index "
                    + _index);
        }
        // If there is an enclosing director, then just execute
        // its current round.
        ContinuousDirector enclosingContinuousDirector = _enclosingContinuousDirector();
        if (enclosingContinuousDirector != null) {
            // The local model time is not synchronized here because
            // the prefire method is responsible for it.
            _currentStepSize = enclosingContinuousDirector._currentStepSize;
            int round = enclosingContinuousDirector._ODESolver._getRound();
            _ODESolver._setRound(round);
            if (_debugging) {
                _debug("-- Get step size from enclosing Continuous director: "
                        + _currentStepSize + ", and also the solver round: "
                        + round + ".");
            }
            _resetAllReceivers();
            _transferInputsToInside();
            super.fire();
            _transferOutputsToEnvironment();
            return;
        }

        // If there is a commit pending, then this firing should just
        // produce the outputs from the speculative execution.
        // The commit will occur in postfire.
        if (_commitIsPending) {
            // We are presumably at superdense index 0 of the environment,
            // but have already executed superdense index 0 of the local model.
            // Hence, we should produce outputs now and increment our local
            // superdense index to 1.
            _transferOutputsToEnvironment();
            return;
        }

        _resetAllReceivers();

        // If there is an enclosing director, we know it is not a ContinuousDirector
        // because we would have returned above if it were. Inputs from any
        // enclosing domain that is not continuous are discrete, so we should
        // not read them if the current superdense index is 0. This would
        // violate the piecewise continuity constraint.
        if (!_redoingSolverIteration && _index == 0 && isEmbedded()) {
            // Check the input ports to see if any has an input. If
            // it does, set the current step size to zero and execute
            // one round without the input. The input will be read at
            // the next superdense time index.
            List<IOPort> ports = ((Actor) getContainer()).inputPortList();
            for (IOPort port : ports) {
                for (int i = 0; i < port.getWidth(); i++) {
                    if (port.isKnown(i) && port.hasToken(i)) {
                        // There is an input. Do a zero step-size iteration.
                        _currentStepSize = 0.0;
                        _ODESolver._reset();
                        // Before firing, we need to assert absent on all inside
                        // ports, since we aren't transferring the inputs.
                        _assertAbsentInside();
                        super.fire();
                        _transferOutputsToEnvironment();
                        return;
                    }
                }
            }
        }

        // If there are external inputs and there is no enclosing continuous
        // director, then those external inputs are to be understood as discrete
        // inputs. We must process them when we have a step size of zero.
        // However, if we just backtracked (due to our previous step size
        // having been too big, and the speculative execution having been
        // nullified by the appearance of an unexpected event), then we should
        // not call _transferInptusToInside(), because the environment time
        // is ahead of the local time and any inputs that might present
        // are in the future. Presumably, since we are redoing a speculative
        // solver iteration, there must have been no inputs at the
        // _iterationBeginTime, which is now our _currentTime.
        if (!_redoingSolverIteration && _transferInputsToInside()
                || _currentStepSize == 0.0) {
            _currentStepSize = 0.0;
            _ODESolver._reset();
            super.fire();
            _transferOutputsToEnvironment();
            _redoingSolverIteration = false;
            return;
        }

        // If we get to here, then we are either at the top level, or we
        // are enclosed within a director that is not a ContinuousDirector
        // and there are no external inputs and the step size is greater
        // than zero.  We now need to perform integration.
        boolean outputsProduced = false;
        while (!_stopRequested) {
            // Some solvers take multiple rounds to perform an integration step.
            // Tell the solver we are starting an integration step by resetting
            // the solver.
            _ODESolver._reset();

            if (_debugging) {
                _debug("-- Execute from iteration begin time "
                        + _iterationBeginTime + " with step size "
                        + _currentStepSize + " at index " + _index + ".");
            }

            // Iterate until the solver is done with the integration step
            // or the maximum number of iterations is reached.
            int iterations = 0;
            try {
                _isIntermediateStep = true;
                while (!_ODESolver._isStepFinished()
                        && iterations < _maxIterations && !_stopRequested) {

                    _resetAllReceivers();

                    // Set receivers connected to the inputs to "absent."
                    // Note that this cannot be done by calling
                    // _transferInputsToInside() because if we are redoing
                    // the solver iteration then there are inputs available,
                    // but they should not be consumed yet.
                    // FIXME: Shouldn't this be done only if enclosingContinousDirector is null?
                    if (enclosingContinuousDirector == null) {
                        _assertAbsentInside();
                    }

                    super.fire();
                    // Outputs should only be produced on the first iteration of
                    // the solver because after that we are computing values in
                    // the future. We should not produce more outputs if we
                    // reduce the step size and try again because that could
                    // result in multiple output events where there should only
                    // be one.
                    if (!_redoingSolverIteration && !outputsProduced) {
                        // Outputs need to be produced now, since the
                        // values at the output ports are now the correct
                        // values at the _iterationBeginTime, which on
                        // the first pass through, will match the
                        // current environment time. We do not do this
                        // if we are redoing the solver iteration, since
                        // presumably those outputs were already produced,
                        // and current time in the environment has passed
                        // the time that they were produced.
                        _transferOutputsToEnvironment();
                        outputsProduced = true;
                    }

                    // Advance the local view of time such that
                    // the inputs to the integrators
                    // can be calculated by firing all the actors.
                    // Note that this doesn't change global model time.
                    // It only changes the local view of time.
                    double timeIncrement = _ODESolver._getRoundTimeIncrement();
                    // This will return 1.0 when there is one or zero more
                    // steps for the solver. Hence, the next step will not
                    // be an intermediate step.
                    if (timeIncrement == 1.0) {
                        _isIntermediateStep = false;
                    }
                    localClock.setLocalTime(_iterationBeginTime
                            .add(_currentStepSize * timeIncrement));
                    _index = 0;
                    if (_debugging) {
                        _debug("-- Setting current time for the next ODE solver round: "
                                + getModelTime() + " and index to 0.");
                    }
                    _ODESolver._setRound(_ODESolver._getRound() + 1);

                    if (_debugging) {
                        _debug("ODE solver solves the round #"
                                + _ODESolver._getRound());
                    }
                    // Increase the iteration count.
                    iterations++;
                }
            } finally {
                _isIntermediateStep = false;
                _redoingSolverIteration = false;
            }
            // If the step size is accurate and we did not reach the
            // maximum number of iterations then we are done.
            // Otherwise, we have to try again with a smaller step size.
            if (isStepSizeAccurate() && iterations <= _maxIterations) {
                // All actors agree with the current step size,
                // or we have reached the maximum allowed number of iterations.
                // The integration step is finished.
                break;
            } else {
                if (iterations > _maxIterations) {
                    // If any step size control actor is unsatisfied with the
                    // current step size, refine the step size to a smaller one.
                    _setCurrentStepSize(_currentStepSize / 2);
                } else {
                    // There is some step size control actor that is
                    // unsatisfied with the current step size, refine the
                    // step size to a smaller one.
                    double refinedStep = refinedStepSize();
                    // Make sure the result is actually a change.
                    if (refinedStep >= _currentStepSize) {
                        // Actors suggestion is not useful.
                        // Choose instead half the current step size.
                        refinedStep = _currentStepSize * 0.5;
                        if (_debugging) {
                            _debug("-- Adjusting step size to: " + refinedStep);
                        }
                    }
                    _setCurrentStepSize(refinedStep);
                }

                if (_debugging) {
                    _debug("-- Step was not accurate. Refine the step size to: "
                            + _currentStepSize);
                }
                // Restore the saved state of the stateful actors,
                // including the save starting time of this integration.
                rollBackToCommittedState();
            }
        }
    }

    /** Handle firing requests from the contained actors by registering
     *  breakpoints. If the specified time is earlier than the current time,
     *  then request a firing at the current time.
     *  Otherwise, insert the specified time into the
     *  breakpoint table. If the specified time is earlier than or
     *  equal to the current time, then for the breakpoint table entry,
     *  use an index one larger than the current index, unless this director
     *  is currently in initialize(), in which case use the current index.
     *  If the requested time is in the future, then use the requested index.
     *  @param actor The actor that requests the firing.
     *  @param time The requested firing time.
     *  @param index The microstep.
     *  @return The time at which the actor passed as an argument
     *   will be fired.
     *  @exception IllegalActionException If the time is earlier than
     *  the current time.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int index)
            throws IllegalActionException {
        if (_debugging) {
            String name = "this director";
            if (actor != null) {
                name = actor.getName();
            }
            _debug("** fireAt() called by " + name
                    + ", which requests refiring at " + time);
        }
        synchronized (_breakpoints) {
            // Check if the request time is earlier than the current time.
            Time currentTime = getModelTime();
            int comparisonResult = time.compareTo(currentTime);
            // Adjust time to at least match the current time.
            if (comparisonResult < 0) {
                time = currentTime;
            }
            if (comparisonResult <= 0) {
                // If during initialization, do not increase the microstep.
                // This is based on the assumption that an actor only requests
                // one firing during initialization. In fact, if an actor requests
                // several firings at the same time,
                // only the first request will be granted.
                if (index <= _index) {
                    if (_isInitializing) {
                        index = _index;
                    } else {
                        index = _index + 1;
                    }
                }
            }
            // Insert a superdense time object as a breakpoint into the
            // breakpoint table.
            _breakpoints.insert(new SuperdenseTime(time, index));
            if (_debugging) {
                _debug("** Inserted breakpoint with time = " + time
                        + ", and index = " + index);
            }
            return time;
        }
    }

    /** Return the current integration step size.
     *  @return The current integration step size.
     */
    @Override
    public final double getCurrentStepSize() {
        // This method is final for performance reason.
        return _currentStepSize;
    }

    /** Return the local truncation error tolerance.
     *  @return The local truncation error tolerance.
     */
    @Override
    public final double getErrorTolerance() {
        // This method is final for performance reason.
        return _errorTolerance;
    }

    /** Initialize model after type resolution.
     *  If a start time has been explicitly set, then set the start
     *  time to that value. Otherwise, inherit if from the environment,
     *  if there is one, and set it to 0.0 otherwise.
     *  In addition to calling the initialize() method of the super class,
     *  this method records the current system time as the "real" starting
     *  time of the execution. This starting time is used when the
     *  execution is synchronized to real time.
     *
     *  @exception IllegalActionException If the super class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _isInitializing = true;

        // In case we are being reinitialized by a reset transition,
        // clear the breakpoint table. This must be done before
        // actors are initialized because they may call fireAt(),
        // which will insert items in the breakpoint table.
        _breakpoints.clear();

        // set current time and initialize actors.
        super.initialize();

        // The above method sets the microstep to match the enclosing
        // director if this director is embedded. In the Continuous
        // domain, however, this is not the right thing to do. We
        // might be getting initialized within a modal model, in
        // which case the enclosing director may have a larger
        // microstep. But we still want to begin with a microstep of zero.
        // FIXME: No idea whether this is right!
        // _index = 0;

        // Make sure the first step has zero step size.
        // This ensures that actors like plotters will be postfired at
        // the start time.
        _currentStepSize = 0.0;
        Time startTime = getModelStartTime();
        Time stopTime = getModelStopTime();
        Time currentTime = getModelTime();

        // If this director is embedded, then request a firing at the
        // start and stop times. However, do not do this if there is
        // an enclosing ContinuousDirector.

        // The reason for doing this is that if a Continuous composite actor
        // is embedded in a DE model but has no input ports, without the
        // following statements, the composite actor has no chance to be fired.
        // Also, we want to be sure to be fired at the stop time.
        if (isEmbedded() && _enclosingContinuousDirector() == null) {
            // In preinitialize(), _startTime was set to the start time of
            // the executive director. However, this invocation of initialize()
            // may be occurring later in the execution of a model, as a result
            // for example of a reset transition. In that case, _startTime
            // may be in the past relative to the environment time.
            // The super.initialize() call above should have set our
            // current time to match the environment time, so we use
            // the maximum of _startTime and the current time as the
            // first firing time.
            if (startTime.compareTo(currentTime) >= 0) {
                fireContainerAt(startTime, 0);
            } else {
                // Use a microstep of 1 here on the assumption
                // that initialization could create discontinuities.
                fireContainerAt(currentTime, 1);
            }
            if (!stopTime.isInfinite() && stopTime.compareTo(currentTime) >= 0) {
                fireContainerAt(stopTime);
            }
        }
        // Set a breakpoint with index 0 for the stop time.
        if (!stopTime.isInfinite() && stopTime.compareTo(currentTime) >= 0) {
            _breakpoints.insert(new SuperdenseTime(stopTime, 0));
        }

        _commitIsPending = false;
        _postfireReturns = true;
        _isInitializing = false;
        _redoingSolverIteration = false;

        // NOTE: If we are being reinitialized during execution (e.g. because
        // of a reset transition), then our current local time is set to match
        // the environment time if startTime is blank (the default), meaning that
        // start time is inherited from the environment. Otherwise, the accumulated
        // suspend time should be set to the difference between the current time
        // of the environment and the start time. In either case, this is the
        // difference between the current time and the environment time.
    }

    /** Return true if all step size control actors agree that the current
     *  step is accurate and if there are no breakpoints in the past.
     *  @return True if all step size control actors agree with the current
     *   step size.
     */
    @Override
    public boolean isStepSizeAccurate() {
        _debug("-- Check accuracy for output step size control actors:");

        // A zero step size is always accurate.
        if (_currentStepSize == 0) {
            return true;
        }

        boolean accurate = true;

        // Ask ALL the actors whether the current step size is accurate.
        // Note that ALL the step size controllers need to be queried.
        Iterator stepSizeControlActors = _stepSizeControllers().iterator();
        while (stepSizeControlActors.hasNext() && !_stopRequested) {
            ContinuousStepSizeController actor = (ContinuousStepSizeController) stepSizeControlActors
                    .next();
            boolean thisAccurate = actor.isStepSizeAccurate();
            if (_debugging) {
                _debug("---- Checking output step size control actor: "
                        + ((NamedObj) actor).getName() + ", which returns "
                        + thisAccurate);
            }
            accurate = accurate && thisAccurate;
        }

        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time breakpointTime = nextBreakpoint.timestamp();
            int comparison = breakpointTime.compareTo(getModelTime());
            if (comparison < 0) {
                accurate = false;
            }
        }

        if (_debugging) {
            _debug("-- Result of accuracy check: " + accurate);
        }
        return accurate;
    }

    /** If this director is not at the top level and the breakpoint table
     *  is not empty, request a refiring at the first breakpoint or at
     *  the local current time (iteration start time plus the step size),
     *  whichever is less. Postfire all controlled actors.
     *  <p>
     *  If the <i>synchronizeToRealTime</i> parameter is <i>true</i>,
     *  then this method will block execution until the real time catches
     *  up with current model time. The units for time are seconds.
     *  @return True if the Director wants to be fired again in the future.
     *  @exception IllegalActionException If the current model time exceeds
     *   the stop time, or refiring can not be granted, or the super class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling postfire().");
        }
        // If time exceeds the stop time, then either we failed
        // to execute at the stop time, or the return value of
        // false was ignored, or the return value of false in
        // prefire() was ignored. All of these conditions are bugs.
        if (getModelTime().compareTo(getModelStopTime()) > 0) {
            throw new IllegalActionException(this,
                    "Current time exceeds the specified stopTime.");
        }
        boolean result = _postfireReturns;
        // This code is sufficiently confusing that, at the expense
        // of code duplication, we completely separate three cases.
        if (_enclosingContinuousDirector() != null) {
            // Need to closely coordinate with the enclosing director
            result = result && _postfireWithEnclosingContinuousDirector();
        } else if (isEmbedded()) {
            result = result && _postfireWithEnclosingNonContinuousDirector();
        } else {
            // This director is at the top level.
            result = result && _postfireAtTopLevel();
        }
        return result;
    }

    /** Initialize the fixed-point iteration by setting all receivers to
     *  unknown, and return true if we have not passed the stop time.
     *
     *  Record the current model time as the beginning time of the current
     *  iteration, and if there is a pending invocation of postfire()
     *  from a previous integration step, invoke that now.
     *  @return True if this director is ready to fire.
     *  @exception IllegalActionException If thrown by the super class,
     *   or if the model time of the environment is less than our current
     *   model time.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("\nCalling prefire() at time " + getModelTime()
                    + " and index " + _index);
        }
        // This code is sufficiently confusing that, at the expense
        // of code duplication, we completely separate three cases.
        if (_enclosingContinuousDirector() != null) {
            // Need to closely coordinate with the enclosing director
            return _prefireWithEnclosingContinuousDirector();
        } else if (isEmbedded()) {
            return _prefireWithEnclosingNonContinuousDirector();
        } else {
            // This director is at the top level.
            return _prefireAtTopLevel();
        }
    }

    /** Preinitialize the model for an execution. This method is
     *  called only once for each simulation.
     *
     *  @exception IllegalActionException If the super class throws it, or
     *  local variables cannot be initialized.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Time objects can only be instantiated after super.preinitialize()
        // is called, where the time resolution is resolved.
        _initializeLocalVariables();
    }

    /** Return the refined step size, which is the minimum of the
     *  current step size and the suggested step size of all actors that
     *  implement ContinuousStepSizeController and that also ensures
     *  that we do not pass a breakpoint. If these actors
     *  request a step size smaller than the time resolution, then
     *  the first time this happens this method returns the time resolution.
     *  If it happens again on the next call to this method, then this
     *  method throws an exception.
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it or the
     *  refined step size is less than the time resolution.
     */
    @Override
    public double refinedStepSize() throws IllegalActionException {
        if (_debugging) {
            _debug("-- Refining the current step size from " + _currentStepSize);
        }

        double timeResolution = getTimeResolution();
        double refinedStep = _currentStepSize;

        Iterator stepSizeControlActors = _stepSizeControllers().iterator();
        while (stepSizeControlActors.hasNext() && !_stopRequested) {
            ContinuousStepSizeController actor = (ContinuousStepSizeController) stepSizeControlActors
                    .next();
            refinedStep = Math.min(refinedStep, actor.refinedStepSize());
        }

        // If the requested step size is smaller than the time
        // resolution, then set the step size to the time resolution.
        // Set a flag indicating that we have done that so that if
        // the step size as time resolution is still too large,
        // throw an exception.
        if (refinedStep < timeResolution) {
            if (!_triedTheMinimumStepSize) {
                // First time requested step size is less than time resolution
                refinedStep = timeResolution;
                _triedTheMinimumStepSize = true;
            } else {
                throw new IllegalActionException(this,
                        "The refined step size is less than the time "
                                + "resolution, at time " + getModelTime());
            }
        } else {
            _triedTheMinimumStepSize = false;
        }

        if (_debugging) {
            _debug("-- Refined step size suggested by the actors: "
                    + refinedStep);
        }

        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time nextBreakpointTime = nextBreakpoint.timestamp();
            int comparison = nextBreakpointTime.compareTo(_iterationBeginTime
                    .add(refinedStep));
            if (comparison < 0) {
                refinedStep = nextBreakpointTime.subtract(_iterationBeginTime)
                        .getDoubleValue();
                if (refinedStep < 0.0) {
                    throw new IllegalActionException(this,
                            "Cannot set a step size to respect the breakpoint at "
                                    + nextBreakpoint);
                }
                if (_debugging) {
                    _debug("-- Refined step size determined by the breakpoint table: "
                            + refinedStep);
                }
            }
        }

        return refinedStep;
    }

    /** Resume the actor at the specified time. If the actor has not
     *  been suspended since the last call to initialize(), then this
     *  has no effect.
     *  @exception IllegalActionException If the fireAt() request throws it.
     */
    @Override
    public void resume() throws IllegalActionException {
        super.resume();
        // Request a firing at the current time to ensure that
        // we restart here.
        fireAt(null, getModelTime());
    }

    /** Roll back all actors that implement ContinuousStatefulComponent
     *  to committed state, and set local model time to the start
     *  of the integration period.
     *  @exception IllegalActionException If the rollback attempts to go
     *   back further than the last committed time.
     */
    @Override
    public void rollBackToCommittedState() throws IllegalActionException {
        // Restore the local view of model time to
        // the start of the integration step.
        localClock.setLocalTime(_iterationBeginTime);
        _index = _iterationBeginIndex;

        if (_debugging) {
            _debug("-- Roll back time to: " + _iterationBeginTime
                    + " and index " + _index);
        }

        Iterator rollbacks = _statefulComponents().iterator();
        while (rollbacks.hasNext()) {
            ContinuousStatefulComponent actor = (ContinuousStatefulComponent) rollbacks
                    .next();
            actor.rollBackToCommittedState();
        }
    }

    /** Set a new value to the current time of the model. This overrides
     *  the base class to allow time to move backwards (to support rollback)
     *  and to discard any breakpoints in the breakpoint table that are
     *  in the past relative to the specified time.
     *  This overrides the setCurrentTime() in the Director base class.
     *  Although this method is public, actors should never call it.
     *  @param newTime The new current simulation time.
     *  @exception IllegalActionException If the time is in the past
     *   relative to the time of local committed state.
     */
    @Override
    public final void setModelTime(Time newTime) throws IllegalActionException {
        // We don't call super.getModelTime() because Director.setModelTime()
        // merely sets the local time of the localClock.  We want to do more here.

        if (_ignoreSetTime) {
            return;
        }
        // This method is final for performance reason.
        if (_debugging) {
            _debug("** Environment is setting current time to " + newTime);
        }
        Time currentTime = getModelTime();
        int comparison = newTime.compareTo(currentTime);
        if (comparison > 0) {
            // New time is ahead of the current local time.
            // This should not happen.
            throw new IllegalActionException(this,
                    "ContinuousDirector expected to be invoked at time "
                            + currentTime
                            + ", but instead its time is being set to "
                            + newTime);
            // NOTE: An alternative would be invalidate a pending commit
            // and discard intermediate breakpoints.
            // Code is below.
            /*
            if (_commitIsPending) {
                _commitIsPending = false;
                rollBackToCommittedState();
            }
            _currentTime = newTime;
            _iterationBeginTime = _currentTime;
            _handleBreakpointsBefore(_currentTime, 0);
            // Set the step size to 0.0, which will ensure that any
            // actors (like plotters) are postfired at the new model time.
            // This also reinitializes the adaptive step size calculation,
            // which is probably reasonable since things may have
            // changed significantly.
            _currentStepSize = 0.0;
             */
        } else if (comparison < 0) {
            // The new time is behind the current time.
            // This is legal only if we have a commit pending.
            if (!_commitIsPending) {
                throw new IllegalActionException(this,
                        "Attempting to roll back time from " + currentTime
                        + " to " + newTime
                        + ", but state has been committed.");
            }
            // We have to re-do the integration
            // with a smaller step size that brings us up to the current
            // environment time.
            // NOTE: This depends on the property that if an integration
            // step with a larger step size was successful and produced
            // no events, then an integration step with this now smaller
            // step size will also be successful and produce no events.
            _currentStepSize = newTime.subtract(_iterationBeginTime)
                    .getDoubleValue();
            if (_debugging) {
                _debug("**** Setting step size to: " + _currentStepSize);
            }
            // If the step size is now negative, then we are trying
            // to roll back too far.
            if (_currentStepSize < 0.0) {
                throw new IllegalActionException(this,
                        "Attempting to roll back time from "
                                + _iterationBeginTime + " to " + newTime
                                + ", but state has been committed.");
            }
            rollBackToCommittedState();
        }
    }

    /** Return an array of suggested ModalModel directors to use
     *  with ContinuousDirector. The only available director is
     *  ModalDirector because we need a director that implements
     *  the strict actor semantics.
     *  @return An array of suggested directors to be used with ModalModel.
     */
    @Override
    public String[] suggestedModalModelDirectors() {
        // This method does not call the method defined in the super class,
        // because this method provides complete new information.
        String[] defaultSuggestions = new String[1];
        defaultSuggestions[0] = "ptolemy.domains.continuous.kernel.HybridModalDirector";
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
     *  @exception IllegalActionException If an actor suggests an illegal step size.
     */
    @Override
    public double suggestedStepSize() throws IllegalActionException {
        double suggestedStep = _initStepSize;

        if (_currentStepSize != 0.0) {
            // Increase the current step size, then ask the step-size control
            // actors for their suggestions and choose the minimum.
            // We could use _maxStepSize here, but if the current step
            // size is very small w.r.t. the maximum, then this will almost
            // certainly be too much of a change in step size. Hence, we
            // increase the step size more slowly.
            suggestedStep = 10.0 * _currentStepSize;
            if (_debugging) {
                _debug("---- Speculatively set step size to " + suggestedStep);
            }

            Iterator stepSizeControlActors = _stepSizeControllers().iterator();
            while (stepSizeControlActors.hasNext() && !_stopRequested) {
                ContinuousStepSizeController actor = (ContinuousStepSizeController) stepSizeControlActors
                        .next();
                double suggestedStepSize = actor.suggestedStepSize();
                if (suggestedStepSize < 0.0) {
                    throw new IllegalActionException((Actor) actor,
                            "Actor requests invalid step size: "
                                    + suggestedStepSize);
                }
                if (_debugging) {
                    _debug("---- Step size control actor "
                            + ((NamedObj) actor).getName()
                            + " suggests next step size = " + suggestedStepSize);
                }
                if (suggestedStep > suggestedStepSize) {
                    if (_debugging) {
                        _debug("----- Revising step size to "
                                + suggestedStepSize);
                    }
                    suggestedStep = suggestedStepSize;
                }
            }
        }
        // The suggested step size should not exceed the maximum step size.
        if (suggestedStep > _maxStepSize) {
            suggestedStep = _maxStepSize;
        }

        // If there are breakpoints, set the suggested step size as the
        // time to the next breakpoint, if it is less than the value
        // so far.
        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time breakpointTime = nextBreakpoint.timestamp();
            double result = breakpointTime.subtract(getModelTime())
                    .getDoubleValue();
            if (result < 0.0) {
                throw new InternalErrorException("Missed a breakpoint at time "
                        + breakpointTime + ". Current time is "
                        + getModelTime());
            }
            if (result < suggestedStep) {
                suggestedStep = result;
                if (_debugging) {
                    _debug("----- The first breakpoint is at " + nextBreakpoint);
                    _debug("----- Revising suggested step size due to breakpoint to "
                            + suggestedStep);
                }
            }
        }

        // Next ensure the selected step size does not take us
        // past the stop time.
        // NOTE: This test could possibly be eliminated by
        // putting the stop time on the breakpoint table. Be sure,
        // however, that this results in the right number of
        // events generated at the stop time. This is very tricky,
        // and probably not worth the effort.
        Time targetTime = getModelTime().add(suggestedStep);
        Time stopTime = getModelStopTime();
        if (targetTime.compareTo(stopTime) > 0) {
            suggestedStep = stopTime.subtract(getModelTime()).getDoubleValue();
            if (_debugging) {
                _debug("----- Revising step size due to stop time to "
                        + suggestedStep);
            }
        }
        return suggestedStep;
    }

    /** Override the base class to do nothing. The fire() method of
     *  this director handles transferring inputs.
     *  @param port The port to transfer tokens from.
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        return false;
    }

    /** Override the base class to do nothing. The fire() method of
     *  this director handles transferring outputs.
     *  @param port The port to transfer tokens from.
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the current step size.
     *  @return The current step size.
     */
    protected double _getCurrentStepSize() {
        return _currentStepSize;
    }

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

            iterations.setVisibility(Settable.NONE);

            ODESolver = new StringParameter(this, "ODESolver");
            ODESolver.setExpression("ExplicitRK23Solver");
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

    /** Return true if the solver is at the first or intermediate steps
     *  of a multi-step solver. If there is an enclosing continuous
     *  director, this method delegates to that director. Otherwise,
     *  it checks the current step of the solver.
     *  @return True if either the solver is not doing a multi-step
     *   solution or it is at the last step of the multi-step solution.
     *  @exception IllegalActionException If the Time class throws it.
     */
    protected boolean _isIntermediateStep() throws IllegalActionException {
        ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
        if (enclosingDirector != null) {
            return enclosingDirector._isIntermediateStep();
        }
        return _isIntermediateStep;
    }

    /** Expose the debug method to the package.
     *  @param message The message that is to be reported.
     */
    protected void _reportDebugMessage(String message) {
        _debug(message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A local boolean variable indicating whether this director is in
     *  initialization phase execution.
     */
    protected boolean _isInitializing = false;

    /** The current time at the start of the current integration step.
     */
    protected Time _iterationBeginTime;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set all receivers on the inside of input ports to
     *  absent.
     *  @exception IllegalActionException If the send fails.
     */
    private void _assertAbsentInside() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        Iterator inports = container.inputPortList().iterator();
        while (inports.hasNext()) {
            IOPort port = (IOPort) inports.next();
            int insideWidth = port.getWidthInside();
            for (int i = 0; i < insideWidth; i++) {
                port.sendInside(i, null);
            }
        }
    }

    /** Commit the current state by postfiring the actors under the
     *  control of this director. Also, set the current time, index,
     *  and step size for the next iteration. If the current step size
     *  is 0.0, then this just increments the index. Otherwise, it sets
     *  the index to 0.
     *  @return True if it is OK to fire again.
     */
    private boolean _commit() throws IllegalActionException {
        Time currentTime = getModelTime();
        if (_debugging) {
            _debug("Committing the current states at " + currentTime);
        }

        // If current time matches a time on the breakpoint table
        // with the current index, then remove that breakpoint because we have
        // just completed execution of that iteration. Only do this, however,
        // if the current step size is 0.0, since breakpoints should be dealt
        // with always with step size 0.0
        if (_currentStepSize == 0.0 && !_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time breakpointTime = nextBreakpoint.timestamp();
            int comparison = breakpointTime.compareTo(currentTime);
            if (comparison == 0 && nextBreakpoint.index() <= _index) {
                if (_debugging) {
                    _debug("Removing breakpoint at " + nextBreakpoint);
                }
                _breakpoints.removeFirst();
            }
        }

        // Postfire the contained actors.
        // This must be done before refining the step size and
        // before updating _index because the actors may call fireAt()
        // in their postfire() method, which will insert breakpoints,
        // which will affect the next step size.
        // NOTE: This increments _index.
        // FIXME: This also resets all receivers, which is redundant since in this
        // director, resetting receivers is handled by fire(). Efficiency concern?
        _postfireReturns = super.postfire();

        // If the current time is equal to the stop time, return false.
        // Check, however, to make sure that the breakpoints table
        // does not contain the current model time, which would mean
        // that more events may be generated at this time.
        // If the index of the breakpoint is the same as the current
        // index, then we have executed it.  This must be called
        // after postfire() of the controlled actors is called, because
        // they may call fireAt(), which inserts events in the breakpoint
        // table.
        if (currentTime.equals(getModelStopTime())) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            if (nextBreakpoint == null
                    || nextBreakpoint.timestamp().compareTo(currentTime) > 0) {
                return false;
            }
        }

        // Update the index. Note that this must happen before
        // we refine the step size because the updated value gives
        // the step size for the _next_ iteration.
        if (_currentStepSize != 0.0) {
            _index = 0;
        }

        // Set the suggested step size for next integration step by polling
        // the contained actors and examining the breakpoint table.
        _setCurrentStepSize(suggestedStepSize());

        // Set the start time of the current iteration.
        // The iterationBegintime will be used for roll back when the current
        // step size is incorrect.
        _iterationBeginTime = currentTime;
        _iterationBeginIndex = _index;

        return _postfireReturns;
    }

    /** Return the enclosing continuous director, or null if there
     *  is none.  The enclosing continuous director is a director
     *  above this in the hierarchy, possibly separated by composite
     *  actors with actors that implement the QuasiTransparentDirector
     *  interface, such as FSMDirector or CaseDirector.
     *  @return The enclosing ContinuousDirector, or null if there is none.
     */
    private ContinuousDirector _enclosingContinuousDirector() {
        if (_enclosingContinuousDirectorVersion != _workspace.getVersion()) {
            // Update the cache.
            _enclosingContinuousDirector = null;
            NamedObj container = getContainer().getContainer();
            while (container != null) {
                if (container instanceof Actor) {
                    Director director = ((Actor) container).getDirector();
                    if (director instanceof ContinuousDirector) {
                        _enclosingContinuousDirector = (ContinuousDirector) director;
                        break;
                    }
                    if (!(director instanceof QuasiTransparentDirector)) {
                        break;
                    }
                }
                container = container.getContainer();
            }
            _enclosingContinuousDirectorVersion = _workspace.getVersion();
        }
        return _enclosingContinuousDirector;
    }

    /** Throw an exception if there are any breakpoints in the breakpoint
     *  table before the specified time.
     *  @param time The time.
     *  @param index The superdense time index.
     *  @exception IllegalActionException If there are any breakpoints in the
     *   breakpoint table earlier than the specified time.
     */
    private void _handleBreakpointsBefore(Time time, int index)
            throws IllegalActionException {
        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time breakpointTime = nextBreakpoint.timestamp();
            if (breakpointTime.compareTo(time) < 0) {
                throw new IllegalActionException(this,
                        "ContinuousDirector expected to be fired at time "
                                + breakpointTime
                                + " but instead is being fired at time " + time);
            }
        }
        // NOTE: An alternative would be to discard breakpoints earlier
        // than the specified time. The code for doing that is below.
        /*
        while (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time breakpointTime = nextBreakpoint.timestamp();
            int comparison = breakpointTime.compareTo(time);
            if (comparison > 0
                    || (comparison == 0 && nextBreakpoint.index() > index)) {
                // Next breakpoint is in the future.
                break;
            } else {
                if (_debugging) {
                    _debug(">>>> Discarding a breakpoint from the breakpoint table: "
                            + nextBreakpoint);
                }
                _breakpoints.removeFirst();
            }
        }
         */
    }

    /** Initialize the local variables of this ContinuousDirector. Create or
     *  clear the breakpoints table. Instantiate an ODE solver.
     *  This method is called in the preinitialize method.
     */
    private void _initializeLocalVariables() throws IllegalActionException {
        _maxIterations = ((IntToken) maxIterations.getToken()).intValue();
        _maxStepSize = ((DoubleToken) maxStepSize.getToken()).doubleValue();
        _currentStepSize = _initStepSize;

        _iterationBeginTime = getModelStartTime();
        _iterationBeginIndex = 0;

        // clear the existing breakpoint table or
        // create a breakpoint table if necessary
        if (_breakpoints != null) {
            if (_debugging) {
                _debug(getFullName(), "clears the breakpoint table.");
            }
            _breakpoints.clear();
        } else {
            if (_debugging) {
                _debug(getFullName(), "creates a breakpoint table.");
            }
            _breakpoints = new TotallyOrderedSet(new GeneralComparator());
        }

        // Instantiate an ODE solver, using the class name given
        // by ODESolver parameter, which is a string parameter.
        String solverClassName;
        // If there is an enclosing ContinuousDirector, then use its solver
        // specification instead of the local one.
        ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
        if (enclosingDirector != null) {
            solverClassName = enclosingDirector.ODESolver.stringValue().trim();
        } else {
            solverClassName = ODESolver.stringValue().trim();
        }
        _ODESolver = _instantiateODESolver(_solverClasspath + solverClassName);
    }

    /** Postfire method when this director is at the top level.
     *  @return True if it is OK to fire again.
     */
    private boolean _postfireAtTopLevel() throws IllegalActionException {
        // Commit the current state and postfire all actors.
        boolean result = _commit();
        // The above call will increment the index if the current step
        // size is zero, and set it to zero otherwise. At the top level,
        // we want to set it to 1, not zero because we just committed
        // the index 0 iteration for the current time. The next iteration
        // should run with index 1.
        if (_index == 0) {
            _index++;
        }
        return result;
    }

    /** Postfire method when the enclosing director
     *  is an instance of this same class.
     *  @return True if it is OK to fire again.
     */
    private boolean _postfireWithEnclosingContinuousDirector()
            throws IllegalActionException {
        boolean postfireResult = _commit();
        // The above call will increment the index if the current step
        // size is zero, and set it to zero otherwise. At the top level,
        // we want to set it to 1, not zero because we just committed
        // the index 0 iteration for the current time. The next iteration
        // should run with index 1.
        if (_index == 0) {
            _index++;
        }

        // Request a refiring at the current time if the
        // next step size has been set to 0.0, and at the
        // next breakpoint time otherwise.
        if (_currentStepSize == 0.0) {
            // We assume the enclosing director will
            // post this firing request at the next microstep.
            fireContainerAt(getModelTime());
        } else if (_breakpoints.size() > 0) {
            // Request a firing at the time of the first breakpoint.
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            fireContainerAt(nextBreakpoint.timestamp(), nextBreakpoint.index());
        }

        return postfireResult;
    }

    /** Postfire method when the enclosing director
     *  is not an instance of this same class.
     *  @return True if it is OK to fire again.
     */
    private boolean _postfireWithEnclosingNonContinuousDirector()
            throws IllegalActionException {
        Time currentTime = getModelTime();
        // If a commit is pending, just do the commit and request a refiring
        // at the current time.
        if (_commitIsPending) {
            _commitIsPending = false;
            fireContainerAt(currentTime, 0);

            // Commit the current state and postfire all actors.
            boolean result = _commit();
            // The above call will increment the index if the current step
            // size is zero, and set it to zero otherwise. At the top level,
            // we want to set it to 1, not zero because we just committed
            // the index 0 iteration for the current time. The next iteration
            // should run with index 1.
            if (_index == 0) {
                _index++;
            }
            return result;
        }
        Director enclosingDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        int currentTimeAheadOfOutsideTime = currentTime
                .compareTo(enclosingDirector.getModelTime());
        if (currentTimeAheadOfOutsideTime > 0) {
            // We have to defer the commit until current time of the environment
            // matches our local current time. Call fireAt() to ensure that the
            // enclosing director invokes prefire at the local current time.
            // This local current time should not exceed the least time on
            // the breakpoint table.
            // The following will throw an exception if the enclosing director
            // does not respect the fireAt() request exactly.
            fireContainerAt(currentTime, 0);
            // When that firing occurs, we want the index to be 0.
            _index = 0;
            _commitIsPending = true;
            if (_debugging) {
                _debug("postfire() requests refiring at time " + currentTime
                        + " and defers the commit.");
            }
            return true;
        } else {
            // Current time is less than or equal to the environment time.
            // NOTE: It is, in theory, impossible for local current time to
            // be less than the environment time because the prefire() method
            // would have thrown an exception. Hence, current time must match
            // the environment time.

            // Request a refiring at the current time.
            // The reason for this is that local time has not advanced,
            // so we can't be sure of any interval of future time over which
            // we will not produce an event. Only when the step size is
            // greater than zero, as we have speculatively executed into
            // the future, can we allow the enclosing director to advance time.
            // The following will throw an exception if the enclosing director
            // does not respect the fireAt() request exactly.
            // However, it could be that the current iteration is actually
            // a deferred commit, in which case local time has advanced
            // and we don't need to request a refiring at the current time.
            fireContainerAt(currentTime, 0);

            // The following call will increment the index if the current step
            // size is zero, and set it to zero otherwise. At the top level,
            return _commit();
        }
    }

    /** Prefire method when this director is at the top level.
     *  @return True if it is OK to fire.
     */
    private boolean _prefireAtTopLevel() throws IllegalActionException {
        Time currentTime = getModelTime();
        // If the current time and index matches the first entry in the breakpoint
        // table, then remove that entry.
        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time breakpointTime = nextBreakpoint.timestamp();
            int comparison = breakpointTime.compareTo(currentTime);
            // Note that we don't compare the superdense time of the breakpoint
            // because it is not important. It was created, probably, with
            // fireAt(), which does not take a superdense time index.
            if (comparison < 0) {
                // At the top level, we should not have missed a breakpoint.
                throw new IllegalActionException(this,
                        "Missed a breakpoint time at " + breakpointTime
                        + ", with index " + nextBreakpoint.index());
            } else if (comparison == 0 && nextBreakpoint.index() <= _index) {
                if (_debugging) {
                    _debug("-- The current superdense time is a breakpoint, "
                            + nextBreakpoint + ", which is removed.");
                }
                _breakpoints.removeFirst();
            }
        }
        boolean result = super.prefire();
        // Initialize everything for the fixedpoint iteration.
        // NOTE: This is done in fire. Not necessary here.
        // _resetAllReceivers();

        if (_debugging) {
            _debug("ContinuousDirector: prefire() returns " + result);
        }
        return result;
    }

    /** Prefire method when the enclosing director
     *  is an instance of this same class.
     *  @return True if it is OK to fire.
     */
    private boolean _prefireWithEnclosingContinuousDirector()
            throws IllegalActionException {
        // Call the super.prefire() method, which will synchronize time to the outside time.
        // That time may get modified below to align not with the immediate outside time,
        // but rather with the time of the enclosing continuous director, modified as
        // needed by accumulated suspend time. As a consequence, we set a flag to
        // ignore the time that is set.
        boolean result = true;
        Time currentTime = getModelTime();
        try {
            _ignoreSetTime = true;
            result = super.prefire();
        } finally {
            _ignoreSetTime = false;
        }

        // Set the time and step size to match that of the enclosing director.
        ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
        _currentStepSize = enclosingDirector._currentStepSize;
        // Do not use setCommittedTime on the following line because we
        // are probably speculatively executing into the future.
        localClock.setLocalTime(localClock
                .getLocalTimeForCurrentEnvironmentTime());

        if (_debugging) {
            _debug("-- Setting current time to " + currentTime
                    + ", which aligns with the enclosing director's time of "
                    + enclosingDirector.getModelTime());
        }

        _iterationBeginTime = localClock
                .getLocalTimeForEnvironmentTime(enclosingDirector._iterationBeginTime);

        // FIXME: Probably shouldn't make the index match that of the environment!
        // There may have been suspensions happening. So what should the index be?
        // Here we set it to zero if the round is greater than zero. But what
        // about other conditions?
        // _index = enclosingDirector._index;
        int round = enclosingDirector._ODESolver._getRound();
        if (round > 0) {
            _index = 0;
        } else if (_currentStepSize == 0.0) {
            /* FIXME: Bogus. This will force the first execution to be at microstep 1.
            _index++;
             */
        }

        _iterationBeginIndex = enclosingDirector._iterationBeginIndex;

        // If we have passed the stop time, then return false.
        // This can occur if we are inside a modal model and were not
        // active when the stop time elapsed.
        if (_iterationBeginTime.compareTo(getModelStopTime()) > 0) {
            if (_debugging) {
                _debug("-- prefire() returns false because stop time is exceeded at "
                        + _iterationBeginTime);
            }
            return false;
        }

        // Handle breakpoint entries earlier
        // than or equal to the iteration begin time.
        _handleBreakpointsBefore(_iterationBeginTime, _index);

        // Initialize everything for the fixedpoint iteration.
        // NOTE: This is done in fire(), and anyway, should not be done
        // if there is commit pending.
        // _resetAllReceivers();

        if (_debugging) {
            _debug("-- prefire() returns " + result);
        }
        return result;
    }

    /** Prefire method when the enclosing director
     *  is not an instance of this same class.
     *  @return True if it is OK to fire.
     */
    private boolean _prefireWithEnclosingNonContinuousDirector()
            throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        Director executiveDirector = ((Actor) container).getExecutiveDirector();
        Time currentTime = getModelTime();

        // Check the enclosing model time against the local model time.
        // Note that time has already been automatically adjusted with the
        // accumulated suspend time.
        Time outTime = localClock.getLocalTimeForCurrentEnvironmentTime();

        int localTimeExceedsOutsideTime = currentTime.compareTo(outTime);

        Time modifiedTime = _consultTimeRegulators(currentTime);
        int modifiedTimeExceedsLocalTime = modifiedTime.compareTo(currentTime);

        // Rollback has to occur if either the local time exceeds the modified time
        // or a time regulator requires a smaller current time than the current time.
        if (localTimeExceedsOutsideTime > 0 || modifiedTimeExceedsLocalTime < 0) {
            ///////////////////////////////////////////////////////////////
            // First case: Local current time exceeds that of the environment.
            if (!_commitIsPending) {
                throw new IllegalActionException(this, "The model time of "
                        + container.getFullName()
                        + " is greater than the environment time. "
                        + "Environment: " + outTime
                        + ", the model time (iteration begin time): "
                        + currentTime);
            }
            if (modifiedTimeExceedsLocalTime < 0
                    && modifiedTime.compareTo(outTime) < 0) {
                throw new IllegalActionException(
                        this,
                        "A TimeRegulator requires time to be set back to "
                                + modifiedTime
                                + ", which is less than the last commit time of "
                                + outTime);
            }
            // If we get here, local time exceeds the environment time
            // and we have speculatively executed past that local time.
            // But now, we cannot commit that execution because an unexpected
            // event has occurred.  Instead, we have to re-do the integration
            // with a smaller step size that brings us up to the current
            // environment time.
            // NOTE: This depends on the property that if an integration
            // step with a larger step size was successful and produced
            // no events, then an integration step with this now smaller
            // step size will also be successful and produce no events.
            // FIXME: This property is not true in general. E.g., we
            // might have missed a level crossing with a larger step
            // size that we won't miss with the smaller one! What
            // to do about this?
            _currentStepSize = outTime.subtract(_iterationBeginTime)
                    .getDoubleValue();
            // If the step size is now negative, then we are trying
            // to roll back too far.
            if (_currentStepSize < 0.0) {
                throw new IllegalActionException(this,
                        "Attempting to roll back time from "
                                + _iterationBeginTime + " to " + outTime
                                + ", but state has been committed.");
            }
            rollBackToCommittedState();
            _commitIsPending = false;
            // Set a flag to prevent the fire() method from reading inputs
            // and resetting the step size to 0.0 if there are inputs
            // present. Presumably, there are no inputs present at the
            // _iterationBeginTime because had there been any, then
            // the speculative execution that we are rolling back
            // would not have occurred. However, it could be a new input
            // has occurred at the _iterationBeginTime, but with a
            // larger microstep. In this case, there could be new inputs.
            if (_currentStepSize != 0.0) {
                _redoingSolverIteration = true;
            }
        } else if (localTimeExceedsOutsideTime == 0 && _commitIsPending) {
            ///////////////////////////////////////////////////////////////
            // Second case:
            // A commit is pending and the environment time matches
            // our local time. This means that no additional events have
            // arrived during the interval of the last successful integration
            // step. This microstep will simply do the commit that is pending,
            // producing any outputs previously computed in the speculative
            // execution. It will then request a refiring at the current time.
            // FIXME: Why set the current step size to zero?
            _currentStepSize = 0.0;
            // If the enclosing director implements SuperdenseTimeDirector,
            // then we used to get the current index from it.
            // However, we can't really trust the outside director to have
            // incremented the index, and it would incorrect to execute
            // without increment it. So we use our own index.
            /*
            if (executiveDirector instanceof SuperdenseTimeDirector) {
                _index = ((SuperdenseTimeDirector) executiveDirector).getIndex();
            }
             */
            return true;
        } else if (localTimeExceedsOutsideTime < 0) {
            ///////////////////////////////////////////////////////////////
            // Third case:
            // Local current time is behind environment time.
            // This should not happen. This used to happen if
            // we are inside a modal model and have been
            // disabled at the time that the commit
            // would be effective. We should cancel any pending commit.
            if (_commitIsPending) {
                _commitIsPending = false;
                rollBackToCommittedState();
            }
            // We should set current time to the environment time and set
            // the step size to zero.
            // FIXME: This says it is setting current time, but unless
            // a commit is pending, it is not. Should we just throw an exception here?
            if (_debugging) {
                _debug("-- Setting current time to match enclosing non-ContinuousDirector: "
                        + currentTime + ", and step size to 0.0.");
            }
            _currentStepSize = 0.0;
        } else {
            ///////////////////////////////////////////////////////////////
            // Fourth case:
            // Environment time matches local time and there is no pending
            // commit.
            // Adjust the step size to
            // make sure the time does not exceed the next iteration
            // time of the environment during this next integration step.
            Time environmentNextIterationTime = localClock
                    .getLocalTimeForEnvironmentTime(executiveDirector
                            .getModelNextIterationTime());

            Time localTargetTime = _iterationBeginTime.add(_currentStepSize);
            if (environmentNextIterationTime.compareTo(localTargetTime) < 0) {
                _currentStepSize = environmentNextIterationTime.subtract(
                        currentTime).getDoubleValue();
                if (_debugging) {
                    _debug("-- Revising step size due to environment's next iteration time to "
                            + _currentStepSize);
                }
            }
            // If the enclosing director implements SuperdenseTimeDirector,
            // then we used to get the current index from it.
            // But we can't trust the enclosing director to do the right
            // thing with the index. It becomes too fragile.
            /*
            if (executiveDirector instanceof SuperdenseTimeDirector) {
                _index = ((SuperdenseTimeDirector) executiveDirector).getIndex();
            }
             */
        }

        // If the current time and index match the first entry in the breakpoint
        // table, then remove that entry. An index is deemed to match if
        // the breakpoint index is less than or equal to the current time index.
        if (!_breakpoints.isEmpty()) {
            SuperdenseTime nextBreakpoint = (SuperdenseTime) _breakpoints
                    .first();
            Time breakpointTime = nextBreakpoint.timestamp();
            localTimeExceedsOutsideTime = breakpointTime.compareTo(currentTime);
            if (localTimeExceedsOutsideTime < 0) {
                throw new IllegalActionException(this,
                        "ContinuousDirector expected to be fired at time "
                                + breakpointTime
                                + " but instead is being fired at time "
                                + currentTime);
            }

            // NOTE: An alternative would be
            // to remove any breakpoints from the table that are now in the past.
            // In theory, this should not happen. The code for doing that is below.
            /*
            while (localTimeExceedsOutsideTime < 0
                    || (localTimeExceedsOutsideTime == 0 && nextBreakpoint
                            .index() < _index)) {
                if (_debugging) {
                    _debug("Remove a breakpoint that we missed: "
                            + breakpointTime);
                }
                _breakpoints.removeFirst();
                if (_breakpoints.isEmpty()) {
                    break;
                }
                nextBreakpoint = (SuperdenseTime) _breakpoints.first();
                breakpointTime = nextBreakpoint.timestamp();
                localTimeExceedsOutsideTime = breakpointTime
                        .compareTo(_currentTime);
            }
             */
            if (localTimeExceedsOutsideTime == 0
                    && nextBreakpoint.index() <= _index) {
                if (_debugging) {
                    _debug("-- The current superdense time is a breakpoint, "
                            + nextBreakpoint + ", which is removed.");
                }
                _breakpoints.removeFirst();
            }
        }

        // Do not call super.prefire() because it sets the local mode
        // time to match that of the executive director, defeating the
        // above logic. Therefore, we have duplicate what it does here.
        _synchronizeToRealTime();
        _postfireReturns = true;

        // Initialize everything for the fixedpoint iteration.
        // NOTE: This is handled by fire(), and anyway, should
        // not be done if there is a commit pending.
        // _resetAllReceivers();

        if (_debugging) {
            _debug("Called prefire(), which returns true.");
        }
        return true;
    }

    /** Set the current step size.
     *  @param stepSize The step size to be set.
     *  @see #_currentStepSize
     */
    private void _setCurrentStepSize(double stepSize) {
        if (_debugging) {
            _debug("----- Setting the current step size to " + stepSize);
        }
        _currentStepSize = stepSize;
    }

    /** Return a list of stateful components, which includes actors
     *  deeply contained within the container of this director that
     *  implement the ContinuousStatefulComponent interface and
     *  directors of opaque composite actors within the container
     *  of this director that implement the same interface.
     *  @return A list of objects implementing ContinuousStatefulComponent.
     */
    private List _statefulComponents() {
        if (_workspace.getVersion() != _statefulComponentsVersion) {
            // Construct the list.
            _statefulComponents.clear();
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof ContinuousStatefulComponent) {
                    _statefulComponents.add(actor);
                } else if (actor instanceof CompositeActor
                        && ((CompositeEntity) actor).isOpaque()
                        && !((CompositeEntity) actor).isAtomic()) {
                    Director director = ((Actor) actor).getDirector();
                    if (director instanceof ContinuousStatefulComponent) {
                        _statefulComponents.add(director);
                    }
                }
            }
            _statefulComponentsVersion = _workspace.getVersion();
        }
        return _statefulComponents;
    }

    /** Return a list of step-size controllers.
     *  @return A list of step-size control actors.
     */
    private List _stepSizeControllers() {
        if (_workspace.getVersion() != _stepSizeControllersVersion) {
            // Construct the list.
            _stepSizeControllers.clear();
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof ContinuousStepSizeController) {
                    _stepSizeControllers.add(actor);
                } else if (actor instanceof CompositeActor
                        && ((CompositeEntity) actor).isOpaque()
                        && !((CompositeEntity) actor).isAtomic()) {
                    Director director = ((Actor) actor).getDirector();
                    if (director instanceof ContinuousStepSizeController) {
                        _stepSizeControllers.add(director);
                    }
                }
            }
            _stepSizeControllersVersion = _workspace.getVersion();
        }
        return _stepSizeControllers;
    }

    /** Transfer inputs from the environment to inside.
     *  @exception IllegalActionException If the transferInputs(Port)
     *   method throws it.
     *  @return True if at least one token is transferred.
     */
    private boolean _transferInputsToInside() throws IllegalActionException {
        // If there are no input ports, this method does nothing.
        if (_debugging) {
            _debug("** Transfer inputs to the inside.");
        }
        boolean result = false;
        CompositeActor container = (CompositeActor) getContainer();
        Iterator inports = container.inputPortList().iterator();
        while (inports.hasNext() && !_stopRequested) {
            IOPort port = (IOPort) inports.next();
            result = super.transferInputs(port) || result;
        }
        return result;
    }

    /** Transfer outputs to the environment.
     *  @exception IllegalActionException If the transferOutputs(Port)
     *   method throws it.
     */
    private void _transferOutputsToEnvironment() throws IllegalActionException {
        if (_debugging) {
            _debug("** Transfer outputs to the environment.");
        }

        // If there are no output ports, this method does nothing.
        CompositeActor container = (CompositeActor) getContainer();
        Iterator outports = container.outputPortList().iterator();
        while (outports.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) outports.next();
            super.transferOutputs(p);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A table for breakpoints. */
    private TotallyOrderedSet _breakpoints;

    /** Flag indicating that postfire() did not commit the state at the
     *  local current time.
     */
    private boolean _commitIsPending = false;

    /** Simulation step sizes. */
    private double _currentStepSize;

    /** The enclosing continuous director. */
    private ContinuousDirector _enclosingContinuousDirector = null;

    /** The version for __enclosingContinuousDirector. */
    private long _enclosingContinuousDirectorVersion = -1;

    /** The error tolerance for state resolution. */
    private double _errorTolerance;

    /** Flag to temporarily ignore the setModelTime() calls. */
    private boolean _ignoreSetTime = false;

    /** A cache of the value of initStepSize. */
    private double _initStepSize;

    /** Flag indicating that the solver is iterating through the first or
     *  intermediate steps in a multi-step solver.
     */
    private boolean _isIntermediateStep = false;

    /** The index of the time at which the current integration step began. */
    private int _iterationBeginIndex;

    /** The maximum iterations for implicit ODE solver to resolve states. */
    private int _maxIterations;

    /** The maximum step size used for integration. */
    private double _maxStepSize;

    /** The ODE solver, which is an instance of the class given by
     *  the <i>ODESolver</i> parameter.
     */
    private ContinuousODESolver _ODESolver = null;

    /** Flag indicating that we are redoing a speculative solver iteration. */
    private boolean _redoingSolverIteration = false;

    /** The package name for the solvers supported by this director. */
    private static String _solverClasspath = "ptolemy.domains.continuous.kernel.solver.";

    /** The list of stateful actors. */
    private List _statefulComponents = new LinkedList();

    /** The version for the list of step size control actors. */
    private long _statefulComponentsVersion = -1;

    /** The list of step size control actors. */
    private List _stepSizeControllers = new LinkedList();

    /** The version for the list of step size control actors. */
    private long _stepSizeControllersVersion = -1;

    /** The local flag variable indicating whether the we have tried
     *  the time resolution as the integration step size. */
    private boolean _triedTheMinimumStepSize = false;
}
