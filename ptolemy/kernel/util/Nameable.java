/* Interface for objects with a name and a container.

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
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// Nameable
/**
This is an interface for objects with a name and a container. A simple
name is an arbitrary string that identifies the object in the context of
its container. In addition, the interface supports a
"full name" which in implementation should identify both the container
and the individual object. The implementations in the kernel package
define the full name of an object to be the full name of its container
followed by a period followed by the simple name of the object.
Periods and braces are not permitted in the name.

@author Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
*/

public interface Nameable extends ModelErrorHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a description of the object. The general
     *  form of the description is a space-delimited list of the form
     *  "className fullName <i>keyword</i> field <i>keyword</i> field ...".
     *  If any of the items contain spaces, then they must be surrounded
     *  by braces, as in "{two words}". Return characters or newlines
     *  may be be used as delimiters as well. The fields are usually
     *  lists of descriptions of this same form, although different
     *  forms can be used for different keywords.  The keywords are
     *  extensible, but the following are in use: links, ports, entities,
     *  relations, attributes, and inside links, at least.
     *  @return A description of this object.
     */
    public String description();

    /** Return the container. */
    public NamedObj getContainer();

    /** Return the full name, which reflects the container object, if there
     *  is one. For example the implementation in NamedObj concatenates the
     *  full name of the container objects with the name of the this object,
     *  separated by periods.
     *  @return The full name of the object.
     */
    public String getFullName();

    /** Return the name of the object.
     *  @return The name of the object.
     */
    public String getName();
    
    /** Get the name of this object relative to the specified container.
     *  If this object is contained directly by the specified container,
     *  this is just its name, as returned by getName().  If it is deeply
     *  contained by the specified container, then the relative name is
     *  <i>x1</i>.<i>x2</i>. ... .<i>name</i>, where <i>x1</i> is directly
     *  contained by the specified container, <i>x2</i> is contained by
     *  <i>x1</i>, etc.  If this object is not deeply contained by the
     *  specified container, then this method returns the full name of
     *  this object, as returned by getFullName().
     *  @param relativeTo The object relative to which you want the name.
     *  @return A string of the form "name2...nameN".
     *  @exception InvalidStateException If a recursive structure is
     *   encountered, where this object directly or indirectly contains
     *   itself. Note that this is a runtime exception so it need not
     *   be declared explicitly.
     */
    public String getName(NamedObj relativeTo) throws InvalidStateException;

    /** Set or change the name. By convention, if the argument is null,
     *  the name should be set to an empty string rather than to null.
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException If the container already
     *   contains an object with this name.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException;
}
