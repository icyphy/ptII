/* The type of base token classes.

Copyright (c) 1997-2005 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.EventToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.Numerical;
import ptolemy.data.ObjectToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.XMLToken;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
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
public abstract class BaseType implements Type, Serializable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return A BaseType.
     */
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
    public abstract Token convert(Token t) throws IllegalActionException;

    /** Determine if the argument represents the same BaseType as this
     *  object.
     *  @param object Another object.
     *  @return True if the argument represents the same BaseType as
     *   this object; false otherwise.
     */
    public boolean equals(Object object) {
        // since BaseType is a type safe enumeration, can use == to
        // test equality.
        return this == object;
    }

    /** Return an instance of this class that corresponds to tokens
     *  of a class with the given name, or null if none exists.
     *  @return An instance of BaseType.
     */
    public static Type forClassName(String className) {
        return (Type) _classNameToType.get(className);
    }

    /** Return an instance of this class with the specified name,
     *  or null if none exists.
     *  @return An instance of BaseType.
     */
    public static Type forName(String name) {
        return (Type) _nameToType.get(name);
    }

    /** Return the class for tokens that this basetype represents.
     */
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
     *  @return A number between 0 and HASH_MAX, or HASH_INVALID.
     */
    public int getTypeHash() {
        return Type.HASH_INVALID;
    }

    /** Return a hash code value for this object.
     */
    public int hashCode() {
        return super.hashCode();
    }

    /** Test if the argument type is compatible with this type. The method
     *  returns true if this type is UNKNOWN, since any type is a substitution
     *  instance of it. If this type is not UNKNOWN, this method returns true
     *  if the argument type is less than or equal to this type in the type
     *  lattice, and false otherwise.
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    public boolean isCompatible(Type type) {
        if (this == UNKNOWN) {
            return true;
        }

        int typeInfo = TypeLattice.compare(this, type);
        return ((typeInfo == CPO.SAME) || (typeInfo == CPO.HIGHER));
    }

    /** Test if this Type is UNKNOWN.
     *  @return True if this Type is not UNKNOWN; false otherwise.
     */
    public boolean isConstant() {
        return this != UNKNOWN;
    }

    /** Determine if this type corresponds to an instantiable token
     *  classes. A BaseType is instantiable if it does not correspond
     *  to an abstract token class, or an interface, or UNKNOWN.
     *  @return True if this type is instantiable.
     */
    public boolean isInstantiable() {
        if (this == UNKNOWN) {
            return false;
        }

        int mod = _tokenClass.getModifiers();

        if (Modifier.isAbstract(mod)) {
            return false;
        }

        if (_tokenClass.isInterface()) {
            return false;
        }

        return true;
    }

    /** Return true if the argument is a substitution instance of this type.
     *  @param type A Type.
     *  @return True if this type is UNKNOWN; false otherwise.
     */
    public boolean isSubstitutionInstance(Type type) {
        return (this == UNKNOWN) || (this == type);
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    public String toString() {
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
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

        public Token convert(Token t) throws IllegalActionException {
            // Since any type is a substitution instance of UNKNOWN, just
            // return the argument.
            return t;
        }

        public int getTypeHash() {
            return 0;
        }
    }

    public static final UnknownType UNKNOWN = new UnknownType();

    /** The boolean data type */
    public static class BooleanType extends BaseType {
        private BooleanType() {
            super(BooleanToken.class, "boolean");
        }

        public Token convert(Token t) throws IllegalActionException {
            return BooleanToken.convert(t);
        }

        public int getTypeHash() {
            return 1;
        }
    }

    public static final BooleanType BOOLEAN = new BooleanType();
    public static final UnsizedMatrixType.BooleanMatrixType BOOLEAN_MATRIX = new UnsizedMatrixType.BooleanMatrixType();

    /** The unsigned byte data type */
    public static class UnsignedByteType extends BaseType {
        private UnsignedByteType() {
            super(UnsignedByteToken.class, "unsignedByte");
        }

        public Token convert(Token t) throws IllegalActionException {
            return UnsignedByteToken.convert(t);
        }

        public int getTypeHash() {
            return 2;
        }
    }

    public static final UnsignedByteType UNSIGNED_BYTE = new UnsignedByteType();

    /** The complex data type */
    public static class ComplexType extends BaseType {
        private ComplexType() {
            super(ComplexToken.class, "complex");
        }

        public Token convert(Token t) throws IllegalActionException {
            return ComplexToken.convert(t);
        }

        public int getTypeHash() {
            return 3;
        }
    }

    public static final ComplexType COMPLEX = new ComplexType();
    public static final UnsizedMatrixType.ComplexMatrixType COMPLEX_MATRIX = new UnsizedMatrixType.ComplexMatrixType();

    /** The double data type */
    public static class DoubleType extends BaseType {
        private DoubleType() {
            super(DoubleToken.class, "double");
        }

        public Token convert(Token t) throws IllegalActionException {
            return DoubleToken.convert(t);
        }

        public int getTypeHash() {
            return 4;
        }
    }

    public static final DoubleType DOUBLE = new DoubleType();
    public static final UnsizedMatrixType.DoubleMatrixType DOUBLE_MATRIX = new UnsizedMatrixType.DoubleMatrixType();

    /** The fix data type */

    //   public static class FixType extends BaseType {
    //         private FixType() {
    //             super(FixToken.class, "fixedpoint");
    //         }
    //         public Token convert(Token t) throws IllegalActionException {
    //             return FixToken.convert(t);
    //         }
    //     }
    public static final FixType FIX = FixType.BOTTOM; //new FixType();
    public static final UnsizedMatrixType.FixMatrixType FIX_MATRIX = new UnsizedMatrixType.FixMatrixType();

    /** The integer data type */
    public static class IntType extends BaseType {
        private IntType() {
            super(IntToken.class, "int");
        }

        public Token convert(Token t) throws IllegalActionException {
            return IntToken.convert(t);
        }

        public int getTypeHash() {
            return 5;
        }
    }

    public static final IntType INT = new IntType();
    public static final UnsizedMatrixType.IntMatrixType INT_MATRIX = new UnsizedMatrixType.IntMatrixType();

    /** The long integer data type */
    public static class LongType extends BaseType {
        private LongType() {
            super(LongToken.class, "long");
        }

        public Token convert(Token t) throws IllegalActionException {
            return LongToken.convert(t);
        }

        public int getTypeHash() {
            return 6;
        }
    }

    public static final LongType LONG = new LongType();
    public static final UnsizedMatrixType.LongMatrixType LONG_MATRIX = new UnsizedMatrixType.LongMatrixType();

    /** The matrix data type: The least upper bound of all the matrix types. */
    public static final TopMatrixType MATRIX = TopMatrixType.getInstance();

    /** The numerical data type */
    public static class NumericalType extends BaseType {
        private NumericalType() {
            super(Numerical.class, "numerical");
        }

        public Token convert(Token t) throws IllegalActionException {
            throw new IllegalActionException("Cannot convert token " + t
                    + " to type numerical, because numerical is not a concrete type.");
        }

        public int getTypeHash() {
            return 7;
        }
    }

    public static final NumericalType NUMERICAL = new NumericalType();

    /** The object data type */
    public static class ObjectType extends BaseType {
        private ObjectType() {
            super(ObjectToken.class, "object");
        }

        public Token convert(Token t) throws IllegalActionException {
            return ObjectToken.convert(t);
        }

        public int getTypeHash() {
            return 8;
        }
    }

    public static final ObjectType OBJECT = new ObjectType();

    /** The XmlToken data type */
    public static class XmlTokenType extends BaseType {
        private XmlTokenType() {
            super(XMLToken.class, "xmltoken");
        }

        public Token convert(Token t) throws IllegalActionException {
            return XMLToken.convert(t);
        }

        public int getTypeHash() {
            return 8;
        }
    }

    public static final XmlTokenType XMLTOKEN = new XmlTokenType();

    /** The scalar data type: The least upper bound of all the scalar types. */
    public static class ScalarType extends BaseType {
        private ScalarType() {
            super(ScalarToken.class, "scalar");
        }

        public Token convert(Token t) throws IllegalActionException {
            throw new IllegalActionException("Cannot convert token " + t
                    + " to type scalar, because scalar is not a concrete type.");
        }

        public int getTypeHash() {
            return 9;
        }
    }

    public static final ScalarType SCALAR = new ScalarType();

    /** The string data type */
    public static class StringType extends BaseType {
        private StringType() {
            super(StringToken.class, "string");
        }

        public Token convert(Token t) throws IllegalActionException {
            return StringToken.convert(t);
        }

        public int getTypeHash() {
            return 10;
        }
    }

    public static final StringType STRING = new StringType();

    /** The general data type: The top of the lattice.  */
    public static class GeneralType extends BaseType {
        private GeneralType() {
            super(Token.class, "general");
        }

        public Token convert(Token t) throws IllegalActionException {
            // FIXME: what does converting to general MEAN?
            return t;

            //     throw new IllegalActionException("Cannot convert token "
            //         + t + " to type general, because general is not a "
            //         + concrete type.");
        }

        public int getTypeHash() {
            return 11;
        }
    }

    public static final GeneralType GENERAL = new GeneralType();

    /** The event data type */
    public static class EventType extends BaseType {
        private EventType() {
            super(EventToken.class, "event");
        }

        public Token convert(Token t) throws IllegalActionException {
            // FIXME: what does converting to general MEAN?
            return t;

            //     throw new IllegalActionException("Cannot convert token "
            //         + t + " to type general, because general is not a "
            //         + concrete type.");
        }

        public int getTypeHash() {
            return 12;
        }
    }

    public static final EventType EVENT = new EventType();

    ///////////////////////////////////////////////////////////////////
    ////                    package private method                 ////
    // Add entries in this class to index the given name and class to
    // the given type.
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

    ///////////////////////////////////////////////////////////////////
    ////                      private constructor                  ////
    // The constructor is private to make a type safe enumeration.
    private BaseType(Class c, String name) {
        _tokenClass = c;
        _name = name;
        _addType(this, name, c);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Class _tokenClass;
    private String _name;

    // A map from type name to the type for all base types.
    private static Map _nameToType;

    // A map from class name to the type for all base types.
    private static Map _classNameToType;
}
