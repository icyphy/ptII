/* Discrete Event (DE) domain receiver.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// DEReceiver

/** An implementation of the ptolemy.actor.Receiver interface for the
    DE domain. Tokens that are put into this receiver logically have time
    stamps. The put() method sends the specified token to the
    director, which returns it to this receiver (via the protected method
    _triggerEvent()) when current time matches the time stamp of the
    token. The get() method returns only tokens that the director has so
    returned. Thus, when a token is put into the receiver using put(), it
    does not become immediately available to the get() method.

    <p>By default, the time stamp of a token is the current model time of the
    director when put() is called. This should be done in a synchronized manner, 
    since there could be multiple thread running in this domain.

    <p>Before firing an actor, the director is expected to put at least one
    token into at least one of the receivers contained by the actor.

    @author Lukito Muliadi, Edward A. Lee, Jie Liu
    @version $Id$
    @since Ptolemy II 0.2
    @Pt.ProposedRating Green (liuj)
    @Pt.AcceptedRating Green (cxh)
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
     *   not accept this receiver.
     */
    public DEReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained tokens.
     */
    public void clear() {
        _tokens.clear();
    }

    /** Get the first token from the receiver. The token returned is one that
     *  was put in the receiver with a time stamp equal to or earlier than
     *  the current time.  Note that there might be multiple such
     *  tokens in the receiver. In that case, FIFO behaviour is used with
     *  respect to the put() method. If there is no such token, throw an
     *  exception. This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @return A token.
     *  @exception NoTokenException If there are no more tokens. This is
     *   a runtime exception, so it need not to be declared explicitly.
     */
    public synchronized Token get() throws NoTokenException {
        if (_tokens.isEmpty()) {
            throw new NoTokenException(getContainer(),
                    "No more tokens in the DE receiver.");
        }
        return (Token)_tokens.removeFirst();
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
     *  @return True.
     */
    public boolean hasRoom(int tokens) {
        return true;
    }

    /** Return true if there is at least one token available to the 
     *  get() method.
     *  @return True if there are more tokens.
     */
    public boolean hasToken() {
        return (!_tokens.isEmpty());
    }

    /** Return true if there are <i>numberOfTokens</i>
     *  tokens tokens available to the get() method.
     *  @param numberOfTokens An int indicating how many tokens are needed.
     *  @return True if there are numberOfTokens tokens available.
     */
    public boolean hasToken(int numberOfTokens) {
        return (_tokens.size() >= numberOfTokens);
    }

    /** Put a token into this receiver. Note that
     *  this token does not become immediately available to the get() method.
     *  Instead, the token is queued with the director, and the director
     *  must put the token back into this receiver using the protected method
     *  _triggerEvent() in order for the token to become available to
     *  the get() method. This token will be enqueued by
     *  the director with the current model time of the director.  
     *  This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @param token The token to be put.
     */
    public synchronized void put(Token token) {
        try {
            DEDirector dir = _getDirector();
            dir._enqueueEvent(this, token, dir.getModelTime());
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex, null);
        }
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
    ////                         private methods                  ////

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
                        if (dir instanceof DEDirector) {
                            _director = (DEDirector)dir;
                            _directorVersion = port.workspace().getVersion();
                            return _director;
                        } else {
                            throw new IllegalActionException(getContainer(),
                                    "Does not have a DEDirector.");
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director where this DEReceiver should register for De events.
    private DEDirector _director;
    private long _directorVersion = -1;

    // List for storing tokens.  Access with clear(), add(), and take().
    private LinkedList _tokens = new LinkedList();
}
