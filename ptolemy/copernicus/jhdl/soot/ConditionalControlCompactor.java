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

import ptolemy.copernicus.jhdl.*;

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
//// ConditionalControlCompactor
/**
 * This class contains a method that takes a SootMethod object and
 * compacts the control-flow representation by merging successive
 * conditional statements. Simple Boolean statements are implemented in
 * Byte codes as a series of conditional branches and unnecessarily complicate
 * the Control flow. This class will analyze the byte codes and create
 * Boolean statements in place of a series of conditional statements.
 * This significantly simplifies subsequent control-flow anaylsis.
 *
 * @see ptolemy.copernicus.jhdl.soot.CompoundBooleanExpression
 *
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class ConditionalControlCompactor {

    /**
     * This static method takes a SootMethod and compacts successive
     * Control-flow statements. The result is a modified SootMethod
     * that includes CompoundBooleanExpression Nodes in place of
     * multiple ConditionalExpression statements. Note that this
     * modified SootMethod cannot be used to generate byte codes as
     * several new Expression nodes unique to this package are used.
     **/
    public static void compact(SootMethod method)
            throws IllegalActionException {

        Body mbody = method.retrieveActiveBody();
        PatchingChain chain = mbody.getUnits();

        // Iterate over entire graph until compaction results in no new changes
        boolean unitsModified = false;
        // This vector contains Units that have been removed in a given
        // pass of the loop. This is used to keep track of Units that
        // may be skipped when analyzing the Unit chain.
        Vector removedUnits = new Vector(chain.size());
        do {
            unitsModified = false;

            // iterate over all Units in the method chain
            Iterator i = chain.snapshotIterator();
            for (Unit current = (Unit) i.next();i.hasNext();) {

                //System.out.println("Attemping to merge Unit "+
                //current);

                // If current Unit has been removed, skip over it
                // and get the next one.
                if (removedUnits.contains(current)) {
                    //System.out.println("\tRemoved - ignore");
                    if (i.hasNext())
                        current = (Unit) i.next();
                    else
                        current = null;
                    continue;
                }

                // Attempt to merge unit into current chain.
                Unit mergedUnit = mergeUnit(chain,current);
                // If merging is possible, add to removedUnits list
                // and marge units as modfied. If merging is not
                // possible, get next unit.
                if (mergedUnit != null) {
                    removedUnits.add(mergedUnit);
                    unitsModified = true;
                } else {
                    if (i.hasNext()) {
                        current = (Unit) i.next();
                    }
                    else
                        current = null;
                }

            } while (i.hasNext());

        } while (unitsModified);

    }

    /**
     * This method perfroms the merging. This method looks for the following
     * pattern when deciding to merge:
     * - Current unit must be an instance of the IfStmt
     * - Successor of unit must also be an IfStmt
     * - The target of the root IfStmt is the same as one of
     *   the two targets of the successor IfStmt.
     *
     * This method is looking for the following pattern:
     * Unit n:   rootIfStmt -> target1
     * Unit n+1: succIfStmt -> target2
     * Unit n+2: succSuccessor
     *
     * And (target1 == target2) or (target1 == succSuccessor)
     *
     * If (target1 == succSuccessor), replace Unit n and Unit n+1
     * with the following CompoundAndExpression:
     *    If (NOT(rootIfStmt condition) AND succIfStmt condition)
     *
     * If (target1 == succSuccessor), replace Unit n and Unit n+1
     * with the following CompoundOrExpression:
     *   If (rootIfStmt condition OR succIfStmt condition)
     *
     * This transform avoids the need to insert any additional
     * Gotos and preserves the byte code ordering of the chain.
     *
     **/
    protected static Unit mergeUnit(PatchingChain chain, Unit root)
            throws IllegalActionException {

        // 1. Is root an IfStmt?
        if (!(root instanceof IfStmt))
            return null;
        IfStmt rootIfStmt = (IfStmt) root;
        Unit rootTarget = rootIfStmt.getTarget();

        // 2. Is successor an IfStmt? If so, continue with the merge.
        //    If not, call mergeBooleanAssign to attempt a different
        //    merge.
        Unit successor = (Unit) chain.getSuccOf(root);
        if (!(successor instanceof IfStmt))
            return null;
        IfStmt successorIfStmt = (IfStmt) successor;
        Unit successorSuccessor = (Unit) chain.getSuccOf(successor);
        Unit successorTarget = successorIfStmt.getTarget();

        // 3. See if target of rootIfStmt goes to same unit
        //    as target OR succesesor of successorIfStmt
        if (!((rootTarget == successorSuccessor) ^
                (rootTarget == successorTarget)))
            return null;

        // root and successor units can be merged.
        Value rootCondition = rootIfStmt.getCondition();
        Value successorCondition = successorIfStmt.getCondition();

        CompoundBooleanExpression newExpression;
        if (rootTarget == successorSuccessor) {
            // Expression = 'rootCondition & successorCondition
            newExpression = new CompoundAndExpression(
                    CompoundBooleanExpression.invertValue(rootCondition),
                    successorCondition);

        } else {
            // Expression = rootCondition | successorCondition
            newExpression =
                new CompoundOrExpression(rootCondition,
                        successorCondition);
        }
        rootIfStmt.setCondition(newExpression);

        // 5. Remove successor & patch target
        chain.remove(successor);
        if (rootTarget == successorSuccessor)
            rootIfStmt.setTarget(successorTarget);

        return successor;
    }

    protected static Unit mergeUnit2(PatchingChain chain, Unit root)
            throws IllegalActionException {

        // 1. Is root an IfStmt?
        if (!(root instanceof IfStmt))
            return null;
        IfStmt rootIfStmt = (IfStmt) root;
        Unit rootTarget = rootIfStmt.getTarget();

        // 2. Is successor an IfStmt?
        Unit successor = (Unit) chain.getSuccOf(root);
        if (!(successor instanceof IfStmt))
            return null;
        IfStmt successorIfStmt = (IfStmt) successor;
        Unit successorSuccessor = (Unit) chain.getSuccOf(successor);
        Unit successorTarget = successorIfStmt.getTarget();

        // 3. See if target of rootIfStmt goes to same unit
        //    as target OR succesesor of successorIfStmt
        if (!((rootTarget == successorSuccessor) ^
                (rootTarget == successorTarget)))
            return null;

        // root and successor units can be merged.
        Value rootCondition = rootIfStmt.getCondition();
        Value successorCondition = successorIfStmt.getCondition();

        CompoundBooleanExpression newExpression;
        if (rootTarget == successorSuccessor) {
            // Expression = 'rootCondition & successorCondition
            newExpression = new CompoundAndExpression(
                    CompoundBooleanExpression.invertValue(rootCondition),
                    successorCondition);

        } else {
            // Expression = rootCondition | successorCondition
            newExpression =
                new CompoundOrExpression(rootCondition,
                        successorCondition);
        }
        rootIfStmt.setCondition(newExpression);

        // 5. Remove successor & patch target
        chain.remove(successor);
        if (rootTarget == successorSuccessor)
            rootIfStmt.setTarget(successorTarget);

        return successor;
    }


    public static void main(String args[]) {

        soot.SootMethod testMethod =
            ptolemy.copernicus.jhdl.test.Test.getSootMethod(args);

        soot.Body body = testMethod.retrieveActiveBody();
        soot.toolkits.graph.CompleteUnitGraph unitGraph =
            new soot.toolkits.graph.CompleteUnitGraph(body);
        BriefBlockGraph bbgraph = new BriefBlockGraph(body);
        BlockGraphToDotty toDotty = new BlockGraphToDotty();
        toDotty.writeDotFile(".", "beforegraph",bbgraph);
        try {
            //            compactConditionalControl(testMethod);
            compact(testMethod);
        } catch (IllegalActionException e) {
            System.err.println(e);
        }
        bbgraph = new BriefBlockGraph(body);
        toDotty.writeDotFile(".", "aftergraph",bbgraph);

        // create dataflow for each block
    }

}
