/* A transformer that reports library methods used.

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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.EntryPoints;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.MethodOrMethodContext;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.EdgePredicate;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;

//////////////////////////////////////////////////////////////////////////
//// LibraryUsageReporter

/**
 A Transformer that reports reachable methods in the Java libraries.

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)

 */
public class LibraryUsageReporter extends SceneTransformer implements
HasPhaseOptions {
    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static LibraryUsageReporter v() {
        return _instance;
    }

    @Override
    public String getPhaseName() {
        return "";
    }

    @Override
    public String getDefaultOptions() {
        return "analyzeAllReachables:false";
    }

    @Override
    public String getDeclaredOptions() {
        return "outFile analyzeAllReachables";
    }

    @Override
    protected void internalTransform(String phaseName, Map options) {
        String outFile = PhaseOptions.getString(options, "outFile");
        boolean analyzeAllReachables = PhaseOptions.getBoolean(options,
                "analyzeAllReachables");
        System.out.println("LibraryUsageReporter.internalTransform("
                + phaseName + ", " + options + ")");
        Scene.v().releaseCallGraph();

        CallGraphBuilder cg = new CallGraphBuilder(DumbPointerAnalysis.v() /*,true*/);
        cg.build();
        CallGraph callGraph = Scene.v().getCallGraph();
        ReachableMethods reachableMethods = new ReachableMethods(callGraph,
                new ArrayList<MethodOrMethodContext>(EntryPoints.v().application()));

        reachableMethods.update();

        Hierarchy hierarchy = Scene.v().getActiveHierarchy();

        final Set createableClasses = new HashSet();

        for (Iterator reachables = reachableMethods.listener(); reachables
                .hasNext();) {
            SootMethod method = (SootMethod) reachables.next();
            //String methodName = method.getSignature();

            if (method.getName().equals("<init>")
                    && !method.getDeclaringClass().getName().startsWith("java")) {
                createableClasses
                .addAll(hierarchy.getSuperclassesOfIncluding(method
                        .getDeclaringClass()));
                _addAllInterfaces(createableClasses, method.getDeclaringClass());
            }
        }

        System.out.println("createableClasses = " + createableClasses);

        // Now create a new set of reachable methods that only
        // includes methods that are static or are declared in classes
        // that can are created.
        Filter filter = new Filter(new EdgePredicate() {
            @Override
            public boolean want(Edge e) {
                SootMethod target = e.tgt();
                return e.isExplicit()
                        && (target.isStatic() || createableClasses
                                .contains(target.getDeclaringClass()));
            }
        });
        Set necessaryClasses = new HashSet();
        ReachableMethods RTAReachableMethods = new ReachableMethods(callGraph,
                new ArrayList<MethodOrMethodContext>(EntryPoints.v().application()).iterator(), filter);
        RTAReachableMethods.update();

        List list = new LinkedList();

        for (Iterator reachables = RTAReachableMethods.listener(); reachables
                .hasNext();) {
            SootMethod method = (SootMethod) reachables.next();
            String methodName = method.getSignature();
            list.add(methodName);

            SootClass declaringClass = method.getDeclaringClass();

            if (!declaringClass.getName().startsWith("java")) {
                necessaryClasses.add(declaringClass);
            }

            if (method.isConcrete()) {
                for (Iterator units = method.retrieveActiveBody().getUnits()
                        .iterator(); units.hasNext();) {
                    Unit unit = (Unit) units.next();

                    for (Iterator boxes = unit.getUseBoxes().iterator(); boxes
                            .hasNext();) {
                        ValueBox box = (ValueBox) boxes.next();
                        Value value = box.getValue();

                        if (value instanceof CastExpr) {
                            CastExpr expr = (CastExpr) value;
                            Type castType = expr.getCastType();

                            if (castType instanceof RefType) {
                                SootClass castClass = ((RefType) castType)
                                        .getSootClass();

                                if (castClass.isInterface()) {
                                    necessaryClasses.add(castClass);
                                } else {
                                    necessaryClasses
                                    .addAll(hierarchy
                                            .getSuperclassesOfIncluding(castClass));
                                }

                                _addAllInterfaces(necessaryClasses, castClass);
                            }
                        } else if (value instanceof InstanceOfExpr) {
                            InstanceOfExpr expr = (InstanceOfExpr) value;
                            Type checkType = expr.getCheckType();

                            if (checkType instanceof RefType) {
                                SootClass checkClass = ((RefType) checkType)
                                        .getSootClass();

                                if (!checkClass.isInterface()) {
                                    necessaryClasses
                                    .addAll(hierarchy
                                            .getSuperclassesOfIncluding(checkClass));
                                }

                                _addAllInterfaces(necessaryClasses, checkClass);
                            }
                        }
                    }
                }
            }
        }

        // Print out all the used methods
        Collections.sort(list);

        for (Iterator names = list.iterator(); names.hasNext();) {
            System.out.println(names.next());
        }

        try {
            // Add to the set of necessary classes all that they depend on.
            DependedClasses dependedClasses = new DependedClasses(
                    necessaryClasses);
            FileWriter writer = null;
            try {
                writer = new FileWriter(outFile);

                for (Iterator classes = dependedClasses.list().iterator(); classes
                        .hasNext();) {
                    SootClass theClass = (SootClass) classes.next();

                    if (analyzeAllReachables) {
                        // Set the class to be an application class, so we can
                        // analyze it.
                        theClass.setApplicationClass();
                    }

                    writer.write(theClass.getName());
                    writer.write("\n");
                }
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void _addAllInterfaces(Set classSet, SootClass theClass) {
        for (Iterator i = theClass.getInterfaces().iterator(); i.hasNext();) {
            SootClass theInterface = (SootClass) i.next();
            classSet.add(theInterface);
            _addAllInterfaces(classSet, theInterface);
        }
    }

    private static LibraryUsageReporter _instance = new LibraryUsageReporter();
}
