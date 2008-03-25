/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.KernelRuntimeException;

//////////////////////////////////////////////////////////////////////////
////GTRuleGraphFrame

/**

  @author Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 6.1
  @see ptolemy.vergil.actor.ActorGraphFrame
  @Pt.ProposedRating Red (tfeng)
  @Pt.AcceptedRating Red (tfeng)
*/
public class CombinedCollection<E> implements Collection<E> {

    public CombinedCollection() {
    }

    public CombinedCollection(Collection<? extends E>... collections) {
        for (Collection<? extends E> collection : collections) {
            _collectionList.add(collection);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean add(E o) {
        Collection collection;
        if (_isLastListModifiable) {
            collection = _collectionList.get(_collectionList.size() - 1);
        } else {
            collection = new LinkedList<Collection<?>>();
            _collectionList.add(collection);
            _isLastListModifiable = true;
        }
        collection.add(o);
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        _collectionList.add(c);
        _isLastListModifiable = false;
        return true;
    }

    public void clear() {
        _collectionList.clear();
    }

    public boolean contains(Object o) {
        for (Collection<? extends E> collection : _collectionList) {
            if (collection.contains(o)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object object : c) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        for (Collection<? extends E> collection : _collectionList) {
            if (!collection.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public java.util.Iterator<E> iterator() {
        return new Iterator();
    }

    public boolean remove(Object o) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public boolean removeAll(Collection<?> c) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public boolean retainAll(Collection<?> c) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public int size() {
        int size = 0;
        for (Collection<? extends E> collection : _collectionList) {
            size += collection.size();
        }
        return size;
    }

    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        int size = size();
        if (array.length < size) {
            array = (T[]) java.lang.reflect.Array.newInstance(array.getClass()
                    .getComponentType(), size);
        }

        int i = 0;
        Object[] result = array;
        for (Collection<? extends E> collection : _collectionList) {
            for (E object : collection) {
                result[i++] = object;
            }
        }

        if (array.length > size)
            array[size] = null;

        return array;
    }

    protected List<Collection<? extends E>> _getCollectionList() {
        return _collectionList;
    }

    private List<Collection<? extends E>> _collectionList = new LinkedList<Collection<? extends E>>();

    private boolean _isLastListModifiable = false;

    private class Iterator implements java.util.Iterator<E> {

        public boolean hasNext() {
            return _objectIterator != null;
        }

        public E next() {
            E next = _objectIterator.next();
            _ensureNext();
            return next;
        }

        public void remove() {
            throw new KernelRuntimeException("Not implemented.");
        }

        Iterator() {
            _collectionIterator = _collectionList.iterator();
            _ensureNext();
        }

        private void _ensureNext() {
            if (_objectIterator == null || !_objectIterator.hasNext()) {
                _objectIterator = null;
                while (_collectionIterator.hasNext()) {
                    Collection<? extends E> collection = _collectionIterator
                            .next();
                    if (!collection.isEmpty()) {
                        _objectIterator = collection.iterator();
                        break;
                    }
                }
            }
        }

        private java.util.Iterator<Collection<? extends E>> _collectionIterator;

        private java.util.Iterator<? extends E> _objectIterator;
    }

}
