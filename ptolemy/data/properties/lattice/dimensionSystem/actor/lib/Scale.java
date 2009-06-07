/* A helper class for ptolemy.actor.lib.Scale.

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
import ptolemy.data.expr.Parameter;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.dimensionSystem.actor.AtomicActor;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Scale

/**
 A helper class for ptolemy.actor.lib.Scale.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Scale extends AtomicActor {

    /**
     * Construct a Scale helper for the dimensionSystem lattice.
     * @param solver The given solver.
     * @param actor The given Scale actor.
     * @exception IllegalActionException
     */
    public Scale(PropertyConstraintSolver solver,
            ptolemy.actor.lib.Scale actor)
            throws IllegalActionException {

        super(solver, actor, false);
     }

    public List<Inequality> constraintList()
            throws IllegalActionException {
        ptolemy.actor.lib.Scale actor = 
            (ptolemy.actor.lib.Scale) getComponent();

        setAtLeast(actor.output, new OutputFunctionTerm(actor.input, actor.factor));
        setAtLeast(actor.input, new InputFunctionTerm(actor.output, actor.factor));
        
        return super.constraintList();
    }
    
    
    // Added by Charles Shelton 05/11/09:
    // The factor parameter for the Scale actor must be added to the list of
    // propertyable attributes in order for its property to be resolved.
    
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        result.add(((ptolemy.actor.lib.Scale) getComponent()).factor);
        return result;
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class OutputFunctionTerm extends MonotonicFunction {

        TypedIOPort _input;
        Parameter _factor;
        
        public OutputFunctionTerm(TypedIOPort input, Parameter parameter) {
            _input = input;
            _factor = parameter;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {
            
            Property inputProperty = (Property) getSolver().getProperty(_input);
            Property factorProperty = (Property) getSolver().getProperty(_factor);

            Property time = _lattice.getElement("TIME");
            Property position = _lattice.getElement("POSITION");
            Property speed = _lattice.getElement("SPEED");
            Property acceleration = _lattice.getElement("ACCELERATION");
            Property unitless = _lattice.getElement("UNITLESS");
            Property unknown = _lattice.getElement("UNKNOWN");
            Property top = _lattice.getElement("TOP");
            
            if ((inputProperty == speed && factorProperty == time) ||
                (inputProperty == time && factorProperty == speed)) {
                return position;
            }

            if ((inputProperty == acceleration && factorProperty == time) || 
                (inputProperty == time && factorProperty == acceleration)) {
                return speed;
            }

            if (inputProperty == unknown || factorProperty == unknown) {
                return unknown;
            }
            
            if (inputProperty == top || factorProperty == top) {
                return top;
            }
                
            if (factorProperty == unitless) {
                return inputProperty;
            }

            if (inputProperty == unitless) {
                return factorProperty;
            }
            
            return top;
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] {
                getPropertyTerm(_input),
                getPropertyTerm(_factor)
            };
        }
    }
    
    private class InputFunctionTerm extends MonotonicFunction {

        TypedIOPort _output;
        Parameter _factor;
        
        public InputFunctionTerm(TypedIOPort input, Parameter parameter) {
            _output = input;
            _factor = parameter;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {
            
            Property outputProperty = (Property) getSolver().getProperty(_output);
            Property factorProperty = (Property) getSolver().getProperty(_factor);

            Property time = _lattice.getElement("TIME");
            Property position = _lattice.getElement("POSITION");
            Property speed = _lattice.getElement("SPEED");
            Property acceleration = _lattice.getElement("ACCELERATION");
            Property unitless = _lattice.getElement("UNITLESS");
            Property unknown = _lattice.getElement("UNKNOWN");
            Property top = _lattice.getElement("TOP");
            
            if (outputProperty == speed && factorProperty == time) {
                return acceleration;
            }

            if (outputProperty == position && factorProperty == time) {
                return speed;
            }

            if (outputProperty == unknown || factorProperty == unknown) {
                return unknown;
            }
            
            if (outputProperty == top || factorProperty == top) {
                return top;
            }

            if (factorProperty == outputProperty) {
                return unitless;
            }
                
            if (factorProperty == unitless) {
                return outputProperty;
            }

            if (outputProperty == unitless) {
                return factorProperty;
            }
            
            return top;
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] {
                getPropertyTerm(_output),
                getPropertyTerm(_factor)
            };
        }
    }

}
