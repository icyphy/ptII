/* The receiver for the SR domain.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
The status is automatically set to known when the put() method or setAbsent()
method is called.  Once a receiver becomes known, its value (or lack of a
value if it is absent) cannot change until the next iteration of the
director.  The hasRoom() method always returns true, but attempting to change
the status of a receiver from present to absent or from absent to present will
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

@author Paul Whitaker
@version $Id$
@see ptolemy.domains.sr.kernel.SRDirector
*/
public class SRReceiver extends Mailbox {

    /** Construct an SRReceiver with unknown state and the given director.
     */
    public SRReceiver(SRDirector director) {
        super();
        reset();
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the contained Token without modifying or removing it.  If there
     *  is none, throw an exception.
     *  @return The token contained in the receiver.
     */
    public Token get() {
        if (isKnown()) {
            return _getToken();
        } else {
            throw new UnknownTokenException(getContainer(),
                    "get() called on SRReceiver with unknown state.");
        }
    }

    /** Return true, since a token can always be accepted.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true if the receiver contains a token, or false otherwise.
     *  If the receiver has unknown status, this method will throw an
     *  exception.
     *  @return True if this receiver contains a token.
     */
    public boolean hasToken() {
        if (isKnown()) {
            return super.hasToken();
        } else {
            throw new UnknownTokenException(getContainer(),
                    "hasToken() called on SRReceiver with unknown state.");
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
     */
    public void put(Token token) {
        if (token == null) {
            throw new InternalErrorException("SRReceiver.put(null) is " +
                    "invalid.");
        }
        if (!isKnown()) {
            _putToken(token);
        } else {
            if (!hasToken()) {
                throw new IllegalOutputException(getContainer(),
                        "SRReceiver cannot transition from an absent state " +
                        "to a present state.");
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

    /** Set the state of this receiver to be known and to contain no token.
     */
    public void setAbsent() {
        if (!isKnown()) {
            _putToken(null);
        } else {
            if (hasToken()) {
                throw new IllegalOutputException(getContainer(),
                        "SRReceiver cannot transition from a present " +
                        "state to an absent state.");
            }
            // Otherwise, do nothing, because already in an absent state.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Reset the receiver by removing any contained token and setting
     *  the state of this receiver to be unknown.  Should be called
     *  only by the director.  Note that this method has no access specifier,
     *  so it defaults to package protection.  Thus, the director can invoke
     *  this method, but actors in other packages cannot.
     */
    void reset() {
        if (isKnown()) {
            if (hasToken()) {
                super.get();
            }
            _known = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the contained token without modifying or removing it.
     *  @return The token contained in this receiver.
     */
    private Token _getToken() {
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
        _director.receiverChanged(this);
        _known = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A flag indicating whether this receiver has known state.  A receiver
    // has known state if the token in the receiver is known or if the
    // receiver is known not to contain a token.
    private boolean _known;

    // The director of this receiver.
    private SRDirector _director;
}

