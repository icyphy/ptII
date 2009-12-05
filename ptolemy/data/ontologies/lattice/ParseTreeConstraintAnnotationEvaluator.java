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
package ptolemy.data.ontologies.lattice;

import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.ontologies.ParseTreeAnnotationEvaluator;
import ptolemy.data.ontologies.PropertyLattice;
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
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        try {
            super.visitLeafNode(node);

        } catch (IllegalActionException ex) {

            // The label may be a lattice element name.
            PropertyLattice lattice = ((PropertyConstraintHelper) _adapter)
                    .getSolver().getLattice();

            _evaluatedObject = lattice.getElement(_getNodeLabel(node)
                    .toUpperCase());
        }

        // FIXME: Not handling AST constraint yet.
    }

    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {

        ((ASTPtRootNode) node.jjtGetChild(0)).visit(this);
        Object leftChild = _evaluatedObject;

        ((ASTPtRootNode) node.jjtGetChild(1)).visit(this);
        Object rightChild = _evaluatedObject;

        Token operator = node.getOperator();
        if (operator.kind == PtParserConstants.EQUALS) {
            ((PropertyConstraintHelper) _adapter).setSameAsManualAnnotation(
                    leftChild, rightChild);

        } else if (operator.kind == PtParserConstants.GTE) {
            ((PropertyConstraintHelper) _adapter).setAtLeastManualAnnotation(
                    leftChild, rightChild);

        } else if (operator.kind == PtParserConstants.LTE) {
            ((PropertyConstraintHelper) _adapter).setAtLeastManualAnnotation(
                    rightChild, leftChild);

        } else {
            throw _unsupportedVisitException("operator not supported.");
        }
        _evaluatedObject = null;

    }

}
