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

An Entity is created within a Workspace.  If the workspace is
not specified as a constructor argument, then the default workspace
is used.  Almost all actions are synchronized on the workspace,
so using only the default workspace will have the effect of
serializing all method calls.

@author John S. Davis II, Edward A. Lee
@version @(#)Entity.java	1.15 01/20/98
@see Port
@see Relation
*/
public class Entity extends NamedObj {

    /** Construct an entity in the default workspace with an empty string
     *  as its name.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public Entity() {
	super();
        _portList = new NamedList(this);
    }

    /** Construct an entity in the default workspace with the given name.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public Entity(String name) {
	super(name);
        _portList = new NamedList(this);
    }

    /** Construct an entity in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version of the workspace.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this object.
     */
    public Entity(Workspace workspace, String name) {
	super(workspace, name);
        _portList = new NamedList(this);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Append a port to the list of ports belonging to this entity.
     *  If the port has a container (other than this), remove it from
     *  the port list of that container.  Set the container of the port
     *  to point to this entity.  If the port and this entity are not
     *  in the same workspace, throw an exception.  If the port argument
     *  is null, do nothing.
     *  This method is sychronized on the
     *  workspace and increments its version number.
     *  @param port
     *  @exception IllegalActionException Port is not of the expected class
     *   (thrown in derived classes only), or port has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the port list.
     */	
    public void addPort(Port port) 
            throws IllegalActionException, NameDuplicationException {
        if (port == null) return;
        if (workspace() != port.workspace()) {
            throw new IllegalActionException(this, port,
                    "Cannot add port because workspaces are different.");
        }
        synchronized(workspace()) {
            Entity prevcontainer = (Entity) port.getContainer();
            // Do this first, because it may throw an exception.
            _addPort(port);
            if (prevcontainer != null) {
                prevcontainer._removePort(port);
            } else {
                workspace().remove(port);
            }
            port._setContainer(this);
        }
    }

    /** Enumerate all connected ports.
     *  Ports in this entity is not included unless there is a loopback, 
     *  meaning that two distinct ports of this entity are linked to the same
     *  relation.  This method is sychronized on the workspace.  The connected
     *  entities can be obtained from the ports using getContainer().
     *  @return An enumeration of Port objects.
     */	
    public Enumeration getConnectedPorts() {
        synchronized(workspace()) {
            // This works by constructing a linked list and then enumerating it.
            LinkedList storedEntities = new LinkedList();
            Enumeration ports = _portList.getElements(); 

            while( ports.hasMoreElements() ) {
                Port port = (Port)ports.nextElement();
                storedEntities.appendElements( port.getConnectedPorts() );
            }
            return storedEntities.elements();
        }
    }

    /** Enumerate linked relations. 
     *  This method is sychronized on the workspace.
     *  @return An enumeration of Relation objects.
     */	
    public Enumeration getLinkedRelations() {
        synchronized(workspace()) {
            LinkedList storedRelations = new LinkedList();
            Enumeration ports = _portList.getElements(); 

            while( ports.hasMoreElements() ) {
                Port port = (Port)ports.nextElement(); 
                Enumeration relations = port.getLinkedRelations(); 
                storedRelations.appendElements( relations );
            }
            return storedRelations.elements();
        }
    }

    /** Return the port belonging to this entity that has the specified name.
     *  This method is sychronized on the workspace.
     *  @return A port with the given name, or null if none exists.
     */	
    public Port getPort(String name) {
        synchronized(workspace()) {
            return (Port)_portList.get(name);
        }
    }

    /** Enumerate the ports belonging to this entity, in the order in which
     *  they were created by newPort() or added by addPort().
     *  This method is sychronized on the workspace.
     *  @return An enumeration of Port objects.
     */	
    public Enumeration getPorts() {
        synchronized(workspace()) {
            return _portList.getElements();
        }
    }

    /** Create a new port with the specified name.
     *  The container of the port is set to this entity.
     *  This method is sychronized on the workspace, and increments
     *  its version number.
     *  @param name
     *  @return The new port
     *  @exception IllegalActionException Argument is null.
     *  @exception NameDuplicationException Entity already has a port with
     *  that name.
     */	
    public Port newPort(String name) 
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            Port port = new Port(this, name);
            workspace().incrVersion();
            return port;
        }
    }

    /** Remove all ports
     *  As a side effect, the ports will be unlinked from all relations.
     *  This method is sychronized on the workspace, and increments
     *  its version number.
     *  @exception InvalidStateException Inconsistent port-container
     *   relationship.
     */	
    public void removeAllPorts()
            throws InvalidStateException {
        synchronized(workspace()) {
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
            workspace().incrVersion();
        }
    }

    /** Remove a port from the list of ports belonging to this entity.
     *  If the port does not belong to this entity or is null, throw
     *  an exception.
     *  As a side effect, the port will be unlinked from all relations.
     *  This method is sychronized on the workspace, and increments
     *  its version number.
     *  @param port Port to remove.
     *  @exception IllegalActionException Port does not belong to me,
     *   or the argument is null.
     *  @exception InvalidStateException Inconsistent port-container
     *   relationship.
     */	
    public void removePort(Port port)
            throws IllegalActionException, InvalidStateException {
        synchronized(workspace()) {
            if (port == null) {
                throw new IllegalActionException(this,
                        "Attempt to remove null port.");
            }
            if (!_portList.includes(port)) {
                throw new IllegalActionException(this, port,
                        "Attempt to remove a port from an entity that "
                        + "does not contain it.");
            }
            Entity portcontainer = (Entity) port.getContainer();
            if (portcontainer != this) {
                throw new InvalidStateException(this, port,
                        "Inconsistent container relationship!");
            }
            port.unlinkAll();
            _removePort(port);
            port._setContainer(null);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Add a port to this entity with minimal error checking.
     *  Unlike the corresponding public method, this method does not set
     *  the container of the port to point to this entity.
     *  This method is sychronized on the workspace.
     *  It assumes the port is in the workspace as this entity, but does
     *  not check.  The caller should check.
     *  @param port Port to contain.
     *  @exception IllegalActionException Argument has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the entity port list.
     */	
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            _portList.append(port);
        }
    }

    /** Remove the specified port with minimal error checking.
     *  The port is assumed to be contained by this composite (otherwise,
     *  nothing happens). Unlike the corresponding public method, this
     *  method does not alter the container of the port.
     *  This method is sychronized on the workspace.
     *  @param port Port to remove
     */	
    protected void _removePort(Port port) {
        synchronized(workspace()) {
            _portList.remove(port);
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         private variables                       ////

    // A list of Ports owned by this Entity.
    private NamedList _portList;
}
