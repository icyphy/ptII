/* The receiver for use with FixedPointDirector or any of its subclasses.

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
package ptolemy.actor.sched;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

///////////////////////////////////////////////////////////////////
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
 Normally, the reset() method is called only by the director and constructors.
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
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Yellow (eal)
 */
public class FixedPointReceiver extends AbstractReceiver {

    /** Construct an FixedPointReceiver with unknown status.
     *  This constructor does not need a director.
     */
    public FixedPointReceiver() {
        this(null);
    }

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
    @Override
    public void clear() throws IllegalActionException {
        if (isKnown()) {
            if (hasToken()) {
                throw new IllegalActionException(getContainer(),
                        "Cannot change the status from present" + " to absent.");
            }
        } else {
            _token = null;
            _known = true;
            if (_director != null) {
                _director._receiverChanged();
            }
        }
    }

    /** Return a list with the token currently in the receiver, or
     *  an empty list if there is no such token.
     *  @return A list of instances of Token.
     */
    @Override
    public List<Token> elementList() {
        List<Token> result = new LinkedList<Token>();
        if (_token != null) {
            result.add(_token);
        }
        return result;
    }

    /** Return the contained token.  If there is no token or the status
     *  of this receiver is unknown, throw an exception.
     *  @return The token contained in the receiver.
     *  @exception NoTokenException If there is no token.
     *  @exception InvalidStateException If the status is unknown.
     */
    @Override
    public Token get() throws NoTokenException {
        if (!isKnown()) {
            throw new InvalidStateException(
                    "FixedPointReceiver: get() called on an "
                            + "FixedPointReceiver " + "with status unknown.");
        }
        if (_token == null) {
            throw new NoTokenException(_director,
                    "FixedPointReceiver: Attempt to get data from an "
                            + "empty receiver.");
        }
        return _token;
    }

    /** Return true if the status of the receiver is unknown.
     *  @return True if the status of the receiver is unknown.
     *  @see #isKnown()
     */
    @Override
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
    @Override
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
    @Override
    public boolean hasToken() {
        if (isKnown()) {
            return _token != null;
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
    @Override
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
    @Override
    public boolean isKnown() {
        IOPort container = getContainer();
        if (container != null && container.sourcePortList().size() == 0
                        && container.insideSourcePortList().size() == 0) {
                // There are no sources connected to the container port,
                // so the port is presumably empty and known.
                return true;
        }
        return _known;
    }

    /** If the specified token is non-null, then
     *  set the status of this receiver to known and present, and to contain the
     *  specified token. If the receiver is already known and the value of
     *  the contained token is different from that of the new token, throw
     *  an exception. If the specified token is null, then set the status to
     *  be known and absent (by calling {@link #clear()}).
     *  @param token The token to be put into this receiver.
     *  @exception IllegalArgumentException If the argument is null.
     *  @exception IllegalActionException If the status is known and absent,
     *   or a token is present but not have the same value, or a token
     *   is present and cannot be compared to the specified token.
     */
    @Override
    public void put(Token token) throws IllegalActionException {
        if (token == null) {
            clear();
            return;
        }
        if (!isKnown()) {
            _token = token;
            _known = true;
            if (_director != null) {
                _director._receiverChanged();
            }
        } else {
            if (!hasToken()) {
                throw new IllegalActionException(getContainer(),
                        "Cannot change from an absent status "
                                + "to a present status.  Call reset() first.");
            } else {
                if (!token.isEqualTo(_token).booleanValue()) {
                    throw new IllegalActionException(
                            getContainer(),
                            "Cannot put a token with a different value "
                                    + token
                                    + " into a receiver with an already established value "
                                    + _token);
                }
            }
        }
    }

    /** Reset the receiver by deleting any contained tokens and setting
     *  the status of this receiver to unknown, unless the containing port
     *  has no sources, in which case set to known and absent.  This is called
     *  by the , normally in its initialize() and postfire()
     *  methods.
     */
    @Override
    public void reset() {
        _token = null;
        _known = false;
    }

    /** Set the container. This overrides the base class so that
     *  if the container is being set to null, and if the director of
     *  this receiver is not null, this method removes the receiver
     *  from the list in that director.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort. Not thrown in this base class,
     *   but may be thrown in derived classes.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(IOPort port) throws IllegalActionException {
        if (port == null && _director != null) {
            _director._receivers.remove(this);
        }
        super.setContainer(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** The director of this receiver. */
    protected FixedPointDirector _director;

    /** A flag indicating whether this receiver has status known. */
    protected boolean _known = false;

    /** The token held. */
    protected Token _token = null;
}
