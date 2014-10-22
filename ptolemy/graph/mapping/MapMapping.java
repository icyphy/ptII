/* A base class for a Mapping that is based on a Map.

 Copyright (c) 2003-2014 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.graph.mapping;

import java.util.Map;

///////////////////////////////////////////////////////////////////
//// MapMapping

/** A Mapping that is based on a Map. The domain of the Mapping is the
 set of keys in the Map. MapMappings are immutable in the
 sense that the underlying Map cannot be changed (although the keys and
 values associated with the Map can be changed).

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ssb)
 @Pt.AcceptedRating Red (ssb)
 @author Shuvra S. Bhattacharyya
 @version $Id$
 */
public abstract class MapMapping implements Mapping {
    /** Construct a MapMapping from a given Map.
     *  Modifications to the argument Map after construction
     *  of this mapping will be reflected in the Mapping.
     *  @param map The given map.
     */
    public MapMapping(Map map) {
        _map = map;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the given object is of the same Class and based
     *  on the same Map as this one.
     *  @param object The given object.
     *  @return True if the given object is of the same class and based
     *  on the same Map as this one.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        return _map.equals(((MapMapping) object)._map);
    }

    /** Return the hash code of this MapMapping. The hash code is
     *  simply that of the Map that this Mapping is based on.
     */
    @Override
    public int hashCode() {
        return _map.hashCode();
    }

    /** Return true if the given object is a key in the Map that is associated
     *  with this mapping.
     *  @param object The given object.
     *  @return True if the given object is a key in the Map that is associated
     *  with this mapping.
     */
    @Override
    public boolean inDomain(Object object) {
        return _map.containsKey(object);
    }

    /** Return a string representation of this MapMapping. The
     *  string representation is the class name, followed by a
     *  delimiting string, followed by a
     *  string representation of the underlying Map.
     */
    @Override
    public String toString() {
        return getClass().getName() + "based on the following Map\n"
                + _map.toString() + "\n";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /** The Map on which this Mapping is based. */
    protected Map _map;
}
