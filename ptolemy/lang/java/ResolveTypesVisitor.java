/*
A JavaVisitor that resolves class or interface type declarations.
Based on st-package.cc in the Titanium project.

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

import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A JavaVisitor that resolves class or interface type declarations.
 *  Based on st-package.cc in the Titanium project.
 *
 *  @author Jeff Tsay
 */
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
    
    /** Resolve the possible TypeNameNode that is the base class. */
    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        node.getBaseType().accept(this, args);
        
        return null;       
    }

    /** Visit the types defined in this file. */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _currentPackage = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        TNLManip.traverseList(this, node, 
         TNLManip.cons(node.getDefinedProperty(ENVIRON_KEY)), node.getDefTypes());

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input environment
        node.getSuperClass().accept(this, args);
           
        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return _visitNodeWithEnviron(node);    
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {        
        return _visitNodeWithEnviron(node);
    }

    /** The default visit method. Visits all child nodes with the same
     *  environment as in the argument list. Nodes that do not have their
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

        // remove the ENVIRON_KEY property which is no longer needed
        node.removeProperty(ENVIRON_KEY);

        return null;
    }

    /** Handle ClassDeclNodes and InterfaceDeclNodes. */
    protected Object _visitUserTypeNode(UserTypeDeclNode node, LinkedList args) {
        // environment for this class is argument for children
        LinkedList childArgs = TNLManip.cons(node.getDefinedProperty(ENVIRON_KEY));
        
        TNLManip.traverseList(this, node, childArgs, node.getInterfaces());
        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        // remove the ENVIRON_KEY property which is no longer needed
        node.removeProperty(ENVIRON_KEY);

        return null;
    }

    /** The package this compile unit is in. */
    protected PackageDecl _currentPackage = null;
}
