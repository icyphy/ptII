/* A declaration of some entity.

Copyright (c) 1998-2001 The Regents of the University of California.
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
A Decl encapsulates information about a declaration of some entity.
There is a unique Decl for each Decl in the compilation.
<p>
The class Decl and its subclasses declare attributes, most of which make
sense only for certain types of Decl.  Attempts to access nonsensical
attributes will cause runtime errors.
<p>
By convention, a Decl member named "getFoo" will return the "foo"
attribute when called with no parameters, and a member "setFoo" will
set the "foo" attribute when called with one parameter.
Also, if member "foo" is not valid for all Decls, there is a member
"hasFoo()" that returns true or false depending on whether object
on which it is called has a class for which "foo" may be called.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public abstract class Decl extends TrackedPropertyMap {

    public Decl(String name, int newCategory) {
        _name = name;
        category = newCategory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override Object.equals() so that equality is defined as having
     *  the same name and category. If the object being compared against
     *  is not a Decl, throw a RuntimeException.
     *  @param object The object to compare against.
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof Decl)) {
            throw new RuntimeException("cannot compare a Decl " +
                    "with a non-Decl");
        }

        Decl decl = (Decl) object;
        return matches(decl.getName(), decl.category);
    }

    /** Return the name of this Decl.
     *  @return The name of this Decl.
     */
    public final String getName() { return _name; }

    /** Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * java.util.Hashtable.
     */
    public int hashCode() {
        // If we override equals, we should override hashCode.
        // http://java.sun.com/docs/books/tutorial/java/javaOO/objectclass.html
        // FIXME: should we divide by 2 here or not to avoid overflow?
        return _name.hashCode()/2 + category/2;
    }

    /** Return true if at least some of the bits in the mask are set
     *  in the category argument and the name argument matches the name of
     *  this Decl, or the name argument is ANY_NAME or the name of
     *  the Decl is ANY_NAME.
     *  @param name The String to compare against.
     *  @param mask The mask to compare against.
     *  @return true If the name and mask match.
     */
    public final boolean matches(String name, int mask) {
        if ((category & mask) != 0) {
            return (name.equals(ANY_NAME) ||
                    _name.equals(ANY_NAME)||
                    name.equals(_name));
        }
	// If two Decls have a category of 0, then they
	// do technically match
	if (category == 0 && mask == 0) {
	    return (name.equals(ANY_NAME) || name.equals(_name));
	}
        return false;
    }

    /** Set the name of this Decl. */
    public final void setName(String name) { _name = name; }

    /** Return a String representation of this Decl.
     *  @return The string representation of this Decl.
     */
    public String toString() {
        return "{" + _name + ", " + category + "}";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public int category;

    public static final int CG_ANY = 0xFFFFFFFF; // Any category
    public static final String ANY_NAME = "*";

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected String _name;
}
