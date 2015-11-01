/* A Graph Controller for ontology models.
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import ptolemy.actor.gui.Configuration;
import ptolemy.data.ontologies.Concept;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.WithIconGraphController;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.FigureAction;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionDragger;
import diva.graph.EdgeController;
import diva.graph.GraphException;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.NodeController;

///////////////////////////////////////////////////////////////////
//// OntologyGraphController

/** A Graph Controller for ontology models. This controller allows lattice
 *  elements to be dragged and dropped onto its graph. Arcs can be created by
 *  control-clicking and dragging from one element to another.
 *
 *  @author Charles Shelton, Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class OntologyGraphController extends WithIconGraphController {

    /** Create a new ontology graph controller object.
     */
    public OntologyGraphController() {
        super();
        _createControllers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  For the ontology editor, a command is added to
     *  the graph menu to create a new ontology concept.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    @Override
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);

        // Add an item that adds new concepts.
        diva.gui.GUIUtilities.addMenuItem(menu, _newConceptAction);
    }

    /** Return the edge controller appropriate for the given edge. In the
     *  ontology editor, all edges are ontology relations, so the controller
     *  is always a RelationController object.
     *  @param object The given edge in the ontology model editor.
     *  @return The RelationController for all ontology relations in the
     *   ontology editor.
     */
    @Override
    public EdgeController getEdgeController(Object object) {
        return _relationController;
    }

    /** Return the node controller appropriate for the given node object. In the
     *  ontology editor, all nodes are ontology concepts, so the controller
     *  is always a ConceptController object or an AttributeController object
     *  for annotation attributes.
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

            if (semanticObject instanceof Concept) {
                return _conceptController;
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
        _conceptController.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add hot keys to the actions in the given JGraph. For the ontology
     *  graph controller, add the hot keys for the concept controller and
     *  attribute controller.
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);

        _conceptController.addHotKeys(jgraph);
        _attributeController.addHotKeys(jgraph);
    }

    /** Initialize all the controller objects for elements in the ontology
     *  editor. This consists of a controller for attributes, concepts,
     *  and relations in the ontology.
     *  The parent class WithIconGraphController also references a port
     *  controller, so we must initialize it here even though the ontology
     *  model editor has no visible ports.
     */
    @Override
    protected void _createControllers() {
        _attributeController = new AttributeInOntologyController(this);
        _conceptController = new ConceptController(this);
        _relationController = new ConceptRelationController(this);

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

        // Create the interactor that drags new edges.
        _relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_shortcutFilter);

        // NOTE: Do not use _initializeInteraction() because we are
        // still in the constructor, and that method is overloaded in
        // derived classes.
        ((CompositeInteractor) _conceptController.getNodeInteractor())
        .addInteractor(_relationCreator);
    }

    /** Initialize interactions for the specified controller.  This
     *  method is called when a new controller is constructed. In this
     *  class, this method attaches a relation creator to the controller
     *  if the controller is an instance of ConceptController.
     *  @param controller The controller for which to initialize interaction.
     */
    @Override
    protected void _initializeInteraction(NamedObjController controller) {
        super._initializeInteraction(controller);

        if (controller instanceof ConceptController) {
            Interactor interactor = controller.getNodeInteractor();

            if (interactor instanceof CompositeInteractor) {
                ((CompositeInteractor) interactor)
                .addInteractor(_relationCreator);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The controller for attribute objects in the model. */
    protected AttributeInOntologyController _attributeController;

    /** The controller for concepts in the ontology model. */
    protected ConceptController _conceptController;

    /** The controller for relations in the ontology model. */
    protected ConceptRelationController _relationController;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The action for creating concepts in the ontology. */
    private NewConceptAction _newConceptAction = new NewConceptAction();

    /** The interactor that interactively creates edges. */
    private RelationCreator _relationCreator; // For control-click

    /** The selection interactor for drag-selecting nodes */
    private SelectionDragger _selectionDragger;

    /** The filter for shortcut operations.  This is used for creation
     *  of relations and creation of links from relations. Under PC,
     *  this is a control-1 click.  Under Mac OS X, the control key is
     *  used for context menus and this corresponds to the command-1
     *  click.  For details, see the Apple java archive
     *  http://lists.apple.com/archives/java-dev User: archives,
     *  passwd: archives
     */
    private MouseFilter _shortcutFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK, Toolkit.getDefaultToolkit()
            .getMenuShortcutKeyMask());

    ///////////////////////////////////////////////////////////////////
    ////                         private inner classes             ////

    ///////////////////////////////////////////////////////////////////
    //// NewConceptAction

    /** An action to create a new concept in the ontology model. */
    @SuppressWarnings("serial")
    private class NewConceptAction extends FigureAction {

        /** Construct a new concept. */
        public NewConceptAction() {
            super("New Concept");
            putValue("tooltip", "New Concept");
        }

        /** Execute the action.
         *  @param e The event that is received to perform the new concept action.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            if (getSourceType() == MENUBAR_TYPE) {
                // No location in the action, so put it in the middle.
                BasicGraphFrame frame = OntologyGraphController.this.getFrame();
                Point2D center;

                if (frame != null) {
                    // Put in the middle of the visible part.
                    center = frame.getCenter();
                    x = center.getX();
                    y = center.getY();
                } else {
                    // Put in the middle of the pane.
                    GraphPane pane = getGraphPane();
                    center = pane.getSize();
                    x = center.getX() / 2;
                    y = center.getY() / 2;
                }
            } else {
                x = getX();
                y = getY();
            }

            OntologyGraphModel graphModel = (OntologyGraphModel) getGraphModel();
            NamedObj toplevel = graphModel.getPtolemyModel();

            String conceptName = toplevel.uniqueName("Concept");

            // Create the concept.
            String moml = null;
            String locationName = "_location";

            // Try to get the class name for the concept from the library,
            // so that the library and the toolbar are assured of creating
            // the same object.
            try {
                LibraryAttribute attribute = (LibraryAttribute) toplevel
                        .getAttribute("_library", LibraryAttribute.class);

                if (attribute != null) {
                    CompositeEntity library = attribute.getLibrary();
                    Entity prototype = library.getEntity("Concept");

                    if (prototype != null) {
                        Location newConceptLocation = (Location) prototype
                                .getAttribute(locationName, Location.class);
                        if (newConceptLocation == null) {
                            newConceptLocation = new Location(prototype,
                                    locationName);
                            newConceptLocation
                            .setLocation(new double[] { x, y });
                        }
                        newConceptLocation.setLocation(new double[] { x, y });
                        moml = prototype.exportMoML(conceptName);
                    }
                }
            } catch (Exception ex) {
                // Ignore and use the default.
                // Avoid a FindBugs warning about ignored exception.
                moml = null;
            }

            if (moml == null) {
                moml = "<entity name=\""
                        + conceptName
                        + "\" class=\"ptolemy.data.ontologies.FiniteConcept\">\n"
                        + "<property name=\"solutionColor\" "
                        + "class=\"ptolemy.actor.gui.ColorAttribute\" "
                        + "value=\"{0.2,1.0,0.4,1.0}\"/>\n"
                        + "<property name=\"LatticeElementIcon\" "
                        + "class=\"ptolemy.vergil.ontologies.ConceptIcon\"/>\n"
                        + "<property name=\"" + locationName
                        + "\" class=\"ptolemy.kernel.util.Location\""
                        + " value=\"[" + x + ", " + y + "]\"/>\n"
                        + "</entity>\n";
            }

            ChangeRequest request = new MoMLChangeRequest(this, toplevel, moml);
            toplevel.requestChange(request);

            try {
                request.waitForCompletion();
            } catch (Exception ex) {
                throw new GraphException(ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// RelationCreator

    /** An interactor that interactively drags edges from one terminal
     *  to another.
     */
    private class RelationCreator extends AbstractInteractor {

        /** Initiate creation of an arc.
         *  @param event The mouse event to be reacted to by the relation
         *   creator.
         */
        @Override
        public void mousePressed(LayerEvent event) {
            Figure source = event.getFigureSource();
            NamedObj sourceObject = (NamedObj) source.getUserObject();

            Link relationLink = new Link();

            // Set the tail, going through the model so the link is added
            // to the list of links.
            OntologyGraphModel model = (OntologyGraphModel) getGraphModel();
            model.getRelationModel().setTail(relationLink, sourceObject);

            try {
                // add it to the foreground layer.
                FigureLayer layer = getGraphPane().getForegroundLayer();
                Site headSite;
                Site tailSite;

                // Temporary sites.  One of these will get removed later.
                headSite = new AutonomousSite(layer, event.getLayerX(),
                        event.getLayerY());
                tailSite = new AutonomousSite(layer, event.getLayerX(),
                        event.getLayerY());

                // Render the edge.
                Connector c = getEdgeController(relationLink).render(
                        relationLink, layer, tailSite, headSite);

                // get the actual attach site.
                tailSite = getEdgeController(relationLink).getConnectorTarget()
                        .getTailSite(c, source, event.getLayerX(),
                                event.getLayerY());

                if (tailSite == null) {
                    throw new RuntimeException("Invalid connector target: "
                            + "no valid site found for tail of new connector.");
                }

                // And reattach the connector.
                c.setTailSite(tailSite);

                // Add it to the selection so it gets a manipulator, and
                // make events go to the grab-handle under the mouse
                Figure ef = getFigure(relationLink);
                getSelectionModel().addSelection(ef);

                ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
                GrabHandle gh = cm.getHeadHandle();
                layer.grabPointer(event, gh);
            } catch (Exception ex) {
                MessageHandler.error("Drag connection failed:", ex);
            }
        }
    }
}
