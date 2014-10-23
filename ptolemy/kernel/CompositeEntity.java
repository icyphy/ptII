/* A CompositeEntity is a cluster in a clustered graph.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

 */
package ptolemy.kernel;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CompositeEntity

/**
 A CompositeEntity is a cluster in a clustered graph.
 I.e., it is a non-atomic entity, in that
 it can contain other entities and relations.  It supports transparent ports,
 where, in effect, the port of a contained entity is represented by a port
 of this entity. Methods that "deeply" traverse the topology
 see right through transparent ports.
 It may be opaque, in which case its ports are opaque and methods
 that "deeply" traverse the topology do not see through them.
 For instance, deepEntityList() returns the opaque entities
 directly or indirectly contained by this entity.
 <p>
 To add an entity or relation to this composite, call its
 setContainer() method with this composite as an argument.  To
 remove it, call its setContainer() method with a null argument (or
 another container). The entity must be an instance of
 ComponentEntity and the relation of ComponentRelation or an
 exception is thrown.  Derived classes may further constrain these
 to subclasses.  To do that, they should override the protected
 methods _addEntity() and _addRelation() and the public member
 newRelation().
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
 A CompositeEntity can contain instances of ComponentPort.  By default
 these ports will be transparent, although subclasses of CompositeEntity
 can make them opaque by overriding the isOpaque() method to return
 <i>true</i>. Derived classes may further constrain the ports to a
 subclass of ComponentPort.
 To do this, they should override the public method newPort() to create
 a port of the appropriate subclass, and the protected method _addPort()
 to throw an exception if its argument is a port that is not of the
 appropriate subclass.
 <p>
 Since contained entities implement the
 {@link ptolemy.kernel.util.Instantiable} interface,
 some may be class definitions.  If an entity is a class definition,
 then it is not included in the lists returned by
 {@link #entityList()}, {@link #entityList(Class)},
 {@link #deepEntityList()}, and {@link #allAtomicEntityList()}.
 Correspondingly, if it is not a class definition, then it is not
 included in the list returned by {@link #classDefinitionList()}.
 Contained class definitions are nonetheless required to have names
 distinct from contained entities that are not class definitions,
 and the method {@link #getEntity(String)} will return either
 a class definition or an entity that is not a class definition,
 as long as the name matches.  Note that contained entities that
 are class definitions cannot be connected to other entities.
 Moreover, they cannot be deleted as long as there are either
 subclasses or instances present.

 @author John S. Davis II, Edward A. Lee, contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class CompositeEntity extends ComponentEntity {
    /** Construct an entity in the default workspace with an empty string
     *  as its name. Add the entity to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public CompositeEntity() {
        super();
        _addIcon();
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
        _addIcon();
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
        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list that consists of all the atomic entities in a model.
     *  This method differs from {@link #deepEntityList()} in that
     *  this method looks inside opaque entities, whereas deepEntityList()
     *  does not. The returned list does not include any entities that
     *  are class definitions.
     *  @return a List of all atomic entities in the model.
     */
    public List allAtomicEntityList() {
        // We don't use an Iterator here so that we can modify the list
        // rather than having both an Iterator and a result list.
        //
        // Note:
        // deepEntityList() should be renamed to deepOpaqueEntityList()
        // allAtomicEntityList() to deepAtomicEntityList()
        // However, the change would require a fair amount of work.
        //LinkedList entities = (LinkedList) deepEntityList();
        List entities = deepEntityList();

        for (int i = 0; i < entities.size(); i++) {
            Object entity = entities.get(i);

            if (entity instanceof CompositeEntity) {
                // Remove the composite actor and add its containees.
                entities.remove(i);

                // Note that removing an element from the list causes
                // the indices of later elements to shift forward by 1.
                // We reduce the index i by one to match the index in
                // the list.
                i--;
                entities.addAll(((CompositeEntity) entity)
                        .allAtomicEntityList());
            }
        }

        return entities;
    }

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

    /** List the contained class definitions
     *  in the order they were added
     *  (using their setContainer() method). The returned list does
     *  not include any entities that are not class definitions.
     *  The returned list is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of class definitions.
     *  This method is read-synchronized on the workspace.
     *  @return A list of ComponentEntity objects.
     *  @see #entityList()
     */
    public List classDefinitionList() {
        try {
            _workspace.getReadAccess();

            if (_workspace.getVersion() == _classDefinitionListVersion) {
                return _classDefinitionListCache;
            }

            List result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                Iterator entities = _containedEntities.elementList().iterator();

                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();

                    if (entity.isClassDefinition()) {
                        result.add(entity);
                    }
                }

                _classDefinitionListCache = result;
                _classDefinitionListVersion = _workspace.getVersion();
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  This method gets read access on the workspace associated with
     *  this object.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *  cannot be cloned.
     *  @return A new CompositeEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        try {
            workspace().getReadAccess();
            // NOTE: The following assumes we will not do an exportMoML()
            // at the same time we are doing a clone(). Since clone() is used
            // to instantiate objects, this seems safe. But the field below
            // is shared with exportMoML().
            _levelCrossingLinks = new LinkedList<LinkRecord>();

            CompositeEntity newEntity = (CompositeEntity) super
                    .clone(workspace);

            newEntity._containedEntities = new NamedList(newEntity);
            newEntity._containedRelations = new NamedList(newEntity);

            // Clone the contained relations.
            Iterator relations = relationList().iterator();
            while (relations.hasNext()) {
                ComponentRelation relation = (ComponentRelation) relations
                        .next();
                ComponentRelation newRelation = (ComponentRelation) relation
                        .clone(workspace);
                // Assume that since we are dealing with clones,
                // exceptions won't occur normally.  If they do, throw a
                // CloneNotSupportedException.
                try {
                    newRelation.setContainer(newEntity);
                    // To support relation groups, duplicate any links
                    // to other relations. The links to ports will be
                    // done below in a way that preserves the order of the
                    // links. Links in ports have an order, whereas links
                    // in relations do not.
                    Enumeration links = relation._linkList.getContainers();
                    while (links.hasMoreElements()) {
                        Object link = links.nextElement();
                        if (link instanceof Relation) {
                            // Create the link only if the corresponding
                            // relation has been created. This ensures
                            // that the link is created exactly once.
                            // Get the relation using a relative name
                            // in case it's a level-crossing link.
                            Relation farRelation = newEntity
                                    .getRelation(((Nameable) link)
                                            .getName(this));
                            if (farRelation != null) {
                                newRelation.link(farRelation);
                            }
                        }
                    }
                } catch (KernelException ex) {
                    throw new CloneNotSupportedException(
                            "Failed to clone a CompositeEntity: "
                                    + ex.getMessage());
                }
            }

            // Clone the contained classes.
            Iterator classes = classDefinitionList().iterator();

            while (classes.hasNext()) {
                ComponentEntity classDefinition = (ComponentEntity) classes
                        .next();
                ComponentEntity newSubentity = (ComponentEntity) classDefinition
                        .clone(workspace);

                // Assume that since we are dealing with clones,
                // exceptions won't occur normally.  If they do, throw a
                // CloneNotSupportedException.
                try {
                    newSubentity.setContainer(newEntity);
                } catch (KernelException ex) {
                    throw new CloneNotSupportedException(
                            "Failed to clone a CompositeEntity: "
                                    + KernelException.stackTraceToString(ex));
                }
            }

            // Clone the contained entities.
            Iterator entities = entityList().iterator();

            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity) entities.next();
                ComponentEntity newSubentity = (ComponentEntity) entity
                        .clone(workspace);

                // Assume that since we are dealing with clones,
                // exceptions won't occur normally.  If they do, throw a
                // CloneNotSupportedException.
                try {
                    newSubentity.setContainer(newEntity);
                } catch (KernelException ex) {
                    throw new CloneNotSupportedException(
                            "Failed to clone a CompositeEntity: "
                                    + KernelException.stackTraceToString(ex));
                }

                // Clone the links of the ports of the cloned entities.
                Iterator ports = entity.portList().iterator();

                while (ports.hasNext()) {
                    ComponentPort port = (ComponentPort) ports.next();
                    Enumeration linkedRelations = port.linkedRelations();

                    int index = 0;
                    while (linkedRelations.hasMoreElements()) {
                        ComponentRelation rel = (ComponentRelation) linkedRelations
                                .nextElement();

                        // A null link (supported since indexed links) might
                        // yield a null relation here. EAL 7/19/00.
                        if (rel != null) {
                            //                             if (rel.getContainer() != this) {
                            //                                 throw new CloneNotSupportedException(
                            //                                         "Cannot clone a CompositeEntity with "
                            //                                                 + "level crossing transitions."
                            //                                                 + "  The relation was: " + rel
                            //                                                 + ", its container was: "
                            //                                                 + rel.getContainer()
                            //                                                 + ", which is not equal to "
                            //                                                 + this);
                            //                             }

                            Port newPort = newSubentity.getPort(port.getName());

                            // This may be a level-crossing link, in which case we have to
                            // defer it to the common container.
                            if (rel.getContainer() == this) {
                                // Not a level-crossing link.
                                ComponentRelation newRelation = newEntity
                                        .getRelation(rel.getName());
                                try {
                                    newPort.link(newRelation);
                                } catch (IllegalActionException ex) {
                                    throw new CloneNotSupportedException(
                                            "Failed to clone a CompositeEntity: "
                                                    + ex.getMessage());
                                }
                            } else {
                                // It is a level-crossing link.
                                // Find the common container.
                                NamedObj container = _commonContainer(port, rel);
                                if (container instanceof CompositeEntity) {
                                    List<LinkRecord> linkRecords = ((CompositeEntity) container)._levelCrossingLinks;
                                    if (linkRecords == null) {
                                        throw new CloneNotSupportedException(
                                                "Level crossing link goes outside of the class definition boundary: "
                                                        + port.getFullName()
                                                        + " and "
                                                        + rel.getFullName());
                                    }
                                    // NOTE: The record has the new port (after cloning), but
                                    // the old relation (before cloning) because we can't be sure
                                    // the new relation exists yet.
                                    LinkRecord record = new LinkRecord();
                                    record.port = newPort;
                                    record.relation1 = rel;
                                    record.relation2 = null;
                                    record.index = index;
                                    linkRecords.add(record);
                                }
                            }
                        }
                        index++;
                    }
                }
            }

            // Clone the inside links from the ports of this entity.
            Iterator ports = portList().iterator();

            while (ports.hasNext()) {
                ComponentPort port = (ComponentPort) ports.next();
                relations = port.insideRelationList().iterator();

                while (relations.hasNext()) {
                    Relation relation = (Relation) relations.next();
                    // To support level-crossing links to the inside,
                    // be sure to get the name of the relation relative to this.
                    ComponentRelation newRelation = newEntity
                            .getRelation(relation.getName(this));
                    Port newPort = newEntity.getPort(port.getName());

                    try {
                        newPort.link(newRelation);
                    } catch (IllegalActionException ex) {
                        throw new CloneNotSupportedException(
                                "Failed to clone a CompositeEntity: "
                                        + ex.getMessage());
                    }
                }
            }

            // Finally, clone level-crossing links, if there are any.
            if (_levelCrossingLinks != null) {
                for (LinkRecord record : _levelCrossingLinks) {
                    try {
                        if (record.port != null) {
                            String relationName = record.relation1
                                    .getName(this);
                            Relation newRelation = newEntity
                                    .getRelation(relationName);
                            if (newRelation == null) {
                                throw new CloneNotSupportedException(
                                        "Cloning level-crossing links failed. Relation missing: "
                                                + relationName);
                            }
                            record.port.insertLink(record.index, newRelation);
                        } else {
                            record.relation1.link(record.relation2);
                        }
                    } catch (IllegalActionException ex) {
                        throw new CloneNotSupportedException(
                                "Cloning level-crossing links failed: " + ex);
                    }
                }
            }

            return newEntity;
        } finally {
            _levelCrossingLinks = null;
            try {
                workspace().doneReading();
            } catch (Throwable ex) {
                throw new InternalErrorException(this, ex, "Internal Error: "
                        + "workspace().doneReading() failed?");
            }
        }
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
     *  <p>Note that if this method is being called many times, then
     *  it may be more efficient to use
     *  {@link #connect(ComponentPort, ComponentPort, String)}
     *  instead of this method because this method calls
     *  {@link #uniqueName(String)} each time, which
     *  searches the object for attributes, ports, entities and relations
     *  that may match a candidate unique name.
     *
     *  @param port1 The first port to connect.
     *  @param port2 The second port to connect.
     *  @return The ComponentRelation that is created to connect port1 and
     *  port2.
     *  @exception IllegalActionException If one of the arguments is null, or
     *   if a disallowed level-crossing connection would result.
     */
    public ComponentRelation connect(ComponentPort port1, ComponentPort port2)
            throws IllegalActionException {
        try {
            return connect(port1, port2, uniqueName("_R"));
        } catch (NameDuplicationException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(this, ex,
                    "Internal error in CompositeEntity.connect() method!");
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
     *  @param relationName The name of the new relation.
     *  @return The ComponentRelation that is created to connect port1 and
     *  port2.
     *  @exception IllegalActionException If one of the arguments is null, or
     *   if a disallowed level-crossing connection would result, or if the two
     *   ports are not in the same workspace as this entity.
     *  @exception NameDuplicationException If there is already a relation with
     *   the specified name in this entity.
     */
    public ComponentRelation connect(ComponentPort port1, ComponentPort port2,
            String relationName) throws IllegalActionException,
            NameDuplicationException {
        if (port1 == null || port2 == null) {
            throw new IllegalActionException(this,
                    "Attempt to connect null port.");
        }

        if (port1.workspace() != port2.workspace()
                || port1.workspace() != _workspace) {
            throw new IllegalActionException(port1, port2,
                    "Cannot connect ports because workspaces are different.");
        }

        try {
            _workspace.getWriteAccess();

            ComponentRelation ar = newRelation(relationName);

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
            _workspace.doneWriting();
        }
    }

    /** Return an iterator over contained objects. In this class,
     *  this is an iterator over attributes, ports, classes,
     *  entities, and relations.
     *  @return An iterator over instances of NamedObj contained by this
     *   object.
     */
    @Override
    public Iterator containedObjectsIterator() {
        return new ContainedObjectsIterator();
    }

    /** Return a list that consists of all the transparent and opaque
     *  composite entities in a model.  This method differs from
     *  {@link #allAtomicEntityList()} in that this method returns
     *  CompositeEntities and allAtomicEntityList() returns atomic
     *  entities.  This method differs from {@link #deepEntityList()}
     *  in that this method returns only opaque and transparent
     *  CompositeEntities, whereas deepEntityList() returns opaque
     *  ComponentEntities.  The returned list of this method does not
     *  include any entities that are class definitions.
     *  The {@link #entityList(Class)} method only returns entities in the
     *  current level, this method traverses the hierarchy.
     *  @return a List of all transparent and opaque composite entities in the
     *  model.  A transparent composite is a composite that does not
     *  contain a director.  An opaque composite is a composite that does
     *  contain a director.  Note that class definitions are also returned.
     */
    public List<CompositeEntity> deepCompositeEntityList() {
        try {
            _workspace.getReadAccess();
            LinkedList result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                Iterator entities = _containedEntities.elementList().iterator();
                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();
                    if (/*!entity.isClassDefinition() */
                            /* &&!entity.isOpaque() */
                            entity instanceof CompositeEntity) {
                        result.add(entity);
                        result.addAll(((CompositeEntity) entity)
                                .deepCompositeEntityList());
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the opaque entities that are directly or indirectly
     *  contained by this entity.  The list will be empty if there
     *  are no such contained entities. This list does not include
     *  class definitions nor anything contained by them.
     *  This method is read-synchronized on the workspace.
     *  @return A list of opaque ComponentEntity objects.
     *  @see #classDefinitionList()
     *  @see #allAtomicEntityList()
     */
    public List deepOpaqueEntityList() {
        try {
            _workspace.getReadAccess();
            List results = new ArrayList();
            _deepOpaqueEntityList(results);
            return results;
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the opaque entities that are directly or indirectly
     *  contained by this entity.  The list will be empty if there
     *  are no such contained entities. This list does not include
     *  class definitions nor anything contained by them.
     *  This method is read-synchronized on the workspace.
     *  @return A list of opaque ComponentEntity objects.
     *  @see #classDefinitionList()
     *  @see #allAtomicEntityList()
     */
    public List deepEntityList() {
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                Iterator entities = _containedEntities.elementList().iterator();
                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();

                    if (!entity.isClassDefinition()) {
                        if (entity.isOpaque()) {
                            result.add(entity);
                        } else {
                            result.addAll(((CompositeEntity) entity)
                                    .deepEntityList());
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the NamedObjs that are directly or indirectly
     *  contained by this entity.  The list will be empty if there
     *  are no such contained NamedObjs. This list does not include
     *  class definitions nor anything contained by them.
     *  This method is read-synchronized on the workspace.
     *  This method differs from deepEntityList() in that deepEntityList()
     *  does not look inside opaques.
     *  @return A list of opaque ComponentEntity objects.
     *  @see #classDefinitionList()
     *  @see #allAtomicEntityList()
     */
    public List deepNamedObjList() {
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                Iterator entities = _containedEntities.elementList().iterator();
                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();

                    if (!entity.isClassDefinition()) {
                        // if (entity.isOpaque()) {
                        result.add(entity);
                        if (!entity.isOpaque()) {
                            result.addAll(((CompositeEntity) entity)
                                    .deepNamedObjList());
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a set with the relations that are directly or indirectly
     *  contained by this entity.  The set will be empty if there
     *  are no such contained relations.
     *  This method is read-synchronized on the workspace.
     *  @return A set of ComponentRelation objects.
     */
    public Set<ComponentRelation> deepRelationSet() {
        try {
            _workspace.getReadAccess();

            Set<ComponentRelation> result = new HashSet<ComponentRelation>();

            _addAll(result, relationList());

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {

                for (Object entityObject : _containedEntities.elementList()) {
                    ComponentEntity entity = (ComponentEntity) entityObject;
                    if (!entity.isClassDefinition()) {
                        if (entity instanceof CompositeEntity) {
                            _addAll(result,
                                    ((CompositeEntity) entity)
                                    .deepRelationSet());
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the opaque entities that are directly or indirectly
     *  contained by this entity.  The enumeration will be empty if there
     *  are no such contained entities. The enumeration does not include
     *  any entities that are class definitions.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use deepEntityList() instead.
     *  @return An enumeration of opaque ComponentEntity objects.
     */
    @Deprecated
    public Enumeration deepGetEntities() {
        return Collections.enumeration(deepEntityList());
    }

    /** List the contained entities in the order they were added
     *  (using their setContainer() method). The returned list does
     *  not include any class definitions.
     *  The returned list is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of entities.
     *  This method is read-synchronized on the workspace.
     *  @return A list of ComponentEntity objects.
     *  @see #classDefinitionList()
     */
    public List entityList() {
        try {
            _workspace.getReadAccess();

            if (_workspace.getVersion() == _entityListVersion) {
                List entityList = _entityListCache.get();
                if (entityList != null) {
                    return entityList;
                }
            }

            List result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                Iterator entities = _containedEntities.elementList().iterator();

                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();

                    if (!entity.isClassDefinition()) {
                        result.add(entity);
                    }
                }

                _entityListCache = new WeakReference<List>(result);
                _entityListVersion = _workspace.getVersion();
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of the component entities contained by this object that
     *  are instances of the specified Java class.  If there are no such
     *  instances, then return an empty list. The returned list does not
     *  include class definitions.
     *  This method is read-synchronized on the workspace.
     *  @param filter The class of ComponentEntity of interest.
     *  @param <T> The type corresponding to the class of interest.
     *  @return A list of instances of specified class.
     *  @see #classDefinitionList()
     */
    public <T> List<T> entityList(Class<T> filter) {
        try {
            _workspace.getReadAccess();

            List<T> result = new LinkedList<T>();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                Iterator entities = _containedEntities.elementList().iterator();

                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();

                    if (filter.isInstance(entity)
                            && !entity.isClassDefinition()) {
                        result.add((T) entity);
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a sequence of MoML link attributes that describe
     *  any link between objects (ports, entities, and relations) that are
     *  present in the <i>filter</i> argument.  Both ends of the link
     *  must be present in <i>filter</i> for MoML to be generated for that
     *  link.  The <i>filter</i>
     *  argument normally contains ports, relations, and entities
     *  that are contained by this composite entity. If it contains
     *  an entity, then that is equivalent to containing all the ports
     *  contained by that entity.  It is recommended to use a collection
     *  class (such as HashSet) for which the contains() method is
     *  efficient.
     *  <p>
     *  If the filter argument is null, then return all the links that this
     *  composite is responsible for (i.e., apply no filtering).  If the
     *  argument is an empty collection, then return none of the links.  The
     *  links that this entity is responsible for are the inside links of
     *  its ports, and links on ports contained by contained entities.
     *  <p>
     *  If any link is found where both ends of the link are inherited objects,
     *  then that link is not exported. It is assumed that the base class
     *  will export that link.  For this purpose, a port of a contained
     *  entity is deemed to be an inherited object if it is itself a class
     *  element <i>and</i> its container is an inherited object.
     *  @param depth The depth below the MoML export in the hierarchy.
     *  @param filter A collection of ports, parameters, and entities, or
     *   null to apply no filtering.
     *  @return A string that describes the links present in the
     *  <i>filter</i>.
     *  @exception IOException If an I/O error occurs.
     */
    public String exportLinks(int depth, Collection filter) throws IOException {
        // To get the ordering right,
        // we read the links from the ports, not from the relations.
        StringBuffer result = new StringBuffer();

        // First, produce the inside links on contained ports.
        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            ComponentPort port = (ComponentPort) ports.next();
            // Skip the port if it is not persistent.
            if (port == null || !port.isPersistent()) {
                continue;
            }
            Iterator relations = port.insideRelationList().iterator();

            // The following variables are used to determine whether to
            // specify the index of the link explicitly, or to leave
            // it implicit.
            int index = -1;
            boolean useIndex = false;

            while (relations.hasNext()) {
                index++;

                ComponentRelation relation = (ComponentRelation) relations
                        .next();

                // Skip the relation if it is not persistent.
                if (relation != null && !relation.isPersistent()) {
                    continue;
                }

                if (relation == null) {
                    // Gap in the links.  The next link has to use an
                    // explicit index.
                    useIndex = true;
                    continue;
                }

                // If both ends of the link are inherited objects, then
                // suppress the export. This depends on the level of export
                // because if both ends of the link are implied, then the
                // link is implied.
                if (_commonImplier(relation, depth, port, depth)) {
                    continue;
                }

                // Apply filter.
                if (filter == null
                        || filter.contains(relation)
                        && (filter.contains(port) || filter.contains(port
                                .getContainer()))) {
                    // If the relation is not persistent, then do not export the link.
                    if (relation != null && !relation.isPersistent()) {
                        continue;
                    }
                    // In order to support level-crossing links, consider the
                    // possibility that the relation is not contained by this.
                    String relationName;

                    if (relation.getContainer() == this) {
                        relationName = relation.getName();
                    } else {
                        if (deepContains(relation)) {
                            // NOTE: This used to export the full name, but the
                            // relative name is sufficient.
                            relationName = relation.getName(this);
                        } else {
                            // Can't export the link here since when the
                            // MoML file is re-read there is no assurance that
                            // the relation exists when the link is to be
                            // created.  Need to delegate to the least common
                            // container.
                            _recordLevelCrossingLink(port, relation, null,
                                    index);
                            continue;
                        }
                    }

                    String escapedPortName = StringUtilities.escapeForXML(port
                            .getName());
                    String escapedRelationName = StringUtilities
                            .escapeForXML(relationName);
                    if (useIndex) {
                        useIndex = false;
                        result.append(_getIndentPrefix(depth) + "<link port=\""
                                + escapedPortName + "\" insertAt=\"" + index
                                + "\" relation=\"" + escapedRelationName
                                + "\"/>\n");
                    } else {
                        result.append(_getIndentPrefix(depth) + "<link port=\""
                                + escapedPortName + "\" relation=\""
                                + escapedRelationName + "\"/>\n");
                    }
                }
            }
        }

        // Next, produce the links on ports contained by contained entities.
        Iterator entities = entityList().iterator();

        while (entities.hasNext()) {
            ComponentEntity entity = (ComponentEntity) entities.next();

            // Skip the entity if it is not persistent.
            if (entity == null || !entity.isPersistent()) {
                continue;
            }

            ports = entity.portList().iterator();

            while (ports.hasNext()) {
                ComponentPort port = (ComponentPort) ports.next();
                // Skip the port if it is not persistent.
                if (port == null || !port.isPersistent()) {
                    continue;
                }
                Iterator relations = port.linkedRelationList().iterator();

                // The following variables are used to determine whether to
                // specify the index of the link explicitly, or to leave
                // it implicit.
                int index = -1;
                boolean useIndex = false;

                while (relations.hasNext()) {
                    index++;

                    ComponentRelation relation = (ComponentRelation) relations
                            .next();

                    // Skip the relation if it is not persistent.
                    if (relation != null && !relation.isPersistent()) {
                        continue;
                    }

                    if (relation == null) {
                        // Gap in the links.  The next link has to use an
                        // explicit index.
                        useIndex = true;
                        continue;
                    }

                    // If both ends of the link are inherited objects, then
                    // suppress the export.  This depends on the level of export
                    // because if both ends of the link are implied, then the
                    // link is implied. Note that we need for both the port
                    // to be implied and the port's container to share a
                    // common implier with the relation. We know that the port
                    // is contained within its container, so we don't have to
                    // check it separately for a common implier.
                    if (port.getDerivedLevel() <= depth + 1
                            && _commonImplier(relation, depth,
                                    port.getContainer(), depth)) {
                        continue;
                    }
                    // Used to have the previous logic here, skipping the link export,
                    // instead of the above.
                    // But careful!  It may be that the both the relation and
                    // the port are derived, but not from the same object.
                    // This can happen with level-crossing links.
                    // Check that the container above at which these two objects
                    // are implied is the same container.
                    // EAL 6/6/09
                    /*
                    int relationLevel = relation.getDerivedLevel();
                    int portLevel = port.getDerivedLevel();
                    if ((relationLevel <= depth)
                            && (portLevel <= (depth + 1))
                            && ((port.getContainer()).getDerivedLevel() <= depth)) {
                        continue;
                    }
                     */

                    // Apply filter.
                    if (filter == null
                            || filter.contains(relation)
                            && (filter.contains(port) || filter.contains(port
                                    .getContainer()))) {
                        // If the relation is not persistent, then do
                        // not export the link.
                        if (relation == null || !relation.isPersistent()) {
                            continue;
                        }
                        // In order to support level-crossing links,
                        // consider the possibility that the relation
                        // is not contained by this.
                        String relationName;

                        if (relation.getContainer() == this) {
                            relationName = relation.getName();
                        } else {
                            if (deepContains(relation)) {
                                // NOTE: This used to export the full name, but the
                                // relative name is sufficient.
                                relationName = relation.getName(this);
                            } else {
                                // Can't export the link here since when the
                                // MoML file is re-read there is no assurance that
                                // the relation exists when the link is to be
                                // created.  Need to delegate to the least common
                                // container.
                                _recordLevelCrossingLink(port, relation, null,
                                        index);
                                continue;
                            }
                        }

                        // Escape any < character that occurs in name.
                        // setName(String).
                        String escapedName = StringUtilities
                                .escapeForXML(entity.getName());
                        String escapedPortName = StringUtilities
                                .escapeForXML(port.getName());
                        String escapedRelationName = StringUtilities
                                .escapeForXML(relationName);
                        if (useIndex) {
                            useIndex = false;
                            result.append(_getIndentPrefix(depth)
                                    + "<link port=\"" + escapedName + "."
                                    + escapedPortName + "\" insertAt=\""
                                    + index + "\" relation=\""
                                    + escapedRelationName + "\"/>\n");
                        } else {
                            result.append(_getIndentPrefix(depth)
                                    + "<link port=\"" + escapedName + "."
                                    + escapedPortName + "\" relation=\""
                                    + escapedRelationName + "\"/>\n");
                        }
                    }
                }
            }
        }

        // Finally, produce the links that are between contained
        // relations only. Slight trickiness here: Both relations
        // on either side of a link have links to each other,
        // but we only want to represent one of the links.
        // It doesn't matter which one. We do this by accumulating
        // a set of visited relations.
        Set visitedRelations = new HashSet();
        Iterator relations = relationList().iterator();

        while (relations.hasNext()) {
            ComponentRelation relation = (ComponentRelation) relations.next();
            visitedRelations.add(relation);

            // Skip the relation if it is not persistent.
            if (relation == null || !relation.isPersistent()) {
                continue;
            }

            Iterator portsAndRelations = relation.linkedObjectsList()
                    .iterator();

            while (portsAndRelations.hasNext()) {
                Object portOrRelation = portsAndRelations.next();

                if (portOrRelation instanceof Relation) {
                    Relation otherRelation = (Relation) portOrRelation;

                    // Skip the relation if it is not persistent.
                    if (otherRelation == null || !otherRelation.isPersistent()) {
                        continue;
                    }

                    // If we have visited the other relation already, then
                    // we have already represented the link. Skip this.
                    if (visitedRelations.contains(otherRelation)) {
                        continue;
                    }

                    // If both ends of the link are inherited objects, then
                    // suppress the export. This depends on the level of export
                    // because if both ends of the link are implied, then the
                    // link is implied.
                    if (_commonImplier(relation, depth, otherRelation, depth)) {
                        continue;
                    }

                    // Apply filter.
                    if (filter == null || filter.contains(relation)
                            && filter.contains(otherRelation)) {
                        // In order to support level-crossing links, consider the
                        // possibility that the relation is not contained by this.
                        String relationName;

                        if (relation.getContainer() == this) {
                            relationName = relation.getName();
                        } else {
                            if (deepContains(relation)) {
                                // NOTE: This used to export the full name, but the
                                // relative name is sufficient.
                                relationName = relation.getName(this);
                            } else {
                                // Can't export the link here since when the
                                // MoML file is re-read there is no assurance that
                                // the relation exists when the link is to be
                                // created.  Need to delegate to the least common
                                // container.
                                _recordLevelCrossingLink(null, relation,
                                        otherRelation, 0);
                                continue;
                            }
                        }

                        String otherRelationName;

                        if (otherRelation.getContainer() == this) {
                            otherRelationName = otherRelation.getName();
                        } else {
                            // Can't export the link here since when the
                            // MoML file is re-read there is no assurance that
                            // the relation exists when the link is to be
                            // created.  Need to delegate to the least common
                            // container.
                            _recordLevelCrossingLink(null, relation,
                                    otherRelation, 0);
                            continue;
                        }

                        result.append(_getIndentPrefix(depth)
                                + "<link relation1=\"" + relationName
                                + "\" relation2=\"" + otherRelationName
                                + "\"/>\n");
                    }
                }
            }
        }

        return result.toString();
    }

    /** Override the base class to initialize a data structure that can
     *  capture and then export level-crossing links deeply contained
     *  structure within. Otherwise, this delegates to the base
     *  class to do all the work.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     *  @see ptolemy.kernel.util.MoMLExportable
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        try {
            _levelCrossingLinks = new LinkedList<LinkRecord>();
            super.exportMoML(output, depth, name);
        } finally {
            _levelCrossingLinks = null;
        }
    }

    /** Get the attribute with the given name. The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is contained by a (deeply) contained attribute, port,
     *  relation, or entity.
     *  If the name contains one or more periods, then it is assumed
     *  to be the relative name of an attribute contained by one of
     *  the contained attributes, ports, entities or relations.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    @Override
    public Attribute getAttribute(String name) {
        try {
            _workspace.getReadAccess();

            // Check attributes and ports first.
            Attribute result = super.getAttribute(name);

            if (result == null) {
                // Check entities first.
                String[] subnames = _splitName(name);

                if (subnames[1] != null) {
                    ComponentEntity entity = getEntity(subnames[0]);

                    if (entity != null) {
                        result = entity.getAttribute(subnames[1]);
                    }

                    if (result == null) {
                        // Check relations.
                        ComponentRelation relation = getRelation(subnames[0]);

                        if (relation != null) {
                            result = relation.getAttribute(subnames[1]);
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the contained entities in the order they were added
     *  (using their setContainer() method).
     *  The returned enumeration is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of entities.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use entityList() instead.
     *  @return An enumeration of ComponentEntity objects.
     */
    @Deprecated
    public Enumeration getEntities() {
        return Collections.enumeration(entityList());
    }

    /** Get a contained entity by name. The name may be compound,
     *  with fields separated by periods, in which case the entity
     *  returned is contained by a (deeply) contained entity.
     *  This method will return class definitions
     *  and ordinary entities.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired entity.
     *  @return An entity with the specified name, or null if none exists.
     */
    public ComponentEntity getEntity(String name) {
        try {
            _workspace.getReadAccess();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities == null) {
                return null;
            }

            String[] subnames = _splitName(name);

            if (subnames[1] == null) {
                return (ComponentEntity) _containedEntities.get(name);
            } else {
                Object match = _containedEntities.get(subnames[0]);

                if (match == null) {
                    return null;
                } else {
                    if (match instanceof CompositeEntity) {
                        return ((CompositeEntity) match).getEntity(subnames[1]);
                    } else {
                        return null;
                    }
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get a contained port by name. The name may be compound,
     *  with fields separated by periods, in which case the port returned is
     *  contained by a (deeply) contained entity.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired port.
     *  @return A port with the specified name, or null if none exists.
     */
    @Override
    public Port getPort(String name) {
        try {
            _workspace.getReadAccess();

            String[] subnames = _splitName(name);

            if (subnames[1] == null) {
                return super.getPort(name);
            } else {
                ComponentEntity match = getEntity(subnames[0]);

                if (match == null) {
                    return null;
                } else {
                    return match.getPort(subnames[1]);
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get a contained relation by name. The name may be compound,
     *  with fields separated by periods, in which case the relation
     *  returned is contained by a (deeply) contained entity.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired relation.
     *  @return A relation with the specified name, or null if none exists.
     */
    public ComponentRelation getRelation(String name) {
        try {
            _workspace.getReadAccess();

            String[] subnames = _splitName(name);

            if (subnames[1] == null) {
                return (ComponentRelation) _containedRelations.get(name);
            } else {
                ComponentEntity match = getEntity(subnames[0]);

                if (match == null) {
                    return null;
                } else {
                    if (match instanceof CompositeEntity) {
                        return ((CompositeEntity) match)
                                .getRelation(subnames[1]);
                    } else {
                        return null;
                    }
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the relations contained by this entity.
     *  The returned enumeration is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of relations.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use relationList() instead.
     *  @return An enumeration of ComponentRelation objects.
     */
    @Deprecated
    public Enumeration getRelations() {
        return Collections.enumeration(relationList());
    }

    /** Return false since CompositeEntities are not atomic.
     *  Note that this will return false even if there are no contained
     *  entities or relations.  Derived classes may not override this.
     *  To hide the contents of the entity, they should override isOpaque().
     *  @return False.
     */
    @Override
    public final boolean isAtomic() {
        return false;
    }

    /** Return false.  Derived classes may return true in order to hide
     *  their components behind opaque ports.
     *  @return True if the entity is opaque.
     *  @see ptolemy.kernel.CompositeEntity
     */
    @Override
    public boolean isOpaque() {
        return false;
    }

    /** Lazy version of {@link #allAtomicEntityList()}.
     *  In this base class, this is identical to allAtomicEntityList(),
     *  except that if any inside entities are lazy, their contents
     *  are listed lazily.  Derived classes may omit from the returned list any
     *  entities whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    public List lazyAllAtomicEntityList() {
        // We don't use an Iterator here so that we can modify the list
        // rather than having both an Iterator and a result list.
        //
        // Note:
        // deepEntityList() should be renamed to deepOpaqueEntityList()
        // allAtomicEntityList() to deepAtomicEntityList()
        // However, the change would require a fair amount of work.
        //LinkedList entities = (LinkedList) deepEntityList();
        List entities = lazyDeepEntityList();

        for (int i = 0; i < entities.size(); i++) {
            Object entity = entities.get(i);

            if (entity instanceof CompositeEntity) {
                // Remove the composite actor and add its containees.
                entities.remove(i);

                // Note that removing an element from the list causes
                // the indices of later elements to shift forward by 1.
                // We reduce the index i by one to match the index in
                // the list.
                i--;
                entities.addAll(((CompositeEntity) entity)
                        .lazyAllAtomicEntityList());
            }
        }

        return entities;
    }

    /** Lazy version of {#link #allCompositeEntityList()}.
     *  In this base class, this is identical to allCompositeEntityList(),
     *  except that if any contained composite is lazy, its contents
     *  are listed lazily.
     *  Derived classes may omit from the returned list any class
     *  definitions whose instantiation is deferred.
     *  @return A list of transparent ComponentEntity objects.
     */
    public List lazyAllCompositeEntityList() {
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                // Note that by directly using _containedEntities rather than
                // entityList() we are automatically lazy.
                Iterator entities = _containedEntities.elementList().iterator();

                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();
                    if (/*!entity.isClassDefinition()&& */!entity.isOpaque() /*entity instanceof CompositeEntity*/) {
                        result.add(entity);
                        result.addAll(((CompositeEntity) entity)
                                .lazyAllCompositeEntityList());

                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return all the transparent and opaque composites.
     *  In this base class, if any contained composite is lazy, its contents
     *  are listed lazily.
     *  Derived classes may omit from the returned list any class
     *  definitions whose instantiation is deferred.
     *  @return A list of transparent ComponentEntity objects.
     */
    public List lazyAllCompositeTransparentAndOpaqueEntityList() {
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                // Note that by directly using _containedEntities rather than
                // entityList() we are automatically lazy.
                Iterator entities = _containedEntities.elementList().iterator();
                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();
                    if (/*!entity.isClassDefinition()&& !entity.isOpaque()*/
                            entity instanceof CompositeEntity) {
                        result.add(entity);
                        result.addAll(((CompositeEntity) entity)
                                .lazyAllCompositeTransparentAndOpaqueEntityList());

                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Lazy version of {@link #classDefinitionList()}.
     *  In this base class, this is identical to classDefinitionList(),
     *  but derived classes may omit from the returned list any class
     *  definitions whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    public List lazyClassDefinitionList() {
        return classDefinitionList();
    }

    /** Lazy version of {@link #deepEntityList()}.
     *  In this base class, this is identical to deepEntityList(),
     *  except that if any contained composite is lazy, its contents
     *  are listed lazily.
     *  Derived classes may omit from the returned list any entities
     *  whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    public List lazyDeepEntityList() {
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();

            // This might be called from within a superclass constructor,
            // in which case there are no contained entities yet.
            if (_containedEntities != null) {
                // Note that by directly using _containedEntities rather than
                // entityList() we are automatically lazy.
                Iterator entities = _containedEntities.elementList().iterator();
                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();

                    if (!entity.isClassDefinition()) {
                        if (entity.isOpaque()) {
                            result.add(entity);
                        } else {
                            result.addAll(((CompositeEntity) entity)
                                    .lazyDeepEntityList());
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Lazy version of {@link #entityList()}.
     *  In this base class, this is identical to entityList(),
     *  but derived classes may omit from the returned list any
     *  entities whose instantiation is deferred.
     *  @return A list of ComponentEntity objects.
     */
    public List lazyEntityList() {
        return entityList();
    }

    /** Lazy version of {@link #relationList()}.
     *  In this base class, this is identical to relationList(),
     *  but derived classes may omit from the returned list any
     *  relations whose instantiation is deferred.
     *  @return A list of Relation objects.
     */
    public List lazyRelationList() {
        return relationList();
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
            _workspace.getWriteAccess();

            ComponentRelation rel = new ComponentRelation(this, name);
            return rel;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return the number of contained entities, not including
     *  class definitions.
     *  This method is read-synchronized on the workspace.
     *  @return The number of entities.
     *  @deprecated Use numberOfEntities
     *  @see #numberOfEntities()
     */
    @Deprecated
    public int numEntities() {
        return numberOfEntities();
    }

    /** Return the number of contained relations.
     *  This method is read-synchronized on the workspace.
     *  @return The number of relations.
     *  @deprecated Use numberOfRelations.
     */
    @Deprecated
    public int numRelations() {
        return numberOfRelations();
    }

    /** Return the number of contained entities, not including
     *  class definitions.
     *  This method is read-synchronized on the workspace.
     *  @return The number of entities.
     */
    public int numberOfEntities() {
        try {
            _workspace.getReadAccess();
            return entityList().size();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the number of contained relations.
     *  This method is read-synchronized on the workspace.
     *  @return The number of relations.
     */
    public int numberOfRelations() {
        try {
            _workspace.getReadAccess();
            return _containedRelations.size();
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the relations contained by this entity.
     *  The returned list is static in the sense
     *  that it is not affected by any subsequent additions or removals
     *  of relations.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of ComponentRelation objects.
     */
    public List relationList() {
        try {
            _workspace.getReadAccess();

            // Copy the list so we can create a static enumeration.
            NamedList relationsCopy = new NamedList(_containedRelations);
            return relationsCopy.elementList();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Remove all contained entities and unlink them from all relations.
     *  This is done by setting their containers to null.
     *  This method is read-synchronized on the workspace
     *  and increments its version number.
     */
    public void removeAllEntities() {
        try {
            _workspace.getReadAccess();

            Iterator entities = entityList().iterator();

            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity) entities.next();

                try {
                    entity.setContainer(null);
                } catch (KernelException ex) {
                    // This exception should not be thrown.
                    throw new InternalErrorException(this, ex,
                            "Internal error in CompositeEntity."
                                    + "removeAllEntities() method!");
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Remove all contained relations and unlink them from everything.
     *  This is done by setting their containers to null.
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     */
    public void removeAllRelations() {
        try {
            _workspace.getWriteAccess();

            Iterator relations = relationList().iterator();

            while (relations.hasNext()) {
                ComponentRelation relation = (ComponentRelation) relations
                        .next();

                try {
                    relation.setContainer(null);
                } catch (KernelException ex) {
                    // This exception should not be thrown.
                    throw new InternalErrorException(this, ex,
                            "Internal error in CompositeEntity."
                                    + "removeAllRelations() method!");
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Specify whether this object is a class definition.
     *  If the argument is true and this entity is not a class
     *  definition, then all level crossing relations that
     *  This method overrides the base class to check that if the
     *  argument is true, then this entity contains no ports with links.
     *  This method is write synchronized on the workspace.
     *  @param isClass True to make this object a class definition.
     *  @exception IllegalActionException If the argument is true and
     *   this entity contains ports with links.
     */
    @Override
    public void setClassDefinition(boolean isClass)
            throws IllegalActionException {
        // The situation is that an AO class definition is not allowed to have
        // any connections to other things.  Thus, if an instance is converted
        // to a class, it should be first disconnected. However, a Subscriber
        // has a "hidden" connection.  This connection may or may not cross
        // the boundary of the class definition. It should only be disconnected
        // if it does.

        // We need to disconnect upon invocation of
        // setClassDefinition().
        // It does have to traverse the whole tree below the actor being
        // converted to a class and disconnect any level-crossing link that
        // traverses to outside the class definition.

        // We also need to worry about the converse: When a class is converted
        // to an instance, we need to find all inside Publisher/Subscriber actors
        // and call _updateLinks().  (FIXME: this is not done)
        if (isClass && !isClassDefinition()) {
            try {
                workspace().getWriteAccess();
                // Converting from an instance to a class.
                super.setClassDefinition(isClass);
                _unlinkLevelCrossingLinksToOutside(this);
            } finally {
                workspace().doneWriting();
            }
        } else {
            super.setClassDefinition(isClass);
        }
    }

    /** Override the base class so that if the argument is null, all
     *  level-crossing links from inside this composite to outside this
     *  composite are removed.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container == null) {
            // This composite is being removed from the model.
            // Remove level-crossing links.
            try {
                _workspace.getWriteAccess();
                _unlinkLevelCrossingLinksToOutside(this);
                // Findbugs reports "load of known null value" so we use null
                super.setContainer(null);
            } finally {
                _workspace.doneWriting();
            }
        } else {
            super.setContainer(container);
        }
    }

    /** Return a string describing how many actors, parameters,
     * ports, and relations are in this CompositeEntity.
     * Entities whose instantiation is deferred are not
     * included.
     * @param className If non-null and non-empty, then also
     * include the number of objects with the give name.
     * @return a string describing the number of components.
     * @exception IllegalActionException If the class named by
     * actorClassName cannot be found.
     */
    public String statistics(String className) throws IllegalActionException {
        // FIXME: The right way to do this is to have each class
        // in the hierarchy have a statistics method.
        try {
            _workspace.getReadAccess();

            Class clazz = null;
            try {
                if (className != null && className.length() > 0) {
                    clazz = Class.forName(className);
                }
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex,
                        "Failed to instantiate \"" + className + "\"");
            }

            // Use the lazy version to avoid triggering a populate of LazyTypedCompositeActor.
            List atomicEntities = lazyAllAtomicEntityList();
            int entityCount = atomicEntities.size();

            Map<String, Integer> actorMap = new HashMap<String, Integer>();
            Integer one = Integer.valueOf(1);

            int attributeCount = 0, entityClassCount = 0;
            Iterator entities = atomicEntities.iterator();
            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity) entities.next();
                List attributeList = entity.attributeList();
                attributeCount += attributeList.size();

                Class entityClass = entity.getClass();

                // Create a map with the count of actors
                String entityClassName = entityClass.getName();
                if (!actorMap.containsKey(entityClassName)) {
                    actorMap.put(entityClassName, one);
                } else {
                    actorMap.put(entityClassName,
                            Integer.valueOf(actorMap.get(entityClassName) + 1));
                }

                if (clazz != null) {
                    if (clazz.isAssignableFrom(entityClass)) {
                        entityClassCount++;
                    } else {
                        // Search the attributes
                        Iterator attributes = attributeList.iterator();
                        while (attributes.hasNext()) {
                            Attribute attribute = (Attribute) attributes.next();
                            if (clazz.isAssignableFrom(attribute.getClass())) {
                                entityClassCount++;
                            }
                        }
                    }
                }
            }

            ArrayList actorArrayList = new ArrayList(actorMap.entrySet());
            //Sort the values based on values first and then keys.
            Collections.sort(actorArrayList, new CountComparator());

            StringBuffer actorNames = new StringBuffer();
            Iterator actors = actorArrayList.iterator();
            while (actors.hasNext()) {
                Map.Entry<String, Integer> actor = (Map.Entry) actors.next();
                actorNames.append(actor.getKey() + " " + actor.getValue()
                        + "\n");
            }

            int compositeEntityCount = 0;
            int opaqueCompositeEntityCount = 0;
            List relationList = lazyRelationList();
            int relationCount = relationList.size();
            if (clazz != null) {
                // Search the relations
                Iterator relations = relationList.iterator();
                while (relations.hasNext()) {
                    Relation relation = (Relation) relations.next();
                    if (clazz.isAssignableFrom(relation.getClass())) {
                        entityClassCount++;
                    }
                }
            }

            Map<Integer, Integer> compositeEntityDepthMap = new TreeMap<Integer, Integer>();
            entities = lazyAllCompositeTransparentAndOpaqueEntityList()
                    .iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                if (entity instanceof CompositeEntity) {
                    compositeEntityCount++;
                    if (((CompositeEntity) entity).isOpaque()) {
                        opaqueCompositeEntityCount++;
                    }
                    // Find the depth and add it to the list
                    Integer depth = Integer.valueOf(entity.depthInHierarchy());
                    if (!compositeEntityDepthMap.containsKey(depth)) {
                        compositeEntityDepthMap.put(depth, one);
                    } else {
                        compositeEntityDepthMap.put(depth,
                                Integer.valueOf(compositeEntityDepthMap
                                        .get(depth) + 1));
                    }

                    relationList = ((CompositeEntity) entity)
                            .lazyRelationList();
                    relationCount += relationList.size();
                    if (clazz != null) {
                        if (clazz.isAssignableFrom(entity.getClass())) {
                            entityClassCount++;
                        } else {
                            // Search the relations
                            Iterator relations = relationList.iterator();
                            while (relations.hasNext()) {
                                Relation relation = (Relation) relations.next();
                                if (clazz.isAssignableFrom(relation.getClass())) {
                                    entityClassCount++;
                                }
                            }
                        }
                    }
                }
            }

            // Generate a string with the depths
            StringBuffer compositeEntityDepths = new StringBuffer();
            for (Map.Entry<Integer, Integer> depth : compositeEntityDepthMap
                    .entrySet()) {
                compositeEntityDepths.append("Depth: " + depth.getKey()
                        + " # of Composites at that depth: " + depth.getValue()
                        + "\n");
            }

            return "Size Statistics for "
            + getFullName()
            + "\nAtomicEntities: "
            + entityCount
            + "\nCompositeEntities: "
            + compositeEntityCount
            + "\nOpaqueCompositeEntities: "
            + opaqueCompositeEntityCount
            + "\nRelations: "
            + relationCount
            + "\nAttributes: "
            + attributeCount
            + (clazz == null ? "" : "\nEntities of type \""
                    + clazz.getName() + "\": " + entityClassCount)
                    + "\nAtomic Actor Names and Counts:\n" + actorNames
                    + "\nComposite Entity Depths and Counts:\n"
                    + compositeEntityDepths;

        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a name that is guaranteed to not be the name of
     *  any contained attribute, port, class, entity, or relation.
     *  In this implementation, the argument
     *  is stripped of any numeric suffix, and then a numeric suffix
     *  is appended and incremented until a name is found that does not
     *  conflict with a contained attribute, port, class, entity, or relation.
     *  If this composite entity or any composite entity that it contains
     *  defers its MoML definition (i.e., it is an instance of a class or
     *  a subclass), then the prefix gets appended with "_<i>n</i>_",
     *  where <i>n</i> is the depth of this deferral. That is, if the object
     *  deferred to also defers, then <i>n</i> is incremented.
     *  <p>Note that this method should be called judiciously from when
     *  the CompositeEntity is large.  The reason is that this method
     *  searches for matching attributes, ports, classes, entities
     *  and relations, which can result in slow performance.
     *
     *  @param prefix A prefix for the name.
     *  @return A unique name.
     */
    @Override
    public String uniqueName(String prefix) {
        if (prefix == null) {
            prefix = "null";
        }

        prefix = _stripNumericSuffix(prefix);

        String candidate = prefix;

        // NOTE: The list returned by getPrototypeList() has
        // length equal to the number of containers of this object
        // that return non-null to getParent(). That number is
        // assured to be at least one greater than the corresponding
        // number for any of the parents returned by getParent().
        // Hence, we can use that number to minimize the likelyhood
        // of inadvertent capture.
        try {
            int depth = getPrototypeList().size();

            if (depth > 0) {
                prefix = prefix + "_" + depth + "_";
            }
        } catch (IllegalActionException e) {
            // Derivation invariant is not satisified.
            throw new InternalErrorException(e);
        }

        // FIXME: because we start with 2 each time, then if
        // we are calling this method many times we will need
        // to search the CompositeEntity for matching
        // attributes,  ports, entities and releations.
        // This will have poor behaviour for large CompositeEntities.
        // However, if we cached the uniqueNameIndex, then
        // it would tend to increase over time, which would be
        // unusual if we created a relation (_R2), deleted it
        // and created another relation, which would get the name
        // _R3, instead of _R2.

        int uniqueNameIndex = 2;

        while (getAttribute(candidate) != null || getPort(candidate) != null
                || getEntity(candidate) != null
                || getRelation(candidate) != null) {
            candidate = prefix + uniqueNameIndex++;
        }

        return candidate;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an entity or class definition to this container. This method
     *  should not be used directly.  Call the setContainer() method of
     *  the entity instead. This method does not set
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
                    "Attempt to construct recursive containment");
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

    /** Adjust the deferrals in this object. This method should
     *  be called on any newly created object that is created by
     *  cloning. While cloning, parent relations are set to null.
     *  That is, no object in the clone has a parent. This method
     *  identifies the correct parent for any object in the clone.
     *  To do this, it uses the class name. Specifically, if this
     *  object has a class name that refers to a class in scope,
     *  then it replaces the current parent with that object.
     *  To look for a class in scope, we go up the hierarchy, but
     *  no more times than the return value of getDerivedLevel().
     *  The reason for this is that if the class from which this
     *  object is defined is above that level, then we do not want
     *  to establish a parent relationship with that class. This
     *  object is implied, and the parent relationship of the object
     *  from which it is implied is sufficient.
     *  <p>
     *  Derived classes that contain other objects should recursively
     *  call this method on contained objects.
     *  @exception IllegalActionException If the class found in scope
     *   cannot be set.
     */
    @Override
    protected void _adjustDeferrals() throws IllegalActionException {
        super._adjustDeferrals();

        Iterator containedClasses = lazyClassDefinitionList().iterator();

        while (containedClasses.hasNext()) {
            NamedObj containedObject = (NamedObj) containedClasses.next();

            if (containedObject instanceof ComponentEntity) {
                ((ComponentEntity) containedObject)._adjustDeferrals();
            }
        }

        Iterator containedEntities = lazyEntityList().iterator();

        while (containedEntities.hasNext()) {
            NamedObj containedObject = (NamedObj) containedEntities.next();

            if (containedObject instanceof ComponentEntity) {
                ((ComponentEntity) containedObject)._adjustDeferrals();
            }
        }
    }

    /** Return a list of decorators contained by this object.
     *  This overrides the base class to include not only attributes that
     *  implement the {@link Decorator} interface, but also entities.
     *  @return A list of contained decorators.
     */
    @Override
    protected List<Decorator> _containedDecorators() {
        List<Decorator> result = super._containedDecorators();
        result.addAll(entityList(Decorator.class));
        return result;
    }

    /** List the opaque entities that are directly or indirectly
     *  contained by this entity.  The list will be empty if there
     *  are no such contained entities. This list does not include
     *  class definitions nor anything contained by them.
     *  This method is <b>not</b> read-synchronized on the workspace,
     *  its caller should be read-synchronized.
     *  @param result The list of opaque ComponentEntity objects.
     *  @see #classDefinitionList()
     *  @see #allAtomicEntityList()
     *  @see #deepEntityList()
     */
    protected void _deepOpaqueEntityList(List result) {

        // This might be called from within a superclass constructor,
        // in which case there are no contained entities yet.
        if (_containedEntities != null) {
            Iterator entities = _containedEntities.elementList().iterator();

            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity) entities.next();
                if (!entity.isClassDefinition()) {
                    if (entity.isOpaque()) {
                        result.add(entity);
                    } else {
                        ((CompositeEntity) entity)
                        ._deepOpaqueEntityList(result);
                    }
                }
            }
        }
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
     *  @exception IllegalActionException If thrown while getting the
     *  description of subcomponents.
     */
    @Override
    protected String _description(int detail, int indent, int bracket)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            StringBuffer result = new StringBuffer();

            if (bracket == 1 || bracket == 2) {
                result.append(super._description(detail, indent, 1));
            } else {
                result.append(super._description(detail, indent, 0));
            }

            if ((detail & CONTENTS) != 0) {
                if (result.toString().trim().length() > 0) {
                    result.append(" ");
                }

                result.append("classes {\n");

                Iterator classes = classDefinitionList().iterator();

                while (classes.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) classes.next();
                    result.append(entity._description(detail, indent + 1, 2)
                            + "\n");
                }

                result.append(_getIndentPrefix(indent) + "} entities {\n");

                Iterator entities = entityList().iterator();

                while (entities.hasNext()) {
                    ComponentEntity entity = (ComponentEntity) entities.next();
                    result.append(entity._description(detail, indent + 1, 2)
                            + "\n");
                }

                result.append(_getIndentPrefix(indent) + "} relations {\n");

                Iterator relations = relationList().iterator();

                while (relations.hasNext()) {
                    Relation relation = (Relation) relations.next();
                    result.append(relation._description(detail, indent + 1, 2)
                            + "\n");
                }

                result.append(_getIndentPrefix(indent) + "}");
            }

            if (bracket == 2) {
                result.append("}");
            }

            return result.toString();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class are the attributes, ports, contained relations,
     *  and contained entities, plus all links.  The links are written
     *  in an order that respects the ordering in ports, but not necessarily
     *  the ordering in relations.  This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        if (depth == 1 && getContainer() == null) {
            if (getAttribute("_createdBy") == null) {
                // If there is no _createdBy attribute, then add one.
                output.write(_getIndentPrefix(depth)
                        + "<property name=\"_createdBy\" " + "class=\""
                        + VersionAttribute.CURRENT_VERSION.getClass().getName()
                        + "\" value=\""
                        + VersionAttribute.CURRENT_VERSION.getExpression()
                        + "\">\n");
                output.write(_getIndentPrefix(depth) + "</property>\n");
            } else if (getAttribute("_createdBy") != null) {
                try {
                    ((VersionAttribute) getAttribute("_createdBy"))
                    .setExpression(VersionAttribute.CURRENT_VERSION
                            .getExpression());
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(this, ex,
                            "Failed to update _createdBy");
                }
            }
        }

        super._exportMoMLContents(output, depth);

        Iterator classes = classDefinitionList().iterator();

        while (classes.hasNext()) {
            ComponentEntity entity = (ComponentEntity) classes.next();
            entity.exportMoML(output, depth);
        }

        Iterator entities = entityList().iterator();

        while (entities.hasNext()) {
            ComponentEntity entity = (ComponentEntity) entities.next();
            entity.exportMoML(output, depth);
        }

        Iterator relations = relationList().iterator();

        while (relations.hasNext()) {
            ComponentRelation relation = (ComponentRelation) relations.next();
            relation.exportMoML(output, depth);
        }

        // NOTE: We used to write the links only if
        // this object did not defer to another
        // (getMoMLInfo().deferTo was null), and
        // would instead record links in a MoMLAttribute.
        // That mechanism was far too fragile.
        // EAL 3/10/04
        output.write(exportLinks(depth, null));

        // Export level crossing links, if there are any.
        if (_levelCrossingLinks != null) {
            for (LinkRecord record : _levelCrossingLinks) {
                if (record.port != null) {
                    // Do not export if the relation and port are derived and
                    // share a common container that has the parent-child
                    // relation that implies them.
                    if (!_commonImplier(record.relation1, depth
                            + _depthInside(record.relation1), record.port,
                            depth + _depthInside(record.port))) {

                        // Escape any < character in name. unescapeForXML occurs in
                        // setName(String).
                        String escapedRecordPortName = StringUtilities
                                .escapeForXML(record.port.getName(this));
                        String escapedRecordRelation1Name = StringUtilities
                                .escapeForXML(record.relation1.getName(this));
                        output.write(_getIndentPrefix(depth) + "<link port=\""
                                + escapedRecordPortName + "\" insertAt=\""
                                + record.index + "\" relation=\""
                                + escapedRecordRelation1Name + "\"/>\n");
                    }
                } else {
                    // Do not export if both relations are derived and
                    // share a common container that has the parent-child
                    // relation that implies them.
                    if (!_commonImplier(record.relation1, depth
                            + _depthInside(record.relation1), record.relation2,
                            depth + _depthInside(record.relation2))) {

                        String escapedRecordRelation1Name = StringUtilities
                                .escapeForXML(record.relation1.getName(this));
                        String escapedRecordRelation2Name = StringUtilities
                                .escapeForXML(record.relation2.getName(this));

                        output.write(_getIndentPrefix(depth)
                                + "<link relation1=\""
                                + escapedRecordRelation1Name
                                + "\" relation2=\""
                                + escapedRecordRelation2Name + "\"/>\n");
                    }
                }
            }
        }
    }

    /** Notify this entity that the given entity has been added inside it.
     *  This base class does nothing.   Derived classes may override it to
     *  do something useful in responds to the notification.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity The contained entity.
     */
    protected void _finishedAddEntity(ComponentEntity entity) {
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

    /** Validate attributes deeply contained by this object if they
     *  implement the Settable interface by calling their validate() method.
     *  This method overrides the base class to check attributes contained
     *  by the contained entities and relations.
     *  Errors that are triggered by this validation are handled by calling
     *  handleModelError().
     *  @param attributesValidated A HashSet of Attributes that have
     *  already been validated.  For example, Settables that implement
     *  the SharedSettable interface are validated only once.
     *  @see ptolemy.kernel.util.NamedObj#handleModelError(NamedObj, IllegalActionException)
     *  @exception IllegalActionException If the superclass throws it
     *  or if handleModelError() throws it.
     */
    @Override
    protected void _validateSettables(Collection attributesValidated)
            throws IllegalActionException {
        super._validateSettables(attributesValidated);
        Iterator classes = classDefinitionList().iterator();
        while (classes.hasNext()) {
            Entity entity = (Entity) classes.next();
            if (entity instanceof Settable) {
                try {
                    Collection validated = ((Settable) entity).validate();
                    if (validated != null) {
                        attributesValidated.addAll(validated);
                    }
                    attributesValidated.add(entity);
                } catch (IllegalActionException ex) {
                    if (!handleModelError(this, ex)) {
                        throw ex;
                    }
                }
            }
            entity._validateSettables(attributesValidated);
        }

        Iterator entities = entityList().iterator();
        while (entities.hasNext()) {
            Entity entity = (Entity) entities.next();
            if (entity instanceof Settable) {
                try {
                    Collection validated = ((Settable) entity).validate();
                    if (validated != null) {
                        attributesValidated.addAll(validated);
                    }
                    attributesValidated.add(entity);
                } catch (IllegalActionException ex) {
                    if (!handleModelError(this, ex)) {
                        throw ex;
                    }
                }
            }
            entity._validateSettables(attributesValidated);
        }

        Iterator relations = relationList().iterator();
        while (relations.hasNext()) {
            Relation relation = (Relation) relations.next();
            if (relation instanceof Settable) {
                try {
                    Collection validated = ((Settable) relation).validate();
                    if (validated != null) {
                        attributesValidated.addAll(validated);
                    }
                    attributesValidated.add(relation);
                } catch (IllegalActionException ex) {
                    if (!handleModelError(this, ex)) {
                        throw ex;
                    }
                }
            }
            relation.validateSettables();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Level-crossing links within this composite for which this composite
     *  is responsible.  This data structure is populated when exportMoML()
     *  is called.
     */
    protected List<LinkRecord> _levelCrossingLinks;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Add all elements from the sourceList into the targetList.
     */
    @SuppressWarnings("unchecked")
    private static <T> void _addAll(Set<T> result, Collection<?> sourceList) {
        for (Object object : sourceList) {
            result.add((T) object);
        }
    }

    /** Add a default icon description. */
    private void _addIcon() {
        _attachText("_iconDescription", _defaultIcon);
    }

    /** Find the least common container of the two objects.
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @return The least common container, or null if there
     *   isn't one.
     */
    private NamedObj _commonContainer(NamedObj object1, NamedObj object2) {
        NamedObj container = object1.getContainer();
        while (container != null) {
            if (container.deepContains(object2)) {
                return container;
            }
            container = container.getContainer();
        }
        return null;
    }

    /** Return true if the two specified objects are both derived,
     *  it is the same container above them whose parent-child
     *  relationship makes them derived, or if one of the containers
     *  contains the other, and those containers are
     *  no more than <i>depth1</i> and <i>depth2</i> above them,
     *  respectively.
     *  @param object1 The first object.
     *  @param depth1 The depth of the first object.
     *  @param object2 The second object.
     *  @param depth2 The depth of the second object.
     *  @return True if the two specified objects are both derived
     *   at a common level no more than depth above them.
     */
    private boolean _commonImplier(NamedObj object1, int depth1,
            NamedObj object2, int depth2) {
        if (object1 == null || object2 == null) {
            return false;
        }
        int object1Level = object1.getDerivedLevel();
        int object2Level = object2.getDerivedLevel();
        if (object1Level <= depth1 && object2Level <= depth2) {
            NamedObj object1Container = object1;
            while (object1Level > 0) {
                object1Container = object1Container.getContainer();
                object1Level--;
                // It's not clear to me how this occur, but if relationCaontiner
                // is null, then clearly there is no common container that
                // implies the two objects.
                if (object1Container == null) {
                    return false;
                }
            }
            NamedObj object2Container = object2;
            while (object2Level > 0) {
                object2Container = object2Container.getContainer();
                object2Level--;
                // It's not clear to me how this occur, but if relationCaontiner
                // is null, then clearly there is no common container that
                // implies the two objects.
                if (object2Container == null) {
                    return false;
                }
            }
            if (object1Container == object2Container
                    || object1Container.deepContains(object2Container)
                    || object2Container.deepContains(object1Container)) {
                return true;
            }
        }
        return false;
    }

    /** Return the depth of specified object inside this.
     *  That is, return 0 if the specified object is this, 1 if this
     *  contains it, 2 if this contains the container
     *  of it, etc.
     *  @param containee The object contained.
     *  @return The depth of the containment, or -1 if the specified object
     *   is not deeply contained by this.
     */
    private int _depthInside(NamedObj containee) {
        int result = 0;
        NamedObj candidate = containee;
        while (candidate != null) {
            if (candidate == this) {
                return result;
            }
            result++;
            candidate = candidate.getContainer();
        }
        return -1;
    }

    /** Record a level-crossing link with the least common container if there
     *  is such a least common container and if that least common container is
     *  currently exporting MoML. Otherwise, do nothing.
     *  @param port The port, or null for a link between relations.
     *  @param relation1 The first relation.
     *  @param relation2 The second relation.
     *  @param index The index of the link.
     */
    private void _recordLevelCrossingLink(Port port, Relation relation1,
            Relation relation2, int index) {
        // Find the least common container.
        NamedObj container;
        if (port != null) {
            container = _commonContainer(port, relation1);
        } else {
            // This is a link between relations.
            // Find the common container.
            container = _commonContainer(relation1, relation2);
            // We have to make sure
            // that the link only appears once in the export MoML.
            // To do this, check to see whether the common container
            // already contains a link record with relation2 in the
            // position of relation1.
            if (container instanceof CompositeEntity) {
                List<LinkRecord> linkRecords = ((CompositeEntity) container)._levelCrossingLinks;
                if (linkRecords != null) {
                    for (LinkRecord record : linkRecords) {
                        if (record.relation1 == relation2) {
                            return;
                        }
                    }
                }
            }
        }
        if (container instanceof CompositeEntity) {
            List<LinkRecord> linkRecords = ((CompositeEntity) container)._levelCrossingLinks;
            // If the common container is outside the scope of what is being exported,
            // then linkRecords will be null.
            if (linkRecords != null) {
                LinkRecord record = new LinkRecord();
                record.port = port;
                record.relation1 = relation1;
                record.relation2 = relation2;
                record.index = index;
                linkRecords.add(record);
            }
        }
    }

    /** Remove all level-crossing links from relations contained by
     *  the specified entity to ports or relations outside this
     *  composite entity, and from ports contained by entities
     *  contained by the specified entity to relations outside this
     *  composite entity.
     *  @param entity The entity in which to look for relations or
     *   (if it is an instance of CompositeEntity), entities with ports.
     */
    private void _unlinkLevelCrossingLinksToOutside(CompositeEntity entity) {
        // Look for relations with level crossing links first.
        // Here we use the lazy version so as to not trigger evaluation
        // of lazy contents. We assume that if and when those contents
        // are evaluated, if there is a level-crossing list, it will
        // trigger an error.
        Iterator relations = entity.lazyRelationList().iterator();
        while (relations.hasNext()) {
            ComponentRelation relation = (ComponentRelation) relations.next();
            Iterator linkedObjects = relation.linkedObjectsList().iterator();
            while (linkedObjects.hasNext()) {
                Object linkedObject = linkedObjects.next();

                Nameable relationContainer = relation.getContainer();
                if (linkedObject instanceof Relation) {

                    Relation linkedRelation = (Relation) linkedObject;
                    Nameable linkedObjectContainer = linkedRelation
                            .getContainer();
                    if (relationContainer != linkedObjectContainer
                            && linkedObjectContainer.getContainer() != relationContainer) {
                        relation.unlink(linkedRelation);
                    }
                } else {
                    // Must be a port.
                    Port linkedPort = (Port) linkedObject;
                    Nameable linkedObjectContainer = linkedPort.getContainer();
                    if (relationContainer != linkedObjectContainer
                            && linkedObjectContainer.getContainer() != relationContainer) {
                        linkedPort.unlink(relation);
                    }
                }
            }
        }
        // Next look for ports with level-crossing links.
        Iterator entities = entity.entityList().iterator();
        while (entities.hasNext()) {
            ComponentEntity containedEntity = (ComponentEntity) entities.next();
            // If the contained entity is a composite entity, then unlink
            // anything inside it as well.
            if (containedEntity instanceof CompositeEntity) {
                _unlinkLevelCrossingLinksToOutside((CompositeEntity) containedEntity);
            }
            // Now unlink its ports.
            Iterator ports = containedEntity.portList().iterator();
            while (ports.hasNext()) {
                ComponentPort port = (ComponentPort) ports.next();
                Iterator linkedRelations = port.linkedRelationList().iterator();
                while (linkedRelations.hasNext()) {
                    ComponentRelation relation = (ComponentRelation) linkedRelations
                            .next();
                    if (relation != null && !deepContains(relation)) {
                        port.unlink(relation);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         friendly variables                 ////
    // The following are friendly to support the move* methods of
    // Relation and ComponentEntity.

    /** List of contained entities. */
    NamedList _containedEntities = new NamedList(this);

    /** @serial List of contained ports. */
    NamedList _containedRelations = new NamedList(this);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Cache of class definition list. */
    private transient List _classDefinitionListCache;

    /** Workspace version for cache. */
    private transient long _classDefinitionListVersion = -1L;

    /** The default value icon.  This is static so that we avoid doing
     *  string concatenation each time we construct this object.
     */
    private static String _defaultIcon = "<svg>\n"
            + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
            + "height=\"40\" style=\"fill:red\"/>\n"
            + "<rect x=\"-28\" y=\"-18\" width=\"56\" "
            + "height=\"36\" style=\"fill:lightgrey\"/>\n"
            + "<rect x=\"-15\" y=\"-10\" width=\"10\" height=\"8\" "
            + "style=\"fill:white\"/>\n"
            + "<rect x=\"-15\" y=\"2\" width=\"10\" height=\"8\" "
            + "style=\"fill:white\"/>\n"
            + "<rect x=\"5\" y=\"-4\" width=\"10\" height=\"8\" "
            + "style=\"fill:white\"/>\n"
            + "<line x1=\"-5\" y1=\"-6\" x2=\"0\" y2=\"-6\"/>"
            + "<line x1=\"-5\" y1=\"6\" x2=\"0\" y2=\"6\"/>"
            + "<line x1=\"0\" y1=\"-6\" x2=\"0\" y2=\"6\"/>"
            + "<line x1=\"0\" y1=\"0\" x2=\"5\" y2=\"0\"/>" + "</svg>\n";

    /** Cache of entity list. */
    private transient WeakReference<List> _entityListCache;

    /** Workspace version for cache. */
    private transient long _entityListVersion = -1L;

    /** @serial Flag indicating whether level-crossing connect is permitted. */
    private boolean _levelCrossingConnectAllowed = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class is an iterator over all the contained objects
     *  (all instances of NamedObj). In this class, the contained
     *  objects are attributes first, then ports, then entities,
     *  then relations.
     */
    protected class ContainedObjectsIterator extends
    Entity.ContainedObjectsIterator {
        /** Create an iterator over all the contained objects, which
         *  for CompositeEntities are attributes, ports, classes
         *  entities, and relations.
         */
        public ContainedObjectsIterator() {
            super();
            _classListIterator = classDefinitionList().iterator();
            _entityListIterator = entityList().iterator();
            _relationListIterator = relationList().iterator();
        }

        /** Return true if the iteration has more elements.
         *  In this class, this returns true if there are more
         *  attributes, ports, classes, entities, or relations.
         *  @return True if there are more elements.
         */
        @Override
        public boolean hasNext() {
            if (super.hasNext()) {
                return true;
            }
            if (_classListIterator.hasNext()) {
                return true;
            }
            if (_entityListIterator.hasNext()) {
                return true;
            }
            return _relationListIterator.hasNext();
        }

        /** Return the next element in the iteration.
         *  In this base class, this is the next attribute or port.
         *  @return The next attribute or port.
         */
        @Override
        public Object next() {
            if (super.hasNext()) {
                return super.next();
            }

            if (_classListIterator.hasNext()) {
                return _classListIterator.next();
            }

            if (_entityListIterator.hasNext()) {
                return _entityListIterator.next();
            }

            return _relationListIterator.next();
        }

        /** The remove() method is not supported because is is not
         *  supported in NamedObj.ContainedObjectsIterator.remove().
         */
        @Override
        public void remove() {
            super.remove();
        }

        private Iterator _classListIterator = null;

        private Iterator _entityListIterator = null;

        private Iterator _relationListIterator = null;
    }

    /** A comparator for a &lt;String&gt;&lt;Integer&gt; Map. */
    private static class CountComparator implements Comparator {
        @Override
        public int compare(Object object1, Object object2) {
            int result = 0;
            Map.Entry entry1 = (Map.Entry) object1;
            Map.Entry entry2 = (Map.Entry) object2;

            Integer value1 = (Integer) entry1.getValue();
            Integer value2 = (Integer) entry2.getValue();

            if (value1.compareTo(value2) == 0) {
                String className1 = (String) entry1.getKey();
                String className2 = (String) entry2.getKey();
                result = className1.compareTo(className2);
            } else {
                result = value2.compareTo(value1);
            }

            return result;
        }
    }

    /** A data structure for level-crossing links. */
    private static class LinkRecord {
        public Port port;
        public Relation relation1;
        public Relation relation2;
        public int index;
    }
}
