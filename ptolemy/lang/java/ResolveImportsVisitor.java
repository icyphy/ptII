/*
A JavaVisitor that resolves the names of the import nodes of an
abstract syntax tree for a Java program.

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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

public class ResolveImportsVisitor extends JavaVisitor
    implements JavaStaticSemanticConstants {

    public ResolveImportsVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _compileUnit = node;
        _fileEnv = (Environ) node.getDefinedProperty(ENVIRON_KEY); // file environment
        _importEnv = _fileEnv.parent().parent();

        // initialize importedPackages property
        if (!node.hasProperty(IMPORTED_PACKAGES_KEY)) {
            _importedPackages = new LinkedList();

            node.setProperty(IMPORTED_PACKAGES_KEY, _importedPackages);
        } else {
            _importedPackages =
                (Collection) node.getDefinedProperty(IMPORTED_PACKAGES_KEY);
        }

        _importOnDemand("java.lang");

        TNLManip.traverseList(this, null, node.getImports());

        return null;
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {

        NameNode name = node.getName();

        StaticResolution.resolveAName(name,
                (Environ) StaticResolution.SYSTEM_PACKAGE.getEnviron(), null, null,
                CG_USERTYPE);

        JavaDecl old = (JavaDecl) _fileEnv.lookupProper(name.getIdent());
        JavaDecl current = (JavaDecl) name.getProperty(DECL_KEY);

        if ((old != null) && (old != current)) {
            if (old != current) {
                throw new RuntimeException("attempt to import conflicting name: " +
                        old.getName());
            }
        }

        _importEnv.add((ClassDecl) name.getDefinedProperty(DECL_KEY));

        return null;
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {

        NameNode name = node.getName();

        StaticResolution.resolveAName(name,
                StaticResolution.SYSTEM_PACKAGE.getEnviron(), null,  null, CG_PACKAGE);

        PackageDecl decl = (PackageDecl) name.getDefinedProperty(DECL_KEY);

        _importOnDemand(decl);

        return null;
    }

    /** The default visit method. We shouldn't visit this node, so throw an exception. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("ResolveImports not defined on node type : " +
                node.getClass().getName());
    }

    protected final void _importOnDemand(PackageDecl importedPackage) {

        ApplicationUtility.trace("importOnDemand : importing " +
                importedPackage.toString());

        // ignore duplicate imports
        if (_importedPackages.contains(importedPackage)) {
            ApplicationUtility.warn("importOnDemand : ignoring duplicated package "
                    + importedPackage.toString());
            return;
        }

        _importedPackages.add(importedPackage);

        Environ pkgEnv = importedPackage.getEnviron();

        Iterator envItr = pkgEnv.allProperDecls();

        while (envItr.hasNext()) {
            JavaDecl type = (JavaDecl) envItr.next();

            if (type.category != CG_PACKAGE) {
                ApplicationUtility.trace("importOnDemand: adding " + type.toString());
                _importEnv.add(type); // conflicts appear on use only
            }
        }

        ApplicationUtility.trace("importOnDemand : finished" +
                importedPackage.toString());
    }

    // we can get rid of this by added java.lang to the import list
    protected final void _importOnDemand(String qualName) {
        NameNode name = (NameNode) StaticResolution.makeNameNode(qualName);

        StaticResolution.resolveAName(name,
                StaticResolution.SYSTEM_PACKAGE.getEnviron(), null, null,
                CG_PACKAGE);

        _importOnDemand((PackageDecl) name.getDefinedProperty(DECL_KEY));
    }


    /** The CompileUnitNode that is the root of the tree. */
    protected CompileUnitNode _compileUnit = null;

    /** The file environment. */
    protected Environ _fileEnv = null;

    /** The import environment. */
    protected Environ _importEnv = null;

    /** The Collection of imported packages for this compile unit. */
    protected Collection _importedPackages = null;
}
