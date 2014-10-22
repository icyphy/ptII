/* A collection that is the combination of one or more collections.

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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import ptolemy.kernel.util.KernelRuntimeException;

///////////////////////////////////////////////////////////////////
//// CombinedCollection

/**
  A collection that is the combination of one or more collections. This is a
  more efficient implementation that copying all the elements of those
  collections into a new collection and using that collection. No copying is
  necessary because the component collections are maintained locally and their
  elements are retrieved only when they are used.

  @param <E> The type of elements in the collection.
  @author Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 8.0
  @see ptolemy.vergil.actor.ActorGraphFrame
  @Pt.ProposedRating Yellow (tfeng)
  @Pt.AcceptedRating Red (tfeng)
 */
public class CombinedCollection<E> implements Collection<E> {

    /** Construct a combined collection with no collection as its component.
     */
    public CombinedCollection() {
    }

    /** Construct a combined collection with one or more collections as its
     *  components. The order of those components coincides with the order of
     *  the elements of this combined collection.
     *
     *  @param collections The collections.
     */
    public CombinedCollection(Collection<? extends E>... collections) {
        for (Collection<? extends E> collection : collections) {
            _collectionList.add(collection);
        }
    }

    /** Add an element to the end of this collection. Component collections are
     *  not modified because an extra collection is added to the end that allows
     *  to add elements to.
     *
     *  @param element The new element.
     *  @return Always true.
     */
    @Override
    public boolean add(E element) {
        Collection collection;
        if (_isLastListModifiable) {
            collection = _collectionList.get(_collectionList.size() - 1);
        } else {
            collection = new LinkedList<Collection<?>>();
            _collectionList.add(collection);
            _isLastListModifiable = true;
        }
        collection.add(element);
        return true;
    }

    /** Add all elements of the provided collection to this collection. The
     *  given collection is added as a component of this collection and no
     *  copying is involved.
     *
     *  @param collection The collection to be added.
     *  @return Always true.
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        _collectionList.add(collection);
        _isLastListModifiable = false;
        return true;
    }

    /** Clear this collection by removing all its components.
     */
    @Override
    public void clear() {
        _collectionList.clear();
    }

    /** Test whether this collection contains the given element.
     *
     *  @param element The element.
     *  @return true if the element is contained in this collection.
     */
    @Override
    public boolean contains(Object element) {
        for (Collection<? extends E> collection : _collectionList) {
            if (collection.contains(element)) {
                return true;
            }
        }
        return false;
    }

    /** Test whether this collection contains all the elements of the given
     *  collection.
     *
     *  @param collection The collection.
     *  @return true if all the elements are contained in this collection.
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object object : collection) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    /** Test whether this collection is empty.
     *
     *  @return true if it is empty.
     */
    @Override
    public boolean isEmpty() {
        for (Collection<? extends E> collection : _collectionList) {
            if (!collection.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Return an iterator to iterate all the elements in this collection.
     *
     *  @return The iterator.
     */
    @Override
    public java.util.Iterator<E> iterator() {
        return new Iterator();
    }

    /** Throw a runtime exception because removal is not supported.
     *
     *  @param element The element to be removed.
     *  @return None.
     */
    @Override
    public boolean remove(Object element) {
        throw new KernelRuntimeException("Not implemented.");
    }

    /** Throw a runtime exception because removal is not supported.
     *
     *  @param collection The collection whose elements are to be removed.
     *  @return None.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new KernelRuntimeException("Not implemented.");
    }

    /** Throw a runtime exception because removal is not supported.
     *
     *  @param collection The collection whose elements are to be retained.
     *  @return None.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new KernelRuntimeException("Not implemented.");
    }

    /** Return size of this collection.
     *
     *  @return The size.
     */
    @Override
    public int size() {
        int size = 0;
        for (Collection<? extends E> collection : _collectionList) {
            size += collection.size();
        }
        return size;
    }

    /** Return an array that contains all the elements in this collection.
     *
     *  @return The array.
     */
    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /** Store all the elements in this collection into the given array if its
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
        int size = size();
        if (array.length < size) {
            array = (T[]) Array.newInstance(
                    array.getClass().getComponentType(), size);
        }

        int i = 0;
        Object[] result = array;
        for (Collection<? extends E> collection : _collectionList) {
            for (E object : collection) {
                result[i++] = object;
            }
        }

        if (array.length > size) {
            array[size] = null;
        }

        return array;
    }

    /** Get the list containing all the component collections. This list cannot
     *  be modified.
     *
     *  @return The list containing all the component collections.
     */
    protected List<Collection<? extends E>> _getCollectionList() {
        return Collections.unmodifiableList(_collectionList);
    }

    /** The list containing all the component collections.
     */
    private List<Collection<? extends E>> _collectionList = new LinkedList<Collection<? extends E>>();

    /** Whether the last component collection in the list can be modified (i.e.,
     *  it is not given by the user but is created for storing new elements.
     */
    private boolean _isLastListModifiable = false;

    ///////////////////////////////////////////////////////////////////
    //// Iterator

    /**
     The iterator for iterating elements in this collection.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class Iterator implements java.util.Iterator<E> {

        /** Test whether there is a next element.
         *
         *  @return true if there is a next element.
         */
        @Override
        public boolean hasNext() {
            return _elementIterator != null;
        }

        /** Return the next element, if any, or throw a {@link
         *  NoSuchElementException} if the end of the collection has already
         *  been reached.
         *
         *  @return The next element.
         */
        @Override
        public E next() {
            E next = _elementIterator.next();
            _ensureNext();
            return next;
        }

        /** Throw a runtime exception because removal is not supported.
         */
        @Override
        public void remove() {
            throw new KernelRuntimeException("Not implemented.");
        }

        /** Construct an iterator.
         */
        Iterator() {
            _collectionIterator = _collectionList.iterator();
            _ensureNext();
        }

        /** Move the pointer to the next element in the collection.
         */
        private void _ensureNext() {
            if (_elementIterator == null || !_elementIterator.hasNext()) {
                _elementIterator = null;
                while (_collectionIterator.hasNext()) {
                    Collection<? extends E> collection = _collectionIterator
                            .next();
                    if (!collection.isEmpty()) {
                        _elementIterator = collection.iterator();
                        break;
                    }
                }
            }
        }

        /** The iterator to iterate the component collections.
         */
        private java.util.Iterator<Collection<? extends E>> _collectionIterator;

        /** The iterator to iterate the elements of the chosen component
         *  collection.
         */
        private java.util.Iterator<? extends E> _elementIterator;
    }
}
