/* The type of base token classes.

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
@AcceptedRating Red
*/

package ptolemy.data.type;

import java.lang.reflect.Modifier;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// BaseType
/**
The type of base token classes. This class provides a type safe
enumeration of base types.

@author Yuhong Xiong
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

    /** Return the class object for the token whose type is represented
     *  by this object.
     *  @return An instance of Class.
     */
//    public Class getTokenClass() {
//	return _tokenClass;
//    }

    /** Determine if the argument represents the same BaseType as this
     *  object.
     *  @param t A Type.
     *  @return True if the argument represents the same BaseType as
     *   this object; false otherwise.
     */
    public boolean isEqualTo(Type t) {
	// since BaseType is a type safe enumeration, can use == to
	// test equality.
	return this==t;
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

    /** The complex data type */
    public static final BaseType COMPLEX = new BaseType(ComplexToken.class,
	"complex",
	new ConvertOperator() {
	    public Token convert(Token t) throws IllegalActionException {
		return ComplexToken.convert(t);
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

    /** The integer data type */
    public static final BaseType INT = new BaseType(IntToken.class, "int",
	new ConvertOperator() {
	    public Token convert(Token t) throws IllegalActionException {
		return IntToken.convert(t);
	    }
	});

    /** The long integer data type */
    public static final BaseType LONG = new BaseType(LongToken.class, "long",
	new ConvertOperator() {
	    public Token convert(Token t) throws IllegalActionException {
		return LongToken.convert(t);
	    }
	});

    /** The bottom element of the data type lattice */
    public static final BaseType NAT = new BaseType(Void.TYPE, "NaT",
	new ConvertOperator() {
	    public Token convert(Token t) throws IllegalActionException {
		throw new IllegalActionException("Cannot convert to NaT.");
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
    public static final BaseType GENERAL = new BaseType(Token.class, "token",
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
    ////                        private methods                    ////

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

