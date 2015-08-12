/* An event that represents a token or tokens sent or received by an IOPort.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.data.Token;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PortEvent

/**
 An event that is published by an IOPort when a token or tokens are sent or
 received.

 <p>In Kepler the provenance recorder and the activity monitor use
 these events to save intermediate results / show status of the
 workflow.  In the future these events could be used for "smart"
 reruns or a fault tolerance mechanism.

 @author  Oscar Barney, Norbert Podhorszki
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating Red (barney)
 @Pt.AcceptedRating Red (barney)
 */
public class IOPortEvent implements DebugEvent {
    /** Create a new port event with the given parameters.  This
     *  constructor is used when an array of tokens is sent or received.
     *  @param port The IOPort where the event occurred.
     *  @param event The type of event.
     *  @param channel Channel the token was sent/received on. Use
     *  IOPortEvent.ALLCHANNELS to indicate a broadcast event.
     *  @param outside True if the event is related the port's outside
     *  activity, false if the event is related to the port's inside
     *  activity.
     *  @param tokens The token array used for the send/receive.
     *  @param vectorLength The number of tokens sent/received.
     */
    public IOPortEvent(IOPort port, int event, int channel, boolean outside,
            Token[] tokens, int vectorLength) {
        _port = port;
        _event = event;
        _channel = channel;
        _outside = outside;
        _tokenArray = tokens;
        _token = null;
        _vectorLength = vectorLength;
        _receiverPort = null;
    }

    /** Create a new port event with the given parameters.  This
     *  constructor is used when a token is sent or sent inside,
     *  received or received inside.
     *  @param port The IOPort where the event occurred
     *  @param event The type of event.
     *  @param channel Channel the token was sent/received on. Use
     *  IOPortEvent.ALLCHANNELS to indicate a broadcast event.
     *  @param outside True if the event is related the port's outside
     *  activity, false if the event is related to the port's inside
     *  activity.
     *  @param token The token that was sent/received.
     */
    public IOPortEvent(IOPort port, int event, int channel, boolean outside,
            Token token) {
        _port = port;
        _event = event;
        _channel = channel;
        _outside = outside;
        _tokenArray = null;
        _token = token;
        _vectorLength = SINGLETOKEN;
        _receiverPort = null;
    }

    /** Create a new port event with the given parameters.  This
     *  constructor is used when a token is directly put in a
     *  receiver instead of transferred with IOPort's send() or
     *  sendInside() methods.
     *  @param port The IOPort where the event occurred
     *  @param receiverPort The IOPort of the receiver.
     *  @param isBegin True if this event is the start.
     *  @param channel Channel the token was transferred on.
     *  @param outside True if the event is related the port's outside
     *  activity, false if the event is related to the port's inside
     *  activity.
     *  @param token The token that was transferred.
     */
    public IOPortEvent(IOPort port, IOPort receiverPort, boolean isBegin,
            int channel, boolean outside, Token token) {
        _port = port;
        if (isBegin) {
            _event = SEND_BEGIN;
        } else {
            _event = SEND_END;
        }
        _receiverPort = receiverPort;
        _channel = channel;
        _outside = outside;
        _tokenArray = null;
        _token = token;
        _vectorLength = SINGLETOKEN;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the port that caused this event.
     * @return The port.
     */
    @Override
    public NamedObj getSource() {
        return _port;
    }

    /** Return the channel on which the token was sent or received.
     *  @return The channel number.
     */
    public int getChannel() {
        return _channel;
    }

    /**
     * Return the receiver's port if this event represents a token put directly
     * into a receiver instead of transferred via IOPort send or sendInside.
     * @return Return the receiver's port or null if token was transferred in IOPort.
     */
    public IOPort getReceiverPort() {
        return _receiverPort;
    }

    /**
     * Return the type of event.
     * @return The int event.
     */
    public int getEventType() {
        return _event;
    }

    /** Return the flag indicating that the event related to the port's outside
     *  activity (true) or to its inside activity.
     *  @return The int event.
     */
    public boolean getOutsideFlag() {
        return _outside;
    }

    /** Return the port where the event occurred.
     *  @return An instance of IOPort.
     */
    public IOPort getPort() {
        return _port;
    }

    /** Return the token that was sent or received by the IOPort.
     *  Return null if the event was for an array of tokens. To get
     *  the tokens that were sent use getTokenArray() instead.  The
     *  condition (getVectorLength == IOPortEvent.SINGLETOKEN) is true
     *  if there was a single token involved.  It returns null if it
     *  was GET_BEGIN event, since that has no related token at all.
     *  @return The token sent/received by the IOPort.
     */
    public Token getToken() {
        return _token;
    }

    /** Return the array of tokens that the IOPort sent or received.
     *  Return null if an individual token was sent or received
     *  instead of an array.
     *  @return The array of tokens which were sent/received.
     */
    public Token[] getTokenArray() {
        //may want to return the array shortened to be vector length long?
        return _tokenArray;
    }

    /** Return the number of tokens in the array sent by the IOPort.
     *  Return IOPortEvent.SINGLETOKEN if an individual token
     *  was sent instead of an array of tokens.
     *  @return The number of tokens sent by the port.
     */
    public int getVectorLength() {
        return _vectorLength;
    }

    /** Return a string representation of this event.
     *  @return A user-readable string describing the event.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("The port " + _port);
        if (_event == SEND_BEGIN) {
            buffer.append(" began sending ");
        } else if (_event == GET_BEGIN) {
            buffer.append(" began reading ");
        } else if (_event == GET_END) {
            buffer.append(" read ");
        } else if (_event == SEND_END) {
            buffer.append(" wrote ");
        }
        if (_vectorLength != SINGLETOKEN) {
            buffer.append(_vectorLength);
            if (_vectorLength == 1) {
                buffer.append(" token");
            } else {
                buffer.append(" tokens");
            }
        } else if (_token == null) {
            buffer.append("a null token");
        } else {
            buffer.append(_token.toString());
        }
        if (_channel != ALLCHANNELS) {
            buffer.append(" on channel " + _channel + ".");
        } else {
            buffer.append(" on all channels");
        }

        return buffer.toString();
    }

    // FIXME change to typesafe enum.

    /** An event corresponding with a token being sent.
     *  @deprecated Use SEND_BEGIN or SEND_END instead.
     */
    @Deprecated
    public final static int SEND = 1;

    /** An event corresponding with the beginning of a token being sent. */
    public final static int SEND_BEGIN = 1;

    /** An event corresponding with the beginning of a token being received. */
    public final static int GET_BEGIN = 2;

    /** An event corresponding with the ending of a token being received. */
    public final static int GET_END = 3;

    /** An event corresponding with the ending of a token being sent. */
    public final static int SEND_END = 4;

    /** The token was broadcast on all channels. */
    public final static int ALLCHANNELS = -1;

    /** A single token related event in getVectorLength(). */
    public final static int SINGLETOKEN = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The channel on which the token was sent. */
    private int _channel;

    /** The receiver's port if this event represents a token put directly
     * into a receiver instead of transferred via IOPort send or sendInside.
     */
    private IOPort _receiverPort;

    /** The IOPort that was activated. */
    private IOPort _port;

    /** The event type. */
    // FIXME: we should use a type safe enumeration here.
    private int _event;

    /** The direction of the event (outside vs inside). */
    private boolean _outside;

    /** The token sent by the IOPort. */
    private Token _token;

    /** The array of tokens sent by the IOPort. */
    private Token[] _tokenArray;

    /** The number of tokens from the array that were sent by the IOPort. */
    private int _vectorLength;
}
