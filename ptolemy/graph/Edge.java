/* A weighted edge for a directed or undirected graph.

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

//////////////////////////////////////////////////////////////////////////
//// Edge
/**
A weighted edge for a directed or undirected graph. Edges should
normally be manipulated (created, and modified) only by classes of
graphs that contain them. The connectivity of edges is specified by
"source" nodes and "sink" nodes.  A directed edge is directed "from"
its source node "to" its sink node.  For an undirected edge, the
source node is simply the first node that was specified when the edge
was created, and the sink node is the second node.  This convention
allows undirected edges to later be "converted" in a consistent manner
to directed edges, if desired.

<p>Self-loop edges (edges whose source and sink nodes are identical)
are allowed.

<p>An arbitrary object can be associated with an edge as the "weight"
of the edge.

@author Shuvra S. Bhattacharyya 
@version $Id$
@see ptolemy.graph.Node
*/
public class Edge {

    /** Construct an edge with a specified source node, sink node, and
     *  edge weight.
     *  The edge weight may be <em>null</em>.
     *  @param the source node.
     *  @param the sink node.
     *  @param the edge weight.
     */
    public Edge(Node source, Node sink, Object weight) {
        _source = source;
        _sink = sink;
        _weight = weight;
        source.addOutputEdge(this);
        sink.addInputEdge(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the sink node of the edge 
     *  @return the sink node. 
     */
    public Node sink() {return _sink;}
    
    /** Return the source node of the edge 
     *  @return the source node. 
     */
    public Node source() {return _source;}

    /** Return a string representation of the edge. The string
     *  representation is of the form <p> (<em>source</em>,
     *  <em>sink</em>, <em>weight</em>), <p> where <em>source</em>,
     *  <em>sink</em>, and <em>weight</em> are string representations
     *  of the source node, sink node, and edge weight, respectively.
     *  The edge weight is suppressed from the representation if the
     *  method argument is <em>false</em>.
     *  @param showWeight include a string representation of the edge
     *  weight in the edge's string representation.
     *  @return the edge's string representation. 
     */  
    public String toString(boolean showWeight) {
        String result = new String("(" + _source + ", " + _sink);
        if (showWeight) {
            result += ", ";
            if (_weight == null) result += "null";
            else result += _weight;
        }
        result += ")";
        return result;
    }

    /** Return a string representation of the edge. Include information 
     *  about the edge weight.
     *  @see #toString(boolean)
     */ 
    public String toString() {
        return toString(true);
    }

    /** Return the weight of the edge 
     *  @return the edge weight. 
     */
    public Object weight() {return _weight;}

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
   
    // The sink node of the edge. 
    private Node _sink;

    // The source node of the edge.
    private Node _source;

    // The weight of the edge
    private Object _weight;
}
