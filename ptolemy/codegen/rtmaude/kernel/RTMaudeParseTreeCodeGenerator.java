/* RTMaude Parse-tree Code generator class

 Copyright (c) 2009-2010 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN AS IS BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.rtmaude.kernel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
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
import ptolemy.data.expr.ASTPtPowerNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtShiftNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// RTMaudeParseTreeCodeGenerator

/**
 * Generate RTMaude code for expressions.
 *
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.AcceptedRating Red (kquine)
 * @Pt.ProposedRating Red (kquine)*
 */
public class RTMaudeParseTreeCodeGenerator extends AbstractParseTreeVisitor
        implements ParseTreeCodeGenerator {

    /**
     * Create a new instance of the RTMaude parse tree
     * code generator.
     */
    public RTMaudeParseTreeCodeGenerator() {
        super();
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.ParseTreeCodeGenerator#evaluateParseTree(ptolemy.data.expr.ASTPtRootNode, ptolemy.data.expr.ParserScope)
     */
    public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
            ParserScope scope) throws IllegalActionException {

        StringWriter writer = new StringWriter();

        _writer = new PrintWriter(writer);
        node.visit(this);
        this.result = writer.toString();
        return null;
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.ParseTreeCodeGenerator#generateFireCode()
     */
    public String generateFireCode() {
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitLeafNode(ptolemy.data.expr.ASTPtLeafNode)
     */
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            ptolemy.data.Token tok = node.getToken();
            String res = node.getToken().toString();

            //FIXME: complex value (e.g. 0.0 + 3.2i)
            if (tok instanceof StringToken) {
                _writer.print("# " + escapeForTargetLanguage(res));
            } else {
                _writer.print("# " + res);
            }
        } else {
            _writer.print(_transformLeaf(node.getName())); // when variable identifier
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitArrayConstructNode(ptolemy.data.expr.ASTPtArrayConstructNode)
     */
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _writer.print("{");
        _printChildrenSeparated(node, ", ", false);
        _writer.print("}");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitLogicalNode(ptolemy.data.expr.ASTPtLogicalNode)
     */
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image, true);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitBitwiseNode(ptolemy.data.expr.ASTPtBitwiseNode)
     */
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image, true);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitPowerNode(ptolemy.data.expr.ASTPtPowerNode)
     */
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, "^", true);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitProductNode(ptolemy.data.expr.ASTPtProductNode)
     */
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitRelationalNode(ptolemy.data.expr.ASTPtRelationalNode)
     */
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image, true);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitShiftNode(ptolemy.data.expr.ASTPtShiftNode)
     */
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image, true);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitSumNode(ptolemy.data.expr.ASTPtSumNode)
     */
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitUnaryNode(ptolemy.data.expr.ASTPtUnaryNode)
     */
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _writer.print("(");
        if (node.isMinus()) {
            _writer.print("- ");
        } else if (node.isNot()) {
            _writer.print("! ");
        } else {
            _writer.print("~ ");
        }
        _printChild(node, 0);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitFunctionalIfNode(ptolemy.data.expr.ASTPtFunctionalIfNode)
     */
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print(" ? ");
        _printChild(node, 1);
        _writer.print(" : ");
        _printChild(node, 2);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitFunctionApplicationNode(ptolemy.data.expr.ASTPtFunctionApplicationNode)
     */
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print("(");
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            _printChild(node, i);
            if (i < node.jjtGetNumChildren() - 1) {
                _writer.print(", ");
            }
        }
        _writer.print("))");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitFunctionDefinitionNode(ptolemy.data.expr.ASTPtFunctionDefinitionNode)
     */
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        _writer.print("(function(");

        List args = node.getArgumentNameList();
        int n = args.size();

        // Notice : type constraints are omitted.
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                _writer.print(", ");
            }
            _writer.print("'" + (String) args.get(i));
        }

        _writer.print(") ");
        node.getExpressionTree().visit(this);
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitMatrixConstructNode(ptolemy.data.expr.ASTPtMatrixConstructNode)
     */
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        int n = 0;
        int rowCount = node.getRowCount();
        int columnCount = node.getColumnCount();

        _writer.print("[");
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                _printChild(node, n++);
                if (j < columnCount - 1) {
                    _writer.print(", ");
                }
            }
            if (i < rowCount - 1) {
                _writer.print("; ");
            }
        }
        _writer.print("]");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitMethodCallNode(ptolemy.data.expr.ASTPtMethodCallNode)
     */
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print(" .. "); // .. instead of .
        _writer.print("'" + node.getMethodName()); //
        _writer.print("(");
        if (node.jjtGetNumChildren() > 1) {
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                if (i > 1) {
                    _writer.print(", ");
                }
                _printChild(node, i);
            }
        } else {
            _writer.print("nil");
        }
        _writer.print(")");
        _writer.print(")");
    }

    /* (non-Javadoc)
     * @see ptolemy.data.expr.AbstractParseTreeVisitor#visitRecordConstructNode(ptolemy.data.expr.ASTPtRecordConstructNode)
     */
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        Iterator names = node.getFieldNames().iterator();
        _writer.print("{");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                _writer.print(", ");
            }
            _writer.print("(" + "'" + names.next().toString()); //
            _writer.print(" <- "); // <- instead =
            _printChild(node, i);
            _writer.print(")");
        }
        _writer.print("}");
    }

    /**
     * Visits the index-th child of the given node, so that the
     * expression of the child node is generated.
     * @param node The node
     * @param index The index which will be visited
     * @exception IllegalActionException
     */
    private void _printChild(ASTPtRootNode node, int index)
            throws IllegalActionException {
        ((ASTPtRootNode) node.jjtGetChild(index)).visit(this);
    }

    /**
     * Visits all children of the given node, and the resulting
     * expression is the list with the given separators.
     * @param node The node
     * @param separatorList The list of separators
     * @exception IllegalActionException
     */
    private void _printChildrenSeparated(ASTPtRootNode node, List separatorList)
            throws IllegalActionException {

        char[] lefts = new char[separatorList.size()];
        Arrays.fill(lefts, '(');
        _writer.print(lefts); // starts with left parentheses

        Iterator separators = separatorList.iterator();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                _writer.print(" "
                        + _transformOp(((Token) separators.next()).image) + " ");
            }
            _printChild(node, i);
            if (i > 0) {
                _writer.print(')');
            }
        }
    }

    /**
     * Visits all children of the given node, and the resulting
     * expression is the list with the given separator.
     * @param node The node
     * @param string The separator
     * @exception IllegalActionException
     */
    private void _printChildrenSeparated(ASTPtRootNode node, String string,
            boolean paran) throws IllegalActionException {
        if (paran) {
            char[] lefts = new char[node.jjtGetNumChildren() - 1];
            Arrays.fill(lefts, '(');
            _writer.print(lefts); // starts with left parentheses
        }

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                _writer.print(" " + _transformOp(string) + " ");
            }
            _printChild(node, i);
            if (paran && i > 0) {
                _writer.print(')'); // close a parenthesis.
            }
        }
    }

    /**
     * Returns a RTMaude operator from the given Ptolemy expression operator.
     * @param op The given Ptolemy expression operator
     * @return The RTMaude operator
     */
    private static String _transformOp(String op) {
        if (op.equals("<")) {
            return "lessThan";
        } else if (op.equals(">")) {
            return "greaterThan";
        } else if (op.equals("==")) {
            return "equals";
        } else {
            return op;
            // && ||
            // & | #
            //<< >> >>>
        }
    }

    /**
     * Returns a RTMaude term from the given (Leaf) Ptolemy expression.
     * @param id The given Ptolemy expression.
     * @return The RTMaude term
     */
    private String _transformLeaf(String id) {
        Matcher m = Pattern.compile("(.*)_isPresent").matcher(id);
        if (m.matches()) {
            String pid = m.group(1);
            return "isPresent(" + "'" + pid + ")";
        } else {
            if (id.equals("Infinity")) {
                return id;
            } else {

                //TODO: built-in functions should be enclosed by "builtin(...)"
                return "'" + id;
            }
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.codegen.kernel.ParseTreeCodeGenerator#escapeForTargetLanguage(java.lang.String)
     */
    public String escapeForTargetLanguage(String string) {
        string = StringUtilities.substitute(string, "\\", "\\\\");
        string = StringUtilities.substitute(string, "\"", "\\\"");
        string = StringUtilities.substitute(string, "\n", "\\n");
        return string;
    }

    /** Used to accumulate generated strings. */
    protected PrintWriter _writer;

    private String result = null;
}
