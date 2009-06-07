/* A DE event that saves the token as well as the timestamp.

 Copyright (c) 1998-2008 The Regents of the University of California.
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
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DETokenEvent

/** A DE event that saves the token, as well as the receiver this token is destined
 *  at, in addition to the information that is stored in the super class, such as
 *  the timestamp, etc. This class is used
 *  in the PTIDES domain, because events may arrive out of timestamp order.
 * 
 *  @author Lukito Muliadi, Edward A. Lee, Haiyang Zheng, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 0.2
 *  @Pt.ProposedRating Green (hyzheng)
 *  @Pt.AcceptedRating Green (hyzheng)
 */
public class DETokenEvent extends DEEvent {
    /** Construct a pure event with the specified destination actor,
     *  timestamp, microstep, and depth.
     *  @param actor The destination actor
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination actor.
     *  @param token The token associated with the event.
     *  @exception IllegalActionException If the actor has a priority parameter,
     *  but its value cannot be obtained, which should be an integer.
     */
    public DETokenEvent(Actor actor, Time timeStamp, int microstep, int depth, 
            Token token, Receiver receiver)
            throws IllegalActionException {
        super(actor, timeStamp, microstep, depth);
        _token = token; 
        _receiver = receiver;
    }
    
    /** Construct a trigger event with the specified destination IO port,
     *  timestamp, microstep, and depth.
     *  @param ioPort The destination IO port.
     *  @param timeStamp The time when the event occurs.
     *  @param microstep The phase of execution within a fixed time.
     *  @param depth The topological depth of the destination IO Port.
     *  @param token The token associated with the event.
     *  @exception IllegalActionException If the actor has a priority parameter,
     *  but its value cannot be obtained, which should be an integer.
     */
    public DETokenEvent(IOPort ioPort, int channel, Time timeStamp, int microstep, int depth, 
            Token token, Receiver receiver)
            throws IllegalActionException {
        super(ioPort, timeStamp, microstep, depth);
        _channel = channel;
        _token = token;
        _receiver = receiver;
    }
    
    /** Return the channel
     *  @return The channel.
     */
    public final int channel() {
        return _channel;
    }
    
    /** Indicate whether some the super class returns they are equal
     *  and their tokens are equal.
     *  @param object The object with which to compare.
     *  @return true if the object is a DEEvent and the fields of
     *  the object and of this object are equal.
     *  @see #hashCode()
     */
    public boolean equals(Object object) {
        boolean result = super.equals(object);
        if (result == true && ((DETokenEvent)object).token() == _token) {
            return true;
        } else {
            return false;
        }
    }
    
    /** Return the receiver.
     *  @return The receiver.
     */
    public final Receiver receiver() {
        return _receiver;
    }
    
    /** Return the token.
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
        String name = "null";
        if (_actor != null) {
            name = ((NamedObj) _actor).getFullName();
        }
        if (_ioPort != null) {
            return "DEEvent(time = " + _timestamp + ", microstep = "
                    + _microstep + ", depth = " + _depth + ", token = "
                    + _token + ", dest = " + name + "."
                    + _ioPort.getName() + "." + _channel + ").";
        } else {
            return "DEEvent(time = " + _timestamp + ", microstep = "
                    + _microstep + ", depth = " + _depth + ", token = "
                    + _token + ", dest = " + name + ")"
                    + " -- A PURE EVENT.";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** The channel this event is destined to */
    private int _channel;
    
    /** The token associated with this event. */
    private Token _token;
    
    /** The receiver this token is destined at. */
    private Receiver _receiver;
}
