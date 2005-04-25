/* A DE event in the DE domain.

Copyright (c) 1998-2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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
package ptolemy.domains.de.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// DEEvent

/**
   This class defines the structure of events in the DE domain.
   Conceptually, a DE event is a trigger that contains a tag and a reference to
   its destination actor. The purpose of a DE event is to schedule its
   destination actor to fire at the timestamp and microstep specified by
   its tag.
   <p>
   A tag is a tuple of a timestamp and a microstep. The timestamp is the model
   time when the event exists. The microstep defines the order of a sequence
   of (simultaneous) events that exist at the same model time.
   <p>
   A DE event is associated with a destination, which is either an actor or
   an IO port of an actor. A DE event, whose destination is an actor, is
   called a <i>pure</i> event. A pure event does not have a destination IO
   port. A DE event, whose destination is an IO port, is called a
   <i>trigger</i> event. A trigger event has a destination actor, which is
   the container of the destination IO port.
   <p>
   A DE event also has a depth, which is the topology information of its
   destinations. For a pure event, the depth is that of its destination actor.
   For a trigger event, the depth is that of its destination IO port. A larger
   value of depth indicates a lower priority when the simulator processes
   events with the same tag.
   <p>
   Two DE events can be compared to see which one happens first. The order
   is defined by the relationship between their time stamps, microsteps, and
   depths. See {@link DEEventQueue} for more details. DE events can be compared
   by using the compareTo() method.
   <p>
   This class is final to improve the simulation performance because new
   events get created and discarded through the whole simulation.

   @author Lukito Muliadi, Edward A. Lee, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Green (hyzheng)
*/
public final class DEEvent implements Comparable {
    /** Construct a pure event with the specified destination actor,
     *  timestamp, microstep, and depth.
     *  @param actor The destination actor
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination actor.
     */
    public DEEvent(Actor actor, Time timeStamp, int microstep, int depth) {
        _actor = actor;
        _ioPort = null;
        _timestamp = timeStamp;
        _microstep = microstep;
        _depth = depth;
    }

    /** Construct a trigger event with the specified destination IO port,
     *  timestamp, microstep, and depth.
     *  @param ioPort The destination IO port.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination IO Port.
     */
    public DEEvent(IOPort ioPort, Time timeStamp, int microstep, int depth) {
        _actor = (Actor) ioPort.getContainer();
        _ioPort = ioPort;
        _timestamp = timeStamp;
        _microstep = microstep;
        _depth = depth;
    }

    /** Return the destination actor for this event.
     *  @return The destination actor.
     */
    public final Actor actor() {
        return _actor;
    }

    /** Compare this event with the argument event for an order.
     *  See {@link #compareTo(DEEvent event)} for the comparison rules.
     *  The argument event has to be an instance of DEEvent. Otherwise, a
     *  ClassCastException will be thrown.
     *
     *  @param event The event to compare against.
     *  @return -1, 0, or 1, depending on the order of the events.
     *  @exception ClassCastException If the argument event is not an instance
     *  of DEEvent.
     */
    public final int compareTo(Object event) {
        return compareTo((DEEvent) event);
    }

    /** Compare the tag and depth of this event with those of the argument
     *  event for the order. Return -1, 0, or 1 if this event happens
     *  earlier than, the same time as, or later than the argument event.
     *  <p>
     *  Their timestamps are compared first. If the two timestamps are not
     *  the same, their order defines the events' order. Otherwise, the
     *  microsteps of events are compared for the order, where an event with
     *  the smaller microstep happens earlier. If the events have the same
     *  microstep, their depths are compared. The event with a smaller depth
     *  happens earlier. If the two events have the same tag and depth, then
     *  they happen at the same time.
     *
     *  @param event The event to compare against.
     *  @return -1, 0, or 1, depends on the order of the events.
     */
    public final int compareTo(DEEvent event) {
        if (timeStamp().compareTo(event.timeStamp()) > 0) {
            return 1;
        } else if (timeStamp().compareTo(event.timeStamp()) < 0) {
            return -1;
        } else if (microstep() > event.microstep()) {
            return 1;
        } else if (microstep() < event.microstep()) {
            return -1;
        } else if (depth() > event.depth()) {
            return 1;
        } else if (depth() < event.depth()) {
            return -1;
        } else {
            return 0;
        }
    }

    /** Return the depth of this event. For a pure event, it is the depth of
     *  the destination actor in the topological sort. For a trigger event, it
     *  is the depth of the destination IO port.
     *  @return The depth of this event.
     */
    public final int depth() {
        return _depth;
    }

    /** Return true if this event has the same tag with the specified one,
     *  and their depths are the same.
     *  @param event The event to compare against.
     *  @return True if this event has the same tag with the specified one,
     *  and their depths are the same.
     */
    public final boolean hasTheSameTagAndDepthAs(DEEvent event) {
        return hasTheSameTagAs(event) && (depth() == event.depth());
    }

    /** Return true if this event has the same tag as the argument DE event.
     *  @param event The DE event to compare against.
     *  @return True if this event has the same tag as the specified one.
     */
    public final boolean hasTheSameTagAs(DEEvent event) {
        return (timeStamp().equals(event.timeStamp()))
            && (microstep() == event.microstep());
    }

    /** Return the destination IO port of this event. Note that
     *  for a pure event, the destination IO Port is null.
     *  @return The destination ioPort.
     */
    public final IOPort ioPort() {
        return _ioPort;
    }

    /** Return the microstep of this event.
     *  @return The microstep of this event.
     */
    public final int microstep() {
        return _microstep;
    }

    /** Return the timestamp.
     *  @return The timestamp.
     */
    public final Time timeStamp() {
        return _timestamp;
    }

    /** Return a description of the event, including the the tag, depth,
     *  and destination information.
     *  @return The token as a string with the time stamp.
     */
    public String toString() {
        if (_ioPort != null) {
            return "DEEvent(time = " + _timestamp + ", microstep = "
                + _microstep + ", depth = " + _depth + ", dest = "
                + ((NamedObj) _actor).getFullName() + "." + _ioPort.getName()
                + ").";
        } else {
            return "DEEvent(time = " + _timestamp + ", microstep = "
                + _microstep + ", depth = " + _depth + ", dest = "
                + ((NamedObj) _actor).getFullName() + ")" + " -- A PURE EVENT.";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the depth of this event if the new depth is no less than
     *  0. Otherwise, do nothing.
     *  @param newDepth The new depth for this event.
     */
    protected void _updateDepth(int newDepth) {
        if (_depth >= 0) {
            _depth = newDepth;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The destination actor.
    private Actor _actor;

    // The depth of this event.
    private int _depth;

    // The destination IO port.
    private IOPort _ioPort;

    // The microstep of this event.
    private int _microstep;

    // The timestamp of the event.
    private Time _timestamp;
}
