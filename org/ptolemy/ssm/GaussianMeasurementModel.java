package org.ptolemy.ssm;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj; 


public class GaussianMeasurementModel extends MirrorDecorator 
implements StateSpaceActor {
    
    public GaussianMeasurementModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        _init();
    }
    /**
     * The measurement port
     */
    public TypedIOPort z;
    
    /**
     * The measurement port
     */
    public Parameter zParameter;
    
    /**
     * The noise mean
     */
    public Parameter noiseMean;
    
    /**
     * The noise covariance
     */
    public Parameter noiseCovariance;
    /**
     * The measurement equation that will refer to the state space model.
     */

    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof InferenceActor) {
            try {
                MeasurementModelAttributes ssa = new MeasurementModelAttributes(target, this);
                registerListener(ssa);
                return ssa;
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }
    @Override
    public boolean validUniqueDecoratorAssociationExists()
            throws IllegalActionException {
        boolean found = false;
        for (Decorator d : this.decorators()) {
            if (d instanceof StateSpaceModel) {
                Parameter isEnabled = (Parameter) this.getDecoratorAttribute(d, "enable");
                if ( ((BooleanToken)isEnabled.getToken()).booleanValue()) {
                    if (!found) {
                        found = true; 
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
    public List<NamedObj> decoratedObjects() throws IllegalActionException {
        if (workspace().getVersion() == _decoratedObjectsVersion) {
            return _decoratedObjects;
        }
        _decoratedObjectsVersion = workspace().getVersion();
        List<NamedObj> list = new ArrayList();
        CompositeEntity container = (CompositeEntity) getContainer();
        for (Object object : container.deepEntityList()) {
            if (object instanceof InferenceActor) {
                list.add((NamedObj)object); 
            }
        }
        _decoratedObjects = list;
        return list;
    }
//    @Override
//    protected void _addAttribute(Attribute attr) 
//            throws NameDuplicationException, IllegalActionException { 
//        if (attr == noiseCovariance) {
//            sendParameterEvent(DecoratorEvent.ADDED_PARAMETER, (Parameter)attr); 
//            _addedParameters.add((Parameter) attr); 
//        } else { 
//            super._addAttribute(attr);
//        }
//    }
    
    private void _init() throws IllegalActionException, NameDuplicationException {
        
        z = new TypedIOPort(this, "z", false, true);
        
        zParameter = new Parameter(this,"zParameter");
        zParameter.setDisplayName("z");
        zParameter.setExpression("");
        
        noiseMean = new Parameter(this, "noiseMean");
        noiseMean.setExpression("0.0");
        
        noiseCovariance = new Parameter(this, "noiseCovariance");
        noiseCovariance.setExpression("5.0"); 
    }
    
    public String getMeasurementParameterPostfix() {
        return MEASUREMENT_PARAMETER_POSTFIX;
    }

    private final String MEASUREMENT_PARAMETER_POSTFIX = "Parameter";
    

}
