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

import ptolemy.actor.Actor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.lib.ExecutionTimeListener;
import ptolemy.domains.ptides.lib.ExecutionTimeListener.ExecutionEventType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

///////////////////////////////////////////////////////////////////
//// PtidesMulticoreDirector
/**
 *  This director simulates the execution of the Ptides programming model
 *  on multi-core platforms. The goal is to provide a framework for evaluation
 *  of different multi-core execution strategies.
 *
 *  @author Michael Zimmer
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mzimmer)
 *  @Pt.AcceptedRating Red (mzimmer)
 *
 */
public class PtidesMulticoreDirector extends PtidesPreemptiveEDFDirector {    

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
    
    /** The number of cores available for event processing (actor firing). */
    public Parameter coresForEventProcessing;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Initialize all the actors and variables. Perform static analysis on 
     *  superdense dependencies between ports in the topology.
     *  @exception IllegalActionException If the initialize() method of
     *  the super class throws it.
     */
    public void initialize() throws IllegalActionException {     
        super.initialize();
        _calculateSuperdenseDependenices();
        // Create a stack to contain currently processing events on each core.
        int cores = ((IntToken)coresForEventProcessing.getToken()).intValue();
        _currentlyProcessingEvents = 
                new ArrayList<Stack<ProcessingPtidesEvents>>(cores);
        for(int i = 0; i < cores; i++) {
            _currentlyProcessingEvents
                    .add(i, new Stack<ProcessingPtidesEvents>());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Calculate the superdense dependency (minimum model time delay) between
     * a source input port and the input port group of a destination input 
     * port. The Floyd-Warshall algorithm is used to calculate the minimum 
     * model time delay paths.
     * @exception IllegalActionException If the container is not a
     * TypedCompositeActor. 
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

        // Initialize HashMap.
        _superdenseDependencyPair = 
            new HashMap<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>>();
        
        // Create a list for all input ports. A List is needed since Set does 
        // not make any guarantees on iteration order.
        List<TypedIOPort> inputPorts = new ArrayList<TypedIOPort>();
        
        // Calculate superdense dependency from each input port of an
        // actor to the input ports of immediate predecessor actors.
        // This builds a directed graph.
        for(Actor actor : (List<Actor>)((TypedCompositeActor) 
                getContainer()).deepEntityList()) {

            CausalityInterface actorCausality = actor.getCausalityInterface();
            
            for(TypedIOPort inputPort: 
                    (List<TypedIOPort>)(actor.inputPortList())) {
                
                // Ignore input if not connected to anything.
                if(!inputPort.isOutsideConnected()) {
                    continue;
                }

                // Initialize nested HashMap.
                _superdenseDependencyPair.put(inputPort, 
                        new HashMap<TypedIOPort,SuperdenseDependency>());
                
                // Add input port to list.
                inputPorts.add(inputPort);
                
                // Set dependency with self.
                _putSuperdenseDependencyPair(inputPort, inputPort, 
                        SuperdenseDependency.OTIMES_IDENTITY);
    
                for(TypedIOPort outputPort: 
                        (List<TypedIOPort>)(actor.outputPortList())) {
                    // Superdense dependency between input port and output
                    // port of current actor.
                    SuperdenseDependency minDelay = 
                        (SuperdenseDependency) actorCausality.getDependency(
                        inputPort, outputPort);
                    // Only if dependency exists...
                    if(!minDelay.equals(
                            SuperdenseDependency.OPLUS_IDENTITY)) {
                        // Set input port pair for all connected ports.
                        // Assumes no delay from connections.
                        for(TypedIOPort connectedPort: 
                                (List<TypedIOPort>)
                                outputPort.deepConnectedPortList()) {
                            // Exclude ports which aren't input ports.
                            //TODO: is this the right check?
                            if(connectedPort.isInput()) {
                                /*
                                _debug(inputPort.getName(dir) + "->" + 
                                            connectedPort.getName(dir) + ": " +
                                            minDelay.timeValue() + " (" + 
                                            minDelay.indexValue() + ")");
                                */
                                _putSuperdenseDependencyPair(inputPort, 
                                        connectedPort, minDelay);
                            }
                        }
                    } else {
                        //_debug("no dependency");
                    }
                }
            }
        }
        
        // Floyd-Warshall algorithm. This finds the minimum model time delay
        // between all input ports.
        for(TypedIOPort k : inputPorts) {
            for(TypedIOPort i : inputPorts) {
                for(TypedIOPort j : inputPorts) {
                    SuperdenseDependency ij, ik, kj;
                    ij = _getSuperdenseDependencyPair(i, j);
                    ik = _getSuperdenseDependencyPair(i, k);
                    kj = _getSuperdenseDependencyPair(k, j);
                    // Check if i->k->j is better than i->j.
                    if(ij.compareTo(ik.oTimes(kj)) == 
                            SuperdenseDependency.GREATER_THAN) {
                        _putSuperdenseDependencyPair(i, j, 
                                (SuperdenseDependency) ik.oTimes(kj));
                    }
                }
            }
        }
        
        // Currently have the superdense dependency from each source input 
        // port to every destination input port, not the input port group
        // of the destination input port. Find input port groups and fix this.
        for(Actor actor : (List<Actor>)((TypedCompositeActor) 
                getContainer()).deepEntityList()) {
            
            CausalityInterface actorCausality = actor.getCausalityInterface();

            for(TypedIOPort outputPort: 
                    (List<TypedIOPort>)(actor.outputPortList())) {      
                
                Set<TypedIOPort> inputPortGroup = new HashSet<TypedIOPort>();
    
                for(TypedIOPort inputPort: 
                        (List<TypedIOPort>)(actor.inputPortList())) {
                    // Superdense dependency between input port and output
                    // port of current actor.
                    SuperdenseDependency minDelay = 
                        (SuperdenseDependency) actorCausality.getDependency(
                        inputPort, outputPort);
                    // Only if dependency exists...
                    if(!minDelay.equals(
                            SuperdenseDependency.OPLUS_IDENTITY)) {                     
                        inputPortGroup.add(inputPort);   
                    } 
                }
                
                // Go through all source input ports and find the superdense
                // dependency to the current destination input port group of
                // this actor.
                // FIXME: If an actor has non-disjoint input port groups, then
                // the value stored may depend on the order in which input
                // port groups are visited.
                for(TypedIOPort srcPort : inputPorts) {
                    SuperdenseDependency min = 
                        SuperdenseDependency.OPLUS_IDENTITY;
                    for(TypedIOPort destPort : inputPortGroup) {
                        min = (SuperdenseDependency) min.oPlus(
                                    _getSuperdenseDependencyPair(
                                            srcPort, destPort));
                    }
                    if(!min.equals(SuperdenseDependency.OPLUS_IDENTITY)) {
                        for(TypedIOPort destPort : inputPortGroup) {
                            _putSuperdenseDependencyPair(
                                    srcPort, destPort, min);
                        }
                    }
                }
            }
        }

        if(_debugging) {
            StringBuffer buf = new StringBuffer();
            buf.append("\t");
            for(TypedIOPort srcPort : inputPorts) {
                buf.append(srcPort.getName(getContainer()) + "\t");
            }
            _debug(buf.toString());
            for(TypedIOPort srcPort : inputPorts) {
                buf = new StringBuffer();
                buf.append(srcPort.getName(getContainer()) + "\t");
                for(TypedIOPort destPort : inputPorts) {
                    buf.append(_getSuperdenseDependencyPair(srcPort, destPort)
                            .timeValue() + "(" +
                            _getSuperdenseDependencyPair(srcPort, destPort)
                            .indexValue() + ")\t"); 
                }
                _debug(buf.toString());
            }
        }
    } 
    
    /** Model time is only used for correct execution of actors and the
     * scheduler will determine whether another event can be fired in
     * the current firing of the platform, so this method isn't needed.
     *  @return true Always.
     */
    protected boolean _checkForNextEvent() {
        return true;
    }
    
    /** Return the actor to fire in this iteration, or null if no actor should
     * be fired. To simulate execution time, an event may be marked as being
     * processed, but the actor is only fired when execution time expires. This
     * is based on the assumption that an actor only produces events when it
     * is done firing, not during firing. Since _checkForNextEvent() always
     * returns true, this method will keep being called until it returns null.  
     * @exception IllegalActionException If getPlatformPhysicalTag() throws it.
     * 
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
        for(int i = 0; i < _currentlyProcessingEvents.size(); i++) {
            Stack<ProcessingPtidesEvents> coreStack = 
                _currentlyProcessingEvents.get(i);
            if (coreStack.size() != 0) {   
                ProcessingPtidesEvents processingEvent = coreStack.peek();
                int compare = processingEvent.finishTime.compareTo(
                        executionPhysicalTag.timestamp);
                // If event is finished processing, then fire actor.
                if(compare == 0) {
                    PtidesEvent eventToFire = processingEvent.events.get(0);
                    // Actor needs model time to be that of the timestamp.
                    setTag(eventToFire.timeStamp(), eventToFire.microstep());
                    // Remove processing event.
                    coreStack.pop();
                    // Record time that execution finished.
                    _sendExecutionTimeEvent(eventToFire.actor(), 
                            executionPhysicalTag.timestamp.getDoubleValue(), 
                            ExecutionEventType.STOP, i);
                    // Used for pure events.
                    _saveEventInformation(processingEvent.events);
                    _lastActorFired = eventToFire.actor();
                    // Keep PtidesBasicDirector happy.
                    _lastExecutionTime = executionPhysicalTag.timestamp;
                    
                    // Return actor to fire.
                    return eventToFire.actor();
                // Missed it!
                } else if(compare < 0) {
                    throw new IllegalActionException(this, 
                            "Missed firing actor: " + processingEvent);
                // Find earliest finish time amongst cores.
                } else {
                    if(nextFinishTime == null) {
                        nextFinishTime = processingEvent.finishTime;
                    } else if(processingEvent.finishTime.compareTo(
                            nextFinishTime) < 0) {
                        nextFinishTime = processingEvent.finishTime;
                    }
                }
            }
        }
        
        // Request firing at earliest finish time.
        // TODO: preemption may cause a refiring to become useless and
        // require finish times to be modified.
        if(nextFinishTime != null) {
            _debug("next finish time: " + nextFinishTime);
            _fireAtPlatformTime(nextFinishTime, executionTimeClock);
        }
             
        return null;
    }
    
    /** Return an event which is safe-to-process and should be processed next.
     *  If no such event exists, null is returned. In this implementation, the
     *  event that should be processed next is the safe-to-process event that
     *  is earliest in <absolute deadline, tag, microstep, depth> lexical 
     *  ordering. Note that this method does not modify the event
     *  queue.
     *  @return Safe event to process.
     *  @exception IllegalActionException 
     */
    protected PtidesEvent _getNextSafeEvent() throws IllegalActionException {

        // Safe-to-process may set a time when scheduler should be run again.
        _nextRunScheduler = null;
        
        // Put events in EDF order <absolute deadline, tag, microstep, depth>.
        List<PtidesEvent> EDF = new ArrayList<PtidesEvent>(_eventQueue.size());
        for(int i = 0; i < _eventQueue.size(); i++) {
            EDF.add(((PtidesListEventQueue)_eventQueue).get(i));
        }        
        Collections.sort(EDF, new EDFComparator());

        // Return first event in <AD, T, I, D> order that is safe-to-process.
        for(PtidesEvent event : EDF) {
            if(_safeToProcess(event)) {
                //_takeAllSameTagEventsFromQueue.
                // TODO: improve _takeAllSameTagEventsFromQueue to
                // not require _peekingIndex and remove this code.
                for(int i = 0; i < _eventQueue.size(); i++) {
                    if(((PtidesListEventQueue)_eventQueue).get(i).equals(
                            event)) {
                        _peekingIndex = i;
                        break;
                    }
                }
                // End code to remove.
                return event;
            }
        }

        // If no events are safe-to-process, then request a refiring when the
        // earliest event will be. (Assume refiring occurs at end of event
        // processing, so this is based on platform time check)
        if(_nextRunScheduler != null) {
            _debug("next scheduler run: " + _nextRunScheduler);
            _fireAtPlatformTime(_nextRunScheduler, platformTimeClock);
        }
        
        return null;

    }
    
    /** Return the superdense dependency between a source input port and the
     * input port group of a destination input port. If the mapping does not
     * exist, it is assumed to be SuperdenseDependency.OPLUS_IDENTITY.
     * @param source Source input port.
     * @param destination Destination input port.
     * @return Superdense dependency.
     */
    protected SuperdenseDependency _getSuperdenseDependencyPair(
            TypedIOPort source, TypedIOPort destination) {
        if(_superdenseDependencyPair.containsKey(source) &&
                _superdenseDependencyPair.get(source).containsKey(destination))
        {
            return _superdenseDependencyPair.get(source).get(destination);
        } else {
            return SuperdenseDependency.OPLUS_IDENTITY;
        }
    }
    
    /** Store the superdense dependency between a source input port and the
     * input port group of a destination input port. If the mapping does not
     * exist, it is assumed to be SuperdenseDependency.OPLUS_IDENTITY.
     * @param source Source input port.
     * @param destination Destination input port.
     * @param dependency Superdense dependency.
     */
    protected void _putSuperdenseDependencyPair(TypedIOPort source, 
            TypedIOPort destination, SuperdenseDependency dependency) {
        if(!dependency.equals(SuperdenseDependency.OPLUS_IDENTITY)) {
            _superdenseDependencyPair.get(source).put(destination, dependency);
        }
    }
    
    /** Decide whether an event should begin processing at the provided
     * execution time, and if so, what event and core. To process an 
     * event, all events with the same tag and destination input port group
     * are removed from the event queue, and added to the 
     * _currentlyProcessingEvents stack for the core being processed on.
     * This method is responsible for taking overhead execution times into
     * account.
     * @param executionPhysicalTag Current execution physical time.
     * @exception IllegalActionException 
     */
    protected void _runScheduler(Tag executionPhysicalTag) throws IllegalActionException {
        
        _debug("Scheduler running @ " + executionPhysicalTag);
        
        // Get the next event which should be processed (if it exists).
        PtidesEvent nextEvent = _getNextSafeEvent();
        
        // Start processing of all events which can be processed.
        while(nextEvent != null) {
            
            _debug("_getNextSafeEvent(): " + nextEvent);
            
            // Find a core to process next event on, if such a core exists. 
            // This may either be a core which isn't currently processing 
            // an event or a core processing an event which should be 
            // preempted.
            Stack<ProcessingPtidesEvents> coreToProcessOn = null;
            
            // If there are any open cores, then process the event on that
            // core.
            for(Stack<ProcessingPtidesEvents> coreStack : 
                _currentlyProcessingEvents) {
                if(coreStack.size() == 0) {
                    _debug("Found open core.");
                    coreToProcessOn = coreStack;
                    break;
                }
            }
            
            // TODO: preemption
            
            // nextEvent shouldn't be processed yet, so return.
            if(coreToProcessOn == null) {
                _debug("Don't process yet.");
                return;
            }
            
            // Process next event on selected core.
            _debug("Process event at " + nextEvent.actor() + " on core " + 
                    _currentlyProcessingEvents.indexOf(coreToProcessOn));
            
            // Remove events with same tag and destination input port
            // group from event queue.
            List<PtidesEvent> eventList = 
                    _takeAllSameTagEventsFromQueue(nextEvent);
            // Find execution time of actor which event fires.
            Time executionTime = new Time(this, _getExecutionTime(
                    nextEvent.ioPort(), nextEvent.actor()));
            //double executionTime = _getExecutionTime(
            //        nextEvent.ioPort(), nextEvent.actor());
            Time finishTime = 
                    executionPhysicalTag.timestamp.add(executionTime);
            _debug(executionPhysicalTag.timestamp + "+" + executionTime + "=" + finishTime);
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
     * may be safe-to-process. In multi-core, an additional check is required
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

        //TODO: write test cases
        //TODO: _timedInterruptTimes not implemented (used for scheduling
        // overhead)
        
        double delayOffset = 0;
        if(!event.isPureEvent()) {
            _getDelayOffset(event.ioPort(),
                    event.channel(), false);
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
            if(_nextRunScheduler == null) {
                _nextRunScheduler = waitUntilPhysicalTime;
            } else if(waitUntilPhysicalTime.compareTo(
                    _nextRunScheduler) < 0) {
                _nextRunScheduler = waitUntilPhysicalTime;
            }
            return false;
        }

        // If a currently processing event has a superdense dependency with
        // the event being checked, then it COULD causally affect it, so
        // conservatively return not safe to process.
        for(Stack<ProcessingPtidesEvents> coreStack : 
                _currentlyProcessingEvents) {
            for(ProcessingPtidesEvents processingEvents : coreStack) {
                // Only need to consider one of the events since they should
                // all have their destination port in same input port group.
                PtidesEvent processingEvent = 
                    processingEvents.events.get(0);
                // FIXME: ioport of a pure event?
                assert(!event.isPureEvent());
                if(_getSuperdenseDependencyPair(
                        (TypedIOPort) processingEvent.ioPort(), 
                        (TypedIOPort) event.ioPort()) !=
                        SuperdenseDependency.OPLUS_IDENTITY) {
                    // Assume scheduler is run again when event finishes
                    // processing, so no need to request refiring.
                    return false;
                }
            }  
        }
        
        return true;
    }
    
    /** Do nothing. When plotting execution times for multicore, the core
     * id is needed, which this method does not provide.
     * TODO: PtidesBasicDirector should be modified to not call this method.
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
        
        /*
        if (_executionTimeListeners != null) {
            for (ExecutionTimeListener listener : _executionTimeListeners) {
                listener.event(actor, time, event, core);
            }
        }
        */
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** Store the superdense dependency between pairs of input ports using 
     * nested Maps. Providing the source input as a key will return a Map 
     * value, where the destination input port can be used as a key to return 
     * the superdense dependency. 
     */
    protected Map<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>> 
            _superdenseDependencyPair;
    
    /** Store a stack of currently processing events for each core. Each
     * event can be a collection of ptides events with the same tag and 
     * destination input port group. A stack is used since a core can be 
     * currently processing multiple events if preemption occurs.
     */
    protected List<Stack<ProcessingPtidesEvents>> _currentlyProcessingEvents;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Initialize parameters to default values. */
    private void _initParameters() {    
        try {
            coresForEventProcessing = 
                    new Parameter(this, "coresForEventProcessing");
            coresForEventProcessing.setExpression("4");
            coresForEventProcessing.setTypeEquals(BaseType.INT); 
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
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** Compare PtidesEvents based on <absolute deadline, tag, microstep, 
     * depth> lexical ordering.
     */
    protected class EDFComparator implements Comparator<PtidesEvent> {

        /** Return -1, 0, or 1 if the first argument is less than, equal to, 
         * or greater than the seconds argument based on <absolute deadline,
         * tag, microstep, depth> lexical ordering.
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
            
                if(compare != 0) {
                    return compare;
                }
                compare = e1.timeStamp().compareTo(e2.timeStamp());
                if(compare != 0) {
                    return compare;
                }
                if(e1.microstep() < e2.microstep()) {
                    return -1;
                } else if(e1.microstep() > e2.microstep()) {
                    return 1;
                }
                if(e1.depth() < e2.depth()) {
                    return -1;
                } else if(e1.depth() > e2.depth()) {
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
         * input port group */
        protected List<PtidesEvent> events;
        
        /** Execution physical tag when processing started. */
        protected Tag startTag;
        
        /** Execution physical time when processing will finish. */
        // TODO: modify with preemption
        protected Time finishTime;
    }
        
    
}
