/* A Token holder for Port-Based Objects

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)

*/

package ptolemy.domains.pbo.kernel;
import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// PBOReceiver
/**
A token holder with capacity one.   This is similar to a Mailbox, except
this receiver will always accept a token.

@author Stephen Neuendorffer
@version $Id$
*/
public class PBOReceiver extends AbstractReceiver {

    /** Construct an empty PBOReceiver with no container.
     */
    public PBOReceiver() {
        super();
    }

    /** Construct an empty PBOReceiver with the specified container.
     *  @param container The container.
     */
    public PBOReceiver(IOPort container)
            throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Clear this receiver of any contained tokens.
     */
    public void clear() {
        _token = null;
    }

    /** Get the contained Token.  This returns a reference to the contained
     *  token, but does not remove the token from the receiver.
     *  @return A token.
     */
    public Token get() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty PBOReceiver");
        }
        return _token;
    }

    /** Get an array of tokens from this receiver. The <i>numberOfTokens</i>
     *  argument specifies the number of tokens to get. In an implementation,
     *  the length of the returned array is allowed to be greater than
     *  <i>numberOfTokens</i>, in which case, only the first
     *  <i>numberOfTokens</i> elements are the ones that have been read
     *  from the receiver. In some domains, this allows the implementation
     *  to be more efficient.
     *  @param numberOfTokens The number of tokens to get in the
     *   returned array.
     *  @return An array of tokens read from the receiver.
     *  @exception NoTokenException If there are not <i>numberOfTokens</i>
     *   tokens.
     */
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
        Token[] tokens = new Token[numberOfTokens];
        for (int i = 0; i < numberOfTokens; i++) {
            tokens[i] = _token;
        }
        return tokens;
    }

    /** Return the container.
     *  @return The container.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** Return true if the PBOReceiver has room for a token.
     *  @return True
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true if the receiver has room to put the specified number of
     *  tokens into it (via the put() method).
     *  Returning true in this method guarantees that the next
     *  <i>numberOfTokens</i> calls to put() or a corresponding call
     *  to putArray() will not result in an exception.
     *  @param numberOfTokens The number of tokens to put into this receiver.
     *  @return True if the next <i>numberOfTokens</i> calls to put()
     *   will not result in a NoRoomException.
     */
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /** Return true if the PBOReceiver contains a token.
     *  @return True if the PBOReceiver is not empty.
     */
    public boolean hasToken() {
        return (_token != null);
    }

    /** Return true if the receiver contains the specified number of tokens.
     *  In an implementation, returning true in this method guarantees
     *  that the next <i>numberOfTokens</i> calls to get(), or a
     *  corresponding call to getArray(), will not result in an exception.
     *  @param numberOfTokens The number of tokens desired.
     *  @return True if the next <i>numberOfTokens</i> calls to get()
     *   will not result in a NoTokenException.
     */
    public boolean hasToken(int numberOfTokens) {
        return true;
    }

    /** Put a token into the mailbox.  If the argument is null, then the
     *  mailbox will not contain a token after this returns.
     *  @param token The token to be put into the mailbox.
     *  @exception NoRoomException If the PBOReceiver already contains
     *   a previously put token that has not been gotten.
     */
    public void put(Token token) throws NoRoomException {
        _token = token;
    }

    /** Put a portion of the specified token array into this receiver.
     *  The first <i>numberOfTokens</i> elements of the token array are put
     *  into this receiver.  The ability to specify a longer array than
     *  needed allows certain domains to have more efficient implementations.
     *  @param tokenArray The array containing tokens to put into this
     *   receiver.
     *  @param numberOfTokens The number of elements of the token
     *   array to put into this receiver.
     *  @exception NoRoomException If the token array cannot be put.
     */
    public void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException {
        _token = tokenArray[numberOfTokens - 1];
    }

    /** Set the container. */
    public void setContainer(IOPort port) {
        _container = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container of this object.
    private IOPort _container = null;

    // The token held.
    private Token _token = null;
}
