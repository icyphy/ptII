/* Assignment transformer.

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
   The assignment transformer to transform Java source programs into new
   programs that support backtracking.
   <p>
   In this scheme, each assignment to a <em>state variable</em> is
   refactored to become a method call, which, before actually assigning
   the new value, back up the old value of the field in a record (see
   {@link FieldRecord}).
  
   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class AssignmentTransformer
        implements AssignmentHandler, ClassHandler {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Handle an assignment, and refactor it to be a special method call if the
     *  left-hand side of the assignment is a state variable. The accessed field
     *  is also recorded in a list so that extra private methods used to back up
     *  fields are only created for those used fields. If the field on the
     *  left-hand side is a multi-dimensional array, access with different
     *  numbers of indices is recorded with multiple entries. For each of those
     *  entries, an extra method is created for each different number of
     *  indices.
     * 
     *  @param node The assignment to be refactored.
     *  @param state The current state of the type analyzer.
     */
    public void handle(Assignment node, TypeAnalyzerState state) {
        AST ast = node.getAST();
        
        // Get the left-hand side.
        Expression leftHand = node.getLeftHandSide();
        while (leftHand instanceof ParenthesizedExpression)
            leftHand = ((ParenthesizedExpression)leftHand).getExpression();
        
        // Get the right-hand side.
        Expression rightHand = node.getRightHandSide();
        
        // If left-hand side is an array access, store the indices.
        List indices = new LinkedList();
        while (leftHand instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess)leftHand;
            indices.add(0, ASTNode.copySubtree(ast, arrayAccess.getIndex()));
            leftHand = arrayAccess.getArray();
            while (leftHand instanceof ParenthesizedExpression)
                leftHand = 
                    ((ParenthesizedExpression)leftHand).getExpression();
        }
        
        // For expression.name on the left-hand side, set newObject to be the
        // expression and name to be the name. newObject may be null.
        Expression newObject = null;
        SimpleName name;
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
        
        // Get the owner of the left-hand side, if it is a field.
        Type owner = Type.getOwner(leftHand);
        if (owner == null)  // Not a field.
            return;

        // Get the class of the owner and test the modifiers of the field.
        Class ownerClass;
        boolean isStatic;
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
        if (isStatic && !HANDLE_STATIC_FIELDS)
            return;
        
        // The new method invocation to replace the assignment.
        MethodInvocation invocation = ast.newMethodInvocation();

        // Set the expression and name of the method invocation.
        if (newObject != null)
            invocation.setExpression(newObject);
        SimpleName newName = 
            ast.newSimpleName(_getAssignMethodName(name.getIdentifier()));
        invocation.setName(newName);
        
        // If the field is static, add the checkpoint object as the first
        // argument.
        if (isStatic)
            invocation.arguments().add(ast.newSimpleName("$CHECKPOINT"));
        
        // Add all the indices into the argument list.
        invocation.arguments().addAll(indices);
        
        // Add the right-hand side expression to the argument list.
        invocation.arguments().add(ASTNode.copySubtree(ast, rightHand));
        
        // Set the type of this invocation.
        Type.propagateType(invocation, node);
        
        // Replace the assignment node with this method invocation.
        _replaceNode(node, invocation);
        
        // Record the field access (a corresponding method will be generated
        // later.
        _recordAccessedField(owner.getName(), name.getIdentifier(), 
                indices.size());
    }
    
    /** Handle an anonymous class declaration, and add extra methods and fields
     *  to it.
     *  <p>
     *  This function is called after all the existing methods and fields in
     *  the same class has been visited, and all the field assignments in it
     *  are handled with {@link #handle(Assignment, TypeAnalyzerState)}.
     * 
     *  @param node
     *  @param state
     */
    public void handle(AnonymousClassDeclaration node, 
            TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }
    
    /** Handle a class declaration, and add extra methods and fields to it.
     *  <p>
     *  This function is called after all the existing methods and fields in
     *  the same class has been visited, and all the field assignments in it
     *  are handled with {@link #handle(Assignment, TypeAnalyzerState)}.
     * 
     *  @param node
     *  @param state
     */
    public void handle(TypeDeclaration node, TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public fields                       ////

    /** The prefix of assignment methors.
     */
    public static String ASSIGN_PREFIX = "$ASSIGN$";
    
    /** The name of the checkpoint object.
     */
    public static String CHECKPOINT_NAME = "$CHECKPOINT";
    
    /** Whether to refactor private static fields.
     */
    public static boolean HANDLE_STATIC_FIELDS = true;
    
    /** Whether to optimize method calls. (Not implemented yet.)
     */
    public static boolean OPTIMIZE_CALL = true;
    
    /** The prefix of records (new fields to be added to a class).
     */
    public static String RECORD_PREFIX = "$RECORD$";
    
    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    /** Create assignment methods for each access field that has been
     *  recorded. If a field is a multi-dimensional array, access with
     *  different numbers of indices is recorded with multiple entries.
     *  For each of those entries, an extra method is created for each
     *  different number of indices.
     * 
     *  @param currentClass The current class.
     *  @param ast The {@link AST} object.
     *  @param fieldName The name of the field to be handled.
     *  @param fieldType The type of the field to be handled.
     *  @param indices The number of indices.
     *  @param isStatic Whether the field is static.
     *  @param loader The class loader.
     *  @return The declaration of the method that handles assignment to
     *   the field.
     */
    private MethodDeclaration _createAssignMethod(Class currentClass, AST ast, 
            String fieldName, Type fieldType, int indices, boolean isStatic, 
            ClassLoader loader) {
        
        MethodDeclaration method = ast.newMethodDeclaration();
        String methodName = _getAssignMethodName(fieldName);
        
        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (_isMethodDuplicated(currentClass, methodName, fieldType, 
                indices, isStatic, loader))
            throw new ASTDuplicatedMethodException(currentClass.getName(), 
                    methodName);
        
        // Get the type of the new value. The return value has the same
        // type. 
        int dimensions = fieldType.dimensions();
        for (int i = 0; i < indices; i++)
            try {
                fieldType = fieldType.removeOneDimension();
            } catch (ClassNotFoundException e) {
                throw new ASTClassNotFoundException(fieldType);
            }
        
        // Set the name and return type.
        SimpleName name = ast.newSimpleName(methodName);
        method.setName(name);
        org.eclipse.jdt.core.dom.Type type = 
            _createType(ast, fieldType.getName());
        method.setReturnType(_createType(ast, fieldType.getName()));
        
        // If the field is static, add a checkpoint object argument.
        if (isStatic) {
            // Add a "$CHECKPOINT" argument.
            SingleVariableDeclaration checkpoint = 
                ast.newSingleVariableDeclaration();
            checkpoint.setType(ast.newSimpleType(
                    _createName(ast, Checkpoint.class.getName())));
            checkpoint.setName(ast.newSimpleName(CHECKPOINT_NAME));
            method.parameters().add(checkpoint);
        }
        
        // Add all the indices.
        for (int i = 0; i < indices; i++) {
            SingleVariableDeclaration index = 
                ast.newSingleVariableDeclaration();
            index.setType(ast.newPrimitiveType(PrimitiveType.INT));
            index.setName(ast.newSimpleName("index" + i));
            method.parameters().add(index);
        }
        
        // Add a new value argument with name "newValue".
        SingleVariableDeclaration argument = 
            ast.newSingleVariableDeclaration();
        argument.setType(
                (org.eclipse.jdt.core.dom.Type)ASTNode.copySubtree(ast, type));
        argument.setName(ast.newSimpleName("newValue"));
        method.parameters().add(argument);
        
        // If the field is static, the method is also static; the method
        // is also private.
        int modifiers = Modifier.PRIVATE | Modifier.FINAL;
        if (isStatic)
            modifiers |= Modifier.STATIC;
        method.setModifiers(modifiers);
        
        // Create the method body.
        Block body = _createAssignmentBlock(ast, currentClass.getName(), 
                fieldName, fieldType, indices, dimensions);
        method.setBody(body);
        
        return method;
    }
    
    /** Create the body of an assignment method, which backs up the field
     *  before a new value is assigned to it.
     * 
     *  @param ast The {@link AST} object.
     *  @param className The full name of the current class.
     *  @param fieldName The name of the field.
     *  @param fieldType The type of the left-hand side (with <tt>indices</tt>
     *   dimensions less than the original field type).
     *  @param indices The number of indices.
     *  @param dimensions The number of dimensions of the original field type.
     *  @return The method body.
     */
    private Block _createAssignmentBlock(AST ast, String className, 
            String fieldName, Type fieldType, int indices, int dimensions) {
        Block block = ast.newBlock();
        
        // Test if the checkpoint object is not null.
        IfStatement ifStatement = ast.newIfStatement();
        InfixExpression testExpression = ast.newInfixExpression();
        testExpression.setLeftOperand(ast.newSimpleName(CHECKPOINT_NAME));
        testExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
        testExpression.setRightOperand(ast.newNullLiteral());
        ifStatement.setExpression(testExpression);
        
        // The "then" branch.
        Block thenBranch = ast.newBlock();
        
        // Method call to store old value.
        MethodInvocation recordInvocation = ast.newMethodInvocation();
        recordInvocation.setExpression(
                ast.newSimpleName(_getRecordName(fieldName)));
        recordInvocation.setName(ast.newSimpleName("add"));

        // If there are indices, create an integer array of those indices, 
        // and add it as an argument.
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

        // If there are indices, add them ("index0", "index1", ...) after the
        // field.
        Expression field  = ast.newSimpleName(fieldName);
        if (indices > 0) {
            for (int i = 0; i < indices; i++) {
                ArrayAccess arrayAccess = ast.newArrayAccess();
                arrayAccess.setArray(field);
                arrayAccess.setIndex(ast.newSimpleName("index" + i));
                field = arrayAccess;
            }
        }

        // Set the field as the next argument.
        recordInvocation.arguments().add(field);

        // Get current timestamp from the checkpoint object.
        MethodInvocation timestampGetter = ast.newMethodInvocation();
        timestampGetter.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
        timestampGetter.setName(ast.newSimpleName("getTimestamp"));

        // Set the timestamp as the next argument.
        recordInvocation.arguments().add(timestampGetter);
        
        // The statement of the method call.
        ExpressionStatement recordStatement = 
            ast.newExpressionStatement(recordInvocation);
        thenBranch.statements().add(recordStatement);
        
        ifStatement.setThenStatement(thenBranch);
        block.statements().add(ifStatement);
        
        // Finally, assign the new value to the field.
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(
                (Expression)ASTNode.copySubtree(ast, field));
        assignment.setRightHandSide(ast.newSimpleName("newValue"));
        assignment.setOperator(Assignment.Operator.ASSIGN);
        
        // Return the result of the assignment.
        ReturnStatement returnStatement = ast.newReturnStatement();
        returnStatement.setExpression(assignment);
        block.statements().add(returnStatement);
        
        return block;
    }
    
    /** Create the record of a field. The record is stored in an extra private
     *  field of the current class.
     * 
     *  @param currentClass The current class.
     *  @param ast The {@link AST} object.
     *  @param fieldName The name of the field.
     *  @param dimensions The number of dimensions of the field.
     *  @param isStatic Whether the field is static.
     *  @return The field declaration to be added to the current class
     *   declaration.
     */
    private FieldDeclaration _createFieldRecord(Class currentClass, AST ast, 
            String fieldName, int dimensions, boolean isStatic) {
        String recordName = _getRecordName(fieldName);
        
        // Check if the field is duplicated (possibly because the source
        // program is refactored twice).
        if (_isFieldDuplicated(currentClass, recordName))
            throw new ASTDuplicatedFieldException(currentClass.getName(), 
                    recordName);
        
        String typeName = FieldRecord.class.getName();

        // The only fragment of this field declaration.
        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(recordName));
        
        // Create the initializer, and use the number of dimensions as its
        // argument.
        ClassInstanceCreation initializer = ast.newClassInstanceCreation();
        initializer.setName(_createName(ast, typeName));
        initializer.arguments().add(
                ast.newNumberLiteral(Integer.toString(dimensions)));
        fragment.setInitializer(initializer);
        
        // The field declaration.
        FieldDeclaration field = ast.newFieldDeclaration(fragment);
        field.setType(_createType(ast, typeName));
        
        // If the field is static, the record field is also static; the record
        // field is also private.
        int modifiers = Modifier.PRIVATE;
        if (isStatic)
            modifiers |= Modifier.STATIC;
        field.setModifiers(modifiers);
        
        return field;
    }
    
    /** Create an AST name node with a name string (possibly partitioned with
     *  ".").
     * 
     *  @param ast The {@link AST} object.
     *  @param name The name.
     *  @return The AST name node.
     */
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
    
    /** Create an AST type node with a type string (possibly partitioned with
     *  "." and "[]").
     *  
     *  @param ast The {@link AST} object.
     *  @param type The type.
     *  @return The AST type node.
     */
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
    
    /** Get the list of indices of an accessed field. If the field is not of an
     *  array type but it is accessed at least once, the returned list contains
     *  only integer 0, which means no index is ever used. If the field is an
     *  array, the returned list is the list of numbers of indices that have
     *  ever been used.
     * 
     *  @param className The full name of the current class.
     *  @param fieldName The field name.
     *  @return The list of indices, or <tt>null</tt> if the field is not found
     *   or has never been used.
     */
    private List _getAccessedField(String className, String fieldName) {
        Hashtable classTable = (Hashtable)_accessedFields.get(className);
        if (classTable == null)
            return null;
        List indicesList = (List)classTable.get(fieldName);
        return indicesList;
    }
    
    /** Get the name of the assignment method.
     * 
     *  @param fieldName The field name.
     *  @return The name of the assignment method.
     */
    private String _getAssignMethodName(String fieldName) {
        return ASSIGN_PREFIX + fieldName;
    }
    
    /** Get the name of the record.
     * 
     *  @param fieldName The field name.
     *  @return The record name.
     */
    private String _getRecordName(String fieldName) {
        return RECORD_PREFIX + fieldName;
    }
    
    /** Handle a class declaration or anonymous class declaration. Records and
     *  assignment methods are added to the declaration.
     * 
     *  @param node The AST node of class declaration or anonymous class
     *   declaration.
     *  @param bodyDeclarations The list of body declarations in the class.
     *  @param state The current state of the type analyzer.
     */
    private void _handleDeclaration(ASTNode node, List bodyDeclarations, 
            TypeAnalyzerState state) {
        Class currentClass = state.getCurrentClass();
        List newMethods = new LinkedList();
        List newFields = new LinkedList();
        
        // Iterate over all the body declarations.
        Iterator bodyIter = bodyDeclarations.iterator();
        while (bodyIter.hasNext()) {
            Object nextDeclaration = bodyIter.next();
            
            // Handle only field declarations.
            if (nextDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDecl = (FieldDeclaration)nextDeclaration;
                boolean isStatic = Modifier.isStatic(fieldDecl.getModifiers());
                
                // If HANDLE_STATIC_FIELDS is set to false, do not refactor
                // static fields.
                if (isStatic && HANDLE_STATIC_FIELDS != true)
                    continue;
                
                // Handle only private fields.
                if (Modifier.isPrivate(fieldDecl.getModifiers())) {
                    AST ast = fieldDecl.getAST();
                    Type type = Type.getType(fieldDecl);
                    
                    // Iterate over all the fragments in the field declaration.
                    Iterator fragmentIter = fieldDecl.fragments().iterator();
                    while (fragmentIter.hasNext()) {
                        VariableDeclarationFragment fragment = 
                            (VariableDeclarationFragment)fragmentIter.next();
                        String fieldName = fragment.getName().getIdentifier();

                        // Get the list of numbers of indices.
                        List indicesList = 
                            _getAccessedField(currentClass.getName(), 
                                    fieldName);
                        if (indicesList == null)
                            continue;
                        
                        // Iterate over all the numbers of indices.
                        Iterator indicesIter = indicesList.iterator();
                        while (indicesIter.hasNext()) {
                            int indices = 
                                ((Integer)indicesIter.next()).intValue();
                            
                            // Create an extra method for every different
                            // number of indices.
                            newMethods.add(_createAssignMethod(currentClass, 
                                    ast, fieldName, type, indices, isStatic, 
                                    state.getClassLoader()));
                        }
                        
                        // Create a record field.
                        newFields.add(_createFieldRecord(currentClass, ast, 
                                fieldName, type.dimensions(), isStatic));
                    }
                }
            }
        }
        
        // Add all the methods and then all the fields.
        bodyDeclarations.addAll(newMethods);
        bodyDeclarations.addAll(newFields);
        
        // Add a special checkpoint object field.
        AST ast = node.getAST();
        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(CHECKPOINT_NAME));
        FieldDeclaration checkpointField = ast.newFieldDeclaration(fragment);
        checkpointField.setType(_createType(ast, Checkpoint.class.getName()));
        checkpointField.setModifiers(Modifier.PRIVATE);
        bodyDeclarations.add(checkpointField);
    }
    
    /** Find the first appearance of any of the given characters in a string.
     * 
     *  @param s The string.
     *  @param chars The array of characters.
     *  @param startPos The starting position from which the search begins.
     *  @return The index of the first appearance of any of the given
     *   characters in the string, or -1 if none of them is found.
     */
    private int _indexOf(String s, char[] chars, int startPos) {
        int pos = -1;
        for (int i = 0; i < chars.length; i++) {
            int newPos = s.indexOf(chars[i], startPos);
            if (pos == -1 || newPos < pos)
                pos = newPos;
        }
        return pos;
    }
    
    /** Test if a field to be added already exists.
     * 
     *  @param c The current class.
     *  @param fieldName The field name.
     *  @return <tt>true</tt> if the field is already in the class.
     *  @see #_isMethodDuplicated(Class, String, Type, int, boolean, 
     *   ClassLoader)
     */
    private boolean _isFieldDuplicated(Class c, String fieldName) {
        // Does NOT check fields inherited from interfaces.
        try {
            c.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
    
    /** Test if a method to be added already exists.
     * 
     *  @param c The current class.
     *  @param methodName The method name.
     *  @param fieldType The type of the field which the method manages.
     *  @param indices The number of indices.
     *  @param isStatic Whether the field is static.
     *  @param loader The class loader to be used.
     *  @return <tt>true</tt> if the method is already in the class.
     *  @see #_isFieldDuplicated(Class, String)
     */
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
    
    /** Record an accessed field (and possible indices after it) in a list.
     *  Extra methods and fields for these field accesses will be added to
     *  class declarations later when the traversal on the class finishes. 
     * 
     *  @param className The name of the current class.
     *  @param fieldName The field name.
     *  @param indices The number of indices.
     */
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
    
    /** Replace an AST node with another one by substituting the corresponding
     *  child of its parent.
     * 
     *  @param node The node to be replace.
     *  @param newNode The new node.
     */
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
    
    /** The table of access fields and their indices. Keys are class names;
     *  valus are hash tables. In each value, keys are field names, values
     *  are lists of indices.
     */
    private Hashtable _accessedFields = new Hashtable();
}
