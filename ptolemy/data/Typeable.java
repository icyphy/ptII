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

import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Typeable
/**
Interface for objects with types. This interface defines methods for
setting and getting types and type constraints. Type constraints are
represented as inequalities between Typeable objects.

@author Yuhong Xiong, Xiaojun Liu, Edward A. Lee
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

    /** Return an InequalityTerm representing this object.
     *  @return An InequalityTerm.
     */
    InequalityTerm getTypeTerm();

    /** Constrain the type of this object to be equal to or greater
     *  than the type of the argument.  Notice that this constraint
     *  is not enforced until type resolution is done, and is not
     *  enforced if type resolution is not done.
     */
    void setTypeAtLeast(Typeable lesser);

    /** Constrain the type of this object to be equal to or less
     *  than the argument.  Because the argument is a concrete type,
     *  rather than a Typeable object (which may not yet have a type),
     *  the constraint is immediately enforced.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint.
     */
    void setTypeAtMost(Class type) throws IllegalActionException;

    /** Set a type constraint that the type of this object equal
     *  the specified value.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint.
     */
    void setTypeEquals(Class type) throws IllegalActionException;

    /** Constrain the type of this object to be the same as the
     *  type of the argument.  Notice that this constraint
     *  is not enforced until type resolution is done, and is not
     *  enforced if type resolution is not done.
     */
    void setTypeSameAs(Typeable equal);

    /** Return the type constraints of this object.
     *  The constraints are an enumeration of inequalities.
     *  @return an enumeration of instances of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    Enumeration typeConstraints();
}
