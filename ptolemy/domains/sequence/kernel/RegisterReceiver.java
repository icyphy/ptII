/* A receiver which extends Mailbox and holds a single token.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RegisterReceiver

/**
 A receiver which extends Mailbox and holds a single token.

 @author Elizabeth Latronico (Bosch)
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (beth)
 @Pt.AcceptedRating Red (beth)
 */
public class RegisterReceiver extends Mailbox {

    /** Construct an empty RegisterReceiver with no container.
     */
    public RegisterReceiver() {
        super();
    }

    /** Construct an empty RegisterReceiver with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public RegisterReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    /** Get the contained Token.  If there is none, throw an exception.
     *  @return The token contained by this receiver.
     *  @exception NoTokenException If this receiver is empty.
     */
    @Override
    public Token get() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty mailbox.");
        }

        return _token;
    }

    /** Put a token into the registerReceiver.
     *  @param token The token to be put into the mailbox, or null to set
     *   the mailbox to empty.
     *  @exception NoRoomException Not thrown in this base class.
     */
    @Override
    public void put(Token token) throws NoRoomException {
        _token = token;
    }
}
