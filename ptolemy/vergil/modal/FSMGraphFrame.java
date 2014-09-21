/* A simple graph view for Ptolemy models

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.BooleanToken;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalPort;
import ptolemy.domains.modal.modal.RefinementPort;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphPane;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.graph.GraphPane;

///////////////////////////////////////////////////////////////////
//// FSMGraphFrame

/**
 This is a graph editor frame for ptolemy FSM models.  Given a composite
 entity and a tableau, it creates an editor and populates the menus
 and toolbar.  This overrides the base class to associate with the
 editor an instance of FSMGraphController.

 @author  Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class FSMGraphFrame extends ExtendedGraphFrame implements ActionListener {

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
    public FSMGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    /** React to the actions specific to this FSM graph frame.
     *
     *  @param e The action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem target = (JMenuItem) e.getSource();
        String actionCommand = target.getActionCommand();
        if (actionCommand.equals("Import Design Pattern")) {
            importDesignPattern();
        } else if (actionCommand.equals("Export Design Pattern")) {
            exportDesignPattern();
        }
    }

    /** Get the currently selected objects from this document, if any,
     *  and place them on the clipboard in MoML format.
     */
    @Override
    public void copy() {
        HashSet<NamedObj> namedObjSet = _getSelectionSet();
        try {
            for (NamedObj namedObj : namedObjSet) {
                if (namedObj instanceof State) {
                    ((State) namedObj).saveRefinementsInConfigurer
                            .setToken(BooleanToken.TRUE);
                }
            }
            super.copy();
        } catch (IllegalActionException e) {
            MessageHandler.error("Unable to set attributes of the selected "
                    + "states.");
        } finally {
            for (NamedObj namedObj : namedObjSet) {
                if (namedObj instanceof State) {
                    try {
                        ((State) namedObj).saveRefinementsInConfigurer
                                .setToken(BooleanToken.FALSE);
                    } catch (IllegalActionException e) {
                        // Ignore.
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        // _graphMenu is instantiated in BasicGraphFrame.
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

        // Set the action command and listener for each menu item.
        for (JMenuItem debugMenuItem : debugMenuItems) {
            debugMenuItem.setActionCommand(debugMenuItem.getText());
            debugMenuItem.addActionListener(debugMenuListener);
            _debugMenu.add(debugMenuItem);
        }

        _menubar.add(_debugMenu);
    }

    /** Return a new DebugMenuListener.
     *  @return the new DebugMenuListener.
     */
    protected ActionListener _getDebugMenuListener() {
        return new DebugMenuListener();
    }

    /** Return an array of debug menu items.
     *  @return an array of debug menu items.
     */
    protected JMenuItem[] _debugMenuItems() {
        // Add debug menu.
        JMenuItem[] debugMenuItems = {
                new JMenuItem("Listen to Director", KeyEvent.VK_D),
                new JMenuItem("Listen to State Machine", KeyEvent.VK_L),
                new JMenuItem(_getAnimationMenuText(), KeyEvent.VK_A),
                new JMenuItem("Stop Animating", KeyEvent.VK_S), };
        return debugMenuItems;
    }

    /** Close the window.  Override the base class to remove the debug
     *  listener, if there is one.
     *  @return False if the user cancels on a save query.
     */
    @Override
    protected boolean _close() {
        // Running with a headless display (Xvfb) could result in the model being null.
        if (getModel() != null) {
            getModel().removeDebugListener(_controller);
        }
        return super._close();
    }

    /** Create the items in the File menu. A null element in the array
     *  represents a separator in the menu.
     *
     *  @return The items in the File menu.
     */
    @Override
    protected JMenuItem[] _createFileMenuItems() {
        JMenuItem[] fileMenuItems = super._createFileMenuItems();
        int i = 0;
        for (JMenuItem item : fileMenuItems) {
            i++;
            if (item.getActionCommand().equals("Save As")) {
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

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     *  @param entity The object to be displayed in the pane (which must be
     *   an instance of CompositeEntity).
     *  @return The pane that is created.
     */
    @Override
    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new FSMGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.
        final FSMGraphModel graphModel = new FSMGraphModel(
                (CompositeEntity) entity);
        return new BasicGraphPane(_controller, graphModel, entity);
    }

    /** Export the model into the writer with the given name.
     *
     *  @param writer The writer.
     *  @param model The model to export.
     *  @param name The name of the exported model.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    protected void _exportDesignPattern(Writer writer, NamedObj model,
            String name) throws IOException {
        if (_query != null && _query.hasEntry("selected")
                && _query.getBooleanValue("selected")) {
            List<State> modifiedStates = new LinkedList<State>();
            try {
                Set<?> set = _getSelectionSet();
                for (Object object : set) {
                    if (object instanceof State) {
                        State state = (State) object;
                        modifiedStates.add(state);
                        state.saveRefinementsInConfigurer
                                .setToken(BooleanToken.TRUE);
                    }
                }
                super._exportDesignPattern(writer, model, name);
            } catch (IllegalActionException e) {
                throw new InternalErrorException(null, e, "Unable to set "
                        + "attributes for the states.");
            } finally {
                for (State state : modifiedStates) {
                    try {
                        state.saveRefinementsInConfigurer
                                .setToken(BooleanToken.FALSE);
                    } catch (IllegalActionException e) {
                        // Ignore.
                    }
                }
            }
        } else {
            ((FSMActor) model).exportSubmodel(writer, 0, name);
        }
    }

    /** Finish exporting a design pattern.
     */
    @Override
    protected void _finishExportDesignPattern() {
        super._finishExportDesignPattern();

        for (IOPort port : _modifiedPorts) {
            try {
                port.setInput(true);
            } catch (IllegalActionException e) {
                // Ignore.
            }
        }
    }

    /** Return the text to be used in the animation menu item. In this base
     *  class, always return "Animate States".
     *
     *  @return The text for the menu item.
     */
    protected String _getAnimationMenuText() {
        return "Animate States";
    }

    /** Prepare to export a design pattern.
     *
     *  @exception InternalErrorException Thrown if attributes of the ports to
     *   be exported cannot be set.
     */
    @Override
    protected void _prepareExportDesignPattern() {
        super._prepareExportDesignPattern();

        try {
            FSMActor actor = (FSMActor) getModel();
            List<IOPort> ports = actor.portList();
            _modifiedPorts.clear();
            for (IOPort port : ports) {
                if (port instanceof RefinementPort && port.isInput()
                        && port.isOutput()) {
                    List<IOPort> connectedPorts = port.connectedPortList();
                    for (IOPort connectedPort : connectedPorts) {
                        if (connectedPort instanceof ModalPort
                                && !connectedPort.isInput()) {
                            _modifiedPorts.add(port);
                            port.setInput(false);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalErrorException(null, e, "Fail to prepare for "
                    + "exporting a design pattern.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The controller.
     *  The controller is protected so that the subclass
     * (InterfaceAutomatonGraphFrame) can set it to a more specific
     * controller.
     */
    protected FSMGraphController _controller;

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;

    // The list of ports modified by the previous invocation of
    // _prepareExportDesignPattern().
    private List<IOPort> _modifiedPorts = new LinkedList<IOPort>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {
        /** React to a menu command. */
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            try {
                if (actionCommand.equals("Listen to Director")) {
                    Effigy effigy = (Effigy) getTableau().getContainer();

                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy,
                            effigy.uniqueName("debug listener"));
                    DebugListenerTableau tableau = new DebugListenerTableau(
                            textEffigy, textEffigy.uniqueName("debugListener"));
                    tableau.setDebuggable(((FSMActor) getModel()).getDirector());
                } else if (actionCommand.equals("Listen to State Machine")) {
                    Effigy effigy = (Effigy) getTableau().getContainer();

                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy,
                            effigy.uniqueName("debug listener"));
                    DebugListenerTableau tableau = new DebugListenerTableau(
                            textEffigy, textEffigy.uniqueName("debugListener"));
                    tableau.setDebuggable(getModel());
                } else if (actionCommand.equals(_getAnimationMenuText())) {
                    // Dialog to ask for a delay time.
                    Query query = new Query();
                    query.addLine("delay", "Time (in ms) to hold highlight",
                            Long.toString(_lastDelayTime));

                    ComponentDialog dialog = new ComponentDialog(
                            FSMGraphFrame.this, "Delay for Animation", query);

                    if (dialog.buttonPressed().equals("OK")) {
                        try {
                            _lastDelayTime = Long.parseLong(query
                                    .getStringValue("delay"));
                            _controller.setAnimationDelay(_lastDelayTime);

                            NamedObj model = getModel();

                            if (model != null && _listeningTo != model) {
                                if (_listeningTo != null) {
                                    _listeningTo
                                            .removeDebugListener(_controller);
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
                    MessageHandler.warning("Failed to create debug listener: "
                            + ex);
                } catch (CancelException exception) {
                }
            }
        }

        private NamedObj _listeningTo;
    }

}
