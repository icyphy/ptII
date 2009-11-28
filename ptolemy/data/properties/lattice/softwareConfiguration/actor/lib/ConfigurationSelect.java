/* A helper class for ptolemy.actor.lib.ConfigurationSelect

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.softwareConfiguration.actor.lib;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.softwareConfiguration.actor.AtomicActor;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ConfigurationSelect

/**
 A helper class for ptolemy.actor.lib.ConfigurationSelect.

 @author Charles Shelton
 @version $Id: ConfigurationSelect.java 55837 2009-10-13 23:09:28Z cxh $
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ConfigurationSelect extends AtomicActor {
    /**
     * Construct a ConfigurationSelect helper.
     * ConfigurationSelect does NOT use the default constraints
     * @param solver The associated solver.
     * @param actor The associated actor.
     * @exception IllegalActionException If thrown by the super class.
     */
    public ConfigurationSelect(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.ConfigurationSelect actor)
            throws IllegalActionException {
        super(solver, actor, false);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the constraints of this component. The constraints are a list of
     * inequalities.
     * @return The constraints of this component.
     * @exception IllegalActionException If thrown while manipulating the lattice
     * or getting the solver.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.ConfigurationSelect actor = (ptolemy.actor.lib.ConfigurationSelect) getComponent();

        // Rules for forward solver are determined by monotonic function
        setAtLeast(actor.output, new FunctionTerm(actor.trueInput, actor.falseInput, actor.selector));
        
        // Rules for backward solver are implemented here
        // The selected input is at least the output 
        // No relation between the unselected input and the output
        
        if (actor.selector != null) {
            if (((BooleanToken) actor.selector.getToken()).booleanValue()) {
                setAtLeast(actor.trueInput, actor.output);
            } else {
                setAtLeast(actor.falseInput, actor.output);
            }        
        }
        
        // Output is determined by function below ("forward solver" rules)
        // Hopefully the forward solver + backward solver rules form a 
        // monotonic function when combined - I think they do.

        return super.constraintList();
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** 
     * A monotonic function of the input port type. The result of the
     * function is the same as the input type if is not Complex;
     * otherwise, the result is Double.
     */
    private class FunctionTerm extends MonotonicFunction {

        TypedIOPort _trueInput;
        TypedIOPort _falseInput;
        Parameter _control;

        public FunctionTerm(TypedIOPort trueInput, TypedIOPort falseInput, Parameter control) {
            _trueInput = trueInput;
            _falseInput = falseInput;
            _control = control;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Property trueInputProperty = getSolver().getProperty(_trueInput);
            Property falseInputProperty = getSolver().getProperty(_falseInput);
            
            // Rules for forward solver are implemented here
            // If control parameter is null, return NotSpecified
            // If the control parameter is set to a value, then the output the property
            // from the selected input (either trueInput or falseInput).

            if (_control != null) {
                if (((BooleanToken) _control.getToken()).booleanValue()) {
                    return trueInputProperty;
                } else {
                    return falseInputProperty;
                }
            }
            
            return _lattice.getElement("NotSpecified");
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_trueInput), getPropertyTerm(_falseInput) };
        }

        // Return true
        public boolean isEffective() {
            return true;
        }

        // FIXME:  What to do here?
        public void setEffective(boolean isEffective) {
            // TODO Auto-generated method stub            
        }
    }
}
