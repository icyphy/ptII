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
import java.util.Map;

import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

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
     *  @param inputConceptValues The array of concept values to which the
     *   arguments are set.
     *  @param solverModel The ontology solver model that contains the scope
     *   of other concept functions that can be called in the expression.
     *  @param argumentDomainOntologies The array of ontologies that
     *   represent the concept domain for each input concept argument.
     *  @throws IllegalActionException If there is a problem instantiating
     *   the parse tree evaluator object.
     */
    public ExpressionConceptFunctionParseTreeEvaluator(List<String> argumentNames,
            List<Concept> inputConceptValues, OntologySolverModel solverModel,
            List<Ontology> argumentDomainOntologies)
        throws IllegalActionException {
        _argumentNames = new LinkedList<String>(argumentNames);
        _argumentConceptValues = new LinkedList<Concept>(inputConceptValues);
        _solverModel = solverModel;
        _domainOntologies = new LinkedList<Ontology>(argumentDomainOntologies);

        _typeInference = new ExpressionConceptFunctionParseTreeTypeInference();
        _addConceptConstants();
    }
    
    /** Construct an ExpressionConceptFunctionParseTreeEvaluator for
     *  evaluating expressions that represent concept functions.
     *  
     *  This constructor can only be used on parse trees whose arguments
     *  are all drawn from the same ontology.
     *  
     *  @param arguments A map of argument names to concept values.
     *  @param solverModel The ontology solver model that contains the scope
     *   of other concept functions that can be called in the expression.
     *  @param argumentDomainOntology The ontology representing the
     *   domain from which all input arguments are drawn.
     *  @throws IllegalActionException If there is a problem instantiating
     *   the parse tree evaluator object.
     */
    public ExpressionConceptFunctionParseTreeEvaluator(
            Map<String, Concept> arguments,
            OntologySolverModel solverModel,
            Ontology argumentDomainOntology)
        throws IllegalActionException {
        _solverModel = solverModel;
        _domainOntologies = new LinkedList<Ontology>();
        _domainOntologies.add(argumentDomainOntology);
        
        _argumentNames = new LinkedList<String>();
        _argumentConceptValues = new LinkedList<Concept>();
        for (Map.Entry<String, Concept> entry : arguments.entrySet()) {
            _argumentNames.add(entry.getKey());
            _argumentConceptValues.add(entry.getValue());
        }
        
        _typeInference = new ExpressionConceptFunctionParseTreeTypeInference();
        _addConceptConstants();
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
        List<ConceptFunctionDefinitionAttribute> conceptFunctionDefs =
            _conceptFunctionDefinitions();

        ConceptFunction function = null;
        for (Object functionDef : conceptFunctionDefs) {
            if (((ConceptFunctionDefinitionAttribute) functionDef)
                    .getName().equals(functionName)) {
                function = ((ConceptFunctionDefinitionAttribute) functionDef)
                        .createConceptFunction();
                break;
            }
        }

        // The first child contains the function name as an id.  It is
        // ignored, and not evaluated unless necessary.
        int argCount = node.jjtGetNumChildren() - 1;
        List<Concept> argValues = new LinkedList<Concept>();

        // First try to find a signature using argument token values.
        for (int i = 0; i < argCount; i++) {
            // Save the resulting value.
            _evaluateChild(node, i + 1);
            ConceptToken token = (ConceptToken) _evaluatedChildToken;
            argValues.add(token.conceptValue());
        }

        if (node.getFunctionName().compareTo("lub") == 0) {
            CPO cpo = ((Ontology) argValues.get(0).getContainer()).getConceptGraph();
            Concept bound = (Concept) cpo.leastUpperBound(argValues.toArray());
            _evaluatedChildToken = new ConceptToken(bound);
            return;
        }

        if (function == null) {
            throw new IllegalActionException(
                    "Unrecognized concept function name: " + functionName
                            + " in the concept function expression string.");
        }

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


        // Evaluate the concept function and set the evaluated token
        // to the string name of the concept that is the output of the
        // function.
        _evaluatedChildToken = new ConceptToken(function.evaluateFunction(
                argValues));
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
        _evaluatedChildToken = null;        
        String nodeLabel = _getNodeLabel(node);
        
        // If the leaf node's parent is a method call node, then
        // we are calling a Java method on an object, and we
        // reuse the normal expression parser to evaluate it.
        // The scope of which java objects are valid
        // for concept functions is defined by the
        // ExpressionConceptFunctionParserScope class that is a subclass
        // of ModelScope which implements the ParserScope interface.
        if (node.jjtGetParent() instanceof ASTPtMethodCallNode) {
            super.visitLeafNode(node);
            return;
        }

        // If the node is an argument in the function evaluate it to
        // the name of the concept it holds.
        for (int i = 0; i < _argumentNames.size(); i++) {
            if (nodeLabel.equals(_argumentNames.get(i))) {
                _evaluatedChildToken = new ConceptToken(
                        _argumentConceptValues.get(i));
                break;
            }
        }

        // If the node is not an argument, it must be a name of a concept itself.
        if (_evaluatedChildToken == null) {
            _evaluatedChildToken = new ConceptToken(_getNamedConcept(nodeLabel));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add all the Concepts from all the domain Ontologies as constants
     *  for the parse tree evaluator.
     *  @throws IllegalActionException If there is a problem adding any
     *   of the concepts to the Constants hash table.
     */
    protected void _addConceptConstants() throws IllegalActionException {
        for (Ontology domainOntology : _domainOntologies) {
            for (Object entity : domainOntology.allAtomicEntityList()) {
                if (entity instanceof Concept) {
                    Constants.add(((Concept) entity).getName(),
                            new ConceptToken((Concept)entity));
                }
            }
        }
    }

    /** Return a list of the attributes for all concept functions
     *  that are defined in the local solver model.
     *  If the local solver model is set to null, this returns the empty list.
     *  
     *  Note, that we perform an unchecked cast from List to
     *  List<ConceptFunctionDefinitionAttribute> here, which relies on
     *  the correctness of the implementation of
     *  {@link ptolemy.kernel.util.NamedObj#attributeList(Class)}
     *  
     *  @return A list of the local concept function definition attributes. 
     */
    @SuppressWarnings("unchecked")
    private List<ConceptFunctionDefinitionAttribute> _conceptFunctionDefinitions() {
        if (_solverModel == null) {
            return new LinkedList<ConceptFunctionDefinitionAttribute>();
        } else {
            return _solverModel.attributeList(
                    ConceptFunctionDefinitionAttribute.class);
        }
    }

    /** Return the concept with the specified name. If it cannot be
     *  found in any of the argument domain ontologies, throw an
     *  exception.
     *  @param conceptName The specified name the concept should have.
     *  @return The concept with the specified name if it is found.
     *  @throws IllegalActionException If the concept cannot be found.
     */
    protected Concept _getNamedConcept(String conceptName)
        throws IllegalActionException {

        Concept outputConcept = null;
        for (Ontology domainOntology : _domainOntologies) {
            outputConcept = (Concept) domainOntology.getEntity(conceptName);
            if (outputConcept != null) {
                break;
            }
        }
        if (outputConcept == null) {
            throw new IllegalActionException("Concept named " + conceptName +
                    " was not found in any of the domain ontologies.");
        }
        
        return outputConcept;
    }
    
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
