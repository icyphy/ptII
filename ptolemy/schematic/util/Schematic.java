/* A Schematic represents a PtII design

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
//// Schematic
/**

A schematic represents a PtolemyII schematic. Schematic objects
are constructed from (and written to) XML files by an instance
of SchematicParser. A schematic objects contains information
about entities, relations, and parameters, in a form that gives
the client enough information about the system to construct
and run a simulation, or to display and edit the visual schematic
(with the help of other classes such as Icon and IconLibrary).


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Schematic extends PTMLObject 
    implements diva.graph.model.Graph {

    /**
     * Create a new Schematic object.
     */
    public Schematic () {
        this("schematic");
   }

    /**
     * Create a new Schematic object with the specified attributes
     */
    public Schematic (String name) {
        super(name);
        _ports = new NamedList();
        _terminals = new NamedList();
        _entities = new NamedList();        
        _relations = new NamedList();        
    }

    /**
     * Add a new entity to this schematic. The name
     * of the entity must be unique in this schematic.
     *  @exception IllegalActionException If the entity has no name.
     *  @exception NameDuplicationException If the name of the entity
     *  coincides with the name of another entity 
     *  contained in this schematic.
     */
    public void addEntity (SchematicEntity entity) 
            throws IllegalActionException, NameDuplicationException {
        _entities.append(entity);
	//	entity.setContainer(this);
    }

    /**
     * Add a new port to the schematic. The port name must be unique
     * within this schematic.
     *  @exception IllegalActionException If the port has no name.
     *  @exception NameDuplicationException If the name of the port
     *  coincides with the name of another port 
     *  contained in this schematic.
     */
    public void addPort (SchematicPort port) 
            throws IllegalActionException, NameDuplicationException {
        _ports.append(port);
    }

   /**
     * Add a new relation to this schematic. The name
     * of the relation must be unique in this schematic.
     *  @exception IllegalActionException If the relation has no name.
     *  @exception NameDuplicationException If the name of the relation
     *  coincides with the name of another relation 
     *  contained in this schematic.
     */
    public void addRelation (SchematicRelation relation) 
            throws IllegalActionException, NameDuplicationException {
        _relations.append(relation);
    }

    /**
     * Add a new terminal to the schematic. The terminal name must be unique
     * within this schematic.
     *  @exception IllegalActionException If the terminal has no name.
     *  @exception NameDuplicationException If the name of the terminal
     *  coincides with the name of another terminal 
     *  contained in this schematic.
     */
    public void addTerminal (SchematicTerminal terminal) 
            throws IllegalActionException, NameDuplicationException {
        _terminals.append(terminal);
	//	terminal.setContainer(this);
    }

    /**
     * Test if there is an entity with the given name in the
     * schematic.
     */
    public boolean containsEntity (SchematicEntity entity) {
        return _entities.includes(entity);
    }

    /**
     * Test if this schematic contains a port with the
     * given name.
     */
    public boolean containsPort (SchematicPort port) {
        return _ports.includes(port);
    }
    
    /**
     * Test if there is an relation with the given name in the
     * schematic.
     */
    public boolean containsRelation (SchematicRelation relation) {
        return _relations.includes(relation);
    }

    /**
     * Test if this schematic contains a terminal with the
     * given name.
     */
    public boolean containsTerminal (SchematicTerminal terminal) {
        return _terminals.includes(terminal);
    }
    
     /**
     * Return an enumeration over the entities in this
     * schematic.
     *
    public Enumeration entityNames () {
        return _entities.keys();
    }
*/
    /**
     * Return the schematic entity that has the given name.
     * Throw an exception if there is no entity with the
     * given name in this schematic.
     *
    public SchematicEntity getEntity (String name) {
        return (SchematicEntity) _entities.at(name);
    }
    */
     /**
     * Return the schematic relation that has the given name.
     * Throw an exception if there is no relation with the
     * given name in this schematic.
     *
    public SchematicRelation getRelation (String name) {
        return (SchematicRelation) _relations.at(name);
    }
*/
    /**
     * Return an enumeration over the entities in this
     * schematic.
     */
    public Enumeration entities() {
        return _entities.elements();
    }

    /**
     * Return an enumeration over the ports in this schematic.
     *
     * @return an enumeration of SchematicPorts
     */
    public Enumeration ports() {
        return _ports.elements();
    }

    /**
     * Return an enumeration over the relations in this
     * schematic.
     */
    public Enumeration relations() {
        return _relations.elements();
    }

    /**
     * Return an enumeration over the terminals in this schematic.
     *
     * @return an enumeration of SchematicTerminals
     */
    public Enumeration terminals() {
        return _terminals.elements();
    }

    /**
     * Remove the schemtic entity with the given name.
     * Throw an exception if the entity does not exist
     * in this schematic.
     */
    public void removeEntity(SchematicEntity entity) {
 	_entities.remove(entity);
	//	entity.setContainer(null);
    }

    /**
     * Remove a port from the schematic. Throw an exception if
     * a port with this name is not contained in the schematic.
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
     * Remove the schematic relation with the given name.
     * Throw an exception if the relation does not exist
     * in this schematic.
     */
    public void removeRelation(SchematicRelation relation) {
        _relations.remove(relation);
    }

    /**
     * Remove the schematic terminal with the given name.
     * Throw an exception if the terminal does not exist
     * in this schematic.
     */
    public void removeTerminal(SchematicTerminal terminal) {
        _terminals.remove(terminal);
	//	terminal.setContainer(null);
    }

    /**
     * Return a string representing this Schematic.
     */
    public String toString() {
        Enumeration entities = entities();
        String str = getName() + "({";
        while(entities.hasMoreElements()) {
            SchematicEntity entity = (SchematicEntity) entities.nextElement();
            str += "\n..." + entity.toString();
        }
        str += "}{";
	Enumeration enumrelations = relations();
        while(enumrelations.hasMoreElements()) {
	    SchematicRelation relation = 
		(SchematicRelation) enumrelations.nextElement();
	    str += "\n..." + relation.toString();
	}
        str += "}{";
	Enumeration enumports = ports();
        while(enumports.hasMoreElements()) {
	    SchematicPort port =
		(SchematicPort) enumports.nextElement();
	    str += "\n..." + port.toString();
	}
        str += "}{";
	Enumeration enumterminals = terminals();
        while(enumterminals.hasMoreElements()) {
	    SchematicTerminal terminal = 
		(SchematicTerminal) enumterminals.nextElement();
	    str += "\n..." + terminal.toString();
	}
	str += "})";
	return str;
    }

    //diva.graph.model.Graph
    /**
     * Return true if this graph contains the given node.
     */
    public boolean contains(Node n) {
        if(n instanceof SchematicEntity) 
            return containsEntity((SchematicEntity) n);
        else if(n instanceof SchematicTerminal) 
            return containsTerminal((SchematicTerminal) n);
        else return false;

    }

    /**
     * Return the number of nodes contained in
     * this graph.
     */
    public int getNodeCount() {
        return _entities.size() + _terminals.size(); 
    }

    /* Get the semantic object of this node. Generally this
     * is used when this node is a "wrapper" for some other object
     * or model with deeper meaning.
     */
    public Object getSemanticObject() {
	return _semanticObject;
    }
    
    /**
     * Provide an iterator over the nodes in this
     * graph.  This iterator does not support removal
     * operations.
     */
    public Iterator nodes() {
        return new EnumerationIterator(entities());
    }

    /**  Set the semantic object of this node. Generally this
     * is used when this node is a "wrapper" for some other object
     * or model with deeper meaning.
     */
    public void setSemanticObject(Object o) {
	_semanticObject = o;
    }


    /**
     * The underlying semantic object.
     */
    private Object _semanticObject = null;

    private NamedList _entities;
    private NamedList _ports;
    private NamedList _terminals;
    private NamedList _relations;
}

