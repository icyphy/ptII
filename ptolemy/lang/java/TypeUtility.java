/* 
Methods dealing with types. Most of the code and comments were taken from the 
Titanium project.

Copyright (c) 1998-2000 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.Iterator;
import ptolemy.lang.*;

public class TypeUtility implements JavaStaticSemanticConstants {

    /** Public constructor allows inheritence of methods although this class has no
     *  instance members.
     */
    public TypeUtility() {}

    /** For nodes that represent field accesses (ObjectFieldAccessNode, 
     *  ThisFieldAccessNode, SuperFieldAccessNode) the type of the 
     *  object that is accessed (e.g., for a node representing FOO.BAR, 
     *  the type of FOO. This method figures out the sub-type of NODE
     *  and calls the appropriate more specific method. 
     */
    public static TypeNameNode accessedObjectType(FieldAccessNode node) {
        if (node instanceof TypeFieldAccessNode) {
           return accessedObjectType((TypeFieldAccessNode) node);
        } else if (node instanceof ObjectFieldAccessNode) {
           return accessedObjectType((ObjectFieldAccessNode) node);
        } else if (node instanceof ThisFieldAccessNode) {
           return accessedObjectType((ThisFieldAccessNode) node);
        } else if (node instanceof SuperFieldAccessNode) {
           return accessedObjectType((SuperFieldAccessNode) node);
        } else {
           ApplicationUtility.error("accessObjectType() not supported for node " + node);
        }
        return null;              
    }

    public static TypeNameNode accessedObjectType(TypeFieldAccessNode node) {
        return JavaDecl.getDecl((NamedNode) node.getFType()).getDefType();
    }

    public static TypeNameNode accessedObjectType(ObjectFieldAccessNode node) {
        return (TypeNameNode) type((ExprNode) node.getObject());        
    }

    public static TypeNameNode accessedObjectType(ThisFieldAccessNode node)  {
        return (TypeNameNode) node.getDefinedProperty(THIS_CLASS_KEY);
    }

    public static TypeNameNode accessedObjectType(SuperFieldAccessNode node) {    
        ClassDecl myClass = (ClassDecl) JavaDecl.getDecl(
         (NamedNode) node.getDefinedProperty(THIS_CLASS_KEY));
        ClassDecl sclass = myClass.getSuperClass();
        
        return sclass.getDefType();
    }

    /** Return true if TypeNodes t1 and t2 are identical. */
    public static boolean compareTypes(TypeNode t1, TypeNode t2) {
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
    public static boolean compareTypeNames(TypeNameNode tn1,
     TypeNameNode tn2) {
        return (JavaDecl.getDecl((NamedNode) tn1) == JavaDecl.getDecl((NamedNode) tn2));
    }

    /** Return true iff MethodDecl m1 is more specific 
     *  (in the sense of 14.11.2.2) than MethodDecl m2.  Actually, the right term 
     *  should be "no less specific than", but the Reference Manual is the 
     *  Reference Manual. 
     */
    public static boolean isMoreSpecific(final MethodDecl m1, final MethodDecl m2) {

        LinkedList params1 = m1.getParams();
        LinkedList params2 = m2.getParams();

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
        // Actually, it doesn't seem to matter what direction we go...
        for (int i = numParams - 1; i >= 0; i--) {
            TypeNode param2 = (TypeNode) params2.get(i);
            TypeNode param1 = (TypeNode) params1.get(i);

            if (!isAssignableFromType(param2, param1)) {
               return false;
            }
        }

        return true;    
    }

    public static boolean isCallableWith(MethodDecl m, LinkedList argTypes) {
        LinkedList formalTypes = m.getParams();

        if (argTypes.size() != formalTypes.size()) {
           return false;
        }

        Iterator formalItr = formalTypes.iterator();
        Iterator argItr = argTypes.iterator();

        while (formalItr.hasNext()) {
           TypeNode formalType = (TypeNode) formalItr.next();
           TypeNode argType = (TypeNode) argItr.next();

           if (!isAssignableFromType(formalType, argType)) {
              return false;
           }
        }
        return true;
    } 

    /** Return true iff type is a primitive type. */
    public static boolean isPrimitiveType(TypeNode type)  {
        return (type instanceof PrimitiveTypeNode);
    }

    /** Return true iff type is a reference type. */     
    public static boolean isReferenceType(TypeNode type)  {
        return ((type == NullTypeNode.instance) || (type instanceof TypeNameNode));
    }

    public static boolean isArrayType(TypeNode type) {
        return (type instanceof ArrayTypeNode);
    }

    /** Return true iff type is a arithmetic type. */         
    public static boolean isArithType(TypeNode type)  {     
       return _isOneOf(type, _ARITH_TYPES);
    }

    /** Return true iff type is a floating point type. */             
    public static boolean isFloatType(TypeNode type)  {
       return _isOneOf(type, _FLOAT_TYPES); 
    }

    /** Return true iff type is a discrete valued type. */         
    public static boolean isIntegralType(TypeNode type) {
       return _isOneOf(type, _INTEGRAL_TYPES);        
    }

    /** Return true iff type is a String type. */         
    public static boolean isStringType(TypeNode type) { 
       return compareTypes(type, StaticResolution.STRING_DECL.getDefType());
    }     
   
    public static boolean isAssignableFromConstant(TypeNode type, ExprNode expr) {    
       switch (kind(type)) {
         case TYPE_KIND_BYTE:
         return ExprUtility.isIntConstant(expr, Byte.MIN_VALUE, Byte.MAX_VALUE);
              
         case TYPE_KIND_CHAR:
         return ExprUtility.isIntConstant(expr, Character.MIN_VALUE, Character.MAX_VALUE);
         
         case TYPE_KIND_SHORT:
         return ExprUtility.isIntConstant(expr, Short.MIN_VALUE, Short.MAX_VALUE);       
         
         // not in Titanium ...
         case TYPE_KIND_INT:
         return ExprUtility.isIntConstant(expr, Integer.MIN_VALUE, Integer.MAX_VALUE);                
       }   
       return false; 
    }
    
    public static boolean isAssignableFromType(final TypeNode type1, 
     final TypeNode type2)  {

       int kind1 = kind(type1);
       int kind2 = kind(type2);

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

       switch (kind1) {
         case TYPE_KIND_CLASS:
         switch (kind2) {
   	       case TYPE_KIND_NULL: 
           return true;

	         case TYPE_KIND_INTERFACE: 
	         {
              JavaDecl decl = JavaDecl.getDecl((NamedNode) type1);	       

	            return (decl == StaticResolution.OBJECT_DECL);
  	       }

	         case TYPE_KIND_CLASS:
	         if (isSubClass((ClassDecl) JavaDecl.getDecl((NamedNode) type2), 
	                        (ClassDecl) JavaDecl.getDecl((NamedNode) type1))) {
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

	         case TYPE_KIND_ARRAYINIT: 
  	       return isArrayType(type1);
	       }
	       return false;

         case TYPE_KIND_INTERFACE:
         switch (kind2) {
 	         case TYPE_KIND_NULL: 
  	       return true;

	         case TYPE_KIND_CLASS:
	         {
             ClassDecl decl1 = (ClassDecl) JavaDecl.getDecl(type1);	       
             ClassDecl decl2 = (ClassDecl) JavaDecl.getDecl(type2);	       

	           return doesImplement(decl2, decl1);
	         }

	         case TYPE_KIND_INTERFACE:
	         {
             ClassDecl decl1 = (ClassDecl) JavaDecl.getDecl(type1);	       
             ClassDecl decl2 = (ClassDecl) JavaDecl.getDecl(type2);
  	         return isSuperInterface(decl1, decl2); 
	         }

	         default: 
	         return false;
	       }

         case TYPE_KIND_NULL:
         return false;         
       }       

       // type1 is class o
       return false;
    }   

    public static int kind(TypeNode type) {
    
       switch (type.classID()) {
       
         // null type
         case NullTypeNode.NULLTYPENODE_ID:     return TYPE_KIND_NULL;              

          // primitive types          
         case BoolTypeNode.BOOLTYPENODE_ID:     return TYPE_KIND_BOOL;          
         case CharTypeNode.CHARTYPENODE_ID:     return TYPE_KIND_CHAR; 
         case ByteTypeNode.BYTETYPENODE_ID:     return TYPE_KIND_BYTE; 
         case ShortTypeNode.SHORTTYPENODE_ID:   return TYPE_KIND_SHORT; 
         case IntTypeNode.INTTYPENODE_ID:       return TYPE_KIND_INT; 
         case LongTypeNode.LONGTYPENODE_ID:     return TYPE_KIND_LONG; 
         case FloatTypeNode.FLOATTYPENODE_ID:   return TYPE_KIND_FLOAT; 
         case DoubleTypeNode.DOUBLETYPENODE_ID: return TYPE_KIND_DOUBLE;                  
         
         // class or interface
         case TypeNameNode.TYPENAMENODE_ID:     return kind((TypeNameNode) type);
         
         // array types (derive from Object)
         case ArrayTypeNode.ARRAYTYPENODE_ID:   return TYPE_KIND_CLASS;
       }

       ApplicationUtility.error("unknown type encountered : " + type);
       return TYPE_KIND_UNKNOWN;
    }

    public static int kind(TypeNameNode type) {
       Decl d = JavaDecl.getDecl((NamedNode) type);

       if (d != null) {
          if (d.category == CG_INTERFACE) {
             return TYPE_KIND_INTERFACE;
          } 
       }    
       return TYPE_KIND_CLASS;
    }
             
    public static TypeNode primitiveKindToType(int kind) {
       if (kind < 0) {
          ApplicationUtility.error("unknown type is not primitive");
       }
       
       if (kind > NUM_PRIMITIVE_TYPES) {
          ApplicationUtility.error("type is not primitive");
       }
       
       return _PRIMITIVE_KIND_TO_TYPE[kind];                    
    }         
             
    public static final TypeNode type(final ExprNode expr) {
       return (TypeNode) expr.accept(new TypeVisitor(), null);       
    }         

    public static boolean isSubClass(JavaDecl decl1, JavaDecl decl2) {
       int d1cat = decl1.category;
       int d2cat = decl2.category;

       if ((d1cat != CG_CLASS) || (d2cat != CG_CLASS)) {
          return false;
       }

       return isSubClass((ClassDecl) decl1, (ClassDecl) decl2);
    }

    public static boolean isSubClass(ClassDecl decl1, ClassDecl decl2) {

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

    public static boolean doesImplement(ClassDecl classDecl, ClassDecl iFaceDecl) {
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

    public static boolean isSuperInterface(ClassDecl decl1, ClassDecl decl2) {   
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

    public static TypeNode arithPromoteType(TypeNode type) {
       switch (kind(type)) {
         case TYPE_KIND_BYTE:      
         case TYPE_KIND_CHAR:
         case TYPE_KIND_SHORT:
         case TYPE_KIND_INT:
         return IntTypeNode.instance;
       }
       return type;   
    }

    public static TypeNode arithPromoteType(TypeNode type1, TypeNode type2) {
       int kind1 = kind(type1);
       int kind2 = kind(type2);

       if ((kind1 == TYPE_KIND_DOUBLE) ||
           (kind2 == TYPE_KIND_DOUBLE)) {
          return DoubleTypeNode.instance;
       }
       if ((kind1 == TYPE_KIND_FLOAT) ||
           (kind2 == TYPE_KIND_FLOAT)) {
          return FloatTypeNode.instance;
       }

       if ((kind1 == TYPE_KIND_LONG) ||
           (kind2 == TYPE_KIND_LONG)) {
          return LongTypeNode.instance;
       }

       if ((kind1 == TYPE_KIND_BOOL) ||
           (kind2 == TYPE_KIND_BOOL)) {     
          return BoolTypeNode.instance;
       }
       return IntTypeNode.instance;
    }       

    protected static final boolean _isOneOf(TypeNode type, TypeNode[] typeArray) {
       for (int i = 0; i < typeArray.length; i++) {
           if (typeArray[i] == type) {
              return true;
           }
       }
       return false;          
    }
    
    public static final int TYPE_KIND_UNKNOWN = -1;  
    
    // primitive types
          
    public static final int TYPE_KIND_BOOL    = 0; // first primitive type should start at 0       
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
                      
    protected static final TypeNode[] _PRIMITIVE_KIND_TO_TYPE = new TypeNode[]
     { BoolTypeNode.instance, ByteTypeNode.instance, ShortTypeNode.instance, 
       CharTypeNode.instance, IntTypeNode.instance, LongTypeNode.instance, 
       FloatTypeNode.instance, DoubleTypeNode.instance };
                                                      
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
