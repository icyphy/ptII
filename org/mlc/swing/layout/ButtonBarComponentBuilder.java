/*
 * Copyright (c) 2004-2007 by Michael Connor. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of FormLayoutBuilder or Michael Connor nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mlc.swing.layout;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * This is the component builder for the JGoodies
 * ButtonBar.
 *
 * @author Michael Connor
@version $Id$
@since Ptolemy II 8.0
 */
public class ButtonBarComponentBuilder implements ComponentBuilder {
    private static final String left = "left";
    private static final String right = "right";
    private static final String center = "center";
    private static final String justification = "justification";
    List<BeanProperty> properties = new ArrayList<BeanProperty>();

    /** Creates a new instance of ButtonBarComponentBuilder */
    public ButtonBarComponentBuilder() throws IntrospectionException {
        properties.add(new BeanProperty("justification", String.class));
        properties.add(new BeanProperty("button1Name", String.class));
        properties.add(new BeanProperty("button1Text", String.class));
        properties.add(new BeanProperty("button2Name", String.class));
        properties.add(new BeanProperty("button2Text", String.class));
        properties.add(new BeanProperty("button3Name", String.class));
        properties.add(new BeanProperty("button3Text", String.class));
        properties.add(new BeanProperty("button4Name", String.class));
        properties.add(new BeanProperty("button4Text", String.class));
        properties.add(new BeanProperty("button5Name", String.class));
        properties.add(new BeanProperty("button5Text", String.class));
    }

    @Override
    public String getDeclaration(String name, Map<String, Object> properties) {
        StringBuffer declaration = new StringBuffer();
        StringBuffer buttonAdds = new StringBuffer(
                "new javax.swing.JButton[] {");

        int buttonCount = 0;

        for (int i = 1; i < 6; i++) {
            String buttonText = (String) properties.get("button" + i + "Text");
            String buttonName = (String) properties.get("button" + i + "Name");
            if (buttonText != null && buttonText.trim().length() > 0) {
                buttonCount++;
                if (buttonName == null) {
                    buttonName = name + "Button" + i;
                }

                declaration.append("javax.swing.JButton " + buttonName
                        + " = new javax.swing.JButton (\"" + buttonText
                        + "\");\n");
                buttonAdds.append(buttonName + ",");
            }
        }

        // let's remove the last comma from the buttonAdds
        if (buttonCount > 1) {
            buttonAdds.deleteCharAt(buttonAdds.length() - 1);
        }

        buttonAdds.append("}");
        declaration.append("java.awt.Component " + name
                + " = com.jgoodies.forms.factories.ButtonBarFactory.");
        String justificationValue = (String) properties.get(justification);

        if (justificationValue == null
                || justificationValue.trim().length() == 0) {
            justificationValue = right;
        }

        if (left.equals(justificationValue)) {
            declaration.append("buildRightAlignedBar");
        } else if (center.equals(justificationValue)) {
            declaration.append("buildCenteredBar");
        } else {
            declaration.append("buildRightAlignedBar");
        }

        declaration.append("(" + buttonAdds.toString() + ");\n");
        return declaration.toString();
    }

    @Override
    public String toString() {
        return "ButtonBar";
    }

    @Override
    public java.awt.Component getInstance(
            java.util.Map<String, Object> properties)
                    throws InstantiationException, IllegalAccessException,
                    InvocationTargetException {
        List<JButton> buttons = new ArrayList<JButton>();

        for (int i = 1; i < 6; i++) {
            String buttonText = (String) properties.get("button" + i + "Text");
            if (buttonText != null && buttonText.trim().length() > 0) {
                buttons.add(new JButton(buttonText));
            }
        }

        JButton[] buttonArray = new JButton[buttons.size()];
        buttonArray = buttons.toArray(buttonArray);

        String justification = (String) properties.get("justification");
        if (justification == null || justification.trim().length() == 0) {
            justification = left;
        } else if (!justification.equals(left) && !justification.equals(right)
                && !justification.equals(center)) {
            throw new InstantiationException(
                    "justification should be either left, right, or center");
        }

        if (justification.equals(right)) {
            return ButtonBarFactory.buildRightAlignedBar(buttonArray);
        } else if (justification.equals(center)) {
            return ButtonBarFactory.buildCenteredBar(buttonArray);
        } else {
            return ButtonBarFactory.buildLeftAlignedBar(buttonArray);
        }
    }

    @Override
    public boolean isComponentALayoutContainer() {
        return false;
    }

    @Override
    public List<BeanProperty> getProperties() {
        return properties;
    }

    @Override
    public ComponentDef getComponentDef(String name,
            Map<String, Object> beanProperties) {
        String decl = getDeclaration("${name}", beanProperties);
        String add = "${container}.add(${name}, \"${name}\");";
        ComponentDef cd = new ComponentDef(name, "", decl, add);
        return cd;
    }

}
