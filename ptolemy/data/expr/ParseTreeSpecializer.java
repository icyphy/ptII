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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeSpecializer
/**
This class reduces a parse tree, given a scope of bound variables.  If
an identifier is not found in the given scope, then the identifier is
bound to any constants registered with the expression parser.  If any
subtrees of the parse tree become constant, they are evaluated and
replaced with leaf nodes containing the evaluated result.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeSpecializer extends AbstractParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new parse tree resulting from the specialization of
     *  the given parse tree.  Every identifier reference is replaced
     *  by constants according to the given scope.  Constant subtrees
     *  are replaced with constant leaf nodes.  Exclude the given set
     *  of names from being replaced.  The given parse tree is not
     *  destroyed in the process.
     */
    public ASTPtRootNode specialize(ASTPtRootNode node, List excludedNames,
            ParserScope scope)
            throws IllegalActionException {
        _excludedNames = excludedNames;
        _scope = scope;
        _evaluator = new ParseTreeEvaluator();
        try {
            _result = (ASTPtRootNode) node.clone();
            _result._parent = null;
        } catch(CloneNotSupportedException ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to clone node for specialization");
        }
        _result.visit(this);
        _evaluator = null;
        _scope = null;
        _excludedNames = null;
        ASTPtRootNode result = _result;
        _result = null;
        return result;
    }

    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _defaultVisit(node);

    }

    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }

    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        // Check to see if we are referencing a function closure in scope.
        ptolemy.data.Token value = null;
        String functionName = node.getFunctionName();
        if (_scope != null && functionName != null) {
            if (!_excludedNames.contains(functionName)) {
                value = _scope.get(node.getFunctionName());
            }
        }

        if (value == null) {
            // Just visit arguments other than the first.
            int numChildren = node.jjtGetNumChildren();
            for (int i = 1; i < numChildren; i++) {
                _visitChild(node, i);
            }
        } else {
            _defaultVisit(node);
        }
    }

    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        List excludedNames = new LinkedList(_excludedNames);
        // Don't substitute any names in the parse tree that are
        // bound in the definition.
        excludedNames.addAll(node.getArgumentNameList());
        List oldExcludedNames = _excludedNames;
        _excludedNames = excludedNames;

        // Recurse, with the new set of bound identifiers.
        node.getExpressionTree().visit(this);

        _excludedNames = oldExcludedNames;
    }

    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitLeafNode(ASTPtLeafNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            return;
        }
        if (!_excludedNames.contains(node.getName())) {
            ptolemy.data.Token token = null;
            if (_scope != null) {
                token = _scope.get(node.getName());
            }
            if(token == null) {
                token = Constants.get(node.getName());
            }
            if(token != null) {
                node.setToken(token);
                node.setConstant(true);
                // Reset the name, since it no longer makes sense.
                node._name = null;
                return;
            }
            throw new IllegalActionException(
                    "The ID " + node.getName() + " is undefined.");
        }
    }

    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitSumNode(ASTPtSumNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _defaultVisit(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if all of the children of this node are constant.
     */
    protected boolean _childrenAreConstant(ASTPtRootNode node) {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
            if (!child.isConstant()) {
                return false;
            }
        }
        return true;
    }

    protected void _defaultVisit(ASTPtRootNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
        boolean isConstant = _childrenAreConstant(node);
        if (isConstant) {
            _replaceConstantNode(node);
        }
    }

    protected void _replaceConstantNode(ASTPtRootNode node)
            throws IllegalActionException {
        // Create the replacement
        ASTPtLeafNode newNode =
            new ASTPtLeafNode(PtParserTreeConstants.JJTPTLEAFNODE);
        ptolemy.data.Token token =
            _evaluator.evaluateParseTree(node, _scope);
        newNode.setToken(token);
        newNode.setType(token.getType());
        newNode.setConstant(true);

        ASTPtRootNode parent = (ASTPtRootNode)node._parent;
        if (parent == null) {
            _result = newNode;
        } else {
            // Replace the old with the new.
            newNode._parent = parent;
            int index = parent._children.indexOf(node);
            parent._children.set(index, newNode);
        }
    }

    protected List _excludedNames;
    protected ASTPtRootNode _result;
    protected ParserScope _scope;
    protected ParseTreeEvaluator _evaluator;
}
