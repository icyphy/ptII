/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
 */
package diva.util.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import diva.util.Filter;
import diva.util.FilteredIterator;
import diva.util.IteratorUtilities;

/**
 * An XmlElement is a node of a tree representing an XML file.
 * It is a concrete class. An XmlReader object will construct a
 * tree of XmlElements from an XML file and place it into an
 * XmlDocument. Applications can traverse a tree of XmlElements to
 * extract data or build their own application-specific data structure.
 * They can also construct a tree of XmlElements and add it to
 * an XmlDocument and generate an XML output file.
 *
 * @author Steve Neuendorffer, John Reekie
 * @version $Id$
 */
public class XmlElement {

    /** The child elements of this element. Could be a LinkedList
     * instead, I suppose...
     */
    private List _children;

    // The attributes of this element.
    private TreeMap _attributes;

    // The type of this element.
    private String _type;

    // The character data that is contained in this element.
    private String _pcdata = "";

    // The XmlElement that contains this element (possibly null).
    private XmlElement _parent;

    /**
     * Create a new XmlElement with element type given by the string.
     * The element has no attributes and no child elements.
     *
     * @param type The element type
     */
    public XmlElement(String type) {
        _children = new ArrayList();
        _attributes = new TreeMap();
        _type = type;
    }

    /**
     * Create a new XmlElement of unknown type.
     */
    public XmlElement() {
        this("UNKNOWN");
    }

    /**
     * Create a new XmlElement with element type with the given name and the
     * given attributes.  The element starts with no child elements.
     *
     * @param type The element type
     * @param attrs The attributes of this XmlElement.
     */
    public XmlElement(String type, Map attrs) {
        _children = new ArrayList();
        _attributes = new TreeMap(attrs);
        _type = type;
    }

    /** Add a child element to this element. Child elements
     * are ordered.
     */
    public void addElement(XmlElement e) {
        _children.add(e);
        e.setParent(this);
    }

    /**
     * Add the String to the end of the current PCDATA for this element.
     */
    public void appendPCData(String s) {
        _pcdata = _pcdata + s;
    }

    /**
     * Return an enumeration over the names of the attributes
     * in this schematic. There is no order guarantee on the attributes.
     */
    public Iterator attributeNames () {
        return _attributes.keySet().iterator();
    }

    /**
     * Test if this element contains the given element. This
     * is included mainly to allow test suites to be written. In
     * general, check the parent of the child.
     */
    public boolean containsElement (XmlElement elt) {
        return _children.contains(elt);
    }

    /**
     * Return the number of child elements.
     */
    public int elementCount () {
        return _children.size();
    }

    /**
     * Return an Iterator of all the child elements of this element.
     * The elements are generated in the order in which they were
     * added.
     */
    public Iterator elements () {
        return _children.iterator();
    }

    /**
     * Return an Iterator of all the child elements of this element that
     * have the given element type.
     *
     *  @return an Iterator of XmlElements
     */
    public Iterator elements (final String type) {
        return new FilteredIterator(elements(), new Filter () {
                public boolean accept (Object o) {
                    return ((XmlElement)o)._type.equals(type);
                }
            });
    }

    /**
     * Return the value of the attribute with the given name,
     * or null if there isn't one.
     */
    public String getAttribute (String name) {
        return (String) _attributes.get(name);
    }

    /**
     * Return the map from attribute names to value. This returns the
     * internal object that holds attributes, and is provided so that
     * more sophisticated operations than provided by the methods in
     * this interface can be implemented when needed. <b>Note</b>: if
     * you add any attributes to this list, you must be sure that the
     * value is a String.
     */
    public Map getAttributeMap () {
        return _attributes;
    }

    /**
     * Return the list of child elements. This returns the internal object
     * that holds child elements, and is provided so that more sophisticated
     * operations than provided by the methods in this interface can
     * be implemented when needed. <b>Note</b>: if you add any
     * elements to this list, you MUST call the setParent() method
     * of that element with this object.
     */
    public List getChildList () {
        return _children;
    }

    /**
     * Return the first child element of this element with the given type,
     * or null if there isn't one.
     */
    public XmlElement getElement(final String type) {
        return (XmlElement) IteratorUtilities.firstMatch(elements(), new Filter() {
                public boolean accept (Object o) {
                    return ((XmlElement)o)._type.equals(type);
                }
            });
    }

    /**
     * Return the first child element of this element with the given type
     * and name, or null if there isn't one.
     */
    public XmlElement getElement(final String type, final String name) {
        return (XmlElement) IteratorUtilities.firstMatch(elements(), new Filter() {
                public boolean accept (Object o) {
                    XmlElement elt = (XmlElement) o;
                    return elt._type.equals(type)
                        && elt.getAttribute("name").equals(name);
                }
            });
    }

    /** Return the type of this XmlElement.  The type is immutably set when
     *  the XmlElement is created.
     */
    public String getType() {
        return _type;
    }

    /**
     * Return the parent element of this element, or null if it has no
     * parent.
     */
    public XmlElement getParent() {
        return _parent;
    }

    /**
     * Return the PCData that is associated with this XmlElement.
     */
    public String getPCData() {
        return _pcdata;
    }

    /**
     * Test if this element has the attribute with the given name.
     */
    public boolean hasAttribute (String name) {
        return _attributes.containsKey(name);
    }

    /**
     * Remove an attribute from this element
     */
    public void removeAttribute(String name) {
        _attributes.remove(name);
    }

    /**
     * Remove a child element from this element
     */
    public void removeElement(XmlElement e) {
        e.setParent(null);
        _children.remove(e);
    }

    /**
     * Set the attribute with the given name to the given value.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public void setAttribute (String name, String value) {
        _attributes.put(name, value);
    }

    /**
     * Set the parent element of this element.
     */
    public void setParent(XmlElement p) {
        _parent = p;
    }

    /**
     * Set the text of this element to the given string.
     */
    public void setPCData(String s) {
        _pcdata = s;
    }

    /**
     * Set the type of this element
     */
    public void setType(String s) {
        _type = s;
    }

    /**
     * Convert this element to a string in XML
     */
    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            writeXML(sw, "");
        } catch (Exception e) {}
        return sw.toString();
    }

    /**
     * Print this element to a Writer
     */
    public void writeXML(Writer out, String prefix) throws IOException {
        out.write(prefix + "<" + getType());
        Iterator attrs = attributeNames();
        while (attrs.hasNext()) {
            String name = (String) attrs.next();
            String value = getAttribute(name);
            out.write(" " + name + "=\"" + value + "\"");
        }
        String pcdata = getPCData();
        if (elementCount() > 0 || pcdata.length() > 0) {
            out.write(">");
            if (elementCount() > 0) {
                Iterator children = elements();
                out.write("\n");
                while (children.hasNext()) {
                    XmlElement child = (XmlElement) children.next();
                    child.writeXML(out, prefix + "    ");
                }
                out.write(prefix);
            }
            if (pcdata.length() > 0) {
                out.write(pcdata);
            }
            out.write("</" + getType() + ">\n");

        } else {
            out.write("/>\n");
        }
    }
}


