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

    /** Copy graph and provide a sink node if necessary **/
    public static DirectedGraph copyGraph(BriefBlockGraph bbg) {

	List blockList=bbg.getBlocks();
	Vector terminatingNodes = new Vector();
	DirectedGraph dg = new DirectedGraph(blockList.size());

	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    dg.addNodeWeight(block);
	}

	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    Node nb = dg.node(block);
	    //Get successors to this block and add an edge to graph for each one
	    List successors = block.getSuccs();
	    if (successors.size() == 0) 
		terminatingNodes.add(nb);
	    else {
		for (Iterator succs=successors.iterator(); succs.hasNext();){
		    Block succ=(Block)succs.next();
		    dg.addEdge(nb,dg.node(succ));
		}
	    }
	}

	/*
	// Add sink node if necessary
	if (terminatingNodes.size() > 1) {
	    Body body = Jimple.v().newBody();
	    Unit u = Jimple.v().newReturnVoidStmt();
	    body.getUnits().add(u);
	    Block b = new Block(u,u,body,blockList.size(),1,bbg);
	    //	    Node terminatingNode = dg.addNodeWeight("sink");
	    Node terminatingNode = dg.addNodeWeight(b);
	    for (int i=0;i<terminatingNodes.size();i++) {
		Node tn = (Node) terminatingNodes.get(i);
		dg.addEdge(tn,terminatingNode);
	    }
	}
	*/

	return dg;
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

    public static void mergeBlocks(BriefBlockGraph g, Block mblock)
	throws IllegalActionException {
	List successors = g.getSuccsOf(mblock);
	
    }

    public static DirectedGraph annotateGraph(BriefBlockGraph g) 
    throws IllegalActionException {
	DirectedGraph dg = new DirectedGraph();

	// add sink node
	Node sinkNode = dg.addNodeWeight("Sink");

	// copy nodes
	List blockList = g.getBlocks();
	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    dg.addNodeWeight(block);
	}

	// create edges. Iterate over blocks in topological order.
	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    Node nb = dg.node(block);

	    // Look at all incoming edges of this node and create root ConditionLabel
	    Collection inEdges = dg.inputEdges(nb);
	    ConditionLabel rootConditionLabel=null;
	    if (inEdges.size() == 0) {
		// no predecessors. Root label is "true"
		rootConditionLabel = new ConditionLabel();
	    } else if (inEdges.size() == 1) {
		// one predecessor. Root label is label on incoming edge
		Edge p = (Edge) inEdges.iterator().next();
		rootConditionLabel = (ConditionLabel) p.weight();
	    } else {
		// more than one predecessor. Root ConditionLabel is the "OR" of
		// two incoming edges.
		for (Iterator i=inEdges.iterator();i.hasNext();) {
		    Edge e = (Edge) i.next();
		    ConditionLabel eConditionLabel = (ConditionLabel) e.weight();
		    if (rootConditionLabel == null)
			rootConditionLabel = eConditionLabel;
		    else
			rootConditionLabel = new ConditionLabel(eConditionLabel,rootConditionLabel,ConditionLabel.OR);
		}
	    }

	    // Create new edge for each CFG edge
	    List successors = g.getSuccsOf(block);
	    if (successors.size() == 0) {
		// Add an edge to sink node
		dg.addEdge(nb,sinkNode,rootConditionLabel);
	    } else if (successors.size() == 1) {
		// One successor
		Block successorBlock = (Block) successors.get(0);
		Node successorNode = dg.node(successorBlock);
		dg.addEdge(nb,successorNode,rootConditionLabel);
	    } else {
		Unit tail = block.getTail();
		// More than one successor
		if (tail instanceof IfStmt) {
		    Block trueBlock=null;
		    Block falseBlock=null;
		    for (Iterator i=g.getSuccsOf(block).iterator();
			 i.hasNext();) {
			Block s = (Block) i.next();
			if (((IfStmt)tail).getTargetBox().getUnit() ==
			    s.getHead())
			    trueBlock = s;
			else
			    falseBlock = s;			
		    }
		    Node falseNode = dg.node(falseBlock);
		    Node trueNode = dg.node(trueBlock);
		    dg.addEdge(nb,falseNode,new ConditionLabel(new ConditionLabel(block,false),
						      rootConditionLabel,
						      ConditionLabel.AND));
		    dg.addEdge(nb,trueNode,new ConditionLabel(new ConditionLabel(block),
						     rootConditionLabel,
						     ConditionLabel.AND));
		} else {
		    throw new IllegalActionException("Unit  "+
						     tail.getClass().getName()+
						     " not supported");
		}
	    }
	}

	return dg;
    }


    public static String nodeString(Node n) {
	Object o = n.weight();
	if (o instanceof Block) {
	    return "B" + ((Block) o).getIndexInMethod();
	} else if (o instanceof String) {
	    return (String) o;
	}
	return o.getClass().getName() + " " +o.toString();
    }

    public static void addMuxNodes(DirectedGraph g, 
				   Collection sortedNodes,
				   DominatorHashMap dominators) {

	// iterate over all nodes in topological order
	for (Iterator i=sortedNodes.iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    // See if current node joins
	    Collection nPredecessors = g.predecessors(n);
	    if (nPredecessors.size() > 1) {
		// s is a join, process the mux
		do {
		    Vector e=getClosestNodes(nPredecessors,dominators);
		    System.out.print(nodeString(n)+":(");
		    for (Iterator j=e.iterator();j.hasNext();) {
			Node o=(Node) j.next();
			System.out.print(nodeString(o)+" ");
		    } 
		    System.out.println(")");
		    nPredecessors.removeAll(e);
//  		    System.exit(1);
		} while (nPredecessors.size() > 0);
	    }
	}	
    }

    public static Vector getClosestNodes(Collection predecessors,
					 DominatorHashMap dominators) {
	// Search list of predecessors that has a dominator that is
	// furthest from the root in the topological order
	Node deepest=null;
	for (Iterator i=predecessors.iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (deepest == null) {
		deepest = n;
	    }
	    else if (dominators.deeperDominator(n,deepest)) {
		deepest = n;
	    }
	}
	// Search the list of predecessors and find the predecessors
	// that are closest in depth to the deepest.
	Vector closest=new Vector(predecessors.size());
	for (Iterator i=predecessors.iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (closest.size() == 0)
		// No closest predecessors. Add this to closest vector.
		closest.add(n);
	    else {
		// all closest predecessors should have the same
		// "deepestDominator". If the current node has a deeper
		// dominator, clear out the array and add it
		Node c = (Node) closest.get(0);
		if (dominators.deeperDominator(c,n)) {
		    closest.clear();
		    closest.add(c);
		} else if (dominators.equalDeepestDominator(c,n))
		    closest.add(c);		
	    }
	}
	closest.add(deepest);
	return closest;
    }

    
    // 	Vector sortedNodes = new Vector(g.nodeCount());
    public static HashMap numberOfPaths(DirectedGraph g, 
					Collection sortedNodes) {

	HashMap map = new HashMap(g.nodeCount()); 
	Node root = (Node) sortedNodes.iterator().next();
	annotateNumberOfPaths(g,map,root,0);
	return map;
    } 

    public static void annotateNumberOfPaths(DirectedGraph g, 
					     HashMap map,
					     Node node,
					     int inNumPaths) {

	// annotate for current Node. path count = the path count coming
	// in + 1 - number of edges coming into current node. If more
	// than 1 edge is coming in, path cound will be reduced.
	Collection p = g.predecessors(node);
	int currentNumPaths = inNumPaths + 1 - p.size();
	map.put(node,new Integer(currentNumPaths));
	System.out.println("Node "+node+" has paths="+currentNumPaths);

	// recursively annoatate successors
	Collection s = g.successors(node);
	int outgoingNumPaths = currentNumPaths + s.size() - 1;
	for (Iterator i=s.iterator();i.hasNext();) {
	    Node successor = (Node) i.next();
	    annotateNumberOfPaths(g,map,successor,outgoingNumPaths);
	}
    }

    /*
    public static void addSinkNode(BriefBlockGraph g) {
 	// Now add a final node if there is more than one sink node
	Vector terminatingBlocks = new Vector();
 	for (Iterator i = g.iterator();i.hasNext();) {
 	    Block b = (Block) i.next();
 	    List l = g.getSuccsOf(b);
 	    if (l.size() == 0)
 		terminatingBlocks.add(b);	    
 	}
	// See if an additional Node needs to be created
 	if (terminatingBlocks.size() > 1) {
 	}
    }   
    */

    public static void controlFlowAnalysis(Body body) 
	throws IllegalActionException {

	BriefBlockGraph bbgraph = new BriefBlockGraph(body);
	List blockList = bbgraph.getBlocks();
	Block firstBlock = (Block) blockList.get(0);
	//mergeBlocks(firstBlock);

	// Recursive (starting with first node)
	// - if one successor, merge with successor and 
	//   call on new merged node
	// - if two successors, call on each tree (make sure they are one)
	//   when done, merge two nodes

	// perform a topological sort on the graph
	// For each node in graph (topological order)
	// - combineConditionLabels
	// - for each successor
	//   - propagateConditionLabelsto
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
	//	BlockControlFlowGraph bcfg = new BlockControlFlowGraph(body);

	try {
	    ConditionalControlCompactor.compact(testMethod);
	    soot.Body body = testMethod.retrieveActiveBody();
	
	    BriefBlockGraph bbgraph = new BriefBlockGraph(body);
	    ptolemy.copernicus.jhdl.util.BlockGraphToDotty.writeDotFile("bbgraph",bbgraph);

	    DirectedGraph dg = copyGraph(bbgraph);
  	    ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile(methodname,dg);
	    Collection sortedNodes = dg.attemptTopologicalSort(dg.nodes());
	    DominatorHashMap dominators = new DominatorHashMap(dg);
	    System.out.println(dominators);
	    Interval r = new Interval(dg);
	    System.out.println(r);
  	    ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile("interval",dg);
//	    addMuxNodes(dg,sortedNodes,dominators);
//	    HashMap map = numberOfPaths(dg,sortedNodes);	    
//  	    DirectedGraph dg = annotateGraph(bbgraph);
	    
	} catch (IllegalActionException e) {
	    System.err.println(e);
	    //System.exit(1);
	}
    }

    protected BriefBlockGraph _bbgraph;

}

class ConditionLabel {

    public ConditionLabel() {
	_true = true;
	_left = null;
	_right = null;
	_block = null;
	minimize();
    }
    
    public ConditionLabel(Block b) {
	this(b,true);
	minimize();
    }

    public ConditionLabel(Block b, boolean sense) {
	_true = false;
	_block = b;
	_blockTrue = sense;
	minimize();
    }

    public ConditionLabel(ConditionLabel left, ConditionLabel right, int op) {
	_block = null;
	_true = false;
	_left = left;
	_right = right;
	_op = op;
	minimize();
    }

    public void copy(ConditionLabel l) {
	if (l._true) {
	    _block = null;
	    _true = true;
	    _left = null;
	    _right = null;
	    _op = 0;
	}
	if (l._block != null) {
	    _block = l._block;
	    _blockTrue = l._blockTrue;
	    _left = null;
	    _right = null;
	    _true = false;
	    _op = 0;
	}
	if (l._op != 0) {
	    _block = null;
	    _true = false;
	    _left = l._left;
	    _right = l._right;
	    _op = l._op;
	}
    }

    public void minimize() {
	if (_op != 0) {
	    if (_left._true && _right._true) {
		copy(new ConditionLabel());
		return;
	    }
	    if (_left._true)
		copy(_right);
	    if (_right._true)
		copy(_left);	    
	}
    }

    public String toString() {
	if (_true)
	    return "1";
	else if (_block != null) {
	    if (_blockTrue)
		return new String("B"+_block.getIndexInMethod());
	    else
		return new String("~B"+_block.getIndexInMethod());
	} else {
	    if (_op == AND)
		return new String("("+_left.toString()+"&&"+
				  _right.toString()+")");
	    else
		return new String("("+_left.toString()+"||"+
				  _right.toString()+")");
	}
    }

    public static final int AND = 1;
    public static final int OR = 2;

    protected ConditionLabel _left;
    protected ConditionLabel _right;
    protected boolean _true;
    protected int _op;
    protected Block _block;
    protected boolean _blockTrue;

}

/*
class RootedDirectedGraph extends DirectedGraph {
    public RootedDirectedGraph() {
	super();
	_root = null;
    }
    public RootedDirectedGraph(int i) {
	super(i);
	_root = null;
    }
    public Node determineRoot() {
    }
    public Node getRoot() {
	return _root;
    }
    protected Node _root;
} 
*/
