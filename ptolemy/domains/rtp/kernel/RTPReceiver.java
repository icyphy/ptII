/* The receiver for the Real-Time Processes domain.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.rtp.kernel;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.process.Branch;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// RTPReceiver

/**
 A receiver for the RTP domain.  FIXME: Description of how this works.

 @author  Christoph Meyer, Ben Horowitz, and Edward A. Lee
 @version $Id$
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (liuj)
 */
public class RTPReceiver extends AbstractReceiver implements ProcessReceiver {
    /** Construct an empty RTPReceiver with no container.
     */
    public RTPReceiver() {
        super();
    }

    /** Construct an empty RTPReceiver with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public RTPReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Methods from the ProcessReceivers interface.
     */
    /** Clear this receiver of any contained tokens.
     */
    public void clear() {
        reset();
    }

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

    public boolean isConsumerReceiver() {
        return false;
    }

    public boolean isInsideBoundary() {
        return false;
    }

    public boolean isOutsideBoundary() {
        return false;
    }

    /**
     */
    public boolean isProducerReceiver() {
        return false;
    }

    public void reset() {
        _token = null;
        _terminate = false;
    }

    public void requestFinish() {
        // This implementation is borrowed from PN.
        // Set a flag.
        _terminate = true;

        synchronized (_lock) {
            // wake up any thread waiting to read from
            // this receiver
            _lock.notifyAll();
        }
    }

    /** Return false because this receiver is never read blocked.
     *  @return false
     */
    public boolean isReadBlocked() {
        return false;
    }

    /** Return false because this receiver is never write blocked.
     *  @return false
     */
    public boolean isWriteBlocked() {
        return false;
    }

    /** Throw an exception because hierarchy is not yet supported.
     *  @param controllingBranch The branch.
     *  @return Not returned in this method.
     */
    public Token get(Branch controllingBranch) {
        throw new InvalidStateException(getContainer(),
                "hierarchy not supported yet.");
    }

    /** Throw an exception because hierarchy is not yet supported.
     *  @param controllingBranch The branch.
     */
    public void put(Token token, Branch controllingBranch) {
        throw new InvalidStateException(getContainer(),
                "hierarchy not supported yet.");
    }

    /** Blocking read on the token. This method will not return until
     *  there is a new token be put into the receiver.
     *  @return A token.
     *  @exception NoTokenException Not thrown in this base class
     *  @exception TerminateProcessException If the director has requested
     *   the execution to stop.
     */
    public Token get() throws NoTokenException {
        Token t;

        synchronized (_lock) {
            while (_token == null) {
                try {
                    _lock.wait();
                } catch (InterruptedException ex) {
                    // Ignore, keep waiting.
                }

                if (_terminate) {
                    throw new TerminateProcessException("RTPReceiver stopped.");
                }
            }

            t = _token;
            _token = null;
        }

        return t;
    }

    /** Return true, since writing to the receiver is always allowed.
     *  @return Always return true.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true, since writing to the receiver is always allowed.
     *  @param numberOfTokens The number of tokens 
     *  (ignored in this base class). 
     *  @return Always return true.
     */
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /** Return true if there is a token available.
     *  @return Always return True, because there is always a token available.
     */
    public boolean hasToken() {
        return true;
    }

    /** Return true, since the get() method will stall until a
     *  token is available.
     *  @param numberOfTokens The number of tokens 
     *  (ignored in this base class). 
     *  @return Always return true.
     */
    public boolean hasToken(int numberOfTokens) {
        return true;
    }

    /** Put a token into this receiver. It will notify all the blocking
     *  reads on the token.
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException Not thrown in this base class.
     *  @exception TerminateProcessException If the director has requested
     *   the execution to stop.
     */
    public void put(Token token) throws NoRoomException {
        if (_terminate) {
            throw new TerminateProcessException("RTPReceiver stopped.");
        }

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

    // If true, then the director wants to stop executing the model.
    private boolean _terminate = false;

    ///////////////////////////////////////////////////////////////////
    ////                        Inner Class                        ////

    /** A null token indicating no token is available.
     */
    private class Lock extends Object {
    }
}
