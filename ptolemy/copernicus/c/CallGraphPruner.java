/* Class that uses the Soot Framework to find out which methods/classes
   are really needed for code generation.

 Copyright (c) 2002-2003 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;


import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.Hierarchy;

import soot.jimple.FieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.SparkTransformer;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.TransitiveTargets;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.Edge;

import soot.jimple.spark.pag.PAG;

import soot.options.SparkOptions;

import soot.util.queue.QueueReader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// CallGraphPruner
/**
Class that uses the Soot Framework to find out which methods/classes
are really needed for code generation.

@author Ankush Varma
@version $Id$
*/

public class CallGraphPruner {

    /** Dummy constructor to allow inheritance.*/
    public CallGraphPruner() {
    }

    /** Constructor. Creates an CallGraph and applies a specialized
     * pruning strategy to it, tailored for C code generation.
     * @param source The class to use as the root for the pruned tree.
     */
    public CallGraphPruner(SootClass source) {
        Scene.v().setMainClass(source);

        Map sootOptions = new Hashtable();
        //sootOptions.put("vta", "true");
        sootOptions.put("on-fly-cg", "true");
        sootOptions.put("simulate-natives", "false");
        sootOptions.put("enabled", "true");
        sootOptions.put("verbose", "true");

        sootOptions.put("propagator", "worklist");
        sootOptions.put("set-impl", "double");
        sootOptions.put("double-set-old", "hybrid");
        sootOptions.put("double-set-new", "hybrid");
        //CHATransformer.v().transform("cg.cha", sootOptions);

        SparkOptions sparkOptions = new SparkOptions(sootOptions);
        /*
        PAG analyzer = new PAG(sparkOptions);

        CallGraphBuilder builder = new CallGraphBuilder(analyzer);
        CallGraph callGraph = builder.getCallGraph();
        Scene.v().setCallGraph(callGraph);
        */

        // Set entry points for call graph. The default entry points lead to
        // excessive code size.
        Scene.v().setEntryPoints(_getCallGraphEntryPoints(source));
        SparkTransformer.v().transform("cg.spark", sootOptions);

        _growTree(source);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the set of all reachable classes.
     *  @return The set of all reachable classes.
     */
    public HashSet getReachableClasses() {
        return _reachableClasses;
    }

    /** Get the set of all reachable fields.
     *  @return The set of all reachable fields.
     */
    public HashSet getReachableFields() {
        return _reachableFields;
    }

    /** Get the set of all reachable methods.
     *  @return The set of all reachable methods.
     */
    public HashSet getReachableMethods() {
        return _reachableMethods;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /** Adds a whole collection of classes/methods/fields to _gray.
     * @param nodes The collection of nodes to add.
     */
    protected void _add(Collection nodes) {
        Iterator i = nodes.iterator();
        while (i.hasNext()){
            _add(i.next());
        }
    }

    /** Adds an object to the _gray list if the object is not in the gray
     * or a _reachable list. The object is assumed to be a class, method or
     * field.
     * @param node The object to add.
     */
    protected void _add(Object node) {
        if (!_gray.contains(node)) {
            if (node instanceof SootClass) {
                if (!_reachableClasses.contains(node)) {
                    _gray.addLast(node);
                }
            }
            else if (node instanceof SootMethod) {
                if (!_reachableMethods.contains(node)) {
                    _gray.addLast(node);
                }
            }
            else if (node instanceof SootField) {
                if (!_reachableFields.contains(node)) {
                    _gray.addLast(node);
                }
            }
        }

    }

    /** Adds a class, method or field  to the appropriate reachable list.
     * @param node The class/field/method to process.
     */
    protected void _done(Object node) {
        if (_gray.contains(node)) {
            _gray.remove(node);
        }
        if (node instanceof SootClass) {
            _reachableClasses.add(node);
        }
        else if (node instanceof SootMethod) {
            _reachableMethods.add(node);
        }
        else if (node instanceof SootField) {
            _reachableFields.add(node);
        }
    }

    /** Returns the list of methods that should be considered entry points
     * for building the CallGraph.
     * @param source The main class in the Scene.
     * @return The list of entry points.
     */
    protected LinkedList _getCallGraphEntryPoints(SootClass source) {
        // Just pick the compulsory nodes that are methods but are not
        // overridden.
        LinkedList entryPoints = new LinkedList();
        Iterator nodes = _getCompulsoryNodes().iterator();

        while (nodes.hasNext()){
            Object node = nodes.next();
            if (node instanceof SootMethod) {
                SootMethod method = (SootMethod)node;
                if (method.isConcrete()
                        && !OverriddenMethodGenerator.isOverridden(method)) {
                    entryPoints.add(method);
                }
            }
        }

        if (source.declaresMethodByName("main")) {
            entryPoints.add(source.getMethodByName("main"));
        }

        return entryPoints;
    }

    /** Returns the list of nodes(methods/classes/fields) that are always
     * needed, regardless of whether the source class reaches them
     * explicitly.
     * @return The list of compulsory methods.
     */
    protected LinkedList _getCompulsoryNodes() {
        LinkedList compulsoryNodes = new LinkedList();

        // Add java.lang.String.String(char[]). Initializer
        SootClass source = Scene.v().getSootClass("java.lang.String");
        SootMethod method = source.getMethod("void <init>(char[])");
        compulsoryNodes.add(method);
        // Add java.lang.String.<clinit>.
        method = source.getMethodByName("<clinit>");
        compulsoryNodes.add(method);
        // java.lang.String.toString() is needed.
        method = source.getMethodByName("toString");
        compulsoryNodes.add(method);
        // All fields of String are required.
        compulsoryNodes.addAll(source.getFields());

        // Add java.lang.System.initializeSystemClass()
        source = Scene.v().getSootClass("java.lang.System");
        method = source.getMethodByName("initializeSystemClass");
        compulsoryNodes.add(method);
        // System.out is required by initializeSystemClass
        SootField field = source.getFieldByName("out");
        compulsoryNodes.add(field);
        // System.err is required by initializeSystemClass
        field = source.getFieldByName("err");
        compulsoryNodes.add(field);


        // Printstream is required by the force-overridden version of
        // System.initializeSystemClass(), but it doesn't have a clinit.
        source = Scene.v().getSootClass("java.io.PrintStream");
        method = source.getMethod("void println(int)");
        compulsoryNodes.add(method);

        // Class is required by Object.
        source = Scene.v().getSootClass("java.lang.Class");
        compulsoryNodes.add(source);

        // Exception is required pccg_runtime.h
        source = Scene.v().getSootClass("java.lang.Exception");
        compulsoryNodes.add(source);

        // Printstream is required in initializeSystemClass.
        source = Scene.v().getSootClass("java.io.PrintStream");
        compulsoryNodes.add(source);

        return compulsoryNodes;
    }

    /** Returns the list of methods required by inheritance. If a class C
     * implements an interface I or extends a class I, then C.m is required
     * if I.m is required.
     * @param classSet The set of classes that are candidates for C.
     * @param methodSet The set of methods to which I.m may belong.
     * @return The set of all methods of <i>classes</i> that may be
     * implementing a method in <i>methods </i>.
     */
    protected HashSet _getMethodsRequiredByInheritance(Collection
        classSet, Collection methodSet) {
        HashSet requiredMethodSet = new HashSet();

        Iterator classes = classSet.iterator();
        while (classes.hasNext()) {
            SootClass source = (SootClass)classes.next();
            Iterator methods = methodSet.iterator();
            Hierarchy hierarchy = new Hierarchy();

            // Candidates for I. All superclasses and all implemented
            // interfaces.
            Collection allParents = AnalysisUtilities.getAllInterfacesOf(source);
            if (!source.isInterface()) {
                     allParents.addAll(hierarchy.getSuperclassesOf(source));
            }

            while (methods.hasNext()) {
                SootMethod method = (SootMethod)methods.next();
                String subSignature = method.getSubSignature();

                if (source.declaresMethod(subSignature)
                    && allParents.contains(method.getDeclaringClass())
                    ){
                    requiredMethodSet.add(source.getMethod(subSignature));
                }
            }
        }

        return requiredMethodSet;
    }


    /** Returns a set of the nodes referenced in the body of a given
     * method. This includes:
     * <UL>
     * <LI> Methods directly called (sometimes invokegraph fails to catch
     * these).
     * <LI> Fields accessed.
     * <LI> Classes called by instanceof expressions.
     * </UL>
     * These are computed here in the same method so that only one pass
     * through the statements in the body is required.
     * @param method The method.
     * @return The set of nodes unambiguously referenced in the statements
     * comprising its body.
     */
    protected HashSet _getNodesAccessedInBodyOf(SootMethod method) {
        HashSet nodes = new HashSet();
        Scene.v().loadClassAndSupport(method.getDeclaringClass().getName());

        if (method.isConcrete() && !OverriddenMethodGenerator
                .isOverridden(method)) {
            boolean leaf = _isLeaf(method);

            Iterator units = method.retrieveActiveBody()
                .getUnits().iterator();
            while (units.hasNext()) {
                Unit unit = (Unit)units.next();
                if (unit instanceof Stmt) {
                    Stmt stmt = (Stmt)unit;

                    // Add accessed fields.
                    if (stmt.containsFieldRef()) {
                        FieldRef fieldRef = (FieldRef)stmt.getFieldRef();
                        SootField field = fieldRef.getField();
                        nodes.add(field);
                    }

                    // Add directly called methods.
                                        if (!leaf && stmt.containsInvokeExpr()) {
                        SootMethod m = ((InvokeExpr)stmt.getInvokeExpr())
                            .getMethod();
                        nodes.add(m);
                        nodes.add(m.getDeclaringClass());
                    }
                }

                // Get all classes used in all "instanceof" expressions.
                Iterator boxes = unit.getUseAndDefBoxes().iterator();
                while (boxes.hasNext()) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    if (value instanceof InstanceOfExpr) {
                        InstanceOfExpr expr = (InstanceOfExpr)value;
                        Type checkType = expr.getCheckType();
                        if (checkType instanceof RefType) {
                            nodes.add(((RefType)checkType).getSootClass());
                        }
                    }
                }
            }
        }

        return nodes;
    }

    /** Gets the set of fields, methods and classes to be started off with.
     * @return The set of nodes that are needed to start off with.
     */
    protected HashSet _getRoots(SootClass source) {
        // Using a HashSet prevents duplication.
        HashSet roots = new HashSet();

        roots.addAll(source.getMethods());
        roots.addAll(source.getFields());
        roots.addAll(_getCompulsoryNodes());
        roots.add(source);
        roots.addAll(Scene.v().getSootClass("java.lang.Object").getMethods());

        return roots;
    }

    /** Computes the set of classes, methods and fields reachable from a
     * given class.
     * @param source The class.
     */
    protected void _growTree(SootClass source) {

        // FIFO queue to store all nodes for processing.
        _gray.addAll(_getRoots(source));


        while (!_gray.isEmpty()) {
            while (!_gray.isEmpty()) {

                Object node = _gray.getFirst();
                if (node instanceof SootClass) {
                    _processClass((SootClass)node);
                }
                else if (node instanceof SootMethod) {
                    if (((SootMethod)node).isDeclared()) {
                        _processMethod((SootMethod)node);
                    }
                    else {
                        _gray.removeFirst();
                        System.out.println(
                            "CallGraphPruner._growTree: "
                            + "Removed an undeclared method\n");
                    }
                }
                else if (node instanceof SootField) {
                    _processField((SootField)node);
                }
                else {
                    throw new RuntimeException("Invalid node type.");
                }
            }

            // We move this to an outer loop to prevent it from being
            // executed frequently.
            //_add(_getMethodsRequiredByInheritance(_reachableClasses,
            //        _reachableMethods));
        }

     }

    /** Figures out if a the targets of a method need to be computed.
     * Computation terminates at a "leaf" method. All native and
     * force-overridden methods are leaves.
     * @param method The method.
     * @return True if the method is a leaf.
     */
    protected boolean _isLeaf(SootMethod method) {
        return (method.isNative()
                || OverriddenMethodGenerator.isOverridden(method));
    }

    /** Performs the appropriate operations for the discovery of a new
     *  class.
     *  @param node The class.
     */
    protected void _processClass(SootClass node) {
        if (_reachableClasses.contains(node)) {
            return;
        }
        if (!OverriddenMethodGenerator.isOverridden(node)) {
            // Add the clinit method.
            if (node.declaresMethodByName("<clinit>")) {
                _add(node.getMethodByName("<clinit>"));
            }
            // Add all superClasses.
            SootClass superclass = node;
            while (superclass.hasSuperclass()
                    && !_reachableClasses.contains(superclass)) {
                superclass = superclass.getSuperclass();
                _add(superclass);
            }

        }

        _done(node);
    }

    /** Performs the appropriate operations for the discovery of a new
     * field.
     * @param field The field.
     */
    protected void _processField(SootField field) {
        if (_reachableFields.contains(field)) {
            return;
        }
        _add(AnalysisUtilities.classesRequiredBy(field));
        _done(field);
    }

    /** Performs the appropriate operations for the discovery of a new
     * method.
     * @param method The method.
     */
    protected void _processMethod(SootMethod method) {
        // If the method is in an undiscovered class, refresh the
        // invokeGraph.
        SootClass source = method.getDeclaringClass();
        int oldSize = _gray.size();

        // Care must be taken in what goes inside this if block. All trails
        // that terminate at leaf nodes must be inside it.
        if (!_isLeaf(method)) {
            // Add all methods shown by the local callGraph to be called
            // by this method.
            CallGraph callGraph = Scene.v().getCallGraph();
            Iterator outEdges = callGraph.edgesOutOf(method);
            while (outEdges.hasNext()) {
                Edge edge = (Edge)outEdges.next();
                SootMethod targetMethod = edge.tgt();
                _add(targetMethod);
            }

            // Add the nodes called in the body of the method.
            _add(_getNodesAccessedInBodyOf(method));

            // Add the locals declared in the body of this method.
            _add(AnalysisUtilities.getLocalTypeClasses(method));

            // Add all exceptions that can be caught by this method.
            if (method.isConcrete()
                    && !OverriddenMethodGenerator.isOverridden(method)) {
                Iterator traps = method.retrieveActiveBody().getTraps()
                    .iterator();
                while (traps.hasNext()){
                    Trap trap = (Trap)traps.next();
                    _add(trap.getException());
                }
            }
        }


        // Add all exception classes that can be thrown by this method.
        _add(method.getExceptions());

        // Add all arguments of this method.
        _add(AnalysisUtilities.getArgumentClasses(method));

        /*
          if (_gray.size() - oldSize >20) {
          System.out.println(method);
          }
        */
        // Remove the method from the queue.
        _done(method);
    }



    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////
    /** The list of all classes, methods, and fields discovered but not yet
     * processed.
     */
    protected LinkedList _gray = new LinkedList();

    /** The list of all reachable classes.
     */
    protected HashSet _reachableClasses = new HashSet();

    /** The list of all reachable fields.
     */
    protected HashSet _reachableFields = new HashSet();

    /** The list of all reachable methods.
     */
    protected HashSet _reachableMethods = new HashSet();



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Set all classes in the Scene as library classes. */
    private void _setAllClassesAsLibrary() {

        if (_verbose) {
            System.out.println(
                    "Setting all classes to library classes ...");
        }

        Iterator classes = Scene.v().getClasses().iterator();
        while (classes.hasNext()) {
            SootClass source = (SootClass)classes.next();
            source.setLibraryClass();
        }
    }

    /** Set all classes in the Scene that are not overridden as library
        classes.
      */
    private void _setUnOverriddenClassesAsLibrary() {
        if (_verbose) {
            System.out.println(
                    "Setting all un-overridden classes to library classes ...");
        }

        Iterator classes = Scene.v().getClasses().iterator();
        while (classes.hasNext()) {
            SootClass source = (SootClass)classes.next();
            if (OverriddenMethodGenerator.isOverridden(source)) {
                source.setLibraryClass();
            }
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** True if the "verbose" option is on. */
    private boolean _verbose;

}
