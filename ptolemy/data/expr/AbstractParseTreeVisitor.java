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

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// AbstractParseTreeVisitor

/**
 This class implements a base class visitor for parse trees in the
 expression language.  Primarily this class exists to give nice error
 messages for visitors that are partly implemented, and to allow us to
 extend the expression language without completely breaking existing
 code.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class AbstractParseTreeVisitor implements ParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtArrayConstructNode");
    }

    @Override
    public void visitAssignmentNode(ASTPtAssignmentNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtAssignmentNode");
    }

    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtBitwiseNode");
    }

    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtFunctionApplicationNode");
    }

    @Override
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtFunctionDefinitionNode");
    }

    @Override
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtFunctionalIfNode");
    }

    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtLeafNode");
    }

    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtLogicalNode");
    }

    @Override
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtMatrixConstructNode");
    }

    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtMethodCallNode");
    }

    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtPowerNode");
    }

    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtProductNode");
    }

    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtRecordConstructNode");
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtRelationalNode");
    }

    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtShiftNode");
    }

    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtSumNode");
    }

    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtUnaryNode");
    }

    @Override
    public void visitUnionConstructNode(ASTPtUnionConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtUnionConstructNode");
    }

    /** Return an exception that describes an unsupported node type.
     *  @param name The name of the node type.
     *  @return An exception that describes an unsupported node type.
     */
    protected IllegalActionException _unsupportedVisitException(String name) {
        new Exception("Unsuppported...").printStackTrace();
        return new IllegalActionException("Nodes of type " + name
                + " cannot be visited by a " + getClass().getName() + ".");
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     *  @param node The node whose children are to be looped through.
     *  @exception IllegalActionException If thrown while visiting a child
     *  node.
     */
    protected void _visitAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numChildren; i++) {
            _visitChild(node, i);
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     *  @param node The node.
     *  @param i The index of the child to be visited.
     *  @exception IllegalActionException If thrown while visiting a child
     *  node.
     */
    protected void _visitChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        child.visit(this);
    }
}
