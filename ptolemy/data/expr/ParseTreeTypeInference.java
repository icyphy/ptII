/* A visitor for parse trees of the expression language that infers types.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeTypeInference
/**
This class visits parse trees and infers a type for each node in the
parse tree.  This type is stored in the parse tree.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeTypeInference extends AbstractParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Infer the type of the parse tree with the specified root node.
     *  @param node The root of the parse tree.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public Type inferTypes(ASTPtRootNode node)
            throws IllegalActionException {
        node.visit(this);
        return node.getType();
    }

    /** Infer the type of the parse tree with the specified root node using
     *  the specified scope to resolve the values of variables.
     *  @param node The root of the parse tree.
     *  @param scope The scope for evaluation.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    public Type inferTypes(ASTPtRootNode node, ParserScope scope)
            throws IllegalActionException {
        _scope = scope;
        node.visit(this);
        _scope = null;
        return node.getType();
    }

    /** Set the type of the given node to be an ArrayType that is the
     *  least upper bound of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);

        Type[] childTypes = _getChildTypes(node);

        _setType(node, new ArrayType((Type)
                TypeLattice.lattice().leastUpperBound(childTypes)));
    }

    /** Set the type of the given node to be the type that is the
     *  least upper bound of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _visitAllChildren(node);

        Type[] childTypes = _getChildTypes(node);

        // FIXME: not consistent with expression evaluator.
        _setType(node,
                (Type)TypeLattice.lattice().leastUpperBound(childTypes));
    }

    /** Set the type of the given node to be a function type whose
     *  argument types are determined by the children of the node.
     *  The return type of the function type is determined by
     *  inferring the type of function's expression in a scope that
     *  adds identifiers for each argument to the current scope.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        final Map map = new HashMap();
        for (int i = 0; i < node._argTypes.length; i++) {
            map.put(node.getArgumentNameList().get(i),
                    node.getArgumentTypes()[i]);
        }
        // Push the current scope.
        final ParserScope currentScope = _scope;
        ParserScope functionScope = new ParserScope() {
                public ptolemy.data.Token get(String name) {
                    return null;
                }
                public Type getType(String name)
                        throws IllegalActionException {
                    Type type = (Type)map.get(name);
                    if (type == null && currentScope != null) {
                        return currentScope.getType(name);
                    } else {
                        return type;
                    }
                }
                public NamedList variableList() {
                    return null;
                }
            };
        _scope = functionScope;
        node.getExpressionTree().visit(this);
        Type returnType = node.getExpressionTree().getType();
        FunctionType type = new FunctionType(node._argTypes, returnType);
        _setType(node, type);
        _scope = currentScope;
        return;
    }

    /** Set the type of the given node to be the return type of the
     *  function determined for the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitFunctionNode(ASTPtFunctionNode node)
            throws IllegalActionException {
        int argCount = node.jjtGetNumChildren() - 1;

        _visitAllChildren(node);

        // Get the child types.
        Type[] childTypes = new Type[argCount];
        for (int i = 0; i < argCount; i++) {
            childTypes[i] =
                ((ASTPtRootNode) node.jjtGetChild(i + 1)).getType();
            if (childTypes[i] == null) {
                throw new RuntimeException("node " +
                        node + " has null type.");
            }
        }

        Type baseType = ((ASTPtRootNode) node.jjtGetChild(0)).getType();

        // Handle as an array or matrix index into a named
        // variable reference.
        if (baseType instanceof FunctionType) {
            FunctionType functionType = (FunctionType)baseType;
            _setType(node, ((FunctionType)baseType).getReturnType());
            return;
        } else if (argCount == 1) {
            if (baseType instanceof ArrayType) {
                _setType(node, ((ArrayType)baseType).getElementType());
                return;            } else {
                    _assert(true, node, "Cannot use array "
                            + "indexing on '" + node.getFunctionName()
                            + "' because it does not have an array type.");
                }
        } else if (argCount == 2) {
            if (baseType instanceof UnsizedMatrixType) {
                _setType(node, ((UnsizedMatrixType) baseType).getElementType());
                return;
            } else {
                _assert(true, node, "Cannot use matrix "
                        + "indexing on '" + node.getFunctionName()
                        + "' because it does not have a matrix type.");
            }
        }

        String functionName = node.getFunctionName();
        if (functionName == null) {
            throw new IllegalActionException("Wrong number of indices "
                    + "when referencing " + node.getFunctionName());
        }

        // Psuedo-temporary hack for casts....
        if (functionName.compareTo("cast") == 0 && argCount == 2) {
            ASTPtRootNode castTypeNode =
                ((ASTPtRootNode)node.jjtGetChild(0 + 1));
            ParseTreeEvaluator parseTreeEvaluator = new ParseTreeEvaluator();
            try {
                ptolemy.data.Token t = 
                    parseTreeEvaluator.evaluateParseTree(castTypeNode, _scope);
                _setType(node, t.getType());
            } catch (IllegalActionException ex) {
                _setType(node, castTypeNode.getType());
            }
            return;

            // Note: We used to just do this, but in some case is it
            // useful to have functions which are type constructors...
            // Hence the above code.
            //  _setType(node,
            //     ((ASTPtRootNode) node.jjtGetChild(0 + 1)).getType());
            //  return;
        }

        if (functionName.compareTo("eval") == 0) {
            // We can't infer the type of eval expressions...
            _setType(node, BaseType.GENERAL);
            return;
        }

        if (functionName.compareTo("matlab") == 0) {
            // We can't infer the type of eval expressions...
            _setType(node, BaseType.GENERAL);
            return;
        }

        // Otherwise, try to reflect the method name.
        CachedMethod cachedMethod =
            CachedMethod.findMethod(functionName,
                    childTypes, CachedMethod.FUNCTION);

        if (cachedMethod.isValid()) {
            Type type = cachedMethod.getReturnType();
            _setType(node, type);
        } else {
            // If we reach this point it means the function was not found on
            // the search path.
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < childTypes.length; i++) {
                if (i == 0) {
                    buffer.append(childTypes[i].toString());
                } else {
                    buffer.append(", " + childTypes[i].toString());
                }
            }
            throw new IllegalActionException("No matching function " +
                    node.getFunctionName() + "( " + buffer + " ).");
        }
    }

    /** Set the type of the given node to be the least upper bound of
     *  the types of the two branches of the if.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _visitAllChildren(node);

        Type conditionalType = ((ASTPtRootNode)node.jjtGetChild(0)).getType();
        if (conditionalType != BaseType.BOOLEAN) {
            throw new IllegalActionException(
                    "Functional-if must branch on a boolean, but instead type was "
                    + conditionalType);
        }

        Type trueType = ((ASTPtRootNode)node.jjtGetChild(1)).getType();
        Type falseType = ((ASTPtRootNode)node.jjtGetChild(2)).getType();

        _setType(node,
                (Type)TypeLattice.lattice().leastUpperBound(
                        trueType, falseType));
    }

    /** Set the type of the given node to be the type of constant the
     *  variable refers to, if the node represents a constant, or the
     *  type of the identifier the node refers to in the current
     *  scope.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error
     *  occurs, or an identifier is not bound in the current scope.
     */
    public void visitLeafNode(ASTPtLeafNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _setType(node,
                    node.getToken().getType());
            return;
        }

        _setType(node, _getTypeForName(node.getName()));
    }

    /** Set the type of the given node to be boolean.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
        _setType(node, BaseType.BOOLEAN);
    }

    /** Set the type of the given node to be an MatrixType based on the
     *  least upper bound of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);

        Type[] childTypes = _getChildTypes(node);

        Type elementType = (Type)
            TypeLattice.lattice().leastUpperBound(childTypes);

        Type matrixType
            = UnsizedMatrixType.getMatrixTypeForElementType(elementType);
        _setType(node, matrixType);
    }

    /** Set the type of the given node to be the return type of the
     *  method determined for the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
        // Otherwise, try to reflect the method name.
        Type[] childTypes = _getChildTypes(node);

        // Handle indexing into a record.
        if (childTypes.length == 1 &&
                childTypes[0] instanceof RecordType) {
            RecordType type = (RecordType)childTypes[0];
            if (type.labelSet().contains(node.getMethodName())) {
                _setType(node, type.get(node.getMethodName()));
                return;
            }
        }

        CachedMethod cachedMethod =
            CachedMethod.findMethod(node.getMethodName(),
                    childTypes, CachedMethod.METHOD);

        if (cachedMethod.isValid()) {
            Type type = cachedMethod.getReturnType();
            _setType(node, type);
        } else {
            // If we reach this point it means the function was not found on
            // the search path.
            StringBuffer buffer = new StringBuffer();
            for (int i = 1; i < childTypes.length; i++) {
                if (i == 1) {
                    buffer.append(childTypes[i].toString());
                } else {
                    buffer.append(", " + childTypes[i].toString());
                }
            }
            throw new IllegalActionException("No matching method " +
                    childTypes[0].toString() + "." +
                    node.getMethodName() + "( " + buffer + " ).");
        }
    }

    /** Set the type of the given node to be the type of the first
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _visitAllChildren(node);

        Type baseType = ((ASTPtRootNode)node.jjtGetChild(0)).getType();
        _setType(node, baseType);
    }

    /** Set the type of the given node to be the least upper bound
     *  type of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {

        _visitAllChildren(node);

        Type[] childTypes = _getChildTypes(node);

        _setType(node,
                (Type)TypeLattice.lattice().leastUpperBound(childTypes));
    }

    /** Set the type of the given node to be a record token that
     *  contains fields for each name in the record construnction,
     *  where the type of each field in the record is determined by
     *  the corresponding type of the child nodes.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);

        Type[] childTypes = _getChildTypes(node);
        String[] names = (String[])node.getFieldNames().toArray(
                new String[node.jjtGetNumChildren()]);

        _setType(node,
                new RecordType(names, childTypes));

    }

    /** Set the type of the given node to be boolean.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
        _setType(node, BaseType.BOOLEAN);
    }

    /** Set the type of the given node to be the type of the first
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
        Type baseType = ((ASTPtRootNode) node.jjtGetChild(0)).getType();
        _setType(node, baseType);
    }

    /** Set the type of the given node to be the least upper bound
     *  type of the types of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitSumNode(ASTPtSumNode node)
            throws IllegalActionException {
        _visitAllChildren(node);

        Type[] childTypes = _getChildTypes(node);

        _setType(node,
                (Type)TypeLattice.lattice().leastUpperBound(childTypes));
    }

    /** Set the type of the given node to be the type of the
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
        Type baseType = ((ASTPtRootNode) node.jjtGetChild(0)).getType();
        _setType(node, baseType);
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
        if (!flag) {
            throw new InternalErrorException(message + ": " + node.toString());
        }
    }

    /** Return a new array that contains the types of the children of
     *  the given node.
     */
    protected Type[] _getChildTypes(ASTPtRootNode node) {
        Type[] types = new Type[node.jjtGetNumChildren()];
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            types[i] =  ((ASTPtRootNode) node.jjtGetChild(i)).getType();
            if (types[i] == null) {
                throw new RuntimeException("node " + node
                        + " has null child.");
            }
        }
        return types;
    }

    /** Return the type of the identifier with the given name.
     *  @exception IllegalActionException If the identifier is undefined.
     */
    protected Type _getTypeForName(String name)
            throws IllegalActionException {
        if (_scope != null) {
            Type type = _scope.getType(name);
            if (type != null) {
                return type;
            }
        }

        // Look up for constants.
        if (Constants.get(name) != null) {
            // A named constant that is recognized by the parser.
            return Constants.get(name).getType();
        }

        return BaseType.GENERAL;
    }

    /** Test if the given identifier is valid.
     */
    protected boolean _isValidName(String name)
            throws IllegalActionException {
        if (_scope != null) {
            try {
                return (_scope.getType(name) != null);
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    protected void _setType(ASTPtRootNode node, Type type) {
        //      System.out.println("type of " + node + " is " + type);
        node.setType(type);
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     */
    protected void _visitAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            _visitChild(node, i);
            ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
            Type type = child.getType();
            if (type == null) {
                throw new RuntimeException("node " + child + " has no type.");
            }
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     */
    protected void _visitChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
        child.visit(this);
    }

    protected ParserScope _scope;
}
