/*
A JavaVisitor that resolves class or interface type declarations.

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

public class ResolveTypesVisitor extends ResolveVisitorBase {

    /** Create a ResolveTypesVisitor. */
    ResolveTypesVisitor() {
        super();
    }

    /** Resolve the name of the type. */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {

        Environ env = (Environ) args.get(0);

        NameNode name = node.getName();

        NameNode newName = (NameNode) StaticResolution.resolveAName(
         name, env, null, false, _pkgDecl, JavaDecl.CG_USERTYPE);

        // this is not necessary, but by convention ...
        node.setName(newName);

        return null;
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return null;
    }

    /** Visit the types defined in this file. */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        _initLazyFlag(node);

        _pkgDecl = (PackageDecl) node.getDefinedProperty("thePackage");

        LinkedList childArgs = new LinkedList();
        childArgs.add(node.getDefinedProperty("environ")); // file environment

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        if (_lazy) {
           if ((node.getModifiers() & Modifier.PRIVATE_MOD) != 0) {
              // don't resolve anything if it's a private method
              return null;
           }

           // resolve only the return type, parameters and exceptions thrown
           node.getReturnType().accept(this, args);
           TNLManip.traverseList(this, node, args, node.getParams());
           TNLManip.traverseList(this, node, args, node.getThrowsList());
           return null;
        }
        return _defaultVisit(node, args);
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        if (_lazy) {
           if ((node.getModifiers() & Modifier.PRIVATE_MOD) != 0) {
              // don't resolve anything if it's a private constructor
              return null;
           }

           // resolve only the parameters and exceptions thrown
           TNLManip.traverseList(this, node, args, node.getParams());
           TNLManip.traverseList(this, node, args, node.getThrowsList());
           return null;
        }
        return _defaultVisit(node, args);
    }

    /** The default visit method. Visits all child nodes with the same
     *  environment as in the argument list. Only nodes that do not have their
     *  own environment should call this method.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // just pass on the same environment to the children
        return TNLManip.traverseList(this, node, args, node.children());
    }

    /** Get environment from this node, and pass it to the children.
     *  Only nodes that do have their own environment should call this method.
     */
    protected Object _visitNodeWithEnviron(TreeNode node) {
        Object envObj = node.getDefinedProperty("environ");

        LinkedList childArgs = new LinkedList();
        childArgs.addLast(envObj);

        return TNLManip.traverseList(this, node, childArgs, node.children());
    }

    protected Object _visitUserTypeNode(UserTypeDeclNode node, LinkedList args) {
        if ((node.getModifiers() & Modifier.PRIVATE_MOD) != 0) {
           // don't resolve anything if it's a private class
           return null;
        }

        LinkedList childArgs = new LinkedList();
        // environment for this class
        childArgs.addLast(node.getDefinedProperty("environ"));

        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        return null;
    }

    /** The package this compile unit is in. */
    protected PackageDecl _pkgDecl = null;
}
