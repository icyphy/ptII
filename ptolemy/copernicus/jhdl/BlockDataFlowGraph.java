/* A transformer that removes unnecessary fields from classes.

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

import soot.SootMethod;
import soot.SootField;
import soot.Value;
import soot.Local;

import soot.jimple.AssignStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.UnopExpr;
import soot.jimple.NegExpr;
import soot.jimple.BinopExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.IfStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;

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
import soot.toolkits.scalar.*;
//import soot.dava.*;
//import soot.util.*;

import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.BaseType;

import ptolemy.data.expr.Variable;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.graph.Edge;
import ptolemy.copernicus.kernel.*;
import ptolemy.copernicus.java.*;
import ptolemy.copernicus.jhdl.util.SuperBlock;
import ptolemy.copernicus.jhdl.util.GraphNode;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.copernicus.jhdl.util.SynthesisToDotty;
import ptolemy.copernicus.jhdl.util.BlockGraphToDotty;

//////////////////////////////////////////////////////////////////////////
//// BlockDataFlowGraph
/**

@author Mike Wirthlin and Matthew Koecher
@version $Id$
@since Ptolemy II 2.0
*/

public class BlockDataFlowGraph extends DirectedGraph {

    BlockDataFlowGraph(Block block) {
	super();
	_block = block;	
	_requiredNodeSet = new HashSet();
	
	//Maps values to Nodes. Use this map to grab the most recent
	//Node to add edges to.
	Map nodeMap=new HashMap();
	
	// Iterate over all units within block
	for(Iterator units = _block.iterator(); units.hasNext();) {
	    // Process all units in this graph
	    Stmt stmt = (Stmt)units.next();
	    if(stmt instanceof AssignStmt) {
		_processAssignStmt(stmt, nodeMap);
	    } else if(stmt instanceof InvokeStmt) {
		_processInvokeStmt(stmt, nodeMap);
	    } else if(stmt instanceof ReturnStmt) {
		// return statements do not affect dataflow
	    } else if(stmt instanceof ReturnVoidStmt) {
		// return statements do not affect dataflow
	    } else if(stmt instanceof IfStmt) {
		// if statements shoudl be last statement in basic block.
		// No data flow is added at this point - control flow
		// analysis may look at this statement at a later time.
	    } else if(stmt instanceof GotoStmt) {
		// Goto statements shoudl be last statement in basic block.
		// No data flow is added at this point - control flow
		// analysis may look at this statement at a later time.
	    } else if(stmt instanceof IdentityStmt) {
		// Ignore for now. Not quite sure why yet.
	    } else {
		System.out.println("Unsupported statement="+
				   stmt.getClass().getName());
	    }
	}
	
    }

    public HashSet getRequiredNodeSet() {
	return _requiredNodeSet;
    }

    protected void _processAssignStmt(Stmt stmt, Map nodeMap) {
	/////////////////////////////////
	// Assignment Statement
	/////////////////////////////////
	    
      
	//
	// Step 1: add node corresponding to leftOp (value being
	//         assigned).
	Object leftOp = ((AssignStmt)stmt).getLeftOp();
		
	// if the Left op is a FieldRef, get the actual
	// field and add the field to the Graph
	if(leftOp instanceof FieldRef) {
	    SootField field = ((FieldRef)leftOp).getField();
	    leftOp = field;
	}
	// Add left op node to graph (it is a new Value)
	Node leftOpNode=addNodeWeight(leftOp);
                                
	//
	// Step 2: add data flow for generating value to be assigned.
	//
	Value rightOp = ((AssignStmt)stmt).getRightOp();

	/////////////////////////////////
	// FieldRef Right Operator 
	/////////////////////////////////
	if(rightOp instanceof FieldRef) {
	    SootField field = ((FieldRef)rightOp).getField();
	    ValueTag tag = (ValueTag)field.getTag("_CGValue");
	    if(tag == null || !(tag.getObject() instanceof Token)) {
		//This represents some state that is being read
		// Then treat as a local.
		if(!containsNodeWeight(field)) {
		    nodeMap.put(field, 
				addNodeWeight(field));
		}
		addEdge((Node)nodeMap.get(field),
			leftOpNode);
	    } else {
		// This is a token that has been initialized to some
		// value get the constant value of the token.
		Token valueToken=(Token)tag.getObject();
		//requiredNodeSet.add(valueToken);  
		//Is this node really required?
		if(!containsNodeWeight(valueToken)) {
		    nodeMap.put(valueToken, 
				addNodeWeight(valueToken));
		}
		addEdge((Node)nodeMap.get(valueToken),
			leftOpNode);
	    }   
	} // end: if (rightOp instanceof FieldRef)
		
	/////////////////////////////////
	// Local Right Operator 
	/////////////////////////////////
	else  if(rightOp instanceof Local) {

	    if(!containsNodeWeight(rightOp)) {
		nodeMap.put(rightOp, addNodeWeight(rightOp));
	    }
	    addEdge((Node)nodeMap.get(rightOp),
		    leftOpNode);
	} 

	/////////////////////////////////
	// InvokeExpr Right Operator 
	/////////////////////////////////
	else if(rightOp instanceof InvokeExpr) {
	    InvokeExpr invokeExpr = (InvokeExpr)rightOp;
	    SootMethod invokedMethod = invokeExpr.getMethod();
	    boolean connectArguments=true;

	    if(rightOp instanceof InstanceInvokeExpr) {
		Value base = 
		    ((InstanceInvokeExpr)invokeExpr).getBase();
		/*			
					if(invokedMethod.getName().equals("get")) {
					//FIXME:  Make sure this is really a port.get() call, not just any get()
					Port port = 
					InlinePortTransformer.getPortValue(
					method, 
					(Local)base,
					stmt, 
					localDefs, 
					localUses);
					if(!containsNodeWeight(port)) {
					nodeMap.put(port, 
					addNodeWeight(port));
					}
					//requiredNodeSet.add(port); //Is this node really required?
					addEdge((Node)nodeMap.get(port),
					leftOpNode);

					connectArguments=false;//Don't create nodes for the arguments below
					} else {
		*/ // end of if "get"
		//This is for all methods that have not been
		//inlined yet (and aren't "get"s).  Must handle
		//these eventually
		nodeMap.put(invokedMethod,
			    addNodeWeight(invokedMethod));
		if (!containsNodeWeight(base))
		    nodeMap.put(base, 
				addNodeWeight(base));
		Node baseNode=(Node)nodeMap.get(base);

		//Fix situations such as r=r.toString();, so that r is in the localGraph
		//(added as the leftOp) but not in nodeMap yet.  So just use leftOp.
		if (baseNode == null) baseNode=leftOpNode;
			    
		addEdge(baseNode,
			(Node)nodeMap.get(invokedMethod),
			"base");
		/*} else on if "get" */
	    } else {
		//StaticInvokeExpr
		nodeMap.put(invokedMethod, addNodeWeight(invokedMethod));
	    }

	    if (connectArguments){
		//This is skipped for a "get" method call

		int argCount=0;
		for(Iterator arguments = 
			((InvokeExpr)rightOp).getArgs().iterator();
		    arguments.hasNext();) {			    
		    Value argument = (Value)arguments.next();
		    if(!containsNodeWeight(argument)) {
			nodeMap.put(argument, addNodeWeight(argument));
		    }
		    addEdge((Node)nodeMap.get(argument),
			    (Node)nodeMap.get(invokedMethod),
			    new Integer(argCount++));
		}
		addEdge((Node)nodeMap.get(invokedMethod),
			leftOpNode);
	    }
		    
	} 

	/////////////////////////////////
	// BinopExpr Right Operator 
	/////////////////////////////////

	else if (rightOp instanceof BinopExpr){
	    Value op1=((BinopExpr)rightOp).getOp1();
	    Value op2=((BinopExpr)rightOp).getOp2();
		  
	    nodeMap.put(rightOp, addNodeWeight(rightOp));
		    
	    if (!containsNodeWeight(op1)){
		nodeMap.put(op1, addNodeWeight(op1));
	    }
	    if (!containsNodeWeight(op2)){
		nodeMap.put(op2, addNodeWeight(op2));
	    }

	    addEdge((Node)nodeMap.get(op1),
		    (Node)nodeMap.get(rightOp), "op1");
	    addEdge((Node)nodeMap.get(op2),
		    (Node)nodeMap.get(rightOp), "op2");
	    addEdge((Node)nodeMap.get(rightOp),
		    leftOpNode);
	} 		
	/////////////////////////////////
	// UnopExpr Right Operator 
	/////////////////////////////////
	else if (rightOp instanceof UnopExpr){
	    /////////////////////////////////
	    // NegExpr Right Operator 
	    /////////////////////////////////
	    if (rightOp instanceof NegExpr){
		Value op=((UnopExpr)rightOp).getOp();
		    
		nodeMap.put(rightOp, addNodeWeight(rightOp));
		if (!containsNodeWeight(op)){
		    nodeMap.put(op, addNodeWeight(op));
		}
		addEdge((Node)nodeMap.get(op),
			(Node)nodeMap.get(rightOp));
		addEdge((Node)nodeMap.get(rightOp),
			leftOpNode);
	    }
	} else {
	    //If its nothing above, just add a node for it with
	    //an edge to leftOp.  Constants will be caught here.
	    if (!containsNodeWeight(rightOp)){
		nodeMap.put(rightOp, addNodeWeight(rightOp));
	    }
	    addEdge((Node)nodeMap.get(rightOp),
		    leftOpNode);
	}

	// discuss this
	nodeMap.put(leftOp, leftOpNode);
    }

    protected void _processInvokeStmt(Stmt stmt, Map nodeMap) {

	Value op = ((InvokeStmt)stmt).getInvokeExpr();

	if(op instanceof VirtualInvokeExpr) {
	    VirtualInvokeExpr invokeExpr = (VirtualInvokeExpr)op;
	    SootMethod invokedMethod = invokeExpr.getMethod();
	    Value base = invokeExpr.getBase();
	    if(invokedMethod.getName().equals("send")) {
		/* TODO
		   Port port = 
		   InlinePortTransformer.getPortValue(
		   method,
		   (Local)base,
		   stmt,
		   localDefs, 
		   localUses);
		   // String portName = port.getName();
		   if(!containsNodeWeight(port)) {
		   nodeMap.put(port, addNodeWeight(port));
		   }
		   _requiredNodeSet.add(port);
		   
		   Value tokenValue = invokeExpr.getArg(1);
		   if(!containsNodeWeight(tokenValue)) {
		   nodeMap.put(tokenValue, addNodeWeight(tokenValue));
		   }
		   addEdge((Node)nodeMap.get(tokenValue),
		   (Node)nodeMap.get(port));
		*/
	    }
	} else {
	    System.out.println("Unsupported Invoke Expression="+
			       op.getClass().getName());
	}
    }

    /** The Soot block used to create this graph **/
    protected Block _block;

    protected HashSet _requiredNodeSet;
}
