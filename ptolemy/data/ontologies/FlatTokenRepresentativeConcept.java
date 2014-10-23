/* A finite concept in an ontology that represents a flat set of infinite
 * concepts that map to a set of arbitrary Ptolemy tokens.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
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
 *
 */
package ptolemy.data.ontologies;

import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FlatTokenRepresentativeConcept

/** A finite concept in an ontology that represents a flat set of infinite
 *  concepts that map to a set of arbitrary Ptolemy tokens.
 *  @see FlatTokenInfiniteConcept
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class FlatTokenRepresentativeConcept extends
InfiniteConceptRepresentative {

    /** Create a new FlatTokenRepresentativeConcept with the specified name and
     *  ontology.
     *
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public FlatTokenRepresentativeConcept(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the string can represent a FlatTokenInfiniteConcept for
     *  this representative, false otherwise.
     *  @param infiniteConceptString The string that represents the infinite
     *   concept.
     *  @return true if the string can represent an infinite concept for this
     *   representative, false otherwise.
     */
    @Override
    public boolean containsThisInfiniteConceptString(
            String infiniteConceptString) {
        if (infiniteConceptString.startsWith(getName() + "_")) {
            return true;
        } else {
            return false;
        }
    }

    /** Return the FlatTokenInfiniteConcept with the given token that is
     *  contained by this representative.
     *  If the FlatTokenInfiniteConcept has already been instantiated, return it.
     *  If not, instantiate a new FlatTokenInfiniteConcept with the given token
     *  and return it.
     *  @param tokenValue The token value that is used to get a
     *   FlatTokenInfiniteConcept.
     *  @return The FlatTokenInfiniteConcept that has the given token and this
     *   representative.
     *  @exception IllegalActionException Thrown if there is an error finding or
     *   creating the FlatTokenInfiniteConcept from the given token.
     */
    public FlatTokenInfiniteConcept getFlatTokenInfiniteConceptByToken(
            Token tokenValue) throws IllegalActionException {

        String conceptString = getName() + "_" + tokenValue.toString();
        return (FlatTokenInfiniteConcept) getInfiniteConceptByString(conceptString);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new FlatTokenInfiniteConcept for the given concept string.
     *  @param infiniteConceptString The specified concept string that
     *   represents the FlatTokenInfiniteConcept to be created.
     *  @return The newly created FlatTokenInfiniteConcept object.
     *  @exception IllegalActionException Thrown if a valid
     *   FlatTokenInfiniteConcept cannot be created.
     */
    @Override
    protected FlatTokenInfiniteConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException {
        if (containsThisInfiniteConceptString(infiniteConceptString)) {
            String expression = infiniteConceptString.substring(getName()
                    .length() + 1);
            Variable tempTokenVariable = null;
            try {
                // Use a temporary Variable object to parse the
                // expression string that represents the token.
                tempTokenVariable = new Variable(this, "_tempTokenVariable");
                tempTokenVariable.setExpression(expression);

                return _instantiateFlatTokenInfiniteConcept(tempTokenVariable
                        .getToken());
            } catch (NameDuplicationException nameDupEx) {
                throw new IllegalActionException(this, nameDupEx,
                        "Could not instantiate "
                                + "a FlatTokenInfiniteConcept for "
                                + infiniteConceptString + ".");
            } finally {
                try {
                    if (tempTokenVariable != null) {
                        tempTokenVariable.setContainer(null);
                    }
                } catch (NameDuplicationException nameDupExAfterSetContainerToNull) {
                    throw new IllegalActionException(
                            this,
                            nameDupExAfterSetContainerToNull,
                            "Could "
                                    + "not remove tempTokenVariable object from this "
                                    + "concept after it is no longer needed.");
                }
            }
        } else {
            throw new IllegalActionException(this, "The given string cannot "
                    + "be used to derive a valid infinite concept contained "
                    + "by this representative.");
        }
    }

    /** Return a new FlatTokenInfiniteConcept for this representative with
     *  the given token value.
     *
     *  @param tokenValue The token value for the FlatTokenInfiniteConcept
     *          to be instantiated.
     *  @return A new FlatTokenInfiniteConcept
     *  @exception IllegalActionException Thrown if the FlatTokenInfiniteConcept
     *   cannot be created.
     */
    protected FlatTokenInfiniteConcept _instantiateFlatTokenInfiniteConcept(
            Token tokenValue) throws IllegalActionException {
        return FlatTokenInfiniteConcept.createFlatTokenInfiniteConcept(
                getOntology(), this, tokenValue);
    }
}
