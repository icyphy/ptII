/* Adapter for RelationalNodes in the monotonicity analysis.

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
import java.util.TreeSet;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.FiniteConcept;
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
public class ASTPtRelationalNode extends LatticeOntologyASTNodeAdapter {

    /**
     * Construct an property constraint adapter for the given ASTPtRelationalNode.
     * @param solver The given solver to get the lattice from.
     * @param node The given ASTPtRelationalNode.
     * @exception IllegalActionException Thrown if the parent construct
     *  throws it.
     */
    public ASTPtRelationalNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtRelationalNode node)
                    throws IllegalActionException {
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

        ptolemy.data.expr.ASTPtRelationalNode _relationalNode = (ptolemy.data.expr.ASTPtRelationalNode) _getNode();

        ASTPtRelationalNodeFunction astRelationFunction = new ASTPtRelationalNodeFunction(
                _relationalNode.getOperator(), getSolver().getOntology(),
                getSolver().getAllContainedOntologies());

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
    private static class ASTPtRelationalNodeFunction extends
    MonotonicityConceptFunction {
        // FindBugs indicates that this should be a static class.

        /** Create a new function for inferring the monotonicity concept
         *  over a relational node, given the operator at the node,
         *  the monotonicity ontology.
         *
         *  @param operator Token for the operator at this node.
         *  @param monotonicityOntology The monotonicity ontology.
         *  @param domainOntologies The ontologies over which the
         *   expression containing this relational node is defined.
         *  @exception IllegalActionException If a function cannot be created.
         */
        public ASTPtRelationalNodeFunction(ptolemy.data.expr.Token operator,
                Ontology monotonicityOntology, List<Ontology> domainOntologies)
                        throws IllegalActionException {
            super("defaultASTPtRelationalNodeFunction", 2,
                    monotonicityOntology, domainOntologies);
            _operator = operator.toString();
        }

        /** Return the monotonicity concept that results from running the
         *  monotonicity analysis on the given relational statement.
         *  We abuse the notation here slightly, as the return type of a
         *  relational statement (an inequality) is boolean, so the
         *  monotonicity of a relational statement would depend on an
         *  ordering of booleans.  This analysis assumes that true <= false.
         *  This means, for example, that for a monotonic variable x,
         *  x <= Constant
         *  is monotonic with respect to x, and
         *  x >= Constant
         *  is antimonotonic with respect to x.
         *
         *  @param inputConceptValues The list of concept inputs to the function.
         *    (i.e. The monotonicity of each of the conditional's branches)
         *  @return The monotonicity of the overall relational statement.
         *  @exception IllegalActionException If there is an error evaluating the function.
         *  @see ptolemy.data.ontologies.ConceptFunction#_evaluateFunction(java.util.List)
         */
        @Override
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {

            Concept c1 = inputConceptValues.get(0);
            Concept c2 = inputConceptValues.get(1);
            if (c1.equals(_monotonicityAnalysisOntology.getConceptGraph()
                    .bottom())
                    || c2.equals(_monotonicityAnalysisOntology
                            .getConceptGraph().bottom())) {
                return _monotonicityAnalysisOntology.getConceptGraph().bottom();
            } else if (c1 instanceof MonotonicityConcept
                    && c2 instanceof MonotonicityConcept) {
                MonotonicityConcept lhs = (MonotonicityConcept) c1;
                MonotonicityConcept rhs = (MonotonicityConcept) c2;

                MonotonicityConcept result = MonotonicityConcept
                        .createMonotonicityConcept(_monotonicityAnalysisOntology);
                TreeSet<String> variables = new TreeSet<String>(lhs.keySet());
                variables.addAll(rhs.keySet());

                for (String v : variables) {
                    FiniteConcept monotonicity = _evaluateFiniteConcept(
                            lhs.getMonotonicity(v), rhs.getMonotonicity(v));
                    result.putMonotonicity(v, monotonicity);
                }

                return result;
            } else {
                return _monotonicityAnalysisOntology.getConceptGraph().top();
            }
        }

        /** Return the single variable concept from the monotonicity lattice that
         *  results from analyzing the relational statement with respect
         *  to a given variable.  We again abuse the notation, assuming
         *  a relationship between boolean values of true <= false.
         *
         *  @param lhs The monotonicity of the left hand side of the
         *    relational node for a specific variable.  One of
         *    <code>Constant, Monotonic, Antimonotonic, Nonmonotonic</code>.
         *  @param rhs The monotonicity of the right hand side of the
         *    relational node for a specific variable.  One of
         *    <code>Constant, Monotonic, Antimonotonic, Nonmonotonic</code>.
         *  @return One of <code>Constant, Monotonic, Antimonotonic,
         *    Nonmonotonic</code>, depending on the result of the analysis.
         *  @exception IllegalActionException If there is an error evaluating the function.
         */
        private FiniteConcept _evaluateFiniteConcept(Concept lhs, Concept rhs)
                throws IllegalActionException {
            if (_constantConcept.isAboveOrEqualTo(lhs)
                    && _constantConcept.isAboveOrEqualTo(rhs)) {
                return _constantConcept;
            }
            boolean monotonicAntimonotonic = _monotonicConcept
                    .isAboveOrEqualTo(lhs)
                    && _antimonotonicConcept.isAboveOrEqualTo(rhs);
            boolean antimonotonicMonotonic = _antimonotonicConcept
                    .isAboveOrEqualTo(lhs)
                    && _monotonicConcept.isAboveOrEqualTo(rhs);
            if (_operator.equals("<=") || _operator.equals("<")) {
                if (monotonicAntimonotonic) {
                    return _monotonicConcept;
                } else if (antimonotonicMonotonic) {
                    return _antimonotonicConcept;
                }
            } else if (_operator.equals(">=") || _operator.equals(">")) {
                if (monotonicAntimonotonic) {
                    return _antimonotonicConcept;
                } else if (antimonotonicMonotonic) {
                    return _monotonicConcept;
                }
            }
            return _generalConcept;
        }

        /** String representation of the operator for the relational node
         *  that this function is defined over.
         */
        private String _operator;

    }
}
