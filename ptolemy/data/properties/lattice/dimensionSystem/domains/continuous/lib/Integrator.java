/* An adapter class for ptolemy.domains.continuous.lib.Integrator.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.dimensionSystem.domains.continuous.lib;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Integrator

/**
 An adapter class for ptolemy.domains.continuous.lib.Integrator.

 @author Man-Kit Leung, Ben Lickly
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Integrator extends PropertyConstraintHelper {

    /**
     * Construct a Integrator adapter for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given Integrator actor
     * @exception IllegalActionException
     */
    public Integrator(PropertyConstraintSolver solver,
            ptolemy.domains.continuous.lib.Integrator actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.domains.continuous.lib.Integrator actor =
          (ptolemy.domains.continuous.lib.Integrator) getComponent();

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

            Property inputProperty = getSolver().getProperty(_derivative);

            if (inputProperty == _lattice.getElement("SPEED")) {
                return _lattice.getElement("POSITION");
            }

            if (inputProperty == _lattice.getElement("ACCELERATION")) {
                return _lattice.getElement("SPEED");
            }

            // Interesting case: the integral of unitless value gives a time.
            if (inputProperty == _lattice.getElement("UNITLESS")) {
                return _lattice.getElement("TIME");
            }

            if (inputProperty == null
                    || inputProperty == _lattice.getElement("UNKNOWN")) {
                return _lattice.getElement("UNKNOWN");
            } else {
                return _lattice.getElement("TOP");
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

            Property inputProperty = getSolver().getProperty(_state);

            if (inputProperty == _lattice.getElement("POSITION")) {
                return _lattice.getElement("SPEED");
            }

            if (inputProperty == _lattice.getElement("SPEED")) {
                return _lattice.getElement("ACCELERATION");
            }

            // Interesting case: the integral of unitless value gives a time.
            if (inputProperty == _lattice.getElement("TIME")) {
                return _lattice.getElement("UNITLESS");
            }

            if (inputProperty == null
                    || inputProperty == _lattice.getElement("UNKNOWN")) {
                return _lattice.getElement("UNKNOWN");
            } else {
                return _lattice.getElement("TOP");
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
