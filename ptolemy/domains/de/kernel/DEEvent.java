/* An event in the Ptolemy II DE domain.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DEEvent

/**
This class defines the structure of events in Ptolemy II DE domain.
Conceptually, an event in the Ptolemy II DE domain contains
a token and a tag.  In addition, the event has a destination,
which is an actor and possibly a receiver (for non-pure events).
A pure event has no destination receiver and no token, so methods
for accessing those return null.
The tag consists of a time stamp, a microstep, and a depth.
The microstep represents the phase of execution
when processing simultaneous events in directed loops, or when an
actor schedules itself for firing later at the current time
(using fireAt()).
The depth is the index of the destination actor in a topological
sort.  A larger value of depth represents a lower priority when
processing events.
<p>
The time stamp, microstep, and depth are compared in that order by the
compareTo() method from the Comparable interface.
<p>
@author Lukito Muliadi, Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
@see DEReceiver
@see ptolemy.actor.util.CalendarQueue
@see DEDirector
*/
public final class DEEvent implements Comparable {

    /** Construct an event with the specified destination receiver,
     *  token, time stamp, microstep, and depth. The destination actor is
     *  the one containing the destination receiver.
     *  @param receiver The destination receiver.
     *  @param token The transferred token.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination receiver.
     *  @exception NullPointerException If the receiver is null or is
     *   not contained by a port contained by an actor.
     */
    public DEEvent(DEReceiver receiver, Token token, double timeStamp,
            int microstep, int depth) {
        _receiver = receiver;
        // FIXME: should we check whether the receiver is null?
        // When the receiver is null, it is actually a Pure Event.
        // We should use another constructor.
        if (receiver!= null) {
            _ioPort = (IOPort) receiver.getContainer();
            _actor = (Actor) _ioPort.getContainer();
        } else {
            _ioPort = null;
            _actor = null;
        }
        _token = token;
        _timeStamp = timeStamp;
        _microstep = microstep;
        _receiverDepth = depth;
    }

    /** Construct a pure event with the specified destination actor, time
     *  stamp, microstep, and depth.
     *  @param actor The destination actor
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination receiver.
     */
    public DEEvent(Actor actor, double timeStamp, int microstep, int depth) {
        _actor = actor;
        _ioPort = null;
        _timeStamp = timeStamp;
        _microstep = microstep;
        _receiverDepth = depth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the destination actor for this event.
     *  @return The destination actor.
     */
    public final Actor actor() {
        return _actor;
    }

    /** Compare the tag of this event with the specified event for order.
     *  See compareTo(DEEvent event) for the comparison rules.
     *  The argument has to be an instance of DEEvent or a
     *  ClassCastException will be thrown.
     *
     * @param event The event to compare against.
     * @return -1, 0, or 1, depends on the order of the events.
     * @exception ClassCastException If the argument is not an instance
     *  of DEEvent.
     */
    public final int compareTo(Object event) {
        return compareTo((DEEvent)event);
    }

    /** Compare the tag of this event with the specified event for order.
     *  Return -1, zero, or +1 if this
     *  event is less than, equal to, or greater than the specified event.
     *  The time stamp is checked first.  If the two time stamps are
     *  identical, then the microstep is checked.  If those are identical,
     *  then the receiver depth is checked.
     *
     * @param event The event to compare against.
     * @return -1, 0, or 1, depends on the order of the events.
     */
    public final int compareTo(DEEvent event) {

        if ( _timeStamp > event._timeStamp)  {
            return 1;
        } else if ( _timeStamp < event._timeStamp) {
            return -1;
        } else if ( _microstep > event._microstep) {
            return 1;
        } else if ( _microstep < event._microstep) {
            return -1;
        } else if ( _receiverDepth > event._receiverDepth) {
            return 1;
        } else if ( _receiverDepth < event._receiverDepth) {
            return -1;
        } else {
            return 0;
        }
    }

    /** Return the depth, which is the position of the destination actor
     *  in the topological sort.
     *  @return The depth.
     */
    public final int depth() {
        return _receiverDepth;
    }

    /** Return the ioPort of the destination actor for this event.
     *  @return The destination ioPort.
     */
    public final IOPort ioPort() {
        return _ioPort;
    }

    /** Compare the tag of this event with the specified and return true
     *  if they are equal and false otherwise.  This is provided along
     *  with compareTo() because it is slightly faster when all you need
     *  to know is whether the events are simultaneous.
     *  @param event The event to compare against.
     */
    public final boolean isSimultaneousWith(DEEvent event) {
        return ( _timeStamp == event._timeStamp) &&
            ( _microstep == event._microstep) &&
            ( _receiverDepth == event._receiverDepth);
    }

    /** Return the microstep.
     *  @return The microstep.
     */
    public final int microstep() {
        return _microstep;
    }

    /** Return the destination receiver of this event. If the event is pure,
     *  then return null.
     *  @return The destination receiver
     */
    public final DEReceiver receiver() {
        return _receiver;
    }

    /** Return the time stamp.
     *  @return The time stamp.
     */
    public final double timeStamp() {
        return _timeStamp;
    }

    /** Return the token contained by this event. If the event is pure
     *  then return null.
     *  @return The token in this event.
     */
    public final Token token() {
        return _token;
    }

    /** Return a description of the event, including the contained token
     *  (or "null" if there is none) and the time stamp.
     *  @return The token as a string with the time stamp.
     */
    public String toString() {
        if (_ioPort != null) {
            return "DEEvent(token=" + _token + ", time=" + _timeStamp + ", dest="
                + ((NamedObj)_actor).getFullName() + "." + _ioPort.getName() + ")";
        } else {
            return "DEEvent(token=" + _token + ", time=" + _timeStamp + ", dest="
                + ((NamedObj)_actor).getFullName() + ")" + " -- A PURE EVENT.";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The destination actor.
    private Actor _actor;
    
    // The ioPort of the destination actor.
    private IOPort _ioPort;

    // The microstep.
    private int _microstep;

    // The destination receiver (only set for non-pure events).
    private DEReceiver _receiver;

    // The depth of the destination receiver.
    private int _receiverDepth;

    // The time stamp of the event.
    private double _timeStamp;

    // The token contained by this event.
    private Token _token;
}
