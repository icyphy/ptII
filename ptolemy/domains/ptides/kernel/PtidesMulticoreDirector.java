/* This director simulates the execution of the Ptides programming model
   on multi-core platforms.

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.lib.ExecutionTimeListener;
import ptolemy.domains.ptides.lib.ExecutionTimeListener.ExecutionEventType;
import ptolemy.domains.ptides.lib.io.ActuatorPort;
import ptolemy.domains.ptides.lib.io.NetworkReceiverPort;
import ptolemy.domains.ptides.lib.io.NetworkTransmitterPort;
import ptolemy.domains.ptides.lib.io.PtidesPort;
import ptolemy.domains.ptides.lib.io.SensorPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtidesMulticoreDirector
/**
 *  This director simulates the execution of the Ptides programming model
 *  on multicore execution platforms. The goal is to provide a framework for 
 *  evaluation of different multicore execution strategies.
 *
 *  @author Michael Zimmer
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mzimmer)
 *  @Pt.AcceptedRating Red (mzimmer)
 *
 */
public class PtidesMulticoreDirector extends PtidesBasicDirector {    

    /** Construct a PtidesMulticoreDirector in the given container with 
     *  the given name. Parameters for the director are also initialized.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the constructor of the super 
     *  class throws it or there is an error initializing parameters.
     *  @exception NameDuplicationException If the constructor of the super 
     *  class throws it.
     */
    public PtidesMulticoreDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
    }
   
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** If true, the dependency check between the tags of two events for 
     * safe-to-process analysis will be reduced to a binary check for either
     * a finite or infinite minimum model time delay path. This is more
     * conservative, but would reduce data storage overhead on execution
     * platforms.
     */
    public Parameter binaryDependencyCheck;
    
    /** The number of cores available for event processing (actor firing). */
    public Parameter coresForEventProcessing;
    
    /** If true, safe-to-process analysis will consider trigger ports, making
     * it more optimistic. Parameter "isTrigger" must be set to false on a port
     * to have it not be considered a trigger port.
     */
    public Parameter considerTriggerPorts;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Initialize all the actors and variables. Perform static analysis on 
     *  superdense dependencies between input ports in the topology.
     *  @exception IllegalActionException If any of the methods contained
     *  in initialize() throw it.
     */
    public void initialize() throws IllegalActionException  {     
        super.initialize();
        _inputPortsForPureEvent = new HashMap<TypedIOPort, Set<TypedIOPort>>();
        _relativeDeadlineForPureEvent = new HashMap<TypedIOPort, Double>();
        _calculateSuperdenseDependenices();
        _calculateDelayOffsetsTriggerOnly();
        _calculateRelativeDeadlines();
        // Create a stack to contain currently processing events on each core.
        int cores = ((IntToken)coresForEventProcessing.getToken()).intValue();
        _currentlyProcessingEvents = 
                new ArrayList<Stack<ProcessingPtidesEvents>>(cores);
        for (int i = 0; i < cores; i++) {
            _currentlyProcessingEvents
                    .add(i, new Stack<ProcessingPtidesEvents>());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** After an actor has fired, check if it produced any tokens destined
     * for outside the platform.  If it did, remove from event queue and
     * call _transferOutputs().
     * @exception IllegalActionException If cannot set tag or transfer outputs.
     */
    protected void _actorFired() throws IllegalActionException {
        int eventIndex = 0;
        synchronized (_eventQueue) {
            while (eventIndex < _eventQueue.size()) {
                PtidesEvent nextEvent = ((PtidesListEventQueue) _eventQueue)
                        .get(eventIndex);
                if (nextEvent.ioPort() != null &&
                        nextEvent.ioPort().isOutput()) {
                    PtidesEvent ptidesEvent = ((PtidesListEventQueue)
                            _eventQueue).take(eventIndex);
                    setTag(ptidesEvent.timeStamp(), ptidesEvent.microstep());
                    _transferOutputs(nextEvent.ioPort());
                } else {
                    // This event index should point to the next event if no
                    // event is taken from the event queue.
                    eventIndex++;
                }
            }
        }
    }
    
    /** Calculate the delay offset for each input port. If the parameter 
     * 'considerTriggerPorts' is true, then only paths through trigger ports 
     * are considered. The delay offset is used in safe-to-process analysis
     * to know when no future events can occur at a sensor or network 
     * receiver port that can result in an event arriving at an input port
     * with an earlier timestamp than the event currently there.
     * @exception IllegalActionException If cannot set 'delayOffset' parameter
     * for an input port.
     */
    protected void _calculateDelayOffsetsTriggerOnly() 
            throws IllegalActionException {
          
        // Calculate delayOffset to each input port.
        for (TypedIOPort port : _inputPorts) {
            
            // Disallow SensorPort and NetworkReceiverPort.
            if (port instanceof SensorPort || 
                    port instanceof NetworkReceiverPort) {
                continue;
            }
            
            // Find minimum delay offset from all sensor or network receiver 
            // input ports to the input port group of this port.
            double delayOffset = Double.POSITIVE_INFINITY;
            for (TypedIOPort inputPort : _inputPorts) {
                // Only allow SensorPort and NetworkReceiverPort.
                if (!(inputPort instanceof SensorPort || 
                        inputPort instanceof NetworkReceiverPort)) {
                    continue;
                }
                double deviceDelayBound = _getDoubleParameterValue(
                        inputPort, "deviceDelayBound");
                SuperdenseDependency minDelay = 
                    SuperdenseDependency.OPLUS_IDENTITY;
                // Find minimum path to input port group.
                for (TypedIOPort groupPort : _inputPortGroups.get(port)) {
                    minDelay = (SuperdenseDependency)minDelay.oPlus(
                            _getSuperdenseDependencyPair(
                            inputPort, groupPort, true));
                }
                    
                // Check if best so far.
                double thisDelayOffset = minDelay.timeValue()
                        - deviceDelayBound;
                if (thisDelayOffset < delayOffset) {
                    delayOffset = thisDelayOffset;
                }           
            }
            _setDelayOffset(port, delayOffset);
        }
    }
    
    /** Calculate the relative deadline for each input port. The relative
     * deadline is used along with the timestamp of the event at the input port
     * to determine the earliest time that this event may cause for an event 
     * that needs to be output at an actuator or network transmitter.
     * @exception IllegalActionException If cannot set 'relativeDeadline2'
     * parameter or cannot get device delay bound.
     */
    protected void _calculateRelativeDeadlines() 
            throws IllegalActionException {
        
        // Calculate relativeDeadline for each input port.
        for (TypedIOPort port : _inputPorts) {
            
            // Disallow SensorPort and NetworkReceiverPort.
            if (port instanceof SensorPort || 
                    port instanceof NetworkReceiverPort) {
                continue;
            }
            
            // Find minimum model time delay path from the input
            // port to any actuator or network transmitter.
            double relativeDeadline = Double.POSITIVE_INFINITY;
            for (TypedIOPort outputPort : _inputPorts) {
                // Only allow ActuatorPort and NetworkTransmitterPort.
                if (!(outputPort instanceof ActuatorPort || 
                        outputPort instanceof NetworkTransmitterPort)) {
                    continue;
                }
                double deviceDelayBound = 
                    _getDoubleParameterValue(outputPort, "deviceDelayBound");
                SuperdenseDependency minDelay = 
                            _getSuperdenseDependencyPair(
                            port, outputPort, false);  
                    
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
            for (TypedIOPort connectedPort : _inputPortsForPureEvent.get(port)) 
            {
                Double thisRelativeDeadline =
                    _getRelativeDeadline(connectedPort);
                if (thisRelativeDeadline.compareTo(
                        relativeDeadline) < 0) {
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
    protected void _calculateSuperdenseDependenices() 
            throws IllegalActionException {
        
        //TODO: Code assumes code generation is at atomic actor level, so if
        // code generation is modified to cluster atomic actors (to reduce
        // execution overhead) this method will need to be modified.
        // Code generation would also need to handle multiports differently.

        if (!(getContainer() instanceof TypedCompositeActor)) {
            throw new IllegalActionException(getContainer(), 
                    getContainer().getFullName() + 
                    " is not a TypedCompositeActor");
        }

        // Initialize HashMaps. These will end up being identical if parameter
        // 'considerTriggerPorts' is false.
        _superdenseDependencyPair = 
            new HashMap<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>>();
        _superdenseDependencyPairTriggerOnly = 
            new HashMap<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>>();
        
        // Create a list for all input ports. A List is needed since Set does 
        // not make any guarantees on iteration order.
        _inputPorts = new ArrayList<TypedIOPort>();
        
        // Store input port groups for all input ports.
        _inputPortGroups = new
                HashMap<TypedIOPort, Set<TypedIOPort>>();
        
        // Get boolean value of parameter to ignore non-trigger ports.
        boolean considerTrigger = ((BooleanToken)considerTriggerPorts.
                getToken()).booleanValue(); 
        
        // Find all input ports (consider actuator and network transmitter 
        // ports as input ports as well) and add connections to other inputs.
        // This will build a weighted directed graph.
        
        // Add sensor, actuator, and network ports.
        for (TypedIOPort port : (List<TypedIOPort>)
                ((TypedCompositeActor)getContainer()).portList()) {
            
            // Only allow ports which are PtidesPorts.
            if (!(port instanceof PtidesPort)) {
                throw new IllegalActionException(port, 
                        port.getFullName() + 
                        " is not a PtidesPort");
            }

            _addInputPort(port);
            
            // Add path from sensor or network input port to connected 
            // input ports. These connections have a weight of 0.
            if (port instanceof SensorPort || 
                    port instanceof NetworkReceiverPort) {

                for (IOPort connectedPort : (List<IOPort>)
                        (port.insideSinkPortList())) {
                    _putSuperdenseDependencyPair(port, 
                            (TypedIOPort)connectedPort, 
                            SuperdenseDependency.OTIMES_IDENTITY, false);
                    _putSuperdenseDependencyPair(port, 
                            (TypedIOPort)connectedPort, 
                            SuperdenseDependency.OTIMES_IDENTITY, true);
                }
            }
        }
       
        // Calculate superdense dependency from each input port of an
        // actor to the input ports of immediate predecessor actors (or
        // actuators or network transmitters) using causality interface 
        // of the actor.
        for (Actor actor : (List<Actor>)((TypedCompositeActor) 
                getContainer()).deepEntityList()) {

            CausalityInterface actorCausality = actor.getCausalityInterface();
            
            for (TypedIOPort inputPort: 
                    (List<TypedIOPort>)(actor.inputPortList())) {
                
                // Ignore input if it's not connected to anything.
                if (!inputPort.isOutsideConnected()) {
                    continue;
                }

                _addInputPort(inputPort);
    
                for (TypedIOPort outputPort: 
                        (List<TypedIOPort>)(actor.outputPortList())) {
                    // Get superdense dependency between input port and output
                    // port of current actor.
                    SuperdenseDependency minDelay = 
                        (SuperdenseDependency) actorCausality.getDependency(
                        inputPort, outputPort);
                    // Only if dependency exists...
                    if (!minDelay.equals(
                            SuperdenseDependency.OPLUS_IDENTITY)) {
                        // Add connected input ports if this input port can
                        // produce pure events.
                        if (!minDelay.equals(
                                SuperdenseDependency.OTIMES_IDENTITY)) {
                            if (!_inputPortsForPureEvent.containsKey(inputPort)) 
                            {
                                _inputPortsForPureEvent.put(
                                        inputPort, new HashSet<TypedIOPort>());
                            }
                            _inputPortsForPureEvent.get(inputPort).addAll(
                                    (List<TypedIOPort>)
                                    outputPort.deepConnectedPortList());
                        }
                        // Set input port pair for all connected ports.
                        // Assumes no delay from connections.
                        for (TypedIOPort connectedPort: 
                                (List<TypedIOPort>)
                                outputPort.deepConnectedPortList()) {
                            _putSuperdenseDependencyPair(inputPort, 
                                    connectedPort, minDelay, false);
                            if (!considerTrigger || 
                                    _isTriggerPort(inputPort)) {
                                _putSuperdenseDependencyPair(inputPort, 
                                        connectedPort, minDelay, true);
                            }
                        }    
                        // Find input port group.
                        for (TypedIOPort inPort: 
                                (List<TypedIOPort>)(actor.inputPortList())) {
                            minDelay = (SuperdenseDependency) actorCausality
                                    .getDependency(inPort, outputPort);
                            if (!minDelay.equals(
                                    SuperdenseDependency.OPLUS_IDENTITY)) {
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
                    ij = _getSuperdenseDependencyPair(i, j, false);
                    ik = _getSuperdenseDependencyPair(i, k, false);
                    kj = _getSuperdenseDependencyPair(k, j, false);
                    // Check if i->k->j is better than i->j.
                    if (ij.compareTo(ik.oTimes(kj)) == 
                            SuperdenseDependency.GREATER_THAN) {
                        _putSuperdenseDependencyPair(i, j, 
                                (SuperdenseDependency) ik.oTimes(kj), false);
                    }
                    // Only trigger input ports.
                    ij = _getSuperdenseDependencyPair(i, j, true);
                    ik = _getSuperdenseDependencyPair(i, k, true);
                    kj = _getSuperdenseDependencyPair(k, j, true);
                    // Check if i->k->j is better than i->j.
                    if (ij.compareTo(ik.oTimes(kj)) == 
                            SuperdenseDependency.GREATER_THAN) {
                        _putSuperdenseDependencyPair(i, j, 
                                (SuperdenseDependency) ik.oTimes(kj), true);
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
                    buf.append(_getSuperdenseDependencyPair(
                            srcPort, destPort, false)
                            .timeValue() + "(" +
                            _getSuperdenseDependencyPair(
                            srcPort, destPort, false) 
                            .indexValue() + ")\t"); 
                }
                _debug(buf.toString()); 
                buf = new StringBuffer();
                buf.append(srcPort.getName(getContainer()) + "\t");
                for (TypedIOPort destPort : _inputPorts) {
                    buf.append(_getSuperdenseDependencyPair(
                            srcPort, destPort, true)
                            .timeValue() + "(" +
                            _getSuperdenseDependencyPair(
                            srcPort, destPort, true) 
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
    
    /** Return true if the 'source' event cannot causally affect the 'event'
     * event. This method will consider the 'considerTriggerPorts' and
     * 'binaryDependencyCheck' parameters.
     * @param source Event which may causally affect event being checked.
     * @param event Event being checked.
     * @return True if 'source' cannot causally affect 'event'.
     * @exception IllegalActionException If cannot read parameter.
     */
    protected boolean _dependencyCheck(PtidesEvent source, PtidesEvent event) 
            throws IllegalActionException {
        
        boolean binary = ((BooleanToken)binaryDependencyCheck.
                getToken()).booleanValue();
        
        // Find minimum model time delay path to input port group.
        SuperdenseDependency path = 
            SuperdenseDependency.OPLUS_IDENTITY;
        for (TypedIOPort groupPort : _inputPortGroups.get(
                (TypedIOPort)event.ioPort())) {
            path = (SuperdenseDependency) path.oPlus(
                    _getSuperdenseDependencyPair(
                    (TypedIOPort) source.ioPort(), 
                    groupPort, true));
        }
        
        if (binary) {
            // Return false if finite dependency exists.
            if (path != SuperdenseDependency.OPLUS_IDENTITY) {          
                return false;
            }
        } else {
            // Return false if event can arrive with earlier or equal tag.
            int compare = source.timeStamp().add(
                    path.timeValue()).compareTo(event.timeStamp());
            if (compare < 0 || ((compare == 0) && (
                    (source.microstep() + path.indexValue())
                     <= event.microstep()))) {
                return false;
            }
        }
        return true;
        
    }
    /** Put a pure event into the event queue to schedule the given actor to
     *  fire at the specified timestamp.
     *  @param actor Actor to be fired.
     *  @param time Time to fire actor at event.
     *  @param defaultMicrostep Ignored in this base class.
     *  @exception IllegalActionException If the time argument is less than
     *  the current model time, or the depth of the actor has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueEvent(Actor actor, Time time, int defaultMicrostep)
            throws IllegalActionException {
        if ((_eventQueue == null)
                    || ((_disabledActors != null) && _disabledActors
                                .contains(actor))) {
            return;
        }
        
        // Reset the microstep to 1.
        int microstep = 1;
        
        if (time.compareTo(getModelTime()) == 0) {
            // If during initialization, do not increase the microstep.
            // This is based on the assumption that an actor only requests
            // one firing during initialization. In fact, if an actor requests
            // several firings at the same time,
            // only the first request will be granted.
            if (_isInitializing) {
                microstep = _microstep;
            } else {
                microstep = _microstep + 1;
            }
        } else if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(actor,
                    "Attempt to queue an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }
        
        int depth = _getDepthOfActor(actor);
        
        if (_debugging) {
            _debug("enqueue a pure event: ", ((NamedObj) actor).getName(),
                    "time = " + time + " microstep = " + microstep
                            + " depth = " + depth);
        }

        IOPort causalPort = _lastPtidesEvent.ioPort();
        
        Time absoluteDeadline = time.add(
                _relativeDeadlineForPureEvent.get(causalPort));
        
        PtidesEvent newEvent = new PtidesEvent(actor, causalPort, time,
                microstep, depth, absoluteDeadline);
        _eventQueue.put(newEvent);
}
    
    /** Return the absolute deadline for an event. If the event is a trigger
     * event, it's the timestamp added to the relative deadline for the port.
     * If it's a pure event, the absolute deadline was already calculated
     * when the event was created.
     * @param event The event.
     * @return Absolute deadline for the event.
     * @exception IllegalActionException If cannot get absolute or relative 
     * deadline.
     */
    protected Time _getAbsoluteDeadline(PtidesEvent event) 
            throws IllegalActionException {
        if (event.isPureEvent()) {
            return event.absoluteDeadline();
        } else {
            return event.timeStamp().add(
                    _getRelativeDeadline((TypedIOPort) event.ioPort()));
        } 
    }
    
    /** Return the value of the 'delayOffset' parameter for an input port.
     * @param port Input port.
     * @return Delay offset of input port for safe-to-process analysis.
     * @exception IllegalActionException If cannot read parameter.
     */
    protected Double _getDelayOffset(TypedIOPort port) 
            throws IllegalActionException {
        
        Parameter parameter = (Parameter)port.getAttribute("delayOffset2");
        if (parameter != null) {
            return ((DoubleToken)parameter.getToken()).doubleValue();
        } else {
            throw new IllegalActionException(port,
                    "delayOffset2 parameter does not exist at port " +
                    port.getFullName() + ".");
        }
    }
    
    /** Return the value of the 'relativeDeadline2' parameter for an input 
     * port.
     * @param port Input port.
     * @return Relative Deadline of input port.
     * @exception IllegalActionException If cannot read parameter.
     */
    protected Double _getRelativeDeadline(TypedIOPort port) 
            throws IllegalActionException {
        
        Parameter parameter = 
                (Parameter)port.getAttribute("relativeDeadline2");
        if (parameter != null) {
            return ((DoubleToken)parameter.getToken()).doubleValue();
        } else {
            throw new IllegalActionException(port,
                    "relativeDeadline2 parameter does not exist at port " +
                    port.getFullName() + ".");
        }
    }
    
    /** Return the actor to fire in this iteration, or null if no actor should
     * be fired. To simulate execution time, an event may be marked as being
     * processed, but the actor is only fired when execution time expires. This
     * is based on the assumption that an actor only produces events when it
     * is done firing, not during firing. Since _checkForNextEvent() always
     * returns true, this method will keep being called until it returns null.  
     * @exception IllegalActionException If getPlatformPhysicalTag() throws it
     * or missed firing actor at correct time.
     */
    protected Actor _getNextActorToFire() throws IllegalActionException {
        
        // TODO: need good way of representing the reason this method is being
        // fired. Ex. Sensor, execution finishing, event becomes safe, etc.
        // Also, can these refirings be removed easily?
        // Two event queues?
        
        Tag executionPhysicalTag = 
            getPlatformPhysicalTag(executionTimeClock);
        
        // Run scheduler. This may result in event(s) being added to a
        // _currentlyExecutingEvents stack. It may also request a
        // refiring at a future time (Otherwise it would only be fired on
        // sensor/network inputs).
        _runScheduler(executionPhysicalTag);

        // If any of the currently processing events have reached their finish
        // time, they should be fired.
        Time nextFinishTime = null;
        for (int i = 0; i < _currentlyProcessingEvents.size(); i++) {
            Stack<ProcessingPtidesEvents> coreStack = 
                _currentlyProcessingEvents.get(i);
            if (coreStack.size() != 0) {   
                ProcessingPtidesEvents processingEvent = coreStack.peek();
                int compare = processingEvent.finishTime.compareTo(
                        executionPhysicalTag.timestamp);
                // If event is finished processing, then fire actor.
                if (compare == 0) {
                    PtidesEvent eventToFire = processingEvent.events.get(0);
                    // Actor needs model time to be that of the timestamp.
                    setTag(eventToFire.timeStamp(), eventToFire.microstep());
                    // Remove processing event.
                    coreStack.pop();
                    // Record time that execution finished.
                    _sendExecutionTimeEvent(eventToFire.actor(), 
                            executionPhysicalTag.timestamp.getDoubleValue(), 
                            ExecutionEventType.STOP, i);
                    // If another event was preempted, record that it has
                    // started again.
                    if (coreStack.size() != 0) {
                        _sendExecutionTimeEvent(
                            coreStack.peek().events.get(0).actor(), 
                            executionPhysicalTag.timestamp.getDoubleValue(), 
                            ExecutionEventType.START, i);
                    }
                    // Used for pure events.
                    _lastPtidesEvent = eventToFire;
                    // Return actor to fire.
                    return eventToFire.actor();
                // Missed it!
                } else if (compare < 0) {
                    throw new IllegalActionException(this, 
                            "Missed firing actor: " + processingEvent);
                // Find earliest finish time amongst cores.
                } else {
                    if (nextFinishTime == null) {
                        nextFinishTime = processingEvent.finishTime;
                    } else if (processingEvent.finishTime.compareTo(
                            nextFinishTime) < 0) {
                        nextFinishTime = processingEvent.finishTime;
                    }
                }
            }
        }
        
        // Request firing at earliest finish time.
        // TODO: preemption may cause a refiring to become useless and
        // require finish times to be modified.
        if (nextFinishTime != null) {
            _debug("next finish time: " + nextFinishTime);
            _fireAtPlatformTime(nextFinishTime, executionTimeClock);
        }
             
        return null;
    }

    /** Return an event which is safe-to-process and should be processed next.
     *  If no such event exists, null is returned. In this implementation, the
     *  event that should be processed next is the safe-to-process event that
     *  is earliest in &gt;absolute deadline, tag, microstep, depth&lt; lexical 
     *  ordering. Note that this method does not modify the event
     *  queue.
     *  @return Safe event to process.
     *  @exception IllegalActionException If _safeToProcess throws it.
     */
    protected PtidesEvent _getNextSafeEvent() throws IllegalActionException  {

        // Safe-to-process may set a time when scheduler should be run again.
        _nextRunScheduler = null;
        
        // Put events in EDF order <absolute deadline, tag, microstep, depth>.
        List<PtidesEvent> EDF = new ArrayList<PtidesEvent>(_eventQueue.size());
        for (int i = 0; i < _eventQueue.size(); i++) {
            EDF.add(((PtidesListEventQueue)_eventQueue).get(i));
        }        
        Collections.sort(EDF, new EDFComparator());

        // Return first event in <AD, T, I, D> order that is safe-to-process.
        for (PtidesEvent event : EDF) {
            if (_safeToProcess(event)) {
                return event;
            }
        }

        // If no events are safe-to-process, then request a refiring when the
        // earliest event will be. (Assume refiring occurs at end of event
        // processing, so this is based on platform time check)
        if (_nextRunScheduler != null) {
            _debug("next scheduler run: " + _nextRunScheduler);
            _fireAtPlatformTime(_nextRunScheduler, platformTimeClock);
        }
        
        return null;

    }
    
    /** Return the superdense dependency between a source and a destination 
     * input port. If the mapping does not exist, it is assumed to be 
     * SuperdenseDependency.OPLUS_IDENTITY.
     * @param source Source input port.
     * @param destination Destination input port.
     * @param triggerOnly Only consider paths through trigger ports. 
     * @return Superdense dependency.
     */
    protected SuperdenseDependency _getSuperdenseDependencyPair(
            TypedIOPort source, TypedIOPort destination, boolean triggerOnly) {
        Map<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>> pair;
        if (triggerOnly) {
            pair = _superdenseDependencyPairTriggerOnly;        
        } else {
            pair = _superdenseDependencyPair;
        }
        if (pair.containsKey(source) &&
                pair.get(source).containsKey(destination))
        {
            return pair.get(source).get(destination);
        } else {
            return SuperdenseDependency.OPLUS_IDENTITY;
        }
    }
    
    /** Return whether a port is considered a trigger port. Assumes trigger 
     * port by default.
     * @param port Input port.
     * @return True if trigger port, false otherwise.
     * @exception IllegalActionException Cannot read parameter.
     */
    protected boolean _isTriggerPort(TypedIOPort port) 
            throws IllegalActionException {
        //TODO: part of analysis instead of parameter?
        boolean isTrigger = true;
        Parameter isTriggerParameter = (Parameter)
                port.getAttribute("isTrigger");
        if (isTriggerParameter != null) {
            isTrigger = ((BooleanToken)isTriggerParameter.getToken())
                    .booleanValue();
        }
        return isTrigger;
    }
    /** Store the superdense dependency between a source and destination input 
     * port. If the mapping does not exist, it is assumed to be 
     * SuperdenseDependency.OPLUS_IDENTITY.
     * @param source Source input port.
     * @param destination Destination input port.
     * @param triggerOnly Only consider paths through trigger ports.
     * @param dependency Superdense dependency.
     */
    protected void _putSuperdenseDependencyPair(TypedIOPort source, 
            TypedIOPort destination, SuperdenseDependency dependency,
            boolean triggerOnly) {
        Map<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>> pair;
        if (triggerOnly) {
            pair = _superdenseDependencyPairTriggerOnly;        
        } else {
            pair = _superdenseDependencyPair;
        }
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
        List<PtidesEvent> eventList = new ArrayList<PtidesEvent>();
        int i = 0;
        while (i < _eventQueue.size()) {
            PtidesEvent eventInQueue = 
                    ((PtidesListEventQueue)_eventQueue).get(i);
            // If event has same tag and destined to same actor, remove from
            // queue.
            // TODO: or input port group?
            if (eventInQueue.hasTheSameTagAs(event) && 
                    eventInQueue.actor().equals(event.actor())) {
                eventList.add(eventInQueue);
                ((PtidesListEventQueue)_eventQueue).take(i);
                continue; 
            }
            i++;
        }
        return eventList;
    }
    
    /** Decide whether an event should begin processing at the provided
     * execution time, and if so, what event and core. To process an 
     * event, all events with the same tag and destination input port group
     * are removed from the event queue, and added to the 
     * _currentlyProcessingEvents stack for the core being processed on.
     * This method is responsible for taking overhead execution times into
     * account.
     * @param executionPhysicalTag Current execution physical time.
     * @exception IllegalActionException If _getNextSafeEvent() throws it.
     */
    protected void _runScheduler(Tag executionPhysicalTag) 
            throws IllegalActionException {
        
        _debug("Scheduler running @ " + executionPhysicalTag);
        
        // Get the next event which should be processed (if it exists).
        PtidesEvent nextEvent = _getNextSafeEvent();
        
        // Start processing of all events which can be processed.
        while (nextEvent != null) {
            
            _debug("_getNextSafeEvent(): " + nextEvent);
            
            // Find a core to process next event on, if such a core exists. 
            // This may either be a core which isn't currently processing 
            // an event or a core processing an event which should be 
            // preempted.
            Stack<ProcessingPtidesEvents> coreToProcessOn = null;
            
            // If there are any open cores, then process the event on that
            // core.
            for (Stack<ProcessingPtidesEvents> coreStack : 
                _currentlyProcessingEvents) {
                if (coreStack.size() == 0) {
                    _debug("Found open core.");
                    coreToProcessOn = coreStack;
                    break;
                }
            }
            
            // Preempt the event with latest deadline if selected event has
            // earlier deadline.
            if (coreToProcessOn == null) {
                EDFComparator comparator = new EDFComparator();
                // Find processing event with latest deadline.
                PtidesEvent latestEvent = null;
                Stack<ProcessingPtidesEvents> latestCore = null;
                for (Stack<ProcessingPtidesEvents> coreStack : 
                    _currentlyProcessingEvents) {
                    if (coreStack.size() != 0) {
                        PtidesEvent processingEvent = 
                            coreStack.peek().events.get(0);
                        if (latestEvent == null || comparator.compare(
                                processingEvent, latestEvent) > 0) {
                            latestEvent = processingEvent;
                            latestCore = coreStack;
                        }
                    }
                }
                // If next event has earlier deadline than the lastest deadline
                // processing event, preempt it.
                if (comparator.compare(nextEvent, latestEvent) < 0) {
                    _debug("preempt " + latestEvent.actor().getName());
                    coreToProcessOn = latestCore;
                    // Execution time of event causing preemption.
                    // TODO: better way for execution time for pure event?
                    Time executionTime;
                    if (nextEvent.isPureEvent()) {
                        executionTime = new Time(this, _getExecutionTime(
                                null, nextEvent.actor()));
                    } else 
                        executionTime = new Time(this, _getExecutionTime(
                                nextEvent.ioPort(), nextEvent.actor()));
                    // Add this to all the finish times on the core.
                    for (ProcessingPtidesEvents events : latestCore) {
                        events.finishTime = 
                            events.finishTime.add(executionTime);
                        _sendExecutionTimeEvent(events.events.get(0).actor(), 
                            executionPhysicalTag.timestamp.getDoubleValue(), 
                           ExecutionEventType.PREEMPTED, 
                           _currentlyProcessingEvents.indexOf(latestCore));
                    }
                }
            }
            
            // nextEvent shouldn't be processed yet, so return.
            if (coreToProcessOn == null) {
                _debug("Don't process yet.");
                return;
            }
            
            // Process next event on selected core.
            _debug("Process event at " + nextEvent.actor() + " on core " + 
                    _currentlyProcessingEvents.indexOf(coreToProcessOn));
            
            // Remove events with same tag and destination input port
            // group from event queue.
            List<PtidesEvent> eventList = 
                    _removeEventsFromQueue(nextEvent);
            // Find execution time of actor which event fires.
            // TODO: better way for execution time for pure event?
            // Currently the execution time of the actor is used.
            Time executionTime;
            if (nextEvent.isPureEvent()) {
                executionTime = new Time(this, _getExecutionTime(
                        null, nextEvent.actor()));
            } else 
                executionTime = new Time(this, _getExecutionTime(
                        nextEvent.ioPort(), nextEvent.actor()));  
            Time finishTime = 
                    executionPhysicalTag.timestamp.add(executionTime);
            _debug(executionPhysicalTag.timestamp + "+" + executionTime 
                    + "=" + finishTime);
            // Record events as processing.
            ProcessingPtidesEvents events = new ProcessingPtidesEvents(
                    eventList, executionPhysicalTag, finishTime);
            coreToProcessOn.push(events);
            // Record time execution started.
            _sendExecutionTimeEvent(nextEvent.actor(), 
                    executionPhysicalTag.timestamp.getDoubleValue(), 
                    ExecutionEventType.START,
                    _currentlyProcessingEvents.indexOf(coreToProcessOn));
            // TODO: handle core id better?
            
            nextEvent = _getNextSafeEvent();
        }
        
        
        
    }
    
    /** Check whether the event is safe to process. If the platform time has
     * passed the timestamp of the event minus the delayOffset, then no
     * later events arriving at a sensor/network port can reach the input
     * port group of the event with an earlier timestamp, and the event
     * may be safe-to-process. In multicore, an additional check is required
     * since a currently processing event MAY causally affect the event 
     * (the processing event may eventually cause an event to arrive at the 
     * input port group of the event with an earlier timestamp). A subclass
     * may improve this check (make less conservative) by knowing more about
     * operation of actors and/or tokens of events.
     * @param event Event being checked if it is safe-to-process.
     * @return True if the event is safe-to-process, false otherwise.
     * @exception IllegalActionException If can't get delayOffset for a port.
     */
    protected boolean _safeToProcess(PtidesEvent event) 
            throws IllegalActionException {

        double delayOffset;
        if (event.isPureEvent()) {
            // FIXME: This is not correct if pure event updates state.
            delayOffset = Double.POSITIVE_INFINITY;
        } else {
            delayOffset = _getDelayOffset((TypedIOPort)event.ioPort());
        }
        
        // If the platform time has not passed the timestamp of the event 
        // minus the delay offset, return false.
        Time waitUntilPhysicalTime = event.timeStamp().
                subtract(delayOffset);
        Tag platformPhysicalTag = 
                getPlatformPhysicalTag(platformTimeClock);
        int compare = platformPhysicalTag.timestamp.compareTo(
                waitUntilPhysicalTime);
        int microstep = platformPhysicalTag.microstep;
        if ((compare < 0) || compare == 0 && 
                (microstep < event.microstep())) {
            if (_nextRunScheduler == null) {
                _nextRunScheduler = waitUntilPhysicalTime;
            } else if (waitUntilPhysicalTime.compareTo(
                    _nextRunScheduler) < 0) {
                _nextRunScheduler = waitUntilPhysicalTime;
            }
            return false;
        }

        // If a currently processing event has a superdense dependency with
        // the event being checked, then it COULD causally affect it, so
        // conservatively return not safe to process.
        for (Stack<ProcessingPtidesEvents> coreStack : 
                _currentlyProcessingEvents) {
            for (ProcessingPtidesEvents processingEvents : coreStack) {
                // Only need to consider one of the events since they should
                // all have their destination port in same input port group.
                PtidesEvent processingEvent = 
                    processingEvents.events.get(0);
                if (!_dependencyCheck(processingEvent, event)) {
                    return false;
                }
            }  
        }
        

        if (!((BooleanToken)considerTriggerPorts.
                getToken()).booleanValue()) {
            return true;
        }
        // If non-trigger ports are ignored, an event which causally affects 
        // this event may have failed safe to process, so this event must not 
        // be safe to process.
        // Put events in EDF order <absolute deadline, tag, microstep, depth>.
        List<PtidesEvent> EDF = new ArrayList<PtidesEvent>(_eventQueue.size());
        for (int i = 0; i < _eventQueue.size(); i++) {
            EDF.add(((PtidesListEventQueue)_eventQueue).get(i));
        }        
        Collections.sort(EDF, new EDFComparator());
        for (PtidesEvent earlierEvent : EDF) {
            // If no earlier events causally affect the event, then safe
            // to process.
            if (earlierEvent.equals(event)) {
                return true;
            }
            if (!_dependencyCheck(earlierEvent, event)) {
                return false;
            }

        }
        
        return true;
    }
    
    /** Do nothing. When plotting execution times for multicore, the core
     * id is needed, which this method does not provide.
     * TODO: PtidesBasicDirector could be modified to not call this method.
     * @param actor Actor that produced an execution time event.
     *  @param time Time when the event occurred.
     *  @param event Type of the event. 
     */
    protected void _sendExecutionTimeEvent(Actor actor, double time,
            ExecutionEventType event) {

    }
    
    /** Send an execution time event to all listeners.
     *  @param actor Actor that produced an execution time event.
     *  @param time Time when the event occurred.
     *  @param event Type of the event.
     *  @param core Core the event occured on.
     */
    protected void _sendExecutionTimeEvent(Actor actor, double time,
            ExecutionEventType event, int core) {
        _debug(actor + " " + time + " " + event + " " + core);
        
        if (_executionTimeListeners != null) {
            for (ExecutionTimeListener listener : _executionTimeListeners) {
                listener.event(actor, time, event, core);
            }
        }
        
    }
    
    /** Set the value of the 'delayOffset' parameter for an input port.
     * @param port Input port.
     * @param delayOffset Delay offset for safe-to-process analysis.
     * @exception IllegalActionException If cannot set parameter.
     */
    protected void _setDelayOffset(TypedIOPort port, Double delayOffset) 
            throws IllegalActionException {
        
        DoubleToken token = new DoubleToken(delayOffset);    
        Parameter parameter = (Parameter)port.getAttribute("delayOffset2");
        if (parameter == null) {
            try {
                parameter = new Parameter(port, "delayOffset2", token);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(port,
                        "delayOffset2 parameter already exists at " +
                        port.getFullName() + ".");
            }
        } else {
            parameter.setToken(token);
        }


    }
    
    /** Set the value of the 'relativeDeadline' parameter for an input port.
     * @param port Input port.
     * @param relativeDeadline Relative deadline for input port.
     * @exception IllegalActionException If cannot set parameter.
     */
    protected void _setRelativeDeadline(TypedIOPort port, 
            Double relativeDeadline) throws IllegalActionException {
        DoubleToken token = new DoubleToken(relativeDeadline);    
        Parameter parameter = 
                (Parameter)port.getAttribute("relativeDeadline2");
        if (parameter == null) {
            try {
                parameter = new Parameter(port, "relativeDeadline2", token);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(port,
                        "relativeDeadline2 parameter already exists at " +
                        port.getFullName() + ".");
            }
        } else {
            parameter.setToken(token);
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
    
    /** Store the superdense dependency between pairs of input ports using 
     * nested Maps. Providing the source input as a key will return a Map 
     * value, where the destination input port can be used as a key to return 
     * the superdense dependency. 
     */
    protected Map<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>> 
            _superdenseDependencyPair;

    /** Store the superdense dependency between pairs of input ports using 
     * nested Maps, where only paths through trigger ports are considered. 
     * If trigger ports are to not be considered, this will be the same as
     * _superdenseDependencyPair.
     * Providing the source input as a key will return a Map value, where the 
     * destination input port can be used as a key to return the superdense
     * dependency. 
     */
    protected Map<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>> 
            _superdenseDependencyPairTriggerOnly;
    
    /** Store a stack of currently processing events for each core. Each
     * event can be a collection of ptides events with the same tag and 
     * destination input port group. A stack is used since a core can be 
     * currently processing multiple events if preemption occurs.
     */
    protected List<Stack<ProcessingPtidesEvents>> _currentlyProcessingEvents;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    private void _addInputPort(TypedIOPort inputPort) {
        
        // Initialize nested HashMaps.
        _superdenseDependencyPair.put(inputPort, 
                new HashMap<TypedIOPort,SuperdenseDependency>());
        _superdenseDependencyPairTriggerOnly.put(inputPort,
                new HashMap<TypedIOPort,SuperdenseDependency>());
        
        // Add input port to list.
        _inputPorts.add(inputPort);
        
        // Initialize input port groups.
        _inputPortGroups.put(inputPort, new HashSet<TypedIOPort>());;
        
        // Set dependency with self.
        _putSuperdenseDependencyPair(inputPort, inputPort, 
                SuperdenseDependency.OTIMES_IDENTITY, false);
        _putSuperdenseDependencyPair(inputPort, inputPort, 
                SuperdenseDependency.OTIMES_IDENTITY, true);
        
    }
    
    /** Initialize parameters to default values. */
    private void _initParameters() {    
        try {
            
            coresForEventProcessing = 
                    new Parameter(this, "coresForEventProcessing");
            coresForEventProcessing.setExpression("2");
            coresForEventProcessing.setTypeEquals(BaseType.INT); 
            
            binaryDependencyCheck = 
                    new Parameter(this, "binaryDependencyCheck");
            binaryDependencyCheck.setExpression("false");
            binaryDependencyCheck.setTypeEquals(BaseType.BOOLEAN);
            
            considerTriggerPorts = 
                    new Parameter(this, "considerTriggerPorts");
            considerTriggerPorts.setExpression("true");
            considerTriggerPorts.setTypeEquals(BaseType.BOOLEAN);
            
        } catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        }    
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The time when the scheduler should next check whether any
     * events should be processed. In some hardware implementations,
     * this could be the time an interrupt is set to.
     */
    private Time _nextRunScheduler;
    
    /** Connected input ports for an input port which may produce a pure 
     * event. Used to calculate _relativeDeadlineForPureEvent. 
     */
    private Map<TypedIOPort, Set<TypedIOPort>> _inputPortsForPureEvent;
    
    /** Store the last PtidesEvent that was fired. */
    private PtidesEvent _lastPtidesEvent;
    
    /** Map the input port where an event caused a pure event to the relative
     * deadline for that pure event.
     */
    private Map<TypedIOPort, Double> _relativeDeadlineForPureEvent;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** Compare PtidesEvents based on &gt;absolute deadline, tag, microstep, 
     * depth&lt; lexical ordering.
     */
    protected class EDFComparator implements Comparator<PtidesEvent> {

        /** Return -1, 0, or 1 if the first argument is less than, equal to, 
         * or greater than the seconds argument based on &gt;absolute deadline,
         * tag, microstep, depth&lt; lexical ordering.
         * @param e1 The first PtidesEvent.
         * @param e2 The seconds PtidesEvent.
         * @return Integer -1, 0, or 1 if first argument is less than,
         * equal to, or greater than the second argument.
         */
        public int compare(PtidesEvent e1, PtidesEvent e2) {
            _debug("COMPARE e1: " + e1 + "\nCOMPARE e2: " + e2);
            try {
                int compare;
                compare = _getAbsoluteDeadline(e1).compareTo(
                        _getAbsoluteDeadline(e2));
            
                if (compare != 0) {
                    return compare;
                }
                compare = e1.timeStamp().compareTo(e2.timeStamp());
                if (compare != 0) {
                    return compare;
                }
                if (e1.microstep() < e2.microstep()) {
                    return -1;
                } else if (e1.microstep() > e2.microstep()) {
                    return 1;
                }
                if (e1.depth() < e2.depth()) {
                    return -1;
                } else if (e1.depth() > e2.depth()) {
                    return 1;
                } else {
                    return 0;
            }
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
            return 0;
        }
        
        
    }
    
    /** A group of PtidesEvent being processed with the same tag and
     * destination input port group, along with the start tag of
     * execution and finish time (execution physical time). */
    protected class ProcessingPtidesEvents {
        
        /** Construct the group of PtidesEvent being processed with the
         * provided arguments.
         * @param events Group of PtidesEvent (with same tag and destination
         * input port group) being processed.
         * @param startTag Execution physical tag when processing started.
         * @param finishTime Execution physical time when processing will 
         * finish.
         */
        protected ProcessingPtidesEvents(List<PtidesEvent> events,
                Tag startTag, Time finishTime) {
            this.events = events;
            this.startTag = startTag;
            this.finishTime = finishTime;
        }
        
        /** Group of PtidesEvent with same tag and destination
         * input port group. */
        protected List<PtidesEvent> events;
        
        /** Execution physical tag when processing started. */
        protected Tag startTag;
        
        /** Execution physical time when processing will finish. */
        protected Time finishTime;
    }
        
}
