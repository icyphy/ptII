/* A JavaVisitor that resolves class or interface type declarations.

Copyright (c) 1998-2001 The Regents of the University of California.
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
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class ResolveTypesVisitor extends ResolveVisitorBase
    implements JavaStaticSemanticConstants {

    /** Create a ResolveTypesVisitor. */
    ResolveTypesVisitor() {
        super();
    }

    /** Resolve the name of the type. */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        Scope scope = (Scope) args.get(0);
        boolean debug = false;

        NameNode name = node.getName();
        
        if (StaticResolution.traceLoading) {
            System.out.println("Calling resolveAName from " + 
                    "ResolveTypesVisitor.visitTypeNameNode#1(" + name.getIdent() + ")");
        }

	try {

	    NameNode newName =
		(NameNode) StaticResolution.resolveAName(name, scope, null,
							 _currentPackage,
							 CG_USERTYPE);
	    node.setName(newName);
	} catch (RuntimeException ex) {
	    // FIXME?  This is a bit of a problem for type safe
	    // enumerations where we refer to a public field in a
	    // public inner class.
	    // For example data/expr/Variable has a line:
	    // private Settable.Visibility _visibility = Settable.NONE;
	    // which refers to a type safe enum in
	    // ptolemy/kernel/util.Visibility
	    NameNode parentName = (NameNode) name.getQualifier();
  		System.err.println("ResolveTypesVisitor.visitTypeNameNode():" +
                        " failed to resolve " + node.getName().getIdent());
//  				   " failed to resolve\n" + node +
//  				   "\n name = " + name +
//    				   " node.hashCode() = " + node.hashCode() +
//    				   " name.hashCode() = " + name.hashCode() +
//  				   "\n Trying potential inner class " +
//      				   parentName +
//      				   " parentName.hashCode() = " + parentName.hashCode() +
//    				   "\n parentName.getIdent()" +
//  				   parentName.getIdent());
	    if (parentName != null) {
		try {
            if (StaticResolution.traceLoading) {
                System.out.println("Calling resolveAName from " + 
                        "ResolveTypesVisitor.visitTypeNameNode#2(" + 
                        parentName.getIdent() + ")");
            }
		    NameNode newName =
			(NameNode) StaticResolution.resolveAName(parentName,
								 scope, null,
								 _currentPackage,
								 CG_USERTYPE);
		newName.setIdent(parentName.getIdent() + '$' +
				     name.getIdent());
		newName.removeProperty(DECL_KEY);

        if (debug) {
    		System.err.println("ResolveTypesVisitor.visitTypeNameNode():" +
    				   "newName after setIdent = " + newName +
    				   "\n newName.hashCode()" +
    				   newName.hashCode() +
    				   "newName.getQualifier()" +
    				   newName.getQualifier());
        }
		    try {
            if (StaticResolution.traceLoading) {
                System.out.println("Calling resolveAName from " + 
                        "ResolveTypesVisitor.visitTypeNameNode#3(" + 
                        newName.getIdent() + ")");
            }
			NameNode newerName =
			    (NameNode) StaticResolution.
			    resolveAName(newName,
					 scope, null,
					 _currentPackage,
					 CG_USERTYPE);
//  			System.err.println("ResolveTypesVisitor." +
//  					   "visitTypeNameNode():" +
//  					   "newerName = " + newerName +
//  					   "\n newerName.hashCode()" +
//  					   newerName.hashCode());

			node.setName(newerName);
  			System.out.println("ResolveTypesVisitor." +
  					   "visitTypeNameNode(): substituted "+
					   node.getName().getIdent());
//  			System.out.println("ResolveTypesVisitor." +
//  					   "visitTypeNameNode(): did " +
//  					   "subsitution. " +
//  					   node.isSingleton() +
//  					   " node.hashCode = " +
//  					   node.hashCode() +
//  					   " node is now" + node +
//  					   " newerName.hashCode = " +
//  					   newerName.hashCode() +
//  					   "\n newerName = " + newerName);
		    } catch (RuntimeException runtimeException) {
			System.err.println("ResolveTypesVisitor." +
					   "visitTypeNameNode(): " +
					   "failed to resolve newName\n" +
					   newName + "\n: ");
			throw runtimeException;
		    }
		} catch (RuntimeException runtimeException) {
		    System.err.println("ResolveTypesVisitor." +
				       "visitTypeNameNode(): " +
				       "failed to resolve\n" +
				       parentName + "\n: ");
	          throw runtimeException;
		}
	    } else {
		throw ex;
	    }
	}
        // this is not necessary, but by convention ...

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

        TNLManip.traverseList(this,
                TNLManip.addFirst(node.getDefinedProperty(SCOPE_KEY)),
                node.getDefTypes());

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        node.getSuperClass().accept(this, args);

        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return _visitNodeWithScope(node);
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        return _visitNodeWithScope(node);
    }

    /** The default visit method. Visits all child nodes with the same
     *  scope as in the argument list. Nodes that do not have their
     *  own scope should call this method.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // just pass on the same scope to the children
        return TNLManip.traverseList(this, args, node.children());
    }

    /** Get scope from this node, and pass it to the children.
     *  Only nodes that have their own scope should call this method.
     */
    protected Object _visitNodeWithScope(TreeNode node) {

        // scope for this class is argument for children
        LinkedList childArgs = TNLManip.addFirst(node.getDefinedProperty(SCOPE_KEY));

        TNLManip.traverseList(this, childArgs, node.children());

        // remove the SCOPE_KEY property which is no longer needed
        node.removeProperty(SCOPE_KEY);

        return null;
    }

    /** Handle ClassDeclNodes and InterfaceDeclNodes. */
    protected Object _visitUserTypeNode(UserTypeDeclNode node, LinkedList args) {
        // scope for this class is argument for children
        LinkedList childArgs = TNLManip.addFirst(node.getDefinedProperty(SCOPE_KEY));

	//System.out.println("ResolveTypesVisitor:_visitUserTypeNode: " +
	//		   node.getName().getIdent() +
	//		   " interfaces: " + node.getInterfaces());
        TNLManip.traverseList(this, childArgs, node.getInterfaces());
        TNLManip.traverseList(this, childArgs, node.getMembers());

        // remove the SCOPE_KEY property which is no longer needed
        node.removeProperty(SCOPE_KEY);

        return null;
    }

    /** The package this compile unit is in. */
    protected PackageDecl _currentPackage = null;
}
