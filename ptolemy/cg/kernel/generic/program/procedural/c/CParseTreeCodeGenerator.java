/* A visitor for parse trees of the expression language.

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
package ptolemy.cg.kernel.generic.program.procedural.c;

import java.util.List;

import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralParseTreeCodeGenerator;
import ptolemy.data.ArrayToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtMatrixConstructNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ExpressionFunction;
import ptolemy.data.expr.ParseTreeSpecializer;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

////ParseTreeEvaluator

/**
 This class evaluates a parse tree given a reference to its root node.
 It implements a visitor that visits the parse tree in depth-first order,
 evaluating each node and storing the result as a token in the node.
 Two exceptions are logic nodes and the ternary if node (the ? : construct),
 which do not necessarily evaluate all children nodes.

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
public class CParseTreeCodeGenerator extends ProceduralParseTreeCodeGenerator {

    /**
     * Create a CParseTreeCodeGenerator that is used by
     * the given code generator to generate code for expressions.
     * @param generator The given code generator.
     */
    public CParseTreeCodeGenerator(ProgramCodeGenerator generator) {
        super(generator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        String[] childCode = new String[numChildren];

        ptolemy.data.Token[] tokens = new ptolemy.data.Token[numChildren];

        //ptolemy.data.Token[] tokens = _evaluateAllChildren(node);
        //_fireCode.append("$new(Array(" + numChildren + ", " + numChildren);
        //String[] elements = new String[numChildren];
        StringBuffer result = new StringBuffer(numChildren + ", " + numChildren);

        // Convert up to LUB.
        // Assume UNKNOWN is the lower type.
        ptolemy.data.type.Type elementType = BaseType.UNKNOWN;

        for (int i = 0; i < numChildren; i++) {
            //_fireCode.append(", ");

            //int nextIndex = _fireCode.length();

            if (_typeInference == null) {
                _typeInference = new ParseTreeTypeInference();
            }
            _typeInference.inferTypes(node, _scope);

            tokens[i] = _evaluateChild(node, i);

            childCode[i] = _childCode;

            Type valueType = ((ASTPtRootNode) node.jjtGetChild(i)).getType();
            if (!elementType.equals(valueType)) { // find max type
                elementType = TypeLattice.leastUpperBound(elementType,
                        valueType);
            }
        }

        for (int i = 0; i < numChildren; i++) {
            Type valueType = ((ASTPtRootNode) node.jjtGetChild(i)).getType();

            //tokens[i] = elementType.convert(tokens[i]);
            if (valueType.equals(elementType)) {
                result.append(", " + childCode[i]);
            } else {
                result.append(", $convert_" + _codeGenType(valueType) + "_"
                        + _codeGenType(elementType) + "(" + childCode[i] + ")");
            }
        }

        if (elementType instanceof ArrayType) {
            _childCode = "$new(" + "Array(" + result + "))";
        } else {
            _childCode = "$new(" + _codeGenType(elementType) + "Array("
                    + result + "))";

        }

        // Tests CParseTreeCodeGenerator-16.2 and
        // CParseTreeCodeGenerator-17.2 require that
        // _evaluatedChildToken be set here, otherwise
        // _evaluatedChildToken will be set to the value
        // of the last token.
        _evaluatedChildToken = new ArrayToken(elementType, tokens);

        //if (node.isConstant()) {
        //    node.setToken(_evaluatedChildToken);
        //}

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

        String functionCode = "";

        /*
        // FIXME: Generate function declaration in _preinitCode.
        functionCode += type.getReturnType().toString();
        functionCode += " $actorSymbol(function) (";
        List argumentNames = node.getArgumentNameList();
        Type[] argumentTypes = node.getArgumentTypes();

        if (argumentNames.size() > 0) {
            functionCode += argumentTypes[0] + " ";
            functionCode += argumentNames.get(0);

            for (int i = 1; i < argumentNames.size(); i++) {
                functionCode += ", " + argumentTypes[i] + " ";
                functionCode += argumentNames.get(i);
            }
        }
        functionCode += ") {\n";
        functionCode += "    return ";
        functionCode += evaluateParseTree(node.getExpressionTree(), _scope);
        functionCode += ";\n}\n";
         */

        _childCode = functionCode;
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
            if (_evaluatedChildToken instanceof StringToken) {
                // In C, Strings should have \n tags substituted.
                // See Test 17.2

                //_fireCode.append(escapeForTargetLanguage(_evaluatedChildToken.toString()));

                _childCode = escapeForTargetLanguage(_evaluatedChildToken
                        .toString());

            } else if (_evaluatedChildToken instanceof LongToken) {
                //_fireCode.append(((LongToken) _evaluatedChildToken).longValue() + "LL");
                _childCode = ((LongToken) _evaluatedChildToken).longValue()
                        + "LL";
            } else {
                //_fireCode.append(_evaluatedChildToken.toString());
                _childCode = _evaluatedChildToken.toString();
            }
            return;
        }

        String name = node.getName();
        boolean isPresentMark = false;

        if (name.endsWith("_isPresent")) {
            isPresentMark = true;
            name = name.substring(0, name.length() - 10);
        }

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
            if (isPresentMark) {
                String label2 = value.toString();
                if (label2.startsWith("object(")) {
                    label2 = label2.substring(7, label2.length() - 1)
                            + "_isPresent";
                } else {
                    label2 += "_isPresent";
                }
                value = new ObjectToken(label2);
            }
            _evaluatedChildToken = value;

            String label = value.toString();

            if (label.startsWith("object(")) {
                // If this is an ObjectToken, we only wants the label.
                //_fireCode.append(label.substring(7, label.length() - 1));
                _childCode = label.substring(7, label.length() - 1);
            } else {
                //_fireCode.append(label);
                _childCode = label;
            }

            return;
        }

        throw new IllegalActionException("The ID " + node.getName()
                + " is undefined.");
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
                    result.append(", ");

                    int index = i * column + j;

                    //int nextIndex = _fireCode.length();
                    tokens[index] = _evaluateChild(node, index);

                    Type valueType = tokens[index].getType();

                    if (_generator.isPrimitive(valueType)) {
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
            if (_generator.targetType(elementType).equals("Token")) {
                result.append(", -1");
            } else {
                result.append(", TYPE_" + codegenType);
            }
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
     */
    @Override
    protected String _codeGenType(Type ptType) {
        // This method exists because JavaParseTreeCodeGenerator specializes it.
        return _generator.codeGenType(ptType);
    }

    /** Evaluate the array index operation represented by the given node.
     *  @param node The node that caused this method to be called.
     *  @param value The token that is being indexed into, which must
     *   be an ArrayToken.
     *  @param type The element type.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
    protected void _evaluateArrayIndex(ASTPtRootNode node,
            ptolemy.data.Token value, Type type) throws IllegalActionException {

        Type elementType = ((ArrayType) type).getElementType();

        //_fireCode.append("Array_get(");
        StringBuffer result = new StringBuffer(_codeGenType(elementType)
                + "Array_get(");

        String name = value.toString();
        if (name.startsWith("object(")) {
            //_fireCode.append(name.substring(7, name.length() - 1) + ", ");
            result.append(name.substring(7, name.length() - 1) + ", ");
        }

        // get the array index
        _evaluateChild(node, 1);
        result.append(_childCode);

        //_fireCode.append(")");
        _childCode = result.toString() + ")";
    }

    /** Evaluate the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     *  @param node The given node.
     *  @param i The given index.
     *  @return The resulting token.
     *  @exception IllegalActionException If an parse error occurs.
     */
    @Override
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
    @Override
    protected String _powCall(String x, String y) {
        return "pow((double)" + x + ", (double)" + y + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    static {
        _functionMap.put("matrixToArray", "$matrixToArray");
        _functionMap.put("roundToInt", "(int)");
        _functionMap.put("repeat", "$arrayRepeat");
        _functionMap.put("sum", "$arraySum");
    }
}
