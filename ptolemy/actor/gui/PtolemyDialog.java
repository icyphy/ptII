/* A frame for presenting a dialog

 Copyright (c) 2003 The Regents of the University of California.
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
@Pt.ProposedRating Red (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;

import ptolemy.util.MessageHandler;
import ptolemy.kernel.Entity;

//////////////////////////////////////////////////////////////////////////
//// PtolemyDialog
/**

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 2.0
*/

public abstract class PtolemyDialog extends JFrame implements ActionListener {

    public PtolemyDialog(
        String title,
        DialogTableau dialogTableau,
        Frame owner,
        Entity target,
        Configuration configuration) {

        super(title);
        _owner = owner;
        _dialogTableau = dialogTableau;
        _target = target;
        _configuration = configuration;
        _dialogTableau.setTitle(title);

        // This JDialog consists of two components. On top is a JComponent
        // inside of a JScrolPane that displays the ports and their attributes.
        // On bottom is a JPanel that contains buttons that cause various
        // actions.

        JPanel _buttons = _createButtonsPanel();
        getContentPane().add(_buttons, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                _cancel();
            }
        });

        _owner.addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                _iconify();
            }
            public void windowDeiconified(WindowEvent e) {
                _deiconify();
            }
        });
        setLocationRelativeTo(_owner);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // This method gets invoked as a result of a GUI action. Presently, it
    // handles button presses. It has to be a public, not protected, or private,
    // since it is inherited from ActionListener where it is public. Since it
    // isn't meant to be invoked by the developer there is no javadoc.
    public void actionPerformed(ActionEvent aEvent) {
        String command = aEvent.getActionCommand();
        if (aEvent.getSource() instanceof JButton) {
            _processButtonPress(command);
        }
    }

    /** Return the DialogTableau.
     * @return The DialogTableau
     */
    public DialogTableau getDialogTableau() {
        return _dialogTableau;
    }

    /** Return the target.
     * @return The target.
     */
    public Entity getTarget() {
        return _target;
    }

    public void setContents(JComponent contents) {
        _contents = contents;
        JScrollPane scrollPane = new JScrollPane(_contents);
        //        JViewport xx = new JViewport();
        //        xx.setView(((JTable) _contents).getTableHeader());
        //        scrollPane.setColumnHeader(xx);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    /** Set the target of this dialog.
     * @param entity Target of this dialog.
     */
    public void setTarget(Entity entity) {
        _target = entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    protected void _cancel() {
        dispose();
    }

    protected abstract void _createExtendedButtons(JPanel _buttons);

    protected boolean _isDirty() {
        return _dirty;
    }

    /** The button semantics are
     *  Commit - Apply and then cancel the dialog.
     *  Apply  - make the changes that have been expressed thus far.
     *  Help   - Show the associated help.
     *  Cancel - Remove the dialog without making any pending changes.
     * @param button
     */

    protected void _processButtonPress(String button) {
        if (button.equals("Help")) {
            {
                URL toRead =
                    getClass().getClassLoader().getResource(
                        "ptolemy/actor/gui/doc/portDialog.htm");
                if (toRead != null && _configuration != null) {
                    try {
                        _configuration.openModel(
                            null,
                            toRead,
                            toRead.toExternalForm());
                    } catch (Exception ex) {
                        MessageHandler.error("Help screen failure", ex);
                    }
                } else {
                    MessageHandler.error("No help available.");
                }
            }
        } else if (button.equals("Cancel")) {
            _cancel();
        } else {
            //TODO throw an exception here
        }
    }

    /** Set the boolean that determines if the GUI has a change that has not
     *  applied to the system
     * @param b True if the GUI has a change that has not been applied.
     */
    protected void _setDirty(boolean b) {
        _dirty = b;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                   ////

    protected boolean _debug = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private JPanel _createButtonsPanel() {
        JPanel _buttons = new JPanel();

        _createExtendedButtons(_buttons);
        _helpButton = new JButton("Help");
        _buttons.add(_helpButton);
        _cancelButton = new JButton("Cancel");
        _buttons.add(_cancelButton);
        for (int i = 0; i < _buttons.getComponentCount(); i++) {
            ((JButton) (_buttons.getComponent(i))).addActionListener(this);
        }
        return _buttons;
    }

    private void _deiconify() {
        setExtendedState(Frame.NORMAL);
    }

    private void _iconify() {
        setExtendedState(Frame.ICONIFIED);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private JButton _helpButton, _cancelButton;
    private Configuration _configuration;
    private JComponent _contents;
    // The following is true if any of the values have been changed but not
    // applied.
    private boolean _dirty = false;
    private Frame _owner;
    private DialogTableau _dialogTableau;
    private Entity _target;
}
