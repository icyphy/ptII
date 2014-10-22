/* A ToIntMapping that is based on a Map.

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
//// ToIntMapMapping

/** A ToIntMapping that is based on a Map. The values in the Map
 must be instances of Integer. ToIntMapMappings are immutable in the
 sense that the underlying Map cannot be changed (although the keys and
 values associated with the Map can be changed).

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ssb)
 @Pt.AcceptedRating Red (ssb)
 @author Shuvra S. Bhattacharyya
 @version $Id$
 */
public class ToIntMapMapping extends MapMapping implements ToIntMapping {
    /** Construct a ToIntMapMapping from a given map. The values in the
     *  must be instances of Integer; otherwise, unpredictable behavior
     *  may result. Modifications to the argument Map after construction
     *  of this mapping will be reflected in the mapping. The Map modifications
     *  must follow the restriction that all added values to the Map
     *  must be instances of Integer.
     *  @param map The given map.
     */
    public ToIntMapMapping(Map map) {
        super(map);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the given object is in the domain of this Mapping.
     *  More precisely, return true if the given object is a valid argument
     *  to {@link #toInt(Object)}, which means that the object is a
     *  key in the Map that is associated with this Mapping and the value
     *  in the Map is an instance of Integer.
     *  @param object The given object.
     *  @return True if the given object is in the domain of this Mapping.
     */
    @Override
    public boolean inDomain(Object object) {
        return _map.containsKey(object) && _map.get(object) instanceof Integer;
    }

    /** Return the int value that is associated with given object under
     *  this mapping. For efficiency, no error checking is performed
     *  on the argument, and consequently, a runtime exception may result as
     *  noted below. To perform argument validity checking before mapping an
     *  object, use {@link ptolemy.graph.mapping.Mapping#inDomain(Object)}.
     *  @param object The given object.
     *  @return The int value that is associated with given object under
     *  this mapping.
     *  @exception RuntimeException If the given object is not an instance
     *  of {@link java.lang.Integer} or if the given object is not in the
     *  domain of the mapping.
     */
    @Override
    public int toInt(Object object) {
        return ((Integer) _map.get(object)).intValue();
    }

    @Override
    public Object toObject(Object object) {
        return _map.get(object);
    }
}
