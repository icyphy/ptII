/* A base class representing a concept function for ontology constraints.
 *
 * Copyright (c) 1998-2014 The Regents of the University of California. All
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

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConceptFunction

/** The base class for concept functions that are used
 *  for ontology constraints. A concept function is a function over a set of
 *  concept values in an ontology.  It takes a list of input concepts from specified
 *  ontologies and returns an output concept from a specified ontology. The concept
 *  inputs and outputs need not necessarily be from the same ontology.
 *  Derived classes must implement the protected _evaluateFunction method
 *  to provide the output concept given an input array of concepts.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public abstract class ConceptFunction {

    /** Create the concept function with the number of arguments it takes
     *  and the ontologies from which input and output concepts can be taken.
     *  @param name The name of the concept function.
     *  @param numArgsIsFixed True if the number of arguments for this function
     *   is fixed and cannot change, false otherwise.
     *  @param argumentDomainOntologies The list of ontologies that
     *   represent the concept domain for each input concept argument.
     *  @param outputRangeOntology The ontology that represents the
     *   range of output concepts for this concept function.
     *  @exception IllegalActionException If the output ontology is null.
     */
    public ConceptFunction(String name, boolean numArgsIsFixed,
            List<Ontology> argumentDomainOntologies,
            Ontology outputRangeOntology) throws IllegalActionException {
        _name = name;
        _numArgsIsFixed = numArgsIsFixed;
        _argumentDomainOntologies = new LinkedList<Ontology>(
                argumentDomainOntologies);
        _outputRangeOntology = outputRangeOntology;

        if (_outputRangeOntology == null) {
            throw new IllegalActionException(
                    "The outputRangeOntology cannot be null.");
        }
    }

    /** Create the concept function where all arguments and
     *  output values are drawn from the same ontology.
     *  @param name The name of the concept function.
     *  @param numArgs The number of arguments for this function, if
     *   this number is fixed, and -1 otherwise.
     *  @param inputOutputOntology The ontology that represents the
     *   domain and range for this concept function.
     *  @exception IllegalActionException If the output ontology is null,
     *   or numArgs is invalid.
     */
    public ConceptFunction(String name, int numArgs,
            Ontology inputOutputOntology) throws IllegalActionException {
        if (inputOutputOntology == null) {
            throw new IllegalActionException("The ontology cannot be null.");
        }
        if (numArgs < -1) {
            throw new IllegalActionException("Invalid number of arguments: "
                    + numArgs);
        }

        _name = name;
        _numArgsIsFixed = numArgs >= 0;
        _outputRangeOntology = inputOutputOntology;
        _argumentDomainOntologies = new LinkedList<Ontology>();
        if (_numArgsIsFixed) {
            for (int i = 0; i < numArgs; i++) {
                _argumentDomainOntologies.add(inputOutputOntology);
            }
        } else {
            _argumentDomainOntologies.add(inputOutputOntology);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the output of the concept function based on the concept inputs.
     *  @param argValues The list of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error with
     *   the input argument array or evaluating the function.
     */
    public Concept evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {
        if (argValues == null) {
            throw new IllegalActionException(
                    "The input array to the ConceptFunction " + this
                            + " is null.");

            // If the function has a fixed number of arguments, check to make sure
            // the input list has the right number of arguments and also check
            // that each argument is contained by its domain ontology.
            // If the number of arguments is not fixed, then we cannot
            // check the domain ontologies here, and the derived class must do
            // its own check depending on how it implements a variable argument
            // concept function.
        } else if (_numArgsIsFixed) {
            if (argValues.size() != getNumberOfArguments()) {
                throw new IllegalActionException(
                        "The input array to the ConceptFunction "
                                + this
                                + " has the "
                                + "wrong number of arguments. Expected number of arguments: "
                                + getNumberOfArguments()
                                + ", size of the input argument array: "
                                + argValues.size());
            } else {
                // Check each concept argument value to make sure it is either null or
                // contained in the ontology domain for that argument.
                int index = 0;
                for (Concept argument : argValues) {
                    if (argument != null
                            && !_argumentDomainOntologies.get(index)
                                    .entityList(Concept.class)
                                    .contains(argument)) {
                        throw new IllegalActionException(
                                "The input value "
                                        + argument
                                        + " at argument index "
                                        + index
                                        + " to the ConceptFunction "
                                        + this
                                        + " is not within the expected domain ontology "
                                        + _argumentDomainOntologies.get(index)
                                        + ".");
                    }
                    index++;
                }
            }
        }

        Concept outputValue = _evaluateFunction(argValues);

        // Check that the output is either null or in the output range ontology
        if (outputValue != null
                && !_outputRangeOntology.equals(outputValue.getOntology())) {
            throw new IllegalActionException("The ConceptFunction " + this
                    + " has evaluated to the value " + outputValue
                    + " which is not in the expected ontology range "
                    + _outputRangeOntology + ".");
        } else {
            return outputValue;
        }
    }

    /** Return the array of ontologies that represent the domains for
     *   the input arguments to the concept function.
     *  @return The array of ontologies that represent the domains for
     *   the input arguments to the concept function.
     */
    public List<Ontology> getArgumentDomainOntologies() {
        return _argumentDomainOntologies;
    }

    /** Return the name of the concept function.
     *  @return The name of the concept function.
     */
    public String getName() {
        return _name;
    }

    /** Return the number of input arguments for this concept function,
     *  or -1 if the concept function can take a variable number of
     *  arguments.
     *  @return The number of arguments taken as input for this function.
     */
    public int getNumberOfArguments() {
        if (_numArgsIsFixed) {
            return _argumentDomainOntologies.size();
        } else {
            return -1;
        }
    }

    /** Return the ontology that represents the range of concepts that can be
     *  output by this concept function.
     *
     *  @return The ontology that represents the range of concepts that can be
     *   output by this concept function.
     */
    public Ontology getOutputRangeOntology() {
        return _outputRangeOntology;
    }

    /** Determine whether the concept function is monotonic over the
     *  ontology for the output range and all the inputs that are also
     *  in that same ontology. Some of the input arguments to the
     *  function might be in other domain ontologies that are not the
     *  same as the output ontology range, so those arguments are not
     *  considered for determining monotonicity.
     *  @return true if the concept function is monotonic, false otherwise.
     */
    public boolean isMonotonic() {
        // FIXME: 02/04/10 Charles Shelton - We need to figure out an implementation
        // to validate monotonicity for a concept function.
        return true;
    }

    /** Return true if this concept function has a fixed number of
     *  arguments, false otherwise.
     *  @return True if the function has a fixed number of arguments,
     *   false otherwise.
     */
    public boolean isNumberOfArgumentsFixed() {
        return _numArgsIsFixed;
    }

    /** Return a string representing the name of this concept function.
     *  @return A string representing the name of this concept function.
     */
    @Override
    public String toString() {
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the output of the concept function based on the concept
     *  inputs. Derived classes must implement this method to provide
     *  the definition of the concept function.
     *  @param argValues The list of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error evaluating the function.
     */
    protected abstract Concept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of ontologies that specify the domain for
     *  each input argument to the concept function. The size of this
     *  list indicates the number of arguments for the function
     *  if it has a fixed argument list.
     */
    protected List<Ontology> _argumentDomainOntologies;

    /** The name of the concept function. */
    protected String _name;

    /** Flag that indicates whether or not the number of arguments for this
     *  concept function is fixed or variable.
     */
    protected boolean _numArgsIsFixed;

    /** The ontology that specifies the range of concepts that can be output
     *  by this concept function.
     */
    protected Ontology _outputRangeOntology;
}
