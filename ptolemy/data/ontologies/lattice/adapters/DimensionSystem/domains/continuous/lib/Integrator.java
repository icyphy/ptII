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
package ptolemy.data.ontologies.lattice.adapters.DimensionSystem.domains.continuous.lib;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.adapters.DimensionSystem.DimensionSystemAdapter;
import ptolemy.data.properties.lattice.MonotonicFunction;
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
     * @exception IllegalActionException
     */
    public Integrator(LatticeOntologySolver solver,
            ptolemy.domains.continuous.lib.Integrator actor)
            throws IllegalActionException {

        super(solver, actor, false);

    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.domains.continuous.lib.Integrator actor = (ptolemy.domains.continuous.lib.Integrator) getComponent();

        setAtLeast(actor.state, new StateOfDerivative(actor.derivative));
        setSameAs(actor.state, actor.initialState);
        setAtLeast(actor.derivative, new DerivativeOfState(actor.state));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function that takes in the
    // input property of the integrator (the derivative) and returns
    // the output property (the state).
    private class StateOfDerivative extends MonotonicFunction {

        TypedIOPort _derivative;

        public StateOfDerivative(TypedIOPort derivative) {
            _derivative = derivative;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Concept inputProperty = getSolver().getProperty(_derivative);

            if (inputProperty == _velocityConcept) {
                return _positionConcept;
            }

            if (inputProperty == _accelerationConcept) {
                return _velocityConcept;
            }

            // Interesting case: the integral of unitless value gives a time.
            if (inputProperty == _dimensionlessConcept) {
                return _timeConcept;
            }

            if (inputProperty == null || inputProperty == _unknownConcept) {
                return _unknownConcept;
            } else {
                return _conflictConcept;
            }
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_derivative) };
        }
    }

    // This class implements a monotonic function that takes in the
    // input property of the integrator (the derivative) and returns
    // the output property (the state).
    private class DerivativeOfState extends MonotonicFunction {

        TypedIOPort _state;

        public DerivativeOfState(TypedIOPort state) {
            _state = state;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Concept inputProperty = getSolver().getProperty(_state);

            if (inputProperty == _positionConcept) {
                return _velocityConcept;
            }

            if (inputProperty == _velocityConcept) {
                return _accelerationConcept;
            }

            // Interesting case: the integral of unitless value gives a time.
            if (inputProperty == _timeConcept) {
                return _dimensionlessConcept;
            }

            if (inputProperty == null || inputProperty == _unknownConcept) {
                return _unknownConcept;
            } else {
                return _conflictConcept;
            }
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_state) };
        }
    }

}
