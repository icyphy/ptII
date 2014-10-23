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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.save.AttributesManager;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// AttributesListPanel

/**
 * A reusable panel that accepts the model name and allows adding, removing,
 * and changing of database attributes.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */

@SuppressWarnings("serial")
public class AttributesListPanel extends JPanel {

    /**
     * Construct a AttributesListPanel. Add swing Components to the panel. Add a
     * listener for the "Add Attribute" button, which adds a ModelAttributePanel
     * to the tabbed pane and delete buttons that are mapped to each
     * ModelAttributePanel.
     *
     * @param model The model that is being saved to the database.
     *
     */
    public AttributesListPanel(NamedObj model) {

        _model = model;
        _aList = new HashMap();
        _AttDelete = new HashMap();
        _modified = false;
        _currentText = "";

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        _nameText = new JTextField(model.getName());
        _attListPanel = new JPanel();
        _scrollPane = new JScrollPane(_attListPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        _nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        _modelNamePanel.setAlignmentX(LEFT_ALIGNMENT);
        _innerPanel.setAlignmentX(LEFT_ALIGNMENT);
        _nameText.setAlignmentX(LEFT_ALIGNMENT);
        _topPanel.setAlignmentX(LEFT_ALIGNMENT);
        _attListPanel.setAlignmentX(LEFT_ALIGNMENT);
        _scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        _bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        _nameLabel.setAlignmentY(TOP_ALIGNMENT);
        _modelNamePanel.setAlignmentY(TOP_ALIGNMENT);
        _innerPanel.setAlignmentY(TOP_ALIGNMENT);
        _nameText.setAlignmentY(TOP_ALIGNMENT);
        _topPanel.setAlignmentY(TOP_ALIGNMENT);
        _attListPanel.setAlignmentY(TOP_ALIGNMENT);
        _scrollPane.setAlignmentY(TOP_ALIGNMENT);
        _bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        _topPanel.setLayout(new BoxLayout(_topPanel, BoxLayout.Y_AXIS));
        _modelNamePanel.setLayout(new BoxLayout(_modelNamePanel,
                BoxLayout.X_AXIS));
        _innerPanel.setLayout(new BoxLayout(_innerPanel, BoxLayout.Y_AXIS));
        _attListPanel.setLayout(new BoxLayout(_attListPanel, BoxLayout.Y_AXIS));
        _scrollPane.setLayout(new ScrollPaneLayout());
        _scrollPane.setPreferredSize(new Dimension(800, 300));

        _modelNamePanel.setMaximumSize(new Dimension(300, 20));
        _nameText.setPreferredSize(new Dimension(100, 20));
        _nameLabel.setPreferredSize(new Dimension(70, 20));

        _topPanel.setBorder(BorderFactory.createEmptyBorder());
        _nameLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        _modelNamePanel.add(_nameLabel);
        _modelNamePanel.add(_nameText);
        _innerPanel.add(_modelNamePanel);
        _innerPanel.add(_scrollPane);

        try {

            AttributesManager attributeManager = new AttributesManager();
            List<XMLDBAttribute> xmlAttList = attributeManager
                    .getDBAttributes();

            for (XMLDBAttribute a : xmlAttList) {

                _aList.put(a.getAttributeName(), a);

            }

        } catch (DBExecutionException e) {

            MessageHandler.error(
                    "Could not retrieve attributes from the database.", e);

        } catch (DBConnectionException e) {

            MessageHandler.error(
                    "Could not retrieve attributes from the database.", e);

        }

        JButton add_Button = new JButton("Add Attribute");
        add_Button.setMnemonic(KeyEvent.VK_A);
        add_Button.setActionCommand("Add Attribute");
        add_Button.setHorizontalTextPosition(SwingConstants.CENTER);

        add_Button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                addAttribute(null);
                setModified(true);

            }
        });

        _nameText.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent arg0) {
                // Do nothing.

            }

            @Override
            public void focusLost(FocusEvent arg0) {

                if (!_nameText.getText().equals(_currentText)) {
                    setModified(true);
                    _currentText = _nameText.getText();
                }

            }

        });

        _topPanel.add(_innerPanel);
        _bottomPanel.add(add_Button);
        add(_topPanel);
        add(_bottomPanel);
        validate();
        repaint();

    }

    /** Add a database attribute to the panel for display.  An associated
     *  delete button is also created.
     *
     * @param stringParameter
     *          The parameter to add to the panel for display.
     */
    public void addAttribute(StringParameter stringParameter) {

        JPanel modelDeletePanel = new JPanel();
        modelDeletePanel.setLayout(new BoxLayout(modelDeletePanel,
                BoxLayout.X_AXIS));
        modelDeletePanel.setAlignmentX(LEFT_ALIGNMENT);
        modelDeletePanel.setAlignmentY(TOP_ALIGNMENT);

        ModelAttributePanel modelAttPanel = new ModelAttributePanel(_aList);

        // Set a value if there is one.
        if (stringParameter != null) {

            modelAttPanel.setAttributeName(stringParameter.getName());
            modelAttPanel.setValue(stringParameter.getExpression());

        } else {

            modelAttPanel.setAttributeName("");
        }

        JButton deleteButton = new JButton("Delete");
        deleteButton.setAlignmentY(TOP_ALIGNMENT);

        deleteButton.setActionCommand("Delete");
        deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);

        modelDeletePanel.add(modelAttPanel);
        modelDeletePanel.add(deleteButton);

        _AttDelete.put(deleteButton, modelDeletePanel);

        _attListPanel.add(modelDeletePanel);
        _attListPanel.setMaximumSize(getMinimumSize());

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                _attListPanel.remove((JPanel) _AttDelete.get(event.getSource()));
                _AttDelete.remove(event.getSource());
                _attListPanel.remove((JButton) event.getSource());

                validate();
                repaint();

                setModified(true);

            }

        });

        validate();
        repaint();

    }

    /** Get an ArrayList of all displayed attributes as Attribute objects.
     *
     * @return An ArrayList of Attributes that are present in the display.
     *          Thrown if more than one attribute added to the display, has the
     *          same name.
     * @exception IllegalActionException
     *          Thrown if a problem occurs creating the attribute objects.
     */
    protected ArrayList<Attribute> getAttributes()
            throws IllegalActionException {

        ArrayList<Attribute> returnList = new ArrayList();

        // Get a list of all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        StringParameter stringParameter;
                        try {
                            stringParameter = new StringParameter(_model,
                                    ((ModelAttributePanel) element2)
                                    .getAttributeName());
                        } catch (NameDuplicationException e) {
                            stringParameter = (StringParameter) _model
                                    .getAttribute(((ModelAttributePanel) element2)
                                            .getAttributeName());
                        }
                        stringParameter
                        .setExpression(((ModelAttributePanel) element2)
                                .getValue());
                        returnList.add(stringParameter);

                    }

                }

            }

        }

        return returnList;

    }

    /** Get the model name.
     *
     * @return The model name.
     * @see #setModelName(String name)
     */
    public String getModelName() {

        return _nameText.getText();

    }

    /** Set the model name.
     * @param name The name to be set
     * @see #getModelName()
     */
    public void setModelName(String name) {

        _nameText.setText(name);
        _currentText = _nameText.getText();
    }

    /**
     * Set the focus on the model name text field.
     */
    public void setModelNameFocus() {
        _nameText.requestFocus();
    }

    /** Get an indication if the specified attribute name is in the set of
     *  database attributes.
     *
     * @param attributeName
     *          The name of the attribute to check.
     * @return
     *          An indication if the specified name is in the set of
     *          database attributes (true if is. false if it is not).
     */
    public boolean isDBAttribute(String attributeName) {

        return _aList.containsKey(attributeName);

    }

    /** Get an indication if the panel contains duplicate attributes.
     * @return
     *          An indication if the panel contains duplicate attributes
     *           (true if does. false if it does not).
     *
     */
    public boolean containsDuplicates() {

        ArrayList<String> attributes = new ArrayList();

        // Get a list of all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        attributes.add(((ModelAttributePanel) element2)
                                .getAttributeName());

                    }

                }

            }

        }

        // Check for duplicate attributes.
        HashSet set = new HashSet();
        for (int i = 0; i < attributes.size(); i++) {

            boolean val = set.add(attributes.get(i));
            if (val == false) {

                return true;

            }

        }

        return false;
    }

    /** Get an indication if all attributes in the panel have names.
     * @return
     *          An indication if all attributes in the panel have names.
     *           (true they do. false if they do not).
     *
     */
    protected boolean allAttributeNamesSet() {

        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        if (((ModelAttributePanel) element2).getAttributeName()
                                .equals("")) {

                            return false;

                        }

                    }

                }

            }

        }

        return true;
    }

    /** Get an indication if all attributes in the panel have values.
     * @return
     *          An indication if all attributes in the panel have values.
     *           (true they do. false if they do not).
     *
     */
    protected boolean allAttributeValuesSet() {

        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        if (((ModelAttributePanel) element2).getValue().equals(
                                "")) {

                            return false;

                        }

                    }

                }

            }

        }

        return true;
    }

    /** Get the number of attributes displayed in the panel.
     * @return
     *          The number of attributes displayed in the panel.
     *
     */
    public int getAttributeCount() {

        return _AttDelete.size();

    }

    /**
     * Get the component of the text field to let the user input the model
     *  name.
     *
     * @return The JTextField instance for the model name.
     */
    public JTextField getNameTextField() {

        return _nameText;
    }

    /** Get an indication if the panel has been modified.
     *  True if it has, false if it hasn't.
     *
     * @return
     *         An indication if the panel has been modified.
     *
     * @see #setModified(boolean)
     *
     */
    public boolean isModified() {

        boolean panelsModified = false;

        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        if (panelsModified
                                || ((ModelAttributePanel) element2)
                                .isModified()) {
                            panelsModified = true;
                        }

                    }
                }
            }
        }

        // Note:  AttributeListPanel may already be modified if the
        // user has added or deleted ModelAttributePanels.  This is independent
        // of the modified variables contained in each individual
        // ModelAttributePanel.  However, if they have been modified, we
        // consider their container (this AttributeListPanel) to be modified
        // as well.
        if (panelsModified) {

            _modified = true;

        }

        return _modified;

    }

    /** Regroup the attributes displayed alphabetically.
     *
     */
    public void regroup() {

        ArrayList<ModelAttributePanel> orderedList = new ArrayList();
        ArrayList<GenericAttributePanel> orderedListGeneric = new ArrayList();

        // Get a list of all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        orderedList.add((ModelAttributePanel) element2);

                    } else if (element2 instanceof GenericAttributePanel) {

                        orderedListGeneric
                        .add((GenericAttributePanel) element2);

                    }

                }

            }

        }

        //Sort the panels.  A bubble sort is OK.  They are not likely to have
        //that many attributes to search on.
        boolean changeRequired = false;
        int n = orderedList.size();
        for (int pass = 1; pass < n; pass++) {

            for (int i = 0; i < n - pass; i++) {

                if (orderedList
                        .get(i)
                        .getAttributeName()
                        .compareToIgnoreCase(
                                orderedList.get(i + 1).getAttributeName()) > 0) {

                    changeRequired = true;
                    ModelAttributePanel temp = orderedList.get(i);
                    orderedList.set(i, orderedList.get(i + 1));
                    orderedList.set(i + 1, temp);

                }
            }
        }

        //Sort the Generic panels.
        boolean changeRequiredGeneric = false;
        int m = orderedListGeneric.size();
        for (int pass = 1; pass < m; pass++) {

            for (int i = 0; i < m - pass; i++) {

                if (orderedListGeneric
                        .get(i)
                        .getAttributeName()
                        .compareToIgnoreCase(
                                orderedListGeneric.get(i + 1)
                                .getAttributeName()) > 0) {

                    changeRequiredGeneric = true;
                    GenericAttributePanel temp = orderedListGeneric.get(i);
                    orderedListGeneric.set(i, orderedListGeneric.get(i + 1));
                    orderedListGeneric.set(i + 1, temp);

                }
            }
        }

        //If a change was required, remove all panels and re-add the
        //ordered panels
        if (changeRequired || changeRequiredGeneric) {

            _attListPanel.removeAll();
            _AttDelete.clear();

            JPanel modelDeletePanel;
            JButton deleteButton;

            for (int i = 0; i < orderedList.size(); i++) {

                modelDeletePanel = new JPanel();
                modelDeletePanel.setLayout(new BoxLayout(modelDeletePanel,
                        BoxLayout.X_AXIS));
                modelDeletePanel.setAlignmentX(LEFT_ALIGNMENT);
                modelDeletePanel.setAlignmentY(TOP_ALIGNMENT);

                deleteButton = new JButton("Delete");
                deleteButton.setAlignmentY(TOP_ALIGNMENT);
                deleteButton.setActionCommand("Delete");
                deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);

                modelDeletePanel.add(orderedList.get(i));
                modelDeletePanel.add(deleteButton);

                _AttDelete.put(deleteButton, modelDeletePanel);

                _attListPanel.add(modelDeletePanel);
                _attListPanel.setMaximumSize(getMinimumSize());

                deleteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {

                        _attListPanel.remove((JPanel) _AttDelete.get(event
                                .getSource()));
                        _AttDelete.remove(event.getSource());
                        _attListPanel.remove((JButton) event.getSource());

                        validate();
                        repaint();

                        setModified(true);

                    }

                });

                validate();
                repaint();

            }

            for (int i = 0; i < orderedListGeneric.size(); i++) {

                modelDeletePanel = new JPanel();
                modelDeletePanel.setLayout(new BoxLayout(modelDeletePanel,
                        BoxLayout.X_AXIS));
                modelDeletePanel.setAlignmentX(LEFT_ALIGNMENT);
                modelDeletePanel.setAlignmentY(TOP_ALIGNMENT);

                deleteButton = new JButton("Delete");
                deleteButton.setAlignmentY(TOP_ALIGNMENT);
                deleteButton.setActionCommand("Delete");
                deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);

                modelDeletePanel.add(orderedListGeneric.get(i));
                modelDeletePanel.add(deleteButton);

                _AttDelete.put(deleteButton, modelDeletePanel);

                _attListPanel.add(modelDeletePanel);
                _attListPanel.setMaximumSize(getMinimumSize());

                deleteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {

                        _attListPanel.remove((JPanel) _AttDelete.get(event
                                .getSource()));
                        _AttDelete.remove(event.getSource());
                        _attListPanel.remove((JButton) event.getSource());

                        validate();
                        repaint();

                        setModified(true);

                    }

                });

                validate();
                repaint();

            }

        }

    }

    /** Set the panel to modified or unmodified.
     *
     * @param modified True to set to modified.  False to set to unmodified.
     *
     * @see #isModified()
     *
     */
    public void setModified(boolean modified) {

        _modified = modified;

        // Propagate _modified to all contained ModelAttributePanels.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        ((ModelAttributePanel) element2).setModified(_modified);

                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                    protected variables                        ////

    /**
     * Top display panel.
     */
    protected JPanel _topPanel = new JPanel();
    /**
     * Bottom display panel.
     */
    protected JPanel _bottomPanel = new JPanel();
    /**
     * Inner display panel.
     */
    protected JPanel _innerPanel = new JPanel();
    /**
     * Panel displaying model name.
     */
    protected JPanel _modelNamePanel = new JPanel();
    /**
     * Label displaying the string "Model Name".
     */
    protected JLabel _nameLabel = new JLabel("Model Name");
    /**
     * Map between attributes and their delete buttons.
     */
    protected HashMap _AttDelete;
    /**
     * The panel containing attributes.
     */
    protected JPanel _attListPanel;
    /**
     * Map between attribute names and their display panel.
     */
    protected HashMap _aList;

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JScrollPane _scrollPane;
    private NamedObj _model;
    private JTextField _nameText;
    private boolean _modified;
    private String _currentText;

}
