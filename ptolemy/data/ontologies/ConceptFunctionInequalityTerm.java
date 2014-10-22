/* A class for concept function inequality terms for ontology constraints.
 *
 * Copyright (c) 2010-2014 The Regents of the University of California. All
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

import java.util.ArrayList;
import java.util.List;

import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConceptFunctionInequalityTerm

/** An inequality term wrapper for concept functions that are used for
 *  ontology constraints. Use this class to set up inequality
 *  constraints between variables and concept functions.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConceptFunctionInequalityTerm implements InequalityTerm {

    /** Initialize the inequality term with the ConceptFunction it
     *  refers to and the array of inequality terms that are inputs to
     *  the function.
     *  @param conceptFunction The concept function to be called to
     *   get the value for this inequality term.
     *  @param inputTerms The array of inequality term inputs to the
     *  concept function.
     *  @exception IllegalActionException If the number of input terms
     *   does not match the required number of arguments for the
     *   concept function.
     */
    public ConceptFunctionInequalityTerm(ConceptFunction conceptFunction,
            InequalityTerm[] inputTerms) throws IllegalActionException {
        _conceptFunction = conceptFunction;

        _dependentTerms = new InequalityTerm[inputTerms.length];
        System.arraycopy(inputTerms, 0, _dependentTerms, 0, inputTerms.length);
        if (_dependentTerms == null) {
            throw new IllegalActionException(
                    "The inputTerms array cannot be null.");
        }

        if (_conceptFunction == null) {
            throw new IllegalActionException(
                    "The conceptFunction cannot be null.");
        } else if (_conceptFunction.isNumberOfArgumentsFixed()
                && _conceptFunction.getNumberOfArguments() != inputTerms.length) {
            throw new IllegalActionException(
                    "Wrong number of input arguments for the concept function "
                            + "contained by "
                            + "this inequality term. Input terms has "
                            + inputTerms.length + " elements "
                            + "but the concept function " + _conceptFunction
                            + " takes "
                            + _conceptFunction.getNumberOfArguments()
                            + " arguments.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the concept function associated with this concept
     *  function inequality term.
     *  @return The concept function evaluated by this concept
     *  function inequality term.
     */
    @Override
    public Object getAssociatedObject() {
        return _conceptFunction;
    }

    /** Return an array of constants contained in this term. Since
     *  this term represents a function, return an array containing
     *  all the Concept constants in the function.
     *  @return An array of InequalityTerms
     */
    public final InequalityTerm[] getConstants() {
        List<InequalityTerm> terms = new ArrayList<InequalityTerm>();

        for (InequalityTerm term : _dependentTerms) {
            if (!term.isSettable()) {
                terms.add(term);
            }
        }

        InequalityTerm[] array = new InequalityTerm[terms.size()];
        System.arraycopy(terms.toArray(), 0, array, 0, terms.size());

        return array;
    }

    /** Return the array of dependent terms contained by this term.  For a
     *  ConceptFunctionInequalityTerm, these terms represent the input
     *  arguments to the concept function in the order they are passed
     *  to the function.
     *  @return An array of InequalityTerms that represent the dependent terms.
     */
    public final InequalityTerm[] getDependentTerms() {
        InequalityTerm[] array = new InequalityTerm[_dependentTerms.length];
        System.arraycopy(_dependentTerms, 0, array, 0, _dependentTerms.length);

        return array;
    }

    /** Return the value of this inequality term. Since this term is
     *  for a concept function, return the evaluation of the concept
     *  function based on the current values of variables passed into
     *  the function.
     *  @return An Object representing an element in the underlying CPO.
     *  @exception IllegalActionException If the value of this
     *  inequality term is not valid.
     *  @see #setValue(Object)
     */
    @Override
    public Object getValue() throws IllegalActionException {
        List<Concept> inputConcepts = new ArrayList<Concept>();

        // Get the concept values currently held by all the input terms.
        for (InequalityTerm inputTerm : _dependentTerms) {
            inputConcepts.add((Concept) inputTerm.getValue());
        }

        // Return the current value of the function based on the
        // current input concepts.
        return _conceptFunction.evaluateFunction(inputConcepts);
    }

    /** Return the concept variables for this inequality term. This
     *  method returns an array of InequalityTerms that the concept
     *  function referred to by this ConceptFunctionInequalityTerm
     *  depends on.
     *  @return An array of InequalityTerms.
     */
    @Override
    public final InequalityTerm[] getVariables() {
        List<InequalityTerm> terms = new ArrayList<InequalityTerm>();

        for (InequalityTerm term : _dependentTerms) {
            if (term.isSettable()) {
                terms.add(term);
            }
        }

        InequalityTerm[] array = new InequalityTerm[terms.size()];
        System.arraycopy(terms.toArray(), 0, array, 0, terms.size());

        return array;
    }

    /** Throw an Exception. This method cannot be called on a
     *  function term.
     *  @param e The object value used to initialize the inequality
     *   term.  Since this method always throws an exception, this
     *   parameter is never used.
     *  @exception IllegalActionException Always thrown.
     */
    @Override
    public final void initialize(Object e) throws IllegalActionException {
        throw new IllegalActionException(getClass().getName()
                + ": Cannot initialize a function term.");
    }

    /** Return false.  Concept function terms are not settable.
     *  @return False.
     */
    @Override
    public final boolean isSettable() {
        return false;
    }

    /** Return true.  Function terms are, by default, always
     *  acceptable.
     *  @return True.
     */
    @Override
    public boolean isValueAcceptable() {
        return true;
    }

    /** Throw an Exception. The value of a function term cannot be set.
     *  @param e The object value to set the inequality term. Since
     *   this method always throws an exception, this parameter is
     *   never used.
     *  @exception IllegalActionException Always thrown.
     *  @see #getValue()
     */
    @Override
    public final void setValue(Object e) throws IllegalActionException {
        throw new IllegalActionException(getClass().getName()
                + ": The inequality term is not settable.");
    }

    /** Override the base class to give a description of this term.
     *  @return A description of this term.
     */
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer("(" + _conceptFunction + "(");

        int index = 0;
        for (InequalityTerm inputTerm : _dependentTerms) {
            output.append(inputTerm);
            if (index < _dependentTerms.length - 1) {
                output.append(", ");
            }
            index++;
        }
        output.append("), ");

        try {
            return output.toString() + getValue() + ")";
        } catch (IllegalActionException ex) {
            return output.toString() + "INVALID" + ")";
        }
    }

    /** The concept function to be evaluated for the inequality term. */
    private ConceptFunction _conceptFunction;

    /** The array of inequality terms which are inputs to concept
     * function.
     */
    private InequalityTerm[] _dependentTerms;
}
