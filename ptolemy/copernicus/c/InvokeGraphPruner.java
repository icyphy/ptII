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
import soot.jimple.FieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// InvokeGraphPruner
/**
Class that uses the Soot Framework to find out which methods/classes
are really needed for code generation.

@author Ankush Varma
@version $Id$
*/

public class InvokeGraphPruner {

    /** Dummy constructor to allow inheritance.*/
    public InvokeGraphPruner() {
    }

    /** Constructor. Creates an InvokeGraph and applies a specialized
     * pruning strategy to it, tailored for C code generation.
     * @param source The class to use as the root for the pruned tree.
     */
    public InvokeGraphPruner(SootClass source) {
        _verbose = Options.v().getBoolean("verbose");
        _cache = new InvokeGraphCache();
        if (!_cache.isPrecomputed()) {
            _setAllClassesAsLibrary();
            InvokeGraph invokeGraph = _generateNewInvokeGraph();
            Scene.v().setActiveInvokeGraph(invokeGraph);
            VariableTypeAnalysis vta = new VariableTypeAnalysis(invokeGraph);
            vta.trimActiveInvokeGraph();
            _cache.store(invokeGraph);
        }
        _cache.load();

        _generatePluginInvokeGraph();
        // All methods in the source, and no other methods, are required in
        // singleclass mode.
        if (!Options.v().get("compileMode").equals("singleClass")) {
            _setAllClassesAsLibrary();// To see inside method bodies.
            _growTree(source);
        }
        else {
            // singleClass mode.
            _reachableClasses = new HashSet();
            _reachableClasses.add(source);
            _reachableMethods = new HashSet();
            _reachableMethods.addAll(source.getMethods());
            _reachableFields.addAll(source.getFields());
        }
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

    /** Print how many classes and methods are there in the given
     * InvokeGraph, and how many reachable from the given SootMethod.
     *
     * @param invokeGraph The InvokeGraph.
     * @param method The method.
     */
    public void printStats(InvokeGraph invokeGraph
            , SootMethod method) {
        Iterator methods = invokeGraph.getReachableMethods().iterator();
        HashSet requiredClasses = new HashSet();
        while (methods.hasNext()) {
            requiredClasses.add(((SootMethod)methods.next())
                    .getDeclaringClass());
        }

        requiredClasses = new HashSet();
        methods = invokeGraph.getTransitiveTargetsOf(method).iterator();
        while (methods.hasNext()) {
            requiredClasses.add(((SootMethod)methods.next())
                    .getDeclaringClass());
        }

        System.out.println(method
                + " reaches "
                + invokeGraph.getTransitiveTargetsOf(method).size()
                + " methods in "
                + requiredClasses.size()
                + " classes.");
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
    //FIXME: Can be made more efficient by checking the type of node and
    //comparing only against the list of that type.
    protected void _add(Object node) {
        if ((!_gray.contains(node))
                && (!_reachableClasses.contains(node))
                && (!_reachableMethods.contains(node))
                && (!_reachableFields.contains(node))){
            _gray.addLast(node);
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
        _gray.addAll(_getRoots(source));

        while (!_gray.isEmpty()) {
            //System.out.println(_gray.size());
            Object node = _gray.getFirst();
            if (node instanceof SootClass) {
                _processClass((SootClass)node);
            }
            else if (node instanceof SootMethod) {
                _processMethod((SootMethod)node);
            }
            else if (node instanceof SootField) {
                _processField((SootField)node);
            }
            else {
                throw new RuntimeException("Invalid node type.");
            }
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

        _add(source);


        if (!_isLeaf(method)) {
            // Add all methods shown by the local invokeGraph to be called
            // by this method.
            InvokeGraph invokeGraph = Scene.v().getActiveInvokeGraph();
            Collection targets = invokeGraph.getTargetsOf(method);
            _add(targets);

            // Add all methods shown by the cached invokeGraph to be
            // targets of this method.
            if (_cache.isCached(method)) {
                targets = _cache.getTargetsOf(method);
                _add(targets);
            }
        }


        // Add the nodes called in the body of the method.
        _add(_getNodesAccessedInBodyOf(method));

        // Add all exception classes that can be thrown by this method.
        _add(method.getExceptions());
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

        // Add all arguments of this method.
        _add(AnalysisUtilities.getArgumentClasses(method));

        // Add the locals declared in the body of this method.
        _add(AnalysisUtilities.getLocalTypeClasses(method));

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

    /** Generate a new InvokeGraph.
     * @return The new InvokeGraph.
     */
    private InvokeGraph _generateNewInvokeGraph() {
        if (_verbose) {
            System.out.println("Computing new Invoke Graph ...");
        }
        InvokeGraph invokeGraph = ClassHierarchyAnalysis.newInvokeGraph();
        return invokeGraph;
    }

    /** Set The InvokeGraph of the Scene to the invokeGraph made of
     * non-precomputed classes only.
     */
    private void _generatePluginInvokeGraph() {
        _setUncachedClassesAsLibrary();
        InvokeGraph invokeGraph = _generateNewInvokeGraph();
        Scene.v().setActiveInvokeGraph(invokeGraph);
    }


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

    /** Set classes not in the cache as library classes */
    private void _setUncachedClassesAsLibrary() {
        if (_verbose) {
            System.out.println("Setting uncached classes to library classes ...");
        }

        Iterator classes = Scene.v().getClasses().iterator();
        while (classes.hasNext()) {
            SootClass source = (SootClass)classes.next();
            if (!_cache.isCached(source)) {
                source.setLibraryClass();
            }
        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    /** The cache where previuosly-computed invokeGraphs are stored. */
    private InvokeGraphCache _cache;

    /** True if the "verbose" option is on. */
    private boolean _verbose;

}
