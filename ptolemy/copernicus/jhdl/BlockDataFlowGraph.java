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

import soot.SootField;
import soot.Value;
import soot.Local;

import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.LookupSwitchStmt;

import soot.jimple.VirtualInvokeExpr;
import soot.jimple.UnopExpr;
import soot.jimple.NegExpr;
import soot.jimple.BinopExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.IfStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;

import soot.jimple.toolkits.invoke.MethodCallGraph;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
import soot.jimple.toolkits.invoke.VTATypeGraph;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;

import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.CompleteUnitGraph;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.graph.Edge;

import ptolemy.copernicus.jhdl.util.SuperBlock;
import ptolemy.copernicus.jhdl.util.GraphNode;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.copernicus.jhdl.util.SynthesisToDotty;
import ptolemy.copernicus.jhdl.util.BlockGraphToDotty;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// BlockDataFlowGraph
/**
 * This class will take a Soot block and create a ptolemy 
 * DirectedGraph.
 *

@author Mike Wirthlin and Matthew Koecher
@version $Id$
@since Ptolemy II 2.0
*/

public class BlockDataFlowGraph extends DirectedGraph {

    /**
     * Constructor iterates through the statements found within the
     * Block to create a dataflow graph of the block behavior.
     **/
    BlockDataFlowGraph(Block block) throws IllegalActionException {

	super();
	_block = block;	
	_requiredNodeSet = new HashSet();
	_nodeMap = new HashMap();

	
	// Iterate over all units within block
	for(Iterator units = _block.iterator(); units.hasNext();) {

	    // Process all Stmt units in this graph
	    Stmt stmt = (Stmt)units.next();
	    if (DEBUG) System.out.println("Statement "+stmt.getClass().getName()+
					  "="+stmt);
	    if(stmt instanceof AssignStmt) {
		_processAssignStmt((AssignStmt) stmt);
	    } else if(stmt instanceof IdentityStmt) {
		_processIdentityStmt((IdentityStmt) stmt);
	    } else if(stmt instanceof InvokeStmt) {
		_processInvokeStmt((InvokeStmt) stmt);
	    } else if(stmt instanceof ReturnStmt) {
		// return statements do not affect dataflow
	    } else if(stmt instanceof ReturnVoidStmt) {
		// return statements do not affect dataflow
	    } else if(stmt instanceof IfStmt) {
		// if statements shoudl be last statement in basic block.
		// No data flow is added at this point - control flow
		// analysis may look at this statement at a later time.
	    } else if (stmt instanceof TableSwitchStmt) {
		// No data flow is added at this point - control flow
		// analysis may look at this statement at a later time.
	    } else if (stmt instanceof LookupSwitchStmt) {
		// No data flow is added at this point - control flow
		// analysis may look at this statement at a later time.
	    } else if(stmt instanceof GotoStmt) {
		// Goto statements shoudl be last statement in basic block.
		// No data flow is added at this point - control flow
		// analysis may look at this statement at a later time.
	    } else {
		throw new IllegalActionException("Unsupported statement="+
						 stmt.getClass().getName());
	    }
	}
    }

    public HashSet getRequiredNodeSet() {
	return _requiredNodeSet;
    }


    public Node[] getInputNodes() {
	ArrayList nodes = new ArrayList();
	// iterate over all nodes in the graph
	for (Iterator i = nodes().iterator(); i.hasNext();) {
	    Node node = (Node) i.next();
	    // if node has no input edges, it is considered an input Node
	    if (inputEdgeCount(node) == 0)
		nodes.add(node);
	}
	return (Node[]) nodes.toArray();
    }

    public Node[] getOutputNodes() {
	ArrayList nodes = new ArrayList();
	// iterate over all nodes in the graph
	for (Iterator i = nodes().iterator(); i.hasNext();) {
	    Node node = (Node) i.next();
	    // if node has no input edges, it is considered an input Node
	    if (outputEdgeCount(node) == 0)
		nodes.add(node);
	}
	return (Node[]) nodes.toArray();
    }

    /** Combine input dataflow graph *after* current dataflow
     * graph. Connect appropriate edges (i.e. variables)
     **/
    public void combineSerial(BlockDataFlowGraph block) {
    }

    /**
     * This method will evaluate AssignStmt statements and add
     * the corresponding dataflow to the graph.
     **/
    protected void _processAssignStmt(AssignStmt stmt) 
	throws IllegalActionException {
	
	// Create Node for LeftOp
	Value leftOp = stmt.getLeftOp();
	Node leftOpNode = null;
	if (leftOp instanceof Local) {
	    leftOpNode = _addNodeValueWeight(leftOp);
	} else if(leftOp instanceof InstanceFieldRef) {
	    leftOpNode = _createInstanceFieldRefNode((InstanceFieldRef)leftOp);
	} else {
	    throw new IllegalActionException("Unsupported Left AssignOp="+
					     leftOp.getClass().getName());
	}
                                
	// Create Node for RightOp
	Value rightOp = stmt.getRightOp();
	Node rightOpNode = null;
	if (DEBUG) System.out.println("Expression "+rightOp.getClass().getName()+
				      "="+rightOp);
	if (rightOp instanceof UnopExpr){
	    rightOpNode = _processUnopExpr( (UnopExpr) rightOp);
	} else if (rightOp instanceof BinopExpr){
	    rightOpNode = _processBinopExpr( (BinopExpr) rightOp);
	} else  if(rightOp instanceof Local) {
	    rightOpNode = _processLocal((Local)rightOp);
	} else if(rightOp instanceof InstanceFieldRef) {
	    rightOpNode = _processInstanceFieldRef((InstanceFieldRef) rightOp);
	} else if(rightOp instanceof InstanceInvokeExpr) {
	    rightOpNode = _processInstanceInvokeExpr((InstanceInvokeExpr) rightOp);
	} else if(rightOp instanceof Constant){
	    rightOpNode = _processConstant((Constant) rightOp);
	} else {
	    throw new IllegalActionException("Unsupported Assign Statement right op="+
					     rightOp.getClass().getName());
	}
	addEdge(rightOpNode,leftOpNode);
    }

    /**
     * This method will process an IdentityStmt. 
     **/
    protected void _processIdentityStmt(IdentityStmt stmt) {
	Value leftOp = stmt.getLeftOp();	
	Value rightOp = stmt.getRightOp();	
	if (DEBUG) System.out.println("IdentityStmt left="+
				      leftOp.getClass().getName()+
				      " right="+rightOp.getClass().getName());
	Node leftOpNode= _addNodeValueWeight(leftOp);
	Node rightOpNode = _getNodeFromValue(rightOp);
	addEdge(rightOpNode, leftOpNode);
    }


    protected void _processInvokeStmt(InvokeStmt stmt) 
	throws IllegalActionException {

	Value op = stmt.getInvokeExpr();
	if(op instanceof InstanceInvokeExpr) {
	    _createInstanceInvokeExprNode( (InstanceInvokeExpr) op);
	} else {
	    throw new IllegalActionException("Unsupported Invoke Expression="+
					     op.getClass().getName());
	}
    }

    /**
     * Process a UnopExpr and create the corresponding dataflow graph.
     * Return a Node in the dataflow graph that represents the result.
     **/
    protected Node _processUnopExpr(UnopExpr expr) 
	throws IllegalActionException {
	if (expr instanceof NegExpr){
	    Node n = _addNodeValueWeight(expr);
	    Value rightValue=expr.getOp();
	    Node rv = _getNodeFromValue(rightValue);
	    addEdge(rv,n);
	    return n;
	} else {
	    throw new IllegalActionException("Unsupported Unary Operator="+
					     expr.getClass().getName());
	}	
    }

    /**
     * Process a binary operation. 
     **/
    protected Node _processBinopExpr(BinopExpr expr) {
	Value rightValue1=expr.getOp1();
	Value rightValue2=expr.getOp2();
	Node n = _addNodeValueWeight(expr);		    
	Node r1n = _getNodeFromValue(rightValue1);
	Node r2n = _getNodeFromValue(rightValue2);
	addEdge(r1n,n,"op1");
	addEdge(r2n,n,"op2");
	return n;
    }

    protected Node _processLocal(Local l) {
	return _getNodeFromValue(l);
    }

    protected Node _processInstanceFieldRef(InstanceFieldRef ifr) {
	SootField field = ifr.getField();
	Value base = ifr.getBase();
	// See if a FieldRef with same base exists in _nodeMap	    
	// - search all previous InstanceFieldRefs and check for
	//   same base and field
	InstanceFieldRef previous=null;
	for(Iterator it = _nodeMap.keySet().iterator();it.hasNext();) {
	    Object node = it.next();		
	    if (node instanceof InstanceFieldRef) {
		InstanceFieldRef ifr_n = (InstanceFieldRef) node;
		if (ifr_n.getBase().equals(base) &&
		    ifr_n.getField().equals(field)) {
		    previous = ifr_n;
		}
	    }
	}
	if (previous==null) {
	    return _createInstanceFieldRefNode(ifr);
	}  else {
	    return (Node)_nodeMap.get(previous);
	}
    }


    protected Node _processInstanceInvokeExpr(InstanceInvokeExpr expr) {
	return _createInstanceInvokeExprNode(expr);
    }
    
    protected Node _processConstant(Constant c) {
	return _getNodeFromValue(c);
    }

    protected Node _createInstanceFieldRefNode(InstanceFieldRef ifr) {
	// create node for new field reference
	Node n = _addNodeValueWeight(ifr);
	// get node for base
	Node b = _getNodeFromValue(ifr.getBase());
	// add edge between base and ifr
	addEdge(b,n,"base");
	return n;
    }

    protected Node _createInstanceInvokeExprNode(InstanceInvokeExpr iie) {
	// Add node 
	Node n = _addNodeValueWeight(iie);
	Node b = _getNodeFromValue(iie.getBase());
	// Add link to base 
	addEdge(b,n,"base");

	// add argument links
	int argCount=0;
	for(Iterator arguments = iie.getArgs().iterator();arguments.hasNext();) {
	    Value argument = (Value)arguments.next();
	    Node a_n = _getNodeFromValue(argument);
	    addEdge(a_n,n,new Integer(argCount++));
	}
	return n;
    }

    /**
     * This method adds a "Value" weighted Node to the graph and
     * adds a Map between the value and the new Node.
     **/
    protected Node _addNodeValueWeight(Value v) {
	Node n = addNodeWeight(v);
	_nodeMap.put(v,n);
	return n;
    }

    /**
     * This method will search the graph to see if the given Value
     * exists as a weight of a Node in the graph.  If it is, the
     * corresponding Node is returned. If not, a new Node is created
     * in the graph with the given Value as the Node weight. Further,
     * the _nodeMap is updated to Map the Value to the Node.
     **/
    protected Node _getNodeFromValue(Value value) {
	Node n=null;
	if (!containsNodeWeight(value)) {
	    n = _addNodeValueWeight(value);
	    return n;
	} else {
	    return (Node)_nodeMap.get(value);
	}
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
	CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
	
	BriefBlockGraph bbgraph = new BriefBlockGraph(body);
	BlockGraphToDotty.writeDotFile("bbgraph",bbgraph);
	List blockList=bbgraph.getBlocks();
	for (int blockNum=0; blockNum < blockList.size(); blockNum++){
	    Block block=(Block)blockList.get(blockNum);
	    BlockDataFlowGraph dataFlowGraph=null;
	    try {
		dataFlowGraph=new BlockDataFlowGraph(block);
	    } catch (IllegalActionException e) {
		System.err.println(e);
	    }
	    PtDirectedGraphToDotty.writeDotFile("bbgraph"+blockNum,
						dataFlowGraph);
	}
    }
	
    /** The Soot block used to create this graph **/
    protected Block _block;

    /** 
     * Maps Soot "Values" (leftOp and rightOps) to Nodes. The key for
     * this Map is a "Soot Value" and the value of the Map is a Node.
     * Each unique "Soot Value" corresponds to one Node in the graph. 
     *
     * This mapping allows you to get a Node from a Value. Note that
     * the reverse mapping is built into the graph - Nodes in the
     * graph contain a "Value" for their weight.
     **/
    protected Map _nodeMap;

    protected HashSet _requiredNodeSet;

    public static boolean DEBUG = false; 

}
