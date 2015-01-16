/* This director implements the Ptides programming model.

@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.ActorExecutionAspect;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.DiscreteClock;
import ptolemy.actor.lib.PoissonClock;
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.PeriodicDirector;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.ptides.lib.PtidesPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/** This director implements the Ptides programming model,
 *  which is used for the design of distributed real-time systems.
 *
 *  <p> This director can only be used inside the PtidesPlatform
 *  and if the PtidesPlatform is dragged and dropped from the library,
 *  it already contains a PtidesDirector. A PtidesPlatform must be
 *  embedded in a timed director such as DE or Continuous. The time of
 *  this enclosing director simulates physical time.
 *  The localClock of this PtidesDirector simulates platformTime.</p>
 *
 *  <p> The Ptides director is based on the DE director. Like the DE
 *  director, this director maintains a totally ordered set of events.
 *  Event timestamps are given in logical time. The logical time is decoupled
 *  from the platformTime. The model time of the director is the
 *  platformTime unless an actor is fired; then the model time is
 *  the timestamp of the event that caused the actor firing.</p>
 *
 *  <p>Unlike the DE Director, this director can process events out of timestamp order
 *  if they are not causally related. Whether events can be processed
 *  is checked in a safe-to-process analysis.
 *  This analysis returns a boolean to indicate whether an event
 *  can be processed without violating Ptides
 *  semantics, based on information such as events currently in the
 *  event queue, their model time relationship with each other, as
 *  well as the current platform physical time. In this particular version of
 *  the Ptides scheduler, the director takes all events
 *   from the event queue, and compares their timestamp
 *  with the current platform time + a pre-computed offset (call this
 *  the delayOffset). If the platform time is larger, than this event
 *  is safe to process. Otherwise, we wait for platform time to pass
 *  until this event becomes safe, at which point it is processed.
 *  Other, smarter kinds of safe-to-process analysis can be
 *  implemented in future versions. </p>
 *
 *  <p>Currently, only
 *  the DE director can be used as the enclosing director. One reason
 *  for using the DE director is that time cannot go backwards in DE,
 *  which is an important physical time property. More importantly,
 *  the fire method of this director changes the persistent state of
 *  actors, which means this director cannot be used inside of an
 *  actor that performs fix point iteration, which includes (currently),
 *  Continuous, CT and SR. For more details, please refer
 *  to Edward A. Lee, Haiyang Zheng. <a
 *  href="http://chess.eecs.berkeley.edu/pubs/430.html">Leveraging
 *  Synchronous Language Principles for Heterogeneous Modeling
 *  and Design of Embedded Systems</a>, Proceedings of the
 *  7th ACM &amp; IEEE international conference on Embedded
 *  software, ACM, 114-123, 2007.</p>
 *
 *  <p> This director provides a set of features to address both
 *  the distributed and the real-time aspects of system design.
 *  To address the distributed aspect, each PtidesPlatform simulates
 *  a computation platform
 *  (e.g., a microprocessor), while the enclosing director simulates
 *  the physical world. Actors under the Ptides director then communicate
 *  to the outside via SensorPorts, ActuatorPorts, or network ports
 *  (NetworkReceivers, NetworkTransmitters).</p>
 *
 *  <p> This director allows for simulation of execution time. If the PtidesPlatform
 *  contains ResourceSchedulers, the scheduling of actors is performed by these.
 *  Actors must specify in parameters which ResourceSchedulers they are assigned
 *  to and the executionTime. The passage of execution time equals the passage
 *  of platformTime. Execution time has no influence on the event timestamps.</p>
 *
 *  <p> In a Ptides environment, all platforms are assumed to be synchronized
 *  within a bounded error.</p>
 *
 *  <p> The platform time is used in the following
 *  situations: generating timestamps for sensor events, enforcing deadlines
 *  for actuation events, and to setup the wake-up time for timed interrupts.
 *  Also, the Ptides operational semantics assumes
 *  a bound in the time synchronization error. This error is captured in the
 *  parameter {@link #clockSynchronizationErrorBound}. If
 *  the actual error exceeds this bound, the safe-to-process analysis could
 *  produce an incorrect result. The demo PtidesNetworkLatencyTest illustrates
 *  this error.</p>
 *
 *  <p> The implementation is based on the operational semantics
 *  of Ptides, as described in: Jia Zou, Slobodan Matic, Edward
 *  A. Lee, Thomas Huining Feng, Patricia Derler.  <a
 *  href="http://chess.eecs.berkeley.edu/pubs/529.html">Execution
 *  Strategies for Ptides, a Programming Model for Distributed
 *  Embedded Systems</a>, 15th IEEE Real-Time and Embedded Technology
 *  and Applications Symposium, 2009, IEEE Computer Society, 77-86,
 *  April, 2009.</p>
 *
 *
 *  @author Patricia Derler, Edward A. Lee, Slobodan Matic, Mike Zimmer, Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class PtidesDirector extends DEDirector implements Decorator {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public PtidesDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        clockSynchronizationErrorBound = new SharedParameter(this,
                "clockSynchronizationErrorBound");
        clockSynchronizationErrorBound.setTypeEquals(BaseType.DOUBLE);
        clockSynchronizationErrorBound.setExpression("0.0");
        _clockSynchronizationErrorBound = new Time(this, 0.0);

        autoThrottling = new Parameter(this, "autoThrotting");
        autoThrottling.setTypeEquals(BaseType.BOOLEAN);
        autoThrottling.setExpression("true");
        autoThrottling.setVisibility(Settable.EXPERT);
        _autoThrottling = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** Bound on clock synchronization error across all platforms.
     */
    public SharedParameter clockSynchronizationErrorBound;

    /** Auto throttling of local sources. This parameter is only visible
     *  in expert mode and defaults to the boolean value true.
     */
    public Parameter autoThrottling;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new event to the input queue. Compute the time when
     *  this input can be consumed and store in queue. The time depends on
     *  the device delay.
     *  @param sourcePort the source port.
     *  @param event New input event.
     *  @param deviceDelay The device delay.
     *  @exception IllegalActionException If device delay parameter cannot be computed.
     */
    public void addInputEvent(PtidesPort sourcePort, PtidesEvent event,
            double deviceDelay) throws IllegalActionException {
        if (sourcePort.isNetworkReceiverPort()) {
            double networkDelayBound = PtidesDirector._getDoubleParameterValue(
                    sourcePort, "networkDelayBound");
            double sourcePlatformDelayBound = PtidesDirector
                    ._getDoubleParameterValue(sourcePort,
                            "sourcePlatformDelayBound");
            if (localClock.getLocalTime().subtract(event.timeStamp())
                    .getDoubleValue() > sourcePlatformDelayBound
                    + networkDelayBound
                    + _clockSynchronizationErrorBound.getDoubleValue()) {
                event = _handleTimingError(
                        sourcePort,
                        event,
                        "Event on this network receiver came in too late. "
                                + "(Physical time: "
                                + localClock.getLocalTime()
                                + ", Event timestamp: " + event.timeStamp()
                                + ", Source platform delay bound: "
                                + sourcePlatformDelayBound
                                + ", Network delay bound: " + networkDelayBound
                                + ")");
            }
        }

        if (event != null) {
            Time inputReady = getModelTime().add(deviceDelay);
            List<PtidesEvent> list = _inputEventQueue.get(inputReady);
            if (list == null) {
                list = new ArrayList<PtidesEvent>();
            }

            list.add(event);
            _inputEventQueue.put(inputReady, list);
        }
    }

    /** Update the director parameters when attributes are changed.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *  Not thrown in this class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == clockSynchronizationErrorBound) {
            _clockSynchronizationErrorBound = new Time(this,
                    ((DoubleToken) clockSynchronizationErrorBound.getToken())
                    .doubleValue());
        } else if (attribute == autoThrottling) {
            _autoThrottling = ((BooleanToken) autoThrottling.getToken())
                    .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PtidesDirector newObject = (PtidesDirector) super.clone(workspace);
        try {
            newObject._clockSynchronizationErrorBound = new Time(newObject, 0.0);
            newObject._numberOfTokensPerPort = new HashMap<IOPort, Integer>();
        } catch (IllegalActionException e) {
            // cannot happen.
        }
        return newObject;
    }

    /** Create and return the decorated attributes for the PtidesDirector.
     *  The director decorates local sources with throttling attributes that
     *  allow for specification of how far ahead in logical time these actors
     *  can execute or how many tokens they can produce at a time.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or null if the
     *   specified NamedObj is not decorated by this decorator.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (_isLocalSource(target)) {
            try {
                return new ThrottleAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }
        return null;
    }

    /**
     * Return the default dependency between input and output ports,
     * which for the Ptides domain is a {@link SuperdenseDependency}.
     *
     * @return The default dependency that describes a time delay of 0.0,
     *          and a index delay of 0 between ports.
     */
    @Override
    public Dependency defaultDependency() {
        return SuperdenseDependency.OTIMES_IDENTITY;
    }

    /** Return local sources contained by the composite of this director.
     *  @return List of entities.
     */
    @Override
    public List<NamedObj> decoratedObjects() {
        List<NamedObj> list = new ArrayList();
        CompositeEntity container = (CompositeEntity) getContainer();
        for (Object target : container.entityList()) {
            if (_isLocalSource(target)) {
                list.add((NamedObj) target);
            }
        }
        return list;
    }

    /**
     * Before super.fire() is called, transfer all input events that are ready are
     * transferred. After super.fire() is called, transfer all output events that
     * are ready are transferred.
     */
    @Override
    public void fire() throws IllegalActionException {

        // Transfer all inputs that are ready.
        List<PtidesEvent> list = _inputEventQueue.get(getModelTime());
        if (list != null) {
            for (PtidesEvent event : list) {
                if (event.ioPort() != null) {
                    _currentLogicalTime = event.timeStamp();
                    _currentSourceTimestamp = event.sourceTimestamp();
                    _currentLogicalIndex = event.microstep();
                    event.receiver().put(event.token());
                    _currentLogicalTime = null;
                    if (_debugging) {
                        _debug("iiiiiiii - transfer inputs from "
                                + event.ioPort());
                    }
                }
            }
            _inputEventQueue.remove(getModelTime());
        }

        super.fire();

        // Transfer all outputs to the ports that are ready.
        list = _outputEventDeadlines.get(getModelTime());
        if (list != null) {
            for (PtidesEvent event : list) {
                _currentLogicalTime = event.timeStamp();
                _currentSourceTimestamp = event.sourceTimestamp();
                _currentLogicalIndex = event.microstep();
                if (event.ioPort() instanceof PtidesPort) {
                    double deviceDelay = _getDoubleParameterValue(
                            event.ioPort(), "deviceDelay");

                    Queue<PtidesEvent> ptidesOutputPortList = _ptidesOutputPortEventQueue
                            .get(event.ioPort());
                    if (ptidesOutputPortList == null) {
                        ptidesOutputPortList = new LinkedList<PtidesEvent>();
                    }

                    // modify deadline of event such that it will be output after deviceDelay
                    PtidesEvent newEvent = new PtidesEvent(event.ioPort(),
                            event.channel(), event.timeStamp(),
                            event.microstep(), event.depth(), event.token(),
                            event.receiver(), localClock.getLocalTime().add(
                                    deviceDelay), event.sourceTimestamp());

                    ptidesOutputPortList.add(newEvent);

                    _ptidesOutputPortEventQueue.put(
                            (PtidesPort) event.ioPort(), ptidesOutputPortList);
                }
                _currentLogicalTime = null;
            }
            _outputEventDeadlines.remove(getModelTime());
        }

        // Transfer all outputs from ports to the outside
        for (PtidesPort port : _ptidesOutputPortEventQueue.keySet()) {
            Queue<PtidesEvent> ptidesOutputPortList = _ptidesOutputPortEventQueue
                    .get(port);
            if (ptidesOutputPortList != null && ptidesOutputPortList.size() > 0) {
                PtidesEvent event = ptidesOutputPortList.peek();
                if (event.absoluteDeadline().equals(localClock.getLocalTime())) {
                    _currentLogicalTime = event.timeStamp();
                    _currentSourceTimestamp = event.sourceTimestamp();
                    _currentLogicalIndex = event.microstep();
                    event.ioPort().send(0, event.token());
                    _currentLogicalTime = null;
                    ptidesOutputPortList.poll();
                }
            }
        }
    }

    /** Add a pure event to the queue of pure events.
     *  @param actor Actor to fire.
     *  @param time Time the actor should be fired at.
     *  @param index Microstep the actor should be fired at.
     *  @return The time the actor requested to be refired at.
     * @exception IllegalActionException If firing of the container doesn't succeed.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int index)
            throws IllegalActionException {
        // Setting a stop time for the director calls this method
        // with the actor equal to the container.
        if (actor == this.getContainer()) {
            fireContainerAt(time);
            return time;
        }
        int newIndex = index;
        if (_currentLogicalTime != null) {
            //newIndex = _currentLogicalIndex;
            if (_currentLogicalTime.compareTo(time) == 0 && index <= getIndex()) {
                if (!(actor instanceof CompositeActor)
                        || ((CompositeActor) actor).getDirector()
                        .scheduleContainedActors()) {
                    newIndex = Math.max(getIndex(), index) + 1;
                }
            }
        }

        if (_isInitializing) {
            _currentSourceTimestamp = time;
        }

        int depth = 1;
        if (!(actor instanceof ActorExecutionAspect)) {
            depth = _getDepthOfActor(actor);
        }

        _pureEvents.put(new PtidesEvent(actor, null, time, newIndex, depth,
                _zeroTime, _currentSourceTimestamp));
        _currentSourceTimestamp = null;

        Time environmentTime = super.getEnvironmentTime();
        if (environmentTime.compareTo(time) <= 0) {
            fireContainerAt(time, newIndex);
        }
        return time;
    }

    /** Return the source timestamp of the event that is currently
     *  being processed. If no event is being processed,
     *  (i.e. event is analyzed for safe to process, actor is fired, ...) this
     *  method can return null or the timestamp of the previous event.
     *  This method should not be called if no event is currently being
     *  processed.
     * @return The current source timestamp.
     */
    public Time getCurrentSourceTimestamp() {
        return _currentSourceTimestamp;
    }

    /** Compute the deadline for an actor that requests a firing at time
     *  <i>timestamp</i>.
     *  @param actor The actor that requests firing.
     *  @param timestamp The time when the actor wants to be fired.
     *  @return The deadline for the actor.
     *  @exception IllegalActionException If time objects cannot be created.
     */
    @Override
    public Time getDeadline(NamedObj actor, Time timestamp)
            throws IllegalActionException {
        Time relativeDeadline = Time.POSITIVE_INFINITY;

        for (int i = 0; i < ((Actor) actor).outputPortList().size(); i++) {
            for (int j = 0; j < ((IOPort) ((Actor) actor).outputPortList().get(
                    i)).sinkPortList().size(); j++) {
                double newRelativeDeadline = _getRelativeDeadline((TypedIOPort) ((IOPort) ((Actor) actor)
                        .outputPortList().get(i)).sinkPortList().get(j));
                if (newRelativeDeadline < Double.MAX_VALUE
                        && newRelativeDeadline < relativeDeadline
                        .getDoubleValue()) {
                    relativeDeadline = new Time(this, newRelativeDeadline);
                }
            }
        }
        return timestamp.add(relativeDeadline);
    }

    /** Return a superdense time index for the current time,
     *  where the index is equal to the microstep.
     *  @return A superdense time index.
     *  @see #setIndex(int)
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
    @Override
    public int getIndex() {
        if (_currentLogicalTime != null) {
            return _currentLogicalIndex;
        }
        return getMicrostep();
    }

    /** Return the local time or, (i) if an actor is executing or (ii) an input
     *  token is read, (i) the timestamp of the event that caused the actor
     *  execution or (ii) the timestamp of the input event.
     *  @return The local time or the semantic time.
     */
    @Override
    public Time getModelTime() {
        if (_currentLogicalTime != null) {
            return _currentLogicalTime;
        }
        return super.getModelTime();
    }

    /** Return the current microstep or the microstep of the event, if
     *  an actor is currently executing.
     */
    @Override
    public int getMicrostep() {
        if (_currentLogicalTime != null) {
            return _currentLogicalIndex;
        }
        return super.getMicrostep();
    }

    /** Return the superdense dependency hashmap
     * This is used for the code generation in order to fill
     * the generated director hashmap.
     * @return The Superdense dependency hashmap
     */
    public Map<TypedIOPort, Map<TypedIOPort, SuperdenseDependency>> getSuperdenseDependencyPair() {
        return _superdenseDependencyPair;
    }

    /** Initialize all the actors and variables. Perform static analysis on
     *  superdense dependencies between input ports in the topology.
     *  @exception IllegalActionException If any of the methods contained
     *  in initialize() throw it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (_numberOfTokensPerPort != null) {
            _numberOfTokensPerPort.clear();
        }
        super.initialize();
    }

    /** Returns false, as this director only decorates local sources
     *  immediately contained by the PtidesDirector, thus it should
     *  not cross opaque hierarchy boundaries.
     *  @return false.
     */
    @Override
    public boolean isGlobalDecorator() {
        return true;
    }

    /** Return a new receiver of the type {@link PtidesReceiver}.
     *  @return A new PtidesReceiver.
     */
    @Override
    public Receiver newReceiver() {
        if (_debugging && _verbose) {
            _debug("Creating a new Ptides receiver.");
        }

        return new PtidesReceiver();
    }

    /** Return false if there are no more actors to be fired or the stop()
     *  method has been called.
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If the stopWhenQueueIsEmpty parameter
     *   does not contain a valid token, or refiring can not be requested.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // Do not call super.postfire() because that requests a
        // refiring at the next event time on the event queue.
        Boolean result = !_stopRequested && !_finishRequested;
        if (getModelTime().compareTo(getModelStopTime()) >= 0) {
            // If there is a still event on the event queue with time stamp
            // equal to the stop time, we want to process that event before
            // we declare that we are done.
            if (_eventQueue.size() == 0
                    || !_eventQueue.get().timeStamp()
                    .equals(getModelStopTime())) {
                result = false;
            }
        }

        // Potentially set next fire time from _outputEventQueue.
        Set<Time> deliveryTimes = _outputEventDeadlines.keySet();
        if (deliveryTimes.size() > 0) {
            TreeSet<Time> set = new TreeSet<Time>(deliveryTimes);
            for (PtidesEvent event : _outputEventDeadlines.get(set.first())) {
                if (event.ioPort() instanceof PtidesPort
                        && ((PtidesPort) event.ioPort()).isActuatorPort()
                        && getEnvironmentTime().compareTo(event.timeStamp()) > 0) {
                    handleModelError(
                            event.ioPort(),
                            new IllegalActionException(event.ioPort(),
                                    "Missed Deadline at platform time "
                                            + localClock.getLocalTime()
                                            + " with logical time "
                                            + event.timeStamp() + " at port "
                                            + event.ioPort() + "!"));
                }
            }
            _setNextFireTime(set.first());
        }
        //... or from _inputEventQueue
        deliveryTimes = _inputEventQueue.keySet();
        if (deliveryTimes.size() > 0) {
            TreeSet<Time> set = new TreeSet<Time>(deliveryTimes);
            _setNextFireTime(set.first());
        }
        // ... or from ptides output port queue
        for (PtidesPort port : _ptidesOutputPortEventQueue.keySet()) {
            Queue<PtidesEvent> ptidesOutputPortList = _ptidesOutputPortEventQueue
                    .get(port);
            if (ptidesOutputPortList != null && ptidesOutputPortList.size() > 0) {
                PtidesEvent event = ptidesOutputPortList.peek();

                if (port.isActuatorPort()
                        && getEnvironmentTime().compareTo(
                                event.absoluteDeadline()) > 0) {
                    handleModelError(event.ioPort(),
                            new IllegalActionException(port,
                                    "Missed Deadline at " + port + "!"));
                }

                _setNextFireTime(event.absoluteDeadline());
            }
        }
        // ... or could also have already been set in safeToProcess().

        // If not null, request refiring.
        if (_nextFireTime != null) {
            if (_debugging) {
                _debug("--> fire " + this.getName() + " next at "
                        + _nextFireTime.toString());
            }
            fireContainerAt(_nextFireTime, 1);
        } else {
            if (_debugging) {
                _debug("--> no next fire time");
            }
        }

        return result;
    }

    /** Override the base class to not set model time to that of the
     *  enclosing director. This method always returns true, deferring the
     *  decision about whether to fire an actor to the fire() method.
     *  @return True.
     * @exception IllegalActionException
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        Time currentTime = _consultTimeRegulators(localClock
                .getLocalTimeForCurrentEnvironmentTime());
        setModelTime(currentTime);
        if (_debugging) {
            _debug("...prefire @ " + currentTime);
        }
        setIndex(1);
        _nextFireTime = null;
        return true;
    }

    /** Call the preinitialize of the super class and create new event
     *  Queue.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _eventQueue = new PtidesListEventQueue();
        _inputEventQueue = new HashMap<Time, List<PtidesEvent>>();
        _outputEventDeadlines = new HashMap<Time, List<PtidesEvent>>();
        _ptidesOutputPortEventQueue = new HashMap<PtidesPort, Queue<PtidesEvent>>();
        _nextFireTime = Time.POSITIVE_INFINITY;
        _pureEvents = new PtidesListEventQueue();
        _currentLogicalTime = null;

        _inputPortsForPureEvent = new HashMap<TypedIOPort, Set<TypedIOPort>>();
        _relativeDeadlineForPureEvent = new HashMap<TypedIOPort, Double>();

        _calculateSuperdenseDependencies();
        _calculateDelayOffsets();
        _calculateRelativeDeadlines();
    }

    /** In DE, a warning is issued when execution aspects are used because
     *  these might change the DE semantics of the execution. In Ptides,
     *  this is not the case.
     */
    @Override
    protected void _issueExecutionAspectWarning() {
        // DO NOTHING
    }

    @Override
    public void resumeActor(NamedObj actor) throws IllegalActionException {
        prefire();
        _actorsFinished.add((Actor) actor);
        fireContainerAt(localClock.getLocalTime());
    }

    /** Override the base class to first set the container, then establish
     *  a connection with any decorated objects it finds in scope in the new
     *  container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                // The following will create the DecoratorAttributes if it does not
                // already exist, and associate it with this decorator.
                decoratedObject.getDecoratorAttributes(this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Model time is only used for correct execution of actors and the
     * scheduler will determine whether another event can be fired in
     * the current firing of the platform, so this method isn't needed.
     * By always returning true, _getNextActorToFire() will be called which
     * runs the scheduler.
     *  @return true Always.
     */
    @Override
    protected boolean _checkForNextEvent() {
        return true;
    }

    /** Put a trigger event into the event queue.
     *  <p>
     *  The trigger event has the same timestamp as that of the director.
     *  The microstep of this event is always equal to the current microstep
     *  of this director. The depth for the queued event is the
     *  depth of the destination IO port. Finally, the token and the
     *  destination receiver are also stored in the event.
     *  </p><p>
     *  If the event queue is not ready or the actor that contains the
     *  destination port is disabled, do nothing.</p>
     *
     *  @param ioPort The destination IO port.
     *  @param token The token associated with this event.
     *  @param receiver The destination receiver.
     *  @exception IllegalActionException If the time argument is not the
     *  current time, or the depth of the given IO port has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueTriggerEvent(IOPort ioPort, Token token,
            Receiver receiver) throws IllegalActionException {

        Actor actor = (Actor) ioPort.getContainer();

        if (_eventQueue == null || _disabledActors != null
                && _disabledActors.contains(actor)) {
            return;
        }
        int depth = _getDepthOfIOPort(ioPort);

        if (_debugging) {
            _debug("enqueue a trigger event for ",
                    ((NamedObj) actor).getName(), " port " + ioPort
                    + " time = " + getModelTime() + " microstep = "
                    + _microstep + " depth = " + depth);
        }

        // Register this trigger event.
        PtidesEvent newEvent = new PtidesEvent(ioPort,
                ioPort.getChannelForReceiver(receiver), getModelTime(),
                _currentLogicalIndex, depth, token, receiver,
                _currentSourceTimestamp);

        if (ioPort.isOutput()) {

            Time deliveryTime;
            deliveryTime = localClock.getLocalTime();
            if (((PtidesPort) ioPort).isActuatorPort()) {
                if (((PtidesPort) ioPort).actuateAtEventTimestamp()) {
                    deliveryTime = getModelTime().subtract(
                            _getDoubleParameterValue(ioPort, "deviceDelay"));
                }

                if (getModelTime().compareTo(deliveryTime) < 0) {
                    newEvent = _handleTimingError(
                            (PtidesPort) ioPort,
                            newEvent,
                            "Missed Deadline at "
                                    + ioPort
                                    + "!\n "
                                    + " At "
                                    + getModelTime()
                                    + " which is smaller than current platform time "
                                    + localClock.getLocalTime());
                }
            } else if (((PtidesPort) ioPort).isNetworkTransmitterPort()) {
                if (localClock.getLocalTime().subtract(getModelTime())
                        .getDoubleValue() > _getDoubleParameterValue(ioPort,
                                "platformDelayBound")) {
                    newEvent = _handleTimingError(
                            (PtidesPort) ioPort,
                            newEvent,
                            "Token is being sent out onto the network too late."
                                    + "Current platform time: "
                                    + localClock.getLocalTime()
                                    + " Event timestamp: "
                                    + getModelTime()
                                    + " Platform delay: "
                                    + _getDoubleParameterValue(ioPort,
                                            "platformDelayBound"));
                }
            }

            if (newEvent != null) {
                List<PtidesEvent> list = _outputEventDeadlines
                        .get(deliveryTime);
                if (list == null) {
                    list = new ArrayList<PtidesEvent>();
                }
                list.add(newEvent);
                _outputEventDeadlines.put(deliveryTime, list);
                if (_debugging) {
                    _debug("  enqueue actuator event for time " + deliveryTime);
                }
            }
        } else {
            _eventQueue.put(newEvent);
            if (_numberOfTokensPerPort == null) {
                _numberOfTokensPerPort = new HashMap<IOPort, Integer>();
            }
            IOPort port = newEvent.ioPort();
            if (port != null) {
                if (_numberOfTokensPerPort == null) {
                    _numberOfTokensPerPort = new HashMap<IOPort, Integer>();
                }
                Integer numberofTokens = _numberOfTokensPerPort.get(port);
                if (numberofTokens == null) {
                    numberofTokens = 0;
                }
                _numberOfTokensPerPort.put(port, numberofTokens + 1);
            }
        }
    }

    /** Return the value stored in a parameter associated with
     *  the NamedObj.
     *  @param object The object that has the parameter.
     *  @param parameterName The name of the parameter to be retrieved.
     *  @return the value of the named parameter if the parameter is not
     *  null. Otherwise return null.
     *  @exception IllegalActionException If thrown while getting the value
     *  of the parameter.
     */
    protected static Double _getDoubleParameterValue(NamedObj object,
            String parameterName) throws IllegalActionException {
        Parameter parameter = (Parameter) object.getAttribute(parameterName);
        if (parameter != null && parameter.getToken() != null) {
            return Double.valueOf(((DoubleToken) parameter.getToken())
                    .doubleValue());
        }
        return null;
    }

    /** Return the value stored in a parameter associated with
     *  the NamedObj.
     *  @param object The object that has the parameter.
     *  @param parameterName The name of the parameter to be retrieved.
     *  @return the value of the named parameter if the parameter is not
     *  null. Otherwise return null.
     *  @exception IllegalActionException If thrown while getting the value
     *  of the parameter.
     */
    protected static Integer _getIntParameterValue(NamedObj object,
            String parameterName) throws IllegalActionException {
        Parameter parameter = (Parameter) object.getAttribute(parameterName);
        if (parameter != null && parameter.getToken() != null) {
            return Integer
                    .valueOf(((IntToken) parameter.getToken()).intValue());
        }
        return null;
    }

    /** Return the actor to fire in this iteration, or null if no actor should
     * be fired. Since _checkForNextEvent() always
     * returns true, this method will keep being called until it returns null.
     * @exception IllegalActionException If _isSafeToProcess() throws it.
     */
    @Override
    protected Actor _getNextActorToFire() throws IllegalActionException {
        Actor actor = _getNextActorFrom(_pureEvents);
        if (actor != null) {
            return actor;
        }
        actor = _getNextActorFrom(_eventQueue);
        if (actor != null) {
            return actor;
        }
        _currentLogicalTime = null;
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** List of all input ports in the model (actuator and network transmitter
     * ports are also considered input ports).
     */
    protected List<TypedIOPort> _inputPorts;

    /** Map an input port to a set which is its input port group. */
    protected Map<TypedIOPort, Set<TypedIOPort>> _inputPortGroups;

    /** The earliest time this director should be refired. */
    protected Time _nextFireTime;

    /** Store the superdense dependency between pairs of input ports using
     * nested Maps. Providing the source input as a key will return a Map
     * value, where the destination input port can be used as a key to return
     * the superdense dependency.
     */
    protected Map<TypedIOPort, Map<TypedIOPort, SuperdenseDependency>> _superdenseDependencyPair;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add input port to list.
     * @param inputPort
     */
    private void _addInputPort(TypedIOPort inputPort) {

        // Initialize nested HashMaps.
        _superdenseDependencyPair.put(inputPort,
                new HashMap<TypedIOPort, SuperdenseDependency>());

        // Add input port to list.
        _inputPorts.add(inputPort);

        // Initialize input port groups.
        _inputPortGroups.put(inputPort, new HashSet<TypedIOPort>());
        ;

        // Set dependency with self.
        _putSuperdenseDependencyPair(inputPort, inputPort,
                SuperdenseDependency.OTIMES_IDENTITY);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate the delay offset for each input port.
     * The delay offset is used in the safe-to-process analysis
     * to know when no future events can occur at a sensor or network
     * receiver port that can result in an event arriving at an input port
     * with an earlier timestamp than the event currently there.
     * @exception IllegalActionException If cannot set 'delayOffset' parameter
     * for an input port.
     */
    private void _calculateDelayOffsets() throws IllegalActionException {

        // find all local source actors
        List<Actor> localSourceActors = new ArrayList<Actor>();
        for (Object entity : ((CompositeActor) getContainer()).entityList()) {
            if (entity instanceof Actor) {
                Actor actor = (Actor) entity;
                boolean isSource = true;
                for (Object inputPortObject : actor.inputPortList()) {
                    if (((IOPort) inputPortObject).sourcePortList().size() > 0) {
                        isSource = false;
                        break;
                    }
                }
                if (isSource) {
                    localSourceActors.add(actor);
                }
            }
        }

        // Calculate delayOffset to each input port.
        for (TypedIOPort port : _inputPorts) {

            // Disallow SensorPort and NetworkReceiverPort.
            if (port instanceof PtidesPort
                    && (((PtidesPort) port).isSensorPort() || ((PtidesPort) port)
                            .isNetworkReceiverPort())) {
                continue;
            }

            // Find minimum delay offset from all sensor or network receiver
            // input ports to the input port group of this port.
            double delayOffset = Double.POSITIVE_INFINITY;
            for (TypedIOPort inputPort : _inputPorts) {
                // Only allow SensorPort and NetworkReceiverPort.
                if (!(inputPort instanceof PtidesPort && (((PtidesPort) inputPort)
                        .isSensorPort() || ((PtidesPort) inputPort)
                        .isNetworkReceiverPort()))) {
                    continue;
                }
                double deviceDelayBound = _getDoubleParameterValue(inputPort,
                        "deviceDelayBound");
                if (((PtidesPort) inputPort).isNetworkReceiverPort()) {
                    deviceDelayBound += _getDoubleParameterValue(inputPort,
                            "networkDelayBound");
                    deviceDelayBound += _getDoubleParameterValue(inputPort,
                            "sourcePlatformDelayBound");
                }
                SuperdenseDependency minDelay = SuperdenseDependency.OPLUS_IDENTITY;
                // Find minimum path to input port group.
                for (TypedIOPort groupPort : _inputPortGroups.get(port)) {
                    minDelay = (SuperdenseDependency) minDelay
                            .oPlus(_getSuperdenseDependencyPair(inputPort,
                                    groupPort));
                }

                // Check if best so far.
                double thisDelayOffset = minDelay.timeValue()
                        - deviceDelayBound;
                if (thisDelayOffset < delayOffset) {
                    delayOffset = thisDelayOffset;
                }
            }

            for (Actor localSource : localSourceActors) {
                Double delayOffsetAtSource = null;
                ThrottleAttributes attributes = (ThrottleAttributes) ((NamedObj) localSource)
                        .getDecoratorAttributes(this);
                if (attributes != null
                        && ((BooleanToken) attributes.useMaximumLookaheadTime
                                .getToken()).booleanValue()) {
                    delayOffsetAtSource = ((DoubleToken) attributes.maximumLookaheadTime
                            .getToken()).doubleValue();
                }

                SuperdenseDependency sourceDelay = SuperdenseDependency.OPLUS_IDENTITY;
                if (delayOffsetAtSource != null) {
                    sourceDelay = SuperdenseDependency.valueOf(
                            delayOffsetAtSource, 1);
                }
                SuperdenseDependency minDelay = SuperdenseDependency.OPLUS_IDENTITY;
                // Find minimum path to input port group.
                for (TypedIOPort groupPort : _inputPortGroups.get(port)) {
                    for (Object lsOutput : localSource.outputPortList()) {
                        IOPort outputPort = (IOPort) lsOutput;
                        for (IOPort sinkPort : outputPort.sinkPortList()) {
                            SuperdenseDependency dependency = _getSuperdenseDependencyPair(
                                    (TypedIOPort) sinkPort, groupPort);
                            if (!dependency
                                    .equals(SuperdenseDependency.OPLUS_IDENTITY)) {
                                minDelay = (SuperdenseDependency) minDelay
                                        .oPlus(dependency);
                            }
                        }
                    }
                }

                // Check if best so far.
                minDelay = (SuperdenseDependency) sourceDelay.oTimes(minDelay);
                double thisDelayOffset = minDelay.timeValue();
                if (thisDelayOffset < delayOffset) {
                    delayOffset = thisDelayOffset;
                }
            }

            _setDelayOffset(
                    port,
                    delayOffset
                    - _clockSynchronizationErrorBound.getDoubleValue());
        }

        // Calculate delayOffset to each actor
        for (Object entity : ((CompositeActor) getContainer()).entityList()) {
            if (entity instanceof TimeDelay
                    && ((TimeDelay) entity).delay.getPort()
                    .isOutsideConnected()) {
                _setDelayOffset((NamedObj) entity,
                        ((DoubleToken) ((TimeDelay) entity).minimumDelay
                                .getToken()).doubleValue());
            }
            if (entity instanceof CompositeActor
                    && ((CompositeActor) entity).getDirector() instanceof PeriodicDirector) {
                // TODO calculate delayOffset
                double delay = _calculateSRDelay((CompositeActor) entity);
                _setDelayOffset((NamedObj) entity, delay);
            }
        }
    }

    private double _calculateSRDelay(CompositeActor composite)
            throws IllegalActionException {
        double minDelay = Double.POSITIVE_INFINITY;
        double delay = 0.0;
        for (Object inputPort : composite.inputPortList()) {
            for (Object insidePort : ((IOPort) inputPort).deepInsidePortList()) {
                if (((IOPort) insidePort).isOutput()) {
                    if (delay < minDelay) {
                        minDelay = delay;
                    }
                } else {
                    Actor actor = ((Actor) ((IOPort) insidePort).getContainer());
                    List<Actor> visited = new ArrayList();
                    delay = _getDelay(composite, actor, (IOPort) insidePort,
                            minDelay, visited);
                }
            }
        }
        return delay;
    }

    private double _getDelay(CompositeActor composite, Actor actor,
            IOPort port, double minDelay, List<Actor> visited)
                    throws IllegalActionException {
        if (visited.contains(actor)) {
            // found loop
            return 0.0;
        }
        visited.add(actor);
        double delay = 0.0;
        CausalityInterface causalityInterface = actor.getCausalityInterface();
        for (Object outputPort : actor.outputPortList()) {
            SuperdenseDependency dependency = (SuperdenseDependency) causalityInterface
                    .getDependency(port, (IOPort) outputPort);
            delay += dependency.timeValue();
            for (Object connectedPort : ((IOPort) outputPort)
                    .connectedPortList()) {
                if (composite.outputPortList().contains(connectedPort)) {
                    if (delay < minDelay) {
                        minDelay = delay;
                    }
                } else {
                    Actor downstreamActor = (Actor) ((IOPort) connectedPort)
                            .getContainer();
                    delay += _getDelay(composite, downstreamActor,
                            (IOPort) connectedPort, minDelay, visited);
                    if (delay < minDelay) {
                        minDelay = delay;
                    }
                }
            }
        }
        return minDelay;
    }

    /** Calculate the relative deadline for each input port. The relative
     * deadline is used along with the timestamp of the event at the input port
     * to determine the earliest time that this event may cause for an event
     * that needs to be output at an actuator or network transmitter.
     * @exception IllegalActionException If cannot set 'relativeDeadline'
     * parameter or cannot get device delay bound.
     */
    private void _calculateRelativeDeadlines() throws IllegalActionException {

        // Calculate relativeDeadline for each input port.
        for (TypedIOPort port : _inputPorts) {

            // Disallow SensorPort and NetworkReceiverPort.
            if (port instanceof PtidesPort
                    && (((PtidesPort) port).isSensorPort() || ((PtidesPort) port)
                            .isNetworkReceiverPort())) {
                continue;
            }

            // Find minimum model time delay path from the input
            // port to any actuator or network transmitter.
            double relativeDeadline = Double.POSITIVE_INFINITY;
            for (TypedIOPort outputPort : _inputPorts) {
                // Only allow ActuatorPort and NetworkTransmitterPort.
                if (!(outputPort instanceof PtidesPort && (((PtidesPort) outputPort)
                        .isActuatorPort() || ((PtidesPort) outputPort)
                        .isNetworkTransmitterPort()))) {
                    continue;
                }
                double deviceDelayBound = _getDoubleParameterValue(outputPort,
                        "deviceDelayBound");
                SuperdenseDependency minDelay = _getSuperdenseDependencyPair(
                        port, outputPort);

                // Check if best so far.
                double thisRelativeDeadline = minDelay.timeValue()
                        - deviceDelayBound;
                if (thisRelativeDeadline < relativeDeadline) {
                    relativeDeadline = thisRelativeDeadline;
                }
            }
            _setRelativeDeadline(port, relativeDeadline);
        }

        // Set relative deadlines for pure events.
        // FIXME: may need to be modified to handle pure events which update
        // state.
        for (TypedIOPort port : _inputPortsForPureEvent.keySet()) {
            Double relativeDeadline = Double.POSITIVE_INFINITY;
            for (TypedIOPort connectedPort : _inputPortsForPureEvent.get(port)) {
                Double thisRelativeDeadline = _getRelativeDeadline(connectedPort);
                if (thisRelativeDeadline.compareTo(relativeDeadline) < 0) {
                    relativeDeadline = thisRelativeDeadline;
                }
            }
            _relativeDeadlineForPureEvent.put(port, relativeDeadline);
        }
    }

    /** Calculate the superdense dependency (minimum model time delay) between
     * all source and destination input ports. The Floyd-Warshall algorithm is
     * used to calculate the minimum model time delay paths.
     * @exception IllegalActionException If the container is not a
     * TypedCompositeActor.
     * Note: This algorithm assumes all channels have same dependency as multiport.
     */
    private void _calculateSuperdenseDependencies()
            throws IllegalActionException {

        //TODO: Code assumes code generation is at atomic actor level, so if
        // code generation is modified to cluster atomic actors (to reduce
        // execution overhead) this method will need to be modified.
        // Code generation would also need to handle multiports differently.

        if (!(getContainer() instanceof TypedCompositeActor)) {
            throw new IllegalActionException(getContainer(), getContainer()
                    .getFullName() + " is not a TypedCompositeActor");
        }

        // Initialize HashMaps. These will end up being identical if parameter
        // 'considerTriggerPorts' is false.
        _superdenseDependencyPair = new HashMap<TypedIOPort, Map<TypedIOPort, SuperdenseDependency>>();

        // Create a list for all input ports. A List is needed since Set does
        // not make any guarantees on iteration order.
        _inputPorts = new ArrayList<TypedIOPort>();

        // Store input port groups for all input ports.
        _inputPortGroups = new HashMap<TypedIOPort, Set<TypedIOPort>>();

        // Find all input ports (consider actuator and network transmitter
        // ports as input ports as well) and add connections to other inputs.
        // This will build a weighted directed graph.

        // Add sensor, actuator, and network ports.
        for (TypedIOPort port : (List<TypedIOPort>) ((TypedCompositeActor) getContainer())
                .portList()) {
            if (port instanceof ParameterPort) {
                continue;
            }

            // Only allow ports which are PtidesPorts.
            if (!(port instanceof PtidesPort)) {
                throw new IllegalActionException(port, port.getFullName()
                        + " is not a PtidesPort");
            }

            _addInputPort(port);

            // Add path from sensor or network input port to connected
            // input ports. These connections have a weight of 0.
            if (((PtidesPort) port).isSensorPort()
                    || ((PtidesPort) port).isNetworkReceiverPort()) {

                for (IOPort connectedPort : port.insideSinkPortList()) {
                    _putSuperdenseDependencyPair(port,
                            (TypedIOPort) connectedPort,
                            SuperdenseDependency.OTIMES_IDENTITY);
                }
            }
        }

        // Calculate superdense dependency from each input port of an
        // actor to the input ports of immediate predecessor actors (or
        // actuators or network transmitters) using causality interface
        // of the actor.
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) getContainer())
                .deepEntityList()) {

            CausalityInterface actorCausality = actor.getCausalityInterface();

            for (TypedIOPort inputPort : (List<TypedIOPort>) actor
                    .inputPortList()) {

                // Ignore input if it's not connected to anything.
                if (!inputPort.isOutsideConnected()) {
                    continue;
                }

                _addInputPort(inputPort);

                for (TypedIOPort outputPort : (List<TypedIOPort>) actor
                        .outputPortList()) {
                    // Get superdense dependency between input port and output
                    // port of current actor.
                    SuperdenseDependency minDelay = (SuperdenseDependency) actorCausality
                            .getDependency(inputPort, outputPort);
                    // Only if dependency exists...
                    if (!minDelay.equals(SuperdenseDependency.OPLUS_IDENTITY)) {
                        // Set input port pair for all connected ports.
                        // Assumes no delay from connections.
                        // Add connected input ports if this input port can
                        // produce pure events.
                        if (!minDelay
                                .equals(SuperdenseDependency.OTIMES_IDENTITY)) {
                            if (!_inputPortsForPureEvent.containsKey(inputPort)) {
                                _inputPortsForPureEvent.put(inputPort,
                                        new HashSet<TypedIOPort>());
                            }
                            _inputPortsForPureEvent.get(inputPort).addAll(
                                    outputPort.deepConnectedPortList());
                        }
                        for (TypedIOPort connectedPort : (List<TypedIOPort>) outputPort
                                .deepConnectedPortList()) {
                            _putSuperdenseDependencyPair(inputPort,
                                    connectedPort, minDelay);
                        }
                        // Find input port group.
                        for (TypedIOPort inPort : (List<TypedIOPort>) actor
                                .inputPortList()) {
                            minDelay = (SuperdenseDependency) actorCausality
                                    .getDependency(inPort, outputPort);
                            if (!minDelay
                                    .equals(SuperdenseDependency.OPLUS_IDENTITY)) {
                                _inputPortGroups.get(inputPort).add(inPort);
                            }
                        }
                    }
                }
            }
        }

        // Floyd-Warshall algorithm. This finds the minimum model time delay
        // between all input ports.
        for (TypedIOPort k : _inputPorts) {
            for (TypedIOPort i : _inputPorts) {
                for (TypedIOPort j : _inputPorts) {
                    SuperdenseDependency ij, ik, kj;
                    // All input ports.
                    ij = _getSuperdenseDependencyPair(i, j);
                    ik = _getSuperdenseDependencyPair(i, k);
                    kj = _getSuperdenseDependencyPair(k, j);
                    // Check if i->k->j is better than i->j.
                    if (ij.compareTo(ik.oTimes(kj)) == Dependency.GREATER_THAN) {
                        _putSuperdenseDependencyPair(i, j,
                                (SuperdenseDependency) ik.oTimes(kj));
                    }
                }
            }
        }

        // Print debug table.
        if (_debugging) {
            StringBuffer buf = new StringBuffer();
            buf.append("\t");
            for (TypedIOPort srcPort : _inputPorts) {
                buf.append(srcPort.getName(getContainer()) + "\t");
            }
            _debug(buf.toString());
            for (TypedIOPort srcPort : _inputPorts) {
                buf = new StringBuffer();
                buf.append(srcPort.getName(getContainer()) + "\t");
                for (TypedIOPort destPort : _inputPorts) {
                    buf.append(_getSuperdenseDependencyPair(srcPort, destPort)
                            .timeValue()
                            + "("
                            + _getSuperdenseDependencyPair(srcPort, destPort)
                            .indexValue() + ")\t");
                }
                _debug(buf.toString());
            }

            for (Object actor : ((CompositeActor) getContainer()).entityList()) {
                if (actor instanceof Actor) {
                    _debug(((Actor) actor).getName() + "\t"
                            + _getDepthOfActor((Actor) actor));
                }
            }
        }
    }

    /** Get the next actor that can be fired from a specified event queue.
     *  Check whether the event is safe to process, the actors prefire
     *  returns true and the event can be scheduled. Because Ptides does
     *  not store tokens in receivers but keeps them in the event until
     *  the actor is really fired, we have to temporarily put tokens into
     *  receivers and then remove them in order for the prefire to give
     *  correct results.
     *  @param queue The event queue.
     *  @return The next actor to fire or null.
     *  @exception IllegalActionException Thrown by safeToProcess, prefire
     *    or schedule.
     */
    private Actor _getNextActorFrom(DEEventQueue queue)
            throws IllegalActionException {
        Object[] eventArray = queue.toArray();
        for (Object event : eventArray) {
            if (_isSafeToProcess((PtidesEvent) event)) {
                PtidesEvent ptidesEvent = (PtidesEvent) event;
                Actor actor = ptidesEvent.actor();
                Time timestamp = ptidesEvent.timeStamp();

                // Check if actor can be fired by putting token into receiver
                // and calling prefire.

                // if this is a pure event but there is an event in the
                // trigger events with a smaller timestamp, pick that one

                if (queue == _pureEvents) {
                    for (Object triggeredEventObject : _eventQueue.toArray()) {
                        PtidesEvent triggeredEvent = (PtidesEvent) triggeredEventObject;
                        if (triggeredEvent.actor() == actor
                                && triggeredEvent.timeStamp().compareTo(
                                        timestamp) < 0) {
                            ptidesEvent = triggeredEvent;
                        }
                    }
                }
                actor = ptidesEvent.actor();
                timestamp = ptidesEvent.timeStamp();

                List<PtidesEvent> sameTagEvents = new ArrayList<PtidesEvent>();
                int i = 0;
                while (i < queue.size()) {
                    PtidesEvent eventInQueue = ((PtidesListEventQueue) queue)
                            .get(i);
                    // If event has same tag and destined to same actor, remove from
                    // queue.
                    if (eventInQueue.actor().equals(actor) && 
                            eventInQueue.hasTheSameTagAs(ptidesEvent)) {
                        sameTagEvents.add(eventInQueue);
                        if (eventInQueue.receiver() != null) {
                            if (eventInQueue.receiver() instanceof PtidesReceiver) {
                                ((PtidesReceiver) eventInQueue.receiver())
                                .putToReceiver(eventInQueue.token());
                            }
                        }
                    }
                    i++;
                }

                _currentLogicalTime = timestamp;
                _currentLogicalIndex = ptidesEvent.microstep();
                _currentSourceTimestamp = ptidesEvent.sourceTimestamp();
                boolean prefire = actor.prefire();
                _currentLogicalTime = null;

                // Remove tokens again.
                for (PtidesEvent sameTagEvent : sameTagEvents) {
                    if (sameTagEvent.receiver() != null) {
                        if (sameTagEvent.receiver() instanceof PtidesReceiver) {
                            ((PtidesReceiver) sameTagEvent.receiver())
                            .remove(sameTagEvent.token());
                        }
                    }
                }
                if (prefire
                        && (
                                // There are no resource schedulers that need to be asked.
                                !_aspectsPresent ||
                                // There are resource schedulers that need to be asked. however,
                                // because the timeDelay actor is fired twice, we only schedule
                                // one firing on the resource scheduler: the second firing due to
                                // the pure event.
                                ((actor instanceof TimeDelay && !ptidesEvent
                                        .isPureEvent()) ||
                                        // Actor was previously scheduled and just finished.
                                        _actorsFinished.contains(actor) ||
                                        // The actor is scheduled and is instantaneously granted all
                                        // resources.
                                        _schedule((NamedObj) actor, timestamp))
                                        && (
                                                // If actor is a composite actor we check whether the
                                                // contained actors can be scheduled.
                                                !(actor instanceof CompositeActor) || ((CompositeActor) actor)
                                                .getDirector()
                                                .scheduleContainedActors()))) {
                    _currentLogicalTime = timestamp;
                    _currentLogicalIndex = ptidesEvent.microstep();
                    _currentSourceTimestamp = ptidesEvent.sourceTimestamp();

                    // remove all events with same tag from all queues.
                    _removeEventsFromQueue(_eventQueue, ptidesEvent);
                    _removeEventsFromQueue(_pureEvents, ptidesEvent);
                    _actorsFinished.remove(actor);
                    if (_debugging) {
                        _debug(">>> next actor: " + actor + " @ " + timestamp);
                    }
                    return actor;
                }
            }
        }
        if (_debugging) {
            _debug("--###--- no next actor");
        }
        return null;
    }

    private int _getNumberOfFutureEventsFrom(Actor actor) {
        int maxEvents = 0;
        // Find all sink actors.
        for (Object object : actor.outputPortList()) {
            IOPort port = (IOPort) object;
            for (Object sinkPort : port.sinkPortList()) {
                if (((IOPort) sinkPort).getContainer() == this.getContainer()) {
                    for (Time time : _outputEventDeadlines.keySet()) {
                        maxEvents += _outputEventDeadlines.get(time).size();
                    }
                } else {
                    if (_numberOfTokensPerPort == null) {
                        _numberOfTokensPerPort = new HashMap<IOPort, Integer>();
                    }
                    Integer numberOfTokens = _numberOfTokensPerPort
                            .get(sinkPort);
                    if (numberOfTokens == null) {
                        numberOfTokens = 0;
                    }
                    maxEvents += numberOfTokens;
                }
            }
        }
        return maxEvents;
    }

    /** Return the value of the 'relativeDeadline' parameter for an input
     * port or the maximum double value if no parameter is found.
     * @param port Input port.
     * @return Relative Deadline of input port.
     * @exception IllegalActionException If cannot read parameter.
     */
    private Double _getRelativeDeadline(TypedIOPort port)
            throws IllegalActionException {
        Parameter parameter = (Parameter) port.getAttribute("relativeDeadline");
        if (parameter != null) {
            return ((DoubleToken) parameter.getToken()).doubleValue();
        } else {
            return Double.MAX_VALUE;
        }
    }

    /** Return the superdense dependency between a source and a destination
     * input port. If the mapping does not exist, it is assumed to be
     * SuperdenseDependency.OPLUS_IDENTITY.
     * @param source Source input port.
     * @param destination Destination input port.
     * @return The Superdense dependency.
     */
    private SuperdenseDependency _getSuperdenseDependencyPair(
            TypedIOPort source, TypedIOPort destination) {
        if (_superdenseDependencyPair.containsKey(source)
                && _superdenseDependencyPair.get(source).containsKey(
                        destination)) {
            return _superdenseDependencyPair.get(source).get(destination);
        } else {
            return SuperdenseDependency.OPLUS_IDENTITY;
        }
    }

    /** Handle timing error on a PtidesPort. This method simply throws an exception.
     * @param port The port where the error occurred.
     * @param event The event that caused the error; i.e. that arrived too late or out of order.
     * @param message The error message.
     * @return A new PtidesEvent that can be safely processed or null if no event should be processed.
     * @exception IllegalActionException If error handling actor throws this.
     */
    private PtidesEvent _handleTimingError(PtidesPort port, PtidesEvent event,
            String message) throws IllegalActionException {
        throw new IllegalActionException(port, message);
    }

    private boolean _isLocalSource(Object target) {
        if (target instanceof DiscreteClock || target instanceof PoissonClock) {
            return true;
        }
        if (target instanceof ModalModel) {
            return true;
        }
        if (target instanceof CompositeActor
                && ((CompositeActor) target).isOpaque()) {
            if (((CompositeActor) target).inputPortList().size() == 0
                    || ((CompositeActor) target).getDirector() instanceof PeriodicDirector) {
                return true;
            }
            for (Object entity : ((CompositeActor) target)
                    .allAtomicEntityList()) {
                if (entity instanceof DiscreteClock
                        || entity instanceof PoissonClock) {
                    return true;
                }
            }

        }
        return false;
    }

    /** Check if event is safe to process.
     * @param event The event to be checked.
     * @return true if the event is safe to process.
     * @exception IllegalActionException If the delayOffset aparameter
     * cannot be read.
     */
    private boolean _isSafeToProcess(PtidesEvent event)
            throws IllegalActionException {
        // resource scheduler events are only safe to process when physical time
        // equals event timestamp.
        if (event.actor() instanceof ActorExecutionAspect) {
            if ((event.timeStamp().compareTo(localClock.getLocalTime())) > 0) {
                if (_debugging) {
                    _debug("*** resourceScheduler !safe" + event);
                }
                return false;
            }
        }

        // Check if there are any events upstream that have to be
        // processed before this one.
        Object[] eventArray = _eventQueue.toArray();
        for (Object object : eventArray) {
            PtidesEvent ptidesEvent = (PtidesEvent) object;
            if (event.timeStamp().compareTo(ptidesEvent.timeStamp()) > 0) {
                break;
            }
            if (ptidesEvent.actor() != event.actor()
                    && ptidesEvent.ioPort() != null && event.ioPort() != null) {
                SuperdenseDependency minDelay = _getSuperdenseDependencyPair(
                        (TypedIOPort) ptidesEvent.ioPort(),
                        (TypedIOPort) event.ioPort());
                if (event.timeStamp().getDoubleValue()
                        - ptidesEvent.timeStamp().getDoubleValue() >= minDelay
                        .timeValue()) {
                    if (_debugging) {
                        _debug("*** upstream !safe" + event);
                    }
                    return false;
                }
            }
        }

        // Throttling actors with maximum future events parameter.
        ThrottleAttributes attributes = (ThrottleAttributes) ((NamedObj) event
                .actor()).getDecoratorAttributes(this);
        if (attributes != null
                && ((BooleanToken) attributes.useMaximumFutureEvents.getToken())
                .booleanValue()) {
            Integer maxFutureEvents = ((IntToken) attributes.maximumFutureEvents
                    .getToken()).intValue();

            if (maxFutureEvents != null) {
                int futureEvents = _getNumberOfFutureEventsFrom(event.actor());
                if (_debugging) {
                    _debug("*** safe?" + (futureEvents <= maxFutureEvents)
                            + " " + event);
                }
                return (futureEvents <= maxFutureEvents);
            }
        }

        Double delayOffset = null;
        Time eventTimestamp = event.timeStamp();
        IOPort port = event.ioPort();

        if (port != null) {
            Actor actor = (Actor) port.getContainer();
            for (int i = 0; i < actor.inputPortList().size(); i++) {
                Object ioPort = actor.inputPortList().get(i);
                Parameter parameter = (Parameter) ((NamedObj) ioPort)
                        .getAttribute("delayOffset");
                Double ioPortDelayOffset = null;
                if (parameter != null) {
                    Token token = parameter.getToken();
                    if (token instanceof DoubleToken) {
                        ioPortDelayOffset = ((DoubleToken) token).doubleValue();
                    } else if (token instanceof ArrayToken) {
                        ioPortDelayOffset = ((DoubleToken) ((ArrayToken) token)
                                .getElement(0)).doubleValue();
                    }
                }
                if (ioPortDelayOffset != null
                        && (delayOffset == null || ioPortDelayOffset < delayOffset)) {
                    delayOffset = ioPortDelayOffset;
                }
            }
        } else {
            attributes = (ThrottleAttributes) ((NamedObj) event.actor())
                    .getDecoratorAttributes(this);
            if (attributes != null) {
                if (((BooleanToken) attributes.useMaximumLookaheadTime
                        .getToken()).booleanValue()) {
                    delayOffset = Double
                            .valueOf(((DoubleToken) attributes.maximumLookaheadTime
                                    .getToken()).doubleValue());
                }
            }
            if (((NamedObj) event.actor()).getAttribute("delayOffset") != null) {
                delayOffset = ((DoubleToken) ((Parameter) ((NamedObj) event
                        .actor()).getAttribute("delayOffset")).getToken())
                        .doubleValue();
            }

        }
        if (delayOffset == null
                || localClock
                .getLocalTime()
                .compareTo(eventTimestamp.subtract(delayOffset)/*.subtract(
                                                                               _clockSynchronizationErrorBound)*/) >= 0) {

            // Default throttling actors.
            if (_autoThrottling) {
                int futureEvents = _getNumberOfFutureEventsFrom(event.actor());
                if (futureEvents > _maxNumberOfFutureEvents) {
                    if (_debugging) {
                        _debug("*** throttling futureEvents !safe" + event);
                    }
                    return false;
                }
            }
            if (_debugging) {
                _debug("*** safe" + event);
            }
            return true;
        }

        _setNextFireTime(eventTimestamp.subtract(delayOffset));
        if (_debugging) {
            _debug("*** delayOffset !safe" + event);
        }
        return false;
    }

    /** Store the superdense dependency between a source and destination input
     * port. If the mapping does not exist, it is assumed to be
     * SuperdenseDependency.OPLUS_IDENTITY.
     * @param source Source input port.
     * @param destination Destination input port.
     * @param dependency Superdense dependency.
     */
    private void _putSuperdenseDependencyPair(TypedIOPort source,
            TypedIOPort destination, SuperdenseDependency dependency) {
        if (!dependency.equals(SuperdenseDependency.OPLUS_IDENTITY)) {
            _superdenseDependencyPair.get(source).put(destination, dependency);
        }
    }

    /** Remove all events with the same tag and at the same actor from the
     * event queue.
     * @param event The event.
     * @return A list of all events with same tag and at the same actor as the
     * event.
     */
    private List<PtidesEvent> _removeEventsFromQueue(DEEventQueue queue,
            PtidesEvent event) {
        List<PtidesEvent> eventList = new ArrayList<PtidesEvent>();
        int i = 0;
        while (i < queue.size()) {
            PtidesEvent eventInQueue = ((PtidesListEventQueue) queue).get(i);
            // If event has same tag and destined to same actor, remove from
            // queue.
            if (eventInQueue.actor().equals(event.actor()) &&
                    eventInQueue.hasTheSameTagAs(event)) {
                eventList.add(eventInQueue);
                IOPort port = eventInQueue.ioPort();
                if (port != null) {
                    _numberOfTokensPerPort.put(port,
                            _numberOfTokensPerPort.get(port) - 1);
                }
                ((PtidesListEventQueue) queue).take(i);
                continue;
            }
            i++;
        }
        return eventList;
    }

    /** Set the value of the 'delayOffset' parameter for a NamedObj.
     * @param namedObj The NamedObj to have the parameter set.
     * @param delayOffset Delay offset for safe-to-process analysis.
     * @exception IllegalActionException If cannot set parameter.
     */
    private void _setDelayOffset(NamedObj namedObj, Double delayOffset)
            throws IllegalActionException {
        DoubleToken token = new DoubleToken(delayOffset);
        Parameter parameter = (Parameter) namedObj.getAttribute("delayOffset");
        if (parameter == null) {
            try {
                parameter = new Parameter(namedObj, "delayOffset", token);
                parameter.setPersistent(false);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(namedObj,
                        "delayOffset parameter already exists at "
                                + namedObj.getFullName() + ".");
            }
        } else {
            parameter.setToken(token);
        }

    }

    /** Set the next time to fire the director to the provided time if it is earlier than
     * the currently set next fire time.
     * @param time The next fire time.
     */
    private void _setNextFireTime(Time time) {
        if (_nextFireTime == null) {
            _nextFireTime = time;
        } else if (_nextFireTime.compareTo(time) > 0) {
            _nextFireTime = time;
        }
    }

    /** Set the value of the 'relativeDeadline' parameter for an input port.
     * @param port Input port.
     * @param relativeDeadline Relative deadline for input port.
     * @exception IllegalActionException If cannot set parameter.
     */
    private void _setRelativeDeadline(TypedIOPort port, Double relativeDeadline)
            throws IllegalActionException {
        DoubleToken token = new DoubleToken(relativeDeadline);
        Parameter parameter = (Parameter) port.getAttribute("relativeDeadline");
        if (parameter == null) {
            try {
                // Findbugs: avoid a dead local store here.
                /* parameter = */new Parameter(port, "relativeDeadline", token);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(port,
                        "relativeDeadline parameter already exists at "
                                + port.getFullName() + ".");
            }
        } else {
            parameter.setToken(token);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _autoThrottling;
    private Time _clockSynchronizationErrorBound;
    private Time _currentLogicalTime;
    private Time _currentSourceTimestamp;
    private int _currentLogicalIndex;

    private HashMap<Time, List<PtidesEvent>> _inputEventQueue;

    /** Connected input ports for an input port which may produce a pure
     * event. Used to calculate _relativeDeadlineForPureEvent.
     */
    private Map<TypedIOPort, Set<TypedIOPort>> _inputPortsForPureEvent;

    /** Store number of tokens per port. This is used for throttling local sources.
     *  The number could also be retrieved from the event queue but iterating the
     *  entire event queue for every event would be too time consuming.
     */
    private Map<IOPort, Integer> _numberOfTokensPerPort;

    /** Map the input port where an event caused a pure event to the relative
     * deadline for that pure event.
     */
    private Map<TypedIOPort, Double> _relativeDeadlineForPureEvent;

    /** Deadline for event at ptides output ports.
     */
    private HashMap<Time, List<PtidesEvent>> _outputEventDeadlines;

    /**
     */
    private HashMap<PtidesPort, Queue<PtidesEvent>> _ptidesOutputPortEventQueue;

    /** Separate event queue for pure events.
     */
    private DEEventQueue _pureEvents;

    /** Maximum number of future events in the event queue. This number is important for
     *  automatic throttling of local sources. A local source can only produce up to 10
     *  future events. Manual throttling can be done by using the parameters
     *  <i>maxFutureEvents</i> or <
     */
    private static int _maxNumberOfFutureEvents = 10;

}
