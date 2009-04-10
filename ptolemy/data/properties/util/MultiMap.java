/** A map interface that associates a key with multiple values.

 Copyright (c) 1997-2009 The Regents of the University of California.
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
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// MultiMap

/**
A map that associates a key with multiple values.

Similar to MultiSet, MultiMap is a generalization of a abstract
data structure (Map). Like Map, MultiMap does not allow duplicate
keys. It associates a collection of objects to each key. Putting
a new object under a key adds to the associated collection.
Likewise, removing a object removes from the collection.

Several things are open to the implementation classes. First is the
ordering of the collection of all contained values. Second, ordering
within the collection is also open to the implementation class.
Each collection may or may not contain duplicates. For example, a
implementation class may use HashSets to represent the collections. This
effectively eliminate all duplicates but gives no ordering guarantee.
Although it is rare, but one can also imagine having a hybrid
implementation where each collection provide different guarantees
depending on the key that they map under.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public interface MultiMap extends Map {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Remove a specified value from the map.
     * @param key The specified key.
     * @param object The specified object to remove from the collection.
     * @return The removed item, or null if the item
     *  does not exist.
     */
    public Object remove(Object key, Object object);

}
