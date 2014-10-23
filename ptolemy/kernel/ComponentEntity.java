/* A ComponentEntity is a vertex in a clustered graph.

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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ComponentEntity

/**
 A ComponentEntity is a component in a CompositeEntity.
 It might itself be composite, but in this base class it is assumed to
 be atomic (meaning that it cannot contain components).
 <p>
 Derived classes may further constrain the container to be
 a subclass of CompositeEntity.  To do this, they should override
 the protected method _checkContainer() to throw an exception.
 <p>
 A ComponentEntity can contain instances of ComponentPort.  Derived
 classes may further constrain to a subclass of ComponentPort.
 To do this, they should override the public method newPort() to create
 a port of the appropriate subclass, and the protected method _addPort()
 to throw an exception if its argument is a port that is not of the
 appropriate subclass.

 @author John S. Davis II, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class ComponentEntity<T extends ComponentPort> extends Entity<T> {
    /** Construct an entity in the default workspace with an empty string
     *  The object is added to the workspace directory.
     *  as its name. Increment the version number of the workspace.
     */
    public ComponentEntity() {
        super();
        _addIcon();
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public ComponentEntity(Workspace workspace) {
        super(workspace);
        _addIcon();
    }

    /** Construct an entity with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ComponentEntity(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name);
        setContainer(container);
        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new component entity that defers its definition to the
     *  same object as this one (or to none) that has no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return A new instance of ComponentEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ComponentEntity newObject = (ComponentEntity) super.clone(workspace);
        newObject._container = null;
        return newObject;
    }

    /** Get the container entity.
     *  @return The container, which is an instance of CompositeEntity.
     *  @see #setContainer(CompositeEntity)
     */
    @Override
    public NamedObj getContainer() {
        return _container;
    }

    /** Create an instance by cloning this object and then adjusting
     *  the parent-child relationships between the clone and its parent.
     *  Specifically, the clone defers its definition to this object,
     *  which becomes its "parent." It inherits all the objects contained
     *  by this object. If this object is a composite, then this method
     *  adjusts any deferral relationships that are entirely contained
     *  within the clone. That is, for any parent-child relationship that
     *  is entirely contained within this object (i.e., both the parent
     *  and the child are deeply contained by this object), a corresponding
     *  parent-child relationship is created within the clone such that
     *  both the parent and the child are entirely contained within
     *  the clone.
     *  <p>
     *  The new object is not a class definition (it is by default an
     *  "instance" rather than a "class").  To make it a class
     *  definition (a "subclass"), call setClassDefinition(true).
     *  <p>
     *  This method overrides the base class to use setContainer() to
     *  specify the container.
     *  @see #setClassDefinition(boolean)
     *  @param container The container for the instance, or null
     *   to instantiate it at the top level.
     *  @param name The name for the clone.
     *  @return A new instance that is a clone of this object
     *   with adjusted deferral relationships.
     *  @exception CloneNotSupportedException If this object
     *   cannot be cloned.
     *  @exception IllegalActionException If this object is not a
     *   class definition
     *   or the proposed container is not acceptable.
     *  @exception NameDuplicationException If the name collides with
     *   an object already in the container.
     */
    @Override
    public Instantiable instantiate(NamedObj container, String name)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        if (container != null && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this,
                    "Cannot instantiate into a container that is not an "
                            + "instance of CompositeEntity: "
                            + container.getFullName());
        }

        ComponentEntity clone = (ComponentEntity) super.instantiate(container,
                name);
        // Derived classes like this one are responsible for setting the container.
        clone.setContainer((CompositeEntity) container);

        clone._adjustDeferrals();

        // Now that there is a new parent-child relationship,
        // we need to propagate values from the parent to the child.
        // Note that this isn't needed to get the
        // right values, since the child has been cloned
        // from the parent. However, this will set the override
        // levels appropriately in the child.
        // NOTE: This could be done by calling propagateValues(),
        // but that would be extremely inefficient since it would
        // propagate to all previously instantiated instances as
        // well as to the one new clone.
        clone._adjustOverride(0);

        return clone;
    }

    /** Return true if the entity is atomic.
     *  An atomic entity is one that cannot have components.
     *  Instances of this base class are always atomic.
     *  Derived classes that return false are assumed to be instances of
     *  CompositeEntity or a class derived from that.
     *  @return True if the entity is atomic.
     *  @see ptolemy.kernel.CompositeEntity
     */
    public boolean isAtomic() {
        return true;
    }

    /** Return true if the entity is opaque.
     *  An opaque entity is one that either is atomic or hides
     *  its components behind opaque ports.
     *  Instances of this base class are always opaque.
     *  Derived classes may be transparent, in which case they return false
     *  to this method and to isAtomic().
     *  @return True if the entity is opaque.
     *  @see ptolemy.kernel.CompositeEntity
     */
    public boolean isOpaque() {
        return true;
    }

    /** Move this object down by one in the list of entities of
     *  its container. If this object is already last, do nothing.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveDown() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedEntities.moveDown(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedEntities.moveDown(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the first position in the list
     *  of entities of the container. If this object is already first,
     *  do nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToFirst() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedEntities.moveToFirst(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedEntities.moveToFirst(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the specified position in the list
     *  of entities of the container. If this object is already at
     *  the specified position, do nothing. Increment the version
     *  of the workspace.
     *  @param index The position to move this object to.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container or if the index is out of bounds.
     */
    @Override
    public int moveToIndex(int index) throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedEntities.moveToIndex(this, index);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedEntities.moveToIndex(derived, index);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the last position in the list
     *  of entities of the container.  If this object is already last,
     *  do nothing.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToLast() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedEntities.moveToLast(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedEntities.moveToLast(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object up by one in the list of
     *  entities of the container. If this object is already first, do
     *  nothing. Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveUp() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._containedEntities.moveUp(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = (CompositeEntity) derived.getContainer();
                container._containedEntities.moveUp(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Create a new port with the specified name.
     *  The container of the port is set to this entity.
     *  This overrides the base class to create an instance of ComponentPort.
     *  Derived classes may override this to further constrain the ports.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param name The new port name.
     *  @return The new port
     *  @exception IllegalActionException If the argument is null.
     *  @exception NameDuplicationException If this entity already has a
     *   port with the specified name.
     */
    @Override
    public Port newPort(String name) throws IllegalActionException,
    NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            Port port = new ComponentPort(this, name);
            return port;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Propagate the existence of this object.
     *  If this object has a container, then ensure that all
     *  objects derived from the container contain an object
     *  with the same class and name as this one. Create that
     *  object when needed. Return the list of objects that are created.
     *  This overrides the base class to adjust deferrals within
     *  the objects that are created by cloning this one.
     *  @return A list of derived objects of the same class
     *   as this object that are created.
     *  @exception IllegalActionException If the object does
     *   not exists and cannot be created.
     */
    @Override
    public List propagateExistence() throws IllegalActionException {
        // Otherwise, _override probably doesn't get set in the
        // derived object that is created.
        List result = super.propagateExistence();

        // Adjust deferrals in all the newly created objects.
        Iterator clones = result.iterator();

        while (clones.hasNext()) {
            ComponentEntity clone = (ComponentEntity) clones.next();
            clone._adjustDeferrals();
        }

        return result;
    }

    /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.  If this entity is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  <p>Note that for entities that are created by parsing MoML,
     *  it may be necessary to call MoMLParser.purgeModelRecord().
     *  This is especially critical when the MoML file imports
     *  MoML classes.  See Effigy.setContainer() for details.
     *
     *  <p>Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context.  This method is write-synchronized
     *  to the workspace and increments its version number.
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
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null && _workspace != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }

        CompositeEntity previousContainer = (CompositeEntity) getContainer();

        if (previousContainer == container) {
            return;
        }

        _notifyHierarchyListenersBeforeChange();

        try {
            _workspace.getWriteAccess();
            _checkContainer(container);

            // NOTE: The following code is quite tricky.  It is very careful
            // to leave a consistent state even in the face of unexpected
            // exceptions.  Be very careful if modifying it.

            // Do this first, because it may throw an exception, and we have
            // not yet changed any state.
            if (container != null) {
                // checkContainer() above ensures that this cast is valid.
                container._addEntity(this);

                if (previousContainer == null) {
                    _workspace.remove(this);
                }
            }

            _container = container;

            if (previousContainer != null) {
                // This is safe now because it does not throw an exception.
                previousContainer._removeEntity(this);
            }

            if (container == null) {
                Iterator ports = portList().iterator();

                while (ports.hasNext()) {
                    Port port = (Port) ports.next();
                    port.unlinkAll();
                }

                // Since the new container is null, this object is being
                // deleted. Break deferral references that it may have.
                _setParent(null);
            } else {
                // checkContainer() above ensures that this cast is valid.
                container._finishedAddEntity(this);

                // Transfer any queued change requests to the
                // new container.  There could be queued change
                // requests if this component is deferring change
                // requests.
                if (_changeRequests != null) {
                    Iterator requests = _changeRequests.iterator();

                    while (requests.hasNext()) {
                        ChangeRequest request = (ChangeRequest) requests.next();
                        container.requestChange(request);
                    }

                    _changeRequests = null;
                }
            }

            // Validate all deeply contained settables, since
            // they may no longer be valid in the new context.
            // Optimization: During construction, the previous
            // container will be null. It doesn't make sense
            // to validate at this point, since there shouldn't
            // actually be any contained settables. Moreover,
            // if the container is being set to null, then the
            // component is being discarded. It shouldn't be
            // necessary to validate settables at this point
            // since all dependents should be within this
            // scope. EAL 9/6/06
            if (previousContainer != null && container != null) {
                // NOTE: This somehow prevents CompositeEntity.validateSettables
                // from ever being called when a model is loaded.
                validateSettables();
            }
        } finally {
            try {
                // Since we definitely notified the listeners
                // before the change, we must definitely notify
                // them after the change, even if the change caused
                // some exceptions. Note that this too may trigger
                // exceptions.
                _notifyHierarchyListenersAfterChange();
            } finally {
                _workspace.doneWriting();
            }
        }
    }

    /** Set the name of the ComponentEntity. If there is already
     *  a ComponentEntity of the container with the same name, throw an
     *  exception.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there already is an entity
     *   in the container with the same name.
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        if (name == null) {
            name = "";
        }

        CompositeEntity container = (CompositeEntity) getContainer();

        if (container != null) {
            ComponentEntity another = container.getEntity(name);

            if (another != null && another != this) {
                throw new NameDuplicationException(container,
                        "Name duplication: " + name);
            }
        }

        super.setName(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
    protected void _adjustDeferrals() throws IllegalActionException {
        // Use the class name.
        String className = getClassName();

        // Search upwards in the hierarchy.
        NamedObj context = this;
        int levelsToSearch = getDerivedLevel();
        int aboveLevel = 0;
        ComponentEntity candidate = null;

        // Make sure we get a real candidate, which is a
        // class definition. The second term in the if will
        // cause the search to continue up the hierarchy.
        // NOTE: There is still an oddness, in that
        // the class scoping results in a subtle (and
        // maybe incomprehensible) identification of
        // the base class, particularly when pasting
        // an instance or subclass into a new context.
        while (aboveLevel < levelsToSearch
                && (candidate == null || !candidate.isClassDefinition())
                && context != null) {
            context = context.getContainer();

            if (context instanceof CompositeEntity) {
                candidate = ((CompositeEntity) context).getEntity(className);
            }

            aboveLevel += 1;
        }

        if (candidate != null) {
            _setParent(candidate);
            _markContentsDerived(0);

            // For every object contained by the new parent,
            // we need to make sure its value is propagated
            // to this new child and that the override field
            // gets set to reflect that.
            candidate.propagateValues();
        }
    }

    /** Check the specified container.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not an
     *   instance of CompositeEntity, or if the proposed container is
     *   null and there are other objects that defer their definitions
     *   to this one.
     */
    protected void _checkContainer(InstantiableNamedObj container)
            throws IllegalActionException {
        if (container != null && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
                    "Component entity can only be contained by "
                            + "a CompositeEntity");
        }

        // NOTE: If we don't enforce this, then undo could fail,
        // since deletions occur in the opposite order of the re-additions
        // in undo.  So being silent about this error will not do.
        if (container == null) {
            // If the class has objects that defer to it, then
            // refuse to delete.
            boolean hasDeferrals = false;
            List deferred = getChildren();
            StringBuffer names = new StringBuffer();

            if (deferred != null) {
                // List contains weak references, so it's not
                // sufficient to just check the length.
                Iterator deferrers = deferred.iterator();

                while (deferrers.hasNext()) {
                    WeakReference deferrer = (WeakReference) deferrers.next();
                    NamedObj deferrerObject = (NamedObj) deferrer.get();

                    if (deferrerObject != null) {
                        hasDeferrals = true;

                        if (names.length() > 0) {
                            names.append(", ");
                        }

                        names.append(deferrerObject.getFullName());
                    }
                }
            }

            if (hasDeferrals) {
                throw new IllegalActionException(this, "Cannot delete because "
                        + "there are instances and/or subclasses:\n"
                        + names.toString());
            }
        }
    }

    /** Get an entity with the specified name in the specified container.
     *  The returned object is assured of being an
     *  instance of the same class as this object.
     *  @param relativeName The name relative to the container.
     *  @param container The container expected to contain the object, which
     *   must be an instance of CompositeEntity.
     *  @return An object of the same class as this object, or null if there
     *   is none.
     *  @exception IllegalActionException If the object exists
     *   and has the wrong class, or if the specified container is not
     *   an instance of CompositeEntity.
     */
    @Override
    protected NamedObj _getContainedObject(NamedObj container,
            String relativeName) throws IllegalActionException {
        if (!(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, "Expected "
                    + container.getFullName()
                    + " to be an instance of ptolemy.kernel.CompositeEntity,"
                    + " but it is " + container.getClass().getName());
        }

        ComponentEntity candidate = ((CompositeEntity) container)
                .getEntity(relativeName);

        if (candidate != null && !getClass().isInstance(candidate)) {
            throw new IllegalActionException(this, "Expected "
                    + candidate.getFullName() + " to be an instance of "
                    + getClass().getName() + ", but it is "
                    + candidate.getClass().getName());
        }

        return candidate;
    }

    /** Propagate existence of this object to the
     *  specified object. This overrides the base class
     *  to set the container.
     *  @param container Object to contain the new object.
     *  @exception IllegalActionException If the object
     *   cannot be cloned.
     *  @return A new object of the same class and name
     *   as this one.
     */
    @Override
    protected NamedObj _propagateExistence(NamedObj container)
            throws IllegalActionException {
        try {
            ComponentEntity newObject = (ComponentEntity) super
                    ._propagateExistence(container);
            // FindBugs warns that the cast of container is
            // unchecked.
            if (!(container instanceof CompositeEntity)) {
                throw new InternalErrorException(container
                        + " is not a CompositeEntity.");
            } else {
                newObject.setContainer((CompositeEntity) container);
            }
            return newObject;
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Define the icon. */
    private void _addIcon() {
        _attachText("_iconDescription", _defaultIcon);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The container. */
    private CompositeEntity _container;

    /** The default value icon.  This is static so that we avoid doing
     *  string concatenation each time we construct this object.
     */
    private static String _defaultIcon = "<svg>\n"
            + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
            + "height=\"40\" style=\"fill:white\"/>\n"
            + "<polygon points=\"-20,-10 20,0 -20,10\" "
            + "style=\"fill:blue\"/>\n" + "</svg>\n";
}
