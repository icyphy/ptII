/*
@Copyright (c) 2010-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptdb.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.DBModelNotFoundException;
import ptdb.common.exception.IllegalNameException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// RenameModelFrame

/**
 * The frame for the user to input the new name to rename a model.  There is
 *  a text box in this frame for the user to input the new name, an update
 *   button and a close button.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@SuppressWarnings("serial")
public class RenameModelFrame extends JFrame implements PTDBBasicFrame {

    /**
     * Construct the rename model frame.
     *
     * @param model The model to be renamed.
     * @param sourceFrame The source frame from which opened this frame.
     */
    public RenameModelFrame(NamedObj model, ActorGraphDBFrame sourceFrame) {

        super("Rename Model");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        _model = model;
        _sourceFrame = sourceFrame;

        addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) {
                _sourceFrame.setEnabled(false);

            }

            public void windowIconified(WindowEvent e) {
                // Do nothing.
            }

            public void windowDeiconified(WindowEvent e) {
                // Do nothing.
            }

            public void windowDeactivated(WindowEvent e) {
                // Do nothing.
            }

            public void windowClosing(WindowEvent e) {
                // Do nothing.

            }

            public void windowClosed(WindowEvent e) {
                _sourceFrame.setEnabled(true);
                _sourceFrame.toFront();

            }

            public void windowActivated(WindowEvent e) {
                // Do nothing.
            }
        });

        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        _topPanel = new JPanel();
        _bottomPanel = new JPanel();

        JLabel newNameLabel = new JLabel("New Model Name: ");
        newNameLabel.setAlignmentX(LEFT_ALIGNMENT);

        _topPanel.setAlignmentX(CENTER_ALIGNMENT);
        _bottomPanel.setAlignmentX(CENTER_ALIGNMENT);

        _topPanel.setAlignmentY(TOP_ALIGNMENT);
        _bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        _topPanel.setBorder(BorderFactory.createEmptyBorder());

        _newNameTextField = new JTextField();
        _newNameTextField.setPreferredSize(new Dimension(100, 20));

        _closeButton = new JButton("Close");

        _updateButton = new JButton("Update");

        _updateButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    _update();

                } catch (IllegalNameException e1) {
                    JOptionPane.showMessageDialog(RenameModelFrame.this,
                            e1.getMessage());
                }

            }
        });

        _updateButton.setMnemonic(KeyEvent.VK_ENTER);
        _closeButton.setMnemonic(KeyEvent.VK_ESCAPE);

        _updateButton.setActionCommand("Update");
        _closeButton.setActionCommand("Close");

        _updateButton.setHorizontalTextPosition(SwingConstants.CENTER);
        _closeButton.setHorizontalTextPosition(SwingConstants.CENTER);

        _closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }

        });

        _topPanel.add(newNameLabel);
        _topPanel.add(_newNameTextField);
        _bottomPanel.add(_updateButton);
        _bottomPanel.add(_closeButton);

        add(_topPanel);
        add(_bottomPanel);

        setResizable(false);

        validate();
        repaint();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Close this window.
     */

    public void closeFrame() {
        dispose();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Perform the update of the model renaming.
     *
     * @exception IllegalNameException Thrown if the new model name is illegal.
     */
    private void _update() throws IllegalNameException {

        String newName = _newNameTextField.getText();

        Utilities.checkModelName(newName);

        SaveModelManager saveModelManager = new SaveModelManager();

        XMLDBModel xmldbModel = new XMLDBModel(_model.getName());
        String oldName = _model.getName();

        xmldbModel.setModelId(Utilities.getIdFromModel(_model));

        try {

            saveModelManager.renameModel(xmldbModel, newName);

            _model.setName(newName);
            // Update the moml to update to the new model name.

            MoMLChangeRequest change = new MoMLChangeRequest(this, null,
                    _model.exportMoML());
            change.setUndoable(true);

            _model.requestChange(change);

            // If no exception thrown, show the update success message.
            JOptionPane.showMessageDialog(this, "Rename model successfully!");

            _sourceFrame.setTitle(_model.getName());

            try {

                ((ActorGraphDBFrame) _sourceFrame).updateDBModelHistory(
                        newName, false);
                ((ActorGraphDBFrame) _sourceFrame).updateDBModelHistory(
                        oldName, true);

            } catch (IOException e) {
                // Ignore if recent files are not updated.
            }

            _sourceFrame.repaint();

            dispose();

        } catch (IllegalArgumentException e) {
            MessageHandler.error("Unable to rename the model.", e);
        } catch (DBConnectionException e) {
            MessageHandler.error("Unable to rename the model.", e);
        } catch (DBExecutionException e) {
            MessageHandler.error("Unable to rename the model.", e);
        } catch (ModelAlreadyExistException e) {
            MessageHandler.error("Unable to rename the model.", e);
        } catch (DBModelNotFoundException e) {
            MessageHandler.error("Unable to rename the model.", e);
        } catch (IllegalActionException e) {
            MessageHandler.error("Unable to rename the model.", e);
        } catch (NameDuplicationException e) {
            MessageHandler.error("Unable to rename the model.", e);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JPanel _bottomPanel;

    private JButton _closeButton;

    private NamedObj _model;

    private JTextField _newNameTextField;

    private JFrame _sourceFrame;

    private JPanel _topPanel;

    private JButton _updateButton;

}
