/* Discrete Event (DE) domain Receiver.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.data.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// DEReceiver
/** An implementation of the Receiver interface for the DE domain.  Tokens
 *  received by this receiver have a time stamp.  If the time stamp is not
 *  explicitly given, then it is assumed to be the current time (which is
 *  maintained by the director).  The get() method returns only tokens
 *  with time stamps equal to or earlier than the current time. Thus, when
 *  a token is put into the receiver using put(), it does not become
 *  immediately available to the get() method.  Instead, it is sent to
 *  the director to be queued. The director sorts tokens by time stamp.
 *  When the current time advances to the match the time stamp of
 *  the token, the director inserts the token into the receiver (using
 *  the _triggerEvent() method).  After that point, the token can be
 *  retrieved using get().
 *  <p>
 *  To explicitly specify the time stamp of a token, a version of the put()
 *  method is provided that takes one more argument representing the time
 *  stamp. The standard put() method from the Receiver interface implicitly
 *  uses the current time as the time stamp.
 *  <p>
 *  For use by the director, this receiver has a 'depth' field
 *  indicating its depth in the topology.  This is used by the receiver
 *  to prioritize firings of actors when dealing with simultaneous events.
 *  Actors containing receivers with a smaller depth are given priority
 *  over actors with a larger depth.
 *  <p>
 *  Before firing an actor, the director is expected to put at least one
 *  token into at least one of the receivers contained by the actor.
 *
 *  @author Lukito Muliadi, Edward A. Lee
 *  @version $Id$
 */
public class DEReceiver implements Receiver {

    /** Construct an empty DEReceiver with no container.
     */
    public DEReceiver() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from the receiver.  The token returned is one that
     *  was put in the receiver with a time stamp equal to or earlier than
     *  the current time.  If there is no such token, throw an
     *  exception.
     *
     *  @return A token.
     *  @exception NoSuchItemException If there are no more tokens.
     */
    public Token get() throws NoSuchItemException {
        if(_tokens.isEmpty()) {
            throw new NoSuchItemException(getContainer(),
                    "No more tokens in the DE receiver.");
        }
        return (Token)_tokens.take();
    }

    /** Return the container.
     *  @return An instance of IOPort.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return the director.
     *  @return An instance of DECQDirector.
     *  @exception IllegalActionException If there is no container port, or
     *   if the port has no container actor, or if the actor has no director.
     */
    // FIXME: This should return DEDirector, not DECQDirector.
    public DECQDirector getDirector() throws IllegalActionException {
        IOPort port = (IOPort)getContainer();
        if (port != null) {
            if (_directorVersion == port.workspace().getVersion()) {
                return _director;
            }
            // Cache is invalid.  Reconstruct it.
            Actor actor = (Actor)port.getContainer();
            if (actor != null) {
                Director dir = actor.getDirector();
                if (dir instanceof DECQDirector) {
                    _director = (DECQDirector)dir;
                    _directorVersion = port.workspace().getVersion();
                    return _director;
                }
            }
        }
        throw new IllegalActionException(getContainer(),
        "Does not have a DE director.");
    }

    /** Return true (the capacity is unbounded).
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true if there are tokens available to the get() method.
     *  @return True if there are more tokens.
     */
    public boolean hasToken() {
        return (!_tokens.isEmpty());
    }

    /** Put a token into the receiver with no delay.  I.e., the time stamp
     *  is equal to the current time (obtained from the director).  Note that
     *  this token does not become immediately available to the get() method.
     *  Instead, the token is queued with the director, and the director
     *  must put the token back into this receiver using the _triggerEvent()
     *  method in order for the token to become available.
     *
     *  @param token The token to put.
     */
    public void put(Token token) throws IllegalActionException{
        getDirector().enqueueEvent(this, token, 0.0, _depth);
    }

    /** Put a token with the specified delay into the receiver.  The time
     *  stamp of the token is equal to the current time (obtained from the
     *  director) plus the delay.  Note that
     *  this token does not become immediately available to the get() method.
     *  Instead, the token is queued with the director, and the director
     *  must put the token back into this receiver using the _triggerEvent()
     *  method in order for the token to become available.
     *
     *  @param token The token to put.
     *  @param delay The delay of the token.
     *  @exception IllegalActionException If the delay is negative, or if
     *   there is no director.
     */
    public void put(Token token, double delay)
            throws IllegalActionException {
        getDirector().enqueueEvent(this, token, delay, _depth);
    }

    /** Set the container.
     *  @param port The container.
     */
    public void setContainer(IOPort port) {
        _container = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the depth of this receiver, obtained from the topological
     *  sort.  The depth determines the priority assigned to tokens
     *  with equal time stamps.  A smaller depth corresponds to a
     *  higher priority.
     */
    protected void setDepth(long depth) {
        _depth = depth;
    }

    /** Make a token available to the get() method.
     *  Normally, only a director should use this method. It uses it
     *  when its current time matches the time stamp of the token.
     *
     *  @param token The token to make available.
     */
    protected void _triggerEvent(Token token) throws IllegalActionException {
        _tokens.insertFirst(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // _container
    private IOPort _container = null;

    // _depth: The topological depth associated with this receiver.
    long _depth = 0;

    // FIXME: This should be DEDirector, not DECQDirector.
    DECQDirector _director;
    long _directorVersion = -1;

    // List for storing tokens.  Access with clear(), insertFirst(), take().
    private LinkedList _tokens = new LinkedList();
}
