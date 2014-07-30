/** A class representing the type of an ArrayToken.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

 */
package ptolemy.data.type;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**

 A class representing the type of an ArrayToken.

 @author Steve Neuendorffer, Yuhong Xiong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayType extends StructuredType implements Cloneable {
    /** Construct a new ArrayType with the specified type for the array
     *  elements. To leave the element type undeclared, use BaseType.UNKNOWN.
     *  @param elementType The type of the array elements.
     *  @exception IllegalArgumentException If the argument is null.
     */
    public ArrayType(Type elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("Cannot create ArrayType "
                    + " with null elementType");
        }

        try {
            _declaredElementType = (Type) elementType.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new InternalErrorException("The specified type "
                    + elementType + " cannot be cloned.");
        }

        _elementType = _declaredElementType;
        _length = -1;
    }

    /** Construct a new ArrayType with the specified type for the array
     *  elements. To leave the element type undeclared, use BaseType.UNKNOWN.
     *  @param elementType The type of the array elements.
     *  @param length Then length of the array.
     *  @exception IllegalArgumentException If the argument is null.
     */
    public ArrayType(Type elementType, int length) {
        this(elementType);
        if (length < 0) {
            throw new IllegalArgumentException("Cannot create ArrayType "
                    + "with negative length.");
        }
        _length = length;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a type constraint that can be used to constrain
     *  another typeable object to have a type related to an
     *  array whose element type is the type of the specified
     *  typeable.  A typical usage of this is as follows:
     *  <pre>
     *      output.setTypeAtLeast(ArrayType.arrayOf(input));
     *  </pre>
     *  where input and output are ports (this is the type
     *  constraint of SequenceToArray, for example).
     *  @param typeable A typeable.
     *  @return An InequalityTerm that can be passed to methods
     *   like setTypeAtLeast() of the Typeable interface.
     *  @exception IllegalActionException If the specified typeable
     *   cannot be set to an array type.
     */
    public static InequalityTerm arrayOf(Typeable typeable)
            throws IllegalActionException {
        return new TypeableArrayTypeTerm(typeable);
    }

    /** Return a type constraint that can be used to constrain
     *  another typeable object to have a type related to an
     *  array whose element type is the type of the specified
     *  typeable.  A typical usage of this is as follows:
     *  <pre>
     *      output.setTypeAtLeast(ArrayType.arrayOf(input, length));
     *  </pre>
     *  where input and output are ports (this is the type
     *  constraint of SequenceToArray, for example).
     *  @param typeable A typeable.
     *  @param length The length of array.
     *  @return An InequalityTerm that can be passed to methods
     *   like setTypeAtLeast() of the Typeable interface.
     *  @exception IllegalActionException If the specified typeable
     *   cannot be set to an array type.
     */
    public static InequalityTerm arrayOf(Typeable typeable, int length)
            throws IllegalActionException {
        return new TypeableSizedArrayTypeTerm(typeable, length);
    }

    /** Return a deep copy of this ArrayType if it is a variable, or
     *  itself if it is a constant.
     *  @return An ArrayType.
     */
    @Override
    public Object clone() {
        ArrayType newObj = new ArrayType(_declaredElementType);
        newObj._length = _length;

        try {
            newObj.updateType(this);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("ArrayType.clone: "
                    + "Cannot update new instance. " + ex.getMessage());
        }
        return newObj;
    }

    /** Convert the argument token into an ArrayToken having this
     *  type, if lossless conversion can be done.  If the argument
     *  is not an ArrayToken, then the result is an array token with
     *  one entry, the argument.
     *  @param token A token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    @Override
    public Token convert(Token token) throws IllegalActionException {
        Type myElementType = getElementType();
        // Cannot convert to unknown element type.
        if (myElementType.equals(BaseType.UNKNOWN)) {
            if (token instanceof ArrayToken) {
                // Following the logic implemented in BaseType for UNKNOWN,
                // since every array token is a substitution instance for
                // {unknown}, just return the token.
                return token;
            }
            // If it's not an ArrayToken, then something is wrong.
            throw new IllegalActionException("Cannot convert " + token
                    + " to type {unknown}");
        }
        if (!(token instanceof ArrayToken)) {
            if (hasKnownLength() && length() != 1) {
                throw new IllegalActionException(null,
                        Token.notSupportedConversionMessage(token, toString()));
            }
            // NOTE: Added 7/17/06 by EAL to support type -> {type} conversion.
            Token[] contents = new Token[1];
            contents[0] = token;
            return new ArrayToken(myElementType, contents);
        }

        ArrayToken argumentArrayToken = (ArrayToken) token;
        if (hasKnownLength() && argumentArrayToken.length() != length()) {
            throw new IllegalActionException(null,
                    Token.notSupportedConversionMessage(token, toString()));
        }

        if (myElementType.equals(argumentArrayToken.getElementType())) {
            return token;
        }

        Token[] argumentArray = argumentArrayToken.arrayValue();
        Token[] resultArray = new Token[argumentArray.length];

        try {
            for (int i = 0; i < argumentArray.length; i++) {
                resultArray[i] = myElementType.convert(argumentArray[i]);
            }
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(null, ex,
                    Token.notSupportedConversionMessage(token, "int"));
        }

        if (resultArray.length < 1) {
            // Support your local zero length array.
            // actor/lib/test/auto/NilTokenTypeTest.xml requires this.
            Type argumentArrayElementType = argumentArrayToken.getElementType();
            try {
                return new ArrayToken(argumentArrayElementType);
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex,
                        "Failed to construct an array of type "
                                + argumentArrayElementType);
            }
        }
        return new ArrayToken(myElementType, resultArray);
    }

    /** Return the depth of an array type. The depth of an
     *  array type is the number of times it
     *  contains other structured types. For example, an array
     *  of arrays has depth 2, and an array of arrays of records
     *  has depth 3.
     *  @return the depth of a structured type.
     */
    @Override
    public int depth() {
        int depth = 1;
        if (_elementType instanceof StructuredType) {
            depth += ((StructuredType) _elementType).depth();
        }
        return depth;
    }

    /** Determine if the argument represents the same ArrayType as this
     *  object.
     *  @param object Another object.
     *  @return True if the argument represents the same ArrayType as
     *   this object; false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ArrayType)) {
            return false;
        }

        ArrayType argumentType = (ArrayType) object;
        return _elementType.equals(argumentType.getElementType())
                && _length == argumentType._length;
    }

    /** Return a type constraint that can be used to constrain
     *  another typeable object to have a type related to the
     *  element type of the specified typeable.  As a side
     *  effect, the specified typeable is constrained to have an array
     *  type.  A typical usage of this is as follows:
     *  <pre>
     *      output.setTypeAtLeast(ArrayType.elementType(input));
     *  </pre>
     *  where input and output are ports. This forces the input
     *  port to have an array type and the output port to have
     *  a type at least that of the elements of input arrays.
     *  @param typeable An array-valued typeable.
     *  @return An InequalityTerm that can be passed to methods
     *   like setTypeAtLeast() of the Typeable interface.
     *  @exception IllegalActionException If the specified typeable
     *   cannot be set to an array type.
     */
    public static InequalityTerm elementType(Typeable typeable)
            throws IllegalActionException {
        typeable.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        return new TypeableElementTypeTerm(typeable);
    }

    /** Return the declared type of the array elements.
     *  @return a Type.
     */
    public Type getDeclaredElementType() {
        return _declaredElementType;
    }

    /** Return the type of the array elements.
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
        // This should be public because of copernicus.java.TypeSpecializer
        return _elemTypeTerm;
    }

    /** Return the class for tokens that this type represents.
     *  @return The class for tokens that this type represents.
     */
    @Override
    public Class getTokenClass() {
        return ArrayToken.class;
    }

    /** Return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return _elementType.hashCode() + 2917;
    }

    /** Return true if the length of this array type has been determined.
     *  @return true if the length has been determined.
     */
    public boolean hasKnownLength() {
        return _length >= 0;
    }

    /** Return true if this type does not correspond to a single token
     *  class.  This occurs if the type is not instantiable, or it
     *  represents either an abstract base class or an interface.
     *  @return true if the element type is abstract.
     */
    @Override
    public boolean isAbstract() {
        return _elementType.isAbstract() || !hasKnownLength();
    }

    /** Set the elements that have declared type BaseType.UNKNOWN (the leaf
     *  type variable) to the specified type.
     *  @param t the type to set the leaf type variable to.
     */
    @Override
    public void initialize(Type t) {
        try {
            if (!isConstant()) {
                _elemTypeTerm.initialize(t);
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException("ArrayType.initialize: Cannot "
                    + "initialize the element type to " + t + ". "
                    + iae.getMessage());
        }
    }

    /** Test if the argument type is compatible with this type.
     *  If this type is a constant, the argument is compatible if it is less
     *  than or equal to this type in the type lattice; If this type is a
     *  variable, the argument is compatible if it is a substitution
     *  instance of this type.
     *  @param type A Type.
     *  @return True if the argument is compatible with this type.
     *  @see ptolemy.data.type.ArrayType#convert
     */
    @Override
    public boolean isCompatible(Type type) {
        ArrayType arrayType;

        if (type instanceof ArrayType) {
            arrayType = (ArrayType) type;
            if (hasKnownLength() && arrayType.hasKnownLength()
                    && length() != arrayType.length()) {
                return false;
            }
            // If the length of this type is unknown, then the
            // argument length is compatible.
        } else {
            if (type.equals(BaseType.GENERAL)) {
                // If we have a Const {1,2,3} -> Display, then
                // this method needs to return true because Display
                // has an input port of type General.
                return true;
            }
            return false;
        }

        Type elementType = arrayType.getElementType();
        return _elementType.isCompatible(elementType);
    }

    /** Test if this ArrayType is a constant. An ArrayType is a constant if
     *  it does not contain BaseType.UNKNOWN in any level.
     *  @return True if this type is a constant.
     */
    @Override
    public boolean isConstant() {
        return _declaredElementType.isConstant();
    }

    /** Determine if this type corresponds to an instantiable token
     *  class. An ArrayType is instantiable if its element type is
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    @Override
    public boolean isInstantiable() {
        return _elementType.isInstantiable();
    }

    /** Return true if the specified type is a substitution instance of this
     *  type.
     *  @param type A Type.
     *  @return True if the argument is a substitution instance of this type.
     *  @see Type#isSubstitutionInstance
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        ArrayType arrayType;
        if (type instanceof ArrayType) {
            arrayType = (ArrayType) type;
            if (hasKnownLength() && arrayType.hasKnownLength()
                    && length() != arrayType.length()) {
                return false;
            }
        } else {
            return false;
        }

        Type argElemType = ((ArrayType) type).getElementType();
        return _declaredElementType.isSubstitutionInstance(argElemType);
    }

    /** Return the length of this array type.
     *  @return The length of this type.
     *  @exception RuntimeException If the length is not known.
     */
    public int length() {
        if (!hasKnownLength()) {
            throw new RuntimeException("Length is not known.");
        }
        return _length;
    }

    /** Set the type to the specified type, which is required to be
     *  an array type.
     *  @param type The new type.
     *  @exception IllegalActionException If the specified type is not
     *   an instance of ArrayType.
     */
    public void setType(Type type) throws IllegalActionException {
        if (!(type instanceof ArrayType)) {
            throw new IllegalActionException(
                    "Cannot change an array type to a non-array type.");
        }
        try {
            Type clone = (Type) ((ArrayType) type).getElementType().clone();
            _elementType = clone;
            _declaredElementType = clone;
            _length = ((ArrayType) type).length();
        } catch (CloneNotSupportedException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Return the string representation of this type. The format is
     *  {<i>type</i>}, where <i>type</i> is the element type.
     *  @return A String.
     */
    @Override
    public String toString() {
        if (hasKnownLength()) {
            return "arrayType(" + getElementType().toString() + "," + _length
                    + ")";
        } else {
            return "arrayType(" + getElementType().toString() + ")";
        }
    }

    /** Update this Type to the specified ArrayType.
     *  The specified type must be an ArrayType with the same structure as
     *  this type, and have depth less than the MAXDEPTHBOUND.
     *  This method will only update the component whose declared type is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type is not an
     *   ArrayType or it does not have the same structure as this one.
     */
    @Override
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        super.updateType(newType);

        // This type is a variable.
        if (!this.isSubstitutionInstance(newType)) {
            throw new IllegalActionException("ArrayType.updateType: "
                    + "The type " + this + " cannot be updated to " + newType
                    + ".");
        }

        ArrayType arrayType = (ArrayType) newType;
        if (!arrayType.hasKnownLength() || arrayType._length != _length) {
            _length = -1; // Other length cases should be guarded by
            // the isSubstituionInstance method
        }

        Type newElemType = ((ArrayType) newType).getElementType();

        if (_declaredElementType.equals(BaseType.UNKNOWN)) {
            try {
                _elementType = (Type) newElemType.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("ArrayType.updateType: "
                        + "The specified element type cannot be cloned: "
                        + _elementType);
            }
        } else if (!_declaredElementType.equals(newElemType)) {
            // _declaredElementType is a StructuredType. _elementType
            // must also be.
            ((StructuredType) _elementType)
            .updateType((StructuredType) newElemType);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A term to use when declaring the type of some parameter or port
     *  to be an array.  The way to use this is to declare:
     *  <pre>
     *     param.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
     *  </pre>
     *  for a parameter "param".
     */
    public static final InequalityTerm ARRAY_BOTTOM = new ArrayBottomTypeTerm(
            BaseType.ARRAY_BOTTOM);

    /** A term to use when declaring the type of some parameter or port
     *  to be an array, with unknown length.  The way to use this is to declare:
     *  <pre>
     *     param.setTypeAtLeast(ArrayType.ARRAY_UNSIZED_BOTTOM);
     *  </pre>
     *  for a parameter "param".
     */
    public static final InequalityTerm ARRAY_UNSIZED_BOTTOM = new ArrayBottomTypeTerm(
            new ArrayType(BaseType.UNKNOWN));

    //  (new ArrayType(BaseType.UNKNOWN) {
    //         // This particular inequality term always has an acceptable type
    //         // because it has no visible array that will ever be evaluated.
    //         // It is essential that isValueAcceptable() return true, or the
    //         // idiom above will result in reported type errors.
    //         public InequalityTerm getElementTypeTerm() {
    //             return _replacementElementTerm;
    //         }

    //         private InequalityTerm _replacementElementTerm = new ElementTypeTerm() {
    //             public boolean isValueAcceptable() {
    //                 return true;
    //             }
    //         };
    //     }).getElementTypeTerm();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be an ArrayType, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type an ArrayType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    @Override
    protected int _compare(StructuredType type) {
        if (!(type instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.compare: "
                    + "The argument " + type + " is not an ArrayType.");
        }

        int retval = TypeLattice.compare(_elementType,
                ((ArrayType) type).getElementType());

        ArrayType arrayArgType = (ArrayType) type;
        if (hasKnownLength() && arrayArgType.hasKnownLength()) {
            if (length() != arrayArgType.length()) {
                retval = ptolemy.graph.CPO.INCOMPARABLE;
            }
        } else if (hasKnownLength()) {
            if (retval == ptolemy.graph.CPO.HIGHER) {
                retval = ptolemy.graph.CPO.INCOMPARABLE;
            } else if (retval == ptolemy.graph.CPO.SAME) {
                // same element type but arrayArgType has no length [a, n] <= [a]
                retval = ptolemy.graph.CPO.LOWER;
            }
        } else if (arrayArgType.hasKnownLength()) {
            if (retval == ptolemy.graph.CPO.LOWER) {
                retval = ptolemy.graph.CPO.INCOMPARABLE;
            } else if (retval == ptolemy.graph.CPO.SAME) {
                // same element type but this type has no length [a] >= [a, n]
                retval = ptolemy.graph.CPO.HIGHER;
            }
        }

        //System.out.println("comparing " + this + " and " + arrayArgType + " = " + retval);
        return retval;
    }

    /** Return a static instance of ArrayType.
     *  @return an ArrayType.
     */
    @Override
    protected StructuredType _getRepresentative() {
        return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param type an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    @Override
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if (!(type instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.greatestLowerBound: "
                    + "The argument " + type + " is not an ArrayType.");
        }

        Type elementGLB = (Type) TypeLattice.lattice().greatestLowerBound(
                _elementType, ((ArrayType) type).getElementType());

        ArrayType arrayArgType = (ArrayType) type;
        if (!hasKnownLength() && !arrayArgType.hasKnownLength()) {
            return new ArrayType(elementGLB);
        } else if (hasKnownLength() && arrayArgType.hasKnownLength()) {
            if (length() != arrayArgType.length()) {
                // FIXME:
                // return BaseType.ARRAY_BOTTOM;
            }
        }
        if (hasKnownLength()) {
            return new ArrayType(elementGLB, length());
        } else {
            return new ArrayType(elementGLB, arrayArgType.length());
        }
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param type an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    @Override
    protected StructuredType _leastUpperBound(StructuredType type) {
        if (!(type instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.leastUpperBound: "
                    + "The argument " + type + " is not an ArrayType.");
        }

        Type elementLUB = (Type) TypeLattice.lattice().leastUpperBound(
                _elementType, ((ArrayType) type).getElementType());

        ArrayType arrayArgType = (ArrayType) type;
        if (hasKnownLength() && arrayArgType.hasKnownLength()) {
            if (length() == arrayArgType.length()) {
                return new ArrayType(elementLUB, length());
            }
        }

        //   System.out.println("least upper bound of " + this + " and " + type + " = " + new ArrayType(elementLUB));

        return new ArrayType(elementLUB);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // the type of array elements.
    private Type _declaredElementType;

    private Type _elementType;

    private int _length;

    private ElementTypeTerm _elemTypeTerm = new ElementTypeTerm();

    private static ArrayType _representative = new ArrayType(BaseType.UNKNOWN);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An InequalityTerm representing an arbitrary array type.
     */
    private static class ArrayBottomTypeTerm implements InequalityTerm {

        /** Construct a term.
         */
        public ArrayBottomTypeTerm(Type type) {
            _arrayType = type;
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return an array type with element types given by the
         *  associated typeable.
         *  @return An ArrayType.
         */
        @Override
        public Object getAssociatedObject() {
            return _arrayType;
        }

        /** Return an array type with element types given by the
         *  associated typeable.
         *  @return An ArrayType.
         *  @exception IllegalActionException If the type of the
         *  associated typeable cannot be determined.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            return _arrayType;
        }

        /** Return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            return new InequalityTerm[0];
        }

        /** Throw an exception. This term cannot be set.
         *  @param e A Type.
         *  @exception IllegalActionException If this type is a constant,
         *   or the argument is not a Type.
         */
        @Override
        public void initialize(Object e) throws IllegalActionException {
            throw new IllegalActionException(
                    "ArrayType$ArraybottomTypeTerm.setValue: "
                            + "Is not settable.");
        }

        /** Return false.
         *  @return False.
         */
        @Override
        public boolean isSettable() {
            return false;
        }

        /** Delegate to an array type with elements given by the
         *  type of the associated typeable.
         *  @return True if the element type is acceptable.
         */
        @Override
        public boolean isValueAcceptable() {
            return true;
        }

        /** Throw an exception.
         *  @param type a Type.
         *  @exception IllegalActionException Always
         */
        @Override
        public void setValue(Object type) throws IllegalActionException {
            throw new IllegalActionException(
                    "ArrayType$ArrayBottomTypeTerm.setValue: "
                            + "Is not settable.");
        }

        /** Delegate to an array type with elements given by the
         *  type of the associated typeable.
         *  @return A String.
         */
        @Override
        public String toString() {
            return _arrayType.toString();
        }

        ///////////////////////////////////////////////////////////////
        ////                   private members                     ////

        /** The array type with element types matching the typeable. */
        private Type _arrayType;
    }

    /** An InequalityTerm associated with an instance of ArrayType. */
    private class ElementTypeTerm implements InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this ArrayType.
         *  @return an ArrayType.
         */
        @Override
        public Object getAssociatedObject() {
            return ArrayType.this;
        }

        /** Return the element type.
         *  @return a Type.
         */
        @Override
        public Object getValue() {
            return _elementType;
        }

        /** Return this ElementTypeTerm in an array if this term
         *  represents a type variable. Otherwise, return an array of
         *  size zero.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return new InequalityTerm[0];
        }

        /** Reset the variable part of the element type to the specified
         *  type.
         *  @param e A Type.
         *  @exception IllegalActionException If this type is a constant,
         *   or the argument is not a Type.
         */
        @Override
        public void initialize(Object e) throws IllegalActionException {
            if (isConstant()) {
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.initialize: " + "This type "
                                + this + " is not settable.");
            }

            if (!(e instanceof Type)) {
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.initialize: "
                                + "The argument " + this + " is not a Type.");
            }

            if (_declaredElementType.equals(BaseType.UNKNOWN)) {
                _elementType = (Type) e;
            } else {
                // element type is a structured type.
                ((StructuredType) _elementType).initialize((Type) e);
            }
        }

        /** Test if the element type is a type variable.
         *  @return True if the element type is a type variable.
         */
        @Override
        public boolean isSettable() {
            return !_declaredElementType.isConstant();
        }

        /** Check whether the current element type is acceptable.
         *  The element type is acceptable if it represents an
         *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        @Override
        public boolean isValueAcceptable() {
            return _elementType.isInstantiable();
        }

        /** Set the element type to the specified type.
         *  @param e a Type.
         *  @exception IllegalActionException If the specified type violates
         *   the declared type of the element.
         */
        @Override
        public void setValue(Object e) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.setValue: This type " + e
                        + " is not settable.");
            }

            if (!_declaredElementType.isSubstitutionInstance((Type) e)) {
                // The LUB of the _elementType and another type is General,
                // this is a type conflict.
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.setValue: "
                                + "Cannot update the element type of this array to "
                                + "the new type." + " Element type: "
                                + _declaredElementType.toString()
                                + ", New type: " + e.toString());
            }

            if (_declaredElementType.equals(BaseType.UNKNOWN)) {
                try {
                    _elementType = (Type) ((Type) e).clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "ArrayType$ElementTypeTerm.setValue: "
                                    + "The specified type " + e
                                    + " cannot be cloned.");
                }
            } else {
                ((StructuredType) _elementType).updateType((StructuredType) e);
            }
        }

        /** Return a string representation of this term.
         *  @return A String.
         */
        @Override
        public String toString() {
            return "(ArrayElementType(" + getAssociatedObject() + "), "
                    + getValue() + ")";
        }
    }

    /** An InequalityTerm representing an array type whose elements
     *  have the type of the specified typeable.  The purpose of this class
     *  is to defer to as late as possible actually accessing
     *  the type of the typeable, since it may change dynamically.
     *  This term is not variable and cannot be set.
     */
    private static class TypeableArrayTypeTerm implements InequalityTerm {

        /** Construct a term that will defer to the type of the
         *  specified typeable.
         *  @param typeable The object to defer requests to.
         */
        public TypeableArrayTypeTerm(Typeable typeable) {
            _typeable = typeable;
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return the associated typeable.
         *  @return A Typeable.
         */
        @Override
        public Object getAssociatedObject() {
            return _typeable;
        }

        /** Return an array type with element types given by the associated typeable.
         *  @return An ArrayType.
         *  @exception IllegalActionException If the type of the associated typeable
         *   cannot be determined.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            return _getArrayTypeRaw();
        }

        /** Return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            return new InequalityTerm[0];
        }

        /** Throw an exception. This term cannot be set.
         *  @param e A Type.
         *  @exception IllegalActionException If this type is a constant,
         *   or the argument is not a Type.
         */
        @Override
        public void initialize(Object e) throws IllegalActionException {
            throw new IllegalActionException(
                    "ArrayType$TypeableArrayTypeTerm.initialize: "
                            + "This array type given with elements given by "
                            + _typeable + " is not settable.");
        }

        /** Return false.
         *  @return False.
         */
        @Override
        public boolean isSettable() {
            return false;
        }

        /** Delegate to an array type with elements given by the
         *  type of the associated typeable.
         *  @return True if the element type is acceptable.
         */
        @Override
        public boolean isValueAcceptable() {
            ArrayType type = _getArrayType();
            return type.getElementTypeTerm().isValueAcceptable();
        }

        /** Throw an exception.
         *  @param type a Type.
         *  @exception IllegalActionException Always
         */
        @Override
        public void setValue(Object type) throws IllegalActionException {
            throw new IllegalActionException(
                    "ArrayType$TypeableArrayTypeTerm.setValue: "
                            + "The array type with element type given by "
                            + _typeable + " is not settable.");
        }

        /** Delegate to an array type with elements given by the
         *  type of the associated typeable.
         *  @return A String.
         */
        @Override
        public String toString() {
            try {
                return "(TypeableArrayType(" + getAssociatedObject() + "), "
                        + getValue() + ")";
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                   private methods                     ////

        /** Get an array type with element type matching the type
         *  of the associated typeable.
         *  @return An array type for the associated typeable.
         */
        private ArrayType _getArrayType() {
            try {
                return _getArrayTypeRaw();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }

        /** Get an array type with element type matching the type
         *  of the associated typeable.
         *  @return An array type for the associated typeable.
         *  @exception IllegalActionException If the type of the typeable
         *   cannot be determined.
         */
        private ArrayType _getArrayTypeRaw() throws IllegalActionException {
            Type type = _typeable.getType();
            if (_arrayType == null || !_arrayType.getElementType().equals(type)) {
                _arrayType = new ArrayType(type);
            }
            return _arrayType;
        }

        ///////////////////////////////////////////////////////////////
        ////                   private members                     ////

        /** The associated typeable. */
        private Typeable _typeable;

        /** The array type with element types matching the typeable. */
        private ArrayType _arrayType;
    }

    /** An InequalityTerm representing the element types
     *  of an instance of Typeable.  The purpose of this class
     *  is to defer to as late as possible actually accessing
     *  the type of the typeable, since it may change dynamically.
     */
    private static class TypeableElementTypeTerm implements InequalityTerm {

        /** Construct a term that will defer to the type of the
         *  specified typeable.
         *  @param typeable The object to defer requests to.
         */
        public TypeableElementTypeTerm(Typeable typeable) {
            _typeable = typeable;
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Delegate to the element type term of the associated typeable.
         *  @return an ArrayType.
         */
        @Override
        public Object getAssociatedObject() {
            return _typeable;
        }

        /** Delegate to the element type term of the associated typeable.
         *  @return a Type.
         *  @exception IllegalActionException If the delegate throws it.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            InequalityTerm term = _getElementTypeTerm();
            if (term == null) {
                return BaseType.UNKNOWN;
            } else {
                return term.getValue();
            }
        }

        /** Delegate to the element type term of the associated typeable.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return new InequalityTerm[0];
        }

        /** Delegate to the element type term of the associated typeable.
         *  @param type A Type.
         *  @exception IllegalActionException If the delegate throws it.
         */
        @Override
        public void initialize(Object type) throws IllegalActionException {
            InequalityTerm term = _getElementTypeTerm();
            if (term == null) {
                return;
            } else {
                term.initialize(type);
            }
        }

        /** Delegate to the element type term of the associated typeable.
         *  @return True if the element type is a type variable.
         */
        @Override
        public boolean isSettable() {
            InequalityTerm term = _getElementTypeTerm();
            if (term == null) {
                return true;
            } else {
                return term.isSettable();
            }
        }

        /** Delegate to the element type term of the associated typeable.
         *  @return True if the element type is acceptable.
         */
        @Override
        public boolean isValueAcceptable() {
            InequalityTerm term = _getElementTypeTerm();
            if (term == null) {
                // Array has no element type.
                // If the type of the associated typable is
                // unknown and if this is acceptable, then it
                // is OK for the element types to be unknown.
                try {
                    Type arrayType = _typeable.getType();
                    if (BaseType.UNKNOWN.equals(arrayType)
                            && _typeable.isTypeAcceptable()) {
                        return true;
                    }
                } catch (IllegalActionException e) {
                    // Ignore and return false.
                }
                return false;
            } else {
                return term.isValueAcceptable();
            }
        }

        /** Delegate to the element type term of the associated typeable.
         *  @param type a Type.
         *  @exception IllegalActionException If the specified type violates
         *   the declared type of the element.
         */
        @Override
        public void setValue(Object type) throws IllegalActionException {
            InequalityTerm term = _getElementTypeTerm();
            if (term == null) {
                return;
            } else {
                term.setValue(type);
            }
        }

        /** Delegate to the element type term of the associated typeable.
         *  @return A String.
         */
        @Override
        public String toString() {
            try {
                return "(ArrayElementType(" + getAssociatedObject() + "), "
                        + getValue() + ")";
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }

        }

        ///////////////////////////////////////////////////////////////
        ////                   private methods                     ////

        /** Get an inequality term for elements of the associated
         *  typeable. If the associated typeable does not already have
         *  an array type, then return null, indicating that the type
         *  of the typeable hasn't yet resolved to an array type.
         *  @return An array type for the associated typeable.
         */
        private InequalityTerm _getElementTypeTerm() {
            try {
                Type type = _typeable.getType();
                if (!(type instanceof ArrayType)) {
                    return null;
                }
                return ((ArrayType) type).getElementTypeTerm();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                   private members                     ////

        /** The associated typeable. */
        private Typeable _typeable;
    }

    /** An InequalityTerm representing an array types whose elements
     *  have the type of the specified typeable.  The purpose of this class
     *  is to defer to as late as possible actually accessing
     *  the type of the typeable, since it may change dynamically.
     *  This term is not variable and cannot be set.
     */
    private static class TypeableSizedArrayTypeTerm implements InequalityTerm {

        /** Construct a term that will defer to the type of the
         *  specified typeable.
         *  @param typeable The object to defer requests to.
         */
        public TypeableSizedArrayTypeTerm(Typeable typeable, int length) {
            _typeable = typeable;
            _length = length;
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return an array type with element types given by the associated typeable.
         *  @return An ArrayType.
         */
        @Override
        public Object getAssociatedObject() {
            return _getArrayType();
        }

        /** Return an array type with element types given by the associated typeable.
         *  @return An ArrayType.
         *  @exception IllegalActionException If the type of the associated typeable
         *   cannot be determined.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            return _getArrayTypeRaw();
        }

        /** Return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            return new InequalityTerm[0];
        }

        /** Throw an exception. This term cannot be set.
         *  @param e A Type.
         *  @exception IllegalActionException If this type is a constant,
         *   or the argument is not a Type.
         */
        @Override
        public void initialize(Object e) throws IllegalActionException {
            throw new IllegalActionException(
                    "ArrayType$TypeableArrayTypeTerm.initialize: "
                            + "This array type given with elements given by "
                            + _typeable + " is not settable.");
        }

        /** Return false.
         *  @return False.
         */
        @Override
        public boolean isSettable() {
            return false;
        }

        /** Delegate to an array type with elements given by the
         *  type of the associated typeable.
         *  @return True if the element type is acceptable.
         */
        @Override
        public boolean isValueAcceptable() {
            ArrayType type = _getArrayType();
            return type.getElementTypeTerm().isValueAcceptable();
        }

        /** Throw an exception.
         *  @param type a Type.
         *  @exception IllegalActionException Always
         */
        @Override
        public void setValue(Object type) throws IllegalActionException {
            throw new IllegalActionException(
                    "ArrayType$TypeableArrayTypeTerm.setValue: "
                            + "The array type with element type given by "
                            + _typeable + " is not settable.");
        }

        /** Delegate to an array type with elements given by the
         *  type of the associated typeable.
         *  @return A String.
         */
        @Override
        public String toString() {
            return _getArrayType().toString();
        }

        ///////////////////////////////////////////////////////////////
        ////                   private methods                     ////

        /** Get an array type with element type matching the type
         *  of the associated typeable.
         *  @return An array type for the associated typeable.
         */
        private ArrayType _getArrayType() {
            try {
                return _getArrayTypeRaw();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }

        /** Get an array type with element type matching the type
         *  of the associated typeable.
         *  @return An array type for the associated typeable.
         *  @exception IllegalActionException If the type of the typeable
         *   cannot be determined.
         */
        private ArrayType _getArrayTypeRaw() throws IllegalActionException {
            Type type = _typeable.getType();
            if (_arrayType == null || !_arrayType.getElementType().equals(type)) {
                _arrayType = new ArrayType(type, _length);
            }
            return _arrayType;
        }

        ///////////////////////////////////////////////////////////////
        ////                   private members                     ////

        /** The associated typeable. */
        private Typeable _typeable;

        /** The array type with element types matching the typeable. */
        private ArrayType _arrayType;

        private int _length;
    }
}
