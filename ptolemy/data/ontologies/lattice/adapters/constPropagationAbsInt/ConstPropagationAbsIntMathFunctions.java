/* A concept function that returns the Concept result of a math operation
 * between two FlatScalarTokenInfiniteConcepts in the constPropagationAbsInt
 * ontology.
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
package ptolemy.data.ontologies.lattice.adapters.constPropagationAbsInt;

import java.util.List;

import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.FlatScalarTokenInfiniteConcept;
import ptolemy.data.ontologies.FlatScalarTokenRepresentativeConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConstPropagationAbsIntMathFunctions

/** A concept function that returns the Concept result of a math operation
 *  between two FlatScalarTokenInfiniteConcepts in the constPropagationAbsInt
 *  ontology.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConstPropagationAbsIntMathFunctions extends ConceptFunction {

    /** Create a new ConstPropagationAbsIntMathFunctions concept function.
     *  @param ontology The domain and range unit system ontology for this
     *   concept function.
     *  @param mathOperation Indicates whether this concept function will perform
     *   addition, subtraction, multiplication or division for the
     *   FlatScalarTokenInfiniteConcepts.
     *  @exception IllegalActionException Thrown if the concept function cannot be created.
     */
    public ConstPropagationAbsIntMathFunctions(Ontology ontology,
            String mathOperation) throws IllegalActionException {
        super("ConstPropagationAbsIntMathFunction_" + mathOperation, 2,
                ontology);

        _mathOperation = mathOperation;
        _constPropagationAbsIntOntology = ontology;
        _positiveRepresentative = (FlatScalarTokenRepresentativeConcept) _constPropagationAbsIntOntology
                .getConceptByString("PositiveValue");
        _negativeRepresentative = (FlatScalarTokenRepresentativeConcept) _constPropagationAbsIntOntology
                .getConceptByString("NegativeValue");
        _zeroConcept = _constPropagationAbsIntOntology
                .getConceptByString("Zero");

        ConceptGraph ontologyGraph = _constPropagationAbsIntOntology
                .getConceptGraph();
        if (ontologyGraph == null) {
            throw new IllegalActionException("The Ontology "
                    + _constPropagationAbsIntOntology
                    + " has a null concept graph.");
        } else {
            _topOfTheLattice = ontologyGraph.top();
            _bottomOfTheLattice = ontologyGraph.bottom();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the function output from the given input arguments. The output
     *  concept is a FlatScalarTokenInfiniteConcept that is the result of the
     *  addition, subtraction, multiplication or division of the two input
     *  FlatScalarTokenInfiniteConcepts. If the operation results in a value
     *  of zero, return the finite Zero concept in the constPropagationAbsInt
     *  ontology.
     *  @param argValues The 2 FlatScalarTokenInfiniteConcept input arguments.
     *  @return The output FlatScalarTokenInfiniteConcept.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the output FlatScalarTokenInfiniteConcept.
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

        } else {
            if (_isDivisionAndSecondArgumentIsZero(arg2)) {
                return _bottomOfTheLattice;
            } else {
                return _getMathOperationResultConcept(arg1, arg2);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a new constPropagationAbsInt infinite concept based on the
     *  value of the scalar token.
     *  @param value The scalar value to be contained in the infinite concept.
     *  @return The infinite concept with the given value.
     *  @exception IllegalActionException Thrown if there is a problem creating the
     *   new infinite concept.
     */
    private Concept _createNewConstPropagationAbsIntConcept(ScalarToken value)
            throws IllegalActionException {
        if (value.isEqualTo(value.zero()).booleanValue()) {
            return _zeroConcept;
        } else if (value.isLessThan((ScalarToken) value.zero()).booleanValue()) {
            return _negativeRepresentative
                    .getFlatTokenInfiniteConceptByToken(value);
        } else {
            return _positiveRepresentative
                    .getFlatTokenInfiniteConceptByToken(value);
        }
    }

    /** Return the Concept that is the result of applying
     *  the math operation to the given input arguments.
     *  @param concept1 The first argument of the concept function.
     *  @param concept2 The second argument of the concept function.
     *  @return The Concept that is the result of the math
     *   operation for this concept function.
     *  @exception IllegalActionException Thrown if the math operation cannot be
     *   performed.
     */
    private Concept _getMathOperationResultConcept(Concept concept1,
            Concept concept2) throws IllegalActionException {

        _validateInputConcept(concept1);
        _validateInputConcept(concept2);

        ScalarToken token1 = null;
        ScalarToken token2 = null;
        if (concept1.equals(_zeroConcept) && concept2.equals(_zeroConcept)) {
            token1 = DoubleToken.ZERO;
            token2 = DoubleToken.ZERO;
        } else if (concept1.equals(_zeroConcept)) {
            token2 = ((FlatScalarTokenInfiniteConcept) concept2)
                    .getTokenValue();
            token1 = (ScalarToken) token2.zero();
        } else if (concept2.equals(_zeroConcept)) {
            token1 = ((FlatScalarTokenInfiniteConcept) concept1)
                    .getTokenValue();
            token2 = (ScalarToken) token1.zero();
        } else {
            token1 = ((FlatScalarTokenInfiniteConcept) concept1)
                    .getTokenValue();
            token2 = ((FlatScalarTokenInfiniteConcept) concept2)
                    .getTokenValue();
        }

        ScalarToken resultToken = _getMathOperationResultToken(token1, token2);
        return _createNewConstPropagationAbsIntConcept(resultToken);
    }

    /** Return the scalar token result of the math operation for the given input
     *  tokens.
     *  @param token1 The scalar token from the first concept input argument.
     *  @param token2 The scalar token from the second concept input argument.
     *  @return The scalar token result of the math operation.
     *  @exception IllegalActionException Thrown if the math operation is
     *   unrecognized.
     */
    private ScalarToken _getMathOperationResultToken(ScalarToken token1,
            ScalarToken token2) throws IllegalActionException {
        if (_mathOperation.equals("+")) {
            return (ScalarToken) token1.add(token2);
        } else if (_mathOperation.equals("-")) {
            return (ScalarToken) token1.subtract(token2);
        } else if (_mathOperation.equals("*")) {
            return (ScalarToken) token1.multiply(token2);
        } else if (_mathOperation.equals("/")) {
            return (ScalarToken) token1.divide(token2);
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
    private boolean _isDivisionAndSecondArgumentIsZero(Concept concept2)
            throws IllegalActionException {
        if (_mathOperation.equals("/") && concept2.equals(_zeroConcept)) {
            return true;
        } else {
            return false;
        }
    }

    /** Ensure that the input concept has the correct FlatTokenRepresentativeConcept
     *  and that it has a non-null token value.
     *  @param inputConcept The input concept to be tested.
     *  @exception IllegalActionException Thrown if the input concept fails the
     *   validation checks.
     */
    private void _validateInputConcept(Concept inputConcept)
            throws IllegalActionException {
        if (!(inputConcept.equals(_zeroConcept) || inputConcept instanceof FlatScalarTokenInfiniteConcept)) {
            throw new IllegalActionException(inputConcept, "Invalid argument: "
                    + "the input concepts must be instances of "
                    + "FlatScalarTokenInfiniteConcept or the Concept Zero.");
        } else if (!(inputConcept.equals(_zeroConcept) || inputConcept instanceof FlatScalarTokenInfiniteConcept
                && (_positiveRepresentative
                        .equals(((FlatScalarTokenInfiniteConcept) inputConcept)
                                .getRepresentative()) || _negativeRepresentative
                        .equals(((FlatScalarTokenInfiniteConcept) inputConcept)
                                .getRepresentative()))
                && ((FlatScalarTokenInfiniteConcept) inputConcept)
                        .getTokenValue() != null)) {
            throw new IllegalActionException(inputConcept,
                    "Invalid argument: "
                            + "the FlatScalarTokenInfiniteConcept "
                            + inputConcept.getName()
                            + "has an incorrect representative concept or "
                            + "a null token value. It's representative is: "
                            + ((FlatScalarTokenInfiniteConcept) inputConcept)
                                    .getRepresentative()
                            + " and it should be "
                            + _positiveRepresentative
                            + " or "
                            + _negativeRepresentative
                            + ". It's token value is "
                            + ((FlatScalarTokenInfiniteConcept) inputConcept)
                                    .getTokenValue());
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
    private Ontology _constPropagationAbsIntOntology;

    /** The representative for the negative infinite scalar token concepts. */
    private FlatScalarTokenRepresentativeConcept _negativeRepresentative;

    /** The representative for the positive infinite scalar token concepts. */
    private FlatScalarTokenRepresentativeConcept _positiveRepresentative;

    /** The concept that represents the Zero concept in the
     *  constPropagationAbsInt ontology.
     */
    private Concept _zeroConcept;
}
