/*
 * A class representing a concept function that is defined by a boolean expression
 * of concepts specified in a ConceptFunctionDefinitionAttribute.
 * 
 * Copyright (c) 1998-2010 The Regents of the University of California. All
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

import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ExpressionConceptFunction

/** This is a class for concept functions that are used
 *  specified using an expression from a ConceptFunctionDefinitionAttribute. 
 *  The expression is evaluated with the input arguments to evaluate the 
 *  concept function.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ExpressionConceptFunction extends ConceptFunction {
    
    /** Initialize the expression concept function with the number of arguments it takes,
     *  the ontologies from which input and output concepts can be taken, the name of the function,
     *  and the string boolean expression that defines the function.
     *  @param numArgs The number of arguments to be passed into this function.
     *  @param argumentDomainOntologies The array of ontologies that represent the concept domain
     *   for each input concept argument.
     *  @param outputRangeOntology The ontology that represents the range of output concepts for this
     *   concept function.
     *  @param name The string name used to identify this concept function.
     *  @param argumentNames The list of strings that represent the names of the arguments to be used in parsing
     *   the concept function expression.
     *  @param conceptFunctionExpression A string representing the boolean expression that defines the concept function.
     *  @throws IllegalActionException If the ontology inputs are null or the length of the array of domain ontologies does not
     *   match the number of arguments for the function.
     */
    public ExpressionConceptFunction(String name, int numArgs, Ontology[] argumentDomainOntologies,
            Ontology outputRangeOntology, String[] argumentNames, String conceptFunctionExpression)
        throws IllegalActionException {
        
        super(name, numArgs, argumentDomainOntologies, outputRangeOntology);
        
        _argumentNames = argumentNames;        
        if (_argumentNames == null) {
            throw new IllegalActionException("The argumentNames list cannot be null.");
        } else if (_argumentNames.length != numArgs) {
            throw new IllegalActionException("The size of the argument name list for the concept function's " +
                    "argument array does not match the given number of arguments for the concept " +
                    "function " + this + ": numArgs = " + _numArgs + ", size of the argument name list = " +
                    _argumentNames.length);
        }
        
        _conceptFunctionExpression = conceptFunctionExpression;
        if (_conceptFunctionExpression == null) {
            throw new IllegalActionException("The conceptFunctionExpression cannot be null.");
        }
    }    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Update the concept function parameters when the values change in
     *  the ConceptFunctionDefinitionAttribute.
     * 
     * @param numArgs The new number of arguments for the function.
     * @param argumentDomainOntologies The new argument domain ontologies.
     * @param outputRangeOntology The new output range ontology.
     * @param name The new name of the concept function.
     * @param argumentNames The new array of argument names.
     * @param conceptFunctionExpression The new expression string that defines the function.
     * @throws IllegalActionException If there is a problem setting the parameter values.
     */
    public void updateFunctionParameters(String name, int numArgs, Ontology[] argumentDomainOntologies,
            Ontology outputRangeOntology, String[] argumentNames, String conceptFunctionExpression) 
        throws IllegalActionException {
        _numArgs = numArgs;
        _argumentDomainOntologies = argumentDomainOntologies;
        _outputRangeOntology = outputRangeOntology;
        
        if (_outputRangeOntology == null) {
            throw new IllegalActionException("The outputRangeOntology cannot be null.");
        }
        
        if (_argumentDomainOntologies == null) {
            throw new IllegalActionException("The argumentDomainOntologies cannot be null.");
        } else if (_argumentDomainOntologies.length != numArgs) {
            throw new IllegalActionException("The size of the array of domain ontologies for the concept function's " +
                    "argument array does not match the given number of arguments for the concept " +
                    "function " + this + ": numArgs = " + _numArgs + ", size of domain ontologies array = " +
                    _argumentDomainOntologies.length);
        }
        
        _name = name;
        _argumentNames = argumentNames;
        if (_argumentNames == null) {
            throw new IllegalActionException("The argumentNames list cannot be null.");
        } else if (_argumentNames.length != numArgs) {
            throw new IllegalActionException("The size of the argument name list for the concept function's " +
                    "argument array does not match the given number of arguments for the concept " +
                    "function " + this + ": numArgs = " + _numArgs + ", size of the argument name list = " +
                    _argumentNames.length);
        }
        
        _conceptFunctionExpression = conceptFunctionExpression;     
        if (_conceptFunctionExpression == null) {
            throw new IllegalActionException("The conceptFunctionExpression cannot be null.");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return the output of the concept function based on the concept inputs. This
     *  method evaluates the expression string using the concept inputs and the argument
     *  names array.
     *  @param inputConceptValues The array of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error evaluating the function.
     */
    protected Concept _evaluateFunction(Concept[] inputConceptValues) throws IllegalActionException {
        // Get the shared parser.
        PtParser parser = new PtParser();

        // create parse tree
        ASTPtRootNode parseTree = parser.generateParseTree(_conceptFunctionExpression);

        // Evaluate concept function expression
        ExpressionConceptFunctionParseTreeEvaluator evaluator =
            new ExpressionConceptFunctionParseTreeEvaluator(_argumentNames, inputConceptValues);
        StringToken conceptNameToken = (StringToken) evaluator.evaluateParseTree(parseTree);
        Concept output = (Concept) _outputRangeOntology.getEntity(conceptNameToken.stringValue());
        
        if (output == null) {
            throw new IllegalActionException("Output Concept " + conceptNameToken.stringValue() +
                    " not found in output range ontology " + _outputRangeOntology + ".");
        }
        
        return output;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The list of argument names for the argument array to be used for parsing the
     *  concept function expression string.
     */
    private String[] _argumentNames;
    
    /** The boolean expression string that when evaluated implements the concept function. */
    private String _conceptFunctionExpression;
}
