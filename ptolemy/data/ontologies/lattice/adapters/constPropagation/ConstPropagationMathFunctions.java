/* A concept function that returns the Concept result of a math operation
 * between two FlatTokenInfiniteConcepts in the constPropagation ontology.
 *
 * Copyright (c) 1998-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */
package ptolemy.data.ontologies.lattice.adapters.constPropagation;

import java.util.List;

import ptolemy.data.Token;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.FlatTokenInfiniteConcept;
import ptolemy.data.ontologies.FlatTokenRepresentativeConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConstPropagationMathFunctions

/** A concept function that returns the Concept result of a math operation
 *  between two FlatTokenInfiniteConcepts in the constPropagation ontology.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConstPropagationMathFunctions extends ConceptFunction {

    /** Create a new ConstPropagationMathFunctions concept function.
     *  @param ontology The domain and range unit system ontology for this
     *   concept function.
     *  @param mathOperation Indicates whether this concept function will perform
     *   addition, subtraction, multiplication or division for the
     *   FlatTokenInfiniteConcepts.
     *  @exception IllegalActionException Thrown if the concept function cannot be created.
     */
    public ConstPropagationMathFunctions(Ontology ontology, String mathOperation)
            throws IllegalActionException {
        super("ConstPropagationMathFunction_" + mathOperation, 2, ontology);

        _mathOperation = mathOperation;
        _constPropagationOntology = ontology;

        ConceptGraph ontologyGraph = _constPropagationOntology
                .getConceptGraph();
        if (ontologyGraph == null) {
            throw new IllegalActionException("The Ontology "
                    + _constPropagationOntology + " has a null concept graph.");
        } else {
            _topOfTheLattice = ontologyGraph.top();
            _bottomOfTheLattice = ontologyGraph.bottom();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the function output from the given input arguments. The output
     *  concept is a FlatTokenInfiniteConcept that is the result of the
     *  addition, subtraction, multiplication or division of the two input
     *  FlatTokenInfiniteConcepts.
     *  @param argValues The 2 FlatTokenInfiniteConcept input arguments.
     *  @return The output FlatTokenInfiniteConcept.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the output FlatTokenInfiniteConcept.
     */
    @Override
    protected Concept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {

        Concept arg1 = argValues.get(0);
        Concept arg2 = argValues.get(1);

        // If either concept is the bottom of the lattice, return bottom.
        if (arg1.equals(_bottomOfTheLattice)
                || arg2.equals(_bottomOfTheLattice)) {
            return _bottomOfTheLattice;

            // If either concept is the top of the lattice, return top.
        } else if (arg1.equals(_topOfTheLattice)
                || arg2.equals(_topOfTheLattice)) {
            return _topOfTheLattice;

        } else if (arg1 instanceof FlatTokenInfiniteConcept
                && arg2 instanceof FlatTokenInfiniteConcept) {
            if (_isDivisionAndSecondArgumentIsZero((FlatTokenInfiniteConcept) arg2)) {
                return _bottomOfTheLattice;
            } else {
                return _getMathOperationResultConcept(
                        (FlatTokenInfiniteConcept) arg1,
                        (FlatTokenInfiniteConcept) arg2);
            }
        } else {
            throw new IllegalActionException("Concept inputs must be"
                    + " FlatTokenInfiniteConcepts. Input" + " Concepts were: "
                    + arg1 + " and " + arg2 + ".");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the FlatTokenInfiniteConcept that is the result of applying
     *  the math operation to the given input arguments.
     *  @param concept1 The first argument of the concept function.
     *  @param concept2 The second argument of the concept function.
     *  @return The FlatTokenInfiniteConcept that is the result of the math
     *   operation for this concept function.
     *  @exception IllegalActionException Thrown if the math operation cannot be
     *   performed.
     */
    private FlatTokenInfiniteConcept _getMathOperationResultConcept(
            FlatTokenInfiniteConcept concept1, FlatTokenInfiniteConcept concept2)
            throws IllegalActionException {

        if (concept1.getRepresentative().equals(concept2.getRepresentative())) {
            Token token1 = concept1.getTokenValue();
            Token token2 = concept2.getTokenValue();
            Token resultToken = _getMathOperationResultToken(token1, token2);
            FlatTokenRepresentativeConcept representative = concept1
                    .getRepresentative();
            return representative
                    .getFlatTokenInfiniteConceptByToken(resultToken);
        } else {
            throw new IllegalActionException("Cannot perform a math "
                    + "operation on two FlatTokenInfiniteConcepts with "
                    + "different representatives.");
        }
    }

    /** Return the token result of the math operation for the given input
     *  tokens.
     *  @param token1 The token from the first concept input argument.
     *  @param token2 The token from the second concept input argument.
     *  @return The token result of the math operation.
     *  @exception IllegalActionException Thrown if the math operation is
     *   unrecognized.
     */
    private Token _getMathOperationResultToken(Token token1, Token token2)
            throws IllegalActionException {
        if (_mathOperation.equals("+")) {
            return token1.add(token2);
        } else if (_mathOperation.equals("-")) {
            return token1.subtract(token2);
        } else if (_mathOperation.equals("*")) {
            return token1.multiply(token2);
        } else if (_mathOperation.equals("/")) {
            return token1.divide(token2);
        } else {
            throw new IllegalActionException("Unrecognized math operation: "
                    + _mathOperation);
        }
    }

    /** Return true if the concept function operation is division and the
     *  second concept argument is zero.
     *  @param concept2 The second argument in the concept function.
     *  @return True if the concept function operation is division and the
     *   second concept argument is zero, or false otherwise.
     *  @exception IllegalActionException Thrown if there is a problem checking
     *   if the token in the concept is zero.
     */
    private boolean _isDivisionAndSecondArgumentIsZero(
            FlatTokenInfiniteConcept concept2) throws IllegalActionException {
        Token tokenValue = concept2.getTokenValue();
        if (_mathOperation.equals("/")
                && tokenValue.isEqualTo(tokenValue.zero()).booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The string that contains the math operation for this concept function. */
    private String _mathOperation;

    /** The bottom of the lattice for the constant propagation ontology */
    private Concept _bottomOfTheLattice;

    /** The top of the lattice for the constant propagation ontology */
    private Concept _topOfTheLattice;

    /** The constant propagation ontology for this concept function. */
    private Ontology _constPropagationOntology;
}
