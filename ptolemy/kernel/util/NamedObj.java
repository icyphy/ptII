/* Base class for objects with a name and a container.

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
package ptolemy.kernel.util;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import ptolemy.kernel.CompositeEntity;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// NamedObj

/**
 This is a base class for almost all Ptolemy II objects.
 <p>
 This class supports a naming scheme, change requests, a persistent
 file format (MoML), a mutual exclusion mechanism for models (the
 workspace), an error handler, and a hierarchical class mechanism
 with inheritance.
 <p>
 An instance of this class can also be parameterized by attaching
 instances of the Attribute class.
 Instances of Attribute can be attached by calling their setContainer()
 method and passing this object as an argument. Those instances will
 then be reported by the {@link #getAttribute(String)},
 {@link #getAttribute(String, Class)}, {@link #attributeList()}
 and {@link #attributeList(Class)} methods.
 Classes derived from NamedObj may constrain attributes to be a
 subclass of Attribute.  To do that, they should override the protected
 _addAttribute(Attribute) method to throw an exception if
 the object provided is not of the right class.
 <p>
 An instance of this class has a name.
 A name is an arbitrary string with no periods.  If no
 name is provided, the name is taken to be an empty string (not a null
 reference). An instance also has a full name, which is a concatenation
 of the container's full name and the simple name, separated by a
 period. If there is no container, then the full name begins with a
 period. The full name is used for error reporting throughout Ptolemy
 II.
 <p>
 Instances of this class are associated with a workspace, specified as
 a constructor argument.  The reference to the workspace is immutable.
 It cannot be changed during the lifetime of this object.  It is used for
 synchronization of methods that depend on or modify the state of
 objects within it. If no workspace is specified, then the default
 workspace is used.  Note that the workspace should not be confused
 with the container.  The workspace never serves as a container.
 <p>
 In this base class, the container is null by default, and no
 method is provided to change it. Derived classes that support
 hierarchy provide one or more methods that set the container.
 By convention, if the container is set,
 then the instance should be removed from the workspace directory, if
 it is present.  The workspace directory is expected to list only
 top-level objects in a hierarchy.  The NamedObj can still use the
 workspace for synchronization.  Any object contained by another uses
 the workspace of its container as its own workspace by default.
 <p>
 This class supports <i>change requests</i> or <i>mutations</i>,
 which are changes to a model that are performed in a disciplined
 fashion.  In particular, a mutation can be requested via the
 {@link #requestChange(ChangeRequest)} method. By default, when
 a change is requested, the change is executed immediately.
 However, by calling {@link #setDeferringChangeRequests(boolean)},
 you can ensure that change requests are queued to be executed
 only when it is safe to execute them.
 <p>
 This class supports the notion of a <i>model error</i>, which is
 an exception that is handled by a registered model error handler, or
 passed up the container hierarchy if there is no registered model
 error handler.  This mechanism complements the exception mechanism in
 Java. Instead of unraveling the calling stack to handle exceptions,
 this mechanism passes control up the Ptolemy II hierarchy.
 <p>
 Derived classes should override the _description() method to
 append new fields if there is new information that should be included
 in the description.
 <p>
 A NamedObj can contain instances of {@link DecoratorAttributes}. These are attributes that are
 added by another NamedObj that implements the {@link Decorator} interface.
 These attributes are stored separately and can be retrieved by using
 {@link #getDecoratorAttributes(Decorator)} or
 {@link #getDecoratorAttribute(Decorator, String)}.

 @author Mudit Goel, Edward A. Lee, Neil Smyth, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)

 @see Attribute
 @see Workspace
 */
public class NamedObj implements Changeable, Cloneable, Debuggable,
DebugListener, Derivable, MoMLExportable, ModelErrorHandler, Moveable {
    // This class used to implement Serializable, but the implementation was never
    // complete and thus cause many warnings.

    // Note that Nameable extends ModelErrorHandler, so this class
    // need not declare that it directly implements ModelErrorHandler.

    /** Construct an object in the default workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NamedObj() {
        this((Workspace) null);
    }

    /** Construct an object in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public NamedObj(String name) throws IllegalActionException {
        this(_DEFAULT_WORKSPACE, name);
    }

    /** Construct an object in the specified workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking
     */
    public NamedObj(Workspace workspace) {
        // NOTE: Can't call the constructor below, which has essentially
        // the same code, without also spuriously throwing
        // IllegalActionException.
        if (workspace == null) {
            workspace = _DEFAULT_WORKSPACE;
        }

        _workspace = workspace;

        // Exception cannot occur, so we ignore. The object does not
        // have a container, and is not already on the workspace list.
        // NOTE: This does not need to be write-synchronized on the workspace
        // because the only side effect is adding to the directory,
        // and methods for adding and reading from the directory are
        // synchronized.
        try {
            workspace.add(this);
        } catch (IllegalActionException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(null, ex,
                    "Internal error in NamedObj constructor!");
        }

        try {
            setName("");
        } catch (KernelException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(null, ex,
                    "Internal error in NamedObj constructor!");
        }
    }

    /** Construct an object in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public NamedObj(Workspace workspace, String name)
            throws IllegalActionException {
        this(workspace, name, true);
    }

    /** Construct an object in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this object.
     *  @param incrementWorkspaceVersion False to not add this to the workspace
     *   or do anything else that might change the workspace version number.
     *  @exception IllegalActionException If the name has a period.
     */
    protected NamedObj(Workspace workspace, String name,
            boolean incrementWorkspaceVersion) throws IllegalActionException {
        if (workspace == null) {
            workspace = _DEFAULT_WORKSPACE;
        }

        _workspace = workspace;

        // Exception cannot occur, so we ignore. The object does not
        // have a container, and is not already on the workspace list.
        // NOTE: This does not need to be write-synchronized on the workspace
        // because the only side effect is adding to the directory,
        // and methods for adding and reading from the directory are
        // synchronized.
        if (incrementWorkspaceVersion) {
            try {
                workspace.add(this);
                setName(name);
            } catch (NameDuplicationException ex) {
                // This exception should not be thrown.
                throw new InternalErrorException(null, ex,
                        "Internal error in NamedObj constructor!");
            }
        } else {
            _name = name;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a change listener.  If there is a container, then
     *  delegate to the container.  Otherwise, add the listener
     *  to the list of change listeners in this object. Each listener
     *  will be notified of the execution (or failure) of each
     *  change request that is executed via the requestChange() method.
     *  Note that in this implementation, only the top level of a
     *  hierarchy executes changes, which is why this method delegates
     *  to the container if there is one.
     *  <p>
     *  If the listener is already in the list, remove the previous
     *  instance and add it again in the first position.
     *  This listener is also notified before
     *  other listeners that have been previously registered with the
     *  top-level object.
     *  @param listener The listener to add.
     *  @see #removeChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see Changeable
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        NamedObj container = getContainer();

        if (container != null) {
            container.addChangeListener(listener);
        } else {
            synchronized (_changeLock) {
                if (_changeListeners == null) {
                    _changeListeners = new LinkedList<WeakReference<ChangeListener>>();
                } else {
                    // In case there is a previous instance, remove it.
                    removeChangeListener(listener);
                }

                _changeListeners.add(0, new WeakReference(listener));
            }
        }
    }

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     *  @see #removeDebugListener(DebugListener)
     */
    @Override
    public void addDebugListener(DebugListener listener) {
        // NOTE: This method needs to be synchronized to prevent two
        // threads from each creating a new _debugListeners list.
        synchronized (this) {
            if (_debugListeners == null) {
                _debugListeners = new LinkedList();
            }
        }

        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized (_debugListeners) {
            if (_debugListeners.contains(listener)) {
                return;
            } else {
                _debugListeners.add(listener);
            }

            _debugging = true;
        }
    }

    /** Add a hierarchy listener. If the listener is already
     *  added, do nothing. This will cause the object to also
     *  be added as a hierarchy listener in the container of
     *  this object, if there is one, and in its container,
     *  up to the top of the hierarchy.
     *  @param listener The listener to add.
     *  @see #removeHierarchyListener(HierarchyListener)
     */
    public void addHierarchyListener(HierarchyListener listener) {
        if (_hierarchyListeners == null) {
            _hierarchyListeners = new HashSet<HierarchyListener>();
        }
        _hierarchyListeners.add(listener);

        // Add to the container.
        NamedObj container = getContainer();
        if (container != null) {
            container.addHierarchyListener(listener);
        }
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.  In derived classes, this method may
     *  throw an exception, indicating that the new attribute value
     *  is invalid.  It is up to the caller to restore the attribute
     *  to a valid value if an exception is thrown.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
    }

    /** React to the deletion of an attribute. This method is called
     *  by a contained attributed when it is deleted. In this base class,
     *  the method does nothing. In derived classes, this method may deal
     *  with consequences of deletion, for instance, update local variables.
     *  @param attribute The attribute that was deleted.
     *  @exception IllegalActionException If the deletion is not acceptable
     *    to this container (not thrown in this base class).
     */
    public void attributeDeleted(Attribute attribute)
            throws IllegalActionException {
    }

    /** Return a list of the attributes contained by this object.
     *  If there are no attributes, return an empty list.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of instances of Attribute.
     */
    public List attributeList() {
        try {
            _workspace.getReadAccess();

            if (_attributes == null) {
                _attributes = new NamedList();
            }

            return _attributes.elementList();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of the attributes contained by this object that
     *  are instances of the specified class.  If there are no such
     *  instances, then return an empty list.
     *  This method is read-synchronized on the workspace.
     *  @param filter The class of attribute of interest.
     *  @param <T> The type of that class.
     *  @return A list of instances of specified class.
     */
    public <T> List<T> attributeList(Class<T> filter) {
        try {
            _workspace.getReadAccess();

            if (_attributes == null) {
                _attributes = new NamedList();
            }

            List<T> result = new LinkedList<T>();
            Iterator<?> attributes = _attributes.elementList().iterator();

            while (attributes.hasNext()) {
                Object attribute = attributes.next();

                if (filter.isInstance(attribute)) {
                    @SuppressWarnings("unchecked")
                    T tAttribute = (T) attribute;
                    result.add(tAttribute);
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** React to a change in the type of an attribute.  This method is
     *  called by a contained attribute when its type changes.
     *  In this base class, the method does nothing.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
    }

    /** Clone the object into the current workspace by calling the clone()
     *  method that takes a Workspace argument.
     *  This method read-synchronizes on the workspace.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        // FindBugs warns that "clone method does not call super.clone()",
        // but that can be ignored because clone(Workspace) calls
        // super.clone().
        return clone(_workspace);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there). This uses the clone() method of
     *  java.lang.Object, which makes a field-by-field copy.
     *  It then adjusts the workspace reference and clones the
     *  attributes on the attribute list, if there is one.  The attributes
     *  are set to the attributes of the new object.
     *  The new object will be set to defer change requests, so change
     *  requests can be safely issued during cloning. However, it is
     *  up to the caller of this clone() method to then execute the
     *  the change requests, or to call setDeferringChangeRequests(false).
     *  This method read-synchronizes on the workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     *  @see #setDeferringChangeRequests(boolean)
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        // NOTE: It is safe to clone an object into a different
        // workspace. It is not safe to move an object to a new
        // workspace, by contrast. The reason this is safe is that
        // after the this method has been run, there
        // are no references in the clone to objects in the old
        // workspace. Moreover, no object in the old workspace can have
        // a reference to the cloned object because we have only just
        // created it and have not returned the reference.
        try {
            _workspace.getReadAccess();

            NamedObj newObject = (NamedObj) super.clone();

            newObject._hierarchyListeners = null;

            newObject._changeLock = new SerializableObject();

            // The clone should have its own listeners, otherwise
            // debug messages from the clone will go to the master.
            // See 8.1.0 in NamedObj.tcl. Credit: Colin Endicott
            newObject._debugListeners = null;

            // During the cloning process, change requests might
            // be issued (e.g. in an actor's _addEntity() method).
            // Execution of these change requests need to be deferred
            // until after cloning is complete.  To ensure that,
            // we set the following.  Note that when the container
            // of an object being cloned is set, any queued change
            // requests will be delegated to the new container, and
            // the value of this private variable will no longer
            // have any effect.
            newObject._deferChangeRequests = true;

            // NOTE: It is not necessary to write-synchronize on the other
            // workspace because this only affects its directory, and methods
            // to access the directory are synchronized.
            newObject._attributes = null;

            newObject._decoratorAttributes = new HashMap<Decorator, DecoratorAttributes>();
            newObject._decoratorAttributesVersion = -1L;

            // NOTE: As of version 5.0, clones inherit the derived
            // level of the object from which they are cloned.
            // This is somewhat risky, but since cloning is usually
            // used for instantiation, and instantiation fixes up
            // the derived level, this creates no problems there.
            // In the rare cases when clone is actually used directly
            // (mainly in tests), it is appropriate for the clone
            // to be indentical in this regard to the object from
            // which it is cloned.  EAL 3/3/05
            // newObject._derivedLevel = Integer.MAX_VALUE;
            if (workspace == null) {
                newObject._workspace = _DEFAULT_WORKSPACE;
            } else {
                newObject._workspace = workspace;
            }

            newObject._fullNameVersion = -1;

            if (_attributes != null) {
                Iterator<?> parameters = _attributes.elementList().iterator();

                while (parameters.hasNext()) {
                    Attribute parameter = (Attribute) parameters.next();
                    Attribute newParameter = (Attribute) parameter
                            .clone(workspace);

                    try {
                        newParameter.setContainer(newObject);
                    } catch (KernelException exception) {
                        throw new CloneNotSupportedException(
                                "Failed to clone attribute "
                                        + parameter.getFullName() + ": "
                                        + exception);
                    }
                }
            }

            if (_debugging) {
                if (workspace == null) {
                    _debug("Cloned", getFullName(), "into default workspace.");
                } else {
                    _debug("Cloned", getFullName(), "into workspace:",
                            workspace.getFullName());
                }
            }

            newObject._elementName = _elementName;
            newObject._source = _source;

            // NOTE: It's not clear that this is the right thing to do
            // here, having the same override properties as the original
            // seems reasonable, so we leave this be. However, it is essential
            // to clone the list in case it is later modified in the source
            // of the clone.
            if (_override != null) {
                newObject._override = new LinkedList<Integer>(_override);
            }

            // NOTE: The value for the classname and superclass isn't
            // correct if this cloning operation is meant to create
            // an extension rather than a clone.  A clone has exactly
            // the same className and superclass as the master.
            // It is up to the caller to correct these fields if
            // that is the case.  It cannot be done here because
            // we don't know the name of the new class.
            newObject.setClassName(getClassName());

            _cloneFixAttributeFields(newObject);

            return newObject;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return an iterator over contained objects. In this base class,
     *  this is simply an iterator over attributes.  In derived classes,
     *  the iterator will also traverse ports, entities, classes,
     *  and relations.
     *  @return An iterator over instances of NamedObj contained by this
     *   object.
     */
    public Iterator containedObjectsIterator() {
        return new ContainedObjectsIterator();
    }

    /** Return the set of decorators that decorate this object.
     *  @see Decorator
     *  @return The decorators that decorate this object (which may
     *   be an empty set).
     * @exception IllegalActionException If a decorator referenced
     *  by a DecoratorAttributes cannot be found.
     */
    public Set<Decorator> decorators() throws IllegalActionException {
        synchronized (_decoratorAttributes) {
            _updateDecoratorAttributes();
            return _decoratorAttributes.keySet();
        }
    }

    /** Return true if this object contains the specified object,
     *  directly or indirectly.  That is, return true if the specified
     *  object is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     *  This method ignores whether the entities report that they are
     *  atomic (see CompositeEntity), and always returns false if the entities
     *  are not in the same workspace.
     *  This method is read-synchronized on the workspace.
     *  @param inside The object to check for inside this object.
     *  @return True if this contains the argument, directly or indirectly.
     */
    public boolean deepContains(NamedObj inside) {
        try {
            _workspace.getReadAccess();

            // Start with the inside and check its containers in sequence.
            if (inside != null) {
                if (_workspace != inside._workspace) {
                    return false;
                }

                Nameable container = inside.getContainer();

                while (container != null) {
                    if (container == this) {
                        return true;
                    }

                    container = container.getContainer();
                }
            }

            return false;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the depth in the hierarchy of this object. If this object
     *  has no container, then return 0.  If its container has no container,
     *  then return 1.  Etc.
     *  @return The depth in the hierarchy of this object.
     */
    public int depthInHierarchy() {
        int result = 0;
        Nameable container = getContainer();

        while (container != null) {
            result++;
            container = container.getContainer();
        }

        return result;
    }

    /** Return a full description of the object. This is accomplished
     *  by calling the description method with an argument for full detail.
     *  This method read-synchronizes on the workspace.
     *  @return A description of the object.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but derived classes could throw an exception if there is a problem
     *  accessing subcomponents of this object.
     *  @see #exportMoML(Writer, int, String)
     */
    @Override
    public String description() throws IllegalActionException {
        return description(COMPLETE);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in this class (NamedObj).  This method returns an empty
     *  string (not null) if there is nothing to report.
     *  It read-synchronizes on the workspace.
     *  @param detail The level of detail.
     *  @return A description of the object.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but derived classes could throw an exception if there is a problem
     *  accessing subcomponents of this object.
     *  @see #exportMoML(Writer, int, String)
     */
    public String description(int detail) throws IllegalActionException {
        return _description(detail, 0, 0);
    }

    /** React to the given debug event by relaying to any registered
     *  debug listeners.
     *  @param event The event.
     *  @since Ptolemy II 2.3
     */
    @Override
    public void event(DebugEvent event) {
        if (_debugging) {
            _debug(event);
        }
    }

    /** Execute previously requested changes. If there is a container, then
     *  delegate the request to the container.  Otherwise, this method will
     *  execute all pending changes (even if
     *  {@link #isDeferringChangeRequests()} returns true.
     *  Listeners will be notified of success or failure.
     *  @see #addChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see #isDeferringChangeRequests()
     *  @see Changeable
     */
    @Override
    public void executeChangeRequests() {
        NamedObj container = getContainer();

        if (container != null) {
            container.executeChangeRequests();
            return;
        }

        // Have to execute a copy of the change request list
        // because the list may be modified during execution.
        List<ChangeRequest> copy = _copyChangeRequestList();

        if (copy != null) {
            _executeChangeRequests(copy);
            // Change requests may have been queued during the execute.
            // Execute those by a recursive call.
            executeChangeRequests();
        }
    }

    /** Get a MoML description of this object.  This might be an empty string
     *  if there is no MoML description of this object or if this object is
     *  not persistent or if this object is a derived object.  This uses the
     *  three-argument version of this method.  It is final to ensure that
     *  derived classes only need to override that method to change
     *  the MoML description.
     *  @return A MoML description, or an empty string if there is none.
     *  @see MoMLExportable
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #getDerivedLevel()
     */
    @Override
    public final String exportMoML() {
        try {
            StringWriter buffer = new StringWriter();
            exportMoML(buffer, 0);
            return buffer.toString();
        } catch (IOException ex) {
            // This should not occur.
            throw new InternalErrorException(this, ex, null);
        }
    }

    /** Get a MoML description of this object with its name replaced by
     *  the specified name.  The description might be an empty string
     *  if there is no MoML description of this object or if this object
     *  is not persistent, or this object a derived object.  This uses the
     *  three-argument version of this method.  It is final to ensure that
     *  derived classes only override that method to change
     *  the MoML description.
     *  @param name The name of we use when exporting the description.
     *  @return A MoML description, or the empty string if there is none.
     *  @see MoMLExportable
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #getDerivedLevel()
     */
    @Override
    public final String exportMoML(String name) {
        try {
            StringWriter buffer = new StringWriter();
            exportMoML(buffer, 0, name);
            return buffer.toString();
        } catch (IOException ex) {
            // This should not occur.
            throw new InternalErrorException(this, ex, null);
        }
    }

    /** Write a MoML description of this object using the specified
     *  Writer.  If there is no MoML description, or if the object
     *  is not persistent, or if this object is a derived object,
     *  then nothing is written. To write to standard out, do
     *  <pre>
     *      exportMoML(new OutputStreamWriter(System.out))
     *  </pre>
     *  This method uses the three-argument
     *  version of this method.  It is final to ensure that
     *  derived classes only need to override that method to change
     *  the MoML description.
     *  @param output The stream to write to.
     *  @exception IOException If an I/O error occurs.
     *  @see MoMLExportable
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #getDerivedLevel()
     */
    @Override
    public final void exportMoML(Writer output) throws IOException {
        exportMoML(output, 0);
    }

    /** Write a MoML description of this entity with the specified
     *  indentation depth.  This calls the three-argument version of
     *  this method with getName() as the third argument.
     *  This method is final to ensure that
     *  derived classes only override the three-argument method to change
     *  the MoML description.
     *  If the object is not persistent, or if there is no MoML description,
     *  or if this object is a class instance, then write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see MoMLExportable
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #getDerivedLevel()
     */
    @Override
    public final void exportMoML(Writer output, int depth) throws IOException {
        exportMoML(output, depth, getName());
    }

    /** Write a MoML description of this object with the specified
     *  indentation depth and with the specified name substituting
     *  for the name of this object.  The class name is determined
     *  by {@link #getClassName()}, the source is determined by
     *  {@link #getSource()}. The description has the form:
     *  <pre>
     *      &lt;<i>element</i> name="<i>name</i>" class="<i>classname</i>" source="<i>source</i>"&gt;&gt;
     *          <i>body, determined by _exportMoMLContents()</i>
     *      &lt;/<i>element</i>&gt;
     *  </pre>
     *  By default, the element name is "entity."  The default class name
     *  is the Java classname of this instance.
     *  The source attribute is by default left off altogether.
     *  <p>
     *  If this object has no container and the depth argument is zero,
     *  then this method prepends XML file header information, which is:
     *  <pre>
     *  &lt;?xml version="1.0" standalone="no"?&gt;
     *  &lt;!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
     *      "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
     *  </pre>
     *  In the above, "entity" may be replaced by "property" or "port"
     *  if somehow a top-level property or port is exported.
     *  <p>
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.
     *  Derived classes can override this method to change the MoML
     *  description of an object.  They can override the protected
     *  method _exportMoMLContents() if they need to only change which
     *  contents are described.
     *  <p>
     *  If this object is not persistent, or if there is no MoML
     *  description of this object, or if this object is a class
     *  instance, then write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     *  @see MoMLExportable
     *  @see #clone(Workspace)
     *  @see #isPersistent()
     *  @see #getDerivedLevel()
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {

        // Escape any < character in name. unescapeForXML occurs in
        // setName(String).
        name = StringUtilities.escapeForXML(name);

        try {
            _workspace.getReadAccess();

            // If the object is not persistent, or the MoML is
            // redundant with what would be propagated, then do
            // not generate any MoML.
            if (_isMoMLSuppressed(depth)) {
                return;
            }

            String className = getClassName();

            if (depth == 0 && getContainer() == null) {
                // No container, and this is a top level moml element.
                // Generate header information.
                // NOTE: Used to generate this only if the top-level
                // was an entity, with the following test:
                // if (_elementName.equals("entity")) {}
                // However, this meant that when saving icons,
                // they would not have the header information,
                // and when opened, would open as a text file
                // instead of in the icon editor.
                output.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + _elementName + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");
            }

            output.write(_getIndentPrefix(depth) + "<" + _elementName
                    + " name=\"" + name + "\" class=\"" + className + "\"");

            if (getSource() != null) {
                output.write(" source=\"" + getSource() + "\">\n");
            } else {
                output.write(">\n");
            }

            // Callers of _exportMoMLContents() should hold a read lock
            // so as to avoid ConcurrentModificationExceptions
            _exportMoMLContents(output, depth + 1);

            // Write the close of the element.
            output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get a MoML description of this object without any XML headers.
     *  This differs significantly from exportMoML() only if this
     *  object has no container, because if it has a container, then
     *  it will not export MoML headers anyway.
     *  @return A MoML description, or the empty string if there is none.
     *  @see #exportMoML()
     */
    public final String exportMoMLPlain() {
        try {
            StringWriter buffer = new StringWriter();
            // Using a depth of 1 suppresses the XML header.
            // It also, unfortunately, indents the result.
            // But I guess this is harmless.
            exportMoML(buffer, 1, getName());
            return buffer.toString();
        } catch (IOException ex) {
            // This should not occur.
            throw new InternalErrorException(this, ex, null);
        }
    }

    /** Get the attribute with the given name. The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is contained by a (deeply) contained attribute.
     *  If the given name is null, then an InternalErrorException is thrown.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    public Attribute getAttribute(String name) {
        try {
            _workspace.getReadAccess();

            if (_attributes == null) {
                // No attribute has been added to this NamedObj yet.
                return null;
            } else {
                if (name == null) {
                    // If MoMLParser has problems, we may end up here,
                    // so rather than having _splitName() throw a
                    // NullPointerException, we do the check here and
                    // include 'this' so that we know where the problem
                    // is occurring.
                    throw new InternalErrorException(this, null,
                            "This should not be happening: getAttribute() "
                                    + "was called with a null name");
                }

                // This method gets called often,
                // so avoid the call to _splitName().
                // This change is good for a 2-3% speed up
                // in ptesdf mini-model-aggregator.
                // Below is the old code:

                // String[] subnames = _splitName(name);
                // if (subnames[1] == null) {
                //   return (Attribute) _attributes.get(name);
                // else {
                //   Attribute match = (Attribute) _attributes.get(subnames[0]);

                //   if (match == null) {
                //       return null;
                //   } else {
                //       return match.getAttribute(subnames[1]);
                //   }
                // }

                final int period = name.indexOf(".");

                if (period < 0) {
                    return (Attribute) _attributes.get(name);
                } else {
                    final Attribute match = (Attribute) _attributes.get(name
                            .substring(0, period));
                    if (match == null) {
                        return null;
                    } else {
                        return match.getAttribute(name.substring(period + 1));
                    }
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the attribute with the given name and class. If an attribute
     *  is found that has the specified name, but the class does not match,
     *  then throw an IllegalActionException.  The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is contained by a (deeply) contained attribute.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @param attributeClass The class of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     *  @exception IllegalActionException If an attribute is found with
     *   the specified name that is not an instance of the specified class.
     */
    public Attribute getAttribute(String name, Class attributeClass)
            throws IllegalActionException {
        Attribute attribute = getAttribute(name);

        if (attribute != null) {
            if (!attributeClass.isInstance(attribute)) {
                throw new IllegalActionException(attribute,
                        "Expected attribute of class "
                                + attributeClass.getName()
                                + " but got attribute of class "
                                + attribute.getClass().getName());
            }
        }

        return attribute;
    }

    /** Return an enumeration of the attributes attached to this object.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use attributeList() instead.
     *  @return An enumeration of instances of Attribute.
     */
    @Deprecated
    public Enumeration getAttributes() {
        return Collections.enumeration(attributeList());
    }

    /** Return a list of weak references to change listeners,
     *  or null if there is none.
     *  @return A list of weak references to change listeners,
     *   or null if there is none.
     */
    public List getChangeListeners() {
        return _changeListeners;
    }

    /** Return the MoML class name.  This is either the
     *  class of which this object is an instance, or if this
     *  object is itself a class, then the class that it extends.
     *  By default, it will be the Java class name of this object.
     *  This method never returns null.
     *  @return The MoML class name.
     *  @see MoMLExportable
     *  @see #setClassName(String)
     */
    @Override
    public String getClassName() {
        if (_className == null) {
            _className = getClass().getName();
        }

        return _className;
    }

    /** Get the container.  Always return null in this base class.
     *  A null returned value should be interpreted as indicating
     *  that there is no container.
     *  @return null.
     */
    @Override
    public NamedObj getContainer() {
        return null;
    }

    /** Return the decorator attribute with the specified name for the
     *  specified decorator, or null the specified decorator provides
     *  no attribute with the specified name or the decorator does not
     *  decorate this object. This method is normally called by the
     *  decorator itself to retrieve its decorated parameter values for
     *  this NamedObj.
     *  If this object has no decorator attributes, then calling
     *  this method will cause them to be created and assigned default values,
     *  if the specified decorator decorates this object.
     *  @see ptolemy.kernel.util.Decorator#createDecoratorAttributes(NamedObj)
     *  @see #getDecoratorAttributes(Decorator)
     *  @param decorator The decorator.
     *  @param name The name of the attribute.
     *  @return The attribute with the given name for the decorator, or null
     *   if the specified decorator does not provide an attribute with the specified
     *   name.
     *  @exception IllegalActionException If a decorator referenced
     *   by a DecoratorAttributes cannot be found.
     */
    public Attribute getDecoratorAttribute(Decorator decorator, String name)
            throws IllegalActionException {
        DecoratorAttributes attributes = getDecoratorAttributes(decorator);
        if (attributes != null) {
            return attributes.getAttribute(name);
        }
        return null;
    }

    /** Return the decorated attributes of this NamedObj, as decorated by the
     *  specified decorator. If there are no such attributes, then calling
     *  this method will cause them to attempt to be created and assigned default values.
     *  If the specified decorator does not decorate this object, then this method will
     *  return null.
     *  @see ptolemy.kernel.util.Decorator#createDecoratorAttributes(NamedObj)
     *  @see #getDecoratorAttribute(Decorator, String)
     *  @param decorator The decorator.
     *  @return The decorated attributes, or null if the specified decorator does not
     *   decorate this object.
     *  @exception IllegalActionException If a decorator referenced
     *   by a DecoratorAttributes cannot be found.
     */
    public DecoratorAttributes getDecoratorAttributes(Decorator decorator)
            throws IllegalActionException {
        synchronized (_decoratorAttributes) {
            _updateDecoratorAttributes();
            return _decoratorAttributes.get(decorator);
        }
    }

    /** Get the minimum level above this object in the hierarchy where a
     *  parent-child relationship implies the existence of this object.
     *  A value Integer.MAX_VALUE is used to indicate that this object is
     *  not a derived object. A value of 1 indicates that the container
     *  of the object is a child, and that the this object is derived
     *  from a prototype in the parent of the container. Etc.
     *  @return The level above this object in the containment
     *   hierarchy where a parent-child relationship implies this object.
     *  @see Derivable
     *  @see #setDerivedLevel(int)
     */
    @Override
    public int getDerivedLevel() {
        return _derivedLevel;
    }

    /** Return a list of objects derived from this one.
     *  This is the list of objects that are "inherited" by their
     *  containers from a container of this object. The existence of
     *  these derived objects is "implied" by a parent-child relationship
     *  somewhere above this object in the containment hierarchy.
     *  This method returns a complete list, including objects that
     *  have been overridden.
     *  @return A list of objects of the same class as the object on
     *   which this is called, or an empty list if there are none.
     *  @see Derivable
     */
    @Override
    public List getDerivedList() {
        try {
            return _getDerivedList(null, false, false, this, 0, null, null);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }
    }

    /** Return a name to present to the user. If setDisplayName(String)
     *  has been called, then return the name specified there, and
     *  otherwise return the name returned by getName().
     *  @return A name to present to the user.
     *  @see #setDisplayName(String)
     */
    @Override
    public String getDisplayName() {
        if (_displayName != null) {
            return _displayName;
        }
        return getName();
    }

    /** Get the MoML element name. This defaults to "entity"
     *  but can be set to something else by subclasses.
     *  @return The MoML element name for this object.
     *  @see MoMLExportable
     */
    @Override
    public String getElementName() {
        return _elementName;
    }

    /** Return a string of the form ".name1.name2...nameN". Here,
     *  "nameN" is the name of this object,
     *  and the intervening names are the names of the containers
     *  of this other name of this object, if there are containers.
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, results in a runtime exception of class
     *  InvalidStateException.  Note that it is
     *  not possible to construct a recursive structure using this class alone,
     *  since there is no container.
     *  But derived classes might erroneously permit recursive structures,
     *  so this error is caught here.
     *  This method is read-synchronized on the workspace.
     *  @return The full name of the object.
     */
    @Override
    public String getFullName() {
        try {
            _workspace.getReadAccess();

            if (_fullNameVersion == _workspace.getVersion()) {
                return _fullNameCache;
            }

            // Cache is not valid. Recalculate full name.
            String fullName = getName();

            // Use a hash set to keep track of what we've seen already.
            Set<Nameable> visited = new HashSet<Nameable>();
            visited.add(this);

            Nameable container = getContainer();

            while (container != null) {
                if (visited.contains(container)) {
                    // Cannot use "this" as a constructor argument to the
                    // exception or we'll get stuck infinitely
                    // calling this method, since this method is used to report
                    // exceptions.  InvalidStateException is a runtime
                    // exception, so it need not be declared.
                    throw new InvalidStateException(
                            "Container contains itself!");
                }

                fullName = container.getName() + "." + fullName;
                visited.add(container);
                container = container.getContainer();
            }

            _fullNameCache = "." + fullName;
            _fullNameVersion = _workspace.getVersion();
            return _fullNameCache;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the model error handler specified by setErrorHandler().
     *  @return The error handler, or null if none.
     *  @see #setModelErrorHandler(ModelErrorHandler handler)
     */
    public ModelErrorHandler getModelErrorHandler() {
        return _modelErrorHandler;
    }

    /** Get the name. If no name has been given, or null has been given,
     *  then return an empty string, "".
     *  @return The name of the object.
     *  @see #setName(String)
     */
    @Override
    public String getName() {
        return _name;
    }

    /** Get the name of this object relative to the specified container.
     *  If this object is contained directly by the specified container,
     *  this is just its name, as returned by getName().  If it is deeply
     *  contained by the specified container, then the relative name is
     *  <i>x1</i>.<i>x2</i>. ... .<i>name</i>, where <i>x1</i> is directly
     *  contained by the specified container, <i>x2</i> is contained by
     *  <i>x1</i>, etc.  If this object is not deeply contained by the
     *  specified container, then this method returns the full name of
     *  this object, as returned by getFullName().
     *  <p>
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, may result in a runtime exception of class
     *  InvalidStateException if it is detected.  Note that it is
     *  not possible to construct a recursive structure using this class alone,
     *  since there is no container.
     *  But derived classes might erroneously permit recursive structures,
     *  so this error is caught here.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *  @param parent The object relative to which you want the name.
     *  @return A string of the form "name2...nameN".
     *  @exception InvalidStateException If a recursive structure is
     *   encountered, where this object directly or indirectly contains
     *   itself. Note that this is a runtime exception so it need not
     *   be declared explicitly.
     *  @see #setName(String)
     */
    @Override
    public String getName(NamedObj parent) throws InvalidStateException {
        if (parent == null) {
            return getFullName();
        }

        try {
            _workspace.getReadAccess();

            StringBuffer name = new StringBuffer(getName());

            // Use a hash set to keep track of what we've seen already.
            Set<Nameable> visited = new HashSet<Nameable>();
            visited.add(this);

            Nameable container = getContainer();

            while (container != null && container != parent) {
                if (visited.contains(container)) {
                    // Cannot use "this" as a constructor argument to the
                    // exception or we'll get stuck infinitely
                    // calling getFullName(),
                    // since that method is used to report
                    // exceptions.  InvalidStateException is a runtime
                    // exception, so it need not be declared.
                    throw new InvalidStateException(
                            "Container contains itself!");
                }

                name.insert(0, ".");
                name.insert(0, container.getName());
                visited.add(container);
                container = container.getContainer();
            }

            if (container == null) {
                return getFullName();
            }

            return name.toString();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of prototypes for this object. The list is ordered
     *  so that more local prototypes are listed before more remote
     *  prototypes. Specifically, if the container has a parent, and
     *  that parent contains an object whose name matches the name
     *  of this object, then that object is the first prototype listed.
     *  If the container of the container has a parent, and that parent
     *  (deeply) contains a prototype, then that prototype is listed next.
     *  And so on up the hierarchy.
     *  @return A list of prototypes for this object, each of which is
     *   assured of being an instance of the same (Java) class as this
     *   object, or an empty list if there are no prototypes.
     *  @exception IllegalActionException If a prototype with the right
     *   name but the wrong class is found.
     *  @see Derivable
     */
    @Override
    public List getPrototypeList() throws IllegalActionException {
        List<NamedObj> result = new LinkedList<NamedObj>();
        NamedObj container = getContainer();
        String relativeName = getName();

        while (container != null) {
            if (container instanceof Instantiable) {
                Instantiable parent = ((Instantiable) container).getParent();

                if (parent != null) {
                    // Check whether the parent has it...
                    NamedObj prototype = _getContainedObject((NamedObj) parent,
                            relativeName);

                    if (prototype != null) {
                        result.add(prototype);
                    }
                }
            }

            relativeName = container.getName() + "." + relativeName;
            container = container.getContainer();
        }

        return result;
    }

    /** Get the source, which gives an external URL
     *  associated with an entity (presumably from which the entity
     *  was defined).  This becomes the value in the "source"
     *  attribute of exported MoML.
     *  @return The source, or null if there is none.
     *  @see #setSource(String)
     *  @see MoMLExportable
     */
    @Override
    public String getSource() {
        return _source;
    }

    /** Handle a model error. If a model error handler has been registered
     *  with setModelErrorHandler(), then handling is delegated to that
     *  handler.  Otherwise, or if the registered error handler declines
     *  to handle the error by returning false, then if there is a
     *  container, handling is delegated to the container.
     *  If there is no container and no handler that agrees to
     *  handle the error, then return false.
     *  <p>
     *  A typical use of this facility is where a subclass of NamedObj
     *  does the following:
     *  <pre>
     *     handleModelError(this, new IllegalActionException(this, message));
     *  </pre>
     *  instead of this:
     *  <pre>
     *     throw new IllegalActionException(this, message);
     *  </pre>
     *  The former allows a container in the hierarchy to intercept the
     *  exception, whereas the latter simply throws the exception.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error is handled, false otherwise.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.
     *  @see #setModelErrorHandler(ModelErrorHandler handler)
     */
    @Override
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {
        // FIXME: This code fails horribly when one forgets to add a
        // BasicModelErrorHandler at the toplevel of the model.  In
        // reality, this code should do what BasicModelErrorHandler
        // does and throw an exception when anything falls off the top
        // of the model.
        if (_modelErrorHandler != null) {
            if (_modelErrorHandler.handleModelError(context, exception)) {
                return true;
            }
        }

        ModelErrorHandler container = getContainer();

        if (container != null) {
            return container.handleModelError(context, exception);
        }

        return false;
    }

    /** Return true if setDeferringChangeRequests(true) has been called
     *  to specify that change requests should be deferred. If there
     *  is a container, this delegates to the container.
     *  @return True if change requests are being deferred.
     *  @see #setDeferringChangeRequests(boolean)
     *  @see Changeable
     */
    @Override
    public boolean isDeferringChangeRequests() {
        NamedObj container = getContainer();

        if (container != null) {
            return container.isDeferringChangeRequests();
        }

        return _deferChangeRequests;
    }

    /** Return true if propagateValue() has been called, which
     *  indicates that the value of this object (if any) has been
     *  overridden from the default defined by its class definition.
     *  Note that if setDerivedLevel() is called after propagateValue(),
     *  then this method will return false, since setDerivedLevel()
     *  resets the override property.
     *  @return True if propagateValues() has been called.
     *  @see #propagateValue()
     *  @see #setDerivedLevel(int)
     */
    public boolean isOverridden() {
        // Return true only if _override is a list of length 1
        // with the value 0.
        if (_override == null) {
            return false;
        }

        if (_override.size() != 1) {
            return false;
        }

        int override = _override.get(0).intValue();
        return override == 0;
    }

    /** Return true if this object is persistent.
     *  A persistent object has a MoML description that can be stored
     *  in a file and used to re-create the object. A non-persistent
     *  object has an empty MoML description.
     *  @return True if the object is persistent.
     *  @see #setPersistent(boolean)
     *  @see MoMLExportable
     */
    @Override
    public boolean isPersistent() {
        return _isPersistent == null || _isPersistent.booleanValue();
    }

    /** Return an iterator over contained object that currently exist,
     *  omitting any objects that have not yet been instantiated because
     *  they are "lazy". A lazy object is one that is instantiated when it
     *  is needed, but not before. In this base class, this method returns
     *  the same iterator returned by {@link #containedObjectsIterator()}.
     *  If derived classes override it, they must guarantee that any omitted
     *  objects are genuinely not needed in whatever uses this method.
     *  @return An iterator over instances of NamedObj contained by this
     *   object.
     */
    public Iterator lazyContainedObjectsIterator() {
        return containedObjectsIterator();
    }

    /** React to a debug message by relaying it to any registered
     *  debug listeners.
     *  @param message The debug message.
     *  @since Ptolemy II 2.3
     */
    @Override
    public void message(String message) {
        if (_debugging) {
            _debug(message);
        }
    }

    /** Move this object down by one in the list of objects in
     *  its container. If this object is already
     *  last, do nothing.  In this base class, this method throws
     *  an IllegalActionException because this base class does not
     *  have a setContainer() method, and hence cannot be contained.
     *  Any derived object that implements setContainer() should
     *  also implement this method.
     *  @return This base class does not return. In derived classes, it should
     *   return the index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    @Override
    public int moveDown() throws IllegalActionException {
        // NOTE: This method could be made abstract, but NamedObj
        // is not abstract to allow for more complete testing.
        throw new IllegalActionException(this, "Has no container.");
    }

    /** Move this object to the first position in the list
     *  of attributes of the container. If this object is already
     *  first, do nothing.  In this base class, this method throws
     *  an IllegalActionException because this base class does not
     *  have a setContainer() method, and hence cannot be contained.
     *  Any derived object that implements setContainer() should
     *  also implement this method.
     *  @return This base class does not return. In derived classes, it should
     *   return the index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    @Override
    public int moveToFirst() throws IllegalActionException {
        // NOTE: This method could be made abstract, but NamedObj
        // is not abstract to allow for more complete testing.
        throw new IllegalActionException(this, "Has no container.");
    }

    /** Move this object to the specified position in the list of
     *  attributes of the container. If this object is already at the
     *  specified position, do nothing.  In this base class, this
     *  method throws an IllegalActionException because this base
     *  class does not have a setContainer() method, and hence cannot
     *  be contained.
     *  Any derived object that implements setContainer() should
     *  also implement this method.
     *  @param index The position to move this object to.
     *  @return This base class does not return. In derived classes, it should
     *   return the index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    @Override
    public int moveToIndex(int index) throws IllegalActionException {
        // NOTE: This method could be made abstract, but NamedObj
        // is not abstract to allow for more complete testing.
        throw new IllegalActionException(this, "Has no container.");
    }

    /** Move this object to the last position in the list
     *  of attributes of the container.  If this object is already last,
     *  do nothing. In this base class, this method throws
     *  an IllegalActionException because this base class does not
     *  have a setContainer() method, and hence cannot be contained.
     *  Any derived object that implements setContainer() should
     *  also implement this method.
     *  @return This base class does not return. In derived classes, it should
     *   return the index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    @Override
    public int moveToLast() throws IllegalActionException {
        // NOTE: This method could be made abstract, but NamedObj
        // is not abstract to allow for more complete testing.
        throw new IllegalActionException(this, "Has no container.");
    }

    /** Move this object up by one in the list of
     *  attributes of the container. If this object is already first, do
     *  nothing. In this base class, this method throws
     *  an IllegalActionException because this base class does not
     *  have a setContainer() method, and hence cannot be contained.
     *  Any derived object that implements setContainer() should
     *  also implement this method.
     *  @return This base class does not return. In derived classes, it should
     *   return the index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    @Override
    public int moveUp() throws IllegalActionException {
        // NOTE: This method could be made abstract, but NamedObj
        // is not abstract to allow for more complete testing.
        throw new IllegalActionException(this, "Has no container.");
    }

    /** React to a change in a contained named object. This method is
     *  called by a contained named object when its name or display name
     *  changes. In this base class, the method does nothing.
     *  @param object The object that changed.
     */
    public void notifyOfNameChange(NamedObj object) {
    }

    /** Propagate the existence of this object.
     *  If this object has a container, then ensure that all
     *  objects derived from the container contain an object
     *  with the same class and name as this one. Create that
     *  object when needed. The contents of each so created
     *  object is marked as derived using setDerivedLevel().
     *  Return the list of objects that are created.
     *  @return A list of derived objects of the same class
     *   as this object that are created.
     *  @exception IllegalActionException If the object does
     *   not exist and cannot be created.
     *  @see Derivable
     *  @see #setDerivedLevel(int)
     */
    @Override
    public List propagateExistence() throws IllegalActionException {
        return _getDerivedList(null, false, true, this, 0, null, null);
    }

    /** Propagate the value (if any) held by this
     *  object to derived objects that have not been overridden.
     *  This leaves all derived objects unchanged if any single
     *  derived object throws an exception
     *  when attempting to propagate the value to it.
     *  This also marks this object as overridden.
     *  @return The list of objects to which this propagated.
     *  @exception IllegalActionException If propagation fails.
     *  @see Derivable
     *  @see #isOverridden()
     */
    @Override
    public List propagateValue() throws IllegalActionException {
        // Mark this object as having been modified directly.
        _override = new LinkedList<Integer>();
        _override.add(Integer.valueOf(0));

        return _getDerivedList(null, true, false, this, 0, _override, null);
    }

    /** If this object has a value that has been set directly,
     *  or if it has a value that has propagated in, then
     *  propagate that value to all derived objects, and
     *  then repeat this for all objects this object contains.
     *  Unlike propagateValue(), this does not assume this
     *  object or any of its contained objects is having
     *  its value set directly. Instead, it uses the current
     *  state of override of this object as the starting point.
     *  @exception IllegalActionException If propagation fails.
     */
    public void propagateValues() throws IllegalActionException {
        // If this object has not had its value set directly or
        // by propagation into it, then there is no need to do
        // any propagation.
        if (_override != null) {
            _getDerivedList(null, true, false, this, 0, _override, null);
        }

        Iterator<?> containedObjects = containedObjectsIterator();

        while (containedObjects.hasNext()) {
            NamedObj containedObject = (NamedObj) containedObjects.next();
            containedObject.propagateValues();
        }
    }

    /** Remove a change listener. If there is a container, delegate the
     *  request to the container.  If the specified listener is not
     *  on the list of listeners, do nothing.
     *  @param listener The listener to remove.
     *  @see #addChangeListener(ChangeListener)
     *  @see Changeable
     */
    @Override
    public synchronized void removeChangeListener(ChangeListener listener) {
        NamedObj container = getContainer();

        if (container != null) {
            container.removeChangeListener(listener);
        } else {
            synchronized (_changeLock) {
                if (_changeListeners != null) {
                    ListIterator<WeakReference<ChangeListener>> listeners = _changeListeners
                            .listIterator();

                    while (listeners.hasNext()) {
                        WeakReference<ChangeListener> reference = listeners
                                .next();

                        if (reference.get() == listener) {
                            listeners.remove();
                        } else if (reference.get() == null) {
                            listeners.remove();
                        }
                    }
                }
            }
        }
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    @Override
    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }

        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized (_debugListeners) {
            _debugListeners.remove(listener);

            if (_debugListeners.size() == 0) {
                _debugging = false;
            }

            return;
        }
    }

    /** Remove a hierarchy listener. If the listener is already
     *  removed, do nothing. This will cause the object to also
     *  be removed as a hierarchy listener in the container of
     *  this object, if there is one, and in its container,
     *  up to the top of the hierarchy.
     *  @param listener The listener to remove.
     *  @see #addHierarchyListener(HierarchyListener)
     */
    public void removeHierarchyListener(HierarchyListener listener) {
        if (_hierarchyListeners != null) {
            _hierarchyListeners.remove(listener);

            // Remove from the container.
            NamedObj container = getContainer();
            if (container != null) {
                container.removeHierarchyListener(listener);
            }
        }
    }

    /** Request that the given change be executed.   In this base class,
     *  delegate the change request to the container, if there is one.
     *  If there is no container, then execute the request immediately,
     *  unless this object is deferring change requests. If
     *  setDeferChangeRequests() has been called with a true argument,
     *  then simply queue the request until either setDeferChangeRequests()
     *  is called with a false argument or executeChangeRequests() is called.
     *  If this object is already in the middle of executing a change
     *  request, then that execution is finished before this one is performed.
     *  Change listeners will be notified of success (or failure) of the
     *  request when it is executed.
     *  @param change The requested change.
     *  @see #executeChangeRequests()
     *  @see #setDeferringChangeRequests(boolean)
     *  @see Changeable
     */
    @Override
    public void requestChange(ChangeRequest change) {
        NamedObj container = getContainer();

        if (container != null) {
            container.requestChange(change);
        } else {
            // Synchronize to make sure we don't modify
            // the list of change requests while some other
            // thread is also trying to read or modify it.
            synchronized (_changeLock) {
                // Queue the request.
                // Create the list of requests if it doesn't already exist
                if (_changeRequests == null) {
                    _changeRequests = new LinkedList<ChangeRequest>();
                }

                _changeRequests.add(change);
            }
            if (!_deferChangeRequests) {
                executeChangeRequests();
            }
        }
    }

    /** Set the MoML class name.  This is either the
     *  class of which this object is an instance, or if this
     *  object is itself a class, then the class that it extends.
     *  @param name The MoML class name.
     *  @see #getClassName()
     */
    public void setClassName(String name) {
        _className = name;
    }

    /** Specify whether change requests made by calls to requestChange()
     *  should be executed immediately. If there is a container, then
     *  this request is delegated to the container. Otherwise,
     *  if the argument is true, then requests
     *  are simply queued until either this method is called again
     *  with argument false, or until executeChangeRequests() is called.
     *  If the argument is false, then execute any pending change requests
     *  and set a flag requesting that future requests be executed
     *  immediately.
     *  @param isDeferring If true, defer change requests.
     *  @see #addChangeListener(ChangeListener)
     *  @see #executeChangeRequests()
     *  @see #isDeferringChangeRequests()
     *  @see #requestChange(ChangeRequest)
     *  @see Changeable
     */
    @Override
    public void setDeferringChangeRequests(boolean isDeferring) {
        NamedObj container = getContainer();

        if (container != null) {
            container.setDeferringChangeRequests(isDeferring);
            return;
        }

        // Make sure to avoid modification of this flag in the middle
        // of a change request or change execution.
        List<ChangeRequest> copy = null;
        synchronized (_changeLock) {
            _deferChangeRequests = isDeferring;

            if (isDeferring == false) {
                // Must not hold _changeLock while executing change requests.
                copy = _copyChangeRequestList();
            }
        }
        if (copy != null) {
            _executeChangeRequests(copy);
        }
    }

    /** Set the level above this object in the hierarchy where a
     *  parent-child relationship implies the existence of this object.
     *  When this object is originally created by a constructor or
     *  by the clone method, the level is set to the default Integer.MAX_VALUE,
     *  which indicates that the object is not implied. When this
     *  is called multiple times, the level will be the minimum of
     *  all the levels specified. Thus, a value of 1 indicates that the
     *  container of the object is a child, and that this object is
     *  implied by a like object in the parent of the container, for example.
     *  If an object is implied, then it normally has no persistent
     *  representation when it is exported to MoML (unless it
     *  is overridden), and normally it cannot have its name or
     *  container changed.  An exception, however, is that the object
     *  may appear in the MoML if the exported MoML does not include
     *  the level of the hierarchy above this with the parent-child
     *  relationship that implies this object.
     *  Calling this method also has the side effect of resetting the
     *  flag used to determine whether the value of this object overrides
     *  some inherited value. So this method should only be called when
     *  object is first being constructed.
     *  <p>
     *  NOTE: This method is tricky to use correctly. It is public because
     *  the MoML parser needs access to it. It should not be considered part
     *  of the public interface, however, in that only very sophisticated
     *  users should use it.
     *  @param level The minimum level above this object in the containment
     *   hierarchy where a parent-child relationship implies this object.
     *  @see #getDerivedLevel()
     *  @see #setPersistent(boolean)
     *  @see Derivable
     */
    public final void setDerivedLevel(int level) {
        if (level < _derivedLevel) {
            _derivedLevel = level;
        }

        // Setting override to null indicates that no override has
        // occurred.
        // NOTE: This setting of _override to null was commented
        // out, justified by the following NOTE.  However,
        // the following NOTE is questionable, since this public
        // method is invoked by the MoMLParser and MoMLChangeRequest
        // in circumstances where they really want the objects to be
        // marked as not overridden. Thus, I have changed this so that
        // local uses of this method are replaced with direct actions
        // and public accesses of this method revert to the original
        // behavior.  11/07 EAL
        // OBSOLETE NOTE: This is no longer the right thing to do.
        // Upon instantiating, the clone method creates a copy
        // of the _override field.  Then the _adjustOverrides()
        // method adjusts the value of that field to reflect
        // that the new object gets its value from the object
        // from which it was cloned, or from whatever that
        // gets it from.
        _override = null;
    }

    /** Set a name to present to the user.
     *  @param name A name to present to the user.
     *  @see #getDisplayName()
     */
    public void setDisplayName(String name) {
        _displayName = name;
        // Notify container of this change.
        NamedObj container = getContainer();
        if (container != null) {
            container.notifyOfNameChange(this);
        }
    }

    /** Set the model error handler.
     *  @param handler The error handler, or null to specify no handler.
     *  @see #getModelErrorHandler()
     */
    public void setModelErrorHandler(ModelErrorHandler handler) {
        _modelErrorHandler = handler;
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period
     *   or if the object is a derived object and the name argument does
     *   not match the current name.
     *  @exception NameDuplicationException Not thrown in this base class.
     *   May be thrown by derived classes if the container already contains
     *   an object with this name.
     *  @see #getName()
     *  @see #getName(NamedObj)
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        String oldName = "";

        if (_debugging) {
            oldName = getFullName();
        }

        if (name == null) {
            name = "";
        }

        if (name.equals(_name)) {
            // Nothing to do.
            return;
        }

        int period = name.indexOf(".");

        if (period >= 0) {
            throw new IllegalActionException(this,
                    "Cannot set a name with a period: " + name);
        }

        // Unescape if necessary. escapeForXML occurs in
        // exportMoML(Writer output, int depth, String name)
        // See http://chess.eecs.berkeley.edu/ptolemy/listinfo/ptolemy/2010-April/011999.html
        name = StringUtilities.unescapeForXML(name);

        try {
            _workspace.getWriteAccess();
            _name = name;
        } finally {
            _workspace.doneWriting();
        }

        // Notify container of this change.
        NamedObj container = getContainer();
        if (container != null) {
            container.notifyOfNameChange(this);
        }

        if (_debugging) {
            _debug("Changed name from " + oldName + " to " + getFullName());
        }
    }

    /** Set the persistence of this object. If the persistence is not
     *  specified with this method, then by default the object will be
     *  persistent unless it is derivable by derivation from a class.
     *  A persistent object has a non-empty MoML description that can be used
     *  to re-create the object. To make an instance non-persistent,
     *  call this method with the argument <i>false</i>. To force
     *  it to always be persistent, irrespective of its relationship
     *  to a class, then call this with argument <i>true</i>. Note
     *  that this will have the additional effect that it no longer
     *  inherits properties from the class, so in effect, calling
     *  this with <i>true</i> overrides values given by the class.
     *  @param persistent False to make this object non-persistent.
     *  @see #isPersistent()
     *  @see MoMLExportable
     */
    @Override
    public void setPersistent(boolean persistent) {
        if (persistent) {
            _isPersistent = Boolean.TRUE;
        } else {
            _isPersistent = Boolean.FALSE;
        }
    }

    /** Set the source, which gives an external URL
     *  associated with an entity (presumably from which the entity
     *  was defined).  This becomes the value in the "source"
     *  attribute of exported MoML. Call this with null to prevent
     *  any source attribute from being generated.
     *  @param source The source, or null if there is none.
     *  @see #getSource()
     *  @see MoMLExportable
     */
    @Override
    public void setSource(String source) {
        _source = source;
    }

    /** Return an ordered list of contained objects filtered by the specified
     *  filter. The attributes are listed first, followed by ports,
     *  classes, entities, and relations, in that order. Within each
     *  category, objects are listed in the order they were created
     *  (or as later modified by methods like moveDown()). The filter
     *  gives a collection of objects to include. Only objects
     *  contained by the filter are included.
     *  @param filter A collection specifying which objects to include
     *   in the returned list.
     *  @return A list of contained instances of NamedObj that are
     *   in the specified filter, or an empty list if there are none.
     */
    public List sortContainedObjects(Collection filter) {
        LinkedList<NamedObj> result = new LinkedList<NamedObj>();
        Iterator<?> containedObjects = containedObjectsIterator();

        while (containedObjects.hasNext()) {
            NamedObj object = (NamedObj) containedObjects.next();

            if (filter.contains(object)) {
                result.add(object);
            }
        }

        return result;
    }

    /** Return the class name and the full name of the object,
     *  with syntax "className {fullName}".
     *  @return The class name and the full name. */
    @Override
    public String toString() {
        return getClass().getName() + " {" + getFullName() + "}";
    }

    /** Return the top level of the containment hierarchy.
     *  @return The top level, or this if this has no container.
     */
    public NamedObj toplevel() {
        NamedObj result = this;

        while (result.getContainer() != null) {
            result = result.getContainer();
        }

        return result;
    }

    /** Return a name that is guaranteed to not be the name of any
     *  contained attribute.  In derived classes, this should be overridden
     *  so that the returned name is guaranteed to not conflict with
     *  any contained object.  In this implementation, the argument
     *  is stripped of any numeric suffix, and then a numeric suffix
     *  is appended and incremented until a name is found that does not
     *  conflict with a contained attribute.
     *  @param prefix A prefix for the name.
     *  @return A unique name, which will be exactly the prefix if possible,
     *   or the prefix extended by a number.
     */
    public String uniqueName(String prefix) {
        if (prefix == null) {
            prefix = "null";
        }

        prefix = _stripNumericSuffix(prefix);

        String candidate = prefix;
        int uniqueNameIndex = 2;

        while (getAttribute(candidate) != null) {
            candidate = prefix + uniqueNameIndex++;
        }

        return candidate;
    }

    /** Validate attributes deeply contained by this object if they
     *  implement the Settable interface by calling their validate() method.
     *  Errors that are triggered by this validation are handled by calling
     *  handleModelError().  Normally this should be called after constructing
     *  a model or after making changes to it.  It is called, for example,
     *  by the MoMLParser.
     *  @see #handleModelError(NamedObj context, IllegalActionException exception)
     *  @exception IllegalActionException If there is a problem validating
     *  the deeply contained attributes.
     */
    public void validateSettables() throws IllegalActionException {
        _validateSettables(new HashSet<Settable>());
    }

    /** Get the workspace. This method never returns null, since there
     *  is always a workspace.
     *  @return The workspace responsible for this object.
     */
    public final Workspace workspace() {
        return _workspace;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include everything.
     */
    public static final int COMPLETE = -1;

    /** Indicate that the description(int) method should include the class name.
     */
    public static final int CLASSNAME = 1;

    /** Indicate that the description(int) method should include the full name.
     *  The full name is surrounded by braces "{name}" in case it has spaces.
     */
    public static final int FULLNAME = 2;

    /** Indicate that the description(int) method should include the links
     *  (if any) that the object has.  This has the form "links {...}"
     *  where the list is a list of descriptions of the linked objects.
     *  This may force some of the contents to be listed.  For example,
     *  a description of an entity will include the ports if this is set,
     *  irrespective of whether the CONTENTS bit is set.
     */
    public static final int LINKS = 4;

    /** Indicate that the description(int) method should include the contained
     *  objects (if any) that the object has.  This has the form
     *  "keyword {{class {name}} {class {name}} ... }" where the keyword
     *  can be ports, entities, relations, or anything else that might
     *  indicate what the object contains.
     */
    public static final int CONTENTS = 8;

    /** Indicate that the description(int) method should include the contained
     *  objects (if any) that the contained objects have.  This has no effect
     *  if CONTENTS is not also specified.  The returned string has the form
     *  "keyword {{class {name} keyword {...}} ... }".
     */
    public static final int DEEP = 16;

    /** Indicate that the description(int) method should include attributes
     *  (if any).
     */
    public static final int ATTRIBUTES = 32;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an attribute.  This method should not be used directly.
     *  Instead, call setContainer() on the attribute.
     *  Derived classes may further constrain the class of the attribute.
     *  To do this, they should override this method to throw an exception
     *  when the argument is not an instance of the expected class.
     *  <p>
     *  This method is write-synchronized on the workspace and increments its
     *  version number.</p>
     *
     *  @param attribute The attribute to be added.
     *  @exception NameDuplicationException If this object already
     *   has an attribute with the same name.
     *  @exception IllegalActionException If the attribute is not an
     *   an instance of the expect class (in derived classes).
     */
    protected void _addAttribute(Attribute attribute)
            throws NameDuplicationException, IllegalActionException {
        try {
            _workspace.getWriteAccess();

            if (_attributes == null) {
                _attributes = new NamedList();
            }

            _attributes.append(attribute);

            if (_debugging) {
                _debug("Added attribute", attribute.getName(), "to",
                        getFullName());
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Adjust the _override field of this object, if there is
     *  one, by incrementing the value at the specified depth
     *  by one, and do the same for all contained objects, with
     *  one larger depth.
     *  @param depth The depth.
     */
    protected void _adjustOverride(int depth) {
        // This method is called after cloning, so having a non-null
        // _override means that this object was cloned from and
        // is derived from an object whose value is set from some
        // other object according to the _override field.
        if (_override != null) {
            // If the _override field is not long enough,
            // then we need to make it long enough.
            while (_override.size() <= depth) {
                _override.add(Integer.valueOf(0));
            }
            int breadth = _override.get(depth).intValue();
            _override.set(depth, Integer.valueOf(breadth + 1));
        }
        Iterator<?> objects = lazyContainedObjectsIterator();
        while (objects.hasNext()) {
            NamedObj object = (NamedObj) objects.next();
            object._adjustOverride(depth + 1);
        }
    }

    /** Attach the specified text as an attribute with the specified
     *  name.  This is a convenience method (syntactic sugar) that
     *  creates an instance of TransientSingletonConfigurableAttribute
     *  and configures it with the specified text.  This attribute
     *  is transient, meaning that it is not described by exported
     *  MoML.  Moreover, it is a singleton, meaning that it will
     *  replace any previously contained instance of SingletonAttribute
     *  that has the same name.
     *  <p>
     *  Note that attribute names beginning with an underscore "_"
     *  are reserved for system use.  This method is used in several
     *  places to set the value of such attributes.
     *  @param name The name of the attribute.
     *  @param text The text with which to configure the attribute.
     */
    protected void _attachText(String name, String text) {
        try {
            SingletonConfigurableAttribute icon = new SingletonConfigurableAttribute(
                    this, name);
            icon.setPersistent(false);

            // The first argument below is the base w.r.t. which to open
            // relative references within the text, which doesn't make
            // sense in this case, so it's null. The second argument is
            // an external URL source for the text, which is again null.
            icon.configure(null, null, text);
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Error creating singleton attribute named " + name
                    + " for " + getFullName());
        }
    }

    /** Fix the fields of the given object which point to Attributes.
     *  The object is assumed to be a clone of this one.  The fields
     *  are fixed to point to the corresponding attribute of the clone,
     *  instead of pointing to attributes of this object.
     *  @param newObject The object in which we fix the fields.
     *  @exception CloneNotSupportedException If there is a problem
     *   getting the attribute
     */
    protected void _cloneFixAttributeFields(NamedObj newObject)
            throws CloneNotSupportedException {
        // If the new object has any public fields whose name
        // matches that of an attribute, then set the public field
        // equal to the attribute.
        Class<? extends NamedObj> myClass = getClass();
        Field[] fields = myClass.getFields();

        for (int i = 0; i < fields.length; i++) {
            try {
                // VersionAttribute has a final field
                if (!Modifier.isFinal(fields[i].getModifiers())) {
                    Object object = fields[i].get(this);

                    if (object instanceof Attribute) {
                        String name = ((NamedObj) object).getName(this);
                        fields[i].set(newObject, newObject.getAttribute(name));
                    }
                }
            } catch (IllegalAccessException ex) {
                // CloneNotSupportedException does not have a
                // constructor that takes a cause argument, so we call
                // initCause() and then throw.
                CloneNotSupportedException cloneException = new CloneNotSupportedException(
                        "The field associated with " + fields[i].getName()
                        + " could not be automatically cloned because "
                        + ex.getMessage() + ".  This can be caused if "
                        + "the field is not defined in a public class.");

                cloneException.initCause(ex);
                throw cloneException;
            }
        }
    }

    /** Return a list of decorators contained by this object.
     *  In this base class, this list consists of Attributes that implement
     *  the {@link Decorator} interface. In subclasses, it can contain other
     *  objects that implement the Decorator interface, such as Entities.
     *  @return A list of contained decorators.
     */
    protected List<Decorator> _containedDecorators() {
        return attributeList(Decorator.class);
    }

    /** Return a copy of the current list of change requests, or return
     *  null if there aren't any pending change requests.
     *  @return A copy of the change request list, or null if there aren't any.
     */
    protected List<ChangeRequest> _copyChangeRequestList() {
        synchronized (_changeLock) {
            if (_changeRequests != null && _changeRequests.size() > 0) {
                // Copy the change requests lists because it may
                // be modified during execution.
                List<ChangeRequest> copy = new LinkedList(_changeRequests);

                // Remove the changes to be executed.
                // We remove them even if there is a failure because
                // otherwise we could get stuck making changes that
                // will continue to fail.
                _changeRequests.clear();

                return copy;
            }
        }
        return null;
    }

    /** Send a debug event to all debug listeners that have registered.
     *  @param event The event.
     */
    protected final void _debug(DebugEvent event) {
        if (_debugging) {
            // We copy this list so that responding to the event may block.
            // while the execution thread is blocked, we want to be able to
            // add more debug listeners...
            // Yes, this is slow, but hey, it's debug code.
            List<DebugListener> list;

            if (_debugListeners == null) {
                System.err
                .println("Warning, _debugListeners was null, "
                        + "which means that _debugging was set to true, but no "
                        + "listeners were added?");
                System.err.println(event);
            } else {
                // NOTE: This used to synchronize on this, which caused
                // deadlocks. We use a more specialized lock now.
                synchronized (_debugListeners) {
                    list = new ArrayList<DebugListener>(_debugListeners);
                }

                for (DebugListener listener : list) {
                    listener.event(event);
                }
            }
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param message The message.
     */
    protected final void _debug(String message) {
        if (_debugging) {
            // We copy this list so that responding to the event may block.
            // while the execution thread is blocked, we want to be able to
            // add more debug listeners...
            // Yes, this is slow, but hey, it's debug code.
            List<DebugListener> list;

            if (_debugListeners == null) {
                System.err
                .println("Warning, _debugListeners was null, "
                        + "which means that _debugging was set to true, but no "
                        + "listeners were added?");
                System.err.println(message);
            } else {
                // NOTE: This used to synchronize on this, which caused
                // deadlocks.  We use a more specialized lock now.
                synchronized (_debugListeners) {
                    list = new ArrayList<DebugListener>(_debugListeners);
                }

                for (DebugListener listener : list) {
                    listener.message(message);
                }
            }
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  The message is a concatenation of the two parts, with a space between
     *  them.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param part1 The first part of the message.
     *  @param part2 The second part of the message.
     */
    protected final void _debug(String part1, String part2) {
        if (_debugging) {
            _debug(part1 + " " + part2);
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  The message is a concatenation of the three parts, with a space between
     *  them.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param part1 The first part of the message.
     *  @param part2 The second part of the message.
     *  @param part3 The third part of the message.
     */
    protected final void _debug(String part1, String part2, String part3) {
        if (_debugging) {
            _debug(part1 + " " + part2 + " " + part3);
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  The message is a concatenation of the four parts, with a space between
     *  them.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param part1 The first part of the message.
     *  @param part2 The second part of the message.
     *  @param part3 The third part of the message.
     *  @param part4 The fourth part of the message.
     */
    protected final void _debug(String part1, String part2, String part3,
            String part4) {
        if (_debugging) {
            _debug(part1 + " " + part2 + " " + part3 + " " + part4);
        }
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in this class (NamedObj).  Lines are indented according to
     *  to the level argument using the static method _getIndentPrefix().
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
     *  @exception IllegalActionException Not thrown in this base class,
     *  but derived classes could throw an exception if there is a problem
     *  accessing subcomponents of this object.
     */
    protected String _description(int detail, int indent, int bracket)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            StringBuffer result = new StringBuffer(_getIndentPrefix(indent));

            if (bracket == 1 || bracket == 2) {
                result.append("{");
            }

            if ((detail & CLASSNAME) != 0) {
                result.append(getClass().getName());

                if ((detail & FULLNAME) != 0) {
                    result.append(" ");
                }
            }

            if ((detail & FULLNAME) != 0) {
                result.append("{" + getFullName() + "}");
            }

            if ((detail & ATTRIBUTES) != 0) {
                if ((detail & (CLASSNAME | FULLNAME)) != 0) {
                    result.append(" ");
                }

                result.append("attributes {\n");

                // Do not recursively list attributes unless the DEEP
                // bit is set.
                if ((detail & DEEP) == 0) {
                    detail &= ~ATTRIBUTES;
                }

                if (_attributes != null) {
                    Iterator<?> parameters = _attributes.elementList()
                            .iterator();

                    while (parameters.hasNext()) {
                        Attribute parameter = (Attribute) parameters.next();
                        result.append(parameter._description(detail,
                                indent + 1, 2) + "\n");
                    }
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

    /** Execute the specified list of change requests.
     *  @param changeRequests The list of change requests to execute.
     */
    protected void _executeChangeRequests(List<ChangeRequest> changeRequests) {
        Iterator requests = changeRequests.iterator();
        boolean previousDeferStatus = _deferChangeRequests;

        try {
            // Get write access once on the outside, to make
            // getting write access on each individual
            // modification faster.
            // NOTE: This optimization, it turns out,
            // drastically slows down execution of models
            // that do graphical animation or that change
            // parameter values during execution. Changing
            // parameter values does not require write
            // access to the workspace.
            // _workspace.getWriteAccess();

            // Defer change requests so that if changes are
            // requested during execution, they get queued.
            _deferChangeRequests = true;
            // FIXME: How do we ensure that _deferChangeRequests
            // does not get changed during the following loop?
            // It is not OK to change _changeLock, as this will
            // lead to deadlock. In particular, another thread
            // that holds read permission on the workspace
            // might try to acquire _changeLock and block,
            // and then this thread will try to get write
            // permission on the workspace, and it will block.
            while (requests.hasNext()) {
                ChangeRequest change = (ChangeRequest) requests.next();

                // The following is a bad idea because there may be
                // many fine-grain change requests in the list, and
                // notification triggers expensive operations such
                // as repairing the graph model in diva and repainting.
                // Hence, we do the notification once after all the
                // change requests have executed.  Note that this may
                // make it harder to optimize Vergil so that it
                // repaints only damaged regions of the screen.
                change.setListeners(_changeListeners);

                if (_debugging) {
                    _debug("-- Executing change request "
                            + "with description: " + change.getDescription());
                }
                change.execute();
            }
        } finally {
            // NOTE: See note above.
            // _workspace.doneWriting();

            _deferChangeRequests = previousDeferStatus;
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this base class is the attributes.  This method is called
     *  by exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  Callers of this method should hold read access before
     *  calling this method.  Note that exportMoML() does this for us.
     *
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see #exportMoML(Writer, int)
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        // If the display name has been set, then include a display element.
        // Note that copying parameters that have _displayName set need
        // to export _displayName.
        // See: http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3361
        if (_displayName != null) {
            output.write(_getIndentPrefix(depth) + "<display name=\"");
            output.write(StringUtilities.escapeForXML(_displayName));
            output.write("\"/>\n");
        }

        // Callers of this method should hold read access
        // so as to avoid ConcurrentModificationException.
        if (_attributes != null) {
            Iterator<?> attributes = _attributes.elementList().iterator();

            while (attributes.hasNext()) {
                Attribute attribute = (Attribute) attributes.next();
                attribute.exportMoML(output, depth);
            }
        }
    }

    /** Get an object with the specified name in the specified container.
     *  The type of object sought is an instance of the same class as
     *  this object.  In this base class, return null, as there
     *  is no containment mechanism. Derived classes should override this
     *  method to return an object of their same type.
     *  @param relativeName The name relative to the container.
     *  @param container The container expected to contain the object.
     *  @return null.
     *  @exception IllegalActionException If the object exists
     *   and has the wrong class. Not thrown in this base class.
     */
    protected NamedObj _getContainedObject(NamedObj container,
            String relativeName) throws IllegalActionException {
        return null;
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }

    /** Return true if describing this class in MoML is redundant.
     *  This will return true if setPersistent() has been called
     *  with argument false, irrespective of other conditions.
     *  If setPersistent() has not been called, or has been called
     *  with argument true, then things are more complicated.
     *  If the <i>depth</i> argument is 0 or if this object is
     *  not derived, then this method returns false, indicating
     *  that MoML should be exported. Otherwise, whether to export
     *  MoML depends on whether the MoML specifies information
     *  that should be created by propagation rather than explicitly
     *  represented in MoML.  If this is a derived object, then whether
     *  its information can be created by propagation depends on whether
     *  the object from which that propagation would occur is included
     *  in the MoML, which depends on the <i>depth</i> argument.
     *  This method uses the <i>depth</i> argument to determine whether
     *  the exported MoML both contains an object that implies the
     *  existence of this object and contains an object that implies
     *  the value of this object.  If both conditions are satisfied,
     *  then it returns false.  Finally, if we haven't already
     *  returned false, then check all the contained objects, and
     *  if any of them requires a MoML description, then return false.
     *  Otherwise, return true.
     *  @param depth The depth of the requested MoML.
     *  @return Return true to suppress MoML export.
     */
    protected boolean _isMoMLSuppressed(int depth) {
        // Check whether suppression of MoML has been explicitly
        // requested.
        if (_isPersistent != null) {
            return !_isPersistent.booleanValue();
        }

        // Object is persistent, but export may still not
        // be required since the structure and values might
        // be implied by inheritance. However, if that
        // inheritance occurs above the level of the hierarchy
        // where we are doing the export, then we need to
        // give structure and values anyway. Otherwise, for
        // example, copy and paste won't work properly.
        if (_derivedLevel > depth) {
            // Object is either not derived or the derivation occurs
            // above in the hierarchy where we are exporting.
            return false;
        }

        // At this point, we know the object is implied.
        // However, we may need to export anyway because it
        // may have an overridden value.
        // Export MoML if the value of the object is
        // propagated in but from outside the scope
        // of the export.
        if (_override != null) {
            // Export MoML if the value of the object is
            // propagated in but from outside the scope
            // of the export.
            if (_override.size() > depth + 1) {
                return false;
            }

            // Export MoML if the value has been set directly.
            if (_override.size() == 1 && _override.get(0).intValue() == 0) {
                return false;
            }
        }

        // If any contained object wishes to have
        // MoML exported, then this object will export MoML.
        Iterator<?> objects = containedObjectsIterator();

        while (objects.hasNext()) {
            NamedObj object = (NamedObj) objects.next();

            if (!object._isMoMLSuppressed(depth + 1)) {
                return false;
            }
        }

        // If we get here, then this object is a derived object
        // whose value is defined somewhere within the scope
        // of the export, and all its contained
        // objects do not need to export MoML. In this case,
        // it is OK to suppress MoML.
        return true;
    }

    /** Mark the contents of this object as being derived objects.
     *  Specifically, the derivation depth of the immediately contained
     *  objects is set to one greater than the <i>depth</i> argument,
     *  and then this method is called on that object with an argument
     *  one greater than the <i>depth</i> argument. For the contained
     *  objects, this will also cancel any previous call to
     *  setPersistent(true), since it's a derived object.
     *  @param depth The derivation depth for this object, which
     *   should be 0 except on recursive calls.
     *  @see #setDerivedLevel(int)
     */
    protected void _markContentsDerived(int depth) {
        depth = depth + 1;

        Iterator<?> objects = lazyContainedObjectsIterator();

        while (objects.hasNext()) {
            NamedObj containedObject = (NamedObj) objects.next();
            // NOTE: Do not invoke setDerivedLevel() because that
            // method nulls the _override field.
            // containedObject.setDerivedLevel(depth);
            if (depth < containedObject._derivedLevel) {
                containedObject._derivedLevel = depth;
            }
            containedObject._markContentsDerived(depth);

            // If this object has previously had
            // persistence set to true (e.g., it was
            // cloned from an object that had persistence
            // set to true), then override that and
            // reset to where persistence is unspecified.
            if (containedObject._isPersistent != null
                    && containedObject._isPersistent.booleanValue()) {
                containedObject._isPersistent = null;
            }
        }
    }

    /** If any hierarchy listeners are registered, notify them
     *  that a change has occurred in the hierarchy.
     *  @exception IllegalActionException If the change to the
     *   hierarchy is not acceptable to the listener.
     *  @see #addHierarchyListener(HierarchyListener)
     *  @see HierarchyListener
     */
    protected void _notifyHierarchyListenersAfterChange()
            throws IllegalActionException {
        if (_hierarchyListeners != null) {
            // The hierarchy has changed. Add all hierarchy listeners
            // up the new hierarchy. This should be done before notification
            // because notification may result in exceptions.
            NamedObj container = getContainer();
            if (container != null) {
                for (HierarchyListener listener : _hierarchyListeners) {
                    container.addHierarchyListener(listener);
                }
            }

            for (HierarchyListener listener : _hierarchyListeners) {
                listener.hierarchyChanged();
            }
        }
    }

    /** If any hierarchy listeners are registered, notify them
     *  that a change is about to occur in the hierarchy.
     *  @exception IllegalActionException If changing the
     *   hierarchy is not acceptable to the listener.
     *  @see #addHierarchyListener(HierarchyListener)
     *  @see HierarchyListener
     */
    protected void _notifyHierarchyListenersBeforeChange()
            throws IllegalActionException {
        if (_hierarchyListeners != null) {
            for (HierarchyListener listener : _hierarchyListeners) {
                listener.hierarchyWillChange();
            }
            // If changing the hierarchy is acceptable to all listeners,
            // then we get to here. At this point, we should remove all
            // listeners from containers above us in the hierarchy, to
            // re-add them after the change in the hierarchy is complete.
            NamedObj container = getContainer();
            if (container != null) {
                for (HierarchyListener listener : _hierarchyListeners) {
                    container.removeHierarchyListener(listener);
                }
            }
        }
    }

    /** Propagate existence of this object to the
     *  specified object. The specified object is required
     *  to be an instance of the same class as the container
     *  of this one, or an exception will be thrown. In this
     *  base class, this object is cloned, and its name
     *  is set to the same as this object.
     *  Derived classes with a setContainer() method are
     *  responsible for ensuring that this returned object
     *  has its container set to the specified container.
     *  This base class ensures that the returned object
     *  is in the same workspace as the container.
     *  <p>
     *  NOTE: Any object that creates objects in its
     *  constructor that it does not contain must override
     *  this method and call propagateExistence() on those
     *  objects. Otherwise, those objects will not be
     *  propagated to subclasses or instances when this
     *  object is contained by a class definition.
     *  See PortParameter for an example.
     *  @param container Object to contain the new object.
     *  @exception IllegalActionException If the object
     *   cannot be cloned.
     *  @return A new object of the same class and name
     *   as this one.
     */
    protected NamedObj _propagateExistence(NamedObj container)
            throws IllegalActionException {
        try {
            // Look for error condition.
            if (container == null) {
                throw new IllegalActionException(this,
                        "Attempting to propagate into a null container");
            }
            return (NamedObj) clone(container.workspace());
        } catch (CloneNotSupportedException e) {
            throw new IllegalActionException(this, e,
                    "Failed to propagate instance.");
        }
    }

    /** Propagate the value of this object (if any) to the
     *  specified object. The specified object is required
     *  to be an instance of the same class as this one, or
     *  an exception will be thrown. In this base class,
     *  there is no value, and so nothing needs to be done.
     *  Derived classes that have values should override
     *  this method.
     *  @param destination Object to which to propagate the
     *   value.
     *  @exception IllegalActionException If the value cannot
     *   be propagated.
     */
    protected void _propagateValue(NamedObj destination)
            throws IllegalActionException {
    }

    /** Remove the given attribute.
     *  If there is no such attribute, do nothing.
     *  This method is write-synchronized on the workspace and increments its
     *  version. It should only be called by setContainer() in Attribute.
     *  @param param The attribute to be removed.
     */
    protected void _removeAttribute(Attribute param) {
        try {
            _workspace.getWriteAccess();
            _attributes.remove(param);

            if (_debugging) {
                _debug("Removed attribute", param.getName(), "from",
                        getFullName());
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Remove attribute from list of attributes.
     *  @param param Attribute to remove.
     */
    public void removeAttribute(Attribute param) {
        _removeAttribute(param);
    }

    /** Split the specified name at the first period and return the
     *  two parts as a two-element array.  If there is no period, the second
     *  element is null.
     *  @param name The name to split.
     *  @return The name before and after the first period as a two-element
     *   array.
     */
    protected static final String[] _splitName(String name) {
        String[] result = new String[2];
        int period = name.indexOf(".");

        if (period < 0) {
            result[0] = name;
        } else {
            result[0] = name.substring(0, period);
            result[1] = name.substring(period + 1);
        }

        return result;
    }

    /** Return a string that is identical to the specified string
     *  except any trailing digits are removed.
     *  @param string The string to strip of its numeric suffix.
     *  @return A string with no numeric suffix.
     */
    protected static String _stripNumericSuffix(String string) {
        int length = string.length();
        char[] chars = string.toCharArray();

        for (int i = length - 1; i >= 0; i--) {
            char current = chars[i];

            if (Character.isDigit(current)) {
                length--;
            } else {
                //if (current == '_') {
                //    length--;
                //}
                // Found a non-numeric, so we are done.
                break;
            }
        }

        if (length < string.length()) {
            // Some stripping occurred.
            char[] result = new char[length];
            System.arraycopy(chars, 0, result, 0, length);
            return new String(result);
        } else {
            return string;
        }
    }

    /** Validate attributes deeply contained by this object if they
     *  implement the Settable interface by calling their validate() method.
     *  Errors that are triggered by this validation are handled by calling
     *  handleModelError().
     *  @param attributesValidated A collection of Settables that have
     *  already been validated.  For example, Settables that implement
     *  the ShareableSettable interface are validated only once.
     *  @see #handleModelError(NamedObj context, IllegalActionException exception)
     *  @exception IllegalActionException If there is a problem validating
     *  the deeply contained attributes.
     */
    protected void _validateSettables(Collection attributesValidated)
            throws IllegalActionException {
        Iterator<Settable> attributes = attributeList(Settable.class)
                .iterator();
        while (attributes.hasNext()) {
            Settable attribute = attributes.next();
            if (attributesValidated.contains(attribute)) {
                continue;
            }
            try {
                Collection<Settable> validated = attribute.validate();
                if (validated != null) {
                    attributesValidated.addAll(validated);
                }
                attributesValidated.add(attribute);
            } catch (IllegalActionException ex) {
                if (!handleModelError(this, ex)) {
                    throw ex;
                }
            }
            ((NamedObj) attribute)._validateSettables(attributesValidated);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A list of weak references to change listeners. */
    protected List _changeListeners;

    /** Object for locking accesses to change request list and status.
     *  NOTE: We could have used _changeRequests or _changeListeners,
     *  but those lists are only created when needed.  A simple
     *  Object here is presumably cheaper than a list, but it is
     *  truly unfortunate to have to carry this in every NamedObj.
     */
    protected Object _changeLock = new SerializableObject();

    /** A list of pending change requests. */
    protected List _changeRequests;

    /** Flag that is true if there are debug listeners. */
    protected boolean _debugging = false;

    /** The list of DebugListeners registered with this object.
     *  NOTE: Because of the way we synchronize on this object, it should
     *  never be reset to null after the first list is created.
     */
    protected LinkedList _debugListeners = null;

    /** Flag indicating that we should not immediately
     *  execute a change request.
     */
    protected transient boolean _deferChangeRequests = false;

    /** The MoML element name. This defaults to "entity".
     *  Subclasses that wish this to be different should set it
     *  in their constructor, or override getElementName()
     *  to return the desired value.
     */
    protected String _elementName = "entity";

    /** Boolean variable to indicate the persistence of the object.
     *  If this is null (the default), then instances of NamedObj are
     *  persistent unless they are inferrable through propagation.
     *  We use Boolean here rather than boolean because a null value
     *  is used to indicate that no persistence has been specified.
     */
    protected Boolean _isPersistent = null;

    /** The workspace for this object.
     *  This should be set by the constructor and never changed.
     */
    protected Workspace _workspace;

    /** Flag that is true if detailed debug information is necessary.
     */
    protected boolean _verbose = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a list of derived objects. If the <i>propagate</i>
     *  argument is true, then this list will contain only those derived
     *  objects whose values are not overridden and that are not
     *  shadowed by objects whose values are overridden. Also, if
     *  that argument is true, then the value of this object is
     *  propagated to those returned objects during the construction
     *  of the list. This method is read-synchronized on the workspace.
     *  If the <i>force</i> argument is true, then if an expected
     *  derived object does not exist, then it is created by calling
     *  the _propagateExistence() protected method.
     *  @param visited A set of objects that have previously been
     *   visited. This should be non-null only on the recursive calls
     *   to this method.
     *  @param propagate True to propagate the value of this object
     *   (if any) to derived objects that have not been overridden
     *   while the list is being constructed.
     *  @param force Force derived objects to exist where they should
     *   be if they do not already exist.
     *  @param context The context (this except in recursive calls).
     *  @param depth The depth (0 except in recursive calls).
     *  @param relativeName The name of the object relative to the
     *   context (null except in recursive calls).
     *  @param override The list of override breadths (one per depth).
     *   If propagate is true, then this should be a list with
     *   a single Integer 0 for outside callers, and otherwise it
     *   should be null.
     *  @return A list of instances of the same class as this object
     *   which are derived from
     *   this object. The list is empty in this base class, but
     *   subclasses that override _getContainedObject() can
     *   return non-empty lists.
     *  @exception IllegalActionException If propagate is true
     *   and propagation fails.
     */
    private List<NamedObj> _getDerivedList(Collection<NamedObj> visited,
            boolean propagate, boolean force, NamedObj context, int depth,
            List<Integer> override, String relativeName)
                    throws IllegalActionException {
        try {
            workspace().getReadAccess();

            LinkedList<NamedObj> result = new LinkedList<NamedObj>();

            // We may have visited this container already, in
            // which case the propagation has occurred already.
            // It should not occur again because the first occurrence
            // would have been from an object that propagated to
            // this occurrence. A similar propagation will occur
            // in propagation from the container, and for propageting
            // to work in that context, we must not issue the
            // change from here.  There is also no need to
            // go any further up the containment tree, since
            // the previous occurrence would have taken care of that.
            if (visited == null) {
                visited = new HashSet<NamedObj>();
            } else {
                if (visited.contains(context)) {
                    return result;
                }
            }

            visited.add(context);

            // Need to do deepest propagations
            // (those closest to the root of the tree) first.
            NamedObj container = context.getContainer();

            if (container != null) {
                String newRelativeName;

                if (relativeName == null) {
                    newRelativeName = context.getName();
                } else {
                    newRelativeName = context.getName() + "." + relativeName;
                }

                // Create a new override list to pass to the container.
                List<Integer> newOverride = null;

                if (propagate) {
                    newOverride = new LinkedList<Integer>(override);

                    // If the override list is not long enough for the
                    // new depth, make it long enough. It should at most
                    // be one element short.
                    if (newOverride.size() <= depth + 1) {
                        newOverride.add(Integer.valueOf(0));
                    }
                }

                result.addAll(_getDerivedList(visited, propagate, force,
                        container, depth + 1, newOverride, newRelativeName));
            }

            if (!(context instanceof Instantiable)) {
                // This level can't possibly defer, so it has
                // nothing to add.
                return result;
            }

            // Extract the current breadth from the list.
            int myBreadth = 0;

            if (propagate) {
                myBreadth = override.get(depth).intValue();
            }

            // Iterate over the children.
            List<?> othersList = ((Instantiable) context).getChildren();
            if (othersList != null) {
                Iterator<?> others = othersList.iterator();

                while (others.hasNext()) {
                    WeakReference<?> reference = (WeakReference<?>) others
                            .next();
                    NamedObj other = (NamedObj) reference.get();
                    if (other != null) {
                        // Found a deferral.
                        // Look for an object with the relative name.
                        NamedObj candidate = other;

                        if (relativeName != null) {
                            candidate = _getContainedObject(other, relativeName);
                        }

                        if (candidate == null) {
                            if (force) {
                                // Need to get the container.
                                // Is there a better way than parsing
                                // the relativeName?
                                NamedObj remoteContainer = other;
                                int lastPeriod = relativeName.lastIndexOf(".");

                                if (lastPeriod > 0) {
                                    String containerName = relativeName
                                            .substring(0, lastPeriod);
                                    // NOTE: The following may return null
                                    // if the propagation hasn't occurred yet.
                                    // This happens when invoking createHierarchy
                                    // in classes. It is a bug if propagation of the
                                    // container hasn't happened.
                                    remoteContainer = getContainer()
                                            ._getContainedObject(other,
                                                    containerName);
                                }

                                candidate = _propagateExistence(remoteContainer);

                                // Indicate that the existence of the
                                // candidate is implied by a
                                // parent-child relationship at the
                                // current depth. NOTE: Do not use setDerivedLevel()
                                // because that method resets the _override field.
                                // candidate.setDerivedLevel(depth);
                                if (depth < candidate._derivedLevel) {
                                    candidate._derivedLevel = depth;
                                }

                                candidate._markContentsDerived(depth);
                                candidate._adjustOverride(depth);
                            } else {
                                // No candidate and no error.
                                // We can reach this line if this method
                                // is called during construction of an object
                                // in a class definition, before propagation has
                                // occurred. For example, some constructors call
                                // moveToLast() or moveToFirst() on attributes.
                                // These methods normally propagate the change
                                // to instances and derived classes. But if
                                // those instances and derived classes have not
                                // yet had the object propagated to them, then
                                // this will fail.
                                continue;
                                /* NOTE: This used to throw the following exception,
                                 * but this exception was spurious.
                                 * To test, create a class an an instance, then
                                 * drop an SDFDirector into the class. This used
                                 * to result in this exception being thrown.
                                 throw new InternalErrorException("Expected "
                                 + other.getFullName()
                                 + " to contain an object named "
                                 + relativeName + " of type "
                                 + getClass().toString());
                                 */
                            }
                        }

                        // We may have done this already.  Check this
                        // by finding the object that will be affected by
                        // this propagation.
                        if (visited.contains(candidate)) {
                            // Skip this candidate. We've done it already.
                            // Continue to the next deferral in the list.
                            continue;
                        }

                        List<Integer> newOverride = null;

                        // If the propagate argument is true, then
                        // determine whether the candidate object is
                        // shadowed, and if it is not, then apply the
                        // propagation change to it.
                        if (propagate) {
                            // Is it shadowed?  Create a new override
                            // list to pass to the candidate.
                            newOverride = new LinkedList<Integer>(override);
                            newOverride.set(depth,
                                    Integer.valueOf(myBreadth + 1));

                            if (_isShadowed(candidate._override, newOverride)) {
                                // Yes it is.
                                continue;
                            }

                            // FIXME: If the following throws an
                            // exception, we have to somehow restore
                            // values of previous propagations.
                            _propagateValue(candidate);

                            // Set the override.
                            candidate._override = newOverride;
                        }

                        result.add(candidate);

                        // Add objects derived from this candidate.
                        // Note that depth goes back to zero, since the
                        // existence of objects derived from this candidate
                        // will be determined by the depth of propagation from
                        // this candidate.
                        result.addAll(candidate._getDerivedList(visited,
                                propagate, force, candidate, 0, newOverride,
                                null));

                        // Note that the above recursive call will
                        // add the candidate to the HashSet, so we
                        // don't have to do that here.
                    }
                }
            }

            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return true if the first argument (an _override list)
     *  indicates that the object owning that override list should
     *  be shadowed relative to a change made via the path defined by
     *  the second override list.
     *  @param candidate The override list of the candidate for a change.
     *  @param changer The override list for a path for the change.
     *  @return True if the candidate is shadowed.
     */
    private boolean _isShadowed(List<Integer> candidate, List<Integer> changer) {
        if (candidate == null) {
            return false;
        }

        if (changer == null) {
            // Probably it makes no sense for the second argument
            // to be null, but in case it is, we declare that there
            // is shadowing if it is.
            return true;
        }

        // If the the candidate object has a value that has been
        // set more locally (involving fewer levels of the hierarchy)
        // than the proposed changer path, then it is shadowed.
        if (candidate.size() < changer.size()) {
            return true;
        }

        // If the sizes are equal, then we need to compare the
        // elements of the list, starting with the last.
        if (candidate.size() == changer.size()) {
            int index = candidate.size() - 1;

            while (index >= 0
                    && candidate.get(index).equals(changer.get(index))) {
                index--;
            }

            if (index < 0) {
                // The two lists are identical, so there be no shadowing.
                return false;
            } else {
                int candidateBreadth = candidate.get(index).intValue();
                int changerBreadth = changer.get(index).intValue();

                if (candidateBreadth < changerBreadth) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Update the decorator attributes.
     *  This method finds all decorators in scope (above this object in the hierarchy),
     *  and for each decorator that can decorate this object, creates an entry in
     *  _decoratorAttributes.
     *  @see ptolemy.kernel.util.Decorator#createDecoratorAttributes(NamedObj)
     *  @see #getDecoratorAttributes(Decorator)
     *  @exception IllegalActionException If thrown while checking to
     *  see if the decorator is global, while getting the decorator or
     *  while creating a decorator.
     */
    private void _updateDecoratorAttributes() throws IllegalActionException {
        synchronized (_decoratorAttributes) {
            if (workspace().getVersion() != _decoratorAttributesVersion) {
                _decoratorAttributes.clear();
                // Find all the decorators in scope, and store them indexed by full name.
                Set<Decorator> decorators = new HashSet<Decorator>();
                NamedObj container = getContainer();
                boolean crossedOpaqueBoundary = false;
                while (container != null) {
                    List<Decorator> localDecorators = container
                            ._containedDecorators();
                    for (Decorator decorator : localDecorators) {
                        if (!crossedOpaqueBoundary
                                || decorator.isGlobalDecorator()) {
                            decorators.add(decorator);
                        }
                    }
                    if (container instanceof CompositeEntity
                            && ((CompositeEntity) container).isOpaque()) {
                        crossedOpaqueBoundary = true;
                    }
                    container = container.getContainer();
                }

                // Find all the instances of DecoratorAttributes contained by this NamedObj,
                // and put these in the cache, associated with the right decorator.
                List<DecoratorAttributes> decoratorAttributes = attributeList(DecoratorAttributes.class);
                for (DecoratorAttributes decoratorAttribute : decoratorAttributes) {
                    Decorator decorator = decoratorAttribute.getDecorator();
                    // If the decorator is not found, decorator will be null.
                    // In that case, we leave the decoratorAttributes for now (in case the
                    // decorator reappears, e.g. through an undo). When the model is saved,
                    // decoratorAttributes will disappear.
                    if (decorator != null) {
                        // Since this decorator is now associated with a decoratorAttribute, remove it.
                        boolean removed = decorators.remove(decorator);
                        // If the above returns null, then the decorator is no longer in scope, so
                        // we do not add it to the cache. We do not want to remove the decoratorAttributes,
                        // however, until the model is saved.
                        if (removed) {
                            // The decorator was found. Put in cache.
                            _decoratorAttributes.put(decorator,
                                    decoratorAttribute);
                        }
                    }
                }

                // For each remaining decorator, if it decorates this NamedObj, create an entry.
                for (Decorator decorator : decorators) {
                    DecoratorAttributes attribute = decorator
                            .createDecoratorAttributes(this);
                    _decoratorAttributes.put(decorator, attribute);
                }

                _decoratorAttributesVersion = workspace().getVersion();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         friendly variables                 ////
    // The following is friendly to support the move* methods of
    // Attribute.

    /** The Attributes attached to this object. */
    NamedList _attributes;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The class name for MoML exports. */
    private String _className;

    /** A map from decorators to the decorated attributes which which each decorator has decorated this NamedObj.
     *  This is a cache that may not be complete. To get a complete list of decorated attributes, get a list
     *  of attributes of type DecoratorAttributes.
     */
    private Map<Decorator, DecoratorAttributes> _decoratorAttributes = new HashMap<Decorator, DecoratorAttributes>();

    /** Workspace version for decorated attributes. */
    private long _decoratorAttributesVersion = -1L;

    /** Instance of a workspace that can be used if no other
     *  is specified.
     */
    private static Workspace _DEFAULT_WORKSPACE = new Workspace();

    // Variable indicating at what level above this object is derived.
    // Integer.MAX_VALUE indicates that it is not derived.
    private int _derivedLevel = Integer.MAX_VALUE;

    /** The display name, if set. */
    private String _displayName;

    // Cached value of the full name.
    private String _fullNameCache;

    // Version of the workspace when cache last updated.
    private long _fullNameVersion = -1;

    /** List of hierarchy listeners, if any. */
    private Set<HierarchyListener> _hierarchyListeners;

    // The model error handler, if there is one.
    private ModelErrorHandler _modelErrorHandler = null;

    /** The name */
    private String _name;

    /** List indicating whether and how this derived
     *  object has been modified.
     */
    private List<Integer> _override = null;

    /** The value for the source MoML attribute. */
    private String _source;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class is an iterator over all the contained objects
     *  (all instances of NamedObj). In this base class, the contained
     *  objects are attributes.  In derived classes, they include
     *  ports, relations, and entities as well.
     */
    protected class ContainedObjectsIterator implements Iterator {

        /** Create an iterator over all the contained objects. */
        public ContainedObjectsIterator() {
            super();
            // This iterator gets called quite a bit, so at Kevin Ruland's
            // suggestion, we move instantiation of the iterator
            // into the constructor so that hasNext() and next() don't
            // have to check if _attributeListIterator is null each time.
            _attributeListIterator = attributeList().iterator();
        }

        /** Return true if the iteration has more elements.
         *  In this base class, this returns true if there are more
         *  attributes.
         *  @return True if there are more attributes.
         */
        @Override
        public boolean hasNext() {
            return _attributeListIterator.hasNext();
        }

        /** Return the next element in the iteration.
         *  In this base class, this is the next attribute.
         *  @return The next attribute.
         */
        @Override
        public Object next() {
            return _attributeListIterator.next();
        }

        /** Throw a UnsupportedOperationException because remove() is not
         *  supported.  The reason is because this iterator calls
         *  attributeList().iterator(), which returns a NamedList that
         *  is unmodifiable.
         */
        @Override
        public void remove() {
            // Iterator requires a remove().
            throw new UnsupportedOperationException("remove() not supported "
                    + "because attributeList().iterator() returns a NamedList "
                    + "that is unmodifiable");
            //_attributeListIterator.remove();
        }

        private Iterator<?> _attributeListIterator = null;
    }

    /** Serializable version of the Java Object class. */
    @SuppressWarnings("serial")
    private static class SerializableObject extends Object implements
    Serializable {
        // FindBugs suggested making this class a static inner class:
        //
        // "This class is an inner class, but does not use its embedded
        // reference to the object which created it. This reference makes
        // the instances of the class larger, and may keep the reference
        // to the creator object alive longer than necessary. If
        // possible, the class should be made into a static inner class."
    }
}
