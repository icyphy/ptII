/* A transformer that unrolls loops where the loop bounds can be statically determined

 Copyright (c) 2001-2005 The Regents of the University of California.
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
 */
package ptolemy.copernicus.java;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.util.Chain;

//////////////////////////////////////////////////////////////////////////
//// ConstantLoopUnroller

/**
 A Transformer that attempts to determine the bounds of a loop at compile
 time, and then unroll the loop.  In terms of code generation, the primary
 value of this transformation is that it reduces the control logic of the
 source code, which can increase the power of other compile-time optimizations.

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ConstantLoopUnroller extends BodyTransformer {
    /** Construct a new transformer
     */
    private ConstantLoopUnroller() {
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static ConstantLoopUnroller v() {
        return instance;
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("ConstantLoopUnroller.internalTransform("
                + phaseName + ", " + options + ")");

        Iterator classes = Scene.v().getApplicationClasses().iterator();

        while (classes.hasNext()) {
            SootClass theClass = (SootClass) classes.next();
            Iterator methods = theClass.getMethods().iterator();

            while (methods.hasNext()) {
                SootMethod m = (SootMethod) methods.next();

                if (!m.isConcrete()) {
                    continue;
                }

                JimpleBody body = (JimpleBody) m.retrieveActiveBody();

                internalTransform(body, phaseName, options);
            }
        }
    }

    protected void internalTransform(Body body, String phaseName, Map options) {
        Chain units = body.getUnits();

        BlockGraph graph = new CompleteBlockGraph(body);
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLiveLocals liveLocals = new SimpleLiveLocals(unitGraph);

        for (Iterator blocks = graph.iterator(); blocks.hasNext();) {
            Block block = (Block) blocks.next();

            // filter out anything that doesn't look like a loop block.
            if ((block.getPreds().size() != 1)
                    || (block.getSuccs().size() != 1)) {
                continue;
            }

            // filter out anything that isn't attached to something
            // that looks like a conditional jump.
            Block conditional = (Block) block.getSuccs().get(0);

            if ((conditional != block.getPreds().get(0))
                    || (conditional.getPreds().size() != 2)
                    || (conditional.getSuccs().size() != 2)) {
                continue;
            }

            Block whilePredecessor = (Block) conditional.getPreds().get(0);

            if (whilePredecessor == block) {
                whilePredecessor = (Block) conditional.getPreds().get(1);
            }

            System.out.println("block = " + block);
            System.out.println("cond = " + conditional);
            System.out.println("whilePredecessor = " + whilePredecessor);

            // How many times do we unroll the loop?
            // Look through the conditional, and find the jump back to the
            // start of the block. It should be the only jump in the block.
            IfStmt jumpStmt = null;

            for (Iterator stmts = conditional.iterator(); stmts.hasNext();) {
                Stmt stmt = (Stmt) stmts.next();

                if (stmt instanceof IfStmt) {
                    IfStmt ifStmt = (IfStmt) stmt;

                    if (ifStmt.getTarget() == block.getHead()) {
                        if (jumpStmt == null) {
                            jumpStmt = ifStmt;
                        } else {
                            throw new RuntimeException(
                                    "Two jumps in conditional!");
                        }
                    }
                }
            }

            if (jumpStmt == null) {
                continue;
            }

            // assume that the last statement in the body is the loop counter.
            DefinitionStmt counterStmt = (DefinitionStmt) block.getTail();

            Local counterLocal = (Local) counterStmt.getLeftOp();

            // Determine the loop increment.
            int increment = 0;

            if (counterStmt.getRightOp() instanceof AddExpr) {
                AddExpr addExpr = (AddExpr) counterStmt.getRightOp();
                Value incrementValue;

                if (addExpr.getOp1().equals(counterLocal)) {
                    incrementValue = addExpr.getOp2();
                } else {
                    incrementValue = addExpr.getOp1();
                }

                if (!Evaluator.isValueConstantValued(incrementValue)) {
                    continue;
                }

                increment = ((IntConstant) Evaluator
                        .getConstantValueOf(incrementValue)).value;
                System.out.println("increment = " + increment);
            } else {
                continue;
            }

            BinopExpr conditionalExpr = (BinopExpr) jumpStmt.getCondition();

            // Now determine the loop limit from the conditional block.
            Value limitValue;

            if (conditionalExpr.getOp1().equals(counterLocal)) {
                limitValue = conditionalExpr.getOp2();
            } else {
                limitValue = conditionalExpr.getOp1();
            }

            int limit;

            if (Evaluator.isValueConstantValued(counterStmt.getRightOp())) {
                limit = ((IntConstant) Evaluator.getConstantValueOf(limitValue)).value;
                System.out.println("limit = " + limit + " " + increment);
            } else {
                continue;
            }

            // Lastly, find the initial value of the loop.
            List defsList = localDefs.getDefsOfAt(counterLocal,
                    whilePredecessor.getTail());
            DefinitionStmt initializeStmt = (DefinitionStmt) defsList.get(0);
            int initial;

            if (Evaluator.isValueConstantValued(counterStmt.getRightOp())) {
                initial = ((IntConstant) Evaluator
                        .getConstantValueOf(counterStmt.getRightOp())).value;
                System.out.println("initial = " + initial);
            } else {
                continue;
            }

            System.out.println("initial = " + initial);
            System.out.println("increment = " + increment);
            System.out.println("limit = " + increment);

            /*
             List conditionalLiveLocals = liveLocals.getLiveLocalsBefore(conditional.getHead());
             for (Iterator locals = conditionalLiveLocals.iterator();
             locals.hasNext();) {
             Local local = (Local)locals.next();
             System.out.println("local = " + local);
             System.out.println("stmt = " + block.getTail());
             List defsList;
             try {
             defsList = localDefs.getDefsOfAt(local, block.getTail());
             } catch (Exception ex) {
             continue;
             }
             if (defsList.size() == 0) {
             DefinitionStmt counterStmt = (DefinitionStmt)defsList.get(0);
             if (counterStmt.getRightOp() instanceof AddExpr) {
             AddExpr addExpr = (AddExpr)counterStmt.getRightOp();
             Value incrementValue;
             if (addExpr.getOp1().equals(local)) {
             incrementValue = addExpr.getOp2();
             } else {
             incrementValue = addExpr.getOp1();
             }
             if (!Evaluator.isValueConstantValued(incrementValue)) continue;
             int increment = ((IntConstant)Evaluator.getConstantValueOf(incrementValue)).value;
             defsList = localDefs.getDefsOfAt(local, whilePredecessor.getTail());

             if (defsList.size() == 0) {
             DefinitionStmt initializeStmt =
             (DefinitionStmt)defsList.get(0);
             if (Evaluator.isValueConstantValued(counterStmt.getRightOp())) {
             int initial = ((IntConstant)Evaluator.getConstantValueOf(
             counterStmt.getRightOp())).value;
             Value conditionalExpr = jumpStmt.getCondition();

             Value limitValue;
             if (addExpr.getOp1().equals(local)) {
             limitValue = addExpr.getOp2();
             } else {
             limitValue = addExpr.getOp1();
             }
             int limit = ((IntConstant)Evaluator.getConstantValueOf(incrementValue)).value;

             System.out.println("initial = " + initial);
             System.out.println("increment = " + increment);
             System.out.println("limit = " + increment);
             }
             }
             }
             }
             }

             /*


             List conditionalBoxList = conditionalExpr.getUseBoxes();

             List predecessorDefs = new LinkedList();
             List bodyDefs = new LinkedList();
             List conditionalDefs = new LinkedList();
             while (conditionalBoxList.size() > 0) {
             ValueBox box = (ValueBox)conditionalBoxList.get(0);
             conditionalBoxList.remove(box);
             if (!(box.getValue() instanceof Local)) continue;
             List defsList = localDefs.getDefsOfAt((Local)box.getValue(), jumpStmt);
             for (Iterator defs = defsList.iterator();
             defs.hasNext();) {
             Unit definitionStmt = (Unit)defs.next();

             if (_blockContains(block, definitionStmt)) {
             bodyDefs.add(definitionStmt);
             } else if (_blockContains(conditional, definitionStmt)) {
             // conditionalBoxList.addAll(definitionStmt.getUseBoxes());
             conditionalDefs.add(definitionStmt);
             } else {
             predecessorDefs.add(definitionStmt);
             }
             }
             }
             System.out.println("predecessorDefs = ");
             for (Iterator i = predecessorDefs.iterator();
             i.hasNext();) {
             System.out.println("stmt = " + i.next());
             }
             System.out.println("bodyDefs = ");
             for (Iterator i = bodyDefs.iterator();
             i.hasNext();) {
             System.out.println("stmt = " + i.next());
             }

             // FINALLY we know we've found something we can unroll... :)
             // System.out.println("is unrollable...");

             // There should be a jump from the predecessor to the
             // condition.  Redirect this jump to the block.
             /*
             conditional.getHead().redirectJumpsToThisTo(block.getHead());

             Local thisLocal = body.getThisLocal();
             Chain units = body.getUnits();
             List bodyStmtList = new LinkedList();
             // pull the statements that we are inlining out of the body
             // so that we can copy them.  Note that this also removes
             // them from the method body.
             Unit insertPoint = (Unit)units.getSuccOf(block.getTail());
             for (Iterator blockStmts = block.iterator();
             blockStmts.hasNext();) {
             Stmt original = (Stmt)blockStmts.next();
             blockStmtList.add(original);
             blockStmts.remove();
             }

             // Loop through and unroll the loop block once for
             // every element of the field list.
             /*
             for (int i = 0; i < times; i++) {
             SootField insertField =
             (SootField)fields.next();
             for (Iterator blockStmts = blockStmtList.iterator();
             blockStmts.hasNext();) {
             // clone each statement
             Stmt original = (Stmt)blockStmts.next();
             Stmt clone = (Stmt)original.clone();
             // If the statement is a call to the next() method,
             // then inline it with the next value of the iterator.
             for (Iterator boxes = clone.getUseBoxes().iterator();
             boxes.hasNext();) {
             ValueBox box = (ValueBox)boxes.next();
             Value value = box.getValue();
             if (value instanceof InvokeExpr) {
             InvokeExpr r = (InvokeExpr)value;
             if (r.getMethod() == iteratorNextMethod) {
             box.setValue(Jimple.v()
             .newInstanceFieldRef(thisLocal,
             insertField));
             }
             }
             }
             units.insertBefore(clone, insertPoint);
             }
             }

             // remove the conditional
             for (Iterator blockStmts = conditional.iterator();
             blockStmts.hasNext();) {
             Stmt original = (Stmt)blockStmts.next();
             blockStmts.remove();
             }*/
        }
    }

    private static boolean _blockContains(Block block, Object object) {
        for (Iterator i = block.iterator(); i.hasNext();) {
            if (i.next().equals(object)) {
                return true;
            }
        }

        return false;
    }

    private static ConstantLoopUnroller instance = new ConstantLoopUnroller();
}
