/* An abstract base class for directors in the CT domain.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
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
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Utilities;

//////////////////////////////////////////////////////////////////////////
//// CTDirector
/**
   This is the abstract base class for directors in the CT domain.
   <P>
   A CTDirector has a CTScheduler which provides the schedules for firing
   the actors in different phases of execution.
   <P>
   A CTDirector may have more than one ODE solvers. In each iteration, one
   of the ODE solvers is taking charge of solving the ODEs. This solver
   is called the <I>current ODE solver</I>.
   <P>
   The continuous time (CT) domain is a timed domain. There is a global
   notion of time that all the actors are aware of. Time is maintained
   by the director. The method getCurrentTime() returns the current
   notion of time. Time can be set by the setCurrentTime() method, but this
   method should not the called by the actors. Time can only be set
   by directors or their ODE solvers.
   <P>
   This base class maintains a list of parameters that may be used by
   ODE solvers and actors. These parameters are: <Br>
   <LI> <code>startTime</code>: The start time of the
   simulation. This parameter is effective only if the director
   is at the top level. Default value is 0.0.
   <LI> <code>stopTime</code>: The stop time of the simulation.
   This parameter is effective only if the director
   is at the top level. The default value is Infinity, which
   results in execution that does not stop on its own.
   <LI> <code>initStepSize</code>: The suggested integration step size
   by the user. This will be the step size for fixed step
   size ODE solvers if there is no breakpoint. However, it is just
   a hint otherwise. Default value is 0.1
   <LI> <code>minStepSize</code>: The minimum step
   size that the user wants to use in the simulation. Default value is 1e-5.
   <LI> <code>maxStepSize</code>: The maximum step
   size the user wants to use in the simulation. Usually used to control
   the simulation speed. Default value is 1.0.
   <LI> <code>maxIterations</code>:
   Used only in implicit ODE solvers. This is the maximum number of
   iterations for finding the fixed point at one time point.
   Default value is 20.
   <LI> <code>errorTolerance</code>: This is the local truncation
   error tolerance, used for controlling the integration accuracy
   in variable step size ODE solvers. If the local truncation error
   at some step size control actors are greater than this tolerance, then the
   integration step is considered to have failed, and should be restarted with
   a reduced step size. Default value 1e-4.
   <LI> <code>valueResolution</code>:
   This is used to control the convergence of fixed point iterations.
   If in two successive iterations the differences of the state variables
   is less than this resolution, then the fixed point is considered to have
   reached.
   Default value is 1e-6.
   <LI> <code>timeResolution</code>: The minimum resolution
   of time. If two time values differ less than this value,
   they are considered equivalent. Default value is 1e-10.
   <P>
   This director also maintains a breakpoint table to record all
   predictable breakpoints that are greater than the current time.
   The breakpoints are sorted in their chronological order in the table.
   Breakpoints at the same time (controlled by the time resolution parameter)
   are considered to be one. A breakpoint can be inserted into the table by
   calling the fireAt() method. The fireAt method may be requested
   by the director, which inserts the start time and stop time of the
   execution. In this case, the first parameter actor is null. The fireAt
   method may also be requested by the actors and the requests are handled
   differently depending on the second parameter time. If the time is the
   current time, and if the actors are discrete actors, the actors are
   added into a refireActor list and will be fired in the current iteration.
   Otherwise, the time will be inserted into the breakpoint table.
   <p>

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (liuj)
   @Pt.AcceptedRating Green (chf)
   @see ptolemy.actor.Director
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
        } catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                    "Error setting a CTScheduler.");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("There is already a scheduler" +
                    " with name " + this.getFullName());
        }
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string.
     *  All the parameters take their default values. A CTScheduler
     *  is created.
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
        } catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                    "Error setting a CTScheduler.");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("There is already a scheduler" +
                    " with name " + this.getFullName());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** ODE solving error tolerance, only effective in variable step
     *  size methods.
     *  The default value is 1e-4, and the type is double.
     */
    public Parameter errorTolerance;

    /** User's hint for the initial integration step size.
     *  The default value is 0.1, and the type is double.
     */
    public Parameter initStepSize;

    /** The maximum number of iterations in looking for a fixed-point.
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

    /** Stop time of the simulation. The default value is Infinity, and
     *  the type is double.
     */
    public Parameter stopTime;

    /** Indicator whether the execution will synchronize to real time. The
     *  default value is false, and the type is boolean.
     */
    public Parameter synchronizeToRealTime;

    /** The resolution in comparing time.
     *  The default value is 1e-10, and the type is double.
     */
    public Parameter timeResolution;

    /** Value resolution in looking for a fixed-point.
     *  The default value is 1e-6, and the type is double.
     */
    public Parameter valueResolution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) _debug("Updating CTDirector parameter: ",
                attribute.getName());
        if (attribute == startTime) {
            double startTimeValue = 
                ((DoubleToken)startTime.getToken()).doubleValue();
            //_startTime = new Time(this, startTimeValue);
        } else if (attribute == stopTime) {
            double stopTimeValue = 
                ((DoubleToken)stopTime.getToken()).doubleValue();
            //_stopTime = new Time(this, stopTimeValue);
        } else if (attribute == initStepSize) {
            double value = ((DoubleToken)initStepSize.getToken()).
                doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative step size.");
            }
            _initStepSize = value;
        } else if (attribute == errorTolerance) {
            double value = ((DoubleToken)errorTolerance.getToken()).
                doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative error tolerance.");
            }
            _errorTolerance = value;
        } else if (attribute == minStepSize) {
            double value = ((DoubleToken)minStepSize.getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative step size.");
            }
            _minStepSize = value;
        } else if (attribute == maxStepSize) {
            double value = ((DoubleToken)maxStepSize.getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative step size.");
            }
            _maxStepSize = value;
        } else if (attribute == valueResolution) {
            double value = ((DoubleToken)valueResolution.getToken()).
                doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative value resolution.");
            }
            _valueResolution = value;
        } else if (attribute == timeResolution) {
            double value = ((DoubleToken)timeResolution.getToken()).
                doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative or zero time resolution.");
            }
            setTimeResolution(value);
            TotallyOrderedSet table = getBreakPoints();
        } else if (attribute == maxIterations) {
            int value = ((IntToken)maxIterations.getToken()).intValue();
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
     *  Derived class should override this to show whether it can
     *  serve as an inside director.
     */
    public abstract boolean canBeInsideDirector();

    /** Return true if the director can be a top-level director.
     *  Derived class should override this to show whether it can
     *  serve as a top-level director.
     */
    public abstract boolean canBeTopLevelDirector();

    /** Override the fire method of the super class. In this abstract
     *  base class, nothing is performed. 
     */
    public void fire() throws IllegalActionException {
    }
    
    /** Handle the firing request from the containing actors.
     *  If the specified time is the current time (within the
     *  <i>timeResolution</i> of this director), and the actor is
     *  not null nor a step size control actor, then the actor will
     *  be added into a refiredActors list and fired as part of the
     *  current execution phase.  If the specified time is in the
     *  future, or the actor is null, or the actor is a step size
     *  control actor, this director will register a breakpoint at
     *  the specified time.
     *  <p>
     *  From this director's point of view, it is irrelevant
     *  which actor requests the breakpoint. All actors will be
     *  executed at every breakpoint.
     *  <p>
     *  If the actor is null, it indicates the time for the start or
     *  the stop of an execution. Otherwise, it is the actor that
     *  requests the firing.
     *  <p>
     *  In CT domain, the signals may be piecewise continuous. They
     *  may generate discontinuities at some time points. In order to
     *  generate the discontinuities, the actors request to be fired at
     *  some time as many times as necessary, and each time they produce
     *  some outputs. The CT director does not deal with discontinuity
     *  but only handle the requests from the actors it contains. For
     *  example, the ContinuousClock actor fires twice at each offset.

     *  @param actor The actor that requested the firing or null that
     *    indicates the time of the start or stop of an execution.
     *  @param time The requested firing time.
     *  @exception IllegalActionException If the time is earlier than
     *   the current time.
     */
    public void fireAt(Actor actor, Time time)
            throws IllegalActionException{
        double resolution = getTimeResolution();
        Time currentTime = getCurrentTime();

        // Check if the request time is earlier than the current time.
        if (time.compareTo(currentTime) < 0 ) {
            throw new IllegalActionException((Nameable)actor,
                    "Requested fire time: " + time + " is earlier than" +
                    " the current time." + currentTime );
        }

        // Construct a breakpoint table if necessary
        if (_breakPoints == null) {
            _breakPoints = new TotallyOrderedSet(new GeneralComparator());
        }
        
        if (_debugging) {
            String name = "Director";
            if (actor != null) {
                name = ((Nameable)actor).getName();
            }
            _debug("     ----> " + name
                    + " requests refire at "
                    + time);
        }
        
        _breakPoints.insert(time);
    }

    /** Return the breakpoint table. If the breakpoint table has never
     *  been created, then return null. This method is final
     *  for performance reason.
     *  @return The breakpoint table.
     */
    public final TotallyOrderedSet getBreakPoints() {
        return _breakPoints;
    }

    /** Return the current ODE solver. This method is final
     *  for performance reason.
     *  @return The current ODE solver.
     */
    public final ODESolver getCurrentODESolver() {
        return _currentSolver;
    }

    /** Return the current integration step size. This method is final
     *  for performance reason.
     *  @return The current step size.
     */
    public double getCurrentStepSize() {
        return _currentStepSize;
    }

    /** Return the local truncation error tolerance, used by
     *  variable step size solvers. This method is final
     *  for performance reason.
     *  @return The local truncation error tolerance.
     */
    public final double getErrorTolerance() {
        return _errorTolerance;
    }

    /** Return the initial step size. This method is final
     *  for performance reason.
     *  @return The initial step size.
     */
    public final double getInitialStepSize() {
        return _initStepSize;
    }

    /** Return the begin time of the current iteration. This method is final
     *  for performance reason.
     *  @return The begin time of the current iteration.
     */
    public final Time getIterationBeginTime() {
        return _iterationBeginTime;
    }

    /** Return the end time of the current iteration. This method is final
     *  for performance reason.
     *  @return The end time of the current iteration.
     */
    public final Time getIterationEndTime() {
        return _iterationEndTime;
    }

    /** Return the maximum number of iterations in fixed point
     *  calculation. If the iteration has exceeded this number
     *  and the fixed point is still not found, then the algorithm
     *  is considered to have failed. This method is final
     *  for performance reason.
     *  @return The maximum number of iterations when calculating
     *  fixed points.
     */
    public final int getMaxIterations() {
        return _maxIterations;
    }

    /** Return the maximum step size used in variable step size
     *  ODE solvers. This method is final
     *  for performance reason.
     *  @return The maximum step size.
     */
    public final double getMaxStepSize() {
        return _maxStepSize;
    }

    /** Return the minimum step size used in variable step size
     *  ODE solvers. This method is final
     *  for performance reason.
     *  @return The minimum step size.
     */
    public final double getMinStepSize() {
        return _minStepSize;
    }

    /** Return the current iteration begin time plus the current
     *  step size.
     *  @return The iteration begin time plus the current step size.
     */
    public Time getNextIterationTime() {
        return getIterationBeginTime().add(getCurrentStepSize());
    }

    /** Return the ODE solver.
     *  @return The default ODE solver
     */
    public abstract ODESolver getODESolver();

    /** Return the start time parameter value. This method is final
     *  for performance reason.
     *  @return the start time.
     */
    public final Time getStartTime() {
        return _startTime;
    }

    /** Return the stop time. This method is final
     *  for performance reason.
     *  @return the stop time.
     */
    public final Time getStopTime() {
        return _stopTime;
    }

    /** Return the suggested next step size. The suggested step size is
     *  the minimum step size that the step-size-control actors suggested
     *  at the end of last integration step. It is the prediction
     *  of the new step size. This method is final
     *  for performance reason.
     *  @return The suggested next step size.
     */
    public final double getSuggestedNextStepSize() {
        return _suggestedNextStepSize;
    }

    /** Return the value resolution, used for testing if an implicit method
     *  has reached the fixed point. Two values that are differed less than
     *  this accuracy are considered identical in the fixed point
     *  calculation. This method is final
     *  for performance reason.
     *
     *  @return The value resolution for finding fixed point.
     */
    public final double getValueResolution() {
        return _valueResolution;
    }

    /** Initialization after type resolution.
     *  In addition to calling the initialize() method of its super class,
     *  this method records the current system time as the "real" starting
     *  time of the execution. This starting time is used when the
     *  execution is synchronized to real time. This method also reset 
     *  the protected variables. 
     *  
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        // Reset the _postFireReturns to true.
        _postfireReturns = true;
        // Record starting point of the real time (the computer system time)
        // in case the director is synchronized to the real time.
        _timeBase = System.currentTimeMillis();
        
        super.initialize();
    }

    /** Return true if this is the first iteration after a breakpoint.
     *  In a breakpoint iteration, the ODE solver is the breakpoint
     *  ODE solver, and the step size is zero or the minimum step size.
     *  This method is final for performance reason.
     *  @return True if this is a breakpoint iteration.
     */
    public final boolean isBreakpointIteration() {
        return _breakpointIteration;
    }

    /** Return true if this is the discrete phase execution.
     *  @return True if this is the discrete phase execution.
     */
    public boolean isDiscretePhase() {
        return _discretePhase;
    }

    /** Return true if the actor has been prefired in the current iteration.
     *  @param actor The actor about which we are asking.
     *  @see #setPrefireComplete(Actor)
     *  @return True if the actor has been prefired.
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

    // FIXME: postfire method, request refiring if not at top level.
    /** If this director is not at top level, request a refiring at
     *  the first breakpoint. 
     *  FIXME: the rest of the breakpoints may not be interesting...
     *  However, the stop time is important. 
     *  Return true if the director wishes to be scheduled for another
     *  iteration. This base class returns true if all of the actors
     *  iterated since the last call to prefire returned true from their
     *  postfire() method and if stop() has not been called. Subclasses
     *  may override this method to perform additional
     *  domain-specific behavior.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if (!_isTopLevel() && (getBreakPoints().size() > 0)) {
            Time time = (Time) getBreakPoints().removeFirst();
            CompositeActor container = (CompositeActor)getContainer();
            container.getExecutiveDirector().fireAt(container, time);
        }
        return _postfireReturns && !_stopRequested;
    }

    /** Clear the set of actors that have been prefired.
     */
    public void prefireClear() {
        _prefiredActors.clear();
    }

    /** Prepare for an execution.
     *  If this director does not have a container and a scheduler,
     *  or the director does not fit this level of hierarchy,
     *  an IllegalActionException will be thrown.
     *  Invalidate the schedule. Clear statistical variables.
     *  Clear the break point table.
     *  Preinitialize all the directed actors.
     *  Time does not have a meaning yet. So actors should not
     *  use a notion of time at the preinitialize stage.
     *
     *  @exception IllegalActionException If the director has no
     *  container, the director does not fit this level of hierarchy,
     *  or there is no scheduler.
     */
    public void preinitialize() throws IllegalActionException {
        if (_debugging) _debug(getFullName(), "preinitializing.");

        // Verify the correct director resides in an approriate level
        // of the hierarchy.
        Nameable nameable = getContainer();
        if (!(nameable instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "has no CompositeActor container.");
        }
        CompositeActor container = (CompositeActor)nameable;
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
        CTScheduler scheduler = (CTScheduler)getScheduler();
        if (scheduler == null) {
            throw new IllegalActionException( this,
                    "has no scheduler.");
        }
        // FIXME: Why do we need this?
        // Force to reconstruct a CT scheduler??
        // Will it be OK to synchronize to workspace version? 
        // invalidate schedule
        scheduler.setValid(false);

        super.preinitialize();

        // Initialize the local variables. 
        // The following method must be called after 
        // the super.preinitialize() method.
        _initializeLocalVariables();
    }

    /** Set the current step size. The current step size
     *  is very import during
     *  the simulation and should NOT be changed in the middle of an
     *  iteration.
     *  @param stepsize The step size to be set.
     */
    public void setCurrentStepSize(double stepsize) {
        // FIXME: we may not need to do this... since a small difference
        // of step size does not matter considering the value error tolerance
        // of the solvers...
        double newStepsize = Utilities.round(stepsize, getTimeResolution());
        if (_debugging) {
            _debug("----- Setting current step size to (adjusted) " 
                + newStepsize);
        }
        _currentStepSize = newStepsize;
    }

    /** Set a new value to the current time of the model, where the new
     *  time can be earlier than the current time to support rollback.
     *  This overrides the setCurrentTime() in the Director base class.
     *  It is OK that the new time is less than the current time
     *  in the director, since CT sometimes needs roll-back.
     *  This is a critical parameter in an execution, and the
     *  actors are not supposed to call it.
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(Time newTime) {
        // FIXME: we must be very careful about this...
        // If this feature is only necessary for the CTMixedSignalDirector,
        // but not the others... put the following code into there only...
        
        if (_debugging) {
            _debug("----- Setting current time to " + newTime);
        }
        _currentTime = newTime;
    }

    /** Mark the specified actor as having been prefired in the current
     *  iteration.
     *  @see #isPrefireComplete(Actor)
     *  @param actor The actor about which we are asking.
     */
    public void setPrefireComplete(Actor actor) {
        _prefiredActors.add(actor);
    }

    /** Set the suggested next step size. If the argument is
     *  large than the maximum step size, then set the
     *  suggested next step size to the
     *  maximum step size.
     *  @param stepsize The suggested next step size.
     */
    public void setSuggestedNextStepSize(double stepsize) {
        if (stepsize > getMaxStepSize()) {
            _suggestedNextStepSize = getMaxStepSize();
        } else {
            _suggestedNextStepSize = stepsize;
        }
    }

    /** Create and initialize all parameters to their default values.
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
            
            timeResolution = new Parameter(this, "timeResolution",
                    new DoubleToken(1e-10));
            timeResolution.setTypeEquals(BaseType.DOUBLE);
            
            synchronizeToRealTime = new Parameter(this,
                    "synchronizeToRealTime");
            synchronizeToRealTime.setExpression("false");
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    /** Instantiate an ODESolver from its classname. Given the solver's full
     *  class name, this method will try to instantiate it by looking
     *  for the java class.
     *  @param className The solver's full class name.
     *  @exception IllegalActionException If the solver is unable to be
     *       created.
     */
    protected ODESolver _instantiateODESolver(String className)
            throws IllegalActionException {
        ODESolver newSolver;
        if (_debugging) _debug("instantiating solver..." + className);
        try {
            Class solver = Class.forName(className);
            newSolver = (ODESolver)solver.newInstance();
        } catch(ClassNotFoundException e) {
            throw new IllegalActionException(this, "ODESolver: "+
                    className + " not found.");
        } catch(InstantiationException e) {
            throw new IllegalActionException(this, "ODESolver: "+
                    className + " instantiation failed.");
        } catch(IllegalAccessException e) {
            throw new IllegalActionException(this, "ODESolver: "+
                    className + " not accessible.");
        }
        newSolver._makeSolverOf(this);
        return newSolver;
    }

    /** Set to indicate that this is an iteration just after a breakpoint.
     *  A CTDirector, after finding out that a breakpoint has happened at
     *  the current time, should call this method with a true argument.
     *  In the next iteration,
     *  the solver may be changed to handle the breakpoint.
     *
     *  @param breakpoint True if this is a breakpoint iteration.
     */
    protected void _setBreakpointIteration(boolean breakpoint) {
        // FIXME: this method is misleading...
        // A better name is CreateIterationStates...
        
        // FIXME: This method may not be necessary since it is basically
        // wrapped by _setDiscretePhase ..
        if (_debugging) {
            if (breakpoint) {
                _debug("     << Creating the starting states for continuous " +
                    "phase execution ...");
            } else {
                _debug("     The starting states were created. >>");
            }
        }
        _breakpointIteration = breakpoint;
    }

    /** Set the current ODE Solver to be the argument.
     *  Derived class may throw an exception if the argument
     *  cannot serve as the current ODE solver
     *  @param solver The solver to set.
     *  @exception  IllegalActionException Not thrown in this base class.
     *     It may be thrown by the derived classes if the solver is not
     *     appropriate.
     */
    protected void _setCurrentODESolver(ODESolver solver)
            throws IllegalActionException {
        _currentSolver = solver;
    }

    /** Set the current phase of execution as a discrete phase. The value
     *  set will be returned by the isDiscretePhase() method.
     *  @param discrete True if this is the discrete phase.
     */
    protected void _setDiscretePhase(boolean discrete) {
        _discretePhase = discrete;
    }

    /** Set the iteration begin time. The iteration begin time is
     *  the start time for one integration step. This variable is used
     *  when the integration step is failed, and need to be restarted
     *  with another step size.
     *  @param time The iteration begin time.
     */
    protected void _setIterationBeginTime(Time time) {
        _iterationBeginTime = time;
    }

    /** Set the iteration end time. The iteration end time is
     *  the stop time for one integration step. This variable is used
     *  to ensure an iteration ends at an expected time without introducing
     *  small difference (as small as time resolution) via numerical 
     *  computation of solver.
     *  @param time The iteration begin time.
     */
    protected void _setIterationEndTime(Time time) {
        _iterationEndTime = time;
    }

    /** Returns false always, indicating that this director does not need to
     *  modify the topology during one iteration.
     *
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** This flag will be set to false if any actor returns false to
     *  its postfire().
     */
    protected boolean _postfireReturns = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the local variables of the CTDirector. 
     *  NOTE: This has to be called in the preinitialize method because
     *  the Time objects (_startTime and _stopTime) need the director
     *  to provide a time resolution. 
     *  NOTE: Time resolution is provided bythe super class (Director)
     *  with the preinitialize method. So, this method must be called 
     *  after the super.preinitialize() is called.
     *  NOTE: _timeBase is not initialized here but in initialize method
     *  instead to provide more accurate real time information.
     */
    private void _initializeLocalVariables() throws IllegalActionException {
       _currentSolver = null;

       _prefiredActors = new HashSet();

       double startTimeValue = 
           ((DoubleToken)startTime.getToken()).doubleValue();
       _startTime = new Time(this, startTimeValue);

       double stopTimeValue = 
           ((DoubleToken)stopTime.getToken()).doubleValue();
       _stopTime = new Time(this, stopTimeValue);

       _iterationBeginTime = _startTime;
       _iterationEndTime = _stopTime;

       _initStepSize = 0.1;
       _minStepSize = 1e-5;
       _maxStepSize = 1.0;
       _currentStepSize = _initStepSize;
       _suggestedNextStepSize = _initStepSize;

       _maxIterations = 20;
       _errorTolerance = 1e-4;
       _valueResolution = 1e-6;

        // A simulation always starts with a discrete phase execution
       _breakpointIteration = true;
       _discretePhase = true;

       // clear the existing breakpoint table or 
       // create a breakpoint table if necessary
       if (_debugging) _debug(getFullName(),
               "create/clear break point table.");
       TotallyOrderedSet breakpoints = getBreakPoints();
       if (breakpoints != null) {
           breakpoints.clear();
       } else {
           _breakPoints = new TotallyOrderedSet(new GeneralComparator());
       }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // FIXME: are all the following private variables initialized before using?
    
    // Current ODE solver.
    private ODESolver _currentSolver = null;

    /** Collection of actors that have been prefired(). */
    private Set _prefiredActors = new HashSet();

    // Local copies of parameters.
    private Time _startTime;
    private Time _stopTime;
    private double _initStepSize;
    private double _minStepSize;
    private double _maxStepSize;
    private int _maxIterations;
    private double _errorTolerance;
    private double _valueResolution;

    // The real starting time in term of system millisecond counts.
    protected long _timeBase;

    // Indicate whether this is a breakpoint iteration.
    private boolean _breakpointIteration = false;

    // Simulation step sizes.
    private double _currentStepSize;
    private double _suggestedNextStepSize;

    // A table for breakpoints.
    private TotallyOrderedSet _breakPoints;

    // The begin time of a iteration. This value is remembered so that
    // we don't need to resolve it from the iteration end time and step size.
    private Time _iterationBeginTime;
    
    private Time _iterationEndTime;

    // Indicate that this is the discrete phase.
    private boolean _discretePhase;

}
