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

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.kernel.CompositeEntity;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorGraphTableau;
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
public class ThalesGraphFrame extends ActorGraphFrame {

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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        // TRT Add JNI Menu

        JMenuItem[] jniMenuItems = {
                new JMenuItem("Generate C Interface",
                KeyEvent.VK_G)};
            
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
        
        GraphPane result = super._createGraphPane();

        // Add the ArgumentDialogFactory to the context menu for actors
        _controller.getEntityController()
            .addMenuItemFactory(new ArgumentDialogFactory());

        return result;
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The delay time specified that last time animation was set.
     */
    private long _lastDelayTime = 0;
}
