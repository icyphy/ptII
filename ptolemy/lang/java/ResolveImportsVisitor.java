/*
A JavaVisitor that resolves the names of the import nodes of an
abstract syntax tree for a Java program.

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

public class ResolveImportsVisitor extends JavaVisitor {
    ResolveImportsVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(node);                               // compile unit
        childArgs.addLast(node.getDefinedProperty("environ")); // file environment

        TNLManip.traverseList(this, node, childArgs, node.getImports());

        return null;
    }


    public Object visitImportNode(ImportNode node, LinkedList args) {

        Environ fileEnv = (Environ) args.get(1);
        NameNode name = node.getName();

        StaticResolution.resolveAName(name,
         (Environ) StaticResolution.SYSTEM_PACKAGE.getEnviron(),
         null, false, null, JavaDecl.CG_USERTYPE);

        JavaDecl old = (JavaDecl) fileEnv.lookupProper(name.getIdent());
        JavaDecl current = (JavaDecl) name.getProperty("decl");

        if ((old != null) && (old != current)) {
  	        if (old != current) {
 	           throw new RuntimeException("attempt to import conflicting name: " +
               old.getName());
           }
	     }
        fileEnv.add((ClassDecl) name.getDefinedProperty("decl"));

        return null;
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {
        CompileUnitNode file = (CompileUnitNode) args.get(0);

        NameNode name = node.getName();

        StaticResolution.resolveAName(name,
         StaticResolution.SYSTEM_PACKAGE.getEnviron(), null, false,
         null, JavaDecl.CG_PACKAGE);

        PackageDecl decl = (PackageDecl) name.getDefinedProperty("decl");

        StaticResolution.importOnDemand(file, decl);
        return null;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("ResolveImports not defined on node type : " +
         node.getClass().getName());
    }
}
