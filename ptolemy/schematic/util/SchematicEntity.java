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

package ptolemy.schematic.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.CircularList;
import ptolemy.kernel.util.IllegalActionException;

 //////////////////////////////////////////////////////////////////////////
//// SchematicEntity
/**
The SchematicEntity class represents an Entity within a Schematic. 
A schematic entity is immutably associated with an entity template in some 
entity library.   The template specifies the immutable aspects of the entity, 
such as its icon, ports, and a default terminal style.  The entity has its 
own terminal style, which is set to the terminal style of the template 
upon creation (or by the resetTerminalStyle method).  

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicEntity extends PTMLTemplateObject {

    /**
     * Create a new SchematicEntity object with no set attributes.
     */
    public SchematicEntity (EntityTemplate et) {
        this(et.getName(), et);
    }

    /**
     * Create a new SchematicEntity object with the given attributes and an
     * unspecified entitytype.
     *
     * @param attributes a CircularList from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public SchematicEntity (String name, EntityTemplate et) {
        super(name, et);
	_terminalstyle = et.getTerminalStyle();
        _x = 0;
        _y = 0;
        _ports = new CircularList();
        _terminals = new CircularList();
 	//setIcon(DEFAULTICONNAME);
    }

    /**
     * Add a new port to the schematic. The port name must be unique
     * within this schematic.
     *
     * @throw IllegalActionException if a port with the same name as
     * the new port is already contained in this Schematic.
     */
    public void addPort (SchematicPort port) throws IllegalActionException {
        if(containsPort(port))
            throw new IllegalActionException("Port with name " + 
		 port.getName() + " already exists.");
        _ports.insertLast(port);
    }

    /**
     * Add a new port to the schematic. The port name must be unique
     * within this schematic.
     *
     * @throw IllegalActionException if a port with the same name as
     * the new port is already contained in this Schematic.
     */
    public void addPort (SchematicTerminal port) throws IllegalActionException {
        if(containsTerminal(port))
            throw new IllegalActionException("Terminal with name " + 
		 port.getName() + " already exists.");
        _ports.insertLast(port);
    }

    /**
     * Test if this entity contains the given port.
     */
    public boolean containsPort (SchematicPort port) {
        return _ports.includes(port);
    }
    
    /**
     * Test if this entity contains the given port.
     */
    public boolean containsTerminal (SchematicTerminal port) {
        return _ports.includes(port);
    }
    
    /**
     * Get the icon of this entity.
     */
    public Icon getIcon () {
        return ((EntityTemplate) getTemplate()).getIcon();
    }

    /**
     * Get the terminal style of this entity.
     */
    public TerminalStyle getTerminalStyle () {
        return _terminalstyle;
    }

    /**
     * Return the port contained in this object with the given name.
     *
     * @throw IllegalActionException if no port exists with the given name.
     */
    /*   public SchematicPort getPort(String name)
	throws IllegalActionException {
	_template.getPort(name);
	}*/

    /**
     * Return the X position of this Entity
     */
    public double getX() {
        return _x;
    }

    /**
     * Return the Y position of this Entity
     */
    public double getY() {
        return _y;
    }

    /**
     * Return an enumeration over the ports in this object.
     *
     * @return an enumeration of SchematicPorts
     */
    public Enumeration ports() {
        return _ports.elements();
    }

    /**
     * Remove a port from the entity. Throw an exception if
     * a port with this name is not contained in the entity.
     */
    public void removePort (SchematicPort port) throws IllegalActionException {
        try {
	    _ports.removeOneOf(port);
	}
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Entity does not contain a " +
                    "port with name " + port.getName());
        }
    }

    /**
     * Remove a port from the entity. Throw an exception if
     * a port with this name is not contained in the entity.
     */
    public void removeTerminal (SchematicTerminal port) throws IllegalActionException {
        try {
	    _ports.removeOneOf(port);
	}
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Entity does not contain a " +
                    "port with name " + port.getName());
        }
    }

    /** 
     * Reset the TerminalStyle to the TerminalStyle of the 
     * template.
     */
    public void resetTerminalStyle () {
	_terminalstyle = ((EntityTemplate) getTemplate()).getTerminalStyle();
    }

    /**
     * Set the TerminalStyle that describes this entity.
     */
    public void setTerminalStyle (TerminalStyle style) {
	_terminalstyle = style;
    }
    
   /**
     * Set the X location of this Entity.
     */
    public void setX(double x) {
        _x = x;
    }

   /**
     * Set the Y location of this Entity.
     */
    public void setY(double y) {
        _y = y;
    }

    /**
     * Return an enumeration over the terminals in this object.
     *
     * @return an enumeration of SchematicTerminals
     */
    public Enumeration terminals() {
        return _terminals.elements();
    }

    /**
     * Return a string this representing Entity.
     */
    protected String _description(int indent) {
        String result = super._description(indent);
        result += _getIndentPrefix(indent) + "ports\n";
        Enumeration els = ports();
        while(els.hasMoreElements()) {
            SchematicPort port = (SchematicPort) els.nextElement();
	    result += port._description(indent);
        }
        //       result += _getIndentPrefix(indent) + "terminalstyle";
        //result += getTerminalStyle().getFullName();
        result += _getIndentPrefix(indent) + "terminals\n";
        els = terminals();
        while(els.hasMoreElements()) {
            SchematicTerminal term = (SchematicTerminal) els.nextElement();
	    result += term._description(indent);
            }    
        return result;
    }

    public static final String DEFAULTICONNAME = "default";
    private TerminalStyle _terminalstyle;
    private CircularList _ports;
    private CircularList _terminals;
    private double _x;
    private double _y;
}






