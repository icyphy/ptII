/* A token that contains a reference to an arbitrary object.

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
package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// ObjectToken

/**
 A token that contains a reference to an arbitrary object.
 Note that when this token constructed, the object passed to the constructor
 is not cloned. Thus, care must be exercised to ensure that actors do
 not modify that object in a nondeterministic way, unless such nondeterminism
 is acceptable.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (wbwu)
 */
public class ObjectToken extends Token {

    /** Construct an empty token.
     */
    public ObjectToken() {
    }

    /** Construct a token with a reference to the specified object.
     *  @param value The specified object referred to by this token.
     *  @exception IllegalActionException If the argument is not of
     *  the appropriate type (may be thrown by derived classes, but is
     *  not thrown here).
     */
    public ObjectToken(Object value) throws IllegalActionException {
        _value = value;
        _class = null;
    }

    /** Construct a token with the given value and the given class as the
     *  value's type.
     *
     *  @param value The value.
     *  @param valueClass The class of the value.
     *  @exception IllegalActionException If the argument is not of
     *  the appropriate type.
     */
    public ObjectToken(Object value, Class<?> valueClass)
            throws IllegalActionException {
        if (value != null && valueClass != null
                && !valueClass.isInstance(value)) {
            throw new IllegalActionException("The value " + value + " is not "
                    + "an instance of class " + valueClass);
        }
        _value = value;
        _class = valueClass;
    }

    /** Convert the specified token into an instance of ObjectToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of ObjectToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below ObjectToken in the type hierarchy, it is converted to
     *  an instance of ObjectToken or one of the subclasses of
     *  ObjectToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to an ObjectToken.
     *  @return An ObjectToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static ObjectToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof ObjectToken) {
            return (ObjectToken) token;
        }

        throw new IllegalActionException(notSupportedConversionMessage(token,
                "object"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the argument is an instance of ObjectToken and its
     *  contained object is equal to the object contained in this token,
     *  as tested by the equals() method of the contained object.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of ObjectToken and its
     *   contained object is equal to the object contained in this token.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        ObjectToken objectToken = (ObjectToken) object;
        if (_class == null && objectToken._class == null || _class != null
                && _class.equals(objectToken._class)) {
            return _value == null && objectToken._value == null
                    || _value != null && _value.equals(objectToken._value);
        } else {
            return false;
        }
    }

    /** Return the type of this token.
     *  @return BaseType.OBJECT
     */
    @Override
    public Type getType() {
        if (_class == null) {
            return BaseType.OBJECT;
        } else {
            try {
                return new ObjectType(_value, _class);
            } catch (IllegalActionException e) {
                throw new InternalErrorException("This ObjectToken does not "
                        + "contain a value that conforms to the specified "
                        + "class.");
            }
        }
    }

    /** Return the value of the token, a reference to an object.
     *  @return The Object in this token.
     */
    public Object getValue() {
        return _value;
    }

    /** Return the class of the object contained in this token.
     *  @return The class of the object.
     */
    public Class getValueClass() {
        return _class;
    }

    /** Return a hash code value for this token. This method returns the
     *  hash code of the contained object.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        if (_value == null && _class == null) {
            return 0;
        } else if (_value == null) {
            return _class.hashCode();
        } else if (_class == null) {
            return _value.hashCode();
        } else {
            return _value.hashCode() + _class.hashCode();
        }
    }

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. If the argument token is not of
     *  the same type as this token, then either this token or the
     *  argument will be converted, if possible, to the type of the other.
     *  <p>
     *  Subclasses should not
     *  generally override this method, but override the protected
     *  _isCloseTo() method to ensure that type conversion is performed
     *  consistently.
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.  Ignored in this class.
     *  @return A boolean token that contains the value true if the
     *   value and units of this token are close to those of the
     *   argument token.
     *  @exception IllegalActionException If the argument token and
     *   this token are of incomparable types, or the operation does
     *   not make sense for the given types.
     */
    @Override
    public final BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        // FIXME: This is copied from AbstractConvertibleToken.

        // Note that if we had absolute(), subtraction() and islessThan()
        // we could perhaps define this method for all tokens.
        int typeInfo = TypeLattice.compare(getType(), token);

        if (typeInfo == CPO.SAME) {
            return _isCloseTo(token, epsilon);
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(token);

            try {
                BooleanToken result = _isCloseTo(convertedArgument, epsilon);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "isCloseTo", this, token));
            }
        } else if (typeInfo == CPO.LOWER) {
            return token.isCloseTo(this, epsilon);
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "isCloseTo", this, token));
        }
    }

    /** Compare this ObjectToken to the given argument, and return true if the
     *  values contained in the two are the same Java object.
     *
     *  @param rightArgument The argument.
     *  @return true if the values are the same Java object, or false otherwise.
     */
    @Override
    public BooleanToken isEqualTo(Token rightArgument) {
        if (rightArgument instanceof ObjectToken
                && ((ObjectToken) rightArgument)._value == _value) {
            return BooleanToken.TRUE;
        } else {
            return BooleanToken.FALSE;
        }
    }

    /** Return an ObjectToken with value null and class specified by the
     *  className argument.
     *
     *  @param className The className.
     *  @return The ObjectToken.
     *  @exception IllegalActionException If the class with className as its name
     *   cannot be loaded.
     */
    public static ObjectToken object(String className)
            throws IllegalActionException {
        try {
            Class objectClass = Class.forName(className);
            return new ObjectToken(null, objectClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(null, e, "Unable to load class "
                    + className);
        }
    }

    /** Return the value of this token as a string.
     *  The returned syntax looks like a function call to a one argument method
     *  named "object".  The argument is the string representation of the
     *  contained object, or the string "null" if the object is null.  Notice
     *  that this syntax is not currently parsable by the expression language.
     *  @return A String representing the object.
     */
    @Override
    public String toString() {
        String value = _value == null ? "null" : _value.toString();
        // String clazz = _class == null ? "" : ": " + _class.getName();
        return "object(" + value + ")";
    }

    /** A new empty ObjectToken. */
    public static final ObjectToken NULL = new ObjectToken();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  an ObjectToken.
     *  @param rightArgument The token to add to this token.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.  This parameter is ignored by this class.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        return _isEqualTo(rightArgument);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  ObjectToken.
     *  @param rightArgument The token to add to this token.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {
        ObjectToken convertedArgument = (ObjectToken) rightArgument;
        return BooleanToken.getInstance(toString().compareTo(
                convertedArgument.toString()) == 0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The class of the object.
     */
    private Class _class = null;

    /** The actual Object.
     *  This is protected to allow access in derived classes only.
     */
    private Object _value = null;
}
