/* A helper class for ptolemy.actor.lib.ConfigurationSwitch

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
//// ConfigurationSwitch

/**
 A helper class for ptolemy.actor.lib.ConfgiurationSwitch.

 @author Charles Shelton
 @version $Id: ConfigurationSwitch.java 55837 2009-10-13 23:09:28Z cxh $
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ConfigurationSwitch extends AtomicActor {
    /**
     * Construct a BooleanSelect helper.
     * BooleanSelect does NOT use the default constraints
     * @param actor the associated actor
     * @throws IllegalActionException 
     */
    public ConfigurationSwitch(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.ConfigurationSwitch actor)
            throws IllegalActionException {
        super(solver, actor, false);
    }
    
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.ConfigurationSwitch actor = (ptolemy.actor.lib.ConfigurationSwitch) getComponent();

        // Rules for backward solver are determined by monotonic function
        setAtLeast(actor.input, new FunctionTerm(actor.trueOutput, actor.falseOutput, actor.selector));
        
        // Rules for forward solver are implemented here
        // Whichever output is selected is set to at least the input
        // The other output is set to NotConfigured
        
        if (actor.selector != null) {
            if (((BooleanToken) actor.selector.getToken()).booleanValue()) {
                setAtLeast(actor.trueOutput, actor.input);
                setAtLeast(actor.falseOutput, _lattice.getElement("NotConfigured"));
            } else {
                setAtLeast(actor.falseOutput, actor.input);
                setAtLeast(actor.trueOutput, _lattice.getElement("NotConfigured"));
            }        
        }
        
        // Input is determined by function below ("backward solver" rules)
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

        TypedIOPort _trueOutput;
        TypedIOPort _falseOutput;
        Parameter _control;

        public FunctionTerm(TypedIOPort trueOutput, TypedIOPort falseOutput, Parameter control) {
            _trueOutput = trueOutput;
            _falseOutput = falseOutput;
            _control = control;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Property trueOutputProperty = getSolver().getProperty(_trueOutput);
            Property falseOutputProperty = getSolver().getProperty(_falseOutput);
            
            // Rules for backward solver are implemented here
            // If control parameter is null, return NotSpecified
            // If the control parameter is set to a value, then set the input to the property
            // from the selected output (either trueOutput or falseOutput).

            if (_control != null) {
                if (((BooleanToken) _control.getToken()).booleanValue()) {
                    return trueOutputProperty;
                } else {
                    return falseOutputProperty;
                }
            }
            
            return _lattice.getElement("NotSpecified");
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_trueOutput), getPropertyTerm(_falseOutput) };
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
