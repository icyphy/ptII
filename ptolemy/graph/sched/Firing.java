/* A schedule element that contains a reference to a firing element.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.graph.sched;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ptolemy.kernel.util.InvalidStateException;

///////////////////////////////////////////////////////////////////
//// Firing

/**
 This class is a ScheduleElement that contains a reference to a
 firing element.  The firingElement could be any Object.
 This class is used together with the Schedule class to
 construct a Schedule. The setFiringElement() method is used to create the
 reference to a firing element. The getFiringElement() method will return a
 reference to this firing element.
 <p>

 It is more efficient to use this class than to simply maintain a list of
 firing elements since firing elements will often firing multiple times
 consecutively.  Using
 this class (and the Schedule data structure in general) greatly reduces the
 memory requirements of most large schedules.

 @author Shahrooz Shahparnia, Mingyung Ko,
 University of Maryland at College Park based on a file by
 Brian K. Vogel, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating red (shahrooz)
 @Pt.AcceptedRating red (ssb)
 @see ptolemy.graph.sched.Firing
 @see ptolemy.graph.sched.Schedule
 @see ptolemy.graph.sched.ScheduleElement
 */
public class Firing extends ScheduleElement {
    /** Construct a firing with a default iteration count equal to one
     *  and with no parent schedule.
     */
    public Firing() {
        super();
    }

    /** Construct a firing with a firingElement, an iteration count equal to one
     *  and no parent schedule. A Firing constructed using this constructor,
     *  using the setFiringElement() method, will only accept firing elements
     *  with the same class type as the given firingElement, using the
     *  setFiringElement() method.
     *  @param firingElement The firing element in the firing.
     */
    public Firing(Object firingElement) {
        super(firingElement.getClass());
        _firingElement = firingElement;
    }

    /** Construct a firing with a given firing element type, an iteration
     *  count equal to one and no parent schedule.
     *  In a Firing constructed using this constructor, the
     *  setFiringElement() method, will only accept firing elements
     *  with the same given class type, using the setFiringElement() method.
     *  @param firingElementClass The class of the firing element in the firing.
     */
    public Firing(Class firingElementClass) {
        super(firingElementClass);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the firing element invocation sequence of the schedule in the
     *  form of a sequence of firing elements. For a valid schedule, all of the
     *  lowest-level nodes should be an instance of Firing. If the
     *  schedule is not valid, then the returned iterator will contain
     *  null elements.
     *  <p>
     *  A runtime exception is thrown if the underlying schedule structure
     *  is modified while the iterator is active.
     *
     *  @return An iterator over a sequence of firing elements.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    @Override
    public Iterator firingElementIterator() {
        return new FiringElementIterator();
    }

    /** Return the firing element invocation sequence in the form
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
     *  underlying schedule structure is modified while the iterator
     *  is active.
     */
    @Override
    public Iterator firingIterator() {
        // FIXME: a ConcurrentModificationException will not necessarily
        // be thrown, see the failing tests.
        if (_firing == null) {
            _firing = new LinkedList();
            _firing.add(this);
        }

        return _firing.iterator();
    }

    /** Get the firing element associated with this Firing. The
     *  setFiringElement() method is used to set the firing element that this
     *  method returns.
     *  If setFiringElement() was never called, then throw an exception.
     *
     *  @return The actor associated with this Firing.
     *  @see #setFiringElement(Object)
     */
    public Object getFiringElement() {
        // FIXME: the exception is never thrown in the original version
        return _firingElement;
    }

    /** Set the firing element associated with this firing. This firing element
     *  will then be returned when the getFiringElement() method is invoked.
     *  If this firing already contains a reference to a firing element,
     *  then the reference will overwritten.
     *
     *  @param firingElement The firing element to associate with this firing.
     *  @see #getFiringElement()
     */
    public void setFiringElement(Object firingElement) {
        if (this.firingElementClass() != null) {
            if (this.firingElementClass().isAssignableFrom(
                    firingElement.getClass())) {
                _incrementVersion();
                _firingElement = firingElement;

                if (_firing != null) {
                    _firing.clear();
                    _firing.add(this);
                }
            } else {
                throw new RuntimeException("Attempt to add a non "
                        + "authorized firing element");
            }
        } else {
            _incrementVersion();
            _firingElement = firingElement;

            if (_firing != null) {
                _firing.clear();
                _firing.add(this);
            }
        }
    }

    /** Print the firing in a parenthesis style.
     *
     *  @param nameMap A mapping from firing element to its short name.
     *  @param delimiter The delimiter between iteration count and iterand.
     *  @return The parenthesis expression for this firing.
     */
    @Override
    public String toParenthesisString(Map nameMap, String delimiter) {
        String name = (String) nameMap.get(getFiringElement());
        int iterations = getIterationCount();

        if (iterations > 1) {
            return "(" + iterations + delimiter + name + ")";
        } else {
            return name;
        }
    }

    /** Return a string representation of this Firing.
     *
     *  @return Return a string representation of this Firing.
     */
    @Override
    public String toString() {
        String result = "Fire firing element " + _firingElement;

        if (getIterationCount() > 1) {
            result += " " + getIterationCount() + " times";
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An adapter class for iterating over the elements of this
     *  schedule. An exception is thrown if the schedule structure
     *  changes while this iterator is active.
     */
    private class FiringElementIterator implements Iterator {
        // As of 8/02, it seems like this inner class is not really
        // used except by the test suite.

        /** Construct a ScheduleIterator.
         */
        public FiringElementIterator() {
            _startingVersion = _getVersion();
            _currentElement = 0;
        }

        /** Return true if the iteration has more elements.
         *  @exception ConcurrentModificationException If the schedule
         *  data structure has changed since this iterator
         *  was created.
         *  @return True if the iterator has more elements.
         */
        @Override
        public boolean hasNext() {
            if (_startingVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else {
                return _currentElement <= getIterationCount();
            }
        }

        /** Return the next object in the iteration.
         *
         *  @exception InvalidStateException If the schedule
         *  data structure has changed since this iterator
         *  was created.
         *  @return The next object in the iteration.
         */
        @Override
        public Object next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("No element to return.");
            } else if (_startingVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else {
                _currentElement++;
                return getFiringElement();
            }
        }

        /** Throw an exception, since removal is not allowed. It really
         *  doesn't make sense to remove an actor from an actor invocation
         *  sequence anyway.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private long _startingVersion;

        private int _currentElement;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The firing element associated with this firing.
    private Object _firingElement;

    // The list containing this firing as the only element.
    private List _firing = null;
}
