/* A weighted node for an undirected or directed graph.

 Copyright (c) 2001-2002 The University of Maryland
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.graph;

//////////////////////////////////////////////////////////////////////////
//// Node
/**
An optionally-weighted node for an undirected or directed graph.
More specifically, a node consists
of an optional <em>weight</em> (an arbitrary object that is associated with the
node), and information about the edges that are incident to the node.
The incidence information involves the set of input edges, set of
output edges, and set of all incident edges (all input and output
edges).  For run-time efficiency, there is some redundancy in the way
this information is stored.  For undirected graphs, it does not matter
whether an edge is attached (made incident) to a node as an input edge
or an output edge. The orientation of attachment only becomes relevant
when the graph is operated on in a manner that takes orientation into
account (e.g., by iterating over the set of input edges, rather than
the set of incident edges, in one or more nodes).

<p> We say that a node is <em>unweighted</em> if it does not have an
assigned weight. It is an error to attempt to access the weight of
an unweighted node. Node weights must be genuine (non-null) objects.

<p> A directed self loop edge (an edge whose source and sink nodes are
identical) is placed in both the input edge list and the output
edge list of the incident node, but it is not duplicated in the
incident edges list. Thus, the number of incident edges is equal to
<em>I + O - S</em>, where <em>I</em> is the number of input edges,
<em>O</em> is the number of output edges, and <em>S</em> is the number
of self loop edges.

<p> Multiple edges in the node's input (output) edge list that are
directed from (to) the same node are allowed. Thus, multigraphs are
supported.

<p>Note that nodes and edge lists in nodes should normally be
manipulated (created or modified) only by classes of graphs that contain them.

@author Shuvra S. Bhattacharyya
@version $Id$
@see ptolemy.graph.Edge
*/
public final class Node {

    /** Construct an unweighted node.
     */
    public Node() {
        _weight = null;
    }

    /** Construct a node with a given node weight.
     *  @exception IllegalArgumentException If the specified weight is
     *  <code>null</code>.
     */
    public Node(Object weight) {
        if (weight == null) {
            throw new IllegalArgumentException("Attempt to assign a null "
                    + "weight to a node.");
        } else {
            _weight = weight;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return <code>true</code> if and only if this is a weighted node.
     *  @return <code>True</code> if and only if this is a weighted node.
     */
    public boolean hasWeight() {
        return _weight != null;
    }

    /** Return a string representation of the node.
     *  The string representation is simply a representation of the node
     *  weight (or the string <code>"<unweighted node>"</code> if
     *  the node is unweighted.
     */
    public String toString() {
        if (_weight == null) {
            return "<unweighted node>";
        }
        else {
            return _weight.toString();
        }
    }

    /** Return the weight that has been associated with this node.
     *  @return the associated weight.
     *  @exception IllegalStateException If this is an unweighted node.
     */
    public Object weight() {
        if (!hasWeight()) {
            throw new IllegalStateException("Attempt to access the weight "
                    + "of an unweighted node.\n");
        } else {
            return _weight;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The weight that has been associated with the node.
    private Object _weight;

}

