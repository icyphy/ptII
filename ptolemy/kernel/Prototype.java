/* An Prototype is a named object that can be either a class or an instance.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Prototype
/**
An Prototype is a named object that can be either a class definition
or not.  If it is a class definition, then "instances" of that class
definition can be created by the instantiate() method.
It supports a deferral and propagation mechanism. That is, changes
to the prototype propagate automatically to "instances" that
were created from the prototype.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 4.0
*/
public class Prototype extends NamedObj implements Instantiable {

    /** Construct a prototype in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public Prototype() {
        super();
    }

    /** Construct a prototype in the default workspace with the given name.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param name The name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public Prototype(String name) throws IllegalActionException {
        super(name);
    }

    /** Construct a prototype in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     */
    public Prototype(Workspace workspace) {
        super(workspace);
    }

    /** Construct a prototype in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     *  @param name The name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public Prototype(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new prototype that defers its definition to the
     *  same object as this one (or to none).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return A new Prototype.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Prototype newObject = (Prototype)super.clone(workspace);

        if (getParent() != null) {
            try {
                newObject.setParent(getParent());
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex);
            }
        }
        // The new object does not have any other objects deferring
        // their MoML definitions to it, so we have to reset this.
        newObject._children = null;

        return newObject;
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
     *  If this object is not persistent, or if there is no MoML
     *  description of this object, or if this object is a class
     *  instance, then write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        // If the object is not persistent, and we are not
        // at level 0, do nothing.
        if (_suppressMoML(depth)) {
            return;
        }
        if (!isClassDefinition()) {
            super.exportMoML(output, depth, name);
            return;
        }

        if (depth == 0 && getContainer() == null) {
            // No container, and this is a top level moml element.
            // Generate header information.
            output.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE class PUBLIC "
                    + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                    + "    \"http://ptolemy.eecs.berkeley.edu"
                    + "/xml/dtd/MoML_1.dtd\">\n");
        }

        output.write(_getIndentPrefix(depth)
                + "<class name=\""
                + name
                + "\" extends=\""
                + getClassName()
                + "\"");

        if (getSource() != null) {
            output.write(" source=\"" + getSource() + "\">\n");
        } else {
            output.write(">\n");
        }
        _exportMoMLContents(output, depth + 1);

        // Write the close of the element.
        output.write(_getIndentPrefix(depth) + "</class>\n");
    }

    /** Get a list of weak references to instance of Instantiable
     *  that defer to this object.
     *  @return An unmodifiable list of weak references to
     *   instances of Instantiable or null if no object defers to this one.
     */
    public List getChildren() {
        if (_children == null) {
            return null;
        }
        return Collections.unmodifiableList(_children);
    }

    /** Get the MoML element name. If this is a class definition, then
     *  return "class". Otherwise, defer to the base class.
     *  @return The MoML element name for this object.
     */
    public String getElementName() {
        if (isClassDefinition()) {
            return "class";
        } else {
            return super.getElementName();
        }
    }

    /** Get the prototype to which this object defers its definition.
     *  @return A prototype or null to indicate that this object does
     *   not defer its definition.
     */
    public Instantiable getParent() {
        return _parent;
    }

    /** Create an instance by cloning this prototype and then adjust
     *  the deferral relationship between the the clone and its parent.
     *  Specifically, the
     *  clone defers its definition to this prototype, which is its
     *  "parent." It inherits all the objects contained by this prototype.
     *  <p>
     *  The new object is not a class definition (it is by default an
     *  "instance" rather than a "class").  To make it a class
     *  definition (a "subclass"), call setClassDefinition(true).
     *  <p>
     *  In this base class, the container argument is ignored except that
     *  it provides the workspace into which to clone this prototype. Derived
     *  classes with setContainer() methods are responsible for overriding this
     *  and calling setContainer().
     *  @see #setClassDefinition(boolean)
     *  @param container The container for the instance, or null
     *   to instantiate it at the top level.
     *  @param name The name for the clone.
     *  @return A new instance that is a clone of this prototype
     *   with adjusted deferral relationships.
     *  @exception CloneNotSupportedException If this prototype
     *   cannot be cloned.
     *  @exception IllegalActionException If this object is not a
     *   class definition or the proposed container is not acceptable.
     *  @exception NameDuplicationException If the name collides with
     *   an object already in the container.
     */
    public Instantiable instantiate(NamedObj container, String name)
            throws CloneNotSupportedException,
            IllegalActionException, NameDuplicationException {
        if (!isClassDefinition()) {
            throw new IllegalActionException(this,
            "Cannot instantiate an object that is not a class definition");
        }
        // Use the workspace of the container, if there is one,
        // or the workspace of this object, if there isn't.
        Workspace workspace = workspace();
        if (container != null) {
            workspace = container.workspace();
        }
        Prototype clone = (Prototype)clone(workspace);
        // Set the name before the container to not get
        // spurious name conflicts.
        clone.setName(name);
        clone.setParent(this);
        clone.setClassDefinition(false);
        clone.setClassName(getFullName());

        return clone;
    }

    /** Return true if this object is a class definition, which means that
     *  it can be instantiated.
     *  @return True if this object is a class definition.
     *  @see #setClassDefinition(boolean)
     */
    public final boolean isClassDefinition() {
        return _isClassDefinition;
    }

    /** Return the maximum deferral depth of this object.
     *  Going up the hierarchy, each time a container is encountered
     *  that defers its definition to a parent, increment the parent
     *  depth by one. Return the largest such
     *  incremented depths, or zero if no container defers its
     *  definition.
     *  @return The maximum deferral depth.
     */
    public int maximumParentDepth() {
        Prototype context = this;
        int result = 0;
        while (context != null) {
            Instantiable parent = context.getParent();
            if (parent != null) {
                int newDepth = 1 + parent.maximumParentDepth();
                if (newDepth > result) {
                    result = newDepth;
                }
            }
            // Subclasses ensure that the container
            // is an instance of Prototype.
            context = (Prototype)context.getContainer();
        }
        // No deferrals encountered while moving up the hierarchy.
        return result;
    }

    /** Specify whether this object is a class definition.
     *  This method is write synchronized on the workspace.
     *  @param isClass True to make this object a class definition.
     *  @throws IllegalActionException If setting the class definition
     *   is not allowed.
     */
    public void setClassDefinition(boolean isClass)
            throws IllegalActionException {
        // No need for a try-catch, since it's an atomic set.
        workspace().getWriteAccess();
        _isClassDefinition = isClass;
        workspace().doneWriting();
    }

    /** Specify that this object defers its definition to another
     *  object.  This should be called to make this object either an
     *  an instance or a subclass of the other object. When generating
     *  a MoML description of this object, instead of giving a detailed
     *  description, this object will henceforth refer to the
     *  specified other object.  The name of that other object goes
     *  into the "class" or "extends" attribute of the MoML element
     *  defining this object (depending on whether this is an instance
     *  or a subclass).  This method is called when this object
     *  is constructed by cloning another object that identifies itself
     *  as a MoML "class". This method is write synchronized on
     *  the workspace because it modifies the object that is the
     *  argument to refer back to this one.
     *  @param parent The object to defer to, or null to defer to none.
     *  @see #exportMoML(Writer, int)
     *  @exception IllegalActionException If the parent is not an
     *   instance of Prototype.
     */
    public void setParent(Instantiable parent) throws IllegalActionException {
        if (parent != null && !(parent instanceof Prototype)) {
            throw new IllegalActionException(this,
            "Parent of a Prototype must also be a Prototype.");
        }
        try {
            _workspace.getWriteAccess();
            if (_parent != null) {
                // Previously existing deferral.
                // Remove it.
                List deferredFromList = _parent._children;
                if (deferredFromList != null) {
                    // Removing a previous reference.
                    // Note that this is a list of weak references, so
                    // it is not sufficient to just remove this!
                    ListIterator references = deferredFromList.listIterator();
                    while (references.hasNext()) {
                        WeakReference reference =
                            (WeakReference)references.next();
                        if (reference == null || reference.get() == this) {
                            references.remove();
                        }
                    }
                }
            }
            _parent = (Prototype)parent;
            if (parent != null) {
                if (((Prototype)parent)._children == null) {
                    ((Prototype)parent)._children = new LinkedList();
                }
                // NOTE: These need to be weak references.
                ((Prototype)parent)._children.add(new WeakReference(this));
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Adjust the deferral relationships in the specified clone.
     *  Specifically, if this object defers to something that is
     *  deeply contained by the specified prototype, then find
     *  the corresponding object in the specified clone and defer
     *  to it instead. This method also calls the same method
     *  on all contained class definitions and ordinary entities.
     *  @param prototype The object that was cloned.
     *  @param clone The clone.
     *  @exception IllegalActionException If the clone does not contain
     *   a corresponding object to defer to.
     */
    protected void _adjustDeferrals(
            CompositeEntity prototype, CompositeEntity clone) {
        Instantiable originalDefersTo = getParent();
        if (originalDefersTo instanceof NamedObj) {
            // defersTo was cloned from the original, presumably.
            if (prototype.deepContains((NamedObj)originalDefersTo)) {
                String relativeName = originalDefersTo.getName(prototype);
                ComponentEntity revisedDefersTo = clone.getEntity(relativeName);
                if (revisedDefersTo == null) {
                    throw new InternalErrorException(
                    "Clone is not identical to the prototype!");
                }
                try {
                    setParent(revisedDefersTo);
                } catch (IllegalActionException e) {
                    // This should not occur because the parent
                    // relationship was acceptable to the source
                    // of the clone.
                    throw new InternalErrorException(e);
                }
            }
        }
    }

    /** Return the depth of the deferral that defines the specified object.
     *  This overrides the base class so that if this object defers to
     *  another that defines the defined object, and the exported
     *  MoML of the defined object is identical to the exported MoML
     *  of the deferred to object, then it returns 0.  Otherwise,
     *  it defers to the base class.
     *  Otherwise, it defers to the base class.
     *  @param definedObject The object whose definition we seek.
     *  @return The depth of the deferral.
     */
    protected int _getDeferralDepth(NamedObj definedObject) {
        if (_parent != null && deepContains(definedObject)) {
            String relativeName = definedObject.getName(this);
            // Regrettably, we have to look at the type
            // of definedObject to figure out how to look it up.
            if (definedObject instanceof Attribute) {
                Attribute definition = _parent.getAttribute(relativeName);
                if (definition != null
                        && definedObject.exportMoML()
                        .equals(definition.exportMoML())) {
                    return 0;
                }
            }
        }
        return super._getDeferralDepth(definedObject);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of prototypes that defer their definition to this object. */
    private List _children;

    /** Prototype to which this object defers its definition to, or
     *  null if none.
     */
    private Prototype _parent;

    /** Indicator of whether this is a class definition. */
    private boolean _isClassDefinition;
}
