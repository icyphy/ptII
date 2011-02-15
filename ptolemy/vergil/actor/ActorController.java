/*  The node controller for entities

 Copyright (c) 1998-2010 The Regents of the University of California.
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
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.OpenInstanceDialog;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.debugger.BreakpointDialogFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.toolbox.EditIconAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PortSite;
import ptolemy.vergil.toolbox.RemoveIconAction;
import ptolemy.vergil.toolbox.RotateOrFlipPorts;
import ptolemy.vergil.unit.ConfigureUnitsAction;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.JGraph;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.AbstractGlobalLayout;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ActorController

/**
 * This class provides interaction with nodes that represent Ptolemy II
 * entities. It provides a double click binding and context menu entry to edit
 * the parameters of the node ("Configure"), a command to get documentation, and
 * a command to open an actor. It can have one of two access levels, FULL or
 * PARTIAL. If the access level is FULL, the the context menu also contains a
 * command to rename the node and to configure its ports. In addition, a layout
 * algorithm is applied so that the figures for ports are automatically placed
 * on the sides of the figure for the entity.
 * <p>
 * NOTE: This class is abstract because it is missing the code for laying out
 * ports. Use the concrete subclasses ActorInstanceController or
 * ClassDefinitionController instead.
 *
 * @author Steve Neuendorffer and Edward A. Lee, Elaine Cheong, Contributor: Sven Koehler
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (johnr)
 * @see ActorInstanceController
 * @see ClassDefinitionController
 */
public abstract class ActorController extends AttributeController {
    /**
     * Create an entity controller associated with the specified graph
     * controller with full access.
     *
     * @param controller
     *            The associated graph controller.
     */
    public ActorController(GraphController controller) {
        this(controller, FULL);
    }

    /**
     * Create an entity controller associated with the specified graph
     * controller.
     *
     * @param controller
     *            The associated graph controller.
     * @param access
     *            The access level.
     */
    public ActorController(GraphController controller, Access access) {
        super(controller, access);

        _access = access;

        // "Configure Ports"
        if (access == FULL) {
            // Add to the context menu, configure submenu.
            _portDialogAction = new PortDialogAction("Ports");
            _configureMenuFactory.addAction(_portDialogAction, "Customize");
            _configureUnitsAction = new ConfigureUnitsAction(
                    "Units Constraints");
            _configureMenuFactory.addAction(_configureUnitsAction, "Customize");
        }

        // NOTE: The following requires that the configuration be
        // non-null, or it will report an error.  However, in order to
        // get the "Look Inside" menu to work for composite actors in
        // Kepler, we create these menu items now.
        _menuFactory
                .addMenuItemFactory(new MenuActionFactory(_lookInsideAction));
        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                _openInstanceAction));

        if (_configuration != null) {
            if (access == FULL) {
                // Create an Appearance submenu.
                _createAppearanceSubmenu();
            }
        }

        /*
         * The following proves not so useful since atomic actors do not
         * typically have suitable constructors (that take only a Workspace
         * argument) to be usable at the top level.
         * _menuFactory.addMenuItemFactory( new MenuActionFactory(new
         * SaveInFileAction()));
         */
        // if (((BasicGraphController) getController()).getFrame() != null) {
        // If we are in an applet, then we have no frame, so no need
        // for a "Listen to Actor" or "Save in Library" menu choices.
        // FIXME: this is not perfect, it would be better if we
        // could just test if we are in an applet or else fix this
        // so we have a frame.
        // NOTE: This requires that the configuration be non null, or it
        // will report an error.
        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                new SaveInLibraryAction()));

        // }
        // "Set Breakpoints"
        if (access == FULL) {
            // Add to the context menu.
            // FIXME: does this work outside of SDF? Should
            // we check to see if the director is an SDF director?
            // We should use reflection to check this so that
            // this class does not require SDFDirector.
            // See $PTII/doc/coding/debugging.htm
            _breakpointDialogFactory = new BreakpointDialogFactory(
                    (BasicGraphController) getController());
            _menuFactory.addMenuItemFactory(_breakpointDialogFactory);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * If access is FULL, then add the jni.ArgumentDailogFactory() to
     * _menuFactory. If access is not FULL, then do nothing.
     *
     * @param menuItemFactory
     *            The MenuItemFactory to be added.
     */
    public void addMenuItemFactory(MenuItemFactory menuItemFactory) {
        // This method is called by jni.ThalesGraphFrame to add a context
        // menu.
        if (_access == FULL) {
            _menuFactory.addMenuItemFactory(menuItemFactory);
        }
    }

    /**
     * Add hot keys to the actions in the given JGraph. It would be better that
     * this method was added higher in the hierarchy. Now most controllers
     *
     * @param jgraph
     *            The JGraph to which hot keys are to be added.
     */
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _lookInsideAction);
    }

    /**
     * Set the configuration. This is used to open documentation files.
     *
     * @param configuration
     *            The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);

        if (_portDialogAction != null) {
            _portDialogAction.setConfiguration(configuration);
        }
        if (_configureUnitsAction != null) {
            _configureUnitsAction.setConfiguration(configuration);
        }

        if (_configuration != null) {
            if (_access == FULL) {
                // Create an Appearance submenu.
                _createAppearanceSubmenu();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the class label of the component.
     *
     * @return the class label of the component.
     */
    protected String _getComponentType() {
        return "Actor";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The access level defined in the constructor. */
    protected Access _access;

    /** The action that handles edit custom icon. */
    protected EditIconAction _editIconAction = new EditIconAction();

    /** An action that handles flipping the ports horizontally. */
    protected RotateOrFlipPorts _flipPortsHorizontal = new RotateOrFlipPorts(
            RotateOrFlipPorts.FLIP_HORIZONTAL, "Flip Ports Horizontally");

    /** An action that handles flipping the ports vertically. */
    protected RotateOrFlipPorts _flipPortsVertical = new RotateOrFlipPorts(
            RotateOrFlipPorts.FLIP_VERTICAL, "Flip Ports Vertically");

    /**
     * The action that handles opening an actor. This is accessed by by
     * ActorViewerController to create a hot key for the editor. The name
     * "lookInside" is historical and preserved to keep backward compatibility
     * with subclasses.
     */
    protected LookInsideAction _lookInsideAction = new LookInsideAction();

    /**
     * The action that handles opening an instance.
     */
    protected OpenInstanceAction _openInstanceAction = new OpenInstanceAction();

    /** The action that handles removing a custom icon. */
    protected RemoveIconAction _removeIconAction = new RemoveIconAction();

    /** An action that handles rotating the ports by 90 degrees. */
    protected RotateOrFlipPorts _rotatePortsClockwise = new RotateOrFlipPorts(
            RotateOrFlipPorts.CLOCKWISE, "Rotate Ports Clockwise");

    /** An action that handles rotating the ports by 90 degrees. */
    protected RotateOrFlipPorts _rotatePortsCounterclockwise = new RotateOrFlipPorts(
            RotateOrFlipPorts.COUNTERCLOCKWISE, "Rotate Ports Counterclockwise");

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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Create an Appearance submenu.
     */
    private void _createAppearanceSubmenu() {
        _editIconAction.setConfiguration(_configuration);
        _removeIconAction.setConfiguration(_configuration);
        Action[] actions = { _editIconAction, _removeIconAction,
                _flipPortsHorizontal, _flipPortsVertical,
                _rotatePortsClockwise, _rotatePortsCounterclockwise };
        _appearanceMenuActionFactory.addActions(actions, "Appearance");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private BreakpointDialogFactory _breakpointDialogFactory;

    private ConfigureUnitsAction _configureUnitsAction;

    private PortDialogAction _portDialogAction;

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
            super(new BasicLayoutTarget(getController()));
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /**
         * Layout the ports of the specified node.
         *
         * @param node
         *            The node, which is assumed to be an entity.
         */
        public void layout(Object node) {
            GraphModel model = getController().getGraphModel();

            // System.out.println("layout = " + node);
            // new Exception().printStackTrace();
            Iterator nodes = model.nodes(node);
            Vector westPorts = new Vector();
            Vector eastPorts = new Vector();
            Vector southPorts = new Vector();
            Vector northPorts = new Vector();

            while (nodes.hasNext()) {
                Port port = (Port) nodes.next();
                int portRotation = IOPortController._getCardinality(port);
                int direction = IOPortController._getDirection(portRotation);
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
            for (Object p : portList){
                Port port = (Port)p;
                Attribute portHide = port.getAttribute("_hide");
                try {
                    if ( ! (portHide != null
                                    && portHide instanceof Variable
                                    && ((Variable)portHide).getToken().equals(BooleanToken.TRUE)
                                    && port.linkedRelationList().isEmpty()) ) {
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
                Figure portFigure = getController().getFigure(port);
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
                            && ((Variable)portHide).getToken().equals(BooleanToken.TRUE)
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
                    toShow += e.getMessage();
                }

                if (toShow != null) {
                    LabelFigure labelFigure = _createPortLabelFigure(toShow,
                            _portLabelFont, x, y, direction);
                    figure.add(labelFigure);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ListenToActorAction

    /**
     * An action to listen to debug messages in the actor. This is static so
     * that other classes can use it.
     */
    public static class ListenToActorAction extends FigureAction {
        // Kepler uses this action.

        /** Create an action to listen to debug messages.
         *
         * @param tableauFrame The associated TableauFrame.
         */
        public ListenToActorAction(TableauFrame tableauFrame) {
            super("Listen to Actor");
            _tableauFrame = tableauFrame;
        }

        /** Create an action to listen to debug messages in the actor.
         *
         * @param controller The controller associated with this action.
         */
        public ListenToActorAction(BasicGraphController controller) {
            super("Listen to Actor");
            _controller = controller;
        }

        /** Create an action to listen to debug messages in the actor.
         *
         * @param target The actor to which to listen.
         * @param controller  The controller associated with this action.
         */
        public ListenToActorAction(NamedObj target,
                BasicGraphController controller) {
            super("Listen to Actor");
            _target = target;
            _controller = controller;
        }

        /** Perform the action.
         *  @param event The action event
         */
        public void actionPerformed(ActionEvent event) {
            if ((_configuration == null) && (_tableauFrame == null)) {
                MessageHandler
                        .error("Cannot listen to actor without a configuration.");
                return;
            }

            // Determine which entity was selected for the listen to
            // actor action.
            super.actionPerformed(event);

            NamedObj object = _target;

            if (object == null) {
                object = getTarget();
            }
            Tableau tableau = null;
            try {
                if (_tableauFrame == null) {
                    BasicGraphFrame frame = _controller.getFrame();
                    tableau = frame.getTableau();
                } else {
                    tableau = _tableauFrame.getTableau();
                }

                // effigy is the whole model.
                Effigy effigy = (Effigy) tableau.getContainer();

                // We want to open a new window that behaves as a
                // child of the model window. So, we create a new text
                // effigy inside this one. Specify model's effigy as
                // a container for this new effigy.
                Effigy textEffigy = new TextEffigy(effigy, effigy
                        .uniqueName("debugListener" + object.getName()));

                DebugListenerTableau debugTableau = new DebugListenerTableau(
                        textEffigy, textEffigy.uniqueName("debugListener"
                                + object.getName()));
                debugTableau.setDebuggable(object);
            } catch (KernelException ex) {
                MessageHandler.error("Failed to create debug listener.", ex);
            }
        }

        /**
         * Set the configuration for use by the help screen.
         *
         * @param configuration
         *            The configuration.
         */
        public void setConfiguration(Configuration configuration) {
            _configuration = configuration;
        }

        private Configuration _configuration;
        private BasicGraphController _controller;
        private NamedObj _target;
        private TableauFrame _tableauFrame = null;
    }

    ///////////////////////////////////////////////////////////////////
    //// LookInsideAction
    /**
     * An action to open a composite. This private class must remain named
     * LookInsideAction for backward compatibility.
     */
    private class LookInsideAction extends FigureAction {
        public LookInsideAction() {
            super("Open Actor");

            // Attach a key binding for look inside (also called
            // open actor).
            // If we are in an applet, so Control-L or Command-L will
            // be caught by the browser as "Open Location", so we don't
            // supply Control-L or Command-L as a shortcut under applets.
            if (!StringUtilities.inApplet()) {
                // For some inexplicable reason, the I key doesn't work here.
                // Use L, which used to be used for layout.
                // Avoid Control_O, which is open file.
                putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                                .getMenuShortcutKeyMask()));
            }
        }

        public void actionPerformed(ActionEvent event) {
            if (_configuration == null) {
                MessageHandler.error("Cannot open an actor "
                        + "without a configuration.");
                return;
            }

            // Determine which entity was selected for the open actor action.
            super.actionPerformed(event);

            NamedObj object = getTarget();

            try {
                StringParameter actorInteractionAddonParameter;
                actorInteractionAddonParameter = (StringParameter) _configuration
                    .getAttribute("_actorInteractionAddon", Parameter.class);

                if (actorInteractionAddonParameter != null) {
                    String actorInteractionAddonClassName = actorInteractionAddonParameter
                            .stringValue();
                    
                        Class actorInteractionAddonClass = Class
                            .forName(actorInteractionAddonClassName);
                        
                        ActorInteractionAddon actorInteractionAddon =
                            (ActorInteractionAddon) actorInteractionAddonClass
                            .newInstance();
                        
                        if(actorInteractionAddon.isActorOfInterestForAddonController(object)){
                            actorInteractionAddon.lookInsideAction(this, object);
                        }                    
                            
                    }

            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // NOTE: Used to open source code here if the object
            // was not a CompositeEntity. But this made it impossible
            // to associate a custom tableau with an atomic entity.
            // So now, the Configuration opens the source code as a
            // last resort.
            try {
                _configuration.openModel(object);
            } catch (Exception ex) {
                MessageHandler.error("Open actor failed.", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OpenInstanceAction

    /**
     * An action to open an instance. This is similar to LookInsideAction except
     * that it does not open the class definition, but rather opens the
     * instance.
     */
    private class OpenInstanceAction extends FigureAction {
        public OpenInstanceAction() {
            super("Open Instance");
        }

        public void actionPerformed(ActionEvent event) {
            if (_configuration == null) {
                MessageHandler.error("Cannot open an instance "
                        + "without a configuration.");
                return;
            }

            // Determine which entity was selected for the open actor action.
            super.actionPerformed(event);
            NamedObj object = getTarget();

            try {
                StringParameter actorInteractionAddonParameter;
                actorInteractionAddonParameter = (StringParameter) _configuration
                    .getAttribute("_actorInteractionAddon", Parameter.class);

                if (actorInteractionAddonParameter != null) {
                    String actorInteractionAddonClassName 
                        = actorInteractionAddonParameter
                            .stringValue();

                        Class actorInteractionAddonClass = Class
                            .forName(actorInteractionAddonClassName);
                        
                        ActorInteractionAddon actorInteractionAddon =
                            (ActorInteractionAddon) actorInteractionAddonClass
                            .newInstance();

                        if(actorInteractionAddon.isActorOfInterestForAddonController(object)){
                            actorInteractionAddon.openInstanceAction(this, object);
                        }                   
                            
                    }

            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (object instanceof CompositeEntity) {
                try {
                    _configuration.openInstance(object);
                } catch (Exception ex) {
                    MessageHandler.error("Open instance failed.", ex);
                }
            } else if (object instanceof Entity) {
                // If this is not a CompositeEntity, need to
                // do something different here as the method above will
                // open the source code as a last resort.
                Frame parent = getFrame();
                DialogTableau dialogTableau = DialogTableau.createDialog(
                        parent, _configuration, ((TableauFrame) parent)
                                .getEffigy(), OpenInstanceDialog.class,
                        (Entity) object);

                if (dialogTableau != null) {
                    dialogTableau.show();
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// SaveInFileAction

    /**
     * An action to save this actor in a file.
     */
    // private class SaveInFileAction extends FigureAction {
    // /** Create a new action to save a model in a file.
    // */
    // public SaveInFileAction() {
    // super("Save Actor In File");
    // putValue("tooltip", "Save actor in a file");
    // }
    //
    // /** Save the target object in a file.
    // * @param event The action event.
    // */
    // public void actionPerformed(ActionEvent event) {
    //// Find the target.
    // super.actionPerformed(event);
    //
    // NamedObj object = getTarget();
    //
    // if (object instanceof Entity) {
    // Entity entity = (Entity) object;
    //
    // BasicGraphController controller = (BasicGraphController) getController();
    // BasicGraphFrame frame = controller.getFrame();
    //
    // try {
    // frame.saveComponentInFile(entity);
    // } catch (Exception e) {
    // MessageHandler.error("Save failed.", e);
    // }
    // }
    // }
    // }

    ///////////////////////////////////////////////////////////////////
    //// SaveInLibraryAction
    /**
     * An action to save this actor in the library.
     */
    private class SaveInLibraryAction extends FigureAction {
        /**
         * Create a new action to save an actor in a library.
         */
        public SaveInLibraryAction() {
            super("Save Actor In Library");
            putValue("tooltip",
                    "Save the actor as a component in the user library");
        }

        /**
         * Create a new instance of the current model in the actor library of
         * the configuration.
         *
         * @param event
         *            The action event.
         */
        public void actionPerformed(ActionEvent event) {
            // Find the target.
            super.actionPerformed(event);

            NamedObj object = getTarget();

            if (object instanceof Entity) {
                Entity entity = (Entity) object;
                try {
                    UserActorLibrary.saveComponentInLibrary(_configuration,
                            entity);
                } catch (Exception ex) {
                    // We catch exceptions here because this method used to
                    // not throw Exceptions, and we don't want to break
                    // compatibility.
                    MessageHandler.error("Failed to save \"" + entity.getName()
                            + "\".");
                }
            }
        }
    }
}
