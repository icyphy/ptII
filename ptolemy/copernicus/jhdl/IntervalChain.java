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

public class IntervalChain {

    public IntervalChain(DominatorCFG graph) throws IllegalActionException {
	_parent = null;
	_graph = graph;
	_root = _graph.source();
	_init();
    }

    public IntervalChain(IntervalChain parent, Node root) throws IllegalActionException {
	_parent = parent;
	_graph = getGraph();
	_root = root;
	_init();
    }

    public DominatorCFG getGraph() {
	if (_parent == null)
	    return _graph;
	else
	    return _parent.getGraph();
    }

    /** Return the sink node of the interval **/
    public Node getSink() {
	return _sink;
    }

    /** Return the sink node of the interval **/
    public Node getChainSinkNode() {
	if (_next == null)
	    return _sink;
	else
	    return _next.getChainSinkNode();
    }

    public Node getRoot() {
	return _root;
    }

    public boolean isValid() {
	return _valid;
    }

    /** The sink Node <i>must</i> have at most one outgoing edge. This
     * method will return the target of this edge. **/
    public Node getSinkTarget() {
	Collection targets = _graph.successors(_sink);
	if (targets.size() == 0)
	    return null;
	return (Node) targets.iterator().next();
    }


    public String toShortString() {
	return "I"+_graph.nodeString(_root);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Interval "+toShortString());
	
	if (_parent == null)
	    sb.append(" no parent");
	else
	    sb.append(" parent="+is(_parent));
	
	if (_children != null) {
	    sb.append (" children=");
	    for (Iterator i=_children.values().iterator();i.hasNext();) {
		IntervalChain r=(IntervalChain) i.next();
		sb.append(r.toShortString()+" ");
	    }
	} else {
	    if (_specialChildren != null) {
		sb.append(" special=");
		for (Iterator i=_specialChildren.iterator();i.hasNext();) {
		    Node n = (Node) i.next();
		    sb.append(ns(n)+" ");
		}
	    } else		
		sb.append(" no children");
	}

	sb.append(" sink=");
	if (_sink == null)
	    sb.append("none");
	else 
	    sb.append(ns(_sink));

	if (_next != null) {
	    sb.append(" next="+is(_next));	}

	sb.append("\n");

	// Print all children
	if (_children != null) {
	    for (Iterator i=_children.values().iterator();i.hasNext();) {
		IntervalChain r=(IntervalChain) i.next();
		sb.append(r);
	    }
	}
	// Print next
	if (_next != null)
	    sb.append(_next);

	return sb.toString();
    }

    protected String ns(Node n) {
	if (n != null)
	    return _graph.nodeString(n);
	else 
	    return "null";
    }

    protected String is(IntervalChain i) {
	Node n = i._root;
	return _graph.nodeString(n);
    }

    protected void _init() throws IllegalActionException {

	Collection rootSuccessors = _graph.successors(_root);
	int numSuccessors = rootSuccessors.size();
	Node child;

	_valid = true;
	switch(numSuccessors) {
	case 0:
	    // No successor Nodes to _root (this is a leaf node).
	    // Set the _sinkNode to _root and end the chain.
	    _sink = _root;
	    _next = null;
	    System.out.print("Leaf Node="+ns(_root));
	    break;
	case 1:
	    // _root has only one successor. This Interval is
	    // a single node - set _sinkNode to _root.
	    child = (Node) rootSuccessors.iterator().next();
	    _sink = _root;
	    // If the _root dominates the child, then the child
	    // is the next Node in a chain. Continue the chain.
	    // If the _root does not dominate the child, the chain
	    // ends here (a dominator will create an Interval for
	    // the child).
	    if (_graph.dominates(_root,child)) {
		// root dominate the one child
		_next = new IntervalChain(this,child);
	    } else {
		// root does not dominate the one child
		_next = null;
	    }
	    System.out.print("Sequential Node="+ns(_root));
	    if (_next != null)
		System.out.print(" next="+is(_next));
	    else
		System.out.print(" no next");
	    break;
	default:	    
	    
	    // There are three types of Intervals that occur with
	    // fork Nodes:
	    // 1. Its children are all dominated and the Immediate
	    //    post dominator is dominated by fork (i.e. this is
	    //    a standard fork/join interval). This is the
	    //    "normal" fork interval.
	    // 2. The node forks, but its children are not dominated.
	    //    This is an "invalid" interval. (i.e. multiple
	    //    exits). Invalid intervals will be part of 
	    //    "special" intervals.
	    // 3. The node forks, but it has children that are
	    //    "invalid". This node will include a sub-tree
	    //    of nodes and will terminate at the immediate
	    //    postDominator of the root.

	    // iterate over direct children. Children are either dominated
	    // by the root or not. If they are dominated, create a new
	    // interval and store. 
	    // If not, save nonDominated children in a list.
	    HashMap domChildren = new HashMap(numSuccessors);
	    Vector nonDomChildren = new Vector(numSuccessors);
	    boolean specialInterval = false;
	    for (Iterator i = rootSuccessors.iterator(); i.hasNext();) {
		child = (Node) i.next();
		if (_graph.dominates(_root,child)) {
		    // root dominates current child.
		    IntervalChain childInterval = new IntervalChain(this,child);
		    domChildren.put(child,childInterval);
		    if (!childInterval.isValid())
			specialInterval = true;
		} else {
		    // root does not dominate current child.
		    nonDomChildren.add(child);
		}
	    }

	    // Determine the immediate post-dominator of the current
	    // node. 
	    Node immediatePostDominator = 
		_graph.getImmediatePostDominator(_root);
	    System.out.print("Fork "+ns(_root)+" ipd="+
			       ns(immediatePostDominator));

	    // Process nonSpecial intervals
	    if (!specialInterval) {

		// Determine whether there is a common target
		// for children (i.e. is this a Normal interval).
		boolean canMerge = true;
		System.out.print(" domChildren=");
		for (Iterator i = domChildren.values().iterator();
		     i.hasNext() && canMerge;) {
		    IntervalChain childInterval  = (IntervalChain) i.next();
		    System.out.print(is(childInterval)+" ");
		    // The childInterval must point to the ipd or
		    // it must be the ipd
		    if (childInterval.getSinkTarget() != 
			immediatePostDominator &&
			childInterval.getRoot() != immediatePostDominator)
			canMerge = false;
		}
		for (Iterator i = nonDomChildren.iterator(); 
		     i.hasNext() && canMerge;) {
		    child = (Node) i.next();
		    if (child != immediatePostDominator)
			canMerge = false;
		}
		// If Interval cannot be merged, it is invalid
		if (!canMerge) {
		    _valid = false;
		    System.out.print(" invalid");
		}
		else {
		    System.out.print(" normal");
		    // Add an extra Node if the _root does not dominate
		    // the immediatePostDominator
		    if (!_graph.dominates(_root,immediatePostDominator)) {
			System.out.print(" merge node added");
			immediatePostDominator = 
			    addImmediatePostDominator(_root,domChildren,
						      immediatePostDominator);
		    }
		}
		_children = domChildren;
	    } else {
		// a special interval
		System.out.print(" special");
		Vector nodes = new Vector();
		boolean addMode=false;
		for (Iterator search = _graph.topologicalSort().iterator();
		     search.hasNext();) {
		    Node n = (Node) search.next();
		    if (addMode) {
			nodes.add(n);
			if (n==immediatePostDominator)
			    addMode = false;
		    } else {
			if (n==_root) {
			    addMode = true;
			    nodes.add(n);
			}
		    }
		}		
		_specialChildren = nodes;
	    }

	    if (!_valid)
		break;

	    // continue chain (specify next in chain)

	    // Should a target fork node be split? 
	    immediatePostDominator = 
		splitTargetForkNode(immediatePostDominator);
	    _sink = immediatePostDominator;

	    Node n = getSinkTarget();
	    if (n != null)
		_next = new IntervalChain(this,n);
	    else
		_next = null;
	} 
	System.out.println();
    }

    protected Node splitTargetForkNode(Node ipd) {
	Collection ctEdges = _graph.outputEdges(ipd);
	if (ctEdges.size() > 1) {
	    String newNodeWeight="";
	    Vector remove = new Vector(_graph.inputEdges(ipd).size());
	    // Figure out the weight and collect edges
	    for (Iterator i=_graph.inputEdges(ipd).iterator();i.hasNext();) {
		Edge e = (Edge) i.next();
		newNodeWeight += ns(e.source());
		remove.add(e);
	    }	    	    
	    Node newNode = _graph.addNodeWeight(newNodeWeight);
	    for (Iterator i=remove.iterator();i.hasNext();) {
		Edge e = (Edge) i.next();
		_graph.removeEdge( e );
		_graph.addEdge(e.source(),newNode);		
	    }
	    _graph.addEdge(newNode, ipd);
	    return newNode;
	}
	return ipd;
    }

    protected Node addImmediatePostDominator(Node root, 
					     HashMap domChildren,
					     Node ipd) throws IllegalActionException {
	// Add new target node	
	Node newNode = _graph.addNodeWeight(new String(ns(root)+ns(ipd)));
	// Add edge between new target Node and common non dominated
	// Node
	_graph.addEdge(newNode,ipd);
	// Iterate through dominated intervals and change the edge
	// leaving the sink node to the newNode 
	for (Iterator i = domChildren.values().iterator();i.hasNext();) {
	    IntervalChain ic = (IntervalChain) i.next();
	    Node sink = ic.getChainSinkNode();
	    Edge e = (Edge) _graph.outputEdges(sink).iterator().next();
	    _graph.removeEdge(e);
	    _graph.addEdge(sink,newNode);
	}
	
	// Get all edges from _root to commonTarget and move them
	// to newNode
	for (Iterator i = _graph.outputEdges(root).iterator();
	     i.hasNext();) {
	    Edge e = (Edge) i.next();
	    if (e.sink() == ipd) {
		_graph.removeEdge(e);
		_graph.addEdge(_root,newNode);
	    }
	}
	_graph.updateDominators();
	//System.out.println("updated"+_graph);

	return newNode;
    }

    public static IntervalChain _main(String args[]) {
	IntervalChain ic=null;
	try {
	    DominatorCFG _cfg = DominatorCFG._main1(args);
	    ic = new IntervalChain(_cfg);
  	    ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile("interval",_cfg);
	    
	} catch (IllegalActionException e) {
	    System.err.println(e);
	    System.exit(1);
	}
	System.out.println("IntervalChain=\n"+ic);
	return ic;
    }

    public static void main(String args[]) {
	IntervalChain ic = _main(args);
    }

    /** The top-level DAG associated with this IntervalChain **/
    protected DominatorCFG _graph;

    /** The Root Node of the interval (i.e. the entry Node of the
     * Interval). **/
    protected Node _root;
       
    /** This Node is the sink for the current Interval. By definition, each
     * IntervalChain must have one entry point (_root) and one exit point. The
     * _sink is the exit point of the Interval.
     **/
    protected Node _sink;

    /** The parent Interval of this Interval. If _parent is null, this
     * interval is the top-level interval **/
    IntervalChain _parent;

    /** An Interval is a linked list of sequentially executing intervals.
     * _next is the next Interval in the list. If _next is null, this
     * is the end of the interval chain.
     **/
    protected IntervalChain _next;

    protected boolean _valid;

    HashMap _children;

    Vector _specialChildren;
}
