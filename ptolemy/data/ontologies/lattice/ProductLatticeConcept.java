/* A concept in a product lattice-based ontology.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
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
 *
 */

package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ProductLatticeConcept

/** A concept in a product lattice-based ontology.
 *  Represents a concept that is composed of a tuple of other concepts derived
 *  from other ontologies.
 *
 *  @see ProductLatticeOntology
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeConcept extends FiniteConcept {

    /** Create a new product lattice concept with the specified name and the
     *  specified product lattice ontology.
     *
     *  @param ontology The specified product lattice ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public ProductLatticeConcept(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
    }

    /** Create a new product lattice concept with the specified name and the
     *  specified product lattice ontology.
     *
     *  @param ontology The specified product lattice ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @param conceptTuple The list of concepts that compose this product lattice concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public ProductLatticeConcept(ProductLatticeOntology ontology, String name,
            List<Concept> conceptTuple) throws NameDuplicationException,
            IllegalActionException {
        this(ontology, name);
        _conceptTuple = new ArrayList<Concept>(conceptTuple);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the color attribute associated with this ProductLatticeConcept.
     *  This depends on the component color ontology that is set by the
     *  container {@link ProductLatticeOntology#setColorOntology(Ontology)}.
     *  The color of the concept will be derived from the color of the concept
     *  in the tuple from the specified component color ontology.
     *  @return The current color attribute for this product lattice concept.
     *  @exception IllegalActionException Thrown if there is an error getting the
     *   color from the component concept value.
     */
    @Override
    public ColorAttribute getColor() throws IllegalActionException {
        Ontology colorOntology = ((ProductLatticeOntology) getOntology())
                .getColorOntology();

        if (colorOntology != null) {
            Concept componentConcept = getComponentConceptValue(colorOntology);
            if (componentConcept != null) {
                return componentConcept.getColor();
            }
        }
        return null;
    }

    /** Return the first concept that is a component of this product lattice
     *  ontology concept tuple and belongs to the given ontology.
     *  @param ontology The specified ontology from which to get the component
     *   concept.  This should be one of the component ontologies from the
     *   product lattice ontology for this concept.
     *  @return The concept from the concept.
     *  @exception IllegalActionException If the specified ontology is not a component
     *   of the product lattice ontology to which this concept belongs.
     */
    public Concept getComponentConceptValue(Ontology ontology)
            throws IllegalActionException {
        // FIXME: What about if there are multiple elements in the tuple with
        // the same ontology?
        for (Concept innerConcept : _conceptTuple) {

            // FIXME: A single ontology could have multiple instances but we don't
            // have a defined equals() method for ontologies, so this hack of
            // comparing their Ptolemy class names is used.
            if (innerConcept.getOntology().getName().equals(ontology.getName())) {
                return ontology.getConceptByString(innerConcept.toString());
            }
        }
        throw new IllegalActionException(
                this,
                "The ontology "
                        + ontology.getName()
                        + " is not a component of this concept's product lattice ontology "
                        + getOntology().getName() + ".");
    }

    /** Return the list of concepts that compose this product lattice concept.
     *  @return The list of concepts that compose this product lattice concept.
     */
    public List<Concept> getConceptTuple() {
        return new ArrayList<Concept>(_conceptTuple);
    }

    /** Return the product lattice ontology that contains this concept.
     *
     *  @return The containing product lattice ontology.
     */
    @Override
    public Ontology getOntology() {
        NamedObj container = getContainer();
        if (container instanceof ProductLatticeOntology) {
            return (ProductLatticeOntology) container;
        } else {
            return null;
        }
    }

    /** Return the string that represents this concept, its name.
     *  @return The string name that represents this concept.
     */
    @Override
    public String toString() {
        if (_conceptTuple == null) {
            return "Uninitialized Product Lattice Concept";
        }
        StringBuffer conceptStringBuffer = new StringBuffer();
        for (Concept concept : _conceptTuple) {
            conceptStringBuffer.append(concept.toString());
        }
        return conceptStringBuffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of concepts that comprise this product lattice concept. */
    private List<Concept> _conceptTuple;
}
