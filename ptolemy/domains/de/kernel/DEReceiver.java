/* DE domain Receiver.

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
import collections.CollectionEnumeration;

//////////////////////////////////////////////////////////////////////////
//// DEReceiver
/** 
DEReceiver extends the Mailbox receiver by adding the capability to 
interface with the global queue implemented using the CalendarQueue class.
<p>
Before invoking an actor, the director is responsible to put a token into the
receiver.

@author Lukito Muliadi
@version $Id$
*/
public class DEReceiver extends Mailbox {

    /** Construct an empty DEReceiver with the specified director and
     *  no container.
     *  @param director The specified director.
     */
    public DEReceiver(DECQDirector director) {
        super();
        _deDirector = director;
    }

    /** Construct an empty DEReceiver with the specified director and
     *  container.
     *  @param container The specified container.
     *  @param director The specified director.
     */
    public DEReceiver(IOPort container, DECQDirector director) {
        super(container);
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
                new DESortKey(timeStamp, _fineLevel));
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

    /** Invoke the put method from the parent class, which will manually
     *  put a token into the receiver. Only a director should use this 
     *  method.
     */
    public void superPut(Token token) throws IllegalActionException{
        super.put(token);
    }

    /** Set the fine level of this receiver, obtained from the topological
     *  sort.
     */
    public void setFineLevel(int fineLevel) {
        _fineLevel = fineLevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // _deDirector: the director that created this receiver.
    DECQDirector _deDirector = null;
    // _actor: the actor that contains this receiver.
    Actor _actor = null;
    // _fineLevel: The fine level associated with this receiver.
    int _fineLevel;
}









