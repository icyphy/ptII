/* A type inference subclass for the parser for expression concept functions.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ExpressionConceptFunctionParseTreeTypeInference

/** A type inference subclass for the parser for expression concept functions.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ExpressionConceptFunctionParseTreeTypeInference extends
ParseTreeTypeInference {

    /** Set the type of the given node to be the return type of the
     *  function determined for the given node.  Since all functions in a
     *  Concept function expression are also Concept functions, the return type
     *  is always a Concept.
     *  @param node The specified node.
     *  @exception IllegalActionException Not thrown in this overridden
     *   method.
     */
    @Override
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        _setType(node, new ConceptType());
    }

    /** Set the type of the given node. Since the expression concept function
     *  leaf nodes are all tokens containing Concept objects, always set the
     *  type of the leaf node to be a token that holds a concept object.
     *  @param node The specified node.
     *  @exception IllegalActionException Not thrown in this overridden
     *   method.
     */
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {

        // If the leaf node's parent is a method call node, then
        // we are calling a Java method on an object, and we
        // reuse the normal expression parse tree type inference to evaluate
        // its type.  The scope of which java objects are valid
        // for concept functions is defined by the
        // ActorModelScope class that is a subclass
        // of ModelScope which implements the ParserScope interface.
        if (node.jjtGetParent() instanceof ASTPtMethodCallNode) {
            super.visitLeafNode(node);
            return;
        }

        _setType(node, new ConceptType());
    }
}
