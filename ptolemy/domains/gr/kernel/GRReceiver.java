/* The receiver for the GR domain.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.gr.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// GRReceiver

/**
 The receiver for the GR domain. This receiver is a mailbox with capacity one,
 and any token put in the receiver overwrites any token previously present in
 the receiver. As a consequence, the hasRoom() method always returns true. The
 get() method will consume the token if there exists one. After the
 consumption, the hasToken() method will return false, until a token is put
 into this receiver.

 @see ptolemy.actor.Mailbox

 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating yellow (chf)
 @Pt.AcceptedRating yellow (vogel)
 */
public class GRReceiver extends Mailbox {
    /** Construct an empty GRReceiver with no container.
     */
    public GRReceiver() {
        super();
    }

    /** Construct an empty GRReceiver with the specified container.
     *
     *  @param container The port that contains the receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public GRReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true, since the new token will override the old one.
     *
     *  @return True.
     */
    @Override
    public boolean hasRoom() {
        return true;
    }

    /** Put a token into this receiver. If the argument is null,
     *  then this receiver will not contain a token after this method
     *  returns. If the receiver already has a token, then the new token
     *  will override the old token, and the old
     *  token will be lost.
     *
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException Not thrown in this base class
     */
    @Override
    public void put(Token token) throws NoRoomException {
        _token = token;
    }
}
