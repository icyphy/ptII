/*
A class that identifies special types.

Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.lang.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A class that identifies special types.
 *
 *  This class is intended to be overridden to implement different
 *  typing policies.
 *
 *  @author Jeff Tsay
 */
public class TypeIdentifier implements JavaStaticSemanticConstants {

    public TypeIdentifier() {}

    /** Return true iff the kind is a class kind. In derived classes, the
     *  kind() may return a different number for special classes, so this
     *  method checks if the kind is any class kind.
     */
    public boolean isClassKind(int kind) {
        return (kind == TYPE_KIND_CLASS);
    }

    /** Return true iff the kind is a interface kind. In derived classes, the
     *  kind() may return a different number for special interfaces, so this
     *  method checks if the kind is any interface kind.
     */
    public boolean isInterfaceKind(int kind) {
        return (kind == TYPE_KIND_INTERFACE);
    }


    /** Return the kind (an integer) of the type. If the type node is a TypeNameNode,
     *  return kindOfTypeNameNode(type).
     */
    public int kind(TypeNode type) {

       switch (type.classID()) {
         // null type
         case NULLTYPENODE_ID:      return TYPE_KIND_NULL;

          // primitive types
         case BOOLTYPENODE_ID:      return TYPE_KIND_BOOLEAN;
         case CHARTYPENODE_ID:      return TYPE_KIND_CHAR;
         case BYTETYPENODE_ID:      return TYPE_KIND_BYTE;
         case SHORTTYPENODE_ID:     return TYPE_KIND_SHORT;
         case INTTYPENODE_ID:       return TYPE_KIND_INT;
         case LONGTYPENODE_ID:      return TYPE_KIND_LONG;
         case FLOATTYPENODE_ID:     return TYPE_KIND_FLOAT;
         case DOUBLETYPENODE_ID:    return TYPE_KIND_DOUBLE;

         // class or interface
         case TYPENAMENODE_ID:      return kindOfTypeNameNode((TypeNameNode) type);

         // array initializer (not used in the static semantic analysis)
         case ARRAYINITTYPENODE_ID: return TYPE_KIND_ARRAYINIT;

         // array types (derive from Object)
         case ARRAYTYPENODE_ID:     return TYPE_KIND_CLASS;

         // void type
         case VOIDTYPENODE_ID:      return TYPE_KIND_VOID;
       }

       ApplicationUtility.error("unknown type encountered : " + type);
       return TYPE_KIND_UNKNOWN;
    }

    /** Return the kind of the user type, either a class type or an interface type.
     *  This method should be called in kind() for TypeNameNodes.
     */
    public int kindOfTypeNameNode(TypeNameNode type) {
        return kindOfClassDecl((ClassDecl) JavaDecl.getDecl((NamedNode) type));
    }

    public int kindOfClassDecl(ClassDecl classDecl) {
       if (classDecl != null) {
          if (classDecl.category == CG_INTERFACE) {
             return TYPE_KIND_INTERFACE;
          }
       }
       return TYPE_KIND_CLASS;
    }

    /** Return the primitive type corresponding to the argument kind. */
    public TypeNode primitiveKindToType(int kind) {
        if (kind < 0) {
           ApplicationUtility.error("unknown type is not primitive");
        }

        if (kind > NUM_PRIMITIVE_TYPES) {
           ApplicationUtility.error("type is not primitive");
        }

        return _PRIMITIVE_KIND_TO_TYPE[kind];
    }

    // kinds of types, a mapping from types to integers

    public static final int TYPE_KIND_UNKNOWN = -1;

    // primitive types

    public static final int TYPE_KIND_BOOLEAN = 0; // first primitive type should start at 0
    public static final int TYPE_KIND_BYTE    = 1;
    public static final int TYPE_KIND_SHORT   = 2;
    public static final int TYPE_KIND_CHAR    = 3;
    public static final int TYPE_KIND_INT     = 4;
    public static final int TYPE_KIND_LONG    = 5;
    public static final int TYPE_KIND_FLOAT   = 6;
    public static final int TYPE_KIND_DOUBLE  = 7;

    /** The number of primitive types in Java. */
    public static final int NUM_PRIMITIVE_TYPES = TYPE_KIND_DOUBLE + 1;

    // user defined types

    public static final int TYPE_KIND_CLASS   = 8;
    public static final int TYPE_KIND_INTERFACE = 9;

    /** The kind for an array initializer expression { 0, 1, .. }. */
    public static final int TYPE_KIND_ARRAYINIT = 10;

    /** The kind of NULL. */
    public static final int TYPE_KIND_NULL    = 11;

    /** The void type (for return types). */
    public static final int TYPE_KIND_VOID    = 12;

    public static final int TYPE_KINDS = TYPE_KIND_VOID + 1;

    /** An array, indexed by kind, of the primitive type corresponding to the kind. */
    protected static final TypeNode[] _PRIMITIVE_KIND_TO_TYPE = new TypeNode[]
     { BoolTypeNode.instance, ByteTypeNode.instance, ShortTypeNode.instance,
       CharTypeNode.instance, IntTypeNode.instance, LongTypeNode.instance,
       FloatTypeNode.instance, DoubleTypeNode.instance };
