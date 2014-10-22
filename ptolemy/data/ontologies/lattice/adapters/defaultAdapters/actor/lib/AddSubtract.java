/* The default adapter class for ptolemy.actor.lib.AddSubtract.

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
//// AddSubtract

/** The default adapter class for ptolemy.actor.lib.AddSubtract.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class AddSubtract extends LatticeOntologyAdapter {

    /** Construct a default lattice ontology adapter for the AddSubtract actor.
     *  @param solver The given solver.
     *  @param actor The given AddSubtract actor.
     *  @exception IllegalActionException If the adapter cannot be initialized.
     */
    public AddSubtract(LatticeOntologySolver solver,
            ptolemy.actor.lib.AddSubtract actor) throws IllegalActionException {
        super(solver, actor, false);

        _addDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.ADD_FUNCTION_NAME);
        _negateDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.NEGATE_FUNCTION_NAME);
        _subtractDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.SUBTRACT_FUNCTION_NAME);

        // If definitions for addition, subtraction, and negation concept
        // functions cannot be found, just use the default constraints.
        if (_addDefinition == null || _negateDefinition == null
                || _subtractDefinition == null) {
            _useDefaultConstraints = true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of constraints for the AddSubtract actor.
     *  @return The list of constraints for this adapter.
     *  @exception IllegalActionException If there is an error creating
     *   the constraint list.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();

        ConceptFunction addFunction = null;
        if (_addDefinition != null) {
            addFunction = _addDefinition.createConceptFunction();
        }

        ConceptFunction negateFunction = null;
        if (_negateDefinition != null) {
            negateFunction = _negateDefinition.createConceptFunction();
        }

        ConceptFunction subtractFunction = null;
        if (_subtractDefinition != null) {
            subtractFunction = _subtractDefinition.createConceptFunction();
        }

        if (addFunction != null && negateFunction != null
                && subtractFunction != null) {
            if (interconnectConstraintType == ConstraintType.EQUALS
                    || interconnectConstraintType == ConstraintType.SINK_GE_SOURCE) {

                // If the plus input is a multiport with multiple input ports,
                // set up the constraint to be the sum of the inputs.
                List<IOPort> plusInputs = _getSourcePortList(actor.plus);
                if (plusInputs.size() > 1) {
                    InequalityTerm[] plusTerms = new InequalityTerm[plusInputs
                            .size()];
                    for (int i = 0; i < plusTerms.length; i++) {
                        plusTerms[i] = getPropertyTerm(plusInputs.get(i));
                    }
                    setAtLeast(actor.plus, new ConceptFunctionInequalityTerm(
                            new ApplyBinaryFunctionToMultipleArguments(
                                    "sumPlusInputs", _solver.getOntology(),
                                    addFunction), plusTerms));
                }

                // If the minus input is a multiport with multiple input ports,
                // set up the constraint to be the sum of the inputs.
                List<IOPort> minusInputs = _getSourcePortList(actor.minus);
                if (minusInputs.size() > 1) {
                    InequalityTerm[] minusTerms = new InequalityTerm[minusInputs
                            .size()];
                    for (int i = 0; i < minusTerms.length; i++) {
                        // Coverity identified a copy paste error here:
                        //minusTerms[i] = getPropertyTerm(plusInputs.get(i));
                        minusTerms[i] = getPropertyTerm(minusInputs.get(i));
                    }
                    setAtLeast(actor.minus, new ConceptFunctionInequalityTerm(
                            new ApplyBinaryFunctionToMultipleArguments(
                                    "sumMinusInputs", _solver.getOntology(),
                                    addFunction), minusTerms));
                }

                // If the minus input port is unconnected, then the output
                // is >= plus input.
                if (minusInputs.size() == 0) {
                    setAtLeast(actor.output, actor.plus);

                    // If the plus input port is unconnected, then the output
                    // is >= negation of the minus input.
                } else if (plusInputs.size() == 0) {
                    setAtLeast(
                            actor.output,
                            new ConceptFunctionInequalityTerm(
                                    negateFunction,
                                    new InequalityTerm[] { getPropertyTerm(actor.minus) }));

                    // Otherwise the output is >= multiply input / divide input.
                } else {
                    setAtLeast(actor.output, new ConceptFunctionInequalityTerm(
                            subtractFunction, new InequalityTerm[] {
                                    getPropertyTerm(actor.plus),
                                    getPropertyTerm(actor.minus) }));
                }
            }
        }

        // Add back in default constraints for the output to input relationship.
        if (!_useDefaultConstraints
                && (interconnectConstraintType == ConstraintType.EQUALS || interconnectConstraintType == ConstraintType.SOURCE_GE_SINK)) {
            setAtLeast(actor.plus, actor.output);
            setAtLeast(actor.minus, actor.output);
        }

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The multiplication concept function definition found in the solver model. */
    private ConceptFunctionDefinitionAttribute _addDefinition;

    /** The division concept function definition found in the solver model. */
    private ConceptFunctionDefinitionAttribute _negateDefinition;

    /** The multiplication concept function definition found in the solver model. */
    private ConceptFunctionDefinitionAttribute _subtractDefinition;
}
