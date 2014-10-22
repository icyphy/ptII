/* An event that represents a token or tokens sent.

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

///////////////////////////////////////////////////////////////////
//// TokenSentEvent

/**
 An event that is published by IOPorts whenever broadcast, send or
 sendInside is called.  The appropriate event should be published
 whenever a token is transferred from one port to another.  In
 Kepler the provenance recorder uses these events to save
 intermediate results of the workflow.  In the future these events
 could be used for "smart" reruns or a fault tolerance mechanism.

 @author  Oscar Barney
 @version $Id$
 @since Ptolemy II 5.2
 @deprecated Use IOPortEvent instead.
 @Pt.ProposedRating Red (barney)
 @Pt.AcceptedRating Red (barney)
 */
@Deprecated
public class TokenSentEvent {
    /** Create a new token sent event with the given parameters.  This
     *  constructor is used when an array of tokens is sent.
     *  @param source The IOPort the token came from.
     *  @param channel Channel the token was sent on.
     *  @param tokens The token array used for the send.
     *  @param vectorLength The number of tokens sent.
     */
    public TokenSentEvent(IOPort source, int channel, Token[] tokens,
            int vectorLength) {
        _port = source;
        _channel = channel;
        _tokenArray = tokens;
        _token = null;
        _vectorLength = vectorLength;
    }

    /** Create a new token sent event with the given parameters.  This
     *  constructor is used when a token is sent or sent inside.
     *  @param source The IOPort the token came from.
     *  @param channel Channel the token was sent on.
     *  @param token The token that was sent.
     */
    public TokenSentEvent(IOPort source, int channel, Token token) {
        _port = source;
        _channel = channel;
        _tokenArray = null;
        _token = token;
        _vectorLength = -1;
    }

    /** Create a new token sent event with the given parameters.  This
     *  constructor is used when a token is broadcast.
     *  @param source The IOPort the token came from.
     *  @param token The token that was sent.
     */
    public TokenSentEvent(IOPort source, Token token) {
        _port = source;
        _channel = -1;
        _tokenArray = null;
        _token = token;
        _vectorLength = -1;
    }

    /** Create a new token sent event with the given parameters.  This
     *  constructor is used when an array of tokens is broadcast.
     *  @param source The IOPort the token came from.
     *  @param tokens The token array used for the broadcast.
     *  @param vectorLength The number of tokens sent.
     */
    public TokenSentEvent(IOPort source, Token[] tokens, int vectorLength) {
        _port = source;
        _channel = -1;
        _tokenArray = tokens;
        _token = null;
        _vectorLength = vectorLength;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the channel the token was sent on.
     *  @return The channel number.
     */
    public int getChannel() {
        return _channel;
    }

    /** Return the port that the token was sent from.
     *  @return An instance of IOPort.
     */
    public IOPort getPort() {
        return _port;
    }

    /** Return the token that was sent by the IOPort.  The variable
     *  _token will be null if the event was for a sent array. To get
     *  the tokens that were sent use getTokenArray() instead.
     *  @return The token sent by the IOPort.
     */
    public Token getToken() {
        return _token;
    }

    /** Return the array of tokens that the IOPort sent. The variable
     *  _tokenArray will be null if an individual token was sent instead
     *  of an array.
     *  @return The array of tokens which were sent.
     */
    public Token[] getTokenArray() {
        //may want to return the array shortened to be vector length long?
        return _tokenArray;
    }

    /** Return the number of tokens in the array sent by the IOPort.
     *  The variable _vectorLength will be -1 if an individual token
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
        StringBuffer buffer = new StringBuffer();
        buffer.append("The port " + _port + " sent ");
        if (_vectorLength != -1) {
            buffer.append(_vectorLength);
            buffer.append(" tokens ");
        } else {
            buffer.append(_token.toString());
        }
        if (_channel != -1) {
            buffer.append(" to channel " + _channel + ".");
        } else {
            buffer.append(" to all channels");
        }

        return buffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //The channel the token was sent on.
    private int _channel;

    // The IOPort that was activated.
    private IOPort _port;

    //The token sent by the IOPort.
    private Token _token;

    //The array of tokens sent by the IOPort.
    private Token[] _tokenArray;

    //The number of tokens from the array that were sent by the IOPort.
    private int _vectorLength;

}
