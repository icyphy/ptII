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

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.graph.CPO.BoundType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FlatTokenInfiniteConcept

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
 *  @see FlatTokenRepresentativeConcept
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class FlatTokenInfiniteConcept extends InfiniteConcept {

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
    public static FlatTokenInfiniteConcept createFlatTokenInfiniteConcept(
            Ontology ontology, FlatTokenRepresentativeConcept representative,
            Token value) throws IllegalActionException {
        try {
            return new FlatTokenInfiniteConcept(ontology, representative, value);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(
                    "Name conflict with automatically generated infinite concept name.\n"
                            + "This should never happen."
                            + "Original exception:" + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare this concept with the given concept.
     *  Returns an int value that corresponds to the ordering between
     *  the elements as given in the CPO interface.
     *
     *  @param concept The concept with which we are comparing.
     *  @return CPO.HIGHER if this concept is above the given concept,
     *          CPO.LOWER if this concept is below the given concept,
     *          CPO.SAME if both concepts are the same,
     *      and CPO.INCOMPARABLE if concepts are incomparable.
     *  @exception IllegalActionException If the specified concept
     *          does not have the same ontology as this one.
     *  @see ptolemy.data.ontologies.Concept#isAboveOrEqualTo(ptolemy.data.ontologies.Concept)
     */
    @Override
    public int compare(Concept concept) throws IllegalActionException {
        if (concept.getOntology() == null
                || !concept.getOntology().equals(getOntology())) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements from two distinct ontologies");
        }

        // Note that this is different from how equals rules out subclasses
        // with object.getClass() != getClass().
        if (!(concept instanceof FlatTokenInfiniteConcept)) {
            return getOntology().getConceptGraph().compare(_representative,
                    concept);
        } else {
            if (!_representative
                    .equals(((FlatTokenInfiniteConcept) concept)._representative)) {
                return getOntology().getConceptGraph().compare(_representative,
                        ((FlatTokenInfiniteConcept) concept)._representative);
            } else {
                if (_tokenValue.isEqualTo(
                        ((FlatTokenInfiniteConcept) concept)._tokenValue)
                        .booleanValue()) {
                    return CPO.SAME;
                } else {
                    return CPO.INCOMPARABLE;
                }
            }
        }
    }

    /** Return true if the class of the argument is RecordToken, and
     *  the argument has the same set of labels as this token and the
     *  corresponding fields are equal, as determined by the equals
     *  method of the contained tokens.
     *  @param object An instance of Object.
     *  @return True if the argument is equal to this token.
     *  @see #hashCode()
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        // We don't use the compare() method here.  This means this method
        // could be more restrictive than compare().  Note that compare()
        // is not compareTo(), but compareTo() would be a subset of compare().
        // See http://findbugs.sourceforge.net/bugDescriptions.html#EQ_COMPARETO_USE_OBJECT_EQUALS
        // "Eq: Class defines compareTo(...) and uses Object.equals() (EQ_COMPARETO_USE_OBJECT_EQUALS)"
        FlatTokenInfiniteConcept concept = (FlatTokenInfiniteConcept) object;
        Ontology ontology = getOntology();
        if (ontology != null && ontology.equals(concept.getOntology())) {
            if (getRepresentative().equals(concept.getRepresentative())) {
                if (getTokenValue().equals(concept.getTokenValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return the color attribute associated with this FlatTokenInfiniteConcept.
     *  This will be the color to be the color of the
     *  FlatTokenRepresentativeConcept representative which is a finite concept
     *  with a color given by its model color attribute.
     *  @return The color attribute of the representative concept.
     *  @exception IllegalActionException Thrown if there is an error getting the
     *   color from the representative concept.
     */
    @Override
    public ColorAttribute getColor() throws IllegalActionException {
        return _representative.getColor();
    }

    /** Get the concept that represents this set of infinite concepts in the
     *  ontology lattice.
     *  @return The representative concept.
     */
    @Override
    public FlatTokenRepresentativeConcept getRepresentative() {
        return _representative;
    }

    /** Get the token value contained by this concept.
     *  @return The token value contained by this concept.
     */
    public Token getTokenValue() {
        return _tokenValue;
    }

    /** Compute the greatest lower bound (GLB) of this and another concept.
     *
     *  @param concept The other concept
     *  @return The concept that is the GLB of this and the given concept.
     */
    @Override
    public Concept greatestLowerBound(Concept concept) {
        return _getBoundWithOtherConcept(concept, BoundType.GREATESTLOWER);
    }

    /** Compute the least upper bound (LUB) of this and another concept.
     *
     *  @param concept The other concept
     *  @return The concept that is the LUB of this and the given concept.
     */
    @Override
    public Concept leastUpperBound(Concept concept) {
        return _getBoundWithOtherConcept(concept, BoundType.LEASTUPPER);
    }

    /** Return the hash code of this record concept, which is uniquely
     *  determined by the ontology and the set of record field-concept
     *  mappings.
     *  @return The hash code of this concept.
     */
    @Override
    public int hashCode() {
        return getOntology().hashCode() + _representative.hashCode()
                + _tokenValue.hashCode();
    }

    /** Return the string representation of this flat token infinite concept.
     *  It concatenates the name of the representative concept with the
     *  token value.
     *
     *  @return The string representation of this concept.
     */
    @Override
    public String toString() {
        return _representative.getName() + "_" + _tokenValue.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////

    /** Create a new FlatTokenInfiniteConcept, belonging to the given
     *  ontology.
     *
     *  @param ontology The ontology to which this concept belongs.
     *  @param representative The finite concept that represents where the infinite
     *   token concepts belong in the ontology lattice.
     *  @param value The token value for this FlatTokenInfiniteConcept.
     *  @exception NameDuplicationException Should never be thrown.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected FlatTokenInfiniteConcept(Ontology ontology,
            FlatTokenRepresentativeConcept representative, Token value)
                    throws IllegalActionException, NameDuplicationException {
        super(ontology);
        _representative = representative;
        _tokenValue = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the concept that is the correct bound for the given two
     *  concepts.
     *  @param concept1 The first concept.
     *  @param concept2 The second concept.
     *  @param boundType Specifies the type of bound to be returned; either
     *    GREATESTLOWER or LEASTUPPER.
     *  @return The concept that is either the least upper bound or greatest
     *   lower bound of the array of concepts.
     */
    private Concept _getBoundFromConceptGraph(Concept concept1,
            Concept concept2, BoundType boundType) {
        ConceptGraph conceptGraph = getOntology().getConceptGraph();
        switch (boundType) {
        case GREATESTLOWER:
            return conceptGraph.greatestLowerBound(concept1, concept2);
        case LEASTUPPER:
            return conceptGraph.leastUpperBound(concept1, concept2);
        default:
            throw new IllegalArgumentException("Unrecognized bound type: "
                    + boundType + ". Expected either GREATESTLOWER or "
                    + "LEASTUPPER");
        }
    }

    /** Compute either the least upper bound or the greatest lower bound of
     *  this and another concept.
     *
     *  @param concept The other concept.
     *  @param boundType Specifies the type of bound to be returned; either
     *   GREATESTLOWER or LEASTUPPER.
     *  @return The concept that is the bound of this and the given concept.
     */
    private Concept _getBoundWithOtherConcept(Concept concept,
            BoundType boundType) {
        if (concept instanceof FlatTokenInfiniteConcept
                && _representative.equals(((FlatTokenInfiniteConcept) concept)
                        .getRepresentative())) {
            if (this.equals(concept)) {
                return this;
            } else {
                return _getConceptAboveOrBelowRepresentative(boundType);
            }
        } else {
            Concept otherConcept = concept;
            Concept otherConceptRepresentative = null;
            if (concept instanceof InfiniteConcept) {
                otherConceptRepresentative = ((InfiniteConcept) concept)
                        .getRepresentative();
                if (otherConceptRepresentative != null) {
                    otherConcept = otherConceptRepresentative;
                }
            }
            Concept bound = _getBoundFromConceptGraph(_representative,
                    otherConcept, boundType);
            if (bound.equals(_representative)) {
                return this;
            } else if (bound.equals(otherConceptRepresentative)
                    && otherConceptRepresentative != null) {
                return concept;
            } else if (bound instanceof InfiniteConceptRepresentative) {
                return null;
            } else {
                return bound;
            }
        }
    }

    /** Return the concept directly above or below the representative concept
     *  in the ontology lattice.  If there is more than one or zero concepts
     *  directly above or below, or the concept is an InfiniteConceptRepresentative,
     *  then return null.
     *
     *  @param boundType Specifies the type of bound; either
     *   GREATESTLOWER or LEASTUPPER.
     *  @return The concept directly above if boundType is LEASTUPPER or
     *   the concept directly below if boundType is GREATESTLOWER.
     */
    private FiniteConcept _getConceptAboveOrBelowRepresentative(
            BoundType boundType) {
        FiniteConcept[] conceptsAboveOrBelow = new FiniteConcept[0];

        switch (boundType) {
        case GREATESTLOWER:
            conceptsAboveOrBelow = _representative.getCoverSetBelow().toArray(
                    conceptsAboveOrBelow);
            break;
        case LEASTUPPER:
            conceptsAboveOrBelow = _representative.getCoverSetAbove().toArray(
                    conceptsAboveOrBelow);
            break;
        default:
            throw new IllegalArgumentException("Unrecognized bound type: "
                    + boundType + ". Expected either GREATESTLOWER or "
                    + "LEASTUPPER");
        }

        // If there is more than one concept above or below, or the one
        // concept is an InfiniteConceptRepresentative, then there is no
        // least upper bound and the ontology is not a lattice.
        if (conceptsAboveOrBelow.length != 1) {
            return null;
        } else {
            FiniteConcept result = conceptsAboveOrBelow[0];
            if (result instanceof InfiniteConceptRepresentative) {
                return null;
            } else {
                return result;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The finite concept that represents where the infinite token concepts belong
     *  in the ontology lattice.
     */
    protected FlatTokenRepresentativeConcept _representative;

    /** The token value for this FlatTokenInfiniteConcept. */
    protected Token _tokenValue;
}
