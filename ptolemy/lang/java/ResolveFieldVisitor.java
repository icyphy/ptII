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

import java.util.LinkedList;
import ptolemy.lang.*;

/** A visitor that does field and method resolution.
 *  
 *  Code and comments taken from the Titanium project.
 * 
 *  @author ctsay@eecs.berkeley.edu
 */
public class ResolveFieldVisitor extends ReplacementJavaVisitor {
    public ResolveFieldVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _currentPackage = (PackageDecl) node.getDefinedProperty("thePackage");
        
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
        subCtx.inStatic = ((node.getModifiers() & Modifier.STATIC_MOD) != 0);
        
        LinkedList childArgs = TNLManip.cons(subCtx);
        
        node.setInitExpr((TreeNode) node.getInitExpr().accept(this, childArgs));    
    
        return node;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        FieldContext subCtx = new FieldContext((FieldContext) args.get(0));
        subCtx.inStatic = ((node.getModifiers() & Modifier.STATIC_MOD) != 0);
        
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
         JavaDecl.CG_CONSTRUCTOR);
         
        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));
                 
        node.setProperty("decl", StaticResolution.resolveCall(methods, node.getArgs()));    
        
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
         JavaDecl.CG_CONSTRUCTOR);
         
        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));
                 
        node.setProperty("decl", StaticResolution.resolveCall(methods, node.getArgs()));            
        
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
        
        if (!TypeUtility.isReferenceType(ot)) {
           ApplicationUtility.error("attempt to select from non-reference type " + ot);
        } else {                     
           resolveAField(node, false, false, ctx);
        }

        if (expr instanceof ThisNode) {
           return new ThisFieldAccessNode((TypeNameNode) expr.getDefinedProperty("theClass"), 
                                          node.getName()).accept(this, args);                    
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
        
        if ((d.getModifiers() & Modifier.STATIC_MOD) == 0) {        
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
        
        node.setMethod((TreeNode) node.getMethod().accept(this, TNLManip.cons(subCtx)));

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
        
        node.setEnclosingInstance((ExprNode) 
         node.getEnclosingInstance().accept(this, args));            
        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));
        
        TypeNameNode typeName = node.getDtype();
        
        if (TypeUtility.kind(typeName) != TypeUtility.TYPE_KIND_CLASS) {
           ApplicationUtility.error("can't allocate something of non-class type " +
            typeName.getName().getIdent());
        } else {
           ClassDecl typeDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) typeName);
           
           if ((typeDecl.getModifiers() & Modifier.ABSTRACT_MOD) != 0) {
              ApplicationUtility.error("cannot allocate abstract " +
               typeName.getName().getIdent());
           }
           
           EnvironIter methods = typeDecl.getEnviron().lookupFirstProper(
            typeDecl.getName(), JavaDecl.CG_CONSTRUCTOR);
            
           MethodDecl constructor = StaticResolution.resolveCall(methods, node.getArgs()); 
           
           node.setProperty("decl", constructor);                                 
        }

        // checkFieldAccess(constructor, dtype()->decl(), false, false, true,
    	//	     ctx, position());
                    
        return node;              
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        node.setEnclosingInstance((ExprNode) node.getEnclosingInstance().accept(this, args));    
        node.setSuperArgs(TNLManip.traverseList(this, node, args, node.getSuperArgs()));
    
        ClassDecl superDecl = (ClassDecl) node.getDefinedProperty("superclass");
                
        EnvironIter methods = superDecl.getEnviron().lookupFirstProper(
         superDecl.getName(), JavaDecl.CG_CONSTRUCTOR);

        MethodDecl constructor = StaticResolution.resolveCall(methods, node.getSuperArgs()); 
           
        node.setProperty("constructor", constructor);                                 

        FieldContext subCtx = new FieldContext(); 
        subCtx.currentClass = (TypeNameNode) node.getDefinedProperty("type");
        subCtx.currentClassDecl = (ClassDecl) node.getDefinedProperty("decl");
        subCtx.inStatic = false;
        subCtx.methodArgs = null;        
         
        node.setMembers(TNLManip.traverseList(this, node, TNLManip.cons(subCtx), 
         node.getMembers()));
           
        return node;
    }

    protected void resolveAField(FieldAccessNode node, boolean thisAccess, boolean isSuper, 
     FieldContext ctx) {
        EnvironIter resolutions;
        TypeNameNode oType = TypeUtility.accessedObjectType(node);
        
        ClassDecl typeDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) oType);
        JavaDecl d = null;
        LinkedList methodArgs = ctx.methodArgs;
        String nameString = node.getName().getIdent();

        if (methodArgs == null) {
           d = JavaDecl.getDecl((NamedNode) node);
           if (d == null) { // don't repeat work
           	  resolutions = typeDecl.getEnviron().
	           lookupFirstProper(nameString, JavaDecl.CG_FIELD);
	           
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
            JavaDecl.CG_METHOD);
               
           if (!resolutions.hasNext()) {
              ApplicationUtility.error("no " + nameString + " method in " +
               typeDecl.getName());
    	   } else {
       	      d = StaticResolution.resolveCall(resolutions, methodArgs);
		   }		   
        }
  
        node.getName().setProperty("decl", d);
        //checkFieldAccess(d, typeDecl, thisAccess, isSuper, false, ctx, position());
    }

    /* The default visit method comes from ReplacementJavaVisitor. */

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
        public LinkedList methodArgs = null;
    }


    /** The current package. */
    protected PackageDecl _currentPackage = null;
}    
