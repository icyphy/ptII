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

package ptolemy.copernicus.jhdl.soot;


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
	this(ic,null);
    }

    public IntervalBlockDirectedGraph(IntervalChain ic, 
				      UniqueVector _parentRequiredDefinitions) 
	throws JHDLUnsupportedException {

	// Create DFG from the Block object associated with the root
	// Node of the given IntervalChain
	super((Block) ic.getRoot().getWeight());
	_ic = ic;
	_processChain();
	_requiredDefinitions = new UniqueVector(_parentRequiredDefinitions);
    }

    public Collection getRequiredDefinitions() {
	return _requiredDefinitions;
    }

    protected void _processChain() throws JHDLUnsupportedException {

	// 1. Create graph for next inverval in chain if there is one.
	//    (This algorithm works bottom up so that all required
	//     definitions downstream are known before upstream
	//     DFGs are constructed).
	IntervalChain nextChain = _ic.getNext();
	if (nextChain != null) {
	    _next = new IntervalBlockDirectedGraph(nextChain);
	    _requiredDefinitions.addAll(_next.getRequiredDefinitions());
	} else {
	    _next = null;
	}

	// 2. Merge Children Nodes
	//    Update _requiredDefinitions by children
	if (_ic.isSimpleMerge())
	    simpleMerge();
	else if (_ic.isSpecialMerge()) {
	    throw new JHDLUnsupportedException("Special Nodes not yet supported");
	}	

	// Update required definitions
	_requiredDefinitions.addAll(_valueMap.unassignedValues());

	// 3. Connect previously created chain to this node
	if (_next != null) { 
  	    _valueMap.mergeSerial(_next._valueMap);
	}

    }

    protected void simpleMerge() throws JHDLUnsupportedException {

	// key=IntervalChain root Node, Value=IntervalChain
	Map children = _ic.getChildren();

	// Iterate over the children:
	// - merge the child
	// - save the ValueMap for the control path 
	// key=IntervalBlockDirectedGraph, Value=ValueMap
	HashMap childrenValueMaps = new HashMap(children.size());
	Iterator i = children.values().iterator();i.hasNext();
	while (i.hasNext()) {
	    IntervalChain childInterval = (IntervalChain) i.next();
	    IntervalBlockDirectedGraph childDFG = 
		new IntervalBlockDirectedGraph(childInterval);	    
	    ValueMap valueMapCopy = (ValueMap) _valueMap.clone();
	    valueMapCopy.mergeSerial(childDFG._valueMap);
	    childrenValueMaps.put(childDFG,valueMapCopy);
	    _requiredDefinitions.addAll(childDFG.getRequiredDefinitions());
	}

	Iterator childMapIterator = childrenValueMaps.values().iterator();
	int numChildren = children.values().size();
	ValueMap childrenMaps[] = new ValueMap[numChildren];
	if (numChildren == 1) {
	    // merge root w/child
	    ValueMap childMap = (ValueMap) childMapIterator.next();
	    childrenMaps[0] = childMap;
	    joinOneChild(childMap,_requiredDefinitions);
	} else if (numChildren == 2) {
	    // merge two children
	    ValueMap childMap1 = (ValueMap) childMapIterator.next();
	    ValueMap childMap2 = (ValueMap) childMapIterator.next();
	    joinTwoChildren(childMap1,childMap2,_requiredDefinitions);
	    childrenMaps[0] = childMap1;
	    childrenMaps[1] = childMap1;
	} else {
	    // A switch.
	    throw new JHDLUnsupportedException("Switches not yet supported");
	    // merge switch targets
	}
	//_valueMap.joinChildren(childrenMaps);

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
    
    public IntervalChain getIntervalChain() { return _ic; }

    public String toBriefString() {
	return _ic.toShortString();
    }

    /**
     * This method will join one control-flow branch with a root
     * control-flow branch. This method must look at all values
     * that are assigned in both the child branch and root branch.
     * Assignment values must be multiplexed in either of the two
     * conditions:
     * 1. If the root and child both define the same value
     * 2. If the child defines a value that is required by a
     *    parent of the root (need the defined and not defined path)
     *
     **/
    public void joinOneChild(ValueMap childMap, Collection neededBySuccessors) {
	// Determine which branch is true

	// Iterate over all values that are defined in the child
	for (Iterator i=childMap.definitionValues().iterator();i.hasNext();) {
	    Value v = (Value) i.next();
	    if (neededBySuccessors.contains(v)) {
		Node childn = childMap.getValueNode(v);
		Node rootn = _valueMap.getOrAddValueNode(v);
		//_multiplexNodes();
	    }
	}
    }

    public void joinTwoChildren(ValueMap child1, ValueMap child2,
				Collection neededBySuccessors) {
    }
    
    protected Node _multiplexNodes(ValueMap child1Map,
				   Node child1,
				   Node child2) {

	/*

 	// Get the edges associated with the original CFG.
 	Node cNode = getConditionNode();

	Node trueNode = child1;
 	Node falseNode = child2;
	if (!isTrueNode(child1DFG)) {
	    trueNode = child2;
	    falseNode = child1;
	}
	
	Value value = (Value) child1.getWeight();
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
    


    /** 
     * Return the Node associated with the Condition of the IfStmt.
     **/ 
    public Node getConditionNode() {
	IfStmt ifs = getIfStmt();
	Value v = ifs.getCondition();
	return _valueMap.getValueNode(v);
    }

    /**
     * This method will return the IfStmt associated with the
     * last Unit in the Block associated with this graph.
     * If the last Unit is not an IfStmt, this method will return a 
     * null.
     **/
    public IfStmt getIfStmt() {
	Node root = _ic.getRoot();
	Block b = (Block) root.getWeight();
	Unit u = b.getTail();
	if (u instanceof IfStmt)
	    return (IfStmt) u;
	else
	    return null;
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

    /**
     * This UniqueVector contains a list of all Values that are
     * used by this block and succeeding blocks in the dataflow graph.
     * This Collection is used during the merging process in order to
     * decide which defined Values must be merged with with some
     * earlier Value. If a Value is defined but is not required,
     * then it does not need to be merged. 
     *
     * The algorithm for computing requiredDefinitions is a bottom-up
     * search of Values that are defined.
     **/
    protected UniqueVector _requiredDefinitions;

}
