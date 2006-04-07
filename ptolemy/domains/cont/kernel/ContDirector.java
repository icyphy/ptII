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
package ptolemy.domains.cont.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
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
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// ContDirector

/**
 The continuous time (cont) domain is a timed domain that supports
 continuous-time signals, discrete-event signals, and hybrids of the
 two. There is a global notion of time that all the actors are aware of.
 There is an ordinary differential equation solver that governs the
 execution.
 <P>
 The parameters are: <Br>
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
 <p>
 This director is based on the CTDirector by Jie Liu and Haiyang Zheng,
 but it has a much simpler scheduler.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class ContDirector extends FixedPointDirector implements
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
    public ContDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
        setScheduler(new ContScheduler(this, "scheduler"));
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

    /**
     * The class name of the normal ODE solver used in iterations 
     * for integration. 
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
        _debug("Updating ContDirector parameter: ", attribute.getName());

        if (attribute == startTime) {
            double startTimeValue = ((DoubleToken) startTime.getToken())
                    .doubleValue();
            _startTimeValue = startTimeValue;
        } else if (attribute == stopTime) {
            double stopTimeValue = ((DoubleToken) stopTime.getToken())
                    .doubleValue();
            _stopTimeValue = stopTimeValue;
        } else if (attribute == initStepSize) {
            double value = ((DoubleToken) initStepSize.getToken())
                    .doubleValue();

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
        } else if (attribute == ODESolver) {
            if (_debugging) {
                _debug(getFullName() + " updating ODE solver...");
            }

            String newSolverClassName = ((StringToken) ODESolver.getToken())
                    .stringValue().trim();

            if (newSolverClassName.trim().startsWith(_solverClasspath)) {
                // The old solver name is a parameter starts with
                // "ptolemy.domains.cont.kernel.solver."
                _ODESolverClassName = newSolverClassName;
            } else {
                _ODESolverClassName = _solverClasspath + newSolverClassName;
            }

            _ODESolver = _instantiateODESolver(_ODESolverClassName);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the fire() method of the super class. This method is
     *  abstract in this abstract base class. The derived classes need to
     *  override this method for concrete implementation.
     */
    public void fire() throws IllegalActionException {
        
        ////////////////////////////////////////////////////////////
        // Find an appropriate step size, which may be refined later.
        ////////////////////////////////////////////////////////////

        // Choose a suggested step size, which is a guess.
        setCurrentStepSize(getSuggestedNextStepSize());
        
        // Refine the correct step size for the continuous phase execution
        // with respect to the breakpoint table.
        setCurrentStepSize(_refinedStepWRTBreakpoints());
        
        _debug("execute the system from " + getModelTime()
                + " with a step size " + getCurrentStepSize());

        // Resolve the initial states at a future time
        // (the current time plus the current step size).
        while (!_stopRequested) {

            ////////////////////////////////////////////////////////////
            // Resolve states with the chosen step size.
            ////////////////////////////////////////////////////////////

            // Reset the round counts and the convergencies to false.
            // NOTE: some solvers have their convergencies depending on
            // the round counts. For example, it takes 3 rounds for a
            // RK-23 solver to solve states.
            // FIXME: the following statement may not be necessary since
            // the ODE solvers reset the round count in their fire() methods.
            _ODESolver._resetRoundCount();
            _ODESolver._setConverged(false);

            // repeating resolving states until states converge, or the
            // maximum iterations for finding states have been reached.
            // FIXME: this design does not support hierarchical execution yet.
            while (!_ODESolver._isConverged() && _ODESolver.resolvedStates()) {
                _ODESolver.fire();
//                _ODESolver._incrementRoundCount();
                _resetAllReceivers();
                super.prefire();
                super.fire();
            }
            
            ////////////////////////////////////////////////////////////
            // Refine the step size if necessary.
            ////////////////////////////////////////////////////////////

            if (_isStepSizeAccurate()) {
                // all actors agree with the current step size
                break;
            } else {
                // If any step size control actor is unsatisfied with the 
                // current step size, refine the step size to a smaller one.
                setCurrentStepSize(_refinedStepSize());
                _debug("Refine the current step size"
                        + " with a smaller one " + getCurrentStepSize());
                
                // Restore the saved state of the stateful actors, 
                // including the save starting time of this integration.
                setModelTime(getIterationBeginTime());
                
                // FIXME: may generate StatefulActor set for more 
                // efficient execution.
                Schedule schedule = getScheduler().getSchedule();
                Iterator firingIterator = schedule.firingIterator();
                while (firingIterator.hasNext() && !_stopRequested) {
                    Actor actor = ((Firing) firingIterator.next()).getActor();
                    if (actor instanceof ContStatefulActor) {
                        _debug("Restoring states of " + actor);
                        ((ContStatefulActor) actor).goToMarkedState();
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

    /** Return the ODE solver used to resolve states by the director.
     *  @return The ODE solver used to resolve states by the director.
     */
    public final ContODESolver getODESolver() {
        // This method is final for performance reason.
        return _ODESolver;
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

        // Now, the current time is equal to the stop time.
        // If the breakpoints table does not contain the current model time,
        // which means no events are and will be generated,
        // the execution stops by returning false in this postfire method.
        if (getModelTime().equals(getModelStopTime())
                && !_breakpoints.contains(getModelTime())) {
            return false;
        }

        // predict the next step size.
        setSuggestedNextStepSize(_predictNextStepSize());

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
        _setIterationBeginTime(getModelTime());
    
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
        _startTime = new Time(this, _startTimeValue);
        _stopTime = new Time(this, _stopTimeValue);
        _iterationBeginTime = _startTime;
        _iterationEndTime = _stopTime;
        
        // Instantiate an ODE solver.
        _ODESolver = _instantiateODESolver(_ODESolverClassName);
    }

    /** Set the current step size. Only CT directors can call this method.
     *  Solvers and actors must not call this method.
     *  @param stepSize The step size to be set.
     *  @see #getCurrentStepSize
     */
    public void setCurrentStepSize(double stepSize) {
        _debug("----- Setting the current step size to " + stepSize);
        _currentStepSize = stepSize;
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
     *  with ContDirector. The default director is HSFSMDirector, which
     *  is used in hybrid system. FSMDirector could also be used
     *  with ContDirector in some simple cases.
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
            iterations.setVisibility(Settable.NONE);

            _ODESolverClassName = 
                "ptolemy.domains.cont.kernel.solver.ExplicitRK45Solver";
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
    protected final ContODESolver _instantiateODESolver(String className)
            throws IllegalActionException {
        _debug("instantiating solver..." + className);

        ContODESolver newSolver;
        
        try {
            Class solver = Class.forName(className);
            newSolver = (ContODESolver) solver.newInstance();
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
            if (actor instanceof ContStatefulActor) {
                _debug("Saving states of " + actor);
                ((ContStatefulActor) actor).markState();
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
    
            // FIXME: may generate StatefulActor set for more 
            // efficient execution.
            Schedule schedule = getScheduler().getSchedule();
            Iterator firingIterator = schedule.firingIterator();
            while (firingIterator.hasNext() && !_stopRequested) {
                Actor actor = ((Firing) firingIterator.next()).getActor();
                if (actor instanceof ContStepSizeControlActor) {
                    _debug("Saving states of " + actor);
                    double suggestedStepSize = 
                        ((ContStepSizeControlActor) actor).predictedStepSize();
                        if (predictedStep > suggestedStepSize) {
                            predictedStep = suggestedStepSize;
                        }
                }
            }
    
            if (predictedStep > getMaxStepSize()) {
                predictedStep = getMaxStepSize();
            }
        }
    
        return predictedStep;
    }

    /** Set the ODE solver to be the given ODE solver.
     *  @param solver The solver to set.
     *  @exception  IllegalActionException Not thrown in this base class.
     */
    protected final void _setODESolver(ContODESolver solver)
            throws IllegalActionException {
        _ODESolver = solver;
    }

    /** Set the current phase of execution as a discrete phase. The value
     *  set can be returned by the isDiscretePhase() method.
     *  @param discrete True if this is the discrete phase.
     */
    protected final void _setDiscretePhase(boolean discrete) {
        _discretePhase = discrete;
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
        // This check may be redundant, but it is put here anyway.
        if (time.compareTo(getModelTime()) < 0) {
            throw new InvalidStateException(this, " Iteration end time" + time
                    + " is earlier than" + " the current time."
                    + getModelTime());
        }
        _iterationEndTime = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The real starting time in term of system millisecond counts.
     */
    protected long _timeBase;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize the local variables of this ContDirector. This is called in
    // the preinitialize method.
    // NOTE: Time objects are not initialized here. They are initialized at
    // the end of the preinitialize method of this director.
    private void _initializeLocalVariables() throws IllegalActionException {
        _errorTolerance = ((DoubleToken) errorTolerance.getToken())
                .doubleValue();
        _initStepSize = ((DoubleToken) initStepSize.getToken()).doubleValue();
        _maxIterations = ((IntToken) maxIterations.getToken()).intValue();
        _maxStepSize = ((DoubleToken) maxStepSize.getToken()).doubleValue();
        _minStepSize = ((DoubleToken) minStepSize.getToken()).doubleValue();
        _valueResolution = ((DoubleToken) valueResolution.getToken())
                .doubleValue();
    
        _ODESolver = null;
        _currentStepSize = _initStepSize;
        _suggestedNextStepSize = _initStepSize;
    
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
        if (getCurrentStepSize() == 0) {
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
            if (actor instanceof ContStepSizeControlActor) {
                boolean thisAccurate = 
                    ((ContStepSizeControlActor) actor).isStepSizeAccurate();
                _debug("  Checking output step size control actor: "
                        + actor.getName() + ", which returns " + thisAccurate);
                accurate = accurate && thisAccurate;
            }
        }
    
        _debug("Overall output accuracy result: " + accurate);
    
        return accurate;
    }

    // Return the refined step size with respect to the breakpoints.
    // If the current time plus the current step size exceeds the
    // time of the next breakpoint, reduce the step size such that the next
    // breakpoint is the end time of the current iteration.
    // NOTE: if the current time is a breakpoint, that breakpoint is
    // removed. Otherwise, the breakpoint table is left unmodified.
    private double _refinedStepWRTBreakpoints() {
        double currentStepSize = getCurrentStepSize();
        if (!_breakpoints.isEmpty()) {
            Time point = ((Time) _breakpoints.first());
            _debug("The first breakpoint is at " + point);
            double maximumAllowedStepSize = 
                point.subtract(getModelTime()).getDoubleValue();
            if (maximumAllowedStepSize == 0.0) {
                _breakpoints.removeFirst();
            }
            if (currentStepSize > maximumAllowedStepSize) {
                currentStepSize = maximumAllowedStepSize;
            }
        }
        _setIterationEndTime(getModelTime().add(currentStepSize));
        return currentStepSize;
    }

    /** Return the refined step size with respect to the output actors.
     *  All the step size control actors in the output schedule are queried for
     *  a refined step size. The smallest one is returned.
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it or the
     *  refined step size is less than the time resolution.
     */
    private double _refinedStepSize() throws IllegalActionException {
        _debug("Refining the current step size WRT step size control actors:");
    
        double timeResolution = getTimeResolution();
        double refinedStep = getCurrentStepSize();
    
        // FIXME: may generate StepSizeControlActor set for more 
        // efficient execution.
        Schedule schedule = getScheduler().getSchedule();
        Iterator firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext() && !_stopRequested) {
            Actor actor = ((Firing) firingIterator.next()).getActor();
            if (actor instanceof ContStepSizeControlActor) {
                refinedStep = Math.min(refinedStep, 
                        ((ContStepSizeControlActor) actor).refinedStepSize());
            }
        }
    
        if (refinedStep < (0.5 * timeResolution)) {
            if (_triedTheMinimumStepSize) {
                _debug("The previous step size is the time"
                        + " resolution. The refined step size is less than"
                        + " the time resolution. We can not refine the step"
                        + " size more.");
                throw new IllegalActionException(this,
                        "The refined step size is less than the minimum time "
                                + "resolution, at time " + getModelTime());
            } else {
                _debug("The previous step size is bigger than the time"
                        + " resolution. The refined step size is less than"
                        + " the time resolution, try setting the step size"
                        + " to the time resolution.");
                refinedStep = timeResolution;
                _triedTheMinimumStepSize = true;
            }
        } else {
            _triedTheMinimumStepSize = false;
        }
    
        _setIterationEndTime(getModelTime().add(refinedStep));
        return refinedStep;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // A table for breakpoints.
    private TotallyOrderedSet _breakpoints;

    // NOTE: all the following private variables are initialized
    // in the _initializeLocalVariables() method before their usage.
    // Current ODE solver.
    private ContODESolver _ODESolver = null;

    // Simulation step sizes.
    private double _currentStepSize;

    // Indicate that this is the discrete phase.
    private boolean _discretePhase;

    // the error tolerance for state resolution
    private double _errorTolerance;

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

    private String _ODESolverClassName;
    
    private static String _solverClasspath = 
        "ptolemy.domains.cont.kernel.solver.";
    
    // Local copies of parameters.
    private Time _startTime;

    private double _startTimeValue;

    private Time _stopTime;

    private double _stopTimeValue;

    private double _suggestedNextStepSize;

    private boolean _triedTheMinimumStepSize = false;

    private double _valueResolution;
}
