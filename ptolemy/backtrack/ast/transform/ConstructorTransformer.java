/*

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.ast.transform;

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
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ptolemy.backtrack.ast.Type;
import ptolemy.backtrack.ast.TypeAnalyzerState;

//////////////////////////////////////////////////////////////////////////
//// ConstructorTransformer
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ConstructorTransformer extends AbstractTransformer
        implements ConstructorHandler, ClassHandler, CrossAnalysisHandler,
        MethodDeclarationHandler {

    public void enter(FieldDeclaration node, TypeAnalyzerState state) {
        _isStaticField.push(new Boolean(Modifier.isStatic(node.getModifiers())));
    }

    public void exit(FieldDeclaration node, TypeAnalyzerState state) {
        _isStaticField.pop();
    }

    public void enter(MethodDeclaration node, TypeAnalyzerState state) {
        _currentMethods.push(node);
    }

    public void exit(MethodDeclaration node, TypeAnalyzerState state) {
        _currentMethods.pop();
    }

    /**
     *  @param node
     *  @param state
     */
    public void handle(MethodDeclaration node, TypeAnalyzerState state) {
    }

    /**
     *  @param node
     *  @param state
     */
    public void handle(ClassInstanceCreation node, TypeAnalyzerState state) {
        if (_currentMethods.peek() == null) {
            // Do not refactor static fields.
            if (((Boolean)_isStaticField.peek()).booleanValue())
                return;

            // Do not refactor class instance creations within methods.
            Type type = Type.getType(node);
            String typeName = type.getName();
            if (state.getCrossAnalyzedTypes().contains(typeName) ||
                    (HANDLE_SPECIAL_TYPE_MAPPINGS &&
                            SPECIAL_TYPE_MAPPING.containsKey(typeName))) {
                if (!state.getCrossAnalyzedTypes().contains(typeName))
                    state.getAnalyzer().addCrossAnalyzedType(typeName);

                // The type needs to be cross-analyzed.
                _refactor(node, state);
            } else
                addToLists(_unhandledNodes, typeName, node);
        }
    }

    public void handle(SuperConstructorInvocation node, TypeAnalyzerState state) {
    }

    public void enter(AnonymousClassDeclaration node,
            TypeAnalyzerState state) {
        _currentMethods.push(null);
    }

    public void exit(AnonymousClassDeclaration node,
            TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
        _currentMethods.pop();
    }

    public void enter(TypeDeclaration node, TypeAnalyzerState state) {
        _currentMethods.push(null);
    }

    public void exit(TypeDeclaration node, TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
        _currentMethods.pop();
    }

    public void handle(TypeAnalyzerState state) {
        Set crossAnalyzedTypes = state.getCrossAnalyzedTypes();
        Iterator crossAnalysisIter = crossAnalyzedTypes.iterator();
        while (crossAnalysisIter.hasNext()) {
            String typeName = (String)crossAnalysisIter.next();
            List list = (List)_unhandledNodes.get(typeName);
            if (list != null) {
                Iterator nodesIter = list.iterator();
                while (nodesIter.hasNext()) {
                    ASTNode node = (ASTNode)nodesIter.next();
                    if (node instanceof ClassInstanceCreation)
                        _refactor((ClassInstanceCreation)node, state);
                    nodesIter.remove();
                }
            }
        }
    }

    public final static boolean HANDLE_SPECIAL_TYPE_MAPPINGS = true;

    public static final Hashtable SPECIAL_TYPE_MAPPING = new Hashtable();

    static {
        SPECIAL_TYPE_MAPPING.put("java.util.Random",
                "ptolemy.backtrack.util.java.util.Random");
        SPECIAL_TYPE_MAPPING.put("java.util.TreeMap",
                "ptolemy.backtrack.util.java.util.TreeMap");
    }

    private void _handleDeclaration(ASTNode node, List bodyDeclarations,
            TypeAnalyzerState state) {
    }

    private void _refactor(ClassInstanceCreation node,
            TypeAnalyzerState state) {
        AST ast = node.getAST();
        CompilationUnit root = (CompilationUnit)node.getRoot();
        Type type = Type.getType(node);
        ClassInstanceCreation newNode =
            (ClassInstanceCreation)ASTNode.copySubtree(ast, node);

        if (SPECIAL_TYPE_MAPPING.containsKey(type.getName())) {
            type = Type.createType((String)SPECIAL_TYPE_MAPPING.get(type.getName()));
            newNode.setName(createName(ast,
                    getClassName(type.getName(), state, root)));
            Type.setType(node, type);
        }

        String setCheckpointName = SET_CHECKPOINT_NAME;
        MethodInvocation extraSetCheckpoint = ast.newMethodInvocation();
        extraSetCheckpoint.setExpression(newNode);
        extraSetCheckpoint.setName(ast.newSimpleName(setCheckpointName));
        extraSetCheckpoint.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));

        CastExpression typeCast = ast.newCastExpression();
        typeCast.setExpression(extraSetCheckpoint);
        typeCast.setType(
                createType(ast, getClassName(type.getName(), state, root)));
        replaceNode(node, typeCast);
    }

    private Hashtable _unhandledNodes = new Hashtable();

    private Stack _currentMethods = new Stack();

    private Stack _isStaticField = new Stack();
}
