/* Preemptive EDF Ptides director that allows preemption, and uses EDF to determine whether preemption should occur.

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ptides.lib.NetworkReceiver;
import ptolemy.domains.ptides.lib.SensorHandler;
import ptolemy.domains.ptides.lib.io.NetworkTransmitterPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 *  This director implements preemptive PTIDES scheduling algorithm, and uses
 *  EDF as the foundation to determine whether we should preempt executing events.
 *  This director is different from PtidesBasicPreemptiveDDFDirector because that director
 *  only takes the first event and analyze it for safe to process, however this director
 *  looks at all events in the event queue, takes the one that is safe to process and has
 *  the smallest deadline. Here, deadline is calculated by summing timestamp with the
 *  relativeDeadline parameter as annotated by the user.
 *  <p>
 *  Notice this director has to use RealDependency, though all PTIDES directors should be
 *  using RealDependency. The reason it is used here is because safe to process relies on
 *  the correct ordering of depth in order to provide the correct answer, and
 *  BooleanDependency does not return the correct value for depth.
 *
 *  @author Slobodan Matic, Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */
@Deprecated
public class PtidesPreemptiveEDFDirector extends PtidesBasicDirector {
    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesPreemptiveEDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call super's initialize method, and then
     *  initialize saved deadline variable.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

    }

    /** Clear what's stored in _eventToProcess, and call the super method
     *  of preinitialize.
     *  @exception IllegalActionException If the superclass throws
     *   it or if there is no executive director.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _pureEventDeadlines = new HashMap<Actor, Time>();
        _eventToProcess = null;
        _calculateDeadline();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Calculate the absolute deadline for the pure event, which is simply the
     *  deadline of the last trigger event.
     *  @param actor The destination actor for the pure event.
     *  @see #_saveEventInformation(List)
     *  @return The absolute deadline for this event.
     */
    protected Time _absoluteDeadlineForPureEvent(Actor actor) {
        Time lastAbsoluteDeadline = _pureEventDeadlines.get(actor);
        return lastAbsoluteDeadline;
    }

    /** Perform bookkeeping after actor firing. This procedure consist of
     *  two actions:
     *  <ol>
     *  <li>An actor has just been fired. A token with destination to the
     *  outside of the Ptides platform could have been produced. If so,
     *  the corresponding event is taken out of event queue, and the token
     *  is placed at the actuator/network port, ready to be transferred
     *  to the outside.</li>
     *  <li> Bookkeeping structures that keep track of which actor
     *  has just fired are cleared.</li>
     *  </ol>
     *  @exception IllegalActionException If unable to get the next actuation event.
     */
    protected void _actorFired() throws IllegalActionException {
        super._actorFired();
        if (_lastActorFired != null) {
            _pureEventDeadlines.remove(_lastActorFired);
        }
    }

    /** Calculate the deadline for each channel in each input port within the
     *  composite actor governed by this Ptides director. Deadlines are calculated
     *  with only model time delays, not worst-case-execution-times (WCET).
     *  @exception IllegalActionException when ports that are both input/output ports exist.
     */
    protected void _calculateDeadline() throws IllegalActionException {
        // The algorithm used is exactly the same as the one in _calculateMinDelay. The only
        // difference is that we are starting from the input ports of actuators and traversing
        // backwards.

        // FIXME: If there are composite actors within the top level composite actor,
        // does this algorithm work?
        // initialize all port model delays to infinity.
        HashMap portDeadlines = new HashMap<IOPort, SuperdenseDependency>();
        for (Actor actor : (List<Actor>) (((TypedCompositeActor) getContainer())
                .deepEntityList())) {
            for (TypedIOPort inputPort : (List<TypedIOPort>) (actor
                    .inputPortList())) {
                portDeadlines.put(inputPort,
                        SuperdenseDependency.OPLUS_IDENTITY);
            }
            for (TypedIOPort outputPort : (List<TypedIOPort>) (actor
                    .outputPortList())) {
                portDeadlines.put(outputPort,
                        SuperdenseDependency.OPLUS_IDENTITY);
            }
        }

        for (TypedIOPort outputPort : (List<TypedIOPort>) (((Actor) getContainer())
                .outputPortList())) {
            SuperdenseDependency startDelay = SuperdenseDependency.OTIMES_IDENTITY;
            // If platformDelayBound is specified use that to update deadline.
            if (outputPort instanceof NetworkTransmitterPort) {
                startDelay = SuperdenseDependency
                        .valueOf(
                                ((DoubleToken) ((NetworkTransmitterPort) outputPort).platformDelayBound
                                        .getToken()).doubleValue(), 0);
            }
            portDeadlines.put(outputPort, startDelay);
        }
        // Now start from each sensor (input port at the top level), traverse through all
        // ports.
        for (TypedIOPort startPort : (List<TypedIOPort>) (((TypedCompositeActor) getContainer())
                .outputPortList())) {
            // Setup a local priority queue to store all reached ports
            HashMap localPortDeadlines = new HashMap<IOPort, SuperdenseDependency>(
                    portDeadlines);

            PriorityQueue distQueue = new PriorityQueue<PortDependency>();
            distQueue.add(new PortDependency(startPort,
                    (SuperdenseDependency) localPortDeadlines.get(startPort)));

            // Dijkstra's algorithm to find all shortest time delays.
            while (!distQueue.isEmpty()) {
                PortDependency portDependency = (PortDependency) distQueue
                        .remove();
                IOPort port = portDependency.port;
                SuperdenseDependency prevDependency = (SuperdenseDependency) portDependency.dependency;
                Actor actor = (Actor) port.getContainer();
                if (port.isInput() && port.isOutput()) {
                    throw new IllegalActionException(
                            "the causality analysis cannot deal with"
                                    + "port that are both input and output");
                }
                // we do not want to traverse to the outside of the platform.
                if (actor != getContainer()) {
                    if (port.isOutput()) {
                        Collection<IOPort> inputs = _finiteDependentPorts(port);
                        for (IOPort inputPort : inputs) {
                            SuperdenseDependency minimumDelay = (SuperdenseDependency) _getDependency(
                                    inputPort, port);
                            // FIXME: what do we do with the microstep portion of the dependency?
                            // need to make sure we did not visit this port before.
                            SuperdenseDependency modelTime = (SuperdenseDependency) prevDependency
                                    .oTimes(minimumDelay);
                            if (((SuperdenseDependency) localPortDeadlines
                                    .get(inputPort)).compareTo(modelTime) > 0) {
                                localPortDeadlines.put(inputPort, modelTime);
                                distQueue.add(new PortDependency(inputPort,
                                        modelTime));
                            }
                        }
                    } else { // port is an input port
                        // For each receiving port channel pair, add the dependency in inputModelTimeDelays.
                        // We do not need to check whether there already exists a dependency because if
                        // a dependency already exists for that pair, that dependency must have a greater
                        // value, meaning it should be replaced. This is because the output port that
                        // led to that pair would not be in distQueue if it the dependency was smaller.

                        for (IOPort sourcePort : port.sourcePortList()) {
                            // Assume output ports only have width of 1.
                            // we do not want to traverse to the outside of the platform.
                            if (sourcePort.getContainer() != getContainer()) {
                                // for this port channel pair, add the dependency.
                                // After updating dependencies, we need to decide whether we should keep traversing
                                // the graph.
                                if (((SuperdenseDependency) localPortDeadlines
                                        .get(sourcePort))
                                        .compareTo(prevDependency) > 0) {
                                    localPortDeadlines.put(sourcePort,
                                            prevDependency);
                                    distQueue.add(new PortDependency(
                                            sourcePort, prevDependency));
                                }
                            }
                        }
                    }
                } else if (port == startPort) {
                    // The (almost) same code (except for getting receivers) is used if the
                    // port is a startPort or an input port.
                    // This does not support input/output port, should it?
                    for (IOPort sourcePort : (List<IOPort>) port
                            .deepInsidePortList()) {
                        // Assume output ports only have width of 1.
                        // we do not want to traverse to the outside of the platform.
                        if (sourcePort.getContainer() != getContainer()) {
                            // for this port channel pair, add the dependency.
                            // After updating dependencies, we need to decide whether we should keep traversing
                            // the graph.
                            if (((SuperdenseDependency) localPortDeadlines
                                    .get(sourcePort)).compareTo(prevDependency) > 0) {
                                localPortDeadlines.put(sourcePort,
                                        prevDependency);
                                distQueue.add(new PortDependency(sourcePort,
                                        prevDependency));
                            }
                        }
                    }
                }
            }
            portDeadlines = localPortDeadlines;
        }

        // inputModelTimeDelays is the delays as calculated through shortest path algorithm. Now we
        // need to use these delays to calculate the minDelay, which is calculated as follows:
        // For each port, get all finite equivalent ports except itself. Now for a particular port
        // channel pair, Find the smallest model time delay among all of the channels on all these
        // ports, if they exist. that smallest delay is  the minDelay for that port channel pair.
        // If this smallest value does not exist, then the event arriving at this port channel pair
        // is always safe to process, thus minDelay does not change (it was by default set to
        // double.POSITIVE_INFINITY.
        for (IOPort port : (Set<IOPort>) (portDeadlines.keySet())) {
            if (port.isInput()) {
                SuperdenseDependency dependency = (SuperdenseDependency) (portDeadlines
                        .get(port));
                // If this port is an input port of SensorHandler or
                // NetworkReceiver, give this port the highest priority so it
                // execute first. We do this because in PtidyOS, interrupts
                // are always executed (to insert new events into the event
                // queue). Since the SensorHandler and NetworkReceiver simulates
                // interrupt handling, these must execute with the highest
                // priority in order to correctly simulate execute time.
                if (port.getContainer() instanceof SensorHandler
                        || port.getContainer() instanceof NetworkReceiver) {
                    _setDeadline(port, SuperdenseDependency.valueOf(
                            Double.NEGATIVE_INFINITY, 0));
                } else {
                    _setDeadline(port, dependency);
                }
            }
        }
    }

    /** Check to see if the current actor is already firing, in which case we should not preempt.
     *  @param actor The currently firing actor.
     *  @return whether the current actor is firing or no.
     */
    protected boolean _currentlyFiring(Actor actor) {
        for (int index = 0; index < _currentlyExecutingStack.size(); index++) {
            ExecutionTimedEvent doubleTimedEvent = _currentlyExecutingStack
                    .get(0);
            List<PtidesEvent> executingEvents = (List<PtidesEvent>) doubleTimedEvent.contents;
            if (actor == executingEvents.get(0).actor()) {
                return true;
            }
        }
        return false;
    }

    /** Return the absolute deadline of this event. If this event is a pure event,
     *  then the relative deadline should be stored in the event itself. Otherwise
     *  the trigger event's relative deadline is the relativeDeadline
     *  parameter of this event's destination port. The absolute deadline of this
     *  event is the timestamp of the event plus the relative deadline.
     *  @param event Reference event whose deadline we are interested in.
     *  @return deadline The deadline of this event.
     *  @exception IllegalActionException If the relative deadline of the event
     *  cannot be obtained.
     */
    protected Time _getAbsoluteDeadline(PtidesEvent event)
            throws IllegalActionException {
        if (event.isPureEvent()) {
            return event.absoluteDeadline();
        }
        return event.timeStamp().add(_getRelativeDeadline(event.ioPort()));
    }

    /** Return the event that was selected to preempt in _preemptExecutingActor.
     *  If no event was selected, return the event of smallest deadline that is
     *  safe to process.
     *  This is when _eventToProcess is last used in this iteration, so it should be
     *  cleared to null, so that later iterations will not see the same events stored
     *  in _eventToProcess.
     */
    protected PtidesEvent _getNextSafeEvent() throws IllegalActionException {
        PtidesEvent tempEvent;
        if (_eventToProcess == null) {
            // _eventToProcess is set in the following method.
            // _peekingIndex is also updated.
            _getSmallestDeadlineSafeEventFromQueue();
        }
        // if _preemptExecutingActor already decided on an event to process,
        // that event is stored in _eventToProcess, and _peekingIndex is set
        // to point to that event already.
        tempEvent = _eventToProcess;
        // _eventToProcess is used to keep track of the preempting event.
        // Now that we know what that event should be, _eventToProcess
        // is cleared so that it could be used properly in the next iteration.
        _eventToProcess = null;
        return tempEvent;
    }

    /** Find the event in the queue that is of the smallest deadline
     *  The event found is stored in _eventToProcess. It then stores the
     *  index of the event in _peekingIndex.
     *  @return false if no event is found. returns false, otherwise returns true.
     *  @exception IllegalActionException
     */
    protected boolean _getSmallestDeadlineSafeEventFromQueue()
            throws IllegalActionException {

        // clear _eventToProcess.
        _eventToProcess = null;
        Time smallestDeadline = new Time(this, Double.POSITIVE_INFINITY);
        int result = 0;

        for (int eventIndex = 0; eventIndex < _eventQueue.size(); eventIndex++) {
            PtidesEvent event = ((PtidesListEventQueue) _eventQueue)
                    .get(eventIndex);
            if (event.isPureEvent()) {
                if (event.actor().inputPortList().size() == 0) {
                    throw new IllegalActionException(
                            "When getting the deadline for "
                                    + "a pure event at " + event.actor()
                                    + ", this actor"
                                    + "does not have an input port, thus"
                                    + "unable to get relative deadline");
                }
            }

            // The event from queue needs to be safe to process AND has smaller deadline.
            if (_safeToProcess(event) && !_currentlyFiring(event.actor())) {
                Time absNextDeadline = _getAbsoluteDeadline(event);
                if (absNextDeadline.compareTo(smallestDeadline) < 0) {
                    smallestDeadline = absNextDeadline;
                    _eventToProcess = event;
                    result = eventIndex;
                } else if (absNextDeadline.compareTo(smallestDeadline) == 0) {
                    // if we haven't found an event, then this event is it.
                    if (_eventToProcess == null) {
                        // don't need to replace deadline, because they are equal.
                        _eventToProcess = event;
                        result = eventIndex;
                    } // else if they are equal, take the previous event.
                }
            } // if the actor is currently firing, or if it's not safe to process, we
              // do not considering firing it.
        }

        if (_eventToProcess == null) {
            return false;
        } else {
            _peekingIndex = result;
            return true;
        }
    }

    /** Return whether we want to preempt the currently executing actor
     *  and instead execute another event from the event queue.
     *  This method iterate through all events in the event queue, and finds
     *  the event in the queue that is both safe, and also has the smallest
     *  deadline. This event is then stored in _eventToProcess, and returned
     *  in _getNextSafeEvent().
     *  If there are several safe events with the smallest deadline, then the
     *  event of smallest tag + depth is stored in _eventToProcess.
     *  @return whether we want to preempt the executing event.
     *  @exception IllegalActionException
     *  @see #_getNextSafeEvent()
     */
    protected boolean _preemptExecutingActor() throws IllegalActionException {

        // First, _eventToProcess is set to the event of smallest deadline in the queue.
        // If no event is found, then there's no preemption
        if (!_getSmallestDeadlineSafeEventFromQueue()) {
            return false;
        }

        // check if we want to preempt whatever that's executing with _eventToProcess.
        // First make smallestDeadline the smallest deadline among all events
        // at the top of the stack.
        Time smallestStackDeadline = new Time(this, Double.POSITIVE_INFINITY);
        ExecutionTimedEvent doubleTimedEvent = _currentlyExecutingStack.peek();
        List eventList = (List<PtidesEvent>) (doubleTimedEvent.contents);
        PtidesEvent executingEvent = (PtidesEvent) eventList.get(0);
        for (int i = 0; i < eventList.size(); i++) {
            Time absExecutingDeadline = _getAbsoluteDeadline(((PtidesEvent) eventList
                    .get(i)));
            if (absExecutingDeadline.compareTo(smallestStackDeadline) <= 0) {
                smallestStackDeadline = absExecutingDeadline;
            }
        }

        Time smallestQueueDeadline = _getAbsoluteDeadline(_eventToProcess);

        // if we decide not to preempt because the one on stack has smaller deadline,
        // then we set _eventToProcess back to null;
        if (smallestQueueDeadline.compareTo(smallestStackDeadline) > 0) {
            _eventToProcess = null;
        } else if (smallestQueueDeadline.compareTo(smallestStackDeadline) == 0) {
            if (_eventToProcess.compareTo(executingEvent) >= 0) {
                _eventToProcess = null;
            } // if the deadline and tag and depth are all equal, don't preempt.
        }

        if (_eventToProcess == null) {
            if (_debugging) {
                _debug("We decided not to do preemption in this round, "
                        + "but to keep executing " + executingEvent.actor()
                        + " at physical time "
                        + getPlatformPhysicalTag(executionTimeClock).timestamp
                        + "."
                        + getPlatformPhysicalTag(executionTimeClock).microstep);
            }
            return false;
        } else {

            if (_debugging) {
                _debug("We decided to preempt the current "
                        + "executing event at actor: " + executingEvent.actor()
                        + " with another event at actor: "
                        + _eventToProcess.actor()
                        + ". This preemption happened at physical time "
                        + getPlatformPhysicalTag(executionTimeClock).timestamp
                        + "."
                        + getPlatformPhysicalTag(executionTimeClock).microstep);
            }

            return true;
        }
    }

    /** Call this method from the super class, then save the absolute
     *  deadline for the last executing event.
     *  @param eventsToProcess The set of events to be processed.
     *  @exception IllegalActionException If the super class throws it or if failed
     *  to get the absolute deadline for input events.
     */
    protected void _saveEventInformation(List<PtidesEvent> eventsToProcess)
            throws IllegalActionException {
        super._saveEventInformation(eventsToProcess);
        Actor actorToFire = eventsToProcess.get(0).actor();
        Time lastAbsoluteDeadline = Time.POSITIVE_INFINITY;
        List<IOPort> inputPorts = new ArrayList<IOPort>();

        for (PtidesEvent event : eventsToProcess) {
            Time absoluateDeadline = _getAbsoluteDeadline(event);
            if (absoluateDeadline.compareTo(lastAbsoluteDeadline) < 0) {
                lastAbsoluteDeadline = absoluateDeadline;
            }
            IOPort port = event.ioPort();
            if (port != null) {
                inputPorts.add(port);
            }
        }
        _pureEventDeadlines.put(actorToFire, lastAbsoluteDeadline);
    }

    /** Set the relativeDeadline parameter for an input port.
     *  @param inputPort The port to set the parameter.
     *  @param dependency The value of the relativeDeadline to be set.
     *  @exception IllegalActionException If unsuccessful in getting the attribute.
     */
    protected void _setDeadline(IOPort inputPort,
            SuperdenseDependency dependency) throws IllegalActionException {
        Parameter parameter = (Parameter) (inputPort)
                .getAttribute("relativeDeadline");
        if (parameter == null) {
            try {
                parameter = new Parameter(inputPort, "relativeDeadline");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(
                        "A relativeDeadline parameter already exists");
            }
        }
        parameter.setToken(new DoubleToken(dependency.timeValue()));
    }

    /** Return all the events in the event queue that are of the same tag as the event
     *  passed in, AND are destined to the same finite equivalence class. These events
     *  should be removed from the event queue in the process.
     *  @param event The reference event.
     *  @return List of events of the same tag.
     *  @exception IllegalActionException
     */
    protected List<PtidesEvent> _takeAllSameTagEventsFromQueue(PtidesEvent event)
            throws IllegalActionException {
        if (event != ((PtidesListEventQueue) _eventQueue).get(_peekingIndex)) {
            throw new IllegalActionException(
                    "The event to get is not the event pointed "
                            + "to by peeking index.");
        }
        List<PtidesEvent> eventList = new ArrayList<PtidesEvent>();
        eventList.add(((PtidesListEventQueue) _eventQueue).take(_peekingIndex));
        // The original event at _peekingIndex has been taken, so the eventIndex points to
        // the next event.
        int eventIndex = _peekingIndex;
        while (eventIndex < _eventQueue.size()) {
            PtidesEvent nextEvent = ((PtidesListEventQueue) _eventQueue)
                    .get(eventIndex);
            if (nextEvent.hasTheSameTagAs(event)) {
                if (_sameInputPortGroup(event, nextEvent)) {
                    eventList.add(((PtidesListEventQueue) _eventQueue)
                            .take(eventIndex));
                } else {
                    eventIndex++;
                }
            } else {
                break;
            }
        }
        while (_peekingIndex > 0) {
            PtidesEvent nextEvent = ((PtidesListEventQueue) _eventQueue)
                    .get(_peekingIndex - 1);
            if (nextEvent.hasTheSameTagAs(event)) {
                if (_sameInputPortGroup(event, nextEvent)) {
                    eventList.add(((PtidesListEventQueue) _eventQueue)
                            .take(_peekingIndex - 1));
                }
            } else {
                break;
            }
            _peekingIndex--;
        }
        return eventList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The event to be processed next. */
    protected PtidesEvent _eventToProcess;

    /** The index of the event we are peeking in the event queue. */
    protected int _peekingIndex;

    /** Store absolute deadline information for pure events that will be produced
     *  in the future.
     */
    private Map<Actor, Time> _pureEventDeadlines;

    ///////////////////////////////////////////////////////////////////
    ////                         private method                 ////

    /** Returns the relativeDeadline parameter.
     *  @param port The port the relativeDeadline is associated with.
     *  @return relativeDeadline parameter
     *  @exception IllegalActionException If token of relativeDeadline
     *  parameter cannot be evaluated.
     */
    private static double _getRelativeDeadline(IOPort port)
            throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("relativeDeadline");
        if (parameter != null) {
            return ((DoubleToken) parameter.getToken()).doubleValue();
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
