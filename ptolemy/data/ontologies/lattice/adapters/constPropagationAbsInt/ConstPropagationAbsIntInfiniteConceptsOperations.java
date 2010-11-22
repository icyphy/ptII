/* Class that provides helper methods for applying mathematical
 * operations to token-based infinite concepts for the constPropagationAbsInt
 * ontology.
 * 
 * Copyright (c) 2010 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
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
 */
package ptolemy.data.ontologies.lattice.adapters.constPropagationAbsInt;

import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ExpressionOperationsForInfiniteConcepts;
import ptolemy.data.ontologies.FlatScalarTokenInfiniteConcept;
import ptolemy.data.ontologies.FlatScalarTokenRepresentativeConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConstPropagationAbsIntInfiniteConceptsOperations

/** Class that provides helper methods for applying mathematical
 *  operations to token-based infinite concepts for the constPropagationAbsInt
 *  ontology.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class ConstPropagationAbsIntInfiniteConceptsOperations extends
        ExpressionOperationsForInfiniteConcepts {

    /** Create a new ConstPropagationInfiniteConceptsOperations object.
     * 
     *  @param constPropagationAbsIntOntology The constPropagationAbsInt ontology.
     *  @throws IllegalActionException If the constant value representative
     *   concept is not found in the ontology.
     */
    public ConstPropagationAbsIntInfiniteConceptsOperations(Ontology constPropagationAbsIntOntology)
        throws IllegalActionException {
        super(constPropagationAbsIntOntology);
        _positiveValueRepresentative = (FlatScalarTokenRepresentativeConcept)
            constPropagationAbsIntOntology.getConceptByString("PositiveValue");
        _negativeValueRepresentative = (FlatScalarTokenRepresentativeConcept)
            constPropagationAbsIntOntology.getConceptByString("NegativeValue");
        _zeroConcept = constPropagationAbsIntOntology.getConceptByString("Zero");
        _unknownConcept = constPropagationAbsIntOntology.getConceptByString("Unknown");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////
    
    /** Add two token infinite concepts and return the resulting concept.
     * 
     *  @param addend1 The first addend concept.
     *  @param addend2 The second addend concept.
     *  @return The concept that represents the sum of the two input concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public Concept add(Concept addend1, Concept addend2) throws IllegalActionException {
        _validateInputConcept(addend1);
        _validateInputConcept(addend2);
        
        if (addend1.equals(_zeroConcept)) {
            return addend2;
        } else if (addend2.equals(_zeroConcept)) {
            return addend1;
        } else {        
            ScalarToken sum = (ScalarToken) ((FlatScalarTokenInfiniteConcept) addend1).
                getTokenValue().add(((FlatScalarTokenInfiniteConcept) addend2).getTokenValue());
            return _createNewConstPropagationAbsIntConcept(sum);
        }
    }

    /** Divide two token infinite concepts and return the resulting concept.
     * 
     *  @param dividend The dividend concept.
     *  @param divisor The divisor concept.
     *  @return The concept that represents the quotient of the two input
     *   concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public Concept divide(Concept dividend, Concept divisor)
        throws IllegalActionException {
        _validateInputConcept(dividend);
        _validateInputConcept(divisor);
        
        if (dividend.equals(_zeroConcept)) {
            return _zeroConcept;
        } else if (divisor.equals(_zeroConcept)) {
            return _unknownConcept;
        } else {
            ScalarToken quotient = (ScalarToken) ((FlatScalarTokenInfiniteConcept) dividend).
                getTokenValue().divide(((FlatScalarTokenInfiniteConcept) divisor).getTokenValue());
            return _createNewConstPropagationAbsIntConcept(quotient);
        }
    }

    /** Return the boolean token that represents boolean true if the left concept
     *  value is less than the right concept values, and false otherwise.
     * 
     *  @param left The left concept.
     *  @param right The right concept.
     *  @return The boolean token that represents true if the left concept
     *   value is less than the right concept value, or false otherwise.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public BooleanToken isLessThan(Concept left, Concept right) throws IllegalActionException {
        _validateInputConcept(left);
        _validateInputConcept(right);
        
        if (left.equals(_zeroConcept) && right.equals(_zeroConcept)) {
            return BooleanToken.FALSE;
        } else if (left.equals(_zeroConcept)) {
            if (((FlatScalarTokenInfiniteConcept) right).getRepresentative().equals(_positiveValueRepresentative)) {
                return BooleanToken.TRUE;
            } else {
                return BooleanToken.FALSE;
            }
        } else if (right.equals(_zeroConcept)) {
            if (((FlatScalarTokenInfiniteConcept) left).getRepresentative().equals(_positiveValueRepresentative)) {
                return BooleanToken.FALSE;
            } else {
                return BooleanToken.TRUE;
            }
        } else {        
            BooleanToken booleanResult = ((ScalarToken) ((FlatScalarTokenInfiniteConcept) left).getTokenValue()).
                isLessThan((ScalarToken) ((FlatScalarTokenInfiniteConcept) right).getTokenValue());
            if (booleanResult.booleanValue()) {
                return BooleanToken.TRUE;
            } else {
                return BooleanToken.FALSE;
            }
        }
    }

    /** Multiply two token infinite concepts and return the resulting concept.
     * 
     *  @param factor1 The first factor.
     *  @param factor2 The second factor.
     *  @return The concept that represents the product of the two input
     *    concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public Concept multiply(Concept factor1, Concept factor2) throws IllegalActionException {
        _validateInputConcept(factor1);
        _validateInputConcept(factor2);
        
        if (factor1.equals(_zeroConcept) || factor2.equals(_zeroConcept)) {
            return _zeroConcept;
        } else {
            ScalarToken product = (ScalarToken) ((FlatScalarTokenInfiniteConcept) factor1).
                getTokenValue().multiply(((FlatScalarTokenInfiniteConcept) factor2).getTokenValue());
            return _createNewConstPropagationAbsIntConcept(product);
        }
    }

    /** Return the concept that represents the multiplicative reciprocal
     *  of the given token infinite concept.
     * 
     *  @param concept The input concept.
     *  @return The concept that represents the multiplicative reciprocal
     *   of the given token infinite concept.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concept.
     */
    public Concept reciprocal(Concept concept)
            throws IllegalActionException {
        _validateInputConcept(concept);
        
        if (concept.equals(_zeroConcept)) {
            return _unknownConcept;
        } else {        
            Token input = ((FlatScalarTokenInfiniteConcept) concept).getTokenValue();
            ScalarToken reciprocal = (ScalarToken) input.one().divide(input);
            return _createNewConstPropagationAbsIntConcept(reciprocal);
        }
    }

    /** Subtract two token infinite concepts and return the resulting concept.
     * 
     *  @param subtractor The first subtraction argument.
     *  @param subtractee The second subtraction argument.
     *  @return The concept that represents the difference of the two input
     *   concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public Concept subtract(Concept subtractor, Concept subtractee)
        throws IllegalActionException {
        _validateInputConcept(subtractor);
        _validateInputConcept(subtractee);
        
        ScalarToken difference = null;
        if (subtractee.equals(_zeroConcept)) {
            return subtractor;
        } else if (subtractor.equals(_zeroConcept)) {
            difference = (ScalarToken) ((FlatScalarTokenInfiniteConcept) subtractee).
                getTokenValue().zero().subtract(((FlatScalarTokenInfiniteConcept) subtractee).getTokenValue());
        } else {
            difference = (ScalarToken) ((FlatScalarTokenInfiniteConcept) subtractor).
                getTokenValue().subtract(((FlatScalarTokenInfiniteConcept) subtractee).getTokenValue());
        }
        return _createNewConstPropagationAbsIntConcept(difference);
    }   

    /** Return the concept that represents the zero concept for this
     *  token infinite concept.
     * 
     *  @param concept The input concept.
     *  @return The concept that represents the zero concept for the given
     *   token infinite concept.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concept.
     */
    public Concept zero(Concept concept)
            throws IllegalActionException {
        return _zeroConcept;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Create a new constPropagationAbsInt infinite concept based on the
     *  value of the scalar token.
     *  @param value The scalar value to be contained in the infinite concept.
     *  @return The infinite concept with the given value.
     *  @throws IllegalActionException Thrown if there is a problem creating the
     *   new infinite concept.
     */
    private Concept _createNewConstPropagationAbsIntConcept(ScalarToken value)
        throws IllegalActionException {
        if (value.isEqualTo(value.zero()).booleanValue()) {
            return _zeroConcept;
        } else if (value.isLessThan((ScalarToken) value.zero()).booleanValue()) {
            return FlatScalarTokenInfiniteConcept.createFlatScalarTokenInfiniteConcept(
                    _negativeValueRepresentative.getOntology(),
                    _negativeValueRepresentative, value);
        } else {
            return FlatScalarTokenInfiniteConcept.createFlatScalarTokenInfiniteConcept(
                    _positiveValueRepresentative.getOntology(),
                    _positiveValueRepresentative, value);
        } 
    }
    
    /** Ensure that the input concept has the correct FlatTokenRepresentativeConcept
     *  and that it has a non-null token value.
     *  @param inputConcept The input concept to be tested.
     *  @throws IllegalActionException Thrown if the input concept fails the
     *   validation checks.
     */
    private void _validateInputConcept(Concept inputConcept)
        throws IllegalActionException {
        if (!(inputConcept.equals(_zeroConcept) ||
                inputConcept instanceof FlatScalarTokenInfiniteConcept)) {
            throw new IllegalActionException(inputConcept, "Invalid argument: " +
                    "the input concepts must be instances of " +
                    "FlatScalarTokenInfiniteConcept or the Concept Zero.");
        } else if (!(inputConcept.equals(_zeroConcept) || (inputConcept instanceof FlatScalarTokenInfiniteConcept &&
                (_positiveValueRepresentative.equals(((FlatScalarTokenInfiniteConcept) inputConcept).getRepresentative())
                || _negativeValueRepresentative.equals(((FlatScalarTokenInfiniteConcept) inputConcept).getRepresentative()))
                && ((FlatScalarTokenInfiniteConcept) inputConcept).getTokenValue() != null))) {
            throw new IllegalActionException(inputConcept, "Invalid argument: " +
                    "the FlatScalarTokenInfiniteConcept " + inputConcept.getName() +
                    "has an incorrect representative concept or " +
                    "a null token value. It's representative is: " +
                    ((FlatScalarTokenInfiniteConcept) inputConcept).getRepresentative() + " and it should be " +
                    _positiveValueRepresentative + " or " + _negativeValueRepresentative +
                    ". It's token value is " + ((FlatScalarTokenInfiniteConcept) inputConcept).getTokenValue());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The Negative Value representative concept in the constPropagationAbsInt ontology. */
    private FlatScalarTokenRepresentativeConcept _negativeValueRepresentative;
    
    /** The Positive Value representative concept in the constPropagationAbsInt ontology. */
    private FlatScalarTokenRepresentativeConcept _positiveValueRepresentative;   
    
    /** The Unknown concept in the constPropagationAbsInt ontology. */
    private Concept _unknownConcept;
    
    /** The Zero concept in the constPropagationAbsInt ontology. */
    private Concept _zeroConcept;
}
