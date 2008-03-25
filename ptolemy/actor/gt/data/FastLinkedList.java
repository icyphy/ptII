/* Linked list that provides efficient add and remove functions.

@Copyright (c) 1997-2008 The Regents of the University of California.
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

import java.util.Collection;
import java.util.Iterator;

import ptolemy.kernel.util.KernelRuntimeException;

public class FastLinkedList<E> implements Collection<E> {

    public boolean add(E element) {
        Entry entry = new Entry(this, element);
        addEntryToTail(entry);
        return true;
    }

    public boolean addAll(Collection<? extends E> collection) {
        for (E element : collection) {
            add(element);
        }
        return true;
    }

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

    public void clear() {
        _head = _tail = null;
        _size = 0;
        _recalculateSize = false;
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object element) {
        return findEntry((E) element) != null;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    public Entry findEntry(E element) {
        Entry entry = _head;
        while (entry != null) {
            if (entry._value == element || entry._value.equals(element)) {
                return entry;
            }
            entry = entry._next;
        }
        return null;
    }

    public Entry getHead() {
        return _head;
    }

    public Entry getTail() {
        return _tail;
    }

    public boolean isEmpty() {
        return _head == null;
    }

    public Iterator<E> iterator() {
        // FIXME: Implement later.
        throw new KernelRuntimeException("Not implemented.");
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        Entry entry = findEntry((E) o);
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

    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object element : c) {
            modified |= remove(element);
        }
        return modified;
    }

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

    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;
        Entry entry = _head;
        while (entry != null) {
            if (!collection.contains(entry._value)) {
                remove(entry);
                modified = true;
            }
            entry = entry._next;
        }
        return modified;
    }

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

    public Object[] toArray() {
        Object[] array = new Object[size()];
        Entry entry = _head;
        int i = 0;
        while (entry != null) {
            array[i++] = entry._value;
            entry = entry._next;
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public <S> S[] toArray(S[] array) {
        if (array.length < size()) {
            array = (S[]) java.lang.reflect.Array.newInstance(array.getClass()
                    .getComponentType(), size());
        }
        int i = 0;
        Entry entry = _head;
        while (entry != null) {
            array[i++] = (S) entry._value;
            entry = entry._next;
        }
        return array;
    }

    public class Entry {

        public FastLinkedList<E> getList() {
            return _list;
        }

        public Entry getNext() {
            return _next;
        }

        public Entry getPrevious() {
            return _previous;
        }

        public E getValue() {
            return _value;
        }

        public boolean hasNext() {
            return _next != null;
        }

        public boolean hasPrevious() {
            return _previous != null;
        }

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

        private Entry(FastLinkedList<E> list, E value) {
            _list = list;
            _value = value;
        }

        private FastLinkedList<E> _list;

        private Entry _next;

        private Entry _previous;

        private E _value;
    }

    private Entry _head;

    private boolean _recalculateSize;

    private int _size;

    private Entry _tail;
}
