/* A token that contains an ontology concept.

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
package ptolemy.data.ontologies;

import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
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
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (blickly)
 @Pt.AcceptedRating Red (blickly)
 */
public class ConceptToken extends ObjectToken implements PartiallyOrderedToken {

    /** Create a ConceptToken from a given Concept.
     *  @param c The given Concept
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the ConceptTolen object.
     */
    public ConceptToken(Concept c) throws IllegalActionException {
        super(c, c == null ? null : c.getClass());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the concept encapsulated by this token.
     *  @return The concept value.
     */
    public Concept conceptValue() {
        return (Concept) getValue();
    }

    /** Return the type of this token.
     *  @return ConceptType.CONCEPT
     */
    @Override
    public ConceptType getType() {
        return ConceptType.CONCEPT;
    }

    /** Compare this ConceptToken to the given argument, and return true if
     *  they refer to the same concept in the same lattice.  If either is null,
     *  return false.
     *
     *  @param rightArgument The argument.
     *  @return true if the values are the same Concept, or false otherwise.
     */
    @Override
    public BooleanToken isEqualTo(Token rightArgument) {
        if (this != null
                && rightArgument != null
                && rightArgument instanceof ConceptToken
                && ((ConceptToken) rightArgument).conceptValue() != null
                && conceptValue() != null
                && ((ConceptToken) rightArgument).conceptValue().equals(
                        conceptValue())) {
            return BooleanToken.TRUE;
        } else {
            return BooleanToken.FALSE;
        }
    }

    /** Check whether the value of this token is strictly less than that of the
     *  argument token.  Note that ontologies are only partial orders, so
     *  !(a &lt; b) does not imply (a &ge; b).
     *
     *  @param rightArgument The token on greater than side of the inequality.
     *  @return BooleanToken.TRUE, if this token is less than the
     *    argument token. BooleanToken.FALSE, otherwise.
     *  @exception IllegalActionException If the argument token and this
     *    token are of incomparable types, or are concepts from
     *    different ontologies.
     */
    @Override
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
        boolean lessThanOrEqual = rightConcept != null
                && rightConcept.isAboveOrEqualTo(leftConcept);
        boolean equal = leftConcept != null && rightConcept != null
                && leftConcept.equals(rightConcept);
        return new BooleanToken(lessThanOrEqual && !equal);
    }

    /** Return the value of this concept token as a string.
     *  @return The name of the concept contained by this token as a string value.
     */
    @Override
    public String toString() {
        if (conceptValue() != null) {
            return conceptValue().toString();
        } else {
            return super.toString();
        }
    }
}
