/* An abstract base class for a finite concept in an ontology that represents
 * a set of infinite concepts.
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

import java.util.HashSet;
import java.util.Set;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// InfiniteConceptRepresentative

/** An abstract base class for a finite concept in an ontology that represents
 *  a set of infinite concepts.
 *  @see InfiniteConcept
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public abstract class InfiniteConceptRepresentative extends FiniteConcept {

    /** Create a new InfiniteConceptRepresentative with the specified name and
     *  ontology.
     *
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public InfiniteConceptRepresentative(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
        _instantiatedInfiniteConcepts = new HashSet<InfiniteConcept>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the InfiniteConceptRepresentative into the specified workspace.
     *  The result is a new InfiniteConceptRepresentative that is cloned normally
     *  with the superclass ComponentEntity clone() method with the exception
     *  that its private set of instantiated infinite concepts is not copied to
     *  the new object. This is necessary to prevent the cloned
     *  InfiniteConceptRepresentative from having references to the original
     *  representative's set of instantiated infinite concepts.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return A new instance of InfiniteConceptRepresentative.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        InfiniteConceptRepresentative newObject = (InfiniteConceptRepresentative) super
                .clone(workspace);
        newObject._instantiatedInfiniteConcepts = new HashSet<InfiniteConcept>();
        return newObject;
    }

    /** Return the InfiniteConcept that is represented by the given string.
     *  If the infinite concept has already been instantiated, return it.
     *  If not, instantiate a new infinite concept for the given string and
     *  return it.
     *  @param infiniteConceptString The specified string from which to
     *   return an infinite concept for this representative.
     *  @return The InfiniteConcept that is represented by the given string.
     *  @exception IllegalActionException Thrown if there is an error finding or
     *   creating the infinite concept from the given string.
     */
    public InfiniteConcept getInfiniteConceptByString(
            String infiniteConceptString) throws IllegalActionException {
        InfiniteConcept result = _findInstantiatedInfiniteConcept(infiniteConceptString);

        if (result == null) {
            result = _createInfiniteConceptInstance(infiniteConceptString);
            _addInfiniteConcept(result);
        }

        return result;
    }

    /** Return the set of instantiated infinite concepts that are represented
     *  by this concept.
     *  @return The set of instantiated infinite concepts that are represented
     *   by this concept.
     */
    public Set<InfiniteConcept> getInstantiatedInfiniteConcepts() {
        return _instantiatedInfiniteConcepts;
    }

    /** Return true if the string can represent an infinite concept for this
     *  representative, false otherwise. Derived classes must implement
     *  this method based on how they generate their infinite concepts.
     *  @param infiniteConceptString The string that represents the infinite
     *   concept.
     *  @return true if the string can represent an infinite concept for this
     *   representative, false otherwise.
     */
    public abstract boolean containsThisInfiniteConceptString(
            String infiniteConceptString);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the set of instantiated infinite concepts for this
     *  representative.
     *  @exception IllegalActionException Thrown if there is a problem setting
     *   the containers of the infinite concepts to null.
     */
    protected void _clearInstantiatedInfiniteConcepts()
            throws IllegalActionException {
        for (InfiniteConcept infiniteConcept : _instantiatedInfiniteConcepts) {
            try {
                infiniteConcept.setContainer(null);
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(this, ex, "Could not set "
                        + "the container for infinite concept "
                        + infiniteConcept + " to null.");
            }
        }
        _instantiatedInfiniteConcepts.clear();
    }

    /** Create a new infinite concept for the given infinite concept string.
     *  Derived classes must implement this method to enable new infinite
     *  concepts to be generated from their representatives.
     *  @param infiniteConceptString The specified concept string that
     *   represents the infinite concept to be created.
     *  @return The newly created InfiniteConcept object.
     *  @exception IllegalActionException Thrown if the infinite concept cannot
     *   be created.
     */
    protected abstract InfiniteConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add an instantiated infinite concept that is represented by this
     *  concept to its set of instantiated infinite concepts.
     *  @param concept The FlatTokenInfiniteConcept to be added.
     */
    private void _addInfiniteConcept(InfiniteConcept concept) {
        _instantiatedInfiniteConcepts.add(concept);
    }

    /** Return the infinite concept whose toString() method returns the given
     *  string if it has already been instantiated. If an infinite concept
     *  contained by this representative with the given string has not been
     *  instantiated, return null.
     *  @param infiniteConceptString The string that represents the
     *   infinite concept to be found.
     *  @return The infinite concept if it is already instantiated,
     *   otherwise null.
     */
    private InfiniteConcept _findInstantiatedInfiniteConcept(
            String infiniteConceptString) {
        for (InfiniteConcept concept : _instantiatedInfiniteConcepts) {
            if (infiniteConceptString.equals(concept.toString())) {
                return concept;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of instantiated infinite concepts for this representative. */
    private Set<InfiniteConcept> _instantiatedInfiniteConcepts;
}
