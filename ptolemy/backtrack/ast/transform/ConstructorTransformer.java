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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ptolemy.backtrack.Checkpoint;
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
        ASTNode classDeclaration = node.getParent();
        AST ast = node.getAST();
        CompilationUnit root = (CompilationUnit)node.getRoot();
        
        MethodDeclaration newConstructor = 
            ast.newMethodDeclaration();
        newConstructor.setConstructor(true);
        newConstructor.setModifiers(node.getModifiers());
        newConstructor.setName(
                ast.newSimpleName(node.getName().getIdentifier()));
        
        // Copy all the parameters.
        Iterator parameters = node.parameters().iterator();
        List oldParameters = new LinkedList();
        while (parameters.hasNext()) {
            SingleVariableDeclaration parameter = 
                (SingleVariableDeclaration)parameters.next();
            
            // If a checkpoint parameter already exists, do not handle this
            // constructor.
            if (parameter.getType().toString().equals(
                    Checkpoint.class.getName()))
                return;
            
            newConstructor.parameters().add(
                    ASTNode.copySubtree(ast, parameter));
            oldParameters.add(
                    ast.newSimpleName(parameter.getName().getIdentifier()));
        }
        
        // Add a checkpoint parameter.
        SingleVariableDeclaration checkpoint = 
            ast.newSingleVariableDeclaration();
        String checkpointType = _getClassName(Checkpoint.class, state, root);
        checkpoint.setType(_createType(ast, checkpointType));
        checkpoint.setName(ast.newSimpleName(CHECKPOINT_NAME));
        newConstructor.parameters().add(checkpoint);
        
        // The first statement: call the old constructor.
        ConstructorInvocation invocation = ast.newConstructorInvocation();
        invocation.arguments().addAll(oldParameters);
        
        // The second statement: set the checkpoint.
        MethodInvocation setCheckpoint = ast.newMethodInvocation();
        setCheckpoint.setName(ast.newSimpleName(SET_CHECKPOINT_NAME));
        setCheckpoint.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));
        ExpressionStatement setCheckpointExpression = 
            ast.newExpressionStatement(setCheckpoint);
        
        // Create the body.
        Block body = ast.newBlock();
        body.statements().add(invocation);
        body.statements().add(setCheckpointExpression);
        newConstructor.setBody(body);
        
        // Record the new constructor to be added.
        String currentClassName = state.getCurrentClass().getName();
        List constructorList = (List)_newConstructors.get(currentClassName);
        if (constructorList == null) {
            constructorList = new LinkedList();
            _newConstructors.put(currentClassName, constructorList);
        }
        constructorList.add(newConstructor);
    }

    /**
     *  @param node
     *  @param state
     */
    public void handle(ClassInstanceCreation node, TypeAnalyzerState state) {
        if (_currentMethods.peek() == null) {
            // Do not refactor class instance creations within methods.
            Type type = Type.getType(node);
            String typeName = type.getName();
            if (state.getCrossAnalyzedTypes().contains(typeName))
                // The type needs to be cross-analyzed.
                _refactor(node);
            else
                _addToLists(_unhandledNodes, typeName, node);
        }
    }
    
    public void handle(SuperConstructorInvocation node, TypeAnalyzerState state) {
        /*String superName = state.getCurrentClass().getSuperclass().getName();
        if (state.getCrossAnalyzedTypes().contains(superName))
            // The type needs to be cross-analyzed.
            _refactor(node);
        else
            _addToLists(_unhandledNodes, superName, node);*/
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
    
    private void _handleDeclaration(ASTNode node, List bodyDeclarations, 
            TypeAnalyzerState state) {
        String currentClassName = state.getCurrentClass().getName();
        List constructorList = (List)_newConstructors.get(currentClassName);
        if (constructorList == null && node instanceof TypeDeclaration) {
            // For a class without a constructor, create a dummy constructor.
            AST ast = node.getAST();
            MethodDeclaration constructor = ast.newMethodDeclaration();
            constructor.setConstructor(true);
            constructor.setName(ast.newSimpleName(
                    ((TypeDeclaration)node).getName().getIdentifier()));
            constructor.setBody(ast.newBlock());
            constructor.setModifiers(Modifier.PUBLIC);
            bodyDeclarations.add(constructor);
            handle(constructor, state);
            constructorList = (List)_newConstructors.get(currentClassName);
        }
        if (constructorList != null) {
            _newConstructors.remove(currentClassName);
            bodyDeclarations.addAll(constructorList);
        }
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
                        _refactor((ClassInstanceCreation)node);
                    else if (node instanceof SuperConstructorInvocation)
                        _refactor((SuperConstructorInvocation)node);
                    nodesIter.remove();
                }
            }
        }
    }
    
    private void _refactor(ClassInstanceCreation node) {
        AST ast = node.getAST();
        node.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));
    }
    
    private void _refactor(SuperConstructorInvocation node) {
        AST ast = node.getAST();
        node.arguments().add(ast.newSimpleName(CHECKPOINT_NAME));
    }
    
    private Hashtable _newConstructors = new Hashtable();
    
    private Hashtable _unhandledNodes = new Hashtable();
    
    private Stack _currentMethods = new Stack();
}
