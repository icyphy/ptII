/** Interface for objects with types.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu, liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data;

//////////////////////////////////////////////////////////////////////////
//// Typeable
/**
Interface for objects with types. This interface defines methods for
setting and getting types and type constraints. For type resolution,
each Typeable object must encapsulte its type in an InequalityTerm,
this term can be obtained through getTypeTerm().

@author Yuhong Xiong, Xiaojun Liu
$Id$
@see ptolemy.graph.InequalityTerm
*/

public interface Typeable
{
    /** Return the type of this object. If the type is not
     *  determined, return null.
     *  @return An instance of Class representing the type.
     */
    Class getType();

    /** Return an InequalityTerm whose value is the type of this object.
     *  @return An InequalityTerm.
     */
    InequalityTerm getTypeTerm();

    /** Set the type of this object to the specified value.
     */
    void setType(Class type);

    /** Constraint that the type of this object is equal to or greater
     *  than the type of the specified object.
     */
    void setTypeAtLeast(Typeable lesser);

    /** Constraint that the type of this object is the same as the
     *  type of the specified object.
     */
    void setTypeEquals(Typeable equal);

    /** Return the type constraints of this object.
     *  The constraints are an enumeration of inequalities.
     *  @return an enumeration of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    Enumeration typeConstraints();
}

