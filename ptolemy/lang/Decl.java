/*
A declaration of some entity. Code converted from Decl in Titanium.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/
package ptolemy.lang;


//////////////////////////////////////////////////////////////////////////
//// Decl
/**
 *  A Decl encapsulates information about a declaration of some entity.
 *  There is a unique Decl for each Decl in the compilation.
 *
 *  The class Decl and its subclasses declare attributes, most of which make
 *  sense only for certain types of Decl.  Attempts to access nonsensical
 *  attributes will cause runtime errors.
 *
 *  By convention, a Decl member named "getFoo" will return the "foo"
 *  attribute when called with no parameters, and set the "foo"
 *  attribute when called with one parameter.  Thus, decl.getType() is
 *  the type of the entity referred to by decl (a Decl, presumably),
 *  and decl.setFoo(aType) sets the type attribute of decl to aType.
 *  Also, if member "foo" is not valid for all Decls, there is a member
 *  "hasFoo()" that returns true or false depending on whether object
 *  on which it is called has a class for which "foo" may be called.
 *
 *  Objects of type Decl should not be allocated; the class is intended
 *  as a base class for others.
 *
 *   ATTRIBUTE name
 *     All Decls have a name, of type String.  These are
 *     the unique representative strings assigned by lexical analysis.
 *     The names of two Decls are considered the same iff they are the
 *     same pointer, ignoring contents: names that are different pointers
 *     to strings containing the same characters are considered distinct.
 *
 *  This class and comments were converted from Decl in the Titanium project.
 *
 *  @author ctsay@eecs.berkeley.edu
 */
public class Decl extends TrackedPropertyMap {

  protected Decl(String name, int category0) {
    _name = name;
    category = category0;
  }

  public final boolean matches(String name, int mask) {
    if ((category & mask) != 0) {
       return (name.equals(ANY_NAME) || name.equals(_name));
    }
    return false;
  }

  public final String getName() { return _name; }

  public final void setName(String name) { _name = name; }

  public boolean equals(Object o) {
    if (o == this) {
       return true;
    }

    if (!(o instanceof Decl)) {
       return false;
    }

    Decl d = (Decl) o;

    return ((category == d.category) && _name.equals(d.getName()));
  }

  public String toString() {
    return "{" + _name + ", " + category + "}";
  }

  public int category;

  protected String _name;

  public static final int CG_ANY = 0xFFFFFFFF; // Any category
  public static final String ANY_NAME = "*";
}
