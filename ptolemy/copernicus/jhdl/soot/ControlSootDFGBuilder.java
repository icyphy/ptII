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

package ptolemy.copernicus.jhdl.soot;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.copernicus.jhdl.*;

import soot.toolkits.graph.*;
import soot.*;
import soot.jimple.*;

import ptolemy.graph.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// 
/**
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class ControlSootDFGBuilder extends SootDFGBuilder {

    public ControlSootDFGBuilder(Block block, SootBlockDirectedGraph g) 
	throws SootASTException {
	super(block,g);
    }

    public Value processConditionExpr(ConditionExpr ce) 
	throws SootASTException {
	if (ce instanceof CompoundBooleanExpression)
	    return processCompoundBooleanExpression((CompoundBooleanExpression) ce);
	else	    
	    return super.processConditionExpr(ce);
    }    

    public Value processConditionExpr(ConditionExpr ce, Value op1, Value op2) {
	// connect Node associated with two ops 
	Node op1Node = _valueMap.getValueNode(op1);
	Node op2Node = _valueMap.getValueNode(op2);
	Node ceNode = _valueMap.getValueNode(ce);
	System.out.println(op1Node + " " + op2Node + " " +ceNode);
	_graph.addEdge(op1,ceNode);
	_graph.addEdge(op2,ceNode);
	return ce;
    }

    public Value processCompoundBooleanExpression(CompoundBooleanExpression ce) 
	throws SootASTException {

	Value op1 = ce.getOp1();
	Value op2 = ce.getOp2();
	Value cond1 = processConditionExpr((ConditionExpr) op1);
	Value cond2 = processConditionExpr((ConditionExpr) op2);
	return processCompoundBooleanExpression(ce,(ConditionExpr)cond1,
						(ConditionExpr) cond2);
    }

    public Value processCompoundBooleanExpression(CompoundBooleanExpression ce,
						  ConditionExpr cond1, 
						  ConditionExpr cond2) {
	return null;
    }

    public Value processUnopExpr(UnopExpr expr, Value op) {
	if (expr instanceof JHDLNotExpr)
	    return processJHDLNotExpr((JHDLNotExpr) expr, op);
	else
	    return super.processUnopExpr(expr, op);
    }

    public Value processJHDLNotExpr(JHDLNotExpr expr, Value op) {
	_graph.addEdge(_valueMap.getValueNode(op),
			  _valueMap.getValueNode(expr));
	return expr;
	//return null;
    }

    public static SootBlockDirectedGraph[] getGraphs(String args[]) {
	//SootASTVisitor.DEBUG = true;
	Block blocks[] = getBlocks(args);
	SootBlockDirectedGraph graphs[] = 
	    new SootBlockDirectedGraph[blocks.length];	
	for (int i = 0 ; i < blocks.length; i++) {
	    graphs[i] = new SootBlockDirectedGraph(blocks[i]);
	    try {
		ControlSootDFGBuilder s = 
		    new ControlSootDFGBuilder(blocks[i],
					      graphs[i]);
	    } catch (SootASTException e) {
		//System.err.println(e);
		e.printStackTrace();
		System.exit(1);
	    }
	} 
	return graphs;
    }

    public static void main(String args[]) {
	SootBlockDirectedGraph[] g=getGraphs(args);
	for (int i = 0;i<g.length;i++) {
	    PtDirectedGraphToDotty.writeDotFile("bgraph"+i,g[i]);
	    System.out.print("Sources=");
	    Iterator j=g[i].requiredDefinitions().iterator();
	    while (j.hasNext()) {
		System.out.print(j.next() + " ");
	    }
	    System.out.println();
	}
    }

}
