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
     * a unique name.
     */
    public SchematicEntity () {
        this("entity", null);
	try {
	    setName(_createUniqueName());
	} catch (NameDuplicationException ex) {
	    throw new InternalErrorException("Unique name was not unique!");
	}
    }

    /**
     * Create a new SchematicEntity object with no template and
     * the given name.
     */
    public SchematicEntity (String name) {
        this(name, null);
    }

    /** 
     * Create a new entity with the given template, and a unique name
     * based on the name of the template
     */
    public SchematicEntity (SchematicEntity et) {
        this("entity", et);
        setTemplate(et);
        try {
            setName(_createUniqueName());
        } 
        catch (NameDuplicationException e) {
	    throw new InternalErrorException("Unique name was not unique!");
	}
    }

    /**
     * Create a new SchematicEntity object with the given name and entity
     * template.
     */
    public SchematicEntity (String name, SchematicEntity et) {
        super(name, et);
        _x = 0;
        _y = 0;
        _ports = new NamedList();
        _terminals = new NamedList();
	_terminalstyle = null;
        // In case our template has a terminalstyle.
  	_createTerminals();        
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
	port.setContainer(this);
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
	terminal.setContainer(this);
    }

    /**
     * Clone this entity.  Return a new SchematicEntity with a unique name
     * and copies of this entities ports and terminals.
     */
    public Object clone() throws CloneNotSupportedException {
       try {
           SchematicEntity newobj = 
           //    new SchematicEntity(_createUniqueName(), 
           //            (SchematicEntity)getTemplate());
               (SchematicEntity) super.clone();
           newobj._ports = new NamedList();
           Enumeration objects = ports();
           while(objects.hasMoreElements()) {
               PTMLObject object = (PTMLObject)objects.nextElement();
               newobj.addPort((SchematicPort)object.clone());
           }
           
           /*newobj._terminals = new NamedList();
           objects = terminals();
           while(objects.hasMoreElements()) {
               PTMLObject object = (PTMLObject)objects.nextElement();
               newobj.addTerminal((SchematicTerminal)object.clone());
               }*/

           return newobj;
       } catch (Exception e) {
           if(e instanceof CloneNotSupportedException)
               throw (CloneNotSupportedException)e;
           else 
               throw new CloneNotSupportedException(e.getMessage());
       }
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
    public boolean containsTerminal (SchematicTerminal terminal) {
        return _terminals.includes(terminal);
    }
    
   /**
     * Test if this relation contains a terminal with the given name.
     */
    public boolean containsTerminal (String name) {
        return _terminals.get(name) != null;
    }

    /**
     * Get the icon of this entity.
     */
    public Icon getIcon () {
        if(hasTemplate() && (_icon == null)) 
            return ((SchematicEntity) getTemplate()).getIcon();
        else
            return _icon;
    }

    /**
     * Get a string representing the implementation of this entity.  This
     * may be a java class name, or a URL for a PTML schematic object.
     */
    public String getImplementation () {
        if(hasTemplate() && (_implementation == null)) 
            return ((SchematicEntity) getTemplate()).getImplementation();
        else
            return _implementation;
    }

    /**
     * Get the terminal map for this entity.
     */
    public TerminalMap getTerminalMap () {
        if(hasTemplate() && (_terminalmap == null)) 
            return ((SchematicEntity) getTemplate()).getTerminalMap();
        else
	    return _terminalmap;
    }

    /**
     * Get the terminal style of this entity.
     */
    public TerminalStyle getTerminalStyle () {
       if(hasTemplate() && (_terminalstyle == null)) 
            return ((SchematicEntity) getTemplate()).getTerminalStyle();
       else
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
     * Return the schematic terminal that has the given name.
     * Throw an exception if there is no terminal with the
     * given name in this relation.
     */
    public SchematicTerminal getTerminal (String name)         
            throws IllegalActionException {
        SchematicTerminal terminal = (SchematicTerminal) _terminals.get(name);
        if(terminal == null) throw new IllegalActionException(
                "Terminal not found with name " + name);
        return terminal;
    }     

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
    public void removePort (SchematicPort port) 
	throws IllegalActionException {
        try {
	    _ports.remove(port);
	    port.setContainer(null);
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
    public void removeTerminal (SchematicTerminal terminal) 
	throws IllegalActionException {
        try {
	    _terminals.remove(terminal);
	    terminal.setContainer(null);
	}
        catch (NoSuchElementException e) {
            throw new IllegalActionException("Entity does not contain a " +
                    "terminal with name " + terminal.getName());
        }
    }

    /**
     * Set the icon that describes this entity.  Note that if this entity
     * has a template, this corresponds to overriding the value that is 
     * set in the template, but does not affect the template in any way.
     * to return to the value set in the template, call this method with a 
     * null argument.
     */
    public void setIcon (Icon icon) {
	_icon = icon;
    }

    /** 
     * Set the string that represents the implementation of this entity.
     * Note that if this entity
     * has a template, this corresponds to overriding the value that is 
     * set in the template, but does not affect the template in any way.
     * to return to the value set in the template, call this method with a 
     * null argument.
     * @see #getImplementation
     */
    public void setImplementation (String implementation) {
	_implementation = implementation;
    }

    /**
     * Set the template entity for this entity.  In addition, create terminals
     * for each terminal in the terminal style.
     */
    public void setTemplate(PTMLObject obj) {
        super.setTemplate(obj);
        _createTerminals();
    }

    /**
     * Set the terminal map that describes this entity. 
     * Note that if this entity
     * has a template, this corresponds to overriding the value that is 
     * set in the template, but does not affect the template in any way.
     * to return to the value set in the template, call this method with a 
     * null argument.
     */
    public void setTerminalMap (TerminalMap tmap) {
	_terminalmap = tmap;
    }

    /**
     * Set the terminal style that describes this entity.  If the terminal
     * style is already set, then do nothing.  If the terminal style changes
     * and the new terminal style is not null, then create a schematicTerminal
     * for each terminal in the terminalstyle.
     */
    public void setTerminalStyle (TerminalStyle style) {
        _terminalstyle = style;
        _createTerminals();
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
	return new EnumerationIterator(terminals());
    }

    /**
     * Return a string this representing Entity.
     */
    protected String _description(int indent, int bracket) {
        String result = "";
        if(bracket == 0) 
            result += super._description(indent, 0);
        else 
            result += super._description(indent, 1);

        result += " icon {\n";
        result += _getDescription(_icon, indent);

	result += _getIndentPrefix(indent) + "} implementation {\n";
        if(_implementation == null) 
            result += _getIndentPrefix(indent + 1) + "null\n";
        else
            result += _getIndentPrefix(indent + 1) + 
                _implementation + "\n";
        
	result += _getIndentPrefix(indent) + "} terminalstyle {\n";
        result += _getDescription(_terminalstyle, indent);
        
	result += _getIndentPrefix(indent) + "} terminalmap {\n";
        if(_terminalmap == null) 
            result += _getIndentPrefix(indent + 1) + "null\n";
        else
            result += _getIndentPrefix(indent + 1) + 
                _terminalmap.toString() + "\n";
        
	result += _getIndentPrefix(indent) + "} ports {\n";
        result += _enumerationDescription(ports(), indent);

        result += _getIndentPrefix(indent) + "} terminals {\n";
        result += _enumerationDescription(terminals(), indent);

        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    /** 
     * Create terminals based on the terminalstyle of this entity.
     * This method is called after the template or terminal style is set
     * to create new terminals for this entity.
     */
    protected void _createTerminals() {
	// Blow away the old terminals
        // FIXME This may be bad if things are connected to the old terminals.
	_terminals.removeAll();

	if(getTerminalStyle() != null) {
	    Enumeration templateTerminals = getTerminalStyle().terminals();
	    while(templateTerminals.hasMoreElements()) {
		SchematicTerminal terminal = 
		    (SchematicTerminal)templateTerminals.nextElement();
		try {
                    addTerminal(new SchematicTerminal(terminal.getName(), 
						      terminal));
                    
		} catch (Exception ex) {
		    throw new InternalErrorException(ex.getMessage());
		    // This should never happen, because the terminals in the
		    // template must follow the same rules as the schematic 
		    // terminals.
		}
	    }
	}
    }

    public static final String DEFAULTICONNAME = "default";
    private Icon _icon;
    private String _implementation;
    private NamedList _ports;
    private NamedList _terminals;
    private TerminalStyle _terminalstyle;
    private TerminalMap _terminalmap;
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






