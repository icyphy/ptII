/* A CompositeEntity is a cluster in a clustered graph.

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

import java.util.Hashtable;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CompositeEntity
/** 
A CompositeEntity is a cluster in a clustered graph.
It can contain other entities and relations.  It supports transparent ports,
where, in effect, the port of a contained entity is represented by a port
of the container entity. Methods that "deeply" traverse the topology
see right through transparent ports.
In addition, deepGetEntities() returns the atomic entities
directly or indirectly contained by this entity.
<p>
To add an entity or relation to this composite, call its setContainer method
with this composite as an argument.  To remove it, call its setContainer
method with a null argument (or another container). The entity must be
an instance of ComponentEntity and the relation of ComponentRelation or
an exception is thrown.  Derived classes may further constrain these
to subclasses.  To do that, they should override the protected methods
_addEntity() and _addRelation() and the public member newRelation().
<p>
A CompositeEntity may be contained by another CompositeEntity.
To set that up, call the setContainer() method of the inside entity.
Derived classes may further constrain the container to be
a subclass of CompositeEntity.  To do this, they should override
setContainer() to throw an exception.  Recursive containment
structures, where an entity directly or indirectly contains itself,
are disallowed, and an exception is thrown on an attempt to set up
such a structure.
<p>
A CompositeEntity can contain instances of ComponentPort.  Normally
these ports will be transparent, although subclasses of CompositeEntity
can make them opaque by overriding the isAtomic() method to return
<i>true</i>. Derived classes may further constrain the ports to a
subclass of ComponentPort.
To do this, they should override the public method newPort() to create
a port of the appropriate subclass, and the protected method _addPort()
to throw an exception if its argument is a port that is not of the
appropriate subclass.

@author John S. Davis II, Edward A. Lee
@version $Id$
*/
public class CompositeEntity extends ComponentEntity {

    /** Construct an entity in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     */
    public CompositeEntity() {
        super();
        _containedEntities = new NamedList();
        _containedRelations = new NamedList();
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
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
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
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

    /** If the argument is true, allow the connect() method to create
     *  level-crossing connections.  Otherwise, disallow this (the
     *  default behavior). This method is synchronized on the workspace.
     *  @param boole True to allow level-crossing connections.
     */	
    public void allowLevelCrossingConnect(boolean boole) {
        synchronized(workspace()) {
            _levelCrossingConnectAllowed = boole;
        }
    }

    /** Clone the object and register the clone in the workspace.
     *  NOTE: This will not work if there are level-crossing transitions.
     *  The result is an entity with clones of the ports of the original
     *  entity, the contained entities, and the contained relations.
     *  The ports of the returned entity are not connected to anything.
     *  The connections of the relations are duplicated in the new entity,
     *  unless they cross levels, in which case an exception is thrown.
     *  The entity is registered with the workspace.
     *  @exception CloneNotSupportedException If the entity contains
     *   level crossing transitions so that its connections cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        CompositeEntity newentity = (CompositeEntity)super.clone();

        // Clone the contained relations.
        Enumeration relations = getRelations();
        while (relations.hasMoreElements()) {
            ComponentRelation relation =
                    (ComponentRelation)relations.nextElement();
            ComponentRelation newrelation = (ComponentRelation)relation.clone();
            // Assume that since we are dealing with clones,
            // exceptions won't occur normally.  If they do, throw a
            // CloneNotSupportedException.
            try {
                newrelation.setContainer(newentity);
            } catch (KernelException ex) {
                throw new CloneNotSupportedException(
                "Failed to clone a CompositeEntity: " + ex.getMessage());
            }
        }

        // Clone the contained entities.
        Enumeration entities = getEntities();
        while (entities.hasMoreElements()) {
            ComponentEntity entity= (ComponentEntity)entities.nextElement();
            ComponentEntity newsubentity = (ComponentEntity)entity.clone();
            // Assume that since we are dealing with clones,
            // exceptions won't occur normally.  If they do, throw a
            // CloneNotSupportedException.
            try {
                newsubentity.setContainer(newentity);
            } catch (KernelException ex) {
                throw new CloneNotSupportedException(
                "Failed to clone a CompositeEntity: " + ex.getMessage());
            }

            // Clone the links of the ports of the cloned entities.
            Enumeration ports = entity.getPorts();
            while (ports.hasMoreElements()) {
                ComponentPort port = (ComponentPort)ports.nextElement();
                Enumeration lrels = port.linkedRelations();
                while (lrels.hasMoreElements()) {
                    ComponentRelation rel =
                           (ComponentRelation)lrels.nextElement();
                    if (rel.getContainer() != this) {
                        throw new CloneNotSupportedException(
                        "Cannot clone a CompositeEntity with level crossing" +
                        " transitions.");
                    }
                    ComponentRelation newrel =
                           newentity.getRelation(rel.getName());
                    Port newport =
                           newsubentity.getPort(port.getName());
                    try {
                        newport.link(newrel);
                    } catch (IllegalActionException ex) {
                        throw new CloneNotSupportedException(
                        "Failed to clone a CompositeEntity: " +
                        ex.getMessage());
                    }                        
                }
            }
        }

        return newentity;
    }

    /** Create a new relation and use it to connect two ports.
     *  It creates a new relation using newRelation() with an automatically
     *  generated name and uses it to link the specified ports.
     *  The order of the ports determines the order in which the
     *  links to the relation are established, but otherwise has no
     *  importance.
     *  The name is of the form _"E<i>i</i>" where <i>i</i> is an integer.
     *  Level-crossing connections are not permitted unless
     *  allowLevelCrossingConnect() has been called with a <i>true</i>
     *  argument. A reference to the newly created alias relation is returned.
     *  To remove the relation, call its setContainer() method with a null
     *  argument. This method is synchronized on the workspace
     *  and increments its version number.
     *  @param port1 The first port to connect.
     *  @param port2 The second port to connect.
     *  @exception IllegalActionException If one of the arguments is null, or
     *   if a disallowed level-crossing connection would result.
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

    /** Create a new relation with the specified name and use it to
     *  connect two ports. Level-crossing connections are not permitted
     *  unless allowLevelCrossingConnect() has been called with a true
     *  argument. A reference to the newly created alias relation is returned.
     *  To remove the relation, call its setContainer() method with a null
     *  argument. This method is synchronized on the workspace
     *  and increments its version number.
     *  @param port1 The first port to connect.
     *  @param port2 The second port to connect.
     *  @param relationname The name of the new relation.
     *  @exception IllegalActionException If one of the arguments is null, or
     *   if a disallowed level-crossing connection would result.
     *  @exception NameDuplicationException If there is already a relation with
     *   the specified name in this entity.
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
     *  contained by this entity.  The enumeration will be empty if there
     *  are no such contained entities.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of atomic ComponentEntity objects.
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

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the Nameable interface.
     *  This method is synchronized on the workspace.
     *  @param verbosity The level of detail.
     *  @return A description of the object.
     */
    public String description(int verbosity){
        synchronized(workspace()) {
            String result = super.description(verbosity);
            if ((verbosity & CONTENTS) != 0) {
                if (result.length() > 0) {
                    result += " ";
                }
                result += "entities {";
                Enumeration enume = getEntities();
                while (enume.hasMoreElements()) {
                    ComponentEntity entity =
                            (ComponentEntity)enume.nextElement();
                    result = result + "\n" + entity.description(verbosity);
                }
                result += "\n} ";
                result += "relations {";
                Enumeration enum = getRelations();
                while (enum.hasMoreElements()) {
                    Relation relation = (Relation)enum.nextElement();
                    result = result + "\n" + relation.description(verbosity);
                }
                result += "\n} ";
            }
            return result;
        }
    }

    /** Get a contained entity by name.
     *  This method is synchronized on the workspace.
     *  @param name The name of the desired relation.
     *  @return An entity with the specified name, or null if none exists.
     */	
    public ComponentEntity getEntity(String name) {
        synchronized(workspace()) {
            return (ComponentEntity)_containedEntities.get(name);
        }
    }

    /** Enumerate the contained entities in the order they were added.
     *  The returned enumeration is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of entities.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentEntity objects.
     */	
    public Enumeration getEntities() {
        synchronized(workspace()) {
            // Copy the list so we can create a static enumeration.
            NamedList entitiesCopy = new NamedList(_containedEntities);
            Enumeration entities = entitiesCopy.getElements();
            return entities;
        }
    }

    /** Get a contained relation by name.
     *  This method is synchronized on the workspace.
     *  @param name The name of the desired relation.
     *  @return A relation with the specified name, or null if none exists.
     */	
    public ComponentRelation getRelation(String name) {
        synchronized(workspace()) {
            return (ComponentRelation)_containedRelations.get(name);
        }
    }

    /** Enumerate the relations contained by this entity.
     *  The returned enumeration is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of relations.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentRelation objects.
     */	
    public Enumeration getRelations() {
        synchronized(workspace()) {
            // Copy the list so we can create a static enumeration.
            NamedList relationsCopy = new NamedList(_containedRelations);
            Enumeration relations = relationsCopy.getElements();
            //return _containedRelations.getElements();
            return relations;
        }
    }

    /** Return false since CompositeEntities are not atomic.
     *  Note that this will return false even if there are no contained
     *  entities or relations.  Derived classes may override this
     *  to return true in order to hide the contents of the entity.
     *  @return False.
     */	
    public boolean isAtomic() {
	return false;
    }

    /** Create a new relation with the specified name, add it to the
     *  relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of ComponentRelation.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param name The name of the new relation.
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already in the container.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            ComponentRelation rel = new ComponentRelation(this, name);
            workspace().incrVersion();
            return rel;
        }
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
     */	
    public void removeAllEntities() {
        synchronized(workspace()) {
            // Have to copy list to avoid corrupting the enumeration.
            // NOTE: Is this still true?  Or was this due to a bug in
            // NamedList?
            NamedList entitiesCopy = new NamedList(_containedEntities);
            Enumeration entities = entitiesCopy.getElements();

            while (entities.hasMoreElements()) {
                ComponentEntity entity =
                    (ComponentEntity)entities.nextElement();
                try {
                    entity.setContainer(null);
                } catch (KernelException ex) {
                    // Ignore exceptions that can't occur.
                }
            }
        }
    }

    /** Remove all contained relations and unlink them from everything.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     */	
    public void removeAllRelations() {
        synchronized(workspace()) {
            // Have to copy list to avoid corrupting the enumeration.
            // NOTE: Is this still true?  Or was this due to a bug in
            // NamedList?
            NamedList relationsCopy = new NamedList(_containedRelations);
            Enumeration relations = relationsCopy.getElements();

            while (relations.hasMoreElements()) {
                ComponentRelation relation = 
                    (ComponentRelation)relations.nextElement();
                try {
                    relation.setContainer(null);
                } catch (KernelException ex) {
                    // Ignore exceptions that can't occur.
                }
            }
        }
    }

    /** Override the base class to throw an exception if this object directly
     *  or indirectly contains the proposed container, which would
     *  result in a recursive containment structure.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and the proposed container are not in the same workspace.
     *  @exception NameDuplicationException If the name collides with a name 
     *   already in the container.
     */	
    public void setContainer(CompositeEntity container) 
            throws IllegalActionException, NameDuplicationException {
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

    /** Add an entity to this container. This method should not be used
     *  directly.  Call the setContainer() method of the entity instead.
     *  This method does not set
     *  the container of the entity to point to this composite entity.
     *  It assumes that the entity is in the same workspace as this
     *  container, but does not check.  The caller should check.
     *  Derived classes may override this method to constrain the
     *  the entity to a subclass of ComponentEntity.
     *  This method is synchronized on the workspace.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException If the entity has no name, or the
     *   action would result in a recursive containment structure.
     *  @exception NameDuplicationException If the name collides with a name 
     *  already in the entity.
     */	
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            if (!entity.isAtomic()) {
                if (((CompositeEntity)entity).deepContains(this)) {
                    throw new IllegalActionException(entity, this,
                            "Attempt to construct recursive containment.");
                }
            }
            _containedEntities.append(entity);
        }
    }

    /** Add a relation to this container. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set
     *  the container of the relation to refer to this container.
     *  It is synchronized on the workspace.
     *  @param relation Relation to contain.
     *  @exception IllegalActionException If the relation has no name.
     *  @exception NameDuplicationException If the name collides with a name 
     *   already on the contained relations list.
     */	
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            _containedRelations.append(relation);
        }
    }

    /** Clear references that are not valid in a cloned object.  The clone()
     *  method makes a field-by-field copy, which results
     *  in invalid references to objects. 
     *  In this class, this method resets the private members
     *  _containedEntities and _containedRelations.
     */
    protected void _clear() {
        super._clear();
        _containedEntities = new NamedList();
        _containedRelations = new NamedList();
    }

    /** Remove the specified entity. This method should not be used
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is synchronized on the workspace.
     *  @param entity The entity to remove.
     */	
    protected void _removeEntity(ComponentEntity entity) {
        synchronized(workspace()) {
            _containedEntities.remove(entity);
        }
    }

    /** Remove the specified relation. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead with
     *  a null argument.
     *  The relation is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the relation in any way.
     *  This method is synchronized on the workspace.
     *  @param relation The relation to remove.
     */	
    protected void _removeRelation(ComponentRelation relation) {
        synchronized(workspace()) {
            _containedRelations.remove(relation);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    // Return a name that is not the name of any contained entity.
    // This can be used when it is necessary to name an entity, but you
    // do not care what the name is.
    //
    private synchronized String _uniqueEntityName() {
        String name = new String("_E" + _entitynamecount);
        while (getEntity(name) != null) {
            _entitynamecount += 1;
            name = "_E" + _entitynamecount;
        }
        return name;
    }

    // Return a name that is not the name of any contained relation.
    // This can be used when it is necessary to name a relation, but you
    // do not care what the name is.
    //
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
