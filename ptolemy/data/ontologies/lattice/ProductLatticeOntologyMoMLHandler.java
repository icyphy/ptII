/* An attribute that helps a ProductLatticeOntologySolver to issue MoML requests and
 make changes to the model.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.util.List;
import java.util.Set;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.OntologyMoMLHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntologyMoMLHandler

/** This is an attribute used by the ProductLatticeOntologySolver to issue MoML
 *  requests and make changes to the model. It extends the OntologyMoMLHandler
 *  by allowing a ProductLatticeOntologySolver to highlight its concepts
 *  according to the concept values of the individual lattices that compose
 *  the product lattice ontology.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
public class ProductLatticeOntologyMoMLHandler extends OntologyMoMLHandler {

    /** Construct a ProductLstticeOntologyMoMLHandler with the specified container and name.
     *  @param container The container which should be an instance of ProductLatticeOntologySolver.
     *  @param name The name of the ProductLatticeOntologyMoMLHandler.
     *  @exception IllegalActionException If the ProductLatticeOntologyMoMLHandler
     *   is not of an acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ProductLatticeOntologyMoMLHandler(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _highlightColorsOntology = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Highlight concepts that have already been resolved, but do not run solver.
     *  The highlight colors are based on whichever of the product lattice ontology's
     *  component ontologies is currently being used. This is set by calling
     *  {@link #setHighlightOntology}.
     *  Otherwise, do nothing.
     *  @param objects The set of objects to highlight.
     */
    public void highlightConcepts(Set<Object> objects) {
        if (objects != null && _highlightColorsOntology != null) {
            // Get the PropertySolver.
            ProductLatticeOntologySolver solver = (ProductLatticeOntologySolver) getContainer();
            
            for (Object object : objects) {
                if (object instanceof NamedObj) {
                    ProductLatticeConcept productLatticeConcept = (ProductLatticeConcept) solver.getResolvedConcept(object, false);
                    Concept concept = _getTupleConcept(productLatticeConcept, _highlightColorsOntology);
                    if (concept != null) {
                        // Use the color in the concept instance.
                        List<ColorAttribute> colors = concept
                                .attributeList(ColorAttribute.class);
                        if (colors != null && colors.size() > 0) {
                            // ConceptIcon renders the first found ColorAttribute,
                            // so we use that one here as well.
                            ColorAttribute conceptColor = colors.get(0);
                            String request = "<property name=\"_highlightColor\" "
                                    + "class=\"ptolemy.actor.gui.ColorAttribute\" value=\""
                                    + conceptColor.getExpression() + "\"/>";
                            MoMLChangeRequest change = new MoMLChangeRequest(this,
                                    (NamedObj) object, request, false);
                            ((NamedObj) object).requestChange(change);
                        }
                    }
                }
            }
            // Force a single repaint after all the above requests have been processed.
            solver.requestChange(new MoMLChangeRequest(this, solver, "<group/>"));
        }
        
        if (_highlightColorsOntology == null) {
            try {
                clearDisplay(true, false);
            } catch (IllegalActionException ex) {
                throw new IllegalArgumentException("", ex);
            }
        }
    }
    
    /** Set the current ontology from which to derive the highlight colors
     *  for the ProductLatticeOntology. If the argument is null, no highlight
     *  colors will be used.
     *  @param ontology The ontology from which to derive highlight colors, or
     *   null if no colors will be used.
     *  @throws IllegalActionException Thrown if the ontology is not a contained
     *   in the product lattice ontology.
     */
    public void setHighlightOntology(Ontology ontology) throws IllegalActionException {
        if (ontology != null) {
            ProductLatticeOntology productOntology =
                ((ProductLatticeOntologySolver) getContainer()).getOntology();
            List<Ontology> ontologies = productOntology.getLatticeOntologies();
            if (!ontologies.contains(ontology)) {
                throw new IllegalActionException(this, "The ontology " +
                        ontology.getName() + " is not contained in the product lattice " +
                            "ontology " + productOntology.getName() + ".");
            }
        }
        _highlightColorsOntology = ontology;
    }    
    
    /** Get the concept from the highlight ontology that is contained in the given
     *  product lattice concept, or null if it is not found in the highlight ontology.
     *  @param productLatticeConcept The product lattice concept
     *  @param highlightOntology The sub ontology from which the colors will be used.
     *  @return The concept from the highlightOntology that is contained in
     *   the productLatticeConcept, or null if it is not found in the highlight ontology.
     */
    private Concept _getTupleConcept(ProductLatticeConcept productLatticeConcept, Ontology highlightOntology) {
        if (productLatticeConcept != null) {
            List<Concept> tupleConcepts = productLatticeConcept.getConceptTuple();
            for (Concept concept : tupleConcepts) {
                if (concept.getOntology().getClassName().equals(highlightOntology.getClassName())) {
                    return concept;
                }
            }
        }
        
        return null;
    }
    
    /** The current ontology from which to derive the highlight colors for the
     *  product lattice ontology. Or null if no colors will be highlighted.
     */
    Ontology _highlightColorsOntology;
}
