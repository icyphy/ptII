/*
A class that identifies special types in Extended Java.

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

package ptolemy.lang.java.extended;

import ptolemy.codegen.PtolemyTypeIdentifier;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;


/** A class that identifies special types in Extended Java.
 *
 *  @author Jeff Tsay
 */
public class ExtendedJavaTypeIdentifier extends TypeIdentifier {

    public ExtendedJavaTypeIdentifier() {}

    public boolean isClassKind(int kind) {
        return (((kind >= TYPE_KIND_COMPLEX) &&
                 (kind <= TYPE_KIND_FIX_POINT_MATRIX)) ||
                super.isClassKind(kind));
    }

    /** Return the kind (an integer) of the type. If the type node is a TypeNameNode,
     *  return kindOfTypeNameNode(type).
     */
    public int kind(TypeNode type) {

       if (type.classID() == ARRAYTYPENODE_ID) {
          int arrayDim = TypeUtility.arrayDimension(type);

          if (arrayDim == 2) {
             TypeNode baseTypeNode = TypeUtility.arrayBaseType(type);

             switch (kind(baseTypeNode)) {
               case TYPE_KIND_BOOLEAN:   return TYPE_KIND_BOOLEAN_MATRIX;
               case TYPE_KIND_INT:       return TYPE_KIND_INT_MATRIX;
               case TYPE_KIND_LONG:      return TYPE_KIND_LONG_MATRIX;
               case TYPE_KIND_DOUBLE:    return TYPE_KIND_DOUBLE_MATRIX;
               case TYPE_KIND_COMPLEX:   return TYPE_KIND_COMPLEX_MATRIX;
               case TYPE_KIND_FIX_POINT: return TYPE_KIND_FIX_POINT_MATRIX;

               // no default here
             }
          }
       }

       return super.kind(type);
    }

    /** Return an integer representing the user type that has the specified ClassDecl,
     *  which may be a special type in Ptolemy. If the type is not a special type,
     *  return the integer given by super.kindOfClassDecl(classDecl).
     */
    public int kindOfClassDecl(ClassDecl classDecl) {
        if (classDecl == PtolemyTypeIdentifier.COMPLEX_DECL) {
           return TYPE_KIND_COMPLEX;
        } else if (classDecl == PtolemyTypeIdentifier.FIX_POINT_DECL) {
           return TYPE_KIND_FIX_POINT;
        } else if (classDecl == StaticResolution.STRING_DECL) {
           return TYPE_KIND_STRING;
        } else if (classDecl == TOKEN_DECL) {
           return TYPE_KIND_TOKEN;
        } else {
           return super.kindOfClassDecl(classDecl);
        }
    }

    // Ptolemy math kinds

    public static final int TYPE_KIND_COMPLEX
     = PtolemyTypeIdentifier.TYPE_KIND_COMPLEX;
    public static final int TYPE_KIND_FIX_POINT = TYPE_KIND_COMPLEX + 1;

    // String kind
    public static final int TYPE_KIND_STRING = TYPE_KIND_FIX_POINT + 1;

    // Token kind (used for unresolved tokens)
    public static final int TYPE_KIND_TOKEN = TYPE_KIND_STRING + 1;

    // matrix kinds

    public static final int TYPE_KIND_BOOLEAN_MATRIX =
     TYPE_KIND_TOKEN + 1;
    public static final int TYPE_KIND_INT_MATRIX =
     TYPE_KIND_BOOLEAN_MATRIX + 1;
    public static final int TYPE_KIND_LONG_MATRIX =
     TYPE_KIND_INT_MATRIX + 1;
    public static final int TYPE_KIND_DOUBLE_MATRIX =
     TYPE_KIND_LONG_MATRIX + 1;
    public static final int TYPE_KIND_COMPLEX_MATRIX =
     TYPE_KIND_DOUBLE_MATRIX + 1;
    public static final int TYPE_KIND_FIX_POINT_MATRIX =
     TYPE_KIND_COMPLEX_MATRIX + 1;

    // Token type (not currently used)
    public static final ClassDecl TOKEN_DECL =
     PtolemyTypeIdentifier.TOKEN_DECL;
    public static final TypeNameNode TOKEN_TYPE =
     PtolemyTypeIdentifier.TOKEN_TYPE;

    // Ptolemy math types

    public static final ClassDecl COMPLEX_DECL =
     PtolemyTypeIdentifier.COMPLEX_DECL;
    public static final TypeNameNode COMPLEX_TYPE =
     PtolemyTypeIdentifier.COMPLEX_TYPE;

    public static final ClassDecl FIX_POINT_DECL =
     PtolemyTypeIdentifier.FIX_POINT_DECL;
    public static final TypeNameNode FIX_POINT_TYPE =
     PtolemyTypeIdentifier.FIX_POINT_TYPE;
}
