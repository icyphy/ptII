/*          
An object that encapsulates the type policy for Extended Java.

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

import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** An object that encapsulates the type policy for Extended Java.
 *
 *  @author Jeff Tsay
 */ 
public class ExtendedJavaTypePolicy extends TypePolicy {
    public ExtendedJavaTypePolicy() {
        super(new ExtendedJavaTypeIdentifier());
    }

    public ExtendedJavaTypePolicy(TypeIdentifier typeID) {
        super(typeID);
    }
    
    public TypeNode arithPromoteType(final TypeNode type1, final TypeNode type2) {
        int kind1 = _typeID.kind(type1);
        int kind2 = _typeID.kind(type2);

        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX)) {
           return TypeUtility.makeArrayType((TypeNode) 
            ExtendedJavaTypeIdentifier.FIX_POINT_TYPE.clone(), 2);
        }
 
        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX)) {
           return TypeUtility.makeArrayType((TypeNode) 
            ExtendedJavaTypeIdentifier.COMPLEX_TYPE.clone(), 2);
        }
  
        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX)) {
           return TypeUtility.makeArrayType(DoubleTypeNode.instance, 2);
        }

        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX)) {
           return TypeUtility.makeArrayType(LongTypeNode.instance, 2);
        }

        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX)) {
           return TypeUtility.makeArrayType(IntTypeNode.instance, 2);
        }
        
        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX)) {
           return TypeUtility.makeArrayType(BoolTypeNode.instance, 2);
        }

        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT)) {
           return (TypeNode) ExtendedJavaTypeIdentifier.FIX_POINT_TYPE.clone();
        }
                
        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX)) {
           return (TypeNode) ExtendedJavaTypeIdentifier.COMPLEX_TYPE.clone();
        }

        return super.arithPromoteType(type1, type2);        
    }          
}
