/*          
An object that encapsulates a type policy.

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

/** An object that encapsulates a type policy.
 *
 *  @author Jeff Tsay
 */ 
public class TypePolicy implements JavaStaticSemanticConstants {

    public TypePolicy() {
        this(new TypeIdentifier());
    }
    
    public TypePolicy(TypeIdentifier typeID) {
        _typeID = typeID;
    }
        
    /** Return true if TypeNodes t1 and t2 are identical. */
    public boolean compareTypes(TypeNode t1, TypeNode t2) {
        if (t1 == t2) {  // primitive types, or reference to same type node
                         // this relies on the fact that the primitive
                         // types are singletons
           return true;
        }

        if (isArrayType(t1)) {
           if (!isArrayType(t2)) {
              return false;
           }

           ArrayTypeNode at1 = (ArrayTypeNode) t1;
           ArrayTypeNode at2 = (ArrayTypeNode) t2;

           return compareTypes(at1.getBaseType(), at2.getBaseType());
        }

        // t1 and t2 must now both be TypeNameNodes if they are to be equal
        TypeNameNode tn1 = null;
        TypeNameNode tn2 = null;
        if (t1.classID() == TYPENAMENODE_ID) {
           tn1 = (TypeNameNode) t1;
        } else {
           return false;
        }

        if (t2.classID() == TYPENAMENODE_ID) {
           tn2 = (TypeNameNode) t2;
        } else {
           return false;
        }
        return compareTypeNames(tn1, tn2);
    }

    /** Return true if TypeNameNodes tn1 and tn2 are identical. */
    public boolean compareTypeNames(TypeNameNode tn1, TypeNameNode tn2) {
        return (JavaDecl.getDecl((NamedNode) tn1) == JavaDecl.getDecl((NamedNode) tn2));
    }
        
    /** Return true if two methods conflict, i.e. they have the same
     *  parameter types).
     */
    public boolean doMethodsConflict(MethodDecl decl1, MethodDecl decl2) {
        Iterator myParamTypesItr =  decl1.getParams().iterator();
        Iterator argParamTypesItr = decl2.getParams().iterator();

        // Search for different parameter types. If a different type is
        // found, the methods do not conflict.
        while (myParamTypesItr.hasNext() && argParamTypesItr.hasNext()) {
           TypeNode myParamType = (TypeNode) myParamTypesItr.next();
           TypeNode argParamType = (TypeNode) argParamTypesItr.next();
           if (!compareTypes(myParamType, argParamType)) {
              return false;
           }
        }

        // If there are any more parameters leftover, the two methods
        // do not conflict. Otherwise, they do conflict.
        return !(myParamTypesItr.hasNext() || argParamTypesItr.hasNext());
    }
        
    /** Return true iff the class corresponding to classDecl implements the
     *  interface corresponding to iFaceDecl.
     */
    public boolean doesImplement(ClassDecl classDecl, ClassDecl iFaceDecl) {
        Iterator iFaceItr = classDecl.getInterfaces().iterator();

        while (iFaceItr.hasNext()) {
           ClassDecl implIFace = (ClassDecl) iFaceItr.next();
           if (isSuperInterface(iFaceDecl, implIFace)) {
              return true;
           }
        }

        if (classDecl == StaticResolution.OBJECT_DECL) {
           return false;
        } else {
           return doesImplement(classDecl.getSuperClass(), iFaceDecl);
        }      
    }

    /** Return true iff type is a arithmetic type. */         
    public boolean isArithType(TypeNode type)  {     
       return _isOneOf(type, _ARITH_TYPES);
    }

    /** Return true iff type is an array type. */     
    public boolean isArrayType(TypeNode type) {
        return (type.classID() == ARRAYTYPENODE_ID);
    }

    public boolean isAssignableFromConstant(TypeNode type, ExprNode expr) {    
       switch (_typeID.kind(type)) {
         case TypeIdentifier.TYPE_KIND_BYTE:
         return ExprUtility.isIntConstant(expr, Byte.MIN_VALUE, Byte.MAX_VALUE);
              
         case TypeIdentifier.TYPE_KIND_CHAR:
         return ExprUtility.isIntConstant(expr, Character.MIN_VALUE, Character.MAX_VALUE);
         
         case TypeIdentifier.TYPE_KIND_SHORT:
         return ExprUtility.isIntConstant(expr, Short.MIN_VALUE, Short.MAX_VALUE);       
         
         // not in Titanium ..
         case TypeIdentifier.TYPE_KIND_INT:
         return ExprUtility.isIntConstant(expr, Integer.MIN_VALUE, Integer.MAX_VALUE);                
       }   
       return false; 
    }
    
    public boolean isAssignableFromType(final TypeNode type1, 
     final TypeNode type2)  {

       int kind1 = _typeID.kind(type1);
       int kind2 = _typeID.kind(type2);

       if (isPrimitiveType(type1)) {
          if (isPrimitiveType(type2)) {       
             // table driven for 2 primitive types
             return _isOneOf(type1, _TYPES_ASSIGNABLE_TO[kind2]);
          } else {
             // type1 is primitive type, type2 is user type
             return false;
          }
       } else if (isPrimitiveType(type2)) {
          // type1 is user type, type2 is primitive
          return false;
       }

       if (kind1 == TypeIdentifier.TYPE_KIND_NULL) {
          return false;
       } else if (_typeID.isClassKind(kind1)) {
          if (kind2 == TypeIdentifier.TYPE_KIND_NULL) {
             return true;
          } else if (kind2 == TypeIdentifier.TYPE_KIND_ARRAYINIT) {
             return isArrayType(type1);
          } else if (_typeID.isInterfaceKind(kind2)) {
             JavaDecl decl = JavaDecl.getDecl((NamedNode) type1);           

             return (decl == StaticResolution.OBJECT_DECL);         
          } else if (_typeID.isClassKind(kind2)) {
             if (isSubClass(type2, type1)) {
                return true;
             }

             if (isArrayType(type1) && isArrayType(type2)) {
                ArrayTypeNode arrType1 = (ArrayTypeNode) type1;
                ArrayTypeNode arrType2 = (ArrayTypeNode) type2;

                TypeNode elementType1 = arrType1.getBaseType();     
                TypeNode elementType2 = arrType2.getBaseType();     

                return isAssignableFromType(elementType1, elementType2);
             }  
             return false;
          }
          return false;
       } else if (_typeID.isInterfaceKind(kind1)) {
          if (kind2 == TypeIdentifier.TYPE_KIND_NULL) {
             return true;
          } else if (_typeID.isClassKind(kind2)) {             
             ClassDecl decl1 = (ClassDecl) JavaDecl.getDecl(type1);           
             ClassDecl decl2 = (ClassDecl) JavaDecl.getDecl(type2);           
             return doesImplement(decl2, decl1);
          } else if (_typeID.isInterfaceKind(kind2)) {             
             ClassDecl decl1 = (ClassDecl) JavaDecl.getDecl(type1);           
             ClassDecl decl2 = (ClassDecl) JavaDecl.getDecl(type2);
             return isSuperInterface(decl1, decl2); 
          }
          return false;
       }
       
       // type1 is class o
       return false;
    }   

    /** Return true iff type is a floating point type. */             
    public boolean isFloatType(TypeNode type)  {
       return _isOneOf(type, _FLOAT_TYPES); 
    }

    /** Return true iff type is a discrete valued type. */         
    public boolean isIntegralType(TypeNode type) {
       return _isOneOf(type, _INTEGRAL_TYPES);        
    }
    
    /** Return true iff type is a primitive type. */
    public boolean isPrimitiveType(TypeNode type)  {
        return (type instanceof PrimitiveTypeNode);
    }

    /** Return true iff type is a reference type. */     
    public boolean isReferenceType(TypeNode type)  {
        return ((type == NullTypeNode.instance) || (type instanceof TypeNameNode));
    }


    /** Return true iff type is a String type. */         
    public boolean isStringType(TypeNode type) { 
       return compareTypes(type, StaticResolution.STRING_TYPE);
    }     
                          
    /** Return true iff type1 is the same class as or a subclass of type2. 
     *  In particular, return true if type1 is an ArrayTypeNode and
     *  type2 is the TypeNameNode for Object.
     */
    public boolean isSubClass(TypeNode type1, TypeNode type2) {
         
        if (type2.classID() != TYPENAMENODE_ID) {
           return false;       
        }
    
        ClassDecl decl2 = (ClassDecl) JavaDecl.getDecl((NamedNode) type2);
       
        // arrays are subclasses of Object
        if ((decl2 == StaticResolution.OBJECT_DECL) && 
            (type1.classID() == ARRAYTYPENODE_ID)) {
           return true;
        } 
  
        if (type1.classID() != TYPENAMENODE_ID) {
           return false;
        }   
       
        ClassDecl decl1 = (ClassDecl) JavaDecl.getDecl((NamedNode) type1);
    
        if ((decl1.category != CG_CLASS) || (decl2.category != CG_CLASS)) {
           return false;
        }
                    
        return isSubClass(decl1, decl2);
    }

    /** Return true iff decl1 corresponds to a class that is the same or
     *  a subclass of the class correspoinding to decl2.
     */
    public boolean isSubClass(ClassDecl decl1, ClassDecl decl2) {
        while (true) {
           if (decl1 == decl2) {
              return true;
           }
 
           if ((decl1 == StaticResolution.OBJECT_DECL) || (decl1 == null)) {
              return false;
           }
 
           decl1 = decl1.getSuperClass();
        }
    }                  
       
    public boolean isSuperInterface(ClassDecl decl1, ClassDecl decl2) {   
        if (decl1 == decl2) {
           return true;
        }

        Iterator iFaceItr = decl2.getInterfaces().iterator();

        while (iFaceItr.hasNext()) {
           ClassDecl implIFace = (ClassDecl) iFaceItr.next();
           if (isSuperInterface(decl1, implIFace)) {
              return true;
           }
        }
        return false;
    }
    
    public TypeIdentifier typeIdentifier() {
        return _typeID;
    }
        
    /** Return true if type is one of the types contained in typeArray. The
     *  comparison is made between references only, so this only works for
     *  primitive types (which are singletons).
     */
    protected static final boolean _isOneOf(TypeNode type, TypeNode[] typeArray) {
        for (int i = 0; i < typeArray.length; i++) {
            if (typeArray[i] == type) {
               return true;
            }
        }
        return false;          
    }    
    
    protected final TypeIdentifier _typeID;
        
    protected static final TypeNode[] _ARITH_TYPES = new TypeNode[] 
     { ByteTypeNode.instance, ShortTypeNode.instance, CharTypeNode.instance,
       IntTypeNode.instance, LongTypeNode.instance, FloatTypeNode.instance,
       DoubleTypeNode.instance };

    protected static final TypeNode[] _FLOAT_TYPES = new TypeNode[] 
     { FloatTypeNode.instance, DoubleTypeNode.instance };

    protected static final TypeNode[] _INTEGRAL_TYPES = new TypeNode[]
     { ByteTypeNode.instance, ShortTypeNode.instance, CharTypeNode.instance,
       IntTypeNode.instance, LongTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_BOOL = new TypeNode[] 
     { BoolTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_BYTE = new TypeNode[] 
     { ByteTypeNode.instance, ShortTypeNode.instance, CharTypeNode.instance, 
       IntTypeNode.instance, LongTypeNode.instance, FloatTypeNode.instance, 
       DoubleTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_CHAR = new TypeNode[] 
     { ShortTypeNode.instance, CharTypeNode.instance, IntTypeNode.instance, 
       LongTypeNode.instance, FloatTypeNode.instance, DoubleTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_SHORT = new TypeNode[] 
     { ShortTypeNode.instance, IntTypeNode.instance, LongTypeNode.instance, 
       FloatTypeNode.instance, DoubleTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_INT = new TypeNode[] 
     { IntTypeNode.instance, LongTypeNode.instance, FloatTypeNode.instance, 
       DoubleTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_LONG = new TypeNode[] 
     { LongTypeNode.instance, FloatTypeNode.instance, DoubleTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_FLOAT = new TypeNode[] 
     { FloatTypeNode.instance, DoubleTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_DOUBLE = new TypeNode[] 
     { DoubleTypeNode.instance };

    /** An uneven matrix of primitive types that may be assigned to a 
     *  primitive type, the kind of which is the first array index.
     */
    protected static final TypeNode[][] _TYPES_ASSIGNABLE_TO = new TypeNode[][] 
     { 
       _TYPES_ASSIGNABLE_TO_BOOL, _TYPES_ASSIGNABLE_TO_BYTE, _TYPES_ASSIGNABLE_TO_SHORT, 
       _TYPES_ASSIGNABLE_TO_CHAR, _TYPES_ASSIGNABLE_TO_INT, _TYPES_ASSIGNABLE_TO_LONG, 
       _TYPES_ASSIGNABLE_TO_FLOAT, _TYPES_ASSIGNABLE_TO_DOUBLE
     };                  
}
    
