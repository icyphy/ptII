/*
Fills in class and interface environments with inherited members.

Code and comments adopted from st-inherit.cc from the Titanium project.

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
import java.util.LinkedList;
import java.util.Set;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// ResolveInheritanceVisitor
/** Fills in class and interface environments with inherited members.
 *  Code and comments adopted from st-inherit.cc from the Titanium project.
 *
 *  @author Jeff Tsay
 */
public class ResolveInheritanceVisitor extends ResolveVisitorBase 
       implements JavaStaticSemanticConstants {
       
    /** Create a new visitor that uses the default type policy. */
    public ResolveInheritanceVisitor() {
        this(new TypeVisitor());
    }

    public ResolveInheritanceVisitor(TypeVisitor typeVisitor) {
        _typeVisitor = typeVisitor;
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        ApplicationUtility.trace("resolveInheritance for " +
         node.getProperty(IDENT_KEY));
        
        TNLManip.traverseList(this, node, null, node.getDefTypes());
        
        ApplicationUtility.trace("finished resolveInheritance for " +
         node.getProperty(IDENT_KEY));
        
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
     	       ApplicationUtility.error("final class " + superClass.getName() +
	            " cannot be extended");
           }

           _fillInInheritedMembers(me, superClass);
        } else {
           ApplicationUtility.assert(me == StaticResolution.OBJECT_DECL);
        }
        

        Iterator iFaceItr = me.getInterfaces().iterator();

        while (iFaceItr.hasNext()) {
           _fillInInheritedMembers(me, (ClassDecl) iFaceItr.next());
        }

        if ((modifiers & ABSTRACT_MOD) == 0) {
           if (_hasAbstractMethod(node)) {
	           ApplicationUtility.error(me.getName() +
               " has abstract methods: must be declared abstract");
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
   	       ApplicationUtility.error("final class " + superClass.getName() +
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
  
    /** Return true iff newThrows is a "subset" of oldThrows (j8.4.4) */
    protected static boolean _throwsSubset(Set newThrows,
     Set oldThrows) {
        // FIXME : even Titanium appears to be having trouble
        return true;
    }

    /** Return true iff MEMBER would be hidden or overridden by a declaration in TO.
     */
    protected boolean _overriddenIn(JavaDecl member, ClassDecl to) {
        Environ env = to.getEnviron();
        String memberName = member.getName();

        if (member.category == CG_FIELD) {
           FieldDecl current = (FieldDecl)
            env.lookupProper(memberName, CG_FIELD);

           // Only definitions in the destination environment override fields
           // If multiple definitions are inherited, a compile-time error
           // must be reported for any use (this is achieved by letting the
           // environment contain multiple copies of the field, which will
           // produce an ambiguous reference error in the name lookup)
           // But: multiple inheritances of the *same* field only count once
           return ((current != null) &&
                   ((current.getContainer() == to) || (member == current)));

        } else if (member.category == CG_METHOD) {
           MethodDecl methodMember = (MethodDecl) member;

           Iterator methodItr =
            env.lookupFirstProper(memberName, CG_METHOD);

           while (methodItr.hasNext()) {

              MethodDecl d = (MethodDecl) methodItr.next();

	          if (_typeVisitor.doMethodsConflict(d, methodMember)) {

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

	             if (!_typeVisitor.compareTypes(d.getType(),
                     methodMember.getType())) {
  	                ApplicationUtility.error("overriding of " + memberName +
                    " changes return type");
                 }

	             if ((dm & STATIC_MOD) != (mm & STATIC_MOD)) {
		            ApplicationUtility.error("overriding of "
                    + memberName + " adds/removes 'static'");
                 }

    	         // make sure d was a legal override/hide of member
   	             if ((mm & FINAL_MOD) != 0) {
		            ApplicationUtility.error("cannot override final " +
                    memberName);
                 }

                 /*
                 if ((mm & PUBLIC_MOD) &&
                     ((dm & PUBLIC_MOD) == 0) ||
       	          ((mm & PROTECTED_MOD) != 0) &&
       		      !(dm & (PUBLIC_MOD | PROTECTED_MOD)) ||
		              !(mm & (PUBLIC_MOD | PROTECTED_MOD)) &&
	   	              (dm & PRIVATE_MOD)) {
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
           return true; // declared inner classes override those in outer scope 
        } else if (member.category == CG_INTERFACE) {
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
                (CG_FIELD | CG_METHOD | CG_USERTYPE)) != 0) &&
               ((member.getModifiers() & PRIVATE_MOD) == 0) &&
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

          if ((member.category == CG_METHOD) &&
              ((member.getModifiers() & ABSTRACT_MOD) != 0)) {              
             ApplicationUtility.trace("found abstract method: " +  member);
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
   
    /** A TypeVisitor used to do comparison of types. */
    protected TypeVisitor _typeVisitor = null;
    
    /** The Class object of this visitor. */
    private static Class _myClass = new ResolveInheritanceVisitor().getClass();
    
   
    
}
