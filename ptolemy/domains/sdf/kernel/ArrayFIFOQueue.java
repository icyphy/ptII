/* A queue with constant capacity and optional history.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.util.*;

import collections.LinkedList;
import collections.CollectionEnumeration;
import java.util.NoSuchElementException;

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

    /** Construct an empty queue with no container.
     */
    public ArrayFIFOQueue() {
        _queuearray = new Object[DEFAULT_CAPACITY];
        _queuecapacity = DEFAULT_CAPACITY;
        _historylist = new LinkedList();
    }

    /** Construct an empty queue with no container and the given size.
     */
    public ArrayFIFOQueue(int size) {
        _queuearray = new Object[size];
        _queuecapacity = size;
        _historylist = new LinkedList();
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
            _queuesize = model._queuesize;
            _queuecapacity = model._queuecapacity;
            _queuearray = new Object[_queuecapacity];
            _queuefront = model._queuefront;
            _queueback = model._queueback;
            System.arraycopy(model._queuearray, 0, _queuearray, 0, 
                    _queuecapacity);
            _historylist.appendElements(model.historyElements());
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
     *  @see collections.LinkedList#elements()
     */
    public CollectionEnumeration elements() {
        LinkedList l = new LinkedList();
        int i;
        if((_queuefront < _queueback)||isFull()) {
            for(i = _queueback; (i < _queuecapacity); i++) 
                l.insertLast(_queuearray[i]);
            for(i = 0; (i < _queuefront); i++)
                l.insertLast(_queuearray[i]);
        }
        else
            for(i = _queueback; (i < _queuefront); i++)
                l.insertLast(_queuearray[i]);
        return l.elements();
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
        try {
            if (offset >= 0) {
                int loc = _queueback + offset;
                if(loc > _queuefront)
                    throw new NoSuchElementException("bad offset");
                else if(loc >= _queuecapacity)
                    loc = loc % _queuecapacity;                    
                obj = _queuearray[loc];
            } else {
                obj = _historylist.at(historySize()+offset);
            }
        } catch (NoSuchElementException ex) {
            String str = ".";
            if (_container != null) {
                str = " contained by " + _container.getFullName();
            }
            throw new NoSuchElementException("No object at offset "
                    + offset + " in the FIFOQueue" + str);
        }
        return obj;
    }

    /** Return the queue capacity
     *  @return The capacity of the queue.
     */
    public int getCapacity() {
        return _queuecapacity;
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
        return _historycapacity;
    }

    /** Enumerate the objects in the history, which are the N most recent
     *  objects taken from the queue, beginning with the oldest, where
     *  N is less than or equal to the history capacity. If the history
     *  capacity is infinite, then the enumeration includes all objects
     *  previously taken from the queue. If the history capacity is zero,
     *  then return an empty enumeration.
     *  @return An enumeration of objects in the history.
     *  @see collections.LinkedList#elements()
     */
    public CollectionEnumeration historyElements() {
        return _historylist.elements();
    }

    /** Return the number of objects in the history.
     *  @return The current number of objects in the history.
     */
    public int historySize() {
        return _historylist.size();
    }

    /** Return true if the number of objects in the queue is zero
     *  @return A boolean indicating whether the queue is empty.
     */
    public boolean isEmpty() {
        return (_queuesize == 0);
    }

    /** Return true if the number of objects in the queue equals the
     *  queue capacity.
     *  @return A boolean indicating whether the queue is full.
     */
    public boolean isFull() {
        return (_queuesize >= _queuecapacity);
    }

    /** Put an object in the queue and return true if this will not
     *  cause the capacity to be exceeded. Otherwise, do not put
     *  the object in the queue and return false.
     *  @param element An object to be put in the queue.
     *  @return A boolean indicating success.
     */
    public boolean put(Object element) {
        if (!isFull()) {
            _queuearray[_queuefront++] = element;
            if(_queuefront >= _queuecapacity) 
                _queuefront = _queuefront % _queuecapacity;
            _queuesize++;
            return true;
        } else {
            Object newqueue[] = new Object[_queuecapacity * 2];
            System.arraycopy(_queuearray, 0, newqueue, 0, _queuecapacity);
            _queuearray = newqueue;
            _queuecapacity = _queuecapacity * 2;
            return put(element);
        }
    }

    /** Put an array of objects in the queue and return true if this will not
     *  cause the capacity to be exceeded. Otherwise, do not put
     *  the object in the queue and return false.
     *  @param element An array of objects to be put in the queue.
     *  @return A boolean indicating success.
     */
    public boolean put(Object element[]) {
        if (_queuecapacity - _queuesize >= element.length) {
            int i;
            if(element.length <= (_queuecapacity - _queuefront)) {
                System.arraycopy(element, 0, _queuearray, _queuefront, 
                        element.length);
                _queuefront += element.length;
                if(_queuefront >= _queuecapacity) 
                    _queuefront = _queuefront % _queuecapacity;
                _queuesize += element.length;
            } else {
                System.arraycopy(element, 0, _queuearray, _queuefront, 
                        _queuecapacity - _queuefront);
                System.arraycopy(element, _queuecapacity - _queuefront,
                        _queuearray, 0, 
                        element.length - (_queuecapacity - _queuefront));
                _queuefront += element.length;
                if(_queuefront >= _queuecapacity) 
                    _queuefront = _queuefront % _queuecapacity;
                _queuesize += element.length;
            }  
                /*
            for(i = 0; i < element.length; i++) {
                _queuearray[_queuefront++] = element[i];
                if(_queuefront >= _queuecapacity) 
                    _queuefront = _queuefront % _queuecapacity;
                _queuesize++;
                }*/
            return true;
        } else {
            Object newqueue[] = new Object[_queuecapacity * 2];
            System.arraycopy(_queuearray, 0, newqueue, 0, _queuecapacity);
            _queuearray = newqueue;
            _queuecapacity = _queuecapacity * 2;
            return put(element);
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
        if (capacity < 0) {
            throw new IllegalActionException(_container,
                    "Cannot set queue capacity to " + capacity);
        }
        if (size() > capacity) {
            throw new IllegalActionException(_container,
                    "Queue contains more elements than the proposed capacity.");
        }
        Object newarray[] = new Object[capacity];
        if((_queuefront < _queueback)||isFull()) {
            System.arraycopy(_queuearray, _queueback,
                    newarray, 0, _queuecapacity - _queueback);
            System.arraycopy(_queuearray, 0,
                    newarray, _queuecapacity - _queueback,
                    _queuefront);
            _queuefront = _queuecapacity - _queueback + _queuefront;
        } else {
            System.arraycopy(_queuearray, _queueback,
                    newarray, 0, _queuefront - _queueback);
            _queuefront = _queuefront - _queueback;
        }
        _queuecapacity = capacity;
        _queuearray = newarray;
        _queueback = 0;
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
            while (_historylist.size() > capacity) {
                _historylist.take();
            }
        } else if (capacity == 0) {
            _historylist.clear();
        } else if (capacity != INFINITE_CAPACITY) {
            throw new IllegalActionException(_container,
                    "Cannot set history capacity to " + capacity);
        }
        _historycapacity = capacity;
    }

    /** Return the number of objects in the queue.
     *  @return The number of objects in the queue.
     */
    public int size() {
        return _queuesize;
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
            obj = _queuearray[_queueback];
            _queuearray[_queueback] = null;
            _queueback++;
            if(_queueback >= _queuecapacity) 
                _queueback = _queueback % _queuecapacity;
            _queuesize--;
        } catch (NoSuchElementException ex) {
            String str = "";
            if (_container != null) {
                str = " contained by " + _container.getFullName();
            }
            throw new NoSuchElementException("The FIFOQueue" + str
                    + " is empty!");
        }
        if (_historycapacity != 0) {
            if (_historycapacity == _historylist.size()) {
                _historylist.take();
            }
            _historylist.insertLast(obj);
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
    public Object[] take(int count) throws NoSuchElementException {
        Object obj[] = null;
        try {
            if(_queuesize < count) 
                throw new NoSuchElementException("Empty Queue");
            obj = new Object[count];
            
            if(count <= (_queuecapacity - _queueback)) {
                System.arraycopy(_queuearray, _queueback, obj, 0,
                        count);
            } else {
                System.arraycopy(_queuearray, _queueback, obj, 0,
                        _queuecapacity - _queueback);
                System.arraycopy(_queuearray, 0,
                        obj, _queuecapacity - _queueback,
                        count - (_queuecapacity - _queueback));
            }
            _queueback += count;
            if(_queueback >= _queuecapacity) 
                _queueback = _queueback % _queuecapacity;
            _queuesize -= count;
            
            /*
              for(i = 0; i < count; i++) {
                obj[i] = _queuearray[_queueback];
                _queuearray[_queueback] = null;
                _queueback++;
                if(_queueback >= _queuecapacity) 
                    _queueback = _queueback % _queuecapacity;
                _queuesize--;
            }
             */
        } catch (NoSuchElementException ex) {
            String str = "";
            if (_container != null) {
                str = " contained by " + _container.getFullName();
            }
            throw new NoSuchElementException("The FIFOQueue" + str
                    + " is empty!");
        }
        if (_historycapacity != 0) {
            if (_historycapacity == _historylist.size()) {
                _historylist.take();
            }
            _historylist.insertLast(obj);
        }
        return obj;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Used to indicate that the size of the queue or the history
     *  queue is infinite.
     */
    public static final int INFINITE_CAPACITY = -1;
    
    /**
     * the default capacity of the queue.
     */
    public static final int DEFAULT_CAPACITY = 10;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container, if there is one.
    private Nameable _container = null;

    // The capacity of the queue.
    private int _queuecapacity = DEFAULT_CAPACITY;

    // The list of objects currently in the queue.
    private Object _queuearray[];
    
    // The location of the next place to insert in _queuearray
    private int _queuefront = 0;
    
    // The location of the next place to remove from _queuearray
    private int _queueback = 0;

    // The number of elements in the queue.
    private int _queuesize = 0;

    // The capacity of the history queue, defaulting to zero.
    private int _historycapacity = 0;

    // The list of objects recently removed from the queue.
    private LinkedList _historylist = null;

}










