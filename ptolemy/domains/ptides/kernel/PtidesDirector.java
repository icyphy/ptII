/* This director implements the Ptides programming model.

@Copyright (c) 2008-2011 The Regents of the University of California.
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
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.ptides.lib.io.ActuatorPort;
import ptolemy.domains.ptides.lib.io.NetworkReceiverPort;
import ptolemy.domains.ptides.lib.io.PtidesPort;
import ptolemy.domains.ptides.lib.io.SensorPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** This director implements the Ptides programming model,
 *  which is used for the design of distributed real-time systems. 
 * 
 * @author Patricia Derler, Edward A. Lee, Slobodan Matic, Mike Zimmer, Jia Zou
   @version $Id$
   @since Ptolemy II 0.2

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
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
     *  @param event New input event.
     *  @param deviceDelay The device delay. 
     *  @throws IllegalActionException If device delay parameter cannot be computed.
     */
    public void addInputEvent(PtidesEvent event, double deviceDelay)
            throws IllegalActionException {
        // FIXME distinguish between sensorinput and network input!!
        Time inputReady = getModelTime().add(deviceDelay);
        List<PtidesEvent> list = _inputEventQueue.get(inputReady);
        if (list == null) {
            list = new ArrayList<PtidesEvent>();
        }
        list.add(event);
        _inputEventQueue.put(inputReady, list);
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
                                    deviceDelay));

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
                    event.ioPort().send(0, event.token());
                    _currentLogicalTime = null;
                    ptidesOutputPortList.poll();
                }
            }
        }
    }

    /** Return the local time or, (i) if an actor is executing or (ii) an input
     *  token is read, (i) the timestamp of the event that caused the actor
     *  execution or (ii) the timestamp of the input event. 
     *  @return The local time or the semantic
     */
    public Time getModelTime() {
        if (_currentLogicalTime != null) {
            return _currentLogicalTime;
        }
        return super.getModelTime();
    }

    /** Add a pure event to the queue of pure events.
     *  @param actor Actor to fire.
     *  @param time Time the actor should be fired at.
     *  @param index Microstep the actor should be fired at.
     *  @return The time the actor requested to be refired at.
     * @throws IllegalActionException If firing of the container doesn't succeed.
     */
    public Time fireAt(Actor actor, Time time, int index)
            throws IllegalActionException {
        // Setting a stop time for the director calls this method
        // with the actor equal to the container.
        if (actor == this.getContainer()) {
            fireContainerAt(time);
            return time;
        }
        _pureEvents.add(new PtidesEvent(actor, null, time, 0, 0, _zeroTime));
        Time environmentTime = super.getEnvironmentTime();
        if (environmentTime.compareTo(time) <= 0) {
            fireContainerAt(time);
        }
        if (_isInitializing) {

        }
        return time;
    }

    /** Initialize all the actors and variables. Perform static analysis on 
     *  superdense dependencies between input ports in the topology.
     *  @exception IllegalActionException If any of the methods contained
     *  in initialize() throw it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _calculateSuperdenseDependenices();
        _calculateDelayOffsets();
        for (Object entity : ((CompositeActor) getContainer()).entityList()) {
            if (entity instanceof ResourceScheduler) {
                if (_resourceSchedulers == null) {
                    _resourceSchedulers = new ArrayList();
                }
                _resourceSchedulers.add((ResourceScheduler) entity);
            }
        }

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
                
                if (port instanceof ActuatorPort && 
                        getEnvironmentTime().compareTo(event.absoluteDeadline()) > 0) {
                    throw new IllegalActionException("Missed Deadline at " + port + "!");
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
     * @throws IllegalActionException 
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
        _pureEvents = new LinkedList<PtidesEvent>();
        _currentLogicalTime = null;
    }

    /** Find resource scheduler for actor and request scheduling.
     *  TODO: This method could be moved to the Director class such that all other
     *  MoCs can do resource usage simulation.
     *  @param actor The actor to be scheduled.
     *  @return Time until next scheduling action or 0.0 if actor can start execution.
     *  @throws IllegalActionException
     */
    public Time scheduleActor(Actor actor) throws IllegalActionException {
        if (_schedulerForActor == null) {
            _schedulerForActor = new HashMap();
        }
        Object object = _schedulerForActor.get(actor);
        if (!_schedulerForActor.containsKey(actor)) {
            if (object == null) {
                List attributeList = ((NamedObj) actor).attributeList();
                if (attributeList.size() > 0) {
                    for (int i = 0; i < attributeList.size(); i++) {
                        Object attr = attributeList.get(i);
                        if (attr instanceof Parameter) {
                            Token paramToken = ((Parameter) attr).getToken();
                            if (paramToken instanceof ObjectToken) {
                                Object paramObject = ((ObjectToken) paramToken)
                                        .getValue();
                                if (paramObject instanceof ResourceScheduler) {
                                    ResourceScheduler scheduler = (ResourceScheduler) paramObject;
                                    _schedulerForActor.put(actor, scheduler);
                                    object = scheduler;
                                    break;
                                }
                            }
                        }
                    }
                    if (!_schedulerForActor.containsKey(actor)) {
                        _schedulerForActor.put(actor, null);
                    }
                }
            }
        }
        if (object != null) {
            return ((ResourceScheduler) object).schedule(actor,
                    getEnvironmentTime());
        } else {
            return null;
        }
    }

    /** Return true if the actor finished execution.
     *  @param actor The actor.
     *  @return True if the actor finished execution.
     */
    public boolean actorFinished(Actor actor) {
        return (_schedulerForActor.get(actor) != null && _schedulerForActor
                .get(actor).lastScheduledActorFinished());
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
    protected void _calculateDelayOffsets() throws IllegalActionException {

        // Calculate delayOffset to each input port.
        for (TypedIOPort port : _inputPorts) {

            // Disallow SensorPort and NetworkReceiverPort.
            if (port instanceof SensorPort
                    || port instanceof NetworkReceiverPort) {
                continue;
            }

            // Find minimum delay offset from all sensor or network receiver 
            // input ports to the input port group of this port.
            double delayOffset = Double.POSITIVE_INFINITY;
            for (TypedIOPort inputPort : _inputPorts) {
                // Only allow SensorPort and NetworkReceiverPort.
                if (!(inputPort instanceof SensorPort || inputPort instanceof NetworkReceiverPort)) {
                    continue;
                }
                double deviceDelayBound = _getDoubleParameterValue(inputPort,
                        "deviceDelayBound");
                if (inputPort instanceof NetworkReceiverPort) {
                    deviceDelayBound += _getDoubleParameterValue(inputPort, 
                            "networkDelayBound");
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

    /** Calculate the superdense dependency (minimum model time delay) between
     * all source and destination input ports. The Floyd-Warshall algorithm is 
     * used to calculate the minimum model time delay paths.
     * @exception IllegalActionException If the container is not a
     * TypedCompositeActor. 
     * TODO: Assumes all channels have same dependency as multiport.
     */
    protected void _calculateSuperdenseDependenices()
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
            if (port instanceof SensorPort
                    || port instanceof NetworkReceiverPort) {

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
                        //                        // Add connected input ports if this input port can
                        //                        // produce pure events.
                        //                        if(!minDelay.equals(
                        //                                SuperdenseDependency.OTIMES_IDENTITY)) {
                        //                            if(!_inputPortsForPureEvent.containsKey(inputPort)) 
                        //                            {
                        //                                _inputPortsForPureEvent.put(
                        //                                        inputPort, new HashSet<TypedIOPort>());
                        //                            }
                        //                            _inputPortsForPureEvent.get(inputPort).addAll(
                        //                                    (List<TypedIOPort>)
                        //                                    outputPort.deepConnectedPortList());
                        //                        }
                        // Set input port pair for all connected ports.
                        // Assumes no delay from connections.
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

    /** Return the superdense dependency between a source and a destination 
     * input port. If the mapping does not exist, it is assumed to be 
     * SuperdenseDependency.OPLUS_IDENTITY.
     * @param source Source input port.
     * @param destination Destination input port.
     * @return The Superdense dependency.
     */
    protected SuperdenseDependency _getSuperdenseDependencyPair(
            TypedIOPort source, TypedIOPort destination) {
        Map<TypedIOPort, Map<TypedIOPort, SuperdenseDependency>> pair;
        pair = _superdenseDependencyPair;
        if (pair.containsKey(source)
                && pair.get(source).containsKey(destination)) {
            return pair.get(source).get(destination);
        } else {
            return SuperdenseDependency.OPLUS_IDENTITY;
        }
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
                depth, token, receiver);

        // FIXME: any way of knowing if coming from sensor?

        if (ioPort.isOutput()) {
            // FIXME: add deviceDelay
            Time deliveryTime;
            if (ioPort instanceof ActuatorPort) {
                
                deliveryTime = getModelTime().subtract(
                        _getDoubleParameterValue(ioPort, "deviceDelayBound"));
                
                if (getEnvironmentTime().compareTo(deliveryTime) > 0) {
                    throw new IllegalActionException("Missed Deadline at " + ioPort + "!");
                }
            } else {
                deliveryTime = localClock.getLocalTime();
            }

            List<PtidesEvent> list = _outputEventQueue.get(deliveryTime);
            if (list == null) {
                list = new ArrayList<PtidesEvent>();
            }
            list.add(newEvent);
            _outputEventQueue.put(deliveryTime, list);
            if (_debugging) {
                _debug("  enqueue actuator event for time " + deliveryTime);
            }
        } else {
            _eventQueue.put(newEvent);
        }
    }

    /** Return the value stored in a parameter associated with
     *  the input port.
     *  Used for deviceDelay, deviceDelayBound, networkDelayBound, 
     *  platformDelay and sourcePlatformDelay. 
     *  FIXME: specialized ports do contain the parameters, don't
     *  have to get the attribute with the string! For now leave it
     *  that way to support older models that do not use PtidesPorts.
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
        if (parameter != null) {
            return Double.valueOf(((DoubleToken) parameter.getToken())
                    .doubleValue());
        }
        return null;
    }

    /** Return the actor to fire in this iteration, or null if no actor should
     * be fired. Since _checkForNextEvent() always
     * returns true, this method will keep being called until it returns null.  
     * @exception IllegalActionException If _isSafeToProcess() throws it.
     */
    protected Actor _getNextActorToFire() throws IllegalActionException {
        //        Actor nextActorToFire = null;
        //        if (_pureEvents != null && _pureEvents.size() > 0) {
        //            PtidesEvent event = _pureEvents.peek();
        //            if (_isSafeToProcess(event)) {
        //                _currentLogicalTime = event.timeStamp();
        //                nextActorToFire = event.actor();
        //                _pureEvents.poll();
        //            }
        //        } 
        //        
        //        if (nextActorToFire == null & _eventQueue.size() > 0) {
        //            PtidesEvent event = null;
        //            if (currentEvent == null) {
        //                event = (PtidesEvent) _eventQueue.get();
        //            } else {
        //                event = currentEvent;
        //            }
        //            if (_isSafeToProcess(event)) {
        //                Time time = scheduleActor(event.actor());
        //                if (time != null && time.getDoubleValue() > 0.0) {
        //                    fireContainerAt(getEnvironmentTime().add(time));
        //                    if (currentEvent == null) {
        //                        currentEvent = event;
        //                    }
        //                    _currentLogicalTime = null;
        //                    return null;
        //                } 
        //                
        //                currentEvent = null;
        //            } 
        //        }
        //        
        //        if (nextActorToFire != null) {
        //            if (_debugging) {
        //                _debug("-> next actor to fire = " + nextActorToFire + " @ " + _currentLogicalTime);
        //            } 
        //            return nextActorToFire; 
        //        }
        //        _currentLogicalTime = null;
        //        return null;

        for (PtidesEvent event : _pureEvents) {
            if (_isSafeToProcess(event)) {

                Time time = scheduleActor(event.actor());
                Boolean finished = actorFinished(event.actor());
                if (time != null && time.getDoubleValue() > 0.0) {
                    fireContainerAt(getEnvironmentTime().add(time));
                }
                if (time == null || finished) {
                    _currentLogicalTime = event.timeStamp();
                    _pureEvents.poll();
                    return event.actor();
                }
            }
        }
        for (Object event : _eventQueue.toArray()) {
            if (_isSafeToProcess((PtidesEvent) event)) {
                Time time = scheduleActor(((PtidesEvent) event).actor());
                Boolean finished = actorFinished(((PtidesEvent) event).actor());
                if (time != null && time.getDoubleValue() > 0.0) {
                    fireContainerAt(getEnvironmentTime().add(time));
                }
                if (time == null || finished) {
                    _currentLogicalTime = ((PtidesEvent) event).timeStamp();
                    _removeEventsFromQueue((PtidesEvent) event);
                    return ((PtidesEvent) event).actor();
                }
            }
        }
        _currentLogicalTime = null;
        return null;
    }

    /** Check if event is safe to process.
     * @param event The event to be checked.
     * @return true if the event is safe to process.
     * @throws IllegalActionException If the delayOffset aparameter
     * cannot be read.
     */
    protected boolean _isSafeToProcess(PtidesEvent event)
            throws IllegalActionException {
        Time eventTimestamp = event.timeStamp();
        IOPort port = event.ioPort();
        Double delayOffset;
        if (port != null) {
            delayOffset = _getDoubleParameterValue(port, "delayOffset");
        } else {
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
    protected void _putSuperdenseDependencyPair(TypedIOPort source,
            TypedIOPort destination, SuperdenseDependency dependency) {
        Map<TypedIOPort, Map<TypedIOPort, SuperdenseDependency>> pair;
        pair = _superdenseDependencyPair;
        if (!dependency.equals(SuperdenseDependency.OPLUS_IDENTITY)) {
            pair.get(source).put(destination, dependency);
        }
    }

    /** Remove all events with the same tag and at the same actor from the
     * event queue.
     * @param event The event.
     * @return A list of all events with same tag and at the same actor as the
     * event.
     */
    protected List<PtidesEvent> _removeEventsFromQueue(PtidesEvent event) {
        // FIXME: Move to PtidesListEventQueue?
        List<PtidesEvent> eventList = new ArrayList<PtidesEvent>();
        int i = 0;
        while (i < _eventQueue.size()) {
            PtidesEvent eventInQueue = ((PtidesListEventQueue) _eventQueue)
                    .get(i);
            // If event has same tag and destined to same actor, remove from
            // queue.
            // TODO: or input port group?
            if (eventInQueue.hasTheSameTagAs(event)
                    && eventInQueue.actor().equals(event.actor())) {
                eventList.add(eventInQueue);
                ((PtidesListEventQueue) _eventQueue).take(i);
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
    protected void _setDelayOffset(NamedObj namedObj, Double delayOffset)
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
    protected void _setNextFireTime(Time time) {
        if (_nextFireTime == null) {
            _nextFireTime = time;
        } else if (_nextFireTime.compareTo(time) > 0) {
            _nextFireTime = time;
        }
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
    ////                         private variables                 ////


    private Time _currentLogicalTime;
    
    private HashMap<Time, List<PtidesEvent>> _inputEventQueue;

    private HashMap<Time, List<PtidesEvent>> _outputEventQueue;

    private HashMap<PtidesPort, Queue<PtidesEvent>> _ptidesOutputPortEventQueue;
    
    private Queue<PtidesEvent> _pureEvents;

    private List<ResourceScheduler> _resourceSchedulers;

    private HashMap<Actor, ResourceScheduler> _schedulerForActor;

}
