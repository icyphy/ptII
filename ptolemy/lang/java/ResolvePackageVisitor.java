/*
A JavaVisitor that adds the names of the types defined in the CompileUnitNode
to the file environment, creates the environments for all nodes,
then resolves names of the imports with a ResolveImportsVisitor.

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

import ptolemy.lang.*;
import java.util.LinkedList;

public class ResolvePackageVisitor extends ResolveVisitorBase {

    ResolvePackageVisitor() {
        super();
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _pkgDecl = (PackageDecl) node.getDefinedProperty("thePackage");

        Environ environ = (Environ) node.getDefinedProperty("environ");

        _pkgEnv  = (Environ) environ.parent();

        LinkedList childArgs = new LinkedList();
        childArgs.addLast(environ);            // enclosing environment =
                                               // file environment
        childArgs.addLast(Boolean.FALSE);      // inner class = false
        childArgs.addLast(NullValue.instance); // no enclosing decl

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());

        node.accept(new ResolveImportsVisitor(), null);

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, true);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        _visitNode(node.getBody(), env);
        return null;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        _visitNode(node.getBody(), env);
        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, false);
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        _visitList(node.getStmts(), env);
        return null;
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        _visitNode(node.getStmt(), env);
        return null;
    }

    public Object visitCatchNode(CatchNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        _visitNode(node.getBlock(), env);
        return null;
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        _visitNode(node.getStmt(), env);
        return null;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        ClassDecl decl = new ClassDecl("<anon>", null);        
        decl.setSource(node);
        decl.setEnviron(env);

        node.setProperty("decl", decl);

        LinkedList listArgs = new LinkedList();
        listArgs.addLast(env);                  // last environment
        listArgs.addLast(Boolean.TRUE);         // inner class = true
        listArgs.addLast(NullValue.instance);   // no enclosing decl
        TNLManip.traverseList(this, null, listArgs, node.getMembers());
        
        return null;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // just pass on args as is
        TNLManip.traverseList(this, node, args, node.children());
        return null;
    }

    protected Object _visitUserTypeDeclNode(UserTypeDeclNode node,
     LinkedList args, boolean isClass) {
        Environ encEnv = (Environ) args.get(0);

        // inner class change
        boolean isInner = ((Boolean) args.get(1)).booleanValue();

        String className = ((NameNode) node.getName()).getIdent();

        Decl other = encEnv.lookupProper(className);

        if (other != null) {
           ApplicationUtility.error("attempt to redefine " + other.getName() +
           " as a class");
        }

        ClassDecl ocl = (ClassDecl) _pkgEnv.lookupProper(className);

        if ((ocl != null) &&
            ((ocl.getSource() == null) ||
             (ocl.getSource() == AbsentTreeNode.instance))) {
           // Assume this is the definition of 'other'
           ocl.setSource(node);
           ocl.setModifiers(node.getModifiers());

        } else {
           int modifiers = node.getModifiers();
           JavaDecl encDecl;
           // boolean topLevel = !isInner || (modifiers & Modifier.STATIC_MOD);

           //if (topLevel) {
           if (!isInner) {
              encDecl = _pkgDecl;
           } else {
              Object encDeclObject = args.get(2);
              if (encDeclObject == NullValue.instance) {
                 encDecl = null;
              } else {
                 encDecl = (JavaDecl) encDeclObject; // enclosing decl
              }
           }

           ClassDecl cl = new ClassDecl(className,
            isClass ? JavaDecl.CG_CLASS : JavaDecl.CG_INTERFACE,
            null, node.getModifiers(), node, encDecl);

           if (ocl != null)  { // Redefinition in same package.
              ApplicationUtility.error("user type name " + className +
               " conflicts with " + ocl.getName() + " in same package");
           }

           //if (topLevel) {
           if (!isInner) {
  	           _pkgEnv.add(cl);
           }

           ocl = cl;
        }

        // fix category if this is an interface
        if (!isClass) {
           ocl.category = JavaDecl.CG_INTERFACE;
        }

        Environ env = new Environ(encEnv);
        node.setProperty("environ", env);

        ocl.setEnviron(env);
        encEnv.add(ocl);

        node.getName().setProperty("decl", ocl);

        LinkedList memberArgs = new LinkedList();
        memberArgs.addLast(env);                  // environment for this class
        memberArgs.addLast(Boolean.TRUE);         // inner class = true
        memberArgs.addLast(ocl);                  // last class decl
        TNLManip.traverseList(this, node, memberArgs, node.getMembers());

        return null;
    }

    protected Environ _makeEnviron(TreeNode node, LinkedList args) {
        Environ encEnv = (Environ) args.get(0);

        Environ env = new Environ(encEnv);
        node.setProperty("environ", env);

        return env;
    }

    protected void _visitNode(TreeNode node, Environ env) {
        LinkedList nodeArgs = new LinkedList();
        nodeArgs.addLast(env);                  // last environment
        nodeArgs.addLast(Boolean.TRUE);         // inner class = true
        nodeArgs.addLast(NullValue.instance);   // no enclosing decl
        node.accept(this, nodeArgs);
    }

    protected void _visitList(LinkedList nodeList, Environ env) {
        LinkedList listArgs = new LinkedList();
        listArgs.addLast(env);                  // last environment
        listArgs.addLast(Boolean.TRUE);         // inner class = true
        listArgs.addLast(NullValue.instance);   // no enclosing decl
        TNLManip.traverseList(this, null, listArgs, nodeList);
    }

    /** The package this compile unit is in. */
    protected PackageDecl _pkgDecl = null;

    /** The package environment. */
    protected Environ _pkgEnv = null;
}
