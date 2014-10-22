/* Adapter for LeafNodes in the monotonicity analysis.

 Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.data.expr;

import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.LatticeOntologyASTNodeAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept;
import ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConceptFunction;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtFunctionalIfNode

/**
 Adapter for RelationalNodes in the monotonicity analysis.

 @author Ben Lickly
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ASTPtLeafNode extends LatticeOntologyASTNodeAdapter {

    /**
     * Construct an property constraint adapter for the given ASTPtRelationalNode.
     * @param solver The given solver to get the lattice from.
     * @param node The given ASTPtRelationalNode.
     * @exception IllegalActionException Thrown if the parent construct
     *  throws it.
     */
    public ASTPtLeafNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtLeafNode node) throws IllegalActionException {
        super(solver, node, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraint list for the adapter.
     *  @exception IllegalActionException If there is an error building the constraint list.
     *  @return The list of constraints for this adapter.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {

        ptolemy.data.expr.ASTPtLeafNode leafNode = (ptolemy.data.expr.ASTPtLeafNode) _getNode();

        ASTPtLeafNodeFunction astRelationFunction = new ASTPtLeafNodeFunction(
                leafNode, getSolver().getOntology(), getSolver()
                        .getAllContainedOntologies());

        ConceptFunctionInequalityTerm constraint = new ConceptFunctionInequalityTerm(
                astRelationFunction, _getChildNodeTerms());
        if (((LatticeOntologySolver) _solver).isLeastFixedPoint()) {
            setAtLeast(_getNode(), constraint);
        } else {
            setAtMost(_getNode(), constraint);
        }

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private inner class                    ////

    /** A representation of the monotonic function used to infer the
     *  monotonicity of leaf nodes in the abstract
     *  syntax trees of Ptolemy expressions.
     */
    private static class ASTPtLeafNodeFunction extends
            MonotonicityConceptFunction {
        // FindBugs indicates that this should be a static class.

        /** Create a new function for inferring the monotonicity concept
         *  over a leaf node, given the ontologies representing both the
         *  domain in question and the monotonicity analysis lattice itself.
         *
         *  @param leafNode The leaf node in question
         *  @param monotonicityOntology The monotonicity ontology.
         *  @param domainOntologies The ontologies over which the expression
         *    is defined.
         *  @exception IllegalActionException If a function cannot be created.
         */
        public ASTPtLeafNodeFunction(ptolemy.data.expr.ASTPtLeafNode leafNode,
                Ontology monotonicityOntology, List<Ontology> domainOntologies)
                throws IllegalActionException {
            super("MonotonicityASTPtLeafNodeFunction", 0, monotonicityOntology,
                    domainOntologies);
            _leafNode = leafNode;
        }

        /** Return the monotonicity concept that results from analyzing the
         *  leaf node.  This is independent of the input concept values.
         *
         *  @param inputConceptValues Ignored in this leaf node (should be an empty list).
         *  @return The monotonicity concept that this leaf node evaluates to.
         *  @exception IllegalActionException Thrown if there is an error getting
         *   the concept value from the original domain ontology with the
         *   leaf node's name string.
         */
        @Override
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {

            MonotonicityConcept result = MonotonicityConcept
                    .createMonotonicityConcept(_monotonicityAnalysisOntology);

            // Don't evaluate monotonicity of function names.
            if (_leafNode.jjtGetParent() instanceof ptolemy.data.expr.ASTPtFunctionApplicationNode
                    && _leafNode.jjtGetParent().jjtGetChild(0) == _leafNode) {
                return result;
            }

            // Check if the leaf is a constant.
            if (_leafNode.isConstant()) {
                return result;
            }

            String conceptString = _leafNode.getName();
            for (Ontology domainOntology : _domainOntologies) {
                if (domainOntology.getConceptByString(conceptString) != null) {
                    return result;
                }
            }

            // Otherwise, it is a free variable.
            result.putMonotonicity(conceptString, _monotonicConcept);
            return result;
        }

        /** The leaf node that this function is defined over.
         */
        private ptolemy.data.expr.ASTPtLeafNode _leafNode;
    }

}
