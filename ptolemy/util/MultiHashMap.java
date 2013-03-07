/** A map that associates a key with multiple values.

 Copyright (c) 1997-2013 The Regents of the University of California.
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
package ptolemy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

///////////////////////////////////////////////////////////////////
//// MultiHashMap

/**
MultiHashMap is an implementation of the MultiMap interface. It
associates a collection of objects to each key. Putting a new
object under a key adds to the associated collection. Likewise,
removing a object removes from the collection. It is possible
that the given object to remove is not contained by the collection.
In which case, no changes is made and null is returned. The items
in each collection are ordered by insertion, and duplicates are
stored in the collections.

For example, given a key K and object O1, and O2:

    MultiHashMap map = new MultiHashMap();
    map.put(K, O1);
    map.put(K, O1);
    map.put(K, O2);

then, map.size(K) would return 3. Iterating through the map returns
O1, O1, and O2 in order.

 @author Man-Kit Leung, Ben Lickly
 @version $Id$
 @param <K> The type of the keys of the multimap.
 @param <V> The type of the values of the multimap.
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public class MultiHashMap<K, V> {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the collection of values mapped to by a given key.
     *  @param key The index into the multimap.
     *  @return The collection of values at that index.
     */
    public Collection<V> get(K key) {
        return _map.get(key);
    }

    /** Return a set of all key values represented by this multimap.
     *  @return A set of values that are keys of this multimap.
     */
    public Set<K> keySet() {
        return _map.keySet();
    }

    /** Return whether or not this multimap is empty.
     *  @return True, if the map is empty. False, otherwise.
     */
    public boolean isEmpty() {
        return _map.isEmpty();
    }

    /** Add the value to the collection associated with the specified key.
     *  @param key The specified key.
     *  @param value The specified value to add to the collection.
     */
    public void put(K key, V value) {
        Collection<V> values = _map.get(key);
        if (values == null) {
            values = new ArrayList<V>();
            _map.put(key, values);
        }
        values.add(value);
    }

    /** Remove a specified value from the map. The value is removed
     *  from the collection mapped to the specified key. If this is
     *  the last value removed from the given key, the specified key
     *  is also removed from the map. Subsequent call to get(key) will
     *  return false.
     *  @param key The specified key to remove the value from.
     *  @param value The specified value to remove.
     *  @return True, if the value was removed. False, otherwise.
     */
    public boolean remove(K key, V value) {
        Collection<V> values = _map.get(key);

        if (values == null) {
            return false;
        } else {
            boolean removed = values.remove(value);
            if (values.size() == 0) {
                _map.remove(key);
            }
            return removed;
        }
    }

    /** Return the size of the collection mapped to the specified key.
     *  @param key The specified key.
     *  @return The size of the collection, or zero if key is
     *    not in the map.
     */
    public int size(Object key) {
        Collection<V> values = _map.get(key);

        if (values == null) {
            return 0;
        } else {
            return values.size();
        }
    }

    /** Return a view of the collection containing all values in the map.
     *  This is a collection containing the union of each collection
     *  mapped to the keys.
     *  @return A view of all values contained in this map.
     */
    public Collection<V> values() {
        Collection<V> result = new ArrayList<V>();
        for (Collection<V> values : _map.values()) {
            result.addAll(values);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The HashMap that stores the mappings of this MultiMap.  The multimap
     *  is constructed by having the values of the HashMap be collections.
     */
    private HashMap<K, Collection<V>> _map = new HashMap<K, Collection<V>>();
}
