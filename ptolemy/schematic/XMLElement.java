/* A XMLElement represents a PtII design

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic;

import java.util.NoSuchElementException;
import java.util.Enumeration;
import collections.HashedMap;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// XMLElement
/**

An XMLElement is the abstract superclass of classes that represent
XML elements. It contains some basic support for accessing elements.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class XMLElement extends Object {

    /**
     * Create a new XMLElement with element type given by the string.
     * The element has no attributes and no child elements.
     *
     * @param name The element type
     */
    public XMLElement(String type) {
        _attributes = (HashedMap) new HashedMap();
        _childelements = (LinkedList) new LinkedList();
        _elementtype = type;
        _pcdata = "";
    }

    /**
     * Create a new XMLElement with element type with the given name and the
     * given attributes.  The element starts with no child elements.
     *
     * @param name The element type
     * @param attribs The attributes of this XMLElement.
     */
    public XMLElement(String type, HashedMap attribs) {
        _attributes = attribs;
        _elementtype = type;
        _childelements = (LinkedList) new LinkedList();
        _pcdata = "";
    }

    /** Add a child element to this element
     */
    public void addChildElement(XMLElement e) {
        _childelements.insertLast(e);
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
     * in this schematic.
     */
    public Enumeration attributeNames () {
        return _attributes.keys();
    }

    /**
     * Return an Enumeration of all the child elements of this element.
     *
     *  @return an Enumeration of XMLElements
     */
    public Enumeration childElements() {
        return _childelements.elements();
    }

   /**
     * Return an Enumeration of all the child elements of this element that
     * have the given element type.
     *
     *  @return an Enumeration of XMLElements
     */
    public Enumeration childElements(String type) {
        LinkedList filter = new LinkedList();
        Enumeration elements = childElements();
        while(elements.hasMoreElements()) {
            XMLElement el = (XMLElement) elements.nextElement();
            if(type.equals(el.getElementType()))
                filter.insertFirst(el);
        }
        return filter.elements();
    }

    /**
     * Return the value of the attribute with the given name.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public String getAttribute (String name) {
        return (String) _attributes.at(name);
    }

    /**
     * Return the first child element of this element with the given type
     * @throws NoSuchElementException if no element with the given type exists.
     */
    public XMLElement getChildElement(String type)
    throws NoSuchElementException {
       Enumeration elements = childElements();
        while(elements.hasMoreElements()) {
            XMLElement el = (XMLElement) elements.nextElement();
            if(type.equals(el.getElementType()))
                return el;
        }
        throw new NoSuchElementException("XMLElement does not contain a " +
                "child element with type " + type);
    }

    /**
     * Return the first child element of this element with the given type
     * and name.
     * @throws NoSuchElementException if no element with the given type 
     * and name exists.
     */
    public XMLElement getChildElement(String type, String name)
    throws NoSuchElementException {
       Enumeration elements = childElements();
        while(elements.hasMoreElements()) {
            XMLElement el = (XMLElement) elements.nextElement();
            if(type.equals(el.getElementType())&&
                    name.equals(el.getAttribute("name")))
                return el;
        }
        throw new NoSuchElementException("XMLElement does not contain a " +
                "child element with type " + type);
    }

    /** Return the type of this XMLElement.  The type is immutably set when
     *  the XMLElement is created.
     */
    public String getElementType() {
        return _elementtype;
    }

    /**
     * Return the parent element of this element, or null if the parent
     * has not been set.
     */
    public XMLElement getParent() {
        return _parent;
    }

    /**
     * Return the PCData that is associated with this XMLElement.
     */
    public String getPCData() {
        return _pcdata;
    }


    /**
     * Get the URL that this file was parsed from. This will return
     * a null string if this element is not the root element
     * of an XML document.
     */
    public String getXMLFileLocation() {
        return _xmlFileLocation;
    }

    /**
     * Test if this schematic has the attribute wuth the given name.
     */
    public boolean hasAttribute (String name) {
        return _attributes.includesKey(name);
    }

    /**
     * Test if the element is a child element of this element
     */
    public boolean hasChildElement(XMLElement e) {
        return _childelements.includes(e);
    }

    /**
     * Test if a child element of this element has the given type
     */
    /*public boolean hasChildElement(String type) {
        Enumeration elements = childElements();
        while(elements.hasMoreElements()) {
            XMLElement el = (XMLElement) elements.nextElement();
            if(type.equals(el.getElementType()))
                return true;
        }
        return false;
        }*/

    /**
     * Remove an attribute from this element
     */
    public void removeAttribute(String name) {
        _attributes.removeAt(name);
    }

    /**
     * Remove an child element from this element
     */
    public void removeChildElement(XMLElement e) {
        _childelements.removeOneOf(e);
    }

    /**
     * Set the attribute with the given name to the given value.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public void setAttribute (String name, String value) {
        _attributes.putAt(name, value);
    }

    /**
     * Set the parent element of this element.
     */
    public void setParent(XMLElement p) {
        _parent = p;
    }

    /**
     * Set the text of this element to the given string.
     */
    public void setPCData(String s) {
        _pcdata = s;
    }

    /**
     * Set the location that this file was parsed from.
     * Is called by the parser for document root elements only.
     */
    void setXMLFileLocation(String s) {
        _xmlFileLocation = s;
    }

    /**
     * Convert this element to a string in XML
     */
    public String toString() {
        String s = "";
        s = s + "<";
        s = s + getElementType();
        Enumeration attribs = attributeNames();
        while(attribs.hasMoreElements()) {
            String name = (String) attribs.nextElement();
            String value = getAttribute(name);
            s = s + " ";
            s = s + name;
            s = s + "=\"";
            s = s + value;
            s = s + "\"";
        }
        s = s + ">";
        Enumeration children = childElements();
        if(children.hasMoreElements()) s = s + "\n";
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            s = s + child.toString();
        }
        s = s + _pcdata;
        s = s + "</";
        s = s + getElementType();
        s = s + ">\n";
        return s;
    }

    /**
     * Add an attribute with the given name to this element.
     * This method is package-private, since only the XML
     * parser is allowed to add attributes. The initial value
     * of a new attribute is the null string.
     */
    void addAttribute (String name) {
        ;
    }

    /**
     * Take an arbitrary XMLElement and figure out what type it is, then
     * figure out what semantic meaning that has within this XMLElement.
     * By default an arbitrary XMLElement has no semantic meaning for its
     * child elements, so it passes the element to its parent to see if the
     * parent has any semantics for it.
     * This is primarily used by the parser to keep the semantic structures
     * within an XMLElement consistant with the childElements.
     */
    void applySemanticsToChild(XMLElement e) {
        if(_parent != null) _parent.applySemanticsToChild(e);
    }

    // The child elements of this element
    private LinkedList _childelements;

    // The attributes of this element
    private HashedMap _attributes;

    // The element type of this element
    private String _elementtype;

    // The character data that is contained in this element
    private String _pcdata;

    // The XMLElement that contains this element (possibly null).
    private XMLElement _parent;

    // The location that thisfile was parsed from
    private String _xmlFileLocation = null;
}

