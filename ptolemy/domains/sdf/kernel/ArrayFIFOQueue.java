/* A queue with constant capacity and optional history.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)

*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.util.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ArrayFIFOQueue
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
<p>
This queue is implemented as a circular array.  When the array becomes full,
it is transparently doubled in size.

@author Steve Neuendorffer
@version $Id$
*/
public final class ArrayFIFOQueue implements Cloneable {

    /** Construct an empty queue with no container, and an infinite capacity.
     */
    public ArrayFIFOQueue() {
        _queueArray = new Object[STARTING_ARRAYSIZE];
        _queueMaxCapacity = INFINITE_CAPACITY;
        _historyList = new LinkedList();
    }

    /** Construct an empty queue with no container and the given capacity.
     */
    public ArrayFIFOQueue(int size) {
        _queueArray = new Object[size];
        _queueMaxCapacity = size;
        _historyList = new LinkedList();
    }

    /** Construct an empty queue with the specified container. The
     *  container is only used for error reporting.
     *  @param container The container of the queue.
     */
    public ArrayFIFOQueue(Nameable container) {
        this();
        _container = container;
    }

    /** Construct an empty queue with the specified container and the
     *  given size. The container is only used for error reporting.
     *  @param container The container of the queue.
     */
    public ArrayFIFOQueue(Nameable container, int size) {
        this(size);
        _container = container;
    }

    /** Copy constructor. Create a copy of the specified queue, but
     *  with no container. This is useful to permit enumerations over
     *  a queue while the queue continues to be modified. The objects
     *  in the queue themselves are not cloned.
     *  @param model The queue to be copied.
     */
    public ArrayFIFOQueue(ArrayFIFOQueue model) {
        this();
        synchronized(model) {
            _queueSize = model._queueSize;
            _queueArray = new Object[model._queueArray.length];
            _queueFront = model._queueFront;
            _queueBack = model._queueBack;
            System.arraycopy(model._queueArray, 0, _queueArray, 0,
                    _queueArray.length);
            _historyList.addAll(model._historyList);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this queue. The cloned queue has no container. The
     *  objects in the queue themselves are not cloned.
     *  @return A clone of this queue
     */
    public Object clone() {
        return new ArrayFIFOQueue(this);
    }

    /** Enumerate the objects in the queue, beginning with the oldest.
     *  @return An enumeration of objects.
     */
    public Enumeration elements() {
        return Collections.enumeration(elementList());
    }

    /** Return a list containing all the elements in the queue, beginning
     *  with the oldest.
     *  @return A list of objects
     */
    public List elementList() {
        LinkedList l = new LinkedList();
        int i;
        if((_queueFront < _queueBack)||isFull()) {
            for(i = _queueBack; (i < _queueArray.length); i++)
                l.addLast(_queueArray[i]);
            for(i = 0; (i < _queueFront); i++)
                l.addLast(_queueArray[i]);
        }
        else
            for(i = _queueBack; (i < _queueFront); i++)
                l.addLast(_queueArray[i]);
        return l;
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
    public Object get(int offset)
            throws NoSuchElementException {
        Object obj = null;
        if (offset >= 0) {
            if(offset >= size()) {
                String str = ".";
                if (_container != null) {
                    str = " contained by " + _container.getFullName();
                }
                throw new NoSuchElementException("No object at offset "
                        + offset + " in the FIFOQueue" + str);
            }
            int loc = _queueBack + offset;
            if(loc >= _queueArray.length)
                loc = loc % _queueArray.length;
            obj = _queueArray[loc];
        } else {
            try {
                obj = _historyList.get(historySize()+offset);

            } catch (Exception ex) {
                String str = ".";
                if (_container != null) {
                    str = " contained by " + _container.getFullName();
                }
                throw new NoSuchElementException("No object at offset "
                        + offset + " in the FIFOQueue" + str);
            }
        }
        return obj;
    }

    /** Return the queue capacity
     *  This will be INFINITE_CAPACITY if the capacity is infinite.
     *  @return The capacity of the queue.
     */
    public int getCapacity() {
        return _queueMaxCapacity;
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
     *  N is less than or equal to the history capacity. If the history
     *  capacity is infinite, then the enumeration includes all objects
     *  previously taken from the queue. If the history capacity is zero,
     *  then return an empty enumeration.
     *  @return An enumeration of objects in the history.
     */
    public Enumeration historyElements() {
        return Collections.enumeration(_historyList);
    }

    /** Return the number of objects in the history.
     *  @return The current number of objects in the history.
     */
    public int historySize() {
        return _historyList.size();
    }

    /** Return true if the number of objects in the queue is zero
     *  @return A boolean indicating whether the queue is empty.
     */
    public boolean isEmpty() {
        return (_queueSize == 0);
    }

    /** Return true if the number of objects in the queue equals the
     *  queue capacity.
     *  @return A boolean indicating whether the queue is full.
     */
    public boolean isFull() {
        return (_queueSize >= _queueArray.length);
    }

    /** Put an object in the queue and return true if this will not
     *  cause the capacity to be exceeded. Otherwise, do not put
     *  the object in the queue and return false.
     *  @param element An object to be put in the queue.
     *  @return A boolean indicating success.
     */
    public boolean put(Object element) {
        if (_queueArray.length - _queueSize >= 1) {
            _queueArray[_queueFront++] = element;
            if(_queueFront >= _queueArray.length)
                _queueFront = _queueFront % _queueArray.length;
            _queueSize++;
            return true;
        } else {
	    if(_queueMaxCapacity == INFINITE_CAPACITY) {
                _resizeArray(_queueArray.length * 2);
		return put(element);
	    } else
		return false;
        }
    }

    /** Put an array of objects in the queue and return true if this will not
     *  cause the capacity to be exceeded. Otherwise, do not put
     *  any of the object in the queue and return false.
     *  @param element An array of objects to be put in the queue.
     *  @return A boolean indicating success.
     */
    public boolean putArray(Object element[]) {
        if (_queueArray.length - _queueSize >= element.length) {
            int i;
            if(element.length <= (_queueArray.length - _queueFront)) {
                System.arraycopy(element, 0, _queueArray, _queueFront,
                        element.length);
                _queueFront += element.length;
                if(_queueFront >= _queueArray.length)
                    _queueFront = _queueFront % _queueArray.length;
                _queueSize += element.length;
            } else {
                System.arraycopy(element, 0, _queueArray, _queueFront,
                        _queueArray.length - _queueFront);
                System.arraycopy(element, _queueArray.length - _queueFront,
                        _queueArray, 0,
                        element.length - (_queueArray.length - _queueFront));
                _queueFront += element.length;
                if(_queueFront >= _queueArray.length)
                    _queueFront = _queueFront % _queueArray.length;
                _queueSize += element.length;
            }
            return true;
        } else {
	    if(_queueMaxCapacity == INFINITE_CAPACITY) {
		try {
		    _resizeArray(_queueArray.length * 2);
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		return putArray(element);
	    } else
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
        if (capacity == INFINITE_CAPACITY) {
            _queueMaxCapacity = INFINITE_CAPACITY;
            return;
        }
	if (capacity < -1) {
	    throw new IllegalActionException(_container,
                    "Queue Capacity cannot be negative");
        }

	if (size() > capacity) {
            throw new IllegalActionException(_container, "Queue contains " +
                    "more elements than the proposed capacity.");
        }
        _queueMaxCapacity = capacity;
 	_resizeArray(capacity);
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
                _historyList.removeFirst();;
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
        return _queueSize;
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
            if(isEmpty()) throw new NoSuchElementException("Empty Queue");
            obj = _queueArray[_queueBack];
            _queueArray[_queueBack] = null;
            _queueBack++;
            if(_queueBack >= _queueArray.length)
                _queueBack = _queueBack % _queueArray.length;
            _queueSize--;
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
                _historyList.removeFirst();;
            }
            _historyList.addLast(obj);
        }
        return obj;
    }

    /** Remove the count oldest objects from the queue and return them.
     *  If there is no such object in the queue (the queue is empty),
     *  throw an exception. If the history mechanism is enabled,
     *  then put the taken object in the history queue. If the capacity
     *  of the history queue would be exceeded by this, then first remove
     *  the oldest object in the history queue.
     *  @return An array of objects from the queue.
     *  @exception NoSuchElementException If the queue is empty.
     */
    public void takeArray(Object obj[]) throws NoSuchElementException {
        int count = obj.length;
        if(size() < count) {
            String str = "";
            if (_container != null) {
                str = " contained by " + _container.getFullName();
            }
            throw new NoSuchElementException("The FIFOQueue" + str
                    + " does not contain enough elements!");
        }

        if(count <= (_queueArray.length - _queueBack)) {
            System.arraycopy(_queueArray, _queueBack, obj, 0,
                    count);
        } else {
            System.arraycopy(_queueArray, _queueBack, obj, 0,
                    _queueArray.length - _queueBack);
            System.arraycopy(_queueArray, 0,
                    obj, _queueArray.length - _queueBack,
                    count - (_queueArray.length - _queueBack));
        }
        _queueBack += count;
        if(_queueBack >= _queueArray.length)
            _queueBack = _queueBack % _queueArray.length;
        _queueSize -= count;
        if (_historyCapacity != 0) {
            if (_historyCapacity == _historyList.size()) {
                _historyList.removeFirst();;
            }
            _historyList.addLast(obj);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Used to indicate that the size of the queue or the history
     *  queue is infinite.
     */
    public static final int INFINITE_CAPACITY = -1;

    /**
     * The default capacity of the queue.
     */
    public static final int DEFAULT_CAPACITY = INFINITE_CAPACITY;

    /**
     * The starting size of the circular buffer, if the capacity is
     * infinite.
     */
    public static final int STARTING_ARRAYSIZE = 4;

    /**
     * The default capacity of the history queue.
     */
    public static final int DEFAULT_HISTORY_CAPACITY = 0;


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Resize the internal circular array to have the given size.
     * @exception InternalErrorException If the proposed size is greater than
     *   the declared maximum size, or if the queue contains more
     *   objects than the proposed size or the proposed size
     *   is illegal. .
     */
    private void _resizeArray(int newsize) {
	if (newsize < 0) {
	    throw new InternalErrorException(
                    "Buffer size of " + newsize +
                    " is not greater than zero.");
        }

	if (size() > newsize) {
            throw new InternalErrorException("Queue contains " +
                    "more elements than the proposed array size.");
        }

	if((_queueMaxCapacity != INFINITE_CAPACITY)&&
                (newsize > _queueMaxCapacity)) {
	    throw new InternalErrorException("The proposed" +
                    " array size exceeds the maximum declared queue size.");
	}

	Object newarray[] = new Object[newsize];
        if((_queueFront < _queueBack) || isFull()) {
            System.arraycopy(_queueArray, _queueBack,
                    newarray, 0, _queueArray.length - _queueBack);
            System.arraycopy(_queueArray, 0,
                    newarray, _queueArray.length - _queueBack,
                    _queueFront);
            _queueFront = _queueArray.length - _queueBack + _queueFront;
        } else {
            System.arraycopy(_queueArray, _queueBack,
                    newarray, 0, _queueFront - _queueBack);
            _queueFront = _queueFront - _queueBack;
        }
        _queueArray = newarray;
        _queueBack = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container, if there is one.
    private Nameable _container = null;

    // The maximum capacity of the queue
    private int _queueMaxCapacity = INFINITE_CAPACITY;

    // The list of objects currently in the queue.
    private Object _queueArray[];

    // The location of the next place to insert in _queueArray
    private int _queueFront = 0;

    // The location of the next place to remove from _queueArray
    private int _queueBack = 0;

    // The number of elements in the queue.
    private int _queueSize = 0;

    // The capacity of the history queue, defaulting to zero.
    private int _historyCapacity = DEFAULT_HISTORY_CAPACITY;

    // The list of objects recently removed from the queue.
    private LinkedList _historyList = null;

}
