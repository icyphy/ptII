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

import java.util.*;
import ptolemy.kernel.util.*;
import diva.util.*;
import diva.graph.model.*;

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
public class SchematicEntity extends PTMLTemplateObject 
    implements diva.graph.model.CompositeNode {

    /**
     * Create a new SchematicEntity object with no template and
     * the name "SchematicEntity".
     */
    //    public SchematicEntity () {
    //    this("SchematicEntity", null);
    // }

    /**
     * Create a new SchematicEntity object with the given entity template and
     * the name of the template.
     */
    //public SchematicEntity (EntityTemplate et) {
    //    this(et.getName(), et);
    //}

    /**
     * Create a new SchematicEntity object with the given name and entity
     * template.
     */
    public SchematicEntity (String name, EntityTemplate et) {
        super(name, et);
	if(et != null) 
	    _terminalstyle = et.getTerminalStyle();
	else 
	    _terminalstyle = null;
        _x = 0;
        _y = 0;
        _ports = new NamedList();
        _terminals = new NamedList();
 	//setIcon(DEFAULTICONNAME);
    }

    /**
     * Add a new port to the schematic. The port name must be unique
     * within this schematic.
     *
     *  @exception IllegalActionException If the port has no name.
     *  @exception NameDuplicationException If the name of the port
     *  coincides with the name of another port 
     *  contained in this entity.
     */
    public void addPort (SchematicPort port) 
            throws IllegalActionException, NameDuplicationException {
        _ports.append(port);
    }

    /**
     * Add a new terminal to the schematic. The terminal name must be unique
     * within this schematic.
     *
     *  @exception IllegalActionException If the terminal has no name.
     *  @exception NameDuplicationException If the name of the terminal
     *  coincides with the name of another terminal 
     *  contained in this entity.
     */
    public void addTerminal (SchematicTerminal terminal) 
            throws IllegalActionException, NameDuplicationException {
        _terminals.append(terminal);
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
        if(hasTemplate()) 
            return ((EntityTemplate) getTemplate()).getIcon();
        else
            return null;
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
	    _ports.remove(port);
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
    public void removeTerminal (SchematicTerminal port) 
	throws IllegalActionException {
        try {
	    _ports.remove(port);
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

    // diva.graph.Node
    /**
     * Return the parent graph of this node.
     */
    public Graph getParent() {
	return _parent;
    }

    /* Get the semantic object of this node. Generally this
     * is used when this node is a "wrapper" for some other object
     * or model with deeper meaning.
     */
    public Object getSemanticObject() {
	return _semanticObject;
    }
    
    /* Get the visual object of this node. Generally this
     * is used when this node has a visual representation.
     */
    public Object getVisualObject() {
	return _visualObject;
    }
    
    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges() {
	return new NullIterator();
    }

    /**
     * Return the visited flag for this node.  This is typically used
     * by graph traversal algorithms.
     */
    public boolean isVisited() {
	return _visited;
    }
		
    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges() {
	return new NullIterator();
    }

    /** Set the parent of this node, that is, the graph in
     * which it is contained.
     */
    public void setParent(Graph g) {
        _parent = g;
    }

    /**  Set the semantic object of this node. Generally this
     * is used when this node is a "wrapper" for some other object
     * or model with deeper meaning.
     */
    public void setSemanticObject(Object o) {
	_semanticObject = o;
    }

    /**  Set the visual object of this node.
     */
    public void setVisualObject(Object o) {
	_visualObject = o;
    }

    /**
     * Set the visited flag for this node.  Algorithms that use this
     * flag are responsible for setting the visited flag to "false"
     * before they begin a traversal (in other words,
     * they cannot expect that a previous traversal has
     * left the nodes unmarked).
     */
    public void setVisited(boolean val) {
	_visited = val;
    }

    // diva.graph.Graph
    /**
     * Return true if this graph contains the given node.
     */
    public boolean contains(Node n) {
	//FIXME: This Nameable cast is dangerous
	if(_ports.includes((Nameable)n)) return true;
	// FIXME don't worry about terminals for right now.
	//	else if(_terminals.includes(n)) return true;
	else return false;
    }

    /**
     * Return the number of nodes contained in
     * this graph.
     */
    public int getNodeCount() {
	return _ports.size();
    }

    /**
     * Provide an iterator over the nodes in this
     * graph.  This iterator does not support removal
     * operations.
     */
    public Iterator nodes() {
	return new EnumerationIterator(ports());
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
	    result += port._description(indent + 1);
        }
        //       result += _getIndentPrefix(indent) + "terminalstyle";
        //result += getTerminalStyle().getFullName();
        result += _getIndentPrefix(indent) + "terminals\n";
        els = terminals();
        while(els.hasMoreElements()) {
            SchematicTerminal term = (SchematicTerminal) els.nextElement();
	    result += term._description(indent + 1);
            }    
        return result;
    }

    public static final String DEFAULTICONNAME = "default";
    private TerminalStyle _terminalstyle;
    private NamedList _ports;
    private NamedList _terminals;
    private double _x;
    private double _y;

    /**
     * The graph to which this node belongs.
     */
    private Graph _parent = null;

    /**
     * Whether or not this node has been visited.
     */
    private boolean _visited = false;

    /**
     * The underlying semantic object.
     */
    private Object _semanticObject = null;

    /**
     * The visual representation.
     */
    private Object _visualObject = null;
}






