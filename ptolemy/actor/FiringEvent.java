/* An event that represents an actor activation.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// FiringEvent
/**
An event that is published by directors whenever an actor is activated.
An activation occurs whenever an actor is prefired, fired, or postfired.
The event of the same name should be published just before the associated
method of the executable interface is called.  The postpostfire event is
published just after the postfire method is called.  The iterate event
is published by those directors which vectorize firings of a
particular actor.  This event may represent many individual calls
to prefire, fire and postfire.   As an example of
how to implement a director that publishes these events, see the SDF Director.
One way in which these events are used is to trace the firings of different
actors.  A user interface can implement a breakpoint mechanism by
pausing execution of the executing thread in response to one of these events.

@author  Steve Neuendorffer
@version $Id$
@see ptolemy.kernel.util.DebugListener
*/
public class FiringEvent implements DebugEvent {

    /**
     * Create a new firing event with the given source, actor, and type.
     */
    public FiringEvent(Director source, Actor actor, FiringEventType type) {
	_director = source;
	_actor = actor;
	_type = type;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor that is being activated. */
    public Actor getActor() {
	return _actor;
    }

    /** Return the source of the event.  This class returns the director
     *  that activated the actor.
     *  @return An instance of Director.
     */
    public NamedObj getSource() {
	return _director;
    }

    /** Return the type of activation that this event represents. */
    public FiringEventType getType() {
	return _type;
    }

    /** Return a string representation of this event.
     *  @return A user-readable string describing the event.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("The actor ");
        buffer.append(((NamedObj)_actor).getFullName());
        buffer.append(" was ");
        buffer.append(_type.getName());
        buffer.append("d.");
        return buffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final FiringEventType PREFIRE =
    new FiringEventType("prefire");
    public static final FiringEventType FIRE =
    new FiringEventType("fire");
    public static final FiringEventType POSTFIRE =
    new FiringEventType("postfire");
    public static final FiringEventType POSTPOSTFIRE =
    new FiringEventType("postpostfire");
    public static final FiringEventType ITERATE =
    new FiringEventType("iterate");

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Actor _actor;
    private NamedObj _director;
    private FiringEventType _type;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public static class FiringEventType {
	private FiringEventType(String name) {
	    _name = name;
	}

	public String getName() {
	    return _name;
	}
	private String _name;
    }
}
