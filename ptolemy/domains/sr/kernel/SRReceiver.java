/* The receiver for the SR domain.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)

*/

package ptolemy.domains.sr.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// SRReceiver
/**

FIXME: update.

FIXME: correct types of exceptions?

The receiver for the SR domain. This receiver is a mailbox with capacity one,
and any token put in the receiver overwrites any token previously present in
the receiver. As a consequence, hasRoom() method always returns true. The
get() method will consume the token if one exists. After the
consumption, the hasToken() method will return false, until a token is put
into this receiver.

@author Paul Whitaker
@version $Id$
*/
public class SRReceiver extends Mailbox {

    /** Construct an empty SRReceiver with no container.
     */
    public SRReceiver() {
        super();
        reset();
    }

    /** Construct an empty SRReceiver with the specified container.
     *  @param container The port that contains the receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public SRReceiver(IOPort container) throws IllegalActionException {
        super(container);
        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the contained Token.  If there is none, throw an exception.
     *  The token is removed.
     *  @return The token contained in the receiver.
     *  @exception NoTokenException If the state of this receiver
     *   is unknown.
     */
    public Token get() throws NoTokenException {
        if (isKnown()) {
            return _getToken();
        } else {
            throw new NoTokenException(getContainer(),
                    "get() called on SRReceiver with unknown state.");
        }
    }

    /** Return true, since the new token will override the old one.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true if the receiver contains a token, or false otherwise.
     *  FIXME: If the receiver has unknown status, what should this method
     *  return?  super.hasToken() does not throw any compile-time exceptions.
     *  @return True if this receiver contains a token.
     */
    public boolean hasToken() {
        if (isKnown()) {
            return super.hasToken();
        } else {
            if (super.hasToken()) {
                throw new InvalidStateException("SRReceiver with unknown " +
                        "state cannot contain a token.");
            } else {
                return super.hasToken();
                //throw new IllegalActionException("hasToken() called on " +
                //"SRReceiver with unknown state.");
            }
        }
    }

    /** Return true if the token in this receiver is known or if this 
     *  receiver is known not to contain a token.
     *  @return True if this receiver has known state.
     */
    public boolean isKnown() {
        return _known;
    }

    /** Put a token into this receiver. If the argument is null,
     *  then this receiver will not contain any token after this method
     *  returns (if no exception was thrown). If the receiver already 
     *  has a token, it will not change.
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException If the argument is null and this receiver
     *   already contains a token, or if the argument is a token and this 
     *   receiver already contains a different token, or if the argument is
     *   a token and this receiver is in an absent state.
     */
    public void put(Token token) throws NoRoomException {
        if (token == null) {
            _setAbsent();
        } else {
            _setPresent(token);
        }
    }

    /** Reset the receiver by removing any contained token and setting
     *  the state of this receiver to be unknown.
     */
    public void reset() {
        if (hasToken()) super.get();
        _known = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the contained token without modifying or removing it.
     *  @return The token contained in this receiver.
     *  @exception NoTokenException If this receiver contains no token.
     */
    private Token _getToken() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty mailbox.");
        }
        return _token;
    }

    /** Discard any contained token, and replace it with the specified
     *  token or null for no token.
     *  @param token The token to be put into this receiver.
     */
    private void _putToken(Token token) {
        reset();
        super.put(token);
        _known = true;
    }

    /** Set the state of this receiver to be known and to contain no token.
     *  @exception NoRoomException If this receiver already contains 
     *   a token.
     */
    private void _setAbsent() throws NoRoomException {
        if (isKnown()) {
            if (hasToken()) {
                throw new NoRoomException(getContainer(),
                        "SRReceiver cannot transition from a present " +
                        "state to an absent state.");
            } else {
                // Do nothing, because already in an absent state.
            }
        } else {
            _putToken(null);
        }
    }

    /** Set the state of this receiver to be known and to contain the
     *  specified token.
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException If this receiver already contains 
     *   a different token, or if this receiver is in an absent state.
     */
    private void _setPresent(Token token) throws NoRoomException {
        if (isKnown()) {
            if (hasToken()) {
                try {
                    if ( (token.getType() == _token.getType()) &&
                            (token.isEqualTo(_token).booleanValue()) ) {
                        // Do nothing, because this token was already present.
                    } else {
                        throw new NoRoomException(getContainer(), 
                                "SRReceiver cannot receive two tokens " +
                                "that differ.");
                    }
                } catch(IllegalActionException ex) {
                    // Should never happen.
                    throw new InternalErrorException("SRReceiver cannot " +
                            "determine whether the two tokens received are " +
                            "equal.");
                }
            } else {
                throw new NoRoomException(getContainer(), 
                        "SRReceiver cannot transition from an absent state " +
                        "to a present state.");
            }
        } else {
            _putToken(token);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A flag indicating whether this receiver has known state.  A receiver
    // has known state if the token in the receiver is known or if the
    // receiver is known not to contain a token.
    private boolean _known;

}

