/* A top-level dialog window containing an arbitrary component.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.gui;

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

//////////////////////////////////////////////////////////////////////////
//// ComponentDialog
/**

This class is a modal dialog box that contains an arbitrary component.
It can be used, for example, to put an instance of Query in a
top-level dialog box.  The general way to use this class is to create
the component that you wish to have contained in the dialog.
Then pass that component to the constructor of this class.  The dialog
is modal, so the statement that creates the dialog will not return
until the user dismisses the dialog.  The method buttonPressed()
can then be called to find out whether the user clicked the OK button
or the Cancel button (or any other button specified in the constructor).
Then you can access the component to determine what values were set
by the user.

@author Edward A. Lee
@version $Id$
*/
public class ComponentDialog extends JDialog {

    /** Construct a dialog with the specified owner, title, and component.
     *  An "OK" and a "Cancel" button are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param title The title to put on the window.
     *  @param component The component to insert in the dialog.
     */
    public ComponentDialog(JFrame owner, String title, Component component) {
        this(owner, title, component, null);
    }

    /** Construct a dialog with the specified owner, title, component,
     *  and buttons.  The first button is the "default" in that
     *  it is the one activated by "Enter" or "Return" keys.
     *  If the last argument is null, then an "OK"
     *  and a "Cancel" button will be created.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param title The title to put on the window.
     *  @param component The component to insert in the dialog.
     *  @param buttons An array of labels for buttons at the bottom
     *   of the dialog.
     */
    public ComponentDialog(
            JFrame owner, String title, Component component, String[] buttons) {

        super(owner, title, true);

        contents = component;
        Object[] array = {component};

        if (buttons != null) {
            _buttons = buttons;
        }

        _optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                _buttons,
                _buttons[0]);

        getContentPane().add(_optionPane);

        setLocationRelativeTo(owner);
        // FIXME: Perhaps this should center on the screen otherwise.

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

                    if (value instanceof String) {
                        // A button was pressed...
                        _buttonPressed = (String)value;
                    }
                    setVisible(false);
                }
            }
        });
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the label of the button that triggered closing the
     *  dialog, or an empty string if none.
     */
    public String buttonPressed () {
        return _buttonPressed;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The component contained by this dialog.
     */
    public Component contents;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The label of the button pushed to dismiss the dialog.
    private String _buttonPressed = "";

    // Button labels.
    private String[] _buttons = {"OK", "Cancel"};

    // The pane with the buttons.
    private JOptionPane _optionPane;
}
