/* A XMLElement represents a PtII design

 Copyright (c) 1998 The Regents of the University of California.
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

import java.util.Enumeration;
import collections.HashedMap;
import collections.HashedSet;

//////////////////////////////////////////////////////////////////////////
//// XMLElement
/**

An XMLElement is the abstract superclass of classes that represent
XML elements. It contains some basic support for accessing elements.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class XMLElement {
    
    /** 
     * Create a new XMLElement with element type given by the string.
     * The element has no attributes and no child elements.
     *
     * @param name The element type
     */
    XMLElement(String type) {
        attributes = (HashedMap) new HashedMap();
        childelements = (HashedSet) new HashedSet();
        elementtype = type;
    }

    /** 
     * Create a new XMLElement with element type with the given name and the
     * given attributes.  The element starts with no child elements.
     *
     * @param name The element type
     * @param attribs The attributes of this XMLElement.
     */
    XMLElement(String type, HashedMap attribs) {
        attributes = attribs;
        elementtype = type;
        childelements = (HashedSet) new HashedSet();
    }

    /** Add a child element to this element
     */
    public void addChildElement(XMLElement e) {
        childelements.include(e);
    }
    
    /**
     * Add the String to the end of the current PCDATA for this element.
     */
    public void appendPCData(String s) {
        pcdata = pcdata + s;
    }

    /**
     * Return an enumeration over the names of the attributes
     * in this schematic.
     */
    public Enumeration attributeNames () {
        return attributes.keys();
    }

    /** Return an Enumaration of all the child elements of this element.
     *  
     *  @return an Enumeration of XMLElements
     */
    public Enumeration childElements() {
        return childelements.elements();
    }

    /**
     * Return the value of the attribute with the given name.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public String getAttribute (String name) {
        return (String) attributes.at(name);
    }

    /** Return the type of this XMLElement.  The type is immutably set when
     *  the XMLElement is created.  
     */
    public String getElementType() {
        return elementtype;
    }
    
    /**
     * Return the parent element of this element, or null if the parent 
     * has not been set.
     */
    public XMLElement getParent() {
        return parent;
    }

    /**
     * Return the PCData that is associated with this XMLElement.
     */
    public String getPCData() {
        return pcdata;
    }
    
    /**
     * Test if this schematic has the attribute wuth the given name.
     */
    public boolean hasAttribute (String name) {
        return attributes.includesKey(name);
    }

    /** Test if the element is a child element of this element 
     */
    public boolean hasChildElement(XMLElement e) {
        return childelements.includes(e);
    }

    /** Remove an child element from this element
     */
    public void removeChildElement(XMLElement e) {
        childelements.exclude(e);
    }

    /**
     * Set the attribute with the given name to the given value.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public void setAttribute (String name, String value) {
        attributes.putAt(name, value);
    }

    /** 
     * Set the parent element of this element.
     */
    public void setParent(XMLElement p) {
        parent = p;
    }

    /** 
     * Set the text of this element to the given string. 
     */
    public void setPCData(String s) {
        pcdata = s;
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

    // The child elements of this element
    private HashedSet childelements;
    // The attributes of this element
    private HashedMap attributes;
    // The element type of this element
    private String elementtype;
    // The character data that is contained in this element
    private String pcdata;
    // The XMLElement that contains this element (possibly null).
    private XMLElement parent;
}

