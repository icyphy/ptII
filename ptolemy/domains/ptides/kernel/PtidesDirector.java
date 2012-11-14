/* This director implements the Ptides programming model.

@Copyright (c) 2008-2012 The Regents of the University of California.
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
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.domains.ptides.lib.ErrorHandlingAction;
import ptolemy.domains.ptides.lib.PtidesPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** This director implements the Ptides programming model,
 *  which is used for the design of distributed real-time systems.
 *
 *  <p> This director can only be used inside the PtidesPlatform
 *  and if the PtidesPlatform is dragged and dropped from the library,
 *  it already contains a PtidesDirector. A PtidesPlatform must be
 *  embedded in a timed director such as DE or Continuous. The time of
 *  this enclosing director simulates physical time.
 *  The localClock of this PtidesDirector simulates platformTime.
 *
 *  <p> The Ptides director is based on the DE director. Like the DE
 *  director, this director maintains a totally ordered set of events.
 *  Event timestamps are given in logical time. The logical time is decoupled
 *  from the platformTime. The model time of the director is the
 *  platformTime unless an actor is fired; then the model time is
 *  the timestamp of the event that caused the actor firing.
 *
 *  Unlike the DE Director, this director can process events out of timestamp order
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
 *  Currently, only
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
 *  7th ACM & IEEE international conference on Embedded
 *  software, ACM, 114-123, 2007.</p>
 *
 *  <p> This director provides a set of features to address both
 *  the distributed and the real-time aspects of system design.
 *  To address the distributed aspect, each PtidesPlatform simulates
 *  a computation platform
 *  (e.g., a microprocessor), while the enclosing director simulates
 *  the physical world. Actors under the Ptides director then communicate
 *  to the outside via SensorPorts, ActuatorPorts, or network ports
 *  (NetworkReceivers, NetworkTransmitters).
 *  </p>
 *
 *  <p> This director allows for simulation of execution time. If the PtidesPlatform
 *  contains ResourceSchedulers, the scheduling of actors is performed by these.
 *  Actors must specify in parameters which ResourceSchedulers they are assigned
 *  to and the executionTime. The passage of execution time equals the passage
 *  of platformTime. Execution time has no influence on the event timestamps.
 *
 *  <p> In a Ptides environment, all platforms are assumed to be synchronized
 *  within a bounded error.
 *
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
 *  @since Ptolemy II 8.1
 *
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class PtidesDirector extends DEDirector {

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

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** Bound on clock synchronization error across all platforms.
     *  FIXME: eventually set parameter per platform or for some
     *  platforms.
     */
    public SharedParameter clockSynchronizationErrorBound;

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
                    + networkDelayBound) {
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

    /**
     * Return the default dependency between input and output ports,
     * which for the Ptides domain is a {@link SuperdenseDependency}.
     *
     * @return The default dependency that describes a time delay of 0.0,
     *          and a index delay of 0 between ports.
     */
    public Dependency defaultDependency() {
        return SuperdenseDependency.OTIMES_IDENTITY;
    }

    /**
     * Before super.fire() is called, transfer all input events that are ready are
     * transferred. After super.fire() is called, transfer all output events that
     * are ready are transferred.
     */
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
                }
            }
            _inputEventQueue.remove(getModelTime());
        }

        super.fire();

        // Transfer all outputs to the ports that are ready.
        list = _outputEventQueue.get(getModelTime());
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
            _outputEventQueue.remove(getModelTime());
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
    public Time fireAt(Actor actor, Time time, int index)
            throws IllegalActionException {
        // Setting a stop time for the director calls this method
        // with the actor equal to the container.
        if (actor == this.getContainer()) {
            fireContainerAt(time);
            return time;
        }
        int newIndex = index;
        if (index <= getIndex()) {
            newIndex = Math.max(getIndex(), index) + 1;
        }

        if (_isInitializing) {
            _currentSourceTimestamp = time;
        }
        _pureEvents.put(new PtidesEvent(actor, null, time, newIndex, 0,
                _zeroTime, _currentSourceTimestamp));
        _currentSourceTimestamp = null;

        Time environmentTime = super.getEnvironmentTime();
        if (environmentTime.compareTo(time) <= 0) {
            fireContainerAt(time);
        }
        if (_isInitializing) {

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

    /** Return a superdense time index for the current time,
     *  where the index is equal to the microstep.
     *  @return A superdense time index.
     *  @see #setIndex(int)
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
    public int getIndex() {
        return getMicrostep();
    }

    /** Return the local time or, (i) if an actor is executing or (ii) an input
     *  token is read, (i) the timestamp of the event that caused the actor
     *  execution or (ii) the timestamp of the input event.
     *  @return The local time or the semantic
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

    /** Initialize all the actors and variables. Perform static analysis on 
     *  superdense dependencies between input ports in the topology.
     *  @exception IllegalActionException If any of the methods contained
     *  in initialize() throw it.
     */
    public void initialize() throws IllegalActionException {
        _inputPortsForPureEvent = new HashMap<TypedIOPort, Set<TypedIOPort>>();
        _relativeDeadlineForPureEvent = new HashMap<TypedIOPort, Double>();
        _executionTimes = new HashMap<Actor, Time>();

        _calculateSuperdenseDependenices();
        _calculateDelayOffsets();
        _calculateRelativeDeadlines();

        super.initialize();
    }

    /** Return a new receiver of the type {@link PtidesReceiver}.
     *  @return A new PtidesReceiver.
     */
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
    public boolean postfire() throws IllegalActionException {
        // Do not call super.postfire() because that requests a
        // refiring at the next event time on the event queue.
        Boolean result = !_stopRequested && !_finishRequested;
        if (getModelTime().compareTo(getModelStopTime()) >= 0) {
            // If there is a still event on the event queue with time stamp
            // equal to the stop time, we want to process that event before
            // we declare that we are done.
            // FIXME: With EDF, event at head of event queue may not have smallest timestamp.
            if (!_eventQueue.get().timeStamp().equals(getModelStopTime())) {
                result = false;
            }
        }

        // Potentially set next fire time from _outputEventQueue.
        Set<Time> deliveryTimes = _outputEventQueue.keySet();
        if (deliveryTimes.size() > 0) {
            TreeSet<Time> set = new TreeSet<Time>(deliveryTimes);
            for (PtidesEvent event : _outputEventQueue.get(set.first())) {
                if (event.ioPort() instanceof PtidesPort
                        && ((PtidesPort) event.ioPort()).isActuatorPort()
                        && getEnvironmentTime().compareTo(event.timeStamp()) > 0) {
                    handleModelError(event.ioPort(),
                            new IllegalActionException(event.ioPort(),
                                    "Missed Deadline at " + event.ioPort()
                                            + "!"));
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
    public boolean prefire() throws IllegalActionException {
        setModelTime(localClock.getLocalTimeForCurrentEnvironmentTime());
        if (_debugging) {
            _debug("...prefire @ " + localClock.getLocalTime());
        }
        setIndex(1);
        _nextFireTime = null;
        return true;
    }

    /** Call the preinitialize of the super class and create new event
     *  Queue.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _eventQueue = new PtidesListEventQueue();
        _inputEventQueue = new HashMap<Time, List<PtidesEvent>>();
        _outputEventQueue = new HashMap<Time, List<PtidesEvent>>();
        _ptidesOutputPortEventQueue = new HashMap<PtidesPort, Queue<PtidesEvent>>();
        _nextFireTime = Time.POSITIVE_INFINITY;
        _pureEvents = new PtidesListEventQueue();
        _currentLogicalTime = null;
    }

    /** Model time is only used for correct execution of actors and the
     * scheduler will determine whether another event can be fired in
     * the current firing of the platform, so this method isn't needed.
     * By always returning true, _getNextActorToFire() will be called which
     * runs the scheduler.
     *  @return true Always.
     */
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

        if ((_eventQueue == null)
                || ((_disabledActors != null) && _disabledActors
                        .contains(actor))) {
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
                ioPort.getChannelForReceiver(receiver), getModelTime(), 1,
                depth, token, receiver, _currentSourceTimestamp);

        // FIXME: any way of knowing if coming from sensor?

        if (ioPort.isOutput()) {

            Time deliveryTime;
            deliveryTime = localClock.getLocalTime();
            if (((PtidesPort) ioPort).isActuatorPort()) {
                if (((PtidesPort) ioPort).actuateAtEventTimestamp()) {
                    deliveryTime = getModelTime().subtract(
                            _getDoubleParameterValue(ioPort, "deviceDelay"));
                }

                if (getModelTime().compareTo(deliveryTime) < 0) {
                    newEvent = _handleTimingError((PtidesPort) ioPort,
                            newEvent,
                            "Missed Deadline at " + ioPort + "!\n " + " At "
                                    + getModelTime()
                                    + " which is bigger than currentTime "
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
                                    + "Current Physical time: "
                                    + localClock.getLocalTime()
                                    + " Event timestamp: "
                                    + getModelTime()
                                    + " Platform delay: "
                                    + _getDoubleParameterValue(ioPort,
                                            "platformDelayBound"));
                }
            }

            if (newEvent != null) {
                List<PtidesEvent> list = _outputEventQueue.get(deliveryTime);
                if (list == null) {
                    list = new ArrayList<PtidesEvent>();
                }
                list.add(newEvent);
                _outputEventQueue.put(deliveryTime, list);
                if (_debugging) {
                    _debug("  enqueue actuator event for time " + deliveryTime);
                }
            }
        } else {
            _eventQueue.put(newEvent);
        }
    }

    /** Compute the deadline for an actor that requests a firing at time
     *  <i>timestamp</i>.
     *  @param actor The actor that requests firing. 
     *  @param timestamp The time when the actor wants to be fired.
     *  @return The deadline for the actor.
     *  @exception IllegalActionException If time objects cannot be created. 
     */
    protected double _getDeadline(Actor actor, Time timestamp)
            throws IllegalActionException {
        Time relativeDeadline = Time.POSITIVE_INFINITY;
        for (int i = 0; i < actor.outputPortList().size(); i++) {
            for (int j = 0; j < ((IOPort) actor.outputPortList().get(i))
                    .sinkPortList().size(); j++) {
                double newRelativeDeadline = _getRelativeDeadline(((TypedIOPort) ((IOPort) actor
                        .outputPortList().get(i)).sinkPortList().get(j)));
                if (newRelativeDeadline < Double.MAX_VALUE
                        && newRelativeDeadline < relativeDeadline
                                .getDoubleValue()) {
                    relativeDeadline = new Time(this, newRelativeDeadline);
                }
            }
        }
        return timestamp.add(relativeDeadline).getDoubleValue();
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
    ////                         protected methods                 ////
    
    /** Calculate the delay offset for each input port.
     * The delay offset is used in the safe-to-process analysis
     * to know when no future events can occur at a sensor or network
     * receiver port that can result in an event arriving at an input port
     * with an earlier timestamp than the event currently there.
     * @exception IllegalActionException If cannot set 'delayOffset' parameter
     * for an input port.
     */
    private void _calculateDelayOffsets() throws IllegalActionException {
    
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
                if (!((inputPort instanceof PtidesPort) && (((PtidesPort) inputPort)
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
            _setDelayOffset(
                    port,
                    delayOffset
                            - ((DoubleToken) clockSynchronizationErrorBound
                                    .getToken()).doubleValue());
        }
    
        // Calculate delayOffset to each actor
        for (Object entity : ((CompositeActor) getContainer()).entityList()) {
            if (entity instanceof TimeDelay) {
                _setDelayOffset((NamedObj) entity,
                        ((DoubleToken) ((TimeDelay) entity).minimumDelay
                                .getToken()).doubleValue());
            }
        }
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
                if (!((outputPort instanceof PtidesPort) && (((PtidesPort) outputPort)
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
     * TODO: Assumes all channels have same dependency as multiport.
     */
    private void _calculateSuperdenseDependenices()
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
    
                for (IOPort connectedPort : (List<IOPort>) (port
                        .insideSinkPortList())) {
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
    
            for (TypedIOPort inputPort : (List<TypedIOPort>) (actor
                    .inputPortList())) {
    
                // Ignore input if it's not connected to anything.
                if (!inputPort.isOutsideConnected()) {
                    continue;
                }
    
                _addInputPort(inputPort);
    
                for (TypedIOPort outputPort : (List<TypedIOPort>) (actor
                        .outputPortList())) {
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
                                    (List<TypedIOPort>) outputPort
                                            .deepConnectedPortList());
                        }
                        for (TypedIOPort connectedPort : (List<TypedIOPort>) outputPort
                                .deepConnectedPortList()) {
                            _putSuperdenseDependencyPair(inputPort,
                                    connectedPort, minDelay);
                        }
                        // Find input port group.
                        for (TypedIOPort inPort : (List<TypedIOPort>) (actor
                                .inputPortList())) {
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
                    if (ij.compareTo(ik.oTimes(kj)) == SuperdenseDependency.GREATER_THAN) {
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
        }
    }

    /** Return the execution time of a given actor. If the flag scheduleWithZeroExecutionTime
     *  is true then return 0.0.
     * @param scheduleWithZeroExecutionTime If true execution time is 0.0.
     * @param actor The actor.
     * @return The execution Time.
     * @exception IllegalActionException Thrown if time objects cannot be created.
     */
    private Time _getExecutionTime(boolean scheduleWithZeroExecutionTime,
            Actor actor) throws IllegalActionException {
        Time executionTime = null;
        if (scheduleWithZeroExecutionTime) {
            executionTime = new Time(this, 0.0);
        } else {
            if (_executionTimes == null) {
                _executionTimes = new HashMap<Actor, Time>();
            }
            executionTime = _executionTimes.get(actor);
            if (executionTime == null) {
                Double executionTimeParam = _getDoubleParameterValue(
                        (NamedObj) actor, "executionTime");
                if (executionTimeParam == null) {
                    executionTime = new Time(this, 0.0);
                } else {
                    executionTime = new Time(this, executionTimeParam);
                }
                _executionTimes.put(actor, executionTime);
            }
        }
        return executionTime;
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
                PtidesEvent ptidesEvent = ((PtidesEvent) event);
    
                // Check if actor can be fired by putting token into receiver 
                // and accling prefire. 
    
                List<PtidesEvent> sameTagEvents = new ArrayList<PtidesEvent>();
                int i = 0;
                while (i < queue.size()) {
                    PtidesEvent eventInQueue = ((PtidesListEventQueue) queue)
                            .get(i);
                    // If event has same tag and destined to same actor, remove from
                    // queue.
                    // TODO: or input port group?
                    if (eventInQueue.hasTheSameTagAs(ptidesEvent)
                            && eventInQueue.actor().equals(ptidesEvent.actor())) {
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
    
                _currentLogicalTime = ptidesEvent.timeStamp();
                _currentLogicalIndex = ptidesEvent.microstep();
                _currentSourceTimestamp = ptidesEvent.sourceTimestamp();
                boolean prefire = ptidesEvent.actor().prefire();
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
    
                if (prefire && (!_resourceScheduling || (_resourceScheduling && _schedule(
                                ptidesEvent.actor(),
                                ptidesEvent.timeStamp(),
                                _getExecutionTime(
                                        queue != _pureEvents
                                                && ptidesEvent.actor() instanceof TimeDelay,
                                        ptidesEvent.actor()))))) {
                    _currentLogicalTime = ptidesEvent.timeStamp();
                    _currentLogicalIndex = ptidesEvent.microstep();
                    _currentSourceTimestamp = ptidesEvent.sourceTimestamp();
                    _removeEventsFromQueue(queue, ptidesEvent);
                    return ptidesEvent.actor();
                }
            }
        }
        return null;
    }

    private int _getNumberOfFutureEventsFrom(Actor actor) {
        HashMap<Actor, Integer> sinkActorEventQueueSize = new HashMap();
        int maxEvents = 0;
        // Find all sink actors.
        for (Object object : actor.outputPortList()) {
            IOPort port = (IOPort) object;
            for (Object sinkPort : port.sinkPortList()) {
                sinkActorEventQueueSize.put(
                        (Actor) ((IOPort) sinkPort).getContainer(),
                        Integer.valueOf(0));
            }
        }
        Object[] eventArray = _eventQueue.toArray();
        for (Object object : eventArray) {
            PtidesEvent event = (PtidesEvent) object;
            if (sinkActorEventQueueSize.keySet().contains(event.actor())) {
                int events = sinkActorEventQueueSize.get(event.actor()) + 1;
                sinkActorEventQueueSize.put(event.actor(), events);
                if (events > maxEvents) {
                    maxEvents = events;
                }
            }
        }
        return maxEvents;
    }

    private IOPort _getPort(CompositeActor actor, String name) {
        for (Object object : actor.portList()) {
            if (((IOPort) object).getName().equals(name)) {
                return (IOPort) object;
            }
        }
        return null;
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

    
    /** Handle timing error on a PtidesPort. 
     * 
     * FIXME: for now this can only drop the event that caused the error or throw a message. 
     * TODO: implement different behaviors.
     * @param port The port where the error occurred.
     * @param event The event that caused the error; i.e. that arrived too late or out of order.
     * @param message The error message.
     * @return A new PtidesEvent that can be safely processed or null if no event should be processed.
     * @throws IllegalActionException If error handling actor throws this.
     */
    private PtidesEvent _handleTimingError(PtidesPort port, PtidesEvent event, String message)
            throws IllegalActionException {
         List list = ((CompositeActor)getContainer()).entityList();
         for (int i = 0; i < list.size(); i++) {
             Object entity = list.get(i);
             if (entity instanceof CompositeActor && 
                     ((CompositeActor)entity).getName().equals("ErrorHandler")) {
                 CompositeActor errorHandler = (CompositeActor) entity;
                 
                 List errorHandlerEntities = errorHandler.entityList(); 
                 for (int j = 0; j < errorHandlerEntities.size(); j++) {
                     Object errorHandlerEntity = errorHandlerEntities.get(j);
                     if (errorHandlerEntity instanceof Const && 
                             ((Const)errorHandlerEntity).getName().equals("missed" + port.getName())) {
                         
                         int index = 1;
                         ((CompositeActor)errorHandler).getDirector().setModelTime(getModelTime());
                         ((DEDirector)((CompositeActor)errorHandler).getDirector()).setIndex(index);
                         ((Const)errorHandlerEntity).fire();
                         
                         
                         Time time = errorHandler.getDirector().getModelNextIterationTime();
                         
                         ((CompositeActor)errorHandler).prefire(); 
                         ((CompositeActor)errorHandler).fire(); 
                         ((CompositeActor)errorHandler).postfire();
                         
                         List attributes = errorHandler.attributeList();
                         for (int k = 0; k < attributes.size(); k++) {
                             Attribute attribute = (Attribute) attributes.get(k);
                             if (attribute instanceof Parameter) {
                                 if (((Parameter)attribute).getName().equals(ErrorHandlingAction.DropEvent)) {
                                     if (((Parameter)attribute).getToken() != null && 
                                             ((BooleanToken)((Parameter)attribute).getToken()).booleanValue()) {
                                         ((Parameter)attribute).setToken("false");
                                         return null;
                                     }
                                 } 
                                 if (((Parameter)attribute).getName().equals(ErrorHandlingAction.ExecuteEvent)) {
                                     if (((Parameter)attribute).getToken() != null && 
                                             ((BooleanToken)((Parameter)attribute).getToken()).booleanValue()) {
                                         return event;
                                     }
                                 }
                                 if (((Parameter)attribute).getName().equals(ErrorHandlingAction.FixTimestamp)) {
                                     if (((Parameter)attribute).getToken() != null && 
                                             ((BooleanToken)((Parameter)attribute).getToken()).booleanValue()) {
                                         PtidesEvent newEvent = new PtidesEvent(event.ioPort(), event.channel(), 
                                                 getModelTime(),
                                                 event.microstep(), event.depth(), event.token(), event.receiver(), 
                                                 event.sourceTimestamp());
                                         return newEvent;
                                     } 
                                 }
                                 if (((Parameter)attribute).getName().equals(ErrorHandlingAction.ClearAllEvents)) {
                                     if (((Parameter)attribute).getToken() != null && 
                                             ((BooleanToken)((Parameter)attribute).getToken()).booleanValue()) {
                                         _eventQueue.clear();
                                         _outputEventQueue.clear();
                                         _pureEvents.clear();
                                         return null;
                                     }
                                 }
                                 if (((Parameter)attribute).getName().equals(ErrorHandlingAction.ClearEarlierEvents)) {
                                     if (((Parameter)attribute).getToken() != null && 
                                             ((BooleanToken)((Parameter)attribute).getToken()).booleanValue()) { 
                                         int idx = 0;
                                         while (i < _eventQueue.size()) {
                                             PtidesEvent eventInQueue = ((PtidesListEventQueue) _eventQueue).get(idx); 
                                             if (eventInQueue.sourceTimestamp().compareTo(event.sourceTimestamp()) < 0) {
                                                 ((PtidesListEventQueue) _eventQueue).take(i); 
                                                 continue;
                                             }
                                             idx++; 
                                         }
                                          
                                         idx = 0;
                                         while (i < _pureEvents.size()) {
                                             PtidesEvent eventInQueue = ((PtidesListEventQueue) _pureEvents).get(idx); 
                                             if (eventInQueue.sourceTimestamp().compareTo(event.sourceTimestamp()) < 0) {
                                                 ((PtidesListEventQueue) _pureEvents).take(i); 
                                                 continue;
                                             }
                                             idx++; 
                                         } 
                                     }
                                     for (Time outputTime : _outputEventQueue.keySet()) {
                                         for (PtidesEvent outputEvent : _outputEventQueue.get(outputTime)) {
                                             if (outputEvent.sourceTimestamp().compareTo(event.sourceTimestamp()) < 0) { 
                                                 _outputEventQueue.remove(outputTime);
                                             }
                                         }
                                     }
                                     return null;
                                 }
                                 if (((Parameter)attribute).getName().equals(ErrorHandlingAction.ClearCorruptEvents)) {
                                     // TODO
                                     return null;
                                 }
                             }
                         }
                     }
                 }
             }
         }
        throw new IllegalActionException(port, message);
    }

    /** Check if event is safe to process.
     * @param event The event to be checked.
     * @return true if the event is safe to process.
     * @exception IllegalActionException If the delayOffset aparameter
     * cannot be read.
     */
    private boolean _isSafeToProcess(PtidesEvent event)
            throws IllegalActionException {
        Time eventTimestamp = event.timeStamp();
    
        IOPort port = event.ioPort();
        Double delayOffset;
        if (port != null) {
            delayOffset = _getDoubleParameterValue(port, "delayOffset");
        } else {
            // A local source can have a maximum future events parameter.
            Integer maxFutureEvents = _getIntParameterValue(
                    (NamedObj) event.actor(), "maxFutureEvents");
            if (maxFutureEvents != null) {
                int futureEvents = _getNumberOfFutureEventsFrom(event.actor());
                if (futureEvents > maxFutureEvents) {
                    return false;
                } else {
                    return true;
                }
            }
    
            // A local source can have a delay offset parameter.
            delayOffset = _getDoubleParameterValue((NamedObj) event.actor(),
                    "delayOffset");
            if (delayOffset == null) {
                delayOffset = new Double(0.0);
            }
        }
        if (localClock.getLocalTime().compareTo(
                eventTimestamp.subtract(delayOffset)) >= 0) {
            return true;
        }
    
        _setNextFireTime(eventTimestamp.subtract(delayOffset));
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
            // TODO: or input port group?
            if (eventInQueue.hasTheSameTagAs(event)
                    && eventInQueue.actor().equals(event.actor())) {
                eventList.add(eventInQueue);
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
    
        // FIXME: change method to _setDoubleParameterValue?
        DoubleToken token = new DoubleToken(delayOffset);
        Parameter parameter = (Parameter) namedObj.getAttribute("delayOffset");
        if (parameter == null) {
            try {
                parameter = new Parameter(namedObj, "delayOffset", token);
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
    private void _setRelativeDeadline(TypedIOPort port,
            Double relativeDeadline) throws IllegalActionException {
        DoubleToken token = new DoubleToken(relativeDeadline);
        Parameter parameter = (Parameter) port.getAttribute("relativeDeadline");
        if (parameter == null) {
            try {
                parameter = new Parameter(port, "relativeDeadline", token);
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

    private Time _currentLogicalTime;
    private Time _currentSourceTimestamp;
    private int _currentLogicalIndex;

    /** The execution times of actors.
     */
    private HashMap<Actor, Time> _executionTimes;

    private HashMap<Time, List<PtidesEvent>> _inputEventQueue;

    /** Connected input ports for an input port which may produce a pure
     * event. Used to calculate _relativeDeadlineForPureEvent.
     */
    private Map<TypedIOPort, Set<TypedIOPort>> _inputPortsForPureEvent;

    /** Map the input port where an event caused a pure event to the relative
     * deadline for that pure event.
     */
    private Map<TypedIOPort, Double> _relativeDeadlineForPureEvent;

    private HashMap<Time, List<PtidesEvent>> _outputEventQueue;

    private HashMap<PtidesPort, Queue<PtidesEvent>> _ptidesOutputPortEventQueue;

    private DEEventQueue _pureEvents;

}
