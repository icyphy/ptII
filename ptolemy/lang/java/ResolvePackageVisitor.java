/*
A JavaVisitor that adds the names of the types defined in the CompileUnitNode
to the file environment, creates the environments for all nodes,
then resolves names of the imports with a ResolveImportsVisitor.

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

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// ResolvePackageVisitor

/** A JavaVisitor that adds the names of the types defined in the
CompileUnitNode to the file environment, creates the environments for
all nodes, then resolves names of the imports with a
ResolveImportsVisitor.

Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class ResolvePackageVisitor extends ResolveVisitorBase 
       implements JavaStaticSemanticConstants {

    ResolvePackageVisitor() {
        super();
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        //System.out.println("resolve package visitor (CUN)" + 
        // node.getDefinedProperty(IDENT_KEY));
    
        _pkgDecl = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        Environ environ = (Environ) node.getDefinedProperty(ENVIRON_KEY);

        _pkgEnv  = (Environ) environ.parent();

        LinkedList childArgs = new LinkedList();
        childArgs.addLast(environ);            // enclosing environment =
                                               // file environment
        childArgs.addLast(Boolean.FALSE);      // inner class = false
        childArgs.addLast(NullValue.instance); // no enclosing decl

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());

        node.accept(new ResolveImportsVisitor(), null);

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {               
        return _visitUserTypeDeclNode(node, args, true);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, false);
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        // make a new environment for inner class declarations
        
        Environ env = _makeEnviron(node, args);

        _visitList(node.getStmts(), env);
        return null;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        Environ env = _makeEnviron(node, args);

        ClassDecl decl = new ClassDecl("<anon>", null);        
        decl.setSource(node);
        decl.setEnviron(env);
        
        // FIXME : will this name be resolved???
        
        NameNode typeName = new NameNode(AbsentTreeNode.instance, "<anon>");
        typeName.setProperty(DECL_KEY, decl);
                        
        node.setProperty(DECL_KEY, decl);

        LinkedList listArgs = new LinkedList();
        listArgs.addLast(env);                  // last environment
        listArgs.addLast(Boolean.TRUE);         // inner class = true
        listArgs.addLast(NullValue.instance);   // no enclosing decl
        TNLManip.traverseList(this, null, listArgs, node.getMembers());
        
        return null;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // just pass on args as is
        TNLManip.traverseList(this, node, args, node.children());
        return null;
    }

    protected Object _visitUserTypeDeclNode(UserTypeDeclNode node,
     LinkedList args, boolean isClass) {
        Environ encEnv = (Environ) args.get(0);

        // inner class change
        boolean isInner = ((Boolean) args.get(1)).booleanValue();

        String className = ((NameNode) node.getName()).getIdent();

        Decl other = encEnv.lookupProper(className);

        if (other != null) {
           ApplicationUtility.error("attempt to redefine " + other.getName() +
           " as a class");
        }

        ClassDecl ocl;
        
        if (isInner) {
           ocl = null;
        } else {
           // lookup in package
           ocl = (ClassDecl) _pkgEnv.lookupProper(className);                      
        }

        if ((ocl != null) &&
            ((ocl.getSource() == null) ||
             (ocl.getSource() == AbsentTreeNode.instance))) {
           // Assume this is the definition of 'ocl'
           ocl.setSource(node);
           ocl.setModifiers(node.getModifiers());

           // fix category if this is an interface
           if (!isClass) {
              ocl.category = CG_INTERFACE;
           }           

        } else {
           int modifiers = node.getModifiers();
           JavaDecl encDecl;
           // boolean topLevel = !isInner || (modifiers & Modifier.STATIC_MOD);

           //if (topLevel) {
           
           // Set the enclosing declaration
           if (!isInner) {
              // For a top-level class, it's the package declaration
              encDecl = _pkgDecl;
           } else {
              // For an inner class, it's the enclosing class declaration
              Object encDeclObject = args.get(2);
              
              // CHECKME
              if (encDeclObject == NullValue.instance) {
                 encDecl = null;
              } else {
                 encDecl = (JavaDecl) encDeclObject; // enclosing decl
              }
           }

           //System.out.println("creating new class decl for " + className + " in rpv");
           ClassDecl cl = new ClassDecl(className,
            isClass ? CG_CLASS : CG_INTERFACE,
            new TypeNameNode(node.getName()), node.getModifiers(), node, encDecl);

           if (ocl != null)  { // Redefinition in same package.
              ApplicationUtility.error("user type name " + className +
               " conflicts with " + ocl.getName() + " in same package");
           }

           // add to the package environment if it's an top-level class
           if (!isInner) {
              _pkgEnv.add(cl);
           }

           ocl = cl;
        }

        Environ env = new Environ(encEnv);
        node.setProperty(ENVIRON_KEY, env);

        // JUST TRY THIS
        ocl.setEnviron(env);
        encEnv.add(ocl);

        node.getName().setProperty(DECL_KEY, ocl);

        LinkedList memberArgs = new LinkedList();
        memberArgs.addLast(env);                  // environment for this class
        memberArgs.addLast(Boolean.TRUE);         // inner class = true
        memberArgs.addLast(ocl);                  // last class decl
        TNLManip.traverseList(this, node, memberArgs, node.getMembers());

        return null;
    }

    protected static Environ _makeEnviron(TreeNode node, LinkedList args) {
        Environ encEnv = (Environ) args.get(0);

        Environ env = new Environ(encEnv);
        node.setProperty(ENVIRON_KEY, env);

        return env;
    }
 
    protected void _visitList(List nodeList, Environ env) {
        LinkedList listArgs = new LinkedList();
        listArgs.addLast(env);                  // last environment
        listArgs.addLast(Boolean.TRUE);         // inner class = true
        listArgs.addLast(NullValue.instance);   // no enclosing decl
        TNLManip.traverseList(this, null, listArgs, nodeList);
    }

    /** The package this compile unit is in. */
    protected PackageDecl _pkgDecl = null;

    /** The package environment. */
    protected Environ _pkgEnv = null;
}
