/*
A JavaVisitor that regenerates Java code from the abstract syntax tree.

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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A JavaVisitor that regenerates Java code from the abstract syntax tree.
 * 
 *  @author Jeff Tsay
 */
public class JavaCodeGenerator extends JavaVisitor implements JavaStaticSemanticConstants {
    public JavaCodeGenerator() {
        super(TM_CHILDREN_FIRST);
    }

    public Object visitNameNode(NameNode node, LinkedList args) {
        String ident = node.getIdent();
        TreeNode qualifier = node.getQualifier();
        if (qualifier == AbsentTreeNode.instance) {
           return ident;
        }

        String qualPartStr = (String) node.childReturnValueAt(node.CHILD_INDEX_QUALIFIER);

        return qualPartStr + '.' + ident;
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return "";
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return node.getLiteral();
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return node.getLiteral();
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return node.getLiteral();
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return node.getLiteral();
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return node.getLiteral();
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return "'" + node.getLiteral() + '\''; 
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return "\"" + node.getLiteral() + '\"';
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return "boolean";
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return "char";
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return "byte";
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return "short";
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return "int";
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return "float";
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return "long";
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return "double";
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_BASETYPE) + "[]";
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return "void";
    }

    public Object visitPackageNode(PackageNode node, LinkedList args) {
        String nameString = (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);

        return "package " + nameString + ";\n";
    }

    public Object visitNullTypeNode(NullTypeNode node, LinkedList args) {
        ApplicationUtility.error("NullTypeNode should not appear in the " +
         "final parse tree.");
        return null;
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_TYPE) + ".this";
    }

    public Object visitOuterSuperAccess(OuterSuperAccessNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_TYPE) + ".super";
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        String pkgStr = (String) node.childReturnValueAt(node.CHILD_INDEX_PKG);

        // not using package nodes
        if (pkgStr.length() > 0) {
           sb.append("package " + pkgStr + ";\n");
        }

        List impList = (List) node.childReturnValueAt(node.CHILD_INDEX_IMPORTS);

        Iterator impItr = impList.iterator();

        while (impItr.hasNext()) {
          sb.append((String) impItr.next());
        }

        sb.append('\n');

        List typeList = (List) node.childReturnValueAt(node.CHILD_INDEX_DEFTYPES);

        Iterator typeItr = typeList.iterator();

        while (typeItr.hasNext()) {
          sb.append((String) typeItr.next() + '\n');
        }

        return sb.toString();
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {
        String nameString = (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);

        return "import " + nameString + ";\n";
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {
        String nameString = (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);

        return "import " + nameString + ".*;\n";
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();
        String name = (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);

        sb.append(Modifier.toString(node.getModifiers()) + "class " + name + ' ');

        TreeNode superClass = node.getSuperClass();

        if (superClass != AbsentTreeNode.instance) {
           sb.append("extends " + node.childReturnValueFor(superClass) + ' ');
        }

        List implList = (List) node.childReturnValueAt(node.CHILD_INDEX_INTERFACES);

        if (!implList.isEmpty()) {
           sb.append("implements " + _commaList(implList) + ' ');
        }

        sb.append("{\n");

        List memberList = (List) node.childReturnValueAt(node.CHILD_INDEX_MEMBERS);

        if (!memberList.isEmpty()) {
           Iterator memberItr = memberList.iterator();

           while (memberItr.hasNext()) {
             String methodString = (String) memberItr.next();
             sb.append(methodString);
           }
        }

        sb.append("}\n");

        return sb.toString();
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {

        StringBuffer sb = new StringBuffer();

        sb.append(Modifier.toString(node.getModifiers()) + 
                  (String) node.childReturnValueAt(node.CHILD_INDEX_DEFTYPE) + ' ' + 
                  node.getName().getIdent());

        String initStr = (String) node.childReturnValueAt(node.CHILD_INDEX_INITEXPR);
        
        // AbsentTreeNode returns a String of length 0
        if (initStr.length() > 0) {
           sb.append(" = " + initStr);
        }

        sb.append(";\n");

        return sb.toString();
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return Modifier.toString(node.getModifiers()) + 
               (String) node.childReturnValueAt(node.CHILD_INDEX_DEFTYPE) +
               ' ' + node.getName().getIdent() + " = " +
               (String) node.childReturnValueAt(node.CHILD_INDEX_INITEXPR)  + ";\n";
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        List paramList = (List) node.childReturnValueAt(node.CHILD_INDEX_PARAMS);
        
        sb.append(Modifier.toString(node.getModifiers()) + 
                  (String) node.childReturnValueAt(node.CHILD_INDEX_RETURNTYPE) + 
                  ' ' + (String) node.childReturnValueAt(node.CHILD_INDEX_NAME) + '(' + 
                  _commaList(paramList) + ')');

        List throwsList = (List) node.childReturnValueAt(node.CHILD_INDEX_THROWSLIST);

        if (!throwsList.isEmpty()) {
           sb.append(" throws " + _commaList(throwsList));
        }

        if (node.getBody() == AbsentTreeNode.instance) {
           sb.append(';');
        } else {
           sb.append(" " + (String) node.childReturnValueAt(node.CHILD_INDEX_BODY));
        }

        sb.append('\n');

        return sb.toString();
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();
        List paramList = (List) node.childReturnValueAt(node.CHILD_INDEX_PARAMS);

        sb.append(Modifier.toString(node.getModifiers()));

        sb.append(node.getName().getIdent() + '(' + _commaList(paramList) + ") ");

        List throwsList = (List) node.childReturnValueAt(node.CHILD_INDEX_THROWSLIST);

        if (!throwsList.isEmpty()) {
           sb.append("throws " + _commaList(throwsList) + ' ');
        }

        sb.append("{\n");

        sb.append((String) node.childReturnValueAt(
         node.CHILD_INDEX_CONSTRUCTORCALL));

        String bodyStr = (String) node.childReturnValueAt(node.CHILD_INDEX_BODY);

        if (bodyStr.length() > 2) {
           // get rid of the first '{' and '\n' of the block node string
           bodyStr = bodyStr.substring(2);
           sb.append(bodyStr);
        }

        sb.append('\n');

        return sb.toString();
    }

    public Object visitThisConstructorCallNode(ThisConstructorCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);

        return "this(" + _commaList(argsList) + ");\n";
    }

    public Object visitStaticInitNode(StaticInitNode node, LinkedList args) {
        return "static " +
               (String) node.childReturnValueAt(node.CHILD_INDEX_BLOCK) + '\n';
    }

    public Object visitInstanceInitNode(InstanceInitNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_BLOCK);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        String name = (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);    
    
        StringBuffer sb = new StringBuffer();

        sb.append(Modifier.toString(node.getModifiers()) +
                  "interface " + name + ' ');

        List implList = (List) node.childReturnValueAt(node.CHILD_INDEX_INTERFACES);

        if (!implList.isEmpty()) {
           sb.append("extends " + _commaList(implList) + ' ');
        }

        sb.append("{\n");

        List memberList = (List) node.childReturnValueAt(node.CHILD_INDEX_MEMBERS);

        if (!memberList.isEmpty()) {
           Iterator memberItr = memberList.iterator();

           while (memberItr.hasNext()) {
             String methodString = (String) memberItr.next();
             sb.append(methodString);
           }
        }

        sb.append("}\n");

        return sb.toString();
    }

    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        sb.append('{');

        List initializers = (List) node.childReturnValueAt(node.CHILD_INDEX_INITIALIZERS);

        sb.append(_commaList(initializers));

        sb.append('}');

        return sb.toString();
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        sb.append(Modifier.toString(node.getModifiers()));

        sb.append((String) node.childReturnValueAt(node.CHILD_INDEX_DEFTYPE));
        sb.append(" " + node.getName().getIdent());

        return sb.toString();
    }

    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);      

        return "super(" + _commaList(argsList) + ");\n";
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return "{\n" + _lineList((List) node.childReturnValueAt(node.CHILD_INDEX_STMTS)) +
               "}\n";
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return ";\n";
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_NAME) +       
               ": " + (String) node.childReturnValueAt(node.CHILD_INDEX_STMT);
    }

    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        sb.append("if (" + 
                  (String) node.childReturnValueAt(node.CHILD_INDEX_CONDITION) +
                  ") " +
                  (String) node.childReturnValueAt(node.CHILD_INDEX_THENPART));

        TreeNode elsePart = node.getElsePart();
        if (elsePart != AbsentTreeNode.instance) {
           sb.append(" else " +
                     (String) node.childReturnValueAt(node.CHILD_INDEX_ELSEPART));
        }

        sb.append('\n');

        return sb.toString();
    }

    public Object visitSwitchNode(SwitchNode node, LinkedList args) {        
        return "switch (" + 
               (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR) + ") {\n" +
               _listToString((List) node.childReturnValueAt(node.CHILD_INDEX_SWITCHBLOCKS), "\n") + 
               "}\n";                                                         
    }

    public Object visitCaseNode(CaseNode node, LinkedList args) {
        if (node.getExpr() == AbsentTreeNode.instance) {
           return "default:\n";
        }
    
        return "case " + (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR) + ":\n";               
    }

    public Object visitSwitchBranchNode(SwitchBranchNode node, LinkedList args) {
        return _lineList((List) node.childReturnValueAt(node.CHILD_INDEX_CASES)) +
               _lineList((List) node.childReturnValueAt(node.CHILD_INDEX_STMTS));
    }

    public Object visitLoopNode(LoopNode node, LinkedList args) {
        if (node.getForeStmt().classID() == EMPTYSTMTNODE_ID) {
           // while loop
           return "while (" +
                  (String) node.childReturnValueAt(node.CHILD_INDEX_TEST) +
                  ") \n" +
                  (String) node.childReturnValueAt(node.CHILD_INDEX_AFTSTMT);
        } else {
           // do loop
           return "do " +
                  (String) node.childReturnValueAt(node.CHILD_INDEX_FORESTMT) +
                  " while (" +
                  (String) node.childReturnValueAt(node.CHILD_INDEX_TEST) +
                  ");\n";
        }
    }

    public Object visitExprStmtNode(ExprStmtNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR) + ";\n";
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        return "for (" + _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_INIT)) +
               "; " + (String) node.childReturnValueAt(node.CHILD_INDEX_TEST) + "; " +
               _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_UPDATE)) + ") " +
               (String) node.childReturnValueAt(node.CHILD_INDEX_STMT) + '\n';
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        sb.append("break");

        if (node.getLabel() != AbsentTreeNode.instance) {
           sb.append(" " + (String) node.childReturnValueAt(node.CHILD_INDEX_LABEL));
        }
        sb.append(";\n");

        return sb.toString();
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        sb.append("continue");

        if (node.getLabel() != AbsentTreeNode.instance) {
           sb.append(" " + (String) node.childReturnValueAt(node.CHILD_INDEX_LABEL));
        }
        sb.append(";\n");

        return sb.toString();

    }

    public Object visitReturnNode(ReturnNode node, LinkedList args) {
        return "return " + (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR) + ";\n";
    }

    public Object visitThrowNode(ThrowNode node, LinkedList args) {
        return "throw " + (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR) + ";\n";
    }

    public Object visitSynchronizedNode(SynchronizedNode node, LinkedList args) {
        return "synchronized (" +
               (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR) + ") " +
               (String) node.childReturnValueAt(node.CHILD_INDEX_STMT) + "\n";
    }

    public Object visitCatchNode(CatchNode node, LinkedList args) {
        return "catch (" + 
               (String) node.childReturnValueAt(node.CHILD_INDEX_PARAM) + ") " +
               (String) node.childReturnValueAt(node.CHILD_INDEX_BLOCK);
    }

    public Object visitTryNode(TryNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();
        
        sb.append("try " + (String) node.childReturnValueAt(node.CHILD_INDEX_BLOCK));

        List catchesList = (List) node.childReturnValueAt(node.CHILD_INDEX_CATCHES);

        Iterator catchesItr = catchesList.iterator();

        while (catchesItr.hasNext()) {
          sb.append((String) catchesItr.next());
        }

        if (node.getFinly() != AbsentTreeNode.instance) {
           sb.append(" finally " +  
                     (String) node.childReturnValueAt(node.CHILD_INDEX_FINLY));
        }

        return sb.toString();
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return "null";
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return "this";
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_ARRAY) +
               '[' +  (String) node.childReturnValueAt(node.CHILD_INDEX_INDEX) + ']';
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_OBJECT) + '.' +
               (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return "super." + (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_FTYPE) +
               '.' +  (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {        
        return "this." + (String) node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_FTYPE) + ".class";
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);        

        return (String) node.childReturnValueAt(node.CHILD_INDEX_METHOD) +
               '(' + _commaList(argsList) + ')';
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        String enclosingInstance = (String)
         node.childReturnValueAt(node.CHILD_INDEX_ENCLOSINGINSTANCE);

        if (!(enclosingInstance.equals("") || enclosingInstance.equals("this"))) {
           sb.append(enclosingInstance + '.');
        }

        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);

        sb.append("new " + (String) node.childReturnValueAt(node.CHILD_INDEX_DTYPE) +
                  '(' + _commaList(argsList) +  ')');

        return sb.toString();
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {

        StringBuffer sb = new StringBuffer();

        sb.append("new " + (String) node.childReturnValueAt(node.CHILD_INDEX_DTYPE));

        List dimExprList = (List) node.childReturnValueAt(node.CHILD_INDEX_DIMEXPRS);

        Iterator dimExprItr = dimExprList.iterator();

        while (dimExprItr.hasNext()) {
            sb.append("[" + (String) dimExprItr.next() + ']');
        }

        for (int dimsLeft = node.getDims(); dimsLeft > 0; dimsLeft--) {
            sb.append("[]");
        }

        if (node.getInitExpr() != AbsentTreeNode.instance) {
           String initExpr =
            (String) node.childReturnValueAt(node.CHILD_INDEX_INITEXPR);

           sb.append(" " + initExpr);
        }

        return sb.toString();
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitPostIncrNode(PostIncrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "++", true);
    }

    public Object visitPostDecrNode(PostDecrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "++", true);
    }

    public Object visitUnaryPlusNode(UnaryPlusNode node, LinkedList args) {
        return _visitSingleExprNode(node, "+", false);
    }

    public Object visitUnaryMinusNode(UnaryMinusNode node, LinkedList args) {
        return _visitSingleExprNode(node, "-", false);
    }

    public Object visitPreIncrNode(PreIncrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "++", false);
    }

    public Object visitPreDecrNode(PreDecrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "--", false);
    }

    public Object visitComplementNode(ComplementNode node, LinkedList args) {
        return _visitSingleExprNode(node, "~", false);
    }

    public Object visitNotNode(NotNode node, LinkedList args) {
        return _visitSingleExprNode(node, "!", false);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        String exprString = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR);
        ExprNode expr = (ExprNode) node.getExpr();
       
        return "(" + (String) node.childReturnValueAt(node.CHILD_INDEX_DTYPE) +
               ") " + _parenExpr(expr, exprString);
    }

    public Object visitMultNode(MultNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "*");
    }

    public Object visitDivNode(DivNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "/");
    }

    public Object visitRemNode(RemNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "%");
    }

    public Object visitPlusNode(PlusNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "+");
    }

    public Object visitMinusNode(MinusNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "-");
    }

    public Object visitLeftShiftLogNode(LeftShiftLogNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "<<");
    }

    public Object visitRightShiftLogNode(RightShiftLogNode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">>>");
    }

    public Object visitRightShiftArithNode(RightShiftArithNode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">>");
    }

    public Object visitLTNode(LTNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "<");
    }

    public Object visitGTNode(GTNode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">");
    }

    public Object visitLENode(LENode node, LinkedList args) {
        return _visitBinaryOpNode(node, "<=");
    }

    public Object visitGENode(GENode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">=");
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        return (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR) +
               " instanceof " +
               (String) node.childReturnValueAt(node.CHILD_INDEX_DTYPE);
    }

    public Object visitEQNode(EQNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "==");
    }

    public Object visitNENode(NENode node, LinkedList args) {
        return _visitBinaryOpNode(node, "!=");
    }

    public Object visitBitAndNode(BitAndNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "&");
    }

    public Object visitBitOrNode(BitOrNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "|");
    }

    public Object visitBitXorNode(BitXorNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "^");
    }

    public Object visitCandNode(CandNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "&&");
    }

    public Object visitCorNode(CorNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "||");
    }

    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        String e1String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        String e2String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);
        String e3String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR3);

        e1String = _parenExpr(node.getExpr1(), e1String);
        e2String = _parenExpr(node.getExpr2(), e2String);
        e3String = _parenExpr(node.getExpr3(), e2String);

        return e1String + " ? " + e2String + " : " + e3String;
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        StringBuffer sb = new StringBuffer();

        String e1String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        String e2String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);

        sb.append(e1String + " = " + e2String);

        return sb.toString();
    }

    public Object visitMultAssignNode(MultAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "*=");
    }

    public Object visitDivAssignNode(DivAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "/=");
    }

    public Object visitRemAssignNode(RemAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "%=");
    }

    public Object visitPlusAssignNode(PlusAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "+=");
    }

    public Object visitMinusAssignNode(MinusAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "-=");
    }

    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "<<=");
    }

    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, ">>>=");
    }

    public Object visitRightShiftArithAssignNode(RightShiftArithAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, ">>=");
    }

    public Object visitBitAndAssignNode(BitAndAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "&=");
    }

    public Object visitBitXorAssignNode(BitXorAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "^=");
    }

    public Object visitBitOrAssignNode(BitOrAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "|=");
    }

    protected String _visitSingleExprNode(SingleExprNode node, String opString,
     boolean post) {
        String exprString = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR);

        exprString = _parenExpr(node.getExpr(), exprString);

        return (post ? (exprString + opString) : (opString + exprString));
    }

    protected String _visitBinaryOpNode(BinaryOpNode node, String opString) {
        String e1String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        String e2String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);

        e1String = _parenExpr(node.getExpr1(), e1String);
        e2String = _parenExpr(node.getExpr2(), e2String);

        return e1String + ' ' + opString + ' ' + e2String;
    }

    protected String _visitBinaryOpAssignNode(BinaryOpAssignNode node, String opString) {       
        String e1String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        String e2String = (String) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);

        return e1String + ' ' + opString + ' ' + e2String;
    }

    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // for testing purposes only
        return "";
    }

    protected static String _commaList(List list) {
        return _listToString(list, ", ");
    }

    protected static String _lineList(List list) {
        return _listToString(list, "");
    }        
    
    protected static String _listToString(List list, String separator) {
        StringBuffer sb = new StringBuffer();

        Iterator itr = list.iterator();

        while (itr.hasNext()) {
          sb.append((String) itr.next());

          if (itr.hasNext()) {
             sb.append(separator);
          }
        }

        return sb.toString();
    }
        
    protected String _parenExpr(ExprNode expr, String exprStr) {
       if ((expr instanceof BinaryOpNode) ||
           (expr instanceof BinaryOpAssignNode) ||
           (expr instanceof AssignNode) ||
           (expr instanceof InstanceOfNode) ||
           (expr instanceof EqualityNode) ||
           (expr instanceof IfExprNode)) {
          return "(" + exprStr + ')';
       }

       return exprStr;
    }
}
