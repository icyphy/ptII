/* The graph controller for FSM models.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.modal;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import ptolemy.actor.gui.Configuration;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.modal.modal.ModalTransitionController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.unit.ConfigureUnitsAction;
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
import diva.graph.GraphException;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.FigureIcon;

///////////////////////////////////////////////////////////////////
//// FSMGraphController

/**
 A Graph Controller for FSM models.  This controller allows states to be
 dragged and dropped onto its graph. Links can be created by
 control-clicking and dragging from one state to another.

 @author Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class FSMGraphController extends FSMViewerGraphController {
    /** Create a new basic controller with default
     *  terminal and edge interactors.
     */
    public FSMGraphController() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  In this class, commands are added to create
     *  ports and relations.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    @Override
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);

        // Only include the port actions if there is an actor library.
        // The ptinyViewer configuration uses this.
        if (getConfiguration().getEntity("actor library") != null) {
            diva.gui.GUIUtilities.addMenuItem(menu, _newInputPortAction);
            diva.gui.GUIUtilities
            .addToolBarButton(toolbar, _newInputPortAction);
            diva.gui.GUIUtilities.addMenuItem(menu, _newOutputPortAction);
            diva.gui.GUIUtilities.addToolBarButton(toolbar,
                    _newOutputPortAction);
            diva.gui.GUIUtilities.addMenuItem(menu, _newInOutPortAction);
            diva.gui.GUIUtilities
            .addToolBarButton(toolbar, _newInOutPortAction);
            diva.gui.GUIUtilities.addMenuItem(menu, _newInputMultiportAction);
            diva.gui.GUIUtilities.addToolBarButton(toolbar,
                    _newInputMultiportAction);
            diva.gui.GUIUtilities.addMenuItem(menu, _newOutputMultiportAction);
            diva.gui.GUIUtilities.addToolBarButton(toolbar,
                    _newOutputMultiportAction);
            diva.gui.GUIUtilities.addMenuItem(menu, _newInOutMultiportAction);
            diva.gui.GUIUtilities.addToolBarButton(toolbar,
                    _newInOutMultiportAction);

            // Add an item that adds new states.
            menu.addSeparator();
            diva.gui.GUIUtilities.addMenuItem(menu, _newStateAction);
            diva.gui.GUIUtilities.addToolBarButton(toolbar, _newStateAction);
        }
    }

    /** Set the configuration.  The configuration is used when
     *  opening documentation files.
     *  @param configuration The configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);

        if (_portDialogAction != null) {
            _portDialogAction.setConfiguration(configuration);
        }

        if (_configureUnitsAction != null) {
            _configureUnitsAction.setConfiguration(configuration);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  In this class, controllers with FULL access are created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    @Override
    protected void _createControllers() {
        _attributeController = new AttributeController(this,
                AttributeController.FULL);
        _portController = new ExternalIOPortController(this,
                AttributeController.FULL);
        _stateController = new StateController(this, AttributeController.FULL);
        _modalTransitionController = new ModalTransitionController(this);
        _transitionController = new TransitionController(this);
    }

    /** Initialize interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    @Override
    protected void initializeInteraction() {
        // NOTE: This method name does not have a leading underscore
        // because it is a diva method.
        super.initializeInteraction();

        /* GraphPane pane = */getGraphPane();

        // Add a menu command to configure the ports.
        _portDialogAction = new PortDialogAction("Ports");
        _portDialogAction.setConfiguration(getConfiguration());

        _configureMenuFactory.addAction(_portDialogAction, "Customize");
        _configureUnitsAction = new ConfigureUnitsAction("Units Constraints");
        _configureMenuFactory.addAction(_configureUnitsAction, "Customize");
        _configureUnitsAction.setConfiguration(getConfiguration());

        // Create the interactor that drags new edges.
        _linkCreator = new LinkCreator();
        _linkCreator.setMouseFilter(_shortcutFilter);

        // NOTE: Do not use _initializeInteraction() because we are
        // still in the constructor, and that method is overloaded in
        // derived classes.
        ((CompositeInteractor) _stateController.getNodeInteractor())
        .addInteractor(_linkCreator);
    }

    /** Initialize interactions for the specified controller.  This
     *  method is called when a new controller is constructed. In this
     *  class, this method attaches a link creator to the controller
     *  if the controller is an instance of StateController.
     *  @param controller The controller for which to initialize interaction.
     */
    @Override
    protected void _initializeInteraction(NamedObjController controller) {
        super._initializeInteraction(controller);

        if (controller instanceof StateController) {
            Interactor interactor = controller.getNodeInteractor();

            if (interactor instanceof CompositeInteractor) {
                ((CompositeInteractor) interactor).addInteractor(_linkCreator);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ConfigureUnitsAction _configureUnitsAction;

    /** The interactor that interactively creates edges. */
    private LinkCreator _linkCreator; // For control-click

    /** The action for creating states. */
    private NewStateAction _newStateAction = new NewStateAction();

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

    /** Action for creating a new input port. */
    private Action _newInputPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INPUT, "New input port",
            KeyEvent.VK_I, new String[][] {
                    { "/ptolemy/vergil/actor/img/single_in.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/actor/img/single_in_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/actor/img/single_in_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/actor/img/single_in_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new output port. */
    private Action _newOutputPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_OUTPUT, "New output port",
            KeyEvent.VK_O, new String[][] {
                    { "/ptolemy/vergil/actor/img/single_out.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/actor/img/single_out_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/actor/img/single_out_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/actor/img/single_out_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new input/output port. */
    private Action _newInOutPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INOUT, "New input/output port",
            KeyEvent.VK_P, new String[][] {
                    { "/ptolemy/vergil/actor/img/single_inout.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/actor/img/single_inout_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/actor/img/single_inout_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/actor/img/single_inout_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new input multiport. */
    private Action _newInputMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INPUT_MULTIPORT,
            "New input multiport", KeyEvent.VK_N, new String[][] {
                    { "/ptolemy/vergil/actor/img/multi_in.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/actor/img/multi_in_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/actor/img/multi_in_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/actor/img/multi_in_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new output multiport. */
    private Action _newOutputMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_OUTPUT_MULTIPORT,
            "New output multiport", KeyEvent.VK_U, new String[][] {
                    { "/ptolemy/vergil/actor/img/multi_out.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/actor/img/multi_out_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/actor/img/multi_out_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/actor/img/multi_out_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new inout multiport. */
    private Action _newInOutMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INOUT_MULTIPORT,
            "New input/output multiport", KeyEvent.VK_T, new String[][] {
                    { "/ptolemy/vergil/actor/img/multi_inout.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/actor/img/multi_inout_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/actor/img/multi_inout_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/actor/img/multi_inout_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

    /** The port dialog factory. */
    private PortDialogAction _portDialogAction;

    /** Prototype state for rendering. */
    private static Location _prototypeState;

    static {
        CompositeEntity container = new CompositeEntity();

        try {
            State state = new State(container, "S");
            _prototypeState = new Location(state, "_location");
        } catch (KernelException ex) {
            // This should not happen.
            throw new InternalErrorException(null, ex, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// LinkCreator

    /** An interactor that interactively drags edges from one terminal
     *  to another.
     */
    protected class LinkCreator extends AbstractInteractor {
        /** Initiate creation of an arc. */
        @Override
        public void mousePressed(LayerEvent event) {
            Figure source = event.getFigureSource();
            NamedObj sourceObject = (NamedObj) source.getUserObject();

            Link link = new Link();

            // Set the tail, going through the model so the link is added
            // to the list of links.
            FSMGraphModel model = (FSMGraphModel) getGraphModel();
            model.getArcModel().setTail(link, sourceObject);

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
                Connector c = getEdgeController(link).render(link, layer,
                        tailSite, headSite);

                // get the actual attach site.
                tailSite = getEdgeController(link).getConnectorTarget()
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
                Figure ef = getFigure(link);
                getSelectionModel().addSelection(ef);

                ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
                GrabHandle gh = cm.getHeadHandle();
                layer.grabPointer(event, gh);
            } catch (Exception ex) {
                MessageHandler.error("Drag connection failed:", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// NewStateAction

    /** An action to create a new state. */
    @SuppressWarnings("serial")
    public class NewStateAction extends FigureAction {
        /** Construct a new state. */
        public NewStateAction() {
            super("New State");
            putValue("tooltip", "New State");

            NodeRenderer renderer = new StateController.StateRenderer(
                    getGraphModel());
            Figure figure = renderer.render(_prototypeState);

            // Standard toolbar icons are 25x25 pixels.
            FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
            putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            putValue("tooltip", "New State");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_W));
        }

        /** Execute the action. */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            if (getSourceType() == TOOLBAR_TYPE
                    || getSourceType() == MENUBAR_TYPE) {
                // No location in the action, so put it in the middle.
                BasicGraphFrame frame = FSMGraphController.this.getFrame();
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

            FSMGraphModel graphModel = (FSMGraphModel) getGraphModel();
            NamedObj toplevel = graphModel.getPtolemyModel();

            String stateName = toplevel.uniqueName("state");

            // Create the state.
            String moml = null;
            String locationName = "_location";

            // Try to get the class name for the state from the library,
            // so that the library and the toolbar are assured of creating
            // the same object.
            try {
                LibraryAttribute attribute = (LibraryAttribute) toplevel
                        .getAttribute("_library", LibraryAttribute.class);

                if (attribute != null) {
                    CompositeEntity library = attribute.getLibrary();
                    Entity prototype = library.getEntity("state");

                    if (prototype != null) {
                        moml = prototype.exportMoML(stateName);

                        // FIXME: Get location name from prototype.
                    }
                }
            } catch (Exception ex) {
                // Ignore and use the default.
                // Avoid a FindBugs warning about ignored exception.
                moml = null;
            }

            if (moml == null) {
                moml = "<entity name=\"" + stateName
                        + "\" class=\"ptolemy.domains.modal.kernel.State\">\n"
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
}
