/* An Entity is an aggregation of ports.

 Copyright (c) 1997 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package pt.kernel;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Entity
/** 
An Entity is an aggregation of ports.
It is meant to represent a vertex in a slight generalization of
non-hierarchical graphs, where the arcs connecting vertices are
grouped.  The role of Ports is to organize such aggregates.
Derived classes support hierarchy by defining entities that
aggregate other entities.

@author John S. Davis II, Edward A. Lee
@version $Id$
@see Port
@see Relation
*/
public class Entity extends NamedObj {

    /** Create an entity with no ports and no name. */	
    public Entity() {
	super();
        _portList = new NamedList(this);
    }

    /** Create an entity with the specified name.
     *  @param name
     *  @exception IllegalActionException Argument is null.
     */	
    public Entity(String name) 
            throws IllegalActionException {
	super(name);
        _portList = new NamedList(this);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Append a port to the list of ports belonging to this entity.
     *  If the port has a container (other than this), remove it from
     *  the port list of that container.  Set the container of the port
     *  to point to this entity.
     *  @param port
     *  @exception IllegalActionException Port is not of the expected class
     *   (thrown in derived classes only).
     *  @exception InvalidStateException Inconsistent port-container
     *   relationship, or the port has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *  on the port list.
     */	
    public void addPort(Port port) 
            throws IllegalActionException, InvalidStateException,
            NameDuplicationException {
        // NOTE: This code is fairly tricky, and is designed to ensure
        // consistency.  It works in concert with Port.setContainer()
        // and with removePort(), so do not modify it without considering
        // those.
        Entity prevcontainer = (Entity) port.getContainer();
        if (prevcontainer == this) {
            // If the port is not on the port list, then this is a half
            // constructed link, and we should complete it and return.
            // Otherwise, we should throw a name duplication exception
            // for an attempt to put the same port on the list twice.
            try {
                _portList.append(port);
            } catch (IllegalActionException ex) {
                throw new InvalidStateException(port, "Has no name!");
            }
            return;
        }
        port.setContainer(this);
    }

    /** Enumerate all connected entities.
     *  This entity is not included unless there is a loopback, meaning
     *  that two distinct ports are linked to the same relation.
     *  @return An enumeration of Entity objects.
     */	
    public Enumeration getConnectedEntities() {
        // This works by constructing a linked list and then enumerating it.
        LinkedList storedEntities = new LinkedList();
        Enumeration ports = _portList.getElements(); 

        while( ports.hasMoreElements() ) {
            Port port = (Port)ports.nextElement();
            Enumeration entities = getConnectedEntities(port);
            storedEntities.appendElements( entities );
        }
        return storedEntities.elements();
    }

    /** Enumerate entities connected to the specified Port.
     *  The port is normally contained by this entity, but this is not
     *  enforced.
     *  This entity is not included unless there is a loopback, meaning
     *  that it is linked through another port to a relation linked to
     *  the specified port.
     *  @param port
     *  @return An enumeration of Entity objects.
     */	
    public Enumeration getConnectedEntities(Port port) {
        // This works by constructing a linked list and then enumerating it.
        LinkedList storedEntities = new LinkedList();
        Enumeration relations = port.getLinkedRelations(); 
	 
        while( relations.hasMoreElements() ) { 
            Relation relation = (Relation)relations.nextElement(); 
            Enumeration otherPorts = relation.getLinkedPortsExcept( port );

            while( otherPorts.hasMoreElements() ) {
                Port otherPort = (Port)otherPorts.nextElement(); 
                storedEntities.insertLast( otherPort.getContainer() );
            }
        }
        return storedEntities.elements();
    }

    /** Enumerate entities connected to the port with the given name.
     *  This entity is not included unless there is a loopback, meaning
     *  that it is linked through another port to a relation linked to
     *  the specified port.
     *  @param portname
     *  @return An enumeration of Entity objects.
     *  @exception NoSuchItemException No such port.
     */	
    public Enumeration getConnectedEntities(String portname)
            throws NoSuchItemException {
        Port port = (Port)_portList.get(portname);
        if (port == null) {
            throw new NoSuchItemException(this,
            "Attempt to get the connected entities of a non-existent port: " 
            + portname);
        }
        return getConnectedEntities(port);
    }

    /** Enumerate linked relations. 
     *  @return An enumeration of Relation objects.
     */	
    public Enumeration getLinkedRelations() {
	 LinkedList storedRelations = new LinkedList();
	 Enumeration ports = _portList.getElements(); 

	 while( ports.hasMoreElements() ) {
	     Port newPort = (Port)ports.nextElement(); 
	     Enumeration relations = newPort.getLinkedRelations(); 
	     storedRelations.appendElements( relations );
	 }
	 return storedRelations.elements();
    }

    /** Enumerate the relations that are linked to the specified port.
     *  The port is normally contained by this entity, but this is not
     *  enforced.
     *  @param port
     *  @return An enumeration of Relation objects
     */	
    public Enumeration getLinkedRelations(Port port) {
        return port.getLinkedRelations(); 
    }

    /** Enumerate the relations that are linked to the port with the
     *  specified name.
     *  @param portname
     *  @return An enumeration of Relation objects.
     *  @exception NoSuchItemException No such port.
     */	
    public Enumeration getLinkedRelations(String portname)
            throws NoSuchItemException {
        Port port = (Port)_portList.get(portname);
        if (port == null) {
            throw new NoSuchItemException(this,
            "Attempt to get the connected entities of a non-existent port: " 
            + portname);
        }
        return getLinkedRelations(port);
    }

    /** Return the port belonging to this entity that has the specified name.
     *  @return A port with the given name, or null if none exists.
     */	
    public Port getPort(String name) {
	 return (Port)_portList.get(name);
    }

    /** Enumerate the ports belonging to this entity, in the order in which
     *  they were created by newPort() or added by addPort().
     *  @return An enumeration of Port objects.
     */	
    public Enumeration getPorts() {
	 return _portList.getElements();
    }

    /** Create a new port with the specified name.
     *  The container of the port is set to this entity.
     *  @param name
     *  @return The new port
     *  @exception IllegalActionException Argument is null.
     *  @exception NameDuplicationException Entity already has a port with
     *  that name.
     */	
    public Port newPort(String name) 
             throws IllegalActionException, NameDuplicationException {
        Port port = new Port(this, name);
        return port;
    }

    /** Remove all ports
     *  @exception InvalidStateException Inconsistent port-container
     *   relationship.
     */	
    public void removeAllPorts()
            throws InvalidStateException {
        // Have to copy _portList to avoid corrupting the enumeration.
        NamedList portListCopy = new NamedList(_portList);
        Enumeration ports = portListCopy.getElements();

        while (ports.hasMoreElements()) {
            Port port = (Port)ports.nextElement();
            try {
                removePort(port);
            } catch (IllegalActionException ex) {
                throw new InvalidStateException(this, port,
                       "Inconsistent container relationship!");
            }
        }
    }

    /** Remove a port from the list of ports belonging to this entity.
     *  If the port does not belong to this entity, trigger an exception.
     *  As a side effect, the port will be unlinked from all relations.
     *  @param port
     *  @exception IllegalActionException Port does not belong to me.
     *  @exception InvalidStateException Inconsistent port-container
     *   relationship.
     */	
    public void removePort(Port port)
            throws IllegalActionException, InvalidStateException {
        // NOTE: This code is fairly tricky, and is designed to ensure
        // consistency.  It works in concert with Port.setContainer()
        // and with addPort(), so do not modify it without considering
        // those.
        // NOTE: The port is not unlinked from all relations if the
        // port container is already null.  This condition is part of
        // the specialized interaction with Port.setContainer().
        Entity portcontainer = (Entity) port.getContainer();
        if (portcontainer == null) {
            // If we are half-way through a remove, then the port is still
            // on the port list.  Otherwise, this is an error.
            if (_portList.includes(port)) {
                _portList.remove(port);
                return;
            } else {
                throw new IllegalActionException(this, port,
                        "Attempt to remove a port with no container.");
            }
        }
        if (portcontainer != this) {
            if (_portList.includes(port)) {
                // Changing owners, initiated by Port.setContainer()
                _portList.remove(port);
               return;                
            } else {
                throw new IllegalActionException(this, port,
                        "Attempt to remove a port from an entity that "
                        + "does not contain it.");
            }
        }
        // Ignore exception since it can't occur.
        try {
            port.setContainer(null);
        } catch (NameDuplicationException ex) {};
    }

    /** Remove a port with the specified name from the list of ports
     *  belonging to this entity. If there is no such port, trigger
     *  an exception.
     *  @param name
     *  @exception InvalidStateException Thrown only if data is inconsistent
     *  (e.g. port and container do not agree).  Should not be thrown.
     *  @exception NoSuchItemException No such port.
     */	
    public void removePort(String name)
            throws InvalidStateException, NoSuchItemException {
        Port port = (Port)_portList.get(name);
        if (port == null) {
            throw new NoSuchItemException(this,
                    "Attempt to remove a nonexistent port: " + name);
        }
        // In case there are outstanding references to the port.
        // The following exception should never trigger, since the port
        // belongs to me.
        try {
            removePort(port);
        } catch (IllegalActionException ex) {
            // Disaster has struck.  We have an inconsistent data structure.
            throw new InvalidStateException(this, port,
                    "Inconsistent containment relationship!");
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // A list of Ports owned by this Entity.
    private NamedList _portList;
}
