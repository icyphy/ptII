/* A Java AST visitor that transforms Actor code into code suitable
   for standalone execution (without dependancies on the ptolemy.actor
   and ptolemy.data packages)

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.domains.sdf.codegen;

import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

public class ActorTransformerVisitor extends ReplacementJavaVisitor 
     implements JavaStaticSemanticConstants {
    
    public ActorTransformerVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        switch (PtolemyDecls.kind(node)) {
        
          case PtolemyDecls.TYPE_KIND_BOOLEAN_TOKEN:
          return BoolTypeNode.instance;
                    
          case PtolemyDecls.TYPE_KIND_INT_TOKEN:
          return IntTypeNode.instance;
                    
          case PtolemyDecls.TYPE_KIND_DOUBLE_TOKEN:
          return DoubleTypeNode.instance;
          
          case PtolemyDecls.TYPE_KIND_LONG_TOKEN:
          return LongTypeNode.instance;
          
          case PtolemyDecls.TYPE_KIND_COMPLEX_TOKEN:
          return PtolemyDecls.COMPLEX_TYPE;
          
          case PtolemyDecls.TYPE_KIND_FIX_TOKEN:
          return PtolemyDecls.FIX_POINT_TYPE;          
                    
          case PtolemyDecls.TYPE_KIND_OBJECT_TOKEN: 
          return StaticResolution.OBJECT_TYPE;
          
          case PtolemyDecls.TYPE_KIND_STRING_TOKEN:
          return StaticResolution.STRING_TYPE;
          
          case PtolemyDecls.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(BoolTypeNode.instance, 2);
          
          case PtolemyDecls.TYPE_KIND_INT_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(IntTypeNode.instance, 2);
                    
          case PtolemyDecls.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(DoubleTypeNode.instance, 2);          
          
          case PtolemyDecls.TYPE_KIND_LONG_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(LongTypeNode.instance, 2);          
    
          case PtolemyDecls.TYPE_KIND_COMPLEX_MATRIX_TOKEN:      
          return TypeUtility.makeArrayType(PtolemyDecls.COMPLEX_TYPE, 2);    
          
          case PtolemyDecls.TYPE_KIND_FIX_MATRIX_TOKEN:      
          return TypeUtility.makeArrayType(PtolemyDecls.FIX_POINT_TYPE, 2);                                              
          
          default:
          return node;
        }
    }
                              
    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitArrayInitTypeNode(ArrayInitTypeNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node, args);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node, args);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);                
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitThisConstructorCallNode(ThisConstructorCallNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitStaticInitNode(StaticInitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitInstanceInitNode(InstanceInitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitReturnNode(ReturnNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitThrowNode(ThrowNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    // try and catch were here

    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {    
        // transform the methods
        List oldMethodArgs = node.getArgs();
        TypeNode[] oldMethodArgTypes = TypeUtility.typeArray(oldMethodArgs);
        
        List methodArgs = 
         TNLManip.traverseList(this, null, null, oldMethodArgs);                
         
        node.setArgs(methodArgs);
                        
        // find the type (if any) of the accessed object
        
        FieldAccessNode oldFieldAccessNode = (FieldAccessNode) node.getMethod();

        ExprNode accessedObj = (ExprNode) ExprUtility.accessedObject(oldFieldAccessNode);
        
        // if this is a static method call, we can't do any more.
        if (accessedObj == null) {
           return node;
        }
           
        FieldAccessNode fieldAccessNode = 
         (FieldAccessNode) oldFieldAccessNode.accept(this, null);
         
        NameNode methodNameNode = fieldAccessNode.getName();
                      
        MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);
        
        String methodName = methodDecl.getName();        
                                                
        ExprNode firstArg = null;
        TypeNode firstArgType = null;
        boolean firstArgTypeIsScalarToken = false;
        boolean firstArgTypeIsStringToken = false;        
        
        if (oldMethodArgs.size() > 0) {
           firstArg = (ExprNode) methodArgs.get(0);
           firstArgType = oldMethodArgTypes[0];
           firstArgTypeIsScalarToken = PtolemyDecls.isScalarTokenType(
            PtolemyDecls.kind(firstArgType));
           firstArgTypeIsStringToken = TypeUtility.compareTypes(firstArgType, 
            PtolemyDecls.STRING_TOKEN_TYPE);           
        }
                        
        switch (PtolemyDecls.kind(TypeUtility.type(accessedObj))) {

          case PtolemyDecls.TYPE_KIND_BOOLEAN_TOKEN:       
          {
            if (firstArgTypeIsScalarToken) {                        
               if (methodName.equals("booleanValue")) {
                  // booleanValue() method, return the boolean
                  return accessedObj;
               } else if (methodName.equals("add") || methodName.equals("subtract")) {
                  // xor
                  return new CandNode(new CorNode(accessedObj, firstArg), 
                   new NotNode(new CandNode(accessedObj, firstArg)));                  
               } else if (methodName.equals("multiply")) {
                  return new CandNode(accessedObj, firstArg);
               } else if (methodName.equals("not")) {
                  return new NotNode(accessedObj);
               } else if (methodName.equals("zero")) {
                  return new BoolLitNode("false");               
               } else if (methodName.equals("one")) {
                  return new BoolLitNode("true");
               } else if (methodName.equals("toString")) {
                  return new MethodCallNode(new ObjectNode(
                   new NameNode(new NameNode(AbsentTreeNode.instance, "String"), 
                    "valueOf")), TNLManip.cons(accessedObj));                                         
               }
            }   

            if (firstArgTypeIsStringToken && methodName.equals("add")) {
               return new PlusNode(accessedObj, firstArg);           
            }
            
            // create a new BooleanToken, and call the method
            return new AllocateNode(PtolemyDecls.BOOLEAN_TOKEN_TYPE,
             oldMethodArgs, AbsentTreeNode.instance);               
          }
    
          case PtolemyDecls.TYPE_KIND_INT_TOKEN:
          {
            if (methodName.equals("intValue")) {
               // intValue() method, return the int
               return accessedObj;
            } 
                                    
            if (firstArgTypeIsScalarToken) {                        
               if (methodName.equals("add")) {
                  return new PlusNode(accessedObj, firstArg);
               } else if (methodName.equals("subtract")) {
                  return new MinusNode(accessedObj, firstArg);
               } else if (methodName.equals("multiply")) {
                  return new MultNode(accessedObj, firstArg);
               } else if (methodName.equals("divide")) {
                  return new DivNode(accessedObj, firstArg);
               } else if (methodName.equals("modulo")) {
                  return new RemNode(accessedObj, firstArg);
               } else if (methodName.equals("convert")) {
                  return new CastNode(IntTypeNode.instance, accessedObj);
               } else if (methodName.equals("zero")) {
                  return new BoolLitNode("false");               
               } else if (methodName.equals("one")) {
                  return new BoolLitNode("true");
               } else if (methodName.equals("toString")) {
                  return new MethodCallNode(new ObjectNode(
                   new NameNode(new NameNode(AbsentTreeNode.instance, "String"), 
                    "valueOf")), TNLManip.cons(accessedObj));                                         
               }                          
            } 
            
            if (firstArgTypeIsStringToken && methodName.equals("add")) {
               return new PlusNode(accessedObj, firstArg);           
            }
            
            // method not supported, create a new IntToken, and call the method
            // with the new old (converted) args. This may be a problem.
            return new AllocateNode(PtolemyDecls.INT_TOKEN_TYPE,
             methodArgs, AbsentTreeNode.instance);
          }
                    
          case PtolemyDecls.TYPE_KIND_DOUBLE_TOKEN:
          {
            if (methodName.equals("doubleValue")) {
               // doubleValue() method, return the double
               return accessedObj;
            } 
          
            if (firstArgTypeIsScalarToken) {                        
               if (methodName.equals("add")) {
                  return new PlusNode(accessedObj, firstArg);
               } else if (methodName.equals("subtract")) {
                  return new MinusNode(accessedObj, firstArg);
               } else if (methodName.equals("multiply")) {
                  return new MultNode(accessedObj, firstArg);
               } else if (methodName.equals("divide")) {
                  return new DivNode(accessedObj, firstArg);
               } else if (methodName.equals("convert")) {
                  return new CastNode(DoubleTypeNode.instance, accessedObj);
               } else if (methodName.equals("zero")) {
                  return new DoubleLitNode("0.0");               
               } else if (methodName.equals("one")) {
                  return new DoubleLitNode("1.0");
               } else if (methodName.equals("toString")) {
                  return new MethodCallNode(new ObjectNode(
                   new NameNode(new NameNode(AbsentTreeNode.instance, "String"), 
                    "valueOf")), TNLManip.cons(accessedObj));                                         
               }                                          
            } 
            
            if (firstArgTypeIsStringToken && methodName.equals("add")) {
               return new PlusNode(accessedObj, firstArg);           
            }
            
            // method not supported, create a new DoubleToken, and call the method
            // with the new (converted) args. This may be a problem.
            return new AllocateNode(PtolemyDecls.DOUBLE_TOKEN_TYPE,
             methodArgs, AbsentTreeNode.instance);                           
          }                    
          
          case PtolemyDecls.TYPE_KIND_LONG_TOKEN:
          {
            if (methodName.equals("longValue")) {
               // longValue() method, return the long
               return accessedObj;
            } 
                                    
            if (firstArgTypeIsScalarToken) {                        
               if (methodName.equals("add")) {
                  return new PlusNode(accessedObj, firstArg);
               } else if (methodName.equals("subtract")) {
                  return new MinusNode(accessedObj, firstArg);
               } else if (methodName.equals("multiply")) {
                  return new MultNode(accessedObj, firstArg);
               } else if (methodName.equals("divide")) {
                  return new DivNode(accessedObj, firstArg);
               } else if (methodName.equals("modulo")) {
                  return new RemNode(accessedObj, firstArg);
               } else if (methodName.equals("convert")) {
                  return new CastNode(IntTypeNode.instance, accessedObj);
               }  else if (methodName.equals("zero")) {
                  return new LongLitNode("0L");               
               } else if (methodName.equals("one")) {
                  return new LongLitNode("1L");
               } else if (methodName.equals("toString")) {
                  return new MethodCallNode(new ObjectNode(
                   new NameNode(new NameNode(AbsentTreeNode.instance, "String"), 
                    "valueOf")), TNLManip.cons(accessedObj));                                         
               }                                                                                    
            } 
            
            if (firstArgTypeIsStringToken && methodName.equals("add")) {
               return new PlusNode(accessedObj, firstArg);           
            }
            
            // method not supported, create a new LongToken, and call the method
            // with new (converted) args. This may be a problem.
            return new AllocateNode(PtolemyDecls.LONG_TOKEN_TYPE,
             methodArgs, AbsentTreeNode.instance);
          }

          case PtolemyDecls.TYPE_KIND_COMPLEX_TOKEN:
          {
            if (methodName.equals("complexValue")) {
               // complexValue() method, return the complex number
               return accessedObj;
            } 
                                    
            if (firstArgTypeIsScalarToken) {                        
               if (methodName.equals("add")) {
                  
               } else if (methodName.equals("subtract")) {
                  
               } else if (methodName.equals("multiply")) {
                  
               } else if (methodName.equals("divide")) {
                  
               } else if (methodName.equals("modulo")) {
                  
               } else if (methodName.equals("convert")) {
                  // call the constructor for Complex(), which should take the
                  // argument specified by the accessed object
                  return new AllocateNode(PtolemyDecls.COMPLEX_TYPE, 
                   TNLManip.cons(accessedObj), AbsentTreeNode.instance);
               }  else if (methodName.equals("zero")) {
                  return new ObjectNode(new NameNode(
                   new NameNode(AbsentTreeNode.instance, "Complex"), "ZERO"));
               } else if (methodName.equals("one")) {
                  return new ObjectNode(new NameNode(
                   new NameNode(AbsentTreeNode.instance, "Complex"), "ONE"));
               }                                                                                    
            } 
            
            if (firstArgTypeIsStringToken && methodName.equals("add")) {
               // change method name to 'toString'
               fieldAccessNode.setName(new NameNode(AbsentTreeNode.instance, "toString"));
               return new PlusNode(new MethodCallNode(accessedObj, new LinkedList()), 
                                   firstArg);           
            }
            
            // method not supported, create a new ComplexToken, and call the method
            // with new (converted) args. This may be a problem.
            return new MethodCallNode(
             new ObjectFieldAccessNode(methodNameNode,
              new AllocateNode(PtolemyDecls.COMPLEX_TOKEN_TYPE,
               TNLManip.cons(accessedObj), AbsentTreeNode.instance)),
              methodArgs);
          }
        
        
        }
                
        return node;
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        // get the old type
        TypeNameNode type = node.getDtype();
                
        MethodDecl constructorDecl = 
         (MethodDecl) node.getDefinedProperty(DECL_KEY);
                                
        List constructorArgs = 
         TNLManip.traverseList(this, null, null, node.getArgs());                
         
        node.setArgs(constructorArgs); 
         
        TypeNode[] constructorTypes = TypeUtility.typeArray(constructorArgs);
                                                                           
        switch (PtolemyDecls.kind(type)) {
        
          case PtolemyDecls.TYPE_KIND_BOOLEAN_TOKEN:               
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], BoolTypeNode.instance)) {
             // new BooleanToken(boolean)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new BooleanToken(???, ???, ...)
             
             // call the booleanValue() method of the created BooleanToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "booleanValue"), node), 
              new LinkedList());             
          }          
          
          case PtolemyDecls.TYPE_KIND_INT_TOKEN:          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], IntTypeNode.instance)) {
             // new IntToken(int)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new IntToken(???, ???, ...)
             
             // call the intValue() method of the created IntToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "intValue"), node), 
              new LinkedList());
          }
          
          case PtolemyDecls.TYPE_KIND_DOUBLE_TOKEN:  
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], DoubleTypeNode.instance)) {
             // new DoubleToken(double)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new DoubleToken(???, ???, ...)
             
             // call the doubleValue() method of the created DoubleToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "doubleValue"), node), 
              new LinkedList());             
          }  
          
          case PtolemyDecls.TYPE_KIND_LONG_TOKEN:          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], LongTypeNode.instance)) {
             // new LongToken(long)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new LongToken(???, ???, ...)
             
             // call the longValue() method of the created LongToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "longValue"), node), 
              new LinkedList());             
          }  
          
          case PtolemyDecls.TYPE_KIND_COMPLEX_TOKEN:                           
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], PtolemyDecls.COMPLEX_TYPE)) {
             // new ComplexToken(Complex)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new ComplexToken(???, ???, ...)
             
             // call the complexValue() method of the created ComplexToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "complexValue"), node), 
              new LinkedList());             
          }  

          case PtolemyDecls.TYPE_KIND_FIX_TOKEN:                           
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], PtolemyDecls.FIX_POINT_TYPE)) {
             // new FixToken(Fix)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new FixToken(???, ???, ...)
             
             // call the fixValue() method of the created FixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "fixValue"), node), 
              new LinkedList());             
          }  

                           
          case PtolemyDecls.TYPE_KIND_OBJECT_TOKEN:           
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], StaticResolution.OBJECT_TYPE)) {
             // new ObjectToken(Object)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new ObjectToken(???, ???, ...)
             
             // call the getValue() method of the created ObjectToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "getValue"), node), 
              new LinkedList());             
          }  
                    
          case PtolemyDecls.TYPE_KIND_STRING_TOKEN:          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], StaticResolution.STRING_TYPE)) {
             // new StringToken(String)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new StringToken(???, ???, ...)
             
             // call the stringValue() method of the created StringToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "stringValue"), node), 
              new LinkedList());             
          }  
          
          case PtolemyDecls.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(BoolTypeNode.instance, 2))) {
             // new BooleanMatrixToken(boolean[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new BooleanMatrixToken(???, ???, ...)
             
             // call the booleanMatrix() method of the created BooleanMatrixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "booleanMatrix"), node), 
              new LinkedList());                                        
          }  
          
          case PtolemyDecls.TYPE_KIND_INT_MATRIX_TOKEN:                              
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(IntTypeNode.instance, 2))) {
             // new IntMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new IntMatrixToken(???, ???, ...)
             
             // call the intMatrix() method of the created IntMatrixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "intMatrix"), node), 
              new LinkedList());                                        
          }  
          
          case PtolemyDecls.TYPE_KIND_DOUBLE_MATRIX_TOKEN:          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(DoubleTypeNode.instance, 2))) {
             // new DoubleMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new DoubleMatrixToken(???, ???, ...)
             
             // call the doubleMatrix() method of the created DoubleMatrixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "doubleMatrix"), node), 
              new LinkedList());                                        
          }  
          
          case PtolemyDecls.TYPE_KIND_LONG_MATRIX_TOKEN:    
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(LongTypeNode.instance, 2))) {
             // new LongMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new LongMatrixToken(???, ???, ...)
             
             // call the longMatrix() method of the created LongMatrixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "longMatrix"), node), 
              new LinkedList());                                        
          }  
          
          case PtolemyDecls.TYPE_KIND_COMPLEX_MATRIX_TOKEN:         
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(PtolemyDecls.COMPLEX_TYPE, 2))) {
             // new ComplexMatrixToken(Complex[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new ComplexMatrixToken(???, ???, ...)
             
             // call the complexMatrix() method of the created ComplexMatrixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "complexMatrix"), node), 
              new LinkedList());                                        
          }  
          
          case PtolemyDecls.TYPE_KIND_FIX_MATRIX_TOKEN:         
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(PtolemyDecls.FIX_POINT_TYPE, 2))) {
             // new FixMatrixToken(FixPoint[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } else {
             // new FixMatrixToken(???, ???, ...)
             
             // call the fixMatrix() method of the created FixMatrixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "fixMatrix"), node), 
              new LinkedList());                                        
          }            
        }
              
        return node;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitEQNode(EQNode node, LinkedList args) {
    
        // warn on comparison to null
        return _defaultVisit(node, args);
    }

    public Object visitNENode(NENode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public VarInitDeclNode _visitVarInitDeclNode(VarInitDeclNode node, LinkedList args) {       
        TreeNode initExpr = (TreeNode) node.getInitExpr().accept(this, null);
        node.setInitExpr(initExpr);
        
        TypeNode type = node.getDefType();
        
        if (PtolemyDecls.isSupportedTokenType(PtolemyDecls.kind(type))) {                            
           if (initExpr.classID() == NULLPNTRNODE_ID) {
              node.setInitExpr(_dummyValue(type));
           }
        }                 
        
        node.setDefType((TypeNode) type.accept(this, null));
    
        return (VarInitDeclNode) node;
    }
    
    /** Return an expression to substitute for null, when null is assigned to
     *  a Token of the given type.
     */
    protected ExprNode _dummyValue(TypeNode type) {
        switch (PtolemyDecls.kind(type)) {
    
          case PtolemyDecls.TYPE_KIND_BOOLEAN_TOKEN:       
          return new BoolLitNode("false");
    
          case PtolemyDecls.TYPE_KIND_INT_TOKEN:
          return new IntLitNode("-1");

          case PtolemyDecls.TYPE_KIND_DOUBLE_TOKEN:
          return new DoubleLitNode("-1.0");           

          case PtolemyDecls.TYPE_KIND_LONG_TOKEN:
          return new LongLitNode("-1L");

          case PtolemyDecls.TYPE_KIND_COMPLEX_TOKEN:                   
          case PtolemyDecls.TYPE_KIND_FIX_TOKEN:                             
          case PtolemyDecls.TYPE_KIND_OBJECT_TOKEN:           
          case PtolemyDecls.TYPE_KIND_STRING_TOKEN:          
          case PtolemyDecls.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:          
          case PtolemyDecls.TYPE_KIND_INT_MATRIX_TOKEN:                    
          case PtolemyDecls.TYPE_KIND_DOUBLE_MATRIX_TOKEN:          
          case PtolemyDecls.TYPE_KIND_LONG_MATRIX_TOKEN:    
          case PtolemyDecls.TYPE_KIND_COMPLEX_MATRIX_TOKEN:                 
          case PtolemyDecls.TYPE_KIND_FIX_MATRIX_TOKEN:                           
          return new NullPntrNode();           
          
          // remove this later
          case PtolemyDecls.TYPE_KIND_TOKEN:
          case PtolemyDecls.TYPE_KIND_SCALAR_TOKEN:                                         
          return new IntLitNode("0");
          
          case PtolemyDecls.TYPE_KIND_MATRIX_TOKEN:                               
          return new NullPntrNode();
          
          
        }

        ApplicationUtility.error("unexpected type for dummy() : " + type);        
        return null;        
    }       
}