/* A named object that can be either a class or an instance.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// InstantiableNamedObj

/**
 An InstantiableNamedObj is a named object that can be either a class
 definition or an instance.  If it is a class definition, then "instances" of
 that class definition can be created by the instantiate() method. Those
 instances are called the "children" of this "parent." Changes
 to the parent propagate automatically to the children as described
 in the {@link Instantiable} interface.
 <p>
 Note that the {@link #instantiate(NamedObj, String)} permits instantiating
 an object into a workspace that is different from the one associated with
 this object.  This means that some care must be exercised when propagating
 changes from a parent to a child, since they may be in different workspaces.
 Suppose for example that the change that has to propagate is made via a
 change request. Although it may be a safe time to execute a change request
 in the parent, it is not necessarily a safe time to execute a change request
 in the child.  Classes that restrict these safe times should override
 the propagateExistence(), propagateValue(), and propagateValues() methods
 to ensure that the destinations of the propagation are in a state that
 they can accept changes.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @see Instantiable
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (neuendor)
 */
public class InstantiableNamedObj extends NamedObj implements Instantiable {
    /** Construct an object in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public InstantiableNamedObj() {
        super();
    }

    /** Construct an object in the default workspace with the given name.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param name The name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public InstantiableNamedObj(String name) throws IllegalActionException {
        super(name);
    }

    /** Construct an object in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     */
    public InstantiableNamedObj(Workspace workspace) {
        super(workspace);
    }

    /** Construct an object in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     *  @param name The name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public InstantiableNamedObj(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there). The result is a new instance of
     *  InstantiableNamedObj that is a child of the parent of this object,
     *  if this object has a parent. The new instance has no children.
     *  This method gets read access on the workspace associated with
     *  this object.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return A new instance of InstantiableNamedObj.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        try {
            workspace().getReadAccess();

            InstantiableNamedObj newObject = (InstantiableNamedObj) super
                    .clone(workspace);

            // The new object does not have any other objects deferring
            // their MoML definitions to it, so we have to reset this.
            newObject._children = null;

            // Set the parent using _setParent() rather than the default
            // clone to get the side effects.
            newObject._parent = null;

            // NOTE: This used to do the following,
            // but we now rely on _adjustDeferrals()
            // to fix the parent relationships.

            /*
             if (_parent != null) {
             try {
             newObject._setParent(_parent);
             } catch (IllegalActionException ex) {
             throw new InternalErrorException(ex);
             }
             }
             */
            return newObject;
        } finally {
            workspace().doneReading();
        }
    }

    /** Write a MoML description of this object with the specified
     *  indentation depth and with the specified name substituting
     *  for the name of this object. The description has one of two
     *  forms, depending on whether this is a class definition.
     *  If it is, then the exported MoML looks like this:
     *  <pre>
     *      &lt;class name="<i>name</i>" extends="<i>classname</i> source="<i>source</i>"&gt;
     *          <i>body, determined by _exportMoMLContents()</i>
     *      &lt;/class&gt;
     *  </pre>
     *  Otherwise, the exported MoML is that generated by the
     *  superclass method that this overrides.
     *  <p>
     *  If this object has no container and the depth argument is zero,
     *  then this method prepends XML file header information, which is:
     *  <pre>
     *  &lt;?xml version="1.0" standalone="no"?&gt;
     *  &lt;!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
     *      "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
     *  </pre>
     *  In the above, "entity" may be replaced by "property" or
     *  "port" if what is being exported is an attribute or a port.
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
     *  description of this object, or if this object is implied
     *  by a parent-child relationship that less than <i>depth</i>
     *  levels up in the containment hierarchy and it has not
     *  been overridden, then write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     *  @see ptolemy.kernel.util.MoMLExportable
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {

        if (!isClassDefinition()) {
            super.exportMoML(output, depth, name);
            return;
        }

        // escape any < character in name. unescapeForXML occurs in
        // NamedObj.setName(String)
        // If we don't escape the name here then we generate
        // MoML that is not valid XML.  See MoMLParser-34.0 in
        // moml/test/MoMLParser.tcl
        name = StringUtilities.escapeForXML(name);

        // If the object is not persistent, and we are not
        // at level 0, do nothing.
        if (_isMoMLSuppressed(depth)) {
            return;
        }

        if (depth == 0 && getContainer() == null) {
            // No container, and this is a top level moml element.
            // Generate header information.
            // NOTE: Currently, there is only one class designation,
            // and it always applies to an Entity. Attributes that
            // are classes are not yet supported. When they are,
            // then "class" below may need to replaced with something
            // else.
            output.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE class PUBLIC "
                    + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                    + "    \"http://ptolemy.eecs.berkeley.edu"
                    + "/xml/dtd/MoML_1.dtd\">\n");
        }

        output.write(_getIndentPrefix(depth) + "<class name=\"" + name
                + "\" extends=\"" + getClassName() + "\"");

        if (getSource() != null) {
            output.write(" source=\"" + getSource() + "\">\n");
        } else {
            output.write(">\n");
        }

        _exportMoMLContents(output, depth + 1);

        // Write the close of the element.
        output.write(_getIndentPrefix(depth) + "</class>\n");
    }

    /** Get a list of weak references to instances of Instantiable
     *  that are children of this object.  This method
     *  may return null or an empty list to indicate that there are
     *  no children.
     *  @return An unmodifiable list of instances of
     *   java.lang.ref.WeakReference that refer to
     *   instances of Instantiable or null if this object
     *   has no children.
     *  @see Instantiable
     *  @see java.lang.ref.WeakReference
     */
    @Override
    public List getChildren() {
        if (_children == null) {
            return null;
        }

        return Collections.unmodifiableList(_children);
    }

    /** Get the MoML element name. If this is a class definition, then
     *  return "class". Otherwise, defer to the base class.
     *  @return The MoML element name for this object.
     *  @see ptolemy.kernel.util.MoMLExportable
     */
    @Override
    public String getElementName() {
        if (isClassDefinition()) {
            return "class";
        } else {
            return super.getElementName();
        }
    }

    /** Return the parent of this object, or null if there is none.
     *  @return The parent of this object, or null if there is none.
     *  @see #_setParent(Instantiable)
     *  @see Instantiable
     */
    @Override
    public Instantiable getParent() {
        return _parent;
    }

    /** Return a list of prototypes for this object. The list is ordered
     *  so that more local prototypes are listed before more remote
     *  prototypes. Specifically, if this object has a parent, then the
     *  parent is listed first. If the container has a parent, and
     *  that parent contains an object whose name matches the name
     *  of this object, then that object is listed next.
     *  If the container of the container has a parent, and that parent
     *  (deeply) contains a prototype, then that prototype is listed next.
     *  And so on up the hierarchy.
     *  @return A list of prototypes for this object, each of which is
     *   assured of being an instance of the same (Java) class as this
     *   object, or an empty list if there are no prototypes.
     *  @exception IllegalActionException If a prototype with the right
     *   name but the wrong class is found.
     *  @see ptolemy.kernel.util.Derivable
     */
    @Override
    public List getPrototypeList() throws IllegalActionException {
        List result = super.getPrototypeList();

        if (getParent() != null) {
            result.add(0, getParent());
        }

        return result;
    }

    /** Create an instance by (deeply) cloning this object and then adjusting
     *  the parent-child relationships between the clone and its parent.
     *  Specifically, the clone defers its definition to this object,
     *  which becomes its "parent." The "child" inherits all the objects
     *  contained by this object. If this object is a composite, then this
     *  method adjusts any parent-child relationships that are entirely
     *  contained within the child. That is, for any parent-child relationship
     *  that is entirely contained within this object (i.e., both the parent
     *  and the child are deeply contained by this object), a corresponding
     *  parent-child relationship is created within the clone such that
     *  both the parent and the child are entirely contained within
     *  the clone.
     *  <p>
     *  The new object is not a class definition by default (it is an
     *  "instance" rather than a "class").  To make it a class
     *  definition (a "subclass"), call {@link #setClassDefinition(boolean)}
     *  with a <i>true</i> argument.
     *  <p>
     *  In this base class, the container argument is ignored except that
     *  it provides the workspace into which to clone this object. Derived
     *  classes with setContainer() methods are responsible for overriding
     *  this and calling setContainer().
     *  <p>
     *  Note that the workspace for the instantiated object can be different
     *  from the workspace for this object. This means that propagation of
     *  changes from a parent to a child may not be able to be safely
     *  performed in the child even when they are safely performed in the
     *  parent. Subclasses that restrict when changes are performed are
     *  therefore required to check whether the workspaces are the same
     *  before propagating changes.
     *  @param container The container for the instance, or null
     *   to instantiate it at the top level. Note that this base class
     *   does not set the container. It uses the container argument to
     *   get the workspace. Derived classes are responsible for
     *   setting the container.
     *  @param name The name for the instance.
     *  @return A new instance that is a clone of this object
     *   with adjusted parent-child relationships.
     *  @exception CloneNotSupportedException If this object
     *   cannot be cloned.
     *  @exception IllegalActionException If this object is not a
     *   class definition or the proposed container is not acceptable.
     *  @exception NameDuplicationException If the name collides with
     *   an object already in the container.
     *  @see #setClassDefinition(boolean)
     *  @see Instantiable
     */
    @Override
    public Instantiable instantiate(NamedObj container, String name)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        if (!isClassDefinition()) {
            throw new IllegalActionException(this,
                    "Cannot instantiate an object that is not a "
                            + "class definition");
        }

        // Use the workspace of the container, if there is one,
        // or the workspace of this object, if there isn't.
        Workspace workspace = workspace();

        if (container != null) {
            workspace = container.workspace();
        }

        InstantiableNamedObj clone = (InstantiableNamedObj) clone(workspace);

        // The cloning process results an object that defers change
        // requests.  By default, we do not want to defer change
        // requests, but more importantly, we need to execute
        // any change requests that may have been queued
        // during cloning. The following call does that.
        clone.setDeferringChangeRequests(false);

        // Set the name before the container to not get
        // spurious name conflicts.
        clone.setName(name);
        clone._setParent(this);
        clone.setClassDefinition(false);
        clone.setClassName(getFullName());

        // Mark the contents of the instantiated object as being derived.
        clone._markContentsDerived(0);

        return clone;
    }

    /** Return true if this object is a class definition, which means that
     *  it can be instantiated.
     *  @return True if this object is a class definition.
     *  @see #setClassDefinition(boolean)
     *  @see Instantiable
     */
    @Override
    public final boolean isClassDefinition() {
        return _isClassDefinition;
    }

    /** Return true if this object is a class definition or is within
     *  a class definition, which means that
     *  any container above it in the hierarchy is
     *  a class definition.
     *  @return True if this object is within a class definition.
     *  @see #setClassDefinition(boolean)
     *  @see Instantiable
     */
    public final boolean isWithinClassDefinition() {
        if (_isClassDefinition) {
            return true;
        } else {
            NamedObj container = getContainer();
            while (container != null) {
                if (container instanceof InstantiableNamedObj) {
                    if (((InstantiableNamedObj) container)._isClassDefinition) {
                        return true;
                    }
                }
                container = container.getContainer();
            }
            return false;
        }
    }

    /** Specify whether this object is a class definition.
     *  This method is write synchronized on the workspace.
     *  @param isClass True to make this object a class definition, false
     *   to make it an instance.
     *  @exception IllegalActionException If there are subclasses and/or
     *   instances and the argument is false.
     *  @see #isClassDefinition()
     *  @see Instantiable
     */
    public void setClassDefinition(boolean isClass)
            throws IllegalActionException {
        workspace().getWriteAccess();

        try {
            if (!isClass && _isClassDefinition && getChildren() != null
                    && getChildren().size() > 0) {
                throw new IllegalActionException(this,
                        "Cannot change from a class to an instance because"
                                + " there are subclasses and/or instances.");
            }

            if (_isClassDefinition != isClass) {
                // Changing the class status is a hierarchy event that
                // contained objects need to be notified of.
                _notifyHierarchyListenersBeforeChange();
                _isClassDefinition = isClass;
            }
        } finally {
            workspace().doneWriting();
            _notifyHierarchyListenersAfterChange();
        }
    }

    /** Specify the parent for this object.  This  method should be called
     *  to make this object either an instance or a subclass of
     *  the other object. When generating
     *  a MoML description of this object, instead of giving a detailed
     *  description, this object will henceforth refer to the
     *  specified other object.  The name of that other object goes
     *  into the "class" or "extends" attribute of the MoML element
     *  defining this object (depending on whether this is an instance
     *  or a subclass).  This method is called when this object
     *  is constructed using the {@link #instantiate(NamedObj, String)}
     *  method. This method is write synchronized on
     *  the workspace because it modifies the object that is the
     *  argument to refer back to this one.
     *  <p>
     *  Note that a parent references a child via a weak reference.
     *  This means that the parent will not prevent the child from
     *  being garbage collected. However, as long as the child has
     *  not been garbage collected, changes to the parent will
     *  propagate to the child even if there are no other live
     *  references to the child. If there are a large number of
     *  such dangling children, this could create performance
     *  problems when making changes to the parent.
     *  @param parent The parent, or null to specify that there is
     *   no parent.
     *  @exception IllegalActionException If the parent is not an
     *   instance of InstantiableNamedObj.
     *  @see #exportMoML(Writer, int)
     *  @see #getParent()
     *  @see Instantiable
     */
    protected void _setParent(Instantiable parent)
            throws IllegalActionException {
        if (parent != null && !(parent instanceof InstantiableNamedObj)) {
            throw new IllegalActionException(this,
                    "Parent of an InstantiableNamedObj must also "
                            + "be an InstantiableNamedObj.");
        }

        try {
            _workspace.getWriteAccess();

            if (_parent != null) {
                // Previously existing deferral.
                // Remove it.
                // NOTE: If WeakReference overrides equal(),
                // then this could probably be done more simply.
                List deferredFromList = _parent._children;

                if (deferredFromList != null) {
                    // Removing a previous reference.
                    // Note that this is a list of weak references, so
                    // it is not sufficient to just remove this!
                    ListIterator references = deferredFromList.listIterator();

                    while (references.hasNext()) {
                        WeakReference reference = (WeakReference) references
                                .next();

                        if (reference == null || reference.get() == this) {
                            references.remove();
                        }
                    }
                }
            }

            _parent = (InstantiableNamedObj) parent;

            if (_parent != null) {
                if (_parent._children == null) {
                    _parent._children = new LinkedList();
                }

                // NOTE: These need to be weak references.
                _parent._children.add(new WeakReference(this));
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of weak references to children for which this object
     *  is the parent.
     */
    private List _children;

    /** Parent to which this object defers its definition to, or
     *  null if there is none.
     */
    private InstantiableNamedObj _parent;

    /** Indicator of whether this is a class definition. */
    private boolean _isClassDefinition;
}
