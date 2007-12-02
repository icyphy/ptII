/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.actor.gt;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
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
import ptolemy.data.expr.Node;
import ptolemy.data.expr.ParseTreeSpecializer;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.PtParserTreeConstants;
import ptolemy.kernel.util.IllegalActionException;

/**

  @author Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 6.1
  @Pt.ProposedRating Red (tfeng)
  @Pt.AcceptedRating Red (tfeng)
  @see ParseTreeSpecializer
 */
public class PartialEvaluator extends AbstractParseTreeVisitor {

    public PartialEvaluator(ParserScope scope, Pattern pattern,
            MatchResult matchResult) {
        _scope = scope;
        _evaluator = new GTParameter.Evaluator(pattern, matchResult);
    }

    public ASTPtRootNode evaluate(ASTPtRootNode root)
    throws IllegalActionException {
        try {
            _result = (ASTPtRootNode) root.clone();
            _result.visit(this);
            return _result;
        } catch (CloneNotSupportedException e) {
            throw new IllegalActionException(null, e,
                    "Unable to clone parse tree");
        }
    }

    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
    throws IllegalActionException {
        _defaultVisit(node);
    }

    public void visitBitwiseNode(ASTPtBitwiseNode node)
    throws IllegalActionException {
        _defaultVisit(node);
    }

    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
    throws IllegalActionException {
        _defaultVisit(node);
    }

    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
    throws IllegalActionException {
        String gtEvalString = null;
        if (node.getFunctionName().equals("gt_eval")
                && node.jjtGetNumChildren() == 2) {
            Node child = node.jjtGetChild(1);
            if (child instanceof ASTPtLeafNode) {
                ASTPtLeafNode leaf = (ASTPtLeafNode) child;
                if (leaf.isConstant()
                        && leaf.getToken() instanceof StringToken) {
                    gtEvalString =
                        ((StringToken) leaf.getToken()).stringValue();
                }
            }
        }

        if (gtEvalString == null) {
            _defaultVisit(node);
        } else {
            ASTPtRootNode root = _parser.generateParseTree(gtEvalString);
            Token token = _evaluator.evaluateParseTree(root, _scope);

            ASTPtLeafNode newNode = new ASTPtLeafNode(
                    PtParserTreeConstants.JJTPTLEAFNODE);
            newNode.setToken(token);
            newNode.setType(token.getType());
            newNode.setConstant(true);

            Node parent = node.jjtGetParent();
            if (parent == null) {
                _result = newNode;
            } else {
                newNode.jjtSetParent(parent);
                for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                    if (parent.jjtGetChild(i) == node) {
                        parent.jjtAddChild(newNode, i);
                        break;
                    }
                }
            }
        }
    }

    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
    throws IllegalActionException {
        _defaultVisit(node);
    }

    public void visitLeafNode(ASTPtLeafNode node)
    throws IllegalActionException {
        _defaultVisit(node);
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

    public void visitSumNode(ASTPtSumNode node) throws IllegalActionException {
        _defaultVisit(node);
    }

    public void visitUnaryNode(ASTPtUnaryNode node)
    throws IllegalActionException {
        _defaultVisit(node);
    }

    protected void _defaultVisit(ASTPtRootNode node)
    throws IllegalActionException {
        _visitAllChildren(node);
    }

    private GTParameter.Evaluator _evaluator;

    private PtParser _parser = new PtParser();

    private ASTPtRootNode _result;

    private ParserScope _scope;

}
