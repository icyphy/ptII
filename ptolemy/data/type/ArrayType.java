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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.graph.*;
import ptolemy.data.Token;
import ptolemy.data.MatrixToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.expr.Variable;

//////////////////////////////////////////////////////////////////////////
//// ArrayType
/**
A class representing the type of an array.

@author Steve Neuendorffer, Yuhong Xiong
$Id$
*/

public class ArrayType extends StructuredType {

    /** Construct a new ArrayType with the specified type for the array
     *  elements. To leave the element type undeclared, use BaseType.NAT.
     *  @exception IllegalArgumentException If the argument is null.
     */
    public ArrayType(Type elementType) {
	if (elementType == null) {
	    throw new IllegalArgumentException("ArrayType: elementType is"
			+ " null");
	}

	_setElementType(elementType);
	_declaredElementType = elementType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep copy of this ArrayType if it is a variable, or
     *  itself if it is a constant. The returned copy does
     *  not have the user set.
     *  @return An ArrayType.
     */
    public Object clone() {
	ArrayType newObj = new ArrayType(_declaredElementType);
	if ( !isConstant()) {
	    try {
	        newObj.updateType(this);
	    } catch (IllegalActionException ex) {
		throw new InternalErrorException("ArrayType.clone: Cannot " +
			"update new instance. " + ex.getMessage());
	    }
	}

	return newObj;
    }

    /** Convert the argument token into an ArrayToken having this type,
     *  if losslessly conversion can be done.
     *  The argument can be an ArrayToken or a MatrixToken. If the argument
     *  is a MatrixToken, it will be converted to an ArrayToken containing
     *  a one dimensional token array if the MatrixToken has only one row,
     *  or it will be converted to an ArrayToken containing another ArrayToken
     *  (an array of arrays) if the MatrixToken has more than one row.
     *  If this type is a variable, convert the the argument into a
     *  substitution instance of this variable.
     *  @param t A token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token t)
	    throws IllegalActionException {
	if ( !isCompatible(t)) {
	    throw new IllegalArgumentException("ArrayType.convert: " +
		"Cannot convert the argument token to this type.");
	}

	ArrayToken argArrTok;
	if (t instanceof MatrixToken) {
	    argArrTok = fromMatrixToken((MatrixToken)t);
	} else {
	    argArrTok = (ArrayToken)t;
	}

	if (isConstant()) {
	    if (isEqualTo(argArrTok.getType())) {
		return argArrTok;
	    } else {
		Token[] argArray = argArrTok.arrayValue();
		Token[] result = new Token[argArray.length];
		for (int i = 0; i < argArray.length; i++) {
	    	    result[i] = _elementType.convert(argArray[i]);
		} 
		return new ArrayToken(result);
	    }
	}

	// This type is a variable. argArrTok must be a substitution instance
	// since it is compatible. But do a sanity check.
	if (isSubstitutionInstance(argArrTok.getType())) {
	    return argArrTok;
	} else {
	    throw new InternalErrorException("ArrayType.convert: Argument " +
		"is not a substitution instance but is compatible.");
	}
    }

    /** Disallow type resolution to change this type.
     */
    public void fixType() {
	getElementTypeTerm().fixValue();
    }

    /** Convert the argument MatrixToken to an ArrayToken. If the argument
     *  has more than one row, convert it to an ArrayToken containing an
     *  ArrayToken (an array of array).
     *  @param tok A MatrixToken.
     *  @return An ArrayToken.
     */
    public static ArrayToken fromMatrixToken(MatrixToken tok) {
	int rows = tok.getRowCount();
	if (rows == 1) {
	    return _fromMatrixToken(tok, 0);
	} else {
	    Token[] tokArray = new Token[rows];
	    for (int i=0; i<rows; i++) {
		tokArray[i] = _fromMatrixToken(tok, i);
	    }
	    return new ArrayToken(tokArray);
	}
    }

    /** Return the type of the array elements. This methods always
     *  returns the argument passed into the constructor.
     *  @return a Type.
     */
    public Type getElementType() {
	return _elementType;
    }

    /** Return the InequalityTerm representing the element type.
     *  @return An InequalityTerm.
     *  @see ptolemy.graph.InequalityTerm
     */
    public InequalityTerm getElementTypeTerm() {
	if (_elemTypeTerm == null) {
	    _elemTypeTerm = new ElementTypeTerm(this);
	}
	return _elemTypeTerm;
    }

    /** Return the user of this StructuredType. If the user is not set,
     *  return null.
     *  @return An Object.
     */
    public Object getUser() {
	return _user;
    }

    /** Test if the argument token is compatible with this type.
     *  If this type is a constant, the argument is compatible if it can be
     *  converted losslessly to a token of this type; If this type is a
     *  variable, the argument is compatible if its type is a substitution
     *  instance of this type, or if it can be converted losslessly to a
     *  substitution instance of this type. For ArrayTypes, in addition to
     *  the lossless conversion relation defined by the type lattice,
     *  MatrixTokens can also be losslessly converted to ArrayTokens of the
     *  same type.
     *  @param t A Token.
     *  @return True if the argument is compatible with this type.
     *  @see ptolemy.data.type.ArrayType#convert
     */
    public boolean isCompatible(Token t) {
	Type argType = t.getType();
	if (t instanceof MatrixToken) {
	    MatrixToken argCast = (MatrixToken)t;
	    if (argCast.getRowCount() == 1) {
		// argument is 1-D
		argType = new ArrayType(
				argCast.getElementAsToken(0, 0).getType());
	    } else {
		// argument is 2-D
		argType = new ArrayType(new ArrayType(
				argCast.getElementAsToken(0, 0).getType()));
	    }
	}

	if (isConstant()) {
	    int typeInfo = TypeLattice.compare(this, argType);
	    if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
		return true;
	    }
	} else {
	    // This type is a variable.
	    if (isSubstitutionInstance(argType)) {
		return true;
	    }
	}
	return false;
    }

    /** Test if this ArrayType is a constant. An ArrayType is a constant if
     *  it does not contain BaseType.NAT in any level.
     *  @return True if this type is a constant.
     */
    public boolean isConstant() {
	return _declaredElementType.isConstant();
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

    /** Return true if the specified type is a substitution instance of this
     *  type. For the argument to be a substitution instance, this type must
     *  be a variable, and the argument must be a type that can be obtained
     *  by replacing the BaseType.NAT component of the declared type by
     *  another type.
     *  @parameter type A Type.
     *  @return True is the argument is a substitution instance of this type.
     */
    public boolean isSubstitutionInstance(Type type) {
	if (isConstant() || ( !(type instanceof ArrayType))) {
	    return false;
	}

	Type argElemType = ((ArrayType)type).getElementType();
	return _declaredElementType.isSubstitutionInstance(argElemType);
    }

    /** Notify this type that its user, which is a Variable, has changed its
     *  expression so this type may be changed.
     *  @param user A Variable.
     */
    public void needEvaluate(Variable user) {
	if (_elementType instanceof StructuredType) {
	    ((StructuredType)_elementType).needEvaluate(user);
	}
	_userVariable = user;
    }

    /** Set the user of this ArrayType. The user can only be set once,
     *  otherwise an exception will be thrown.
     *  @param Object The user.
     *  @exception IllegalActionException If the user is already set, or
     *   if the argument is null.
     */
    public void setUser(Object user)
	    throws IllegalActionException {
	if (_user != null) {
	    throw new IllegalActionException("ArrayType._setUser: " +
		"The user is already set.");
	}

	if (user == null) {
	    throw new IllegalActionException("ArrayType._setUser" +
		"The specified user is null.");
	}

	_user = user;
    }

    /** Return the string representation of this type. The format is
     *  (<type>) array, where <type> is is the elemenet type.
     *  @return A String.
     */
    public String toString() {
	return "(" + _elementType.toString() + ")array";
    }

    /** Reset the element type to the value it was first constructed.
     *  This method is called at the beginning of type resolution.
     */
     //  @exception IllegalActionException If this type is a constant.
    public void reset() {
	    // throws IllegalActionException {
	if (isConstant()) {
	    // throw new IllegalActionException("ArrayType.reset: " +
	    //	"Cannot reset a constant type.");
	    return;
	}

	if (_declaredElementType == BaseType.NAT) {
	    _elementType = BaseType.NAT;
	} else {
	    // element type is a structured type.
	    ((StructuredType)_elementType).reset();
	}
    }

    /** Allow type resolution to change this type, if this type is a variable.
     */
    public void unfixType() {
	getElementTypeTerm().unfixValue();
    }

    /** Update this Type to the specified ArrayType.
     *  The specified type must be a substitution instance of this type.
     *  This method will only update the component whose declared type is
     *  BaseType.NAT, and leave the constant part of this type intact.
     *  This method does not check for circular usage, the caller should
     *  perform this check.
     *  @param st A StructuredType.
     *  @exception IllegalActionException If the specified type is not a
     *   substitution instance of this type.
     */
    public void updateType(StructuredType newType)
	    throws IllegalActionException {
	if ( !this.isSubstitutionInstance(newType)) {
	    throw new IllegalActionException("ArrayType.updateType: " +
		"The argument is not a substitution instance of this type.");
	}

	Type newElemType = ((ArrayType)newType).getElementType();
	if (_declaredElementType == BaseType.NAT) {
	    _setElementType(newElemType);
	} else {
	    // _declaredElementType is a StructuredType. _elementType
	    // must also be.
	    ((StructuredType)_elementType).updateType(
						(StructuredType)newElemType);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

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
    protected int _compare(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.compare: " +
		"The argument is not an ArrayType.");
	}

	return TypeLattice.compare(_elementType,
				   ((ArrayType)t).getElementType());
    }

    /** Determine if the specified StructuredType is this object, or
     *  a user of this type, or a user of a higher level.
     *  @return True if the above condition is true.
     */
    protected boolean _deepIsUser(Object st) {
	if (st == this) {
	    return true;
	}

	if (_user != null && (_user instanceof StructuredType)) {
	    return ((StructuredType)_user)._deepIsUser(st);
	}

	return false;
    }

    /** Return a static instance of ArrayType.
     *  @return an ArrayType.
     */
    protected StructuredType _getRepresentative() {
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
    protected StructuredType _greatestLowerBound(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.greatestLowerBound: "
		+ "The argument is not an ArrayType.");
	}

	Type elementGLB = (Type)TypeLattice.lattice().greatestLowerBound(
			    _elementType, ((ArrayType)t).getElementType());
	return new ArrayType(elementGLB);
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected StructuredType _leastUpperBound(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.leastUpperBound: "
		+ "The argument is not an ArrayType.");
	}

	Type elementLUB = (Type)TypeLattice.lattice().leastUpperBound(
			    _elementType, ((ArrayType)t).getElementType());
	return new ArrayType(elementLUB);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    // convert a row of a MatrixToken into an ArrayToken.
    private static ArrayToken _fromMatrixToken(MatrixToken tok, int row) {
	int cols = tok.getColumnCount();
	Token[] tokArray = new Token[cols];
	for (int i=0; i<cols; i++) {
	    tokArray[i] = tok.getElementAsToken(row, i);
	}
	return new ArrayToken(tokArray);
    }

    // Set the elementType. Clone and set the user of the specified
    // element type if necessary.
    private void _setElementType(Type elementType) {
	if (elementType instanceof BaseType) {
            _elementType = elementType;
	} else {
	    // elementType is a StructuredType
	    StructuredType elemTypeStruct = (StructuredType)elementType;

	    if (elemTypeStruct.isConstant()) {
                _elementType = elementType;
	    } else {
	        // elementType is a non-constant StructuredType
		try {
	            if (elemTypeStruct.getUser() == null) {
		        elemTypeStruct.setUser(this);
                        _elementType = elementType;
		    } else {
		        // user already set, clone elementType
		        StructuredType newElemType =
				(StructuredType)elemTypeStruct.clone();
		        newElemType.setUser(this);
		        _elementType = newElemType;
		    }
		} catch (IllegalActionException ex) {
		    // since the user was null, this should never happen.
		    throw new InternalErrorException(
			"ArrayToken._setElementType: " +
			" Cannot set user on the elementType. " +
			ex.getMessage());
		}
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // the type of array elements.
    private Type _declaredElementType;
    private Type _elementType;

    private Object _user = null;

    private ElementTypeTerm _elemTypeTerm = null;

    private static ArrayType _representative = new ArrayType(BaseType.NAT);

    private Variable _userVariable = null;

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

	/** Disallow the value of this term to be changed.
	 */
	public void fixValue() {
	    _valueFixed = true;
	    Object value = getValue();
	    if (value instanceof StructuredType) {
		((StructuredType)value).fixType();
	    }
	}

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
	    if (_userVariable != null) {
		// evaluate the containing Variable by calling getType().
		try {
		    _userVariable.getType();
		} catch (IllegalActionException ex) {
		    throw new InternalErrorException(
			"ArrayType$ElementTypeTerm.getValue: Cannot " +
			"evaluate the containing Variable.");
		}
		_userVariable = null;
	    }
	    return _elementType;
	}

        /** Return this ElementTypeTerm in an array if this term
	 *  represents a type variable. Otherwise, return an array of
	 *  size zero.
	 *  @return An array of InequalityTerm.
	 */
    	public InequalityTerm[] getVariables() {
	    if (isSettable()) {
		InequalityTerm[] variable = new InequalityTerm[1];
		variable[0] = this;
		return variable;
	    }
	    return (new InequalityTerm[0]);
	}

        /** Reset the variable part of the element type to the specified
	 *  type.
	 *  @parameter e A Type.
         *  @exception IllegalActionException If this type is not settable,
	 *   or the argument is not a Type.
         */
        public void initialize(Object e)
	    throws IllegalActionException {
	    if ( !isSettable()) {
		throw new IllegalActionException("ArrayType$ElementTypeTerm." +
		    "initialize: The type is not settable.");
	    }

	    if ( !(e instanceof Type)) {
		throw new IllegalActionException("ElementTypeTerm.initialize: "
		    + "The argument is not a Type.");
	    }

	    reset();
	}

        /** Test if the element type is a type variable.
	 *  @return True if the element type is a type variable.
     	 */
    	public boolean isSettable() {
	    if(isConstant() || _valueFixed) {
		return false;
	    }
	    return true;
	}

        /** Check whether the current element type is acceptable.
	 *  The element type is acceptable if it represents an
	 *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        public boolean isValueAcceptable() {
	    return isInstantiable();
	}

    	/** Set the element type to the specified type.
         *  @param e a Type.
         *  @exception IllegalActionException If setting the element type to
	 *   to the specified one would result in circular type structure;
	 *   or the specified type is not a substitution instance of the
	 *   element type.
     	 */
    	public void setValue(Object e)
             throws IllegalActionException {
	    if ( !isSettable()) {
		throw new IllegalActionException(
		    "ArrayType$ElementTypeTerm.setValue: The type is not " +
		    "settable.");
	    }

	    // check for circular type containment
	    if (e instanceof StructuredType) {
		if (_arrayType._deepIsUser(e)) {
		    throw new IllegalActionException(
			"ElementTypeTerm.setValue: Attempt to construct " +
			"circular type structure.");
		}
	    }

	    if ( !_declaredElementType.isSubstitutionInstance((Type)e)) {
	        // The LUB of the _elementType and another type is General,
		// this is a type conflict.

		// FIXME Should throw TypeConflictException
		// LinkedList conflict = new LinkedList();
		// conflict.add(_arrayType);
		// throw new TypeConflictException(conflict.elements(),
		//    "Type conflict occurs when updating array element "
		//    + "type. Old type: " + _elementType.toString() +
		//    + "; New type: " + e.toString());

	    	throw new IllegalActionException("ElementTypeTerm.setValue:" +
		    " The new type is not a substitution instance of the " +
		    "element type. element type: " +
		    _declaredElementType.toString() + "new type: " +
		    e.toString());
	    }

	    if (_declaredElementType == BaseType.NAT) {
		_elementType = (Type)e;
	    } else {
	        ((StructuredType)_elementType).updateType((StructuredType)e);
	    }
	}

	/** Return a string representation of this term.
	 *  @return A String.
	 */
	public String toString() {
	    return "(ArrayElementType, " + getValue() + ")";
	}

	/** Allow the type of this term to be changed, if this term is a
	 *  variable.
	 */
	public void unfixValue() {
	    _valueFixed = false;
	    Object value = getValue();
	    if (value instanceof StructuredType) {
		((StructuredType)value).unfixType();
	    }
	}

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////

	private ArrayType _arrayType = null;
	private boolean _valueFixed = false;
    }
}

