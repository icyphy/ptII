/* A graph view for the case construct for Ptolemy models.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.vergil.fsm;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.fsm.modal.Case;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.domains.fsm.modal.RefinementPort;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.toolbox.FigureAction;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// CaseGraphFrame

/**
 This is a graph editor frame for ptolemy case models.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.1
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

        // Override the default help file.
        // FIXME
        // helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source instanceof JTabbedPane) {
            Component selected = ((JTabbedPane)source).getSelectedComponent();
            if (selected instanceof JGraph) {
                setJGraph((JGraph)selected);
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
        // FIXME: Is it OK to assign to a local variable?
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(this);
        Iterator cases = ((Case)entity).entityList(Refinement.class).iterator();
        while (cases.hasNext()) {
            Refinement refinement = (Refinement)cases.next();
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
            tabbedPane.add(refinement.getName(), jgraph);
            setJGraph(jgraph);
            jgraph.setBackground(BACKGROUND_COLOR);

            // Create a drop target for the jgraph.
            // FIXME: Should override _setDropIntoEnabled to modify all the drop targets created.
            new EditorDropTarget(jgraph);
        }
        return tabbedPane;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The case menu. */
    protected JMenu _caseMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The action to add a case. */
    private AddCaseAction _addCaseAction;
    
    /** The Case actor displayed by this frame. */
    private Case _case;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** Class implementing the Add Case menu command. */
    public class AddCaseAction extends FigureAction {
        
        /** Create a case action with label "Add Case". */
        public AddCaseAction() {
            super("Add Case");
        }
        
        ///////////////////////////////////////////////////////////////////////////////
        ////                            public methods                             ////
        
        /** Perform the action. */
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            // Dialog to ask for a case name.
            Query query = new Query();
            query.addLine("case", "Pattern that the control input must match", "");
            ComponentDialog dialog = new ComponentDialog(
                    CaseGraphFrame.this, "Add Case", query);
            if (dialog.buttonPressed().equals("OK")) {
                final String pattern = query.getStringValue("case");
                // NOTE: We do not use a TransitionRefinement because we don't
                // want the sibling input ports that come with output ports.
                String moml = "<entity name=\"" + pattern + "\" class=\"ptolemy.domains.fsm.modal.Refinement\"/>";
                
                // The following is, regrettably, copied from ModalTransitionController.
                MoMLChangeRequest change = new MoMLChangeRequest(this, _case,
                        moml) {
                    protected void _execute() throws Exception {
                        super._execute();
                        
                        // Move the default to last.
                        Entity defaultRefinement = _case.getEntity("default");
                        if (defaultRefinement != null) {
                            defaultRefinement.moveToLast();
                        }
                        
                        // FIXME: use setModel() and make the new case the current one.

                        // Mirror the ports of the container in the refinement.
                        // Note that this is done here rather than as part of
                        // the MoML because we have set protected variables
                        // in the refinement to prevent it from trying to again
                        // mirror the changes in the container.
                        Entity entity = _case.getEntity(pattern);

                        // Get the initial port configuration from the container.
                        Iterator ports = _case.portList().iterator();

                        while (ports.hasNext()) {
                            Port port = (Port) ports.next();
                            // No need to mirror the control port, as it's a PortParameter.
                            // Hence, its value is available as a parameter.
                            if (port == _case.control.getPort()) {
                                continue;
                            }

                            try {
                                // NOTE: This is awkward.
                                if (entity instanceof Refinement) {
                                    ((Refinement) entity).setMirrorDisable(true);
                                } else if (entity instanceof ModalController) {
                                    ((ModalController) entity)
                                            .setMirrorDisable(true);
                                }

                                Port newPort = entity.newPort(port.getName());

                                if (newPort instanceof RefinementPort
                                        && port instanceof IOPort) {
                                    try {
                                        ((RefinementPort) newPort)
                                                .setMirrorDisable(true);

                                        if (((IOPort) port).isInput()) {
                                            ((RefinementPort) newPort)
                                                    .setInput(true);
                                        }

                                        if (((IOPort) port).isOutput()) {
                                            ((RefinementPort) newPort)
                                                    .setOutput(true);
                                        }

                                        if (((IOPort) port).isMultiport()) {
                                            ((RefinementPort) newPort)
                                                    .setMultiport(true);
                                        }
                                    } finally {
                                        ((RefinementPort) newPort)
                                                .setMirrorDisable(false);
                                    }
                                }
                            } finally {
                                // NOTE: This is awkward.
                                if (entity instanceof Refinement) {
                                    ((Refinement) entity).setMirrorDisable(false);
                                } else if (entity instanceof ModalController) {
                                    ((ModalController) entity)
                                            .setMirrorDisable(false);
                                }
                            }
                        }
                    }
                };

                _case.requestChange(change);
            }
        }
    }
}
