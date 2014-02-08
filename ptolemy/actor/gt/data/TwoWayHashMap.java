/* A hash map that has one-to-one relationship between keys and values.

@Copyright (c) 2007-2009 The Regents of the University of California.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 A hash map that has one-to-one relationship between keys and values. With the
 implementation of two hash maps instead of one, the lookup for keys with values
 is just as efficient as the lookup for values with keys.

 @param <K> The key type.
 @param <V> The value type.
 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
@SuppressWarnings("serial")
public class TwoWayHashMap<K, V> extends HashMap<K, V> {

    /** Construct an empty two-way hash map.
     */
    public TwoWayHashMap() {
        _reverseMap = new HashMap<V, K>();
    }

    /** Construct a two-way hash map with the given hash map, which must have a
     *  one-to-one relationship between elements.
     *
     *  @param map The hash map.
     */
    public TwoWayHashMap(Map<? extends K, ? extends V> map) {
        _reverseMap = new HashMap<V, K>();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /** Construct an empty two-way hash map with an initial capacity.
     *
     *  @param initialCapacity The initial capacity.
     */
    public TwoWayHashMap(int initialCapacity) {
        super(initialCapacity);
        _reverseMap = new HashMap<V, K>(initialCapacity);
    }

    /** Construct an empty two-way hash map with an initial capacity and a load
     *  factor.
    *
    *   @param initialCapacity The initial capacity.
    *   @param loadFactor The load factor.
    */
    public TwoWayHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        _reverseMap = new HashMap<V, K>(initialCapacity, loadFactor);
    }

    /** Clear this two-way hash map.
     */
    public void clear() {
        super.clear();
        _reverseMap.clear();
    }

    /** Clone this two-way hash map and return the clone.
     *
     *  @return The clone.
     */
    public Object clone() {
        TwoWayHashMap map = (TwoWayHashMap) super.clone();
        map._reverseMap = new HashMap<V, K>(_reverseMap);
        return map;
    }

    /** Test whether this two-way hash map contains the given value.
     *
     *  @param value The value.
     *  @return true if the value is contained.
     */
    public boolean containsValue(Object value) {
        return _reverseMap.containsKey(value);
    }

    /** Given a value, return the corresponding key in this two-way hash map.
     *
     *  @param value The value.
     *  @return the key.
     */
    public K getKey(Object value) {
        return _reverseMap.get(value);
    }

    /** Put a value into this two-way hash map and associate it with a key.
     *
     *  @param key The key.
     *  @param value The value.
     *  @return The value previously associated with the key in this two-way
     *   hash map.
     */
    public V put(K key, V value) {
        if (value == null) {
            return remove(key);
        } else {
            V oldValue = super.put(key, value);
            if (oldValue != null) {
                _reverseMap.remove(oldValue);
            }
            _reverseMap.put(value, key);
            return oldValue;
        }
    }

    /** Remove the value associated with the given key.
     *
     *  @param key The key.
     *  @return The removed value, or null if not found.
     */
    public V remove(Object key) {
        V oldValue = super.remove(key);
        if (oldValue != null) {
            _reverseMap.remove(oldValue);
        }
        return oldValue;
    }

    /** Return a set containing all the values in this two-way hash map.
     *
     *  @return The set.
     */
    public Set<V> values() {
        return Collections.unmodifiableSet(_reverseMap.keySet());
    }

    /** The reverse hash map that maps values back to keys.
     */
    private HashMap<V, K> _reverseMap;
}
