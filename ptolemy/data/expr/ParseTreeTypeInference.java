/* A visitor for parse trees of the expression language that infers types.

 Copyright (c) 1998-2014 The Regents of the University of California.
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


 */
package ptolemy.data.expr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FixType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.math.Precision;

///////////////////////////////////////////////////////////////////
//// ParseTreeTypeInference

/**
 This class visits parse trees and infers a type for each node in the
 parse tree.  This type is stored in the parse tree.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeTypeInference extends AbstractParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Infer the type of the parse tree with the specified root node.
     *  @param node The root of the parse tree.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public Type inferTypes(ASTPtRootNode node) throws IllegalActionException {
        node.visit(this);
        return _inferredChildType;
    }

    /** Infer the type of the parse tree with the specified root node using
     *  the specified scope to resolve the values of variables.
     *  @param node The root of the parse tree.
     *  @param scope The scope for evaluation.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    public Type inferTypes(ASTPtRootNode node, ParserScope scope)
            throws IllegalActionException {
        _scope = scope;
        node.visit(this);
        _scope = null;
        return _inferredChildType;
    }

    /** Set the type of the given node to be an ArrayType that is the
     *  least upper bound of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        _setType(node, new ArrayType((Type) TypeLattice.lattice()
                .leastUpperBound(new HashSet<Type>(Arrays.asList(childTypes))),
                childTypes.length));
    }

    /** Set the type of the given node to be the type that is the
     *  least upper bound of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        // FIXME: not consistent with expression evaluator.
        _setType(
                node,
                (Type) TypeLattice.lattice().leastUpperBound(
                        new HashSet<Type>(Arrays.asList(childTypes))));
    }

    /** Set the type of the given node to be the return type of the
     *  function determined for the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        int argCount = node.jjtGetNumChildren() - 1;
        final String functionName = node.getFunctionName();

        // Get the child types.
        Type[] childTypes = new Type[argCount];

        for (int i = 0; i < argCount; i++) {
            childTypes[i] = _inferChild(node, i + 1);

            if (childTypes[i] == null) {
                throw new RuntimeException("node " + node + " has null type.");
            }
        }

        Type type = null;
        Type baseType = null;

        if (_scope != null && functionName != null) {
            type = _scope.getType(functionName);
            if (!(type instanceof ObjectType)) {
                // Pretend that we cannot resolve the type if it is an
                // ObjectType.
                baseType = type;
            }
        }

        if (baseType != null || functionName == null) {
            baseType = _inferChild(node, 0);

            // Handle as an array or matrix index into a named
            // variable reference.
            if (baseType instanceof FunctionType) {
                _setType(node, ((FunctionType) baseType).getReturnType());
                return;
            } else if (argCount == 1) {
                if (baseType instanceof ArrayType) {
                    _setType(node, ((ArrayType) baseType).getElementType());
                    return;
                } else {
                    // Child node is not an array, but it can be
                    // losslessly converted to an array (anything can be).
                    // Note: parse tree evaluator also support
                    // constructs like (1)(0), where the constant 1
                    // gets converted automatically to an array.
                    ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(0);
                    _setType(child, new ArrayType(baseType));
                    return;
                }
            } else if (argCount == 2) {
                if (baseType instanceof MatrixType) {
                    _setType(node, ((MatrixType) baseType).getElementType());
                    return;
                } else {
                    // Child node is not a matrix, but it might be
                    // losslessly convertible to a matrix.  Attempt to do that.
                    // Note: parse tree evaluator also support
                    // constructs like (1)(0,0), where the constant 1
                    // gets converted automatically to a matrix.
                    ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(0);
                    _setType(child,
                            MatrixType.getMatrixTypeForElementType(baseType));
                    return;
                }
            }

            throw new IllegalActionException("Wrong number of indices "
                    + "when referencing function \"" + functionName
                    + "\".  The number of indices was " + argCount
                    + ". For arrays, the number of indices should be "
                    + "1. For matrices, the number of indices should be 2. "
                    + " The type of the function was \"" + baseType + "\".");
        }

        // Note that an alternative to searching for functions by name
        // is to define fooReturnType() in data.expr.UtilityFunctions
        // so that fooReturnType() is found by CacheMethod.
        // Psuedo-temporary hack for casts....
        if (functionName.compareTo("cast") == 0 && argCount == 2) {
            ASTPtRootNode castTypeNode = (ASTPtRootNode) node
                    .jjtGetChild(0 + 1);
            ParseTreeEvaluator parseTreeEvaluator = new ParseTreeEvaluator();

            try {
                ptolemy.data.Token t = parseTreeEvaluator.evaluateParseTree(
                        castTypeNode, _scope);
                _setType(node, t.getType());
            } catch (IllegalActionException ex) {
                _setType(node, childTypes[0]);
            }

            return;

            // Note: We used to just do this, but in some case is it
            // useful to have functions which are type constructors...
            // Hence the above code.
            //  _setType(node,
            //     ((ASTPtRootNode) node.jjtGetChild(0 + 1)).getType());
            //  return;
        }

        // A hack, because the result of the 'fix' function is
        // dependent on its arguments, which should be constant.
        if (functionName.compareTo("fix") == 0 && argCount == 3) {
            ASTPtRootNode lengthNode = (ASTPtRootNode) node.jjtGetChild(1 + 1);
            ASTPtRootNode integerBitsNode = (ASTPtRootNode) node
                    .jjtGetChild(2 + 1);
            ParseTreeEvaluator parseTreeEvaluator = new ParseTreeEvaluator();

            try {
                ptolemy.data.Token length = parseTreeEvaluator
                        .evaluateParseTree(lengthNode, _scope);

                ptolemy.data.Token integerBits = parseTreeEvaluator
                        .evaluateParseTree(integerBitsNode, _scope);
                _setType(
                        node,
                        new FixType(new Precision(((ScalarToken) length)
                                .intValue(), ((ScalarToken) integerBits)
                                .intValue())));
                return;
            } catch (Throwable throwable) {
                // Do nothing... rely on the regular method resolution
                // to generate the right type.
            }
        }

        if (functionName.compareTo("eval") == 0) {
            // We can't infer the type of eval expressions...
            _setType(node, BaseType.GENERAL);
            return;
        }

        if (functionName.compareTo("matlab") == 0) {
            // We can't infer the type of matlab expressions...
            _setType(node, BaseType.GENERAL);
            return;
        }

        if (functionName.compareTo("fold") == 0) {
            if (argCount == 3) {
                if (childTypes[0] instanceof FunctionType) {
                    FunctionType function = (FunctionType) childTypes[0];
                    if (function.getArgCount() != 2) {
                        throw new IllegalActionException(
                                "The first argument "
                                        + "to the function \"fold\" must be a function "
                                        + "that accepts two arguments.");
                    }
                    if (!function.getArgType(0).isCompatible(childTypes[1])) {
                        throw new IllegalActionException("The second "
                                + "argument of the function \"fold\" is not "
                                + "compatible with the first parameter to the "
                                + "function provided to \"fold\".");
                    }
                    // Do not check the type of the second argument of the
                    // function, because if the collection is a Java collection,
                    // then it is impossible to infer the types of the elements
                    // in it without actually knowing the collection.
                    _setType(node, function.getReturnType());
                    return;
                }
            }

            throw new IllegalActionException("The function \"fold\" is "
                    + "a higher-order function that takes exactly 3 "
                    + "arguments. The first argument must be a function "
                    + "that takes 2 arguments. The second must be a value "
                    + "that can be passed to the function as its first "
                    + "argument. The third must be a list of values that "
                    + "can be passed to the function as its second "
                    + "argument.");
        }

        if (functionName.equals("object") && argCount == 1) {
            ASTPtRootNode classNameNode = (ASTPtRootNode) node.jjtGetChild(1);
            if (classNameNode instanceof ASTPtLeafNode) {
                ptolemy.data.Token token = ((ASTPtLeafNode) classNameNode)
                        .getToken();
                if (token != null && token instanceof StringToken) {
                    String className = ((StringToken) token).stringValue();
                    try {
                        Class clazz = Class.forName(className);
                        _setType(node, new ObjectType(clazz));
                        return;
                    } catch (ClassNotFoundException e) {
                        throw new IllegalActionException(
                                "Unable to load class " + className);
                    }
                } else if (token == null) {
                    _setType(node, new ObjectType(Object.class));
                    return;
                }
            }
        }

        // Otherwise, try to reflect the method name.
        CachedMethod cachedMethod;

        try {
            cachedMethod = CachedMethod.findMethod(functionName, childTypes,
                    CachedMethod.FUNCTION);
        } catch (Exception ex) {
            // Deal with what happens if the method is not found.
            // FIXME: hopefully this is monotonic???
            _setType(node, BaseType.UNKNOWN);
            return;
        }

        if (cachedMethod.isValid()) {
            baseType = cachedMethod.getReturnType();
            _setType(node, baseType);
            return;
        }

        if (type instanceof ObjectType) {
            // If it is ObjectType, set it here.
            _setType(node, type);
            return;
        }

        // If we reach this point it means the function was not found on the
        // search path.
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < childTypes.length; i++) {
            if (i == 0) {
                buffer.append(childTypes[i].toString());
            } else {
                buffer.append(", " + childTypes[i].toString());
            }
        }

        throw new IllegalActionException("No matching function " + functionName
                + "( " + buffer + " ).");
    }

    /** Set the type of the given node to be a function type whose
     *  argument types are determined by the children of the node.
     *  The return type of the function type is determined by
     *  inferring the type of function's expression in a scope that
     *  adds identifiers for each argument to the current scope.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        final Map map = new HashMap();

        for (int i = 0; i < node._argTypes.length; i++) {
            map.put(node.getArgumentNameList().get(i),
                    node.getArgumentTypes()[i]);
        }

        // Push the current scope.
        final ParserScope currentScope = _scope;
        ParserScope functionScope = new ParserScope() {
            @Override
            public ptolemy.data.Token get(String name) {
                return null;
            }

            @Override
            public Type getType(String name) throws IllegalActionException {
                Type type = (Type) map.get(name);

                if (type == null && currentScope != null) {
                    return currentScope.getType(name);
                } else {
                    return type;
                }
            }

            @Override
            public InequalityTerm getTypeTerm(String name)
                    throws IllegalActionException {
                Type type = (Type) map.get(name);

                if (type == null && currentScope != null) {
                    return currentScope.getTypeTerm(name);
                } else {
                    return new TypeConstant(type);
                }
            }

            @Override
            public Set identifierSet() throws IllegalActionException {
                Set set = currentScope.identifierSet();
                set.addAll(map.keySet());
                return set;
            }
        };

        _scope = functionScope;
        node.getExpressionTree().visit(this);

        Type returnType = _inferredChildType;
        FunctionType type = new FunctionType(node._argTypes, returnType);
        _setType(node, type);
        _scope = currentScope;
        return;
    }

    /** Set the type of the given node to be the least upper bound of
     *  the types of the two branches of the if.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        Type conditionalType = _inferChild(node, 0);

        if (conditionalType != BaseType.BOOLEAN) {
            throw new IllegalActionException(
                    "Functional-if must branch on a boolean, "
                            + "but instead type was " + conditionalType);
        }

        Type trueType = _inferChild(node, 1);
        Type falseType = _inferChild(node, 2);

        _setType(
                node,
                (Type) TypeLattice.lattice().leastUpperBound(trueType,
                        falseType));
    }

    /** Set the type of the given node to be the type of constant the
     *  variable refers to, if the node represents a constant, or the
     *  type of the identifier the node refers to in the current
     *  scope.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error
     *  occurs, or an identifier is not bound in the current scope.
     */
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _setType(node, node.getToken().getType());
            return;
        }

        String name = node.getName();

        Type type = null;
        if (_scope != null) {
            type = _scope.getType(name);

            if (type != null && !(type instanceof ObjectType)) {
                // Pretend that we cannot resolve the type if it is an
                // ObjectType.
                _setType(node, type);
                return;
            }
        }

        // Look up for constants.
        if (Constants.get(name) != null) {
            // A named constant that is recognized by the parser.
            _setType(node, Constants.get(name).getType());
            return;
        }

        if (type != null) {
            _setType(node, type);
            return;
        }

        throw new IllegalActionException("The ID " + name + " is undefined.");
    }

    /** Set the type of the given node to be boolean.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _inferAllChildren(node);

        // FIXME: check arguments are valid?
        _setType(node, BaseType.BOOLEAN);
    }

    /** Set the type of the given node to be an MatrixType based on the
     *  least upper bound of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        Type elementType = (Type) TypeLattice.lattice().leastUpperBound(
                new HashSet<Type>(Arrays.asList(childTypes)));

        Type matrixType = MatrixType.getMatrixTypeForElementType(elementType);
        _setType(node, matrixType);
    }

    /** Set the type of the given node to be the return type of the
     *  method determined for the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        
        Type[] childTypes = _inferAllChildren(node);

        // Handle indexing into a record.
        if (childTypes.length == 1 && childTypes[0] instanceof RecordType) {
            RecordType type = (RecordType) childTypes[0];

            if (type.labelSet().contains(node.getMethodName())) {
                _setType(node, type.get(node.getMethodName()));
                return;
            }
        }
        
        // Fix to Kepler bug #6629:
        // Depending on the order in which type constraints are satisfied,
        // some child types may remain UNKNOWN for one or more iterations
        // of Mogensen's algorithm. Hence, keep the type of the node
        // to UNKNOWN until the types for the children are resolved.
        if (childTypes.length == 1 && childTypes[0] == BaseType.UNKNOWN) {
            _setType(node, BaseType.UNKNOWN);
        } else {
            _setType(node, _methodCall(node.getMethodName(), childTypes));
        }
    }

    /** Set the type of the given node to be the type of the first
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        // FIXME: Check that exponents are valid??
        Type baseType = childTypes[0];
        _setType(node, baseType);
    }

    /** Set the type of the given node to be the least upper bound
     *  type of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        Type resultType = childTypes[0];
        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);
            Type nextType = childTypes[i];
            if (operator.kind == PtParserConstants.MULTIPLY) {
                resultType = resultType.multiply(nextType);
            } else if (operator.kind == PtParserConstants.DIVIDE) {
                resultType = resultType.divide(nextType);
            } else if (operator.kind == PtParserConstants.MODULO) {
                resultType = resultType.modulo(nextType);
            } else {
                _assert(false, node, "Invalid operation");
            }
        }
        _setType(node, resultType);
    }

    /** Set the type of the given node to be a record token that
     *  contains fields for each name in the record construction,
     *  where the type of each field in the record is determined by
     *  the corresponding type of the child nodes.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        String[] names = (String[]) node.getFieldNames().toArray(
                new String[node.jjtGetNumChildren()]);

        _setType(node, new RecordType(names, childTypes));
    }

    /** Set the type of the given node to be boolean.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        /* Type[] childTypes = */_inferAllChildren(node);

        // FIXME: Check args are booleans?
        _setType(node, BaseType.BOOLEAN);
    }

    /** Set the type of the given node to be the type of the first
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        // FIXME: Check others are scalars?
        Type baseType = childTypes[0];
        _setType(node, baseType);
    }

    /** Set the type of the given node to be the least upper bound
     *  type of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        Type resultType = childTypes[0];
        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);
            Type nextType = childTypes[i];
            if (operator.kind == PtParserConstants.PLUS) {
                resultType = resultType.add(nextType);
            } else if (operator.kind == PtParserConstants.MINUS) {
                resultType = resultType.subtract(nextType);
            } else {
                _assert(false, node, "Invalid operation");
            }
        }
        _setType(node, resultType);
    }

    /** Set the type of the given node to be the type of the
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        Type[] childTypes = _inferAllChildren(node);
        Type baseType = childTypes[0];
        if (node.isMinus()) {
            _setType(node, baseType.zero().subtract(baseType));
        } else {
            _setType(node, baseType);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Assert that the given boolean value, which describes the given
     * parse tree node is true.  If it is false, then throw a new
     * InternalErrorException that describes the node that includes
     * the given message.
     * @param flag If false, then throw an InternalErrorException
     * @param node The node
     * @param message The message included in the exception
     */
    protected void _assert(boolean flag, ASTPtRootNode node, String message) {
        if (!flag) {
            throw new InternalErrorException(message + ": " + node.toString());
        }
    }

    /** Get the return type of a method belonging to the specified class, or the
     *  type of a field belonging to it.
     */
    protected Type _getMethodReturnType(Class<?> clazz, String methodName,
            Type[] argTypes) throws IllegalActionException {
        Class<?> result = null;

        if (argTypes.length == 1) {
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                if (field.getName().equals(methodName)
                        && Modifier.isPublic(field.getModifiers())) {
                    result = field.getType();
                    break;
                }
            }
        }

        Method[] methods = clazz.getMethods();
        int argCount = argTypes.length - 1;
        for (Method method : methods) {
            if (method.getName().equals(methodName)
                    && Modifier.isPublic(method.getModifiers())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != argCount) {
                    continue;
                }
                boolean compatible = true;
                for (int i = 0; compatible && i < argCount; i++) {
                    Class<?> argumentType = ConversionUtilities
                            .convertTokenTypeToJavaType(argTypes[i + 1]);
                    if (!parameterTypes[i].isAssignableFrom(argumentType)) {
                        compatible = false;
                    }
                }
                if (compatible) {
                    result = method.getReturnType();
                    break;
                }
            }
        }

        if (result == null) {
            return null;
        } else {
            if (Variable.class.isAssignableFrom(result)) {
                return BaseType.UNKNOWN;
            } else {
                return ConversionUtilities.convertJavaTypeToTokenType(result);
            }
        }
    }

    /** Return the type of the identifier with the given name.
     *  @exception IllegalActionException If the identifier is undefined.
     */
    protected Type _getTypeForName(String name) throws IllegalActionException {
        Type type = null;
        if (_scope != null) {
            type = _scope.getType(name);

            if (type != null && !(type instanceof ObjectType)) {
                // Pretend that we cannot resolve the type if it is an
                // ObjectType.
                return type;
            }
        }

        // Look up for constants.
        if (Constants.get(name) != null) {
            // A named constant that is recognized by the parser.
            return Constants.get(name).getType();
        }

        if (type != null) {
            return type;
        }

        throw new IllegalActionException("The ID " + name + " is undefined.");

        //        return BaseType.GENERAL;
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     */
    protected Type[] _inferAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        Type[] types = new Type[node.jjtGetNumChildren()];
        int numChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numChildren; i++) {
            _inferChild(node, i);

            Type type = _inferredChildType;

            if (type == null) {
                throw new RuntimeException("node " + node.jjtGetChild(i)
                        + " has no type.");
            }

            types[i] = type;
        }

        return types;
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     */
    protected Type _inferChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        child.visit(this);
        return _inferredChildType;
    }

    /** Test if the given identifier is valid.
     */
    protected boolean _isValidName(String name) throws IllegalActionException {
        if (_scope != null) {
            try {
                return _scope.getType(name) != null;
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    /** Infer the type of the specified method.  The type of the object on which
     *  the method is evaluated should be the first argument.
     *  @param methodName The method name.
     *  @param argTypes An array of argument types.
     *  @exception IllegalActionException If an evaluation error occurs.
     *  @see ParseTreeEvaluator#_methodCall(String, Type[], Object[])
     */
    protected Type _methodCall(String methodName, Type[] argTypes)
            throws IllegalActionException {
        CachedMethod cachedMethod = CachedMethod.findMethod(methodName,
                argTypes, CachedMethod.METHOD);

        if (cachedMethod.isValid()) {
            Type type = cachedMethod.getReturnType();
            return type;
        }

        if (argTypes[0] instanceof ObjectType) {
            Object object = ((ObjectType) argTypes[0]).getValue();
            if (object != null) {
                if (object instanceof NamedObj) {
                    Object result = ((NamedObj) object)
                            .getAttribute(methodName);
                    if (result == null && object instanceof Entity) {
                        result = ((Entity) object).getPort(methodName);
                    }
                    if (result == null && object instanceof CompositeEntity) {
                        result = ((CompositeEntity) object)
                                .getEntity(methodName);
                        if (result == null) {
                            result = ((CompositeEntity) object)
                                    .getRelation(methodName);
                        }
                    }

                    if (result == null) {
                        List attributes = ((NamedObj) object)
                                .attributeList(ContainmentExtender.class);
                        Iterator attrIterator = attributes.iterator();
                        while (result == null && attrIterator.hasNext()) {
                            ContainmentExtender extender = (ContainmentExtender) attrIterator
                                    .next();
                            result = extender.getContainedObject(methodName);
                        }
                    }
                    if (result != null) {
                        if (result instanceof Variable) {
                            return ((Variable) result).getType();
                        } else {
                            return new ObjectType(result, result.getClass());
                        }
                    }
                }
            }

            Class<?> valueClass = ((ObjectType) argTypes[0]).getValueClass();
            if (valueClass == null) {
                valueClass = Object.class;
            }
            Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(valueClass);
            while (!classes.isEmpty()) {
                Iterator<Class<?>> iterator = classes.iterator();
                valueClass = iterator.next();
                iterator.remove();

                if (!Modifier.isPublic(valueClass.getModifiers())) {
                    for (Class<?> interf : valueClass.getInterfaces()) {
                        classes.add(interf);
                    }
                    Class<?> superclass = valueClass.getSuperclass();
                    if (superclass != null) {
                        classes.add(superclass);
                    }
                } else {
                    Type result = _getMethodReturnType(valueClass, methodName,
                            argTypes);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        // If we reach this point it means the function was not found on
        // the search path.
        StringBuffer buffer = new StringBuffer();

        for (int i = 1; i < argTypes.length; i++) {
            if (i == 1) {
                buffer.append(argTypes[i].toString());
            } else {
                buffer.append(", " + argTypes[i].toString());
            }
        }

        throw new IllegalActionException("No matching method " + argTypes[0]
                + "." + methodName + "( " + buffer + " ).");
    }

    protected void _setType(ASTPtRootNode node, Type type) {
        //      System.out.println("type of " + node + " is " + type);
        _inferredChildType = type;
        node.setType(type);
    }

    protected Type _inferredChildType;

    protected ParserScope _scope;
}
