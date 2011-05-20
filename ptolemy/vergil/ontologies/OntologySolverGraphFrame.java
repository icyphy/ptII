/* A graph editor frame for ontology solver models.

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
package ptolemy.vergil.ontologies;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.data.ontologies.Ontology;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphPane;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.basic.IconController;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.FigureIcon;

///////////////////////////////////////////////////////////////////
//// OntologySolverGraphFrame

/** This is a graph editor frame for ontology solver models. This class is
 *  largely adapted from {@link ptolemy.vergil.actor.ActorGraphFrame
 *  ActorGraphFrame}.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 2.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class OntologySolverGraphFrame extends ExtendedGraphFrame implements
        ActionListener {
    
    /** Construct a frame associated with the specified ontology solver model. After
     *  constructing this, it is necessary to call setVisible(true) to make the
     *  frame appear. This is typically done by calling show() on the controlling
     *  tableau. This constructor results in a graph frame that obtains its
     *  library either from the model (if it has one) or the default library
     *  defined in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public OntologySolverGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified ontology solver model. After
     *  constructing this, it is necessary to call setVisible(true) to make the
     *  frame appear. This is typically done by calling show() on the controlling
     *  tableau. This constructor results in a graph frame that obtains its
     *  library either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined in the
     *  configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library to use
     *   if the model does not have a library.
     */
    public OntologySolverGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
        _initOntologySolverGraphFrame();
    }

    /** React to the actions specific to this ontology solver graph frame.
     *  There are no actions implemented for the ontology solver graph frame so
     *  this method does nothing.
     *  @param e The action event.
     */
    public void actionPerformed(ActionEvent e) {
    }
    
    /** Dispose of this frame.
     *  Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method invokes the 
     *  dispose() method of the superclass,
     *  {@link ptolemy.vergil.basic.ExtendedGraphFrame}.
     */
    public void dispose() {
        if (_debugClosing) {
            System.out.println("OntologySolverGraphFrame.dispose() : " + this.getName());
        }

        KeyStroke[] keyStroke = _rightComponent.getRegisteredKeyStrokes();
        int count = keyStroke.length;
        for (int i = 0; i < count; i++) {
            KeyStroke ks = keyStroke[i];
            _rightComponent.unregisterKeyboardAction(ks);
        }
        _insertOntologyAction = null;
        _saveInLibraryAction = null;
        _importLibraryAction = null;
        _instantiateAttributeAction = null;
        _instantiateEntityAction = null;
        _layoutAction = null;
        _debugMenuListener = null;
        
        super.dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize this class. The help file is set, and various actions are
     *  instantiated.
     */
    protected void _initOntologySolverGraphFrame() {

        // FIXME: Help file for ontology solver editor doesn't exist yet.
        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilOntologySolverEditorHelp.htm";

        _insertOntologyAction = new InsertOntologyAction();
        _layoutAction = new LayoutAction();
        
        _saveInLibraryAction = new SaveInLibraryAction();
        _importLibraryAction = new ImportLibraryAction();
        _instantiateAttributeAction = new InstantiateAttributeAction();
        _instantiateEntityAction = new InstantiateEntityAction();
    }

    /** Create the menus that are used by this frame. It is essential that
     *  _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        _graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
        _menubar.add(_graphMenu);
        GUIUtilities.addHotKey(_getRightComponent(), _layoutAction);
        GUIUtilities.addMenuItem(_graphMenu, _layoutAction);
        _graphMenu.addSeparator();

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
        
        // Create the ontology menu.
        _ontologyMenu = new JMenu("Ontology");
        _ontologyMenu.setMnemonic(KeyEvent.VK_O);
        _menubar.add(_ontologyMenu);
        GUIUtilities.addHotKey(_getRightComponent(), _insertOntologyAction);
        GUIUtilities.addMenuItem(_ontologyMenu, _insertOntologyAction);
        GUIUtilities.addToolBarButton(_toolbar, _insertOntologyAction);

        // Add debug menu.
        JMenuItem[] debugMenuItems = {
                new JMenuItem("Check Monotonicity of Concept Functions") };

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
        
        // Even though the OntologySolverGraphController adds no menu items
        // or toolbar buttons, this call is necessary to add the hot key
        // for the ontology controller look inside action.
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        _menubar.add(_debugMenu);
    }

    /** Create a new graph pane. Note that this method is called in constructor
     *  of the base class, so it must be careful to not reference local variables
     *  that may not have yet been created.
     * 
     *  @param entity The object to be displayed in the pane.
     *  @return The pane that is created.
     */
    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new OntologySolverGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final OntologySolverGraphModel graphModel =
            new OntologySolverGraphModel((CompositeEntity) entity);
        return new BasicGraphPane(_controller, graphModel, entity);
    }

    ///////////////////////////////////////////////////////////////////
    ///                    protected variables                     ////

    /** The graph controller. This is created in _createGraphPane(). */
    protected OntologySolverGraphController _controller;

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    /** The graph menu. */
    protected JMenu _graphMenu;
    
    /** The ontology menu. */
    protected JMenu _ontologyMenu;

    /** The action for automatically laying out the graph. */
    protected Action _layoutAction;

    /** The action for saving the current model in a library. */
    protected Action _saveInLibraryAction;

    /** The action for importing a library of components. */
    protected Action _importLibraryAction;
    
    /** The action for inserting an ontology into the ontology solver model. */
    protected Action _insertOntologyAction;

    /** The action for instantiating an attribute. */
    protected Action _instantiateAttributeAction;

    /** The action for instantiating an entity. */
    protected Action _instantiateEntityAction;
    
    /** Listener for debug menu commands. */
    protected DebugMenuListener _debugMenuListener;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The most recent class name for instantiating an attribute. */
    private String _lastAttributeClassName =
        "ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute";

    /** The most recent class name for instantiating an entity. */
    private String _lastEntityClassName = "ptolemy.data.ontologies.Ontology";

    /** The most recent location for instantiating a class. */
    private String _lastLocation = "";
    
    /** Prototype ontology for rendering. */
    private static Location _prototypeOntology;
    
    static {
        CompositeEntity container = new CompositeEntity();

        try {
            Ontology ontology = new Ontology(container, "");
            _prototypeOntology = new Location(ontology, "_location");
        } catch (KernelException ex) {
            // This should not happen.
            throw new InternalErrorException(null, ex, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public inner classes                 ////

    // NOTE: The following class is very similar to the inner class
    // in FSMGraphFrame. Is there some way to merge these?
    // There seem to be enough differences that this may be hard.

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {
        
        /** React to a menu command.
         *  @param e The event that is received to be reacted to.
         */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();
            
            if (actionCommand.equals("Check Monotonicity of Concept Functions")) {                    
                MessageHandler.message("This function is not implemented yet.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private inner classes                    ////

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

        /** Import a library by first opening a file chooser dialog and then
         *  importing the specified library.
         *  @param e The event that is received to be reacted to.
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

            int result = chooser.showOpenDialog(OntologySolverGraphFrame.this);

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
    //// InstantiateOntologyAction

    /** An action to insert an ontology into the ontology solver model. */
    private class InsertOntologyAction extends AbstractAction {
        
        /** Create a new action to instantiate an entity. */
        public InsertOntologyAction() {
            super("Insert Ontology");
            putValue("tooltip", "Insert an Ontology into the Ontology Solver");
            
            NodeRenderer renderer = new IconController.IconRenderer(_getGraphModel());
            Figure figure = renderer.render(_prototypeOntology);

            // Standard toolbar icons are 25x25 pixels.
            FigureIcon icon = new FigureIcon(figure, 25, 25, 1, false);
            putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_I, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        }

        /** Instantiate a class by first opening a dialog to get a class name and
         *  then issuing a change request.
         *  @param e The event that is received to be reacted to.
         */
        public void actionPerformed(ActionEvent e) {            
            Query query = new Query();
            query.setTextWidth(60);
            query.addFileChooser("ontologyFilename", "Ontology Model Filename",
                    "", null, _directory);

            ComponentDialog dialog = new ComponentDialog(OntologySolverGraphFrame.this,
                    "Insert Ontology", query);

            if (dialog.buttonPressed().equals("OK")) {
                // Get the associated Ptolemy model.
                GraphController controller = getJGraph().getGraphPane()
                        .getGraphController();
                AbstractBasicGraphModel model = (AbstractBasicGraphModel) controller
                        .getGraphModel();
                NamedObj context = model.getPtolemyModel();

                String ontologyFileName = query.getStringValue("ontologyFilename");
                File ontologyFile = new File(ontologyFileName);
                
                // Update most recent directory object.
                _directory = new File(ontologyFile.getParent());
                
                String sourceURL = "";
                try {
                    sourceURL = ontologyFile.toURI().toURL().toString();
                } catch (MalformedURLException ex) {
                    report("Error getting ontology model file URL: ", ex);
                }
                String source = " source=\"" + sourceURL + "\"";
                
                String ontologyName = ontologyFile.getName();
                ontologyName = ontologyName.substring(0, ontologyName.indexOf("."));

                // Use the center of the screen as a location.
                Rectangle2D bounds = getVisibleCanvasRectangle();
                double x = bounds.getWidth() / 2.0;
                double y = bounds.getHeight() / 2.0;

                // Use the "auto" namespace group so that name collisions
                // are automatically avoided by appending a suffix to the name.
                String moml = "<group name=\"auto\"><entity name=\"" + ontologyName
                        + "\" class=\"" + ontologyName + "\"" + source
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
    //// InstantiateAttributeAction

    /** An action to instantiate an attribute given a class name. */
    private class InstantiateAttributeAction extends AbstractAction {
        /** Create a new action to instantiate an entity. */
        public InstantiateAttributeAction() {
            super("Instantiate Attribute");
            putValue("tooltip", "Instantiate an attribute by class name");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        }

        /** Instantiate a class by first opening a dialog to get a class name and
         *  then issuing a change request.
         *  @param e The event that is received to be reacted to.
         */
        public void actionPerformed(ActionEvent e) {
            Query query = new Query();
            query.setTextWidth(60);
            query.addLine("class", "Class name", _lastAttributeClassName);

            ComponentDialog dialog = new ComponentDialog(OntologySolverGraphFrame.this,
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

        /** Instantiate a class by first opening a dialog to get a class name and
         *  then issuing a change request.
         *  @param e The event that is received to be reacted to.
         */
        public void actionPerformed(ActionEvent e) {
            Query query = new Query();
            query.setTextWidth(60);
            query.addLine("class", "Class name", _lastEntityClassName);
            query.addLine("location", "Location (URL)", _lastLocation);

            ComponentDialog dialog = new ComponentDialog(OntologySolverGraphFrame.this,
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

        /** Lay out the graph.
         *  @param e The event that is received to be reacted to.
         */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraph();
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

        /** Create a new instance of the current model in the actor library of
         *  the configuration.
         *  @param e The event that is received to be reacted to.
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
