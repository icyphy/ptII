/* The default adapter class for ptolemy.data.expr.ASTPtProductNode.

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionDefinitionAttribute;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.LatticeOntologyASTNodeAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtProductNode

/**
 The default adapter class for ptolemy.data.expr.ASTPtProductNode.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ASTPtProductNode extends LatticeOntologyASTNodeAdapter {

    /** Construct an property constraint adapter for the given ASTPtProductNode.
     *  @param solver The given solver to get the lattice from.
     *  @param node The given ASTPtProductNode.
     *  @exception IllegalActionException Thrown if the parent construct
     *   throws it.
     */
    public ASTPtProductNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtProductNode node)
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

        // Find the multiply and divide concept functions that
        // are needed for the PtProductNode monotonic function.
        ConceptFunction multiplyFunction = null;
        ConceptFunctionDefinitionAttribute multiplyDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.MULTIPLY_FUNCTION_NAME);
        if (multiplyDefinition != null) {
            multiplyFunction = multiplyDefinition.createConceptFunction();
        }

        ConceptFunction divideFunction = null;
        ConceptFunctionDefinitionAttribute divideDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.DIVIDE_FUNCTION_NAME);
        if (divideDefinition != null) {
            divideFunction = divideDefinition.createConceptFunction();
        }

        InequalityTerm[] childNodeTerms = _getChildNodeTerms();
        List<Ontology> argumentDomainOntologies = new ArrayList<Ontology>(
                childNodeTerms.length);
        for (InequalityTerm childNodeTerm : childNodeTerms) {
            argumentDomainOntologies.add(getSolver().getOntology());
        }

        List<Token> operatorTokenList = ((ptolemy.data.expr.ASTPtProductNode) _getNode())
                .getLexicalTokenList();

        ASTPtProductNodeFunction astProductFunction = new ASTPtProductNodeFunction(
                argumentDomainOntologies, getSolver().getOntology(),
                multiplyFunction, divideFunction, operatorTokenList);

        if (!astProductFunction.isMonotonic()) {
            throw new IllegalActionException(
                    _solver,
                    "The concept function for determining the "
                            + "PtProductNode concept is not monotonic. All concept functions used for a "
                            + "lattice ontology solver must be monotonic.");
        }

        setAtLeast(_getNode(), new ConceptFunctionInequalityTerm(
                astProductFunction, childNodeTerms));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private inner classes                    ////

    /** The Concept Function that outputs the concept for
     *  the product node based on the input child nodes.
     *  This is a general function that covers any combination
     *  of multiplication and division operators in the product node.
     *  Modulo operators are not supported.
     */
    private class ASTPtProductNodeFunction extends ConceptFunction {

        /** Initialize the ASTPtProductNodeFunction.
         * @param argumentDomainOntologies The array of domain ontologies
         *  for the function arguments.
         * @param outputRangeOntology The ontology range for the output.
         * @param multiplyFunction The multiplication concept function to be
         *  used when calculating the product node function.  If this is null,
         *  then the simple least upper bound is used between two multiplication
         *  operands.
         * @param divideFunction The division concept function to be
         *  used when calculating the product node function.  If this is null,
         *  then the simple least upper bound is used between two division
         *  operands.
         * @param operatorTokenList The list of operator tokens for the product node
         *  expression.
         * @exception IllegalActionException If the class cannot be initialized.
         */
        public ASTPtProductNodeFunction(
                List<Ontology> argumentDomainOntologies,
                Ontology outputRangeOntology, ConceptFunction multiplyFunction,
                ConceptFunction divideFunction, List<Token> operatorTokenList)
                throws IllegalActionException {
            super("defaultASTPtProductNodeFunction", true,
                    argumentDomainOntologies, outputRangeOntology);

            _multiplyFunction = multiplyFunction;
            _divideFunction = divideFunction;
            _operatorTokenList = operatorTokenList;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         protected inner methods           ////

        /** Return the function result.
         *  @param inputConceptValues The array of input arguments for the function.
         *  @return The concept value that is output from this function.
         *  @exception IllegalActionException If there is a problem evaluating the function
         */
        @Override
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {
            // Updated by Charles Shelton 12/15/09:
            // Created a general function that covers any combination of multiplication
            // and division operators.  Modulo operators are not supported.

            // Throw an exception if there is a modulo (%) operation in the product node expression.
            for (Object lexicalToken : _operatorTokenList) {
                if (((Token) lexicalToken).kind == PtParserConstants.MODULO) {
                    throw new IllegalActionException(
                            ASTPtProductNode.this.getSolver(),
                            "The lattice ontology solver analysis "
                                    + "supports only multiplication and division, not modulo.");
                }
            }

            // Loop through all the child node concepts in the product node
            // and get the correct result property by calling the multiply concept function
            // or divide concept function depending on the operator used.
            // If the multiply function or divide function is null, then just
            // get the least upper bound of the concepts.

            // Initialize the result to the first node in the product node
            Concept result = inputConceptValues.get(0);

            // Iterate through the operator tokens
            Iterator<Token> lexicalTokenIterator = _operatorTokenList
                    .iterator();

            for (int i = 1; i < inputConceptValues.size(); i++) {
                if (lexicalTokenIterator.hasNext()) {
                    Token lexicalToken = lexicalTokenIterator.next();
                    Concept nodeChildConcept = inputConceptValues.get(i);
                    List<Concept> conceptInputs = new ArrayList<Concept>(2);
                    conceptInputs.add(result);
                    conceptInputs.add(nodeChildConcept);

                    // If operator token is '*' call the MultiplyMonotonicFunction
                    if (lexicalToken.kind == PtParserConstants.MULTIPLY) {
                        if (_multiplyFunction != null) {
                            result = _multiplyFunction
                                    .evaluateFunction(conceptInputs);
                        } else {
                            // FIXME: Implement LUB and change this
                            result = _outputRangeOntology.getConceptGraph()
                                    .leastUpperBound(result, nodeChildConcept);
                        }

                        // If operator token is '/' call the DivideMonotonicFunction
                    } else {
                        if (_divideFunction != null) {
                            result = _divideFunction
                                    .evaluateFunction(conceptInputs);
                        } else {
                            // FIXME: Implement LUB and change this
                            result = _outputRangeOntology.getConceptGraph()
                                    .leastUpperBound(result, nodeChildConcept);
                        }
                    }

                } else {
                    throw new IllegalActionException(
                            ASTPtProductNode.this.getSolver(),
                            "Error in the product expression; "
                                    + "the number of operators don't match the number of operands.");
                }
            }

            return result;
        }

        ///////////////////////////////////////////////////////////////////
        ////                      private inner variables              ////

        /** The division concept function to be
         *  used when calculating the product node function.
         */
        private ConceptFunction _divideFunction;

        /** The multiplication concept function to be
         *  used when calculating the product node function.
         */
        private ConceptFunction _multiplyFunction;

        /** The list of operator tokens '*', '/', and '%' that
         *  are contained in the Ptolemy AST product node. Modulo
         *  operators '%' are not supported by this concept function.
         */
        private List<Token> _operatorTokenList;
    }

}
