/* 

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl;

import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import java.util.HashMap;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;

import ptolemy.kernel.util.IllegalActionException;

public class Region {

    public Region(DirectedGraph graph) throws IllegalActionException {
	_graph = graph;
	_dominators = new DominatorHashMap(_graph);
	_root = (Node) _dominators.getPartialOrder().iterator().next();
	_parent = null;
	_init();
    }

    public Region(Region parent, Node root) {
	_parent = parent;
	_root = root;
	_graph = getGraph();
	_dominators = getDominators();
	_init();
    }

    public DirectedGraph getGraph() {
	if (_parent == null)
	    return _graph;
	else
	    return _parent.getGraph();
    }

    public DominatorHashMap getDominators() {
	if (_parent == null)
	    return _dominators;
	else
	    return _parent.getDominators();
    }

    protected String nodeString(Node n) {
	return BlockControlFlowGraph.nodeString(n);
    }

    protected void _init() {
	_children = new HashMap();
	_sinkNode = null;

	// This vector holds successors (not necessarily direct) of
	// the _root. This vector is initialized with the direct
	// successors of the Root. As the algorithm processes nodes,
	// downstream successors to the root will be added to this vector.
	Vector successors = new Vector(_graph.successors(_root));

	// This vector holds Nodes within a Region that contain
	// Edges that leave the Region. This vector also holds those
	// Nodes within the region that have no successors (i.e return
	// statements). 
	//
	// Ideally, there should only be one such Node in a region. 
	// If more than one such Node is found, a final node is created
	// and the topology is modified to insure that one and only one
	// exit Node exists for the region.
	Vector exitNodes = new Vector();

	// All edges that leave a region must go to the *same* node
	// outside the region. This variable will point to this
	// outside Node when the algorithm has finished processing.
	// Note that there may not necessarily be an outsideNode.
	Node outsideNode = null;

	for (int i = 0;i < successors.size();i++) {

	    // Node s is the current Successor Node we are looking at
	    Node s = (Node) successors.get(i);
	    System.out.print(nodeString(_root)+":");
	    System.out.print(" with Child "+nodeString(s));

	    // If a child is dominated by root, create a new child region
	    if (_dominators.dominates(_root,s)) {

		System.out.print(" is dominated");
		if (!_children.containsKey(s)) {
		    System.out.println(" will be added");
		    Region childRegion = new Region(this,s);
		    _children.put(s,childRegion);
		    // Look at the out edges that exits child. If edge exits
		    // region, add to _exitEdge list. If not, create a new
		    // child region
		    Node exitChild = childRegion.getExitNode();
		    if (exitChild != null) {
			Collection outputEdges = _graph.outputEdges(exitChild);
			if (outputEdges.size() == 0) {
			    exitNodes.add(exitChild);
			} else {
			    for (Iterator j=outputEdges.iterator();
				 j.hasNext();) {
				
				Edge childEdge = (Edge) j.next();
				Node childSink = childEdge.sink();
				
				System.out.print(nodeString(_root)+": child "+
						 nodeString(s)+" edge sink= "+
						 nodeString(childSink));
				
				if (_dominators.dominates(_root,childSink)) {
				    System.out.print(" is dominated by root");
				    if (!_children.containsKey(childSink) &&
					!successors.contains(childSink)) {
					successors.add(childSink);
					System.out.println(" will be added");
				    } else
					System.out.println(" will not be added");
				} else {
				// childSink is a successor of s but
				// is not dominated by root node
				    exitNodes.add(s);
				    if (outsideNode == null)
					outsideNode = childSink;
				    else if (outsideNode != childSink)
					throw new RuntimeException("mismatch");
				    outsideNode = childSink;
				    System.out.println(" is not dominated by root");
				}
			    }
			}
		    }
		    
		} else
		    System.out.println(" : all ready added");
		
	    } else {
		// The successor Node s is not dominated by _root.
		// This means that the region can be exited through
		// _root. Add _root as an exitNode
		System.out.println(" is not dominated");
		exitNodes.add(_root);
	    }
	}
	// Loop through all exit Edges. If there is more than one edge
	// that exits this region, create a new exiting Node create 
	// a single exit edge
	System.out.print(BlockControlFlowGraph.nodeString(_root)+
			 ": exit Node=");
	if (exitNodes.size() == 1) {
	    _sinkNode = (Node) exitNodes.get(0);
	    System.out.println(nodeString(_sinkNode));
	} else if (exitNodes.size() == 0) {
	    _sinkNode = _root;
	    System.out.println(nodeString(_root));
	    //	    throw new RuntimeException("No sink node");
	} else {
	    System.out.print(" new node ");
	    Node newNode = _graph.addNodeWeight("region");
	    _sinkNode = newNode;

	    if (outsideNode == null) {
		// there are more than one leaf nodes. Iterate through these
		// leaf nodes and add edges between leaves and newNode
		for (Iterator i=exitNodes.iterator();i.hasNext();) {
		    Node n = (Node) i.next();
		    _graph.addEdge(n,newNode);
		}
	    } else {
		// All exitNodes go to the same outside node. Fix topology.
		_graph.addEdge(newNode,outsideNode);
		for (Iterator i=exitNodes.iterator();i.hasNext();) {
		    Node n = (Node) i.next();		
		    for (Iterator j=_graph.outputEdges(n).iterator();
			 j.hasNext();) {
			Edge e = (Edge) j.next();
			if (e.sink() == outsideNode) {
			    System.out.print("("+nodeString(e.source())+","+
					     nodeString(e.sink())+")");
			    // create new edge from e.source() to newNode
			    _graph.addEdge(e.source(),newNode);
			    _graph.removeEdge(e);
			}
		    }
		}
	    }
	    System.out.println(nodeString(newNode));
	}
    }

    public Node getExitNode() {
	return _sinkNode;
    }

    public String toShortString() {
	return "R"+BlockControlFlowGraph.nodeString(_root);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Region "+toShortString()+" children=");
	for (Iterator i=_children.values().iterator();i.hasNext();) {
	    Region r=(Region) i.next();
	    sb.append(r.toShortString()+" ");
	}
	sb.append(" exitNode=");
	if (_sinkNode == null)
	    sb.append("none");
	else 
	    sb.append(nodeString(_sinkNode));
	sb.append("\n");
	for (Iterator i=_children.values().iterator();i.hasNext();) {
	    Region r=(Region) i.next();
	    sb.append(r);
	}
	return sb.toString();
    }

    DirectedGraph _graph;
    Node _root;
    DominatorHashMap _dominators;
    Region _parent;
    HashMap _children;

    
    /** This Node is the sink for the current Region. By definition, each
     * region must have one entry point (_root) and one exit point. The
     * _sinkNode is the exit point of the Region.
     **/
    protected Node _sinkNode;

}
