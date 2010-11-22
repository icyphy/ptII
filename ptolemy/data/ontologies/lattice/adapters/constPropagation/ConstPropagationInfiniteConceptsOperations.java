/* Class that provides helper methods for applying mathematical
 * operations to token-based infinite concepts for the constPropagation
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
package ptolemy.data.ontologies.lattice.adapters.constPropagation;

import ptolemy.data.BooleanToken;
import ptolemy.data.PartiallyOrderedToken;
import ptolemy.data.Token;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ExpressionOperationsForInfiniteConcepts;
import ptolemy.data.ontologies.FlatTokenInfiniteConcept;
import ptolemy.data.ontologies.FlatTokenRepresentativeConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConstPropagationInfiniteConceptsOperations

/** Class that provides helper methods for applying mathematical
 *  operations to token-based infinite concepts for the constPropagation
 *  ontology.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class ConstPropagationInfiniteConceptsOperations extends
        ExpressionOperationsForInfiniteConcepts {

    /** Create a new ConstPropagationInfiniteConceptsOperations object.
     * 
     *  @param constPropagationOntology The constPropagation ontology.
     *  @throws IllegalActionException If the constant value representative
     *   concept is not found in the ontology.
     */
    public ConstPropagationInfiniteConceptsOperations(Ontology constPropagationOntology)
        throws IllegalActionException {
        super(constPropagationOntology);
        _constantValueRepresentative = (FlatTokenRepresentativeConcept)
            constPropagationOntology.getConceptByString("ConstantValue");
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
    public Concept add(Concept addend1, Concept addend2)
        throws IllegalActionException {
        _validateInputConcept(addend1);
        _validateInputConcept(addend2);
        
        Token sum = ((FlatTokenInfiniteConcept) addend1).getTokenValue().
            add(((FlatTokenInfiniteConcept) addend2).getTokenValue());
        return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(
                _constantValueRepresentative.getOntology(),
                _constantValueRepresentative, sum);
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
        
        Token quotient = ((FlatTokenInfiniteConcept) dividend).getTokenValue().
            divide(((FlatTokenInfiniteConcept) divisor).getTokenValue());
        return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(
                _constantValueRepresentative.getOntology(),
                _constantValueRepresentative, quotient);
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
    public BooleanToken isLessThan(Concept left, Concept right)
        throws IllegalActionException {
        _validateInputConcept(left);
        _validateInputConcept(right);
        
        return ((PartiallyOrderedToken) ((FlatTokenInfiniteConcept) left).getTokenValue()).
            isLessThan((PartiallyOrderedToken) ((FlatTokenInfiniteConcept) right).getTokenValue());
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
    public Concept multiply(Concept factor1, Concept factor2)
        throws IllegalActionException {
        _validateInputConcept(factor1);
        _validateInputConcept(factor2);
        
        Token product = ((FlatTokenInfiniteConcept) factor1).getTokenValue().
            multiply(((FlatTokenInfiniteConcept) factor2).getTokenValue());
        return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(
                _constantValueRepresentative.getOntology(),
                _constantValueRepresentative, product);
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
    public Concept reciprocal(Concept concept) throws IllegalActionException {
        _validateInputConcept(concept);
        
        Token input = ((FlatTokenInfiniteConcept) concept).getTokenValue();
        Token reciprocal = input.one().divide(input);
        return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(
                _constantValueRepresentative.getOntology(),
                _constantValueRepresentative, reciprocal);
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
        
        Token difference = ((FlatTokenInfiniteConcept) subtractor).
            getTokenValue().subtract(((FlatTokenInfiniteConcept) subtractee).getTokenValue());
        return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(
                _constantValueRepresentative.getOntology(),
                _constantValueRepresentative, difference);
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
        _validateInputConcept(concept);
        
        Token input = ((FlatTokenInfiniteConcept) concept).getTokenValue();
        Token zero = input.zero();
        return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(
                _constantValueRepresentative.getOntology(),
                _constantValueRepresentative, zero);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Ensure that the input concept has the correct FlatTokenRepresentativeConcept
     *  and that it has a non-null token value.
     *  @param inputConcept The input concept to be tested.
     *  @throws IllegalActionException Thrown if the input concept fails the
     *   validation checks.
     */
    private void _validateInputConcept(Concept inputConcept)
        throws IllegalActionException {
        if (!(inputConcept instanceof FlatTokenInfiniteConcept)) {
            throw new IllegalActionException(inputConcept, "Invalid argumen: " +
                    "the input concepts must be instances of FlatTokenInfiniteConcept.");
        } else if (!(_constantValueRepresentative.equals(((FlatTokenInfiniteConcept) inputConcept).getRepresentative())
                && ((FlatTokenInfiniteConcept) inputConcept).getTokenValue() != null)) {
            throw new IllegalActionException(inputConcept, "Invalid argument: " +
                    "the FlatTokenInfiniteConcept " + inputConcept.getName() +
                    "has an incorrect representative concept or " +
                    "a null token value. It's representative is: " +
                    ((FlatTokenInfiniteConcept) inputConcept).getRepresentative() + " and it should be " +
                    _constantValueRepresentative + ". It's token value is " +
                    ((FlatTokenInfiniteConcept) inputConcept).getTokenValue());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The Constant Value representative concept in the constPropagation ontology. */
    private FlatTokenRepresentativeConcept _constantValueRepresentative;
}
