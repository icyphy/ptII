/*
 This widget visualizes attributes based on their style definition.

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
package ptolemy.homer.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.netbeans.api.visual.widget.Scene;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.actor.gui.style.LineStyle;
import ptolemy.actor.gui.style.NotEditableLineStyle;
import ptolemy.actor.gui.style.ParameterEditorStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// AttributeStyleWidget

/**
 * This widget visualizes attributes based on their style definition.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class AttributeStyleWidget extends GlassPaneWidget implements
MinSizeInterface {

    /**
     * Create a new instance visualizing the positionable element based on its style definitions.
     * If there is a no style defined, revert to a text field.
     * @param scene The scene containing the widget.
     * @param element The element to visualize.
     * @exception IllegalActionException if there is a problem parsing attribute's style.
     */
    @SuppressWarnings("serial")
    public AttributeStyleWidget(Scene scene, PositionableElement element)
            throws IllegalActionException {
        super(scene, element);

        NamedObj namedObject = getPositionableElement().getElement();
        List<ParameterEditorStyle> styles = namedObject
                .attributeList(ParameterEditorStyle.class);
        ParameterEditorStyle style = null;
        if (!styles.isEmpty()) {
            style = styles.get(0);
        }
        if (style instanceof CheckBoxStyle) {
            JCheckBox checkBox = new JCheckBox(namedObject.getName());
            _attributeComponent = checkBox;
        } else if (style instanceof ChoiceStyle) {
            JComboBox comboBox = new JComboBox();
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list,
                        Object value, int index, boolean isSelected,
                        boolean cellHasFocus) {
                    if (value instanceof Settable) {
                        value = ((Settable) value).getExpression();
                    }
                    return super.getListCellRendererComponent(list, value,
                            index, isSelected, cellHasFocus);
                }
            });
            _attributeComponent = comboBox;
        } else if (style instanceof NotEditableLineStyle) {
            JLabel label = new JLabel();
            _attributeComponent = label;
        } else if (style instanceof LineStyle) {
            JScrollPane panel = new JScrollPane();
            JTextArea textArea = new JTextArea();
            panel.setViewportView(textArea);
            _attributeComponent = panel;
        } else {
            JTextField textField = new JTextField();
            _attributeComponent = textField;
        }
        _attributeComponent.setBorder(new CompoundBorder(new EmptyBorder(1, 1,
                1, 1), _attributeComponent.getBorder()));
        updateValue();
        _containerPanel.add(_attributeComponent, BorderLayout.CENTER);
        setGlassPaneSize(_attributeComponent.getPreferredSize());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Update the value of the widget based on the attributes expression (if applicable).
     * @exception IllegalActionException if there is a problem parsing the attribute's expression.
     */
    public void updateValue() throws IllegalActionException {
        NamedObj namedObject = getPositionableElement().getElement();
        List<ParameterEditorStyle> styles = namedObject
                .attributeList(ParameterEditorStyle.class);
        ParameterEditorStyle style = null;
        if (!styles.isEmpty()) {
            style = styles.get(0);
        }
        if (style instanceof CheckBoxStyle) {
            JCheckBox checkBox = (JCheckBox) _attributeComponent;
            if (namedObject instanceof Parameter) {
                Token token = ((Parameter) namedObject).getToken();
                if (token instanceof BooleanToken) {
                    checkBox.setSelected(((BooleanToken) token).booleanValue());
                }
            }
        } else if (style instanceof ChoiceStyle) {
            // Will add combobox elements just to make it conceptually closes to the running UI.
            List<Settable> attributes = style.attributeList(Settable.class);
            JComboBox comboBox = (JComboBox) _attributeComponent;
            ArrayList<String> values = new ArrayList<String>();
            for (Settable value : attributes) {
                values.add(value.getExpression());
            }
            comboBox.setModel(new DefaultComboBoxModel(values.toArray()));
            if (namedObject instanceof Settable) {
                comboBox.setSelectedItem(((Settable) namedObject)
                        .getExpression());
            }
        } else if (style instanceof NotEditableLineStyle) {
            JLabel label = (JLabel) _attributeComponent;
            if (namedObject instanceof Settable) {
                label.setText(((Settable) namedObject).getExpression());
            }
        } else if (style instanceof LineStyle) {
            JScrollPane panel = (JScrollPane) _attributeComponent;
            JTextArea textArea = (JTextArea) panel.getViewport().getView();
            panel.setViewportView(textArea);
            if (namedObject instanceof Settable) {
                textArea.setText(((Settable) namedObject).getExpression());
            }
        } else {
            JTextField textField = (JTextField) _attributeComponent;
            if (namedObject instanceof Settable) {
                textField.setText(((Settable) namedObject).getExpression());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The component visualizing the positionable element.
     */
    private JComponent _attributeComponent;

    /**
     * Return minimal width which is the preferred width of the widget if it's defined.
     */
    @Override
    public Integer getMinWidth() {
        Dimension preferredSize = _attributeComponent.getPreferredSize();
        return preferredSize != null ? preferredSize.width : null;
    }

    /**
     * Return minimal height which is the preferred height of the widget if it's defined.
     */
    @Override
    public Integer getMinHeight() {
        Dimension preferredSize = _attributeComponent.getPreferredSize();
        return preferredSize != null ? preferredSize.height : null;
    }
}
