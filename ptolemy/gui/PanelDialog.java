/* A top-level dialog window containing an arbitrary panel.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.gui;

import java.awt.*;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

//////////////////////////////////////////////////////////////////////////
//// PanelDialog
/**

This class is a modal dialog box that contains an arbitrary panel.
It can be used, for example, to put an instance of Query in a
top-level dialog box.  The general way to use this class is to create
the panel that you wish to have contained in the dialog.
Then pass that panel to the constructor of this class.  The dialog
is modal, so the statement that creates the dialog will not return
until the user dismisses the dialog.  The method changesAccepted()
can then be called to find out whether the user clicked the OK button
or the Cancel button.  Then you can access the panel to determine
what values were set by the user.

@author Edward A. Lee
@version $Id$
*/
public class PanelDialog extends JDialog {

    /** Construct a dialog with the specified owner, title, and panel.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param title The title to put on the window.
     *  @param panel The panel to insert in the dialog.
     */
    public PanelDialog(Frame owner, String title, JPanel panel) {
        super(owner, title, true);

        contents = panel;
        Object[] array = {panel};

        final String btnString1 = "OK";
        final String btnString2 = "Cancel";
        Object[] options = {btnString1, btnString2};

        _optionPane = new JOptionPane(array, 
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);
        
        getContentPane().add(_optionPane);

        setLocationRelativeTo(owner);

        pack();
        setResizable(false);

        // The following code is based on Sun's CustomDialog example...
        _optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                // PropertyChange is an extremely non-selective listener,
                // so we have to filter...
                if (isVisible() 
                        && (e.getSource() == _optionPane)
                        && (prop.equals(JOptionPane.VALUE_PROPERTY) ||
                        prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {

                    Object value = _optionPane.getValue();

                    //ignore reset
                    if (value == JOptionPane.UNINITIALIZED_VALUE) return;

                    // Reset the JOptionPane's value.
                    // If you don't do this, then if the user
                    // presses the same button next time, no
                    // property change event will be fired.
                    _optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

                    if (value.equals(btnString1)) {
                        // OK button was pressed... React accordingly.
                        _accepted = true;
                    }
                    setVisible(false);
                }
            }
        });
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the dialog was closed by the OK button.
     *  Return false otherwise.
     */
    public boolean changesAccepted () {
        return _accepted;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The panel contained by this dialog.
     */
    public JPanel contents;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A flag indicating whether the changes to the dialog have been
    // accepted.
    private boolean _accepted = false;

    // The pane with the buttons.
    private JOptionPane _optionPane;
}
