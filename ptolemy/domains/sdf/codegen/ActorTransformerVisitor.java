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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A Java AST visitor that transforms Actor code into code suitable
 *  for standalone execution (without dependancies on the ptolemy.actor
 *  and ptolemy.data packages)
 *
 *  @author Jeff Tsay
 */
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
                              
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _actorInfo = (PerActorCodeGeneratorInfo) args.get(0);
    
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
                               
        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();
        
        // if this is a static method call, we can't do any more.
        if (fieldAccessNode instanceof TypeFieldAccessNode) {
           node.setMethod((FieldAccessNode) fieldAccessNode.accept(this, null));
           return node;
        }
        
        ExprNode accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);   

        // save the kind of the old accessed object    
        int accessedObjKind = PtolemyDecls.kind(TypeUtility.type(accessedObj));        
        
        fieldAccessNode = (FieldAccessNode) fieldAccessNode.accept(this, null);
         
        node.setMethod(fieldAccessNode); 
        
        accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);
                            
        MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);        
        String methodName = methodDecl.getName();        
                                                
        ExprNode firstArg = null;
        TypeNode firstArgType = null;
        boolean firstArgTypeIsScalarToken = false;
        boolean firstArgTypeIsStringToken = false;        
        
        if (oldMethodArgs.size() > 0) {
           firstArg = (ExprNode) methodArgs.get(0);
           firstArgType = oldMethodArgTypes[0];
           firstArgTypeIsScalarToken = PtolemyDecls.isScalarTokenKind(
            PtolemyDecls.kind(firstArgType));
           firstArgTypeIsStringToken = TypeUtility.compareTypes(firstArgType, 
            PtolemyDecls.STRING_TOKEN_TYPE);           
        }
                                          
        if (PtolemyDecls.isSupportedTokenKind(accessedObjKind)) {
            boolean accessedObjIsMatrix = PtolemyDecls.isMatrixTokenKind(accessedObjKind);     
            boolean accessedObjIsScalar = PtolemyDecls.isScalarTokenKind(accessedObjKind);     
            boolean accessedObjIsBoolean = 
             (accessedObjKind == PtolemyDecls.TYPE_KIND_BOOLEAN_TOKEN);
        
           if (methodName.equals("booleanValue")) {
              return new CastNode(BoolTypeNode.instance, accessedObj);
           } else if (methodName.equals("intValue")) {
              return new CastNode(IntTypeNode.instance, accessedObj);
           } else if (methodName.equals("longValue")) {
              return new CastNode(LongTypeNode.instance, accessedObj);
           } else if (methodName.equals("doubleValue")) {
              return new CastNode(DoubleTypeNode.instance, accessedObj);
           } else if (methodName.equals("complexValue")) {
              return new CastNode(PtolemyDecls.COMPLEX_TYPE, accessedObj);
           } else if (methodName.equals("fixValue")) {
              return new CastNode(PtolemyDecls.FIX_POINT_TYPE, accessedObj);
           } else if (methodName.equals("booleanMatrix")) {
              return new CastNode(TypeUtility.makeArrayType(BoolTypeNode.instance, 2), 
               accessedObj);
           } else if (methodName.equals("intMatrix")) {
              return new CastNode(TypeUtility.makeArrayType(IntTypeNode.instance, 2), 
               accessedObj);
           } else if (methodName.equals("longMatrix")) {
              return new CastNode(TypeUtility.makeArrayType(LongTypeNode.instance, 2), 
               accessedObj);                                         
           } else if (methodName.equals("doubleMatrix")) {
              return new CastNode(TypeUtility.makeArrayType(DoubleTypeNode.instance, 2), 
               accessedObj);
           } else if (methodName.equals("complexMatrix")) {
              return new CastNode(TypeUtility.makeArrayType(PtolemyDecls.COMPLEX_TYPE, 2), 
               accessedObj);
           } else if (methodName.equals("fixMatrix")) {
              return new CastNode(TypeUtility.makeArrayType(PtolemyDecls.FIX_POINT_TYPE, 2), 
               accessedObj);
           } else if (methodName.equals("add")) {
              return new PlusNode(accessedObj, firstArg);               
           } else if (methodName.equals("addReverse")) {
              return new PlusNode(firstArg, accessedObj);                                 
           } else if (methodName.equals("subtract")) {
              return new MinusNode(accessedObj, firstArg);
           } else if (methodName.equals("subtractReverse")) {
              return new MinusNode(firstArg, accessedObj);                  
           } else if (methodName.equals("multiply")) {
              return new MultNode(accessedObj, firstArg);
           } else if (methodName.equals("multiplyReverse")) {
              return new MultNode(firstArg, accessedObj);                  
           } else if (methodName.equals("divide")) {
              return new DivNode(accessedObj, firstArg);
           } else if (methodName.equals("divideReverse")) {
              return new DivNode(firstArg, accessedObj);                  
           } else if (methodName.equals("modulo")) {
              return new RemNode(accessedObj, firstArg);
           } else if (methodName.equals("moduloReverse")) {
              return new RemNode(firstArg, accessedObj);                  
           } else if (methodName.equals("convert")) {
              // this requires that name, field, and type resolution be redone to avoid
              // abstract types
              
              // what if the accessed object is of type Token, from a return value of
              // a method??
              return new CastNode(PtolemyDecls.typeNodeForKind(accessedObjKind), firstArg);
           } else if (methodName.equals("zero")) {
              if (accessedObjIsBoolean) {
                 return new BoolLitNode("false");
              } else if (accessedObjIsScalar) {
                 return new IntLitNode("0");    
              }                  
           } else if (methodName.equals("one")) {
              if (accessedObjIsBoolean) {
                 return new BoolLitNode("true");
              } else if (accessedObjIsScalar) {
                 return new IntLitNode("1");    
              }                                                                
           } else if (accessedObjIsScalar && methodName.equals("isEqualTo")) {
              return new EQNode(accessedObj, firstArg);              
           } else if (methodName.equals("getColumnCount")) {
              return new ObjectFieldAccessNode( 
               new NameNode(AbsentTreeNode.instance, "length"),
               new ArrayAccessNode(accessedObj, new IntLitNode("0")));
           } else if (methodName.equals("getRowCount")) {
              return new ObjectFieldAccessNode( 
               new NameNode(AbsentTreeNode.instance, "length"),
               accessedObj);                              
           } else if (methodName.equals("getElementAsToken") || 
                      methodName.equals("getElementAt")) {
              ExprNode secondArg = (ExprNode) methodArgs.get(1);
              return new ArrayAccessNode(
               new ArrayAccessNode(accessedObj, firstArg), secondArg);                          
           } else if (methodName.equals("toString")) {

              // uh duh ,...
           
           }
                  
           // method not supported, create a new Token, and call the method
           // with new (converted) args. This may be a problem.
            return new MethodCallNode(
             new ObjectFieldAccessNode(fieldAccessNode.getName(),
              new AllocateNode(PtolemyDecls.typeNodeForKind(accessedObjKind),
               TNLManip.cons(accessedObj), AbsentTreeNode.instance)),
              methodArgs);
          
        } else if (accessedObjKind == PtolemyDecls.TYPE_KIND_PARAMETER) {
            switch (accessedObj.classID()) {
              case THISFIELDACCESSNODE_ID:
              case SUPERFIELDACCESSNODE_ID: // CHECKME : is this right?
              {
                if (methodName.equals("getToken")) {
                   return accessedObj;
                }
              }
            }                                         
        } else if (PtolemyDecls.isSupportedPortKind(accessedObjKind)) {
            switch (accessedObj.classID()) {
              case THISFIELDACCESSNODE_ID:
              case SUPERFIELDACCESSNODE_ID: // CHECKME : is this right?
              {
                TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
                String varName = typedDecl.getName();
                
                TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(varName);                 
                
                if (methodName.equals("getWidth")) {                                                   
                   return new IntLitNode(String.valueOf(port.getWidth()));
                } else if (methodName.equals("hasRoom")) {                                                   
                   if ((port.getWidth() > 0) && port.isOutput()) {
                      return new BoolLitNode("true");
                   } else {
                      return new BoolLitNode("false");
                   }
                } else if (methodName.equals("hasToken")) {                                                   
                   if ((port.getWidth() > 0) && port.isInput()) {
                      return new BoolLitNode("true");
                   } else {
                      return new BoolLitNode("false");
                   }
                } else if (methodName.equals("isInput")) {
                   if (port.isInput()) {
                      return new BoolLitNode("true");
                   } else {
                      return new BoolLitNode("false");
                   }
                } else if (methodName.equals("isMultiport")) {
                   if (port.isMultiport()) {
                      return new BoolLitNode("true");
                   } else {
                      return new BoolLitNode("false");
                   }
                } else if (methodName.equals("isOutput")) {
                   if (port.isOutput()) {
                      return new BoolLitNode("true");
                   } else {
                      return new BoolLitNode("false");
                   }
                }  
              }                                                   
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
          if (constructorTypes.length == 0) {
            return new BoolLitNode("false");
          } 
                    
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], BoolTypeNode.instance)) {
             // new BooleanToken(boolean)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new BooleanToken(???, ???, ...)             
          // call the booleanValue() method of the created BooleanToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "booleanValue"), node), 
            new LinkedList());             
           
          case PtolemyDecls.TYPE_KIND_INT_TOKEN:          
          if (constructorTypes.length == 0) {
            return new IntLitNode("0");
          }           
          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], IntTypeNode.instance)) {
             // new IntToken(int)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new IntToken(???, ???, ...)             
          // call the intValue() method of the created IntToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "intValue"), node), 
            new LinkedList());
            
          case PtolemyDecls.TYPE_KIND_DOUBLE_TOKEN:  
          if (constructorTypes.length == 0) {
            return new DoubleLitNode("0.0");
          } 
          
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
          if (constructorTypes.length == 0) {
            return new LongLitNode("0L");
          } 
                    
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], LongTypeNode.instance)) {
             // new LongToken(long)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new LongToken(???, ???, ...)               
          // call the longValue() method of the created LongToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "longValue"), node), 
            new LinkedList());             
          
          case PtolemyDecls.TYPE_KIND_COMPLEX_TOKEN:                           
          if (constructorTypes.length == 0) {
            return new AllocateNode(PtolemyDecls.COMPLEX_TYPE, new LinkedList(),
             AbsentTreeNode.instance);
          } 
                    
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], PtolemyDecls.COMPLEX_TYPE)) {
             // new ComplexToken(Complex)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new ComplexToken(???, ???, ...)             
          // call the complexValue() method of the created ComplexToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "complexValue"), node), 
           new LinkedList());             

          case PtolemyDecls.TYPE_KIND_FIX_TOKEN:                           
          if (constructorTypes.length == 0) {
            return new AllocateNode(PtolemyDecls.FIX_POINT_TYPE, new LinkedList(),
             AbsentTreeNode.instance);
          } 
                 
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
          } 
          
          // new ObjectToken(???, ???, ...)             
          // call the getValue() method of the created ObjectToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "getValue"), node), 
            new LinkedList());             
                    
          case PtolemyDecls.TYPE_KIND_STRING_TOKEN:          
          if (constructorTypes.length == 0) {
             return new StringLitNode("");
          } 
                    
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], StaticResolution.STRING_TYPE)) {
             // new StringToken(String)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new StringToken(???, ???, ...)
             
          // call the stringValue() method of the created StringToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "stringValue"), node), 
           new LinkedList());             
          
          case PtolemyDecls.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:          
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(BoolTypeNode.instance, 2))) {
             // new BooleanMatrixToken(boolean[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new BooleanMatrixToken(???, ???, ...)
          // call the booleanMatrix() method of the created BooleanMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "booleanMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyDecls.TYPE_KIND_INT_MATRIX_TOKEN:  
          // no support for constructor with no arguments
                                      
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(IntTypeNode.instance, 2))) {
             // new IntMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new IntMatrixToken(???, ???, ...)             
          // call the intMatrix() method of the created IntMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "intMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyDecls.TYPE_KIND_DOUBLE_MATRIX_TOKEN:          
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(DoubleTypeNode.instance, 2))) {
             // new DoubleMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
             
          // new DoubleMatrixToken(???, ???, ...)             
          // call the doubleMatrix() method of the created DoubleMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "doubleMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyDecls.TYPE_KIND_LONG_MATRIX_TOKEN:    
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(LongTypeNode.instance, 2))) {
             // new LongMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
             
             // new LongMatrixToken(???, ???, ...)             
             // call the longMatrix() method of the created LongMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "longMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyDecls.TYPE_KIND_COMPLEX_MATRIX_TOKEN:         
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(PtolemyDecls.COMPLEX_TYPE, 2))) {
             // new ComplexMatrixToken(Complex[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          
          // new ComplexMatrixToken(???, ???, ...)             
          // call the complexMatrix() method of the created ComplexMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "complexMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyDecls.TYPE_KIND_FIX_MATRIX_TOKEN:         
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              TypeUtility.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(PtolemyDecls.FIX_POINT_TYPE, 2))) {
             // new FixMatrixToken(FixPoint[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, null);
          } 
          // new FixMatrixToken(???, ???, ...)
             
          // call the fixMatrix() method of the created FixMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "fixMatrix"), node), 
            new LinkedList());                                        
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
        // discard casts to abstract tokens
        
        TypeNode type = node.getDtype();
        
        int kind = PtolemyDecls.kind(type);
        if (PtolemyDecls.isSupportedTokenKind(kind) && 
            !PtolemyDecls.isConcreteTokenKind(kind)) {
           return node.getExpr();        
        }
        
        node.setDtype((TypeNode) node.getDtype().accept(this, null));
        node.setExpr((ExprNode) node.getExpr().accept(this, null));
        return node;        
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
        
        int kind = PtolemyDecls.kind(type);
        
        if (PtolemyDecls.isSupportedTokenKind(kind)) {                            
           if (initExpr.classID() == NULLPNTRNODE_ID) {
              node.setInitExpr(_dummyValue(type));
           }
        } else if (kind == PtolemyDecls.TYPE_KIND_PARAMETER) {
           String paramName = node.getName().getIdent();
           
           Token token = (Token) _actorInfo.parameterNameToTokenMap.get(paramName);
          
           if (token != null) {
              node.setDefType((TypeNode) 
               PtolemyDecls.typeNodeForTokenType(token.getType()).accept(this, null));
                                    
              node.setInitExpr(PtolemyDecls.tokenToExprNode(token));  
              
              return node;                              
           }
        }                
        
        node.setDefType((TypeNode) type.accept(this, null));
    
        return node;
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
    
    protected PerActorCodeGeneratorInfo _actorInfo = null;
}