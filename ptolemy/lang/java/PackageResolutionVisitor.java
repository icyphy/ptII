package ptolemy.lang.java;

import ptolemy.lang.*;
import java.util.LinkedList;

class PackageResolutionVisitor extends JavaVisitor {

    PackageResolutionVisitor() {
        super(TM_CHILDREN_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        // initialize importedPackges property
        if (!node.hasProperty("importedPackages")) {
           node.setProperty("importedPackages", new LinkedList());
        }

        PackageDecl thePkgDecl;
        TreeNode pkgDeclNode = node.getPkg();

        if (pkgDeclNode == AbsentTreeNode.instance) {
           thePkgDecl = StaticResolution.UNNAMED_PACKAGE;
        } else {
           NameNode name = (NameNode) StaticResolution.resolveAName(
            (NameNode) pkgDeclNode,
            (Environ) StaticResolution.SYSTEM_PACKAGE.getProperty("environ"),
		     null, false, null, JavaDecl.CG_PACKAGE);
           thePkgDecl = (PackageDecl) name.getProperty("decl");
        }

        node.setDefinedProperty("thePackage", thePkgDecl);

        // build environment for this file
        Environ importOnDemandEnv = new Environ(
         (Environ) StaticResolution.SYSTEM_PACKAGE.getDefinedProperty("environ"));

        Environ pkgEnv = new Environ(importOnDemandEnv);

        pkgEnv.copyDeclList((Environ) thePkgDecl.getDefinedProperty("environ"));

        Environ environ = new Environ(pkgEnv); // the file level environment

        node.setProperty("environ", environ);

        LinkedList nameList = new LinkedList();
        nameList.addLast("java");
        nameList.addLast("lang");
        StaticResolution.importOnDemand(node, nameList.listIterator());

        node.accept(new ResolvePackageVisitor(), null);

        node.accept(new ResolveTypesVisitor(), null);

        return null;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("ResolveImports not defined on node type : " +
         node.getClass().getName());
    }
}
