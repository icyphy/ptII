/* An event that represents an actor activation.

Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// FiringEvent

/**
   An event that is published by directors whenever an actor is activated.
   An activation occurs whenever an actor is prefired, fired, or
   postfired.  The appropriate event should be published just before
   or after the associated method of the executable interface is
   called.  The iterate event is published by those directors which
   vectorize firings of a particular actor.  This event may be
   published instead of many individual prefire, fire and postfire
   events.  As an example of how to implement a director that
   publishes these events, see the SDF Director.
   One way in which these events are used is to trace the firings of
   different actors.  A user interface can also implement a breakpoint
   mechanism by pausing execution of the executing thread in response
   to one of these events.

   <p>
   Note that since most directors work with a constant set of actors, and fire
   them repeatedly, it may improve efficiency dramatically to use a
   flyweight design pattern with firing events.  This can result in greatly
   reducing the load on the garbage collector.

   @author  Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Green (neuendor)
   @Pt.AcceptedRating Yellow (neuendor)
   @see ptolemy.kernel.util.DebugListener
*/
public class FiringEvent implements DebugEvent {
    /** Create a new firing event with the given source, actor, and type.
     *  @param source The director invoking the firing.
     *  @param actor The actor being fired.
     *  @param type An identifier for the method being invoked, which is
     *   one of AFTER_PREFIRE, AFTER_FIRE, AFTER_POSTFIRE, AFTER_ITERATE,
     *   BEFORE_PREFIRE, BEFORE_FIRE, BEFORE_POSTFIRE, or BEFORE_ITERATE.
     */
    public FiringEvent(Director source, Actor actor, FiringEventType type) {
        _director = source;
        _actor = actor;
        _type = type;
    }

    /** Create a new firing event with the given source, actor, type,
     *  and multiplicity.
     *  @param source The director invoking the firing.
     *  @param actor The actor being fired.
     *  @param type An identifier for the method being invoked, which is
     *   one of AFTER_PREFIRE, AFTER_FIRE, AFTER_POSTFIRE, AFTER_ITERATE,
     *   BEFORE_PREFIRE, BEFORE_FIRE, BEFORE_POSTFIRE, or BEFORE_ITERATE.
     *  @param multiplicity The multiplicity of the firing.
     *
     */
    public FiringEvent(Director source, Actor actor, FiringEventType type,
        int multiplicity) {
        _director = source;
        _actor = actor;
        _type = type;
        _multiplicity = multiplicity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor that is being activated. */
    public Actor getActor() {
        return _actor;
    }

    /** Return the director that activated the actor.
     */
    public Director getDirector() {
        return _director;
    }

    /** Return the source of the event.  This class returns the director
     *  that activated the actor.
     *  @return An instance of Director.
     */
    public NamedObj getSource() {
        return _director;
    }

    /** Return the type of activation that this event represents.
     */
    public FiringEventType getType() {
        return _type;
    }

    /** Return a string representation of this event.
     *  @return A user-readable string describing the event.
     */
    public String toString() {
        // Note that a string buffer is used to reduce the overhead.
        // Additionally it may be useful to cache this string, but since
        // in current cases we only ever call this method once, lets
        // not bother.
        StringBuffer buffer = new StringBuffer();
        buffer.append("The actor ");
        buffer.append(((NamedObj) _actor).getFullName());
        buffer.append(" ");
        buffer.append(_type.getName());

        if (_multiplicity > 1) {
            buffer.append(" ");
            buffer.append(_multiplicity);
            buffer.append(" times");
        }

        buffer.append(".");
        return buffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** This type of event is published after a prefire method is called. */
    public static final FiringEventType AFTER_PREFIRE = new FiringEventType(
            "was prefired");

    /** This type of event is published after a fire method is called. */
    public static final FiringEventType AFTER_FIRE = new FiringEventType(
            "was fired");

    /** This type of event is published after a postfire method is called. */
    public static final FiringEventType AFTER_POSTFIRE = new FiringEventType(
            "was postfired");

    /** This type of event is published after an iterate method is called. */
    public static final FiringEventType AFTER_ITERATE = new FiringEventType(
            "was iterated");

    /** This type of event is published before a prefire method is called. */
    public static final FiringEventType BEFORE_PREFIRE = new FiringEventType(
            "will be prefired");

    /** This type of event is published before a fire method is called. */
    public static final FiringEventType BEFORE_FIRE = new FiringEventType(
            "will be fired");

    /** This type of event is published before a postfire method is called. */
    public static final FiringEventType BEFORE_POSTFIRE = new FiringEventType(
            "will be postfired");

    /** This type of event is published before an iterate method is called. */
    public static final FiringEventType BEFORE_ITERATE = new FiringEventType(
            "will be iterated");

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The actor that was activated.
    private Actor _actor;

    // The director that activated the actor.
    private Director _director;

    // The multiplicity.
    private int _multiplicity = 1;

    // The type of activation.
    private FiringEventType _type;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A type of firing event that can be published.  This class implements
     *  a type-safe enumeration.  The constructor is private so that the
     *  only admissible event types are the static members of the
     *  FiringEvent class.
     */
    public static class FiringEventType {
        // Create a new event type with the given name.
        private FiringEventType(String name) {
            _name = name;
        }

        /** Return the string name of this event type. */
        public String getName() {
            return _name;
        }

        /** Return a string description of this event type. */
        public String toString() {
            return "FiringEventType(" + _name + ")";
        }

        // The name of this event type.
        private String _name;
    }
}
