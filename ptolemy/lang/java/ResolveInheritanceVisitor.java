/*
Fills in class and interface environments with inherited members.

Code and comments adopted from st-inherit.cc from the Titanium project.

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

import java.util.Set;
import java.util.LinkedList;
import java.util.Iterator;

import ptolemy.lang.*;

public class ResolveInheritanceVisitor extends ResolveVisitorBase {
    public ResolveInheritanceVisitor() {
        super();
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        TNLManip.traverseList(this, node, null, node.getDefTypes());
        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        if (!me.addVisitor("ResolveInheritance")) {
           return null;
        }

        int modifiers = node.getModifiers();

        ClassDecl superClass = me.getSuperClass();

        if (superClass != null) {
           if ((superClass.getModifiers() & Modifier.FINAL_MOD) != 0) {
     	       ApplicationUtility.error("final class " + superClass.getName() +
	            " cannot be extended");
           }

           _fillInInheritedMembers(me, superClass);
        }

        Iterator iFaceItr = me.getInterfaces().iterator();

        while (iFaceItr.hasNext()) {
           _fillInInheritedMembers(me, (ClassDecl) iFaceItr.next());
        }

        if ((modifiers & Modifier.ABSTRACT_MOD) == 0) {
           if (_hasAbstractMethod(node)) {
	           ApplicationUtility.error(me.getName() +
               " has abstract methods: must be declared abstract");
           }
        }
        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        if (!me.addVisitor("ResolveInheritance")) {
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
        ClassDecl me = (ClassDecl) node.getDefinedProperty("decl");

        if (!me.addVisitor("ResolveInheritance")) {
           return null;
        }

        ClassDecl superClass = me.getSuperClass();

        if ((superClass.getModifiers() & Modifier.FINAL_MOD) != 0) {
   	       ApplicationUtility.error("final class " + superClass.getName() +
	        " cannot be extended");
        }

        _fillInInheritedMembers(me, superClass);
        
        Object iFaceObj = node.getDefinedProperty("implements");
        
        if (iFaceObj != NullValue.instance) {
           ClassDecl iFace = (ClassDecl) iFaceObj;
           
           _fillInInheritedMembers(me, iFace);        
        }
        
        return null;                   
    }
 
    /** Returns true if newThrows is a "subset" of oldThrows (j8.4.4) */
    protected static boolean _throwsSubset(Set newThrows,
     Set oldThrows) {
        // FIXME : even Titanium appears to be having trouble
        return true;
    }

    /** True iff MEMBER would be hidden or overridden by a declaration in TO.
     */
    protected static boolean _overriddenIn(JavaDecl member, ClassDecl to) {
        Environ env = to.getEnviron();
        String memberName = member.getName();

        if (member.category == JavaDecl.CG_FIELD) {
           FieldDecl current = (FieldDecl)
            env.lookupProper(memberName, JavaDecl.CG_FIELD);

           // Only definitions in the destination environment override fields
           // If multiple definitions are inherited, a compile-time error
           // must be reported for any use (this is achieved by letting the
           // environment contain multiple copies of the field, which will
           // produce an ambiguous reference error in the name lookup)
           // But: multiple inheritances of the *same* field only count once
           return ((current != null) &&
                   ((current.getContainer() == to) || (member == current)));

        } else if (member.category == JavaDecl.CG_METHOD) {
           MethodDecl methodMember = (MethodDecl) member;

           Iterator methodItr =
            env.lookupFirstProper(memberName, JavaDecl.CG_METHOD);

           while (methodItr.hasNext()) {

              MethodDecl d = (MethodDecl) methodItr.next();

	          if (d.conflictsWith(methodMember)) {

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
                  (!isLocalDecl && ((dm & Modifier.ABSTRACT_MOD) != 0));

    	         TypeNode dtype = d.getType();

	             if (!TypeUtility.compareTypes(d.getType(),
                     methodMember.getType())) {
  	                ApplicationUtility.error("overriding of " + memberName +
                    " changes return type");
                 }

	             if ((dm & Modifier.STATIC_MOD) != (mm & Modifier.STATIC_MOD)) {
		            ApplicationUtility.error("overriding of "
                    + memberName + " adds/removes 'static'");
                 }

    	         // make sure d was a legal override/hide of member
   	             if ((mm & Modifier.FINAL_MOD) != 0) {
		            ApplicationUtility.error("cannot override final " +
                    memberName);
                 }

                 /*
                 if ((mm & Modifier.PUBLIC_MOD) &&
                     ((dm & Modifier.PUBLIC_MOD) == 0) ||
       	          ((mm & Modifier.PROTECTED_MOD) != 0) &&
       		      !(dm & (Modifier.PUBLIC_MOD | Modifier.PROTECTED_MOD)) ||
		              !(mm & (Modifier.PUBLIC_MOD | Modifier.PROTECTED_MOD)) &&
	   	              (dm & Modifier.PRIVATE_MOD)) {
                    ApplicationUtility.error("overriding of " + memberName +
                     " must provide at least as much access");
                 }
                 */

     	         if (!inheritAllAbstract &&
    		         !_throwsSubset(d.getThrows(), methodMember.getThrows())) {
	  	            ApplicationUtility.error(d.getName() +
                    " throws more exceptions than overridden " + memberName);
                 }

   	             // update overriding/hiding information for declarations of 'to'
	             if (isLocalDecl) {
		            methodMember.addOverrider(d);

	                switch (member.getContainer().category) {
		            case JavaDecl.CG_CLASS:
		            d.setOverrides(methodMember);
		            break;

		            case JavaDecl.CG_INTERFACE:
                    d.addImplement(methodMember);
		            break;
                    }
		         }
                 return !inheritAllAbstract;
              } // if (_methodsConflict(dtype, mtype))
	       } // while methodItr.hasNext()
           return false;
	    } else if (member.category == JavaDecl.CG_CLASS) {
           return true; // declared inner classes override those in outer scope 
        } else if (member.category == JavaDecl.CG_INTERFACE) {
           return true; // declared inner classes override those in outer scope 
        }

        return false;
    }

    /** Given ClassDecls TO and FROM, add to TO's environ the members in
     *  CATEGORIES (a bitwise union of category values) that TO
     *  inherits from FROM.
     */
    protected void _fillInInheritedMembers(ClassDecl to,
     ClassDecl from) {
        // make sure 'from' is filled in
        TreeNode sourceNode = from.getSource();

        if ((sourceNode != null) && (sourceNode != AbsentTreeNode.instance)) {
           sourceNode.accept(this, null);
        }

        Iterator declItr = from.getEnviron().allProperDecls();

        Environ toEnviron = to.getEnviron();

        while (declItr.hasNext()) {
           JavaDecl member = (JavaDecl) declItr.next();

           if (((member.category &
                (JavaDecl.CG_FIELD | JavaDecl.CG_METHOD | JavaDecl.CG_USERTYPE))
               != 0) &&
               ((member.getModifiers() & Modifier.PRIVATE_MOD) == 0) &&
               !_overriddenIn(member, to)) {
              toEnviron.add(member);
           }
        }
    }

    /** Return true if there is at least one abstract method in the class
     *  environment.
     */
    protected static boolean _hasAbstractMethod(UserTypeDeclNode node) {
       Environ classEnv = JavaDecl.getDecl((NamedNode) node).getEnviron();

       Iterator memberItr = classEnv.allProperDecls();

       while (memberItr.hasNext()) {
          JavaDecl member = (JavaDecl) memberItr.next();

          if ((member.category == JavaDecl.CG_METHOD) &&
              ((member.getModifiers() & Modifier.ABSTRACT_MOD) != 0)) {
             return true;
          }
       }
       return false;
    }

    /** The default visit method. Visit all child nodes. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        TNLManip.traverseList(this, node, args, node.children());
        return null;
    }
}
