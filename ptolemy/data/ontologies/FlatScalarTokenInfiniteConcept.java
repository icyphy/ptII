/* A concept that represents the concept values of entries in a record token.
 *
 * Copyright (c) 2010-2014 The Regents of the University of California. All
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
package ptolemy.data.ontologies;

import ptolemy.data.ScalarToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A concept that represents the concept values of entries in a record token.
 *
 *  A conceptable model element such as a port or node in a Ptolemy expression
 *  could contain a token value that is a record data type.  A record token
 *  is a token that is a collection of multiple token values of different
 *  types. For example, we might want to specify a record that indicates the
 *  (x,y) pixel position on a black-and-white screen, and also true or false
 *  for whether that pixel position is on or off.  We can use a record of the form:
 *  {x = 34, y = 26, pixelOn = true}
 *  <p>
 *  This RecordConcept allows any record to be assigned concept values for its
 *  individual elements from an arbitrary finite ontology. For example, if we
 *  wanted to assign a concept to the token above from the constAbstractInterpretation
 *  ontology, it would be:
 *  {x = Positive, y = Positive, pixelOn = BooleanTrue}
 *
 *  This code is adapted from the
 *  {@link ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept}
 *  implementation.
 *  @see FlatScalarTokenRepresentativeConcept
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class FlatScalarTokenInfiniteConcept extends FlatTokenInfiniteConcept {

    ///////////////////////////////////////////////////////////////////
    ////             public constructors/factories                 ////

    /** Create a new flat token infinite concept, belonging to the given
     *  ontology, with an automatically generated name.
     *
     *  @param ontology The ontology to which this concept belongs.
     *  @param representative The finite concept that represents where the infinite
     *   token concepts belong in the ontology lattice.
     *  @param value The token value for this FlatTokenInfiniteConcept.
     *  @return The newly created RecordConcept.
     *  @exception IllegalActionException If the base class throws it.
     */
    public static FlatScalarTokenInfiniteConcept createFlatScalarTokenInfiniteConcept(
            Ontology ontology,
            FlatScalarTokenRepresentativeConcept representative,
            ScalarToken value) throws IllegalActionException {
        try {
            if (!representative.withinInterval(value)) {
                throw new IllegalActionException("Token " + value + " is not "
                        + "within the numerical interval defined in the "
                        + "representative concept.");
            }
            return new FlatScalarTokenInfiniteConcept(ontology, representative,
                    value);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(
                    "Name conflict with automatically generated infinite concept name.\n"
                            + "This should never happen."
                            + "Original exception:" + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the concept that represents this set of infinite concepts in the
     *  ontology lattice.
     *  @return The representative concept.
     */
    @Override
    public FlatScalarTokenRepresentativeConcept getRepresentative() {
        return (FlatScalarTokenRepresentativeConcept) _representative;
    }

    /** Get the token value contained by this concept.
     *  @return The token value contained by this concept.
     */
    @Override
    public ScalarToken getTokenValue() {
        return (ScalarToken) _tokenValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////

    /** Create a new Record concept, belonging to the given
     *  ontology.
     *
     *  @param ontology The ontology to which this concept belongs.
     *  @param representative The finite concept that represents where the infinite
     *   token concepts belong in the ontology lattice.
     *  @param value The token value for this FlatTokenInfiniteConcept.
     *  @exception NameDuplicationException Should never be thrown.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected FlatScalarTokenInfiniteConcept(Ontology ontology,
            FlatScalarTokenRepresentativeConcept representative,
            ScalarToken value) throws IllegalActionException,
            NameDuplicationException {
        super(ontology, representative, value);
    }
}
