/* A simple graph view for Ptolemy models

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.fsm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.graph.GraphPane;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphFrame
/**
This is a graph editor frame for ptolemy FSM models.  Given a composite
entity and a tableau, it creates an editor and populates the menus
and toolbar.  This overrides the base class to associate with the
editor an instance of FSMGraphController.

@author  Steve Neuendorffer, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class FSMGraphFrame extends ExtendedGraphFrame {

    /** Construct a frame associated with the specified FSM model.
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
    public FSMGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified FSM model.
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
    public FSMGraphFrame(
            CompositeEntity entity,
            Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        // Add any commands to graph menu and toolbar that the controller
        // wants in the graph menu and toolbar.
        _graphMenu.addSeparator();
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        // Add debug menu.
        JMenuItem[] debugMenuItems = {
            new JMenuItem("Listen to Director", KeyEvent.VK_D),
            new JMenuItem("Listen to State Machine", KeyEvent.VK_L),
            new JMenuItem("Animate States", KeyEvent.VK_A),
            new JMenuItem("Stop Animating", KeyEvent.VK_S),
        };
        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);
        DebugMenuListener debugMenuListener = new DebugMenuListener();
        // Set the action command and listener for each menu item.
        for (int i = 0; i < debugMenuItems.length; i++) {
            debugMenuItems[i].setActionCommand(debugMenuItems[i].getText());
            debugMenuItems[i].addActionListener(debugMenuListener);
            _debugMenu.add(debugMenuItems[i]);
        }
        _menubar.add(_debugMenu);
    }

    /** Close the window.  Override the base class to remove the debug
     *  listener, if there is one.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        getModel().removeDebugListener(_controller);
        return super._close();
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {
        _controller = new FSMGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
        final FSMGraphModel graphModel = new FSMGraphModel(getModel());
        return new GraphPane(_controller, graphModel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The controller is protected so that the subclass
    // (InterfaceAutomatonGraphFrame) can set it to a more specific
    // controller.
    protected FSMGraphController _controller;

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {

        /** React to a menu command. */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Listen to Director")) {
                    Effigy effigy = (Effigy)getTableau().getContainer();
                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy,
                            effigy.uniqueName("debug listener"));
                    DebugListenerTableau tableau =
                        new DebugListenerTableau(textEffigy,
                                textEffigy.uniqueName("debugListener"));
                    tableau.setDebuggable(
                            ((FSMActor)getModel()).getDirector());
                }
                else if (actionCommand.equals("Listen to State Machine")) {
                    Effigy effigy = (Effigy)getTableau().getContainer();
                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy,
                            effigy.uniqueName("debug listener"));
                    DebugListenerTableau tableau =
                        new DebugListenerTableau(textEffigy,
                                textEffigy.uniqueName("debugListener"));
                    tableau.setDebuggable(getModel());
                } else if (actionCommand.equals("Animate States")) {
                    // Dialog to ask for a delay time.
                    Query query = new Query();
                    query.addLine("delay",
                            "Time (in ms) to hold highlight",
                            Long.toString(_lastDelayTime));
                    ComponentDialog dialog = new ComponentDialog(
                            FSMGraphFrame.this,
                            "Delay for Animation",
                            query);
                    if (dialog.buttonPressed().equals("OK")) {
                        try {
                            _lastDelayTime = Long.parseLong(
                                    query.getStringValue("delay"));
                            _controller.setAnimationDelay(_lastDelayTime);
                            NamedObj model = getModel();
                            if (model != null && _listeningTo != model) {
                                if (_listeningTo != null) {
                                    _listeningTo.removeDebugListener(
                                            _controller);
                                }
                                _listeningTo = model;
                                _listeningTo.addDebugListener(_controller);
                            }
                        } catch (NumberFormatException ex) {
                            MessageHandler.error(
                                    "Invalid time, which is required "
                                    + "to be an integer: ", ex);
                        }
                    }
                } else if (actionCommand.equals("Stop Animating")
                        && _listeningTo != null) {
                    _listeningTo.removeDebugListener(_controller);
                    _controller.clearAnimation();
                    _listeningTo = null;
                }
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning(
                            "Failed to create debug listener: " + ex);
                } catch (CancelException exception) {}
            }
        }
        private NamedObj _listeningTo;
    }
}
