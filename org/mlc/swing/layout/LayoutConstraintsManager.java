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
import java.awt.Container;
import java.awt.Insets;
import java.beans.Statement;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jgoodies.forms.layout.CellConstraints;

/**
 * This class handles the serialization and deserialization of the xml files
 * that we are using to store the layout constraints.
 *
 * <p>In the consuming program, the use of this class might look like:
<br><code>
InputStream constraints = this.getClass().getResourceAsStream(xmlFile);
LayoutConstraintsManager layoutConstraintsManager =
        LayoutConstraintsManager.getLayoutConstraintsManager(constraints);
LayoutManager layout = layoutConstraintsManager.createLayout("panel", this);
this.setLayout(layout);
</code>
 *
 * <p>[I'm sure there are more
 * elegant ways of handling this (like JAXB) or some other mapping software but
 * this is simple, it works, and we don't have to package a bunch of other
 * software or files.]
 *
 * @author Michael Connor
@version $Id$
@since Ptolemy II 8.0
 */
public class LayoutConstraintsManager {
    String defaultColumnSpecs = "right:max(30dlu;pref),3dlu,80dlu,10dlu,right:max(30dlu;pref),3dlu,80dlu,1dlu:grow";

    String defaultRowSpecs = "pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref:grow";

    static Set<Class> textComponents = new HashSet<Class>();

    static {
        textComponents.add(JButton.class);
        textComponents.add(JCheckBox.class);
        textComponents.add(JRadioButton.class);
        textComponents.add(JToggleButton.class);
        textComponents.add(JLabel.class);
    }

    Map<ContainerLayout, Container> containers = new HashMap<ContainerLayout, Container>();

    List<ContainerLayout> layouts = new ArrayList<ContainerLayout>();

    /**
     * This method will create a LayoutConstraintsManager with default JGoodies
     * row and column specs that are common in applications. The user can then
     * manipulate these default specs using the LayoutFrame to fine tune the specs
     * to be whatever they want.
     */
    public LayoutConstraintsManager() {
    }

    /**
     * This method will create a LayoutConstraintsManager with the JGoodies specs
     * provided as default
     */
    public LayoutConstraintsManager(String defaultColumnSpecs,
            String defaultRowSpecs) {
        this.defaultColumnSpecs = defaultColumnSpecs;
        this.defaultRowSpecs = defaultRowSpecs;
    }

    public List<ContainerLayout> getLayouts() {
        List<ContainerLayout> list = new ArrayList<ContainerLayout>(
                layouts.size());
        list.addAll(layouts);
        return list;
    }

    /**
     * This method will build a layout from the xml file based on the name and
     * call setLayout on the container passed in.
     */
    public void setLayout(String name, Container container) {
        ContainerLayout containerLayout = getLayout(name);

        if (containerLayout == null) {
            containerLayout = new ContainerLayout(name, defaultColumnSpecs,
                    defaultRowSpecs);
            layouts.add(containerLayout);
        }
        //    else
        //      containers.remove(containerLayout);

        container.setLayout(containerLayout);
        containers.put(containerLayout, container);
    }

    /**
     * This method creates a layout by first trying to look in memory to see if a
     * layout has been defined with the given name. If a layout exists, it is
     * returned. Note that when I say in memory that probably means that it was
     * defined in the xml file. If one doesn't exist, a layout with what I
     * consider relatively generic constraints will be created and returned. The
     * reason this method requires the container is because in the case where you
     * are trying to layout the container visually, I need to be able to get a
     * handle on the container so I can make calls to add components to it during
     * interactive layout. This will not be touched at runtime if you are not
     * doing anything interactively. This method should be seen as a replacement
     * for LayoutConstraintsManager.setLayout(String name, Container container) as
     * it's more natural to set the layout on the container yourself.
     */
    public ContainerLayout createLayout(String name, Container container) {
        ContainerLayout containerLayout = getLayout(name);

        if (containerLayout == null) {
            containerLayout = new ContainerLayout(name, defaultColumnSpecs,
                    defaultRowSpecs);
            layouts.add(containerLayout);
        }

        containers.put(containerLayout, container);
        return containerLayout;
    }

    public Container getContainer(ContainerLayout layout) {
        return containers.get(layout);
    }

    private ContainerLayout getLayout(String name) {
        for (int i = 0; i < layouts.size(); i++) {
            if (layouts.get(i).getName().equals(name)) {
                return layouts.get(i);
            }
        }

        return null;
    }

    // KBR NYI
    //  public List<String> getContainerNames()
    //  {
    //    List<String> names = new ArrayList<String>();
    //
    //    return names;
    //  }

    public ContainerLayout getContainerLayout(String containerName) {
        ContainerLayout layout = getLayout(containerName);
        return layout;
    }

    public void removeLayout(ContainerLayout containerLayout) {
        layouts.remove(containerLayout);
    }

    public void addLayout(ContainerLayout containerLayout) {
        layouts.add(containerLayout);
    }

    /**
      Get an XML representation of the FormLayout constraints for all containers
      in this manager.
     */
    public String getXML() {
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        xml.append("<containers>\n");

        for (int index = 0; index < layouts.size(); index++) {
            ContainerLayout layout = layouts.get(index);
            LinkedHashMap<String, CellConstraints> constraintMap = layout
                    .getCellConstraints();

            xml.append("    <container name=\"" + layout.getName() + "\"\n");
            xml.append("               columnSpecs=\""
                    + layout.getColumnSpecsString() + "\"\n");
            xml.append("               rowSpecs=\""
                    + layout.getRowSpecsString() + "\">\n");

            for (Object element : constraintMap.keySet()) {
                String componentName = (String) element;
                CellConstraints constraints = constraintMap.get(componentName);
                xml.append("        <cellconstraints ");
                xml.append("name=\"" + componentName + "\" ");
                xml.append("gridX=\"" + constraints.gridX + "\" ");
                xml.append("gridY=\"" + constraints.gridY + "\" ");
                xml.append("gridWidth=\"" + constraints.gridWidth + "\" ");
                xml.append("gridHeight=\"" + constraints.gridHeight + "\" ");
                xml.append("horizontalAlignment=\""
                        + getAlignment(constraints.hAlign) + "\" ");
                xml.append("verticalAlignment=\""
                        + getAlignment(constraints.vAlign) + "\" ");
                xml.append("topInset=\"" + constraints.insets.top + "\" ");
                xml.append("bottomInset=\"" + constraints.insets.bottom + "\" ");
                xml.append("rightInset=\"" + constraints.insets.right + "\" ");
                xml.append("leftInset=\"" + constraints.insets.left + "\"/>\n");
            }

            for (Object element : constraintMap.keySet()) {
                String componentName = (String) element;
                Component component = layout.getComponentByName(componentName);

                if (component != null) {
                    Map<String, Object> customProperties = layout
                            .getCustomProperties(componentName);

                    boolean hasProperties = false;
                    boolean isTextComponent = isTextComponent(component);
                    // we need to look through these and see if we have
                    // any valid properties. the text props don't count
                    // for controls like JLabel, JButton, etc. because
                    // we'll put those in the constructor.

                    // EAL: In Ptolemy, we need text in the properties
                    // fields. So remove this check below, and force
                    // isTextComponent to false.
                    isTextComponent = false;

                    //for (String propertyName : customProperties.keySet()) {
                    //    if ((!isTextComponent) /* EAL: || (!propertyName.equals("text")) */) {
                    //        hasProperties = true;
                    //        break;
                    //    }
                    //}
                    if (!customProperties.keySet().isEmpty()) {
                        hasProperties = true;
                    }

                    if (hasProperties) {
                        xml.append("\n        <properties component=\""
                                + componentName + "\">\n");
                        for (String propertyName : customProperties.keySet()) {

                            if (isTextComponent && propertyName.equals("text")) {
                                break;
                            }

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            XMLEncoder xmlEncoder = new XMLEncoder(stream);
                            xmlEncoder.setOwner(component);

                            String methodName = "set"
                                    + propertyName.substring(0, 1).toUpperCase(
                                            Locale.getDefault())
                                            + (propertyName.length() > 1 ? propertyName
                                                    .substring(1) : "");
                            xmlEncoder.writeStatement(new Statement(xmlEncoder,
                                    methodName, new Object[] { customProperties
                                    .get(propertyName) }));
                            xmlEncoder.close();

                            String propertyXml = new String(
                                    stream.toByteArray());
                            int voidStart = propertyXml.indexOf("<void");
                            int voidEnd = propertyXml.indexOf(">", voidStart);
                            int end = propertyXml.lastIndexOf("</void>");
                            String xmlWithoutDec = propertyXml.substring(
                                    voidEnd + 1, end);
                            xmlWithoutDec = xmlWithoutDec.trim();
                            String indented = "          "
                                    + xmlWithoutDec.replaceAll("\n",
                                            "\n        ");
                            xml.append("         <property name=\""
                                    + propertyName + "\">");
                            xml.append(indented);
                            xml.append("</property>\n");
                        }

                        xml.append("        </properties>\n");
                    }
                }
            }

            xml.append("    </container>\n");
        }

        xml.append("</containers>\n");
        return xml.toString();
    }

    public static boolean isTextComponent(Component component) {
        for (Class clazz : textComponents) {
            if (clazz.isAssignableFrom(component.getClass())) {
                return true;
            }
        }
        return false;
    }

    private static String createString(NodeList childNodes) {

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                DOMSource source = new DOMSource(child);
                StreamResult result = new StreamResult(byteStream);
                transformer.transform(source, result);
            }
            return new String(byteStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("unexpected error");
        }
    }

    public static final String DEFAULT = "default";

    public static final String FILL = "fill";

    public static final String CENTER = "center";

    public static final String LEFT = "left";

    public static final String RIGHT = "right";

    public static final String TOP = "top";

    public static final String BOTTOM = "bottom";

    /**
     * Translates an alignment value to a string.
     */
    public static String getAlignment(CellConstraints.Alignment alignment) {
        String value = null;

        if (alignment == CellConstraints.DEFAULT) {
            value = DEFAULT;
        } else if (alignment == CellConstraints.FILL) {
            value = FILL;
        } else if (alignment == CellConstraints.CENTER) {
            value = CENTER;
        } else if (alignment == CellConstraints.LEFT) {
            value = LEFT;
        } else if (alignment == CellConstraints.RIGHT) {
            value = RIGHT;
        } else if (alignment == CellConstraints.TOP) {
            value = TOP;
        } else if (alignment == CellConstraints.BOTTOM) {
            value = BOTTOM;
        }

        if (value == null) {
            throw new RuntimeException("Unknown alignment type");
        } else {
            return value;
        }
    }

    /**
     * Translates a string to an alignment value.
     */
    public static CellConstraints.Alignment getAlignment(String value) {
        CellConstraints.Alignment alignment = null;

        if (value.equalsIgnoreCase(DEFAULT)) {
            alignment = CellConstraints.DEFAULT;
        } else if (value.equalsIgnoreCase(FILL)) {
            alignment = CellConstraints.FILL;
        } else if (value.equalsIgnoreCase(CENTER)) {
            alignment = CellConstraints.CENTER;
        } else if (value.equalsIgnoreCase(LEFT)) {
            alignment = CellConstraints.LEFT;
        } else if (value.equalsIgnoreCase(RIGHT)) {
            alignment = CellConstraints.RIGHT;
        } else if (value.equalsIgnoreCase(TOP)) {
            alignment = CellConstraints.TOP;
        } else if (value.equalsIgnoreCase(BOTTOM)) {
            alignment = CellConstraints.BOTTOM;
        } else {
            throw new RuntimeException("Invalid alignment");
        }

        return alignment;
    }

    /**
     * Returns a LayoutConstraintsManager based on an input stream for an xml
     * file. The root node in the xml file should be called <code>containers</code> and should
     * adhere to the xml format for this tool.
     */
    public static LayoutConstraintsManager getLayoutConstraintsManager(
            InputStream stream) {
        Document dataDocument = null;

        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            dataDocument = documentBuilder.parse(stream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to create DocumentBuilder", e);
        }

        Node root = dataDocument.getDocumentElement();
        return getLayoutConstraintsManager(root);
    }

    /**
     * Returns a layout constraints manager given a containers node. This will
     * enable you to keep a lot of different constraints in a single file or at
     * least provide a little more flexibility.
     */
    public static LayoutConstraintsManager getLayoutConstraintsManager(
            Node containersNode) {

        if (!containersNode.getNodeName().equals("containers")) {
            throw new RuntimeException("Expected a node named containers");
        }

        LayoutConstraintsManager layoutConstraintsManager = new LayoutConstraintsManager();
        Node[] containerNodes = getNodesNamed(containersNode, "container");

        for (Node containerNode : containerNodes) {
            Map<String, String> containerAttributes = getAttributeMap(containerNode);
            String containerName = containerAttributes.get("name");
            if (containerName == null) {
                throw new RuntimeException(
                        "Container must have a name attribute");
            }
            String columnSpecs = containerAttributes.get("columnSpecs") != null ? containerAttributes
                    .get("columnSpecs") : "";
                    String rowSpecs = containerAttributes.get("rowSpecs") != null ? containerAttributes
                            .get("rowSpecs") : "";

                            final ContainerLayout containerLayout = new ContainerLayout(
                                    containerName, columnSpecs, rowSpecs);

                            Node[] cellConstraints = getNodesNamed(containerNode,
                                    "cellconstraints");
                            for (Node cellConstraint : cellConstraints) {
                                Map<String, String> constraintAttributes = getAttributeMap(cellConstraint);

                                String name = null;
                                CellConstraints.Alignment horizontalAlignment = CellConstraints.DEFAULT;
                                CellConstraints.Alignment verticalAlignment = CellConstraints.DEFAULT;
                                int gridX = 1;
                                int gridY = 1;
                                int gridWidth = 1;
                                int gridHeight = 1;
                                int topInset = 0;
                                int bottomInset = 0;
                                int rightInset = 0;
                                int leftInset = 0;

                                if (constraintAttributes.get("name") == null) {
                                    throw new RuntimeException(
                                            "cellconstraints attribute name cannot be null for container "
                                                    + containerName);
                                }
                                name = constraintAttributes.get("name");
                                if (constraintAttributes.get("horizontalAlignment") != null) {
                                    horizontalAlignment = getAlignment(constraintAttributes
                                            .get("horizontalAlignment"));
                                }
                                if (constraintAttributes.get("verticalAlignment") != null) {
                                    verticalAlignment = getAlignment(constraintAttributes
                                            .get("verticalAlignment"));
                                }
                                if (constraintAttributes.get("gridX") != null) {
                                    gridX = Integer.parseInt(constraintAttributes.get("gridX"));
                                }
                                if (constraintAttributes.get("gridY") != null) {
                                    gridY = Integer.parseInt(constraintAttributes.get("gridY"));
                                }
                                if (constraintAttributes.get("gridWidth") != null) {
                                    gridWidth = Integer.parseInt(constraintAttributes
                                            .get("gridWidth"));
                                }
                                if (constraintAttributes.get("gridHeight") != null) {
                                    gridHeight = Integer.parseInt(constraintAttributes
                                            .get("gridHeight"));
                                }
                                if (constraintAttributes.get("topInset") != null) {
                                    topInset = Integer.parseInt(constraintAttributes
                                            .get("topInset"));
                                }
                                if (constraintAttributes.get("bottomInset") != null) {
                                    bottomInset = Integer.parseInt(constraintAttributes
                                            .get("bottomInset"));
                                }
                                if (constraintAttributes.get("rightInset") != null) {
                                    rightInset = Integer.parseInt(constraintAttributes
                                            .get("rightInset"));
                                }
                                if (constraintAttributes.get("leftInset") != null) {
                                    leftInset = Integer.parseInt(constraintAttributes
                                            .get("leftInset"));
                                }

                                CellConstraints constraints = new CellConstraints(gridX, gridY,
                                        gridWidth, gridHeight, horizontalAlignment,
                                        verticalAlignment, new Insets(topInset, leftInset,
                                                bottomInset, rightInset));

                                containerLayout.addCellConstraints(name, constraints);
                            }

                            Node[] propertiesNodes = getNodesNamed(containerNode, "properties");

                            // this is sooooo lame. we now how to construct a fake xml doc
                            // so the parser can read it. i'm starting to think it would have
                            // been easier to just do the whole damn thing by hand. arggg..
                            String fakeDoc = "<java version=\"1.4.0\" class=\"java.beans.XMLDecoder\">";
                            fakeDoc += "<void id=\"controller\" property=\"owner\"/>\n";
                            fakeDoc += "<object idref=\"controller\">";

                            for (Node propertiesNode : propertiesNodes) {
                                Map<String, String> propertyAttributes = getAttributeMap(propertiesNode);
                                String componentName = propertyAttributes.get("component");
                                if (componentName == null) {
                                    throw new RuntimeException(
                                            "propertyset must have an attribute called component");
                                }

                                Node[] propertyNodes = getNodesNamed(propertiesNode, "property");
                                for (Node propertyNode : propertyNodes) {
                                    Map<String, String> voidAttributes = getAttributeMap(propertyNode);
                                    String property = voidAttributes.get("name");
                                    if (property == null) {
                                        throw new RuntimeException(
                                                "property element must have a name");
                                    }
                                    fakeDoc += "<void method=\"setProperty\"><string>"
                                            + componentName + "</string>";
                                    fakeDoc += "<string>" + property + "</string>";
                                    fakeDoc += createString(propertyNode.getChildNodes());
                                    fakeDoc += "</void>\n";

                                }
                            }

                            fakeDoc += "</object></java>";

                            if (propertiesNodes.length > 0) {

                                //        Object controller = new Object()
                                //        {
                                //          public void configureProperty(String componentName, String property,
                                //              Object value)
                                //          {
                                //            containerLayout.setProperty(componentName, property, value);
                                //          }
                                //        };

                                XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(
                                        fakeDoc.getBytes()));
                                decoder.setOwner(containerLayout);
                                decoder.readObject();
                                decoder.close();
                            }

                            layoutConstraintsManager.addLayout(containerLayout);
        }

        return layoutConstraintsManager;
    }

    private static Map<String, String> getAttributeMap(Node node) {

        Map<String, String> attributeMap = new HashMap<String, String>();

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int index = 0; index < attributes.getLength(); index++) {
                Node attribute = attributes.item(index);
                attributeMap.put(attribute.getNodeName(),
                        attribute.getNodeValue());
            }
        }

        return attributeMap;
    }

    private static Node[] getNodesNamed(Node parent, String nodeName) {
        NodeList children = parent.getChildNodes();
        List<Node> childList = new ArrayList<Node>();
        for (int i = 0; i < children.getLength(); i++) {
            if (nodeName.equals(children.item(i).getNodeName())) {
                childList.add(children.item(i));
            }
        }
        Node[] result = new Node[childList.size()];
        return childList.toArray(result);
    }

    public static void main(String[] args) {
        LayoutConstraintsManager l = LayoutConstraintsManager
                .getLayoutConstraintsManager(LayoutConstraintsManager.class
                        .getResourceAsStream("editableLayoutConstraints.xml"));
        /*ContainerLayout cl =*/l.getContainerLayout("mainLayout");
    }

}
