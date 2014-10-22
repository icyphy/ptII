/* Receiver for the component interaction (CI) domain.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.domains.ci.kernel;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// CIReceiver

/**
 An implementation of the ptolemy.actor.Receiver interface for the CI
 domain. This receiver provides a FIFO buffer between an active actor
 and an inactive actor or two inactive actors. When an active actor
 with push output puts a token in a receiver, the inactive actor that
 reads from the receiver will be put in the task queue of the director.
 When the director fires an inactive actor, the actors that receive
 data from this actor are executed as data-driven. For an active actor
 with pull input, its actor manager will be notified when an input
 token arrives, and will continue to iterate the actor.
 <p>

 @author Xiaojun Liu, Yang Zhao
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 */
public class CIReceiver extends AbstractReceiver {
    /** Construct an empty receiver.
     *  @param director The director that creates this receiver.
     */
    public CIReceiver(CIDirector director) {
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver.
     */
    @Override
    public void clear() {
        _tokens.clear();
    }

    /** Get a token from this receiver.
     *  @return A token from this receiver.
     *  @exception ptolemy.actor.NoTokenException If there is no token.
     */
    @Override
    public synchronized Token get() throws NoTokenException {
        if (_tokens.size() == 0) {
            throw new NoTokenException(getContainer(),
                    "No more tokens in the CI receiver.");
        }

        return _tokens.removeFirst();
    }

    /** Return true. The receiver acts as an infinite FIFO buffer.
     *  @return True if the next call to put() will not result in a
     *   NoRoomException.
     */
    @Override
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
    @Override
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /** Return true if the receiver contains a token that can be obtained
     *  by calling the get() method.
     *  @return True if the next call to get() will not result in a
     *   NoTokenException.
     */
    @Override
    public synchronized boolean hasToken() {
        return _tokens.size() > 0;
    }

    /** Return true if the receiver contains the specified number of tokens.
     *  In an implementation, returning true in this method guarantees
     *  that the next <i>numberOfTokens</i> calls to get(), or a
     *  corresponding call to getArray(), will not result in an exception.
     *  @param numberOfTokens The number of tokens desired.
     *  @return True if the next <i>numberOfTokens</i> calls to get()
     *   will not result in a NoTokenException.
     */
    @Override
    public synchronized boolean hasToken(int numberOfTokens) {
        return _tokens.size() >= numberOfTokens;
    }

    /** Put the specified token into this receiver.
     *  @param token The token to put into the receiver, or null to put no token.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    @Override
    public synchronized void put(Token token) throws NoRoomException {
        if (token == null) {
            return;
        }
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
    @Override
    public synchronized void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException {
        for (int i = 0; i < numberOfTokens; i++) {
            _tokens.add(tokenArray[i]);
        }

        _notify();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _initialize() throws IllegalActionException {
        IOPort port = getContainer();
        _isPush = CIDirector._isPushPort(port);
        _actor = (Actor) port.getContainer();
        _isAsyncPullSink = !_isPush && CIDirector._isActive(_actor);

        Iterator<?> sourcePorts = port.sourcePortList().iterator();

        if (sourcePorts.hasNext()) {
            port = (IOPort) sourcePorts.next();

            Actor actor = (Actor) port.getContainer();
            _isAsyncPushSink = _isPush && CIDirector._isActive(actor);
        }

        _initialized = true;
    }

    /* If an active actor with push output puts a token in this receiver,
     * put the inactive actor that reads from this receiver in the task
     * queue of the director. If an active actor with pull input reads
     * from this receiver, notify its actor manager.
     */
    private void _notify() {
        if (!_initialized) {
            try {
                _initialize();
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex);
                // At this time IllegalActionExceptions are not allowed to happen.
                // Width inference should already have been done.
            }
        }

        if (_isPush) {
            if (_isAsyncPushSink) {
                _director._addAsyncPushedActor(_actor);
            } else {
                _director._addSyncPushedActor(_actor);
            }
        } else {
            if (_director._isPulled(_actor)) {
                try {
                    if (_actor.prefire()) {
                        _director._actorEnabled(_actor);
                    } else {
                        _director._requestSyncPull(_actor);
                    }
                } catch (IllegalActionException ex) {
                    _actor.getManager().notifyListenersOfException(ex);
                }
            } else if (_isAsyncPullSink) {
                synchronized (_actor) {
                    _actor.notifyAll();
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The CI director that created this receiver.
    private CIDirector _director;

    // The actor that reads from this receiver.
    private Actor _actor;

    // List for storing tokens.
    private LinkedList<Token> _tokens = new LinkedList<Token>();

    private boolean _initialized = false;

    // True if an active actor put token in this receiver.
    private boolean _isAsyncPushSink = false;

    // True if an active actor reads from this receiver.
    private boolean _isAsyncPullSink = false;

    // True if this receiver is in a push input port.
    private boolean _isPush = false;
}
