package org.ptolemy.ssm;

import java.util.HashMap; 

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
implements InferenceActor {

    public ParticleFilterSSM(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _decorator = null;
        _measurementDecorators = new HashMap<>();
        // TODO Auto-generated constructor stub
    } 

    public ParticleFilterSSM(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace); 
        _decorator = null;
        _measurementDecorators = new HashMap<>();
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
            } else if (d instanceof GaussianMeasurementModel) {
                Parameter isEnabled = (Parameter) this.getDecoratorAttribute(d, "enable");
                if ( ((BooleanToken)isEnabled.getToken()).booleanValue()) {
                    _measurementDecorators.put(d.getName(),(GaussianMeasurementModel)d);
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

    /**
     * Return the first encountered value for now
     * FIXME
     * @param eqnName
     * @return
     * @throws IllegalActionException
     */
    @Override
    protected Parameter getMeasurementParameter(String fullName) 
            throws IllegalActionException {

        String[] completeName = fullName.split("_");  
        String decoratorName = completeName[0];
        String portName = "";
        for (int i = 1; i < completeName.length; i++) {
            portName += completeName[i];
        }
        GaussianMeasurementModel m = _measurementDecorators.get(decoratorName);
        if (m != null) {
            String postfix = m.getMeasurementParameterPostfix();
            Attribute attr = this.getDecoratorAttribute(m,portName+postfix); 

            if ( attr != null) {
                return ((Parameter)attr); 
            } else {
                throw new IllegalActionException("Specified parameter for: " +
                        portName + " not found in referred decorator " + decoratorName);
            }
        } else {
            throw new IllegalActionException("Decorator not found: " + decoratorName);
        }
    } 

    private StateSpaceModel _decorator;
    private HashMap<String,GaussianMeasurementModel> _measurementDecorators;
    @Override
    protected InputType getInputType(String inputName) {
        String[] nameStruct = inputName.split("_");
        if (nameStruct.length >= 2) {
            return InputType.MEASUREMENT_INPUT;
        } else {
            return InputType.CONTROL_INPUT;
        }
    }

    @Override
    protected Parameter getNoiseParameter(String fullName) throws IllegalActionException {
        String[] completeName = fullName.split("_");  
        String decoratorName = completeName[0]; 
        GaussianMeasurementModel m = _measurementDecorators.get(decoratorName);
        if (m != null) { 
            Attribute attr = this.getDecoratorAttribute(m,"noiseCovariance"); 

            if ( attr != null) {
                return ((Parameter)attr); 
            } else {
                throw new IllegalActionException("Specified parameter for noise"
                        + " not found in referred decorator " + decoratorName);
            }
        } else {
            throw new IllegalActionException("Decorator not found: " + decoratorName);
        }
    }

}
