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

package ptolemy.copernicus.jhdl.soot;

import ptolemy.copernicus.jhdl.util.*;

import ptolemy.graph.*;
import soot.toolkits.graph.Block;
import soot.*;
import soot.jimple.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// 
/**
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class ValueMap extends HashListMap {
    
    public ValueMap(SootBlockDirectedGraph graph) {
	super();
	_graph = graph;
    }

    public ValueMap(ValueMap vm) {
	super(vm);
	_graph = vm._graph;
    }

    public Object clone() {
	return new ValueMap(this);
    }

    public Node getValueNode(Value v) {
	if (containsKey(v))
	    return (Node) getLast(v);
	if (v instanceof InstanceFieldRef) {
	    InstanceFieldRef ifr = 
		getMatchingInstanceFieldRef((InstanceFieldRef) v);
	    Node ifrNode = (Node) getLast(ifr);
	    //add(v,n);
	    return ifrNode;
	}
	return null;
    }

    public Node getOrAddValueNode(Value v) {
	Node n = getValueNode(v);
	if (n == null)
	    return addValueNode(v);
	else
	    return n;
    }

    public Node addValueNode(Node n) {
	return addValueNode((Value) n.getWeight());
    }

    public Node addValueNode(Value v) {
	Value nodeValue = v; 
	if (v instanceof InstanceFieldRef) {
	    InstanceFieldRef ifr = 
		getMatchingInstanceFieldRef((InstanceFieldRef) v);
	    if (ifr != null)
		nodeValue = ifr;
	}
	Node n = _graph.addNodeWeight(nodeValue);
	add(nodeValue,n);
	return n;
    }

    // origValue will map to the list associated with newValue
    /*
    public void replaceValueNode(Value origValue, Value newValue) {
	List l = getList(newValue);
	setList(origValue,l);
	//return getValueNode(newValue);
    }
    */

    /**
     * Search all Nodes in the graph to see if there is an InstanceFieldRef
     * that matches the InstanceField passed as an argument.
     **/
    public InstanceFieldRef getMatchingInstanceFieldRef(InstanceFieldRef ifr) {

	// Iterate through all nodes in the graph and see if there
	// is a matching InstanceFieldRef (i.e. same base and
	// same field).
	InstanceFieldRef dupIfr = null;
	for (Iterator i = _graph.nodes().iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (n.getWeight() instanceof InstanceFieldRef) {
		InstanceFieldRef t_ifr = (InstanceFieldRef) n.getWeight();
		if (equal(t_ifr, ifr) && ifr != t_ifr) {
		    dupIfr = t_ifr;
		}
	    }
	}
	return dupIfr;
    }

    public static boolean equal(InstanceFieldRef ifr1, InstanceFieldRef ifr2) {
	return ifr1.getBase().equals(ifr2.getBase()) &&
	    ifr1.getField().equals(ifr2.getField());
    }

    public void mergeSerial(SootBlockDirectedGraph succeedingGraph) {

	// temporary hashmap between old nodes & new. 
	// Used when connecting edges
	HashMap nodeMap = new HashMap(); 

	// Obtain required definitions of succGraph
	Collection undrivenNodes = succeedingGraph.requiredDefinitions();

	// Add all nodes from dfg to graph
	for (Iterator i = succeedingGraph.nodes().iterator(); i.hasNext();) {
	    Node node = (Node) i.next();	    
	    //if (DEBUG) System.out.print("Adding node="+node);

	    // See if the given node in the succGraph needs to be
	    // driven by a Node in the current graph
	    Node sourceNode = null;
	    if (undrivenNodes.contains(node)) {
		// Search current graph for a Node with an equivelant
		// Value.
		Value nodeValue = (Value) node.getWeight();
		sourceNode = this.getValueNode(nodeValue);
	    }
	    // No driver - create new node
	    if (sourceNode == null) {
		sourceNode = this.addValueNode(node);
	    }
	    nodeMap.put(node,sourceNode);
	}

	// Iterate through all edges and add to graph
	for (Iterator i = succeedingGraph.edges().iterator(); i.hasNext();) {
	    Edge e = (Edge) i.next();
	    Node src = e.source();
	    if (nodeMap.containsKey(src))
		src = (Node) nodeMap.get(src);
	    Node snk = e.sink();		
	    if (nodeMap.containsKey(snk))
		snk = (Node) nodeMap.get(snk);

	    // Check and see if the current graph already has an
	    // edge between the two nodes. If not, add the edge.
	    if (_graph.successorEdges(src,snk).size() == 0) {
		if (e.hasWeight())
		    _graph.addEdge(src,snk,e.getWeight());
		else
		    _graph.addEdge(src,snk);
	    }
	}	
    }

    protected SootBlockDirectedGraph _graph;

}
