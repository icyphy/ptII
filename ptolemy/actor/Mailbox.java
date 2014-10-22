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
package ptolemy.actor;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Mailbox

/**
 A token holder with capacity one.

 @author Jie Liu, Edward A. Lee, Lukito Muliadi
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (neuendor)
 */
public class Mailbox extends AbstractReceiver {
    /** Construct an empty Mailbox with no container.
     */
    public Mailbox() {
        super();
    }

    /** Construct an empty Mailbox with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public Mailbox(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained token.
     *  @exception IllegalActionException If a derived class throws it (not
     *   thrown in this base class).
     */
    @Override
    public void clear() throws IllegalActionException {
        _token = null;
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

    /** Get the contained Token.  If there is none, throw an exception.
     *  The token is removed.
     *  @return The token contained by this mailbox.
     *  @exception NoTokenException If this mailbox is empty.
     */
    @Override
    public Token get() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty mailbox.");
        }

        Token token = _token;
        _token = null;
        return token;
    }

    /** Return true if this mailbox is empty.
     *  @return True if this mailbox is empty.
     */
    @Override
    public boolean hasRoom() {
        return _token == null;
    }

    /** Return true if the argument is 1 and the mailbox is empty,
     *  and otherwise return false.
     *  @param numberOfTokens The number of tokens to put into the mailbox.
     *  @return True if the argument is 1 and the mailbox is empty,
     *  and otherwise return false.
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

        if (numberOfTokens == 1) {
            return _token == null;
        }

        return false;
    }

    /** Return true if this mailbox is not empty.
     *  @return True if this mailbox is not empty.
     */
    @Override
    public boolean hasToken() {
        return _token != null;
    }

    /** Return true if the argument is 1 and this mailbox is not empty,
     *  and otherwise return false.
     *  @param numberOfTokens The number of tokens to get from the receiver.
     *  @return True if the argument is 1 and this mailbox is not empty.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    @Override
    public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasToken() requires a positive argument.");
        }

        if (numberOfTokens == 1) {
            return _token != null;
        }

        return false;
    }

    /** Put a token into the mailbox.  If the argument is null, then the
     *  mailbox will not contain a token after this returns.
     *  @param token The token to be put into the mailbox.
     *  @exception NoRoomException If this mailbox is not empty.
     */
    @Override
    public void put(Token token) throws NoRoomException {
        if (_token != null) {
            throw new NoRoomException(getContainer(),
                    "Cannot put a token in a full mailbox.");
        }

        _token = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The token held. */
    protected Token _token = null;
}
