/* A class containing declarations created by the compiler of
   of known fields and methods in the ptolemy.actor and ptolemy.data
   packages.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.codegen;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

//import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A class containing declarations created by the compiler of
of known fields and methods in the ptolemy.actor and ptolemy.data
packages.
@author Jeff Tsay
@version: $Id$
*/
public class PtolemyTypeIdentifier extends TypeIdentifier {

    /** Empty constructor that does nothing. */
    public PtolemyTypeIdentifier() {}

    /** Return a new TypeNode which reprents the type of the data
     *  encapsulated by the token class of the argument kind. Throw an
     *  IllegalArgumentException if the kind is not a kind of
     *  token. If kind is TYPE_KIND_TOKEN or TYPE_KIND_SCALAR, return
     *  IntTypeNode.instance. If kind is TYPE_KIND_MATRIX_TOKEN,
     *  return an array type with base type IntTypeNode.instance and
     *  dimension 2.
     */
    public TypeNode encapsulatedDataType(int kind) {
        if (!isSupportedTokenKind(kind)) {
            throw new IllegalArgumentException("kind "
                    + kind + " is not a token kind");
        }

        return
            (TypeNode) _TOKEN_CONTAINED_TYPES[kind - TYPE_KIND_TOKEN].clone();
    }

    /** Return a new TypeNode which reprents the type of the data
     *  encapsulated by the token class of the argument Ptolemy
     *  type. Throw an IllegalArgumentException if the Ptolemy type is
     *  not supported. If kind is TYPE_KIND_TOKEN or TYPE_KIND_SCALAR,
     *  return IntTypeNode.instance.  If kind is
     *  TYPE_KIND_MATRIX_TOKEN, return an matrix type with base type
     *  IntTypeNode.instance and dimension 2.
     */
    public TypeNode encapsulatedDataType(Type type) {
        if (_debug) {
            System.out.println("PtolemyTypeIdentifier.encapsulatedDataType(): "
                    + type);
        }
        return encapsulatedDataType(kindOfTokenType(type));
    }

    /** Return true iff the kind is a class kind. In derived classes, the
     *  kind() may return a different number for special classes, so this
     *  method checks if the kind is any class kind.
     */
    public boolean isClassKind(int kind) {
        return (((kind >= TYPE_KIND_COMPLEX) &&
                (kind <= TYPE_KIND_NO_TOKEN_EXCEPTION)) ||
                super.isClassKind(kind));
    }

    /** Return true iff the kind is that of a concrete token class. */
    public boolean isConcreteTokenKind(int kind) {
        if (!isSupportedTokenKind(kind)) {
            return false;
        }
        return _IS_CONCRETE_TOKEN[kind - TYPE_KIND_TOKEN];
    }

    /** Return true iff the kind is that of a matrix token class. */
    public boolean isMatrixTokenKind(int kind) {
        return ((kind >= TYPE_KIND_MATRIX_TOKEN) &&
                (kind <= TYPE_KIND_FIX_MATRIX_TOKEN));
    }

    /** Return true iff the kind represents an exception in
     *  Ptolemy. This method should be overridden in derived classes
     *  if additional exceptions are thrown in a domain.
     */
    public boolean isPtolemyExceptionKind(int kind) {
        return (/*(kind >= TYPE_KIND_CHANGE_FAILED_EXCEPTION) &&*/
		(kind >= TYPE_KIND_ILLEGAL_ACTION_EXCEPTION) &&
                (kind <= TYPE_KIND_NO_TOKEN_EXCEPTION));
    }

    /** Return true iff the kind represents a runtime exception in
     *  Ptolemy. This method should be overridden in derived classes
     *  if additional runtime exceptions are thrown in a domain.
     */
    public boolean isPtolemyRuntimeExceptionKind(int kind) {
        return ((kind >= TYPE_KIND_INTERNAL_ERROR_EXCEPTION) &&
                (kind <= TYPE_KIND_NO_TOKEN_EXCEPTION));
    }

    /** Return true iff the kind is that of a scalar token class. */
    public boolean isScalarTokenKind(int kind) {
        return ((kind >= TYPE_KIND_SCALAR_TOKEN) &&
                (kind <= TYPE_KIND_FIX_TOKEN));
    }

    /** Return true iff the kind represents a TypedAtomicActor or a
     *  subclass of it that is valid in the given domain. This method
     *  should be overridden in derived classes if there are
     *  additional actor classes in a domain.
     */
    public boolean isSupportedActorKind(int kind) {
        return (kind == TYPE_KIND_TYPED_ATOMIC_ACTOR);
    }

    /** Return true iff the kind represents a TypedIOPort or a subclass of it
     *  that is valid in the given domain. This method should be overridden in
     *  derived classes.
     */
    public boolean isSupportedPortKind(int kind) {
        return (kind == TYPE_KIND_TYPED_IO_PORT);
    }

    /** Return true iff the kind represents a supported type of token.
     */
    public boolean isSupportedTokenKind(int kind) {
        return ((kind >= TYPE_KIND_TOKEN) &&
                (kind <= TYPE_KIND_FIX_MATRIX_TOKEN));
    }

    /** Return an integer representing the user type that has the
     *  specified ClassDecl, which may be a special type in
     *  Ptolemy. If the type is not a special type, return the integer
     *  given by super.kindOfClassDecl(classDecl).
     */
    public int kindOfClassDecl(ClassDecl classDecl) {
        for (int i = 0; i < _KNOWN_CLASS_DECLS.length; i++) {
            if (classDecl == _KNOWN_CLASS_DECLS[i]) {
                return _KNOWN_KINDS[i];
            }
        }
        return super.kindOfClassDecl(classDecl);
    }

    /** Return the kind of token that would be returned by
     *  getElementAsToken() on a Token of the given kind. Throw an
     *  IllegalArgumentException if the kind is not a MatrixToken
     *  kind. If kind is TYPE_KIND_MATRIX_TOKEN, return TYPE_KIND_INT.
     */
    public int kindOfMatrixElement(int kind) {
        if ((kind < TYPE_KIND_MATRIX_TOKEN) ||
                (kind > TYPE_KIND_FIX_MATRIX_TOKEN)) {
            throw new IllegalArgumentException("matrixElementTokenKind() "
                    + "kind ("
                    + kind
                    + ") is not a MatrixToken kind,");
        }

        return _MATRIX_ELEMENT_TOKEN_KINDS[kind - TYPE_KIND_MATRIX_TOKEN];
    }

    /** Return the kind corresponding to a type in Ptolemy. The argument should
     *  not be null.
     */
    public int kindOfTokenType(Type type) {
        for (int i = 0; i < _KNOWN_TOKEN_TYPES.length; i++) {
            //if (type == _KNOWN_TOKEN_TYPES[i]) {
	    if (type.equals(_KNOWN_TOKEN_TYPES[i])) {
                return i + TYPE_KIND_TOKEN;
            }
        }

        //System.out.println("kindOfTokenType(): Warning: type = '"
        //        + type
        //        + "' Known types =\n");
        //for (int i = 0; i < _KNOWN_TOKEN_TYPES.length; i++) {
        //    System.err.println(i + " " + _KNOWN_TOKEN_TYPES[i]);
        //}

        if (_debug) {
            System.err.println("PtolemyTypeIdentifier.kindOfTokenType(): "
                    + "type = '" + type + "' == failed, trying " 
                    + "equals()");
        }

        //try {
        //    throw new Exception("kindOfTokenType() stack");
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
        for (int i = 0; i < _KNOWN_TOKEN_TYPES.length; i++) {
            if (type.equals(_KNOWN_TOKEN_TYPES[i])) {
                if (_debug) {
                    System.err.println("kindOfTokenType(): equals() found"
                            + "one\n type: " + type
                            + " " + type.hashCode()
                            + "\n   _KNOWN_TOKEN_TYPES: "
                            + _KNOWN_TOKEN_TYPES[i] 
                            + " " + _KNOWN_TOKEN_TYPES[i].hashCode()
                                       );
                }
                return i + TYPE_KIND_TOKEN;
            }
        }

        throw new RuntimeException("kindOfTokenType(): type unknown, type = "
                + type);
        //return TYPE_KIND_UNKNOWN;
    }

    /** Return a new TypeNameNode corresponding to a token type in Ptolemy.
     *  If the argument type is BaseType.UNKNOWN, return a clone of
     *  DUMMY_LOWER_BOUND_TYPE. The argument should not be null.
     */
    public TypeNameNode typeNodeForTokenType(Type type) {
        //System.out.println("PtolemyTypeIdentifier.typeNodeForTokenType(): "
        //        + type);
        return typeNodeForKind(kindOfTokenType(type));
    }

    /** Return a new TypeNameNode that corresponds to the type indicated by
     *  the kind. The TypeNameNode must be reallocated so that later operations
     *  on the node do not affect TypeNameNode stored in this class.
     */
    public TypeNameNode typeNodeForKind(int kind) {
        return (TypeNameNode) _KNOWN_TYPENAMENODES[kind - TYPE_KINDS].clone();
    }

    // Mathematical kinds.
    public static final int TYPE_KIND_COMPLEX              = TYPE_KINDS;
    public static final int TYPE_KIND_FIX_POINT            = TYPE_KIND_COMPLEX + 1;

    // Actor kinds.
    public static final int TYPE_KIND_TYPED_ATOMIC_ACTOR   = TYPE_KIND_FIX_POINT + 1;

    // Token kinds.
    public static final int TYPE_KIND_TOKEN                = TYPE_KIND_TYPED_ATOMIC_ACTOR + 1;
    public static final int TYPE_KIND_BOOLEAN_TOKEN        = TYPE_KIND_TOKEN + 1;
    public static final int TYPE_KIND_SCALAR_TOKEN         = TYPE_KIND_BOOLEAN_TOKEN + 1;
    public static final int TYPE_KIND_INT_TOKEN            = TYPE_KIND_SCALAR_TOKEN + 1;
    public static final int TYPE_KIND_DOUBLE_TOKEN         = TYPE_KIND_INT_TOKEN + 1;
    public static final int TYPE_KIND_LONG_TOKEN           = TYPE_KIND_DOUBLE_TOKEN + 1;
    public static final int TYPE_KIND_COMPLEX_TOKEN        = TYPE_KIND_LONG_TOKEN + 1;
    public static final int TYPE_KIND_FIX_TOKEN            = TYPE_KIND_COMPLEX_TOKEN + 1;
    public static final int TYPE_KIND_OBJECT_TOKEN         = TYPE_KIND_FIX_TOKEN + 1;
    public static final int TYPE_KIND_STRING_TOKEN         = TYPE_KIND_OBJECT_TOKEN + 1;

    public static final int TYPE_KIND_ARRAY_TOKEN          = TYPE_KIND_STRING_TOKEN + 1;
    public static final int TYPE_KIND_INT_ARRAY_TOKEN      = TYPE_KIND_ARRAY_TOKEN + 1;
    public static final int TYPE_KIND_DOUBLE_ARRAY_TOKEN   = TYPE_KIND_INT_ARRAY_TOKEN + 1;
    // FIXME: what about long arrays etc.

    //public static final int TYPE_KIND_MATRIX_TOKEN         = TYPE_KIND_STRING_TOKEN + 1;
    public static final int TYPE_KIND_MATRIX_TOKEN         = TYPE_KIND_DOUBLE_ARRAY_TOKEN + 1;

    public static final int TYPE_KIND_BOOLEAN_MATRIX_TOKEN = TYPE_KIND_MATRIX_TOKEN + 1;
    public static final int TYPE_KIND_INT_MATRIX_TOKEN     = TYPE_KIND_BOOLEAN_MATRIX_TOKEN + 1;
    public static final int TYPE_KIND_DOUBLE_MATRIX_TOKEN  = TYPE_KIND_INT_MATRIX_TOKEN + 1;
    public static final int TYPE_KIND_LONG_MATRIX_TOKEN    = TYPE_KIND_DOUBLE_MATRIX_TOKEN + 1;
    public static final int TYPE_KIND_COMPLEX_MATRIX_TOKEN = TYPE_KIND_LONG_MATRIX_TOKEN + 1;
    // TYPE_KIND_FIX_MATRIX_TOKEN should be the last supported token,
    // isSupportedTokenKind() uses TYPE_KIND_FIX_MATRIX_TOKEN,
    // so if you change this change isSupportedTokenKind().
    public static final int TYPE_KIND_FIX_MATRIX_TOKEN     = TYPE_KIND_COMPLEX_MATRIX_TOKEN + 1;

    public static final int TYPE_KIND_DUMMY_TOKEN          = TYPE_KIND_FIX_MATRIX_TOKEN + 1;

    // Parameter kind.
    public static final int TYPE_KIND_PARAMETER            = TYPE_KIND_DUMMY_TOKEN + 1;

    // StringAttribute kind.
    public static final int TYPE_KIND_STRINGATTRIBUTE      = TYPE_KIND_PARAMETER + 1;

    // Port kind.
    public static final int TYPE_KIND_TYPED_IO_PORT        = TYPE_KIND_STRINGATTRIBUTE + 1;

    // Ptolemy exception kinds.
    public static final int TYPE_KIND_ILLEGAL_ACTION_EXCEPTION
    = TYPE_KIND_TYPED_IO_PORT + 1;
    public static final int TYPE_KIND_KERNEL_EXCEPTION
    = TYPE_KIND_ILLEGAL_ACTION_EXCEPTION + 1;
    public static final int TYPE_KIND_NAME_DUPLICATION_EXCEPTION
    = TYPE_KIND_KERNEL_EXCEPTION + 1;
    public static final int TYPE_KIND_NO_SUCH_ITEM_EXCEPTION
    = TYPE_KIND_NAME_DUPLICATION_EXCEPTION + 1;
    public static final int TYPE_KIND_TYPE_CONFLICT_EXCEPTION
    = TYPE_KIND_NO_SUCH_ITEM_EXCEPTION + 1;
    public static final int TYPE_KIND_INTERNAL_ERROR_EXCEPTION
    = TYPE_KIND_TYPE_CONFLICT_EXCEPTION + 1;
    public static final int TYPE_KIND_INVALID_STATE_EXCEPTION
    = TYPE_KIND_INTERNAL_ERROR_EXCEPTION + 1;
    public static final int TYPE_KIND_NOT_SCHEDULABLE_EXCEPTION
    = TYPE_KIND_INVALID_STATE_EXCEPTION + 1;
    public static final int TYPE_KIND_NO_ROOM_EXCEPTION
    = TYPE_KIND_NOT_SCHEDULABLE_EXCEPTION + 1;
    public static final int TYPE_KIND_NO_TOKEN_EXCEPTION
	= TYPE_KIND_NO_ROOM_EXCEPTION + 1;

    // Actor interface kind.
    public static final int TYPE_KIND_ACTOR = TYPE_KIND_NO_TOKEN_EXCEPTION + 1;

    public static final int TYPE_KIND_SEQUENCE_ACTOR = TYPE_KIND_ACTOR + 1;

    public static final int LAST_PTOLEMY_TYPE_KIND = TYPE_KIND_SEQUENCE_ACTOR;

    // Atomic actor type.
    public static final ClassDecl TYPED_ATOMIC_ACTOR_DECL;
    public static final TypeNameNode TYPED_ATOMIC_ACTOR_TYPE;

    // Mathematical types.
    public static final ClassDecl COMPLEX_DECL;
    public static final TypeNameNode COMPLEX_TYPE;

    public static final ClassDecl FIX_POINT_DECL;
    public static final TypeNameNode FIX_POINT_TYPE;

    // Token types.
    public static final ClassDecl TOKEN_DECL;
    public static final TypeNameNode TOKEN_TYPE;

    public static final ClassDecl SCALAR_TOKEN_DECL;
    public static final TypeNameNode SCALAR_TOKEN_TYPE;

    public static final ClassDecl BOOLEAN_TOKEN_DECL;
    public static final TypeNameNode BOOLEAN_TOKEN_TYPE;

    public static final ClassDecl INT_TOKEN_DECL;
    public static final TypeNameNode INT_TOKEN_TYPE;

    public static final ClassDecl DOUBLE_TOKEN_DECL;
    public static final TypeNameNode DOUBLE_TOKEN_TYPE;

    public static final ClassDecl LONG_TOKEN_DECL;
    public static final TypeNameNode LONG_TOKEN_TYPE;

    public static final ClassDecl COMPLEX_TOKEN_DECL;
    public static final TypeNameNode COMPLEX_TOKEN_TYPE;

    public static final ClassDecl FIX_TOKEN_DECL;
    public static final TypeNameNode FIX_TOKEN_TYPE;

    public static final ClassDecl STRING_TOKEN_DECL;
    public static final TypeNameNode STRING_TOKEN_TYPE;

    public static final ClassDecl OBJECT_TOKEN_DECL;
    public static final TypeNameNode OBJECT_TOKEN_TYPE;

    public static final ClassDecl ARRAY_TOKEN_DECL;
    public static final TypeNameNode ARRAY_TOKEN_TYPE;

    public static final ClassDecl INT_ARRAY_TOKEN_DECL;
    public static final TypeNameNode INT_ARRAY_TOKEN_TYPE;

    public static final ClassDecl DOUBLE_ARRAY_TOKEN_DECL;
    public static final TypeNameNode DOUBLE_ARRAY_TOKEN_TYPE;

    public static final ClassDecl MATRIX_TOKEN_DECL;
    public static final TypeNameNode MATRIX_TOKEN_TYPE;

    public static final ClassDecl BOOLEAN_MATRIX_TOKEN_DECL;
    public static final TypeNameNode BOOLEAN_MATRIX_TOKEN_TYPE;

    public static final ClassDecl INT_MATRIX_TOKEN_DECL;
    public static final TypeNameNode INT_MATRIX_TOKEN_TYPE;

    public static final ClassDecl DOUBLE_MATRIX_TOKEN_DECL;
    public static final TypeNameNode DOUBLE_MATRIX_TOKEN_TYPE;

    public static final ClassDecl LONG_MATRIX_TOKEN_DECL;
    public static final TypeNameNode LONG_MATRIX_TOKEN_TYPE;

    public static final ClassDecl COMPLEX_MATRIX_TOKEN_DECL;
    public static final TypeNameNode COMPLEX_MATRIX_TOKEN_TYPE;

    public static final ClassDecl FIX_MATRIX_TOKEN_DECL;
    public static final TypeNameNode FIX_MATRIX_TOKEN_TYPE;

    // A lower bound for type resolution.
    public static final ClassDecl DUMMY_LOWER_BOUND;
    public static final TypeNameNode DUMMY_LOWER_BOUND_TYPE;

    // Parameter type.
    public static final ClassDecl PARAMETER_DECL;
    public static final TypeNameNode PARAMETER_TYPE;

     // Parameter type.
    public static final ClassDecl STRINGATTRIBUTE_DECL;
    public static final TypeNameNode STRINGATTRIBUTE_TYPE;

    // Port type.
    public static final ClassDecl TYPED_IO_PORT_DECL;
    public static final TypeNameNode TYPED_IO_PORT_TYPE;

    // Kernel exception types.
    public static final ClassDecl ILLEGAL_ACTION_EXCEPTION_DECL;
    public static final TypeNameNode ILLEGAL_ACTION_EXCEPTION_TYPE;

    public static final ClassDecl KERNEL_EXCEPTION_DECL;
    public static final TypeNameNode KERNEL_EXCEPTION_TYPE;

    public static final ClassDecl NAME_DUPLICATION_EXCEPTION_DECL;
    public static final TypeNameNode NAME_DUPLICATION_EXCEPTION_TYPE;

    public static final ClassDecl NO_SUCH_ITEM_EXCEPTION_DECL;
    public static final TypeNameNode NO_SUCH_ITEM_EXCEPTION_TYPE;

    public static final ClassDecl TYPE_CONFLICT_EXCEPTION_DECL;
    public static final TypeNameNode TYPE_CONFLICT_EXCEPTION_TYPE;

    public static final ClassDecl INTERNAL_ERROR_EXCEPTION_DECL;
    public static final TypeNameNode INTERNAL_ERROR_EXCEPTION_TYPE;

    public static final ClassDecl INVALID_STATE_EXCEPTION_DECL;
    public static final TypeNameNode INVALID_STATE_EXCEPTION_TYPE;

    public static final ClassDecl NOT_SCHEDULABLE_EXCEPTION_DECL;
    public static final TypeNameNode NOT_SCHEDULABLE_EXCEPTION_TYPE;

    public static final ClassDecl NO_ROOM_EXCEPTION_DECL;
    public static final TypeNameNode NO_ROOM_EXCEPTION_TYPE;

    public static final ClassDecl NO_TOKEN_EXCEPTION_DECL;
    public static final TypeNameNode NO_TOKEN_EXCEPTION_TYPE;

    // Actor interface type.
    public static final ClassDecl ACTOR_DECL;
    public static final TypeNameNode ACTOR_TYPE;

    // SequenceActor is a marker interface that we remove
    // in ActorTransformerVisitor
    public static final ClassDecl SEQUENCE_ACTOR_DECL;
    public static final TypeNameNode SEQUENCE_ACTOR_TYPE;

    public static final Integer PTOLEMY_TRANSFORMED_KEY =
    new Integer(RESERVED_JAVA_PROPERTIES);

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    /** An array indexed by (kind - TYPE_KINDS) containing known declarations
     *  of types in Ptolemy.
     */
    protected static final ClassDecl[] _KNOWN_CLASS_DECLS;

    /** An array indexed by (kind - TYPE_KINDS) containing known types in
     *  Ptolemy.
     */
    protected static final TypeNameNode[] _KNOWN_TYPENAMENODES;

    /** An array indexed by (kind - TYPE_KINDS) containing kinds of types in
     *  Ptolemy.
     */
    protected static final int[] _KNOWN_KINDS = {
        TYPE_KIND_COMPLEX, TYPE_KIND_FIX_POINT,
        TYPE_KIND_TYPED_ATOMIC_ACTOR,
        TYPE_KIND_TOKEN, TYPE_KIND_BOOLEAN_TOKEN, TYPE_KIND_SCALAR_TOKEN,
        TYPE_KIND_INT_TOKEN, TYPE_KIND_DOUBLE_TOKEN, TYPE_KIND_LONG_TOKEN,
        TYPE_KIND_COMPLEX_TOKEN, TYPE_KIND_FIX_TOKEN, TYPE_KIND_OBJECT_TOKEN,
        TYPE_KIND_STRING_TOKEN,
        TYPE_KIND_ARRAY_TOKEN, TYPE_KIND_INT_ARRAY_TOKEN,
	TYPE_KIND_DOUBLE_ARRAY_TOKEN,
        TYPE_KIND_MATRIX_TOKEN,
        TYPE_KIND_BOOLEAN_MATRIX_TOKEN, TYPE_KIND_INT_MATRIX_TOKEN,
        TYPE_KIND_DOUBLE_MATRIX_TOKEN, TYPE_KIND_LONG_MATRIX_TOKEN,
        TYPE_KIND_COMPLEX_MATRIX_TOKEN, TYPE_KIND_FIX_MATRIX_TOKEN,
        TYPE_KIND_DUMMY_TOKEN,
        TYPE_KIND_PARAMETER,
        TYPE_KIND_STRINGATTRIBUTE,
        TYPE_KIND_TYPED_IO_PORT,
	TYPE_KIND_ILLEGAL_ACTION_EXCEPTION,
        TYPE_KIND_KERNEL_EXCEPTION, TYPE_KIND_NAME_DUPLICATION_EXCEPTION,
        TYPE_KIND_NO_SUCH_ITEM_EXCEPTION, TYPE_KIND_TYPE_CONFLICT_EXCEPTION,
        TYPE_KIND_INTERNAL_ERROR_EXCEPTION, TYPE_KIND_INVALID_STATE_EXCEPTION,
        TYPE_KIND_NOT_SCHEDULABLE_EXCEPTION, TYPE_KIND_NO_ROOM_EXCEPTION,
        TYPE_KIND_NO_TOKEN_EXCEPTION,
	TYPE_KIND_ACTOR,
	TYPE_KIND_SEQUENCE_ACTOR
    };

    /** An array indexed by (kind - TYPE_KIND_TOKEN) that is the corresponding
     *  Ptolemy type of the kind of token.
     */
    protected static final Type[] _KNOWN_TOKEN_TYPES = new Type[] {
        BaseType.GENERAL, BaseType.BOOLEAN, BaseType.SCALAR,
            BaseType.INT, BaseType.DOUBLE, BaseType.LONG,
            BaseType.COMPLEX, BaseType.FIX, BaseType.OBJECT,
            BaseType.STRING,
            // FIXME: we need to either generalize this to handle all
            // types, or add in each type by hand.
            // Array???,
	    ArrayType.ARRAY_TYPE,
	    ArrayType.INT_ARRAY_TYPE,
	    ArrayType.DOUBLE_ARRAY_TYPE,
            BaseType.MATRIX,
            BaseType.BOOLEAN_MATRIX, BaseType.INT_MATRIX,
            BaseType.DOUBLE_MATRIX, BaseType.LONG_MATRIX,
            BaseType.COMPLEX_MATRIX, BaseType.FIX_MATRIX,
            BaseType.UNKNOWN };

    /** An array indexed by (kind - TYPE_KIND_TOKEN) that indicates whether
     *  or not the token kind is concrete.
     */
    protected static final boolean[] _IS_CONCRETE_TOKEN = {
        false, true, false,
        true, true, true,
        true, true, true,
	false,
        true, true,
        true, false,
        true, true,
        true, true,
        true, true,
        true };

    /** An array indexed by (kind - TYPE_KIND_MATRIX_TOKEN) that gives the
     *  the kind of the token that would be returned by getElementAsToken()
     *  on a token of the index kind.
     */
    protected static final int[] _MATRIX_ELEMENT_TOKEN_KINDS = new int[] {
        TYPE_KIND_TOKEN, TYPE_KIND_INT_TOKEN,
            TYPE_KIND_BOOLEAN_TOKEN, TYPE_KIND_INT_TOKEN,
            TYPE_KIND_DOUBLE_TOKEN, TYPE_KIND_LONG_TOKEN,
            TYPE_KIND_COMPLEX_TOKEN, TYPE_KIND_FIX_TOKEN };

    /** An array indexed by (kind - TYPE_KIND_TOKEN) that is the corresponding
     *  type of the data encapsulated by the kind of token.
     */
    protected static final TypeNode[] _TOKEN_CONTAINED_TYPES;

    // Set to true to turn on debugging messages.
    protected final static boolean _debug = false;

    static {
        System.out.println("PtolemyTypeIdentifier<static>: start");

        try {
            CompileUnitNode typedAtomicActorUnit = StaticResolution.loadFile(
                    SearchPath.NAMED_PATH.openSource(
                            "ptolemy.actor.TypedAtomicActor"), 1,
                            "ptolemy.actor.TypedAtomicActor");
            TYPED_ATOMIC_ACTOR_DECL = (ClassDecl) StaticResolution.findDecl(
                    typedAtomicActorUnit, "TypedAtomicActor", CG_CLASS);
            TYPED_ATOMIC_ACTOR_TYPE = TYPED_ATOMIC_ACTOR_DECL.getDefType();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        //System.out.println("PtolemyTypeIdentifier<static>: load Complex");

        CompileUnitNode complexUnit =
            StaticResolution.loadClassName("ptolemy.math.Complex", 1);
        COMPLEX_DECL = (ClassDecl) StaticResolution.findDecl(complexUnit,
                "Complex", CG_CLASS);
        COMPLEX_TYPE = COMPLEX_DECL.getDefType();


        CompileUnitNode fixPointUnit =
            StaticResolution.loadClassName("ptolemy.math.FixPoint", 1);
        FIX_POINT_DECL = (ClassDecl) StaticResolution.findDecl(fixPointUnit,
                "FixPoint", CG_CLASS);
        FIX_POINT_TYPE = FIX_POINT_DECL.getDefType();


        CompileUnitNode tokenUnit =
            StaticResolution.loadClassName("ptolemy.data.Token", 1);
        TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(tokenUnit,
                "Token", CG_CLASS);
        TOKEN_TYPE = TOKEN_DECL.getDefType();


        CompileUnitNode booleanTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.BooleanToken", 1);
        BOOLEAN_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(booleanTokenUnit,
                    "BooleanToken", CG_CLASS);
        BOOLEAN_TOKEN_TYPE = BOOLEAN_TOKEN_DECL.getDefType();


        CompileUnitNode scalarTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.ScalarToken", 1);
        SCALAR_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(scalarTokenUnit,
                    "ScalarToken", CG_CLASS);
        SCALAR_TOKEN_TYPE = SCALAR_TOKEN_DECL.getDefType();


        CompileUnitNode intTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.IntToken", 1);
        INT_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(intTokenUnit,
                "IntToken", CG_CLASS);
        INT_TOKEN_TYPE = INT_TOKEN_DECL.getDefType();


        CompileUnitNode doubleTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.DoubleToken", 1);
        DOUBLE_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(doubleTokenUnit,
                    "DoubleToken", CG_CLASS);
        DOUBLE_TOKEN_TYPE = DOUBLE_TOKEN_DECL.getDefType();


        CompileUnitNode longTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.LongToken", 1);
        LONG_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(longTokenUnit,
                "LongToken", CG_CLASS);
        LONG_TOKEN_TYPE = LONG_TOKEN_DECL.getDefType();


        CompileUnitNode complexTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.ComplexToken", 1);
        COMPLEX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(complexTokenUnit,
                    "ComplexToken", CG_CLASS);
        COMPLEX_TOKEN_TYPE = COMPLEX_TOKEN_DECL.getDefType();


        CompileUnitNode fixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.FixToken", 1);
        FIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(fixTokenUnit,
                "FixToken", CG_CLASS);
        FIX_TOKEN_TYPE = FIX_TOKEN_DECL.getDefType();


        CompileUnitNode objectTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.ObjectToken", 1);
        OBJECT_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(objectTokenUnit,
                    "ObjectToken", CG_CLASS);
        OBJECT_TOKEN_TYPE = OBJECT_TOKEN_DECL.getDefType();


        CompileUnitNode stringTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.StringToken", 1);
        STRING_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(stringTokenUnit,
                    "StringToken", CG_CLASS);
        STRING_TOKEN_TYPE = STRING_TOKEN_DECL.getDefType();


        CompileUnitNode arrayTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.ArrayToken", 1);
        ARRAY_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(arrayTokenUnit,
                    "ArrayToken", CG_CLASS);
        ARRAY_TOKEN_TYPE = ARRAY_TOKEN_DECL.getDefType();

        CompileUnitNode intArrayTokenUnit =
            StaticResolution.loadClassName("ptolemy.codegen.data.IntArrayToken", 1);
        INT_ARRAY_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(intArrayTokenUnit,
                    "IntArrayToken", CG_CLASS);
        INT_ARRAY_TOKEN_TYPE = INT_ARRAY_TOKEN_DECL.getDefType();

        CompileUnitNode doubleArrayTokenUnit =
            StaticResolution.loadClassName("ptolemy.codegen.data.DoubleArrayToken",
                    1);
        DOUBLE_ARRAY_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(doubleArrayTokenUnit,
                    "DoubleArrayToken", CG_CLASS);
        DOUBLE_ARRAY_TOKEN_TYPE = DOUBLE_ARRAY_TOKEN_DECL.getDefType();

        CompileUnitNode matrixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.MatrixToken", 1);
        MATRIX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(matrixTokenUnit,
                    "MatrixToken", CG_CLASS);
        MATRIX_TOKEN_TYPE = MATRIX_TOKEN_DECL.getDefType();


        CompileUnitNode booleanMatrixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.BooleanMatrixToken",
                    1);
        BOOLEAN_MATRIX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(booleanMatrixTokenUnit,
                    "BooleanMatrixToken", CG_CLASS);
        BOOLEAN_MATRIX_TOKEN_TYPE = BOOLEAN_MATRIX_TOKEN_DECL.getDefType();


        CompileUnitNode intMatrixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.IntMatrixToken", 1);
        INT_MATRIX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(intMatrixTokenUnit,
                    "IntMatrixToken", CG_CLASS);
        INT_MATRIX_TOKEN_TYPE = INT_MATRIX_TOKEN_DECL.getDefType();


        CompileUnitNode doubleMatrixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.DoubleMatrixToken",
                    1);
        DOUBLE_MATRIX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(doubleMatrixTokenUnit,
                    "DoubleMatrixToken", CG_CLASS);
        DOUBLE_MATRIX_TOKEN_TYPE = DOUBLE_MATRIX_TOKEN_DECL.getDefType();


        CompileUnitNode longMatrixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.LongMatrixToken", 1);
        LONG_MATRIX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(longMatrixTokenUnit,
                    "LongMatrixToken", CG_CLASS);
        LONG_MATRIX_TOKEN_TYPE = LONG_MATRIX_TOKEN_DECL.getDefType();


        CompileUnitNode complexMatrixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.ComplexMatrixToken",
                    1);
        COMPLEX_MATRIX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl(complexMatrixTokenUnit,
                    "ComplexMatrixToken", CG_CLASS);
        COMPLEX_MATRIX_TOKEN_TYPE = COMPLEX_MATRIX_TOKEN_DECL.getDefType();


        CompileUnitNode  fixMatrixTokenUnit =
            StaticResolution.loadClassName("ptolemy.data.FixMatrixToken", 1);
        FIX_MATRIX_TOKEN_DECL =
            (ClassDecl) StaticResolution.findDecl( fixMatrixTokenUnit,
                    "FixMatrixToken", CG_CLASS);
        FIX_MATRIX_TOKEN_TYPE = FIX_MATRIX_TOKEN_DECL.getDefType();


        DUMMY_LOWER_BOUND = new ClassDecl("DummyLowerBound", null);
        NameNode dummyName = new NameNode(AbsentTreeNode.instance,
                "DummyLowerBound");
        dummyName.setProperty(DECL_KEY, DUMMY_LOWER_BOUND);
        DUMMY_LOWER_BOUND_TYPE = new TypeNameNode(dummyName);


        CompileUnitNode parameterUnit =
            StaticResolution.loadClassName("ptolemy.data.expr.Parameter", 1);
        PARAMETER_DECL = (ClassDecl) StaticResolution.findDecl( parameterUnit,
                "Parameter", CG_CLASS);
        PARAMETER_TYPE = PARAMETER_DECL.getDefType();

        CompileUnitNode stringAttributeUnit =
            StaticResolution.loadClassName("ptolemy.kernel.util.StringAttribute", 1);
        STRINGATTRIBUTE_DECL = (ClassDecl) StaticResolution.findDecl( stringAttributeUnit,
                "StringAttribute", CG_CLASS);
        STRINGATTRIBUTE_TYPE = STRINGATTRIBUTE_DECL.getDefType();


        CompileUnitNode typedIOPortUnit =
            StaticResolution.loadClassName("ptolemy.actor.TypedIOPort", 1);
        TYPED_IO_PORT_DECL =
            (ClassDecl) StaticResolution.findDecl(typedIOPortUnit,
                    "TypedIOPort", CG_CLASS);
        TYPED_IO_PORT_TYPE = TYPED_IO_PORT_DECL.getDefType();


        CompileUnitNode illegalActionUnit =
            StaticResolution
            .loadClassName("ptolemy.kernel.util.IllegalActionException", 1);
        ILLEGAL_ACTION_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(illegalActionUnit,
                    "IllegalActionException", CG_CLASS);
        ILLEGAL_ACTION_EXCEPTION_TYPE =
            ILLEGAL_ACTION_EXCEPTION_DECL.getDefType();

        CompileUnitNode kernelExceptionUnit =
            StaticResolution
            .loadClassName("ptolemy.kernel.util.KernelException", 1);
        KERNEL_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(kernelExceptionUnit,
                    "KernelException", CG_CLASS);
        KERNEL_EXCEPTION_TYPE = KERNEL_EXCEPTION_DECL.getDefType();


        CompileUnitNode nameDuplicationUnit =
            StaticResolution
            .loadClassName("ptolemy.kernel.util.NameDuplicationException", 1);
        NAME_DUPLICATION_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(nameDuplicationUnit,
                    "NameDuplicationException", CG_CLASS);
        NAME_DUPLICATION_EXCEPTION_TYPE =
            NAME_DUPLICATION_EXCEPTION_DECL.getDefType();


        CompileUnitNode nosuchItemUnit =
            StaticResolution
            .loadClassName("ptolemy.kernel.util.NoSuchItemException", 1);
        NO_SUCH_ITEM_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(nosuchItemUnit,
                    "NoSuchItemException", CG_CLASS);
        NO_SUCH_ITEM_EXCEPTION_TYPE = NO_SUCH_ITEM_EXCEPTION_DECL.getDefType();


        CompileUnitNode typeConflictUnit =
            StaticResolution
            .loadClassName("ptolemy.actor.TypeConflictException", 1);
        TYPE_CONFLICT_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(typeConflictUnit,
                    "TypeConflictException", CG_CLASS);
        TYPE_CONFLICT_EXCEPTION_TYPE =
            TYPE_CONFLICT_EXCEPTION_DECL.getDefType();


        CompileUnitNode internalErrorUnit =
            StaticResolution
            .loadClassName("ptolemy.kernel.util.InternalErrorException", 1);
        INTERNAL_ERROR_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(internalErrorUnit,
                    "InternalErrorException", CG_CLASS);
        INTERNAL_ERROR_EXCEPTION_TYPE =
            INTERNAL_ERROR_EXCEPTION_DECL.getDefType();


        CompileUnitNode invalidStateUnit =
            StaticResolution
            .loadClassName("ptolemy.kernel.util.InvalidStateException", 1);
        INVALID_STATE_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(invalidStateUnit,
                    "InvalidStateException", CG_CLASS);
        INVALID_STATE_EXCEPTION_TYPE =
            INVALID_STATE_EXCEPTION_DECL.getDefType();


        CompileUnitNode notSchedulableUnit =
            StaticResolution
            .loadClassName("ptolemy.actor.sched.NotSchedulableException", 1);
        NOT_SCHEDULABLE_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(notSchedulableUnit,
                    "NotSchedulableException", CG_CLASS);
        NOT_SCHEDULABLE_EXCEPTION_TYPE =
            NOT_SCHEDULABLE_EXCEPTION_DECL.getDefType();


        CompileUnitNode noRoomUnit =
            StaticResolution
            .loadClassName("ptolemy.actor.NoRoomException", 1);
        NO_ROOM_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(noRoomUnit,
                    "NoRoomException", CG_CLASS);
        NO_ROOM_EXCEPTION_TYPE = NO_ROOM_EXCEPTION_DECL.getDefType();


        CompileUnitNode noTokenUnit =
            StaticResolution
            .loadClassName("ptolemy.actor.NoTokenException", 1);
        NO_TOKEN_EXCEPTION_DECL =
            (ClassDecl) StaticResolution.findDecl(noTokenUnit,
                    "NoTokenException", CG_CLASS);
        NO_TOKEN_EXCEPTION_TYPE = NO_TOKEN_EXCEPTION_DECL.getDefType();


        CompileUnitNode actorUnit =
            StaticResolution.loadClassName("ptolemy.actor.Actor", 1);
        ACTOR_DECL = (ClassDecl) StaticResolution.findDecl(actorUnit,
                "Actor", CG_INTERFACE);
        ACTOR_TYPE = ACTOR_DECL.getDefType();


        CompileUnitNode sequenceActorUnit =
            StaticResolution
            .loadClassName("ptolemy.actor.lib.SequenceActor", 1);
        SEQUENCE_ACTOR_DECL = (ClassDecl) StaticResolution.findDecl(
                sequenceActorUnit,
                "SequenceActor", CG_INTERFACE);
        SEQUENCE_ACTOR_TYPE = SEQUENCE_ACTOR_DECL.getDefType();


        _KNOWN_CLASS_DECLS = new ClassDecl[] {
            COMPLEX_DECL, FIX_POINT_DECL,
                TYPED_ATOMIC_ACTOR_DECL,
                TOKEN_DECL, BOOLEAN_TOKEN_DECL, SCALAR_TOKEN_DECL,
                INT_TOKEN_DECL, DOUBLE_TOKEN_DECL, LONG_TOKEN_DECL,
                COMPLEX_TOKEN_DECL, FIX_TOKEN_DECL, OBJECT_TOKEN_DECL,
                STRING_TOKEN_DECL,
	        ARRAY_TOKEN_DECL, INT_ARRAY_TOKEN_DECL,
	        DOUBLE_ARRAY_TOKEN_DECL,
                MATRIX_TOKEN_DECL,
                BOOLEAN_MATRIX_TOKEN_DECL, INT_MATRIX_TOKEN_DECL,
                DOUBLE_MATRIX_TOKEN_DECL, LONG_MATRIX_TOKEN_DECL,
                COMPLEX_MATRIX_TOKEN_DECL, FIX_MATRIX_TOKEN_DECL,
                DUMMY_LOWER_BOUND,
                PARAMETER_DECL,
                STRINGATTRIBUTE_DECL,
                TYPED_IO_PORT_DECL,
		ILLEGAL_ACTION_EXCEPTION_DECL,
                KERNEL_EXCEPTION_DECL, NAME_DUPLICATION_EXCEPTION_DECL,
                NO_SUCH_ITEM_EXCEPTION_DECL, TYPE_CONFLICT_EXCEPTION_DECL,
                INTERNAL_ERROR_EXCEPTION_DECL, INVALID_STATE_EXCEPTION_DECL,
                NOT_SCHEDULABLE_EXCEPTION_DECL, NO_ROOM_EXCEPTION_DECL,
                NO_TOKEN_EXCEPTION_DECL,
                ACTOR_DECL,
                SEQUENCE_ACTOR_DECL
                };

        _KNOWN_TYPENAMENODES = new TypeNameNode[] {
            COMPLEX_TYPE, FIX_POINT_TYPE,
                TYPED_ATOMIC_ACTOR_TYPE,
                TOKEN_TYPE, BOOLEAN_TOKEN_TYPE, SCALAR_TOKEN_TYPE,
                INT_TOKEN_TYPE, DOUBLE_TOKEN_TYPE, LONG_TOKEN_TYPE,
                COMPLEX_TOKEN_TYPE, FIX_TOKEN_TYPE, OBJECT_TOKEN_TYPE,
                STRING_TOKEN_TYPE,
       	        ARRAY_TOKEN_TYPE, INT_ARRAY_TOKEN_TYPE,
	        DOUBLE_ARRAY_TOKEN_TYPE,
                MATRIX_TOKEN_TYPE,
                BOOLEAN_MATRIX_TOKEN_TYPE, INT_MATRIX_TOKEN_TYPE,
                DOUBLE_MATRIX_TOKEN_TYPE, LONG_MATRIX_TOKEN_TYPE,
                COMPLEX_MATRIX_TOKEN_TYPE, FIX_MATRIX_TOKEN_TYPE,
                DUMMY_LOWER_BOUND_TYPE,
                PARAMETER_TYPE,
                STRINGATTRIBUTE_TYPE,
                TYPED_IO_PORT_TYPE,
		ILLEGAL_ACTION_EXCEPTION_TYPE,
                KERNEL_EXCEPTION_TYPE, NAME_DUPLICATION_EXCEPTION_TYPE,
                NO_SUCH_ITEM_EXCEPTION_TYPE, TYPE_CONFLICT_EXCEPTION_TYPE,
                INTERNAL_ERROR_EXCEPTION_TYPE, INVALID_STATE_EXCEPTION_TYPE,
                NOT_SCHEDULABLE_EXCEPTION_TYPE, NO_ROOM_EXCEPTION_TYPE,
                NO_TOKEN_EXCEPTION_TYPE,
                ACTOR_TYPE,
                SEQUENCE_ACTOR_TYPE
                };

        _TOKEN_CONTAINED_TYPES = new TypeNode[] {
            // the first and third entries are hacks to allow for
            // unresolved token types
            IntTypeNode.instance, BoolTypeNode.instance, IntTypeNode.instance,
                IntTypeNode.instance, DoubleTypeNode.instance,
                LongTypeNode.instance,
                COMPLEX_TYPE, FIX_POINT_TYPE, StaticResolution.OBJECT_TYPE,
                StaticResolution.STRING_TYPE,
	        // Hack for ArrayToken
                TypeUtility.makeArrayType(IntTypeNode.instance, 1),
                // IntArray
                TypeUtility.makeArrayType(IntTypeNode.instance, 1),
	        // DoubleArray
                TypeUtility.makeArrayType(DoubleTypeNode.instance, 1),
                // Hack for MatrixToken
                TypeUtility.makeArrayType(IntTypeNode.instance, 2),
                TypeUtility.makeArrayType(BoolTypeNode.instance, 2),
                TypeUtility.makeArrayType(IntTypeNode.instance, 2),
                TypeUtility.makeArrayType(DoubleTypeNode.instance, 2),
                TypeUtility.makeArrayType(LongTypeNode.instance, 2),
                TypeUtility.makeArrayType(COMPLEX_TYPE, 2),
                TypeUtility.makeArrayType(FIX_POINT_TYPE, 2),
                IntTypeNode.instance // hack for DummyToken
                };

	// OK, do some sanity checks on array lengths.

        if (_debug) {
            System.out.println("PtolemyTypeIdentifier.<static>: "
                    + "\n _KNOWN_KINDS.length: "
                    + _KNOWN_KINDS.length
                    + "\n _KNOWN_TOKEN_TYPES.length: "
                    + _KNOWN_TOKEN_TYPES.length
                    + "\n _IS_CONCRETE_TOKEN.length: "
                    + _IS_CONCRETE_TOKEN.length
                    + "\n _MATRIX_ELEMENT_TOKEN_KINDS.length: "
                    + _MATRIX_ELEMENT_TOKEN_KINDS.length
                    + "\n _KNOWN_CLASS_DECLS.length: "
                    + _KNOWN_CLASS_DECLS.length
                    + "\n _KNOWN_TYPENAMENODES.length: "
                    + _KNOWN_TYPENAMENODES.length
                    + "\n _TOKEN_CONTAINED_TYPES.length: "
                    + _TOKEN_CONTAINED_TYPES.length
                               );
        }
	if ( _KNOWN_KINDS.length != _KNOWN_CLASS_DECLS.length ) {
	    throw new
		RuntimeException("PtolemyTypeIdentifier.<static>"
				 + " _KNOWN_KINDS.length '"
				 + _KNOWN_KINDS.length
				 + "' != _KNOWN_CLASS_DECLS.length '"
				 + _KNOWN_CLASS_DECLS.length + "'");
	}

	if ( _KNOWN_KINDS.length != _KNOWN_TYPENAMENODES.length ) {
	    throw new
		RuntimeException("PtolemyTypeIdentifier.<static>"
				 + " _KNOWN_KINDS.length '"
				 + _KNOWN_KINDS.length
				 + "' != _KNOWN_TYPENAMENODES.length '"
				 + _KNOWN_TYPENAMENODES.length + "'");
	}

	if ( _KNOWN_TOKEN_TYPES.length != _IS_CONCRETE_TOKEN.length ) {
	    throw new
		RuntimeException("PtolemyTypeIdentifier.<static>"
				 + " _KNOWN_TOKEN_TYPES.length '"
				 + _KNOWN_TOKEN_TYPES.length
				 + "' != _IS_CONCRETE_TOKEN.length '"
				 + _IS_CONCRETE_TOKEN.length + "'");
	}

	if ( _KNOWN_TOKEN_TYPES.length != _TOKEN_CONTAINED_TYPES.length ) {
	    throw new
		RuntimeException("PtolemyTypeIdentifier.<static>"
				 + " _KNOWN_TOKEN_TYPES.length '"
				 + _KNOWN_TOKEN_TYPES.length
				 + "' != _TOKEN_CONTAINED_TYPES.length '"
				 + _TOKEN_CONTAINED_TYPES.length + "'");
	}

        System.out.println("PtolemyTypeIdentifier<static>: end");
    }
}
