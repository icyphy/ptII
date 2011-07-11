/* The graph controller for the vergil viewer

 Copyright (c) 1999-2011 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.LocatableNodeController;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.RelationController;
import diva.canvas.Figure;
import diva.canvas.interactor.SelectionDragger;
import diva.graph.EdgeController;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.NodeController;

///////////////////////////////////////////////////////////////////
//// ActorViewerGraphController

/**
 A graph controller for the Ptolemy II schematic viewer.
 This controller contains a set of default node controllers for attributes,
 entities, links, ports, and relations.  Those default controllers can
 be overridden by attributes of type NodeControllerFactory.
 The getNodeController() method determines which controller to return
 for each node.
 <p>
 In addition, this controller provides graph-wide operations that allow
 nodes to be moved and context menus to be created.  It does
 not provide interaction for adding or removing nodes; those are provided
 by a derived class.  If does provide toolbar buttons for executing
 the model (or if this is not the top level, delegating to the top
 level to execute). Right-clicking on the background will
 create a context-sensitive menu for the graph.

 @author Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ActorViewerGraphController extends RunnableGraphController {
    /** Create a new basic controller with default
     *  terminal and edge interactors and default context menus.
     */
    public ActorViewerGraphController() {
        _createControllers();

        // The following is a fallback controller used when encountering
        // instances of Locatable that have no container. Do not create
        // this in _createControllers() because that is overridden by
        // derived classes.
        _locatableController = new LocatableNodeController(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to an event by highlighting the actor being iterated.
     *  This effectively animates the execution.
     *  @param event The debug event.
     */
    public void event(DebugEvent event) {
        if (event instanceof FiringEvent) {
            Actor actor = ((FiringEvent) event).getActor();

            if (actor instanceof NamedObj) {
                NamedObj objToHighlight = (NamedObj) actor;

                // If the object is not contained by the associated
                // composite, then find an object above it in the hierarchy
                // that is.
                AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) getGraphModel();
                NamedObj toplevel = graphModel.getPtolemyModel();

                while ((objToHighlight != null)
                        && (objToHighlight.getContainer() != toplevel)) {
                    objToHighlight = objToHighlight.getContainer();
                }

                if (objToHighlight == null) {
                    return;
                }

                Object location = objToHighlight.getAttribute("_location");

                if (location != null) {
                    Figure figure = getFigure(location);

                    if (figure != null) {
                        if (_animationRenderer == null) {
                            _animationRenderer = new AnimationRenderer();
                        }

                        FiringEvent.FiringEventType type = ((FiringEvent) event)
                                .getType();

                        if ((type == FiringEvent.BEFORE_ITERATE)
                                || (type == FiringEvent.BEFORE_FIRE)) {
                            _animationRenderer.renderSelected(figure);
                            _animated = figure;

                            long animationDelay = getAnimationDelay();

                            if (animationDelay > 0) {
                                try {
                                    Thread.sleep(animationDelay);
                                } catch (InterruptedException ex) {
                                }
                            }
                        } else if ((type == FiringEvent.AFTER_ITERATE)
                                || (type == FiringEvent.AFTER_POSTFIRE)) {
                            if (_animated != null) {
                                _animationRenderer.renderDeselected(_animated);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Return the edge controller appropriate for the given node,
     *  which in this case is the same link controller returned by
     *  getLinkController().
     *  @param edge The edge object.
     *  @return the edge controller.
     */
    public EdgeController getEdgeController(Object edge) {
        return _linkController;
    }

    /** Return the value of the entity controller.
     *  Callers may add context menus by calling
     *  <pre>
     *  getEntityController.addMenuItemFactory(new XXXDialogFactory);
     *  </pre>
     *  @return the entity controller
     */
    public ActorController getEntityController() {
        // Used by jni.ThalesGraphFrame to add jni.ArgumentDialogFactory
        return _entityController;
    }

    /** Return the node controller appropriate for the given object.
     *  If the object is an instance of Vertex, then return the
     *  local relation controller.  If it implements Locatable,
     *  then determine whether it is an Entity, Attribute, or Port,
     *  and return the appropriate default controller.
     *  If the argument is an instance of Port, then return the
     *  local port controller.
     *  @param object A Vertex, Locatable, or Port.
     *  @return the node controller
     */
    public NodeController getNodeController(Object object) {
        // Defer to the superclass if it can provide a controller.
        NodeController result = super.getNodeController(object);

        if (result != null) {
            return result;
        }

        // Superclass cannot provide a controller. Use defaults.
        if (object instanceof Vertex) {
            return _relationController;
        } else if (object instanceof Locatable) {
            Object semanticObject = getGraphModel().getSemanticObject(object);

            if (semanticObject instanceof Entity) {

                boolean isActorOfInterest = false;

                try {

                    StringParameter actorInteractionAddonParameter;
                    actorInteractionAddonParameter = (StringParameter) this
                            .getConfiguration().getAttribute(
                                    "_actorInteractionAddon", Parameter.class);

                    if (actorInteractionAddonParameter != null) {
                        String actorInteractionAddonClassName = actorInteractionAddonParameter
                                .stringValue();

                        Class actorInteractionAddonClass = Class
                                .forName(actorInteractionAddonClassName);

                        ActorInteractionAddon actorInteractionAddon = (ActorInteractionAddon) actorInteractionAddonClass
                                .newInstance();

                        isActorOfInterest = actorInteractionAddon
                                .isActorOfInterestForAddonController((NamedObj) semanticObject);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // In the viewer, there will not be a class definition
                // controller that is distinct from the entity controller.
                // In the edit, there will be.
                if ((_classDefinitionController != null)
                        && ((Entity) semanticObject).isClassDefinition()) {
                    return _classDefinitionController;
                } else if (isActorOfInterest) {
                    return _addonActorController;
                } else {
                    return _entityController;
                }
            } else if (semanticObject instanceof Attribute) {
                return _attributeController;
            } else if (semanticObject instanceof Port) {
                return _portController;
            } else {
                return _locatableController;
            }
        } else if (object instanceof Port) {
            return _entityPortController;
        }

        throw new RuntimeException("Node with unknown semantic object: "
                + object);
    }

    /** Set the configuration.  The configuration is used when
     *  opening documentation files.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _attributeController.setConfiguration(configuration);
        _classDefinitionController.setConfiguration(configuration);
        _entityController.setConfiguration(configuration);
        _entityPortController.setConfiguration(configuration);
        _relationController.setConfiguration(configuration);
        _linkController.setConfiguration(configuration);

        try {

            StringParameter actorInteractionAddon;
            actorInteractionAddon = (StringParameter) this.getConfiguration()
                    .getAttribute("_actorInteractionAddon", Parameter.class);

            if (actorInteractionAddon != null) {
                _addonActorController.setConfiguration(configuration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add hot keys to the actions in the given JGraph.
     *
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);
        _entityController.addHotKeys(jgraph);
        _classDefinitionController.addHotKeys(jgraph);
        _attributeController.addHotKeys(jgraph);

        try {

            StringParameter actorInteractionAddon;
            actorInteractionAddon = (StringParameter) this.getConfiguration()
                    .getAttribute("_actorInteractionAddon", Parameter.class);

            if (actorInteractionAddon != null) {
                _addonActorController.addHotKeys(jgraph);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Create the controllers for nodes in this graph.
     *  In this base class, controllers with PARTIAL access are created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    protected void _createControllers() {
        super._createControllers();
        _attributeController = new AttributeController(this,
                AttributeController.PARTIAL);

        // NOTE: Use an ordinary ActorController rather than
        // ClassDefinitionController because access is only PARTIAL.
        _classDefinitionController = new ClassDefinitionController(this,
                AttributeController.PARTIAL);

        try {

            StringParameter actorInteractionAddonParameter;
            actorInteractionAddonParameter = (StringParameter) this
                    .getConfiguration().getAttribute("_actorInteractionAddon",
                            Parameter.class);

            if (actorInteractionAddonParameter != null) {
                String actorInteractionAddonClassName = actorInteractionAddonParameter
                        .stringValue();
                Class actorInteractionAddonClass = Class
                        .forName(actorInteractionAddonClassName);

                ActorInteractionAddon actorInteractionAddon = (ActorInteractionAddon) actorInteractionAddonClass
                        .newInstance();

                _addonActorController = actorInteractionAddon
                        .getControllerInstance(this, false);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        _entityController = new ActorInstanceController(this,
                AttributeController.PARTIAL);
        _entityPortController = new IOPortController(this,
                AttributeController.PARTIAL);
        _relationController = new RelationController(this);
        _linkController = new LinkController(this);
    }

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionModel(getSelectionModel());

        // If the selectionDragger is consuming, then popup menus don't
        // disappear properly.
        _selectionDragger.setConsuming(false);

        super.initializeInteraction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The attribute controller. */
    protected NamedObjController _attributeController;

    /** The class definition controller. */
    protected ActorController _classDefinitionController;

    /** The controller for actors with addon gui behavior. */
    protected ActorController _addonActorController;

    /** The entity controller. */
    protected ActorController _entityController;

    /** The entity port controller. */
    protected NamedObjController _entityPortController;

    /** The link controller. */
    protected LinkController _linkController;

    /** The relation controller. */
    protected NamedObjController _relationController;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The default controller, used only for instances of Locatable
     *  that have no container.
     */
    private LocatableNodeController _locatableController;

    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;
}
