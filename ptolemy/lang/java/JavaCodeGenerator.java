/*
A JavaVisitor that regenerates Java code from the abstract syntax tree.
For efficiency reasons, the return value of each visit method returns a 
string list, which is a List composed of Strings or string lists. 
_stringListToString() converts a string list into a flattened String.

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

import java.io.FileOutputStream;
import java.io.IOException;
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

    /** Return the code fragment corresponding to a node. */
    public static String writeCodeFragment(TreeNode node) {
        JavaCodeGenerator jcg = new JavaCodeGenerator();
        Object retval = node.accept(jcg, null);
     
        if (retval instanceof String) {
           return (String) retval;
        } 

        return _stringListToString((List) retval);
    }

    /** Write out the code files for each member of the argument list of 
     *  CompileUnitNodes to the files with names corresponding to the members 
     *  of the argument filename list.
     */
    public static void writeCompileUnitNodeList(List unitList, List filenameList) {
        Iterator unitItr = unitList.iterator();
        Iterator filenameItr = filenameList.iterator();

        while (unitItr.hasNext()) {
            CompileUnitNode unitNode = (CompileUnitNode) unitItr.next();

            String outCode = writeCodeFragment(unitNode);

            String outFileName = (String) filenameItr.next();

            try {
                FileOutputStream outFile = new FileOutputStream(outFileName);
                outFile.write(outCode.getBytes());
                outFile.close();
            } catch (IOException e) {
                System.err.println("error opening/writing/closing output file "
                        + outFileName);
                System.err.println(e.toString());
            }
        }
    }

    public Object visitNameNode(NameNode node, LinkedList args) {
        String ident = node.getIdent();
        TreeNode qualifier = node.getQualifier();
        if (qualifier == AbsentTreeNode.instance) {
            return TNLManip.cons(ident);
        }

        List qualPartList =
            (List) node.childReturnValueAt(node.CHILD_INDEX_QUALIFIER);

        return TNLManip.arrayToList(new Object[] {qualPartList, "." + ident});
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return TNLManip.cons("");
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return TNLManip.cons(node.getLiteral());
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return TNLManip.cons(node.getLiteral());
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return TNLManip.cons(node.getLiteral());
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return TNLManip.cons(node.getLiteral());
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return TNLManip.cons(node.getLiteral());
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return TNLManip.cons("'" + node.getLiteral() + '\'');
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return TNLManip.cons("\"" + node.getLiteral() + '\"');
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return TNLManip.cons("boolean");
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return TNLManip.cons("char");
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return TNLManip.cons("byte");
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return TNLManip.cons("short");
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return TNLManip.cons("int");
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return TNLManip.cons("float");
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return TNLManip.cons("long");
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return TNLManip.cons("double");
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_BASETYPE), "[]"});
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return TNLManip.cons("void");
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_TYPE), ".this"});
    }

    public Object visitOuterSuperAccess(OuterSuperAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_TYPE), ".super"});
    }

    // FIXME : This returns a String instead of a string list, as it should
    // for consistency. However, returning a string list will break some
    // existing code, so this change will be deferred.
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        if (node.getPkg() != AbsentTreeNode.instance) {
            retList.addLast("package ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_PKG));
            retList.addLast(";\n");
        }

        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_IMPORTS));

        retList.addLast("\n");

        List typeList = (List) node.childReturnValueAt(node.CHILD_INDEX_DEFTYPES);

        Iterator typeItr = typeList.iterator();

        while (typeItr.hasNext()) {
            retList.addLast(typeItr.next());
            retList.addLast("\n");
        }

        // for new version:
        // return retList;
        
        return _stringListToString(retList);
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"import ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME), ";\n"});
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {

        return TNLManip.arrayToList(new Object[] {"import ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME), ".*;\n"});
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_NAME));
        retList.addLast(" ");

        TreeNode superClass = node.getSuperClass();

        if (superClass != AbsentTreeNode.instance) {
            retList.addLast("extends ");
            retList.addLast(node.childReturnValueFor(superClass));
            retList.addLast(" ");
        }

        List implList = (List) node.childReturnValueAt(node.CHILD_INDEX_INTERFACES);

        if (!implList.isEmpty()) {
            retList.addLast("implements ");
            retList.addLast(_commaList(implList));
            retList.addLast(" ");
        }

        retList.addLast("{\n");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_MEMBERS));
        retList.addLast("}\n");

        return retList;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_RETURNTYPE));
        retList.addLast(" ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_NAME));
        retList.addLast("(");
        retList.addLast(
                _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_PARAMS)));
        retList.addLast(")");

        List throwsList = (List) node.childReturnValueAt(node.CHILD_INDEX_THROWSLIST);

        if (!throwsList.isEmpty()) {
            retList.addLast(" throws ");
            retList.addLast(_commaList(throwsList));
        }

        if (node.getBody() == AbsentTreeNode.instance) {
            retList.addLast(";");
        } else {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_BODY));
        }

        retList.addLast("\n");

        return retList;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast(node.getName().getIdent());
        retList.addLast("(");
        retList.addLast(
                _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_PARAMS)));
        retList.addLast(") ");

        List throwsList = (List) node.childReturnValueAt(node.CHILD_INDEX_THROWSLIST);

        if (!throwsList.isEmpty()) {
            retList.addLast("throws ");
            retList.addLast(_commaList(throwsList));
            retList.addLast(" ");
        }

        retList.addLast("{\n");

        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_CONSTRUCTORCALL));

        LinkedList bodyStrList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_BODY);

        if (bodyStrList.size() > 1) {
            // get rid of the first '{' and '\n' of the block node string
            bodyStrList.removeFirst();
            retList.addLast(bodyStrList);
        }

        retList.addLast("\n");

        return retList;
    }

    public Object visitThisConstructorCallNode(ThisConstructorCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);

        return TNLManip.arrayToList(new Object[] {"this(",
                                                      _commaList(argsList), ");\n"});
    }

    public Object visitStaticInitNode(StaticInitNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"static ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_BLOCK), "\n"});
    }

    public Object visitInstanceInitNode(InstanceInitNode node, LinkedList args) {
        return node.childReturnValueAt(node.CHILD_INDEX_BLOCK);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("interface ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_NAME));
        retList.addLast(" ");

        List implList = (List) node.childReturnValueAt(node.CHILD_INDEX_INTERFACES);

        if (!implList.isEmpty()) {
            retList.addLast("extends ");
            retList.addLast(_commaList(implList));
            retList.addLast(" ");
        }

        retList.addLast("{\n");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_MEMBERS));
        retList.addLast("}\n");

        return retList;
    }

    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"{", _commaList(
                (List) node.childReturnValueAt(node.CHILD_INDEX_INITIALIZERS)),
                                                      "}"});
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            Modifier.toString(node.getModifiers()),
                node.childReturnValueAt(node.CHILD_INDEX_DEFTYPE),
                " " + node.getName().getIdent()});
    }

    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"super(",
                                                      _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_ARGS)),
                                                      ");\n"});
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"{\n",
                                                      node.childReturnValueAt(node.CHILD_INDEX_STMTS), "}\n"});
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return TNLManip.cons(";\n");
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_NAME),
                ": ",
                node.childReturnValueAt(node.CHILD_INDEX_STMT)});
    }

    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("if (");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_CONDITION));
        retList.addLast(") ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_THENPART));

        TreeNode elsePart = node.getElsePart();
        if (elsePart != AbsentTreeNode.instance) {
            retList.addLast(" else ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_ELSEPART));
        }

        retList.addLast("\n");

        return retList;
    }

    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"switch (",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ") {\n",
                                                      _separateList((List) node.childReturnValueAt(node.CHILD_INDEX_SWITCHBLOCKS), "\n"),
                                                      "}\n"});
    }

    public Object visitCaseNode(CaseNode node, LinkedList args) {
        if (node.getExpr() == AbsentTreeNode.instance) {
            return TNLManip.cons("default:\n");
        }

        return TNLManip.arrayToList(new Object[] {"case ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ":\n"});
    }

    public Object visitSwitchBranchNode(SwitchBranchNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_CASES),
                node.childReturnValueAt(node.CHILD_INDEX_STMTS)});
    }

    public Object visitLoopNode(LoopNode node, LinkedList args) {
        if (node.getForeStmt().classID() == EMPTYSTMTNODE_ID) {
            // while loop
            return TNLManip.arrayToList(new Object[] {"while (",
                                                          node.childReturnValueAt(node.CHILD_INDEX_TEST), ") \n",
                                                          node.childReturnValueAt(node.CHILD_INDEX_AFTSTMT)});
        } else {
            // do loop
            return TNLManip.arrayToList(new Object[] {"do ",
                                                          node.childReturnValueAt(node.CHILD_INDEX_FORESTMT),
                                                          " while (",
                                                          node.childReturnValueAt(node.CHILD_INDEX_TEST),
                                                          ");\n"});
        }
    }

    public Object visitExprStmtNode(ExprStmtNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_EXPR), ";\n"});
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"for (",
                                                      _forInitStringList(node.getInit()), "; ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_TEST), "; ",
                                                      _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_UPDATE)),
                                                      ") ", node.childReturnValueAt(node.CHILD_INDEX_STMT), "\n"});
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("break");

        if (node.getLabel() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_LABEL));
        }
        retList.addLast(";\n");

        return retList;
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("continue");

        if (node.getLabel() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_LABEL));
        }
        retList.addLast(";\n");

        return retList;
    }

    public Object visitReturnNode(ReturnNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"return ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ";\n"});
    }

    public Object visitThrowNode(ThrowNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"throw ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ";\n"});
    }

    public Object visitSynchronizedNode(SynchronizedNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"synchronized (",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ") ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_STMT), "\n"});
    }

    public Object visitCatchNode(CatchNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"catch (",
                                                      node.childReturnValueAt(node.CHILD_INDEX_PARAM), ") ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_BLOCK)});
    }

    public Object visitTryNode(TryNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("try ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_BLOCK));

        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_CATCHES));

        if (node.getFinly() != AbsentTreeNode.instance) {
            retList.addLast(" finally ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_FINLY));
        }

        return retList;
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return TNLManip.cons("null");
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return TNLManip.cons("this");
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        LinkedList arrayStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_ARRAY);

        return TNLManip.arrayToList(new Object[] {
            _parenExpr(node.getArray(), arrayStringList), "[",
                node.childReturnValueAt(node.CHILD_INDEX_INDEX), "]"});
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        LinkedList objectStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_OBJECT);

        return TNLManip.arrayToList(new Object[] {
            _parenExpr(node.getObject(), objectStringList), ".",
                node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"super.",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_FTYPE), ".",
                node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"this.",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_FTYPE), ".class"});
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);

        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_METHOD), "(",
                _commaList(argsList), ")"});
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode enclosingInstance = node.getEnclosingInstance();

        int enclosingID = enclosingInstance.classID();

        if ((enclosingID != ABSENTTREENODE_ID) && (enclosingID != THISNODE_ID)) {

            LinkedList enclosingStringList = (LinkedList)
                node.childReturnValueAt(node.CHILD_INDEX_ENCLOSINGINSTANCE);

            retList.addLast(_parenExpr(enclosingInstance, enclosingStringList));
            retList.addLast(".");
        }

        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);

        retList.addLast("new ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_DTYPE));
        retList.addLast("(");
        retList.addLast(_commaList(argsList));
        retList.addLast(")");

        return retList;
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {

        LinkedList retList = new LinkedList();

        retList.addLast("new ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_DTYPE));

        List dimExprList = (List) node.childReturnValueAt(node.CHILD_INDEX_DIMEXPRS);

        Iterator dimExprItr = dimExprList.iterator();

        while (dimExprItr.hasNext()) {
            retList.addLast("[");
            retList.addLast(dimExprItr.next());
            retList.addLast("]");
        }

        for (int dimsLeft = node.getDims(); dimsLeft > 0; dimsLeft--) {
            retList.addLast("[]");
        }

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_INITEXPR));
        }

        return retList;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode enclosingInstance = node.getEnclosingInstance();

        int enclosingID = enclosingInstance.classID();

        if ((enclosingID != ABSENTTREENODE_ID) && (enclosingID != THISNODE_ID)) {

            LinkedList enclosingStringList = (LinkedList)
                node.childReturnValueAt(node.CHILD_INDEX_ENCLOSINGINSTANCE);

            retList.addLast(_parenExpr(enclosingInstance, enclosingStringList));
            retList.addLast(".");
        }

        retList.addLast("new ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_SUPERTYPE));
        retList.addLast("(");
        retList.addLast(
                _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_SUPERARGS)));
        retList.addLast(") {\n");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_MEMBERS));
        retList.addLast("}\n");

        return retList;
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
        LinkedList exprStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR);

        return TNLManip.arrayToList(new Object[] {"(",
                                                      node.childReturnValueAt(node.CHILD_INDEX_DTYPE), ") ",
                                                      _parenExpr(node.getExpr(), exprStringList)});
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
        LinkedList exprStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR);

        return TNLManip.arrayToList(new Object[] {
            _parenExpr(node.getExpr(), exprStringList), " instanceof ",
                node.childReturnValueAt(node.CHILD_INDEX_DTYPE)});
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
        LinkedList e1StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        LinkedList e2StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);
        LinkedList e3StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR3);

        e1StringList = _parenExpr(node.getExpr1(), e1StringList);
        e2StringList = _parenExpr(node.getExpr2(), e2StringList);
        e3StringList = _parenExpr(node.getExpr3(), e2StringList);

        return TNLManip.arrayToList(new Object[] {e1StringList, " ? ",
                                                      e2StringList, " : ", e3StringList});
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_EXPR1), " = ",
                node.childReturnValueAt(node.CHILD_INDEX_EXPR2)});
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

    protected LinkedList _visitVarInitDeclNode(VarInitDeclNode node) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_DEFTYPE));
        retList.addLast(" " + node.getName().getIdent());

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" = ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_INITEXPR));
        }

        retList.addLast(";\n");

        return retList;
    }

    protected LinkedList _visitSingleExprNode(SingleExprNode node, String opString,
            boolean post) {
        LinkedList exprStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR);

        exprStringList = _parenExpr(node.getExpr(), exprStringList);

        if (post) {
            return TNLManip.arrayToList(new Object[] {exprStringList, opString});
        }

        return TNLManip.arrayToList(new Object[] {opString, exprStringList});
    }

    protected LinkedList _visitBinaryOpNode(BinaryOpNode node, String opString) {
        LinkedList e1StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        LinkedList e2StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);

        e1StringList = _parenExpr(node.getExpr1(), e1StringList);
        e2StringList = _parenExpr(node.getExpr2(), e2StringList);

        return TNLManip.arrayToList(new Object[] {e1StringList, " ",
                                                      opString, " ", e2StringList});
    }

    protected LinkedList _visitBinaryOpAssignNode(BinaryOpAssignNode node, String opString) {
        List e1StringList = (List) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        List e2StringList = (List) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);

        return TNLManip.arrayToList(new Object[] {e1StringList , " ",
                                                      opString, " ", e2StringList});
    }

    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // for testing purposes only
        return new LinkedList();
    }

    protected static LinkedList _commaList(List stringList) {
        return _separateList(stringList, ", ");
    }

    protected static LinkedList _separateList(List stringList,
            String separator) {
        Iterator stringListItr = stringList.iterator();
        LinkedList retList = new LinkedList();

        while (stringListItr.hasNext()) {
            retList.addLast(stringListItr.next());
            if (stringListItr.hasNext()) {
                retList.addLast(separator);
            }
        }

        return retList;
    }

    protected List _forInitStringList(List list) {
        int length = list.size();

        if (length <= 0) return TNLManip.cons("");

        TreeNode firstNode = (TreeNode) list.get(0);

        if (firstNode.classID() == LOCALVARDECLNODE_ID) {
            // a list of local variables, with the same type and modifier
            LocalVarDeclNode varDeclNode = (LocalVarDeclNode) firstNode;
            LinkedList retList = new LinkedList();

            retList.addLast(Modifier.toString(varDeclNode.getModifiers()));

            retList.addLast(varDeclNode.getDefType().accept(this, null));
            retList.addLast(" ");

            Iterator declNodeItr = list.iterator();

            while (declNodeItr.hasNext()) {
                LocalVarDeclNode declNode = (LocalVarDeclNode) declNodeItr.next();
                retList.addLast(declNode.getName().getIdent());

                TreeNode initExpr = declNode.getInitExpr();
                if (initExpr != AbsentTreeNode.instance) {
                    retList.addLast(" = ");
                    retList.addLast(initExpr.accept(this, null));
                }

                if (declNodeItr.hasNext()) {
                    retList.addLast(", ");
                }
            }

            return retList;

        } else {
            return _separateList(
                    TNLManip.traverseList(this, null, list), "; ");
        }
    }

    protected static LinkedList _parenExpr(TreeNode expr, LinkedList exprStrList) {
        int classID = expr.classID();

        switch (classID) {
        case INTLITNODE_ID:
        case LONGLITNODE_ID:
        case FLOATLITNODE_ID:
        case DOUBLELITNODE_ID:
        case BOOLLITNODE_ID:
        case CHARLITNODE_ID:
        case STRINGLITNODE_ID:
        case BOOLTYPENODE_ID:
        case ARRAYINITNODE_ID:
        case NULLPNTRNODE_ID:
        case THISNODE_ID:
        case ARRAYACCESSNODE_ID:
        case OBJECTNODE_ID:
        case OBJECTFIELDACCESSNODE_ID:
        case SUPERFIELDACCESSNODE_ID:
        case TYPEFIELDACCESSNODE_ID:
        case THISFIELDACCESSNODE_ID:
        case TYPECLASSACCESSNODE_ID:
        case OUTERTHISACCESSNODE_ID:
        case OUTERSUPERACCESSNODE_ID:
        case METHODCALLNODE_ID:
        case ALLOCATENODE_ID:
        case ALLOCATEANONYMOUSCLASSNODE_ID:
        case ALLOCATEARRAYNODE_ID:
        case POSTINCRNODE_ID:
        case POSTDECRNODE_ID:
        case UNARYPLUSNODE_ID:
        case UNARYMINUSNODE_ID:
        case PREINCRNODE_ID:
        case PREDECRNODE_ID:
        case COMPLEMENTNODE_ID:
        case NOTNODE_ID:
            return exprStrList;

        default:
            return TNLManip.arrayToList(new Object[] {"(", exprStrList, ")"});
        }
    }

    protected static String _stringListToString(List stringList) {
        Iterator stringItr = stringList.iterator();
        StringBuffer sb = new StringBuffer();

        while (stringItr.hasNext()) {
            Object stringObj = stringItr.next();

            if (stringObj instanceof List) {
                // only use separators for top level
                sb.append(_stringListToString((List) stringObj));
            } else if (stringObj instanceof String) {
                sb.append((String) stringObj);
            } else {
                throw new IllegalArgumentException(
                        "unknown object in string list : " + stringObj);
            }
        }

        return sb.toString();
    }
}
