/*
Create declarations for fields, constructors, and methods, and add them to
their enclosing class's scope.

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// ResolveClassVisitor
/**
Create declarations for fields, constructors, and methods, and add them to
their enclosing class's scope.

This is part 1 of pass 1:

Adding proper class and interface members to the respective
scopes.  All source files known to the compiler must undergo
this step before the next step.

Part 2 of pass 1 happens in ResolveInheritanceVisitor.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@see ResolveInheritanceVisitor

@author Jeff Tsay
@version $Id$
*/
public class ResolveClassVisitor extends ResolveVisitorBase
    implements JavaStaticSemanticConstants  {
    public ResolveClassVisitor() {
        super();
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        //System.out.println("resolveClass for " + node.getProperty(IDENT_KEY));

        _pkgDecl = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        LinkedList childArgs = new LinkedList();
        childArgs.add(NullValue.instance); // enclosing class decl
        childArgs.add(NullValue.instance); // enclosing class scope

        TNLManip.traverseList(this, childArgs, node.getDefTypes());

        //System.out.println("finished resolveClass for " +
        //                node.getProperty(IDENT_KEY));

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        // find the outer class/interface, if applicable
        Object classDeclObj = args.get(0);

        if (classDeclObj instanceof ClassDecl) {
            ClassDecl classDecl = (ClassDecl) classDeclObj;

            if (classDecl.category == CG_INTERFACE) {
                // classes of an interface are always static and final, and
                // public if the interface is public

                int classPublic = classDecl.getModifiers() & PUBLIC_MOD;

                int modifiers = node.getModifiers();
                modifiers |= (classPublic | STATIC_MOD | FINAL_MOD);
                node.setModifiers(modifiers);
            }
        }

        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        // record the fact that we are modifying the ClassDecl
        // exit if we have already done class resolution
        if (!me.addVisitor(_myClass)) {
            return null;
        }

        TreeNode superClass = node.getSuperClass();

        ClassDecl superDecl;

        if (me == StaticResolution.OBJECT_DECL) {
            superDecl = null;
        } else if (superClass == AbsentTreeNode.instance) {
            superDecl = StaticResolution.OBJECT_DECL;
        } else {
	    try {
		//superDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) superClass);
		//System.out.println("ResolveClassVisitor." +
                //			 "visitClassDeclNode():" +
		//			 "Calling JavaDecl.getDecl " +
		//			 superClass);
		superDecl = (ClassDecl) JavaDecl.getDecl(superClass);
	    } catch (Exception e) {
		throw new RuntimeException("ResolveClassVisitor.visitClassDeclNode() node=" +
                        node + "\nargs.get(0) = " +
                        args.get(0) + ")\n superClass = " +
                        superClass + "\n" +
                        superClass.toString() +
                        ": " + e);
	    }
        }
	//System.out.println("ResolveClassVisitor.visitClassDeclNode(): " +
	//		   node.getName().getIdent() +
	//		   ": " + superDecl + ": " +
	//		   superClass + ": " + StaticResolution.OBJECT_DECL );
        if ((superDecl != null) && (superDecl.category != CG_CLASS)) {
            throw new RuntimeException("class " + node.getName().getIdent() +
                    " cannot extend interface " + superDecl.getName());
        }

        if (superDecl != null) {
            node.setSuperClass(superDecl.getDefType());
        }
        me.setSuperClass(superDecl);

        // initialize the implements list.
        LinkedList declInterfaceList = new LinkedList();

        Iterator interfaceItr = node.getInterfaces().iterator();

        while (interfaceItr.hasNext()) {
            ClassDecl intf = (ClassDecl) JavaDecl.getDecl(
                    (NamedNode) interfaceItr.next());

            if ((intf.category & CG_INTERFACE) == 0) {
                throw new RuntimeException("class " + node.getName().getIdent() +
                        " cannot implement class " + intf.getName() +
			" " + intf.category);
            }
            declInterfaceList.addLast(intf);
        }

        me.setInterfaces(declInterfaceList);

        // add this declaration to outer class's scope, if applicable
        _addUserTypeToEnclosingClassScope(args.get(1), me);

        // have members add themselves to this class's scope
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(me.getScope());

        TNLManip.traverseList(this, childArgs, node.getMembers());

        return null;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        ClassDecl classDecl = (ClassDecl) args.get(0);

        int modifiers = node.getModifiers();

        if (classDecl.category == CG_INTERFACE) {
            // fields of an interface are always static and final, and
            // public if the interface is public

            int classPublic = classDecl.getModifiers() & PUBLIC_MOD;

            modifiers |= (classPublic | STATIC_MOD | FINAL_MOD);
            node.setModifiers(modifiers);
        }

        String nameString = node.getName().getIdent();

        // Leftover from Titanium. Why??
        // dtype()->resolveClass(package, cclass, fileEnv);

        Scope encScope = (Scope) args.get(1);

        Decl d = encScope.lookupLocal(nameString, CG_FIELD);

        if (d != null) {
            throw new RuntimeException("redeclaration of " + d.getName());
        }

        d = new FieldDecl(nameString, node.getDefType(), modifiers,
                node, (ClassDecl) args.get(0));

        encScope.add(d);

        node.getName().setProperty(DECL_KEY, d);

        // now that we have anonymous classes, we should visit the init
        // expression
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(NullTypeNode.instance); // enclosing class decl
        childArgs.addLast(NullTypeNode.instance); // enclosing class scope
        node.getInitExpr().accept(this, childArgs);

        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        // find the outer class/interface, if applicable
        Object classDeclObj = args.get(0);

        if (classDeclObj instanceof ClassDecl) {
            ClassDecl classDecl = (ClassDecl) classDeclObj;

            if (classDecl.category == CG_INTERFACE) {
                // inner interfaces of an interface are always static, and
                // public if the interface is public

                int classPublic = classDecl.getModifiers() & PUBLIC_MOD;

                int modifiers = node.getModifiers();
                modifiers |= classPublic | STATIC_MOD;
                node.setModifiers(modifiers);
            }
        }

        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        // record the fact that we are modifying the ClassDecl
        // exit if we have already done class resolution
        if (!me.addVisitor(_myClass)) {
            return null;
        }

        LinkedList declInterfaceList = new LinkedList();

        Iterator interfaceItr = node.getInterfaces().iterator();

        while (interfaceItr.hasNext()) {
            ClassDecl intf = (ClassDecl) JavaDecl.getDecl(
                    (NamedNode) interfaceItr.next());

            if ((intf.category & CG_INTERFACE) == 0) {
                throw new RuntimeException("class " + node.getName().getIdent() +
                        " cannot implement class " + intf.getName() +
			" " + intf.category);
            }
            declInterfaceList.addLast(intf);
        }

        me.setInterfaces(declInterfaceList);

        // add this declaration to outer class's scope, if applicable
        _addUserTypeToEnclosingClassScope(args.get(1), me);

        // have members add themselves to this class's scope
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(me.getScope());

        TNLManip.traverseList(this, childArgs, node.getMembers());

        return null;
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        return null;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {

        ClassDecl classDecl = (ClassDecl) args.get(0);

        int modifiers = node.getModifiers();

        // Check that this constructor is legal

        Scope classEnv = (Scope) args.get(1);

        NameNode name = node.getName();
        String constructorName = name.getIdent();

        MethodDecl d = new MethodDecl(constructorName, CG_CONSTRUCTOR,
                NullTypeNode.instance, modifiers, node, classDecl,
                _makeTypeList(node.getParams()), (Collection) node.getThrowsList());


        /* Do not do any checking that involves types.

           Iterator constructorItr = classEnv.lookupFirstLocal(constructorName,
           CG_CONSTRUCTOR);

           while (constructorItr.hasNext()) {

           MethodDecl dd = (MethodDecl) constructorItr.next();

           if (dd.conflictsWith(d)) {
           throw new RuntimeException("illegal overloading of " +
           constructorName);
           }
           }
        */

        classEnv.add(d);

        name.setProperty(DECL_KEY, d);

        // now that we have anonymous classes, we should visit the body
        TreeNode block = node.getBody();

        if (block != AbsentTreeNode.instance) {

            LinkedList childArgs = new LinkedList();
            childArgs.addLast(NullTypeNode.instance); // enclosing class decl
            childArgs.addLast(NullTypeNode.instance); // enclosing class scope
            block.accept(this, childArgs);
        }

        return null;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {

        ClassDecl classDecl = (ClassDecl) args.get(0);

        int modifiers = node.getModifiers();

        // FIXME: classDecl.category can be set to CG_CLASS | CG_INTERFACE??
        if ((classDecl.category & CG_CLASS) !=0) {
            // private methods or methods in private or final classes are final
	    //
	    // FIXME: I'm not sure if the above is true, so I've commented
	    // out the code below
	    // sun.misc.URLClassPath$JarLoader extends
	    // sun.misc.URLClassPath$Loader
	    // both have a getResource(java.lang.String, boolean) method
	    // To verify, use
	    // javap sun.misc.URLClassPath\$Loader
	    // javap sun.misc.URLClassPath\$JarLoader

            //              if ((modifiers & PRIVATE_MOD) != 0) {
            //                  modifiers |= FINAL_MOD;
            //              }

            //              int classMod = classDecl.getModifiers();

            //              if ((classMod & (PRIVATE_MOD | FINAL_MOD)) != 0) {
            //                  modifiers |= FINAL_MOD;
            //              }
        } else {
            // we are inside an interface, all methods are abstract,
            // and public if the enclosing interface is public
            int classPublic = classDecl.getModifiers() & PUBLIC_MOD;
            modifiers |= (classPublic | ABSTRACT_MOD);
        }
        node.setModifiers(modifiers);

        if (node.getBody() == AbsentTreeNode.instance) {
            if ((modifiers & ABSTRACT_MOD) != 0) {
                if ((modifiers &
                        (PRIVATE_MOD | STATIC_MOD | FINAL_MOD | SYNCHRONIZED_MOD |
                                NATIVE_MOD)) != 0) {
                    throw new RuntimeException("can't use private, static, final, " +
                            "synchronized, or native with abstract");
                }
            } else if ((modifiers & NATIVE_MOD) == 0) {
		// If we are using reflection, then the method will not
		// have a body.
                //throw new RuntimeException("abstract or native modifier " +
		// required on  methods without a body");
            }
        } else if ((modifiers & (ABSTRACT_MOD | NATIVE_MOD)) != 0) {
            throw new RuntimeException("an abstract or native method " +
                    "cannot have a body");
        }

        // Check that this method is legal

        Scope classEnv = (Scope) args.get(1);

        NameNode name = node.getName();
        String methodName = name.getIdent();

        Iterator methodItr = classEnv.lookupFirstLocal(methodName, CG_METHOD);

        MethodDecl d = new MethodDecl(methodName, CG_METHOD,
                node.getReturnType(), modifiers, node, classDecl,
                _makeTypeList(node.getParams()), (Collection) node.getThrowsList());

        /* Do not do any checking that involves types.
           while (methodItr.hasNext()) {

           MethodDecl dd = (MethodDecl) methodItr.next();

           if (dd.conflictsWith(d)) {
           throw new RuntimeException("illegal overloading of " + methodName);
           }
           }
        */

        classEnv.add(d);

        name.setProperty(DECL_KEY, d);

        // now that we have anonymous classes, we should visit the body
        TreeNode block = node.getBody();

        if (block != AbsentTreeNode.instance) {
            LinkedList childArgs = new LinkedList();
            childArgs.addLast(NullTypeNode.instance); // enclosing class decl
            childArgs.addLast(NullTypeNode.instance); // enclosing class scope
            block.accept(this, childArgs);
        }

        return null;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {

        node.getEnclosingInstance().accept(this, args);

        ClassDecl me = (ClassDecl) node.getDefinedProperty(DECL_KEY);

        // record the fact that we are modifying the ClassDecl
        // exit if we have already done class resolution
        if (!me.addVisitor(_myClass)) {
            return null;
        }

        TypeNameNode superType = node.getSuperType();

        ClassDecl sdecl = (ClassDecl) JavaDecl.getDecl((NamedNode) superType);

        ClassDecl superClass = null;
        ClassDecl implIFace = null;

        if (sdecl.category == CG_CLASS) {
            superClass = sdecl;
        } else if (sdecl.category == CG_INTERFACE) {
            superClass = StaticResolution.OBJECT_DECL;
            implIFace = sdecl;
        }

        node.setProperty(SUPERCLASS_KEY, superClass);
        node.setProperty(INTERFACE_KEY, implIFace);

        me.setSuperClass(superClass);

        if (implIFace != null) {
            me.setInterfaces(TNLManip.addFirst(implIFace));
        }

        Scope myScope = me.getScope();

        // have members add themselves to this class's scope
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(myScope);

        TNLManip.traverseList(this, childArgs, node.getMembers());

        return null;
    }

    /** Return the Class object of this visitor. */
    public static Class visitorClass() {
        return _myClass;
    }

    /** The default visit method. Visits all child nodes with no enclosing
     *  class declaration.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        childArgs.add(NullValue.instance); // no enclosing class decl
        childArgs.add(NullValue.instance); // no enclosing class scope
        TNLManip.traverseList(this, args, node.children());
        return null;
    }

    protected void _addUserTypeToEnclosingClassScope(Object encClassScopeObject,
            Decl decl) {
        if (encClassScopeObject != NullValue.instance) {
            // this is an inner class, add to outer class's scope
            Scope encClassScope = (Scope) encClassScopeObject;
            encClassScope.add(decl);
        }
    }

    /** Given a list of ParameterNodes, return a new list of TypeNodes
     *  corresponding to the type of each parameter.
     */
    protected static List _makeTypeList(List paramList) {
        LinkedList retval = new LinkedList();

        Iterator paramItr = paramList.iterator();

        while (paramItr.hasNext()) {
            ParameterNode param = (ParameterNode) paramItr.next();

            TypeNode type = param.getDefType();

            retval.addLast(type);
        }

        return retval;
    }

    /** The package this compile unit is in. */
    protected PackageDecl _pkgDecl = null;

    /** The Class object of this visitor. */
    private static Class _myClass = new ResolveClassVisitor().getClass();
}
