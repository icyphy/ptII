/** A map that holds a collection of values against each key.

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

 added description() method
 made many methods throw IllegalActionException

 */
package ptolemy.data.properties.util;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// MultiMap

/**
Defines a map that holds a collection of values against each key. 

A MultiMap is a Map with slightly different semantics. Putting a
value into the map will add the value to a Collection at that key.
Getting a value will return a Collection, holding all the values
put to that key.

For example: 

 MultiMap mhm = new MultiHashMap();
 mhm.put(key, "A");
 mhm.put(key, "B");
 mhm.put(key, "C");
 Collection coll = (Collection) mhm.get(key);coll will be a
 collection containing "A", "B", "C".

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public interface MultiMap extends Map {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Removes a specific value from map.
     * @param key
     * @param item
     * @return
     */
    public Object remove(Object key, Object item);
    
}
