/* An operation to update an object in the host model.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.actor.gt.ingredients.operations;

import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.Replacement;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeWriter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParserTreeConstants;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Operation

/**
 An operation to update an object in the host model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class Operation extends GTIngredient {

    /** Construct an operation within the given list as its owner containing a
     *  given number of elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param elementCount The number of elements that the GTIngredient has.
     */
    public Operation(GTIngredientList owner, int elementCount) {
        super(owner, elementCount);
    }

    /** Get the change request to update the object in the host model.
     *
     *  @param pattern The pattern of the transformation rule.
     *  @param replacement The replacement of the transformation rule.
     *  @param matchResult The match result.
     *  @param patternObject The object in the pattern, or null.
     *  @param replacementObject The object in the replacement that corresponds
     *   to the object in the pattern.
     *  @param hostObject The object in the host model corresponding to the
     *   object in the replacement.
     *  @return The change request.
     *  @exception IllegalActionException If error occurs in generating the
     *   change request.
     */
    public abstract ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            NamedObj patternObject, NamedObj replacementObject,
            NamedObj hostObject) throws IllegalActionException;

    /** Given a parse tree of a Ptolemy expression, evaluate it in the given
     *  scope and return a new constant AST node that has the result as its
     *  value.
     *
     *  @param parseTree The parse tree.
     *  @param evaluator The evaluator to be used.
     *  @param scope The scope.
     *  @return The new AST node with the result as its value.
     *  @exception IllegalActionException If an error occurs during the
     *   evaluation.
     */
    protected ASTPtLeafNode _evaluate(ASTPtRootNode parseTree,
            ParseTreeEvaluator evaluator, ParserScope scope)
                    throws IllegalActionException {
        Token token = evaluator.evaluateParseTree(parseTree, scope);
        ASTPtLeafNode newNode = new ASTPtLeafNode(
                PtParserTreeConstants.JJTPTLEAFNODE);
        newNode.setToken(token);
        newNode.setType(token.getType());
        newNode.setConstant(true);
        return newNode;
    }

    /** A parse tree writer to output the contents of a parse tree. For a node
     *  in the parse tree that is a string constant, it outputs the result of
     *  stringValue() instead of toString().
     */
    protected ParseTreeWriter _parseTreeWriter = new ParseTreeWriter() {

        @Override
        public void visitLeafNode(ASTPtLeafNode node)
                throws IllegalActionException {
            if (node.isConstant() && node.isEvaluated()) {
                Token token = node.getToken();
                if (token instanceof StringToken) {
                    // For StringToken, call stringValue instead of toString to
                    // avoid having an extra pair of quotes.
                    _writer.write(((StringToken) token).stringValue());
                    return;
                }
            }
            super.visitLeafNode(node);
        }
    };

}
