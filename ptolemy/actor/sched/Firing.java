/* A schedule element that contains a reference to an actor.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (vogel@eecs.berkeley.edu)
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.actor.sched;

import ptolemy.actor.Actor;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// Firing
/**
This class is a schedule element that contains a reference to an
actor.  This class is used together with the Schedule class to
construct a static schedule.  This class contains a reference to an
actor, and is used to represent an actor term of a schedule loop. The
setActor() method is used to create the reference to an actor. The
getActor() method will return a reference to this actor. <p>

It is more efficient to use this class than to simply maintain a list of
actors since actors will often firing multiple times consecutively.  Using
this class (and the Schedule data structure in general) greatly reduces the
memory requirements of most large schedules.

@author Brian K. Vogel, Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.actor.sched.Schedule
@see ptolemy.actor.sched.ScheduleElement
*/

public class Firing extends ScheduleElement {
    /** Construct a firing with a default iteration count equal to one
     *  and with no parent schedule.
     */
    public Firing() {
        super();
    }

    /** Construct a firing with a actor, an iteration count equal to one
     *  and no parent schedule.
     *  @param actor The actor in the firing.
     */
    public Firing(Actor actor) {
        super();
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the actor invocation sequence of the schedule in the
     *  form of a sequence of actors. For a valid schedule, all of the
     *  lowest-level nodes should be an instance of Firing. If the
     *  schedule is not valid, then the returned iterator will contain
     *  null elements.
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *
     *  @return An iterator over a sequence of actors.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    public Iterator actorIterator() {
        // As of 8/02, it seems like this method class is not really
        // used except by the test suite.
        return new ActorIterator(getIterationCount());
    }

    /** Return the actor invocation sequence in the form
     *  of a sequence of firings.
     *  Since this ScheduleElement is a Firing, the
     *  iterator returned will contain exactly one Firing (this Firing).
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *
     *  @return An iterator over a sequence of firings.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    public Iterator firingIterator() {
        // FIXME: a ConcurrentModificationException will not necessarily
        // be thrown, see the failing tests.
        if (_firing == null) {
            _firing = new LinkedList();
            _firing.add(this);
        }
        return _firing.iterator();
    }

    /** Get the actor associated with this Firing. The setActor()
     *  method is used to set the actor that this method returns.
     *  If setActor() was never called, then throw an exception.
     *
     * @return The actor associated with this Firing.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Set the actor associated with this firing. This actor will
     *  then be returned when the getActor() method is invoked. If this
     *  firing already contains a reference to an actor, then the
     *  reference will overwritten.
     *
     *  @param actor The actor to associate with this firing.
     */
    public void setActor(Actor actor) {
        _incrementVersion();
        _actor  = actor;
        if (_firing != null) {
            _firing.clear();
            _firing.add(this);
        }
    }

    /**
     * Output a string representation of this Firing.
     */
    public String toString() {
        String result = "Fire Actor " + _actor;
        int iterationCount = getIterationCount();
        if (iterationCount > 1)
            result += " " + iterationCount + " times";
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An adapter class for iterating over the elements of this
     *  schedule. An exception is thrown if the schedule structure
     *  changes while this iterator is active.
     */
    private class ActorIterator implements Iterator {
        // As of 8/02, it seems like this inner class is not really
        // used except by the test suite.
        /** Construct a ScheduleIterator.
         */
        public ActorIterator(int iterationCount) {
            _startingVersion = _getVersion();
            _currentElement = 0;
            _iterationCount = iterationCount;
        }

        /** Return true if the iteration has more elements.
         *
         *  @exception ConcurrentModificationException If the schedule
         *   data structure has changed since this iterator
         *   was created.
         *  @return true if the iterator has more elements.
         */
        public boolean hasNext() {
            if (_startingVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else {
                return(_currentElement <= _iterationCount);
            }
        }

        /** Return the next object in the iteration.
         *
         *  @exception InvalidStateException If the schedule
         *   data structure has changed since this iterator
         *   was created.
         *  @return the next object in the iteration.
         */
        public Object next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("No element to return.");
            } else {
                _currentElement++;
                return getActor();
            }
        }

        /** Throw an exception, since removal is not allowed. It really
         *  doesn't make sense to remove an actor from an actor invocation
         *  sequence anyway.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private long _startingVersion;
        private int _currentElement;
        private int _iterationCount;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor associated with this firing.
    private Actor _actor;
    // The list containing this firing as the only element.
    private List _firing = null;
}
