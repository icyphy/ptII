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

import soot.*;
import soot.jimple.*;
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
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
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
import ptolemy.graph.*;
import ptolemy.copernicus.kernel.*;
import ptolemy.copernicus.java.*;

//////////////////////////////////////////////////////////////////////////
//// CircuitAnalysis
/**

@author Steve Neuendorffer and Ben Warlick
@version $Id$
@since Ptolemy II 2.0
*/
public class CircuitAnalysis {
    /** Construct a new analysis
     */
    public CircuitAnalysis(Entity entity, SootClass theClass) {
	DirectedGraph graph = new DirectedGraph();
        _graph = graph;
       
        System.out.println("className = " + entity.getClass().getName());

	//Handle cases that have been predefined and don't need analyzing
	//  	if (_pd.isDefined(entity)){
	//  	  try {
	//  	    _pd.convertEntityToGraph(entity, graph);
	//  	  } catch (IllegalActionException e){
	//  	    System.out.println("Error in CircuitAnalysis: "+e);
	//  	  }
	//  	  return;
	//  	}
	
        // Analyze the bodies of the appropriate methods for things that
        // are not sample delays.
        Set requiredNodeSet = new HashSet();

	//  	try {
	//  	  byucc.util.flowgraph.JavaControlFlowGraph jcfg =
	//  	    new byucc.util.flowgraph.JavaControlFlowGraph(theClass.toString(),"fire");

	//  	  Iterator it = jcfg.getAllNodes().iterator();
	//  	  while (it.hasNext()) {
	//  	    ((byucc.util.newgraph.Node) it.next()).setDotLabelFromToString();
	//  	  }

	//  	  jcfg.writeDotFile(theClass.toString()+".dot");
	//  	  jcfg.writeDataFlowDotFile(theClass.toString()+"_df.dot");
	//  	}
	//  	catch (IOException e){
	//  	  System.out.println(e);
	//  	}
	
        if(theClass.declaresMethodByName("prefire")) {
            _analyze(graph, requiredNodeSet,
		     theClass.getMethodByName("prefire"));
        }
        if(theClass.declaresMethodByName("fire")) {
            _analyze(graph, requiredNodeSet,
		     theClass.getMethodByName("fire"));
        }
        if(theClass.declaresMethodByName("postfire")) {
            _analyze(graph, requiredNodeSet,
		     theClass.getMethodByName("postfire"));
        }


        // get rid of non-essential nodes of 
        boolean changed = true;
        while(changed) {
            changed = false;
            for(Iterator nodes = graph.nodes().iterator();
                nodes.hasNext();) {
                Node node = (Node)nodes.next();
                if(requiredNodeSet.contains(node)) {
                    continue;
                }
                HashSet set = new HashSet(graph.successors(node));
                set.retainAll(requiredNodeSet);
                if(set.isEmpty()) {
                    continue;
                }
                requiredNodeSet.add(node);
                changed = true;
            }
        }

        //System.out.println("Original graph:\r\n" + graph + "\r\n");
	//  	System.out.println(DirectedGraphToDotty.convert(graph,
	//  							entity.getName()));

        // Go though and eliminate unnecessary nodes.  These are nodes
        // that are not the names of output ports and have no targets,
        // or locals
        Set removeSet = new HashSet();

	// find removable nodes and add new edges between removed
	// nodes predecessors and successors
        for(Iterator nodes = graph.nodes().iterator();
            nodes.hasNext();) {
            Node node = (Node)nodes.next();
            if(node.weight() instanceof Local ||
	       node.weight() instanceof SootField ||
	       !requiredNodeSet.contains(node)) {
                // Then remove the node.
                for(Iterator preds = graph.predecessors(node).iterator();
                    preds.hasNext();) {
                    Node pred = (Node)preds.next();
                    for(Iterator succs = graph.successors(node).iterator();
                        succs.hasNext();) {
                        Node succ = (Node)succs.next();
                        graph.addEdge(pred, succ);
                    }
                }
                removeSet.add(node);
            }
        }
       
        // Remove all the edges & nodes
        for(Iterator nodes = removeSet.iterator();
            nodes.hasNext();) {
            Node node = (Node)nodes.next();
            List predList = new LinkedList(graph.predecessors(node));
            for(Iterator preds = predList.iterator();
                preds.hasNext();) {
                Node pred = (Node)preds.next();
                graph.removeEdge((Edge)graph.successorEdges(pred, node).toArray()[0]);
            }
            List succList = new LinkedList(graph.successors(node));
            for(Iterator succs = succList.iterator();
                succs.hasNext();) {
                Node succ = (Node)succs.next();
                graph.removeEdge((Edge)graph.successorEdges(node, succ).toArray()[0]);
            }
            graph.removeNode(node);
        }

        //System.out.println("Filtered graph:\r\n" + graph + "\r\n");
    }

    public DirectedGraph getOperatorGraph() {
        return _graph;
    }

    public Inliner getInliner(){
	return new Inliner() {
		protected boolean shouldInline(SootMethod sootMethod){
		    String name=sootMethod.getName();

		    return false;
	  
		    //  	  //Don't inline get() or send() methods on ports.  They are
		    //  	  //handled separately
		    //  	  //if (name.equals("get") || name.equals("send")){
		    //  	    //FIXME - this code should check to make sure this
		    //  	    //method is declared by a Port class
		    //  	    if (isClass("ptolemy.kernel.Port", sootMethod.getDeclaringClass())){
		    //  	      return false;
		    //  	    }
		    //  	    //}
		    //  	  //Don't inline Token's methods, as they are mostly just
		    //  	  //arithmetic methods, such as add(), multiply(), etc.
		    //  	  if (isClass("ptolemy.data.Token", sootMethod.getDeclaringClass())){
		    //  	    return false;
		    //  	  }
		    //  	  return true;
		}

		protected boolean isClass(String name, SootClass sc){

		    if (name.equals(sc.getName()))
			return true;
	  
		    while (sc.hasSuperclass()){
			sc=sc.getSuperclass();
			if (name.equals(sc.getName()))
			    return true;
		    }
	  
		    return false;
		}
	    };
    }
  
    protected void _analyze(DirectedGraph graph, 
			    Set requiredNodeSet, SootMethod method) {
        Body body = method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);


	BriefBlockGraph test=new BriefBlockGraph(body);
	//System.out.println(DirectedGraphToDotty.convert(test, "test"));
	
	//Inline methods
	Inliner inliner = getInliner();
	inliner.inline(body);
	
        for(Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Stmt stmt = (Stmt)units.next();
            if(stmt instanceof AssignStmt) {
                Object leftOp = ((AssignStmt)stmt).getLeftOp();

                if(leftOp instanceof FieldRef) {
                    SootField field = ((FieldRef)leftOp).getField();
                    // Then treat as a local.
                    if(!graph.containsNodeWeight(field)) {
                        graph.addNodeWeight(field);
                    }
                    leftOp = field;
                }

                if(graph.containsNodeWeight(leftOp)) {
                    // Insert a delay.
		    //Object delayNode = new String("delay" + count++);
		    Object delayNode = new RegisterDelay();
                    graph.addNodeWeight(delayNode);
                    graph.addEdge(delayNode, leftOp);
                    leftOp = delayNode;
                } else {
                    graph.addNodeWeight(leftOp);
                }
                                
                Value rightOp = ((AssignStmt)stmt).getRightOp();
                if(rightOp instanceof FieldRef) {
                    SootField field = ((FieldRef)rightOp).getField();
                    ValueTag tag = (ValueTag)field.getTag("_CGValue");
                    if(tag == null || !(tag.getObject() instanceof Token)) {
			//This represents some state that is being read
                        // Then treat as a local.
                        if(!graph.containsNodeWeight(field)) {
                            graph.addNodeWeight(field);
                        }
                        graph.addEdge(field, leftOp);
                    } else {
			//This is a token that has been initialized to some
			//value
                        // Get the constant value of the token.
			//                          String valueString = 
			//                              ((Token)tag.getObject()).toString();
			//                          requiredNodeSet.add(valueString);
			//                          if(!graph.containsNodeWeight(valueString)) {
			//                              graph.addNodeWeight(valueString);
			//                          }
			//                          graph.addEdge(valueString, leftOp);
			Token valueToken=(Token)tag.getObject();
                        requiredNodeSet.add(valueToken);
                        if(!graph.containsNodeWeight(valueToken)) {
                            graph.addNodeWeight(valueToken);
                        }
			//                          graph.addEdge(valueString, leftOp);
                        graph.addEdge(valueToken, leftOp);
                    }   
                } else if(rightOp instanceof Local) {
                    if(!graph.containsNodeWeight(rightOp)) {
                        graph.addNodeWeight(rightOp);
                    }
                    graph.addEdge(rightOp, leftOp);
                } else if(rightOp instanceof InvokeExpr) {
                    InvokeExpr invokeExpr = (InvokeExpr)rightOp;
                    SootMethod invokedMethod = invokeExpr.getMethod();
                    //String opName = invokedMethod.getName() + count++;
                    if(rightOp instanceof VirtualInvokeExpr) {
                        Value base = ((VirtualInvokeExpr)invokeExpr).getBase();
                        if(invokedMethod.getName().equals("get")) {
                            Port port = InlinePortTransformer.getPortValue(
									   method, (Local)base, stmt, localDefs, 
									   localUses);
                            // String portName = port.getName();
                            if(!graph.containsNodeWeight(port)) {
				graph.addNodeWeight(port);
				//    			      graph.addNodeWeight(port.getName());
                            }
			    requiredNodeSet.add(port);
			    graph.addEdge(port, leftOp);
			    //  			    requiredNodeSet.add(port.getName());
			    //  			    graph.addEdge(port.getName(), leftOp);
                            continue;
                        } else {
			    //This is for all methods that have not been
			    //inlined yet (and aren't "get"s).  Must handle
			    //these eventually
			    //                              graph.addNodeWeight(opName);
			    //                              graph.addEdge(base, opName);
                            graph.addNodeWeight(invokedMethod);
                            graph.addEdge(base, invokedMethod);
                        }
                    }
                    for(Iterator arguments = 
                            ((InvokeExpr)rightOp).getArgs().iterator();
                        arguments.hasNext();) {
                        Value argument = (Value)arguments.next();
                        if(!graph.containsNodeWeight(argument)) {
                            graph.addNodeWeight(argument);
                        }
                        graph.addEdge(argument, invokedMethod);
                    }
                    graph.addEdge(invokedMethod, leftOp);
                } else if (rightOp instanceof BinopExpr){
		    Value op1=((BinopExpr)rightOp).getOp1();
		    Value op2=((BinopExpr)rightOp).getOp2();
		    //  		  Marker op1Marker=new Marker("op1");
		    //  		  Marker op2Marker=new Marker("op2");
		  
		    graph.addNodeWeight(rightOp);
		    //  		  graph.addNodeWeight(op1Marker);
		    //  		  graph.addNodeWeight(op2Marker);
		    if (!graph.containsNodeWeight(op1)){
			graph.addNodeWeight(op1);
		    }
		    if (!graph.containsNodeWeight(op2)){
			graph.addNodeWeight(op2);
		    }

		    //  		  graph.addEdge(op1, op1Marker);
		    //  		  graph.addEdge(op2, op2Marker);
		    //  		  graph.addEdge(op1Marker, rightOp);
		    //  		  graph.addEdge(op2Marker, rightOp);
		    graph.addEdge(op1, rightOp, "op1");
		    graph.addEdge(op2, rightOp, "op2");
		    graph.addEdge(rightOp, leftOp);
		} else if (rightOp instanceof UnopExpr){
		    if (rightOp instanceof NegExpr){
			Value op=((UnopExpr)rightOp).getOp();
		    
			graph.addNodeWeight(rightOp);
			if (!graph.containsNodeWeight(op)){
			    graph.addNodeWeight(op);
			}
			graph.addEdge(op, rightOp);
			graph.addEdge(rightOp, leftOp);
		    }
		}
		// end of AssignStmt 'if'
            } else if(stmt instanceof InvokeStmt) {
                Object op = ((InvokeStmt)stmt).getInvokeExpr();
                if(op instanceof VirtualInvokeExpr) {
                    VirtualInvokeExpr invokeExpr = 
                        (VirtualInvokeExpr)op;
                    SootMethod invokedMethod = invokeExpr.getMethod();
                    Value base = invokeExpr.getBase();
                    if(invokedMethod.getName().equals("send")) {
                        Port port = InlinePortTransformer.getPortValue(
								       method, (Local)base, stmt, localDefs, 
								       localUses);
                        // String portName = port.getName();
                        if(!graph.containsNodeWeight(port)) {
                            graph.addNodeWeight(port);
                        }
                        requiredNodeSet.add(port);
                           
                        Value tokenValue = invokeExpr.getArg(1);
                        if(!graph.containsNodeWeight(tokenValue)) {
                            graph.addNodeWeight(tokenValue);
                        }
                        graph.addEdge(tokenValue, port);
                    }
                }
            }
        }
    }

    protected ptolemy.data.type.Type _getPortType(Port port) 
	throws RuntimeException {
	ptolemy.data.type.Type t=null;
	try {
	    TypedIOPort tport=(TypedIOPort)port;
	    t=tport.getType();
	} catch (ClassCastException e){
	    throw new RuntimeException("Must have ports that are TypedIOPorts");
	}
	if (t.equals(BaseType.FIX) || t.equals(BaseType.INT) ||
	    t.equals(BaseType.LONG) || t.equals(BaseType.BYTE) ||
	    t.equals(BaseType.BOOLEAN) ) {
	    return t;
	} else {
	    throw new RuntimeException("Unsupported port type "+t+" in port "+port);
	}
    }

    private DirectedGraph _graph;
    private Set _requiredNodeSet;
    private int count = 0;
    private static Predefined _predefined=new Predefined();
}
