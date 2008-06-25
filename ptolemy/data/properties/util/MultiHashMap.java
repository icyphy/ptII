/** A Map that maps multiple items to a key.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.data.properties.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// MultiHashMap

/**
MultiHashMap is the default implementation of the MultiMap interface. 
A MultiMap is a Map with slightly different semantics. Putting a value
into the map will add the value to a Collection at that key. Getting a
value will return a Collection, holding all the values put to that key.

This implementation uses an ArrayList as the collection. The internal
storage list is made available without cloning via the get(Object)
and entrySet() methods. The implementation returns null when there
are no values mapped to a key.

For example: 

 MultiMap mhm = new MultiHashMap();
 mhm.put(key, "A");
 mhm.put(key, "B");
 mhm.put(key, "C");
 List list = (List) mhm.get(key);list will be a list 
 containing "A", "B", "C". 

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public class MultiHashMap extends HashMap implements MultiMap {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Adds the value to the collection associated with the specified key.
     * @param key The key to store against.
     * @param value The value to add to the collection at the key. 
     * @return The value added if the map changed and null if the map
     *  did not change.
     */
    public Object put(Object key, Object value) {
        ArrayList values = (ArrayList) get(key);
        if (values == null) { 
            values = new ArrayList();
            super.put(key, values);
        }
        values.add(value);
        return value;
    }
        
    /**
     * Removes a specific value from map. The item is removed
     * from the collection mapped to the specified key. Other 
     * values attached to that key are unaffected. If the last
     * value for a key is removed, null will be returned from
     * a subsequant get(key). 
     * @param key The key to remove from.
     * @param item The value to remove.
     * @return The value removed (which was passed in), null if
     *  nothing removed.
     */
    public Object remove(Object key, Object item) {
        Collection values = (Collection) get(key);

        if (values == null) {
            return null;
        } else {
            return values.remove(item);
        } 
    }
    
    /**
     * Gets the size of the collection mapped to the specified key.
     * @param key The key to get size for.
     * @return The size of the collection at the key, zero if key not in map.
     */
    public int size(Object key) {
        Collection values = (Collection) get(key);
        
        if (values == null) {
            return 0;
        } else {
            return values.size();
        }
    }
    
    /**
     * Gets a collection containing all the values in the map. This
     * returns a collection containing the combination of values from
     * all keys. 
     * @return A collection view of the values contained in this map.
     */
    public Collection values() {
        Collection result = new ArrayList();
        for (Object object : super.values()) {
            Collection values = (Collection) object;
            result.addAll(values);
        }
        return result;
    }
}
