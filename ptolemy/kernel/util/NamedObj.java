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

package pt.kernel;

import pt.data.*;
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
container, then the full name is the same as the simple name. The
full name is used for error reporting throughout the package, which
is why it is supported at this low level of the class hierarchy.
<p>
Instances of this class are associated with a workspace, specified
as a constructor argument.  The workspace is immutable.  It cannot
be changed during the lifetime of the object.  It is used for
synchronization of methods that depend on or modify the state of
objects within it.  It is also used for version tracking.
Any method in this class or derived classes that changes visible
state of any object within the workspace should call the
incrVersion() method of the workspace.  If no workspace is
specified, then the default workspace is used.
<p>
The container for instances of this class is always null, although
derived classes that support hierarchy may report a non-null container.
If they do, then they are not explicitly listed in the workspace, although
they will still use it for synchronization.  I.e., the workspace
keeps track only of top-level objects in the containment hierarchy.
Any object contained by another uses the workspace of its container
as its own workspace by default.

@author Mudit Goel, Edward A. Lee
@version $Id$
*/

public class NamedObj implements Nameable, Serializable, Cloneable {

    /** Construct an object in the default workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NamedObj() {
        _workspace = _defaultworkspace;
        // Ignore exception that can't occur.
        try {
            _workspace.add(this);
            setName("");
        } catch (IllegalActionException ex) {}
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
        // Exception cannot occur, so we ignore.
        try {
            workspace.add(this);
        } catch (IllegalActionException ex) {}
        setName(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Add a parameter.
     *  Increment the version number of the workspace.
     *  This method is synchronized on the workspace and increments its
     *  version.
     *  @param p The parameter to be added.
     *  @exception NameDuplicationException If this object already
     *   has a parameter with the same name.
     *  @exception IllegalActionException If the argument is not
     *   contained by this NamedObj.
     */
    public void addParam(Param p)
            throws NameDuplicationException, IllegalActionException {
        synchronized(workspace()) {
            if (((NamedObj)p.getContainer()) != this) {
                throw new IllegalActionException(
                        "Attempt to attach a parameter to a namedObj " +
                        "that is not its container");
            }
            try {
                if (_params == null) {
                    _params = new NamedList();
                }
                _params.append(p);
            } catch (IllegalActionException ex) {   
                // a Param cannot be constructed without a name, so we can
                // ignore the exception.
            }
            workspace().incrVersion();
        }
    }

    /** Clone the object and register the clone in the workspace.
     *  This overrides the protected <code>clone()</code> method of
     *  java.lang.Object, which makes a field-by-field copy, to
     *  clone the parameter list and to call the protected method
     *  _clear(), which is defined in derived classes to remove
     *  references that should not be in the clone.
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     */
    public Object clone() throws CloneNotSupportedException {
        NamedObj result = (NamedObj)super.clone();
        result._clear();
        try {
            workspace().add(result);
        } catch (IllegalActionException ex) {
            // Ignore.  Can't occur.
        }
        // FIXME: Clone the parameter list.
        return result;
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the Nameable interface.  This method returns an empty
     *  string (not null) if there is nothing to report.
     *  This method is synchronized on the workspace.
     *  @param verbosity The level of detail.
     *  @return A description of the object.
     */
    public String description(int verbosity) {
        synchronized (workspace()) {
            String result = new String("");
            if((verbosity & CLASS) != 0) {
                result = getClass().getName();
                if((verbosity & NAME) != 0) {
                    result += " ";
                }
            }
            if((verbosity & NAME) != 0) {
                result = result + "{" + getFullName() + "}";
            }
            if((verbosity & PARAMS) != 0) {
                // FIXME -- implement.
            }
            return result;
        }
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
     *  of this othe name of this object, if there are containers.
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, results in a runtime exception of class
     *  InvalidStateException.  Note that it is 
     *  not possible to construct a recursive structure using this class alone,
     *  since there is no container.
     *  But derived classes might erroneously permit recursive structures,
     *  so this error is caught here.
     *  This method is synchronized on the workspace.
     *  @return The full name of the object.
     */
    public String getFullName() {
        synchronized (workspace()) {
            String fullname = new String(getName());
            // Use a linked list to keep track of what we've seen already.
            LinkedList visited = new LinkedList();
            visited.insertFirst(this);
            Nameable parent = getContainer();

            while (parent != null) {
                if (visited.firstIndexOf(parent) >= 0) {
                    // Cannot use this pointer or we'll get stuck infinitely
                    // calling this method, since it's used to report
                    // exceptions.  This is a runtime exception.
                    throw new InvalidStateException(
                            "Container contains itself!");
                }
                fullname = parent.getName() + "." + fullname;
                visited.insertFirst(parent);
                parent = parent.getContainer();
            }
            return workspace().getName() + "." + fullname;
        }
    }

    /** Get the name. If no name has been given, or null has been given,
     *  then return an empty string, "".
     *  @return The name of the object. 
     */	
    public String getName() { 
        return _name; 
    }

    /** Get the parameter with the given name.
     *  This method is synchronized on the workspace.
     *  @param name The name of the desired parameter.
     *  @return The requested parameter if it is found, null otherwise
     */
    public Param getParam(String name) {
        synchronized(workspace()) {
            return (Param) _params.get(name);
        }
    }

    /** Return an enumeration of the parameters attached to this object.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of Param objects.
     */
    public Enumeration getParams() {
        synchronized(workspace()) {
            if  (_params == null) {
                return (new NamedList()).getElements();
            } else {
                return _params.getElements();
            }
        }
    }

    /** Remove the parameter with the given name.
     *  If there is no such parameter, do nothing.
     *  This method is synchronized on the workspace and increments its
     *  version.
     *  @param name The name of the parameter to be removed.
     *  @return The removed parameter if it is found, null otherwise.
     */
    public Param removeParam(String name) {
        synchronized(workspace()) {
            Param p = (Param)_params.remove(name);
            workspace().incrVersion();
            return p;
        }
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is synchronized on the workspace.
     *  @param name The new name.
     */
    public void setName(String name) {
        if (name == null) {
            name = new String("");
        }
        synchronized (workspace()) {
            _name = name;
            workspace().incrVersion();
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

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Clear references that are not valid in a cloned object.  The clone()
     *  method makes a field-by-field copy, which in derived classes results
     *  in invalid references to objects.  For example, Port has a private
     *  member _relationsList that refers to an instance of CrossRefList.
     *  The clone() method copies this reference, leaving the cloned port
     *  having a reference to exactly the same CrossRefList.   But the
     *  CrossRefList has not back reference to the cloned object.  Thus,
     *  the Port class should override this method to set that member to null.
     *  In this base class, this method sets the private _params member,
     *  which refers to a list of parameters, to null.
     */
    protected void _clear() {
        _params = null;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    // Instance of a workspace that can be used if no other is specified.
    protected static Workspace _defaultworkspace = new Workspace();

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The name
    private String _name;

    // The Params attached to this object.
    private NamedList _params;

    // The workspace for this object.
    // This should be set by the constructor and never changed.
    private Workspace _workspace;
}
