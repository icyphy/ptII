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

import java.awt.Component;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * This is meant to be subclassed when you want to create a ComponentBuilder
 * that has simplistic behavior.
 *
 * @author Michael Connor mlconnor&#064;yahoo.com
@version $Id$
@since Ptolemy II 8.0
 */
public class DefaultComponentBuilder implements ComponentBuilder {
    Class clazz;

    BeanInfo beanInfo;

    List<PropertyDescriptor> editableProperties = new ArrayList<PropertyDescriptor>();

    Map<String, PropertyDescriptor> nameToDescriptor = new HashMap<String, PropertyDescriptor>();

    List<BeanProperty> properties = new ArrayList<BeanProperty>();

    /** Creates a new instance of DefaultComponentFactory */
    public DefaultComponentBuilder(Class clazz) throws IntrospectionException {
        this(clazz, null);
    }

    /** Creates a new instance of DefaultComponentFactory */
    public DefaultComponentBuilder(Class clazz, String[] properties)
            throws IntrospectionException {
        this.clazz = clazz;

        beanInfo = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] propertyDescriptors = beanInfo
                .getPropertyDescriptors();

        if (properties != null) {
            for (String propertyName : properties) {
                PropertyDescriptor propertyDescriptor = null;

                for (PropertyDescriptor thisDescriptor : propertyDescriptors) {
                    if (thisDescriptor.getName().equals(propertyName)) {
                        propertyDescriptor = thisDescriptor;
                        break;
                    }
                }

                if (propertyDescriptor == null) {
                    throw new RuntimeException("Could not find property '"
                            + propertyName + "' in class " + clazz.getName());
                } else {
                    this.properties.add(new BeanProperty(propertyDescriptor
                            .getName(), propertyDescriptor.getPropertyType()));
                    nameToDescriptor.put(propertyDescriptor.getName(),
                            propertyDescriptor);
                }
            }
        }
    }

    @Override
    public String getDeclaration(String name, Map<String, Object> beanProperties) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(clazz.getName() + " " + name + " = new "
                + clazz.getName() + "(");

        if (beanProperties.containsKey("text")) {
            buffer.append("\"" + (String) beanProperties.get("text") + "\"");
        }

        buffer.append(");\n");
        return buffer.toString();
    }

    @Override
    public ComponentDef getComponentDef(String name,
            Map<String, Object> beanProperties) {
        // imports
        // declarations
        // add

        String imp = "import " + clazz.getName() + ";";
        // does not work with JDK 1.4
        //          String decl = clazz.getSimpleName() + " ${name}= new " + clazz.getSimpleName() + "(";
        String decl = clazz.getName() + " ${name}= new " + clazz.getName()
                + "(";

        if (beanProperties.containsKey("text")) {
            decl += "\"" + (String) beanProperties.get("text") + "\"";
        }
        decl += ");";

        //          String decl = getDeclaration(name, beanProperties);
        String add = "${container}.add(${name}, \"${name}\");";

        ComponentDef cd = new ComponentDef(name, imp, decl, add);
        return cd;
    }

    @Override
    public java.awt.Component getInstance(
            java.util.Map<String, Object> objectProperties)
                    throws InstantiationException, IllegalAccessException,
                    InvocationTargetException {
        Object instance = clazz.newInstance();

        for (Object element : objectProperties.keySet()) {
            String key = (String) element;
            PropertyDescriptor propertyDescriptor = nameToDescriptor.get(key);
            Object value = objectProperties.get(key);
            Method writeMethod = propertyDescriptor.getWriteMethod();
            writeMethod.invoke(instance, new Object[] { value });
        }

        return (Component) instance;
    }

    @Override
    public boolean isComponentALayoutContainer() {
        return clazz.equals(JPanel.class);
    }

    @Override
    public String toString() {
        return clazz.getName();
    }

    @Override
    public List<BeanProperty> getProperties() {
        return properties;
    }

}
