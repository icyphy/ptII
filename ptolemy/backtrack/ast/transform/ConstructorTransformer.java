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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ptolemy.backtrack.Checkpoint;
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
        implements ConstructorHandler, ClassHandler, CrossAnalysisHandler {

    /**
     *  @param node
     *  @param state
     */
    public void handle(MethodDeclaration node, TypeAnalyzerState state) {
        ASTNode classDeclaration = node.getParent();
        AST ast = node.getAST();
        
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
        checkpoint.setType(_createType(ast, Checkpoint.class.getName()));
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
        List constructorList = (List)newConstructors.get(currentClassName);
        if (constructorList == null) {
            constructorList = new LinkedList();
            newConstructors.put(currentClassName, constructorList);
        }
        constructorList.add(newConstructor);
    }

    /**
     *  @param node
     *  @param state
     */
    public void handle(ClassInstanceCreation node, TypeAnalyzerState state) {
        
    }

    public void handle(AnonymousClassDeclaration node, 
            TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }
    
    public void handle(TypeDeclaration node, TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }
    
    private void _handleDeclaration(ASTNode node, List bodyDeclarations, 
            TypeAnalyzerState state) {
        String currentClassName = state.getCurrentClass().getName();
        List constructorList = (List)newConstructors.get(currentClassName);
        if (constructorList == null && node instanceof TypeDeclaration) {
            // For a class without a constructor, create a dummy constructor.
            AST ast = node.getAST();
            MethodDeclaration constructor = ast.newMethodDeclaration();
            constructor.setConstructor(true);
            constructor.setName(ast.newSimpleName(
                    ((TypeDeclaration)node).getName().getIdentifier()));
            constructor.setBody(ast.newBlock());
            bodyDeclarations.add(constructor);
            handle(constructor, state);
            constructorList = (List)newConstructors.get(currentClassName);
        }
        if (constructorList != null) {
            newConstructors.remove(currentClassName);
            bodyDeclarations.addAll(constructorList);
        }
    }
    
    public void handle(TypeAnalyzerState state) {
        
    }
    
    private Hashtable newConstructors = new Hashtable();
}
