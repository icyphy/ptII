/* A visitor for parse trees of the expression language.

 Copyright (c) 1998-2003 The Regents of the University of California
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)

*/
package ptolemy.domains.fsm.kernel;

import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.kernel.util.IllegalActionException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeEvaluatorForGuardExpression
/**
A ParseTreeEvaluatorForGuardExpression contains a relation list which
is used to indicate the value change of the relations embedded in a
guard expression. It extends ParseTreeEvaluator. It is specially
designed for guard expressions associated with transitions of FSM. It
visits a parse tree and evaluates it into a token.

<p> The ParseTreeEvaluatorForGuardExpression has two operation modes:
construction and updat. During construction mode, the relation list
expands. During update mode, the relation list doesn't expand but has
its element updated.

<p> During the parse tree evaluation, if the visiting node is a leaf
node and the evaluated token is a boolean token, or the visiting node
is a relational node, the evaluator decides the 'difference' and
'relationType' of the node, and stores these information into the
relation list.

<p> The 'difference' of a node is calculated this way:

<p> For a leaf node evaluated as a boolean token, the difference is
0. For a relation node, (scalarLeft relationOperator scalarRight), the
difference is the absolute double value of (scalarLeft - scalarRight).

<p> The 'relationType' of a node has 5 different values with meaning:
1: true; 2: false; 3: equal/inequal; 4: less_than: 5: bigger_than.  It
is calculated this way:

<p> For a leaf node evaluated as a boolean token, the relationType is
assigned according to the boolean value of the result token, 1 for
true and 2 for false.  For a relation node, (scalarLeft
relationOperator scalarRight), the relationType depends on the
relationOperator. If the relationOperator is '==' or '!=', rhe
relationType can be 3 indicating the two scalars equal or not equal, 4
indicating the left scalar is less than the right one, and 5 to
indicate left scalar is bigger than the right one. For other kind of
relationOperators, the relationType is assigned according to the
boolean value of the relation, i.e., 1 for true and 2 for false.

<p> If the evaluator is in construction mode, the node information is
added into a relationList, if it is in update mode, the according
element of a relationList gets updated.

<p> Note, this evaluator does not use short-circuit evaluation on
logical nodes.

@author Haiyang Zheng
@version $Id
@since Ptolemy II 2.2
@see RelationList
@see ptolemy.data.expr.ParseTreeEvaluator
*/

public class ParseTreeEvaluatorForGuardExpression extends ParseTreeEvaluator {

    /** Construct a parse tree evaluator for guard expression. The relation
     *  list is used to store the information of the relation nodes and leaf
     *  nodes with boolean tokens. If the relation list is empty, the evaluator
     *  is in construction mode, otherwise, it is in update mode.
     *  @param relationList The relation list.
     *  @param errorTolerance The errorTolerance.
     */
    public ParseTreeEvaluatorForGuardExpression(
            RelationList relationList, double errorTolerance) {
        if (relationList.isEmpty()) {
            _constructingRelationList = true;
        } else {
            _constructingRelationList = false;
        }
        _relationList = relationList;
        _relationIndex = 0;
        _absentDiscreteVariables = new LinkedList();
        _errorTolerance = errorTolerance;
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
    public ptolemy.data.Token evaluateParseTree(
            ASTPtRootNode node, ParserScope scope)
            throws IllegalActionException {
        _relationIndex = 0;
        return super.evaluateParseTree(node, scope);
    }

    /** Set the mode of parse tree evaluator.  If the given flag is
     * true, then the relation list will be populated, based on the
     * nodes in the parse tree.  If the given flag is false, then the
     * list will simply be updated with new information
     * @param mode The mode of the parse tree evaluator.
     */
    public void setEvaluationMode(boolean mode) {
        _constructingRelationList = mode;
    }

    /** Visit the leaf node. It is evaluated the same way as normal parse tree
     *  evaluator, except that if the the result is a boolean token, the
     *  information about the node (the difference and relationType) is either
     *  added into the relation list or used to update the according element
     *  in the relation list, depending on the evaluator mode.
     *  @param node The leaf node to be evaluated.
     *  @exception IllegalActionException If the super class method
     *  visitLeafNode throws the IllegalActionException.
     */

    public void visitLeafNode(ASTPtLeafNode node)
            throws IllegalActionException {

        // FIXME: based on the *_isPresent variable, we figure out
        // the discrete variables and do not evaluate it when it is
        // not present.
        // This is not the best solution, we should use the signalType
        // attribute to differentiate the signals. Unfortunately, the
        // signalType is not passed along as the type system does.
        // That is future work.
        String nodeName = node.getName();
        String discreteVariableName = "";
        if (nodeName != null) {
            int variableNameEndIndex = nodeName.indexOf("_isPresent");
            if (variableNameEndIndex != -1) {
                discreteVariableName = nodeName.substring(0,
                        variableNameEndIndex);
            }
        }

        if (_absentDiscreteVariables.contains(nodeName)) {
            // Set the result token to be false token
            // because the variable is discrete and has no value.
            // Note usually the usage is "x_isPresent && x"
            _evaluatedChildToken = new BooleanToken(false);

            if (_constructingRelationList) {
                _relationList.addRelation(0, 0.0);
            }

            // Only increment the relation index but do not update
            // the relation node.
            _relationIndex ++;
            if (_relationIndex >= _relationList.length()) {
            	_relationIndex -= _relationList.length();
            }
       
            return;
        }

        // evaluate the leaf node.
        super.visitLeafNode(node);

        ptolemy.data.Token result = _evaluatedChildToken;

        if (!(result instanceof BooleanToken)) return;

        if (((BooleanToken) result).booleanValue()) {
            _relationType = 1;
            if (_absentDiscreteVariables.contains(discreteVariableName)) {
                // remove the discrete variable from the absent discrete variables list
                _absentDiscreteVariables.remove(discreteVariableName);
            }
        } else {
            _relationType = 2;
            if (!_absentDiscreteVariables.contains(discreteVariableName)) {
                // add the discrete variable into the absent discrete variables list
                _absentDiscreteVariables.add(discreteVariableName);
            }
        }
        _difference = 0.0;

        if (_constructingRelationList) {
            _relationList.addRelation(_relationType, _difference);
        } else {
            _relationList.setRelation(_relationIndex, _relationType,
                    _difference);
        }

        _relationIndex ++;
		if (_relationIndex >= _relationList.length()) {
			_relationIndex -= _relationList.length();
		}
    }

    /** Visit the logical node. This visitor does not use short-circuit
     *  evaluation. It evaluates both the nodes beside the logical operator.
     *  @param node The logical node to be evaluated.
     *  @exception IllegalActionException If the super class method
     *  visitLogicalNode throws the IllegalActionException.
     */
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            return;
        }

        // Note that we do evaluate all of the children...
        // We evaluate al the children in order until the final value is
        // determined.

        // FIXME: Discrete variables should be treated differently.

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
        _assert(node.isLogicalAnd() ^ node.isLogicalOr(),
                node, "Invalid operation");

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
                result = ((BooleanToken)nextToken).and((BooleanToken)result);
            } else {
                result = ((BooleanToken)nextToken).or((BooleanToken)result);
            }
        }
        _evaluatedChildToken = result;
    }

    /** Visit the relation node. The evaluation part is the same as normal
     *  parseTreeEvaluator, except that information about each relation (the
     *  difference and relationType) is either added into the relation list or
     *  used to update the according element in the relation list, depending
     *  on the evaluator mode.
     *  @param node The relation node to be evaluated.
     *  @exception IllegalActionException If the super class method
     *  visitRelationNode throws the IllegalActionException.
     */
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {

        if (node.isConstant() && node.isEvaluated()) {
            return;
        }

        // Check whether the relation node has some absent discrete variables.
        // If yes, skip the node, otherwise, evaluate (visit) the node.
        Set variablesOfNode =
            _variableCollector.collectFreeVariables(node);
        Iterator absentDiscreteVariables = _absentDiscreteVariables.listIterator();
        while (absentDiscreteVariables.hasNext()) {
            String variableName = (String) absentDiscreteVariables.next();
            if (variablesOfNode.contains(variableName)) {
                // Set the result token to be false token
                // because the variable is discrete and has no value.
                // Note usually the usage is "x_isPresent && x == 1.0"
                _evaluatedChildToken = new BooleanToken(false);

                if (_constructingRelationList) {
                    _relationList.addRelation(0, 0.0);
                }
                _relationIndex++;
				if (_relationIndex >= _relationList.length()) {
					_relationIndex -= _relationList.length();
				}     
                return;
            }
        }

        ptolemy.data.Token[] tokens = _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        ptolemy.data.expr.Token operator = (ptolemy.data.expr.Token)node.getOperator();
        ptolemy.data.Token leftToken = tokens[0];
        ptolemy.data.Token rightToken = tokens[1];

        ptolemy.data.Token result;
        
        if (operator.kind == PtParserConstants.EQUALS ||
                operator.kind == PtParserConstants.NOTEQUALS) {
            // If the operator is about equal or notEqual relations,
            if (operator.kind == PtParserConstants.EQUALS) {
                result = leftToken.isCloseTo(rightToken, _errorTolerance);
            } else {
                result = leftToken.isCloseTo(rightToken, _errorTolerance).not();
            }
            // If the left and right tokens are scalars.
            if ((leftToken instanceof ScalarToken) &&
                    (rightToken instanceof ScalarToken)) {
                 // handle the relations like x == 2.0
                ScalarToken difference = (ScalarToken) leftToken.subtract(
                        rightToken);
                if ( ( (BooleanToken) result).booleanValue()) {
                    _relationType = 3;
                }
                else {
                    if (difference.doubleValue() < 0) {
                        _relationType = 4;
                    }
                    else {
                        _relationType = 5;
                    }
                }
                _difference = difference.doubleValue();
             } else {
                 // handle the relations like x == true or x == "string"
                 if (((BooleanToken) result).booleanValue()) {
                     _relationType = 1;
                 } else {
                     _relationType = 2;
                 }
                 _difference = 0.0;
             } 
        } else {
            // If the operator is not about equal or notEqual relations.
            if (!((leftToken instanceof ScalarToken) &&
                    (rightToken instanceof ScalarToken))) {
                throw new IllegalActionException(
                        "The " + operator.image +
                        " operator can only be applied between scalars.");
            }
            ScalarToken leftScalar = (ScalarToken)leftToken;
            ScalarToken rightScalar = (ScalarToken)rightToken;
            if (operator.kind == PtParserConstants.GTE) {
                result = leftScalar.isLessThan(rightScalar).not();
            } else if (operator.kind == PtParserConstants.GT) {
                result = rightScalar.isLessThan(leftScalar);
            } else if (operator.kind == PtParserConstants.LTE) {
                result = rightScalar.isLessThan(leftScalar).not();
            } else if (operator.kind == PtParserConstants.LT) {
                result = leftScalar.isLessThan(rightScalar);
            } else {
                throw new IllegalActionException(
                        "Invalid operation " + operator.image + " between " +
                        leftToken.getClass().getName() + " and " +
                        rightToken.getClass().getName());
            }
            if (((BooleanToken) result).booleanValue()) {
                _relationType = 1;
            } else {
                _relationType = 2;
            }
            _difference = ((ScalarToken)leftScalar.subtract(rightScalar)).doubleValue();
        }
        _evaluatedChildToken = result;

        if (_constructingRelationList) {
            _relationList.addRelation(_relationType, _difference);
        } else {
            _relationList.setRelation(_relationIndex, _relationType,
                    _difference);
        }

        _relationIndex++;
		if (_relationIndex >= _relationList.length()) {
			_relationIndex -= _relationList.length();
		}    
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of discrete variables without values.
    private LinkedList _absentDiscreteVariables;

    // Flag to indicate the parse tree evaluator in construction mode
    // of update mode.
    private boolean _constructingRelationList;

    // The metric for relations.
    private double _difference;

    // The error tolerance.
    private double _errorTolerance;

    // The list to store the relation nodes and leaf nodes with
    // boolean tokens inside a guard expression.
    private RelationList _relationList;
    
    // the relation types have 5 integer values with meaning:
    // 1: true; 2: false; 3: equal/inequal; 4: less_than: 5: bigger_than.
    private int _relationType;

    // variable collector
    private ParseTreeFreeVariableCollector _variableCollector;

    // private relation node index
    private int _relationIndex;
}
