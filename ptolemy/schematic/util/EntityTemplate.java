/* A EntityTemplate is a generic Entity stored in the EntityLibrary

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

package ptolemy.schematic.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.CircularList;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// EntityTemplate
/**
An Entity Template represents the immutable parts of a schematic entity. 
The template includes the icon, ports, and default terminalstyle for the 
entity.  Entity templates are stored in the entity library.


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class EntityTemplate extends PTMLObject {

    /**
     * Create a new EntityTemplate object wtih no set attributes.
     */
    public EntityTemplate () {
        this("entitytemplate");
    }

    /**
     * Create a new EntityTemplate object with the given attributes and an
     * unspecified entitytype.
     *
     * @param attributes a CircularList from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public EntityTemplate (String name) {
        super(name);
	_icon = null;
	_terminalstyle = null;
        _ports = (CircularList) new CircularList();
	//setIcon(DEFAULTICONNAME);
    }

    /**
     * Add a new port to the template. The port name must be unique within this
     * entity template.
     *
     * @throw IllegalActionException if a port with the same name as
     * the new port is already contained in this EntityTemplate.
     */
    public void addPort (EntityPort port) throws IllegalActionException {
        if(containsPort(port))
            throw new IllegalActionException("Port with name " + 
		 port.getName() + " already exists.");
        _ports.insertLast(port);
    }

    /**
     * Test if this entity contains a port with the
     * given name.
     */
    public boolean containsPort (EntityPort port) {
        return _ports.includes(port);
    }
    
    /**
     * Get the string that specifies the icon for this entity.
     */
    public Icon getIcon () {
        return _icon;
    }

    /**
     * Get the string that specifies the icon for this entity.
     */
    public TerminalStyle getTerminalStyle () {
        return _terminalstyle;
    }

    /**
     * Return the port contained in this object with the given name.
     *
     * @throw IllegalActionException if no port exists with the given name.
     */
    /*   public EntityPort getPort(String name)
	throws IllegalActionException {
        try {
            EntityPort s = (EntityPort) _ports.at(name);
            return s;
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("EntityTemplate does not " +
	        "contain a port with name " + name);
        }
	}*/

    /**
     * Return an enumeration over the ports in this object.
     *
     * @return an enumeration of EntityPorts
     */
    public Enumeration ports() {
        return _ports.elements();
    }

    /**
     * Remove a port from the entity. Throw an exception if
     * a port with this name is not contained in the entity.
     */
    public void removePort (EntityPort port) throws IllegalActionException {
        try {
	    _ports.removeOneOf(port);
	}
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Entity does not contain a " +
                    "port with name " + port.getName());
        }
    }

    /**
     * Set the Icon that describes this entity.
     */
    /*
     * @param iconspec A string specifiying a unique icon within
     * ptolemy.   This string is in the form
     * "hierarchical.library.name.iconname"
     */
    public void setIcon (Icon icon) {
	_icon = icon;
    }

    /**
     * Set the TerminalStyle that describes this entity.
     */
    public void setTerminalStyle (TerminalStyle tstyle) {
	_terminalstyle = tstyle;
    }

    public static final String DEFAULTICONNAME = "default";
    private CircularList _ports;
    private Icon _icon;
    private TerminalStyle _terminalstyle;
}






