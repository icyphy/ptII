/* A class representing a concept function that is defined by a boolean expression
 * of concepts specified in a ConceptFunctionDefinitionAttribute.
 *
 * Copyright (c) 2010-2013 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */
package ptolemy.data.ontologies;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ExpressionConceptFunction

/** <p>This is a class for concept functions that are used
 *  specified using an expression from a ConceptFunctionDefinitionAttribute.
 *  The expression is evaluated with the input arguments to evaluate the
 *  concept function.
 *
 *  <p>An expression that represents a concept function is a boolean if-then expression
 *  that has multiple conditional statements that specifies the output concept value
 *  based on the input concept valuesof the arguments.  Example: consider a function
 *  that has two arguments arg1 and arg2, and each argument can be a concept from
 *  the DimensionSystem ontology which has the possible Concept values:
 *
 *  <BR><em>{Unknown, Dimensionless, Time, Position, Velocity, Acceleration, Conflict}</em>
 *  <p>
 *  A valid expression concept function could be defined like this:
 *  <BR>
 *  <code>f(arg1, arg2) =
 *  <BR>&nbsp;&nbsp; arg1 == Unknown || arg2 == Unknown ? Unknown :
 *  <BR>&nbsp;&nbsp;&nbsp;&nbsp; arg1 == Position &amp;&amp; arg2 == Time ? Velocity :
 *  <BR>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; arg1 == Velocity &amp;&amp; arg2 == Time ? Acceleration :
 *  <BR>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Conflict
 *  </code>
 *  <p>This expression function specifies that the output of the function is <em>Unknown</em>
 *  if either argument is <em>Unknown</em>, <em>Velocity</em> if arg1 is <em>Position</em> and arg2 is <em>Time</em>,
 *  <em>Acceleration</em> if arg1 is <em>Velocity</em> and arg2 is <em>Time</em>, and <em>Conflict</em> if arg1 and
 *  arg2 are any other combination of concept values.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ExpressionConceptFunction extends ConceptFunction {

    /** Initialize the expression concept function with the number of
     *  arguments it takes, the ontologies from which input and output
     *  concepts can be taken, the name of the function, and the
     *  string boolean expression that defines the function.
     *  @param numArgsIsFixed True if the number of arguments for this function
     *   is fixed and cannot change, false otherwise.
     *  @param argumentDomainOntologies The array of ontologies that
     *   represent the concept domain for each input concept argument.
     *  @param outputRangeOntology The ontology that represents the
     *   range of output concepts for this concept function.
     *  @param name The string name used to identify this concept function.
     *  @param argumentNames The list of strings that represent the
     *   names of the arguments to be used in parsing the concept
     *   function expression.
     *  @param conceptFunctionExpression A string representing the
     *  boolean expression that defines the concept function.
     *  @param solverModel The ontology solver model that contains
     *   other concept function definitions that could be called by
     *   the function defined in this expression.
     *  @param functionScopeModelElement knk.
     *  @exception IllegalActionException If the ontology inputs are null
     *   or the length of the array of domain ontologies does not
     *   match the number of arguments for the function.
     */
    public ExpressionConceptFunction(String name, boolean numArgsIsFixed,
            List<Ontology> argumentDomainOntologies,
            Ontology outputRangeOntology, List<String> argumentNames,
            String conceptFunctionExpression, OntologySolverModel solverModel,
            NamedObj functionScopeModelElement) throws IllegalActionException {

        super(name, numArgsIsFixed, argumentDomainOntologies,
                outputRangeOntology);
        _argumentNames = new LinkedList<String>(argumentNames);

        if (_argumentNames == null) {
            throw new IllegalActionException(
                    "The argumentNames list cannot be null.");
        } else if (numArgsIsFixed
                && _argumentNames.size() != getNumberOfArguments()) {
            throw new IllegalActionException(
                    "The size of the argument name list for the concept function's "
                            + "argument list does not match the given number of "
                            + "arguments for the concept " + "function " + this
                            + ": number of arguments = "
                            + getNumberOfArguments()
                            + ", size of the argument name list = "
                            + _argumentNames.size());
        }

        _conceptFunctionExpression = conceptFunctionExpression;
        if (_conceptFunctionExpression == null) {
            throw new IllegalActionException(
                    "The conceptFunctionExpression cannot be null.");
        }

        _solverModel = solverModel;

        if (functionScopeModelElement != null) {
            _functionScope = new ActorModelScope(functionScopeModelElement);
        } else {
            _functionScope = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the output of the concept function based on the concept
     *  inputs. This method evaluates the expression string using the
     *  concept inputs and the argument names array.
     *  @param inputConceptValues The array of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error
     *  evaluating the function.
     */
    @Override
    protected Concept _evaluateFunction(List<Concept> inputConceptValues)
            throws IllegalActionException {
        // Get the shared parser.
        PtParser parser = new PtParser();

        // Create the parse tree.
        ASTPtRootNode parseTree = parser
                .generateParseTree(_conceptFunctionExpression);

        // Evaluate the concept function expression.
        ExpressionConceptFunctionParseTreeEvaluator evaluator = new ExpressionConceptFunctionParseTreeEvaluator(
                _argumentNames, inputConceptValues, _solverModel,
                _argumentDomainOntologies, _outputRangeOntology);

        ConceptToken conceptToken = null;
        if (_functionScope != null) {
            conceptToken = evaluator.evaluateParseTree(parseTree,
                    _functionScope);
        } else {
            conceptToken = evaluator.evaluateParseTree(parseTree);
        }
        Concept output = conceptToken.conceptValue();

        if (output == null) {
            throw new IllegalActionException(
                    "Error evaluating ExpressionConceptFunction:"
                            + " output value is null.");
        }

        return output;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of argument names for the argument array to be used
     *  for parsing the concept function expression string.
     */
    private List<String> _argumentNames;

    /** The boolean expression string that when evaluated implements
     *  the concept function.
     */
    private String _conceptFunctionExpression;

    /** The ontology solver model that contains definitions of other
     *  concept functions that could be called in this expression.
     */
    private OntologySolverModel _solverModel;

    /** The Ptolemy element scope for the concept function expression. */
    private ActorModelScope _functionScope;
}
