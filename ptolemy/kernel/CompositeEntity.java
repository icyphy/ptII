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
It can contain other entities and relations.  It supports aliasing of ports,
where, in effect, the port of a contained entity is represented by a port
of the container entity.  The port of the container entity is called
an alias.  As with all classes that support hierarchical graphs,
methods that read the graph structure come in two versions,
shallow and deep.  The deep version flattens the hierarchy.
In particular, deepGetEntities() returns the atomic entities
directly or indirectly contained by this entity.

@author John S. Davis II, Edward A. Lee
@version $Id$
*/
public class CompositeEntity extends ComponentEntity {

    /** Create an object with no name and no container */	
    public CompositeEntity() {
         super();
         _containedEntities = new NamedList();
         _containedRelations = new NamedList();
    }

    /** Create an object with a name and no container. 
     *  @exception IllegalActionException Argument is null.
     *  @param name
     */	
    public CompositeEntity(String name)
           throws IllegalActionException {
        super(name);
         _containedEntities = new NamedList();
         _containedRelations = new NamedList();
    }

    /** Create an object with a name and a container. 
     *  @param container
     *  @param name
     *  @exception IllegalActionException name argument is null.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the container's contents list.
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
     *  An exception is thrown if this object
     *  is directly or indirectly contained by the entity being added.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException Recursive containment structure.
     *  @exception InvalidStateException Inconsistent entity-container
     *   relationship (preexisting)
     *  @exception NameDuplicationException Name collides with a name already
     *  on the entity contents list.
     */	
    public void addEntity(ComponentEntity entity)
            throws IllegalActionException, InvalidStateException,
            NameDuplicationException {
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

        CompositeEntity prevcontainer = (CompositeEntity)entity.getContainer();
        if (prevcontainer != null) {
            try {
                prevcontainer.removeEntity(entity);
            } catch (IllegalActionException ex) {
                throw new InvalidStateException(prevcontainer, entity,
                      "Inconsistent containment relationship!");
            }
        }

        entity._setContainer(this);

        // Add the entity to the contents list.
        _containedEntities.append(entity);
    }

    /** Add a relation to this container.  Set the container of the 
     *  relation to refer to this container.
     *  @param relation Relation to contain.
     *  @exception InvalidStateException Inconsistent entity-container
     *   relationship (preexisting)
     *  @exception NameDuplicationException Name collides with a name already
     *   on the contained relations list.
     */	
    public void addRelation(ComponentRelation relation)
            throws InvalidStateException, NameDuplicationException {
        // Check for name conflicts first, before any state is changed.
        if (getEntity(relation.getName()) != null) {
            throw new NameDuplicationException(this, relation);
        }
        CompositeEntity prevcontainer =
                (CompositeEntity)relation.getContainer();
        if (prevcontainer != null) {
            try {
                prevcontainer.removeRelation(relation);
            } catch (IllegalActionException ex) {
                throw new InvalidStateException(prevcontainer, relation,
                      "Inconsistent containment relationship!");
            }
        }

        relation._setContainer(this);

        // Add the entity to the contents list.
        try {
            _containedRelations.append(relation);
        } catch (IllegalActionException ex) {
            throw new InvalidStateException(relation, "Has no name!");
        }
    }

    /** This method is a shortcut for establishing a new alias relation.
     *  It creates a new AliasRelation object with an automatically generated
     *  name and uses it to link the specified ports.
     *  The name is of the form _Ri, where i is an integer.
     *  The downPort must be contained by an entity contained by this
     *  entity, and the upPort must be a port of this entity.
     *  A reference to the newly created alias relation is returned.
     *  It can be used to remove the alias by calling removeRelation().
     *  @param downport The down port.
     *  @param upport The up port, or alias port.
     *  @param relationname The name of the new relation.
     *  @exception IllegalActionException Alias relationship does not
     *   span exactly one level of the hierarchy, or one of the arguments
     *   is null.
     */	
    public AliasRelation alias(ComponentPort downport, ComponentPort upport)
            throws IllegalActionException {
        // Catch and ignore duplicate name exception.  Not thrown.
        try {
            return alias(downport, upport, _uniqueRelationName());
        } catch (NameDuplicationException ex) {
            return null;
        }
    }

    /** This method is a shortcut for establishing a new alias relation.
     *  It creates a new AliasRelation object with the specified name
     *  and uses it to link the specified ports.
     *  The downPort must be contained by an entity contained by this
     *  entity, and the upPort must be a port of this entity.
     *  A reference to the newly created alias relation is returned.
     *  It can be used to remove the alias by calling removeRelation().
     *  @param downport The down port.
     *  @param upport The up port, or alias port.
     *  @param relationname The name of the new relation.
     *  @exception IllegalActionException Alias relationship does not
     *   span exactly one level of the hierarchy, or one of the arguments
     *   is null.
     *  @exception NameDuplicationException There is already a relation with
     *   the specified name.
     */	
    public AliasRelation alias(ComponentPort downport, ComponentPort upport,
            String relationname)
            throws IllegalActionException, NameDuplicationException {
        if (downport == null || upport == null) {
            throw new IllegalActionException(downport, upport,
                   "Attempt to alias a null port.");
        }
        AliasRelation ar = new AliasRelation(this, relationname);
        downport.link(ar);
        // Have to catch the exception to restore the original state.
        try {
            ar.setUpAlias(upport);
        } catch (IllegalActionException ex) {
            downport.unlink(ar);
            throw ex;
        }
        return ar;
    }

    /** This method is a shortcut for establishing a new connection.
     *  It creates a new ComponentRelation object with an automatically
     *  generated name and uses it to link the specified ports.
     *  The name is of the form _Ei, where i is an integer.
     *  Both port arguments must be contained by an entity contained by this
     *  entity. A reference to the newly created alias relation is returned.
     *  It can be used to remove the connection by calling removeRelation().
     *  @param port1
     *  @param port2
     *  @exception IllegalActionException Ports are not at the right
     *   level of the hierarchy, or one of the arguments is null.
     */	
    public ComponentRelation connect(ComponentPort port1, ComponentPort port2)
            throws IllegalActionException {
        // Catch and ignore duplicate name exception.  Not thrown.
        try {
            return connect(port1, port2, _uniqueEntityName());
        } catch (NameDuplicationException ex) {
            return null;
        }
    }

    /** This method is a shortcut for establishing a new connection.
     *  It creates a new relation using newRelation() with the specified name
     *  and uses it to link the specified ports.
     *  Both port arguments must be contained by an entity contained by this
     *  entity. A reference to the newly created alias relation is returned.
     *  It can be used to remove the connection by calling removeRelation().
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
            throw new IllegalActionException(port1, port2,
                   "Attempt to connect a null port.");
        }
        ComponentRelation ar = newRelation(relationname);
        port1.link(ar);
        // Have to catch the exception to restore the original state.
        try {
            port2.link(ar);
        } catch (IllegalActionException ex) {
            port1.unlink(ar);
            throw ex;
        }
        return ar;
    }

    /** Return true if this object contains the specified entity,
     *  directly or indirectly.  That is, return true if the specified
     *  entity is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     */	
    public boolean deepContains(ComponentEntity entity) {
        // Start with the entity and check its containers in sequence.
        // NOTE: It might be worth cacheing this result.
        // For deep hierarchies, it is expensive.
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

    /** Enumerate the atomic entities that are directly or indirectly
     *  contained by this entity.
     *  @return An enumaration of atomic ComponentEntity objects.
     */	
    public Enumeration deepGetEntities() {
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

    /** Get a contained entity by name.
     *  @return An entity with the specified, or null if none exists.
     */	
    public ComponentEntity getEntity(String name) {
        return (ComponentEntity)_containedEntities.get(name);
    }

    /** Enumerate the contained entities in the order they were added
     *  by addEntity().
     *  @return An enumeration of ComponentEntity objects.
     */	
    public Enumeration getEntities() {
        return _containedEntities.getElements();
    }

    /** Get a contained relation by name.
     *  @return A relation with the specified, or null if none exists.
     */	
    public ComponentRelation getRelation(String name) {
        return (ComponentRelation)_containedRelations.get(name);
    }

    /** Enumerate the relations contained by this entity.
     *  @return An enumeration of ComponentRelation objects.
     */	
    public Enumeration getRelations() {
        return _containedRelations.getElements();
    }

    /** Return true since CompositeEntities are not atomic.
     *  Note that this will return true even if there are no contained
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

    /** Return the number of contained entities. */	
    public int numEntities() {
	return _containedEntities.size();
    }

    /** Return the number of contained relations. */	
    public int numRelations() {
	return _containedRelations.size();
    }

    /** Remove all contained entities.
     *  @exception InvalidStateException Inconsistent container relationship.
     */	
    public void removeAllEntities() 
            throws InvalidStateException {
        // Have to copy list to avoid corrupting the enumeration.
        NamedList entitiesCopy = new NamedList(_containedEntities);
        Enumeration entities = entitiesCopy.getElements();

        while (entities.hasMoreElements()) {
            ComponentEntity entity = (ComponentEntity)entities.nextElement();
            try {
                removeEntity(entity);
            } catch (IllegalActionException ex) {
                throw new InvalidStateException(this, entity,
                       "Inconsistent container relationship!");
            }
        }
    }

    /** Remove all contained relations.
     *  @exception InvalidStateException Inconsistent container relationship.
     */	
    public void removeAllRelations() 
            throws InvalidStateException {
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

    /** Remove the specified entity. If the entity is not contained
     *  by this entity, trigger an exception, and do not remove the entity.
     *  As a side effect, the container of the entity is set to null.
     *  @param entity
     *  @exception IllegalActionException Entity does not belong to me.
     */	
    public void removeEntity(ComponentEntity entity) 
            throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) entity.getContainer();
        if (container != this) {
            throw new IllegalActionException(this, entity,
                    "Attempt to remove an entity from a container that "
                    + "does not contain it.");
        }
        _containedEntities.remove(entity);
        entity._setContainer(null);
    }

    /** Remove the entity specified by name. If no entity contained
     *  by this entity has such a name, trigger an exception.
     *  As a side effect, the container of the entity is set to null.
     *  @param name
     *  @exception InvalidStateException Inconsistent containment relationship.
     *  @exception NoSuchItemException No such entity.
     */	
    public void removeEntity(String name) 
            throws InvalidStateException, NoSuchItemException {
        ComponentEntity entity = (ComponentEntity)_containedEntities.get(name);
        if (entity == null) {
            throw new NoSuchItemException(this,
                    "Attempt to remove a nonexistent entity: " + name);
        }
        try {
            removeEntity(entity);
        } catch (IllegalActionException ex) {
            // Disaster has struck.  We have an inconsistent data structure.
            throw new InvalidStateException(this, entity,
                    "Inconsistent containment relationship!");
        }
    }

    /** Remove the specified relation. If the relation is not contained
     *  by this entity, trigger an exception, and do not remove the relation.
     *  As a side effect, the container of the relation is set to null.
     *  @param relation
     *  @exception IllegalActionException Relation does not belong to me.
     */	
    public void removeRelation(ComponentRelation relation) 
            throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) relation.getContainer();
        if (container != this) {
            throw new IllegalActionException(this, relation,
                    "Attempt to remove a relation from a container that "
                    + "does not contain it.");
        }
        if (relation.isAlias()) {
            // Remove the up alias of the relation as well.
            ((AliasRelation)relation).setUpAlias(null);
        }
            
        _containedRelations.remove(relation);
        relation._setContainer(null);
    }

    /** Remove the relation specified by name. If no relation contained
     *  by this entity has such a name, trigger an exception, and do
     *  not remove the relation.
     *  As a side effect, the container of the relation is set to null.
     *  @param name
     *  @exception InvalidStateException Inconsistent containment relationship.
     *  @exception NoSuchItemException No such relation.
     */	
    public void removeRelation(String name) 
            throws InvalidStateException, NoSuchItemException {
        ComponentRelation relation = 
                (ComponentRelation)_containedRelations.get(name);
        if (relation == null) {
            throw new NoSuchItemException(this,
                    "Attempt to remove a nonexistent relation: " + name);
        }
        try {
            removeRelation(relation);
        } catch (IllegalActionException ex) {
            // Disaster has struck.  We have an inconsistent data structure.
            throw new InvalidStateException(this, relation,
                    "Inconsistent containment relationship!");
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /** Return a name that is not the name of any contained entity.
     *  This can be used when it is necessary to name an entity, but you
     *  do not care what the name is.
     */	
    private String _uniqueEntityName() {
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
    private String _uniqueRelationName() {
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
}
