/* Sample file that contains a type safe enumeration, like kernel.util.Settable.Visibility.

 Copyright (c) 2000 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.lang.java.test; 
//////////////////////////////////////////////////////////////////////////
//// JavaScope

/** Sample file that contains a type safe enumeration, like kernel.util.Settable.Visibility.

@author Christopher Hylands
@version $Id$
*/
public interface TestTypeSafeEnumeration {

    /** Get the TypeSafeEnumeration of this TestTypeSafeEnumeration,
     *  as set by setTypeSafeEnumeration().  If
     *  setTypeSafeEnumeration() has not been called, then
     *  implementations of this interface should return some default,
     *  not null, indicating user-level TypeSafeEnumeration. The
     *  returned value is one of the static instances of the
     *  TypeSafeEnumeration inner class.  
     *  @return The TypeSafeEnumeration of this Settable.
     */
    public TestTypeSafeEnumeration.TypeSafeEnumeration getTypeSafeEnumeration();

    /** Set the TypeSafeEnumeration of this TestTypeSafeEnumeration.
     *  The argument should be one of the static public instances of
     *  the inner class TypeSafeEnumeration.  This is enforced by
     *  making it impossible to construct instances of this inner
     *  class outside this interface definition.  If this method is
     *  not called, then implementations of this interface should
     *  return some default, not null.
     *
     *  @param TypeSafeEnumeration The TypeSafeEnumeration of this Settable.
     */
    public void setTypeSafeEnumeration(
            TestTypeSafeEnumeration.TypeSafeEnumeration typeSafeEnumeration);

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Indicator that we are in FORWARD gear. */
    public static TypeSafeEnumeration FORWARD = new TypeSafeEnumeration();

    /** Indicator that we are in NEUTRAL gear. */
    public static TypeSafeEnumeration NEUTRAL = new TypeSafeEnumeration();

    /** Indicator that we are in REVERSE gear. */
    public static TypeSafeEnumeration REVERSE = new TypeSafeEnumeration();


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Inner class used for the static enumeration of indicators of
     *  TypeSafeEnumeration.  Instances of this class cannot be
     *  constructed outside the enclosing interface because its
     *  constructor is private.
     */

    public static class TypeSafeEnumeration {

        // Private constructor prevents construction outside.
        // This constructor should not be called!
        // it is protected to work around a compiler bug in JDK1.2.2
        protected TypeSafeEnumeration() {}
    }
}
