/* A CompositeEntity is a non-atomic vertex in a hierarchical graph.

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

import java.util.Hashtable;
import java.io.IOException;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CompositeEntity
/** 
A CompositeEntity is a non-atomic vertex in a hierarchical graph.
It can contain other entities and relations.  It supports transparent ports,
where, in effect, the port of a contained entity is represented by a port
of the container entity.  
As with all classes that support hierarchical graphs,
methods that read the graph structure come in two versions,
shallow and deep.  The deep version flattens the hierarchy.
In particular, deepGetEntities() returns the atomic entities
directly or indirectly contained by this entity.

@author John S. Davis II, Edward A. Lee
@version $Id$
*/
public class CompositeEntity extends ComponentEntity {

    /** Construct an entity in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public CompositeEntity() {
        super();
        _containedEntities = new NamedList();
        _containedRelations = new NamedList();
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public CompositeEntity(Workspace workspace) {
	super(workspace);
        _containedEntities = new NamedList();
        _containedRelations = new NamedList();
    }

    /** Create an object with a name and a container. 
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException Name argument is null.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */	
    public CompositeEntity(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _containedEntities = new NamedList();
        _containedRelations = new NamedList();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Add an entity to this container.  Set the container of the entity to
     *  refer to this container.  If the entity is already contained by
     *  another object, remove it from the entity list of that other object.
     *  If the entity is null, do nothing and return.
     *  An exception is thrown if this object
     *  is directly or indirectly contained by the entity being added.
     *  This method is sychronized on the
     *  workspace and increments its version number.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException Recursive containment structure.
     *  @exception NameDuplicationException Name collides with a name already
     *  on the entity contents list.
     */	
    public void addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (entity == null) return;
        if (workspace() != entity.workspace()) {
            throw new IllegalActionException(this, entity,
                    "Cannot add entity because workspaces are different.");
        }
        synchronized(workspace()) {
            // Check for name conflicts first, before any state is changed.
            if (getEntity(entity.getName()) != null) {
                throw new NameDuplicationException(this, entity);
            }
            // Check for recursive containment, before any state is changed.
            if (!entity.isAtomic()) {
                if (((CompositeEntity)entity).deepContains(this)) {
                    throw new IllegalActionException(entity, this,
                            "Attempt to construct recursive containment.");
                }
            }
            CompositeEntity prevcontainer =
                (CompositeEntity)entity.getContainer();
            if (prevcontainer != null) {
                // Use protected version to not alter the links nor the
                // container of the entity.
                prevcontainer._removeEntity(entity);
            } else {
                workspace().remove(this);
            }

            entity._setContainer(this);

            // Add the entity to the contents list.
            _addEntity(entity);
        }
    }

    /** Add a relation to this container.  Set the container of the 
     *  relation to refer to this container.
     *  If the relation is null, do nothing and return.
     *  This method is sychronized on the
     *  workspace and increments its version number.
     *  @param relation Relation to contain.
     *  @exception IllegalActionException Argument has a different
     *   top container, or relation has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the contained relations list.
     */	
    public void addRelation(ComponentRelation relation)
            throws IllegalActionException, InvalidStateException,
            NameDuplicationException {
        if (relation == null) return;
        if (workspace() != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot add relation because workspaces are different.");
        }
        synchronized(workspace()) {

            // Check for name conflicts first, before any state is changed.
            if (getEntity(relation.getName()) != null) {
                throw new NameDuplicationException(this, relation);
            }
            CompositeEntity prevcontainer =
                (CompositeEntity)relation.getContainer();
            if (prevcontainer != null) {
                // Use protected version to not alter the links nor the
                // container of the relation.
                prevcontainer._removeRelation(relation);
            } else {
                workspace().remove(this);
            }

            relation._setContainer(this);

            // Add the relation to the contents list.
            _addRelation(relation);
        }
    }

    /** If the argument is true, allow the connect() method to create
     *  level-crossing connections.  Otherwise, disallow this (the
     *  default behavior).
     *  This method is synchronized on the workspace.
     *  @param boole  True to allow level-crossing connections.
     */	
    public void allowLevelCrossingConnect(boolean boole) {
        synchronized(workspace()) {
            _levelCrossingConnectAllowed = boole;
        }
    }

    /** This method is a shortcut for establishing a new connection.
     *  It creates a new ComponentRelation object with an automatically
     *  generated name and uses it to link the specified ports.
     *  The name is of the form _Ei, where i is an integer.
     *  Level-crossing connections are not permitted.
     *  A reference to the newly created alias relation is returned.
     *  It can be used to remove the connection by calling removeRelation().
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param port1
     *  @param port2
     *  @exception IllegalActionException Ports are not at the right
     *   level of the hierarchy, or one of the arguments is null.
     */	
    public ComponentRelation connect(ComponentPort port1, ComponentPort port2)
            throws IllegalActionException {
        // Catch and ignore duplicate name exception.  Not thrown.
        try {
            return connect(port1, port2, _uniqueRelationName());
        } catch (NameDuplicationException ex) {
            return null;
        }
    }

    /** This method is a shortcut for establishing a new connection.
     *  It creates a new relation using newRelation() with the specified name
     *  and uses it to link the specified ports.
     *  Level-crossing connections are not permitted.
     *  A reference to the newly created alias relation is returned.
     *  It can be used to remove the connection by calling removeRelation().
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param port1
     *  @param port2
     *  @param relationname The name of the new relation.
     *  @exception IllegalActionException Ports are not at the right
     *   level of the hierarchy, or one of the arguments is null.
     *  @exception NameDuplicationException There is already a relation with
     *   the specified name.
     */	
    public ComponentRelation connect(ComponentPort port1, ComponentPort port2,
            String relationname)
            throws IllegalActionException, NameDuplicationException {
        if (port1 == null || port2 == null) {
            throw new IllegalActionException(this,
                    "Attempt to connect null port.");
        }
        if (port1.workspace() != port2.workspace()) {
            throw new IllegalActionException(port1, port2,
                    "Cannot connect ports because workspaces are different.");
        }
        synchronized(workspace()) {
            ComponentRelation ar = newRelation(relationname);
            if (_levelCrossingConnectAllowed) {
                port1.liberalLink(ar);
            } else {
                port1.link(ar);
            }
            // Have to catch the exception to restore the original state.
            try {
                if (_levelCrossingConnectAllowed) {
                    port2.liberalLink(ar);
                } else {
                    port2.link(ar);
                }
            } catch (IllegalActionException ex) {
                port1.unlink(ar);
                throw ex;
            }
            return ar;
        }
    }

    /** Return true if this object contains the specified entity,
     *  directly or indirectly.  That is, return true if the specified
     *  entity is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     *  This method is synchronized on the workspace.
     */	
    public boolean deepContains(ComponentEntity entity) {
        synchronized(workspace()) {
            // Start with the entity and check its containers in sequence.
            if (entity != null) {
                Nameable parent = entity.getContainer();
                while (parent != null) {
                    if (parent == this) {
                        return true;
                    }
                    parent = parent.getContainer();
                }
            }
            return false;
        }
    }

    /** Enumerate the atomic entities that are directly or indirectly
     *  contained by this entity.
     *  This method is synchronized on the workspace.
     *  @return An enumaration of atomic ComponentEntity objects.
     */	
    public Enumeration deepGetEntities() {
        synchronized(workspace()) {
            Enumeration enum = _containedEntities.getElements();
            // Construct a linked list and then return an enumeration of it.
            LinkedList result = new LinkedList();
            
            while (enum.hasMoreElements()) {
                ComponentEntity entity = (ComponentEntity)enum.nextElement();
                if (entity.isAtomic()) {
                    result.insertLast(entity);
                } else {
                    result.appendElements(
                            ((CompositeEntity)entity).deepGetEntities());
                }
            }
            return result.elements();
        }
    }

    /** Get a contained entity by name.
     *  This method is synchronized on the workspace.
     *  @return An entity with the specified name, or null if none exists.
     */	
    public ComponentEntity getEntity(String name) {
        synchronized(workspace()) {
            return (ComponentEntity)_containedEntities.get(name);
        }
    }

    /** Enumerate the contained entities in the order they were added
     *  by addEntity().
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentEntity objects.
     */	
    public Enumeration getEntities() {
        synchronized(workspace()) {
            return _containedEntities.getElements();
        }
    }

    /** Get a contained relation by name.
     *  This method is synchronized on the workspace.
     *  @return A relation with the specified name, or null if none exists.
     */	
    public ComponentRelation getRelation(String name) {
        synchronized(workspace()) {
            return (ComponentRelation)_containedRelations.get(name);
        }
    }

    /** Enumerate the relations contained by this entity.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentRelation objects.
     */	
    public Enumeration getRelations() {
        synchronized(workspace()) {
            return _containedRelations.getElements();
        }
    }

    /** Return false since CompositeEntities are not atomic.
     *  Note that this will return false even if there are no contained
     *  entities or relations.
     */	
    public boolean isAtomic() {
	return false;
    }

    /** Create a new relation with the specified name, add it to the
     *  relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of ComponentRelation.
     *  @exception IllegalActionException name argument is null.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the container's contents list.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        return new ComponentRelation(this, name);
    }

    /** Return the number of contained entities.
     *  This method is synchronized on the workspace.
     */	
    public int numEntities() {
        synchronized(workspace()) {
            return _containedEntities.size();
        }
    }

    /** Return the number of contained relations.	
     *  This method is synchronized on the workspace.
     */	
    public int numRelations() {
        synchronized(workspace()) {
            return _containedRelations.size();
        }
    }

    /** Remove all contained entities and unlink them from all relations.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @exception InvalidStateException Inconsistent container relationship.
     */	
    public void removeAllEntities() 
            throws InvalidStateException {
        synchronized(workspace()) {
            // Have to copy list to avoid corrupting the enumeration.
            NamedList entitiesCopy = new NamedList(_containedEntities);
            Enumeration entities = entitiesCopy.getElements();

            while (entities.hasMoreElements()) {
                ComponentEntity entity =
                    (ComponentEntity)entities.nextElement();
                try {
                    removeEntity(entity);
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException(this, entity,
                            "Inconsistent container relationship!");
                }
            }
        }
    }

    /** Remove all contained relations and unlink them from everything.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @exception InvalidStateException Inconsistent container relationship.
     */	
    public void removeAllRelations() 
            throws InvalidStateException {
        synchronized(workspace()) {
            // Have to copy list to avoid corrupting the enumeration.
            NamedList relationsCopy = new NamedList(_containedRelations);
            Enumeration relations = relationsCopy.getElements();

            while (relations.hasMoreElements()) {
                ComponentRelation relation = 
                    (ComponentRelation)relations.nextElement();
                try {
                    removeRelation(relation);
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException(this, relation,
                            "Inconsistent container relationship!");
                }
            }
        }
    }

    /** Remove the specified entity. If the entity is not contained
     *  by this entity, trigger an exception, and do not remove the entity.
     *  As a side effect, the container of the entity is set to null
     *  and the entity is unliked from everything.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param entity
     *  @exception IllegalActionException Entity does not belong to me.
     */	
    public void removeEntity(ComponentEntity entity) 
            throws IllegalActionException {
        synchronized(workspace()) {
            CompositeEntity container = (CompositeEntity) entity.getContainer();
            if (container != this) {
                throw new IllegalActionException(this, entity,
                        "Attempt to remove an entity from a container that "
                        + "does not contain it.");
            }
            _removeEntity(entity);
            entity._setContainer(null);

            // Unlink from everything.
            Enumeration ports = entity.getPorts();
            while (ports.hasMoreElements()) {
                Port port = (Port)ports.nextElement();
                port.unlinkAll();
            }
        }
    }

    /** Remove the specified relation. If the relation is not contained
     *  by this entity, trigger an exception, and do not remove the relation.
     *  As a side effect, the container of the relation is set to null and
     *  the relation is unlinked from everything.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param relation
     *  @exception IllegalActionException Relation does not belong to me.
     */	
    public void removeRelation(ComponentRelation relation) 
            throws IllegalActionException {
        synchronized(workspace()) {
            CompositeEntity container =
                (CompositeEntity) relation.getContainer();
            if (container != this) {
                throw new IllegalActionException(this, relation,
                        "Attempt to remove a relation from a container that "
                        + "does not contain it.");
            }
            _removeRelation(relation);
            relation._setContainer(null);
            relation.unlinkAll();
        }
    }

    /** Override the base class to throw an exception if this object directly
     *  or indirectly contains the proposed container, which would
     *  result in a recursive containment structure.
     *  @param container The proposed container.
     *  @exception IllegalActionException Recursive containment structure, or
     *   this entity and container are not in the same workspace..
     *  @exception NameDuplicationException Name collides with a name already
     *   on the contents list of the container.
     */	
    public void setContainer(CompositeEntity container) 
            throws IllegalActionException, NameDuplicationException {
        if (container != null && workspace() != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
        synchronized(workspace()) {
            if (deepContains(container)) {
                throw new IllegalActionException(this, container,
                        "Attempt to construct recursive containment.");
            }
            super.setContainer(container);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Add an entity to this container with minimal error checking.
     *  Unlike the corresponding public method, this method does not set
     *  the container of the entity to point to this composite entity.
     *  This method is sychronized on the workspace.
     *  It assumes this container is in the workspace as the entity, but does
     *  not check.  The caller should check.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException Argument has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *  on the entity contents list.
     */	
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            _containedEntities.append(entity);
        }
    }

    /** Add a relation to this container with minimal error checking.
     *  Unlike the corresponding public method, this method not set
     *  the container of the relation to refer to this container.
     *  This method is sychronized on the workspace.
     *  It assumes this container is in the workspace as the relation, but does
     *  not check.  The caller should check.
     *  @param relation Relation to contain.
     *  @exception IllegalActionException Relation has no name.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the contained relations list.
     */	
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            _containedRelations.append(relation);
        }
    }

    /** Remove the specified entity with minimal error checking.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). Do not alter the container of the entity nor the
     *  links in the ports of the entity.
     *  This method is sychronized on the workspace.
     *  @param entity
     */	
    protected void _removeEntity(ComponentEntity entity) {
        synchronized(workspace()) {
            _containedEntities.remove(entity);
        }
    }

    /** Remove the specified relation with minimal error checking.
     *  The relation is assumed to be contained by this composite (otherwise,
     *  nothing happens). Do not alter the container of the relation nor its
     *  links.
     *  This method is sychronized on the workspace.
     *  @param relation
     */	
    protected void _removeRelation(ComponentRelation relation) {
        synchronized(workspace()) {
            _containedRelations.remove(relation);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /** Return a name that is not the name of any contained entity.
     *  This can be used when it is necessary to name an entity, but you
     *  do not care what the name is.
     */	
    private synchronized String _uniqueEntityName() {
        String name = new String("_E" + _entitynamecount);
        while (getEntity(name) != null) {
            _entitynamecount += 1;
            name = "_E" + _entitynamecount;
        }
        return name;
    }

    /** Return a name that is not the name of any contained relation.
     *  This can be used when it is necessary to name a relation, but you
     *  do not care what the name is.
     */	
    private synchronized String _uniqueRelationName() {
        String name = new String("_R" + _relationnamecount);
        while (getRelation(name) != null) {
            _relationnamecount += 1;
            name = "_R" + _relationnamecount;
        }
        return name;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // List of contained entities.
    private NamedList _containedEntities;

    // List of contained ports.
    private NamedList _containedRelations;

    // Count of automatic names generated for entities and relations.
    private int _entitynamecount = 0;
    private int _relationnamecount = 0;

    // Flag indicating whether level-crossing connect is permitted.
    private boolean _levelCrossingConnectAllowed = false;
}
