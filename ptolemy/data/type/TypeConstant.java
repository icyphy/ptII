/* An InequalityTerm that encapsulates a constant type.

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
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;

//////////////////////////////////////////////////////////////////////////
//// TypeConstant
/**
An InequalityTerm that encapsulate a constant type. The constant type
is specified in the constructor.
This class represents a constant term in an inequality constraint for
type resolution.

@author Yuhong Xiong
@version $Id$
@see ptolemy.graph.InequalityTerm
*/

public class TypeConstant implements InequalityTerm {

    /** Construct a TypeConstant.
     *  @param type An instance of Type.
     */
    public TypeConstant(Type type) {
	_type = type;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing, since this term is a constant.
     */
    public void fixValue() {
    }

    /** Return null.
     *  @return null.
     */
    public Object getAssociatedObject() {
	return null;
    }

    /** Return the constant type represented by this term.
     *  @return A Type.
     */
    public Object getValue() {
	return _type;
    }

    /* Return an array of size zero.
     * @return An array of InequalityTerm of size 0.
     */
    public InequalityTerm[] getVariables() {
	return (new InequalityTerm[0]);
    }

    /** Throw an Exception since type constant cannot be initialized.
     *  @exception IllegalActionException Always thrown.
     */
    public void initialize(Object e)
	    throws IllegalActionException {
	throw new IllegalActionException("TypeConstant.initialize: " +
	    "Type constant cannot be initialized.");
    }

    /** Return false since this term represent a constant.
     *  @return false.
     */
    public boolean isSettable() {
	return false;
    }

    /** Check whether the current type of this term is acceptable,
     *  and return true if it is.  A type is acceptable
     *  if it represents an instantiable object.
     *  @return True if the current type is acceptable.
     */
    public boolean isValueAcceptable() {
        if (_type.isInstantiable()) {
            return true;
        }
        return false;
    }

    /** Throw IllegalActionException since the value of this term
     *  cannot be changed.
     *  @exception IllegalActionException Always thrown.
     */
    public void setValue(Object e)
	    throws IllegalActionException {
	throw new IllegalActionException("TypeConstant.setValue: Cannot set "
                + "the value of a type constant.");
    }

    /** Return a string representation of this term.
     *  @return A String.
     */
    public String toString() {
	return "(TypeConstant, " + getValue() + ")";
    }

    /** Do nothing, since this term is a constant.
     */
    public void unfixValue() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////

    private Type _type = null;
}
