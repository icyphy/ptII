/* The type of base token classes.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Red
*/

package ptolemy.data.type;

import java.lang.reflect.Modifier;
import java.util.Hashtable;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// BaseType
/**
The type of base token classes. This class provides a type safe
enumeration of base types.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
*/

public class BaseType implements Type {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token to a token having the type
     *  represented by this object.
     *  @param t A token.
     *  @return A token.
     *  @exception IllegalActionException If lossless conversion cannot
     *   be done.
     */
    public Token convert(Token t)
	    throws IllegalActionException {
	return _convertOp.convert(t);
    }

    /** Test if the argument token is compatible with this type. The method
     *  returns true if this type is NAT, since any type is a substitution
     *  instance of it. If this type is not NAT, this method returns true
     *  if the argument type is less than or equal to this type in the type
     *  lattice, and false otherwise.
     *  @param t A Token.
     *  @return True if the argument token is compatible with this type.
     */
    public boolean isCompatible(Token t) {
	if (this == NAT) {
	    return true;
	}

	int typeInfo = TypeLattice.compare(this, t.getType());
	return (typeInfo == CPO.SAME || typeInfo == CPO.HIGHER);
    }

    /** Test if this Type is NAT.
     *  @return True if this Type is not NAT; false otherwise.
     */
    public boolean isConstant() {
	return this != NAT;
    }

    /** Determine if the argument represents the same BaseType as this
     *  object.
     *  @param t A Type.
     *  @return True if the argument represents the same BaseType as
     *   this object; false otherwise.
     */
    public boolean isEqualTo(Type t) {
	// since BaseType is a type safe enumeration, can use == to
	// test equality.
	return this == t;
    }

    /** Determine if this type corresponds to an instantiable token
     *  classes. A BaseType is instantiable if it does not correspond
     *  to an abstract token class, or an interface, or NaT.
     *  @return True if this type is instantiable.
     */
    public boolean isInstantiable() {
	if (this == NAT) {
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
     *  @return True if this type is NAT; false otherwise.
     */
    public boolean isSubstitutionInstance(Type type) {
	return this == NAT;
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    public String toString() {
	return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////

    /** The boolean data type */
    public static final BaseType BOOLEAN = new BaseType(BooleanToken.class,
            "boolean",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return BooleanToken.convert(t);
        }
    });

    /** The boolean matrix data type */
    public static final BaseType BOOLEAN_MATRIX = new BaseType(
            BooleanMatrixToken.class,
            "booleanMatrix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return BooleanMatrixToken.convert(t);
        }
    });

    /** The complex data type */
    public static final BaseType COMPLEX = new BaseType(ComplexToken.class,
            "complex",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return ComplexToken.convert(t);
        }
    });

    /** The complex matrix data type */
    public static final BaseType COMPLEX_MATRIX = new BaseType(
            ComplexMatrixToken.class,
            "complexMatrix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return ComplexMatrixToken.convert(t);
        }
    });

    /** The double data type */
    public static final BaseType DOUBLE = new BaseType(DoubleToken.class,
            "double",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return DoubleToken.convert(t);
        }
    });

    /** The double matrix data type */
    public static final BaseType DOUBLE_MATRIX = new BaseType(
            DoubleMatrixToken.class,
            "doubleMatrix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return DoubleMatrixToken.convert(t);
        }
    });

    /** The fix data type */
    public static final BaseType FIX = new BaseType(FixToken.class, "fix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return FixToken.convert(t);
        }
    });

    /** The fix matrix data type */
    public static final BaseType FIX_MATRIX = new BaseType(
            FixMatrixToken.class,
            "fixMatrix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return FixMatrixToken.convert(t);
        }
    });

    /** The integer data type */
    public static final BaseType INT = new BaseType(IntToken.class, "int",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return IntToken.convert(t);
        }
    });

    /** The integer matrix data type */
    public static final BaseType INT_MATRIX = new BaseType(
            IntMatrixToken.class,
            "intMatrix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return IntMatrixToken.convert(t);
        }
    });

    /** The long integer data type */
    public static final BaseType LONG = new BaseType(LongToken.class, "long",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return LongToken.convert(t);
        }
    });

    /** The long matrix data type */
    public static final BaseType LONG_MATRIX = new BaseType(
            LongMatrixToken.class,
            "longMatrix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return LongMatrixToken.convert(t);
        }
    });

    /** The matrix data type */
    public static final BaseType MATRIX = new BaseType(
            MatrixToken.class,
            "matrix",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return MatrixToken.convert(t);
        }
    });

    /** The bottom element of the data type lattice */
    public static final BaseType NAT = new BaseType(Void.TYPE, "NaT",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            // Since any type is a substitution instance of NAT, just
            // return the argument.
            return t;
        }
    });

    /** The numerical data type */
    public static final BaseType NUMERICAL = new BaseType(Numerical.class,
            "numerical",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            throw new IllegalActionException("Cannot convert to " +
                    "numerical.");
        }
    });

    /** The object data type */
    public static final BaseType OBJECT = new BaseType(ObjectToken.class,
            "object",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return ObjectToken.convert(t);
        }
    });

    /** The scalar data type */
    public static final BaseType SCALAR = new BaseType(ScalarToken.class,
            "scalar",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return ScalarToken.convert(t);
        }
    });

    /** The string data type */
    public static final BaseType STRING = new BaseType(StringToken.class,
            "string",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return StringToken.convert(t);
        }
    });

    /** The general data type */
    public static final BaseType GENERAL = new BaseType(Token.class, "general",
            new ConvertOperator() {
        public Token convert(Token t) throws IllegalActionException {
            return Token.convert(t);
        }
    });

    ///////////////////////////////////////////////////////////////////
    ////                      private constructor                  ////

    // The constructor is private to make a type safe enumeration.
    private BaseType(Class c, String name, ConvertOperator op) {
	_tokenClass = c;
	_name = name;
	_convertOp = op;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private Class _tokenClass;
    private String _name;
    private ConvertOperator _convertOp;

    ///////////////////////////////////////////////////////////////////
    ////                      private interface                    ////

    // This interface achieves the effect of HOF by passing the convert
    // method in an object implementing this interface.
    private interface ConvertOperator {
	public Token convert(Token t) throws IllegalActionException;
    }
}
