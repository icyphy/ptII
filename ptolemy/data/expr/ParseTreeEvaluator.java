/* A visitor for parse trees of the expression language.

 Copyright (c) 1998-2003 The Regents of the University of California
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.data.ArrayToken;
import ptolemy.data.BitwiseOperationToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


//////////////////////////////////////////////////////////////////////////
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
    public ptolemy.data.Token evaluateParseTree(
            ASTPtRootNode node, ParserScope scope)
            throws IllegalActionException {
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
    public String traceParseTreeEvaluation(
            ASTPtRootNode node, ParserScope scope)
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
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }
        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        // Convert up to LUB.
        ptolemy.data.type.Type elementType = tokens[0].getType();
        for (int i = 0; i < numChildren; i++) {
            Type valueType = tokens[i].getType();
            if (!elementType.equals(valueType)) {
                elementType = TypeLattice.leastUpperBound(
                        elementType, valueType);
            }
        }
        for (int i = 0; i < numChildren; i++) {
            tokens[i] = elementType.convert(tokens[i]);
        }

        _evaluatedChildToken = (new ArrayToken(tokens));
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Evaluate a bitwise operator on the children of the specified
     *  node, where the particular operator is property of the node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
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
            throw new IllegalActionException(
                    "Operation " + node.getOperator().image +
                    " not defined on " + result +
                    " which does not support bitwise operations.");
        }

        BitwiseOperationToken bitwiseResult =
            (BitwiseOperationToken)result;

        // Make sure that exactly one of AND, OR, XOR is set.
        _assert(node.isBitwiseAnd() ^ node.isBitwiseOr() ^ node.isBitwiseXor(),
                node, "Invalid operation");

        for (int i = 1; i < numChildren; i++ ) {
            ptolemy.data.Token nextToken = tokens[i];
            if (!(nextToken instanceof BitwiseOperationToken)) {
                throw new IllegalActionException(
                        "Operation " + node.getOperator().image +
                        " not defined on " + result +
                        " which does not support bitwise operations.");
            }
            if (node.isBitwiseAnd()) {
                bitwiseResult = bitwiseResult.bitwiseAnd(nextToken);
            } else if (node.isBitwiseOr()) {
                bitwiseResult = bitwiseResult.bitwiseOr(nextToken);
            } else {
                bitwiseResult = bitwiseResult.bitwiseXor(nextToken);
            }
        }
        _evaluatedChildToken = ((ptolemy.data.Token)bitwiseResult);
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
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        // First check to see if the name references a valid variable.
        ptolemy.data.Token value = null;
        String functionName = node.getFunctionName();
        if ( functionName != null && _scope != null) {
            value = _scope.get(node.getFunctionName());
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
            if(value == null) {
                value = _evaluateChild(node, 0);
            }

            if (value instanceof ArrayToken) {
                if (argCount == 1) {
                    result = _evaluateArrayIndex(node, value,
                            argValues[0]);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, an array expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (value instanceof MatrixToken) {
                if (argCount == 2) {
                    result = _evaluateMatrixIndex(node, value,
                            argValues[0], argValues[1]);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, a matrix expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (value instanceof FunctionToken) {
                FunctionToken function = (FunctionToken)value;
                // check number of children against number of arguments of
                // function
                if (function.getNumberOfArguments() != argCount) {
                    throw new IllegalActionException("Wrong number of "
                            + "arguments when applying function "
                            + value.toString());
                }
           
                result = function.apply(argValues);
            } else {
                // the value cannot be indexed or applied
                // throw exception
                throw new IllegalActionException(
                        "Cannot index or apply arguments to "
                        + value.toString());
            }
            _evaluatedChildToken = (result);
            return;
        }

        if (node.getFunctionName().compareTo("eval") == 0) {
            if (argCount == 1) {
                ptolemy.data.Token token =
                    argValues[0];

                if (token instanceof StringToken) {
                    // Note that we do not want to store a reference to
                    // the parser, because parsers take up alot of memory.
                    PtParser parser = new PtParser();
                    ASTPtRootNode tree = parser.generateParseTree(
                            ((StringToken)token).stringValue());

                    // Note that we evaluate the recursed parse tree
                    // in the same scope as this parse tree.
                    tree.visit(this);
                    //  _evaluatedChildToken = (tree.getToken());
                    // FIXME cache?
                    return;
                }
            }
            throw new IllegalActionException("The function \"eval\" is" +
                    " reserved for reinvoking the parser, and takes" +
                    " exactly one String argument.");
        }

        if (node.getFunctionName().compareTo("matlab") == 0) {
            if (argCount == 1) { 
                ptolemy.data.Token token =
                argValues[0];
                if (token instanceof StringToken) {
                    String expression = ((StringToken)token).stringValue();
                    ParseTreeFreeVariableCollector collector =
                        new ParseTreeFreeVariableCollector();
                    Set freeVariables =
                        collector.collectFreeVariables(node, _scope);
                    _evaluatedChildToken =
                        MatlabUtilities.evaluate(
                                expression, freeVariables, _scope);
                    return;
                } 
            } else {
                throw new IllegalActionException("The function \"matlab\" is" +
                        " reserved for invoking the matlab engine, and takes" +
                        " a string matlab expression argument followed by" +
                        " a list of variable names that the matlab expression" +
                        " refers to.");
            }
        }

        // If not a special function, then reflect the name of the function.
        ptolemy.data.Token result = _functionCall(
                node.getFunctionName(), argTypes, argValues);
        _evaluatedChildToken = (result);
    }

    /** Define a function, where the children specify the argument types
     *  and the expression.  The expression is not evaluated. The resulting
     *  token in the node is an instance of FunctionToken.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
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
        FunctionType type = (FunctionType)node.getType();
        ExpressionFunction definedFunction =
            new ExpressionFunction(
                    node.getArgumentNameList(), node.getArgumentTypes(),
                    cloneTree);
        FunctionToken result = new FunctionToken(definedFunction, type);
        _evaluatedChildToken = (result);
        return;
    }

    /** Evaluate the first child, and depending on its (boolean) result,
     *  evaluate either the second or the third child. The result of
     *  that evaluation becomes the result of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
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

        boolean value = ((BooleanToken)test).booleanValue();

        // Choose the correct sub-expression to evaluate,
        // and type check the other.
        if (_typeInference == null) {
            _typeInference = new ParseTreeTypeInference();
        }

        ASTPtRootNode tokenChild, typeChild;
        if (value) {
            tokenChild = (ASTPtRootNode)node.jjtGetChild(1);
            typeChild = (ASTPtRootNode)node.jjtGetChild(2);
        } else {
            tokenChild = (ASTPtRootNode)node.jjtGetChild(2);
            typeChild = (ASTPtRootNode)node.jjtGetChild(1);
        }

        tokenChild.visit(this);
        ptolemy.data.Token token = _evaluatedChildToken;
        Type type = _typeInference.inferTypes(typeChild, _scope);

        Type conversionType = (Type)TypeLattice.lattice().leastUpperBound(
                type, token.getType());

        token = conversionType.convert(token);
        _evaluatedChildToken = (token);
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
    public void visitLeafNode(ASTPtLeafNode node)
            throws IllegalActionException {
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
        if (value == null) {
            // A named constant that is recognized by the parser.
            value = Constants.get(name);
        }

        // Set the value, if we found one.
        if (value != null) {
            _evaluatedChildToken = value;
            return;
        }
        throw new IllegalActionException(
                "The ID " + node.getName() + " is undefined.");
    }

    /** Evaluate a logical AND or OR on the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
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
        _assert(node.isLogicalAnd() ^ node.isLogicalOr(),
                node, "Invalid operation");

        // Perform both the short-circuit AND and short-circuit OR in
        // one piece of code.
        // FIXME: I dislike that this is not done in the token classes...
        boolean flag = node.isLogicalAnd();
        for (int i = 0; i < numChildren; i++) {
            ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
            // Evaluate the child
            child.visit(this);
            // Get its value.
            ptolemy.data.Token nextToken = _evaluatedChildToken;
            if (!(nextToken instanceof BooleanToken)) {
                throw new IllegalActionException("Cannot perform logical "
                        + "operation on " + nextToken + " which is a "
                        + result.getClass().getName());
            }
            if (flag != ((BooleanToken)nextToken).booleanValue()) {
                _evaluatedChildToken = (BooleanToken.getInstance(!flag));
                // Note short-circuit eval.
                return;
            }
        }
        _evaluatedChildToken = (BooleanToken.getInstance(flag));
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Construct a matrix containing the children nodes.
     *  The specified node ends up with a MatrixToken value.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
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
                        (ScalarToken)tokens[0],
                        (ScalarToken)tokens[1],
                        (ScalarToken)tokens[2]);
                // Make sure that all following rows have the same number
                // of columns.
                for (int i = 1; i < node.getRowCount(); ++i) {
                    if (columnCount != MatrixToken.determineSequenceLength(
                            (ScalarToken)tokens[3*i],
                            (ScalarToken)tokens[3*i+1],
                            (ScalarToken)tokens[3*i+2])) {
                        throw new IllegalActionException("Matrix "
                                + "should have the same number of columns "
                                + "for all rows.");
                    }
                }

                ptolemy.data.Token[] matrixTokens =
                    new ptolemy.data.Token[node.getRowCount() * columnCount];
                for (int i = 0; i < node.getRowCount(); i++) {
                    ptolemy.data.Token[] newTokens =
                        MatrixToken.createSequence(
                                (ScalarToken)tokens[3*i],
                                (ScalarToken)tokens[3*i+1],
                                columnCount);
                    System.arraycopy(newTokens, 0,
                            matrixTokens, columnCount * i, columnCount);
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
        _evaluatedChildToken = (result);
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
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.

        int argCount = node.jjtGetNumChildren();
        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        // Handle indexing into a record.
        if (argCount == 1 &&
                tokens[0] instanceof RecordToken) {
            RecordToken record = (RecordToken)tokens[0];
            if (record.labelSet().contains(node.getMethodName())) {
                _evaluatedChildToken = (record.get(node.getMethodName()));
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

        ptolemy.data.Token result = _methodCall(
                node.getMethodName(), argTypes, argValues);

        _evaluatedChildToken = (result);
    }

    /** Evaluate the power operator on the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
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
            // UnsignedByteToken will also work here.
            if (!(token instanceof ScalarToken)) {
                throw new IllegalActionException(
                        "Exponent must be ScalarToken and have a valid "
                        + "lossless conversion to integer. Integer or "
                        + "unsigned byte meet these criteria.\n"
                        + "Use pow(10, 3.5) for non-integer exponents");
            }
            try {
                times = ((ptolemy.data.ScalarToken)token).intValue();
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(
                        "Exponent must have a valid "
                        + "lossless conversion to integer. Integer or "
                        + "unsigned byte meet this criterion.\n"
                        + "Use pow(10, 3.5) for non-integer exponents");
            }

            result = result.pow(times);
        }
        _evaluatedChildToken = (result);
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Multiply the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
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
                "The number of child nodes is " +
                "not equal to number of operators plus one");
        ptolemy.data.Token result = tokens[0];
        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token)lexicalTokenList.get(i - 1);
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
        _evaluatedChildToken = (result);
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Construct a record by assigning the fields values given by
     *  the children nodes.
     *  @param node The record constructor node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }
        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(node.getFieldNames().size() == numChildren,
                node, "The number of labels and values does not " +
                "match in parsing a record expression.");
        String[] labels = (String[]) node.getFieldNames().toArray(
                new String[numChildren]);

        _evaluatedChildToken = (new RecordToken(labels, tokens));
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }
        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        Token operator = (Token)node.getOperator();
        ptolemy.data.Token leftToken = tokens[0];
        ptolemy.data.Token rightToken = tokens[1];
        ptolemy.data.Token result;
        if (operator.kind == PtParserConstants.EQUALS) {
            result = leftToken.isEqualTo(rightToken);
        } else if (operator.kind == PtParserConstants.NOTEQUALS) {
            result = leftToken.isEqualTo(rightToken).not();
        } else {
            if (!((leftToken instanceof ScalarToken) &&
                    (rightToken instanceof ScalarToken))) {
                throw new IllegalActionException(
                        "The " + operator.image +
                        " operator can only be applied between scalars.");
            }
            ScalarToken leftScalar = (ScalarToken)leftToken;
            ScalarToken rightScalar = (ScalarToken)rightToken;
            if (operator.kind == PtParserConstants.GTE) {
                result = leftScalar.isLessThan(rightScalar).not();
            } else if (operator.kind == PtParserConstants.GT) {
                result = rightScalar.isLessThan(leftScalar);
            } else if (operator.kind == PtParserConstants.LTE) {
                result = rightScalar.isLessThan(leftScalar).not();
            } else if (operator.kind == PtParserConstants.LT) {
                result = leftScalar.isLessThan(rightScalar);
            } else {
                throw new IllegalActionException(
                        "Invalid operation " + operator.image + " between " +
                        leftToken.getClass().getName() + " and " +
                        rightToken.getClass().getName());
            }
        }
        _evaluatedChildToken = (result);
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a shift operator to the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }
        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        Token operator = (Token)node.getOperator();
        ptolemy.data.Token token = tokens[0];
        ptolemy.data.Token bitsToken = tokens[1];
        ptolemy.data.Token result = null;

        if (!(token instanceof ScalarToken)) {
            throw new IllegalActionException(
                    "The " + operator + " operator requires " +
                    "the left operand to be a scalar.");
        }
        if (!(bitsToken instanceof ScalarToken)) {
            throw new IllegalActionException(
                    "The " + operator + " operator requires " +
                    "the right operand to be a scalar.");
        }
        // intValue() is used rather than testing for IntToken
        // because any token with an intValue() is OK.  However,
        // we need a try...catch to generate a proper error message.
        try {
            if (operator.kind == PtParserConstants.SHL) {
                result = ((ScalarToken)token).leftShift(
                        ((ScalarToken)bitsToken).intValue());
            } else if (operator.kind == PtParserConstants.SHR) {
                result = ((ScalarToken)token).rightShift(
                        ((ScalarToken)bitsToken).intValue());
            } else if (operator.kind == PtParserConstants.LSHR) {
                result = ((ScalarToken)token).logicalRightShift(
                        ((ScalarToken)bitsToken).intValue());
            } else {
                _assert(false, node, "Invalid operation");
            }
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(
                    "The " + operator + " operator requires " +
                    "the right operand to have an integer value.");
        }
        _evaluatedChildToken = (result);
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a sum operator to the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public void visitSumNode(ASTPtSumNode node)
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
                "The number of child nodes is " +
                "not equal to number of operators plus one");
        ptolemy.data.Token result = tokens[0];
        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token)lexicalTokenList.get(i - 1);
            ptolemy.data.Token nextToken = tokens[i];
            if (operator.kind == PtParserConstants.PLUS) {
                result = result.add(nextToken);
            } else if (operator.kind == PtParserConstants.MINUS) {
                result = result.subtract(nextToken);
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        _evaluatedChildToken = (result);
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Apply a unary operator to the single child of the specified node.
     *  @param node The specified node.
     */
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
                result = ((BooleanToken)result).not();
            } else {
                throw new IllegalActionException(
                        "Not operator not support for non-boolean token: " +
                        result.toString());
            }
        } else if (node.isBitwiseNot()) {
            if (!(result instanceof BitwiseOperationToken)) {
                throw new IllegalActionException(
                        "Bitwise negation" +
                        " not defined on " + result +
                        " which does not support bitwise operations.");
            }
            result = (ptolemy.data.Token)
                ((BitwiseOperationToken)result).bitwiseNot();
        } else {
            _assert(false, node, "Unrecognized unary node");
        }
        _evaluatedChildToken = (result);
        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

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
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token[] _evaluateAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
        ptolemy.data.Token[] tokens =
            new ptolemy.data.Token[numChildren];
        for (int i = 0; i < numChildren; i++) {
            /* ASTPtRootNode child = (ASTPtRootNode) */ node.jjtGetChild(i);
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
            ptolemy.data.Token value,
            ptolemy.data.Token index) throws IllegalActionException {
        if (!(value instanceof ArrayToken)) {
            throw new IllegalActionException(
                    "Array indexing cannot be applied to '"
                    + value.toString()
                    + "' because its value is not an array.");
        }
        if (!(index instanceof IntToken)) {
            throw new IllegalActionException(
                    "Array indexing requires an integer. Got: " + index);
        }
        int integerIndex = ((IntToken)index).intValue();
        try {
            return ((ArrayToken)value).getElement(integerIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException("The index '"
                    + index + "' is out of bounds on the array '"
                    + value + "'.");
        }
    }

    /** Evaluate the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _evaluateChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
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
            ptolemy.data.Token value,
            ptolemy.data.Token rowIndex,
            ptolemy.data.Token columnIndex) throws IllegalActionException {
        if (!(value instanceof MatrixToken)) {
            throw new IllegalActionException(
                    "Matrix indexing cannot be applied to '"
                    + value.toString()
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
        int integerRowIndex = ((IntToken)rowIndex).intValue();
        int integerColumnIndex = ((IntToken)columnIndex).intValue();
        try {
            return ((MatrixToken)value).getElementAsToken(
                    integerRowIndex, integerColumnIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException("The index ("
                    + rowIndex + "," + columnIndex
                    + ") is out of bounds on the matrix '"
                    + value + "'.");
        }
    }

    /** Evaluate the given parse tree in the scope given to the
     *  constructor of this class.
     *  @param node The root of the parse tree to evaluate.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _evaluateParseTree(ASTPtRootNode node)
            throws IllegalActionException {
        // Evaluate the value of the root node.
        node.visit(this);
        return _evaluatedChildToken;
    }

    /** Evaluate the specified function.  The function must be defined
     *  as one of the registered functions with PtParser.
     *  @param functionName The function name.
     *  @param argTypes An array of argument types.
     *  @param argValues An array of argument values.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _functionCall(String functionName,
            Type[] argTypes, Object[] argValues)
            throws IllegalActionException {
        CachedMethod method = CachedMethod.findMethod(functionName,
                argTypes, CachedMethod.FUNCTION);
        if (method.isValid()) {
            if (_trace != null) {
                _trace("Invoking " + method.methodDescription());
                _trace("as " + method);
            }
            ptolemy.data.Token result = method.invoke(argValues);
            return result;
        } else {
            throw new IllegalActionException("No function found matching " +
                    method.toString());
        }
    }

    /** Evaluate the specified method.  The object on which the method
     *  is evaluated should be the first argument.
     *  @param methodName The method name.
     *  @param argTypes An array of argument types.
     *  @param argValues An array of argument values.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    protected ptolemy.data.Token _methodCall(String methodName,
            Type[] argTypes, Object[] argValues)
            throws IllegalActionException {
        CachedMethod method = CachedMethod.findMethod(methodName,
                argTypes, CachedMethod.METHOD);
        if (method.isValid()) {
            if (_trace != null) {
                _trace("Invoking " + method.methodDescription());
                _trace("as " + method);
            }
            ptolemy.data.Token result = method.invoke(argValues);
            return result;
        } else {
            throw new IllegalActionException("No method found matching " +
                    method.toString());
        }
    }

    /** Add a record to the current trace corresponding to the given message.
     *  If the trace is null, do nothing.
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
     */
    protected void _traceLeave(ASTPtRootNode node) {
        if (_trace != null) {
            _depth--;
            for (int i = 0; i < _depth; i++) {
                _trace.append("  ");
            }
            _trace.append("Node " + node.getClass().getName() +
                    " evaluated to " + _evaluatedChildToken + "\n");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    // Temporary storage for the result of evaluating a child node.
    // This is protected so that derived classes can access it.
    protected ptolemy.data.Token _evaluatedChildToken = null;

    private ParserScope _scope = null;
    private ParseTreeTypeInference _typeInference = null;
    private StringBuffer _trace = null;
    private int _depth = 0;
}
