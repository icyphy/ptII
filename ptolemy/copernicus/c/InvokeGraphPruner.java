/* Class that uses the Soot Framework to find out which methods/classes
   are really needed for code generation.

 Copyright (c) 2002 The University of Maryland.
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

@ProposedRating Red (<your email address>)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;

import soot.jimple.toolkits.invoke.*;

import soot.jimple.Stmt;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Collection;



//////////////////////////////////////////////////////////////////////////
//// InvokeGraphPruner
/**
Class that uses the Soot Framework to find out which methods/classes
are really needed for code generation.

@author Ankush Varma
@version $Id$
*/

public class InvokeGraphPruner {

    /** Constructor. Creates an InvokeGraph and applies a specialized
     * pruning strategy to it, tailored for C code generation.
     * @param source The class to use as the root for the pruned tree.
     */
   public InvokeGraphPruner(SootClass source) {
        InvokeGraph invokeGraph;
        Scene.v().setMainClass(source);
        source.setApplicationClass();

        // All methods in the source, and no other methods, are required in
        // singleclass mode.
        if (!Options.v().get("compileMode").equals("singleClass")) {
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


    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Adds a whole collection of classes/methods/fields to _gray.
     */
    private void _add(Collection nodes) {
        Iterator i = nodes.iterator();
        while (i.hasNext()){
            _add(i.next());
        }
    }

    /** Adds an object to the _gray list if the object is not in the gray
     * or a _reachable list. The object is assumed to be a class, method or
     * field. FIXME: Can be made more efficient by checking
     * the type of node and comparing only against the list of that type.
     */
    private void _add(Object node) {
        if ((!_gray.contains(node))
                && (!_reachableClasses.contains(node))
                && (!_reachableMethods.contains(node))
                && (!_reachableFields.contains(node))){
            _gray.addLast(node);
        }
    }

    /** Adds a class, method or field  to the appropriate reachable list.
     */
    private void _done(Object node) {
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
    private LinkedList _getCompulsoryNodes() {
        LinkedList compulsoryNodes = new LinkedList();

        // Add java.lang.String.String(char[]). Initializer
        SootClass source = Scene.v().getSootClass("java.lang.String");
        SootMethod method = source.getMethod("void <init>(char[])");
        compulsoryNodes.add(method);

        // Add java.lang.String.<clinit>.
        source = Scene.v().getSootClass("java.lang.String");
        method = source.getMethodByName("<clinit>");
        compulsoryNodes.add(method);

        // Add java.lang.System.initializeSystemClass()
        source = Scene.v().getSootClass("java.lang.System");
        method = source.getMethodByName("initializeSystemClass");
        compulsoryNodes.add(method);
        // System.out is required by initializeSystemClass
        SootField field = source.getFieldByName("out");
        compulsoryNodes.add(field);

        // Printstream is required by the force-overriden version of
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

    /** Sets the set of fields, methods and classes to be started off with.
     */
    private HashSet _getRoots(SootClass source) {
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
     */
    private void _growTree(SootClass source) {
        _gray.addAll(_getRoots(source));
        Scene.v().setActiveInvokeGraph(ClassHierarchyAnalysis
                .newInvokeGraph(true));

        while (!_gray.isEmpty()) {
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
    private boolean _isLeaf(SootMethod method) {
        return (method.isNative()
                || OverriddenMethodGenerator.isOverridden(method));
    }


    private void _processClass(SootClass node) {
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

            // Refresh the invokeGraph();
            node.setApplicationClass();
            Scene.v().setActiveInvokeGraph(ClassHierarchyAnalysis
                    .newInvokeGraph(true));

        }

        _done(node);
    }


    private void _processField(SootField field) {
        if (_reachableFields.contains(field)) {
            return;
        }
        _add(AnalysisUtilities.classesRequiredBy(field));
        _done(field);
    }

    private void _processMethod(SootMethod method) {
        // If the method is in an undiscovered class, refresh the
        // invokeGraph.
        SootClass source = method.getDeclaringClass();
        if (!_reachableClasses.contains(source)) {
            source.setApplicationClass();
            _add(source);
            Scene.v().setActiveInvokeGraph(ClassHierarchyAnalysis
                    .newInvokeGraph(true));
        }

        // Add all methods called by this this method directly.
        if (!_isLeaf(method)) {
            InvokeGraph invokeGraph = Scene.v().getActiveInvokeGraph();
            _add(invokeGraph.getTargetsOf(method));
        }

        // Add all fields called by this method.
        if (!OverriddenMethodGenerator.isOverridden(method)) {
            _add(AnalysisUtilities.getFieldsAccessedBy(method));
        }

        // Add all arguments of this method.
        _add(AnalysisUtilities.getArgumentClasses(method));

        // Remove the method from the queue.
        _done(method);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    /** The list of all classes, methods, and fields discovered but not yet
     * processed.
     */
    LinkedList _gray = new LinkedList();

    /** The list of all reachable classes.
     */
    private HashSet _reachableClasses = new HashSet();

    /** The list of all reachable fields.
     */
    private HashSet _reachableFields = new HashSet();

    /** The list of all reachable methods.
     */
    private HashSet _reachableMethods = new HashSet();
}
