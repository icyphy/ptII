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
import java.util.List;
import java.util.Map;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

import soot.Body;
import soot.SootMethod;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

//////////////////////////////////////////////////////////////////////////
//// BlockControlFlowGraph
/**
 * This class will take a Soot block and create a DirectedGraph.
 *

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class BlockControlFlowGraph extends DirectedGraph {

    BlockControlFlowGraph(SootMethod method) {
	this(method.retrieveActiveBody());
    }

    BlockControlFlowGraph(Body body) {
	super();	

	_bbgraph = new BriefBlockGraph(body);
	_createGraph();
    }

    public DirectedGraph createDataFlowGraph() {
	// 1. Create dataflow graph of each basic block
	// 2. Determine hierarchical super block boundaries
	// 3. Determine signal multiplexing
	// 4. Combine dataflow graphs

	// Create dataflow of each basic block
	List blockList = _bbgraph.getBlocks();
	for (int blockNum=0;blockNum<blockList.size();blockNum++) {
	    Block block=(Block)blockList.get(blockNum);
	    BlockDataFlowGraph dataFlowGraph=null;
	    try {
		dataFlowGraph = new BlockDataFlowGraph(block);
	    } catch(IllegalActionException e) {
		System.err.println(e);
	    }
	}
	return null;
    }

    /**
     * create the topology of the graph.
     **/
    protected void _createGraph() {
	List blockList=_bbgraph.getBlocks();

	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    addNodeWeight(block);
	}

	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    Node nb = node(block);
	    //Get successors to this block and add an edge to graph for each one
	    for (Iterator succs=block.getSuccs().iterator(); succs.hasNext();){
		Block succ=(Block)succs.next();
		addEdge(nb,node(succ));
	    }
	}
    }

    /*
    public static ArrayList conditionLeafs(Block source, ArrayList list) {
	// 1. If list==null, Make sure source ends with an IF
	//    Initialize list to null
	//    Visit successors
	// 2. If list!=null
	//       If visited, exit
	//       If is only an if statement
	//          mark node as visited
	//          Visit successors
	//       If is not only an if statement
	//          add self to list and return
    }
    */

    public void controlFlowAnalysis() throws IllegalActionException {
	// perform a topological sort on the graph
	Object sortedNodes[] = attemptTopologicalSort(nodes()).toArray();
	// For each node in graph (topological order)
	// - combineLabels
	// - for each successor
	//   - propagateLabelsto
    }


    public static void main(String args[]) {
	String classname = ptolemy.copernicus.jhdl.test.Test.TEST1;
	String methodname = "method1";
	if (args.length > 0)
	    classname = args[0];
	if (args.length > 1)
	    methodname = args[1];
	
	soot.SootClass testClass = 
	    ptolemy.copernicus.jhdl.test.Test.getApplicationClass(classname);
	if (testClass == null) {
	    System.err.println("Class "+classname+" not found");
	    System.exit(1);
	}
	System.out.println("Loading class "+classname+" method "+methodname);
	if (!testClass.declaresMethodByName(methodname)) {
	    System.err.println("Method "+methodname+" not found");
	    System.exit(1);
	}
	soot.SootMethod testMethod = testClass.getMethodByName(methodname);
	soot.Body body = testMethod.retrieveActiveBody();
	
	BriefBlockGraph bbgraph = new BriefBlockGraph(body);
	ptolemy.copernicus.jhdl.util.BlockGraphToDotty.writeDotFile("bbgraph",bbgraph);
	BlockControlFlowGraph bcfg = new BlockControlFlowGraph(body);
    }

    protected BriefBlockGraph _bbgraph;

}
