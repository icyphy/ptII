/* An IconLibrary stores icons in PTML files.

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
import collections.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// IconLibrary
/**
An IconLibrary is the hierarchical object for organizing Icons.   An 
IconLibrary contains a set of Icons, and a set of other IconLibraries 
called suiblibraries.  All the Icons contained within this library should 
have a unique name and a unique entitytype.   The sublibraries and 
description of this Iconlibrary are kept within a dummy XMLElement with
element type of "header".  This element should be the first child element
to be parsed, allowing the parse to be halted without parsing all of the
icons, since the file may be rather large.
<!-- icon elements will be parsed into class Icon -->
<!ELEMENT entity (description?, entitytype, parameter*, port*)>
<!ATTLIST entity
name ID #REQUIRED
iconlibrary CDATA #REQUIRED>
<!-- The following don't have separate classes. -->
<!ELEMENT description (#PCDATA)>
<!ELEMENT header (description, sublibrary*)>
<!ELEMENT sublibrary (#PCDATA)>

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class IconLibrary extends XMLElement{

    /** Create an IconLibrary object with no attributes.  
     * The Library should then be associated
     * with a URL and parsed before expecting valid results from the other 
     * methods
     */
    public IconLibrary() {
        super("iconlibrary");
        sublibraries = (HashedMap) new HashedMap();
        icons = (HashedMap) new HashedMap();
        header = new XMLElement("header");
        addChildElement(header);
        description = new XMLElement("description");
        header.addChildElement(description);
        setName("");
        setVersion("");

    }

    /** Create an IconLibrary object with the attributes given in the 
     *  HashedMap.
     * The Library should then be associated
     * with a URL and parsed before expecting valid results from the other 
     * methods
     */
    public IconLibrary(HashedMap attributes) {
        super("iconlibrary", attributes);        
        sublibraries = (HashedMap) new HashedMap();
        icons = (HashedMap) new HashedMap();
        header = new XMLElement("header");
        addChildElement(header);
        description = new XMLElement("description");
        header.addChildElement(description);
        if(!hasAttribute("name")) setName("");
        if(!hasAttribute("version")) setVersion("");
   }
        
    /**
     * Add an Icon to this library
     */
    public void addIcon(Icon i) {
        addChildElement(i);
        icons.putAt(i.getName(),i);
    }

    /** 
     * Add a sublibrary to this library.   
     */
    public void addSubLibrary(String name) {
        XMLElement e = new XMLElement("sublibrary");
        e.setPCData(name);
        header.addChildElement(e);
        sublibraries.putAt(name, e);
    }
    
    /**
     * Test if the library contains an Icon with the given name
     */
    public boolean containsIcon(String name) {
        return icons.includesKey(name);
    }

    /**
     * Test if the library contains the sublibrary
     */
    public boolean containsSubLibrary(String name) {
        return sublibraries.includesKey(name);
    }
    
    /** 
     * Get the Icon that is stored in this IconLibrary with the specified 
     * type signature
     */
    public Icon getIcon(EntityType e) {
        return (Icon) icons.at(e);
    }

    /** 
     * Return a long description string of the the Icons in thie Library.
     */
    public String getDescription() {
        return description.getPCData();
    }

    /**
     * Return the name of this library.
     */
    public String getName() {
        return getAttribute("name");
    }

    /** Return the version of this library.
     */
    public String getVersion() {
        return getAttribute("version");
    }

    /** 
     * Return the icons that are contained in this icon library.
     * 
     * @return an enumeration of Icon
     */
    public Enumeration icons() {
        if(icons==null) return null;
        return icons.elements();
    }
    
   /**
     * Remove an icon from this IconLibrary
     */
    public void removeIcon(String name) {
        XMLElement e = (XMLElement) icons.at(name);
        removeChildElement(e);
        sublibraries.removeAt(name);
    }

    /**
     * Remove a sublibrary from this IconLibrary
     */
    public void removeSubLibrary(String name) {
        XMLElement e = (XMLElement) sublibraries.at(name);
        header.removeChildElement(e);
        sublibraries.removeAt(name);
    }

    /** 
     * Set the string that contains the long description of this library.
     */
    public void setDescription(String s) {
        description.setPCData(s);
    }
    
    /** 
     * Set the short name of this library
     */
    public void setName(String s) {
        setAttribute("name", s);
    }

    /** Set the string that represents the version of this library.
     */
    public void setVersion(String s) {
        setAttribute("version",s);
    }

    /** 
     * Return the names of subLibraries of this IconLibrary.   These names 
     * are URL Strings which can be passed to other IconLibrary objects 
     * for parsing.
     * @return an Enumeration of Strings
     */
    public Enumeration subLibraries() {
        return sublibraries.keys();
    }

    /**
     * Take an arbitrary XMLElement and figure out what type it is, then
     * figure out what semantic meaning that has within this XMLElement.
     * By default an arbitrary XMLElement has no semantic meaning for its
     * child elements, so this just returns.
     * This is primarily used by the parser to keep the semantic structures
     * within an XMLElement consistant with the childElements.
     */
    void applySemanticsToChild(XMLElement e) {
        if(e instanceof Icon) {
            // if it's an Icon, then just add it to the list of icons.
            icons.putAt(
                    ((Icon) e).getName(), e);
        } else if(e.getElementType().equals("sublibrary")) {
            // if it's a sublibrary, then add it to the list of sublibraries.
            String filename = e.getPCData();
            sublibraries.putAt(filename,e);
        } else if(e.getElementType().equals("header")) {
            /* Remove the old header and swap in the new one.
               if the new header does not contain a description, then 
               keep the old description */
            if(!e.hasChildElement(description)) {
                header.removeChildElement(description);
                e.addChildElement(description);
            }
            removeChildElement(header);
            header = e;
        } else if(e.getElementType().equals("description")) {
            // if it's a description, then replace the old description that
            // was in the header. Remember that the description is not
            // a child element of this object, but we have to applySemantics
            // to it because header doesn't have it's own object.
            header.removeChildElement(description);
            description = e;
        }    
    }

    private XMLElement description;
    private XMLElement header;
    private HashedMap sublibraries;
    private HashedMap icons;
}

