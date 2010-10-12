/* An adapter class for ptolemy.domains.continuous.lib.Integrator.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.dimensionSystem.domains.continuous.lib;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.adapters.dimensionSystem.DimensionSystemAdapter;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Integrator

/**
 An adapter class for ptolemy.domains.continuous.lib.Integrator.

 @author Man-Kit Leung, Ben Lickly
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Integrator extends DimensionSystemAdapter {

    /**
     * Construct a Integrator adapter for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given Integrator actor
     * @exception IllegalActionException If the adapter cannot be initialized.
     */
    public Integrator(LatticeOntologySolver solver,
            ptolemy.domains.continuous.lib.Integrator actor)
            throws IllegalActionException {

        super(solver, actor, false);
        _domainOntologiesList = new ArrayList<Ontology>(1);
        _domainOntologiesList.add(_dimensionSystemOntology);
    }

    /** Return the constraint list for the adapter.
     *  @throws IllegalActionException If there is an error building the constraint list.
     *  @return The list of constraints for this adapter.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.domains.continuous.lib.Integrator actor = (ptolemy.domains.continuous.lib.Integrator) getComponent();

        FunctionForStateOutput stateFunction = new FunctionForStateOutput();
        if (!stateFunction.isMonotonic()) {
            throw new IllegalActionException(
                    _solver,
                    "The concept function for determining the "
                            + "state output of the integrator is not monotonic. All concept functions used for a "
                            + "lattice ontology solver must be monotonic.");
        }

        FunctionForDerivativeInput derivativeFunction = new FunctionForDerivativeInput();
        if (!derivativeFunction.isMonotonic()) {
            throw new IllegalActionException(
                    _solver,
                    "The concept function for determining the "
                            + "derivative input of the integrator is not monotonic. All concept functions used for a "
                            + "lattice ontology solver must be monotonic.");
        }

        setAtLeast(actor.state, new ConceptFunctionInequalityTerm(
                stateFunction,
                new InequalityTerm[] { getPropertyTerm(actor.derivative) }));
        setSameAs(actor.state, actor.initialState);
        setAtLeast(actor.derivative, new ConceptFunctionInequalityTerm(
                derivativeFunction,
                new InequalityTerm[] { getPropertyTerm(actor.state) }));

        return super.constraintList();
    }
    
    /** The list of domain ontologies for the integrator's
     *  concept functions.
     */
    private List<Ontology> _domainOntologiesList;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class implements a monotonic function that takes in the
     *  input concept of the integrator (the derivative) and returns
     *  the output concept (the state).
     */
    private class FunctionForStateOutput extends ConceptFunction {

        /** Initialize this ConceptMonotonicFunction. It takes 1 concept argument
         *  and both the input and output should be from the DimensionSystem ontology.
         *  @throws IllegalActionException If the ConceptMonotonicFunction class cannot be initialized.
         */
        public FunctionForStateOutput() throws IllegalActionException {
            super("functionForStateOutput", true,
                    _domainOntologiesList,
                    _dimensionSystemOntology);
        }

        ///////////////////////////////////////////////////////////////
        ////                       protected inner methods            ////

        /** Return the function result.
         *  @param inputConceptValues The array of input arguments for the function.
         *  @return The concept value that is output from this function.
         *  @exception IllegalActionException If there is a problem evaluating the function.
         */
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {

            Concept derivativeProperty = inputConceptValues.get(0);

            if (derivativeProperty == _velocityConcept) {
                return _positionConcept;
            }

            if (derivativeProperty == _accelerationConcept) {
                return _velocityConcept;
            }

            // Interesting case: the integral of unitless value gives a time.
            if (derivativeProperty == _dimensionlessConcept) {
                return _timeConcept;
            }

            if (derivativeProperty == null
                    || derivativeProperty == _unknownConcept) {
                return _unknownConcept;
            } else {
                return _conflictConcept;
            }
        }
    }

    /** This class implements a monotonic function that takes in the
     *  output concept of the integrator (the state) and returns
     *  the input concept (the derivative).
     */
    private class FunctionForDerivativeInput extends ConceptFunction {

        /** Initialize this ConceptMonotonicFunction. It takes 1 concept argument
         *  and both the input and output should be from the DimensionSystem ontology.
         *  @throws IllegalActionException If the ConceptMonotonicFunction class cannot be initialized.
         */
        public FunctionForDerivativeInput() throws IllegalActionException {
            super("functionForDerivativeInput", true,
                    _domainOntologiesList,
                    _dimensionSystemOntology);
        }

        ///////////////////////////////////////////////////////////////
        ////                       protected inner methods            ////

        /** Return the function result.
         *  @param inputConceptValues The array of input arguments for the function.
         *  @return The concept value that is output from this function.
         *  @exception IllegalActionException If there is a problem evaluating the function.
         */
        protected Concept _evaluateFunction(List<Concept> inputConceptValues)
                throws IllegalActionException {

            Concept stateProperty = inputConceptValues.get(0);

            if (stateProperty == _positionConcept) {
                return _velocityConcept;
            }

            if (stateProperty == _velocityConcept) {
                return _accelerationConcept;
            }

            // Interesting case: the integral of unitless value gives a time.
            if (stateProperty == _timeConcept) {
                return _dimensionlessConcept;
            }

            if (stateProperty == null || stateProperty == _unknownConcept) {
                return _unknownConcept;
            } else {
                return _conflictConcept;
            }
        }
    }

}
