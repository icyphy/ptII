/** A class representing the type of a multi-dimensional array.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.*;
import ptolemy.data.Token;
import ptolemy.data.ArrayToken;

//////////////////////////////////////////////////////////////////////////
//// ArrayType
/**
A class representing the type of an array.

@author Steve Neuendorffer, Yuhong Xiong
$Id$
*/

public class ArrayType implements StructuredType {

    /** Construct a new ArrayType with the specified type for the array
     *  elements. To leave the element type undeclared, use BaseType.NAT.
     *  @exception IllegalArgumentException If the argument is null.
     */
    public ArrayType(Type elementType) {
	if (elementType == null) {
	    throw new IllegalArgumentException("ArrayType: elementType is"
			+ " null");
	}
        _declaredElementType = elementType;
        _elementType = elementType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare this type with the specified type. The specified type
     *  must be an ArrayType, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param t an ArrayType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    public int compare(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.compare: " +
		"The argument is not an ArrayType.");
	}

	return TypeLattice.compare(_elementType,
				   ((ArrayType)t).getElementType());
    }

    /** Convert the specified token into an ArrayToken having the
     *  type represented by this object.
     *  @param t A token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token t)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, t.getType());
	if (compare == CPO.INCOMPARABLE || compare == CPO.LOWER) {
	    throw new IllegalArgumentException("ArrayType.convert: " +
		"Cannot convert the argument token to this type.");
	}

	// argument must be an ArrayToken.
	Token[] argArray = ((ArrayToken)t).arrayValue();
	Token[] result = new Token[argArray.length];
	for (int i = 0; i < argArray.length; i++) {
	    result[i] = _elementType.convert(argArray[i]);
	}

	return new ArrayToken(result);
    }

    /** Return the type of the array elements. This methods always
     *  returns the argument passed into the constructor.
     *  @return a Type.
     */
    public Type getElementType() {
	return _elementType;
    }

    /** Return a static instance of ArrayType.
     *  @return an ArrayType.
     */
    public StructuredType getRepresentative() {
	return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    public StructuredType greatestLowerBound(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.greatestLowerBound: "
		+ "The argument is not an ArrayType.");
	}

	Type elementGLB = (Type)TypeLattice.lattice().greatestLowerBound(
			    _elementType, ((ArrayType)t).getElementType());
	return new ArrayType(elementGLB);
    }

    /** Determine if the argument represents the same ArrayType as this
     *  object.
     *  @param t A Type.
     *  @return True if the argument represents the same ArrayType as
     *   this object; false otherwise.
     */
    public boolean isEqualTo(Type t) {
	if ( !(t instanceof ArrayType)) {
	    return false;
	}
	return _elementType.isEqualTo(((ArrayType)t).getElementType());
    }

    /** Determine if this type corresponds to an instantiable token
     *  class. An ArrayType is instantiable if its element type is
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    public boolean isInstantiable() {
	return _elementType.isInstantiable();
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    public StructuredType leastUpperBound(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.leastUpperBound: "
		+ "The argument is not an ArrayType.");
	}

	Type elementLUB = (Type)TypeLattice.lattice().leastUpperBound(
			    _elementType, ((ArrayType)t).getElementType());
	return new ArrayType(elementLUB);
    }

    /** Return the string representation of this type. The format is
     *  (<type>) array, where <type> is is the elemenet type.
     *  @return A String.
     */
    public String toString() {
	return "(" + _elementType.toString() + ") array";
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // the type of array elements.
    private Type _declaredElementType;
    private Type _elementType;

    private static ArrayType _representative = new ArrayType(BaseType.NAT);

    ///////////////////////////////////////////////////////////////////
    ////                           inner class                     ////

    private class ElementTypeTerm implements InequalityTerm {

        // Pass the ArrayType reference in the constructor so it can be
	// returned by getAssociatedObject().
	private ElementTypeTerm(ArrayType t) {
	    _arrayType = t;
	}

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this ArrayType.
         *  @return an ArrayType.
         */
    	public Object getAssociatedObject() {
	    return _arrayType;
	}

        /** Return the element type.
         *  @return a Type.
         */
        public Object getValue() {
	    return _elementType;
	}

        /** Return this ElementTypeTerm in an array if this term
	 *  represents a type variable. This term represents a type
	 *  variable if NaT is passed to the constructor of ArrayType. 
	 *  If a type other than NaT is passed to the constructor,
	 *  return an array of size zero.
	 *  @return An array of InequalityTerm.
	 */
    	public InequalityTerm[] getVariables() {
	    if (_declaredElementType == BaseType.NAT) {
		InequalityTerm[] variable = new InequalityTerm[1];
		variable[0] = this;
		return variable;
	    }
	    return (new InequalityTerm[0]);
	}

        /** Test if the element type can be changed. The element type
	 *  can be changed if it is set to NaT in the ArrayType
	 *  constructor.
	 *  @return True if the element type can be changed; false
	 *   otherwise.
     	 */
    	public boolean isSettable() {
	    if (_declaredElementType instanceof BaseType) {
	    	return _declaredElementType == BaseType.NAT;
	    }

	    return ((StructuredType)_declaredElementType).contains(NaT);
	}

        /** Check whether the current element type is acceptable.
	 *  The element type is acceptable if it represents an
	 *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        public boolean isValueAcceptable() {
	    return isInstantiable();
	}

    	/** Set the element type if it is settable.
         *  @param e a Type.
         *  @exception IllegalActionException If the element type is
	 *   not settable.
     	 */
//FIXME: should delegate to _declaredElementType.getElementTypeTerm().setValue()
//if _declaredElementType is a structured type.
    	public void setValue(Object e)
             throws IllegalActionException {
	    if (!isSettable()) {
	    	throw new IllegalActionException("ElementTypeTerm.setValue:" +
		" The element type cannot be changed.");
	    }

	    // check for circular type containment
	    if (e instanceof StructuredType) {
		if (((StructuredType)e).isElement(_elementType)) {
		    throw new IllegalActionException(
			"ElementTypeTerm.setValue: Attempt to construct " +
			"circular type structure.");
		}
	    }


	    if (_declaredElementType == BaseType.NAT) {
		_elementType = (Type)e;
	    }

	    ((StructuredType)_declaredElementType).refineType(e);
	}

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////

	private ArrayType _arrayType = null;
    }
}

