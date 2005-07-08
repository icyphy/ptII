/* A mapping from an arbitrary domain of values into some range.

 Copyright (c) 2003-2005 The University of Maryland.
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

//////////////////////////////////////////////////////////////////////////
//// Mapping

/** A mapping from some domain of values into some range.
 Mappings are different from Maps (see {@link java.util.List}) in that the set
 of keys (domain values) is not necessarily stored with or even known to a
 Mapping.  Enumeration of or iteration through the domain values is thus not in
 general possible.
 <p>
 This is a base interface for specific mappings. For efficiency, the derived
 mappings should define their own methods to actually perform the associated
 mapping function. These methods can thus be specialized, for example, to return
 the desired return type (rather than an Object). Also, derived Mappings
 may choose, again for efficiency reasons, to forego any error-checking
 in the methods that implement their mapping functions (i.e, they may
 assume that the arguments are in the corresponding domains). In such cases,
 the {@link #inDomain(Object)} method can be used when it is desired
 to make sure that that a candidate argument is in the domain.

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ssb)
 @Pt.AcceptedRating Red (ssb)
 @author Shuvra S. Bhattacharyya, Shahrooz Shahparnia
 @version $Id$
 */
public interface Mapping {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Returns true if a given object is in the domain of the mapping.
     *
     *  @param object The given object.
     *  @return True if a given object is in the domain of the mapping
     */
    public boolean inDomain(Object object);

    /** Return the object associated with the given object in the mapping.
     *
     *  @param object The given object.
     *  @return Return the object associated with the given object in the
     *  mapping.
     */
    public Object toObject(Object object);
}
