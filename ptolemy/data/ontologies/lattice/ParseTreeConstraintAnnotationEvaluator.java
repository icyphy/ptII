/* A visitor for parse trees of the expression language that infers properties.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import ptolemy.kernel.util.IllegalActionException;

////ParseTreePropertyInference

/**
 This class visits parse trees and infers a property for each node in the
 parse tree.  This property is stored in the parse tree.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeConstraintAnnotationEvaluator extends
ParseTreeAnnotationEvaluator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  visitLeafNode method is called when parsing an Annotation for a manual constraint.
     *  12/16/09 Charles Shelton
     *
     *  This visitLeafNode method assumes the node will refer to either a component
     *  in the model or a Concept in the ontology.  The _evaluatedObject will then be set to
     *  that component or Concept.
     *  If it is not, then an exception is thrown.
     *
     *  This method calls the superclass method for model Components, and then
     *  catches the superclass' exception to check for a Concept.
     *
     *  @param node The leaf node to be visited
     *  @exception IllegalActionException If the node label cannot be resolved to a
     *  component in the model
     */
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        try {
            super.visitLeafNode(node);

        } catch (IllegalActionException ex) {
            _evaluatedObject = _adapter.getSolver().getOntology()
                    .getConceptByString(_getNodeLabel(node));

            if (_evaluatedObject == null) {
                throw new IllegalActionException(_adapter.getSolver()
                        .getOntology(), "Cannot resolve label: "
                                + _getNodeLabel(node)
                                + ". There is no matching component in the model, "
                                + "and there is no matching Concept in the Ontology.");
            }
        }

        // FIXME: Not handling AST constraint yet.
    }

    /**
     * Visit the relational node when parsing a user-defined manual constraint
     * doe the LatticeOntologySolver.  It should be an operator that is either
     * '==', '&ge;', or '&le;'.
     *
     * @param node The relational node to be visited
     * @exception IllegalActionException If the operator is not supported (should be
     * one of '==', '&ge;', or '&le;')
     */
    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {

        ((ASTPtRootNode) node.jjtGetChild(0)).visit(this);
        Object leftChild = _evaluatedObject;

        ((ASTPtRootNode) node.jjtGetChild(1)).visit(this);
        Object rightChild = _evaluatedObject;

        Token operator = node.getOperator();
        if (operator.kind == PtParserConstants.EQUALS) {
            ((LatticeOntologyAdapter) _adapter)
            .setSameAs(leftChild, rightChild);

        } else if (operator.kind == PtParserConstants.GTE) {
            ((LatticeOntologyAdapter) _adapter).setAtLeast(leftChild,
                    rightChild);

        } else if (operator.kind == PtParserConstants.LTE) {
            ((LatticeOntologyAdapter) _adapter).setAtLeast(rightChild,
                    leftChild);

        } else {
            throw _unsupportedVisitException("operator not supported.");
        }
        _evaluatedObject = null;

    }

}
