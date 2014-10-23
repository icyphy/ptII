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
import java.awt.LayoutManager2;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class acts as a surrogate layout manager for the JGoodies
 * (www.jgoodies.com) FormLayout manager. This layout manager enables us to
 * associate names with components and then define the constraints for the
 * component elsewhere (like xml)
 *
 * @author Michael Connor
@version $Id$
@since Ptolemy II 8.0
 */
public class ContainerLayout implements LayoutManager2 {
    java.util.List<String> rowSpecs = new ArrayList<String>();

    java.util.List<String> columnSpecs = new ArrayList<String>();

    LinkedHashMap<String, CellConstraints> componentConstraints = new LinkedHashMap<String, CellConstraints>();

    Map<Component, String> componentsToNames = new HashMap<Component, String>();

    Map<String, Map<String, Object>> componentNameToCustomProps = new HashMap<String, Map<String, Object>>();

    Map<String, ComponentDef> componentNameToComponentDef = new HashMap<String, ComponentDef>();

    FormLayout formLayout;

    String name;

    public ContainerLayout(String name, String columnSpecs, String rowSpecs) {
        this.name = name;
        StringTokenizer cols = new StringTokenizer(columnSpecs, ",", false);
        StringTokenizer rows = new StringTokenizer(rowSpecs, ",", false);

        while (cols.hasMoreTokens()) {
            this.columnSpecs.add(cols.nextToken());
        }

        while (rows.hasMoreTokens()) {
            this.rowSpecs.add(rows.nextToken());
        }

        formLayout = new FormLayout(columnSpecs, rowSpecs);

    }

    private void buildLayout() throws IllegalArgumentException {
        formLayout = new FormLayout(delimit(columnSpecs), delimit(rowSpecs));

        // now we have to add all of the compenents to the new form
        for (Object element : componentsToNames.keySet()) {
            Component component = (Component) element;
            String componentName = componentsToNames.get(component);
            CellConstraints constraints = componentConstraints
                    .get(componentName);
            formLayout.addLayoutComponent(component, constraints);
        }
    }

    private String delimit(List<String> values) {
        StringBuffer buffer = new StringBuffer();

        for (int index = 0; index < values.size(); index++) {
            buffer.append(values.get(index));
            if (index < values.size() - 1) {
                buffer.append(",");
            }
        }

        return buffer.toString();
    }

    protected Map<Component, String> getComponentsToNames() {
        return Collections.unmodifiableMap(componentsToNames);
    }

    /**
     * Registers the value of the name property
     *
     * @param name
     *          The value of the property
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value of the name property
     *
     * @return The value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns custom properties for the component. If no custom props exist then
     * an empty map will be returned.
     */
    public Map<String, Object> getCustomProperties(String componentName) {
        return componentNameToCustomProps.containsKey(componentName) ? componentNameToCustomProps
                .get(componentName) : new HashMap<String, Object>();
    }

    /**
     * Set a user defined property for this component so that the tool can manage
     * the properties of the component thus reducing the burden on the user
     */
    public void setProperty(String componentName, String property, Object value) {
        Map customProps = componentNameToCustomProps.get(componentName);
        if (customProps == null) {
            customProps = new HashMap<String, Object>();
            componentNameToCustomProps.put(componentName, customProps);
        }
        customProps.put(property, value);
    }

    public void setCellConstraints(String componentName,
            CellConstraints constraints) {
        componentConstraints.put(name, constraints);

        for (Object element : componentsToNames.keySet()) {
            Component component = (Component) element;
            String thisName = componentsToNames.get(component);
            if (thisName.equals(componentName)) {
                formLayout.setConstraints(component, constraints);
                break;
            }
        }
    }

    public LinkedHashMap<String, CellConstraints> getCellConstraints() {
        return componentConstraints;
    }

    public void addComponent(String componentName, ComponentDef componentDef,
            CellConstraints constraints) {
        componentConstraints.put(componentName, constraints);
        componentNameToComponentDef.put(componentName, componentDef);
    }

    public ComponentDef getComponentDef(String componentName) {
        return componentNameToComponentDef.get(componentName);
    }

    public String getColumnSpecsString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator i = columnSpecs.iterator(); i.hasNext();) {
            buffer.append(i.next());
            if (i.hasNext()) {
                buffer.append(",");
            }
        }

        return buffer.toString();
    }

    public String getRowSpecsString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator i = rowSpecs.iterator(); i.hasNext();) {
            buffer.append(i.next());
            if (i.hasNext()) {
                buffer.append(",");
            }
        }

        return buffer.toString();
    }

    public int getRowCount() {
        return rowSpecs.size();
    }

    public int getColumnCount() {
        return columnSpecs.size();
    }

    public List<String> getRowSpecs() {
        return this.rowSpecs;
    }

    public List<String> getColumnSpecs() {
        return this.columnSpecs;
    }

    public void constraintsChanged(String name, CellConstraints constraints) {
        componentConstraints.put(name, constraints);
    }

    public CellConstraints getCellConstraints(String name) {
        return componentConstraints.get(name);
    }

    public void addCellConstraints(String name, CellConstraints constraints) {
        componentConstraints.put(name, constraints);
    }

    public CellConstraints removeCellConstraints(String name) {
        CellConstraints constraints = componentConstraints.remove(name);
        return constraints;
    }

    public void addColumnSpec(String columnSpec)
            throws IllegalArgumentException {
        columnSpecs.add(columnSpec);
        buildLayout();
    }

    public String getRowSpec(int index) {
        return rowSpecs.get(index);
    }

    public String getColumnSpec(int index) {
        return columnSpecs.get(index);
    }

    public void setRowSpec(int index, String rowSpec)
            throws IllegalArgumentException {
        rowSpecs.set(index, rowSpec);
        buildLayout();
    }

    public void setColumnSpec(int index, String columnSpec)
            throws IllegalArgumentException {
        columnSpecs.set(index, columnSpec);
        buildLayout();
    }

    public void addRowSpec(String rowSpec) throws IllegalArgumentException {
        rowSpecs.add(rowSpec);
        buildLayout();
    }

    public String removeRowSpec(int index) {
        String rowSpec = rowSpecs.remove(index);
        try {
            buildLayout();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        return rowSpec;
    }

    public String removeColumnSpec(int index) {
        String spec = columnSpecs.remove(index);
        try {
            buildLayout();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        return spec;
    }

    public void addRowSpec(int index, String rowSpec)
            throws IllegalArgumentException {
        rowSpecs.add(index, rowSpec);
        buildLayout();
    }

    public void addColumnSpec(int index, String columnSpec)
            throws IllegalArgumentException {
        columnSpecs.add(index, columnSpec);
        buildLayout();
    }

    /* the following methods realize the LayoutManager interface */

    public String getComponentName(Component component) {
        return componentsToNames.get(component);
    }

    /**
     * Returns the component with the given name or null if not found
     */
    public Component getComponentByName(String name) {
        for (Component component : componentsToNames.keySet()) {
            String testName = componentsToNames.get(component);
            if (testName.equals(name)) {
                return component;
            }
        }
        return null;
    }

    public CellConstraints getComponentConstraints(Component component) {
        String name = componentsToNames.get(component);
        if (name == null) {
            throw new RuntimeException("Unable to find name for component "
                    + component);
        }
        return componentConstraints.get(name);
    }

    // interface for LayoutManager2
    @Override
    public void addLayoutComponent(String name, java.awt.Component comp) {
        throw new RuntimeException(
                "This method should not be called.  Call addLayoutComponent (Component, Object) instead");
    }

    @Override
    public float getLayoutAlignmentX(java.awt.Container target) {
        return formLayout.getLayoutAlignmentX(target);
    }

    @Override
    public float getLayoutAlignmentY(java.awt.Container target) {
        return formLayout.getLayoutAlignmentY(target);
    }

    public FormLayout.LayoutInfo getLayoutInfo(java.awt.Container container) {
        // KBR added to allow FormDebugPanel to work with ContainerLayout
        return this.formLayout.getLayoutInfo(container);
    }

    @Override
    public void invalidateLayout(java.awt.Container target) {
        formLayout.invalidateLayout(target);
    }

    @Override
    public void layoutContainer(java.awt.Container parent) {
        formLayout.layoutContainer(parent);
    }

    @Override
    public java.awt.Dimension maximumLayoutSize(java.awt.Container target) {
        return formLayout.maximumLayoutSize(target);
    }

    @Override
    public java.awt.Dimension minimumLayoutSize(java.awt.Container parent) {
        return formLayout.minimumLayoutSize(parent);
    }

    @Override
    public java.awt.Dimension preferredLayoutSize(java.awt.Container parent) {
        return formLayout.preferredLayoutSize(parent);
    }

    @Override
    public void removeLayoutComponent(java.awt.Component comp) {
        String componentName = componentsToNames.get(comp);
        componentsToNames.remove(comp);
        componentConstraints.remove(componentName);
        formLayout.removeLayoutComponent(comp);
    }

    @Override
    public void addLayoutComponent(java.awt.Component comp, Object constraints) {
        if (!(constraints instanceof String)) {
            throw new RuntimeException(
                    "The constraints must be a String name which should reference a CellConstraints entry in the xml file");
        }
        String componentName = (String) constraints;
        CellConstraints cellConstraints = componentConstraints
                .get(componentName);
        if (cellConstraints == null) {
            System.out.println("Warning : " + componentName
                    + " was added without constraints");
            cellConstraints = new CellConstraints();
            componentConstraints.put(componentName, cellConstraints);
            comp.setVisible(false);
        } else {

            Map<String, Object> customProps = componentNameToCustomProps
                    .get(componentName);
            if (customProps != null) {
                for (String prop : customProps.keySet()) {
                    Object value = customProps.get(prop);
                    // KBR Class compClass = comp.getClass();
                    try {
                        BeanInfo beanInfo = Introspector.getBeanInfo(comp
                                .getClass());
                        PropertyDescriptor[] props = beanInfo
                                .getPropertyDescriptors();
                        for (PropertyDescriptor propertyDescriptor : props) {
                            if (propertyDescriptor.getName().equals(prop)) {
                                Method writeMethod = propertyDescriptor
                                        .getWriteMethod();
                                writeMethod
                                .invoke(comp, new Object[] { value });
                                break;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        componentsToNames.put(comp, componentName);
        formLayout.addLayoutComponent(comp, cellConstraints);
    }

    //  private class LocationScore implements Comparable<LocationScore>
    //  {
    //    public int score;
    //
    //    public int row;
    //
    //    public int col;
    //
    //    public int width;
    //
    //    public LocationScore(int score, int row, int col, int width)
    //    {
    //      this.score = score;
    //      this.row = row;
    //      this.col = col;
    //      this.width = width;
    //    }
    //
    //    public int compareTo(LocationScore testScore)
    //    {
    //      return this.score < testScore.score ? -1
    //          : this.score > testScore.score ? 1 : 0;
    //    }
    //  }

    public static void main(String[] args) {
    }

}
