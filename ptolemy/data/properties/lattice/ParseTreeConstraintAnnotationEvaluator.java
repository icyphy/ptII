/* A visitor for parse trees of the expression language that infers properties.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice;

import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtBitwiseNode;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtMatrixConstructNode;
import ptolemy.data.expr.ASTPtPowerNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtShiftNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.ASTPtUnionConstructNode;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.properties.ParseTreeAnnotationEvaluator;
import ptolemy.kernel.util.IllegalActionException;

////ParseTreePropertyInference

/**
 This class visits parse trees and infers a property for each node in the
 parse tree.  This property is stored in the parse tree.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeConstraintAnnotationEvaluator extends
        ParseTreeAnnotationEvaluator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {

        // FIXME: handle PropertySet element.
        throw _unsupportedVisitException("ASTPtArrayConstructNode");
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
        try {
            super.visitLeafNode(node);

        } catch (IllegalActionException ex) {

            // The label may be a lattice element name.
            PropertyLattice lattice = ((PropertyConstraintHelper) _helper)
                    .getSolver().getLattice();

            _evaluatedObject = lattice.getElement(_getNodeLabel(node)
                    .toUpperCase());
        }

        // FIXME: Not handling AST constraint yet.
    }

    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtLogicalNode");
    }

    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtMatrixConstructNode");
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

        ((ASTPtRootNode) node.jjtGetChild(0)).visit(this);
        Object leftChild = _evaluatedObject;

        ((ASTPtRootNode) node.jjtGetChild(1)).visit(this);
        Object rightChild = _evaluatedObject;

        Token operator = node.getOperator();
        if (operator.kind == PtParserConstants.EQUALS) {
            ((PropertyConstraintHelper) _helper).setSameAsManualAnnotation(
                    leftChild, rightChild);

        } else if (operator.kind == PtParserConstants.GTE) {
            ((PropertyConstraintHelper) _helper).setAtLeastManualAnnotation(
                    leftChild, rightChild);

        } else if (operator.kind == PtParserConstants.LTE) {
            ((PropertyConstraintHelper) _helper).setAtLeastManualAnnotation(
                    rightChild, leftChild);

        } else {
            throw _unsupportedVisitException("operator not supported.");
        }
        _evaluatedObject = null;

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

    public void visitUnionConstructNode(ASTPtUnionConstructNode node)
            throws IllegalActionException {
        throw _unsupportedVisitException("ASTPtUnionConstructNode");
    }

}
