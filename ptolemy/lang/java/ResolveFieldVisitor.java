/* 
Resolve fields, overloading, and do other random semantic checks.
Code adopted from st-field.cc from the Titanium project.

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
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A visitor that does field and method resolution.
 *  
 *  Code and comments taken from the Titanium project.
 * 
 *  @author Jeff Tsay
 */
public class ResolveFieldVisitor extends ReplacementJavaVisitor 
       implements JavaStaticSemanticConstants {
    public ResolveFieldVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _currentPackage = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);
        
        LinkedList childArgs = TNLManip.cons(new FieldContext());
    
        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());  
    
        return node;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        FieldContext subCtx = new FieldContext((FieldContext) args.get(0));
        ClassDecl d = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        subCtx.currentClass = d.getDefType();
        subCtx.currentClassDecl = d;
                
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        TNLManip.traverseList(this, node, childArgs, node.getMembers());
              
        return node;    
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        FieldContext subCtx = new FieldContext((FieldContext) args.get(0));
        subCtx.inStatic = ((node.getModifiers() & STATIC_MOD) != 0);
        
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        node.setInitExpr((TreeNode) node.getInitExpr().accept(this, childArgs));    
    
        return node;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        FieldContext subCtx = new FieldContext((FieldContext) args.get(0));
        subCtx.inStatic = ((node.getModifiers() & STATIC_MOD) != 0);
        
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        node.setParams(TNLManip.traverseList(this, node, childArgs, node.getParams()));
        node.setBody((TreeNode) node.getBody().accept(this, childArgs));
        
        return node;        
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        ClassDecl classDecl = 
         (ClassDecl) JavaDecl.getDecl((NamedNode) ctx.currentClass);
        String className = classDecl.getName();
        
        if (!node.getName().getIdent().equals(className)) {
           ApplicationUtility.error("constructor for " + className + " must be named " +
            className);
        }

        node.setParams(TNLManip.traverseList(this, node, args, node.getParams()));
        node.setConstructorCall((ConstructorCallNode) 
         node.getConstructorCall().accept(this, args));                
        node.setBody((BlockNode) node.getBody().accept(this, args));

        return node;    
    }

    public Object visitThisConstructorCallNode(ThisConstructorCallNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        ClassDecl classDecl = ctx.currentClassDecl;
        Environ classEnv = classDecl.getEnviron();
        
        EnvironIter methods = classEnv.lookupFirstProper(classDecl.getName(), 
         CG_CONSTRUCTOR);
         
        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));
                 
        node.setProperty(DECL_KEY, resolveCall(methods, node.getArgs()));    
        
        // checkFieldAccess omitted 
        
        return node;
    }

    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
                              
        ClassDecl superDecl = ctx.currentClassDecl.getSuperClass();    
        
        if (superDecl == null) {
           // the class is 'Object'
           return AbsentTreeNode.instance;        
        }
        
        Environ superEnv = superDecl.getEnviron();
        
        EnvironIter methods = superEnv.lookupFirstProper(superDecl.getName(), 
         CG_CONSTRUCTOR);
         
        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));
                 
        node.setProperty(DECL_KEY, resolveCall(methods, node.getArgs()));            
        
        // checkFieldAccess(decl(), ctx->currentClass->decl()->superClass(),
		   //true, true, true, ctx, position());        
        return node;
    }

    public Object visitStaticInitNode(StaticInitNode node, LinkedList args) {
        FieldContext subCtx = new FieldContext((FieldContext) args.get(0));
        
        subCtx.inStatic = true;
                
        LinkedList childArgs = TNLManip.cons(subCtx);
    
        node.getBlock().accept(this, childArgs);
    
        return node;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        FieldContext subCtx = new FieldContext((FieldContext) args.get(0));
        ClassDecl d = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        subCtx.currentClass = d.getDefType();
                
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        TNLManip.traverseList(this, node, childArgs, node.getMembers());
              
        return node;    
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic) {
           ApplicationUtility.error("cannot use 'this' in static code");
        }

        return node;           
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);  
    
        FieldContext subCtx = new FieldContext(ctx);
        
        subCtx.methodArgs = null;
        
        // FIXME : getObject() should return an expression after name resolution
        ExprNode expr = (ExprNode) node.getObject().accept(this, TNLManip.cons(subCtx));
             
        node.setObject(expr);

        TypeNode ot = TypeUtility.type(expr);
        
        if (!(TypeUtility.isReferenceType(ot) || TypeUtility.isArrayType(ot))) {
           ApplicationUtility.error("attempt to select from non-reference type " + ot);
        } else {                                           
           resolveAField(node, false, false, ctx);
        }

        if (expr.classID() == THISNODE_ID) {
           ThisFieldAccessNode retval = new ThisFieldAccessNode(node.getName());
           
           retval.setProperty(THIS_CLASS_KEY, 
            (TypeNameNode) expr.getDefinedProperty(THIS_CLASS_KEY));
                       
           return retval.accept(this, args);                    
        } 
        
        return node;
    } 
        
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
    
        if (ctx.inStatic) {
           ApplicationUtility.error("cannot use 'super' in static code");
        }
        
        resolveAField(node, true, true, ctx);

        return node;    
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
    
        resolveAField(node, true, false, ctx);
        
        JavaDecl d = JavaDecl.getDecl((NamedNode) node);
        
        if ((d.getModifiers() & STATIC_MOD) == 0) {        
           ClassDecl typeDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) node.getFType());
           
           if (!TypeUtility.isSubClass(ctx.currentClassDecl, typeDecl)) {
              ApplicationUtility.error("access to non-static " + d.getName() +
               " that does not exist in this class");
           }                 
        }

        return node;    
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
    
        resolveAField(node, true, false, ctx);
    
        return node;    
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        
        TNLManip.traverseList(this, node, args, node.getArgs());

        FieldContext subCtx = new FieldContext(ctx);
        
        subCtx.methodArgs = node.getArgs();
                         
        node.setMethod((ExprNode) node.getMethod().accept(this, TNLManip.cons(subCtx)));
        return node;        
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic) {
           ApplicationUtility.error("cannot use 'this' in static code");
        }     
        return node;
    }

    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic) {
           ApplicationUtility.error("cannot use 'super' in static code");
        }           
        return node;         
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        //  dtype()->resolveField (ctx, NULL);
        FieldContext ctx = (FieldContext) args.get(0);
        
        if (!ctx.inStatic && 
            (node.getEnclosingInstance() == AbsentTreeNode.instance)) {
           ThisNode thisNode = new ThisNode();
           
           // duplicates what's done by ResolveNameVisitor
           thisNode.setProperty(THIS_CLASS_KEY, ctx.currentClass);              
           node.setEnclosingInstance((TreeNode) thisNode.accept(this, args));           
        } else {                        
           node.setEnclosingInstance((TreeNode) 
            node.getEnclosingInstance().accept(this, args));            
        }
        
        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));
        
        TypeNameNode typeName = node.getDtype();
        
        if (TypeUtility.kind(typeName) != TypeUtility.TYPE_KIND_CLASS) {
           ApplicationUtility.error("can't allocate something of non-class type " +
            typeName.getName().getIdent());
        } else {
           ClassDecl typeDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) typeName);
           
           if ((typeDecl.getModifiers() & ABSTRACT_MOD) != 0) {
              ApplicationUtility.error("cannot allocate abstract " +
               typeName.getName().getIdent());
           }
           
           EnvironIter methods = typeDecl.getEnviron().lookupFirstProper(
            typeDecl.getName(), CG_CONSTRUCTOR);
            
           MethodDecl constructor = resolveCall(methods, node.getArgs()); 
           
           node.setProperty(DECL_KEY, constructor);                                 
        }

        // checkFieldAccess(constructor, dtype()->decl(), false, false, true,
    	//	     ctx, position());
                    
        return node;              
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);        
          
        if (!ctx.inStatic && 
            (node.getEnclosingInstance() == AbsentTreeNode.instance)) {
           ThisNode thisNode = new ThisNode();
           
           // duplicates what's done by ResolveNameVisitor
           thisNode.setProperty(THIS_CLASS_KEY, ctx.currentClass);              
           node.setEnclosingInstance((TreeNode) thisNode.accept(this, args));           
        } else {                        
           node.setEnclosingInstance((TreeNode) 
            node.getEnclosingInstance().accept(this, args));            
        }
        
        node.setSuperArgs(TNLManip.traverseList(this, node, args, node.getSuperArgs()));
    
        ClassDecl superDecl = (ClassDecl) node.getDefinedProperty(SUPERCLASS_KEY);
                
        EnvironIter methods = superDecl.getEnviron().lookupFirstProper(
         superDecl.getName(), CG_CONSTRUCTOR);

        MethodDecl constructor = resolveCall(methods, node.getSuperArgs()); 
           
        node.setProperty(CONSTRUCTOR_KEY, constructor);                                 

        FieldContext subCtx = new FieldContext(); 
        subCtx.currentClass = (TypeNameNode) node.getDefinedProperty(TYPE_KEY);
        subCtx.currentClassDecl = (ClassDecl) node.getDefinedProperty(DECL_KEY);
        subCtx.inStatic = false;
        subCtx.methodArgs = null;        
         
        node.setMembers(TNLManip.traverseList(this, node, TNLManip.cons(subCtx), 
         node.getMembers()));
           
        return node;
    }
    
    // default visit is from ResolveVisitorBase
    
    protected void resolveAField(FieldAccessNode node, boolean thisAccess, boolean isSuper, 
     FieldContext ctx) {
        EnvironIter resolutions;
        TypeNode oType = TypeUtility.accessedObjectType(node);
        ClassDecl typeDecl;        
        
        if (oType.classID() == ARRAYTYPENODE_ID) {
           typeDecl = StaticResolution.ARRAY_CLASS_DECL;
        } else {
           typeDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) oType);
        }
        
        JavaDecl d = null;
        List methodArgs = ctx.methodArgs;
        String nameString = node.getName().getIdent();

        if (methodArgs == null) {
           d = JavaDecl.getDecl((NamedNode) node);
           if (d == null) { // don't repeat work
           	  resolutions = typeDecl.getEnviron().lookupFirstProper(nameString, CG_FIELD);
	           
      	      if (!resolutions.hasNext()) {
            	 ApplicationUtility.error ("no " + nameString + " field in " +
         		   typeDecl.getName());
           	  } else {
         	    d = (JavaDecl) resolutions.nextDecl();
        	    if (resolutions.hasNext()) {
         	      ApplicationUtility.error("ambiguous reference to " + d.getName());
            	}
              }
           } 
        } else {
           resolutions = typeDecl.getEnviron().lookupFirstProper(nameString, 
            CG_METHOD);
               
           if (!resolutions.hasNext()) {
              ApplicationUtility.error("no " + nameString + " method in " +
                typeDecl.getName());
    	   } else {
       	      d = resolveCall(resolutions, methodArgs);
		   }		   
        }
  
        node.getName().setProperty(DECL_KEY, d);
        //checkFieldAccess(d, typeDecl, thisAccess, isSuper, false, ctx, position());
    }

    public static MethodDecl resolveCall(EnvironIter methods, List args) {

        Decl aMethod = methods.head();      
        Decl d;
   
        LinkedList types = new LinkedList();
      
        LinkedList argTypes = new LinkedList();
  
        Iterator argsItr = args.iterator();
      
        while (argsItr.hasNext()) {
           ExprNode expr = (ExprNode) argsItr.next();
           argTypes.addLast(TypeUtility.type(expr));
        }
      
        LinkedList matches = new LinkedList();
                
        while (methods.hasNext()) {
           MethodDecl method = (MethodDecl) methods.next();
           if (TypeUtility.isCallableWith(method, argTypes)) {
              matches.addLast(method);
           }          
        }
      
        if (matches.size() == 0) {
           ApplicationUtility.error("no matching " + aMethod.getName() +
            "(" + TNLManip.toString(argTypes) + ")");
        }
       
        Iterator matchesItr1 = matches.iterator();
      
        while (matchesItr1.hasNext()) {
           MethodDecl m1 = (MethodDecl) matchesItr1.next();
           Iterator matchesItr2 = matches.iterator();
           boolean thisOne = true;
         
           while (matchesItr2.hasNext()) {
              MethodDecl m2 = (MethodDecl) matchesItr2.next();
              if (m1 == m2) {
                continue; // get out of this inner loop      
              }
              if (!TypeUtility.isMoreSpecific(m1, m2) || TypeUtility.isMoreSpecific(m2, m1)) {
                 thisOne = false; // keep looking
                 continue; // get out of this inner loop      
              }             
           } 
                   
           if (thisOne) {
              return m1;
           }
        }
      
        ApplicationUtility.error ("ambiguous method call to " + aMethod.getName());
        return null;
    }

    protected static class FieldContext {
        public FieldContext() {}

        public FieldContext(FieldContext ctx) {
            currentClass = ctx.currentClass;
            currentClassDecl = ctx.currentClassDecl;
            inStatic = ctx.inStatic;
            methodArgs = ctx.methodArgs;
        }
        /** The type representing the class we are currently in. */
        public TypeNameNode currentClass = null;
        
        /** The declaration representing the class we are currently in. */       
        public ClassDecl currentClassDecl = null;
        
        /** A flag indicating that we are in static code. */
        public boolean inStatic = false;
        
        /** A list of the method arguments. */
        public List methodArgs = null;
    }

    /** The current package. */
    protected PackageDecl _currentPackage = null;
}    
