/*  Represents a widget for attributes that are in the form of checkbox
 
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
package ptolemy.homer.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

/**
* Represents a widget for attributes that are in the form of checkbox
* @author Ishwinder Singh
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ishwinde)
* @Pt.AcceptedRating Red (ishwinde)
*/

public class AttributeStyleWidget extends GlassPaneWidget {

    public AttributeStyleWidget(Scene scene, PositionableElement element)
            throws NameDuplicationException, IllegalActionException {
        super(scene, element);
        NamedObj namedObject = element.getElement();
        List<ParameterEditorStyle> styles = namedObject
                .attributeList(ParameterEditorStyle.class);
        ParameterEditorStyle style = null;
        if (!styles.isEmpty()) {
            style = styles.get(0);
        }
        JComponent component;
        if (style instanceof CheckBoxStyle) {
            JCheckBox checkBox = new JCheckBox(namedObject.getName());
            if (namedObject instanceof Parameter) {
                Token token = ((Parameter) namedObject).getToken();
                if (token instanceof BooleanToken) {
                    checkBox.setSelected(((BooleanToken) token).booleanValue());
                }
            }
            component = checkBox;
        } else if (style instanceof ChoiceStyle) {
            // Will add combobox elements just to make it conceptually closes to the running UI.
            List<Settable> attributes = style.attributeList(Settable.class);
            JComboBox comboBox = new JComboBox(attributes.toArray());
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
            component = comboBox;
        } else if (style instanceof NotEditableLineStyle) {
            JLabel label = new JLabel();
            if (namedObject instanceof Settable) {
                label.setText(((Settable) namedObject).getExpression());
            }
            component = label;
        } else if (style instanceof LineStyle) {
            JScrollPane panel = new JScrollPane();
            JTextArea textArea = new JTextArea();
            panel.setViewportView(textArea);
            if (namedObject instanceof Settable) {
                textArea.setText(((Settable) namedObject).getExpression());
            }
            component = panel;
        } else {
            JTextField textField = new JTextField();
            if (namedObject instanceof Settable) {
                textField.setText(((Settable) namedObject).getExpression());
            }
            component = textField;
        }
        _containerPanel.add(component, BorderLayout.CENTER);
        setGlassPaneSize(component.getPreferredSize());
    }

}
