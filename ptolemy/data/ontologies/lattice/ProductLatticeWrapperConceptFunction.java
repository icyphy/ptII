/** A wrapper concept function to create a new derived concept function
 *  for a product lattice ontology from one of its component ontologies.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
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

package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeWrapperConceptFunction

/** A wrapper concept function to create a new derived concept function
 *  for a product lattice ontology from one of its component ontologies.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeWrapperConceptFunction extends ConceptFunction {

    /** Create a new ProductLatticeWrapperConceptFunction with the given name,
     *  derived from the given original ConceptFunction.
     *  @param name The name of the wrapper concept function.
     *  @param inputOutputOntology The ProductLatticeOntology for the wrapper function.
     *  @param originalFunctionOntology The original ontology for the original concept function.
     *  @param originalFunction The original concept function.
     *  @exception IllegalActionException Thrown if the wrapper concept function
     *   cannot be created.
     */
    public ProductLatticeWrapperConceptFunction(String name,
            ProductLatticeOntology inputOutputOntology,
            Ontology originalFunctionOntology, ConceptFunction originalFunction)
            throws IllegalActionException {
        super(name, originalFunction.getNumberOfArguments(),
                inputOutputOntology);

        _originalFunctionOntology = originalFunctionOntology;
        _originalFunction = originalFunction;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the output of the concept function based on the concept
     *  inputs. Since the inputs and outputs to this function must be
     *  in the ProductLatticeOntology, the inputs must be shifted
     *  to the original concept function's domain, call the original concept
     *  function to get the output value, then shift that output concept
     *  back to the product lattice ontology domain.
     *  @param argValues The list of concept inputs to the function.
     *  @return The concept output result of the function.
     *  @exception IllegalActionException If there is an error evaluating the function.
     */
    @Override
    protected Concept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {

        List<Concept> originalArgs = new ArrayList<Concept>();
        for (Concept arg : argValues) {
            if (arg == null) {
                return null;
            }
            originalArgs.add(ProductLatticeOntologyAdapter
                    .getComponentConceptFromProductLatticeConcept(arg,
                            _originalFunctionOntology));
        }

        Concept originalFunctionValue = _originalFunction
                .evaluateFunction(originalArgs);
        return ProductLatticeOntologyAdapter
                .getDerivedConceptForProductLattice(originalFunctionValue,
                        (ProductLatticeOntology) _outputRangeOntology);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The original ontology from which the concept function was taken. */
    private Ontology _originalFunctionOntology;

    /** The original concept function for which this function is a wrapper. */
    private ConceptFunction _originalFunction;
}
