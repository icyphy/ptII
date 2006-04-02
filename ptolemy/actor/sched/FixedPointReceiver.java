/* The receiver for use with FixedPointDirector or any of its subclasses.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.actor.sched;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// FixedPointReceiver

/**
 The receiver for use with FixedPointDirector or any of its subclasses.
 This receiver has capacity 1. 
 The status of this receiver can be either <i>known</i> or <i>unknown</i>.
 If it is known, then it can be either <i>present</i> or <i>absent</i>.
 If it is present, then it has a token, which provides a value.
 <p>
 At first, an instance of this class has status unknown.
 The clear() method makes the status known and absent.
 The put() method makes the status known and present, and provides a value.
 The reset() method reverts the status to unknown.
 Once the status of a receiver becomes known, the value
 cannot be changed, nor can the status be changed from present to absent
 or vice versa. To change the value or the status, call reset() first.
 Normally, the reset() method is called only by the director.
 <p>
 The isKnown() method returns true if the receiver has status known. 
 The hasRoom() method returns true if the receiver has status unknown. 
 If the receiver has a known status, the hasToken() method returns true 
 if the receiver contains a token. If the receiver has an unknown status, 
 the hasToken() method will throw an InvalidStateException.
 <p>
 This class is based on the original SRReceiver, written by Paul Whitaker.
 
 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (reviewModerator)
 */
public class FixedPointReceiver extends AbstractReceiver {

    /** Construct an FixedPointReceiver with unknown status.
     *  @param director The director of this receiver.
     */
    public FixedPointReceiver(FixedPointDirector director) {
        super();
        reset();
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the status of this receiver to be known and absent.
     *  @exception IllegalActionException If this receiver is known and 
     *   present. 
     */
    public void clear() throws IllegalActionException {
        if (isKnown()) {
            if (hasToken()) {
                throw new IllegalActionException(getContainer(),
                        "Cannot change the status from present" + " to absent.");
            }
        } else {
            _token = null;
            _known = true;
            _director._receiverChanged();
        }
    }

    /** Return the contained token.  If there is no token or the status 
     *  of this receiver is unknown, throw an exception.
     *  @return The token contained in the receiver.
     *  @exception NoTokenException If there is no token.
     *  @exception InvalidStateException If the status is unknown.
     */
    public Token get() throws NoTokenException {
        if (!isKnown()) {
            throw new InvalidStateException(
                    "FixedPointReceiver: get() called on an "
                            + "FixedPointReceiver " + "with status unknown.");
        }
        if (_token == null) {
            throw new NoTokenException(
                    "FixedPointReceiver: Attempt to get data from an "
                            + "empty receiver.");
        }
        return _token;
    }

    /** Return true if the status of the receiver is unknown. 
     *  @return True if the status of the receiver is unknown. 
     *  @see #isKnown()
     */
    public boolean hasRoom() {
        return !isKnown();
    }

    /** If the argument is 1, return true if the status of the receiver
     *  is unknown. Otherwise, throw an exception. This receiver has
     *  capacity one.
     *  @param numberOfTokens The number of tokens to put into the receiver.
     *  @return True if the receiver can accept a token.
     *  @exception IllegalArgumentException If the argument is not positive.
     *  @see #isKnown()
     *  @see #hasRoom()
     */
    public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "FixedPointReceiver: hasRoom() requires a "
                            + "positive argument.");
        }
        if (numberOfTokens == 1) {
            return !isKnown();
        }
        return false;
    }

    /** Return true if the receiver contains a token, and false otherwise.
     *  If the receiver has status unknown, this method will throw an
     *  exception.
     *  @return True if this receiver contains a token.
     *  @exception InvalidStateException If the status is unknown.
     */
    public boolean hasToken() {
        if (isKnown()) {
            return (_token != null);
        } else {
            throw new InvalidStateException(getContainer(),
                    "hasToken() called on FixedPointReceiver with "
                            + "unknown status.");
        }
    }

    /** If the argument is 1, return true if the receiver
     *  contains a token, and false otherwise. If the argument is
     *  larger than 1, return false (this receiver has capacity one).
     *  If the receiver has status unknown, throw an exception.
     *  @param numberOfTokens The number of tokens.
     *  @return True if the argument is 1 and the receiver has a token.
     *  @exception IllegalArgumentException If the argument is not positive.
     *  @see #hasToken()
     *  @exception InvalidStateException If the status is unknown.
     */
    public boolean hasToken(int numberOfTokens) {
        if (!isKnown()) {
            throw new InvalidStateException(getContainer(), "hasToken(int)"
                    + " called on FixedPointReceiver with unknown status.");
        }
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "FixedPointReceiver: hasToken(int) requires a "
                            + "positive argument.");
        }
        if (numberOfTokens == 1) {
            return hasToken();
        }
        return false;
    }

    /** Return true if this receiver has status known, that is, this 
     *  receiver either is either known to have a token or known to 
     *  not to have a token.
     *  @return True if this receiver has status known.
     */
    public boolean isKnown() {
        return _known;
    }

    /** Set the status of this receiver to known and present, and to contain the
     *  specified token.  If the receiver is already known and the value of
     *  the contained token is different from that of the new token, throw 
     *  an exception.
     *  @param token The token to be put into this receiver.
     *  @exception IllegalArgumentException If the argument is null.
     *  @exception IllegalActionException If the status is known and absent,
     *   or a token is present but not have the same value, or a token
     *   is present and cannot be compared to the specified token.
     */
    public void put(Token token) throws IllegalActionException {
        if (token == null) {
            throw new IllegalArgumentException(
                    "FixedPointReceiver.put(null) is invalid. To set the "
                            + "status to absent, use the clear() method.");
        }
        if (!isKnown()) {
            _token = token;
            _known = true;
            _director._receiverChanged();
        } else {
            if (!hasToken()) {
                throw new IllegalActionException(getContainer(),
                        "Cannot change from an absent status "
                                + "to a present status.  Call reset() first.");
            } else {
                if (!token.isEqualTo(_token).booleanValue()) {
                    throw new IllegalActionException(getContainer(),
                            "Cannot put a token with a different value"
                                    + " into a receiver with present status.");
                }
            }
        }
    }

    /** Reset the receiver by deleting any contained tokens and setting
     *  the status of this receiver to unknown.  This is called
     *  by the director, normally in its initialize() and postfire()
     *  methods.
     */
    public void reset() {
        _token = null;
        _known = false;
        _lastKnown = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if this receiver changes from unknown to known status.
     *  @return True if this receiver changes from unknown to known status.
     */
    protected boolean _becomesKnown() {
        if (_lastKnown != _known) {
            _lastKnown = _known;
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // The director of this receiver.
    private FixedPointDirector _director;

    // A flag indicating whether this receiver has status known.  
    private boolean _known = false;

    // A flag indicating whether the receiver has status known already.
    private boolean _lastKnown = false;

    // The token held.
    private Token _token = null;
}
