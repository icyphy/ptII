/* Discrete Event (DE) domain receiver.

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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.data.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// DEReceiver
/** An implementation of the ptolemy.actor.Receiver interface for
 *  the DE domain.  Tokens that are put into this receiver logically
 *  have time stamps. If the time stamp is not explicitly given using
 *  the setDelay() method,  then it is assumed to be the current time
 *  (which is maintained by the director).  The put() method sends
 *  the specified token to the director, which returns it to this receiver
 *  (via the protected method _triggerEvent()) when current time matches
 *  the time stamp of the token. The get() method returns only tokens
 *  that the director has so returned. Thus, when
 *  a token is put into the receiver using put(), it does not become
 *  immediately available to the get() method.
 *  <p>
 *  By default, the time stamp of a token is the current time of the
 *  director when put() is called. To specify a time stamp in the
 *  future, call setDelay() prior to calling put().
 *  <p>
 *  Before firing an actor, the director is expected to put at least one
 *  token into at least one of the receivers contained by the actor.
 *
 *  @author Lukito Muliadi, Edward A. Lee, Jie Liu
 *  @version $Id$
 */
public class DEReceiver implements Receiver {

    /** Construct an empty DEReceiver with no container.
     */
    public DEReceiver() {
    }

    /** Construct an empty DEReceiver with the specified container.
     *  @param container The container.
     */
    public DEReceiver(IOPort container) {
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from the receiver.  The token returned is one that
     *  was put in the receiver with a time stamp equal to or earlier than
     *  the current time.  Note that there might be multiple such
     *  tokens in the receiver. In that case, FIFO behaviour is used with
     *  respect to the put() method. If there is no such token, throw an
     *  exception.
     *  @return A token.
     *  @exception NoTokenException If there are no more tokens. This is
     *   a runtime exception, so it need not be declared explicitly.
     */
    public Token get() throws NoTokenException {
        if(_tokens.isEmpty()) {
            throw new NoTokenException(getContainer(),
                    "No more tokens in the DE receiver.");
        }
        return (Token)_tokens.removeFirst();
    }

    /** Return the IOPort containing this receiver.
     *  @return An instance of IOPort.
     */
    public final IOPort getContainer() {
        return _container;
    }

    /** Return the director that created this receiver. Note that
     *  the director returned is guaranteed to be non-null.
     *  @return An instance of DEDirector.
     *  @exception IllegalActionException If there is no container port, or
     *   if the port has no container actor, or if the actor has no director,
     *   or if the director is not an instance of DEDirector.
     */
    public DEDirector getDirector() throws IllegalActionException {
        IOPort port = (IOPort)getContainer();
        if (port != null) {
            if (_directorVersion == port.workspace().getVersion()) {
                return _director;
            }
            // Cache is invalid.  Reconstruct it.
            Actor actor = (Actor)port.getContainer();
            if (actor != null) {
                Director dir;
                if ( (port.isOutput()) &&
                        (actor instanceof CompositeActor) &&
                        ((CompositeActor)actor).isOpaque()) {
                    dir = actor.getDirector();
                } else {
                    dir = actor.getExecutiveDirector();
                }
                if (dir != null) {
                    if (dir instanceof DEDirector) {
                        _director = (DEDirector)dir;
                        _directorVersion = port.workspace().getVersion();
                        return _director;
                    }
                }
            }
        }
        throw new IllegalActionException(getContainer(),
                "Does not have a DE director.");
    }

    /** Return true, indicating that there is always room.
     *  @return True.
     */
    public final boolean hasRoom() {
        return true;
    }

    /** Return true if there are tokens available to the get() method.
     *  @return True if there are more tokens.
     */
    public final boolean hasToken() {
        return (!_tokens.isEmpty());
    }

    /** Put a token into this receiver. Note that
     *  this token does not become immediately available to the get() method.
     *  Instead, the token is queued with the director, and the director
     *  must put the token back into this receiver using the _triggerEvent()
     *  protected method in order for the token to become available to
     *  the get() method.  By default, this token will be enqueued by
     *  the director with the current time
     *  of the director.  However, by calling setDelay() before calling put(),
     *  you can enqueue the event with a time stamp at a future time.
     *  @param token The token to be put.
     */
    public void put(Token token) {
        try {
            DEDirector dir = getDirector();
            if (_useDelay) {
                _useDelay = false;
                if (_delay == 0.0) {
                    // Use special enqueue method to increment microstep.
                    dir._enqueueEvent(this, token);
                } else {
                    dir._enqueueEvent(this, token,
                            dir.getCurrentTime() + _delay);
                }
            } else {
                dir._enqueueEvent(this, token, dir.getCurrentTime());
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Put a token into this receiver with a future time stamp. This token
     *  will be available to the get() method at the time specified.
     *  Note that the time should be greater than or equal
     *  to the current time of the director, otherwise an exception will
     *  be thrown.
     *
     *  @param token The token to be put.
     *  @param time The time stamp of the token
     *  @exception IllegalActionException If time is less than the
     *     current time of the director, or no director is available.
     */
    public void put(Token token, double time)
            throws IllegalActionException{
        DEDirector dir = getDirector();
        double now = dir.getCurrentTime();
        if (time < now) {
            throw new IllegalActionException(getContainer(),
                    "Cannot enqueue a token in the past.");
        }
        if (time == now) {
            // Use special enqueue method to increment microstep.
            dir._enqueueEvent(this, token);
        } else {
            dir._enqueueEvent(this, token, time);
        }
    }

    /** Set the IOPort containing this receiver.
     *  @param port The container.
     */
    public void setContainer(IOPort port) {
        _container = port;
    }

    /** Set the delay for the next call to put().  This causes the director
     *  to make the token available for the get() method at some future time,
     *  i.e. current time plus the specified delay.  This value of delay is
     *  only used in the next call to put().
     *  If the specified delay is zero, then the next event is queued to be
     *  processed in the next microstep.
     *  @param delay The delay.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void setDelay(double delay) throws IllegalActionException {
        if (delay < 0.0) {
            throw new IllegalActionException(getContainer(),
                    "Cannot specify a negative delay.");
        }
        _delay = delay;
        _useDelay = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make a token available to the get() method.
     *  Normally, only a director will call this method. It calls it
     *  when current time matches the time stamp of the token, i.e.
     *  when the delay specified by setDelay() has elapsed.
     *  @param token The token to make available to get().
     */
    protected void _triggerEvent(Token token) {
        _tokens.add(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The delay for the next call to put().
    private double _delay = 0.0;

    // A flag indicating that setDelay() has been called.
    private boolean _useDelay = false;

    // IOPort containing this receiver.
    private IOPort _container = null;

    // The topological depth associated with this receiver.
    //private int _depth = 0;

    // The director where this DEReceiver should register the
    // events being put in it. If this receiver is an inside receiver of
    // an output port of
    // an opaque composite actor, then the director will be the local director
    // of the container of its port. Otherwise, it's the executive director of
    // the container of its port.
    // NOTE: This should be accessed only via getDirector().
    private DEDirector _director;
    private long _directorVersion = -1;

    // List for storing tokens.  Access with clear(), add(),
    // and take().
    private LinkedList _tokens = new LinkedList();
}
