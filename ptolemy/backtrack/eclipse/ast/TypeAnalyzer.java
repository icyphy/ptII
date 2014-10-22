/* Analyze an Eclipse AST and try to assign a type to each
 (sub-)expression node in it.

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
package ptolemy.backtrack.eclipse.ast;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ptolemy.backtrack.eclipse.ast.TypeAnalyzerState.CurrentClassElement;
import ptolemy.backtrack.eclipse.ast.transform.AliasHandler;
import ptolemy.backtrack.eclipse.ast.transform.AssignmentHandler;
import ptolemy.backtrack.eclipse.ast.transform.ClassHandler;
import ptolemy.backtrack.eclipse.ast.transform.ConstructorHandler;
import ptolemy.backtrack.eclipse.ast.transform.CrossAnalysisHandler;
import ptolemy.backtrack.eclipse.ast.transform.FieldDeclarationHandler;
import ptolemy.backtrack.eclipse.ast.transform.HandlerList;
import ptolemy.backtrack.eclipse.ast.transform.MethodDeclarationHandler;
import ptolemy.backtrack.util.PathFinder;

///////////////////////////////////////////////////////////////////
//// TypeAnalyzer

/**
 A type analyzer for Java Abstract Syntax Trees (ASTs) generated by
 <a href="http://eclipse.org/" target="_blank">Eclipse</a>.
 <p>
 This analyzer does a depth-first traversal on the given AST. It tries
 to assign a type to every expression and sub-expression node a type
 according to the Java 1.5 semantics (but it is also backward compatible
 with Java 1.4). This type information can be used later to decide the
 behavior of each method call and field access.
 <p>
 When other classes are referenced in the AST, this analyzer tries to
 load them with the Java reflection mechanism. Once loaded, their names
 are assigned to the corresponding nodes as their types. This analyzer
 uses {@link LocalClassLoader} to simulate loading a class with its
 partial name within another class.
 <p>
 This analyzer assumes that the classes being analyzed (including those
 classes indirectly referred to) have been compiled with the Java
 compiler. It does not check for type errors in the AST, but it mimics
 the typing semantics of the Java compiler.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TypeAnalyzer extends ASTVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct an analyzer with no explicit class path for its
     *  class loader (an instanceof {@link LocalClassLoader}). Such
     *  a class loader cannot resolve classes other than Java
     *  built-in classes.
     *  @exception  MalformedURLException If a classpath is not a proper URL.
     */
    public TypeAnalyzer() throws MalformedURLException {
        this(null);
    }

    /** Construct an analyzer with with an array of explicit class
     *  paths for its class loader (an instanceof {@link
     *  LocalClassLoader}).
     *
     *  @param classPaths The class paths.
     *  @exception  MalformedURLException If a classpath is not a proper URL.
     */
    public TypeAnalyzer(String[] classPaths) throws MalformedURLException {
        _state.setClassLoader(new LocalClassLoader(classPaths));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a name of a type to the set of types to be cross-analyzed.
     *
     *  @param fullName The name of the type to be added.
     *  @see #addCrossAnalyzedTypes(String[])
     */
    public void addCrossAnalyzedType(String fullName) {
        _state.getCrossAnalyzedTypes().add(fullName);

        if (_handlers.hasCrossAnalysisHandler()) {
            for (CrossAnalysisHandler handler : _handlers
                    .getCrossAnalysisHandlers()) {
                handler.handle(_state);
            }
        }
    }

    /** Add an array of names of types to the set of types to be
     *  cross-analyzed.
     *
     *  @param fullNames The array of names of types to be added.
     *  @see #addCrossAnalyzedType(String)
     */
    public void addCrossAnalyzedTypes(String[] fullNames) {
        _state.getCrossAnalyzedTypes().addAll(Arrays.asList(fullNames));

        if (_handlers.hasCrossAnalysisHandler()) {
            for (CrossAnalysisHandler handler : _handlers
                    .getCrossAnalysisHandlers()) {
                handler.handle(_state);
            }
        }
    }

    /** End the visit of an anonymous class declaration and close its
     *  scope. The current class is set back to the last visited class.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(AnonymousClassDeclaration node) {
        if (_handlers.hasClassHandler()) {
            for (ClassHandler handler : _handlers.getClassHandlers()) {
                handler.exit(node, _state);
            }
        }

        _unrecordFields();
        _state.unsetClassScope();
        _closeScope();
        _state.leaveClass();
    }

    /** Visit an array access node and set its type to be the type with one
     *  less dimension.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ArrayAccess node) {
        Type arrayType = Type.getType(node.getArray());
        try {
            Type.setType(node, arrayType.removeOneDimension());
            Type.propagateOwner(node, node.getArray());
        } catch (ClassNotFoundException ex) {
            throw new UnknownASTException(ex);
        }
    }

    /** Propagate the type of the array to this node.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ArrayCreation node) {
        Type.propagateType(node, node.getType());
    }

    /** Visit an array type node and set its type to be the type with one
     *  more dimension than its component type.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ArrayType node) {
        Type componentType = Type.getType(node.getComponentType());
        Type newType = componentType.addOneDimension();
        Type.setType(node, newType);
    }

    /** Propagate the type of the left-hand side of the assignment
     *  to this node.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(Assignment node) {
        Type.propagateType(node, node.getLeftHandSide());

        if (_handlers.hasAssignmentHandler()) {
            for (AssignmentHandler handler : _handlers.getAssignmentHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** End the visit of a block node and close the scope opened by the
     *  previous visit function.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(Block node) {
        _closeScope();
        _state.leaveBlock();
    }

    /** Visit a literal node and set its type to be the same type as the
     *  literal.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(BooleanLiteral node) {
        Type.setType(node, Type.BOOLEAN_TYPE);
    }

    /** Propagate the type of the cast class to this node.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(CastExpression node) {
        Type.propagateType(node, node.getType());
        Type.propagateOwner(node, node.getExpression());
    }

    /** Visit a literal node and set its type to be the same type as the
     *  literal.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(CharacterLiteral node) {
        Type.setType(node, Type.CHAR_TYPE);
    }

    /** Propagate the type of the instantiated class to this node.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ClassInstanceCreation node) {
        Type.propagateType(node, node.getType());

        Expression expression = node.getExpression();

        if (expression != null) {
            Type.setOwner(node, Type.getType(expression));
        }

        if (_handlers.hasConstructorHandler()) {
            for (ConstructorHandler handler : _handlers
                    .getConstructorHandlers()) {
                handler.handle(node, _state);
            }
        }

        if (_handlers.hasAliasHandler()) {
            for (AliasHandler handler : _handlers.getAliasHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** Visit a conditional expression node and set its type to be
     *  the type that is compatible with both the then expression and
     *  the else expression.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ConditionalExpression node) {
        Type type1 = Type.getType(node.getThenExpression());
        Type type2 = Type.getType(node.getElseExpression());

        Type commonType = Type.getCommonType(type1, type2);

        try {
            if ((commonType == null)
                    && (type1.compatibility(type2, _state.getClassLoader()) >= 0)) {
                commonType = type2;
            }
        } catch (ClassNotFoundException e) {
        }

        try {
            if ((commonType == null)
                    && (type2.compatibility(type1, _state.getClassLoader()) >= 0)) {
                commonType = type1;
            }
        } catch (ClassNotFoundException e) {
        }

        if (commonType == null) {
            throw new UnknownASTException();
        }

        Type.setType(node, commonType);
    }

    /** End the visit of an enhanced for statement and close the scope
     *  opened by the previous visit function.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(EnhancedForStatement node) {
        _closeScope();
    }

    /** Visit a field access node, resolve it in the current scope, and set
     *  its type to be the type of field referred to.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(FieldAccess node) {
        Expression expression = node.getExpression();
        SimpleName name = node.getName();
        Type owner = Type.getType(expression);
        TypeAndOwner typeAndOwner = _resolveName(name.getIdentifier(), owner);

        Type.setOwner(node, typeAndOwner._getOwner());
        Type.setType(node, typeAndOwner._getType());
    }

    /** Visit a field declaration and set its type to be the same as the
     *  declared type.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(FieldDeclaration node) {
        Type.propagateType(node, node.getType());

        if (_handlers.hasConstructorHandler()) {
            for (ConstructorHandler handler : _handlers
                    .getConstructorHandlers()) {
                handler.exit(node, _state);
            }
        }

        if (_handlers.hasFieldDeclarationHandler()) {
            for (FieldDeclarationHandler handler : _handlers
                    .getFieldDeclarationHandlers()) {
                handler.exit(node, _state);
            }
        }
    }

    /** End the visit of a for statement and close the scope opened by
     *  the previous visit function.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ForStatement node) {
        _closeScope();
    }

    /** End the visit of an importation declaration and record the
     *  imported class or package in the class loader. If a class is
     *  imported, it is loaded immediately and put in a hash table to
     *  enable fast class lookup.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ImportDeclaration node) {
        String importName = node.getName().toString();

        if (node.isOnDemand()) {
            _state.getClassLoader().importPackage(importName);
        } else {
            _state.getClassLoader().importClass(importName);
            _importClass(importName);
        }
    }

    /** Visit an infix expression and compute the type for it.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(InfixExpression node) {
        InfixExpression.Operator operator = node.getOperator();
        Expression leftHand = node.getLeftOperand();
        Expression rightHand = node.getRightOperand();
        List extendedOps = node.extendedOperands();
        Type type = null;

        if (operator.equals(InfixExpression.Operator.PLUS)
                || operator.equals(InfixExpression.Operator.MINUS)
                || operator.equals(InfixExpression.Operator.TIMES)
                || operator.equals(InfixExpression.Operator.DIVIDE)
                || operator.equals(InfixExpression.Operator.REMAINDER)
                || operator.equals(InfixExpression.Operator.LEFT_SHIFT)
                || operator.equals(InfixExpression.Operator.RIGHT_SHIFT_SIGNED)
                || operator
                        .equals(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED)
                || operator.equals(InfixExpression.Operator.AND)
                || operator.equals(InfixExpression.Operator.OR)
                || operator.equals(InfixExpression.Operator.XOR)) {
            type = Type.getCommonType(Type.getType(leftHand), Type
                    .getType(rightHand));

            Iterator extendedIter = extendedOps.iterator();

            while ((type != null) && extendedIter.hasNext()) {
                type = Type.getCommonType(type, Type
                        .getType((Expression) extendedIter.next()));
            }
        } else if (operator.equals(InfixExpression.Operator.LESS)
                || operator.equals(InfixExpression.Operator.LESS_EQUALS)
                || operator.equals(InfixExpression.Operator.GREATER)
                || operator.equals(InfixExpression.Operator.GREATER_EQUALS)
                || operator.equals(InfixExpression.Operator.CONDITIONAL_AND)
                || operator.equals(InfixExpression.Operator.CONDITIONAL_OR)
                || operator.equals(InfixExpression.Operator.EQUALS)
                || operator.equals(InfixExpression.Operator.NOT_EQUALS)) {
            type = Type.BOOLEAN_TYPE;
        } else {
            throw new UnknownASTException();
        }

        if (type == null) {
            throw new UnknownASTException();
        }

        Type.setType(node, type);
    }

    /** Visit an <tt>instanceof</tt> expression and set its type to
     *  be boolean.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(InstanceofExpression node) {
        Type.setType(node, Type.BOOLEAN_TYPE);
    }

    /** End the visit of a method declaration and close the scope
     *  opened by the previous visit function. The type of the method
     *  declaration is set to be the same as the return type.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(MethodDeclaration node) {
        if (node.isConstructor() && _handlers.hasConstructorHandler()) {
            for (ConstructorHandler handler : _handlers
                    .getConstructorHandlers()) {
                handler.handle(node, _state);
            }
        }

        if (_handlers.hasMethodDeclarationHandler()) {
            for (MethodDeclarationHandler handler : _handlers
                    .getMethodDeclarationHandlers()) {
                handler.exit(node, _state);
            }
        }

        _closeScope();
        org.eclipse.jdt.core.dom.Type returnType = node.getReturnType2();
        if (returnType != null) {
            Type.propagateType(node, returnType);
        }
    }

    /** Visit a method invocation node and resolve the invoked method. The
     *  type of the node is set to the return type of the method.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(MethodInvocation node) {
        Expression expression = node.getExpression();
        Class owner = null;

        if (expression != null) {
            Type ownerType = null;
            ownerType = Type.getType(expression);

            try {
                owner = ownerType.toClass(_state.getClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new ASTClassNotFoundException(ownerType, ex);
            }
        }

        Type[] types = _argumentsToTypes(node.arguments());
        TypeAndOwner typeAndOwner = _resolveMethod(owner, node.getName()
                .getIdentifier(), types);

        if (typeAndOwner == null) {
            throw new ASTResolutionException((owner == null) ? _state
                    .getCurrentClass().getName() : owner.getName(), node
                    .getName().getIdentifier());
        } else {
            Type.setOwner(node, typeAndOwner._getOwner());
            Type.setOwner(node.getName(), typeAndOwner._getOwner());
            Type.setType(node, typeAndOwner._getType());
            Type.setType(node.getName(), typeAndOwner._getType());
        }

        if (_handlers.hasAliasHandler()) {
            for (AliasHandler handler : _handlers.getAliasHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** Visit a literal node and set its type to be the same type as the
     *  literal.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(NullLiteral node) {
        Type.setType(node, Type.NULL_TYPE);
    }

    /** Visit a literal node and set its type to be the same type as the
     *  literal.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(NumberLiteral node) {
        String token = node.getToken();

        if (!token.startsWith("0x")
                && ((token.indexOf('.') != -1) || token.endsWith("d") || token
                        .endsWith("f"))) { // double/float

            if (token.endsWith("f")) {
                Type.setType(node, Type.FLOAT_TYPE);
            } else {
                Type.setType(node, Type.DOUBLE_TYPE);
            }
        } else { // int/long/short

            if (token.endsWith("l")) {
                Type.setType(node, Type.LONG_TYPE);
            } else {
                Type.setType(node, Type.INT_TYPE);
            }
        }
    }

    /** End the visit of a package declaration and set the current package
     *  to be the full name of that declared package.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(PackageDeclaration node) {
        _state.getClassLoader().setCurrentPackage(node.getName().toString());
    }

    /** Visit a parameterized type and set its type to be the base type.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ParameterizedType node) {
        Type.propagateType(node, node.getType());
    }

    /** Propagate the type of the expression between the parentheses
     *  to this node.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ParenthesizedExpression node) {
        Type.propagateType(node, node.getExpression());
        Type.propagateOwner(node, node.getExpression());
    }

    /** Propagate the type of the its sub-expression to this node.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(PostfixExpression node) {
        Type.propagateType(node, node.getOperand());

        PostfixExpression.Operator operator = node.getOperator();

        if (((operator == PostfixExpression.Operator.INCREMENT) || (operator == PostfixExpression.Operator.DECREMENT))
                && _handlers.hasClassHandler()) {
            for (AssignmentHandler handler : _handlers.getAssignmentHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** Propagate the type of the its sub-expression to this node.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(PrefixExpression node) {
        Type.propagateType(node, node.getOperand());

        PrefixExpression.Operator operator = node.getOperator();

        if (((operator == PrefixExpression.Operator.INCREMENT) || (operator == PrefixExpression.Operator.DECREMENT))
                && _handlers.hasClassHandler()) {
            for (AssignmentHandler handler : _handlers.getAssignmentHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** Visit a primitive type node and set its type to be the corresponding
     *  primitive type.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(PrimitiveType node) {
        String typeName = node.getPrimitiveTypeCode().toString();
        Type.setType(node, Type.createType(typeName));
    }

    /** Visit a qualified name, resolve it in the current scope, and set
     *  its type to be the type of object referred to by that name.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(QualifiedName node) {
        if (_state.getCurrentClass() != null) {
            Name qualifier = node.getQualifier();
            SimpleName name = node.getName();
            Type owner;

            if (qualifier instanceof SimpleName) {
                TypeAndOwner ownerTypeAndOwner = _resolveName(
                        ((SimpleName) qualifier).getIdentifier(), null);

                if (ownerTypeAndOwner == null) {
                    owner = null;
                } else {
                    Type.setOwner(qualifier, ownerTypeAndOwner._getOwner());
                    owner = ownerTypeAndOwner._getType();
                    Type.setType(qualifier, owner);
                }
            } else {
                owner = Type.getType(qualifier);
            }

            String resolveName;

            if (owner == null) {
                resolveName = qualifier.toString() + "." + name.getIdentifier();
            } else {
                resolveName = name.getIdentifier();
            }

            TypeAndOwner nodeTypeAndOwner = _resolveName(resolveName, owner);

            if (nodeTypeAndOwner == null) {
                if (!(node.getParent() instanceof QualifiedName)) {
                    throw new ASTResolutionException((owner == null) ? _state
                            .getCurrentClass().getName() : owner.getName(),
                            resolveName);
                }
            } else {
                Type.setOwner(node, nodeTypeAndOwner._getOwner());
                Type.setOwner(name, nodeTypeAndOwner._getOwner());
                Type.setType(node, nodeTypeAndOwner._getType());
                Type.setType(name, nodeTypeAndOwner._getType());
            }
        }
    }

    /** Visit a return statement.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ReturnStatement node) {
        super.visit(node);

        if (_handlers.hasAliasHandler()) {
            for (AliasHandler handler : _handlers.getAliasHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** Visit a simple name, and resolve it if possible. Some simple names
     *  are not resolved, such as the name of a class to be declared, and the
     *  name of a local variable in its definition.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(SimpleName node) {
        if (_state.getCurrentClass() != null) {
            ASTNode parent = node.getParent();
            Type owner = null;
            boolean handle = true;

            // Do not type check some simple names.
            if (parent instanceof QualifiedName) {
                handle = false;
            } else if (parent instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) parent;

                if (type.getName() == node) {
                    handle = false;
                }
            } else if (parent instanceof BodyDeclaration) {
                handle = false;
            } else if (parent instanceof BreakStatement
                    && (((BreakStatement) parent).getLabel() == node)) {
                handle = false;
            } else if (parent instanceof SimpleType
                    && parent.getParent() instanceof ClassInstanceCreation) {
                Expression expression = ((ClassInstanceCreation) parent
                        .getParent()).getExpression();

                if (expression != null) {
                    owner = Type.getType(expression);
                }
            } else if (parent instanceof FieldAccess
                    && (((FieldAccess) parent).getName() == node)) {
                handle = false;
            } else if (parent instanceof LabeledStatement
                    && (((LabeledStatement) parent).getLabel() == node)) {
                handle = false;
            } else if (parent instanceof MethodInvocation
                    && (((MethodInvocation) parent).getName() == node)) {
                handle = false;
            } else if (parent instanceof SuperMethodInvocation
                    && (((SuperMethodInvocation) parent).getName() == node)) {
                handle = false;
            } else if (parent instanceof SuperFieldAccess
                    && (((SuperFieldAccess) parent).getName() == node)) {
                handle = false;
            } else if (parent instanceof VariableDeclaration
                    && (((VariableDeclaration) parent).getName() == node)) {
                handle = false;
            }

            if (handle) {
                String name = node.getIdentifier();
                TypeAndOwner typeAndOwner = _resolveName(name, owner);

                if (typeAndOwner == null) {
                    String currentClassName = _state.getCurrentClass()
                            .getName();
                    throw new ASTResolutionException(currentClassName, name);
                }

                Type.setOwner(node, typeAndOwner._getOwner());
                Type.setType(node, typeAndOwner._getType());
            }
        }
    }

    /** Visit a simple type node, and propergate the type of its name to it if
     *  it is in a {@link ClassInstanceCreation}.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(SimpleType node) {
        if (node.getParent() instanceof ClassInstanceCreation) {
            Type.propagateType(node, node.getName());
        }

        super.endVisit(node);
    }

    /** Visit a single variable declaration and set its type to be the
     *  declared type. The variable is recorded in the current scope.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(SingleVariableDeclaration node) {
        Type type = Type.getType(node.getType());

        for (int i = 0; i < node.getExtraDimensions(); i++) {
            type = type.addOneDimension();
        }

        Type.setType(node, type);
        _state.addVariable(node.getName().getIdentifier(), type);
    }

    /** Visit a literal node and set its type to be the same type as the
     *  literal.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(StringLiteral node) {
        Type.setType(node, Type.createType("java.lang.String"));
    }

    /** Visit a super constructor invocation node and calls the constructor
     *  handlers associated with this type analyzer (if any).
     *
     *  @param node The node to be visited.
     */
    public void endVisit(SuperConstructorInvocation node) {
        if (_handlers.hasConstructorHandler()) {
            for (ConstructorHandler handler : _handlers
                    .getConstructorHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** Visit a super field access node (<tt>super.FieldName</tt>), and
     *  resolve the field from the superclass of the given class name
     *  (if any, like <tt>ClassName.super.FieldName</tt>) or from the
     *  superclass of the class being inspected.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(SuperFieldAccess node) {
        Name qualifier = node.getQualifier();
        Class owner = null;

        if (qualifier != null) {
            Type ownerType = null;
            ownerType = Type.getType(qualifier);

            try {
                owner = ownerType.toClass(_state.getClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new ASTClassNotFoundException(ownerType, ex);
            }
        } else {
            owner = _state.getCurrentClass();
        }

        owner = owner.getSuperclass();

        if (owner == null) {
            throw new UnknownASTException();
        }

        TypeAndOwner typeAndOwner = _resolveName(
                node.getName().getIdentifier(), Type
                        .createType(owner.getName()));

        if (typeAndOwner == null) {
            throw new ASTResolutionException((owner == null) ? _state
                    .getCurrentClass().getName() : owner.getName(), node
                    .getName().getIdentifier());
        }

        Type.setOwner(node, typeAndOwner._getOwner());
        Type.setType(node, typeAndOwner._getType());
    }

    /** Visit a super method invocation node (<tt>super.method(...)</tt>),
     *  resolve the method from the superclass of the given class name
     *  (if any, like <tt>ClassName.super.FieldName</tt>) or from the
     *  superclass of the class being inspected. The type of the node is
     *  set to be the same as the return type of the method.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(SuperMethodInvocation node) {
        Name qualifier = node.getQualifier();
        Class owner = null;

        if (qualifier != null) {
            Type ownerType = null;
            ownerType = Type.getType(qualifier);

            try {
                owner = ownerType.toClass(_state.getClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new ASTClassNotFoundException(ownerType, ex);
            }
        } else {
            owner = _state.getCurrentClass();
        }

        owner = owner.getSuperclass();

        if (owner == null) {
            throw new UnknownASTException();
        }

        Type[] types = _argumentsToTypes(node.arguments());
        TypeAndOwner typeAndOwner = _resolveMethod(owner, node.getName()
                .getIdentifier(), types);

        if (typeAndOwner == null) {
            throw new ASTResolutionException((owner == null) ? _state
                    .getCurrentClass().getName() : owner.getName(), node
                    .getName().getIdentifier());
        } else {
            Type.setOwner(node, typeAndOwner._getOwner());
            Type.setOwner(node.getName(), typeAndOwner._getOwner());
            Type.setType(node, typeAndOwner._getType());
            Type.setType(node.getName(), typeAndOwner._getType());
        }
    }

    /** Visit a <tt>this</tt> expression, and set its type to be
     *  the class preceding it (if any) or the class currently being
     *  inspected.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(ThisExpression node) {
        Name qualifier = node.getQualifier();

        if (qualifier != null) {
            Type.propagateType(node, qualifier);
        } else {
            Type.setType(node, Type.createType(_state.getCurrentClass()
                    .getName()));
        }
    }

    /** End the visit of a type declaration and close its scope. The
     *  current class is set back to the last visited class.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(TypeDeclaration node) {
        Class currentClass = _state.getCurrentClass();

        if (_handlers.hasClassHandler()) {
            for (ClassHandler handler : _handlers.getClassHandlers()) {
                handler.exit(node, _state);
            }
        }

        _unrecordFields();
        _state.unsetClassScope();
        _closeScope();
        _state.leaveClass();

        if (node.getParent() instanceof TypeDeclarationStatement) {
            String typeName = node.getName().getIdentifier();
            Hashtable<String, Class> classTable = _state.getPreviousClasses()
                    .peek();
            classTable.put(typeName, currentClass);
        }
    }

    /** Visit a literal node and set its type to be the same type as the
     *  literal.
     *  <p>
     *  A type literal is of the form "<tt>CLASSNAME.class</tt>", so its
     *  type is always {@link Class}.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(TypeLiteral node) {
        Type.setType(node, Type.createType("java.lang.Class"));
    }

    /** Visit a variable declaration expression and set its type to be
     *  the declared type.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(VariableDeclarationExpression node) {
        Type.propagateType(node, node.getType());
    }

    /** Visit a variable declaration fragment and set its type to be the
     *  declared type of the node's parent (a variable declaration or field
     *  declaration). The variable is recorded in the current scope.
     *
     *  @param node The node to be visited.
     */
    public void endVisit(VariableDeclarationFragment node) {
        Type type = null;
        ASTNode parent = node.getParent();

        if (parent instanceof VariableDeclarationStatement) {
            type = Type.getType(((VariableDeclarationStatement) parent)
                    .getType());
        } else if (parent instanceof FieldDeclaration) {
            type = Type.getType(((FieldDeclaration) parent).getType());
        } else if (parent instanceof VariableDeclarationExpression) {
            type = Type.getType(((VariableDeclarationExpression) parent)
                    .getType());
        } else {
            throw new UnknownASTException();
        }

        for (int i = 0; i < node.getExtraDimensions(); i++) {
            type = type.addOneDimension();
        }

        Type.setType(node, type);
        Type.setType(node.getName(), type);
        _state.addVariable(node.getName().getIdentifier(), type);

        if (_handlers.hasAliasHandler()) {
            for (AliasHandler handler : _handlers.getAliasHandlers()) {
                handler.handle(node, _state);
            }
        }
    }

    /** Get the list of handlers to be called back when traversing the AST.
     *
     *  @return The list of handlers.
     */
    public HandlerList getHandlers() {
        return _handlers;
    }

    /** Get the current state of the analyzer.
     *
     *  @return The state.
     */
    public TypeAnalyzerState getState() {
        return _state;
    }

    /** Take a list of Java files as input and type-check all of them.
     *  The result of the type-checking is written to the standard
     *  output.
     *
     *  @param args Command-line arguments, which is a list of Java files.
     *  @exception Exception If any error occurs.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("USAGE:"
                    + " java ptolemy.backtrack.eclipse.ast.TypeAnalyzer"
                    + " [.java files...]");
        } else {
            String[] paths = PathFinder.getPtClassPaths();
            Writer writer = new OutputStreamWriter(System.out);

            for (int i = 0; i < args.length; i++) {
                String fileName = args[i];
                CompilationUnit root = ASTBuilder.parse(fileName);

                TypeAnalyzer analyzer = new TypeAnalyzer(paths);
                root.accept(analyzer);

                ASTDump dump = new ASTDump(writer);
                root.accept(dump);
            }

            writer.close();
        }
    }

    /** Visit an anonymous class declaration and set the current class to
     *  be the {@link Class} object loaded with the same internal name. A
     *  scope is opened for field declarations in it.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(AnonymousClassDeclaration node) {
        Class currentClass = _state.getCurrentClass();

        // First, try to simply append the anonymous count to the end.
        if (!_eclipse_anonymous_scheme) {
            try {
                currentClass = _state.getClassLoader().searchForClass(
                        currentClass.getName() + "$"
                                + _state.nextAnonymousCount());
                _state.nextTotalAnonymousCount();
            } catch (ClassNotFoundException ex) {
                _eclipse_anonymous_scheme = true;
            }
        }

        if (_eclipse_anonymous_scheme) {
            StringBuffer currentName = new StringBuffer(currentClass.getName());
            int dollarPos = currentName.indexOf("$");

            if (dollarPos >= 0) {
                currentName.setLength(dollarPos);
            }

            currentName.append('$');
            currentName.append(_state.nextTotalAnonymousCount());

            try {
                currentClass = _state.getClassLoader().searchForClass(
                        currentName.toString());
                _state.enterClass(currentClass);
            } catch (ClassNotFoundException ex) {
                throw new ASTClassNotFoundException(currentName.toString(), ex);
            }
        }

        _state.enterClass(currentClass);

        // Add the class to be cross-analyzed.
        addCrossAnalyzedType(currentClass.getName());

        // A class declaration starts a new scope.
        _openScope();
        _state.setClassScope();
        _recordFields();

        // Sort body declarations.
        _sortBodyDeclarations(node);

        if (_handlers.hasClassHandler()) {
            for (ClassHandler handler : _handlers.getClassHandlers()) {
                handler.enter(node, _state);
            }
        }

        return super.visit(node);
    }

    /** Visit a block and open a scope for variable declarations in it.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(Block node) {
        _state.enterBlock();
        _openScope();
        return super.visit(node);
    }

    /** Visit an enhanced for statement and open a scope for variable
     *  declarations in it.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(EnhancedForStatement node) {
        _openScope();
        return super.visit(node);
    }

    /** Visit a field declaration.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(FieldDeclaration node) {
        if (_handlers.hasConstructorHandler()) {
            for (ConstructorHandler handler : _handlers
                    .getConstructorHandlers()) {
                handler.enter(node, _state);
            }
        }

        if (_handlers.hasFieldDeclarationHandler()) {
            for (FieldDeclarationHandler handler : _handlers
                    .getFieldDeclarationHandlers()) {
                handler.enter(node, _state);
            }
        }

        return super.visit(node);
    }

    /** Visit a for statement and open a scope for variable declarations
     *  in it.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(ForStatement node) {
        _openScope();
        return super.visit(node);
    }

    /** Override the behavior of visiting an importation declaration.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(ImportDeclaration node) {
        return super.visit(node);
    }

    /** Visit a method declaration and open a scope for variable
     *  declarations in it.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(MethodDeclaration node) {
        _openScope();

        if (_handlers.hasMethodDeclarationHandler()) {
            for (MethodDeclarationHandler handler : _handlers
                    .getMethodDeclarationHandlers()) {
                handler.enter(node, _state);
            }
        }

        return super.visit(node);
    }

    /** Override the behavior of visiting a package declaration.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(PackageDeclaration node) {
        return super.visit(node);
    }

    /** Visit a simple type node and set its type to be the same as the
     *  type associated with its name.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(SimpleType node) {
        if (!(node.getParent() instanceof ClassInstanceCreation)) {
            Class c = _lookupClass(node.getName().toString());
            if (c != null) {
                Type type = Type.createType(c.getName());
                Type.setType(node, type);
            }
        }

        return super.visit(node);
    }

    /** Visit an type declaration and set the current class to be the
     *  {@link Class} object loaded with the same (internal) name. A
     *  scope is opened for field declarations in it.
     *
     *  @param node The node to be visited.
     *  @return The return value of the overridden function.
     */
    public boolean visit(TypeDeclaration node) {
        // Enter the class.
        String typeName = node.getName().getIdentifier();
        Class currentClass = _state.getCurrentClass();

        if (currentClass == null) {
            try {
                currentClass = _state.getClassLoader().searchForClass(typeName);
            } catch (ClassNotFoundException ex) {
                throw new ASTClassNotFoundException(typeName, ex);
            }
        } else {
            if (!_eclipse_anonymous_scheme) {
                try {
                    if (!(node.getParent() instanceof CompilationUnit)
                            && !(node.getParent() instanceof TypeDeclaration)) {
                        currentClass = _state.getClassLoader().searchForClass(
                                currentClass.getName() + "$1" + typeName);
                    } else {
                        currentClass = _state.getClassLoader().searchForClass(
                                currentClass.getName() + "$" + typeName);
                    }

                    typeName = currentClass.getName();
                } catch (ClassNotFoundException e) {
                    _eclipse_anonymous_scheme = true;
                }
            }

            if (_eclipse_anonymous_scheme) {
                try {
                    typeName = currentClass.getName() + "$" + typeName;
                    currentClass = _state.getClassLoader().searchForClass(
                            typeName);
                } catch (ClassNotFoundException ex) {
                    throw new ASTClassNotFoundException(typeName, ex);
                }
            }
        }

        _state.enterClass(currentClass);

        // Add the class to be cross-analyzed.
        addCrossAnalyzedType(currentClass.getName());

        // A class declaration starts a new scope.
        _openScope();
        _state.setClassScope();
        _recordFields();

        // Sort body declarations.
        _sortBodyDeclarations(node);

        if (_handlers.hasClassHandler()) {
            for (ClassHandler handler : _handlers.getClassHandlers()) {
                handler.enter(node, _state);
            }
        }

        // Tell calling function not to visit the children again.
        return super.visit(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the type of a field in a class by its name. If not found
     *  in the class definition, this function also searches the
     *  superclasses of that class, as well as the interfaces that
     *  the class and its superclasses implement.
     *
     *  @param c The class from which the field name is resolved.
     *  @param name The name of the field.
     *  @return The type of the field if found; otherwise, <tt>null</tt>.
     *  @see #_getMethodType(Class, String, Type[])
     */
    protected TypeAndOwner _getFieldTypeAndOwner(Class c, String name) {
        // Try to resolve special field "length" of arrays.
        if (c.isArray() && name.equals("length")) {
            return new TypeAndOwner(Type.INT_TYPE, Type.createType(c.getName()));
        }

        // Find the field with reflection.
        Field field;
        List<Class> workList = new LinkedList<Class>();
        Set<Class> handledSet = new HashSet<Class>();
        workList.add(c);

        while (!workList.isEmpty()) {
            Class topClass = workList.remove(0);

            try {
                field = topClass.getDeclaredField(name);
                return new TypeAndOwner(Type.createType(field.getType()
                        .getName()), Type.createType(topClass.getName()));
            } catch (NoSuchFieldException e1) {
            }

            handledSet.add(topClass);

            Class superClass = topClass.getSuperclass();

            if ((superClass == null)
                    && !topClass.getName().equals("java.lang.Object")) {
                superClass = Object.class;
            }

            if (superClass != null) {
                workList.add(superClass);
            }

            Class[] interfaces = topClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++) {
                if (!handledSet.contains(interfaces[i])) {
                    workList.add(interfaces[i]);
                }
            }
        }

        return null;
    }

    /** Get the type of a method in a class by its name and types of
     *  actural arguments. If not found in the class definition, this
     *  function also searches the superclasses of that class, as well
     *  as the interfaces that the class and its superclasses implement.
     *  <p>
     *  This function always tries to find the best match if multiple
     *  methods with the same name and the same number of arguments are
     *  defined in the class and interface hierarchy. This is
     *  accomplished by computing the compatibility rating between each
     *  pair of formal argument and actural argument, and sum those
     *  numbers together.
     *
     *  @param c The class from which the method is resolved.
     *  @param name The name of the field.
     *  @param args The types of actural arguments for a call.
     *  @return The return type of the method if found; otherwise,
     *   <tt>null</tt>.
     *  @see #_getFieldTypeAndOwner(Class, String)
     *  @see Type#compatibility(Type, ClassLoader)
     */
    protected Type _getMethodType(Class c, String name, Type[] args) {
        Method[] methods = null;
        int best_compatibility = -1;
        Method best_method = null;
        List<Class> workList = new LinkedList<Class>();
        Set<Class> handledSet = new HashSet<Class>();
        workList.add(c);

        while (!workList.isEmpty()) {
            Class topClass = workList.remove(0);
            methods = topClass.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];

                // FIXME: Ignore volatile methods.
                if (Modifier.isVolatile(method.getModifiers())) {
                    continue;
                }

                if (method.getName().equals(name)) {
                    Class[] formalParams = method.getParameterTypes();

                    if (formalParams.length == args.length) {
                        int compatibility = 0;

                        for (int j = 0; j < formalParams.length; j++) {
                            try {
                                Type formalType = Type
                                        .createType(formalParams[j].getName());
                                int comp = args[j].compatibility(formalType,
                                        _state.getClassLoader());

                                if (comp == -1) {
                                    compatibility = -1;
                                    break;
                                } else {
                                    compatibility += comp;
                                }
                            } catch (ClassNotFoundException ex) {
                                // Not exact.
                                throw new ASTClassNotFoundException(args[j], ex);
                            }
                        }

                        if (compatibility == -1) {
                            continue;
                        } else if ((best_compatibility == -1)
                                || (best_compatibility > compatibility)) {
                            best_compatibility = compatibility;
                            best_method = method;

                            if (best_compatibility == 0) { // The best found.
                                break;
                            }
                        }
                    }
                }
            }

            if (best_compatibility == 0) { // The best found.
                break;
            }

            handledSet.add(topClass);

            Class superClass = topClass.getSuperclass();

            if ((superClass == null)
                    && !topClass.getName().equals("java.lang.Object")) {
                superClass = Object.class;
            }

            if ((superClass != null) && !handledSet.contains(superClass)) {
                workList.add(superClass);
            }

            Class[] interfaces = topClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++) {
                if (!handledSet.contains(interfaces[i])) {
                    workList.add(interfaces[i]);
                }
            }
        }

        if (best_compatibility != -1) {
            return Type.createType(best_method.getReturnType().getName());
        } else {
            return null;
        }
    }

    /** Sort the body declarations of an abstract type declaration. The
     *  resulting order conforms to the Java compiler.
     *  <p>
     *  The Java compiler takes a depth-first search to generate code for
     *  classes and members of classes. When inspecting a class, it first
     *  visits all the fields (and their children), then next visits all
     *  the nested classes defined in it (and their children), and finally
     *  visits all the methods (and their children).
     *  <p>
     *  Using a different order to traverse the Eclipse AST does not change
     *  the program semantics, but names of anonymous classes would be
     *  different because different numbers are assigned to them. This
     *  function sorts the declarations in a class in the same order as
     *  the Java compiler uses.
     *
     *  @param node The abstract type declaration whose members are to be
     *   sorted.
     */
    protected static void _sortBodyDeclarations(AbstractTypeDeclaration node) {
        _sortBodyDeclarations(node.bodyDeclarations());
    }

    /** Sort the body declarations of an anonymous class declaration. The
     *  resulting order conforms to the Java compiler.
     *  <p>
     *  The Java compiler takes a depth-first search to generate code for
     *  classes and members of classes. When inspecting a class, it first
     *  visits all the fields (and their children), then next visits all
     *  the nested classes defined in it (and their children), and finally
     *  visits all the methods (and their children).
     *  <p>
     *  Using a different order to traverse the Eclipse AST does not change
     *  the program semantics, but names of anonymous classes would be
     *  different because different numbers are assigned to them. This
     *  function sorts the declarations in a class in the same order as
     *  the Java compiler uses.
     *
     *  @param node The anonymous class declaration whose members are to be
     *   sorted.
     */
    protected static void _sortBodyDeclarations(AnonymousClassDeclaration node) {
        _sortBodyDeclarations(node.bodyDeclarations());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Convert a list of arguments into an array of types of those arguments.
     *  Each argument in the list is an {@link ASTNode} with type associated
     *  to it.
     *
     *  @param arguments The argument list.
     *  @return The array of types of the arguments.
     */
    private Type[] _argumentsToTypes(List arguments) {
        Type[] types = new Type[arguments.size()];
        Iterator argIter = arguments.iterator();

        for (int i = 0; argIter.hasNext(); i++) {
            types[i] = Type.getType((ASTNode) argIter.next());
        }

        return types;
    }

    /** Close the last opened scope. All the variables defined in that scope
     *  are forgotten.
     */
    private void _closeScope() {
        _state.getVariableStack().pop();
    }

    /** Find a variable or field from the current scope.
     *
     *  @param name The name of the variable or field.
     *  @return The variable or field.
     */
    private TypeAndOwner _findVariable(String name) {
        Stack<Hashtable<String, Type>> variableStack = _state
                .getVariableStack();
        int i = variableStack.size() - 1;

        if (i == -1) {
            return null;
        }

        Hashtable<String, Type> table = variableStack.peek();

        while (!table.containsKey(name) && (i >= 1)) {
            table = variableStack.get(--i);
        }

        if (table.containsKey(name)) {
            Type type = table.get(name);
            Integer hashCode = Integer.valueOf(i);
            Class ownerClass = _classScopeRelation.get(hashCode);
            Type owner = (ownerClass == null) ? null : Type
                    .createType(ownerClass.getName());
            return new TypeAndOwner(type, owner);
        } else {
            return null;
        }
    }

    /** Get the simple class name of a class (not including any
     *  "." or "$"). The same function is provided in {@link
     *  Class} class in Java 1.5.
     *
     *  @param c The class object.
     *  @return The simple name of the class object.
     */
    private String _getSimpleClassName(Class c) {
        String name = c.getName();
        int lastSeparator1 = name.lastIndexOf('.');
        int lastSeparator2 = name.lastIndexOf('$');
        int lastSeparator = (lastSeparator1 >= lastSeparator2) ? lastSeparator1
                : lastSeparator2;

        name = name.substring(lastSeparator + 1);

        String newName = name;
        char ch = newName.charAt(0);

        while ((ch >= '0') && (ch <= '9') && (newName.length() > 1)) {
            newName = newName.substring(1);
            ch = newName.charAt(0);
        }

        if (newName.length() == 0) {
            return name;
        } else {
            return newName;
        }
    }

    /** Import a class with its full name, using "." instead
     *  of "$" to separate names of nested classes when they
     *  are referred to. The imported class is added to a
     *  {@link Hashtable} to be looked up in name resolving.
     *
     *  @param classFullName The full name of the class to be
     *   imported.
     */
    private void _importClass(String classFullName) {
        int lastDotPos = classFullName.lastIndexOf('.');
        String simpleName = classFullName.substring(lastDotPos + 1);

        try {
            _importedClasses.put(simpleName, _state.getClassLoader().loadClass(
                    classFullName));
        } catch (ClassNotFoundException ex) {
            throw new ASTClassNotFoundException(classFullName, ex);
        }
    }

    /** Lookup a class with a partially given name as may
     *  appear in Java source code. The name may be relative
     *  to the current class and its enclosing classes. It
     *  may also be a full name.
     *
     *  @param partialSimpleName The partially given class
     *   name.
     *  @return The class; <tt>null</tt> is returned if the
     *   class cannot be found.
     */
    private Class _lookupClass(String partialSimpleName) {
        int dotPos = partialSimpleName.indexOf('.');
        String simpleName;

        if (dotPos == -1) {
            simpleName = partialSimpleName;
        } else {
            simpleName = partialSimpleName.substring(0, dotPos);
        }

        Class result = null;

        if (result == null) {
            Stack<Hashtable<String, Class>> previousClasses = _state
                    .getPreviousClasses();
            int previousNumber = previousClasses.size();

            for (int i = previousNumber; i >= 0; i--) {
                Class workingClass = null;

                if (i == previousNumber) {
                    workingClass = _state.getCurrentClass();
                } else {
                    Hashtable<String, Class> previousTable = previousClasses
                            .get(i);

                    if (previousTable != null) {
                        if (previousTable instanceof CurrentClassElement) {
                            workingClass = ((CurrentClassElement) previousTable)
                                    .getCurrentClassElement();
                        } else if (previousTable.keySet().contains(simpleName)) {
                            result = previousTable.get(simpleName);
                            break;
                        }
                    }
                }

                if (workingClass != null) {
                    if (_getSimpleClassName(workingClass).equals(simpleName)) {
                        result = workingClass;
                        break;
                    }

                    Class[] declaredClasses = workingClass.getDeclaredClasses();

                    for (int j = 0; j < declaredClasses.length; j++) {
                        if (_getSimpleClassName(declaredClasses[j]).equals(
                                simpleName)) {
                            result = declaredClasses[j];
                            break;
                        }
                    }

                    if (result != null) {
                        break;
                    }
                }
            }
        }

        // Look for imported classes.
        if ((result == null) && _importedClasses.containsKey(simpleName)) {
            result = _importedClasses.get(simpleName);
        }

        // A result is found for simpleName.
        if ((result != null) && (dotPos >= 0)) {
            try {
                result = _state.getClassLoader().loadClass(
                        result.getName() + partialSimpleName.substring(dotPos));
            } catch (ClassNotFoundException e) {
                result = null;
            }
        }

        // Fall back to simple class loader.
        if (result == null) {
            try {
                result = _state.getClassLoader().searchForClass(
                        partialSimpleName);
            } catch (ClassNotFoundException e) {
            }
        }

        return result;
    }

    /** Open a new scope.
     */
    private void _openScope() {
        _state.getVariableStack().push(new Hashtable<String, Type>());
    }

    /** Record all the fields of the currently inspected class
     *  in the variable table on the top of the variable stack.
     *
     *  @see #_unrecordFields()
     */
    private void _recordFields() {
        Class c = _state.getCurrentClass();
        Hashtable<String, Type> table = _state.getVariableStack().peek();
        Integer hashCode = Integer.valueOf(_state.getVariableStack().size() - 1);
        _classScopeRelation.put(hashCode, c);

        while (c != null) {
            Field[] fields = c.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                // FIXME: Ignore volatile fields.
                if (Modifier.isVolatile(field.getModifiers())) {
                    continue;
                }

                String fieldName = field.getName();

                if (!table.containsKey(fieldName)) {
                    Class fieldType = field.getType();
                    table.put(fieldName, Type.createType(fieldType.getName()));
                }
            }

            Class[] interfaces = c.getInterfaces();

            for (int i = 0; i < interfaces.length; i++) {
                fields = interfaces[i].getDeclaredFields();

                for (int j = 0; j < fields.length; j++) {
                    String fieldName = fields[j].getName();

                    if (!table.containsKey(fieldName)) {
                        Class fieldType = fields[j].getType();
                        table.put(fieldName, Type.createType(fieldType
                                .getName()));
                    }
                }
            }

            Class superclass = c.getSuperclass();

            if ((superclass == null) && !c.getName().equals("java.lang.Object")) {
                superclass = Object.class;
            }

            c = superclass;
        }
    }

    /** Resolve a method in the given class with an array of arguments.
     *
     *  @param owner The class where the method belongs to. If it is null,
     *   the current class is assumed, and all the enclosing classes are
     *   searched, if necessary.
     *  @param methodName The name of the method.
     *  @param arguments The array of arguments.
     *  @return The return type and the owner of the method, or <tt>null</tt>
     *   if the method cannot be found.
     *  @see #_getMethodType(Class, String, Type[])
     */
    private TypeAndOwner _resolveMethod(Class owner, String methodName,
            Type[] arguments) {
        Set<Class> handledSet = new HashSet<Class>();
        Stack<Hashtable<String, Class>> previousClasses = _state
                .getPreviousClasses();
        int previousNum = previousClasses.size();
        Class oldOwner = owner;

        if (owner == null) {
            owner = _state.getCurrentClass();
        }

        do {
            Type type = _getMethodType(owner, methodName, arguments);

            if (type != null) {
                return new TypeAndOwner(type, Type.createType(owner.getName()));
            }

            handledSet.add(owner);
            owner = null;

            if (oldOwner == null) {
                while ((owner == null) && (previousNum > 0)) {
                    previousNum--;

                    Hashtable<String, Class> previousTable = previousClasses
                            .get(previousNum);

                    if (previousTable != null
                            && previousTable instanceof CurrentClassElement) {
                        owner = ((CurrentClassElement) previousTable)
                                .getCurrentClassElement();

                        if (handledSet.contains(owner)) {
                            owner = null;
                        }
                    }
                }
            }
        } while (owner != null);

        return null;
    }

    /** Resolve a simple name within the scope of the given type. The name can
     *  be a variable name, a field name, or a class relative to that type. If
     *  the name corresponds to a field of a class, its owner is encoded in the
     *  return object.
     *
     *  @param name The simple name to be resolved.
     *  @param lastType The type from which the name is resolved. It is used as
     *   the scope for the name. If it is null, the name is resolved in the
     *   current scope.
     *  @return The type and owner of the name. If the name cannot be found,
     *   <tt>null</tt> is returned.
     */
    private TypeAndOwner _resolveName(String name, Type lastType) {
        // Not in a class yet.
        if (_state.getCurrentClass() == null) {
            return null;
        }

        if (lastType == null) {
            TypeAndOwner varTypeAndOwner = _findVariable(name);

            if (varTypeAndOwner != null) {
                return varTypeAndOwner;
            }

            Class c = _lookupClass(name);

            if (c != null) {
                return new TypeAndOwner(Type.createType(c.getName()), null);
            }
        }

        TypeAndOwner typeAndOwner;

        if (lastType == null) {
            typeAndOwner = _resolveNameFromClass(_state.getCurrentClass(), name);
        } else {
            try {
                typeAndOwner = _resolveNameFromClass(lastType.toClass(_state
                        .getClassLoader()), name);
            } catch (ClassNotFoundException ex) {
                throw new ASTClassNotFoundException(lastType, ex);
            }
        }

        if ((typeAndOwner == null) && (lastType == null)) {
            Stack<Hashtable<String, Class>> previousClasses = _state
                    .getPreviousClasses();
            int previousNumber = previousClasses.size() - 1;

            while ((typeAndOwner == null) && (previousNumber >= 0)) {
                Hashtable<String, Class> previousTable = previousClasses
                        .get(previousNumber--);

                if (previousTable != null
                        && previousTable instanceof CurrentClassElement) {
                    Class previousClass = ((CurrentClassElement) previousTable)
                            .getCurrentClassElement();

                    if (previousClass != null) {
                        typeAndOwner = _resolveNameFromClass(previousClass,
                                name);
                    }
                }
            }
        }

        return typeAndOwner;
    }

    /** Resolve a name from a class. The name can be a field name, or the
     *  name of a class nested in the given class. If the name corresponds
     *  to a field, its owner (the type it belongs to) is encoded in the
     *  return object.
     *
     *  @param owner The class to be searched.
     *  @param name The name to search for.
     *  @return The type and owner of the field or class if found, or
     *   <tt>null</tt> if not found. For nested classes, the owner is always
     *   <tt>null</tt>.
     */
    private TypeAndOwner _resolveNameFromClass(Class owner, String name) {
        // Try to get the field.
        TypeAndOwner typeAndOwner = _getFieldTypeAndOwner(owner, name);

        if (typeAndOwner != null) {
            return typeAndOwner;
        }

        // Try class name resolution.
        try {
            Class c = _state.getClassLoader().searchForClass(
                    new StringBuffer(name), owner);
            return new TypeAndOwner(Type.createType(c.getName()), null);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /** Sort a list or body declarations according to the Java compiler
     *  convention.
     *
     *  @param bodyDeclarations The list of body declarations.
     *  @see #_sortBodyDeclarations(AbstractTypeDeclaration)
     *  @see #_sortBodyDeclarations(AnonymousClassDeclaration)
     */
    private static void _sortBodyDeclarations(
            List<BodyDeclaration> bodyDeclarations) {
        int size = bodyDeclarations.size();
        BodyDeclaration[] bodyArray = bodyDeclarations
                .toArray(new BodyDeclaration[size]);
        Arrays.sort(bodyArray, new Comparator<BodyDeclaration>() {
            public int compare(BodyDeclaration bodyDeclaration1,
                    BodyDeclaration bodyDeclaration2) {
                if (bodyDeclaration1.getClass().isInstance(bodyDeclaration2)) {
                    return 0;
                }

                Class[] classes = new Class[] { FieldDeclaration.class,
                        TypeDeclaration.class, MethodDeclaration.class,
                        Initializer.class };

                for (int i = 0; i < classes.length; i++) {
                    if (classes[i].isInstance(bodyDeclaration1)) {
                        for (int j = i + 1; j < classes.length; j++) {
                            if (classes[j].isInstance(bodyDeclaration2)) {
                                return -1;
                            }
                        }
                    }
                }

                return 1;
            }
        });
        bodyDeclarations.clear();
        bodyDeclarations.addAll(Arrays.asList(bodyArray));
    }

    /** Undo the recording of all the fields of the currently inspected
     *  class.
     *
     *  @see #_recordFields()
     */
    private void _unrecordFields() {
        Integer hashCode = Integer.valueOf(_state.getVariableStack().size() - 1);
        _classScopeRelation.remove(hashCode);
    }

    ///////////////////////////////////////////////////////////////////
    //// TypeAndOwner

    /** A relation between classes and their scopes. Keys are {@link
     *  Integer} objects of the hash code of scopes ({@link Hashtable});
     *  values are {@link Class} objects.
     */
    private Hashtable<Integer, Class> _classScopeRelation = new Hashtable<Integer, Class>();

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** Whether the anonymous class naming scheme conforms to eclipse.
     */
    private boolean _eclipse_anonymous_scheme = false;

    /** The list of handlers to be called back when traversing the AST.
     */
    private HandlerList _handlers = new HandlerList();

    /** Table of all the explicitly imported classes (not including
     *  package importations). Keys are class names; values are {@link
     *  Class} objects.
     */
    private Hashtable<String, Class> _importedClasses = new Hashtable<String, Class>();

    /** The current state of the analyzer.
     */
    private TypeAnalyzerState _state = new TypeAnalyzerState(this);

    /**
     The return of name resolution that includes a type of the resolved name
     and a type of its owner (the class that owns that name). The owner may
     be <tt>null</tt> when that name does not belong to any type (e.g., a
     local variable).

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class TypeAndOwner {
        /** Construct a type and owner tuple.
         *
         *  @param type The type.
         *  @param owner The owner.
         */
        TypeAndOwner(Type type, Type owner) {
            _type = type;
            _owner = owner;
        }

        /** Get the owner.
         *
         *  @return The owner.
         */
        Type _getOwner() {
            return _owner;
        }

        /** Get the type.
         *
         *  @return The type.
         */
        Type _getType() {
            return _type;
        }

        /** The owner.
         */
        private Type _owner;

        /** The type.
         */
        private Type _type;
    }
}
