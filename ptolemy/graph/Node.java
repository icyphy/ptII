/* A weighted node for an undirected or directed graph.

 Copyright (c) 2001 The University of Maryland
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

import java.util.ArrayList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Node
/**
A weighted node for an undirected or directed graph. A node consists
of a <em>weight</em> (an arbitrary object that is associated with the
node), and information about the edges that are incident to the node.
The incidence information inolves the set of input edges, set of
output eges, and set of all incident edges (all input and output
edges).  For run-time efficiency, there is some redundancy in the way
this information is stored.  For undirected graphs, it does not matter
whether an edge is attached (made incident) to a node as an input edge
or an output edge. The orientation of attachment only becomes relevant
when the graph is operated on in a manner that takes orientation into
account (e.g., by iterating over the set of input edges, rather than
the set of incident edges, in one or more nodes).

<p> A directed self loop edge (an edge whose source and sink nodes are
identical) is placed in the both the input edge list and the output
edge list of the incident node, but it is not duplicated in the
incident edges list. Thus, the number of incident edges is equal to
<em>I + O - S</em>, where <em>I</em> is the number of input edges,
<em>O</em> is the number of output edges, and <em>S</em> is the number
of self loop edges.

<p> Multiple edges in the node's input (output) edge list that are
directed from (to) the same node are allowed. Thus, multigraphs are
supported.

<p>To keep this code as fast as possible, minimal error checking is
performed. Consistency between nodes and their edge lists is up to the
classes (e.g., graphs) that use the nodes. For example, when {@link
#addInputEdge(Edge)} is used to insert an edge into a node's input
edge list, there is no checking performed to see whether or not the
node is actually the sink node of the edge being inserted.

<p>An edge should not be attached (incident) to multiple nodes at the
same time, except for self loop edges, which should be attached twice
(with {@link #addInputEdge(Edge)} and {@link #addOutputEdge(Edge)}).
Otherwise, unpredictable behavior may result.

<p>Note that nodes and edge lists in nodes should normally be
manipulated (created or modified) only by classes of graphs that contain them.

@author Shuvra S. Bhattacharyya
@version $Id$
@see ptolemy.graph.Edge
*/
public class Node {

    /** Construct a node with a null node weight.
     */
    public Node() {
        _weight = null;
        _inputEdges = new ArrayList();
        _outputEdges = new ArrayList();
        _incidentEdges = new ArrayList();
    }

    /** Construct a node with a given node weight.
     */
    public Node(Object weight) {
        this();
        _weight = weight;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Attach an undirected edge to be incident to this node. If the
     *  edge is operated on subsequently as a directed edge, it will be
     *  treated as an input edge.
     *  @param edge the incident edge to attach.
     */
    public void addIncidentEdge(Edge edge) {
        addInputEdge(edge);
    }

    /** Attach an edge to be incident to this node as an input edge.
     *  @param edge the incident input edge to attach.
     */
    public void addInputEdge(Edge edge) {
        _inputEdges.add(edge);

        // To avoid double-counting self-loop edges in the incident edges set,
        // check first to see whether the edge has already been registered
        // as an incident edge.

        if (!_incidentEdges.contains(edge)) {
            _incidentEdges.add(edge);
        }
    }

    /** Attach an edge to be incident to this node as an output edge.
     *  @param edge the incident output edge to attach.
     */
    public void addOutputEdge(Edge edge) {
        _outputEdges.add(edge);
        // To avoid double-counting self-loop edges in the incident edges set,
        // check first to see whether the edge has already been registered
        // as an incident edge.

        if (!_incidentEdges.contains(edge)) {
            _incidentEdges.add(edge);
        }
    }

    /** Return the number of incident edges of this node.
     *  @return the number of incident edges.
     */
    public int incidentEdgeCount() {return _incidentEdges.size();}

    /** Return an iterator over the set of all edges that are incident
     *  to this node.  For a node in a directed graph, this returns an
     *  iterator over the union of all input and output edges
     *  (self-loop edges are not counted twice).  Each element in the
     *  Iterator is an {@link Edge}.
     *
     *  @return an iterator over the set of all incident edges.
     */
    public Iterator incidentEdges() {
        return _incidentEdges.iterator();
    }

    /** Return the number of input edges of this node.
     *  @return the number of input edges.
     */
    public int inputEdgeCount() {return _inputEdges.size();}

    /** Return an iterator over the set of all input edges of this
     *  node.  More precisely, the method returns an iterator over the
     *  set of all edges that were attached to this node using either
     *  <code>addIncidentEdge</code> or <code>addInputEdge</code>.
     *  Each element in the Iterator is an {@link Edge}.
     *
     *  @return an iterator over the set of all input edges.
     */
    public Iterator inputEdges() {
        return _inputEdges.iterator();
    }

    /** Return the number of output edges of this node.
     *  @return the number of output edges.
     */
    public int outputEdgeCount() {return _outputEdges.size();}

    /** Return an iterator over the set of all output edges of this
     *  node.  More precisely, the method returns an iterator over the
     *  set of all edges that were attached to this node using
     *  <code>addOutputEdge</code>.  Each element in the Iterator is
     *  an {@link Edge}.
     *
     *  @return an iterator over the set of all output edges.
     */
    public Iterator outputEdges() {
        return _outputEdges.iterator();
    }

    /** Remove incidence of an edge. The specified edge will no longer
     *  be incident * to this node in any form (as an input edge or
     *  output edge). Do nothing if the specified edge is not incident
     *  to the node to begin with.
     *
     *  @param edge the edge.
     */
    public void removeEdge(Edge edge) {
        _removeIfPresent(_incidentEdges, edge);
        _removeIfPresent(_inputEdges, edge);
        _removeIfPresent(_outputEdges, edge);
    }

    /** Return the number of self loop edges of this node.
     *  @return the number of self loop edges.
     */
    public int selfLoopEdgeCount() {
        return _inputEdges.size() + _outputEdges.size()
            - _incidentEdges.size();
    }

    /** Return a string representation of the node.
     *  The string representation is simply a representation of the node
     *  weight (or the string "null" if the weight is <em>null</em>).
     */
    public String toString() {
        if (_weight == null) return "null";
        else return _weight.toString();
    }

    /** Return the weight that has been associated with this node.
     *  @return the associated weight.
     */
    public Object weight() {return _weight;}

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    // Remove an object from an ArrayList if it exists in the list.
    public void _removeIfPresent(ArrayList list, Object element) {
        int index;
        if ((index = list.indexOf(element)) != -1) list.remove(index);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    // The weight that has been associated with the node.
    private Object _weight;

    // The set of input edges that are incident to the node.
    private ArrayList _inputEdges;

    // The set of output edges that are incident to the node.
    private ArrayList _outputEdges;

    // The union of the sets of input and output edges (self loop edges appear
    // only once). This redundant information is maintained for improved
    // run-time efficiency when handing undirected graphs, or when operating
    // on directed graphs in ways for which edge orientation is not relevant.
    private ArrayList _incidentEdges;
}
