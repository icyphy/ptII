/* An Entity is an aggregation of ports.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import ptolemy.kernel.util.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
@see ptolemy.kernel.Port
@see ptolemy.kernel.Relation
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
     *  @exception IllegalActionException If the name has a period.
     */
    public Entity(String name) throws IllegalActionException {
	super(name);
        _portList = new NamedList(this);
    }

    /** Construct an entity in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     */
    public Entity(Workspace workspace) {
	super(workspace);
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
     *  @exception IllegalActionException If the name has a period.
     */
    public Entity(Workspace workspace, String name)
            throws IllegalActionException {
	super(workspace, name);
        _portList = new NamedList(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new entity with clones of the ports of the original
     *  entity.  The ports are not connected to anything.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return The new Entity.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Entity newentity = (Entity)super.clone(ws);
        newentity._portList = new NamedList(newentity);
        // Clone the ports.
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            Port newport = (Port)port.clone(ws);
            // Assume that since we are dealing with clones,
            // exceptions won't occur normally (the original was successfully
            // incorporated, so this one should be too).  If they do, throw an
            // InvalidStateException.
            try {
                newport.setContainer(newentity);
            } catch (KernelException ex) {
                ws.remove(newentity);
                throw new InvalidStateException(this,
                        "Failed to clone an Entity: " + ex.getMessage());
            }
        }
        return newentity;
    }

    /** Return a list of the ports that are connected to contained ports.
     *  Ports in this entity are not included unless there is a loopback,
     *  meaning that two distinct ports of this entity are linked to the same
     *  relation.   The connected entities can be obtained from the ports
     *  using getContainer().  Note that a port may be listed more than
     *  once if there is more than one connection to it.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Port objects.
     */
    public List connectedPortList() {
        try {
            _workspace.getReadAccess();
            // This works by constructing a linked list and returning it.
            // That list will not be corrupted by changes
            // in the topology.
            // The linked list is cached for efficiency.
            if (_workspace.getVersion() != _connectedportsversion) {
                // Cache is not valid, so update it.
                _connectedports = new LinkedList();
                Iterator ports = _portList.elementList().iterator();

                while( ports.hasNext() ) {
                    Port port = (Port)ports.next();
                    _connectedports.addAll( port.connectedPortList() );
                }
                _connectedportsversion = _workspace.getVersion();
            }
            return Collections.unmodifiableList(_connectedports);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate all ports that are connected to contained ports.
     *  Ports in this entity are not included unless there is a loopback,
     *  meaning that two distinct ports of this entity are linked to the same
     *  relation.   The connected entities can be obtained from the ports
     *  using getContainer().  Note that a port may be listed more than
     *  once if there is more than one connection to it.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use connectedPortList() instead.
     *  @return An enumeration of Port objects.
     */
    public Enumeration connectedPorts() {
        return Collections.enumeration(connectedPortList());
    }

    /** Notify this entity that the links to the specified port have
     *  been altered.  The default implementation in this base class
     *  is to do nothing, but derived classes may want to react to new
     *  connections.
     */
    public void connectionsChanged(Port port) {
    }

    /** Get the attribute with the given name. The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is (deeply) contained by a contained attribute or port.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    public Attribute getAttribute(String name) {
        try {
            _workspace.getReadAccess();
            Attribute result = super.getAttribute(name);
            if (result == null) {
                // Check ports.
                String[] subnames = _splitName(name);
                if (subnames[1] != null) {
                    Port match = getPort(subnames[0]);
                    if (match != null) {
                        result = match.getAttribute(subnames[1]);
                    }
                }
            }
            return result;
        } finally {
            _workspace.doneReading();
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
            _workspace.getReadAccess();
            return (Port)_portList.get(name);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the ports belonging to this entity.
     *  The order is the order in which they became contained by this entity.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use portList() instead.
     *  @return An enumeration of Port objects.
     */
    public Enumeration getPorts() {
        return Collections.enumeration(portList());
    }

    /** Get all relations that are linked to ports contained by this
     *  entity. Note that a relation may be listed more once.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Relation objects.
     */
    public List linkedRelationList() {
        try {
            _workspace.getReadAccess();
            // This method constructs a list and then enumerates it.
            // The list is cached for efficiency.
            if (_workspace.getVersion() != _linkedrelationsversion) {
                // Cache is not valid.  Update it.
                _linkedrelations = new LinkedList();
                Iterator ports = _portList.elementList().iterator();

                while( ports.hasNext() ) {
                    Port port = (Port)ports.next();
                    _linkedrelations.addAll( port.linkedRelationList() );
                }
                _linkedrelationsversion = _workspace.getVersion();
            }
            return Collections.unmodifiableList(_linkedrelations);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate relations that are linked to ports contained by this
     *  entity. Note that a relation may be listed more once.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use linkedRelationList() instead.
     *  @return An enumeration of Relation objects.
     */
    public Enumeration linkedRelations() {
        return Collections.enumeration(linkedRelationList());
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
            _workspace.getWriteAccess();
            Port port = new Port(this, name);
            return port;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Get the ports belonging to this entity.
     *  The order is the order in which they became contained by this entity.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Port objects.
     */
    public List portList() {
        try {
            _workspace.getReadAccess();
            return _portList.elementList();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Remove all ports by setting their container to null.
     *  As a side effect, the ports will be unlinked from all relations.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     */
    public void removeAllPorts() {
        try {
            _workspace.getWriteAccess();
            // Have to copy _portList to avoid corrupting the iterator.
            // NOTE: Could use a ListIterator here instead.
            NamedList portListCopy = new NamedList(_portList);
            Iterator ports = portListCopy.elementList().iterator();

            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                try {
                    port.setContainer(null);
                } catch (KernelException ex) {
                    // Should not be thrown.
                    throw new InternalErrorException(
                            "Internal error in Port constructor!"
                            + ex.getMessage());
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a name that is guaranteed to not be the name of
     *  any contained attribute or port.  In derived classes, this should be
     *  overridden so that the returned name is guaranteed to not conflict
     *  with any contained object.
     *  @param prefix A prefix for the name.
     *  @return A unique name.
     */
    public String uniqueName(String prefix) {
        String candidate = prefix + _uniqueNameIndex++;
        while(getAttribute(candidate) != null
                || getPort(candidate) != null) {
            candidate = prefix + _uniqueNameIndex++;
        }
        return candidate;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & CONTENTS) != 0 || (detail & LINKS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "ports {\n";
                Iterator iter = portList().iterator();
                while (iter.hasNext()) {
                    Port port = (Port)iter.next();
                    result += port._description(detail, indent+1, 2) + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class are the attributes plus the ports.  This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @throws IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            port.exportMoML(output, depth);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial A list of Ports owned by this Entity. */
    private NamedList _portList;

    // Cached list of connected ports.
    private transient LinkedList _connectedports;
    private transient long _connectedportsversion = -1;

    // @serial Cached list of linked relations.
    private transient LinkedList _linkedrelations;
    private transient long _linkedrelationsversion = -1;
}
