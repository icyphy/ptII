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

    public Local generateCode(ASTPtRootNode node, JimpleBody body) 
            throws IllegalActionException {
        _body = body;
        _nodeToLocal = new HashMap();
        _units = body.getUnits();
        node.visit(this);
        Local local = (Local)_nodeToLocal.get(node);
        _nodeToLocal = null;
        _units = null;
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
       throw new IllegalActionException(
                "unimplemented case");
    }
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
       throw new IllegalActionException(
                "unimplemented case");
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
       throw new IllegalActionException(
                "unimplemented case");
    }
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node) 
            throws IllegalActionException {
       throw new IllegalActionException(
                "unimplemented case");
    }
    public void visitMethodCallNode(ASTPtMethodCallNode node) 
            throws IllegalActionException {
       throw new IllegalActionException(
                "unimplemented case");
    }
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
       throw new IllegalActionException(
                "unimplemented case");
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
        throw new IllegalActionException(
                "The ID " + name + " is undefined.");
 
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
}
