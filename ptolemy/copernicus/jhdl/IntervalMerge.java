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

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;

import soot.Value;

import soot.toolkits.graph.Block;

import ptolemy.copernicus.jhdl.util.JHDLUnsupportedException;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;

import ptolemy.graph.Node;
import ptolemy.graph.Edge;

/**
 * The graph that is manipuated is the graph associated with
 * the root.
 **/
public class IntervalMerge {

    public IntervalMerge(IntervalChain ic) {
	_ic = ic;
	Block icB = (Block) _ic.getRoot().weight();
	try {
	    _dfg = new BlockDataFlowGraph(icB);
	} catch (JHDLUnsupportedException e) {
	    System.err.println(e);
	}

	// 1. Create graph for child if there is one
	// 2. Create graph for self
	// 3. Merge serial child with self

	// 1. Create graph for child if there is one.
	IntervalChain nextChain = _ic.getNext();
	if (nextChain != null)
	    _next = new IntervalMerge(nextChain);
	
	// 2. Create graph for self
	//   - single node: do nothing (graph is root graph)
	//   - clean merge node:
	//     - switch:
	//     - if stmt:
	//   - special node
	if (_ic.isSingleNode())
	    return;

	if (_ic.isSimpleMerge()) {
	    Collection _rootOutputNodes = getOutputNodes();
	    // For each child:
	    // 1. Create new IntervalMerge
	    // 2. Connect input nodes of child to outputNodes of root
	    // Wrap up by inserting the appropriate MUX
	    HashMap children = _ic.getChildren();
	    HashMap childrenValues = new HashMap(children.size());
	    for (Iterator i = children.values().iterator();i.hasNext();) {
		IntervalChain child = (IntervalChain) i.next();		
		IntervalMerge childIM = new IntervalMerge(child);
		HashMap childValues = mergeSerial(childIM);
		childrenValues.put(childIM,childValues);
	    }
	    
	    if (children.values().size() == 1) {
		// Only one branch. Merge the root graph with
		// the branch graph.
		System.exit(1);
	    } else if (children.values().size() == 2) {
		// Two branches. Merge all of their outputs.		
		System.exit(1);
	    } else {
		// A switch.
		System.exit(1);
	    }
	}
	if (_ic.isSpecialMerge()) {
	    System.exit(1);
	}	

    }
    public Collection getOutputNodes() {
	return _dfg.getOutputNodes();
    }
    public Collection getInputNodes() {
	return _dfg.getInputNodes();
    }
    public BlockDataFlowGraph getDFG() {
	return _dfg;
    }
    public IntervalChain getIntervalChain() { return _ic; }

    protected HashMap mergeSerial(IntervalMerge im) {
	BlockDataFlowGraph imdfg = im.getDFG();

	// a hashmap between old nodes & new. Used when connecting edges
	HashMap newNodes = new HashMap(); 

	// key = Value object in top-level graph, 
	// Value = new Value object in lower graph.
	HashMap newDefinitions = new HashMap();

	// Iterate through all nodes and add to graph
	for (Iterator i = imdfg.nodes().iterator(); i.hasNext();) {

	    Node node = (Node) i.next();	    
	    Node new_node = null;
	    Object weight = null;
	    if (node.hasWeight())
		weight = node.weight();

	    Collection inEdges = imdfg.inputEdges(node);
	    if (inEdges.size() == 0) {
		// This is a reference to a previous definition -
		// try to find a corresponding
		// weight of Node in main graph. If there is not one
		// found, add a new node.
		if (!_dfg.containsNodeWeight(weight)) {
		    new_node = _dfg.addNode(new Node(weight));
		    newNodes.put(node,new_node);
		} else {
		    newNodes.put(node,_dfg.node(weight));
		}
	    } else {
		// a new definition
		if (!_dfg.containsNodeWeight(weight)) {
		    // a new definition that does not overlap
		    new_node = _dfg.addNode(new Node(weight));
		    newNodes.put(node,new_node);
		} else {
		    // new definition overlaps. Need to create a new
		    // definition of this weight.
		    Object new_weight = ((Value)weight).clone();
		    new_node = _dfg.addNode(new Node(weight));
		    newNodes.put(node,new_node);
		    newDefinitions.put(weight,new_weight);
		}
	    }
	}

	// Iterate through all edges and add to graph
	for (Iterator i = imdfg.edges().iterator(); i.hasNext();) {
	    Edge e = (Edge) i.next();
	    Node src = e.source();
	    Node snk = e.sink();
	    Node new_src = (Node)newNodes.get(src);
	    Node new_snk = (Node)newNodes.get(snk);
	    if (e.hasWeight())
		_dfg.addEdge(new_src,new_snk,e.weight());
	    else
		_dfg.addEdge(new_src,new_snk);
	}
	return newDefinitions;
    }

    public static IntervalMerge _main(String args[]) {
	IntervalChain ic = IntervalChain._main(args);
	System.out.println("IntervalChain=\n"+ic);
	return new IntervalMerge(ic);
    }

    public static void main(String args[]) {
	IntervalMerge im = _main(args);	
	BlockDataFlowGraph graphs[] = 
	    BlockDataFlowGraph.getBlockDataFlowGraphs(args);
	System.out.println(im);
	PtDirectedGraphToDotty.writeDotFile("merge",
					    im.getDFG());
    }

    protected IntervalChain _ic;
    protected BlockDataFlowGraph _dfg;
    protected IntervalMerge _next;
}
