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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class FastHashMap<K, V> extends HashMap<K, V> {

    public FastHashMap() {
    }

    public FastHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public FastHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public FastHashMap(Map<? extends K, ? extends V> map) {
        super(map);
        resetValueSet();
    }

    public boolean containsValue(Object value) {
        return _valueSet.contains(value);
    }

    public V put(K key, V value) {
        _valueSet.add(value);
        return super.put(key, value);
    }
    
    public V remove(Object key) {
        _valueSet.remove(get(key));
        return super.remove(key);
    }

    private void resetValueSet() {
        _valueSet.clear();
        for (V value : values()) {
            _valueSet.add(value);
        }
    }

    private Set<V> _valueSet = new HashSet<V>();

    private static final long serialVersionUID = -655174160100973989L;

}
