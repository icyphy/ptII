package ptolemy.lang.java;

import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

public class PackageResolutionVisitor extends JavaVisitor 
       implements JavaStaticSemanticConstants {

    public PackageResolutionVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        PackageDecl thePkgDecl;
        TreeNode pkgDeclNode = node.getPkg();

        if (pkgDeclNode == AbsentTreeNode.instance) {
           thePkgDecl = StaticResolution.UNNAMED_PACKAGE;
        } else {
           NameNode name = (NameNode) StaticResolution.resolveAName(
            (NameNode) pkgDeclNode,
            StaticResolution.SYSTEM_PACKAGE.getEnviron(), null, null, CG_PACKAGE);
           thePkgDecl = (PackageDecl) name.getProperty(DECL_KEY);
        }

        node.setProperty(PACKAGE_KEY, thePkgDecl);

        // build environment for this file
        Environ importOnDemandEnv = new Environ(
         StaticResolution.SYSTEM_PACKAGE.getEnviron());

        Environ pkgEnv = new Environ(importOnDemandEnv);

        pkgEnv.copyDeclList(thePkgDecl.getEnviron());

        Environ environ = new Environ(pkgEnv); // the file level environment
        node.setProperty(ENVIRON_KEY, environ);

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
