/* Director for the synchronous dataflow model of computation.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.sdf.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.PeriodicDirector;
import ptolemy.actor.util.PeriodicDirectorHelper;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SDFDirector

/**
 Director for the synchronous dataflow (SDF) model of computation.

 <h1>SDF overview</h1>
 The Synchronous Dataflow(SDF) domain supports the efficient
 execution of Dataflow graphs that
 lack control structures.   Dataflow graphs that contain control structures
 should be executed using the Process Networks(PN) domain instead.
 SDF allows efficient execution, with very little overhead at runtime.  It
 requires that the rates on the ports of all actors be known before hand.
 SDF also requires that the rates on the ports not change during
 execution.  In addition, in some cases (namely systems with feedback) delays,
 which are represented by initial tokens on relations must be explicitly
 noted.  SDF uses this rate and delay information to determine
 the execution sequence of the actors before execution begins.
 <h2>Schedule Properties</h2>
 <ul>
 <li>The number of tokens accumulated on every relation is bounded, given
 an infinite number of executions of the schedule.</li>
 <li>Deadlock will never occur, given an infinite number of executions of
 the schedule.</li>
 </ul>
 <h1>Class comments</h1>
 An SDFDirector is the class that controls execution of actors under the
 SDF domain.  By default, actor scheduling is handled by the SDFScheduler
 class.  Furthermore, the newReceiver method creates Receivers of type
 SDFReceiver, which extends QueueReceiver to support optimized gets
 and puts of arrays of tokens.
 <p>
 Actors are assumed to consume and produce exactly one token per channel on
 each firing.  Actors that do not follow this convention should set
 the appropriate parameters on input and output ports to declare the number
 of tokens they produce or consume.  See the
 {@link ptolemy.domains.sdf.kernel.SDFScheduler} for more information.
 The {@link ptolemy.domains.sdf.lib.SampleDelay} actor is usually used
 in a model to specify the delay across a relation.
 </p><p>
 The <i>allowDisconnectedGraphs</i> parameter of this director determines
 whether disconnected graphs are permitted.
 A model may have two or more graphs of actors that
 are not connected.  The schedule can jump from one graph to
 another among the disconnected graphs. There is nothing to
 force the scheduler to finish executing all actors on one
 graph before firing actors on another graph. However, the
 order of execution within an graph should be correct.
 Usually, disconnected graphs in an SDF model indicates an
 error.
 The default value of the allowDisconnectedGraphs parameter is a
 BooleanToken with the value false.
 </p><p>
 The <i>iterations</i> parameter of this director corresponds to a
 limit on the number of times the director will fire its hierarchy
 before it returns false in postfire.  If this number is not greater
 than zero, then no limit is set and postfire will always return true.
 The default value of the iterations parameter is an IntToken with value one.
 </p><p>
 If any actor's postfire() method returns false during an iteration,
 then at the conclusion of the iteration, this director's postfire() method
 will return false. This will normally result in termination of the execution.
 The reasoning for this behavior is that the model cannot continue executing
 without the participation of all actors, and if any actor returns false
 in postfire(), then it is indicating that it wishes to not continue executing.
 </p><p>
 The <i>vectorizationFactor</i> parameter of this director sets the number
 of times that the basic schedule is executed during each firing of this
 director.  This might allow the director to execute the model more efficiently,
 by combining multiple firings of each actor.  The default value of the
 vectorizationFactor parameter is an IntToken with value one.
 </p><p>
 The SDF director has a <i>period</i> parameter which specifies the
 amount of model time that elapses per iteration. If the value of
 <i>period</i> is 0.0 (the default), then it has no effect, and
 this director never increments time nor calls fireAt() on the
 enclosing director. If the period is greater than 0.0, then
 if this director is at the top level, it increments
 time by this amount in each invocation of postfire().
 If it is not at the top level, then it calls
 fireAt(currentTime + period) in postfire().
 </p><p>
 This behavior gives an interesting use of SDF within DE:
 You can "kick start" an SDF submodel with a single
 event, and then if the director of that SDF submodel
 has a period greater than 0.0, then it will continue to fire
 periodically with the specified period.
 </p><p>
 If <i>period</i> is greater than 0.0 and the parameter
 <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the prefire() method stalls until the real time elapsed
 since the model started matches the period multiplied by
 the iteration count.
 This ensures that the director does not get ahead of real time. However,
 of course, this does not ensure that the director keeps up with real time.
 </p>
 @see ptolemy.domains.sdf.kernel.SDFScheduler
 @see ptolemy.domains.sdf.kernel.SDFReceiver

 @author Steve Neuendorffer, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class SDFDirector extends StaticSchedulingDirector implements
PeriodicDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SDFDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     *
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SDFDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The SDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A parameter representing whether disconnected graphs are
     *  permitted.  A model may have two or more graphs of actors that
     *  are not connected.  The schedule can jump from one graph to
     *  another among the disconnected graphs. There is nothing to
     *  force the scheduler to finish executing all actors on one
     *  graph before firing actors on another graph. However, the
     *  order of execution within an graph should be correct.
     *  Usually, disconnected graphs in an SDF model indicates an
     *  error.  The default value is a BooleanToken with the value
     *  false.
     */
    public Parameter allowDisconnectedGraphs;

    /** A parameter representing whether dynamic rate changes are
     *  permitted.  An SDF model may constructed such that the values
     *  of rate parameters are modified during the execution of the
     *  system.  If this parameter is true, then such models are
     *  valid and this class dynamically computes a new schedule at
     *  runtime.  If this parameter is false, then the SDF domain
     *  performs a static check to disallow such models.  Note that in
     *  order to generate code from an SDF model, this parameter must
     *  be set to false.  This is a boolean with default
     *  value false.
     */
    public Parameter allowRateChanges;

    /** If true, then buffer sizes are fixed according to the schedule,
     *  and attempts to write to the buffer that cause the buffer to
     *  exceed the schedule size result in an exception. This method
     *  works by setting the capacity of the receivers if the value is
     *  true. This parameter is a boolean that defaults to true.
     */
    public Parameter constrainBufferSizes;

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in postfire,
     *  and thus the execution can continue forever. Note that the amount
     *  of data processed by the SDF model is a function of both this
     *  parameter and the value of parameter <i>vectorizationFactor</i>, since
     *  <i>vectorizationFactor</i> can influence the choice of schedule.
     *
     *  <p>If the number of iterations is -1, which is the value of
     *  the AUTO choice in the UI, then if the container of the
     *  director is the the top level then one iteration will occur
     *  before postfire() returns false.</p>
     *
     *  <p>If the number of iterations is -1 and and the container of
     *  the director is <b>not</b> at the top level then postfire()
     *  will always return true and execution will continue
     *  forever.</p>
     *
     *  The default value is an IntToken with the value AUTO, which
     *  is -1.  The UI has a second choice: UNBOUNDED, which is 0.
     */
    public Parameter iterations;

    /** The time period of each iteration.  This parameter has type double
     *  and default value 0.0, which means that this director does not
     *  increment model time and does not request firings by calling
     *  fireAt() on any enclosing director.  If the value is set to
     *  something greater than 0.0, then if this director is at the
     *  top level, it will increment model time by the specified
     *  amount in its postfire() method. If it is not at the top
     *  level, then it will call fireAt() on the enclosing executive
     *  director with the argument being the current time plus the
     *  specified period.
     */
    public Parameter period;

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter has type boolean and defaults
     *  to false. If set to true, then this director stalls in the
     *  prefire() method until the elapsed real real time matches
     *  the product of the <i>period</i> parameter value and the
     *  iteration count. If the <i>period</i> parameter has value
     *  0.0 (the default), then changing this parameter to true
     *  has no effect.
     */
    public Parameter synchronizeToRealTime;

    /** A Parameter representing the requested vectorization factor.
     *  The director will attempt to construct a schedule where each
     *  actor fires <i>vectorizationFactor</i> times more often than
     *  it would in a minimal schedule.  This can allow actor executions
     *  to be grouped together, resulting in faster execution.  This is
     *  more likely to be possible in graphs without tight feedback.
     *  This parameter must be a positive integer.
     *  The default value is an IntToken with the value one.
     */
    public Parameter vectorizationFactor;

    /** The value used to signify special behavior for the
     *  iterations parameter.
     */
    public static final IntToken AUTO_INTTOKEN = new IntToken(-1);

    /** The name of the AUTO iterations parameter choice: "AUTO". */
    public static final String AUTO_NAME = "AUTO";

    /** The UNBOUNDED iterations choice is equivalent to IntToken.ZERO. */
    public static final IntToken UNBOUNDED_INTTOKEN = IntToken.ZERO;

    /** The name of the UNBOUNDED iterations parameter choice: "UNBOUNDED". */
    public static final String UNBOUNDED_NAME = "UNBOUNDED";

    /** The name of the iterations parameter: "iterations". */
    public static final String ITERATIONS_NAME = "iterations";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // NOTE: Invalidate the schedules only if the values of these
        // parameters have changed.
        if (attribute == allowDisconnectedGraphs) {
            Token token = allowDisconnectedGraphs.getToken();
            boolean newValue = ((BooleanToken) token).booleanValue();
            if (newValue != _allowDisconnectedGraphs) {
                _allowDisconnectedGraphs = newValue;
                invalidateSchedule();
            }
        } else if (attribute == vectorizationFactor) {
            Token token = vectorizationFactor.getToken();
            int newValue = ((IntToken) token).intValue();
            if (newValue != _vectorizationFactor) {
                _vectorizationFactor = newValue;
                invalidateSchedule();
            }
        }

        super.attributeChanged(attribute);
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
        SDFDirector newObject = (SDFDirector) super.clone(workspace);

        // Subclasses may set this to null and handle this themselves.
        try {
            newObject._periodicDirectorHelper = new PeriodicDirectorHelper(
                    newObject);
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException(
                    "Failed to create PeriodicDirectorHelper.");
        }

        return newObject;
    }

    /**  Create the SDF schedule for this director.
     */
    @Override
    public void createSchedule() throws IllegalActionException {
        BaseSDFScheduler scheduler = (BaseSDFScheduler) getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to initialize "
                    + "SDF system with no scheduler");
        }

        // force the schedule to be computed.
        if (_debugging) {
            _debug("### Schedule:");
        }

        try {
            Schedule schedule = scheduler.getSchedule();
            if (_debugging) {
                _debug(schedule.toString());
                _debug("### End schedule");
            }
        } catch (NotSchedulableException ex) {
            // Capt. Robbins suggested that we show which actors are connected
            // or disconnected at the top, rather than burying it.
            throw ex;
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to compute schedule:");
        }

        // Declare the dependencies of rate parameters of external
        // ports.  Note that this must occur after scheduling, since
        // rate parameters are assumed to exist.
        scheduler.declareRateDependency();
    }

    /** Return the number of iterations.
     *
     *  <p>The number of iterations returned depends on the value of
     *  the <i>iterations</i> parameter and whether the container
     *  of the director is at the top level.
     *  See the {@link #iterations} documentation for details.</p>
     *
     *  <p>Code that uses SDFDirector should call getIterations()
     *  instead of directly referring to the value of the
     *  <i>iterations</i> parameter.</p>
     *
     *  @return the number of iterations
     *  @exception IllegalActionException If thrown while getting the
     *  value of the iterations parameter.
     */
    public int getIterations() throws IllegalActionException {
        // See "SDF director iterations parameter default of 0 is unfriendly"
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5546
        int iterationsValue = ((IntToken) iterations.getToken()).intValue();
        if (iterationsValue > 0) {
            return iterationsValue;
        }
        // The director should call isEmbedded()
        // instead of seeing whether the container's container is null.
        // The reason for this is RunCompositeActor, where the container's
        // container is not null, but you still want the model to behave
        // as if it were at the top level...
        if (!isEmbedded()) {
            // The container of this director is at the toplevel
            if (iterations.getToken().equals(AUTO_INTTOKEN)) {
                return 1;
            }
        }
        return 0;
    }

    /** Return the time value of the next iteration.
     *  If this director is at the top level, then the returned value
     *  is the current time plus the period. Otherwise, this method
     *  delegates to the executive director.
     *  @return The time of the next iteration.
     *  @exception IllegalActionException If time objects cannot be created.
     */
    @Override
    public Time getModelNextIterationTime() throws IllegalActionException {
        if (!_isTopLevel()) {
            return super.getModelNextIterationTime();
        }
        try {
            double periodValue = periodValue();

            if (periodValue > 0.0) {
                return getModelTime().add(periodValue);
            } else {
                return getModelTime();
            }
        } catch (IllegalActionException exception) {
            // This should have been caught by now.
            throw new InternalErrorException(exception);
        }
    }

    /** Call super.fire() and reset the _prefire flag.
     *  @exception IllegalActionException Thrown by super class.
     */
    @Override
    public void fire() throws IllegalActionException {
        _prefire = false;
        super.fire();
    }

    /** Request a firing of the given actor at the given absolute
     *  time, and return the time at which the specified will be
     *  fired. If the <i>period</i> is 0.0 and there is no enclosing
     *  director, then this method returns the current time. If
     *  the period is 0.0 and there is an enclosing director, then
     *  this method delegates to the enclosing director, returning
     *  whatever it returns. If the <i>period</i> is not 0.0, then
     *  this method checks to see whether the
     *  requested time is equal to the current time plus an integer
     *  multiple of the period. If so, it returns the requested time.
     *  If not, it returns current time plus the period.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The microstep (ignored by this director).
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     *  @return Either the requested time or the current time plus the
     *  period.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        if (_periodicDirectorHelper != null) {
            return _periodicDirectorHelper.fireAt(actor, time);
        }
        return super.fireAt(actor, time);
    }

    /** Initialize the actors associated with this director and then
     *  set the iteration count to zero.  The order in which the
     *  actors are initialized is arbitrary.  In addition, if actors
     *  connected directly to output ports have initial production,
     *  then copy that initial production to the outside of the
     *  composite actor.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler.
     */
    @Override
    public void initialize() throws IllegalActionException {

        super.initialize();
        _iterationCount = 0;

        if (_periodicDirectorHelper != null) {
            _periodicDirectorHelper.initialize();
        }

        CompositeActor container = (CompositeActor) getContainer();

        for (Iterator ports = container.outputPortList().iterator(); ports
                .hasNext();) {
            IOPort port = (IOPort) ports.next();

            // Create external initial production.
            int rate = DFUtilities.getTokenInitProduction(port);

            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    for (int k = 0; k < rate; k++) {
                        if (port.hasTokenInside(i)) {
                            Token t = port.getInside(i);

                            if (_debugging) {
                                _debug(getName(), "transferring output from "
                                        + port.getName());
                            }

                            port.send(i, t);
                        } else {
                            throw new IllegalActionException(this, port,
                                    "Port should produce " + rate
                                    + " tokens, but there were only "
                                    + k + " tokens available.");
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }
    }

    /** Return a new receiver consistent with the SDF domain.
     *  @return A new SDFReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Return the value of the period as a double.
     *  @return The value of the period as a double.
     *  @exception IllegalActionException If the period parameter
     *   cannot be evaluated
     */
    @Override
    public double periodValue() throws IllegalActionException {
        return ((DoubleToken) period.getToken()).doubleValue();
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do.  If there are no input ports, then also return true.
     *  Otherwise, return false.  Note that this does not call prefire()
     *  on the contained actors.
     *  <p>
     *  This method also implements the functionality of
     *  <i>synchronizeToRealTime</i> by waiting for real time
     *  to elapse if the parameter value is true.
     *  @exception IllegalActionException If port methods throw it.
     *  @return true If all of the input ports of the container of this
     *  director have enough tokens.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // Set current time based on the enclosing model.

        // If prefire returns true and prefire is called again
        // without calling fire in between,
        // which can happen when resourceScheduling is enabled,
        // then return true again. Otherwise check prefire
        // conditions.
        if (_aspectsPresent && _prefire) {
            return true;
        }
        _prefire = super.prefire();

        if (!_prefire) {
            return false;
        }

        double periodValue = periodValue();
        boolean synchronizeValue = ((BooleanToken) synchronizeToRealTime
                .getToken()).booleanValue();

        if (periodValue > 0.0 && synchronizeValue) {
            int depth = 0;
            try {
                synchronized (this) {
                    while (true) {
                        long elapsedTime = elapsedTimeSinceStart();

                        // NOTE: We assume that the elapsed time can be
                        // safely cast to a double.  This means that
                        // the SDF domain has an upper limit on running
                        // time of Double.MAX_VALUE milliseconds.
                        double elapsedTimeInSeconds = elapsedTime / 1000.0;
                        double currentTime = getModelTime().getDoubleValue();

                        if (currentTime <= elapsedTimeInSeconds) {
                            break;
                        }

                        long timeToWait = (long) ((currentTime - elapsedTimeInSeconds) * 1000.0);

                        if (_debugging) {
                            _debug("Waiting for real time to pass: "
                                    + timeToWait);
                        }

                        try {
                            // NOTE: The built-in Java wait() method
                            // does not release the
                            // locks on the workspace, which would block
                            // UI interactions and may cause deadlocks.
                            // SOLUTION: explicitly release read permissions.
                            if (timeToWait > 0) {
                                // Bug fix from J. S. Senecal:
                                //
                                //  The problem was that sometimes, the
                                //  method Object.wait(timeout) was called
                                //  with timeout = 0. According to java
                                //  documentation:
                                //
                                // " If timeout is zero, however, then
                                // real time is not taken into
                                // consideration and the thread simply
                                // waits until notified."
                                depth = _workspace.releaseReadPermission();
                                wait(timeToWait);
                            }
                        } catch (InterruptedException ex) {
                            // Continue executing.
                        }
                    }
                }
            } finally {
                if (depth > 0) {
                    _workspace.reacquireReadPermission(depth);
                }
            }
        }

        // Refuse to fire if the period is greater than zero and the current
        // time is not a multiple of the period.
        if (_periodicDirectorHelper != null
                && !_periodicDirectorHelper.prefire()) {
            if (_debugging) {
                _debug("Current time is not a multiple of the period or the microstep is 0. Returning false.\n"
                        + "Current time: "
                        + getModelTime()
                        + "  Period: "
                        + periodValue);
            }
            return false;
        }

        // Check to see whether the input ports have enough data.
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Iterator inputPorts = container.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();

            // NOTE: If the port is a ParameterPort, then we should not
            // insist on there being an input.
            if (inputPort instanceof ParameterPort) {
                continue;
            }

            int threshold = DFUtilities.getTokenConsumptionRate(inputPort);

            if (_debugging) {
                _debug("checking input " + inputPort.getFullName());
                _debug("Threshold = " + threshold);
            }

            for (int channel = 0; channel < inputPort.getWidth(); channel++) {
                if (threshold > 0 && !inputPort.hasToken(channel, threshold)) {
                    if (_debugging) {
                        _debug("Port " + inputPort.getFullName()
                                + " does not have enough tokens: " + threshold
                                + " Prefire returns false.");
                    }

                    return false;
                }
            }
        }

        if (_debugging) {
            _debug("Director prefire returns true.");
        }

        return true;
    }

    /** Preinitialize the actors associated with this director and
     *  compute the schedule.  The schedule is computed during
     *  preinitialization so that hierarchical opaque composite actors
     *  can be scheduled properly, since the act of computing the
     *  schedule sets the rate parameters of the external ports.  In
     *  addition, performing scheduling during preinitialization
     *  enables it to be present during code generation.  The order in
     *  which the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        createSchedule();
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the system return
     *  false in postfire.  Increment the number of iterations.
     *  If the "iterations" parameter is greater than zero, then
     *  see if the limit has been reached.  If so, return false.
     *  Otherwise return true if all of the fired actors since the last
     *  call to prefire returned true.
     *  If the <i>period</i> parameter is greater than 0.0, then
     *  if this director is at the top level, then increment time
     *  by the specified period, and otherwise request a refiring
     *  at the current time plus the period.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the iterations parameter
     *  does not contain a legal value.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        int iterationsValue = getIterations();
        _iterationCount++;

        if (iterationsValue > 0 && _iterationCount >= iterationsValue) {
            _iterationCount = 0;
            if (_debugging) {
                _debug("Reached specified number of iterations: "
                        + iterationsValue);
            }
            return false;
        }

        boolean result = super.postfire();
        if (_periodicDirectorHelper != null) {
            _periodicDirectorHelper.postfire();
        }
        return result;
    }

    /** Return an array of suggested ModalModel directors  to use with
     *  SDFDirector. The default director is HDFFSMDirector, which supports
     *  multirate actors and only allows state transitions on each iteration.
     *  This is the most safe director to use with SDF models.
     *  MultirateFSMDirector supports multirate actors and allows state
     *  transitions on each firing of the modal model. MultirateFSMDirector
     *  can be used with SDF if rate signatures for all the states in the
     *  modal model are same. If rate signatures change during an iteration,
     *  the SDFDirector will throw an exception.
     *  FSMDirector can be used with SDFDirector only when rate signatures
     *  for modal model are all 1.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    @Override
    public String[] suggestedModalModelDirectors() {
        return new String[] { "ptolemy.domains.modal.kernel.FSMDirector",
                "ptolemy.domains.modal.kernel.MultirateFSMDirector",
        "ptolemy.domains.hdf.kernel.HDFFSMDirector" };
    }

    /** Return true to indicate that a ModalModel under control
     *  of this director supports multirate firing.
     *  @return True indicating a ModalModel under control of this director
     *  supports multirate firing.
     */
    @Override
    public boolean supportMultirateFiring() {
        return true;
    }

    /** Override the base class method to transfer enough tokens to
     *  complete an internal iteration.  If there are not enough tokens,
     *  then throw an exception.  If the port is not connected on the
     *  inside, or has a narrower width on the inside than on the outside,
     *  then consume exactly one token from the corresponding outside
     *  channels and discard it.  Thus, a port connected on the outside
     *  but not on the inside can be used as a trigger for an SDF
     *  composite actor.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port, or if there are not enough input tokens available.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }

        // The number of tokens depends on the schedule, so make sure
        // the schedule is valid.
        getScheduler().getSchedule();

        int rate = DFUtilities.getTokenConsumptionRate(port);
        boolean wasTransferred = false;

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (i < port.getWidthInside()) {
                    for (int k = 0; k < rate; k++) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);

                            if (_debugging) {
                                _debug(getName(), "transferring input from "
                                        + port.getName());
                            }

                            port.sendInside(i, t);
                            wasTransferred = true;
                        } else {
                            throw new IllegalActionException(this, port,
                                    "Port should consume " + rate
                                    + " tokens, but there were only "
                                    + k + " tokens available.");
                        }
                    }
                } else if (port.isKnown(i)) {
                    // No inside connection to transfer tokens to.
                    // Tolerate an unknown input, but if it is known, then
                    // transfer the input token if there is one.
                    // In this case, consume one input token if there is one.
                    if (_debugging) {
                        _debug(getName(),
                                "Dropping single input from " + port.getName());
                    }

                    if (port.hasToken(i)) {
                        port.get(i);
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }

        return wasTransferred;
    }

    /** Override the base class method to transfer enough tokens to
     *  fulfill the output production rate.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port. If any channel of the output port has no data, then
     *  that channel is ignored.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  output port.
     */
    @Override
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque output port.");
        }

        int rate = DFUtilities.getTokenProductionRate(port);
        boolean wasTransferred = false;

        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                for (int k = 0; k < rate; k++) {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);

                        if (_debugging) {
                            _debug(getName(), "transferring output from "
                                    + port.getName());
                        }

                        port.send(i, t);
                        wasTransferred = true;
                    } else {
                        throw new IllegalActionException(this, port,
                                "Port should produce " + rate
                                + " tokens, but there were only " + k
                                + " tokens available.");
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }

        return wasTransferred;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The iteration count. */
    protected int _iterationCount = 0;

    /** Helper class supporting the <i>period</i> parameter. */
    protected PeriodicDirectorHelper _periodicDirectorHelper;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler, an iterations
     *  parameter and a vectorizationFactor parameter.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {

        // AUTO and UNBOUNDED are used to set the value of iterations,
        // see the getIterations() method.

        Parameter AUTO = new Parameter(this, AUTO_NAME);
        AUTO.setToken(AUTO_INTTOKEN);
        AUTO.setVisibility(Settable.EXPERT);
        AUTO.setPersistent(false);

        Parameter UNBOUNDED = new Parameter(this, UNBOUNDED_NAME);
        UNBOUNDED.setToken(UNBOUNDED_INTTOKEN);
        UNBOUNDED.setVisibility(Settable.EXPERT);
        UNBOUNDED.setPersistent(false);

        iterations = new Parameter(this, ITERATIONS_NAME);
        iterations.setTypeEquals(BaseType.INT);
        iterations.addChoice(AUTO_NAME);
        iterations.addChoice(UNBOUNDED_NAME);
        iterations.setExpression(AUTO_NAME);

        vectorizationFactor = new Parameter(this, "vectorizationFactor");
        vectorizationFactor.setTypeEquals(BaseType.INT);
        vectorizationFactor.setExpression("1");

        allowDisconnectedGraphs = new Parameter(this, "allowDisconnectedGraphs");
        allowDisconnectedGraphs.setTypeEquals(BaseType.BOOLEAN);
        allowDisconnectedGraphs.setExpression("false");

        allowRateChanges = new Parameter(this, "allowRateChanges");
        allowRateChanges.setTypeEquals(BaseType.BOOLEAN);
        allowRateChanges.setExpression("false");

        constrainBufferSizes = new Parameter(this, "constrainBufferSizes");
        constrainBufferSizes.setTypeEquals(BaseType.BOOLEAN);
        constrainBufferSizes.setExpression("true");

        period = new Parameter(this, "period", new DoubleToken(1.0));
        period.setTypeEquals(BaseType.DOUBLE);
        period.setExpression("0.0");

        synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
        synchronizeToRealTime.setExpression("false");
        synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

        startTime.moveToLast();
        stopTime.moveToLast();

        SDFScheduler scheduler = new SDFScheduler(this, uniqueName("Scheduler"));
        scheduler.constrainBufferSizes.setExpression("constrainBufferSizes");
        setScheduler(scheduler);

        // Subclasses may set this to null and handle this themselves.
        _periodicDirectorHelper = new PeriodicDirectorHelper(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                package friendly variables                 ////

    /** Cache of the value of allowDisconnectedGraphs. */
    boolean _allowDisconnectedGraphs = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Cache of the most recent value of vectorizationFactor. */
    private int _vectorizationFactor = 1;

}
