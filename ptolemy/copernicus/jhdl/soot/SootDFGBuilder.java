/* Dataflow representation of a Soot Block

 Copyright (c) 2001-2003 The Regents of the University of California.
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

import java.util.*;

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.graph.*;

import soot.toolkits.graph.Block;
import soot.*;
import soot.jimple.*;

//////////////////////////////////////////////////////////////////////////
//// SootDFGBuilder
/**
 * 
 * This class extends the SootASTVisitor to generate a directed graph
 * from a Soot Block object. Specifically, this class will generate a
 * SootBlockDirectedGraph (which extends
 * Ptolemy.graph.DirectedGraph). 
 *
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
@see SootBlockDirectedGraph

*/

public class SootDFGBuilder extends SootASTVisitor {

    public SootDFGBuilder(SootBlockDirectedGraph g)
            throws SootASTException {
        _graph = g;
        _valueMap = _graph.getValueMap();
        processBlock(_graph.getBlock());
    }

    public static SootBlockDirectedGraph createGraph(Block block)
            throws SootASTException {

        SootBlockDirectedGraph graph =
            new SootBlockDirectedGraph(block);
        new SootDFGBuilder(graph);
        return graph;
    }

    public Stmt processDefinitionStmt(DefinitionStmt stmt,
            Value rightOp, Value leftOp) {

        if (DEBUG) {
            System.out.println("Definition Statment "+stmt);
            System.out.println("\tRight Op="+rightOp);
            System.out.println("\tLeft Op="+leftOp);
        }

        Node rightNode = _valueMap.getValueNode(rightOp);
        Node leftNode = _valueMap.getValueNode(leftOp);

        // Add edge
        _graph.addEdge(rightNode,leftNode);

        return stmt;
    }

    public Value processValue(Value val, boolean left)
            throws SootASTException {

        if (DEBUG) System.out.println("SootDFGBuilder:Value="+val);

        if (!left)
            _valueMap.getOrAddValueNode(val); // make sure it is added
        else
            _valueMap.addValueNode(val);

        Value v = super.processValue(val,left);
        return v;
    }

    public Value processUnopExpr(UnopExpr expr, Value op) {
        Node opNode = _valueMap.getValueNode(op);
        Node exprNode = _valueMap.getValueNode(expr);
        _graph.addEdge(opNode,exprNode);
        return expr;
    }

    public Value processBinopExpr(BinopExpr expr, Value op1, Value op2) {
        Node op1Node = _valueMap.getValueNode(op1);
        Node op2Node = _valueMap.getValueNode(op2);
        Node exprNode = _valueMap.getValueNode(expr);
        _graph.addEdge(op1Node,exprNode,"op1");
        _graph.addEdge(op2Node,exprNode,"op2");
        return expr;
    }

    public Stmt processReturnVoidStmt(ReturnVoidStmt stmt) {
        return stmt;
    }
    public Stmt processReturnStmt(ReturnStmt stmt, Value returnVal) {
        Node returnedValue = _valueMap.getValueNode(returnVal);
        // TODO: I need to mark the given Value with a "return" flag
        Node returnNode = _graph.addNodeWeight(stmt);
        _graph.addEdge(returnedValue,returnNode);
        return stmt;
    }
    public Stmt processInvokeStmt(InvokeStmt stmt, InvokeExpr ie) {
        return stmt;
    }
    public Stmt processIfStmt(IfStmt stmt, ConditionExpr condition) {
        return stmt;
    }
    public Stmt processGotoStmt(GotoStmt stmt) { return stmt; }
    public Stmt processTableSwitchStmt(TableSwitchStmt stmt) { return stmt; }
    public Value processThisRef(ThisRef ifr) {return ifr;}
    public Value processParameterRef(ParameterRef ifr) {return ifr; }
    public Value processConstant(Constant c) { return c; }

    public Value processLocal(Local l, boolean left) {
        return l;
    }

    public Value processCastExpr(CastExpr val, Value op) {
        Node castNode = _valueMap.getValueNode(val);
        Node opNode = _valueMap.getValueNode(op);
        _graph.addEdge(opNode,castNode);
        return val;
    }

    public Value processInstanceFieldRef(InstanceFieldRef ifr, Value base,
            boolean left) {

        // Node that represents field-ref Base
        Node baseNode = _valueMap.getValueNode(base);
        // Node that represents ifr.
        Node ifrNode = _valueMap.getValueNode(ifr);

        // Determine whether a base edge has been created
        Edge baseEdge=null;
        for (Iterator i=_graph.inputEdges(ifrNode).iterator();i.hasNext();) {
            Edge e = (Edge) i.next();
            if (e.hasWeight() && e.getWeight().equals(BASE_WEIGHT))
                baseEdge = e;
        }
        if (baseEdge == null)
            _graph.addEdge(baseNode,ifrNode,BASE_WEIGHT);

        return ifr;
    }

    // Note: processVirtualInvokeExpr & processSpecialInvokeExpr use the
    // same code. Modify for code reuse.
    public Value processVirtualInvokeExpr(VirtualInvokeExpr ie,
            Value args[], Value base) {
        Node invokeNode = _valueMap.getValueNode(ie);
        Node baseNode = _valueMap.getValueNode(base);
        for (int i = 0; i < args.length; i++) {
            Node argNode = _valueMap.getValueNode(args[i]);
            //System.out.println("arg="+argNode+" invokeNode="+invokeNode);
            _graph.addEdge(argNode,invokeNode,"arg"+i);
        }
        _graph.addEdge(baseNode,invokeNode);
        return ie;
    }
    public Value processSpecialInvokeExpr(SpecialInvokeExpr ie,
            Value args[], Value base) {
        if (ie.getMethod().getName().equals("<init>"))
            return processConstructorInvokeExpr(ie,args,base);
        return null;
        /*
          System.out.println("SpecialInvoke="+ie+" method="+ie.getMethod().getName());
          Node invokeNode = _valueMap.getValueNode(ie);
          Node baseNode = _valueMap.getValueNode(base);
          for (int i = 0; i < args.length; i++) {
          Node argNode = _valueMap.getValueNode(args[i]);
          //System.out.println("arg="+argNode+" invokeNode="+invokeNode);
          _graph.addEdge(argNode,invokeNode,"arg"+i);
          }
          _graph.addEdge(baseNode,invokeNode);
          return ie;
        */
    }
    public Value processConstructorInvokeExpr(SpecialInvokeExpr ie,
            Value args[], Value base) {
        System.out.println("ConstructorInvoke="+ie+" method="+ie.getMethod().getName());
        Node invokeNode = _valueMap.getValueNode(ie);
        Node baseNode = _valueMap.getValueNode(base);
        for (int i = 0; i < args.length; i++) {
            Node argNode = _valueMap.getValueNode(args[i]);
            //System.out.println("arg="+argNode+" invokeNode="+invokeNode);
            _graph.addEdge(argNode,invokeNode,"arg"+i);
        }

        _graph.addEdge(invokeNode, baseNode, "constructor");
        return ie;

    }

    public Value processNewExpr(NewExpr ifr) {
        return ifr;
    }

    /**
     * This static method will create an array of SootBlockDirectedGraph
     * objects from a Class file specified by the String array arguments.
     *
     * @param args Specifies the Classname (args[0]) and the
     * Methodname (args[1]).
     *
     **/
    public static SootBlockDirectedGraph[] createDataFlowGraphs(String args[],
            boolean writeGraphs) {

        //SootASTVisitor.DEBUG = true;
        Block blocks[] =
            ptolemy.copernicus.jhdl.test.Test.getMethodBlocks(args);
        SootBlockDirectedGraph graphs[] =
            new SootBlockDirectedGraph[blocks.length];
        PtDirectedGraphToDotty dgToDotty =
            new PtDirectedGraphToDotty();
        for (int i = 0 ; i < blocks.length; i++) {
            try {
                graphs[i] = createGraph(blocks[i]);
                dgToDotty.writeDotFile(".", "bbgraph"+i, graphs[i]);
            } catch (SootASTException e) {
                //System.err.println(e);
                e.printStackTrace();
                System.exit(1);
            }
        }
        return graphs;
    }

    public static void main(String args[]) {
        SootASTVisitor.DEBUG = true;
        Block blocks[] =
            ptolemy.copernicus.jhdl.test.Test.getMethodBlocks(args);
        SootBlockDirectedGraph graphs[] =
            new SootBlockDirectedGraph[blocks.length];
        PtDirectedGraphToDotty dgToDotty =
            new PtDirectedGraphToDotty();
        for (int i = 0 ; i < blocks.length; i++) {
            try {
                graphs[i] = createGraph(blocks[i]);
                dgToDotty.writeDotFile(".", "bgraph" + i, graphs[i]);
            } catch (SootASTException e) {
                //System.err.println(e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * This String is used as the weight object associated with
     * edges corresponding to base references for referenced objects.
     **/
    public static final String BASE_WEIGHT = "base";

    public static boolean DEBUG = false;

    protected SootBlockDirectedGraph _graph;

    protected ValueMap _valueMap;
}

