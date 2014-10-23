/* A base class visitor for parse trees of the expression language.

 Copyright (c) 2006-2014 The Regents of the University of California
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
package ptolemy.cg.kernel.generic.program.procedural;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ptolemy.cg.kernel.generic.ParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.ArrayToken;
import ptolemy.data.BitwiseOperationToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtBitwiseNode;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtMatrixConstructNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtOrderedRecordConstructNode;
import ptolemy.data.expr.ASTPtPowerNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtShiftNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ExpressionFunction;
import ptolemy.data.expr.ParseTreeSpecializer;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.math.Complex;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ProceduralParseTreeCodeGenerator

/**
 A base class visitor for parse trees of the expression language.

 <p> A derived class would Evaluate a parse tree given a reference to
 its root node and generate C or Java code.  It implements a visitor that
 visits the parse tree in depth-first order, evaluating each node and
 storing the result as a token in the node.  Two exceptions are logic
 nodes and the ternary if node (the ? : construct), which do not
 necessarily evaluate all children nodes.

 <p>This class has the following limitations:
 <ul>
 <li> It is a copy of ParseTreeEvaluator from data/expr and thus
 has lots of code for evaluating expressions, which we don't need
 <li> It is not properly converting types: We need to add logic to
 convert types.
 <li> The .tcl test has known failures involving nulls.
 <li> It does not evaluate constants.
 </ul>

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ProceduralParseTreeCodeGenerator extends AbstractParseTreeVisitor
implements ParseTreeCodeGenerator {

    /**
     * Create a ProceduralParseTreeCodeGenerator that is used by
     * the given code generator to generate code for expressions.
     * @param generator The given code generator.
     */
    public ProceduralParseTreeCodeGenerator(ProgramCodeGenerator generator) {
        _generator = generator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the parse tree with the specified root node.
     *  @param node The root of the parse tree.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an parse error occurs.
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
    @Override
    public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
            ParserScope scope) throws IllegalActionException {

        // Make a first pass to infer types.
        ParseTreeTypeInference typeInference = new ParseTreeTypeInference();
        typeInference.inferTypes(node, scope);

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
    @Override
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
            _trace(KernelException.stackTraceToString(ex));
        }

        _scope = null;

        // Return the trace.
        String trace = _trace.toString();
        _trace = null;
        return trace;
    }

    /** Generate code that corresponds with the fire() method.
     *  @return The generated code.
     */
    @Override
    public String generateFireCode() {
        return _childCode;
    }

    /** Generate code that corresponds with the initialize() method.
     *  @return The generated code.
     */
    public String generateInitializeCode() {
        return _initializeCode.toString();
    }

    /** Generate code that corresponds with the preinitialize() method.
     *  @return The generated code.
     */
    public String generatePreinitializeCode() {
        return _preinitializeCode.toString();
    }

    /** Generate shared code.
     *  @return The generated code.
     */
    public String generateSharedCode() {
        return _sharedCode.toString();
    }

    /** Generate code that corresponds with the wrapup() method.
     *  @return The generated code.
     */
    public String generateWrapupCode() {
        return _wrapupCode.toString();
    }

    /** Given a string, escape special characters as necessary.
     *  For C and Java, we do:
     *  <pre>
     *  \\ becomes \\\\
     *  which means:
     *  \{ becomes \\{
     *  \} becomes \\}
     *  \( becomes \\(
     *  \) becomes \\)
     *  and
     *  \\" becomes \"
     *  newline becomes \n
     *  </pre>
     *  @param string The string to escape.
     *  @return A new string with special characters replaced.
     *  @see ptolemy.util.StringUtilities#escapeForXML(String)
     */
    @Override
    public/*static*/String escapeForTargetLanguage(String string) {
        string = StringUtilities.substitute(string, "\\", "\\\\");
        //string = StringUtilities.substitute(string, "\\{", "\\\\{");
        //string = StringUtilities.substitute(string, "\\}", "\\\\}");
        //string = StringUtilities.substitute(string, "\\(", "\\\\(");
        //string = StringUtilities.substitute(string, "\\)", "\\\\)");
        //string = StringUtilities.substitute(string, "\\\"", "\\\\\"");
        string = StringUtilities.substitute(string, "\\\\\"", "\\\"");
        string = StringUtilities.substitute(string, "\n", "\\n");

        return string;
    }

    /** Construct an ArrayToken that contains the tokens from the
     *  children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        int numChildren = node.jjtGetNumChildren();

        ptolemy.data.Token[] tokens = new ptolemy.data.Token[numChildren];

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        //_fireCode.append("$new(Array(" + numChildren + ", " + numChildren);
        //String[] elements = new String[numChildren];
        StringBuffer result = new StringBuffer("$new(Array(" + numChildren
                + ", " + numChildren);

        // Convert up to LUB.
        // Assume UNKNOWN is the lower type.
        ptolemy.data.type.Type elementType = BaseType.UNKNOWN;

        for (int i = 0; i < numChildren; i++) {
            //_fireCode.append(", ");
            result.append(", ");

            //int nextIndex = _fireCode.length();

            if (_typeInference == null) {
                _typeInference = new ParseTreeTypeInference();
            }
            _typeInference.inferTypes(node, _scope);

            tokens[i] = _evaluateChild(node, i);

            Type valueType = ((ASTPtRootNode) node.jjtGetChild(i)).getType();

            if (_isPrimitive(valueType)) {
                //_fireCode.insert(nextIndex, "$new(" + _codeGenType(valueType) + "(");
                //_fireCode.append("))");
                result.append("$new(" + _codeGenType(valueType) + "("
                        + _childCode + "))");
            } else {
                result.append(_childCode);
            }

            if (!elementType.equals(valueType)) { // find max type
                elementType = TypeLattice.leastUpperBound(elementType,
                        valueType);
            }
        }

        //for (int i = 0; i < numChildren; i++) {
        //tokens[i] = elementType.convert(tokens[i]);
        //}

        // Insert the elementType of the array as the last argument.
        if (_targetType(elementType).equals("Token")) {
            //_fireCode.append(", -1");
            result.append(", TYPE_Token");
        } else {
            //_fireCode.append(", TYPE_" + _codeGenType(elementType));
            result.append(", TYPE_" + _codeGenType(elementType));
        }

        //_fireCode.append("))");
        _childCode = result.toString() + "))";

        // Tests JavaParseTreeCodeGenerator-16.2 and
        // JavaParseTreeCodeGenerator-17.2 require that
        // _evaluatedChildToken be set here, otherwise
        // _evaluatedChildToken will be set to the value
        // of the last token.
        _evaluatedChildToken = new ArrayToken(elementType, tokens);

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}

    }

    /** Evaluate a bitwise operator on the children of the specified
     *  node, where the particular operator is property of the node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        //_fireCode.append("(");
        StringBuffer result = new StringBuffer();

        //ptolemy.data.Token result = tokens[0];
        ptolemy.data.Token childToken = _evaluateChild(node, 0);
        result.append(_childCode);

        if (!(childToken instanceof BitwiseOperationToken)) {
            throw new IllegalActionException("Operation "
                    + node.getOperator().image + " not defined on "
                    + childToken
                    + " which does not support bitwise operations.");
        }

        BitwiseOperationToken bitwiseResult = (BitwiseOperationToken) childToken;

        // Make sure that exactly one of AND, OR, XOR is set.
        _assert(node.isBitwiseAnd() ^ node.isBitwiseOr() ^ node.isBitwiseXor(),
                node, "Invalid operation");

        for (int i = 1; i < numChildren; i++) {
            if (node.isBitwiseAnd()) {
                //_fireCode.append("&");
                result.append("&");
            } else if (node.isBitwiseOr()) {
                //_fireCode.append("|");
                result.append("|");
            } else {
                //_fireCode.append("^");
                result.append("^");
            }

            //ptolemy.data.Token nextToken = tokens[i];
            ptolemy.data.Token nextToken = _evaluateChild(node, i);
            result.append(_childCode);

            if (!(nextToken instanceof BitwiseOperationToken)) {
                throw new IllegalActionException("Operation "
                        + node.getOperator().image + " not defined on "
                        + childToken
                        + " which does not support bitwise operations.");
            }

            //             if (node.isBitwiseAnd()) {
            //                 //bitwiseResult = bitwiseResult.bitwiseAnd(nextToken);
            //             } else if (node.isBitwiseOr()) {
            //                 //bitwiseResult = bitwiseResult.bitwiseOr(nextToken);
            //             } else {
            //                 //bitwiseResult = bitwiseResult.bitwiseXor(nextToken);
            //             }
        }

        //_fireCode.append(")");
        _childCode = "(" + result + ")";

        _evaluatedChildToken = (ptolemy.data.Token) bitwiseResult;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
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
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {

        StringBuffer result = new StringBuffer();

        // First check to see if the name references a valid variable.

        ptolemy.data.Token value = null;
        Type type = null;
        String functionName = node.getFunctionName();

        if (functionName != null && _scope != null) {
            value = _scope.get(node.getFunctionName());
            type = _scope.getType(node.getFunctionName());
        }

        // The following block of codes applies when multirate
        // expression is used.  Anonymous functions have no name!

        if (functionName != null) {
            int index = functionName.indexOf("Array");
            if (index > 0 && value != null) {
                String label = value.toString();
                if (label.startsWith("object(")) {
                    label = label.substring(7, label.length() - 1);
                    int position = label.indexOf("(@)");

                    //_fireCode.append(label.substring(0, position + 1));
                    result.append(label.substring(0, position + 1));

                    _evaluateChild(node, 1);

                    //_fireCode.append(label.substring(position + 2));
                    result.append(_childCode + label.substring(position + 2));
                    _childCode = result.toString();
                    return;
                }
            }

            // Translate function to c functions.
            String cFunction = _functionMap.get(functionName);
            if (cFunction != null) {
                functionName = cFunction;
            }
        }

        // The first child contains the function name as an id.  It is
        // ignored, and not evaluated unless necessary.
        int argCount = node.jjtGetNumChildren() - 1;

        if (value != null || functionName == null) {
            // The value of the first child should be either a FunctionToken,
            // an ArrayToken, or a MatrixToken.
            // ptolemy.data.Token result;

            // Evaluate it, if necessary.
            if (value == null) {
                value = _evaluateChild(node, 0);
            }

            if (type instanceof ArrayType) {
                if (argCount == 1) {
                    _evaluateArrayIndex(node, value, type);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, an array expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (type instanceof MatrixType) {
                //FIXME :todo
                if (argCount == 2) {
                    // _evaluateMatrixIndex(node, value, argValues[0],
                    //         argValues[1]);
                } else {
                    //FIXME need better error message when the first child
                    // is, say, a matrix expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if (type instanceof FunctionType) {
                //FIXME :todo
                FunctionToken function = (FunctionToken) value;

                // check number of children against number of arguments of
                // function
                if (function.getNumberOfArguments() != argCount) {
                    throw new IllegalActionException("Wrong number of "
                            + "arguments when applying function "
                            + value.toString());
                }
            } else {
                // the value cannot be indexed or applied
                // throw exception
                throw new IllegalActionException(
                        "Cannot index or apply arguments to token of type \""
                                + value.getType() + "\" with value \""
                                + value.toString() + "\". Index or apply "
                                + "arguments only works with ArrayTokens, "
                                + "MatrixTokens and FunctionTokens.");
            }

            return;
        }

        result.append(functionName + "(");

        for (int i = 0; i < argCount; i++) {
            if (i != 0) {
                result.append(", ");
            }
            _evaluateChild(node, i + 1);

            result.append(_specializeArgument(functionName, i,
                    ((ASTPtRootNode) node.jjtGetChild(i + 1)).getType(),
                    _childCode));
        }
        _childCode = _specializeReturnValue(functionName, node.getType(),
                result + ")");
    }

    /** Define a function, where the children specify the argument types
     *  and the expression.  The expression is not evaluated. The resulting
     *  token in the node is an instance of FunctionToken.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
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

        StringBuffer functionCode = new StringBuffer(
                "\n/* FIXME: This code does work.*/\n");

        // Handle functions like iterate()
        // FIXME: Needs to be finished.  iterate() takes a Ptolemy Token
        // as a third argument.
        // $PTII/bin/ptcg -language java /Users/cxh/ptII/ptolemy/codegen/c/actor/lib/test/auto/knownFailedTests/ExpressionIterate.xml

        // FIXME: Generate function declaration in _preinitCode.
        //functionCode += type.getReturnType().toString();

        List argumentNames = node.getArgumentNameList();
        Type[] argumentTypes = node.getArgumentTypes();

        // FIXME: If we are going to use ptolemy classes, we need
        // to add $PTII to the classpath some how.
        functionCode.append(" new ptolemy.data.expr.ExpressionFunction(\n"
                + "java.util.Arrays.asList(new String[]\n{\n");
        for (int i = 0; i < argumentNames.size(); i++) {
            functionCode.append("\"" + argumentNames.get(i) + "\"");
            if (i < argumentNames.size() - 1) {
                functionCode.append(", ");
            }
        }
        functionCode.append("\n}\n),\n new ptolemy.data.type.Type[] \n{\n");
        for (int i = 0; i < argumentTypes.length; i++) {
            functionCode.append("ptolemy.data.type.BaseType."
                    + argumentTypes[i].toString().toUpperCase(
                            Locale.getDefault()));
            if (i < argumentTypes.length - 1) {
                functionCode.append(", ");
            }
        }
        functionCode.append("\n}\n,\n");

        // FIXME: The problem here is that we need a way to create
        // a ASTPtRootNode at run time that contains the functionality
        // required.  One idea would be to create an anonymous class
        // that extended ExpressionFunction and had an apply() method
        // that had a body that consisted of the functionality we want.
        functionCode.append(null + "\n/*" + node.getExpressionTree() + "*/\n)");

        //functionCode += ") {\n";
        //functionCode += "    return ";
        // See ExpressionFunction.apply() for how to create a temporary scope.
        //functionCode += evaluateParseTree(node.getExpressionTree(), _scope);
        //functionCode += ";\n}\n";

        _childCode = functionCode.toString();
        return;
    }

    /** Evaluate the first child, and depending on its (boolean) result,
     *  evaluate either the second or the third child. The result of
     *  that evaluation becomes the result of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();

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

        StringBuffer result = new StringBuffer();

        // evaluate the first sub-expression
        _evaluateChild(node, 0);
        result.append(_childCode);

        /*
         ptolemy.data.Token test = _evaluatedChildToken;

         if (!(test instanceof BooleanToken)) {
         throw new IllegalActionException(
         "Functional-if must branch on a boolean, but instead test "
         + (test == null ? "was null " : "was "
         + test.toString() + "an instance of "
         + test.getClass().getName()));
         }

         boolean value = ((BooleanToken) test).booleanValue();

         // Choose the correct sub-expression to evaluate,
         // and type check the other.
         if (_typeInference == null) {
         _typeInference = new ParseTreeTypeInference();
         }


         if (value) {
         */
        //_fireCode.append(" ? ");
        result.append(" ? ");

        ASTPtRootNode tokenChild1 = (ASTPtRootNode) node.jjtGetChild(1);
        ASTPtRootNode tokenChild2 = (ASTPtRootNode) node.jjtGetChild(2);

        tokenChild1.visit(this);
        result.append(_childCode);

        //_fireCode.append(" : ");
        result.append(" : ");

        tokenChild2.visit(this);
        result.append(_childCode);

        _childCode = "(" + result + ")";

        ptolemy.data.Token token1 = _evaluatedChildToken;
        ptolemy.data.Token token2 = _evaluatedChildToken;

        Type conversionType = (Type) TypeLattice.lattice().leastUpperBound(
                token1.getType(), token2.getType());

        _evaluatedChildToken = conversionType.convert(token1);

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}

    }

    /** Evaluate a numeric constant or an identifier. In the case of an
     *  identifier, its value is obtained from the scope or from the list
     *  of registered constants.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            if (_evaluatedChildToken instanceof ComplexToken) {
                Complex complex = ((ComplexToken) _evaluatedChildToken)
                        .complexValue();
                _childCode = "$Complex_new(" + complex.real + ", "
                        + complex.imag + ")";
            } else if (_evaluatedChildToken instanceof StringToken) {
                // In C, Strings should have \n tags substituted.
                // See Test 17.2

                //_fireCode.append(escapeForTargetLanguage(_evaluatedChildToken.toString()));

                _childCode = escapeForTargetLanguage(_evaluatedChildToken
                        .toString());

            } else if (_evaluatedChildToken instanceof LongToken) {
                //_fireCode.append(((LongToken) _evaluatedChildToken).longValue() + "LL");
                _childCode = ((LongToken) _evaluatedChildToken).longValue()
                        + "L";
            } else {
                //_fireCode.append(_evaluatedChildToken.toString());
                _childCode = _evaluatedChildToken.toString();
            }
            return;
        }

        String name = node.getName();

        // The node refers to a variable, or something else that is in
        // scope
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

            String label = value.toString();
            if (label.startsWith("object(")) {

                // If this is an ObjectToken, we only wants the label.
                //_fireCode.append(label.substring(7, label.length() - 1));

                _childCode = label.substring(7, label.length() - 1).trim();
                //if (_childCode.equals("object(null)")) {
                //    _childCode = "null";
                //}

            } else {
                // FIXME: handle the rest of the constants from data.expr.Constants
                if (label.equals("Infinity")) {
                    _childCode = "Double.POSITIVE_INFINITY";
                } else if (label.equals("NaN")) {
                    // $PTII/bin/ptcg -language java ./adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/TestNaN.xml
                    _childCode = "Double.NaN";
                } else {
                    //_fireCode.append(label);
                    _childCode = label;
                }
            }

            return;
        }

        throw new IllegalActionException("The ID " + node.getName()
                + " is undefined.");
    }

    /** Evaluate a logical AND or OR on the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //_fireCode.append("(");
        StringBuffer result = new StringBuffer();

        // Note that we do not always evaluate all of the children...
        // We perform short-circuit evaluation instead and evaluate the
        // children in order until the final value is determined, after
        // which point no more children are evaluated.
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        _evaluateChild(node, 0);

        result.append(_childCode);

        //ptolemy.data.Token result = _evaluatedChildToken;

        /*
         if (!(result instanceof BooleanToken)) {
         throw new IllegalActionException("Cannot perform logical "
         + "operation on " + result + " which is a "
         + result.getClass().getName());
         }
         */
        // Make sure that exactly one of AND or OR is set.
        _assert(node.isLogicalAnd() ^ node.isLogicalOr(), node,
                "Invalid operation");

        // Perform both the short-circuit AND and short-circuit OR in
        // one piece of code.
        // FIXME: I dislike that this is not done in the token classes...
        boolean flag = node.isLogicalAnd();

        for (int i = 1; i < numChildren; i++) {
            /*ASTPtRootNode child = (ASTPtRootNode)*/node.jjtGetChild(i);

            result.append(flag ? " && " : " || ");

            // Evaluate the child
            //child.visit(this);
            _evaluateChild(node, i);
            result.append(_childCode);

            // Get its value.
            //ptolemy.data.Token nextToken = _evaluatedChildToken;

            /*
             if (!(nextToken instanceof BooleanToken)) {
             throw new IllegalActionException("Cannot perform logical "
             + "operation on " + nextToken + " which is a "
             + result.getClass().getName());
             }

             if (flag != ((BooleanToken) nextToken).booleanValue()) {
             _evaluatedChildToken = (BooleanToken.getInstance(!flag));

             // Note short-circuit eval.
             return;
             }
             */
        }

        _childCode = "(" + result + ")";

        _evaluatedChildToken = BooleanToken.getInstance(flag);

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
    }

    /** Construct a matrix containing the children nodes.
     *  The specified node ends up with a MatrixToken value.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        int row = node.getRowCount();
        int column = node.getColumnCount();
        ptolemy.data.Token[] tokens = new ptolemy.data.Token[row * column];

        //_fireCode.append("((Token*) $new(Matrix(" + row + ", " + column);
        StringBuffer result = new StringBuffer(row + ", " + column + ", " + row
                * column);

        ptolemy.data.Token childToken = null;
        ptolemy.data.type.Type elementType = BaseType.UNKNOWN;

        if (node.getForm() == 1) {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    //_fireCode.append(", ");
                    result.append(", ");

                    int index = i * column + j;

                    //int nextIndex = _fireCode.length();
                    tokens[index] = _evaluateChild(node, index);

                    Type valueType = tokens[index].getType();

                    if (_isPrimitive(valueType)) {
                        //_fireCode.insert(nextIndex, "$new(" + _codeGenType(valueType) + "(");
                        //_fireCode.append("))");
                        result.append("$new(" + _codeGenType(valueType) + "("
                                + _childCode + "))");
                    }

                    if (!elementType.equals(valueType)) { // find max type
                        elementType = TypeLattice.leastUpperBound(elementType,
                                valueType);
                    }
                }
            }

            String codegenType = _codeGenType(elementType);

            // Insert the elementType of the array as the last argument.
            if (_targetType(elementType).equals("Token")) {
                //_fireCode.append(", -1");
                result.append(", -1");
            } else {
                //_fireCode.append(", TYPE_" + _codeGenType(elementType));
                result.append(", TYPE_" + codegenType);
            }
            //_fireCode.append(")))");
            result = new StringBuffer("($new(" + /*codegenType + */"Matrix("
                    + result.toString());
            _childCode = result.toString() + ")))";

            childToken = MatrixToken.arrayToMatrix(tokens, node.getRowCount(),
                    node.getColumnCount());
        } else if (node.getForm() == 2) {
            try {
                // FIXME: Do codegen for the matlab form.
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

                childToken = MatrixToken.arrayToMatrix(matrixTokens,
                        node.getRowCount(), columnCount);
            } catch (IllegalActionException ex) {
                // FIXME: better detail message that includes the thing
                // we were parsing.
                throw new IllegalActionException(null, null, ex,
                        "Matrix Token construction failed.");
            }
        }

        _evaluatedChildToken = childToken;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
    }

    /** Apply a method to the children of the specified node, where the
     *  first child is the object on which the method is defined and the
     *  rest of the children are arguments. This also handles indexing into
     *  a record, which looks the same.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        //_fireCode.append(node.getMethodName() + "(");
        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.
        int argCount = node.jjtGetNumChildren();
        ptolemy.data.Token token = _evaluateChild(node, 0);
        StringBuffer result = new StringBuffer(_childCode);

        // Handle indexing into a record.
        if (argCount == 1 && token instanceof RecordToken) {
            RecordToken record = (RecordToken) token;

            if (record.labelSet().contains(node.getMethodName())) {
                _evaluatedChildToken = record.get(node.getMethodName());
                return;
            }
        }

        // The first child is the object to invoke the method on.
        Type[] argTypes = new Type[argCount];
        Object[] argValues = new Object[argCount];
        argValues[0] = token;
        argTypes[0] = token.getType();

        // First try to find a signature using argument token values.
        for (int i = 1; i < argCount; i++) {
            //_fireCode.append(", ");
            result.append(", ");

            // Save the resulting value.
            token = _evaluateChild(node, i);
            result.append(_childCode);

            argValues[i] = token;
            argTypes[i] = token.getType();
        }

        //_fireCode.append("->" + node.getMethodName() + "()");
        // FIXME: Applying functions to tokens does not work
        // $PTII/bin/ptcg -language java $PTII/ptolemy/actor/lib/xslt/test/auto/XSLTransformerTest.xml
        result.append("->" + node.getMethodName() + "()");
        _childCode = result.toString();
    }

    /** Evaluate the power operator on the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //_fireCode.append("(");
        StringBuffer result = new StringBuffer();

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        //int startIndex = _childCode.length();

        // Operator is always exponentiation
        // Note that since we use an iterative integer method, instead of
        // a logarithmic method, the fastest thing is to apply the
        // exponentiation inside out, i.e. left to right.
        //ptolemy.data.Token result = tokens[0];
        ptolemy.data.Token childToken = _evaluateChild(node, 0);
        result.append(_childCode);

        Type resultType = ((ASTPtRootNode) node.jjtGetChild(0)).getType();

        for (int i = 1; i < numChildren; i++) {
            //int times = 1;
            //_fireCode.insert(startIndex, "pow(");
            //_fireCode.append(", ");

            //ptolemy.data.Token token = tokens[i];
            /*ptolemy.data.Token token =*/_evaluateChild(node, i);

            result = new StringBuffer("(" + _targetType(resultType) + ")"
                    + _powCall(result.toString(), _childCode));

            // Note that we check for ScalarTokens because anything
            // that has a meaningful intValue() method, such as
            // UnsignedByteToken will also work here.
            /*
            if (!(token instanceof ScalarToken)) {
                throw new IllegalActionException(
                        "Exponent must be ScalarToken and have a valid "
                                + "lossless conversion to integer. Integer or "
                                + "unsigned byte meet these criteria.\n"
                                + "Use pow(10, 3.5) for non-integer exponents");
            }

            try {
                times = ((ptolemy.data.ScalarToken) token).intValue();
            } catch (IllegalActionException ex) {
                throw new IllegalActionException("Exponent must have a valid "
                        + "lossless conversion to integer. Integer or "
                        + "unsigned byte meet this criterion.\n"
                        + "Use pow(10, 3.5) for non-integer exponents");
            }
             */
            //childToken = childToken.pow(times);
            //_fireCode.append(")");
            //result.append(")");
        }

        _evaluatedChildToken = childToken;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}

        //_fireCode.append(")");
        _childCode = "(" + result + ")";
    }

    /** Multiply the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        //_fireCode.append("(");
        StringBuffer result = new StringBuffer();

        //ptolemy.data.Token result = tokens[0];
        ptolemy.data.Token childToken = _evaluateChild(node, 0);
        result.append(_childCode);
        Type resultType = ((ASTPtRootNode) node.jjtGetChild(0)).getType();

        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);

            _evaluateChild(node, i);

            Type type = ((ASTPtRootNode) node.jjtGetChild(i)).getType();

            if (operator.kind == PtParserConstants.MULTIPLY) {
                if (type != null) {
                    result = new StringBuffer("$multiply_"
                            + _codeGenType(resultType) + "_"
                            + _codeGenType(type) + "(" + result.toString()
                            + ", " + _childCode + ")");

                    resultType = resultType.multiply(type);

                } else {
                    result.append("*" + _childCode);
                }
            } else if (operator.kind == PtParserConstants.DIVIDE) {
                if (type != null) {
                    result = new StringBuffer("$divide_"
                            + _codeGenType(resultType) + "_"
                            + _codeGenType(type) + "(" + result + ", "
                            + _childCode + ")");

                    resultType = resultType.divide(type);

                } else {
                    result.append("/" + _childCode);
                }
            } else if (operator.kind == PtParserConstants.MODULO) {
                if (type != null) {
                    result = new StringBuffer("$modulo_"
                            + _codeGenType(resultType) + "_"
                            + _codeGenType(type) + "(" + result.toString()
                            + ", " + _childCode + ")");

                    resultType = resultType.divide(type);

                } else {
                    result.append("%" + _childCode);
                }
            }

            if (operator.kind == PtParserConstants.MULTIPLY) {
                //childToken = childToken.multiply(nextToken);
            } else if (operator.kind == PtParserConstants.DIVIDE) {
                //childToken = childToken.divide(nextToken);
            } else if (operator.kind == PtParserConstants.MODULO) {
                //childToken = childToken.modulo(nextToken);
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        //_fireCode.append(")");
        _childCode = "(" + result + ")";

        _evaluatedChildToken = childToken;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
    }

    /** Construct a record by assigning the fields values given by
     *  the children nodes.
     *  @param node The record constructor node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }
        int numChildren = node.jjtGetNumChildren();

        StringBuffer result = new StringBuffer();

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        ptolemy.data.Token[] tokens = new ptolemy.data.Token[numChildren];
        for (int i = 0; i < numChildren; i++) {
            tokens[i] = _evaluateChild(node, i);
            result.append(_childCode);
        }

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

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}

        _childCode = result.toString();
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        //_fireCode.append("(");
        StringBuffer result = new StringBuffer();

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        Token operator = node.getOperator();
        ptolemy.data.Token leftToken = _evaluateChild(node, 0);
        result.append(_childCode);

        if (operator.kind == PtParserConstants.EQUALS) {
            //_fireCode.append(" == ");
            result.append(" == ");
        } else if (operator.kind == PtParserConstants.NOTEQUALS) {
            //_fireCode.append(" != ");
            result.append(" != ");
        } else if (operator.kind == PtParserConstants.GTE) {
            //_fireCode.append(" >= ");
            result.append(" >= ");
        } else if (operator.kind == PtParserConstants.GT) {
            //_fireCode.append(" > ");
            result.append(" > ");
        } else if (operator.kind == PtParserConstants.LTE) {
            //_fireCode.append(" <= ");
            result.append(" <= ");
        } else if (operator.kind == PtParserConstants.LT) {
            //_fireCode.append(" < ");
            result.append(" < ");
        }

        ptolemy.data.Token rightToken = _evaluateChild(node, 1);
        //ptolemy.data.Token resultToken = null;

        //_fireCode.append(")");
        _childCode = "(" + result + _childCode + ")";

        if (operator.kind == PtParserConstants.EQUALS) {
            //resultToken = leftToken.isEqualTo(rightToken);
        } else if (operator.kind == PtParserConstants.NOTEQUALS) {
            //resultToken = leftToken.isEqualTo(rightToken).not();
        } else { /*
                              if (!((leftToken instanceof ScalarToken) && (rightToken instanceof ScalarToken))) {
                              throw new IllegalActionException("The " + operator.image
                              + " operator can only be applied between scalars.");
                              }

                              ScalarToken leftScalar = (ScalarToken) leftToken;
                              ScalarToken rightScalar = (ScalarToken) rightToken;
         */

            if (operator.kind == PtParserConstants.GTE) {
                //result = leftScalar.isLessThan(rightScalar).not();
            } else if (operator.kind == PtParserConstants.GT) {
                //result = rightScalar.isLessThan(leftScalar);
            } else if (operator.kind == PtParserConstants.LTE) {
                //result = rightScalar.isLessThan(leftScalar).not();
            } else if (operator.kind == PtParserConstants.LT) {
                //result = leftScalar.isLessThan(rightScalar);
            } else {
                throw new IllegalActionException("Invalid operation "
                        + operator.image + " between "
                        + leftToken.getClass().getName() + " and "
                        + rightToken.getClass().getName());
            }
        }

        // FindBugs reports "Load of known null value"
        //_evaluatedChildToken = (resultToken);
        _evaluatedChildToken = null;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
    }

    /** Apply a shift operator to the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        Token operator = node.getOperator();

        //ptolemy.data.Token token = tokens[0];
        //ptolemy.data.Token bitsToken = tokens[1];

        //_fireCode.append("(");

        /*ptolemy.data.Token token =*/_evaluateChild(node, 0);
        StringBuffer result = new StringBuffer(_childCode);

        if (operator.kind == PtParserConstants.SHL) {
            //_fireCode.append(" << ");
            result.append(" << ");
        } else if (operator.kind == PtParserConstants.SHR) {
            //_fireCode.append(" >> ");
            result.append(" >> ");
        } else if (operator.kind == PtParserConstants.LSHR) {
            //_fireCode.append(" >>> ");
            result.append(" >>> ");
        }

        /*ptolemy.data.Token bitsToken =*/_evaluateChild(node, 1);
        //ptolemy.data.Token resultToken = null;

        //_fireCode.append(")");
        _childCode = "(" + result + _childCode + ")";

        /*
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
                resultToken = ((ScalarToken) token)
                        .leftShift(((ScalarToken) bitsToken).intValue());
            } else if (operator.kind == PtParserConstants.SHR) {
                resultToken = ((ScalarToken) token)
                        .rightShift(((ScalarToken) bitsToken).intValue());
            } else if (operator.kind == PtParserConstants.LSHR) {
                resultToken = ((ScalarToken) token)
                        .logicalRightShift(((ScalarToken) bitsToken).intValue());
            } else {
                _assert(false, node, "Invalid operation");
            }
        } catch (IllegalActionException ex) {
            throw new IllegalActionException("The " + operator
                    + " operator requires "
                    + "the right operand to have an integer value.");
        }
         */

        // FindBugs reports "Load of known null value"
        //_evaluatedChildToken = (resultToken);
        _evaluatedChildToken = null;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
    }

    /** Apply a sum operator to the children of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        //ptolemy.data.Token result = tokens[0];
        StringBuffer result = new StringBuffer();

        ptolemy.data.Token childToken = _evaluateChild(node, 0);

        String childType = _codeGenType(((ASTPtRootNode) node.jjtGetChild(0))
                .getType());

        String nodeType = _codeGenType(node.getType());

        result.append("$convert_" + childType + "_" + nodeType + "("
                + _childCode + ")");

        for (int i = 1; i < numChildren; i++) {
            childType = _codeGenType(((ASTPtRootNode) node.jjtGetChild(i))
                    .getType());

            Token operator = (Token) lexicalTokenList.get(i - 1);

            //ptolemy.data.Token nextToken = tokens[i];
            if (operator.kind == PtParserConstants.PLUS) {
                result = new StringBuffer("$add_" + nodeType + "_" + childType
                        + "(" + result.toString() + ", ");

                //childToken = childToken.add(_evaluateChild(node, i));
            } else if (operator.kind == PtParserConstants.MINUS) {
                result = new StringBuffer("$subtract_" + nodeType + "_"
                        + childType + "(" + result.toString() + ", ");

                //childToken = childToken.subtract(_evaluateChild(node, i));
            } else {
                _assert(false, node, "Invalid operation");
            }

            _evaluateChild(node, i);
            result.append(_childCode + ")");
        }

        _childCode = "(" + result + ")";

        _evaluatedChildToken = childToken;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
    }

    /** Apply a unary operator to the single child of the specified node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            //_fireCode.append(_evaluatedChildToken.toString());
            _childCode = _evaluatedChildToken.toString();
            return;
        }

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        _assert(node.jjtGetNumChildren() == 1, node,
                "Unary node must have exactly one child!");

        String result = "";

        if (node.isMinus()) {
            //_fireCode.append("-");
            result = "-";
        } else if (node.isNot()) {
            //_fireCode.append("!");
            result = "!";
        } else if (node.isBitwiseNot()) {
            //_fireCode.append("~");
            result = "~";
        }

        //ptolemy.data.Token result = tokens[0];
        ptolemy.data.Token childToken = _evaluateChild(node, 0);
        _childCode = result + _childCode;

        /*
         if (node.isMinus()) {
         result = result.zero().subtract(result);
         } else if (node.isNot()) {
         if (result instanceof BooleanToken) {
         //result = ((BooleanToken) result).not();
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

         //result = (ptolemy.data.Token) ((BitwiseOperationToken) result)
         //        .bitwiseNot();
         } else {
         _assert(false, node, "Unrecognized unary node");
         }
         */
        _evaluatedChildToken = childToken;

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}
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

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
     */
    protected String _codeGenType(Type ptType) {
        // FIXME: this is duplicated code from CodeGeneratorHelper.codeGenType

        // FIXME: We may need to add more types.
        // FIXME: We have to create separate type for different matrix types.
        String result = ptType == BaseType.INT ? "Int"
                : ptType == BaseType.LONG ? "Long"
                        : ptType == BaseType.STRING ? "String"
                                : ptType == BaseType.DOUBLE ? "Double"
                                        : ptType == BaseType.BOOLEAN ? "Boolean"
                                                : ptType == BaseType.UNSIGNED_BYTE ? "UnsignedByte"
                                                        //: ptType == PointerToken.POINTER ? "Pointer"
                                                        : ptType == BaseType.COMPLEX ? "Complex"
                                                                // FIXME: Why do we have to use equals with BaseType.OBJECT
                                                                : ptType.equals(BaseType.OBJECT) ? "Object"
                                                                        //: ptType == BaseType.OBJECT ? "Object"
                                                                        : null;

        if (result == null) {
            if (ptType instanceof ArrayType) {
                //result = codeGenType(((ArrayType) ptType).getElementType()) + "Array";
                result = "Array";
            } else if (ptType instanceof MatrixType) {
                //result = ptType.getClass().getSimpleName().replace("Type", "");
                result = "Matrix";
            }
        }

        //if (result.length() == 0) {
        //    throw new IllegalActionException(
        //            "Cannot resolve codegen type from Ptolemy type: " + ptType);
        //}

        // Java specific changes
        if (result != null) {
            return result.replace("Int", "Integer").replace("Array", "Token");
        }
        return result;
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them; this will cause their token
     *  value to be determined.
     *  @param node The node whose children are evaluated.
     *  @return The array of resulting tokens.
     *  @exception IllegalActionException If an parse error occurs.
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
     *  @param type The element type.
     *  @exception IllegalActionException If an parse error occurs.
     */
    protected void _evaluateArrayIndex(ASTPtRootNode node,
            ptolemy.data.Token value, Type type) throws IllegalActionException {

        //_fireCode.append("Array_get(");
        StringBuffer result = new StringBuffer("Array_get(");

        String name = value.toString();
        if (name.startsWith("object(")) {
            //_fireCode.append(name.substring(7, name.length() - 1) + ", ");
            result.append(name.substring(7, name.length() - 1) + ", ");
        }

        // get the array index
        _evaluateChild(node, 1);

        //_fireCode.append(")");
        result.append(_childCode + ")");

        Type elementType = ((ArrayType) type).getElementType();

        //_fireCode.append(".payload." + _codeGenType(elementType));
        _childCode = result.toString() + ".payload/*jptcg*/."
                + _codeGenType(elementType);
    }

    /** Evaluate the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     *  @param node The given node.
     *  @param i The given index.
     *  @return The resulting token.
     *  @exception IllegalActionException If an parse error occurs.
     */
    protected ptolemy.data.Token _evaluateChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        _traceEnter(child);
        child.visit(this);
        _traceLeave(child);
        return _evaluatedChildToken;
    }

    //     /** Evaluate the Matrix index operation represented by the given node.
    //      *  @param node The node that caused this method to be called.
    //      *  @param value The token that is being indexed into, which must
    //      *   be a MatrixToken.
    //      *  @param rowIndex The row index, which must be an integer token.
    //      *  @param columnIndex The column index, which must be an integer token.
    //      *  @return The element of the given token at the given index.
    //      *  @exception IllegalActionException If an parse error occurs.
    //      */
    //     protected ptolemy.data.Token _evaluateMatrixIndex(ASTPtRootNode node,
    //             ptolemy.data.Token value, ptolemy.data.Token rowIndex,
    //             ptolemy.data.Token columnIndex) throws IllegalActionException {
    //         if (!(value instanceof MatrixToken)) {
    //             throw new IllegalActionException(
    //                     "Matrix indexing cannot be applied to '" + value.toString()
    //                             + "' because its value is not a matrix.");
    //         }

    //         if (!(rowIndex instanceof IntToken)) {
    //             throw new IllegalActionException(
    //                     "Matrix row index must be an integer. Got: " + rowIndex);
    //         }

    //         if (!(columnIndex instanceof IntToken)) {
    //             throw new IllegalActionException(
    //                     "Matrix column index must be an integer. Got: "
    //                             + columnIndex);
    //         }

    //         int integerRowIndex = ((IntToken) rowIndex).intValue();
    //         int integerColumnIndex = ((IntToken) columnIndex).intValue();

    //         try {
    //             return ((MatrixToken) value).getElementAsToken(integerRowIndex,
    //                     integerColumnIndex);
    //         } catch (ArrayIndexOutOfBoundsException ex) {
    //             throw new IllegalActionException("The index (" + rowIndex + ","
    //                     + columnIndex + ") is out of bounds on the matrix '"
    //                     + value + "'.");
    //         }
    //     }

    /** Return the string for the the pow() call.
     *  @param x The first argument for pow().
     *  @param y The second argument for pow().
     *  @return The string to invoke the pow() function.
     */
    protected String _powCall(String x, String y) {
        return "Math.pow((double)" + x + ", (double)" + y + ")";
    }

    /** Specialize an argument of a function.
     *  The function "$arrayRepeat" is handled specially here.
     *  @param function The function
     *  @param argumentIndex The index of the argument to be specialized
     *  @param argumentType The type of the the argument.
     *  @param argumentCode The code for the argument.
     *  @return the specialized return value.
     */
    protected String _specializeArgument(String function, int argumentIndex,
            Type argumentType, String argumentCode) {

        if (function.equals("$arrayRepeat") && argumentIndex == 1) {
            if (_isPrimitive(argumentType)) {
                return "$new(" + _codeGenType(argumentType) + "("
                        + argumentCode + "))";
            }
        }
        return argumentCode;
    }

    /** Specialize the return value of a function.
     *  The function "$arraySum" is handled specially here.
     *  @param function The function
     *  @param returnType The return type of the function
     *  @param returnCode If the function is "$arraySum", and the return
     *  type is primitive, then the value of the returnCode parameter
     *  is returned with the payload and codeGen type returned.
     *  Otherwise, just the returnCode is returned.
     *  @return the specialized return value.
     */
    protected String _specializeReturnValue(String function, Type returnType,
            String returnCode) {
        if (function.equals("$arraySum") && _isPrimitive(returnType)) {

            returnCode += ".payload." + _codeGenType(returnType);
        }
        return returnCode;
    }

    /** Add a record to the current trace corresponding to the given message.
     *  If the trace is null, do nothing.
     *  @param string The given message.
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
     *  @param node The given node.
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
     *  @param node The given node.
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Temporary storage for the result of evaluating a child node.
     *  This is protected so that derived classes can access it.
     */
    protected ptolemy.data.Token _evaluatedChildToken = null;

    /** The fire() method code. */
    //protected StringBuffer _fireCode = new StringBuffer();
    protected String _childCode;

    /** The code generator. */
    protected ProgramCodeGenerator _generator;

    /** The initialize() method code. */
    protected StringBuffer _initializeCode = new StringBuffer();

    /** The preinitialize() method code. */
    protected StringBuffer _preinitializeCode = new StringBuffer();

    /** Shared code code. */
    protected StringBuffer _sharedCode = new StringBuffer();

    /** The wrapup() method code. */
    protected StringBuffer _wrapupCode = new StringBuffer();

    /** The scope for evaluation. */
    protected ParserScope _scope = null;

    /** Used for type checking. */
    protected ParseTreeTypeInference _typeInference = null;

    /** Used for debugging. */
    protected StringBuffer _trace = null;

    /** The depth, used for debugging and indenting. */
    protected int _depth = 0;

    /** The map of functions. */
    protected static Map<String, String> _functionMap = new HashMap();
    static {
        //_functionMap.put("matrixToArray", "$matrixToArray");
    }

    /**
     * Determine if the given type is primitive.
     * @param ptType The given ptolemy type.
     * @return true if the given type is primitive, otherwise false.
     */
    private boolean _isPrimitive(Type ptType) {
        // FIXME: this is duplicated code from CodeGeneratorHelper.isPrimitive()
        return _primitiveTypes.contains(_codeGenType(ptType));
    }

    /**
     * Get the corresponding type in Java from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The Java data type.
     */
    private String _targetType(Type ptType) {
        // FIXME: this is duplicated code from CodeGeneratorHelper.targetType()
        // FIXME: we may need to add more primitive types.
        return ptType == BaseType.INT ? "int"
                : ptType == BaseType.STRING ? "String"
                        : ptType == BaseType.DOUBLE ? "double"
                                : ptType == BaseType.BOOLEAN ? "boolean"
                                        : ptType == BaseType.LONG ? "long"
                                                : ptType == BaseType.UNSIGNED_BYTE ? "byte"
                                                        //: ptType == PointerToken.POINTER ? "void*"
                                                        : "Token";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A static list of the primitive types supported by the code generator. */
    private static final List _primitiveTypes = Arrays.asList(new String[] {
            "Integer", "Double", "String", "Long", "Boolean", "UnsignedByte",
            "Pointer", "Complex", "Object" });

}
