/* A visitor for parse trees of the expression language that infers properties.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.data.properties.token;

import java.util.Set;

import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.Variable;
import ptolemy.data.properties.ParseTreeAnnotationEvaluator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


////ParseTreePropertyInference

/**
 This class visits parse trees and infers a property for each node in the
 parse tree.  This property is stored in the parse tree.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeTokenAnnotationEvaluator extends ParseTreeAnnotationEvaluator {

    public ParseTreeTokenAnnotationEvaluator() {
        evaluator = new ParseTreeEvaluator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Evaluate an annotation assignment. The left-hand side is
     * assumed to be a propertyable object. The right-hand side
     * is evaluated by the expression language using the scope
     * of the container of this expression.
     */
    public void visitAssignmentNode(ASTPtAssignmentNode node)
    throws IllegalActionException {
        ((ASTPtRootNode) node.jjtGetChild(0)).visit(this);
        Object object = _evaluatedObject;

        ASTPtRootNode expression = node.getExpressionTree();

        Token expressionValue = null;
        try {
            expressionValue =
                evaluator.evaluateParseTree(expression, new VariableScope());

        } catch (IllegalActionException ex) {
            // FIXME: need to keep the exception chain.
            throw _unsupportedVisitException(
                    "Cannot resolve assignment expression: " + expression);
        }

        if (expressionValue == null) {
            throw _unsupportedVisitException(
                    "Cannot resolve assignment: " + node.getAssignment());
        }

        _helper.setEquals(object, new PropertyToken(expressionValue));
    }


    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        try {
            super.visitLeafNode(node);

        } catch (IllegalActionException ex) {

            // The label may be a token property.
            if (node.isConstant()) {
                _evaluatedObject = node.getToken();
            }
        }

        // FIXME: Not handling AST constraint yet.
    }

    private ParseTreeEvaluator evaluator;// = new ParseTreeEvaluator();



    /** This class implements a scope, which is used to generate the
     *  parsed expressions in target language.
     */
    protected class VariableScope extends ModelScope {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Look up and return the macro or expression in the target language
         *  corresponding to the specified name in the scope.
         *  @param name The given name string.
         *  @return The macro or expression with the specified name in the scope.
         *  @exception IllegalActionException If thrown while getting buffer
         *   sizes or creating ObjectToken.
         */
        public Token get(String name) throws IllegalActionException {

            NamedObj container = (NamedObj) _helper.getComponent();

            Variable result = getScopedVariable(null, container, name);

            if (result != null) {
                return result.getToken();
            } else {
                return null;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The name of the attribute to look up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
        throws IllegalActionException {
            NamedObj container = (NamedObj) _helper.getComponent();
            Variable result = getScopedVariable(null, container, name);
            if (result != null) {
                return result.getType();
            } else {
                return null;
            }
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the type term to look up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
        throws IllegalActionException {
            NamedObj container = (NamedObj) _helper.getComponent();
            Variable result = getScopedVariable(null, container, name);
            if (result != null) {
                return result.getTypeTerm();
            } else {
                return null;
            }
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of variable names within the scope.
         *  @exception IllegalActionException If there is a problem
         *  getting the identifier set from the variable.
         */
        public Set identifierSet() throws IllegalActionException {
            NamedObj container = (NamedObj) _helper.getComponent();
            return getAllScopedVariableNames(null, container);
        }
    }


}
