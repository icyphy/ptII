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
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package pt.kernel;

import pt.kernel.util.*;

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
To add an entity or relation to this composite, call its setContainer() method
with this composite as an argument.  To remove it, call its setContainer()
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
     *  as its name. Add the entity to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public CompositeEntity() {
        super();
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the entity to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public CompositeEntity(Workspace workspace) {
	super(workspace);
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
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Allow or disallow connections that are created using the connect()
     *  method to cross levels of the hierarchy.
     *  The default is that such connections are disallowed.
     *  Generally it is a bad idea to allow level-crossing
     *  connections, since it breaks modularity.  This loss of modularity
     *  means, among other things, that this composite cannot be cloned.
     *  Nonetheless, this capability is provided for the benefit of users
     *  that feel they just must have it, and who are willing to sacrifice
     *  clonability and modularity.
     *  @param boole True to allow level-crossing connections.
     */
    public void allowLevelCrossingConnect(boolean boole) {
        _levelCrossingConnectAllowed = boole;
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  NOTE: This will not work if there are level-crossing transitions.
     *  The result is an entity with clones of the ports of the original
     *  entity, the contained entities, and the contained relations.
     *  The ports of the returned entity are not connected to anything.
     *  The connections of the relations are duplicated in the new entity,
     *  unless they cross levels, in which case an exception is thrown.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the entity contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new CompositeEntity.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        CompositeEntity newentity = (CompositeEntity)super.clone(ws);

        newentity._containedEntities = new NamedList(newentity);
        newentity._containedRelations = new NamedList(newentity);

        // Clone the contained relations.
        Enumeration relations = getRelations();
        while (relations.hasMoreElements()) {
            ComponentRelation relation =
                (ComponentRelation)relations.nextElement();
            ComponentRelation newrelation=(ComponentRelation)relation.clone(ws);
            // Assume that since we are dealing with clones,
            // exceptions won't occur normally.  If they do, throw a
            // CloneNotSupportedException.
            try {
                newrelation.setContainer(newentity);
            } catch (KernelException ex) {
                throw new CloneNotSupportedException(
                        "Failed to clone a CompositeEntity: " +
                        ex.getMessage());
            }
        }

        // Clone the contained entities.
        Enumeration entities = getEntities();
        while (entities.hasMoreElements()) {
            ComponentEntity entity= (ComponentEntity)entities.nextElement();
            ComponentEntity newsubentity = (ComponentEntity)entity.clone(ws);
            // Assume that since we are dealing with clones,
            // exceptions won't occur normally.  If they do, throw a
            // CloneNotSupportedException.
            try {
                newsubentity.setContainer(newentity);
            } catch (KernelException ex) {
                throw new CloneNotSupportedException(
                        "Failed to clone a CompositeEntity: " +
                        ex.getMessage());
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
                                "Cannot clone a CompositeEntity with level " +
                                "crossing transitions.");
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
     *  The name is of the form "_R<i>i</i>" where <i>i</i> is an integer.
     *  Level-crossing connections are not permitted unless
     *  allowLevelCrossingConnect() has been called with a <i>true</i>
     *  argument.  Note that is rarely a good idea to permit level crossing
     *  connections, since they break modularity and cloning.
     *  A reference to the newly created relation is returned.
     *  To remove the relation, call its setContainer() method with a null
     *  argument. This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param port1 The first port to connect.
     *  @param port2 The second port to connect.
     *  @exception IllegalActionException If one of the arguments is null, or
     *   if a disallowed level-crossing connection would result.
     */
    public ComponentRelation connect(ComponentPort port1, ComponentPort port2)
            throws IllegalActionException {
        try {
            return connect(port1, port2, _uniqueRelationName());
        } catch (NameDuplicationException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                "Internal error in ComponentRelation connect() method!"
                + ex.getMessage());
        }
    }

    /** Create a new relation with the specified name and use it to
     *  connect two ports. Level-crossing connections are not permitted
     *  unless allowLevelCrossingConnect() has been called with a true
     *  argument.   Note that is rarely a good idea to permit level crossing
     *  connections, since they break modularity and cloning.
     *  A reference to the newly created alias relation is returned.
     *  To remove the relation, call its setContainer() method with a null
     *  argument. This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param port1 The first port to connect.
     *  @param port2 The second port to connect.
     *  @param relationname The name of the new relation.
     *  @exception IllegalActionException If one of the arguments is null, or
     *   if a disallowed level-crossing connection would result, or if the two
     *   ports are not in the same workspace as this entity.
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
        if (port1.workspace() != port2.workspace() ||
                port1.workspace() != workspace()) {
            throw new IllegalActionException(port1, port2,
                    "Cannot connect ports because workspaces are different.");
        }
        try {
            workspace().getWriteAccess();
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
        } finally {
            workspace().doneWriting();
        }
    }

    /** Enumerate the atomic entities that are directly or indirectly
     *  contained by this entity.  The enumeration will be empty if there
     *  are no such contained entities.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of atomic ComponentEntity objects.
     */
    public Enumeration deepGetEntities() {
        try {
            workspace().getReadAccess();
            Enumeration enum = _containedEntities.elements();
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
        } finally {
            workspace().doneReading();
        }
    }

    /** Get a contained entity by name.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired entity.
     *  @return An entity with the specified name, or null if none exists.
     */
    public ComponentEntity getEntity(String name) {
        try {
            workspace().getReadAccess();
            return (ComponentEntity)_containedEntities.get(name);
        } finally {
            workspace().doneReading();
        }
    }

    /** Enumerate the contained entities in the order they were added
     *  (using their setContainer() method).
     *  The returned enumeration is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of entities.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentEntity objects.
     */
    public Enumeration getEntities() {
        try {
            workspace().getReadAccess();
            // Copy the list so we can create a static enumeration.
            NamedList entitiesCopy = new NamedList(_containedEntities);
            Enumeration entities = entitiesCopy.elements();
            return entities;
        } finally {
            workspace().doneReading();
        }
    }

    /** Get a contained relation by name.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired relation.
     *  @return A relation with the specified name, or null if none exists.
     */
    public ComponentRelation getRelation(String name) {
        try {
            workspace().getReadAccess();
            return (ComponentRelation)_containedRelations.get(name);
        } finally {
            workspace().doneReading();
        }
    }

    /** Enumerate the relations contained by this entity.
     *  The returned enumeration is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of relations.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentRelation objects.
     */
    public Enumeration getRelations() {
        try {
            workspace().getReadAccess();
            // Copy the list so we can create a static enumeration.
            NamedList relationsCopy = new NamedList(_containedRelations);
            Enumeration relations = relationsCopy.elements();
            return relations;
        } finally {
            workspace().doneReading();
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
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param name The name of the new relation.
     *  @return The new relation.
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already in the container.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            ComponentRelation rel = new ComponentRelation(this, name);
            return rel;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return the number of contained entities.
     *  This method is read-synchronized on the workspace.
     *  @return The number of entities.
     */
    public int numEntities() {
        try {
            workspace().getReadAccess();
            return _containedEntities.size();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the number of contained relations.
     *  This method is read-synchronized on the workspace.
     *  @return The number of relations.
     */
    public int numRelations() {
        try {
            workspace().getReadAccess();
            return _containedRelations.size();
        } finally {
            workspace().doneReading();
        }
    }

    /** Remove all contained entities and unlink them from all relations.
     *  This is done by setting their containers to null.
     *  This method is read-synchronized on the workspace
     *  and increments its version number.
     */
    public void removeAllEntities() {
        try {
            workspace().getReadAccess();
            // Have to copy list to avoid corrupting the enumeration.
            // NOTE: Is this still true?  Or was this due to a bug in
            // NamedList?
            NamedList entitiesCopy = new NamedList(_containedEntities);
            Enumeration entities = entitiesCopy.elements();

            while (entities.hasMoreElements()) {
                ComponentEntity entity =
                    (ComponentEntity)entities.nextElement();
                try {
                    entity.setContainer(null);
                } catch (KernelException ex) {
                    // This exception should not be thrown.
                    throw new InternalErrorException(
                        "Internal error in ComponentRelation "
                        + "removeAllEntities() method!"
                        + ex.getMessage());
                }
            }
        } finally {
            workspace().doneReading();
        }
    }

    /** Remove all contained relations and unlink them from everything.
     *  This is done by setting their containers to null.
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     */
    public void removeAllRelations() {
        try {
            workspace().getWriteAccess();
            // Have to copy list to avoid corrupting the enumeration.
            // NOTE: Is this still true?  Or was this due to a bug in
            // NamedList?
            NamedList relationsCopy = new NamedList(_containedRelations);
            Enumeration relations = relationsCopy.elements();

            while (relations.hasMoreElements()) {
                ComponentRelation relation =
                    (ComponentRelation)relations.nextElement();
                try {
                    relation.setContainer(null);
                } catch (KernelException ex) {
                    // This exception should not be thrown.
                    throw new InternalErrorException(
                        "Internal error in ComponentRelation "
                        + "removeAllRelations() method!"
                        + ex.getMessage());
                }
            }
        } finally {
            workspace().doneWriting();
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
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException If the entity has no name, or the
     *   action would result in a recursive containment structure.
     *  @exception NameDuplicationException If the name collides with a name
     *  already in the entity.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (entity.deepContains(this)) {
            throw new IllegalActionException(entity, this,
                    "Attempt to construct recursive containment.");
        }
        _containedEntities.append(entity);
    }

    /** Add a relation to this container. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set
     *  the container of the relation to refer to this container.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param relation Relation to contain.
     *  @exception IllegalActionException If the relation has no name.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        _containedRelations.append(relation);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is speicified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket){
        try {
            workspace().getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & CONTENTS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "entities {\n";
                Enumeration enume = getEntities();
                while (enume.hasMoreElements()) {
                    ComponentEntity entity =
                        (ComponentEntity)enume.nextElement();
                    result +=
                            entity._description(detail, indent+1, 2) + "\n";
                }
                result += _getIndentPrefix(indent) + "} relations {\n";
                Enumeration enum = getRelations();
                while (enum.hasMoreElements()) {
                    Relation relation = (Relation)enum.nextElement();
                    result += relation._description(detail, indent+1, 2) + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Remove the specified entity. This method should not be used
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param entity The entity to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
        _containedEntities.remove(entity);
    }

    /** Remove the specified relation. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead with
     *  a null argument.
     *  The relation is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the relation in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param relation The relation to remove.
     */
    protected void _removeRelation(ComponentRelation relation) {
        _containedRelations.remove(relation);
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
        _entitynamecount += 1;
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
        _relationnamecount += 1;
        return name;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // List of contained entities.
    private NamedList _containedEntities = new NamedList(this);

    // List of contained ports.
    private NamedList _containedRelations = new NamedList(this);

    // Count of automatic names generated for entities and relations.
    private int _entitynamecount = 0;
    private int _relationnamecount = 0;

    // Flag indicating whether level-crossing connect is permitted.
    private boolean _levelCrossingConnectAllowed = false;
}
