/* The receiver for the SR domain.

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

@ProposedRating Green (pwhitake@eecs.berkeley.edu)
@AcceptedRating Green (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.kernel;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// SRReceiver
/**
The receiver for the Synchronous Reactive (SR) domain.  This receiver is a
mailbox with capacity one.  The status of this receiver can be known (either
known to contain a token or known not to contain a token) or unknown.  The
isKnown() method returns true if the receiver has known status.  If the
receiver has known status, the hasToken() method returns whether the receiver
has a token.  If the receiver has unknown status, the hasToken() method
will throw an UnknownTokenException.
<p>
In the course of an iteration in SR, receivers can change from unknown status
to known status, but never the other way around, as shown by the transitions
in the diagram below.
<p>
<pre>
known values:     absent     value (present)
                     ^         ^
                     |         |
                      \       /
                       \     /
                        |   |
                       unknown
</pre>
<p>
The status is automatically set to known when the put() method or clear()
method is called.  Once a receiver becomes known, its value (or lack of a
value if it is absent) cannot change until the next call to reset().
The SRDirector calls reset() between iterations.
The hasRoom() method returns true if the state of the receiver is
unknown or if it is known but not absent, since only in these circumstances
can it accept a token. Attempting to change the status of a receiver
from present to absent or from absent to present will
result in an exception.  An exception will also be thrown if a receiver has
present status and it receives a token that is not the same as the one it
already contains (as determined by the isEqualTo() method of the token).
Thus, for an actor to be valid in SR, a firing must produce the same outputs
given the same inputs (in a given iteration).
<p>
Since the value of a receiver cannot change (once it is known) in the course
of an iteration, tokens need not be consumed.  A receiver retains its token
until the director calls the reset() method at the beginning of the next
iteration, which resets the receiver to have unknown status.  There is no way
for an actor to reset a receiver to have unknown status.

@author Paul Whitaker, contributor: Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.domains.sr.kernel.SRDirector
*/
public class SRReceiver extends AbstractReceiver {

    /** Construct an SRReceiver with unknown state and the given director.
     */
    public SRReceiver(SRDirector director) {
        super();
        reset();
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the state of this receiver to be known and to contain no token.
     *  Note that during an iteration, SR semantics does not allow a receiver
     *  to receive a token and then be cleared.  Thus, the clear will
     *  trigger an exception.
     *  @exception IllegalActionException If the receiver state is known
     *   and the receiver has a token.
     */
    public void clear() throws IllegalActionException {
        if (isKnown() && hasToken()) {
            throw new IllegalActionException(
                    "SRReceiver: Cannot transition from a present state "
                    + "to an absent state.");
        }
        _token = null;
        _known = true;
    }

    /** Get the contained Token without modifying or removing it.  If there
     *  is none, throw an exception.
     *  @return The token contained in the receiver.
     *  @exception NoTokenException If this mailbox is empty.
     */
    public Token get() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(
                    "SRReceiver: Attempt to get data from an empty receiver.");
        }
        if (!isKnown()) {
            throw new UnknownTokenException(
                    "SRReceiver: get() called on SRReceiver with unknown state.");
        }
        return _token;
    }

    /** Return true if the state of the receiver is unknown or if it is
     *  known and not empty.  This is equivalent to the expression
     *  <pre>
     *    !isKnown() || hasToken()
     *  </pre>
     *  @return True if the receiver can accept a token.
     */
    public boolean hasRoom() {
        return !isKnown() || hasToken();
    }

    /** Return what hasRoom() returns if the argument is 1,
     *  and otherwise return false.
     *  @see #hasRoom()
     *  @param numberOfTokens The number of tokens to put into the mailbox.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasRoom() requires a positive argument.");
        }
        if (numberOfTokens == 1) return hasRoom();
        return false;
    }

    /** Return true if the receiver contains a token, or false otherwise.
     *  If the receiver has unknown status, this method will throw an
     *  exception.
     *  @return True if this receiver contains a token.
     *  @exception UnknownTokenException If the state is unknown.
     */
    public boolean hasToken() {
        if (isKnown()) {
            return (_token != null);
        } else {
            throw new UnknownTokenException(getContainer(),
                    "hasToken() called on SRReceiver with unknown state.");
        }
    }

    /** Return what hasToken() returns if the argument is 1,
     *  and otherwise return false.
     *  If the receiver has unknown status, this method will throw
     *  an UnknownTokenException, which is a RuntimeException so it
     *  need not be declared explicitly.
     *  If the argument is 0 or a negative number, then this method will
     *  throw an IllegalArgumentException, which is a RuntimeException.
     *  @param numberOfTokens The number of tokens to get from the receiver.
     *  @return True if the argument is 1 and the receiver has a token.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     *  @exception UnknownTokenException If the state is unknown.
     *  @exception IllegalArgumentException If the state is unknown.
     *  @see #hasToken()
     *  @since Ptolemy II 2.1
     */
    public boolean hasToken(int numberOfTokens)
            throws IllegalArgumentException {
        if (!isKnown()) {
            throw new UnknownTokenException(getContainer(),
                    "hasToken(" + numberOfTokens
                    + ") called on SRReceiver with unknown state.");
        }
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "SRReceiver: hasToken() requires a positive argument.");
        }
        if (numberOfTokens == 1) {
            return hasToken();
        } else {
            return false;
        }
    }

    /** Return true if this receiver changes from unknown to known.
     *  @return True if this receiver changes from unknown to known state.
     */
    public boolean isChanged() {
        if (_cachedToken == null) {
            if (_token != null) {
                // the token changes from unknown
                // to known with some concrete value.
                _cachedToken = _token;
                _lastKnownStatus = _known;
                return true;
            } else {
                if (_known) {
                    // the token changes from unknown to
                    // absent status.
                    if (!_lastKnownStatus) {
                        _lastKnownStatus = _known;
                        return true;
                    }
                } 
            }
            return false;
        } else {
            // since tokens can not change according to
            // the SR semantics, we simply return false.
            // We do not check the possible violations
            // of the SR semantics here.
            return false;
        }
    }

    /** Return true if this receiver has known state, that is, the token in
     *  this receiver is known or if this receiver is known not to contain a
     *  token.
     *  @return True if this receiver has known state.
     */
    public boolean isKnown() {
        return _known;
    }

    /** Set the state of this receiver to be known and to contain the
     *  specified token.  If the receiver already contains an equal token,
     *  do nothing.
     *  @param token The token to be put into this receiver.
     *  @exception IllegalArgumentException If the argument is null.
     *  @exception IllegalOutputException If the state is known and absent,
     *   or a token is present and does not have the same value.
     */
    public void put(Token token) {
        if (token == null) {
            throw new IllegalArgumentException(
                    "SRReceiver.put(null) is invalid.");
        }
        if (!isKnown()) {
            _putToken(token);
        } else {
            if (!hasToken()) {
                throw new IllegalOutputException(getContainer(),
                        "SRReceiver cannot transition from an absent state " +
                        "to a present state.  Call reset().");
            } else {
                try {
                    if ( (token.getType().equals( _token.getType())) &&
                            (token.isEqualTo(_token).booleanValue()) ) {
                        // Do nothing, because this token was already present.
                    } else {
                        throw new IllegalOutputException(getContainer(),
                                "SRReceiver cannot receive two tokens " +
                                "that differ.");
                    }
                } catch(IllegalActionException ex) {
                    // Should never happen.
                    throw new InternalErrorException("SRReceiver cannot " +
                            "determine whether the two tokens received are " +
                            "equal.");
                }
            }
        }
    }

    /** Reset the receiver by removing any contained token and setting
     *  the state of this receiver to be unknown.  This is called
     *  by the director between iterations.
     */
    public void reset() {
        _token = null;
        _known = false;
        _cachedToken = null;
        _lastKnownStatus = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The token held. */
    protected Token _token = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Discard any contained token, and replace it with the specified
     *  token.
     *  @param token The token to be put into this receiver.
     */
    private void _putToken(Token token) {
        _token = token;
        _known = true;
        _director.receiverChanged(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _lastKnownStatus;
    private Token _cachedToken;

    // A flag indicating whether this receiver has known state.  A receiver
    // has known state if the token in the receiver is known or if the
    // receiver is known not to contain a token.
    private boolean _known;

    // The director of this receiver.
    private SRDirector _director;
}

