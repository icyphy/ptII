/* A helper class for ptolemy.domains.continuous.lib.Integrator.

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
 A helper class for ptolemy.domains.continuous.lib.Integrator.

 @author Man-Kit Leung
 @version $Id: Integrator.java 53046 2009-04-10 23:04:25Z cxh $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Integrator extends PropertyConstraintHelper {

    /**
     * Construct a Integrator helper for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given Integrator actor
     * @exception IllegalActionException
     */
    public Integrator(PropertyConstraintSolver solver,
            ptolemy.domains.continuous.lib.Integrator actor)
            throws IllegalActionException {

        super(solver, actor, false);
     }

    public List<Inequality> constraintList()
            throws IllegalActionException {
        ptolemy.domains.continuous.lib.Integrator actor = 
            (ptolemy.domains.continuous.lib.Integrator) getComponent();

        // TODO: write a monotonic function.
        setAtLeast(actor.state, new FunctionTerm(actor.derivative));
        setSameAs(actor.state, actor.initialState);

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class FunctionTerm extends MonotonicFunction {

        TypedIOPort _derivative;
        
        public FunctionTerm(TypedIOPort derivative) {
            _derivative = derivative;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {
            
            Property inputProperty = (Property) getSolver().getProperty(_derivative);

            if (inputProperty == _lattice.getElement("SPEED")) {
                return _lattice.getElement("POSITION");
            }

            if (inputProperty == _lattice.getElement("ACCELERATION")) {
                return _lattice.getElement("SPEED");
            }

            if (inputProperty == null || 
                    inputProperty == _lattice.getElement("UNKNOWN")) {
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
            return new InequalityTerm[] {
                getPropertyTerm(_derivative)
            };
        }
    }
}
