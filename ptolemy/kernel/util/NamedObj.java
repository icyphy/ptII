/* Concrete base class for objects with a name and a container.

 Copyright (c) 1997 The Regents of the University of California.
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
*/

package pt.kernel;

import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// NamedObj
/** 
Base class for objects with a name and a container.
A simple name is an arbitrary string. If no simple name is
provided, the name is taken to be an empty string (not a null
reference). The class also has a full name, which is a concatenation
of the container's full name and the simple name, separating by a
period. Obviously, if the simple name contains a period then there
may be some confusion resolving the full name, so periods are discouraged.
If there is no container, then the full name is the same as
the simple name. The full name is used for error reporting
throughout the package, which is why it is supported at this low
level of the class hierarchy.

@author Mudit Goel, Edward A. Lee
@version $Id$
*/

public class NamedObj implements Nameable {

    /** Construct an object with an empty string as its name. */	
    public NamedObj() {
        // Ignore exception that can't occur because name is legit.
        try {
            setName("");
        } catch (IllegalActionException ex) {};
    }

    /** Construct an object with the given name.
     *  @exception IllegalActionException Argument is null.
     */
    public NamedObj(String name) 
            throws IllegalActionException {
	setName(name);
    }

    /** Construct an object with the given container and name.
     *  @exception IllegalActionException Name argument is null.
     */
    public NamedObj(Nameable container, String name)
            throws IllegalActionException {
        _container = container;
        setName(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Get the owner or container.
     *  @return The owner object.
     */
    public Nameable getContainer() {
	return _container;
    }

    /** The full name is a concatenation of the full name of the container
     *  and the name of the this object, separated by a period ".".  If there
     *  is no container, return getName().
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, results in an exception.  Note that it is not
     *  possible to construct a recursive structure using this class, since
     *  the only way to set the container is using a constructor argument.
     *  But derived classes might permit recursive structures.
     *  @return The full name of the object.
     *  @exception InvalidStateException Container contains itself.
     */
    public String getFullName() 
            throws InvalidStateException {
        String fullname = new String(getName());
        // Use a linked list to keep track of what we've seen already.
        LinkedList visited = new LinkedList();
        visited.insertFirst(this);
        Nameable parent = _container;

        while (parent != null) {
            if (visited.firstIndexOf(parent) >= 0) {
                // Cannot use this pointer or we'll get stuck infinitely
                // calling this method, since it's used to report exceptions.
                throw new InvalidStateException(
                        "Container contains itself.");
            }
            fullname = parent.getName() + "." + fullname;
            visited.insertFirst(parent);
            parent = parent.getContainer();
        }
        return fullname;
    }

    /** Get the name.
     *  @return The name of the object. 
     */	
    public String getName() { 
        return _name; 
    }

    /** Set or change the name.
     *  @param name The new name.  
     */
    public void setName(String name)
            throws IllegalActionException {
        if (name == null) {
            throw new IllegalActionException(
                    "Attempt to set name of a NamedObj to null.");
        }
        _name = name;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    // The Entity that owns this object.
    // Note that this should only be accessed by derived classes.
    // Java does not provide any mechanism for enforcing that restriction,
    // so we rely on convention.
    protected Nameable _container = null;

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private String _name;
}

