/*  The node controller for entities

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
package ptolemy.vergil.actor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.OpenInstanceDialog;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.LookInsideAction;
import ptolemy.vergil.debugger.BreakpointDialogFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.toolbox.EditIconAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.RemoveIconAction;
import ptolemy.vergil.toolbox.RotateOrFlipPorts;
import ptolemy.vergil.unit.ConfigureUnitsAction;
import diva.graph.GraphController;
import diva.graph.JGraph;
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
    @Override
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _lookInsideAction);
        GUIUtilities.addHotKey(jgraph, _openInstanceAction);
    }

    /**
     * Set the configuration. This is used to open documentation files.
     *
     * @param configuration
     *            The configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);

        _lookInsideAction.setConfiguration(configuration);

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
    @Override
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
    protected LookInsideAction _lookInsideAction = new LookInsideAction(
            "Open Actor");

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

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ListenToActorAction

    /**
     * An action to listen to debug messages in the actor. This is static so
     * that other classes can use it.
     */
    @SuppressWarnings("serial")
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
        @Override
        public void actionPerformed(ActionEvent event) {
            if (_configuration == null && _tableauFrame == null) {
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
                Effigy textEffigy = new TextEffigy(effigy,
                        effigy.uniqueName("debugListener" + object.getName()));

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
    //// OpenInstanceAction

    /**
     * An action to open an instance. This is similar to LookInsideAction except
     * that it does not open the class definition, but rather opens the
     * instance.
     */
    @SuppressWarnings("serial")
    private class OpenInstanceAction extends FigureAction {
        public OpenInstanceAction() {
            super("Open Instance");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_L, java.awt.Event.ALT_MASK));
        }

        @Override
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
                    String actorInteractionAddonClassName = actorInteractionAddonParameter
                            .stringValue();

                    Class actorInteractionAddonClass = Class
                            .forName(actorInteractionAddonClassName);

                    ActorInteractionAddon actorInteractionAddon = (ActorInteractionAddon) actorInteractionAddonClass
                            .newInstance();

                    if (actorInteractionAddon
                            .isActorOfInterestForAddonController(object)) {
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
                        parent, _configuration,
                        ((TableauFrame) parent).getEffigy(),
                        OpenInstanceDialog.class, (Entity) object);

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
    @SuppressWarnings("serial")
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
        @Override
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
