/* A concept function that applies a binary concept function to multiple
 * arguments in sequence.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ApplyBinaryFunctionToMultipleArguments

/** A concept function that applies a binary concept function to multiple
 *  arguments in sequence.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ApplyBinaryFunctionToMultipleArguments extends ConceptFunction {

    /** Create a new ApplyBinaryFunctionToMultipleArguments concept function
     *  for the specified binary concept function and ontology.
     *  @param name The name of this function.
     *  @param inputOutputOntology The ontology over which this function is
     *   defined.
     *  @param binaryFunction The binary concept function to be applied to the
     *   input arguments
     *  @exception IllegalActionException Thrown if the given binary concept function
     *   is not specified to have exactly 2 arguments.
     */
    public ApplyBinaryFunctionToMultipleArguments(String name,
            Ontology inputOutputOntology, ConceptFunction binaryFunction)
                    throws IllegalActionException {
        super(name, false, new LinkedList<Ontology>(), inputOutputOntology);

        if (!binaryFunction.isNumberOfArgumentsFixed()
                || binaryFunction.getNumberOfArguments() != 2) {
            throw new IllegalActionException("The specified binary concept "
                    + "function " + binaryFunction.getName() + "must require "
                    + "exactly 2 arguments.");
        }
        _binaryFunction = binaryFunction;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Evaluate the concept function for the given list of arguments. If the
     *  list of arguments is empty, return null.  If there is only one concept
     *  in the argument list, return that concept.  If there are 2 or more
     *  concepts in the argument list, apply the specified binary concept
     *  function sequentially to all input values, and return the final result.
     *  @param argValues The list of concept arguments for which the function
     *   will be evaluated.
     *  @return The output concept value;
     *  @exception IllegalActionException Thrown if there is an error calculating
     *   the output concept result.
     */
    @Override
    protected Concept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {

        if (argValues.isEmpty()) {
            return null;
        } else {
            Concept result = argValues.get(0);
            if (argValues.size() == 1) {
                return result;
            } else {
                for (int i = 1; i < argValues.size(); i++) {
                    List<Concept> binaryArgs = new ArrayList<Concept>(2);
                    binaryArgs.add(result);
                    binaryArgs.add(argValues.get(i));
                    result = _binaryFunction.evaluateFunction(binaryArgs);
                }

                return result;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The binary concept function to be applied to the given arguments for
     *  this concept function.
     */
    private ConceptFunction _binaryFunction;
}
