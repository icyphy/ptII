/* A visitor that writes parse trees.

 Copyright (c) 2002-2003 The Regents of the University of California
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeWriter
/**
This class implements a visitor that writes parse trees in the
expression language.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeWriter extends AbstractParseTreeVisitor {

    public void displayParseTree(ASTPtRootNode root) {
        _prefix = "";
        _writer = new PrintWriter(System.out);
        try {
            root.visit(this);
        } catch (IllegalActionException ex) {
            _writer.println(ex);
            ex.printStackTrace(_writer);
        }
    }

    public String printParseTree(ASTPtRootNode root) {
        _prefix = "";
        StringWriter writer = new StringWriter();
        _writer = new PrintWriter(writer);
        try {
            root.visit(this);
        } catch (IllegalActionException ex) {
            _writer.println(ex);
            ex.printStackTrace(_writer);
        }
        return writer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _writer.print("{");
        _printChildrenSeparated(node, ", ");
        _writer.print("}");
    }
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException  {
        _printChild(node, 0);
        _writer.print("(");
        int n = node.jjtGetNumChildren();
        for (int i = 1; i < n - 1; ++i) {
            _printChild(node, i);
            _writer.print(", ");
        }
        if (n > 1) {
            _printChild(node, n - 1);
        }
        _writer.print(")");
    }
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException  {
        // This code is duplicated with the FunctionToken.
        _writer.print("(function(");
        List args = node.getArgumentNameList();
        ptolemy.data.type.Type[] argTypes = node.getArgumentTypes();
        int n = args.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                _writer.print(", ");
            }
            _writer.print((String)args.get(i));
            ptolemy.data.type.Type type = argTypes[i];
            if (type != ptolemy.data.type.BaseType.GENERAL) {
                _writer.print(":");
                _writer.print(type.toString());
            }
        }
        _writer.print(") ");
        node.getExpressionTree().visit(this);
        _writer.print(")");
    }
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChild(node, 0);
        _writer.print("?");
        _printChild(node, 1);
        _writer.print(":");
        _printChild(node, 2);
        _writer.print(")");
    }
    public void visitLeafNode(ASTPtLeafNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _writer.print(node.getToken().toString());
        } else {
            _writer.print(node.getName());
        }
    }
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _writer.print("[");
        int n = 0;
        int rowCount = node.getRowCount();
        int columnCount = node.getColumnCount();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                _printChild(node, n++);
                if (j < columnCount - 1) _writer.print(", ");
            }
            if (i < rowCount - 1) _writer.print("; ");
        }
        _writer.print("]");
    }
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _printChild(node, 0);
        _writer.print(".");
        _writer.print(node.getMethodName());
        _writer.print("(");
        if (node.jjtGetNumChildren() > 1) {
            _printChild(node, 1);
            for (int i = 2; i < node.jjtGetNumChildren(); i++) {
                _writer.print(", ");
                _printChild(node, i);
            }
        }
        _writer.print(")");
    }
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _printChildrenSeparated(node, "^");
    }
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        Iterator names = node.getFieldNames().iterator();
        _writer.print("{");
        if (node.jjtGetNumChildren() > 0) {
            _writer.print(names.next());
            _writer.print("=");
            _printChild(node, 0);
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                _writer.print(", ");
                _writer.print(names.next());
                _writer.print("=");
                _printChild(node, i);
            }
        }
        _writer.print("}");
    }
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getOperator().image);
        _writer.print(")");
    }
    public void visitSumNode(ASTPtSumNode node)
            throws IllegalActionException {
        _writer.print("(");
        _printChildrenSeparated(node, node.getLexicalTokenList());
        _writer.print(")");
    }
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        if (node.isMinus()) {
            _writer.print("-");
        } else if (node.isNot()) {
            _writer.print("!");
        } else {
            _writer.print("~");
        }
        _printChild(node, 0);
    }

    private void _printChild(ASTPtRootNode node, int index)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(index);
        child.visit(this);
    }

    private void _printChildrenSeparated(ASTPtRootNode node, String string)
            throws IllegalActionException {
        if (node.jjtGetNumChildren() > 0) {
            _printChild(node, 0);
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                _writer.print(string);
                _printChild(node, i);
            }
        }
    }

    private void _printChildrenSeparated(
            ASTPtRootNode node, List separatorList)
            throws IllegalActionException {
        Iterator separators = separatorList.iterator();
        if (node.jjtGetNumChildren() > 0) {
            _printChild(node, 0);
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                Token separator = (Token)separators.next();
                _writer.print(separator.image);
                _printChild(node, i);
            }
        }
    }

    private String _prefix;
    private PrintWriter _writer = new PrintWriter(System.out);
}
