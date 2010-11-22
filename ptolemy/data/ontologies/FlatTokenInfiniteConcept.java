/* A concept that represents the concept values of entries in a record token.
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
package ptolemy.data.ontologies;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
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
 *  
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 9.0
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
     *  @throws IllegalActionException If the base class throws it.
     */
    public static FlatTokenInfiniteConcept createFlatTokenInfiniteConcept(
            Ontology ontology, FlatTokenRepresentativeConcept representative, Token value)
                throws IllegalActionException {
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
    ////                          public methods                   ////

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
    public int compare(Concept concept) throws IllegalActionException {
        if (concept == null) {
            return CPO.INCOMPARABLE;
        }
        
        if (concept.getOntology() == null || !(concept.getOntology().equals(getOntology()))) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements from two distinct ontologies");
        }
        
        if (!(concept instanceof FlatTokenInfiniteConcept)) {
            return getOntology().getConceptGraph().compare(_representative,
                    concept);
        } else {
            if (!_representative.equals(((FlatTokenInfiniteConcept) concept).
                    _representative)) {
                return getOntology().getConceptGraph().compare(_representative,
                        ((FlatTokenInfiniteConcept) concept)._representative);
            } else {
                if (_tokenValue.isEqualTo(((FlatTokenInfiniteConcept) concept).
                        _tokenValue).booleanValue()) {
                    return CPO.SAME;
                } else {
                    return CPO.INCOMPARABLE;
                }
            }
        }
    }
    
    /** Return the color attribute associated with this FlatTokenInfiniteConcept.
     *  This will be the color to be the color of the
     *  FlatTokenRepresentativeConcept representative which is a finite concept
     *  with a color given by its model color attribute.
     *  @return The color attribute of the representative concept.
     */
    public ColorAttribute getColor() {
        return _representative.getColor();
    }
    
    /** Get the concept that represents this set of infinite concepts in the
     *  ontology lattice.
     *  @return The representative concept.
     */
    public FlatTokenRepresentativeConcept getRepresentative() {
        return _representative;
    }

    /** Get the token value contained by this concept.
     *  @return The token value contained by this concept.
     */
    public Token getTokenValue() {
        return _tokenValue;
    }

    /** Compute the least upper bound (LUB) of this and another concept.
     *  
     *  @param concept The other concept
     *  @return The concept that is the LUB of this and the given concept.
     */
    public Concept leastUpperBound(Concept concept) {
        if (!(concept instanceof FlatTokenInfiniteConcept)) {
            Concept lub = getOntology().getConceptGraph().leastUpperBound(
                    _representative, concept);
            if (lub.equals(_representative)) {
                return this;
            } else {
                return lub;
            }
        } else {
            // We have two FlatTokenInfiniteConcepts
            return leastUpperBound((FlatTokenInfiniteConcept) concept);
        }
    }
    
    /** Compute the least upper bound (LUB) of this and another flat token
     *  infinite concept.
     *  
     *  @param concept The other flat token infinite concept
     *  @return The concept that is the LUB of this and the given concept.
     */
    public Concept leastUpperBound(FlatTokenInfiniteConcept concept) {
        if (!_representative.equals(concept._representative)) {
            Concept lub = getOntology().getConceptGraph().leastUpperBound(
                    _representative, concept._representative);
            if (lub.equals(_representative)) {
                return this;
            } else if (lub.equals(concept._representative)) {
                return concept;
            } else {
                if (lub instanceof FlatTokenRepresentativeConcept) {
                    return getOntology().getConceptGraph().leastUpperBound((
                            (FiniteConcept) lub).getCoverSetAbove().toArray());
                } else {
                    return lub;
                }
            }
        } else {
            // If the concepts have the same representative then they are
            // either the exact same concept and the least upper bound is this,
            // or two incomparable concepts from the same set where their least
            // upper bound is the least upper bound of the concepts directly
            // above the representative in the finite lattice.
            if (this.equals(concept)) {
                return this;
            } else {
                return getOntology().getConceptGraph().leastUpperBound(
                    _representative.getCoverSetAbove().toArray());
            }
        }
    }

    /** Return the hash code of this record concept, which is uniquely
     *  determined by the ontology and the set of record field-concept
     *  mappings.
     *  @return The hash code of this concept.
     */
    public int hashCode() {
        return getOntology().hashCode() + _representative.hashCode() +
            _tokenValue.hashCode();
    }

    /** Return the string representation of this flat token infinite concept.
     *  It concatenates the name of the representative concept with the
     *  token value.
     *  
     *  @return The string representation of this concept.
     */
    public String toString() {
        return _representative.getName() + "_" + _tokenValue.toString();
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
     *  @throws NameDuplicationException Should never be thrown.
     *  @throws IllegalActionException If the base class throws it.
     */
    protected FlatTokenInfiniteConcept(Ontology ontology, FlatTokenRepresentativeConcept representative,
            Token value)
                throws IllegalActionException, NameDuplicationException {
        super(ontology);
        _representative = representative;
        _tokenValue = value;
        _representative.addInfiniteConcept(this);
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
