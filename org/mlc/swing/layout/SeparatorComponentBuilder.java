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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jgoodies.forms.factories.DefaultComponentFactory;

/**
 * This is the component builder for the JGoodies Separator.
 *
 * @author Michael Connor
@version $Id$
@since Ptolemy II 8.0
 */
public class SeparatorComponentBuilder implements ComponentBuilder {
    List<BeanProperty> properties = new ArrayList<BeanProperty>();

    private static final String TEXT = "text";

    /** Creates a new instance of SeparatorComponentBuilder */
    public SeparatorComponentBuilder() {
        properties.add(new BeanProperty(TEXT, String.class));
    }

    @Override
    public String getDeclaration(String name,
            java.util.Map<String, Object> beanProperties) {
        String text = (String) beanProperties.get(TEXT);
        if (text == null) {
            text = "";
        }
        return "java.awt.Component "
        + name
        + " = com.jgoodies.forms.factories.DefaultComponentFactory.getInstance().createSeparator(\""
        + text + "\");\n";
    }

    @Override
    public java.awt.Component getInstance(
            java.util.Map<String, Object> beanProperties) throws Exception {
        String text = (String) beanProperties.get(TEXT);
        if (text == null) {
            text = "";
        }
        return DefaultComponentFactory.getInstance().createSeparator(text);
    }

    @Override
    public java.util.List<BeanProperty> getProperties() {
        return properties;
    }

    @Override
    public boolean isComponentALayoutContainer() {
        return false;
    }

    @Override
    public String toString() {
        return "JGoodies separator";
    }

    @Override
    public ComponentDef getComponentDef(String name,
            Map<String, Object> beanProperties) {
        String imp = "";
        String decl = getDeclaration(name, beanProperties);
        String add = "${container}.add(${name}, \"${name}\");";
        ComponentDef cd = new ComponentDef(name, imp, decl, add);
        return cd;
    }

}
