/* Test AbstractReceiver

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
package ptolemy.actor.test;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// TestAbstractReceiver

/**
 Test AbstractReceiver.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestAbstractReceiver extends AbstractReceiver {
    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public TestAbstractReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from this receiver.
     *  @exception NoTokenException If there is no token.
     */
    @Override
    public Token get() throws NoTokenException {
        return null;
    }

    /** Return true if the receiver has room to put a token into it
     *  (via the put() method).
     *  Returning true in this method guarantees that the next call to
     *  put() will not result in an exception.
     *  @return True
     */
    @Override
    public boolean hasRoom() {
        return true;
    }

    /** Return true if the receiver has room to put the specified number of
     *  tokens into it (via the put() method).
     *  Returning true in this method guarantees that the next
     *  <i>numberOfTokens</i> calls to put() or a corresponding call
     *  to putArray() will not result in an exception.
     *  @param numberOfTokens The number of tokens to put into this receiver.
     *  @return True
     */
    @Override
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /** Return true if the receiver contains a token that can be obtained
     *  by calling the get() method.  In an implementation,
     *  returning true in this method guarantees that the next
     *  call to get() will not result in an exception.
     *  @return True
     */
    @Override
    public boolean hasToken() {
        return true;
    }

    /** Return true if the receiver contains the specified number of tokens.
     *  In an implementation, returning true in this method guarantees
     *  that the next <i>numberOfTokens</i> calls to get(), or a
     *  corresponding call to getArray(), will not result in an exception.
     *  @param numberOfTokens The number of tokens desired.
     *  @return True
     */
    @Override
    public boolean hasToken(int numberOfTokens) {
        return true;
    }

    /** Return <i>true</i>.  Most domains have no notion of the state of
     *  the receiver being unknown.  It is always known whether there is
     *  a token available. Certain domains with fixed point semantics,
     *  however, such as SR, will need to override this method.
     *  @return True.
     */
    @Override
    public boolean isKnown() {
        return true;
    }

    /** Put the specified token into this receiver.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the put fails
     *   (e.g. because of incompatible types).
     */
    @Override
    public void put(Token token) throws NoRoomException, IllegalActionException {

    }
}
