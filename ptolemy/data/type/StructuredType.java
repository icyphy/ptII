/** Base class for structured type.

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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.expr.Variable;

//////////////////////////////////////////////////////////////////////////
//// StructuredType
/**
Base class for structured type. Making this an abstract class (not an
interface) allows the methods to be protected.
All the types of the same structured type (e.g. all the array types)
must form a lattice. Each instance of a structured type must know how
to compare itself with another instance of the same structured type,
and compute the least upper bound and greatest lower bound. This
interface defines methods for these operations.
<p>
Subclasses should override clone() to do a deep cloning.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
*/

public abstract class StructuredType implements Type, Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep copy this StructuredType. The returned copy does
     *  not have the user set.
     *  @return A StructuredType.
     */
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException ex) {
	    throw new InternalErrorException("StructuredType.clone: " +
		ex.getMessage());
	}
    }

    /** If this type is a variable, disallow it to be changed by the
     *  type resolution procedure. Calling unfixType() will reverse the
     *  effect of this mehtod. This method does nothing if this type
     *  is a constant.
     */
    public abstract void fixType();

    /** Return the user of this StructuredType. If the user is not set,
     *  return null.
     *  @return An Object.
     */
    public abstract Object getUser();

    /** Notify this type that its user, which is a Variable, has changed its
     *  expression so this type may be changed.
     *  @param user A Variable.
     */
    public abstract void needEvaluate(Variable user);

    /** Set the user of this StructuedType. The user can only be set once
     *  Otherwise, an exception will be thrown.
     *  @param Object The user.
     *  @exception IllegalActionException If the user is already set, or
     *   if the argument is null.
     */
    public abstract void setUser(Object user) throws IllegalActionException;

    /** Reset this type to the value it was first constructed.
     *  This method is called at the beginning of type resolution.
     */
     //  @exception IllegalActionException If this type is a constant.
    public abstract void reset();
	    // throws IllegalActionException;

    /** If this type is a variable, allow it to be changed by the type
     *  resolution procedure. This method reverses the effect of fixValue().
     *  If this type is a constant, this method does nothing.
     */
    public abstract void unfixType();

    /** Update this StructuredType to the specified Structured Type.
     *  The specified StructuredType must not be a constant, otherwise an
     *  exception will be thrown. The specified type must have the same
     *  structure as this type. This method will only update the
     *  component type that is BaseType.NAT, and leave the constant
     *  part of this type intact.
     *  @param st A StructuredType.
     *  @exception IllegalActionException If this Structured type
     *   is a constant, or the specified type has a different structure.
     */
    public abstract void updateType(StructuredType st)
	throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Compare this type with the specified type. The specified type
     *  must be of the same structured type, otherwise an exception will
     *  be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param t a StructuredType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected abstract int _compare(StructuredType t);

    /** Determine if the specified StructuredType is a direct or
     *  indirect user of this type. This method returns true if the
     *  argument is this StructuredType itself, or is the user of this
     *  StructuredType, or the user on a higher level.
     *  @return A boolean.
     */
    protected abstract boolean _deepIsUser(Object st);

    /** Return a static instance of this structured type. The return
     *  value is used by TypeLattice to represent this type.
     *  @return a StructuredType.
     */
    protected abstract StructuredType _getRepresentative();

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param t a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected abstract StructuredType _greatestLowerBound(StructuredType t);

    /** Return the least upper bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param t a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected abstract StructuredType _leastUpperBound(StructuredType t);

}

