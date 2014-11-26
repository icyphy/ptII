/* A State space model simulator.
 
 Copyright (c) 2009-2014 The Regents of the University of California.
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
package org.ptolemy.ssm;

import java.util.HashMap; 

import org.ptolemy.machineLearning.particleFilter.AbstractParticleFilter; 
import org.ptolemy.machineLearning.particleFilter.AbstractStateSpaceSimulator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
///////////////////////////////////////////////////////////////////
////ParticleFilterSSM

/**
An actor that simulates a state space model that decorates itself.
Defines an initial value for the state and simulates the model from there.

@author Ilge Akkaya 
@version $Id$
@since Ptolemy II 10.1
@Pt.ProposedRating Red (ilgea)
@Pt.AcceptedRating
*/
public class StateSpaceSimulator extends AbstractStateSpaceSimulator implements StateSpaceActor {

    public StateSpaceSimulator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _decorator = null;  
    } 

    public StateSpaceSimulator(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace); 
        _decorator = null; 
    } 



    /** Check the dimensions of all parameters and ports.
     *  @exception IllegalActionException If the dimensions are illegal.
     */
    @Override
    protected void _checkParameters() throws IllegalActionException {
        // Check state variable names. 

        if (validUniqueDecoratorAssociationExists()) { 
            Parameter stateVariableNames = 
                    (Parameter) this.getDecoratorAttribute(_decorator, STATE_VARIABLE_NAMES);
            _stateNames = (ArrayToken) stateVariableNames.getToken();
            int n = _stateNames.length(); 
            if (n < 1) {
                throw new IllegalActionException(this, "There must be at "
                        + "least one state variable for the state space model.");
            }
            for (int i = 0; i < n; i++) {
                String name = ((StringToken) _stateNames.getElement(i))
                        .stringValue().trim();

                if (name.equals("")) {
                    throw new IllegalActionException(this, "A state variable "
                            + "name should not be an empty string.");
                } 
                // Check state equations.
                String equation = name + "_update"; 
                if (this.getUserDefinedParameter(equation) == null) {
                    throw new IllegalActionException(
                            this,
                            "Please add a "
                                    + "parameter with name \""
                                    + equation
                                    + "\" that gives the state update expression for state "
                                    + name + ".");
                }
            }
        } else {
            throw new IllegalActionException(this, "No valid State Space Model association found!");
        }
    }

    /**
     * Check if the Actor is associated with a unique enabled StateSpaceModel. Ideally,
     * here, we would also be checking whether the enabled decorator provides the parameters
     * expected by the actor.
     * @throws IllegalActionException 
     */
    @Override
    public boolean validUniqueDecoratorAssociationExists() throws IllegalActionException {
        boolean found = false;
        for (Decorator d : this.decorators()) {
            if (d instanceof StateSpaceModel) {
                Parameter isEnabled = (Parameter) this.getDecoratorAttribute(d, "enable");
                if ( ((BooleanToken)isEnabled.getToken()).booleanValue()) {
                    if (!found) {
                        found = true;
                        _decorator = (StateSpaceModel) d;
                    } else {
                        throw new IllegalActionException(this, "A StateSpaceActor "
                                + "can be associated with exactly one StateSpaceModel "
                                + "at a time.");
                    }
                }
            }  
        }
        return found;
    }

    @Override
    protected Parameter getUserDefinedParameter(String eqnName) 
            throws IllegalActionException {

        if (_decorator != null) {
            Attribute attr = this.getDecoratorAttribute(_decorator,eqnName); 
            return ((Parameter)attr); 
        } else {
            throw new IllegalActionException("No decorator found!");
        }
    }  

    private StateSpaceModel _decorator;  
}
