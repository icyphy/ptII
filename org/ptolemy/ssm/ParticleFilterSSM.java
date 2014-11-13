package org.ptolemy.ssm;

import org.ptolemy.machineLearning.particleFilter.AbstractParticleFilter; 

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class ParticleFilterSSM extends AbstractParticleFilter 
implements StateSpaceActor {

    public ParticleFilterSSM(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _decorator = null;
        // TODO Auto-generated constructor stub
    } 

    public ParticleFilterSSM(Workspace workspace)
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

        for (Decorator d : this.decorators()) {
            if (d instanceof MirrorDecorator) {
                Parameter isEnabled = (Parameter) this.getDecoratorAttribute(d, "enable");
                if (((BooleanToken)isEnabled.getToken()).booleanValue()) {
                    _decorator = (StateSpaceModel) d;
                    Parameter stateVariableNames = (Parameter) this.getDecoratorAttribute(d, "stateVariableNames");
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
                }
            }
        } 
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
