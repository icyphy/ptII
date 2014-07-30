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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.IllegalNameException;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.save.AttributesManager;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// ConfigureAttributesFrame

/**
 * The window for the user to configure the user defined attributes for
 * Ptolemy models.  The user can add, delete, edit the attributes in this
 * window.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
@SuppressWarnings("serial")
public class ConfigureAttributesFrame extends JFrame implements PTDBBasicFrame {

    /**
     * Creates new form ConfigureAttributesFrame.
     *
     */
    public ConfigureAttributesFrame() {

        _attributeManager = new AttributesManager();

        _initComponents();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Close this frame.
     */

    @Override
    public void closeFrame() {
        _containedFramesManager.closeContainedFrames();

        dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addButtonActionPerformed(ActionEvent evt) {

        if (_checkChanged()) {
            // Ask to save the model or not.

            if (JOptionPane.showConfirmDialog(this,
                    "Do you want to save the change of this attribute?",
                    "Save Attribute", JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    _saveButtonActionPerformed(evt);

                } catch (IllegalNameException e) {
                    // Do nothing, the message has already been shown.
                    return;
                }
            }

        }

        _resetEditPanel();

    }

    private boolean _canEnableDeleteButton() {
        if (_currentEditedAttribute != null
                && !_currentEditedAttribute.getAttributeId().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean _canEnableSaveButton() {

        if (!_checkChanged()) {
            return false;
        }

        if (_attributeNameField.getText().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Check whether the attribute under editing has been changed.
     *
     * @return true - the attribute information has been changed.<br>
     *          false - the attribute information hasn't been changed.
     */
    private boolean _checkChanged() {

        if (_currentEditedAttribute == null
                || _currentEditedAttribute.getAttributeId() == null
                || _currentEditedAttribute.getAttributeId().isEmpty()) {
            // New attribute.
            if (_attributeNameField.getText().isEmpty()
                    && _attributeTypeField.getSelectedIndex() == 0
                    && _listItems == null) {
                return false;
            } else {
                return true;
            }

        } else {

            // Existing attribute.
            if (!_attributeNameField.getText().equals(
                    _currentEditedAttribute.getAttributeName())
                    || !_attributeTypeField.getSelectedItem().equals(
                            _currentEditedAttribute.getAttributeType())) {

                return true;
            } else {
                if (_attributeTypeField.getSelectedItem().equals(
                        XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {

                    if (_currentEditedAttribute.getAttributeValuesPlain() == null
                            && _listItems == null) {
                        return false;
                    }

                    if (_currentEditedAttribute.getAttributeValuesPlain() != null
                            && _listItems != null) {
                        if (_currentEditedAttribute.getAttributeValuesPlain()
                                .equals(_listItems)) {
                            return false;
                        } else {
                            return true;
                        }
                    }

                    return true;
                }
                return false;
            }
        }

    }

    private void _deleteButtonActionPerformed(ActionEvent evt) {

        int optionChoice = JOptionPane.showConfirmDialog(this,
                "Are you sure to delete this attribute?", "Delete Attribute",
                JOptionPane.YES_OPTION);

        if (optionChoice == JOptionPane.YES_OPTION) {
            try {
                _attributeManager.deleteAttribute(_currentEditedAttribute);

                _attributes.remove(_currentEditedAttribute);

                XMLDBAttribute tempAttribute = _currentEditedAttribute;

                _resetEditPanel();

                ((ArrayModelList) _attributesList.getModel())
                .removeItem(tempAttribute.getAttributeName());

                _statusMsgLabel.setText("Attribute was deleted successfully.");

            } catch (DBConnectionException e) {
                MessageHandler.error("Cannot delete this attribute now.", e);
            } catch (DBExecutionException e) {
                MessageHandler.error("Cannot delete this attribute now.", e);
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void _initComponents() {

        setTitle("Configure Attributes");

        setResizable(false);

        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
                // Do Nothing.
            }

            @Override
            public void windowIconified(WindowEvent e) {
                // Do Nothing.
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // Do Nothing.
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // Do Nothing.
            }

            @Override
            public void windowClosing(WindowEvent e) {
                _containedFramesManager.closeContainedFrames();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                _containedFramesManager.closeContainedFrames();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                if (_canEnableSaveButton()) {
                    _saveButton.setEnabled(true);
                }

            }
        });

        _mainDialog = new javax.swing.JDialog();
        //        _jTabbedPane1 = new javax.swing.JTabbedPane();
        _statusMsgLabel = new javax.swing.JLabel();
        _statusLabel = new javax.swing.JLabel();
        _editPanel = new javax.swing.JPanel();
        _nameLabel = new javax.swing.JLabel();
        _typeLabel = new javax.swing.JLabel();
        _deleteButton = new javax.swing.JButton();
        _attributeNameField = new javax.swing.JTextField();
        _attributeTypeField = new javax.swing.JComboBox();
        _saveButton = new javax.swing.JButton();
        _addButton = new javax.swing.JButton();
        _listEditButton = new javax.swing.JButton();
        _attributesPanel = new javax.swing.JPanel();
        _jScrollPane1 = new javax.swing.JScrollPane();
        _attributesList = new javax.swing.JList();
        _attributeNameLabel = new javax.swing.JLabel();
        _closeButton = new javax.swing.JButton();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(
                _mainDialog.getContentPane());
        _mainDialog.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(jDialog1Layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400,
                        Short.MAX_VALUE));
        jDialog1Layout.setVerticalGroup(jDialog1Layout.createParallelGroup(
                javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300,
                        Short.MAX_VALUE));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        //        _statusLabel.setFont(new Font("Title", Font.BOLD, 18)); // NOI18N
        _statusLabel.setText("Status:");

        _editPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        //        _nameLabel.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        _nameLabel.setText("Name");

        //        _typeLabel.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        _typeLabel.setText("Type");

        //        _deleteButton.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        _deleteButton.setForeground(new java.awt.Color(255, 0, 51));
        _deleteButton.setText("X");
        _deleteButton.setToolTipText("Delete the Current Attribute");

        _deleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _deleteButtonActionPerformed(evt);
            }
        });

        _attributeTypeField.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { XMLDBAttribute.ATTRIBUTE_TYPE_STRING,
                        XMLDBAttribute.ATTRIBUTE_TYPE_BOOLEAN,
                        XMLDBAttribute.ATTRIBUTE_TYPE_LIST }));

        _attributeTypeField.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (_attributeTypeField.getSelectedItem().equals(
                        XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
                    _listEditButton.setEnabled(true);
                } else {
                    _listEditButton.setEnabled(false);
                }

                _saveButton.setEnabled(_canEnableSaveButton());
            }
        });

        _saveButton.setText("<< Save");

        //        _addButton.setFont(new java.awt.Font("Lucida Grande", 1, 24)); // NOI18N
        _addButton.setForeground(new java.awt.Color(0, 153, 0));
        _addButton.setText("+");
        _addButton.setToolTipText("Add a New Attribute");

        _addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _addButtonActionPerformed(e);

            }
        });

        _listEditButton.setText("Edit");

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(
                _editPanel);
        _editPanel.setLayout(editPanelLayout);
        editPanelLayout
        .setHorizontalGroup(editPanelLayout
                .createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                editPanelLayout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        editPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(
                                                        _typeLabel)
                                                        .addComponent(
                                                                _nameLabel))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(
                                                                                editPanelLayout
                                                                                .createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                                        false)
                                                                                        .addGroup(
                                                                                                editPanelLayout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(
                                                                                                        _attributeTypeField,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        108,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                .addComponent(
                                                                                                                        _listEditButton,
                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                        64,
                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                        .addComponent(
                                                                                                                                _attributeNameField))
                                                                                                                                .addContainerGap(
                                                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                        Short.MAX_VALUE))
                                                                                                                                        .addGroup(
                                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                                editPanelLayout
                                                                                                                                                .createSequentialGroup()
                                                                                                                                                .addContainerGap(125, Short.MAX_VALUE)
                                                                                                                                                .addComponent(
                                                                                                                                                        _deleteButton,
                                                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                        49,
                                                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                        .addPreferredGap(
                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                        _addButton,
                                                                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                        51,
                                                                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                        .addGap(34, 34, 34))
                                                                                                                                                                        .addGroup(
                                                                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                                                                editPanelLayout.createSequentialGroup()
                                                                                                                                                                                .addContainerGap(100, Short.MAX_VALUE)
                                                                                                                                                                                .addComponent(_saveButton)
                                                                                                                                                                                .addGap(60, 60, 60)));

        editPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL,
                new java.awt.Component[] { _addButton, _deleteButton });

        editPanelLayout
        .setVerticalGroup(editPanelLayout
                .createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                editPanelLayout
                                .createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(
                                        editPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(
                                                        _deleteButton)
                                                        .addComponent(
                                                                _addButton,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                32,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(60, 60, 60)
                                                                .addGroup(
                                                                        editPanelLayout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(
                                                                                        _nameLabel)
                                                                                        .addComponent(
                                                                                                _attributeNameField))
                                                                                                .addGap(44, 44, 44)
                                                                                                .addGroup(
                                                                                                        editPanelLayout
                                                                                                        .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                .addComponent(
                                                                                                                        _listEditButton)
                                                                                                                        .addComponent(
                                                                                                                                _typeLabel)
                                                                                                                                .addComponent(
                                                                                                                                        _attributeTypeField))
                                                                                                                                        .addGap(58, 58, 58)
                                                                                                                                        .addComponent(_saveButton)
                                                                                                                                        .addGap(26, 26, 26)));

        editPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL,
                new java.awt.Component[] { _addButton, _deleteButton });

        _nameLabel.getAccessibleContext().setAccessibleName(
                "_attributeNameLabel");
        _typeLabel.getAccessibleContext().setAccessibleName(
                "attributeTypeLabel");
        _deleteButton.getAccessibleContext().setAccessibleName(
                "deleteAttributeButton");
        _deleteButton.setEnabled(false);

        _attributeNameField.getAccessibleContext().setAccessibleName(
                "_attributeNameField");

        _attributeNameField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // Do nothing here.
            }

            @Override
            public void keyReleased(KeyEvent e) {

                _saveButton.setEnabled(_canEnableSaveButton());

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Do nothing here.

            }
        });

        _attributeTypeField.getAccessibleContext().setAccessibleName(
                "_attributeTypeField");

        // Set the save button.
        _saveButton.getAccessibleContext().setAccessibleName("saveButton");
        _saveButton.setEnabled(false);

        _saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    _saveButtonActionPerformed(e);
                } catch (IllegalNameException e1) {
                    // Do nothing here, since the error message has been shown.
                }

            }
        });

        _listEditButton.getAccessibleContext().setAccessibleName(
                "_listEditButton");

        _listEditButton.setEnabled(false);

        _listEditButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (_listItems == null) {
                    _listItems = new ArrayList<String>();
                }

                AttributeListEditFrame attributeListEditFrame = new AttributeListEditFrame(
                        ConfigureAttributesFrame.this, _listItems,
                        _attributeNameField.getText());

                _containedFramesManager
                .addContainedFrame(attributeListEditFrame);

                attributeListEditFrame.setVisible(true);

            }
        });

        _attributesPanel.setBorder(javax.swing.BorderFactory
                .createEtchedBorder());

        // Get the existing attributes to set in the List.

        try {
            _attributes = _attributeManager.getDBAttributes();

            _attributesNames = new ArrayList<String>();

            for (Object element : _attributes) {
                XMLDBAttribute attribute = (XMLDBAttribute) element;
                _attributesNames.add(attribute.getAttributeName());
            }

            ListModel initialListModel = new ArrayModelList(_attributesNames);
            _attributesList.setModel(initialListModel);

            _jScrollPane1.setViewportView(_attributesList);
            _attributesList.getAccessibleContext().setAccessibleName(
                    "_attributesList");

        } catch (DBExecutionException e) {
            MessageHandler.error("Cannot retrive existing attributes.", e);
        } catch (DBConnectionException e) {
            MessageHandler.error("Cannot retrive existing attributes.", e);
        }

        _attributesList.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (_currentEditedAttribute == null
                        || !_attributesList.getSelectedValue().equals(
                                _currentEditedAttribute.getAttributeName())) {
                    boolean canChange = true;

                    if (_checkChanged()) {
                        if (JOptionPane
                                .showConfirmDialog(
                                        ConfigureAttributesFrame.this,
                                        "Do you want to save the change you have made to this attribute?",
                                        "Save Change", JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION) {

                            try {
                                _saveButtonActionPerformed(null);
                            } catch (IllegalNameException e1) {
                                canChange = false;
                            }
                        }
                    }

                    if (canChange) {
                        for (Object element : _attributes) {
                            XMLDBAttribute attribute = (XMLDBAttribute) element;
                            if (attribute.getAttributeName().equals(
                                    _attributesList.getSelectedValue())) {

                                _currentEditedAttribute = attribute;

                                _attributeNameField.setText(attribute
                                        .getAttributeName());
                                _attributeTypeField.setSelectedItem(attribute
                                        .getAttributeType());

                                if (attribute.getAttributeType().equals(
                                        XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
                                    _listEditButton.setEnabled(true);
                                    _listItems = new ArrayList<String>();
                                    _listItems.addAll(attribute
                                            .getAttributeValuesPlain());
                                }

                                _deleteButton
                                .setEnabled(_canEnableDeleteButton());

                                _saveButton.setEnabled(_canEnableSaveButton());

                                break;
                            }
                        }
                    } else {
                        _attributesList.setSelectedValue(
                                _currentEditedAttribute.getAttributeName(),
                                true);
                    }
                }

            }
        });

        //        _attributesList.addListSelectionListener(new ListSelectionListener() {
        //
        //
        //
        //            public void valueChanged(ListSelectionEvent e) {
        //
        //                if (_currentEditedAttribute == null
        //                        || !_attributesList.getSelectedValue().equals(
        //                                _currentEditedAttribute.getAttributeName())) {
        //                    boolean canChange = true;
        //
        //                    if (_checkChanged()) {
        //                        if (JOptionPane
        //                                .showConfirmDialog(
        //                                        ConfigureAttributesFrame.this,
        //                                        "Do you want to save the change you have made to this attribute?",
        //                                        "Save Change", JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION) {
        //
        //                            try {
        //                                _saveButtonActionPerformed(null);
        //                            } catch (IllegalNameException e1) {
        //                                canChange = false;
        //                            }
        //                        }
        //                    }
        //
        //                    if (canChange) {
        //                        for (Iterator iterator = _attributes.iterator(); iterator
        //                                .hasNext();) {
        //                            XMLDBAttribute attribute = (XMLDBAttribute) iterator
        //                                    .next();
        //                            if (attribute.getAttributeName().equals(
        //                                    _attributesList.getSelectedValue())) {
        //
        //                                _currentEditedAttribute = attribute;
        //
        //                                _attributeNameField.setText(attribute
        //                                        .getAttributeName());
        //                                _attributeTypeField.setSelectedItem(attribute
        //                                        .getAttributeType());
        //
        //                                if (attribute.getAttributeType().equals(
        //                                        XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
        //                                    _listEditButton.setEnabled(true);
        //                                    _listItems = new ArrayList<String>();
        //                                    _listItems.addAll(attribute
        //                                            .getAttributeValuesPlain());
        //                                }
        //
        //                                _deleteButton
        //                                        .setEnabled(_canEnableDeleteButton());
        //
        //                                _saveButton.setEnabled(_canEnableSaveButton());
        //
        //                                break;
        //                            }
        //                        }
        //                    } else {
        //                        _attributesList.setSelectedValue(
        //                                _currentEditedAttribute.getAttributeName(),
        //                                true);
        //                    }
        //                }
        //
        //            }
        //        });

        _attributeNameLabel.setFont(new Font("Title", Font.BOLD, 12)); // NOI18N
        _attributeNameLabel.setText("  List of Available Attributes");

        javax.swing.GroupLayout attributesPanelLayout = new javax.swing.GroupLayout(
                _attributesPanel);
        _attributesPanel.setLayout(attributesPanelLayout);
        attributesPanelLayout
        .setHorizontalGroup(attributesPanelLayout
                .createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                attributesPanelLayout
                                .createSequentialGroup()
                                .addGroup(
                                        attributesPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(
                                                        _attributeNameLabel,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        211,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(
                                                                attributesPanelLayout
                                                                .createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(
                                                                        _jScrollPane1,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        209,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                        .addContainerGap(
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)));
        attributesPanelLayout
        .setVerticalGroup(attributesPanelLayout
                .createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                attributesPanelLayout
                                .createSequentialGroup()
                                .addComponent(
                                        _attributeNameLabel,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        36,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(
                                                        _jScrollPane1,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        269,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addContainerGap(10, Short.MAX_VALUE)));

        _attributeNameLabel.getAccessibleContext().setAccessibleName(
                "_attributeNameLabel");

        _closeButton.setText("Close");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                layout.createSequentialGroup()
                                                .addGap(40, 40,
                                                        40)
                                                        .addComponent(
                                                                _attributesPanel,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(
                                                                                _editPanel,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                260,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                //                                                                        .addGap(
                                                                                //                                                                                397,
                                                                                //                                                                                397,
                                                                                //                                                                                397)
                                                                                //                                                                        .addComponent(
                                                                                //                                                                                _jTabbedPane1,
                                                                                //                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                //                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                //                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addGroup(
                                                                                        layout.createSequentialGroup()
                                                                                        .addGap(51, 51,
                                                                                                51)
                                                                                                .addComponent(
                                                                                                        _statusLabel)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                .addComponent(
                                                                                                                        _statusMsgLabel,
                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                        263,
                                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                        .addGap(37, 37,
                                                                                                                                37)
                                                                                                                                .addComponent(
                                                                                                                                        _closeButton)))
                                                                                                                                        .addContainerGap(
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                Short.MAX_VALUE)));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                        //                                                        .addGroup(
                                        //                                                                layout
                                        //                                                                        .createSequentialGroup()
                                        //                                                                        .addGap(
                                        //                                                                                176,
                                        //                                                                                176,
                                        //                                                                                176)
                                        //                                                                        .addComponent(
                                        //                                                                                _jTabbedPane1,
                                        //                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                        //                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                        //                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                layout.createSequentialGroup()
                                                .addGap(40, 40,
                                                        40)
                                                        .addGroup(
                                                                layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                        .addComponent(
                                                                                _editPanel,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                                .addComponent(
                                                                                        _attributesPanel,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        327,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                                                        .addGroup(
                                                                                                layout.createParallelGroup(
                                                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                        .addGroup(
                                                                                                                layout.createSequentialGroup()
                                                                                                                .addGap(41, 41,
                                                                                                                        41)
                                                                                                                        .addGroup(
                                                                                                                                layout.createParallelGroup(
                                                                                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                                                        .addComponent(
                                                                                                                                                _statusMsgLabel,
                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                29,
                                                                                                                                                Short.MAX_VALUE)
                                                                                                                                                .addComponent(
                                                                                                                                                        _statusLabel,
                                                                                                                                                        javax.swing.GroupLayout.Alignment.LEADING)))
                                                                                                                                                        .addGroup(
                                                                                                                                                                layout.createSequentialGroup()
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                        .addComponent(
                                                                                                                                                                                _closeButton)))
                                                                                                                                                                                .addGap(32, 32, 32)));

        _statusMsgLabel.getAccessibleContext().setAccessibleName(
                "_statusMsgLabel");
        _statusLabel.getAccessibleContext().setAccessibleName("_statusLabel");

        // Set the close button.
        _closeButton.getAccessibleContext().setAccessibleName("_closeButton");

        _closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (_checkChanged()) {

                    Object[] options = { "Yes", "No", "Cancel" };

                    String msg = "The current attribute was modified, do you want to save it?";

                    int selected = JOptionPane.showOptionDialog(
                            ConfigureAttributesFrame.this, msg, "Save Changes",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);

                    if (selected == JOptionPane.YES_OPTION) {

                        _saveButton.getActionListeners()[0]
                                .actionPerformed(null);

                        ConfigureAttributesFrame.this.dispose();

                    } else if (selected == JOptionPane.NO_OPTION) {

                        ConfigureAttributesFrame.this.dispose();

                    } else if (selected == JOptionPane.CANCEL_OPTION) {

                        // Do nothing.
                    }

                } else {

                    ConfigureAttributesFrame.this.dispose();
                }

            }
        });

        pack();
    }

    private void _resetEditPanel() {
        _currentEditedAttribute = null;

        _attributeNameField.setText("");
        _attributeTypeField.setSelectedIndex(0);
        _listItems = null;

        _saveButton.setEnabled(_canEnableSaveButton());
        _deleteButton.setEnabled(_canEnableDeleteButton());

    }

    private void _saveButtonActionPerformed(ActionEvent evt)
            throws IllegalNameException {

        try {
            _validate();
        } catch (IllegalNameException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());

            throw e;
        }

        //        if (!_validate()) {

        //            JOptionPane
        //                    .showMessageDialog(
        //                            this,
        //                            "Empty or duplicated attribute name! Please change to other name.",
        //                            "Invalid Attribute Name", JOptionPane.ERROR_MESSAGE);
        //            throw new IllegalNameException(
        //                    "Empty or duplication attribute name.");
        //        } else {
        if (_currentEditedAttribute != null
                && _currentEditedAttribute.getAttributeId() != null
                && !_currentEditedAttribute.getAttributeId().isEmpty()) {

            // Existing attribute
            boolean nameChanged = false;

            if (!_currentEditedAttribute.getAttributeName().equals(
                    _attributeNameField.getText())) {
                nameChanged = true;
            }
            _currentEditedAttribute.setAttributeName(_attributeNameField
                    .getText());
            _currentEditedAttribute
            .setAttributeType((String) _attributeTypeField
                    .getSelectedItem());

            if (_attributeTypeField.getSelectedItem().equals(
                    XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
                if (_listItems == null) {
                    _listItems = new ArrayList<String>();
                }
                _currentEditedAttribute.setAttributeValuesPlain(_listItems);
            }

            try {
                _attributeManager.updateAttribute(_currentEditedAttribute);

                _statusMsgLabel.setText("Updated successfully.");

                // Only update the list when the attribute name is changed.
                if (nameChanged) {
                    int newIndex = ((ArrayModelList) _attributesList.getModel())
                            .updateItem(
                                    _currentEditedAttribute.getAttributeName(),
                                    _attributesList.getSelectedIndex());
                    _attributesList.setSelectedIndex(newIndex);
                }

                // Update the stored list in this class.
                int index = 0;
                for (Object element : _attributes) {
                    XMLDBAttribute attribute = (XMLDBAttribute) element;
                    if (attribute.getAttributeId().equals(
                            _currentEditedAttribute.getAttributeId())) {
                        _attributes.set(index, _currentEditedAttribute);

                        break;
                    }

                    index++;
                }
                _resetEditPanel();
            } catch (DBConnectionException e) {
                MessageHandler.error("Cannot update the attribute now.", e);
            } catch (DBExecutionException e) {
                MessageHandler.error("Cannot update the attribute now.", e);
            }

        } else {

            // New attribute.
            XMLDBAttribute newXmldbAttribute = new XMLDBAttribute(
                    _attributeNameField.getText(),
                    (String) _attributeTypeField.getSelectedItem());

            if (_attributeTypeField.getSelectedItem().equals(
                    XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
                if (_listItems == null) {
                    _listItems = new ArrayList<String>();
                }

                newXmldbAttribute.setAttributeValuesPlain(_listItems);
            }

            try {
                newXmldbAttribute = _attributeManager
                        .createAttribute(newXmldbAttribute);

                _statusMsgLabel
                .setText("The new attribute is saved successfully.");

                _resetEditPanel();

                ((ArrayModelList) _attributesList.getModel())
                .addItem(newXmldbAttribute.getAttributeName());

                _attributes.add(newXmldbAttribute);

            } catch (DBConnectionException e) {
                MessageHandler.error("Cannot save the attribute now.", e);
            } catch (DBExecutionException e) {
                MessageHandler.error("Cannot save the attribute now.", e);
            }
        }
        //        }

    }

    /**
     * Validate whether the edited attribute is valid to save or update to the
     * database.
     *
     * @return true - the edited attribute is valid to save.<br>
     *          false - the edited attribute is invalid to save.
     * @exception IllegalNameException Thrown if the attribute name is illegal.
     */
    private boolean _validate() throws IllegalNameException {

        Utilities.checkAttributeName(_attributeNameField.getText());

        for (Object element : _attributes) {
            XMLDBAttribute existingAttribute = (XMLDBAttribute) element;

            if (existingAttribute.getAttributeName().equals(
                    _attributeNameField.getText())) {

                if (_currentEditedAttribute == null
                        || _currentEditedAttribute.getAttributeId() == null
                        || _currentEditedAttribute.getAttributeId().isEmpty()) {
                    // New attribute
                    throw new IllegalNameException("Duplicated name!");
                } else {
                    // Existing attribute.
                    if (existingAttribute.getAttributeId().equals(
                            _currentEditedAttribute.getAttributeId())) {
                        // Same attribute.
                        return true;
                    } else {
                        throw new IllegalNameException("Duplicated name!");
                    }
                }

            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Swing components
    private javax.swing.JButton _addButton;
    private javax.swing.JTextField _attributeNameField;
    private javax.swing.JLabel _attributeNameLabel;
    private javax.swing.JComboBox _attributeTypeField;
    private javax.swing.JList _attributesList;
    private javax.swing.JPanel _attributesPanel;
    private javax.swing.JButton _closeButton;
    private javax.swing.JButton _deleteButton;
    private javax.swing.JPanel _editPanel;
    private javax.swing.JButton _saveButton;
    private javax.swing.JDialog _mainDialog;
    private javax.swing.JScrollPane _jScrollPane1;
    //    private javax.swing.JTabbedPane _jTabbedPane1;
    private javax.swing.JButton _listEditButton;
    private javax.swing.JLabel _nameLabel;
    private javax.swing.JLabel _statusLabel;
    private javax.swing.JLabel _statusMsgLabel;
    private javax.swing.JLabel _typeLabel;

    /**
     * The attribute manager that will handle all the attributes related
     * requests.
     */
    private AttributesManager _attributeManager;

    private List<String> _attributesNames;

    /**
     * The list of attributes configured by the user.
     */
    private List<XMLDBAttribute> _attributes;

    /**
     * The attribute that is currently being edited.
     */
    private XMLDBAttribute _currentEditedAttribute;

    private List<String> _listItems;

    private PTDBContainedFramesManager _containedFramesManager = new PTDBContainedFramesManager();

}
