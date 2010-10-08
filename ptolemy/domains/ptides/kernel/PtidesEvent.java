/* A PtidesEvent event that saves the token as well as the timestamp.

 Copyright (c) 2009-2010 The Regents of the University of California.
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
package ptolemy.domains.ptides.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtidesEvent

/** A Ptides event that saves the token, as well as the receiver this token is destined
 *  at, in addition to the information that is stored in the super class, such as
 *  the timestamp, etc. This class is used
 *  in the PTIDES domain, because events may arrive out of timestamp order.
 *  <p>
 *  Note in PtidesEvent, unlike DEEvent, the IOPort parameter is special, in that
 *  it's not necessarily the port the event is destined to, but the port where this
 *  event is causally related with.
 *
 *  @author Jia Zou, Slobodan Matic
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class PtidesEvent extends DEEvent {
    /** Construct a pure event with the specified destination actor,
     *  timestamp, microstep, depth, and minDelay offset.
     *  A pure event is one that does not contained a token (value) that 
     *  is destined to an input port.
     *  @param actor The destination actor
     *  @param ioPort The causally related IO port.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination actor.
     *  @param absoluteDeadline The absolute deadline of this pure event.
     *  @exception IllegalActionException If the actor has a priority parameter,
     *  but its value cannot be obtained, which should be an integer.
     */
    public PtidesEvent(Actor actor, IOPort ioPort, Time timeStamp,
            int microstep, int depth, Time absoluteDeadline)
            throws IllegalActionException {
        super(actor, timeStamp, microstep, depth);
        _ioPort = ioPort;
        _channel = 0;
        _isPureEvent = true;
        _absoluteDeadline = absoluteDeadline;
    }

    /** Construct a trigger event with the specified destination IO port,
     *  timestamp, microstep, and depth.
     *  @param ioPort The destination IO port.
     *  @param channel The channel the event is destined to.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination IO Port.
     *  @param token The token associated with the event.
     *  @param receiver The Receiver the event is destined to.
     *  @exception IllegalActionException If the actor has a priority parameter,
     *  but its value cannot be obtained, which should be an integer.
     */
    public PtidesEvent(IOPort ioPort, int channel, Time timeStamp,
            int microstep, int depth, Token token, Receiver receiver)
            throws IllegalActionException {
        super(ioPort, timeStamp, microstep, depth);
        _channel = channel;
        _token = token;
        _receiver = receiver;
        _isPureEvent = false;
    }

    /** Return the absolute deadline of this event.
     *  A pure event is one that does not contained a token (value) that 
     *  is destined to an input port.
     *  @return absolute deadline if the event is not a pure event.
     *  @exception InternalErrorException If event is not a pure event.
     */
    public final Time absoluteDeadline() {
        // FIXME: This should throw an IllegalActionException because the
        // exception is detectable by the caller.
        if (!isPureEvent()) {
            throw new InternalErrorException("Event is not a pure event, "
                    + "in which case the absolute deadline should be obtained "
                    + "from the destination port of the event.");
        }
        return _absoluteDeadline;
    }


    /** Return the channel this event is destined to.
     *  @return The channel.
     */
    public final int channel() {
        return _channel;
    }

    /** The compareTo method should not override the compareTo of the super class.
     *  This is because in most cases comparison between PtidesEvent's are
     *  for all fields other than the absoluteDeadline. Only for certain classes
     *  of schedulers, and when a pure event is present, do we compare events
     *  using absoluteDeadline as a metric. Those cases should be taken care of
     *  independently within those schedulers.
     */
    public int compareTo(DEEvent event) {
        return super.compareTo(event);
    }

    /** Indicate whether some other object is equal to this PtidesEvent.
     *  PtidesEvents are equal if the super class indicates they are equal
     *  and their tokens, receiver, channel, absoluteDeadline,
     *  as well as indication whether they are pure events are all equal.
     *  @param object The object with which to compare.
     *  @return true if the object is a DEEvent and the fields of
     *  the object and of this object are equal.
     *  @see #hashCode()
     */
    public boolean equals(Object object) {
        if (!(object instanceof PtidesEvent)) {
            return false;
        }
        PtidesEvent event = (PtidesEvent) object;

        return (super.equals(object)
                && event.token().equals(_token)
                && event.isPureEvent() == _isPureEvent
                && event.receiver() == _receiver
                && event.channel() == _channel
                // Only call absoluteEvent if the event is a Pure Event.
                && ((event.isPureEvent() && 
                        event.absoluteDeadline().equals(_absoluteDeadline))
                        || !event.isPureEvent()));
    }

    /** Return the hash code for the event object.
     *  @return The hash code for the event object.
     *  @see #equals(Object)
     */
    public int hashCode() {
        // FIXME: hashCode should take into account the fields added by
        // this class.  Otherwise, two objects that are not equal will have
        // the same hash.  This is permitted, but is less efficient.
        return super.hashCode();
    }

    /** Return true if this event is a pure event.
     *  A pure event is one that does not contained a token that is destined
     *  to an input port.
     *  @return True if this event is a pure event.
     */
    public final boolean isPureEvent() {
        return _isPureEvent;
    }

    /** Return the receiver this event is destined to.
     *  @return The receiver.
     */
    public final Receiver receiver() {
        return _receiver;
    }

    /** Return the token (value) of this event.
     *  @return The token.
     */
    public final Token token() {
        return _token;
    }

    /** Return a description of the event, including the the tag, depth,
     *  the token, and destination information.
     *  @return The token as a string with the time stamp.
     */
    public String toString() {
        // FIXME: Ideally, this would be in a format that could be easily parsed
        // by the expression language, such as a record format.
        String name = "null";
        if (_actor != null) {
            name = ((NamedObj) _actor).getFullName();
        }
        return "PtidesEvent{time = " + _timestamp + ", microstep = "
            + _microstep + ", depth = " + _depth
            + ", token = " + _token
            + ", absoluteDeadline = " + _absoluteDeadline
            + ", dest = " + name + "."
            + (_ioPort == null ? "null" : _ioPort.getName())
            + "." + _channel
            + ", isPureEvent = "
            + _isPureEvent
            + (_receiver == null ? "null" : getClass().getName() + " {"
                    + (_receiver.getContainer() != null ? 
                    _receiver.getContainer().getFullName() : "")
                    + ".receiver }")
            + "}";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The channel this event is destined to */
    private int _channel;

    /** Indicates whether this event is a pure event */
    private boolean _isPureEvent;

    /** The receiver the token variable of this event is destined to. */
    private Receiver _receiver;

    /** The absolute deadline of this event. This field is used only when this
     *  event is a pure event.
     */
    private Time _absoluteDeadline;

    /** The token associated with this event. */
    private Token _token;
}
