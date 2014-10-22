/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.verification.kernel.maude;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// RTMLExpTranslator

/**
 * A Real-Time Maude Expression Translator.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class RTMExpTranslator extends AbstractParseTreeVisitor {

    public RTMExpTranslator(boolean time) {
        super();
        this.isTime = time;
    }

    public String translateExpression(String exp) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode pt = parser.generateParseTree(exp);
        return translateParseTree(pt);
    }

    public String translateParseTree(ASTPtRootNode root)
            throws IllegalActionException {
        StringWriter writer = new StringWriter();
        _writer = new PrintWriter(writer);
        root.visit(this);
        return writer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            String res = node.getToken().toString();
            try { // when number
                double num = Double.parseDouble(res);
                if (isTime) {
                    _writer.print("#r(" + _toRational(num) + ")");
                } else {
                    _writer.print("#f(" + num + ")");
                }
            } catch (NumberFormatException e) {
                if (res.equals("true") || res.equals("false")) {
                    _writer.print("#b(" + res + ")");
                } else {
                    _writer.print(res);
                }
            }
        } else {
            _writer.print(_transformLeaf(node.getName())); // when variable identifier
        }
    }

    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _writer.print("(| "); // (| instead {
        _printChildrenSeparated(node, ", ");
        _writer.print(" |)"); // |) instead }
    }

    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }

    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }

    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, "^");
        _writer.print(")");
    }

    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }

    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }

    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }

    @Override
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

    @Override
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

    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print(" $ (");
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            _printChild(node, i);
            if (i < node.jjtGetNumChildren() - 1) {
                _writer.print(", ");
            }
        }
        _writer.print("))");
    }

    @Override
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
            _writer.print(RTMTerm.transId((String) args.get(i)));
        }

        _writer.print(") ");
        node.getExpressionTree().visit(this);
        _writer.print(")");
    }

    @Override
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

    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print(" .. "); // -> instead of .
        _writer.print(RTMTerm.transId(node.getMethodName())); //
        _writer.print(" $ (");
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            if (i > 1) {
                _writer.print(", ");
            }
            _printChild(node, i);
        }
        _writer.print("))");
    }

    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        Iterator names = node.getFieldNames().iterator();
        if (node instanceof ASTPtOrderedRecordConstructNode) {
            _writer.print("[");
        } else {
            _writer.print("{");
        }

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                _writer.print(", ");
            }
            _writer.print("(" + RTMTerm.transId(names.next().toString())); //
            _writer.print(" <- "); // <- instead =
            _printChild(node, i);
            _writer.print(")");
        }
        if (node instanceof ASTPtOrderedRecordConstructNode) {
            _writer.print("]");
        } else {
            _writer.print("}");
        }
    }

    protected PrintWriter _writer = new PrintWriter(System.out);

    private boolean isTime = false;

    private void _printChild(ASTPtRootNode node, int index)
            throws IllegalActionException {
        ((ASTPtRootNode) node.jjtGetChild(index)).visit(this);
    }

    private void _printChildrenSeparated(ASTPtRootNode node, List separatorList)
            throws IllegalActionException {
        Iterator separators = separatorList.iterator();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                _writer.print(" "
                        + _transformOp(((Token) separators.next()).image) + " ");
            }
            _printChild(node, i);
        }
    }

    private void _printChildrenSeparated(ASTPtRootNode node, String string)
            throws IllegalActionException {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                _writer.print(" " + _transformOp(string) + " ");
            }
            _printChild(node, i);
        }
    }

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

    private static String _transformLeaf(String id) {
        Matcher m = Pattern.compile("(.*)_isPresent").matcher(id);
        if (m.matches()) {
            return "isPresent(" + RTMTerm.transId(m.group(1)) + ")";
        } else {
            return RTMTerm.transId(id);
        }

        // Infinity ...
    }

    private static String _toRational(double f) {
        double base = 1.0;
        // Findebugs: FE Test for floating point equality.
        // This looks ok, as this loop will run at least once.
        while (Math.ceil(f * base) != f * base) {
            base = base * 10.0;
        }

        int gcd = _GCD((int) f, (int) base);
        int nn = (int) (f * base) / gcd, nd = (int) base / gcd;

        if (nd > 1) {
            return nn + "/" + nd;
        } else {
            return nn + "";
        }
    }

    private static int _GCD(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return _GCD(b, a % b);
        }
    }

}
