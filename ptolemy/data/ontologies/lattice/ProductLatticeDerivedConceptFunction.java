/** A concept function that returns a derived concept from a given input
 *  concept for a product lattice ontology to be used for constraints
 *  generated from an ontology that is a component of the product lattice ontology.
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
 */

package ptolemy.data.ontologies.lattice;

import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeDerivedConceptFunction

/** A concept function that returns a derived concept from a given input
 *  concept for a product lattice ontology to be used for constraints
 *  generated from an ontology that is a component of the product lattice ontology.
 *  Whatever the input product lattice concept is, the output is a product
 *  lattice concept whose tuple preserves the value of the concept in the
 *  given component ontology, but sets the other concepts in the tuple to the
 *  bottom of their respective ontology lattices. This is used for constraints
 *  that are derived from the lattice ontology adapters of ontologies that
 *  comprise the product lattice ontology, so that their constraints are
 *  orthogonal to the other ontologies in the product lattice ontology.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeDerivedConceptFunction extends ConceptFunction {

    /** Create a new ProductLatticeDerivedConceptFunction with the given name,
     *  derived from the given original component ontology.
     *  @param name The name of the derived concept function.
     *  @param inputOutputOntology The ProductLatticeOntology for the derived function.
     *  @param originalConceptOntology The original ontology for the original concept input.
     *  @exception IllegalActionException Thrown if the wrapper concept function
     *   cannot be created.
     */
    public ProductLatticeDerivedConceptFunction(String name,
            ProductLatticeOntology inputOutputOntology,
            Ontology originalConceptOntology) throws IllegalActionException {
        super(name, 1, inputOutputOntology);
        _originalConceptOntology = originalConceptOntology;
    }

    /** Return the output of the concept function based on the concept
     *  inputs. Whatever the input product lattice concept is, the output is a product
     *  lattice concept whose tuple preserves the value of the concept in the
     *  given component ontology, but sets the other concepts in the tuple to the
     *  bottom of their respective ontology lattices.
     *  @param argValues The list of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error evaluating the function.
     */
    @Override
    protected Concept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {
        Concept inputConcept = argValues.get(0);

        if (inputConcept != null) {
            Concept originalOntologyInputConcept = ProductLatticeOntologyAdapter
                    .getComponentConceptFromProductLatticeConcept(inputConcept,
                            _originalConceptOntology);

            if (originalOntologyInputConcept != null) {
                Concept result = ProductLatticeOntologyAdapter
                        .getDerivedConceptForProductLattice(
                                originalOntologyInputConcept,
                                (ProductLatticeOntology) _outputRangeOntology);
                if (result == null) {
                    throw new IllegalActionException(
                            "Could not derive product lattice "
                                    + "concept for "
                                    + _outputRangeOntology.getName()
                                    + " from original concept "
                                    + originalOntologyInputConcept
                                    + " in the ontology "
                                    + _originalConceptOntology + ".");
                } else {
                    return result;
                }
            } else {
                throw new IllegalActionException("Could not get the original "
                        + "ontology concept from the input concept "
                        + inputConcept + " for the component ontology "
                        + _originalConceptOntology.getName() + ".");
            }
        } else {
            throw new IllegalActionException(
                    "Input concept argument cannot be null.");
        }
    }

    /** The original ontology from which the input concept was taken. */
    private Ontology _originalConceptOntology;
}
