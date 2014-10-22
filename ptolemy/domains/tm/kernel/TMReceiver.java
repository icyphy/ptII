/* TM domain Receiver.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.domains.tm.kernel;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// TMReceiver

/**
 The receiver for the TM domain. This receiver contains a FIFO queue.
 Upon receiving a token, it creates a TMEvent. The properties of the
 TM events are:
 <ul>
 <li> The destination receiver is this receiver.
 <li> The destination actor is the container's container of this receiver.
 <li> The token is the received token.
 <li> The priority is the value of the parameter with name <i>priority</i>
 of the container of this receiver. If the container does not has a
 parameter with that name, then look at the actor. If none of them
 has the parameter, then use the default priority value, which is
 java.Thread.NORMAL_PRIORITY.
 <li> The flag <i>hasStarted</i> is false.
 <li> The processing time is obtained from the container or the container's
 container of this receiver, similar to the way obtaining the priority
 value. If none of them has the parameter, then use the default value 0.
 </ul>
 The event is then queued with the director, so it is not immediately
 available by the get() method. Later, the director may make the
 token available again by calling the _triggerEvent() method.
 See the TMDirector class for the event dispatching mechanism.

 @author Edward A. Lee, Jie Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (liuj)
 @Pt.AcceptedRating Yellow (janneck)
 @see ptolemy.domains.tm.kernel.TMDirector
 */
public class TMReceiver extends AbstractReceiver {
    /** Construct an empty TMReceiver with no container.
     */
    public TMReceiver() {
        super();
    }

    //FIXME: why doesn't this have the usual constructors
    // TMReceiver(IOPort container), TMReceiver(IOPort container, name)
    // It could also use TMReceiver(IOPort container, priority)
    // like DDEReceiver(IOPort container, priority)
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained tokens.
     */
    @Override
    public void clear() {
        _tokens.clear();
    }

    /** Return a list with the tokens currently in the receiver, or
     *  an empty list if there are no such tokens.
     *  @return A list of instances of Token.
     */
    @Override
    public List<Token> elementList() {
        return _tokens;
    }

    /** Get a token from the receiver.  The token returned is one that
     *  was put in the receiver that is ready for process.
     *  A token is ready to be processed if it has the highest priority
     *  among all the appending events (system wide), and the resource
     *  is ready to be allocated to its destination actor. Whether the
     *  resource is ready depends on whether there is any active tasks,
     *  and whether the execution is preemptive.
     *  Note that there might be multiple such
     *  tokens in the receiver. In that case, FIFO behaviour is used with
     *  respect to the put() method. If there is no such token, throw an
     *  exception. This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @return A token.
     *  @exception NoTokenException Not thrown in this base class.
     */
    @Override
    public synchronized Token get() throws NoTokenException {
        if (_tokens.isEmpty()) {
            throw new NoTokenException(getContainer(),
                    "No more tokens in the TM receiver.");
        }

        return (Token) _tokens.removeFirst();
    }

    /** Return the director that created this receiver.
     *  If this receiver is an inside receiver of
     *  an output port of an opaque composite actor,
     *  then the director will be the local director
     *  of the container of its port. Otherwise, it's the executive
     *  director of the container of its port. Note that
     *  the director returned is guaranteed to be non-null.
     *  This method is read synchronized on the workspace.
     *  @return An instance of TMDirector that creates this receiver.
     *  @exception IllegalActionException If there is no container port, or
     *   if the port has no container actor, or if the actor has no director,
     *   or if the director is not an instance of TMDirector.
     */
    public TMDirector getDirector() throws IllegalActionException {
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
                    Director director;

                    if (port.isOutput() && actor instanceof CompositeActor
                            && ((CompositeActor) actor).isOpaque()) {
                        director = actor.getDirector();
                    } else {
                        director = actor.getExecutiveDirector();
                    }

                    if (director != null) {
                        if (director instanceof TMDirector) {
                            _director = (TMDirector) director;
                            _directorVersion = port.workspace().getVersion();
                            return _director;
                        } else {
                            throw new IllegalActionException(getContainer(),
                                    "Does not have a TMDirector.");
                        }
                    }
                }
            } finally {
                port.workspace().doneReading();
            }
        }

        throw new IllegalActionException(getContainer(),
                "Does not have a IOPort as the container of the receiver.");
    }

    /** Return true, indicating that there is always room.
     *  @return True.
     */
    @Override
    public final boolean hasRoom() {
        return true;
    }

    /** Return true, indicating that there is always room for any number
     *  of tokens.
     *  @param tokens The number of tokens, currently ignored.
     *  @return True.
     */
    @Override
    public final boolean hasRoom(int tokens) {
        return true;
    }

    /** Return true if there is at least one token available to the
     *  get() method.
     *  @return True if there are more tokens.
     */
    @Override
    public final boolean hasToken() {
        return !_tokens.isEmpty();
    }

    /** Return true if there are <i>numberOfTokens</i>
     *  tokens available to the get() method.
     *  @param numberOfTokens The number of tokens, currently ignored.
     *  @return True if there are <i>numberOfTokens</i> tokens available.
     */
    @Override
    public final boolean hasToken(int numberOfTokens) {
        return _tokens.size() >= numberOfTokens;
    }

    /** Put a token into this receiver. Note that
     *  this token does not become immediately available to the get() method.
     *  Instead, the token is queued with the director, and the director
     *  must put the token back into this receiver using the _triggerEvent()
     *  protected method in order for the token to become available to
     *  the get() method.  By default, this token will be enqueued by
     *  the director with the default priority -- 5.
     *  However, by setting a <i>priority</i> parameter to the container
     *  of this receiver, or the container's container,
     *  you can enqueue the event with any priority.
     *  This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @param token The token to be put, or null to put no token.
     */
    @Override
    public synchronized void put(Token token) {
        if (token == null) {
            return;
        }
        try {
            IOPort port = getContainer();

            if (port == null) {
                throw new InternalErrorException(
                        "put() requires that the port has a container");
            }

            Parameter priority = (Parameter) port.getAttribute("priority");

            if (priority == null) {
                if (port.getContainer() == null) {
                    throw new InternalErrorException(
                            "put() requires that the port '"
                                    + port
                                    + "' that contains this receiver be itself "
                                    + "contained");
                }

                priority = (Parameter) port.getContainer().getAttribute(
                        "priority");
            }

            int priorityValue = 5;

            if (priority != null) {
                try {
                    priorityValue = ((IntToken) priority.getToken()).intValue();
                } catch (ClassCastException ex) {
                    throw new InternalErrorException(null, ex,
                            "priorityValue '" + priority.getToken()
                                    + "' must be an integer in "
                                    + getContainer());
                }
            }

            getDirector()._enqueueEvent(
                    new TMEvent(this, token, priorityValue, -1.0));
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make a token available to the get() method.
     *  Normally, only a director will call this method.
     *  @param token The token to make available to get().
     */
    protected void _triggerEvent(Token token) {
        _tokens.add(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The director that creates this receiver.
    private TMDirector _director;

    private long _directorVersion = -1;

    // List for storing tokens.  Access with clear(), add(),
    // and take().
    private LinkedList _tokens = new LinkedList();
}
