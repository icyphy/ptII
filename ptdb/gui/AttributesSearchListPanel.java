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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ptdb.common.dto.PTDBSearchAttribute;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// AttributesSearchListPanel

/**
 * An extension of the AttributesListPanel to include a button for
 * adding a GenericAttributePanel.  This is used in the SimpleSearchFrame.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */

@SuppressWarnings("serial")
public class AttributesSearchListPanel extends AttributesListPanel {

    /**
     * Construct a AttributesSearchListPanel. Add swing Components to the panel.
     * Add a listener for the "Add Generic Attribute" button,
     * which adds a GenericAttributePanel to the tabbed pane and delete buttons
     * that are mapped to each GenericAttributePanel.
     *
     * @param model The model that is the context for inserting search results.
     *
     */
    public AttributesSearchListPanel(NamedObj model) {
        super(model);

        JButton _genericSearchButton = new JButton("Add Generic Attribute");
        _genericSearchButton.setMnemonic(KeyEvent.VK_G);
        _genericSearchButton.setActionCommand("Add Generic Attribute");
        _genericSearchButton.setHorizontalTextPosition(SwingConstants.CENTER);

        _genericSearchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                addGenericAttribute();
                setModified(true);

            }
        });

        _bottomPanel.add(_genericSearchButton);

    }

    /**
     * Add a GenericAttributePanel to the list of attributes to be searched.
     */
    public void addGenericAttribute() {

        JPanel modelDeletePanel = new JPanel();
        modelDeletePanel.setLayout(new BoxLayout(modelDeletePanel,
                BoxLayout.X_AXIS));
        modelDeletePanel.setAlignmentX(LEFT_ALIGNMENT);
        modelDeletePanel.setAlignmentY(TOP_ALIGNMENT);

        GenericAttributePanel genericAttPanel = new GenericAttributePanel();

        JButton deleteButton = new JButton("Delete");
        deleteButton.setAlignmentY(TOP_ALIGNMENT);

        deleteButton.setActionCommand("Delete");
        deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);

        modelDeletePanel.add(genericAttPanel);
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

    @Override
    protected ArrayList<Attribute> getAttributes()
            throws IllegalActionException {

        //ArrayList<Attribute> returnList = super.getAttributes();

        ArrayList<Attribute> returnList = new ArrayList();

        // Get a list of all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof ModelAttributePanel) {

                        try {

                            StringParameter stringParameter;
                            stringParameter = new StringParameter(
                                    new NamedObj(),
                                    ((ModelAttributePanel) element2)
                                    .getAttributeName());
                            stringParameter
                            .setExpression(((ModelAttributePanel) element2)
                                    .getValue());
                            returnList.add(stringParameter);

                        } catch (NameDuplicationException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }

        }

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof GenericAttributePanel) {

                        try {

                            PTDBSearchAttribute attribute = new PTDBSearchAttribute();

                            attribute.setGenericAttribute(true);

                            if (((GenericAttributePanel) element2)
                                    .getAttributeClass().length() > 0) {
                                attribute
                                .setGenericClassName(((GenericAttributePanel) element2)
                                        .getAttributeClass());
                            }

                            attribute
                            .setName(((GenericAttributePanel) element2)
                                    .getAttributeName());

                            //if ((((GenericAttributePanel) componentArray2[j])
                            //        .getValue()).length()>0) {
                            attribute
                            .setExpression(((GenericAttributePanel) element2)
                                    .getValue());
                            //}

                            returnList.add(attribute);

                        } catch (NameDuplicationException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }

        return returnList;

    }

    /** Get an indication if all attributes in the panel have names.
     * @return
     *          An indication if all attributes in the panel have names.
     *           (true they do. false if they do not).
     *
     */
    @Override
    public boolean allAttributeNamesSet() {

        if (!super.allAttributeNamesSet()) {
            return false;
        }

        Component[] componentArray1 = _attListPanel.getComponents();

        for (Component element : componentArray1) {

            if (element instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) element)
                        .getComponents();

                for (Component element2 : componentArray2) {

                    if (element2 instanceof GenericAttributePanel) {

                        if (((GenericAttributePanel) element2)
                                .getAttributeName().equals("")) {

                            return false;

                        }

                    }

                }

            }

        }

        return true;
    }

}
