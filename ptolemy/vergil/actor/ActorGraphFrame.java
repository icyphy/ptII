/* A graph editor frame for Ptolemy models.

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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphPane;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ActorGraphFrame

/**
 * This is a graph editor frame for ptolemy models. Given a composite entity and
 * an instance of ActorGraphTableau, it creates an editor and populates the
 * menus and toolbar. This overrides the base class to associate with the editor
 * an instance of ActorEditorGraphController.
 * 
 * @see ActorEditorGraphController
 * @author Steve Neuendorffer, Contributor: Edward A. Lee, KIELER Layout: Christian Motika <cmot@informatik.uni-kiel.de>
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Red (neuendor)
 * @Pt.AcceptedRating Red (johnr)
 */
public class ActorGraphFrame extends ExtendedGraphFrame implements
        ActionListener {
    /**
     * Construct a frame associated with the specified Ptolemy II model. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one) or the default library
     * defined in the configuration.
     * 
     * @see Tableau#show()
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     */
    public ActorGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /**
     * Construct a frame associated with the specified Ptolemy II model. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one), or the
     * <i>defaultLibrary</i> argument (if it is non-null), or the default
     * library defined in the configuration.
     * 
     * @see Tableau#show()
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     * @param defaultLibrary An attribute specifying the default library to use
     *            if the model does not have a library.
     */
    public ActorGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
        _initActorGraphFrame();
    }

    /**
     * React to the actions specific to this actor graph frame.
     * 
     * @param e The action event.
     */
    public void actionPerformed(ActionEvent e) {
        JMenuItem target = (JMenuItem) e.getSource();
        String actionCommand = target.getActionCommand();
        if (actionCommand.equals("Import Design Pattern")) {
            importDesignPattern();
        } else if (actionCommand.equals("Export Design Pattern")) {
            exportDesignPattern();
        }
    }
    
    /** Dispose of this frame.
     *     Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method invokes the 
     *  dispose() method of the superclass,
     *  {@link ptolemy.vergil.basic.ExtendedGraphFrame}.
     */
    public void dispose() {
        KeyStroke[] keyStroke = _rightComponent.getRegisteredKeyStrokes();
        int count = keyStroke.length;
        for (int i = 0; i < count; i++) {
            KeyStroke ks = keyStroke[i];
            _rightComponent.unregisterKeyboardAction(ks);
        }
        _saveInLibraryAction = null;
        _importLibraryAction = null;
        _instantiateAttributeAction = null;
        _instantiateEntityAction = null;
        _layoutAction = null;
        _advancedLayoutDialogAction = null;
        _createHierarchyAction = null;
        _controller.setFrame(null);
        _debugMenuListener = null;
        super.dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Initialize this class. In this base class, the help file is set, and
     * various actions are instantiated.
     */
    protected void _initActorGraphFrame() {

        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilGraphEditorHelp.htm";

        _createHierarchyAction = new CreateHierarchyAction();
        _layoutAction = new LayoutAction();
        _advancedLayoutDialogAction = new AdvancedLayoutDialogAction();
        // Only include the various actions if there is an actor library
        // The ptinyViewer configuration uses this.
        if (getConfiguration().getEntity("actor library") != null) {
            _saveInLibraryAction = new SaveInLibraryAction();
            _importLibraryAction = new ImportLibraryAction();
            _instantiateAttributeAction = new InstantiateAttributeAction();
            _instantiateEntityAction = new InstantiateEntityAction();
        }

    }

    /**
     * Create the menus that are used by this frame. It is essential that
     * _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        _graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
        _menubar.add(_graphMenu);
        GUIUtilities.addHotKey(_getRightComponent(), _layoutAction);
        GUIUtilities.addMenuItem(_graphMenu, _layoutAction);
        GUIUtilities.addMenuItem(_graphMenu, _advancedLayoutDialogAction);
        _graphMenu.addSeparator();
        // Only include the various actions if there is an actor library
        // The ptinyViewer configuration uses this.
        if (getConfiguration().getEntity("actor library") != null) {
            GUIUtilities.addHotKey(_getRightComponent(), _saveInLibraryAction);
            GUIUtilities.addMenuItem(_graphMenu, _saveInLibraryAction);
            GUIUtilities.addHotKey(_getRightComponent(), _importLibraryAction);
            GUIUtilities.addMenuItem(_graphMenu, _importLibraryAction);
            GUIUtilities.addMenuItem(_graphMenu, _instantiateAttributeAction);
            GUIUtilities.addHotKey(_getRightComponent(),
                    _instantiateAttributeAction);
            GUIUtilities.addMenuItem(_graphMenu, _instantiateEntityAction);
            GUIUtilities.addHotKey(_getRightComponent(),
                    _instantiateEntityAction);
            _graphMenu.addSeparator();
            diva.gui.GUIUtilities.addHotKey(_getRightComponent(),
                    _createHierarchyAction);
            diva.gui.GUIUtilities.addMenuItem(_graphMenu,
                    _createHierarchyAction);
        }
        // Add any commands to graph menu and toolbar that the controller
        // wants in the graph menu and toolbar.
        _graphMenu.addSeparator();
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        // Add debug menu.
        // Generate .smv file newly added by Patrick
        JMenuItem[] debugMenuItems = {
                new JMenuItem("Listen to Director", KeyEvent.VK_L),
                new JMenuItem("Animate Execution", KeyEvent.VK_A),
                new JMenuItem("Stop Animating", KeyEvent.VK_S), };

        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);

        _debugMenuListener = new DebugMenuListener();

        // Set the action command and listener for each menu item.
        for (int i = 0; i < debugMenuItems.length; i++) {
            debugMenuItems[i].setActionCommand(debugMenuItems[i].getText());
            debugMenuItems[i].addActionListener(_debugMenuListener);
            _debugMenu.add(debugMenuItems[i]);
        }

        _menubar.add(_debugMenu);
    }

    /**
     * If the ptolemy model associated with this frame is a top-level composite
     * actor, use its manager to stop it. Remove the listeners that this frame
     * registered with the ptolemy model. Also remove the listeners our graph
     * model has created.
     * 
     * @return True if the close completes, and false otherwise.
     */
    protected boolean _close() {
        NamedObj ptModel = getModel();

        if (ptModel instanceof CompositeActor
                && (ptModel.getContainer() == null)) {
            CompositeActor ptActorModel = (CompositeActor) ptModel;
            Manager manager = ptActorModel.getManager();

            if (manager != null) {
                manager.stop();
            }
        }

        return super._close();
    }

    /**
     * Create the items in the File menu. A null element in the array represents
     * a separator in the menu.
     * 
     * @return The items in the File menu.
     */
    protected JMenuItem[] _createFileMenuItems() {
        JMenuItem[] fileMenuItems = super._createFileMenuItems();
        int i = 0;
        for (JMenuItem item : fileMenuItems) {
            i++;
            // Only include the various actions if there is an actor library
            // The ptinyViewer configuration uses this.
            Configuration configuration = getConfiguration();
            if ((configuration != null && configuration
                    .getEntity("actor library") != null)
                    && item.getActionCommand().equals("Save As")) {
                // Add a SaveAsDesignPattern here.
                JMenuItem importItem = new JMenuItem("Import Design Pattern",
                        KeyEvent.VK_D);
                JMenuItem exportItem = new JMenuItem("Export Design Pattern",
                        KeyEvent.VK_D);
                JMenuItem[] newItems = new JMenuItem[fileMenuItems.length + 4];
                System.arraycopy(fileMenuItems, 0, newItems, 0, i);
                newItems[i + 1] = importItem;
                importItem.addActionListener(this);
                newItems[i + 2] = exportItem;
                exportItem.addActionListener(this);
                System.arraycopy(fileMenuItems, i, newItems, i + 4,
                        fileMenuItems.length - i);
                return newItems;
            }
        }
        return fileMenuItems;
    }

    /**
     * Create a new graph pane. Note that this method is called in constructor
     * of the base class, so it must be careful to not reference local variables
     * that may not have yet been created.
     * 
     * @param entity The object to be displayed in the pane.
     * @return The pane that is created.
     */
    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new ActorEditorGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new BasicGraphPane(_controller, graphModel, entity);
    }

    ///////////////////////////////////////////////////////////////////
    ///                    protected variables                     ////

    /** The graph controller. This is created in _createGraphPane(). */
    protected ActorEditorGraphController _controller;

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    /** The graph menu. */
    protected JMenu _graphMenu;

    /** The action for creating a level of hierarchy. */
    protected Action _createHierarchyAction;

    /** The action for automatically laying out the graph. */
    protected Action _layoutAction;

    /** The action for opening the advanced layout dialog. */
    protected Action _advancedLayoutDialogAction;

    /** The action for saving the current model in a library. */
    protected Action _saveInLibraryAction;

    /** The action for importing a library of components. */
    protected Action _importLibraryAction;

    /** The action for instantiating an attribute. */
    protected Action _instantiateAttributeAction;

    /** The action for instantiating an entity. */
    protected Action _instantiateEntityAction;
    
    /** Listener for debug menu commands. */
    protected DebugMenuListener _debugMenuListener;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The most recent class name for instantiating an attribute. */
    private String _lastAttributeClassName = "ptolemy.vergil.kernel.attributes.EllipseAttribute";

    /** The most recent class name for instantiating an entity. */
    private String _lastEntityClassName = "ptolemy.actor.lib.Ramp";

    /** The most recent location for instantiating a class. */
    private String _lastLocation = "";

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;

    ///////////////////////////////////////////////////////////////////
    ////                      public inner classes                 ////

    // NOTE: The following class is very similar to the inner class
    // in FSMGraphFrame. Is there some way to merge these?
    // There seem to be enough differences that this may be hard.

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {
        /** React to a menu command. */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            try {
                if (actionCommand.equals("Listen to Director")) {
                    NamedObj model = getModel();
                    boolean success = false;

                    if (model instanceof Actor) {
                        Director director = ((Actor) model).getDirector();

                        if (director != null) {
                            Effigy effigy = (Effigy) getTableau()
                                    .getContainer();

                            // Create a new text effigy inside this one.
                            Effigy textEffigy = new TextEffigy(effigy,
                                    effigy.uniqueName("debug listener"));
                            DebugListenerTableau tableau = new DebugListenerTableau(
                                    textEffigy,
                                    textEffigy.uniqueName("debugListener"));
                            tableau.setDebuggable(director);
                            success = true;
                        }
                    }

                    if (!success) {
                        MessageHandler.error("No director to listen to!");
                    }
                } else if (actionCommand.equals("Animate Execution")) {
                    // To support animation, add a listener to the
                    // first director found above in the hierarchy.
                    // NOTE: This doesn't properly support all
                    // hierarchy. Insides of transparent composite
                    // actors do not get animated if they are classes
                    // rather than instances.
                    NamedObj model = getModel();

                    if (model instanceof Actor) {
                        // Dialog to ask for a delay time.
                        Query query = new Query();
                        query.addLine("delay",
                                "Time (in ms) to hold highlight",
                                Long.toString(_lastDelayTime));

                        ComponentDialog dialog = new ComponentDialog(
                                ActorGraphFrame.this, "Delay for Animation",
                                query);

                        if (dialog.buttonPressed().equals("OK")) {
                            try {
                                _lastDelayTime = Long.parseLong(query
                                        .getStringValue("delay"));
                                _controller.setAnimationDelay(_lastDelayTime);

                                Director director = ((Actor) model)
                                        .getDirector();

                                while ((director == null)
                                        && model instanceof Actor) {
                                    model = model.getContainer();

                                    if (model instanceof Actor) {
                                        director = ((Actor) model)
                                                .getDirector();
                                    }
                                }

                                if ((director != null)
                                        && (_listeningTo != director)) {
                                    if (_listeningTo != null) {
                                        _listeningTo
                                                .removeDebugListener(_controller);
                                    }

                                    director.addDebugListener(_controller);
                                    _listeningTo = director;
                                } else {
                                    MessageHandler
                                            .error("Cannot find the director. "
                                                    + "Possibly this is because this "
                                                    + "is a class, not an instance.");
                                }

                            } catch (NumberFormatException ex) {
                                MessageHandler.error(
                                        "Invalid time, which is required "
                                                + "to be an integer", ex);
                            }
                        }
                    } else {
                        MessageHandler
                                .error("Model is not an actor. Cannot animate.");
                    }
                } else if (actionCommand.equals("Stop Animating")) {
                    if (_listeningTo != null) {
                        _listeningTo.removeDebugListener(_controller);
                        _controller.clearAnimation();
                        _listeningTo = null;
                    }
                }
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning("Failed to create debug listener: "
                            + ex);
                } catch (CancelException exception) {
                }
            }
        }

        private Director _listeningTo;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// CreateHierarchy

    /**
     * Action to create a typed composite actor that contains the the selected
     * actors.
     */
    private class CreateHierarchyAction extends AbstractAction {
        /**
         * Create a new action to introduce a level of hierarchy.
         */
        public CreateHierarchyAction() {
            super("Create Hierarchy");
            putValue("tooltip",
                    "Create a TypedCompositeActor that contains the"
                            + " selected actors.");

            // putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
            // KeyStroke.getKeyStroke(KeyEvent.VK_H,
            // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            // putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
            // Integer.valueOf(KeyEvent.VK_H));
        }

        public void actionPerformed(ActionEvent e) {
            createHierarchy();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ImportLibraryAction

    /** An action to import a library of components. */
    private class ImportLibraryAction extends AbstractAction {
        /** Create a new action to import a library of components. */
        public ImportLibraryAction() {
            super("Import Library");
            putValue("tooltip", "Import a library into the Palette");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_M));
        }

        /**
         * Import a library by first opening a file chooser dialog and then
         * importing the specified library.
         */
        public void actionPerformed(ActionEvent e) {
            // NOTE: this code is mostly copied from Top.
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a library");

            if (_getDirectory() != null) {
                chooser.setCurrentDirectory(_getDirectory());
            } else {
                // The default on Windows is to open at user.home, which is
                // typically an absurd directory inside the O/S installation.
                // So we use the current directory instead.
                // FIXME: This will throw a security exception in an applet?
                String cwd = StringUtilities.getProperty("user.dir");

                if (cwd != null) {
                    chooser.setCurrentDirectory(new File(cwd));
                }
            }

            int result = chooser.showOpenDialog(ActorGraphFrame.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();

                    PtolemyEffigy effigy = (PtolemyEffigy) getTableau()
                            .getContainer();
                    Configuration configuration = (Configuration) effigy
                            .toplevel();
                    UserActorLibrary.openLibrary(configuration, file);

                    _setDirectory(chooser.getCurrentDirectory());
                } catch (Throwable throwable) {
                    MessageHandler.error("Library import failed.", throwable);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// InstantiateAttributeAction

    /** An action to instantiate an entity given a class name. */
    private class InstantiateAttributeAction extends AbstractAction {
        /** Create a new action to instantiate an entity. */
        public InstantiateAttributeAction() {
            super("Instantiate Attribute");
            putValue("tooltip", "Instantiate an attribute by class name");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        }

        /**
         * Instantiate a class by first opening a dialog to get a class name and
         * then issuing a change request.
         */
        public void actionPerformed(ActionEvent e) {
            Query query = new Query();
            query.setTextWidth(60);
            query.addLine("class", "Class name", _lastAttributeClassName);

            ComponentDialog dialog = new ComponentDialog(ActorGraphFrame.this,
                    "Instantiate Attribute", query);

            if (dialog.buttonPressed().equals("OK")) {
                // Get the associated Ptolemy model.
                GraphController controller = getJGraph().getGraphPane()
                        .getGraphController();
                AbstractBasicGraphModel model = (AbstractBasicGraphModel) controller
                        .getGraphModel();
                NamedObj context = model.getPtolemyModel();

                _lastAttributeClassName = query.getStringValue("class");

                // Find the root for the instance name.
                String rootName = _lastAttributeClassName;
                int period = rootName.lastIndexOf(".");

                if ((period >= 0) && (rootName.length() > (period + 1))) {
                    rootName = rootName.substring(period + 1);
                }

                // Use the center of the screen as a location.
                Rectangle2D bounds = getVisibleCanvasRectangle();
                double x = bounds.getWidth() / 2.0;
                double y = bounds.getHeight() / 2.0;

                // Use the "auto" namespace group so that name collisions
                // are automatically avoided by appending a suffix to the name.
                String moml = "<group name=\"auto\"><property name=\""
                        + rootName + "\" class=\"" + _lastAttributeClassName
                        + "\"><property name=\"_location\" "
                        + "class=\"ptolemy.kernel.util.Location\" value=\"" + x
                        + ", " + y + "\"></property></property></group>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        context, moml);
                context.requestChange(request);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// InstantiateEntityAction

    /** An action to instantiate an entity given a class name. */
    private class InstantiateEntityAction extends AbstractAction {
        /** Create a new action to instantiate an entity. */
        public InstantiateEntityAction() {
            super("Instantiate Entity");
            putValue("tooltip", "Instantiate an entity by class name");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
        }

        /**
         * Instantiate a class by first opening a dialog to get a class name and
         * then issuing a change request.
         */
        public void actionPerformed(ActionEvent e) {
            Query query = new Query();
            query.setTextWidth(60);
            query.addLine("class", "Class name", _lastEntityClassName);
            query.addLine("location", "Location (URL)", _lastLocation);

            ComponentDialog dialog = new ComponentDialog(ActorGraphFrame.this,
                    "Instantiate Entity", query);

            if (dialog.buttonPressed().equals("OK")) {
                // Get the associated Ptolemy model.
                GraphController controller = getJGraph().getGraphPane()
                        .getGraphController();
                AbstractBasicGraphModel model = (AbstractBasicGraphModel) controller
                        .getGraphModel();
                NamedObj context = model.getPtolemyModel();

                _lastEntityClassName = query.getStringValue("class");
                _lastLocation = query.getStringValue("location");

                // Find the root for the instance name.
                String rootName = _lastEntityClassName;
                int period = rootName.lastIndexOf(".");

                if ((period >= 0) && (rootName.length() > (period + 1))) {
                    rootName = rootName.substring(period + 1);
                }

                // Use the center of the screen as a location.
                Rectangle2D bounds = getVisibleCanvasRectangle();
                double x = bounds.getWidth() / 2.0;
                double y = bounds.getHeight() / 2.0;

                // If a location is given, construct MoML to
                // specify a "source".
                String source = "";

                if (!(_lastLocation.trim().equals(""))) {
                    source = " source=\"" + _lastLocation.trim() + "\"";
                }

                // Use the "auto" namespace group so that name collisions
                // are automatically avoided by appending a suffix to the name.
                String moml = "<group name=\"auto\"><entity name=\"" + rootName
                        + "\" class=\"" + _lastEntityClassName + "\"" + source
                        + "><property name=\"_location\" "
                        + "class=\"ptolemy.kernel.util.Location\" value=\"" + x
                        + ", " + y + "\"></property></entity></group>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        context, moml);
                context.requestChange(request);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// LayoutAction

    /** Action to automatically lay out the graph. */
    private class LayoutAction extends AbstractAction {
        /** Create a new action to automatically lay out the graph. */
        public LayoutAction() {
            super("Automatic Layout");
            putValue("tooltip", "Layout the Graph (Ctrl+T)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_T, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        /** Lay out the graph. */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraph();
            } catch (Exception ex) {
                MessageHandler.error("Layout failed", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// AdvancedLayoutDialogAction

    /** Action to automatically lay out the graph. */
    private class AdvancedLayoutDialogAction extends AbstractAction {
        /** Create a new action to automatically lay out the graph. */
        public AdvancedLayoutDialogAction() {
            super("Advanced Layout Dialog");
            putValue("tooltip", "Open the advanced layout dialog");
            // putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
            // KeyEvent.VK_T, Toolkit.getDefaultToolkit()
            // .getMenuShortcutKeyMask()));
            // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        /** Lay out the graph. */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraphDialog();
            } catch (Exception ex) {
                MessageHandler.error("Layout failed", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// SaveInLibraryAction

    /** An action to save the current model in a library. */
    private class SaveInLibraryAction extends AbstractAction {
        /** Create a new action to save a model in a library. */
        public SaveInLibraryAction() {
            super("Save In Library");
            putValue("tooltip", "Save as a Component in Library");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
        }

        /**
         * Create a new instance of the current model in the actor library of
         * the configuration.
         */
        public void actionPerformed(ActionEvent e) {
            PtolemyEffigy effigy = (PtolemyEffigy) getTableau().getContainer();
            NamedObj object = effigy.getModel();

            if (object == null) {
                return;
            }

            if (!(object instanceof Entity)) {
                throw new KernelRuntimeException("Could not save in "
                        + "library, '" + object + "' is not an Entity");
            }

            Entity entity = (Entity) object;
            Configuration configuration = (Configuration) effigy.toplevel();
            try {
                UserActorLibrary.saveComponentInLibrary(configuration, entity);
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
