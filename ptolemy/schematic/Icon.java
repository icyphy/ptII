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
     * Test if this icon contains a graphic in the
     * given format.
     */
    public boolean containsGraphic (String format) {
        return graphics.includesKey(format);
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
        return graphics.elements();
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

    XMLElement description;
    EntityType entitytype;
    HashedMap graphics;

}

