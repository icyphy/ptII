/*          
A JavaVisitor that figures out the types of expressions. 

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

/** A JavaVisitor that figures out the types of expressions. This visitor may be run
 *  starting at any expression node.
 *
 *  This visitor attempts to encapsulate all typing policies and is intended to be
 *  overridden to implement different typing policies.
 *
 *  @author Jeff Tsay
 */ 
public class TypeVisitor extends JavaVisitor implements JavaStaticSemanticConstants {
    public TypeVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return _setType(node, IntTypeNode.instance);
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return _setType(node, LongTypeNode.instance);
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return _setType(node, FloatTypeNode.instance);
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return _setType(node, DoubleTypeNode.instance);
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return _setType(node, CharTypeNode.instance);
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {         
        return _setType(node, (TypeNode) StaticResolution.STRING_TYPE.clone());
    }
     
    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        return _setType(node, ArrayInitTypeNode.instance);    
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return _setType(node, NullTypeNode.instance);
    }
    
    public Object visitThisNode(ThisNode node, LinkedList args) {
        TypeNameNode type = (TypeNameNode) node.getDefinedProperty(THIS_CLASS_KEY);
                
        return _setType(node, type);
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        ArrayTypeNode arrType = (ArrayTypeNode) type(node.getArray()); 
        return _setType(node, arrType.getBaseType());
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        TypedDecl decl = (TypedDecl) JavaDecl.getDecl(node.getName());
        return _setType(node, decl.getType());        
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {   
        return _setType(node, StaticResolution.CLASS_TYPE);
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return _setType(node, node.getType());
    }

    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node, LinkedList args) {
        ClassDecl thisDecl = (ClassDecl) node.getType().getName().
         getDefinedProperty(DECL_KEY);           
        return _setType(node, thisDecl.getSuperClass().getDefType());
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        MethodDecl decl = (MethodDecl) JavaDecl.getDecl(node.getMethod());
        
        return _setType(node, decl.getType());
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        return _setType(node, node.getDtype());
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        // returned type is an N-D array of the element type, where 
        // N = # dimension expressions + # empty dimensions
        return _setType(node, 
                 TypeUtility.makeArrayType(node.getDtype(), 
                  node.getDimExprs().size() + node.getDims()));
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        ClassDecl decl = (ClassDecl) node.getDefinedProperty(DECL_KEY);
        NameNode nameNode = new NameNode(AbsentTreeNode.instance, decl.getName());
        nameNode.setProperty(DECL_KEY, decl);
     
        return _setType(node, new TypeNameNode(nameNode));
    }
        
    public Object visitPostIncrNode(PostIncrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitPostDecrNode(PostDecrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitUnaryPlusNode(UnaryPlusNode node, LinkedList args) {
        return _visitUnaryArithNode(node);
    }

    public Object visitUnaryMinusNode(UnaryMinusNode node, LinkedList args) {
        return _visitUnaryArithNode(node);
    }

    public Object visitPreIncrNode(PreIncrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitPreDecrNode(PreDecrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitComplementNode(ComplementNode node, LinkedList args) {
        return _setType(node, arithPromoteType(
         type(node.getExpr())));
    }

    public Object visitNotNode(NotNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        return _setType(node, node.getDtype());
    }

    public Object visitMultNode(MultNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitDivNode(DivNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitRemNode(RemNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitPlusNode(PlusNode node, LinkedList args) {
        TypeNode type1 = type(node.getExpr1());
        
        if (compareTypes(type1, StaticResolution.STRING_TYPE)) {
           return _setType(node, StaticResolution.STRING_TYPE);
        } 

        TypeNode type2 = type(node.getExpr2());
        
        if (compareTypes(type2, StaticResolution.STRING_TYPE)) {
           return _setType(node, StaticResolution.STRING_TYPE);
        } 
                                             
        return _setType(node, arithPromoteType(type1, type2));
    }

    public Object visitMinusNode(MinusNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitLeftShiftLogNode(LeftShiftLogNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRightShiftLogNode(RightShiftLogNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));    
    }

    public Object visitRightShiftArithNode(RightShiftArithNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));    
    }

    public Object visitLTNode(LTNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitGTNode(GTNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitLENode(LENode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitGENode(GENode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitEQNode(EQNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitNENode(NENode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitBitAndNode(BitAndNode node, LinkedList args) {
        return _visitBitwiseNode(node);    
    }

    public Object visitBitOrNode(BitOrNode node, LinkedList args) {
        return _visitBitwiseNode(node);
    }

    public Object visitBitXorNode(BitXorNode node, LinkedList args) {
        return _visitBitwiseNode(node);
    }

    public Object visitCandNode(CandNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitCorNode(CorNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        TypeNode thenType = type(node.getExpr2());
        TypeNode elseType = type(node.getExpr3());
          
        if (compareTypes(thenType, elseType)) {
           return _setType(node, thenType);
        }

        if (isArithType(thenType)) {
           if (((thenType == ByteTypeNode.instance) && 
                (elseType == ShortTypeNode.instance)) ||
               ((thenType == ShortTypeNode.instance) && 
                (elseType == ByteTypeNode.instance))) {
              return _setType(node, ShortTypeNode.instance);
           }
           
           ExprNode thenExpr = node.getExpr2();
           ExprNode elseExpr = node.getExpr3();
           
           // check _validIf() for byte, short, char 
           for (int kind = TYPE_KIND_BYTE; 
                kind <= TYPE_KIND_CHAR; kind++) {
               if (_validIf(thenExpr, thenType, elseExpr, elseType, kind)) {
                  return _setType(node, primitiveKindToType(kind));
               }
           }
           
           return _setType(node, arithPromoteType(thenType, elseType)); 
                
        } else if (isReferenceType(thenType)) {
           if (isAssignableFromType(thenType, elseType)) {
              return _setType(node, thenType);
           } else {
              return _setType(node, elseType);
           }
        }
        
        return _setType(node, arithPromoteType(thenType, elseType));
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitMultAssignNode(MultAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitDivAssignNode(DivAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRemAssignNode(RemAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitPlusAssignNode(PlusAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitMinusAssignNode(MinusAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRightShiftArithAssignNode(RightShiftArithAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitBitAndAssignNode(BitAndAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitBitXorAssignNode(BitXorAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitBitOrAssignNode(BitOrAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public TypeNode _visitFieldAccessNode(FieldAccessNode node) {
        FieldDecl fieldDecl = (FieldDecl) JavaDecl.getDecl((NamedNode) node);
        
        return _setType(node, fieldDecl.getType());
    }
    
    public TypeNode _visitUnaryArithNode(UnaryArithNode node) {
        return _setType(node, arithPromoteType(type(node.getExpr())));
    }

    public TypeNode _visitIncrDecrNode(IncrDecrNode node) {
        return _setType(node, arithPromoteType(type(node.getExpr())));
    }

    public TypeNode _visitBinaryArithNode(BinaryArithNode node) {
        return _setType(node, arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    public TypeNode _visitBitwiseNode(BitwiseNode node) {
        return _setType(node, arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    protected boolean _validIf(ExprNode e1, TypeNode t1, ExprNode e2, 
     TypeNode t2, int kind) {   
        return (((kind(t1) == kind) && 
                 isAssignableFromConstant(t1, e2)) ||
                ((kind(t2) == kind) && 
                 isAssignableFromConstant(t2, e1)));
    }

    /** Memoize the type, and return it. */
    protected TypeNode _setType(ExprNode expr, TypeNode type) {
        expr.setProperty(TYPE_KEY, type);
        return type;            
    }

    /** Return the type of an expression node, checking for a memoized type before 
     *  starting the visitation.
     *  The visitor must not call this method with the same node it handles,
     *  or else an infinite recursion will occur.
     */
    public TypeNode type(ExprNode node) {
        if (node.hasProperty(TYPE_KEY)) {
           return (TypeNode) node.getDefinedProperty(TYPE_KEY); 
        }
        return (TypeNode) node.accept(this, null); 
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        ApplicationUtility.error("node " + node.toString() +
        " is not an expression, so it does not have a type");
        return null;
    }
    
    public TypeNode arithPromoteType(TypeNode type) {
        switch (kind(type)) {
          case TYPE_KIND_BYTE:      
          case TYPE_KIND_CHAR:
          case TYPE_KIND_SHORT:
          case TYPE_KIND_INT:
          return IntTypeNode.instance;
        }
        return type;   
    }
    
    public TypeNode arithPromoteType(final TypeNode type1, final TypeNode type2) {
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

        if ((kind1 == TYPE_KIND_BOOLEAN) ||
            (kind2 == TYPE_KIND_BOOLEAN)) {     
           return BoolTypeNode.instance;
        }
        return IntTypeNode.instance;
    }       
    
    /** Return true iff MethodDecl m1 is more specific 
     *  (in the sense of 14.11.2.2) than MethodDecl m2.  Actually, the right term 
     *  should be "no less specific than", but the Reference Manual is the 
     *  Reference Manual. 
     */
    public boolean isMoreSpecific(final MethodDecl m1, final MethodDecl m2) {

        List params1 = m1.getParams();
        List params2 = m2.getParams();

        if (params2.size() != params2.size()) {         
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
        
        Iterator params1Itr = params1.iterator();
        Iterator params2Itr = params2.iterator();
        
        while (params1Itr.hasNext()) {
            TypeNode param2 = (TypeNode) params2Itr.next();
            TypeNode param1 = (TypeNode) params1Itr.next();

            if (!isAssignableFromType(param2, param1)) {
               return false;
            }
        }
        return true;    
    }

    public boolean isCallableWith(MethodDecl m, List argTypes) {
        List formalTypes = m.getParams();

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
                
    /** For nodes that represent field accesses (ObjectFieldAccessNode, 
     *  ThisFieldAccessNode, SuperFieldAccessNode) the type of the 
     *  object that is accessed (e.g., for a node representing FOO.BAR, 
     *  the type of FOO. This method figures out the sub-type of NODE
     *  and calls the appropriate more specific method. 
     */
    public TypeNode accessedObjectType(FieldAccessNode node) {
        switch (node.classID()) {
        
          case TYPEFIELDACCESSNODE_ID:
          return accessedObjectType((TypeFieldAccessNode) node);
           
          case OBJECTFIELDACCESSNODE_ID:
          return accessedObjectType((ObjectFieldAccessNode) node);
 
          case THISFIELDACCESSNODE_ID:
          return accessedObjectType((ThisFieldAccessNode) node);
          
          case SUPERFIELDACCESSNODE_ID:
          return accessedObjectType((SuperFieldAccessNode) node);        
        } 
        
        ApplicationUtility.error("accessObjectType() not supported for node " + node);        
        return null;              
    }

    /** Return the type of the object that is accessed. */
    public TypeNameNode accessedObjectType(TypeFieldAccessNode node) {
        return JavaDecl.getDecl((NamedNode) node.getFType()).getDefType();
    }

    /** Return the type of the object that is accessed. */
    public TypeNode accessedObjectType(ObjectFieldAccessNode node) {
        return (TypeNode) type((ExprNode) node.getObject());        
    }

    /** Return the type of the object that is accessed, which is the type
     *  of THIS.
     */
    public TypeNameNode accessedObjectType(ThisFieldAccessNode node)  {
        return (TypeNameNode) node.getDefinedProperty(THIS_CLASS_KEY);
    }

    /** Return the type of the object that is accessed, which is the type
     *  of the superclass of THIS.
     */
    public TypeNameNode accessedObjectType(SuperFieldAccessNode node) {    
        ClassDecl myClass = (ClassDecl) JavaDecl.getDecl(
         (NamedNode) node.getDefinedProperty(THIS_CLASS_KEY));
        ClassDecl sclass = myClass.getSuperClass();
        
        return sclass.getDefType();
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
    
    public boolean isAssignableFromType(final TypeNode type1, 
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
       return compareTypes(type, StaticResolution.STRING_DECL.getDefType());
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

    /** Return the kind (an integer) of the type. If the type node is a TypeNameNode,
     *  return kindOfTypeNameNode(type).
     */
    public int kind(TypeNode type) {
    
       switch (type.classID()) {       
         // null type
         case NULLTYPENODE_ID:     return TYPE_KIND_NULL;              

          // primitive types          
         case BOOLTYPENODE_ID:    return TYPE_KIND_BOOLEAN;          
         case CHARTYPENODE_ID:    return TYPE_KIND_CHAR; 
         case BYTETYPENODE_ID:    return TYPE_KIND_BYTE; 
         case SHORTTYPENODE_ID:   return TYPE_KIND_SHORT; 
         case INTTYPENODE_ID:     return TYPE_KIND_INT; 
         case LONGTYPENODE_ID:    return TYPE_KIND_LONG; 
         case FLOATTYPENODE_ID:   return TYPE_KIND_FLOAT; 
         case DOUBLETYPENODE_ID:  return TYPE_KIND_DOUBLE;                  
                       
         // class or interface
         case TYPENAMENODE_ID:    return kindOfTypeNameNode((TypeNameNode) type);
         
         // array types (derive from Object)
         case ARRAYTYPENODE_ID:   return TYPE_KIND_CLASS;

         // void type          
         case VOIDTYPENODE_ID:    return TYPE_KIND_VOID;
       }

       ApplicationUtility.error("unknown type encountered : " + type);
       return TYPE_KIND_UNKNOWN;
    }

    /** Return the kind of the user type, either a class type or an interface type. 
     *  This method should be called in kind() for TypeNameNodes.
     */
    public int kindOfTypeNameNode(TypeNameNode type) {
       Decl d = JavaDecl.getDecl((NamedNode) type);

       if (d != null) {
          if (d.category == CG_INTERFACE) {
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
    
    /** Given a list of expressions, return an array of the corresponding types
     *  of the expressions.
     */
    public TypeNode[] typeArray(List exprList) {
        Iterator exprItr = exprList.iterator();
        TypeNode[] retval = new TypeNode[exprList.size()];
        
        int i = 0; 
        while (exprItr.hasNext()) {
            retval[i] = type((ExprNode) exprItr.next());            
            i++;
        }    
        return retval;
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

    // kinds of types, a mapping from types to integers
    
    public static final int TYPE_KIND_UNKNOWN = -1;  
    
    // primitive types
          
    public static final int TYPE_KIND_BOOLEAN    = 0; // first primitive type should start at 0       
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
