/** Interface for objects with types.

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

@ProposedRating Red (yuhong@eecs.berkeley.edu, liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Typeable
/**
Interface for objects with types. This interface defines methods for
setting and getting types and type constraints. Type constraints are
represented as inequalities between Typeable objects.

@author Yuhong Xiong, Xiaojun Liu, Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
@see ptolemy.graph.InequalityTerm
*/

public interface Typeable extends HasTypeConstraints {
    /** Return the type of this object. An exception is thrown if the type
     *  cannot be determined. This can happen if the type of this object
     *  is dependent on some other objects whose value is not available yet.
     *  @return An instance of Type.
     *  @exception IllegalActionException If the type cannot be determined.
     */
    public Type getType() throws IllegalActionException;

    /** Return an InequalityTerm representing this object.
     *  @return An InequalityTerm.
     */
    public InequalityTerm getTypeTerm();

    /** Check whether the type of this object is acceptable.
     *  @return True if the type of this object is acceptable.
     */
    public boolean isTypeAcceptable();

    /** Constrain the type of this object to be equal to or greater
     *  than the type of the argument.  Notice that this constraint
     *  is not enforced until type resolution is done, and is not
     *  enforced if type resolution is not done.
     *  @param lesser A Typeable object.
     */
    public void setTypeAtLeast(Typeable lesser);

    /** Constrain the type of this object to be equal to or greater
     *  than the type represented by the specified InequalityTerm. Notice
     *  that this constraint is not enforced until type resolution is done,
     *  and is not enforced if type resolution is not done.
     *  @param typeTerm An InequalityTerm object.
     */
    public void setTypeAtLeast(InequalityTerm typeTerm);

    /** Constrain the type of this object to be equal to or less
     *  than the argument.  Because the argument is a concrete type,
     *  rather than a Typeable object (which may not yet have a type),
     *  the constraint is immediately enforced.
     *  @param type An instance of Type.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint.
     */
    public void setTypeAtMost(Type type) throws IllegalActionException;

    /** Set a type constraint that the type of this object equal
     *  the specified value.
     *  @param type An instance of Type.
     *  @exception IllegalActionException If the type of this object
     *   already violates this constraint.
     */
    public void setTypeEquals(Type type) throws IllegalActionException;

    /** Constrain the type of this object to be the same as the
     *  type of the argument.  Notice that this constraint
     *  is not enforced until type resolution is done, and is not
     *  enforced if type resolution is not done.
     */
    public void setTypeSameAs(Typeable equal);
}
