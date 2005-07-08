/* A count and a list of schedule elements.

 Copyright (c) 2003-2005 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// Schedule

/**
 This class represents a static schedule of firing elements invocation.  An
 instance of this class is returned by the scheduler of a model to
 represent order of firing element firings in the model.  A schedule consists of
 a list of schedule elements and the number of times the schedule
 should repeat (called the <i>iteration count</i>). <p>

 Each element of
 the schedule is represented by an instance of the ScheduleElement
 class.  Each element may correspond to a number of firings of a single
 firing element (represented by the Firing class) or an entire sub-schedule
 (represented by a hierarchically contained instance of this class).
 This nesting allows this concise representation of looped schedules.
 The nesting can be arbitrarily deep, but must be a tree where the
 leaf nodes represent firings.  It is up to the scheduler to
 enforce this requirement. <p>

 The add() and remove() methods are used to add or
 remove schedule elements. Only elements of type ScheduleElement (Schedule
 or Firing) may be added to the schedule list.

 The iteration count is set by the
 setIterationCount() method. If this method is not invoked, a default value
 of one will be used. <p>

 As an example, suppose that we have an SDF graph containing actors
 A, B, C, and D, with the firing order ABCBCBCDD.
 This firing order can be represented by a simple looped schedule.
 The code to create this schedule appears below.
 <p>
 <pre>
 *       Schedule S = new Schedule();
 *       Firing S1 = new Firing();
 *       Schedule S2 = new Schedule();
 *       Firing S3 = new Firing();
 *       S.add(S1);
 *       S.add(S2);
 *       S.add(S3);
 *       S1.setFiringElement(A);
 *       S2.setIterationCount(3);
 *       Firing S2_1 = new Firing();
 *       Firing S2_2 = new Firing();
 *       S2_1.setFiringElement(B);
 *       S2_2.setFiringElement(C);
 *       S2.add(S2_1);
 *       S2.add(S2_2);
 *       S3.setIterationCount(2);
 *       S3.setFiringElement(D);
 </pre>
 <p>

 Note that this implementation is not synchronized. It is therefore not safe
 for a thread to make modifications to the schedule structure while
 multiple threads are concurrently accessing the schedule.
 <h1>References</h1>
 S. S. Bhattacharyya, P K. Murthy, and E. A. Lee,
 Software Syntheses from Dataflow Graphs, Kluwer Academic Publishers, 1996.

 @since Ptolemy II 4.0
 @Pt.ProposedRating red (shahrooz)
 @Pt.AcceptedRating red (ssb)
 @author Shahrooz Shahparnia, Mingyung Ko,
 University of Maryland at College Park, based on a file by
 Brian K. Vogel, Steve Neuendorffer
 @version $Id$
 @see ptolemy.graph.sched.Firing
 @see ptolemy.graph.sched.Schedule
 @see ptolemy.graph.sched.ScheduleElement
 */
public class Schedule extends ScheduleElement {
    /** Construct a schedule with iteration count of one and an
     *  empty schedule list. This constructor should be used when
     *  creating a root schedule.
     */
    public Schedule() {
        super();

        // This list will contain the schedule elements.
        _schedule = new LinkedList();

        //_firingIteratorVersion = 0;
        // Default tree depth to use for allocation state arrays
        // for the firingIterator() method. The arrays will be
        // dynamically resized as needed. 3 was an arbitrary
        // choice. Any positive integer will do. The arrays are
        // only resized while iterating through the first iterator
        // that was created since the schedule was modified.
        _treeDepth = 3;
    }

    /** Construct a schedule with iteration count of one and an
     *  empty schedule list. This constructor should be used when
     *  creating a root schedule. If this constructor is used,
     *  {@link ScheduleElement}s containing firing elements of the given class
     *  would only be accepted as next elements for this schedule.
     *
     *  @param firingElementClass The given class type.
     */
    public Schedule(Class firingElementClass) {
        super(firingElementClass);

        // This list will contain the schedule elements.
        _schedule = new LinkedList();

        //_firingIteratorVersion = 0;
        // Default tree depth to use for allocation state arrays
        // for the firingIterator() method. The arrays will be
        // dynamically resized as needed. 3 was an arbitrary
        // choice. Any positive integer will do. The arrays are
        // only resized while iterating through the first iterator
        // that was created since the schedule was modified.
        _treeDepth = 3;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the specified schedule element to the end of the schedule
     *  list. This element must be an instance of ScheduleElement.
     *
     *  @param element The schedule element to add.
     */
    public void add(ScheduleElement element) {
        add(_schedule.size(), element);
    }

    /** Insert the specified schedule element at the specified position in
     *  the schedule list. This element must be an instance of
     *  ScheduleElement. An exception is thrown if the index is out of
     *  range.
     *
     *  @param index The index at which the specified element is to be
     *   inserted.
     *  @param element The schedule element to add.
     *  @exception IndexOutOfBoundsException If the specified index is out of
     *   range (index < 0 || index > size()).
     */
    public void add(int index, ScheduleElement element) {
        // Give element a reference to this schedule so that it can
        // notify this schedule (via _incrementVersions()) when
        // element is modified.
        if (this.firingElementClass() != null) {
            Class firingElementClass = this.firingElementClass();
            Class elementClass = element.firingElementClass();

            if (elementClass != null) {
                if (firingElementClass.isAssignableFrom(elementClass)) {
                    element.setParent(this);
                    _incrementVersion();
                    _schedule.add(index, element);
                } else {
                    throw new RuntimeException("Attempt to add a non "
                            + "authorized firing element");
                }
            } else {
                throw new RuntimeException("Attempt to add a non "
                        + "authorized firing element");
            }
        } else {
            element.setParent(this);
            _incrementVersion();
            _schedule.add(index, element);
        }
    }

    /** The number of times the given firing element appears in the
     *  schedule. Generally, firing elements represent program blocks.
     *  Appearances of elements indicate how many copies of codes are
     *  in the schedule. The more appearances, the larger program
     *  space is required.
     *
     *  @param firingElement The given firing element.
     *  @return The number of appearances.
     */
    public int appearanceCount(Object firingElement) {
        return firings(firingElement).size();
    }

    /** Return the firing element invocation sequence of the schedule in the
     *  form of a sequence of firing elements. For a valid schedule, all of the
     *  lowest-level nodes should be an instance of Firing. If the
     *  schedule is not valid, then the returned iterator will contain
     *  null elements.
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *
     *  @return An iterator over a sequence of firing elements.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    public Iterator firingElementIterator() {
        return new FiringElementIterator();
    }

    /** Return the Firing invocation sequence of this schedule in the form
     *  of a sequence of firings. All of the lowest-level nodes of the
     *  schedule should be an instance of Firing. Otherwise an exception will
     *  occur at some point in the iteration.
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *  <p>
     *  Implementation note: This method is optimized to be memory
     *  efficient. It walks the schedule tree structure as the
     *  iterator methods are invoked.
     *
     *  @return An iterator over a sequence of firings.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    public Iterator firingIterator() {
        return new FiringIterator(this);
    }

    /** Get firings for the firing element. This is used in determining
     *  element appearances.
     *
     *  @param firingElement The given firing element.
     *  @return A list of firings for the firing element.
     */

    //FIXME: It's probably suitable for 'protected' or 'private'.
    //FIXME: The returned type can be replaced with un-ordered
    //       collection, eg. Set, if the sequence of firings
    //       doesn't matter.
    public List firings(Object firingElement) {
        Map firingElementFiringsMap = _getFiringElementFiringsMap();
        return Collections.unmodifiableList((List) firingElementFiringsMap
                .get(firingElement));
    }

    /** Return the element at the specified position in the list.
     *
     * @param index The index of the element to return.
     * @return The element at the specified position in the list.
     */
    public ScheduleElement get(int index) {
        return ((ScheduleElement) _schedule.get(index));
    }

    /** Return an iterator over the schedule elements of this schedule.
     *  The ordering of elements in the iterator sequence is the order
     *  of the schedule list. The elements of the iterator sequence are
     *  instances of Firing or Schedule.
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *
     *  @return An iterator over the schedule elements of this schedule.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    public Iterator iterator() {
        return _schedule.iterator();
    }

    /** Get the lexical order of firing elements. Only single appearance
     *  schedules are qualified for this operation. It's meaningless to
     *  have the order for multiple appearance schedules.
     *
     *  @return The lexical order of firing elements.
     *  @exception IllegalStateException If this is not a single
     *             appearance schedule.
     */
    public List lexicalOrder() {
        List lexicalList = new ArrayList();
        Iterator firingInstances = _getFiringInstancesList().iterator();

        while (firingInstances.hasNext()) {
            Firing firing = (Firing) firingInstances.next();
            Object firingElement = firing.getFiringElement();

            if (lexicalList.contains(firingElement)) {
                throw new IllegalStateException(
                        "Not a single appearance schedule to compute "
                                + "the lexical order of nodes.");
            } else {
                lexicalList.add(firingElement);
            }
        }

        return Collections.unmodifiableList(lexicalList);
    }

    /** Get the maximum appearance counts of all firing elements in
     *  the schedule.
     *
     *  @return The maximum appearance counts.
     */
    public int maxAppearanceCount() {
        int maxCount = 0;
        Iterator firingLists = _getFiringElementFiringsMap().values()
                .iterator();

        while (firingLists.hasNext()) {
            int firingListSize = ((List) firingLists.next()).size();

            if (maxCount < firingListSize) {
                maxCount = firingListSize;
            }
        }

        return maxCount;
    }

    /** Remove the schedule element at the specified position in the
     *  schedule list.
     *
     *  @param index The index of the schedule element to be removed.
     *  @return The schedule element that was removed.
     */
    public ScheduleElement remove(int index) {
        _incrementVersion();
        return ((ScheduleElement) _schedule.remove(index));
    }

    /** Return the number of elements in this list.
     *
     *  @return The number of elements in this list.
     */
    public int size() {
        return (_schedule.size());
    }

    /** Print the schedule in a nested parenthesis style. Please see
     *  {@link ScheduleElement#toParenthesisString(Map, String)}.
     *
     *  @param nameMap The map from firing elements to their short names.
     *  @param delimiter The delimiter between iterands.
     *  @return A nested parenthesis expression of the schedule.
     */
    public String toParenthesisString(Map nameMap, String delimiter) {
        String result = new String() + "(";
        int iterations = getIterationCount();

        if (iterations > 1) {
            result += iterations;
        }

        Iterator schedules = iterator();

        while (schedules.hasNext()) {
            ScheduleElement schedule = (ScheduleElement) schedules.next();
            result += (delimiter + schedule.toParenthesisString(nameMap,
                    delimiter));
        }

        result += ")";
        return result;
    }

    /** Output a string representation of this Schedule.
     *
     *  @return Return a string representation of this Schedule.
     */
    public String toString() {
        String result = "Execute Schedule{\n";
        Iterator i = iterator();

        while (i.hasNext()) {
            ScheduleElement e = (ScheduleElement) i.next();
            result += (e + "\n");
        }

        result += "}";

        if (getIterationCount() > 1) {
            result += (" " + getIterationCount() + " times");
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An adapter class for iterating over the firing elements of this
     *  schedule. An exception is thrown if the schedule structure
     *  changes while this iterator is active.
     */
    private class FiringElementIterator implements Iterator {
        /** Construct a ScheduleIterator.
         */
        public FiringElementIterator() {
            _advance = true;
            _firingIterator = firingIterator();
            _currentVersion = _getVersion();
            _currentIteration = 0;
        }

        /** Return true if the iteration has more elements.
         *
         *  @exception ConcurrentModificationException If the schedule
         *  data structure has changed since this iterator was created.
         *  @return True if the iterator has more elements.
         */
        public boolean hasNext() {
            if (_currentVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else if (_advance == true) {
                boolean returnValue;

                if (_currentFiring == null) {
                    returnValue = _firingIterator.hasNext();

                    if (returnValue == true) {
                        _currentFiring = (Firing) _firingIterator.next();
                        _currentFiringElement = _currentFiring
                                .getFiringElement();
                        _currentIteration++;
                        _advance = false;
                        _lastHasNext = true;
                        return _lastHasNext;
                    } else {
                        _advance = false;
                        _lastHasNext = false;
                        return _lastHasNext;
                    }
                } else {
                    if (_currentIteration < _currentFiring.getIterationCount()) {
                        _currentIteration++;
                        _advance = false;
                        _lastHasNext = true;
                        return _lastHasNext;
                    } else {
                        _currentIteration = 0;
                        returnValue = _firingIterator.hasNext();

                        if (returnValue == true) {
                            _currentFiring = (Firing) _firingIterator.next();
                            _currentFiringElement = _currentFiring
                                    .getFiringElement();
                            _currentIteration++;
                            _advance = false;
                            _lastHasNext = true;
                            return _lastHasNext;
                        } else {
                            _advance = false;
                            _lastHasNext = false;
                            return _lastHasNext;
                        }
                    }
                }
            } else {
                return _lastHasNext;
            }
        }

        /** Return the next object in the iteration.
         *
         *  @exception InvalidStateException If the schedule data structure
         *  has changed since this iterator was created.
         *  @return The next object in the iteration.
         */
        public Object next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("No element to return.");
            } else if (_currentVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else {
                _advance = true;
                return _currentFiringElement;
            }
        }

        /** Throw an exception, since removal is not allowed. It really
         *  doesn't make sense to remove an actor from an actor invocation
         *  sequence anyway.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private Iterator _firingIterator;

        private Firing _currentFiring;

        private Object _currentFiringElement;

        private long _currentVersion;

        private int _currentIteration;

        private boolean _advance;

        private boolean _lastHasNext;
    }

    /** An adapter class for iterating over the firings of this
     *  schedule. An exception is thrown if the schedule structure
     *  changes while this iterator is active. The iterator walks
     *  the schedule tree as the hasNext() and next() methods are
     *  invoked, using a small number of state variables.
     */
    private class FiringIterator implements Iterator {
        /** Construct a ScheduleIterator.
         */
        public FiringIterator(Schedule schedule) {
            // If _advance is true, then hasNext() can move
            // to the next node when invoked.
            _advance = true;
            _startingVersion = _getVersion();

            // state. This is the current position as we walk
            // the schedule tree.
            _currentNode = schedule;

            // The depth of _currentNode in the schedule tree.
            // Depth 0 corresponds to the level of the root node
            // of the tree.
            _currentDepth = 0;

            // These state arrays are dynamically increased
            // in size as we deeper into the tree. Their
            // length is equal to the number of nesting levels
            // in the schedule.
            _iterationCounts = new int[_treeDepth];
            _horizontalNodePosition = new int[_treeDepth];
        }

        /** Return true if the iteration has more elements.
         *
         *  @exception ConcurrentModificationException If the schedule
         *  data structure has changed since this iterator was created.
         *  @exception InternalErrorException If the schedule contains
         *  any leaf nodes that are not an instance of Firing.
         *  @return True if the iterator has more elements.
         */
        public boolean hasNext() {
            // This code may look messy, but it simply walks the
            // schedule tree.
            if (_startingVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else if (_advance == true) {
                // Try to go to the next firing node in the tree. Return
                // false if we fail.
                if (_currentNode instanceof Firing) {
                    Schedule scheduleNode = _backTrack(_currentNode);
                    _currentNode = _findLeafNode(scheduleNode);

                    if (_currentNode == null) {
                        // There are no more Firings in the tree.
                        _advance = false;
                        _lastHasNext = false;
                        return _lastHasNext;
                    }
                } else if (_currentNode instanceof Schedule) {
                    // This condition should only happen for the first element
                    // in the iteration.
                    _currentNode = _findLeafNode((Schedule) _currentNode);

                    if (_currentNode == null) {
                        // There are no more Firings in the tree at all.
                        _advance = false;
                        _lastHasNext = false;
                        return _lastHasNext;
                    }
                } else {
                    // Throw runtime exception.
                    throw new RuntimeException(
                            "Encountered a ScheduleElement that "
                                    + "is not an instance "
                                    + "of Schedule or Firing.");
                }

                _advance = false;
                _lastHasNext = true;
                return _lastHasNext;
            } else {
                return _lastHasNext;
            }
        }

        /** Return the next object in the iteration.
         *
         *  @exception InvalidStateException If the schedule
         *  data structure has changed since this iterator was created.
         *  @return The next object in the iteration.
         */
        public Object next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("No element to return.");
            } else if (_startingVersion != _getVersion()) {
                throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
            } else {
                _advance = true;
                return _currentNode;
            }
        }

        /** Throw an exception, since removal is not allowed. It really
         *  doesn't make sense to remove a firing from the firing
         *  sequence anyway, since there is not a 1-1 correspondence
         *  between the firing in a firing iterator and a firing in the
         *  schedule.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                 ////

        /** Start at the specified node in the schedule tree and
         *  move up the tree (towards the root node) until we
         *  find a node the has children we have not iterated through
         *  yet or children that we have not iterated through enough
         *  times (not reached the maximum iteration count).
         *
         *  @param firingNode The starting node to backtrack from.
         */
        private Schedule _backTrack(ScheduleElement firingNode) {
            if (_currentDepth == 0) {
                // Don't backtrack past the root node.
                return null;
            }

            _currentDepth--;

            Schedule node = (Schedule) firingNode.getParent();

            if (node == null) {
                return null;
            } else if (node.size() > (++_horizontalNodePosition[_currentDepth + 1])) {
                return node;
            } else if ((++_iterationCounts[_currentDepth]) < node
                    .getIterationCount()) {
                _horizontalNodePosition[_currentDepth + 1] = 0;
                return node;
            }

            _horizontalNodePosition[_currentDepth + 1] = 0;
            _iterationCounts[_currentDepth] = 0;
            return (_backTrack(node));
        }

        /** Start at the specified node and move down the tree
         *  (away from the root node) until we find the next Firing
         *  in the iteration order. Through a runtime exception
         *  if we encounter a leaf node that is not an instance of
         *  Firing.
         *
         *  @param node The schedule node to start from.
         */
        private ScheduleElement _findLeafNode(Schedule node) {
            // Check if we need to resize the arrays.
            if ((_iterationCounts.length - 1) < (_currentDepth + 2)) {
                // Need to resize.
                int[] temp = new int[_currentDepth + 2];

                for (int i = 0; i < _iterationCounts.length; i++) {
                    temp[i] = _iterationCounts[i];
                }

                _iterationCounts = temp;

                int[] temp2 = new int[_currentDepth + 2];

                for (int i = 0; i < _horizontalNodePosition.length; i++) {
                    temp2[i] = _horizontalNodePosition[i];
                }

                _horizontalNodePosition = temp2;

                // Set new max tree depth. Any new iterators will
                // create state arrays with this length to avoid
                // needing to resize in the future (provide the
                // schedule structure is not modified).
                _treeDepth = _currentDepth + 2;
            }

            if (node == null) {
                return null;
            } else if (node.size() > _horizontalNodePosition[_currentDepth + 1]) {
                _currentDepth++;

                ScheduleElement nodeElement = node
                        .get(_horizontalNodePosition[_currentDepth]);

                if (nodeElement instanceof Firing) {
                    return nodeElement;
                } else {
                    return _findLeafNode((Schedule) nodeElement);
                }
            } else if (node.size() == 0) {
                Schedule scheduleNode = _backTrack(_currentNode);
                return _findLeafNode(scheduleNode);
            } else if (_iterationCounts[_currentDepth] < node
                    .getIterationCount()) {
                ScheduleElement nodeElement = node.get(0);
                _currentDepth++;

                if (nodeElement instanceof Firing) {
                    return nodeElement;
                } else {
                    return _findLeafNode((Schedule) nodeElement);
                }
            } else {
                return null;
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////
        private boolean _advance;

        private ScheduleElement _currentNode;

        private boolean _lastHasNext;

        // The current depth in the schedule tree.
        private int _currentDepth;

        private long _startingVersion;

        // This array contains the iteration counts of schedule elements
        // indexed by tree depth.
        // This array will grow to the depth of the schedule tree.
        private int[] _iterationCounts;

        private int[] _horizontalNodePosition;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Get unique firing instances. Repeated firings generally
     *  occur in the iterator returned by
     *  {@link ptolemy.graph.sched.ScheduleElement#firingIterator()}.
     *  This method removes duplications and keeps a list of
     *  unique firings. Though not sure whether it matters for the
     *  sequence, firings returned are in the form of <code>List</code>
     *  rather than un-ordered <code>Collection</code>.
     *
     *  @return A list of distinct firings.
     */
    private List _getFiringInstancesList() {
        /* Build _firingInstanceList if it does not exist. This cache
         * style implementation ensures execution efficiency.
         */
        if (_firingInstancesList == null) {
            _firingInstancesList = new ArrayList();

            Iterator originalFirings = firingIterator();

            while (originalFirings.hasNext()) {
                Object firing = originalFirings.next();

                if (!_firingInstancesList.contains(firing)) {
                    _firingInstancesList.add(firing);
                }
            }
        }

        return _firingInstancesList;
    }

    /*  Get the map of firing elements to firings. Multiple firings
     *  can be created for any single element and the firings are
     *  obtained in a <code>List</code>. Therefore, the map returned
     *  is actually a mapping from elements to <code>List</code>s
     *  of firings.
     *
     *  @return The map from elements to firings.
     */
    private Map _getFiringElementFiringsMap() {
        /* Build _nodeFiringsMap if it does not exist. This cache
         * style implementation ensures execution efficiency.
         */
        if (_firingElementFiringsMap == null) {
            _firingElementFiringsMap = new HashMap();

            Iterator firingInstances = _getFiringInstancesList().iterator();

            while (firingInstances.hasNext()) {
                Firing firing = (Firing) firingInstances.next();
                Object node = firing.getFiringElement();
                List firingList = null;

                if (!_firingElementFiringsMap.containsKey(node)) {
                    firingList = new ArrayList();
                    _firingElementFiringsMap.put(node, firingList);
                } else {
                    firingList = (List) _firingElementFiringsMap.get(node);
                }

                firingList.add(firing);
            }
        }

        return Collections.unmodifiableMap(_firingElementFiringsMap);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The list of schedule elements contained by this schedule.
    protected List _schedule;

    // The list of Firings for this schedule.
    //private List _firingList;
    // The current version of the firing iterator list.
    //private long _firingIteratorVersion;
    // The depth of this schedule tree. This may grow.
    private int _treeDepth;

    /* A list of all distinct firings. */
    private List _firingInstancesList;

    /* A map from firing elements to firings. */
    private Map _firingElementFiringsMap;
}
