/*

 Copyright (c) 2008 The Regents of the University of California.
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
import ptolemy.kernel.util.IllegalActionException;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ObjectType implements Type {

    /**
     *
     */
    public ObjectType() {
        this(null);
    }

    public ObjectType(Class<?> objectClass) {
        _valueClass = objectClass;
    }

    public Type add(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    public Object clone() {
        return new ObjectType(_valueClass);
    }

    /**
     *  @param token
     *  @return
     *  @throws IllegalActionException
     */
    public Token convert(Token token) throws IllegalActionException {
        if (token instanceof ObjectToken) {
            ObjectToken objectToken = (ObjectToken) token;
            Object value = objectToken.getValue();
            if (_valueClass.isInstance(value)) {
                return new ObjectToken(value, _valueClass);
            }
        }
        throw new IllegalArgumentException(Token
                .notSupportedConversionMessage(token, this.toString()));
    }

    public Type divide(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ObjectType)) {
            return false;
        } else {
            Class<?> class1 = _valueClass;
            Class<?> class2 = ((ObjectType) object)._valueClass;
            return class1 == class2 || class1 != null && class1.equals(class2);
        }
    }

    /**
     *  @return
     */
    public Class<?> getTokenClass() {
        return ObjectToken.class;
    }

    public int getTypeHash() {
        return Type.HASH_INVALID;
    }

    public Class<?> getValueClass() {
        return _valueClass;
    }

    public int hashCode() {
        int hash = 324342;
        if (_valueClass != null) {
            hash += _valueClass.hashCode();
        }
        return hash;
    }

    public boolean isAbstract() {
        return _valueClass != null;
    }

    /**
     *  @param type
     *  @return
     */
    public boolean isCompatible(Type type) {
        if (type.equals(BaseType.UNKNOWN)) {
            return true;
        }
        if (!(type instanceof ObjectType)) {
            return false;
        }
        return _isLessThanOrEqualTo((ObjectType) type, this);
    }

    /**
     *  @return
     */
    public boolean isConstant() {
        return true;
    }

    /**
     *  @return
     */
    public boolean isInstantiable() {
        return true;
    }

    /**
     *  @param type
     *  @return
     */
    public boolean isSubstitutionInstance(Type type) {
        return equals(type);
    }

    public Type modulo(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    public Type multiply(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    public Type one() {
        return this;
    }

    public Type subtract(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    public String toString() {
        if (_valueClass == null) {
            return "object(null)";
        } else {
            return "object(\"" + _valueClass.getName() + "\")";
        }
    }

    public Type zero() {
        return this;
    }

    public static final ObjectType BOTTOM = new ObjectType(BottomClass.class);

    public static final ObjectType TOP = new ObjectType();

    public static class BottomClass {
    }

    /**
     *  @param type
     *  @return
     */
    protected Type _greatestLowerBound(Type type) {
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

    /**
     *  @param type
     *  @return
     */
    protected Type _leastUpperBound(Type type) {
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
            Class<?> class1 = _valueClass;
            Class<?> class2 = objectType._valueClass;
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

    private boolean _isLessThanOrEqualTo(ObjectType t1, ObjectType t2) {
        Class<?> class1 = t1._valueClass;
        Class<?> class2 = t2._valueClass;
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

    private Class<?> _valueClass;

}
