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
 *  @author Jeff Tsay
 */ 
public class TypeVisitor extends JavaVisitor implements JavaStaticSemanticConstants {
    public TypeVisitor() {        
        this(new TypePolicy());
    }
    
    public TypeVisitor(TypePolicy typePolicy) {
        super(TM_CUSTOM);
        _typeID = typePolicy.typeIdentifier();            
        _typePolicy = typePolicy;
    }

    public TypeIdentifier typeIdentifier() {
        return _typeID;    
    }

    public TypePolicy typePolicy() {
        return _typePolicy;    
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
        return _setType(node, _typePolicy.arithPromoteType(
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
        
        if (_typePolicy.compareTypes(type1, StaticResolution.STRING_TYPE)) {
           return _setType(node, StaticResolution.STRING_TYPE);
        } 

        TypeNode type2 = type(node.getExpr2());
        
        if (_typePolicy.compareTypes(type2, StaticResolution.STRING_TYPE)) {
           return _setType(node, StaticResolution.STRING_TYPE);
        } 
                                             
        return _setType(node, _typePolicy.arithPromoteType(type1, type2));
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
          
        if (_typePolicy.compareTypes(thenType, elseType)) {
           return _setType(node, thenType);
        }

        if (_typePolicy.isArithType(thenType)) {
           if (((thenType == ByteTypeNode.instance) && 
                (elseType == ShortTypeNode.instance)) ||
               ((thenType == ShortTypeNode.instance) && 
                (elseType == ByteTypeNode.instance))) {
              return _setType(node, ShortTypeNode.instance);
           }
           
           ExprNode thenExpr = node.getExpr2();
           ExprNode elseExpr = node.getExpr3();
           
           // check _validIf() for byte, short, char 
           for (int kind = TypeIdentifier.TYPE_KIND_BYTE; 
                kind <= TypeIdentifier.TYPE_KIND_CHAR; kind++) {
               if (_validIf(thenExpr, thenType, elseExpr, elseType, kind)) {
                  return _setType(node, _typeID.primitiveKindToType(kind));
               }
           }
           
           return _setType(node, _typePolicy.arithPromoteType(thenType, elseType)); 
                
        } else if (_typePolicy.isReferenceType(thenType)) {
           if (_typePolicy.isAssignableFromType(thenType, elseType)) {
              return _setType(node, thenType);
           } else {
              return _setType(node, elseType);
           }
        }
        
        return _setType(node, _typePolicy.arithPromoteType(thenType, elseType));
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
        return _setType(node, _typePolicy.arithPromoteType(type(node.getExpr())));
    }

    public TypeNode _visitIncrDecrNode(IncrDecrNode node) {
        return _setType(node, _typePolicy.arithPromoteType(type(node.getExpr())));
    }

    public TypeNode _visitBinaryArithNode(BinaryArithNode node) {
        return _setType(node, _typePolicy.arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    public TypeNode _visitBitwiseNode(BitwiseNode node) {
        return _setType(node, _typePolicy.arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    protected boolean _validIf(ExprNode e1, TypeNode t1, ExprNode e2, 
     TypeNode t2, int kind) {   
        return (((_typeID.kind(t1) == kind) && 
                 _typePolicy.isAssignableFromConstant(t1, e2)) ||
                ((_typeID.kind(t2) == kind) && 
                 _typePolicy.isAssignableFromConstant(t2, e1)));
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
        
        ApplicationUtility.error("accessdObjectType() not supported for node " + node);        
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
                        
    protected final TypeIdentifier _typeID;
    protected final TypePolicy _typePolicy;
}
