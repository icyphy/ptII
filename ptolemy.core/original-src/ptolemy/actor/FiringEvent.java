/* An event that represents an actor activation.

 Copyright (c) 2000-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
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

    /** Return the actor that is being activated.
     *  @return The actor that is being activated.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Return the director that activated the actor.
     *  @return The director that activated the actor.
     */
    public Director getDirector() {
        return _director;
    }

    /** Return the source of the event.  This class returns the director
     *  that activated the actor.
     *  @return An instance of Director.
     */
    @Override
    public NamedObj getSource() {
        return _director;
    }

    /** Return the type of activation that this event represents.
     *  @return the type of activation that this event represents.
     */
    public FiringEventType getType() {
        return _type;
    }

    /** Return a string representation of this event.
     *  @return A user-readable string describing the event.
     */
    @Override
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
            "prefired", false);

    /** This type of event is published after a fire method is called. */
    public static final FiringEventType AFTER_FIRE = new FiringEventType(
            "fired", false);

    /** This type of event is published after a postfire method is called. */
    public static final FiringEventType AFTER_POSTFIRE = new FiringEventType(
            "postfired", false);

    /** This type of event is published after an iterate method is called. */
    public static final FiringEventType AFTER_ITERATE = new FiringEventType(
            "iterated", false);

    /** This type of event is published before a prefire method is called. */
    public static final FiringEventType BEFORE_PREFIRE = new FiringEventType(
            "prefired", true);

    /** This type of event is published before a fire method is called. */
    public static final FiringEventType BEFORE_FIRE = new FiringEventType(
            "fired", true);

    /** This type of event is published before a postfire method is called. */
    public static final FiringEventType BEFORE_POSTFIRE = new FiringEventType(
            "postfired", true);

    /** This type of event is published before an iterate method is called. */
    public static final FiringEventType BEFORE_ITERATE = new FiringEventType(
            "iterated", true);

    // FIXME: Kepler should be fixed so that these are not necessary.

    /** The type of event published as part of the Kepler sql actor. */
    public static final FiringEventType BEFORE_RW_FIRE = new FiringEventType(
            "rw fired", true);

    /** The type of event published as part of the Kepler sql actor. */
    public static final FiringEventType AFTER_RW_FIRE = new FiringEventType(
            "rw fired", false);

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
        /** Create a new event type with the given name.
         *  @param name The name of this event type.
         *  @param isStart true if this event type is a start event,
         *  false if it is an end event.
         */
        private FiringEventType(String name, boolean isStart) {
            _isStart = isStart;
            _name = name;
        }

        /** Return the string name of this event type.
         *  @return the string name of this event type.
         */
        public String getName() {
            if (_isStart) {
                return "will be " + _name;
            } else {
                return "was " + _name;
            }
        }

        /** Return a string description of this event type.
         *  @return a string description of this event type.
         */
        @Override
        public String toString() {
            return "FiringEventType(" + getName() + ")";
        }

        /** Return true if this event corresponds with a start event.
         *  @return true if this event corresponds with a start event.
         */
        public boolean isStart() {
            return _isStart;
        }

        /** Return the name of this event type.
         *  @return the name of this event type.
         */
        public String getTypeName() {
            return _name;
        }

        // The name of this event type.
        private String _name;

        // True if start of this event type.
        private boolean _isStart;
    }
}
