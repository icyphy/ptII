/* A visitor for parse trees of the expression language that collects the set of free variables.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ParseTreeFreeVariableRenamer

/**
 This class visits parse trees and renames the free variables
 that match a specified variable. Use this class prior to changing
 the name of a variable to update references to the variable.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeFreeVariableRenamer extends AbstractParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Rename the variables, if any, in the dependentVariable
     *  that refer to the specified variableToRename.
     *  @param node The root node of the parse tree.
     *  @param dependentVariable The dependent variable.
     *  @param variableToRename The variable to rename.
     *  @param name The new name.
     */
    public void renameVariables(ASTPtRootNode node, Variable dependentVariable,
            Variable variableToRename, String name)
                    throws IllegalActionException {
        _scope = dependentVariable.getParserScope();
        _dependentVariable = dependentVariable;
        _variableToRename = variableToRename;
        // If the variable containing a reference to the variable to be
        // renamed is in string mode, prepend a $ to the name.
        if (_dependentVariable.isStringMode()) {
            _name = "$" + name;
        } else {
            _name = name;
        }
        node.visit(this);
        _scope = null;
        _variableToRename = null;
        _name = null;
    }

    @Override
    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        Collection arguments = node.getArgumentNameList();
        // Make sure that parameters that are already formal
        // formal parameters remain so after.
        Collection alreadyFormal = _intersection(_formalParameters, arguments);
        _formalParameters.addAll(arguments);
        node.getExpressionTree().visit(this);
        _formalParameters.removeAll(arguments);
        _formalParameters.addAll(alreadyFormal);
    }

    @Override
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            return;
        }
        String name = node.getName();
        if (name != null
                && !_formalParameters.contains(name)
                && ModelScope.getScopedVariable(null, _dependentVariable, name) == _variableToRename) {
            node._name = _name;
        }
    }

    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _visitAllChildren(node);
    }

    @Override
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the intersection of two collections.
     *  @param collection1 The first collection.
     *  @param collection2 The second collection.
     */
    protected Collection _intersection(Collection collection1,
            Collection collection2) {
        Set result = new HashSet();
        Iterator items = collection1.iterator();
        while (items.hasNext()) {
            Object item = items.next();
            if (collection2.contains(item)) {
                result.add(items);
            }
        }
        return result;
    }

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     */
    @Override
    protected void _visitAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numChildren; i++) {
            _visitChild(node, i);
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     */
    @Override
    protected void _visitChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        child.visit(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The variable to which the parse tree belongs. */
    protected Variable _dependentVariable;

    /** Formal parameters within a function definition. */
    protected Set _formalParameters = new HashSet();

    /** The new name. */
    protected String _name;

    /** The scope. */
    protected ParserScope _scope;

    /** The variable to be renamed. */
    protected Variable _variableToRename;
}
