/* 
Methods dealing with types. Most of the code and comments were taken from the 
Titanium project.

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

public class TypeUtility {

    /** Private constructor prevents instantiation of this class. */
    private TypeUtility() {}

    /** Return true if TypeNodes t1 and t2 are identical. */
    public static final boolean compareTypes(TypeNode t1, TypeNode t2) {
        if (t1 == t2) {  // primitive types, or reference to same type node
                         // this relies on the fact that the primitive
                         // types are singletons
           return true;
        }

        if (t1 instanceof ArrayTypeNode) {
           if (!(t2 instanceof ArrayTypeNode)) {
              return false;
           }
           
           ArrayTypeNode at1 = (ArrayTypeNode) t1;
           ArrayTypeNode at2 = (ArrayTypeNode) t2;

           return compareTypes(at1.getBaseType(), at2.getBaseType());
        }

        // t1 and t2 must now both be TypeNameNodes if they are to be equal
        TypeNameNode tn1 = null;
        TypeNameNode tn2 = null;
        if (t1 instanceof TypeNameNode) {
           tn1 = (TypeNameNode) t1;
        } else {
           return false;
        }

        if (t2 instanceof TypeNameNode) {
           tn2 = (TypeNameNode) t2;
        } else {
           return false;
        }

        return compareTypeNames(tn1, tn2);
    }

    /** Return true if TypeNameNodes tn1 and tn2 are identical. */
    public static final boolean compareTypeNames(TypeNameNode tn1,
     TypeNameNode tn2) {
        return (JavaDecl.getDecl(tn1) == JavaDecl.getDecl(tn2));
    }

    /** Return true iff MethodDecl m1 is more specific 
     *  (in the sense of 14.11.2.2) than MethodDecl m2.  Actually, the right term 
     *  should be "no less specific than", but the Reference Manual is the 
     *  Reference Manual. 
     */
    public static final boolean isMoreSpecific(final MethodDecl m1, final MethodDecl m2) {
     
        LinkedList params1 = m1.getParamList();
        LinkedList params2 = m2.getParamList();
          
        int numParams = params1.size();    
        if (numParams != params2.size()) {         
           return false;
        }
         
        ClassDecl container2 = (ClassDecl) m2.getContainer();
        TypeNameNode classType2 = container2.getDefType(); 
        
        ClassDecl container1 = (ClassDecl) m1.getContainer();
        TypeNameNode classType1 = container1.getDefType(); 
         
        if (!isAssignableFromType(classType2, classType1)) {
           return false;
        }
         
        // This is inefficient ... iterate the list backwards once everything's working
        for (int i = length - 1; i >= 0; i--) {
            TypeNode param2 = (TypeNode) params2.get(i);
            TypeNode param1 = (TypeNode) params1.get(i);
             
            if (!isAssignableFromType(param2, param1) {
               return false;
            }
        }
         
        return true;    
    }

    /** Return true iff type is a primitive type. */
    public static final boolean isReference(TypeNode type)  {
        return (type instanceof PrimitiveTypeNode);
    }

    /** Return true iff type is a reference type. */     
    public static final boolean isReference(TypeNode type)  {
        return ((type == NullTypeNode.instance) || (type instanceof TypeNameNode));
    }
     
    /** Return true iff type is a arithmetic type. */         
    public static final boolean isArithType(TypeNode type)  {     
       return _isOneOf(type, _ARITH_TYPES);
    }

    /** Return true iff type is a floating point type. */             
    public static final boolean isFloatType(TypeNode type)  {
       return _isOneOf(type, _FLOAT_TYPES); 
    }

    /** Return true iff type is a discrete valued type. */         
    public static final boolean isIntegralType(TypeNode type) {
       return _isOneOf(type, _INTEGRAL_TYPES);        
    }

    /** Return true iff type is a String type. */         
    public static boolean isStringType(TypeNode type) { 
       return compareType(type, StaticResolution.STRING_DECL.getDefType());
    }     
    
    boolean isAssignableFromType(final TypeNode type1, final TypeNode type2)  {
       if (isPrimitive(type1)) {
          // table driven for primitive types
          return _isOneOf(type1, _typesAssignableTo(type2));
       }
       
  switch (kind())
    {
    case Common::ClassKind:
      switch (T1->kind())
	{
	case Common::NullKind: return isReference(); // not immutable
	case Common::InterfaceKind:
	  return decl() == ObjectDecl && isLocalWidening(this, T1);
	case Common::ClassKind:
	  return isLocalWidening(this, T1) &&
	    (isSubClass(T1->decl(), decl()) ||
	     isJavaArrayType() && T1->isJavaArrayType() &&
	     elementType()->isAssignableFromType(T1->elementType()) &&
	     areQualifiersEqual(elementType(), T1->elementType()));
	case Common::ArrayInitializerKind: return isArrayType();
	default: return false;
	}
    case Common::InterfaceKind:
      switch (T1->kind())
	{
	case Common::NullKind: return true;
	case Common::ClassKind:
	  return implements(T1->decl(), decl()) && isLocalWidening(this, T1);
	case Common::InterfaceKind:
	  return isSuperInterface(decl(), T1->decl()) && isLocalWidening(this, T1);
	default: return false;
	}
    case Common::NullKind:
      return false;
    default:
      assert(0);
      return false;
    }       
    }   
    
    public static final boolean _isOneOf(TypeNode type, TypeNode[] typeArray) {
       for (int i = 0; i < typeArray.length; i++) {
           if (typeArray[i] == type) {
              return true;
           }
       }
       return false;          
    }
    
    protected static final TypeNode[] _typesAssignableTo(TypeNode type) {
       switch (type) {
       
       case BoolTypeNode.instance:
       return _TYPES_ASSIGNABLE_TO_BOOL;
       
       case CharTypeNode.instance:
       return _TYPES_ASSIGNABLE_TO_CHAR;
       
       case ByteTypeNode.instance:
       return _TYPES_ASSIGNABLE_TO_BYTE;
              
       case ShortTypeNode.instance:       
       return _TYPES_ASSIGNABLE_TO_SHORT;
       
       case IntTypeNode.instance:
       return _TYPES_ASSIGNABLE_TO_INT;
       
       case LongTypeNode.instance:
       return _TYPES_ASSIGNABLE_TO_LONG;
       
       case FloatTypeNode.instance:
       return _TYPES_ASSIGNABLE_TO_FLOAT;
       
       case DoubleTypeNode.instance: 
       return _TYPES_ASSIGNABLE_TO_DOUBLE;
       
       }
            
       
        // more 
    
    }
    
    protected static final TypeNode[] _ARITH_TYPES = new TypeNode[] 
     { ByteTypeNode.instance, CharTypeNode.instance, ShortTypeNode.instance,
       IntTypeNode.instance, LongTypeNode.instance, FloatTypeNode.instance,
       DoubleTypeNode.instance };

    protected static final TypeNode[] _FLOAT_TYPES = new TypeNode[] 
     { FloatTypeNode.instance, DoubleTypeNode.instance };

    protected static final TypeNode[] _INTEGRAL_TYPES = new TypeNode[]
     { ByteTypeNode.instance, CharTypeNode.instance, ShortTypeNode.instance,
       IntTypeNode.instance, LongTypeNode.instance };
       
    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_BOOL = new TypeNode[] 
     { BoolTypeNode.instance };

    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_BYTE = new TypeNode[] 
     { ByteTypeNode.instance, CharTypeNode.instance, ShortTypeNode.instance, 
       IntTypeNode.instance, LongTypeNode.instance, FloatTypeNode.instance, 
       DoubleTypeNode.instance };
       
    protected static final TypeNode[] _TYPES_ASSIGNABLE_TO_CHAR = new TypeNode[] 
     { CharTypeNode.instance, ShortTypeNode.instance, IntTypeNode.instance, 
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
}


