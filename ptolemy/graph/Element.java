/* A base class for graph elements (nodes and edges).

 Copyright (c) 2001-2005 The University of Maryland
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
package ptolemy.graph;

////////////////////////////////////////////////////////////////////////// //
//Element

/**
 A base class for graph elements (nodes and edges).
 A graph element consists of an optional <i>weight</i> (an arbitrary
 object that is associated with the element).  We say that an element is
 <i>unweighted</i> if it does not have an assigned weight. It is an error to
 attempt to access the weight of an unweighted element. Element weights must
 be non-null objects.

 @author Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.graph.Edge
 @see ptolemy.graph.Node
 */
public abstract class Element {
    /** Construct an unweighted element.
     */
    public Element() {
        _weight = null;
    }

    /** Construct an element with a given weight.
     *  @exception IllegalArgumentException If the specified weight is
     *  <code>null</code>.
     *  @param weight The given weight.
     */
    public Element(Object weight) {
        setWeight(weight);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** A one-word description of the type of this graph element.
     *  @return The description.
     */
    public String descriptor() {
        return "element";
    }

    /** Return the weight that has been associated with this element.
     *  @return The associated weight.
     *  @exception IllegalStateException If this is an unweighted element.
     */
    public final Object getWeight() {
        if (!hasWeight()) {
            throw new IllegalStateException("Attempt to access the weight "
                    + "of the following unweighted " + descriptor() + ": "
                    + this + "\n");
        } else {
            return _weight;
        }
    }

    /** Return <code>true</code> if and only if this is a weighted element.
     *  @return True if and only if this is a weighted element.
     */
    public final boolean hasWeight() {
        return _weight != null;
    }

    /** Make the element unweighted. This method should be used with
     *  caution since it may make the element incompatible with graphs that
     *  already contain it. The method has no effect if the element is already
     *  unweighted.
     *  @see Graph#validEdgeWeight(Object)
     *  @see Graph#validNodeWeight(Object)
     *  @see Graph#validateWeight(Node)
     */
    public final void removeWeight() {
        // FIXME: add @see Graph#validateWeight(Edge)
        _weight = null;
    }

    /** Set or change the weight of an element. This method should be used with
     *  caution since it may make the element incompatible with graphs that
     *  already contain it.
     *  @param weight The new weight.
     *  @exception IllegalArgumentException If the object that is passed as
     *   argument is null.
     *  @see Graph#validEdgeWeight(Object)
     *  @see Graph#validNodeWeight(Object)
     *  @see Graph#validateWeight(Node)
     */
    public final void setWeight(Object weight) {
        // FIXME: add @see Graph#validateWeight(Edge)
        if (weight == null) {
            throw new IllegalArgumentException("Attempt to assign a null "
                    + "weight to the following " + descriptor() + ": " + this
                    + "\n");
        } else {
            _weight = weight;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The weight that is associated with the element if the element is
     *  weighted. If the element is not weighted,  the value of this
     *  field is null.
     */
    protected Object _weight;
}
