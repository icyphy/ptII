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
import java.util.HashMap;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

import soot.Body;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.AssignStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.SootMethod;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

//////////////////////////////////////////////////////////////////////////
//// DirectedAcyclicCFG
/**
 * This class will take a Soot block and create a DirectedGraph.
 *

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class DirectedAcyclicCFG extends DirectedGraph {

    DirectedAcyclicCFG(SootMethod method) throws IllegalActionException {
	this(method.retrieveActiveBody());
    }

    DirectedAcyclicCFG(Body body) throws IllegalActionException {
	this(new BriefBlockGraph(body));
    }

    DirectedAcyclicCFG(BriefBlockGraph bbg) throws IllegalActionException {
	super();
	_createGraph(bbg);
    }

    public Node source() {
	return _source;
    }

    public Node sink() {
	return _sink;
    }

    public List topologicalSort() {
	return (List) _sortedNodes;
    }

    /**
     * create the topology of the graph.
     **/
    protected void _createGraph(BriefBlockGraph bbg) 
	throws IllegalActionException {	

	// Save Body
	_bbgraph = bbg;

	// create copy of graph
	List blockList=_bbgraph.getBlocks();

	// Add one Node for each Block in the graph. The
	// weight of the Node is the corresponding Block.
	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    addNodeWeight(block);
	}

	// Copy edges. Iterate through each Node in the graph and
	// copy edges to its successors.
	//
	// In the process of adding edges, search for the roots and
	// successors.
	Vector sources = new Vector(nodeCount());
	Vector sinks = new Vector(nodeCount());
	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    Node nb = node(block);

	    List succs = block.getSuccs();
	    if (succs.size() == 0)
		sinks.add(nb);
	    List preds = block.getPreds();
	    if (preds.size() == 0)
		sources.add(nb);
	    //Get successors to this block and add an edge to graph for 
	    //each one.
	    for (Iterator successors=succs.iterator(); successors.hasNext();){
		Block succ=(Block)successors.next();
		addEdge(nb,node(succ));
	    }
	}

	// Identify source
	if (sources.size() == 0)
	    throw new IllegalActionException("There is no source Node");
	if (sources.size() > 1)
	    throw new IllegalActionException("There are more than one source nodes");
	_source = (Node) sources.get(0);

	// Identify sink
	if (sinks.size() == 0)
	    throw new IllegalActionException("There are no sinks");
	if (sinks.size() == 1) {
	    _sink = (Node) sinks.get(0);
	} else {
	    _sink = addNodeWeight("sink");
	    for (Iterator i=sinks.iterator();i.hasNext();) {
		Node n = (Node) i.next();
		addEdge(n,_sink);
	    }
	}

	// Obtain a topological sort of the graph 
	_sortedNodes = attemptTopologicalSort(nodes());
	
    }

    public void update() throws IllegalActionException {
	_sortedNodes = attemptTopologicalSort(nodes());
    }

    public String nodeString(Node n) {
	if (n.hasWeight()) {
	    Object o = n.weight();
	    if (o instanceof Block) {
		return "B" + ((Block) o).getIndexInMethod();
	    } else if (o instanceof String) {
		return (String) o;
	    }
	    return o.getClass().getName() + " " +o.toString();
	}
	return "";
    }

    public static soot.SootMethod getSootMethod(String args[]) {
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

	return testClass.getMethodByName(methodname);
    }

    public static DirectedAcyclicCFG _main(String args[]) {
	soot.SootMethod testMethod = getSootMethod(args);
	DirectedAcyclicCFG _cfg=null;
	try {
	    ConditionalControlCompactor.compact(testMethod);
	    soot.Body body = testMethod.retrieveActiveBody();
	    BriefBlockGraph bbgraph = new BriefBlockGraph(body);
	    ptolemy.copernicus.jhdl.util.BlockGraphToDotty.writeDotFile("bbgraph",bbgraph);
	    _cfg = new DirectedAcyclicCFG(bbgraph);

  	    ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile(testMethod.getName(),_cfg);
	} catch (IllegalActionException e) {
	    System.err.println(e);
	    System.exit(1);
	}
	return _cfg;
    }

    public static void main(String args[]) {
	_main(args);
    }

    protected BriefBlockGraph _bbgraph;
    protected Collection _sortedNodes;
    protected Node _source;
    protected Node _sink;

}
