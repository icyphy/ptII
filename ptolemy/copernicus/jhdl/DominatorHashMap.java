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

import java.util.*;
import ptolemy.graph.*;
import soot.toolkits.graph.Block;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// DominatorHashMap
/**
 * This class determines the dominators of each Block within a CFG.
 * The key of each entry in this hashMap is a node within a
 * DirectedGraph. The Values of the HashMap are Vectors that contain
 * references to dominating nodes.

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class DominatorHashMap extends HashMap {

    public DominatorHashMap(DirectedGraph g) throws IllegalActionException {
	super(g.nodeCount());
	_graph = g;
	_computeDominators();
    }

    public Vector getDominators(Node n) {
	return (Vector) get(n);
    }
    
    /** Returns true if Node d dominates Node n **/
    public boolean dominates(Node d, Node n) {
	Vector dominates = getDominators(n);
	if (dominates.contains(d))
	    return true;
	else
	    return false;
    }

    public Node getDeepestDominator(Node n) {
	Vector d = (Vector) get(n);
	return (Node) d.lastElement();
    }

    public Collection getPartialOrder() {
	return _sortedNodes;
    }

    // Is node n1 deeper in topological order than n2?
    public boolean deeperDominator(Node n1, Node n2) {
	System.out.println("Node "+BlockControlFlowGraph.nodeString(n1)+
			   " dd="+
			   _sortedNodes.indexOf(getDeepestDominator(n1))
			   +" Node "+
			   BlockControlFlowGraph.nodeString(n2)+" dd="+
			   _sortedNodes.indexOf(getDeepestDominator(n2)));
						
	return _sortedNodes.indexOf(getDeepestDominator(n1)) >
	    _sortedNodes.indexOf(getDeepestDominator(n2));
    }

    public boolean equalDeepestDominator(Node n1, Node n2) {
	return _sortedNodes.indexOf(getDeepestDominator(n1)) ==
	    _sortedNodes.indexOf(getDeepestDominator(n2));
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	// debug printing
	for (Iterator i=keySet().iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    sb.append(BlockControlFlowGraph.nodeString(n)+":");
	    Vector v = (Vector) get(n);
	    for (Iterator j=v.iterator();j.hasNext();) {
		Node d = (Node) j.next();
		sb.append(BlockControlFlowGraph.nodeString(d)+" ");
	    }
	    sb.append("\r\n");
	}
	// topological sort
	/*
	for (Iterator i=_sortedNodes.iterator();i.hasNext();) {
	    Node n= (Node) i.next();
	    sb.append(BlockControlFlowGraph.nodeString(n)+"="+_sortedNodes.indexOf(n)+"\n");
	}
 	*/
	return sb.toString();
    }


    // see page 671 in DragonBook
    protected void _computeDominators() throws IllegalActionException {

	// Sort the graph Nodes
	_sortedNodes = (List) _graph.attemptTopologicalSort(_graph.nodes());
	Node root = (Node) _sortedNodes.iterator().next();
	int graphSize = _graph.nodeCount();

 	// Create a Dominator Vector for each node in graph
	for (Iterator i = _graph.nodes().iterator(); i.hasNext();) {
	    Node n = (Node) i.next();
	    Vector d=null;
	    if (n == root) {
		d = new Vector(1);
		d.add(root);
	    } else {
		d = new Vector(graphSize);
		// Inititally, assume all nodes dominate current node
		for (Iterator j=_graph.nodes().iterator(); j.hasNext();) {
		    d.add( (Node) j.next() );
		}
	    }
	    // Place initalized Vector into the hashMap
	    put(n,d);
	}

	boolean changed=false;
	do {
	    changed = false;
	    for (Iterator i=_graph.nodes().iterator(); i.hasNext();) {
		Node n = (Node) i.next();
		if (n==root)
		    continue;
//    		System.out.println("Dominators for block "+
//    				   ((Block)n.weight()).getIndexInMethod());
		Vector nDominators = (Vector) get(n);
		// Loop through predecessors of n
		Vector intersection=null;
		for (Iterator j=_graph.predecessors(n).iterator();j.hasNext();) {
		    Node p = (Node) j.next();		    
		    Vector pDominators = (Vector) get(p);
		    if (intersection == null) {			
			// If intersection vector is null, initialize it
			// with the dominators of p
			intersection = new Vector(pDominators.size());
			intersection.addAll(pDominators);
		    } else {
			// compute the intersection with p
			Vector remove = new Vector(intersection.size());
  			for (Iterator k=intersection.iterator();k.hasNext();) {
  			    Object o=k.next();
  			    if (!(pDominators.contains(o)))
				remove.add(o);
  			}
			// remove elements that need removing
			for (Iterator k=remove.iterator();k.hasNext();) {
			    intersection.remove(k.next());
			}
		    }
		}
		// Vector intersection now contains the intersection of
		// the dominators of all predecessors. 
		// Add itself.
		intersection.add(n);
		if (intersection.size() < nDominators.size()) {
  		    changed = true;
		    put(n,intersection);
		}
	    }
	} while(changed);
    }

    protected DirectedGraph _graph;
    protected List _sortedNodes;
}
