/*
A JavaVisitor that generates C header (.h) file from a Java abstract 
syntax tree.

Copyright (c) 2001 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.lang.c;

import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;


/** A C code generator that generates a C header (.h) file from a Java abstract 
 *  syntax tree
 *
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 *
 */

// FIXME: Some visitation results are never used. For example
// an assignment statement within a method declaration will
// not be used in a header file. For more efficinet operation,
// this class should be streamlined to eliminate unnecesary
// computation within node visits.


public class HeaderFileGenerator extends CCodeGenerator {
    
    /** Configure the code generator to import header (.h) files 
     *  only for references to user-defined types that are made
     *  outside of code blocks (BlockNode subtrees).
     */
    public HeaderFileGenerator() {
        super();
        _importForAllTypeNames = false;
    }


    public Object visitImportNode(ImportNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"import ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME), ";\n"});
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {

        return TNLManip.arrayToList(new Object[] {"import ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME), ".*;\n"});
    }


    /** Generate C code for a Java class declaration. 
     *  Code for two struct-based type definitions is generated here.
     *  One type corresponds to the class itself (class variables, 
     *  function pointers to methods, etc.), and the other type
     *  is for instances (variables) of the class.
     *  @param node The AST node of the Java class declaration that is
     *  to be translated to C. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given class declaration.
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        // debug
        TreeNode parent = node.getParent();
        System.out.println("ClassDecl parent class is: " + 
                parent.getClass().getName());

        // An iterator that sweeps through all different member declarations
        // (declarations for fields, methods, constructors, etc.) of the
        // given Java class declaration.
        Iterator membersIter;

        // Extract the unique type name to use for the class
        String className = CNameGenerator.getCNameOf(node);

        // Avoid multiple inclusions of the generated header file
        _headerCode.addLast("\n#ifndef _" + className + "_h\n");
        _headerCode.addLast("#define _" + className + "_h\n\n");
        _footerCode.addLast("\n#endif\n\n");

        // Generate the type declaration header for the class
        // structure
        retList.addLast("/* Structure that implements Class ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_NAME));
        retList.addLast(" */\n");
        retList.addLast("typedef struct ");
        retList.addLast(" {\n\n");

        // Generate the method table
        retList.addLast(_indent(1) + "struct {\n");
        MethodListGenerator generator = new MethodListGenerator(node);
        retList.addLast(_generateMethodPointers(node, 
                generator.getInheritedMethods(),
                "Inherited/overridden methods"));
        retList.addLast(_generateMethodPointers(node,
                generator.getNewMethods(),
                "New methods"));
        retList.addLast(_generateMethodPointers(node,
                generator.getPrivateMethods(),
                "Private methods"));
        retList.addLast(_indent(1) + "} methods;\n");
 
        // FIXME: Figure out what needs to be done for super classes,
        // modifiers, and interfaces.

        // Extract the declarations of member methods, fields, constructors,
        // etc.  
        List members = node.getMembers();
   
        // Extract the fields, and insert them into the struct
        // that is declared to implement the class. Methods will
        // be implemented as separate functions outside the struct.
        membersIter = members.iterator();
        while (membersIter.hasNext()) {
            Object member = membersIter.next();
            if (member instanceof FieldDeclNode) {
                FieldDeclNode fieldMember = (FieldDeclNode)member;
                retList.addLast(_indent(1));
                retList.addLast(fieldMember.returnValueAsElement());
            } 
        }

        // Terminator for declared type.
        retList.addLast("\n} *" + className + ";\n\n");    


        // Declare each constructor of the Java class as a separate C function.
        // Include only the function prototype in the header file.
        // FIXME: Implement this, but only for constructors that need
        // prototypes in the header file.
        /*
        membersIter = members.iterator();
        int constructorCount = 0;
        while (membersIter.hasNext()) {
            Object member = membersIter.next();
            if (member instanceof ConstructorDeclNode) {
                if ((++constructorCount)==1) {
                    retList.addLast(_comment("Class constructors") + "\n");   
                }
                ConstructorDeclNode declaration = (ConstructorDeclNode)member;
                retList.addLast(declaration.returnValueAsElement());
            }
        }
        */

        // Declare each method of the Java class as a separate C function.
        // Include only the function prototype in the header file.
		// FIXME: implement this, but do it only for static methods.
        // Regular (non-static methods) do not need prototypes in the
        // header file.
        /*
        membersIter = members.iterator();
        int methodCount = 0;
        while (membersIter.hasNext()) {
            Object member = membersIter.next();
            if (member instanceof MethodDeclNode) {
                if ((++methodCount)==1) {
                    if (constructorCount > 0) retList.addLast("\n");
                    retList.addLast(_comment("Member methods") + "\n");   
                }
                MethodDeclNode declaration = (MethodDeclNode)member;
                retList.addLast(declaration.returnValueAsElement());
            }
        }
        */


        return retList;
    }


    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        System.out.println("debug field decl: ");
        _printStringList(_visitVarInitDeclNode(node)); 
        System.out.println("debug field decl end");

        return _visitVarInitDeclNode(node);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    /** Generate C code for the function prototype associated with a
     *  method declaration.
     *  @param node The AST node corresponding to a method declaration.
     *  @param args Unused placeholder for visitor arguments.
     *  @return C code for the prototype of a function that implements
     *  the declared method.
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("extern ");
        retList.addLast(_generateMethodHeader(node));
        retList.addLast(";\n");

        return retList;
    }


    /** Generate C code for the function prototype associated with a
     *  constructor declaration.
     *  @param node The AST node corresponding to a constructor declaration.
     *  @param args Unused placeholder for visitor arguments.
     *  @return C code for the prototype of a function that implements
     *  the declared constructor.
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node, 
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("extern ");
        retList.addLast(_generateConstructorHeader(node));
        retList.addLast(";\n");

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

    /** Generate C code for a Java block. 
     *  @param node The AST node of the Java block that is 
     *  to be translated to C. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given block.
     */
    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
                _indent(node), "{\n",
                node.childReturnValueAt(node.CHILD_INDEX_STMTS), 
                _indent(node), "}\n"});
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return TNLManip.addFirst(";\n");
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_NAME),
                ": ",
                node.childReturnValueAt(node.CHILD_INDEX_STMT)});
    }

    /** Generate C code for a Java if statement (including the else
     *  part, if applicable.
     *  @param node The AST node of the Java if statement that is
     *  to be translated to C. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given if statement.
     */
    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(_indent(node));
        retList.addLast("if (");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_CONDITION));
            retList.addLast(") ");
        _generateStmtOrBlock(node, node.CHILD_INDEX_THENPART, retList);

        TreeNode elsePart = node.getElsePart();
        if (elsePart != AbsentTreeNode.instance) {
            retList.addLast(_indent(node));
            retList.addLast("else ");
            _generateStmtOrBlock(node, node.CHILD_INDEX_ELSEPART, retList);
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
            return TNLManip.addFirst("default:\n");
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
        return TNLManip.arrayToList(new Object[] {_indent(node), "return ",
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
        return TNLManip.addFirst("null");
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return TNLManip.addFirst("this");
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
        return TNLManip.arrayToList(new Object[] {"this->",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_FTYPE), ".class"});
    }


    /** Generate C code for a method call.
     *
     *  @param node The AST node of the method call to be translated. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given method call.
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);

        // Object methodName = node.childReturnValueAt(node.CHILD_INDEX_METHOD);
        // System.out.println("------------- debug -----------------");
        // System.out.println(methodName.getClass().getName());
        // System.out.println(methodName.toString());
        // System.out.println("------------- debug -----------------");
  
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
        LinkedList retList = new LinkedList();
        retList.addLast(_indent(node));
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_EXPR1));
        retList.addLast(" = ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_EXPR2));
        return retList;
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
        
        retList.addLast(_indent(node));
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

        if (length <= 0) return TNLManip.addFirst("");

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


    /** Print information pertaining to a child of a node in
     *  in an abstract syntax tree. The information printed for
     *  each child tree node includes object class information, 
     *  and the return value of the last visit to the node. 
     *
     *  Example call: 
     *  _debugChildReturnValue(classNode, classNode.CHILD_INDEX_MEMBERS)
     * 
     *  @param node The parent of the AST child whose information is
     *  to be printed. 
     *  @index The index of the child to be printed. If the index
     *  references a "hierarchical" tree node (a list), then information 
     *  pertaining to all elements of the list will be printed. 
     */
    protected void _debugChildReturnValue(TreeNode node, int index) {
        Object child = node.getChild(index);
        Object childReturnVal = node.childReturnValueAt(index);
        System.out.println("------- ccg debug: child information --------");
        System.out.println("Child class: " + child.getClass().getName());
        System.out.println("Child return value: ");
        if (childReturnVal instanceof List) {
            System.out.println(_stringListToString((List)childReturnVal));
        }
        if (child instanceof List) {  
            System.out.print("The child is a list. ");
            System.out.println("A description of each list element follows.");
            Iterator elements = ((List)child).iterator();
            while (elements.hasNext()) {
                Object element = elements.next();
                System.out.print("List element class: ");
                System.out.println(element.getClass().getName());
            }
        }
        System.out.println("----------------------------------------------");
    }

    /**
     *  Generate C code for a tree node that can be either a statement
     *  or a block (e.g., the then part of an if statement).
     *  The code is added to the given linked list. Code for
     *  a statement is placed on a separate line, and indented 
     *  by one position beyond the indentation
     *  effected by the associated node indentation levels.
     *  @param parent The parent of the statement-or-node block for
     *  which C code is to be generated.
     *  @param index The index of the child for which code is to be generated.
     *  @param retList The linked list to which the generated code is to
     *  be appended.
     */
    protected void _generateStmtOrBlock(TreeNode parent, int childIndex, 
            LinkedList retList) {
        Object child = parent.getChild(childIndex);
        if ((parent == null) || (child == null)) return;

        if (child instanceof BlockNode) {
            retList.addLast(parent.childReturnValueAt(childIndex));
        }
        else {
            retList.addLast("\n");
            retList.addLast(_indent(1));
            retList.addLast(parent.childReturnValueAt(childIndex));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Enclose a given string of text within appropriate delimiters to
     *  form a comment in the generated code.
     *  @param text The text to place in the generated comment.
     *  @return The generated comment.
     */  
    private final String _comment(String text) {
        return("/* " + text + " */");
    }

    /** Given a list of Java methods, generate code that declares function 
     *  pointers corresponding to the methods in the list.
     *  The format of a method pointer declaration is as follows:
     *
     *  functionReturnType (*functionName)(paramOneType, paramTwoType, ...);
     *
     *  The function pointer (method table structure field) is given the
     *  same name as the function that it points to.
     *
     *  @param node The AST node for the enclosing Java class (the class
     *  that contains the methods in the methods list).
     *  @param methodList The list of methods.
     *  @param comment A comment to insert in the generated code. This comment
     *  is inserted at the beginning, before the methods are declared.
     *  @return Function pointer code for specified Java methods.
     */
    LinkedList _generateMethodPointers(ClassDeclNode node, LinkedList methodList, 
            String comment) {
        LinkedList returnList = new LinkedList();
        if (methodList.isEmpty()) return returnList;
        final String indent = _indent(2);
        returnList.addLast("\n" + indent + _comment(comment) + "\n");
        Iterator methods = methodList.iterator();
        while (methods.hasNext()) {
            Object methodElement = methods.next();
            if (!(methodElement instanceof MethodDecl))
                nodeVisitationError(node, "Invalid method list entry.");
            MethodDecl method = (MethodDecl)methodElement;
            TreeNode methodSource = method.getSource();
            if (!(methodSource instanceof MethodDeclNode))
                nodeVisitationError(node, 
                        "Invalid method source node reference.");
            MethodDeclNode methodNode = (MethodDeclNode)methodSource;
            returnList.addLast(indent);
            returnList.addLast(CNameGenerator.getCNameOf(methodNode.getReturnType()));
            returnList.addLast(" (*");
            returnList.addLast(CNameGenerator.getCNameOf(methodNode));
            returnList.addLast(")(");

            // Generate code for the parameter type list.
            Iterator parameters = method.getParams().iterator();
            int numberOfParameters = 0;
            while (parameters.hasNext()) {
                if ((++numberOfParameters) > 1)
                    returnList.addLast(", ");
                Object parameterElement = parameters.next();
                if (!(parameterElement instanceof TypeNode))
                    nodeVisitationError(node, "Invalid parameter list entry.");
                returnList.addLast(CNameGenerator.getCNameOf((TypeNode)parameterElement));
            }

            returnList.addLast(");\n");
        }
        System.out.println("begin debug");
        _printStringList(returnList);
        System.out.println("end debug");
        return returnList;
    }


    /** Return a string that generates an indentation string (a sequence
     *  of spaces) for the given indentation level. Each indentation
     *  level unit is four characters wide.
     *  @param level The indentation level.
     *  @return The indentation string that corresponds to the given 
     *  indentation level.
     */  
    private String _indent(int level) {
        StringBuffer indent = new StringBuffer();
        int i;
        for (i = 0; i < level; i++) {
            indent.append("    ");
        }
        return indent.toString();
    }
   
    /** Return a string that generates an indentation string (a sequence
     *  of spaces) based on the indentation level of the given AST node. 
     *  @param node The AST node.
     *  @return The indentation string that corresponds to the 
     *  indentation level of the given AST node.
     */  
    private String _indent(TreeNode node) {
        int level = ((Integer)(node.getProperty(INDENTATION_KEY))).intValue();
        return _indent(level);
    }

}
