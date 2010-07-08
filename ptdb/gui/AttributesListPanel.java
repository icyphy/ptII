/*
@Copyright (c) 2010 The Regents of the University of California.
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
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 * 
 */

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

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        JPanel innerPanel = new JPanel();
        JPanel modelNamePanel = new JPanel();
        JLabel nameLabel = new JLabel("Model Name");

        _nameText = new JTextField(model.getName());
        _attListPanel = new JPanel();
        _scrollPane = new JScrollPane(_attListPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        modelNamePanel.setAlignmentX(LEFT_ALIGNMENT);
        innerPanel.setAlignmentX(LEFT_ALIGNMENT);
        _nameText.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        _attListPanel.setAlignmentX(LEFT_ALIGNMENT);
        _scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        nameLabel.setAlignmentY(TOP_ALIGNMENT);
        modelNamePanel.setAlignmentY(TOP_ALIGNMENT);
        innerPanel.setAlignmentY(TOP_ALIGNMENT);
        _nameText.setAlignmentY(TOP_ALIGNMENT);
        topPanel.setAlignmentY(TOP_ALIGNMENT);
        _attListPanel.setAlignmentY(TOP_ALIGNMENT);
        _scrollPane.setAlignmentY(TOP_ALIGNMENT);
        bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        modelNamePanel
                .setLayout(new BoxLayout(modelNamePanel, BoxLayout.X_AXIS));
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        _attListPanel.setLayout(new BoxLayout(_attListPanel, BoxLayout.Y_AXIS));
        _scrollPane.setLayout(new ScrollPaneLayout());
        _scrollPane.setPreferredSize(new Dimension(800, 300));

        modelNamePanel.setMaximumSize(new Dimension(300, 20));
        _nameText.setPreferredSize(new Dimension(100, 20));
        nameLabel.setPreferredSize(new Dimension(70, 20));

        topPanel.setBorder(BorderFactory.createEmptyBorder());
        nameLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        modelNamePanel.add(nameLabel);
        modelNamePanel.add(_nameText);
        innerPanel.add(modelNamePanel);
        innerPanel.add(_scrollPane);

        try {

            AttributesManager attributeManager = new AttributesManager();
            List<XMLDBAttribute> xmlAttList = new ArrayList();
            xmlAttList = attributeManager.getDBAttributes();

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

            public void actionPerformed(ActionEvent event) {

                addAttribute(null);

            }
        });

        topPanel.add(innerPanel);
        bottomPanel.add(add_Button);
        add(topPanel);
        add(bottomPanel);
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
            public void actionPerformed(ActionEvent event) {

                _attListPanel
                        .remove((JPanel) _AttDelete.get(event.getSource()));
                _AttDelete.remove(event.getSource());
                _attListPanel.remove((JButton) event.getSource());

                validate();
                repaint();

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
    public ArrayList<Attribute> getAttributes()
            throws IllegalActionException {

        ArrayList<Attribute> returnList = new ArrayList();

        // Get a list of all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (int i = 0; i < componentArray1.length; i++) {

            if (componentArray1[i] instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) componentArray1[i])
                        .getComponents();

                for (int j = 0; j < componentArray2.length; j++) {

                    if (componentArray2[j] instanceof ModelAttributePanel) {

                        StringParameter stringParameter;
                        try {
                            stringParameter = new StringParameter(
                                    _model,
                                    ((ModelAttributePanel) componentArray2[j])
                                            .getAttributeName());
                        } catch (NameDuplicationException e) {
                            stringParameter = (StringParameter)_model.getAttribute(((ModelAttributePanel) componentArray2[j])
                                            .getAttributeName());
                        }
                        stringParameter
                                .setExpression(((ModelAttributePanel) componentArray2[j])
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
     */
    public String getModelName() {

        return _nameText.getText();

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

        for (int i = 0; i < componentArray1.length; i++) {

            if (componentArray1[i] instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) componentArray1[i])
                        .getComponents();

                for (int j = 0; j < componentArray2.length; j++) {

                    if (componentArray2[j] instanceof ModelAttributePanel) {

                        attributes
                                .add(((ModelAttributePanel) componentArray2[j])
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
    public boolean allAttributeNamesSet() {

        Component[] componentArray1 = _attListPanel.getComponents();

        for (int i = 0; i < componentArray1.length; i++) {

            if (componentArray1[i] instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) componentArray1[i])
                        .getComponents();

                for (int j = 0; j < componentArray2.length; j++) {

                    if (componentArray2[j] instanceof ModelAttributePanel) {

                        if (((ModelAttributePanel) componentArray2[j])
                                .getAttributeName().equals("")) {

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
    public boolean allAttributeValuesSet() {

        Component[] componentArray1 = _attListPanel.getComponents();

        for (int i = 0; i < componentArray1.length; i++) {

            if (componentArray1[i] instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) componentArray1[i])
                        .getComponents();

                for (int j = 0; j < componentArray2.length; j++) {

                    if (componentArray2[j] instanceof ModelAttributePanel) {

                        if (((ModelAttributePanel) componentArray2[j])
                                .getValue().equals("")) {

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

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JPanel _attListPanel;
    private HashMap _aList;
    private JScrollPane _scrollPane;
    private HashMap _AttDelete;
    private NamedObj _model;
    private JTextField _nameText;

}
