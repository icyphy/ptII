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

public class Interval {

    public Interval(DirectedGraph graph) throws IllegalActionException {
	_graph = graph;
	_dominators = new DominatorHashMap(_graph);
	_root = (Node) _dominators.getPartialOrder().iterator().next();
	_parent = null;
	_init();
    }

    public Interval(Interval parent, Node root) throws IllegalActionException {
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

    protected String ns(Node n) {
	return BlockControlFlowGraph.nodeString(n);
    }

    protected String is(Interval i) {
	Node n = i._root;
	return BlockControlFlowGraph.nodeString(n);
    }

    protected void _init() throws IllegalActionException {
	_sinkNode = null;
	_next = null;

	Interval childInterval = null;
	Node child = null;

	HashMap domChildren = new HashMap();
	Vector nonDomChildren = new Vector();

	Collection rootSuccessors = _graph.successors(_root);

//  	System.out.print(ns(_root));
//  	if (_parent != null) System.out.print(" (parent="+is(_parent)+")");
//  	else System.out.print(" (root)");

	switch(rootSuccessors.size()) {
	case 0:
	    // No successor Nodes (a leaf node)
	    _sinkNode = _root;
//  	    System.out.print(" terminating");
	    break;
	case 1:
	    // one successor Node
	    child = (Node) rootSuccessors.iterator().next();
	    _sinkNode = _root;
	    if (_dominators.dominates(_root,child)) {
		// root dominate the one child
		_next = new Interval(this,child);
//  		System.out.print(" Child="+is(_next));
	    } else {
		// root does not dominate the one child
//  		System.out.print(" single Node (nonDominated child)");
	    }
	    break;
	default:
	    
	    // iterate over direct children. Children are either dominated
	    // by the root or not. If they are dominated, create a new
	    // interval and store. If not, save in a list.
	    for (Iterator i = rootSuccessors.iterator(); i.hasNext();) {
		child = (Node) i.next();
		if (_dominators.dominates(_root,child)) {
		    // root dominates current child.
		    childInterval = new Interval(this,child);
		    domChildren.put(child,childInterval);
		} else {
		    // root does not dominate current child.
		    // If root does not dominate current child, all
		    // children MUST have this non-dominated node as
		    // the target in order for the forks to be part
		    // of this interval.
		    nonDomChildren.add(child);
		}
	    }

	    // More than one direct successor. 
	    // Three cases:
	    // - merge all successors
	    // - stop as a single node interval (clear children)
	    // - iterate through topology for a complex merge
	    
	    // Determine if children can be merged.
	    Node target = _commonTarget(domChildren,nonDomChildren);
	    if (target != null) {
		// merge successors directly
		_sinkNode = target;
		_children = domChildren;
		// Continue chain?
		Collection ctout = _graph.outputEdges(target);
		if (ctout.size() == 1) {
		    Node nextNode = ((Edge)ctout.iterator().next()).sink();
		    if (_dominators.dominates(target,nextNode))
			_next = new Interval(this,nextNode);
		}
	    } else if (nonDomChildren.size() > 0) {
		// stop as a single node interval (clear children)
		_sinkNode = _root;
		_children = null;
	    } else {
		// iterate through topology for a complex merge
		System.exit(1);
	    }

	    break;
	}	
//  	System.out.println();
    }

    protected Node _commonTarget(HashMap domChildren, Vector nonDomChildren) 
	throws IllegalActionException {

	Node commonTarget = null;
	// Perform a two-dimensional iteration over all dominated children
	// (i.e. compare each dominated child with the others) to see
	// if one of the dominated children is the target of all other
	// dominated children chains.
	if (domChildren.values().size() > 1) {
	    for (Iterator i = domChildren.values().iterator();i.hasNext();) {
		// intv is the Interval that is being considered as the
		// common target.
		Interval intv = (Interval) i.next();
		// TODO: is getRootNode() correct?
		Node pct = intv.getRootNode(); // possible common target
		//  	    System.out.print("possible target="+ns(pct)+"-");
		for (Iterator j = domChildren.values().iterator();j.hasNext();) {
		    Interval jntv = (Interval) j.next();
		    //  		System.out.print(is(jntv));
		    // don't compare with self.
		    if (jntv == intv)
			continue;
		    Node ppct = jntv.getChainSinkNode();
		    // see if sink node points to pct
		    if (_graph.outputEdges(ppct).size() != 1) {
			pct = null;
			continue;
		    }
		    Node target = ((Edge)_graph.outputEdges(ppct).iterator().next()).sink();
		    if (target != pct)
			pct = null;
		}
		if (pct != null) {
		    //  		System.out.println("Found!");
		    commonTarget = pct;
		    break;
		}
	    }
	}
//  	System.out.println();

	// 1. Make sure targetNode of all children intervals point
	//    to the same Node.
	if (commonTarget == null) {
	    for (Iterator i = domChildren.values().iterator();i.hasNext();) {
		Interval intv = (Interval) i.next();
		Node sink = intv.getChainSinkNode();
		Collection edges = _graph.outputEdges(sink);
		// if the sink node has more than one output edge,
		// it cannot have a common target with other children.
		if (edges.size() > 1) {
//  		    System.out.print(" child interval "+is(intv)+" has more than one edge");
		    return null;
		}
		Node ct = null;
		if (edges.size() == 0) {
		    // a leaf node. See if a non-leaf node is common
		    if (commonTarget != null) {
//  			System.out.print(" leaf target not common");
			return null;
		    }
		} else {
		    // One edge
		    ct = ((Edge) edges.iterator().next()).sink();
		    if (commonTarget == null)
			commonTarget = ct;
		    else if (commonTarget != ct) {
//  			System.out.print(" child target not common");
			return null;
		    }
		}		
	    }
	}

	// 2. Make sure that any nonDominatedSuccessors are the
	//    same and equal to commonTarget.
	for (Iterator i = nonDomChildren.iterator();i.hasNext();){
	    Node nds = (Node) i.next();
	    if (commonTarget != nds) {
//  		System.out.print(" ND child target not common");
		return null;
	    }
	}

	// Add Nodes!
	boolean graphChanged = false;

	// See if additional Nodes need to be created.
	// Case #1: merge on a nonDominated target node
	if (!_dominators.dominates(_root,commonTarget)) {
	    graphChanged = true;
//  	    System.out.print(" add common node");
	    // Add new target node
	    Node newNode = _graph.addNodeWeight("merge");
	    // Add edge between new target Node and common non dominated
	    // Node
	    _graph.addEdge(newNode,commonTarget);
	    // Iterate through dominated intervals and change the edge
	    // leaving the sink node to the newNode 
	    for (Iterator i = domChildren.values().iterator();i.hasNext();) {
		Interval intv = (Interval) i.next();
		Node sink = intv.getChainSinkNode();
		Edge e = (Edge) _graph.outputEdges(sink).iterator().next();
		_graph.removeEdge(e);
		_graph.addEdge(sink,newNode);
	    }
	    // Get all edges from _root to commonTarget and move them
	    // to newNode
	    for (Iterator i = _graph.outputEdges(_root).iterator();
		 i.hasNext();) {
		Edge e = (Edge) i.next();
		if (e.sink() == commonTarget) {
		    _graph.removeEdge(e);
		    _graph.addEdge(_root,newNode);
		}
	    }
	    commonTarget = newNode;
	}

	// Case #2: return statements (null common node)
	if (commonTarget == null) {
	    graphChanged = true;
//   	    System.out.print(" add common leaf node");
	    // Add new target node
	    Node newNode = _graph.addNodeWeight("merge");
	    // iterate through Intervals
	    for (Iterator i=domChildren.values().iterator();i.hasNext();) {
		Interval intv = (Interval) i.next();
		Node sink = intv.getChainSinkNode();
		_graph.addEdge(sink,newNode);
	    }	    
	    commonTarget = newNode;
	}

	// Case #3: commonTarget forks
	Collection ctEdges = _graph.outputEdges(commonTarget);
	if (ctEdges.size() > 1) {
	    graphChanged = true;
	    Node newNode = _graph.addNodeWeight("merge");
	    _graph.addEdge(newNode, commonTarget);
	    for (Iterator i=domChildren.values().iterator();i.hasNext();) {
		Interval intv = (Interval) i.next();
		Node sink = intv.getChainSinkNode();
		Edge e = (Edge) _graph.outputEdges(sink).iterator().next();
		_graph.removeEdge(e);
		_graph.addEdge(sink,newNode);
	    }	    
	    commonTarget = newNode;
	}
	
	// update dominators list
	if (graphChanged) {
	    _dominators = new DominatorHashMap(_graph);
	}

	// TODO:
	// - Add nodes that are required.
	// - return the "_sinkNode"
	// - Figure out how to traverse topology
//  	System.out.print(" merge c=");
	for (Iterator i=domChildren.values().iterator();i.hasNext();) {
	    Interval invt = (Interval) i.next();
//  	    System.out.print(is(invt));
	}
	if (nonDomChildren.size() > 0) {
//  	    System.out.print(" nonDom=");
	    for (Iterator i=nonDomChildren.iterator();i.hasNext();) {
		Node nds = (Node) i.next();
//  		System.out.print(ns(nds));
	    }
	}

	return commonTarget;
    }

    protected Node _nonCommonTarget(HashMap domChildren, Vector nonDomChildren) 
	throws IllegalActionException {
	return null;
    }

    /** Return the sink node of the interval **/
    public Node getSinkNode() {
	return _sinkNode;
    }

    /** Return the sink node of the interval **/
    public Node getChainSinkNode() {
	if (_next == null)
	    return _sinkNode;
	else
	    return _next.getChainSinkNode();
    }

    public Node getRootNode() {
	return _root;
    }


    public String toShortString() {
	return "I"+BlockControlFlowGraph.nodeString(_root);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Interval "+toShortString());
	
	if (_children != null) {
	    sb.append (" children=");
	    for (Iterator i=_children.values().iterator();i.hasNext();) {
		Interval r=(Interval) i.next();
		sb.append(r.toShortString()+" ");
	    }
	}

	sb.append(" sinkNode=");
	if (_sinkNode == null)
	    sb.append("none");
	else 
	    sb.append(ns(_sinkNode));

	if (_next != null) {
	    sb.append(" next="+is(_next));	}

	sb.append("\n");

	// Print all children
	if (_children != null) {
	    for (Iterator i=_children.values().iterator();i.hasNext();) {
		Interval r=(Interval) i.next();
		sb.append(r);
	    }
	}
	// Print next
	if (_next != null)
	    sb.append(_next);

	return sb.toString();
    }

    DirectedGraph _graph;
    Node _root;
    DominatorHashMap _dominators;
    Interval _parent;
    HashMap _children;

    /** An Interval is a linked list of sequentially executing intervals.
     * _next is the next Interval in the list. If _next is null, this
     * is the end of the interval chain.
     **/
    protected Interval _next;
    
    /** This Node is the sink for the current Interval. By definition, each
     * region must have one entry point (_root) and one exit point. The
     * _sinkNode is the exit point of the Interval.
     **/
    protected Node _sinkNode;

}
