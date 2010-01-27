/* An adapter class for ptolemy.actor.lib.MultiplyDivide.

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
package ptolemy.data.ontologies.lattice.adapters.DimensionSystem.actor.lib;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.dimensionSystem.actor.AtomicActor;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MultiplyDivide

/**
 An adapter class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class MultiplyDivide extends AtomicActor {

    /**
     * Construct a MultiplyDivide adapter for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given MultiplyDivide actor
     * @exception IllegalActionException
     */
    public MultiplyDivide(PropertyConstraintSolver solver,
            ptolemy.actor.lib.MultiplyDivide actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        if (actor.multiply.getWidth() != 1 || actor.divide.getWidth() != 1) {
            throw new IllegalActionException(
                    actor,
                    "The property analysis "
                            + "currently supports only binary division (e.g. exactly 1 "
                            + "connection to the multiply port and 1 connection "
                            + "to the divide port.");
        }

        setAtLeast(actor.output, new FunctionTerm(actor.multiply, actor.divide));
        setAtLeast(actor.multiply, new MultiplyFunctionTerm(actor.output,
                actor.divide));
        setAtLeast(actor.divide, new DivideFunctionTerm(actor.output,
                actor.multiply));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class FunctionTerm extends MonotonicFunction {

        TypedIOPort _multiply;
        TypedIOPort _divide;

        public FunctionTerm(TypedIOPort multiply, TypedIOPort divide) {
            _multiply = multiply;
            _divide = divide;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Property multiplyProperty = getSolver().getProperty(_multiply);
            Property divideProperty = getSolver().getProperty(_divide);

            Property time = _lattice.getElement("TIME");
            Property position = _lattice.getElement("POSITION");
            Property speed = _lattice.getElement("SPEED");
            Property acceleration = _lattice.getElement("ACCELERATION");
            Property unitless = _lattice.getElement("UNITLESS");
            Property unknown = _lattice.getElement("UNKNOWN");

            if (multiplyProperty == speed && divideProperty == time) {
                return acceleration;
            }

            if (multiplyProperty == position && divideProperty == time) {
                return speed;
            }

            if (multiplyProperty == position && divideProperty == speed) {
                return time;
            }

            if (multiplyProperty == speed && divideProperty == acceleration) {
                return time;
            }

            if (divideProperty == unitless) {
                return multiplyProperty;
            }

            if (multiplyProperty == unknown || divideProperty == unknown) {
                return unknown;
            }
            if (multiplyProperty == divideProperty) {
                return unitless;
            }
            return _lattice.getElement("TOP");
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_multiply),
                    getPropertyTerm(_divide) };
        }
    }

    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class MultiplyFunctionTerm extends MonotonicFunction {

        TypedIOPort _output;
        TypedIOPort _divide;

        public MultiplyFunctionTerm(TypedIOPort output, TypedIOPort divide) {
            _output = output;
            _divide = divide;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Property outputProperty = getSolver().getProperty(_output);
            Property divideProperty = getSolver().getProperty(_divide);

            Property time = _lattice.getElement("TIME");
            Property position = _lattice.getElement("POSITION");
            Property speed = _lattice.getElement("SPEED");
            Property acceleration = _lattice.getElement("ACCELERATION");
            Property unitless = _lattice.getElement("UNITLESS");
            Property unknown = _lattice.getElement("UNKNOWN");

            if (outputProperty == acceleration && divideProperty == time) {
                return speed;
            }

            if (outputProperty == speed && divideProperty == time) {
                return position;
            }

            if (outputProperty == time && divideProperty == speed) {
                return position;
            }

            if (outputProperty == time && divideProperty == acceleration) {
                return speed;
            }

            if (divideProperty == unitless) {
                return outputProperty;
            }

            if (outputProperty == unitless) {
                return divideProperty;
            }

            if (outputProperty == unknown || divideProperty == unknown) {
                return unknown;
            }

            if (outputProperty == divideProperty) {
                return unitless;
            }

            return _lattice.getElement("TOP");
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_output),
                    getPropertyTerm(_divide) };
        }
    }

    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class DivideFunctionTerm extends MonotonicFunction {

        TypedIOPort _output;
        TypedIOPort _multiply;

        public DivideFunctionTerm(TypedIOPort output, TypedIOPort multiply) {
            _output = output;
            _multiply = multiply;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Property outputProperty = getSolver().getProperty(_output);
            Property multiplyProperty = getSolver().getProperty(_multiply);

            Property time = _lattice.getElement("TIME");
            Property position = _lattice.getElement("POSITION");
            Property speed = _lattice.getElement("SPEED");
            Property acceleration = _lattice.getElement("ACCELERATION");
            Property unitless = _lattice.getElement("UNITLESS");
            Property unknown = _lattice.getElement("UNKNOWN");

            if (outputProperty == acceleration && multiplyProperty == speed) {
                return time;
            }

            if (outputProperty == speed && multiplyProperty == position) {
                return time;
            }

            if (outputProperty == time && multiplyProperty == position) {
                return speed;
            }

            if (outputProperty == time && multiplyProperty == speed) {
                return acceleration;
            }

            if (multiplyProperty == unitless) {
                return outputProperty;
            }

            if (outputProperty == unitless) {
                return multiplyProperty;
            }

            if (outputProperty == unknown || multiplyProperty == unknown) {
                return unknown;
            }

            if (outputProperty == multiplyProperty) {
                return unitless;
            }

            return _lattice.getElement("TOP");
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_output),
                    getPropertyTerm(_multiply) };
        }
    }
}
