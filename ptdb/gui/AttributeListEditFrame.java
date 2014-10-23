/*
@Copyright (c) 2010-2014 The Regents of the University of California.
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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ptdb.common.exception.IllegalNameException;
import ptdb.common.util.Utilities;

///////////////////////////////////////////////////////////////////
//// AttributeListEditFrame

/**
 * The frame for attribute list items editor window.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@SuppressWarnings("serial")
public class AttributeListEditFrame extends JFrame implements PTDBBasicFrame {

    /**
     * Creates new form AttributeListEditFrame.
     *
     * @param parentFrame The parent frame from which creates this frame.
     * @param listItems The list of items to be displayed and edited in this
     *  frame.
     *  @param listName The name of the list being edited.
     */
    public AttributeListEditFrame(ConfigureAttributesFrame parentFrame,
            List<String> listItems, String listName) {

        setTitle("Attribute List Editor");
        _parentFrame = parentFrame;
        _storedListItems = listItems;
        _listName = listName;
        _initComponents();

        _parentFrame.setEnabled(false);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Close the window.
     */

    @Override
    public void closeFrame() {
        dispose();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void _initComponents() {

        _listItemTextField = new JTextField();
        _jScrollPane1 = new JScrollPane();
        _itemsJList = new JList();
        _addButton = new JButton();
        _deleteButton = new JButton();
        _editListItemsLabel = new JLabel();
        _newItemLabel = new JLabel();
        _saveButton = new JButton();

        //        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setResizable(false);

        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
                // Do nothing special.

            }

            @Override
            public void windowIconified(WindowEvent e) {
                // Do nothing special.

            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // Do nothing special.

            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // Do nothing special.

            }

            @Override
            public void windowClosing(WindowEvent e) {
                _parentFrame.setEnabled(true);
                AttributeListEditFrame.this.dispose();

            }

            @Override
            public void windowClosed(WindowEvent e) {
                _parentFrame.setEnabled(true);
                AttributeListEditFrame.this.dispose();

            }

            @Override
            public void windowActivated(WindowEvent e) {
                // Do nothing special.

            }
        });

        _itemsJList.setModel(new ArrayModelList(_storedListItems));

        _itemsJList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                _deleteButton.setEnabled(true);

            }
        });

        _jScrollPane1.setViewportView(_itemsJList);

        _addButton.setText("Add >>");
        _addButton.setEnabled(false);

        _addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    _validate();

                    ((ArrayModelList) _itemsJList.getModel())
                    .addItem(_listItemTextField.getText());

                    _addButton.setEnabled(false);
                    _listItemTextField.setText("");

                } catch (IllegalNameException exception) {
                    JOptionPane.showMessageDialog(AttributeListEditFrame.this,
                            exception.getMessage(), "Invalid value Item",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        //        _deleteButton.setSize(100, );
        _deleteButton.setText("<< Delete");
        _deleteButton.setEnabled(false);

        _deleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((ArrayModelList) _itemsJList.getModel())
                .removeItem((String) _itemsJList.getSelectedValue());

                _deleteButton.setEnabled(false);

            }
        });

        _editListItemsLabel.setFont(new Font("Title", Font.BOLD, 12));
        _editListItemsLabel
        .setText("Edit List Items in " + _listName + " List");

        _newItemLabel.setText("New Item ");

        _saveButton.setText("Add to Attribute");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        javax.swing.GroupLayout.Alignment.TRAILING,
                        layout.createSequentialGroup()
                        .addContainerGap(50, Short.MAX_VALUE)
                        .addComponent(_newItemLabel)
                        .addComponent(_listItemTextField,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                143,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                false)
                                                .addComponent(
                                                        _deleteButton,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                        .addComponent(
                                                                _addButton,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE))
                                                                .addGap(18, 18, 18)
                                                                .addComponent(_jScrollPane1,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        156,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(62, 62, 62))
                                                                        .addGroup(
                                                                                layout.createSequentialGroup().addGap(28, 28, 28)
                                                                                .addComponent(_editListItemsLabel)
                                                                                .addContainerGap(380, Short.MAX_VALUE))
                                                                                .addGroup(
                                                                                        layout.createSequentialGroup()
                                                                                        .addGap(224, 224, 224)
                                                                                        .addComponent(_saveButton,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE).addGap(241, 241, 241)));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(_editListItemsLabel)
                        .addGap(76, 76, 76)
                        .addGroup(
                                layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(_newItemLabel)
                                        .addComponent(
                                                _listItemTextField,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(_addButton))
                                                .addGap(30, 30, 30)
                                                .addComponent(_deleteButton)
                                                .addPreferredGap(
                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        120, Short.MAX_VALUE)
                                                        .addComponent(_saveButton).addGap(48, 48, 48))
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                .addGap(74, 74, 74)
                                                                .addComponent(_jScrollPane1,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        202,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addContainerGap(105, Short.MAX_VALUE)));

        _listItemTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // Do nothing.
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (_listItemTextField.getText().isEmpty()) {
                    _addButton.setEnabled(false);
                } else {
                    _addButton.setEnabled(true);
                }

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Do nothing.
            }
        });

        _saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                _parentFrame.setEnabled(true);

                AttributeListEditFrame.this.dispose();

            }
        });

        pack();
    }

    /**
     * Validate whether the added item is valid to add to the list.
     *
     * @return true - It is valid to add that item.<br>
     *          false - It is invalid to add that item.
     * @exception IllegalNameException Thrown if the item value is legal.
     */
    private boolean _validate() throws IllegalNameException {

        try {
            Utilities.checkAttributeName(_listItemTextField.getText());
        } catch (IllegalNameException e) {
            throw new IllegalNameException(
                    "Illegal list item value! The list item value can"
                            + " only contain numbers and letters.", e);
        }

        ArrayModelList modelList = (ArrayModelList) _itemsJList.getModel();
        for (int i = 0; i < modelList.getSize(); i++) {
            if (_listItemTextField.getText().equals(modelList.getElementAt(i))) {
                throw new IllegalNameException("Duplicated item value!");
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private javax.swing.JButton _addButton;
    private javax.swing.JButton _deleteButton;
    private javax.swing.JLabel _editListItemsLabel;
    private javax.swing.JLabel _newItemLabel;
    private javax.swing.JList _itemsJList;
    private javax.swing.JScrollPane _jScrollPane1;
    private javax.swing.JTextField _listItemTextField;
    private javax.swing.JButton _saveButton;

    private List<String> _storedListItems;

    private ConfigureAttributesFrame _parentFrame;
    private String _listName;

}
