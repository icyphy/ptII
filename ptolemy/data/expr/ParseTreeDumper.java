/* A visitor that writes parse trees.

 Copyright (c) 2002-2014 The Regents of the University of California
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

import java.io.PrintStream;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ParseTreeDumper

/**
 This class implements a visitor that writes parse trees in a
 debug format.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeDumper extends AbstractParseTreeVisitor {
    /** Print the contents of a parse tree.
     *  @param root The parse tree to be displayed.
     */
    public void displayParseTree(ASTPtRootNode root) {
        _prefix = "";

        try {
            root.visit(this);
        } catch (IllegalActionException ex) {
            _stream.println(ex);
            ex.printStackTrace(_stream);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _displayNode(node);
    }

    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _displayNode(node);
    }

    /** Display the given node with the current prefix, recursing into
     *  the children of the node.
     *  @param node The node to be displayed.
     *  @exception IllegalActionException If there is a problem
     *  displaying the children.
     */
    protected void _displayNode(ASTPtRootNode node)
            throws IllegalActionException {
        if (node.isEvaluated()) {
            String str = node.toString(_prefix) + ", Token type: ";
            str = str + node.getToken().getClass().getName() + ", Value: ";
            _stream.println(str + node.getToken().toString());
        } else {
            _stream.println(node.toString(_prefix) + "  _ptToken is null");
        }

        _stream.println(" static type is " + node.getType());

        if (node.jjtGetNumChildren() > 0) {
            String oldPrefix = _prefix;
            _prefix = " " + oldPrefix;

            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
                child.visit(this);
            }

            _prefix = oldPrefix;
        }
    }

    private String _prefix;

    private PrintStream _stream = System.out;
}
