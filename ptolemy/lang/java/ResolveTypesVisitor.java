/*
A JavaVisitor that resolves class or interface type declarations.

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

public class ResolveTypesVisitor extends ResolveVisitorBase 
       implements JavaStaticSemanticConstants {

    /** Create a ResolveTypesVisitor. */
    ResolveTypesVisitor() {
        super();
    }

    /** Resolve the name of the type. */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {

        Environ env = (Environ) args.get(0);

        NameNode name = node.getName();

        NameNode newName = (NameNode) StaticResolution.resolveAName(
         name, env, null, _currentPackage, CG_USERTYPE);

        // this is not necessary, but by convention ...
        node.setName(newName);

        return null;
    }

    /** Visit the types defined in this file. */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        _initLazyFlag(node);

        _currentPackage = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        LinkedList childArgs = new LinkedList();
        childArgs.add(node.getDefinedProperty(ENVIRON_KEY)); // file environment

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        if (!_isSkippable(node)) {
           // resolve the super class with the same input environment
           node.getSuperClass().accept(this, args);
        }
        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {

        if (_lazy) {
           if ((node.getModifiers() & PRIVATE_MOD) != 0) {
              // don't resolve anything if it's a private method
              return null;
           }

           // get the environment of this node
           LinkedList childArgs = new LinkedList();
           childArgs.addLast(node.getDefinedProperty(ENVIRON_KEY));

           // resolve only the return type, parameters and exceptions thrown
           node.getReturnType().accept(this, childArgs);
           TNLManip.traverseList(this, node, childArgs, node.getParams());
           TNLManip.traverseList(this, node, childArgs, node.getThrowsList());
           return null;
        }
        return _visitNodeWithEnviron(node);
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        if (_lazy) {
           if ((node.getModifiers() & PRIVATE_MOD) != 0) {
              // don't resolve anything if it's a private constructor
              return null;
           }

           // the environment of this node is the argument for the children
           LinkedList childArgs = TNLManip.cons(node.getDefinedProperty(ENVIRON_KEY));
           
           // resolve only the parameters and exceptions thrown
           TNLManip.traverseList(this, node, childArgs, node.getParams());
           TNLManip.traverseList(this, node, childArgs, node.getThrowsList());
           return null;
        }
        return _visitNodeWithEnviron(node);
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return _visitNodeWithEnviron(node);    
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        return _visitNodeWithEnviron(node);
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {        
        return _visitNodeWithEnviron(node);
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
     *  Only nodes that have their own environment should call this method.
     */
    protected Object _visitNodeWithEnviron(TreeNode node) {

        // environment for this class is argument for children
        LinkedList childArgs = TNLManip.cons(node.getDefinedProperty(ENVIRON_KEY));
        
        TNLManip.traverseList(this, node, childArgs, node.children());

        return null;
    }

    /** Handle ClassDeclNodes and InterfaceDeclNodes. */
    protected Object _visitUserTypeNode(UserTypeDeclNode node, LinkedList args) {
        if (_isSkippable(node)) {
           // don't resolve anything if it's a private class and we're doing lazy resolution
           return null;
        }

        // environment for this class is argument for children
        LinkedList childArgs = TNLManip.cons(node.getDefinedProperty(ENVIRON_KEY));
        
        TNLManip.traverseList(this, node, childArgs, node.getInterfaces());
        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        return null;
    }

    /** The package this compile unit is in. */
    protected PackageDecl _currentPackage = null;
}
