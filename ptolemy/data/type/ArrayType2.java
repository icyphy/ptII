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
A class representing the type of a multidimensional array.   The type
of an array is represented as a composite type consisting of a Type object and
a DimensionType.

<<<<<<< ArrayType.java
@author Steve Neuendorffer, Yuhong Xiong
$Id$
=======
@author Steve Neuendorffer
$Id$
>>>>>>> 1.5

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

<<<<<<< ArrayType.java
    /** Return a static instance of ArrayType.
     *  @return an ArrayType.
=======
public class ArrayType extends CompositeType 
{
    /** 
     * Create a new array type constant with the given characteristics.
     * @param type The type of data contained in the array.
     * @param dimension The size of the array.
     */
    public ArrayType(Type type, DimensionType dimension) {
        _dimension = dimension;
 	_type = type;
   }

   /** Return true if the given type is equal to this type.   In other words, 
     *  an object with this type can be expressed as an object of Type t with 
     *  no conversion.
     */
    public boolean equals(Object type) {
        if(!(type instanceof ArrayType)) return false;
        ArrayType dtype = (ArrayType) type;
        
        if(!_dimension.equals(dtype._dimension)) return false;
        return _type.equals(dtype._type);
    }

    /** Given a constraint on this Type, return an enumeration of constraints
     *  on other types, on which this type depends.
     *  If the constraint is not on array types, then throw an exception.
     */
    public Enumeration expandConstraint(Inequality constraint) {
        LinkedList list = new LinkedList();
        
        // Add a constraint on the types contained by the array type         
        ArrayType lesser = (ArrayType) constraint.getLesserTerm();
        ArrayType greater = (ArrayType) constraint.getGreaterTerm();
        Inequality newConstraint;
        // Create a constraint on the Type
        newConstraint = new Inequality(lesser._type, 
                greater._type);
        list.appendElements(lesser._type.expandConstraint(newConstraint));
        // Create a constriant on the Dimension
        newConstraint = new Inequality(lesser._dimension, 
                greater._dimension);
        list.appendElements(lesser._dimension.expandConstraint(newConstraint));
        return list.elements();
    }

    /** Return the dimension type of this
     *  array.
     */
    public DimensionType getDimension() {
        return _dimension;
    }

    /** Return the type that is associated with all the elements of this
     *  array.
     */
    public Type getType() {
        return _type;
    }

    /** Check whether the current type of this term is acceptable,
     *  and return true if it is.  Normally, a type is acceptable
     *  if it represents an instantiable object.
     *  @return True if the current type is acceptable.
>>>>>>> 1.5
     */
<<<<<<< ArrayType.java
    public StructuredType getRepresentative() {
	return _representative;
=======
    public boolean isValueAcceptable() {
        return _type.isValueAcceptable() && _dimension.isValueAcceptable();
>>>>>>> 1.5
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
<<<<<<< ArrayType.java
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
=======
    public String toString() {
        String s = new String("ArrayType(");
        s += _type.toString() + "," +
            _dimension.toString();
        s += ")";
        return s;
>>>>>>> 1.5
    }

<<<<<<< ArrayType.java
    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
=======
    /** The number of dimensions of an object of this type.
     *  1 = 1D array, 2 = 2D array, etc.
>>>>>>> 1.5
     */
<<<<<<< ArrayType.java
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
=======
    private DimensionType _dimension;
   
    /** The type of the objects contained in this array.
>>>>>>> 1.5
     */
<<<<<<< ArrayType.java
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
		
	    if (_declaredElementType == BaseType.NAT) {
		_elementType = (Type)e;
	    }

	    ((StructuredType)_declaredElementType).refineType(e);
	}
=======
    private Type _type;
}
>>>>>>> 1.5

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////

	private ArrayType _arrayType = null;
    }
}

