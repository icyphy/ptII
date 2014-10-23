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

import java.util.Set;
import java.util.TreeSet;

import ptolemy.graph.CPO;
import ptolemy.graph.CPO.BoundType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
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
 *
 *  @author Charles Shelton, Ben Lickly, Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class RecordConcept extends MapTypeInfiniteConcept<Concept> {

    ///////////////////////////////////////////////////////////////////
    ////             public constructors/factories                 ////

    /** Create a new record concept, belonging to the given
     *  ontology, with an automatically generated name.
     *
     *  @param ontology The ontology to which this concept belongs.
     *  @return The newly created RecordConcept.
     *  @exception InternalErrorException If there .
     */
    public static RecordConcept createRecordConcept(Ontology ontology) {
        try {
            return new RecordConcept(ontology);
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(
                    "Name conflict with automatically generated infinite concept name.\n"
                            + "This should never happen.\n"
                            + "Original exception:" + e.toString());
        } catch (IllegalActionException e) {
            throw new InternalErrorException(
                    "There was an error creating a new RecordConcept"
                            + "in the " + ontology + "ontology\n."
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

        // Original bottom and top remain bottom and top.
        if (concept.equals(getOntology().getConceptGraph().bottom())) {
            return CPO.HIGHER;
        } else if (concept.equals(getOntology().getConceptGraph().top())) {
            return CPO.LOWER;
        } else if (!(concept instanceof RecordConcept)) {
            return CPO.INCOMPARABLE;
        }

        RecordConcept righthandSide = (RecordConcept) concept;
        CPO graph = getOntology().getConceptGraph();

        boolean isSuperset = keySet().containsAll(righthandSide.keySet());
        boolean isSubset = righthandSide.keySet().containsAll(keySet());
        if (!isSubset && !isSuperset) {
            return CPO.INCOMPARABLE;
        }

        Set<String> commonFields = _commonKeys(righthandSide);

        boolean seenHigher = false;
        boolean seenLower = false;
        boolean seenIncomparable = false;
        for (String field : commonFields) {
            int result = graph.compare(getConcept(field),
                    righthandSide.getConcept(field));
            switch (result) {
            case CPO.HIGHER:
                seenHigher = true;
                break;
            case CPO.LOWER:
                seenLower = true;
                break;
            case CPO.INCOMPARABLE:
                seenIncomparable = true;
                break;
            case CPO.SAME:
                break;
            default:
                throw new IllegalActionException(this, "ConceptGraph compare "
                        + "did not return one of the defined CPO values. "
                        + "Return value was " + result + ". This should "
                        + "never happen.");
            }
        }
        // Following the Ptolemy type system conventions, a record that has a superset
        // of fields is lower in the lattice, and a record that has a subset of fields
        // is higher in the lattice.
        if (!seenHigher && !seenLower && !seenIncomparable && isSubset
                && isSuperset) {
            return CPO.SAME;
        } else if (!seenLower && !seenIncomparable && isSubset) {
            return CPO.HIGHER;
        } else if (!seenHigher && !seenIncomparable && isSuperset) {
            return CPO.LOWER;
        } else {
            return CPO.INCOMPARABLE;
        }
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the concept that is the correct bound for the given two
     *  concepts.
     *  @param concept1 The first concept.
     *  @param concept2 The second concept.
     *  @param boundType Specifies the type of bound to be returned; either
     *    GREATESTLOWER or LEASTUPPER.
     *  @return The concept that is either the least upper bound or greatest
     *   lower bound of the two concepts.
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
        if (concept instanceof RecordConcept) {
            return _getBoundWithOtherRecordConcept((RecordConcept) concept,
                    boundType);
        } else {
            Concept latticeEnd = null;
            Concept otherLatticeEnd = null;
            ConceptGraph conceptGraph = getOntology().getConceptGraph();
            switch (boundType) {
            case GREATESTLOWER:
                latticeEnd = conceptGraph.bottom();
                otherLatticeEnd = conceptGraph.top();
                break;
            case LEASTUPPER:
                latticeEnd = conceptGraph.top();
                otherLatticeEnd = conceptGraph.bottom();
                break;
            default:
                throw new IllegalArgumentException("Unrecognized bound type: "
                        + boundType + ". Expected either GREATESTLOWER or "
                        + "LEASTUPPER");
            }

            if (concept.equals(otherLatticeEnd)) {
                return this;
            } else {
                return latticeEnd;
            }
        }
    }

    /** Compute either the least upper bound or the greatest lower bound of
     *  this and another record concept.
     *
     *  @param concept The other flat token infinite concept
     *  @param boundType Specifies the type of bound to be returned; either
     *   GREATESTLOWER or LEASTUPPER.
     *  @return The concept that is the bound of this and the given concept.
     */
    private Concept _getBoundWithOtherRecordConcept(RecordConcept concept,
            BoundType boundType) {
        RecordConcept result = createRecordConcept(getOntology());
        Set<String> commonFields = _commonKeys(concept);

        // The least upper bound is the record concept that only contains
        // the common fields and the least upper bound of each concept in that
        // field.
        for (String field : commonFields) {
            Concept fieldConcept = _getBoundFromConceptGraph(
                    this.getConcept(field), concept.getConcept(field),
                    boundType);
            result.putConcept(field, fieldConcept);
        }

        // The greatest lower bound is a record concept that includes all the
        // disjoint fields from both records in addition to the greatest lower
        // bounds of each common field.
        if (boundType.equals(BoundType.GREATESTLOWER)) {
            Set<String> disjointFields = new TreeSet<String>(this.keySet());
            disjointFields.removeAll(commonFields);
            for (String field : disjointFields) {
                result.putConcept(field, this.getConcept(field));
            }

            disjointFields = new TreeSet<String>(concept.keySet());
            disjointFields.removeAll(commonFields);
            for (String field : disjointFields) {
                result.putConcept(field, concept.getConcept(field));
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////

    /** Create a new Record concept, belonging to the given
     *  ontology.
     *
     *  @param ontology The ontology to which this RecordConcept belongs.
     *  @exception NameDuplicationException Should never be thrown.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected RecordConcept(Ontology ontology) throws IllegalActionException,
    NameDuplicationException {
        super(ontology);
    }

}
