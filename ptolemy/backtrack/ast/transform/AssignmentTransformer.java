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

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
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
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.ast.ASTClassNotFoundException;
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
 *  @since Ptolemy II 5.1
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
        while (leftHand instanceof ParenthesizedExpression)
            leftHand = ((ParenthesizedExpression)leftHand).getExpression();
        Expression rightHand = node.getRightHandSide();
        Expression newObject = null;
        SimpleName name;
        List indices = new LinkedList();
        
        while (leftHand instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess)leftHand;
            indices.add(0, ASTNode.copySubtree(ast, arrayAccess.getIndex()));
            leftHand = arrayAccess.getArray();
            while (leftHand instanceof ParenthesizedExpression)
                leftHand = 
                    ((ParenthesizedExpression)leftHand).getExpression();
        }
        
        if (leftHand instanceof FieldAccess) {
            Expression object = ((FieldAccess)leftHand).getExpression();
            name = ((FieldAccess)leftHand).getName();

            Type type = Type.getType(object);
            if (!type.getName().equals(state.getCurrentClass().getName()))
                return;
            
            newObject = (Expression)ASTNode.copySubtree(ast, object);
        } else if (leftHand instanceof SimpleName)
            name = (SimpleName)leftHand;
        else
            return; // Some unknown situation.
        
        Type owner = Type.getOwner(leftHand);
        Class ownerClass;
        boolean isStatic;
        if (owner == null)  // Not a field.
            return;
        else {
            try {
                ownerClass = owner.toClass(state.getClassLoader());
                Field field = 
                    ownerClass.getDeclaredField(name.getIdentifier());
                int modifiers = field.getModifiers();
                if (!java.lang.reflect.Modifier.isPrivate(modifiers))
                    return; // Not handling non-private fields.
                isStatic = 
                    java.lang.reflect.Modifier.isStatic(modifiers);
            } catch (ClassNotFoundException e) {
                throw new ASTClassNotFoundException(owner.getName());
            } catch (NoSuchFieldException e) {
                // The field is not defined in this class.
                return;
            }
        }
        if (isStatic && !HANDLE_STATIC_FIELDS)
            return;
        
        MethodInvocation invocation = ast.newMethodInvocation();
        
        if (newObject != null)
            invocation.setExpression(newObject);
        SimpleName newName = 
            ast.newSimpleName(_getAssignMethodName(name.getIdentifier()));
        invocation.setName(newName);
        Expression newRightHand = 
            (Expression)ASTNode.copySubtree(ast, rightHand);
        if (isStatic)
            invocation.arguments().add(ast.newSimpleName("$CHECKPOINT"));
        
        Iterator indicesIter = indices.iterator();
        while (indicesIter.hasNext())
            invocation.arguments().add((Expression)indicesIter.next());
        
        invocation.arguments().add(newRightHand);
        Type.propagateType(invocation, node);
        _replaceNode(node, invocation);
        
        _recordAccessedField(owner.getName(), name.getIdentifier(), 
                indices.size());
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
    
    public static boolean OPTIMIZE_CALL = true;
    
    public static boolean HANDLE_STATIC_FIELDS = true;
    
    private MethodDeclaration _createAssignMethod(Class currentClass, AST ast, 
            String fieldName, Type fieldType, int indices, boolean isStatic, 
            ClassLoader loader) {
        
        String methodName = _getAssignMethodName(fieldName);
        if (_isMethodDuplicated(currentClass, methodName, fieldType, 
                indices, isStatic, loader))
            throw new ASTDuplicatedMethodException(currentClass.getName(), 
                    methodName);
        
        MethodDeclaration method = ast.newMethodDeclaration();
        
        int dimensions = fieldType.dimensions();
        for (int i = 0; i < indices; i++)
            try {
                fieldType = fieldType.removeOneDimension();
            } catch (ClassNotFoundException e) {
                throw new ASTClassNotFoundException(fieldType);
            }
        
        SimpleName name = ast.newSimpleName(methodName);
        method.setName(name);
        
        org.eclipse.jdt.core.dom.Type type = 
            _createType(ast, fieldType.getName());
        method.setReturnType(type);
        
        if (isStatic) {
            // Add a "$CHECKPOINT" argument.
            SingleVariableDeclaration checkpoint = 
                ast.newSingleVariableDeclaration();
            checkpoint.setType(ast.newSimpleType(
                    _createName(ast, Checkpoint.class.getName())));
            checkpoint.setName(ast.newSimpleName(CHECKPOINT_NAME));
            method.parameters().add(checkpoint);
        }
        
        for (int i = 0; i < indices; i++) {
            SingleVariableDeclaration index = 
                ast.newSingleVariableDeclaration();
            index.setType(ast.newPrimitiveType(PrimitiveType.INT));
            index.setName(ast.newSimpleName("index" + i));
            method.parameters().add(index);
        }
        
        SingleVariableDeclaration argument = 
            ast.newSingleVariableDeclaration();
        argument.setType(
                (org.eclipse.jdt.core.dom.Type)ASTNode.copySubtree(ast, type));
        argument.setName(ast.newSimpleName("newValue"));
        method.parameters().add(argument);
        
        Block body = _createAssignmentBlock(ast, currentClass.getName(), 
                fieldName, fieldType, indices, dimensions);
        method.setBody(body);
        
        int modifiers = Modifier.PRIVATE | Modifier.FINAL;
        if (isStatic)
            modifiers |= Modifier.STATIC;
        method.setModifiers(modifiers);
        
        return method;
    }
    
    private Block _createAssignmentBlock(AST ast, String className, 
            String variableName, Type fieldType, int indices, int dimensions) {
        Block block = ast.newBlock();
        
        Expression field  = ast.newSimpleName(variableName);
        if (indices > 0) {
            for (int i = 0; i < indices; i++) {
                ArrayAccess arrayAccess = ast.newArrayAccess();
                arrayAccess.setArray(field);
                arrayAccess.setIndex(ast.newSimpleName("index" + i));
                field = arrayAccess;
            }
        }
        
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

        // Method call to store old value.
        MethodInvocation recordInvocation = ast.newMethodInvocation();
        recordInvocation.setExpression(
                ast.newSimpleName(_getRecordName(variableName)));
        recordInvocation.setName(ast.newSimpleName("add"));

        if (indices == 0)
            recordInvocation.arguments().add(ast.newNullLiteral());
        else {
            ArrayCreation arrayCreation = ast.newArrayCreation();
            ArrayType arrayType = 
                ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT));
            ArrayInitializer initializer = ast.newArrayInitializer();
            for (int i = 0; i < indices; i++)
                initializer.expressions().add(
                        ast.newSimpleName("index" + i));
            arrayCreation.setType(arrayType);
            arrayCreation.setInitializer(initializer);
            recordInvocation.arguments().add(arrayCreation);
        }
        recordInvocation.arguments().add(field);
        recordInvocation.arguments().add(timestampGetter);
        ExpressionStatement recordStatement = 
            ast.newExpressionStatement(recordInvocation);
        thenBranch.statements().add(recordStatement);
        
        ifStatement.setThenStatement(thenBranch);
        block.statements().add(ifStatement);
        
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(
                (Expression)ASTNode.copySubtree(ast, field));
        assignment.setRightHandSide(ast.newSimpleName("newValue"));
        assignment.setOperator(Assignment.Operator.ASSIGN);
        
        ReturnStatement returnStatement = ast.newReturnStatement();
        returnStatement.setExpression(assignment);
        block.statements().add(returnStatement);
        
        return block;
    }
    
    private FieldDeclaration _createFieldRecord(Class currentClass, AST ast, 
            String fieldName, boolean isStatic) {
        String recordName = _getRecordName(fieldName);
        if (_isFieldDuplicated(currentClass, recordName))
            throw new ASTDuplicatedFieldException(currentClass.getName(), 
                    recordName);
        
        String typeName = FieldRecord.class.getName();

        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(recordName));
        
        ClassInstanceCreation initializer = ast.newClassInstanceCreation();
        initializer.setName(_createName(ast, typeName));

        fragment.setInitializer(initializer);
        
        FieldDeclaration field = ast.newFieldDeclaration(fragment);
        field.setType(_createType(ast, typeName));
        
        int modifiers = Modifier.PRIVATE;
        if (isStatic)
            modifiers |= Modifier.STATIC;
        field.setModifiers(modifiers);
        
        return field;
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
    
    private org.eclipse.jdt.core.dom.Type _createType(AST ast, String type) {
        String elementName = Type.getElementType(type);
        int dimensions = Type.dimensions(type);
        
        org.eclipse.jdt.core.dom.Type elementType;
        if (Type.isPrimitive(elementName))
            elementType = 
                ast.newPrimitiveType(PrimitiveType.toCode(elementName));
        else {
            Name element = _createName(ast, elementName);
            elementType = ast.newSimpleType(element);
        }
        
        org.eclipse.jdt.core.dom.Type returnType = elementType;
        for (int i = 1; i < dimensions; i++)
            returnType = ast.newArrayType(returnType);
        
        return returnType;
    }
    
    private List _getAccessedField(String className, String fieldName) {
        Hashtable classTable = (Hashtable)_accessedFields.get(className);
        if (classTable == null)
            return null;
        List indicesList = (List)classTable.get(fieldName);
        return indicesList;
    }
    
    private String _getAssignMethodName(String fieldName) {
        return ASSIGN_PREFIX + fieldName;
    }
    
    private String _getRecordName(String fieldName) {
        return RECORD_PREFIX + fieldName;
    }
    
    private void _handleDeclaration(ASTNode node, List bodyDeclarations, 
            TypeAnalyzerState state) {
        Class currentClass = state.getCurrentClass();
        List newMethods = new LinkedList();
        List newFields = new LinkedList();
        Iterator bodyIter = bodyDeclarations.iterator();
        while (bodyIter.hasNext()) {
            Object nextDeclaration = bodyIter.next();
            if (nextDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDecl = (FieldDeclaration)nextDeclaration;
                boolean isStatic = Modifier.isStatic(fieldDecl.getModifiers());
                
                if (isStatic && HANDLE_STATIC_FIELDS != true)
                    continue;
                
                if (Modifier.isPrivate(fieldDecl.getModifiers())) {
                    AST ast = fieldDecl.getAST();
                    Type type = Type.getType(fieldDecl);
                    Iterator fragmentIter = fieldDecl.fragments().iterator();
                    while (fragmentIter.hasNext()) {
                        VariableDeclarationFragment fragment = 
                            (VariableDeclarationFragment)fragmentIter.next();
                        String fieldName = fragment.getName().getIdentifier();

                        List indicesList = 
                            _getAccessedField(currentClass.getName(), 
                                    fieldName);
                        if (indicesList == null)
                            continue;
                        
                        Iterator indicesIter = indicesList.iterator();
                        while (indicesIter.hasNext()) {
                            int indices = 
                                ((Integer)indicesIter.next()).intValue();
                            
                            newMethods.add(_createAssignMethod(currentClass, 
                                    ast, fieldName, type, indices, isStatic, 
                                    state.getClassLoader()));
                        }
                        newFields.add(_createFieldRecord(currentClass, ast, 
                                fieldName, isStatic));
                    }
                }
            }
        }
        bodyDeclarations.addAll(newMethods);
        bodyDeclarations.addAll(newFields);
        
        AST ast = node.getAST();
        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(CHECKPOINT_NAME));
        FieldDeclaration checkpointField = ast.newFieldDeclaration(fragment);
        checkpointField.setType(_createType(ast, Checkpoint.class.getName()));
        checkpointField.setModifiers(Modifier.PRIVATE);
        bodyDeclarations.add(checkpointField);
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
    
    private boolean _isFieldDuplicated(Class c, String fieldName) {
        // Does NOT check fields inherited from interfaces.
        try {
            c.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
    
    private boolean _isMethodDuplicated(Class c, String methodName, 
            Type fieldType, int indices, boolean isStatic, 
            ClassLoader loader) {
        try {
            for (int i = 0; i < indices; i++)
                fieldType = fieldType.removeOneDimension();
            int nArguments = indices + 1;
            if (isStatic)
                nArguments++;
            Class[] arguments = new Class[nArguments];
            int start = 0;
            if (isStatic)
                arguments[start++] = Checkpoint.class;
            for (int i = start; i < nArguments - 1; i++)
                arguments[i] = int.class;
            arguments[nArguments - 1] = fieldType.toClass(loader);
            try {
                c.getDeclaredMethod(methodName, arguments);
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            throw new ASTClassNotFoundException(fieldType);
        }
    }
    
    private void _replaceNode(ASTNode node, ASTNode newNode) {
        ASTNode parent = node.getParent();
        StructuralPropertyDescriptor location = node.getLocationInParent();
        if (location.isChildProperty())
            parent.setStructuralProperty(location, newNode);
        else {
            List properties = 
                (List)parent.getStructuralProperty(location);
            int position = properties.indexOf(node);
            properties.set(position, newNode);
        }
    }
    
    private void _recordAccessedField(String className, String fieldName, 
            int indices) {
        Hashtable classTable = (Hashtable)_accessedFields.get(className);
        if (classTable == null) {
            classTable = new Hashtable();
            _accessedFields.put(className, classTable);
        }
        List indicesList = (List)classTable.get(fieldName);
        if (indicesList == null) {
            indicesList = new LinkedList();
            classTable.put(fieldName, indicesList);
        }
        Integer iIndices = new Integer(indices);
        if (!indicesList.contains(iIndices))
            indicesList.add(iIndices);
    }
    
    private Hashtable _accessedFields = new Hashtable();
}
