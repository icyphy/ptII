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
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
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
public class AssignmentTransformer extends AbstractTransformer
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
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
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
     *  @param node The AST node of the anonymous class declaration.
     *  @param state The current state of the type analyzer.
     */
    public void handle(TypeDeclaration node, TypeAnalyzerState state) {
        _handleDeclaration(node, node.bodyDeclarations(), state);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       public fields                       ////

    /** The prefix of assignment methods.
     */
    public static String ASSIGN_PREFIX = "$ASSIGN$";
    
    /** Whether to refactor private static fields.
     */
    public static boolean HANDLE_STATIC_FIELDS = true;
    
    /** Whether to optimize method calls. (Not implemented yet.)
     */
    public static boolean OPTIMIZE_CALL = true;
    
    /** The name of the proxy class created in each anonymous class.
     */
    public static String PROXY_NAME = "_PROXY_";
    
    /** The prefix of records (new fields to be added to a class).
     */
    public static String RECORD_PREFIX = "$RECORD$";
    
    /** The name of restore methods.
     */
    public static String RESTORE_NAME = "$RESTORE";
    
    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    /** Create assignment methods for each accessed field that has been
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
        String methodName = _getAssignMethodName(fieldName);
        
        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (_isMethodDuplicated(currentClass, methodName, fieldType, 
                indices, isStatic, loader))
            throw new ASTDuplicatedMethodException(currentClass.getName(), 
                    methodName);
        
        MethodDeclaration method = ast.newMethodDeclaration();

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
    
    private FieldDeclaration _createCheckpointField(AST ast) {
        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(CHECKPOINT_NAME));
        
        ClassInstanceCreation checkpoint = ast.newClassInstanceCreation();
        checkpoint.setName(_createName(ast, Checkpoint.class.getName()));
        checkpoint.arguments().add(ast.newThisExpression());
        fragment.setInitializer(checkpoint);
        
        FieldDeclaration checkpointField = ast.newFieldDeclaration(fragment);
        checkpointField.setType(_createType(ast, Checkpoint.class.getName()));
        checkpointField.setModifiers(Modifier.PRIVATE);
        
        return checkpointField;
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
    
    private TypeDeclaration _createProxyClass(AST ast) {
        // Create the nested class.
        TypeDeclaration classDeclaration = ast.newTypeDeclaration();
        classDeclaration.setName(ast.newSimpleName(_getProxyName()));
        classDeclaration.superInterfaces().add(
                _createName(ast, Rollbackable.class.getName()));
        
        // Add a restore method.
        MethodDeclaration proxy = ast.newMethodDeclaration();
        proxy.setName(ast.newSimpleName(_getRestoreMethodName(false)));
        
        // Add two parameters.
        SingleVariableDeclaration timestamp = 
            ast.newSingleVariableDeclaration();
        timestamp.setType(ast.newPrimitiveType(PrimitiveType.INT));
        timestamp.setName(ast.newSimpleName("timestamp"));
        proxy.parameters().add(timestamp);
        
        SingleVariableDeclaration trim = 
            ast.newSingleVariableDeclaration();
        trim.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
        trim.setName(ast.newSimpleName("trim"));
        proxy.parameters().add(trim);
        
        // Add a call to the restore method in the enclosing anonymous class.
        MethodInvocation invocation = ast.newMethodInvocation();
        invocation.setName(ast.newSimpleName(_getRestoreMethodName(true)));
        invocation.arguments().add(ast.newSimpleName("timestamp"));
        invocation.arguments().add(ast.newSimpleName("trim"));

        Block body = ast.newBlock();
        body.statements().add(ast.newExpressionStatement(invocation));
        proxy.setBody(body);
        
        proxy.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        classDeclaration.bodyDeclarations().add(proxy);
        
        // Add a set checkpoint method.
        MethodDeclaration setCheckpoint = ast.newMethodDeclaration();
        setCheckpoint.setName(
                ast.newSimpleName(_getSetCheckpointMethodName(false)));
        
        // Add a single checkpoint parameter.
        SingleVariableDeclaration checkpoint = 
            ast.newSingleVariableDeclaration();
        checkpoint.setType(_createType(ast, Checkpoint.class.getName()));
        checkpoint.setName(ast.newSimpleName("checkpoint"));
        setCheckpoint.parameters().add(checkpoint);
        
        // Add a call to the restore method in the enclosing anonymous class.
        invocation = ast.newMethodInvocation();
        invocation.setName(ast.newSimpleName(_getSetCheckpointMethodName(true)));
        invocation.arguments().add(ast.newSimpleName("checkpoint"));

        body = ast.newBlock();
        body.statements().add(ast.newExpressionStatement(invocation));
        setCheckpoint.setBody(body);
        
        setCheckpoint.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        classDeclaration.bodyDeclarations().add(setCheckpoint);
        
        classDeclaration.setModifiers(Modifier.FINAL);
        return classDeclaration;
    }
    
    /** Create a restore method for a class, which restores all its state
     *  variables.
     * 
     *  @param currentClass The current class.
     *  @param ast The {@link AST} object.
     *  @param fieldNames The list of all the accessed fields.
     *  @param fieldTypes The types corresponding to the accessed fields.
     *  @param isAnonymous Whether the current class is anonymous.
     *  @return The declaration of the method that restores the old value
     *   of all the private fields.
     */
    private MethodDeclaration _createRestoreMethod(Class currentClass, AST ast,  
            List fieldNames, List fieldTypes, boolean isAnonymous) {
        String methodName = _getRestoreMethodName(isAnonymous);
        
        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (_isMethodDuplicated(currentClass, methodName, 
                new Class[]{int.class, boolean.class}))
            throw new ASTDuplicatedMethodException(currentClass.getName(), 
                    methodName);

        MethodDeclaration method = ast.newMethodDeclaration();
        
        // Set the method name.
        method.setName(ast.newSimpleName(methodName));
        
        // Add a timestamp parameter.
        SingleVariableDeclaration timestamp = 
            ast.newSingleVariableDeclaration();
        timestamp.setType(ast.newPrimitiveType(PrimitiveType.INT));
        timestamp.setName(ast.newSimpleName("timestamp"));
        method.parameters().add(timestamp);
        
        // Add a trim parameter.
        SingleVariableDeclaration trim = 
            ast.newSingleVariableDeclaration();
        trim.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
        trim.setName(ast.newSimpleName("trim"));
        method.parameters().add(trim);
        
        // Return type default to "void".
        
        // The method body.
        Block body = ast.newBlock();
        method.setBody(body);
        
        Iterator namesIter = fieldNames.iterator();
        Iterator typesIter = fieldTypes.iterator();
        while (namesIter.hasNext()) {
            String fieldName = (String)namesIter.next();
            Type fieldType = (Type)typesIter.next();

            MethodInvocation restoreMethodCall = ast.newMethodInvocation();
            restoreMethodCall.setExpression(
                    ast.newSimpleName(_getRecordName(fieldName)));
        
            // Set the restore method name.
            String restoreMethodName;
            if (fieldType.isPrimitive()) {
                String typeName = fieldType.getName();
                restoreMethodName = "restore" +
                        Character.toUpperCase(typeName.charAt(0)) +
                        typeName.substring(1);
            } else
                restoreMethodName = "restoreObject";
            
            restoreMethodCall.arguments().add(ast.newSimpleName(fieldName));
            restoreMethodCall.setName(ast.newSimpleName(restoreMethodName));
        
            // Add two arguments to the restore method call.
            restoreMethodCall.arguments().add(ast.newSimpleName("timestamp"));
            restoreMethodCall.arguments().add(ast.newSimpleName("trim"));
        
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(ast.newSimpleName(fieldName));
            if (fieldType.isPrimitive())
                assignment.setRightHandSide(restoreMethodCall);
            else {
                CastExpression castExpression = ast.newCastExpression();
                castExpression.setType(_createType(ast, fieldType.getName()));
                castExpression.setExpression(restoreMethodCall);
                assignment.setRightHandSide(castExpression);
            }
        
            ExpressionStatement assignStatement = 
                ast.newExpressionStatement(assignment);
            body.statements().add(assignStatement);
        }
        
        method.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        return method;
    }
    
    /** Create a set checkpoint method for a class, which sets its checkpoint
     *  object.
     * 
     *  @param currentClass The current class.
     *  @param ast The {@link AST} object.
     *  @param isAnonymous Whether the current class is anonymous.
     *  @return The declaration of the method that restores the old value
     *   of all the private fields.
     */
    private MethodDeclaration _createSetCheckpointMethod(Class currentClass,  
            AST ast, boolean isAnonymous) {
        String methodName = _getSetCheckpointMethodName(isAnonymous);
        
        // Check if the method is duplicated (possibly because the source
        // program is refactored twice).
        if (_isMethodDuplicated(currentClass, methodName, 
                new Class[]{Checkpoint.class}))
            throw new ASTDuplicatedMethodException(currentClass.getName(), 
                    methodName);

        MethodDeclaration method = ast.newMethodDeclaration();
        
        // Set the method name.
        method.setName(ast.newSimpleName(methodName));
        
        // Add a checkpoint parameter.
        SingleVariableDeclaration checkpoint = 
            ast.newSingleVariableDeclaration();
        checkpoint.setType(_createType(ast, Checkpoint.class.getName()));
        checkpoint.setName(ast.newSimpleName("checkpoint"));
        method.parameters().add(checkpoint);
        
        // Return type default to "void".
        
        // The test.
        IfStatement test = ast.newIfStatement();
        InfixExpression testExpression = ast.newInfixExpression();
        testExpression.setLeftOperand(ast.newSimpleName(CHECKPOINT_NAME));
        testExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
        testExpression.setRightOperand(ast.newSimpleName("checkpoint"));
        test.setExpression(testExpression);
        
        // The "then" branch of the test.
        Block thenBranch = ast.newBlock();
        test.setThenStatement(thenBranch);
        Block body = ast.newBlock();
        body.statements().add(test);
        method.setBody(body);
        
        // Backup the old checkpoint.
        VariableDeclarationFragment fragment = 
            ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName("oldCheckpoint"));
        fragment.setInitializer(ast.newSimpleName(CHECKPOINT_NAME));
        VariableDeclarationStatement tempDeclaration = 
            ast.newVariableDeclarationStatement(fragment);
        tempDeclaration.setType(_createType(ast, Checkpoint.class.getName()));
        thenBranch.statements().add(tempDeclaration);
        
        // Assign the new checkpoint.
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(ast.newSimpleName(CHECKPOINT_NAME));
        assignment.setRightHandSide(ast.newSimpleName("checkpoint"));
        ExpressionStatement statement = 
            ast.newExpressionStatement(assignment);
        thenBranch.statements().add(statement);
        
        // Propagate the change to other objects monitored by the same old
        // checkpoint.
        MethodInvocation propagate = ast.newMethodInvocation();
        propagate.setExpression(ast.newSimpleName("oldCheckpoint"));
        propagate.setName(ast.newSimpleName("setCheckpoint"));
        propagate.arguments().add(ast.newSimpleName("checkpoint"));
        thenBranch.statements().add(ast.newExpressionStatement(propagate));
        
        // Add this object to the list in the checkpoint.
        MethodInvocation addInvocation = ast.newMethodInvocation();
        addInvocation.setExpression(ast.newSimpleName("checkpoint"));
        addInvocation.setName(ast.newSimpleName("addObject"));
        if (isAnonymous) {
            ClassInstanceCreation proxy = ast.newClassInstanceCreation();
            proxy.setName(ast.newSimpleName(_getProxyName()));
            addInvocation.arguments().add(proxy);
        } else
            addInvocation.arguments().add(ast.newThisExpression());
        thenBranch.statements().add(
                ast.newExpressionStatement(addInvocation));
        
        method.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        return method;
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
    
    /** Get the name of the proxy class to be created in each anonymous class.
     * 
     *  @return The proxy class name.
     */
    private String _getProxyName() {
        return PROXY_NAME;
    }
    
    /** Get the name of the record.
     * 
     *  @param fieldName The field name.
     *  @return The record name.
     */
    private String _getRecordName(String fieldName) {
        return RECORD_PREFIX + fieldName;
    }
    
    /** Get the name of the restore method.
     * 
     *  @param isAnonymous Whether the current class is an anonymous class.
     *  @return The name of the restore method.
     */
    private String _getRestoreMethodName(boolean isAnonymous) {
        return RESTORE_NAME + (isAnonymous ? "_ANONYMOUS" : "");
    }
    
    /** Get the name of the set checkpoint method.
     * 
     *  @param isAnonymous Whether the current class is an anonymous class.
     *  @return
     */
    private String _getSetCheckpointMethodName(boolean isAnonymous) {
        return SET_CHECKPOINT_NAME + (isAnonymous ? "_ANONYMOUS" : "");
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
        
        // The lists for _createRestoreMethod.
        List fieldNames = new LinkedList();
        List fieldTypes = new LinkedList();
        
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
                        
                        fieldNames.add(fieldName);
                        fieldTypes.add(type);
                        
                        // Create a record field.
                        newFields.add(_createFieldRecord(currentClass, ast, 
                                fieldName, type.dimensions(), isStatic));
                    }
                }
            }
        }
        
        AST ast = node.getAST();

        newMethods.add(_createRestoreMethod(currentClass, ast, fieldNames, 
                fieldTypes, node instanceof AnonymousClassDeclaration));
        
        newMethods.add(_createSetCheckpointMethod(currentClass, ast, 
                node instanceof AnonymousClassDeclaration));
        
        // Add an interface.
        if (node instanceof TypeDeclaration)
            ((TypeDeclaration)node).superInterfaces().add(
                    _createName(ast, Rollbackable.class.getName()));
        else
            bodyDeclarations.add(_createProxyClass(ast));
        
        // Add all the methods and then all the fields.
        bodyDeclarations.addAll(newMethods);
        bodyDeclarations.addAll(newFields);
        
        if (node instanceof AnonymousClassDeclaration) {
            // Create a simple initializer.
            Initializer initializer = ast.newInitializer();
            Block body = ast.newBlock();
            initializer.setBody(body);
            MethodInvocation addInvocation = ast.newMethodInvocation();
            addInvocation.setExpression(ast.newSimpleName(CHECKPOINT_NAME));
            addInvocation.setName(ast.newSimpleName("addObject"));
            ClassInstanceCreation proxy = ast.newClassInstanceCreation();
            proxy.setName(ast.newSimpleName(_getProxyName()));
            addInvocation.arguments().add(proxy);
            body.statements().add(ast.newExpressionStatement(addInvocation));
            bodyDeclarations.add(initializer);
        } else {
            // Add a special checkpoint object field.
            FieldDeclaration checkpointField = _createCheckpointField(ast);
            bodyDeclarations.add(0, checkpointField);
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
    
    /** The table of access fields and their indices. Keys are class names;
     *  valus are hash tables. In each value, keys are field names, values
     *  are lists of indices.
     */
    private Hashtable _accessedFields = new Hashtable();
}
