/* Resolve names of local variables, formal parameters, field accesses,
method calls, and statement labels.

Copyright (c) 1998-2001 The Regents of the University of California.
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

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A visitor that does name resolution.
After this phase, all fields and methods are referred to via
ThisFieldAccessNode, SuperFieldAccessNode or ObjectFieldAccessNode.
ObjectNode is only used for local variables and parameters.

The decl in methods may be wrong, because overloading resolution is
done later (when types become available)

Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class ResolveNameVisitor extends ReplacementJavaVisitor
    implements JavaStaticSemanticConstants {
    public ResolveNameVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return node;
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        System.out.print("Resolve name on ");
        Object identProperty = node.getProperty(IDENT_KEY);
        String identString = (identProperty == null) ? 
               "<unidentified>" : (String)identProperty;
        System.out.println(identString);
        
        _currentPackage = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        NameContext c = new NameContext();
        c.scope = (Scope) node.getDefinedProperty(SCOPE_KEY);

        LinkedList childArgs = TNLManip.addFirst(c);

        TNLManip.traverseList(this, childArgs, node.getDefTypes());

        System.out.println("Finished resolve name on " + identString);
                
        return node;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        node.setInitExpr((TreeNode) node.getInitExpr().accept(this, args));

        NameContext ctx = (NameContext) args.get(0);
        Scope scope = ctx.scope;

        NameNode name = node.getName();
        String varName = name.getIdent();

        Decl other = scope.lookup(varName, CG_FORMAL);

        if (other != null) {
            throw new RuntimeException("declaration shadows " + varName);
        }

        other = scope.lookupLocal(varName, CG_LOCALVAR);

        if (other != null) {
            throw new RuntimeException("redeclaration of " + varName);
        }

        LocalVarDecl d = new LocalVarDecl(varName, node.getDefType(),
                node.getModifiers(), node);

        scope.add(d);
        name.setProperty(DECL_KEY, d);

        return node;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();

        subCtx.encLoop = null;
        subCtx.breakTarget = null;

        Scope newScope1 = new Scope(ctx.scope);
        subCtx.scope = newScope1;

        LinkedList childArgs = TNLManip.addFirst(subCtx);

        node.setParams(TNLManip.traverseList(this, childArgs,
                node.getParams()));

        TreeNode body = node.getBody();
        subCtx.scope = new Scope(newScope1);

        if (body != AbsentTreeNode.instance) {
            node.setBody((BlockNode) body.accept(this, childArgs));
        }

        return node;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();

        subCtx.encLoop = null;
        subCtx.breakTarget = null;

        Scope newScope1 = new Scope(ctx.scope);
        subCtx.scope = newScope1;

        LinkedList childArgs = TNLManip.addFirst(subCtx);

        node.setParams(TNLManip.traverseList(this, childArgs,
                node.getParams()));

        subCtx.scope = new Scope(newScope1);

        node.setConstructorCall((ConstructorCallNode)
                node.getConstructorCall().accept(this, childArgs));

        node.setBody((BlockNode) node.getBody().accept(this, childArgs));

        return node;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        Scope scope = ctx.scope;

        NameNode name = node.getName();
        String varName = name.getIdent();

        if (StaticResolution.debugLoading) {
            System.out.println("ResolveNameVisitor.visitParameterNode: "
                    + "varName = " + varName + ".\nThe parameter's AST follows.\n"
                    + node.toString());
        }

        Decl other = scope.lookup(varName, CG_FORMAL | CG_LOCALVAR);

        if (other != null) {
            throw new RuntimeException("declaration shadows " + varName);
        }

        FormalParameterDecl d = new FormalParameterDecl(varName,
                node.getDefType(), node.getModifiers(), node);

        name.setProperty(DECL_KEY, d);
        scope.add(d);

        return node;
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        NameContext subctx = (NameContext) ctx.clone();
        subctx.scope = new Scope(ctx.scope);

        node.setStmts(TNLManip.traverseList(this, TNLManip.addFirst(subctx),
                node.getStmts()));

        return node;
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        NameNode label = node.getName();
        String labelString = label.getIdent();

        Decl other = ctx.scope.lookup(labelString, CG_STMTLABEL);

        Scope newScope = new Scope(ctx.scope);

        if (other != null) {
            throw new RuntimeException("duplicate " + labelString);
        }

        StmtLblDecl d = new StmtLblDecl(labelString, node);

        label.setProperty(DECL_KEY, d);

        newScope.add(d);

        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.scope = newScope;

        LinkedList childArgs = TNLManip.addFirst(subCtx);

        node.setStmt((StatementNode) node.getStmt().accept(this, childArgs));
        return node;
    }

    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();

        node.setExpr((ExprNode) node.getExpr().accept(this, args));

        subCtx.breakTarget = node;
        subCtx.scope = new Scope(ctx.scope);

        node.setSwitchBlocks(
                TNLManip.traverseList(this, TNLManip.addFirst(subCtx),
                        node.getSwitchBlocks()));

        return node;
    }

    public Object visitLoopNode(LoopNode node, LinkedList args) {
        node.setTest((ExprNode) node.getTest().accept(this, args));

        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();

        subCtx.breakTarget = node;
        subCtx.encLoop = node;
        subCtx.scope = new Scope(ctx.scope);

        LinkedList childArgs = TNLManip.addFirst(subCtx);
        node.setForeStmt((TreeNode) node.getForeStmt().accept(this, childArgs));
        node.setAftStmt((TreeNode) node.getAftStmt().accept(this, childArgs));

        return node;
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();

        subCtx.scope = new Scope(ctx.scope);

        LinkedList childArgs = TNLManip.addFirst(subCtx);

        node.setInit(TNLManip.traverseList(this, childArgs, node.getInit()));
        subCtx.breakTarget = node;
        subCtx.encLoop = node;

        node.setTest((ExprNode) node.getTest().accept(this, childArgs));
        node.setUpdate(TNLManip.traverseList(this, childArgs, node.getUpdate()));
        node.setStmt((StatementNode) node.getStmt().accept(this, childArgs));

        return node;
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        if ((node.getLabel() == AbsentTreeNode.instance) &&
                (ctx.breakTarget == null)) {
            throw new RuntimeException("unlabeled break only allowed in loops or switches");
        }

        _resolveJump(node, ctx.breakTarget, ctx.scope);

        return node;
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        if (ctx.encLoop == null) {
            throw new RuntimeException("unlabeled continue only allowed in loops");
        }

        _resolveJump(node, ctx.encLoop, ctx.scope);

        if (node.hasProperty(JUMP_DESTINATION_KEY)) {

            StatementNode dest = (StatementNode)
                node.getDefinedProperty(JUMP_DESTINATION_KEY);

            if (!(dest instanceof IterationNode)) {
                throw new RuntimeException("continue's target is not a loop");
            }
        }

        return node;
    }

    public Object visitCatchNode(CatchNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.scope = new Scope(ctx.scope);

        LinkedList childArgs = TNLManip.addFirst(subCtx);

        node.setParam((ParameterNode) node.getParam().accept(this, childArgs));
        node.setBlock((BlockNode) node.getBlock().accept(this, childArgs));

        return node;
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        node.setProperty(THIS_CLASS_KEY, ctx.currentClass);

        return node;
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameNode name = node.getName();

        
        if (StaticResolution.debugLoading) {
            System.out.println("Calling resolveAName from " + 
                    "ResolveNameVisitor.visitObjectNode(" + 
                    name.getIdent() + ")");
        }
        return StaticResolution.resolveAName(name, ctx.scope,
                ctx.currentClass, _currentPackage,
                ctx.resolveAsObject ? (CG_FIELD | CG_LOCALVAR | CG_FORMAL) : CG_METHOD);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        NameContext subCtx = (NameContext) ((NameContext) args.get(0)).clone();
        subCtx.resolveAsObject = true;

        node.setObject((ExprNode) (node.getObject().accept(this, TNLManip.addFirst(subCtx))));

        return node;
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        node.setProperty(THIS_CLASS_KEY, ctx.currentClass);

        return node;
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        NameContext subCtx = (NameContext) ((NameContext) args.get(0)).clone();

        subCtx.resolveAsObject = true;

        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // CHECK ME : is this all that needs to be done?

        return node;
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        node.setProperty(THIS_CLASS_KEY, ctx.currentClass);

        return node;
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        node.setArgs(TNLManip.traverseList(this, args, node.getArgs()));

        NameContext subCtx = (NameContext) ((NameContext) args.get(0)).clone();
        subCtx.resolveAsObject = false;

        node.setMethod((ExprNode) node.getMethod().accept(this, TNLManip.addFirst(subCtx)));

        return node;
    }

    /* The default visit method comes from ReplacementJavaVisitor. */

    protected Object _visitUserTypeDeclNode(UserTypeDeclNode node,
            LinkedList args) {
        NameContext  ctx = new NameContext();

        ClassDecl decl = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        ctx.scope = decl.getScope();
        ctx.currentClass = decl.getDefType();

        LinkedList childArgs = TNLManip.addFirst(ctx);

        node.setMembers(
                TNLManip.traverseList(this, childArgs, node.getMembers()));

        return node;
    }

    protected static JumpStmtNode _resolveJump(JumpStmtNode node, TreeNode noLabel,
            Scope scope) {
        TreeNode label = node.getLabel();

        if (label == AbsentTreeNode.instance) {
            node.setProperty(JUMP_DESTINATION_KEY, noLabel);
        } else {
            NameNode labelName = (NameNode) label;
            String labelString = labelName.getIdent();

            StmtLblDecl dest = (StmtLblDecl)
                scope.lookup(labelString, CG_STMTLABEL);

            if (dest == null) {
                throw new RuntimeException("label " + labelString + " not found");
            }

            labelName.setProperty(DECL_KEY, dest);

            LabeledStmtNode labeledStmtNode = (LabeledStmtNode) dest.getSource();
            node.setProperty(JUMP_DESTINATION_KEY, labeledStmtNode.getStmt());
        }
        return node;
    }

    protected static class NameContext implements Cloneable {
        public NameContext() {}

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new InternalError("clone of NameContext not supported");
            }
        }

        /** The last scope. */
        public Scope scope = null;

        /** The type of the current class. */
        public TypeNameNode currentClass = null;

        public TreeNode breakTarget = null;

        /** The enclosing loop. null if not in a loop. */
        public TreeNode encLoop = null;

        boolean resolveAsObject = true;
    }

    /** The package this compile unit is in. */
    protected PackageDecl _currentPackage = null;
}
