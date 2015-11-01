/* A graph editor frame for ontology solver models.

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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ptolemy.actor.gui.Tableau;
import ptolemy.data.ontologies.Ontology;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphFrame.InstantiateAttributeAction;
import ptolemy.vergil.actor.ActorGraphFrame.InstantiateEntityAction;
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
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
@SuppressWarnings("serial")
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
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /** Dispose of this frame.
     *  Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method invokes the
     *  dispose() method of the superclass,
     *  {@link ptolemy.vergil.basic.ExtendedGraphFrame}.
     */
    @Override
    public void dispose() {
        if (_debugClosing) {
            System.out.println("OntologySolverGraphFrame.dispose() : "
                    + this.getName());
        }

        KeyStroke[] keyStroke = _rightComponent.getRegisteredKeyStrokes();
        int count = keyStroke.length;
        for (int i = 0; i < count; i++) {
            KeyStroke ks = keyStroke[i];
            _rightComponent.unregisterKeyboardAction(ks);
        }
        _insertOntologyAction = null;
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
        _instantiateAttributeAction = new InstantiateAttributeAction(this,
                "ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute");
        _instantiateEntityAction = new InstantiateEntityAction(this,
                "ptolemy.data.ontologies.Ontology");
    }

    /** Create the menus that are used by this frame. It is essential that
     *  _createGraphPane() be called before this.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        _graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
        _menubar.add(_graphMenu);
        _addLayoutMenu(_graphMenu);

        GUIUtilities.addMenuItem(_graphMenu, _instantiateAttributeAction);
        GUIUtilities.addHotKey(_getRightComponent(),
                _instantiateAttributeAction);
        GUIUtilities.addMenuItem(_graphMenu, _instantiateEntityAction);
        GUIUtilities.addHotKey(_getRightComponent(), _instantiateEntityAction);

        // Create the ontology menu.
        _ontologyMenu = new JMenu("Ontology");
        _ontologyMenu.setMnemonic(KeyEvent.VK_O);
        _menubar.add(_ontologyMenu);
        GUIUtilities.addHotKey(_getRightComponent(), _insertOntologyAction);
        GUIUtilities.addMenuItem(_ontologyMenu, _insertOntologyAction);
        GUIUtilities.addToolBarButton(_toolbar, _insertOntologyAction);

        // Add debug menu.
        JMenuItem[] debugMenuItems = { new JMenuItem(
                "Check Monotonicity of Concept Functions") };

        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);

        _debugMenuListener = new DebugMenuListener();

        // Set the action command and listener for each menu item.
        for (JMenuItem debugMenuItem : debugMenuItems) {
            debugMenuItem.setActionCommand(debugMenuItem.getText());
            debugMenuItem.addActionListener(_debugMenuListener);
            _debugMenu.add(debugMenuItem);
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
    @Override
    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new OntologySolverGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final OntologySolverGraphModel graphModel = new OntologySolverGraphModel(
                (CompositeEntity) entity);
        return new BasicGraphPane(_controller, graphModel, entity);
    }

    ///////////////////////////////////////////////////////////////////
    ///                    protected variables                     ////

    /** The graph controller. This is created in _createGraphPane(). */
    protected OntologySolverGraphController _controller;

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    /** The ontology menu. */
    protected JMenu _ontologyMenu;

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
    public static class DebugMenuListener implements ActionListener {

        /** React to a menu command.
         *  @param e The event that is received to be reacted to.
         */
        @Override
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
    //// InsertOntologyAction

    /** An action to insert an ontology into the ontology solver model. */
    private class InsertOntologyAction extends AbstractAction {

        /** Create a new action to instantiate an entity. */
        public InsertOntologyAction() {
            super("Insert Ontology");
            putValue("tooltip", "Insert an Ontology into the Ontology Solver");

            // We only need to instantiate this IconController in order to
            // render the figure for the insert ontology button on the toolbar
            IconController toolbarIconController = new IconController(
                    _controller);
            NodeRenderer toolbarRenderer = toolbarIconController.new IconRenderer();
            Figure toolbarFigure = toolbarRenderer.render(_prototypeOntology);

            // Standard toolbar icons are 25x25 pixels.
            FigureIcon toolbarIcon = new FigureIcon(toolbarFigure, 25, 25, 1,
                    false);
            putValue(diva.gui.GUIUtilities.LARGE_ICON, toolbarIcon);
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_I, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));

            // Initialize the default ontology directory to null in the
            // constructor because the current directory may be changed later
            // before this action is executed for the first time.
            _ontologyDirectory = null;
        }

        /** Instantiate a class by first opening a dialog to get a class name and
         *  then issuing a change request.
         *  @param e The event that is received to be reacted to.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // Initialize the default ontology directory to the current directory.
            if (_ontologyDirectory == null) {
                _ontologyDirectory = _directory;
            }

            Query query = new Query();
            query.setTextWidth(60);
            query.addFileChooser("ontologyFilename", "Ontology Model Filename",
                    "", null, _ontologyDirectory);

            ComponentDialog dialog = new ComponentDialog(
                    OntologySolverGraphFrame.this, "Insert Ontology", query);

            if (dialog.buttonPressed().equals("OK")) {
                // Get the associated Ptolemy model.
                GraphController controller = getJGraph().getGraphPane()
                        .getGraphController();
                AbstractBasicGraphModel model = (AbstractBasicGraphModel) controller
                        .getGraphModel();
                NamedObj context = model.getPtolemyModel();

                String ontologyFileName = query
                        .getStringValue("ontologyFilename");
                File ontologyFile = new File(ontologyFileName);

                // Update the ontology directory to the most recent directory visited in the dialog.
                _ontologyDirectory = new File(ontologyFile.getParent());

                String sourceURL = "";
                try {
                    sourceURL = ontologyFile.toURI().toURL().toString();
                } catch (MalformedURLException ex) {
                    report("Error getting ontology model file URL: ", ex);
                }
                String source = " source=\"" + sourceURL + "\"";

                String ontologyName = ontologyFile.getName();
                ontologyName = ontologyName.substring(0,
                        ontologyName.indexOf("."));

                // Use the center of the screen as a location.
                Rectangle2D bounds = getVisibleCanvasRectangle();
                double x = bounds.getWidth() / 2.0;
                double y = bounds.getHeight() / 2.0;

                // Use the "auto" namespace group so that name collisions
                // are automatically avoided by appending a suffix to the name.
                String moml = "<group name=\"auto\"><entity name=\""
                        + ontologyName + "\" class=\"" + ontologyName + "\""
                        + source + "><property name=\"_location\" "
                        + "class=\"ptolemy.kernel.util.Location\" value=\"" + x
                        + ", " + y + "\"></property></entity></group>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        context, moml);
                context.requestChange(request);
            }
        }

        /** Holds the most recent directory visited in the file dialog when
         *  selecting an ontology file.
         */
        private File _ontologyDirectory;
    }
}
