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
//// BooleanSelect

/**
 A helper class for ptolemy.actor.lib.BooleanSelect.

 @author Charles Shelton
 @version $Id: ConfigurationSelect.java 55837 2009-10-13 23:09:28Z cxh $
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ConfigurationSelect extends AtomicActor {
    /**
     * Construct a BooleanSelect helper.
     * BooleanSelect does NOT use the default constraints
     * @param actor the associated actor
     * @throws IllegalActionException 
     */
    public ConfigurationSelect(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.ConfigurationSelect actor)
            throws IllegalActionException {
        super(solver, actor, false);
    }
    
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.ConfigurationSelect actor = (ptolemy.actor.lib.ConfigurationSelect) getComponent();

        // Rules for forward solver are determined by monotonic function
        setAtLeast(actor.output, new FunctionTerm(actor.trueInput, actor.falseInput, actor.selector));
        
        // Rules for backward solver are implemented here
        // Control input is at least the output 
        // No relation between the data inputs and the output
        // (because, maybe only one of them is used.  In this case
        // the other one can be anything.)
        //setAtLeast(actor.selector, actor.output);
        
        if (actor.selector != null) {
            if (((BooleanToken) actor.selector.getToken()).booleanValue()) {
                setAtLeast(actor.trueInput, actor.output);
                //setAtLeast(actor.falseInput, _lattice.getElement("NotConfigured"));
            } else {
                setAtLeast(actor.falseInput, actor.output);
                //setAtLeast(actor.falseInput, _lattice.getElement("NotConfigured"));
            }        
        }
        
        // Output is determined by function below ("forward solver" rules)
        // Hopefully the forward solver + backward solver rules form a 
        // monotonic function when combined - I think they do.

        return super.constraintList();
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
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
            //Property controlProperty = getSolver().getProperty(_control);
            
            // Rules for forward solver are implemented here
            // If control property is null, return NotSpecified
            // If control is NotConfigured, then output is at least NotConfigured
            // (interpreted as, the whole BooleanSelect actor is not configured)
            // If contol is Conflict, then the output is at least Conflict
            // (interpreted as, there is a problem with the BooleanSelect actor)
            // Otherwise,
            // If at least one input is Configured, then the output is at least Configured
            // (here we assume that the BooleanSelect is using the Configured input -
            //  an improvement would be to see if the control is Configured and Const,
            //  and then use 

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
