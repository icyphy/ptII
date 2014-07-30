/* The size dialog for setting the height and width of the scene.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.homer.gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

///////////////////////////////////////////////////////////////////
//// SizeDialog

/** Class that defines the screen size dialog.
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
@SuppressWarnings("serial")
public class SizeDialog extends JPanel {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create the dialog with the specified height/width values set.
     *  @param height The initial value of the height spinner.
     *  @param width The initial value of the width spinner.
     */
    public SizeDialog(int height, int width) {
        _heightSpinner.setValue(height);
        _widthSpinner.setValue(width);

        setLayout(new GridLayout(2, 2));
        add(new JLabel("Height: "));
        add(_heightSpinner);
        add(new JLabel("Width: "));
        add(_widthSpinner);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Show the prompt and return the selection.
     *  @return The return value of the dialog.
     */
    public int showPrompt() {
        JOptionPane optionPane = new JOptionPane(this,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog("Screen Size");
        dialog.pack();
        dialog.setVisible(true);
        dialog.setModal(true);

        if (optionPane.getValue() == null) {
            return JOptionPane.CANCEL_OPTION;
        } else {
            return (Integer) optionPane.getValue();
        }
    }

    /** Get the dimensions from the spinners for use in resizing the scene.
     *  @return The dimension selected for the scene.
     */
    public Dimension getDimensions() {
        return new Dimension(((SpinnerNumberModel) _widthSpinner.getModel())
                .getNumber().intValue(),
                ((SpinnerNumberModel) _heightSpinner.getModel()).getNumber()
                .intValue());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The height UI element.
     */
    private final JSpinner _heightSpinner = new JSpinner(
            new SpinnerNumberModel(100, 1, 9999, 1));

    /** The width UI element.
     */
    private final JSpinner _widthSpinner = new JSpinner(new SpinnerNumberModel(
            100, 1, 9999, 1));
}
