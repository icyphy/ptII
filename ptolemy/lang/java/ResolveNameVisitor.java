/*
Resolve names of local variables, formal parameters, field accesses,
method calls, and statement labels. Code adopted from st-name.cc from
the Titanium project.

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

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A visitor that does name resolution.
 *  
 *  After this phase, all fields and methods are referred to via
 *  ThisFieldAccessNode, SuperFieldAccessNode or ObjectFieldAccessNode. 
 *  ObjectNode is only used for local variables and parameters.
 *  
 *  The decl in methods may be wrong, because overloading resolution is
 *  done later (when types become available) 
 *
 *  Code and comments taken from the Titanium project.
 * 
 *  @author Jeff Tsay
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
        _currentPackage = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        NameContext c = new NameContext();
        c.environ = (Environ) node.getDefinedProperty(ENVIRON_KEY);

        LinkedList childArgs = TNLManip.cons(c);
        
        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());

        return node;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        node.setInitExpr((TreeNode) node.getInitExpr().accept(this, args));

        NameContext ctx = (NameContext) args.get(0);
        Environ env = ctx.environ;

        NameNode name = node.getName();
        String varName = name.getIdent();

        Decl other = env.lookup(varName, CG_FORMAL);

        if (other != null) {
           ApplicationUtility.error("declaration shadows " + varName);
        } else {
           other = env.lookupProper(varName, CG_LOCALVAR);

           if (other != null) {
              ApplicationUtility.error("redeclaration of " + varName);
           }
        }

        LocalVarDecl d = new LocalVarDecl(varName, node.getDefType(),
         node.getModifiers(), node);

        env.add(d);
        name.setProperty(DECL_KEY, d);

        return node;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = new NameContext(ctx);

        subCtx.encLoop = null;
        subCtx.breakTarget = null;
         
        Environ env = (Environ) node.getDefinedProperty(ENVIRON_KEY);
        subCtx.environ = env;
         
        LinkedList childArgs = TNLManip.cons(subCtx);
         
        node.setParams(TNLManip.traverseList(this, node, childArgs, 
         node.getParams()));
                           
        TreeNode body = node.getBody();
        
        if (body != AbsentTreeNode.instance) {                         
           node.setBody((BlockNode) body.accept(this, childArgs)); 
        }
        
        return node;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = new NameContext(ctx);

        subCtx.encLoop = null;
        subCtx.breakTarget = null;
         
        Environ env = (Environ) node.getDefinedProperty(ENVIRON_KEY);
        subCtx.environ = env;
         
        LinkedList childArgs = TNLManip.cons(subCtx);
         
        node.setParams(TNLManip.traverseList(this, node, childArgs, 
         node.getParams()));
                  
        //node.setThrowsList(TNLManip.traverseList(this, node, childArgs,
         // node.getThrowsList()); 
                 
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
        Environ env = ctx.environ;

        NameNode name = node.getName();
        String varName = name.getIdent();

        Decl other = env.lookup(varName, CG_FORMAL | CG_LOCALVAR);

        if (other != null) {
           ApplicationUtility.error("declaration shadows " + varName);
        }

        FormalParameterDecl d = new FormalParameterDecl(varName,
         node.getDefType(), node.getModifiers(), node);

        env.add(d);
        name.setProperty(DECL_KEY, d);

        return node;
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        NameContext subctx = new NameContext((NameContext) args.get(0));

        subctx.environ = (Environ) node.getDefinedProperty(ENVIRON_KEY);

        LinkedList childArgs = TNLManip.cons(subctx);

        node.setStmts(TNLManip.traverseList(this, node, childArgs,
         node.getStmts()));

        return node;
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);

        Environ env = (Environ) node.getDefinedProperty(ENVIRON_KEY);

        NameNode label = node.getName();
        String labelString = label.getIdent();

        Decl other = ctx.environ.lookup(labelString, CG_STMTLABEL);

        if (other != null) {
           ApplicationUtility.error("duplicate " + labelString);
        }

        StmtLblDecl d = new StmtLblDecl(labelString, node);

        label.setProperty(DECL_KEY, d);

        env.add(d);

        NameContext subCtx = new NameContext(ctx);
        subCtx.environ = env;

        LinkedList childArgs = TNLManip.cons(subCtx);

        node.setStmt((StatementNode) node.getStmt().accept(this, childArgs));
        return node;
    }

    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        NameContext subCtx = new NameContext((NameContext) args.get(0));

        node.setExpr((ExprNode) node.getExpr().accept(this, args));
                
        subCtx.breakTarget = node;
        LinkedList childArgs = TNLManip.cons(subCtx);
               
        node.setSwitchBlocks(
         TNLManip.traverseList(this, node, childArgs, node.getSwitchBlocks()));
         
        return node;    
    }

    public Object visitLoopNode(LoopNode node, LinkedList args) {
        node.setTest((ExprNode) node.getTest().accept(this, args));
        
        NameContext subCtx = new NameContext((NameContext) args.get(0));
        subCtx.breakTarget = node;
        subCtx.encLoop = node;

        LinkedList childArgs = TNLManip.cons(subCtx);
        node.setForeStmt((TreeNode) node.getForeStmt().accept(this, childArgs));
        node.setAftStmt((TreeNode) node.getAftStmt().accept(this, childArgs));
        
        return node;
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        NameContext subCtx = new NameContext((NameContext) args.get(0));

        Environ env = (Environ) node.getDefinedProperty(ENVIRON_KEY);
        subCtx.environ = env;
        
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        node.setInit(TNLManip.traverseList(this, node, childArgs, node.getInit()));
        subCtx.breakTarget = node;
        subCtx.encLoop = node;

        node.setTest((ExprNode) node.getTest().accept(this, childArgs));
        node.setUpdate(TNLManip.traverseList(this, node, childArgs, node.getUpdate()));
        node.setStmt((StatementNode) node.getStmt().accept(this, childArgs));

        return node;
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
    
        if ((node.getLabel() == AbsentTreeNode.instance) && 
            (ctx.breakTarget == null)) {
           ApplicationUtility.error("unlabeled break only allowed in loops or switches");
        }

        _resolveJump(node, ctx.breakTarget, ctx.environ);
               
        return node;
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
    
        if (ctx.encLoop == null) {
           ApplicationUtility.error("unlabeled continue only allowed in loops");
        }
        
        _resolveJump(node, ctx.encLoop, ctx.environ);
     
        if (node.hasProperty(JUMP_DESTINATION_KEY)) {
          
           StatementNode dest = (StatementNode) 
            node.getDefinedProperty(JUMP_DESTINATION_KEY);   
           
           if (!(dest instanceof IterationNode)) {
              ApplicationUtility.error("continue's target is not a loop");
           }
        }

        return node;    
    }

    public Object visitCatchNode(CatchNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = new NameContext(ctx);

        Environ env = (Environ) node.getDefinedProperty(ENVIRON_KEY);
        subCtx.environ = env;
         
        LinkedList childArgs = TNLManip.cons(subCtx);
         
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
        
        return StaticResolution.resolveAName(name, ctx.environ, 
         ctx.currentClass, _currentPackage, 
         ctx.resolveAsObject ? (CG_FIELD | CG_LOCALVAR | CG_FORMAL) : CG_METHOD);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        NameContext subCtx = new NameContext((NameContext) args.get(0));        
        subCtx.resolveAsObject = true;
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        node.setObject((ExprNode) (node.getObject().accept(this, childArgs)));
        
        return node;
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        
        node.setProperty(THIS_CLASS_KEY, ctx.currentClass);   
        
        return node;    
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        NameContext subCtx = new NameContext((NameContext) args.get(0));
        subCtx.resolveAsObject = true;
        
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        // not really necessary             
        node.setFType((TypeNameNode) node.getFType().accept(this, childArgs));
        
        return node;    
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        
        node.setProperty(THIS_CLASS_KEY, ctx.currentClass);   
        
        return node;    
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));

        NameContext subCtx = new NameContext((NameContext) args.get(0));
        subCtx.resolveAsObject = false;        
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        node.setMethod((ExprNode) node.getMethod().accept(this, childArgs));
        
        return node;    
    }
    
    /* The default visit method comes from ReplacementJavaVisitor. */
       
    protected Object _visitUserTypeDeclNode(UserTypeDeclNode node,
     LinkedList args) {
        NameContext  ctx = new NameContext();

        ctx.environ = (Environ) node.getDefinedProperty(ENVIRON_KEY);

        ClassDecl decl = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        ctx.currentClass = decl.getDefType();

        LinkedList childArgs = TNLManip.cons(ctx);

        node.setMembers(
         TNLManip.traverseList(this, node, childArgs, node.getMembers()));

        return node;
    }

    protected JumpStmtNode _resolveJump(JumpStmtNode node, TreeNode noLabel, Environ env) {
        TreeNode label = node.getLabel();

        if (label == AbsentTreeNode.instance) {
          node.setProperty(JUMP_DESTINATION_KEY, noLabel);
        } else {
          NameNode labelName = (NameNode) label;
          String labelString = labelName.getIdent();
          
          StmtLblDecl dest = (StmtLblDecl) 
           env.lookup(labelString, CG_STMTLABEL);
          
          if (dest == null) {
             ApplicationUtility.error("label " + labelString + " not found");
          } 
          
          labelName.setProperty(DECL_KEY, dest);
                    
          LabeledStmtNode labeledStmtNode = (LabeledStmtNode) dest.getSource();
          node.setProperty(JUMP_DESTINATION_KEY, labeledStmtNode.getStmt());
        }
        return node;
    }

    protected static class NameContext {
        public NameContext() {}

        public NameContext(NameContext ctx) {
            environ = ctx.environ;
            currentClass = ctx.currentClass;
            breakTarget = ctx.breakTarget;
            encLoop = ctx.encLoop;     
            resolveAsObject = ctx.resolveAsObject;
        }
        
        /** The last environment. */
        public Environ environ = null;
        
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
