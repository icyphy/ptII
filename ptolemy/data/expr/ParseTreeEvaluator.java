/* A visitor for parse trees of the expression language.

 Copyright (c) 1998-2014 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 */
package ptolemy.data.expr;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.BitwiseOperationToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.PartiallyOrderedToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.UnionToken;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ParseTreeEvaluator

/**
 This class evaluates a parse tree given a reference to its root node.
 It implements a visitor that visits the parse tree in depth-first order,
 evaluating each node and storing the result as a token in the node.
 Two exceptions are logic nodes and the ternary if node (the ? : construct),
 which do not necessarily evaluate all children nodes.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeEvaluator extends AbstractParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the parse tree with the specified root node.
     *  @param node The root of the parse tree.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node)
            throws IllegalActionException {
        return evaluateParseTree(node, null);
    }

    /** Evaluate the parse tree with the specified root node using
     *  the specified scope to resolve the values of variables.
     *  @param node The root of the parse tree.
     *  @param scope The scope for evaluation.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
            ParserScope scope) throws IllegalActionException {
        _scope = scope;

        // Evaluate the value of the root node.
        node.visit(this);

        // and return it.
        _scope = null;
        return _evaluatedChildToken;
    }

    /** Trace the evaluation of the parse tree with the specified root
     *  node using the specified scope to resolve the values of
     *  variables.
     *  @param node The root of the parse tree.
     *  @param scope The scope for evaluation.
     *  @return The trace of the evaluation.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    public String traceParseTreeEvaluation(ASTPtRootNode node, ParserScope scope)
            throws IllegalActionException {
        _scope = scope;
        _trace = new StringBuffer();
        _depth = 0;
        _traceEnter(node);

        try {
            // Evaluate the value of the root node.
            node.visit(this);
            _traceLeave(node);
        } catch (Exception ex) {
            // If an exception occurs, then bind the exception into
            // the trace and return the trace.
            _trace(ex.toString());
        }

        _scope = null;

        // Return the trace.
        String trace = _trace.toString();
        _trace = null;
        return trace;
    }

    /** Construct an ArrayToken that contains the tokens from the
     *  children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        if (tokens.length == 0) {
            _evaluatedChildToken = ArrayToken.NIL;
            node.setToken(_evaluatedChildToken);
            return;
        }

        int numChildren = node.jjtGetNumChildren();

        // Convert up to LUB.
        ptolemy.data.type.Type elementType = tokens[0].getType();

        for (int i = 0; i < numChildren; i++) {
            Type valueType = tokens[i].getType();

            if (!elementType.equals(valueType)) {
                elementType = TypeLattice.leastUpperBound(elementType,
                        valueType);
            }
        }

        for (int i = 0; i < numChildren; i++) {
            tokens[i] = elementType.convert(tokens[i]);
        }

        _evaluatedChildToken = new ArrayToken(elementType, tokens);

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Evaluate a bitwise operator on the children of the specified
     *  node, where the particular operator is property of the node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        ptolemy.data.Token result = tokens[0];

        if (!(result instanceof BitwiseOperationToken)) {
            throw new IllegalActionException("Operation "
                    + node.getOperator().image + " not defined on " + result
                    + " which does not support bitwise operations.");
        }

        BitwiseOperationToken bitwiseResult = (BitwiseOperationToken) result;

        // Make sure that exactly one of AND, OR, XOR is set.
        _assert(node.isBitwiseAnd() ^ node.isBitwiseOr() ^ node.isBitwiseXor(),
                node, "Invalid operation");

        for (int i = 1; i < numChildren; i++) {
            ptolemy.data.Token nextToken = tokens[i];

            if (!(nextToken instanceof BitwiseOperationToken)) {
                throw new IllegalActionException("Operation "
                        + node.getOperator().image + " not defined on "
                        + result
                        + " which does not support bitwise operations.");
            }

            if (node.isBitwiseAnd()) {
                bitwiseResult = bitwiseResult.bitwiseAnd(nextToken);
            } else if (node.isBitwiseOr()) {
                bitwiseResult = bitwiseResult.bitwiseOr(nextToken);
            } else {
                bitwiseResult = bitwiseResult.bitwiseXor(nextToken);
            }
        }

        _evaluatedChildToken = (ptolemy.data.Token) bitwiseResult;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a function to the children of the specified node.
     *  This also handles indexing into matrices and arrays, which look
     *  like function calls.
     *
     *  In the simplest cases, if the function is being applied to an
     *  expression that evaluated to a FunctionToken, an ArrayToken,
     *  or a MatrixToken, then the function application is simply
     *  applied to the available arguments.
     *
     *  More complex is if the function is being applied to an
     *  expression that does not evaluate as above, resulting in three
     *  cases:  Of primary interest is a function node that represents the
     *  invocation of a Java method registered with the expression
     *  parser.  This method uses the reflection mechanism in the
     *  CachedMethod class to find the correct method, based on the
     *  types of the arguments and invoke it.  See that class for
     *  information about how method arguments are matched.
     *
     *  A second case is the eval() function, which is handled
     *  specially in this method.  The argument to the function is
     *  evaluated, and the parsed as a string using the expression
     *  parser.  The result is then evaluated *in this evaluator*.
     *  This has the effect that any identifiers are evaluated in the
     *  same scope as the original expression.
     *
     *  A third case is the matlab() function, which is also handled
     *  specially in this method, allowing the evaluation of
     *  expressions in matlab if matlab is installed.  The format
     *  of the function is covered in
     *  {@link ptolemy.data.expr.MatlabUtilities#evaluate(String, Set, ParserScope)}
     *  .
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        // First check to see if the name references a valid variable.
        ptolemy.data.Token value = null;
        ptolemy.data.Token scopedValue = null;
        String functionName = node.getFunctionName();

        if (functionName != null && _scope != null) {
            scopedValue = _scope.get(functionName);
            if (!(scopedValue instanceof ObjectToken)) {
                // Pretend that we cannot resolve the name if it is an
                // ObjectToken.
                value = scopedValue;
            }
        }

        // The first child contains the function name as an id.  It is
        // ignored, and not evaluated unless necessary.
        int argCount = node.jjtGetNumChildren() - 1;
        Type[] argTypes = new Type[argCount];
        ptolemy.data.Token[] argValues = new ptolemy.data.Token[argCount];

        // First try to find a signature using argument token values.
        for (int i = 0; i < argCount; i++) {
            // Save the resulting value.
            _evaluateChild(node, i + 1);

            ptolemy.data.Token token = _evaluatedChildToken;
            argValues[i] = token;
            argTypes[i] = token.getType();
        }

        if (value != null || functionName == null) {
            // The value of the first child should be either a FunctionToken,
            // an ArrayToken, or a MatrixToken.
            ptolemy.data.Token result;

            // Evaluate it, if necessary.
            if (value == null) {
                value = _evaluateChild(node, 0);
            }

            if (value instanceof ArrayToken) {
                if (argCount == 1) {
                    result = _evaluateArrayIndex(node, value, argValues[0]);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, an array expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (value instanceof MatrixToken) {
                if (argCount == 2) {
                    result = _evaluateMatrixIndex(node, value, argValues[0],
                            argValues[1]);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, a matrix expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (value instanceof FunctionToken) {
                FunctionToken function = (FunctionToken) value;

                // check number of children against number of arguments of
                // function
                if (function.getNumberOfArguments() != argCount) {
                    throw new IllegalActionException("Wrong number of "
                            + "arguments when applying function "
                            + value.toString());
                }

                result = function.apply(argValues);
            } else {
                // If the argument is a scalar that can be
                // losslessly coverted to an array or matrix
                // and the indexes have value 0 then the evaluation
                // is just the value itself.
                if (argCount == 2) {
                    // Possible matrix promotion, where we allow
                    // scalar(0,0) to simply have value scalar.
                    if (argValues[0] instanceof IntToken
                            && ((IntToken) argValues[0]).intValue() == 0
                            && argValues[1] instanceof IntToken
                            && ((IntToken) argValues[1]).intValue() == 0) {
                        // If there is a corresponding matrix type,
                        // then return the value. To find out whether there
                        // is a corresponding matrix type, just try to create
                        // one.
                        try {
                            ptolemy.data.Token[] tmp = new ptolemy.data.Token[1];
                            tmp[0] = value;
                            MatrixToken.arrayToMatrix(tmp, 1, 1);
                        } catch (IllegalActionException ex) {
                            // No such matrix.
                            throw new IllegalActionException(
                                    "Cannot apply array indexing to "
                                            + value.toString());
                        }
                        result = value;
                    } else {
                        // Either the arguments are not ints or
                        // they are not both zero.
                        throw new IllegalActionException(
                                "Invalid matrix indexing for "
                                        + value.toString());
                    }
                } else if (argCount == 1) {
                    // Possible array promotion, where we allow
                    // scalar(0) to simply have value scalar.
                    if (argValues[0] instanceof IntToken
                            && ((IntToken) argValues[0]).intValue() == 0) {
                        result = value;
                    } else {
                        // Either the argument is not an int or
                        // it is not zero.
                        throw new IllegalActionException(
                                "Invalid array indexing for "
                                        + value.toString());
                    }
                } else {
                    // FIXME: It might be the a parameter is
                    // shadowing a built-in function, in which
                    // case, thrown an exception seems bogus.

                    // The value cannot be indexed or applied
                    // throw exception.
                    throw new IllegalActionException(
                            "Cannot index or apply arguments to "
                                    + value.toString());
                }
            }

            _evaluatedChildToken = result;
            return;
        }

        if (node.getFunctionName().compareTo("eval") == 0) {
            if (argCount == 1) {
                ptolemy.data.Token token = argValues[0];

                if (token instanceof StringToken) {
                    // Note that we do not want to store a reference to
                    // the parser, because parsers take up alot of memory.
                    PtParser parser = new PtParser();
                    ASTPtRootNode tree = parser
                            .generateParseTree(((StringToken) token)
                                    .stringValue());

                    // Note that we evaluate the recursed parse tree
                    // in the same scope as this parse tree.
                    tree.visit(this);

                    //  _evaluatedChildToken = (tree.getToken());
                    // FIXME cache?
                    return;
                }
            }

            throw new IllegalActionException("The function \"eval\" is"
                    + " reserved for reinvoking the parser, and takes"
                    + " exactly one String argument.");
        }

        if (node.getFunctionName().compareTo("matlab") == 0) {
            _evaluateChild(node, 1);

            ptolemy.data.Token token = _evaluatedChildToken;

            if (token instanceof StringToken) {
                String expression = ((StringToken) token).stringValue();
                ParseTreeFreeVariableCollector collector = new ParseTreeFreeVariableCollector();
                Set freeVariables = collector
                        .collectFreeVariables(node, _scope);
                _evaluatedChildToken = MatlabUtilities.evaluate(expression,
                        freeVariables, _scope);
                return;
            } else {
                throw new IllegalActionException(
                        "The function \"matlab\" is"
                                + " reserved for invoking the matlab engine, and takes"
                                + " a string matlab expression argument followed by"
                                + " a list of variable names that the matlab expression"
                                + " refers to.");
            }
        }

        if (node.getFunctionName().compareTo("fold") == 0) {
            if (argCount == 3) {
                if (argValues[0] instanceof FunctionToken) {
                    FunctionToken function = (FunctionToken) argValues[0];
                    if (((FunctionType) function.getType()).getArgCount() != 2) {
                        throw new IllegalActionException(
                                "The first argument "
                                        + "to the function \"fold\" must be a function "
                                        + "that accepts two arguments.");
                    }
                    ptolemy.data.Token current = argValues[1];
                    if (argValues[2] instanceof ArrayToken) {
                        ArrayToken array = (ArrayToken) argValues[2];
                        for (int i = 0; i < array.length(); i++) {
                            current = function.apply(new ptolemy.data.Token[] {
                                    current, array.getElement(i) });
                        }
                        _evaluatedChildToken = current;
                        return;
                    } else if (argValues[2] instanceof ObjectToken) {
                        Object object = ((ObjectToken) argValues[2]).getValue();
                        if (object.getClass().isArray()) {
                            Object[] array = (Object[]) object;
                            for (Object element : array) {
                                Object second = element;
                                if (!(second instanceof ptolemy.data.Token)) {
                                    second = ConversionUtilities
                                            .convertJavaTypeToToken(second);
                                }
                                current = function
                                        .apply(new ptolemy.data.Token[] {
                                                current,
                                                (ptolemy.data.Token) second });
                            }
                            _evaluatedChildToken = current;
                            return;
                        } else if (object instanceof Iterable) {
                            Iterator iterator = ((Iterable) object).iterator();
                            while (iterator.hasNext()) {
                                Object second = iterator.next();
                                if (!(second instanceof ptolemy.data.Token)) {
                                    second = ConversionUtilities
                                            .convertJavaTypeToToken(second);
                                }
                                current = function
                                        .apply(new ptolemy.data.Token[] {
                                                current,
                                                (ptolemy.data.Token) second });
                            }
                            _evaluatedChildToken = current;
                            return;
                        }
                    }
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
                    _evaluatedChildToken = ObjectToken.object(className);
                    return;
                }
            }
        }

        // If not a special function, then reflect the name of the function.
        ptolemy.data.Token result = null;
        try {
            result = _functionCall(node.getFunctionName(), argTypes, argValues);
        } catch (IllegalActionException e) {
            // Try to consider "expression" as "this.expression" and invoke
            // method again. This allows expressions such as "getContainer()" to
            // be evaluated on an arbitrary variable without using "this".
            // -- tfeng (01/19/2009)
            boolean success = false;
            if (argValues.length == 0) {
                ptolemy.data.Token thisToken = _scope.get("this");
                if (thisToken != null) {
                    argTypes = new Type[] { thisToken.getType() };
                    argValues = new ptolemy.data.Token[] { thisToken };
                    result = _methodCall(node.getFunctionName(), argTypes,
                            argValues);
                    success = true;
                }
            }
            if (!success) {
                throw e;
            }
        }

        if (result == null && scopedValue instanceof ObjectToken) {
            // If it is ObjectToken, set it here.
            result = scopedValue;
        }

        _evaluatedChildToken = result;
    }

    /** Define a function, where the children specify the argument types
     *  and the expression.  The expression is not evaluated. The resulting
     *  token in the node is an instance of FunctionToken.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        ASTPtRootNode cloneTree;

        ParseTreeSpecializer specializer = new ParseTreeSpecializer();
        cloneTree = specializer.specialize(node.getExpressionTree(),
                node.getArgumentNameList(), _scope);

        // Infer the return type.
        if (_typeInference == null) {
            _typeInference = new ParseTreeTypeInference();
        }

        _typeInference.inferTypes(node, _scope);

        FunctionType type = (FunctionType) node.getType();
        ExpressionFunction definedFunction = new ExpressionFunction(
                node.getArgumentNameList(), node.getArgumentTypes(), cloneTree);
        FunctionToken result = new FunctionToken(definedFunction, type);
        _evaluatedChildToken = result;
        return;
    }

    /** Evaluate the first child, and depending on its (boolean) result,
     *  evaluate either the second or the third child. The result of
     *  that evaluation becomes the result of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        int numChildren = node.jjtGetNumChildren();

        if (numChildren != 3) {
            // A functional-if node MUST have three children in the parse
            // tree.
            throw new InternalErrorException(
                    "PtParser error: a functional-if node does not have "
                            + "three children in the parse tree.");
        }

        // evaluate the first sub-expression
        _evaluateChild(node, 0);

        ptolemy.data.Token test = _evaluatedChildToken;

        if (!(test instanceof BooleanToken)) {
            throw new IllegalActionException(
                    "Functional-if must branch on a boolean, but instead was "
                            + test.toString() + " an instance of "
                            + test.getClass().getName());
        }

        boolean value = ((BooleanToken) test).booleanValue();

        // Choose the correct sub-expression to evaluate,
        // and type check the other.
        if (_typeInference == null) {
            _typeInference = new ParseTreeTypeInference();
        }

        ASTPtRootNode tokenChild;
        ASTPtRootNode typeChild;

        if (value) {
            tokenChild = (ASTPtRootNode) node.jjtGetChild(1);
            typeChild = (ASTPtRootNode) node.jjtGetChild(2);
        } else {
            tokenChild = (ASTPtRootNode) node.jjtGetChild(2);
            typeChild = (ASTPtRootNode) node.jjtGetChild(1);
        }

        tokenChild.visit(this);

        ptolemy.data.Token token = _evaluatedChildToken;
        Type type = _typeInference.inferTypes(typeChild, _scope);

        Type conversionType = (Type) TypeLattice.lattice().leastUpperBound(
                type, token.getType());

        token = conversionType.convert(token);
        _evaluatedChildToken = token;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Evaluate a numeric constant or an identifier. In the case of an
     *  identifier, its value is obtained from the scope or from the list
     *  of registered constants.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        String name = node.getName();

        // The node refers to a variable, or something else that is in
        // scope.
        ptolemy.data.Token value = null;

        if (_scope != null) {
            value = _scope.get(name);
        }

        // Look up for constants.
        // Pretend that we cannot resolve the name if it is an ObjectToken.
        if (value == null || value instanceof ObjectToken) {
            // A named constant that is recognized by the parser.
            ptolemy.data.Token constant = Constants.get(name);
            if (constant != null) {
                // Assign value only if no constant can be found, because the
                // value could be an ObjectToken that has been temporarily
                // ignored.
                value = constant;
            }
        }

        // Set the value, if we found one.
        if (value != null) {
            _evaluatedChildToken = value;
            return;
        }

        throw new UndefinedConstantOrIdentifierException(node.getName());
    }

    /** Evaluate a logical AND or OR on the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        // Note that we do not always evaluate all of the children...
        // We perform short-circuit evaluation instead and evaluate the
        // children in order until the final value is determined, after
        // which point no more children are evaluated.
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        _evaluateChild(node, 0);

        ptolemy.data.Token result = _evaluatedChildToken;

        if (!(result instanceof BooleanToken)) {
            throw new IllegalActionException("Cannot perform logical "
                    + "operation on " + result + " which is a "
                    + result.getClass().getName());
        }

        // Make sure that exactly one of AND or OR is set.
        _assert(node.isLogicalAnd() ^ node.isLogicalOr(), node,
                "Invalid operation");

        // Perform both the short-circuit AND and short-circuit OR in
        // one piece of code.
        // FIXME: I dislike that this is not done in the token classes...
        boolean flag = node.isLogicalAnd();

        for (int i = 0; i < numChildren; i++) {
            ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);

            // Evaluate the child
            child.visit(this);

            // Get its value.
            ptolemy.data.Token nextToken = _evaluatedChildToken;

            if (!(nextToken instanceof BooleanToken)) {
                throw new IllegalActionException("Cannot perform logical "
                        + "operation on " + nextToken + " which is a "
                        + result.getClass().getName());
            }

            if (flag != ((BooleanToken) nextToken).booleanValue()) {
                _evaluatedChildToken = BooleanToken.getInstance(!flag);

                // Note short-circuit eval.
                return;
            }
        }

        _evaluatedChildToken = BooleanToken.getInstance(flag);

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Construct a matrix containing the children nodes.
     *  The specified node ends up with a MatrixToken value.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        ptolemy.data.Token result = null;

        if (node.getForm() == 1) {
            //int numChildren = node.jjtGetNumChildren();
            result = MatrixToken.arrayToMatrix(tokens, node.getRowCount(),
                    node.getColumnCount());
        } else if (node.getForm() == 2) {
            try {
                int columnCount = MatrixToken.determineSequenceLength(
                        (ScalarToken) tokens[0], (ScalarToken) tokens[1],
                        (ScalarToken) tokens[2]);

                // Make sure that all following rows have the same number
                // of columns.
                for (int i = 1; i < node.getRowCount(); ++i) {
                    if (columnCount != MatrixToken.determineSequenceLength(
                            (ScalarToken) tokens[3 * i],
                            (ScalarToken) tokens[3 * i + 1],
                            (ScalarToken) tokens[3 * i + 2])) {
                        throw new IllegalActionException("Matrix "
                                + "should have the same number of columns "
                                + "for all rows.");
                    }
                }

                ptolemy.data.Token[] matrixTokens = new ptolemy.data.Token[node
                                                                           .getRowCount() * columnCount];

                for (int i = 0; i < node.getRowCount(); i++) {
                    ptolemy.data.Token[] newTokens = MatrixToken
                            .createSequence(tokens[3 * i], tokens[3 * i + 1],
                                    columnCount);
                    System.arraycopy(newTokens, 0, matrixTokens, columnCount
                            * i, columnCount);
                }

                result = MatrixToken.arrayToMatrix(matrixTokens,
                        node.getRowCount(), columnCount);
            } catch (IllegalActionException ex) {
                // FIXME: better detail message that includes the thing
                // we were parsing.
                throw new IllegalActionException(null, null, ex,
                        "Matrix Token construction failed.");
            }
        }

        _evaluatedChildToken = result;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a method to the children of the specified node, where the
     *  first child is the object on which the method is defined and the
     *  rest of the children are arguments. This also handles indexing into
     *  a record, which looks the same.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.
        int argCount = node.jjtGetNumChildren();
        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        // Handle indexing into a record.
        if (argCount == 1 && tokens[0] instanceof RecordToken) {
            RecordToken record = (RecordToken) tokens[0];

            if (record.labelSet().contains(node.getMethodName())) {
                _evaluatedChildToken = record.get(node.getMethodName());
                return;
            }
        }

        // The first child is the object to invoke the method on.
        Type[] argTypes = new Type[argCount];
        Object[] argValues = new Object[argCount];

        // First try to find a signature using argument token values.
        for (int i = 0; i < argCount; i++) {
            // Save the resulting value.
            ptolemy.data.Token token = tokens[i];
            argValues[i] = token;
            argTypes[i] = token.getType();
        }

        ptolemy.data.Token result = _methodCall(node.getMethodName(), argTypes,
                argValues);

        _evaluatedChildToken = result;
    }

    /** Evaluate the power operator on the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        // Operator is always exponentiation
        // Note that since we use an iterative integer method, instead of
        // a logarithmic method, the fastest thing is to apply the
        // exponentiation inside out, i.e. left to right.
        ptolemy.data.Token result = tokens[0];

        for (int i = 1; i < numChildren; i++) {
            int times = 1;
            ptolemy.data.Token token = tokens[i];

            // Note that we check for ScalarTokens because anything
            // that has a meaningful intValue() method, such as
            // ShortToken or UnsignedByteToken will also work here.
            if (!(token instanceof ScalarToken)) {
                throw new IllegalActionException(
                        "Exponent must be ScalarToken and have a valid "
                                + "lossless conversion to integer. "
                                + "Integer, short or unsigned byte meet "
                                + "these criteria.\n"
                                + "Use pow(10, 3.5) for non-integer exponents");
            }

            try {
                times = ((ptolemy.data.ScalarToken) token).intValue();
            } catch (IllegalActionException ex) {
                throw new IllegalActionException("Exponent must have a valid "
                        + "lossless conversion to integer. "
                        + "Integer, short or unsigned byte meet "
                        + "these criteria.\n"
                        + "Use pow(10, 3.5) for non-integer exponents");
            }

            result = result.pow(times);
        }

        _evaluatedChildToken = result;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Multiply the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        ptolemy.data.Token result = tokens[0];

        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);
            ptolemy.data.Token nextToken = tokens[i];

            if (operator.kind == PtParserConstants.MULTIPLY) {
                result = result.multiply(nextToken);
            } else if (operator.kind == PtParserConstants.DIVIDE) {
                result = result.divide(nextToken);
            } else if (operator.kind == PtParserConstants.MODULO) {
                result = result.modulo(nextToken);
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        _evaluatedChildToken = result;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Construct a record by assigning the fields values given by
     *  the children nodes.
     *  @param node The record constructor node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(node.getFieldNames().size() == numChildren, node,
                "The number of labels and values does not "
                        + "match in parsing a record expression.");

        String[] labels = (String[]) node.getFieldNames().toArray(
                new String[numChildren]);

        if (node instanceof ASTPtOrderedRecordConstructNode) {
            _evaluatedChildToken = new OrderedRecordToken(labels, tokens);
        } else {
            _evaluatedChildToken = new RecordToken(labels, tokens);
        }

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        Token operator = node.getOperator();
        ptolemy.data.Token leftToken = tokens[0];
        ptolemy.data.Token rightToken = tokens[1];
        ptolemy.data.Token result;

        if (operator.kind == PtParserConstants.EQUALS) {
            result = leftToken.isEqualTo(rightToken);
        } else if (operator.kind == PtParserConstants.NOTEQUALS) {
            result = leftToken.isEqualTo(rightToken).not();
        } else {
            if (!(leftToken instanceof PartiallyOrderedToken && rightToken instanceof PartiallyOrderedToken)) {
                throw new IllegalActionException("The " + operator.image
                        + " operator can only be applied on partial orders.");
            }

            PartiallyOrderedToken leftScalar = (PartiallyOrderedToken) leftToken;
            PartiallyOrderedToken rightScalar = (PartiallyOrderedToken) rightToken;

            if (operator.kind == PtParserConstants.GTE) {
                result = rightScalar.isLessThan(leftScalar).or(
                        leftToken.isEqualTo(rightToken));
            } else if (operator.kind == PtParserConstants.GT) {
                result = rightScalar.isLessThan(leftScalar);
            } else if (operator.kind == PtParserConstants.LTE) {
                result = leftScalar.isLessThan(rightScalar).or(
                        leftToken.isEqualTo(rightToken));
            } else if (operator.kind == PtParserConstants.LT) {
                result = leftScalar.isLessThan(rightScalar);
            } else {
                throw new IllegalActionException("Invalid operation "
                        + operator.image + " between "
                        + leftToken.getClass().getName() + " and "
                        + rightToken.getClass().getName());
            }
        }

        _evaluatedChildToken = result;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a shift operator to the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        Token operator = node.getOperator();
        ptolemy.data.Token token = tokens[0];
        ptolemy.data.Token bitsToken = tokens[1];
        ptolemy.data.Token result = null;

        if (!(token instanceof ScalarToken)) {
            throw new IllegalActionException("The " + operator
                    + " operator requires "
                    + "the left operand to be a scalar.");
        }

        if (!(bitsToken instanceof ScalarToken)) {
            throw new IllegalActionException("The " + operator
                    + " operator requires "
                    + "the right operand to be a scalar.");
        }

        // intValue() is used rather than testing for IntToken
        // because any token with an intValue() is OK.  However,
        // we need a try...catch to generate a proper error message.
        try {
            if (operator.kind == PtParserConstants.SHL) {
                result = ((ScalarToken) token)
                        .leftShift(((ScalarToken) bitsToken).intValue());
            } else if (operator.kind == PtParserConstants.SHR) {
                result = ((ScalarToken) token)
                        .rightShift(((ScalarToken) bitsToken).intValue());
            } else if (operator.kind == PtParserConstants.LSHR) {
                result = ((ScalarToken) token)
                        .logicalRightShift(((ScalarToken) bitsToken).intValue());
            } else {
                _assert(false, node, "Invalid operation");
            }
        } catch (IllegalActionException ex) {
            throw new IllegalActionException("The " + operator
                    + " operator requires "
                    + "the right operand to have an integer value.");
        }

        _evaluatedChildToken = result;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a sum operator to the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        ptolemy.data.Token result = tokens[0];

        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);
            ptolemy.data.Token nextToken = tokens[i];

            if (operator.kind == PtParserConstants.PLUS) {
                result = result.add(nextToken);
            } else if (operator.kind == PtParserConstants.MINUS) {
                result = result.subtract(nextToken);
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        _evaluatedChildToken = result;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a unary operator to the single child of the specified node.
     *  @param node The specified node.
     */
    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        _assert(node.jjtGetNumChildren() == 1, node,
                "Unary node must have exactly one child!");

        ptolemy.data.Token result = tokens[0];

        if (node.isMinus()) {
            result = result.zero().subtract(result);
        } else if (node.isNot()) {
            if (result instanceof BooleanToken) {
                result = ((BooleanToken) result).not();
            } else {
                throw new IllegalActionException(
                        "Not operator not support for non-boolean token: "
                                + result.toString());
            }
        } else if (node.isBitwiseNot()) {
            if (!(result instanceof BitwiseOperationToken)) {
                throw new IllegalActionException("Bitwise negation"
                        + " not defined on " + result
                        + " which does not support bitwise operations.");
            }

            result = (ptolemy.data.Token) ((BitwiseOperationToken) result)
                    .bitwiseNot();
        } else {
            _assert(false, node, "Unrecognized unary node");
        }

        _evaluatedChildToken = result;

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Construct a union by assigning the label value given by
     *  the children nodes.
     *  @param node The union constructor node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    @Override
    public void visitUnionConstructNode(ASTPtUnionConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(node.getLabelNames().size() == numChildren, node,
                "The number of labels and values does not "
                        + "match in parsing a record expression.");

        String[] labels = (String[]) node.getLabelNames().toArray(
                new String[numChildren]);

        //_assert(labels.length == 1, node,
        //        "has more than one member type of the union.");

        //If there is more than one members in the union, take the first
        //member value as the value of the union.
        if (labels.length > 0) {
            _evaluatedChildToken = new UnionToken(labels[0], tokens[0]);
        }
        _evaluatedChildToken = new UnionToken(labels[0], tokens[0]);

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Assert that the given boolean value, which describes the given
     *  parse tree node, is true.  If it is false, then throw a new
     *  InternalErrorException that describes the node and includes
     *  the given message.
     *  @param flag The flag that is asserted to be true.
     *  @param node The node on which the assertion is asserted.
     *  @param message The message to include in the exception.
     *  @exception InternalErrorException If the assertion is violated.
     *   Note that this is a runtime exception, so it need not be declared
     *   explicitly.
     */
    protected void _assert(boolean flag, ASTPtRootNode node, String message) {
        if (!flag) {
            throw new InternalErrorException(message + ": " + node.toString());
        }
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them; this will cause their token
     *  value to be determined.
     *  @param node The node whose children are evaluated.
     *  @return The values of the children.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token[] _evaluateAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
        ptolemy.data.Token[] tokens = new ptolemy.data.Token[numChildren];

        for (int i = 0; i < numChildren; i++) {
            /* ASTPtRootNode child = (ASTPtRootNode) */node.jjtGetChild(i);
            tokens[i] = _evaluateChild(node, i);
        }

        return tokens;
    }

    /** Evaluate the array index operation represented by the given node.
     *  @param node The node that caused this method to be called.
     *  @param value The token that is being indexed into, which must
     *   be an ArrayToken.
     *  @param index The index, which must be an integer token.
     *  @return The element of the given token at the given index.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _evaluateArrayIndex(ASTPtRootNode node,
            ptolemy.data.Token value, ptolemy.data.Token index)
                    throws IllegalActionException {
        if (!(value instanceof ArrayToken)) {
            throw new IllegalActionException(
                    "Array indexing cannot be applied to '" + value.toString()
                    + "' because its value is not an array.");
        }

        if (!(index instanceof IntToken)) {
            throw new IllegalActionException(
                    "Array indexing requires an integer. Got: " + index);
        }

        int integerIndex = ((IntToken) index).intValue();

        try {
            return ((ArrayToken) value).getElement(integerIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException("The index '" + index
                    + "' is out of bounds on the array '" + value + "'.");
        }
    }

    /** Evaluate the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     *  @param node The node
     *  @param i The index of the node
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _evaluateChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        _traceEnter(child);
        child.visit(this);
        _traceLeave(child);
        return _evaluatedChildToken;
    }

    /** Evaluate the Matrix index operation represented by the given node.
     *  @param node The node that caused this method to be called.
     *  @param value The token that is being indexed into, which must
     *   be a MatrixToken.
     *  @param rowIndex The row index, which must be an integer token.
     *  @param columnIndex The column index, which must be an integer token.
     *  @return The element of the given token at the given index.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _evaluateMatrixIndex(ASTPtRootNode node,
            ptolemy.data.Token value, ptolemy.data.Token rowIndex,
            ptolemy.data.Token columnIndex) throws IllegalActionException {
        if (!(value instanceof MatrixToken)) {
            throw new IllegalActionException(
                    "Matrix indexing cannot be applied to '" + value.toString()
                    + "' because its value is not a matrix.");
        }

        if (!(rowIndex instanceof IntToken)) {
            throw new IllegalActionException(
                    "Matrix row index must be an integer. Got: " + rowIndex);
        }

        if (!(columnIndex instanceof IntToken)) {
            throw new IllegalActionException(
                    "Matrix column index must be an integer. Got: "
                            + columnIndex);
        }

        int integerRowIndex = ((IntToken) rowIndex).intValue();
        int integerColumnIndex = ((IntToken) columnIndex).intValue();

        try {
            return ((MatrixToken) value).getElementAsToken(integerRowIndex,
                    integerColumnIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException("The index (" + rowIndex + ","
                    + columnIndex + ") is out of bounds on the matrix '"
                    + value + "'.");
        }
    }

    /** Evaluate the specified function.  The function must be defined
     *  as one of the registered functions with PtParser.
     *  @param functionName The function name.
     *  @param argTypes An array of argument types.
     *  @param argValues An array of argument values.
     *  @return The value of returned by the specified method.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _functionCall(String functionName,
            Type[] argTypes, Object[] argValues) throws IllegalActionException {
        CachedMethod method = CachedMethod.findMethod(functionName, argTypes,
                CachedMethod.FUNCTION);

        if (method.isValid()) {
            if (_trace != null) {
                _trace("Invoking " + method.methodDescription());
                _trace("as " + method);
            }

            ptolemy.data.Token result = method.invoke(argValues);
            return result;
        } else {
            throw new IllegalActionException("No function found matching "
                    + method.toString());
        }
    }

    /** Evaluate the specified method.  The object on which the method
     *  is evaluated should be the first argument.
     *  @param methodName The method name.
     *  @param argTypes An array of argument types.
     *  @param argValues An array of argument values.
     *  @return The value of returned by the specified method.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _methodCall(String methodName,
            Type[] argTypes, Object[] argValues) throws IllegalActionException {

        CachedMethod method = CachedMethod.findMethod(methodName, argTypes,
                CachedMethod.METHOD);

        if (method.isValid()) {
            if (_trace != null) {
                _trace("Invoking " + method.methodDescription());
                _trace("as " + method);
            }

            ptolemy.data.Token result = method.invoke(argValues);
            return result;
        }

        if (argValues[0] instanceof ObjectToken) {
            ObjectToken objectToken = (ObjectToken) argValues[0];
            Object object = objectToken.getValue();
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
                            return ((Variable) result).getToken();
                        } else {
                            return new ObjectToken(result, result.getClass());
                        }
                    }
                }
            }

            Class<?> valueClass = object == null ? objectToken.getValueClass()
                    : object.getClass();
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
                    ptolemy.data.Token result = _invokeMethod(valueClass,
                            object, methodName, argTypes, argValues);
                    if (result != null) {
                        return result;
                    }
                }
            }

            if (object == null) {
                throw new IllegalActionException("The object on which method "
                        + "\"" + methodName + "\" is invoked on is null, but "
                        + "the method is not found or is not static.");
            }
        }

        throw new IllegalActionException("No method found matching "
                + method.toString());
    }

    /** Add a record to the current trace corresponding to the given message.
     *  If the trace is null, do nothing.
     *  @param string The string
     */
    protected void _trace(String string) {
        if (_trace != null) {
            for (int i = 0; i < _depth; i++) {
                _trace.append("  ");
            }

            _trace.append(string);
            _trace.append("\n");
        }
    }

    /** Add a record to the current trace corresponding to the start
     *  of the evaluation of the given node.  If the trace is null, then
     *  do nothing.
     *  @param node The node.
     */
    protected void _traceEnter(ASTPtRootNode node) {
        if (_trace != null) {
            for (int i = 0; i < _depth; i++) {
                _trace.append("  ");
            }

            _trace.append("Entering node " + node.getClass().getName() + "\n");
            _depth++;
        }
    }

    /** Add a record to the current trace corresponding to the completion
     *  of the evaluation of the given node.  If the trace is null, then
     *  do nothing.
     *  @param node The node.
     */
    protected void _traceLeave(ASTPtRootNode node) {
        if (_trace != null) {
            _depth--;

            for (int i = 0; i < _depth; i++) {
                _trace.append("  ");
            }

            _trace.append("Node " + node.getClass().getName()
                    + " evaluated to " + _evaluatedChildToken + "\n");
        }
    }

    // Temporary storage for the result of evaluating a child node.
    // This is protected so that derived classes can access it.
    protected ptolemy.data.Token _evaluatedChildToken = null;

    protected ParseTreeTypeInference _typeInference = null;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Invoke a method of the class for the given object, or retrieve a field
     *  of it.
     */
    private ptolemy.data.Token _invokeMethod(Class<?> clazz, Object object,
            String methodName, Type[] argTypes, Object[] argValues)
                    throws IllegalActionException {
        Object result = null;

        if (object != null && argTypes.length == 1) {
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                if (field.getName().equals(methodName)
                        && Modifier.isPublic(field.getModifiers())) {
                    try {
                        result = field.get(object);
                    } catch (IllegalArgumentException e) {
                    } catch (IllegalAccessException e) {
                    }
                }
            }
        }

        Method[] methods = clazz.getMethods();
        int argCount = argTypes.length - 1;
        Object[] args = new Object[argCount];
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
                    } else {
                        Object argument = argValues[i + 1];
                        if (argument instanceof ObjectToken) {
                            args[i] = ((ObjectToken) argument).getValue();
                        } else if (argument instanceof ptolemy.data.Token) {
                            args[i] = ConversionUtilities
                                    .convertTokenToJavaType((ptolemy.data.Token) argument);
                        } else {
                            args[i] = argument;
                        }
                    }
                }
                if (compatible
                        && (object != null || Modifier.isStatic(method
                                .getModifiers()))) {
                    try {
                        result = method.invoke(object, args);
                        if (result == null) {
                            result = new ObjectToken(null,
                                    method.getReturnType());
                        }
                        break;
                    } catch (IllegalArgumentException e) {
                    } catch (IllegalAccessException e) {
                    } catch (InvocationTargetException e) {
                    }
                }
            }
        }

        if (result == null) {
            return null;
        } else {
            return ConversionUtilities.convertJavaTypeToToken(result);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _depth = 0;

    private ParserScope _scope = null;

    private StringBuffer _trace = null;
}
