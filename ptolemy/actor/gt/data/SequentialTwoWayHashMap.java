/*

 Copyright (c) 2008 The Regents of the University of California.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

//////////////////////////////////////////////////////////////////////////
//// SequentialTwoWayHashMap

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SequentialTwoWayHashMap<K, V> extends TwoWayHashMap<K, V> {

    public void clear() {
        super.clear();
        _keySequence.clear();
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        SequentialTwoWayHashMap result =
            (SequentialTwoWayHashMap) super.clone();
        result._keySequence = new LinkedList<K>(_keySequence);
        return result;
    }

    public List<K> keys() {
        return Collections.unmodifiableList(_keySequence);
    }

    public V put(K key, V value) {
        if (value != null && !super.containsKey(key)) {
            _keySequence.add(key);
        }
        return super.put(key, value);
    }

    public V remove(Object key) {
        V oldValue = super.remove(key);
        if (oldValue != null) {
            _keySequence.remove(key);
        }
        return oldValue;
    }

    public void removeLast() {
        int size = _keySequence.size();
        ListIterator<K> iterator = _keySequence.listIterator(size);
        K key = iterator.previous();
        iterator.remove();
        super.remove(key);
    }

    public void retain(int count) {
        int size = _keySequence.size();
        if (size > count) {
            ListIterator<K> iterator = _keySequence.listIterator(size);
            for (; size > count; size--) {
                K key = iterator.previous();
                iterator.remove();
                super.remove(key);
            }
        }
    }

    private List<K> _keySequence = new LinkedList<K>();
}
