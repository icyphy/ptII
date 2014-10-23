/* The default adapter class for ptolemy.data.expr.ASTPtUnaryNode.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.data.expr;

import java.util.List;

import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionDefinitionAttribute;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.lattice.LatticeOntologyASTNodeAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtUnaryNode

/**
 The default adapter class for ptolemy.data.expr.ASTPtUnaryNode.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ASTPtUnaryNode extends LatticeOntologyASTNodeAdapter {

    /** Construct an property constraint adapter for the given ASTPtUnaryNode.
     *  @param solver The given solver to get the lattice from.
     *  @param node The given ASTPtUnaryNode.
     *  @exception IllegalActionException Thrown if the parent construct
     *   throws it.
     */
    public ASTPtUnaryNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtUnaryNode node)
                    throws IllegalActionException {
        super(solver, node, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraint list for the adapter. A Unary node in the Ptolemy
     *  expression language is a node with a single child node and an operator that
     *  is either a negation (-), a logical not (!), or a bitwise not (~). This
     *  ontology constraint supports using the concept functions for the negation and
     *  logical not operations if they are defined in the LatticeOntologySolver's model.
     *  If the concept functions are not defined or the operation is bitwise not, then
     *  the constraint is just between the node and its child node.
     *  @exception IllegalActionException If there is an error building the constraint list.
     *  @return The list of constraints for this adapter.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {

        ptolemy.data.expr.ASTPtUnaryNode node = (ptolemy.data.expr.ASTPtUnaryNode) _getNode();
        InequalityTerm[] childNodeTerms = _getChildNodeTerms();

        if (node.isMinus()) {
            ConceptFunction negateFunction = null;
            ConceptFunctionDefinitionAttribute negateDefinition = (ConceptFunctionDefinitionAttribute) _solver
                    .getContainedModel().getAttribute(
                            LatticeOntologySolver.NEGATE_FUNCTION_NAME);
            if (negateDefinition != null) {
                negateFunction = negateDefinition.createConceptFunction();
                if (negateFunction != null) {
                    setAtLeast(node, new ConceptFunctionInequalityTerm(
                            negateFunction, childNodeTerms));
                }
            }
        } else if (node.isNot()) {
            ConceptFunction notFunction = null;
            ConceptFunctionDefinitionAttribute notDefinition = (ConceptFunctionDefinitionAttribute) _solver
                    .getContainedModel().getAttribute(
                            LatticeOntologySolver.NOT_FUNCTION_NAME);
            if (notDefinition != null) {
                notFunction = notDefinition.createConceptFunction();
                if (notFunction != null) {
                    setAtLeast(node, new ConceptFunctionInequalityTerm(
                            notFunction, childNodeTerms));
                }
            }
        }

        return super.constraintList();
    }
}
