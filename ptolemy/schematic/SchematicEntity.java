/* A SchematicEntity is an entity stored in a schematic

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

import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.HashedMap;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// SchematicEntity
/**

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicEntity extends SchematicElement {

    /**
     * Create a new SchematicEntity object wtih no set attributes.
     */
    public SchematicEntity () {
        super("entity");
        ports = (HashedMap) new HashedMap();
        setIcon(DEFAULTICONNAME);
    }

    /**
     * Create a new SchematicEntity object with the given attributes and an
     * unspecified entitytype.
     *
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public SchematicEntity (HashedMap attributes) {
        super("entity", attributes);
        ports = (HashedMap) new HashedMap();
        if(!hasAttribute("icon")) setIcon(DEFAULTICONNAME);
    }

    /**
     * Create a new SchematicEntity object with the given attributes and the
     * specified entity type.
     *
     * @param attributes a HashedMap from a String specifying the name of
    * an attribute to a String specifying the attribute's value.
     */
    public SchematicEntity (HashedMap attributes, EntityType et) {
        super("entity", attributes);
        ports = (HashedMap) new HashedMap();
        if(!hasAttribute("icon")) setIcon(DEFAULTICONNAME);
    }

    /**
     * Add a new port to the icon. The port name must be unique within this
     * entity.
     *
     * @throw IllegalActionException if a port with the same name as
     * the new port is already contained in this SchematicEntity.
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
     * Test if this entity contains a port with the
     * given name.
     */
    public boolean containsPort (String name) {
        return ports.includesKey(name);
    }

    /**
     * Get the string that specifies the icon for this entity.
     */
    public String getIcon () {
        return getAttribute("icon");
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
            throw new IllegalActionException("SchematicEntity does not " +
                    "contain a port with name " + name);
        }
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
     * Remove a port from the entity. Throw an exception if
     * a port with this name is not contained in the entity.
     */
    public void removePort (String name) throws IllegalActionException {
        try {
            SchematicPort e = (SchematicPort) ports.at(name);
            ports.removeAt(name);
            removeChildElement(e);
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Entity does not contain a " +
                    "port with name " + name);
        }
    }

    /**
     * Set the Icon that describes this entity.
     *
     * @param iconspec A string specifiying a unique icon within
     * ptolemy.   This string is in the form
     * "hierarchical.library.name.iconname"
     */
    public void setIcon (String iconspec) {
        setAttribute("icon",iconspec);
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
        } else if(e instanceof SchematicParameter) {
            // if a parameter, remove the old one and install the new one.
            parameters.putAt(
                    ((SchematicParameter) e).getName(), e);
        }
    }

    public static final String DEFAULTICONNAME = "default";

    protected HashedMap ports;
}






