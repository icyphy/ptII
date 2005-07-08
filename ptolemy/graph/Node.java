/* A weighted node for an undirected or directed graph.

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
//Node

/**
 An optionally-weighted node for an undirected or directed graph.  More
 specifically, a node consists of an optional <i>weight</i> (an arbitrary
 object that is associated with the node).  We say that a node is
 <i>unweighted</i> if it does not have an assigned weight. It is an error to
 attempt to access the weight of an unweighted node. Node weights must be
 genuine (non-null) objects.

 @author Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.graph.Edge
 */
public final class Node extends Element {
    /** Construct an unweighted node.
     */
    public Node() {
        super();
    }

    /** Construct a node with a given node weight.
     *  @exception IllegalArgumentException If the specified weight is
     *  <code>null</code>.
     *  @param weight The given weight.
     */
    public Node(Object weight) {
        super(weight);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** A one-word description of the type of this graph element.
     *  @return The description.
     */
    public String descriptor() {
        return "node";
    }

    /** Return a string representation of the node.
     *  The string representation is simply a representation of the node
     *  weight (or the string <code>"<unweighted node>"</code> if
     *  the node is unweighted.
     */
    public String toString() {
        if (_weight == null) {
            return "<unweighted node>";
        } else {
            return _weight.toString();
        }
    }
}
