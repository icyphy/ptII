/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/*
 *
 */
package ptdb.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ptdb.common.exception.IllegalNameException;
import ptdb.common.util.Utilities;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// AdvancedSimpleSearchFrame

/**
 * The simple search frame to be opened in the advanced DB search window.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@SuppressWarnings("serial")
public class AdvancedSimpleSearchFrame extends JFrame {

    /**
     * Construct the AdvancedSimpleSearchFrame.
     *
     * @param parentFrame The parent frame that invokes this simple search
     * frame.
     */
    public AdvancedSimpleSearchFrame(JFrame parentFrame) {

        super("Simple Search Criteria");

        _parentFrame = parentFrame;

        // Disable the parent frame when this window is active.
        _parentFrame.setEnabled(false);

        setDefaultCloseOperation(HIDE_ON_CLOSE);

        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
                // Do nothing.

            }

            @Override
            public void windowIconified(WindowEvent e) {
                // Do nothing.
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // Do nothing.
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // Do nothing.
            }

            @Override
            public void windowClosing(WindowEvent e) {
                AdvancedSimpleSearchFrame.this.setVisible(false);
                _parentFrame.setEnabled(true);

            }

            @Override
            public void windowClosed(WindowEvent e) {
                AdvancedSimpleSearchFrame.this.setVisible(false);
                _parentFrame.setEnabled(true);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // Do nothing.
            }
        });

        // Configure the layout and panels of the frame.
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        _attributesListPanel = new AttributesListPanel(new NamedObj());

        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        _attributesListPanel.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        _attributesListPanel.setAlignmentY(TOP_ALIGNMENT);
        topPanel.setAlignmentY(TOP_ALIGNMENT);
        bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        topPanel.setBorder(BorderFactory.createEmptyBorder());

        JButton doneButtone = new JButton("Done");

        doneButtone.setMnemonic(KeyEvent.VK_ENTER);

        doneButtone.setActionCommand("Done");

        doneButtone.setHorizontalTextPosition(SwingConstants.CENTER);

        doneButtone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                try {

                    // If the form is in an invalid state, do not continue.
                    if (!_isValid()) {

                        return;

                    }

                    _attributes = _attributesListPanel.getAttributes();
                    _modelName = _attributesListPanel.getModelName();

                    AdvancedSimpleSearchFrame.this.setVisible(false);
                    _parentFrame.setEnabled(true);

                } catch (NameDuplicationException e) {

                    MessageHandler.error("The search cannot be performed now "
                            + "due to a NameDuplicationException.", e);

                } catch (IllegalActionException e) {

                    MessageHandler.error("The search cannot be performed now "
                            + "due to an IllegalActionException.", e);

                }

            }
        });

        topPanel.add(_attributesListPanel);
        bottomPanel.add(doneButtone);

        add(topPanel);
        add(bottomPanel);

        validate();
        repaint();

    }

    ///////////////////////////////////////////////////////////////////
    //                    public  methods                          ////

    /**
     * Get the attributes search criteria that the user specified in this
     * frame.
     *
     * @return The list of attributes search criteria.
     */
    public List<Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Get the model name search criteria that the user specified in this
     * frame.
     *
     * @return The model name search criteria.
     */
    public String getModelName() {
        return _modelName;
    }

    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////

    /**
     * Validate whether the search criteria that the user has input is valid or
     * not.
     *
     * @return true - the search criteria is valid.<br>
     *          false - the search criteria is invalid.
     * @exception NameDuplicationException Thrown if attributes with duplicated
     * names are found in the search criteria.
     * @exception IllegalActionException Thrown if the intend action is illegal.
     */
    private boolean _isValid() throws NameDuplicationException,
    IllegalActionException {

        if (_attributesListPanel.getAttributeCount() == 0
                && _attributesListPanel.getModelName().trim().isEmpty()) {

            return true;

        }

        if (!_attributesListPanel.getModelName().trim().isEmpty()) {

            try {
                Utilities.checkModelName(_attributesListPanel.getModelName());
            } catch (IllegalNameException e) {

                JOptionPane.showMessageDialog(this,
                        "The model name should only "
                                + "contain letters and numbers.",
                                "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

                return false;
            }

        }

        if (_attributesListPanel.containsDuplicates()) {

            JOptionPane.showMessageDialog(this,
                    "The search criteria cannot contain more"
                            + " than one instance " + "of the same attribute.",
                            "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (!_attributesListPanel.allAttributeNamesSet()) {

            JOptionPane.showMessageDialog(this,
                    "You must specify a name for all attributes.",
                    "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (!_attributesListPanel.allAttributeValuesSet()) {

            JOptionPane.showMessageDialog(this,
                    "You must specify a value for all attributes.",
                    "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private AttributesListPanel _attributesListPanel;
    private List<Attribute> _attributes;
    private String _modelName;
    private JFrame _parentFrame;

}
