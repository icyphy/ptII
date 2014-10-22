/* A linked list that provides efficient add and removal functions.

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt.data;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import ptolemy.kernel.util.KernelRuntimeException;

///////////////////////////////////////////////////////////////////
//// FastLinkedList

/**
 A linked list that provides efficient add and removal functions.

 @param <E> The element type of this linked list.
 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class FastLinkedList<E> implements Collection<E> {

    /** Add an element to the end of this linked list.
     *
     *  @param element The element to be added.
     *  @return Always true.
     */
    @Override
    public boolean add(E element) {
        Entry entry = new Entry(this, element);
        addEntryToTail(entry);
        return true;
    }

    /** Add all the elements of the given collection to the end of this linked
     *  list.
     *
     *  @param collection The collection.
     *  @return Always true.
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E element : collection) {
            add(element);
        }
        return true;
    }

    /** Add an entry after previousEntry, or add the entry to the head if
     *  previousEntry is null.
     *
     *  @param entry The entry to be added.
     *  @param previousEntry The previous entry.
     */
    public void addEntryAfter(Entry entry, Entry previousEntry) {
        if (previousEntry == null) {
            addEntryToHead(entry);
        } else {
            entry._previous = previousEntry;
            entry._next = previousEntry._next;
            entry._list = this;
            previousEntry._next = entry;
            if (entry._next != null) {
                entry._next._previous = entry;
            } else {
                _tail = entry;
            }
            _size++;
        }
    }

    /** Add an entry before nextEntry, or add the entry to the tail if nextEntry
     *  is null.
     *
     *  @param entry The entry to be added.
     *  @param nextEntry The next entry.
     */
    public void addEntryBefore(Entry entry, Entry nextEntry) {
        if (nextEntry == null) {
            addEntryToTail(entry);
        } else {
            entry._next = nextEntry;
            entry._previous = nextEntry._previous;
            entry._list = this;
            nextEntry._previous = entry;
            if (entry._previous != null) {
                entry._previous._next = entry;
            } else {
                _head = entry;
            }
            _size++;
        }
    }

    /** Add an entry to the head of this linked list.
     *
     *  @param entry The entry to be added.
     */
    public void addEntryToHead(Entry entry) {
        entry._previous = null;
        entry._next = _head;
        if (_head == null) {
            _head = _tail = entry;
        } else {
            _head._previous = entry;
            _head = entry;
        }
        _size++;
    }

    /** Add an entry to the tail of this linked list.
     *
     *  @param entry The entry to be added.
     */
    public void addEntryToTail(Entry entry) {
        entry._previous = _tail;
        entry._next = null;
        entry._list = this;
        if (_head == null) {
            _head = _tail = entry;
        } else {
            _tail._next = entry;
            _tail = entry;
        }
        _size++;
    }

    /** Clear this linked list.
     */
    @Override
    public void clear() {
        _head = _tail = null;
        _size = 0;
        _recalculateSize = false;
    }

    /** Test whether this linked list has the given element in an entry.
     *
     *  @param element The element.
     *  @return true if the element is found.
     */
    @Override
    public boolean contains(Object element) {
        return findEntry((E) element) != null;
    }

    /** Test whether this linked list has all the elements of the given
     *  collection.
     *
     *  @param collection The collection.
     *  @return true if all the elements are found.
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object element : collection) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    /** Find an entry with the given element and return it. Return null if not
     *  found.
     *
     *  @param element The element.
     *  @return The entry.
     */
    public Entry findEntry(E element) {
        Entry entry = _head;
        while (entry != null) {
            if (entry._element == element || entry._element.equals(element)) {
                return entry;
            }
            entry = entry._next;
        }
        return null;
    }

    /** Get the head entry.
     *
     *  @return The head entry.
     */
    public Entry getHead() {
        return _head;
    }

    /** Get the tail entry.
     *
     *  @return The tail entry.
     */
    public Entry getTail() {
        return _tail;
    }

    /** Test whether this collection is empty.
     *
     *  @return true if this collection is empty.
     */
    @Override
    public boolean isEmpty() {
        return _head == null;
    }

    /** Not implemented.
     *
     *  @return The iterator to iterate elements in this linked list.
     */
    @Override
    public Iterator<E> iterator() {
        throw new KernelRuntimeException("Not implemented.");
    }

    /** Remove the first entry that has the given element.
     *
     *  @param element The element.
     *  @return true if an entry is removed, or false if an entry cannot be
     *   found.
     */
    @Override
    public boolean remove(Object element) {
        Entry entry = findEntry((E) element);
        if (entry == null) {
            return false;
        } else {
            if (entry == _head) {
                _head = entry._next;
            }
            if (entry == _tail) {
                _tail = entry._previous;
            }
            if (entry._previous != null) {
                entry._previous._next = entry._next;
            }
            if (entry._next != null) {
                entry._next._previous = entry._previous;
            }
            _size--;
            return true;
        }
    }

    /** Remove all the elements of the collection from this linked list.
     *
     *  @param collection The collection.
     *  @return true if this linked list is altered, or false if no entry is
     *   removed.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean modified = false;
        for (Object element : collection) {
            modified |= remove(element);
        }
        return modified;
    }

    /** Remove all entries after the given entry.
     *
     *  @param entry The entry, which must be contained in this linked list.
     *  @return true if this linked list is altered, or false if no entry is
     *   removed.
     */
    public boolean removeAllAfter(Entry entry) {
        if (entry == _tail) {
            return false;
        } else {
            if (entry == null) {
                if (isEmpty()) {
                    return false;
                } else {
                    clear();
                    return true;
                }
            } else {
                entry._next = null;
                _tail = entry;
                _recalculateSize = true;
                return true;
            }
        }
    }

    /** Remove all entries before the given entry.
     *
     *  @param entry The entry, which must be contained in this linked list.
     *  @return true if this linked list is altered, or false if no entry is
     *   removed.
     */
    public boolean removeAllBefore(Entry entry) {
        if (entry == _head) {
            return false;
        } else {
            if (entry == null) {
                if (isEmpty()) {
                    return false;
                } else {
                    clear();
                    return true;
                }
            } else {
                entry._previous = null;
                _head = entry;
                _recalculateSize = true;
                return true;
            }
        }
    }

    /** Retain all elements of the given collection, but remove entries whose
     *  elements are not in the collection.
     *
     *  @param collection The collection.
     *  @return true if this linked list is altered, or false if no entry is
     *   removed.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;
        Entry entry = _head;
        while (entry != null) {
            if (!collection.contains(entry._element)) {
                remove(entry);
                modified = true;
            }
            entry = entry._next;
        }
        return modified;
    }

    /** Get the size of this linked list.
     *
     *  @return The size.
     */
    @Override
    public int size() {
        if (_recalculateSize) {
            _size = 0;
            Entry entry = _head;
            while (entry != null) {
                _size++;
                entry = entry._next;
            }
        }
        return _size;
    }

    /** Return an array that contains all the elements in this linked list.
     *
     *  @return The array.
     */
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        Entry entry = _head;
        int i = 0;
        while (entry != null) {
            array[i++] = entry._element;
            entry = entry._next;
        }
        return array;
    }

    /** Store all the elements in this linked list into the given array if its
     *  size is enough for the storage, or create a new array of the same type
     *  as the given array for the storage and return it.
     *
     *  @param <T> The element type of the array.
     *  @param array The array.
     *  @return The given array, or a new array if the given array is not big
     *   enough.
     */
    @Override
    public <T> T[] toArray(T[] array) {
        if (array.length < size()) {
            array = (T[]) Array.newInstance(
                    array.getClass().getComponentType(), size());
        }
        int i = 0;
        Entry entry = _head;
        while (entry != null) {
            array[i++] = (T) entry._element;
            entry = entry._next;
        }
        return array;
    }

    ///////////////////////////////////////////////////////////////////
    //// Entry

    /**
     An entry in this linked list that contains an element.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public class Entry {

        /** Get the element in this entry.
         *
         *  @return The element.
         */
        public E getElement() {
            return _element;
        }

        /** Get the linked list that contains this entry.
         *
         *  @return The linked list.
         */
        public FastLinkedList<E> getList() {
            return _list;
        }

        /** Get the next entry.
         *
         *  @return The next entry, or null if this entry is at the tail.
         */
        public Entry getNext() {
            return _next;
        }

        /** Get the previous entry.
         *
         *  @return The next entry, or null if this entry is at the head.
         */
        public Entry getPrevious() {
            return _previous;
        }

        /** Test whether there is a next entry.
         *
         *  @return true if there is a next entry.
         */
        public boolean hasNext() {
            return _next != null;
        }

        /** Test whether there is a previous entry.
         *
         *  @return true if there is a previous entry.
         */
        public boolean hasPrevious() {
            return _previous != null;
        }

        /** Remove this entry from the linked list that contains it.
         */
        public void remove() {
            if (_previous != null) {
                _previous._next = _next;
            } else {
                _list._head = _next;
            }
            if (_next != null) {
                _next._previous = _previous;
            } else {
                _list._tail = _previous;
            }
            _list._size--;
        }

        /** Construct an entry in a linked list with an element.
         *
         *  @param list The linked list.
         *  @param element The element.
         */
        private Entry(FastLinkedList<E> list, E element) {
            _list = list;
            _element = element;
        }

        /** The element.
         */
        private E _element;

        /** The linked list containing this entry.
         */
        private FastLinkedList<E> _list;

        /** The next entry, or null.
         */
        private Entry _next;

        /** The previous entry, or null.
         */
        private Entry _previous;
    }

    /** The head of this linked list.
     */
    private Entry _head;

    /** Whether the _size field is an inaccurate size of this linked list.
     */
    private boolean _recalculateSize;

    /** The size of this linked list.
     */
    private int _size;

    /** The tail of this linked list.
     */
    private Entry _tail;
}
