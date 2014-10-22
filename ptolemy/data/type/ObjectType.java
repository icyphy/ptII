/* A type of tokens that contain arbitrary Java objects.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

/**

 A type of tokens that contain arbitrary Java objects. An instance of
 this class specifies the Java class that the contents of the
 ObjectTokens of that type must be instances of.

 <p>A special type lattice is defined for variants of ObjectType. The
 top element of the elements in the type lattice is an ObjectType that
 does not specify any Java class as the class for the contents of its
 tokens. In the expression language, that element can be referred to
 with "object" or "object()". Any ObjectToken conforms to this type. A
 subtype of this type specifies a Java class. In the expression
 language, such a subtype can be defined with "object(string)", where
 string is a string (starting and ending with a quote).  For example,
 the following expression refers to a variant of ObjectType to which
 only ObjectTokens containing atomic actors as their contents conform:
 <pre>object("ptolemy.actor.AtomicActor")</pre> This ObjectType is a
 subtype of the most general ObjectType, "object".  Furthermore, it is
 also a subtype of "object(\"ptolemy.kernel.Entity\")", and at the
 same time a supertype of "object(\"ptolemy.actor.lib.Const\")".

 <p>The bottom element of the type lattice is an artificial ObjectType
 with {@link BottomClass} as its specified Java class. In Java, the
 class hierarchy does not form a lattice, so this artificial type is
 needed to be the greatest lower bound for any two classes if one is
 not a subclass of the other.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @see ObjectToken
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ObjectType extends StructuredType implements Cloneable {

    /** Construct an ObjectType with null as the Java class specified in it.
     *  This type is the most general type (top element) among all the
     *  ObjectTypes.
     */
    public ObjectType() {
        this(null);
    }

    /** Construct an ObjectType with the given Java class as the class
     *  specified in it.
     *
     *  @param valueClass The Java class.
     */
    public ObjectType(Class<?> valueClass) {
        _class = valueClass;
    }

    /** Construct an ObjectType with the given Java class as the class
     *  specified in it.
     *
     *  @param value The actual object value, or null if the value is unknown.
     *  @param valueClass The Java class.
     *  @exception IllegalActionException If the <i>value</i> is not
     *  an instance of <i>valueClass</i>.
     */
    public ObjectType(Object value, Class<?> valueClass)
            throws IllegalActionException {
        if (value != null && valueClass != null
                && !valueClass.isInstance(value)) {
            throw new IllegalActionException("The value " + value + " is not "
                    + "an instance of class " + valueClass);
        }
        _value = value;
        _class = valueClass;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep clone of this type.
     *  @return A Type.
     */
    @Override
    public Object clone() {
        ObjectType type = new ObjectType();
        type._value = _value;
        type._class = _class;
        return type;
    }

    /** Convert the specified token into a token having the type
     *  represented by this object.
     *  @param token a token.
     *  @return a token.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    @Override
    public Token convert(Token token) throws IllegalActionException {
        if (token instanceof ObjectToken) {
            ObjectToken objectToken = (ObjectToken) token;
            Object value = objectToken.getValue();
            if (value == null || _class == null || _class.isInstance(value)) {
                return new ObjectToken(value, _class);
            }
        }
        throw new IllegalArgumentException(Token.notSupportedConversionMessage(
                token, this.toString()));
    }

    /** Determine if the argument represents the same type as this object.
     *  @param object A Type.
     *  @return True if the argument represents the same type as this
     *   object; false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ObjectType)) {
            return false;
        } else {
            Class<?> class1 = _class;
            Class<?> class2 = ((ObjectType) object)._class;
            return class1 == class2 || class1 != null && class1.equals(class2);
        }
    }

    /** Return the class for tokens that this type represents. The returned
     *  class is always {@link ObjectToken}.
     *  @return The class for tokens that this type represents.
     */
    @Override
    public Class<?> getTokenClass() {
        return ObjectToken.class;
    }

    /** Get the actual value.
     *
     *  @return The actual value, or null if it is unknown.
     */
    public Object getValue() {
        return _value;
    }

    /** Get the Java class specified in this type, of which the contents of
     *  ObjectTokens conforming to this type must be instances.
     *
     *  @return The Java class specified in this type.
     */
    public Class<?> getValueClass() {
        return _class;
    }

    /** Return the hash code for this object.
     *
     *  @return The hash code.
     */
    @Override
    public int hashCode() {
        int hash = 324342;
        if (_class != null) {
            hash += _class.hashCode();
        }
        return hash;
    }

    /** Ignore, as this type does not have elements.
     *  @param type The type to initialize unknown elements to.
     */
    @Override
    public void initialize(Type type) {
    }

    /** Return true if this type does not correspond to a single token
     *  class.  This occurs if the type is not instantiable, or it
     *  represents either an abstract base class or an interface.
     *  @return True if this type does not correspond to a single token
     *  class.
     */
    @Override
    public boolean isAbstract() {
        return _class != null;
    }

    /** Return true if the specified type is less than or equal to this type.
     *  @param type The type to be checked
     *  @return True if the specified type is less than or equal to this type.
     */
    @Override
    public boolean isCompatible(Type type) {
        if (type.equals(BaseType.UNKNOWN)) {
            return true;
        }
        if (!(type instanceof ObjectType)) {
            return false;
        }
        return _isLessThanOrEqualTo((ObjectType) type, this);
    }

    /** Test if the argument type is compatible with this type.
     *  Compatible is defined as follows: If this type is a constant, the
     *  argument is compatible if it is the same or less than this type in
     *  the type lattice; If this type is a variable, the argument is
     *  compatible if it is a substitution instance of this type.
     *  @return True if the argument is compatible with this type.
     */
    @Override
    public boolean isConstant() {
        return true;
    }

    /** Determine if this Type corresponds to an instantiable token
     *  class.
     *  @return True if this type corresponds to an instantiable
     *   token class.
     */
    @Override
    public boolean isInstantiable() {
        return true;
    }

    /** Return true if the specified type is a substitution instance of this
     *  type. For the argument to be a substitution instance, it must be
     *  either the same as this type, or it must be a type that can be
     *  obtained by replacing the BaseType.UNKNOWN component of this type by
     *  another type.
     *  @param type A Type.
     *  @return True if the argument is a substitution instance of this type.
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        return equals(type);
    }

    /** Return a string describing this object.
     *  @return A string of form 'object("classname")'.
     */
    @Override
    public String toString() {
        if (_class == null) {
            return "object(null)";
        } else {
            return "object(\"" + _class.getName() + "\")";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** The bottom element among all ObjectTypes.
     */
    public static final ObjectType BOTTOM = new ObjectType(BottomClass.class);

    /** The top element among all ObjectTypes.
     *  The value of this variable is == to the
     *  {@link ptolemy.data.type.BaseType#OBJECT} so that
     *  code generation can use "xxx == BaseType.OBJECT".
     */
    public static final ObjectType TOP = BaseType.OBJECT;

    /** An artificial Java class that serves as the bottom element.
     *  No other class subclasses this, so there is nothing below it.
     */
    public static class BottomClass {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be of the same structured type, otherwise an exception will
     *  be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type a StructuredType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    @Override
    protected int _compare(StructuredType type) {
        if (!(type instanceof ObjectType)) {
            throw new IllegalArgumentException("ObjectType._compare: "
                    + "The argument is not an ObjectType.");
        }
        if (this.equals(type)) {
            return CPO.SAME;
        }
        if (_isLessThanOrEqualTo(this, (ObjectType) type)) {
            return CPO.LOWER;
        }
        if (_isLessThanOrEqualTo((ObjectType) type, this)) {
            return CPO.HIGHER;
        }
        return CPO.INCOMPARABLE;
    }

    /** Return a static instance of this object type. The return
     *  value is used by TypeLattice to represent this type.
     *  @return The bottom object type.
     */
    @Override
    protected StructuredType _getRepresentative() {
        return BOTTOM;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param type a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    @Override
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if (!(type instanceof ObjectType)) {
            throw new IllegalArgumentException(
                    "ObjectType._greatestLowerBound: The argument is not an "
                            + "ObjectType.");
        }

        ObjectType objectType = (ObjectType) type;
        if (_isLessThanOrEqualTo(this, objectType)) {
            return this;
        } else if (_isLessThanOrEqualTo(objectType, this)) {
            return type;
        } else {
            return BOTTOM;
        }
    }

    /** Return the least upper bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param type a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    @Override
    protected StructuredType _leastUpperBound(StructuredType type) {
        if (!(type instanceof ObjectType)) {
            throw new IllegalArgumentException(
                    "ObjectType._leastUpperBound: The argument is not an "
                            + "ObjectType.");
        }

        ObjectType objectType = (ObjectType) type;
        if (_isLessThanOrEqualTo(this, objectType)) {
            return objectType;
        } else if (_isLessThanOrEqualTo(objectType, this)) {
            return this;
        } else {
            Class<?> class1 = _class;
            Class<?> class2 = objectType._class;
            if (class2 != null) {
                while (class1 != null) {
                    if (class1.isAssignableFrom(class2)) {
                        return new ObjectType(class1);
                    } else {
                        class1 = class1.getSuperclass();
                    }
                }
            }
            return TOP;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Test whether the first type is less than or equal to the second in the
     *  type lattice.
     *  @param t1 The first class.
     *  @param t2 The second class.
     *  @return true if the first class is less than or equal to the second;
     *   false otherwise.
     */
    private boolean _isLessThanOrEqualTo(ObjectType t1, ObjectType t2) {
        Class<?> class1 = t1._class;
        Class<?> class2 = t2._class;
        if (class1 == null && class2 == null) {
            return true;
        } else if (class1 == null) {
            return false;
        } else if (class2 == null) {
            return true;
        } else if (class1.equals(BottomClass.class)) {
            return true;
        } else {
            return class2.isAssignableFrom(class1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The Java class specified in this type.
     */
    private Class<?> _class;

    /** The actual Object, or null if the actual object is unknown.
     */
    private Object _value;
}
