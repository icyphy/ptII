/* A visitor for parse trees of the guard expressions.

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
package ptolemy.domains.modal.kernel;

import java.util.LinkedList;
import java.util.Set;

import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ParseTreeEvaluatorForGuardExpression

/**
 This class extends the ParseTreeEvaluator class. It is specially
 designed for guard expressions associated with transitions of FSM. An object
 of this class is a visitor that visits a parse tree of a guard expression
 and evaluates it into a token. Meanwhile, this visitor stores the type and
 difference of all relations of the guard expression into a relation list.
 Here a relation means an expression that does not contain a logical operator.
 <p>
 This visitor has two modes of operation: <i>construction</i> mode and
 <i>update mode</i>. During the construction mode, this visitor constructs a
 relation list where each element of the list corresponds to a relation of
 the guard expression. The order of the elements is fixed and it is the same
 as the order that the relations appear in the guard expression. If the guard
 expression changes, the relation list will be reconstructed. During the
 update mode, the relation list gets updated only. The order of the elements
 get updated is the same order the relations of the guard expression get
 evaluated.
 <p>
 When this visitor evaluates the parse tree, if the visiting node is a leaf
 node and the evaluated token is a boolean token, or the visiting node
 is a relational node, the visiting node is treated as a relation. The visitor
 evaluates the 'difference' and 'relationType' of this relation, and stores
 the evaluation results into the corresponding element in the relation list.
 <p>
 The 'difference' of a relation is calculated in the following way.
 For a leaf node evaluated as a boolean token, the difference is
 0. This situation corresponds to the "true", or "false", or "x_isPresent"
 elements in a guard expression. For a relational node with the format
 (scalarLeft relationOperator scalarRight), the difference is the absolute
 double value of (scalarLeft - scalarRight). For details of relation type,
 see {@link RelationType}.
 <p>
 If the evaluator is in the construction mode, the relation information is
 added into the relation list, if it is in the update mode, the corresponding
 element of the relation List gets updated.
 <p>
 Note, this evaluator does not use short-circuit evaluation on
 logical nodes, meaning all nodes will be evaluated.

 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @see RelationList
 @see ptolemy.data.expr.ParseTreeEvaluator
 */
public class ParseTreeEvaluatorForGuardExpression extends ParseTreeEvaluator {
    /** Construct a parse tree evaluator for a guard expression of the
     *  given relation list and the error tolerance for evaluation the relations.
     *  The relation stores the information of the relation. After the parse
     *  tree evaluator is created, it is always in construction mode.
     *  @param relationList The relation list.
     *  @param errorTolerance The errorTolerance.
     */
    public ParseTreeEvaluatorForGuardExpression(RelationList relationList,
            double errorTolerance) {
        _absentDiscreteVariables = new LinkedList<String>();
        _constructingRelationList = true;
        _errorTolerance = errorTolerance;
        _relationIndex = 0;
        _relationList = relationList;
        _variableCollector = new ParseTreeFreeVariableCollector();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the parse tree with the specified root node using
     *  the specified scope to resolve the values of variables.
     *  @param node The root of the parse tree.
     *  @param scope The scope for evaluation.
     *  @return The result of evaluation.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    @Override
    public Token evaluateParseTree(ASTPtRootNode node, ParserScope scope)
            throws IllegalActionException {
        _relationIndex = 0;
        Token result = super.evaluateParseTree(node, scope);
        if (_constructingRelationList) {
            _constructingRelationList = false;
        }
        return result;
    }

    /** Return the list of relations (boolean-valued expressions that do not
     *  contain a logical operator, such as comparison expressions) in the
     *  guard expression of this transition.
     *  @return The list of relations.
     */
    public RelationList getRelationList() {
        return _relationList;
    }

    /** Set parse tree evaluator to be in construction mode.
     *  Clear the relation list and the relation list
     *  will be populated, based on the nodes in the parse tree.
     */
    public void setConstructionMode() {
        _constructingRelationList = true;
        _relationList.destroy();
    }

    /** Visit the leaf node. It is evaluated the same way as normal parse tree
     *  evaluator, except that if the the result is a boolean token, the
     *  information about the node (the difference and relationType) is either
     *  added into the relation list or used to update the corresponding element
     *  in the relation list, depending on the evaluator mode.
     *  @param node The leaf node to be evaluated.
     *  @exception IllegalActionException If the super class method
     *  visitLeafNode throws the IllegalActionException.
     */
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {

        // Note: based on the *_isPresent variable, we figure out
        // the discrete variables and do not evaluate it when it is
        // not present.
        String nodeName = node.getName();

        // Check whether this leaf node contains a discrete variable.
        // If there is a discrete variable, record its name as
        // the discreteVariableName.
        String discreteVariableName = "";
        if (nodeName != null) {
            int variableNameEndIndex = nodeName.indexOf("_isPresent");
            if (variableNameEndIndex != -1) {
                discreteVariableName = nodeName.substring(0,
                        variableNameEndIndex);
            }
        }

        // Note usually the usage is "x_isPresent && x" or
        // "x_isPresent" If we know that the nodeName is one of the
        // absent discrete variables, such as x, we do not evaluate
        // the "x" after the "&&".
        if (_absentDiscreteVariables.contains(nodeName)) {
            // Set the result token to be false token
            // because the variable is discrete and has no value.
            _evaluatedChildToken = new BooleanToken(false);
            // If the current mode of the evaluator is the construction mode,
            // add a relation node into the relation list.
            if (_constructingRelationList) {
                _relationList.addRelation(RelationType.INVALID, 0.0);
            } else {
                // Only increment the relation index but do not update
                // the relation node.
                _relationIndex++;
                // Round the _relationIndex.
                if (_relationIndex >= _relationList.length()) {
                    _relationIndex -= _relationList.length();
                }
            }
            return;
        }

        // evaluate the leaf node.
        super.visitLeafNode(node);

        // Record the evaluation result.
        ptolemy.data.Token result = _evaluatedChildToken;
        // Ignore the result that is not a boolean token, which may be a scalar.
        if (!(result instanceof BooleanToken)) {
            return;
        }

        // If the result is a boolean token, calculate the relation type.
        // Meanwhile, if the nodeName is "x_isPresent", the discreteVariableName
        // is "x", based on the evaluation results of the node, we add or remove
        // the discrete variable from the list of absent discrete variables.
        if (((BooleanToken) result).booleanValue()) {
            _relationType = RelationType.TRUE;
            if (_absentDiscreteVariables.contains(discreteVariableName)) {
                // remove the discrete variable from the absent discrete variables list
                _absentDiscreteVariables.remove(discreteVariableName);
            }
        } else {
            _relationType = RelationType.FALSE;
            if (!_absentDiscreteVariables.contains(discreteVariableName)) {
                // add the discrete variable into the absent discrete variables list
                _absentDiscreteVariables.add(discreteVariableName);
            }
        }

        // In this case, the difference is always 0.0.
        _difference = 0.0;
        if (_constructingRelationList) {
            _relationList.addRelation(_relationType, _difference);
        } else {
            _relationList.setRelation(_relationIndex, _relationType,
                    _difference);
            _relationIndex++;
            // Round the _relationIndex.
            if (_relationIndex >= _relationList.length()) {
                _relationIndex -= _relationList.length();
            }
        }
    }

    /** Visit the logical node. This visitor does not use short-circuit
     *  evaluation. It evaluates both the nodes beside the logical operator.
     *  @param node The logical node to be evaluated.
     *  @exception IllegalActionException If the super class method
     *  visitLogicalNode throws the IllegalActionException.
     */
    @Override
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {

        // If the node is a constant and has been evaluated, do nothing.
        if (node.isConstant() && node.isEvaluated()) {
            return;
        }

        // Note that we do evaluate all the children nodes...
        // We evaluate ALL the children in order until the final value is
        // determined.
        // This is the reason that we can not use the visitLogicalNode() method
        // of the supr class directly.
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        ptolemy.data.Token result = _evaluateChild(node, 0);
        if (!(result instanceof BooleanToken)) {
            throw new IllegalActionException("Cannot perform logical "
                    + "operation on " + result + " which is a "
                    + result.getClass().getName());
        }

        // Make sure that exactly one of AND or OR is set.
        _assert(node.isLogicalAnd() ^ node.isLogicalOr(), node,
                "Invalid operation");
        boolean flag = node.isLogicalAnd();
        for (int i = 1; i < numChildren; i++) {
            // Evaluate the child
            ptolemy.data.Token nextToken = _evaluateChild(node, i);
            if (!(nextToken instanceof BooleanToken)) {
                throw new IllegalActionException("Cannot perform logical "
                        + "operation on " + nextToken + " which is a "
                        + result.getClass().getName());
            }
            if (flag) {
                result = ((BooleanToken) nextToken).and((BooleanToken) result);
            } else {
                result = ((BooleanToken) nextToken).or((BooleanToken) result);
            }
        }
        _evaluatedChildToken = result;
    }

    /** Visit the relation node. The evaluation part is the same as normal
     *  parseTreeEvaluator, except that information about each relation (the
     *  difference and relationType) is either added into the relation list or
     *  used to update the corresponding element in the relation list, depending
     *  on the evaluator mode.
     *  @param node The relation node to be evaluated.
     *  @exception IllegalActionException If the super class method
     *  visitRelationNode throws the IllegalActionException.
     */
    @Override
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {

        // If the node is a constant and has been evaluated, do nothing.
        if (node.isConstant() && node.isEvaluated()) {
            return;
        }

        // Check whether the relation node has some absent discrete variables.
        // If yes, skip the node, otherwise, evaluate (visit) the node.
        // For example, if we have "x_isPresent && x < 10.0", in the
        // visitLeafNode() method, we should know that x is either present or
        // absent. If x is absent, we do not evaluate the "x < 10.0" part here.
        Set<?> variablesOfNode = _variableCollector.collectFreeVariables(node);

        for (String variableName : _absentDiscreteVariables) {
            if (variablesOfNode.contains(variableName)) {
                // Set the result token to be false token
                // because the variable is discrete and has no value.
                // Note usually the usage is "x_isPresent && x == 1.0"
                _evaluatedChildToken = new BooleanToken(false);
                if (_constructingRelationList) {
                    _relationList.addRelation(RelationType.INVALID, 0.0);
                } else {
                    // Only update _relationIndex but do not change relation node.
                    _relationIndex++;
                    if (_relationIndex >= _relationList.length()) {
                        _relationIndex -= _relationList.length();
                    }
                }
                return;
            }
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node, "The number of child nodes must be two");
        ptolemy.data.expr.Token operator = node.getOperator();
        ptolemy.data.Token leftToken = tokens[0];
        ptolemy.data.Token rightToken = tokens[1];

        ptolemy.data.Token result;
        if (operator.kind == PtParserConstants.EQUALS
                || operator.kind == PtParserConstants.NOTEQUALS) {
            // If the operator is about equal or not-equal relations,
            if (operator.kind == PtParserConstants.EQUALS) {
                result = leftToken.isCloseTo(rightToken, _errorTolerance);
            } else {
                result = leftToken.isCloseTo(rightToken, _errorTolerance).not();
            }
            // If both the left and right tokens are scalars:
            // The following code basically works as a level crossing detector
            // that detects level crossing in both rising and falling
            // directions.
            // Note we cannot tell whether two double values exactly equal,
            // therefore, we need an error tolerance. This is the only place
            // where error tolerance is used.
            // Note that subtraction is not supported for BooleanToken,
            // unlike other scalars.
            if (leftToken instanceof ScalarToken
                    && rightToken instanceof ScalarToken
                    && !(leftToken instanceof BooleanToken)) {
                // handle the relations like x == 2.0
                ScalarToken difference = (ScalarToken) leftToken
                        .subtract(rightToken);
                if (((BooleanToken) result).booleanValue()) {
                    _relationType = RelationType.EQUAL_INEQUAL;
                } else {
                    if (difference.doubleValue() < 0) {
                        _relationType = RelationType.LESS_THAN;
                    } else {
                        _relationType = RelationType.GREATER_THAN;
                    }
                }
                _difference = difference.doubleValue();
            } else {
                // handle the relations like x == true, x == "str", or x!= false
                if (((BooleanToken) result).booleanValue()) {
                    _relationType = RelationType.TRUE;
                } else {
                    _relationType = RelationType.FALSE;
                }
                _difference = 0.0;
            }
        } else {
            // If the operator is neither about equal nor not-equal relations,
            // both tokens must be scalar tokens.
            if (!(leftToken instanceof ScalarToken && rightToken instanceof ScalarToken)) {
                throw new IllegalActionException(
                        "The "
                                + operator.image
                                + " operator can only be applied between scalars and not "
                                + "between a " + leftToken.getType()
                                + " and a " + rightToken.getType() + ".");
            }
            ScalarToken leftScalar = (ScalarToken) leftToken;
            ScalarToken rightScalar = (ScalarToken) rightToken;
            // A relation needs strictly satisfied.
            if (operator.kind == PtParserConstants.GTE) {
                result = leftScalar.isLessThan(rightScalar).not();
            } else if (operator.kind == PtParserConstants.GT) {
                result = rightScalar.isLessThan(leftScalar);
            } else if (operator.kind == PtParserConstants.LTE) {
                result = rightScalar.isLessThan(leftScalar).not();
            } else if (operator.kind == PtParserConstants.LT) {
                result = leftScalar.isLessThan(rightScalar);
            } else {
                throw new IllegalActionException("Invalid operation "
                        + operator.image + " between "
                        + leftToken.getClass().getName() + " and "
                        + rightToken.getClass().getName());
            }
            if (((BooleanToken) result).booleanValue()) {
                _relationType = RelationType.TRUE;
            } else {
                _relationType = RelationType.FALSE;
            }
            _difference = ((ScalarToken) leftScalar.subtract(rightScalar))
                    .doubleValue();
        }

        _difference = Math.abs(_difference);
        if (_constructingRelationList) {
            _relationList.addRelation(_relationType, _difference);
        } else {
            _relationList.setRelation(_relationIndex, _relationType,
                    _difference);
            _relationIndex++;
            if (_relationIndex >= _relationList.length()) {
                _relationIndex -= _relationList.length();
            }
        }

        _evaluatedChildToken = result;
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of discrete variables without values.
    private LinkedList<String> _absentDiscreteVariables;

    // Flag to indicate the parse tree evaluator in construction mode
    // of update mode.
    private boolean _constructingRelationList;

    // The metric for relations.
    private double _difference;

    // The error tolerance.
    private double _errorTolerance;

    // private relation node index
    private int _relationIndex;

    // The list to store the relation nodes and leaf nodes with
    // boolean tokens inside a guard expression.
    private RelationList _relationList;

    // the relation types have 6 integer values with meaning:
    // 0: invalid; 1: true; 2: false; 3: equal/inequal; 4: less_than: 5: bigger_than.
    private int _relationType;

    // variable collector
    private ParseTreeFreeVariableCollector _variableCollector;
}
