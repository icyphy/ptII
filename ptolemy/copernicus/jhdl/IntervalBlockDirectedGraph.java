/* A Data Flow Graph generated from an IntervalChain

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import soot.SootField;
import soot.Value;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.UnitBox;
import soot.ValueBox;

import soot.jimple.InstanceFieldRef;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.internal.JimpleLocal;

import soot.toolkits.graph.Block;

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.copernicus.jhdl.soot.*;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.graph.Node;
import ptolemy.graph.Edge;

public class IntervalBlockDirectedGraph extends SootBlockDirectedGraph {

    public IntervalBlockDirectedGraph(IntervalChain ic) 
	throws JHDLUnsupportedException {

	// Create DFG from the Block object associated with the root
	// Node of the given IntervalChain
	super((Block) ic.getRoot().getWeight());
	_ic = ic;
	_processChain();
    }

    protected void _processChain() throws JHDLUnsupportedException {

	// 1. Create graph for next inverval in chain if there is one.
	//    (This algorithm works bottom up so that all required
	//     definitions downstream are known before upstream
	//     DFGs are constructed).
	IntervalChain nextChain = _ic.getNext();
	if (nextChain != null)
	    _next = new IntervalBlockDirectedGraph(nextChain);
	else
	    _next = null;


	// 2. Merge Children Nodes
	if (_ic.isSimpleMerge())
	    simpleMerge();
	else if (_ic.isSpecialMerge()) {
	    throw new JHDLUnsupportedException("Special Nodes not yet supported");
	}	

	// 3. Connect previously created chain to this node
	if (_next != null) { 
  	    mergeSerial(_next);
	}

    }

    protected void simpleMerge() throws JHDLUnsupportedException {

	// key=IntervalChain root Node, Value=IntervalChain
	Map children = _ic.getChildren();

	// Save the ValueMap for the current Graph (i.e. pre-merge)
	// TODO

	// Iterate over the children:
	// - merge the child
	// - save the ValueMap for the control path 
	// key=IntervalBlockDirectedGraph, Value=ValueMap
	HashMap childrenValueMaps = new HashMap(children.size());

	for (Iterator i = children.values().iterator();i.hasNext();) {
	    IntervalChain childInterval = (IntervalChain) i.next();
	    IntervalBlockDirectedGraph childDFG = 
		new IntervalBlockDirectedGraph(childInterval);	    
	    mergeSerial(childDFG);
	    // Need to save new ValueMap
	    // _valueMap = childrenValueMaps.put(childDFG,_valueMap);
	}

	// Restore _valueMap
	//_valueMap = rootVM;

	if (children.values().size() == 1) {
	    // merge root w/child
	} else if (children.values().size() == 2) {
	    // merge two children
	} else {
	    // A switch.
	    throw new JHDLUnsupportedException("Switches not yet supported");
	    // merge switch targets
	}

	/*
	// - Create a DFG for each child associated with this fork.
	// - Update the required definitions for this Node based on
	//   definitions required by each child. 
	// - Merge each DFG in a serial fashion (children DFGs are
	//   not multiplexed here).
	
	
	if (children.values().size() == 1) {
	    // Only one branch. Merge the root graph with
	    // the branch graph.
	    
	    Iterator i = childrenValueMaps.keySet().iterator();
	    IntervalBlockDirectedGraph childDFG = (IntervalBlockDirectedGraph) i.next();
	    ValueMap childMap = (ValueMap) childrenValueMaps.get(childDFG);
	    
	    for (i=childMap.getDefs().keySet().iterator();i.hasNext();) {
		Value origv = (Value) i.next();
		Node n = (Node) childMap.getDefs().get(origv);
		//  		    System.out.println("New def="+n+" id="+
		//  				       System.identityHashCode(n));
		if (isRequired(origv)) {
		    //  			System.out.println("def needed");
		    Node childn = childMap.getLast(origv);
		    Node parentn = getOrCreateNode(origv);
		    _multiplexNodes(childDFG,childn,parentn);
		    //System.out.println("Multiplexing node "+childn);
		}
	    }
	} else if (children.values().size() == 2) {
	    
	    // Two branches. Merge all of their outputs.		
	    // Obtain the defintions defined by both children
	    Iterator i = childrenValueMaps.keySet().iterator();
	    IntervalBlockDirectedGraph child1DFG = (IntervalBlockDirectedGraph) i.next();
	    IntervalBlockDirectedGraph child2DFG = (IntervalBlockDirectedGraph) i.next();
	    ValueMap child1Map = (ValueMap) childrenValueMaps.get(child1DFG);
	    ValueMap child2Map = (ValueMap) childrenValueMaps.get(child2DFG);
	    
	    // Iterate through all of child1Values
	    for (i=child1Map.getDefs().keySet().iterator();i.hasNext();) {
		Value origv = (Value) i.next();
		if (isRequired(origv)) {
		    Node child1n = (Node) child1Map.getDefs().get(origv);
		    if (child2Map.getDefs().containsKey(origv)) {
			Node child2n = (Node) child2Map.getDefs().get(origv);
			_multiplexNodes(child1DFG,child1n,child2n);
		    } else {
			Node parentn = getOrCreateNode(origv);
			_multiplexNodes(child1DFG,child1n,parentn);
		    }
		}
	    }
	    // Iterate through all of child2Values
	    for (i=child2Map.getDefs().keySet().iterator();i.hasNext();) {
		Value origv = (Value) i.next();
		if (isRequired(origv) && 
		    !child1Map.getDefs().containsKey(origv)) {
		    Node child2n = (Node) child2Map.getDefs().get(origv);
		    Node parentn = getOrCreateNode(origv);
		    _multiplexNodes(child2DFG,child2n,parentn);
		    //  			System.out.println("Multiplexing node from parent "+
		    //  					   child2n);
		}
	    }
	} else {
	    // A switch.
	    throw new JHDLUnsupportedException("Switches not yet supported");
	}
	*/
    }
    
    /** returns new lValues **/
    public void mergeSerial(IntervalBlockDirectedGraph dfg) {
	/*
	// temporary hashmap between old nodes & new. 
	// Used when connecting edges
	HashMap nodeMap = new HashMap(); 
	
	// Add all nodes to graph
	for (Iterator i = dfg.nodes().iterator(); i.hasNext();) {
	    
	    Node node = (Node) i.next();	    

	    if (DEBUG) System.out.print("Adding node="+node);
	    
	    Node driver = findNodeDriver(dfg,node);
	    if (driver != null)
		nodeMap.put(node,driver);
	    else {
	    	if (node.getWeight() instanceof InstanceFieldRef) {
		    InstanceFieldRef ifr = (InstanceFieldRef) node.getWeight();
		    InstanceFieldRef nifr =
			_getMatchingInstanceFieldRef((InstanceFieldRef) ifr);
		    if (nifr != null) {
			Node newnode = addNodeWeight(nifr);
			_valueMap.addInstanceFieldRef(nifr,newnode);
			nodeMap.put(node,newnode);
		    } else
			addNode(node);
		} else
		    addNode(node);		
	    }
	}
	    
	// Iterate through all edges and add to graph
	for (Iterator i = dfg.edges().iterator(); i.hasNext();) {
	    Edge e = (Edge) i.next();
	    Node src = e.source();
	    if (nodeMap.containsKey(src))
		src = (Node) nodeMap.get(src);
	    Node snk = e.sink();		
	    if (nodeMap.containsKey(snk))
		snk = (Node) nodeMap.get(snk);

	    if (successorEdges(src,snk).size() == 0) {
		// Add edge (avoid duplicates)
		if (e.hasWeight())
		    addEdge(src,snk,e.getWeight());
		else
		    addEdge(src,snk);
		Object snkWeight = snk.getWeight();
		if (snkWeight instanceof Local ||
		    snkWeight instanceof InstanceFieldRef)
		    _valueMap.addNewDef(snk);
	    }
	}	
	*/
    }

    public Node addNode(Node n) {

	super.addNode(n);
	Object weight = n.getWeight();
  	if (weight instanceof Local) {
	    _valueMap.addLocal((Local)weight,n);
  	}
	if (weight instanceof InstanceFieldRef) {
	    _valueMap.addInstanceFieldRef((InstanceFieldRef)weight,n);
	}
	return n;
    }

    /* This method will find a Node in the current graph that should
     * drive the Node n in the passed in dfg. If there is no Node 
     * in the current graph that with the Value as that of n, null
     * is returned (i.e. no driver)
     */
    protected Node findNodeDriver(BlockDataFlowGraph dfg, Node n) {
	Object weight = n.getWeight();
	if (weight instanceof Local) {
	    if (dfg.inputEdges(n).size() == 0) {
		// return 
		return _valueMap.getLastLocal(weight);
	    } else
		return null; // Node is being driven
	}
	if (weight instanceof InstanceFieldRef) {
	    InstanceFieldRef ifr = (InstanceFieldRef) weight;
	    if (dfg.inputEdges(n).size() == 1) {
		return _valueMap.getMatchingInstanceFieldRefNode(ifr);
	    }
	}
	return null;
    }

    public IntervalChain getIntervalChain() { return _ic; }

    public String toBriefString() {
	return _ic.toShortString();
    }

    
    protected Node _multiplexNodes(IntervalBlockDirectedGraph child1DFG, 
				   Node child1,
				   Node child2) {
	/*
	Value value = (Value) child1.getWeight();

	// Get the edges associated with the original CFG.
	Node cNode = getConditionNode();

	Node trueNode = child1;
	Node falseNode = child2;
	if (!isTrueNode(child1DFG)) {
	    trueNode = child2;
	    falseNode = child1;
	}
	
	BinaryMuxNode bmn = new BinaryMuxNode(trueNode,falseNode,cNode,
					      value.toString());
	Node muxNode = addNodeWeight(bmn);

	addEdge(trueNode,muxNode,"true");
	addEdge(falseNode,muxNode,"false");
	addEdge(cNode,muxNode,"condition");

	Node newValueNode=null;
	try {
	    newValueNode = _addLeftValue(value);
	} catch (JHDLUnsupportedException e) {
	}

	// _addSimpleNode, _addInstanceField
	addEdge(muxNode,newValueNode);
	return newValueNode;
	*/
	return null;
    }
    
    /** move to higher level **/
    public Node getOrCreateNode(Value v) throws JHDLUnsupportedException {
	/*
	Node newNode = _valueMap.getLast(v);
	if (DEBUG)
	    System.out.println("newNode = "+newNode+" value="+v+
			       " id="+System.identityHashCode(v));
	if (newNode == null) {
  	    if (v instanceof InstanceFieldRef) {
  		InstanceFieldRef ifr = (InstanceFieldRef) v;
//  		InstanceFieldRef ifr_p = _getMatchingInstanceFieldRef(ifr);
//  		newNode = _valueMap.getLast(ifr_p);
//  		if (newNode == null)
		newNode = _createInstanceFieldRef(ifr);
	    } else if (v instanceof Local) {
		newNode = _createLocal((Local)v);
	    }
	}
	return newNode;
	*/
	return null;
    }

    public boolean isRequired(Value v) {
	/*
	if (_completeRequiredDefinitions.contains(v))
	    return true;
	if (v instanceof InstanceFieldRef) {
	    InstanceFieldRef vifr = (InstanceFieldRef) v;
	    for (Iterator i=_completeRequiredDefinitions.iterator();i.hasNext();) {
		Object o = i.next();
		if (o instanceof InstanceFieldRef) {
		    InstanceFieldRef oifr = (InstanceFieldRef) o;
		    if (vifr.getBase().equals(oifr.getBase()) &&
			vifr.getField().equals(oifr.getField()))
			return true;
		}
	    }
	}
	*/
	return false;
    }

    /** Return the condition Value object associated with a fork Node
     **/ 
    public Node getConditionNode() {
	IfStmt ifs = getIfStmt();
	Value v = ifs.getCondition();
	return node(v);
    }

    public IfStmt getIfStmt() {
	Node root = _ic.getRoot();
	Block b = (Block) root.getWeight();
	IfStmt ifs = (IfStmt) b.getTail();
	return ifs;
    }

    public boolean isTrueNode(IntervalBlockDirectedGraph cidfg) {

	DirectedAcyclicCFG graph = _ic.getGraph();	
	IfStmt ifs = getIfStmt();

	Node childCFGNode = cidfg._ic.getRoot();

	Block dest = (Block) childCFGNode.getWeight();

//  	System.out.println("IFstmt="+ifs+" target="+
//  			   ifs.getTargetBox().getUnit()+" dest head="+
//  			   dest.getHead());


	if (ifs.getTargetBox().getUnit() == dest.getHead())
	    return true;
	else
	    return false;
    }

    public Collection getInstanceFieldRefInputDefinitions() {
	ArrayList nodes = new ArrayList();
	// iterate over all nodes in the graph
	for (Iterator i = nodes().iterator(); i.hasNext();) {
	    Node node = (Node) i.next();
	    Object weight = node.getWeight();
	    if (inputEdgeCount(node) == 1 &&
		weight instanceof InstanceFieldRef) {
		nodes.add(node);
	    }
	}
	return nodes;
    }

    public static IntervalBlockDirectedGraph createIntervalBlockDirectedGraph(String args[]) 
	throws JHDLUnsupportedException {
	IntervalChain ic = IntervalChain.createIntervalChain(args,true);
	return new IntervalBlockDirectedGraph(ic);
    }

    public static boolean DEBUG=false;

    public static void main(String args[]) {
	//BlockDataFlowGraph.DEBUG=true;
	IntervalBlockDirectedGraph im = null;
	try {
	    im = createIntervalBlockDirectedGraph(args);	
	} catch (JHDLUnsupportedException e) {
	    e.printStackTrace();
	    System.exit(1);
	}

//  	System.out.println(im);
//  	BlockDataFlowGraph graphs[] = 
//  	    BlockDataFlowGraph.getBlockDataFlowGraphs(args);
//  	for (int i = 0;i<graphs.length;i++)
//  	    PtDirectedGraphToDotty.writeDotFile("bbgraph"+i,
//  						graphs[i]);
	PtDirectedGraphToDotty.writeDotFile("merge",im);
    }

    protected IntervalChain _ic;
    protected IntervalBlockDirectedGraph _next;


    protected ValueMap _valueMap;
}

class ValueMap {
    public ValueMap(MapList l, MapList ifr) {
	locals = l;
	instanceFieldRefs = ifr;
	newDefs = new HashMap();
    }
    public Object clone() {
	MapList l = (MapList) locals.clone();
	MapList ifrs = (MapList) instanceFieldRefs.clone();
	ValueMap vm = new ValueMap(l,ifrs);
	return vm;
    }
    public void addLocal(Local l,Node n) {
  	locals.add(l,n);
	//newDefs.add(n);
    }
    public void addInstanceFieldRef(InstanceFieldRef ifr,Node n) {
  	instanceFieldRefs.add(ifr,n);
	//newDefs.add(n);
    }
    public void addNewDef(Node n) {
	newDefs.put(n.getWeight(),n);
    }
    public Map getDefs() { return newDefs; }
    public Node getLast(Object v) {
	if (v instanceof Local)
	    return getLastLocal(v);
	if (v instanceof InstanceFieldRef)
	    return getLastInstanceFieldRef((InstanceFieldRef)v);
	return null;
    }
    public Node getLastLocal(Object v) {
	return (Node) locals.getLast(v);
    }
    public Node getLastInstanceFieldRef(InstanceFieldRef v) {
	return (Node) instanceFieldRefs.getLast(v);
    }
    public Node getMatchingInstanceFieldRefNode(InstanceFieldRef ifr) {
	SootField field = ifr.getField();
	Value baseValue = ifr.getBase();
	InstanceFieldRef previous=null;
	for(Iterator it = instanceFieldRefs.keySet().iterator();it.hasNext();) {
	    InstanceFieldRef ifr_n = (InstanceFieldRef) it.next();
	    if (ifr_n.getBase().equals(baseValue) &&
		ifr_n.getField().equals(field)) {
		previous = ifr_n;
	    }
	}
	return getLastInstanceFieldRef(previous);	
    }

    public MapList locals;
    public MapList instanceFieldRefs;
    public Map newDefs;
}

