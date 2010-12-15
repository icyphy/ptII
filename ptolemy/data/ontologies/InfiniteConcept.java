/* A concept that is not part of a finite ontology.
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

import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A concept that is not part of a finite ontology.
 *  This class represents Concepts that, rather than being drawn from
 *  a fixed finite set of the elements in a ConceptGraph, are drawn
 *  from a potentially infinite set, such as those used to represent
 *  structured datatypes like records, concepts parameterized over values,
 *  and other situations where enumerating all possible concepts beforehand
 *  is not feasible.
 *  <p>
 *  Since this class aims to be a general superclass of any type of infinite
 *  concept, it does not provide any implementations that may be particular
 *  to a particular style of infinite concepts, and is abstract.
 *  Subclasses are responsible for determining exactly what type of infinite
 *  concept they will support.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 *
 */
public abstract class InfiniteConcept extends Concept {

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
     */
    public abstract int compare(Concept concept) throws IllegalActionException;

    /** Return if this concept is equal to the given object,
     *  which is only the case if compare returns CPO.SAME.
     *
     *  @param concept Object with which to compare.
     *  @return True, if both concepts are the same. False, otherwise.
     */
    public boolean equals(Object concept) {
        if (concept instanceof Concept && getOntology() != null
                && ((Concept) concept).getOntology() != null) {
            try {
                return compare((Concept) concept) == CPO.SAME;
            } catch (IllegalActionException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /** Return a hash code for this Concept.
     *  @return A valid hash code.
     *  @see java.lang.Object#hashCode()
     */
    abstract public int hashCode();

    /** Compute the least upper bound (LUB) of this and another concept.
     *  
     *  @param concept The other concept
     *  @return The concept that is the LUB of this and the given concept.
     *  @exception IllegalArgumentException If concepts are not drawn from
     *      the same ontology.
     */
    public abstract Concept leastUpperBound(Concept concept)
            throws IllegalArgumentException;

    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////

    /** Create a new Infinite concept, belonging to the given
     *  ontology, with an automatically generated name.
     *  <p>
     *  This constructor is not thread safe!
     *  Even though this method should never throw a NameDuplicationException,
     *  it is possible in a mutithreaded environment, due to a race condition
     *  that cannot be avoided.
     *  It is recommended that subclasses create a factory method that
     *  synchronizes access to this method to provide thread safety.
     * 
     *  @param ontology The finite ontology to which this belongs.
     *  @exception NameDuplicationException If two threads happen to enter this
     *   constructor concurrently and interleave to generate the same concept
     *   twice.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected InfiniteConcept(Ontology ontology) throws IllegalActionException,
            NameDuplicationException {
        // There is a race condition here if the ++ operation is not atomic.
        // Unfortunately, Java requirements on constructors make adding
        // synchronization impossible.
        super(ontology, "InfiniteConcept_" + ++_conceptNumber);
        setName(getName() + " (of " + getClass().getSimpleName() + ")");

        // Don't store InfiniteConcept instances in the MoML model.
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** Used for internal bookkeeping to make sure that generated
     *  concept names are unique.
     */
    private static int _conceptNumber = 0;

}
