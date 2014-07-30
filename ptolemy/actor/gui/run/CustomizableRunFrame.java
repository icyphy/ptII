/* A top-level frame for a customizable run control panel.

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
package ptolemy.actor.gui.run;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// CustomizableRunFrame

/**
 A top-level frame for a customizable run control panel.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class CustomizableRunFrame extends TableauFrame {
    /** Construct a frame to control the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically accomplished by calling show() on
     *  enclosing tableau.
     *  @param model The model to put in this frame, or null if none.
     *  @param tableau The tableau responsible for this frame.
     *  @exception IllegalActionException If the XML to be parsed has errors.
     */
    public CustomizableRunFrame(CompositeActor model, InterfaceTableau tableau)
            throws IllegalActionException {
        super(tableau);
        _model = model;
        getContentPane().setLayout(new BorderLayout());
        _pane = new CustomizableRunPane(model, null);
        getContentPane().add(_pane, BorderLayout.CENTER);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a Customize menu.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        JMenuItem[] customizeMenuItems = {
                new JMenuItem("Customize Layout", KeyEvent.VK_C),
                new JMenuItem("Revert to Default", KeyEvent.VK_R),
                new JMenuItem("New Layout", KeyEvent.VK_N) };

        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _customizeMenu = new JMenu("Customize");
        _customizeMenu.setMnemonic(KeyEvent.VK_C);

        CustomizeMenuListener customizeMenuListener = new CustomizeMenuListener();

        // Set the action command and listener for each menu item.
        for (JMenuItem customizeMenuItem : customizeMenuItems) {
            customizeMenuItem.setActionCommand(customizeMenuItem.getText());
            customizeMenuItem.addActionListener(customizeMenuListener);
            _customizeMenu.add(customizeMenuItem);
        }

        _menubar.add(_customizeMenu);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Customize menu for this frame. */
    protected JMenu _customizeMenu;

    /** The pane inside this frame. */
    protected CustomizableRunPane _pane;

    /** The associated model. */
    protected CompositeActor _model;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for customize menu commands. */
    public class CustomizeMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Customize Layout")) {
                // Layout frame to customize the layout.
                try {
                    Effigy effigy = getEffigy();
                    LayoutTableau tableau = new LayoutTableau(
                            (PtolemyEffigy) effigy, "Layout Run Panel", _pane);
                    tableau.show();
                } catch (KernelException ex) {
                    try {
                        MessageHandler
                        .warning("Failed to create layout customization frame: "
                                + ex);
                    } catch (CancelException exception) {
                    }
                }
            } else if (actionCommand.equals("Revert to Default")) {
                // Delete the attribute, if it has been created.
                Attribute attribute = _model
                        .getAttribute("_runLayoutAttribute");
                if (attribute != null) {
                    // Do this in a change request so it can be undone.
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            _model,
                            "<deleteProperty name=\"_runLayoutAttribute\"/>") {
                        @Override
                        protected void _execute() throws Exception {
                            super._execute();
                            // Close this window and open a new one.
                            // This must be done in the swing event thread.
                            Runnable reOpen = new Runnable() {
                                @Override
                                public void run() {
                                    // Create a new tableau.  Closing the old frame
                                    // also results in removing the tableau.
                                    PtolemyEffigy effigy = (PtolemyEffigy) getTableau()
                                            .getContainer();
                                    close();
                                    try {
                                        InterfaceTableau tableau = new InterfaceTableau(
                                                effigy,
                                                effigy.uniqueName("interfaceTableau"));
                                        tableau.show();
                                    } catch (KernelException e) {
                                        MessageHandler.error(
                                                "Failed to reopen run window.",
                                                e);
                                    }
                                }
                            };
                            deferIfNecessary(reOpen);
                        }
                    };
                    _model.requestChange(request);
                    // FIXME: Should also close the RunLayoutFrame, if it is open.
                }
            } else if (actionCommand.equals("New Layout")) {
                // Layout frame to create a new layout.
                try {
                    // Remove the existing layout.
                    getContentPane().remove(_pane);
                    // Define a default layout in XML.
                    String xml = "<containers>" + "<container name=\"top\" "
                            + "columnSpecs=\"default\" "
                            + "rowSpecs=\"default\"> "
                            + "</container></containers>";
                    _pane = new CustomizableRunPane(_model, xml);
                    getContentPane().add(_pane, BorderLayout.CENTER);
                    pack();
                    repaint();

                    // Open the customization panel.
                    Effigy effigy = getEffigy();
                    LayoutTableau tableau = (LayoutTableau) effigy
                            .getEntity("Layout Run Panel");
                    if (tableau == null) {
                        tableau = new LayoutTableau((PtolemyEffigy) effigy,
                                "Layout Run Panel", _pane);
                    }
                    tableau.show();

                    // Get this on top, since it is smaller, and will be obscured.
                    setVisible(true);
                } catch (KernelException ex) {
                    try {
                        MessageHandler
                        .warning("Failed to create layout customization frame: "
                                + ex);
                    } catch (CancelException exception) {
                    }
                }
            }
        }
    }
}
