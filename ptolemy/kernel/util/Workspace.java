/* An object for synchronization and version tracking of groups of objects.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)

*/

package pt.kernel.util;

import java.io.Serializable;
import collections.LinkedList;
import collections.CollectionEnumeration;

//////////////////////////////////////////////////////////////////////////
//// Workspace
/** 
A Workspace is used for synchronization and version tracking 
of interdependent groups of objects.
Elements may register with the workspace by calling add(),
although they are not required to do so in order to use the workspace.
The names of the elements in the workspace are not required to be unique.

@author Edward A. Lee
@version $Id$
*/
public class Workspace implements Nameable, Serializable {

    /** Create a workspace with an empty string as its name.
     */	
    public Workspace() {
        setName("");
        _contents = new LinkedList();
    }

    /** Create a workspace with the specified name.  This name will form the
     *  prefix of the full name of all contained objects.
     *  @exception IllegalActionException Argument is null.
     *  @param name Name of the workspace to be created.
     */	
    public Workspace(String name)
            throws IllegalActionException {
        setName(name);
        _contents = new LinkedList();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Append an element to the contents list. The names of the objects
     *  in the workspace are not required to be unique.
     *  Only elements with no container can be added.  Elements with
     *  a container are still viewed as being within the workspace, but
     *  they are not explicitly listed in the contents list.  Instead,
     *  their top-level container is expected to be listed (although this
     *  is not enforced).  Increment the version number.
     *  @param element Object to contain.
     *  @exception IllegalActionException Object has a container or is
     *   already on the list.
     */	
    public synchronized void add(Nameable element)
            throws IllegalActionException {
        if (element.getContainer() != null) {
            throw new IllegalActionException(this, element,
                    "Cannot add an object with a container to a workspace.");
        }
        if (_contents.firstIndexOf(element) >= 0) {
            throw new IllegalActionException(this, element,
                    "Object is already listed in the workspace.");
        }
        _contents.insertLast(element);
        incrVersion();
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the Nameable interface.
     *  This method is synchronized on the workspace.
     *  @param verbosity The level of detail.
     *  @return A description of the object.
     */
    public synchronized String description(int verbosity){
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
        if ((verbosity & CONTENTS) != 0) {
            if (result.length() > 0) {
                result += " ";
            }
            result += "elements {";
            CollectionEnumeration enum = elements();
            while (enum.hasMoreElements()) {
                NamedObj obj = (NamedObj)enum.nextElement();
                // If deep is not set, the zero-out the contents flag
                // for the next round.
                if ((verbosity & DEEP) == 0) {
                    verbosity &= ~CONTENTS;
                }
                result = result + "\n" + obj.description(verbosity);
            }
            result += "\n}";
        }
        return result;
    }

    /** Enumerate the elements in the contents list, in the order in which
     *  they were added.
     *  @return An enumeration of Nameable objects.
     */	
    public synchronized CollectionEnumeration elements() {
        return _contents.elements();
    }

    /** Get the owner or container.  Always return null since a namespace
     *  has no container.
     *  @return null.
     */
    public Nameable getContainer() {
	return null;
    }

    /** Get the name.
     *  @return The name of the workspace.
     */
    public String getFullName() {
        return _name;
    }

    /** Get the name.
     *  @return The name of the object. 
     */	
    public String getName() { 
        return _name; 
    }
    /** Get a the version number
     *  @return A non-negative integer.
     */	
    public synchronized long getVersion() {
        return _version;
    }

    /** Increment the version number by one.
     */	
    public synchronized void incrVersion() {
        _version++;
    }

    /** Remove the specified element from the contents list.
     *  Note that that element will still refer to this workspace as
     *  its workspace (its workspace is immutable).  If the object is
     *  not in the workspace, do nothing.
     *  Increment the version number.
     */	
    public synchronized void remove(Object element) {
        _contents.removeOneOf(element);
        incrVersion();
    }

    /** Remove all elements from the contents list.
     *  Note that those elements will still refer to this workspace as
     *  their workspace (their workspace is immutable).
     *  Increment the version number.
     */	
    public synchronized void removeAll() {
        _contents.clear();
        incrVersion();
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version number.
     *  @param name The new name.
     */
    public synchronized void setName(String name) {
        if (name == null) {
            name = new String("");
        }
        _name = name;
        incrVersion();
    }

    /** Return a concise description of the object. */ 
    public String toString() {
        return "pt.kernel.Workspace {" + getFullName()+ "}";
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // List of contained objects.
    private LinkedList _contents;

    // The name
    private String _name;

    // Version number.
    private long _version;
}
