package ptolemy.lang.java;

import ptolemy.lang.*;
import java.util.LinkedList;

public class PackageResolutionVisitor extends JavaVisitor {

    public PackageResolutionVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        // initialize importedPackages property
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
            StaticResolution.SYSTEM_PACKAGE.getEnviron(), null, null, JavaDecl.CG_PACKAGE);
           thePkgDecl = (PackageDecl) name.getProperty("decl");
        }

        node.setProperty("thePackage", thePkgDecl);

        // build environment for this file
        Environ importOnDemandEnv = new Environ(
         StaticResolution.SYSTEM_PACKAGE.getEnviron());

        Environ pkgEnv = new Environ(importOnDemandEnv);

        pkgEnv.copyDeclList(thePkgDecl.getEnviron());

        Environ environ = new Environ(pkgEnv); // the file level environment

        node.setProperty("environ", environ);

        StaticResolution.importOnDemand(node, new String[] { "java", "lang" });

        node.accept(new ResolvePackageVisitor(), null);

        node.accept(new ResolveTypesVisitor(), null);

        return null;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("PackageResolution not defined on node type : " +
         node.getClass().getName());
    }
}
