/* A PtidesEvent event that saves the token as well as the timestamp.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

/** This class defines the structure of events in the Ptides domain.
 *  Conceptually, a Ptides event is the same as a DE event {@link DEEvent}.
 *  However, scheduling in Ptides is more flexible than in DE. In
 *  order to support this flexibility, fields such as token, receiver,
 *  and absoluteDeadline are added.
 *  <p>
 *  A Ptides event can be of two kinds. A pure event, or a non-pure
 *  (trigger) event. For all pure events, the absolute deadline of
 *  this event is saved. For all non-pure events, the token as well
 *  as the destination receiver is saved. These information are saved
 *  in addition to the information that is stored in the super class,
 *  such as the timestamp, etc. This class is used in the Ptides domain.
 *  <p>
 *  Note in PtidesEvent, unlike DEEvent, the ioPort parameter is overloaded.
 *  If the event is not a pure event, then ioPort indicate the destination
 *  port for this event. However, if the event is a pure event, then ioPort
 *  is the source input port for this event.
 *  <p>
 *  The semantics of equals() and compareTo() in this method are tricky.
 *  equals() indicates two Ptides events are equal if all fields in this
 *  class and the superclass are equal (including absoluteDeadline, token,
 *  channel, and receiver, etc).
 *  However, the compareTo() method does not override that of the super class.
 *  The semantics of the compareTo() is such that two events are equal
 *  if the timestamps, microstep, depth, and priority fields indicate they
 *  are equal. Note
 *  CompareTo() should be called by Ptides directors that try to order
 *  events based on event's timestamps.
 *  If the Ptides director wishes to order events based on deadlines, it
 *  should check event deadlines first. Only in the case when deadlines
 *  of events are equal, should compareTo() be called.
 *
 *  @author Jia Zou, Slobodan Matic
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class PtidesEvent extends DEEvent {
    /** Construct a pure event with the specified destination actor,
     *  timestamp, microstep, depth, and absoluteDeadline.
     *  This constructor should be used if this event is a pure event.
     *  A pure event is one that does not contained a token (value) that
     *  is destined to an input port.
     *  @param actor The destination actor
     *  @param ioPort The causally related IO port.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination actor.
     *  @param absoluteDeadline The absolute deadline of this pure event.
     *          This field should not be null.
     *  @param sourceTimestamp The timestamp of the event on the source platform.
     *  @exception IllegalActionException If the actor has a priority parameter,
     *          but its value cannot be obtained, which should be an integer.
     */
    public PtidesEvent(Actor actor, IOPort ioPort, Time timeStamp,
            int microstep, int depth, Time absoluteDeadline,
            Time sourceTimestamp) throws IllegalActionException {
        super(actor, timeStamp, microstep, depth);
        assert absoluteDeadline != null;
        _ioPort = ioPort;
        _channel = 0;
        _isPureEvent = true;
        _absoluteDeadline = absoluteDeadline;
        _sourceTimestamp = sourceTimestamp;
    }

    /** Construct a trigger event with the specified destination IO port,
     *  timestamp, microstep, and depth.
     *  This constructor should be used if the event is a trigger event
     *  (a non-pure event that is destined to a port).
     *  To construct trigger event, neither _token nor _receiver should be null.
     *  @param ioPort The destination IO port.
     *  @param channel The channel the event is destined to.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination IO Port.
     *  @param token The token associated with the event. This field should
     *          not be null.
     *  @param receiver The Receiver the event is destined to. This field
     *          should not be null.
     *  @param sourceTimestamp The timestamp of the event on the source platform.
     *  @exception IllegalActionException If the actor has a priority parameter,
     *  but its value cannot be obtained, which should be an integer.
     */
    public PtidesEvent(IOPort ioPort, int channel, Time timeStamp,
            int microstep, int depth, Token token, Receiver receiver,
            Time sourceTimestamp) throws IllegalActionException {
        super(ioPort, timeStamp, microstep, depth);
        assert token != null && receiver != null;
        _channel = channel;
        _token = token;
        _receiver = receiver;
        _isPureEvent = false;
        _sourceTimestamp = sourceTimestamp;
    }

    /** Construct a trigger event with the specified destination IO port,
     *  timestamp, microstep, and depth.
     *  This constructor should be used if the event is a trigger event
     *  (a non-pure event that is destined to a port).
     *  To construct trigger event, neither _token nor _receiver should be null.
     *  @param ioPort The destination IO port.
     *  @param channel The channel the event is destined to.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination IO Port.
     *  @param token The token associated with the event. This field should
     *          not be null.
     *  @param receiver The Receiver the event is destined to. This field
     *          should not be null.
     *  @param deadline Time when the event has to be processed.
     *  @param sourceTimestamp The timestamp of the event on the source platform.
     *  @exception IllegalActionException If the actor has a priority parameter,
     *  but its value cannot be obtained, which should be an integer.
     */
    public PtidesEvent(IOPort ioPort, int channel, Time timeStamp,
            int microstep, int depth, Token token, Receiver receiver,
            Time deadline, Time sourceTimestamp) throws IllegalActionException {
        this(ioPort, channel, timeStamp, microstep, depth, token, receiver,
                sourceTimestamp);
        _absoluteDeadline = deadline;
    }

    /** Return the absolute deadline of this event if the event is a
     *  pure event.
     *  A pure event is one that does not contained a token (value) that
     *  is destined to an input port.
     *  @return absolute deadline if the event is a pure event.
     *  @exception InternalErrorException If event is not a pure event,
     *          or the event is a pure event and absoluteDeadline is null.
     */
    public final Time absoluteDeadline() {
        //        if (!isPureEvent()) {
        //            throw new InternalErrorException("This event is not a pure event, "
        //                    + "in which case the absolute deadline should be obtained "
        //                    + "from the destination port of the event.");
        //        }
        return _absoluteDeadline;
    }

    /** Return the destination channel for this event.
     *  @return The channel The destination channel for this event.
     */
    public final int channel() {
        return _channel;
    }

    /** Indicate whether some other object is equal to this PtidesEvent.
     *  PtidesEvents are equal if the super class indicates they are equal
     *  and the event types (pure vs. non-pure) are the same, and
     *  their receivers are the same object, and the channels, tokens,
     *  and absoluteDeadline values are the same.
     *  @param object The object with which to compare.
     *  @return true if the object is a DEEvent and the fields of
     *  the object and of this object are equal.
     *  @see #hashCode()
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PtidesEvent)) {
            return false;
        }
        PtidesEvent event = (PtidesEvent) object;
        boolean result = super.equals(object);
        if (!event.isPureEvent()) {
            result = result && event.token().equals(_token);
        } else {
            if (event.absoluteDeadline() == null && _absoluteDeadline == null) {
                // Not needed: result = result && true;
            } else if (event.absoluteDeadline() == null
                    || _absoluteDeadline == null) {
                return false;
            } else {
                result = result
                        && event.absoluteDeadline().equals(_absoluteDeadline);
            }
        }
        return result && event.isPureEvent() == _isPureEvent
                && event.receiver() == _receiver && event.channel() == _channel;
    }

    /** Return the hash code for the event object.
     *  @return The hash code for the event object.
     *  @see #equals(Object)
     */
    @Override
    public int hashCode() {
        int primitiveFieldHash = super.hashCode() >>> _channel;
        int absoluteDeadlineHash = _absoluteDeadline == null ? 0
                : _absoluteDeadline.hashCode();
        int objectFieldHash = isPureEvent() ? absoluteDeadlineHash : _token
                .hashCode() >>> _receiver.hashCode();
        return primitiveFieldHash >>> objectFieldHash;
    }

    @Override
    public boolean hasTheSameTagAs(DEEvent event) {
        Actor actor = event.actor();
        if (actor == null) {
            actor = (Actor) event.ioPort().getContainer();
        }
        Double clockSyncBound = null;
        try {
            clockSyncBound = PtidesDirector._getDoubleParameterValue(
                    (NamedObj) actor, "_clockSynchronizationBound");
        } catch (IllegalActionException e) {
            // In this case timePrecision is set to 0.0 in the next lines.
        }
        if (clockSyncBound == null) {
            clockSyncBound = 0.0;
        }

        boolean same = false;
        if (clockSyncBound == 0.0) {
            same = _timestamp.compareTo(event.timeStamp()) == 0
                    && _microstep == event.microstep();
        } else {
            same = _timestamp.subtract(clockSyncBound).compareTo(
                    event.timeStamp()) <= 0
                    && _timestamp.add(clockSyncBound).compareTo(
                            event.timeStamp()) >= 0;
            // The microstep in Ptides describes a logical ordering. Therefore,
            // even if the timestamps are not equal, we require the microsteps
            // to be the same.
            same = same & _microstep == event.microstep();
        }

        return same;
    }

    /** Return true if this event is a pure event.
     *  A pure event is one that does not contained a token that is destined
     *  to an input port.
     *  @return True if this event is a pure event.
     */
    public final boolean isPureEvent() {
        return _isPureEvent;
    }

    /** Return the destination receiver for this event.
     *  @return The destination receiver for this event.
     */
    public final Receiver receiver() {
        if (!isPureEvent()) {
            assert _receiver != null;
        }
        return _receiver;
    }

    /** Timestamp of the event on the source platform.
     *  @return The timestamp.
     */
    public final Time sourceTimestamp() {
        return _sourceTimestamp;
    }

    /** Return the token (value) of this event.
     *  @return The token.
     *  @exception InternalErrorException If event is not a pure event
     *  and token field is null.
     */
    public final Token token() {
        if (!isPureEvent() && _token == null) {
            throw new InternalErrorException("A non-pure event should "
                    + "not have a token field that is null");
        }
        return _token;
    }

    /** Return a description of the event, including the the tag, depth,
     *  the token, absolute deadline, and destination information.
     *  @return The token as a string with the time stamp.
     */
    @Override
    public String toString() {
        // FIXME: Ideally, this would be in a format that could be easily parsed
        // by the expression language, such as a record format.
        String name = "null";
        if (_actor != null) {
            name = ((NamedObj) _actor).getFullName();
        }
        return "PtidesEvent{time = "
                + _timestamp
                + ", microstep = "
                + _microstep
                + ", depth = "
                + _depth
                + ", token = "
                + _token
                + ", absoluteDeadline = "
                + (_absoluteDeadline == null ? "null" : _absoluteDeadline
                        .toString())
                + ", dest = "
                + name
                + "."
                + (_ioPort == null ? "null" : _ioPort.getName())
                + "."
                + _channel
                + ", receiver = "
                + (_receiver == null ? "null" : getClass().getName()
                        + " {"
                        + (_receiver.getContainer() != null ? _receiver
                                .getContainer().getFullName() : "")
                        + ".receiver }") + ", isPureEvent = " + _isPureEvent
                + ", sourceTimestamp = " + _sourceTimestamp + "}";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The destination channel for this event. */
    private int _channel;

    /** Indicates whether this event is a pure event. */
    private boolean _isPureEvent;

    /** The destination receiver for the token variable of this event. */
    private Receiver _receiver;

    /** The absolute deadline of this event. This field is used only when this
     *  event is a pure event.
     */
    private Time _absoluteDeadline;

    /** The token associated with this event. */
    private Token _token;

    /** The timestamp this or the source for this event was created by a sensor. */
    private Time _sourceTimestamp;
}
