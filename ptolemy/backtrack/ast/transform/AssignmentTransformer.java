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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.ast.Type;
import ptolemy.backtrack.ast.TypeAnalyzerState;
import ptolemy.backtrack.util.FieldRecord;

//////////////////////////////////////////////////////////////////////////
//// AssignmentTransformer
/**
 *  
 * 
 *  @author Thomas Feng
 *  @version $Id$
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Red (tfeng)
 */
public class AssignmentTransformer
        implements AssignmentHandler, ClassHandler {

    /**
     *  @param node
     *  @param state
     */
    public void handle(Assignment node, TypeAnalyzerState state) {
        AST ast = node.getAST();
        Expression leftHand = node.getLeftHandSide();
        Expression rightHand = node.getRightHandSide();
        Expression object = null;
        SimpleName name;
        
        if (leftHand instanceof FieldAccess) {
            object = ((FieldAccess)leftHand).getExpression();
            name = ((FieldAccess)leftHand).getName();

            Type type = Type.getType(object);
            if (!type.getName().equals(state.getCurrentClass().getName()))
                return;
        } else {
            name = (SimpleName)leftHand;
            // TODO: check variable.
        }
        
        MethodInvocation invocation = ast.newMethodInvocation();
        Expression newObject = (Expression)ASTNode.copySubtree(ast, object);
        invocation.setExpression(newObject);
        SimpleName newName = 
            ast.newSimpleName(ASSIGN_PREFIX + name.getIdentifier());
        invocation.setName(newName);
        Expression newRightHand = 
            (Expression)ASTNode.copySubtree(ast, rightHand);
        invocation.arguments().add(newRightHand);
        Type.propagateType(invocation, node);

        ASTNode parent = node.getParent();
        StructuralPropertyDescriptor location = node.getLocationInParent();
        if (location.isChildProperty())
            parent.setStructuralProperty(location, invocation);
        else {
            List properties = 
                (List)parent.getStructuralProperty(location);
            int position = properties.indexOf(node);
            properties.set(position, invocation);
        }
    }
    
    public void handle(AnonymousClassDeclaration node, 
            TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }
    
    public void handle(TypeDeclaration node, TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }
    
    public static String ASSIGN_PREFIX = "$ASSIGN$";
    
    public static String CHECKPOINT_NAME = "$CHECKPOINT";
    
    public static String RECORD_PREFIX = "$RECORD$";
    
    private void _handleDeclaration(ASTNode node, List bodyDeclarations, 
            TypeAnalyzerState state) {
        Class currentClass = state.getCurrentClass();
        List newDeclarations = new LinkedList();
        Iterator bodyIter = bodyDeclarations.iterator();
        while (bodyIter.hasNext()) {
            Object nextDeclaration = bodyIter.next();
            if (nextDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDecl = (FieldDeclaration)nextDeclaration;
                if (Modifier.isPrivate(fieldDecl.getModifiers())) {
                    AST ast = fieldDecl.getAST();
                    Type type = Type.getType(fieldDecl);
                    Iterator fragmentIter = fieldDecl.fragments().iterator();
                    while (fragmentIter.hasNext()) {
                        VariableDeclarationFragment fragment = 
                            (VariableDeclarationFragment)fragmentIter.next();
                        String fieldName = fragment.getName().getIdentifier();
                        newDeclarations.add(_createAssignMethod(ast, 
                                fieldName, type));
                        newDeclarations.add(_createFieldRecord(ast, fieldName));
                    }
                }
            }
        }
        bodyDeclarations.addAll(newDeclarations);
        
        AST ast = node.getAST();
        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(CHECKPOINT_NAME));
        FieldDeclaration checkpointField = ast.newFieldDeclaration(fragment);
        checkpointField.setType(ast.newSimpleType(
                _createName(ast, Checkpoint.class.getName())));
        checkpointField.setModifiers(Modifier.PRIVATE);
        bodyDeclarations.add(checkpointField);
    }
    
    private MethodDeclaration _createAssignMethod(AST ast, String fieldName, 
            Type fieldType) {
        MethodDeclaration method = ast.newMethodDeclaration();
        
        SimpleName name = ast.newSimpleName(ASSIGN_PREFIX + fieldName);
        method.setName(name);
        
        org.eclipse.jdt.core.dom.Type type = 
            _convertEclipseType(ast, fieldType.getName());
        method.setReturnType(type);
        
        SingleVariableDeclaration argument = 
            ast.newSingleVariableDeclaration();
        argument.setType(
                (org.eclipse.jdt.core.dom.Type)ASTNode.copySubtree(ast, type));
        argument.setName(ast.newSimpleName("newValue"));
        method.parameters().add(argument);
        
        Block body = _createAssignmentBlock(ast, fieldName, fieldType);
        method.setBody(body);
        
        method.setModifiers(Modifier.PRIVATE);
        
        return method;
    }
    
    private Block _createAssignmentBlock(AST ast, String fieldName, 
            Type fieldType) {
        Block block = ast.newBlock();
        
        IfStatement ifStatement = ast.newIfStatement();
        
        InfixExpression testExpression = ast.newInfixExpression();
        testExpression.setLeftOperand(ast.newSimpleName(CHECKPOINT_NAME));
        testExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
        testExpression.setRightOperand(ast.newNullLiteral());
        ifStatement.setExpression(testExpression);
        
        Block thenBranch = ast.newBlock();
        
        MethodInvocation timestampGetter = ast.newMethodInvocation();
        timestampGetter.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
        timestampGetter.setName(ast.newSimpleName("getTimestamp"));

        MethodInvocation recordInvocation = ast.newMethodInvocation();
        recordInvocation.setExpression(ast.newSimpleName(RECORD_PREFIX +
                fieldName));
        recordInvocation.setName(ast.newSimpleName("add"));
        recordInvocation.arguments().add(ast.newSimpleName(fieldName));
        recordInvocation.arguments().add(timestampGetter);
        ExpressionStatement recordStatement = 
            ast.newExpressionStatement(recordInvocation);
        thenBranch.statements().add(recordStatement);
        
        ifStatement.setThenStatement(thenBranch);
        block.statements().add(ifStatement);
        
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(ast.newSimpleName(fieldName));
        assignment.setRightHandSide(ast.newSimpleName("newValue"));
        assignment.setOperator(Assignment.Operator.ASSIGN);
        
        ExpressionStatement assignStatement = 
            ast.newExpressionStatement(assignment);
        block.statements().add(assignStatement);        
        
        return block;
    }
    
    private FieldDeclaration _createFieldRecord(AST ast, String fieldName) {
        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(RECORD_PREFIX + fieldName));
        
        ClassInstanceCreation initializer = ast.newClassInstanceCreation();
        initializer.setName(_createName(ast, FieldRecord.class.getName()));

        fragment.setInitializer(initializer);
        
        FieldDeclaration field = ast.newFieldDeclaration(fragment);
        field.setType(_convertEclipseType(ast, FieldRecord.class.getName()));
        field.setModifiers(Modifier.PRIVATE);
        
        return field;
    }
    
    private org.eclipse.jdt.core.dom.Type _convertEclipseType(AST ast, 
            String typeName) {
        org.eclipse.jdt.core.dom.Type eclipseType;
        if (Type.isPrimitive(typeName))
            eclipseType = 
                ast.newPrimitiveType(PrimitiveType.toCode(typeName));
        else
            eclipseType = ast.newSimpleType(_createName(ast, typeName));
        return eclipseType;
    }
    
    private Name _createName(AST ast, String name) {
        int pos = _indexOf(name, new char[]{'.', '$'}, 0);
        String subname = pos == -1 ? name : name.substring(0, pos);
        Name fullName = ast.newSimpleName(subname);
        while (pos != -1) {
            pos = _indexOf(name, new char[]{'.', '$'}, pos + 1);
            name = pos == -1 ? name : name.substring(0, pos);
            SimpleName simpleName = ast.newSimpleName(subname);
            fullName = ast.newQualifiedName(fullName, simpleName);
        }
        return fullName;
    }
    
    private int _indexOf(String s, char[] chars, int startPos) {
        int pos = -1;
        for (int i = 0; i < chars.length; i++) {
            int newPos = s.indexOf(chars[i], startPos);
            if (pos == -1 || newPos < pos)
                pos = newPos;
        }
        return pos;
    }
}
