/** A graph editor frame for Ptolemy models that use the JNI interface.

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

@ProposedRating Red (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
*/

package jni;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorGraphTableau;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.graph.GraphPane;

//////////////////////////////////////////////////////////////////////////
//// ThalesGraphFrame
/**
This is a graph editor frame for ptolemy models that use the JNI interface.
Given a composite entity and an instance of ThalesGraphTableau,
it creates an editor and populates the menus and toolbar.
This overrides the base class to associate with the editor the JNI interface.

@author  Steve Neuendorffer, Vincent Arnould, Contributor: Edward A. Lee
@version $Id$
*/
public class ThalesGraphFrame extends BasicGraphFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see ptolemy.actor.gui.Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public ThalesGraphFrame(
            CompositeEntity entity,
            ActorGraphTableau tableau) {
        super(entity, tableau);
        //super._addMenus();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();
        // Add any commands to graph menu and toolbar
        // that the controller
        // wants in the graph menu and toolbar.
        _graphMenu.addSeparator();
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        // Add debug menu.
        JMenuItem[] debugMenuItems =
        {
            new JMenuItem("Listen to Director",
                    KeyEvent.VK_L),
            new JMenuItem("Animate Execution",
                    KeyEvent.VK_A),
            new JMenuItem("Stop Animating",
                    KeyEvent.VK_S),
        };

        //TRT Add JNI Menu
        _graphMenu.addSeparator();

        JMenuItem[] jniMenuItems =
        { new JMenuItem("Generate C Interface",
                KeyEvent.VK_G)
            };
        //TRT end

        // NOTE: This has to be initialized here rather than
        // statically because this method is called
        // by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);
        DebugMenuListener debugMenuListener =
            new DebugMenuListener();
        // Set the action command and listener for each menu item.
        for (int i = 0; i < debugMenuItems.length; i++) {
            debugMenuItems[i].setActionCommand(
                    debugMenuItems[i].getText());
            debugMenuItems[i].addActionListener(
                    debugMenuListener);
            _debugMenu.add(debugMenuItems[i]);
        }
        _menubar.add(_debugMenu);

        //TRT begin changes

        _jniMenu = new JMenu("JNI");
        _jniMenu.setMnemonic(KeyEvent.VK_J);
        JNIMenuListener jniMenuListener = new JNIMenuListener();
        // Set the action command and listener for each menu item.
        for (int i = 0; i < jniMenuItems.length; i++) {
            jniMenuItems[i].setActionCommand(
                    jniMenuItems[i].getText());
            jniMenuItems[i].addActionListener(
                    jniMenuListener);
            _jniMenu.add(jniMenuItems[i]);
        }
        _menubar.add(_jniMenu);
        //TRT end

    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be
     *  careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {

        _controller = new ActorEditorGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // Add the ArgumentDialogFactory to the context menu for actors
        _controller.getEntityController()
            .addMenuItemFactory(new ArgumentDialogFactory());

        // NOTE: The cast is safe because the constructor
        // accepts on CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(
                (CompositeEntity)getModel());
        return new GraphPane(_controller, graphModel);
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        try {
            URL doc =
                getClass().getClassLoader().getResource(
                        "ptolemy/configs/doc/vergilGraphEditorHelp.htm");
            getConfiguration().openModel(
                    null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** Listener for jni menu commands.
     */
    public class JNIMenuListener implements ActionListener {

        /** React to a menu command.
         */
        public void actionPerformed(ActionEvent event) {
            JMenuItem target = (JMenuItem) event.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Generate C Interface")) {
                    // NOTE: The cast is safe because the constructor
                    // accepts only CompositeEntity.
                    if (!JNIUtilities.generateJNI((CompositeEntity)getModel())) {
                        MessageHandler.error("No JNIActor to interface to!");
                    }
                }
            } catch (Exception ex) {
                MessageHandler.error("Failed to create C interface : " + ex);
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    //TRT
    /** Jni menu for this frame.
     */
    protected JMenu _jniMenu;
    //TRT end

    /** Debug menu for this frame.
     */
    protected JMenu _debugMenu;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    // NOTE: The following class is very similar to the inner class
    // in FSMGraphFrame.  Is there some way to merge these?
    // There seem to be enough differences that this may be hard.

    /** Listener for debug menu commands.
     */
    public class DebugMenuListener implements ActionListener {

        /** React to a menu command.
         */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Listen to Director"))
                    {
                        NamedObj model = getModel();
                        boolean success = false;
                        if (model instanceof Actor) {
                            Director director = ((Actor) model).getDirector();
                            if (director != null) {
                                Effigy effigy =
                                    (Effigy) getTableau().getContainer();
                                // Create a new text effigy inside this one.
                                Effigy textEffigy =
                                    new TextEffigy(
                                            effigy,
                                            effigy.uniqueName("debug listener"));
                                DebugListenerTableau tableau =
                                    new DebugListenerTableau(
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
                        // hierarchy.  Insides of transparent composite
                        // actors do not get animated if they are classes
                        // rather than instances.
                        NamedObj model = getModel();
                        if (model instanceof Actor) {
                            // Dialog to ask for a delay time.
                            Query query = new Query();
                            query.addLine(
                                    "delay",
                                    "Time (in ms) to hold highlight",
                                    Long.toString(_lastDelayTime));
                            ComponentDialog dialog = new ComponentDialog(
                                    //TRT
                                    ThalesGraphFrame.this,
                                    //TRT end
                                    "Delay for Animation", query);
                            if (dialog.buttonPressed().equals("OK")) {
                                try {
                                    _lastDelayTime =
                                        Long.parseLong(
                                                query.getStringValue("delay"));
                                    _controller.setAnimationDelay(_lastDelayTime);
                                    Director director =
                                        ((Actor) model).getDirector();
                                    while (director == null
                                            && model instanceof Actor) {
                                        model = (NamedObj) model.getContainer();
                                        if (model instanceof Actor) {
                                            director =
                                                ((Actor) model).getDirector();
                                        }
                                    }
                                    if (director != null
                                            && _listeningTo != director) {
                                        if (_listeningTo != null) {
                                            _listeningTo.removeDebugListener(
                                                    _controller);
                                        }
                                        director.addDebugListener(_controller);
                                        _listeningTo = director;
                                    }
                                } catch (NumberFormatException ex) {
                                    MessageHandler.error(
                                            "Invalid time, which is required "
                                            + "to be an integer",
                                            ex);
                                }
                            } else {
                                MessageHandler.error(
                                        "Cannot find the director. Possibly this "
                                        + "is because this is a class, not an "
                                        + "instance.");
                            }
                        } else {
                            MessageHandler.error(
                                    "Model is not an actor. Cannot animate.");
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
                    MessageHandler.warning(
                            "Failed to create debug listener: " + ex);
                } catch (CancelException exception) {
                }
            }
        }
        private Director _listeningTo;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //TRT
    /** The controller
     */
    private ActorEditorGraphController _controller;
    //TRT end

    /** The delay time specified that last time animation was set.
     */
    private long _lastDelayTime = 0;
}
