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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * This is the ComponentBuilder that is used to build JToolBars.
 *
 * @author Michael Connor mlconnor&#064;yahoo.com
@version $Id$
@since Ptolemy II 8.0
 */
public class JToolBarComponentBuilder implements ComponentBuilder {
    List<BeanProperty> properties = new ArrayList<BeanProperty>();

    /** Creates a new instance of SeparatorComponentBuilder */
    public JToolBarComponentBuilder() {
    }

    @Override
    public String getDeclaration(String name,
            java.util.Map<String, Object> beanProperties) {
        return "JToolBar " + name + " = new JToolBar();\n";
    }

    @Override
    public java.awt.Component getInstance(
            java.util.Map<String, Object> beanProperties) throws Exception {
        JToolBar toolbar = new JToolBar();
        toolbar.add(new JButton(
                new ImageIcon(this.getClass().getResource("New24.gif"))));
        toolbar.add(new JButton(
                new ImageIcon(this.getClass().getResource("Remove24.gif"))));
        toolbar.add(new JButton(
                new ImageIcon(this.getClass().getResource("RowDelete24.gif"))));
        toolbar.add(new JButton(new ImageIcon(
                this.getClass().getResource("RowInsertAfter24.gif"))));

        return toolbar;
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
        return "javax.swing.JToolBar";
    }

    @Override
    public ComponentDef getComponentDef(String name,
            Map<String, Object> beanProperties) {
        String imp = "";
        String decl = "javax.swing.JToolbar ${name} = new javax.swing.JToolbar();";
        String add = "${container}.add(${name}, \"${name}\");";

        ComponentDef cd = new ComponentDef(name, imp, decl, add);
        return cd;
    }

}
