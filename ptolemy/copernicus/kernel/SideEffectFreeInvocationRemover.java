/* A transformer that removes dead token and type creations.

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

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.MethodCallGraph;
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
import ptolemy.data.type.Typeable;
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;


/**

*/
public class SideEffectFreeInvocationRemover extends SceneTransformer {
    /** Construct a new transformer
     */
    private SideEffectFreeInvocationRemover() {}

    /* Return the instance of this transformer.
     */
    public static SideEffectFreeInvocationRemover v() {
        return instance;
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions();
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("SideEffectFreeInvocationRemover.internalTransform("
                + phaseName + ", " + options + ")");

        InvokeGraph invokeGraph =
            ClassHierarchyAnalysis.newInvokeGraph();
        MethodCallGraph methodCallGraph =
            (MethodCallGraph)invokeGraph.newMethodGraph();
        SideEffectAnalysis analysis =
            new SideEffectAnalysis(methodCallGraph);

        for (Iterator classes = Scene.v().getApplicationClasses().iterator();
             classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                _removeSideEffectFreeMethodCalls(method, invokeGraph, analysis);
            }
        }
    }

    /** Remove any calls to other methods from the given method that
     *  have no side effects and whose return value is dead.  A method
     *  has no side effects if it does not assign the value to any
     *  fields.
     */
    public static void _removeSideEffectFreeMethodCalls(SootMethod method,
            InvokeGraph invokeGraph, SideEffectAnalysis analysis) {
        Body body = method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLiveLocals liveLocals = new SimpleLiveLocals(unitGraph);

        for (Iterator units = body.getUnits().snapshotIterator();
             units.hasNext();) {
            Unit unit = (Unit)units.next();
            Value useValue;

            // Find a method invocation that doesn't have a return
            // value, or whose return value is dead.
            if (unit instanceof DefinitionStmt) {
                DefinitionStmt stmt = (DefinitionStmt)unit;
                Value left = stmt.getLeftOp();
                // If this statement defines a local that is later used,
                // then we cannot remove it.
                if (liveLocals.getLiveLocalsAfter(stmt).contains(left)) {
                    continue;
                }
                useValue = stmt.getRightOp();
            } else if (unit instanceof InvokeStmt) {
                useValue = ((InvokeStmt)unit).getInvokeExpr();
            } else {
                continue;
            }

            // Special invokes don't get removed.  This is because
            // special invokes are used for super method calls.  We
            // really do want to get rid of constructors to objects
            // that aren't used, but we have to be smarter about the
            // whole business (we have to remove the New as well, for
            // instance)
            if (useValue instanceof VirtualInvokeExpr ||
                    useValue instanceof StaticInvokeExpr) {
                InvokeExpr invokeExpr = (InvokeExpr)useValue;

                // If any targets of the invocation have side effects,
                // then they cannot be removed.
                boolean removable = true;
                for (Iterator i = invokeGraph.getTargetsOf(
                        (Stmt)unit).iterator();
                     i.hasNext() && removable;) {

                    SootMethod targetMethod = (SootMethod)i.next();
                    System.out.println("Checking Target = " + targetMethod);
                    if (analysis.hasSideEffects(targetMethod)) {
                        removable = false;
                    }
                }

                if (removable) {
                    // Otherwise we've found an invocation we can remove.
                    // Remove it.
                    System.out.println("SEFIR: removing " + unit);
                    body.getUnits().remove(unit);
                }
            }
        }
    }

    private static SideEffectFreeInvocationRemover instance =
    new SideEffectFreeInvocationRemover();
}














