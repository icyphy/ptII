/* A transformer that removes unnecessary fields from classes.

 Copyright (c) 2001 The Regents of the University of California.
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

import ptolemy.data.expr.Variable;
import ptolemy.graph.*;
import ptolemy.copernicus.kernel.*;
import ptolemy.copernicus.java.*;



/**

*/
public class CircuitAnalysis {
    /** Construct a new analysis
     */
    public CircuitAnalysis(SootClass theClass) {
        HashMutableDirectedGraph graph = new HashMutableDirectedGraph() {
            public String toString() {
                String string = "nodes = " + getNodes();
                for(Iterator nodes = getNodes().iterator();
                    nodes.hasNext();) {
                        Object source = nodes.next();
                        string += "\nsource = " + source + " targets = ";
                        for(Iterator succs = getSuccsOf(source).iterator();
                            succs.hasNext();) {
                                string += succs.next() + ",";
                            }
                    }
                return string;
            }
        };
        
        if(theClass.declaresMethodByName("prefire")) {
            _analyze(graph, theClass.getMethodByName("prefire"));
        }
        if(theClass.declaresMethodByName("fire")) {
            _analyze(graph, theClass.getMethodByName("fire"));
        }
        if(theClass.declaresMethodByName("postfire")) {
            _analyze(graph, theClass.getMethodByName("postfire"));
        }
        
        System.out.println("graph = " + graph);
        _graph = graph;
    }

    public HashMutableDirectedGraph getOperatorGraph() {
        return _graph;
    }

    protected void _analyze(HashMutableDirectedGraph graph, 
            SootMethod method) {
        Body body = method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);
        
        for(Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Stmt stmt = (Stmt)units.next();
            if(stmt instanceof AssignStmt) {
                Object leftOp = ((AssignStmt)stmt).getLeftOp();
                if(graph.containsNode(leftOp)) {
                    // Insert a delay.
                    Object delayNode = new String("delay");
                    graph.addNode(delayNode);
                    graph.addEdge(delayNode, leftOp);
                    leftOp = delayNode;
                } else {
                    graph.addNode(leftOp);
                }
                                
                Value rightOp = ((AssignStmt)stmt).getRightOp();
                if(rightOp instanceof FieldRef) {
                    SootField field = ((FieldRef)rightOp).getField();
                    if(!graph.containsNode(field)) {
                        graph.addNode(field);
                    }
                    graph.addEdge(field, leftOp);
                } else if(rightOp instanceof Local) {
                    if(!graph.containsNode(rightOp)) {
                        graph.addNode(rightOp);
                    }
                    graph.addEdge(rightOp, leftOp);
                } else if(rightOp instanceof InvokeExpr) {
                    InvokeExpr invokeExpr = (InvokeExpr)rightOp;
                    SootMethod invokedMethod = invokeExpr.getMethod();
                    String opName = invokedMethod.getName();
                    if(rightOp instanceof VirtualInvokeExpr) {
                        Value base = ((VirtualInvokeExpr)invokeExpr).getBase();
                        if(invokedMethod.getName().equals("get")) {
                            Port port = InlinePortTransformer.getPortValue(
                                    method, (Local)base, stmt, localDefs, 
                                    localUses);
                            String portName = port.getName();
                            if(!graph.containsNode(portName)) {
                                graph.addNode(portName);
                            }
                            graph.addEdge(portName, leftOp);
                            continue;
                        } else {
                            if(!graph.containsNode(opName)) {
                                graph.addNode(opName);
                            }
                            graph.addEdge(base, opName);
                        }
                    }
                    for(Iterator arguments = 
                            ((InvokeExpr)rightOp).getArgs().iterator();
                        arguments.hasNext();) {
                        Value argument = (Value)arguments.next();
                        if(!graph.containsNode(argument)) {
                            graph.addNode(argument);
                        }
                        graph.addEdge(argument, opName);
                    }
                    graph.addEdge(opName, leftOp);
                }
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
                        String portName = port.getName();
                        if(!graph.containsNode(portName)) {
                            graph.addNode(portName);
                        }
                        Value tokenValue = invokeExpr.getArg(1);
                        if(!graph.containsNode(tokenValue)) {
                            graph.addNode(tokenValue);
                        }
                        graph.addEdge(tokenValue, portName);
                    }
                }
            }
        }
    }
    private HashMutableDirectedGraph _graph;
}














