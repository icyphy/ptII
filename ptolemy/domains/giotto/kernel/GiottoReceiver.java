/* The receiver for the Giotto domain.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// GiottoReceiver
/**
A receiver for the Giotto domain.  FIXME: Description of how this works.

@author  Cristoph Meyer, Ben Horowitz, and Edward A. Lee
@version $Id$
*/
public class GiottoReceiver extends AbstractReceiver {

    /** Construct an empty GiottoReceiver with no container.
     */
    public GiottoReceiver() {
        super();
    }

    /** Construct an empty GiottoReceiver with the specified container.
     *  @param container The container.
     */
    public GiottoReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the contained and available Token.
     *  @return A token.
     *  @exception NoTokenException If the receiver is empty.
     */
    public Token get() throws NoTokenException {
        if(_token == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty receiver.");
        }
        return _token;;
    }

    /** Return true, since writing to the receiver is always allowed.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true if there is a token available.
     *  @return True if there is a token available.
     */
    public boolean hasToken() {
        return (_token != null);
    }

    /** Put a token into this receiver. If the argument is null,
     *  then this receiver will not contain a token after this
     *  returns. If the receiver already has a token, then the old
     *  token will be lost. The token becomes available to the
     *  get() method only when update() is called.
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException Not thrown in this class.
     */
    public void put(Token token) throws NoRoomException {
        _nextToken = token;
    }

    /** Update the receiver by making any token that has been
     *  passed to put() available to get().
     */
    public void update() {
        _token = _nextToken;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The next token.
    private Token _nextToken = null;

    // The token available for reading.
    private Token _token = null;
}
