/* A token that contains an ontology concept.

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
package ptolemy.data.ontologies;

import ptolemy.data.BooleanToken;
import ptolemy.data.PartiallyOrderedToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 A token that contains an ontology concept.

 Concepts are partially ordered immutable values that belong to an
 ontology. Most of the functionality of this token class is is simply
 inherited from Concept.
 {@link ptolemy.data.ontologies.Concept}

 @author Ben Lickly
 @version $Id$
 @since Ptolemy II 9.0
 @Pt.ProposedRating Red (blickly)
 @Pt.AcceptedRating Red (blickly)
 */
public class ConceptToken extends Token implements PartiallyOrderedToken {

    /** Create a ConceptToken from a given Concept.
     *  @param c The given Concept
     */
    public ConceptToken(Concept c) {
        _concept = c;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the addition operation on two ConceptTokens.  This operation
     *  is only valid for FlatTokenInfiniteConcepts that contain ScalarToken
     *  values.
     *  @param rightArgument The second addend ConceptToken.
     *  @return The ConceptToken that represents the result of the addition
     *   of the two ConceptTokens.
     *  @throws IllegalActionException Thrown if the ConceptTokens are not
     *   FlatTokenInfiniteConcepts which can have their tokens added to create
     *   a new FlatTokenInfiniteConcept with the sum result token.
     */
    public ConceptToken add(Token rightArgument) throws IllegalActionException {
        if (!(rightArgument instanceof ConceptToken)) {
            throw new IllegalActionException("Both arguments must be instances " +
            		"of ConceptToken.");
        }        
        Concept addend1 = _concept;
        Concept addend2 = ((ConceptToken) rightArgument)._concept;
        ExpressionOperationsForInfiniteConcepts expressions = _concept.
            getOntology().getExpressionOperations();
        Concept result = expressions.add(addend1, addend2);        
        return new ConceptToken(result);
    }

    /** Return the concept encapsulated by this token.
     *  @return The concept value.
     */
    public Concept conceptValue() {
        return _concept;
    }
    
    /** Perform the division operation on two ConceptTokens.  This operation
     *  is only valid for FlatTokenInfiniteConcepts that contain ScalarToken
     *  values.
     *  @param rightArgument The divisor ConceptToken.
     *  @return The ConceptToken that represents the result of the division
     *   between the two ConceptTokens.
     *  @throws IllegalActionException Thrown if the ConceptTokens are not
     *   FlatTokenInfiniteConcepts which can have their tokens divided to create
     *   a new FlatTokenInfiniteConcept with the quotient result token.
     */
    public ConceptToken divide(Token rightArgument) throws IllegalActionException {
        if (!(rightArgument instanceof ConceptToken)) {
            throw new IllegalActionException("Both arguments must be instances " +
                        "of ConceptToken.");
        }        
        Concept dividend = _concept;
        Concept divisor = ((ConceptToken) rightArgument)._concept;
        ExpressionOperationsForInfiniteConcepts expressions = _concept.
            getOntology().getExpressionOperations();
        Concept result = expressions.divide(dividend, divisor);        
        return new ConceptToken(result);
    }

    /** Compare this ConceptToken to the given argument, and return true if
     *  they refer to the same concept in the same lattice.  If either is null,
     *  return false.
     *
     *  @param rightArgument The argument.
     *  @return true if the values are the same Concept, or false otherwise.
     */
    public BooleanToken isEqualTo(Token rightArgument) {
        if (this != null && rightArgument != null 
                && rightArgument instanceof ConceptToken
                && ((ConceptToken) rightArgument)._concept != null 
                && _concept != null 
                && ((ConceptToken) rightArgument)._concept.equals(_concept)) {
            return BooleanToken.TRUE;
        } else {
            return BooleanToken.FALSE;
        }
    }

    /** Check whether the value of this token is strictly less than that of the
     *  argument token.  Note that ontologies are only partial orders, so
     *  !(a < b) does not imply (a >= b).
     *
     *  @param rightArgument The token on greater than side of the inequality.
     *  @return BooleanToken.TRUE, if this token is less than the
     *    argument token. BooleanToken.FALSE, otherwise. 
     *  @throws IllegalActionException If the argument token and this
     *    token are of incomparable types, or are concepts from
     *    different ontologies.
     */
    public BooleanToken isLessThan(PartiallyOrderedToken rightArgument)
            throws IllegalActionException {
        if (!(rightArgument instanceof ConceptToken)) {
            //// FIXME: Cannot use the following code while PartiallyOrderedToken is an interface.
            //throw new IllegalActionException(notSupportedIncomparableMessage(
            //        "isLessThan", this, rightArgument));
            // so we use this instead:
            throw new IllegalActionException(
            "Cannot compare Concept with non-Concept.");
        }
        Concept rightConcept = ((ConceptToken) rightArgument).conceptValue();
        Concept leftConcept = this.conceptValue();
        
        // FIXME: This is a hack for tokenized infinite concepts. In certain
        // situations you want to know if the token value of one infinite concept
        // is less than the token value of the other.  This is different than
        // the conventional "lower in the lattice" less than operation for 
        // ontology concepts, and should only be used when both concepts are
        // instances of FlatTokenInfiniteConcept, and their token values can be compared.
        if (leftConcept instanceof FlatTokenInfiniteConcept && rightConcept instanceof FlatTokenInfiniteConcept) {
            ExpressionOperationsForInfiniteConcepts expressions = _concept.
                getOntology().getExpressionOperations();
            return expressions.isLessThan(leftConcept, rightConcept);            
        } else {
            boolean lessThanOrEqual = rightConcept != null && rightConcept.isAboveOrEqualTo(leftConcept);
            boolean equal = leftConcept != null && rightConcept != null && leftConcept.equals(rightConcept);
            return new BooleanToken(lessThanOrEqual && !equal);
        }
    }

    /** Perform the multiplication operation on two ConceptTokens.  This operation
     *  is only valid for FlatTokenInfiniteConcepts that contain ScalarToken
     *  values.
     *  @param rightArgument The divisor ConceptToken.
     *  @return The ConceptToken that represents the result of the multiplication
     *   between the two ConceptTokens.
     *  @throws IllegalActionException Thrown if the ConceptTokens are not
     *   FlatTokenInfiniteConcepts which can have their tokens multiplied to create
     *   a new FlatTokenInfiniteConcept with the product result token.
     */
    public ConceptToken multiply(Token rightArgument) throws IllegalActionException {
        if (!(rightArgument instanceof ConceptToken)) {
            throw new IllegalActionException("Both arguments must be instances " +
                        "of ConceptToken.");
        }        
        Concept factor1 = _concept;
        Concept factor2 = ((ConceptToken) rightArgument)._concept;
        ExpressionOperationsForInfiniteConcepts expressions = _concept.
            getOntology().getExpressionOperations();
        Concept result = expressions.multiply(factor1, factor2);        
        return new ConceptToken(result);
    }
    
    /** Return the ConceptToken that has a concept that represents the
     *  reciprocal for the flat token infinite concept in this concept token.
     *  @return The ConceptToken that has a concept that represents the
     *   reciprocal of the concept in this concept token.
     *  @throws IllegalActionException If the ConceptToken's concept value is
     *   not an instance of {@link FlatTokenInfiniteConcept}.
     */
    public ConceptToken reciprocal() throws IllegalActionException {
        if (_concept instanceof FlatTokenInfiniteConcept) {
            ExpressionOperationsForInfiniteConcepts expressions = _concept.
                getOntology().getExpressionOperations();
            Concept result = expressions.reciprocal(_concept);
            return new ConceptToken(result);
        } else {
            throw new IllegalActionException("Concept value must be an " +
                        "instance of FlatTokenInfiniteConcept.");
        }
    }
    
    /** Perform the subtraction operation on two ConceptTokens.  This operation
     *  is only valid for FlatTokenInfiniteConcepts that contain ScalarToken
     *  values.
     *  @param rightArgument The subtractee ConceptToken.
     *  @return The ConceptToken that represents the result of the subtraction
     *   between the two ConceptTokens.
     *  @throws IllegalActionException Thrown if the ConceptTokens are not
     *   FlatTokenInfiniteConcepts which can have their tokens divided to create
     *   a new FlatTokenInfiniteConcept with the difference result token.
     */
    public ConceptToken subtract(Token rightArgument) throws IllegalActionException {
        if (!(rightArgument instanceof ConceptToken)) {
            throw new IllegalActionException("Both arguments must be instances " +
                        "of ConceptToken.");
        }        
        Concept subtractor = _concept;
        Concept subtractee = ((ConceptToken) rightArgument)._concept;
        ExpressionOperationsForInfiniteConcepts expressions = _concept.
            getOntology().getExpressionOperations();
        Concept result = expressions.subtract(subtractor, subtractee);        
        return new ConceptToken(result);
    }

    /** Return the value of this concept token as a string.
     *  @return The name of the concept contained by this token as a string value.
     */
    public String toString() {
        if (_concept != null) {
            return _concept.toString();
        } else {
            return super.toString();
        }
    }
    
    /** Return the ConceptToken that has a concept that represents zero for
     *  the flat token infinite concepts for that ontology.
     *  @return The ConceptToken that has a concept that represents zero.
     *  @throws IllegalActionException If the ConceptToken's concept value is
     *   not an instance of {@link FlatTokenInfiniteConcept}.
     */
    public ConceptToken zero() throws IllegalActionException {
        if (_concept instanceof FlatTokenInfiniteConcept) {
            ExpressionOperationsForInfiniteConcepts expressions = _concept.
                getOntology().getExpressionOperations();
            Concept result = expressions.zero(_concept);
            return new ConceptToken(result);
        } else {
            throw new IllegalActionException("Concept value must be an " +
                        "instance of FlatTokenInfiniteConcept.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** The concept encapsulated by this token. */
    private Concept _concept;
}
