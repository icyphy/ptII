/* Base class for objects with a name and a container.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
FIXME: Need review of _classElement related changes.  Look
for comments with:
     EAL 12/03
     Corresponding changes are made in:
     kernel.Port
     kernel.ComponentEntity
     kernel.Relation
     (all in their setContainer() methods)
     moml.MoMLParser
FIXME: Need review of:
     isDeferChangeRequests()
     requestChange()
     executeChangeRequests()
     setDeferChangeRequests()
@AcceptedRating Yellow (eal@eecs.berkeley.edu)

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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// NamedObj
/**
This is a base class for objects with a name. A simple name is an arbitrary
string. If no simple name is provided, the name is taken to be an
empty string (not a null reference). The class also has a full name,
which is a concatenation of the container's full name and the simple
name, separating by a period. Obviously, if the simple name contains
a period then there may be some confusion resolving the full name,
so periods are expressly disallowed. If there is no
container, then the full name is a concatenation of the workspace
name with the simple name. The
full name is used for error reporting throughout the package, which
is why it is supported at this low level of the class hierarchy.
<p>
Instances of this class are associated with a workspace, specified
as a constructor argument.  The workspace is immutable.  It cannot
be changed during the lifetime of the object.  It is used for
synchronization of methods that depend on or modify the state of
objects within it.  It is also used for tracking the version of any
topology within the workspace.
Any method in this class or derived classes that changes visible
state of any object within the workspace should call the
incrVersion() method of the workspace.  If no workspace is
specified, then the default workspace is used.
Note that the workspace should not be confused with the container.
The workspace never serves as a container.
<p>
In this base class, the container is null by default, and no method is
provided to change it. Derived classes that support hierarchy provide one
or more methods that set the container.
<p>
By convention, if the container of
a NamedObj is set, then it should be removed from the workspace directory,
if it is present.  The workspace directory is expected
to list only top-level objects in a hierarchy.
The NamedObj can still use the workspace for synchronization.
Any object contained by another uses the workspace of its container
as its own workspace by default.
<p>
Instances of Attribute can be attached by calling their setContainer()
method and passing this object as an argument. These instances will then
be reported by the getAttribute() and attributeList() methods.
Classes derived from NamedObj may constrain attributes to be a subclass
of Attribute.  To do that, they should override the protected
_addAttribute() method to throw an exception if the
object provided is not of the right class.
<p>
This class supports <i>mutations</i>, which are changes to a model
that are performed in a disciplined fashion.  In particular, a
mutation can be requested via the requestChange() method.
Derived classes will ensure that the mutation is executed
at a time when it is safe to execute mutations.
<p>
This class supports the notion of a <i>model error</i>, which is an
exception that is handled by a registered model error handler,
or passed up the container hierarchy if there is no registered model
error handler.  This mechanism complements the exception mechanism
in Java. Instead of unraveling the calling stack to handle exceptions,
this mechanism passes control up the Ptolemy II hierarchy.
<p>
Derived classes should override the _description() method to append
new fields if there is new information that should be included in the
description.

@author Mudit Goel, Edward A. Lee, Neil Smyth
@version $Id$
@since Ptolemy II 0.2
*/

public class NamedObj implements Nameable, Debuggable, DebugListener,
                                 Serializable, Cloneable {

    // Note that Nameable extends ModelErrorHandler, so this class
    // need not declare that it directly implements ModelErrorHandler.

    /** Construct an object in the default workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NamedObj() {
        this((Workspace)null);
    }

    /** Construct an object in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public NamedObj(String name)
            throws IllegalActionException {
        this(_DEFAULT_WORKSPACE, name);
    }

    /** Construct an object in the specified workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
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
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
        }
        try {
            setName("");
        } catch (KernelException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
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
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
        }
        try {
            setName(name);
        } catch (NameDuplicationException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
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
     */
    public void addChangeListener(ChangeListener listener) {
        NamedObj container = (NamedObj) getContainer();
        if (container != null) {
            container.addChangeListener(listener);
        } else {
            synchronized(_changeLock) {
                if (_changeListeners == null) {
                    _changeListeners = new LinkedList();
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
     */
    public void addDebugListener(DebugListener listener) {
        // NOTE: This method needs to be synchronized to prevent two
        // threads from each creating a new _debugListeners list.
        synchronized(this) {
            if (_debugListeners == null) {
                _debugListeners = new LinkedList();
            }
        }
        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized(_debugListeners) {
            if (_debugListeners.contains(listener)) {
                return;
            } else {
                _debugListeners.add(listener);
            }
            _debugging = true;
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
            throws IllegalActionException {}

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
     *  @return A list of instances of specified class.
     */
    public List attributeList(Class filter) {
        try {
            _workspace.getReadAccess();
            if (_attributes == null) {
                _attributes = new NamedList();
            }
            List result = new LinkedList();
            Iterator attributes = _attributes.elementList().iterator();
            while (attributes.hasNext()) {
                Object attribute = attributes.next();
                if (filter.isInstance(attribute)) {
                    result.add(attribute);
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
            throws IllegalActionException {}

    /** Clone the object into the current workspace by calling the clone()
     *  method that takes a Workspace argument.
     *  This method read-synchronizes on the workspace.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return clone(_workspace);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there). This uses the clone() method of
     *  java.lang.Object, which makes a field-by-field copy.
     *  It then adjusts the workspace reference and clones the
     *  attributes on the attribute list, if there is one.  The attributes
     *  are set to the attributes of the new object.  In addition,
     *  if this object has the MoML element name "class", as determined
     *  by elementName field of the associated MoMLInfo object,
     *  then the new object will not export
     *  its contents when exportMoML() is called, but rather will
     *  declare that it extends this one, and will export only its attributes.
     *  This method read-synchronizes on the workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #getMoMLInfo()
     *  @see #exportMoML(Writer, int, String)
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
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
            NamedObj newObject = (NamedObj)super.clone();
            // NOTE: It is not necessary to write-synchronize on the other
            // workspace because this only affects its directory, and methods
            // to access the directory are synchronized.
            newObject._attributes = null;
            
            // Make sure the new object is not marked as a class.
            // It may have been cloned from a class.
            newObject._isClassElement = false;
            
            if(workspace == null) {
                newObject._workspace = _DEFAULT_WORKSPACE;
            } else {
                newObject._workspace = workspace;
            }
            newObject._fullNameVersion = -1;

            if (_attributes != null) {
                Iterator parameters = _attributes.elementList().iterator();
                while (parameters.hasNext()) {
                    Attribute parameter = (Attribute)parameters.next();
                    Attribute newParameter
                        = (Attribute)parameter.clone(workspace);
                    try {
                        newParameter.setContainer(newObject);
                    } catch (KernelException exception) {
                        throw new CloneNotSupportedException(
                                "Failed to clone attribute "
                                + parameter.getFullName()
                                + ": "
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
            if (_MoMLInfo != null) {
                newObject._MoMLInfo = new MoMLInfo(newObject);
                newObject._MoMLInfo.elementName = _MoMLInfo.elementName;
                newObject._MoMLInfo.source = _MoMLInfo.source;
                // The new object does not have any other objects deferring
                // their MoML definitions to it, so we have to reset this.
                newObject._MoMLInfo.deferredFrom = null;

                // If the master defers its MoML definition to
                // another object, then so will the clone.
                // So we have to add the clone to the list of objects
                // in the object deferred to.
                if (_MoMLInfo.deferTo != null) {
                    _MoMLInfo.deferTo._MoMLInfo.getDeferredFrom().add(
                            new WeakReference(newObject));
                }

                // NOTE: The value for the classname and superclass isn't
                // correct if this cloning operation is meant to create
                // an extension rather than a clone.  A clone has exactly
                // the same className and superclass as the master.
                // It is up to the caller to correct these fields if
                // that is the case.  It cannot be done here because
                // we don't know the name of the new class.
                newObject._MoMLInfo.className = _MoMLInfo.className;
                newObject._MoMLInfo.superclass = _MoMLInfo.superclass;

                /* NOTE: This is what we used to do, which isn't right
                   because we don't know whether the class is being
                   cloned or extended.
                   // If the master is a class, then the name of the master
                   // becomes the class name of the instance.
                   if (getMoMLInfo().elementName.equals("class")) {
                   newObject._setDeferMoMLDefinitionTo(this);
                   newObject._MoMLInfo.className = getFullName();
                   } else {
                   newObject._MoMLInfo.className = _MoMLInfo.className;
                   }
                */
            }

            _cloneFixAttributeFields(newObject);
         
            return newObject;
        } finally {
            _workspace.doneReading();
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
     *  @return True if this contains the argument, directly or indirectly.
     */
    public boolean deepContains(NamedObj inside) {
        try {
            _workspace.getReadAccess();
            // Start with the inside and check its containers in sequence.
            if (inside != null) {
                if (_workspace != inside._workspace) return false;
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
     *  @see #exportMoML(Writer, int, String)
     */
    public String description() {
        return description(COMPLETE);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in this class (NamedObj).  This method returns an empty
     *  string (not null) if there is nothing to report.
     *  It read-synchronizes on the workspace.
     *  @param detail The level of detail.
     *  @return A description of the object.
     *  @see #exportMoML(Writer, int, String)
     */
    public String description(int detail) {
        return _description(detail, 0, 0);
    }

    /** React to the given debug event by relaying to any registered
     *  debug listeners.
     *  @param event The event.
     *  @since Ptolemy II 2.3
     */
    public void event(DebugEvent event) {
        if (_debugging) {
            _debug(event);
        }
    }

    /** Execute requested changes. If there is a container, then
     *  delegate the request to the container.  Otherwise,
     *  this method will execute the
     *  pending changes even if setDeferChangeRequests() has been
     *  called with a true argument.  Listeners will be notified
     *  of success or failure.
     *  @see #addChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see #setDeferChangeRequests(boolean)
     */
    public void executeChangeRequests() {
        NamedObj container = (NamedObj) getContainer();
        if (container != null) {
            container.executeChangeRequests();
            return;
        }
        synchronized(_changeLock) {
            if (_changeRequests != null && _changeRequests.size() > 0) {
                // Copy the change requests lists because it may
                // be modified during execution.
                LinkedList copy = new LinkedList(_changeRequests);

                // Remove the changes to be executed.
                // We remove them even if there is a failure because
                // otherwise we could get stuck making changes that
                // will continue to fail.
                _changeRequests.clear();

                Iterator requests = copy.iterator();
                boolean previousDeferStatus = _deferChangeRequests;
                try {
                    // Get write access once on the outside, to make
                    // getting write access on each individual
                    // modification faster.
                    _workspace.getWriteAccess();

                    // Defer change requests so that if changes are
                    // requested during execution, they get queued.                    
                    setDeferChangeRequests(true);
                    while (requests.hasNext()) {
                        ChangeRequest change = (ChangeRequest)requests.next();
                        change.setListeners(_changeListeners);
                        if (_debugging) {
                            _debug("-- Executing change request "
                                    + "with description: "
                                    + change.getDescription());
                        }
                        change.execute();
                    }
                } finally {
                    _workspace.doneWriting();
                    setDeferChangeRequests(previousDeferStatus);
                }
                
                // Change requests may have been queued during the execute.
                // Execute those by a recursive call.
                executeChangeRequests();
            }
        }
    }
    
    /** Get a MoML description of this object.  This might be an empty string
     *  if there is no MoML description of this object or if this object is
     *  not persistent or if this object is a class element.  This uses the
     *  three-argument version of this method.  It is final to ensure that
     *  derived classes only need to override that method to change
     *  the MoML description.
     *  @return A MoML description, or an empty string if there is none.
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #isClassElement()
     */
    public final String exportMoML() {
        try {
            StringWriter buffer = new StringWriter();
            // ptolemy.moml.MoMLWriter writer =
            //    new ptolemy.moml.MoMLWriter(buffer);
            //writer.write(this);
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
     *  is not persistent, or this object a class element.  This uses the
     *  three-argument version of this method.  It is final to ensure that
     *  derived classes only override that method to change
     *  the MoML description.
     *  @return A MoML description, or the empty string if there is none.
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #isClassElement()
     */
    public final String exportMoML(String name) {
        try {
            // If the object is not persistent, return null.
            if (!isPersistent() || isClassElement()) {
                return "";
            }
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
     *  is not persistent, or if this object is a class element,
     *  then nothing is written. To write to standard out, do
     *  <pre>
     *      exportMoML(new OutputStreamWriter(System.out))
     *  </pre>
     *  This method uses the three-argument
     *  version of this method.  It is final to ensure that
     *  derived classes only need to override that method to change
     *  the MoML description.
     *  @exception IOException If an I/O error occurs.
     *  @param output The stream to write to.
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #isClassElement()
     */
    public final void exportMoML(Writer output) throws IOException {
        // If the object is not persistent, do nothing.
        if (!isPersistent() || isClassElement()) {
            return;
        }
        exportMoML(output, 0);
    }

    /** Write a MoML description of this entity with the specified
     *  indentation depth.  This calls the three-argument version of
     *  this method with getName() as the third argument.
     *  This method is final to ensure that
     *  derived classes only override the three-argument method to change
     *  the MoML description.
     *  If the ojbect is not persistent, or if there is no MoML description,
     *  or if this object is a class instance, then write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see #exportMoML(Writer, int, String)
     *  @see #isPersistent()
     *  @see #isClassElement()
     */
    public final void exportMoML(Writer output, int depth) throws IOException {
        // If the object is not persistent, do nothing.
        if (!isPersistent() || isClassElement()) {
            return;
        }
        exportMoML(output, depth, getName());
    }

    /** Write a MoML description of this entity with the specified
     *  indentation depth and with the specified name substituting
     *  for the name of this entity.  The element name, class, and
     *  source attributes are determined by the instance of MoMLInfo
     *  returned by getMoMLInfo(). The description has one of two
     *  forms, depending on the element name. If it is "class", then
     *  the exported MoML looks like this:
     *  <pre>
     *      &lt;class name="<i>name</i>" extends="<i>classname</i> source="<i>source</i>"&gt;
     *          <i>body, determined by _exportMoMLContents()</i>
     *      &lt;/class&gt;
     *  </pre>
     *  If it is anything else (call what it returns
     *  <i>element</i>), then the exported MoML looks like this:
     *  <pre>
     *      &lt;<i>element</i> name="<i>name</i>" class="<i>classname</i>" source="<i>source</i>"&gt;&gt;
     *          <i>body, determined by _exportMoMLContents()</i>
     *      &lt;/<i>element</i>&gt;
     *  </pre>
     *  By default, the element name is "entity."  The default class name
     *  is the Java classname of this instance.
     *  The source attribute is by default left off altogether.
     *  <p>
     *  If this object has been cloned from another whose element name
     *  is "class", then the body contains
     *  only the MoML exported by the attributes. I.e., the
     *  _exportMoMLContents() method is not called in this case.
     *  For example, if "foo" was cloned from "bar", and "bar" is a
     *  MoML "class", then the exported MoML looks like:
     *  <pre>
     *      &lt;entity name="foo" class=".bar"&gt;
     *          <i>attributes</i>
     *      &lt;/entity&gt;
     *  </pre>
     *  Here, ".bar" is the full name of another named object.
     *  <p>
     *  If this object has no container and the depth argument is zero,
     *  then this method prepends XML file header information, which is:
     *  <pre>
     *  &lt;?xml version="1.0" standalone="no"?&gt;
     *  &lt;!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
     *      "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
     *  </pre>
     *  If the element has class name "class" instead of "entity",
     *  then "entity" above is replaced with "class".
     *  <p>
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.
     *  Derived classes can override this method to change the MoML
     *  description of an object.  They can override the protected
     *  method _exportMoMLContents() if they need to only change which
     *  contents are described.
     *  <p>
     *  If this ojbect is not persistent, or if there is no MoML description
     *  of this object, or if this object is a class instance, then write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     *  @see #clone(Workspace)
     *  @see #getMoMLInfo()
     *  @see #isPersistent()
     *  @see #isClassElement()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        // If the object is not persistent, do nothing.
        if (!isPersistent() || isClassElement()) {
            return;
        }
        String momlElement = getMoMLInfo().elementName;
        String className = getMoMLInfo().className;
        String superclass = getMoMLInfo().superclass;
        String template = null;
        if (momlElement.equals("class")) {
            template = "\" extends=\"" + superclass + "\"";
        } else {
            template = "\" class=\"" + className + "\"";
        }
        if (depth == 0 && getContainer() == null) {
            // No container, and this is a top level moml element.
            // Generate header information.
            if (momlElement.equals("class")
                    || momlElement.equals("entity")) {
                output.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + momlElement + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");
            }
        }

        output.write(_getIndentPrefix(depth)
                + "<"
                + momlElement
                + " name=\""
                + name
                + template);

        if (getMoMLInfo().source != null) {
            output.write(" source=\"" + getMoMLInfo().source + "\">\n");
        } else {
            output.write(">\n");
        }

        // NOTE: This used to export MoML contents only if
        // getMoMLInfo().deferTo was null.  This wasn't right
        // because a deeply contained object might have been
        // modified, thus becoming an instance variable.
        // Now, we rely on _classElement == true suppressing
        // export of MoML.  EAL 12/03
        _exportMoMLContents(output, depth + 1);

        // Write the close of the element.
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLInfo().elementName + ">\n");
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
                String[] subnames = _splitName(name);
                if (subnames[1] == null) {
                    return (Attribute) _attributes.get(name);
                } else {
                    Attribute match = (Attribute)_attributes.get(subnames[0]);
                    if (match == null) {
                        return null;
                    } else {
                        return match.getAttribute(subnames[1]);
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
    public Enumeration getAttributes() {
        return Collections.enumeration(attributeList());
    }

    /** Get the container.  Always return null in this base class.
     *  A null returned value should be interpreted as indicating
     *  that there is no container.
     *  @return null.
     */
    public Nameable getContainer() {
        return null;
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
    public String getFullName() {
        try {
            _workspace.getReadAccess();
            if (_fullNameVersion == _workspace.getVersion()) {
                return _fullNameCache;
            }
            // Cache is not valid. Recalculate full name.
            String fullName = getName();
            // Use a hash set to keep track of what we've seen already.
            Set visited = new HashSet();
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

    /** Get the data structure defining the MoML description of this class.
     *  This method creates an instance of the data structure if one does
     *  not already exist, so this method never returns null.
     *  @return An instance of MoMLInfo.
     *  @see NamedObj.MoMLInfo
     */
    public MoMLInfo getMoMLInfo() {
        if (_MoMLInfo == null) {
            _MoMLInfo = new MoMLInfo(this);
        }
        return _MoMLInfo;
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
     */
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
     *  @param parent An object that deeply contains this object.
     *  @return A string of the form "name2...nameN".
     */
    public String getName(NamedObj parent) {
        if (parent == null) {
            return getFullName();
        }
        try {
            _workspace.getReadAccess();
            StringBuffer name = new StringBuffer(getName());
            // Use a hash set to keep track of what we've seen already.
            Set visited = new HashSet();
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
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception)
            throws IllegalActionException {
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

    /** Return true if this object is a class element.  An object
     *  is a class instance if it contained by an instance, but is
     *  defined in the class of the container.  If this returns true,
     *  then MoML will not be  exported for the object, and changing
     *  the name or container will trigger an exception.
     *  @return True if the object is a class element.
     *  @see #setPersistent(boolean)
     */
    public boolean isClassElement() {
        // NOTE: New method added. EAL 12/03
        return _isClassElement;
    }
    
    /** Return true if setDeferChangeRequests() has been called
     *  to specify that change requests should be deferred.
     *  @return True if change requests are being deferred.
     *  @see #setDeferChangeRequests(boolean)
     */
    public boolean isDeferChangeRequests() {
        return _deferChangeRequests;
    }

    /** Return true if this object is persistent.
     *  A persistent object has a MoML description that can be stored
     *  in a file and used to re-create the object. A non-persistent
     *  object has an empty MoML description.
     *  @return True if the object is persistent.
     *  @see #setPersistent(boolean)
     */
    public boolean isPersistent() {
        return _isPersistent;
    }

    /** React to a debug message by relaying it to any registered
     *  debug listeners.
     *  @param message The debug message.
     *  @since Ptolemy II 2.3
     */
    public void message(String message) {
        if (_debugging) {
            _debug(message);
        }
    }

    /** Remove a change listener. If there is a container, delegate the
     *  request to the container.  If the specified listener is not
     *  on the list of listeners, do nothing.
     *  @param listener The listener to remove.
     *  @see #addChangeListener(ChangeListener)
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        NamedObj container = (NamedObj) getContainer();
        if (container != null) {
            container.removeChangeListener(listener);
        } else {
            synchronized(_changeLock) {
                if (_changeListeners != null) {
                    ListIterator listeners = _changeListeners.listIterator();
                    while(listeners.hasNext()) {
                        WeakReference reference = (WeakReference)listeners.next();
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
     */
    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }
        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized(_debugListeners) {
            _debugListeners.remove(listener);
            if (_debugListeners.size() == 0) {
                _debugging = false;
            }
            return;
        }
    }

    /** Request that given change be executed.   In this base class,
     *  delegate the change request to the container, if there is one.
     *  If there is no container, then execute the request immediately,
     *  unless setDeferChangeRequests() has been called. If
     *  setDeferChangeRequests() has been called with a true argument,
     *  then simply queue the request until either setDeferChangeRequests()
     *  is called with a false argument or executeChangeRequests() is called.
     *  If this object is already in the middle of executing a change
     *  request, then that execution is finished before this one is performed.
     *  Change listeners will be notified of success (or failure) of the
     *  request when it is executed.
     *  @param change The requested change.
     *  @see #executeChangeRequests()
     *  @see #setDeferChangeRequests(boolean)
     */
    public void requestChange(ChangeRequest change) {
        NamedObj container = (NamedObj) getContainer();
        if (container != null) {
            container.requestChange(change);
        } else {
            // Have to ensure that the _deferChangeRequests status and
            // the collection of change listeners doesn't change during
            // this execution.  But we don't want to hold a lock on the
            // this NamedObj during execution of the change because this
            // could lead to deadlock.  So we synchronize to _changeLock.
            synchronized(_changeLock) {
                // Queue the request.
                // Create the list of requests if it doesn't already exist
                if (_changeRequests == null) {
                    _changeRequests = new LinkedList();
                }
                _changeRequests.add(change);
                if (!_deferChangeRequests) {
                    executeChangeRequests();
                }
            }
        }
    }
    
    /** Set whether this object is a class element.  If an object
     *  is a class element, then it exports no MoML and cannot have
     *  its name or container changed.
     *  By default, instances of NamedObj are not class elements.
     *  If this method is called with a <i>false</i> argument, then
     *  it will call setClassElement(false) on the container as
     *  well, making all containers above in the hierarchy not
     *  class elements.
     *  @param classElement True to mark this object as a class element.
     *  @see #isClassElement()
     */
    public void setClassElement(boolean classElement) {
        _isClassElement = classElement;
    }

    /** Specify whether change requests made by calls to requestChange()
     *  should be executed immediately. If there is a container, then
     *  this request is delegated to the container. Otherwise,
     *  if the argument is true, then requests
     *  are simply queued until either this method is called again
     *  with argument false, or until executeChangeRequests() is called.
     *  If the argument is false, the execute any pending change requests
     *  and set a flag requesting that future requests be executed
     *  immediately.
     *  @param defer If true, defer change requests.
     *  @see #addChangeListener(ChangeListener)
     *  @see #executeChangeRequests()
     *  @see #isDeferChangeRequests()
     *  @see #requestChange(ChangeRequest)
     */
    public void setDeferChangeRequests(boolean defer) {
        NamedObj container = (NamedObj) getContainer();
        if (container != null) {
            container.setDeferChangeRequests(defer);
            return;
        }

        // Make sure to avoid modification of this flag in the middle
        // of a change request or change execution.
        synchronized(_changeLock) {
            _deferChangeRequests = defer;
            if (defer == false) {
                executeChangeRequests();
            }
        }
    }

    /** Specify that when generating a MoML description of this named
     *  object, instead of giving a detailed description, refer to the
     *  specified other object.  The name of that other object goes
     *  into the "class" or "extends" attribute of the MoML element
     *  defining this object.  This method is called when this object
     *  is constructed by cloning another object that identifies itself
     *  as a MoML "class". This method is write synchronized on
     *  the workspace because it modifies the object that is the
     *  argument to refer back to this one.
     *  @param deferTo The object to defer to.
     *  @see #exportMoML(Writer, int)
     */
    public void setDeferMoMLDefinitionTo(NamedObj deferTo) {
        try {
            _workspace.getWriteAccess();
            if (getMoMLInfo().deferTo != null) {
                // NOTE: Referring directly to _MoMLInfo is safe here
                // because getMoMLInfo() above ensures this is non-null.
                if (_MoMLInfo.deferTo.getMoMLInfo().deferredFrom != null) {
                    // Removing a previous reference.
                    _MoMLInfo.deferTo._MoMLInfo.deferredFrom.remove(this);
                }
            }
            _MoMLInfo.deferTo = deferTo;
            if (deferTo != null) {
                // NOTE: These need to be weak references.
                deferTo._MoMLInfo.getDeferredFrom().add(new WeakReference(this));
            }
        } finally {
            _workspace.doneWriting();
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
     *   or if the object is a class element and the name argument does
     *   not match the current name.
     *  @exception NameDuplicationException Not thrown in this base class.
     *   May be thrown by derived classes if the container already contains
     *   an object with this name.
     *  @see #isClassElement()
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        String oldName = "";
        if (_debugging) {
            oldName = getFullName();
        }
        if (name == null) {
            name = new String("");
        }
        if (name.equals(_name)) {
            // Nothing to do.
            return;
        }
        // If this object is a class element, then its name cannot
        // be changed.  EAL 12/03.
        if (isClassElement()) {
            throw new IllegalActionException(this,
            "Cannot change the name of a class element.");
        }
        int period = name.indexOf(".");
        if (period >= 0) {
            throw new IllegalActionException(this,
                    "Cannot set a name with a period: " + name);
        }
        try {
            _workspace.getWriteAccess();
            _name = name;
        } finally {
            _workspace.doneWriting();
        }
        if (_debugging) {
            _debug("Changed name from " + oldName + " to " + getFullName());
        }
    }

    /** Set the persistence of this object.
     *  By default, instances of NamedObj are persistent, meaning
     *  that they have non-empty MoML descriptions that can be used
     *  to re-create the object. To make an instance non-persistent,
     *  call this method with the argument <i>false</i>. 
     *  @param persistent False to make this object non-persistent.
     *  @see #isPersistent()
     */
    public void setPersistent(boolean persistent) {
        _isPersistent = persistent;
    }

    /** Return the class name and the full name of the object,
     *  with syntax "className {fullName}".
     *  @return The class name and the full name. */
    public String toString() {
        return getClass().getName() + " {" + getFullName()+ "}";
    }

    /** Return the top level of the containment hierarchy.
     *  @return The top level, or this if this has no container.
     */
    public NamedObj toplevel() {
        NamedObj result = this;
        while (result.getContainer() != null) {
            result = (NamedObj)result.getContainer();
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
     */
    public void validateSettables() throws IllegalActionException {
        // This base class contains only attributes, so check those.
        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            if (attribute instanceof Settable) {
                try {
                    ((Settable)attribute).validate();
                } catch (IllegalActionException ex) {
                    handleModelError(this, ex);
                }
            }
            attribute.validateSettables();
        }
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
     *  This method is write-synchronized on the workspace and increments its
     *  version number.
     *  @param p The attribute to be added.
     *  @exception NameDuplicationException If this object already
     *   has an attribute with the same name.
     *  @exception IllegalActionException If the attribute is not an
     *   an instance of the expect class (in derived classes).
     */
    protected void _addAttribute(Attribute p)
            throws NameDuplicationException, IllegalActionException {
        try {
            _workspace.getWriteAccess();
            try {
                if (_attributes == null) {
                    _attributes = new NamedList();
                }
                _attributes.append(p);
            } catch (IllegalActionException ex) {
                // This exception should not be thrown.
                throw new InternalErrorException(
                        "Internal error in NamedObj _addAttribute() method!"
                        + ex.getMessage());
            }
            if (_debugging) {
                _debug("Added attribute", p.getName(), "to", getFullName());
            }
        } finally {
            _workspace.doneWriting();
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
            SingletonConfigurableAttribute icon
                = new SingletonConfigurableAttribute(this, name);
            icon.setPersistent(false);
            // The first argument below is the base w.r.t. which to open
            // relative references within the text, which doesn't make
            // sense in this case, so it's null. The second argument is
            // an external URL source for the text, which is again null.
            icon.configure(null, null, text);
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Error creating singleton attribute named "
                    + name
                    + " for "
                    + getFullName());
        }
    }

    /** Fix the fields of the given object which point to Attributes.
     * The object is assumed to be a clone of this one.  The fields
     * are fixed to point to the corresponding attribute of the clone,
     * instead of pointing to attributes of this object.
     */
    protected void _cloneFixAttributeFields(NamedObj newObject)
            throws CloneNotSupportedException {
        // If the new object has any public fields whose name
        // matches that of an attribute, then set the public field
        // equal to the attribute.
        Class myClass = getClass();
        Field fields[] = myClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                // VersionAttribute has a final field
                if ( !Modifier.isFinal(fields[i].getModifiers())) {
                    Object object = fields[i].get(this);
                    if(object instanceof Attribute) {
                        String name = ((NamedObj) object).getName(this);
                        fields[i].set(newObject,
                                newObject.getAttribute(name));
                    }
                }
            } catch (IllegalAccessException e) {
                
                // FIXME: This would be a nice
                // place for exception chaining.
                throw new CloneNotSupportedException(
                        "The field associated with "
                        + fields[i].getName()
                        + " could not be automatically cloned because "
                        + e.getMessage() + ".  This can be caused if "
                        + "the field is not defined in a public class.");
            }
        }
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
            List list;
            // NOTE: This used to synchronize on this, which caused
            // deadlocks.  We use a more specialized lock now.
            synchronized(_debugListeners) {
                list = new ArrayList(_debugListeners);
            }
            Iterator listeners = list.iterator();
            while (listeners.hasNext()) {
                ((DebugListener)listeners.next()).event(event);
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
            List list;
            // NOTE: This used to synchronize on this, which caused
            // deadlocks.  We use a more specialized lock now.
            synchronized(_debugListeners) {
                list = new ArrayList(_debugListeners);
            }
            Iterator listeners = list.iterator();
            while (listeners.hasNext()) {
                ((DebugListener)listeners.next()).message(message);
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
    protected final void _debug(String part1, String part2,
            String part3, String part4) {
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
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result = _getIndentPrefix(indent);
            if (bracket == 1 || bracket == 2) result += "{";
            if ((detail & CLASSNAME) != 0) {
                result += getClass().getName();
                if ((detail & FULLNAME) != 0) {
                    result += " ";
                }
            }
            if ((detail & FULLNAME) != 0) {
                result += "{" + getFullName() + "}";
            }
            if ((detail & ATTRIBUTES) != 0) {
                if ((detail & (CLASSNAME | FULLNAME)) != 0) {
                    result += " ";
                }
                result += "attributes {\n";
                // Do not recursively list attributes unless the DEEP
                // bit is set.
                if ((detail & DEEP) == 0) {
                    detail &= ~ATTRIBUTES;
                }
                if (_attributes != null) {
                    Iterator parameters = _attributes.elementList().iterator();
                    while (parameters.hasNext()) {
                        Attribute parameter = (Attribute)parameters.next();
                        result += parameter._description(detail, indent+1, 2) +
                            "\n";
                    }
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this base class is the attributes.  This method is called
     *  by _exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see #exportMoML(Writer, int)
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        if (_attributes != null) {
            Iterator attributes = _attributes.elementList().iterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute)attributes.next();
                attribute.exportMoML(output, depth);
            }
        }
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
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
            _attributes.remove((Nameable)param);
            if (_debugging) {
                _debug("Removed attribute", param.getName(), "from",
                        getFullName());
            }
        } finally {
            _workspace.doneWriting();
        }
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

    /** Return a string that is identical to the specified string except
     *  that any trailing characters that are numeric are removed.
     *  @param string The string to strip of its numeric suffix.
     *  @return A string with no numeric suffix.
     */
    protected static String _stripNumericSuffix(String string) {
        // NOTE: Perhaps it would be more efficient here to create
        // a HashSet for these numbers and test the last character for
        // membership.
        int length = string.length();
        char[] chars = string.toCharArray();
        for (int i = length - 1; i >= 0; i--) {
            char current = chars[i];
            if (current == '0'
                    || current == '1'
                    || current == '2'
                    || current == '3'
                    || current == '4'
                    || current == '5'
                    || current == '6'
                    || current == '7'
                    || current == '8'
                    || current == '9') {
                length--;
            } else {
                // Found a non-numeric, so we are done.
                break;
            }
        }
        if (length < string.length()) {
            // Some stipping occurred.
            char[] result = new char[length];
            System.arraycopy(chars, 0, result, 0, length);
            return new String(result);
        } else {
            return string;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** @serial Flag that is true if there are debug listeners. */
    protected boolean _debugging = false;

    /** @serial The list of DebugListeners registered with this object.
     *  NOTE: Because of the way we synchronize on this object, it should
     *  never be reset to null after the first list is created.
     */
    protected LinkedList _debugListeners = null;
    
    /** @serial The workspace for this object.
     * This should be set by the constructor and never changed.
     */
    protected Workspace _workspace;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Attributes attached to this object. */
    private NamedList _attributes;
    
    /** A list of weak references to change listeners. */
    private List _changeListeners;
    
    /** Object for locking accesses to change request list and status.
     *  NOTE: We could have used _changeRequests or _changeListeners,
     *  but those lists are only created when needed.  A simple
     *  Object here is presumably cheaper than a list, but it is
     *  truly unfortunate to have to carry this in every NamedObj.
     */
    private Object _changeLock = new Object();
    
    /** A list of pending change requests. */
    private List _changeRequests;

    /** @serial Instance of a workspace that can be used if no other
     *  is specified.
     */
    private static Workspace _DEFAULT_WORKSPACE = new Workspace();

    /** Flag indicating that we should not immedidately
     *  execute a change request.
     */
    private transient boolean _deferChangeRequests = false;

    // Cached value of the full name.
    private String _fullNameCache;

    // Version of the workspace when cache last updated.
    private long _fullNameVersion = -1;

    // Boolean variable to indicate whether this is a class element.
    // By default, instances of NamedObj are not class elements.
    private boolean _isClassElement = false;

    // Boolean variable to indicate the persistence of the object.
    // By default, instances of NamedObj are persistent.
    private boolean _isPersistent = true;
    
    // The model error handler, if there is one.
    private ModelErrorHandler _modelErrorHandler = null;

    // The MoML information describing this object.
    private MoMLInfo _MoMLInfo;

    /** @serial The name */
    private String _name;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class is a data structure for storing MoML information
     *  used to construct the MoML description of an associated NamedObj.
     *  Each NamedObj has at most one instance of this class.  That
     *  instance can be accessed (or created if it does not exist)
     *  by calling getMoMLInfo().
     *  @see NamedObj#getMoMLInfo()
     */
    public static class MoMLInfo implements Serializable {

        /** Construct an object with default values for the fields.
         *  @param owner The object that this describes.
         */
        protected MoMLInfo(NamedObj owner) {
            Class ownerClass = owner.getClass();
            className = ownerClass.getName();
            superclass = ownerClass.getSuperclass().getName();
        }

        ///////////////////////////////////////////////////////////////
        ////                     public members                    ////

        /** @serial The MoML class name, which defaults to the Java
         *  class name of the enclosing class.*/
        public String className;

        /** @serial A list of weak references to objects that defer
         *  their MoML definitions to the owner of this MoMLInfo object.
         *  Note that this might be null.  To ensure that it is not null,
         *  access it using getDeferredFrom().
         */
        public List deferredFrom;

        /** @serial The object that the owner defers its MoML
         *  definition to, or null if it does not defer its MoML
         *  definition.
         */
        public NamedObj deferTo;

        /** @serial The MoML element name. This defaults to "entity".*/
        public String elementName = "entity";

        /** @serial The superclass of this class.
         */
        public String superclass;

        /** @serial The source attribute, which gives an external URL
         *   associated with an entity (presumably from which the entity
         *   was defined).
         */
        public String source;

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return a list of weak references to objects that defer their
         *  MoML definitions to the owner of this MoMLInfo object.
         *  This might be an empty list, but the returned value is
         *  never null.
         *  @return A list of instances of NamedObj.
         */
        public List getDeferredFrom() {
            if (deferredFrom == null) {
                deferredFrom = new LinkedList();
            }
            return deferredFrom;
        }
    }
}
