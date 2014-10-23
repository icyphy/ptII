/* A transformer that removes dead token and type creations.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.copernicus.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.EntryPoints;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Targets;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalDefs;

/**
 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SideEffectFreeInvocationRemover extends SceneTransformer {
    /** Construct a new transformer
     */
    private SideEffectFreeInvocationRemover() {
    }

    /* Return the instance of this transformer.
     */
    public static SideEffectFreeInvocationRemover v() {
        return instance;
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "";
    }

    @Override
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("SideEffectFreeInvocationRemover.internalTransform("
                + phaseName + ", " + options + ")");

        Scene.v().releaseCallGraph();

        // Temporary hack to deal with interfaces...  assume that methods of
        // interfaces are automatically reachable.
        HashSet forcedReachableMethodSet = new HashSet();
        forcedReachableMethodSet.addAll(EntryPoints.v().application());

        // Loop over all the classes...
        for (Iterator i = Scene.v().getApplicationClasses().iterator(); i
                .hasNext();) {
            SootClass theClass = (SootClass) i.next();

            // If we are in actor mode, then assert that all the
            // methods of the toplevel class are reachable.  We need a
            // way of preserving the container, name constructor
            // instead of the no arg constructor for the toplevel.
            //            SootClass modelClass = ModelTransformer.getModelClass();
            //           if (theClass.equals(modelClass)) {
            //                 Set methodSet = _getMethodSet(theClass);
            //                 forcedReachableMethodSet.addAll(methodSet);
            //             }
            // Assume that any method that is part of an interface that this
            // object implements, is reachable.
            //  System.out.println("forcing interfaces of " + theClass);
            if (!theClass.isInterface()) {
                for (Iterator interfaces = theClass.getInterfaces().iterator(); interfaces
                        .hasNext();) {
                    SootClass theInterface = (SootClass) interfaces.next();

                    _addMethodsFrom(forcedReachableMethodSet, theInterface,
                            theClass);
                }
            }
        }

        System.out.println("forcedMethods = " + forcedReachableMethodSet);

        // Loop over all the classes...
        for (Iterator i = Scene.v().getApplicationClasses().iterator(); i
                .hasNext();) {
            SootClass theClass = (SootClass) i.next();
            Scene.v().loadClassAndSupport(theClass.getName());
        }

        System.out.println("done loading classes!");

        // Construct the graph of methods that are directly reachable
        // from any method.
        // Construct the graph of all method invocations, so we know what
        // method contains each invocation and what method(s) can be
        // targeted by that invocation.
        CallGraphBuilder cg = new CallGraphBuilder(DumbPointerAnalysis.v(),
                true);
        cg.build();
        CallGraph callGraph = Scene.v().getCallGraph();
        Scene.v().setCallGraph(callGraph);

        SideEffectAnalysis analysis = new SideEffectAnalysis();

        for (Iterator classes = Scene.v().getApplicationClasses().iterator(); classes
                .hasNext();) {
            SootClass theClass = (SootClass) classes.next();

            for (Iterator methods = theClass.getMethods().iterator(); methods
                    .hasNext();) {
                SootMethod method = (SootMethod) methods.next();
                _removeSideEffectFreeMethodCalls(method, callGraph, analysis);
            }
        }
    }

    /** Remove any calls to other methods from the given method that
     *  have no side effects and whose return value is dead.  A method
     *  has no side effects if it does not assign the value to any
     *  fields.
     */
    public static void _removeSideEffectFreeMethodCalls(SootMethod method,
            CallGraph callGraph, SideEffectAnalysis analysis) {
        Body body = method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

        // this will help us figure out where locals are defined.
        /*SimpleLocalDefs localDefs = */new SimpleLocalDefs(unitGraph);
        SimpleLiveLocals liveLocals = new SimpleLiveLocals(unitGraph);

        for (Iterator units = body.getUnits().snapshotIterator(); units
                .hasNext();) {
            Unit unit = (Unit) units.next();
            Value useValue;

            // Find a method invocation that doesn't have a return
            // value, or whose return value is dead.
            if (unit instanceof DefinitionStmt) {
                DefinitionStmt stmt = (DefinitionStmt) unit;
                Value left = stmt.getLeftOp();

                // If this statement defines a local that is later used,
                // then we cannot remove it.
                if (liveLocals.getLiveLocalsAfter(stmt).contains(left)) {
                    continue;
                }

                useValue = stmt.getRightOp();
            } else if (unit instanceof InvokeStmt) {
                useValue = ((InvokeStmt) unit).getInvokeExpr();
            } else {
                continue;
            }

            // Special invokes don't get removed.  This is because
            // special invokes are used for super method calls.  We
            // really do want to get rid of constructors to objects
            // that aren't used, but we have to be smarter about the
            // whole business (we have to remove the New as well, for
            // instance)
            if (useValue instanceof VirtualInvokeExpr
                    || useValue instanceof StaticInvokeExpr) {
                //InvokeExpr invokeExpr = (InvokeExpr) useValue;

                // If any targets of the invocation have side effects,
                // then they cannot be removed.
                boolean removable = true;

                for (Iterator i = new Targets(callGraph.edgesOutOf(unit)); i
                        .hasNext() && removable;) {
                    SootMethod targetMethod = (SootMethod) i.next();

                    // System.out.println("Checking Target = " + targetMethod);
                    if (analysis.hasSideEffects(targetMethod)) {
                        removable = false;
                    }
                }

                if (removable) {
                    // Otherwise we've found an invocation we can remove.
                    // Remove it.
                    // System.out.println("SEFIR: removing " + unit);
                    body.getUnits().remove(unit);
                }
            }
        }
    }

    private void _addMethodsFrom(Set forcedReachableMethodSet,
            SootClass theInterface, SootClass theClass) {
        // Except for InequalityTerm...
        if (theInterface.getName().equals("ptolemy.graph.InequalityTerm")) {
            return;
        }

        Set methodSet = _getMethodSet(theInterface);

        for (Iterator methods = methodSet.iterator(); methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();

            SootMethod classMethod = null;
            try {
                classMethod = theClass.getMethod(method.getSubSignature());
                forcedReachableMethodSet.add(classMethod);
            } catch (Exception ex) {
                System.err
                        .println("SideEffectFreeInvocationRemover: could not add "
                                + classMethod + " to the reachable method set.");
            }
        }

        for (Iterator superInterfaces = theInterface.getInterfaces().iterator(); superInterfaces
                .hasNext();) {
            _addMethodsFrom(forcedReachableMethodSet,
                    (SootClass) superInterfaces.next(), theClass);
        }
    }

    // Return a set of the methods in the given class.
    private Set _getMethodSet(SootClass theClass) {
        Set methodSet = new HashSet();
        List methodList = new ArrayList(theClass.getMethods());

        for (Iterator methods = methodList.iterator(); methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();

            if (method != null) {
                System.out.println("Assuming method " + method
                        + " is reachable");
                methodSet.add(method);
            }
        }

        return methodSet;
    }

    private static SideEffectFreeInvocationRemover instance = new SideEffectFreeInvocationRemover();
}
