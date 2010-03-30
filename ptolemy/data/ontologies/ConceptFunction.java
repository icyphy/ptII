/*
 * A base class representing a concept function for ontology constraints.
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

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConceptFunction

/** This is the base class for concept functions that are used
 *  for ontology constraints. Derived classes must implement the
 *  evaluateFunction method to provide the output concept given an
 *  input array of concepts.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public abstract class ConceptFunction {

    /** Initialize the concept function with the number of arguments it takes
     *  and the ontologies from which input and output concepts can be taken.
     *  @param name The name of the concept function.
     *  @param numArgs The number of arguments to be passed into this function.
     *  @param argumentDomainOntologies The array of ontologies that
     *   represent the concept domain for each input concept argument.
     *  @param outputRangeOntology The ontology that represents the
     *   range of output concepts for this concept function.
     *  @exception IllegalActionException If the ontology inputs are null
     *   or the length of the array of domain ontologies does not
     *   match the number of arguments for the function.
     */
    public ConceptFunction(String name, int numArgs,
            Ontology[] argumentDomainOntologies, Ontology outputRangeOntology)
            throws IllegalActionException {
        _name = name;
        _numArgs = numArgs;
        _argumentDomainOntologies = argumentDomainOntologies;
        _outputRangeOntology = outputRangeOntology;

        if (_outputRangeOntology == null) {
            throw new IllegalActionException(
                    "The outputRangeOntology cannot be null.");
        }

        if (_argumentDomainOntologies == null) {
            throw new IllegalActionException(
                    "The argumentDomainOntologies cannot be null.");
        } else if (_argumentDomainOntologies.length != numArgs) {
            throw new IllegalActionException(
                    "The size of the array of domain ontologies for the concept "
                            + "function's argument array does not match the given "
                            + "number of arguments for the concept "
                            + "function " + this + ": numArgs = " + _numArgs
                            + ", size of domain ontologies array = "
                            + _argumentDomainOntologies.length);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the output of the concept function based on the concept inputs.
     *  @param inputArgumentArray The array of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error with
     *   the input argument array or evaluating the function.
     */
    public Concept evaluateFunction(Concept[] inputArgumentArray)
            throws IllegalActionException {
        if (inputArgumentArray == null) {
            throw new IllegalActionException(
                    "The input array to the ConceptFunction " + this
                            + " is null.");
        } else if (inputArgumentArray.length != _numArgs) {
            throw new IllegalActionException(
                    "The input array to the ConceptFunction "
                            + this
                            + " has the "
                            + "wrong number of arguments. Expected number of arguments: "
                            + _numArgs + ", size of the input argument array: "
                            + inputArgumentArray.length);
        } else {
            // Check each concept argument value to make sure it is either null or
            // contained in the ontology domain for that argument.
            int index = 0;
            for (Concept argument : inputArgumentArray) {
                if (argument != null
                        && !_argumentDomainOntologies[index].entityList(
                                Concept.class).contains(argument)) {
                    throw new IllegalActionException("The input value "
                            + argument + " at argument index " + index
                            + " to the ConceptFunction " + this
                            + " is not within the expected domain ontology "
                            + _argumentDomainOntologies[index] + ".");
                }
                index++;
            }
        }

        Concept outputValue = _evaluateFunction(inputArgumentArray);

        // Check that the output is either null or in the output range ontology
        if (outputValue != null
                && !_outputRangeOntology.entityList(Concept.class).contains(
                        outputValue)) {
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
    public Ontology[] getArgumentDomainOntologies() {
        return _argumentDomainOntologies;
    }

    /** Return the name of the concept function.
     *  @return The name of the concept function.
     */
    public String getName() {
        return _name;
    }

    /** Return the number of input arguments for this concept function. 
     *  @return The number of arguments taken as input for this function.
     */
    public int getNumberOfArguments() {
        return _numArgs;
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

    /** Return a string representing the name of this concept function.
     *  @return A string representing the name of this concept function.
     */
    public String toString() {
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the output of the concept function based on the concept
     *  inputs. Derived classes must implement this method to provide
     *  the definition of the concept function.
     *  @param inputConceptValues The array of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error evaluating the function.
     */
    protected abstract Concept _evaluateFunction(Concept[] inputConceptValues)
            throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The array of ontologies that specify the domain for
     *  each input argument to the concept function.
     */
    protected Ontology[] _argumentDomainOntologies;

    /** The name of the concept function. */
    protected String _name;

    /** The number of arguments for this concept function. */
    protected int _numArgs;

    /** The ontology that specifies the range of concepts that can be output
     *  by this concept function.
     */
    protected Ontology _outputRangeOntology;
}
