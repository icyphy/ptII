/* The graph controller for the vergil viewer

 Copyright (c) 1999-2014 The Regents of the University of California.
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

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingConstants;

import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.Vertex;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.LocatableNodeController;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.RelationController;
import ptolemy.vergil.toolbox.PortSite;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.EdgeController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.NodeController;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.AbstractGlobalLayout;

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
    @Override
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

                while (objToHighlight != null
                        && objToHighlight.getContainer() != toplevel) {
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

                        if (type == FiringEvent.BEFORE_ITERATE
                                || type == FiringEvent.BEFORE_FIRE) {
                            _animationRenderer.renderSelected(figure);
                            _animated = figure;

                            long animationDelay = getAnimationDelay();

                            if (animationDelay > 0) {
                                try {
                                    Thread.sleep(animationDelay);
                                } catch (InterruptedException ex) {
                                }
                            }
                        } else if (type == FiringEvent.AFTER_ITERATE
                                || type == FiringEvent.AFTER_POSTFIRE) {
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
    @Override
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
    @Override
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
                    // The Vergil Applet at $PTII/ptolemy/vergil/Vergil.htm might not have a configuration?
                    Configuration configuration = getConfiguration();
                    if (configuration != null) {
                        StringParameter actorInteractionAddonParameter;
                        actorInteractionAddonParameter = (StringParameter) configuration
                                .getAttribute("_actorInteractionAddon",
                                        Parameter.class);

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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // In the viewer, there will not be a class definition
                // controller that is distinct from the entity controller.
                // In the edit, there will be.
                if (_classDefinitionController != null
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
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _attributeController.setConfiguration(configuration);
        _classDefinitionController.setConfiguration(configuration);
        _entityController.setConfiguration(configuration);
        _entityPortController.setConfiguration(configuration);
        _relationController.setConfiguration(configuration);
        _linkController.setConfiguration(configuration);

        try {
            // The Vergil Applet at $PTII/ptolemy/vergil/Vergil.htm might not have a configuration?
            if (configuration != null) {
                StringParameter actorInteractionAddon;
                actorInteractionAddon = (StringParameter) configuration
                        .getAttribute("_actorInteractionAddon", Parameter.class);

                if (actorInteractionAddon != null) {
                    _addonActorController.setConfiguration(configuration);
                }
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
    @Override
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);
        _entityController.addHotKeys(jgraph);
        _classDefinitionController.addHotKeys(jgraph);
        _attributeController.addHotKeys(jgraph);

        try {
            // The Vergil Applet at $PTII/ptolemy/vergil/Vergil.htm might not have a configuration?
            Configuration configuration = getConfiguration();
            if (configuration != null) {
                StringParameter actorInteractionAddon;
                actorInteractionAddon = (StringParameter) configuration
                        .getAttribute("_actorInteractionAddon", Parameter.class);

                if (actorInteractionAddon != null) {
                    _addonActorController.addHotKeys(jgraph);
                }
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
    @Override
    protected void _createControllers() {
        super._createControllers();
        _attributeController = new AttributeController(this,
                AttributeController.PARTIAL);

        // NOTE: Use an ordinary ActorController rather than
        // ClassDefinitionController because access is only PARTIAL.
        _classDefinitionController = new ClassDefinitionController(this,
                AttributeController.PARTIAL);

        try {

            // The Vergil Applet at $PTII/ptolemy/vergil/Vergil.htm might not have a configuration?
            Configuration configuration = getConfiguration();
            if (configuration != null) {
                StringParameter actorInteractionAddonParameter;
                actorInteractionAddonParameter = (StringParameter) configuration
                        .getAttribute("_actorInteractionAddon", Parameter.class);

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
    @Override
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

    /** The selection interactor for drag-selecting nodes. */
    private SelectionDragger _selectionDragger;

    /** Font for port labels. */
    private static Font _portLabelFont = new Font("SansSerif", Font.PLAIN, 8);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// EntityLayout

    /**
     * This layout algorithm is responsible for laying out the ports within an
     * entity.
     */
    public class EntityLayout extends AbstractGlobalLayout {
        /** Create a new layout manager. */
        public EntityLayout() {
            super(new BasicLayoutTarget(ActorViewerGraphController.this));
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /**
         * Layout the ports of the specified node.
         *
         * @param node
         *            The node, which is assumed to be an entity.
         */
        @Override
        public void layout(Object node) {
            GraphModel model = ActorViewerGraphController.this.getGraphModel();

            // System.out.println("layout = " + node);
            // new Exception().printStackTrace();
            Iterator nodes = model.nodes(node);
            Vector westPorts = new Vector();
            Vector eastPorts = new Vector();
            Vector southPorts = new Vector();
            Vector northPorts = new Vector();

            while (nodes.hasNext()) {
                Port port = (Port) nodes.next();
                // Skip the port if it is hidden.
                if (_isHidden(port)) {
                    continue;
                }
                int portRotation = IOPortController.getCardinality(port);
                int direction = IOPortController.getDirection(portRotation);
                if (direction == SwingConstants.WEST) {
                    westPorts.add(port);
                } else if (direction == SwingConstants.NORTH) {
                    northPorts.add(port);
                } else if (direction == SwingConstants.EAST) {
                    eastPorts.add(port);
                } else {
                    southPorts.add(port);
                }
            }

            CompositeFigure figure = (CompositeFigure) getLayoutTarget()
                    .getVisualObject(node);

            _reOrderPorts(westPorts);
            _placePortFigures(figure, westPorts, SwingConstants.WEST);
            _reOrderPorts(eastPorts);
            _placePortFigures(figure, eastPorts, SwingConstants.EAST);
            _reOrderPorts(southPorts);
            _placePortFigures(figure, southPorts, SwingConstants.SOUTH);
            _reOrderPorts(northPorts);
            _placePortFigures(figure, northPorts, SwingConstants.NORTH);
        }

        ///////////////////////////////////////////////////////////////
        ////                     private methods                   ////

        private LabelFigure _createPortLabelFigure(String string, Font font,
                double x, double y, int direction) {
            LabelFigure label;

            if (direction == SwingConstants.SOUTH) {
                // The 1.0 argument is the padding.
                label = new LabelFigure(string, font, 1.0,
                        SwingConstants.SOUTH_WEST);

                // Shift the label down so it doesn't
                // collide with ports.
                label.translateTo(x, y + 5);

                // Rotate the label.
                AffineTransform rotate = AffineTransform.getRotateInstance(
                        Math.PI / 2.0, x, y + 5);
                label.transform(rotate);
            } else if (direction == SwingConstants.EAST) {
                // The 1.0 argument is the padding.
                label = new LabelFigure(string, font, 1.0,
                        SwingConstants.SOUTH_WEST);

                // Shift the label right so it doesn't
                // collide with ports.
                label.translateTo(x + 5, y);
            } else if (direction == SwingConstants.WEST) {
                // The 1.0 argument is the padding.
                label = new LabelFigure(string, font, 1.0,
                        SwingConstants.SOUTH_EAST);

                // Shift the label left so it doesn't
                // collide with ports.
                label.translateTo(x - 5, y);
            } else { // Must be north.

                // The 1.0 argument is the padding.
                label = new LabelFigure(string, font, 1.0,
                        SwingConstants.SOUTH_WEST);

                // Shift the label right so it doesn't
                // collide with ports. It will probably
                // collide with the actor name.
                label.translateTo(x, y - 5);

                // Rotate the label.
                AffineTransform rotate = AffineTransform.getRotateInstance(
                        -Math.PI / 2.0, x, y - 5);
                label.transform(rotate);
            }

            return label;
        }

        /** Return true if a property named "_hide" is set for
         *  the specified object. A property is specified if the specified
         *  object contains an attribute with the specified name and that
         *  attribute is either not a boolean-valued parameter, or it is
         *  a boolean-valued parameter with value true.
         *  @param object The object.
         *  @return True if the property is set.
         */
        private boolean _isHidden(NamedObj object) {
            Attribute attribute = object.getAttribute("_hide");
            if (attribute == null) {
                return false;
            }
            if (attribute instanceof Parameter) {
                try {
                    Token token = ((Parameter) attribute).getToken();

                    if (token instanceof BooleanToken) {
                        if (!((BooleanToken) token).booleanValue()) {
                            return false;
                        }
                    }
                } catch (IllegalActionException e) {
                    // Ignore, using default of true.
                }
            }
            return true;
        }

        // re-order the ports according to _ordinal property
        private void _reOrderPorts(Vector ports) {
            int size = ports.size();
            Enumeration enumeration = ports.elements();
            Port port;
            StringAttribute ordinal = null;
            int number = 0;
            int index = 0;

            while (enumeration.hasMoreElements()) {
                port = (Port) enumeration.nextElement();
                ordinal = (StringAttribute) port.getAttribute("_ordinal");

                if (ordinal != null) {
                    number = Integer.parseInt(ordinal.getExpression());

                    if (number >= size) {
                        ports.remove(index);

                        try {
                            ordinal.setExpression(Integer.toString(size - 1));
                        } catch (Exception e) {
                            MessageHandler.error(
                                    "Error setting ordinal property", e);
                        }

                        ports.add(port);
                    } else if (number < 0) {
                        ports.remove(index);

                        try {
                            ordinal.setExpression(Integer.toString(0));
                        } catch (Exception e) {
                            MessageHandler.error(
                                    "Error setting ordinal property", e);
                        }

                        ports.add(0, port);
                    } else if (number != index) {
                        ports.remove(index);
                        ports.add(number, port);
                    }
                }

                index++;
            }
        }

        // Place the ports.
        private void _placePortFigures(CompositeFigure figure, List portList,
                int direction) {
            Iterator ports = portList.iterator();
            int number = 0;
            // Don't count ports that are hidden and not connected. (Sven Koehler)
            // "Make hidden, unconnected ports not be rendered on the
            // canvas and therefore would not displace other ports on
            // an actor."
            // This is used by Kepler.
            int count = 0;
            for (Object p : portList) {
                Port port = (Port) p;
                Attribute portHide = port.getAttribute("_hide");
                try {
                    if (!(portHide != null
                            && portHide instanceof Variable
                            && ((Variable) portHide).getToken().equals(
                                    BooleanToken.TRUE) && port
                                    .linkedRelationList().isEmpty())) {
                        count++;
                    }
                } catch (IllegalActionException ex) {
                    count = portList.size();
                }
            }
            Figure background = figure.getBackgroundFigure();

            if (background == null) {
                // This could occur if the icon has a _hide parameter.
                background = figure;
            }

            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                Figure portFigure = ActorViewerGraphController.this
                        .getFigure(port);
                // If there is no figure, then ignore this port. This may
                // happen if the port hasn't been rendered yet.
                if (portFigure == null) {
                    continue;
                }

                Attribute portHide = port.getAttribute("_hide");
                // Skip ports that are hidden and not connected (Sven Koehler)
                try {
                    if (portHide != null
                            && portHide instanceof Variable
                            && ((Variable) portHide).getToken().equals(
                                    BooleanToken.TRUE)
                                    && port.linkedRelationList().isEmpty()) {
                        continue;
                    }
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex);
                }

                Rectangle2D portBounds = portFigure.getShape().getBounds2D();
                PortSite site = new PortSite(background, port, number, count,
                        direction);
                number++;

                // NOTE: previous expression for port location was:
                // 100.0 * number / (count+1)
                // But this leads to squished ports with uneven spacing.
                // Note that we don't use CanvasUtilities.translateTo because
                // we want to only get the bounds of the background of the
                // port figure.
                double x = site.getX() - portBounds.getCenterX();
                double y = site.getY() - portBounds.getCenterY();
                portFigure.translate(x, y);

                // If the actor contains a variable named "_showRate",
                // with value true, then visualize the rate information.
                // NOTE: Showing rates only makes sense for IOPorts.
                Attribute showRateAttribute = port.getAttribute("_showRate");

                if (port instanceof IOPort
                        && showRateAttribute instanceof Variable) {
                    boolean showRate = false;

                    try {
                        showRate = ((Variable) showRateAttribute).getToken()
                                .equals(BooleanToken.TRUE);
                    } catch (Exception ex) {
                        // Ignore.
                        showRate = false;
                    }

                    if (showRate) {
                        // Infer the rate. See DFUtilities.
                        String rateString = "";
                        Variable rateParameter = null;

                        if (((IOPort) port).isInput()) {
                            rateParameter = (Variable) port
                                    .getAttribute("tokenConsumptionRate");

                            if (rateParameter == null) {
                                String altName = "_tokenConsumptionRate";
                                rateParameter = (Variable) port
                                        .getAttribute(altName);
                            }
                        } else if (((IOPort) port).isOutput()) {
                            rateParameter = (Variable) port
                                    .getAttribute("tokenProductionRate");

                            if (rateParameter == null) {
                                String altName = "_tokenProductionRate";
                                rateParameter = (Variable) port
                                        .getAttribute(altName);
                            }
                        }

                        if (rateParameter != null) {
                            try {
                                rateString = rateParameter.getToken()
                                        .toString();
                            } catch (KernelException ex) {
                                // Ignore.
                            }
                        }

                        LabelFigure labelFigure = _createPortLabelFigure(
                                rateString, _portLabelFont, x, y, direction);
                        labelFigure.setFillPaint(Color.BLUE);
                        figure.add(labelFigure);
                    }
                }

                // If the port contains an attribute named "_showName",
                // then render the name of the port as well. If the
                // attribute is a boolean-valued parameter, then
                // show the name only if the value is true.
                Attribute showAttribute = port.getAttribute("_showName");
                String toShow = null;
                if (showAttribute != null) {
                    boolean show = true;

                    if (showAttribute instanceof Parameter) {
                        try {
                            Token token = ((Parameter) showAttribute)
                                    .getToken();

                            if (token instanceof BooleanToken) {
                                show = ((BooleanToken) token).booleanValue();
                            }
                        } catch (IllegalActionException e) {
                            // Ignore. Presence of the attribute will prevail.
                        }
                    }

                    if (show) {
                        toShow = port.getDisplayName();
                    }
                }
                // In addition, if the port contains an attribute
                // called "_showInfo", then if that attribute is
                // a variable, then its value is shown. Otherwise,
                // if it is a Settable, then its expression is shown.
                Attribute showInfo = port.getAttribute("_showInfo");
                try {
                    if (showInfo instanceof Variable
                            && !((Variable) showInfo).isStringMode()) {
                        String value = ((Variable) showInfo).getToken()
                                .toString();
                        if (toShow != null && !value.trim().equals("")) {
                            toShow += " (" + value + ")";
                        } else {
                            toShow = value;
                        }
                    } else if (showInfo instanceof Settable) {
                        String value = ((Settable) showInfo).getExpression();
                        if (toShow != null && !value.trim().equals("")) {
                            toShow += " (" + value + ")";
                        } else {
                            toShow = ((Settable) showInfo).getExpression();
                        }
                    }
                } catch (IllegalActionException e) {
                    if (toShow == null) {
                        toShow = e.getMessage();
                    } else {
                        toShow += e.getMessage();
                    }
                }

                // Finally, if the port is an IOPort and it has a defaultValue,
                // show that value. If there is already text to show, the insert
                // a colon before the value.
                if (port instanceof IOPort) {
                    try {
                        Token defaultValue = ((IOPort) port).defaultValue
                                .getToken();
                        if (defaultValue != null) {
                            if (toShow == null) {
                                toShow = defaultValue.toString();
                            } else {
                                toShow += ": " + defaultValue.toString();
                            }
                        }
                    } catch (IllegalActionException e) {
                        if (toShow == null) {
                            toShow = e.getMessage();
                        } else {
                            toShow += e.getMessage();
                        }
                    }
                }

                if (toShow != null) {
                    LabelFigure labelFigure = _createPortLabelFigure(toShow,
                            _portLabelFont, x, y, direction);
                    figure.add(labelFigure);
                }
            }
        }
    }
}
