/*          
An object that encapsulates a type policy in Ptolemy.

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

package ptolemy.codegen;

import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** An object that encapsulates a type policy in Ptolemy.
 *
 *  @author Jeff Tsay
 */ 
public class PtolemyTypePolicy extends TypePolicy {

    public PtolemyTypePolicy() {
        this(new PtolemyTypeIdentifier());
    }
    
    public PtolemyTypePolicy(PtolemyTypeIdentifier ptTypeID) {
        super(ptTypeID);
        _ptTypeID = ptTypeID;
    }
        
    public boolean isSubClassOfSupportedActor(TypeNode type) {
        if (type.classID() != TYPENAMENODE_ID) {
           return false;
        }
        
        ClassDecl decl = (ClassDecl) JavaDecl.getDecl((NamedNode) type);
        
        return isSubClassOfSupportedActor(decl);    
    }

    public boolean isSubClassOfSupportedActor(ClassDecl decl) {

        while (true) {
           if ((decl == StaticResolution.OBJECT_DECL) || (decl == null)) {
              return false;
           }
                
           int kind = _ptTypeID.kindOfClassDecl(decl);
           
           if (_ptTypeID.isSupportedActorKind(kind)) {
              return true;
           }
  
           decl = decl.getSuperClass();
        }
    }    

    /** Return the kind of the token that is more general (the other kind should
     *  be convertible to the returned kind). The kinds must both be those of concrete 
     *  Token subclasses. 
     */
    public int moreGeneralTokenKind(int kind1, int kind2) {
        // handle only half the symmetric cases
        if (kind2 < kind1) return moreGeneralTokenKind(kind2, kind1);
        
        if (kind1 == kind2) return kind1;
        
        // kind1 < kind2
        switch (kind1) {
                              
          case PtolemyTypeIdentifier.TYPE_KIND_INT_TOKEN:          
          if ((kind2 != PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN) && 
              (kind2 != PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN) &&
              (kind2 != PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN)) {
             ApplicationUtility.error("moreSpecificTokenKind() : kind1 = IntToken, kind2 = " + 
              kind2);          
          }
          return kind2;
              
          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN:          
          if (kind2 != PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN) {
             ApplicationUtility.error("moreSpecificTokenKind() : kind1 = DoubleToken, kind2 = " + 
              kind2);          
          }
          return PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN;
          
          case PtolemyTypeIdentifier.TYPE_KIND_INT_MATRIX_TOKEN:          
          if ((kind2 != PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN) && 
              (kind2 != PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN) &&
              (kind2 != PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN)) {
             ApplicationUtility.error("moreSpecificTokenKind() : kind1 = IntMatrixToken, kind2 = " + 
              kind2);          
          }
          return kind2;
              
          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN:          
          if (kind2 != PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN) {
             ApplicationUtility.error("moreGeneralTokenKind() : kind1 = DoubleMatrixToken, kind2 = " + 
              kind2);          
          }
          return PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN;

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_TOKEN: 
          case PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN:          
          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN:          
          case PtolemyTypeIdentifier.TYPE_KIND_FIX_TOKEN:                    
          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX_TOKEN: 
          case PtolemyTypeIdentifier.TYPE_KIND_LONG_MATRIX_TOKEN:          
          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN:          
          case PtolemyTypeIdentifier.TYPE_KIND_FIX_MATRIX_TOKEN:                              
          if (kind2 != PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN) {
             ApplicationUtility.error("moreGeneralTokenKind() : kind1 = " + kind1 + 
              " kind2 = " + kind2);                                                                                          
          }
          return PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN;
          
          // abstract types
          case PtolemyTypeIdentifier.TYPE_KIND_TOKEN: 
          case PtolemyTypeIdentifier.TYPE_KIND_SCALAR_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_MATRIX_TOKEN:

          // types that are already as general as possible
          case PtolemyTypeIdentifier.TYPE_KIND_OBJECT_TOKEN:                    
          case PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN: // change this when we change the type lattice                                                   
          ApplicationUtility.error("moreGeneralTokenKind() : kind1 = " + kind1 + 
           " kind2 = " + kind2);                                                                                          
           
          default:
          ApplicationUtility.error("moreGeneralTokenKind() : kind unknown :  " + kind1);
        }            
        return TypeIdentifier.TYPE_KIND_UNKNOWN;
    }
    
    protected final PtolemyTypeIdentifier _ptTypeID;              
}
