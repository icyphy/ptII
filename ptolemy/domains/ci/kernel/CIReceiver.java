/* Receiver for the component interaction (CI) domain.

 Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.ci.kernel;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.Token;
import ptolemy.actor.*;

import java.util.List;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CIReceiver
/**
An implementation of the ptolemy.actor.Receiver interface for the
CI domain. When a token is put into a CIReceiver, that receiver checks
whether the port containing the receiver is push or pull, and whether the
current thread is the same as the director thread. If it is a push port
and the current thread is the same as the director thread, the actor is
added to a list of actors to be fired by the director, so that the model
executes as data-driven. If it is a push port and the current thread is not
the same as the director thread, the active actor thread will notify the
director to process the pushed data. If it is a pull port, the director will
check whether the actor containing the port has been pulled. If so and its
prefire() is true, the director will fire the actor; if prefire() returns
false, the director then recursively registers actors providing data to this
actor as being pulled, so that the model executes as demand-driven.
<p>
@author Xiaojun Liu, Yang Zhao
@version $Id$
@since Ptolemy II 3.0
*/
public class CIReceiver extends AbstractReceiver {

    /** Construct an empty receiver working with the given CI director.
     *  @param director The director that creates this receiver.
     */
    public CIReceiver(CIDirector director) {
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from this receiver.
     *  @exception NoTokenException If there is no token.
     */
    public synchronized Token get() throws NoTokenException {
        if (_tokens.size() == 0)
            throw new NoTokenException(getContainer(),
                    "No more tokens in the CI receiver.");
        return (Token)_tokens.removeFirst();
    }

    /** Return true. The receiver acts as an infinite FIFO queue.
     *  @return True if the next call to put() will not result in a
     *   NoRoomException.
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

    /** Return true if the receiver contains a token that can be obtained
     *  by calling the get() method.
     *  @return True if the next call to get() will not result in a
     *   NoTokenException.
     */
    public synchronized boolean hasToken() {
        return (_tokens.size() > 0);
    }

    /** Return true if the receiver contains the specified number of tokens.
     *  In an implementation, returning true in this method guarantees
     *  that the next <i>numberOfTokens</i> calls to get(), or a
     *  corresponding call to getArray(), will not result in an exception.
     *  @param numberOfTokens The number of tokens desired.
     *  @return True if the next <i>numberOfTokens</i> calls to get()
     *   will not result in a NoTokenException.
     */
    public synchronized boolean hasToken(int numberOfTokens) {
        return (_tokens.size() >= numberOfTokens);
    }

    /** Put the specified token into this receiver.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    public synchronized void put(Token token) throws NoRoomException {
        _tokens.add(token);
        _notify();
    }

    /** Put a portion of the specified token array into this receiver.
     *  The first <i>numberOfTokens</i> elements of the token array are put
     *  into this receiver.
     *  @param tokenArray The array containing tokens to put into this
     *   receiver.
     *  @param numberOfTokens The number of elements of the token
     *   array to put into this receiver.
     *  @exception NoRoomException If the token array cannot be put.
     */
    public synchronized void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException {
        for (int i = 0; i < numberOfTokens; i++) {
            _tokens.add(tokenArray[i]);
        }
        _notify();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Checks whether the port containing the receiver is push or pull,
    // and whether the current thread is the same as the director thread.
    // If it is a push port and the current thread is the same as the
    // director thread, the actor is added to a list of actors to be fired
    // by the director. If it is a push port and the current thread is not
    // the same as the director thread, the active actor thread will notify
    // the director to process the pushed data. If it is a pull port, the
    // director will check whether the actor containing the port has been
    // pulled. If so and its prefire() is true, the director will fire the
    // actor; if prefire() returns false, the director then recursively
    // registers actors providing data to this actor as being pulled, so that
    // the model executes as demand-driven.
    private void _notify() {
        IOPort port = (IOPort)getContainer();
        boolean isPush = CIDirector._isPushPort(port);
        Actor actor = (Actor)port.getContainer();
        if (isPush) {
            if (Thread.currentThread() != _director._getThread()) {
                _director._addAsyncPushedActor(actor);
            } else {
                _director._addSyncPushedActor(actor);
            }
        } else {
            //FIXME: this does not allow an active source actor (with push
            // output) to be connected directly to a sync actor with pull
            // input
            if (_director._isPulled(actor)) {
                try {
                    if (actor.prefire()) {
                        _director._actorEnabled(actor);
                    } else {
                        _director._requestSyncPull(actor);
                    }
                } catch (IllegalActionException ex) {
                    //FIXME: better way to handle this
                    ex.printStackTrace();
                }
            }
            if (CIDirector._isActive(actor)) {
                synchronized (actor) {
                    actor.notifyAll();
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The CI director that created this receiver.
    private CIDirector _director;

    // List for storing tokens.
    private LinkedList _tokens = new LinkedList();

}

