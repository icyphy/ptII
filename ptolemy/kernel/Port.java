/* A Port is the interface of an Entity to any number of Relations.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
Normally, a Port is contained (owned) by an Entity, although a port
may exist with no container.  The role of a port is to aggregate
some set of links to relations.  Thus, for example, to represent
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
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace,
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the Port.
     *  @exception NameDuplicationException Name coincides with
     *   an element already on the port list of the container.
     */	
    public Port(Entity container, String name) 
            throws NameDuplicationException {
        super(container.workspace(), name);
        try {
            container._addPort(this);
        } catch (IllegalActionException ex) {
            // Ignore -- always has a name.
        }
        // "super" call above puts this on the workspace list.
        workspace().remove(this);
        _setContainer(container);
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
    public Enumeration connectedPorts() {
        synchronized(workspace()) {
            LinkedList result = new LinkedList();
            Enumeration relations = linkedRelations();
            while (relations.hasMoreElements()) {
                Relation relation = (Relation)relations.nextElement();
                result.appendElements(relation.linkedPortsExcept(this));
            }
            return result.elements();
        }
    }

    /** Return a description of the object
     *  @param verbosity The level of verbosity.
     */
    public String description(int verbosity){
        String results = new String();
        switch (verbosity) {
        case pt.kernel.Nameable.CONTENTS:
            results = results.concat(toString() + "\n");
            return results;
        case pt.kernel.Nameable.CONNECTIONS:
            Enumeration enum = linkedRelations();
            while (enum.hasMoreElements()) {
                Relation relation = (Relation)enum.nextElement();
                results = results.concat(relation.description(verbosity));
            }
            return results;
        case pt.kernel.Nameable.PRETTYPRINT:
            return description(CONTENTS) + description(CONNECTIONS);
        case pt.kernel.Nameable.LIST_PRETTYPRINT:
            return description(LIST_CONTENTS) + description(LIST_CONNECTIONS);
        case pt.kernel.Nameable.LIST_CONTENTS:
        case pt.kernel.Nameable.LIST_CONNECTIONS:
        case pt.kernel.Nameable.QUIET:
        default:
            return toString();
        }
    }    

    /** Get the container entity.
     *  This method is synchronized on the workspace.
     *  @return An instance of Entity.
     */
    public Nameable getContainer() {
        synchronized(workspace()) {
            return _container;
        }
    }

    /** Return true if the given Relation is linked to this port.
     */
    public boolean isLinked(Relation r) {
        return _relationsList.isLinked(r);
    }
    
    /** Enumerate the linked relations.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of Relation objects. 
     */
    public Enumeration linkedRelations() {
        synchronized(workspace()) {
            return _relationsList.getLinks();
        }
    }

    /** Link this port with a relation of the same level.
     *  If the argument is null, do nothing.
     *  The relation and the entity that contains this
     *  port are required to be at the same level of the hierarchy.
     *  I.e., if the container of this port has a container,
     *  then that container is required to also contain the relation.
     *  In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param relation
     *  @exception IllegalActionException Link crosses levels of
     *   the hierarchy, or link with an incompatible relation,
     *   or the port has no container, or the port is not in the
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
            Nameable container = getContainer();
            if (container != null) {
                if (container.getContainer() != relation.getContainer()) {
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

    /** Specify the container entity, adding the port to the list of ports
     *  in the container.  If the container already contains
     *  a port with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this port, throw an exception.
     *  If the port already has a container, remove
     *  this port from its port list first.  Otherwise, remove it from
     *  the list of objects in the workspace. If the argument is null, then
     *  unlink the port from any relations, remove it from its container,
     *  and add it to the list of objects in the workspace.
     *  If the port is already contained by the entity, do nothing.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     *  @exception IllegalActionException Port is not of the expected class
     *   for the container, or it has no name, or the port and container are
     *   not in the same workspace.
     *  @exception NameDuplicationException Duplicate name in the container.
     */
    public void setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        if (entity != null && workspace() != entity.workspace()) {
            throw new IllegalActionException(this, entity,
                    "Cannot set container because workspaces are different.");
        }
        synchronized(workspace()) {
            Entity prevcontainer = (Entity)getContainer();
            if (prevcontainer == entity) return;
            // Do this first, because it may throw an exception.
            if (entity != null) {
                entity._addPort(this);
                if (prevcontainer == null) {
                    workspace().remove(this);
                }
            }
            _setContainer(entity);
            if (entity == null) {
                // Ignore exceptions, which mean the object is already
                // on the workspace list.
                try {
                    workspace().add(this);
                } catch (IllegalActionException ex) {}
            }

            if (prevcontainer != null) {
                prevcontainer._removePort(this);
            }
            if (entity == null) {
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
     *  If this port has no container, throw an exception.
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
                if (getContainer() == null) {
                    throw new IllegalActionException(this, relation,
                            "Port must have a container to establish a link.");
                }
                _relationsList.link( relation._getPortList(this) );
            }
            workspace().incrVersion();
        }
    }

    /** Set the container without any effort to maintain consistency
     *  (i.e. nothing is done to ensure that the container includes the
     *  port in its list of ports, nor that the port is removed from
     *  the port list of the previous container.
     *  If the previous container is null and the
     *  new one non-null, remove the port from the list of objects in the
     *  workspace.  If the new container is null, then add the port to
     *  the list of objects in the workspace.
     *  This method is synchronized on the
     *  workspace, and increments its version number.
     *  It assumes the workspace of the container is the same as that
     *  of this port, but this is not checked.  The caller should check.
     *  Use the public version to to ensure consistency.
     *  @param container The new container.
     */	
    protected void _setContainer(Entity container) {
        synchronized(workspace()) {
            _container = container;
            workspace().incrVersion();
        }
    }

    ///////////////////////////////////////////////////////////////////////
    ////                         private variables                     ////

    // The list of relations for this port.
    // This member is protected to allow access from derived classes only.
    private CrossRefList _relationsList;

    // The entity that contains this port.
    private Entity _container;
}
