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
/*
 * ComponentDef.java
 *
 * Created on September 30, 2004, 5:44 PM
 */

package org.mlc.swing.layout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A container class for all the definition data about a Component.
 * Instances of this class make up the component palette and are
 * used when creating and editing component data.
 * @author Michael Connor
@version $Id$
@since Ptolemy II 8.0
 */
public class ComponentDef implements Comparable<Object> {
    //private static final long serialVersionUID = 1L;

    public String name = "";

    public String iconClass = "";

    public Icon icon = null;

    public String declarations = "";

    public String configure = "";

    public String add = "";

    public String remove = "";

    public String imports = "";

    public String preview = "";

    public boolean isContainer = false;

    private String description = "";

    public String getDescription() {
        return description;
    }

    public ComponentDef() {
    }

    // Stolen from Xerces 2.5.0 code - need to replace java 1.5 getTextContent method
    final boolean hasTextContent(Node child) {
        return child.getNodeType() != Node.COMMENT_NODE
                && child.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE;
        //          &&
        //            (child.getNodeType() != Node.TEXT_NODE );
        //          ||
        //             ((TextImpl) child).isIgnorableWhitespace() == false);
    }

    // Stolen from Xerces 2.5.0 code - need to replace java 1.5 getTextContent method
    void getTextContent(Node anode, StringBuffer buf) {
        Node child = anode.getFirstChild();
        while (child != null) {
            if (hasTextContent(child)) {
                buf.append(child.getNodeValue());
            }
            child = child.getNextSibling();
        }
    }

    // Stolen from Xerces 2.5.0 code - need to replace java 1.5 getTextContent method
    private String getTextContent(Node anode) //throws DOMException
    {
        Node child = anode.getFirstChild();
        if (child != null) {
            Node next = child.getNextSibling();
            if (next == null) {
                return hasTextContent(child) ? child.getNodeValue() : "";
            }
            StringBuffer buf = new StringBuffer();
            getTextContent(anode, buf);
            return buf.toString();
        }
        return "";
    }

    private String doNode(Node parent, String nodeName) {
        String temp = "";
        Node[] nodes = getNodesNamed(parent, nodeName);
        for (Node node : nodes) {
            // Java 1.5 library function
            //      temp += nodes[i].getTextContent();
            temp += getTextContent(node);
        }
        return temp;
    }

    public ComponentDef(Node componentNode) {
        Map<String, String> attributes = getAttributeMap(componentNode);
        name = attributes.get("name");
        iconClass = attributes.get("iconClass");
        description = attributes.get("desc");
        // Java 1.5 library function.
        //    isContainer = Boolean.parseBoolean(attributes.get("container"));
        String str = attributes.get("container");
        if (str != null) {
            isContainer = str.compareToIgnoreCase("true") == 0;
        }

        imports = doNode(componentNode, "imports");
        declarations = doNode(componentNode, "declarations");
        configure = doNode(componentNode, "configure");
        remove = doNode(componentNode, "remove");
        add = doNode(componentNode, "add");
        preview = doNode(componentNode, "preview");
    }

    // KBR 12/31/05 Add a ctor for use by the "old-style" new button
    public ComponentDef(String name, String imp, String decl, String add) {
        this.name = name;
        this.imports = imp;
        this.declarations = decl;
        this.add = add;
    }

    private static InputStream getCompFile() {
        // pull the components.xml file out of the root of the jar file
        String jarFileName = "formLayoutMakerx.jar";
        JarFile jf = null;
        try {
            jf = new JarFile(jarFileName);
            JarEntry je = null;
            Enumeration entries = jf.entries();
            while (entries.hasMoreElements()) {
                je = (JarEntry) entries.nextElement();
                if (je.getName().equals("components.xml")) {
                    return jf.getInputStream(je);
                }
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (jf != null) {
                try {
                    jf.close();
                } catch (IOException ex) {
                    System.out.println(
                            "Failed to close \"" + jarFileName + "\": " + ex);
                }
            }
        }
        return null;
    }

    /** Creates a new instance of Component Palette. All component configurations
    are pulled out of components.xml
     */
    @SuppressWarnings("unchecked")
    public static List<ComponentDef> createComponentDefs() {
        List<ComponentDef> components = new ArrayList<ComponentDef>();
        InputStream paletteStream = null;
        try {
            paletteStream = ComponentDef.class
                    .getResourceAsStream("components.xml");
            if (paletteStream == null) {
                paletteStream = getCompFile();
            }
            if (paletteStream == null) {
                return components;
            }

            Document dataDocument = null;

            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory
                        .newInstance().newDocumentBuilder();
                dataDocument = documentBuilder.parse(paletteStream);
                Node paletteNode = dataDocument.getDocumentElement();
                Node[] componentNodes = getNodesNamed(paletteNode, "component");

                for (Node componentNode : componentNodes) {
                    components.add(new ComponentDef(componentNode));
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to create DocumentBuilder",
                        e);
            }

            Collections.sort(components);
        } finally {
            if (paletteStream != null) {
                try {
                    paletteStream.close();
                } catch (Exception ex) {
                    throw new RuntimeException(
                            "Failed to close paletteStream " + paletteStream,
                            ex);
                }
            }
        }
        return components;
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
            String childname = children.item(i).getNodeName();
            if (childname != null) {
                if (nodeName.equals(childname)) {
                    childList.add(children.item(i));
                }
            }
        }
        Node[] result = new Node[childList.size()];
        return childList.toArray(result);
    }

    public String getConfigure(String name) {
        return configure.replaceAll("\\$\\{name\\}", name);
    }

    public String getImports(String name) {
        return imports.replaceAll("\\$\\{name\\}", name);
    }

    public String getDeclarations(String name) {
        return declarations.replaceAll("\\$\\{name\\}", name);
    }

    public String getAdd(String name) {
        return add.replaceAll("\\$\\{name\\}", name);
    }

    /** When dragging from the palette we need a clone rather than modify
        the original.
     */
    @Override
    public ComponentDef clone() {
        ComponentDef newone = new ComponentDef();
        newone.name = name;
        newone.iconClass = iconClass;
        newone.declarations = declarations;
        newone.configure = configure;
        newone.add = add;
        newone.imports = imports;
        newone.preview = preview;
        newone.isContainer = isContainer;
        return newone;
    }

    /** Make it sortable on name so the palette is ordered.
     */
    @Override
    public int compareTo(Object o) {
        return name.compareTo(((ComponentDef) o).name);
    }

    public static void main(String[] args) {
        List<ComponentDef> components = ComponentDef.createComponentDefs();
        System.out.println(components);
    }
}
