/*
A JavaVisitor that adds the names of the types defined in a CompileUnitNode
to the file environment, then resolves names of the imports with a
ResolveImportsVisitor.

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

public class ResolvePackageVisitor extends JavaVisitor {

    ResolvePackageVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        Environ environ = (Environ) node.getDefinedProperty("environ");
        PackageDecl pkgDecl = (PackageDecl) node.getDefinedProperty("thePackage");

        LinkedList childArgs = new LinkedList();
        childArgs.addLast(pkgDecl);            // package declaration
        childArgs.addLast(environ);            // enclosing environment =
                                               // file environment
        childArgs.addLast(new Boolean(false)); // inner class = false
        childArgs.addLast(environ.parent());   // package environment

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());

        /*
        LinkedList rivArgs = new LinkedList();
        rivArgs.addLast(node);
        rivArgs.addLast(environ);

        TNLManip.traverseList(new ResolveImportsVisitor(), node,
         rivArgs, node.getImports());
        */
        node.accept(new ResolveImportsVisitor(), null);

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, true);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, false);
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return null;
    }

    protected Object _visitUserTypeDeclNode(UserTypeDeclNode node,
     LinkedList args, boolean isClass) {
        PackageDecl pkgDecl = (PackageDecl) args.get(0);
        Environ encEnv = (Environ) args.get(1);

        // inner class change
        boolean isInner = ((Boolean) args.get(2)).booleanValue();
        Environ pkgEnv  = (Environ) args.get(3);

        String className = ((NameNode) node.getName()).getIdent();

        Decl other = encEnv.lookupProper(className);

        if (other != null) {
           ApplicationUtility.error("attempt to redefine " + other.getName() +
           " as a class");
        }

        // Environ pkgEnv = encEnv.parent();

        ClassDecl ocl = (ClassDecl) pkgEnv.lookupProper(className);

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
              encDecl = pkgDecl;
           } else {
              encDecl = (ClassDecl) args.get(4); // decl of enclosing class
           }

           ClassDecl cl = new ClassDecl(className,
            isClass ? JavaDecl.CG_CLASS : JavaDecl.CG_INTERFACE,
            NullTypeNode.instance, node.getModifiers(), node, encDecl);

           if (ocl != null)  { // Redefinition in same package.
              ApplicationUtility.error("user type name " + className +
               " conflicts with " + ocl.getName() + " in same package");
           }

           //if (topLevel) {
           if (!isInner) {
  	           pkgEnv.add(cl);
           }

           ocl = cl;
        }

        Environ env = new Environ(encEnv);
        node.setProperty("environ", env);

        ocl.setEnviron(env);
        encEnv.add(ocl);

        node.getName().setProperty("decl", ocl);

        // additions for inner classes
        LinkedList memberArgs = new LinkedList();
        memberArgs.addLast(pkgDecl);              // package declaration
        memberArgs.addLast(env);                  // environment for this class
        memberArgs.addLast(new Boolean(true));    // inner class = true
        memberArgs.addLast(pkgEnv);               // package environment
        memberArgs.addLast(ocl);                  // last class decl
        TNLManip.traverseList(this, node, memberArgs, node.getMembers());

        return null;
    }
}
