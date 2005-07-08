/* A visitor for parse trees of the expression language.

 Copyright (c) 1998-2005 The Regents of the University of California
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

//////////////////////////////////////////////////////////////////////////
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
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtArrayConstructNode");
    }

    public void visitAssignmentNode(ASTPtAssignmentNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtAssignmentNode");
    }

    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtBitwiseNode");
    }

    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtFunctionApplicationNode");
    }

    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtFunctionDefinitionNode");
    }

    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtFunctionalIfNode");
    }

    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtLeafNode");
    }

    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtLogicalNode");
    }

    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtMatrixConstructNode");
    }

    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtMethodCallNode");
    }

    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtPowerNode");
    }

    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtProductNode");
    }

    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtRecordConstructNode");
    }

    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtRelationalNode");
    }

    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtShiftNode");
    }

    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtSumNode");
    }

    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtUnaryNode");
    }

    protected IllegalActionException _unsupportedVisitException(String name) {
        return new IllegalActionException("Nodes of type " + name
                + " cannot be visited by a " + getClass().getName() + ".");
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
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
     */
    protected void _visitChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        child.visit(this);
    }
}
