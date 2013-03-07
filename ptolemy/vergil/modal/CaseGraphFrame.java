/* A graph view for the case construct for Ptolemy models.

 Copyright (c) 2006-2013 The Regents of the University of California.
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.lib.hoc.Case;
import ptolemy.actor.lib.hoc.MultiCompositeActor;
import ptolemy.actor.lib.hoc.Refinement;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.toolbox.FigureAction;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// CaseGraphFrame

/**
 This is a graph editor frame for ptolemy case models.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class CaseGraphFrame extends ActorGraphFrame implements ChangeListener {
    /** Construct a frame associated with the specified case actor.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public CaseGraphFrame(Case entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified case actor.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public CaseGraphFrame(Case entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        _case = entity;
        _addCaseAction = new AddCaseAction();
        _removeCaseAction = new RemoveCaseAction();

        // Override the default help file.
        // FIXME
        // helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open the container, if any, of the entity.
     *  If this entity has no container, then do nothing.
     */
    public void openContainer() {
        // Method overridden since the parent will go from the refinement to
        // the case, which is where we were in the first place.
        if (_case != _case.toplevel()) {
            try {
                Configuration configuration = getConfiguration();
                // FIXME: do what with the return value?
                configuration.openInstance(_case.getContainer());
            } catch (Throwable throwable) {
                MessageHandler.error("Failed to open container", throwable);
            }
        }
    }

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source instanceof JTabbedPane) {
            Component selected = ((JTabbedPane) source).getSelectedComponent();
            if (selected instanceof JGraph) {
                setJGraph((JGraph) selected);
                selected.requestFocus();
            }
            if (_graphPanner != null) {
                _graphPanner.setCanvas((JGraph) selected);
                _graphPanner.repaint();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();
        _caseMenu = new JMenu("Case");
        _caseMenu.setMnemonic(KeyEvent.VK_C);
        _menubar.add(_caseMenu);
        GUIUtilities.addHotKey(_getRightComponent(), _addCaseAction);
        GUIUtilities.addMenuItem(_caseMenu, _addCaseAction);
        GUIUtilities.addHotKey(_getRightComponent(), _removeCaseAction);
        GUIUtilities.addMenuItem(_caseMenu, _removeCaseAction);
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     *  This overrides the base class to create a specialized
     *  graph controller (an inner class).
     *  @param entity The object to be displayed in the pane.
     *  @return The pane that is created.
     */
    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new CaseGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new GraphPane(_controller, graphModel);
    }

    /** Create the component that goes to the right of the library.
     *  NOTE: This is called in the base class constructor, before
     *  things have been initialized. Hence, it cannot reference
     *  local variables.
     *  @param entity The entity to display in the component.
     *  @return The component that goes to the right of the library.
     */
    protected JComponent _createRightComponent(NamedObj entity) {
        if (!(entity instanceof Case)) {
            return super._createRightComponent(entity);
        }
        _tabbedPane = new JTabbedPane();
        _tabbedPane.addChangeListener(this);
        Iterator<?> cases = ((Case) entity).entityList(Refinement.class)
                .iterator();
        boolean first = true;
        while (cases.hasNext()) {
            Refinement refinement = (Refinement) cases.next();
            JGraph jgraph = _addTabbedPane(refinement, false);
            // The first JGraph is the one with the focus.
            if (first) {
                first = false;
                setJGraph(jgraph);
            } else {
                ((CaseGraphController) _controller)._addHotKeys(jgraph);
            }
        }
        return _tabbedPane;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The case menu. */
    protected JMenu _caseMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add a tabbed pane for the specified case.
     *  @param refinement The case.
     *  @param newPane True to add the pane prior to the last pane.
     *  @return The pane.
     */
    private JGraph _addTabbedPane(Refinement refinement, boolean newPane) {
        GraphPane pane = _createGraphPane(refinement);
        pane.getForegroundLayer().setPickHalo(2);
        pane.getForegroundEventLayer().setConsuming(false);
        pane.getForegroundEventLayer().setEnabled(true);
        pane.getForegroundEventLayer().addLayerListener(new LayerAdapter() {
            /** Invoked when the mouse is pressed on a layer
             * or figure.
             */
            public void mousePressed(LayerEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }
        });
        JGraph jgraph = new JGraph(pane);
        String name = refinement.getName();
        jgraph.setName(name);
        int index = _tabbedPane.getComponentCount();
        // Put before the default pane, unless this is the default.
        if (newPane) {
            index--;
        }
        _tabbedPane.add(jgraph, index);
        jgraph.setBackground(BACKGROUND_COLOR);
        // Create a drop target for the jgraph.
        // FIXME: Should override _setDropIntoEnabled to modify all the drop targets created.
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The action to add a case. */
    private AddCaseAction _addCaseAction;

    /** The Case actor displayed by this frame. */
    private Case _case;

    /** The action to remove a case. */
    private RemoveCaseAction _removeCaseAction;

    /** The tabbed pane for cases. */
    private JTabbedPane _tabbedPane;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** Class implementing the Add Case menu command. */
    public class AddCaseAction extends FigureAction {

        /** Create a case action with label "Add Case". */
        public AddCaseAction() {
            super("Add Case");
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        }

        ///////////////////////////////////////////////////////////////////////////////
        ////                            public methods                             ////

        /** Perform the action. */
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            // Dialog to ask for a case name.
            Query query = new Query();
            query.addLine("case", "Pattern that the control input must match",
                    "");
            ComponentDialog dialog = new ComponentDialog(CaseGraphFrame.this,
                    "Add Case", query);
            if (dialog.buttonPressed().equals("OK")) {
                final String pattern = query.getStringValue("case");
                // NOTE: We do not use a TransitionRefinement because we don't
                // want the sibling input ports that come with output ports.
                String moml = "<entity name=\""
                        + StringUtilities.escapeForXML(pattern) + "\" class=\""
                        + _case.refinementClassName() + "\"/>";

                // The following is, regrettably, copied from ModalTransitionController.
                MoMLChangeRequest change = new MoMLChangeRequest(this, _case,
                        moml) {
                    protected void _execute() throws Exception {
                        super._execute();

                        // Mirror the ports of the container in the refinement.
                        // Note that this is done here rather than as part of
                        // the MoML because we have set protected variables
                        // in the refinement to prevent it from trying to again
                        // mirror the changes in the container.
                        Refinement entity = (Refinement) _case
                                .getEntity(pattern);

                        // Get the initial port configuration from the container.
                        Iterator<?> ports = _case.portList().iterator();

                        Set<Port> portsToMirror = new HashSet<Port>();
                        while (ports.hasNext()) {
                            Port port = (Port) ports.next();

                            // see if we should mirror the port
                            if (port != _case.control.getPort()) {
                                portsToMirror.add(port);
                            }
                        }

                        MultiCompositeActor.mirrorContainerPortsInRefinement(
                                entity, portsToMirror);

                        JGraph jgraph = _addTabbedPane(entity, true);
                        ((CaseGraphController) _controller)._addHotKeys(jgraph);
                    }
                };

                _case.requestChange(change);
            }
        }
    }

    /** Specialized graph controller that handles multiple graph models. */
    public class CaseGraphController extends ActorEditorGraphController {
        /** Override the base class to select the graph model associated
         *  with the selected pane.
         */
        public GraphModel getGraphModel() {
            if (_tabbedPane != null) {
                Component tab = _tabbedPane.getSelectedComponent();
                if (tab instanceof JGraph) {
                    GraphPane pane = ((JGraph) tab).getGraphPane();
                    return pane.getGraphModel();
                }
            }
            // Fallback position.
            return super.getGraphModel();
        }

        /** Add hot keys to the actions in the given JGraph.
         *
         *  @param jgraph The JGraph to which hot keys are to be added.
         */
        protected void _addHotKeys(JGraph jgraph) {
            super._addHotKeys(jgraph);
        }
    }

    /** Class implementing the Remove Case menu command. */
    public class RemoveCaseAction extends FigureAction {

        /** Create a case action with label "Add Case". */
        public RemoveCaseAction() {
            super("Remove Case");
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        }

        ///////////////////////////////////////////////////////////////////////////////
        ////                            public methods                             ////

        /** Perform the action. */
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            // Dialog to ask for a case name.
            Query query = new Query();
            List<?> refinements = _case.entityList(Refinement.class);
            if (refinements.size() < 2) {
                MessageHandler.error("No cases to remove.");
            } else {
                String[] caseNames = new String[refinements.size() - 1];
                Iterator<?> cases = refinements.iterator();
                int i = 0;
                while (cases.hasNext()) {
                    String name = ((Nameable) cases.next()).getName();
                    if (!name.equals("default")) {
                        caseNames[i] = name;
                        i++;
                    }
                }
                query.addChoice("case", "Remove case", caseNames, caseNames[0]);
                ComponentDialog dialog = new ComponentDialog(
                        CaseGraphFrame.this, "Remove Case", query);
                if (dialog.buttonPressed().equals("OK")) {
                    final String name = query.getStringValue("case");
                    String moml = "<deleteEntity name=\""
                            + StringUtilities.escapeForXML(name) + "\"/>";

                    // The following is, regrettably, copied from ModalTransitionController.
                    MoMLChangeRequest change = new MoMLChangeRequest(this,
                            _case, moml) {
                        protected void _execute() throws Exception {
                            super._execute();
                            // Find the tabbed pane that matches the name and remove it.
                            int count = _tabbedPane.getTabCount();
                            for (int i = 0; i < count; i++) {
                                if (name.equals(_tabbedPane.getTitleAt(i))) {
                                    _tabbedPane.remove(i);
                                    break;
                                }
                            }
                        }
                    };
                    _case.requestChange(change);
                }
            }
        }
    }
}
