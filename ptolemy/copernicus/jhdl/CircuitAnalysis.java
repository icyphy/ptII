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
import soot.toolkits.graph.*;
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



/**

*/
public class CircuitAnalysis {
    /** Construct a new analysis
     */
    public CircuitAnalysis(Entity entity, SootClass theClass) {
        HashMutableDirectedGraph graph = new HashMutableDirectedGraph() {

            public String toString() {

                String string = "nodes = " + getNodes();
                for (Iterator nodes = getNodes().iterator();
                    nodes.hasNext();) {
                        Object source = nodes.next();
                        string += "\nsource = " + source + " targets = ";
                        for (Iterator succs = getSuccsOf(source).iterator();
                            succs.hasNext();) {
                                string += succs.next() + ",";
                            }
                    }
                return string;
            }
        };
        _graph = graph;

        System.out.println("className = " + entity.getClass().getName());
        // Handle SampleDelay specially.
        if (entity.getClass().getName().equals(
                "ptolemy.domains.sdf.lib.SampleDelay")) {
            Port input = entity.getPort("input");
            Port output = entity.getPort("output");
            //CircuitNode delay = new CircuitNode(entity);
	    //String delay="delay"+count++;
            graph.addNode(input);
            graph.addNode(output);
            graph.addNode(entity);
            graph.addEdge(input, entity);
            graph.addEdge(entity, output);
            return;
        } else if (entity.getClass().getName().equals(
                "ptolemy.domains.sdf.lib.FIR")) {
            Port input = entity.getPort("input");
            Port output = entity.getPort("output");
	    //CircuitNode fir = new CircuitNode(entity);
	    //String fir="FIR"+count++;
            graph.addNode(input);
            graph.addNode(output);
            graph.addNode(entity);
            graph.addEdge(input, entity);
            graph.addEdge(entity, output);
            return;
	}

        // Analyze the bodies of the appropriate methods for things that
        // are not sample delays.
        Set requiredNodeSet = new HashSet();

        if (theClass.declaresMethodByName("prefire")) {
            _analyze(graph, requiredNodeSet,
                    theClass.getMethodByName("prefire"));
        }
        if (theClass.declaresMethodByName("fire")) {
            _analyze(graph, requiredNodeSet,
                    theClass.getMethodByName("fire"));
        }
        if (theClass.declaresMethodByName("postfire")) {
            _analyze(graph, requiredNodeSet,
                    theClass.getMethodByName("postfire"));
        }


        // get rid of non-essential nodes of
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Iterator nodes = graph.getNodes().iterator();
                nodes.hasNext();) {
                Object node = nodes.next();
                if (requiredNodeSet.contains(node)) {
                    continue;
                }
                HashSet set = new HashSet(graph.getSuccsOf(node));
                set.retainAll(requiredNodeSet);
                if (set.isEmpty()) {
                    continue;
                }
                requiredNodeSet.add(node);
                changed = true;
            }
        }

        //System.out.println("Original graph:\r\n" + graph + "\r\n");
	System.out.println(HashMutableToDotty.convert(graph,
						      entity.getName()));

        // Go though and eliminate unnecessary nodes.  These are nodes
        // that are not the names of output ports and have no targets,
        // or locals
        Set removeSet = new HashSet();

	// find removable nodes and add new edges between removed
	// nodes predecessors and successors
        for (Iterator nodes = graph.getNodes().iterator();
            nodes.hasNext();) {
            Object node = nodes.next();
            if (node instanceof Local || node instanceof SootField ||
                    !requiredNodeSet.contains(node)) {
                // Then remove the node.
                for (Iterator preds = graph.getPredsOf(node).iterator();
                    preds.hasNext();) {
                    Object pred = preds.next();
                    for (Iterator succs = graph.getSuccsOf(node).iterator();
                        succs.hasNext();) {
                        Object succ = succs.next();
                        graph.addEdge(pred, succ);
                    }
                }
                removeSet.add(node);
            }
        }

        // Remove all the edges & nodes
        for (Iterator nodes = removeSet.iterator();
            nodes.hasNext();) {
            Object node = nodes.next();
            List predList = new LinkedList(graph.getPredsOf(node));
            for (Iterator preds = predList.iterator();
                preds.hasNext();) {
                Object pred = preds.next();
                graph.removeEdge(pred, node);
            }
            List succList = new LinkedList(graph.getSuccsOf(node));
            for (Iterator succs = succList.iterator();
                succs.hasNext();) {
                Object succ = succs.next();
                graph.removeEdge(node, succ);
            }
            graph.removeNode(node);
        }

        System.out.println("Filtered graph:\r\n" + graph + "\r\n");
    }

    public HashMutableDirectedGraph getOperatorGraph() {
        return _graph;
    }

    protected void _analyze(HashMutableDirectedGraph graph,
            Set requiredNodeSet, SootMethod method) {
        Body body = method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

        for (Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Stmt stmt = (Stmt)units.next();
            if (stmt instanceof AssignStmt) {
                Object leftOp = ((AssignStmt)stmt).getLeftOp();

                if (leftOp instanceof FieldRef) {
                    SootField field = ((FieldRef)leftOp).getField();
                    // Then treat as a local.
                    if (!graph.containsNode(field)) {
                        graph.addNode(field);
                    }
                    leftOp = field;
                }

                if (graph.containsNode(leftOp)) {
                    // Insert a delay.
		    //Object delayNode = new String("delay" + count++);
		    Object delayNode = new RegisterDelay();
                    graph.addNode(delayNode);
                    graph.addEdge(delayNode, leftOp);
                    leftOp = delayNode;
                } else {
                    graph.addNode(leftOp);
                }

                Value rightOp = ((AssignStmt)stmt).getRightOp();
                if (rightOp instanceof FieldRef) {
                    SootField field = ((FieldRef)rightOp).getField();
                    ValueTag tag = (ValueTag)field.getTag("_CGValue");
                    if (tag == null || !(tag.getObject() instanceof Token)) {
			//This represents some state that is being read
                        // Then treat as a local.
                        if (!graph.containsNode(field)) {
                            graph.addNode(field);
                        }
                        graph.addEdge(field, leftOp);
                    } else {
			//This is a token that has been initialized to some
			//value
                        // Get the constant value of the token.
//                          String valueString =
//                              ((Token)tag.getObject()).toString();
//                          requiredNodeSet.add(valueString);
//                          if (!graph.containsNode(valueString)) {
//                              graph.addNode(valueString);
//                          }
//                          graph.addEdge(valueString, leftOp);
			Token valueToken=(Token)tag.getObject();
                        requiredNodeSet.add(valueToken);
                        if (!graph.containsNode(valueToken)) {
                            graph.addNode(valueToken);
                        }
//                          graph.addEdge(valueString, leftOp);
                        graph.addEdge(valueToken, leftOp);
                    }
                } else if (rightOp instanceof Local) {
                    if (!graph.containsNode(rightOp)) {
                        graph.addNode(rightOp);
                    }
                    graph.addEdge(rightOp, leftOp);
                } else if (rightOp instanceof InvokeExpr) {
                    InvokeExpr invokeExpr = (InvokeExpr)rightOp;
                    SootMethod invokedMethod = invokeExpr.getMethod();
                    String opName = invokedMethod.getName() + count++;
                    if (rightOp instanceof VirtualInvokeExpr) {
                        Value base = ((VirtualInvokeExpr)invokeExpr).getBase();
                        if (invokedMethod.getName().equals("get")) {
                            Port port = InlinePortTransformer.getPortValue(
                                    method, (Local)base, stmt, localDefs,
                                    localUses);
                            // String portName = port.getName();
                            if (!graph.containsNode(port)) {
  			      graph.addNode(port);
//    			      graph.addNode(port.getName());
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
//                              graph.addNode(opName);
//                              graph.addEdge(base, opName);
                            graph.addNode(invokedMethod);
                            graph.addEdge(base, invokedMethod);
                        }
                    }
                    for (Iterator arguments =
                            ((InvokeExpr)rightOp).getArgs().iterator();
                        arguments.hasNext();) {
                        Value argument = (Value)arguments.next();
                        if (!graph.containsNode(argument)) {
                            graph.addNode(argument);
                        }
                        graph.addEdge(argument, opName);
                    }
                    graph.addEdge(opName, leftOp);
                }
		// end of AssignStmt 'if'
            } else if (stmt instanceof InvokeStmt) {
                Object op = ((InvokeStmt)stmt).getInvokeExpr();
                if (op instanceof VirtualInvokeExpr) {
                    VirtualInvokeExpr invokeExpr =
                        (VirtualInvokeExpr)op;
                    SootMethod invokedMethod = invokeExpr.getMethod();
                    Value base = invokeExpr.getBase();
                    if (invokedMethod.getName().equals("send")) {
                        Port port = InlinePortTransformer.getPortValue(
                                method, (Local)base, stmt, localDefs,
                                localUses);
                        // String portName = port.getName();
                        if (!graph.containsNode(port)) {
                            graph.addNode(port);
                        }
                        requiredNodeSet.add(port);

                        Value tokenValue = invokeExpr.getArg(1);
                        if (!graph.containsNode(tokenValue)) {
                            graph.addNode(tokenValue);
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

    private HashMutableDirectedGraph _graph;
    private Set _requiredNodeSet;
    private int count = 0;

}
