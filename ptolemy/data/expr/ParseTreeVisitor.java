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
//// ParseTreeVisitor

/**
 This class implements the visitor pattern for parse trees in the
 expression language.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public interface ParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException;

    public void visitAssignmentNode(ASTPtAssignmentNode node)
            throws IllegalActionException;

    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException;

    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException;

    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException;

    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException;

    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException;

    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException;

    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException;

    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException;

    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException;

    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException;

    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException;

    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException;

    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException;

    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException;

    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException;
}
