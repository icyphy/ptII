/* A queue with variable capacity and optional history.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (liuj@eecs.berkeley.edu)

*/

package ptolemy.actor.util;

import ptolemy.kernel.util.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Collections;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// FIFOQueue
/**
A first-in, first-out (FIFO) queue with variable capacity and optional
history. Objects are appended to the queue with the put() method,
and removed from the queue with the take() method. The object
removed is the oldest one in the queue. By default, the capacity is
infinite, but it can be set to any nonnegative size. If the history
capacity is greater than zero (or infinite, by setting the capacity to
INFINITE_CAPACITY), then objects removed from the queue are transferred
to a history queue rather than simply removed. By default, the history
capacity is zero.

@author Edward A. Lee, Xiaojun Liu
@version $Id$
*/
public class FIFOQueue implements Cloneable {

    /** Construct an empty queue with no container.
     */
    public FIFOQueue() {
        _queueList = new LinkedList();
        _historyList = new LinkedList();
    }

    /** Construct an empty queue with the specified container. The
     *  container is only used for error reporting.
     *  @param container The container of the queue.
     */
    public FIFOQueue(Nameable container) {
        this();
        _container = container;
    }

    /** Copy constructor. Create a copy of the specified queue, but
     *  with no container. This is useful to permit enumerations over
     *  a queue while the queue continues to be modified. The objects
     *  in the queue themselves are not cloned.
     *  @param model The queue to be copied.
     */
    public FIFOQueue(FIFOQueue model) {
        this();
        synchronized(model) {
            _queueList.addAll( model.elementList() );
            _historyList.addAll( model.historyElementList());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove all items currently stored in the queue and
     *  clear the history queue. The queue capacity, history
     *  capacity and container remain unchanged.
     */
    public void clear() {
        _queueList.clear();
        _historyList.clear();
    }

    /** Clone this queue. The cloned queue has no container. The
     *  objects in the queue themselves are not cloned.
     *  @return A clone of this queue
     */
    public Object clone() {
        return new FIFOQueue(this);
    }

    /** Enumerate the objects in the queue, beginning with the oldest.
     *  This method is deprecated and calls elementList()
     *  @return An enumeration of objects.
     *  @deprecated Used elementList() instead.
     */
    public Enumeration elements() {
        return Collections.enumeration( _queueList );
    }

    /** List the objects in the queue, beginning with the oldest.
     *  @return A list of objects.
     */
    public List elementList() {
        return _queueList;
    }

    /** Return true if the number of objects in the queue equals the
     *  queue capacity.
     *  @return A boolean indicating whether the queue is full.
     */
    public boolean isFull() {
        return _queueList.size() == _queueCapacity;
    }

    /** Return an object in the queue or history. The object is not
     *  removed from the queue or history. If the offset argument is
     *  zero, return the oldest object in the queue. If the offset is
     *  1, return the second oldest object, etc. If there is no such
     *  object in the queue (the offset is greater than or equal to
     *  the current queue size), throw an exception. If the argument
     *  is -1, return the most recent object that was put in the
     *  history. If the argument is -2, return the second most recent
     *  object in the history, etc. If there is no such object in the
     *  history (the history capacity is zero or the absolute value
     *  of the offset is greater than the current size of the history
     *  queue), throw an exception.
     *  @param offset The position of the desired object.
     *  @return The desired object in the queue or history.
     *  @exception NoSuchElementException If the offset is out of range.
     */
    public Object get(int offset) throws NoSuchElementException {
        Object obj = null;
        try {
            if (offset >= 0) {
                obj = _queueList.get(offset);
            } else {
                obj = _historyList.get(historySize()+offset);
            }
        } catch (IndexOutOfBoundsException ex) {
            String str = ".";
            if (_container != null) {
                str = " contained by " + _container.getFullName();
            }
            throw new NoSuchElementException("No object at offset "
                    + offset + " in the FIFOQueue" + str);
        }
        return obj;
    }

    /** Return the queue capacity, or INFINITE_CAPACITY if it is unbounded.
     *  @return The capacity of the queue.
     */
    public int getCapacity() {
        return _queueCapacity;
    }

    /** Return the container of the queue, or null if there is none.
     *  @return The container of the queue.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return the capacity of the history queue.
     *  This will be zero if the history mechanism is disabled and
     *  INFINITE_CAPACITY if the history capacity is infinite.
     *  @return The capacity of the history queue.
     */
    public int getHistoryCapacity() {
        return _historyCapacity;
    }

    /** Enumerate the objects in the history, which are the N most recent
     *  objects taken from the queue, beginning with the oldest, where
     *  N is less than or equal to the history capacity. This method is
     *  deprecated and calls historyElementList().
     *  @return An enumeration of objects in the history.
     *  @deprecated Use historyElementList() instead.
     */
    public Enumeration historyElements() {
        return Collections.enumeration( _historyList );
    }

    /** List the objects in the history, which are the N most recent
     *  objects taken from the queue, beginning with the oldest, where
     *  N is less than or equal to the history capacity. If the history
     *  capacity is infinite, then the list includes all objects
     *  previously taken from the queue. If the history capacity is zero,
     *  then return an empty list.
     *  @return A list of objects in the history.
     */
    public List historyElementList() {
        return _historyList;
    }

    /** Return the number of objects in the history.
     *  @return The current number of objects in the history.
     */
    public int historySize() {
        return _historyList.size();
    }

    /** Put an object in the queue and return true if this will not
     *  cause the capacity to be exceeded. Otherwise, do not put
     *  the object in the queue and return false.
     *  @param element An object to be put in the queue.
     *  @return A boolean indicating success.
     */
    public boolean put(Object element) {
        if (_queueCapacity == INFINITE_CAPACITY ||
                _queueCapacity > _queueList.size()) {
            _queueList.addLast(element);
            return true;
        } else {
            return false;
        }
    }

    /** Set queue capacity. Use INFINITE_CAPACITY to indicate unbounded
     *  capacity (which is the default). If the current size of the
     *  queue exceeds the desired capacity, throw an exception.
     *  @param capacity The desired capacity.
     *  @exception IllegalActionException If the queue contains more
     *   objects than the proposed capacity or the proposed capacity
     *   is illegal.
     */
    public void setCapacity(int capacity)
            throws IllegalActionException {
        if (capacity < 0 && capacity != INFINITE_CAPACITY) {
            throw new IllegalActionException(_container,
                    "Cannot set queue capacity to " + capacity);
        }
        if (capacity != INFINITE_CAPACITY && size() > capacity) {
            throw new IllegalActionException(_container,
                    "Queue contains more elements than the proposed capacity.");
        }
        _queueCapacity = capacity;
    }

    /** Set the container of the queue. The container is only used
     *  for error reporting.
     *  @param container The container of this queue.
     */
    public void setContainer(Nameable container) {
        _container = container;
    }

    /** Set the capacity of the history queue. Use 0 to disable the
     *  history mechanism and INFINITE_CAPACITY to make the history
     *  capacity unbounded. If the size of the history queue exceeds
     *  the desired capacity, remove the oldest objects from the
     *  history queue until its size equals the proposed capacity.
     *  Note that this can be used to clear the history queue by
     *  supplying 0 as the argument.
     *  @param capacity The desired capacity of the history queue.
     *  @exception IllegalActionException If the desired capacity
     *   is illegal.
     */
    public void setHistoryCapacity(int capacity)
            throws IllegalActionException {
        if (capacity > 0) {
            while (_historyList.size() > capacity) {
                _historyList.removeFirst();
            }
        } else if (capacity == 0) {
            _historyList.clear();
        } else if (capacity != INFINITE_CAPACITY) {
            throw new IllegalActionException(_container,
                    "Cannot set history capacity to " + capacity);
        }
        _historyCapacity = capacity;
    }

    /** Return the number of objects in the queue.
     *  @return The number of objects in the queue.
     */
    public int size() {
        return _queueList.size();
    }

    /** Remove the oldest object from the queue and return it.
     *  If there is no such object in the queue (the queue is empty),
     *  throw an exception. If the history mechanism is enabled,
     *  then put the taken object in the history queue. If the capacity
     *  of the history queue would be exceeded by this, then first remove
     *  the oldest object in the history queue.
     *  @return An object from the queue.
     *  @exception NoSuchElementException If the queue is empty.
     */
    public Object take() throws NoSuchElementException {
        Object obj = null;
        try {
            obj = _queueList.removeFirst();
        } catch (NoSuchElementException ex) {
            String str = "";
            if (_container != null) {
                str = " contained by " + _container.getFullName();
            }
            throw new NoSuchElementException("The FIFOQueue" + str
                    + " is empty!");
        }

        if (_historyCapacity != 0) {
            if (_historyCapacity == _historyList.size()) {
            	_historyList.removeFirst();
            }
            _historyList.addLast(obj);
        }
        return obj;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Used to indicate that the size of the queue or the history
     *  queue is infinite.
     */
    public static final int INFINITE_CAPACITY = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container, if there is one.
    private Nameable _container = null;

    // The capacity of the queue, defaulting to infinite.
    private int _queueCapacity = INFINITE_CAPACITY;

    // The list of objects currently in the queue.
    private LinkedList _queueList;

    // The capacity of the history queue, defaulting to zero.
    private int _historyCapacity = 0;

    // The list of objects recently removed from the queue.
    private LinkedList _historyList = null;

}
