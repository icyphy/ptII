package ptolemy.domains.ptides.kernel;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** 
 *  This director implements preemptive PTIDES scheduling algorithm, and uses
 *  EDF as the foundation to determine whether we should preempt executing events.
 *  This director is different from its super class because PtidesBasicPreemptiveDDFDirector
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
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */
public class PtidesPreemptiveEDFDirector extends PtidesNoPhysicalTimeDirector {
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
    ////                     public methods                        ////

    /** Clears what's stored in _eventToProcess, and call the super method
     *  of preinitialize.
     *  @exception IllegalActionException If the superclass throws
     *   it or if there is no executive director.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _eventToProcess = null;

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////
    
    /** Return all the events in the event queue that are of the same tag as the event
     *  passed in.
     *  <p>
     *  Notice these events should _NOT_ be taken out of the event queue.
     *  @param event The reference event.
     *  @return List of events of the same tag.
     *  @throws IllegalActionException
     */
    protected List<DEEvent> _getAllSameTagEventsFromQueue(DEEvent event) throws IllegalActionException {
        List<DEEvent> eventList = super._getAllSameTagEventsFromQueue(event);
        for (int i = _peekingIndex; (i - 1) >= 0; i--) {
            DEEvent nextEvent = ((DEListEventQueue)_eventQueue).get(_peekingIndex-1);
            // FIXME: when causality interface for RealDependency works, replace this actor
            // by the same equivalence class.
            if (nextEvent.hasTheSameTagAs(event) && (nextEvent.actor() == event.actor())) {
                eventList.add(nextEvent);
                _peekingIndex--;
            } else {
                break;
            }
        }
        return eventList;
    }

    /** Return the actor associated with this event. This method takes
     *  all events of the same tag destined for the same actor from the event 
     *  queue, and return the actor associated with it.
     *  
     */
    protected Actor _getNextActorToFireForTheseEvents(List<DEEvent> events) throws IllegalActionException {
        if (events.get(0) != ((DEListEventQueue)_eventQueue).get(_peekingIndex)) {
            throw new IllegalActionException("The event to get is not the event pointed " +
                        "to by peeking index.");
        }
        // take from the event queue all the events from the event queue starting
        // for _peekingIndex. Here we assume _peekingIndex is the index of the smallest
        // event in the event queue. that we want to process. (This is set in 
        // _getAllSameTagEventsFromQueue()).
        for (int i = 0; i < events.size(); i++) {
            // We always take the one from _peekingIndex because it points to the next
            // event in the queue once we take the previous one from the event queue.
            ((DEListEventQueue)_eventQueue).take(_peekingIndex);
        }
        return events.get(0).actor();
    }
    
    /** Returns the event that was selected to preempt in _preemptExecutingActor.
     *  If no event was selected, return the event of smallest deadline that is
     *  safe to process.
     *  This is when _eventToProcess is last used in this iteration, so it should be
     *  cleared to null, so that later iterations will not see the same events stored
     *  in _eventToProcess.
     */
    protected DEEvent _getNextSafeEvent() throws IllegalActionException {
        DEEvent tempEvent;
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
    
    /** This method finds the event in the queue that is of the smallest deadline
     *  The event found is stored in _eventToProcess. It then stores the
     *  index of the event in _peekingIndex. 
     *  @return false if no event is found. returns false, otherwise returns true.
     *  @throws IllegalActionException
     */
    protected boolean _getSmallestDeadlineSafeEventFromQueue() throws IllegalActionException {

        // clear _eventToProcess.
        _eventToProcess = null;
        Time smallestDeadline = new Time(this, Double.POSITIVE_INFINITY);
        int result = 0;
        
        for (int eventIndex = 0; eventIndex < _eventQueue.size(); eventIndex++) {
            DEEvent event = ((DEListEventQueue)_eventQueue).get(eventIndex);
            IOPort port = event.ioPort();    
            if (port == null) {
                List<IOPort> inputPortList = event.actor().inputPortList();
                if (inputPortList.size() == 0) {
                    throw new IllegalActionException("When getting the deadline for " +
                            "a pure event at " + event.actor() + ", this actor" +
                            "does not have an input port, thus" +
                    "unable to get relative deadline");
                }
                port = inputPortList.get(0);
            }

            // The event from queue needs to be safe to process AND has smaller deadline.
            if (_safeToProcess(event)) {
                Time absNextDeadline = event.timeStamp().add(_getRelativeDeadline(port));
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
            }
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
     *  @throws IllegalActionException
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
        DoubleTimedEvent doubleTimedEvent = _currentlyExecutingStack.peek();
        List eventList = (List<DEEvent>)(doubleTimedEvent.contents);
        DEEvent executingEvent = (DEEvent)eventList.get(0);
        for (int i = 0; i < eventList.size(); i++) {
            Time absExecutingDeadline = _getAbsoluteDeadline(((DEEvent)eventList.get(i)));
            if (absExecutingDeadline.compareTo(smallestStackDeadline) <= 0) {
                smallestStackDeadline = absExecutingDeadline;
            }
        }

        Time smallestQueueDeadline = _getAbsoluteDeadline(_eventToProcess);
            
        // if we decide not to preempt because the one on stack has smaller deadline,
        // then we set _eventToProcess back to null;
        if (smallestQueueDeadline.compareTo(smallestStackDeadline) > 0 ) {
            _eventToProcess = null;
        } else if (smallestQueueDeadline.compareTo(smallestStackDeadline) == 0 ) {
            if (_eventToProcess.compareTo(executingEvent) >= 0) {
                _eventToProcess = null;    
            } // if the deadline and tag and depth are all equal, don't preempt.
        }

        if (_eventToProcess == null) {
            if (_debugging) {
                _debug("We decided not to do preemption in this round, " +
                        "but to keep executing " +
                        executingEvent.actor() + " at physical time " +
                        _getPhysicalTime());
            }
            return false;
        } else {

            if (_debugging) {
                _debug("We decided to preempt the current " +
                                "executing event at actor: " + 
                                executingEvent.actor() +
                                " with another event at actor: " + _eventToProcess.actor() +
                                ". This preemption happened at physical time " +
                                _getPhysicalTime());
            }

            return true;
        }
    }
    
    /** This method first uses the super of _safeToProcess, which only tests if all 
     *  ports at the same actor
     *  contain an event of larger timestamp. If they do, then the super method
     *  returns true, otherwise it returns false. If the super method (check against
     *  model times) returned true, then the event is safe to process, otherwise,
     *  we check against physical time, and see if the event is safe to process. If
     *  this is true, the event is safe to process, otherwise it is not.
     *  
     *  FIXME: Currently the check in super method is commented out...
     */
    protected boolean _safeToProcess(DEEvent event) throws IllegalActionException {
//        boolean result = super._safeToProcess(event);
        boolean result = false;
        if (result == true) {
            return result;
        } else {
            IOPort port = event.ioPort();
            if (port != null) {
                Parameter parameter = (Parameter)((NamedObj) port).getAttribute("minDelay");
                if (parameter != null) {
                    DoubleToken token = ((DoubleToken)((ArrayToken)parameter.getToken())
                            .arrayValue()[((DETokenEvent)event).channel()]);
                    Time waitUntilPhysicalTime = event.timeStamp().subtract(token.doubleValue());
                    if (_getPhysicalTime().subtract(waitUntilPhysicalTime).compareTo(_zero) >= 0) {
                        return true;
                    } else {
                        _setTimedInterrupt(waitUntilPhysicalTime);
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                // event does not have a destination port, must be a pure event.
                return true;
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ////

    /** The event to be processed next. */
    protected DEEvent _eventToProcess;
}
