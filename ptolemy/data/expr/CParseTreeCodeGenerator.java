/* A visitor for parse trees of the expression language that generates C code.

 Copyright (c) 2003-2014 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 */
package ptolemy.data.expr;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// ParseTreeCodeGenerator

/**
 This class visits parse trees and generates soot instructions that evaluate the parse tree.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class CParseTreeCodeGenerator extends AbstractParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Generate code for a node.
     *  @param node The node for which code is generated.
     *  @exception IllegalActionException If type inference fails.
     */
    public void generateCode(ASTPtRootNode node) throws IllegalActionException {
        ParseTreeTypeInference typeInference = new ParseTreeTypeInference();
        typeInference.inferTypes(node); // FIXME: scope?

        //    _scope = scope;
        _nodeToLocalName = new HashMap();
        _nodeNumber = 0;
        node.visit(this);
        _nodeToLocalName = null;

        //   _scope = null;
    }

    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        System.out.println(nodeName + " = FIXME:Array");
    }

    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        // Make sure that exactly one of AND, OR, XOR is set.
        _assert(node.isBitwiseAnd() ^ node.isBitwiseOr() ^ node.isBitwiseXor(),
                node, "Invalid operation");

        StringBuffer statement = new StringBuffer(nodeName + " = "
                + _nodeToLocalName.get(node.jjtGetChild(0)));

        for (int i = 1; i < numChildren; i++) {
            if (node.isBitwiseAnd()) {
                statement.append("&");
            } else if (node.isBitwiseOr()) {
                statement.append("|");
            } else if (node.isBitwiseXor()) {
                statement.append("^");
            } else {
                throw new RuntimeException("Unrecognized node");
            }

            statement.append(_nodeToLocalName.get(node.jjtGetChild(i)));
        }

        System.out.println(statement.toString());
    }

    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.
        int numChildren = node.jjtGetNumChildren();
        int argCount = numChildren - 1;

        for (int i = 1; i < numChildren; i++) {
            _generateChild(node, i);
        }

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        if (_isValidName(node.getFunctionName())) {
            //          Local local = _getLocalForName(node.getFunctionName());
            //             Local resultLocal = Jimple.v().newLocal("token",
            //                     RefType.v(PtolemyUtilities.tokenClass));
            //             _body.getLocals().add(resultLocal);
            if (argCount == 1) {
                // array..
                System.out.println(nodeName + " = ");
                System.out
                .println(_nodeToLocalName.get(node.jjtGetChild(0))
                        + "["
                        + _nodeToLocalName.get(node.jjtGetChild(1))
                        + "]");
            } else if (argCount == 2) {
                // matrix..
                System.out.println(nodeName + " = ");
                System.out.println(_nodeToLocalName.get(node.jjtGetChild(0))
                        + "[" + _nodeToLocalName.get(node.jjtGetChild(1)) + ","
                        + _nodeToLocalName.get(node.jjtGetChild(1)) + "]");
            } else {
                throw new IllegalActionException("Wrong number of indices "
                        + "when referencing " + node.getFunctionName());
            }

            return;
        }

        if (node.getFunctionName().compareTo("eval") == 0) {
            throw new IllegalActionException("unimplemented case");
        }

        if (node.getFunctionName().compareTo("matlab") == 0) {
            throw new IllegalActionException("unimplemented case");
        }

        // Otherwise, try to reflect the method name.
        // The array of token types that the method takes.
        ptolemy.data.type.Type[] argTypes = new ptolemy.data.type.Type[argCount];

        for (int i = 0; i < argCount; i++) {
            argTypes[i] = ((ASTPtRootNode) node.jjtGetChild(i + 1)).getType();
        }

        // Find the method...
        CachedMethod cachedMethod = CachedMethod.findMethod(
                node.getFunctionName(), argTypes, CachedMethod.FUNCTION);

        if (!cachedMethod.isValid()) {
            throw new IllegalActionException("Function " + cachedMethod
                    + " not found.");
        }

        if (cachedMethod instanceof CachedMethod.BaseConvertCachedMethod
                || cachedMethod instanceof CachedMethod.ArrayMapCachedMethod
                || cachedMethod instanceof CachedMethod.MatrixMapCachedMethod) {
            throw new IllegalActionException(
                    "CodeGeneration not supported for "
                            + cachedMethod.getClass());
        }

        Method method = cachedMethod.getMethod();

        //CachedMethod.ArgumentConversion[] conversions = cachedMethod
        //        .getConversions();

        //for (int i = 0; i < argCount; i++) {
        // Insert the appropriate conversion.
        //    String argName = (String) _nodeToLocalName.get(node
        //            .jjtGetChild(i + 1));

        // _convertTokenArgToJavaArg(
        //    tokenLocal, argTypes[i], conversions[i]);
        //            args.add(argLocal);
        //}

        System.out.println(nodeName + " = FIXME:method invocation of "
                + method.getName());

        // Convert the result back to a token.
        String convertedReturnName = "FIXME"; //_convertJavaResultToToken(returnLocal, returnType);
        _nodeToLocalName.put(node, convertedReturnName);
    }

    // Add code to the method being generated to convert the given
    // returnLocal, with the given returnType to a token type.  Return
    // the new token local.

    /*private Local _convertJavaResultToToken(
     Local returnLocal, Type returnType)
     throws IllegalActionException {
     Local tokenLocal;
     FIXME!
     if (returnType instanceof RefType &&
     SootUtilities.derivesFrom(
     ((RefType)returnType).getSootClass(),
     PtolemyUtilities.tokenClass)) {
     tokenLocal = returnLocal;
     } else if (returnType.equals(
     ArrayType.v(PtolemyUtilities.tokenType, 1))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.arrayTokenClass,
     PtolemyUtilities.arrayTokenConstructor,
     returnLocal);
     } else if (returnType.equals(DoubleType.v())) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.doubleTokenClass,
     PtolemyUtilities.doubleTokenConstructor,
     returnLocal);
     } else if (returnType.equals(LongType.v())) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.longTokenClass,
     PtolemyUtilities.longTokenConstructor,
     returnLocal);
     } else if (returnType.equals(RefType.v("java.lang.String"))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.stringTokenClass,
     PtolemyUtilities.stringTokenConstructor,
     returnLocal);
     } else if (returnType.equals(BooleanType.v())) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.booleanTokenClass,
     PtolemyUtilities.booleanTokenConstructor,
     returnLocal);
     } else if (returnType.equals(RefType.v("ptolemy.math.Complex"))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.complexTokenClass,
     PtolemyUtilities.complexTokenConstructor,
     returnLocal);
     } else if (returnType.equals(RefType.v("ptolemy.math.FixPoint"))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.fixTokenClass,
     PtolemyUtilities.fixTokenConstructor,
     returnLocal);
     } else if (returnType.equals(ArrayType.v(BooleanType.v(),2))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.booleanMatrixTokenClass,
     PtolemyUtilities.booleanMatrixTokenConstructor,
     returnLocal);

     } else if (returnType.equals(ArrayType.v(IntType.v(),2))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.intMatrixTokenClass,
     PtolemyUtilities.intMatrixTokenConstructor,
     returnLocal);

     } else if (returnType.equals(ArrayType.v(LongType.v(),2))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.doubleMatrixTokenClass,
     PtolemyUtilities.doubleMatrixTokenConstructor,
     returnLocal);

     } else if (returnType.equals(ArrayType.v(DoubleType.v(),2))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.doubleMatrixTokenClass,
     PtolemyUtilities.doubleMatrixTokenConstructor,
     returnLocal);

     } else if (returnType.equals(ArrayType.v(RefType.v("ptolemy.math.Complex"),2))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.complexMatrixTokenClass,
     PtolemyUtilities.complexMatrixTokenConstructor,
     returnLocal);

     } else if (returnType.equals(ArrayType.v(RefType.v("ptolemy.math.FixPoint"),2))) {
     tokenLocal = PtolemyUtilities.addTokenLocalBefore(_body, _insertPoint, "token",
     PtolemyUtilities.fixMatrixTokenClass,
     PtolemyUtilities.fixMatrixTokenConstructor,
     returnLocal);

     } else {
     throw new IllegalActionException("unrecognized case");
     }
     return tokenLocal;
     }

     private Local _convertTokenArgToJavaArg(Local tokenLocal,
     ptolemy.data.type.Type tokenType,
     CachedMethod.ArgumentConversion conversion)
     throws IllegalActionException {

     if (conversion == CachedMethod.IDENTITY_CONVERSION) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     PtolemyUtilities.tokenType);
     _body.getLocals().add(tempLocal);

     // Add the new local to the list of arguments
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     tokenLocal),
     _insertPoint);
     return tempLocal;
     } else if (conversion == CachedMethod.ARRAYTOKEN_CONVERSION) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.arrayTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ArrayType.v(RefType.v(PtolemyUtilities.objectClass), 1));
     _body.getLocals().add(resultLocal);

     // Add the new local to the list of arguments
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v(PtolemyUtilities.arrayTokenClass))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.arrayValueMethod)), _insertPoint);
     return resultLocal;
     } else if (conversion == CachedMethod.NATIVE_CONVERSION) {
     if (tokenType == ptolemy.data.type.BaseType.DOUBLE) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.doubleTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     DoubleType.v());
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v(PtolemyUtilities.doubleTokenClass))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.doubleValueMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.UNSIGNED_BYTE) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.unsignedByteTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ByteType.v());
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v(PtolemyUtilities.unsignedByteTokenClass))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.unsignedByteValueMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.INT) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.intTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     IntType.v());
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.IntToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.intValueMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.LONG) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.longTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     LongType.v());
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.LongToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.longValueMethod)), _insertPoint);
     return resultLocal;
     }  else if (tokenType == ptolemy.data.type.BaseType.STRING) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.stringTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     RefType.v("java.lang.String"));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.StringToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.stringValueMethod)), _insertPoint);
     return resultLocal;
     }  else if (tokenType == ptolemy.data.type.BaseType.BOOLEAN) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.booleanTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     BooleanType.v());
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.BooleanToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.booleanValueMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.COMPLEX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.complexTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     RefType.v("ptolemy.math.Complex"));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.ComplexToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.complexValueMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.FIX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.intTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     RefType.v("ptolemy.math.FixPoint"));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.FixToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.fixValueMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.DOUBLE_MATRIX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.doubleMatrixTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ArrayType.v(DoubleType.v(),2));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.DoubleMatrixToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.doubleMatrixMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.INT_MATRIX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.intMatrixTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ArrayType.v(IntType.v(),2));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.IntMatrixToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.intMatrixMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.LONG_MATRIX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.longMatrixTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ArrayType.v(LongType.v(),2));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.LongMatrixToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.longMatrixMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.BOOLEAN_MATRIX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.booleanMatrixTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ArrayType.v(BooleanType.v(),2));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.BooleanMatrixToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.booleanMatrixMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.COMPLEX_MATRIX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.complexMatrixTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ArrayType.v(RefType.v("ptolemy.math.Complex"),2));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.ComplexMatrixToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     resultLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.complexMatrixMethod)), _insertPoint);
     return resultLocal;
     } else if (tokenType == ptolemy.data.type.BaseType.FIX_MATRIX) {
     Local tempLocal = Jimple.v().newLocal("arg" ,
     RefType.v(PtolemyUtilities.fixMatrixTokenClass));
     _body.getLocals().add(tempLocal);
     Local resultLocal = Jimple.v().newLocal("arg" ,
     ArrayType.v(RefType.v("ptolemy.math.FixPoint"),2));
     _body.getLocals().add(resultLocal);
     // Add the new local to the list of arguments

     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newCastExpr(
     tokenLocal,
     RefType.v("ptolemy.data.FixMatrixToken"))), _insertPoint);
     _units.insertBefore(
     Jimple.v().newAssignStmt(
     tempLocal,
     Jimple.v().newVirtualInvokeExpr(
     tempLocal,
     PtolemyUtilities.fixMatrixMethod)), _insertPoint);
     return resultLocal;
     } else {// if (argTypes[i] instanceof ArrayType) {
     throw new IllegalActionException(
     "CodeGeneration not supported for arrayType");
     }
     } else {
     throw new IllegalActionException(
     "CodeGeneration not supported for argument " +
     "conversion " + conversion);
     }

     }
     */
    @Override
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        throw new IllegalActionException("Cannot generate code"
                + " for functional if!");

        // Note that we take care to have short-circuit evaluation here.

        /* _generateChild(node, 0);

         Local conditionTokenLocal =
         (Local)_nodeToLocal.get(node.jjtGetChild(0));

         Local booleanTokenLocal = Jimple.v().newLocal("result" ,
         RefType.v(PtolemyUtilities.booleanTokenClass));
         _body.getLocals().add(booleanTokenLocal);
         Local flagLocal = Jimple.v().newLocal("result" ,
         BooleanType.v());
         _body.getLocals().add(flagLocal);

         Local resultLocal = Jimple.v().newLocal("result" ,
         PtolemyUtilities.tokenType);
         _body.getLocals().add(resultLocal);

         Stmt startTrue = Jimple.v().newNopStmt();
         Stmt endTrue = Jimple.v().newNopStmt();

         // Check the condition
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         booleanTokenLocal,
         Jimple.v().newCastExpr(
         conditionTokenLocal,
         RefType.v(PtolemyUtilities.booleanTokenClass))), _insertPoint);
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         flagLocal,
         Jimple.v().newVirtualInvokeExpr(
         booleanTokenLocal,
         PtolemyUtilities.booleanValueMethod)), _insertPoint);
         // If condition is true then skip to start of true branch.
         _units.insertBefore(
         Jimple.v().newIfStmt(
         Jimple.v().newEqExpr(
         flagLocal,
         IntConstant.v(1)),
         startTrue),
         _insertPoint);

         // Otherwise, do the false branch,
         _generateChild(node, 2);
         // Assign the false result
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         resultLocal,
         (Local)_nodeToLocal.get(node.jjtGetChild(2))), _insertPoint);
         // And continue on.
         _units.insertBefore(Jimple.v().newGotoStmt(endTrue),
         _insertPoint);

         _units.insertBefore(startTrue,
         _insertPoint);

         // Otherwise, do the true branch,
         _generateChild(node, 1);
         // Assign the true result
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         resultLocal,
         (Local)_nodeToLocal.get(node.jjtGetChild(1))), _insertPoint);
         _units.insertBefore(endTrue, _insertPoint);

         _nodeToLocal.put(node, resultLocal);
         */
    }

    @Override
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        throw new IllegalActionException("Cannot generate code"
                + " for function definitions!");
    }

    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        if (node.isConstant() && node.isEvaluated()) {
            System.out.println(nodeName + " = " + node.getToken());
            return;
        }

        System.out.println(nodeName + " = "
                + _getLocalNameForName(node.getName()));
    }

    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        // Note: doesn't ensure short circuit
        StringBuffer statement = new StringBuffer(nodeName + " = "
                + _nodeToLocalName.get(node.jjtGetChild(0)));

        for (int i = 1; i < numChildren; i++) {
            if (node.isLogicalAnd()) {
                statement.append("&&");
            } else if (node.isLogicalOr()) {
                statement.append("||");
            } else {
                throw new RuntimeException("Unrecognized node");
            }

            statement.append(_nodeToLocalName.get(node.jjtGetChild(i)));
        }

        System.out.println(statement.toString());
    }

    @Override
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        System.out.println(nodeName + " = FIXME:Matrix");
    }

    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        System.out.println(nodeName + " = FIXME:MethodCall");

        /*    // Method calls are generally not cached...  They are repeated
         // every time the tree is evaluated.

         int argCount = node.jjtGetNumChildren();
         _generateAllChildren(node);
         // The first child is the token on which to invoke the method.

         // Handle indexing into a record.
         ptolemy.data.type.Type baseTokenType =
         ((ASTPtRootNode)node.jjtGetChild(0)).getType();
         if (argCount == 1 &&
         baseTokenType instanceof RecordType) {
         RecordType type = (RecordType)baseTokenType;
         if (type.labelSet().contains(node.getMethodName())) {
         Local originalBaseLocal = (Local)
         _nodeToLocal.get(node.jjtGetChild(0));
         Local baseLocal = Jimple.v().newLocal("base",
         RefType.v(PtolemyUtilities.recordTokenClass));
         _body.getLocals().add(baseLocal);
         // Cast the record.
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         baseLocal,
         Jimple.v().newCastExpr(
         originalBaseLocal,
         RefType.v(PtolemyUtilities.recordTokenClass))), _insertPoint);

         // invoke get()
         Local returnLocal = Jimple.v().newLocal("returnValue",
         RefType.v(PtolemyUtilities.tokenClass));
         _body.getLocals().add(returnLocal);
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         returnLocal,
         Jimple.v().newVirtualInvokeExpr(
         baseLocal,
         PtolemyUtilities.recordGetMethod,
         StringConstant.v(node.getMethodName()))), _insertPoint);
         _nodeToLocal.put(node, returnLocal);

         return;
         }
         }

         // The array of token types that the method takes.
         ptolemy.data.type.Type[] argTypes =
         new ptolemy.data.type.Type[node.jjtGetNumChildren()];
         for (int i = 0; i < node.jjtGetNumChildren(); i++) {
         argTypes[i] = ((ASTPtRootNode)node.jjtGetChild(i)).getType();
         }

         // Find the method...
         CachedMethod cachedMethod =
         CachedMethod.findMethod(node.getMethodName(),
         argTypes, CachedMethod.METHOD);

         if (!cachedMethod.isValid()) {
         throw new IllegalActionException("Function " + cachedMethod +
         " not found.");
         }

         if (cachedMethod instanceof CachedMethod.ArrayMapCachedMethod ||
         cachedMethod instanceof CachedMethod.MatrixMapCachedMethod) {
         throw new IllegalActionException(
         "CodeGeneration not supported for " +
         cachedMethod.getClass());
         }

         Method method = cachedMethod.getMethod();

         // Find the corresponding soot method.
         SootMethod sootMethod = SootUtilities.getSootMethodForMethod(method);

         Local originalBaseLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
         RefType baseType = RefType.v(sootMethod.getDeclaringClass());
         Local baseLocal = Jimple.v().newLocal("base",
         baseType);
         _body.getLocals().add(baseLocal);

         if (cachedMethod instanceof CachedMethod.BaseConvertCachedMethod) {
         RefType tempBaseType = PtolemyUtilities.getSootTypeForTokenType(
         argTypes[0]);
         Local tempBaseLocal = _convertTokenArgToJavaArg(
         originalBaseLocal, argTypes[0],
         ((CachedMethod.BaseConvertCachedMethod)
         cachedMethod).getBaseConversion());
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         baseLocal,
         Jimple.v().newCastExpr(
         tempBaseLocal,
         baseType)), _insertPoint);
         } else {
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         baseLocal,
         Jimple.v().newCastExpr(
         originalBaseLocal,
         baseType)), _insertPoint);
         }


         // The list of locals that are arguments to the function.
         List args = new LinkedList();

         CachedMethod.ArgumentConversion[] conversions =
         cachedMethod.getConversions();
         for (int i = 1; i < node.jjtGetNumChildren(); i++) {
         Local tokenLocal = (Local)_nodeToLocal.get(node.jjtGetChild(i));

         // Insert the appropriate conversion.
         Local argLocal = _convertTokenArgToJavaArg(
         tokenLocal, argTypes[i-1], conversions[i-1]);
         args.add(argLocal);
         }

         Type returnType = sootMethod.getReturnType();
         Local returnLocal = Jimple.v().newLocal("returnValue",
         returnType);
         _body.getLocals().add(returnLocal);

         // Actually invoke the method.
         _units.insertBefore(
         Jimple.v().newAssignStmt(
         returnLocal,
         Jimple.v().newVirtualInvokeExpr(
         baseLocal, sootMethod, args)), _insertPoint);

         // Convert the result back to a token.
         Local tokenLocal = _convertJavaResultToToken(returnLocal, returnType);


         //      RefType objectType = RefType.v(PtolemyUtilities.objectClass);
         //         Local argValuesLocal = Jimple.v().newLocal("tokenArray",
         //                 ArrayType.v(objectType, 1));
         //         _body.getLocals().add(argValuesLocal);
         //         _units.insertBefore(
         //                 Jimple.v().newAssignStmt(
         //                         argValuesLocal,
         //                         Jimple.v().newNewArrayExpr(
         //                                 objectType,
         //                                 IntConstant.v(node.jjtGetNumChildren()))));

         //         for (int i = 0; i < node.jjtGetNumChildren(); i++) {
         //             _units.insertBefore(
         //                     Jimple.v().newAssignStmt(
         //                             Jimple.v().newArrayRef(
         //                                     argValuesLocal,
         //                                     IntConstant.v(i)),
         //                             (Local)_nodeToLocal.get(node.jjtGetChild(i))));
         //         }

         //         RefType typeType = RefType.v("ptolemy.data.type.Type");
         //         Local argTypesLocal = Jimple.v().newLocal("tokenTypes",
         //                 ArrayType.v(typeType, 1));
         //         _body.getLocals().add(argTypesLocal);
         //         Local typeLocal = Jimple.v().newLocal("classType",
         //                 typeType);
         //         _body.getLocals().add(typeLocal);
         //         Local tokenLocal = Jimple.v().newLocal("token",
         //                 PtolemyUtilities.tokenType);
         //         _body.getLocals().add(tokenLocal);

         //         _units.insertBefore(
         //                 Jimple.v().newAssignStmt(
         //                         argTypesLocal,
         //                         Jimple.v().newNewArrayExpr(
         //                                 typeType,
         //                                 IntConstant.v(argCount))));

         //         Local indexLocal = Jimple.v().newLocal("index", IntType.v());
         //         _body.getLocals().add(indexLocal);

         //         // The list of initializer instructions.
         //         List initializerList = new LinkedList();
         //         initializerList.add(
         //                 Jimple.v().newAssignStmt(
         //                         indexLocal,
         //                         IntConstant.v(0)));

         //         // The list of body instructions.
         //         List bodyList = new LinkedList();
         //         bodyList.add(
         //                 Jimple.v().newAssignStmt(
         //                         tokenLocal,
         //                         Jimple.v().newArrayRef(
         //                                 argValuesLocal,
         //                                 indexLocal)));
         //         bodyList.add(
         //                 Jimple.v().newAssignStmt(
         //                         typeLocal,
         //                         Jimple.v().newVirtualInvokeExpr(
         //                                 tokenLocal,
         //                                 PtolemyUtilities.tokenGetTypeMethod)));
         //         bodyList.add(
         //                 Jimple.v().newAssignStmt(
         //                         Jimple.v().newArrayRef(
         //                                 argTypesLocal,
         //                                 indexLocal),
         //                         typeLocal));


         //         // Increment the index.
         //         bodyList.add(
         //                 Jimple.v().newAssignStmt(
         //                         indexLocal,
         //                         Jimple.v().newAddExpr(
         //                                 indexLocal,
         //                                 IntConstant.v(1))));

         //         Expr conditionalExpr =
         //             Jimple.v().newLtExpr(
         //                     indexLocal,
         //                     IntConstant.v(argCount));

         //         Stmt stmt = Jimple.v().newNopStmt();
         //         _units.insertBefore(stmt);

         //         SootUtilities.createForLoopBefore(_body,
         //                 stmt,
         //                 initializerList,
         //                 bodyList,
         //                 conditionalExpr);

         //         SootMethod methodCallEvaluationMethod =
         //             Scene.v().getMethod("<ptolemy.data.expr.ParseTreeEvaluator: ptolemy.data.Token methodCall(java.lang.String,int,ptolemy.data.type.Type[],java.lang.Object[])>");
         //         List argList = new ArrayList();
         //         argList.add(StringConstant.v(node.getMethodName()));
         //         argList.add(IntConstant.v(argCount));
         //         argList.add(argTypesLocal);
         //         argList.add(argValuesLocal);

         //         _units.insertBefore(
         //                 Jimple.v().newAssignStmt(
         //                         tokenLocal,
         //                         Jimple.v().newStaticInvokeExpr(
         //                                 methodCallEvaluationMethod,
         //                                 argList)));

         _nodeToLocal.put(node, tokenLocal);
         */
    }

    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        StringBuffer statement = new StringBuffer(nodeName + " = "
                + _nodeToLocalName.get(node.jjtGetChild(0)));

        for (int i = 1; i < numChildren; i++) {
            statement.append("^" + _nodeToLocalName.get(node.jjtGetChild(i)));
        }

        System.out.println(statement.toString());
    }

    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        StringBuffer statement = new StringBuffer(nodeName + " = "
                + _nodeToLocalName.get(node.jjtGetChild(0)));

        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);

            if (operator.kind == PtParserConstants.MULTIPLY) {
                statement.append("*");
            } else if (operator.kind == PtParserConstants.DIVIDE) {
                statement.append("/");
            } else if (operator.kind == PtParserConstants.MODULO) {
                statement.append("%");
            } else {
                _assert(false, node, "Invalid operation");
            }

            statement.append(_nodeToLocalName.get(node.jjtGetChild(i)));
        }

        System.out.println(statement.toString());
    }

    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        throw new IllegalActionException("Cannot generate code"
                + " for records!");
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        Token operator = node.getOperator();

        StringBuffer statement = new StringBuffer(nodeName + " = "
                + _nodeToLocalName.get(node.jjtGetChild(0)));

        if (operator.kind == PtParserConstants.EQUALS) {
            statement.append("==");
        } else if (operator.kind == PtParserConstants.NOTEQUALS) {
            statement.append("!=");
        } else if (operator.kind == PtParserConstants.GTE) {
            statement.append(">=");
        } else if (operator.kind == PtParserConstants.GT) {
            statement.append(">");
        } else if (operator.kind == PtParserConstants.LTE) {
            statement.append("<=");
        } else if (operator.kind == PtParserConstants.LT) {
            statement.append("<");
        } else {
            throw new IllegalActionException("Invalid operation "
                    + operator.image);
        }

        statement.append(_nodeToLocalName.get(node.jjtGetChild(1)));
        System.out.println(statement.toString());
    }

    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");

        Token operator = node.getOperator();

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        StringBuffer statement = new StringBuffer(nodeName + " = "
                + _nodeToLocalName.get(node.jjtGetChild(0)));

        if (operator.kind == PtParserConstants.SHL) {
            statement.append("<<");
        } else if (operator.kind == PtParserConstants.SHR) {
            statement.append(">>");
        } else if (operator.kind == PtParserConstants.LSHR) {
            statement.append("<");
        } else {
            _assert(false, node, "Invalid operation");
        }

        statement.append(_nodeToLocalName.get(node.jjtGetChild(1)));
        System.out.println(statement.toString());
    }

    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _generateAllChildren(node);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is "
                        + "not equal to number of operators plus one");

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        StringBuffer statement = new StringBuffer(nodeName + " = "
                + _nodeToLocalName.get(node.jjtGetChild(0)));

        for (int i = 1; i < numChildren; i++) {
            Token operator = (Token) lexicalTokenList.get(i - 1);

            if (operator.kind == PtParserConstants.PLUS) {
                statement.append("+");
            } else if (operator.kind == PtParserConstants.MINUS) {
                statement.append("-");
            } else {
                _assert(false, node, "Invalid operation");
            }

            statement.append(_nodeToLocalName.get(node.jjtGetChild(i)));
        }

        System.out.println(statement.toString());
    }

    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _generateAllChildren(node);
        _assert(node.jjtGetNumChildren() == 1, node,
                "Unary node must have exactly one child!");

        String nodeName = "node" + _nodeNumber++;
        _nodeToLocalName.put(node, nodeName);

        StringBuffer statement = new StringBuffer(nodeName + " = ");

        if (node.isMinus()) {
            // Note: not quite ptolemy semantics.
            statement.append("-" + _nodeToLocalName.get(node.jjtGetChild(0)));
        } else if (node.isNot()) {
            statement.append("!" + _nodeToLocalName.get(node.jjtGetChild(0)));
        } else if (node.isBitwiseNot()) {
            statement.append("~" + _nodeToLocalName.get(node.jjtGetChild(0)));
        } else {
            _assert(false, node, "Unrecognized unary node");
        }

        System.out.println(statement.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Assert that the given boolean value, which describes the given
     * parse tree node is true.  If it is false, then throw a new
     * InternalErrorException that describes the node that includes
     * the given message.
     * @param flag The value to be checked.  If false, then an
     * InternalErrorException is thrown.
     * @param node The node.
     * @param message The error message to be included in the exception
     * if the flag parameter is false.
     */
    protected void _assert(boolean flag, ASTPtRootNode node, String message) {
        if (!flag) {
            throw new InternalErrorException(message + ": " + node.toString());
        }
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     *  @param node The node.
     *  @exception IllegalActionException If thrown while
     *  generating a child.
     */
    protected void _generateAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numChildren; i++) {
            _generateChild(node, i);
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     *  @param node The node.
     *  @param i The index of the child to be visited.
     *  @exception IllegalActionException If thrown while visiting a child
     *  node.
     */
    protected void _generateChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        child.visit(this);
    }

    /** Get the local name for this this name.
     *  @param name The name to be looked up.
     *  @return The local name.
     *  @exception IllegalActionException  Always thrown in this base class.
     */
    protected String _getLocalNameForName(String name)
            throws IllegalActionException {
        //   if (_scope != null) {
        //             return "FIXME";
        //         }
        throw new IllegalActionException("The ID " + name + " is undefined.");
    }

    /** Return true if the name is a valid name.  This base class
     *  always returns false.
     *  @param name The name to be looked up.
     *  @return True if the name is valid.  This base class always returns
     *  false.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _isValidName(String name) throws IllegalActionException {
        //    if (_scope != null) {
        //             try {
        //                 return (_scope.getType(name) != null);
        //             } catch (Exception ex) {
        //                 return false;
        //             }
        //         } else {
        return false;

        //        }
    }

    /** A map from node to local node name. */
    protected HashMap _nodeToLocalName;

    /** The node number, used to create unique node names. */
    protected int _nodeNumber;
}
