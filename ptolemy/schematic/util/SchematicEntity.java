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
public class SchematicEntity extends PTMLObject {

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
        super(name);
	_template = et;
	_terminalstyle = et.getTerminalStyle();
        _x = 0;
        _y = 0;
 	//setIcon(DEFAULTICONNAME);
    }

    /**
     * Test if this entity contains the given port.
     */
    public boolean containsPort (SchematicPort port) {
        return _template.containsPort(port);
    }
    
    /**
     * Get the icon of this entity.
     */
    public Icon getIcon () {
        return _template.getIcon();
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
        return _template.ports();
    }

    /** 
     * Reset the TerminalStyle to the TerminalStyle of the 
     * template.
     */
    public void resetTerminalStyle () {
	_terminalstyle = _template.getTerminalStyle();
    }

    /**
     * Set the TerminalStyle that describes this entity.
     */
    public void setTerminalStyle (TerminalStyle tstyle) {
	_terminalstyle = tstyle;
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

    public static final String DEFAULTICONNAME = "default";
    private TerminalStyle _terminalstyle;
    private EntityTemplate _template;
    private double _x;
    private double _y;
}






