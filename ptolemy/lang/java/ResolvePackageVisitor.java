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

class ResolvePackageVisitor extends JavaVisitor {

    ResolvePackageVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        Environ environ = (Environ) node.getDefinedProperty("environ");
        PackageDecl pkgDecl = (PackageDecl) node.getDefinedProperty("decl");

        LinkedList childArgs = new LinkedList();
        childArgs.addLast(pkgDecl);
        childArgs.addLast(environ);

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());

        LinkedList rivArgs = new LinkedList();
        rivArgs.addLast(node);
        rivArgs.addLast(environ);

        TNLManip.traverseList(new ResolveImportsVisitor(), node,
         rivArgs, node.getImports());
        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        PackageDecl pkgDecl = (PackageDecl) args.get(0);
        Environ fileEnv = (Environ) args.get(1);

        String className = ((NameNode) node.getName()).getIdent();

        Decl other = fileEnv.lookupProper(className);

        if (other != null) {
           throw new RuntimeException("attempt to redefine " + other.getName() +
           " as a class");
        }

        Environ pkgEnv = fileEnv.parent();

        ClassDecl ocl = (ClassDecl) pkgEnv.lookupProper(className);

        if ((ocl != null) && (!ocl.hasProperty("source") ||
             (ocl.getProperty("source") == AbsentTreeNode.instance))) {
           // Assume this is the definition of 'other'
           ocl.setProperty("source", node);
           ocl.setModifiers(node.getModifiers());
        } else {
           ClassDecl cl = new ClassDecl(className, JavaDecl.CG_CLASS,
            NullTypeNode.instance, node.getModifiers(), node, pkgDecl);

           if (ocl != null)  { // Redefinition in same package.
              throw new RuntimeException("class name " + className +
              " conflicts with " + ocl.getName() + " in same package");
           } else {
	           pkgEnv.add(cl);
           }

           ocl = cl;
        }
        ocl.setProperty("environ", new Environ(fileEnv));
        fileEnv.add(ocl);

        node.getName().setProperty("decl", ocl);

        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        PackageDecl pkgDecl = (PackageDecl) args.get(0);
        Environ fileEnv = (Environ) args.get(1);

        String interfaceName = ((NameNode) node.getName()).getIdent();

        Decl other = fileEnv.lookupProper(interfaceName);

        if (other != null) {
           throw new RuntimeException("attempt to redefine " + other.getName() +
           " as an interface");
        }

        Environ pkgEnv = fileEnv.parent();

        ClassDecl ocl = (ClassDecl) pkgEnv.lookupProper(interfaceName);

        if ((ocl != null) && (!ocl.hasProperty("source") ||
            (ocl.getProperty("source") == AbsentTreeNode.instance))) {
           // Assume this is the definition of 'other'
           ocl.setProperty("source", node);
           ocl.setModifiers(node.getModifiers());

           // should make sure it's an interface
        } else {
           ClassDecl cl = new ClassDecl(interfaceName, JavaDecl.CG_INTERFACE,
            NullTypeNode.instance, node.getModifiers(), node, pkgDecl);

           if (ocl != null)  {// Redefinition in same package.
              throw new RuntimeException("interface name " + interfaceName +
               " conflicts with " + ocl.getName() + " in same package");
           } else {
	           pkgEnv.add(cl);
           }

           ocl = cl;
        }
        ocl.setProperty("environ", new Environ(fileEnv));
        fileEnv.add(ocl);

        node.getName().setProperty("decl", ocl);

        return null;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("ResolvePackage not defined on node type : " +
         node.getClass().getName());
    }
}
