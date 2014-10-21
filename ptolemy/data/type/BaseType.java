/* The type of base token classes.

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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import ptolemy.data.ActorToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DateToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.EventToken;
import ptolemy.data.FixToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.PetiteToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.XMLToken;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// BaseType

/**
 The type of base token classes. This class provides a type safe
 enumeration of base types.

 @author Yuhong Xiong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 */
public abstract class BaseType implements Type {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new type which represents the type that results from
     *  adding a token of this type and a token of the given argument
     *  type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type add(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return this, that is, return the reference to this object.
     *  @return A BaseType.
     */
    @Override
    public Object clone() {
        return this;
    }

    /** Convert the specified token to a token having the type
     *  represented by this object.
     *  @param t A token.
     *  @return A token.
     *  @exception IllegalActionException If lossless conversion cannot
     *   be done.
     */
    @Override
    public abstract Token convert(Token t) throws IllegalActionException;

    /** Return a new type which represents the type that results from
     *  dividing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type divide(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Determine if the argument represents the same BaseType as this
     *  object.
     *  @param object Another object.
     *  @return True if the argument represents the same BaseType as
     *   this object; false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // since BaseType is a type safe enumeration, can use == to
        // test equality.
        return this == object;
    }

    /** Return an instance of Type of that corresponds to tokens
     *  of a class with the given name, or null if none exists.
     *  @param className The name of the class.
     *  @return An instance of BaseType.
     */
    public static Type forClassName(String className) {
        return (Type) _classNameToType.get(className);
    }

    /** Return an instance of this class with the specified name,
     *  or null if none exists.
     *  @param name The name of the type.
     *  @return An instance of BaseType.
     */
    public static Type forName(String name) {
        return (Type) _nameToType.get(name);
    }

    /** Return the class for tokens that this basetype represents.
     *  @return The class for tokens that this type represents.
     */
    @Override
    public Class getTokenClass() {
        return _tokenClass;
    }

    /** Return a perfect hash for this type.  This number corresponds
     *  uniquely to a particular type, and is used to improve
     *  performance of certain operations in the TypeLattice class.
     *  All instances of a particular type (e.g. integer array) must
     *  return the same number.  Types that return HASH_INVALID will
     *  not have results in TypeLattice cached.  Note that it is safer
     *  to return HASH_INVALID, than to return a number that is not
     *  unique, or different number for the same type from different
     *  instances.  This base class returns HASH_INVALID.
     *  @return A number greater than or equal to 0, or HASH_INVALID.
     */
    @Override
    public int getTypeHash() {
        return Type.HASH_INVALID;
    }

    /** Return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** Return a new type which represents the type that results from
     *  moduloing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type modulo(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return a new type which represents the type that results from
     *  multiplying a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type multiply(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return true if this type does not correspond to a single token
     *  class.  This occurs if the type is not instantiable, or it
     *  represents either an abstract base class or an interface.
     */
    @Override
    public boolean isAbstract() {
        if (!isInstantiable()) {
            return true;
        }

        int mod = _tokenClass.getModifiers();

        if (Modifier.isAbstract(mod)) {
            return true;
        }

        if (_tokenClass.isInterface()) {
            return true;
        }
        return false;
    }

    /** Test if the argument type is compatible with this type. The method
     *  returns true if this type is UNKNOWN, since any type is a substitution
     *  instance of it. If this type is not UNKNOWN, this method returns true
     *  if the argument type is less than or equal to this type in the type
     *  lattice, and false otherwise.
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    @Override
    public boolean isCompatible(Type type) {
        if (this == UNKNOWN) {
            return true;
        }

        int typeInfo = TypeLattice.compare(this, type);
        return typeInfo == CPO.SAME || typeInfo == CPO.HIGHER;
    }

    /** Test if this Type is UNKNOWN.
     *  @return True if this Type is not UNKNOWN; false otherwise.
     */
    @Override
    public boolean isConstant() {
        return this != UNKNOWN;
    }

    /** Determine if this type corresponds to an instantiable token
     *  classes. A BaseType is instantiable if it does not correspond
     *  to an abstract token class, or an interface, or UNKNOWN.
     *  @return True if this type is instantiable.
     */
    @Override
    public boolean isInstantiable() {
        if (this == UNKNOWN) {
            return false;
        }

        return true;
    }

    /** Return true if the argument is a substitution instance of this type.
     *  @param type A Type.
     *  @return True if this type is UNKNOWN; false otherwise.
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        return this == UNKNOWN || this == type;
    }

    /** Return the type of the multiplicative identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type one() {
        return this;
    }

    /** Return a new type which represents the type that results from
     *  subtracting a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type subtract(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    @Override
    public String toString() {
        return _name;
    }

    /** Return the type of the additive identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type zero() {
        return this;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // NOTE: It may seem strange that these inner classes are built this
    // way instead of as anonymous classes...  As anonymous classes, the
    // fields cannot be appropriately typed, which makes type inference
    // much more complex to find the same information.  This is important
    // to the code generator.

    /** The bottom element of the data type lattice. It represents a
     *  type variable.
     */
    public static class UnknownType extends BaseType {
        private UnknownType() {
            super(Void.TYPE, "unknown");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            // Since any type is a substitution instance of UNKNOWN, just
            // return the argument.
            return t;
        }

        @Override
        public int getTypeHash() {
            return 0;
        }
    }

    /** The bottom element of the data type lattice. */
    public static final UnknownType UNKNOWN = new UnknownType();

    /** The bottom element of the array type lattice. */
    public static class ArrayBottomType extends BaseType {
        private ArrayBottomType() {
            super(Void.TYPE, "arrayBottom");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            // Since any type is a substitution instance of UNKNOWN, just
            // return the argument.
            return t;
        }
    }

    /** The bottom element of the array type lattice. */
    public static final ArrayBottomType ARRAY_BOTTOM = new ArrayBottomType();

    /** The boolean data type. */
    public static class BooleanType extends BaseType {
        private BooleanType() {
            super(BooleanToken.class, "boolean");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return BooleanToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 1;
        }
    }

    /** The boolean data type. */
    public static final BooleanType BOOLEAN = new BooleanType();

    /** The boolean matrix data type. */
    public static final MatrixType.BooleanMatrixType BOOLEAN_MATRIX = new MatrixType.BooleanMatrixType();

    /** The unsigned byte data type. */
    public static class UnsignedByteType extends BaseType {
        private UnsignedByteType() {
            super(UnsignedByteToken.class, "unsignedByte");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return UnsignedByteToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 2;
        }
    }

    /** The unsigned byte data type. */
    public static final UnsignedByteType UNSIGNED_BYTE = new UnsignedByteType();

    /** The complex data type. */
    public static class ComplexType extends BaseType {
        private ComplexType() {
            super(ComplexToken.class, "complex");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return ComplexToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 3;
        }
    }

    /** The complex data type. */
    public static final ComplexType COMPLEX = new ComplexType();

    /** The complex matrix data type. */
    public static final MatrixType.ComplexMatrixType COMPLEX_MATRIX = new MatrixType.ComplexMatrixType();

    /** The float data type. */
    public static class FloatType extends BaseType {
        private FloatType() {
            super(FloatToken.class, "float");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return FloatToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 16;
        }
    }

    /** The float data type. */
    public static final FloatType FLOAT = new FloatType();

    /** The double data type. */
    public static class DoubleType extends BaseType {
        private DoubleType() {
            super(DoubleToken.class, "double");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return DoubleToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 4;
        }
    }

    /** The double data type. */
    public static final DoubleType DOUBLE = new DoubleType();

    /** The double matrix data type. */
    public static final MatrixType.DoubleMatrixType DOUBLE_MATRIX = new MatrixType.DoubleMatrixType();

    /** The fix data type. */
    public static class UnsizedFixType extends BaseType {
        private UnsizedFixType() {
            super(FixToken.class, "fixedpoint");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            if (t instanceof FixToken) {
                return t;
            } else {
                throw new IllegalActionException("Cannot convert token " + t
                        + " to type fixed point.");
            }
        }
    }

    /** An alias for the unsized fix data type, provided for backward
     * compatibility with the previous versions of Ptolemy.
     */
    public static final UnsizedFixType FIX = new UnsizedFixType();

    /** The unsized fix data type. */
    public static final UnsizedFixType UNSIZED_FIX = FIX;

    /** The fix data type, with a precision specified. */
    public static final FixType SIZED_FIX = FixType.BOTTOM;

    /** The fix matrix data type. */
    public static final MatrixType.FixMatrixType FIX_MATRIX = new MatrixType.FixMatrixType();

    /** The short integer data type. */
    public static class ShortType extends BaseType {
        private ShortType() {
            super(ShortToken.class, "short");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return ShortToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 15;
        }
    }

    /** The short integer data type. */
    public static final ShortType SHORT = new ShortType();

    /** The integer data type. */
    public static class IntType extends BaseType {
        private IntType() {
            super(IntToken.class, "int");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return IntToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 5;
        }
    }

    /** The integer data type. */
    public static final IntType INT = new IntType();

    /** The integer matrix data type. */
    public static final MatrixType.IntMatrixType INT_MATRIX = new MatrixType.IntMatrixType();

    /** The long integer data type. */
    public static class LongType extends BaseType {
        private LongType() {
            super(LongToken.class, "long");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return LongToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 6;
        }
    }

    /** The long integer data type. */
    public static final LongType LONG = new LongType();

    /** The long integer matrix data type. */
    public static final MatrixType.LongMatrixType LONG_MATRIX = new MatrixType.LongMatrixType();

    /** The numerical data type. */
    // NOTE: Removed NUMERICAL from the type lattice, EAL 6/22/06.
    /*
     public static class NumericalType extends BaseType {
     private NumericalType() {
     super(Numerical.class, "numerical");
     }

     public Token convert(Token t) throws IllegalActionException {
     throw new IllegalActionException(
     "Cannot convert token "
     + t
     + " to type numerical, because numerical is not a concrete type.");
     }

     public int getTypeHash() {
     return 7;
     }
     }
     */

    /** The numerical data type. */
    // NOTE: Removed NUMERICAL from the type lattice, EAL 6/22/06.
    /*
     public static final NumericalType NUMERICAL = new NumericalType();
     */

    /** The object data type. */
    public static final ObjectType OBJECT = new ObjectType();

    /** The actor data type. */
    public static final Type ACTOR = ActorToken.TYPE;

    /** The XmlToken data type. */
    public static class XmlTokenType extends BaseType {
        private XmlTokenType() {
            super(XMLToken.class, "xmltoken");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return XMLToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 8;
        }
    }

    /** The XmlToken data type. */
    public static final XmlTokenType XMLTOKEN = new XmlTokenType();

    /** The scalar data type: The least upper bound of all the scalar types. */
    public static class ScalarType extends BaseType {
        private ScalarType() {
            super(ScalarToken.class, "scalar");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            if (t instanceof ScalarToken) {
                return t;
            }
            throw new IllegalActionException(
                    Token.notSupportedIncomparableConversionMessage(t, "scalar"));
        }

        @Override
        public int getTypeHash() {
            return 9;
        }

        //         public boolean isInstantiable() {
        //             return true;
        //         }
    }

    /** The scalar data type: The least upper bound of all the scalar types. */
    public static final ScalarType SCALAR = new ScalarType();

    /** The matrix data type: The least upper bound of all the matrix types. */
    public static final MatrixType MATRIX = new MatrixType(MatrixToken.class,
            SCALAR, "matrix");

    /** The string data type. */
    public static class StringType extends BaseType {
        private StringType() {
            super(StringToken.class, "string");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return StringToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 10;
        }
    }

    /** The string data type. */
    public static final StringType STRING = new StringType();

    /** The general data type: The top of the lattice.  */
    public static class GeneralType extends BaseType {
        private GeneralType() {
            super(Token.class, "general");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return t;
        }

        @Override
        public int getTypeHash() {
            return 11;
        }
    }

    /** The general data type: The top of the lattice.  */
    public static final GeneralType GENERAL = new GeneralType();

    /** The event data type. */
    public static class EventType extends BaseType {
        private EventType() {
            super(EventToken.class, "event");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return t;
        }

        @Override
        public int getTypeHash() {
            return 12;
        }
    }

    /** The event data type. */
    public static final EventType EVENT = new EventType();

    /** The petite data type. */
    public static class PetiteType extends BaseType {
        private PetiteType() {
            super(PetiteToken.class, "petite");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return PetiteToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 13;
        }
    }

    /** The petite data type. */
    public static final PetiteType PETITE = new PetiteType();

    /** The nil data type. */
    public static class NilType extends BaseType {
        private NilType() {
            super(Token.class, "niltype");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return t;
        }

        @Override
        public int getTypeHash() {
            return 14;
        }
    }

    /** The nil data type. */
    public static final NilType NIL = new NilType();

    /** The date data type. */
    public static class DateType extends BaseType {
        private DateType() {
            super(DateToken.class, "date");
        }

        @Override
        public Token convert(Token t) throws IllegalActionException {
            return DateToken.convert(t);
        }

        @Override
        public int getTypeHash() {
            return 16;
        }
    }

    /** The DateToken data type. */
    public static final DateType DATE = new DateType();

    ///////////////////////////////////////////////////////////////////
    ////                    package private method                 ////

    /** Add entries in this class to index the given name and class to
     *  the given type.
     */
    static void _addType(Type type, String name, Class theClass) {
        // Because the private variables are below the public variables
        // that call this initializer,
        // it doesn't work to initialize this statically.
        if (_nameToType == null) {
            _nameToType = new HashMap();
        }

        if (_classNameToType == null) {
            _classNameToType = new HashMap();
        }

        _nameToType.put(name, type);
        _classNameToType.put(theClass.getName(), type);
    }

    /** Setting the type of something to RECORD allows it to take
     *  on a value that is any record with any fields. This is because
     *  a lossless conversion any such record to an empty record just
     *  returns the original record.  So to force something to have a
     *  record type without specifying what fields it should have, do
     *  <pre>
     *    something.setTypeEquals(BaseType.RECORD);
     *  </pre>
     *  To allow the type to resolve to a specific record type (with
     *  particular fields), do instead
     *  <pre>
     *    something.setTypeAtMost(BaseType.RECORD);
     *  </pre>
     *  This will work for example to require a parameter to have a record
     *  value, but to allow its type to resolve to the specific record
     *  specified.
     */
    static public final RecordType RECORD = RecordType.EMPTY_RECORD;

    ///////////////////////////////////////////////////////////////////
    ////                      private constructor                  ////

    /** The constructor is private to make a type safe enumeration. */
    private BaseType(Class c, String name) {
        _tokenClass = c;
        _name = name;
        _addType(this, name, c);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The class of tokens with this type. */
    private Class _tokenClass;

    /** The name of the type. */
    private String _name;

    /** A map from type name to the type for all base types. */
    private static Map _nameToType;

    /** A map from class name to the type for all base types. */
    private static Map _classNameToType;
}
