/* A Token holder with a capacity of one token.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// FSMReceiver

/**
 A receiver with capacity one for which one can explicitly set the status.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class FSMReceiver extends AbstractReceiver {

    /** Construct an empty receiver with no container.
     *  The initial status is unknown.
     */
    public FSMReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     *  The initial status is unknown.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public FSMReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained token and set the known
     *  status to true.
     */
    @Override
    public void clear() {
        _token = null;
        _isKnown = true;
    }

    /** Return a list with the token currently in the receiver, or
     *  an empty list if there is no such token.
     *  @return A list of instances of Token.
     *  @exception IllegalActionException If the status is unknown.
     */
    @Override
    public List<Token> elementList() throws IllegalActionException {
        if (!_isKnown) {
            throw new IllegalActionException(getContainer(),
                    "Receiver status is unknown.");
        }
        List<Token> result = new LinkedList<Token>();
        if (_token != null) {
            result.add(_token);
        }
        return result;
    }

    /** Get the contained Token.  If there is none, throw an exception.
     *  The token is not removed. It can be repeatedly read.
     *  @return The token contained by this receiver.
     *  @exception NoTokenException If this receiver is empty or unknown
     */
    @Override
    public Token get() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty recever.");
        }
        if (!_isKnown) {
            throw new NoTokenException(getContainer(),
                    "Receiver status is unknown.");
        }
        return _token;
    }

    /** If the argument is 1, there is a token, and the status is known,
     *  then return an array containing the one token. Otherwise, throw
     *  an exception.
     *  @exception NoTokenException If the status is unknown, if there is
     *   no token, or if the argument is not 1.
     */
    @Override
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
        if (!_isKnown) {
            throw new NoTokenException(getContainer(),
                    "Receiver status is unknown.");
        }
        if (numberOfTokens <= 0) {
            throw new IllegalArgumentException(
                    "Illegal argument to getArray():" + numberOfTokens);
        }
        if (numberOfTokens > 1) {
            throw new NoTokenException(getContainer(),
                    "Receiver can only contain one token, but request is for "
                            + numberOfTokens);
        }
        if (_token == null) {
            throw new NoTokenException(getContainer(), "Receiver is empty.");
        }
        // Check whether we need allocate the cached token array.
        if (_tokenCache == null) {
            _tokenCache = new Token[1];
        }
        _tokenCache[0] = _token;
        return _tokenCache;
    }

    /** Return true.
     *  @return True.
     */
    @Override
    public boolean hasRoom() {
        return true;
    }

    /** Return true if the argument is 1, and otherwise return false.
     *  @param numberOfTokens The number of tokens to put into the receiver.
     *  @return True if the argument is 1, and otherwise return false.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    @Override
    public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasRoom() requires a positive argument.");
        }
        if (numberOfTokens > 1) {
            return false;
        }
        return true;
    }

    /** Return true if this mailbox is not empty.
     *  @return True if this mailbox is not empty.
     *  @exception InternalErrorException If the status is not known.
     */
    @Override
    public boolean hasToken() {
        if (!_isKnown) {
            throw new InternalErrorException(getContainer(), null,
                    "Receiver status is not known.");
        }
        return _token != null;
    }

    /** Return true if the argument is 1 and this receiver is not empty,
     *  and otherwise return false.
     *  @param numberOfTokens The number of tokens to get from the receiver.
     *  @return True if the argument is 1 and this receiver is not empty.
     *  @exception InternalErrorException If the status is not known.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    @Override
    public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {
        if (!_isKnown) {
            throw new InternalErrorException(getContainer(), null,
                    "Receiver status is not known.");
        }
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasToken() requires a positive argument.");
        }

        if (numberOfTokens == 1) {
            return _token != null;
        }

        return false;
    }

    /** Return whether the state of the receiver is known.
     *  @return True if the state of the receiver is known.
     *  @see #clear()
     *  @see #put(Token)
     */
    @Override
    public boolean isKnown() {
        return _isKnown;
    }

    /** Put a token into this receiver.  If the argument is null, then the
     *  receiver will not contain a token after this returns, getting the
     *  same effect as calling clear(). If there was previously a token
     *  in the receiver, this overwrites that token.
     *  Set the known status of the receiver to true.
     *  @param token The token to be put into the mailbox.
     *  @exception NoRoomException If this mailbox is not empty.
     */
    @Override
    public void put(Token token) throws NoRoomException {
        _token = token;
        _isKnown = true;
    }

    /** If the argument has one token, then put that token in
     *  the receiver. Otherwise, throw an exception.
     */
    @Override
    public void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException, IllegalActionException {
        if (numberOfTokens != 1 || tokenArray.length < 1) {
            throw new IllegalActionException(getContainer(),
                    "Receiver cannot accept more than one token.");
        }
        put(tokenArray[0]);
    }

    /** Set the receiver to unknown. */
    @Override
    public void reset() throws IllegalActionException {
        _isKnown = false;
        _token = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag indicating whether the state of the receiver is known. */
    private boolean _isKnown = false;

    /** The token held. */
    private Token _token = null;

    /** The cache used by the getArray() method to avoid reallocating. */
    private Token[] _tokenCache;
}
