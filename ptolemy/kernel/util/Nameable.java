/* Interface for objects with a name and a container.

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

//////////////////////////////////////////////////////////////////////////
//// Nameable
/** 
This is an interface for objects with a name and a container. A simple
name is an arbitrary string. In addition, the interface supports a
"full name" which in implementation should identify both the container
and the individual object.

@author Christopher Hylands, Edward A. Lee
@version $Id$
*/

public interface Nameable {

    /////////////////////////////////////////////////////////////////////////
    ////                         public methods                          ////

    /** Return a description of the object.  The level of detail is given
     *  by or-ing the static final ints defined in this class. The general
     *  form of the description is
     *  "classname fullname keyword field keyword field ...".
     *  The classname and fullname are optional.  They are given if
     *  the argument anded with CLASS and NAME is non-zero, respectively.
     *  The fields are usually lists of descriptions, although different
     *  forms can be used for different keywords.  The keywords are
     *  extensible, but the following are in use: links, ports, entities,
     *  relations, and insidelinks, at least.
     *  @param verbosity The level of detail.
     *  @return A description of the object.
     */
    public String description(int verbosity);

    /** Return the container. */
    public Nameable getContainer();

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

    /** Set or change the name. By convention, if the argument is null,
     *  the name should be set to an empty string rather than to null.
     *  @param name The new name.
     */
    public void setName(String name);

    /////////////////////////////////////////////////////////////////////////
    ////                         public variable                         ////

    /** Indicate that the description() method should include the class name.
     */ 
    public static final int CLASS = 1;

    /** Indicate that the description() method should include the full name.
     *  The full name is surrounded by braces "{name}" in case it has spaces.
     */ 
    public static final int NAME = 2;

    /** Indicate that the description() method should include the links
     *  (if any) that the object has.  This has the form "links {...}"
     *  where the list is a list of descriptions of the linked objects.
     */ 
    public static final int LINKS = 4;

    /** Indicate that the description() method should include the contained
     *  objects (if any) that the object has.  This has the form
     *  "keyword {{class {name}} {class {name}} ... }" where the keyword
     *  can be ports, entities, relations, or anything else that might
     *  indicate what the object contains.
     */ 
    public static final int CONTENTS = 8;

    /** Indicate that the description() method should include the contained
     *  objects (if any) that the contained objects have.  This has no effect
     *  if CONTENTS is not also specified.  The returned string has the form
     *  "keyword {{class {name} keyword {...}} ... }".
     */ 
    public static final int DEEP = 16;

    /** Indicate that the description() method should include parameters
     *  (if any).
     */ 
    public static final int PARAMS = 32;
}
