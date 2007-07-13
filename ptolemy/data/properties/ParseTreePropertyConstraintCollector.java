/* A visitor for parse trees of the expression language that infers properties.

 Copyright (c) 1998-2006 The Regents of the University of California.
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


 */
package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Expression;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtBitwiseNode;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtMatrixConstructNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtPowerNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtShiftNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.properties.PropertyConstraintSolver.ConstraintType;
import ptolemy.data.type.Type;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ParseTreePropertyInference

/**
 This class visits parse trees and infers a property for each node in the
 parse tree.  This property is stored in the parse tree.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreePropertyConstraintCollector extends AbstractParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    private NamedObj _container;

    /** Infer the property of the parse tree with the specified root node using
     *  the specified scope to resolve the values of variables.
     *  @param node The root of the parse tree.
     *  @param scope The scope for evaluation.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    public Set collectConstraints(ASTPtRootNode node, NamedObj container, 
        PropertyConstraintSolver solver)
            throws IllegalActionException {

        _constraints = new HashSet();
        _container = container;
        //_scope = scope;
        _solver = solver;
        node.visit(this);
        //_scope = null;
        _solver = null;
        return _constraints;
    }

    /** Set the property of the given node to be an ArrayProperty that is the
     *  least upper bound of the properties of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the property that is the
     *  least upper bound of the properties of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the return property of the
     *  function determined for the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be a function property whose
     *  argument properties are determined by the children of the node.
     *  The return property of the function property is determined by
     *  inferring the property of function's expression in a scope that
     *  adds identifiers for each argument to the current scope.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the least upper bound of
     *  the properties of the two branches of the if.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the property of constant the
     *  variable refers to, if the node represents a constant, or the
     *  property of the identifier the node refers to in the current
     *  scope.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error
     *  occurs, or an identifier is not bound in the current scope.
     */
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            //FIXME:
            //_solver.getHelper(node).setEquals(node, _scope.getProperty(name));
            return;
        }

        NamedObj namedObj = VariableScope.getNamedObject(
                (Entity) _container, node.getName());
        
        if (namedObj != null) {
            _constraints.addAll(
                    _solver.getHelper(node).setSameAs(namedObj, node));
        }

        /*
        if (_scope != null) {
            Property property = _scope.getProperty(name);

            if (property != null) {
                //_setProperty(node, property);
                _solver.getHelper(node).setEquals(node, property);
                return;
            }
        }

        // Look up for constants.
        if (Constants.get(name) != null) {
            // A named constant that is recognized by the parser.
            //_setProperty(node, Constants.get(name).getProperty());
            return;
        }
         //*/
        
        //throw new IllegalActionException("The ID " + name + " is undefined.");
    }

    /** Set the property of the given node to be boolean.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be an MatrixProperty based on the
     *  least upper bound of the properties of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the return property of the
     *  method determined for the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the property of the first
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the least upper bound
     *  property of the properties of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be a record token that
     *  contains fields for each name in the record construction,
     *  where the property of each field in the record is determined by
     *  the corresponding property of the child nodes.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be boolean.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the property of the first
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the least upper bound
     *  property of the properties of the node's children.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _visitAllChildren(node);
    }

    /** Set the property of the given node to be the property of the
     *  child of the given node.
     *  @param node The specified node.
     *  @exception IllegalActionException If an inference error occurs.
     */
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    
    
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

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     */
    protected void _visitAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        List children = new ArrayList();
        
        int numChildren = node.jjtGetNumChildren();

        PropertyConstraintASTNodeHelper helper = _solver.getHelper(node);

        boolean isNone = 
            helper.interconnectConstraintType == ConstraintType.NONE;
        
        boolean constraintParent = 
            (helper.interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET) ||  
            (helper.interconnectConstraintType == ConstraintType.SRC_LESS);
        
        for (int i = 0; i < numChildren; i++) {
            _visitChild(node, i);

            if (!constraintParent && !isNone){
                // Set child <= parent.
                _constraints.add(helper.setAtLeast(node, node.jjtGetChild(i)));
            } else {
                children.add(node.jjtGetChild(i));
            }
        }

        if (helper._useDefaultConstraints) {
            if (constraintParent) {
                _constraints.addAll(
                        helper._constraintObject(helper.interconnectConstraintType, 
                                node, children));
            } 
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     */
    protected void _visitChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        
        child.visit(this);
    }

    protected Set _constraints;

    protected PropertyConstraintSolver _solver;
    
    //protected ParserScope _scope;
    
    
    private static class VariableScope {
        public static NamedObj getNamedObject(Entity container, String name) {

            // Check the port names.
            TypedIOPort port = (TypedIOPort) container.getPort(name);

            if (port != null) {
                return port;
            }

            Variable result = 
                ModelScope.getScopedVariable(null, container, name);

            if (result != null) {
                return result;
            }
            return null;
        }
    }
}
