/* The default adapter class for ptolemy.actor.lib.MultiplyDivide.

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
package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionDefinitionAttribute;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.lattice.ApplyBinaryFunctionToMultipleArguments;
import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MultiplyDivide

/** The default adapter class for ptolemy.actor.lib.MultiplyDivide.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class MultiplyDivide extends LatticeOntologyAdapter {

    /** Construct a default lattice ontology adapter for the MultiplyDivide actor.
     *  @param solver The given solver.
     *  @param actor The given MultiplyDivide actor.
     *  @exception IllegalActionException If the adapter cannot be initialized.
     */
    public MultiplyDivide(LatticeOntologySolver solver,
            ptolemy.actor.lib.MultiplyDivide actor)
            throws IllegalActionException {
        super(solver, actor, false);

        _multiplyDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.MULTIPLY_FUNCTION_NAME);
        _divideDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.DIVIDE_FUNCTION_NAME);
        _reciprocalDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.RECIPROCAL_FUNCTION_NAME);

        // If definitions for multiplication, division, and reciprocal concept
        // functions cannot be found, just use the default constraints.
        if (_multiplyDefinition == null || _divideDefinition == null
                || _reciprocalDefinition == null) {
            _useDefaultConstraints = true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of constraints for the MultiplyDivide actor.
     *  @return The list of constraints for this adapter.
     *  @exception IllegalActionException If there is an error creating
     *   the constraint list.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        ConceptFunction multiplyFunction = null;
        if (_multiplyDefinition != null) {
            multiplyFunction = _multiplyDefinition.createConceptFunction();
        }

        ConceptFunction divideFunction = null;
        if (_divideDefinition != null) {
            divideFunction = _divideDefinition.createConceptFunction();
        }

        ConceptFunction reciprocalFunction = null;
        if (_reciprocalDefinition != null) {
            reciprocalFunction = _reciprocalDefinition.createConceptFunction();
        }

        if (multiplyFunction != null && reciprocalFunction != null
                && divideFunction != null) {
            if (interconnectConstraintType == ConstraintType.EQUALS
                    || interconnectConstraintType == ConstraintType.SINK_GE_SOURCE) {
                List<IOPort> multiplyInputs = _getSourcePortList(actor.multiply);

                // If the multiply input is a multiport with multiple input ports,
                // set up the constraint to be the product of the inputs.
                if (multiplyInputs.size() > 1) {
                    InequalityTerm[] plusTerms = new InequalityTerm[multiplyInputs
                            .size()];
                    for (int i = 0; i < plusTerms.length; i++) {
                        plusTerms[i] = getPropertyTerm(multiplyInputs.get(i));
                    }
                    setAtLeast(
                            actor.multiply,
                            new ConceptFunctionInequalityTerm(
                                    new ApplyBinaryFunctionToMultipleArguments(
                                            "productMultiplyInputs", _solver
                                                    .getOntology(),
                                            multiplyFunction), plusTerms));
                }

                // If the divide input is a multiport with multiple input ports,
                // set up the constraint to be the product of the inputs.
                List<IOPort> divideInputs = _getSourcePortList(actor.divide);
                if (divideInputs.size() > 1) {
                    InequalityTerm[] divideTerms = new InequalityTerm[divideInputs
                            .size()];
                    for (int i = 0; i < divideTerms.length; i++) {
                        divideTerms[i] = getPropertyTerm(divideInputs.get(i));
                    }
                    setAtLeast(
                            actor.divide,
                            new ConceptFunctionInequalityTerm(
                                    new ApplyBinaryFunctionToMultipleArguments(
                                            "productDivideInputs", _solver
                                                    .getOntology(),
                                            multiplyFunction), divideTerms));
                }

                // If the divide input port is unconnected, then the output
                // is >= multiply input.
                if (divideInputs.size() == 0) {
                    setAtLeast(actor.output, actor.multiply);

                    // If the multiply input port is unconnected, then the output
                    // is >= reciprocal of the divide input.
                } else if (multiplyInputs.size() == 0) {
                    setAtLeast(
                            actor.output,
                            new ConceptFunctionInequalityTerm(
                                    reciprocalFunction,
                                    new InequalityTerm[] { getPropertyTerm(actor.divide) }));

                    // Otherwise the output is >= multiply input / divide input.
                } else {
                    setAtLeast(actor.output, new ConceptFunctionInequalityTerm(
                            divideFunction, new InequalityTerm[] {
                                    getPropertyTerm(actor.multiply),
                                    getPropertyTerm(actor.divide) }));
                }
            }
        }

        // Add back in default constraints for the output to input relationship.
        if (!_useDefaultConstraints
                && (interconnectConstraintType == ConstraintType.EQUALS || interconnectConstraintType == ConstraintType.SOURCE_GE_SINK)) {
            setAtLeast(actor.multiply, actor.output);
            setAtLeast(actor.divide, actor.output);
        }

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The division concept function definition found in the solver model. */
    private ConceptFunctionDefinitionAttribute _divideDefinition;

    /** The multiplication concept function definition found in the solver model. */
    private ConceptFunctionDefinitionAttribute _multiplyDefinition;

    /** The reciprocal concept function definition found in the solver model. */
    private ConceptFunctionDefinitionAttribute _reciprocalDefinition;
}
