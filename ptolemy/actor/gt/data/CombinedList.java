/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.actor.gt.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ptolemy.kernel.util.KernelRuntimeException;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class CombinedList<E> extends CombinedCollection<E> implements List<E> {

    public CombinedList(List<? extends E> ... lists) {
        super(lists);
    }

    public void add(int index, E element) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public E get(int index) {
        Iterator<E> iterator = iterator();
        for (int i = 0; i < index && iterator.hasNext(); i++) {
            iterator.next();
        }
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public int indexOf(Object o) {
        Iterator<E> iterator = iterator();
        int i = 0;
        while (iterator.hasNext()) {
            E next = iterator.next();
            if (o == null && next == null || o != null && o.equals(next)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public ListIterator<E> listIterator() {
        throw new KernelRuntimeException("Not implemented.");
    }

    public ListIterator<E> listIterator(int index) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public E remove(int index) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public E set(int index, E element) {
        throw new KernelRuntimeException("Not implemented.");
    }

    public List<E> subList(int fromIndex, int toIndex) {
        throw new KernelRuntimeException("Not implemented.");
    }

}
