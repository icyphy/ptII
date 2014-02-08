/* A two-way hash map where the keys are sorted.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
 A two-way hash map where the keys are sorted. The keys are sorted in the order
 in which they are added to this two-way hash map.

 @param <K> The key type.
 @param <V> The value type.
 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
@SuppressWarnings("serial")
public class SequentialTwoWayHashMap<K, V> extends TwoWayHashMap<K, V> {

    /** Clear this two-way hash map.
     */
    public void clear() {
        super.clear();
        _keySequence.clear();
    }

    /** Clone this two-way hash map and return the clone.
     *
     *  @return The clone.
     */
    public Object clone() {
        SequentialTwoWayHashMap result = (SequentialTwoWayHashMap) super
                .clone();
        result._keySequence = new LinkedList<K>(_keySequence);
        return result;
    }

    /** Return a sorted list of keys.
     *
     *  @return The list of keys.
     */
    public List<K> keys() {
        return Collections.unmodifiableList(_keySequence);
    }

    /** Put a value into this two-way hash map and associate it with a key.
     *
     *  @param key The key.
     *  @param value The value.
     *  @return The value previously associated with the key in this two-way
     *   hash map.
     */
    public V put(K key, V value) {
        if (value != null && !super.containsKey(key)) {
            _keySequence.add(key);
        }
        return super.put(key, value);
    }

    /** Remove the value associated with the given key.
     *
     *  @param key The key.
     *  @return The removed value, or null if not found.
     */
    public V remove(Object key) {
        V oldValue = super.remove(key);
        if (oldValue != null) {
            _keySequence.remove(key);
        }
        return oldValue;
    }

    /** Remove the value associated with the last key.
     */
    public void removeLast() {
        int size = _keySequence.size();
        ListIterator<K> iterator = _keySequence.listIterator(size);
        K key = iterator.previous();
        iterator.remove();
        super.remove(key);
    }

    /** Retain only the first count keys and their values, and remove the other
     *  keys and values.
     *
     *  @param count The number of key-value pairs to be retained.
     */
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

    /** The sequence of the keys, sorted in the order in which they are added to
     *  this two-way hash map.
     */
    private List<K> _keySequence = new LinkedList<K>();
}
