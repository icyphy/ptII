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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

///////////////////////////////////////////////////////////////////
//// GenericAttributePanel

/**
 * An extended JPanel displaying a generic attribute for searching.
 * Multiple GenericAttributePanel objects may be displayed on the
 * SimpleSearchFrame.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */

public class GenericAttributePanel extends JPanel {

    /** Construct a GenericAttributePanel.  All components are
     * created and added to the panel.
     *
     */
    public GenericAttributePanel() {

        super();

        _modified = false;
        _currentNameText = "";
        _currentValueText = "";
        _currentClassText = "";

        setBorder(BorderFactory.createEtchedBorder());

        setMaximumSize(new Dimension(650, 30));
        setMinimumSize(getMaximumSize());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(TOP_ALIGNMENT);

        JLabel nameLabel = new JLabel(" Attribute ");
        nameLabel.setPreferredSize(new Dimension(50, 20));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        _attributeName = new JTextField();
        _attributeName.setPreferredSize(new Dimension(180, 20));
        _attributeName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(" Value ");
        valueLabel.setPreferredSize(new Dimension(40, 20));
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        _attributeValue = new JTextField();
        _attributeValue.setPreferredSize(new Dimension(180, 20));
        _attributeValue.setAlignmentX(LEFT_ALIGNMENT);

        JLabel classLabel = new JLabel(" Class ");
        classLabel.setPreferredSize(new Dimension(40, 20));
        classLabel.setAlignmentX(LEFT_ALIGNMENT);

        _attributeClass = new JTextField();
        _attributeClass.setPreferredSize(new Dimension(180, 20));
        _attributeClass.setAlignmentX(LEFT_ALIGNMENT);

        _attributeName.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                // Do nothing.

            }

            public void focusLost(FocusEvent arg0) {

                if (!_attributeName.getText().equals(_currentNameText)) {
                    setModified(true);
                    _currentNameText = _attributeName.getText();
                }

            }

        });

        _attributeValue.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                // Do nothing.

            }

            public void focusLost(FocusEvent arg0) {

                if (!_attributeValue.getText().equals(_currentValueText)) {
                    setModified(true);
                    _currentValueText = _attributeValue.getText();
                }

            }

        });

        _attributeClass.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                // Do nothing.

            }

            public void focusLost(FocusEvent arg0) {

                if (!_attributeClass.getText().equals(_currentClassText)) {
                    setModified(true);
                    _currentClassText = _attributeClass.getText();
                }

            }

        });

        add(nameLabel);
        add(_attributeName);
        add(valueLabel);
        add(_attributeValue);
        add(classLabel);
        add(_attributeClass);

    }

    ///////////////////////////////////////////////////////////////////
    //                    public methods                          ////

    /** Get the value of this attribute.
    *
    * @return
    *          The string value of the attribute,
    *          It is obtained from the applicable form Component based on
    *          the type mapped to the selected attribute.
    *
    */
    public String getValue() {

        return _attributeValue.getText();

    }

    /** Get the selected attribute's name.
     *
     * @return
     *          This attribute's name.
     *
     */
    public String getAttributeName() {

        return _attributeName.getText();

    }

    /** Get the selected attribute's class.
    *
    * @return
    *          This attribute's class.
    *
    */
    public String getAttributeClass() {

        return _attributeClass.getText();

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

        return _modified;

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

    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JTextField _attributeName;
    private JTextField _attributeValue;
    private JTextField _attributeClass;
    private boolean _modified;
    private String _currentNameText;
    private String _currentValueText;
    private String _currentClassText;

}
