/* A count and a list of schedule elements.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

//////////////////////////////////////////////////////////////////////////
//// Schedule
/**
This class is a schedule, which is a count and a list of schedule elements. 
A schedule consists of an iteration count and a list of schedule elements.
A schedule element can correspond to a single actor, or a schedule
element can itself be a schedule. This nesting can be arbitrarily deep.
However, all of the lowest-level schedule elements must correspond to 
an actor. If this were not the case, then the actor invocation sequence 
corresponding to the schedule would contain null elements. It is up to 
the scheduler to enforce this requirement.
<p>
<i>Terminology</i>
A schedule (or schedule loop) has the form
(n,S<sub>1</sub>,S<sub>2</sub>...,S<sub>m</sub>) where n is a positive 
integer called the iteration count, and S<sub>i</sub> is either another 
schedule loop or an actor. The schedule can be expressed as a sequence 
S<sub>1</sub>S<sub>2</sub>...S<sub>m</sub> where S<sub>i</sub> is either 
an actor or a schedule loop.
<p>
<h1>Usage</h1>
In this implementation, if S<sub>i</sub> corresponds to an actor, then 
S<sub>i</sub> will be an instance of class Firing. Otherwise, if 
S<sub>i</sub> corresponds to a schedule, then S<sub>i</sub> will be an 
instance of Schedule.
<p>
The Schedule class is a schedule element that contains an iteration
count and a list of schedule elements. The Firing class is a schedule 
element that contains only a reference to an actor and an iteration count 
for that actor. Therefore, the top-level schedule element must be an 
instance of Schedule, and all of the lowest-level schedule elements must 
be an instance of Firing. The iteration count is set by the 
setIterationCount() method. If this method is not invoked, a default value 
of one will be used. The add() and remove() methods are used to add or 
remove schedule elements. Only elements of type ScheduleElement (Schedule 
or Firing) may be added to the schedule list. Otherwise an exception will
occur.
<p>
As an example, suppose that we have an SDF graph containing actors
A, B, C, and D, with the schedule (1, A, (3, B, C), (2, D)). 
The schedule can be written as (1, S<sub>1</sub>S<sub>2</sub>S<sub>3</sub>),
where S<sub>1</sub> = A, S<sub>2</sub> = (3, B, C), and S<sub>3</sub> = (2, D).
To construct this schedule, create an instance of Schedule called S.
Then add the schedule elements S<sub>1</sub>, S<sub>2</sub>, S<sub>3</sub>, and
set an iteration count of 1, which is the default. S<sub>1</sub> will be an 
instance of Firing with a reference to actor A and an iteration count of 1.
S<sub>2</sub> will be an instance of Schedule with elements
S<sub>2,1</sub>, S<sub>2,2</sub>, and an iteration count of 3.
S<sub>2,1</sub>, S<sub>2,2</sub> will each be an instance of Firing
with an iteration count of 1 and a reference to actors B and C,
respectively. S<sub>3</sub> will be an instance of Firing with
a reference to actor D and an iteration count of 2. The code to create
this schedule appears below.
<p>
<pre>
Schedule S = new Schedule();
Firing S1 = new Firing();
Schedule S2 = new Schedule();
Firing S3 = new Firing();
S.add(S1);
S.add(S2);
S.add(S3);
S1.setActor(A);
S2.setIterationCount(3);
Firing S2_1 = new Firing();
Firing S2_2 = new Firing();
S2_1.setActor(B);
S2_2.setActor(C);
S2.add(S2_1);
S2.add(S2_2);
S3.setIterationCount(2);
S3.setActor(D);
</pre>
<p>
Note that this implementation is not synchronized. It is therefore not safe
for a thread to make modifications to the schedule structure while
multiple threads are concurrently accessing the schedule.
<h1>References</h1>
S. S. Bhattacharyya, P K. Murthy, and E. A. Lee,
Software Syntheses from Dataflow Graphs, Kluwer Academic Publishers, 1996.

@author Brian K. Vogel
@version $Id$
@see ptolemy.actor.sched.Firing
@see ptolemy.actor.sched.ScheduleElement
*/

public class Schedule extends ScheduleElement {
    /** Construct a schedule with iteration count of one and an 
     *  empty schedule list. This constructor should be used when 
     *  creating a root schedule. The constructor that takes a 
     *  parameter should be used when creating a subschedule.
     */
    public Schedule() {
	super();
	// This list will contain the schedule elements.
	_schedule = new LinkedList();
	_firingIteratorVersion = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the specified schedule element to the end of the schedule
     *  list. This element must be an instance of ScheduleElement.
     *
     *  @param element The schedule element to add.
     */
    public void add(ScheduleElement element) {
	// Give element a reference to this schedule so that it can
	// notify this schedule (via _incrementVerions()) when
	// element is modified.
	element._setParent(this);
	_incrementVersion();
	_schedule.add(element);
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
	// notify this schedule (via _incrementVerions()) when
	// element is modified.
	element._setParent(this);
	_incrementVersion();
	_schedule.add(index, element);
    }

    /** Return the actor invocation sequence of the schedule in the
     *  form of a sequence of actors. For a valid schedule, all of the
     *  lowest-level nodes should be an instance of Firing. If the
     *  schedule is not valid, then the returned iterator will contain
     *  null elements.
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *  <p>
     *  Implementation note: This method is optimized to provide fast
     *  access for the case where the schedule is not modified often.
     *  The first invocation of this method after a schedule modification
     *  (this schedule are any of its subschedules are modified) will
     *  be somewhat time consuming. Subsequent invocations will be fast.
     *
     *  @return An iterator over a sequence of actors.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    public Iterator actorIterator() {
	return new ActorIterator();
    }

    /** Return the actor invocation sequence of this schedule in the form
     *  of a sequence of firings. For a valid schedule, all of the
     *  lowest-level nodes must be an instance of Firing. If not, then
     *  the returned iterator will contain null elements. 
     *  <p>
     *  A runtime exception is thrown if the
     *  underlying schedule structure is modified while the iterator
     *  is active.
     *  <p>
     *  Implementation note: This method is optimized to provide fast
     *  access for the case where the schedule is not modified often.
     *  The first invocation of this method after a schedule modification
     *  (this schedule are any of its subschedules are modified) will
     *  be somewhat time consuming. Subsequent invocations will be fast.
     *
     *  @return An iterator over a sequence of firings.
     *  @exception ConcurrentModificationException If the
     *   underlying schedule structure is modified while the iterator
     *   is active.
     */
    public Iterator firingIterator() {
	if (_firingIteratorVersion != _getVersion()) {
	    // Note: this is expensive to compute, but hopefully
	    // the schedule topology will not change too frequently.
	    _firingList = _createFiringList();
	    _firingIteratorVersion = _getVersion();
	}
	return _firingList.iterator();
    }

    /** Return the element at the specified position in the list.
     *
     * @param index The index of the element to return.
     * @return The element at the specified position in the list.
     */
    public ScheduleElement get(int index) {
	return((ScheduleElement)_schedule.get(index));
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

    /** Remove the schedule element at the specified position in the
     *  schedule list.
     *
     *  @param index The index of the schedule element to be removed.
     *  @return The schedule element that was removed.
     */
    public ScheduleElement remove(int index) {
	_incrementVersion();
	return((ScheduleElement)_schedule.remove(index));
    }

    /** Return the number of elements in this list.
     *
     *  @return The number of elements in this list.
     */
    public int size() {
	return(_schedule.size());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Arrange the Firing nodes of the schedule into a list. This method
     *  walks the schedule tree and creates a list of the Firings in the
     *  schedule. Since this method is expensive, it should only be called
     *  when the schedule version has changed.
     */
    private List _createFiringList() {
	List returnFirings = new LinkedList();
	// Iterator over the elements directly contained by this
	// schedule.
	
	for (int i = 0; i < getIterationCount(); i++) {
	    Iterator scheduleItems = _schedule.iterator();
	    while (scheduleItems.hasNext()) {
		ScheduleElement scheduleElement =
		    (ScheduleElement)scheduleItems.next();
		Iterator firings = scheduleElement.firingIterator();
		while (firings.hasNext()) {
		    Firing firing = (Firing)firings.next();
		    // Add the actor to the list.
		    returnFirings.add(firing);
		}
	    }
	}
	return returnFirings;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An adapter class for iterating over the actors of this
     *  schedule. An exception is thrown if the schedule structure
     *  changes while this iterator is active.
     */
    private class ActorIterator implements Iterator {
	/** Construct a ScheduleIterator.
	 */
	public ActorIterator() {
	    if (_firingIteratorVersion != _getVersion()) {
		// Note: this is expensive to compute, but hopefully
		// the scedule topology will not change too frequently.
		_firingList = _createFiringList();
		_firingIteratorVersion = _getVersion();
	    }
	    _firingListSize = _firingList.size();
	    _currentVersion = _getVersion();
	    _currentFiringElement = 0;
	    _currentIteration = 0;
	}

	/** Return true if the iteration has more elements.
	 *
	 *  @exception ConcurrentModificationException If the schedule
	 *   data structure has changed since this iterator
	 *   was created.
	 *  @return true if the iterator has more elements.
	 */
	public boolean hasNext() {
	    if (_currentVersion != _getVersion()) {
		throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
	    } else {
		if (_currentFiringElement < _firingListSize) {
		    return true;
		}
		return false;
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
	    } else if (_currentVersion != _getVersion()) {
		throw new ConcurrentModificationException(
                        "Schedule structure changed while iterator is active.");
	    } else {
		Firing firing = (Firing)_firingList.get(_currentFiringElement);
		_iterationCount = firing.getIterationCount();
		_currentIteration++;
		if (_currentIteration >= _iterationCount) {
		    _currentIteration = 0;
		    _currentFiringElement++;
		}
		return firing.getActor();
	    }
	}

	/** Throw an exception, since removal is not allowed. It really
	 *  dosn't make sense to remove an actor from an actor invocation
	 *  sequence anyway.
	 */
	public void remove() {
	    throw new UnsupportedOperationException();
	}

	private long _currentVersion;
	private int _currentFiringElement;
	// Number of firings in the firing list.
	private int _firingListSize;
	private int _iterationCount;
	private int _currentIteration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of schedule elements contained by this schedule.
    private List _schedule;
    // The list of Firings for this schedule.
    private List _firingList;
    // The current version of the firing iterator list.
    private long _firingIteratorVersion;
}
