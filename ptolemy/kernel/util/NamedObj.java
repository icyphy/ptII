/* Base class for objects with a name and a container.

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

package pt.kernel.util;

import java.io.Serializable;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// NamedObj
/** 
This is a base class for objects with a name. A simple name is an arbitrary
string. If no simple name is provided, the name is taken to be an
empty string (not a null reference). The class also has a full name,
which is a concatenation of the container's full name and the simple
name, separating by a period. Obviously, if the simple name contains
a period then there may be some confusion resolving the full name,
so periods are discouraged (but not disallowed). If there is no
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
be reported by the getAttribute() and getAttributes() methods.
Classes derived from NamedObj may constrain attributes to be a subclass
of Attribute.  To do that, they should override the protected
_addAttribute() method to throw an exception if the
object provided is not of the right class.

@author Mudit Goel, Edward A. Lee, Neil Smyth
@version $Id$
*/

public class NamedObj implements Nameable, Serializable, Cloneable {

    /** Construct an object in the default workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NamedObj() {
        this(_defaultworkspace, "");
    }

    /** Construct an object in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty 
     *  string. The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public NamedObj(String name) {
        this(_defaultworkspace, name);
    }

    /** Construct an object in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this object.
     */
    public NamedObj(Workspace workspace, String name) {
        if (workspace == null) {
            workspace = _defaultworkspace;
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
            // Ignore
        }
        setName(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Clone the object and add the cloned object to the directory of the
     *  workspace in which the original object resides.
     *  This overrides the protected clone() method of
     *  java.lang.Object, which makes a field-by-field copy, to
     *  clone the attribute list and to call the protected method
     *  _clearAndSetWorkspace(), which is defined in derived classes to remove
     *  references that should not be in the clone.  The _clearAndSetWorkspace()
     *  method serves the function of a constructor, which is not
     *  called, making the state of the cloned object like
     *  one that has just been constructed.
     *  This method read-synchronizes on the workspace.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return clone(workspace());
    }

    /** Clone the object into the specified workspace and add the clone
     *  to the directory of that workspace. This uses the clone() method of
     *  java.lang.Object, which makes a field-by-field copy, and then
     *  calls the protected method _clearAndSetWorkspace(), which is
     *  expected to remove references that should not be in the clone.
     *  The _clearAndSetWorkspace()
     *  method serves the function of a constructor, which is not
     *  called, making the state of the cloned object like
     *  one that has just been constructed.  It then clones the
     *  attributes on the attribute list, if there is one.
     *  This method read-synchronizes on the workspace.
     *  @param ws The workspace in which to list the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // NOTE: It is safe to clone an object into a different
        // workspace. It is not safe to move an object to a new
        // workspace, by contrast. The reason this is safe is that
        // after the _clearAndSetWorkspace() method has been run, there
        // are no references in the clone to objects in the old
        // workspace. Moreover, no object in the old workspace can have
        // a reference to the cloned object because we have only just
        // created it and have not returned the reference.
        try {
            workspace().read();
            NamedObj result = (NamedObj)super.clone();
            // NOTE: It is not necessary to write-synchronize on the other
            // workspace because this only affects its directory, and methods
            // to access the directory are synchronized.
            result._clearAndSetWorkspace(ws);
            try {
                ws.add(result);
            } catch (IllegalActionException ex) {
                // Ignore.  Can't occur.
            }
            Enumeration params = getAttributes();
            while (params.hasMoreElements()) {
                Attribute p = (Attribute)params.nextElement();
                Attribute np = (Attribute)p.clone(ws);
                try {
                    np.setContainer(result);         
                } catch (KernelException ex) {
                    throw new CloneNotSupportedException(
                            "Failed to clone an Attribute of " +
                            getFullName() + ": " + ex.getMessage());
                }
            }
            return result;
        } finally {
            workspace().doneReading();
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
     *  @see CompositeEntity.isAtomic
     *  @return True if this contains the argument, directly or indirectly.
     */	
    public boolean deepContains(NamedObj inside) {
        if (workspace() != inside.workspace()) return false;
        try {
            workspace().read();
            // Start with the inside and check its containers in sequence.
            if (inside != null) {
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
            workspace().doneReading();
        }
    }

    /** Return a full description of the object. This is accomplished
     *  by calling the description method with an argument for full detail.
     *  This method read-synchronizes on the workspace.
     *  @return A description of the object.
     */
    public String description() {
        return description(ALL);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in this class (NamedObj).  This method returns an empty
     *  string (not null) if there is nothing to report.
     *  It read-synchronizes on the workspace.
     *  @param detail The level of detail.
     *  @return A description of the object.
     */
    public String description(int detail) {
        return _description(detail, 0);
    }

    /** Get the container.  Always return null in this base class.
     *  A null returned value should be interpreted as indicating 
     *  that there is no container.
     *  @return null.
     */
    public Nameable getContainer() {
	return null;
    }

    /** Return a string of the form "workspace.name1.name2...nameN". Here,
     *  "nameN" is the name of this object, "workspace" is the name of the
     *  workspace, and the intervening names are the names of the containers
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
            workspace().read();
            // NOTE: For improved performance, the full name could be cached.
            String fullname = getName();
            // Use a linked list to keep track of what we've seen already.
            LinkedList visited = new LinkedList();
            visited.insertFirst(this);
            Nameable container = getContainer();

            while (container != null) {
                if (visited.firstIndexOf(container) >= 0) {
                    // Cannot use "this" as a constructor argument to the
                    // exception or we'll get stuck infinitely
                    // calling this method, since this method is used to report
                    // exceptions.  InvalidStateException is a runtime
                    // exception, so it need not be declared.
                    throw new InvalidStateException(
                            "Container contains itself!");
                }
                fullname = container.getName() + "." + fullname;
                visited.insertFirst(container);
                container = container.getContainer();
            }
            return workspace().getName() + "." + fullname;
        } finally {
            workspace().doneReading();
        }
    }

    /** Get the name. If no name has been given, or null has been given,
     *  then return an empty string, "".
     *  @return The name of the object. 
     */	
    public String getName() { 
        return _name; 
    }

    /** Get the attribute with the given name.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    public Attribute getAttribute(String name) {
        try {
            workspace().read();
            return (Attribute) _attributes.get(name);
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of the attributes attached to this object.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of instances of Attribute.
     */
    public Enumeration getAttributes() {
        try {
            workspace().read();
            if  (_attributes == null) {
                return (new NamedList()).getElements();
            } else {
                return _attributes.getElements();
            }
        } finally {
            workspace().doneReading();
        }
    }
    
    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new name.
     */
    public void setName(String name) {
        if (name == null) {
            name = new String("");
        }
        try {
            workspace().write();
            _name = name;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return the class name and the full name of the object,
     *  with syntax "classname {fullname}".
     *  @return The class name and the full name. */ 
    public String toString() {
        return getClass().getName() + " {" + getFullName()+ "}";
    }

    /** Get the workspace. This method never returns null, since there
     *  is always a workpace.
     *  @return The workspace responsible for this object.
     */	
    public Workspace workspace() {
        return _workspace; 
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         public variables                        ////

    /** Indicate that the description(int) method should include everything.
     */ 
    public static final int ALL = ~0;

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

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

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
            workspace().write();
            try {
                if (_attributes == null) {
                    _attributes = new NamedList();
                }
                _attributes.append(p);
            } catch (IllegalActionException ex) {   
                // a Attribute cannot be constructed without a name, so we can
                // ignore the exception.
            }
        } finally {
            workspace().doneWriting();
        }
    }
 
    /** Clear references that are not valid in a cloned object.  The clone()
     *  method makes a field-by-field copy, which in derived classes results
     *  in invalid references to objects.  For example, Port has a private
     *  member _relationsList that refers to an instance of CrossRefList.
     *  The clone() method copies this reference, leaving the cloned port
     *  having a reference to exactly the same CrossRefList.   But the
     *  CrossRefList has not back reference to the cloned object.  Thus,
     *  the Port class should override this method to set that member to null.
     *  In this base class, this method sets the private _attributes member,
     *  which refers to a list of attributes, to null.
     *  @param ws The workspace that the cloned object is to be placed in.
     */
    protected void _clearAndSetWorkspace(Workspace ws) {
        _attributes = null;
        _workspace = ws;
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in this class (NamedObj).  Lines are indented according to
     *  to the level argument using the protected method _indent().
     *  It is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent) {
        try {
            workspace().read();
            String result = _indent(indent);
            if((detail & CLASSNAME) != 0) {
                result += getClass().getName();
                if((detail & CLASSNAME) != 0) {
                    result += " ";
                }
            }
            if((detail & FULLNAME) != 0) {
                result = result + "{" + getFullName() + "}";
            }
            if((detail & ATTRIBUTES) != 0) {
                if ((detail & (CLASSNAME | FULLNAME)) != 0) {
                    result += " ";
                }
                result += "attributes {\n";
                // Do not recursively list attributes unless the DEEP
                // bit is set.
                if ((detail & DEEP) == 0) {
                    detail &= ~ATTRIBUTES;
                }
                Enumeration params = getAttributes();
                while (params.hasMoreElements()) {
                    Attribute p = (Attribute)params.nextElement();
                    result = result + p._description(detail, indent+1) + "\n";
                }
                result = result + _indent(indent) + "}";
            }
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _indent(int level) {
        String result = "";
        for (int i=0; i < level; i++) {
            result += "    ";
        }
        return result;
    }

    /** Remove the given attribute.
     *  If there is no such attribute, do nothing.
     *  This method is write-synchronized on the workspace and increments its
     *  version. It should only be called by setContainer() in Attribute.
     *  @param param The attribute to be removed.
     */
    protected void _removeAttribute(NamedObj param) {
        try {
            workspace().write();
            _attributes.remove((Nameable)param);
        } finally {
            workspace().doneWriting();
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // Instance of a workspace that can be used if no other is specified.
    private static Workspace _defaultworkspace = new Workspace();

    // The name
    private String _name;

    // The Attributes attached to this object.
    private NamedList _attributes;

    // The workspace for this object.
    // This should be set by the constructor and never changed.
    private Workspace _workspace;
}
