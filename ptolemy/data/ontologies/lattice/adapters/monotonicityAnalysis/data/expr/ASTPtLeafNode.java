/* Adapter for LeafNodes in the monotonicity analysis.

 Copyright (c) 2010 The Regents of the University of California.
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

import ptolemy.data.expr.Constants;
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
 @since Ptolemy II 8.0
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
            ptolemy.data.expr.ASTPtLeafNode node)
            throws IllegalActionException {
        super(solver, node, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraint list for the adapter.
     *  @throws IllegalActionException If there is an error building the constraint list.
     *  @return The list of constraints for this adapter.
     */
    public List<Inequality> constraintList() throws IllegalActionException {

        ptolemy.data.expr.ASTPtLeafNode leafNode = (ptolemy.data.expr.ASTPtLeafNode) _getNode();

        ASTPtLeafNodeFunction astRelationFunction = new ASTPtLeafNodeFunction(
                leafNode,
                getSolver().getOntology());

        setAtLeast(_getNode(), new ConceptFunctionInequalityTerm(
                astRelationFunction, _getChildNodeTerms()));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private inner class                    ////

    
    /** A representation of the monotonic function used to infer the
     *  monotonicity of conditional nodes (if nodes) in the abstract
     *  syntax trees of Ptolemy expressions.
     */
    private class ASTPtLeafNodeFunction extends MonotonicityConceptFunction {

        /** Create a new function for inferring the monotonicity concept
         *  over a relational node, given the operator at the node,
         *  the monotonicity ontology.
         *
         *  @param operator Token for the operator at this node.
         *  @param monotonicityOntology The monotonicity ontology.
         *  @throws IllegalActionException If a function cannot be created.
         */
        public ASTPtLeafNodeFunction(ptolemy.data.expr.ASTPtLeafNode leafNode,
                Ontology monotonicityOntology) throws IllegalActionException {
            super("MonotonicityASTPtLeafNodeFunction", 0,
                    monotonicityOntology);
            _leafNode = leafNode;
        }

        /** Return the monotonicity concept that results from analyzing the
         *  leaf node.  This is independent of the input concept values.
         */
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {

            MonotonicityConcept result = MonotonicityConcept.createMonotonicityConcept(_monotonicityAnalysisOntology);
            
            if (_leafNode.isIdentifier()) {
                // Seems like a hackey way to check that a leaf is not
                // actually a constant. I'm surprised that isConstant
                // doesn't check this.
                if (Constants.get(_leafNode.getName()) == null) {
                    result.putMonotonicity(_leafNode.getName(), _monotonicConcept);
                }
            }
            
            
            return result;
        }
        
        /** String representation of the operator for the relational node
         *  that this function is defined over. 
         */
        private ptolemy.data.expr.ASTPtLeafNode _leafNode;

    }

}
