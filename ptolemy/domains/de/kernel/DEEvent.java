/* A DE event in the Ptolemy II DE domain.

Copyright (c) 1998-2004 The Regents of the University of California.
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
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DEEvent

/**
   This class defines the structure of events in Ptolemy II DE domain.
   Conceptually, a DE event contains a token and a tag.  
   <p>
   A tag consists of a timestamp and a microstep. The timestamp is the model
   time when the event exists. The microstep represents the index of a sequence
   of simultaneous events at a model time. 
   <p>
   A DE event is associated with a destination actor, ioPort, and receiver. A 
   DE event can be a pure event, which is used to schedule an actor to fire at 
   a particular tag. A pure DE event has its desitination ioPort and receiver 
   as null. A DE event can also be a trigger event, which is associated with an
   IO port, where none of the desitinations should be null.  
   <p>
   A DE event also has a depth, which is the topology information of its 
   destinations. For a pure event, the depth is that of the destination actor.
   For a trigger event, the depth is that of the destination io port. A larger 
   value of depth represents a lower priority when processing events with 
   the same tag. 
   <p>
   Two DE events can be compared to see which one happens first. The order 
   is defined by the relationship between their time stamps, microsteps, and 
   depths. {@link DEEventQueue}. DE events can be compared by using the 
   compareTo method.
   <p>
   @author Lukito Muliadi, Edward A. Lee, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public final class DEEvent implements Comparable {

    // FIXME: a DE event does not need the _token field nor the _receiver field.
    
    /** Construct a data DE event with the specified destination receiver,
     *  token, time stamp, microstep, and depth. 
     *  @param receiver The destination receiver.
     *  @param token The transferred token.
     *  @param timeStamp The current model time when the event occurs.
     *  @param microstep The index of this event in a sequence of event at the 
     *  current time stamp.
     *  @param depth The topological depth of this event.
     *  @exception NullPointerException If the receiver is null or is
     *   not contained by a port contained by an actor.
     */
    public DEEvent(Receiver receiver, Token token, Time timeStamp,
            int microstep, int depth) {
        if (receiver!= null) {
            // Infer the io port and actor from the given receiver.
            _ioPort = (IOPort) receiver.getContainer();
            _actor = (Actor) _ioPort.getContainer();
        } else {
            // If the receiver is null, it may be a Pure Event.
            // In which case, the other constructor should be used.
            throw new NullPointerException("Can not construct a data or token " 
                    + "event with a null receiver.");
        }
        _receiver = receiver;
        _token = token;
        _timeStamp = timeStamp;
        _microstep = microstep;
        _depth = depth;
    }

    /** Construct a pure event with the specified destination actor, time
     *  stamp, microstep, and depth.
     *  @param actor The destination actor
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination actor.
     */
    public DEEvent(Actor actor, Time timeStamp, int microstep, int depth) {
        _actor = actor;
        _ioPort = null;
        _receiver = null;
        _token = null;
        _timeStamp = timeStamp;
        _microstep = microstep;
        _depth = depth;
    }
    
    /** Construct a trigger event with the specified destination IO port, time
     *  stamp, microstep, and depth.
     *  @param ioPort The destination IO port.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination IO Port.
     */
    public DEEvent(IOPort ioPort, Time timeStamp, int microstep, int depth) {
        _actor = (Actor)ioPort.getContainer();
        _ioPort = ioPort;
        _receiver = null;
        _token = null;
        _timeStamp = timeStamp;
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
     *  See compareTo(DEEvent event) for the comparison rules.
     *  The argument event has to be an instance of DEEvent. Otherwise,a
     *  ClassCastException will be thrown.
     *
     *  @param event The event to compare against.
     *  @return -1, 0, or 1, depends on the order of the events.
     *  @exception ClassCastException If the argument event is not an instance
     *  of DEEvent.
     */
    public final int compareTo(Object event) {
        return compareTo((DEEvent)event);
    }

    /** Compare the tag and depth of this event with those of the argument 
     *  event for an order. Return -1, 0, or 1 if this event will be handled 
     *  earlier than, the same time as, or later than the argument event.
     *  
     *  Their time stamps are compared first. If the two time stamps are not 
     *  the same, their order defines the events' order. Otherwise, the 
     *  microsteps of events are compared for an order, where the smaller 
     *  microstep, the earlier the event. If the events have the same microstep,
     *  their depths are compared. The smaller depth, the earlier the event. 
     *  If the two events have the same tags and depths, they will be processed
     *  together. 
     *
     * @param event The event to compare against.
     * @return -1, 0, or 1, depends on the order of the events.
     */
    public final int compareTo(DEEvent event) {

        if (timeStamp().compareTo(event.timeStamp()) > 0 ) {
            return 1;
        } else if ( timeStamp().compareTo(event.timeStamp()) < 0) {
            return -1;
        } else if (microstep() > event.microstep()) {
            return 1;
        } else if ( microstep() < event.microstep()) {
            return -1;
        } else if (depth() > event.depth()) {
            return 1;
        } else if ( depth() < event.depth()) {
            return -1;
        } else {
            return 0;
        }
    }

    /** Return the depth of this event. For a pure event, it is the position of 
     *  the destination actor in the topological sort. For a data event, it is
     *  the position of the destination io port.
     *  @return The depth of this event.
     */
    public final int depth() {
        return _depth;
    }

    /** Return true if this event has the same tag with the argument DE event.  
     *  @param event The DE event to compare against.
     *  @return Ture if this event has the same tag with the specified one.
     */
    public final boolean hasTheSameTagAs(DEEvent event) {
        return (timeStamp().equals(event.timeStamp())) &&
            (microstep() == event.microstep());
    }

    /** Return true if this event has the same tag with the specified one,
     *  and their depths are the same.
     *  @param event The event to compare against.
     *  @return True if this event has the same tag with the specified one,
     *  and their depths are the same.
     */
    public final boolean hasTheSameTagAndDepthAs(DEEvent event) {
        return hasTheSameTagAs(event) 
            && (depth() == event.depth());
    }

    /** Return the destination io port of this event.
     *  For pure events, the destination ioPort is null.
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

    /** Return the destination receiver of this event.
     *  For pure events, the destination receiver is null.
     *  @return The destination receiver
     */
    public final Receiver receiver() {
        return _receiver;
    }

    /** Return the time stamp.
     *  @return The time stamp.
     */
    public final Time timeStamp() {
        return _timeStamp;
    }

    /** Return the token contained by this event. For a pure event, the token
     *  is null.
     *  @return The token in this event.
     */
    public final Token token() {
        return _token;
    }

    /** Return a description of the event, including the the tag, depth,
     *  and destination information.
     *  @return The token as a string with the time stamp.
     */
    public String toString() {
        if (_ioPort != null) {
            return "DEEvent(token= " + _token + ", time= " + _timeStamp 
                + ", dest= " + ((NamedObj)_actor).getFullName() + "." 
                + _ioPort.getName() + ").";
        } else {
            return "DEEvent(token= " + _token + ", time= " + _timeStamp 
                + ", dest= " + ((NamedObj)_actor).getFullName() + ")" 
                + " -- A PURE EVENT.";
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

    // The destination io port.
    private IOPort _ioPort;

    // The microstep of this event.
    private int _microstep;

    // The destination receiver.
    private Receiver _receiver;

    // The depth of this event.
    private int _depth;

    // The time stamp of the event.
    private Time _timeStamp;

    // The token contained by this event.
    private Token _token;
}
