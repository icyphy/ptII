/* A visitor for parse trees of the expression language that implements a concept function.

 Copyright (c) 2010 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// ExpressionConceptFunctionParseTreeEvaluator

/**
 Visit a parse tree for a string expression that defines a concept
 function and evaluate to the string name of the concept that should
 be the output.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ExpressionConceptFunctionParseTreeEvaluator extends
        ParseTreeEvaluator {

    /** Construct an ExpressionConceptFunctionParseTreeEvaluator for
     *  evaluating expressions that represent concept functions.
     *  @param argumentNames The array of argument names used in the
     *   concept function expression.
     *  @param argumentConceptValues The array of concept values to which the
     *   arguments are set.
     *  @param solverModel The ontology solver model that contains the scope
     *   of other concept functions that can be called in the expression.
     *  @param argumentDomainOntologies The array of ontologies that
     *   represent the concept domain for each input concept argument.
     */
    public ExpressionConceptFunctionParseTreeEvaluator(List<String> argumentNames,
            List<Concept> argumentConceptValues, OntologySolverModel solverModel,
            List<Ontology> argumentDomainOntologies) {
        _argumentNames = new LinkedList<String>(argumentNames);
        _argumentConceptValues = new LinkedList<Concept>(argumentConceptValues);
        _solverModel = solverModel;
        _domainOntologies = new LinkedList<Ontology>(argumentDomainOntologies);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate a concept function contained in a concept function
     *  expression.  The concept function must be defined in an
     *  attribute contained in the ontology solver model.
     *  @param node The function expression node to be evaluated.
     *  @exception IllegalActionException If the function cannot be
     *  parsed correctly.
     */
    public void visitFunctionApplicationNode(ASTPtFunctionApplicationNode node)
            throws IllegalActionException {
        String functionName = node.getFunctionName();
        List conceptFunctionDefs = _solverModel
                .attributeList(ConceptFunctionDefinitionAttribute.class);

        ConceptFunction function = null;
        for (Object functionDef : conceptFunctionDefs) {
            if (((ConceptFunctionDefinitionAttribute) functionDef)
                    .getName().equals(functionName)) {
                function = ((ConceptFunctionDefinitionAttribute) functionDef)
                        .createConceptFunction();
            }
        }

        if (function == null) {
            throw new IllegalActionException(
                    "Unrecognized concept function name: " + functionName
                            + " in the concept function expression string.");
        }

        // The first child contains the function name as an id.  It is
        // ignored, and not evaluated unless necessary.
        int argCount = node.jjtGetNumChildren() - 1;

        if (function.isNumberOfArgumentsFixed() &&
                argCount != function.getNumberOfArguments()) {
            throw new IllegalActionException(
                    "The concept function "
                            + functionName
                            + " has the wrong number of arguments. "
                            + "Expected # arguments: "
                            + function.getNumberOfArguments()
                            + ", actual # arguments: " + argCount);
        }

        List<Concept> argValues = new LinkedList<Concept>();

        // First try to find a signature using argument token values.
        for (int i = 0; i < argCount; i++) {
            // Save the resulting value.
            _evaluateChild(node, i + 1);

            Concept tempConcept = null;
            ptolemy.data.StringToken token = (StringToken) _evaluatedChildToken;
            for (Ontology domainOntology : _domainOntologies) {
                tempConcept = (Concept) domainOntology
                        .getEntity(token.stringValue());
                if (tempConcept != null) {
                    argValues.add(tempConcept);
                    break;
                }
            }

            if (tempConcept == null) {
                throw new IllegalActionException(
                        "Cannot find Concept named "
                                + token
                                + " in any of the domain ontologies for this "
                                + "concept function.");
            }
        }

        // Evaluate the concept function and set the evaluated token
        // to the string name of the concept that is the output of the
        // function.
        _evaluatedChildToken = new StringToken(function.evaluateFunction(
                argValues).getName());
    }

    /** Evaluate the first child, and depending on its (boolean)
     *  result, evaluate either the second or the third child. The
     *  result of that evaluation becomes the result of the specified
     *  node. We needed to override this function to remove the type
     *  inferences in the superclass since that is not necessary for
     *  concept functions.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        if (node.isConstant() && node.isEvaluated()) {
            _evaluatedChildToken = node.getToken();
            return;
        }

        int numChildren = node.jjtGetNumChildren();

        if (numChildren != 3) {
            // A functional-if node MUST have three children in the parse
            // tree.
            throw new InternalErrorException(
                    "PtParser error: a functional-if node does not have "
                            + "three children in the parse tree.");
        }

        // evaluate the first sub-expression
        _evaluateChild(node, 0);

        ptolemy.data.Token test = _evaluatedChildToken;

        if (!(test instanceof BooleanToken)) {
            throw new IllegalActionException(
                    "Functional-if must branch on a boolean, but instead was "
                            + test.toString() + " an instance of "
                            + test.getClass().getName());
        }

        boolean value = ((BooleanToken) test).booleanValue();

        ASTPtRootNode tokenChild;

        if (value) {
            tokenChild = (ASTPtRootNode) node.jjtGetChild(1);
        } else {
            tokenChild = (ASTPtRootNode) node.jjtGetChild(2);
        }

        tokenChild.visit(this);

        if (node.isConstant()) {
            node.setToken(_evaluatedChildToken);
        }
    }

    /** Evaluate each leaf node in the parse tree to a concept
     *  string. Either replace an argument label from the concept
     *  function with its concept value string name or assume it is a
     *  concept name if it is not a function argument label.
     *  
     *  @param node The leaf node to be visited.
     *  @exception IllegalActionException If the node label cannot be
     *   resolved to a concept.
     */
    public void visitLeafNode(ASTPtLeafNode node) 
            throws IllegalActionException {
        String nodeLabel = _getNodeLabel(node);
        _evaluatedChildToken = null;

        // If the node is an argument in the function evaluate it to
        // the name of the concept it holds.
        for (int i = 0; i < _argumentNames.size(); i++) {
            if (nodeLabel.equals(_argumentNames.get(i))) {
                _evaluatedChildToken = new StringToken(
                        _argumentConceptValues.get(i).getName());
                break;
            }
        }

        // If the node is not an argument, it must be a name of a concept itself.
        if (_evaluatedChildToken == null) {
            _evaluatedChildToken = new StringToken(nodeLabel);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return the label for the leaf node.
     * 
     * @param node The given leaf node
     * @return The string label for the node; If the node
     * is constant this is the token contained in the node
     * as a string, if not then this is the name of the node.
     */
    protected String _getNodeLabel(ASTPtLeafNode node) {
        if (node.isConstant()) {
            return node.getToken().toString();
        } else {
            return node.getName();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of ontologies that specify the domain for each input
     *  argument to the concept function defined by the parsed
     *  expression.
     */
    protected List<Ontology> _domainOntologies;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of concept values to which the arguments are
     *  currently set.
     */
    private List<Concept> _argumentConceptValues;

    /** The list of argument names that are used in the concept
     *  function expression.
     */
    private List<String> _argumentNames;

    /** The ontology solver model that contains definitions of other
     *  concept functions that could be called in this expression.
     */
    private OntologySolverModel _solverModel;
}
