/* Customized dialog for opening models and their layouts.

 Copyright (c) 2011 The Regents of the University of California.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

///////////////////////////////////////////////////////////////////
//// OpenLayoutDialog

/** Customized dialog for opening models and their layouts.
 * 
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class OpenLayoutDialog implements ActionListener {

    public OpenLayoutDialog(JFrame parent, HomerMenu menu) {
        _parent = parent;
        _menu = menu;

        // Initialize other components
        _modelFileField = new JTextField("");
        _chooseModelButton = new JButton("Choose model");
        _chooseModelButton.addActionListener(this);

        _layoutFileField = new JTextField("");
        _chooseLayoutButton = new JButton("Choose layout");
        _chooseLayoutButton.addActionListener(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    public void actionPerformed(ActionEvent e) {

        JFileChooser fileChooser = _menu.getFileChooser();

        if (e.getSource() == _chooseModelButton) {
            //Handle model button action.
            fileChooser.setDialogTitle("Choose a Ptolemy model");
            fileChooser.setFileFilter(_menu.getModelFilter());

            int returnVal = fileChooser.showOpenDialog(_parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                _modelFile = fileChooser.getSelectedFile();
                _modelFileField.setText(_modelFile.getAbsolutePath());
            }

        } else if (e.getSource() == _chooseLayoutButton) {
            //Handle layout button action.
            fileChooser.setDialogTitle("Choose a layout");
            fileChooser.setFileFilter(_menu.getLayoutFilter());

            int returnVal = fileChooser.showSaveDialog(_parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                _layoutFile = fileChooser.getSelectedFile();
                _layoutFileField.setText(_layoutFile.getAbsolutePath());
            }
        }
    }

    public Object showDialog() {
        Object[] content = new Object[] { _modelFileField, _chooseModelButton,
                _layoutFileField, _chooseLayoutButton };
        JOptionPane contentPane = new JOptionPane(content,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog openLayoutDialog = contentPane.createDialog(_parent,
                "Open existing layout");
        openLayoutDialog.pack();
        openLayoutDialog.setVisible(true);

        return contentPane.getValue();

    }

    public File getModelFile() {
        return _modelFile;
    }

    public File getLayoutFile() {
        return _layoutFile;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    private JTextField _modelFileField;
    private JTextField _layoutFileField;
    private JButton _chooseModelButton;
    private JButton _chooseLayoutButton;

    private JFrame _parent;

    private File _modelFile;
    private File _layoutFile;
    private HomerMenu _menu;
}
