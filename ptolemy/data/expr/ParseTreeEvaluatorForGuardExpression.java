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
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.matlab.Engine;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.StringTokenizer;

import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeEvaluatorForGuardExpression
/**
This class visits parse trees and evaluates them into a token value.

@author Haiyang Zheng
@version $Id
@since Ptolemy II 2.1
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeEvaluatorForGuardExpression extends ParseTreeEvaluator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Visit the leaf node. If the leaf node contains "true", the return token
     *  is evaluated to 0.0 in this parseTreeEvaluator. Otherwise, it is evaluated
     *  as normal parseTreeEvaluator.
     *  @param node The leaf node to be evaluated.
     */

    public void visitLeafNode(ASTPtLeafNode node)
        throws IllegalActionException {

        ptolemy.data.Token value = node.getToken();


        // evaluate the leaf node.
        super.visitLeafNode(node);

        // If the token is "true", we set the token as -1.0
        // to indicate that refiring at the same time is necessary.
        // By "the same time", we mean current time + time resolution.
        // FIXME
        value = node.getToken();

        if(value instanceof BooleanToken) {
            if ( ( (BooleanToken) value).booleanValue()) {
                node.setToken(new DoubleToken(-1.0));
                return;
            }
        }
    }

    /** Visit the logical node. The short-circuit evaluation is used here
     *  if one of the children has token as "true" or the only child has
     *  token as "true", known as constant, we return the evaluation result
     *  as -1.0. Otherwise, we return the minimum double value of all children.
     *  @param node The logical node to be evaluated.
     */
    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            ptolemy.data.Token result = node.getToken();
            if(result instanceof BooleanToken) {
                if ( ( (BooleanToken) result).booleanValue()) {
                    node.setToken(new DoubleToken(-1.0));
                }
            }
            return;
        }

        // Note that we do not always evaluate all of the children...
        // We perform short-circuit evaluation instead and evaluate the
        // children in order until the final value is determined, after
        // which point no more children are evaluated.

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        _evaluateChild(node, 0);

        ptolemy.data.Token result = node.jjtGetChild(0).getToken();
        if(result instanceof BooleanToken) {
            if (((BooleanToken) result).booleanValue()) {
                node.setToken(new DoubleToken(-1.0));
                return;
            }
/*            throw new IllegalActionException("Cannot perform minimum "
                    + "operation on " + result + " which is a "
                    + result.getClass().getName());
*/
        }

        // Make sure that exactly one of AND or OR is set.
        _assert(node.isLogicalAnd() ^ node.isLogicalOr(),
                node, "Invalid operation");

        node.setToken(result);

        for(int i = 0; i < numChildren; i++) {
            ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
            // Evaluate the child
            child.visit(this);
            // Get its value.
            ptolemy.data.Token nextToken = child.getToken();

            if(nextToken instanceof BooleanToken) {
                if (((BooleanToken) nextToken).booleanValue()) {
                    node.setToken(new DoubleToken(-1.0));
                    return;
                }
            } else if (((ScalarToken) nextToken).isLessThan((ScalarToken) result).booleanValue()) {
                node.setToken(nextToken);
            }
        }
        return;
    }

    /** Visit the relation node. The evaluation of the relation node in this evaluator
     *  is different. Given a relation expression, we get the absolute value of the
     *  subtract of the two operands beside the relation operator.
     *  @param node The relation node to be evaluated.
     */
    public void visitRelationalNode(ASTPtRelationalNode node)
        throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        Token operator = (Token)node.getOperator();
        ptolemy.data.Token leftToken = node.jjtGetChild(0).getToken();
        ptolemy.data.Token rightToken = node.jjtGetChild(1).getToken();
        ptolemy.data.Token result;

        if(!((leftToken instanceof ScalarToken) &&
                (rightToken instanceof ScalarToken))) {
            _assert(true, node,
                    "The " + operator.image +
                    " operator cannot be applied between " +
                    leftToken.getClass().getName() + " and " +
                    rightToken.getClass().getName());
        }

        if(operator.kind == PtParserConstants.EQUALS ||
           operator.kind == PtParserConstants.NOTEQUALS ||
           operator.kind == PtParserConstants.GTE ||
           operator.kind == PtParserConstants.GT ||
           operator.kind == PtParserConstants.LTE ||
           operator.kind == PtParserConstants.LT) {
            result = ((ScalarToken) leftToken.subtract(rightToken)).absolute();
        } else {
            throw new IllegalActionException(
                    "Invalid operation " + operator.image + " between " +
                    leftToken.getClass().getName() + " and " +
                    rightToken.getClass().getName());
        }

       node.setToken(result);
    }
}
