/* RTOS domain Receiver.

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.NamedObj;

import ptolemy.data.Token;
import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Director;
import ptolemy.data.expr.Parameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;

import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// RTOSReceiver
/**
The receiver for the RTOS domain. This receiver contains a FIFO queue.
Upon receiving a token, it attaches the priority of the token and
queue the event with the director.
The priority of a received token is determined in the following order:
<UL>
<li> If the container of the receiver has a parameter named <i>priority</i>
then the priority equals to the value of the parameter.
<li> If the container of the receiver does not have a <i>priority</i>
parameter, but the actor, which contains the container of this receiver,
has a <i>priority</i> parameter, then the priority equals to the value
of the <i>priority<i> of the actor.
<li> If neither the container nor the container of the container of this
receiver has the <i>priority</i> parameter, then the priority of the
token is the default priority, which is 5.

@author Edward A. Lee, Jie Liu
@version $Id$
*/
public class RTOSReceiver extends AbstractReceiver {

    /** Construct an empty RTOSReceiver with no container.
     */
    public RTOSReceiver() {
	super();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                     public methods                             ////

    /** Get a token from the receiver.  The token returned is one that
     *  was put in the receiver that is ready for process.
     *  Note that there might be multiple such
     *  tokens in the receiver. In that case, FIFO behaviour is used with
     *  respect to the put() method. If there is no such token, throw an
     *  exception. This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @return A token.
     *  @exception NoTokenException If there are no more tokens. This is
     *   a runtime exception, so it need not be declared explicitly.
     */
    public synchronized Token get() throws NoTokenException {
        if(_tokens.isEmpty()) {
            throw new NoTokenException(getContainer(),
                    "No more tokens in the DE receiver.");
        }
        return (Token)_tokens.removeFirst();
    }

    /** Return the director that created this receiver.
     *  If this receiver is an inside receiver of
     *  an output port of an opaque composite actor,
     *  then the director will be the local director
     *  of the container of its port. Otherwise, it's the executive
     *  director of the container of its port. Note that
     *  the director returned is guaranteed to be non-null.
     *  This method is read synchronized on the workspace.
     *  @return An instance of RTOSDirector that creates this receiver.
     *  @exception IllegalActionException If there is no container port, or
     *   if the port has no container actor, or if the actor has no director,
     *   or if the director is not an instance of RTOSDirector.
     */
    public RTOSDirector getDirector() throws IllegalActionException {
        IOPort port = (IOPort)getContainer();
        if (port != null) {
            if (_directorVersion == port.workspace().getVersion()) {
                return _director;
            }
            // Cache is invalid.  Reconstruct it.
            try {
                port.workspace().getReadAccess();
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
                        if (dir instanceof RTOSDirector) {
                            _director = (RTOSDirector)dir;
                            _directorVersion = port.workspace().getVersion();
                            return _director;
                        } else {
                            throw new IllegalActionException(getContainer(),
                                    "Does not have a RTOSDirector.");
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
    public final boolean hasRoom() {
        return true;
    }

    /** Return true if the receiver has room for putting the given number of
     *  tokens into it (via the put() method).
     *  Returning true in this method should also guarantee that calling
     *  the put() method will not result in an exception.
     */
    public boolean hasRoom(int tokens) {
	return true;
    }

    /** Return true if there are tokens available to the get() method.
     *  @return True if there are more tokens.
     */
    public final boolean hasToken() {
        return (!_tokens.isEmpty());
    }
    
    /** Return true if there are <i>numberOfTokens</i>
     *  tokens tokens available to the get() method.
     *  @return True if there are <i>numberOfTokens</i> tokens available.
     */
    public final boolean hasToken(int numberOfTokens) {
        return (_tokens.size() >= numberOfTokens);
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
     *  @param token The token to be put.
     *  @exception NullPointerException If there is no director.
     */
    public synchronized void put(Token token) {
        try {
            IOPort port = getContainer();
            Parameter priority = (Parameter)port.getAttribute("priority");
            if (priority == null) {
                priority = (Parameter)((NamedObj)port.getContainer()).
                    getAttribute("priority");
            }
            int priorityValue = 5;
            if (priority != null) {
                priorityValue = ((IntToken)priority.getToken()).
                    intValue();
            }
            getDirector()._enqueueEvent(new RTOSEvent(this, token, 
                    priorityValue, -1.0));

        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
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
    private RTOSDirector _director;
    private long _directorVersion = -1;

    // List for storing tokens.  Access with clear(), add(),
    // and take().
    private LinkedList _tokens = new LinkedList();
}
