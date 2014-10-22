/* A base class for attributes to be attached to instances of NamedObj.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.kernel.util;

import java.util.Iterator;

///////////////////////////////////////////////////////////////////
//// Attribute

/**
 Attribute is a base class for attributes to be attached to instances
 of NamedObj.  This base class is itself a NamedObj, with the only
 extension being that it can have a container.  The setContainer()
 method puts this object on the list of attributes of the container.

 @author Edward A. Lee, Neil Smyth
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (johnr)
 */
public class Attribute extends NamedObj {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public Attribute() {
        super();
        _elementName = "property";
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public Attribute(Workspace workspace) {
        super(workspace);
        _elementName = "property";
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Attribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, true);
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param incrementWorkspaceVersion False to not add this to the workspace
     *   or do anything else that might change the workspace version number.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    protected Attribute(NamedObj container, String name,
            boolean incrementWorkspaceVersion) throws IllegalActionException,
            NameDuplicationException {
        super(container.workspace(), name, incrementWorkspaceVersion);
        if (incrementWorkspaceVersion) {
            setContainer(container);
        } else {
            // Get writeAccess to the workspace because we are updating
            // the _attributes field of the container.
            // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3255
            try {
                _workspace.getWriteAccess();

                // Avoid methods that increment the workspace version.
                if (container._attributes == null) {
                    container._attributes = new NamedList();
                }
                container._attributes.append(this);

                _container = container;
                // Make sure even the debugging messages are unchanged.
                if (container._debugging) {
                    container._debug("Added attribute", getName(), "to",
                            container.getFullName());
                }
            } finally {
                // Note that _doneTemporaryWriting
                // does not increment the workspace version.
                _workspace.doneTemporaryWriting();
            }
        }
        _elementName = "property";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Attribute newObject = (Attribute) super.clone(workspace);
        newObject._container = null;
        return newObject;
    }

    /** Move this object down by one in the list of attributes of
     *  its container. If this object is already last, do nothing.
     *  This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveDown() throws IllegalActionException {
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._attributes.moveDown(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = derived.getContainer();
                container._attributes.moveDown(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the first position in the list
     *  of attributes of the container. If this object is already first,
     *  do nothing. This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToFirst() throws IllegalActionException {
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._attributes.moveToFirst(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = derived.getContainer();
                container._attributes.moveToFirst(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the specified position in the list
     *  of attributes of the container. If this object is already at
     *  the specified position, do  nothing. This method gets write
     *  access on workspace and increments the version.
     *  @param index The zero based position to which this object is moved.
     *  0 means the first position, 1 means the second position.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container or if the index is out of bounds.
     */
    @Override
    public int moveToIndex(int index) throws IllegalActionException {
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._attributes.moveToIndex(this, index);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = derived.getContainer();
                container._attributes.moveToIndex(derived, index);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the last position in the list
     *  of attributes of the container.  If this object is already last,
     *  do nothing. This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToLast() throws IllegalActionException {
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._attributes.moveToLast(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = derived.getContainer();
                container._attributes.moveToLast(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object up by one in the list of
     *  attributes of the container. If this object is already first, do
     *  nothing. This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveUp() throws IllegalActionException {
        NamedObj container = getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container.");
        }

        try {
            _workspace.getWriteAccess();

            int result = container._attributes.moveUp(this);

            // Propagate.
            Iterator derivedObjects = getDerivedList().iterator();

            while (derivedObjects.hasNext()) {
                NamedObj derived = (NamedObj) derivedObjects.next();
                container = derived.getContainer();
                container._attributes.moveUp(derived);
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Get the NamedObj that this Attribute is attached to.
     *  @return The container, an instance of NamedObj.
     *  @see #setContainer(NamedObj)
     */
    @Override
    public NamedObj getContainer() {
        return _container;
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  If the container already
     *  contains an attribute with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this object being garbage collected.
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  <p>
     *  Subclasses may constrain the type of container by overriding
     *  {@link #setContainer(NamedObj)}.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if (container != null && _workspace != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
        _checkContainer(container);
        if (deepContains(container)) {
            throw new IllegalActionException(this, container,
                    "Attempt to construct recursive containment "
                            + "of attributes");
        }

        NamedObj previousContainer = getContainer();

        if (previousContainer == container) {
            return;
        }

        _notifyHierarchyListenersBeforeChange();

        try {
            _workspace.getWriteAccess();

            // Do this first, because it may throw an exception.
            if (container != null) {
                container._addAttribute(this);

                if (previousContainer == null) {
                    _workspace.remove(this);
                }

                // We have successfully set a new container for this
                // object. Mark it modified to ensure MoML export.
                // FIXME: Inappropriate?
                // setOverrideDepth(0);
            }

            _container = container;

            if (previousContainer != null) {
                previousContainer._removeAttribute(this);
            }

            if (container != null) {
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

    /** Set the name of the attribute. If there is already an attribute
     *  of the container with the same name, then throw a
     *  NameDuplicationException.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException If there is already an
     *       attribute with the same name in the container.
     */
    @Override
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        if (name == null) {
            name = "";
        }

        NamedObj container = getContainer();

        if (container != null) {
            Attribute another = container.getAttribute(name);

            if (another != null && another != this) {
                throw new NameDuplicationException(container,
                        "Name duplication: " + name);
            }
        }

        super.setName(name);
    }

    /** Update the content of this attribute.
     *  In this base class, nothing is performed.
     *  Subclasses need to override this class to update the attribute.
     *  @exception InternalErrorException Not thrown in this base class.
     */
    public void updateContent() throws InternalErrorException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this attribute.  In this base class, this method returns immediately
     *  without doing anything.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.  Not thrown in this base class.
     */
    protected void _checkContainer(NamedObj container)
            throws IllegalActionException {
    }

    /** Get an attribute with the specified name in the specified container.
     *  The type of object sought is an instance of the same class as
     *  this object.  The returned object is assured of being an
     *  instance of the same class as this object.
     *  @param relativeName The name relative to the container.
     *  @param container The container expected to contain the object.
     *  @return An object of the same class as this object, or null
     *   if there is none.
     *  @exception IllegalActionException If the object exists
     *   and has the wrong class.
     */
    @Override
    protected NamedObj _getContainedObject(NamedObj container,
            String relativeName) throws IllegalActionException {
        Attribute candidate = container.getAttribute(relativeName);

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
            Attribute newObject = (Attribute) super
                    ._propagateExistence(container);
            newObject.setContainer(container);
            return newObject;
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial Container of this attribute. */
    private NamedObj _container;
}
