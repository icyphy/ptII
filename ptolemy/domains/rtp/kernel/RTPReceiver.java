/* The receiver for the Real-Time Processes domain.

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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.rtp.kernel;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// RTPReceiver
/**
A receiver for the RTP domain.  FIXME: Description of how this works.

@author  Cristoph Meyer, Ben Horowitz, and Edward A. Lee
@version $Id$
*/
public class RTPReceiver extends AbstractReceiver implements ProcessReceiver {

    /** Construct an empty RTPReceiver with no container.
     */
    public RTPReceiver() {
        super();
    }

    /** Construct an empty RTPReceiver with the specified container.
     *  @param container The container.
     */
    public RTPReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Methods from the ProcessReceivers interface.
     */

    /** FIXME: Let it make sense.
     */
    public boolean isConnectedToBoundary() {
        return false;
    }

    /** FIXME: Let it make sense.
     */
    public boolean isConnectedToBoundaryInside() {
        return false;
    }

    /** FIXME: Let it make sense.
     */
    public boolean isConnectedToBoundaryOutside() {
        return false;
    }

    public boolean isInsideBoundary() {
        return false;
    }

    public boolean isOutsideBoundary() {
        return false;
    }

    public void reset() {
        _token = null;
    }

    public void requestFinish() {
        // How to finish?
        synchronized(_lock) {
            _token = new Token();
            _lock.notifyAll();
        }
    }

    public boolean isReadBlocked() {
        return false;
    }

    public boolean isWriteBlocked() {
        return false;
    }



    /** Bolcking read on the token. This method will not return until
     *  there is a new token be put into the receiver.
     *  @return A token.
     *  @exception NoTokenException Never thrown.
     */
    public Token get() throws NoTokenException {
        Token t;
        synchronized(_lock) {
            while (_token == null) {
                try {
                    _lock.wait();
                } catch (InterruptedException ex) {
                    // Ignore, keep waiting.
                }
            }
            t = _token;
            _token = null;
        }
        return t;
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
        return true;
    }

    /** Put a token into this receiver. It will notify all the blocking
     *  reads on the token.
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException Not thrown in this class.
     */
    public void put(Token token) throws NoRoomException {
        synchronized (_lock) {
            _token = token;
            _lock.notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The token available for reading.
    private Token _token = null;

    // The lock object.
    private Lock _lock = new Lock();

    ///////////////////////////////////////////////////////////////////
    ////                        Inner Class                        ////
    /** A null token indicating no token is available.
     */
    private class Lock extends Object {
    }
}
