/*

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

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.copernicus.jhdl.soot.*;
import ptolemy.copernicus.jhdl.*;

import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.ListIterator;
import java.util.LinkedList;

import ptolemy.copernicus.jhdl.util.BlockGraphToDotty;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.copernicus.jhdl.util.JHDLUnsupportedException;

import ptolemy.copernicus.jhdl.soot.CompoundBooleanExpression;
import ptolemy.copernicus.jhdl.soot.CompoundOrExpression;
import ptolemy.copernicus.jhdl.soot.CompoundAndExpression;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

import soot.jimple.IfStmt;
import soot.jimple.GotoStmt;
import soot.jimple.NeExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.IntConstant;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.AssignStmt;
import soot.jimple.internal.JAssignStmt;

import soot.SootMethod;
import soot.Unit;
import soot.Body;
import soot.Value;
import soot.PatchingChain;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.Block;


//////////////////////////////////////////////////////////////////////////
//// BooleanNotCompactor
/**
 *
 * A Not expression in Java (i.e. the ! operator) is implemented as
 * an IfStmt in which constant Boolean values are assigned to the
 * corresponding value. This method will scan through the chain
 * of units and convert such Not expressions into BooleanNotExpr
 * objects. This simplifies the control-flow analysis by removing
 * this control flow construct with a dataflow expression.
 *
 * @see ptolemy.copernicus.jhdl.soot.BooleanNotExpr
 *
 * @author Mike Wirthlin
 * @version $Id$
 * @since Ptolemy II 2.0
*/
public class BooleanNotCompactor {

    public static void compact(SootMethod method)
            throws IllegalActionException {
        Body mbody = method.retrieveActiveBody();
        PatchingChain chain = mbody.getUnits();

        for (Unit current = (Unit) chain.snapshotIterator().next();
             current != null;) {

            Unit u = mergeBooleanAssign(chain,current);
            if (u == null)
                current = (Unit) chain.getSuccOf(current);
            else
                current = (Unit) chain.getSuccOf(u);
        }
    }

    /**
     * Search for the following pattern:
     * - IfStmt Value
     * - Successor must be an assignment statement
     * - Target must be an assignment statement
     * - Assignment statements must be to the same value
     * - Values being assigned must be constant
     *   (false assignment statement constant must = 1)
     *   (true assignment statement constant must = 0)
     * - Assignment statements must converge to same statement
     **/
    protected static Unit mergeBooleanAssign(PatchingChain chain,
            Unit root) {

        // 0 Unit must be an IfStmt statment
        if (!(root instanceof IfStmt))
            return null;
        IfStmt rootIfStmt = (IfStmt) root;

        // 1. successor must be an assignment statment
        Unit successor = (Unit) chain.getSuccOf(rootIfStmt);
        if (!(successor instanceof AssignStmt))
            return null;

        // 2. target must be an assignment statment
        Unit target = rootIfStmt.getTarget();
        if (!(target instanceof AssignStmt))
            return null;

        //System.out.println("Dual Assignment Targets");

        // 3. Assignment statements must be to the same value
        // TODO: fieldrefs?
        Value falseAssignValue = ((AssignStmt) successor).getLeftOp();
        Value trueAssignValue = ((AssignStmt) target).getLeftOp();
        if (falseAssignValue != trueAssignValue)
            return null;

        //System.out.println("Target same value");

        // 4. Values being assigned must be a constant
        Value falseValue = ((AssignStmt) successor).getRightOp();
        Value trueValue = ((AssignStmt) target).getRightOp();
        if (!(falseValue instanceof IntConstant) ||
                !(trueValue instanceof IntConstant))
            return null;

        // 5. falseInt must be not trueInt
        int falseInt = ((IntConstant) falseValue).value;
        int trueInt = ((IntConstant) trueValue).value;
        //          if ((falseInt ^ trueInt) != 1)
        //              return null;
        if (falseInt != 1 || trueInt != 0)
            return null;

        // 6. Each block must converge
        Unit falseSuccessor = (Unit) chain.getSuccOf(successor);
        Unit trueSuccessor = (Unit) chain.getSuccOf(target);
        if ( !(falseSuccessor instanceof GotoStmt) )
            return null;
        if ( ((GotoStmt) falseSuccessor).getTarget() != trueSuccessor)
            return null;



        // Create new unit
        ConditionExpr ifCondition = (ConditionExpr) rootIfStmt.getCondition();
        System.out.println("if="+ifCondition);
        Value v = null;
        if (ifCondition instanceof CompoundBooleanExpression)
            v = new BooleanNotExpr( ifCondition );
        else
            v = new BooleanNotExpr(ifCondition.getOp1());
        AssignStmt a = new JAssignStmt(falseAssignValue,v);
        System.out.println(a);
        Unit preceeding = (Unit) chain.getPredOf(root);
        chain.insertAfter(a,preceeding);

        // Remove units
        chain.remove(root);
        chain.remove(successor);
        chain.remove(falseSuccessor);
        chain.remove(target);
        //        chain.remove();

        return a;
    }

    public static void main(String args[]) {

        soot.SootMethod testMethod =
            ptolemy.copernicus.jhdl.test.Test.getSootMethod(args);

        soot.Body body = testMethod.retrieveActiveBody();
        soot.toolkits.graph.CompleteUnitGraph unitGraph =
            new soot.toolkits.graph.CompleteUnitGraph(body);
        BriefBlockGraph bbgraph = new BriefBlockGraph(body);
        BlockGraphToDotty toDotty = new BlockGraphToDotty();
        toDotty.writeDotFile(".", "beforegraph", bbgraph);
        try {
            ConditionalControlCompactor.compact(testMethod);
            compact(testMethod);
        } catch (IllegalActionException e) {
            System.err.println(e);
        }
        bbgraph = new BriefBlockGraph(body);
        toDotty.writeDotFile(".", "aftergraph", bbgraph);

    }

}
