/* A Token holder for Port-Based Objects

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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)

*/

package ptolemy.domains.pbo.kernel;
import ptolemy.data.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

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
    public PBOReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the contained Token.  This returns a reference to the contained
     *  token, but does not remove the token from the receiver.
     *  @return A token.
     */
    public Token get() throws NoTokenException {
	if(_token == null) {
	    throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty PBOReceiver");
	}
        return _token;
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

    /** Return true if the PBOReceiver contains a token.
     *  @return True if the PBOReceiver is not empty.
     */
    public boolean hasToken() {
        return (_token != null);
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
