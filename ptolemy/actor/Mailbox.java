/* A Token holder with a capacity of one token.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)

*/

package ptolemy.actor;
import ptolemy.data.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Mailbox
/**
A token holder with capacity one.

@author Jie Liu, Edward A. Lee, Lukito Muliadi
@version $Id$
*/
public class Mailbox extends AbstractReceiver {

    /** Construct an empty Mailbox with no container.
     */
    public Mailbox() {
	super();
    }

    /** Construct an empty Mailbox with the specified container.
     *  @param container The container.
     */
    public Mailbox(IOPort container) {
	super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the contained Token.  If there is none, thrown an exception.
     *  The token is removed.
     *  @return A token.
     *  @exception NoTokenException If the Mailbox is empty.
     */
    public Token get() throws NoTokenException {
        if(_token == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty mailbox.");
        }
        Token t = _token;
        _token = null;
        return t;
    }

    /** Return true if the Mailbox is empty.
     *  @return True if the Mailbox is empty.
     */
    public boolean hasRoom() {
        return (_token == null);
    }

    /** Return true if the Mailbox is not empty.
     *  @return True if the Mailbox is not empty.
     */
    public boolean hasToken() {
        return (_token != null);
    }
    
    /** Put a token into the mailbox.  If the argument is null, then the
     *  mailbox will not contain a token after this returns.
     *  @param token The token to be put into the mailbox.
     *  @exception NoRoomException If the Mailbox already contains
     *   a previously put token that has not been gotten.
     */
    public void put(Token token) throws NoRoomException {
        if(_token != null) {
            throw new NoRoomException(getContainer(),
                    "Cannot put a token in a full mailbox.");
        }
        _token = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The token held.
    private Token _token = null;
}
