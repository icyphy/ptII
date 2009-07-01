/* Basic Ptides director that uses DE and delivers correct but not necessarily optimal execution.

@Copyright (c) 2008-2009 The Regents of the University of California.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.ptides.lib.NetworkInputDevice;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.kernel.attributes.VisibleAttribute;

/**
 *  This director has a local notion time, decoupled from that of the
 *  enclosing director. The enclosing director's time
 *  represents physical time, whereas this time represents model
 *  time in the Ptides model.
 *  Assume the incoming event always has higher priority, so preemption always occurs.
 *  <p>
 *  The receiver used in this case is the PtidesBasicReceiver.
 *  @see PtidesBasicReceiver
 *
 *  @author Patricia Derler, Edward A. Lee, Ben Lickly, Isaac Liu, Slobodan Matic, Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class PtidesBasicDirector extends DEDirector {

    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesBasicDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        animateExecution = new Parameter(this, "animateExecution");
        animateExecution.setExpression("false");
        animateExecution.setTypeEquals(BaseType.BOOLEAN);

        _zero = new Time(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then modify the icon for this director to indicate
     *  the state of execution. This is a boolean that defaults to false.
     */
    public Parameter animateExecution;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     * Return the default dependency between input and output ports which for
     * the Ptides domain is a SuperdenseDependency.
     *
     * @return The default dependency which describes a time delay of 0.0,
     *          and a index delay of 0 between ports.
     */
    public Dependency defaultDependency() {
        return SuperdenseDependency.OTIMES_IDENTITY;
    }

    /**
     * Return a superdense dependency representing a model-time delay of the
     * specified amount.
     *
     * @param delay
     *            The real (timestamp) part of the delay.
     * @param index
     *            The integer (microstep) part of the delay
     * @return A Superdense dependency representing a delay.
     */
    public Dependency delayDependency(double delay, int index) {
        return SuperdenseDependency.valueOf(delay, index);
    }

    /** Get the current microstep.
     *  @see #setMicrostep
     *  @return microstep of the current time.
     */
    public int getMicrostep() {
        return _microstep;
    }

    /** Get the current Tag.
     *  @return timestamp and microstep of the current time.
     */
    public Tag getModelTag() {
        return new Tag(_currentTime, _microstep);
    }

    /** Advance the current model tag to that of the earliest event in
     *  the event queue, and fire all actors that have requested or
     *  are triggered to be fired at the current tag. If
     *  <i>synchronizeToRealTime</i> is true, then before firing, wait
     *  until real time matches or exceeds the timestamp of the
     *  event. Note that the default unit for time is seconds.
     *  <p>
     *  Each actor is fired repeatedly (prefire(), fire()),
     *  until either it has no more input tokens, or its prefire() method
     *  returns false. Note that if the actor fails to consume its
     *  inputs, then this can result in an infinite loop.
     *  Each actor that is fired is then postfired once at the
     *  conclusion of the iteration.
     *  </p><p>
     *  If there are no events in the event queue, then the behavior
     *  depends on the <i>stopWhenQueueIsEmpty</i> parameter. If it is
     *  false, then this thread will stall until events become
     *  available in the event queue. Otherwise, time will advance to
     *  the stop time and the execution will halt.</p>
     *
     *  @exception IllegalActionException If the firing actor throws it, or
     *   event queue is not ready, or an event is missed, or time is set
     *   backwards.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("========= PtidesBasicDirector fires at " + getModelTime()
                    + "  with microstep as " + _microstep);
        }

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        while (true) {
            // Find the next actor to be fired.
            Actor actorToFire = _getNextActorToFire();

            // Check whether the actor to be fired is null.
            // -- If the actor to be fired is null,
            // There are two conditions that the actor to be fired
            // can be null.
            if (actorToFire == null) {
                if (_isTopLevel()) {
                    // Case 1:
                    // If this director is an executive director at
                    // the top level, a null actor means that there are
                    // no events in the event queue.
                    if (_debugging) {
                        _debug("No more events in the event queue.");
                    }

                    // Setting the following variable to true makes the
                    // postfire method return false.
                    // Do not do this if _stopFireRequested is true,
                    // since there may in fact be actors to fire, but
                    // their firing has been deferred.
                    if (!_stopFireRequested) {
                        _noMoreActorsToFire = true;
                    }
                } else {
                    // Case 2:
                    // If this director belongs to an opaque composite model,
                    // which is not at the top level, the director may be
                    // invoked by an update of an external parameter port.
                    // Therefore, no actors contained by the composite model
                    // need to be fired.
                    // NOTE: There may still be events in the event queue
                    // of this director that are scheduled for future firings.
                    if (_debugging) {
                        _debug("No actor requests to be fired "
                                + "at the current tag.");
                    }
                }
                // Nothing more needs to be done in the current iteration.
                // Simply return.
                // Since we are now actually stopping the firing, we can set this false.
                _stopFireRequested = false;
                return;
            }

            // -- If the actor to be fired is not null.
            // If the actor to be fired is the container of this director,
            // the next event to be processed is in an inside receiver of
            // an output port of the container. In this case, this method
            // simply returns, and gives the outside domain a chance to react
            // to that event.
            // NOTE: Topological sort always assigns the composite actor the
            // lowest priority. This guarantees that all the inside actors
            // have fired (reacted to their triggers) before the composite
            // actor fires.
            if (actorToFire == getContainer()) {
                // Since we are now actually stopping the firing, we can set this false.
                _stopFireRequested = false;
                return;
            }

            if (_debugging) {
                _debug("****** Actor to fire: " + actorToFire.getFullName());
            }

            // Keep firing the actor to be fired until there are no more input
            // tokens available in any of its input ports with the same tag, or its prefire()
            // method returns false.
            boolean refire;

            do {
                refire = false;

                // NOTE: There are enough tests here against the
                // _debugging variable that it makes sense to split
                // into two duplicate versions.
                if (_debugging) {
                    // Debugging. Report everything.
                    // If the actor to be fired is not contained by the container,
                    // it may just be deleted. Put this actor to the
                    // list of disabled actors.
                    if (!((CompositeEntity) getContainer())
                            .deepContains((NamedObj) actorToFire)) {
                        _debug("Actor no longer under the control of this director. Disabling actor.");
                        _disableActor(actorToFire);
                        break;
                    }

                    _debug(new FiringEvent(this, actorToFire,
                            FiringEvent.BEFORE_PREFIRE));

                    if (!actorToFire.prefire()) {
                        _debug("*** Prefire returned false.");
                        break;
                    }

                    _debug(new FiringEvent(this, actorToFire,
                            FiringEvent.AFTER_PREFIRE));

                    _debug(new FiringEvent(this, actorToFire,
                            FiringEvent.BEFORE_FIRE));
                    actorToFire.fire();
                    _debug(new FiringEvent(this, actorToFire,
                            FiringEvent.AFTER_FIRE));

                    _debug(new FiringEvent(this, actorToFire,
                            FiringEvent.BEFORE_POSTFIRE));

                    if (!actorToFire.postfire()) {
                        _debug("*** Postfire returned false:",
                                ((Nameable) actorToFire).getName());

                        // This actor requests not to be fired again.
                        _disableActor(actorToFire);
                        break;
                    }

                    _debug(new FiringEvent(this, actorToFire,
                            FiringEvent.AFTER_POSTFIRE));
                } else {
                    // No debugging.
                    // If the actor to be fired is not contained by the container,
                    // it may just be deleted. Put this actor to the
                    // list of disabled actors.
                    if (!((CompositeEntity) getContainer())
                            .deepContains((NamedObj) actorToFire)) {
                        _disableActor(actorToFire);
                        break;
                    }

                    if (!actorToFire.prefire()) {
                        break;
                    }

                    actorToFire.fire();

                    // NOTE: It is the fact that we postfire actors now that makes
                    // this director not comply with the actor abstract semantics.
                    // However, it's quite a redesign to make it comply, and the
                    // semantics would not be backward compatible. It really needs
                    // to be a new director to comply.
                    if (!actorToFire.postfire()) {
                        // This actor requests not to be fired again.
                        _disableActor(actorToFire);
                        break;
                    }
                }

                // Check all the input ports of the actor to see whether there
                // are more input tokens to be processed.
                // FIXME: This particular situation can only occur if either the
                // actor failed to consume a token, or multiple
                // events with the same destination were queued with the same tag.
                // In theory, both are errors. One possible fix for the latter
                // case would be to requeue the token with a larger microstep.
                // A possible fix for the former (if we can detect it) would
                // be to throw an exception. This would be far better than
                // going into an infinite loop.
                Iterator<?> inputPorts = actorToFire.inputPortList().iterator();

                while (inputPorts.hasNext() && !refire) {
                    IOPort port = (IOPort) inputPorts.next();

                    // iterate all the channels of the current input port.
                    for (int i = 0; i < port.getWidth(); i++) {
                        if (port.hasToken(i)) {
                            refire = true;

                            // Found a channel that has input data,
                            // jump out of the for loop.
                            break;
                        }
                    }
                }
            } while (refire); // close the do {...} while () loop
            // NOTE: On the above, it would be nice to be able to
            // check _stopFireRequested, but this doesn't actually work.
            // In particular, firing an actor may trigger a call to stopFire(),
            // for example if the actor makes a change request, as for example
            // an FSM actor will do.  This will prevent subsequent firings,
            // incorrectly.

            // The following code enforces that a firing of a
            // DE director only handles events with the same tag.
            // If the earliest event in the event queue is in the future,
            // this code terminates the current iteration.
            // This code is applied on both embedded and top-level directors.
            synchronized (_eventQueue) {
                if (!_eventQueue.isEmpty()) {
                    DEEvent next = _eventQueue.get();

                    if ((next.timeStamp().compareTo(getModelTime()) != 0)) {
                        // If the next event is in the future time,
                        // jump out of the big while loop and
                        // proceed to postfire().
                        // NOTE: we reset the microstep to 0 because it is
                        // the contract that if the event queue has some events
                        // at a time point, the first event must have the
                        // microstep as 0. See the
                        // _enqueueEvent(Actor actor, Time time) method.
                        _microstep = 0;
                        break;
                    } else if (next.microstep() != _microstep) {
                        // If the next event is has a different microstep,
                        // jump out of the big while loop and
                        // proceed to postfire().
                        break;
                    } else {
                        // The next event has the same tag as the current tag,
                        // indicating that at least one actor is going to be
                        // fired at the current iteration.
                        // Continue the current iteration.
                    }
                }
            }
        } // Close the BIG while loop.

        // Since we are now actually stopping the firing, we can set this false.
        _stopFireRequested = false;

        if (_debugging) {
            _debug("PtidesBasicDirector fired!");
        }
    }

    /** Initialize the actors and request a refiring at the current
     *  time of the executive director. This overrides the base class to
     *  throw an exception if there is no executive director.
     *  @exception IllegalActionException If the superclass throws
     *   it or if there is no executive director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentlyExecutingStack = new Stack<DoubleTimedEvent>();
        _realTimeOutputEventQueue = new PriorityQueue<RealTimeEvent>();
        _realTimeInputEventQueue = new PriorityQueue<RealTimeEvent>();
        _LastConsumedTag = new HashMap<NamedObj, Tag>();
        _physicalTimeExecutionStarted = null;

        // _calculateModelTimeOffsets();

        NamedObj container = getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(this,
                    "No container, or container is not an Actor.");
        }
        Director executiveDirector = ((Actor) container).getExecutiveDirector();
        if (executiveDirector == null) {
            throw new IllegalActionException(this,
                    "The PtidesBasicDirector can only be used within an enclosing director.");
        }
        executiveDirector.fireAtCurrentTime((Actor) container);

        _setIcon(_getIdleIcon(), true);
    }

    /** Return a new receiver of the type PtidesBasicReceiver.
     *  @return A new PtidesBasicReceiver.
     */
    public Receiver newReceiver() {
        if (_debugging && _verbose) {
            _debug("Creating a new PTIDES basic receiver.");
        }

        return new PtidesBasicReceiver();
    }

    /** Uses the preinitialize() method in the super class.
     *  However we use the DEListEventQueue instead of the calendar queue because we need
     *  to access to not just the first event in the event queue.
     *  Also parameters minDelay is calculated here.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Initialize an event queue.
        _eventQueue = new DEListEventQueue();
        _calculateMinDelayOffsets();
        // In Ptides, we should never stop when queue is empty...
        stopWhenQueueIsEmpty.setExpression("false");
    }

    /** Return false if there are no more actors to be fired or the stop()
     *  method has been called.
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If stopWhenQueueIsEmpty parameter
     *   does not contain a valid token, or refiring can not be requested.
     */
    public boolean postfire() throws IllegalActionException {
        // Do not call super.postfire() because that requests a
        // refiring at the next event time on the event queue.

        Boolean result = !_stopRequested;
        if (getModelTime().compareTo(getModelStopTime()) >= 0) {
            // If there is a still event on the event queue with time stamp
            // equal to the stop time, we want to process that event before
            // we declare that we are done.
            if (!_eventQueue.get().timeStamp().equals(getModelStopTime())) {
                result = false;
            }
        }
        return result;
    }

    /** Override the base class to not set model time to that of the
     *  enclosing director. This method always returns true, deferring the
     *  decision about whether to fire an actor to the fire() method.
     *  @return True.
     */
    public boolean prefire() {
        // Do not invoke the superclass prefire() because that
        // sets model time to match the enclosing director's time.
        if (_debugging) {
            _debug("Prefiring: Current time is: " + getModelTime());
        }
        return true;
    }

    /** This method allows the microstep to be set to some value.
     *  This method should be used with extreme caution. This method should
     *  only be used when events from outside of the platform arrive, and
     *  by coincidence the current model time is equal to the model time
     *  of the event. In which case the microstep should be set to 0.
     *  @see #getMicrostep
     *  @param microstep An int specifying the microstep value.
     */
    public void setMicrostep(int microstep) {
        _microstep = microstep;
    }

    /** Set a new value to the current time of the model, where
     *  the new time _can_ be smaller than the current model time,
     *  because PTIDES allow events to be processed out of timestamp order.
     *
     *  @exception IllegalActionException If the new time is less than
     *   the current time returned by getCurrentTime().
     *  @param newTime The new current simulation time.
     *  @see #getModelTime()
     */
    public void setModelTime(Time newTime) throws IllegalActionException {
        int comparisonResult = _currentTime.compareTo(newTime);

        if (comparisonResult > 0) {
            if (_debugging) {
                _debug("==== Set current time backwards from " + getModelTime()
                        + " to: " + newTime);
            }
        } else if (comparisonResult < 0) {
            if (_debugging) {
                _debug("==== Set current time to: " + newTime);
            }
        } else {
            // the new time is equal to the current time, do nothing.
        }
        _currentTime = newTime;
    }

    /** Set the timestamp and microstep of the current time.
     *  @param timestamp A Time object specifying the timestamp.
     *  @param microstep An int specifying the microstep.
     *  @exception IllegalActionException if setModelTime() throws it.
     *  @see #setModelTime(Time)
     */
    public void setTag(Time timestamp, int microstep)
            throws IllegalActionException {
        setModelTime(timestamp);
        setMicrostep(microstep);
    }

    /** Override the base class to reset the icon idle if animation
     *  is turned on.
     *  @exception IllegalActionException If the wrapup() method of
     *  one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _setIcon(_getIdleIcon(), false);
        if (_lastExecutingActor != null) {
            _clearHighlight(_lastExecutingActor);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ////

    /** The list of currently executing actors and their remaining execution time.
     */
    protected Stack<DoubleTimedEvent> _currentlyExecutingStack;

    /** Zero time.
     */
    protected Time _zero;

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Causality analysis that happens at the preinitialization phase.
     *  The goal is to annotate each port with a minDelay parameter,
     *  which is the offset used for safe to process analysis.
     *  <p>
     *  Start from each input port that is connected to the outside of the platform
     *  (These input ports indicate sensors and network interfaces, call them startPorts),
     *  and traverse the graph until we reach the output port connected to the outside of
     *  the platform (actuators). For each input port in between, annotate it with
     *  a minDelay parameter. This parameter is an array of doubles, where each double
     *  corresponds to the minimum delay offset for a particular channel of that port.
     *  This minimum delay offset is used for the safe to process analysis.
     *  </p>
     *  FIXME: currently this algorithm does not support transparent composite actors,
     *  but it should.
     *  @exception IllegalActionException
     */
    protected void _calculateMinDelayOffsets() throws IllegalActionException {

        _clearMinDelayOffsets();

        // A map that saves a dependency for a port channel pair. This dependency is later used to
        // calculate minDelay.
        Map inputModelTimeDelays = new HashMap<IOPort, Map<Integer, SuperdenseDependency>>();

        // NOTE: In portDelays and localPortDelays, which saves the corresponding model time delays
        // for real-time ports to a particular port, we act as if each starting input port (which
        // is a real-time port) and each output
        // port in the graph to to have width 1, even though their width might be of some other
        // value. We do this because each output port and starting port do no need to be annotated
        // with the minDelay parameter, and we assume all dependencies at each port is the same for
        // all channels. For regular input ports however, their width is correctly reflected in
        // portDelays and localPortDelays, because we need all model time delays to each channel
        // to calculate the minDelay paramter.

        // FIXME: If there are composite actors within the top level composite actor,
        // does this algorithm work?
        // initialize all port model delays to infinity.
        HashMap portDelays = new HashMap<IOPort, SuperdenseDependency>();
        for (Actor actor : (List<Actor>) (((TypedCompositeActor) getContainer())
                .deepEntityList())) {
            for (TypedIOPort inputPort : (List<TypedIOPort>) (actor
                    .inputPortList())) {
                portDelays.put(inputPort, SuperdenseDependency.OPLUS_IDENTITY);
            }
            for (TypedIOPort outputPort : (List<TypedIOPort>) (actor
                    .outputPortList())) {
                portDelays.put(outputPort, SuperdenseDependency.OPLUS_IDENTITY);
            }
        }

        for (TypedIOPort inputPort : (List<TypedIOPort>) (((Actor) getContainer())
                .inputPortList())) {
            SuperdenseDependency startDelay = SuperdenseDependency.valueOf(
                    -_getRealTimeDelay(inputPort), 0);
            portDelays.put(inputPort, startDelay);
        }
        // Now start from each sensor (input port at the top level), traverse through all
        // ports.
        for (TypedIOPort startPort : (List<TypedIOPort>) (((TypedCompositeActor) getContainer())
                .inputPortList())) {
            // Setup a local priority queue to store all reached ports
            HashMap localPortDelays = new HashMap<IOPort, SuperdenseDependency>(
                    portDelays);

            PriorityQueue distQueue = new PriorityQueue<PortDependency>();
            distQueue.add(new PortDependency(startPort,
                    (SuperdenseDependency) localPortDelays.get(startPort)));

            // Dijkstra's algorithm to find all shortest time delays.
            while (!distQueue.isEmpty()) {
                PortDependency portDependency = (PortDependency) distQueue
                        .remove();
                IOPort port = (IOPort) portDependency.port;
                SuperdenseDependency prevDependency = (SuperdenseDependency) portDependency.dependency;
                Actor actor = (Actor) port.getContainer();
                if (port.isInput() && port.isOutput()) {
                    throw new IllegalActionException(
                            "the causality analysis cannot deal with"
                                    + "port that are both input and output");
                }
                // we do not want to traverse to the outside of the platform.
                if (actor != getContainer()) {
                    if (port.isInput()) {
                        Collection<IOPort> outputs = _finiteDependentPorts(port);
                        for (IOPort outputPort : outputs) {
                            SuperdenseDependency minimumDelay = (SuperdenseDependency) _getDependency(
                                    port, outputPort);
                            // FIXME: what do we do with the microstep portion of the dependency?
                            // need to make sure we did not visit this port before.
                            SuperdenseDependency modelTime = (SuperdenseDependency) prevDependency
                                    .oTimes(minimumDelay);
                            if (((SuperdenseDependency) localPortDelays
                                    .get(outputPort)).compareTo(modelTime) > 0) {
                                localPortDelays.put(outputPort, modelTime);
                                distQueue.add(new PortDependency(outputPort,
                                        modelTime));
                            }
                        }
                    } else { // port is an output port
                        // For each receiving port channel pair, add the dependency in inputModelTimeDelays.
                        // We do not need to check whether there already exists a dependency because if
                        // a dependency already exists for that pair, that dependency must have a greater
                        // value, meaning it should be replaced. This is because the output port that
                        // led to that pair would not be in distQueue if it the dependency was smaller.
                        Receiver[][] remoteReceivers = port
                                .getRemoteReceivers();
                        for (int i = 0; i < remoteReceivers.length; i++) {
                            for (int j = 0; j < remoteReceivers[i].length; j++) {
                                IOPort sinkPort = remoteReceivers[i][j]
                                        .getContainer();
                                int channel = sinkPort
                                        .getChannelForReceiver(remoteReceivers[i][j]);
                                // we do not want to traverse to the outside of the platform.
                                if (sinkPort.getContainer() != getContainer()) {
                                    // for this port channel pair, add the dependency.
                                    Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) inputModelTimeDelays
                                            .get(sinkPort);
                                    if (channelDependency == null) {
                                        channelDependency = new HashMap<Integer, SuperdenseDependency>();
                                    }
                                    channelDependency.put(Integer
                                            .valueOf(channel), prevDependency);
                                    inputModelTimeDelays.put(sinkPort,
                                            channelDependency);
                                    // After updating dependencies, we need to decide whether we should keep traversing
                                    // the graph.
                                    if (((SuperdenseDependency) localPortDelays
                                            .get(sinkPort))
                                            .compareTo(prevDependency) > 0) {
                                        localPortDelays.put(sinkPort,
                                                prevDependency);
                                        distQueue.add(new PortDependency(
                                                sinkPort, prevDependency));
                                    }
                                }
                            }
                        }
                    }
                } else if (port == startPort) {
                    // The (almost) same code (except for getting receivers) is used if the
                    // port is a startPort or an output port.
                    // This does not support input/output port, should it?
                    Receiver[][] deepReceivers = port.deepGetReceivers();
                    for (int i = 0; i < deepReceivers.length; i++) {
                        for (int j = 0; j < deepReceivers[i].length; j++) {
                            IOPort sinkPort = deepReceivers[i][j]
                                    .getContainer();
                            int channel = sinkPort
                                    .getChannelForReceiver(deepReceivers[i][j]);
                            // we do not want to traverse to the outside of the deepReceivers.
                            if (sinkPort.getContainer() != getContainer()) {
                                // for this port channel pair, add the dependency.
                                Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) inputModelTimeDelays
                                        .get(sinkPort);
                                if (channelDependency == null) {
                                    channelDependency = new HashMap<Integer, SuperdenseDependency>();
                                }
                                channelDependency.put(Integer.valueOf(channel),
                                        prevDependency);
                                inputModelTimeDelays.put(sinkPort,
                                        channelDependency);
                                // After updating dependencies, we need to decide whether we should keep traversing
                                // the graph.
                                if (((SuperdenseDependency) localPortDelays
                                        .get(sinkPort))
                                        .compareTo(prevDependency) > 0) {
                                    localPortDelays.put(sinkPort,
                                            prevDependency);
                                    distQueue.add(new PortDependency(sinkPort,
                                            prevDependency));
                                }
                            }
                        }
                    }
                }
            }
            portDelays = localPortDelays;
        }

        // inputModelTimeDelays is the delays as calculated through shortest path algorithm. Now we
        // need to use these delays to calculate the minDelay, which is calculated as follows:
        // For each port, get all finite equivalent ports except itself. Now for a particular port
        // channel pair, Find the smallest model time delay among all of the channels on all these
        // ports, if they exist. that smallest delay is  the minDelay for that port channel pair.
        // If this smallest value does not exist, then the event arriving at this port channel pair
        // is always safe to process, thus minDelay does not change (it was by default set to
        // double.POSITIVE_INFINITY.
        for (IOPort inputPort : (Set<IOPort>) inputModelTimeDelays.keySet()) {
            Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) inputModelTimeDelays
                    .get(inputPort);
            //            Map<Integer, SuperdenseDependency> minDelays = new HashMap<Integer, SuperdenseDependency>();
            double[] minDelays = new double[channelDependency.size()];
            for (Integer portChannelMinDelay : channelDependency.keySet()) {
                minDelays[portChannelMinDelay.intValue()] = _getMinDelayForPortChannel(
                        inputPort, portChannelMinDelay, inputModelTimeDelays);
            }
            _setMinDelay(inputPort, minDelays);
        }
    }

    /** Clear any highlights on the specified actor.
     *  @param actor The actor to clear.
     *  @exception IllegalActionException If the animateExecution
     *   parameter cannot be evaluated.
     */
    protected void _clearHighlight(Actor actor) throws IllegalActionException {
        if (((BooleanToken) animateExecution.getToken()).booleanValue()) {
            String completeMoML = "<deleteProperty name=\"_highlightColor\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    (NamedObj) actor, completeMoML);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor) container).requestChange(request);
        }
    }

    /** For each input port within the composite actor where this director resides,
     *  if the input port has a minDelay parameter, set the value of that parameter
     *  to Infinity (meaning events arriving at this port will always be safe to
     *  process). If it does not have a minDelay parameter, then do nothing, because
     *  _safeToProcess also interpret that as events are always safe to process in
     *  that case.
     *  @exception IllegalActionException
     */
    protected void _clearMinDelayOffsets() throws IllegalActionException {
        for (Actor actor : (List<Actor>) (((TypedCompositeActor) getContainer())
                .deepEntityList())) {
            for (TypedIOPort inputPort : (List<TypedIOPort>) (actor
                    .inputPortList())) {
                Parameter parameter = (Parameter) (inputPort)
                        .getAttribute("minDelay");
                if (parameter != null) {
                    int channels = inputPort.getWidth();
                    if (channels > 0) {
                        Token[] tokens = new Token[channels];
                        for (int i = 0; i < channels; i++) {
                            tokens[i] = new DoubleToken(
                                    Double.POSITIVE_INFINITY);
                        }
                        parameter.setToken(new ArrayToken(tokens));
                    }
                }
            }
        }
    }

    /** Put a trigger event into the event queue.
     *  <p>
     *  The trigger event has the same timestamp as that of the director.
     *  The microstep of this event is always equal to the current microstep
     *  of this director. The depth for the queued event is the
     *  depth of the destination IO port. Finally, the token and the receiver
     *  this token is destined for are also stored in the event.
     *  </p><p>
     *  If the event queue is not ready or the actor contains the destination
     *  port is disabled, do nothing.</p>
     *
     *  @param ioPort The destination IO port.
     *  @param token The token associated with this event.
     *  @param receiver The receiver the event is destined to.
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
                    ((NamedObj) actor).getName(), " time = " + getModelTime()
                            + " microstep = " + _microstep + " depth = "
                            + depth);
        }

        // Register this trigger event.
        DEEvent newEvent = new DETokenEvent(ioPort, ioPort
                .getChannelForReceiver(receiver), getModelTime(), _microstep,
                depth, token, receiver);
        _eventQueue.put(newEvent);
    }

    /** Return a collection of ports the given port is dependent on, within
     *  the same actor.
     *  This method delegates to the getDependency() method of the corresponding
     *  causality interface the actor is associated with. If getDependency()
     *  returns a dependency not equal to the oPlusIdentity, then the associated
     *  port is added to the Collection.
     *  The returned Collection has no duplicate entries.
     *  @see #_finiteDependentPorts(IOPort)
     *
     *  @param port The given port to find finite dependent ports.
     *  @return Collection of finite dependent ports.
     *  @exception IllegalActionException
     */
    protected static Collection<IOPort> _finiteDependentPorts(IOPort port)
            throws IllegalActionException {
        // FIXME: This does not support ports that are both input and output.
        // Should it?
        Collection<IOPort> result = new HashSet<IOPort>();
        Actor actor = (Actor) port.getContainer();
        CausalityInterface actorCausalityInterface;
        if (actor instanceof AtomicActor) {
            actorCausalityInterface = (DefaultCausalityInterface) actor
                    .getCausalityInterface();
        } else if (actor instanceof CompositeActor) {
            actorCausalityInterface = (CausalityInterfaceForComposites) actor
                    .getCausalityInterface();
        } else {
            throw new IllegalActionException(actor,
                    "Actor is not a typed atomic or typed composite "
                            + "actor, do not know how to deal with it.");
        }
        if (port.isInput()) {
            List<IOPort> outputs = ((Actor) port.getContainer())
                    .outputPortList();
            for (IOPort output : outputs) {
                Dependency dependency = actorCausalityInterface.getDependency(
                        port, output);
                if (dependency != null
                        && dependency.compareTo(dependency.oPlusIdentity()) != 0) {
                    result.add(output);
                }
            }
        } else { // port is output port.
            List<IOPort> inputs = ((Actor) port.getContainer()).inputPortList();
            for (IOPort input : inputs) {
                Dependency dependency = actorCausalityInterface.getDependency(
                        input, port);
                if (dependency != null
                        && dependency.compareTo(dependency.oPlusIdentity()) != 0) {
                    result.add(input);
                }
            }
        }
        return result;
    }

    /** Return a collection of ports that are finite equivalent ports
     *  of the input port.
     *  <p>
     *  A finite equivalence class is defined as follows.
     *  If input ports X and Y each have a dependency not equal to the
     *  default depenency's oPlusIdentity() on any common port
     *  or on two equivalent ports
     *  or on the state of the associated actor, then they
     *  are in a finite equivalence class.
     *  The returned Collection has no duplicate entries.
     *  If the port is not an input port, an exception
     *  is thrown.
     *
     *  @param input The input port.
     *  @return Collection of finite equivalent ports.
     *  @exception IllegalActionException
     */
    protected static Collection<IOPort> _finiteEquivalentPorts(IOPort input)
            throws IllegalActionException {
        Collection<IOPort> result = new HashSet<IOPort>();
        // first get all outputs that are dependent on this input.
        Collection<IOPort> outputs = _finiteDependentPorts(input);
        // now for every input that is also dependent on the output, add
        // it to the list of ports that are returned.
        for (IOPort output : outputs) {
            result.addAll(_finiteDependentPorts(output));
        }
        return result;
    }

    /** Return the absolute deadline of this event, if this event is a trigger event.
     *  If this event is however a pure event, this event inherits the deadline of
     *  the first port on the same actor. If this actor does not have any port, then
     *  an exception is thrown.
     *  FIXME: this is sort of a hack.
     *  @param event Event to find deadline for.
     *  @return deadline of this event.
     *  @exception IllegalActionException
     */
    protected Time _getAbsoluteDeadline(DEEvent event)
            throws IllegalActionException {
        IOPort port = event.ioPort();
        if (port == null) {
            // event is a pure event. So it always has the smallest deadline.
            return Time.NEGATIVE_INFINITY;
        }
        return event.timeStamp().add(_getRelativeDeadline(port));
    }

    /** Return all the events in the event queue that are of the same tag as the event
     *  passed in.
     *  <p>
     *  Notice these events should _NOT_ be taken out of the event queue.
     *  @param event The reference event.
     *  @return List of events of the same tag.
     *  @exception IllegalActionException
     */
    protected List<DEEvent> _getAllSameTagEventsFromQueue(DEEvent event)
            throws IllegalActionException {
        List<DEEvent> eventList = new ArrayList<DEEvent>();
        for (int eventIndex = 0; eventIndex < _eventQueue.size(); eventIndex++) {
            DEEvent nextEvent = ((DEListEventQueue) _eventQueue)
                    .get(eventIndex);
            if (nextEvent.hasTheSameTagAs(event)
                    && nextEvent.actor() == event.actor()) {
                eventList.add(nextEvent);
            } else {
                break;
            }
        }
        return eventList;
    }

    /** Get the dependency between the input and output ports. If the
     *  ports does not belong to the same actor, an exception is thrown.
     *  Depending on the actor, the corresponding getDependency() method in
     *  the actor's causality interface is called.
     *  @param input The input port.
     *  @param output The output port.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     *  @exception IllegalActionException If the ports do not belong to the
     *  same actor.
     *
     */
    protected static Dependency _getDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        Actor actor = (Actor) input.getContainer();
        if (output != null) {
            Actor outputActor = (Actor) output.getContainer();
            if (actor != outputActor) {
                throw new IllegalActionException(
                        input,
                        output,
                        "Cannot get dependency"
                                + "from these two ports, becasue they do not belong"
                                + "to the same actor.");
            }
        }
        CausalityInterface causalityInterface = actor.getCausalityInterface();
        return causalityInterface.getDependency(input, output);
    }

    /** Return a MoML string describing the icon appearance for a Ptides
     *  director that is currently executing the specified actor.
     *  The returned MoML can include a sequence of instances of VisibleAttribute
     *  or its subclasses. In this base class, this returns a rectangle like
     *  the usual director green rectangle used by default for directors,
     *  but filled with red instead of green.
     *  @see VisibleAttribute
     *  @param actorExecuting The actor that's exeucting.
     *  @return A MoML string.
     *  @exception IllegalActionException If the animateExecution parameter cannot
     *   be evaluated.
     */
    protected String _getExecutingIcon(Actor actorExecuting)
            throws IllegalActionException {
        _highlightActor(actorExecuting, "{0.0, 0.0, 1.0, 1.0}");
        return "  <property name=\"rectangle\" class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">"
                + "    <property name=\"height\" value=\"30\"/>"
                + "    <property name=\"fillColor\" value=\"{0.0, 0.0, 1.0, 1.0}\"/>"
                + "  </property>";
    }

    /** Returns the executionTime parameter.
     *  @param port
     *  @return executionTime parameter
     *  @exception IllegalActionException
     */
    protected double _getExecutionTime(IOPort port)
            throws IllegalActionException {
        Double result = PtidesActorProperties.getExecutionTime(port);
        if (result != null) {
            return result;
        } else {
            return PtidesActorProperties.getExecutionTime((Actor) port
                    .getContainer());
        }
    }

    /** Return a MoML string describing the icon appearance for an idle
     *  director. This can include a sequence of instances of VisibleAttribute
     *  or its subclasses. In this base class, this returns a rectangle like
     *  the usual director green rectangle used by default for directors.
     *  @see VisibleAttribute
     *  @return A MoML string.
     */
    protected String _getIdleIcon() {
        return "  <property name=\"rectangle\" class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">"
                + "    <property name=\"height\" value=\"30\"/>"
                + "    <property name=\"fillColor\" value=\"{0.0, 1.0, 0.0, 1.0}\"/>"
                + "  </property>";
    }

    /** Returns the minDelay parameter.
     *  @param port The port where this minDelay parameter is associated to.
     *  @param channel The channel where this minDelay parameter is associated to.
     *  @return minDelay parameter.
     *  @exception IllegalActionException if we cannot get minDelay parameter.
     */
    protected double _getMinDelay(IOPort port, int channel)
            throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("minDelay");
        if (parameter != null) {
            DoubleToken token = ((DoubleToken) ((ArrayToken) parameter
                    .getToken()).arrayValue()[channel]);
            if (token != null) {
                return token.doubleValue();
            } else {
                return Double.POSITIVE_INFINITY;
            }
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    /** Return the actor to fire in this iteration, or null if no actor
     *  should be fired.
     *  In this base class, this method first checks whether the top event from
     *  the event queue is destined for an actuator. If it is, then we check
     *  if physical time has reached the timestamp of the actuation event. If it
     *  has, then we fire the actuator. If it has not, then we take the actuator
     *  event from the event queue and put it onto the _realTimeEventQueue, and
     *  call fireAt() of the executive director. We then check if a real-time event
     *  should be processed by looking at the top event of the
     *  _realTimeEventQueue. If there is on that should be fired, that
     *  actor is returned for firing. If not, we go on and considers two
     *  cases, depending whether there is an actor currently executing,
     *  as follows:
     *  <p>
     *  <b>Case 1</b>: If there is no actor currently
     *  executing, then this method checks the event queue and returns
     *  null if it is empty. If it is not empty, it checks the destination actor of the
     *  earliest event on the event queue, and if it has a non-zero execution
     *  time, then it pushes it onto the currently executing stack and
     *  returns null. Otherwise, if the execution time of the actor is
     *  zero, it sets the current model time to the time stamp of
     *  that earliest event and returns that actor.
     *  <p>
     *  <b>Case 2</b>: If there is an actor currently executing, then this
     *  method checks whether it has a remaining execution time of zero.
     *  If it does, then it returns the currently executing actor.
     *  If it does not, then it checks whether
     *  the earliest event on the event queue should
     *  preempt it (by invoking _preemptExecutingActor()),
     *  and if so, checks the destination actor of that event
     *  and removes the event from the event queue. If that destination
     *  actor has an execution time of zero, then it sets the current
     *  model time to the time stamp of that event, and returns that actor.
     *  Else if the destination actor has an execution time of bigger than
     *  zero, then it calls fireAt()
     *  on the enclosing director passing it the time it expects the currently
     *  executing actor to finish executing, and returns null.
     *  If there is no
     *  event on the event queue or that event should not preempt the
     *  currently executing actor, then it calls fireAt()
     *  on the enclosing director passing it the time it expects the currently
     *  executing actor to finish executing, and returns null.
     *  @return The next actor to be fired, which can be null.
     *  @exception IllegalActionException If event queue is not ready, or
     *  an event is missed, or time is set backwards, or if the enclosing
     *  director does not respect the fireAt call.
     *  @see #_preemptExecutingActor()
     */
    protected Actor _getNextActorToFire() throws IllegalActionException {
        // FIXME: This method changes persistent state, yet it is called in fire().
        // This means that this director cannot be used inside a director that
        // does a fixed point iteration, which includes (currently), Continuous
        // and CT and SR, but in the future may also include DE.
        Time physicalTime = _getPhysicalTime();
        Actor container = (Actor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();

        if (!_currentlyExecutingStack.isEmpty()) {
            // Case 2: We are currently executing an actor.
            DoubleTimedEvent currentEventList = (DoubleTimedEvent) _currentlyExecutingStack
                    .peek();
            // First check whether its remaining execution time is zero.
            Time remainingExecutionTime = currentEventList.remainingExecutionTime;
            Time finishTime = _physicalTimeExecutionStarted
                    .add(remainingExecutionTime);
            int comparison = finishTime.compareTo(physicalTime);
            if (comparison < 0) {
                // NOTE: This should not happen, so if it does, throw an exception.
                throw new IllegalActionException(
                        this,
                        _getActorFromEventList((List<DEEvent>) currentEventList.contents),
                        "Physical time passed the finish time of the currently executing actor");
            } else if (comparison == 0) {
                // Currently executing actor finishes now, so we want to return it.
                // First set current model time.
                setTag(currentEventList.timeStamp, currentEventList.microstep);
                _currentlyExecutingStack.pop();
                // If there is now something on _currentlyExecutingStack,
                // then we are resuming its execution now.
                _physicalTimeExecutionStarted = physicalTime;

                if (_debugging) {
                    _debug("Actor "
                            + _getActorFromEventList(
                                    (List<DEEvent>) currentEventList.contents)
                                    .getName(getContainer())
                            + " finishes executing at physical time "
                            + physicalTime);
                }

                // Animate, if appropriate.
                _setIcon(_getIdleIcon(), false);
                _clearHighlight(_getActorFromEventList((List<DEEvent>) currentEventList.contents));
                _lastExecutingActor = null;

                // Request a refiring so we can process the next event
                // on the event queue at the current physical time.
                executiveDirector.fireAtCurrentTime((Actor) container);

                return _getActorFromEventList((List<DEEvent>) currentEventList.contents);
            } else {
                Time nextEventOnStackFireTime = _currentlyExecutingStack.peek().remainingExecutionTime;
                Time expectedCompletionTime = nextEventOnStackFireTime
                        .add(_physicalTimeExecutionStarted);
                Time fireAtTime = executiveDirector.fireAt(container,
                        expectedCompletionTime);
                if (!fireAtTime.equals(expectedCompletionTime)) {
                    throw new IllegalActionException(
                            executiveDirector,
                            "Ptides director requires refiring at time "
                                    + expectedCompletionTime
                                    + ", but the enclosing director replied that it will refire at time "
                                    + fireAtTime);
                }
                // Currently executing actor needs more execution time.
                // Decide whether to preempt it.
                if (_eventQueue.isEmpty() || !_preemptExecutingActor()) {
                    // Either the event queue is empty or the
                    // currently executing actor does not get preempted
                    // and it has remaining execution time. We should just
                    // return because we previously called fireAt() with
                    // the expected completion time, so we will be fired
                    // again at that time. There is no need to change
                    // the remaining execution time on the stack nor
                    // the _physicalTimeExecutionStarted value because
                    // those will be checked when we are refired.
                    return null;
                }
            }
        }

        // If we get here, then we want to execute the actor destination
        // of the earliest event on the event queue, either because there
        // is no currently executing actor or the currently executing actor
        // got preempted.
        if (_eventQueue.isEmpty()) {
            // Nothing to fire.

            // Animate if appropriate.
            _setIcon(_getIdleIcon(), false);

            return null;
        }

        DEEvent eventFromQueue = _getNextSafeEvent();
        if (eventFromQueue == null) {
            // no event is there to process.
            return null;
        }
        //            DEEvent eventFromQueue = _eventQueue.get();
        Time timeStampOfEventFromQueue = eventFromQueue.timeStamp();
        int microstepOfEventFromQueue = eventFromQueue.microstep();

        // Every time safeToProcess analysis passes, and
        // a new actor is chosen to be start processing, we update _actorLastConsumedTag
        // to store the tag of the last event that was consumed by this actor. This helps us to
        // track if safeToProcess() somehow failed to produce the correct results.
        _trackLastTagConsumedByActor(eventFromQueue);

        List<DEEvent> eventsToProcess = _getAllSameTagEventsFromQueue(eventFromQueue);

        Actor actorToFire = _getNextActorToFireForTheseEvents(eventsToProcess);

        IOPort ioport = eventFromQueue.ioPort();
        if (ioport == null) {
            List<IOPort> inPortList = eventFromQueue.actor().inputPortList();
            ioport = inPortList.get(0);
        }
        Time executionTime = new Time(this, _getExecutionTime(ioport));

        if (executionTime.compareTo(_zero) == 0) {
            // If execution time is zero, return the actor.
            // It will be fired now.
            setTag(timeStampOfEventFromQueue, microstepOfEventFromQueue);

            // Request a refiring so we can process the next event
            // on the event queue at the current physical time.
            executiveDirector.fireAtCurrentTime((Actor) container);

            return actorToFire;
        } else {
            // Execution time is not zero. Push the execution onto
            // the stack, call fireAt() on the enclosing director,
            // and return null.

            Time expectedCompletionTime = physicalTime.add(executionTime);
            Time fireAtTime = executiveDirector.fireAt(container,
                    expectedCompletionTime);

            if (!fireAtTime.equals(expectedCompletionTime)) {
                throw new IllegalActionException(
                        actorToFire,
                        executiveDirector,
                        "Ptides director requires refiring at time "
                                + expectedCompletionTime
                                + ", but the enclosing director replied that it will refire at time "
                                + fireAtTime);
            }

            // If we are preempting a current execution, then
            // update information of the preempted event.
            if (!_currentlyExecutingStack.isEmpty()) {
                // We are preempting a current execution.
                DoubleTimedEvent currentEventList = _currentlyExecutingStack
                        .peek();
                Time elapsedTime = physicalTime
                        .subtract(_physicalTimeExecutionStarted);
                currentEventList.remainingExecutionTime = currentEventList.remainingExecutionTime
                        .subtract(elapsedTime);
                if (currentEventList.remainingExecutionTime.compareTo(_zero) < 0) {
                    // This should not occur.
                    throw new IllegalActionException(
                            this,
                            _getActorFromEventList((List<DEEvent>) currentEventList.contents),
                            "Remaining execution is negative!");
                }
                if (_debugging) {
                    _debug("Preempting actor "
                            + _getActorFromEventList(
                                    (List<DEEvent>) currentEventList.contents)
                                    .getName((NamedObj) container)
                            + " at physical time " + physicalTime
                            + ", which has remaining execution time "
                            + currentEventList.remainingExecutionTime);
                }
            }
            _currentlyExecutingStack.push(new DoubleTimedEvent(
                    timeStampOfEventFromQueue, microstepOfEventFromQueue,
                    eventsToProcess, executionTime));
            _physicalTimeExecutionStarted = physicalTime;

            // Animate if appropriate.
            _setIcon(_getExecutingIcon(actorToFire), false);
            _lastExecutingActor = actorToFire;

            return null;
        }
    }

    /** Return the actor associated with this event. This method should take
     *  all events of the same tag destined for the same actor from the event
     *  queue, and return the actor associated with it.
     *  <p>
     *  In this baseline implementation, super._getNextActorToFire() is called.
     *  @param events list of events that are destined for the
     *  same actor and of the same tag.
     *  @return Actor associated with the event.
     *  @exception IllegalActionException
     */
    protected Actor _getNextActorToFireForTheseEvents(List<DEEvent> events)
            throws IllegalActionException {
        DEEvent eventInList = events.get(0);
        DEEvent eventInQueue = _eventQueue.get();
        if (!(eventInList.hasTheSameTagAs(eventInQueue) && (eventInQueue
                .actor() == eventInList.actor()))) {
            throw new IllegalActionException(
                    "The event passed in is not the top event "
                            + "in the event queue, Probably need to overwrite this method. ");
        }
        return super._getNextActorToFire();
    }

    /** Return the next event we want to process. Notice this event returned must
     *  be safe to process. Any overwriting method must ensure this is true (by
     *  calling some version of _safeToProcess().
     *  <p>
     *  Notice if there are multiple
     *  events in the queue that are safe to process, this function can choose to
     *  return any one of these events, it can also choose to return null depending
     *  on the implementation.
     *  <P>
     *  Also notice this method should _NOT_ take the event from the event queue.
     *  <p>
     *  In this baseline implementation, we only check if
     *  the event at the top of the queue is safe to process. If it is not, then
     *  we return null. Otherwise we return the top event.
     *  @return Next safe event.
     *  @exception IllegalActionException
     */
    protected DEEvent _getNextSafeEvent() throws IllegalActionException {
        DEEvent eventFromQueue = _eventQueue.get();
        if (_safeToProcess(eventFromQueue)) {
            return eventFromQueue;
        } else {
            return null;
        }
    }

    /** Return the model time of the enclosing director, which is our model
     *  of physical time.
     *  @return Physical time.
     */
    protected Time _getPhysicalTime() {
        Actor container = (Actor) getContainer();
        Director director = container.getExecutiveDirector();
        return director.getModelTime();
    }

    /** Returns the realTimeDelay parameter.
     *  @param port The port the realTimeDelay is associated with.
     *  @return realTimeDelay parameter
     *  @exception IllegalActionException
     */
    protected double _getRealTimeDelay(IOPort port)
            throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("realTimeDelay");
        if (parameter != null) {
            return ((DoubleToken) parameter.getToken()).doubleValue();
        } else {
            return 0.0;
        }
    }

    /** Highlight the specified actor with the specified color.
     *  @param actor The actor to highlight.
     *  @param color The color, given as a string description in
     *   the form "{red, green, blue, alpha}", where each of these
     *   is a number between 0.0 and 1.0.
     *  @exception IllegalActionException If the animateExecution
     *   parameter cannot be evaluated.
     */
    protected void _highlightActor(Actor actor, String color)
            throws IllegalActionException {
        if (((BooleanToken) animateExecution.getToken()).booleanValue()) {
            String completeMoML = "<property name=\"_highlightColor\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\""
                    + color + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    (NamedObj) actor, completeMoML);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor) container).requestChange(request);
        }
    }

    /** Return false to get the superclass DE director to behave exactly
     *  as if it is executing at the top level.
     *  @return False.
     */
    protected boolean _isEmbedded() {
        return false;
    }

    /** Return whether we want to preempt the currently executing actor
     *  and instead execute the earliest event on the event queue.
     *  This base class returns false, indicating that the currently
     *  executing actor is never preempted.
     *  @return False.
     * @exception IllegalActionException
     */
    protected boolean _preemptExecutingActor() throws IllegalActionException {
        return false;
    }

    /** If the destination port is the only input port of the actor, or if the port
     *  does not have a minDelay parameter, or if there doesn't exist
     *  a destination port (in case of pure event) then the event is
     *  always safe to process. Otherwise:
     *  If the current physical time has passed the timestamp of the event minus minDelay of
     *  the port, then the event is safe to process. Otherwise the event is not safe to
     *  process, and we calculate the physical time when the event is safe to process and
     *  setup a timed interrupt.
     *
     *  @param event The event checked for safe to process
     *  @return True if the event is safe to process, otherwise return false.
     *  @exception IllegalActionException
     *  @see #_setTimedInterrupt(Time)
     */
    protected boolean _safeToProcess(DEEvent event)
            throws IllegalActionException {
        IOPort port = event.ioPort();
        if (port != null) {
            int channel = ((DETokenEvent) event).channel();
            Parameter parameter = (Parameter) ((NamedObj) port)
                    .getAttribute("minDelay");
            if (parameter != null) {
                DoubleToken token = ((DoubleToken) ((ArrayToken) parameter
                        .getToken()).arrayValue()[channel]);
                Time waitUntilPhysicalTime = event.timeStamp().subtract(
                        token.doubleValue());
                if (_getPhysicalTime().subtract(waitUntilPhysicalTime)
                        .compareTo(_zero) >= 0) {
                    return true;
                } else {
                    _setTimedInterrupt(waitUntilPhysicalTime);
                    return false;
                }
            } else {
                // no minDelay, should only happen if the destination port is the only input port of
                // the destination actor.
                return true;
            }

        } else {
            // event does not have a destination port, must be a pure event.
            // FIXME: in general this is not true.
            return true;
        }
    }

    /** Set the icon for this director if the <i>animateExecution</i>
     *  parameter is set to true.
     *  @param moml A MoML string describing the contents of the icon.
     *  @param clearFirst If true, remove the previous icon before creating a
     *   new one.
     *  @exception IllegalActionException If the <i>animateExecution</i> parameter
     *   cannot be evaluated.
     */
    protected void _setIcon(String moml, boolean clearFirst)
            throws IllegalActionException {
        if (((BooleanToken) animateExecution.getToken()).booleanValue()) {
            String completeMoML = "<property name=\"_icon\" class=\"ptolemy.vergil.icon.EditorIcon\">"
                    + moml + "</property>";
            if (clearFirst && getAttribute("_icon") != null) {
                // If we are running under MoMLSimpleApplication, then the _icon might not
                // be present, so check before trying to delete it.
                completeMoML = "<group><!-- PtidesBasicDirector --><deleteProperty name=\"_icon\"/>"
                        + completeMoML + "</group>";
            }
            MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                    completeMoML);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor) container).requestChange(request);
        }
    }

    /** Call fireAt() of the executive director, which is in charge of bookkeeping the
     *  physical time.
     *  @param wakeUpTime The time to wake up.
     */
    protected void _setTimedInterrupt(Time wakeUpTime) {
        Actor container = (Actor) getContainer();
        Director executiveDirector = ((Actor) container).getExecutiveDirector();
        try {
            executiveDirector.fireAt((Actor) container, wakeUpTime);
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }
    }

    /** This method keeps track of the last event an actor decides to process. This method
     *  is called immediately after _safeToProcess, thus it serves as a check to see if the
     *  processing of this event has violated DE semantics. If it has, then an exception is
     *  thrown, if it has not, then the current tag is saved for checks to see if future
     *  events are processed in timestamp order.
     *  @param event The event that has just been determined to be safe to process.
     *
     *  FIXME: this implementation is actually too conservative, in that it may result in false
     *  negatives. This method assumes each actor should process events in timestamp order,
     *  but in PTIDES we only need each equivalence class to consume events in timetamp
     *  order. Thus this method is correct if the actor's input ports all reside within
     *  the same equivalence class.
     *  @exception IllegalActionException
     */
    protected void _trackLastTagConsumedByActor(DEEvent event)
            throws IllegalActionException {
        NamedObj obj = event.ioPort();
        if (obj == null) {
            obj = (NamedObj) event.actor();
        }

        // FIXME: this is sort of a hack. We have this because the network inteface may receive
        // many events at the same physical time, but then they will decode the incoming token
        // to produce events of (hopefully) different timestamps. Thus here we do not need to
        // check if safe to process was correct if the actor is a NetworkInputDevice.
        if (obj.getContainer() instanceof NetworkInputDevice) {
            return;
        }

        Tag tag = new Tag(event.timeStamp(), event.microstep());
        Tag prevTag = _LastConsumedTag.get(obj);
        if (prevTag != null) {
            if (tag.compareTo(prevTag) <= 0) {
                if (obj instanceof Actor) {
                    throw new IllegalActionException(
                            "Safe to process return the wrong "
                                    + "answer earlier. At Actor: "
                                    + obj.getName()
                                    + ", the tag of the previous processed event is: timestamp = "
                                    + prevTag.timestamp
                                    + ", microstep = "
                                    + prevTag.microstep
                                    + ". The tag of the current event is: timestamp = "
                                    + tag.timestamp + ", microstep = "
                                    + tag.microstep + ". ");
                } else {
                    throw new IllegalActionException(
                            "Safe to process return the wrong "
                                    + "answer earlier. At Actor: "
                                    + ((IOPort) obj).getContainer().getName()
                                    + ", Port "
                                    + obj.getName()
                                    + ", the tag of the previous processed event is: timestamp = "
                                    + prevTag.timestamp
                                    + ", microstep = "
                                    + prevTag.microstep
                                    + ". The tag of the current event is: timestamp = "
                                    + tag.timestamp + ", microstep = "
                                    + tag.microstep + ". ");
                }
            }
        }
        // The check was correct, now we replace the previous tag with the current tag.
        _LastConsumedTag.put(obj, tag);
    }

    /** For all events in the sensorEventQueue, transfer input events that are ready.
     *  For all events that are currently sitting at the input port, if the realTimeDelay
     *  is 0.0, then transfer them into the platform, otherwise move them into the
     *  sensorEventQueue and call fireAt() of the executive director.
     *  In either case, if the input port is a networkPort, we make sure the timestamp of
     *  the data token transmitted is set to the timestamp of the local event associated
     *  with this token.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     */
    protected boolean _transferInputs(IOPort port)
            throws IllegalActionException {

        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }

        boolean result = false;
        Time physicalTime = _getPhysicalTime();
        // First transfer all tokens that are already in the event queue for the sensor.
        // FIXME: notice this is done NOT for the specific port
        // in question. Instead, we do it for ALL events that can be transferred out of
        // this platform.
        // FIXME: there is _NO_ guarantee from the priorityQueue that these events are sent out
        // in the order they arrive at the actuator. We can only be sure that they are sent
        // in the order of the timestamps, but for two events of the same timestamp at an
        // actuator, there's no guarantee on the order of events sent to the outside.
        while (true) {
            if (_realTimeOutputEventQueue.isEmpty()) {
                break;
            }

            RealTimeEvent realTimeEvent = (RealTimeEvent) _realTimeOutputEventQueue
                    .peek();
            int compare = realTimeEvent.deliveryTime.compareTo(physicalTime);

            if (compare > 0) {
                break;
            } else if (compare == 0) {
                // FIXME: Are these needed here?
                Parameter parameter = (Parameter) ((NamedObj) realTimeEvent.port)
                        .getAttribute("realTimeDelay");
                double realTimeDelay = 0.0;
                if (parameter != null) {
                    realTimeDelay = ((DoubleToken) parameter.getToken())
                            .doubleValue();
                } else {
                    // this shouldn't happen.
                    throw new IllegalActionException(
                            "real time delay should not be 0.0");
                }

                Time lastModelTime = _currentTime;
                if (_isNetworkPort(realTimeEvent.port)) {
                    // If the token is transferred from a network port, then there is no need to
                    // set the proper timestamp associated with the token. This is because we rely
                    // on the fact every network input port is directly connected to a networkInputDevice,
                    // which will set the correct timestamp associated with the token.
                    _realTimeOutputEventQueue.poll();
                    realTimeEvent.port.sendInside(realTimeEvent.channel,
                            realTimeEvent.token);
                } else {
                    int lastMicrostep = _microstep;
                    // Since the event comes from a sensor, and we assume no sensor
                    // will produce two events of the same timestamp, the microstep
                    // of the new event is set to 0.
                    setTag(realTimeEvent.deliveryTime.subtract(realTimeDelay),
                            0);
                    _realTimeOutputEventQueue.poll();
                    realTimeEvent.port.sendInside(realTimeEvent.channel,
                            realTimeEvent.token);
                    setTag(lastModelTime, lastMicrostep);
                }
                if (_debugging) {
                    _debug(getName(), "transferring input from "
                            + realTimeEvent.port.getName());
                }
                result = true;

            } else {
                // FIXME: we should probably do something else here.
                throw new IllegalArgumentException(
                        "missed transferring at the sensor. "
                                + "Should transfer input at time = "
                                + realTimeEvent.deliveryTime
                                + ", and current physical time = "
                                + physicalTime);
            }
        }

        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("realTimeDelay");
        // realTimeDelay is default to 0.0;
        double realTimeDelay = 0.0;
        if (parameter != null) {
            realTimeDelay = ((DoubleToken) parameter.getToken()).doubleValue();
        }
        if (realTimeDelay == 0.0) {
            Time lastModelTime = _currentTime;
            if (_isNetworkPort(port)) {
                // If the token is transferred from a network port, then there is no need to
                // set the proper timestamp associated with the token. This is because we rely
                // on the fact every network input port is directly connected to a networkReceiver,
                // which will set the correct timestamp associated with the token.
                super._transferInputs(port);
            } else {
                setTag(physicalTime, 0);
                result = result || super._transferInputs(port);
                setTag(lastModelTime, 0);
            }
        } else {
            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (i < port.getWidthInside()) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);
                            Time waitUntilTime = physicalTime
                                    .add(realTimeDelay);
                            RealTimeEvent realTimeEvent = new RealTimeEvent(
                                    port, i, t, waitUntilTime);
                            _realTimeOutputEventQueue.add(realTimeEvent);
                            result = true;

                            // wait until physical time to transfer the token into the platform
                            Actor container = (Actor) getContainer();
                            container.getExecutiveDirector().fireAt(
                                    (Actor) container, waitUntilTime);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }
        return result;
    }

    /** Overwrite the _transferOutputs() function.
     *  First, for tokens that are stored in the actuator event queue and
     *  send them to the outside of the platform if physical time has arrived.
     *  The second step is to check if this port is a networkedOutput port, if it is, transfer
     *  data tokens immediately to the outside by calling super._transferOutputs(port).
     *  Finally, we check for current model time, if the current model time is equal to the physical
     *  time, we can send the tokens to the outside. Else if current model time has exceeded
     *  the physical time, and we still have tokens to transfer, then we have missed the deadline.
     *  Else if current model time has not arrived at the physical time, then we put the token along
     *  with the port and channel into the actuator event queue, and call fireAt of the executive
     *  director so we could send it at a later physical time.
     */
    protected boolean _transferOutputs(IOPort port)
            throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque input port.");
        }

        // first check for current time, and transfer any tokens that are already ready to output.
        boolean result = false;
        Time physicalTime = _getPhysicalTime();
        int compare = 0;
        // FIXME: notice this is done NOT for the specific port
        // in question. Instead, we do it for ALL events that can be transferred out of
        // this platform.
        // FIXME: there is _NO_ guarantee from the priorityQueue that these events are sent out
        // in the order they arrive at the actuator. We can only be sure that they are sent
        // in the order of the timestamps, but for two events of the same timestamp at an
        // actuator, there's no guarantee on the order of events sent to the outside.
        while (true) {
            if (_realTimeInputEventQueue.isEmpty()) {
                break;
            }
            RealTimeEvent tokenEvent = (RealTimeEvent) _realTimeInputEventQueue
                    .peek();
            compare = tokenEvent.deliveryTime.compareTo(physicalTime);

            if (compare > 0) {
                break;
            } else if (compare == 0) {
                if (_isNetworkPort(tokenEvent.port)) {
                    throw new IllegalActionException(
                            "transferring network event from the"
                                    + "actuator event queue");
                }
                _realTimeInputEventQueue.poll();
                tokenEvent.port.send(tokenEvent.channel, tokenEvent.token);
                if (_debugging) {
                    _debug(getName(), "transferring output " + tokenEvent.token
                            + " from " + tokenEvent.port.getName());
                }
                result = true;
            } else if (compare < 0) {
                // FIXME: we should probably do something else here.
                throw new IllegalArgumentException(
                        "missed deadline at the actuator. Deadline = "
                                + tokenEvent.deliveryTime
                                + ", and current physical time = "
                                + physicalTime);
            }
        }

        if (_isNetworkPort(port)) {
            // if we transferred once to the network output, then return true,
            // and go through this once again.
            while (true) {
                if (!super._transferOutputs(port)) {
                    break;
                }
            }
            // do not need to update the result, because this loop ensures
            // we have transmitted all network output events, so no need
            // to enter here again.
        }

        compare = _currentTime.compareTo(physicalTime);
        // if physical time has reached the timestamp of the last event, transmit data to the output
        // now. Notice this does not guarantee tokens are transmitted, simply because there might
        // not be any tokens to transmit.
        if (compare == 0) {
            result = result || super._transferOutputs(port);
        } else if (compare < 0) {
            for (int i = 0; i < port.getWidthInside(); i++) {
                if (port.hasTokenInside(i)) {
                    // FIXME: we should probably do something else here.
                    throw new IllegalArgumentException(
                            "missed deadline at the actuator at port: "
                                    + port.getName() + ". Deadline = "
                                    + _currentTime
                                    + ", and current physical time = "
                                    + physicalTime);
                }
            }
        } else {
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        RealTimeEvent tokenEvent = new RealTimeEvent(port, i,
                                t, _currentTime);
                        _realTimeInputEventQueue.add(tokenEvent);
                        // wait until physical time to transfer the output to the actuator
                        Actor container = (Actor) getContainer();
                        container.getExecutiveDirector().fireAt(
                                (Actor) container, _currentTime);
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    /** Return the actor associated with the events in the list. All events
     *  within the list should be destined for the same actor.
     *  @param currentEventList A list of events.
     *  @return Actor associated with events in the list.
     */
    private Actor _getActorFromEventList(List<DEEvent> currentEventList) {
        return currentEventList.get(0).actor();
    }

    /** For a particular input port channel pair, find the min delay.
     *  @param inputPort The input port to find min delay for.
     *  @param channel The channel at this input port.
     *  @param inputModelTimeDelays Port channel pairs with associated dependency.
     *  @return The min delay associated with this port channel pair.
     *  @exception IllegalActionException
     */
    private static double _getMinDelayForPortChannel(IOPort inputPort,
            Integer channel,
            Map<IOPort, Map<Integer, SuperdenseDependency>> inputModelTimeDelays)
            throws IllegalActionException {
        SuperdenseDependency smallestDependency = SuperdenseDependency.OPLUS_IDENTITY;
        // for each port that's in the same equivalence class as the input port,
        for (IOPort port : (Collection<IOPort>) _finiteEquivalentPorts(inputPort)) {
            Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) inputModelTimeDelays
                    .get(port);
            if (channelDependency != null) {
                for (Integer integer : channelDependency.keySet()) {
                    if (!(port == inputPort && integer == channel)) {
                        SuperdenseDependency candidate = channelDependency
                                .get(integer);
                        if (smallestDependency.compareTo(candidate) > 0) {
                            smallestDependency = candidate;
                        }
                    }
                }
            }
        }
        return smallestDependency.timeValue();
    }

    /** Returns the relativeDeadline parameter.
     *  @param port The port the relativeDeadline is associated with.
     *  @return relativeDeadline parameter
     *  @exception IllegalActionException
     */
    private double _getRelativeDeadline(IOPort port)
            throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("relativeDeadline");
        if (parameter != null) {
            return ((DoubleToken) parameter.getToken()).doubleValue();
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    /** check if the port is a networkPort
     *  this method is default to return false, i.e., an output port to the outside of the
     *  platform is by default an actuator port.
     * @exception IllegalActionException
     */
    private static boolean _isNetworkPort(IOPort port)
            throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("networkPort");
        if (parameter != null) {
            return ((BooleanToken) parameter.getToken()).booleanValue();
        }
        return false;
    }

    private static void _setMinDelay(IOPort inputPort, double[] minDelays)
            throws IllegalActionException {
        Parameter parameter = (Parameter) (inputPort).getAttribute("minDelay");
        if (parameter == null) {
            try {
                parameter = new Parameter(inputPort, "minDelay");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(
                        "A minDelay parameter already exists");
            }
        }
        DoubleToken[] tokens = new DoubleToken[minDelays.length];
        for (int i = 0; i < minDelays.length; i++) {
            tokens[i] = new DoubleToken(minDelays[i]);
        }
        ArrayToken arrayToken = new ArrayToken(tokens);
        parameter.setToken(arrayToken);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /** Each actor keeps track of the tag of the last event that was consumed when this
     *  actor fired. This helps to identify cases where safe-to-process analysis failed
     *  unexpectedly.
     */
    private HashMap<NamedObj, Tag> _LastConsumedTag;

    /** Last executing actor
     *  Keeps track of the last actor with non-zero executing time that was executing
     *  This helps to clear the highlighting of that actor when executing stops.
     */
    private Actor _lastExecutingActor;

    /** The physical time at which the currently executing actor, if any,
     *  last resumed execution.
     */
    private Time _physicalTimeExecutionStarted;

    /** a sorted queue of RealTimeEvents that buffer events before they are sent to the output.
     */
    private PriorityQueue _realTimeInputEventQueue;

    /** a sorted queue of RealTimeEvents that stores events when they arrive at the input of
     *  the platform, but are not yet visible to the platform (because of real time delay d_o)
     */
    private PriorityQueue _realTimeOutputEventQueue;

    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    /** A TimedEvent extended with an additional field to represent
     *  the remaining execution time (in physical time) for processing
     *  the event.
     */
    public class DoubleTimedEvent extends TimedEvent {

        /** Construct a new event with the specified time stamp,
         *  destination actor, and execution time.
         *  @param timeStamp The time stamp.
         *  @param microstep The microstep.
         *  @param executingEvents The events to execute.
         *  @param executionTime The execution time of the actor.
         */
        public DoubleTimedEvent(Time timeStamp, int microstep,
                Object executingEvents, Time executionTime) {
            super(timeStamp, executingEvents);
            this.microstep = microstep;
            remainingExecutionTime = executionTime;
        }

        /** Remaining execution time of the currently executing event. */
        public Time remainingExecutionTime;

        /** Microstep of the executing event. */
        public int microstep;

        /** Converts the executing event to a string. */
        public String toString() {
            return super.toString() + ", microstep = " + microstep
                    + ", remainingExecutionTime = " + remainingExecutionTime;
        }
    }

    /** A structure that stores a PortChannel and a dependency associated with
     *  that port. This structure is comparable, and it compares using
     *  the dependency information.
     */
    public class PortDependency implements Comparable {

        /** Construct a structure that holds a port and the associated dependency.
         *  @param port The port.
         *  @param dependency The Dependency.
         */
        public PortDependency(IOPort port, Dependency dependency) {
            this.port = port;
            this.dependency = dependency;
        }

        /** The port. */
        public IOPort port;

        /** The dependency. */
        public Dependency dependency;

        /** Compares this PortDependency with another. Compares the dependencies
         *  of these two objects.
         *  @param arg0 The object comparing to.
         */
        public int compareTo(Object arg0) {
            PortDependency portDependency = (PortDependency) arg0;
            if (this.dependency.compareTo(portDependency.dependency) > 0) {
                return 1;
            } else if (this.dependency.compareTo(portDependency.dependency) < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        /** Checks if this PortDependency is the same as another.
         *  @param arg0 The object checking against.
         */
        public boolean equals(Object arg0) {
            if (compareTo(arg0) == 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /** A structure that holds a token with the port and channel it's connected to,
     *  as well as the timestamp associated with this token.
     *  This object is used to hold sensor and actuation events.
     */
    public class RealTimeEvent implements Comparable {

        /** Construct a structure that holds a real-time event. This event saves
         *  the token to be transmitted, the port and channel this token should be deliverd
         *  to, and the time this token should be delivered at.
         *  @param port The destination port.
         *  @param channel The destination channel.
         *  @param token The token to be delivered.
         *  @param timestamp The time of delivery of this token.
         */
        public RealTimeEvent(IOPort port, int channel, Token token,
                Time timestamp) {
            this.port = port;
            this.channel = channel;
            this.token = token;
            this.deliveryTime = timestamp;
        }

        /** The port. */
        public IOPort port;

        /** The channel. */
        public int channel;

        /** The token. */
        public Token token;

        /** The time of delivery. */
        public Time deliveryTime;

        /** Compares this RealTimeEvent with another. Compares the delivery
         *  times of these two events.
         *  @param other The object comparing to.
         */
        public int compareTo(Object other) {
            return deliveryTime.compareTo(((RealTimeEvent) other).deliveryTime);
        }
    }
}
