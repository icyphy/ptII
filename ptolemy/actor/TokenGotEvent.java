/* An event that represents a token or tokens are taken by the actor.
   (read event)

 Copyright (c) 2007-2014 The Regents of the University of California.
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
//// TokenGotEvent

/**
 An event that is published by IOPorts whenever get or
 getInside is called.  The appropriate event should be published
 whenever a token is taken out from the port's queue by the actor code.
 In Kepler the provenance recorder uses these events to save
 intermediate results of the workflow.  In the future these events
 could be used for "smart" reruns or a fault tolerance mechanism.

 @author  Norbert Podhorszki
 @version $Id$
 @since Ptolemy II 6.1
 @deprecated Use IOPortEvent instead.
 @Pt.ProposedRating Red (pnorbert)
 @Pt.AcceptedRating Red (pnorbert)
 */
@Deprecated
public class TokenGotEvent {
    /** Create a new token got event with the given parameters.  This
     *  constructor is used when an array of tokens is taken.
     *  @param sink The IOPort the token is stored at.
     *  @param channel Channel the token was received on.
     *  @param tokens The token array used for the get.
     *  @param vectorLength The number of tokens taken.
     *  @param outside True if the token was taken on outside channel,
     *  false otherwise.
     */
    public TokenGotEvent(IOPort sink, int channel, Token[] tokens,
            int vectorLength, boolean outside) {
        _port = sink;
        _channel = channel;
        _tokenArray = tokens;
        _token = null;
        _vectorLength = vectorLength;
        _outside = outside;
    }

    /** Create a new token got event with the given parameters.  This
     *  constructor is used when a token is taken using get or getInside.
     *  @param sink The IOPort the token is stored at.
     *  @param channel Channel the token was received on.
     *  @param token The token that was received.
     *  @param outside True if the token was taken on outside channel,
     *  false otherwise.
     */

    public TokenGotEvent(IOPort sink, int channel, Token token, boolean outside) {
        _port = sink;
        _channel = channel;
        _tokenArray = null;
        _token = token;
        _vectorLength = -1;
        _outside = outside;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the channel the token was received on.
     *  @return The channel number.
     */
    public int getChannel() {
        return _channel;
    }

    /** Return the port that the token was received at.
     *  @return An instance of IOPort.
     */
    public IOPort getPort() {
        return _port;
    }

    /** Return the token that was received by the IOPort.  The variable
     *  _token will be null if the event was for a get array. To get
     *  the tokens that were received use getTokenArray() instead.
     *  @return The token received at the IOPort.
     */
    public Token getToken() {
        return _token;
    }

    /** Return the array of tokens that was received by the
     *  IOPort. The variable _tokenArray will be null if an individual
     *  token was got instead of an array.
     *  @return The array of tokens which were taken.
     */
    public Token[] getTokenArray() {
        //may want to return the array shortened to be vector length long?
        return _tokenArray;
    }

    /** Return the number of tokens in the array taken at the IOPort.
     *  The variable _vectorLength will be -1 if an individual token
     *  was taken instead of an array of tokens.
     *  @return The number of tokens taken at the port.
     */
    public int getVectorLength() {
        return _vectorLength;
    }

    /** Return the direction flag (outside or inside).  The flag is
     *  true, if the token was taken from outside, false if from
     *  inside
     *  @return The direction flag.
     */
    public boolean getOutsideFlag() {
        return _outside;
    }

    /** Return a string representation of this event.
     *  @return A user-readable string describing the event.
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("The port " + _port + " took ");
        if (_vectorLength != -1) {
            buffer.append(_vectorLength);
            buffer.append(" tokens ");
        } else {
            buffer.append(_token.toString());
        }
        buffer.append(" on channel " + _channel + ".");

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

    // Boolean flag if the token was taken on outside link (true) or
    // inside (false)
    private boolean _outside;
}
