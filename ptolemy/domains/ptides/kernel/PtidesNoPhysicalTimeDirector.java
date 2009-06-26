/* Ptides director that does not check against physical time for safe to process.

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
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This director has a local notion time, decoupled from that of the
 *  enclosing director. The enclosing director's time
 *  represents physical time, whereas this time represents model
 *  time in the Ptides model.
 *  Assume the incoming event always has higher priority, so preemption always occurs.
 *  This director implements distributed discrete event simulation as described in
 *  Chandy and Misra, but without the need for null messages (because physical time is
 *  simulated and can be arbitrarily advanced). This director uses DEListEventQueue too
 *  allow access to all events in the event queue in sorted order.
 *
 *  @author Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class PtidesNoPhysicalTimeDirector extends PtidesBasicDirector {

    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesNoPhysicalTimeDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Initialize the actors and request a refiring at the current
     *  time of the executive director. This overrides the base class to
     *  throw an exception if there is no executive director.
     *  @exception IllegalActionException If the superclass throws
     *   it or if there is no executive director.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _eventAtPort = new HashMap<IOPort, PriorityQueue>();

    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Enqueue event and keep track of what event is at which input port.
     *  @param ioPort The port the event resides.
     *  @exception IllegalActionException
     */
    protected void _enqueueTriggerEvent(IOPort ioPort)
            throws IllegalActionException {
        super._enqueueTriggerEvent(ioPort);
        PriorityQueue events = _eventAtPort.get(ioPort);
        if (events == null) {
            events = new PriorityQueue<Tag>();
        }
        events.add(new Tag(getModelTime(), _microstep));
        _eventAtPort.put(ioPort, events);
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
        if (event != ((DEListEventQueue) _eventQueue).get(_peekingIndex)) {
            throw new IllegalActionException(
                    "The event to get is not the event pointed "
                            + "to by peeking index.");
        }
        List<DEEvent> eventList = new ArrayList<DEEvent>();
        eventList.add(event);
        for (int i = _peekingIndex; (i + 1) < _eventQueue.size(); i++) {
            DEEvent nextEvent = ((DEListEventQueue) _eventQueue).get(i + 1);
            // FIXME: when causality interface for RealDependency works, replace this actor
            // by the same equivalence class.
            if (nextEvent.hasTheSameTagAs(event)
                    && (nextEvent.actor() == event.actor())) {
                eventList.add(nextEvent);
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
    protected Actor _getNextActorToFireForTheseEvents(List<DEEvent> events)
            throws IllegalActionException {
        if (events.get(0) != ((DEListEventQueue) _eventQueue)
                .get(_peekingIndex)) {
            throw new IllegalActionException(
                    "The event to get is not the event pointed "
                            + "to by peeking index.");
        }
        // Assume the event queue orders by Tag and depth so all these events should be
        // next to each other.
        for (int i = 0; (_peekingIndex + i) < events.size(); i++) {
            ((DEListEventQueue) _eventQueue).take(_peekingIndex + i);
        }
        return events.get(0).actor();
    }

    /** Return the next event we want to process, which is the event of smallest tag
     *  that is safe to process.
     */
    protected DEEvent _getNextSafeEvent() throws IllegalActionException {
        _peekingIndex = 0;
        while (_peekingIndex < _eventQueue.size()) {
            DEEvent eventFromQueue = ((DEListEventQueue) _eventQueue)
                    .get(_peekingIndex);
            if (_safeToProcess(eventFromQueue)) {
                return eventFromQueue;
            } else {
                _peekingIndex++;
            }
        }
        return null;
    }

    /** For the interested event, we check to see if all ports at the same actor
     *  contain an event of larger timestamp. If they do, then the interested event
     *  is safe to process, otherwise it is not.
     *  If the event is a pure event, it also needs to wait for all events to be
     *  present at the current inputs to ensure this event is safe to process.
     *
     *  FIXME: this does not work properly for variable delay yet.
     *
     *  FIXME: assumes each input port is not multiport.
     *
     *  @param event The event checked for safe to process
     *  @return True if the event is safe to process, otherwise return false.
     *  @exception IllegalActionException
     *  @see #_setTimedInterrupt(Time)
     */
    protected boolean _safeToProcess(DEEvent event)
            throws IllegalActionException {
        IOPort port = event.ioPort();
        Tag currentTag = new Tag(event.timeStamp(), event.microstep());
        // if this event is a pure event, i.e., port == null, then we don't
        // need to keep track of the events.
        Actor actor = null;
        if (port != null) {
            actor = (Actor) port.getContainer();
        } else {
            actor = event.actor();
        }
        boolean result = true;
        for (IOPort inputPort : (List<IOPort>) actor.inputPortList()) {
            PriorityQueue events = _eventAtPort.get(inputPort);
            if (inputPort != port) {
                if (events != null) {
                    Tag time = (Tag) events.peek();
                    if (time != null) {
                        if (time.compareTo(currentTag) < 0) {
                            result = false;
                        }
                    } else {
                        result = false;
                    }
                } else {
                    result = false;
                }
            }
        }

        // the event at this input port is the smallest among them all, so we take it
        // out of _eventAtPort.
        if (result == true) {
            if (port != null) {
                PriorityQueue<Tag> events = _eventAtPort.get(port);
                // since super._getNextActorToFire() removes the smallest event from the queue,
                // it also has to be the smallest event at the current port.
                Tag tag = events.remove();
                if (tag.compareTo(currentTag) != 0) {
                    throw new IllegalActionException("took out the wrong tag");
                }
                _eventAtPort.put(port, events);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ////

    /** The index of the event we are peeking in the event queue. */
    protected int _peekingIndex;

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    private HashMap<IOPort, PriorityQueue> _eventAtPort;

    /** Index that indicates the index of the top event in the event queue that
     *  we want to process at this interation. Notice this index is reset to 0
     *  each iteration within _getNextSafeEvent().
     *  @see #_getNextSafeEvent()
     */

}
