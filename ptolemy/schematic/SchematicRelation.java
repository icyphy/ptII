/* A SchematicRelation represents a relation in a schematic.

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

//////////////////////////////////////////////////////////////////////////
//// SchematicRelation 
/**

A SchematicRelation represents a relation in a Ptolemy II schematic.
It contains links, which specify the topology of the schematic.
Every link has a name that specifies the unique port within a schematic that 
it is connected to.   A link name is formed by period concatenating the
entity name and the port name that the link is connected to, such as
"entity.port".
<!-- schematic relations will be parsed into class SchemticRelation -->
<!ELEMENT relation (link)*>
<!ATTLIST relation
name ID #REQUIRED>
<!ELEMENT link EMPTY>
<!ATTLIST link
name CDATA #REQUIRED>


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicRelation extends SchematicElement {

    /** 
     * Create a new SchematicRelation object.
     */
    public SchematicRelation () {
        super("relation");
        links = (HashedMap) new HashedMap();
        setWidth("1");
    }

    /**
     * Create a new SchematicRelation object with the given attributes
     *
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public SchematicRelation (HashedMap attributes) {
        super("relation", attributes);
        links = (HashedMap) new HashedMap();
        if(!hasAttribute("width")) setWidth("1");
    }

    /**
     * Add a new link to this relation. The name of the link
     * is the concatenation of the entity name and the port
     * name, separated by a period.
     * 
     * @return an XMLElement that represents the link.
     */
    public void addLink (String name) {
        XMLElement e = new XMLElement("link");
        e.setAttribute("name", name);
        addChildElement(e);    
        links.putAt(name, e);
    }

    /**
     * Test if this relation contains the given link.
     */
    public boolean containsLink (String name) {
        return links.includesKey(name);
    }

    /**
     * Return a string representing the width of this relation
     */
    public String getWidth() {
        return getAttribute("width");
    }

    /**
     * Return an enumeration over the links in this relation.   Each
     * element in the enumeration will be a string representing a 
     * port that is connected to this relation.
     * 
     * @return an Enumeration of String
     */
    public Enumeration links () {
        return links.keys();
    }

    /** 
     * Remove the link with the given name from this relation.  The link 
     * should refer to a 
     */
    public void removeLink(String name) {
        XMLElement e = (XMLElement) links.at(name);
        removeChildElement(e);
        links.removeAt(name);
    }

    /** 
     * Set the width of this relation.
     */
    public void setWidth(String width) {
        setAttribute("width", width);
    }

    /**
     * Take an arbitrary XMLElement and figure out what type it is, then
     * figure out what semantic meaning that has within this XMLElement.
     * This is primarily used by the parser to keep the semantic structures
     * within an XMLElement consistant with the childElements.
     */
    void applySemanticsToChild(XMLElement e) {
        if(e instanceof SchematicParameter) {
            // if a parameter, remove the old one and install the new one.
            parameters.putAt(
                    ((SchematicParameter) e).getName(), e);
        } else if(e.getElementType().equals("link")) {
            // if a link, add to the list of links.
            links.putAt(
                    e.getAttribute("name"), e);
        }
    }

    HashedMap links;

}

