/* Sample file that uses a type safe enumeration.

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
//// UseTypeSafeEnumeration

/** This class uses a type safe enumeration

@author Christopher Hylands
@version $Id$
*/
public class UseTypeSafeEnumeration implements TestTypeSafeEnumeration {

    /** Get the TypeSafeEnumeration of this TestTypeSafeEnumeration,
     *  as set by setTypeSafeEnumeration().  If
     *  setTypeSafeEnumeration() has not been called, then
     *  implementations of this interface should return some default,
     *  not null, indicating user-level TypeSafeEnumeration. The
     *  returned value is one of the static instances of the
     *  TypeSafeEnumeration inner class.  
     *  @return The TypeSafeEnumeration of this Settable.
     */
    public TestTypeSafeEnumeration.TypeSafeEnumeration getTypeSafeEnumeration(
            ) {
        return _typeSafeEnumeration;
    }

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
            TestTypeSafeEnumeration.TypeSafeEnumeration typeSafeEnumeration) {
        _typeSafeEnumeration = typeSafeEnumeration;                
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private TestTypeSafeEnumeration.TypeSafeEnumeration _typeSafeEnumeration =
            TestTypeSafeEnumeration.NEUTRAL;
}
