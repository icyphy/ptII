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
//// ValueMap
/**

This is a HashMap that maps Values to lists of Nodes.

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

    public DirectedGraph getGraph() { return _graph; }

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
	return addValueNode(v,false);
    }

    // TODO: left flag not being called
    public Node addValueNode(Value v, boolean left) {
	Value nodeValue = v; 
	if (v instanceof InstanceFieldRef) {
	    InstanceFieldRef ifr = 
		getMatchingInstanceFieldRef((InstanceFieldRef) v);
	    if (ifr != null)
		nodeValue = ifr;
	}
	Node n = _graph.addNodeWeight(nodeValue);
	add(nodeValue,n);
//    	if (left) {
//    	    _definitions.add(nodeValue);
//    	    System.out.println("Left:"+nodeValue+" of type "+
//    			       nodeValue.getClass().getName());
//    	}
	return n;
    }

    /**
     * Search all Nodes in the graph to see if there is an InstanceFieldRef
     * that matches the InstanceField passed as an argument.
     **/
    public InstanceFieldRef getMatchingInstanceFieldRef(InstanceFieldRef ifr) {

	// Iterate through all nodes in the graph and see if there
	// is a matching InstanceFieldRef (i.e. same base and
	// same field).
	InstanceFieldRef dupIfr = null;
//	for (Iterator i = _graph.nodes().iterator();i.hasNext();) {
	for (Iterator i = getNodes().iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (n.getWeight() instanceof InstanceFieldRef) {
		InstanceFieldRef t_ifr = (InstanceFieldRef) n.getWeight();
		if (equalIFR(t_ifr, ifr) && ifr != t_ifr) {
		    dupIfr = t_ifr;
		}
	    }
	}
	return dupIfr;
    }

    public static boolean equalIFR(InstanceFieldRef ifr1, InstanceFieldRef ifr2) {
	return ifr1.getBase().equals(ifr2.getBase()) &&
	    ifr1.getField().equals(ifr2.getField());
    }

    /**
     * Returns a Collection of all Values in the HashList.
     **/
    public Collection getValues() {
	return keySet();
    }

    public Collection getNodes(Value v) {
	return getList(v);
    }

    public Collection getNodes() {
	Vector nodes = new Vector (_graph.nodeCount());
	Collection values = getValues();
	for (Iterator i=values.iterator();i.hasNext();) {
	    Value v = (Value) i.next();
	    nodes.addAll(getNodes(v));
	}
	return nodes;
    }

    public boolean isVariableValue(Value v) {
	if (v instanceof Local || v instanceof InstanceFieldRef)
	    return true;
	return false;
    }

    /**
     * This method will return a Collection of Values in this ValueMap
     * that are variables (Value of type Local and InstanceFieldRef).
     **/
    public Collection getVariableValues() {
	Collection values = getValues();
	Vector variableValues = new Vector(values.size());
	for (Iterator i = values.iterator();i.hasNext();) {
	    Value v = (Value) i.next();
	    if (isVariableValue(v))
		variableValues.add(v);
	}
	return variableValues;
    }

    public Collection getAssignedNodes() {
	Collection c = getNodes();
	Collection an = new Vector(c.size());
	for (Iterator i = c.iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (isAssigned(n))
		an.add(n);
	}
	return an;
    }

    public Collection getAssignedValues() {
	Collection vs = getVariableValues();
	Collection av = new UniqueVector(vs.size());
	for (Iterator i = vs.iterator();i.hasNext();) {
	    Value v = (Value) i.next();
	    for (Iterator j = getNodes(v).iterator();j.hasNext();) {
		Node n = (Node) j.next();
		if (isAssigned(n))
		    av.add(v);
	    }
	}
	return av;
    }

    public boolean isAssigned(Node n) {
	Value v = (Value) n.getWeight();
	// See if the Value associated with the given Node
	if (!containsKey(v))
	    return false;
	if (!isVariableValue(v))
	    return false;
	if (v instanceof Local && 
	    _graph.inputEdgeCount(n) == 1)
	    return true;
	if (v instanceof InstanceFieldRef && 
	    _graph.inputEdgeCount(n) == 2)
	    return true;
	return false;
    }

    /**
     * This method will update the ValueMap to reflect the Nodes
     * found within the underlying graph. Specifically,
     * it will add Nodes/Values to the map corresponding to 
     * Nodes/Values found in the graph that are not in the ValueMap.
     *
     * TODO: remove Nodes/Values from ValueMap?
     **/
    public void updateMap() {

	// Clear all mappings
	clear();

	// Iterate over all Nodes within the graph (in topological order)
	List topologicalOrder = null;
	try {
	    topologicalOrder = _graph.topologicalSort(_graph.nodes());
	} catch (GraphActionException e) {
	}

	for (Iterator i = topologicalOrder.iterator(); i.hasNext(); ) {
	    Node n = (Node) i.next();
	    add(n.getWeight(),n);
	}	

    }

    /*
    public boolean isDefined(Value v) {
	List l = getCreateList(v);
	if (!isVariableValue(v))
	    return false;
	for (Iterator i=l.iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (v instanceof Local && 
		_graph.inputEdgeCount(n) == 1)
		return true;
	    if (v instanceof InstanceFieldRef && 
		_graph.inputEdgeCount(n) == 2)
		return true;
	}
	return false;
    }

    public Collection assignedVariables() {
	UniqueVector assignedValues = new UniqueVector();
	for (Iterator locals = getVariableValues().iterator();
	     locals.hasNext();) {
	    Value v = (Value) locals.next();
	    if (isDefined(v))
		assignedValues.add(v);
	}
	return assignedValues;
    }

    public Collection unassignedVariables() {
	UniqueVector unassignedValues = new UniqueVector();
	for (Iterator locals = getVariableValues().iterator();
	     locals.hasNext();) {
	    Value v = (Value) locals.next();
	    if (!isDefined(v))
		unassignedValues.add(v);
	}
	return unassignedValues;
    }
    */







    /**
     * This method will return a Collection of Nodes that are assigned
     * values within the graph. To find a Node that has been assigned 
     * a Value, this method will search for all Nodes with a Local
     * as a weight. Those Local Nodes that have an incoming edge
     * are considered "assignedNodes". If a given Value is assigned
     * more than once, only the latest Node will be returned.
     **/
    /*
    public Collection assignedNodes() {
	UniqueVector an = new UniqueVector();
	for (Iterator values = assignedValues().iterator();
	     values.hasNext();) {
	    Value v = (Value) values.next();
	    Node n = getValueNode(v);
	    an.add(n);
	}
	return an;
    }
    */

    /**
     * This method will return a Collection of Nodes that are not assigned
     * within the graph. An undefined node is a Local or InstanceFieldRef
     * that is never assigned a value.
     **/
    /*
    public Collection unassignedNodes() {
	Vector requiredNodes = new Vector();
	Iterator nodes = _graph.nodes().iterator();
	while (nodes.hasNext()) {
	    Node n = (Node) nodes.next();
	    Object nweight = n.getWeight();
	    if (nweight instanceof Local) {
		if (_graph.predecessors(n).size() == 0)
		    requiredNodes.add(n);
	    } else if (nweight instanceof InstanceFieldRef) {		
		if (_graph.predecessors(n).size() == 1)
		    requiredNodes.add(n);
	    }
	}
	return requiredNodes;
    }
    */

    /**
     * This method will merge a data flow graph as a successor of 
     * the current graph. Rather than passing the graph itself, the
     * ValueMap of the graph is passed in. The ValueMap is used
     * to decide which nodes in the successor graph are driven by
     * nodes in this graph.
     **/
    public void mergeSerial(ValueMap successor) {

	DirectedGraph succeedingGraph = successor.getGraph();

	// temporary hashmap between old nodes & new. 
	// Used when connecting edges.
	HashMap nodeMap = new HashMap(); 

	// Add all nodes from dfg to graph
	for (Iterator i = succeedingGraph.nodes().iterator(); i.hasNext();) {
	    Node node = (Node) i.next();	    
	    if (DEBUG) System.out.print("Adding node="+node+" with weight "+
					node.getWeight() + " of type " +
					node.getWeight().getClass().getName()
					+"\n");
	    Value nodeValue = (Value) node.getWeight();

	    Node newNode = null;
	    if (!successor.isAssigned(node)) {
		newNode = this.getValueNode(nodeValue);		    
	    }
	    if (newNode == null) {
		newNode = this.addValueNode(nodeValue);
	    }
	    nodeMap.put(node,newNode);	    
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

    protected boolean DEBUG = false;
    protected SootBlockDirectedGraph _graph;
    //    protected UniqueVector _definitions; 

}
