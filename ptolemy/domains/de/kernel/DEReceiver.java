/* Discrete Event (DE) domain Receiver.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
/** An implementation of Receiver interface for DE domain. In DE domain
 *  receiver, the put() method is overloaded with one more argument
 *  representing time stamp. DEReceiver can contain any number of tokens
 *  corresponding to simultaneous events. It also has a 'depth' field
 *  indicating its depth in the topology.
 *  <p>
 *  Before firing an actor, the director is responsible to put at least one
 *  token into at least one of the receivers contained by the actor.

@author Lukito Muliadi
@version $Id$
*/
public class DEReceiver implements Receiver {

    /** Construct an empty DEReceiver with the specified director and
     *  no container.
     *  @param director The specified director.
     */
    public DEReceiver(DECQDirector director) {
        _deDirector = director;
    }

    /** Construct an empty DEReceiver with the specified director and
     *  container.
     *  @param container The specified container.
     *  @param director The specified director.
     */
    public DEReceiver(IOPort container, DECQDirector director) {
        _actor = (Actor)(container.getContainer());
        // FIXME: how to set the director ???
        _deDirector = (DECQDirector)(_actor.getDirector());
        if (_deDirector != director) {
            _deDirector = director;
            throw new InvalidStateException("DEReceiver, invalid topology.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Get the contained tokens one by one.  If there is no more, throw an
     *  exception.
     *  <p>
     *  Tokens contained by this receiver will all have the same
     *  time stamps. During one firing phase of an actor, this method could
     *  be called several times until the receiver is empty.
     *
     *  @return A token.
     *  @exception NoSuchItemException If there is no more tokens.
     */
    public Token get() throws NoSuchItemException {
        if(_tokens.isEmpty()) {
            throw new NoSuchItemException(getContainer(),
                    "No more tokens in the DE receiver.");
        }
        _dirty = true;
        return (Token)_tokens.take();
    }

    /** Return the container.
     *  @return The container.
     */
    public Nameable getContainer() {
        return _container;
    }
    /** Return true if there are more tokens.
     *  @return True if there are more tokens.
     */
    public boolean hasToken() {
        return (!_tokens.isEmpty());
    }

    /** Always return true because the capacity is assumed to be unbounded.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Set the container.
     *
     *  @param port The IOPort containing this receiver.
     */
    public void setContainer(IOPort port) {
        _container = port;
    }

    /** Put a token and its time stamp into the receiver. Actor that produce
     *  delayed outputs should use this method.
     *
     * @param token The token being put.
     * @param timeStamp The time stamp of the token.
     *
     */
    public void put(Token token, double timeStamp)
            throws IllegalActionException {

        // First check if _actor field is already set.
        // If not, then ask the port containing this object for its
        // container.
        if (_actor == null) {
            _actor = (Actor)getContainer().getContainer();
        }
        // If _actor still null, then the topology is invalid.
        if (_actor == null) {
            throw new IllegalStateException("In DEReceiver, _actor is null");
        }
        // Enqueue the actor, the receiver and the token, respectively.
        _deDirector.enqueueEvent(_actor,
                this,
                token,
                new DESortKey(timeStamp, _depth));
    }

    /** Put a token into the receiver with its time stamp equal to the current
     *  time obtained from the director. Actor that produce zero-delay outputs
     *  and polymorphic actors should use this method.
     *
     * @param token The token being put.
     */
    public void put(Token token) throws IllegalActionException{
        put(token, _deDirector.currentTime());
    }

    /** Trigger an event by inserting its corresponding token into the
     *  receiver. Only a director should use this method.
     *
     *  @param token The token triggered.
     */
    public void triggerEvent(Token token) throws IllegalActionException{
        if (_dirty) {
            _tokens.clear();
            _dirty = false;
        }
        _tokens.insertFirst(token);
    }

    /** Set the depth of this receiver, obtained from the topological
     *  sort.
     */
    public void setDepth(long depth) {
        _depth = depth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // _deDirector: the director that created this receiver.
    DECQDirector _deDirector = null;
    // _actor: the actor that contains this receiver.
    Actor _actor = null;
    // _depth: The topological depth associated with this receiver.
    long _depth;

    // _container
    private IOPort _container = null;

    // clear(), insertFirst(), take().
    private LinkedList _tokens = new LinkedList();

    // If _dirty is set (true) then the _tokens LinkedList will be cleared
    // at the next invocation of superPut().
    boolean _dirty = true;
}









