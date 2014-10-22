/* Constructor transformer.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.ast.transform;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ptolemy.backtrack.eclipse.ast.Type;
import ptolemy.backtrack.eclipse.ast.TypeAnalyzerState;

///////////////////////////////////////////////////////////////////
//// ConstructorTransformer

/**
 The constructor transformer to transform Java source programs into new
 programs that support backtracking.
 <p>
 When the type analyzer detects constructor declarations or a constructor
 invocations, it calls back methods in this class to further handle them.
 This transformer simply adds an extra method call to certain constructor
 invocations to modify the checkpoint objects of the newly created objects.
 <p>
 Normally, when a new object is created as an instance of a refactored class,
 its checkpoint object is default to an empty checkpoint object. The
 checkpoint object then monitors the newly created object only. When the
 extra call is placed on the newly created object, its checkpoint object is
 set to be the same as the object that issues the call.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ConstructorTransformer extends AbstractTransformer implements
        ConstructorHandler, ClassHandler, CrossAnalysisHandler,
        MethodDeclarationHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enter an anonymous class declaration.
     *
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(AnonymousClassDeclaration node, TypeAnalyzerState state) {
        _currentMethods.push(null);
    }

    /** Enter a field declaration.
     *
     *  @param node The AST node of the field declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(FieldDeclaration node, TypeAnalyzerState state) {
        _isStaticField.push(Modifier.isStatic(node.getModifiers()));
    }

    /** Enter a method declaration.
     *
     *  @param node The AST node of the method declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(MethodDeclaration node, TypeAnalyzerState state) {
        _currentMethods.push(node);
    }

    /** Enter a type declaration.
     *
     *  @param node The AST node of the type declaration.
     *  @param state The current state of the type analyzer.
     */
    public void enter(TypeDeclaration node, TypeAnalyzerState state) {
        _currentMethods.push(null);
    }

    /** Exit an anonymous class declaration.
     *
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(AnonymousClassDeclaration node, TypeAnalyzerState state) {
        _currentMethods.pop();
    }

    /** Exit a field declaration.
     *
     *  @param node The AST node of the field declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(FieldDeclaration node, TypeAnalyzerState state) {
        _isStaticField.pop();
    }

    /** Exit a method declaration.
     *
     *  @param node The AST node of the method declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(MethodDeclaration node, TypeAnalyzerState state) {
        _currentMethods.pop();
    }

    /** Exit a type declaration.
     *
     *  @param node The AST node of the type declaration.
     *  @param state The current state of the type analyzer.
     */
    public void exit(TypeDeclaration node, TypeAnalyzerState state) {
        _currentMethods.pop();
    }

    /** Handle a class instance creation. If the instance is not created in a
     *  method, it is not static, and the class that it belongs to is
     *  refactored, a new method call is added to it. The checkpoint object of
     *  the new instance is set to be the same as the object that creates it.
     *
     *  @param node The AST node of the class instance creation.
     *  @param state The current state of the type analyzer.
     */
    public void handle(ClassInstanceCreation node, TypeAnalyzerState state) {
        if (_currentMethods.peek() == null) {
            // Do not refactor static fields.
            if (_isStaticField.peek().booleanValue()) {
                return;
            }

            // Do not refactor class instance creations within methods.
            Type type = Type.getType(node);
            String typeName = type.getName();

            if (state.getCrossAnalyzedTypes().contains(typeName)
                    || (HANDLE_SPECIAL_TYPE_MAPPINGS && SPECIAL_TYPE_MAPPING
                            .containsKey(typeName))) {
                if (!state.getCrossAnalyzedTypes().contains(typeName)) {
                    state.getAnalyzer().addCrossAnalyzedType(typeName);
                }

                // The type needs to be cross-analyzed.
                _refactor(node, state);
            } else {
                addToLists(_unhandledNodes, typeName, node);
            }
        }
    }

    /** Handle a method declaration. Nothing is done in this method.
     *
     *  @param node The AST node of the method declaration.
     *  @param state The current state of the type analyzer.
     */
    public void handle(MethodDeclaration node, TypeAnalyzerState state) {
    }

    /** Handle a super constructor invocation. Nothing is done in this method.
     *
     *  @param node The AST node of the super constructor invocation.
     *  @param state The current state of the type analyzer.
     */
    public void handle(SuperConstructorInvocation node, TypeAnalyzerState state) {
    }

    /** Fix the refactoring result when the set of cross-analyzed types
     *  changes.
     *  <p>
     *  The set of cross-analyzed types defines the growing set of types that
     *  are analyzed in a run. Special care is taken for cross-analyzed types
     *  because they are monitored by checkpoint objects, and
     *  checkpoint-related extra methods are added to them. Unfortunately, it
     *  is not possible to know all the cross-analyzed types at the beginning.
     *  It is then necessary to fix the refactoring result when more
     *  cross-analyzed types are discovered later.
     *
     *  @param state The current state of the type analyzer.
     */
    public void handle(TypeAnalyzerState state) {
        Set crossAnalyzedTypes = state.getCrossAnalyzedTypes();
        Iterator crossAnalysisIter = crossAnalyzedTypes.iterator();

        while (crossAnalysisIter.hasNext()) {
            String typeName = (String) crossAnalysisIter.next();
            List<ASTNode> list = _unhandledNodes.get(typeName);

            if (list != null) {
                Iterator<ASTNode> nodesIter = list.iterator();

                while (nodesIter.hasNext()) {
                    ASTNode node = nodesIter.next();

                    if (node instanceof ClassInstanceCreation) {
                        _refactor((ClassInstanceCreation) node, state);
                    }

                    nodesIter.remove();
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public fields                       ////

    /** Whether to map special types to new types.
     *
     *  @see #SPECIAL_TYPE_MAPPING
     */
    public final static boolean HANDLE_SPECIAL_TYPE_MAPPINGS = true;

    /** Mapping from names of special types to the names of types used to
     *  substitute them. During type analysis, when an object is seen to be
     *  declared as an instance of a special type (such as
     *  <tt>java.util.Random</tt>), its declaring class is substituted to be
     *  the corresponding backtracking-enabled type (such as
     *  <tt>ptolemy.backtrack.util.java.util.Random</tt>).
     */
    public static final Hashtable<String, String> SPECIAL_TYPE_MAPPING = new Hashtable<String, String>();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Refactor a class instance creation node and add a method call to it.
     *  At run-time, this results in setting the checkpoint object of the new
     *  instance to be the same as the checkpoint object of the object that
     *  creates it.
     *
     *  @param node The AST node of the class instance creation.
     *  @param state The current state of the type analyzer.
     */
    private void _refactor(ClassInstanceCreation node, TypeAnalyzerState state) {
        AST ast = node.getAST();
        CompilationUnit root = (CompilationUnit) node.getRoot();
        Type type = Type.getType(node);
        ClassInstanceCreation newNode = (ClassInstanceCreation) ASTNode
                .copySubtree(ast, node);

        if (SPECIAL_TYPE_MAPPING.containsKey(type.getName())) {
            type = Type.createType(SPECIAL_TYPE_MAPPING.get(type.getName()));
            Name newName = createName(ast, getClassName(type.getName(), state,
                    root));
            newNode.setType(ast.newSimpleType(newName));
            Type.setType(node, type);
        }

        String setCheckpointName = SET_CHECKPOINT_NAME;
        MethodInvocation extraSetCheckpoint = ast.newMethodInvocation();
        extraSetCheckpoint.setExpression(newNode);
        extraSetCheckpoint.setName(ast.newSimpleName(setCheckpointName));
        extraSetCheckpoint.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));

        CastExpression typeCast = ast.newCastExpression();
        typeCast.setExpression(extraSetCheckpoint);
        typeCast.setType(createType(ast, getClassName(type.getName(), state,
                root)));
        replaceNode(node, typeCast);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The stack of currently entered method declarations. A <tt>null</tt>
     *  object is added to this stack when a type declaration or an anonymous
     *  class declaration is entered.
     */
    private Stack<MethodDeclaration> _currentMethods = new Stack<MethodDeclaration>();

    /** The stack of {@link Boolean}s on whether the methods entered are
     *  static.
     */
    private Stack<Boolean> _isStaticField = new Stack<Boolean>();

    /** The class instance creation nodes that have not been refactored. Keys
     *  are the types that they depend on; values are those nodes. When the
     *  type that a node depends on is added to the cross-analyzed set, the
     *  node must be refactored in {@link #handle(TypeAnalyzerState)}.
     */
    private Hashtable<String, List<ASTNode>> _unhandledNodes = new Hashtable<String, List<ASTNode>>();

    static {
        SPECIAL_TYPE_MAPPING.put("java.util.Random",
                "ptolemy.backtrack.util.java.util.Random");
        SPECIAL_TYPE_MAPPING.put("java.util.TreeMap",
                "ptolemy.backtrack.util.java.util.TreeMap");
    }
}
