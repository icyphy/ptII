/* An Entity is an aggregation of ports.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
*/

package pt.kernel;

import pt.kernel.util.*;

import java.util.Enumeration;
import java.lang.reflect.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Entity
/**
An Entity is a vertex in a generalized graph. It is an aggregation
of ports. The ports can be linked to relations. The
relations thus represent connections between ports, and hence,
connections between entities. To add a port to an entity, simply
set its container to the entity.  To remove it, set its container
to null, or to some other entity.
<p>
Entities are intended for flat graphs. Derived classes support
hierarchy (clustered graphs) by defining entities that aggregate
other entities.
<p>
An Entity can contain any instance of Port.  Derived classes may
wish to constrain to a subclass of Port.  To do this, subclasses
should override the public method newPort() to create a port of
the appropriate subclass, and the protected method _addPort() to throw
an exception if its argument is a port that is not of the appropriate
subclass.
<p>
An Entity is created within a workspace.  If the workspace is
not specified as a constructor argument, then the default workspace
is used. The workspace is used to synchronize simultaneous accesses
to a topology from multiple threads.  The workspace is immutable
(it cannot be changed during the lifetime of the Entity).

@author John S. Davis II, Edward A. Lee
@version $Id$
@see pt.kernel.Port
@see pt.kernel.Relation
*/
public class Entity extends NamedObj {

    /** Construct an entity in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public Entity() {
	super();
        _portList = new NamedList(this);
    }

    /** Construct an entity in the default workspace with the given name.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param name The name of this object.
     */
    public Entity(String name) {
	super(name);
        _portList = new NamedList(this);
    }

    /** Construct an entity in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     *  @param name The name of this object.
     */
    public Entity(Workspace workspace, String name) {
	super(workspace, name);
        _portList = new NamedList(this);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Clone the object into the specified workspace and list the clone
     *  in the directory of that workspace.
     *  The result is a new entity with clones of the ports of the original
     *  entity.  The ports are not connected to anything.
     *  @param ws The workspace in which to place the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur).
     *  @return The new Entity.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Entity newentity = (Entity)super.clone(ws);
        // Clone the ports.
        Enumeration ports = getPorts();
        while (ports.hasMoreElements()) {
            Port port = (Port)ports.nextElement();
            Port newport = (Port)port.clone(ws);
            // Assume that since we are dealing with clones,
            // exceptions won't occur normally (the original was successfully
            // incorporated, so this one should be too).  If they do, throw a
            // InvalidStateException.
            try {
                newport.setContainer(newentity);
            } catch (KernelException ex) {
                throw new InvalidStateException(this,
                        "Failed to clone an Entity: " + ex.getMessage());
            }
        }
        return newentity;
    }

    /** Enumerate all ports that are connected to contained ports.
     *  Ports in this entity is not included unless there is a loopback,
     *  meaning that two distinct ports of this entity are linked to the same
     *  relation.   The connected entities can be obtained from the ports
     *  using getContainer().  Note that a port may be listed more than
     *  once if there is more than one connection to it.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of Port objects.
     */
    public Enumeration connectedPorts() {
        try {
            workspace().read();
            // This works by constructing a linked list and then enumerating it.
            // While this may seem costly, it means that the returned
            // enumeration is robust.  It will not be corrupted by changes
            // in the topology.
            // The linked list is cached for efficiency.
            if (workspace().getVersion() != _connectedportsversion) {
                // Cache is not valid, so update it.
                _connectedports = new LinkedList();
                Enumeration ports = _portList.getElements();

                while( ports.hasMoreElements() ) {
                    Port port = (Port)ports.nextElement();
                    _connectedports.appendElements( port.connectedPorts() );
                }
                _connectedportsversion = workspace().getVersion();
            }
            return _connectedports.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the port contained by this entity that has the specified name.
     *  If there is no such port, return null.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired port.
     *  @return A port with the given name, or null if none exists.
     */
    public Port getPort(String name) {
        try {
            workspace().read();
            return (Port)_portList.get(name);
        } finally {
            workspace().doneReading();
        }
    }

    /** Enumerate the ports belonging to this entity.
     *  The order is the order in which they became contained by this entity.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of Port objects.
     */
    public Enumeration getPorts() {
        try {
            workspace().read();
            return _portList.getElements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Enumerate relations that are linked to ports contained by this
     *  entity. Note that a relation may be listed more once.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of Relation objects.
     */
    public Enumeration linkedRelations() {
        try {
            workspace().read();
            // This method constructs a list and then enumerates it.
            // The list is cached for efficiency.
            if (workspace().getVersion() != _linkedrelationsversion) {
                // Cache is not valid.  Update it.
                _linkedrelations = new LinkedList();
                Enumeration ports = _portList.getElements();

                while( ports.hasMoreElements() ) {
                    Port port = (Port)ports.nextElement();
                    _linkedrelations.appendElements( port.linkedRelations() );
                }
                _linkedrelationsversion = workspace().getVersion();
            }
            return _linkedrelations.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Create a new port with the specified name. Set its container
     *  to be this entity. Derived classes should override this method
     *  to create a subclass of Port, if they require subclasses of Port.
     *  If the name argument is null, then the name used is an empty string.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception IllegalActionException If the port created is not
     *   of an acceptable class (this is a programming
     *   error; failed to override this method in derived classes).
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    public Port newPort(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().write();
            Port port = new Port(this, name);
            return port;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Remove all ports by setting their container to null.
     *  As a side effect, the ports will be unlinked from all relations.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     */
    public void removeAllPorts() {
        try {
            workspace().write();
            // Have to copy _portList to avoid corrupting the enumeration.
            // NOTE: Is this still true?  Or was this a bug in in NamedList?
            NamedList portListCopy = new NamedList(_portList);
            Enumeration ports = portListCopy.getElements();

            while (ports.hasMoreElements()) {
                Port port = (Port)ports.nextElement();
                try {
                    port.setContainer(null);
                } catch (KernelException ex) {
                    // Ignore exceptions that can't occur.
                }
            }
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////////
    ////                      protected methods                        ////

    /** Add a port to this entity. This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set
     *  the container of the port to point to this entity.
     *  It assumes that the port is in the same workspace as this
     *  entity, but does not check.  The caller should check.
     *  Derived classes should override this method if they require
     *  a subclass of Port to throw an exception if the argument is
     *  not of an acceptable class.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the entity.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        _portList.append(port);
    }

    /** Clear references that are not valid in a cloned object.  The clone()
     *  method in Object makes a field-by-field copy, which results
     *  in invalid references to objects.
     *  In this class, this method reinitializes the private member
     *  _portList.
     *  @param ws The workspace the cloned object is to be placed in.
     */
    protected void _clearAndSetWorkspace(Workspace ws) {
        super._clearAndSetWorkspace(ws);
        _portList = new NamedList(this);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the Nameable interface.  Lines are indented according to
     *  to the level argument using the protected method _indent().
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent) {
        try {
            workspace().read();
            String result = super._description(detail, indent);
            if ((detail & CONTENTS) != 0 || (detail & LINKS) != 0) {
                if (result.length() > 0) {
                    result += " ";
                }
                result += "ports {\n";
                Enumeration enum = getPorts();
                while (enum.hasMoreElements()) {
                    Port port = (Port)enum.nextElement();
                    result = result +
                        port._description(detail, indent+1) + "\n";
                }
                result = result + _indent(indent) + "}";
            }
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Remove the specified port. This method should not be used
     *  directly.  Call the setContainer() method of the port instead
     *  with a null argument. The port is assumed to be contained
     *  by this entity (otherwise, nothing happens). This
     *  method does not alter the container of the port.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param port The port being removed from this entity.
     */
    protected void _removePort(Port port) {
        _portList.remove(port);
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         private variables                       ////

    // A list of Ports owned by this Entity.
    private NamedList _portList;

    // Cached list of connected ports.
    private transient LinkedList _connectedports;
    private transient long _connectedportsversion = -1;

    // Cached list of linked relations.
    private transient LinkedList _linkedrelations;
    private transient long _linkedrelationsversion = -1;
}
