/* Discrete Event (DE) domain receiver.

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
package ptolemy.domains.de.kernel;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DEReceiver

/** An implementation of the ptolemy.actor.Receiver interface for the
 DE domain.
 <p>
 The put() method stores the given token in this receiver and posts a
 trigger event to the director. The director is responsible to dequeue that
 trigger event and invoke the actor that contains this receiver.
 The get() method returns the first available token from the receiver.
 <p>
 Before firing an actor, the director is expected to put at least one
 token into at least one of the receivers contained by the actor.

 @author Lukito Muliadi, Edward A. Lee, Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class DEReceiver extends AbstractReceiver {
    /** Construct an empty DEReceiver with no container.
     */
    public DEReceiver() {
        super();
    }

    /** Construct an empty DEReceiver with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *  not accept this receiver.
     */
    public DEReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained tokens.
     */
    @Override
    public void clear() {
        _tokens.clear();
    }

    /** Return a list with tokens that are currently in the receiver
     *  available for get() or getArray(), beginning with the oldest one.
     *  @return A list of instances of Token.
     */
    @Override
    public List<Token> elementList() {
        return _tokens;
    }

    /** Get the first token from the receiver. The token returned is one that
     *  was put in the receiver with a timestamp equal to or earlier than
     *  the current time. If there is no token, throw an exception. If this
     *  receiver contains more than one event, the oldest event is removed
     *  first. In other words, this receiver has a FIFO behavior.
     *  @return A token.
     *  @exception NoTokenException If there are no more tokens. This is
     *   a runtime exception, so it need not to be declared explicitly.
     */
    @Override
    public Token get() throws NoTokenException {
        if (_tokens.isEmpty()) {
            throw new NoTokenException(getContainer(),
                    "No more tokens in the DE receiver.");
        }

        return (Token) _tokens.removeFirst();
    }

    /** Return true, indicating that there is always room.
     *  @return True.
     */
    @Override
    public final boolean hasRoom() {
        return true;
    }

    /** Return true if the receiver has room for putting the given number of
     *  tokens into it (via the put() method).
     *  Returning true in this method should also guarantee that calling
     *  the put() method will not result in an exception.
     *  @param tokens An int indicating the number of spaces available.
     *  @return True.
     */
    @Override
    public boolean hasRoom(int tokens) {
        return true;
    }

    /** Return true if there is at least one token available to the
     *  get() method.
     *  @return True if there are more tokens.
     */
    @Override
    public boolean hasToken() {
        return !_tokens.isEmpty();
    }

    /** Return true if there are <i>numberOfTokens</i>
     *  tokens tokens available to the get() method.
     *  @param numberOfTokens An int indicating how many tokens are needed.
     *  @return True if there are numberOfTokens tokens available.
     */
    @Override
    public boolean hasToken(int numberOfTokens) {
        return _tokens.size() >= numberOfTokens;
    }

    /** Put a token into this receiver and post a trigger event to the director.
     *  The director will be responsible to dequeue the trigger event at
     *  the correct timestamp and microstep and invoke the corresponding actor
     *  whose input port contains this receiver. This receiver may contain
     *  more than one events.
     *  @param token The token to be put, or null to put no token.
     *  @exception IllegalActionException If cannot get the director or if
     *   the current microstep is zero.
     */
    @Override
    public void put(Token token) throws IllegalActionException {
        if (token == null) {
            return;
        }
        DEDirector dir = _getDirector();
        dir._enqueueTriggerEvent(getContainer());
        _tokens.add(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The version of the workspace of container, used for
     *  caching by _getDirector().
     *  Derived classes that modify the cache may need to update
     *  this variable.
     */
    protected long _directorVersion = -1;

    /** List for storing tokens.  Access with clear(), add(), and take(). */
    protected LinkedList _tokens = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the director that created this receiver.
     *  If this receiver is an inside receiver of
     *  an output port of an opaque composite actor,
     *  then the director will be the local director
     *  of the container of its port. Otherwise, it's the executive
     *  director of the container of its port.Note that
     *  the director returned is guaranteed to be non-null.
     *  This method is read synchronized on the workspace.
     *  @return An instance of DEDirector.
     *  @exception IllegalActionException If there is no container port, or
     *   if the port has no container actor, or if the actor has no director,
     *   or if the director is not an instance of DEDirector.
     */
    private DEDirector _getDirector() throws IllegalActionException {
        IOPort port = getContainer();

        if (port != null) {
            if (_directorVersion == port.workspace().getVersion()) {
                return _director;
            }

            // Cache is invalid.  Reconstruct it.
            try {
                port.workspace().getReadAccess();

                Actor actor = (Actor) port.getContainer();

                if (actor != null) {
                    Director dir;

                    if (!port.isInput() && actor instanceof CompositeActor
                            && ((CompositeActor) actor).isOpaque()) {
                        dir = actor.getDirector();
                    } else {
                        dir = actor.getExecutiveDirector();
                    }

                    if (dir != null) {
                        if (dir instanceof DEDirector) {
                            _director = (DEDirector) dir;
                            _directorVersion = port.workspace().getVersion();
                            return _director;
                        } else {
                            throw new IllegalActionException(getContainer(),
                                    "Does not have a DEDirector.");
                        }
                    } else {
                        throw new IllegalActionException(getContainer(),
                                "No outside director found.");
                    }
                }
            } finally {
                port.workspace().doneReading();
            }
        }

        throw new IllegalActionException(getContainer(),
                "Does not have an IOPort as the container of the receiver.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The director where this DEReceiver should register for De events.
    private DEDirector _director;
}
