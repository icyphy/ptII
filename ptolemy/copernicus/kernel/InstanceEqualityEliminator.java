/* A transformer that tries to statically evaluate object == object

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


package ptolemy.copernicus.kernel;

import soot.Local;
import soot.NullType;
import soot.Options;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.JimpleBody;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.toolkits.graph.CompleteUnitGraph;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
A transformer that removes instance equality checks.
It uses alias analysis to determine what locals can point to the same object,
allowing static evaluation of simple conditions.
Specifically, <i>ref1 == ref2</i> can be replaced with true if <i>ref1</i>
and <i>ref2</i> are must-aliases of each other, and false if <i>ref1</> and <i>ref2</i>
are not maybe aliases of each other.  Similarly, <i>ref1 != ref2</i> can be
replaced with true if <i>ref1</> and <i>ref2</i> are not maybe aliases of
each other and with false if they are must-aliases
<p>
However, in general, making decisions base on must-aliases is much easier
than making decisions on maybe aliases...  in particular, a conservative
must alias analysis makes it safe

*/

public class InstanceEqualityEliminator extends SceneTransformer
{
    private static InstanceEqualityEliminator instance = new InstanceEqualityEliminator();
    private InstanceEqualityEliminator() {}

    public static InstanceEqualityEliminator v() {
        return instance;
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " debug";
    }


    protected void internalTransform(String phaseName, Map options)
    {
        System.out.println("InstanceEqualityEliminator.internalTransform("
                + phaseName + ", " + options + ")");

        boolean debug = Options.getBoolean(options, "debug");

        /*  if (debug) System.out.println("building invoke graph");
            InvokeGraph invokeGraph =
            ClassHierarchyAnalysis.newInvokeGraph();
            if (debug) System.out.println("done");
            if (debug) System.out.println("building method call graph");
            MethodCallGraph methodCallGraph =
            (MethodCallGraph)invokeGraph.newMethodGraph();
            if (debug) System.out.println("done");
            if (debug) System.out.println("analyzing sideeffecting methods");
            SideEffectAnalysis sideEffectAnalysis =
            new SideEffectAnalysis(methodCallGraph);
            if (debug) System.out.println("done");
        */

        Iterator classes = Scene.v().getApplicationClasses().iterator();
        while (classes.hasNext()) {
            SootClass theClass = (SootClass)classes.next();
            Iterator methods = theClass.getMethods().iterator();
            while (methods.hasNext()) {
                SootMethod m = (SootMethod) methods.next();
                if (!m.isConcrete())
                    continue;
                JimpleBody body = (JimpleBody) m.retrieveActiveBody();
                removeInstanceEqualities(body, null, debug);
            }
        }
    }


    public static void removeInstanceEqualities(JimpleBody body,
            InvokeGraph invokeGraph, boolean debug) {
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

        if (debug) System.out.println("analyzing body of " + body.getMethod());

        // The analyses that give us the information to transform the code.
        NullPointerAnalysis nullPointerAnalysis =
            new NullPointerAnalysis(unitGraph);
        // if (debug) System.out.println("done nullpointers");
        MustAliasAnalysis mustAliasAnalysis =
            new MustAliasAnalysis(unitGraph);

        //if (debug) System.out.println("done mustAliases");
        MaybeAliasAnalysis maybeAliasAnalysis =
            new MaybeAliasAnalysis(unitGraph);
        //if (debug) System.out.println("done maybeAliases");

        //System.out.println("done analyzing");
        // Loop through all the unit
        for (Iterator units = body.getUnits().iterator();
             units.hasNext();) {
            Unit unit = (Unit)units.next();
            for (Iterator boxes = unit.getUseBoxes().iterator();
                 boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();

                if (value instanceof BinopExpr) {
                    BinopExpr binop = (BinopExpr)value;
                    Value left = binop.getOp1();
                    Value right = binop.getOp2();
                    if (left.getType() instanceof RefType &&
                            right.getType() instanceof RefType) {
                        Set leftMustAliases, rightMustAliases;
                        Set leftMaybeAliases, rightMaybeAliases;
                        leftMustAliases =
                            mustAliasAnalysis.getAliasesOfBefore(
                                    (Local)left, unit);
                        leftMaybeAliases =
                            maybeAliasAnalysis.getAliasesOfBefore(
                                    (Local)left, unit);
                        rightMustAliases =
                            mustAliasAnalysis.getAliasesOfBefore(
                                    (Local)right, unit);
                        rightMaybeAliases =
                            maybeAliasAnalysis.getAliasesOfBefore(
                                    (Local)right, unit);
                        if (debug) System.out.println("Ref-ref unit = " + unit);
                        if (debug) System.out.println("left aliases = " +
                                leftMustAliases);
                        if (debug) System.out.println("right aliases = " +
                                rightMustAliases);
                        if (debug) System.out.println("left maybe aliases = " +
                                leftMaybeAliases);
                        if (debug) System.out.println("right maybe aliases = "
                                + rightMaybeAliases);

                        if (leftMustAliases.contains(right) &&
                                rightMustAliases.contains(left)) {
                            if (debug) System.out.println("instances are equal");
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(0));
                        } else {
                            // If either set of maybe aliases is unknown, then we can do nothing.
                            if (leftMaybeAliases != null && rightMaybeAliases != null) {
                                if (!leftMaybeAliases.contains(right) && !rightMaybeAliases.contains(left)) {
                                    if (debug) System.out.println("instances are not equal");
                                    // Replace with operands that can be statically evaluated.
                                    binop.getOp1Box().setValue(IntConstant.v(0));
                                    binop.getOp2Box().setValue(IntConstant.v(1));
                                }
                            }
                        }
                    } else if (left.getType() instanceof NullType &&
                            right.getType() instanceof NullType) {
                        if (debug) System.out.println("Null-Null unit = " + unit);
                        // must be equal...
                        binop.getOp1Box().setValue(IntConstant.v(0));
                        binop.getOp2Box().setValue(IntConstant.v(0));
                    } else if (left.getType() instanceof NullType &&
                            right.getType() instanceof RefType) {
                        // Then the right side is the one we must analyze.
                        if (debug) System.out.println("Null-Ref unit = " + unit);
                        Local local = (Local)right;
                        if (nullPointerAnalysis.isAlwaysNullBefore(local, unit)) {
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(0));
                        } else if (nullPointerAnalysis.isNeverNullBefore(local, unit)) {
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(1));
                        }
                    } else if (left.getType() instanceof RefType &&
                            right.getType() instanceof NullType) {
                        // Then the right side is the one we must analyze.
                        if (debug) System.out.println("Ref-Null unit = " + unit);
                        Local local = (Local)left;
                        if (nullPointerAnalysis.isAlwaysNullBefore(local, unit)) {
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(0));
                        } else if (nullPointerAnalysis.isNeverNullBefore(local, unit)) {
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(1));
                        }
                    }
                }
            }
        }
    }
}





