/* A queue with optional capacity and history.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.actor.util;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import collections.LinkedList;
import collections.CollectionEnumeration;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// FIFOQueue
/**
A first-in, first-out (FIFO) queue with optional capacity and
history. Objects are appended to the queue with the put() method,
and removed from the queue with the take() method. The object
removed is the oldest one in the queue. By default, the capacity is
unbounded, but it can be set to any nonnegative size. If the history
capacity is greater than zero (or infinite, indicated by a capacity
of -1), then objects removed from the queue are transfered to a
second queue rather than simply deleted. By default, the history
capacity is zero.

@author Edward A. Lee
@version $Id$
*/
public class FIFOQueue implements Cloneable {

    /** Construct an empty queue with no container.
     */
    public FIFOQueue() {
        _queuelist = new LinkedList();
        _historylist = new LinkedList();
    }

    /** Construct an empty queue with the specified container.
     */
    public FIFOQueue(Nameable container) {
        this();
        _container = container;
    }

    /** Copy constructor.  Create a copy of the specified queue, but
     *  with no container.  This is useful to permit enumerations over
     *  a queue while the queue continues to be modified.
     */
    public FIFOQueue(FIFOQueue model) {
        this();
        _queuelist.appendElements(model.elements());
    }

    /** Copy constructor.  Create a copy of the specified queue, but
     *  with the specified container.
     */
    public FIFOQueue(FIFOQueue model, Nameable container) {
        this(model);
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the capacity, or -1 if it is unbounded.
     */
    public int capacity() {
        return _queuecapacity;
    }

    /** Enumerate the items on the queue, beginning with the oldest.
     *  @return An enumeration of objects.
     *  @see collections.LinkedList#elements()
     */
    public CollectionEnumeration elements() {
        return _queuelist.elements();
    }

    /** Return true if the number of objects in the queue equals the
     *  capacity.
     */
    public boolean full() {
        return _queuelist.size() == _queuecapacity;
    }

    /** Return an element on the queue.  If the offset argument is
     *  zero, return the most recent object that was put on the queue.
     *  If the offset is 1, return second most recent the object, etc.
     *  If there is no such element on the queue (the offset is greater
     *  than or equal to the size, or is negative), throw an exception.
     *  @exception NoSuchElementException The offset is out of range.
     */
    public Object get(int offset)
            throws NoSuchElementException {
        return _queuelist.at(size()-offset-1);
    }

    /** Return the container of the queue, or null if there is none.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Enumerate the items stored in the history queue, which are
     *  the N most recent items taken from the queue, beginning with
     *  the oldest, where N is less
     *  than or equal to the history capacity.  If the history capacity
     *  is -1, then the enumeration includes all items previously taken
     *  from the queue.  If the history capacity is zero, then return an
     *  empty enumeration.
     *  @return An enumeration of objects.
     *  @see collections.LinkedList#elements()
     */
    public CollectionEnumeration history() {
        return _historylist.elements();
    }

    /** Return the capacity of the history queue.
     *  This will be zero if the history mechanism is disabled
     *  and -1 if the history capacity is unbounded.
     */
    public int historyCapacity() {
        return _historycapacity;
    }

    /** Return the number of objects in the history.
     */
    public int historySize() {
        return _historylist.size();
    }

    /** Return an element from the history.  If the offset argument is
     *  zero, return the most recent object in the history, which is
     *  object most recently taken from the queue.
     *  If the offset is 1, return second most recent the object, etc.
     *  If there is no such element in the history (the offset is greater
     *  than or equal to the number of objects in the history, or is
     *  negative), throw an exception.
     *  @exception NoSuchElementException The offset is out of range.
     */
    public Object previous(int offset)
            throws NoSuchElementException {
        return _historylist.at(historySize()-offset-1);
    }

    /** Put an object on the queue and return true if this will not
     *  cause the capacity to be exceeded.  Otherwise, do not put
     *  the object on the queue and return false.
     *  @param element An object to put on the queue.
     *  @return A boolean indicating success.
     */
    public boolean put(Object element) {
        if (_queuecapacity == -1 || _queuecapacity > _queuelist.size()) {
            _queuelist.insertLast(element);
            return true;
        } else {
            return false;
        }
    }

    /** Set the capacity.  Use -1 to indicate unbounded capacity
     *  (which is the default).  If the size of the queue exceeds the
     *  desired capacity, throw an exception.
     *  @exception IllegalActionException Queue contains more elements
     *   than the proposed capacity.
     */
    public void setCapacity(int capacity)
            throws IllegalActionException {
        if (size() > capacity) {
            throw new IllegalActionException(_container,
                    "Queue contains more elements than the proposed capacity.");
        }
        _queuecapacity = capacity;
    }

    /** Set the capacity of the history queue.
     *  Use 0 to disable the history mechanism
     *  and -1 to make the history capacity unbounded.
     *  If the size of the history list exceeds the
     *  desired capacity, then remove the oldest items from
     *  the history list until its size equals the proposed capacity.
     *  Note that this can be used to clear the history list.
     */
    public void setHistoryCapacity(int capacity) {
        if (capacity > 0) {
            while (_historylist.size() > capacity) {
                _historylist.take();
            }
        }
        _historycapacity = capacity;
    }

    /** Return the number of objects in the queue.
     */
    public int size() {
        return _queuelist.size();
    }

    /** Take the oldest object off the queue and return it.
     *  If there is no such object on the queue (the queue is empty),
     *  then return null.  If the history mechanism is enabled,
     *  then put the taken object on the history queue.  If the capacity
     *  of the history queue would be exceeded by this, then first remove
     *  the oldest object on that queue.
     *  @return An object from the queue.
     */
    public Object take() {
        if (_queuelist.size() > 0) {
            // Ignore the exception since we ensure it can't occur.
            try {
                Object obj = _queuelist.take();
                if (_historycapacity != 0) {
                    if (_historycapacity == _historylist.size()) {
                        _historylist.take();
                    }
                    _historylist.insertLast(obj);
                }
                return obj;
            } catch (NoSuchElementException ex) {
                // Does not happen...
                return null;
            }
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container, if there is one.
    private Nameable _container = null;

    // The capacity of the queue, defaulting to infinite.
    private int _queuecapacity = -1;

    // The list of objects currently in the queue.
    private LinkedList _queuelist;

    // The capacity of the history queue, defaulting to zero.
    private int _historycapacity = 0;

    // The list of objects recently removed from the queue.
    private LinkedList _historylist = null;
}
