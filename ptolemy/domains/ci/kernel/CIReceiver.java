/* A receiver for the component interaction domain.

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

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.Token;


import java.util.NoSuchElementException;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CIReceiver
/** An implementation of the ptolemy.actor.Receiver interface for the
CI domain. When a token is put into a CIReceiver,
that receiver check whether the port is push or pull, and
whether the current thread equals to the director thread. If it is a push port
and the current thread equals to the director thread, the director will
add the actor contains the port to _actorsToFire list. If it is a push port
and the current thread doesn't equal to the director thread, the active actor
thread will add the actor to _asyncPushedactors list. If it is a pull port,
the current thread has to equal to the director thread. The director will check
whether the actor which contains the port has been pulled. if so and the prefire
is true, the director will remove this actor from _asyncPulledActors list and
add it to _actorToFire list; if the prefire return false, the director will then
register actors providing data to this actor to be fired.
<p>
There is nothing special about get() and hasToken() methods.
<p>
@author Xiaojun Liu, Yang Zhao
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.actor.Receiver
*/
public class CIReceiver extends AbstractReceiver {
    /** Construct an empty receiver working with the given CI director.
     *
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

    /** Return true if the receiver has room to put a token into it
     *  (via the put() method).
     *  Returning true in this method guarantees that the next call to
     *  put() will not result in an exception.
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
     *  by calling the get() method.  In an implementation,
     *  returning true in this method guarantees that the next
     *  call to get() will not result in an exception.
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
        // token.setTime(_director.getCurrentTime());
        _tokens.add(token);
        _notify();
    }

    /** Put a portion of the specified token array into this receiver.
     *  The first <i>numberOfTokens</i> elements of the token array are put
     *  into this receiver by repeated calling put().
     *  The ability to specify a longer array than
     *  needed allows certain domains to have more efficient implementations.
     *  <p>
     *  This implementation works by calling put() repeatedly.
     *  The caller may feel free to reuse the array after this method returns.
     *  Derived classes may offer more efficient implementations.
     *  This implementation is not synchronized, so it
     *  is not suitable for multithreaded domains
     *  where there might be multiple threads writing to
     *  the same receiver. It <i>is</i> suitable, however,
     *  for multithreaded domains where only one thread
     *  is writing to the receiver.  This is true even if
     *  a separate thread is reading from the receiver, as long
     *  as the put() and get() methods are properly synchronized.
     *
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

    /*
       if the container is a push port,
           if the current thread != director thread
               add actor as async pushed actor
           else
               add actor as sync pushed actor
       else the container is a pull port
           if the current thread != director thread
               // can only happen when an active actor is followed by an actor
               // with pull input, not a legal model
           else
               if the actor is pulled
                   if actor.prefire
                       add actor as sync pushed actor - actor to fire
                   else
                       requestSyncPull(actor)
    */
    private void _notify() {
        IOPort port = (IOPort)getContainer();
        boolean isPush = false;
        if (port.getAttribute("push") != null) {
            isPush = true;
        }
        Actor actor = (Actor)port.getContainer();
        if (isPush) {
            if (Thread.currentThread() != _director._getThread()) {
                _director._addAsyncPushedActor(actor);
            } else {
                _director._addSyncPushedActor(actor);
            }
        } else {
            // assume current thread is director thread
            // this requires that an active push actor do not connect directly
            // to an active pull actor
            if (_director._isPulled(actor)) {
                try {
                    if (actor.prefire()) {
                        _director._actorEnabled(actor);
                    } else {
                        _director._requestSyncPull(actor);
                    }
                } catch (IllegalActionException ex) {
                    //FIXME: ignore for now
                }
            }
        }
        // an active actor may be waiting
        synchronized (actor) {
            actor.notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director.
    private CIDirector _director;

    //List for storing tokens.
    private LinkedList _tokens = new LinkedList();

}
