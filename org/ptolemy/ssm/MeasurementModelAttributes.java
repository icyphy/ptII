package org.ptolemy.ssm; 


import java.util.ArrayList;
import java.util.List;

import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Decorator;  
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException; 
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

public class MeasurementModelAttributes extends MirrorDecoratorAttributes {

    /** Constructor to use when editing a model.
     *  @param target The object being decorated.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public MeasurementModelAttributes(NamedObj target, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(target, decorator); 
        _addedContainerParameters = new ArrayList<>();
    }

    public MeasurementModelAttributes(NamedObj target, String name)
            throws IllegalActionException, NameDuplicationException {
        super(target, name); 
        _addedContainerParameters = new ArrayList<>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    //    /** React to a change in an attribute.  If the attribute is
    //     *  <i>enable</i>, remember the value.
    //     *  @param attribute The attribute that changed.
    //     *  @exception IllegalActionException If the change is not acceptable
    //     *   to this container (not thrown in this base class).
    //     * @throws NameDuplicationException 
    //     */
    //    @Override
    //    public void attributeChanged(Attribute attribute)
    //            throws IllegalActionException{
    //        if (attribute == enable) {
    //            _enabled = ((BooleanToken) enable.getToken()).booleanValue();
    //            try {
    //                if (enabled()) { 
    //                    addStateSpaceVariablesToContainer();
    //                } else { 
    //                    removeStateSpaceVariablesFromContainer();
    //                }
    //            } catch (NameDuplicationException e) {
    //                throw new InternalErrorException(e);
    //            }
    //        }
    //        super.attributeChanged(attribute);
    //    } 
    



    public void addStateSpaceVariablesToContainer() {
        Parameter stateVariableNames = (Parameter) this.getAttribute("stateVariableNames");
        if (stateVariableNames != null) {
            try {
                if (stateVariableNames.getToken() != null) {
                    Token[] tokens = ((ArrayToken)stateVariableNames.getToken()).arrayValue();
                    for (Token t : tokens) {
                        String name = ((StringToken)t).stringValue();
                        Parameter containerParam = (Parameter) this.getContainer().getAttribute(name);
                        Parameter thisParam = (Parameter) this.getAttribute(name);

                        if (thisParam != null && containerParam == null) {
                            containerParam = new Parameter(this.getContainer(), name);
                            _addedContainerParameters.add(name);
                            containerParam.setExpression(thisParam.getExpression());
                            containerParam.setVisibility(Settable.NONE); 
                            thisParam.setVisibility(Settable.NONE);
                        } 
                    }
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            }
        }
    }
    
    
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
 
        MeasurementModelAttributes result = (MeasurementModelAttributes) super.clone(workspace); 
        result._addedContainerParameters = null;
        return result;
    }
    
    /**
     * Add all decorated ports to the container
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    @Override
    public void decorateContainer() {
        super.decorateContainer();
        addStateSpaceVariablesToContainer();

    }


    /**
     * Remove all decorated ports from the container
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public void removeDecorationsFromContainer() { 
        super.removeDecorationsFromContainer();
        removeStateSpaceVariablesFromContainer();
    }

    public void removeStateSpaceVariablesFromContainer() {
        Parameter stateVariableNames = (Parameter) this.getAttribute("stateVariableNames");
        if (stateVariableNames != null) {
            try {
                if (stateVariableNames.getToken() != null) {

                    Token[] tokens = ((ArrayToken)stateVariableNames.getToken()).arrayValue(); 
                    for (Token t : tokens) {
                        String name = ((StringToken)t).stringValue();
                        if (_addedContainerParameters.contains(name)) {
                            Parameter containerParam = (Parameter) this.getContainer().getAttribute(name); 
    
                            if (containerParam != null) {
                                this.getContainer().removeAttribute(containerParam);
                                _addedContainerParameters.remove(name);
                            } 
                        }
                    } 
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            } 
        }
    }

private List<String> _addedContainerParameters;

}

