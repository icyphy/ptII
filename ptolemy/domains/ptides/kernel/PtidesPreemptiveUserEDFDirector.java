/* Preemptive EDF Ptides director that allows users to define deadlines to govern preemptive behavior.

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This director allows users to define deadlines themselves. Unlike
 *  PtidesPreemptiveEDFDirector, which automatically calculates the deadline
 *  using model time delays, this director can work with arbitrary deadlines.
 *  The safe to process analysis in this director guarantees the correct
 *  DE semantics disregarding what deadlines are used.
 *  @author Jia Zou, Slobodan Matic
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */

public class PtidesPreemptiveUserEDFDirector extends
        PtidesPreemptiveEDFDirector {

    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesPreemptiveUserEDFDirector(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        calculateDeadlineFromModelDelay = new Parameter(this,
                "calculateDeadlineFromModelDelay");
        calculateDeadlineFromModelDelay.setExpression("false");
        calculateDeadlineFromModelDelay.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then user the super's method to calculate the deadline
     *  using model time delays.
     */
    public Parameter calculateDeadlineFromModelDelay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Calculates dependencies between each pair of ports in the composite
     *  actor governed by this director. These values are cached and later
     *  used in safe to process analysis.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _inputPairDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
        _calculatePortDependencies();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Only calculate the deadline using model time delay if asked to.
     *  Calculates the deadline for each channel in each input port within the
     *  composite actor governed by this Ptides director. Deadlines are calculated
     *  with only model time delays, not worst-case-execution-times (WCET).
     */
    protected void _calculateDeadline() throws IllegalActionException {
        BooleanToken token = (BooleanToken) calculateDeadlineFromModelDelay
                .getToken();
        if (token != null && token.booleanValue()) {
            super._calculateDeadline();
        }
    }

    /** Calculates the dependencies between each pair of input ports within the composite
     *  actor that is governed by this director.
     *  @exception IllegalActionException
     */
    protected void _calculatePortDependencies() throws IllegalActionException {
        // Initializes the input pairs. By default, each input port pair has oPlusIdentity
        // dependency. However, if two input ports reside in the same finite equivalence
        // class, they have oTimesIdentity dependency. Also, for each input port i, we need
        // to find input ports (j_1, j_2, ...) where each j_n needs to satisfy the following
        // relationship: there exists an output port o where i, o are finitely dependent,
        // AND, there exists another port j', where j' is in o's sinkPortList(), and either
        // j' == j_n, or j' resides in the same finite equivalence class as j_n.
        List<IOPort> startInputs = new ArrayList<IOPort>();
        for (Actor actor : ((List<Actor>) ((TypedCompositeActor) getContainer())
                .deepEntityList())) {
            if (actor == getContainer()) {
                break;
            }
            for (IOPort firstInput : (List<IOPort>) actor.inputPortList()) {
                startInputs.add(firstInput);
                // FIXME: we assume events at the same finite equivalent classes should be processed
                // in timestamp order regardless of the timestamps of events at the outputs. Thus
                // we do not need to know the dependency between the equivalent input ports.
                for (IOPort secondInput : (PtidesBasicDirector
                        ._portsBelongToTheSameInputGroup(firstInput))) {
                    Map<IOPort, Dependency> portDependency = _inputPairDependencies
                            .get(firstInput);
                    if (portDependency == null) {
                        portDependency = new HashMap<IOPort, Dependency>();
                    }
                    portDependency.put(secondInput,
                            SuperdenseDependency.OTIMES_IDENTITY);
                    _inputPairDependencies.put(firstInput, portDependency);
                }
                for (IOPort output : PtidesBasicDirector
                        ._finiteDependentPorts(firstInput)) {
                    Dependency dependency = actor.getCausalityInterface()
                            .getDependency(firstInput, output);
                    for (IOPort secondInput : output.sinkPortList()) {
                        if (secondInput.getContainer() != getContainer()) {
                            Map<IOPort, Dependency> portDependency = _inputPairDependencies
                                    .get(firstInput);
                            if (portDependency == null) {
                                portDependency = new HashMap<IOPort, Dependency>();
                            }
                            SuperdenseDependency prevDependency = (SuperdenseDependency) portDependency
                                    .get(secondInput);
                            if (prevDependency != null
                                    && prevDependency.compareTo(dependency) < 0) {
                                dependency = prevDependency;
                            }
                            portDependency.put(secondInput, dependency);
                            for (IOPort equivSecondInput : (PtidesBasicDirector
                                    ._portsBelongToTheSameInputGroup(secondInput))) {
                                portDependency
                                        .put(equivSecondInput, dependency);
                            }
                            _inputPairDependencies.put(firstInput,
                                    portDependency);
                        }
                    }
                }
            }
        }

        // Given the initialized input pair dependencies, use Floyd-Warshall algorithm to calculate
        // the dependency between all pairs of input ports in the composite actor governed by this
        // director.
        IOPort[] allInputs = (startInputs
                .toArray(new IOPort[startInputs.size()]));
        int length = allInputs.length;
        for (int i = 0; i < length; i++) {
            IOPort middleInput = allInputs[i];
            for (int j = 0; j < length; j++) {
                IOPort startInput = allInputs[j];
                for (int k = 0; k < length; k++) {
                    IOPort endInput = allInputs[k];
                    Dependency middleEndDependency = null;
                    Dependency startMiddleDependency = null;
                    Dependency prevDependency = null;
                    Map<IOPort, Dependency> middlePortDependency = _inputPairDependencies
                            .get(middleInput);
                    if (middlePortDependency != null) {
                        middleEndDependency = middlePortDependency
                                .get(endInput);
                        if (middleEndDependency != null) {
                            Map<IOPort, Dependency> startPortDependency = _inputPairDependencies
                                    .get(startInput);
                            if (startPortDependency != null) {
                                startMiddleDependency = startPortDependency
                                        .get(middleInput);
                                if (startMiddleDependency != null) {
                                    prevDependency = startPortDependency
                                            .get(endInput);
                                    Dependency newDependency = startMiddleDependency
                                            .oTimes(middleEndDependency);
                                    if (prevDependency != null) {
                                        if (newDependency
                                                .compareTo(prevDependency) < 0) {
                                            startPortDependency.put(endInput,
                                                    newDependency);
                                        }
                                    } else {
                                        startPortDependency.put(endInput,
                                                newDependency);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** This method first uses the super of _safeToProcess(). If it's true, that means
     *  there are no events from the outside of the platform that may result in this
     *  event unsafe. If false, then this method should return false.
     *  Then we use _inputPairDependencies as well as the event queue
     *  to check if there are any events in the same platform that may result in
     *  this event unsafe to process. If so, return false, else return true.
     *  @param event The event of interest.
     *  @return true if there are no events from the outside of the platform that
     *  may result in this event unsafe.
     *  @exception IllegalActionException
     */
    protected boolean _safeToProcess(PtidesEvent event)
            throws IllegalActionException {
        boolean result = super._safeToProcess(event);
        if (result == false) {
            return false;
        } else {
            IOPort thisPort = event.ioPort();
            if (event.isPureEvent()) {
                // this event is a pure event.
                if (thisPort == null) {
                    // if the input port is null, i.e., it does not causally relate to any event
                    // that is coming from any other input ports, then this event is always safe
                    // to process
                    return true;
                } else {
                    thisPort = _getOneSinkPort(thisPort);
                }
            }
            // The event is not a pure event, or the event is a pure event,
            // but it depends on events coming from other input ports.
            List<PtidesEvent> eventList = new ArrayList();
            for (ExecutionTimedEvent timedEvent : _currentlyExecutingStack) {
                eventList.addAll((List<PtidesEvent>) timedEvent.contents);
            }
            for (int eventIndex = 0; eventIndex < _eventQueue.size(); eventIndex++) {
                PtidesEvent earlierEvent = ((PtidesListEventQueue) _eventQueue)
                        .get(eventIndex);
                // Since the event queue is sorted in the order of timestamps, any event with
                // larger timestamp cannot causally affect the execution of the event of interest.
                // Any smaller event will also
                if (earlierEvent == event) {
                    break;
                }
                eventList.add(earlierEvent);
            }
            for (PtidesEvent earlierEvent : eventList) {
                IOPort earlierPort = earlierEvent.ioPort();
                if (earlierEvent.isPureEvent()) {
                    if (earlierPort == null) {
                        // The earlier event doesn't have a ioPort, so we assume the pure event
                        // can result in events at all of its output ports.
                        earlierPort = _getOneSinkPort((IOPort) earlierEvent
                                .actor().inputPortList().get(0));
                    } else {
                        earlierPort = _getOneSinkPort(earlierPort);
                    }
                }
                Map<IOPort, Dependency> portDependency = _inputPairDependencies
                        .get(earlierPort);
                if (portDependency != null) {
                    SuperdenseDependency dependency = (SuperdenseDependency) portDependency
                            .get(thisPort);
                    Time timeDifference = event.timeStamp().subtract(
                            earlierEvent.timeStamp());
                    if (dependency != null) {
                        // if the difference in model time between these two events is smaller
                        // or equal to the dependency between the two residing ports, then the
                        // event is unsafe.
                        if (timeDifference.getDoubleValue() > dependency
                                .timeValue()) {
                            return false;
                        } else if (timeDifference.getDoubleValue() == dependency
                                .timeValue()) {
                            // If they are equal, and these two events do not reside at the same
                            // equivalence class, then the event of interest is not safe.
                            if (!_sameInputPortGroup(earlierEvent, event)) {
                                return false;
                            }
                        }
                    }
                }
                //                        throw new IllegalActionException(earlierPort, port, "The dependency " +
                //                                "between these two ports are not calculated, safe to " +
                //                                "process analysis cannot be performed.");
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Computed dependencies between each pair of input ports of actors in the
     *  composite actor that is governed by this director. */
    private Map<IOPort, Map<IOPort, Dependency>> _inputPairDependencies;

    /** Given an input port (a), return one input port (b) such that \delta_0{a, o) < \infty,
     *  where o is an output port, and o is directly connected to b.
     *  @param thisPort
     *  @return one input port.
     *  @exception IllegalActionException
     */
    private IOPort _getOneSinkPort(IOPort thisPort)
            throws IllegalActionException {
        Collection<IOPort> outputPorts = _finiteDependentPorts(thisPort);
        if (outputPorts.size() == 0) {
            throw new IllegalActionException(
                    thisPort.getContainer(),
                    thisPort,
                    "This actor's output ports are not finitely dependent on any "
                            + "of its input ports, We cannot determine whether pure events "
                            + "produced by this actor are safe to process or not.");
        }
        IOPort outputPort = outputPorts.iterator().next();
        List<IOPort> sinkPorts = outputPort.sinkPortList();
        if (sinkPorts.size() == 0) {
            throw new IllegalActionException(outputPort, "This port "
                    + "must be connected to some downstream actor.");
        }
        // This port can be any arbitrary port that is going to receive outputs
        // from this pure event.
        return sinkPorts.get(0);
    }

}
