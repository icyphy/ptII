/* A Port is the interface of an Entity to any number of Relations.

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
//// Port
/** 
A Port is the interface of an Entity to any number of Relations.
Normally, a Port is associated with (owned by) an Entity, although a 
port may exist with no associated entity.  The role of a port is to 
aggregate some set of links to relations.  Thus, for example, to represent
a directed graph, entities can be created with two ports, one for
incoming arcs and one for outgoing arcs.  More generally, the arcs
to an entity may be divided into any number of subsets, with one port
representing each subset.

@author Mudit Goel, Edward A. Lee
@version $Id$
@see Entity
@see Relation
*/
public class Port extends NamedObj {

    /** Construct a port in the default workspace with an empty string
     *  as its name.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public Port() {
	super();
        // Ignore exception because "this" cannot be null.
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public Port(Workspace workspace) {
	super(workspace, "");
        // Ignore exception because "this" cannot be null.
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a port with the given name contained by the specified
     *  entity. The associated entity argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the entity for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace,
     *  unless the associated entity is null.
     *  Increment the version of the workspace.
     *  @param associatedEntity The owner entity.
     *  @param name The name of the Port.
     *  @exception NameDuplicationException Name coincides with
     *   an element already on the port list of the associated entity.
     */	
    public Port(Entity associatedEntity, String name) 
             throws NameDuplicationException {
        super(associatedEntity.workspace(), name);
        try {
            associatedEntity._addPort(this);
        } catch (IllegalActionException ex) {
            // Ignore -- always has a name.
        }
        // "super" call above puts this on the workspace list.
        workspace().remove(this);
        _setAssocEntity(associatedEntity);
        // Ignore exception because "this" cannot be null.
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Enumerate the connected ports.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of Port objects. 
     */
    public Enumeration getConnectedPorts() {
        synchronized(workspace()) {
            LinkedList result = new LinkedList();
            Enumeration relations = getLinkedRelations();
            while (relations.hasMoreElements()) {
                Relation relation = (Relation)relations.nextElement();
                result.appendElements(relation.getLinkedPortsExcept(this));
            }
            return result.elements();
        }
    }

    /** Get the entity associated with this Port.
     *  This method is synchronized on the workspace.
     *  @return An instance of Entity.
     */
    public Nameable getAssocEntity() {
        synchronized(workspace()) {
            return _associatedEntity;
        }
    }

    /** Return a string of the form "workspace.name1.name2...nameN" where
     *  "nameN" is the name of this object, "workspace" is the name of the
     *  workspace, and the intervening names are the names of the associated
     *  entity and its containers, if there are containers.
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, results in an exception.  Note that it is not
     *  possible to construct a recursive structure using this class alone,
     *  since there is no container.
     *  But derived classes might erroneously permit recursive structures,
     *  so this error is caught here.
     *  This method is synchronized on the workspace.
     *  @return The full name of the object.
     *  @exception InvalidStateException Container contains itself.
     */
    public String getFullName()
            throws InvalidStateException {
        synchronized (workspace()) {
            String fullname = new String(getName());
            // Use a linked list to keep track of what we've seen already.
            LinkedList visited = new LinkedList();
            visited.insertFirst(this);
            Nameable parent = getAssocEntity();

            while (parent != null) {
                if (visited.firstIndexOf(parent) >= 0) {
                    // Cannot use this pointer or we'll get stuck infinitely
                    // calling this method, since it's used to report
                    // exceptions.
                    throw new InvalidStateException(
                            "Container contains itself.");
                }
                fullname = parent.getName() + "." + fullname;
                visited.insertFirst(parent);
                parent = parent.getContainer();
            }
            return workspace().getName() + "." + fullname;
        }
    }

    /** Enumerate the linked relations.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of Relation objects. 
     */
    public Enumeration getLinkedRelations() {
        synchronized(workspace()) {
            return _relationsList.getLinks();
        }
    }

    /** Link this port with a relation of the same level.
     *  If the argument is null, do nothing.
     *  The relation and the entity that contains this
     *  port are required to be at the same level of the hierarchy.
     *  I.e., if the associated entity of this port has a container,
     *  then that container is required to also contain the relation.
     *  In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param relation
     *  @exception IllegalActionException Link crosses levels of
     *   the hierarchy, or link with an incompatible relation, or
     *   the port has no associated entity, or the port is not in the
     *   same workspace as the relation.
     */	
    public void link(Relation relation) 
            throws IllegalActionException {
        if (relation == null) return;
        if (workspace() != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        synchronized(workspace()) {
            Nameable associatedEntity = getAssocEntity();
            if (associatedEntity != null) {
                if (associatedEntity.getContainer() 
			!= relation.getContainer()) {
                    throw new IllegalActionException(this, relation,
                            "Link crosses levels of the hierarchy");
                }
            }
            _link(relation);
        }
    }

    /** Return the number of linked relations.
     *  This method is synchronized on the workspace.
     *  @return A non-negative integer
     */
    public int numLinks() {
        synchronized(workspace()) {
            return _relationsList.size();
        }
    }

    /** Specify the associated entity, adding the port to the list of ports
     *  in the container.  If the associated entity already contains
     *  a port with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the entity is not in the same
     *  workspace as this port, throw an exception.
     *  If the port already has an associated entity , remove
     *  this port from its port list first.  Otherwise, remove it from
     *  the list of objects in the workspace. If the argument is null, then
     *  unlink the port from any relations, remove it from its container,
     *  and add it to the list of objects in the workspace.
     *  If the port is already associated with the entity, do nothing.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     *  @exception IllegalActionException Port is not of the expected class
     *   for the container, or it has no name, or the port and associated
     *   entity are not in the same workspace.
     *  @exception NameDuplicationException Duplicate name in the container.
     */
    public void setAssocEntity(Entity associatedEntity)
            throws IllegalActionException, NameDuplicationException {
        if (associatedEntity != null && 
		workspace() != associatedEntity.workspace()) {
            throw new IllegalActionException(this, associatedEntity,
                    "Cannot set container because workspaces are different.");
        }
        synchronized(workspace()) {
            Entity prevAssocEntity = (Entity)getAssocEntity();
            if (prevAssocEntity == associatedEntity) return;
            // Do this first, because it may throw an exception.
            if (associatedEntity != null) {
                associatedEntity._addPort(this);
                if (prevAssocEntity == null) {
                    workspace().remove(this);
                }
            }
            _setAssocEntity(associatedEntity);
            if (associatedEntity == null) {
                // Ignore exceptions, which mean the object is already
                // on the workspace list.
                try {
                    workspace().add(this);
                } catch (IllegalActionException ex) {}
            }

            if (prevAssocEntity != null) {
                prevAssocEntity._removePort(this);
            }
            if (associatedEntity == null) {
                unlinkAll();
            }
        }
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     *  @param relation
     */
    public void unlink(Relation relation) {
        synchronized(workspace()) {
            _relationsList.unlink(relation);
            workspace().incrVersion();
        }
    } 

    /** Unlink all relations.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     */	
    public void unlinkAll() {
        synchronized(workspace()) {
            _relationsList.unlinkAll();
            workspace().incrVersion();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Do nothing if the specified relation is compatible with this port.
     *  In derived classes, this method will throw an exception if the
     *  relation is incompatible with the port.
     *  @param relation
     *  @exception IllegalActionException Incompatible relation.
     */	
    protected void _checkRelation(Relation relation) 
            throws IllegalActionException {
    }

    /** Link this port with a relation.
     *  If the argument is null, do nothing.
     *  If this port has no associated entity, throw an exception.
     *  Invoke _checkRelation, which may also throw an exception,
     *  but do no further checking.  I.e., level-crossing links are allowed.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     *  This port and the relation are assumed to be in the same workspace,
     *  but this not checked here.  The caller should check.
     *  @param relation
     *  @exception IllegalActionException Port has no container.
     */	
    protected void _link(Relation relation) 
            throws IllegalActionException {
        synchronized(workspace()) {
            if (relation != null) {
                _checkRelation(relation);
                if (getAssocEntity() == null) {
                    throw new IllegalActionException(this, relation,
                            "Port must have a container to establish a link.");
                }
                _relationsList.link( relation._getPortList(this) );
            }
            workspace().incrVersion();
        }
    }

    /** Set the associated entity without any effort to maintain consistency
     *  (i.e. nothing is done to ensure that the container includes the
     *  port in its list of ports, nor that the port is removed from
     *  the port list of the previous container.
     *  If the previous associated entity is null and the
     *  new one non-null, remove the port from the list of objects in the
     *  workspace.  If the new associated entity is null, then add the port 
     *  to the list of objects in the workspace.
     *  This method is synchronized on the
     *  workspace, and increments its version number.
     *  It assumes the workspace of the associated entity is the same as that
     *  of this port, but this is not checked.  The caller should check.
     *  Use the public version to to ensure consistency.
     *  @param container The new container.
     */	
    protected void _setAssocEntity(Entity associatedEntity) {
        synchronized(workspace()) {
            _associatedEntity = associatedEntity;
            workspace().incrVersion();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The list of relations for this port.
    // This member is protected to allow access from derived classes only.
    private CrossRefList _relationsList;

    // The entity that owns this port.
    private Entity _associatedEntity;
}
