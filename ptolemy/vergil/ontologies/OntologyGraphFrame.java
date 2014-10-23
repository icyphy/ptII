/* A graph editor frame for ontology models.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.gui.Tableau;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.basic.BasicGraphPane;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.graph.GraphPane;

/** A graph editor frame for ontology models. Given a composite entity and
 *  a tableau, it creates an editor and populates the menus and toolbar. This
 *  overrides the base class to associate with the editor an instance of
 *  OntologyGraphController.
 *
 *  @author Charles Shelton, Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
@SuppressWarnings("serial")
public class OntologyGraphFrame extends ExtendedGraphFrame implements
ActionListener {

    /** Construct a frame associated with the specified ontology model. After
     *  constructing this, it is necessary to call setVisible(true) to make the
     *  frame appear. This is typically done by calling show() on the controlling
     *  tableau. This constructor results in a graph frame that obtains its
     *  library either from the model (if it has one) or the default library
     *  defined in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public OntologyGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified ontology model. After
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
    public OntologyGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // FIXME: Help file for ontology editor doesn't exist yet.
        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilOntologyEditorHelp.htm";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the actions specific to this ontology graph frame.
     *  There are no actions implemented for the ontology graph frame so this
     *  method does nothing.
     *  @param e The action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this ontology graph frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        _graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
        _menubar.add(_graphMenu);
        _addLayoutMenu(_graphMenu);

        // Add any commands to graph menu and toolbar that the controller
        // wants in the graph menu and toolbar.
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        JMenuItem[] debugMenuItems = _debugMenuItems();

        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);

        ActionListener debugMenuListener = _getDebugMenuListener();

        // Set the action command and listener for each menu item in the
        // debug menu.
        for (JMenuItem debugMenuItem : debugMenuItems) {
            debugMenuItem.setActionCommand(debugMenuItem.getText());
            debugMenuItem.addActionListener(debugMenuListener);
            _debugMenu.add(debugMenuItem);
        }

        _menubar.add(_debugMenu);
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class BasicGraphFrame, so it must be careful to
     *  not reference local variables that may not have yet been created.
     *  @param entity The ontology model entity to be shown in the graph pane.
     *  @return The pane that is created.
     */
    @Override
    protected GraphPane _createGraphPane(NamedObj entity) {
        // Initialize the ontology graph controller before it is passed
        // to the BasicGraphPane constructor.
        _createController();

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.
        final OntologyGraphModel graphModel = new OntologyGraphModel(
                (CompositeEntity) entity);
        return new BasicGraphPane(_controller, graphModel, entity);
    }

    /** Create the array of menu items for the debug menu.
     *  @return The array of debug menu items.
     */
    protected JMenuItem[] _debugMenuItems() {
        // Add debug menu.
        JMenuItem[] debugMenuItems = {
                new JMenuItem(CHECK_LATTICE, KeyEvent.VK_D),
                new JMenuItem(CLEAR_LATTICE_ERRORS) };
        return debugMenuItems;
    }

    /** Create the listener for the debug menu actions.
     *  @return The debug menu action listener.
     */
    protected ActionListener _getDebugMenuListener() {
        DebugMenuListener debugMenuListener = new DebugMenuListener();
        return debugMenuListener;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The controller for the ontology editor frame. */
    protected OntologyGraphController _controller;

    /** Debug menu for the ontology editor frame. */
    protected JMenu _debugMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the ontology graph controller for the editor frame.
     */
    private void _createController() {
        _controller = new OntologyGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The check lattice debug menu action string label. */
    private static final String CHECK_LATTICE = "Check Concept Lattice";

    /** The clear lattice errors debug menu action string label. */
    private static final String CLEAR_LATTICE_ERRORS = "Clear Lattice Errors";

    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////

    ///////////////////////////////////////////////////////////////////
    //// DebugMenuListener

    /** The listener class for debug menu commands. */
    public class DebugMenuListener implements ActionListener {

        /** React to a menu command in the debug menu.
         *  @param e The event received from the menu command.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            if (actionCommand.equals(CHECK_LATTICE)) {
                Ontology ontologyModel = (Ontology) getModel();
                ReportOntologyLatticeStatus
                .showStatusAndHighlightCounterExample(ontologyModel,
                        _controller);
            } else if (actionCommand.equals(CLEAR_LATTICE_ERRORS)) {
                _controller.clearAllErrorHighlights();
            }
        }
    }

}
