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

    public ActorTransformerVisitor(ActorCodeGeneratorInfo actorInfo,
                                   PtolemyTypeVisitor typeVisitor) {
        super(TM_CUSTOM);

       _actorInfo = actorInfo;
       _actorName = actorInfo.actor.getName();

       _typeVisitor = typeVisitor;
       _typePolicy = (PtolemyTypePolicy) typeVisitor.typePolicy();
       _typeID = (PtolemyTypeIdentifier) _typePolicy.typeIdentifier();
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        int kind = _typeID.kindOfTypeNameNode(node);

        if (!_typeID.isSupportedTokenKind(kind)) {
           return node;
        }

        // leave Token declarations alone for now
        if (kind == PtolemyTypeIdentifier.TYPE_KIND_TOKEN) {
           return node;
        }

        return _typeID.encapsulatedDataType(kind);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        // add import of ptolemy.math.* (remove if necessary later)
        List importList = node.getImports();

        importList.add(new ImportOnDemandNode((NameNode)
         StaticResolution.makeNameNode("ptolemy.math")));

        return _defaultVisit(node, args);
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        // check if this is the actor, transform only the actor
        String className = node.getName().getIdent();

        // we assume class names beginning with "CG_" and ending with the
        // Ptolemy name of the actor are actor classes created in the first pass
        // to be modified with the code generator
        if (className.startsWith("CG_") && className.endsWith("_" + _actorName)) {
           return _actorClassDeclNode(node, args);
        }

        return node;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        TreeNode initExpr = (TreeNode) node.getInitExpr().accept(this, args);
        node.setInitExpr(initExpr);

        TypeNode type = node.getDefType();

        int kind = _typeID.kind(type);

        if (_typeID.isSupportedTokenKind(kind)) {
           if (initExpr.classID() == NULLPNTRNODE_ID) {
              node.setInitExpr(_dummyValue(type));
           }
        } else if (kind == PtolemyTypeIdentifier.TYPE_KIND_PARAMETER) {
           String paramName = node.getName().getIdent();

           Token token = (Token) _actorInfo.parameterNameToTokenMap.get(paramName);

           if (token != null) {
              node.setModifiers(node.getModifiers() | FINAL_MOD);
              node.setDefType((TypeNode)
               _typeID.typeNodeForTokenType(token.getType()).accept(this, args));

              node.setInitExpr(tokenToExprNode(token));

              return node;
           } else {
              ApplicationUtility.error("found parameter field, but no associated token");
           }
        } else if (_typeID.isSupportedPortKind(kind)) {
           Object retval = _portFieldDeclNode(node, args);

           if (retval != null) return retval;
        }

        node.setDefType((TypeNode) type.accept(this, args));

        return node;
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        TreeNode initExpr = (TreeNode) node.getInitExpr().accept(this, args);
        node.setInitExpr(initExpr);

        TypeNode type = node.getDefType();

        int kind = _typeID.kind(type);

        if (_typeID.isSupportedTokenKind(kind)) {
           if (initExpr.classID() == NULLPNTRNODE_ID) {
              node.setInitExpr(_dummyValue(type));
           }
        } else if (kind == PtolemyTypeIdentifier.TYPE_KIND_PARAMETER) {
           return node; // leave everything alone
        }

        node.setDefType((TypeNode) type.accept(this, args));

        return node;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        String methodName = node.getName().getIdent();

        // get rid of the following methods
        if (methodName.equals("clone") ||
            methodName.equals("attributeChanged") ||
            methodName.equals("attributeTypeChanged")) {
           return NullValue.instance;
        }

        node.setParams(
         TNLManip.traverseList(this, node, args, node.getParams()));

        node.setBody((TreeNode) node.getBody().accept(this, args));

        // eliminate declared, throwable Ptolemy exceptions
        node.setThrowsList(_eliminatePtolemyExceptionList(
         TNLManip.traverseList(this, node, args, node.getThrowsList())));

        node.setReturnType((TypeNode) node.getReturnType().accept(this, args));

        return node;
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

        // eliminate declared, throwable Ptolemy exceptions
        node.setThrowsList(_eliminatePtolemyExceptionList(
         TNLManip.traverseList(this, node, args, node.getThrowsList())));

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

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        // do not transform interfaces
        return node;
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        node.setStmts(_makeStmtList(
         TNLManip.traverseList(this, node, args, node.getStmts())));

        return node;
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        node.setStmt(_makeStmt(node.getStmt().accept(this, args)));

        return node;
    }

    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        node.setCondition((ExprNode) node.getCondition().accept(this, args));

        node.setThenPart(_makeStmt(node.getThenPart().accept(this, args)));

        if (node.getElsePart() != AbsentTreeNode.instance) {
           node.setElsePart(
            (TreeNode) _makeStmt(node.getElsePart().accept(this, args)));
        }

        return node;
    }

    public Object visitSwitchBranchNode(SwitchBranchNode node, LinkedList args) {
        node.setStmts(_makeStmtList(
         TNLManip.traverseList(this, node, args, node.getStmts())));

        return node;
    }

    public Object visitLoopNode(LoopNode node, LinkedList args) {
        node.setTest((ExprNode) node.getTest().accept(this, args));

        if (node.getForeStmt() == AbsentTreeNode.instance) {
           node.setAftStmt((TreeNode)
            _makeStmt(node.getAftStmt().accept(this, args)));
        } else {
           node.setForeStmt((TreeNode)
            _makeStmt(node.getForeStmt().accept(this, args)));
        }
        return node;

    }

    public Object visitExprStmtNode(ExprStmtNode node, LinkedList args) {
        return _makeStmt(node.getExpr().accept(this, args));
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        node.setInit(
         TNLManip.traverseList(this, node, args, node.getInit()));

        node.setTest((ExprNode) node.getTest().accept(this, args));

        node.setUpdate(
         TNLManip.traverseList(this, node, args, node.getUpdate()));

        node.setStmt(_makeStmt(node.getStmt().accept(this, args)));

        return node;
    }

    public Object visitThrowNode(ThrowNode node, LinkedList args) {
        TypeNameNode exceptionType =
         (TypeNameNode) _typeVisitor.type(node.getExpr());

        int kind = _typeID.kindOfTypeNameNode(exceptionType);

        if (_typeID.isPtolemyExceptionKind(kind)) {
           String message = "ptolemy non-runtime exception";
           if (_typeID.isPtolemyRuntimeExceptionKind(kind)) {
              message = "ptolemy runtime exception";
           }

           node.setExpr(new AllocateNode(new TypeNameNode(
            new NameNode(AbsentTreeNode.instance, "RuntimeException")),
            TNLManip.cons(new StringLitNode(message)),
            AbsentTreeNode.instance));

           return node;
        }

        node.setExpr((ExprNode) node.getExpr().accept(this, args));

        return node;
    }

    public Object visitSynchronizedNode(SynchronizedNode node, LinkedList args) {
        node.setStmt((TreeNode) _makeStmt(node.getStmt().accept(this, args)));

        return node;
    }

    public Object visitCatchNode(CatchNode node, LinkedList args) {
        TypeNameNode exceptionType = (TypeNameNode) node.getParam().getDefType();

        int kind = _typeID.kindOfTypeNameNode(exceptionType);

        // don't catch any Ptolemy exceptions
        if (_typeID.isPtolemyExceptionKind(kind)) {
           return NullValue.instance;
        }

        node.setParam((ParameterNode) node.getParam().accept(this, args));
        node.setBlock((BlockNode) node.getBlock().accept(this, args));

        return node;
    }

    public Object visitTryNode(TryNode node, LinkedList args) {

        Iterator catchNodeItr =
         TNLManip.traverseList(this, node, args, node.getCatches()).iterator();

        LinkedList newCatchNodeList = new LinkedList();

        while (catchNodeItr.hasNext()) {
           Object catchNodeObj = catchNodeItr.next();

           // don't add catch clauses that catch Ptolemy exceptions
           if (catchNodeObj != NullValue.instance) {
              newCatchNodeList.addLast(catchNodeObj);
           }
        }

        node.setBlock((BlockNode) node.getBlock().accept(this, args));

        // if there's nothing to catch, return the block
        if (newCatchNodeList.size() < 1) {
           return node.getBlock();
        }

        node.setCatches(newCatchNodeList);
        node.setFinly((TreeNode) node.getFinly().accept(this, args));

        return node;
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {

        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();

        // if this is a static method call, we can't do any more.
        if (fieldAccessNode instanceof TypeFieldAccessNode) {
           return _defaultVisit(node, args);
        }

        ExprNode accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);

        // save the kind of the old accessed object
        TypeNode accessedObjType = _typeVisitor.type(accessedObj);

        int accessedObjKind = _typeID.kind(accessedObjType);

        if (_typePolicy.isSubClassOfSupportedActor(accessedObjType)) {
           Object retval = _actorMethodCallNode(node, args);

           if (retval != null) return retval;

        } else if (_typeID.isSupportedTokenKind(accessedObjKind)) {
           MethodDecl methodDecl =
            (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);

           fieldAccessNode = (FieldAccessNode) fieldAccessNode.accept(this, args);

           node.setMethod(fieldAccessNode);

           accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);

           String methodName = methodDecl.getName();

           // transform the arguments
           List methodArgs =
            TNLManip.traverseList(this, null, args, node.getArgs());

           node.setArgs(methodArgs);

           ExprNode firstArg = null;

           if (methodArgs.size() > 0) {
              firstArg = (ExprNode) methodArgs.get(0);
           }

           if (methodName.equals("booleanValue")) {
              return accessedObj;
           } else if (methodName.equals("intValue")) {
              return accessedObj;
           } else if (methodName.equals("longValue")) {
              return accessedObj;
           } else if (methodName.equals("doubleValue")) {
              return accessedObj;
           } else if (methodName.equals("complexValue")) {
              return accessedObj;
           } else if (methodName.equals("fixValue")) {
              return accessedObj;
           } else if (methodName.equals("booleanMatrix")) {
              return accessedObj;
           } else if (methodName.equals("intMatrix")) {
              return accessedObj;
           } else if (methodName.equals("longMatrix")) {
              return accessedObj;
           } else if (methodName.equals("doubleMatrix")) {
              return accessedObj;
           } else if (methodName.equals("complexMatrix")) {
              return accessedObj;
           } else if (methodName.equals("fixMatrix")) {
              return accessedObj;
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
              return new CastNode(_typeID.typeNodeForKind(accessedObjKind), firstArg);
           } else if (methodName.equals("zero")) {
              return _zeroValue(accessedObjKind, accessedObj);
           } else if (methodName.equals("one")) {
              return _oneValue(accessedObjKind, accessedObj);
           } else if (methodName.equals("oneRight")) {
              return new MethodCallNode(
               new TypeFieldAccessNode(
                new NameNode(AbsentTreeNode.instance, "oneRight"),
                 new TypeNameNode(
                  new NameNode(AbsentTreeNode.instance, "CodeGenUtility"))),
               TNLManip.cons(accessedObj));
           } else if (methodName.equals("isEqualTo")) {
              ExprNode retval = new EQNode(accessedObj, firstArg);
              // mark this node as being created by the actor transformer
              retval.setProperty(PtolemyTypeIdentifier.PTOLEMY_TRANSFORMED_KEY,
               NullValue.instance);
              return retval;
           } else if (methodName.equals("not")) {
              return new NotNode(accessedObj);
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

              switch (accessedObjKind) {
                case PtolemyTypeIdentifier.TYPE_KIND_TOKEN:
                return new StringLitNode("bad token");

                case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_TOKEN:
                case PtolemyTypeIdentifier.TYPE_KIND_INT_TOKEN:
                case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN:
                case PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN:
                return new MethodCallNode(new TypeFieldAccessNode(
                 new NameNode(AbsentTreeNode.instance, "valueOf"),
                 (TypeNameNode) StaticResolution.STRING_TYPE.clone()),
                 TNLManip.cons(accessedObj));

                case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN:
                case PtolemyTypeIdentifier.TYPE_KIND_FIX_TOKEN:
                case PtolemyTypeIdentifier.TYPE_KIND_OBJECT_TOKEN:
                return new MethodCallNode(new ObjectFieldAccessNode(
                 new NameNode(AbsentTreeNode.instance, "toString"),
                 accessedObj), new LinkedList());

                case PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN:
                return accessedObj;

                // for matrices, call the toString() method of
                // the helper classes in ptolemy.math

                case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
                ApplicationUtility.warn("toString() on boolean matrix not " +
                 "supported yet");
                break;

                case PtolemyTypeIdentifier.TYPE_KIND_INT_MATRIX_TOKEN:
                return new MethodCallNode(new TypeFieldAccessNode(
                 new NameNode(AbsentTreeNode.instance, "toString"),
                 new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                  "IntMatrixMath"))),
                 TNLManip.cons(accessedObj));

                case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
                return new MethodCallNode(new TypeFieldAccessNode(
                 new NameNode(AbsentTreeNode.instance, "toString"),
                 new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                  "DoubleMatrixMath"))),
                 TNLManip.cons(accessedObj));

                case PtolemyTypeIdentifier.TYPE_KIND_LONG_MATRIX_TOKEN:
                return new MethodCallNode(new TypeFieldAccessNode(
                 new NameNode(AbsentTreeNode.instance, "toString"),
                 new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                  "LongMatrixMath"))),
                 TNLManip.cons(accessedObj));

                case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN:
                return new MethodCallNode(new TypeFieldAccessNode(
                 new NameNode(AbsentTreeNode.instance, "toString"),
                 new TypeNameNode(new NameNode(AbsentTreeNode.instance,
                  "ComplexMatrixMath"))),
                 TNLManip.cons(accessedObj));

                case PtolemyTypeIdentifier.TYPE_KIND_FIX_MATRIX_TOKEN:
                ApplicationUtility.warn("toString() on fix matrix not " +
                 "supported yet");
                break;
              }
           }

           // method not supported, create a new Token, and call the method
           // with new (converted) args. This may be a problem.
           ApplicationUtility.warn("found unsupported method: " + methodName +
            ", replacing with creation of token and method call");

           return new MethodCallNode(
             new ObjectFieldAccessNode(fieldAccessNode.getName(),
              new AllocateNode(_typeID.typeNodeForKind(accessedObjKind),
               TNLManip.cons(accessedObj), AbsentTreeNode.instance)),
             methodArgs);

        } else if (accessedObjKind == PtolemyTypeIdentifier.TYPE_KIND_PARAMETER) {
           MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);

           fieldAccessNode = (FieldAccessNode) fieldAccessNode.accept(this, args);

           accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);

           String methodName = methodDecl.getName();

           if (accessedObj.classID() == THISFIELDACCESSNODE_ID) {
              if (methodName.equals("getToken")) {
                 return accessedObj; // return the value of the parameter
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
                 // return a list of the transformed arguments
                 // so that side effects are preserved
                 // the list will be processed by visitBlockNode()
                 return TNLManip.traverseList(this, null, args, node.getArgs());
              }
           }
        } else if (_typeID.isSupportedPortKind(accessedObjKind)) {
           Object retval = _portMethodCallNode(node, args);
           if (retval != null) return retval;
        }

        return _defaultVisit(node, args);
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

        switch (_typeID.kind(type)) {

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_TOKEN:
          if (constructorTypes.length == 0) {
            return new BoolLitNode("false");
          }

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0], BoolTypeNode.instance)) {
             // new BooleanToken(boolean)
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          }

          // new BooleanToken(???, ???, ...)
          // call the booleanValue() method of the created BooleanToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "booleanValue"), node),
            new LinkedList());

          case PtolemyTypeIdentifier.TYPE_KIND_INT_TOKEN:
          if (constructorTypes.length == 0) {
             return new IntLitNode("0");
          }

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0], IntTypeNode.instance)) {
             // new IntToken(int)
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          }

          // new IntToken(???, ???, ...)
          // call the intValue() method of the created IntToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "intValue"), node),
            new LinkedList());

          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN:
          if (constructorTypes.length == 0) {
            return new DoubleLitNode("0.0");
          }

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0], DoubleTypeNode.instance)) {
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

          case PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN:
          if (constructorTypes.length == 0) {
            return new LongLitNode("0L");
          }

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0], LongTypeNode.instance)) {
             // new LongToken(long)
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          }

          // new LongToken(???, ???, ...)
          // call the longValue() method of the created LongToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "longValue"), node),
            new LinkedList());

          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN:
          if (constructorTypes.length == 0) {
            return new AllocateNode(PtolemyTypeIdentifier.COMPLEX_TYPE, new LinkedList(),
             AbsentTreeNode.instance);
          }

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
               PtolemyTypeIdentifier.COMPLEX_TYPE)) {
             // new ComplexToken(Complex)
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          }

          // new ComplexToken(???, ???, ...)
          // call the complexValue() method of the created ComplexToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "complexValue"), node),
           new LinkedList());

          case PtolemyTypeIdentifier.TYPE_KIND_FIX_TOKEN:
          if (constructorTypes.length == 0) {
            return new AllocateNode(PtolemyTypeIdentifier.FIX_POINT_TYPE, new LinkedList(),
             AbsentTreeNode.instance);
          }

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
               PtolemyTypeIdentifier.FIX_POINT_TYPE)) {
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


          case PtolemyTypeIdentifier.TYPE_KIND_OBJECT_TOKEN:
          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
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

          case PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN:
          if (constructorTypes.length == 0) {
             return new StringLitNode("");
          }

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
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

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          // no support for constructor with no arguments

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
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

          case PtolemyTypeIdentifier.TYPE_KIND_INT_MATRIX_TOKEN:
          // no support for constructor with no arguments

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
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

          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          // no support for constructor with no arguments

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
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

          case PtolemyTypeIdentifier.TYPE_KIND_LONG_MATRIX_TOKEN:
          // no support for constructor with no arguments

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
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

          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN:
          // no support for constructor with no arguments

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
               TypeUtility.makeArrayType(
                (TypeNode) PtolemyTypeIdentifier.COMPLEX_TYPE.clone(), 2))) {
             // new ComplexMatrixToken(Complex[][])
             return ((ExprNode) constructorArgs.get(0)).accept(this, args);
          }

          // new ComplexMatrixToken(???, ???, ...)
          // call the complexMatrix() method of the created ComplexMatrixToken
          return new MethodCallNode(
           new ObjectFieldAccessNode(
            new NameNode(AbsentTreeNode.instance, "complexMatrix"), node),
            new LinkedList());

          case PtolemyTypeIdentifier.TYPE_KIND_FIX_MATRIX_TOKEN:
          // no support for constructor with no arguments

          if ((constructorTypes.length == 1) &&
              _typePolicy.compareTypes(constructorTypes[0],
               TypeUtility.makeArrayType(
                (TypeNode) PtolemyTypeIdentifier.FIX_POINT_TYPE.clone(), 2))) {
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
        // do not modify anonymous class declarations
        return node;
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        // discard casts to abstract tokens

        ExprNode castTarget = (ExprNode) node.getExpr().accept(this, args);

        TypeNode type = node.getDtype();

        int kind = _typeID.kind(type);
        if (_typeID.isSupportedTokenKind(kind) &&
            !_typeID.isConcreteTokenKind(kind)) {
           return castTarget;
        }

        node.setDtype((TypeNode) node.getDtype().accept(this, args));
        node.setExpr(castTarget);
        return node;
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        TypeNode rightType = node.getDtype();

        if (rightType.classID() == TYPENAMENODE_ID) {

           TypeNode leftType = _typeVisitor.type(node.getExpr());
           int leftKind = _typeID.kind(leftType);

           if (_typeID.isSupportedTokenKind(leftKind)) {
              if (_typePolicy.isSubClass(leftType, rightType)) {
                 return new BoolLitNode("true");
              } else {
                return new BoolLitNode("false");
              }
           }
        }

        return _defaultVisit(node, args);
    }

    public Object visitEQNode(EQNode node, LinkedList args) {
        return _visitEqualityNode(node, args);
    }

    public Object visitNENode(NENode node, LinkedList args) {
        return _visitEqualityNode(node, args);
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        ExprNode leftExpr = node.getExpr1();
        TypeNode leftType = _typeVisitor.type(leftExpr);
        int kind = _typeID.kind(leftType);

        // prevent assignment to ports and parameters
        if (_typeID.isSupportedPortKind(kind) ||
            (kind == PtolemyTypeIdentifier.TYPE_KIND_PARAMETER)) {
           return new NullPntrNode(); // must be eliminated by visitBlockNode()
        }

        node.setExpr1((ExprNode) leftExpr.accept(this, args));
        node.setExpr2((ExprNode) node.getExpr2().accept(this, args));

        return node;
    }

    /** Return a new ExprNode representing the value of the argument Token. */
    public ExprNode tokenToExprNode(Token token) {
        switch (_typeID.kindOfTokenType(token.getType())) {

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_TOKEN:
          if (((BooleanToken) token).booleanValue()) {
             return new BoolLitNode("true");
          }
          return new BoolLitNode("false");

          case PtolemyTypeIdentifier.TYPE_KIND_INT_TOKEN:
          {
            int val = ((IntToken) token).intValue();
            return new IntLitNode(String.valueOf(val));
          }

          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN:
          {
            double val = ((DoubleToken) token).doubleValue();
            return new DoubleLitNode(String.valueOf(val));
          }

          case PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN:
          {
            long val = ((LongToken) token).longValue();
            return new LongLitNode(String.valueOf(val) + "L");
          }

          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN:
          {
            Complex val = ((ComplexToken) token).complexValue();
            LinkedList args = new LinkedList();
            args.addLast(new DoubleLitNode(String.valueOf(val.real)));
            args.addLast(new DoubleLitNode(String.valueOf(val.imag)));

            return new AllocateNode(PtolemyTypeIdentifier.COMPLEX_TYPE,
             args, AbsentTreeNode.instance);
          }

          case PtolemyTypeIdentifier.TYPE_KIND_FIX_TOKEN:
          {
            // FIXME : not done
            FixPoint val = ((FixToken) token).fixValue();
            return null;
          }

          case PtolemyTypeIdentifier.TYPE_KIND_OBJECT_TOKEN:
          ApplicationUtility.error("tokenToExprNode not supported on ObjectToken");

          case PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN:
          {
            return new StringLitNode(token.toString());
          }

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_INT_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_LONG_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN:
          // won't work correctly, but same principle
          case PtolemyTypeIdentifier.TYPE_KIND_FIX_MATRIX_TOKEN:
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

    protected Object _actorClassDeclNode(ClassDeclNode node, LinkedList args) {
        // if the class derives from TypedAtomicActor or SDFAtomicActor,
        // replace the superclass with Object

        TypeNameNode superTypeNode = (TypeNameNode) node.getSuperClass();

        ClassDecl superClassDecl =
         (ClassDecl) JavaDecl.getDecl((NamedNode) superTypeNode);

        int superKind = _typeID.kindOfClassDecl(superClassDecl);

        if (_typeID.isSupportedActorKind(superKind)) {
           node.setSuperClass((TypeNameNode) StaticResolution.OBJECT_TYPE.clone());
           _isBaseClass = true;
        }

        List memberList = node.getMembers();

        memberList = TNLManip.traverseList(this, node, null, memberList);

        Iterator memberItr = memberList.iterator();

        LinkedList newMemberList = new LinkedList();

        while (memberItr.hasNext()) {
           Object memberObj = memberItr.next();

           if (memberObj instanceof List) {
              // allow list return values
              newMemberList.addAll((List) memberObj);
           } else if (memberObj != NullValue.instance) {
              // don't add unwanted fields and methods
              newMemberList.add(memberObj);
           }
        }

        if (_isBaseClass) {
           // default execution methods are not transformed
           _addDefaultExecutionMethods(newMemberList);
        }

        node.setMembers(newMemberList);

        return node;
    }

    public Object _actorMethodCallNode(MethodCallNode node, LinkedList args) {
        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();

        MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);

        String methodName = methodDecl.getName();

        // eliminate the following method calls no matter what
        if (methodName.equals("attributeTypeChanged") ||
            methodName.equals("attributeChanged")) {
           return NullValue.instance;
        }

        ExprNode accessedObj =
         (ExprNode) ExprUtility.accessedObject(fieldAccessNode);

        // for the remaining methods,
        // we can only handle actor calls to the actor itself
        if (accessedObj.classID() != THISFIELDACCESSNODE_ID) return null;

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
           memberList.add(_makeDefaultPreinitializeMethod());
        }

        if (!foundInit) {
           memberList.add(_makeDefaultInitializeMethod());
        }

        if (!foundPrefire) {
           memberList.add(_makeDefaultPrefireMethod());
        }

        if (!foundFire) {
           memberList.add(_makeDefaultFireMethod());
        }

        if (!foundPostfire) {
           memberList.add(_makeDefaultPostfireMethod());
        }

        if (!foundWrapup) {
           memberList.add(_makeDefaultWrapupMethod());
        }
    }

    /** Return an expression to substitute for null, when null is assigned to
     *  a Token of the given type.
     */
    protected ExprNode _dummyValue(TypeNode type) {
        switch (_typeID.kind(type)) {

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_TOKEN:
          return new BoolLitNode("false");

          case PtolemyTypeIdentifier.TYPE_KIND_INT_TOKEN:
          return new IntLitNode("-777777");

          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN:
          return new DoubleLitNode("-777777.77");

          case PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN:
          return new LongLitNode("-777777777777L");

          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_FIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_OBJECT_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_INT_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_LONG_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_FIX_MATRIX_TOKEN:
          return new NullPntrNode();

          // remove this later
          case PtolemyTypeIdentifier.TYPE_KIND_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_SCALAR_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_MATRIX_TOKEN:
          return new NullPntrNode();

          // needed for default port behavior
          case PtolemyTypeIdentifier.TYPE_KIND_DUMMY_TOKEN:
          return new IntLitNode("-777777");
        }

        ApplicationUtility.error("unexpected type for _dummyValue() : " + type);
        return null;
    }

    /** Given a list of TypeNameNodes that are the types of exceptions,
     *  return a new list of TypeNameNodes that are not Ptolemy exception types.
     */
    protected List _eliminatePtolemyExceptionList(List exceptionTypeList) {

        LinkedList retval = new LinkedList();

        Iterator exceptionTypeItr = exceptionTypeList.iterator();

        while (exceptionTypeItr.hasNext()) {
           TypeNameNode exceptionType = (TypeNameNode) exceptionTypeItr.next();

           int exceptionKind = _typeID.kindOfTypeNameNode(exceptionType);

           if (!_typeID.isPtolemyExceptionKind(exceptionKind)) {
              retval.addLast(exceptionType);
           }
        }

        return retval;
    }

    protected Object _makeDefaultPreinitializeMethod() {
        return new MethodDeclNode(PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, "preinitialize"), new LinkedList(),
         new LinkedList(),
         new BlockNode(new LinkedList()), VoidTypeNode.instance);
    }

    protected Object _makeDefaultInitializeMethod() {
        return new MethodDeclNode(PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, "initialize"), new LinkedList(),
         new LinkedList(),
         new BlockNode(new LinkedList()), VoidTypeNode.instance);
    }

    protected Object _makeDefaultPrefireMethod() {
        List blockList = TNLManip.cons(new ReturnNode(new BoolLitNode("true")));
        return new MethodDeclNode(PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, "prefire"), new LinkedList(),
         new LinkedList(),
         new BlockNode(blockList), BoolTypeNode.instance);
    }

    protected Object _makeDefaultFireMethod() {
        return new MethodDeclNode(PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, "fire"), new LinkedList(),
         new LinkedList(),
         new BlockNode(new LinkedList()), VoidTypeNode.instance);
    }

    protected Object _makeDefaultPostfireMethod() {
        List blockList = TNLManip.cons(new ReturnNode(new BoolLitNode("true")));
        return new MethodDeclNode(PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, "postfire"), new LinkedList(),
         new LinkedList(),
         new BlockNode(blockList), BoolTypeNode.instance);
    }

    protected Object _makeDefaultWrapupMethod() {
        return new MethodDeclNode(PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, "wrapup"),
         new LinkedList(),
         new LinkedList(),
         new BlockNode(new LinkedList()), VoidTypeNode.instance);
    }

    protected StatementNode _makeStmt(Object obj) {
        if (obj instanceof List) {
           return new BlockNode(_makeStmtList((List) obj));
        } else if (obj instanceof ExprNode) {
           ExprNode exprNode = (ExprNode) obj;

           if (ExprUtility.isStatementExpression(exprNode)) {
              return new ExprStmtNode(exprNode);
           }

           return new EmptyStmtNode();
        } else if (obj == NullValue.instance) {
           return new EmptyStmtNode();
        } else if (obj instanceof StatementNode) {
           return (StatementNode) obj;
        } else {
           throw new IllegalArgumentException("unknown object : " + obj);
        }
    }

    protected List _makeStmtList(List retvalList) {
        LinkedList newStmtList = new LinkedList();

        Iterator retvalItr = retvalList.iterator();

        while (retvalItr.hasNext()) {
           Object obj = retvalItr.next();

           if (obj instanceof List) {
              newStmtList.addAll(_makeStmtList((List) obj));
           } else if (obj != NullValue.instance) {
              // don't add statements that have NullValue.instance as the result
              // of visitation
              obj = _makeStmt(obj);

              // eliminate empty statements
              if (!(obj instanceof EmptyStmtNode)) {
                 newStmtList.addLast(_makeStmt(obj));
              }
           }
        }

        return newStmtList;
    }

    protected ExprNode _oneValue(int accessedObjKind, ExprNode accessedObj) {
        switch (accessedObjKind) {
          case PtolemyTypeIdentifier.TYPE_KIND_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_SCALAR_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_MATRIX_TOKEN:
          return new NullPntrNode();

          case PtolemyTypeIdentifier.TYPE_KIND_INT_TOKEN:
          return new IntLitNode("1");

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_TOKEN:
          return new BoolLitNode("true");

          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN:
          return new DoubleLitNode("1.0");

          case PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN:
          return new LongLitNode("1L");

          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN:
          return new TypeFieldAccessNode(new NameNode(AbsentTreeNode.instance, "ONE"),
           (TypeNameNode) PtolemyTypeIdentifier.COMPLEX_TYPE.clone());

          case PtolemyTypeIdentifier.TYPE_KIND_OBJECT_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN:
          ApplicationUtility.error("found one() method call on unsupported token kind " +
           accessedObjKind);
          return null;

          case PtolemyTypeIdentifier.TYPE_KIND_FIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_INT_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_LONG_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_FIX_MATRIX_TOKEN:
          return new MethodCallNode(new TypeFieldAccessNode(
                  new NameNode(AbsentTreeNode.instance, "one"),
                   new TypeNameNode(
                    new NameNode(AbsentTreeNode.instance, "CodeGenUtility"))),
                  TNLManip.cons(accessedObj));

          default:
          ApplicationUtility.error("found zero() method call on unknown token kind " +
           accessedObjKind);
          return null;
        }
    }


    protected Object _portFieldDeclNode(FieldDeclNode node, LinkedList args) {
        // by default, get rid of the port field declaration
        return NullValue.instance;
    }

    protected Object _portMethodCallNode(MethodCallNode node, LinkedList args) {
        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();

        MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);

        fieldAccessNode = (FieldAccessNode) fieldAccessNode.accept(this, args);

        node.setMethod(fieldAccessNode);

        ExprNode accessedObj =
         (ExprNode) ExprUtility.accessedObject(fieldAccessNode);

        node.setArgs(TNLManip.traverseList(this, node, args, node.getArgs()));

        // we can only handle ports that are fields of the actor
        if (accessedObj.classID() != THISFIELDACCESSNODE_ID) return node;

        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);

        String methodName = methodDecl.getName();

        String varName = typedDecl.getName();

        TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(varName);

        if (methodName.equals("get")) {
           // dummy value
           TypeNameNode portType =
            _typeID.typeNodeForTokenType(port.getType());

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
        } else if (methodName.equals("send") ||
                   methodName.equals("setInput") ||
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
        return node;
    }

    protected Object _visitEqualityNode(EqualityNode node, LinkedList args) {
        ExprNode leftExpr = node.getExpr1();
        TypeNode leftType = _typeVisitor.type(leftExpr);
        ExprNode rightExpr = node.getExpr2();
        TypeNode rightType = _typeVisitor.type(rightExpr);

        int leftKind = _typeID.kind(leftType);
        int rightKind = _typeID.kind(rightType);

        if (_typeID.isSupportedTokenKind(leftKind)) {

           ApplicationUtility.warn("comparison of Token found : " + leftExpr +
            " with " + rightExpr);

           if (rightExpr.classID() == NULLPNTRNODE_ID) {
              ApplicationUtility.warn("comparison with null replaced with dummy value");

              node.setExpr2(_dummyValue(leftType));

              node.setExpr1((ExprNode) leftExpr.accept(this, args));

              return node;
           }
        }

        if (_typeID.isSupportedTokenKind(rightKind)) {

           ApplicationUtility.warn("comparison of Token found : " + leftExpr +
            " with " + rightExpr + "(may be a duplicate message)");

           if (leftExpr.classID() == NULLPNTRNODE_ID) {
              ApplicationUtility.warn("comparison with null replaced with dummy value");

              node.setExpr1(_dummyValue(rightType));

              node.setExpr2((ExprNode) rightExpr.accept(this, args));

              return node;
           }
        }

        return _defaultVisit(node, args);
    }

    protected ExprNode _zeroValue(int accessedObjKind, ExprNode accessedObj) {
        switch (accessedObjKind) {
          case PtolemyTypeIdentifier.TYPE_KIND_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_SCALAR_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_MATRIX_TOKEN:
          return new NullPntrNode();

          case PtolemyTypeIdentifier.TYPE_KIND_INT_TOKEN:
          return new IntLitNode("0");

          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_TOKEN:
          return new BoolLitNode("false");

          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_TOKEN:
          return new DoubleLitNode("0.0");

          case PtolemyTypeIdentifier.TYPE_KIND_LONG_TOKEN:
          return new LongLitNode("0L");

          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_TOKEN:
          return new TypeFieldAccessNode(new NameNode(AbsentTreeNode.instance, "ZERO"),
           (TypeNameNode) PtolemyTypeIdentifier.COMPLEX_TYPE.clone());

          case PtolemyTypeIdentifier.TYPE_KIND_OBJECT_TOKEN:
          ApplicationUtility.error("found zero() method call on ObjectToken");
          return null;

          case PtolemyTypeIdentifier.TYPE_KIND_STRING_TOKEN:
          return new StringLitNode("");

          case PtolemyTypeIdentifier.TYPE_KIND_FIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_INT_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_LONG_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX_TOKEN:
          case PtolemyTypeIdentifier.TYPE_KIND_FIX_MATRIX_TOKEN:
          return new MethodCallNode(new TypeFieldAccessNode(
                  new NameNode(AbsentTreeNode.instance, "zero"),
                   new TypeNameNode(
                    new NameNode(AbsentTreeNode.instance, "CodeGenUtility"))),
                  TNLManip.cons(accessedObj));

          default:
          ApplicationUtility.error("found zero() method call on unknown token kind " +
           accessedObjKind);
          return null;
        }
    }

    protected ActorCodeGeneratorInfo _actorInfo = null;

    protected PtolemyTypeVisitor _typeVisitor = null;
    protected PtolemyTypePolicy _typePolicy = null;
    protected PtolemyTypeIdentifier _typeID = null;

    /** The Ptolemy name of the instance of the actor. */
    protected final String _actorName;

    /** A count of how many constructors in the actor class that have been
     *  found so far.
     */
    protected int _constructorCount = 0;

    /** A flag indicating that the actor class inherits from object, so
     *  super.XXX() method calls must be eliminated.
     */
    protected boolean _isBaseClass = false;
}
