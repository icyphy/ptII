/* Fills in class and interface scopes with inherited members.

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// ResolveInheritanceVisitor
/** Adds inherited class and interface members to the respective
scopes  This is part 2 of pass 1, part 1 of pass 1 happens in ResolveClassVisitor.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@see ResolveClassVisitor

@author Jeff Tsay, Christopher Hylands
@version $Id$
 */
public class ResolveInheritanceVisitor extends ResolveVisitorBase
    implements JavaStaticSemanticConstants {

    /** Create a new visitor that uses the default type policy. */
    public ResolveInheritanceVisitor() {
        this(new TypePolicy(new TypeIdentifier()));
    }

    public ResolveInheritanceVisitor(TypePolicy typePolicy) {
        _typePolicy = typePolicy;
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        //System.out.println("resolveInheritance for " +
        //        node.getProperty(IDENT_KEY));

        TNLManip.traverseList(this, null, node.getDefTypes());

        //System.out.println("finished resolveInheritance for " +
        //        node.getProperty(IDENT_KEY));

        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        if (!me.addVisitor(_myClass)) {
            return null;
        }

        int modifiers = node.getModifiers();

        ClassDecl superClass = me.getSuperClass();

        if (superClass != null) {
            if ((superClass.getModifiers() & FINAL_MOD) != 0) {
                throw new RuntimeException("final class " + superClass.getName() +
                        " cannot be extended");
            }

            _fillInInheritedMembers(me, superClass);
        } else {
            if (me != StaticResolution.OBJECT_DECL) {
                throw new RuntimeException("ResolveInheritanceVisitor." +
                        "visitClassDeclNode: " + me +
                        "has no superclass, yet is not Object");
            }
        }


        Iterator iFaceItr = me.getInterfaces().iterator();

        while (iFaceItr.hasNext()) {
            _fillInInheritedMembers(me, (ClassDecl) iFaceItr.next());
        }

        if ((modifiers & ABSTRACT_MOD) == 0) {
            if (_hasAbstractMethod(node)) {
                throw new RuntimeException("ResolveInheritanceVisitor." +
                        "visitClassDeclNode(): " +  me.fullName() +
                        " has abstract methods: it must be declared abstract");
            }
        }
        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        if (!me.addVisitor(_myClass)) {
            return null;
        }

        int modifiers = node.getModifiers();

        Iterator interfaceItr = me.getInterfaces().iterator();

        while (interfaceItr.hasNext()) {
            _fillInInheritedMembers(me, (ClassDecl) interfaceItr.next());
        }

        return null;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        ClassDecl me = (ClassDecl) node.getDefinedProperty(DECL_KEY);

        if (!me.addVisitor(_myClass)) {
            return null;
        }

        ClassDecl superClass = me.getSuperClass();

        if ((superClass.getModifiers() & FINAL_MOD) != 0) {
            throw new RuntimeException("final class " + superClass.getName() +
                    " cannot be extended");
        }

        _fillInInheritedMembers(me, superClass);

        Object iFaceObj = node.getDefinedProperty(INTERFACE_KEY);

        if (iFaceObj != NullValue.instance) {
            ClassDecl iFace = (ClassDecl) iFaceObj;

            _fillInInheritedMembers(me, iFace);
        }

        return null;
    }

    /** Return the Class object of this visitor. */
    public static Class visitorClass() {
        return _myClass;
    }

    /** The default visit method. Visit all child nodes. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        TNLManip.traverseList(this, args, node.children());
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The type policy used to do comparison of types. */
    protected TypePolicy _typePolicy = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Given ClassDecls TO and FROM, add to TO's scope the members in
    // CATEGORIES (a bitwise union of category values) that TO
    // inherits from FROM.
    private void _fillInInheritedMembers(ClassDecl to,
            ClassDecl from) {
        // make sure 'from' is filled in
        TreeNode sourceNode = from.getSource();

        if ((sourceNode != null) && (sourceNode != AbsentTreeNode.instance)) {
            sourceNode.accept(this, null);
        }

        Iterator declItr = from.getScope().allLocalDecls();

        Scope toScope = to.getScope();

        while (declItr.hasNext()) {
            JavaDecl member = (JavaDecl) declItr.next();

            if (((member.category &
                    (CG_FIELD | CG_METHOD | CG_USERTYPE)) != 0) &&
                    ((member.getModifiers() & PRIVATE_MOD) == 0) &&
                    !_overriddenIn(member, to)) {
                toScope.add(member);
            }
        }
    }


    // Return true if there is at least one abstract method in the class
    // scope.
    private static boolean _hasAbstractMethod(UserTypeDeclNode node) {

        // If the user type declaration has been loaded shallowly,
        // we should not care if it has abstract methods.
        if (!ASTReflect.isDeep(node)) return false;
  
        Scope classScope = JavaDecl.getDecl((NamedNode) node).getScope();

        Iterator memberItr = classScope.allLocalDecls();

        while (memberItr.hasNext()) {
            JavaDecl member = (JavaDecl) memberItr.next();

            if ((member.category == CG_METHOD) &&
                    ((member.getModifiers() & ABSTRACT_MOD) != 0)) {
                return true;
            }
        }
        return false;
    }

    // Return true iff MEMBER would be hidden or overridden by a
    // declaration in TO.
    private boolean _overriddenIn(JavaDecl member, ClassDecl to) {
        Scope scope = to.getScope();
        String memberName = member.getName();

        if (member.category == CG_FIELD) {
            FieldDecl current = (FieldDecl)
                scope.lookupLocal(memberName, CG_FIELD);

            // Only definitions in the destination scope override fields
            // If multiple definitions are inherited, a compile-time error
            // must be reported for any use (this is achieved by letting the
            // scope contain multiple copies of the field, which will
            // produce an ambiguous reference error in the name lookup)
            // But: multiple inheritances of the *same* field only count once
            return ((current != null) &&
                    ((current.getContainer() == to) || (member == current)));

        } else if (member.category == CG_METHOD) {
            MethodDecl methodMember = (MethodDecl) member;

            Iterator methodItr =
                scope.lookupFirstLocal(memberName, CG_METHOD);

            while (methodItr.hasNext()) {

                MethodDecl d = (MethodDecl) methodItr.next();

                if (_typePolicy.doMethodsConflict(d, methodMember)) {

                    // Note: member is overriden method, d is overriding method

                    if (d == methodMember) {
                        return true; // seeing the same thing twice
                    }

                    boolean isLocalDecl = (d.getContainer() == to);
                    int dm = d.getModifiers();
                    int mm = methodMember.getModifiers();

                    // Note: if !isLocalDecl, then the method necessarily comes
                    // from an interface (i.e. is abstract). If d is also abstract,
                    // then all methods are inherited (j8.4.6.4).
                    boolean inheritAllAbstract =
                        (!isLocalDecl && ((dm & ABSTRACT_MOD) != 0));

                    TypeNode dtype = d.getType();

                    if (!_typePolicy.compareTypes(d.getType(),
                            methodMember.getType())) {
  	                throw new RuntimeException(to.getName() +
                                ": overriding of " + memberName +
                                " changes return type: " +
                                d.getType() + " vs. " +
                                methodMember.getType());
                    }

                    if ((dm & STATIC_MOD) != (mm & STATIC_MOD)) {
                        throw new RuntimeException("overriding of "
                                + memberName + " adds/removes 'static'");
                    }

                    // make sure d was a legal override/hide of member
                    if ((mm & FINAL_MOD) != 0) {
                        throw new RuntimeException(to.getName() +
                                ": cannot override final " +
                                memberName);
                    }

                    /*
                      if ((mm & PUBLIC_MOD) &&
                      ((dm & PUBLIC_MOD) == 0) ||
                      ((mm & PROTECTED_MOD) != 0) &&
       		      !(dm & (PUBLIC_MOD | PROTECTED_MOD)) ||
                      !(mm & (PUBLIC_MOD | PROTECTED_MOD)) &&
                      (dm & PRIVATE_MOD)) {
                      throw new RuntimeException("overriding of " + memberName +
                      " must provide at least as much access");
                      }
                    */

                    if (!inheritAllAbstract &&
                            !_throwsSubset(d.getThrows(),
                                    methodMember.getThrows())) {
                        throw new RuntimeException(d.getName() +
                                " throws more exceptions than overridden " +
                                memberName);
                    }

                    // update overriding/hiding information for
                    // declarations of 'to'
                    if (isLocalDecl) {
                        methodMember.addOverrider(d);

	                switch (member.getContainer().category) {
                        case CG_CLASS:
		            d.setOverrides(methodMember);
		            break;

                        case CG_INTERFACE:
                            d.addImplement(methodMember);
		            break;
                        }
                    }

                    return !inheritAllAbstract;
                } // if (doMethodsConflict(dtype, mtype))
            } // while methodItr.hasNext()
            return false;
        } else if (member.category == CG_CLASS) {
            // Declared inner classes override those in outer scope.
            return true;
        } else if (member.category == CG_INTERFACE) {
            // Declared inner classes override those in outer scope.
            return true;
        }

        return false;
    }

    // Return true iff newThrows is a "subset" of oldThrows (j8.4.4)
    private static boolean _throwsSubset(Set newThrows,
            Set oldThrows) {
        // FIXME : even Titanium appears to be having trouble
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Class object of this visitor. */
    private static Class _myClass = new ResolveInheritanceVisitor().getClass();
}
