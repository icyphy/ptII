/*
A JavaVisitor that resolves class or interface type declarations.

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

import ptolemy.lang.*;
import java.util.LinkedList;

public class ResolveTypesVisitor extends JavaVisitor {

    /** Create a ResolveTypesVisitor. */
    ResolveTypesVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitNameNode(NameNode node, LinkedList args) {
        return null;
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return null;
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return null;
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return null;
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return null;
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return null;
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return null;
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return null;
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return null;
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return null;
    }

    /** Resolve the name of the type. */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        PackageDecl pkgDecl = (PackageDecl) args.get(0);
        Environ env = (Environ) args.get(1);

        NameNode newName = (NameNode) StaticResolution.resolveAName(
         node.getName(), env, null, false, pkgDecl, JavaDecl.CG_USERTYPE);

        // this is not necessary, but by convention ...
        node.setName(newName);

        return null;
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return null;
    }

    /** Visit the types defined in this file. */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(node.getDefinedProperty("thePackage"));  // package decl
        childArgs.addLast(node.getDefinedProperty("environ"));

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());
        
        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return null;
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        return null;
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        return null;
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return null;
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return null;
    }

    /** The default visit method.
     *  Visit all child nodes.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        TNLManip.traverseList(this, node, args, node.children());
        return null;
    }

    protected Object _visitUserTypeNode(UserTypeDeclNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(args.get(0)); // package decl
        childArgs.addLast(node.getDefinedProperty("environ"));

        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        return null;
    }
}