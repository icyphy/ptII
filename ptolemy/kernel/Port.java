/* A Port is an aggregation of links to relations.

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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Port
/**
A Port is the interface of an Entity to any number of Relations.
Normally, a Port is contained by an Entity, although a port
may exist with no container.  The role of a port is to aggregate
a set of links to relations.  Thus, for example, to represent
a directed graph, entities can be created with two ports, one for
incoming arcs and one for outgoing arcs.  More generally, the arcs
to an entity may be divided into any number of subsets, with one port
representing each subset.
<p>
A Port can link to any instance of Relation.  Derived classes may
wish to constrain links to a subclass of Relation.  To do this,
subclasses should override the protected method _link() to throw
an exception if its argument is a relation that is not of the appropriate
subclass.  Similarly, if a subclass wishes to constrain the containers
of the port to be of a subclass of Entity, they should override
setContainer().

@author Mudit Goel, Edward A. Lee, Jie Liu
@version $Id$
@see Entity
@see Relation
*/
public class Port extends NamedObj {

    /** Construct a port in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public Port() {
	super();
        setMoMLElementName("port");
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {
            // Should not be thrown because "this" cannot be null.
            throw new InternalErrorException(
                    "Internal error in Port constructor!"
                    + ex.getMessage());
        }
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public Port(Workspace workspace) {
	super(workspace);
        setMoMLElementName("port");
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {
            // Should not be thrown because "this" cannot be null.
            throw new InternalErrorException(
                    "Internal error in Port constructor!"
                    + ex.getMessage());
        }
    }

    /** Construct a port with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the workspace directory,
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the Port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public Port(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name);
        setMoMLElementName("port");
        setContainer(container);
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {
            // Should not be thrown because "this" cannot be null.
            throw new InternalErrorException(
                    "Internal error in Port constructor!"
                    + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return A new Port.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Port newobj = (Port)super.clone(ws);
        try {
            newobj._relationsList = new CrossRefList(newobj);
        } catch (IllegalActionException ex) {
            // This exception should not occur because newobj is not null.
            throw new InternalErrorException(
                    "Internal error in Port clone() method!"
                    + ex.getMessage());
        }
        newobj._container = null;
        return newobj;
    }

    /** List the connected ports.  Note that a port may be listed
     *  more than once if more than one connection to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Port objects.
     */
    public List connectedPortList() {
        try {
            _workspace.getReadAccess();
            LinkedList result = new LinkedList();
            Iterator relations = linkedRelationList().iterator();
            while (relations.hasNext()) {
                Relation relation = (Relation)relations.next();
                result.addAll(relation.linkedPortList(this));
            }
            return Collections.unmodifiableList(result);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the connected ports.  Note that a port may be listed
     *  more than once if more than one connection to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use connectedPortList() instead.
     *  @return An enumeration of Port objects.
     */
    public Enumeration connectedPorts() {
        return Collections.enumeration(connectedPortList());
    }

    /** Return true, since a simple port is always opaque.
     *  @return True.
     */
    public boolean isOpaque() {
        return true;
    }

    /** Get the container entity.
     *  @return An instance of Entity.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return true if the given Relation is linked to this port.
     *  This method is read-synchronized on the workspace.
     *  @return True if the given relation is linked to this port.
     */
    public boolean isLinked(Relation r) {
        try {
            _workspace.getReadAccess();
            return _relationsList.isLinked(r);
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the linked relations.  Note that a relation may appear
     *  more than once if more than one link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return A list of Relation objects.
     */
    public List linkedRelationList() {
        try {
            _workspace.getReadAccess();
            // Unfortunately, CrossRefList returns an enumeration only.
            // Use it to construct a list.
            LinkedList result = new LinkedList();
            Enumeration relations = _relationsList.getContainers();
            while (relations.hasMoreElements()) {
                result.add(relations.nextElement());
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the linked relations.  Note that a relation may appear
     *  more than once if more than one link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of Relation objects.
     */
    public Enumeration linkedRelations() {
        // NOTE: There is no reason to deprecate this because it does
        // depend on Doug Lea's collections, and it is more efficient than
        // the list version.
        try {
            _workspace.getReadAccess();
            return _relationsList.getContainers();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Link this port with a relation.  The relation is required to be
     *  at the same level of the hierarchy as the entity that contains
     *  this port, meaning that the container of the relation
     *  is the same as the container of the container of the port.
     *  If the argument is null, do nothing.  Note that a port may
     *  be linked to the same relation more than once, in which case
     *  the link will be reported more than once by the linkedRelations()
     *  method. In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation (this is checked
     *  by the _link() protected method).
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param relation The relation to link to this port.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or the port has no container, or the port is not in the
     *   same workspace as the relation.
     */
    public void link(Relation relation)
            throws IllegalActionException {
        if (relation == null) return;
        if (_workspace != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            Nameable container = getContainer();
            if (container != null) {
                if (container.getContainer() != relation.getContainer()) {
                    throw new IllegalActionException(this, relation,
                            "Link crosses levels of the hierarchy");
                }
            }
            _link(relation);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return the number of links to relations.
     *  This method is read-synchronized on the workspace.
     *  @return The number of links, a non-negative integer.
     */
    public int numLinks() {
        try {
            _workspace.getReadAccess();
            return _relationsList.size();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Specify the container entity, adding the port to the list of ports
     *  in the container.  If the container already contains
     *  a port with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this port, throw an exception.
     *  If the port is already contained by the entity, do nothing.
     *  If the port already has a container, remove
     *  this port from its port list first.  Otherwise, remove it from
     *  the workspace directory, if it is present.
     *  If the argument is null, then
     *  unlink the port from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this port being garbage collected.
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param entity The container.
     *  @exception IllegalActionException If this port is not of the
     *   expected class for the container, or it has no name,
     *   or the port and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        if (entity != null && _workspace != entity.workspace()) {
            throw new IllegalActionException(this, entity,
                    "Cannot set container because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            Entity prevcontainer = (Entity)getContainer();
            if (prevcontainer == entity) return;
            // Do this first, because it may throw an exception.
            if (entity != null) {
                entity._addPort(this);
                if (prevcontainer == null) {
                    _workspace.remove(this);
                }
            }
            _container = entity;
            if (prevcontainer != null) {
                prevcontainer._removePort(this);
            }
            if (entity == null) {
                unlinkAll();
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the name of the port. If there is already an port
     *  of the container entity with the same name, then throw an
     *  exception.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there is already a port
     *   with the same name in the container.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        if (name == null) {
            name = new String("");
        }
        Entity container = (Entity) getContainer();
        if((container != null)){
            Port another = container.getPort(name);
            if((another != null) && (another != this)) {
                throw new NameDuplicationException(container,
                        "already contains a port with the name "+name+".");
            }
        }
        super.setName(name);
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing. If the relation is linked
     *  more than once, then unlink the first link only.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        try {
            _workspace.getWriteAccess();
            _relationsList.unlink(relation);
        } finally {
            _workspace.doneWriting();
        }
        Entity container = (Entity)getContainer();
        if (container != null) {
            container.connectionsChanged(this);
        }
    }

    /** Unlink all relations.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     */
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();
            _relationsList.unlinkAll();
            _workspace.incrVersion();
        } finally {
            _workspace.doneWriting();
        }
        Entity container = (Entity)getContainer();
        if (container != null) {
            container.connectionsChanged(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
            if ((detail & LINKS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                // To avoid infinite loop, turn off the LINKS flag
                // when querying the Ports.
                detail &= ~LINKS;
                result += "links {\n";
                Enumeration enum = linkedRelations();
                while (enum.hasMoreElements()) {
                    Relation rel = (Relation)enum.nextElement();
                    result += rel._description(detail, indent+1, 2) + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Link this port with a relation. This method should not be used
     *  directly.  Use the public version instead.
     *  If the argument is null, do nothing.
     *  If this port has no container, throw an exception.
     *  The container is notified by calling connectionsChanged().
     *  Derived classes may constrain the argument to be a subclass of
     *  Relation. Level-crossing links are allowed.
     *  This port and the relation are assumed to be in the same workspace,
     *  but this not checked here.  The caller should check.
     *  This method <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container.
     */
    protected void _link(Relation relation)
            throws IllegalActionException {
        if (relation != null) {
            Entity container = (Entity)getContainer();
            if (container == null) {
                throw new IllegalActionException(this, relation,
                        "Port must have a container to establish a link.");
            }
            // Throw an exception if this port is not of an acceptable
            // class for the relation.
            relation._checkPort(this);
            _relationsList.link( relation._getPortList() );
            container.connectionsChanged(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The list of relations for this port. */
    private CrossRefList _relationsList;

    /** @serial The entity that contains this port. */
    private Entity _container;
}
