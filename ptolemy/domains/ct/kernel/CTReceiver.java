/* The receiver for the CT domain.

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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;
//import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// CTReceiver
/**
The receiver for the continuous time domain. This is basically a mailbox
receiver which has just one capacity. If a token is put into the receiver
when the receiver is full, then the old token will be overwritten.
@author  Jie Liu
@version $Id$
*/
public class CTReceiver extends Mailbox{
    /** Construct an empty CTReceiver with no container.
     */
    public CTReceiver() {
        super();
    }

    /** Construct an empty CTReceiver with the specified container.
     *  @param container The container.
     */
    public CTReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true always, since the new token will override the old one.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Put a token into the CTReceiver. If the argument is null,
     *  then the CTReceiver will not contain a token after this
     *  returns. If the receiver already has a token, then the old
     *  token will be lost, and the receiver only contains the new
     *  token.
     *  @param token The token to be put into the CTReceiver.
     *  @exception NoRoomException Not thrown in this class.
     */
    public void put(Token token) throws NoRoomException{
        if(hasToken()) {
            get();
        }
        super.put(token);
    }
}
