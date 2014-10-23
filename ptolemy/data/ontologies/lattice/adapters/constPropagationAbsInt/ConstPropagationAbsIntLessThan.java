/* A concept function that returns the BooleanTrue or BooleanFalse Concept
 * result of a less than comparison between two FlatScalarTokenInfiniteConcepts
 * in the constPropagationAbsInt ontology.
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

/** A concept function that returns the BooleanTrue or BooleanFalse Concept
 *  result of a less than comparison between two FlatScalarTokenInfiniteConcepts
 *  in the constPropagationAbsInt ontology.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConstPropagationAbsIntLessThan extends ConceptFunction {

    /** Create a new ConstPropagationAbsIntLessThan concept function.
     *  @param ontology The domain and range unit system ontology for this
     *   concept function.
     *  @exception IllegalActionException Thrown if the concept function cannot be created.
     */
    public ConstPropagationAbsIntLessThan(Ontology ontology)
            throws IllegalActionException {
        super("ConstPropagationAbsIntLessThan", 2, ontology);

        _constPropagationAbsIntOntology = ontology;
        _positiveRepresentative = (FlatScalarTokenRepresentativeConcept) _constPropagationAbsIntOntology
                .getConceptByString("PositiveValue");
        _negativeRepresentative = (FlatScalarTokenRepresentativeConcept) _constPropagationAbsIntOntology
                .getConceptByString("NegativeValue");
        _zeroConcept = _constPropagationAbsIntOntology
                .getConceptByString("Zero");
        _booleanTrueConcept = _constPropagationAbsIntOntology
                .getConceptByString("BooleanTrue");
        _booleanFalseConcept = _constPropagationAbsIntOntology
                .getConceptByString("BooleanFalse");

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
     *  concept is a UnitConcept that is the result of multiplication or division
     *  of the two input UnitConcepts, or the top of the ontology lattice if there
     *  is no UnitConcept in the ontology that represents the product or quotient
     *  of the two input concepts.
     *  @param argValues The 2 UnitConcept input arguments.
     *  @return The output UnitConcept.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the output RecordConcept.
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
            return _getLessThanResultConcept(arg1, arg2);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the Concept that is the result of comparing
     *  the given input arguments and determining whether the first argument's
     *  token value is less than the second argument's token.
     *  @param concept1 The first argument of the concept function.
     *  @param concept2 The second argument of the concept function.
     *  @return The Boolean Concept that is the result of the less than
     *   comparison between the two input concepts.
     *  @exception IllegalActionException Thrown if the math operation cannot be
     *   performed.
     */
    private Concept _getLessThanResultConcept(Concept concept1, Concept concept2)
            throws IllegalActionException {

        _validateInputConcept(concept1);
        _validateInputConcept(concept2);

        ScalarToken token1 = null;
        ScalarToken token2 = null;
        if (concept1.equals(_zeroConcept) && concept2.equals(_zeroConcept)) {
            return _booleanFalseConcept;
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

        return _getLessThanResult(token1, token2);
    }

    /** Return the Boolean Concept result of the less than comparison for the
     *  given input tokens.
     *  @param token1 The scalar token from the first concept input argument.
     *  @param token2 The scalar token from the second concept input argument.
     *  @return The Boolean Concept result of the less than comparison.
     *  @exception IllegalActionException Thrown if there is a problem performing
     *   the less than operation.
     */
    private Concept _getLessThanResult(ScalarToken token1, ScalarToken token2)
            throws IllegalActionException {
        boolean comparisonResult = token1.isLessThan(token2).booleanValue();
        if (comparisonResult) {
            return _booleanTrueConcept;
        } else {
            return _booleanFalseConcept;
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

    /** The bottom of the lattice for the constant propagation ontology */
    private Concept _bottomOfTheLattice;

    /** The top of the lattice for the constant propagation ontology */
    private Concept _topOfTheLattice;

    /** The constant propagation ontology for this concept function. */
    private Ontology _constPropagationAbsIntOntology;

    /** The concept that represents the BooleanTrue concept in the
     *  constPropagationAbsInt ontology.
     */
    private Concept _booleanTrueConcept;

    /** The concept that represents the BooleanFalse concept in the
     *  constPropagationAbsInt ontology.
     */
    private Concept _booleanFalseConcept;

    /** The representative for the negative infinite scalar token concepts. */
    private FlatScalarTokenRepresentativeConcept _negativeRepresentative;

    /** The representative for the positive infinite scalar token concepts. */
    private FlatScalarTokenRepresentativeConcept _positiveRepresentative;

    /** The concept that represents the Zero concept in the
     *  constPropagationAbsInt ontology.
     */
    private Concept _zeroConcept;
}
