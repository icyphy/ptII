/* A helper class for ptolemy.actor.lib.MultiplyDivide.

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
package ptolemy.data.properties.lattice.dimensionSystem.actor.lib;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.dimensionSystem.actor.AtomicActor;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide

/**
 A helper class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit Leung
 @version $Id: MultiplyDivide.java 53046 2009-04-10 23:04:25Z cxh $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class MultiplyDivide extends AtomicActor {

    /**
     * Construct a MultiplyDivide helper for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given MultiplyDivide actor
     * @exception IllegalActionException
     */
    public MultiplyDivide(PropertyConstraintSolver solver,
            ptolemy.actor.lib.MultiplyDivide actor)
            throws IllegalActionException {

        super(solver, actor, false);
     }

    public List<Inequality> constraintList()
            throws IllegalActionException {
        ptolemy.actor.lib.MultiplyDivide actor = 
            (ptolemy.actor.lib.MultiplyDivide) getComponent();

//        if (actor.multiply.getWidth() != 1 || actor.divide.getWidth() != 1) {
//            throw new IllegalActionException(actor, "The property analysis " +
//            		"currently supports only binary division (e.g. exactly 1 " +
//            		"connection to the multiply port and 1 connection " +
//            		"to the divide port.");
//        }
        
        setAtLeast(actor.output, new FunctionTerm(actor.multiply, actor.divide));
        
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
            
            Property multiplyProperty = (Property) getSolver().getProperty(_multiply);
            Property divideProperty = (Property) getSolver().getProperty(_divide);

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
            return _lattice.getElement("TOP");
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] {
                getPropertyTerm(_multiply),
                getPropertyTerm(_divide)
            };
        }
    }
}
