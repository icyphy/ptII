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
<p>
Any object that implements the Nameable interface can be attached
to this object as a parameter using addParameter().  Derived classes
may constrain the parameter class further.  To do that, they should
override the addParameter() method to throw an exception if the
object provided is not of the right class.

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

    /** Add a parameter.  A parameter is any instance of NamedObj that
     *  has this object set as its container.
     *  WARNING: This method should only be called by methods which guarantee 
     *  the above condition. Otherwise inconsistant states may result.
     *  Derived classes may further constrain the class of the parameter.
     *  To do this, they should override this method to throw an exception
     *  when the argument is not an instance of the expected class.
     *  This method is synchronized on the workspace and increments its
     *  version number.
     *  @param p The parameter to be added.
     *  @exception NameDuplicationException If this object already
     *   has a parameter with the same name.
     *  @exception IllegalActionException If the parameter is not an
     *   an instance of the expect class (in derived classes, or if
     *   the parameter does not have this object as its container.
     */
    public void addParameter(NamedObj p)
            throws NameDuplicationException, IllegalActionException {
        // NOTE: The argument is a NamedObj rather than Nameable because
        // we need a public clone() method.
        synchronized(workspace()) {
            if (p.getContainer() != this) {
                throw new IllegalActionException(this, p,
                        "Attempt to add a parameter to a NamedObj " +
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
 

    /** Clone the object and register the cloned object in the 
     *  workspace in which the original object resides.
     *  This overrides the protected <code>clone()</code> method of
     *  java.lang.Object, which makes a field-by-field copy, to
     *  clone the parameter list and to call the protected method
     *  _clear(), which is defined in derived classes to remove
     *  references that should not be in the clone.
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     */
    public Object clone() throws CloneNotSupportedException {
        return clone(workspace());
    }

    /** Clone the object and register the cloned object in the specified 
     *  workspace. This uses the <code>clone()</code> method of
     *  java.lang.Object, which makes a field-by-field copy, and then
     *  calls the protected method _clear(), which is expected to remove
     *  references that should not be in the clone. It then clones the
     *  parameter list, if there is one.
     *  @param ws The workspace in which to place the cloned object.
     *  @exception CloneNotSupportedException If any of the parameters
     *   cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // NOTE: It is safe to clone an object into a different workspace.
        // It is not safe to move an object to a new workspace, by contrast.
        // The reason this is safe is that after the _clear() method has been
        // run, there are no references in the clone to objects in the old
        // workspace.  Moreover, no object in the old workspace can have
        // a reference to the cloned object because we have only just created
        // it and have not returned the reference.
        NamedObj result = (NamedObj)super.clone();
        result._clear(ws);
        try {
            ws.add(result);
        } catch (IllegalActionException ex) {
            // Ignore.  Can't occur.
        }
        Enumeration params = getParameters();
        while (params.hasMoreElements()) {
            NamedObj p = (NamedObj)params.nextElement();
            NamedObj np = (NamedObj)p.clone(ws);
            try {
                np.setContainer(result);         
            } catch (KernelException ex) {
                throw new CloneNotSupportedException(
                        "Failed to clone a Parameter: " + ex.getMessage());
            }
        }
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
     *  of this other name of this object, if there are containers.
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
    public NamedObj getParameter(String name) {
        synchronized(workspace()) {
            return (NamedObj) _params.get(name);
        }
    }

    /** Return an enumeration of the parameters attached to this object.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of instances of NamedObj.
     */
    public Enumeration getParameters() {
        synchronized(workspace()) {
            if  (_params == null) {
                return (new NamedList()).getElements();
            } else {
                return _params.getElements();
            }
        }
    }
    
    /** Remove the given parameter.
     *  If there is no such parameter, do nothing.
     *  This method is synchronized on the workspace and increments its
     *  version. It should only be called by setContainer() in Parameter.
     *  @param param The parameter to be removed.
     */
    public void removeParameter(NamedObj param) {
        synchronized(workspace()) {
            _params.remove((Nameable)param);
            workspace().incrVersion();
        }
    }
    

    /** Set the container. In this base class throw an exception as it 
     *  cannot have a container. This method should be overridden in 
     *  derived classes to check the argument is a valid type.
     *  @param namedObj The container of this object.
     *  @exception IllegalException This method should be overridden.
     *  @exception NameDuplicationException Thrown in derived classes.
     */
     public void setContainer(NamedObj namedobj) 
             throws IllegalActionException, NameDuplicationException {
         String str = "setContainer method in NamedObj should be overridden";
         throw new IllegalActionException(this, namedobj, str);
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
     *  @param ws The workspace the cloned object is to be placed in.
     */
    protected void _clear(Workspace ws) {
        _params = null;
        _workspace = ws; //is this correct?
    }

    
    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    // Instance of a workspace that can be used if no other is specified.
    protected static Workspace _defaultworkspace = new Workspace();

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The name
    private String _name;

    // The Parameters attached to this object.
    private NamedList _params;

    // The workspace for this object.
    // This should be set by the constructor and never changed.
    private Workspace _workspace;
}
