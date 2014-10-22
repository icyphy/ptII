/* A Graph Controller for ontology solver models.
 *
 * Below is the copyright agreement for the Ptolemy II system.
 *
 * Copyright (c) 2009-2014 The Regents of the University of California. All rights
 * reserved.
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
package ptolemy.vergil.ontologies;

import ptolemy.actor.gui.Configuration;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Locatable;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.basic.WithIconGraphController;
import diva.canvas.interactor.SelectionDragger;
import diva.graph.EdgeController;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.NodeController;

///////////////////////////////////////////////////////////////////
//// OntologySolverGraphController

/** A Graph Controller for ontology solver models.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class OntologySolverGraphController extends WithIconGraphController {

    /** Create a new ontology graph controller object.
     */
    public OntologySolverGraphController() {
        super();
        _createControllers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the edge controller appropriate for the given edge. In the
     *  ontology solver editor, there are no edges, so this method returns
     *  null.
     *  @param object The given edge in the ontology solver model editor. No
     *   edges exist in the ontology solver model.
     *  @return null.
     */
    @Override
    public EdgeController getEdgeController(Object object) {
        return null;
    }

    /** Return the node controller appropriate for the given node object. In the
     *  ontology solver editor, all nodes are ontology model objects or
     *  attributes for concept functions or adapter definitions, so the
     *  controller is always a OntologyController object or an
     *  AttributeInOntologyController.
     *  @param object The given node in the ontology model editor.
     *  @return Either a ConceptController object or an AttributeController
     *   object.
     */
    @Override
    public NodeController getNodeController(Object object) {

        // Defer to the superclass if it can provide a controller.
        NodeController result = super.getNodeController(object);

        if (result != null) {
            return result;
        }

        // Superclass cannot provide a controller. Use defaults.
        if (object instanceof Locatable) {
            Object semanticObject = getGraphModel().getSemanticObject(object);

            if (semanticObject instanceof Ontology) {
                return _ontologyController;
            } else if (semanticObject instanceof Attribute) {
                return _attributeController;
            }
        }

        throw new RuntimeException("Node with unknown semantic object: "
                + object);
    }

    /** Set the configuration.  This is used by some of the controllers
     *  when opening files or URLs.
     *  @param configuration The configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _attributeController.setConfiguration(configuration);
        _ontologyController.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add hot keys to the actions in the given JGraph. For the ontology
     *  solver graph controller, add the hot keys for the ontology controller
     *  and attribute controller.
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);

        _attributeController.addHotKeys(jgraph);
        _ontologyController.addHotKeys(jgraph);
    }

    /** Initialize all the controller objects for elements in the ontology
     *  solver editor. This consists of a controller for attributes and
     *  ontologies.
     *  The parent class WithIconGraphController also references a port
     *  controller, so we must initialize it here even though the ontology
     *  model editor has no visible ports.
     */
    @Override
    protected void _createControllers() {
        _attributeController = new AttributeInOntologyController(this);
        _ontologyController = new OntologyEntityController(this);

        // The port controller is not used in the ontology model editor,
        // but it must be initialized since it is referenced in the parent
        // class.
        _portController = new ExternalIOPortController(this);
    }

    /** Initialize interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass
     *  AbstractGraphController. This initialization cannot be done in the
     *  constructor because the controller does not yet have a reference to
     *  its pane at that time.
     */
    @Override
    protected void initializeInteraction() {
        // NOTE: This method name does not have a leading underscore
        // because it is a diva method.
        super.initializeInteraction();

        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionModel(getSelectionModel());

        // If the selectionDragger is consuming, then popup menus don't
        // disappear properly.
        _selectionDragger.setConsuming(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The controller for attribute objects in the model. */
    protected AttributeInOntologyController _attributeController;

    /** The controller for ontologies in the ontology solver model. */
    protected OntologyEntityController _ontologyController;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The selection interactor for drag-selecting nodes */
    private SelectionDragger _selectionDragger;
}
