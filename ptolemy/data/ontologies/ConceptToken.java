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
            return (ConceptToken) super.divide(rightArgument);
        }
        
        Concept addend1 = _concept;
        Concept addend2 = ((ConceptToken) rightArgument)._concept;
        
        if (addend1 instanceof FlatTokenInfiniteConcept && addend2 instanceof FlatTokenInfiniteConcept) {
            if (addend1.getOntology().equals(addend2.getOntology())) {
                FlatTokenInfiniteConcept result = FlatTokenInfiniteConcept.
                    createFlatTokenInfiniteConcept(addend1.getOntology(),
                            ((FlatTokenInfiniteConcept) addend1).getRepresentative(),
                            ((FlatTokenInfiniteConcept) addend1).getTokenValue().
                                add(((FlatTokenInfiniteConcept) addend2).getTokenValue()));
                return new ConceptToken(result);
            }
        }
        
        return (ConceptToken) super.add(rightArgument);
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
            return (ConceptToken) super.divide(rightArgument);
        }
        
        Concept dividend = _concept;
        Concept divisor = ((ConceptToken) rightArgument)._concept;
        
        if (dividend instanceof FlatTokenInfiniteConcept && divisor instanceof FlatTokenInfiniteConcept) {
            if (dividend.getOntology().equals(divisor.getOntology())) {
                FlatTokenInfiniteConcept result = FlatTokenInfiniteConcept.
                    createFlatTokenInfiniteConcept(dividend.getOntology(),
                            ((FlatTokenInfiniteConcept) dividend).getRepresentative(),
                            ((FlatTokenInfiniteConcept) dividend).getTokenValue().
                                divide(((FlatTokenInfiniteConcept) divisor).getTokenValue()));
                return new ConceptToken(result);
            }
        }
        
        return (ConceptToken) super.divide(rightArgument);
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
    
        boolean lessThanOrEqual = rightConcept != null && rightConcept.isAboveOrEqualTo(leftConcept);
        boolean equal = leftConcept != null && rightConcept != null && leftConcept.equals(rightConcept);
        return new BooleanToken(lessThanOrEqual && !equal);
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
            return (ConceptToken) super.divide(rightArgument);
        }
        
        Concept factor1 = _concept;
        Concept factor2 = ((ConceptToken) rightArgument)._concept;
        
        if (factor1 instanceof FlatTokenInfiniteConcept && factor2 instanceof FlatTokenInfiniteConcept) {
            if (factor1.getOntology().equals(factor2.getOntology())) {
                FlatTokenInfiniteConcept result = FlatTokenInfiniteConcept.
                    createFlatTokenInfiniteConcept(factor1.getOntology(),
                            ((FlatTokenInfiniteConcept) factor1).getRepresentative(),
                            ((FlatTokenInfiniteConcept) factor1).getTokenValue().
                                multiply(((FlatTokenInfiniteConcept) factor2).getTokenValue()));
                return new ConceptToken(result);
            }
        }
        
        return (ConceptToken) super.multiply(rightArgument);
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
            return (ConceptToken) super.divide(rightArgument);
        }
        
        Concept addend1 = _concept;
        Concept addend2 = ((ConceptToken) rightArgument)._concept;
        
        if (addend1 instanceof FlatTokenInfiniteConcept && addend2 instanceof FlatTokenInfiniteConcept) {
            if (addend1.getOntology().equals(addend2.getOntology())) {
                FlatTokenInfiniteConcept result = FlatTokenInfiniteConcept.
                    createFlatTokenInfiniteConcept(addend1.getOntology(),
                            ((FlatTokenInfiniteConcept) addend1).getRepresentative(),
                            ((FlatTokenInfiniteConcept) addend1).getTokenValue().
                                subtract(((FlatTokenInfiniteConcept) addend2).getTokenValue()));
                return new ConceptToken(result);
            }
        }
        
        return (ConceptToken) super.subtract(rightArgument);
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
    
    /* FIXME: What to do when we want slightly different results for
     * different ontologies?
    private FlatTokenInfiniteConcept _generateResultInfiniteConcept(Token value,
            FlatTokenRepresentativeConcept otherConceptRep) throws IllegalActionException {
        FlatTokenRepresentativeConcept thisConceptRep =
            ((FlatTokenInfiniteConcept) _concept).getRepresentative();
        if (thisConceptRep.equals(otherConceptRep) && !(thisConceptRep instanceof FlatScalarTokenRepresentativeConcept)) {
            return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(_concept.getOntology(), thisConceptRep, value);
        } else if (thisConceptRep instanceof FlatScalarTokenRepresentativeConcept && otherConceptRep instanceof FlatScalarTokenRepresentativeConcept && value instanceof ScalarToken) {
            if (((FlatScalarTokenRepresentativeConcept) thisConceptRep).withinInterval((ScalarToken) value)) {
                return FlatScalarTokenInfiniteConcept.createFlatScalarTokenInfiniteConcept(_concept.getOntology(), (FlatScalarTokenRepresentativeConcept) thisConceptRep, (ScalarToken) value);
            } else if (((FlatScalarTokenRepresentativeConcept) otherConceptRep).withinInterval((ScalarToken) value)) {
                return FlatScalarTokenInfiniteConcept.createFlatScalarTokenInfiniteConcept(_concept.getOntology(), (FlatScalarTokenRepresentativeConcept) otherConceptRep, (ScalarToken) value);
            } else {
                for (Object conceptRep : _concept.getOntology().entityList(FlatScalarTokenRepresentativeConcept.class)) {
                    if (((FlatScalarTokenRepresentativeConcept) conceptRep).withinInterval((ScalarToken) value)) {
                        return FlatScalarTokenInfiniteConcept.createFlatScalarTokenInfiniteConcept(_concept.getOntology(), (FlatScalarTokenRepresentativeConcept) conceptRep, (ScalarToken) value);
                    }
                }                
                return null;
            }
        } else {
            return null;
        }
    }
    */

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** The concept encapsulated by this token
     */
    private Concept _concept;

}
