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

package ptolemy.codegen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.*;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

/** A Java AST visitor that transforms Actor code into code suitable
 *  for standalone execution (without dependancies on the ptolemy.actor
 *  and ptolemy.data packages)
 *
 *  @author Jeff Tsay
 */
public class ActorTransformerVisitor extends ReplacementJavaVisitor 
     implements JavaStaticSemanticConstants {
    
    public ActorTransformerVisitor(ActorCodeGeneratorInfo actorInfo) {
        super(TM_CUSTOM);
        
       _actorInfo = actorInfo;
       _typeVisitor = new PtolemyTypeVisitor(actorInfo);

       _actorName = actorInfo.actor.getName();
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        switch (_typeVisitor.kindOfTypeNameNode(node)) {

          // replace remaining Token types with int
          case PtolemyTypeVisitor.TYPE_KIND_TOKEN:
          return IntTypeNode.instance;   
          
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_TOKEN:
          return BoolTypeNode.instance;
                    
          case PtolemyTypeVisitor.TYPE_KIND_INT_TOKEN:
          return IntTypeNode.instance;
                    
          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_TOKEN:
          return DoubleTypeNode.instance;
          
          case PtolemyTypeVisitor.TYPE_KIND_LONG_TOKEN:
          return LongTypeNode.instance;
          
          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_TOKEN:
          return PtolemyTypeVisitor.COMPLEX_TYPE.clone();
          
          case PtolemyTypeVisitor.TYPE_KIND_FIX_TOKEN:
          return PtolemyTypeVisitor.FIX_POINT_TYPE.clone();          
                    
          case PtolemyTypeVisitor.TYPE_KIND_OBJECT_TOKEN: 
          return StaticResolution.OBJECT_TYPE.clone();
          
          case PtolemyTypeVisitor.TYPE_KIND_STRING_TOKEN:
          return StaticResolution.STRING_TYPE.clone();
          
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(BoolTypeNode.instance, 2);
          
          case PtolemyTypeVisitor.TYPE_KIND_INT_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(IntTypeNode.instance, 2);
                    
          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(DoubleTypeNode.instance, 2);          
          
          case PtolemyTypeVisitor.TYPE_KIND_LONG_MATRIX_TOKEN:
          return TypeUtility.makeArrayType(LongTypeNode.instance, 2);          
    
          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_MATRIX_TOKEN:      
          return TypeUtility.makeArrayType(
           (TypeNode) PtolemyTypeVisitor.COMPLEX_TYPE.clone(), 2);    
          
          case PtolemyTypeVisitor.TYPE_KIND_FIX_MATRIX_TOKEN:      
          return TypeUtility.makeArrayType(
           (TypeNode) PtolemyTypeVisitor.FIX_POINT_TYPE.clone(), 2);                                              
          
          default:
          return node;
        }
    }
                              
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {            
        return _defaultVisit(node, args);
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        // check if this is the actor, transform only the actor
        String className = node.getName().getIdent();
               
        // we assume class names beginning with "CG_" and ending with the
        // Ptolemy name of the actor are actor classes created in the first pass 
        // to be modified with the code generator        
        if (className.startsWith("CG_") && className.endsWith("_" + _actorName)) {
           
           // if the class derives from TypedAtomicActor or SDFAtomicActor,
           // replace the superclass with Object
                      
           TypeNameNode superTypeNode = (TypeNameNode) node.getSuperClass();
           
           ClassDecl superClassDecl = 
            (ClassDecl) JavaDecl.getDecl((NamedNode) superTypeNode);
           
           if ((superClassDecl == _TYPED_ATOMIC_ACTOR_DECL) ||
               (superClassDecl == _SDF_ATOMIC_ACTOR_DECL)) {
              node.setSuperClass((TypeNameNode) StaticResolution.OBJECT_TYPE.clone());                 
              _isBaseClass = true;
           } 
                                 
           List memberList = node.getMembers();
                               
           memberList = TNLManip.traverseList(this, node, args, memberList);                             
           node.setMembers(memberList);
                                            
           Iterator memberItr = memberList.iterator();
        
           // remove unwanted fields and methods
           LinkedList newMemberList = new LinkedList();
        
           while (memberItr.hasNext()) {
              Object memberObj = memberItr.next();
           
              if (memberObj != NullValue.instance) {
                 newMemberList.add(memberObj);           
              }        
           }

           if (_isBaseClass) {
              _addDefaultExecutionMethods(newMemberList);                                
           } 
        
           node.setMembers(newMemberList);                      
        }
        
        return node;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        TreeNode initExpr = (TreeNode) node.getInitExpr().accept(this, args);
        node.setInitExpr(initExpr);
        
        TypeNode type = node.getDefType();
        
        int kind = _typeVisitor.kind(type);
        
        if (_typeVisitor.isSupportedTokenKind(kind)) {                            
           if (initExpr.classID() == NULLPNTRNODE_ID) {
              node.setInitExpr(_dummyValue(type));
           }
        } else if (kind == PtolemyTypeVisitor.TYPE_KIND_PARAMETER) {
           String paramName = node.getName().getIdent();
           
           Token token = (Token) _actorInfo.parameterNameToTokenMap.get(paramName);
          
           if (token != null) {
              node.setDefType((TypeNode) 
               _typeVisitor.typeNodeForTokenType(token.getType()).accept(this, args));
                                    
              node.setInitExpr(tokenToExprNode(token));  
              
              return node;                              
           }
        } else if (_typeVisitor.isSupportedPortKind(kind)) {
           Object retval = _portFieldDeclNode(node);
           
           if (retval != null) return retval;        
        }               
        
        node.setDefType((TypeNode) type.accept(this, args));
    
        return node;
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        TreeNode initExpr = (TreeNode) node.getInitExpr().accept(this, args);
        node.setInitExpr(initExpr);
        
        TypeNode type = node.getDefType();
        
        int kind = _typeVisitor.kind(type);
        
        if (_typeVisitor.isSupportedTokenKind(kind)) {                            
           if (initExpr.classID() == NULLPNTRNODE_ID) {
              node.setInitExpr(_dummyValue(type));
           }
        } else if (kind == PtolemyTypeVisitor.TYPE_KIND_PARAMETER) {
           return node; // leave everything alone              
        }                
        
        node.setDefType((TypeNode) type.accept(this, args));
    
        return node;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        String methodName = node.getName().getIdent();
                        
        if (methodName.equals("clone") || methodName.equals("attributeChanged") ||
            methodName.equals("attributeTypeChanged")) {
           // get rid of this method
           return NullValue.instance;        
        }         
        return _defaultVisit(node, args);   
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        // allow only one constructor
        
        if (_constructorCount++ > 1) {
           ApplicationUtility.error("more than one constructor found in actor " +
            _actorInfo.actor.getName());
        }
        
        // get rid of all parameters
        node.setParams(new LinkedList());
                
        node.setBody((BlockNode) node.getBody().accept(this, args));
        node.setConstructorCall((ConstructorCallNode) 
         node.getConstructorCall().accept(this, args));
         
        return node;
    }

    public Object visitThisConstructorCallNode(ThisConstructorCallNode node, LinkedList args) {
        ApplicationUtility.error("this() constructor call found in actor");
        return null;
    }

    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node, LinkedList args) {
        // set the constructor call to super() with no arguments
        node.setArgs(new LinkedList());
        return node;
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

    public Object visitExprStmtNode(ExprStmtNode node, LinkedList args) {
        // eliminate unwanted method calls and assigments
        // also eliminate expressions that are not statement expressions
        
        Object exprObj = node.getExpr().accept(this, args);
        
        if ((exprObj == NullValue.instance)) { 
           return new EmptyStmtNode();
        }
        
        ExprNode exprNode = (ExprNode) exprObj;
        
        if (!ExprUtility.isStatementExpression(exprNode)) {
           return new EmptyStmtNode();
        }
        
        node.setExpr((ExprNode) exprObj);
        
        return node;
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
        // transform the arguments
        List oldMethodArgs = node.getArgs();
        TypeNode[] oldMethodArgTypes = _typeVisitor.typeArray(oldMethodArgs);
        
        List methodArgs = 
         TNLManip.traverseList(this, null, args, oldMethodArgs);                
         
        node.setArgs(methodArgs);
                               
        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();
        
        // if this is a static method call, we can't do any more.
        if (fieldAccessNode instanceof TypeFieldAccessNode) {
           node.setMethod((FieldAccessNode) fieldAccessNode.accept(this, args));
           return node;
        }
                        
        ExprNode accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);   

        // save the kind of the old accessed object    
        int accessedObjKind = _typeVisitor.kind(_typeVisitor.type(accessedObj));        
        
        fieldAccessNode = (FieldAccessNode) fieldAccessNode.accept(this, args);
         
        node.setMethod(fieldAccessNode); 
        
        accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);
                            
        MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);        
        String methodName = methodDecl.getName();        
        
        // eliminate the following method calls no matter what
        if (methodName.equals("attributeTypeChanged") || 
            methodName.equals("attributeChanged")) {
           return NullValue.instance;        
        }
        
        // eliminate the following method calls if this is the most basic actor class
        // and an invocation of super.XXX() occurs
        if (_isBaseClass && (fieldAccessNode.classID() == SUPERFIELDACCESSNODE_ID)) {         
           if (methodName.equals("initialize") || methodName.equals("fire") ||
               methodName.equals("preinitialize") || methodName.equals("wrapup")) {
              return NullValue.instance;                                             
           }
           
           // return true for prefire() and postfire() which is the default
           if (methodName.equals("prefire") || methodName.equals("postfire")) {
              return new BoolLitNode("true");
           }           
        }
                                                
        ExprNode firstArg = null;
        TypeNode firstArgType = null;
        boolean firstArgTypeIsScalarToken = false;
        boolean firstArgTypeIsStringToken = false;        
        
        if (oldMethodArgs.size() > 0) {
           firstArg = (ExprNode) methodArgs.get(0);
           firstArgType = oldMethodArgTypes[0];
           firstArgTypeIsScalarToken = _typeVisitor.isScalarTokenKind(
            _typeVisitor.kind(firstArgType));
           firstArgTypeIsStringToken = _typeVisitor.compareTypes(firstArgType, 
            PtolemyTypeVisitor.STRING_TOKEN_TYPE);           
        }
                                                                           
        if (_typeVisitor.isSupportedTokenKind(accessedObjKind)) {
            boolean accessedObjIsMatrix = _typeVisitor.isMatrixTokenKind(accessedObjKind);     
            boolean accessedObjIsScalar = _typeVisitor.isScalarTokenKind(accessedObjKind);     
            boolean accessedObjIsBoolean = 
             (accessedObjKind == _typeVisitor.TYPE_KIND_BOOLEAN_TOKEN);
        
           if (methodName.equals("booleanValue")) {
              return new CastNode(BoolTypeNode.instance, accessedObj);
           } else if (methodName.equals("intValue")) {
              return new CastNode(IntTypeNode.instance, accessedObj);
           } else if (methodName.equals("longValue")) {
              return new CastNode(LongTypeNode.instance, accessedObj);
           } else if (methodName.equals("doubleValue")) {
              return new CastNode(DoubleTypeNode.instance, accessedObj);
           } else if (methodName.equals("complexValue")) {
              return new CastNode((TypeNode) PtolemyTypeVisitor.COMPLEX_TYPE.clone(), 
               accessedObj);
           } else if (methodName.equals("fixValue")) {
              return new CastNode((TypeNode) PtolemyTypeVisitor.FIX_POINT_TYPE.clone(), 
               accessedObj);
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
              return new CastNode(TypeUtility.makeArrayType(PtolemyTypeVisitor.COMPLEX_TYPE, 2), 
               accessedObj);
           } else if (methodName.equals("fixMatrix")) {
              return new CastNode(TypeUtility.makeArrayType(PtolemyTypeVisitor.FIX_POINT_TYPE, 2), 
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
              return new CastNode(_typeVisitor.typeNodeForKind(accessedObjKind), firstArg);
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
           } else if (methodName.equals("isEqualTo")) {
              ExprNode retval = new EQNode(accessedObj, firstArg);              
              // mark this node as being created by the actor transformer
              retval.setProperty(PtolemyTypeVisitor.PTOLEMY_TRANSFORMED_KEY, 
               NullValue.instance);
              return retval;              
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
              new AllocateNode(_typeVisitor.typeNodeForKind(accessedObjKind),
               TNLManip.cons(accessedObj), AbsentTreeNode.instance)),
              methodArgs);
          
        } else if (accessedObjKind == PtolemyTypeVisitor.TYPE_KIND_PARAMETER) {
            if (accessedObj.classID() == THISFIELDACCESSNODE_ID) {
                if (methodName.equals("getToken")) {
                   return accessedObj;
                } else if (methodName.equals("removeFromScope") || 
                           methodName.equals("removeValueListener") || 
                           methodName.equals("reset") || 
                           methodName.equals("setContainer") || 
                           methodName.equals("setExpression") ||
                           methodName.equals("setToken") || 
                           methodName.equals("setTypeAtLeast") || 
                           methodName.equals("setTypeAtMost") ||               
                           methodName.equals("setTypeEquals") ||
                           methodName.equals("setTypeSameAs")) {                           
                   return NullValue.instance; // must be eliminated by ExprStmtNode                                           
                }
            }
        } else if (_typeVisitor.isSupportedPortKind(accessedObjKind)) {
            Object retval = _portMethodCall(node, methodDecl, accessedObj);            
            if (retval != null) return retval;
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
         
        TypeNode[] constructorTypes = _typeVisitor.typeArray(constructorArgs);
                                                                           
        switch (_typeVisitor.kind(type)) {
        
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_TOKEN:               
          if (constructorTypes.length == 0) {
            return new BoolLitNode("false");
          } 
                    
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], BoolTypeNode.instance)) {
             // new BooleanToken(boolean)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new BooleanToken(???, ???, ...)             
          // call the booleanValue() method of the created BooleanToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "booleanValue"), node), 
            new LinkedList());             
           
          case PtolemyTypeVisitor.TYPE_KIND_INT_TOKEN:          
          if (constructorTypes.length == 0) {
             return new IntLitNode("0");
          }           
          
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], IntTypeNode.instance)) {
             // new IntToken(int)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new IntToken(???, ???, ...)             
          // call the intValue() method of the created IntToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "intValue"), node), 
            new LinkedList());
            
          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_TOKEN:  
          if (constructorTypes.length == 0) {
            return new DoubleLitNode("0.0");
          } 
          
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], DoubleTypeNode.instance)) {
             // new DoubleToken(double)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } else {
             // new DoubleToken(???, ???, ...)
             
             // call the doubleValue() method of the created DoubleToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "doubleValue"), node), 
              new LinkedList());             
          }  
          
          case PtolemyTypeVisitor.TYPE_KIND_LONG_TOKEN:          
          if (constructorTypes.length == 0) {
            return new LongLitNode("0L");
          } 
                    
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], LongTypeNode.instance)) {
             // new LongToken(long)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new LongToken(???, ???, ...)               
          // call the longValue() method of the created LongToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "longValue"), node), 
            new LinkedList());             
          
          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_TOKEN:                           
          if (constructorTypes.length == 0) {
            return new AllocateNode(PtolemyTypeVisitor.COMPLEX_TYPE, new LinkedList(),
             AbsentTreeNode.instance);
          } 
                    
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               PtolemyTypeVisitor.COMPLEX_TYPE)) {
             // new ComplexToken(Complex)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new ComplexToken(???, ???, ...)             
          // call the complexValue() method of the created ComplexToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "complexValue"), node), 
           new LinkedList());             

          case PtolemyTypeVisitor.TYPE_KIND_FIX_TOKEN:                           
          if (constructorTypes.length == 0) {
            return new AllocateNode(PtolemyTypeVisitor.FIX_POINT_TYPE, new LinkedList(),
             AbsentTreeNode.instance);
          } 
                 
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               PtolemyTypeVisitor.FIX_POINT_TYPE)) {
             // new FixToken(Fix)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } else {
             // new FixToken(???, ???, ...)
             
             // call the fixValue() method of the created FixToken
             return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "fixValue"), node), 
              new LinkedList());             
          }  

                           
          case PtolemyTypeVisitor.TYPE_KIND_OBJECT_TOKEN:           
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               StaticResolution.OBJECT_TYPE)) {
             // new ObjectToken(Object)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new ObjectToken(???, ???, ...)             
          // call the getValue() method of the created ObjectToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "getValue"), node), 
            new LinkedList());             
                    
          case PtolemyTypeVisitor.TYPE_KIND_STRING_TOKEN:          
          if (constructorTypes.length == 0) {
             return new StringLitNode("");
          } 
                    
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               StaticResolution.STRING_TYPE)) {
             // new StringToken(String)          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new StringToken(???, ???, ...)
             
          // call the stringValue() method of the created StringToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "stringValue"), node), 
           new LinkedList());             
          
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:          
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(BoolTypeNode.instance, 2))) {
             // new BooleanMatrixToken(boolean[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new BooleanMatrixToken(???, ???, ...)
          // call the booleanMatrix() method of the created BooleanMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "booleanMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyTypeVisitor.TYPE_KIND_INT_MATRIX_TOKEN:  
          // no support for constructor with no arguments
                                      
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(IntTypeNode.instance, 2))) {
             // new IntMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new IntMatrixToken(???, ???, ...)             
          // call the intMatrix() method of the created IntMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "intMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_MATRIX_TOKEN:          
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(DoubleTypeNode.instance, 2))) {
             // new DoubleMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
             
          // new DoubleMatrixToken(???, ???, ...)             
          // call the doubleMatrix() method of the created DoubleMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "doubleMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyTypeVisitor.TYPE_KIND_LONG_MATRIX_TOKEN:    
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(LongTypeNode.instance, 2))) {
             // new LongMatrixToken(int[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
             
             // new LongMatrixToken(???, ???, ...)             
             // call the longMatrix() method of the created LongMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "longMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_MATRIX_TOKEN:         
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(
                (TypeNode) PtolemyTypeVisitor.COMPLEX_TYPE.clone(), 2))) {
             // new ComplexMatrixToken(Complex[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          } 
          
          // new ComplexMatrixToken(???, ???, ...)             
          // call the complexMatrix() method of the created ComplexMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "complexMatrix"), node), 
            new LinkedList());                                        
          
          case PtolemyTypeVisitor.TYPE_KIND_FIX_MATRIX_TOKEN:         
          // no support for constructor with no arguments
          
          if ((constructorTypes.length == 1) && 
              _typeVisitor.compareTypes(constructorTypes[0], 
               TypeUtility.makeArrayType(
                (TypeNode) PtolemyTypeVisitor.FIX_POINT_TYPE.clone(), 2))) {
             // new FixMatrixToken(FixPoint[][])          
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
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
        
        ExprNode castTarget = (ExprNode) node.getExpr().accept(this, args);
        
        TypeNode type = node.getDtype();
        
        int kind = _typeVisitor.kind(type);
        if (_typeVisitor.isSupportedTokenKind(kind) && 
            !_typeVisitor.isConcreteTokenKind(kind)) {
           return castTarget;        
        }
        
        node.setDtype((TypeNode) node.getDtype().accept(this, args));
        node.setExpr(castTarget);
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

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        ExprNode leftExpr = node.getExpr1();
        TypeNode leftType = _typeVisitor.type(leftExpr);        
        int kind = _typeVisitor.kind(leftType);

        // prevent assignment to ports and parameters
        if (_typeVisitor.isSupportedPortKind(kind) || 
            (kind == PtolemyTypeVisitor.TYPE_KIND_PARAMETER)) {
           return NullValue.instance; // must be eliminated by ExprStmtNode         
        }                        
                
        node.setExpr1((ExprNode) leftExpr.accept(this, args));
        node.setExpr2((ExprNode) node.getExpr2().accept(this, args));
        
        return node;
    }

    /** Return a new ExprNode representing the value of the argument Token. */    
    public ExprNode tokenToExprNode(Token token) {
        switch (_typeVisitor.kindOfTokenType(token.getType())) {
        
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_TOKEN:
          if (((BooleanToken) token).booleanValue()) {
             return new BoolLitNode("true");
          } 
          return new BoolLitNode("false");
          
          case PtolemyTypeVisitor.TYPE_KIND_INT_TOKEN:
          {
            int val = ((IntToken) token).intValue();
            return new IntLitNode(String.valueOf(val));
          }
       
          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_TOKEN:
          {
            double val = ((DoubleToken) token).doubleValue();
            return new DoubleLitNode(String.valueOf(val));
          }

          case PtolemyTypeVisitor.TYPE_KIND_LONG_TOKEN:
          {
            long val = ((LongToken) token).longValue();
            return new LongLitNode(String.valueOf(val) + "L");
          }
           
          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_TOKEN:
          {
            Complex val = ((ComplexToken) token).complexValue();
            LinkedList args = new LinkedList();
            args.addLast(new DoubleLitNode(String.valueOf(val.real)));
            args.addLast(new DoubleLitNode(String.valueOf(val.imag)));
                        
            return new AllocateNode(PtolemyTypeVisitor.COMPLEX_TYPE, 
             args, AbsentTreeNode.instance);              
          }
          
          case PtolemyTypeVisitor.TYPE_KIND_FIX_TOKEN:
          {
            // FIXME : not done
            FixPoint val = ((FixToken) token).fixValue();
            return null;
          }
          
          case PtolemyTypeVisitor.TYPE_KIND_OBJECT_TOKEN:
          ApplicationUtility.error("tokenToExprNode not supported on ObjectToken");            
          
          case PtolemyTypeVisitor.TYPE_KIND_STRING_TOKEN:
          {
            return new StringLitNode(token.toString());
          }
          
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          case PtolemyTypeVisitor.TYPE_KIND_INT_MATRIX_TOKEN:
          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          case PtolemyTypeVisitor.TYPE_KIND_LONG_MATRIX_TOKEN:
          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_MATRIX_TOKEN:
          // won't work correctly, but same principle          
          case PtolemyTypeVisitor.TYPE_KIND_FIX_MATRIX_TOKEN: 
          {
            MatrixToken matrixToken = (MatrixToken) token;
            
            LinkedList rowList = new LinkedList();
            for (int i = 0; i < matrixToken.getRowCount(); i++) {
                
                LinkedList columnList = new LinkedList();
                for (int j = 0; j < matrixToken.getColumnCount(); j++) {
                    columnList.addLast(tokenToExprNode(matrixToken.getElementAsToken(i, j)));                    
                }
                
                rowList.addLast(new ArrayInitNode(columnList));
            }
            return new ArrayInitNode(rowList);
          }
                                                
          default:                
          ApplicationUtility.error("tokenToExprNode() not supported on token class " + 
           token.getClass().getName());
        }    
        return null;
    }
    
    /** Return an expression to substitute for null, when null is assigned to
     *  a Token of the given type.
     */
    protected ExprNode _dummyValue(TypeNode type) {
        switch (_typeVisitor.kind(type)) {
    
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_TOKEN:       
          return new BoolLitNode("false");
    
          case PtolemyTypeVisitor.TYPE_KIND_INT_TOKEN:
          return new IntLitNode("-1");

          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_TOKEN:
          return new DoubleLitNode("-1.0");           

          case PtolemyTypeVisitor.TYPE_KIND_LONG_TOKEN:
          return new LongLitNode("-1L");

          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_TOKEN:                   
          case PtolemyTypeVisitor.TYPE_KIND_FIX_TOKEN:                             
          case PtolemyTypeVisitor.TYPE_KIND_OBJECT_TOKEN:           
          case PtolemyTypeVisitor.TYPE_KIND_STRING_TOKEN:          
          case PtolemyTypeVisitor.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:          
          case PtolemyTypeVisitor.TYPE_KIND_INT_MATRIX_TOKEN:                    
          case PtolemyTypeVisitor.TYPE_KIND_DOUBLE_MATRIX_TOKEN:          
          case PtolemyTypeVisitor.TYPE_KIND_LONG_MATRIX_TOKEN:    
          case PtolemyTypeVisitor.TYPE_KIND_COMPLEX_MATRIX_TOKEN:                 
          case PtolemyTypeVisitor.TYPE_KIND_FIX_MATRIX_TOKEN:                           
          return new NullPntrNode();           
          
          // remove this later
          case PtolemyTypeVisitor.TYPE_KIND_TOKEN:
          case PtolemyTypeVisitor.TYPE_KIND_SCALAR_TOKEN:                                         
          case PtolemyTypeVisitor.TYPE_KIND_MATRIX_TOKEN:                                         
          return new IntLitNode("-1");                    
        }

        ApplicationUtility.error("unexpected type for dummy() : " + type);        
        return null;        
    }       
    
    
    /** Given a list of members of the most basic actor class, add 
     *  preinitialize(), initialize(), prefire(), fire(), postfire(), and
     *  wrapup() method declarations, which do the default behavior, if these
     *  methods do not already appear in the member list.
     */
    protected void _addDefaultExecutionMethods(List memberList) {
        boolean foundPreinit = false;
        boolean foundInit = false;
        boolean foundPrefire = false;
        boolean foundFire = false;
        boolean foundPostfire = false;
        boolean foundWrapup = false;
        
        Iterator memberItr = memberList.iterator();
        
        while (memberItr.hasNext()) {
           TreeNode member = (TreeNode) memberItr.next();
           
           if (member.classID() == METHODDECLNODE_ID) {
              String methodName = ((MethodDeclNode) member).getName().getIdent();
              
              if (methodName.equals("preinitialize")) {
                 foundPreinit = true;
              } else if (methodName.equals("initialize")) {
                 foundInit = true;
              } else if (methodName.equals("prefire")) {
                 foundPrefire = true;
              } else if (methodName.equals("fire")) {
                 foundFire = true;
              } else if (methodName.equals("postfire")) {
                 foundPostfire = true;
              } else if (methodName.equals("wrapup")) {
                 foundWrapup = true;
              }
           }                      
        }
        
        if (!foundPreinit) {
           memberList.add(new MethodDeclNode(PUBLIC_MOD, 
            new NameNode(AbsentTreeNode.instance, "preinitialize"),
            new LinkedList(), new LinkedList(),
            new BlockNode(new LinkedList()), VoidTypeNode.instance));
        }

        if (!foundInit) {
           memberList.add(new MethodDeclNode(PUBLIC_MOD, 
            new NameNode(AbsentTreeNode.instance, "initialize"),
            new LinkedList(), new LinkedList(),
            new BlockNode(new LinkedList()), VoidTypeNode.instance));
        }
        
        if (!foundPrefire) {
           List blockList = TNLManip.cons(
            new ReturnNode(new BoolLitNode("true")));        
           memberList.add(new MethodDeclNode(PUBLIC_MOD, 
            new NameNode(AbsentTreeNode.instance, "prefire"),
            new LinkedList(), new LinkedList(),
            new BlockNode(blockList), BoolTypeNode.instance));
        }

        if (!foundFire) {
           memberList.add(new MethodDeclNode(PUBLIC_MOD, 
            new NameNode(AbsentTreeNode.instance, "fire"),
            new LinkedList(), new LinkedList(),
            new BlockNode(new LinkedList()), VoidTypeNode.instance));
        }
        
        if (!foundPostfire) {
           List blockList = TNLManip.cons(
            new ReturnNode(new BoolLitNode("true")));        
           memberList.add(new MethodDeclNode(PUBLIC_MOD, 
            new NameNode(AbsentTreeNode.instance, "postfire"),
            new LinkedList(), new LinkedList(),
            new BlockNode(blockList), BoolTypeNode.instance));
        }
        
        if (!foundWrapup) {
           memberList.add(new MethodDeclNode(PUBLIC_MOD, 
            new NameNode(AbsentTreeNode.instance, "wrapup"),
            new LinkedList(), new LinkedList(),
            new BlockNode(new LinkedList()), VoidTypeNode.instance));
        }                            
    }
       
    protected Object _portFieldDeclNode(FieldDeclNode node) {
        // by default, get rid of the port field declaration 
        return NullValue.instance;              
    }
       
    protected Object _portMethodCall(MethodCallNode node, MethodDecl methodDecl,
     ExprNode accessedObj) {
        // we can only handle ports that are fields of the actor
        if (accessedObj.classID() != THISFIELDACCESSNODE_ID) return null;
            
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
        
        String methodName = methodDecl.getName();        
        
        String varName = typedDecl.getName();
                
        TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(varName);        

        if (methodName.equals("get")) {
           // dummy value
           TypeNameNode portType = 
            _typeVisitor.typeNodeForTokenType(port.getType());
           
           return _dummyValue(portType);
           
        } else if (methodName.equals("getWidth")) {                                                   
           return new IntLitNode(String.valueOf(port.getWidth()));
        } else if (methodName.equals("hasRoom")) {                                                   
           // dummy value
           return new BoolLitNode("false");
        } else if (methodName.equals("hasToken")) {        
           // dummy value
           return new BoolLitNode("false");        
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
        } else if (methodName.equals("send")) {
           // dummy behavior, do nothing
           return NullValue.instance;
        } else if (methodName.equals("setInput") || 
                   methodName.equals("setMultiport") || 
                   methodName.equals("setOutput") || 
                   methodName.equals("setTokenConsumptionRate") || 
                   methodName.equals("setTokenInitRate") || 
                   methodName.equals("setTokenProductionRate") ||                           
                   methodName.equals("setTypeAtLeast") || 
                   methodName.equals("setTypeAtMost") ||               
                   methodName.equals("setTypeEquals") ||
                   methodName.equals("setTypeSameAs")) {
           return NullValue.instance; // must be eliminated by ExprStmtNode                                    
        }       
        return null;
    }
                 
    protected ActorCodeGeneratorInfo _actorInfo = null;    
    protected PtolemyTypeVisitor _typeVisitor = null;
    
    /** The Ptolemy name of the instance of the actor. */
    protected final String _actorName;
    
    /** A count of how many constructors in the "actor" class that have been
     *  found so far.
     */
    protected int _constructorCount = 0;
    
    /** A flag indicating that actor class inherits from object, so super.XXX()
     *  method calls must be eliminated.
     */      
    protected boolean _isBaseClass = false;
    
    public static final ClassDecl _TYPED_ATOMIC_ACTOR_DECL;
    public static final ClassDecl _SDF_ATOMIC_ACTOR_DECL;
                             
    static {    
        CompileUnitNode typedAtomicActorUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.actor.TypedAtomicActor", true), 1);
         
        _TYPED_ATOMIC_ACTOR_DECL = (ClassDecl) StaticResolution.findDecl(
         typedAtomicActorUnit, "TypedAtomicActor", CG_CLASS, null, null);        
 
        CompileUnitNode sdfAtomicActorUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.domains.sdf.kernel.SDFAtomicActor", true), 1);
         
        _SDF_ATOMIC_ACTOR_DECL = (ClassDecl) StaticResolution.findDecl(
         sdfAtomicActorUnit, "SDFAtomicActor", CG_CLASS, null, null);        
    }     
}
 