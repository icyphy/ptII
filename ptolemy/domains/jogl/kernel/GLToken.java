package ptolemy.domains.jogl.kernel;


import java.io.Serializable;

import javax.media.opengl.GL;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////GLToken

/**
Tokens that contain a GL object. A GL objects is..

@author Yasemin Demir, Based on KeyToken by Christopher Brooks
@version $Id$
@since Ptolemy II 4.0
@Pt.ProposedRating Yellow (cxh)
@Pt.AcceptedRating Yellow (cxh)
*/
public class GLToken extends Token {
   /** Construct a token with a specified GL.
    *  @param value The specified java.security.GL type to construct
    *  the token with.
    */
   public GLToken(GL value) {
       _value = value;
   }

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   /** Return the type of this token.
    *  @return {@link #GL_TYPE}, the least upper bound of all the cryptographic
    *  GL types.
    */
   public Type getType() {
       return GL_TYPE;
   }

   /** Return the java.security.GL.
    *  @return The java.security.GL that this Token was created with.
    */
   public GL getValue() {
       return _value;
   }

   /** Test for equality of the values of this Token and the argument
    *  Token.  Two GLTokens are considered equals if the strings
    *  that name their corresponding algorithms and formats are the same
    *  and the byte arrays that contain the encoding have the same contents.
    *  Consult the java.security.GL documentation for the meaning of these
    *  terms.  If the value of this token or the value of the rightArgument
    *  token is null, then we return False.
    *
    *  @param rightArgument The Token to test against.
    *  @exception IllegalActionException Not thrown in this base class.
    *  @return A boolean token that contains the value true if the
    *  algorithms, formats and encodings are the same.
    */
   public final BooleanToken isEqualTo(Token rightArgument)
           throws IllegalActionException {
       GL rightGL = ((GLToken) rightArgument).getValue();
       GL leftGL = getValue();

       if ((rightGL == null) || (leftGL == null)) {
           return BooleanToken.FALSE;
       }

       // FIXME: We should check if the fields of the two objects are equal.

       return new BooleanToken(rightGL.equals(leftGL));
   }

   /** Return a String representation of the GLToken.
    *  @return A String representation of the GLToken that includes
    *  the value of the algorithm, format and encoding.
    */
   public String toString() {

       return _value.toString();
   }

   /** The cryptographic GL type.
    */
   public static class GLType implements Cloneable, Type, Serializable {
       ///////////////////////////////////////////////////////////////////
       ////                         constructors                      ////
       // The constructor is private to make a type safe enumeration.
       // We could extend BaseType, yet the BaseType(Class, String)
       // Constructor is private.
       private GLType() {
           super();
       }

       ///////////////////////////////////////////////////////////////////
       ////                         public methods                    ////

       /** Return a new type which represents the type that results from
        *  adding a token of this type and a token of the given argument
        *  type.
        *  @param rightArgumentType The type to add to this type.
        *  @return A new type, or BaseType.GENERAL, if the operation does
        *  not make sense for the given types.
        */
       public Type add(Type rightArgumentType) {
           return this;
       }

       /** Return this, that is, return the reference to this object.
        *  @return A GLType
        */
       public Object clone() {
           // FIXME: Note that we do not call super.clone() here.  Is that right?
           return this;
       }

       /** Convert the specified token to a token having the type
        *  represented by this object.
        *  @param token A token.
        *  @return A token.
        *  @exception IllegalActionException If lossless conversion cannot
        *   be done.
        */
       public Token convert(Token token) throws IllegalActionException {
           if (token instanceof GLToken) {
               return token;
           } else {
               throw new IllegalActionException("Attempt to convert token "
                       + token + " into a GL token, which is not possible.");
           }
       }

       /** Return a new type which represents the type that results from
        *  dividing a token of this type and a token of the given
        *  argument type.
        *  @param rightArgumentType The type to add to this type.
        *  @return A new type, or BaseType.GENERAL, if the operation does
        *  not make sense for the given types.
        */
       public Type divide(Type rightArgumentType) {
           return this;
       }

       /** Return the class for tokens that this basetype represents.
        *  @return the class for tokens that this basetype represents.
        */
       public Class getTokenClass() {
           return GLToken.class;
       }

       /** Return true if this type does not correspond to a single token
        *  class.  This occurs if the type is not instantiable, or it
        *  represents either an abstract base class or an interface.
        *  @return Always return false, this token is instantiable.
        */
       public boolean isAbstract() {
           return false;
       }

       /** Test if the argument type is compatible with this type.
        *  The method returns true if this type is UNKNOWN, since any type
        *  is a substitution instance of it. If this type is not UNKNOWN,
        *  this method returns true if the argument type is less than or
        *  equal to this type in the type lattice, and false otherwise.
        *  @param type An instance of Type.
        *  @return True if the argument type is compatible with this type.
        */
       public boolean isCompatible(Type type) {
           return type == this;
       }

       /** Test if this Type is UNKNOWN.
        *  @return True if this Type is not UNKNOWN; false otherwise.
        */
       public boolean isConstant() {
           return true;
       }

       /** Return this type's node index in the (constant) type lattice.
        * @return this type's node index in the (constant) type lattice.
        */
       public int getTypeHash() {
           return Type.HASH_INVALID;
       }

       /** Determine if this type corresponds to an instantiable token
        *  classes. A BaseType is instantiable if it does not correspond
        *  to an abstract token class, or an interface, or UNKNOWN.
        *  @return True if this type is instantiable.
        */
       public boolean isInstantiable() {
           return true;
       }

       /** Return true if the argument is a
        *  substitution instance of this type.
        *  @param type A Type.
        *  @return True if this type is UNKNOWN; false otherwise.
        */
       public boolean isSubstitutionInstance(Type type) {
           return this == type;
       }

       /** Return a new type which represents the type that results from
        *  moduloing a token of this type and a token of the given
        *  argument type.
        *  @param rightArgumentType The type to add to this type.
        *  @return A new type, or BaseType.GENERAL, if the operation does
        *  not make sense for the given types.
        */
       public Type modulo(Type rightArgumentType) {
           return this;
       }

       /** Return a new type which represents the type that results from
        *  multiplying a token of this type and a token of the given
        *  argument type.
        *  @param rightArgumentType The type to add to this type.
        *  @return A new type, or BaseType.GENERAL, if the operation does
        *  not make sense for the given types.
        */
       public Type multiply(Type rightArgumentType) {
           return this;
       }

       /** Return the type of the multiplicative identity for elements of
        *  this type.
        *  @return A new type, or BaseType.GENERAL, if the operation does
        *  not make sense for the given types.
        */
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
       public Type subtract(Type rightArgumentType) {
           return this;
       }

       /** Return the string representation of this type.
        *  @return A String.
        */
       public String toString() {
           return "GL";
       }

       /** Return the type of the additive identity for elements of
        *  this type.
        *  @return A new type, or BaseType.GENERAL, if the operation does
        *  not make sense for the given types.
        */
       public Type zero() {
           return this;
       }
   }

   /** The GL type: the least upper bound of all the cryptographic
    *  GL types.
    */
   public static final Type GL_TYPE = new GLType();

   ///////////////////////////////////////////////////////////////////
   ////                         private variables                 ////

   /** The GL object. */
   private GL _value;
}


