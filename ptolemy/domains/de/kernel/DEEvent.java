/* A representation of events in Ptolemy II DE domain.

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

@ProposedRating red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEEvent
//
/** This class implements the structure of events in Ptolemy II DE domain. 
 *  Conceptually, an event in the Ptolemy II DE domain is an aggregation of
 *  a token and a tag. Actors communicate by sending events to each other.
 *  Thus, it makes sense for the implementation to include the 
 *  destination actor and the destination receiver as fields in the DEEvent
 *  class, as well as the transferred token and the event tag mentioned
 *  previously.
 *  <p>
 *  A pure event is an event
 *  whose values of destination receiver and transferred token are null. This
 *  kind of event is usually used to trigger the activity of an actor at
 *  a specified time in the future.
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 *  @see DEDirector
 */

public class DEEvent {
    /** Construct a DEEvent with the specified destination receiver, the
     *  transferred token, and the event tag. The destination actor is
     *  the one containing the destination receiver.
     *  @param receiver The destination receiver.
     *  @param token The transferred token.
     *  @param tag The event tag.
     */
    DEEvent(DEReceiver receiver, Token token, DEEventTag tag)
            throws IllegalActionException {
        // check the validity of the receiver.
        if(receiver != null) {
            Nameable port = receiver.getContainer();
            if(port != null) {
                _actor = (Actor)port.getContainer();
            }
        }
        if (_actor == null) {
            throw new IllegalActionException(
                    "Attempt to queue an event with an invalid receiver.");
        }
        
        _receiver = receiver;
        _token = token;
        _tag = tag;
    }
    /** Construct a DEEvent with the specified destination actor, and the
     *  event tag. This constructor should be used for constructing
     *  pure event.
     *  @param actor The destination actor
     *  @param tag The event tag
     */
    DEEvent(Actor actor, DEEventTag tag) { _actor = actor; _tag = tag; }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the destination actor of this event.
     *  @return The destination actor
     */
    public final Actor getDestinationActor() {
        return _actor;
    }
    
    /** Return the destination receiver of this event. If the event is a pure
     *  one, then it returns null.
     *  @return The destination receiver
     */
    public final DEReceiver getDestinationReceiver() {
        return _receiver;
    }

    /** Return the token transferred by this event. If the event is a pure
     *  one, then it returns null.
     *  @return The transferred token
     */
    public final Token getTransferredToken() {
        return _token;
    }
    
    /** Return the event tag associated with this event.
     *  @return The event tag
     */
    public final DEEventTag getEventTag() {
        return _tag;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Actor _actor;
    private DEReceiver _receiver;
    private Token _token;
    private DEEventTag _tag;
}
