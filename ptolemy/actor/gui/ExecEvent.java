/* An event that indicates that an actor is in a particular state.

Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// ExecEvent
/**
An event that indicates that an actor is in a particular state.
The event contains two pieces of information:  the actor that
generated the event and the (integer) state of the actor when
the event was generated. A class that listens for ExecEvents
must implement the ExecEventListener interface.

The interpretation of state is determined by the actors and
listeners that use the ExecEvent class and ExecEventListener
interface. The author of code that takes advantage of these
facilities should write the actors and listeners so that they
utilize a common meaning for the integer states.

@author Mudit Goel, John S. Davis II
@version $Id$
*/

public class ExecEvent {

    /** Create an ExecEvent with the specified actor and state.
     *  @param actor The actor that generated the event.
     *  @param actor The state of the actor when the event is
     *   is generated.
     */
    public ExecEvent(Actor actor, int state) {
        _actor = actor;
        _state = state;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public methods                   /////

    /** Return the actor corresponding to the event.
     *  @return The actor that generated this event.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Return the state of the actor when the event
     *  was generated.
     *  @return The state of the actor when the event
     *   was generated.
     */
    public int getCurrentState() {
        return _state;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private variables                 /////

    private int _state = 0;
    private Actor _actor = null;

}
