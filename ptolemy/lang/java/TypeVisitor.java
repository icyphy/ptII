package ptolemy.lang.java;

import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

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
        return _setType(node, StaticResolution.STRING_TYPE);
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

    // for AllocateAnonymousClassNode, type property is already defined by ResolvePackageVisitor

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
        return _setType(node, TypeUtility.arithPromoteType(
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
        
        if (TypeUtility.compareTypes(type1, StaticResolution.STRING_TYPE)) {
           return _setType(node, StaticResolution.STRING_TYPE);
        } 

        TypeNode type2 = type(node.getExpr2());
        
        if (TypeUtility.compareTypes(type2, StaticResolution.STRING_TYPE)) {
           return _setType(node, StaticResolution.STRING_TYPE);
        } 
                                             
        return _setType(node, TypeUtility.arithPromoteType(type1, type2));
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
          
        if (TypeUtility.compareTypes(thenType, elseType)) {
           return _setType(node, thenType);
        }

        if (TypeUtility.isArithType(thenType)) {
           if (((thenType == ByteTypeNode.instance) && 
                (elseType == ShortTypeNode.instance)) ||
               ((thenType == ShortTypeNode.instance) && 
                (elseType == ByteTypeNode.instance))) {
              return _setType(node, ShortTypeNode.instance);
           }
           
           ExprNode thenExpr = node.getExpr2();
           ExprNode elseExpr = node.getExpr3();
           
           // check _validIf() for byte, short, char 
           for (int kind = TypeUtility.TYPE_KIND_BYTE; 
                kind <= TypeUtility.TYPE_KIND_CHAR; kind++) {
               if (_validIf(thenExpr, thenType, elseExpr, elseType, kind)) {
                  return _setType(node, TypeUtility.primitiveKindToType(kind));
               }
           }
           
           return _setType(node, TypeUtility.arithPromoteType(thenType, elseType)); 
                
        } else if (TypeUtility.isReferenceType(thenType)) {
           if (TypeUtility.isAssignableFromType(thenType, elseType)) {
              return _setType(node, thenType);
           } else {
              return _setType(node, elseType);
           }
        }
        
        return _setType(node, TypeUtility.arithPromoteType(thenType, elseType));
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
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr())));
    }

    public TypeNode _visitIncrDecrNode(IncrDecrNode node) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr())));
    }

    public TypeNode _visitBinaryArithNode(BinaryArithNode node) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    public TypeNode _visitBitwiseNode(BitwiseNode node) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    protected static boolean _validIf(ExprNode e1, TypeNode t1, ExprNode e2, 
     TypeNode t2, int kind) {   
        return (((TypeUtility.kind(t1) == kind) && 
                 TypeUtility.isAssignableFromConstant(t1, e2)) ||
                ((TypeUtility.kind(t2) == kind) && 
                 TypeUtility.isAssignableFromConstant(t2, e1)));
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
}
