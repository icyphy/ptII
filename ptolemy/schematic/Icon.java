/* An Icon is the graphical representation of a schematic entity.

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

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// Icon 
/**

An icon is the graphical representation of a schematic entity.
Icons are created by an IconLibrary in response to a request
for an icon. Each icon is represented in an icon library
XML files by the <icon> element.   
<!-- icon elements will be parsed into class Icon -->
<!ELEMENT icon (description, entitytype, graphic*, parameter*, port*)>
<!ATTLIST icon
name ID #REQUIRED>

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Icon extends XMLElement {

    /**
     * Create a new Icon. By default, the icon contains no graphic
     * representations, no attributes, and has an entity type that 
     * exists, but is not specified.
     */
    public Icon () {
        super("icon");
        graphics = (HashedMap) new HashedMap();
        entitytype = new EntityType();
        addChildElement(entitytype);
        description = new XMLElement("description");
        ports = (HashedMap) new HashedMap();
        setName("");
    }

    /**
     * Create a new Icon, containing the specified attributes.
     * By default, the icon contains no graphic
     * representations, and has an entity type that exists, but
     * is not specified.
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public Icon (HashedMap attributes) {
        super("icon", attributes);
        graphics = (HashedMap) new HashedMap();
        entitytype = new EntityType();
        description = new XMLElement("description");
        addChildElement(entitytype);
        ports = (HashedMap) new HashedMap();
        if(!hasAttribute("name")) setName("");
    }
   

    /**
     * Create a new Icon, containing the specified attributes and the
     * specified entity type. By default, the icon contains no graphic
     * representations.
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     * @param et an EntityType object.
     */
    public Icon (HashedMap attributes, EntityType et) {
        super("icon", attributes);
        graphics = (HashedMap) new HashedMap();
        entitytype = et;
        description = new XMLElement("description");
        addChildElement(entitytype);
        ports = (HashedMap) new HashedMap();
        if(!hasAttribute("name")) setName("");
    }

    /** 
     * Add a new graphic to the icon. The format
     * is specified in the "format" attribute of the XMLElement.
     * The XMLElement must be of element type "graphic".
     *  
     * @throw IllegalActionException if the element is not of element type 
     * "graphic"
     * @throw IllegalActionException if a graphic with the same type as 
     * the element is already associated with this Icon. 
     */
    public void addGraphic (XMLElement element) throws IllegalActionException {
        String type = element.getElementType();
        if(!type.equals("graphic")) 
            throw new IllegalActionException("Element must be of " +
                    " type graphic");
        String format = element.getAttribute("format");
        if(containsGraphic(format)) 
            throw new IllegalActionException("Graphic of type " + format + 
                    " already exists.");
        graphics.putAt(format, element);
        addChildElement(element);
    }

    /** 
     * Add a new port to the icon. The port name must be unique within this
     * icon.
     *  
     * @throw IllegalActionException if a port with the same name as 
     * the new port is already contained in this Icon. 
     */
    public void addPort (SchematicPort port) throws IllegalActionException {
        String name = port.getName();
        if(containsPort(name)) 
            throw new IllegalActionException("Port with name " + name + 
                    " already exists.");
        ports.putAt(name, port);
        addChildElement(port);
    }

    /** 
     * Test if this icon contains a graphic in the
     * given format.
     */
    public boolean containsGraphic (String format) {
        return graphics.includesKey(format);
    }

    /** 
     * Test if this icon contains a port with the
     * given name.
     */
    public boolean containsPort (String name) {
        return ports.includesKey(name);
    }

    /** 
     * Given a graphic format attribute, return the graphic
     * that has that format. 
     * 
     * @return an XMLElement with element type of "graphic"
     * @throw IllegalActionException if no graphic exists in the given format
     */
    public XMLElement getGraphic (String format) 
    throws IllegalActionException {
        try {            
            XMLElement s = (XMLElement) graphics.at(format);
            return s;
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Icon does not contain a " +
                    "graphic of format " + format);
        }
    }

    /** 
     * Return the port contained in this object with the given name.
     * 
     * @throw IllegalActionException if no port exists with the given name.
     */
    public SchematicPort getPort(String name) 
    throws IllegalActionException {
        try {            
            SchematicPort s = (SchematicPort) ports.at(name);
            return s;
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Icon does not contain a " +
                    "port with name " + name);
        }
    }

    /** 
     * Return a long description string of the the Icons in thie Library.
     */
    public String getDescription() {
        return description.getPCData();
    }

    /** 
     * Return the EntityType of this Icon
     */
    public EntityType getEntityType () {
        return entitytype;
    }

    /** 
     * Return the name of this Icon.  
     */
    public String getName() {
        return getAttribute("name");
    }

    /**
     * Return an enumeration over the names of the graphics formats
     * supported by this icon. 
     *
     * @return Enumeration of String.
     */
    public Enumeration graphicFormats() {
        return graphics.keys();
    }

    /**
     * Return an enumeration over the ports in this object.
     *
     * @return an enumeration of SchematicPorts
     */
    public Enumeration ports() {
        return ports.elements();
    }

    /** 
     * Remove a graphic from the icon. Throw an exception if
     * a graphic in this format is not associated with the Icon.
     */
    public void removeGraphic (String format) throws IllegalActionException {
        try {
            XMLElement e = (XMLElement) graphics.at(format);
            graphics.removeAt(format);
            removeChildElement(e);
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Icon does not contain a " +
                    "graphic of format " + format);
        }
    }

    /** 
     * Remove a port from the icon. Throw an exception if
     * a port with this name is not contained in the Icon.
     */
    public void removePort (String name) throws IllegalActionException {
        try {
            SchematicPort e = (SchematicPort) ports.at(name);
            ports.removeAt(name);
            removeChildElement(e);
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Icon does not contain a " +
                    "port with name " + name);
        }
    }

    /** 
     * Set the string that contains the long description of this icon.
     */
    public void setDescription(String s) {
        description.setPCData(s);
    }
  
    /**
     * Set the name of this Icon.
     */
    public void setName(String name) {
        setAttribute("name", name);
    }

    /**
     * Take an arbitrary XMLElement and figure out what type it is, then
     * figure out what semantic meaning that has within this XMLElement.
     * This is primarily used by the parser to keep the semantic structures
     * within an XMLElement consistant with the childElements.
     */
    void applySemanticsToChild(XMLElement e) {
        if(e instanceof SchematicPort) {
            // if it's a Port, then just add it to the list of ports.
            ports.putAt(
                    ((SchematicPort) e).getName(), e);
        } else if(e.getElementType().equals("graphic")) {
            // if it's a Graphic, then just add it to the list of graphics.
            graphics.putAt(
                    e.getAttribute("format"), e);
        } else if(e instanceof EntityType) {
            // if an entitytype, remove the old one and install the new one.
            removeChildElement(entitytype);
            entitytype = (EntityType) e;
        } else if(e.getElementType().equals("description")) {
            // if a description, remove the old one and install the new one.
            removeChildElement(description);
            description = e;
        }
    }

    XMLElement description;
    EntityType entitytype;
    HashedMap graphics;
    HashedMap ports;

}

