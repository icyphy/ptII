/* A visitor for parse trees of the expression language that generates soot code.

 Copyright (c) 1998-2002 The Regents of the University of California
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.copernicus.java;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.expr.*;
import ptolemy.data.type.RecordType;
import ptolemy.copernicus.kernel.*;
import ptolemy.kernel.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import soot.*;
import soot.util.*;
import soot.jimple.*;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeCodeGenerator
/**
This class visits parse trees and generates soot instructions that evaluate the parse tree.

@author Steve Neuendorffer
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeCodeGenerator implements ParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Local generateCode(ASTPtRootNode node, JimpleBody body, 
            CodeGenerationScope scope) 
            throws IllegalActionException {
        ParseTreeTypeInference typeInference = new ParseTreeTypeInference();
        typeInference.inferTypes(node, scope);
        _scope = scope;
        _body = body;
        _nodeToLocal = new HashMap();
        _units = body.getUnits();
        node.visit(this);
        Local local = (Local)_nodeToLocal.get(node);
        _nodeToLocal = null;
        _units = null;
        _scope = null;
        return local;
    }

    public void visitArrayConstructNode(ASTPtArrayConstructNode node) 
            throws IllegalActionException {
        _generateAllChildren(node);
        
        Local local = _getChildTokensLocal(node);
  
        Local tokenLocal = Jimple.v().newLocal("token", 
                RefType.v(PtolemyUtilities.arrayTokenClass));
        _body.getLocals().add(tokenLocal);
        
        _units.add(Jimple.v().newAssignStmt(
             tokenLocal, Jimple.v().newNewExpr(
                     RefType.v(PtolemyUtilities.arrayTokenClass))));
        _units.add(Jimple.v().newInvokeStmt(
             Jimple.v().newSpecialInvokeExpr(tokenLocal,
                 PtolemyUtilities.arrayTokenConstructor, local)));
                   
       _nodeToLocal.put(node, tokenLocal);
    }

    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        Local resultLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
         
        // Make sure that exactly one of AND, OR, XOR is set.
        _assert(node.isBitwiseAnd() ^ node.isBitwiseOr() ^ node.isBitwiseXor(),
                node, "Invalid operation");

        Type bitwiseType = RefType.v("ptolemy.data.BitwiseOperationToken");
        Local tokenLocal = Jimple.v().newLocal("token", 
                bitwiseType);
        _body.getLocals().add(tokenLocal);
      
        _units.add(Jimple.v().newAssignStmt(
                           tokenLocal,
                           Jimple.v().newCastExpr(
                                   resultLocal,
                                   bitwiseType)));
            
        for (int i = 1; i < numChildren; i++ ) {
            Local nextLocal = (Local)_nodeToLocal.get(node.jjtGetChild(i));
       
            if(node.isBitwiseAnd()) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newInterfaceInvokeExpr(
                                        tokenLocal,
                                        PtolemyUtilities.tokenBitwiseAndMethod,
                                        nextLocal)));
            } else if(node.isBitwiseOr()) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newInterfaceInvokeExpr(
                                        tokenLocal,
                                        PtolemyUtilities.tokenBitwiseOrMethod,
                                        nextLocal)));
            } else {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newInterfaceInvokeExpr(
                                        tokenLocal,
                                        PtolemyUtilities.tokenBitwiseXorMethod,
                                        nextLocal)));
            } 
        }
        Local tokenCastLocal = Jimple.v().newLocal("token", 
                PtolemyUtilities.tokenType);
        _body.getLocals().add(tokenCastLocal);
        _units.add(
                Jimple.v().newAssignStmt(
                        tokenCastLocal,
                        Jimple.v().newCastExpr(
                                tokenLocal,
                                PtolemyUtilities.tokenType)));
        
        _nodeToLocal.put(node, tokenCastLocal);
    }
    public void visitFunctionNode(ASTPtFunctionNode node) 
            throws IllegalActionException {
        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.

        int argCount = node.jjtGetNumChildren();
        _generateAllChildren(node);

        if(_isValidName(node.getFunctionName())) {
            Local local = _getLocalForName(node.getFunctionName());
            Local resultLocal = Jimple.v().newLocal("token", 
                    RefType.v(PtolemyUtilities.tokenClass));
            _body.getLocals().add(resultLocal);
            if(argCount == 1) {
                // array..
                Local tokenCastLocal = Jimple.v().newLocal("indexToken", 
                        RefType.v(PtolemyUtilities.arrayTokenClass));
                _body.getLocals().add(tokenCastLocal);
                  
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenCastLocal,
                                Jimple.v().newCastExpr(
                                        local,
                                        RefType.v(PtolemyUtilities.arrayTokenClass))));

                Local indexTokenLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
                _units.add(
                        Jimple.v().newAssignStmt(
                                indexTokenLocal,
                                Jimple.v().newCastExpr(
                                        indexTokenLocal,
                                        RefType.v(PtolemyUtilities.intTokenClass))));

                Local indexLocal = Jimple.v().newLocal("index", 
                        IntType.v());
                _body.getLocals().add(indexLocal);
                _units.add(
                        Jimple.v().newAssignStmt(
                                indexLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        indexTokenLocal,
                                        PtolemyUtilities.intValueMethod)));
                             
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tokenCastLocal,
                                        PtolemyUtilities.arrayGetElementMethod,
                                        indexLocal)));
                                        
            } else if(argCount == 2) {
                // matrix..
                Local tokenCastLocal = Jimple.v().newLocal("indexToken", 
                        RefType.v(PtolemyUtilities.matrixTokenClass));
                _body.getLocals().add(tokenCastLocal);
                  
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenCastLocal,
                                Jimple.v().newCastExpr(
                                        local,
                                        RefType.v(PtolemyUtilities.matrixTokenClass))));

                Local rowIndexTokenLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
                Local columnIndexTokenLocal = (Local)_nodeToLocal.get(node.jjtGetChild(1));
                _units.add(
                        Jimple.v().newAssignStmt(
                                rowIndexTokenLocal,
                                Jimple.v().newCastExpr(
                                        rowIndexTokenLocal,
                                        RefType.v(PtolemyUtilities.intTokenClass))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                columnIndexTokenLocal,
                                Jimple.v().newCastExpr(
                                        columnIndexTokenLocal,
                                        RefType.v(PtolemyUtilities.intTokenClass))));

                Local rowIndexLocal = Jimple.v().newLocal("rowIndex", 
                        IntType.v());
                _body.getLocals().add(rowIndexLocal);
                 Local columnIndexLocal = Jimple.v().newLocal("columnIndex", 
                        IntType.v());
                _body.getLocals().add(columnIndexLocal);
                _units.add(
                        Jimple.v().newAssignStmt(
                                rowIndexLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        rowIndexTokenLocal,
                                        PtolemyUtilities.intValueMethod)));
                             
                 _units.add(
                        Jimple.v().newAssignStmt(
                                columnIndexLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        columnIndexTokenLocal,
                                        PtolemyUtilities.intValueMethod)));
                             
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tokenCastLocal,
                                        PtolemyUtilities.matrixGetElementAsTokenMethod,
                                        rowIndexLocal, columnIndexLocal)));
                         
            } else {
		throw new IllegalActionException("Wrong number of indices "
			+ "when referencing " + node.getFunctionName());
            }
            _nodeToLocal.put(node, resultLocal);
            return;
        }
        
        if (node.getFunctionName().compareTo("eval") == 0) {
            throw new IllegalActionException(
                    "unimplemented case");
        }
        
        if (node.getFunctionName().compareTo("matlab") == 0) {
            throw new IllegalActionException(
                    "unimplemented case");
        }

        // Otherwise, try to reflect the method name.
   
        // The array of token types that the method takes.
        ptolemy.data.type.Type[] argTypes =
            new ptolemy.data.type.Type[node.jjtGetNumChildren()];
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            argTypes[i] = ((ASTPtRootNode)node.jjtGetChild(i)).getType();
        }

        // Find the method...
        CachedMethod cachedMethod = 
            CachedMethod.findMethod(node.getFunctionName(),
                    argTypes, CachedMethod.FUNCTION);
        
        if(cachedMethod.isMissing()) {
            throw new IllegalActionException("Function " + cachedMethod +
                    " not found.");
        }

        if(cachedMethod instanceof CachedMethod.BaseConvertCachedMethod ||
                cachedMethod instanceof CachedMethod.ArrayMapCachedMethod ||
                cachedMethod instanceof CachedMethod.MatrixMapCachedMethod) {
            throw new IllegalActionException(
                    "CodeGeneration not supported for " + 
                    cachedMethod.getClass());
        }

        Method method = cachedMethod.getMethod();
      
        // Find the corresponding soot method.
        SootMethod sootMethod = SootUtilities.getSootMethodForMethod(method);

        // The list of locals that are arguments to the function.
        List args = new LinkedList();
     
        CachedMethod.ArgumentConversion[] conversions = 
            cachedMethod.getConversions();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Local tokenLocal = (Local)_nodeToLocal.get(node.jjtGetChild(i));

            // Insert the appropriate conversion.
            Local argLocal = _convertTokenArgToJavaArg(
                    tokenLocal, argTypes[i], conversions[i]);
            args.add(argLocal);
        }

        Type returnType = sootMethod.getReturnType();
        Local returnLocal = Jimple.v().newLocal("returnValue", 
                returnType);
        _body.getLocals().add(returnLocal);

        // Actually invoke the method.
        _units.add(
                Jimple.v().newAssignStmt(
                        returnLocal, 
                        Jimple.v().newStaticInvokeExpr(
                                sootMethod, args)));
      
        // Convert the result back to a token.
        Local tokenLocal = _convertJavaResultToToken(returnLocal, returnType);
        
     
        
        //       Jimple.v().newStaticInvokeExpr(
        //                                 methodCallEvaluationMethod,
        //                                 argList)));
      
        /*
   RefType objectType = RefType.v(PtolemyUtilities.objectClass);
        Local argValuesLocal = Jimple.v().newLocal("tokenArray", 
                ArrayType.v(objectType, 1));
        _body.getLocals().add(argValuesLocal);
        _units.add(
                Jimple.v().newAssignStmt(
                        argValuesLocal,
                        Jimple.v().newNewArrayExpr(
                                objectType,
                                IntConstant.v(node.jjtGetNumChildren()))));

        ptolemy.data.type.Type[] argTypes =
            new ptolemy.data.type.Type[node.jjtGetNumChildren()];
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            argTypes[i] = ((ASTPtRootNode)node.jjtGetChild(i)).getType();
            _units.add(
                    Jimple.v().newAssignStmt(
                            Jimple.v().newArrayRef(
                                    argValuesLocal,
                                    IntConstant.v(i)),
                            (Local)_nodeToLocal.get(node.jjtGetChild(i))));
        }

        RefType typeType = RefType.v("ptolemy.data.type.Type");
        Local argTypesLocal = Jimple.v().newLocal("tokenTypes", 
                ArrayType.v(typeType, 1));
        _body.getLocals().add(argTypesLocal);
        Local typeLocal = Jimple.v().newLocal("classType", 
                typeType);
        _body.getLocals().add(typeLocal);
        Local tokenLocal = Jimple.v().newLocal("token", 
                PtolemyUtilities.tokenType);
        _body.getLocals().add(tokenLocal);

        _units.add(
                Jimple.v().newAssignStmt(
                        argTypesLocal,
                        Jimple.v().newNewArrayExpr(
                                typeType,
                                IntConstant.v(argCount))));
        
        Local indexLocal = Jimple.v().newLocal("index", IntType.v());
        _body.getLocals().add(indexLocal);
        
        // The list of initializer instructions.
        List initializerList = new LinkedList();
        initializerList.add(
                Jimple.v().newAssignStmt(
                        indexLocal,
                        IntConstant.v(0)));
        
        // The list of body instructions.
        List bodyList = new LinkedList();
        bodyList.add(
                Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newArrayRef(
                                argValuesLocal,
                                indexLocal)));
        bodyList.add(
                Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newCastExpr(
                                tokenLocal,
                                PtolemyUtilities.tokenType)));
        bodyList.add(
                Jimple.v().newAssignStmt(
                        typeLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                tokenLocal,
                                PtolemyUtilities.tokenGetTypeMethod)));
        bodyList.add(
                Jimple.v().newAssignStmt(
                        Jimple.v().newArrayRef(
                                argTypesLocal,
                                indexLocal),
                        typeLocal));
                
                
        // Increment the index.
        bodyList.add(
                Jimple.v().newAssignStmt(
                        indexLocal,
                        Jimple.v().newAddExpr(
                                indexLocal,
                                IntConstant.v(1))));
        
        Expr conditionalExpr =
            Jimple.v().newLtExpr(
                    indexLocal,
                    IntConstant.v(argCount));
        
        Stmt stmt = Jimple.v().newNopStmt();
        _units.add(stmt);

        SootUtilities.createForLoopBefore(_body,
                stmt,
                initializerList,
                bodyList,
                conditionalExpr);
               
        SootMethod methodCallEvaluationMethod = 
            Scene.v().getMethod("<ptolemy.data.expr.ParseTreeEvaluator: ptolemy.data.Token functionCall(java.lang.String,int,ptolemy.data.type.Type[],java.lang.Object[])>");
        List argList = new ArrayList();
        argList.add(StringConstant.v(node.getFunctionName()));
        argList.add(IntConstant.v(argCount));
        argList.add(argTypesLocal);
        argList.add(argValuesLocal);

        _units.add(
                Jimple.v().newAssignStmt(
                        tokenLocal,
                        Jimple.v().newStaticInvokeExpr(
                                methodCallEvaluationMethod,
                                argList)));
                         
         */     
        _nodeToLocal.put(node, tokenLocal);
    }

    // Add code to the method being generated to convert the given
    // returnLocal, with the given returnType to a token type.  Return
    // the new token local.
    private Local _convertJavaResultToToken(
            Local returnLocal, Type returnType) 
            throws IllegalActionException {
        Local tokenLocal;

        if(returnType.equals(PtolemyUtilities.tokenType)) {
            tokenLocal = returnLocal;
        } else if(returnType.equals(
                          ArrayType.v(PtolemyUtilities.tokenType, 1))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.arrayTokenClass,
                    PtolemyUtilities.arrayTokenConstructor,
                    returnLocal);
        } else if(returnType.equals(DoubleType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.doubleTokenClass,
                    PtolemyUtilities.doubleTokenConstructor,
                    returnLocal);
        } else if(returnType.equals(LongType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.longTokenClass,
                    PtolemyUtilities.longTokenConstructor,
                    returnLocal);
        } else if(returnType.equals(RefType.v("java.lang.String"))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.stringTokenClass,
                    PtolemyUtilities.stringTokenConstructor,
                    returnLocal);
        } else if(returnType.equals(BooleanType.v())) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.booleanTokenClass,
                    PtolemyUtilities.booleanTokenConstructor,
                    returnLocal);
        } else if(returnType.equals(RefType.v("ptolemy.math.Complex"))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.complexTokenClass,
                    PtolemyUtilities.complexTokenConstructor,
                    returnLocal);
        } else if(returnType.equals(RefType.v("ptolemy.math.FixPoint"))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.fixTokenClass,
                    PtolemyUtilities.fixTokenConstructor,
                    returnLocal);
        } else if(returnType.equals(ArrayType.v(BooleanType.v(),2))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.booleanMatrixTokenClass,
                    PtolemyUtilities.booleanMatrixTokenConstructor,
                    returnLocal);
            
        } else if(returnType.equals(ArrayType.v(IntType.v(),2))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.intMatrixTokenClass,
                    PtolemyUtilities.intMatrixTokenConstructor,
                    returnLocal);
            
        } else if(returnType.equals(ArrayType.v(LongType.v(),2))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.doubleMatrixTokenClass,
                    PtolemyUtilities.doubleMatrixTokenConstructor,
                    returnLocal);
            
        } else if(returnType.equals(ArrayType.v(DoubleType.v(),2))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.doubleMatrixTokenClass,
                    PtolemyUtilities.doubleMatrixTokenConstructor,
                    returnLocal);
            
        } else if(returnType.equals(ArrayType.v(RefType.v("ptolemy.math.Complex"),2))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.complexMatrixTokenClass,
                    PtolemyUtilities.complexMatrixTokenConstructor,
                    returnLocal);
            
        } else if(returnType.equals(ArrayType.v(RefType.v("ptolemy.math.FixPoint"),2))) {
            tokenLocal = PtolemyUtilities.addTokenLocal(_body, "token",
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
        
        if(conversion == CachedMethod.IDENTITY) {
            Local tempLocal = Jimple.v().newLocal("arg" , 
                    PtolemyUtilities.tokenType);
            _body.getLocals().add(tempLocal);
            
            // Add the new local to the list of arguments
            _units.add(
                    Jimple.v().newAssignStmt(
                            tempLocal,
                            tokenLocal));
            return tempLocal;
        } else if(conversion == CachedMethod.ARRAYTOKEN) {
            Local tempLocal = Jimple.v().newLocal("arg" , 
                    RefType.v(PtolemyUtilities.arrayTokenClass));
            _body.getLocals().add(tempLocal);
            Local resultLocal = Jimple.v().newLocal("arg" , 
                    ArrayType.v(RefType.v(PtolemyUtilities.objectClass), 1));
            _body.getLocals().add(resultLocal);
            
            // Add the new local to the list of arguments
            _units.add(
                    Jimple.v().newAssignStmt(
                            tempLocal,
                            Jimple.v().newCastExpr(
                                    tokenLocal,
                                    RefType.v(PtolemyUtilities.arrayTokenClass))));
            _units.add(
                    Jimple.v().newAssignStmt(
                            resultLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    tempLocal,
                                    PtolemyUtilities.arrayValueMethod)));
            return resultLocal;
        } else if(conversion == CachedMethod.NATIVE) {
            if(tokenType == ptolemy.data.type.BaseType.DOUBLE) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.doubleTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        DoubleType.v());
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v(PtolemyUtilities.doubleTokenClass))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.doubleValueMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.UNSIGNED_BYTE) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.unsignedByteTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        ByteType.v());
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v(PtolemyUtilities.unsignedByteTokenClass))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.unsignedByteValueMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.INT) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.intTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        IntType.v());
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.IntToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.intValueMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.LONG) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.longTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        LongType.v());
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.LongToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.longValueMethod)));
                return resultLocal;
            }  else if(tokenType == ptolemy.data.type.BaseType.STRING) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.stringTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        RefType.v("java.lang.String"));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.StringToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.stringValueMethod)));
                return resultLocal;
            }  else if(tokenType == ptolemy.data.type.BaseType.BOOLEAN) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.booleanTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        BooleanType.v());
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.BooleanToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.booleanValueMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.COMPLEX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.complexTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        RefType.v("ptolemy.math.Complex"));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
                    
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.ComplexToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.complexValueMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.FIX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.intTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        RefType.v("ptolemy.math.FixPoint"));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.FixToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.fixValueMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.DOUBLE_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.doubleMatrixTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        ArrayType.v(DoubleType.v(),2));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.DoubleMatrixToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.doubleMatrixMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.INT_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.intMatrixTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        ArrayType.v(IntType.v(),2));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.IntMatrixToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.intMatrixMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.LONG_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.longMatrixTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        ArrayType.v(LongType.v(),2));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.LongMatrixToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.longMatrixMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.BOOLEAN_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.booleanMatrixTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        ArrayType.v(BooleanType.v(),2));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.BooleanMatrixToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.booleanMatrixMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.COMPLEX_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.complexMatrixTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        ArrayType.v(RefType.v("ptolemy.math.Complex"),2));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.ComplexMatrixToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                resultLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.complexMatrixMethod)));
                return resultLocal;
            } else if(tokenType == ptolemy.data.type.BaseType.FIX_MATRIX) {
                Local tempLocal = Jimple.v().newLocal("arg" , 
                        RefType.v(PtolemyUtilities.fixMatrixTokenClass));
                _body.getLocals().add(tempLocal);
                Local resultLocal = Jimple.v().newLocal("arg" , 
                        ArrayType.v(RefType.v("ptolemy.math.FixPoint"),2));
                _body.getLocals().add(resultLocal);
                // Add the new local to the list of arguments
           
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        RefType.v("ptolemy.data.FixMatrixToken"))));
                _units.add(
                        Jimple.v().newAssignStmt(
                                tempLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tempLocal,
                                        PtolemyUtilities.fixMatrixMethod)));
                return resultLocal;
            } else {// if(argTypes[i] instanceof ArrayType) {
                throw new IllegalActionException(
                        "CodeGeneration not supported for arrayType");
            }
        } else {
            throw new IllegalActionException(
                    "CodeGeneration not supported for argument " +
                    "conversion " + conversion);
        }
    }
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        // Note that we take care to have short-circuit evaluation here.
        _generateChild(node, 0);

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
        _units.add(
                Jimple.v().newAssignStmt(
                        booleanTokenLocal,
                        Jimple.v().newCastExpr(
                                conditionTokenLocal,
                                RefType.v(PtolemyUtilities.booleanTokenClass))));
        _units.add(
                Jimple.v().newAssignStmt(
                        flagLocal,
                        Jimple.v().newVirtualInvokeExpr(
                                booleanTokenLocal,
                                PtolemyUtilities.booleanValueMethod)));
        // If condition is true then skip to start of true branch.
        _units.add(
                Jimple.v().newIfStmt(
                        Jimple.v().newEqExpr(
                                flagLocal,
                                IntConstant.v(1)),
                        startTrue));
        
        // Otherwise, do the false branch,
         _generateChild(node, 2);
         // Assign the false result
         _units.add(
                 Jimple.v().newAssignStmt(
                         resultLocal,
                         (Local)_nodeToLocal.get(node.jjtGetChild(2))));
         // And continue on.
         _units.add(Jimple.v().newGotoStmt(endTrue));

         _units.add(startTrue);
         
        // Otherwise, do the true branch,
         _generateChild(node, 1);
         // Assign the true result
         _units.add(
                 Jimple.v().newAssignStmt(
                         resultLocal,
                         (Local)_nodeToLocal.get(node.jjtGetChild(1))));
         _units.add(endTrue);

         _nodeToLocal.put(node, resultLocal);
    }

    public void visitLeafNode(ASTPtLeafNode node) 
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            Stmt insertPoint = Jimple.v().newNopStmt();
            _units.add(insertPoint);
            Local local = PtolemyUtilities.buildConstantTokenLocal(_body, 
                    insertPoint, node.getToken(), "token");
            _nodeToLocal.put(node, local);
            return;
        }

        _nodeToLocal.put(node, _getLocalForName(node.getName()));
    }

    public void visitLogicalNode(ASTPtLogicalNode node) 
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
      
        // Note that we take care to have short-circuit evaluation here.
        Local conditionLocal = Jimple.v().newLocal("condition" , 
                BooleanType.v());
        _body.getLocals().add(conditionLocal);
        
        Local booleanTokenLocal = Jimple.v().newLocal("result" , 
                RefType.v(PtolemyUtilities.booleanTokenClass));
        _body.getLocals().add(booleanTokenLocal);
        Local flagLocal = Jimple.v().newLocal("result" , 
                BooleanType.v());
        _body.getLocals().add(flagLocal);

        Stmt failedStmt = Jimple.v().newNopStmt();
        Stmt satisfiedStmt = Jimple.v().newNopStmt();
        
        // Determine if we are doing AND or OR
        Constant conditionConstant =
            node.isLogicalAnd() ? IntConstant.v(1) : IntConstant.v(0);
        _units.add(
                Jimple.v().newAssignStmt(
                        conditionLocal,
                        conditionConstant));
        for(int i = 0; i < numChildren; i++) {
            _generateChild(node, i);
            Local childLocal = (Local)_nodeToLocal.get(node.jjtGetChild(i));
            // Check the condition
            _units.add(
                    Jimple.v().newAssignStmt(
                            booleanTokenLocal,
                            Jimple.v().newCastExpr(
                                    childLocal,
                                    RefType.v(PtolemyUtilities.booleanTokenClass))));
            _units.add(
                    Jimple.v().newAssignStmt(
                            flagLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    booleanTokenLocal,
                                    PtolemyUtilities.booleanValueMethod)));
            // If condition is true then skip to start of true branch.
            _units.add(
                    Jimple.v().newIfStmt(
                        Jimple.v().newNeExpr(
                                flagLocal,
                                conditionConstant),
                        failedStmt));
        }
        // If we fall through, then must be satisfied.
        _units.add(
                Jimple.v().newGotoStmt(
                        satisfiedStmt));
        _units.add(failedStmt);
        Constant notConditionConstant =
            node.isLogicalAnd() ? IntConstant.v(0) : IntConstant.v(1);
        _units.add(
                Jimple.v().newAssignStmt(
                        conditionLocal,
                        notConditionConstant));
         
        _units.add(satisfiedStmt);

        // Take the result and turn it back into a BooleanToken
        Local resultLocal = PtolemyUtilities.addTokenLocal(_body, "token",
                    PtolemyUtilities.booleanTokenClass,
                    PtolemyUtilities.booleanTokenConstructor,
                    conditionLocal);


        _nodeToLocal.put(node, resultLocal);
    }

    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node) 
            throws IllegalActionException {
        _generateAllChildren(node);

        Local resultLocal = Jimple.v().newLocal("tokenResult", 
                PtolemyUtilities.tokenType);
        _body.getLocals().add(resultLocal);

        if (node.getForm() == 1) {
            Local local = _getChildTokensLocal(node);
            List args = new LinkedList();
            args.add(local);
            args.add(IntConstant.v(node.getRowCount()));
            args.add(IntConstant.v(node.getColumnCount()));
            _units.add(
                    Jimple.v().newAssignStmt(
                            resultLocal,
                            Jimple.v().newStaticInvokeExpr(
                                    PtolemyUtilities.matrixTokenCreateMethod,
                                    args)));
        } else {
            throw new IllegalActionException(
                    "unimplemented case");
        }
        _nodeToLocal.put(node, resultLocal);
    }
    public void visitMethodCallNode(ASTPtMethodCallNode node) 
            throws IllegalActionException {
        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.

        int argCount = node.jjtGetNumChildren();
        _generateAllChildren(node);
        // The first child is the token on which to invoke the method.

        // Handle indexing into a record.
        ptolemy.data.type.Type baseTokenType = 
            ((ASTPtRootNode)node.jjtGetChild(0)).getType();
        if(argCount == 1 &&
                baseTokenType instanceof RecordType) {
            RecordType type = (RecordType)baseTokenType;
            if(type.labelSet().contains(node.getMethodName())) {
                Local originalBaseLocal = (Local)
                    _nodeToLocal.get(node.jjtGetChild(0));
                Local baseLocal = Jimple.v().newLocal("base", 
                        RefType.v(PtolemyUtilities.recordTokenClass));
                _body.getLocals().add(baseLocal);
                // Cast the record.
                _units.add(
                        Jimple.v().newAssignStmt(
                                baseLocal, 
                                Jimple.v().newCastExpr(
                                        originalBaseLocal,
                                        RefType.v(PtolemyUtilities.recordTokenClass))));

                // invoke get()
                Local returnLocal = Jimple.v().newLocal("returnValue", 
                        RefType.v(PtolemyUtilities.tokenClass));
                _body.getLocals().add(returnLocal);
                _units.add(
                        Jimple.v().newAssignStmt(
                                returnLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        baseLocal,
                                        PtolemyUtilities.recordGetMethod,
                                        StringConstant.v(node.getMethodName()))));
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
        
        if(cachedMethod.isMissing()) {
            throw new IllegalActionException("Function " + cachedMethod +
                    " not found.");
        }

        if(cachedMethod instanceof CachedMethod.ArrayMapCachedMethod ||
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
           
        if(cachedMethod instanceof CachedMethod.BaseConvertCachedMethod) {
            RefType tempBaseType = PtolemyUtilities.getSootTypeForTokenType(
                    argTypes[0]);
            Local tempBaseLocal = _convertTokenArgToJavaArg(
                    originalBaseLocal, argTypes[0],
                    ((CachedMethod.BaseConvertCachedMethod)
                            cachedMethod).getBaseConversion());
            _units.add(
                    Jimple.v().newAssignStmt(
                            baseLocal, 
                            Jimple.v().newCastExpr(
                                    tempBaseLocal,
                                    baseType)));
        } else {
            _units.add(
                    Jimple.v().newAssignStmt(
                            baseLocal, 
                            Jimple.v().newCastExpr(
                                    originalBaseLocal,
                                    baseType)));
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
        _units.add(
                Jimple.v().newAssignStmt(
                        returnLocal, 
                        Jimple.v().newVirtualInvokeExpr(
                                baseLocal, sootMethod, args)));
      
        // Convert the result back to a token.
        Local tokenLocal = _convertJavaResultToToken(returnLocal, returnType);
        
     
        
   //      RefType objectType = RefType.v(PtolemyUtilities.objectClass);
//         Local argValuesLocal = Jimple.v().newLocal("tokenArray", 
//                 ArrayType.v(objectType, 1));
//         _body.getLocals().add(argValuesLocal);
//         _units.add(
//                 Jimple.v().newAssignStmt(
//                         argValuesLocal,
//                         Jimple.v().newNewArrayExpr(
//                                 objectType,
//                                 IntConstant.v(node.jjtGetNumChildren()))));

//         for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//             _units.add(
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

//         _units.add(
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
//         _units.add(stmt);

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

//         _units.add(
//                 Jimple.v().newAssignStmt(
//                         tokenLocal,
//                         Jimple.v().newStaticInvokeExpr(
//                                 methodCallEvaluationMethod,
//                                 argList)));
                              
        _nodeToLocal.put(node, tokenLocal);
    }
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        
        _generateAllChildren(node);
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
       
        Local resultLocal = Jimple.v().newLocal("result", 
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(resultLocal);
        
        Local tokenLocal = Jimple.v().newLocal("token", 
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(tokenLocal);
        
        Local timesLocal = Jimple.v().newLocal("times", 
                IntType.v());
        _body.getLocals().add(timesLocal);
        
        _units.add(
                Jimple.v().newAssignStmt(
                        resultLocal,
                        (Local)_nodeToLocal.get(node.jjtGetChild(0))));
        
        for(int i = 1; i < numChildren; i++) {
            _units.add(
                    Jimple.v().newAssignStmt(
                            tokenLocal,
                            (Local)_nodeToLocal.get(node.jjtGetChild(i))));
            _units.add(
                    Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newCastExpr(
                                    tokenLocal,
                                    RefType.v(PtolemyUtilities.scalarTokenClass))));
            _units.add(
                    Jimple.v().newAssignStmt(
                            timesLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    tokenLocal,
                                    PtolemyUtilities.tokenIntValueMethod)));
            
            _units.add(
                    Jimple.v().newAssignStmt(
                            resultLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    resultLocal,
                                    PtolemyUtilities.tokenPowMethod,
                                    timesLocal)));
        }
        _nodeToLocal.put(node, resultLocal);
    }
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {

        _generateAllChildren(node);

        Local tokenLocal = Jimple.v().newLocal("token", 
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(tokenLocal);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is " +
                "not equal to number of operators plus one");
    
        Local resultLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
        for(int i = 1; i < numChildren; i++) {
            Token operator = (Token)lexicalTokenList.get(i - 1);
            Local nextLocal = (Local)_nodeToLocal.get(node.jjtGetChild(i));
            if(operator.kind == PtParserConstants.MULTIPLY) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        resultLocal,
                                        PtolemyUtilities.tokenMultiplyMethod,
                                        nextLocal)));
                resultLocal = tokenLocal;
            } else if(operator.kind == PtParserConstants.DIVIDE) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        resultLocal,
                                        PtolemyUtilities.tokenDivideMethod,
                                        nextLocal)));
                resultLocal = tokenLocal;
            } else if(operator.kind == PtParserConstants.MODULO) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        resultLocal,
                                        PtolemyUtilities.tokenModuloMethod,
                                        nextLocal)));
                resultLocal = tokenLocal;
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        _nodeToLocal.put(node, resultLocal);
    }
    public void visitRecordConstructNode(ASTPtRecordConstructNode node) 
            throws IllegalActionException {
        _generateAllChildren(node);
        
        int numChildren = node.jjtGetNumChildren();
        
        _assert(node.getFieldNames().size() == numChildren,
                node, "The number of labels and values does not " +
                "match in parsing a record expression.");
        String[] labels = (String[]) node.getFieldNames().toArray(
                new String[numChildren]);
        RefType stringType = RefType.v("java.lang.String");
        
        Local labelsLocal = Jimple.v().newLocal("labelArray", 
                ArrayType.v(stringType, 1));
        _body.getLocals().add(labelsLocal);
        _units.add(
                Jimple.v().newAssignStmt(
                        labelsLocal,
                        Jimple.v().newNewArrayExpr(
                                stringType,
                                IntConstant.v(labels.length))));

        for (int i = 0; i < labels.length; i++) {
            _units.add(
                    Jimple.v().newAssignStmt(
                            Jimple.v().newArrayRef(
                                    labelsLocal,
                                    IntConstant.v(i)),
                            StringConstant.v(labels[i])));
        }
        
        
        Local valuesLocal = _getChildTokensLocal(node);
        
        Local tokenLocal = Jimple.v().newLocal("token", 
                RefType.v(PtolemyUtilities.recordTokenClass));
        _body.getLocals().add(tokenLocal);
        
        _units.add(Jimple.v().newAssignStmt(
             tokenLocal, Jimple.v().newNewExpr(
                     RefType.v(PtolemyUtilities.recordTokenClass))));
        _units.add(Jimple.v().newInvokeStmt(
             Jimple.v().newSpecialInvokeExpr(tokenLocal,
                 PtolemyUtilities.recordTokenConstructor, 
                     labelsLocal, valuesLocal)));
                   
       _nodeToLocal.put(node, tokenLocal);
     }
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        Token operator = (Token)node.getOperator();
        Local leftLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
        Local rightLocal = (Local)_nodeToLocal.get(node.jjtGetChild(1));
        System.out.println("left = " + leftLocal.hashCode());
        System.out.println("right = " + rightLocal.hashCode());


        Type scalarType = RefType.v("ptolemy.data.ScalarToken");
        Local tokenLocal = Jimple.v().newLocal("token", 
                PtolemyUtilities.tokenType);
        _body.getLocals().add(tokenLocal);
        Local leftScalarLocal = Jimple.v().newLocal("leftScalar", 
                scalarType);
        _body.getLocals().add(leftScalarLocal);
        Local rightScalarLocal = Jimple.v().newLocal("rightScalar", 
                    scalarType);
        _body.getLocals().add(rightScalarLocal);
        
        if(operator.kind == PtParserConstants.EQUALS) {
            _units.add(
                    Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    leftLocal,
                                    PtolemyUtilities.tokenEqualsMethod,
                                    rightLocal)));
        } else if(operator.kind == PtParserConstants.NOTEQUALS) {
            _units.add(
                    Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    leftLocal,
                                    PtolemyUtilities.tokenEqualsMethod,
                                    rightLocal)));
            _units.add(
                    Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    tokenLocal,
                                    PtolemyUtilities.tokenNotMethod)));
        } else {
            _units.add(Jimple.v().newAssignStmt(
                               leftScalarLocal,
                               Jimple.v().newCastExpr(
                                      leftLocal,
                                      scalarType)));
            _units.add(Jimple.v().newAssignStmt(
                               rightScalarLocal,
                               Jimple.v().newCastExpr(
                                       rightLocal,
                                       scalarType)));
            
            if(operator.kind == PtParserConstants.GTE) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        leftScalarLocal,
                                        PtolemyUtilities.tokenIsLessThanMethod,
                                        rightScalarLocal)));
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tokenLocal,
                                        PtolemyUtilities.tokenNotMethod)));
            } else if(operator.kind == PtParserConstants.GT) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        rightScalarLocal,
                                        PtolemyUtilities.tokenIsLessThanMethod,
                                        leftScalarLocal)));
            } else if(operator.kind == PtParserConstants.LTE) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        rightScalarLocal,
                                        PtolemyUtilities.tokenIsLessThanMethod,
                                        leftScalarLocal)));
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        tokenLocal,
                                        PtolemyUtilities.tokenNotMethod)));
            } else if(operator.kind == PtParserConstants.LT) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        leftScalarLocal,
                                        PtolemyUtilities.tokenIsLessThanMethod,
                                        rightScalarLocal)));
            } else {
                throw new IllegalActionException(
                        "Invalid operation " + operator.image);
            }
        }
       _nodeToLocal.put(node, tokenLocal);
    }
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _generateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        Type scalarType = RefType.v("ptolemy.data.ScalarToken");
        Token operator = (Token)node.getOperator();
        Local tokenLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
        Local bitsLocal = (Local)_nodeToLocal.get(node.jjtGetChild(1));
        Local resultLocal = Jimple.v().newLocal("tokenResult", 
                PtolemyUtilities.tokenType);
        _body.getLocals().add(resultLocal);
         Local scalarLocal = Jimple.v().newLocal("scalar", 
                scalarType);
        _body.getLocals().add(scalarLocal);
        Local intLocal = Jimple.v().newLocal("bits", 
                IntType.v());
        _body.getLocals().add(intLocal);

        _units.add(Jimple.v().newAssignStmt(
                           scalarLocal,
                           Jimple.v().newCastExpr(
                                   bitsLocal,
                                   scalarType)));
        _units.add(Jimple.v().newAssignStmt(
                           intLocal,
                           Jimple.v().newVirtualInvokeExpr(
                                   scalarLocal,
                                   PtolemyUtilities.tokenIntValueMethod)));
        _units.add(Jimple.v().newAssignStmt(
                           scalarLocal,
                           Jimple.v().newCastExpr(
                                   tokenLocal,
                                   scalarType)));
        if(operator.kind == PtParserConstants.SHL) {
            _units.add(Jimple.v().newAssignStmt(
                               resultLocal,
                               Jimple.v().newVirtualInvokeExpr(
                                       scalarLocal,
                                       PtolemyUtilities.tokenLeftShiftMethod,
                                       intLocal)));
        } else if(operator.kind == PtParserConstants.SHR) {
            _units.add(Jimple.v().newAssignStmt(
                               resultLocal,
                               Jimple.v().newVirtualInvokeExpr(
                                       scalarLocal,
                                       PtolemyUtilities.tokenRightShiftMethod,
                                       intLocal)));
        } else if(operator.kind == PtParserConstants.LSHR) {
            _units.add(Jimple.v().newAssignStmt(
                               resultLocal,
                               Jimple.v().newVirtualInvokeExpr(
                                       scalarLocal,
                                       PtolemyUtilities.tokenLogicalRightShiftMethod,
                                       intLocal)));
        } else {
            _assert(false, node, "Invalid operation");
        }
       _nodeToLocal.put(node, resultLocal);    
    }
    public void visitSumNode(ASTPtSumNode node) 
            throws IllegalActionException {
        _generateAllChildren(node);

        Local tokenLocal = Jimple.v().newLocal("token", 
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(tokenLocal);

        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is " +
                "not equal to number of operators plus one");
    
        Local resultLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));
        for(int i = 1; i < numChildren; i++) {
            Token operator = (Token)lexicalTokenList.get(i - 1);
            Local nextLocal = (Local)_nodeToLocal.get(node.jjtGetChild(i));
            if(operator.kind == PtParserConstants.PLUS) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        resultLocal,
                                        PtolemyUtilities.tokenAddMethod,
                                        nextLocal)));
                resultLocal = tokenLocal;
            } else if(operator.kind == PtParserConstants.MINUS) {
                _units.add(
                        Jimple.v().newAssignStmt(
                                tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        resultLocal,
                                        PtolemyUtilities.tokenSubtractMethod,
                                        nextLocal)));
                resultLocal = tokenLocal;
            } else {
                _assert(false, node, "Invalid operation");
            }
        }

        _nodeToLocal.put(node, resultLocal);
    }
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _generateAllChildren(node);
        _assert(node.jjtGetNumChildren() == 1, node,
                "Unary node must have exactly one child!");

        Local resultLocal = Jimple.v().newLocal("token", 
                RefType.v(PtolemyUtilities.tokenClass));
        _body.getLocals().add(resultLocal);

        Local tokenLocal = (Local)_nodeToLocal.get(node.jjtGetChild(0));

        if(node.isMinus()) {
            Local zeroLocal = Jimple.v().newLocal("token", 
                    RefType.v(PtolemyUtilities.tokenClass));
            _body.getLocals().add(zeroLocal);

            _units.add(Jimple.v().newAssignStmt(
                               zeroLocal,
                               Jimple.v().newVirtualInvokeExpr(
                                       tokenLocal,
                                       PtolemyUtilities.tokenZeroMethod)));
            _units.add(Jimple.v().newAssignStmt(
                               resultLocal,
                               Jimple.v().newVirtualInvokeExpr(
                                       zeroLocal,
                                       PtolemyUtilities.tokenSubtractMethod,
                                       tokenLocal)));
        } else if(node.isNot()) {
            Type booleanType = RefType.v("ptolemy.data.BooleanToken");
            Local booleanLocal = Jimple.v().newLocal("token", 
                    booleanType);
            _body.getLocals().add(booleanLocal);
            _units.add(Jimple.v().newAssignStmt(
                               booleanLocal,
                               Jimple.v().newCastExpr(
                                       tokenLocal,
                                       booleanType)));
          
            _units.add(Jimple.v().newAssignStmt(
                               booleanLocal,
                               Jimple.v().newInterfaceInvokeExpr(
                                       booleanLocal,
                                       PtolemyUtilities.tokenBitwiseNotMethod)));
          
             _units.add(Jimple.v().newAssignStmt(
                               resultLocal,
                               Jimple.v().newCastExpr(
                                       booleanLocal,
                                       PtolemyUtilities.tokenType)));
        } else if(node.isBitwiseNot()) {
            Type bitwiseType = RefType.v("ptolemy.data.BitwiseOperationToken");
            Local bitwiseLocal = Jimple.v().newLocal("token", 
                    bitwiseType);
            _body.getLocals().add(bitwiseLocal);
            _units.add(Jimple.v().newAssignStmt(
                               bitwiseLocal,
                               Jimple.v().newCastExpr(
                                       tokenLocal,
                                       bitwiseType)));
          
            _units.add(Jimple.v().newAssignStmt(
                               bitwiseLocal,
                               Jimple.v().newInterfaceInvokeExpr(
                                       bitwiseLocal,
                                       PtolemyUtilities.tokenBitwiseNotMethod)));
          
             _units.add(Jimple.v().newAssignStmt(
                               resultLocal,
                               Jimple.v().newCastExpr(
                                       bitwiseLocal,
                                       PtolemyUtilities.tokenType)));
          
        } else {
            _assert(false, node, "Unrecognized unary node");
        }
        _nodeToLocal.put(node, resultLocal);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /**
     * Assert that the given boolean value, which describes the given
     * parse tree node is true.  If it is false, then throw a new
     * InternalErrorException that describes the node that includes
     * the given message.
     */
    protected void _assert(boolean flag, ASTPtRootNode node, String message) {
        if(!flag) {
            throw new InternalErrorException(message + ": " + node.toString());
        }
    }    

    /** Loop through all of the children of this node, 
     *  visiting each one of them, which will cause their token
     *  value to be determined.
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
     */
    protected void _generateChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
        child.visit(this);
    }
    
    protected Local _getLocalForName(String name)
            throws IllegalActionException {
        if(_scope != null) {
            Local local = _scope.getLocal(name); 
            if(local != null) {
                return local;
            }
        }          
        throw new IllegalActionException(
                "The ID " + name + " is undefined.");
    }

    protected boolean _isValidName(String name)
            throws IllegalActionException {
        if(_scope != null) {
            try {
                return (_scope.getType(name) != null);
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /** Create a new local that references an array of tokens.  The
     * array will have the size of the number of children of the given
     * node.  Add code to the body to initialize and populate the
     * array.
     */
    protected Local _getChildTokensLocal(ASTPtRootNode node) {
        Local arrayLocal = Jimple.v().newLocal("tokenArray", 
                ArrayType.v(PtolemyUtilities.tokenType, 1));
        _body.getLocals().add(arrayLocal);
        _units.add(
                Jimple.v().newAssignStmt(
                        arrayLocal,
                        Jimple.v().newNewArrayExpr(
                                PtolemyUtilities.tokenType,
                                IntConstant.v(node.jjtGetNumChildren()))));

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            _units.add(
                    Jimple.v().newAssignStmt(
                            Jimple.v().newArrayRef(
                                    arrayLocal,
                                    IntConstant.v(i)),
                            (Local)_nodeToLocal.get(node.jjtGetChild(i))));
        }
        return arrayLocal;
    }


    protected HashMap _nodeToLocal;
    protected JimpleBody _body;
    protected Chain _units;
    protected CodeGenerationScope _scope;
}
